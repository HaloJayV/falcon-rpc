package com.falcon.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-4-24 21:55
 * @Description: rpc服务注解
 */
// 保留范围（在源码中存在，还是在字节码里面，还是一直留到运行环境），一般是Runtime，
@Retention(RetentionPolicy.RUNTIME)
// 注解的作用对象，作用在属性字段上
@Target({ElementType.FIELD})
@Component
public @interface RpcAutowired {
    // 版本号
    String version() default "";
}