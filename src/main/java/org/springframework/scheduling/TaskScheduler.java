/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.scheduling;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

/**
 * 抽象基于不同类型的触发器的可运行线程({@link Runnable})调度的任务调度接口。
 * 
 * <p>本接口与调度的任务执行器接口({@link SchedulingTaskExecutor})分开，
 * 是因为它通常表示不同类型的后端实现。
 * 
 * <p>默认实现是线程池任务调度器({@link ThreadPoolTaskScheduler})，
 * 其包装一个原生的调度的执行器服务接口({@link ScheduledExecutorService})并增加扩展的触发器功能。
 * 
 * <p>本接口大致相当于JSR-236的可管理调度的执行服务器接口({@code ManagedScheduledExecutorService})，
 * 在Java EE 6环境中支持。但在Spring 3.0发布时，JSR-236接口并未发布。
 * 
 * 
 * <p>Task scheduler interface that abstracts the scheduling of
 * {@link Runnable Runnables} based on different kinds of triggers.
 *
 * <p>This interface is separate from {@link SchedulingTaskExecutor} since it
 * usually represents for a different kind of backend, i.e. a thread pool with
 * different characteristics and capabilities. Implementations may implement
 * both interfaces if they can handle both kinds of execution characteristics.
 *
 * <p>The 'default' implementation is
 * {@link org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler},
 * wrapping a native {@link java.util.concurrent.ScheduledExecutorService}
 * and adding extended trigger capabilities.
 *
 * <p>This interface is roughly equivalent to a JSR-236
 * {@code ManagedScheduledExecutorService} as supported in Java EE 6
 * environments. However, at the time of the Spring 3.0 release, the
 * JSR-236 interfaces have not been released in official form yet.
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see org.springframework.core.task.TaskExecutor
 * @see java.util.concurrent.ScheduledExecutorService
 * @see org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
 */
public interface TaskScheduler {

	/**
	 * 调度给定的可运行任务，当触发器指示下一次执行的时间时就调用它。
	 * 
	 * <p>一旦调度器关闭或返回的结果延迟可接受的任务({@link ScheduledFuture})被取消时，
	 * 执行将结束。
	 * 
	 * <p>Schedule the given {@link Runnable}, invoking it whenever the trigger
	 * indicates a next execution time.
	 * 
	 * <p>Execution will end once the scheduler shuts down or the returned
	 * {@link ScheduledFuture} gets cancelled.
	 * 
	 * @param task the Runnable to execute whenever the trigger fires
	 * @param trigger an implementation of the {@link Trigger} interface (触发器框架的一个实现),
	 * e.g. a {@link org.springframework.scheduling.support.CronTrigger} object
	 * wrapping a cron expression
	 * @return a {@link ScheduledFuture} representing pending completion of the task (表示等待任务完成),
	 * or {@code null} if the given Trigger object never fires (i.e. returns
	 * {@code null} from {@link Trigger#nextExecutionTime})
	 * @throws org.springframework.core.task.TaskRejectedException if the given task was not accepted
	 * for internal reasons (e.g. a pool overload handling policy or a pool shutdown in progress)
	 * @see org.springframework.scheduling.support.CronTrigger
	 */
	ScheduledFuture<?> schedule(Runnable task, Trigger trigger);

	/**
	 * 调度给定的可运行任务，在指定的开始执行时间调用它。
	 * 
	 * <p>Schedule the given {@link Runnable}, invoking it at the specified execution time.
	 * <p>Execution will end once the scheduler shuts down or the returned
	 * {@link ScheduledFuture} gets cancelled.
	 * 
	 * @param task the Runnable to execute whenever the trigger fires
	 * @param startTime the desired execution time for the task (任务期望的执行时间)
	 * (if this is in the past, the task will be executed immediately, i.e. as soon as possible)
	 * @return a {@link ScheduledFuture} representing pending completion of the task
	 * @throws org.springframework.core.task.TaskRejectedException if the given task was not accepted
	 * for internal reasons (e.g. a pool overload handling policy or a pool shutdown in progress)
	 */
	ScheduledFuture<?> schedule(Runnable task, Date startTime);

