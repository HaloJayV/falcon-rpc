package com.falcon.rpc.proxy;

import com.falcon.rpc.codec.RpcRequest;
import com.falcon.rpc.handler.RpcFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-20 15:59
 * @Description:
 */
public class ObjectProxy<T, P> implements InvocationHandler, RpcService<T, P, SerializableFunction<T>> {

    private static final Logger logger = LoggerFactory.getLogger(ObjectProxy.class);
    private Class<T> clazz;
    private String version;

    public ObjectProxy(Class<T> clazz, String version) {
        this.clazz = clazz;
        this.version = version;
    }



    @Override
    public RpcFuture call(String function, Objects... args) throws Exception {
        return null;
    }

    @Override
    public RpcFuture call(SerializableFunction<T> tSerializableFunction, Objects... args) throws Exception {
        return null;
    }

    // 动态代理客户端，强化执行客户端的请求方法
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            // 重写equals方法
            if("equals".equals(name)) {
                return proxy == args[0];
            } else if("hasCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if("toString".equals(name)){
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler" + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        request.setVersion(version);
        if(logger.isDebugEnabled()) {
            logger.debug(method.getDeclaringClass().getName());
            logger.debug(method.getName());
            for(int i = 0; i < method.getParameterTypes().length; ++i) {
                logger.debug(method.getParameterTypes()[i].getName());
            }
            for(int i = 0; i < args.length; i++) {
                logger.debug(args[i].toString());
            }
        }
    }
}