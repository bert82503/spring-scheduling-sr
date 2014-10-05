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

import java.util.concurrent.ForkJoinPool;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * 一个构建并暴露一个预先配置的"Fork-Join"任务池({@link ForkJoinPool})的
 * Spring工厂Bean({@link FactoryBean})。
 * 可以在Java 7上使用，甚至在使用{@code jsr166.jar}的Java 6上使用。
 * 继承自{@link FactoryBean<ForkJoinPool>}
 * 
 * <p>有关ForkJoinPool API及其递归行为(RecursiveActions)使用的详细信息，请参阅
 * <a href="http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ForkJoinPool.html">"JDK 7文档"</a>。
 * 
 * <p>{@code jsr166.jar}，包含Java 6的并发包({@code java.util.concurrent})更新，
 * 可以从<a href="http://gee.cs.oswego.edu/dl/concurrency-interest/">"并发兴趣站点"</a>
 * 获取到。
 * 
 * <p>A Spring {@link FactoryBean} that builds and exposes a preconfigured {@link ForkJoinPool}.
 * May be used on Java 7 as well as on Java 6 with {@code jsr166.jar} on the classpath
 * (ideally on the VM bootstrap classpath).
 *
 * <p>For details on the ForkJoinPool API and its use with RecursiveActions, see the
 * <a href="http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ForkJoinPool.html">JDK 7 javadoc</a>.
 *
 * <p>{@code jsr166.jar}, containing {@code java.util.concurrent} updates for Java 6, can be obtained
 * from the <a href="http://gee.cs.oswego.edu/dl/concurrency-interest/">concurrency interest website</a>.
 *
 * @author Juergen Hoeller
 * @since 3.1
 */
public class ForkJoinPoolFactoryBean implements FactoryBean<ForkJoinPool>, InitializingBean, DisposableBean {

	// 用于运行Java虚拟机的处理器数量
	private int parallelism = Runtime.getRuntime().availableProcessors();

	// "Fork-Join"工作者线程工厂，默认与"Fork-Join"任务池使用一样的工厂
	private ForkJoinPool.ForkJoinWorkerThreadFactory threadFactory = ForkJoinPool.defaultForkJoinWorkerThreadFactory;

	// 未捕获的异常处理器
	private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

	// 是否使用异步模式
	private boolean asyncMode = false;


	// [背后实现] "Fork-Join"任务池
	private ForkJoinPool forkJoinPool;


	/**
	 * 指定并发级别，默认是可用的处理器数量。
	 * 
	 * <p>Specify the parallelism level. Default is {@link Runtime#availableProcessors()}.
	 */
	public void setParallelism(int parallelism) {
		this.parallelism = parallelism;
	}

	/**
	 * 设置创建新的"Fork-Join"工作者线程的工厂。
	 * 默认与"Fork-Join"任务池(ForkJoinPool)使用一样的工厂
	 * 
	 * <p>Set the factory for creating new ForkJoinWorkerThreads.
	 * Default is {@link ForkJoinPool#defaultForkJoinWorkerThreadFactory}.
	 */
	public void setThreadFactory(ForkJoinPool.ForkJoinWorkerThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
	}

	/**
	 * 设置内部工作者线程由于在执行任务时遇到不可恢复的错误而终止的处理器。
	 * 默认是空
	 * 
	 * <p>Set the handler for internal worker threads that terminate due to unrecoverable errors
	 * encountered while executing tasks. Default is none.
	 */
	public void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
		this.uncaughtExceptionHandler = uncaughtExceptionHandler;
	}

	/**
	 * 指定是否为分开的任务建立一个本地的FIFO调度模式。
	 * 该模式设置为{@code true}可能更合适，这样工作者线程只会处理"事件方式"的异步任务。
	 * 
	 * <p>Specify whether to establish a local first-in-first-out scheduling mode for forked tasks
	 * that are never joined. This mode (asyncMode = {@code true}) may be more appropriate
	 * than the default locally stack-based mode in applications in which worker threads only
	 * process event-style asynchronous tasks. Default is {@code false}.
	 */
	public void setAsyncMode(boolean asyncMode) {
		this.asyncMode = asyncMode;
	}

	// # 初始化Bean(InitializingBean)
	/**
	 * 初始化"Fork-Join"任务池，保证在Spring IoC容器管理下仅有一个实例对象(单实例)。
	 */
	@Override
	public void afterPropertiesSet() {
		// 保证在Spring IoC容器管理下，只会被调用一次，即仅有一个实例对象(单实例)
		this.forkJoinPool =
				new ForkJoinPool(this.parallelism, this.threadFactory, this.uncaughtExceptionHandler, this.asyncMode);
	}


	// # 工厂Bean(FactoryBean)
	@Override
	public ForkJoinPool getObject() {
		return this.forkJoinPool;
	}

	@Override
	public Class<?> getObjectType() {
		return ForkJoinPool.class;
	}

	@Override
	public boolean isSingleton() {
		return true; // 单实例
	}


	// # 销毁Bean(DisposableBean)
	/**
	 * 关闭"Fork-Join"任务池。
	 */
	@Override
	public void destroy() {
		this.forkJoinPool.shutdown();
	}

}
