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
     * 初始化通道对象
     */
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
//        Serializer serializer = ProtostuffSerializer.class.newInstance();
//        Serializer serializer = HessianSerializer.class.newInstance();
        Serializer serializer = KryoSerializer.class.newInstance();
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new IdleStateHandler(0, 0, Beat.BEAT_TIMEOUT, TimeUnit.SECONDS));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        pipeline.addLast(new RpcDecoder(RpcRequest.class, serializer));
        pipeline.addLast(new RpcEncoder(RpcResponse.class, serializer));
        pipeline.addLast(new RpcServerHandler(handlerMap, threadPoolExecutor));
    }
}