
/**
 * Spring调度支持的通用条款，独立于任何特定的调度系统。
 * 
 * <ul>
 * <li>SchedulingAwareRunnable：Runnable接口的扩展，增加长时间运行操作的特殊回调。
 * <li>SchedulingTaskExecutor：任务执行器的扩展，暴露与相关的潜在任务提交者的调度特性。
 * <li>TaskScheduler：任务调度器接口，抽象基于不同触发器的可运行任务的调度。
 * <li>Trigger：触发器对象的公共接口，确定相关任务的下一次执行时间。
 * <li>TriggerContext：封装一个给定任务的最后执行时间和最后完成时间的上下文对象。
 * </ul>
 *
 * <p>General exceptions for Spring's scheduling support,
 * independent of any specific scheduling system.
 *
 */
package org.springframework.scheduling;

