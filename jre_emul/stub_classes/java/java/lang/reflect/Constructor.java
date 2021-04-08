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
 * Stub implementation of Constructor.  The actual implementation
 * is in Constructor.h and Constructor.m, so the declared methods in
 * this class should match the actual methods implemented in order
 * to catch unsupported API references.
 *
 * @see Object
 */

/**
 * {@code Constructor} provides information about, and access to, a single
 * constructor for a class.
 *
 * <p>{@code Constructor} permits widening conversions to occur when matching the
 * actual parameters to newInstance() with the underlying
 * constructor's formal parameters, but throws an
 * {@code IllegalArgumentException} if a narrowing conversion would occur.
 *
 * @param <T> the class in which the constructor is declared
 *
 * @see Member
 * @see java.lang.Class
 * @see java.lang.Class#getConstructors()
 * @see java.lang.Class#getConstructor(Class[])
 * @see java.lang.Class#getDeclaredConstructors()
 *
 * @author      Kenneth Russell
 * @author      Nakul Saraiya
 */
public final class Constructor<T> extends Executable {

  private Constructor() {}

  /**
   * Returns the name of this constructor, as a string.  This is
   * the binary name of the constructor's declaring class.
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
   * {@inheritDoc}
   */
  @Override
  public Class<T> getDeclaringClass() {
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
   * Uses the constructor represented by this {@code Constructor} object to
   * create and initialize a new instance of the constructor's
   * declaring class, with the specified initialization parameters.
   * Individual parameters are automatically unwrapped to match
   * primitive formal parameters, and both primitive and reference
   * parameters are subject to method invocation conversions as necessary.
   *
   * <p>If the number of formal parameters required by the underlying constructor
   * is 0, the supplied {@code initargs} array may be of length 0 or null.
   *
   * <p>If the constructor's declaring class is an inner class in a
   * non-static context, the first argument to the constructor needs
   * to be the enclosing instance; see section 15.9.3 of
   * <cite>The Java&trade; Language Specification</cite>.
   *
   * <p>If the required access and argument checks succeed and the
   * instantiation will proceed, the constructor's declaring class
   * is initialized if it has not already been initialized.
   *
   * <p>If the constructor completes normally, returns the newly
   * created and initialized instance.
   *
   * @param initargs array of objects to be passed as arguments to
   * the constructor call; values of primitive types are wrapped in
   * a wrapper object of the appropriate type (e.g. a {@code float}
   * in a {@link java.lang.Float Float})
   *
   * @return a new object created by calling the constructor
   * this object represents
   *
   * @exception IllegalAccessException    if this {@code Constructor} object
   *              is enforcing Java language access control and the underlying
   *              constructor is inaccessible.
   * @exception IllegalArgumentException  if the number of actual
   *              and formal parameters differ; if an unwrapping
   *              conversion for primitive arguments fails; or if,
   *              after possible unwrapping, a parameter value
   *              cannot be converted to the corresponding formal
   *              parameter type by a method invocation conversion; if
   *              this constructor pertains to an enum type.
   * @exception InstantiationException    if the class that declares the
   *              underlying constructor represents an abstract class.
   * @exception InvocationTargetException if the underlying constructor
   *              throws an exception.
   * @exception ExceptionInInitializerError if the initialization provoked
   *              by this method fails.
   */
  public T newInstance(Object ... initargs) throws InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
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
  @Override
  public AnnotatedType getAnnotatedReturnType() {
    return null;
  }

  /**
   * {@inheritDoc}
   * @since 1.5
   */
  @Override
  public Annotation[][] getParameterAnnotations() {
    return null;
  }

  /**
   * {@inheritDoc}
   * @throws GenericSignatureFormatError {@inheritDoc}
   * @since 1.5
   */
  @Override
  public TypeVariable<Constructor<T>>[] getTypeParameters() {
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
   * Returns a string describing this {@code Constructor},
   * including type parameters.  The string is formatted as the
   * constructor access modifiers, if any, followed by an
   * angle-bracketed comma separated list of the constructor's type
   * parameters, if any, followed by the fully-qualified name of the
   * declaring class, followed by a parenthesized, comma-separated
   * list of the constructor's generic formal parameter types.
   *
   * If this constructor was declared to take a variable number of
   * arguments, instead of denoting the last parameter as
   * "<tt><i>Type</i>[]</tt>", it is denoted as
   * "<tt><i>Type</i>...</tt>".
   *
   * A space is used to separate access modifiers from one another
   * and from the type parameters or return type.  If there are no
   * type parameters, the type parameter list is elided; if the type
   * parameter list is present, a space separates the list from the
   * class name.  If the constructor is declared to throw
   * exceptions, the parameter list is followed by a space, followed
   * by the word "{@code throws}" followed by a
   * comma-separated list of the thrown exception types.
   *
   * <p>The only possible modifiers for constructors are the access
   * modifiers {@code public}, {@code protected} or
   * {@code private}.  Only one of these may appear, or none if the
   * constructor has default (package) access.
   *
   * @return a string describing this {@code Constructor},
   * include type parameters
   *
   * @since 1.5
   * @jls 8.8.3. Constructor Modifiers
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
   * Compares this {@code Constructor} against the specified object.
   * Returns true if the objects are the same.  Two {@code Constructor} objects are
   * the same if they were declared by the same class and have the
   * same formal parameter types.
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
   * Returns a hashcode for this {@code Constructor}. The hashcode is
   * the same as the hashcode for the underlying constructor's
   * declaring class name.
   */
  public int hashCode() {
    return -1;
  }

  /**
   * Returns a string describing this {@code Constructor}.  The string is
   * formatted as the constructor access modifiers, if any,
   * followed by the fully-qualified name of the declaring class,
   * followed by a parenthesized, comma-separated list of the
   * constructor's formal parameter types.  For example:
   * <pre>
   *    public java.util.Hashtable(int,float)
   * </pre>
   *
   * <p>The only possible modifiers for constructors are the access
   * modifiers {@code public}, {@code protected} or
   * {@code private}.  Only one of these may appear, or none if the
   * constructor has default (package) access.
   *
   * @return a string describing this {@code Constructor}
   * @jls 8.8.3. Constructor Modifiers
   */
  public java.lang.String toString() {
    return null;
  }
}
