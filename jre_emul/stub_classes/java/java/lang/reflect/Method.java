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

/*
 * Stub implementation of Method.  The actual implementation
 * is in Method.h and Method.m, so the declared methods in this
 * class should match the actual methods implemented in order
 * to catch unsupported API references. Doc-comments are included
 * to generate JRE reference documentation.
 *
 * @see Object
 */


/**
 * A {@code Method} provides information about, and access to, a single method
 * on a class or interface.  The reflected method may be a class method
 * or an instance method (including an abstract method).
 *
 * <p>A {@code Method} permits widening conversions to occur when matching the
 * actual parameters to invoke with the underlying method's formal
 * parameters, but it throws an {@code IllegalArgumentException} if a
 * narrowing conversion would occur.
 *
 * @see Member
 * @see java.lang.Class
 * @see java.lang.Class#getMethods()
 * @see java.lang.Class#getMethod(String, Class[])
 * @see java.lang.Class#getDeclaredMethods()
 * @see java.lang.Class#getDeclaredMethod(String, Class[])
 *
 * @author Kenneth Russell
 * @author Nakul Saraiya
 */
public final class Method extends Executable {

  private Method() {}

  /**
   * Returns the name of the method represented by this {@code Method}
   * object, as a {@code String}.
   */
  @Override
  public String getName() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getModifiers() {
    return 0;
  }

  /**
   * Returns a {@code Class} object that represents the formal return type
   * of the method represented by this {@code Method} object.
   *
   * @return the return type for the method this object represents
   */
  public Class<?> getReturnType() {
    return null;
  }

  /**
   * Returns a {@code Type} object that represents the formal return
   * type of the method represented by this {@code Method} object.
   *
   * <p>If the return type is a parameterized type,
   * the {@code Type} object returned must accurately reflect
   * the actual type parameters used in the source code.
   *
   * <p>If the return type is a type variable or a parameterized type, it
   * is created. Otherwise, it is resolved.
   *
   * @return  a {@code Type} object that represents the formal return
   *     type of the underlying  method
   * @throws GenericSignatureFormatError
   *     if the generic method signature does not conform to the format
   *     specified in
   *     <cite>The Java&trade; Virtual Machine Specification</cite>
   * @throws TypeNotPresentException if the underlying method's
   *     return type refers to a non-existent type declaration
   * @throws MalformedParameterizedTypeException if the
   *     underlying method's return typed refers to a parameterized
   *     type that cannot be instantiated for any reason
   * @since 1.5
   */
  public Type getGenericReturnType() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Class<?> getDeclaringClass() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Class<?>[] getParameterTypes() {
    return null;
  }

  /**
   * {@inheritDoc}
   * @throws GenericSignatureFormatError {@inheritDoc}
   * @throws TypeNotPresentException {@inheritDoc}
   * @throws MalformedParameterizedTypeException {@inheritDoc}
   * @since 1.5
   */
  @Override
  public Type[] getGenericParameterTypes() {
    return null;
  }

