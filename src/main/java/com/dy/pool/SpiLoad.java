package com.dy.pool;

import java.util.ServiceLoader;

/**
 * @author ifreed0m
 * @since 2022-03-28 3:15 下午
 */
public class SpiLoad {
    private static volatile RefreshAndMonitor LOAD_REFRESH_MONITOR;
    private static final Object LOCK = new Object();

    public static RefreshAndMonitor get() {
        if (LOAD_REFRESH_MONITOR == null) {
            synchronized (LOCK) {
                if (LOAD_REFRESH_MONITOR == null) {
                    ServiceLoader<RefreshAndMonitor> serviceLoader = ServiceLoader.load(RefreshAndMonitor.class);
                    for (RefreshAndMonitor refreshAndMonitor : serviceLoader) {
                        LOAD_REFRESH_MONITOR = refreshAndMonitor;
                        return refreshAndMonitor;
                    }
                }
            }
        }
        return LOAD_REFRESH_MONITOR;
    }
}
