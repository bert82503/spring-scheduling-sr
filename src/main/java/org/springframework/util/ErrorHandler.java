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

package org.springframework.util;

/**
 * 一个处理错误的策略。
 * 当由任务调度器(TaskScheduler)提交的任务在异步执行发生错误时，由它处理这些错误就特别有用。
 * 在这样情况下，抛出错误给初始调用者是不大可能的。
 * 
 * <p>A strategy for handling errors. This is especially useful for handling
 * errors that occur during asynchronous execution of tasks that have been
 * submitted to a TaskScheduler. In such cases, it may not be possible to
 * throw the error to the original caller.
 *
 * @author Mark Fisher
 * @since 3.0
 */
public interface ErrorHandler {

	/**
	 * 处理给定的错误，可能将它作为"一个致命的异常"重新抛出。
	 * 
	 * <p>Handle the given error, possibly rethrowing it as a fatal exception.
	 */
	void handleError(Throwable t);

}
