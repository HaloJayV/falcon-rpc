package com.falcon.rpc.serializer.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.falcon.rpc.codec.RpcRequest;
import com.falcon.rpc.codec.RpcResponse;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.awt.event.KeyListener;

/**
 * @Auther: JayV
 * @Email: javajayv@gmail.com
 * @Date: 2021-8-19 17:38
 * @Description:
 */
public class KryoPoolFactory {

    private static volatile KryoPoolFactory poolFactory = null;

    private KryoFactory factory = new KryoFactory() {
        @Override
        public Kryo create() {
            Kryo kryo = new Kryo();
            kryo.setReferences(false);
            kryo.register(RpcRequest.class);
            kryo.register(RpcResponse.class);
            Kryo.DefaultInstantiatorStrategy strategy = (Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy();
            strategy.setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
            return kryo;
        }
    };

    private KryoPool pool = new KryoPool.Builder(factory).build();

    public KryoPoolFactory() {
    }

    public static KryoPool getKryoPoolInstance() {
        // 单例模式
        if(poolFactory == null) {
            synchronized (KryoPoolFactory.class) {
                if(poolFactory == null) {
                    poolFactory = new KryoPoolFactory();
                }
            }
        }
        return poolFactory.getPool();
    }

    public KryoPool getPool() {
        return pool;
    }
}