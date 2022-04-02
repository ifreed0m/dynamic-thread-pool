package com.dy.pool;

import com.dy.pool.queue.ChangeableBlockingQueue;

import java.util.StringJoiner;

/**
 * @author ifreed0m
 * @since 2021-08-30 6:35 下午
 */
public class ThreadPoolDataIndicators {

    /**
     * namespace-poolName拼接
     */
    private String poolName;
    /**
     * 线程池负载
     */
    private double threadPoolLoad;
    /**
     * 队列使用率
     */
    private double queueLoad;
    /**
     * 当前线程数
     */
    private int poolSize;
    /**
     * 正在积极执行任务的线程的大致数量
     */
    private int activeCount;
    /**
     * 已完成执行的大致任务总数
     */
    private long completedTaskCount;
    /**
     * 线程池曾经最大的线程数量
     */
    private int largestPoolSize;
    /**
     * 当前队列中任务的数量
     */
    private int queueSize;
    /**
     * 队列剩余的容量
     */
    private int remainingCapacity;
    /**
     * 核心线程数
     */
    private int corePoolSize;
    /**
     * 最大线程数
     */
    private int maximumPoolSize;
    /**
     * 队列容量
     */
    private int queueCapacity;
    /**
     * 1分钟内触发拒绝策略次数
     */
    private long rejectedExecutionCount;
    /**
     * 拒绝策略
     */
    private String rejectedExecution;

    public static ThreadPoolDataIndicators build(DynamicThreadPoolExecutor executor) {
        ThreadPoolDataIndicators dats = new ThreadPoolDataIndicators();
        ChangeableBlockingQueue<Runnable> queue = (ChangeableBlockingQueue) executor.getQueue();
        dats.setPoolName(executor.getPoolName());
        dats.setThreadPoolLoad(executor.threadPoolLoad());
        dats.setQueueLoad(executor.queueLoad());
        dats.setPoolSize(executor.getPoolSize());
        dats.setActiveCount(executor.getActiveCount());
        dats.setCompletedTaskCount(executor.getCompletedTaskCount());
        dats.setLargestPoolSize(executor.getLargestPoolSize());
        dats.setQueueSize(queue.size());
        dats.setRemainingCapacity(queue.remainingCapacity());
        dats.setCorePoolSize(executor.getCorePoolSize());
        dats.setMaximumPoolSize(executor.getMaximumPoolSize());
        dats.setQueueCapacity(queue.getCapacity());
        dats.setRejectedExecutionCount(Monitor.getRejectedExecutionCount(executor.getPoolName()));
        dats.setRejectedExecution(executor.getRejectedExecutionHandler().getClass().getName());
        return dats;
    }

    public String getRejectedExecution() {
        return rejectedExecution;
    }

    public void setRejectedExecution(String rejectedExecution) {
        this.rejectedExecution = rejectedExecution;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public long getRejectedExecutionCount() {
        return rejectedExecutionCount;
    }

    public void setRejectedExecutionCount(long rejectedExecutionCount) {
        this.rejectedExecutionCount = rejectedExecutionCount;
    }

    public double getThreadPoolLoad() {
        return threadPoolLoad;
    }

    public void setThreadPoolLoad(double threadPoolLoad) {
        this.threadPoolLoad = threadPoolLoad;
    }

    public double getQueueLoad() {
        return queueLoad;
    }

    public void setQueueLoad(double queueLoad) {
        this.queueLoad = queueLoad;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    public long getCompletedTaskCount() {
        return completedTaskCount;
    }

    public void setCompletedTaskCount(long completedTaskCount) {
        this.completedTaskCount = completedTaskCount;
    }

    public int getLargestPoolSize() {
        return largestPoolSize;
    }

    public void setLargestPoolSize(int largestPoolSize) {
        this.largestPoolSize = largestPoolSize;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getRemainingCapacity() {
        return remainingCapacity;
    }

    public void setRemainingCapacity(int remainingCapacity) {
        this.remainingCapacity = remainingCapacity;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public String alarmMsg() {
        return new StringJoiner(" \n\n", "", "")
                .add("\n\n##### 告警原因:%s")
                .add("线程池参数:")
                .add("线程池poolName(poolName)  : " + poolName)
                .add("线程池当前线程使用率(threadPoolLoad)  : " + threadPoolLoad)
                .add("队列使用率(queueLoad)  : " + queueLoad)
                .add("线程池当前线程数(poolSize)  : " + poolSize)
                .add("正在积极执行任务的线程的大致数量(activeCount)  : " + activeCount)
                .add("已完成执行的大致任务总数(completedTaskCount)  : " + completedTaskCount)
                .add("线程池曾经最大的线程数量(largestPoolSize)  : " + largestPoolSize)
                .add("当前队列中任务的数量(queueSize)  : " + queueSize)
                .add("队列剩余的容量(remainingCapacity)  : " + remainingCapacity)
                .add("核心线程数(corePoolSize)  : " + corePoolSize)
                .add("最大线程数(maximumPoolSize)  : " + maximumPoolSize)
                .add("队列容量(queueCapacity)  : " + queueCapacity)
                .add("1分钟内触发拒绝策略次数(rejectedExecutionCount)  : " + rejectedExecutionCount)
                .add("拒绝策略(rejectedExecution)  : " + rejectedExecution)
                .toString();
    }
}
