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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.util.Assert;

/**
 * 允许以bean方式配置一个JDK 1.5的ThreadPoolExecutor的JavaBean，并作为一个Spring的TaskExecutor暴露。
 * 本类非常适用于管理和监控，其提供了一些有用的属性。如，核心可用连接数、
 * 最大可用连接数、队列容量、连接存活秒数、当前连接数、活跃连接数量。
 * 
 * <p>可选方法是，可以直接使用构造器注入来设置一个ThreadPoolExecutor实例，或者
 * 使用一个指向JDK 1.5 Executors类的工厂方法定义。
 * 为了暴露这个原始的执行器(Executor)作为一个Spring的任务执行器(TaskExecutor)，
 * 只需使用一个ConcurrentTaskExecutor适配器来简单包装一下。
 * 
 * <p><b>注意：</b>本类实现Spring的TaskExecutor接口和JDK 1.5 Executor接口。
 * 前者是主接口，后者只是作为辅助服务。出于这个原因，异常处理遵循TaskExecutor约定，
 * 而非Executor约定。
 * 
 * <p><b>如果你喜欢暴露原生的ExecutorService，
 * 那么考虑使用{@link ThreadPoolExecutorFactoryBean}来代替这个类。</b>
 * 
 * 
 * <p>JavaBean that allows for configuring a JDK 1.5 {@link java.util.concurrent.ThreadPoolExecutor}
 * in bean style (through its "corePoolSize", "maxPoolSize", "keepAliveSeconds", "queueCapacity"
 * properties) and exposing it as a Spring {@link org.springframework.core.task.TaskExecutor}.
 * This class is also well suited for management and monitoring (e.g. through JMX),
 * providing several useful attributes: "corePoolSize", "maxPoolSize", "keepAliveSeconds"
 * (all supporting updates at runtime); "poolSize", "activeCount" (for introspection only).
 *
 * <p>For an alternative, you may set up a ThreadPoolExecutor instance directly using
 * constructor injection, or use a factory method definition that points to the JDK 1.5
 * {@link java.util.concurrent.Executors} class. To expose such a raw Executor as a
 * Spring {@link org.springframework.core.task.TaskExecutor}, simply wrap it with a
 * {@link org.springframework.scheduling.concurrent.ConcurrentTaskExecutor} adapter.
 *
 * <p><b>NOTE:</b> This class implements Spring's
 * {@link org.springframework.core.task.TaskExecutor} interface as well as the JDK 1.5
 * {@link java.util.concurrent.Executor} interface, with the former being the primary
 * interface, the other just serving as secondary convenience. For this reason, the
 * exception handling follows the TaskExecutor contract rather than the Executor contract,
 * in particular regarding the {@link org.springframework.core.task.TaskRejectedException}.
 *
 * <p><b>If you prefer native {@link java.util.concurrent.ExecutorService} exposure instead,
 * consider {@link ThreadPoolExecutorFactoryBean} as an alternative to this class.</b>
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.core.task.TaskExecutor
 * @see java.util.concurrent.ThreadPoolExecutor
 * @see ConcurrentTaskExecutor
 */
@SuppressWarnings("serial")
public class ThreadPoolTaskExecutor extends ExecutorConfigurationSupport implements SchedulingTaskExecutor {

	// "线程池大小"监视对象(锁的引用对象)
	private final Object poolSizeMonitor = new Object();

	// [core] 核心线程池大小，默认为1个线程
	private int corePoolSize = 1;

	// [core] 最大可用线程池大小，默认为无限制
	private int maxPoolSize = Integer.MAX_VALUE;

	// [core] 连接存活时间，默认为60秒
	private int keepAliveSeconds = 60;

	// 是否允许核心线程池超时，默认为false，即无超时
	private boolean allowCoreThreadTimeOut = false;

	// [core] 队列容量，默认为无限制
	private int queueCapacity = Integer.MAX_VALUE;

	// [背后实现] 线程池执行器
	private ThreadPoolExecutor threadPoolExecutor;


