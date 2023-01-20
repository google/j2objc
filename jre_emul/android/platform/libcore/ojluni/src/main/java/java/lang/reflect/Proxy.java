/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1999, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang.reflect;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import sun.reflect.CallerSensitive;

/*-[
#include "IOSClass.h"
#include "IOSPrimitiveClass.h"
#include "IOSProxyClass.h"
#include "IOSReflection.h"
#include "java/lang/IllegalArgumentException.h"
#include "java/lang/System.h"
#include "java/lang/reflect/Method.h"
#include <objc/runtime.h>

// ProxyMethod is used to represent Object's equals(), hashCode() and toString()
// methods as proxied methods with the same name as the original.
@interface ProxyMethod : JavaLangReflectMethod {
  NSString *originalName_;
}

- (instancetype)initWithMethod:(JavaLangReflectMethod *)method
                  originalName:(NSString *)originalName;
@end

@implementation ProxyMethod
- (instancetype)initWithMethod:(JavaLangReflectMethod *)method
                  originalName:(NSString *)originalName {
  if ((self = [super initWithDeclaringClass:method->class_
                                   metadata:method->metadata_])) {
    originalName_ = RETAIN_(originalName);
  }
  return self;
}

// Returns original method name.
- (NSString *)getName {
  return originalName_;
}

#if !__has_feature(objc_arc)
- (void)dealloc {
  RELEASE_(originalName_);
  [super dealloc];
}
#endif
@end
]-*/

