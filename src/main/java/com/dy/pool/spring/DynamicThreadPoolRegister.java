package com.dy.pool.spring;


import com.dy.pool.RefreshAndMonitor;
import com.dy.pool.SpiLoad;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Objects;
import java.util.Set;

/**
 * @author ifreed0m
 * @since 2021-10-14 2:59 下午
 */
public class DynamicThreadPoolRegister implements ImportBeanDefinitionRegistrar {

    private static final RefreshAndMonitor LOAD_REFRESH_MONITOR = SpiLoad.get();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes mapperScanAttrs = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(DynamicThreadPoolScan.class.getName()));
        if (Objects.isNull(mapperScanAttrs)) {
            return;
        }
        registerBeanDefinition(registry, mapperScanAttrs);
    }

    protected void registerBeanDefinition(BeanDefinitionRegistry registry, AnnotationAttributes mapperScanAttrs) {
        String value = mapperScanAttrs.getString("value");
        Set<String> poolNamesSet = LOAD_REFRESH_MONITOR.poolNames(value);
        poolNamesSet.forEach(poolName -> doRegisterBeanDefinition(registry, poolName));
    }

    private void doRegisterBeanDefinition(BeanDefinitionRegistry registry, String poolName) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition();
        beanDefinitionBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_NAME);
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setFactoryBeanName(DynamicThreadPoolFactoryBean.class.getName());
        beanDefinition.setFactoryMethodName("get");
        ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
        constructorArgumentValues.addIndexedArgumentValue(0, poolName);
        beanDefinition.setConstructorArgumentValues(constructorArgumentValues);
        registry.registerBeanDefinition(poolName, beanDefinition);
    }

}
