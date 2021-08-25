package com.falcon.rpc.proxy;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-20 15:59
 * @Description:
 */
@FunctionalInterface
public interface RpcFunction2<T, P1, P2> extends SerializableFunction<T> {
    Object apply(T t, P1 p1, P2 p2);
}