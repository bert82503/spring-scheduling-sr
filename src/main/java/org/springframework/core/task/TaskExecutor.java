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
 * 
 * <p><font color="red">
 * 等价于JDK 1.5的{@link Executor}接口。</font>
 * 在Spring 3.0中扩展它，便于用户可以声明一个Executor的依赖，并接受任何的TaskExecutor实现。<font color="red">
 * 本接口仍然独立于标准的Executor接口，主要是为了向后兼容使用JDK 1.4的Spring 2.x。</font>
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
	 * Execute the given {@code task}.
	 * <p>The call might return immediately if the implementation uses
	 * an asynchronous execution strategy, or might block in the case
	 * of synchronous execution.
	 * @param task the {@code Runnable} to execute (never {@code null})
	 * @throws TaskRejectedException if the given task was not accepted
	 */
	void execute(Runnable task);

}
