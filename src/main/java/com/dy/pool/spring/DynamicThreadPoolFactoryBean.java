package com.dy.pool.spring;


import com.dy.pool.DynamicThreadPoolExecutor;
import com.dy.pool.DynamicThreadPoolFactory;

/**
 * @author ifreed0m
 * @since 2021-10-15 10:48 上午
 *
 *
 */
public class DynamicThreadPoolFactoryBean {
    public DynamicThreadPoolExecutor get(String poolName) {
        return DynamicThreadPoolFactory.get(poolName);
    }
}
