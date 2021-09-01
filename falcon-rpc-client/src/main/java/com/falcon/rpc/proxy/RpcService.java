package com.falcon.rpc.proxy;

import com.falcon.rpc.handler.RpcFuture;

/**
 * @Auther: JayV
 * @Date: 2020-8-18 15:28
 * @Description:
 */
public interface RpcService<T, P, FN extends SerializableFunction<T>> {

    RpcFuture call(String function, Object... args) throws Exception;

    RpcFuture call(FN fn, Object... args) throws Exception;

}
