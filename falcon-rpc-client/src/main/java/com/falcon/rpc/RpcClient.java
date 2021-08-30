package com.falcon.rpc;

import com.falcon.rpc.discovery.ServiceDiscovery;
import com.falcon.rpc.proxy.ObjectProxy;
import com.falcon.rpc.proxy.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Proxy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-20 15:57
 * @Description:
 */
public class RpcClient implements ApplicationContextAware, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);
    // 服务发现
    private ServiceDiscovery serviceDiscovery;
    private static ThreadPoolExecutor threadPoolExecutor =
            new ThreadPoolExecutor(16, 16,
                    600L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));

    public RpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

    // 通过jdk动态代理创建服务代理都西昂
    @SuppressWarnings("unchecked")
    public static <T, P> T createService(Class<T> interfaceClass, String version) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ObjectProxy<T, P>(interfaceClass, version);
        )
    }

    public static<T, P>RpcService creqteAsyncService(Class<T> interfaceClass, String version) {
        return new ObjectProxy<T, P>(interfaceClass, version);
    }

    public void stop() {
        threadPoolExecutor.shutdown();
        serviceDiscovery.stop
    }

    @Override
    public void destroy() throws Exception {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }
}