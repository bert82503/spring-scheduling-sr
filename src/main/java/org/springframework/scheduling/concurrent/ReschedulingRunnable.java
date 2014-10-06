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

import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.DelegatingErrorHandlingRunnable;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.util.ErrorHandler;

/**
 * 一个根据给定的触发器({@link Trigger})建议的下一次执行时间
 * 来重复调度背后的可运行任务的内部适配器。
 * 
 * <p>这是必需的，因为一个原生的"调度的执行器服务({@link ScheduledExecutorService})"
 * 只支持"时延驱动"执行。触发器接口({@link Trigger})的灵活性是它能重复确定
 * 下一次执行时间的时延。(整合ScheduledExecutorService和Trigger功能)
 * 
 * <p>继承自"能捕获任何异常或错误的Runnable包装器(DelegatingErrorHandlingRunnable)"，
 * 并实现"可时延调度的Future(ScheduledFuture<Object>)"接口。
 * 
 * <p>Internal adapter that reschedules an underlying {@link Runnable} according
 * to the next execution time suggested by a given {@link Trigger}.
 *
 * <p>Necessary because a native {@link ScheduledExecutorService} supports
 * delay-driven execution only. The flexibility of the {@link Trigger} interface
 * will be translated onto a delay for the next execution time (repeatedly).
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 3.0
 */
class ReschedulingRunnable extends DelegatingErrorHandlingRunnable implements ScheduledFuture<Object> {

	// [core] 触发器(确定任务的下一次执行时间)
	private final Trigger trigger;

	// 触发器上下文
	private final SimpleTriggerContext triggerContext = new SimpleTriggerContext();

	// 触发器上下文的监视对象
	private final Object triggerContextMonitor = new Object();

	// 下一次调度的执行时间
	private Date scheduledExecutionTime;


	// [core] 调度的执行器服务(JDK)
	private final ScheduledExecutorService executor;

	// 可时延调度的Future
	private ScheduledFuture<Object> currentFuture;


	/**
	 * 实例化一个可重复调度的可运行任务。
	 * 
	 * @param delegate
	 * @param trigger
	 * @param executor
	 * @param errorHandler
	 */
	public ReschedulingRunnable(Runnable delegate, Trigger trigger,
			ScheduledExecutorService executor, ErrorHandler errorHandler) {
		// 实例化一个"能捕获任何异常或错误的Runnable包装器"
		super(delegate, errorHandler);
		
		this.trigger = trigger;
		this.executor = executor;
	}


	/**
	 * 调度任务。
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ScheduledFuture<Object> schedule() {
		synchronized (this.triggerContextMonitor) { // 加锁同步
			// 确定任务的下一次执行时间
			this.scheduledExecutionTime = this.trigger.nextExecutionTime(this.triggerContext);
			if (this.scheduledExecutionTime == null) {
				return null;
			}
			// 第一次调度的时延
			long initialDelay = this.scheduledExecutionTime.getTime() - System.currentTimeMillis();
			this.currentFuture = (ScheduledFuture<Object>) 
					this.executor.schedule(this, initialDelay, TimeUnit.MILLISECONDS);
			return this;
		}
	}

	// DelegatingErrorHandlingRunnable
	/*
	 * 使用"加锁机制"更新"触发器上下文"信息，并在"可时延调度的Future"未取消之前调度它。
	 */
	@Override
	public void run() {
		// 实际的执行时间
		Date actualExecutionTime = new Date();
		super.run();
		// 完成的执行时间
		Date completionTime = new Date();
		
		synchronized (this.triggerContextMonitor) {
			// 更新"触发器上下文"信息
			this.triggerContext.update(this.scheduledExecutionTime, actualExecutionTime, completionTime);
			if (!this.currentFuture.isCancelled()) {
				schedule();
			}
		}
	}


	// Future implementation (全部委托给currentFuture属性处理)
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		synchronized (this.triggerContextMonitor) { // 加锁同步
			return this.currentFuture.cancel(mayInterruptIfRunning);
		}
	}

	@Override
	public boolean isCancelled() {
		synchronized (this.triggerContextMonitor) {
			return this.currentFuture.isCancelled();
		}
	}

	@Override
	public boolean isDone() {
		synchronized (this.triggerContextMonitor) {
			return this.currentFuture.isDone();
		}
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		ScheduledFuture<Object> curr;
		synchronized (this.triggerContextMonitor) {
			curr = this.currentFuture;
		}
		return curr.get();
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		ScheduledFuture<Object> curr;
		synchronized (this.triggerContextMonitor) {
			curr = this.currentFuture;
		}
		return curr.get(timeout, unit);
	}


	// Delayed implementation
	@Override
	public long getDelay(TimeUnit unit) {
		ScheduledFuture<Object> curr;
		synchronized (this.triggerContextMonitor) {
			curr = this.currentFuture;
		}
		return curr.getDelay(unit);
	}

	@Override
	public int compareTo(Delayed other) {
		if (this == other) {
			return 0;
		}
		long diff = getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS);
		return (diff == 0 ? 0 : ((diff < 0) ? -1 : 1));
	}

}