/**
 * {@code Proxy} provides static methods for creating dynamic proxy
 * classes and instances, and it is also the superclass of all
 * dynamic proxy classes created by those methods.
 *
 * <p>To create a proxy for some interface {@code Foo}:
 * <pre>
 *     InvocationHandler handler = new MyInvocationHandler(...);
 *     Class proxyClass = Proxy.getProxyClass(
 *         Foo.class.getClassLoader(), new Class[] { Foo.class });
 *     Foo f = (Foo) proxyClass.
 *         getConstructor(new Class[] { InvocationHandler.class }).
 *         newInstance(new Object[] { handler });
 * </pre>
 * or more simply:
 * <pre>
 *     Foo f = (Foo) Proxy.newProxyInstance(Foo.class.getClassLoader(),
 *                                          new Class[] { Foo.class },
 *                                          handler);
 * </pre>
 *
 * <p>A <i>dynamic proxy class</i> (simply referred to as a <i>proxy
 * class</i> below) is a class that implements a list of interfaces
 * specified at runtime when the class is created, with behavior as
 * described below.
 *
 * A <i>proxy interface</i> is such an interface that is implemented
 * by a proxy class.
 *
 * A <i>proxy instance</i> is an instance of a proxy class.
 *
 * Each proxy instance has an associated <i>invocation handler</i>
 * object, which implements the interface {@link InvocationHandler}.
 * A method invocation on a proxy instance through one of its proxy
 * interfaces will be dispatched to the {@link InvocationHandler#invoke
 * invoke} method of the instance's invocation handler, passing the proxy
 * instance, a {@code java.lang.reflect.Method} object identifying
 * the method that was invoked, and an array of type {@code Object}
 * containing the arguments.  The invocation handler processes the
 * encoded method invocation as appropriate and the result that it
 * returns will be returned as the result of the method invocation on
 * the proxy instance.
 *
 * <p>A proxy class has the following properties:
 *
 * <ul>
 * <li>Proxy classes are public, final, and not abstract.
 *
 * <li>The unqualified name of a proxy class is unspecified.  The space
 * of class names that begin with the string {@code "$Proxy"}
 * should be, however, reserved for proxy classes.
 *
 * <li>A proxy class extends {@code java.lang.reflect.Proxy}.
 *
 * <li>A proxy class implements exactly the interfaces specified at its
 * creation, in the same order.
 *
 * <li>If a proxy class implements a non-public interface, then it will
 * be defined in the same package as that interface.  Otherwise, the
 * package of a proxy class is also unspecified.  Note that package
 * sealing will not prevent a proxy class from being successfully defined
 * in a particular package at runtime, and neither will classes already
 * defined by the same class loader and the same package with particular
 * signers.
 *
 * <li>Since a proxy class implements all of the interfaces specified at
 * its creation, invoking {@code getInterfaces} on its
 * {@code Class} object will return an array containing the same
 * list of interfaces (in the order specified at its creation), invoking
 * {@code getMethods} on its {@code Class} object will return
 * an array of {@code Method} objects that include all of the
 * methods in those interfaces, and invoking {@code getMethod} will
 * find methods in the proxy interfaces as would be expected.
 *
 * <li>The {@link Proxy#isProxyClass Proxy.isProxyClass} method will
 * return true if it is passed a proxy class-- a class returned by
 * {@code Proxy.getProxyClass} or the class of an object returned by
 * {@code Proxy.newProxyInstance}-- and false otherwise.
 *
 * <li>The {@code java.security.ProtectionDomain} of a proxy class
 * is the same as that of system classes loaded by the bootstrap class
 * loader, such as {@code java.lang.Object}, because the code for a
 * proxy class is generated by trusted system code.  This protection
 * domain will typically be granted
 * {@code java.security.AllPermission}.
 *
 * <li>Each proxy class has one public constructor that takes one argument,
 * an implementation of the interface {@link InvocationHandler}, to set
 * the invocation handler for a proxy instance.  Rather than having to use
 * the reflection API to access the public constructor, a proxy instance
 * can be also be created by calling the {@link Proxy#newProxyInstance
 * Proxy.newProxyInstance} method, which combines the actions of calling
 * {@link Proxy#getProxyClass Proxy.getProxyClass} with invoking the
 * constructor with an invocation handler.
 * </ul>
 *
 * <p>A proxy instance has the following properties:
 *
 * <ul>
 * <li>Given a proxy instance {@code proxy} and one of the
 * interfaces implemented by its proxy class {@code Foo}, the
 * following expression will return true:
 * <pre>
 *     {@code proxy instanceof Foo}
 * </pre>
 * and the following cast operation will succeed (rather than throwing
 * a {@code ClassCastException}):
 * <pre>
 *     {@code (Foo) proxy}
 * </pre>
 *
 * <li>Each proxy instance has an associated invocation handler, the one
 * that was passed to its constructor.  The static
 * {@link Proxy#getInvocationHandler Proxy.getInvocationHandler} method
 * will return the invocation handler associated with the proxy instance
 * passed as its argument.
 *
 * <li>An interface method invocation on a proxy instance will be
 * encoded and dispatched to the invocation handler's {@link
 * InvocationHandler#invoke invoke} method as described in the
 * documentation for that method.
 *
 * <li>An invocation of the {@code hashCode},
 * {@code equals}, or {@code toString} methods declared in
 * {@code java.lang.Object} on a proxy instance will be encoded and
 * dispatched to the invocation handler's {@code invoke} method in
 * the same manner as interface method invocations are encoded and
 * dispatched, as described above.  The declaring class of the
 * {@code Method} object passed to {@code invoke} will be
 * {@code java.lang.Object}.  Other public methods of a proxy
 * instance inherited from {@code java.lang.Object} are not
 * overridden by a proxy class, so invocations of those methods behave
 * like they do for instances of {@code java.lang.Object}.
 * </ul>
 *
 * <h3>Methods Duplicated in Multiple Proxy Interfaces</h3>
 *
 * <p>When two or more interfaces of a proxy class contain a method with
 * the same name and parameter signature, the order of the proxy class's
 * interfaces becomes significant.  When such a <i>duplicate method</i>
 * is invoked on a proxy instance, the {@code Method} object passed
 * to the invocation handler will not necessarily be the one whose
 * declaring class is assignable from the reference type of the interface
 * that the proxy's method was invoked through.  This limitation exists
 * because the corresponding method implementation in the generated proxy
 * class cannot determine which interface it was invoked through.
 * Therefore, when a duplicate method is invoked on a proxy instance,
 * the {@code Method} object for the method in the foremost interface
 * that contains the method (either directly or inherited through a
 * superinterface) in the proxy class's list of interfaces is passed to
 * the invocation handler's {@code invoke} method, regardless of the
 * reference type through which the method invocation occurred.
 *
 * <p>If a proxy interface contains a method with the same name and
 * parameter signature as the {@code hashCode}, {@code equals},
 * or {@code toString} methods of {@code java.lang.Object},
 * when such a method is invoked on a proxy instance, the
 * {@code Method} object passed to the invocation handler will have
 * {@code java.lang.Object} as its declaring class.  In other words,
 * the public, non-final methods of {@code java.lang.Object}
 * logically precede all of the proxy interfaces for the determination of
 * which {@code Method} object to pass to the invocation handler.
 *
 * <p>Note also that when a duplicate method is dispatched to an
 * invocation handler, the {@code invoke} method may only throw
 * checked exception types that are assignable to one of the exception
 * types in the {@code throws} clause of the method in <i>all</i> of
 * the proxy interfaces that it can be invoked through.  If the
 * {@code invoke} method throws a checked exception that is not
 * assignable to any of the exception types declared by the method in one
 * of the proxy interfaces that it can be invoked through, then an
 * unchecked {@code UndeclaredThrowableException} will be thrown by
 * the invocation on the proxy instance.  This restriction means that not
 * all of the exception types returned by invoking
 * {@code getExceptionTypes} on the {@code Method} object
 * passed to the {@code invoke} method can necessarily be thrown
 * successfully by the {@code invoke} method.
 *
 * @author      Peter Jones
 * @see         InvocationHandler
 * @since       1.3
 */
