package com.falcon.rpc.route.impl;

import com.falcon.rpc.handler.RpcClientHandler;
import com.falcon.rpc.protocol.RpcProtocol;
import com.falcon.rpc.route.RpcLoadBalance;
import com.sun.scenario.effect.impl.prism.PrCropPeer;
import org.apache.commons.collections4.CollectionUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-20 16:00
 * @Description:
 */
public class RpcLoadBalanceLFU extends RpcLoadBalance {

    // 根据服务方使用频率
    private ConcurrentMap<String, HashMap<RpcProtocol, Integer>> jobLfuMap = new ConcurrentHashMap<String, HashMap<RpcProtocol, Integer>>();
    private long CACHE_VALUE_TIME = 0;


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
        // 缓存过期
        if(System.currentTimeMillis() > CACHE_VALUE_TIME) {
            jobLfuMap.clear();
            CACHE_VALUE_TIME = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        }
        HashMap<RpcProtocol, Integer> lfuItemMap = jobLfuMap.get(serviceKey);
        if(lfuItemMap == null) {
            lfuItemMap = new HashMap<>();
            jobLfuMap.putIfAbsent(serviceKey, lfuItemMap);
        }
        // 新服务put
        for(RpcProtocol address : addressList) {
            if(!lfuItemMap.containsKey(address) || lfuItemMap.get(address) > 1000000) {
                lfuItemMap.put(address, 0);
            }
        }

        // 服务被remove
        List<RpcProtocol> delKeys = new ArrayList<>();
        for(RpcProtocol existKey : lfuItemMap.keySet()) {
            if(!addressList.contains(existKey)) {
                delKeys.add(existKey);
            }
        }
        if(delKeys.size() > 0) {
            for(RpcProtocol delKey : delKeys) {
                lfuItemMap.remove(delKey);
            }
        }
        List<Map.Entry<RpcProtocol, Integer>> lfuItemList = new ArrayList<>(lfuItemMap.entrySet());
        Collections.sort(lfuItemList, new Comparator<Map.Entry<RpcProtocol, Integer>>() {
            @Override
            public int compare(Map.Entry<RpcProtocol, Integer> o1, Map.Entry<RpcProtocol, Integer> o2) {
                // 按照使用频次，从小到大排序
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        //最不常用
        Map.Entry<RpcProtocol, Integer> addressItem = lfuItemList.get(0);
        RpcProtocol minAddress = addressItem.getKey();
        addressItem.setValue(addressItem.getValue() + 1);
        return minAddress;
    }
}