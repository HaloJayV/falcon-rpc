package com.falcon.rpc.proxy;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

/**
 * @Auther: JayV
 * @Date: 2020-8-18 15:28
 * @Description:
 */
public interface SerializableFunction<T> extends Serializable {
    default String getName() throws Exception {
        // 通过反射获取实现类的对应方法名，获取方法
        Method write = this.getClass().getDeclaredMethod("writeReplace");
        write.setAccessible(true);
        // 执行方法
        SerializedLambda serializedLambda = (SerializedLambda) write.invoke(this);
        // 返回当前接口的实现类的实现方法名
        return serializedLambda.getImplMethodName();
    }
}
