package com.falcon.rpc.proxy;

import com.falcon.rpc.handler.RpcFuture;
import sun.plugin2.message.transport.SerializingTransport;

import java.util.Objects;

/**
 * @Auther: JayV
 * @Date: 2020-8-18 15:28
 * @Description:
 */
public interface RpcService<T, P, FN extends SerializableFunction<T>> {

    RpcFuture call(String function, Objects... args) throws Exception;

    RpcFuture call(FN fn, Objects... args) throws Exception;

}
