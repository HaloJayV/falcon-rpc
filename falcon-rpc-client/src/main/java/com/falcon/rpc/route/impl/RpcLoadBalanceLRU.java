package com.falcon.rpc.route.impl;

import com.falcon.rpc.handler.RpcClientHandler;
import com.falcon.rpc.protocol.RpcProtocol;
import com.falcon.rpc.route.RpcLoadBalance;
import org.apache.commons.collections4.CollectionUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-20 16:00
 * @Description:
 */
public class RpcLoadBalanceLRU extends RpcLoadBalance {

    private ConcurrentHashMap<String, LinkedHashMap<RpcProtocol, RpcProtocol>> jobLRUMap
            = new ConcurrentHashMap<>();
    private long CACHE_VALID_TIME = 0;



    @Override
    public RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws Exception {
        Map<String, List<RpcProtocol>> serviceMap = getServiceMap(connectedServerNodes);
        List<RpcProtocol> addressList = serviceMap.get(serviceKey);
        if(!CollectionUtils.isEmpty(addressList)) {
            return doRoute(serviceKey, addressList);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
    }

    private RpcProtocol doRoute(String serviceKey, List<RpcProtocol> addressList) {
        // 超过有效时间
        if(System.currentTimeMillis() > CACHE_VALID_TIME) {
            jobLRUMap.clear();
            // 有效时间一天
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        }
        // 根据使用时间，用哈希链表实现按时间排序
        LinkedHashMap<RpcProtocol, RpcProtocol> lruHashMap = jobLRUMap.get(serviceKey);
        if(lruHashMap == null) {
            lruHashMap = new LinkedHashMap<RpcProtocol, RpcProtocol>(16, 0.75f, true) {
                // LRU策略
                @Override
                protected boolean removeEldestEntry(Map.Entry<RpcProtocol, RpcProtocol> eldest) {
                    if(super.size() > 1000) {
                        return true;
                    }
                    return false;
                }
            };
            jobLRUMap.putIfAbsent(serviceKey, lruHashMap);
        }
        // put new
        for(RpcProtocol address : addressList) {
            if(!lruHashMap.containsKey(address)) {
                lruHashMap.put(address, address);
            }
        }
        // remove old
        ArrayList<RpcProtocol> delKeys = new ArrayList<>();
        for(RpcProtocol existKey : lruHashMap.keySet()) {
            if(!addressList.contains(existKey)) {
                delKeys.add(existKey);
            }
        }
        if(delKeys.size() > 0){
            for(RpcProtocol delKey : delKeys) {
                lruHashMap.remove(delKey);
            }
        }
        // load balance
        RpcProtocol eldestKey = lruHashMap.entrySet().iterator().next().getKey();
        return lruHashMap.get(eldestKey);
    }
}