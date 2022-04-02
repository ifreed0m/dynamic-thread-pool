package com.dy.pool;

import com.dy.pool.queue.ChangeableBlockingQueue;
import com.google.common.collect.Maps;
import com.dy.pool.queue.TaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ifreed0m
 * @since 2021-07-28 下午3:24
 */
public class DynamicThreadPoolExecutor extends ThreadPoolExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicThreadPoolExecutor.class);
    private ThreadPoolConf configData;
    /**
     * task count
     */
    private final AtomicInteger submittedTaskCount = new AtomicInteger(0);
    /**
     * 唯一标识
     */
    private final String poolName;
    /**
     * 统计执行耗时
     */

    private static final Map<String, RejectedExecutionTallyAbstract> REJECTED_EXECUTION = Maps.newConcurrentMap();

    static {
        REJECTED_EXECUTION.put("default-CallerRunsPolicy", new CallerRunsPolicy());
        REJECTED_EXECUTION.put("default-AbortPolicy", new AbortPolicy());
        REJECTED_EXECUTION.put("default-DiscardPolicy", new DiscardPolicy());
        REJECTED_EXECUTION.put("default-DiscardOldestPolicy", new DiscardOldestPolicy());
    }

    DynamicThreadPoolExecutor(ThreadPoolConf configData) {
        super(configData.getCorePoolSize()
                , configData.getMaximumPoolSize()
                , configData.getKeepAliveSeconds()
                , TimeUnit.SECONDS
                , new TaskQueue<>(configData.getQueueCapacity())
                , new DefaultThreadFactory(configData.getThreadPoolName())
                , selectRejectedExecution(configData.getRejectedExecutionClassName()));
        ((TaskQueue) super.getQueue()).setExecutor(this);
        this.poolName = configData.getThreadPoolName();
        this.configData = configData;
    }

    /**
     * @return current tasks which are executed
     */
    public int getSubmittedTaskCount() {
        return submittedTaskCount.get();
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException();
        }
        // do not increment in method beforeExecute!
        submittedTaskCount.incrementAndGet();
        try {
            super.execute(command);
        } catch (RejectedExecutionException rx) {
            // retry to offer the task into queue.
            final TaskQueue queue = (TaskQueue) super.getQueue();
            try {
                if (!queue.retryOffer(command, 0, TimeUnit.MILLISECONDS)) {
                    submittedTaskCount.decrementAndGet();
                    throw new RejectedExecutionException("Queue capacity is full.", rx);
                }
            } catch (InterruptedException x) {
                submittedTaskCount.decrementAndGet();
                throw new RejectedExecutionException(x);
            }
        } catch (Throwable t) {
            // decrease any way
            submittedTaskCount.decrementAndGet();
            throw t;
        }
    }

    /**
     * 线程使用率
     *
     * @return
     */
    public double threadPoolLoad() {
        return Double.parseDouble(String.format("%.2f", ((double) getActiveCount() / (double) getMaximumPoolSize()) * 100));
    }

    /**
     * 队列使用率
     *
     * @return
     */
    public double queueLoad() {
        return Double.parseDouble(String.format("%.2f", queueLoad01()));
    }

    public double queueLoad01() {
        ChangeableBlockingQueue<Runnable> queue = (ChangeableBlockingQueue) getQueue();
        return ((double) queue.size() / (double) queue.getCapacity()) * 100;
    }

    private static RejectedExecutionTallyAbstract selectRejectedExecution(String className) {
        //  check
        RejectedExecutionTallyAbstract rt = REJECTED_EXECUTION.get(className);
        if (Objects.nonNull(rt)) {
            return rt;
        }
        try {
            Class<?> aClass = Class.forName(className);
            if (RejectedExecutionTallyAbstract.class.isAssignableFrom(aClass)) {
                //  check
                rt = REJECTED_EXECUTION.get(className);
                if (Objects.nonNull(rt)) {
                    return rt;
                }
                rt = (RejectedExecutionTallyAbstract) aClass.newInstance();
                //  check
                RejectedExecutionTallyAbstract ifAbsent = REJECTED_EXECUTION.putIfAbsent(className, rt);
                if (Objects.nonNull(ifAbsent)) {
                    return ifAbsent;
                }
                return rt;
            }
            throw new RuntimeException(String.format("className:[%s] 不是 com.wb.async.pool.RejectedExecutionTallyAbstract 的子类", className));
        } catch (ClassNotFoundException e) {
            LOGGER.error("DynamicThreadPoolExecutor ClassNotFoundException newRejectedExecution", e);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            LOGGER.error("DynamicThreadPoolExecutor IllegalAccessException newRejectedExecution", e);
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            LOGGER.error("DynamicThreadPoolExecutor InstantiationException newRejectedExecution", e);
            throw new RuntimeException(e);
        }
    }

    public ThreadPoolConf getConfigData() {
        return configData;
    }

    void setConfigData(ThreadPoolConf configData) {
        this.configData = configData;
    }

    public String getPoolName() {
        return poolName;
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
    }


    /**
     * 关闭线程池
     */
    public void gracefulShutdown() {
        ExecutorUtil.gracefulShutdown(this, configData.getShutdownTimeoutMilliseconds());
    }

    static class DefaultThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix + " -pool-%s";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    String.format(namePrefix, threadNumber.getAndIncrement()),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }


    /**
     * A handler for rejected tasks that runs the rejected task
     * directly in the calling thread of the {@code execute} method,
     * unless the executor has been shut down, in which case the task
     * is discarded.
     */
    static class CallerRunsPolicy extends RejectedExecutionTallyAbstract {
        /**
         * Creates a {@code CallerRunsPolicy}.
         */
        public CallerRunsPolicy() {
        }

        /**
         * Executes task r in the caller's thread, unless the executor
         * has been shut down, in which case the task is discarded.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        @Override
        public void rejectedExecutionOther(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();
            }
        }
    }

    /**
     * A handler for rejected tasks that throws a
     * {@code RejectedExecutionException}.
     */
    static class AbortPolicy extends RejectedExecutionTallyAbstract {
        /**
         * Creates an {@code AbortPolicy}.
         */
        public AbortPolicy() {
        }

        /**
         * Always throws RejectedExecutionException.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         * @throws RejectedExecutionException always
         */
        @Override
        public void rejectedExecutionOther(Runnable r, ThreadPoolExecutor e) {
            throw new RejectedExecutionException("Task " + r.toString() +
                    " rejected from " +
                    e.toString());
        }
    }

    /**
     * A handler for rejected tasks that silently discards the
     * rejected task.
     */
    static class DiscardPolicy extends RejectedExecutionTallyAbstract {

        public DiscardPolicy() {
        }

        @Override
        public void rejectedExecutionOther(Runnable r, ThreadPoolExecutor executor) {

        }
    }


    /**
     * A handler for rejected tasks that discards the oldest unhandled
     * request and then retries {@code execute}, unless the executor
     * is shut down, in which case the task is discarded.
     */
    static class DiscardOldestPolicy extends RejectedExecutionTallyAbstract {
        /**
         * Creates a {@code DiscardOldestPolicy} for the given executor.
         */
        public DiscardOldestPolicy() {
        }

        /**
         * Obtains and ignores the next task that the executor
         * would otherwise execute, if one is immediately available,
         * and then retries execution of task r, unless the executor
         * is shut down, in which case task r is instead discarded.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        @Override
        public void rejectedExecutionOther(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                e.getQueue().poll();
                e.execute(r);
            }
        }
    }
}
