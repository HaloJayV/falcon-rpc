package com.falcon.rpc.core;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-19 15:49
 * @Description:
 */
public abstract class Server {

    public abstract void start() throws Exception;

    public abstract void stop() throws Exception;

}