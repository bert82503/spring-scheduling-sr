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

import org.springframework.core.task.AsyncTaskExecutor;

/**
 * 任务执行器的扩展接口，暴露与潜在任务提交者相关的调度特性。
 * 
 * <p>鼓励调度客户端提交与暴露的{@code TaskExecutor}接口实现引用相匹配的可运行任务。
 * 
 * 
 * <p>A {@link org.springframework.core.task.TaskExecutor} extension exposing
 * scheduling characteristics that are relevant to potential task submitters.
 *
 * <p>Scheduling clients are encouraged to submit
 * {@link Runnable Runnables} that match the exposed preferences
 * of the {@code TaskExecutor} implementation in use.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see SchedulingAwareRunnable
 * @see org.springframework.core.task.TaskExecutor
 * @see org.springframework.scheduling.commonj.WorkManagerTaskExecutor
 */
public interface SchedulingTaskExecutor extends AsyncTaskExecutor {

	/**
	 * 本任务执行器是否喜欢短周期(short-lived)的任务胜过长周期(long-lived)的任务？
	 * 
	 * <p>一个调度任务执行器实现可以表明它是否喜欢所提交的任务，
	 * 这些任务能在单个任务执行中尽可能做少量的工作。
	 * 例如，提交的任务可能会破坏一个重复的循环之后进入后续的独立子任务。
	 * 
	 * <p>这应该算是一个提示。当然，任务执行器(TaskExecutor)接口客户端可以自由地忽略此标记，
	 * 但调度任务执行器(SchedulingTaskExecutor)接口需要关注此标记。
	 * 然而，线程池通常倾向于短周期的任务，以便能够进行更精细地调度。
	 * 
	 * <p>Does this {@code TaskExecutor} prefer short-lived tasks over
	 * long-lived tasks?
	 * 
	 * <p>A {@code SchedulingTaskExecutor} implementation can indicate
	 * whether it prefers submitted tasks to perform as little work as they
	 * can within a single task execution. For example, submitted tasks
	 * might break a repeated loop into individual subtasks which submit a
	 * follow-up task afterwards (if feasible).
	 * 
	 * <p>This should be considered a hint. Of course {@code TaskExecutor}
	 * clients are free to ignore this flag and hence the
	 * {@code SchedulingTaskExecutor} interface overall. However, thread
	 * pools will usually indicated a preference for short-lived tasks, to be
	 * able to perform more fine-grained scheduling.
	 * 
	 * @return {@code true} if this {@code TaskExecutor} prefers
	 * short-lived tasks (如果任务执行器喜欢短周期的任务，则返回{@code true}。)
	 */
	boolean prefersShortLivedTasks();

}
