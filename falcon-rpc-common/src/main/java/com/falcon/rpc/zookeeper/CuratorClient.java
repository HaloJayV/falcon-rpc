package com.falcon.rpc.zookeeper;

import com.falcon.rpc.config.Constant;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-19 09:22
 * @Description: zookeeper客户端框架对象，相当于对zookeeper的监视器
 */
public class CuratorClient {
    private CuratorFramework client;
    // zookeeper节点必要属性：namespace、
    public CuratorClient(String connectString, String namespace,
                          int sessionTimeout, int connectionTimeout) {
        client = CuratorFrameworkFactory.builder()
                .namespace(namespace)
                // 服务器列表
                .connectString(connectString)
                // 会话超时时间
                .sessionTimeoutMs(sessionTimeout)
                // 连接创建超时时间
                .connectionTimeoutMs(connectionTimeout)
                // 连接重试机制
                .retryPolicy(new ExponentialBackoffRetry(1000, 10))
                .build();
    }

    public CuratorFramework getClient() {
        return client;
    }

    public CuratorClient(String connectString) {
        this(connectString, Constant.ZK_NAMESPACE, Constant.ZK_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
    }

    public CuratorClient(String connectString, int timeout) {
        this(connectString, Constant.ZK_NAMESPACE, timeout, timeout);
    }

    // 添加监听zookeeper连接状态的监听器
    public void addConnectionStateListener(ConnectionStateListener connectionStateListener) {
        client.getConnectionStateListenable().addListener(connectionStateListener);
    }

    // 创建节点：创建zookeeper节点的路径和元数据
    public String createPathData(String path, byte[] data) throws Exception {
        return client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path, data);
    }

    // 更新节点数据
    public void updatePathData(String path, byte[] data) throws Exception {
        client.setData().forPath(path, data);
    }

    // 删除path路径的节点
    public void deletePath(String path) throws Exception {
        client.delete().forPath(path);
    }

    // 使用查看器获取path的数据
    public void watchNode(String path, Watcher watcher) throws Exception {
        client.getData().usingWatcher(watcher).forPath(path);
    }

    /**
     * 获取path路径下的节点数据
     * @param path
     * @return
     * @throws Exception
     */
    public byte[] getData(String path) throws Exception {
        return client.getData().forPath(path);
    }

    // 获取当前节点的子节点
    public List<String> getChildren(String path) throws Exception {
        return client.getChildren().forPath(path);
    }

    // 查看、监听path路径下所有可监听的节点
    public void watchTreeNode(String path, TreeCacheListener listener) {
        TreeCache treeCache = new TreeCache(client, path);
        treeCache.getListenable().addListener(listener);
    }

    // 查看path路径下的子节点
    public void watchPathChildrenNode(String path, PathChildrenCacheListener listener) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true);
        // 初始化缓存构建模式
        pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        // 对可监听的节点进行监听
        pathChildrenCache.getListenable().addListener(listener);
    }

    public void close() {
        client.close();
    }
}