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

package org.springframework.scheduling.support;

import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.ErrorHandler;
import org.springframework.util.ReflectionUtils;

/**
 * 装饰"任务错误处理"的工具方法。
 * 
 * <p><b>注意：</b>本类仅供"Spring调度器实现类"内部使用。
 * 
 * <p>Utility methods for decorating tasks with error handling.
 *
 * <p><b>NOTE:</b> This class is intended for internal use by Spring's scheduler
 * implementations. It is only public so that it may be accessed from
 * implementations within other packages. It is <i>not</i> intended for general
 * use and may change in the future.
 *
 * @author Mark Fisher
 * @since 3.0
 */
public abstract class TaskUtils {

	/**
	 * "记录异常信息"的错误处理策略。
	 * 任务会接着继续执行
	 * 
	 * <p>An ErrorHandler strategy that will log the Exception but perform
	 * no further handling. This will suppress the error so that
	 * subsequent executions of the task will not be prevented.
	 */
	public static final ErrorHandler LOG_AND_SUPPRESS_ERROR_HANDLER = new LoggingErrorHandler();

	/**
	 * "记录错误级别信息，然后重新抛出异常"的错误处理策略。
	 * 阻止任务继续执行
	 * 
	 * <p>An ErrorHandler strategy that will log at error level and then
	 * re-throw the Exception. Note: this will typically prevent subsequent
	 * execution of a scheduled task.
	 */
	public static final ErrorHandler LOG_AND_PROPAGATE_ERROR_HANDLER = new PropagatingErrorHandler();


	/**
	 * <p>装饰能"处理错误"的任务。
	 * 如果给定的错误执行器不是null，则它会被使用。
	 * 
	 * Decorates the task for error handling. If the provided
	 * {@link ErrorHandler} is not null, it will be used. Otherwise,
	 * repeating tasks will have errors suppressed by default whereas
	 * one-shot tasks will have errors propagated by default since those
	 * errors may be expected through the returned {@link Future}. In both
	 * cases, the errors will be logged.
	 */
	public static DelegatingErrorHandlingRunnable decorateTaskWithErrorHandler(
			Runnable task, ErrorHandler errorHandler, boolean isRepeatingTask) {

		if (task instanceof DelegatingErrorHandlingRunnable) {
			return (DelegatingErrorHandlingRunnable) task;
		}
		ErrorHandler eh = errorHandler != null ? errorHandler : getDefaultErrorHandler(isRepeatingTask);
		return new DelegatingErrorHandlingRunnable(task, eh);
	}

	/**
	 * 基于"是否重复任务"来返回默认的"错误处理器({@link ErrorHandler})"实现。
	 * 
	 * <p>Return the default {@link ErrorHandler} implementation based on the boolean
	 * value indicating whether the task will be repeating or not. For repeating tasks
	 * it will suppress errors, but for one-time tasks it will propagate. In both
	 * cases, the error will be logged.
	 */
	public static ErrorHandler getDefaultErrorHandler(boolean isRepeatingTask) {
		return (isRepeatingTask ? LOG_AND_SUPPRESS_ERROR_HANDLER : LOG_AND_PROPAGATE_ERROR_HANDLER);
	}


	/**
	 * "在错误级别记录异常信息"的错误处理器实现。
	 * 当错误是预期的行为时，会有用。(任务会接着继续执行)
	 * 
	 * <p>An {@link ErrorHandler} implementation that logs the Throwable at error
 	 * level. It does not perform any additional error handling. This can be
 	 * useful when suppression of errors is the intended behavior.
	 */
	static class LoggingErrorHandler implements ErrorHandler {

		private final Log logger = LogFactory.getLog(LoggingErrorHandler.class);

		public void handleError(Throwable t) {
			if (logger.isErrorEnabled()) {
				logger.error("Unexpected error occurred in scheduled task.", t);
			}
		}
	}


	/**
	 * "在错误级别记录异常信息，然后重新抛出该异常"的错误处理器实现。
	 * 阻止任务继续执行
	 * 
	 * <p>An {@link ErrorHandler} implementation that logs the Throwable at error
	 * level and then propagates it.
	 */
	static class PropagatingErrorHandler extends LoggingErrorHandler {

		public void handleError(Throwable t) {
			super.handleError(t);
			ReflectionUtils.rethrowRuntimeException(t);
		}
	}

}
