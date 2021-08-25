package com.falcon.rpc.protocol;

import cn.hutool.json.JSONUtil;
import com.falcon.rpc.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.io.Serializable;
import java.util.Objects;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-17 15:44
 * @Description: 服务提供方的接口信息
 */
public class RpcServiceInfo implements Serializable {

    // 服务提供方的接口名称
    private String serviceName;
    private String version;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object obj) {
        // 比较hash值
        if (this == obj) {
            return true;
        }
        // 判断对象类和当前类是否相同
        if(obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RpcServiceInfo that = (RpcServiceInfo) obj;
        return Objects.equals(serviceName, that.serviceName)
                && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, version);
    }

    public String toJson() {
        String json = JsonUtil.objectToJson(this);
        return json;
    }

    @Override
    public String toString() {
        return toJson();
    }
}