/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dy.pool;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;


/**
 * @author dubbo
 * https://github.com/apache/dubbo/blob/3.0/dubbo-common/src/main/java/org/apache/dubbo/common/utils/ExecutorUtil.java
 */
public class ExecutorUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorUtil.class);
    private static final ThreadPoolExecutor SHUTDOWN_EXECUTOR = new ThreadPoolExecutor(0, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(100),
            new DynamicThreadPoolExecutor.DefaultThreadFactory("Close-ExecutorService-Timer"));

    public static boolean isTerminated(Executor executor) {
        if (executor instanceof ExecutorService) {
            return ((ExecutorService) executor).isTerminated();
        }
        return false;
    }

    /**
     * Use the shutdown pattern from:
     * https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html
     *
     * @param executor the Executor to shutdown
     * @param timeout  the timeout in milliseconds before termination
     */
    public static void gracefulShutdown(Executor executor, int timeout) {
        if (!(executor instanceof ExecutorService) || isTerminated(executor)) {
            return;
        }
        final ExecutorService es = (ExecutorService) executor;
        try {
            // Disable new tasks from being submitted
            es.shutdown();
        } catch (SecurityException | NullPointerException ex2) {
            return;
        }
        try {
            // Wait a while for existing tasks to terminate
            if (!es.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                es.shutdownNow();
            }
        } catch (InterruptedException ex) {
            es.shutdownNow();
            Thread.currentThread().interrupt();
        }
        if (!isTerminated(es)) {
            LOGGER.warn(" gracefulShutdown  !isTerminated  ");
            newThreadToCloseExecutor(es);
        }
    }


    private static void newThreadToCloseExecutor(final ExecutorService es) {
        if (!isTerminated(es)) {
            int i1 = 1000;
            SHUTDOWN_EXECUTOR.execute(() -> {
                try {
                    for (int i = 0; i < i1; i++) {
                        es.shutdownNow();
                        if (es.awaitTermination(10, TimeUnit.MILLISECONDS)) {
                            break;
                        }
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (Throwable e) {
                    LOGGER.warn(e.getMessage(), e);
                }
            });
        }
    }
}