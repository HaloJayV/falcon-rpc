package com.falcon.rpc.route;

import com.falcon.rpc.handler.RpcClientHandler;
import com.falcon.rpc.protocol.RpcProtocol;
import com.falcon.rpc.protocol.RpcServiceInfo;
import com.falcon.rpc.util.ServiceUtil;
import org.apache.commons.collections4.CollectionUtils;

import javax.print.ServiceUI;
import java.util.*;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-20 16:00
 * @Description:
 */
public abstract class RpcLoadBalance {
    // 负载均衡
    public abstract RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws Exception;
    // 获取当前所有服务接口和对应的服务方节点，才能进行之后的负载均衡路由操作route
    protected Map<String, List<RpcProtocol>> getServiceMap(Map<RpcProtocol, RpcClientHandler> connectedServerNodes) {
        // key为服务接口，value为对应的服务提供方集合
        HashMap<String, List<RpcProtocol>> serviceMap = new HashMap<>();
        if(connectedServerNodes != null && connectedServerNodes.size() > 0) {
            // 所有服务提供方
            for(RpcProtocol rpcProtocol : connectedServerNodes.keySet()) {
                // 服务方对应的所有rpc服务
                for(RpcServiceInfo serviceInfo : rpcProtocol.getServiceInfoList()) {
                    String serviceKey = ServiceUtil.makeServiceKey(serviceInfo.getServiceName(), serviceInfo.getVersion());
                    // 通过服务接口名获取对应所有服务方
                    List<RpcProtocol> rpcProtocolList = serviceMap.get(serviceKey);
                    if(CollectionUtils.isEmpty(rpcProtocolList)) {
                        rpcProtocolList = new ArrayList<>();
                    }
                    // 服务方集合
                    rpcProtocolList.add(rpcProtocol);
                    // 服务接口id对应的服务方集合
                    serviceMap.putIfAbsent(serviceKey, rpcProtocolList);
                }
            }
        }
        return serviceMap;
    }
}