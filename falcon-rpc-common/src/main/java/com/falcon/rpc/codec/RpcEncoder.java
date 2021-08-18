package com.falcon.rpc.codec;

import com.falcon.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-4-25 20:06
 * @Description: 编码器
 */
public class RpcEncoder extends MessageToByteEncoder {
    private static final Logger logger = LoggerFactory.getLogger(RpcEncoder.class);
    // 泛型类
    private Class<?> genericClass;
    // 序列化器
    private Serializer serializer;

    public RpcEncoder(Class<?> genericClass, Serializer serializer) {
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    /**
     *
     * @param ctx
     * @param in
     * @param out
     * @throws Exception
     * 将被编码对象in进行序列化为字节数组后，写到输出缓冲流中
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        // obj.instanceof(class), 这个对象是不是这种类型
        // class.inInstance(obj), 这个对象能不能被转化为这个类
        // 对象genericClass是in类型, 可能是接口、类对象、父类或接口的对象
        if(genericClass.isInstance(in)) {
            try {
                // 将in进行序列化后，零拷贝到out
                byte[] data = serializer.serialize(in);
                out.writeInt(data.length);
                out.writeBytes(data);
            } catch (Exception e) {
                logger.error("Encode error:" + e.toString());
            }
        }
    }
}