public class Proxy implements java.io.Serializable {

    private static final long serialVersionUID = -2222568056686623797L;

    /** prefix for all proxy class names */
    private final static String proxyClassNamePrefix = "$Proxy";

    /** parameter types of a proxy class constructor */
    private final static Class<?>[] constructorParams =
        { InvocationHandler.class };

    /** maps a class loader to the proxy class cache for that loader */
    private static Map<ClassLoader, Map<List<String>, Object>> loaderToCache
        = new WeakHashMap<>();

    /** marks that a particular proxy class is currently being generated */
    private static Object pendingGenerationMarker = new Object();

    /** next number to use for generation of unique proxy class names */
    private static long nextUniqueNumber = 0;
    private static Object nextUniqueNumberLock = new Object();

    /** set of all generated proxy classes, for isProxyClass implementation */
    private static Map<Class<?>, Void> proxyClasses =
        Collections.synchronizedMap(new WeakHashMap<Class<?>, Void>());

    /**
     * the invocation handler for this proxy instance.
     * @serial
     */
    protected InvocationHandler h;

    /* J2ObjC removed.
     * Orders methods by their name, parameters, return type and inheritance relationship.
     *
     * @hide
    private static final Comparator<Method> ORDER_BY_SIGNATURE_AND_SUBTYPE = new Comparator<Method>() {
        @Override public int compare(Method a, Method b) {
            int comparison = Method.ORDER_BY_SIGNATURE.compare(a, b);
            if (comparison != 0) {
                return comparison;
            }
            Class<?> aClass = a.getDeclaringClass();
            Class<?> bClass = b.getDeclaringClass();
            if (aClass == bClass) {
                return 0;
            } else if (aClass.isAssignableFrom(bClass)) {
                return 1;
            } else if (bClass.isAssignableFrom(aClass)) {
                return -1;
            } else {
                return 0;
            }
        }
    };
    */

    /**
     * Prohibits instantiation.
     */
    private Proxy() {
    }

    /**
     * Constructs a new {@code Proxy} instance from a subclass
     * (typically, a dynamic proxy class) with the specified value
     * for its invocation handler.
     *
     * @param   h the invocation handler for this proxy instance
     */
    protected Proxy(InvocationHandler h) {
        this.h = h;
    }

