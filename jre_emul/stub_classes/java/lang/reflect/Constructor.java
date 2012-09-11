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

/**
 * Stub implementation of Constructor.  The actual implementation
 * is in Constructor.h and Constructor.m, so the declared methods in
 * this class should match the actual methods implemented in order
 * to catch unsupported API references.
 *
 * @see Object
 */
public class Constructor<T> extends AccessibleObject {

  public String getName() {
    return null;
  }
  
  public int getModifiers() {
    return 0;
  }
  
  public Class<?> getDeclaringClass() {
    return null;
  }
  
  public Class<?>[] getParameterTypes() {
    return null;
  }
  
  public T newInstance(Object ... initargs) throws InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
    return null;
  }
  
  /* Not implemented
  public Class getDeclaringClass();
  public int getModifiers();
  public TypeVariable[] getTypeParameters();
  public Type getGenericReturnType();
  public Type[] getGenericParameterTypes();
  public Class[] getParameterTypes();
  public Class[] getExceptionTypes();
  public Type[] getGenericExceptionTypes();
  public boolean equals(Object);
  public int hashCode();
  public String toString();
  public String toGenericString();
  public boolean isBridge();
  public boolean isVarArgs();
  public boolean isSynthetic();
  public annotation.Annotation getAnnotation(Class);
  public annotation.Annotation[] getDeclaredAnnotations();
  public Object getDefaultValue();
  public annotation.Annotation[][] getParameterAnnotations();
  */
}
