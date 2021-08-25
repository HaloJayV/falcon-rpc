package com.falcon.rpc.route.impl;

import com.falcon.rpc.handler.RpcClientHandler;
import com.falcon.rpc.protocol.RpcProtocol;
import com.falcon.rpc.route.RpcLoadBalance;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-20 16:00
 * @Description:
 */
public class RpcLoadBalanceRandom extends RpcLoadBalance {


    private Random random = new Random();

    @Override
    public RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws Exception {
        Map<String, List<RpcProtocol>> serviceMap = getServiceMap(connectedServerNodes);
        List<RpcProtocol> addressList = serviceMap.get(serviceKey);
        if(!CollectionUtils.isEmpty(addressList)) {
            return doRoute(addressList);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);

        }
    }

    private RpcProtocol doRoute(List<RpcProtocol> addressList) {
        int size = addressList.size();
        return addressList.get(random.nextInt());
    }
}