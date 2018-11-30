/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.lang;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.security.ProtectionDomain;

/**
 * Stub implementation of java.lang.Class.
 *
 * @see java.lang.Object
 */
public final class Class<T> implements Serializable, GenericDeclaration, Type, AnnotatedElement {
  private static final long serialVersionUID = 3206093459760846163L;

  private Class() {}

  public static Class<?> forName(String className) throws ClassNotFoundException {
    return null;
  }

  public static Class<?> forName(String name, boolean initialize, ClassLoader loader)
      throws ClassNotFoundException {
  	return null;
  }
  public <U> Class<? extends U> asSubclass(Class<U> clazz) {
    return null;
  }

  public T cast(Object obj) {
  	return null;
  }

  public boolean desiredAssertionStatus() {
    return false;
  }

  public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
    return null;
  }

  public Annotation[] getAnnotations() {
    return null;
  }

  public String getCanonicalName() {
  	return "";
  }

  public Class<?>[] getClasses() {
    return null;
  }

  public ClassLoader getClassLoader() {
    return null;
  }

  public Class<?> getComponentType() {
    return null;
  }

  public Constructor<T> getConstructor(Class<?>... parameterTypes)
      throws NoSuchMethodException, SecurityException {
    return null;
  }

  public Constructor<?>[] getConstructors() throws SecurityException {
    return null;
  }

  public Annotation[] getDeclaredAnnotations()  {
    return null;
  }

  public Class<?>[] getDeclaredClasses() {
  	return null;
  }

  public Constructor<T> getDeclaredConstructor(Class<?>... parameterTypes)
      throws NoSuchMethodException, SecurityException {
  	return null;
  }

  public Constructor<?>[] getDeclaredConstructors() throws SecurityException {
  	return null;
  }

  public Field getDeclaredField(String name) throws NoSuchFieldException {
  	return null;
  }

  public Field[] getDeclaredFields() {
  	return null;
  }

  public Method getDeclaredMethod(String name, Class<?>... parameterTypes)
      throws NoSuchMethodException, SecurityException {
  	return null;
  }

  public Method[] getDeclaredMethods() throws SecurityException {
  	return null;
  }

  public Class<?> getDeclaringClass() {
    return null;
  }

  public Class<?> getEnclosingClass() {
    return null;
  }

  public Constructor<?> getEnclosingConstructor() {
    return null;
  }

  public Method getEnclosingMethod() {
    return null;
  }

  public T[] getEnumConstants() {
    return null;
  }

  T[] getEnumConstantsShared() {
    return null;
  }

  public Field getField(String name) throws NoSuchFieldException {
    return null;
  }

  public Field[] getFields() throws SecurityException {
    return null;
  }

  public Type[] getGenericInterfaces() {
  	return null;
  }

  public Type getGenericSuperclass() {
  	return null;
  }

  public Class<?>[] getInterfaces() {
    return null;
  }

  public Method getMethod(String name, Class<?>... parameterTypes)
      throws NoSuchMethodException, SecurityException {
  	return null;
  }

  public Method[] getMethods() throws SecurityException {
    return null;
  }

  public int getModifiers() {
    return 0;
  }

  public String getName() {
  	return "";
  }

  public Package getPackage() {
    return null;
  }

  public ProtectionDomain getProtectionDomain() {
    return null;
  }

  public URL getResource(String name) {
    return null;
  }

  public InputStream getResourceAsStream(String name) {
    return null;
  }

  public Object[] getSigners() {
    return null;
  }

  public String getSimpleName() {
  	return "";
  }

  public Class<? super T> getSuperclass() {
    return null;
  }

  public synchronized TypeVariable<Class<T>>[] getTypeParameters() {
	return null;
  }

  public boolean isAnnotation() {
  	return false;
  }

  public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
    return false;
  }

  public boolean isAnonymousClass() {
  	return false;
  }

  public boolean isArray() {
    return false;
  }

  public boolean isAssignableFrom(Class<?> cls) {
    return false;
  }

  public boolean isEnum() {
  	return false;
  }

  public boolean isInstance(Object obj) {
    return false;
  }

  public boolean isInterface() {
    return false;
  }

  public boolean isLocalClass() {
  	return false;
  }

  public boolean isMemberClass() {
  	return false;
  }

  public boolean isPrimitive() {
    return false;
  }

  public boolean isSynthetic() {
  	return false;
  }

  public T newInstance() throws InstantiationException, IllegalAccessException {
  	return null;
  }

  public String toString() {
    return "";
  }

  /**
   * @since 1.8
   */
  public <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationClass) {
    return null;
  }

  /**
   * @since 1.8
   */
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
    return null;
  }

  /**
   * @since 1.8
   */
  public <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass) {
    return null;
  }

  /**
   * @since 1.8
   */
  public String getTypeName() {
    return null;
  }

  /**
   * @since 1.8
   */
  public String toGenericString() {
    return null;
  }
}
