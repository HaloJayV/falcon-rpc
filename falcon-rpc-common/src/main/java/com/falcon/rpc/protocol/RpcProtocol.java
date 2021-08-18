package com.falcon.rpc.protocol;

import cn.hutool.socket.protocol.Protocol;
import com.falcon.rpc.util.JsonUtil;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-17 15:44
 * @Description: 自定义RPC协议
 */
public class RpcProtocol implements Serializable {

    private String host;
    private int port;
    private List<RpcServiceInfo> serviceInfoList;

    public String toJson() {
        return JsonUtil.objectToJson(this);
    }

    public static RpcProtocol fromJson(String json) {
        return JsonUtil.jsonToObject(json, RpcProtocol.class);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RpcProtocol that = (RpcProtocol) obj;
        return port == that.port &&
                Objects.equals(host, that.host) &&
                isListEqulas(serviceInfoList, that.getServiceInfoList());
    }

    public boolean isListEqulas(List<RpcServiceInfo> thisList, List<RpcServiceInfo> thatList) {
        if(thisList == null && thatList == null) {
            return true;
        }
        if((thisList == null && thatList != null)
        || (thisList != null && thatList == null)
        || (thisList.size() != thatList.size()) {
            return false;
        }
        return thisList.contains(thatList) && thatList.containsAll(thisList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, serviceInfoList.hashCode());
    }

    @Override
    public String toString() {
        return toJson();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<RpcServiceInfo> getServiceInfoList() {
        return serviceInfoList;
    }

    public void setServiceInfoList(List<RpcServiceInfo> serviceInfoList) {
        this.serviceInfoList = serviceInfoList;
    }
}