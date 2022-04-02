package com.dy.pool;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 使用 DynamicThreadPoolExecutor 使用自定义拒绝策略时必须继承该抽象类
 *
 * @author ifreed0m
 */
public abstract class RejectedExecutionTallyAbstract implements RejectedExecutionHandler {

    @Override
    public final void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        if (e instanceof DynamicThreadPoolExecutor) {
            DynamicThreadPoolExecutor dtp = (DynamicThreadPoolExecutor) e;
            //线程池没有Shutdown，并且不忽略触发拒绝策略。
            // 发送告警信息
            if (!e.isShutdown() && !dtp.getConfigData().isIgnoreRejectedExecution()) {
                // 告警
                Monitor.incrementRejectedExecutionTally(dtp.getPoolName());
            }
        }
        if (e.isShutdown()) {
            rejectedExecutionWhenIsShutdown(r, e);
        } else {
            rejectedExecutionOther(r, e);
        }
    }

    /**
     * 拒绝策略
     *
     * @param r
     * @param executor
     */
    public abstract void rejectedExecutionOther(Runnable r, ThreadPoolExecutor executor);

    /**
     * 服务重启线程池执行过 java.util.concurrent.ExecutorService#shutdown() 后，又有任务提交此时触发拒绝策略
     *
     * @param r
     * @param executor
     */
    public void rejectedExecutionWhenIsShutdown(Runnable r, ThreadPoolExecutor executor) {
        rejectedExecutionOther(r, executor);
    }
}