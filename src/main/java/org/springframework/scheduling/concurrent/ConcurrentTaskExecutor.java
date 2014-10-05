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

package org.springframework.scheduling.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.SchedulingTaskExecutor;

/**
 * 持有一个JDK 1.5执行器({@link Executor})的适配器，并将其暴露为一个Spring的任务执行器({@link TaskExecutor})。
 * 还可以探测一个扩展的执行器服务({@link ExecutorService})，
 * 将其适配到调度任务执行器({@link SchedulingTaskExecutor})接口。
 * 
 * <p><b>请注意：</b>有一个预先构建的线程池任务执行器({@link ThreadPoolTaskExecutor})，
 * 其允许以bean方式定义一个JDK 1.5的线程池执行器({@link ThreadPoolExecutor})，
 * 并将其直接暴露为一个Spring的任务执行器({@link TaskExecutor})。
 * 这是一个方便的选择，使用独立于当前适配器类的定义的原生线程池执行器(ThreadPoolExecutor)定义。
 * 
 * 
 * <p>Adapter that takes a JDK 1.5 {@code java.util.concurrent.Executor} and
 * exposes a Spring {@link org.springframework.core.task.TaskExecutor} for it.
 * Also detects an extended {@code java.util.concurrent.ExecutorService}, adapting
 * the {@link SchedulingTaskExecutor} interface accordingly.
 *
 * <p>Note that there is a pre-built {@link ThreadPoolTaskExecutor} that allows for
 * defining a JDK 1.5 {@link java.util.concurrent.ThreadPoolExecutor} in bean style,
 * exposing it as a Spring {@link org.springframework.core.task.TaskExecutor} directly.
 * This is a convenient alternative to a raw ThreadPoolExecutor definition with
 * a separate definition of the present adapter class.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see java.util.concurrent.Executors
 * @see ThreadPoolTaskExecutor
 */
public class ConcurrentTaskExecutor implements SchedulingTaskExecutor { // [目标接口]

	// [被适配者接口] JDK的并发执行器
	private Executor concurrentExecutor;

	// [背后实现的适配器] 适配JDK的并发执行器接口
	private TaskExecutorAdapter adaptedExecutor;


	/**
	 * 使用默认的单线程执行器来创建一个新的并发任务执行器。
	 * 
	 * <p>Create a new ConcurrentTaskExecutor,
	 * using a single thread executor as default.
	 * 
	 * @see java.util.concurrent.Executors#newSingleThreadExecutor()
	 */
	public ConcurrentTaskExecutor() {
		setConcurrentExecutor(null);
	}

	/**
	 * 使用给定的JDK 1.5的并发执行器来创建一个新的并发任务执行器。
	 * 
	 * <p>Create a new ConcurrentTaskExecutor,
	 * using the given JDK 1.5 concurrent executor.
	 * 
	 * @param concurrentExecutor the JDK 1.5 concurrent executor to delegate to
	 */
	public ConcurrentTaskExecutor(Executor concurrentExecutor) {
		setConcurrentExecutor(concurrentExecutor);
	}


	/**
	 * 设置并委托给JDK 1.5的并发执行器。
	 * 
	 * <p>Specify the JDK 1.5 concurrent executor to delegate to.
	 */
	public final void setConcurrentExecutor(Executor concurrentExecutor) {
		// 默认使用单线程的执行器服务
		this.concurrentExecutor =
				(concurrentExecutor != null ? concurrentExecutor : Executors.newSingleThreadExecutor());
		this.adaptedExecutor = new TaskExecutorAdapter(this.concurrentExecutor);
	}

	/**
	 * 返回本适配器委托的JDK 1.5并发执行器。
	 * 
	 * <p>Return the JDK 1.5 concurrent executor that this adapter delegates to.
	 */
	public final Executor getConcurrentExecutor() {
		return this.concurrentExecutor;
	}


	// 所有的请求都委托给背后的任务执行器适配器（将JDK的并发执行器(Executor)适配到异步任务执行器(AsyncTaskExecutor)）
	// # 任务执行器
	@Override
	public void execute(Runnable task) {
		this.adaptedExecutor.execute(task);
	}

	// # 异步任务执行器
	@Override
	public void execute(Runnable task, long startTimeout) {
		this.adaptedExecutor.execute(task, startTimeout);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return this.adaptedExecutor.submit(task);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return this.adaptedExecutor.submit(task);
	}

	// # 调度任务执行器
	/**
	 * 本任务执行器喜欢短周期的工作单元。
	 * 
	 * <p>This task executor prefers short-lived work units.
	 */
	@Override
	public boolean prefersShortLivedTasks() {
		return true;
	}

}
