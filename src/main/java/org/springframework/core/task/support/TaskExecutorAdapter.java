/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.core.task.support;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.util.Assert;

/**
 * 持有一个JDK执行器({@link Executor})的适配器，并将其暴露为一个Spring的任务执行器({@link TaskExecutor})。
 * 也可以探测一个扩展的执行器服务({@link ExecutorService})，通过适配异步任务执行器({@link AsyncTaskExecutor})接口。
 * 
 * <p>Adapter that takes a JDK {@code java.util.concurrent.Executor} and
 * exposes a Spring {@link org.springframework.core.task.TaskExecutor} for it.
 * Also detects an extended {@code java.util.concurrent.ExecutorService}, adapting
 * the {@link org.springframework.core.task.AsyncTaskExecutor} interface accordingly.
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.Executors
 */
public class TaskExecutorAdapter implements AsyncTaskExecutor { // [目标接口]

	/*
	 * 适配器模式 定义，见《Head First 设计模式》P243
	 * 示例：将"枚举"适配到"迭代器"，P249
	 */
	// [被适配者接口] JDK的并发执行器
	private final Executor concurrentExecutor;


	/**
	 * 使用给定的JDK并发执行器来创建一个新的任务执行器适配器。
	 * 
	 * <p>Create a new TaskExecutorAdapter, using the given JDK concurrent executor.
	 * 
	 * @param concurrentExecutor the JDK concurrent executor to delegate to
	 */
	public TaskExecutorAdapter(Executor concurrentExecutor) {
		Assert.notNull(concurrentExecutor, "Executor must not be null");
		this.concurrentExecutor = concurrentExecutor;
	}


	// # 任务执行器
	/**
	 * 委托到指定的JDK并发执行器。
	 * 
	 * <p>Delegates to the specified JDK concurrent executor.
	 * @see java.util.concurrent.Executor#execute(Runnable)
	 */
	@Override
	public void execute(Runnable task) {
		try {
			this.concurrentExecutor.execute(task);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(
					"Executor [" + this.concurrentExecutor + "] did not accept task: " + task, ex);
		}
	}

	// # 异步任务执行器
	@Override
	public void execute(Runnable task, long startTimeout) {
		execute(task);
	}

	@Override
	public Future<?> submit(Runnable task) {
		try {
			if (this.concurrentExecutor instanceof ExecutorService) { // 为"执行器服务"类实例
				return ((ExecutorService) this.concurrentExecutor).submit(task);
			}
			else { // 否则，使用一个"可取消的异步计算任务(FutureTask)"执行任务
				FutureTask<Object> future = new FutureTask<Object>(task, null);
				this.concurrentExecutor.execute(future);
				return future;
			}
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(
					"Executor [" + this.concurrentExecutor + "] did not accept task: " + task, ex);
		}
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		try {
			if (this.concurrentExecutor instanceof ExecutorService) { // 为"执行器服务"类实例
				return ((ExecutorService) this.concurrentExecutor).submit(task);
			}
			else { // 否则，使用一个"可取消的异步计算任务(FutureTask)"执行任务
				FutureTask<T> future = new FutureTask<T>(task);
				this.concurrentExecutor.execute(future);
				return future;
			}
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException(
					"Executor [" + this.concurrentExecutor + "] did not accept task: " + task, ex);
		}
	}

}
