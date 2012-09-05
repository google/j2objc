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

package java.util;

import java.io.Serializable;
import java.lang.reflect.Array;

/**
 * {@code Arrays} contains static methods which operate on arrays.
 * 
 * @since 1.2
 */
public class Arrays {

    /* Specifies when to switch to insertion sort */
    private static final int SIMPLE_LENGTH = 7;

    private static class ArrayList<E> extends AbstractList<E> implements
            List<E>, Serializable, RandomAccess {

        private static final long serialVersionUID = -2764017481108945198L;

        private final E[] a;

        ArrayList(E[] storage) {
            if (storage == null) {
                throw new NullPointerException();
            }
            a = storage;
        }

        @Override
        public boolean contains(Object object) {
            if (object != null) {
                for (E element : a) {
                    if (object.equals(element)) {
                        return true;
                    }
                }
            } else {
                for (E element : a) {
                    if (element == null) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public E get(int location) {
            try {
                return a[location];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException();
            }
        }

        @Override
        public int indexOf(Object object) {
            if (object != null) {
                for (int i = 0; i < a.length; i++) {
                    if (object.equals(a[i])) {
                        return i;
                    }
                }
            } else {
                for (int i = 0; i < a.length; i++) {
                    if (a[i] == null) {
                        return i;
                    }
                }
            }
            return -1;
        }

        @Override
        public int lastIndexOf(Object object) {
            if (object != null) {
                for (int i = a.length - 1; i >= 0; i--) {
                    if (object.equals(a[i])) {
                        return i;
                    }
                }
            } else {
                for (int i = a.length - 1; i >= 0; i--) {
                    if (a[i] == null) {
                        return i;
                    }
                }
            }
            return -1;
        }

        @Override
        public E set(int location, E object) {
            try {
                E result = a[location];
                a[location] = object;
                return result;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException();
            } catch (ArrayStoreException e) {
                throw new ClassCastException();
            }
        }

        @Override
        public int size() {
            return a.length;
        }
    }

    private Arrays() {
        /* empty */
    }

    /**
     * Returns a {@code List} of the objects in the specified array. The size of the
     * {@code List} cannot be modified, i.e. adding and removing are unsupported, but
     * the elements can be set. Setting an element modifies the underlying
     * array.
     * 
     * @param array
     *            the array.
     * @return a {@code List} of the elements of the specified array.
     */
    public static <T> List<T> asList(T... array) {
        return new ArrayList<T>(array);
    }

    /**
     * Performs a binary search for the specified element in the specified
     * ascending sorted array. Searching in an unsorted array has an undefined
     * result. It's also undefined which element is found if there are multiple
     * occurrences of the same element.
     * 
     * @param array
     *            the sorted {@code byte} array to search.
     * @param value
     *            the {@code byte} element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     */
    public static int binarySearch(byte[] array, byte value) {
      return binarySearch(array, 0, array.length, value);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive).
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static int binarySearch(byte[] array, int startIndex, int endIndex, byte value) {
        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            byte midVal = array[mid];

            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else {
                return mid;  // value found
            }
        }
        return ~lo;  // value not present
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array}.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     */
    public static int binarySearch(char[] array, char value) {
        return binarySearch(array, 0, array.length, value);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive).
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static int binarySearch(char[] array, int startIndex, int endIndex, char value) {
        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            char midVal = array[mid];

            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else {
                return mid;  // value found
            }
        }
        return ~lo;  // value not present
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array}.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     */
    public static int binarySearch(double[] array, double value) {
        return binarySearch(array, 0, array.length, value);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive).
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static int binarySearch(double[] array, int startIndex, int endIndex, double value) {
        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            double midVal = array[mid];

            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else if (midVal != 0 && midVal == value) {
                return mid;  // value found
            } else { // Either midVal and value are == 0 or at least one is NaN
                long midValBits = Double.doubleToLongBits(midVal);
                long valueBits  = Double.doubleToLongBits(value);

                if (midValBits < valueBits) {
                    lo = mid + 1; // (-0.0, 0.0) or (not NaN, NaN); midVal < val
                } else if (midValBits > valueBits) {
                    hi = mid - 1; // (0.0, -0.0) or (NaN, not NaN); midVal > val
                } else {
                    return mid; // bit patterns are equal; value found
                }
            }
        }
        return ~lo;  // value not present
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array}.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     */
    public static int binarySearch(float[] array, float value) {
        return binarySearch(array, 0, array.length, value);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive).
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static int binarySearch(float[] array, int startIndex, int endIndex, float value) {
        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            float midVal = array[mid];

            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else if (midVal != 0 && midVal == value) {
                return mid;  // value found
            } else { // Either midVal and value are == 0 or at least one is NaN
                int midValBits = Float.floatToIntBits(midVal);
                int valueBits  = Float.floatToIntBits(value);

                if (midValBits < valueBits) {
                    lo = mid + 1; // (-0.0, 0.0) or (not NaN, NaN); midVal < val
                } else if (midValBits > valueBits) {
                    hi = mid - 1; // (0.0, -0.0) or (NaN, not NaN); midVal > val
                } else {
                    return mid; // bit patterns are equal; value found
                }
            }
        }
        return ~lo;  // value not present
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array}.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     */
    public static int binarySearch(int[] array, int value) {
        return binarySearch(array, 0, array.length, value);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive).
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static int binarySearch(int[] array, int startIndex, int endIndex, int value) {
        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int midVal = array[mid];

            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else {
                return mid;  // value found
            }
        }
        return ~lo;  // value not present
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array}.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     */
    public static int binarySearch(long[] array, long value) {
        return binarySearch(array, 0, array.length, value);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive).
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static int binarySearch(long[] array, int startIndex, int endIndex, long value) {
        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            long midVal = array[mid];

            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else {
                return mid;  // value found
            }
         }
         return ~lo;  // value not present
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array}.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws ClassCastException
     *         if an element in the array or the search element does not
     *         implement {@code Comparable}, or cannot be compared to each other.
     */
    public static int binarySearch(Object[] array, Object value) {
        return binarySearch(array, 0, array.length, value);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive).
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws ClassCastException
     *         if an element in the array or the search element does not
     *         implement {@code Comparable}, or cannot be compared to each other.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static int binarySearch(Object[] array, int startIndex, int endIndex, Object value) {
        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            @SuppressWarnings("unchecked")
            int midValCmp = ((Comparable) array[mid]).compareTo(value);

            if (midValCmp < 0) {
                lo = mid + 1;
            } else if (midValCmp > 0) {
                hi = mid - 1;
            } else {
                return mid;  // value found
            }
        }
        return ~lo;  // value not present
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * using {@code comparator} to compare elements.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param value the element to find.
     * @param comparator the {@code Comparator} used to compare the elements.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws ClassCastException
     *         if an element in the array or the search element does not
     *         implement {@code Comparable}, or cannot be compared to each other.
     */
    public static <T> int binarySearch(T[] array, T value, Comparator<? super T> comparator) {
        return binarySearch(array, 0, array.length, value, comparator);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive),
     * using {@code comparator} to compare elements.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @param comparator the {@code Comparator} used to compare the elements.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws ClassCastException
     *         if an element in the array or the search element does not
     *         implement {@code Comparable}, or cannot be compared to each other.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static <T> int binarySearch(T[] array, int startIndex, int endIndex, T value,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return binarySearch(array, startIndex, endIndex, value);
        }

        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int midValCmp = comparator.compare(array[mid], value);

            if (midValCmp < 0) {
                lo = mid + 1;
            } else if (midValCmp > 0) {
                hi = mid - 1;
            } else {
                return mid;  // value found
            }
        }
        return ~lo;  // value not present
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array}.
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     */
    public static int binarySearch(short[] array, short value) {
        return binarySearch(array, 0, array.length, value);
    }

    /**
     * Performs a binary search for {@code value} in the ascending sorted array {@code array},
     * in the range specified by fromIndex (inclusive) and toIndex (exclusive).
     * Searching in an unsorted array has an undefined result. It's also undefined which element
     * is found if there are multiple occurrences of the same element.
     *
     * @param array the sorted array to search.
     * @param startIndex the inclusive start index.
     * @param endIndex the exclusive start index.
     * @param value the element to find.
     * @return the non-negative index of the element, or a negative index which
     *         is {@code -index - 1} where the element would be inserted.
     * @throws IllegalArgumentException if {@code startIndex > endIndex}
     * @throws ArrayIndexOutOfBoundsException if {@code startIndex < 0 || endIndex > array.length}
     * @since 1.6
     */
    public static int binarySearch(short[] array, int startIndex, int endIndex, short value) {
        checkBinarySearchBounds(startIndex, endIndex, array.length);
        int lo = startIndex;
        int hi = endIndex - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            short midVal = array[mid];

            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else {
                return mid;  // value found
            }
        }
        return ~lo;  // value not present
    }

    private static void checkBinarySearchBounds(int startIndex, int endIndex, int length) {
        if (startIndex > endIndex) {
            throw new IllegalArgumentException();
        }
        if (startIndex < 0 || endIndex > length) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Fills the specified array with the specified element.
     * 
     * @param array
     *            the {@code byte} array to fill.
     * @param value
     *            the {@code byte} element.
     */
    public static void fill(byte[] array, byte value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    /**
     * Fills the specified range in the array with the specified element.
     * 
     * @param array
     *            the {@code byte} array to fill.
     * @param start
     *            the first index to fill.
     * @param end
     *            the last + 1 index to fill.
     * @param value
     *            the {@code byte} element.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void fill(byte[] array, int start, int end, byte value) {
        checkBounds(array.length, start, end);
        for (int i = start; i < end; i++) {
            array[i] = value;
        }
    }

    /**
     * Fills the specified array with the specified element.
     * 
     * @param array
     *            the {@code short} array to fill.
     * @param value
     *            the {@code short} element.
     */
    public static void fill(short[] array, short value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    /**
     * Fills the specified range in the array with the specified element.
     * 
     * @param array
     *            the {@code short} array to fill.
     * @param start
     *            the first index to fill.
     * @param end
     *            the last + 1 index to fill.
     * @param value
     *            the {@code short} element.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void fill(short[] array, int start, int end, short value) {
        checkBounds(array.length, start, end);
        for (int i = start; i < end; i++) {
            array[i] = value;
        }
    }

    /**
     * Fills the specified array with the specified element.
     * 
     * @param array
     *            the {@code char} array to fill.
     * @param value
     *            the {@code char} element.
     */
    public static void fill(char[] array, char value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    /**
     * Fills the specified range in the array with the specified element.
     * 
     * @param array
     *            the {@code char} array to fill.
     * @param start
     *            the first index to fill.
     * @param end
     *            the last + 1 index to fill.
     * @param value
     *            the {@code char} element.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void fill(char[] array, int start, int end, char value) {
        checkBounds(array.length, start, end);
        for (int i = start; i < end; i++) {
            array[i] = value;
        }
    }

    /**
     * Fills the specified array with the specified element.
     * 
     * @param array
     *            the {@code int} array to fill.
     * @param value
     *            the {@code int} element.
     */
    public static void fill(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    /**
     * Fills the specified range in the array with the specified element.
     * 
     * @param array
     *            the {@code int} array to fill.
     * @param start
     *            the first index to fill.
     * @param end
     *            the last + 1 index to fill.
     * @param value
     *            the {@code int} element.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void fill(int[] array, int start, int end, int value) {
        checkBounds(array.length, start, end);
        for (int i = start; i < end; i++) {
            array[i] = value;
        }
    }

    /**
     * Fills the specified array with the specified element.
     * 
     * @param array
     *            the {@code long} array to fill.
     * @param value
     *            the {@code long} element.
     */
    public static void fill(long[] array, long value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    /**
     * Fills the specified range in the array with the specified element.
     * 
     * @param array
     *            the {@code long} array to fill.
     * @param start
     *            the first index to fill.
     * @param end
     *            the last + 1 index to fill.
     * @param value
     *            the {@code long} element.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void fill(long[] array, int start, int end, long value) {
        checkBounds(array.length, start, end);
        for (int i = start; i < end; i++) {
            array[i] = value;
        }
    }

    /**
     * Fills the specified array with the specified element.
     * 
     * @param array
     *            the {@code float} array to fill.
     * @param value
     *            the {@code float} element.
     */
    public static void fill(float[] array, float value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    /**
     * Fills the specified range in the array with the specified element.
     * 
     * @param array
     *            the {@code float} array to fill.
     * @param start
     *            the first index to fill.
     * @param end
     *            the last + 1 index to fill.
     * @param value
     *            the {@code float} element.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void fill(float[] array, int start, int end, float value) {
        checkBounds(array.length, start, end);
        for (int i = start; i < end; i++) {
            array[i] = value;
        }
    }

    /**
     * Fills the specified array with the specified element.
     * 
     * @param array
     *            the {@code double} array to fill.
     * @param value
     *            the {@code double} element.
     */
    public static void fill(double[] array, double value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    /**
     * Fills the specified range in the array with the specified element.
     * 
     * @param array
     *            the {@code double} array to fill.
     * @param start
     *            the first index to fill.
     * @param end
     *            the last + 1 index to fill.
     * @param value
     *            the {@code double} element.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void fill(double[] array, int start, int end, double value) {
        checkBounds(array.length, start, end);
        for (int i = start; i < end; i++) {
            array[i] = value;
        }
    }

    /**
     * Fills the specified array with the specified element.
     * 
     * @param array
     *            the {@code boolean} array to fill.
     * @param value
     *            the {@code boolean} element.
     */
    public static void fill(boolean[] array, boolean value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    /**
     * Fills the specified range in the array with the specified element.
     * 
     * @param array
     *            the {@code boolean} array to fill.
     * @param start
     *            the first index to fill.
     * @param end
     *            the last + 1 index to fill.
     * @param value
     *            the {@code boolean} element.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void fill(boolean[] array, int start, int end, boolean value) {
        checkBounds(array.length, start, end);
        for (int i = start; i < end; i++) {
            array[i] = value;
        }
    }

    /**
     * Fills the specified array with the specified element.
     * 
     * @param array
     *            the {@code Object} array to fill.
     * @param value
     *            the {@code Object} element.
     */
    public static void fill(Object[] array, Object value) {
        for (int i = 0; i < array.length; i++) {
            array[i] = value;
        }
    }

    /**
     * Fills the specified range in the array with the specified element.
     * 
     * @param array
     *            the {@code Object} array to fill.
     * @param start
     *            the first index to fill.
     * @param end
     *            the last + 1 index to fill.
     * @param value
     *            the {@code Object} element.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void fill(Object[] array, int start, int end, Object value) {
        checkBounds(array.length, start, end);
        for (int i = start; i < end; i++) {
            array[i] = value;
        }
    }

    /**
     * Returns a hash code based on the contents of the given array. For any two
     * {@code boolean} arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the
     * {@link List#hashCode()}} method which is invoked on a {@link List}}
     * containing a sequence of {@link Boolean}} instances representing the
     * elements of array in the same order. If the array is {@code null}, the return
     * value is 0.
     * 
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(boolean[] array) {
        if (array == null) {
            return 0;
        }
        int hashCode = 1;
        for (boolean element : array) {
            // 1231, 1237 are hash code values for boolean value
            hashCode = 31 * hashCode + (element ? 1231 : 1237);
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the contents of the given array. For any two
     * not-null {@code int} arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the
     * {@link List#hashCode()}} method which is invoked on a {@link List}}
     * containing a sequence of {@link Integer}} instances representing the
     * elements of array in the same order. If the array is {@code null}, the return
     * value is 0.
     * 
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(int[] array) {
        if (array == null) {
            return 0;
        }
        int hashCode = 1;
        for (int element : array) {
            // the hash code value for integer value is integer value itself
            hashCode = 31 * hashCode + element;
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the contents of the given array. For any two
     * {@code short} arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the
     * {@link List#hashCode()}} method which is invoked on a {@link List}}
     * containing a sequence of {@link Short}} instances representing the
     * elements of array in the same order. If the array is {@code null}, the return
     * value is 0.
     * 
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(short[] array) {
        if (array == null) {
            return 0;
        }
        int hashCode = 1;
        for (short element : array) {
            // the hash code value for short value is its integer value
            hashCode = 31 * hashCode + element;
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the contents of the given array. For any two
     * {@code char} arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the
     * {@link List#hashCode()}} method which is invoked on a {@link List}}
     * containing a sequence of {@link Character}} instances representing the
     * elements of array in the same order. If the array is {@code null}, the return
     * value is 0.
     * 
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(char[] array) {
        if (array == null) {
            return 0;
        }
        int hashCode = 1;
        for (char element : array) {
            // the hash code value for char value is its integer value
            hashCode = 31 * hashCode + element;
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the contents of the given array. For any two
     * {@code byte} arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the
     * {@link List#hashCode()}} method which is invoked on a {@link List}}
     * containing a sequence of {@link Byte}} instances representing the
     * elements of array in the same order. If the array is {@code null}, the return
     * value is 0.
     * 
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(byte[] array) {
        if (array == null) {
            return 0;
        }
        int hashCode = 1;
        for (byte element : array) {
            // the hash code value for byte value is its integer value
            hashCode = 31 * hashCode + element;
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the contents of the given array. For any two
     * {@code long} arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the
     * {@link List#hashCode()}} method which is invoked on a {@link List}}
     * containing a sequence of {@link Long}} instances representing the
     * elements of array in the same order. If the array is {@code null}, the return
     * value is 0.
     * 
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(long[] array) {
        if (array == null) {
            return 0;
        }
        int hashCode = 1;
        for (long elementValue : array) {
            /*
             * the hash code value for long value is (int) (value ^ (value >>>
             * 32))
             */
            hashCode = 31 * hashCode
                    + (int) (elementValue ^ (elementValue >>> 32));
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the contents of the given array. For any two
     * {@code float} arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the
     * {@link List#hashCode()}} method which is invoked on a {@link List}}
     * containing a sequence of {@link Float}} instances representing the
     * elements of array in the same order. If the array is {@code null}, the return
     * value is 0.
     * 
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(float[] array) {
        if (array == null) {
            return 0;
        }
        int hashCode = 1;
        for (float element : array) {
            /*
             * the hash code value for float value is
             * Float.floatToIntBits(value)
             */
            hashCode = 31 * hashCode + Float.floatToIntBits(element);
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the contents of the given array. For any two
     * {@code double} arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the
     * {@link List#hashCode()}} method which is invoked on a {@link List}}
     * containing a sequence of {@link Double}} instances representing the
     * elements of array in the same order. If the array is {@code null}, the return
     * value is 0.
     * 
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(double[] array) {
        if (array == null) {
            return 0;
        }
        int hashCode = 1;

        for (double element : array) {
            long v = Double.doubleToLongBits(element);
            /*
             * the hash code value for double value is (int) (v ^ (v >>> 32))
             * where v = Double.doubleToLongBits(value)
             */
            hashCode = 31 * hashCode + (int) (v ^ (v >>> 32));
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the contents of the given array. If the
     * array contains other arrays as its elements, the hash code is based on
     * their identities not their contents. So it is acceptable to invoke this
     * method on an array that contains itself as an element, either directly or
     * indirectly.
     * <p>
     * For any two arrays {@code a} and {@code b}, if
     * {@code Arrays.equals(a, b)} returns {@code true}, it means
     * that the return value of {@code Arrays.hashCode(a)} equals
     * {@code Arrays.hashCode(b)}.
     * <p>
     * The value returned by this method is the same value as the method
     * Arrays.asList(array).hashCode(). If the array is {@code null}, the return value
     * is 0.
     * 
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int hashCode(Object[] array) {
        if (array == null) {
            return 0;
        }
        int hashCode = 1;
        for (Object element : array) {
            int elementHashCode;

            if (element == null) {
                elementHashCode = 0;
            } else {
                elementHashCode = (element).hashCode();
            }
            hashCode = 31 * hashCode + elementHashCode;
        }
        return hashCode;
    }

    /**
     * Returns a hash code based on the "deep contents" of the given array. If
     * the array contains other arrays as its elements, the hash code is based
     * on their contents not their identities. So it is not acceptable to invoke
     * this method on an array that contains itself as an element, either
     * directly or indirectly.
     * <p>
     * For any two arrays {@code a} and {@code b}, if
     * {@code Arrays.deepEquals(a, b)} returns {@code true}, it
     * means that the return value of {@code Arrays.deepHashCode(a)} equals
     * {@code Arrays.deepHashCode(b)}.
     * <p>
     * The computation of the value returned by this method is similar to that
     * of the value returned by {@link List#hashCode()}} invoked on a
     * {@link List}} containing a sequence of instances representing the
     * elements of array in the same order. The difference is: If an element e
     * of array is itself an array, its hash code is computed by calling the
     * appropriate overloading of {@code Arrays.hashCode(e)} if e is an array of a
     * primitive type, or by calling {@code Arrays.deepHashCode(e)} recursively if e is
     * an array of a reference type. The value returned by this method is the
     * same value as the method {@code Arrays.asList(array).hashCode()}. If the array is
     * {@code null}, the return value is 0.
     * 
     * @param array
     *            the array whose hash code to compute.
     * @return the hash code for {@code array}.
     */
    public static int deepHashCode(Object[] array) {
      throw new AssertionError("not implemented");
    }

    /**
     * Compares the two arrays.
     * 
     * @param array1
     *            the first {@code byte} array.
     * @param array2
     *            the second {@code byte} array.
     * @return {@code true} if both arrays are {@code null} or if the arrays have the
     *         same length and the elements at each index in the two arrays are
     *         equal, {@code false} otherwise.
     */
    public static boolean equals(byte[] array1, byte[] array2) {
        if (array1 == array2) {
            return true;
        }
        if (array1 == null || array2 == null || array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the two arrays.
     * 
     * @param array1
     *            the first {@code short} array.
     * @param array2
     *            the second {@code short} array.
     * @return {@code true} if both arrays are {@code null} or if the arrays have the
     *         same length and the elements at each index in the two arrays are
     *         equal, {@code false} otherwise.
     */
    public static boolean equals(short[] array1, short[] array2) {
        if (array1 == array2) {
            return true;
        }
        if (array1 == null || array2 == null || array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the two arrays.
     * 
     * @param array1
     *            the first {@code char} array.
     * @param array2
     *            the second {@code char} array.
     * @return {@code true} if both arrays are {@code null} or if the arrays have the
     *         same length and the elements at each index in the two arrays are
     *         equal, {@code false} otherwise.
     */
    public static boolean equals(char[] array1, char[] array2) {
        if (array1 == array2) {
            return true;
        }
        if (array1 == null || array2 == null || array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the two arrays.
     * 
     * @param array1
     *            the first {@code int} array.
     * @param array2
     *            the second {@code int} array.
     * @return {@code true} if both arrays are {@code null} or if the arrays have the
     *         same length and the elements at each index in the two arrays are
     *         equal, {@code false} otherwise.
     */
    public static boolean equals(int[] array1, int[] array2) {
        if (array1 == array2) {
            return true;
        }
        if (array1 == null || array2 == null || array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the two arrays.
     * 
     * @param array1
     *            the first {@code long} array.
     * @param array2
     *            the second {@code long} array.
     * @return {@code true} if both arrays are {@code null} or if the arrays have the
     *         same length and the elements at each index in the two arrays are
     *         equal, {@code false} otherwise.
     */
    public static boolean equals(long[] array1, long[] array2) {
        if (array1 == array2) {
            return true;
        }
        if (array1 == null || array2 == null || array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the two arrays. The values are compared in the same manner as
     * {@code Float.equals()}.
     * 
     * @param array1
     *            the first {@code float} array.
     * @param array2
     *            the second {@code float} array.
     * @return {@code true} if both arrays are {@code null} or if the arrays have the
     *         same length and the elements at each index in the two arrays are
     *         equal, {@code false} otherwise.
     * @see Float#equals(Object)
     */
    public static boolean equals(float[] array1, float[] array2) {
        if (array1 == array2) {
            return true;
        }
        if (array1 == null || array2 == null || array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (Float.floatToIntBits(array1[i]) != Float
                    .floatToIntBits(array2[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the two arrays. The values are compared in the same manner as
     * {@code Double.equals()}.
     * 
     * @param array1
     *            the first {@code double} array.
     * @param array2
     *            the second {@code double} array.
     * @return {@code true} if both arrays are {@code null} or if the arrays have the
     *         same length and the elements at each index in the two arrays are
     *         equal, {@code false} otherwise.
     * @see Double#equals(Object)
     */
    public static boolean equals(double[] array1, double[] array2) {
        if (array1 == array2) {
            return true;
        }
        if (array1 == null || array2 == null || array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (Double.doubleToLongBits(array1[i]) != Double
                    .doubleToLongBits(array2[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the two arrays.
     * 
     * @param array1
     *            the first {@code boolean} array.
     * @param array2
     *            the second {@code boolean} array.
     * @return {@code true} if both arrays are {@code null} or if the arrays have the
     *         same length and the elements at each index in the two arrays are
     *         equal, {@code false} otherwise.
     */
    public static boolean equals(boolean[] array1, boolean[] array2) {
        if (array1 == array2) {
            return true;
        }
        if (array1 == null || array2 == null || array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the two arrays.
     * 
     * @param array1
     *            the first {@code Object} array.
     * @param array2
     *            the second {@code Object} array.
     * @return {@code true} if both arrays are {@code null} or if the arrays have the
     *         same length and the elements at each index in the two arrays are
     *         equal according to {@code equals()}, {@code false} otherwise.
     */
    public static boolean equals(Object[] array1, Object[] array2) {
        if (array1 == array2) {
            return true;
        }
        if (array1 == null || array2 == null || array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            Object e1 = array1[i], e2 = array2[i];
            if (!(e1 == null ? e2 == null : e1.equals(e2))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if the two given arrays are deeply equal to one another.
     * Unlike the method {@code equals(Object[] array1, Object[] array2)}, this method
     * is appropriate for use for nested arrays of arbitrary depth.
     * <p>
     * Two array references are considered deeply equal if they are both {@code null},
     * or if they refer to arrays that have the same length and the elements at
     * each index in the two arrays are equal.
     * <p>
     * Two {@code null} elements {@code element1} and {@code element2} are possibly deeply equal if any
     * of the following conditions satisfied:
     * <p>
     * {@code element1} and {@code element2} are both arrays of object reference types, and
     * {@code Arrays.deepEquals(element1, element2)} would return {@code true}.
     * <p>
     * {@code element1} and {@code element2} are arrays of the same primitive type, and the
     * appropriate overloading of {@code Arrays.equals(element1, element2)} would return
     * {@code true}.
     * <p>
     * {@code element1 == element2}
     * <p>
     * {@code element1.equals(element2)} would return {@code true}.
     * <p>
     * Note that this definition permits {@code null} elements at any depth.
     * <p>
     * If either of the given arrays contain themselves as elements, the
     * behavior of this method is uncertain.
     * 
     * @param array1
     *            the first {@code Object} array.
     * @param array2
     *            the second {@code Object} array.
     * @return {@code true} if both arrays are {@code null} or if the arrays have the
     *         same length and the elements at each index in the two arrays are
     *         equal according to {@code equals()}, {@code false} otherwise.
     */
    public static boolean deepEquals(Object[] array1, Object[] array2) {
      throw new AssertionError("not implemented");
    }

    private static boolean isSame(double double1, double double2) {
        // Simple case
        if (double1 == double2 && 0.0d != double1) {
            return true;
        }

        // Deal with NaNs
        if (Double.isNaN(double1)) {
            return Double.isNaN(double2);
        }
        if (Double.isNaN(double2)) {
            return false;
        }

        // Deal with +0.0 and -0.0
        long d1 = Double.doubleToRawLongBits(double1);
        long d2 = Double.doubleToRawLongBits(double2);
        return d1 == d2;
    }

    private static boolean lessThan(double double1, double double2) {
        // A slightly specialized version of
        // Double.compare(double1, double2) < 0.

        // Non-zero and non-NaN checking.
        if (double1 < double2) {
            return true;
        }
        if (double1 > double2) {
            return false;
        }
        if (double1 == double2 && 0.0d != double1) {
            return false;
        }

        // NaNs are equal to other NaNs and larger than any other double.
        if (Double.isNaN(double1)) {
            return false;
        } else if (Double.isNaN(double2)) {
            return true;
        }

        // Deal with +0.0 and -0.0.
        long d1 = Double.doubleToRawLongBits(double1);
        long d2 = Double.doubleToRawLongBits(double2);
        return d1 < d2;
    }

    private static boolean isSame(float float1, float float2) {
        // Simple case
        if (float1 == float2 && 0.0d != float1) {
            return true;
        }

        // Deal with NaNs
        if (Float.isNaN(float1)) {
            return Float.isNaN(float2);
        }
        if (Float.isNaN(float2)) {
            return false;
        }

        // Deal with +0.0 and -0.0
        int f1 = Float.floatToRawIntBits(float1);
        int f2 = Float.floatToRawIntBits(float2);
        return f1 == f2;
    }
    
    private static boolean lessThan(float float1, float float2) {
        // A slightly specialized version of Float.compare(float1, float2) < 0.

        // Non-zero and non-NaN checking.
        if (float1 < float2) {
            return true;
        }
        if (float1 > float2) {
            return false;
        }
        if (float1 == float2 && 0.0f != float1) {
            return false;
        }

        // NaNs are equal to other NaNs and larger than any other float
        if (Float.isNaN(float1)) {
            return false;
        } else if (Float.isNaN(float2)) {
            return true;
        }

        // Deal with +0.0 and -0.0
        int f1 = Float.floatToRawIntBits(float1);
        int f2 = Float.floatToRawIntBits(float2);
        return f1 < f2;
    }

    private static int med3(byte[] array, int a, int b, int c) {
        byte x = array[a], y = array[b], z = array[c];
        return x < y ? (y < z ? b : (x < z ? c : a)) : (y > z ? b : (x > z ? c
                : a));
    }

    private static int med3(char[] array, int a, int b, int c) {
        char x = array[a], y = array[b], z = array[c];
        return x < y ? (y < z ? b : (x < z ? c : a)) : (y > z ? b : (x > z ? c
                : a));
    }

    private static int med3(double[] array, int a, int b, int c) {
        double x = array[a], y = array[b], z = array[c];
        return lessThan(x, y) ? (lessThan(y, z) ? b : (lessThan(x, z) ? c : a))
                : (lessThan(z, y) ? b : (lessThan(z, x) ? c : a));
    }

    private static int med3(float[] array, int a, int b, int c) {
        float x = array[a], y = array[b], z = array[c];
        return lessThan(x, y) ? (lessThan(y, z) ? b : (lessThan(x, z) ? c : a))
                : (lessThan(z, y) ? b : (lessThan(z, x) ? c : a));
    }

    private static int med3(int[] array, int a, int b, int c) {
        int x = array[a], y = array[b], z = array[c];
        return x < y ? (y < z ? b : (x < z ? c : a)) : (y > z ? b : (x > z ? c
                : a));
    }

    private static int med3(long[] array, int a, int b, int c) {
        long x = array[a], y = array[b], z = array[c];
        return x < y ? (y < z ? b : (x < z ? c : a)) : (y > z ? b : (x > z ? c
                : a));
    }

    private static int med3(short[] array, int a, int b, int c) {
        short x = array[a], y = array[b], z = array[c];
        return x < y ? (y < z ? b : (x < z ? c : a)) : (y > z ? b : (x > z ? c
                : a));
    }

    /**
     * Sorts the specified array in ascending numerical order.
     * 
     * @param array
     *            the {@code byte} array to be sorted.
     */
    public static void sort(byte[] array) {
        sort(array, 0, array.length);
    }

    /**
     * Sorts the specified range in the array in ascending numerical order.
     * 
     * @param array
     *            the {@code byte} array to be sorted.
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void sort(byte[] array, int start, int end) {
        if (array == null) {
            throw new NullPointerException();
        }
        checkBounds(array.length, start, end);
        sort(start, end, array);
    }

    private static void checkBounds(int arrLength, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException("Start index (" + start
                + ") is greater than end index (" + end + ")");
        }
        if (start < 0) {
            throw new ArrayIndexOutOfBoundsException("Array index out of range: " + start);
        }
        if (end > arrLength) {
            throw new ArrayIndexOutOfBoundsException("Array index out of range: " + end);
        }
    }

    private static void sort(int start, int end, byte[] array) {
        byte temp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1] > array[j]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        byte partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b] <= partionValue) {
                if (array[b] == partionValue) {
                    temp = array[a];
                    array[a++] = array[b];
                    array[b] = temp;
                }
                b++;
            }
            while (c >= b && array[c] >= partionValue) {
                if (array[c] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d--] = temp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b++] = array[c];
            array[c--] = temp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l++] = array[h];
            array[h++] = temp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l++] = array[h];
            array[h++] = temp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array);
        }
    }

    /**
     * Sorts the specified array in ascending numerical order.
     * 
     * @param array
     *            the {@code char} array to be sorted.
     */
    public static void sort(char[] array) {
        sort(array, 0, array.length);
    }

    /**
     * Sorts the specified range in the array in ascending numerical order.
     * 
     * @param array
     *            the {@code char} array to be sorted.
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void sort(char[] array, int start, int end) {
        if (array == null) {
            throw new NullPointerException();
        }
        checkBounds(array.length, start, end);
        sort(start, end, array);
    }

    private static void sort(int start, int end, char[] array) {
        char temp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1] > array[j]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        char partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b] <= partionValue) {
                if (array[b] == partionValue) {
                    temp = array[a];
                    array[a++] = array[b];
                    array[b] = temp;
                }
                b++;
            }
            while (c >= b && array[c] >= partionValue) {
                if (array[c] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d--] = temp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b++] = array[c];
            array[c--] = temp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l++] = array[h];
            array[h++] = temp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l++] = array[h];
            array[h++] = temp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array);
        }
    }

    /**
     * Sorts the specified array in ascending numerical order.
     * 
     * @param array
     *            the {@code double} array to be sorted.
     * @see #sort(double[], int, int)
     */
    public static void sort(double[] array) {
        sort(array, 0, array.length);
    }

    /**
     * Sorts the specified range in the array in ascending numerical order. The
     * values are sorted according to the order imposed by {@code Double.compareTo()}.
     * 
     * @param array
     *            the {@code double} array to be sorted.
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     * @see Double#compareTo(Double)
     */
    public static void sort(double[] array, int start, int end) {
        if (array == null) {
            throw new NullPointerException();
        }
        checkBounds(array.length, start, end);
        sort(start, end, array);
    }

    private static void sort(int start, int end, double[] array) {
        double temp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && lessThan(array[j], array[j - 1]); j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        double partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && !lessThan(partionValue, array[b])) {
                if (isSame(array[b], partionValue)) {
                    temp = array[a];
                    array[a++] = array[b];
                    array[b] = temp;
                }
                b++;
            }
            while (c >= b && !lessThan(array[c], partionValue)) {
                if (isSame(array[c], partionValue)) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d--] = temp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b++] = array[c];
            array[c--] = temp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l++] = array[h];
            array[h++] = temp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l++] = array[h];
            array[h++] = temp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array);
        }
    }

    /**
     * Sorts the specified array in ascending numerical order.
     * 
     * @param array
     *            the {@code float} array to be sorted.
     * @see #sort(float[], int, int)
     */
    public static void sort(float[] array) {
        sort(array, 0, array.length);
    }

    /**
     * Sorts the specified range in the array in ascending numerical order. The
     * values are sorted according to the order imposed by {@code Float.compareTo()}.
     * 
     * @param array
     *            the {@code float} array to be sorted.
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     * @see Float#compareTo(Float)
     */
    public static void sort(float[] array, int start, int end) {
        if (array == null) {
            throw new NullPointerException();
        }
        checkBounds(array.length, start, end);
        sort(start, end, array);
    }

    private static void sort(int start, int end, float[] array) {
        float temp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && lessThan(array[j], array[j - 1]); j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        float partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && !lessThan(partionValue, array[b])) {
                if (isSame(array[b], partionValue)) {
                    temp = array[a];
                    array[a++] = array[b];
                    array[b] = temp;
                }
                b++;
            }
            while (c >= b && !lessThan(array[c], partionValue)) {
                if (isSame(array[c], partionValue)) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d--] = temp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b++] = array[c];
            array[c--] = temp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l++] = array[h];
            array[h++] = temp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l++] = array[h];
            array[h++] = temp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array);
        }
    }

    /**
     * Sorts the specified array in ascending numerical order.
     * 
     * @param array
     *            the {@code int} array to be sorted.
     */
    public static void sort(int[] array) {
        sort(array, 0, array.length);
    }

    /**
     * Sorts the specified range in the array in ascending numerical order.
     * 
     * @param array
     *            the {@code int} array to be sorted.
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void sort(int[] array, int start, int end) {
        if (array == null) {
            throw new NullPointerException();
        }
        checkBounds(array.length, start, end);
        sort(start, end, array);
    }

    private static void sort(int start, int end, int[] array) {
        int temp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1] > array[j]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        int partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b] <= partionValue) {
                if (array[b] == partionValue) {
                    temp = array[a];
                    array[a++] = array[b];
                    array[b] = temp;
                }
                b++;
            }
            while (c >= b && array[c] >= partionValue) {
                if (array[c] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d--] = temp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b++] = array[c];
            array[c--] = temp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l++] = array[h];
            array[h++] = temp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l++] = array[h];
            array[h++] = temp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array);
        }
    }

    /**
     * Sorts the specified array in ascending numerical order.
     * 
     * @param array
     *            the {@code long} array to be sorted.
     */
    public static void sort(long[] array) {
        sort(array, 0, array.length);
    }

    /**
     * Sorts the specified range in the array in ascending numerical order.
     * 
     * @param array
     *            the {@code long} array to be sorted.
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void sort(long[] array, int start, int end) {
        if (array == null) {
            throw new NullPointerException();
        }
        checkBounds(array.length, start, end);
        sort(start, end, array);
    }

    private static void sort(int start, int end, long[] array) {
        long temp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1] > array[j]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        long partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b] <= partionValue) {
                if (array[b] == partionValue) {
                    temp = array[a];
                    array[a++] = array[b];
                    array[b] = temp;
                }
                b++;
            }
            while (c >= b && array[c] >= partionValue) {
                if (array[c] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d--] = temp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b++] = array[c];
            array[c--] = temp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l++] = array[h];
            array[h++] = temp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l++] = array[h];
            array[h++] = temp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array);
        }
    }

    /**
     * Sorts the specified array in ascending natural order.
     * 
     * @param array
     *            the {@code Object} array to be sorted.
     * @throws ClassCastException
     *                if an element in the array does not implement {@code Comparable}
     *                or if some elements cannot be compared to each other.
     * @see #sort(Object[], int, int)
     */
    public static void sort(Object[] array) {
        sort(array, 0, array.length);
    }

    /**
     * Sorts the specified range in the array in ascending natural order. All
     * elements must implement the {@code Comparable} interface and must be
     * comparable to each other without a {@code ClassCastException} being
     * thrown.
     * 
     * @param array
     *            the {@code Object} array to be sorted.
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @throws ClassCastException
     *                if an element in the array does not implement {@code Comparable}
     *                or some elements cannot be compared to each other.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void sort(Object[] array, int start, int end) {
        if (array == null) {
            throw new NullPointerException();
        }
        checkBounds(array.length, start, end);
        sort(start, end, array);
    }

    private static void sort(int start, int end, Object[] array) {
        int length = end - start;
        if (length <= 0) {
            return;
        }
        if (array instanceof String[]) {
            stableStringSort((String[]) array, start, end);
        } else {
            Object[] out = new Object[end];
            System.arraycopy(array, start, out, start, length);
            mergeSort(out, array, start, end);
        }
    }

    /**
     * Swaps the elements at the given indices in the array.
     * 
     * @param a -
     *            the index of one element to be swapped.
     * @param b -
     *            the index of the other element to be swapped.
     * @param arr -
     *            the array in which to swap elements.
     */
    private static void swap(int a, int b, Object[] arr) {
        Object tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    /**
     * Performs a sort on the section of the array between the given indices
     * using a mergesort with exponential search algorithm (in which the merge
     * is performed by exponential search). n*log(n) performance is guaranteed
     * and in the average case it will be faster then any mergesort in which the
     * merge is performed by linear search.
     * 
     * @param in -
     *            the array for sorting.
     * @param out -
     *            the result, sorted array.
     * @param start
     *            the start index
     * @param end
     *            the end index + 1
     */
    @SuppressWarnings("unchecked")
    private static void mergeSort(Object[] in, Object[] out, int start,
            int end) {
        int len = end - start;
        // use insertion sort for small arrays
        if (len <= SIMPLE_LENGTH) {
            for (int i = start + 1; i < end; i++) {
                Comparable<Object> current = (Comparable<Object>) out[i];
                Object prev = out[i - 1];
                if (current.compareTo(prev) < 0) {
                    int j = i;
                    do {
                        out[j--] = prev;
                    } while (j > start
                            && current.compareTo(prev = out[j - 1]) < 0);
                    out[j] = current;
                }
            }
            return;
        }
        int med = (end + start) >>> 1;
        mergeSort(out, in, start, med);
        mergeSort(out, in, med, end);

        // merging

        // if arrays are already sorted - no merge
        if (((Comparable<Object>) in[med - 1]).compareTo(in[med]) <= 0) {
            System.arraycopy(in, start, out, start, len);
            return;
        }
        int r = med, i = start;

        // use merging with exponential search
        do {
            Comparable<Object> fromVal = (Comparable<Object>) in[start];
            Comparable<Object> rVal = (Comparable<Object>) in[r];
            if (fromVal.compareTo(rVal) <= 0) {
                int l_1 = find(in, rVal, -1, start + 1, med - 1);
                int toCopy = l_1 - start + 1;
                System.arraycopy(in, start, out, i, toCopy);
                i += toCopy;
                out[i++] = rVal;
                r++;
                start = l_1 + 1;
            } else {
                int r_1 = find(in, fromVal, 0, r + 1, end - 1);
                int toCopy = r_1 - r + 1;
                System.arraycopy(in, r, out, i, toCopy);
                i += toCopy;
                out[i++] = fromVal;
                start++;
                r = r_1 + 1;
            }
        } while ((end - r) > 0 && (med - start) > 0);

        // copy rest of array
        if ((end - r) <= 0) {
            System.arraycopy(in, start, out, i, med - start);
        } else {
            System.arraycopy(in, r, out, i, end - r);
        }
    }

    /**
     * Performs a sort on the section of the array between the given indices
     * using a mergesort with exponential search algorithm (in which the merge
     * is performed by exponential search). n*log(n) performance is guaranteed
     * and in the average case it will be faster then any mergesort in which the
     * merge is performed by linear search.
     * 
     * @param in -
     *            the array for sorting.
     * @param out -
     *            the result, sorted array.
     * @param start
     *            the start index
     * @param end
     *            the end index + 1
     * @param c -
     *            the comparator to determine the order of the array.
     */
    @SuppressWarnings("unchecked")
    private static void mergeSort(Object[] in, Object[] out, int start,
            int end, Comparator c) {
        int len = end - start;
        // use insertion sort for small arrays
        if (len <= SIMPLE_LENGTH) {
            for (int i = start + 1; i < end; i++) {
                Object current = out[i];
                Object prev = out[i - 1];
                if (c.compare(prev, current) > 0) {
                    int j = i;
                    do {
                        out[j--] = prev;
                    } while (j > start
                            && (c.compare(prev = out[j - 1], current) > 0));
                    out[j] = current;
                }
            }
            return;
        }
        int med = (end + start) >>> 1;
        mergeSort(out, in, start, med, c);
        mergeSort(out, in, med, end, c);

        // merging

        // if arrays are already sorted - no merge
        if (c.compare(in[med - 1],in[med] ) <= 0) {
            System.arraycopy(in, start, out, start, len);
            return;
        }
        int r = med, i = start;

        // use merging with exponential search
        do {
            Object fromVal = in[start];
            Object rVal = in[r];
            if (c.compare(fromVal, rVal) <= 0) {
                int l_1 = find(in, rVal, -1, start + 1, med - 1, c);
                int toCopy = l_1 - start + 1;
                System.arraycopy(in, start, out, i, toCopy);
                i += toCopy;
                out[i++] = rVal;
                r++;
                start = l_1 + 1;
            } else {
                int r_1 = find(in, fromVal, 0, r + 1, end - 1, c);
                int toCopy = r_1 - r + 1;
                System.arraycopy(in, r, out, i, toCopy);
                i += toCopy;
                out[i++] = fromVal;
                start++;
                r = r_1 + 1;
            }
        } while ((end - r) > 0 && (med - start) > 0);

        // copy rest of array
        if ((end - r) <= 0) {
            System.arraycopy(in, start, out, i, med - start);
        } else {
            System.arraycopy(in, r, out, i, end - r);
        }
    }

    /**
     * Finds the place in the given range of specified sorted array, where the
     * element should be inserted for getting sorted array. Uses exponential
     * search algorithm.
     * 
     * @param arr -
     *            the array with already sorted range
     * 
     * @param val -
     *            object to be inserted
     * 
     * @param l -
     *            the start index
     * 
     * @param r -
     *            the end index
     * 
     * @param bnd -
     *            possible values 0,-1. "-1" - val is located at index more then
     *            elements equals to val. "0" - val is located at index less
     *            then elements equals to val.
     * 
     */
    @SuppressWarnings("unchecked")
    private static int find(Object[] arr, Comparable val, int bnd, int l, int r) {
        int m = l;
        int d = 1;
        while (m <= r) {
            if (val.compareTo(arr[m]) > bnd) {
                l = m + 1;
            } else {
                r = m - 1;
                break;
            }
            m += d;
            d <<= 1;
        }
        while (l <= r) {
            m = (l + r) >>> 1;
            if (val.compareTo(arr[m]) > bnd) {
                l = m + 1;
            } else {
                r = m - 1;
            }
        }
        return l - 1;
    }

    /**
     * Finds the place of specified range of specified sorted array, where the
     * element should be inserted for getting sorted array. Uses exponential
     * search algorithm.
     * 
     * @param arr -
     *            the array with already sorted range
     * @param val -
     *            object to be inserted
     * @param l -
     *            the start index
     * @param r -
     *            the end index
     * @param bnd -
     *            possible values 0,-1. "-1" - val is located at index more then
     *            elements equals to val. "0" - val is located at index less
     *            then elements equals to val.
     * @param c -
     *            the comparator used to compare Objects
     */
    @SuppressWarnings("unchecked")
    private static int find(Object[] arr, Object val, int bnd, int l, int r,
            Comparator c) {
        int m = l;
        int d = 1;
        while (m <= r) {
            if (c.compare(val, arr[m]) > bnd) {
                l = m + 1;
            } else {
                r = m - 1;
                break;
            }
            m += d;
            d <<= 1;
        }
        while (l <= r) {
            m = (l + r) >>> 1;
            if (c.compare(val, arr[m]) > bnd) {
                l = m + 1;
            } else {
                r = m - 1;
            }
        }
        return l - 1;
    }

    /*
     * returns the median index.
     */
    private static int medChar(int a, int b, int c, String[] arr, int id) {
        int ac = charAt(arr[a], id);
        int bc = charAt(arr[b], id);
        int cc = charAt(arr[c], id);
        return ac < bc ? (bc < cc ? b : (ac < cc ? c : a))
                : (bc < cc ? (ac < cc ? a : c) : b);

    }

    /*
     * Returns the char value at the specified index of string or -1 if the
     * index more than the length of this string.
     */
    private static int charAt(String str, int i) {
        if (i >= str.length()) {
            return -1;
        }
        return str.charAt(i);
    }

    /**
     * Copies object from one array to another array with reverse of objects
     * order. Source and destination arrays may be the same.
     * 
     * @param src -
     *            the source array.
     * @param from -
     *            starting position in the source array.
     * @param dst -
     *            the destination array.
     * @param to -
     *            starting position in the destination array.
     * @param len -
     *            the number of array elements to be copied.
     */
    private static void copySwap(Object[] src, int from, Object[] dst, int to,
            int len) {
        if (src == dst && from + len > to) {
            int new_to = to + len - 1;
            for (; from < to; from++, new_to--, len--) {
                dst[new_to] = src[from];
            }
            for (; len > 1; from++, new_to--, len -= 2) {
                swap(from, new_to, dst);
            }

        } else {
            to = to + len - 1;
            for (; len > 0; from++, to--, len--) {
                dst[to] = src[from];
            }
        }
    }

    /**
     * Performs a sort on the given String array. Elements will be re-ordered into
     * ascending order.
     * 
     * @param arr -
     *            the array to sort
     * @param start -
     *            the start index
     * @param end -
     *            the end index + 1
     */
    private static void stableStringSort(String[] arr, int start,
            int end) {
        stableStringSort(arr, arr, new String[end], start, end, 0);
    }

    /**
     * Performs a sort on the given String array. Elements will be re-ordered into
     * ascending order. Uses a stable ternary quick sort algorithm.
     * 
     * @param arr -
     *            the array to sort
     * @param src -
     *            auxiliary array
     * @param dst -
     *            auxiliary array
     * @param start -
     *            the start index
     * @param end -
     *            the end index + 1
     * @param chId -
     *            index of char for current sorting
     */
    private static void stableStringSort(String[] arr, String[] src,
            String[] dst, int start, int end, int chId) {
        int length = end - start;
        // use insertion sort for small arrays
        if (length < SIMPLE_LENGTH) {
            if (src == arr) {
                for (int i = start + 1; i < end; i++) {
                    String current = arr[i];
                    String prev = arr[i - 1];
                    if (current.compareTo(prev) < 0) {
                        int j = i;
                        do {
                            arr[j--] = prev;
                        } while (j > start
                                && current.compareTo(prev = arr[j - 1]) < 0);
                        arr[j] = current;
                    }
                }
            } else {
                int actualEnd = end - 1;
                dst[start] = src[actualEnd--];
                for (int i = start + 1; i < end; i++, actualEnd--) {
                    String current = src[actualEnd];
                    String prev;
                    int j = i;
                    while (j > start
                            && current.compareTo(prev = dst[j - 1]) < 0) {
                        dst[j--] = prev;
                    }
                    dst[j] = current;
                }
            }
            return;
        }
        // Approximate median
        int s;
        int mid = start + length / 2;
        int lo = start;
        int hi = end - 1;
        if (length > 40) {
            s = length / 8;
            lo = medChar(lo, lo + s, lo + s * 2, src, chId);
            mid = medChar(mid - s, mid, mid + s, src, chId);
            hi = medChar(hi, hi - s, hi - s * 2, src, chId);
        }
        mid = medChar(lo, mid, hi, src, chId);
        // median found
        // create 4 pointers <a (in star of src) ,
        // =b(in start of dst), >c (in end of dst)
        // i - current element;
        int midVal = charAt(src[mid], chId);
        int a, b, c;
        a = b = start;
        c = end - 1;
        int cmp;

        for (int i = start; i < end; i++) {
            String el = src[i];
            cmp = charAt(el, chId) - midVal;
            if (cmp < 0) {
                src[a] = el;
                a++;
            } else if (cmp > 0) {
                dst[c] = el;
                c--;
            } else {
                dst[b] = el;
                b++;
            }
        }

        s = b - start;
        if (s > 0) {
            if (arr == src) {
                System.arraycopy(dst, start, arr, a, s);
            } else {
                copySwap(dst, start, arr, a, s);
            }

            if (b >= end && midVal == -1) {
                return;
            }
            stableStringSort(arr, arr, arr == dst ? src : dst, a, a + s,
                    chId + 1);
        }

        s = a - start;
        if (s > 0) {
            stableStringSort(arr, src, dst, start, a, chId);
        }

        c++;
        s = end - c;
        if (s > 0) {
            stableStringSort(arr, dst, src, c, end, chId);
        }
    }

    /**
     * Sorts the specified range in the array using the specified {@code Comparator}.
     * All elements must be comparable to each other without a
     * {@code ClassCastException} being thrown.
     *
     * @param array
     *            the {@code Object} array to be sorted.
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @param comparator
     *            the {@code Comparator}.
     * @throws ClassCastException
     *                if elements in the array cannot be compared to each other
     *                using the {@code Comparator}.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static <T> void sort(T[] array, int start, int end,
            Comparator<? super T> comparator) {
        if (array == null) {
            throw new NullPointerException();
        }
        checkBounds(array.length, start, end);
        sort(start, end, array, comparator);
    }

    private static <T> void sort(int start, int end, T[] array,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            sort(start, end, array);
        } else {
            int length = end - start;
            Object[] out = new Object[end];
            System.arraycopy(array, start, out, start, length);
            mergeSort(out, array, start, end, comparator);
        }
    }

    /**
     * Sorts the specified array using the specified {@code Comparator}. All elements
     * must be comparable to each other without a {@code ClassCastException} being thrown.
     * 
     * @param array
     *            the {@code Object} array to be sorted.
     * @param comparator
     *            the {@code Comparator}.
     * @throws ClassCastException
     *                if elements in the array cannot be compared to each other
     *                using the {@code Comparator}.
     */
    public static <T> void sort(T[] array, Comparator<? super T> comparator) {
        sort(0, array.length, array, comparator);
    }

    /**
     * Sorts the specified array in ascending numerical order.
     * 
     * @param array
     *            the {@code short} array to be sorted.
     */
    public static void sort(short[] array) {
        sort(array, 0, array.length);
    }

    /**
     * Sorts the specified range in the array in ascending numerical order.
     * 
     * @param array
     *            the {@code short} array to be sorted.
     * @param start
     *            the start index to sort.
     * @param end
     *            the last + 1 index to sort.
     * @throws IllegalArgumentException
     *                if {@code start > end}.
     * @throws ArrayIndexOutOfBoundsException
     *                if {@code start < 0} or {@code end > array.length}.
     */
    public static void sort(short[] array, int start, int end) {
        if (array == null) {
            throw new NullPointerException();
        }
        checkBounds(array.length, start, end);
        sort(start, end, array);
    }

    private static void sort(int start, int end, short[] array) {
        short temp;
        int length = end - start;
        if (length < 7) {
            for (int i = start + 1; i < end; i++) {
                for (int j = i; j > start && array[j - 1] > array[j]; j--) {
                    temp = array[j];
                    array[j] = array[j - 1];
                    array[j - 1] = temp;
                }
            }
            return;
        }
        int middle = (start + end) / 2;
        if (length > 7) {
            int bottom = start;
            int top = end - 1;
            if (length > 40) {
                length /= 8;
                bottom = med3(array, bottom, bottom + length, bottom
                        + (2 * length));
                middle = med3(array, middle - length, middle, middle + length);
                top = med3(array, top - (2 * length), top - length, top);
            }
            middle = med3(array, bottom, middle, top);
        }
        short partionValue = array[middle];
        int a, b, c, d;
        a = b = start;
        c = d = end - 1;
        while (true) {
            while (b <= c && array[b] <= partionValue) {
                if (array[b] == partionValue) {
                    temp = array[a];
                    array[a++] = array[b];
                    array[b] = temp;
                }
                b++;
            }
            while (c >= b && array[c] >= partionValue) {
                if (array[c] == partionValue) {
                    temp = array[c];
                    array[c] = array[d];
                    array[d--] = temp;
                }
                c--;
            }
            if (b > c) {
                break;
            }
            temp = array[b];
            array[b++] = array[c];
            array[c--] = temp;
        }
        length = a - start < b - a ? a - start : b - a;
        int l = start;
        int h = b - length;
        while (length-- > 0) {
            temp = array[l];
            array[l++] = array[h];
            array[h++] = temp;
        }
        length = d - c < end - 1 - d ? d - c : end - 1 - d;
        l = b;
        h = end - length;
        while (length-- > 0) {
            temp = array[l];
            array[l++] = array[h];
            array[h++] = temp;
        }
        if ((length = b - a) > 0) {
            sort(start, start + length, array);
        }
        if ((length = d - c) > 0) {
            sort(end - length, end, array);
        }
    }

    /**
     * Creates a {@code String} representation of the {@code boolean[]} passed.
     * The result is surrounded by brackets ({@code &quot;[]&quot;}), each
     * element is converted to a {@code String} via the
     * {@link String#valueOf(boolean)} and separated by {@code &quot;, &quot;}.
     * If the array is {@code null}, then {@code &quot;null&quot;} is returned.
     *
     * @param array
     *            the {@code boolean} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(boolean[] array) {
        if (array == null) {
            return "null"; //$NON-NLS-1$
        }
        if (array.length == 0) {
            return "[]"; //$NON-NLS-1$
        }
        StringBuilder sb = new StringBuilder(2 + array.length * 5);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", "); //$NON-NLS-1$
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a {@code String} representation of the {@code byte[]} passed. The
     * result is surrounded by brackets ({@code &quot;[]&quot;}), each element
     * is converted to a {@code String} via the {@link String#valueOf(int)} and
     * separated by {@code &quot;, &quot;}. If the array is {@code null}, then
     * {@code &quot;null&quot;} is returned.
     *
     * @param array
     *            the {@code byte} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(byte[] array) {
        if (array == null) {
            return "null"; //$NON-NLS-1$
        }
        if (array.length == 0) {
            return "[]"; //$NON-NLS-1$
        }
        StringBuilder sb = new StringBuilder(2 + array.length * 3);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", "); //$NON-NLS-1$
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a {@code String} representation of the {@code char[]} passed. The
     * result is surrounded by brackets ({@code &quot;[]&quot;}), each element
     * is converted to a {@code String} via the {@link String#valueOf(char)} and
     * separated by {@code &quot;, &quot;}. If the array is {@code null}, then
     * {@code &quot;null&quot;} is returned.
     *
     * @param array
     *            the {@code char} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(char[] array) {
        if (array == null) {
            return "null"; //$NON-NLS-1$
        }
        if (array.length == 0) {
            return "[]"; //$NON-NLS-1$
        }
        StringBuilder sb = new StringBuilder(2 + array.length * 2);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", "); //$NON-NLS-1$
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a {@code String} representation of the {@code double[]} passed.
     * The result is surrounded by brackets ({@code &quot;[]&quot;}), each
     * element is converted to a {@code String} via the
     * {@link String#valueOf(double)} and separated by {@code &quot;, &quot;}.
     * If the array is {@code null}, then {@code &quot;null&quot;} is returned.
     *
     * @param array
     *            the {@code double} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(double[] array) {
        if (array == null) {
            return "null"; //$NON-NLS-1$
        }
        if (array.length == 0) {
            return "[]"; //$NON-NLS-1$
        }
        StringBuilder sb = new StringBuilder(2 + array.length * 5);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", "); //$NON-NLS-1$
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a {@code String} representation of the {@code float[]} passed.
     * The result is surrounded by brackets ({@code &quot;[]&quot;}), each
     * element is converted to a {@code String} via the
     * {@link String#valueOf(float)} and separated by {@code &quot;, &quot;}.
     * If the array is {@code null}, then {@code &quot;null&quot;} is returned.
     *
     * @param array
     *            the {@code float} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(float[] array) {
        if (array == null) {
            return "null"; //$NON-NLS-1$
        }
        if (array.length == 0) {
            return "[]"; //$NON-NLS-1$
        }
        StringBuilder sb = new StringBuilder(2 + array.length * 5);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", "); //$NON-NLS-1$
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a {@code String} representation of the {@code int[]} passed. The
     * result is surrounded by brackets ({@code &quot;[]&quot;}), each element
     * is converted to a {@code String} via the {@link String#valueOf(int)} and
     * separated by {@code &quot;, &quot;}. If the array is {@code null}, then
     * {@code &quot;null&quot;} is returned.
     *
     * @param array
     *            the {@code int} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(int[] array) {
        if (array == null) {
            return "null"; //$NON-NLS-1$
        }
        if (array.length == 0) {
            return "[]"; //$NON-NLS-1$
        }
        StringBuilder sb = new StringBuilder(2 + array.length * 4);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", "); //$NON-NLS-1$
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a {@code String} representation of the {@code long[]} passed. The
     * result is surrounded by brackets ({@code &quot;[]&quot;}), each element
     * is converted to a {@code String} via the {@link String#valueOf(long)} and
     * separated by {@code &quot;, &quot;}. If the array is {@code null}, then
     * {@code &quot;null&quot;} is returned.
     *
     * @param array
     *            the {@code long} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(long[] array) {
        if (array == null) {
            return "null"; //$NON-NLS-1$
        }
        if (array.length == 0) {
            return "[]"; //$NON-NLS-1$
        }
        StringBuilder sb = new StringBuilder(2 + array.length * 4);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", "); //$NON-NLS-1$
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a {@code String} representation of the {@code short[]} passed.
     * The result is surrounded by brackets ({@code &quot;[]&quot;}), each
     * element is converted to a {@code String} via the
     * {@link String#valueOf(int)} and separated by {@code &quot;, &quot;}. If
     * the array is {@code null}, then {@code &quot;null&quot;} is returned.
     *
     * @param array
     *            the {@code short} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(short[] array) {
        if (array == null) {
            return "null"; //$NON-NLS-1$
        }
        if (array.length == 0) {
            return "[]"; //$NON-NLS-1$
        }
        StringBuilder sb = new StringBuilder(2 + array.length * 4);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", "); //$NON-NLS-1$
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a {@code String} representation of the {@code Object[]} passed.
     * The result is surrounded by brackets ({@code &quot;[]&quot;}), each
     * element is converted to a {@code String} via the
     * {@link String#valueOf(Object)} and separated by {@code &quot;, &quot;}.
     * If the array is {@code null}, then {@code &quot;null&quot;} is returned.
     *
     * @param array
     *            the {@code Object} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String toString(Object[] array) {
        if (array == null) {
            return "null"; //$NON-NLS-1$
        }
        if (array.length == 0) {
            return "[]"; //$NON-NLS-1$
        }
        StringBuilder sb = new StringBuilder(2 + array.length * 5);
        sb.append('[');
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++) {
            sb.append(", "); //$NON-NLS-1$
            sb.append(array[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Creates a <i>"deep"</i> {@code String} representation of the
     * {@code Object[]} passed, such that if the array contains other arrays,
     * the {@code String} representation of those arrays is generated as well.
     * <p>
     * If any of the elements are primitive arrays, the generation is delegated
     * to the other {@code toString} methods in this class. If any element
     * contains a reference to the original array, then it will be represented
     * as {@code "[...]"}. If an element is an {@code Object[]}, then its
     * representation is generated by a recursive call to this method. All other
     * elements are converted via the {@link String#valueOf(Object)} method.
     *
     * @param array
     *            the {@code Object} array to convert.
     * @return the {@code String} representation of {@code array}.
     * @since 1.5
     */
    public static String deepToString(Object[] array) {
      // Special case null to prevent NPE
      if (array == null) {
          return "null";
      }
      // delegate this to the recursive method
      StringBuilder buf = new StringBuilder(array.length * 9);
      deepToStringImpl(array, new Object[] { array }, buf);
      return buf.toString();
    }

    /**
     * Implementation method used by {@link #deepToString(Object[])}.
     *
     * @param array
     *            the {@code Object[]} to dive into.
     * @param origArrays
     *            the original {@code Object[]}; used to test for self
     *            references.
     * @param sb
     *            the {@code StringBuilder} instance to append to or
     *            {@code null} one hasn't been created yet.
     * @return the result.
     * @see #deepToString(Object[])
     */
    private static void deepToStringImpl(Object[] array, Object[] origArrays,
            StringBuilder sb) {
        if (array == null) {
            sb.append("null");
            return;
        }

        sb.append('[');

        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            // establish current element
            Object elem = array[i];
            if (elem == null) {
                // element is null
                sb.append("null");
            } else {
                // get the Class of the current element
                Class<?> elemClass = elem.getClass();
                if (elemClass.isArray()) {
                    // element is an array type

                    // get the declared Class of the array (element)
                    Class<?> elemElemClass = elemClass.getComponentType();
                    if (elemElemClass.isPrimitive()) {
                        // element is a primitive array
                        if (boolean.class.equals(elemElemClass)) {
                            sb.append(toString((boolean[]) elem));
                        } else if (byte.class.equals(elemElemClass)) {
                            sb.append(toString((byte[]) elem));
                        } else if (char.class.equals(elemElemClass)) {
                            sb.append(toString((char[]) elem));
                        } else if (double.class.equals(elemElemClass)) {
                            sb.append(toString((double[]) elem));
                        } else if (float.class.equals(elemElemClass)) {
                            sb.append(toString((float[]) elem));
                        } else if (int.class.equals(elemElemClass)) {
                            sb.append(toString((int[]) elem));
                        } else if (long.class.equals(elemElemClass)) {
                            sb.append(toString((long[]) elem));
                        } else if (short.class.equals(elemElemClass)) {
                            sb.append(toString((short[]) elem));
                        } else {
                            // no other possible primitives, so we assert that
                            throw new AssertionError();
                        }
                    } else {
                        // element is an Object[], so we assert that
                        assert elem instanceof Object[];
                        if (deepToStringImplContains(origArrays, elem)) {
                            sb.append("[...]");
                        } else {
                            Object[] newArray = (Object[]) elem;
                            Object[] newOrigArrays = new Object[origArrays.length + 1];
                            System.arraycopy(origArrays, 0, newOrigArrays, 0,
                                    origArrays.length);
                            newOrigArrays[origArrays.length] = newArray;
                            // make the recursive call to this method
                            deepToStringImpl(newArray, newOrigArrays, sb);
                        }
                    }
                } else { // element is NOT an array, just an Object
                    sb.append(array[i]);
                }
            }
        }
        sb.append(']');
    }

    /**
     * Utility method used to assist the implementation of
     * {@link #deepToString(Object[])}.
     *
     * @param origArrays
     *            An array of Object[] references.
     * @param array
     *            An Object[] reference to look for in {@code origArrays}.
     * @return A value of {@code true} if {@code array} is an
     *         element in {@code origArrays}.
     */
    private static boolean deepToStringImplContains(Object[] origArrays,
            Object array) {
        if (origArrays == null || origArrays.length == 0) {
            return false;
        }
        for (Object element : origArrays) {
            if (element == array) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copies {@code newLength} elements from {@code original} into a new array.
     * If {@code newLength} is greater than {@code original.length}, the result is padded
     * with the value {@code false}.
     *
     * @param original the original array
     * @param newLength the length of the new array
     * @return the new array
     * @throws NegativeArraySizeException if {@code newLength < 0}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static boolean[] copyOf(boolean[] original, int newLength) {
        if (newLength < 0) {
            throw new NegativeArraySizeException();
        }
        return copyOfRange(original, 0, newLength);
    }

    /**
     * Copies {@code newLength} elements from {@code original} into a new array.
     * If {@code newLength} is greater than {@code original.length}, the result is padded
     * with the value {@code (byte) 0}.
     *
     * @param original the original array
     * @param newLength the length of the new array
     * @return the new array
     * @throws NegativeArraySizeException if {@code newLength < 0}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static byte[] copyOf(byte[] original, int newLength) {
        if (newLength < 0) {
            throw new NegativeArraySizeException();
        }
        return copyOfRange(original, 0, newLength);
    }

    /**
     * Copies {@code newLength} elements from {@code original} into a new array.
     * If {@code newLength} is greater than {@code original.length}, the result is padded
     * with the value {@code '\\u0000'}.
     *
     * @param original the original array
     * @param newLength the length of the new array
     * @return the new array
     * @throws NegativeArraySizeException if {@code newLength < 0}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static char[] copyOf(char[] original, int newLength) {
        if (newLength < 0) {
            throw new NegativeArraySizeException();
        }
        return copyOfRange(original, 0, newLength);
    }

    /**
     * Copies {@code newLength} elements from {@code original} into a new array.
     * If {@code newLength} is greater than {@code original.length}, the result is padded
     * with the value {@code 0.0d}.
     *
     * @param original the original array
     * @param newLength the length of the new array
     * @return the new array
     * @throws NegativeArraySizeException if {@code newLength < 0}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static double[] copyOf(double[] original, int newLength) {
        if (newLength < 0) {
            throw new NegativeArraySizeException();
        }
        return copyOfRange(original, 0, newLength);
    }

    /**
     * Copies {@code newLength} elements from {@code original} into a new array.
     * If {@code newLength} is greater than {@code original.length}, the result is padded
     * with the value {@code 0.0f}.
     *
     * @param original the original array
     * @param newLength the length of the new array
     * @return the new array
     * @throws NegativeArraySizeException if {@code newLength < 0}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static float[] copyOf(float[] original, int newLength) {
        if (newLength < 0) {
            throw new NegativeArraySizeException();
        }
        return copyOfRange(original, 0, newLength);
    }

    /**
     * Copies {@code newLength} elements from {@code original} into a new array.
     * If {@code newLength} is greater than {@code original.length}, the result is padded
     * with the value {@code 0}.
     *
     * @param original the original array
     * @param newLength the length of the new array
     * @return the new array
     * @throws NegativeArraySizeException if {@code newLength < 0}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static int[] copyOf(int[] original, int newLength) {
        if (newLength < 0) {
            throw new NegativeArraySizeException();
        }
        return copyOfRange(original, 0, newLength);
    }

    /**
     * Copies {@code newLength} elements from {@code original} into a new array.
     * If {@code newLength} is greater than {@code original.length}, the result is padded
     * with the value {@code 0L}.
     *
     * @param original the original array
     * @param newLength the length of the new array
     * @return the new array
     * @throws NegativeArraySizeException if {@code newLength < 0}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static long[] copyOf(long[] original, int newLength) {
        if (newLength < 0) {
            throw new NegativeArraySizeException();
        }
        return copyOfRange(original, 0, newLength);
    }

    /**
     * Copies {@code newLength} elements from {@code original} into a new array.
     * If {@code newLength} is greater than {@code original.length}, the result is padded
     * with the value {@code (short) 0}.
     *
     * @param original the original array
     * @param newLength the length of the new array
     * @return the new array
     * @throws NegativeArraySizeException if {@code newLength < 0}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static short[] copyOf(short[] original, int newLength) {
        if (newLength < 0) {
            throw new NegativeArraySizeException();
        }
        return copyOfRange(original, 0, newLength);
    }

    /**
     * Copies {@code newLength} elements from {@code original} into a new array.
     * If {@code newLength} is greater than {@code original.length}, the result is padded
     * with the value {@code null}.
     *
     * @param original the original array
     * @param newLength the length of the new array
     * @return the new array
     * @throws NegativeArraySizeException if {@code newLength < 0}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static <T> T[] copyOf(T[] original, int newLength) {
        if (original == null) {
            throw new NullPointerException();
        }
        if (newLength < 0) {
            throw new NegativeArraySizeException();
        }
        return copyOfRange(original, 0, newLength);
    }

    /**
     * Copies {@code newLength} elements from {@code original} into a new array.
     * If {@code newLength} is greater than {@code original.length}, the result is padded
     * with the value {@code null}.
     *
     * @param original the original array
     * @param newLength the length of the new array
     * @param newType the class of the new array
     * @return the new array
     * @throws NegativeArraySizeException if {@code newLength < 0}
     * @throws NullPointerException if {@code original == null}
     * @throws ArrayStoreException if a value in {@code original} is incompatible with T
     * @since 1.6
     */
    public static <T, U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
        // We use the null pointer check in copyOfRange for exception priority compatibility.
        if (newLength < 0) {
            throw new NegativeArraySizeException();
        }
        return copyOfRange(original, 0, newLength, newType);
    }

    /**
     * Copies elements from {@code original} into a new array, from indexes start (inclusive) to
     * end (exclusive). The original order of elements is preserved.
     * If {@code end} is greater than {@code original.length}, the result is padded
     * with the value {@code false}.
     *
     * @param original the original array
     * @param start the start index, inclusive
     * @param end the end index, exclusive
     * @return the new array
     * @throws ArrayIndexOutOfBoundsException if {@code start < 0 || start > original.length}
     * @throws IllegalArgumentException if {@code start > end}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static boolean[] copyOfRange(boolean[] original, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        int originalLength = original.length;
        if (start < 0 || start > originalLength) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        boolean[] result = new boolean[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    /**
     * Copies elements from {@code original} into a new array, from indexes start (inclusive) to
     * end (exclusive). The original order of elements is preserved.
     * If {@code end} is greater than {@code original.length}, the result is padded
     * with the value {@code (byte) 0}.
     *
     * @param original the original array
     * @param start the start index, inclusive
     * @param end the end index, exclusive
     * @return the new array
     * @throws ArrayIndexOutOfBoundsException if {@code start < 0 || start > original.length}
     * @throws IllegalArgumentException if {@code start > end}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static byte[] copyOfRange(byte[] original, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        int originalLength = original.length;
        if (start < 0 || start > originalLength) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        byte[] result = new byte[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    /**
     * Copies elements from {@code original} into a new array, from indexes start (inclusive) to
     * end (exclusive). The original order of elements is preserved.
     * If {@code end} is greater than {@code original.length}, the result is padded
     * with the value {@code '\\u0000'}.
     *
     * @param original the original array
     * @param start the start index, inclusive
     * @param end the end index, exclusive
     * @return the new array
     * @throws ArrayIndexOutOfBoundsException if {@code start < 0 || start > original.length}
     * @throws IllegalArgumentException if {@code start > end}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static char[] copyOfRange(char[] original, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        int originalLength = original.length;
        if (start < 0 || start > originalLength) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        char[] result = new char[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    /**
     * Copies elements from {@code original} into a new array, from indexes start (inclusive) to
     * end (exclusive). The original order of elements is preserved.
     * If {@code end} is greater than {@code original.length}, the result is padded
     * with the value {@code 0.0d}.
     *
     * @param original the original array
     * @param start the start index, inclusive
     * @param end the end index, exclusive
     * @return the new array
     * @throws ArrayIndexOutOfBoundsException if {@code start < 0 || start > original.length}
     * @throws IllegalArgumentException if {@code start > end}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static double[] copyOfRange(double[] original, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        int originalLength = original.length;
        if (start < 0 || start > originalLength) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        double[] result = new double[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    /**
     * Copies elements from {@code original} into a new array, from indexes start (inclusive) to
     * end (exclusive). The original order of elements is preserved.
     * If {@code end} is greater than {@code original.length}, the result is padded
     * with the value {@code 0.0f}.
     *
     * @param original the original array
     * @param start the start index, inclusive
     * @param end the end index, exclusive
     * @return the new array
     * @throws ArrayIndexOutOfBoundsException if {@code start < 0 || start > original.length}
     * @throws IllegalArgumentException if {@code start > end}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static float[] copyOfRange(float[] original, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        int originalLength = original.length;
        if (start < 0 || start > originalLength) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        float[] result = new float[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    /**
     * Copies elements from {@code original} into a new array, from indexes start (inclusive) to
     * end (exclusive). The original order of elements is preserved.
     * If {@code end} is greater than {@code original.length}, the result is padded
     * with the value {@code 0}.
     *
     * @param original the original array
     * @param start the start index, inclusive
     * @param end the end index, exclusive
     * @return the new array
     * @throws ArrayIndexOutOfBoundsException if {@code start < 0 || start > original.length}
     * @throws IllegalArgumentException if {@code start > end}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static int[] copyOfRange(int[] original, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        int originalLength = original.length;
        if (start < 0 || start > originalLength) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        int[] result = new int[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    /**
     * Copies elements from {@code original} into a new array, from indexes start (inclusive) to
     * end (exclusive). The original order of elements is preserved.
     * If {@code end} is greater than {@code original.length}, the result is padded
     * with the value {@code 0L}.
     *
     * @param original the original array
     * @param start the start index, inclusive
     * @param end the end index, exclusive
     * @return the new array
     * @throws ArrayIndexOutOfBoundsException if {@code start < 0 || start > original.length}
     * @throws IllegalArgumentException if {@code start > end}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static long[] copyOfRange(long[] original, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        int originalLength = original.length;
        if (start < 0 || start > originalLength) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        long[] result = new long[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    /**
     * Copies elements from {@code original} into a new array, from indexes start (inclusive) to
     * end (exclusive). The original order of elements is preserved.
     * If {@code end} is greater than {@code original.length}, the result is padded
     * with the value {@code (short) 0}.
     *
     * @param original the original array
     * @param start the start index, inclusive
     * @param end the end index, exclusive
     * @return the new array
     * @throws ArrayIndexOutOfBoundsException if {@code start < 0 || start > original.length}
     * @throws IllegalArgumentException if {@code start > end}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    public static short[] copyOfRange(short[] original, int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        int originalLength = original.length;
        if (start < 0 || start > originalLength) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        short[] result = new short[resultLength];
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    /**
     * Copies elements from {@code original} into a new array, from indexes start (inclusive) to
     * end (exclusive). The original order of elements is preserved.
     * If {@code end} is greater than {@code original.length}, the result is padded
     * with the value {@code null}.
     *
     * @param original the original array
     * @param start the start index, inclusive
     * @param end the end index, exclusive
     * @return the new array
     * @throws ArrayIndexOutOfBoundsException if {@code start < 0 || start > original.length}
     * @throws IllegalArgumentException if {@code start > end}
     * @throws NullPointerException if {@code original == null}
     * @since 1.6
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] copyOfRange(T[] original, int start, int end) {
        int originalLength = original.length; // For exception priority compatibility.
        if (start > end) {
            throw new IllegalArgumentException();
        }
        if (start < 0 || start > originalLength) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        T[] result = (T[]) Array.newInstance(original.getClass().getComponentType(), resultLength);
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }

    /**
     * Copies elements from {@code original} into a new array, from indexes start (inclusive) to
     * end (exclusive). The original order of elements is preserved.
     * If {@code end} is greater than {@code original.length}, the result is padded
     * with the value {@code null}.
     *
     * @param original the original array
     * @param start the start index, inclusive
     * @param end the end index, exclusive
     * @return the new array
     * @throws ArrayIndexOutOfBoundsException if {@code start < 0 || start > original.length}
     * @throws IllegalArgumentException if {@code start > end}
     * @throws NullPointerException if {@code original == null}
     * @throws ArrayStoreException if a value in {@code original} is incompatible with T
     * @since 1.6
     */
    @SuppressWarnings("unchecked")
    public static <T, U> T[] copyOfRange(U[] original, int start, int end, Class<? extends T[]> newType) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        int originalLength = original.length;
        if (start < 0 || start > originalLength) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int resultLength = end - start;
        int copyLength = Math.min(resultLength, originalLength - start);
        T[] result = (T[]) Array.newInstance(newType.getComponentType(), resultLength);
        System.arraycopy(original, start, result, 0, copyLength);
        return result;
    }
}
