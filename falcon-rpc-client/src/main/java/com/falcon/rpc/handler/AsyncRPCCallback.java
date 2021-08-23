package com.falcon.rpc.handler;

/**
 * @Auther: JayV
 * @Date: 2020-8-18 15:28
 * @Description: 异步RPC回调
 */
public interface AsyncRPCCallback {

    void success(Object result);

    void fail(Exception e);

}
