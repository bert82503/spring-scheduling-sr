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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 设置一个执行器服务({@link ExecutorService})的基类，通常是一个线程池执行器({@link ThreadPoolExecutor})。
 * 定义公共的配置设置项和生命周期处理，继承自"自定义线程工厂(CustomizableThreadFactory)"。
 * 
 * <p>Base class for classes that are setting up a
 * {@code java.util.concurrent.ExecutorService}
 * (typically a {@link java.util.concurrent.ThreadPoolExecutor}).
 * Defines common configuration settings and common lifecycle handling.
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.Executors
 * @see java.util.concurrent.ThreadPoolExecutor
 */
@SuppressWarnings("serial")
public abstract class ExecutorConfigurationSupport extends CustomizableThreadFactory
		implements BeanNameAware, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	// 线程工厂，用于创建新的线程实例，默认使用自身实例
	private ThreadFactory threadFactory = this;

	// 任务被拒绝执行的处理策略，默认采用"终止策略"
	// 其它策略：调用者继续运行策略(CallerRunsPolicy)、丢弃策略(DiscardPolicy)、丢弃最老的未处理请求策略(DiscardOldestPolicy)
	private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();

	// 是否等待任务完成才终止，默认是直接终止任务线程
	private boolean waitForTasksToCompleteOnShutdown = false;

	// 等待终止的秒数，默认是0秒(即立即终止)
	private int awaitTerminationSeconds = 0;

	// 是否设置了线程名称的前缀，默认是未设置
	private boolean threadNamePrefixSet = false;

	// bean名称，通过"Bean名称感知器"保存Bean配置信息
	private String beanName;

	// [core] 执行器服务，用于管理任务终止并产生一个异步计算的结果
	private ExecutorService executor;


	/**
	 * 设置线程创建工厂，以便在执行器服务的线程池中使用。
	 * 默认使用背后的执行器服务的默认线程工厂，即使用自己作为线程工厂。
	 * 
	 * <p>Set the ThreadFactory to use for the ExecutorService's thread pool.
	 * Default is the underlying ExecutorService's default thread factory.
	 * @see java.util.concurrent.Executors#defaultThreadFactory()
	 */
	public void setThreadFactory(ThreadFactory threadFactory) {
		this.threadFactory = (threadFactory != null ? threadFactory : this);
	}

	@Override
	public void setThreadNamePrefix(String threadNamePrefix) {
		super.setThreadNamePrefix(threadNamePrefix);
		this.threadNamePrefixSet = true;
	}

	/**
	 * 设置任务被拒绝执行的处理器。
	 * 默认采用"终止策略"(任务被拒绝时，处理器会抛出一个被拒绝执行的异常)。
	 * 
	 * <p>Set the RejectedExecutionHandler to use for the ExecutorService.
	 * Default is the ExecutorService's default abort policy.
	 * 
	 * @see java.util.concurrent.ThreadPoolExecutor.AbortPolicy
	 */
	public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
		this.rejectedExecutionHandler =
				(rejectedExecutionHandler != null ? rejectedExecutionHandler : new ThreadPoolExecutor.AbortPolicy());
	}

	/**
	 * 设置被调度的任务是否等待完成后才终止，或中断运行的任务并执行队列中的所有任务。
	 * 
	 * <p>默认是"false"，即通过中断正在进行的任务并清空队列来立即关闭。
	 * 设置为"true"，表示期望通过较长的关闭等待阶段，执行完所有的任务。
	 * 
	 * <p><b>注意：</b>Spring容器会等到正在进行的任务都完成之后，才关闭自己。
	 * 如果希望在容器的其余部分继续关闭之前该执行器被阻塞并等待任务的终止，
	 * 请设置{@link #setAwaitTerminationSeconds "awaitTerminationSeconds"}属性。
	 * 
	 * <p>Set whether to wait for scheduled tasks to complete on shutdown,
	 * not interrupting running tasks and executing all tasks in the queue.
	 * 
	 * <p>Default is "false", shutting down immediately through interrupting
	 * ongoing tasks and clearing the queue. Switch this flag to "true" if you
	 * prefer fully completed tasks at the expense of a longer shutdown phase.
	 * 
	 * <p>Note that Spring's container shutdown continues while ongoing tasks
	 * are being completed. If you want this executor to block and wait for the
	 * termination of tasks before the rest of the container continues to shut
	 * down - e.g. in order to keep up other resources that your tasks may need -,
	 * set the {@link #setAwaitTerminationSeconds "awaitTerminationSeconds"}
	 * property instead of or in addition to this property.
	 * 
	 * @see java.util.concurrent.ExecutorService#shutdown()
	 * @see java.util.concurrent.ExecutorService#shutdownNow()
	 */
	public void setWaitForTasksToCompleteOnShutdown(boolean waitForJobsToCompleteOnShutdown) {
		this.waitForTasksToCompleteOnShutdown = waitForJobsToCompleteOnShutdown;
	}

	/**
	 * 设置执行器阻塞关闭的最大等待秒数，以便任务在容器的其余部分继续关闭之前完成执行。
	 * 如果剩余的任务可能需要访问容器管理的其它资源时，这会特别有用。
	 * 
	 * <p>默认情况下，此执行器不会等待任务的终止。如果
	 * {@link #setWaitForTasksToCompleteOnShutdown "waitForTasksToCompleteOnShutdown"}
	 * 标记被设置为{@code true}，它将继续执行所有正在运行的任务和所有队列中剩余的任务。
	 * 
	 * <p>作为一个经验法则：如果设置"waitForTasksToCompleteOnShutdown"为{@code true}，
	 * 则需同时指定最大任务超时时间，这样队列中的所有剩余任务才能得到执行。
	 * 
	 * <p>Set the maximum number of seconds that this executor is supposed to block
	 * on shutdown in order to wait for remaining tasks to complete their execution
	 * before the rest of the container continues to shut down. This is particularly
	 * useful if your remaining tasks are likely to need access to other resources
	 * that are also managed by the container.
	 * 
	 * <p>By default, this executor won't wait for the termination of tasks at all.
	 * It will either shut down immediately, interrupting ongoing tasks and clearing
	 * the remaining task queue - or, if the
	 * {@link #setWaitForTasksToCompleteOnShutdown "waitForTasksToCompleteOnShutdown"}
	 * flag has been set to {@code true}, it will continue to fully execute all
	 * ongoing tasks as well as all remaining tasks in the queue, in parallel to
	 * the rest of the container shutting down.
	 * 
	 * <p>In either case, if you specify an await-termination period using this property,
	 * this executor will wait for the given time (max) for the termination of tasks.
	 * As a rule of thumb, specify a significantly higher timeout here if you set
	 * "waitForTasksToCompleteOnShutdown" to {@code true} at the same time,
	 * since all remaining tasks in the queue will still get executed - in contrast
	 * to the default shutdown behavior where it's just about waiting for currently
	 * executing tasks that aren't reacting to thread interruption.
	 * 
	 * @see java.util.concurrent.ExecutorService#shutdown()
	 * @see java.util.concurrent.ExecutorService#awaitTermination
	 */
	public void setAwaitTerminationSeconds(int awaitTerminationSeconds) {
		this.awaitTerminationSeconds = awaitTerminationSeconds;
	}

	// 在Bean工厂中设置bean的名称(BeanNameAware)
	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}


	// # 初始化"执行器服务"(InitializingBean)
	/**
	 * 在容器应用所有属性值之后，调用初始化方法({@link #initialize()})。
	 * 
	 * <p>Calls {@code initialize()} after the container applied all property values.
	 * @see #initialize()
	 */
