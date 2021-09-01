package com.falcon.rpc.discovery;

import com.falcon.rpc.config.Constant;
import com.falcon.rpc.connect.ConnectionManager;
import com.falcon.rpc.protocol.RpcProtocol;
import com.falcon.rpc.zookeeper.CuratorClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-20 15:58
 * @Description:
 */
public class ServiceDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);
    private CuratorClient curatorClient;

    public ServiceDiscovery(CuratorClient curatorClient) {
        this.curatorClient = curatorClient;
        discoveryService();
    }

    private void discoveryService() {
        try {
            logger.info("Get initial service info");
            getServiceAndUpdateServer();
            // 添加注册中心的子节点的服务事件监听器
            curatorClient.watchPathChildrenNode(Constant.ZK_REGISTRY_PATH, new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                    // 子节点的缓存事件类型
                    PathChildrenCacheEvent.Type type = pathChildrenCacheEvent.getType();
                    ChildData childData = pathChildrenCacheEvent.getData();
                    switch (type) {
                        // 重新连接
                        case CONNECTION_RECONNECTED:
                            logger.info("Reconnected to zk, try to get latest service list");
                            getServiceAndUpdateServer();
                            break;
                        case CHILD_ADDED:
                            // 添加服务节点
                            getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_ADDED);
                        case CHILD_UPDATED:
                            getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_UPDATED);
                        case CHILD_REMOVED:
                            getServiceAndUpdateServer(childData, PathChildrenCacheEvent.Type.CHILD_REMOVED);
                            break;
                    }
                }
            });
        } catch (Exception e) {
            logger.error("Watch node exception: " + e.getMessage());
        }
    }

    /**
     * 获取并更新服务
     */
    private void getServiceAndUpdateServer() {
        try {
            // 获取注册中心下所有子节点
            List<String> nodeList = curatorClient.getChildren(Constant.ZK_REGISTRY_PATH);
            // 将注册中心下的二级子节点的数据RpcProtocol集合
            List<RpcProtocol> dataList = new ArrayList<>();
            for(String node : nodeList) {
                logger.debug("Service node:" + node);
                // 二级节点的数据
                byte[] bytes = curatorClient.getData(Constant.ZK_REGISTRY_PATH + "/" + node);
                String json = new String(bytes);
                RpcProtocol rpcProtocol = RpcProtocol.fromJson(json);
                dataList.add(rpcProtocol);
            }
            logger.debug("Service node data:{}", dataList);
            UpdateConnectedServer(dataList);
        } catch (Exception e) {
            logger.error("Get node exception:" + e.getMessage());
        }
    }

    private void getServiceAndUpdateServer(ChildData childData, PathChildrenCacheEvent.Type type) {
        String path = childData.getPath();
        String data = new String(childData.getData(), StandardCharsets.UTF_8);
        logger.info("Child data updated, path:{},type:{},data:{},", path, type, data);
        RpcProtocol rpcProtocol = RpcProtocol.fromJson(data);
        UpdateConnectedServer(rpcProtocol, type);


    }

    private void UpdateConnectedServer(RpcProtocol rpcProtocol, PathChildrenCacheEvent.Type type) {
        ConnectionManager.getInstance().updateConnectedServer(rpcProtocol, type);
    }

    /**
     * 更新服务的连接信息
     * @param dataList
     */
    private void UpdateConnectedServer(List<RpcProtocol> dataList) {
        ConnectionManager.getInstance().updateConnectedServer(dataList);
    }

    public void stop() {
        this.curatorClient.close();
    }

}