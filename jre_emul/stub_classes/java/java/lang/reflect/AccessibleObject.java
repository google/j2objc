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
 * Stub implementation of AccessibleObject. The actual implementation is in
 * AccessibleObject.h and AccessibleObject.m, so the declared methods in this
 * class should match the actual methods implemented in order to catch
 * unsupported API references.
 */

/**
 * The AccessibleObject class is the base class for Field, Method and
 * Constructor objects.  It provides the ability to flag a reflected
 * object as suppressing default Java language access control checks
 * when it is used.  The access checks--for public, default (package)
 * access, protected, and private members--are performed when Fields,
 * Methods or Constructors are used to set or get fields, to invoke
 * methods, or to create and initialize new instances of classes,
 * respectively.
 *
 * <p>Setting the {@code accessible} flag in a reflected object
 * permits sophisticated applications with sufficient privilege, such
 * as Java Object Serialization or other persistence mechanisms, to
 * manipulate objects in a manner that would normally be prohibited.
 *
 * <p>By default, a reflected object is <em>not</em> accessible.
 *
 * @see Field
 * @see Method
 * @see Constructor
 * @see ReflectPermission
 *
 * @since 1.2
 */
public class AccessibleObject implements AnnotatedElement {

  protected AccessibleObject() {}

  /**
   * Get the value of the {@code accessible} flag for this object.
   *
   * @return the value of the object's {@code accessible} flag
   */
  public boolean isAccessible() {
    return false;
  }

  /**
   * Set the {@code accessible} flag for this object to
   * the indicated boolean value.  A value of {@code true} indicates that
   * the reflected object should suppress Java language access
   * checking when it is used.  A value of {@code false} indicates
   * that the reflected object should enforce Java language access checks.
   *
   * <p>First, if there is a security manager, its
   * {@code checkPermission} method is called with a
   * {@code ReflectPermission("suppressAccessChecks")} permission.
   *
   * <p>A {@code SecurityException} is raised if {@code flag} is
   * {@code true} but accessibility of this object may not be changed
   * (for example, if this element object is a {@link Constructor} object for
   * the class {@link java.lang.Class}).
   *
   * <p>A {@code SecurityException} is raised if this object is a {@link
   * java.lang.reflect.Constructor} object for the class
   * {@code java.lang.Class}, and {@code flag} is true.
   *
   * @param flag the new value for the {@code accessible} flag
   * @throws SecurityException if the request is denied.
   * @see SecurityManager#checkPermission
   * @see java.lang.RuntimePermission
   */
  public void setAccessible(boolean flag) throws SecurityException {}

  /**
   * Convenience method to set the {@code accessible} flag for an
   * array of objects with a single security check (for efficiency).
   *
   * <p>First, if there is a security manager, its
   * {@code checkPermission} method is called with a
   * {@code ReflectPermission("suppressAccessChecks")} permission.
   *
   * <p>A {@code SecurityException} is raised if {@code flag} is
   * {@code true} but accessibility of any of the elements of the input
   * {@code array} may not be changed (for example, if the element
   * object is a {@link Constructor} object for the class {@link
   * java.lang.Class}).  In the event of such a SecurityException, the
   * accessibility of objects is set to {@code flag} for array elements
   * upto (and excluding) the element for which the exception occurred; the
   * accessibility of elements beyond (and including) the element for which
   * the exception occurred is unchanged.
   *
   * @param array the array of AccessibleObjects
   * @param flag  the new value for the {@code accessible} flag
   *              in each object
   * @throws SecurityException if the request is denied.
   * @see SecurityManager#checkPermission
   * @see java.lang.RuntimePermission
   */
  public static void setAccessible(AccessibleObject[] array, boolean flag)
      throws SecurityException {}

  /**
   * @throws NullPointerException {@inheritDoc}
   * @since 1.5
   */
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    return null;
  }

  /**
   * {@inheritDoc}
   * @throws NullPointerException {@inheritDoc}
   * @since 1.5
   */
  @Override
  public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
    return false;
  }

  /**
   * @since 1.5
   */
  public Annotation[] getAnnotations() {
    return null;
  }

  /**
   * @since 1.5
   */
  public Annotation[] getDeclaredAnnotations() {
    return null;
  }

  /**
   * @throws NullPointerException {@inheritDoc}
   * @since 1.8
   */
  @Override
  public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
    return null;
  }

  /**
   * @throws NullPointerException {@inheritDoc}
   * @since 1.8
   */
  @Override
  public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
    return null;
  }

  /**
   * @throws NullPointerException {@inheritDoc}
   * @since 1.8
   */
  @Override
  public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
    return null;
  }
}