  /**
   * Invokes the underlying method represented by this {@code Method}
   * object, on the specified object with the specified parameters.
   * Individual parameters are automatically unwrapped to match
   * primitive formal parameters, and both primitive and reference
   * parameters are subject to method invocation conversions as
   * necessary.
   *
   * <p>If the underlying method is static, then the specified {@code obj}
   * argument is ignored. It may be null.
   *
   * <p>If the number of formal parameters required by the underlying method is
   * 0, the supplied {@code args} array may be of length 0 or null.
   *
   * <p>If the underlying method is an instance method, it is invoked
   * using dynamic method lookup as documented in The Java Language
   * Specification, Second Edition, section 15.12.4.4; in particular,
   * overriding based on the runtime type of the target object will occur.
   *
   * <p>If the underlying method is static, the class that declared
   * the method is initialized if it has not already been initialized.
   *
   * <p>If the method completes normally, the value it returns is
   * returned to the caller of invoke; if the value has a primitive
   * type, it is first appropriately wrapped in an object. However,
   * if the value has the type of an array of a primitive type, the
   * elements of the array are <i>not</i> wrapped in objects; in
   * other words, an array of primitive type is returned.  If the
   * underlying method return type is void, the invocation returns
   * null.
   *
   * @param obj  the object the underlying method is invoked from
   * @param args the arguments used for the method call
   * @return the result of dispatching the method represented by
   * this object on {@code obj} with parameters
   * {@code args}
   *
   * @exception IllegalAccessException    if this {@code Method} object
   *              is enforcing Java language access control and the underlying
   *              method is inaccessible.
   * @exception IllegalArgumentException  if the method is an
   *              instance method and the specified object argument
   *              is not an instance of the class or interface
   *              declaring the underlying method (or of a subclass
   *              or implementor thereof); if the number of actual
   *              and formal parameters differ; if an unwrapping
   *              conversion for primitive arguments fails; or if,
   *              after possible unwrapping, a parameter value
   *              cannot be converted to the corresponding formal
   *              parameter type by a method invocation conversion.
   * @exception InvocationTargetException if the underlying method
   *              throws an exception.
   * @exception NullPointerException      if the specified object is null
   *              and the method is an instance method.
   * @exception ExceptionInInitializerError if the initialization
   * provoked by this method fails.
   */
  public Object invoke(Object obj, Object... args)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    return null;
  }

  /**
   * {@inheritDoc}
   * @throws NullPointerException  {@inheritDoc}
   * @since 1.5
   */
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    return null;
  }

  /**
   * {@inheritDoc}
   * @since 1.5
   */
  public Annotation[] getDeclaredAnnotations() {
    return null;
  }

  /**
   * {@inheritDoc}
   * @since 1.8
   */
  public AnnotatedType getAnnotatedReturnType() {
    return null;
  }

  /**
   * {@inheritDoc}
   * @since 1.5
   */
  public Annotation[][] getParameterAnnotations() {
    return null;
  }

  /**
   * {@inheritDoc}
   * @throws GenericSignatureFormatError {@inheritDoc}
   * @since 1.5
   */
  @Override
  public TypeVariable<Method>[] getTypeParameters() {
    return null;
  }

  /**
   * {@inheritDoc}
   * @jls 13.1 The Form of a Binary
   * @since 1.5
   */
  @Override
  public boolean isSynthetic() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Class<?>[] getExceptionTypes() {
    return null;
  }

  /**
   * {@inheritDoc}
   * @throws GenericSignatureFormatError {@inheritDoc}
   * @throws TypeNotPresentException {@inheritDoc}
   * @throws MalformedParameterizedTypeException {@inheritDoc}
   * @since 1.5
   */
  @Override
  public Type[] getGenericExceptionTypes() {
    return null;
  }

  /**
   * Returns a string describing this {@code Method}, including
   * type parameters.  The string is formatted as the method access
   * modifiers, if any, followed by an angle-bracketed
   * comma-separated list of the method's type parameters, if any,
   * followed by the method's generic return type, followed by a
   * space, followed by the class declaring the method, followed by
   * a period, followed by the method name, followed by a
   * parenthesized, comma-separated list of the method's generic
   * formal parameter types.
   *
   * If this method was declared to take a variable number of
   * arguments, instead of denoting the last parameter as
   * "<tt><i>Type</i>[]</tt>", it is denoted as
   * "<tt><i>Type</i>...</tt>".
   *
   * A space is used to separate access modifiers from one another
   * and from the type parameters or return type.  If there are no
   * type parameters, the type parameter list is elided; if the type
   * parameter list is present, a space separates the list from the
   * class name.  If the method is declared to throw exceptions, the
   * parameter list is followed by a space, followed by the word
   * throws followed by a comma-separated list of the generic thrown
   * exception types.
   *
   * <p>The access modifiers are placed in canonical order as
   * specified by "The Java Language Specification".  This is
   * {@code public}, {@code protected} or {@code private} first,
   * and then other modifiers in the following order:
   * {@code abstract}, {@code default}, {@code static}, {@code final},
   * {@code synchronized}, {@code native}, {@code strictfp}.
   *
   * @return a string describing this {@code Method},
   * include type parameters
   *
   * @since 1.5
   *
   * @jls 8.4.3 Method Modifiers
   */
  @Override
  public String toGenericString() {
    return null;
  }

  /**
   * {@inheritDoc}
   * @since 1.5
   */
  @Override
  public boolean isVarArgs() {
    return false;
  }

  /**
   * Returns the default value for the annotation member represented by
   * this {@code Method} instance.  If the member is of a primitive type,
   * an instance of the corresponding wrapper type is returned. Returns
   * null if no default is associated with the member, or if the method
   * instance does not represent a declared member of an annotation type.
   *
   * @return the default value for the annotation member represented
   *     by this {@code Method} instance.
   * @throws TypeNotPresentException if the annotation is of type
   *     {@link Class} and no definition can be found for the
   *     default class value.
   * @since  1.5
   */
  public Object getDefaultValue() {
    return null;
  }

  /**
   * Returns {@code true} if this method is a bridge
   * method; returns {@code false} otherwise.
   *
   * @return true if and only if this method is a bridge
   * method as defined by the Java Language Specification.
   * @since 1.5
   */
  public boolean isBridge() {
    return false;
  }

  /**
   * Returns {@code true} if this method is a default
   * method; returns {@code false} otherwise.
   *
   * A default method is a public non-abstract instance method, that
   * is, a non-static method with a body, declared in an interface
   * type.
   *
   * @return true if and only if this method is a default
   * method as defined by the Java Language Specification.
   * @since 1.8
   */
  public boolean isDefault() {
    return false;
  }

  /**
   * Compares this {@code Method} against the specified object.  Returns
   * true if the objects are the same.  Two {@code Methods} are the same if
   * they were declared by the same class and have the same name
   * and formal parameter types and return type.
   */
  public boolean equals(Object obj) {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public int getParameterCount() {
    return -1;
  }

  /**
   * Returns a hashcode for this {@code Method}.  The hashcode is computed
   * as the exclusive-or of the hashcodes for the underlying
   * method's declaring class name and the method's name.
   */
  public int hashCode() {
    return -1;
  }

  /**
   * Returns a string describing this {@code Method}.  The string is
   * formatted as the method access modifiers, if any, followed by
   * the method return type, followed by a space, followed by the
   * class declaring the method, followed by a period, followed by
   * the method name, followed by a parenthesized, comma-separated
   * list of the method's formal parameter types. If the method
   * throws checked exceptions, the parameter list is followed by a
   * space, followed by the word throws followed by a
   * comma-separated list of the thrown exception types.
   * For example:
   * <pre>
   *    public boolean java.lang.Object.equals(java.lang.Object)
   * </pre>
   *
   * <p>The access modifiers are placed in canonical order as
   * specified by "The Java Language Specification".  This is
   * {@code public}, {@code protected} or {@code private} first,
   * and then other modifiers in the following order:
   * {@code abstract}, {@code default}, {@code static}, {@code final},
   * {@code synchronized}, {@code native}, {@code strictfp}.
   *
   * @return a string describing this {@code Method}
   *
   * @jls 8.4.3 Method Modifiers
   */
  public java.lang.String toString() {
    return null;
  }
}
