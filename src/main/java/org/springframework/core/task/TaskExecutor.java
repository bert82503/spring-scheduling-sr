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

package org.springframework.core.task;

import java.util.concurrent.Executor;

/**
 * 简单的任务执行器接口，其抽象一个{@link Runnable}的执行。
 * 
 * <p>实现类可以使用各种不同的执行策略，如 同步、异步、使用线程池 等。
 * 
 * <p><font color="red">
 * 等价于JDK 1.5的{@link Executor}接口。</font>
 * 在Spring 3.0中扩展它，(原因)便于用户可以声明一个Executor的依赖，并接收任何TaskExecutor实现。<font color="red">
 * 本接口仍然独立于标准的Executor接口，(原因)主要是为了向后兼容使用JDK 1.4的Spring 2.x。</font>
 * 
 * 
 * <p>Simple task executor interface that abstracts the execution
 * of a {@link Runnable}.
 *
 * <p>Implementations can use all sorts of different execution strategies,
 * such as: synchronous, asynchronous, using a thread pool, and more.
 *
 * <p>Equivalent to JDK 1.5's {@link Executor} interface;
 * extending it now in Spring 3.0, so that clients may declare
 * a dependency on an Executor and receive any TaskExecutor implementation.
 * This interface remains separate from the standard Executor interface
 * mainly for backwards compatibility with JDK 1.4 in Spring 2.x.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see java.util.concurrent.Executor
 */
public interface TaskExecutor extends Executor {

	/**
	 * 执行一个给定的<b>任务({@code task})</b>。
	 * 
	 * <p>如果实现类<font color="red">使用<b>异步执行</b>策略，调用会立即返回</font>；
	 * 如果实现类<font color="red">使用<b>同步执行</b>策略，调用会阻塞</font>。
	 * 
	 * 
	 * <p>Execute the given {@code task}.
	 * 
	 * <p>The call might return immediately if the implementation uses
	 * an asynchronous execution strategy, or might block in the case
	 * of synchronous execution.
	 * 
	 * @param task the {@code Runnable} to execute (never {@code null}, 不能为null)
	 * @throws TaskRejectedException if the given task was not accepted (如果给定的任务不被接受)
	 */
	void execute(Runnable task);

}
