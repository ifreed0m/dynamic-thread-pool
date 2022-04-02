package com.dy.pool;

/**
 * @author ifreed0m
 * @since 2021-10-15 2:31 下午
 */
public class DynamicThreadPoolException extends RuntimeException{
    public DynamicThreadPoolException(String message) {
        super(message);
    }

    public DynamicThreadPoolException(String message, Throwable cause) {
        super(message, cause);
    }

    public DynamicThreadPoolException(Throwable cause) {
        super(cause);
    }

    protected DynamicThreadPoolException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
