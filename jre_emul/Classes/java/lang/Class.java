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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Stub implementation of java.lang.Class.
 *
 * @see java.lang.Object
 */
public final class Class<T> {

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

  public Class<?>[] getDeclaredClasses() throws SecurityException {
  	return null;
  }

  public Constructor<T> getDeclaredConstructor(Class<?>... parameterTypes)
      throws NoSuchMethodException, SecurityException {
  	return null;
  }

  public Constructor<?>[] getDeclaredConstructors() throws SecurityException {
  	return null;
  }

  public Field getDeclaredField(String name) throws NoSuchFieldException, SecurityException {
  	return null;
  }

  public Field[] getDeclaredFields() throws SecurityException {
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

  public Field getField(String name) throws NoSuchFieldException, SecurityException {
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

  public Object[] getSigners() {
    return null;
  }

  public String getSimpleName() {
  	return "";
  }

  public Class<? super T> getSuperclass() {
    return null;
  }

  public Object[] getTypeParameters() {
  	return null;
  }

  public boolean isAnnotation() {
  	return false;
  }

  public boolean isAnnotationPresent() {
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

  /* Unimplemented/mapped methods
  T[] getEnumConstants()
  ProtectionDomain getProtectionDomain()
  InputStream getResourceAsStream(String name)
  URL getResource(String name)
  */
}
