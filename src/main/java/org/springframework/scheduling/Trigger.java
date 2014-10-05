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
 * 触发器对象的公共接口，用于确定与其相关的一个任务的下一次执行时间。
 * 
 * <p>Common interface for trigger objects that determine the next execution time
 * of a task that they get associated with.
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see TaskScheduler#schedule(Runnable, Trigger)
 * @see org.springframework.scheduling.support.CronTrigger
 */
public interface Trigger {

	/**
	 * 根据给定的触发器上下文来确定任务的下一次执行时间。
	 * 
	 * <p>Determine the next execution time according to the given trigger context.
	 * 
	 * @param triggerContext context object encapsulating last execution times
	 * and last completion time (封装任务的上次执行时间和最后完成时间的上下文对象)
	 * @return the next execution time as defined by the trigger,
	 * or {@code null} if the trigger won't fire anymore
	 */
	Date nextExecutionTime(TriggerContext triggerContext);

}