//	@PostConstruct
	@Override
	public void afterPropertiesSet() {
		initialize();
	}

	/**
	 * 设置执行器服务(ExecutorService)。
	 * 
	 * <p>Set up the ExecutorService.
	 */
	public void initialize() {
		if (logger.isInfoEnabled()) { // 记录服务初始化日志
			logger.info("Initializing ExecutorService " + (this.beanName != null ? " '" + this.beanName + "'" : ""));
		}
		if (!this.threadNamePrefixSet && this.beanName != null) {
			// 默认使用Bean名称作为线程名称的前缀
			setThreadNamePrefix(this.beanName + "-");
		}
		// 初始化执行器
		this.executor = initializeExecutor(this.threadFactory, this.rejectedExecutionHandler);
	}

	/**
	 * 创建目标执行器服务({@link java.util.concurrent.ExecutorService})实例。
	 * 通过{@link #afterPropertiesSet()}方法调用。
	 * 
	 * <p>Create the target {@link java.util.concurrent.ExecutorService} instance.
	 * Called by {@code afterPropertiesSet}.
	 * 
	 * @param threadFactory the ThreadFactory to use
	 * @param rejectedExecutionHandler the RejectedExecutionHandler to use
	 * @return a new ExecutorService instance
	 * @see #afterPropertiesSet()
	 */
	protected abstract ExecutorService initializeExecutor(
			ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler);


	// # 释放并关闭服务(DisposableBean)
	/**
	 * 当Bean工厂(BeanFactory)销毁任务执行器实例时，调用关闭方法({@link #shutdown()})。
	 * 
	 * <p>Calls {@code shutdown} when the BeanFactory destroys
	 * the task executor instance.
	 * @see #shutdown()
	 */
