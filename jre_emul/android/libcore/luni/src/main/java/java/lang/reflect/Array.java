/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang.reflect;

/**
 * Provides static methods to create and access arrays dynamically.
 */
public final class Array {
    private Array() {
    }

    private static IllegalArgumentException notAnArray(Object o) {
        throw new IllegalArgumentException("Not an array: " + o.getClass());
    }

    private static IllegalArgumentException incompatibleType(Object o) {
        throw new IllegalArgumentException("Array has incompatible type: " + o.getClass());
    }

    private static RuntimeException badArray(Object array) {
        if (array == null) {
            throw new NullPointerException("array == null");
        } else if (!array.getClass().isArray()) {
            throw notAnArray(array);
        } else {
            throw incompatibleType(array);
        }
    }

    /**
     * Returns the element of the array at the specified index. Equivalent to {@code array[index]}.
     * If the array component is a primitive type, the result is automatically boxed.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static Object get(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof Object[]) {
            return ((Object[]) array)[index];
        }
        if (array instanceof boolean[]) {
            return ((boolean[]) array)[index] ? Boolean.TRUE : Boolean.FALSE;
        }
        if (array instanceof byte[]) {
            return Byte.valueOf(((byte[]) array)[index]);
        }
        if (array instanceof char[]) {
            return Character.valueOf(((char[]) array)[index]);
        }
        if (array instanceof short[]) {
            return Short.valueOf(((short[]) array)[index]);
        }
        if (array instanceof int[]) {
            return Integer.valueOf(((int[]) array)[index]);
        }
        if (array instanceof long[]) {
            return Long.valueOf(((long[]) array)[index]);
        }
        if (array instanceof float[]) {
            return new Float(((float[]) array)[index]);
        }
        if (array instanceof double[]) {
            return new Double(((double[]) array)[index]);
        }
        if (array == null) {
            throw new NullPointerException("array == null");
        }
        throw notAnArray(array);
    }

    /**
     * Returns the boolean at the given index in the given boolean array.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the element at the
     *             index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code index < 0 || index >= array.length}
     */
    public static boolean getBoolean(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof boolean[]) {
            return ((boolean[]) array)[index];
        }
        throw badArray(array);
    }

    /**
     * Returns the byte at the given index in the given byte array.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the element at the
     *             index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code index < 0 || index >= array.length}
     */
    public static byte getByte(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof byte[]) {
            return ((byte[]) array)[index];
        }
        throw badArray(array);
    }

    /**
     * Returns the char at the given index in the given char array.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the element at the
     *             index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code index < 0 || index >= array.length}
     */
    public static char getChar(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof char[]) {
            return ((char[]) array)[index];
        }
        throw badArray(array);
    }

    /**
     * Returns the double at the given index in the given array.
     * Applies to byte, char, float, double, int, long, and short arrays.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the element at the
     *             index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code index < 0 || index >= array.length}
     */
    public static double getDouble(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof double[]) {
            return ((double[]) array)[index];
        } else if (array instanceof byte[]) {
            return ((byte[]) array)[index];
        } else if (array instanceof char[]) {
            return ((char[]) array)[index];
        } else if (array instanceof float[]) {
            return ((float[]) array)[index];
        } else if (array instanceof int[]) {
            return ((int[]) array)[index];
        } else if (array instanceof long[]) {
            return ((long[]) array)[index];
        } else if (array instanceof short[]) {
            return ((short[]) array)[index];
        }
        throw badArray(array);
    }

    /**
     * Returns the float at the given index in the given array.
     * Applies to byte, char, float, int, long, and short arrays.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the element at the
     *             index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code index < 0 || index >= array.length}
     */
    public static float getFloat(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof float[]) {
            return ((float[]) array)[index];
        } else if (array instanceof byte[]) {
            return ((byte[]) array)[index];
        } else if (array instanceof char[]) {
            return ((char[]) array)[index];
        } else if (array instanceof int[]) {
            return ((int[]) array)[index];
        } else if (array instanceof long[]) {
            return ((long[]) array)[index];
        } else if (array instanceof short[]) {
            return ((short[]) array)[index];
        }
        throw badArray(array);
    }

    /**
     * Returns the int at the given index in the given array.
     * Applies to byte, char, int, and short arrays.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the element at the
     *             index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code index < 0 || index >= array.length}
     */
    public static int getInt(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof int[]) {
            return ((int[]) array)[index];
        } else if (array instanceof byte[]) {
            return ((byte[]) array)[index];
        } else if (array instanceof char[]) {
            return ((char[]) array)[index];
        } else if (array instanceof short[]) {
            return ((short[]) array)[index];
        }
        throw badArray(array);
    }

    /**
     * Returns the length of the array. Equivalent to {@code array.length}.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array
     */
    public static int getLength(Object array) {
        if (array instanceof Object[]) {
            return ((Object[]) array).length;
        } else if (array instanceof boolean[]) {
            return ((boolean[]) array).length;
        } else if (array instanceof byte[]) {
            return ((byte[]) array).length;
        } else if (array instanceof char[]) {
            return ((char[]) array).length;
        } else if (array instanceof double[]) {
            return ((double[]) array).length;
        } else if (array instanceof float[]) {
            return ((float[]) array).length;
        } else if (array instanceof int[]) {
            return ((int[]) array).length;
        } else if (array instanceof long[]) {
            return ((long[]) array).length;
        } else if (array instanceof short[]) {
            return ((short[]) array).length;
        }
        throw badArray(array);
      }

    /**
     * Returns the long at the given index in the given array.
     * Applies to byte, char, int, long, and short arrays.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the element at the
     *             index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code index < 0 || index >= array.length}
     */
    public static long getLong(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof long[]) {
            return ((long[]) array)[index];
        } else if (array instanceof byte[]) {
            return ((byte[]) array)[index];
        } else if (array instanceof char[]) {
            return ((char[]) array)[index];
        } else if (array instanceof int[]) {
            return ((int[]) array)[index];
        } else if (array instanceof short[]) {
            return ((short[]) array)[index];
        }
        throw badArray(array);
    }

    /**
     * Returns the short at the given index in the given array.
     * Applies to byte and short arrays.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the element at the
     *             index position can not be converted to the return type
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code index < 0 || index >= array.length}
     */
    public static short getShort(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof short[]) {
            return ((short[]) array)[index];
        } else if (array instanceof byte[]) {
            return ((byte[]) array)[index];
        }
        throw badArray(array);
    }

    /**
     * Returns a new multidimensional array of the specified component type and
     * dimensions. Equivalent to {@code new componentType[d0][d1]...[dn]} for a
     * dimensions array of { d0, d1, ... , dn }.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws NegativeArraySizeException
     *             if any of the dimensions are negative
     * @throws IllegalArgumentException
     *             if the array of dimensions is of size zero, or exceeds the
     *             limit of the number of dimension for an array (currently 255)
     */
    public static Object newInstance(Class<?> componentType, int... dimensions) throws NegativeArraySizeException, IllegalArgumentException {
        if (dimensions.length <= 0 || dimensions.length > 255) {
            throw new IllegalArgumentException("Bad number of dimensions: " + dimensions.length);
        }
        if (componentType == void.class) {
            throw new IllegalArgumentException("Can't allocate an array of void");
        }
        if (componentType == null) {
            throw new NullPointerException("componentType == null");
        }
        return createMultiArray(componentType, dimensions);
    }

    /*
     * Create a multi-dimensional array of objects with the specified type.
     */
    private static native Object createMultiArray(Class<?> componentType, int[] dimensions)
        throws NegativeArraySizeException /*-[
      return [IOSObjectArray arrayWithDimensions:dimensions->size_
                                         lengths:dimensions->buffer_
                                            type:componentType];
    ]-*/;

    /**
     * Returns a new array of the specified component type and length.
     * Equivalent to {@code new componentType[size]}.
     *
     * @throws NullPointerException
     *             if the component type is null
     * @throws NegativeArraySizeException
     *             if {@code size < 0}
     */
    public static Object newInstance(Class<?> componentType, int size) throws NegativeArraySizeException {
        if (!componentType.isPrimitive()) {
            return createObjectArray(componentType, size);
        } else if (componentType == boolean.class) {
            return new boolean[size];
        } else if (componentType == byte.class) {
            return new byte[size];
        } else if (componentType == char.class) {
            return new char[size];
        } else if (componentType == short.class) {
            return new short[size];
        } else if (componentType == int.class) {
            return new int[size];
        } else if (componentType == long.class) {
            return new long[size];
        } else if (componentType == float.class) {
            return new float[size];
        } else if (componentType == double.class) {
            return new double[size];
        } else if (componentType == void.class) {
            throw new IllegalArgumentException("Can't allocate an array of void");
        }
        throw new AssertionError();
    }

    /*
     * Create a one-dimensional array of objects with the specified type.
     */
    private static native Object createObjectArray(Class<?> componentType, int length)
        throws NegativeArraySizeException /*-[
      return [IOSObjectArray arrayWithLength:length type:componentType];
    ]-*/;

    /**
     * Sets the element of the array at the specified index to the value.
     * Equivalent to {@code array[index] = value}. If the array
     * component is a primitive type, the value is automatically unboxed.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static void set(Object array, int index, Object value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (!array.getClass().isArray()) {
            throw notAnArray(array);
        }

        if (array instanceof Object[]) {
            if (value != null && !array.getClass().getComponentType().isInstance(value)) {
                throw incompatibleType(array);
            }
            ((Object[]) array)[index] = value;
        } else {
            if (value == null) {
                throw new IllegalArgumentException("Primitive array can't take null values.");
            }
            if (value instanceof Boolean) {
                setBoolean(array, index, ((Boolean) value).booleanValue());
            } else if (value instanceof Byte) {
                setByte(array, index, ((Byte) value).byteValue());
            } else if (value instanceof Character) {
                setChar(array, index, ((Character) value).charValue());
            } else if (value instanceof Short) {
                setShort(array, index, ((Short) value).shortValue());
            } else if (value instanceof Integer) {
                setInt(array, index, ((Integer) value).intValue());
            } else if (value instanceof Long) {
                setLong(array, index, ((Long) value).longValue());
            } else if (value instanceof Float) {
                setFloat(array, index, ((Float) value).floatValue());
            } else if (value instanceof Double) {
                setDouble(array, index, ((Double) value).doubleValue());
            }
        }
    }

    /**
     * Sets {@code array[index] = value}. Applies to boolean arrays.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if the {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static void setBoolean(Object array, int index, boolean value) {
        if (array instanceof boolean[]) {
            ((boolean[]) array)[index] = value;
        } else {
            throw badArray(array);
        }
    }

    /**
     * Sets {@code array[index] = value}. Applies to byte, double, float, int, long, and short arrays.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if the {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static void setByte(Object array, int index, byte value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof byte[]) {
            ((byte[]) array)[index] = value;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = value;
        } else if (array instanceof float[]) {
            ((float[]) array)[index] = value;
        } else if (array instanceof int[]) {
            ((int[]) array)[index] = value;
        } else if (array instanceof long[]) {
            ((long[]) array)[index] = value;
        } else if (array instanceof short[]) {
            ((short[]) array)[index] = value;
        } else {
            throw badArray(array);
        }
    }

    /**
     * Sets {@code array[index] = value}. Applies to char, double, float, int, and long arrays.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if the {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static void setChar(Object array, int index, char value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof char[]) {
            ((char[]) array)[index] = value;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = value;
        } else if (array instanceof float[]) {
            ((float[]) array)[index] = value;
        } else if (array instanceof int[]) {
            ((int[]) array)[index] = value;
        } else if (array instanceof long[]) {
            ((long[]) array)[index] = value;
        } else {
            throw badArray(array);
        }
    }

    /**
     * Sets {@code array[index] = value}. Applies to double arrays.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if the {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static void setDouble(Object array, int index, double value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof double[]) {
            ((double[]) array)[index] = value;
        } else {
            throw badArray(array);
        }
    }

    /**
     * Sets {@code array[index] = value}. Applies to double and float arrays.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if the {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static void setFloat(Object array, int index, float value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof float[]) {
            ((float[]) array)[index] = value;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = value;
        } else {
            throw badArray(array);
        }
    }

    /**
     * Sets {@code array[index] = value}. Applies to double, float, int, and long arrays.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if the {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static void setInt(Object array, int index, int value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof int[]) {
            ((int[]) array)[index] = value;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = value;
        } else if (array instanceof float[]) {
            ((float[]) array)[index] = value;
        } else if (array instanceof long[]) {
            ((long[]) array)[index] = value;
        } else {
            throw badArray(array);
        }
    }

    /**
     * Sets {@code array[index] = value}. Applies to double, float, and long arrays.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if the {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static void setLong(Object array, int index, long value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof long[]) {
            ((long[]) array)[index] = value;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = value;
        } else if (array instanceof float[]) {
            ((float[]) array)[index] = value;
        } else {
            throw badArray(array);
        }
    }

    /**
     * Sets {@code array[index] = value}. Applies to double, float, int, long, and short arrays.
     *
     * @throws NullPointerException if {@code array == null}
     * @throws IllegalArgumentException
     *             if the {@code array} is not an array or the value cannot be
     *             converted to the array type by a widening conversion
     * @throws ArrayIndexOutOfBoundsException
     *             if {@code  index < 0 || index >= array.length}
     */
    public static void setShort(Object array, int index, short value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array instanceof short[]) {
            ((short[]) array)[index] = value;
        } else if (array instanceof double[]) {
            ((double[]) array)[index] = value;
        } else if (array instanceof float[]) {
            ((float[]) array)[index] = value;
        } else if (array instanceof int[]) {
            ((int[]) array)[index] = value;
        } else if (array instanceof long[]) {
            ((long[]) array)[index] = value;
        } else {
            throw badArray(array);
        }
    }
}
