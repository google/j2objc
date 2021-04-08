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

/*
 * Stub implementation of java.lang.Class.
 *
 * @see java.lang.Object
 */

/**
 * Instances of the class {@code Class} represent classes and
 * interfaces in a running Java application.  An enum is a kind of
 * class and an annotation is a kind of interface.  Every array also
 * belongs to a class that is reflected as a {@code Class} object
 * that is shared by all arrays with the same element type and number
 * of dimensions.  The primitive Java types ({@code boolean},
 * {@code byte}, {@code char}, {@code short},
 * {@code int}, {@code long}, {@code float}, and
 * {@code double}), and the keyword {@code void} are also
 * represented as {@code Class} objects.
 *
 * <p> {@code Class} has no public constructor. Instead {@code Class}
 * objects are constructed automatically by the Java Virtual Machine as classes
 * are loaded and by calls to the {@code defineClass} method in the class
 * loader.
 *
 * <p> The following example uses a {@code Class} object to print the
 * class name of an object:
 *
 * <blockquote><pre>
 *     void printClassName(Object obj) {
 *         System.out.println("The class of " + obj +
 *                            " is " + obj.getClass().getName());
 *     }
 * </pre></blockquote>
 *
 * <p> It is also possible to get the {@code Class} object for a named
 * type (or for void) using a class literal.  See Section 15.8.2 of
 * <cite>The Java&trade; Language Specification</cite>.
 * For example:
 *
 * <blockquote>
 *     {@code System.out.println("The name of class Foo is: "+Foo.class.getName());}
 * </blockquote>
 *
 * @param <T> the type of the class modeled by this {@code Class}
 * object.  For example, the type of {@code String.class} is {@code
 * Class<String>}.  Use {@code Class<?>} if the class being modeled is
 * unknown.
 *
 * @author  unascribed
 * @see     java.lang.ClassLoader#defineClass(byte[], int, int)
 * @since   JDK1.0
 */
public final class Class<T> implements Serializable, GenericDeclaration, Type, AnnotatedElement {
  private static final long serialVersionUID = 3206093459760846163L;

  private Class() {}

  /**
   * Returns the {@code Class} object associated with the class or
   * interface with the given string name.  Invoking this method is
   * equivalent to:
   *
   * <blockquote>
   *  {@code Class.forName(className, true, currentLoader)}
   * </blockquote>
   *
   * where {@code currentLoader} denotes the defining class loader of
   * the current class.
   *
   * <p> For example, the following code fragment returns the
   * runtime {@code Class} descriptor for the class named
   * {@code java.lang.Thread}:
   *
   * <blockquote>
   *   {@code Class t = Class.forName("java.lang.Thread")}
   * </blockquote>
   * <p>
   * A call to {@code forName("X")} causes the class named
   * {@code X} to be initialized.
   *
   * @param      className   the fully qualified name of the desired class.
   * @return     the {@code Class} object for the class with the
   *             specified name.
   * @exception LinkageError if the linkage fails
   * @exception ExceptionInInitializerError if the initialization provoked
   *            by this method fails
   * @exception ClassNotFoundException if the class cannot be located
   */
  public static Class<?> forName(String className) throws ClassNotFoundException {
    return null;
  }

  /**
   * Returns the {@code Class} object associated with the class or
   * interface with the given string name, using the given class loader.
   * Given the fully qualified name for a class or interface (in the same
   * format returned by {@code getName}) this method attempts to
   * locate, load, and link the class or interface.  The specified class
   * loader is used to load the class or interface.  If the parameter
   * {@code loader} is null, the class is loaded through the bootstrap
   * class loader.  The class is initialized only if the
   * {@code initialize} parameter is {@code true} and if it has
   * not been initialized earlier.
   *
   * <p> If {@code name} denotes a primitive type or void, an attempt
   * will be made to locate a user-defined class in the unnamed package whose
   * name is {@code name}. Therefore, this method cannot be used to
   * obtain any of the {@code Class} objects representing primitive
   * types or void.
   *
   * <p> If {@code name} denotes an array class, the component type of
   * the array class is loaded but not initialized.
   *
   * <p> For example, in an instance method the expression:
   *
   * <blockquote>
   *  {@code Class.forName("Foo")}
   * </blockquote>
   *
   * is equivalent to:
   *
   * <blockquote>
   *  {@code Class.forName("Foo", true, this.getClass().getClassLoader())}
   * </blockquote>
   *
   * Note that this method throws errors related to loading, linking or
   * initializing as specified in Sections 12.2, 12.3 and 12.4 of <em>The
   * Java Language Specification</em>.
   * Note that this method does not check whether the requested class
   * is accessible to its caller.
   *
   * <p> If the {@code loader} is {@code null}, and a security
   * manager is present, and the caller's class loader is not null, then this
   * method calls the security manager's {@code checkPermission} method
   * with a {@code RuntimePermission("getClassLoader")} permission to
   * ensure it's ok to access the bootstrap class loader.
   *
   * @param name       fully qualified name of the desired class
   * @param initialize if {@code true} the class will be initialized.
   *                   See Section 12.4 of <em>The Java Language Specification</em>.
   * @param loader     class loader from which the class must be loaded
   * @return           class object representing the desired class
   *
   * @exception LinkageError if the linkage fails
   * @exception ExceptionInInitializerError if the initialization provoked
   *            by this method fails
   * @exception ClassNotFoundException if the class cannot be located by
   *            the specified class loader
   *
   * @see       java.lang.Class#forName(String)
   * @see       java.lang.ClassLoader
   * @since     1.2
   */
  public static Class<?> forName(String name, boolean initialize, ClassLoader loader)
      throws ClassNotFoundException {
  	return null;
  }

  /**
   * Casts this {@code Class} object to represent a subclass of the class
   * represented by the specified class object.  Checks that the cast
   * is valid, and throws a {@code ClassCastException} if it is not.  If
   * this method succeeds, it always returns a reference to this class object.
   *
   * <p>This method is useful when a client needs to "narrow" the type of
   * a {@code Class} object to pass it to an API that restricts the
   * {@code Class} objects that it is willing to accept.  A cast would
   * generate a compile-time warning, as the correctness of the cast
   * could not be checked at runtime (because generic types are implemented
   * by erasure).
   *
   * @param <U> the type to cast this class object to
   * @param clazz the class of the type to cast this class object to
   * @return this {@code Class} object, cast to represent a subclass of
   *    the specified class object.
   * @throws ClassCastException if this {@code Class} object does not
   *    represent a subclass of the specified class (here "subclass" includes
   *    the class itself).
   * @since 1.5
   */
  public <U> Class<? extends U> asSubclass(Class<U> clazz) {
    return null;
  }

  /**
   * Casts an object to the class or interface represented
   * by this {@code Class} object.
   *
   * @param obj the object to be cast
   * @return the object after casting, or null if obj is null
   *
   * @throws ClassCastException if the object is not
   * null and is not assignable to the type T.
   *
   * @since 1.5
   */
  public T cast(Object obj) {
  	return null;
  }

