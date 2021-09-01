package com.falcon.rpc;

import com.falcon.rpc.annotation.NettyRpcService;
import com.falcon.rpc.core.NettyServer;
import org.apache.commons.collections4.MapUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-19 15:48
 * @Description: 提供RPC服务
 */
public class RpcServer extends NettyServer
        implements ApplicationContextAware, InitializingBean, DisposableBean {

    public RpcServer(String serverAddress, String registryAddress) {
        super(serverAddress, registryAddress);
    }


    @Override
    public void destroy() throws Exception {
        super.stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.start();
    }

    /**
     * 添加服务方的服务
     * @param ctx
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        // 获取当前全局容器中包含注解的bean，也就是服务方提供服务的方法接口对象
        // 带有NettyRpcService注解都表示提供的服务，在这里添加带注册中心里
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(NettyRpcService.class);
        if(MapUtils.isNotEmpty(serviceBeanMap)) {
            // 处理每个服务对象
            for(Object serviceBean : serviceBeanMap.values()) {
                NettyRpcService nettyRpcService = serviceBean.getClass().getAnnotation(NettyRpcService.class);
                // 获取该注解下的接口名、版本号
                String interfaceName = nettyRpcService.value().getName();
                String version = nettyRpcService.version();
                // 拼接信息再放到serviceBeanMap里
                super.addService(interfaceName, version, serviceBean);
            }
        }
    }
}