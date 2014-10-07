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

package org.springframework.scheduling.annotation;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 一个传递异步方法执行结果的Future操作，可用于使用一个Future返回类型声明的异步执行方法签名。
 * 
 * <p>实现{@link Future<V>}接口
 * 
 * <p>A pass-through {@code Future} handle that can be used for method signatures
 * which are declared with a Future return type for asynchronous execution.
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see Async
 */
public class AsyncResult<V> implements Future<V> {

	// 异步方法执行的最终结果
	private final V value;


	/**
	 * Create a new AsyncResult holder.
	 * @param value the value to pass through (待传递的返回结果)
	 */
	public AsyncResult(V value) {
		this.value = value;
	}


	// Future implementation
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public V get() {
		return this.value;
	}

	@Override
	public V get(long timeout, TimeUnit unit) {
		return this.value;
	}

}
