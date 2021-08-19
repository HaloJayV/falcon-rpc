package com.falcon.rpc.util;

import com.dyuproject.protostuff.ProtobufIOUtil;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-17 17:18
 * @Description: Protobuf 序列化器
 */
public class SerializationUtil {
    // key为被序列化的类对象，value为Schema对象
    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();
    // 实例化一个特定类的新对象。
    private static Objenesis objenesis = new ObjenesisStd(true);

    private SerializationUtil() {

    }

    // 类作为key去获取Schema对象
    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> cls) {
        return (Schema<T>) cachedSchema.computeIfAbsent(cls, RuntimeSchema::createFrom);
    }

    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            // 将obj对象进行序列化为字节流放到buffer，返回buffer的字节流数据
            return ProtobufIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    public static <T> T deserialize(byte[] data, Class<T> cls) {
        try {
            T message = (T) objenesis.newInstance(cls);
            Schema<T> schema = getSchema(cls);
            // data字节数组反序列化为message类对象
            ProtobufIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}