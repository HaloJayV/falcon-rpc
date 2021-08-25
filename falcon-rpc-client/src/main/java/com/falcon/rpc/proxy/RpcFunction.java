package com.falcon.rpc.proxy;

import java.util.Objects;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-20 15:59
 * @Description: 函数式接口
 */
@FunctionalInterface
public interface RpcFunction<T, P> extends SerializableFunction<T> {
    Object apply(T t, P p);
}