//	@PreDestroy
	@Override
	public void destroy() {
		shutdown();
	}

	/**
	 * 在背后的执行器服务上执行关闭操作，就是调用{@link ExecutorService#shutdown()}。
	 * 
	 * <p>Perform a shutdown on the underlying ExecutorService.
	 * 
	 * @see java.util.concurrent.ExecutorService#shutdown()
	 * @see java.util.concurrent.ExecutorService#shutdownNow()
	 * @see #awaitTerminationIfNecessary()
	 */
	public void shutdown() {
		if (logger.isInfoEnabled()) { // 记录服务关闭日志
			logger.info("Shutting down ExecutorService" + (this.beanName != null ? " '" + this.beanName + "'" : ""));
		}
		if (this.waitForTasksToCompleteOnShutdown) {
			this.executor.shutdown(); // 等待任务执行完再关闭
		} else {
			this.executor.shutdownNow(); // 立即停止所有活跃的执行任务
		}
		awaitTerminationIfNecessary();
	}

	/**
	 * 根据{@link #setAwaitTerminationSeconds "awaitTerminationSeconds"}属性值，
	 * 等待执行器终止。
	 * 
	 * <p>Wait for the executor to terminate, according to the value of the
	 * {@link #setAwaitTerminationSeconds "awaitTerminationSeconds"} property.
	 * 
	 * @see java.util.concurrent.ExecutorService#awaitTermination(long, TimeUnit)
	 */
	private void awaitTerminationIfNecessary() {
		if (this.awaitTerminationSeconds > 0) {
			try {
				if (!this.executor.awaitTermination(this.awaitTerminationSeconds, TimeUnit.SECONDS)) {
					if (logger.isWarnEnabled()) { // 在等待执行器终止时，发生"超时"
						logger.warn("Timed out while waiting for executor" +
								(this.beanName != null ? " '" + this.beanName + "'" : "") + " to terminate");
					}
				}
			}
			catch (InterruptedException ex) { // 在等待执行器终止时，发生"中断异常"
				if (logger.isWarnEnabled()) {
					logger.warn("Interrupted while waiting for executor" +
							(this.beanName != null ? " '" + this.beanName + "'" : "") + " to terminate");
				}
				Thread.currentThread().interrupt(); // 强制中断当前正在执行的线程
			}
		}
	}

}
