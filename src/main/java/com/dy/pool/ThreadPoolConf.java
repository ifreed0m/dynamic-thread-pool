package com.dy.pool;


/**
 * @author ifreed0m
 * @since 2021-08-30 5:09 下午
 */
public class ThreadPoolConf {

    /**
     * threadPoolName
     */
    private String threadPoolName;
    /**
     * 核心线程数 （必填） 必须：corePoolSize <= maximumPoolSize
     */
    private int corePoolSize;
    /**
     * 最大线程数（必填）必须：corePoolSize <= maximumPoolSize
     */
    private int maximumPoolSize;
    /**
     * 队列容量（必填）
     */
    private int queueCapacity;
    /**
     * 超过最大核心线程数的线程，多少秒后没有新任务，回收（必填）
     */
    private int keepAliveSeconds;
    /**
     * 应用关闭时，如果线程池还有未执行完的任务，关闭线程池的等待时间（必填）
     */
    private int shutdownTimeoutMilliseconds = 100;
    /**
     * （必填）
     * 百分比，如 60%告警，就填60
     * 线程池活跃度 pool.activeCount/pool.maximumPoolSize
     * 线程池负载达到alarmThreadPoolLoadThreshold%告警
     */
    private double alarmThreadPoolLoadThreshold = 70;
    /**
     * （必填）
     * 百分比，如 60%告警，就填60
     * queue.size() / queue.getCapacity()
     * 队列使用率达到alarmQueueLoadThreshold%告警
     */
    private double alarmQueueLoadThreshold = 70;
    /**
     * 触发拒绝策略时忽略告警
     */
    private boolean ignoreRejectedExecution = false;
    /**
     * 0 预先不创建任何线程
     * 1 预先创建一个核心线程
     * 2 预先创建所有的核心线程
     */
    private int prestartCoreThreads;
    /**
     * 达到超时时间后没有任务是否允许核心线程也一起回收
     */
    private boolean allowCoreThreadTimeOut = false;
    /**
     * 不告警
     */
    private boolean notAlarm = false;
    /**
     * false: 当queue满的时候才开始创建非核心线程用于处理任务
     * true: 当queue的使用率 >= creatNotCoreThreadWhenQueueThreshold 时开始创建非核心线程用于处理任务
     */
    private boolean eager = false;
    /**
     * eager=true 且 当queue的使用率 >= creatNotCoreThreadWhenQueueThreshold 时开始再提交任务会创建非核心线程用于处理任务
     */
    private double creatNotCoreThreadQueueThreshold;
    /**
     * 拒绝策略。（必填）
     * <p>
     * 已有的的拒绝策略：
     * default-CallerRunsPolicy
     * default-AbortPolicy
     * default-DiscardPolicy
     * default-DiscardOldestPolicy
     * <p>
     * 自定义拒绝策略时，要继承抽象类 com.wb.async.pool.RejectedExecutionTallyAbstract。
     * 不建议自定义的拒绝策略是内部类。
     * 如果是内部类应该这样写，例：com.wb.A$B。用"$"分隔开外部类和内部类
     * rejectedExecutionClassName = 类的全限定名。如：com.wb.async.pool.A
     */
    private String rejectedExecutionClassName;
    /**
     * 用途描述
     */
    private String desc;

    public double getCreatNotCoreThreadQueueThreshold() {
        return creatNotCoreThreadQueueThreshold;
    }

    public void setCreatNotCoreThreadQueueThreshold(double creatNotCoreThreadQueueThreshold) {
        this.creatNotCoreThreadQueueThreshold = creatNotCoreThreadQueueThreshold;
    }

    public boolean isEager() {
        return eager;
    }

    public void setEager(boolean eager) {
        this.eager = eager;
    }

    public String getRejectedExecutionClassName() {
        return rejectedExecutionClassName;
    }

    public void setRejectedExecutionClassName(String rejectedExecutionClassName) {
        this.rejectedExecutionClassName = rejectedExecutionClassName;
    }

