package com.falcon.rpc.route.impl;

import com.falcon.rpc.handler.RpcClientHandler;
import com.falcon.rpc.protocol.RpcProtocol;
import com.falcon.rpc.route.RpcLoadBalance;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-20 16:00
 * @Description: 轮询负载均衡调度算法
 */
public class RpcLoadBalanceRoundRobin extends RpcLoadBalance {

    private AtomicInteger roundRobin = new AtomicInteger(0);

    public RpcProtocol doRoute(List<RpcProtocol> addressList) {
        int size = addressList.size();
        // 节点取模索引
        int index = (roundRobin.getAndAdd(1) + size) % size;
        return addressList.get(index);
    }

    // 通过接口名和对应的服务方信息进行负载均衡
    @Override
    public RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws Exception {
        Map<String, List<RpcProtocol>> serviceMap = getServiceMap(connectedServerNodes);
        // 服务接口对应的服务提供方集合
        List<RpcProtocol> rpcProtocols = serviceMap.get(serviceKey);
        if(!CollectionUtils.isEmpty(rpcProtocols)) {
            return doRoute(rpcProtocols);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
    }
}