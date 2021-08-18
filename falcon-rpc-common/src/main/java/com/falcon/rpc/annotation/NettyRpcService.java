package com.falcon.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-4-24 21:55
 * @Description: rpc服务注解
 *
 *
 *
 */
// 规定生命周期的元注解，保留范围（注解不仅被保存到class文件中，jvm加载class文件为字节码后，仍然存在）
@Retention(RetentionPolicy.RUNTIME)
// 注解的作用对象，类型或者接口或者枚举，表示这个注解作用在类、接口、枚举上
@Target({ElementType.TYPE})
@Component
public @interface NettyRpcService {
    // 类元素
    Class<?> value();
    // 版本号
    String version() default "1.0";
}