	/**
	 * 设置线程池执行器(ThreadPoolExecutor)的"核心线程池大小"属性，默认为1个线程。
	 * 
	 * <p>Set the ThreadPoolExecutor's core pool size.
	 * Default is 1.
	 * <p><b>This setting can be modified at runtime, for example through JMX.</b>
	 */
	public void setCorePoolSize(int corePoolSize) {
		synchronized (this.poolSizeMonitor) { // 同步代码块
			// 同步原因：本对象的"核心线程池大小"属性要与背后实现的线程池执行器里的"核心线程池大小"保持一致
			this.corePoolSize = corePoolSize;
			if (this.threadPoolExecutor != null) {
				this.threadPoolExecutor.setCorePoolSize(corePoolSize);
			}
		}
	}

	/**
	 * Return the ThreadPoolExecutor's core pool size.
	 */
	public int getCorePoolSize() {
		synchronized (this.poolSizeMonitor) {
			return this.corePoolSize;
		}
	}

	/**
	 * 设置线程池执行器(ThreadPoolExecutor)的"最大可用线程池大小"属性，默认为无限制。
	 * 
	 * <p>Set the ThreadPoolExecutor's maximum pool size.
	 * Default is {@code Integer.MAX_VALUE}.
	 * <p><b>This setting can be modified at runtime, for example through JMX.</b>
	 */
	public void setMaxPoolSize(int maxPoolSize) {
		synchronized (this.poolSizeMonitor) { // 同步代码块
			// 同步原因：本对象的"最大可用线程池大小"属性要与背后实现的线程池执行器里的"最大可用线程池大小"保持一致
			this.maxPoolSize = maxPoolSize;
			if (this.threadPoolExecutor != null) {
				this.threadPoolExecutor.setMaximumPoolSize(maxPoolSize);
			}
		}
	}

	/**
	 * Return the ThreadPoolExecutor's maximum pool size.
	 */
	public int getMaxPoolSize() {
		synchronized (this.poolSizeMonitor) {
			return this.maxPoolSize;
		}
	}

	/**
	 * 设置线程池执行器(ThreadPoolExecutor)的"连接存活时间"属性，默认为60秒。
	 * 
	 * <p>Set the ThreadPoolExecutor's keep-alive seconds.
	 * Default is 60.
	 * <p><b>This setting can be modified at runtime, for example through JMX.</b>
	 */
	public void setKeepAliveSeconds(int keepAliveSeconds) {
		synchronized (this.poolSizeMonitor) { // 同步代码块
			// 同步原因：本对象的"连接存活时间"属性要与背后实现的线程池执行器里的"连接存活时间"保持一致
			this.keepAliveSeconds = keepAliveSeconds;
			if (this.threadPoolExecutor != null) {
				this.threadPoolExecutor.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
			}
		}
	}

	/**
	 * Return the ThreadPoolExecutor's keep-alive seconds.
	 */
	public int getKeepAliveSeconds() {
		synchronized (this.poolSizeMonitor) {
			return this.keepAliveSeconds;
		}
	}

