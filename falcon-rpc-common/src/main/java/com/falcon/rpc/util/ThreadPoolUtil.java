package com.falcon.rpc.util;

import io.netty.util.internal.ThreadExecutorMap;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-17 17:18
 * @Description:
 */
public class ThreadPoolUtil {

    public static ThreadPoolExecutor makeServerThreadPool(final String serviceName,
                                                           int corePoolSize,
                                                           int maxPoolSize) {
       ThreadPoolExecutor serverHandlerPool = new ThreadPoolExecutor(
               corePoolSize,
               maxPoolSize,
               60L,
               TimeUnit.SECONDS,
               new LinkedBlockingDeque<Runnable>(1000),
               new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "falcon-rpc-" + serviceName + "-" + r.hashCode());
                    }
                },
                new ThreadPoolExecutor.AbortPolicy()
        );
        return serverHandlerPool;
    }

}