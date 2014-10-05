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

package org.springframework.scheduling;

import java.util.Date;

/**
 * 封装给定任务的上次执行时间和最后完成时间的上下文对象。
 * 
 * <p>Context object encapsulating last execution times and last completion time
 * of a given task.
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
public interface TriggerContext {

	/**
	 * 返回任务的最后一次调度执行时间。
	 * 如果任务之前未被调度过，则返回{@code null}。
	 * 
	 * <p>Return the last <i>scheduled</i> execution time of the task,
	 * or {@code null} if not scheduled before.
	 */
	Date lastScheduledExecutionTime();

	/**
	 * 返回任务的最后一次实际执行时间。
	 * 如果任务之前未被调度过，则返回{@code null}。
	 * 
	 * <p>Return the last <i>actual</i> execution time of the task,
	 * or {@code null} if not scheduled before.
	 */
	Date lastActualExecutionTime();

	/**
	 * 返回任务的最后一次完成执行时间。
	 * 如果任务之前未被调度过，则返回{@code null}。
	 * 
	 * <p>Return the last completion time of the task,
	 * or {@code null} if not scheduled before.
	 */
	Date lastCompletionTime();

}
