package com.falcon.rpc.codec;

import com.falcon.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-4-25 20:06
 * @Description: RPC解码器
 */
public class RpcDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(RpcDecoder.class);
    private Class<?> genericClass;
    private Serializer serializer;

    /**
     * 将被编码对象in进行反序列化为对象集合
     * @param context
     * @param in
     * @param out
     * @throws Exception
     */
    @Override
    protected final void decode(ChannelHandlerContext context, ByteBuf in, List<Object> out) throws Exception {
        // ByteBuf读索引加4
        if(in.readableBytes() < 4) {
            return;
        }
        // 标记当前读的位置
        in.markReaderIndex();
        // 返回当前索引的(无符号) 整型，读索引加4
        int dataLength = in.readInt();
        // 当前读索引不能大于可以读的字节长度
        if(dataLength > in.readableBytes()) {
            // 回归之前mark的读索引
            in.resetReaderIndex();
            return;
        }
        // 开始解码, 也就是反序列化为List
        byte[] data = new byte[dataLength];
        // 将输入缓冲流in读到字节数组data里
        in.readBytes(data);
        Object obj = null;
        try {
            // 字节数组data反序列化为genericClass类型，然后添加到对象集合中
            obj = serializer.deserialize(data, genericClass);
            out.add(obj);
        } catch (Exception e) {
            logger.error("Decode error:".concat(e.toString()));
        }
    }
}
