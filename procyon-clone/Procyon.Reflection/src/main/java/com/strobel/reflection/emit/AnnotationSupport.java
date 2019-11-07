/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.strobel.reflection.emit;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

final class AnnotationSupport {
    static <A extends Annotation> A annotationForMap(
        final Class<A> annotationClass,
        final Map<String, Object> memberValues) {

        return AccessController.doPrivileged(new PrivilegedAction<A>() {
            public A run() {
                @SuppressWarnings("unchecked")
                final A annotationProxy = (A) Proxy.newProxyInstance(
                    annotationClass.getClassLoader(), new Class<?>[] { annotationClass },
                    new AnnotationInvocationHandler(annotationClass, memberValues)
                );

                return annotationProxy;
            }
        });
    }
}

/**
 * Represents an annotation type at run time.  Used to type-check annotations
 * and apply member defaults.
 *
 * @author Josh Bloch
 * @since 1.5
 */
@SuppressWarnings({ "WeakerAccess", "unused" })
final class AnnotationType {
    private final static ClassValue<AnnotationType> ANNOTATION_TYPES = new ClassValue<AnnotationType>() {
        @Override
        protected AnnotationType computeValue(final Class<?> clazz) {
            if (clazz.isAnnotation()) {
                @SuppressWarnings("unchecked")
                final Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) clazz;
                return new AnnotationType(annotationClass);
            }
            return null;
        }
    };

    /**
     * Member name -> type mapping. Note that primitive types are represented by the class objects for the
     * corresponding wrapper types.  This matches the return value that must be used for a dynamic proxy,
     * allowing for a simple isInstance test.
     */
    private final Map<String, Class<?>> memberTypes;

    /**
     * Member name -> default value mapping.
     */
    private final Map<String, Object> memberDefaults;

    /**
     * Member name -> Method object mapping. This (and its associated accessor) are used only to generate
     * AnnotationTypeMismatchExceptions.
     */
    private final Map<String, Method> members;

    /**
     * The retention policy for this annotation type.
     */
    private final RetentionPolicy retention;

    /**
     * Whether this annotation type is inherited.
     */
    private final boolean inherited;

    /**
     * Returns an AnnotationType instance for the specified annotation type.
     *
     * @throws IllegalArgumentException
     *     if the specified class object for does not represent a valid annotation type
     */
    public static AnnotationType getInstance(final Class<? extends Annotation> annotationClass) {
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type");
        }
        return ANNOTATION_TYPES.get(annotationClass);
    }

    /**
     * Sole constructor.
     *
     * @param annotationClass
     *     the class object for the annotation type
     *
     * @throws IllegalArgumentException
     *     if the specified class object for does not represent a valid annotation type
     */
    private AnnotationType(final Class<? extends Annotation> annotationClass) {
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("Not an annotation type");
        }

        Method[] methods =
            AccessController.doPrivileged(new PrivilegedAction<Method[]>() {
                public Method[] run() {
                    // Initialize memberTypes and defaultValues
                    return annotationClass.getDeclaredMethods();
                }
            });

        memberTypes = new HashMap<>(methods.length + 1, 1.0f);
        memberDefaults = new HashMap<>(0);
        members = new HashMap<>(methods.length + 1, 1.0f);

        for (Method method : methods) {
            if (method.getParameterTypes().length != 0) {
                throw new IllegalArgumentException(method + " has params");
            }
            String name = method.getName();
            Class<?> type = method.getReturnType();
            memberTypes.put(name, invocationHandlerReturnType(type));
            members.put(name, method);

            Object defaultValue = method.getDefaultValue();
            if (defaultValue != null) {
                memberDefaults.put(name, defaultValue);
            }
        }

        // Initialize retention, & inherited fields.  Special treatment
        // of the corresponding annotation types breaks infinite recursion.
        if (annotationClass != Retention.class && annotationClass != Inherited.class) {
            final Retention ret = annotationClass.getAnnotation(Retention.class);

            retention = (ret == null ? RetentionPolicy.CLASS : ret.value());
            inherited = annotationClass.getAnnotation(Inherited.class) != null;
        }
        else {
            retention = RetentionPolicy.RUNTIME;
            inherited = false;
        }
    }

    /**
     * Returns the type that must be returned by the invocation handler
     * of a dynamic proxy in order to have the dynamic proxy return
     * the specified type (which is assumed to be a legal member type
     * for an annotation).
     */
    public static Class<?> invocationHandlerReturnType(Class<?> type) {
        // Translate primitives to wrappers
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }

        // Otherwise, just return declared type
        return type;
    }

    /**
     * Returns member types for this annotation type
     * (member name -> type mapping).
     */
    public Map<String, Class<?>> memberTypes() {
        return memberTypes;
    }

    /**
     * Returns members of this annotation type
     * (member name -> associated Method object mapping).
     */
    public Map<String, Method> members() {
        return members;
    }

    /**
     * Returns the default values for this annotation type
     * (Member name -> default value mapping).
     */
    public Map<String, Object> memberDefaults() {
        return memberDefaults;
    }

    /**
     * Returns the retention policy for this annotation type.
     */
    public RetentionPolicy retention() {
        return retention;
    }

    /**
     * Returns true if this this annotation type is inherited.
     */
    public boolean isInherited() {
        return inherited;
    }

    /**
     * For debugging.
     */
    public String toString() {
        return "Annotation Type:\n" +
               "   Member types: " + memberTypes + "\n" +
               "   Member defaults: " + memberDefaults + "\n" +
               "   Retention policy: " + retention + "\n" +
               "   Inherited: " + inherited;
    }
}