	/**
	 * 指定是否允许核心线程池超时，它能对非空队列进行动态地扩展和收缩
	 * (因为一旦队列满了，"最大可用线程池大小"只会增长)。
	 * 默认为"false"
	 * 
	 * <p>Specify whether to allow core threads to time out. This enables dynamic
	 * growing and shrinking even in combination with a non-zero queue (since
	 * the max pool size will only grow once the queue is full).
	 * 
	 * <p>Default is "false". Note that this feature is only available on Java 6
	 * or above. On Java 5, consider switching to the backport-concurrent
	 * version of ThreadPoolTaskExecutor which also supports this feature.
	 * 
	 * @see java.util.concurrent.ThreadPoolExecutor#allowCoreThreadTimeOut(boolean)
	 */
	public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
		this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
	}

	/**
	 * 设置线程池执行器(ThreadPoolExecutor)的阻塞队列(BlockingQueue)的"容量"属性，默认为无限制。
	 * 
	 * <p>任何正整数都将使用"链表的阻塞队列(LinkedBlockingQueue)"实例；
	 * 其他值都将使用"同步队列(SynchronousQueue)"实例。
	 * 
	 * <p>Set the capacity for the ThreadPoolExecutor's BlockingQueue.
	 * Default is {@code Integer.MAX_VALUE}.
	 * 
	 * <p>Any positive value will lead to a LinkedBlockingQueue instance;
	 * any other value will lead to a SynchronousQueue instance.
	 * 
	 * @see java.util.concurrent.LinkedBlockingQueue
	 * @see java.util.concurrent.SynchronousQueue
	 */
	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}


	// # 初始化线程池执行器
	@Override
	protected ExecutorService initializeExecutor(
			ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
		// 1. 根据"队列容量"创建"阻塞队列"
		BlockingQueue<Runnable> queue = createQueue(this.queueCapacity);
		// 2. 创建"线程池执行器"
		ThreadPoolExecutor executor  = new ThreadPoolExecutor(
				this.corePoolSize, this.maxPoolSize, this.keepAliveSeconds, TimeUnit.SECONDS,
				queue, threadFactory, rejectedExecutionHandler);
		if (this.allowCoreThreadTimeOut) {
			executor.allowCoreThreadTimeOut(true);
		}

		this.threadPoolExecutor = executor;
		return executor;
	}

	/**
	 * 创建阻塞队列(BlockingQueue)，用于线程池执行器(ThreadPoolExecutor)。
	 * 
	 * <p>任何正整数队列容量值都将创建"链表的阻塞队列(LinkedBlockingQueue)"实例；
	 * 否则，创建"同步队列(SynchronousQueue)"实例。
	 * 
	 * <p>Create the BlockingQueue to use for the ThreadPoolExecutor.
	 * 
	 * <p>A LinkedBlockingQueue instance will be created for a positive
	 * capacity value; a SynchronousQueue else.
	 * 
	 * @param queueCapacity the specified queue capacity (给定的队列容量)
	 * @return the BlockingQueue instance (返回阻塞队列实例)
	 * @see java.util.concurrent.LinkedBlockingQueue
	 * @see java.util.concurrent.SynchronousQueue
	 */
	protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
		if (queueCapacity > 0) {
			return new LinkedBlockingQueue<Runnable>(queueCapacity);
		}
		else {
			return new SynchronousQueue<Runnable>();
		}
	}

	/**
	 * 返回背后实现的线程池执行器。
	 * 
	 * <p>Return the underlying ThreadPoolExecutor for native access.
	 * 
	 * @return the underlying ThreadPoolExecutor (never {@code null})
	 * @throws IllegalStateException if the ThreadPoolTaskExecutor hasn't been initialized yet
	 */
	public ThreadPoolExecutor getThreadPoolExecutor() throws IllegalStateException {
		Assert.state(this.threadPoolExecutor != null, "ThreadPoolTaskExecutor not initialized");
		return this.threadPoolExecutor;
	}

	/**
	 * 返回当前可用线程池大小。
	 * 
	 * <p>Return the current pool size.
	 * @see java.util.concurrent.ThreadPoolExecutor#getPoolSize()
	 */
	public int getPoolSize() {
		return getThreadPoolExecutor().getPoolSize();
	}

	/**
	 * 返回当前活跃线程的数量。
	 * 
	 * <p>Return the number of currently active threads.
	 * @see java.util.concurrent.ThreadPoolExecutor#getActiveCount()
	 */
	public int getActiveCount() {
		return getThreadPoolExecutor().getActiveCount();
	}


	// # 执行任务
	@Override
	public void execute(Runnable task) {
		// 1. 获取"执行器"
		Executor executor = getThreadPoolExecutor();
		try {
			// 2. 执行"任务"
			executor.execute(task);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}

	@Override
	public void execute(Runnable task, long startTimeout) {
		execute(task); // 未使用"开始执行超时时间"参数
	}

	@Override
	public Future<?> submit(Runnable task) {
		// 1. 获取"执行器服务"
		ExecutorService executor = getThreadPoolExecutor();
		try {
			// 2. 提交"任务"
			return executor.submit(task);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		// 1. 获取"执行器服务"
		ExecutorService executor = getThreadPoolExecutor();
		try {
			return executor.submit(task);
		}
		catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}

	/**
	 * 本任务执行器喜欢"短存活时间"的工作单元。
	 * 
	 * <p>This task executor prefers short-lived work units.
	 */
	@Override
	public boolean prefersShortLivedTasks() {
		return true;
	}

}
