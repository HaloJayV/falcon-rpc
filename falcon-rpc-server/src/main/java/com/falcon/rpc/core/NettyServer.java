package com.falcon.rpc.core;

import com.falcon.rpc.registry.ServiceRegistry;
import com.falcon.rpc.util.ServiceUtil;
import com.falcon.rpc.util.ThreadPoolUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jdk.internal.dynalink.support.DefaultInternalObjectFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-19 15:49
 * @Description: 提供Netty通信服务
 */
public class NettyServer extends Server {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private Thread thread;
    // 服务地址
    private String serverAddress;
    // 服务注册中心
    private ServiceRegistry serviceRegistry;

    private Map<String, Object> serviceMap = new HashMap<>();

    public NettyServer(String serverAddress, String registryAddress) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = new ServiceRegistry(registryAddress);
    }

    // 添加服务
    public void addService(String interfaceName, String version, Object serviceBean) {
        logger.info("Adding service, interface:{}, version:{}, bean:{}",
                interfaceName, version, serviceBean);
        // 拼接服务接口名名和版本号的服务id
        String serviceKey = ServiceUtil.makeServiceKey(interfaceName, version);
        // 请求接口id为key，请求参数对象为val
        serviceMap.put(serviceKey, serviceBean);
    }

    public void start() {
        thread = new Thread(new Runnable() {
            // 配置线程池以及线程命名
            ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.makeServerThreadPool(
                    NettyServer.class.getSimpleName(), 16, 32
            );

            @Override
            public void run() {
                // 处理连接
                NioEventLoopGroup bossGroup = new NioEventLoopGroup();
                // 处理事件
                NioEventLoopGroup workGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup, workGroup)
                            // 设置服务端的channel，服务端启动后进行初始化
                            .channel(NioServerSocketChannel.class)
                            // Socket参数，服务端接受连接的队列长度，如果队列已满，客户端连接将被拒绝。默认值，Windows为200，其他为128。
                            .option(ChannelOption.SO_BACKLOG, 128)
                            // 设置客户端的channel，客户端连接之后才进行初始化
                            // 创建通道初始化对象
                            // 事件处理器
                            .childHandler(new RpcServerInitializer(serviceMap, threadPoolExecutor))
                            // Socket参数，连接保活，默认值为False。启用该功能时，TCP会主动探测空闲连接的有效性。可以将此功能视为TCP的心跳机制，需要注意的是：默认的心跳间隔是7200s即2小时。Netty默认关闭该功能。
                            .childOption(ChannelOption.SO_KEEPALIVE, true);
                    // socket，ip：port
                    String[] array = serverAddress.split(":");
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);
                    // 绑定服务方的ip、端口
                    ChannelFuture future = bootstrap.bind(host, port).sync();
                    if(serviceRegistry != null) {
                        // 服务注册
                        serviceRegistry.registerService(host, port, serviceMap);
                    }
                    logger.info("Server started on port {}", port);
                    // 同步关闭通道
                    future.channel().closeFuture().sync();
                } catch (Exception e) {
                    if(e instanceof InterruptedException) {
                        logger.info("Rpc Server remoting server stop");
                    } else {
                        logger.error("Rpc server remoting server error", e);
                    }
                } finally {
                    try {
                        serviceRegistry.unregisterService();
                        workGroup.shutdownGracefully();
                        bossGroup.shutdownGracefully();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
        thread.start();
    }

    public void stop() throws Exception {
        if(thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }
}