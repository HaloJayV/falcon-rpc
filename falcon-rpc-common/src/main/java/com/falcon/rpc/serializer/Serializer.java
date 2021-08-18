package com.falcon.rpc.serializer;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-4-25 20:17
 * @Description: 自定义序列化器
 */
public abstract class Serializer {
    public abstract <T> byte[] serialize(T object);

    public abstract <T> Object deserialize(byte[] bytes, Class<T> clazz);
}