    /**
     * Returns the {@code java.lang.Class} object for a proxy class
     * given a class loader and an array of interfaces.  The proxy class
     * will be defined by the specified class loader and will implement
     * all of the supplied interfaces.  If a proxy class for the same
     * permutation of interfaces has already been defined by the class
     * loader, then the existing proxy class will be returned; otherwise,
     * a proxy class for those interfaces will be generated dynamically
     * and defined by the class loader.
     *
     * <p>There are several restrictions on the parameters that may be
     * passed to {@code Proxy.getProxyClass}:
     *
     * <ul>
     * <li>All of the {@code Class} objects in the
     * {@code interfaces} array must represent interfaces, not
     * classes or primitive types.
     *
     * <li>No two elements in the {@code interfaces} array may
     * refer to identical {@code Class} objects.
     *
     * <li>All of the interface types must be visible by name through the
     * specified class loader.  In other words, for class loader
     * {@code cl} and every interface {@code i}, the following
     * expression must be true:
     * <pre>
     *     Class.forName(i.getName(), false, cl) == i
     * </pre>
     *
     * <li>All non-public interfaces must be in the same package;
     * otherwise, it would not be possible for the proxy class to
     * implement all of the interfaces, regardless of what package it is
     * defined in.
     *
     * <li>For any set of member methods of the specified interfaces
     * that have the same signature:
     * <ul>
     * <li>If the return type of any of the methods is a primitive
     * type or void, then all of the methods must have that same
     * return type.
     * <li>Otherwise, one of the methods must have a return type that
     * is assignable to all of the return types of the rest of the
     * methods.
     * </ul>
     *
     * <li>The resulting proxy class must not exceed any limits imposed
     * on classes by the virtual machine.  For example, the VM may limit
     * the number of interfaces that a class may implement to 65535; in
     * that case, the size of the {@code interfaces} array must not
     * exceed 65535.
     * </ul>
     *
     * <p>If any of these restrictions are violated,
     * {@code Proxy.getProxyClass} will throw an
     * {@code IllegalArgumentException}.  If the {@code interfaces}
     * array argument or any of its elements are {@code null}, a
     * {@code NullPointerException} will be thrown.
     *
     * <p>Note that the order of the specified proxy interfaces is
     * significant: two requests for a proxy class with the same combination
     * of interfaces but in a different order will result in two distinct
     * proxy classes.
     *
     * @param   loader the class loader to define the proxy class
     * @param   interfaces the list of interfaces for the proxy class
     *          to implement
     * @return  a proxy class that is defined in the specified class loader
     *          and that implements the specified interfaces
     * @throws  IllegalArgumentException if any of the restrictions on the
     *          parameters that may be passed to {@code getProxyClass}
     *          are violated
     * @throws  NullPointerException if the {@code interfaces} array
     *          argument or any of its elements are {@code null}
     */
    @CallerSensitive
    public static Class<?> getProxyClass(ClassLoader loader,
                                         Class<?>... interfaces)
        throws IllegalArgumentException
    {
        return getProxyClass0(loader, interfaces);
    }

