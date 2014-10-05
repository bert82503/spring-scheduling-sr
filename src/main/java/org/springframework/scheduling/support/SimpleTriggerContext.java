/*
 * Copyright 2002-2009 the original author or authors.
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

import org.springframework.scheduling.TriggerContext;

/**
 * 实现TriggerContext接口的简单的数据持有者。
 * 
 * <p>Simple data holder implementation of the {@link TriggerContext} interface.
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
public class SimpleTriggerContext implements TriggerContext {

	/*
	 * 使用volatile类型来确保应用中在多线程之间读写的可视性
	 */
	// 最后一次调度执行时间
	private volatile Date lastScheduledExecutionTime;

	// 最后一次实际执行时间
	private volatile Date lastActualExecutionTime;

	// 最后一次完成执行时间
	private volatile Date lastCompletionTime;


	/**
	 * 更新该上下文持有者的最新执行时间值的状态。
	 * 
	 * <p>Update this holder's state with the latest time values.
	 * 
 	 * @param lastScheduledExecutionTime last <i>scheduled</i> execution time
	 * @param lastActualExecutionTime last <i>actual</i> execution time
	 * @param lastCompletionTime last completion time
	 */
	public void update(Date lastScheduledExecutionTime, Date lastActualExecutionTime, Date lastCompletionTime) {
		this.lastScheduledExecutionTime = lastScheduledExecutionTime;
		this.lastActualExecutionTime = lastActualExecutionTime;
		this.lastCompletionTime = lastCompletionTime;
	}


	// TriggerContext
	@Override
	public Date lastScheduledExecutionTime() {
		return this.lastScheduledExecutionTime;
	}

	@Override
	public Date lastActualExecutionTime() {
		return this.lastActualExecutionTime;
	}

	@Override
	public Date lastCompletionTime() {
		return this.lastCompletionTime;
	}

}
