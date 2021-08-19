package com.falcon.rpc.config;

/**
 * @Auther: JayV
 * @Date: 2020-8-18 15:28
 * @Description:
 */
public interface Constant {

    // zookeeper的session存活时长
    int ZK_SESSION_TIMEOUT = 5000;
    // zookeeper连接的生命周期时长
    int ZK_CONNECTION_TIMEOUT = 5000;

    // 注册中心地址
    String ZK_REGISTRY_PATH = "/registry";
    // 元数据路径
    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";
    // zookeeper节点名称
    String ZK_NAMESPACE = "falcon-rpc";

}
