package com.falcon.rpc.core;

import com.falcon.rpc.codec.Beat;
import com.falcon.rpc.codec.RpcRequest;
import com.falcon.rpc.codec.RpcResponse;
import com.falcon.rpc.util.ServiceUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import net.sf.cglib.reflect.FastClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-19 19:24
 * @Description: 通过读通道继续读取数据
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);
    // serviceKey为key，当前rpc请求的服务对象为value
    private final Map<String, Object> handlerMap;
    private final ThreadPoolExecutor serverHandlerPool;

    public RpcServerHandler(Map<String, Object> handlerMap, ThreadPoolExecutor serverHandlerPool) {
        this.handlerMap = handlerMap;
        this.serverHandlerPool = serverHandlerPool;
    }

    // 读取客户端发送的数据RpcRequest，并转发给其他在线客户端
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        // filter beat ping
        // 请求前进行服务的心跳检测
        if(Beat.BEAT_ID.equalsIgnoreCase(request.getRequestId())) {
            logger.info("Server read heartbeat ping");
            return;
        }
        // 处理读取到的请求事件
        serverHandlerPool.execute(new Runnable() {
            @Override
            public void run() {
                logger.info("Receive request " + request.getRequestId());
                RpcResponse response = new RpcResponse();
                response.setRequestId(request.getRequestId());
                try {
                    // 获取服务调用后返回的结果
                    Object result = handle(request);
                    response.setResult(result);
                } catch (Throwable t) {
                    response.setError(t.toString());
                    logger.error("RPC Server handle request error", t);
                }
                // 读取response响应信息到ChannelHandlerContext通道上下文容器环境中
                // 添加监听器，响应成功后打印日志
                ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        logger.error("Send response for request " + request.getRequestId());
                    }
                });
            }
        });
    }

    // 处理请求后，返回客户端RPC请求对应的响应对象
    public Object handle(RpcRequest request) throws Throwable {
        String className = request.getClassName();
        String version = request.getVersion();
        String serviceKey = ServiceUtil.makeServiceKey(className, version);
        // 当前rpc请求的服务对象， 通过接口名称+版本号去获取对应的服务接口对象
        Object serviceBean = handlerMap.get(serviceKey);
        if(Objects.isNull(serviceBean)) {
            logger.error("Can not find service implement with interface name: {} and version: {}", className, version);
            return null;
        }
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        logger.debug(serviceClass.getName());
        logger.debug(methodName);
        for(int i = 0; i < parameterTypes.length; i++) {
            logger.debug(parameterTypes[i].getName());
        }
        for(int i = 0; i < parameters.length; ++i) {
            logger.debug(parameters[i].toString());
        }

        // JDK reflect
        // 根据参数类型和方法名获 取方法对象，
//        Method method = serviceClass.getMethod(methodName, parameterTypes);
//        method.setAccessible(true);
//        return method.invoke(serviceBean, parameters);

        // 通过jdk或cglib进行动态代理，通过
        // Cglib reflect，通过类对象获取到FastClass（相当于服务端的接口集合），通过方法名、参数类型、参数和接口对象执行对应接口方法
        FastClass serviceFastClass = FastClass.create(serviceClass);
//        FastMethod method = serviceFastClass.getMethod(methodName, parameterTypes);
//        return method.invoke(serviceBean, parameters);
        // 方法对象
        int methodIndex = serviceFastClass.getIndex(methodName, parameterTypes);
        // 通过方法对象、接口Bean、参数进行反射
        return serviceFastClass.invoke(methodIndex, serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Server caught exception" + cause.getMessage());
        ctx.close();
    }

    // event从ctx的pipeline执行链中寻找，Inbound 执行顺序，由上到下。Outbound执行顺序由下到上
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            // 空闲事件
            ctx.channel().close();
            logger.warn("Channel idle in last {} seconds, close it", Beat.BEAT_TIMEOUT);
        } else {
            // 触发事件
            super.userEventTriggered(ctx, evt);
        }
    }
}