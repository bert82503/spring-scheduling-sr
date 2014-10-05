
/**
 * 定义调度的通用支持类，
 * 并为Spring方法调用者(MethodInvoker)提供一个可运行任务的适配器。
 * 
 * <ul>
 * <li>CronSequenceGenerator：一个Crontab模式的时间序列生成器
 * <li>CronTrigger：cron表达式的触发器实现
 * <li>MethodInvokingRunnable：实现可运行接口作为一个基于Spring的方法调用者(MethodInvoker)的可配置方法调用的适配器
 * <li>PeriodicTrigger：一个周期性任务执行的触发器
 * <li>ScheduledMethodRunnable：MethodInvokingRunnable的变形
 * <li>SimpleTriggerContext：实现TriggerContext接口的简单的数据持有者
 * <li>TaskUtils：装饰任务错误处理的工具方法
 * </ul>
 *
 * <p>Generic support classes for scheduling.
 * Provides a Runnable adapter for Spring's MethodInvoker.
 *
 */
package org.springframework.scheduling.support;

