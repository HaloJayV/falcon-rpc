package com.falcon.rpc.proxy;

import com.falcon.rpc.handler.RpcFuture;
import sun.plugin2.message.transport.SerializingTransport;

/**
 * @Auther: JayV
 * @Date: 2020-8-18 15:28
 * @Description:
 */
public interface RpcService<T, P, FN extends SerializingTransport<T>> {

    RpcFuture

}
