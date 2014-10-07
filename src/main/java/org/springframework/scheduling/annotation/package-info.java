
/**
 * 调度与异步方法执行的JDK 1.5+注解支持。
 * 
 * <ul>
 * <li>AsyncConfigurer：异步执行配置者，由带有@EnableAsync注解的配置类实现的接口，想在处理异步方法调用时使用自定义的执行器(Executor)实例
 * <li>SchedulingConfigurer：任务调度配置者，由带有@EnableScheduling注解的配置类实现的可选接口
 * 
 * <li>AbstractAsyncConfiguration：抽象异步执行配置类，为使用Spring的异步方法执行能力提供了公共的结构
 * <li>AnnotationAsyncExecutionInterceptor：特殊的异步执行拦截器(AsyncExecutionInterceptor)实现，委托方法调用到一个基于Async注解的执行器(Executor)
 * <li>AsyncAnnotationAdvisor：异步执行注解通知器，通过Async注解激活异步方法执行的通知器
 * <li>AsyncAnnotationBeanPostProcessor：异步执行注解Bean后置处理器，自动应用异步调用行为到任何在类或方法级别携带Async注解的bean的Bean后置处理器
 * <li>AsyncConfigurationSelector：异步执行配置选择器，基于异步模式值(mode)来选择哪个AbstractAsyncConfiguration实现类应该被使用
 * <li>AsyncResult<V>：异步执行结果，一个传递的Future处理
 * <li>ProxyAsyncConfiguration：代理异步执行配置，注册需要启用基于代理的异步方法执行的Spring基础beans的配置类
 * <li>ScheduledAnnotationBeanPostProcessor：调度注解Bean后置处理器，注册使用@Scheduled注解的方法(被TaskScheduler调用)的Bean后置处理器
 * <li>SchedulingConfiguration：调度配置类，注册一个能处理@Scheduled注解的ScheduledAnnotationBeanPostProcessor bean的配置类
 * 
 * <li>Async：标记一个方法作为异步执行候选者的注解
 * <li>EnableAsync：启用Spring的异步方法执行能力，类似<task:*> XML命名空间的功能
 * <li>EnableScheduling：启用Spring的调度任务执行能力，类似<task:*> XML命名空间的功能
 * <li>Scheduled：标记一个为可调度的注解
 * </ul>
 *
 * <p>JDK 1.5+ annotation for asynchronous method execution.
 *
 */
package org.springframework.scheduling.annotation;