    /**
     * Generate a proxy class.  Must call the checkProxyAccess method
     * to perform permission checks before calling this.
     */
    private static Class<?> getProxyClass0(ClassLoader loader,
                                           Class<?>... interfaces) {
        if (interfaces.length > 65535) {
            throw new IllegalArgumentException("interface limit exceeded");
        }

        Class<?> proxyClass = null;

        /* collect interface names to use as key for proxy class cache */
        String[] interfaceNames = new String[interfaces.length];

        // for detecting duplicates
        Set<Class<?>> interfaceSet = new HashSet<>();

        for (int i = 0; i < interfaces.length; i++) {
            /*
             * Verify that the class loader resolves the name of this
             * interface to the same Class object.
             */
            String interfaceName = interfaces[i].getName();
            Class<?> interfaceClass = null;
            try {
                interfaceClass = Class.forName(interfaceName, false, loader);
            } catch (ClassNotFoundException e) {
            }
            if (interfaceClass != interfaces[i]) {
                throw new IllegalArgumentException(
                    interfaces[i] + " is not visible from class loader");
            }

            /*
             * Verify that the Class object actually represents an
             * interface.
             */
            if (!interfaceClass.isInterface()) {
                throw new IllegalArgumentException(
                    interfaceClass.getName() + " is not an interface");
            }

            /*
             * Verify that this interface is not a duplicate.
             */
            if (interfaceSet.contains(interfaceClass)) {
                throw new IllegalArgumentException(
                    "repeated interface: " + interfaceClass.getName());
            }
            interfaceSet.add(interfaceClass);

            interfaceNames[i] = interfaceName;
        }

        /*
         * Using string representations of the proxy interfaces as
         * keys in the proxy class cache (instead of their Class
         * objects) is sufficient because we require the proxy
         * interfaces to be resolvable by name through the supplied
         * class loader, and it has the advantage that using a string
         * representation of a class makes for an implicit weak
         * reference to the class.
         */
        List<String> key = Arrays.asList(interfaceNames);

        /*
         * Find or create the proxy class cache for the class loader.
         */
        Map<List<String>, Object> cache;
        synchronized (loaderToCache) {
            cache = loaderToCache.get(loader);
            if (cache == null) {
                cache = new HashMap<>();
                loaderToCache.put(loader, cache);
            }
            /*
             * This mapping will remain valid for the duration of this
             * method, without further synchronization, because the mapping
             * will only be removed if the class loader becomes unreachable.
             */
        }

        /*
         * Look up the list of interfaces in the proxy class cache using
         * the key.  This lookup will result in one of three possible
         * kinds of values:
         *     null, if there is currently no proxy class for the list of
         *         interfaces in the class loader,
         *     the pendingGenerationMarker object, if a proxy class for the
         *         list of interfaces is currently being generated,
         *     or a weak reference to a Class object, if a proxy class for
         *         the list of interfaces has already been generated.
         */
        synchronized (cache) {
            /*
             * Note that we need not worry about reaping the cache for
             * entries with cleared weak references because if a proxy class
             * has been garbage collected, its class loader will have been
             * garbage collected as well, so the entire cache will be reaped
             * from the loaderToCache map.
             */
            do {
                Object value = cache.get(key);
                if (value instanceof Reference) {
                    proxyClass = (Class<?>) ((Reference<?>) value).get();
                }
                if (proxyClass != null) {
                    // proxy class already generated: return it
                    return proxyClass;
                } else if (value == pendingGenerationMarker) {
                    // proxy class being generated: wait for it
                    try {
                        cache.wait();
                    } catch (InterruptedException e) {
                        /*
                         * The class generation that we are waiting for should
                         * take a small, bounded time, so we can safely ignore
                         * thread interrupts here.
                         */
                    }
                    continue;
                } else {
                    /*
                     * No proxy class for this list of interfaces has been
                     * generated or is being generated, so we will go and
                     * generate it now.  Mark it as pending generation.
                     */
                    cache.put(key, pendingGenerationMarker);
                    break;
                }
            } while (true);
        }

        try {
            String proxyPkg = null;     // package to define proxy class in

            /*
             * Record the package of a non-public proxy interface so that the
             * proxy class will be defined in the same package.  Verify that
             * all non-public proxy interfaces are in the same package.
             */
            for (int i = 0; i < interfaces.length; i++) {
                int flags = interfaces[i].getModifiers();
                if (!Modifier.isPublic(flags)) {
                    String name = interfaces[i].getName();
                    int n = name.lastIndexOf('.');
                    String pkg = ((n == -1) ? "" : name.substring(0, n + 1));
                    if (proxyPkg == null) {
                        proxyPkg = pkg;
                    } else if (!pkg.equals(proxyPkg)) {
                        throw new IllegalArgumentException(
                            "non-public interfaces from different packages");
                    }
                }
            }

            if (proxyPkg == null) {
                // if no non-public proxy interfaces, use the default package.
                proxyPkg = "";
            }

            {
                // Android-changed: Generate the proxy directly instead of calling
                // through to ProxyGenerator.
                /* J2ObjC removed.
                List<Method> methods = getMethods(interfaces);
                Collections.sort(methods, ORDER_BY_SIGNATURE_AND_SUBTYPE);
                validateReturnTypes(methods);
                List<Class<?>[]> exceptions = deduplicateAndGetExceptions(methods);

                Method[] methodsArray = methods.toArray(new Method[methods.size()]);
                Class<?>[][] exceptionsArray = exceptions.toArray(new Class<?>[exceptions.size()][]);
                */

                /*
                 * Choose a name for the proxy class to generate.
                 */
                final long num;
                synchronized (nextUniqueNumberLock) {
                    num = nextUniqueNumber++;
                }
                String proxyName = proxyPkg + proxyClassNamePrefix + num;

                proxyClass = generateProxy(proxyName, interfaces, loader);
            }
            // add to set of all generated proxy classes, for isProxyClass
            proxyClasses.put(proxyClass, null);

        } finally {
            /*
             * We must clean up the "pending generation" state of the proxy
             * class cache entry somehow.  If a proxy class was successfully
             * generated, store it in the cache (with a weak reference);
             * otherwise, remove the reserved entry.  In all cases, notify
             * all waiters on reserved entries in this cache.
             */
            synchronized (cache) {
                if (proxyClass != null) {
                    cache.put(key, new WeakReference<Class<?>>(proxyClass));
                } else {
                    cache.remove(key);
                }
                cache.notifyAll();
            }
        }
        return proxyClass;
    }

