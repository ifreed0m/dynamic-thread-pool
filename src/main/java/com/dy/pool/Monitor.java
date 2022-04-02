package com.dy.pool;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author ifreed0m
 * @since 2021-08-30 5:37 下午
 */
public class Monitor {
    private static final RefreshAndMonitor LOAD_REFRESH_MONITOR = SpiLoad.get();
    private static final Logger LOGGER = LoggerFactory.getLogger(Monitor.class);
    private static final String REJECTED_EXECUTION_TITLE = "当前拒绝策略触发(%s)次";
    private static final String QUEUE_LOAD_TITLE = "当前队列使用率为(%s%%),达到设置的阈值(%s%%)";
    private static final String THREAD_POOL_LOAD_TITLE = "当前线程池线程使用率为(%s%%),达到设置的阈值(%s%%)";
    private static final String THREAD_POOL_PARAM_ERROR = "线程池(%s)失败，maximumPoolSize >= corePoolSize。maximumPoolSize:%s,corePoolSize:%s";
    private static final String THREAD_POOL_MONITOR_QUOTA = "threadPoolMonitorQuota";
    private static final String THREAD_POOL_MONITOR_PARAM = "threadPoolMonitorParam";
    private static final ThreadPoolExecutor ALARM_THREAD_POOL = new ThreadPoolExecutor(
            1, 5, 1, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10),
            new DynamicThreadPoolExecutor.DefaultThreadFactory("Alarm-ExecutorService-Timer"),
            new ThreadPoolExecutor.DiscardOldestPolicy());
    /**
     * 1分钟内拒绝策略触发次数
     */
    static final LoadingCache<String, LongAdder> REJECTED_EXECUTION_TALLY = CacheBuilder.newBuilder()
            // 缓存有效期
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(Integer.MAX_VALUE)
            .build(new CacheLoader<String, LongAdder>() {
                @Override
                public LongAdder load(String key) {
                    return new LongAdder();
                }
            });

    private Monitor() {
    }


    /**
     * 拒绝策略触发次数
     *
     * @param poolId
     * @return
     */
    static long getRejectedExecutionCount(String poolId) {
        LongAdder ifPresent = REJECTED_EXECUTION_TALLY.getIfPresent(poolId);
        if (Objects.isNull(ifPresent)) {
            return 0L;
        }
        return ifPresent.longValue();
    }

    /**
     * 增加触发拒绝策略次数
     *
     * @param poolId
     * @return
     */
    static void incrementRejectedExecutionTally(String poolId) {
        try {
            REJECTED_EXECUTION_TALLY.get(poolId).increment();
        } catch (ExecutionException e) {
            LOGGER.warn(" rejectedExecutionTally error", e);
        }
    }

    /**
     * 线程池做了更改，初始化拒绝策略执行过的次数
     *
     * @param poolId
     */
    static void refreshRejectedExecutionTally(String poolId) {
        REJECTED_EXECUTION_TALLY.invalidate(poolId);
    }


    static void checkAlarm(DynamicThreadPoolExecutor executor) {
        ThreadPoolConf configData = executor.getConfigData();
        if (configData.isNotAlarm()) {
            return;
        }
        try {
            // 触发拒绝策略
            final long rejectedExecutionCount = getRejectedExecutionCount(executor.getPoolName());
            if (rejectedExecutionCount > 0) {
                doAlarm(RefreshAndMonitor.ALARM_SOURCE_REJ, String.format(REJECTED_EXECUTION_TITLE, rejectedExecutionCount), executor);
                return;
            }
            // 队列使用率达到阈值
            final double queueLoad = executor.queueLoad();
            final double alarmQueueLoadThreshold = configData.getAlarmQueueLoadThreshold();
            if (queueLoad >= alarmQueueLoadThreshold) {
                doAlarm(RefreshAndMonitor.ALARM_SOURCE_QUEUE_LOAD, String.format(QUEUE_LOAD_TITLE, queueLoad, alarmQueueLoadThreshold), executor);
                return;
            }
            // ActiveCount/MaximumPoolSize达到阈值
            final double threadPoolLoad = executor.threadPoolLoad();
            final double alarmThreadPoolLoadThreshold = configData.getAlarmThreadPoolLoadThreshold();
            if (threadPoolLoad >= alarmThreadPoolLoadThreshold) {
                doAlarm(RefreshAndMonitor.ALARM_SOURCE_THREAD_POOL_LOAD, String.format(THREAD_POOL_LOAD_TITLE, threadPoolLoad, alarmThreadPoolLoadThreshold), executor);
            }
        } catch (Exception e) {
            LOGGER.error("Monitor alarm ", e);
        }
    }

    private static void doAlarm(int source, String title, DynamicThreadPoolExecutor executor) {
        ALARM_THREAD_POOL.execute(() -> {
            try {
                String message = ThreadPoolDataIndicators.build(executor).alarmMsg();
                LOAD_REFRESH_MONITOR.alarm(source, executor, String.format(message, title));
            } catch (Exception e) {
                LOGGER.error("doAlarm error ", e);
            }
        });
    }


    static void doMonitor(DynamicThreadPoolExecutor executor) {
       /* String poolId = executor.getPoolName();
        // 线程池活跃度计算公式为：线程池活跃度 = activeCount/maximumPoolSize。这个公式代表当活跃线程数趋向于maximumPoolSize的时候，代表线程负载趋高。
        // 也可以从两方面来看线程池的过载判定条件，一个是发生了Reject异常，一个是队列中有等待任务（支持定制阈值）。
        Metrics.gauge(THREAD_POOL_MONITOR_QUOTA, executor::threadPoolLoad).tag("name", poolId).tag("type", "threadPoolLoad").get();
        // 队列使用率
        Metrics.gauge(THREAD_POOL_MONITOR_QUOTA, executor::queueLoad).tag("name", poolId).tag("type", "queueLoad").get();
        // 当前线程数
        Metrics.gauge(THREAD_POOL_MONITOR_QUOTA, executor::getPoolSize).tag("name", poolId).tag("type", "poolSize").get();
        // 正在积极执行任务的线程的大致数量
        Metrics.gauge(THREAD_POOL_MONITOR_QUOTA, executor::getActiveCount).tag("name", poolId).tag("type", "activeCount").get();
        // 返回已完成执行的大致任务总数
        Metrics.gauge(THREAD_POOL_MONITOR_QUOTA, executor::getCompletedTaskCount).tag("name", poolId).tag("type", "completedTaskCount").get();
        // 曾经同时进入池中的最大线程数
        Metrics.gauge(THREAD_POOL_MONITOR_QUOTA, executor::getLargestPoolSize).tag("name", poolId).tag("type", "largestPoolSize").get();
        // 当前队列中任务的数量(队列已经使用的容量)
        Metrics.gauge(THREAD_POOL_MONITOR_QUOTA, () -> executor.getQueue().size()).tag("name", poolId).tag("type", "queueTaskSize").get();
        // 队列剩余的容量
        Metrics.gauge(THREAD_POOL_MONITOR_QUOTA, () -> executor.getQueue().remainingCapacity()).tag("name", poolId).tag("type", "queueRemainingCapacity").get();


        // 核心线程数
        Metrics.gauge(THREAD_POOL_MONITOR_PARAM, executor::getCorePoolSize).tag("name", poolId).tag("type", "corePoolSize").get();
        // 最大线程数
        Metrics.gauge(THREAD_POOL_MONITOR_PARAM, executor::getMaximumPoolSize).tag("name", poolId).tag("type", "maximumPoolSize").get();
        // 队列总容量
        Metrics.gauge(THREAD_POOL_MONITOR_PARAM, () -> ((ChangeableBlockingQueue) executor.getQueue()).getCapacity()).tag("name", poolId).tag("type", "queueCapacity").get();
    */
    }


}
