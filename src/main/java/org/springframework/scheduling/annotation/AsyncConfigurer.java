/*
 * Copyright 2002-2011 the original author or authors.
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

import java.util.concurrent.Executor;

/**
 * 用于实现配置类(@{@link Configuration})和@{@link EnableAsync}注解的接口，
 * 希望在处理异步方法调用时使用自定义的执行器({@link Executor})实例。
 * 
 * <p>见@{@link EnableAsync}的使用示例。
 * 
 * <p>Interface to be implemented by @{@link org.springframework.context.annotation.Configuration
 * Configuration} classes annotated with @{@link EnableAsync} that wish to customize the
 * {@link Executor} instance used when processing async method invocations.
 *
 * <p>See @{@link EnableAsync} for usage examples.
 *
 * @author Chris Beams
 * @since 3.1
 * @see AbstractAsyncConfiguration
 * @see EnableAsync
 */
public interface AsyncConfigurer {

	/**
	 * 在处理异步方法调用时使用的执行器({@link Executor})实例。
	 * 
	 * <p>The {@link Executor} instance to be used when processing async
	 * method invocations.
	 */
	Executor getAsyncExecutor();

}