    /* J2ObjC removed.
     * Remove methods that have the same name, parameters and return type. This
     * computes the exceptions of each method; this is the intersection of the
     * exceptions of equivalent methods.
     *
     * @param methods the methods to find exceptions for, ordered by name and
     *     signature.
    private static List<Class<?>[]> deduplicateAndGetExceptions(List<Method> methods) {
        List<Class<?>[]> exceptions = new ArrayList<Class<?>[]>(methods.size());

        for (int i = 0; i < methods.size(); ) {
            Method method = methods.get(i);
            Class<?>[] exceptionTypes = method.getExceptionTypes();

            if (i > 0 && Method.ORDER_BY_SIGNATURE.compare(method, methods.get(i - 1)) == 0) {
                exceptions.set(i - 1, intersectExceptions(exceptions.get(i - 1), exceptionTypes));
                methods.remove(i);
            } else {
                exceptions.add(exceptionTypes);
                i++;
            }
        }
        return exceptions;
    }
    */

    /* J2ObjC removed.
     * Returns the exceptions that are declared in both {@code aExceptions} and
     * {@code bExceptions}. If an exception type in one array is a subtype of an
     * exception from the other, the subtype is included in the intersection.
    private static Class<?>[] intersectExceptions(Class<?>[] aExceptions, Class<?>[] bExceptions) {
        if (aExceptions.length == 0 || bExceptions.length == 0) {
            return EmptyArray.CLASS;
        }
        if (Arrays.equals(aExceptions, bExceptions)) {
            return aExceptions;
        }
        Set<Class<?>> intersection = new HashSet<Class<?>>();
        for (Class<?> a : aExceptions) {
            for (Class<?> b : bExceptions) {
                if (a.isAssignableFrom(b)) {
                    intersection.add(b);
                } else if (b.isAssignableFrom(a)) {
                    intersection.add(a);
                }
            }
        }
        return intersection.toArray(new Class<?>[intersection.size()]);
    }
    */

    /* J2ObjC removed.
     * Throws if any two methods in {@code methods} have the same name and
     * parameters but incompatible return types.
     *
     * @param methods the methods to find exceptions for, ordered by name and
     *     signature.
    private static void validateReturnTypes(List<Method> methods) {
        Method vs = null;
        for (Method method : methods) {
            if (vs == null || !vs.equalNameAndParameters(method)) {
                vs = method; // this has a different name or parameters
                continue;
            }
            Class<?> returnType = method.getReturnType();
            Class<?> vsReturnType = vs.getReturnType();
            if (returnType.isInterface() && vsReturnType.isInterface()) {
                // all interfaces are mutually compatible
            } else if (vsReturnType.isAssignableFrom(returnType)) {
                vs = method; // the new return type is a subtype; use it instead
            } else if (!returnType.isAssignableFrom(vsReturnType)) {
                throw new IllegalArgumentException("proxied interface methods have incompatible "
                        + "return types:\n  " + vs + "\n  " + method);
            }
        }
    }

    private static List<Method> getMethods(Class<?>[] interfaces) {
        List<Method> result = new ArrayList<Method>();
        try {
            result.add(Object.class.getMethod("equals", Object.class));
            result.add(Object.class.getMethod("hashCode", EmptyArray.CLASS));
            result.add(Object.class.getMethod("toString", EmptyArray.CLASS));
        } catch (NoSuchMethodException e) {
            throw new AssertionError();
        }

        getMethodsRecursive(interfaces, result);
        return result;
    }
    */

    /* J2ObjC removed.
     * Fills {@code proxiedMethods} with the methods of {@code interfaces} and
     * the interfaces they extend. May contain duplicates.
    private static void getMethodsRecursive(Class<?>[] interfaces, List<Method> methods) {
        for (Class<?> i : interfaces) {
            getMethodsRecursive(i.getInterfaces(), methods);
            Collections.addAll(methods, i.getDeclaredMethods());
        }
    }
    */

