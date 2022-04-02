package com.dy.pool.queue;

import java.util.concurrent.BlockingQueue;

/**
 * @author ifreed0m
 * @since 2021-09-01 2:21 下午
 */
public interface ChangeableBlockingQueue<E> extends BlockingQueue<E> {
    /**
     * 修改 capacity
     *
     * @param capacity
     * @return
     */
    boolean setCapacity(int capacity);

    /**
     * get capacity
     *
     * @return
     */
    int getCapacity();
}
