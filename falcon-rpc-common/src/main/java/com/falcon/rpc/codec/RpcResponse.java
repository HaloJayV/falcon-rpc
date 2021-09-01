package com.falcon.rpc.codec;

import java.io.Serializable;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-4 16:57
 * @Description: 响应对象
 */
public class RpcResponse implements Serializable {
    // 对应的请求id、错误信息、返回数据
    private String requestId;
    private String error;
    private Object result;

    public boolean isError() {
        return error != null;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}