  /**
   * Returns the assertion status that would be assigned to this
   * class if it were to be initialized at the time this method is invoked.
   * If this class has had its assertion status set, the most recent
   * setting will be returned; otherwise, if any package default assertion
   * status pertains to this class, the most recent setting for the most
   * specific pertinent package default assertion status is returned;
   * otherwise, if this class is not a system class (i.e., it has a
   * class loader) its class loader's default assertion status is returned;
   * otherwise, the system class default assertion status is returned.
   * <p>
   * Few programmers will have any need for this method; it is provided
   * for the benefit of the JRE itself.  (It allows a class to determine at
   * the time that it is initialized whether assertions should be enabled.)
   * Note that this method is not guaranteed to return the actual
   * assertion status that was (or will be) associated with the specified
   * class when it was (or will be) initialized.
   *
   * @return the desired assertion status of the specified class.
   * @see    java.lang.ClassLoader#setClassAssertionStatus
   * @see    java.lang.ClassLoader#setPackageAssertionStatus
   * @see    java.lang.ClassLoader#setDefaultAssertionStatus
   * @since  1.4
   */
  public boolean desiredAssertionStatus() {
    return false;
  }

  /**
   * @throws NullPointerException {@inheritDoc}
   * @since 1.5
   */
  public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
    return null;
  }

  /**
   * @since 1.5
   */
  @Override
  public Annotation[] getAnnotations() {
    return null;
  }

  /**
   * Returns the canonical name of the underlying class as
   * defined by the Java Language Specification.  Returns null if
   * the underlying class does not have a canonical name (i.e., if
   * it is a local or anonymous class or an array whose component
   * type does not have a canonical name).
   * @return the canonical name of the underlying class if it exists, and
   * {@code null} otherwise.
   * @since 1.5
   */
  public String getCanonicalName() {
  	return "";
  }

  /**
   * Returns an array containing {@code Class} objects representing all
   * the public classes and interfaces that are members of the class
   * represented by this {@code Class} object.  This includes public
   * class and interface members inherited from superclasses and public class
   * and interface members declared by the class.  This method returns an
   * array of length 0 if this {@code Class} object has no public member
   * classes or interfaces.  This method also returns an array of length 0 if
   * this {@code Class} object represents a primitive type, an array
   * class, or void.
   *
   * @return the array of {@code Class} objects representing the public
   *         members of this class
   *
   * @since JDK1.1
   */
  public Class<?>[] getClasses() {
    return null;
  }

  /**
   * Returns the class loader for the class.  Some implementations may use
   * null to represent the bootstrap class loader. This method will return
   * null in such implementations if this class was loaded by the bootstrap
   * class loader.
   *
   * <p> If a security manager is present, and the caller's class loader is
   * not null and the caller's class loader is not the same as or an ancestor of
   * the class loader for the class whose class loader is requested, then
   * this method calls the security manager's {@code checkPermission}
   * method with a {@code RuntimePermission("getClassLoader")}
   * permission to ensure it's ok to access the class loader for the class.
   *
   * <p>If this object
   * represents a primitive type or void, null is returned.
   *
   * @return  the class loader that loaded the class or interface
   *          represented by this object.
   * @throws SecurityException
   *    if a security manager exists and its
   *    {@code checkPermission} method denies
   *    access to the class loader for the class.
   * @see java.lang.ClassLoader
   * @see SecurityManager#checkPermission
   * @see java.lang.RuntimePermission
   */
  public ClassLoader getClassLoader() {
    return null;
  }

  /**
   * Returns the {@code Class} representing the component type of an
   * array.  If this class does not represent an array class this method
   * returns null.
   *
   * @return the {@code Class} representing the component type of this
   * class if this class is an array
   * @see     java.lang.reflect.Array
   * @since JDK1.1
   */
  public Class<?> getComponentType() {
    return null;
  }

  /**
   * Returns a {@code Constructor} object that reflects the specified
   * public constructor of the class represented by this {@code Class}
   * object. The {@code parameterTypes} parameter is an array of
   * {@code Class} objects that identify the constructor's formal
   * parameter types, in declared order.
   *
   * If this {@code Class} object represents an inner class
   * declared in a non-static context, the formal parameter types
   * include the explicit enclosing instance as the first parameter.
   *
   * <p> The constructor to reflect is the public constructor of the class
   * represented by this {@code Class} object whose formal parameter
   * types match those specified by {@code parameterTypes}.
   *
   * @param parameterTypes the parameter array
   * @return the {@code Constructor} object of the public constructor that
   *         matches the specified {@code parameterTypes}
   * @throws NoSuchMethodException if a matching method is not found.
   * @throws SecurityException
   *         If a security manager, <i>s</i>, is present and
   *         the caller's class loader is not the same as or an
   *         ancestor of the class loader for the current class and
   *         invocation of {@link SecurityManager#checkPackageAccess
   *         s.checkPackageAccess()} denies access to the package
   *         of this class.
   *
   * @since JDK1.1
   */
  public Constructor<T> getConstructor(Class<?>... parameterTypes)
      throws NoSuchMethodException, SecurityException {
    return null;
  }

  /**
   * Returns an array containing {@code Constructor} objects reflecting
   * all the public constructors of the class represented by this
   * {@code Class} object.  An array of length 0 is returned if the
   * class has no public constructors, or if the class is an array class, or
   * if the class reflects a primitive type or void.
   *
   * Note that while this method returns an array of {@code
   * Constructor<T>} objects (that is an array of constructors from
   * this class), the return type of this method is {@code
   * Constructor<?>[]} and <em>not</em> {@code Constructor<T>[]} as
   * might be expected.  This less informative return type is
   * necessary since after being returned from this method, the
   * array could be modified to hold {@code Constructor} objects for
   * different classes, which would violate the type guarantees of
   * {@code Constructor<T>[]}.
   *
   * @return the array of {@code Constructor} objects representing the
   *         public constructors of this class
   * @throws SecurityException
   *         If a security manager, <i>s</i>, is present and
   *         the caller's class loader is not the same as or an
   *         ancestor of the class loader for the current class and
   *         invocation of {@link SecurityManager#checkPackageAccess
   *         s.checkPackageAccess()} denies access to the package
   *         of this class.
   *
   * @since JDK1.1
   */
  public Constructor<?>[] getConstructors() throws SecurityException {
    return null;
  }

  /**
   * @since 1.5
   */
  @Override
  public Annotation[] getDeclaredAnnotations()  {
    return null;
  }

  /**
   * Returns an array of {@code Class} objects reflecting all the
   * classes and interfaces declared as members of the class represented by
   * this {@code Class} object. This includes public, protected, default
   * (package) access, and private classes and interfaces declared by the
   * class, but excludes inherited classes and interfaces.  This method
   * returns an array of length 0 if the class declares no classes or
   * interfaces as members, or if this {@code Class} object represents a
   * primitive type, an array class, or void.
   *
   * @return the array of {@code Class} objects representing all the
   *         declared members of this class
   * @throws SecurityException
   *         If a security manager, <i>s</i>, is present and any of the
   *         following conditions is met:
   *
   *         <ul>
   *
   *         <li> the caller's class loader is not the same as the
   *         class loader of this class and invocation of
   *         {@link SecurityManager#checkPermission
   *         s.checkPermission} method with
   *         {@code RuntimePermission("accessDeclaredMembers")}
   *         denies access to the declared classes within this class
   *
   *         <li> the caller's class loader is not the same as or an
   *         ancestor of the class loader for the current class and
   *         invocation of {@link SecurityManager#checkPackageAccess
   *         s.checkPackageAccess()} denies access to the package
   *         of this class
   *
   *         </ul>
   *
   * @since JDK1.1
   */
  public Class<?>[] getDeclaredClasses() {
  	return null;
  }

  /**
   * Returns a {@code Constructor} object that reflects the specified
   * constructor of the class or interface represented by this
   * {@code Class} object.  The {@code parameterTypes} parameter is
   * an array of {@code Class} objects that identify the constructor's
   * formal parameter types, in declared order.
   *
   * If this {@code Class} object represents an inner class
   * declared in a non-static context, the formal parameter types
   * include the explicit enclosing instance as the first parameter.
   *
   * @param parameterTypes the parameter array
   * @return  The {@code Constructor} object for the constructor with the
   *          specified parameter list
   * @throws  NoSuchMethodException if a matching method is not found.
   * @throws  SecurityException
   *          If a security manager, <i>s</i>, is present and any of the
   *          following conditions is met:
   *
   *          <ul>
   *
   *          <li> the caller's class loader is not the same as the
   *          class loader of this class and invocation of
   *          {@link SecurityManager#checkPermission
   *          s.checkPermission} method with
   *          {@code RuntimePermission("accessDeclaredMembers")}
   *          denies access to the declared constructor
   *
   *          <li> the caller's class loader is not the same as or an
   *          ancestor of the class loader for the current class and
   *          invocation of {@link SecurityManager#checkPackageAccess
   *          s.checkPackageAccess()} denies access to the package
   *          of this class
   *
   *          </ul>
   *
   * @since JDK1.1
   */
  public Constructor<T> getDeclaredConstructor(Class<?>... parameterTypes)
      throws NoSuchMethodException, SecurityException {
  	return null;
  }

  /**
   * Returns an array of {@code Constructor} objects reflecting all the
   * constructors declared by the class represented by this
   * {@code Class} object. These are public, protected, default
   * (package) access, and private constructors.  The elements in the array
   * returned are not sorted and are not in any particular order.  If the
   * class has a default constructor, it is included in the returned array.
   * This method returns an array of length 0 if this {@code Class}
   * object represents an interface, a primitive type, an array class, or
   * void.
   *
   * <p> See <em>The Java Language Specification</em>, section 8.2.
   *
   * @return  the array of {@code Constructor} objects representing all the
   *          declared constructors of this class
   * @throws  SecurityException
   *          If a security manager, <i>s</i>, is present and any of the
   *          following conditions is met:
   *
   *          <ul>
   *
   *          <li> the caller's class loader is not the same as the
   *          class loader of this class and invocation of
   *          {@link SecurityManager#checkPermission
   *          s.checkPermission} method with
   *          {@code RuntimePermission("accessDeclaredMembers")}
   *          denies access to the declared constructors within this class
   *
   *          <li> the caller's class loader is not the same as or an
   *          ancestor of the class loader for the current class and
   *          invocation of {@link SecurityManager#checkPackageAccess
   *          s.checkPackageAccess()} denies access to the package
   *          of this class
   *
   *          </ul>
   *
   * @since JDK1.1
   */
  public Constructor<?>[] getDeclaredConstructors() throws SecurityException {
  	return null;
  }

  /**
   * Returns a {@code Field} object that reflects the specified declared
   * field of the class or interface represented by this {@code Class}
   * object. The {@code name} parameter is a {@code String} that specifies
   * the simple name of the desired field.
   *
   * <p> If this {@code Class} object represents an array type, then this
   * method does not find the {@code length} field of the array type.
   *
   * @param name the name of the field
   * @return  the {@code Field} object for the specified field in this
   *          class
   * @throws  NoSuchFieldException if a field with the specified name is
   *          not found.
   * @throws  NullPointerException if {@code name} is {@code null}
   * @throws  SecurityException
   *          If a security manager, <i>s</i>, is present and any of the
   *          following conditions is met:
   *
   *          <ul>
   *
   *          <li> the caller's class loader is not the same as the
   *          class loader of this class and invocation of
   *          {@link SecurityManager#checkPermission
   *          s.checkPermission} method with
   *          {@code RuntimePermission("accessDeclaredMembers")}
   *          denies access to the declared field
   *
   *          <li> the caller's class loader is not the same as or an
   *          ancestor of the class loader for the current class and
   *          invocation of {@link SecurityManager#checkPackageAccess
   *          s.checkPackageAccess()} denies access to the package
   *          of this class
   *
   *          </ul>
   *
   * @since JDK1.1
   * @jls 8.2 Class Members
   * @jls 8.3 Field Declarations
   */
  public Field getDeclaredField(String name) throws NoSuchFieldException {
  	return null;
  }

  /**
   * Returns an array of {@code Field} objects reflecting all the fields
   * declared by the class or interface represented by this
   * {@code Class} object. This includes public, protected, default
   * (package) access, and private fields, but excludes inherited fields.
   *
   * <p> If this {@code Class} object represents a class or interface with no
   * declared fields, then this method returns an array of length 0.
   *
   * <p> If this {@code Class} object represents an array type, a primitive
   * type, or void, then this method returns an array of length 0.
   *
   * <p> The elements in the returned array are not sorted and are not in any
   * particular order.
   *
   * @return  the array of {@code Field} objects representing all the
   *          declared fields of this class
   * @throws  SecurityException
   *          If a security manager, <i>s</i>, is present and any of the
   *          following conditions is met:
   *
   *          <ul>
   *
   *          <li> the caller's class loader is not the same as the
   *          class loader of this class and invocation of
   *          {@link SecurityManager#checkPermission
   *          s.checkPermission} method with
   *          {@code RuntimePermission("accessDeclaredMembers")}
   *          denies access to the declared fields within this class
   *
   *          <li> the caller's class loader is not the same as or an
   *          ancestor of the class loader for the current class and
   *          invocation of {@link SecurityManager#checkPackageAccess
   *          s.checkPackageAccess()} denies access to the package
   *          of this class
   *
   *          </ul>
   *
   * @since JDK1.1
   * @jls 8.2 Class Members
   * @jls 8.3 Field Declarations
   */
  public Field[] getDeclaredFields() {
  	return null;
  }

  /**
   * Returns a {@code Method} object that reflects the specified
   * declared method of the class or interface represented by this
   * {@code Class} object. The {@code name} parameter is a
   * {@code String} that specifies the simple name of the desired
   * method, and the {@code parameterTypes} parameter is an array of
   * {@code Class} objects that identify the method's formal parameter
   * types, in declared order.  If more than one method with the same
   * parameter types is declared in a class, and one of these methods has a
   * return type that is more specific than any of the others, that method is
   * returned; otherwise one of the methods is chosen arbitrarily.  If the
   * name is "&lt;init&gt;"or "&lt;clinit&gt;" a {@code NoSuchMethodException}
   * is raised.
   *
   * <p> If this {@code Class} object represents an array type, then this
   * method does not find the {@code clone()} method.
   *
   * @param name the name of the method
   * @param parameterTypes the parameter array
   * @return  the {@code Method} object for the method of this class
   *          matching the specified name and parameters
   * @throws  NoSuchMethodException if a matching method is not found.
   * @throws  NullPointerException if {@code name} is {@code null}
   * @throws  SecurityException
   *          If a security manager, <i>s</i>, is present and any of the
   *          following conditions is met:
   *
   *          <ul>
   *
   *          <li> the caller's class loader is not the same as the
   *          class loader of this class and invocation of
   *          {@link SecurityManager#checkPermission
   *          s.checkPermission} method with
   *          {@code RuntimePermission("accessDeclaredMembers")}
   *          denies access to the declared method
   *
   *          <li> the caller's class loader is not the same as or an
   *          ancestor of the class loader for the current class and
   *          invocation of {@link SecurityManager#checkPackageAccess
   *          s.checkPackageAccess()} denies access to the package
   *          of this class
   *
   *          </ul>
   *
   * @jls 8.2 Class Members
   * @jls 8.4 Method Declarations
   * @since JDK1.1
   */
  public Method getDeclaredMethod(String name, Class<?>... parameterTypes)
      throws NoSuchMethodException, SecurityException {
  	return null;
  }

  /**
   *
   * Returns an array containing {@code Method} objects reflecting all the
   * declared methods of the class or interface represented by this {@code
   * Class} object, including public, protected, default (package)
   * access, and private methods, but excluding inherited methods.
   *
   * <p> If this {@code Class} object represents a type that has multiple
   * declared methods with the same name and parameter types, but different
   * return types, then the returned array has a {@code Method} object for
   * each such method.
   *
   * <p> If this {@code Class} object represents a type that has a class
   * initialization method {@code <clinit>}, then the returned array does
   * <em>not</em> have a corresponding {@code Method} object.
   *
   * <p> If this {@code Class} object represents a class or interface with no
   * declared methods, then the returned array has length 0.
   *
   * <p> If this {@code Class} object represents an array type, a primitive
   * type, or void, then the returned array has length 0.
   *
   * <p> The elements in the returned array are not sorted and are not in any
   * particular order.
   *
   * @return  the array of {@code Method} objects representing all the
   *          declared methods of this class
   * @throws  SecurityException
   *          If a security manager, <i>s</i>, is present and any of the
   *          following conditions is met:
   *
   *          <ul>
   *
   *          <li> the caller's class loader is not the same as the
   *          class loader of this class and invocation of
   *          {@link SecurityManager#checkPermission
   *          s.checkPermission} method with
   *          {@code RuntimePermission("accessDeclaredMembers")}
   *          denies access to the declared methods within this class
   *
   *          <li> the caller's class loader is not the same as or an
   *          ancestor of the class loader for the current class and
   *          invocation of {@link SecurityManager#checkPackageAccess
   *          s.checkPackageAccess()} denies access to the package
   *          of this class
   *
   *          </ul>
   *
   * @jls 8.2 Class Members
   * @jls 8.4 Method Declarations
   * @since JDK1.1
   */
  public Method[] getDeclaredMethods() throws SecurityException {
  	return null;
  }

  /**
   * If the class or interface represented by this {@code Class} object
   * is a member of another class, returns the {@code Class} object
   * representing the class in which it was declared.  This method returns
   * null if this class or interface is not a member of any other class.  If
   * this {@code Class} object represents an array class, a primitive
   * type, or void,then this method returns null.
   *
   * @return the declaring class for this class
   * @since JDK1.1
   */
  public Class<?> getDeclaringClass() {
    return null;
  }

  /**
   * Returns the immediately enclosing class of the underlying
   * class.  If the underlying class is a top level class this
   * method returns {@code null}.
   * @return the immediately enclosing class of the underlying class
   * @since 1.5
   */
  public Class<?> getEnclosingClass() {
    return null;
  }

  /**
   * If this {@code Class} object represents a local or anonymous
   * class within a constructor, returns a {@link
   * java.lang.reflect.Constructor Constructor} object representing
   * the immediately enclosing constructor of the underlying
   * class. Returns {@code null} otherwise.  In particular, this
   * method returns {@code null} if the underlying class is a local
   * or anonymous class immediately enclosed by a type declaration,
   * instance initializer or static initializer.
   *
   * @return the immediately enclosing constructor of the underlying class, if
   *     that class is a local or anonymous class; otherwise {@code null}.
   * @since 1.5
   */
  public Constructor<?> getEnclosingConstructor() {
    return null;
  }

  /**
   * If this {@code Class} object represents a local or anonymous
   * class within a method, returns a {@link
   * java.lang.reflect.Method Method} object representing the
   * immediately enclosing method of the underlying class. Returns
   * {@code null} otherwise.
   *
   * In particular, this method returns {@code null} if the underlying
   * class is a local or anonymous class immediately enclosed by a type
   * declaration, instance initializer or static initializer.
   *
   * @return the immediately enclosing method of the underlying class, if
   *     that class is a local or anonymous class; otherwise {@code null}.
   * @since 1.5
   */
  public Method getEnclosingMethod() {
    return null;
  }

  /**
   * Returns the elements of this enum class or null if this
   * Class object does not represent an enum type.
   *
   * @return an array containing the values comprising the enum class
   *     represented by this Class object in the order they're
   *     declared, or null if this Class object does not
   *     represent an enum type
   * @since 1.5
   */
  public T[] getEnumConstants() {
    return null;
  }

  T[] getEnumConstantsShared() {
    return null;
  }

  /**
   * Returns a {@code Field} object that reflects the specified public member
   * field of the class or interface represented by this {@code Class}
   * object. The {@code name} parameter is a {@code String} specifying the
   * simple name of the desired field.
   *
   * <p> The field to be reflected is determined by the algorithm that
   * follows.  Let C be the class or interface represented by this object:
   *
   * <OL>
   * <LI> If C declares a public field with the name specified, that is the
   *      field to be reflected.</LI>
   * <LI> If no field was found in step 1 above, this algorithm is applied
   *      recursively to each direct superinterface of C. The direct
   *      superinterfaces are searched in the order they were declared.</LI>
   * <LI> If no field was found in steps 1 and 2 above, and C has a
   *      superclass S, then this algorithm is invoked recursively upon S.
   *      If C has no superclass, then a {@code NoSuchFieldException}
   *      is thrown.</LI>
   * </OL>
   *
   * <p> If this {@code Class} object represents an array type, then this
   * method does not find the {@code length} field of the array type.
   *
   * @param name the field name
   * @return the {@code Field} object of this class specified by
   *         {@code name}
   * @throws NoSuchFieldException if a field with the specified name is
   *         not found.
   * @throws NullPointerException if {@code name} is {@code null}
   * @throws SecurityException
   *         If a security manager, <i>s</i>, is present and
   *         the caller's class loader is not the same as or an
   *         ancestor of the class loader for the current class and
   *         invocation of {@link SecurityManager#checkPackageAccess
   *         s.checkPackageAccess()} denies access to the package
   *         of this class.
   *
   * @since JDK1.1
   * @jls 8.2 Class Members
   * @jls 8.3 Field Declarations
   */
  public Field getField(String name) throws NoSuchFieldException {
    return null;
  }

  /**
   * Returns an array containing {@code Field} objects reflecting all
   * the accessible public fields of the class or interface represented by
   * this {@code Class} object.
   *
   * <p> If this {@code Class} object represents a class or interface with no
   * no accessible public fields, then this method returns an array of length
   * 0.
   *
   * <p> If this {@code Class} object represents a class, then this method
   * returns the public fields of the class and of all its superclasses.
   *
   * <p> If this {@code Class} object represents an interface, then this
   * method returns the fields of the interface and of all its
   * superinterfaces.
   *
   * <p> If this {@code Class} object represents an array type, a primitive
   * type, or void, then this method returns an array of length 0.
   *
   * <p> The elements in the returned array are not sorted and are not in any
   * particular order.
   *
   * @return the array of {@code Field} objects representing the
   *         public fields
   * @throws SecurityException
   *         If a security manager, <i>s</i>, is present and
   *         the caller's class loader is not the same as or an
   *         ancestor of the class loader for the current class and
   *         invocation of {@link SecurityManager#checkPackageAccess
   *         s.checkPackageAccess()} denies access to the package
   *         of this class.
   *
   * @since JDK1.1
   * @jls 8.2 Class Members
   * @jls 8.3 Field Declarations
   */
  public Field[] getFields() throws SecurityException {
    return null;
  }

  /**
   * Returns the {@code Type}s representing the interfaces
   * directly implemented by the class or interface represented by
   * this object.
   *
   * <p>If a superinterface is a parameterized type, the
   * {@code Type} object returned for it must accurately reflect
   * the actual type parameters used in the source code. The
   * parameterized type representing each superinterface is created
   * if it had not been created before. See the declaration of
   * {@link java.lang.reflect.ParameterizedType ParameterizedType}
   * for the semantics of the creation process for parameterized
   * types.
   *
   * <p> If this object represents a class, the return value is an
   * array containing objects representing all interfaces
   * implemented by the class. The order of the interface objects in
   * the array corresponds to the order of the interface names in
   * the {@code implements} clause of the declaration of the class
   * represented by this object.  In the case of an array class, the
   * interfaces {@code Cloneable} and {@code Serializable} are
   * returned in that order.
   *
   * <p>If this object represents an interface, the array contains
   * objects representing all interfaces directly extended by the
   * interface.  The order of the interface objects in the array
   * corresponds to the order of the interface names in the
   * {@code extends} clause of the declaration of the interface
   * represented by this object.
   *
   * <p>If this object represents a class or interface that
   * implements no interfaces, the method returns an array of length
   * 0.
   *
   * <p>If this object represents a primitive type or void, the
   * method returns an array of length 0.
   *
   * @throws java.lang.reflect.GenericSignatureFormatError
   *     if the generic class signature does not conform to the format
   *     specified in
   *     <cite>The Java&trade; Virtual Machine Specification</cite>
   * @throws TypeNotPresentException if any of the generic
   *     superinterfaces refers to a non-existent type declaration
   * @throws java.lang.reflect.MalformedParameterizedTypeException
   *     if any of the generic superinterfaces refer to a parameterized
   *     type that cannot be instantiated for any reason
   * @return an array of interfaces implemented by this class
   * @since 1.5
   */
  public Type[] getGenericInterfaces() {
  	return null;
  }

  /**
   * Returns the {@code Type} representing the direct superclass of
   * the entity (class, interface, primitive type or void) represented by
   * this {@code Class}.
   *
   * <p>If the superclass is a parameterized type, the {@code Type}
   * object returned must accurately reflect the actual type
   * parameters used in the source code. The parameterized type
   * representing the superclass is created if it had not been
   * created before. See the declaration of {@link
   * java.lang.reflect.ParameterizedType ParameterizedType} for the
   * semantics of the creation process for parameterized types.  If
   * this {@code Class} represents either the {@code Object}
   * class, an interface, a primitive type, or void, then null is
   * returned.  If this object represents an array class then the
   * {@code Class} object representing the {@code Object} class is
   * returned.
   *
   * @throws java.lang.reflect.GenericSignatureFormatError if the generic
   *     class signature does not conform to the format specified in
   *     <cite>The Java&trade; Virtual Machine Specification</cite>
   * @throws TypeNotPresentException if the generic superclass
   *     refers to a non-existent type declaration
   * @throws java.lang.reflect.MalformedParameterizedTypeException if the
   *     generic superclass refers to a parameterized type that cannot be
   *     instantiated  for any reason
   * @return the superclass of the class represented by this object
   * @since 1.5
   */
  public Type getGenericSuperclass() {
  	return null;
  }

  /**
   * Determines the interfaces implemented by the class or interface
   * represented by this object.
   *
   * <p> If this object represents a class, the return value is an array
   * containing objects representing all interfaces implemented by the
   * class. The order of the interface objects in the array corresponds to
   * the order of the interface names in the {@code implements} clause
   * of the declaration of the class represented by this object. For
   * example, given the declaration:
   * <blockquote>
   * {@code class Shimmer implements FloorWax, DessertTopping { ... }}
   * </blockquote>
   * suppose the value of {@code s} is an instance of
   * {@code Shimmer}; the value of the expression:
   * <blockquote>
   * {@code s.getClass().getInterfaces()[0]}
   * </blockquote>
   * is the {@code Class} object that represents interface
   * {@code FloorWax}; and the value of:
   * <blockquote>
   * {@code s.getClass().getInterfaces()[1]}
   * </blockquote>
   * is the {@code Class} object that represents interface
   * {@code DessertTopping}.
   *
   * <p> If this object represents an interface, the array contains objects
   * representing all interfaces extended by the interface. The order of the
   * interface objects in the array corresponds to the order of the interface
   * names in the {@code extends} clause of the declaration of the
   * interface represented by this object.
   *
   * <p> If this object represents a class or interface that implements no
   * interfaces, the method returns an array of length 0.
   *
   * <p> If this object represents a primitive type or void, the method
   * returns an array of length 0.
   *
   * <p> If this {@code Class} object represents an array type, the
   * interfaces {@code Cloneable} and {@code java.io.Serializable} are
   * returned in that order.
   *
   * @return an array of interfaces implemented by this class.
   */
  public Class<?>[] getInterfaces() {
    return null;
  }

  /**
   * Returns a {@code Method} object that reflects the specified public
   * member method of the class or interface represented by this
   * {@code Class} object. The {@code name} parameter is a
   * {@code String} specifying the simple name of the desired method. The
   * {@code parameterTypes} parameter is an array of {@code Class}
   * objects that identify the method's formal parameter types, in declared
   * order. If {@code parameterTypes} is {@code null}, it is
   * treated as if it were an empty array.
   *
   * <p> If the {@code name} is "{@code <init>}" or "{@code <clinit>}" a
   * {@code NoSuchMethodException} is raised. Otherwise, the method to
   * be reflected is determined by the algorithm that follows.  Let C be the
   * class or interface represented by this object:
   * <OL>
   * <LI> C is searched for a <I>matching method</I>, as defined below. If a
   *      matching method is found, it is reflected.</LI>
   * <LI> If no matching method is found by step 1 then:
   *   <OL TYPE="a">
   *   <LI> If C is a class other than {@code Object}, then this algorithm is
   *        invoked recursively on the superclass of C.</LI>
   *   <LI> If C is the class {@code Object}, or if C is an interface, then
   *        the superinterfaces of C (if any) are searched for a matching
   *        method. If any such method is found, it is reflected.</LI>
   *   </OL></LI>
   * </OL>
   *
   * <p> To find a matching method in a class or interface C:&nbsp; If C
   * declares exactly one public method with the specified name and exactly
   * the same formal parameter types, that is the method reflected. If more
   * than one such method is found in C, and one of these methods has a
   * return type that is more specific than any of the others, that method is
   * reflected; otherwise one of the methods is chosen arbitrarily.
   *
   * <p>Note that there may be more than one matching method in a
   * class because while the Java language forbids a class to
   * declare multiple methods with the same signature but different
   * return types, the Java virtual machine does not.  This
   * increased flexibility in the virtual machine can be used to
   * implement various language features.  For example, covariant
   * returns can be implemented with {@linkplain
   * java.lang.reflect.Method#isBridge bridge methods}; the bridge
   * method and the method being overridden would have the same
   * signature but different return types.
   *
   * <p> If this {@code Class} object represents an array type, then this
   * method does not find the {@code clone()} method.
   *
   * <p> Static methods declared in superinterfaces of the class or interface
   * represented by this {@code Class} object are not considered members of
   * the class or interface.
   *
   * @param name the name of the method
   * @param parameterTypes the list of parameters
   * @return the {@code Method} object that matches the specified
   *         {@code name} and {@code parameterTypes}
   * @throws NoSuchMethodException if a matching method is not found
   *         or if the name is "&lt;init&gt;"or "&lt;clinit&gt;".
   * @throws NullPointerException if {@code name} is {@code null}
   * @throws SecurityException
   *         If a security manager, <i>s</i>, is present and
   *         the caller's class loader is not the same as or an
   *         ancestor of the class loader for the current class and
   *         invocation of {@link SecurityManager#checkPackageAccess
   *         s.checkPackageAccess()} denies access to the package
   *         of this class.
   *
   * @jls 8.2 Class Members
   * @jls 8.4 Method Declarations
   * @since JDK1.1
   */
  public Method getMethod(String name, Class<?>... parameterTypes)
      throws NoSuchMethodException, SecurityException {
  	return null;
  }

  /**
   * Returns an array containing {@code Method} objects reflecting all the
   * public methods of the class or interface represented by this {@code
   * Class} object, including those declared by the class or interface and
   * those inherited from superclasses and superinterfaces.
   *
   * <p> If this {@code Class} object represents a type that has multiple
   * public methods with the same name and parameter types, but different
   * return types, then the returned array has a {@code Method} object for
   * each such method.
   *
   * <p> If this {@code Class} object represents a type with a class
   * initialization method {@code <clinit>}, then the returned array does
   * <em>not</em> have a corresponding {@code Method} object.
   *
   * <p> If this {@code Class} object represents an array type, then the
   * returned array has a {@code Method} object for each of the public
   * methods inherited by the array type from {@code Object}. It does not
   * contain a {@code Method} object for {@code clone()}.
   *
   * <p> If this {@code Class} object represents an interface then the
   * returned array does not contain any implicitly declared methods from
   * {@code Object}. Therefore, if no methods are explicitly declared in
   * this interface or any of its superinterfaces then the returned array
   * has length 0. (Note that a {@code Class} object which represents a class
   * always has public methods, inherited from {@code Object}.)
   *
   * <p> If this {@code Class} object represents a primitive type or void,
   * then the returned array has length 0.
   *
   * <p> Static methods declared in superinterfaces of the class or interface
   * represented by this {@code Class} object are not considered members of
   * the class or interface.
   *
   * <p> The elements in the returned array are not sorted and are not in any
   * particular order.
   *
   * @return the array of {@code Method} objects representing the
   *         public methods of this class
   * @throws SecurityException
   *         If a security manager, <i>s</i>, is present and
   *         the caller's class loader is not the same as or an
   *         ancestor of the class loader for the current class and
   *         invocation of {@link SecurityManager#checkPackageAccess
   *         s.checkPackageAccess()} denies access to the package
   *         of this class.
   *
   * @jls 8.2 Class Members
   * @jls 8.4 Method Declarations
   * @since JDK1.1
   */
  public Method[] getMethods() throws SecurityException {
    return null;
  }

  /**
   * Returns the Java language modifiers for this class or interface, encoded
   * in an integer. The modifiers consist of the Java Virtual Machine's
   * constants for {@code public}, {@code protected},
   * {@code private}, {@code final}, {@code static},
   * {@code abstract} and {@code interface}; they should be decoded
   * using the methods of class {@code Modifier}.
   *
   * <p> If the underlying class is an array class, then its
   * {@code public}, {@code private} and {@code protected}
   * modifiers are the same as those of its component type.  If this
   * {@code Class} represents a primitive type or void, its
   * {@code public} modifier is always {@code true}, and its
   * {@code protected} and {@code private} modifiers are always
   * {@code false}. If this object represents an array class, a
   * primitive type or void, then its {@code final} modifier is always
   * {@code true} and its interface modifier is always
   * {@code false}. The values of its other modifiers are not determined
   * by this specification.
   *
   * <p> The modifier encodings are defined in <em>The Java Virtual Machine
   * Specification</em>, table 4.1.
   *
   * @return the {@code int} representing the modifiers for this class
   * @see     java.lang.reflect.Modifier
   * @since JDK1.1
   */
  public int getModifiers() {
    return 0;
  }

  /**
   * Returns the  name of the entity (class, interface, array class,
   * primitive type, or void) represented by this {@code Class} object,
   * as a {@code String}.
   *
   * <p> If this class object represents a reference type that is not an
   * array type then the binary name of the class is returned, as specified
   * by
   * <cite>The Java&trade; Language Specification</cite>.
   *
   * <p> If this class object represents a primitive type or void, then the
   * name returned is a {@code String} equal to the Java language
   * keyword corresponding to the primitive type or void.
   *
   * <p> If this class object represents a class of arrays, then the internal
   * form of the name consists of the name of the element type preceded by
   * one or more '{@code [}' characters representing the depth of the array
   * nesting.  The encoding of element type names is as follows:
   *
   * <blockquote><table summary="Element types and encodings">
   * <tr><th> Element Type <th> &nbsp;&nbsp;&nbsp; <th> Encoding
   * <tr><td> boolean      <td> &nbsp;&nbsp;&nbsp; <td align=center> Z
   * <tr><td> byte         <td> &nbsp;&nbsp;&nbsp; <td align=center> B
   * <tr><td> char         <td> &nbsp;&nbsp;&nbsp; <td align=center> C
   * <tr><td> class or interface
   *                       <td> &nbsp;&nbsp;&nbsp; <td align=center> L<i>classname</i>;
   * <tr><td> double       <td> &nbsp;&nbsp;&nbsp; <td align=center> D
   * <tr><td> float        <td> &nbsp;&nbsp;&nbsp; <td align=center> F
   * <tr><td> int          <td> &nbsp;&nbsp;&nbsp; <td align=center> I
   * <tr><td> long         <td> &nbsp;&nbsp;&nbsp; <td align=center> J
   * <tr><td> short        <td> &nbsp;&nbsp;&nbsp; <td align=center> S
   * </table></blockquote>
   *
   * <p> The class or interface name <i>classname</i> is the binary name of
   * the class specified above.
   *
   * <p> Examples:
   * <blockquote><pre>
   * String.class.getName()
   *     returns "java.lang.String"
   * byte.class.getName()
   *     returns "byte"
   * (new Object[3]).getClass().getName()
   *     returns "[Ljava.lang.Object;"
   * (new int[3][4][5][6][7][8][9]).getClass().getName()
   *     returns "[[[[[[[I"
   * </pre></blockquote>
   *
   * @return  the name of the class or interface
   *          represented by this object.
   */
  public String getName() {
  	return "";
  }

  /**
   * Gets the package for this class.  The class loader of this class is used
   * to find the package.  If the class was loaded by the bootstrap class
   * loader the set of packages loaded from CLASSPATH is searched to find the
   * package of the class. Null is returned if no package object was created
   * by the class loader of this class.
   *
   * <p> Packages have attributes for versions and specifications only if the
   * information was defined in the manifests that accompany the classes, and
   * if the class loader created the package instance with the attributes
   * from the manifest.
   *
   * @return the package of the class, or null if no package
   *         information is available from the archive or codebase.
   */
  public Package getPackage() {
    return null;
  }

  /**
   * Returns the fully qualified package name.
   *
   * <p> If this class is a top level class, then this method returns the fully
   * qualified name of the package that the class is a member of, or the
   * empty string if the class is in an unnamed package.
   *
   * <p> If this class is a member class, then this method is equivalent to
   * invoking {@code getPackageName()} on the {@linkplain #getEnclosingClass
   * enclosing class}.
   *
   * <p> If this class is a {@linkplain #isLocalClass local class} or an {@linkplain
   * #isAnonymousClass() anonymous class}, then this method is equivalent to
   * invoking {@code getPackageName()} on the {@linkplain #getDeclaringClass
   * declaring class} of the {@linkplain #getEnclosingMethod enclosing method} or
   * {@linkplain #getEnclosingConstructor enclosing constructor}.
   *
   * <p> If this class represents an array type then this method returns the
   * package name of the element type. If this class represents a primitive
   * type or void then the package name "{@code java.lang}" is returned.
   *
   * @return the fully qualified package name
   *
   * @since 9
   * @spec JPMS
   * @jls 6.7  Fully Qualified Names
   */
  public String getPackageName() {
    return null;
  }

  /**
   * Returns the {@code ProtectionDomain} of this class.  If there is a
   * security manager installed, this method first calls the security
   * manager's {@code checkPermission} method with a
   * {@code RuntimePermission("getProtectionDomain")} permission to
   * ensure it's ok to get the
   * {@code ProtectionDomain}.
   *
   * @return the ProtectionDomain of this class
   *
   * @throws SecurityException
   *        if a security manager exists and its
   *        {@code checkPermission} method doesn't allow
   *        getting the ProtectionDomain.
   *
   * @see java.security.ProtectionDomain
   * @see SecurityManager#checkPermission
   * @see java.lang.RuntimePermission
   * @since 1.2
   */
  public ProtectionDomain getProtectionDomain() {
    return null;
  }

  /**
   * Finds a resource with a given name.  The rules for searching resources
   * associated with a given class are implemented by the defining
   * {@linkplain ClassLoader class loader} of the class.  This method
   * delegates to this object's class loader.  If this object was loaded by
   * the bootstrap class loader, the method delegates to {@link
   * ClassLoader#getSystemResource}.
   *
   * <p> Before delegation, an absolute resource name is constructed from the
   * given resource name using this algorithm:
   *
   * <ul>
   *
   * <li> If the {@code name} begins with a {@code '/'}
   * (<tt>'&#92;u002f'</tt>), then the absolute name of the resource is the
   * portion of the {@code name} following the {@code '/'}.
   *
   * <li> Otherwise, the absolute name is of the following form:
   *
   * <blockquote>
   *   {@code modified_package_name/name}
   * </blockquote>
   *
   * <p> Where the {@code modified_package_name} is the package name of this
   * object with {@code '/'} substituted for {@code '.'}
   * (<tt>'&#92;u002e'</tt>).
   *
   * </ul>
   *
   * @param  name name of the desired resource
   * @return      A  {@link java.net.URL} object or {@code null} if no
   *              resource with this name is found
   * @since  JDK1.1
   */
  public URL getResource(String name) {
    return null;
  }

  /**
   * Finds a resource with a given name.  The rules for searching resources
   * associated with a given class are implemented by the defining
   * {@linkplain ClassLoader class loader} of the class.  This method
   * delegates to this object's class loader.  If this object was loaded by
   * the bootstrap class loader, the method delegates to {@link
   * ClassLoader#getSystemResourceAsStream}.
   *
   * <p> Before delegation, an absolute resource name is constructed from the
   * given resource name using this algorithm:
   *
   * <ul>
   *
   * <li> If the {@code name} begins with a {@code '/'}
   * (<tt>'&#92;u002f'</tt>), then the absolute name of the resource is the
   * portion of the {@code name} following the {@code '/'}.
   *
   * <li> Otherwise, the absolute name is of the following form:
   *
   * <blockquote>
   *   {@code modified_package_name/name}
   * </blockquote>
   *
   * <p> Where the {@code modified_package_name} is the package name of this
   * object with {@code '/'} substituted for {@code '.'}
   * (<tt>'&#92;u002e'</tt>).
   *
   * </ul>
   *
   * @param  name name of the desired resource
   * @return      A {@link java.io.InputStream} object or {@code null} if
   *              no resource with this name is found
   * @throws  NullPointerException If {@code name} is {@code null}
   * @since  JDK1.1
   */
  public InputStream getResourceAsStream(String name) {
    return null;
  }

  /**
   * Gets the signers of this class.
   *
   * @return  the signers of this class, or null if there are no signers.  In
   *          particular, this method returns null if this object represents
   *          a primitive type or void.
   * @since   JDK1.1
   */
  public Object[] getSigners() {
    return null;
  }

  /**
   * Returns the simple name of the underlying class as given in the
   * source code. Returns an empty string if the underlying class is
   * anonymous.
   *
   * <p>The simple name of an array is the simple name of the
   * component type with "[]" appended.  In particular the simple
   * name of an array whose component type is anonymous is "[]".
   *
   * @return the simple name of the underlying class
   * @since 1.5
   */
  public String getSimpleName() {
  	return "";
  }

  /**
   * Returns the {@code Class} representing the superclass of the entity
   * (class, interface, primitive type or void) represented by this
   * {@code Class}.  If this {@code Class} represents either the
   * {@code Object} class, an interface, a primitive type, or void, then
   * null is returned.  If this object represents an array class then the
   * {@code Class} object representing the {@code Object} class is
   * returned.
   *
   * @return the superclass of the class represented by this object.
   */
  public Class<? super T> getSuperclass() {
    return null;
  }

  /**
   * Returns an array of {@code TypeVariable} objects that represent the
   * type variables declared by the generic declaration represented by this
   * {@code GenericDeclaration} object, in declaration order.  Returns an
   * array of length 0 if the underlying generic declaration declares no type
   * variables.
   *
   * @return an array of {@code TypeVariable} objects that represent
   *     the type variables declared by this generic declaration
   * @throws java.lang.reflect.GenericSignatureFormatError if the generic
   *     signature of this generic declaration does not conform to
   *     the format specified in
   *     <cite>The Java&trade; Virtual Machine Specification</cite>
   * @since 1.5
   */
  public TypeVariable<Class<T>>[] getTypeParameters() {
	return null;
  }

  /**
   * Returns true if this {@code Class} object represents an annotation
   * type.  Note that if this method returns true, {@link #isInterface()}
   * would also return true, as all annotation types are also interfaces.
   *
   * @return {@code true} if this class object represents an annotation
   *      type; {@code false} otherwise
   * @since 1.5
   */
  public boolean isAnnotation() {
  	return false;
  }

  /**
   * {@inheritDoc}
   * @throws NullPointerException {@inheritDoc}
   * @since 1.5
   */
  @Override
  public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
    return false;
  }

  /**
   * Returns {@code true} if and only if the underlying class
   * is an anonymous class.
   *
   * @return {@code true} if and only if this class is an anonymous class.
   * @since 1.5
   */
  public boolean isAnonymousClass() {
  	return false;
  }

  /**
   * Determines if this {@code Class} object represents an array class.
   *
   * @return  {@code true} if this object represents an array class;
   *          {@code false} otherwise.
   * @since   JDK1.1
   */
  public boolean isArray() {
    return false;
  }

  /**
   * Determines if the class or interface represented by this
   * {@code Class} object is either the same as, or is a superclass or
   * superinterface of, the class or interface represented by the specified
   * {@code Class} parameter. It returns {@code true} if so;
   * otherwise it returns {@code false}. If this {@code Class}
   * object represents a primitive type, this method returns
   * {@code true} if the specified {@code Class} parameter is
   * exactly this {@code Class} object; otherwise it returns
   * {@code false}.
   *
   * <p> Specifically, this method tests whether the type represented by the
   * specified {@code Class} parameter can be converted to the type
   * represented by this {@code Class} object via an identity conversion
   * or via a widening reference conversion. See <em>The Java Language
   * Specification</em>, sections 5.1.1 and 5.1.4 , for details.
   *
   * @param cls the {@code Class} object to be checked
   * @return the {@code boolean} value indicating whether objects of the
   * type {@code cls} can be assigned to objects of this class
   * @exception NullPointerException if the specified Class parameter is
   *            null.
   * @since JDK1.1
   */
  public boolean isAssignableFrom(Class<?> cls) {
    return false;
  }

  /**
   * Returns true if and only if this class was declared as an enum in the
   * source code.
   *
   * @return true if and only if this class was declared as an enum in the
   *     source code
   * @since 1.5
   */
  public boolean isEnum() {
  	return false;
  }

  /**
   * Determines if the specified {@code Object} is assignment-compatible
   * with the object represented by this {@code Class}.  This method is
   * the dynamic equivalent of the Java language {@code instanceof}
   * operator. The method returns {@code true} if the specified
   * {@code Object} argument is non-null and can be cast to the
   * reference type represented by this {@code Class} object without
   * raising a {@code ClassCastException.} It returns {@code false}
   * otherwise.
   *
   * <p> Specifically, if this {@code Class} object represents a
   * declared class, this method returns {@code true} if the specified
   * {@code Object} argument is an instance of the represented class (or
   * of any of its subclasses); it returns {@code false} otherwise. If
   * this {@code Class} object represents an array class, this method
   * returns {@code true} if the specified {@code Object} argument
   * can be converted to an object of the array class by an identity
   * conversion or by a widening reference conversion; it returns
   * {@code false} otherwise. If this {@code Class} object
   * represents an interface, this method returns {@code true} if the
   * class or any superclass of the specified {@code Object} argument
   * implements this interface; it returns {@code false} otherwise. If
   * this {@code Class} object represents a primitive type, this method
   * returns {@code false}.
   *
   * @param   obj the object to check
   * @return  true if {@code obj} is an instance of this class
   *
   * @since JDK1.1
   */
  public boolean isInstance(Object obj) {
    return false;
  }

  /**
   * Determines if the specified {@code Class} object represents an
   * interface type.
   *
   * @return  {@code true} if this object represents an interface;
   *          {@code false} otherwise.
   */
  public boolean isInterface() {
    return false;
  }

  /**
   * Returns {@code true} if and only if the underlying class
   * is a local class.
   *
   * @return {@code true} if and only if this class is a local class.
   * @since 1.5
   */
  public boolean isLocalClass() {
  	return false;
  }

  /**
   * Returns {@code true} if and only if the underlying class
   * is a member class.
   *
   * @return {@code true} if and only if this class is a member class.
   * @since 1.5
   */
  public boolean isMemberClass() {
  	return false;
  }

  /**
   * Determines if the specified {@code Class} object represents a
   * primitive type.
   *
   * <p> There are nine predefined {@code Class} objects to represent
   * the eight primitive types and void.  These are created by the Java
   * Virtual Machine, and have the same names as the primitive types that
   * they represent, namely {@code boolean}, {@code byte},
   * {@code char}, {@code short}, {@code int},
   * {@code long}, {@code float}, and {@code double}.
   *
   * <p> These objects may only be accessed via the following public static
   * final variables, and are the only {@code Class} objects for which
   * this method returns {@code true}.
   *
   * @return true if and only if this class represents a primitive type
   *
   * @see     java.lang.Boolean#TYPE
   * @see     java.lang.Character#TYPE
   * @see     java.lang.Byte#TYPE
   * @see     java.lang.Short#TYPE
   * @see     java.lang.Integer#TYPE
   * @see     java.lang.Long#TYPE
   * @see     java.lang.Float#TYPE
   * @see     java.lang.Double#TYPE
   * @see     java.lang.Void#TYPE
   * @since JDK1.1
   */
  public boolean isPrimitive() {
    return false;
  }

  /**
   * Returns {@code true} if this class is a synthetic class;
   * returns {@code false} otherwise.
   * @return {@code true} if and only if this class is a synthetic class as
   *         defined by the Java Language Specification.
   * @jls 13.1 The Form of a Binary
   * @since 1.5
   */
  public boolean isSynthetic() {
  	return false;
  }

  /**
   * Creates a new instance of the class represented by this {@code Class}
   * object.  The class is instantiated as if by a {@code new}
   * expression with an empty argument list.  The class is initialized if it
   * has not already been initialized.
   *
   * <p>Note that this method propagates any exception thrown by the
   * nullary constructor, including a checked exception.  Use of
   * this method effectively bypasses the compile-time exception
   * checking that would otherwise be performed by the compiler.
   * The {@link
   * java.lang.reflect.Constructor#newInstance(java.lang.Object...)
   * Constructor.newInstance} method avoids this problem by wrapping
   * any exception thrown by the constructor in a (checked) {@link
   * java.lang.reflect.InvocationTargetException}.
   *
   * @return  a newly allocated instance of the class represented by this
   *          object.
   * @throws  IllegalAccessException  if the class or its nullary
   *          constructor is not accessible.
   * @throws  InstantiationException
   *          if this {@code Class} represents an abstract class,
   *          an interface, an array class, a primitive type, or void;
   *          or if the class has no nullary constructor;
   *          or if the instantiation fails for some other reason.
   * @throws  ExceptionInInitializerError if the initialization
   *          provoked by this method fails.
   * @throws  SecurityException
   *          If a security manager, <i>s</i>, is present and
   *          the caller's class loader is not the same as or an
   *          ancestor of the class loader for the current class and
   *          invocation of {@link SecurityManager#checkPackageAccess
   *          s.checkPackageAccess()} denies access to the package
   *          of this class.
   */
  public T newInstance() throws InstantiationException, IllegalAccessException {
  	return null;
  }

  /**
   * Converts the object to a string. The string representation is the
   * string "class" or "interface", followed by a space, and then by the
   * fully qualified name of the class in the format returned by
   * {@code getName}.  If this {@code Class} object represents a
   * primitive type, this method returns the name of the primitive type.  If
   * this {@code Class} object represents void this method returns
   * "void".
   *
   * @return a string representation of this class object.
   */
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
   * @throws NullPointerException {@inheritDoc}
   * @since 1.8
   */
  @Override
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
    return null;
  }

  /**
   * @throws NullPointerException {@inheritDoc}
   * @since 1.8
   */
  @Override
  public <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass) {
    return null;
  }

  /**
   * Return an informative string for the name of this type.
   *
   * @return an informative string for the name of this type
   * @since 1.8
   */
  public String getTypeName() {
    return null;
  }

  /**
   * Returns a string describing this {@code Class}, including
   * information about modifiers and type parameters.
   *
   * The string is formatted as a list of type modifiers, if any,
   * followed by the kind of type (empty string for primitive types
   * and {@code class}, {@code enum}, {@code interface}, or
   * <code>&#64;</code>{@code interface}, as appropriate), followed
   * by the type's name, followed by an angle-bracketed
   * comma-separated list of the type's type parameters, if any.
   *
   * A space is used to separate modifiers from one another and to
   * separate any modifiers from the kind of type. The modifiers
   * occur in canonical order. If there are no type parameters, the
   * type parameter list is elided.
   *
   * <p>Note that since information about the runtime representation
   * of a type is being generated, modifiers not present on the
   * originating source code or illegal on the originating source
   * code may be present.
   *
   * @return a string describing this {@code Class}, including
   * information about modifiers and type parameters
   *
   * @since 1.8
   */
  public String toGenericString() {
    return null;
  }
}
