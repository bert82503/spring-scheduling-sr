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

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

/**
 * 启用Spring的异步方法执行功能(开关)，类似于{@code <task:*>} XML命名空间的功能。
 * 在@{@link Configuration}类上使用如下：
 * 
 * <p>{@link #mode()}属性用来控制如何应用通知的代理行为，默认使用{@link AdviceMode#PROXY}，
 * 其它属性控制代理行为。
 *
 * <p><b>注意：</b>如果{@linkplain #mode}属性被设置为{@link AdviceMode#ASPECTJ}，
 * 那么{@link #proxyTargetClass()}属性是必须设置的。
 * 同时，要注意{@code spring-aspects}模块的JAR包必须要在类路径下。
 *
 * <p><font color="red">默认情况下，{@link SimpleAsyncTaskExecutor}会被用于处理异步方法调用。</font>
 * 为了自定义此行为，需实现{@link AsyncConfigurer}接口并通过{@link AsyncConfigurer#getAsyncExecutor() getExecutor()}方法
 * 提供自己的执行器({@link Executor})。
 *
 * <p>示例演示了"基于代码的方法"如何允许通过直接访问实际的组件来实现最大化的可配置性。
 * 
 * <p><b>注意：</b>在上述示例中，{@code ThreadPoolTaskExecutor}并非是一个全由Spring管理的Bean，
 * 如果你希望它是一个完全由Spring管理的bean，请在{@code getAsyncExecutor()}方法上增加{@code @Bean}注解。
 * 在这种情况下，不再需要手动调用{@code executor.initialize()}方法，因为该方法在bean初始化时会被自动调用。
 *
 * 
 * <p>Enables Spring's asynchronous method execution capability, similar to functionality
 * found in Spring's {@code <task:*>} XML namespace. To be used on @{@link Configuration}
 * classes as follows:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableAsync
 * public class AppConfig {
 *     &#064;Bean
 *     public MyAsyncBean asyncBean() {
 *         return new MyAsyncBean();
 *     }
 * }</pre>
 *
 * where {@code MyAsyncBean} is a user-defined type with one or methods annotated
 * with @{@link Async} (or any custom annotation specified by the {@link #annotation()}
 * attribute).
 * 
 * <p>The {@link #mode()} attribute controls how advice is applied; if the mode is
 * {@link AdviceMode#PROXY} (the default), then the other attributes control the behavior
 * of the proxying.
 * 
 * <p>Note that if the {@linkplain #mode} is set to {@link AdviceMode#ASPECTJ}, then
 * the {@link #proxyTargetClass()} attribute is obsolete. Note also that in this case the
 * {@code spring-aspects} module JAR must be present on the classpath.
 * 
 * <p>By default, a {@link org.springframework.core.task.SimpleAsyncTaskExecutor
 * SimpleAsyncTaskExecutor} will be used to process async method invocations. To
 * customize this behavior, implement {@link AsyncConfigurer} and
 * provide your own {@link java.util.concurrent.Executor Executor} through the
 * {@link AsyncConfigurer#getAsyncExecutor() getExecutor()} method.
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableAsync
 * public class AppConfig implements AsyncConfigurer {
 *
 *     &#064;Bean
 *     public MyAsyncBean asyncBean() {
 *         return new MyAsyncBean();
 *     }
 *
 *     &#064;Override
 *     public Executor getAsyncExecutor() {
 *         ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
 *         executor.setCorePoolSize(7);
 *         executor.setMaxPoolSize(42);
 *         executor.setQueueCapacity(11);
 *         executor.setThreadNamePrefix("MyExecutor-");
 *         executor.initialize();
 *         return executor;
 *     }
 * }</pre>
 *
 * <p>For reference, the example above can be compared to the following Spring XML
 * configuration:
 * <pre class="code">
 * {@code
 * <beans>
 *     <task:annotation-driven executor="myExecutor"/>
 *     <task:executor id="myExecutor" pool-size="7-42" queue-capacity="11"/>
 *     <bean id="asyncBean" class="com.foo.MyAsyncBean"/>
 * </beans>
 * }</pre>
 * the examples are equivalent save the setting of the <em>thread name prefix</em> of the
 * Executor; this is because the the {@code task:} namespace {@code executor} element does
 * not expose such an attribute. This demonstrates how the code-based approach allows for
 * maximum configurability through direct access to actual componentry.
 * 
 * <p>Note: In the above example the {@code ThreadPoolTaskExecutor} is not a fully managed
 * Spring Bean. Add the {@code @Bean} annotation to the {@code getAsyncExecutor()} method
 * if you want a fully managed bean. In such circumstances it is no longer necessary to
 * manually call the {@code executor.initialize()} method as this will be invoked
 * automatically when the bean is initialized.
 *
 * @author Chris Beams
 * @since 3.1
 * @see Async
 * @see AsyncConfigurer
 * @see AsyncConfigurationSelector
 */
