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

package org.springframework.core.task;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 异步任务执行器实现的扩展接口(继承自TaskExecutor)，提供一个重载的{@link #execute(Runnable, long)}变体方法。
 * 该方法使用一个启动超时参数来更好地支持携带结果的任务(Callable)。
 * 
 * <p><b>注意：</b>执行器辅助类(Executors)包含一组方法，其可以转换某些常见的类似于闭包的对象。
 * 例如，在任务执行之前将特权行动(PrivilegedAction)转换为{@link Callable}。
 * 
 * <p>实现本接口也预示着{@link #execute(Runnable)}方法不会在调用者线程中执行其Runnable，
 * 而是在其他线程中异步执行。
 * 
 * 
 * <p>Extended interface for asynchronous {@link TaskExecutor} implementations,
 * offering an overloaded {@link #execute(Runnable, long)} variant with a start
 * timeout parameter as well support for {@link java.util.concurrent.Callable}.
 *
 * <p>Note: The {@link java.util.concurrent.Executors} class includes a set of
 * methods that can convert some other common closure-like objects, for example,
 * {@link java.security.PrivilegedAction} to {@link Callable} before executing them.
 *
 * <p>Implementing this interface also indicates that the {@link #execute(Runnable)}
 * method will not execute its Runnable in the caller's thread but rather
 * asynchronously in some other thread.
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see SimpleAsyncTaskExecutor
 * @see org.springframework.scheduling.SchedulingTaskExecutor
 * @see java.util.concurrent.Callable
 * @see java.util.concurrent.Executors
 */
public interface AsyncTaskExecutor extends TaskExecutor {

	/** Constant that indicates immediate execution (表示立即执行) */
	long TIMEOUT_IMMEDIATE = 0;

	/** Constant that indicates no time limit (表示没有时间限制) */
	long TIMEOUT_INDEFINITE = Long.MAX_VALUE;


	/**
	 * 执行一个给定的<b>任务({@code task})</b>。
	 * 
	 * <p>Execute the given {@code task}.
	 * 
	 * @param task the {@code Runnable} to execute (never {@code null}, 不能为null)
	 * @param startTimeout the time duration (milliseconds) within which the task is
	 * supposed to start. This is intended as a hint to the executor, allowing for
	 * preferred handling of immediate tasks. Typical values are {@link #TIMEOUT_IMMEDIATE}
	 * or {@link #TIMEOUT_INDEFINITE} (the default as used by {@link #execute(Runnable)}).
	 * 从任务开始执行到现在的持续时间(毫秒)，这意味着执行器的一个提示，允许即时任务被优先处理。
	 * {@link #execute(Runnable)}默认使用没有时间限制。
	 * @throws TaskTimeoutException in case of the task being rejected because
	 * of the timeout (i.e. it cannot be started in time) (任务由于超时被拒绝)
	 * @throws TaskRejectedException if the given task was not accepted (给定的任务不被接受)
	 */
	void execute(Runnable task, long startTimeout);

	/**
	 * 提交一个可运行(Runnable)的任务，为了执行和接收一个表示该任务异步计算的结果(Future)。
	 * Future在完成后，会返回一个{@code null}的结果。
	 * 
	 * <p>Submit a Runnable task for execution, receiving a Future representing that task.
	 * The Future will return a {@code null} result upon completion.
	 * 
	 * @param task the {@code Runnable} to execute (never {@code null}) (待执行的可运行任务)
	 * @return a Future representing pending completion of the task (表示等待任务完成的一个Future)
	 * @throws TaskRejectedException if the given task was not accepted (给定的任务不被接受)
	 * @since 3.0
	 */
	Future<?> submit(Runnable task);

	/**
	 * 提交一个可调用(Callable)的任务，为了执行和接收一个表示该任务异步计算的结果(Future)。
	 * Future在完成后，会返回Callable的结果。
	 * 
	 * <p>Submit a Callable task for execution, receiving a Future representing that task.
	 * The Future will return the Callable's result upon completion.
	 * 
	 * @param task the {@code Callable} to execute (never {@code null}) (待执行的可调用任务)
	 * @return a Future representing pending completion of the task (表示等待任务完成的一个Future)
	 * @throws TaskRejectedException if the given task was not accepted (给定的任务不被接受)
	 * @since 3.0
	 */
	<T> Future<T> submit(Callable<T> task);

}
