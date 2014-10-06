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

package org.springframework.scheduling.concurrent;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.util.Assert;
import org.springframework.util.ErrorHandler;

/**
 * 实现Spring的任务调度器接口({@link TaskScheduler})，
 * 并包装一个JDK原生的调度的线程池执行器({@link ScheduledThreadPoolExecutor})。
 * 
 * <p>Implementation of Spring's {@link TaskScheduler} interface, wrapping
 * a native {@link java.util.concurrent.ScheduledThreadPoolExecutor}.
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 3.0
 * @see #setPoolSize
 * @see #setThreadFactory
 * @see #setErrorHandler
 */
@SuppressWarnings("serial")
public class ThreadPoolTaskScheduler extends ExecutorConfigurationSupport
		implements TaskScheduler, SchedulingTaskExecutor {

	/*
	 * 使用volatile类型来确保多线程之间读写的可视性
	 */
	// 核心线程池大小，默认为1个线程
	private volatile int poolSize = 1;

	// JDK原生的调度的执行器服务
	private volatile ScheduledExecutorService scheduledExecutor;

	// 错误处理器(处理错误的策略)
	private volatile ErrorHandler errorHandler;


	/**
	 * 设置"调度的执行器服务(ScheduledExecutorService)"的核心线程池大小。
	 * 默认为1
	 * 
	 * <p>Set the ScheduledExecutorService's pool size.
	 * Default is 1.
	 */
	public void setPoolSize(int poolSize) {
		Assert.isTrue(poolSize > 0, "'poolSize' must be 1 or higher");
		this.poolSize = poolSize;
	}

	/**
	 * 提供一个"错误处理器({@link ErrorHandler})策略"。
	 * 
	 * <p>Provide an {@link ErrorHandler} strategy.
	 */
	public void setErrorHandler(ErrorHandler errorHandler) {
		Assert.notNull(errorHandler, "'errorHandler' must not be null");
		this.errorHandler = errorHandler;
	}


	// ExecutorConfigurationSupport
	@Override
	protected ExecutorService initializeExecutor(
			ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {

		this.scheduledExecutor = createExecutor(this.poolSize, threadFactory, rejectedExecutionHandler);
		return this.scheduledExecutor;
	}

	/**
	 * 创建一个新的"调度的执行器服务({@link ScheduledExecutorService})"实例。
	 * 
	 * <p>默认实现是创建一个"调度的线程池执行器({@link ScheduledThreadPoolExecutor})"，
	 * 其可被自定义的"调度的执行器服务"实例覆盖。
	 * 
	 * <p>Create a new {@link ScheduledExecutorService} instance.
	 * 
	 * <p>The default implementation creates a {@link ScheduledThreadPoolExecutor}.
	 * Can be overridden in subclasses to provide custom {@link ScheduledExecutorService} instances.
	 * 
	 * @param poolSize the specified pool size
	 * @param threadFactory the ThreadFactory to use
	 * @param rejectedExecutionHandler the RejectedExecutionHandler to use
	 * @return a new ScheduledExecutorService instance
	 * @see #afterPropertiesSet()
	 * @see java.util.concurrent.ScheduledThreadPoolExecutor
	 */
	protected ScheduledExecutorService createExecutor(
			int poolSize, ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
		// 返回"调度的线程池执行器"
		return new ScheduledThreadPoolExecutor(poolSize, threadFactory, rejectedExecutionHandler);
	}

	/**
	 * 返回背后实现的"调度的执行器服务(ScheduledExecutorService)"。
	 * 
	 * <p>Return the underlying ScheduledExecutorService for native access.
	 * 
	 * @return the underlying ScheduledExecutorService (never {@code null})
	 * @throws IllegalStateException if the ThreadPoolTaskScheduler hasn't been initialized yet
	 */
	public ScheduledExecutorService getScheduledExecutor() throws IllegalStateException {
		Assert.state(this.scheduledExecutor != null, "ThreadPoolTaskScheduler not initialized");
		return this.scheduledExecutor;
	}


	// SchedulingTaskExecutor implementation (直接委托给"可调度的任务执行器"来处理)

	// 执行任务
	// # TaskExecutor
	@Override
	public void execute(Runnable task) {
		Executor executor = getScheduledExecutor();
		try {
			executor.execute(errorHandlingTask(task, false));
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}

	// # AsyncTaskExecutor
	@Override
	public void execute(Runnable task, long startTimeout) {
		execute(task);
	}

	// 提交任务
	@Override
	public Future<?> submit(Runnable task) {
		ExecutorService executor = getScheduledExecutor();
		try {
			return executor.submit(errorHandlingTask(task, false));
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		ExecutorService executor = getScheduledExecutor();
		try {
			Callable<T> taskToUse = task;
			if (this.errorHandler != null) {
				taskToUse = new DelegatingErrorHandlingCallable<T>(task, this.errorHandler);
			}
			return executor.submit(taskToUse);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}

	// # SchedulingTaskExecutor
	@Override
	public boolean prefersShortLivedTasks() {
		return true;
	}


	// TaskScheduler implementation ("任务调度器"实现)

	/*
	 * 基于"可重复调度的可运行任务(ReschedulingRunnable)"来实现任务调度。
	 */
	@Override
	public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
		ScheduledExecutorService executor = getScheduledExecutor();
		try {
			ErrorHandler errorHandler =
					(this.errorHandler != null ? this.errorHandler : TaskUtils.getDefaultErrorHandler(true));
			return new ReschedulingRunnable(task, trigger, executor, errorHandler).schedule();
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}

	// 直接委托给"调度的执行器服务(ScheduledExecutorService)"来处理
	@Override
	public ScheduledFuture<?> schedule(Runnable task, Date startTime) {
		ScheduledExecutorService executor = getScheduledExecutor();
		// 首次调度延迟时间
		long initialDelay = startTime.getTime() - System.currentTimeMillis();
		try {
			return executor.schedule(errorHandlingTask(task, false), initialDelay, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
		ScheduledExecutorService executor = getScheduledExecutor();
		long initialDelay = startTime.getTime() - System.currentTimeMillis();
		try {
			return executor.scheduleAtFixedRate(errorHandlingTask(task, true), initialDelay, period, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
		ScheduledExecutorService executor = getScheduledExecutor();
		try {
			return executor.scheduleAtFixedRate(errorHandlingTask(task, true), 0, period, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
		ScheduledExecutorService executor = getScheduledExecutor();
		long initialDelay = startTime.getTime() - System.currentTimeMillis();
		try {
			return executor.scheduleWithFixedDelay(errorHandlingTask(task, true), initialDelay, delay, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
		ScheduledExecutorService executor = getScheduledExecutor();
		try {
			return executor.scheduleWithFixedDelay(errorHandlingTask(task, true), 0, delay, TimeUnit.MILLISECONDS);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}


	// 处理错误的任务
	private Runnable errorHandlingTask(Runnable task, boolean isRepeatingTask) {
		return TaskUtils.decorateTaskWithErrorHandler(task, this.errorHandler, isRepeatingTask);
	}


	/*
	 * 捕获从其委托的Callable抛出的任何异常或错误的Callable包装器。
	 */
	private static class DelegatingErrorHandlingCallable<V> implements Callable<V> {

		// 可返回结果的任务的委托者
		private final Callable<V> delegate;

		// 错误处理器
		private final ErrorHandler errorHandler;


		public DelegatingErrorHandlingCallable(Callable<V> delegate, ErrorHandler errorHandler) {
			this.delegate = delegate;
			this.errorHandler = errorHandler;
		}


		// Callable
		/*
		 * 调用委托的可返回结果的任务来执行，并使用错误处理器来处理错误。
		 */
		@Override
		public V call() throws Exception {
			try {
				return this.delegate.call();
			}
			catch (Throwable t) {
				this.errorHandler.handleError(t);
				return null;
			}
		}

	}

}
