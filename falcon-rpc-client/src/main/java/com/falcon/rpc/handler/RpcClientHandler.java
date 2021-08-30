package com.falcon.rpc.handler;

import com.falcon.rpc.codec.Beat;
import com.falcon.rpc.codec.RpcRequest;
import com.falcon.rpc.codec.RpcResponse;
import com.falcon.rpc.connect.ConnectionManager;
import com.falcon.rpc.protocol.RpcProtocol;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlTransient;
import java.awt.image.RescaleOp;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-20 15:58
 * @Description: 客户端处理器，绑定服务通道
 */
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);

    // key为请求id，val为请求对应的回调对象RpcFuture
    private ConcurrentHashMap<String, RpcFuture> pendingRPC = new ConcurrentHashMap<>();
    private volatile Channel channel;
    // 序列号
    private SocketAddress remotePeer;
    // 服务方协议对象
    private RpcProtocol rpcProtocol;

    // 绑定服务地址，激活通道，开始与服务端远距离通信
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        // 绑定当前通道channel的地址
        this.remotePeer = this.channel.remoteAddress();
    }

    // 注册当前通道到上下文容器中
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    public RpcClientHandler() {
    }

    // 通过rpc响应对象读取到服务返回信息
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        String requestId = response.getRequestId();
        logger.debug("Receive response: " + requestId);
        RpcFuture rpcFuture = pendingRPC.get(requestId);
        // 表明该响应已经返回回调函数
        if(Objects.isNull(rpcFuture)) {
            // 更新服务的回调状态
            pendingRPC.remove(requestId);
            rpcFuture.done(response);
        } else {
            logger.warn("Can not get pending response for request id: " + requestId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Client caught exception: " + cause.getMessage());
        ctx.channel();
    }

    public void close(){
        // 关闭通道前将通道里的数据写出来
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    // 向服务方发送rpc请求
    public RpcFuture sendRequest(RpcRequest request) {
        RpcFuture rpcFuture = new RpcFuture(request);
        pendingRPC.put(request.getRequestId(), rpcFuture);
        try {
            // 将请求写到通道里，等待被处理，先返回回调对象
            ChannelFuture channelFuture = channel.writeAndFlush(request).sync();
            if(!channelFuture.isSuccess()) {
                logger.error("Send request {} error", request.getRequestId());
            }
        } catch (InterruptedException e) {
            logger.error("Send request exception: " + e.getMessage());
        }
        return rpcFuture;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            // 用事件触发
            sendRequest(Beat.BEAT_PING);
            logger.debug("Client send beat-ping to " + remotePeer);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    public void setRpcProtocol(RpcProtocol rpcProtocol) {
        this.rpcProtocol = rpcProtocol;
    }

    // 通道激活取消
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 操作容器，取消与服务方的通道
        super.channelInactive(ctx);
        ConnectionManager.getInstance().removeHandler(rpcProtocol);
    }
}