package com.dy.pool.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author ifreed0m
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({DynamicThreadPoolFactoryBean.class, DynamicThreadPoolRegister.class})
public @interface DynamicThreadPoolScan {
    /**
     * 自定义字符串，通过重写
     * com.dy.pool.LoadRefreshMonitor#poolNames(java.lang.String)
     * 实现对字符串的解析，解析出所有的要创建的线程池 poolName。
     */
    String value() default "";

}
