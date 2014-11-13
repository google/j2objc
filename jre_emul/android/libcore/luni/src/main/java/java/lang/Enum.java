/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import libcore.util.BasicLruCache;
import libcore.util.EmptyArray;

/**
 * The superclass of all enumerated types. Actual enumeration types inherit from
 * this class, but extending this class does not make a class an enumeration
 * type, since the compiler needs to generate special information for it.
 */
public abstract class Enum<E extends Enum<E>> implements Serializable, Comparable<E> {

    private static final long serialVersionUID = -4300926546619394005L;

    private static final BasicLruCache<Class<? extends Enum>, Object[]> sharedConstantsCache
            = new BasicLruCache<Class<? extends Enum>, Object[]>(64) {
        @Override protected Object[] create(Class<? extends Enum> enumType) {
            if (!enumType.isEnum()) {
                return null;
            }
            try {
                Method method = enumType.getDeclaredMethod("values", EmptyArray.CLASS);
                method.setAccessible(true);
                return (Object[]) method.invoke((Object[]) null);
            } catch (NoSuchMethodException impossible) {
                throw new AssertionError("impossible", impossible);
            } catch (IllegalAccessException impossible) {
                throw new AssertionError("impossible", impossible);
            } catch (InvocationTargetException impossible) {
                throw new AssertionError("impossible", impossible);
            }
        }
    };

    private final String name;

    private final int ordinal;

    /**
     * Constructor for constants of enum subtypes.
     *
     * @param name
     *            the enum constant's declared name.
     * @param ordinal
     *            the enum constant's ordinal, which corresponds to its position
     *            in the enum declaration, starting at zero.
     */
    protected Enum(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    /**
     * Returns the name of this enum constant. The name is the field as it
     * appears in the {@code enum} declaration.
     *
     * @return the name of this enum constant.
     * @see #toString()
     */
    public final String name() {
        return name;
    }

    /**
     * Returns the position of the enum constant in the declaration. The first
     * constant has an ordinal value of zero.
     *
     * @return the ordinal value of this enum constant.
     */
    public final int ordinal() {
        return ordinal;
    }

    /**
     * Returns a string containing a concise, human-readable description of this
     * object. In this case, the enum constant's name is returned.
     *
     * @return a printable representation of this object.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Compares this object with the specified object and indicates if they are
     * equal. In order to be equal, {@code object} must be identical to this
     * enum constant.
     *
     * @param other
     *            the object to compare this enum constant with.
     * @return {@code true} if the specified object is equal to this
     *         {@code Enum}; {@code false} otherwise.
     */
    @Override
    public final boolean equals(Object other) {
        return this == other;
    }

    @Override
    public final int hashCode() {
        return ordinal + (name == null ? 0 : name.hashCode());
    }

    /**
     * {@code Enum} objects are singletons, they may not be cloned. This method
     * always throws a {@code CloneNotSupportedException}.
     *
     * @return does not return.
     * @throws CloneNotSupportedException
     *             is always thrown.
     */
    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Enums may not be cloned");
    }

    /**
     * Compares this object to the specified enum object to determine their
     * relative order. This method compares the object's ordinal values, that
     * is, their position in the enum declaration.
     *
     * @param o
     *            the enum object to compare this object to.
     * @return a negative value if the ordinal value of this enum constant is
     *         less than the ordinal value of {@code o}; 0 if the ordinal
     *         values of this enum constant and {@code o} are equal; a positive
     *         value if the ordinal value of this enum constant is greater than
     *         the ordinal value of {@code o}.
     * @see java.lang.Comparable
     */
    public final int compareTo(E o) {
        return ordinal - ((Enum<?>) o).ordinal;
    }

    /**
     * Returns the enum constant's declaring class.
     *
     * @return the class object representing the constant's enum type.
     */
    @SuppressWarnings("unchecked")
    public final Class<E> getDeclaringClass() {
        Class<?> myClass = getClass();
        Class<?> mySuperClass = myClass.getSuperclass();
        if (Enum.class == mySuperClass) {
            return (Class<E>)myClass;
        }
        return (Class<E>)mySuperClass;
    }

    /**
     * Returns the constant with the specified name of the specified enum type.
     *
     * @param enumType
     *            the class of the enumerated type to search for the constant
     *            value.
     * @param name
     *            the name of the constant value to find.
     * @return the enum constant.
     * @throws NullPointerException
     *             if either {@code enumType} or {@code name} are {@code null}.
     * @throws IllegalArgumentException
     *             if {@code enumType} is not an enumerated type or does not
     *             have a constant value called {@code name}.
     */
    public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
        if (enumType == null) {
            throw new NullPointerException("enumType == null");
        } else if (name == null) {
            throw new NullPointerException("name == null");
        }
        T[] values = getSharedConstants(enumType);
        if (values == null) {
            throw new IllegalArgumentException(enumType + " is not an enum type");
        }
        for (T value : values) {
            if (name.equals(value.name())) {
                return value;
            }
        }
        throw new IllegalArgumentException(name + " is not a constant in " + enumType.getName());
    }

    /**
     * Returns a shared, mutable array containing the constants of this enum. It
     * is an error to modify the returned array.
     *
     * @hide
     */
    @SuppressWarnings("unchecked") // the cache always returns the type matching enumType
    public static <T extends Enum<T>> T[] getSharedConstants(Class<T> enumType) {
        return (T[]) sharedConstantsCache.get(enumType);
    }

    /**
     * Enum types may not have finalizers.
     *
     * @since 1.6
     */
    @Override
    @SuppressWarnings("FinalizeDoesntCallSuperFinalize")
    protected final void finalize() {
    }

    /*-[
    J2OBJC_ETERNAL_SINGLETON
    ]-*/
}
