package com.falcon.rpc.route;

import com.falcon.rpc.handler.RpcClientHandler;
import com.falcon.rpc.protocol.RpcProtocol;

import java.util.Map;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-20 16:00
 * @Description:
 */
public class RpcLoadBalance {
    public abstract RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes);
}