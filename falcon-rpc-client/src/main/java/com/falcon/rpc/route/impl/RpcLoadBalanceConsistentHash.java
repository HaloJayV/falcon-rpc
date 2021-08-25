package com.falcon.rpc.route.impl;

import com.falcon.rpc.handler.RpcClientHandler;
import com.falcon.rpc.protocol.RpcProtocol;
import com.falcon.rpc.route.RpcLoadBalance;
import com.google.common.hash.Hashing;

import java.util.List;
import java.util.Map;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-20 16:00
 * @Description: 哈希负载均衡
 */
public class RpcLoadBalanceConsistentHash extends RpcLoadBalance {


    @Override
    public RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws Exception {
        Map<String, List<RpcProtocol>> serviceMap = getServiceMap(connectedServerNodes);
        List<RpcProtocol> addressList = serviceMap.get(serviceKey);
        if (addressList != null && addressList.size() > 0) {
            return doRoute(serviceKey, addressList);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
    }

    private RpcProtocol doRoute(String serviceKey, List<RpcProtocol> addressList) {
        // 接口id在哈希桶的索引
        int index = Hashing.consistentHash(serviceKey.hashCode(), addressList.size());
        return addressList.get(index);
    }
}