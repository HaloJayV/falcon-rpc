package com.falcon.rpc.connect;

import com.falcon.rpc.RpcClient;
import com.falcon.rpc.handler.RpcClientHandler;
import com.falcon.rpc.handler.RpcClientInitializer;
import com.falcon.rpc.protocol.RpcProtocol;
import com.falcon.rpc.protocol.RpcServiceInfo;
import com.falcon.rpc.route.RpcLoadBalance;
import com.falcon.rpc.route.impl.RpcLoadBalanceRoundRobin;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import jdk.internal.dynalink.support.TypeUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Handler;
import java.util.stream.Collectors;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-20 15:58
 * @Description:  连接管理器对象
 */
public class ConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    // Selector事件连接器组，最多同时有4个事件处理器工作
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            4, 8, 600L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(1000)
    );
    // 服务节点映射，key为服务对象，val为服务方对当前服务方法对应的处理信息
    private Map<RpcProtocol, RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();
    // 存放连接中的服务集合
    private CopyOnWriteArraySet<RpcProtocol> rpcProtocolSet = new CopyOnWriteArraySet<>();
    private ReentrantLock lock = new ReentrantLock();
    // 获得当前锁的信号
    private Condition connected = lock.newCondition();
    private long waitTimeout = 5000;
    private RpcLoadBalance loadBalance = new RpcLoadBalanceRoundRobin();
    private volatile boolean isRunning = true;

    public ConnectionManager() {
    }


    // 获取连接管理器对象
    private static class SingletonHolder {
        private static final ConnectionManager instance = new ConnectionManager();
    }

    public static ConnectionManager getInstance() {
        return SingletonHolder.instance;
    }

    // 批量更新连接状态信息
    public void updateConnectedServer(List<RpcProtocol> serviceList) {
        if(serviceList != null && serviceList.size() > 0) {
            HashSet<RpcProtocol> serviceSet = new HashSet<>(serviceList.size());
            for(RpcProtocol rpcProtocol : serviceList) {
                serviceSet.add(rpcProtocol);
            }

            for(final RpcProtocol rpcProtocol : serviceSet) {
                // 连接池中不包含该服务
                if(!rpcProtocolSet.contains(rpcProtocol)) {
                    // 未连接，则连接服务
                    connectServerNode(rpcProtocol);
                }
            }

            for(RpcProtocol rpcProtocol : rpcProtocolSet) {
                // 不包含在连接池中
                if(!serviceSet.contains(rpcProtocol)) {
                    logger.info("Remove invalid service:" + rpcProtocol.toJson());
                    removeAndCloseHandler(rpcProtocol);
                }
            }
        } else {
            logger.error("No available service");
            for(RpcProtocol rpcProtocol : rpcProtocolSet) {
                removeAndCloseHandler(rpcProtocol);
            }
        }
    }

    // 监听子节点服务的变化，更新状态
    public void updateConnectedServer(RpcProtocol rpcProtocol, PathChildrenCacheEvent.Type type) {
        if(rpcProtocol == null) {
            return;
        }
        // 子节点加入到服务池里
        if(type == PathChildrenCacheEvent.Type.CHILD_ADDED && !rpcProtocolSet.contains(rpcProtocol)) {
            connectServerNode(rpcProtocol);
        } else if(type == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
            // 更新子节点
            removeAndCloseHandler(rpcProtocol);
            connectServerNode(rpcProtocol);
        } else if(type == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
            removeAndCloseHandler(rpcProtocol);
        } else {
            throw new IllegalStateException("Unknow type:" + type);
        }
    }

    // 通过ip和端口连接服务节点
    private void connectServerNode(RpcProtocol rpcProtocol) {
        // 不存在服务
        if(rpcProtocol.getServiceInfoList() == null || rpcProtocol.getServiceInfoList().isEmpty()) {
            logger.info("No service on node, host: {}, port: {}", rpcProtocol.getHost(), rpcProtocol.getPort());
            return;
        }
        rpcProtocolSet.add(rpcProtocol);
        logger.info("New service node, host: {}, port: {}", rpcProtocol.getHost(), rpcProtocol.getPort());
        for(RpcServiceInfo serviceProtocol : rpcProtocol.getServiceInfoList()) {
            logger.info("New service info, name: {}, version: {}", serviceProtocol.getServiceName(), serviceProtocol.getVersion());
        }
        final InetSocketAddress remotePeer =
                new InetSocketAddress(rpcProtocol.getHost(), rpcProtocol.getPort());
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Bootstrap bootstrap = new Bootstrap();
                // 启动通道并配置
                bootstrap.group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new RpcClientInitializer());
                ChannelFuture channelFuture = bootstrap.connect(remotePeer);
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        // 事件处理完成
                        if(channelFuture.isSuccess()) {
                            logger.info("Successfully connect to remote server, remote peer = " + remotePeer);
                            // 获取事件处理的通道处理器
                            RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                            // 添加到已经处理过的map中
                            connectedServerNodes.put(rpcProtocol, handler);
                            handler.setRpcProtocol(rpcProtocol);
                            // 线程执行完成，通过信号量通知可以执行的处理
                            signalAvailableHandler();
                         } else {
                            logger.error("Can not connect to remote server, remote peer = " + remotePeer);
                        }
                    }
                });
            }
        });
    }

    // 通知所有线程执行
    private void signalAvailableHandler() {
        lock.lock();
        try {
            // 通知线程执行
            connected.signalAll();
        } finally {
            lock.lock();
        }
    }

    // 等待处理
    private boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            logger.warn("Waiting for available service");
            return connected.await(this.waitTimeout, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public RpcClientHandler chooseHandler(String serviceKey) throws Exception {
        int size = connectedServerNodes.values().size();
        while(isRunning && size <= 0) {
            try {
                waitingForHandler();
                size = connectedServerNodes.values().size();
            } catch (InterruptedException e) {
                logger.error("Waiting for available service is interrupted!", e);
            }
        }
        // 负载均衡，选择服务节点
        RpcProtocol rpcProtocol = loadBalance.route(serviceKey, connectedServerNodes);
        // 获取响应的处理器
        RpcClientHandler handler = connectedServerNodes.get(rpcProtocol);
        if(!Objects.isNull(handler)) {
            return handler;
        } else {
            throw new Exception("Can not get available connection");
        }
    }

    private void removeAndCloseHandler(RpcProtocol rpcProtocol) {
        RpcClientHandler handler = connectedServerNodes.get(rpcProtocol);
        if (!Objects.isNull(handler)) {
            handler.close();
        }
        connectedServerNodes.remove(rpcProtocol);
        rpcProtocolSet.remove(rpcProtocol);
    }

    public void removeHandler(RpcProtocol rpcProtocol) {
        rpcProtocolSet.remove(rpcProtocol);
        connectedServerNodes.remove(rpcProtocol);
        logger.info("Remove one connection, host: {}, port: {}", rpcProtocol.getHost(), rpcProtocol.getPort());
    }

    public void stop() {
        isRunning = false;
        for(RpcProtocol rpcProtocol : rpcProtocolSet) {
            removeAndCloseHandler(rpcProtocol);
        }
        signalAvailableHandler();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }

}