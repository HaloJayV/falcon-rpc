package com.falcon.rpc.handler;

import com.falcon.rpc.codec.*;
import com.falcon.rpc.serializer.hessian.HessianSerializer;
import com.falcon.rpc.serializer.kryo.KryoSerializer;
import com.falcon.rpc.serializer.protostuff.ProtostuffSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.zookeeper.KeeperException;

import java.util.concurrent.TimeUnit;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-20 15:59
 * @Description: rpc客户端初始化
 */
public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {

    // 初始化通道
    // 绑定当前socket通道所需要执行的netty通道处理链
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
//        ProtostuffSerializer serializer = ProtostuffSerializer.class.newInstance();
//        HessianSerializer serializer = HessianSerializer.class.newInstance();
        KryoSerializer serializer = KryoSerializer.class.newInstance();
        // netty链, 双向链表，按顺序放入要处理的内容
        ChannelPipeline cp = socketChannel.pipeline();
        // 设置心跳
        cp.addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL, TimeUnit.SECONDS));
        // 客户端要先对请求对象进行初始化才能远程请求传输
        cp.addLast(new RpcEncoder(RpcRequest.class, serializer));
        // 解码器自定义长度解决TCP粘包黏包问题
        cp.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4, 0, 0));
        cp.addLast(new RpcDecoder(RpcResponse.class, serializer));
        // 绑定客户端要处理器
        cp.addLast(new RpcClientHandler());
    }
}