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

package org.springframework.core.type;

/**
 * 定义一个特定类的抽象元数据信息的接口。
 * 
 * <p>Interface that defines abstract metadata of a specific class,
 * in a form that does not require that class to be loaded yet.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see StandardClassMetadata
 * @see org.springframework.core.type.classreading.MetadataReader#getClassMetadata()
 * @see AnnotationMetadata
 */
public interface ClassMetadata {

	/**
	 * 返回底层类的名称。
	 * 
	 * <p>Return the name of the underlying class.
	 */
	String getClassName();

	/**
	 * 返回底层类是否表示一个接口。
	 * 
	 * <p>Return whether the underlying class represents an interface.
	 */
	boolean isInterface();

	/**
	 * 返回底层类是否被标记为抽象('abstract')。
	 * 
	 * <p>Return whether the underlying class is marked as abstract.
	 */
	boolean isAbstract();

	/**
	 * 返回底层类是否表示一个具体的类，非一个接口和抽象类。
	 * 
	 * <p>Return whether the underlying class represents a concrete class,
	 * i.e. neither an interface nor an abstract class.
	 */
	boolean isConcrete();

	/**
	 * 返回底层类是否被标记为'final'(是否能定义子类)。
	 * 
	 * <p>Return whether the underlying class is marked as 'final'.
	 */
	boolean isFinal();

	/**
	 * 确定底层类是否独立的。
	 * 例如，是否是顶层类或嵌套类(静态内部类)，其可以独立从一个封闭类来构造。
	 * 
	 * <p>Determine whether the underlying class is independent,
	 * i.e. whether it is a top-level class or a nested class
	 * (static inner class) that can be constructed independent
	 * from an enclosing class.
	 */
	boolean isIndependent();


	// 内部类
	/**
	 * 返回底层类是否含有一个封闭类(底层类是一个内部/嵌套类或者方法中的一个本地类)。
	 * 
	 * <p>如果本方法返回{@code false}，则意味着底层类是一个顶层类。
	 * 
	 * <p>Return whether the underlying class has an enclosing class
	 * (i.e. the underlying class is an inner/nested class or
	 * a local class within a method).
	 * 
	 * <p>If this method returns {@code false}, then the
	 * underlying class is a top-level class.
	 */
	boolean hasEnclosingClass();

	/**
	 * 返回底层类中封闭类的名称；如果底层类是一个顶层类，则返回{@code null}。
	 * 
	 * <p>Return the name of the enclosing class of the underlying class,
	 * or {@code null} if the underlying class is a top-level class.
	 */
	String getEnclosingClassName();

	/**
	 * 返回底层类是否含有一个超类。
	 * 
	 * <p>Return whether the underlying class has a super class.
	 */
	boolean hasSuperClass();

	/**
	 * 返回底层类中超类的名称；如果没有任何超类定义，则返回{@code null}。
	 * 
	 * <p>Return the name of the super class of the underlying class,
	 * or {@code null} if there is no super class defined.
	 */
	String getSuperClassName();

	/**
	 * 返回底层类实现的所有接口的名称；如果未实现任何接口，则返回一个空数组。
	 * 
	 * <p>Return the names of all interfaces that the underlying class
	 * implements, or an empty array if there are none.
	 */
	String[] getInterfaceNames();

	/**
	 * 返回本对象表示的类型所声明的所有成员类型的名称集合，
	 * 包括本类中公共、受保护、包默认和私有的类和接口，但不包括继承的类型和接口。
	 * 如果没有任何成员的类型和接口存在，则返回一个空数组。
	 * 
	 * <p>Return the names of all classes declared as members of the class represented by
	 * this ClassMetadata object. This includes public, protected, default (package)
	 * access, and private classes and interfaces declared by the class, but excludes
	 * inherited classes and interfaces. An empty array is returned if no member classes
	 * or interfaces exist.
	 */
	String[] getMemberClassNames();

}
