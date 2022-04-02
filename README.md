# simple-dynamic-thread-pool。简洁的动态线程池实现

**只提供最基础的动态线程池的拓展接口的暴露。配置的读取、刷新，告警方式，监控方式等功能，使用者通过暴露的接口`com.dy.pool.RefreshAndMonitor`自己实现，。**

###### 特点：

1.线程池的创建完全依赖配置中心的配置

2.使用时按照spring bean的方式注入

3.只抽离出了必要的各方需要定制化的接口。实现`com.dy.pool.RefreshAndMonitor`接口，理论是支持任何开源或自研的配置中心





###### 使用方式：

1.基于**Java Spi**实现的拓展，使用时实现 `com.dy.pool.RefreshAndMonitor`接口。实现接口的方法。

2.实现`com.dy.pool.RefreshAndMonitor#poolNames`方法自己解析`com.dy.pool.spring.DynamicThreadPoolScan#value`的value解析所有要读取的线程池参数配置。

3.使用方式：声明注解`@DynamicThreadPoolScan("value")`，声明在`@Configuration`标注的类或springboot的启动类上，（**value：对字符串格式没有任何要求，自己自定义字符串，在`com.dy.pool.RefreshAndMonitor#poolNames`中自己解析**）。使用bean注入的方式注入线程池对象。bean的名字就是配置中的poolName

4.支持的各项参数`com.dy.pool.ThreadPoolConf`

5.

5.1.**必须：**`maximumPoolSize >= corePoolSize`

Bug Details: [ThreadPoolExecutor’s setCorePoolSize method allows corePoolSize > maxPoolSize](https://bugs.openjdk.java.net/browse/JDK-7153400)

Change Log: http://hg.openjdk.java.net/jdk9/jdk9/jdk/rev/eb6f07ec4509

5.2.**线程池创建后只支持动态修改以下参数**：

5.2.1 线程池配置参数：

`corePoolSize`（核心线程数）

`maximumPoolSize`（最大线程数）

`queueCapacity`（队列容量）

`keepAliveSeconds`（超过最大核心线程数的线程，多少秒后没有新任务，回收）

`shutdownTimeoutMilliseconds`（应用关闭时，如果线程池还有未执行完的任务，关闭线程池的等待时间）

`prestartCoreThreads` (立即创建部分线程)

`allowCoreThreadTimeOut`（允许回收核心线程，回收等待时间是 keepAliveSeconds，默认：false）

2.2 告警配置参数:

`alarmThreadPoolLoadThreshold`（线程池线程负载告警阈值，计算方式:activeCount/maximumPoolSize

`alarmQueueLoadThreshold`（队列使用率告警阈值）

`ignoreRejectedExecution`（触发拒绝策略时不告警，默认:false）

`notAlarm`（不告警，默认：false）

5.2.3 **线程池模式配置，线程池支持两种模式。lazy(默认)和eager。**

5.2.3.1 lazy模式，阻塞队列满了再提交任务才会创建非核心线程；

5.2.3.1 eager模式，当阻塞队列使用率达到`creatNotCoreThreadQueueThreshold`时，再提交任务会开始创建非核心线程用于处理任务；

模式配置参数：

`eager`（false：lazy模式；true：eager模式）

`creatNotCoreThreadQueueThreshold` （eager=true时生效且必填，当阻塞队列使用率达到`creatNotCoreThreadQueueThreshold`时，再提交任务会开始创建非核心线程用于处理任务）

5.3.**注意**

线程池的阻塞队列使用的是 `VariableLinkedBlockingQueue`，队列缩容的时候会允许出现队列长度大于队列容量的情况，多余出的数据并不会做清除处理。

