package com.dy.pool;

import com.dy.pool.queue.ChangeableBlockingQueue;
import com.google.common.collect.Maps;
import org.springframework.util.ObjectUtils;


import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 实现原理：
 * https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html
 * https://mp.weixin.qq.com/s?__biz=Mzg3NjU3NTkwMQ==&mid=2247505103&idx=1&sn=a041dbec689cec4f1bbc99220baa7219&source=41#wechat_redirect
 *
 * @author ifreed0m
 * @since 2021-07-24 下午2:29
 */
public class DynamicThreadPoolFactory {
    private static final RefreshAndMonitor LOAD_REFRESH_MONITOR = SpiLoad.get();
    private static final Map<String, DynamicThreadPoolExecutor> CREATED_POOL = Maps.newConcurrentMap();
    /**
     * 0 预先不创建任何线程
     * 1 预先创建一个核心线程
     * 2 预先创建所有的核心线程
     */
    private static final int NOT_PRESTART_CORE_THREADS = 0;
    private static final int PRESTART_ONE_CORE_THREADS = 1;
    private static final int PRESTART_ALL_CORE_THREADS = 2;
    /**
     * 占位而已
     */
    private static final Integer PLACEHOLDER_VALUE = 1;
    /**
     * 初始化过的线程池和注册过监听的namespace
     */
    private static final Map<String, Map<String, Integer>> REGISTERED_NAMESPACE = Maps.newConcurrentMap();
    private static final ThreadPoolExecutor SHUTDOWN_THREAD_POOL = new ThreadPoolExecutor(
            0, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(100),
            new DynamicThreadPoolExecutor.DefaultThreadFactory("Close-ExecutorService-Timer"));

    private static final AtomicBoolean SHUTDOWN_HOOK_AND_MONITOR_THREAD_HAS_REGISTER = new AtomicBoolean(false);

    /**
     * 注册ShutdownHook和MonitorThread，只能执行一次
     */
    private static void registerShutdownHookAndMonitorThread() {
        if (SHUTDOWN_HOOK_AND_MONITOR_THREAD_HAS_REGISTER.get()) {
            return;
        }
        if (SHUTDOWN_HOOK_AND_MONITOR_THREAD_HAS_REGISTER.getAndSet(true)) {
            return;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(DynamicThreadPoolFactory::shutdownThreadPool, "Thread-shutdownAllThreadPool"));
        // 隔59秒检测一次线程池状态
        new ScheduledThreadPoolExecutor(1, new DynamicThreadPoolExecutor.DefaultThreadFactory("Monitor-ExecutorService-Timer"))
                .scheduleAtFixedRate(
                        () -> CREATED_POOL.forEach((s, executor) -> Monitor.checkAlarm(executor)),
                        59, 59, TimeUnit.SECONDS
                );
    }

    private DynamicThreadPoolFactory() {
    }

    private static void registerShutDownHook() {
        registerShutdownHookAndMonitorThread();
    }

    /**
     * @param threadPoolName
     * @return
     */
    public static DynamicThreadPoolExecutor get(String threadPoolName) {
        // check
        DynamicThreadPoolExecutor executor = CREATED_POOL.get(threadPoolName);
        if (Objects.nonNull(executor)) {
            return executor;
        }
        return createThreadPool(threadPoolName);
    }

    /**
     * 变更线程池配置
     */
    public static void refresh(ThreadPoolConf poolConfig) {
        String poolName = poolConfig.getThreadPoolName();
        DynamicThreadPoolExecutor executor = CREATED_POOL.get(poolName);
        if (Objects.isNull(executor)) {
            return;
        }
        poolConfig.setThreadPoolName(poolConfig.getThreadPoolName());
        String check = poolConfig.check();
        if (!ObjectUtils.isEmpty(check)) {
            LOAD_REFRESH_MONITOR.alarm(RefreshAndMonitor.ALARM_SOURCE_UPDATE, executor, String.format("线程池[%s]修改失败 \n\n%s", poolName, check));
            return;
        }
        changePool(poolConfig, executor);
    }

    private static void changePool(ThreadPoolConf poolConfig, DynamicThreadPoolExecutor executor) {
        // todo 支持修改的参数
        // 核心线程数
        executor.setCorePoolSize(poolConfig.getCorePoolSize());
        // 最大线程数
        executor.setMaximumPoolSize(poolConfig.getMaximumPoolSize());
        // 队列容量
        ((ChangeableBlockingQueue) executor.getQueue()).setCapacity(poolConfig.getQueueCapacity());
        executor.setConfigData(poolConfig);
        settingThreadPool(executor, true);
        Monitor.refreshRejectedExecutionTally(executor.getPoolName());
    }

    private static DynamicThreadPoolExecutor createThreadPool(String threadPoolName) {
        ThreadPoolConf configData = LOAD_REFRESH_MONITOR.config(threadPoolName);
        String check = configData.check();
        if (!ObjectUtils.isEmpty(check)) {
            String format = String.format("线程池[%s]创建失败 \n\n%s", threadPoolName, check);
            LOAD_REFRESH_MONITOR.alarm(RefreshAndMonitor.ALARM_SOURCE_CREATE, null, format);
            throw new DynamicThreadPoolException(format);
        }
        return doCreateThreadPool(configData);
    }

    private static DynamicThreadPoolExecutor doCreateThreadPool(ThreadPoolConf config) {
        // check
        DynamicThreadPoolExecutor executor = CREATED_POOL.get(config.getThreadPoolName());
        if (Objects.nonNull(executor)) {
            return executor;
        }
        executor = new DynamicThreadPoolExecutor(config);
        DynamicThreadPoolExecutor putIfAbsent = CREATED_POOL.putIfAbsent(config.getThreadPoolName(), executor);
        // check
        if (Objects.nonNull(putIfAbsent)) {
            return putIfAbsent;
        }
        // 初始化线程池
        settingThreadPool(executor, false);
        LOAD_REFRESH_MONITOR.metrics(executor);
        registerShutDownHook();
        return executor;
    }

    private static void settingThreadPool(DynamicThreadPoolExecutor executor, boolean isChange) {
        ThreadPoolConf config = executor.getConfigData();
        int prestartCoreThreads = config.getPrestartCoreThreads();
        executor.allowCoreThreadTimeOut(config.isAllowCoreThreadTimeOut());
        if (isChange) {
            executor.setKeepAliveTime(config.getKeepAliveSeconds(), TimeUnit.SECONDS);
        }
        if (prestartCoreThreads == NOT_PRESTART_CORE_THREADS) {
            return;
        }
        if (prestartCoreThreads == PRESTART_ONE_CORE_THREADS) {
            executor.prestartCoreThread();
            return;
        }
        if (prestartCoreThreads == PRESTART_ALL_CORE_THREADS) {
            executor.prestartAllCoreThreads();
        }
    }

    /**
     * 关闭线程池
     */
    private static void shutdownThreadPool() {
        int size = CREATED_POOL.size();
        if (size <= 0) {
            return;
        }
        SHUTDOWN_THREAD_POOL.setCorePoolSize(size);
        SHUTDOWN_THREAD_POOL.setMaximumPoolSize(size);
        CREATED_POOL.forEach((s, threadPool) -> SHUTDOWN_THREAD_POOL.execute(threadPool::gracefulShutdown));
        ExecutorUtil.gracefulShutdown(SHUTDOWN_THREAD_POOL, (int) TimeUnit.SECONDS.toMillis(15));
    }

}
