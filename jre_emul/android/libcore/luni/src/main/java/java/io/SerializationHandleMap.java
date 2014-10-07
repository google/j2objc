/*
 * Copyright (C) 2010 The Android Open Source Project
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

package java.io;

/**
 * A specialization of IdentityHashMap<Object, int> for use when serializing objects.
 * We need to assign each object we write an int 'handle' (densely packed but not starting
 * at zero), and use the same handle any time we write the same object again.
 */
final class SerializationHandleMap {
    /* Default load factor of 0.75; */
    private static final int LOAD_FACTOR = 7500;

    private Object[] keys;
    private int[] values;

    /* Actual number of key-value pairs. */
    private int size;

    /* Maximum number of elements that can be put in this map before having to rehash. */
    private int threshold;

    public SerializationHandleMap() {
        this.size = 0;
        this.threshold = 21; // Copied from IdentityHashMap.
        int arraySize = (int) (((long) threshold * 10000) / LOAD_FACTOR);
        resizeArrays(arraySize);
    }

    private void resizeArrays(int newSize) {
        Object[] oldKeys = keys;
        int[] oldValues = values;

        this.keys = new Object[newSize];
        this.values = new int[newSize];

        if (oldKeys != null) {
            for (int i = 0; i < oldKeys.length; ++i) {
                Object key = oldKeys[i];
                int value = oldValues[i];
                int index = findIndex(key, keys);
                keys[index] = key;
                values[index] = value;
            }
        }
    }

    public int get(Object key) {
        int index = findIndex(key, keys);
        if (keys[index] == key) {
            return values[index];
        }
        return -1;
    }

    /**
     * Returns the index where the key is found at, or the index of the next
     * empty spot if the key is not found in this table.
     */
    private int findIndex(Object key, Object[] array) {
        int length = array.length;
        int index = getModuloHash(key, length);
        int last = (index + length - 1) % length;
        while (index != last) {
            if (array[index] == key || array[index] == null) {
                /*
                 * Found the key, or the next empty spot (which means key is not
                 * in the table)
                 */
                break;
            }
            index = (index + 1) % length;
        }
        return index;
    }

    private int getModuloHash(Object key, int length) {
        return (System.identityHashCode(key) & 0x7FFFFFFF) % length;
    }

    public int put(Object key, int value) {
        Object _key = key;
        int _value = value;

        int index = findIndex(_key, keys);

        // if the key doesn't exist in the table
        if (keys[index] != _key) {
            if (++size > threshold) {
                rehash();
                index = findIndex(_key, keys);
            }
            // insert the key and assign the value to -1 initially
            keys[index] = _key;
            values[index] = -1;
        }

        // insert value to where it needs to go, return the old value
        int result = values[index];
        values[index] = _value;
        return result;
    }

    private void rehash() {
        int newSize = keys.length * 2;
        resizeArrays(newSize);
        threshold = (int) ((long) (keys.length) * LOAD_FACTOR / 10000);
    }

    public int remove(Object key) {
        boolean hashedOk;
        int index, next, hash;
        int result;
        Object object;
        index = next = findIndex(key, keys);

        if (keys[index] != key) {
            return -1;
        }

        // store the value for this key
        result = values[index];

        // shift the following elements up if needed
        // until we reach an empty spot
        int length = keys.length;
        while (true) {
            next = (next + 2) % length;
            object = keys[next];
            if (object == null) {
                break;
            }

            hash = getModuloHash(object, length);
            hashedOk = hash > index;
            if (next < index) {
                hashedOk = hashedOk || (hash <= next);
            } else {
                hashedOk = hashedOk && (hash <= next);
            }
            if (!hashedOk) {
                keys[index] = object;
                values[index] = values[next];
                index = next;
            }
        }
        size--;

        // clear both the key and the value
        keys[index] = null;
        values[index] = -1;

        return result;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
