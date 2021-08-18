package com.falcon.rpc.codec;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-4 16:55
 * @Description: 心跳
 */
public final class Beat {
    // 心跳间隔时间
    public static final int BEAT_INTERVAL =30;
    // 心跳检测超时时间
    public static final int BEAT_TIMEOUT = 3 * BEAT_INTERVAL;
    // 请求的心跳ID
    public static final String BEAT_ID = "BEAT_PING_PONG";

    public static RpcRequest BEAT_PING;

    static {
        BEAT_PING = new RpcRequest() {};
        BEAT_PING.setRequestId(BEAT_ID);
    }


}