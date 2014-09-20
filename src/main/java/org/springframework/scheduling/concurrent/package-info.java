/**
 * 提供支持JDK 1.5+并发包(java.util.concurrent)中的<b>执行器机制</b>的调度工具类，
 * 允许在Spring上下文中设置一个线程池执行器(ThreadPoolExecutor或ScheduledThreadPoolExecutor)
 * 作为一个bean。
 *
 * <p>Scheduling convenience classes for the JDK 1.5+ Executor mechanism
 * in the {@code java.util.concurrent} package, allowing to set up a
 * ThreadPoolExecutor or ScheduledThreadPoolExecutor as a bean in a Spring
 * context. Provides support for the native {@code java.util.concurrent}
 * interfaces as well as the Spring {@code TaskExecutor} mechanism.
 *
 */
package org.springframework.scheduling.concurrent;