    /**
     * Returns an instance of a proxy class for the specified interfaces
     * that dispatches method invocations to the specified invocation
     * handler.  This method is equivalent to:
     * <pre>
     *     Proxy.getProxyClass(loader, interfaces).
     *         getConstructor(new Class[] { InvocationHandler.class }).
     *         newInstance(new Object[] { handler });
     * </pre>
     *
     * <p>{@code Proxy.newProxyInstance} throws
     * {@code IllegalArgumentException} for the same reasons that
     * {@code Proxy.getProxyClass} does.
     *
     * @param   loader the class loader to define the proxy class
     * @param   interfaces the list of interfaces for the proxy class
     *          to implement
     * @param   h the invocation handler to dispatch method invocations to
     * @return  a proxy instance with the specified invocation handler of a
     *          proxy class that is defined by the specified class loader
     *          and that implements the specified interfaces
     * @throws  IllegalArgumentException if any of the restrictions on the
     *          parameters that may be passed to {@code getProxyClass}
     *          are violated
     * @throws  NullPointerException if the {@code interfaces} array
     *          argument or any of its elements are {@code null}, or
     *          if the invocation handler, {@code h}, is
     *          {@code null}
     */
    @CallerSensitive
    public static Object newProxyInstance(ClassLoader loader,
                                          Class<?>[] interfaces,
                                          InvocationHandler h)
        throws IllegalArgumentException
    {
        if (h == null) {
            throw new NullPointerException();
        }

        /*
         * Look up or generate the designated proxy class.
         */
        Class<?> cl = getProxyClass0(loader, interfaces);

        /*
         * Invoke its constructor with the designated invocation handler.
         */
        try {
            final Constructor<?> cons = cl.getConstructor(constructorParams);
            return newInstance(cons, h);
        } catch (NoSuchMethodException e) {
            throw new InternalError(e.toString());
        }
    }

