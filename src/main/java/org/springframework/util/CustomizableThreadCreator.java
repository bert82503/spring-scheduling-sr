/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.util;

import java.io.Serializable;

/**
 * 为创建新的线程实例提供简单地自定义辅助类。
 * 它提供各种bean属性，包括线程名称的前缀、线程的优先级、
 * 是否支持创建守护线程、所在的线程组 等。
 * 
 * <p>作为线程工厂({@link CustomizableThreadFactory})的基类
 * 
 * <p>Simple customizable helper class for creating new {@link Thread} instances.
 * Provides various bean properties: thread name prefix, thread priority, etc.
 *
 * <p>Serves as base class for thread factories such as
 * {@link org.springframework.scheduling.concurrent.CustomizableThreadFactory}.
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see org.springframework.scheduling.concurrent.CustomizableThreadFactory
 */
@SuppressWarnings("serial")
public class CustomizableThreadCreator implements Serializable {

	// 线程名称的前缀
	private String threadNamePrefix;

	// 线程的优先级，默认使用"正常优先级-5"(还包括最小优先级-1、最大优先级-10)
	private int threadPriority = Thread.NORM_PRIORITY;

	// 是否支持创建守护线程，默认为false(不支持)
	private boolean daemon = false;

	// 所在的线程组
	private ThreadGroup threadGroup;


	// 线程计数器，用于生成线程名称的后缀
	private int threadCount = 0;

	// 线程计数监视器，作为"线程计数器"并发控制的同步对象
	private final Object threadCountMonitor = new SerializableMonitor();


	/**
	 * 使用默认的线程名称的前缀来创建一个新的可定制的线程创建者。
	 * 
	 * <p>Create a new CustomizableThreadCreator with default thread name prefix.
	 */
	public CustomizableThreadCreator() {
		this.threadNamePrefix = getDefaultThreadNamePrefix();
	}

	/**
	 * Create a new CustomizableThreadCreator with the given thread name prefix.
	 * @param threadNamePrefix the prefix to use for the names of newly created threads
	 */
	public CustomizableThreadCreator(String threadNamePrefix) {
		this.threadNamePrefix = (threadNamePrefix != null ? threadNamePrefix : getDefaultThreadNamePrefix());
	}


	/**
	 * 指定新创建的线程的名称前缀。
	 * 
	 * <p>Specify the prefix to use for the names of newly created threads.
	 * Default is "SimpleAsyncTaskExecutor-".
	 */
	public void setThreadNamePrefix(String threadNamePrefix) {
		this.threadNamePrefix = (threadNamePrefix != null ? threadNamePrefix : getDefaultThreadNamePrefix());
	}

	/**
	 * Return the thread name prefix to use for the names of newly
	 * created threads.
	 */
	public String getThreadNamePrefix() {
		return this.threadNamePrefix;
	}

	/**
	 * 设置这个工厂创建的线程的优先级，默认是5。
	 * 
	 * <p>Set the priority of the threads that this factory creates.
	 * Default is 5.
	 * @see java.lang.Thread#NORM_PRIORITY
	 */
	public void setThreadPriority(int threadPriority) {
		this.threadPriority = threadPriority;
	}

	/**
	 * Return the priority of the threads that this factory creates.
	 */
	public int getThreadPriority() {
		return this.threadPriority;
	}

	/**
	 * 设置这个工厂是否支持创建守护线程，仅在运行的应用自身环境中执行。
	 * 
	 * <p>默认是"false"，表示不支持：实际的工厂通常支持显示取消。
	 * 这样，如果应用程序关闭了，那么可运行的任务默认也会完成它们的执行。
	 * 
	 * <p>指定为"true"，是希望即使应用程序自身关闭了，那时正在执行的可运行任务线程依旧继续执行。
	 * 
	 * <p>Set whether this factory is supposed to create daemon threads,
	 * just executing as long as the application itself is running.
	 * <p>Default is "false": Concrete factories usually support explicit cancelling.
	 * Hence, if the application shuts down, Runnables will by default finish their
	 * execution.
	 * <p>Specify "true" for eager shutdown of threads which still actively execute
	 * a {@link Runnable} at the time that the application itself shuts down.
	 * @see java.lang.Thread#setDaemon
	 */
	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	/**
	 * Return whether this factory should create daemon threads.
	 */
	public boolean isDaemon() {
		return this.daemon;
	}

	/**
	 * 指定待创建线程所在的线程组的名称。
	 * 
	 * <p>Specify the name of the thread group that threads should be created in.
	 * @see #setThreadGroup
	 */
	public void setThreadGroupName(String name) {
		this.threadGroup = new ThreadGroup(name);
	}

	/**
	 * 指定待创建线程所在的线程组。
	 * 
	 * <p>Specify the thread group that threads should be created in.
	 * @see #setThreadGroupName
	 */
	public void setThreadGroup(ThreadGroup threadGroup) {
		this.threadGroup = threadGroup;
	}

	/**
	 * Return the thread group that threads should be created in
	 * (or {@code null} for the default group).
	 */
	public ThreadGroup getThreadGroup() {
		return this.threadGroup;
	}


	/**
	 * 创建一个新的线程({@link Thread})的模板方法。
	 * 
	 * 默认实现是为给定的可运行任务创建一个新的线程，通过应用适当的线程名称。
	 * 
	 * <p>Template method for the creation of a new {@link Thread}.
	 * 
	 * <p>The default implementation creates a new Thread for the given
	 * {@link Runnable}, applying an appropriate thread name.
	 * 
	 * @param runnable the Runnable to execute
	 * @see #nextThreadName()
	 */
	public Thread createThread(Runnable runnable) {
		Thread thread = new Thread(getThreadGroup(), runnable, nextThreadName());
		thread.setPriority(getThreadPriority());
		thread.setDaemon(isDaemon());
		return thread;
	}

	/**
	 * 返回用于新创建的线程({@link Thread})的名称。
	 * 
	 * 默认实现是返回"指定的线程名称的前缀+递增的线程计数"。
	 * 
	 * <p>Return the thread name to use for a newly created {@link Thread}.
	 * 
	 * <p>The default implementation returns the specified thread name prefix
	 * with an increasing thread count appended: e.g. "SimpleAsyncTaskExecutor-0".
	 * 
	 * @see #getThreadNamePrefix()
	 */
	protected String nextThreadName() {
		int threadNumber = 0;
		synchronized (this.threadCountMonitor) { // 同步"线程计数监视器"对象来实现并发递增（为什么不直接使用AtomicInteger?）
			this.threadCount++;
			threadNumber = this.threadCount;
		}
		return getThreadNamePrefix() + threadNumber;
	}

	/**
	 * 构建默认的线程名称的前缀，返回格式为"类名-"。
	 * 
	 * <p>Build the default thread name prefix for this factory.
	 * 
	 * @return the default thread name prefix (never {@code null})
	 */
	protected String getDefaultThreadNamePrefix() {
		return ClassUtils.getShortName(getClass()) + "-";
	}


	/**
	 * Empty class used for a serializable monitor object.
	 */
	private static class SerializableMonitor implements Serializable {
	}

}
