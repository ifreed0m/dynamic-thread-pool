package com.dy.pool;

import java.util.Set;

/**
 * @author ifreed0m
 * @since 2022-03-28 2:34 下午
 */
public interface RefreshAndMonitor {

    int ALARM_SOURCE_CREATE = 1;
    int ALARM_SOURCE_UPDATE = 2;
    int ALARM_SOURCE_REJ = 3;
    int ALARM_SOURCE_QUEUE_LOAD = 4;
    int ALARM_SOURCE_THREAD_POOL_LOAD = 5;

    /**
     * @param value com.dy.pool.spring.DynamicThreadPoolScan#value()
     * @return
     */
    Set<String> poolNames(String value);

    /**
     * @param poolNames
     * @return
     */
    ThreadPoolConf config(String poolNames);

    /**
     * 触发告警时会调用此方法，接入告警平台需要实现此方法
     *
     * @param source   ALARM_SOURCE_*
     * @param executor 触发告警的线程池对象
     * @param msg      默认的告警文案
     */
    void alarm(int source, DynamicThreadPoolExecutor executor, String msg);

    /**
     * 配置刷新时 调用 refresh(),若重写或实现此方法
     * 必须要调用 DynamicThreadPoolFactory.refresh(poolConfig) 实现对线程池配置的刷新
     *
     * @param poolConfig
     */
    default void refresh(ThreadPoolConf poolConfig) {
        DynamicThreadPoolFactory.refresh(poolConfig);
    }

    /**
     * 每个线程池创建时会调用唯一的一次
     * 自己实现线程池各项参数的监控
     *
     * 例子：
     * void metrics(DynamicThreadPoolExecutor executor){
     *     new ScheduledThreadPoolExecutor(1, new DynamicThreadPoolExecutor.DefaultThreadFactory("metrics-Timer"))
     *                 .scheduleAtFixedRate(
     *                         () -> {
     *                               线程池活跃度计算公式为：线程池活跃度 = activeCount/maximumPoolSize。这个公式代表当活跃线程数趋向于maximumPoolSize的时候，代表线程负载趋高。
     *                               也可以从两方面来看线程池的过载判定条件，一个是发生了Reject异常，一个是队列中有等待任务（支持定制阈值）。
     *                               executor::threadPoolLoad
     *                               <p>
     *                               队列使用率 executor::queueLoad
     *                               当前线程数 executor::getPoolSize
     *                               正在积极执行任务的线程的大致数量 executor::getActiveCount
     *                               返回已完成执行的大致任务总数 executor::getCompletedTaskCount
     *                               曾经同时进入池中的最大线程数 executor::getLargestPoolSize
     *                               当前队列中任务的数量(队列已经使用的容量) executor.getQueue().size()
     *                               队列剩余的容量 executor.getQueue().remainingCapacity()
     *                               核心线程数 executor::getCorePoolSize
     *                               最大线程数 executor::getMaximumPoolSize
     *                               队列总容量 ((ChangeableBlockingQueue) executor.getQueue()).getCapacity()
     *                         },
     *                         59, 59, TimeUnit.SECONDS
     *                 );
     * }
     *
     * @param executor
     */
    void metrics(DynamicThreadPoolExecutor executor);


}