	/**
	 * 调度给定的可运行任务，在指定的开始执行时间调用它，并随后使用给定的周期时间定期地调用它。
	 * 
	 * <p>Schedule the given {@link Runnable}, invoking it at the specified execution time
	 * and subsequently with the given period.
	 * <p>Execution will end once the scheduler shuts down or the returned
	 * {@link ScheduledFuture} gets cancelled.
	 * 
	 * @param task the Runnable to execute whenever the trigger fires
	 * @param startTime the desired first execution time for the task
	 * (if this is in the past, the task will be executed immediately, i.e. as soon as possible)
	 * @param period the interval between successive executions of the task (in milliseconds)
	 * 任务连续执行的时间间隔 (毫秒)
	 * @return a {@link ScheduledFuture} representing pending completion of the task
	 * @throws org.springframework.core.task.TaskRejectedException if the given task was not accepted
	 * for internal reasons (e.g. a pool overload handling policy or a pool shutdown in progress)
	 */
	ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period);

	/**
	 * 调度给定的可运行任务，在任务初始化完成之后就开始运行，并使用给定的周期时间定期地调用它。
	 * 
	 * <p>Schedule the given {@link Runnable}, starting as soon as possible and
	 * invoking it with the given period.
	 * <p>Execution will end once the scheduler shuts down or the returned
	 * {@link ScheduledFuture} gets cancelled.
	 * 
	 * @param task the Runnable to execute whenever the trigger fires
	 * @param period the interval between successive executions of the task (in milliseconds)
	 * @return a {@link ScheduledFuture} representing pending completion of the task
	 * @throws org.springframework.core.task.TaskRejectedException if the given task was not accepted
	 * for internal reasons (e.g. a pool overload handling policy or a pool shutdown in progress)
	 */
	ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period);

	/**
	 * 调度给定的可运行任务，在指定的开始执行时间调用它，
	 * 并随后使用在一次执行完成和下一次执行开始之间的延迟时间定期地调用它。
	 * 
	 * <p>Schedule the given {@link Runnable}, invoking it at the specified execution time
	 * and subsequently with the given delay between the completion of one execution
	 * and the start of the next.
	 * <p>Execution will end once the scheduler shuts down or the returned
	 * {@link ScheduledFuture} gets cancelled.
	 * 
	 * @param task the Runnable to execute whenever the trigger fires
	 * @param startTime the desired first execution time for the task
	 * (if this is in the past, the task will be executed immediately, i.e. as soon as possible)
	 * @param delay the delay between the completion of one execution and the start
	 * of the next (in milliseconds) 在一次执行完成和下一次开始执行之间的延迟时间 (毫秒)
	 * @return a {@link ScheduledFuture} representing pending completion of the task
	 * @throws org.springframework.core.task.TaskRejectedException if the given task was not accepted
	 * for internal reasons (e.g. a pool overload handling policy or a pool shutdown in progress)
	 */
	ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay);

	/**
	 * 调度给定的可运行任务，在任务初始化完成之后就开始运行，
	 * 并随后使用在一次执行完成和下一次执行开始之间的延迟时间定期地调用它。
	 * 
	 * <p>Schedule the given {@link Runnable}, starting as soon as possible and
	 * invoking it with the given delay between the completion of one execution
	 * and the start of the next.
	 * <p>Execution will end once the scheduler shuts down or the returned
	 * {@link ScheduledFuture} gets cancelled.
	 * 
	 * @param task the Runnable to execute whenever the trigger fires
	 * @param delay the interval between successive executions of the task (in milliseconds)
	 * @return a {@link ScheduledFuture} representing pending completion of the task
	 * @throws org.springframework.core.task.TaskRejectedException if the given task was not accepted
	 * for internal reasons (e.g. a pool overload handling policy or a pool shutdown in progress)
	 */
	ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay);

}
