package com.falcon.rpc.registry;

import com.falcon.rpc.config.Constant;
import com.falcon.rpc.protocol.RpcProtocol;
import com.falcon.rpc.protocol.RpcServiceInfo;
import com.falcon.rpc.util.ServiceUtil;
import com.falcon.rpc.zookeeper.CuratorClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-19 15:54
 * @Description: 服务注册中心
 */
public class ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

    private CuratorClient curatorClient;
    private List<String> pathList = new ArrayList<>();

    public ServiceRegistry(String registryAddress) {
        // 客户端连接到注册中心
        this.curatorClient = new CuratorClient(registryAddress, 5000);
    }

    // 注册新的服务到注册中心
    public void registerService(String host, int port, Map<String, Object> serviceMap) {
        // 注册中心的所服务元数据信息集合
        List<RpcServiceInfo> serviceInfoList = new ArrayList<>();
        for(String key : serviceMap.keySet()) {
            // 元数据信息数组
            String[] serviceInfo = key.split(ServiceUtil.SERVICE_CONCAT_TOKEN);
            if(serviceInfo.length > 0) {
                RpcServiceInfo rpcServiceInfo = new RpcServiceInfo();
                rpcServiceInfo.setServiceName(serviceInfo[0]);
                if(serviceInfo.length == 2) {
                    rpcServiceInfo.setVersion(serviceInfo[1]);
                } else {
                    rpcServiceInfo.setVersion("");
                }
                logger.info("Register new service:{}", key);
                serviceInfoList.add(rpcServiceInfo);
            } else {
                logger.warn("Can't get service name and version:{}", key);
            }
        }
        try {
            RpcProtocol rpcProtocol = new RpcProtocol();
            rpcProtocol.setHost(host);
            rpcProtocol.setPort(port);
            rpcProtocol.setServiceInfoList(serviceInfoList);
            String serviceData = rpcProtocol.toJson();
            byte[] bytes = serviceData.getBytes();
            // 拼接zookeeper节点路径，子节点为节点对象哈希值
            String path = Constant.ZK_DATA_PATH + "-" + rpcProtocol.hashCode();
            path = this.curatorClient.createPathData(path, bytes);
            pathList.add(path);
            logger.info("Register {} new service, host: {}, port: {}", serviceInfoList.size(), host, port);
        } catch (Exception e) {
            logger.error("Register service fail, exception: {}", e.getMessage());
        }

        // 需要在注册中心对每个节点添加节点状态监听器
        curatorClient.addConnectionStateListener(new ConnectionStateListener() {
            // 节点发生改变就需要重新注册
            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                if(connectionState == ConnectionState.RECONNECTED) {
                    logger.info("Connection state:{}, register service after reconnected", connectionState);
                    registerService(host, port, serviceMap);
                }
            }
        });
    }

    // 注销节点
    public void unregisterService() {
        logger.info("unregister all service");
        for(String path : pathList) {
            try {
                this.curatorClient.deletePath(path);
            } catch (Exception e) {
                logger.error("Delete service path error:{}", e.getMessage());
            }
        }
        this.curatorClient.close();
    }
}