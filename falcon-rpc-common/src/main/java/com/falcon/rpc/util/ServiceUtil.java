package com.falcon.rpc.util;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-17 17:18
 * @Description:
 */
public class ServiceUtil {

    public static final String SERVICE_CONCAT_TOKEN = "#";

    // 生成在注册中心的服务id：{接口名}#{版本号}
    public static String makeServiceKey(String interfaceName, String version) {
        String serviceKey = interfaceName;
        if(version != null && version.trim().length() > 0) {
            serviceKey += SERVICE_CONCAT_TOKEN.concat(version);
        }
        return serviceKey;
    }

}