    private static Object newInstance(Constructor<?> cons, InvocationHandler h) {
        try {
            return cons.newInstance(new Object[] {h} );
        } catch (IllegalAccessException | InstantiationException e) {
            throw new InternalError(e.toString());
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new InternalError(t.toString());
            }
        }
    }

    /**
     * Returns true if and only if the specified class was dynamically
     * generated to be a proxy class using the {@code getProxyClass}
     * method or the {@code newProxyInstance} method.
     *
     * <p>The reliability of this method is important for the ability
     * to use it to make security decisions, so its implementation should
     * not just test if the class in question extends {@code Proxy}.
     *
     * @param   cl the class to test
     * @return  {@code true} if the class is a proxy class and
     *          {@code false} otherwise
     * @throws  NullPointerException if {@code cl} is {@code null}
     */
    public static native boolean isProxyClass(Class<?> cl) /*-[
      return [nil_chk(cl) isKindOfClass:[IOSProxyClass class]];
    ]-*/;

    /**
     * Returns the invocation handler for the specified proxy instance.
     *
     * @param   proxy the proxy instance to return the invocation handler for
     * @return  the invocation handler for the proxy instance
     * @throws  IllegalArgumentException if the argument is not a
     *          proxy instance
     */
    public static InvocationHandler getInvocationHandler(Object proxy)
        throws IllegalArgumentException
    {
        /*
         * Verify that the object is actually a proxy instance.
         */
        if (!(proxy instanceof Proxy)) {
            throw new IllegalArgumentException("not a proxy instance");
        }
        return ((Proxy) proxy).h;
    }

    // These Object methods are overridden to avoid Objective C runtime optimization
    // that skips calling respondsToSelector: below.
    @Override
    public native String toString() /*-[
      SEL sel = @selector(proxy_toString);
      NSMethodSignature *signature = [self methodSignatureForSelector:sel];
      NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:signature];
      invocation.target = self;
      invocation.selector = @selector(proxy_toString);
      [self forwardInvocation:invocation];
      NSString *s;
      [invocation getReturnValue:&s];
      return s;
    ]-*/;

    String proxy_toString() {
      return "JavaLangReflectProxy@" + Integer.toHexString(proxy_hashCode());
    }

    @Override
    public native int hashCode() /*-[
      SEL sel = @selector(proxy_hashCode);
      NSMethodSignature *signature = [self methodSignatureForSelector:sel];
      NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:signature];
      invocation.target = self;
      invocation.selector = sel;
      [self forwardInvocation:invocation];
      jint hash;
      [invocation getReturnValue:&hash];
      return hash;
    ]-*/;

    native int proxy_hashCode() /*-[
      return JavaLangSystem_identityHashCodeWithId_(self);
    ]-*/;

    @Override
    public native boolean equals(Object obj) /*-[
      SEL sel = @selector(proxy_equalsWithId:);
      NSMethodSignature *signature = [self methodSignatureForSelector:sel];
      NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:signature];
      invocation.target = self;
      invocation.selector = sel;
      [invocation setArgument:&obj atIndex:2];  // 2 is first parameter.
      [self forwardInvocation:invocation];
      jboolean result;
      [invocation getReturnValue:&result];
      return result;
    ]-*/;

    boolean proxy_equals(Object obj) {
      return this == obj;
    }

    private static native Class<?> generateProxy(String name, Class<?>[] interfaces,
        ClassLoader loader) throws IllegalArgumentException /*-[
      Class proxyClass = objc_allocateClassPair([JavaLangReflectProxy class], [name UTF8String], 0);
      jint interfaceCount = interfaces->size_;
      for (jint i = 0; i < interfaceCount; i++) {
        IOSClass *intrface = (IOSClass *) [interfaces objectAtIndex:i];
        if (![intrface isInterface]) {
          @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc]
                              initWithNSString:[intrface description]]);
        }
        class_addProtocol(proxyClass, intrface.objcProtocol);
      }
      objc_registerClassPair(proxyClass);
      SEL sel = @selector(initWithJavaLangReflectInvocationHandler:);
      Method constructor = class_getInstanceMethod([JavaLangReflectProxy class], sel);
      class_addMethod(proxyClass, sel, method_getImplementation(constructor),
          method_getTypeEncoding(constructor));
      return IOSClass_NewProxyClass(proxyClass);
    ]-*/;

    /*-[
    static JavaLangReflectMethod *FindMethod(id self, SEL sel) {
      const char *selName = sel_getName(sel);
      for (IOSClass *cls in [[self java_getClass] getInterfacesInternal]) {
        JavaLangReflectMethod *result = JreMethodForSelectorInherited(cls, sel);
        if (result) {
          return result;
        }
      }
      // Skip all Proxy defined methods, except for the default Object methods
      // which all have a "proxy_" prefix.
      if (strncmp(selName, "proxy_", 6) == 0) {
        JavaLangReflectMethod *method = JreMethodForSelector(JavaLangReflectProxy_class_(), sel);
        if (method) {
          NSString *originalName = strcmp(selName, "proxy_equalsWithId:") == 0
              ? @"equals" : [NSString stringWithUTF8String:(selName + 6)];
          return AUTORELEASE([[ProxyMethod alloc] initWithMethod:method originalName:originalName]);
        }
      }
      return nil;
    }
    ]-*/

    /*-[
    - (NSMethodSignature *)methodSignatureForSelector:(SEL)aSelector {
      return [FindMethod(self, aSelector) getSignature];
    }
    ]-*/

    /*-[
    - (BOOL)respondsToSelector:(SEL)aSelector {
      return FindMethod(self, aSelector) != nil;
    }
    ]-*/

    /*-[
    // Forwards a message to the invocation handler for this proxy.
    -(void)forwardInvocation:(NSInvocation *)anInvocation {
      SEL selector = [anInvocation selector];
      JavaLangReflectMethod *method = FindMethod(self, selector);
      if (!method) {
        [self doesNotRecognizeSelector:_cmd];
      }

      IOSObjectArray *paramTypes = [method getParameterTypes];
      jint numArgs = paramTypes->size_;
      IOSObjectArray *args = [IOSObjectArray arrayWithLength:numArgs type:NSObject_class_()];
      for (jint i = 0; i < numArgs; i++) {
        J2ObjcRawValue arg;
        [anInvocation getArgument:&arg atIndex:i + 2];
        id javaArg = [paramTypes->buffer_[i] __boxValue:&arg];
        [args replaceObjectAtIndex:i withObject:javaArg];
      }

      id javaResult = [h_ invokeWithId:self
                   withJavaLangReflectMethod:method
                           withNSObjectArray:args];
      IOSClass *returnType = [method getReturnType];
      if (returnType != [IOSClass voidClass]) {
        IOSClass *resultType = [javaResult java_getClass];
        if ([returnType isPrimitive]) {
          // Return value is currently wrapped, so check wrapper type instead.
          returnType = [(IOSPrimitiveClass *) returnType wrapperClass];
        }
        if (javaResult && ![returnType isAssignableFrom:resultType]) {
          @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] init]);
        }
        J2ObjcRawValue result;
        [[method getReturnType] __unboxValue:javaResult toRawValue:&result];
        [anInvocation setReturnValue:&result];
      }
    }
    ]-*/
}
