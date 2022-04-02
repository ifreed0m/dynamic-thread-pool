package com.dy.pool.queue;

import com.dy.pool.DynamicThreadPoolExecutor;
import com.dy.pool.ThreadPoolConf;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author ifreed0m
 * @since 2022-01-13 3:35 下午
 * https://github.com/apache/dubbo/blob/3.0/dubbo-common/src/main/java/org/apache/dubbo/common/threadpool/support/eager/EagerThreadPoolExecutor.java
 */
public class TaskQueue<R extends Runnable> extends VariableLinkedBlockingQueue<Runnable> {
    private static final long serialVersionUID = -2635853580887179627L;
    private DynamicThreadPoolExecutor executor;

    public void setExecutor(DynamicThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public TaskQueue(int capacity) {
        super(capacity);
    }


    @Override
    public boolean offer(Runnable runnable) {
        if (executor == null) {
            throw new RejectedExecutionException("The task queue does not have executor!");
        }
        ThreadPoolConf config = executor.getConfigData();
        if (!config.isEager()) {
            return super.offer(runnable);
        }
        int currentPoolThreadSize = executor.getPoolSize();
        // have free worker. put task into queue to let the worker deal with task.
        if (executor.getSubmittedTaskCount() < currentPoolThreadSize) {
            return super.offer(runnable);
        }
        // 队列使用率达到阈值
        if (executor.queueLoad01() >= config.getCreatNotCoreThreadQueueThreshold()) {
            // return false to let executor create new worker.
            if (currentPoolThreadSize < executor.getMaximumPoolSize()) {
                return false;
            }
        }
        // currentPoolThreadSize >= max
        return super.offer(runnable);
    }

    /**
     * retry offer task
     *
     * @param o task
     * @return offer success or not
     * @throws RejectedExecutionException if executor is terminated.
     */
    public boolean retryOffer(Runnable o, long timeout, TimeUnit unit) throws InterruptedException {
        if (executor.isShutdown()) {
            throw new RejectedExecutionException("Executor is shutdown!");
        }
        return super.offer(o, timeout, unit);
    }
}
