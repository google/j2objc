/*
 * Copyright (C) 2011 The Android Open Source Project
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

package java.util;

import java.lang.reflect.Array;

/**
 * An array-backed list that exposes its array.
 *
 * @hide
 */
public class UnsafeArrayList<T> extends AbstractList<T> {
    private final Class<T> elementType;
    private T[] array;
    private int size;

    public UnsafeArrayList(Class<T> elementType, int initialCapacity) {
        this.array = (T[]) Array.newInstance(elementType, initialCapacity);
        this.elementType = elementType;
    }

    @Override public boolean add(T element) {
        if (size == array.length) {
            T[] newArray = (T[]) Array.newInstance(elementType, size * 2);
            System.arraycopy(array, 0, newArray, 0, size);
            array = newArray;
        }
        array[size++] = element;
        ++modCount;
        return true;
    }

    public T[] array() {
        return array;
    }

    public T get(int i) {
        return array[i];
    }

    public int size() {
        return size;
    }
}
