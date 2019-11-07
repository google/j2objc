/*
 * FrugalKeyMap.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.componentmodel;

import com.strobel.annotations.NotNull;
import com.strobel.annotations.Nullable;
import com.strobel.core.VerifyArgument;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public interface FrugalKeyMap {
    public final static FrugalKeyMap EMPTY = new EmptyKeyMap();

    @NotNull
    <V> FrugalKeyMap plus(@NotNull final Key<V> key, @NotNull final V value);

    @NotNull
    <V> FrugalKeyMap minus(@NotNull final Key<V> key);

    @Nullable
    <V> V get(@NotNull final Key<V> key);

    @Override
    String toString();

    boolean isEmpty();
}

final class EmptyKeyMap implements FrugalKeyMap {
    @NotNull
    @Override
    public <V> FrugalKeyMap plus(@NotNull final Key<V> key, @NotNull final V value) {
        VerifyArgument.notNull(key, "key");
        VerifyArgument.notNull(value, "value");

        return new SingleKeyMap<>(key.hashCode(), value);
    }

    @NotNull
    @Override
    public final <V> FrugalKeyMap minus(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");
        return this;
    }

    @Override
    public final <V> V get(@NotNull final Key<V> key) {
        return null;
    }

    @Override
    public final boolean isEmpty() {
        return true;
    }
}

final class SingleKeyMap<V> implements FrugalKeyMap {
    private final int _keyIndex;
    private final V _value;

    SingleKeyMap(final int keyIndex, final V value) {
        _keyIndex = keyIndex;
        _value = value;
    }

    @NotNull
    @Override
    public final <V> FrugalKeyMap plus(@NotNull final Key<V> key, @NotNull final V value) {
        VerifyArgument.notNull(key, "key");
        VerifyArgument.notNull(value, "value");

        if (key.hashCode() == _keyIndex) {
            return new SingleKeyMap<>(key.hashCode(), value);
        }

        return new PairKeyMap(_keyIndex, _value, key.hashCode(), value);
    }

    @NotNull
    @Override
    public final <V> FrugalKeyMap minus(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");

        if (key.hashCode() == _keyIndex) {
            return EMPTY;
        }

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <V> V get(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");

        if (key.hashCode() == _keyIndex) {
            return (V)_value;
        }

        return null;
    }

    @Override
    public final boolean isEmpty() {
        return false;
    }
}

final class PairKeyMap implements FrugalKeyMap {
    private final int _keyIndex1;
    private final int _keyIndex2;
    private final Object _value1;
    private final Object _value2;

    PairKeyMap(
        final int keyIndex1,
        final Object value1,
        final int keyIndex2,
        final Object value2) {

        _keyIndex1 = keyIndex1;
        _keyIndex2 = keyIndex2;
        _value1 = VerifyArgument.notNull(value1, "value1");
        _value2 = VerifyArgument.notNull(value2, "value2");
    }

    @NotNull
    @Override
    public final <V> FrugalKeyMap plus(@NotNull final Key<V> key, @NotNull final V value) {
        VerifyArgument.notNull(key, "key");
        VerifyArgument.notNull(value, "value");

        final int keyIndex = key.hashCode();

        if (keyIndex == _keyIndex1) {
            return new PairKeyMap(keyIndex, value, _keyIndex2, _value2);
        }

        if (keyIndex == _keyIndex2) {
            return new PairKeyMap(keyIndex, value, _keyIndex1, _value1);
        }

        return new ArrayKeyMap(
            new int[] { _keyIndex1, _keyIndex2, keyIndex },
            new Object[] { _value1, _value2, value }
        );
    }

    @NotNull
    @Override
    public final <V> FrugalKeyMap minus(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");

        final int keyIndex = key.hashCode();

        if (keyIndex == _keyIndex1) {
            return new SingleKeyMap<>(_keyIndex2, _value2);
        }

        if (keyIndex == _keyIndex2) {
            return new SingleKeyMap<>(_keyIndex1, _value1);
        }

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <V> V get(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");

        if (key.hashCode() == _keyIndex1) {
            return (V)_value1;
        }

        if (key.hashCode() == _keyIndex2) {
            return (V)_value2;
        }

        return null;
    }

    @Override
    public final boolean isEmpty() {
        return false;
    }
}

final class ArrayKeyMap implements FrugalKeyMap {
    final static int ARRAY_THRESHOLD = 8;

    private final int[] _keyIndexes;
    private final Object[] _values;

    ArrayKeyMap(final int[] keyIndexes, final Object[] values) {
        _keyIndexes = keyIndexes;
        _values = values;
    }

    @NotNull
    @Override
    public final <V> FrugalKeyMap plus(@NotNull final Key<V> key, @NotNull final V value) {
        VerifyArgument.notNull(key, "key");
        VerifyArgument.notNull(value, "value");

        final Object[] newValues;
        final int keyIndex = key.hashCode();
        final int[] oldKeys = _keyIndexes;
        final int oldLength = oldKeys.length;

        for (int i = 0; i < oldLength; i++) {
            final int oldKey = oldKeys[i];

            if (oldKey == keyIndex) {
                final Object oldValue = _values[i];

                if (oldValue == value) {
                    return this;
                }

                newValues = Arrays.copyOf(_values, oldLength);
                newValues[i] = value;

                return new ArrayKeyMap(oldKeys, newValues);
            }
        }

        final int[] newKeys = Arrays.copyOf(oldKeys, oldLength + 1);

        newValues = Arrays.copyOf(_values, oldLength + 1);
        newValues[oldLength] = value;
        newKeys[oldLength] = keyIndex;

        return new ArrayKeyMap(newKeys, newValues);
    }

    @NotNull
    @Override
    public final <V> FrugalKeyMap minus(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");

        final int keyIndex = key.hashCode();
        final int[] oldKeys = _keyIndexes;
        final int oldLength = oldKeys.length;

        for (int i = 0; i < oldLength; i++) {
            final int oldKey = oldKeys[i];

            if (keyIndex == oldKey) {
                final int newLength = oldLength - 1;
                final Object[] oldValues = _values;

                if (newLength == 2) {
                    switch (i) {
                        case 0:
                            return new PairKeyMap(1, oldValues[1], oldKeys[2], oldValues[2]);
                        case 1:
                            return new PairKeyMap(0, oldValues[0], oldKeys[2], oldValues[2]);
                        default:
                            return new PairKeyMap(0, oldValues[0], oldKeys[1], oldValues[1]);
                    }
                }

                final int[] newKeys = new int[newLength];
                final Object[] newValues = new Object[newLength];

                System.arraycopy(oldKeys, 0, newKeys, 0, i);
                System.arraycopy(oldKeys, i + 1, newKeys, i, oldLength - i - 1);
                System.arraycopy(oldValues, 0, newValues, 0, i);
                System.arraycopy(oldValues, i + 1, newValues, i, oldLength - i - 1);

                return new ArrayKeyMap(newKeys, newValues);
            }
        }

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <V> V get(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");

        final int keyIndex = key.hashCode();

        for (int i = 0; i < _keyIndexes.length; i++) {
            if (_keyIndexes[i] == keyIndex) {
                return (V)_values[i];
            }
        }

        return null;
    }

    @Override
    public final boolean isEmpty() {
        return false;
    }
}

final class DictionaryKeyMap implements FrugalKeyMap {
    private final Map<Integer, Object> _map;

    DictionaryKeyMap(final DictionaryKeyMap oldMap, final int excludeIndex) {
        _map = new HashMap<>(
            excludeIndex < 0 ? oldMap._map.size()
                             : oldMap._map.size() - 1
        );

        for (final Integer keyIndex : oldMap._map.keySet()) {
            if (keyIndex != excludeIndex) {
                _map.put(keyIndex, oldMap._map);
            }
        }
    }

    DictionaryKeyMap(final int[] keyIndexes, final int newKey, final Object[] values, final Object newValue) {
        assert newKey >= 0;

        _map = new HashMap<>(keyIndexes.length + 1);

        for (int i = 0; i < keyIndexes.length; i++) {
            _map.put(keyIndexes[i], values[i]);
        }

        _map.put(newKey, newValue);

        assert _map.size() > ArrayKeyMap.ARRAY_THRESHOLD;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public final <V> FrugalKeyMap plus(@NotNull final Key<V> key, @NotNull final V value) {
        VerifyArgument.notNull(key, "key");
        VerifyArgument.notNull(value, "value");

        final int keyIndex = key.hashCode();
        final V oldValue = (V)_map.get(keyIndex);

        if (oldValue == value) {
            return this;
        }

        final DictionaryKeyMap newMap = new DictionaryKeyMap(this, -1);
        newMap._map.put(keyIndex, value);
        return newMap;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public final <V> FrugalKeyMap minus(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");

        final int keyIndex = key.hashCode();

        if (!_map.containsKey(keyIndex)) {
            return this;
        }

        final int oldSize = _map.size();
        final int newSize = oldSize - 1;

        if (newSize > ArrayKeyMap.ARRAY_THRESHOLD) {
            return new DictionaryKeyMap(this, keyIndex);
        }

        final int[] newKeys = new int[newSize];
        final Object[] newValues = new Object[newSize];

        int currentIndex = 0;

        for (final Integer oldKey : _map.keySet()) {
            if (oldKey != keyIndex) {
                final int i = currentIndex++;

                newKeys[i] = oldKey;
                newValues[i] = _map.get(oldKey);
            }
        }

        return new ArrayKeyMap(newKeys, newValues);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <V> V get(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");

        return (V)_map.get(key.hashCode());
    }

    @Override
    public final boolean isEmpty() {
        return false;
    }
}
