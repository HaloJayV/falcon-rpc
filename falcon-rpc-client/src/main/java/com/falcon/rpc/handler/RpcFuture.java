package com.falcon.rpc.handler;

import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.falcon.rpc.RpcClient;
import com.falcon.rpc.codec.RpcRequest;
import com.falcon.rpc.codec.RpcResponse;
import com.sun.corba.se.impl.orbutil.concurrent.Sync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.attribute.standard.Finishings;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-20 15:59
 * @Description: 用于RPC异步调用对象
 */
public class RpcFuture implements Future<Object> {
    private static final Logger logger = LoggerFactory.getLogger(RpcFuture.class);
    // 自定义同步组件
    private Sync sync;
    private RpcRequest request;
    private RpcResponse response;
    private long startTime;
    private long responseTimeThreshold = 5000;
    // 等待异步回调的服务集合
    private List<AsyncRPCCallback> pendingCallbacks = new ArrayList<>();
    // 避免修改状态时候的更新冲突
    private ReentrantLock lock = new ReentrantLock();

    public RpcFuture(RpcRequest request) {
        this.sync = new Sync();
        this.request = request;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        // CAS
        sync.acquire(1);
        if(this.response != null) {
            return this.response.getResult();
        } else {
            return null;
        }
    }

    public void done(RpcResponse rpcResponse) {
        this.response = rpcResponse;
        // 释放锁
        sync.release(1);
        invokeCallbacks();
        long responseTime = System.currentTimeMillis() - startTime;
        // 超过回调过期时间
        if(responseTime > this.responseTimeThreshold) {
            logger.warn("Service response time is too slow. Request id = " + rpcResponse.getRequestId() + ". Response Time = " + responseTime + "ms");
        }
    }

    private void invokeCallbacks() {
        lock.lock();
        try {
            // 遍历还未回调的方法，尝试返回回调
            for (final AsyncRPCCallback callback : pendingCallbacks) {
                runCallback(callback);
            }
        } finally {
            lock.unlock();
        }
    }

    public RpcFuture addCallback(AsyncRPCCallback callback){
        lock.lock();
        try {
            if(isDone()) {
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    private void runCallback(final AsyncRPCCallback callback) {
        final RpcResponse res = this.response;
        RpcClient.submit(new Runnable() {
            @Override
            public void run() {
                if(!res.isError()) {
                    callback.success(res.getResult());
                } else {
                    callback.fail(new RuntimeException("Response error", new Throwable(res.getError())));
                }
            }
        });
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException {
        // 定时、尝试获取独占锁
        boolean success = sync.tryAcquireNanos(1, unit.toNanos(timeout));
        if(success) {
            // 获取函数回调内容回调
            if(this.response != null) {
                return this.response.getResult();
            } else {
                return null;
            }
        } else {
            // 线程获取失败，被占用
            throw new RuntimeException("Timeout exception, Request id: " + this.request.getRequestId()
            + ", Request class name:" + this.request.getClassName()
            + ", Request method: " + this.request.getMethodName());
        }

    }


    // 内部类：同步组件
    static class Sync extends AbstractQueuedSynchronizer {
        // 已经回调
        private final int done = 1;
        private int pending = 0;

        // 尝试获取参数arg的独占锁
        @Override
        protected boolean tryAcquire(int arg) {
            return super.tryAcquire(arg);
        }

        // 释放锁
        @Override
        protected boolean tryRelease(int arg) {
            // 还没有异步回调
            if(getState() == pending) {
                // CAS实现原子性
                if(compareAndSetState(pending, done)) {
                    // 修改状态为已已释放锁
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

        protected boolean isDone() {
            return getState() == done;
        }
    }
}

