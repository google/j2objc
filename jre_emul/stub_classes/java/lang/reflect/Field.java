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
 * Stub implementation of Field.  The actual implementation
 * is in Field.h and Field.m, so the declared fields in this
 * class should match the actual fields implemented in order
 * to catch unsupported API references.
 *
 * @see Object
 */
public class Field extends AccessibleObject {

  public String getName() {
    return null;
  }

  public int getModifiers() {
    return 0;
  }
  
  public Type getType() {
    return null;
  }

  public Class getDeclaringClass() {
    return null;
  }
  
  public Object get() {
    return null;
  }
  
  public boolean getBoolean() {
    return false;
  }
  
  public byte getByte() {
    return 0;
  }
  
  public char getChar() {
    return 0;
  }
  
  public double getDouble() {
    return 0.0;
  }
  
  public float getFloat() {
    return 0.0f;
  }
  
  public int getInt() {
    return 0;
  }
  
  public long getLong() {
    return 0L;
  }
  
  public short getShort() {
    return 0;
  }
  
  public void set(Object o, Object value) {}
  public void set(Object o, boolean b) {}
  public void set(Object o, byte b) {}
  public void set(Object o, char c) {}
  public void set(Object o, double d) {}
  public void set(Object o, float f) {}
  public void set(Object o, int i) {}
  public void set(Object o, long l) {}
  public void set(Object o, short s) {}
  
  /* Not implemented
  public annotation.Annotation getAnnotation(Class);
  public annotation.Annotation[] getDeclaredAnnotations();
  public boolean isEnumConstant();
  public boolean isSynthetic();
  public String toGenericString();
  */
}
