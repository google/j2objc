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
package java.lang.reflect;

import java.lang.annotation.Annotation;

/**
 * Stub implementation of Executable.  The actual implementation
 * is in Method.h and Method.m, so the declared methods in this
 * class should match the actual methods implemented in order
 * to catch unsupported API references.
 *
 * @since 1.8
 * @see Object
 */
public abstract class Executable extends AccessibleObject implements Member, GenericDeclaration {

  Executable() {}

  public abstract Class<?> getDeclaringClass();
  public abstract String getName();
  public abstract int getModifiers();
  public abstract TypeVariable<?>[] getTypeParameters();
  public abstract Class<?>[] getParameterTypes();
  public abstract Class<?>[] getExceptionTypes();
  public abstract Annotation[][] getParameterAnnotations();
  public abstract String toGenericString();

  public int getParameterCount() {
    return 0;
  }

  public Type[] getGenericParameterTypes() {
    return null;
  }

  public Parameter[] getParameters() {
    return null;
  }

  public Type[] getGenericExceptionTypes() {
    return null;
  }

  public boolean isVarArgs() {
    return false;
  }

  public boolean isSynthetic() {
    return false;
  }

  public <T extends Annotation> T getAnnotation(Class<T> cls) {
    return null;
  }

  public <T extends Annotation> T[] getAnnotationsByType(Class<T> cls) {
    return null;
  }

  public Annotation[] getDeclaredAnnotations() {
    return null;
  }

  boolean hasRealParameterData() {
    return false;
  }

  Type[] getAllGenericParameterTypes() {
    return null;
  }

  public final boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
    return false;
  }
}
