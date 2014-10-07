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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个方法作为异步执行候选者的注解。
 * 如果在类型级别上使用，则类里面的所有方法都被认为是异步的。
 * 
 * <p>在目标方法签名方面，任何参数类型都支持。
 * 但是，返回类型必须是{@code void}或{@link Future}。
 * 对于后者，从代理返回的{@code Future}处理将是一个真实的异步{@code Future}，其可用于追踪异步方法执行的结果。
 * 然而，因为目标方法需要实现相同的签名，它必须返回一个临时的{@code Future}处理，
 * 通过异步执行结果({@link AsyncResult})来传递返回值。
 * 
 * <p>Annotation that marks a method as a candidate for <i>asynchronous</i> execution.
 * Can also be used at the type level, in which case all of the type's methods are
 * considered as asynchronous.
 *
 * <p>In terms of target method signatures, any parameter types are supported.
 * However, the return type is constrained to either {@code void} or
 * {@link java.util.concurrent.Future}. In the latter case, the {@code Future} handle
 * returned from the proxy will be an actual asynchronous {@code Future} that can be used
 * to track the result of the asynchronous method execution. However, since the
 * target method needs to implement the same signature, it will have to return
 * a temporary {@code Future} handle that just passes the return value through: e.g.
 * Spring's {@link AsyncResult} or EJB 3.1's {@link javax.ejb.AsyncResult}.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 3.0
 * @see AnnotationAsyncExecutionInterceptor (注解的异步方法执行拦截器)
 * @see AsyncAnnotationAdvisor (异步方法执行注解通知器)
 */
@Target({ElementType.METHOD, ElementType.TYPE}) // 方法、类型
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Async {

	/**
	 * 指定的异步操作的限定符值。
	 * 
	 * <p>可用来确定在执行本方法时使用的目标执行器，
	 * 匹配一个特殊的执行器({@link Executor})或任务执行器({@link TaskExecutor})的bean定义的限定符值(bean名称)。
	 * 
	 * <p>当在类级别上指定{@code @Async}注解时，表示给定的执行器应该用于该类中的所有方法。
	 * 方法级别上使用{@link Async#value}总会覆盖类级别上设定的任何值。
	 * 
	 * <p>A qualifier value for the specified asynchronous operation(s).
	 * 
	 * <p>May be used to determine the target executor to be used when executing this
	 * method, matching the qualifier value (or the bean name) of a specific
	 * {@link java.util.concurrent.Executor Executor} or
	 * {@link org.springframework.core.task.TaskExecutor TaskExecutor}
	 * bean definition.
	 * 
	 * <p>When specified on a class level {@code @Async} annotation, indicates that the
	 * given executor should be used for all methods within the class. Method level use
	 * of {@link Async#value} always overrides any value set at the class level.
	 * @since 3.1.2
	 */
	String value() default "";

}