    public double getAlarmThreadPoolLoadThreshold() {
        return alarmThreadPoolLoadThreshold;
    }

    public void setAlarmThreadPoolLoadThreshold(double alarmThreadPoolLoadThreshold) {
        this.alarmThreadPoolLoadThreshold = alarmThreadPoolLoadThreshold;
    }

    public double getAlarmQueueLoadThreshold() {
        return alarmQueueLoadThreshold;
    }

    public void setAlarmQueueLoadThreshold(double alarmQueueLoadThreshold) {
        this.alarmQueueLoadThreshold = alarmQueueLoadThreshold;
    }

    public String getThreadPoolName() {
        return threadPoolName;
    }

    public void setThreadPoolName(String threadPoolName) {
        this.threadPoolName = threadPoolName;
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

    public int getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    public void setKeepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }

    public int getShutdownTimeoutMilliseconds() {
        return shutdownTimeoutMilliseconds;
    }

    public void setShutdownTimeoutMilliseconds(int shutdownTimeoutMilliseconds) {
        this.shutdownTimeoutMilliseconds = shutdownTimeoutMilliseconds;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isIgnoreRejectedExecution() {
        return ignoreRejectedExecution;
    }

    public void setIgnoreRejectedExecution(boolean ignoreRejectedExecution) {
        this.ignoreRejectedExecution = ignoreRejectedExecution;
    }

    public int getPrestartCoreThreads() {
        return prestartCoreThreads;
    }

    public void setPrestartCoreThreads(int prestartCoreThreads) {
        this.prestartCoreThreads = prestartCoreThreads;
    }

    public boolean isAllowCoreThreadTimeOut() {
        return allowCoreThreadTimeOut;
    }

    public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
    }

    public boolean isNotAlarm() {
        return notAlarm;
    }

    public void setNotAlarm(boolean notAlarm) {
        this.notAlarm = notAlarm;
    }

    public String check() {
        StringBuilder msg = new StringBuilder();
        if (this.corePoolSize <= 0) {
            msg.append(String.format("corePoolSize:%s 必须 > 0 \n\n", corePoolSize));
        }
        if (this.maximumPoolSize <= 0) {
            msg.append(String.format("maximumPoolSize:%s 必须 > 0 \n\n", maximumPoolSize));
        }
        if (this.queueCapacity <= 0) {
            msg.append(String.format("queueCapacity:%s 必须 > 0 \n\n", queueCapacity));
        }
        if (this.keepAliveSeconds <= 0) {
            msg.append(String.format("keepAliveSeconds:%s 必须 > 0 \n\n", keepAliveSeconds));
        }
        if (this.shutdownTimeoutMilliseconds <= 0) {
            msg.append(String.format("shutdownTimeoutMilliseconds:%s 必须 > 0 \n\n", shutdownTimeoutMilliseconds));
        }
        if (this.alarmThreadPoolLoadThreshold <= 0) {
            msg.append(String.format("alarmThreadPoolLoadThreshold:%s 必须 > 0 \n\n", alarmThreadPoolLoadThreshold));
        }
        if (this.alarmQueueLoadThreshold <= 0) {
            msg.append(String.format("alarmQueueLoadThreshold:%s 必须 > 0 \n\n", alarmQueueLoadThreshold));
        }
        if (this.rejectedExecutionClassName == null || "".equals(this.rejectedExecutionClassName)) {
            msg.append("rejectedExecutionClassName 必填 \n\n");
        }
        if (maximumPoolSize < corePoolSize) {
            msg.append(String.format("maximumPoolSize:%s 必须 >= corePoolSize:%s \n\n", maximumPoolSize, corePoolSize));
        }
        if (eager) {
            if (creatNotCoreThreadQueueThreshold <= 0) {
                msg.append(String.format("creatNotCoreThreadWhenQueueThreshold:%s 必须 > 0 \n\n", creatNotCoreThreadQueueThreshold));
            }
        }
        return msg.toString();
    }
}