/**
 * InvocationHandler for dynamic proxy implementation of Annotation.
 *
 * @author Josh Bloch
 * @since 1.5
 */
final class AnnotationInvocationHandler implements InvocationHandler, Serializable {
    private static final long serialVersionUID = 6182022883658399397L;
    private final Class<? extends Annotation> type;
    private final Map<String, Object> memberValues;

    AnnotationInvocationHandler(Class<? extends Annotation> type, Map<String, Object> memberValues) {
        this.type = type;
        this.memberValues = memberValues;
    }

    public Object invoke(Object proxy, Method method, Object[] args) {
        String member = method.getName();
        Class<?>[] paramTypes = method.getParameterTypes();

        //
        // Handle Object and Annotation methods.
        //

        if (member.equals("equals") && paramTypes.length == 1 &&
            paramTypes[0] == Object.class) {
            return equalsImpl(args[0]);
        }

        assert paramTypes.length == 0;

        if (member.equals("toString")) {
            return toStringImpl();
        }

        if (member.equals("hashCode")) {
            return hashCodeImpl();
        }

        if (member.equals("annotationType")) {
            return type;
        }

        //
        // Handle annotation member accessors
        //

        Object result = memberValues.get(member);

        if (result == null) {
            throw new IncompleteAnnotationException(type, member);
        }

        if (result instanceof ExceptionProxy) {
            throw ((ExceptionProxy) result).generateException();
        }

        if (result.getClass().isArray() && Array.getLength(result) != 0) {
            result = cloneArray(result);
        }

        return result;
    }

    /**
     * This method, which clones its array argument, would not be necessary if Cloneable had a
     * public clone method.
     */
    private static Object cloneArray(Object array) {
        Class<?> type = array.getClass();

        if (type == byte[].class) {
            byte[] byteArray = (byte[]) array;
            return byteArray.clone();
        }

        if (type == char[].class) {
            char[] charArray = (char[]) array;
            return charArray.clone();
        }

        if (type == double[].class) {
            double[] doubleArray = (double[]) array;
            return doubleArray.clone();
        }

        if (type == float[].class) {
            float[] floatArray = (float[]) array;
            return floatArray.clone();
        }

        if (type == int[].class) {
            int[] intArray = (int[]) array;
            return intArray.clone();
        }

        if (type == long[].class) {
            long[] longArray = (long[]) array;
            return longArray.clone();
        }

        if (type == short[].class) {
            short[] shortArray = (short[]) array;
            return shortArray.clone();
        }

        if (type == boolean[].class) {
            boolean[] booleanArray = (boolean[]) array;
            return booleanArray.clone();
        }

        Object[] objectArray = (Object[]) array;
        return objectArray.clone();
    }

    /**
     * Implementation of dynamicProxy.toString()
     */
    private String toStringImpl() {
        StringBuilder result = new StringBuilder(128);

        result.append('@');
        result.append(type.getName());
        result.append('(');

        boolean firstMember = true;

        for (Map.Entry<String, Object> e : memberValues.entrySet()) {
            if (firstMember) {
                firstMember = false;
            }
            else {
                result.append(", ");
            }

            result.append(e.getKey());
            result.append('=');
            result.append(memberValueToString(e.getValue()));
        }

        return result.append(')').toString();
    }