@Target(ElementType.TYPE) // 只能标注"类型"
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AsyncConfigurationSelector.class) // 导入"异步代理行为配置选择器"
public @interface EnableAsync {

	/**
	 * 表示在任何类型或方法级别上检测到的"异步"注解类型。
	 * 默认情况下，@{@link Async}和EJB 3.1的@{@code javax.ejb.Asynchronous}会被检测到。
	 * 
	 * <p>本设置属性的存在使得开发人员可以提供自己的注解类型来表明一个方法应该被异步调用。
	 * 
	 * <p>Indicate the 'async' annotation type to be detected at either class
	 * or method level. By default, both the {@link Async} annotation and
	 * the EJB 3.1 {@code javax.ejb.Asynchronous} annotation will be
	 * detected. 
	 * 
	 * <p>This setter property exists so that developers can provide
	 * their own (non-Spring-specific) annotation type to indicate that a method
	 * (or all methods of a given class) should be invoked asynchronously.
	 */
	Class<? extends Annotation> annotation() default Annotation.class;

	/**
	 * 表示基于子类的(CGLIB)代理是否被创建，而不是标准的基于接口的Java代理。
	 * 
	 * <p><font color="red">注意：设置该属性为{@code true}会影响所有Spring管理的需要代理的beans，不仅仅是被标记为{@code @Async}的那些。</font>
	 * 
	 * <p>Indicate whether subclass-based (CGLIB) proxies are to be created as opposed
	 * to standard Java interface-based proxies. The default is {@code false}. <strong>
	 * Applicable only if {@link #mode()} is set to {@link AdviceMode#PROXY}</strong>.
	 *
	 * <p>Note that setting this attribute to {@code true} will affect <em>all</em>
	 * Spring-managed beans requiring proxying, not just those marked with {@code @Async}.
	 * For example, other beans marked with Spring's {@code @Transactional} annotation
	 * will be upgraded to subclass proxying at the same time. This approach has no
	 * negative impact in practice unless one is explicitly expecting one type of proxy
	 * vs another, e.g. in tests.
	 */
	boolean proxyTargetClass() default false;

	/**
	 * 表示如何应用异步通知的代理行为。
	 * 
	 * <p>Indicate how async advice should be applied. The default is
	 * {@link AdviceMode#PROXY}.
	 * @see AdviceMode
	 */
	AdviceMode mode() default AdviceMode.PROXY;

	/**
	 * 表示应用于{@link AsyncAnnotationBeanPostProcessor}的顺序。
	 * 默认值是{@link Ordered#LOWEST_PRECEDENCE}，为了在所有其他后置处理器之后运行，
	 * 这样可以在现有的代理上添加一个通知器(advisor)，而不是双重代理。
	 * 
	 * <p>Indicate the order in which the
	 * {@link org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor}
	 * should be applied. The default is {@link Ordered#LOWEST_PRECEDENCE} in order to run
	 * after all other post-processors, so that it can add an advisor to
	 * existing proxies rather than double-proxy.
	 */
	int order() default Ordered.LOWEST_PRECEDENCE;

}
