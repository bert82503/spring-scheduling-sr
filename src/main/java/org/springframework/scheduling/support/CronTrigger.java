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

package org.springframework.scheduling.support;

import java.util.Date;
import java.util.TimeZone;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

/**
 * cron表达式的定时触发器({@link Trigger})实现，包装一个Crontab模式的时间序列生成器({@link CronSequenceGenerator})。
 * 
 * <p>{@link Trigger} implementation for cron expressions.
 * Wraps a {@link CronSequenceGenerator}.
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see CronSequenceGenerator
 */
public class CronTrigger implements Trigger {

	// 一个"Crontab模式"的时间序列生成器
	private final CronSequenceGenerator sequenceGenerator;


	/**
	 * Build a {@link CronTrigger} from the pattern provided in the default time zone.
	 * @param cronExpression a space-separated list of time fields,
	 * following cron expression conventions
	 */
	public CronTrigger(String cronExpression) {
		this.sequenceGenerator = new CronSequenceGenerator(cronExpression);
	}

	/**
	 * 使用指定的"cron表达式"和时区来创建一个"定时触发器"对象。
	 * 
	 * <p>Build a {@link CronTrigger} from the pattern provided.
	 * 
	 * @param cronExpression a space-separated list of time fields,
	 * following cron expression conventions
	 * @param timeZone a time zone in which the trigger times will be generated
	 */
	public CronTrigger(String cronExpression, TimeZone timeZone) {
		this.sequenceGenerator = new CronSequenceGenerator(cronExpression, timeZone);
	}


	// 覆盖"触发器"(Trigger)
	/**
	 * 根据给定的触发器上下文来确定"任务的下一次执行时间"。
	 * 
	 * <p>任务的"下一次执行时间"是基于"上一次完成执行时间"计算出来的。
	 * 
	 * <p>Determine the next execution time according to the given trigger context.
	 * 
	 * <p>Next execution times are calculated based on the
	 * {@linkplain TriggerContext#lastCompletionTime completion time} of the
	 * previous execution; therefore, overlapping executions won't occur.
	 */
	@Override
	public Date nextExecutionTime(TriggerContext triggerContext) {
		Date date = triggerContext.lastCompletionTime(); // 最后一次完成执行时间
		if (date != null) {
			Date scheduled = triggerContext.lastScheduledExecutionTime(); // 最后一次调度执行时间
			if (scheduled != null && date.before(scheduled)) {
				// Previous task apparently executed too early...
				// Let's simply use the last calculated execution time then,
				// in order to prevent accidental re-fires in the same second.
				date = scheduled;
			}
		}
		else {
			date = new Date();
		}
		return this.sequenceGenerator.next(date);
	}


	public String getExpression() {
		return this.sequenceGenerator.getExpression();
	}


	// 覆盖对象的方法(Object)
	@Override
	public boolean equals(Object obj) {
		return (this == obj || (obj instanceof CronTrigger &&
				this.sequenceGenerator.equals(((CronTrigger) obj).sequenceGenerator)));
	}

	@Override
	public int hashCode() {
		return this.sequenceGenerator.hashCode();
	}

	@Override
	public String toString() {
		return this.sequenceGenerator.toString();
	}

}