    /**
     * Translates a member value (in "dynamic proxy return form") into a string
     */
    private static String memberValueToString(Object value) {
        final Class<?> type = value.getClass();

        if (!type.isArray()) {
            // primitive, string, class, enum const, or annotation
            return value.toString();
        }

        if (type == byte[].class) {
            return Arrays.toString((byte[]) value);
        }

        if (type == char[].class) {
            return Arrays.toString((char[]) value);
        }

        if (type == double[].class) {
            return Arrays.toString((double[]) value);
        }

        if (type == float[].class) {
            return Arrays.toString((float[]) value);
        }

        if (type == int[].class) {
            return Arrays.toString((int[]) value);
        }

        if (type == long[].class) {
            return Arrays.toString((long[]) value);
        }

        if (type == short[].class) {
            return Arrays.toString((short[]) value);
        }

        if (type == boolean[].class) {
            return Arrays.toString((boolean[]) value);
        }

        return Arrays.toString((Object[]) value);
    }

    /**
     * Implementation of dynamicProxy.equals(Object o)
     */
    private Boolean equalsImpl(Object o) {
        if (o == this) {
            return true;
        }

        if (!type.isInstance(o)) {
            return false;
        }

        for (Method memberMethod : getMemberMethods()) {
            String member = memberMethod.getName();
            Object ourValue = memberValues.get(member);
            Object hisValue;
            AnnotationInvocationHandler hisHandler = asOneOfUs(o);

            if (hisHandler != null) {
                hisValue = hisHandler.memberValues.get(member);
            }
            else {
                try {
                    hisValue = memberMethod.invoke(o);
                }
                catch (InvocationTargetException e) {
                    return false;
                }
                catch (IllegalAccessException e) {
                    throw new AssertionError(e);
                }
            }

            if (!memberValueEquals(ourValue, hisValue)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns an object's invocation handler if that object is a dynamic
     * proxy with a handler of type AnnotationInvocationHandler.
     * Returns null otherwise.
     */
    private AnnotationInvocationHandler asOneOfUs(Object o) {
        if (Proxy.isProxyClass(o.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(o);
            if (handler instanceof AnnotationInvocationHandler) {
                return (AnnotationInvocationHandler) handler;
            }
        }
        return null;
    }

    /**
     * Returns true iff the two member values in "dynamic proxy return form"
     * are equal using the appropriate equality function depending on the
     * member type.  The two values will be of the same type unless one of
     * the containing annotations is ill-formed.  If one of the containing
     * annotations is ill-formed, this method will return false unless the
     * two members are identical object references.
     */
    @SuppressWarnings("ConstantConditions")
    private static boolean memberValueEquals(Object v1, Object v2) {
        Class<?> type = v1.getClass();

        // Check for primitive, string, class, enum const, annotation,
        // or ExceptionProxy
        if (!type.isArray()) {
            return v1.equals(v2);
        }

        // Check for array of string, class, enum const, annotation,
        // or ExceptionProxy
        if (v1 instanceof Object[] && v2 instanceof Object[]) {
            return Arrays.equals((Object[]) v1, (Object[]) v2);
        }

        // Check for ill formed annotation(s)
        if (v2.getClass() != type) {
            return false;
        }

        // Deal with array of primitives
        if (type == byte[].class) {
            return Arrays.equals((byte[]) v1, (byte[]) v2);
        }

        if (type == char[].class) {
            return Arrays.equals((char[]) v1, (char[]) v2);
        }

        if (type == double[].class) {
            return Arrays.equals((double[]) v1, (double[]) v2);
        }

        if (type == float[].class) {
            return Arrays.equals((float[]) v1, (float[]) v2);
        }

        if (type == int[].class) {
            return Arrays.equals((int[]) v1, (int[]) v2);
        }

        if (type == long[].class) {
            return Arrays.equals((long[]) v1, (long[]) v2);
        }

        if (type == short[].class) {
            return Arrays.equals((short[]) v1, (short[]) v2);
        }

        assert type == boolean[].class;
        return Arrays.equals((boolean[]) v1, (boolean[]) v2);
    }

    /**
     * Returns the member methods for our annotation type.  These are
     * obtained lazily and cached, as they're expensive to obtain
     * and we only need them if our equals method is invoked (which should
     * be rare).
     */
    private Method[] getMemberMethods() {
        if (memberMethods == null) {
            memberMethods = AccessController.doPrivileged(
                new PrivilegedAction<Method[]>() {
                    public Method[] run() {
                        final Method[] mm = type.getDeclaredMethods();
                        AccessibleObject.setAccessible(mm, true);
                        return mm;
                    }
                });
        }
        return memberMethods;
    }

    private transient volatile Method[] memberMethods = null;

    /**
     * Implementation of dynamicProxy.hashCode()
     */
    private int hashCodeImpl() {
        int result = 0;
        for (Map.Entry<String, Object> e : memberValues.entrySet()) {
            result += (127 * e.getKey().hashCode()) ^
                      memberValueHashCode(e.getValue());
        }
        return result;
    }

    /**
     * Computes hashCode of a member value (in "dynamic proxy return form")
     */
    private static int memberValueHashCode(Object value) {
        Class<?> type = value.getClass();

        if (!type.isArray()) {
            // primitive, string, class, enum const, or annotation
            return value.hashCode();
        }

        if (type == byte[].class) {
            return Arrays.hashCode((byte[]) value);
        }

        if (type == char[].class) {
            return Arrays.hashCode((char[]) value);
        }

        if (type == double[].class) {
            return Arrays.hashCode((double[]) value);
        }

        if (type == float[].class) {
            return Arrays.hashCode((float[]) value);
        }

        if (type == int[].class) {
            return Arrays.hashCode((int[]) value);
        }

        if (type == long[].class) {
            return Arrays.hashCode((long[]) value);
        }

        if (type == short[].class) {
            return Arrays.hashCode((short[]) value);
        }

        if (type == boolean[].class) {
            return Arrays.hashCode((boolean[]) value);
        }

        return Arrays.hashCode((Object[]) value);
    }

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();

        // Check to make sure that types have not evolved incompatibly
        AnnotationType annotationType;

        try {
            annotationType = AnnotationType.getInstance(type);
        }
        catch (IllegalArgumentException e) {
            // Class is no longer an annotation type; time to punch out
            throw new java.io.InvalidObjectException("Non-annotation type in annotation serial stream");
        }

        Map<String, Class<?>> memberTypes = annotationType.memberTypes();

        // If there are annotation members without values, that
        // situation is handled by the invoke method.

        for (Map.Entry<String, Object> memberValue : memberValues.entrySet()) {
            String name = memberValue.getKey();
            Class<?> memberType = memberTypes.get(name);

            if (memberType != null) {  // i.e. member still exists
                Object value = memberValue.getValue();

                if (!(memberType.isInstance(value) || value instanceof ExceptionProxy)) {
                    memberValue.setValue(
                        new AnnotationTypeMismatchExceptionProxy(
                            value.getClass() + "[" + value + "]").setMember(
                            annotationType.members().get(name))
                    );
                }
            }
        }
    }
}

/**
 * An instance of this class is stored in an AnnotationInvocationHandler's
 * "memberValues" map in lieu of a value for an annotation member that
 * cannot be returned due to some exceptional condition (typically some
 * form of illegal evolution of the annotation class).  The ExceptionProxy
 * instance describes the exception that the dynamic proxy should throw if
 * it is queried for this member.
 *
 * @author Josh Bloch
 * @since 1.5
 */
abstract class ExceptionProxy implements java.io.Serializable {
    protected abstract RuntimeException generateException();
}

/**
 * ExceptionProxy for AnnotationTypeMismatchException.
 *
 * @author Josh Bloch
 * @since 1.5
 */
final class AnnotationTypeMismatchExceptionProxy extends ExceptionProxy {
    private static final long serialVersionUID = 7844069490309503934L;
    private Method member;
    private String foundType;

    /**
     * It turns out to be convenient to construct these proxies in
     * two stages.  Since this is a private implementation class, we
     * permit ourselves this liberty even though it's normally a very
     * bad idea.
     */
    AnnotationTypeMismatchExceptionProxy(String foundType) {
        this.foundType = foundType;
    }

    AnnotationTypeMismatchExceptionProxy setMember(Method member) {
        this.member = member;
        return this;
    }

    protected RuntimeException generateException() {
        return new AnnotationTypeMismatchException(member, foundType);
    }
}
