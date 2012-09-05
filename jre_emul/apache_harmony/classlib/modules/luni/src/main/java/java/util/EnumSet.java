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
package java.util;

import java.io.Serializable;

/**
 * An EnumSet is a specialized Set to be used with enums as keys.
 */
public abstract class EnumSet<E extends Enum<E>> extends AbstractSet<E>
        implements Cloneable, Serializable {

    private static final long serialVersionUID = 1009687484059888093L;

    final Class<E> elementClass;

    EnumSet(Class<E> cls) {
        elementClass = cls;
    }

    /**
     * Creates an empty enum set. The permitted elements are of type
     * Class&lt;E&gt;.
     * 
     * @param elementType
     *            the class object for the elements contained.
     * @return an empty enum set, with permitted elements of type {@code
     *         elementType}.
     * @throws ClassCastException
     *             if the specified element type is not and enum type.
     */
    public static <E extends Enum<E>> EnumSet<E> noneOf(Class<E> elementType) {
        if (!elementType.isEnum()) {
            throw new ClassCastException();
        }
        if (elementType.getEnumConstants().length <= 64) {
            return new MiniEnumSet<E>(elementType);
        }
        return new HugeEnumSet<E>(elementType);
    }

    /**
     * Creates an enum set filled with all the enum elements of the specified
     * {@code elementType}.
     * 
     * @param elementType
     *            the class object for the elements contained.
     * @return an enum set with elements solely from the specified element type.
     * @throws ClassCastException
     *             if the specified element type is not and enum type.
     */
    public static <E extends Enum<E>> EnumSet<E> allOf(Class<E> elementType) {
        EnumSet<E> set = noneOf(elementType);
        set.complement();
        return set;
    }

    /**
     * Creates an enum set. All the contained elements are of type
     * Class&lt;E&gt;, and the contained elements are the same as those
     * contained in {@code s}.
     * 
     * @param s
     *            the enum set from which to copy.
     * @return an enum set with all the elements from the specified enum set.
     * @throws ClassCastException
     *             if the specified element type is not and enum type.
     */
    public static <E extends Enum<E>> EnumSet<E> copyOf(EnumSet<E> s) {
        EnumSet<E> set = EnumSet.noneOf(s.elementClass);
        set.addAll(s);
        return set;
    }

    /**
     * Creates an enum set. The contained elements are the same as those
     * contained in collection {@code c}. If c is an enum set, invoking this
     * method is the same as invoking {@link #copyOf(EnumSet)}.
     * 
     * @param c
     *            the collection from which to copy. if it is not an enum set,
     *            it must not be empty.
     * @return an enum set with all the elements from the specified collection.
     * @throws IllegalArgumentException
     *             if c is not an enum set and contains no elements at all.
     * @throws NullPointerException
     *             if {@code c} is {@code null}.
     */
    public static <E extends Enum<E>> EnumSet<E> copyOf(Collection<E> c) {
        if (c instanceof EnumSet) {
            return copyOf((EnumSet<E>) c);
        }
        if (c.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Iterator<E> iterator = c.iterator();
        E element = iterator.next();
        EnumSet<E> set = EnumSet.noneOf(element.getDeclaringClass());
        set.add(element);
        while (iterator.hasNext()) {
            set.add(iterator.next());
        }
        return set;
    }

    /**
     * Creates an enum set. All the contained elements complement those from the
     * specified enum set.
     * 
     * @param s
     *            the specified enum set.
     * @return an enum set with all the elements complementary to those from the
     *         specified enum set.
     * @throws NullPointerException
     *             if {@code s} is {@code null}.
     */
    public static <E extends Enum<E>> EnumSet<E> complementOf(EnumSet<E> s) {
        EnumSet<E> set = EnumSet.noneOf(s.elementClass);
        set.addAll(s);
        set.complement();
        return set;
    }

    abstract void complement();

    /**
     * Creates a new enum set, containing only the specified element. There are
     * six overloadings of the method. They accept from one to five elements
     * respectively. The sixth one receives an arbitrary number of elements, and
     * runs slower than those that only receive a fixed number of elements.
     * 
     * @param e
     *            the element to be initially contained.
     * @return an enum set containing the specified element.
     * @throws NullPointerException
     *             if {@code e} is {@code null}.
     */
    public static <E extends Enum<E>> EnumSet<E> of(E e) {
        EnumSet<E> set = EnumSet.noneOf(e.getDeclaringClass());
        set.add(e);
        return set;
    }

    /**
     * Creates a new enum set, containing only the specified elements. There are
     * six overloadings of the method. They accept from one to five elements
     * respectively. The sixth one receives an arbitrary number of elements, and
     * runs slower than those that only receive a fixed number of elements.
     * 
     * @param e1
     *            the initially contained element.
     * @param e2
     *            another initially contained element.
     * @return an enum set containing the specified elements.
     * @throws NullPointerException
     *             if any of the specified elements is {@code null}.
     */
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2) {
        EnumSet<E> set = of(e1);
        set.add(e2);
        return set;
    }

    /**
     * Creates a new enum set, containing only the specified elements. There are
     * six overloadings of the method. They accept from one to five elements
     * respectively. The sixth one receives an arbitrary number of elements, and
     * runs slower than those that only receive a fixed number of elements.
     * 
     * @param e1
     *            the initially contained element.
     * @param e2
     *            another initially contained element.
     * @param e3
     *            another initially contained element.
     * @return an enum set containing the specified elements.
     * @throws NullPointerException
     *             if any of the specified elements is {@code null}.
     */
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3) {
        EnumSet<E> set = of(e1, e2);
        set.add(e3);
        return set;
    }

    /**
     * Creates a new enum set, containing only the specified elements. There are
     * six overloadings of the method. They accept from one to five elements
     * respectively. The sixth one receives an arbitrary number of elements, and
     * runs slower than those that only receive a fixed number of elements.
     * 
     * @param e1
     *            the initially contained element.
     * @param e2
     *            another initially contained element.
     * @param e3
     *            another initially contained element.
     * @param e4
     *            another initially contained element.
     * @return an enum set containing the specified elements.
     * @throws NullPointerException
     *             if any of the specified elements is {@code null}.
     */
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4) {
        EnumSet<E> set = of(e1, e2, e3);
        set.add(e4);
        return set;
    }

    /**
     * Creates a new enum set, containing only the specified elements. There are
     * six overloadings of the method. They accept from one to five elements
     * respectively. The sixth one receives an arbitrary number of elements, and
     * runs slower than those that only receive a fixed number of elements.
     * 
     * @param e1
     *            the initially contained element.
     * @param e2
     *            another initially contained element.
     * @param e3
     *            another initially contained element.
     * @param e4
     *            another initially contained element.
     * @param e5
     *            another initially contained element.
     * @return an enum set containing the specified elements.
     * @throws NullPointerException
     *             if any of the specified elements is {@code null}.
     */
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4, E e5) {
        EnumSet<E> set = of(e1, e2, e3, e4);
        set.add(e5);
        return set;
    }

    /**
     * Creates a new enum set, containing only the specified elements. It can
     * receive an arbitrary number of elements, and runs slower than those only
     * receiving a fixed number of elements.
     * 
     * @param start
     *            the first initially contained element.
     * @param others
     *            the other initially contained elements.
     * @return an enum set containing the specified elements.
     * @throws NullPointerException
     *             if any of the specified elements is {@code null}.
     */
    public static <E extends Enum<E>> EnumSet<E> of(E start, E... others) {
        EnumSet<E> set = of(start);
        for (E e : others) {
            set.add(e);
        }
        return set;
    }

    /**
     * Creates an enum set containing all the elements within the range defined
     * by {@code start} and {@code end} (inclusive). All the elements must be in
     * order.
     * 
     * @param start
     *            the element used to define the beginning of the range.
     * @param end
     *            the element used to define the end of the range.
     * @return an enum set with elements in the range from start to end.
     * @throws NullPointerException
     *             if any one of {@code start} or {@code end} is {@code null}.
     * @throws IllegalArgumentException
     *             if {@code start} is behind {@code end}.
     */
    public static <E extends Enum<E>> EnumSet<E> range(E start, E end) {
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException();
        }
        EnumSet<E> set = EnumSet.noneOf(start.getDeclaringClass());
        set.setRange(start, end);
        return set;
    }

    abstract void setRange(E start, E end);

    /**
     * Creates a new enum set with the same elements as those contained in this
     * enum set.
     * 
     * @return a new enum set with the same elements as those contained in this
     *         enum set.
     */
    @SuppressWarnings("unchecked")
    @Override
    public EnumSet<E> clone() {
        try {
            return (EnumSet<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    boolean isValidType(Class<?> cls) {
        return cls == elementClass || cls.getSuperclass() == elementClass;
    }
}
