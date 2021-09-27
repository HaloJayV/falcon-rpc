package com.falcon.rpc.core;

import com.esotericsoftware.kryo.KryoSerializable;
import com.falcon.rpc.codec.*;
import com.falcon.rpc.serializer.Serializer;
import com.falcon.rpc.serializer.hessian.HessianSerializer;
import com.falcon.rpc.serializer.kryo.KryoSerializer;
import com.falcon.rpc.serializer.protostuff.ProtostuffSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-19 17:25
 * @Description: 创建通道初始化对象
 */
public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {

    private Map<String, Object> handlerMap;
    private ThreadPoolExecutor threadPoolExecutor;

    public RpcServerInitializer(Map<String, Object> handlerMap, ThreadPoolExecutor threadPoolExecutor) {
        this.handlerMap = handlerMap;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    /**
     * 初始化通道对象，设置通道的IO事件处理器链
     * 1、通道pipeline（一个双向链表）依次在尾部加入处理链
     * 服务端的ChannelHandler一般是ChannelInBoundHandler
     */
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
//        Serializer serializer = ProtostuffSerializer.class.newInstance();
//        Serializer serializer = HessianSerializer.class.newInstance();
        Serializer serializer = KryoSerializer.class.newInstance();
        // ChannelHandler的实例链，双向链表
        ChannelPipeline pipeline = channel.pipeline();
        // 因为服务端是读取请求，因此是添加ChannelInboundHandler事件处理器，先添加的先执行
        // 空闲状态处理器
        pipeline.addLast(new IdleStateHandler(0, 0, Beat.BEAT_TIMEOUT, TimeUnit.SECONDS));
        // 解决解码时出现的毡包拆包的一个解码器
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        // 在服务端设置对请求体的解码和对响应体的编码
        pipeline.addLast(new RpcDecoder(RpcRequest.class, serializer));
        pipeline.addLast(new RpcEncoder(RpcResponse.class, serializer));
        // 设置服务端对应的事件处理器
        pipeline.addLast(new RpcServerHandler(handlerMap, threadPoolExecutor));


    }
}