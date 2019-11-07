/*
 * UserDataStoreBase.java
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
import com.strobel.core.ExceptionUtilities;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class UserDataStoreBase implements UserDataStore, Cloneable {
    public static final Key<FrugalKeyMap> COPYABLE_USER_MAP_KEY = Key.create("COPYABLE_USER_MAP_KEY");

    private final static AtomicReferenceFieldUpdater<UserDataStoreBase, FrugalKeyMap> UPDATER =
        AtomicReferenceFieldUpdater.newUpdater(
            UserDataStoreBase.class,
            FrugalKeyMap.class,
            "_map"
        );

    @NotNull
    @SuppressWarnings("FieldMayBeFinal")
    private volatile FrugalKeyMap _map = FrugalKeyMap.EMPTY;

    @Override
    public <T> T getUserData(@NotNull final Key<T> key) {
        return _map.get(key);
    }

    @Override
    public <T> void putUserData(@NotNull final Key<T> key, @Nullable final T value) {
        while (true) {
            final FrugalKeyMap oldMap = _map;
            final FrugalKeyMap newMap;

            if (value == null) {
                newMap = oldMap.minus(key);
            }
            else {
                newMap = oldMap.plus(key, value);
            }

            if (newMap == oldMap || UPDATER.compareAndSet(this, oldMap, newMap)) {
                return;
            }
        }
    }

    @Override
    public <T> T putUserDataIfAbsent(@NotNull final Key<T> key, @Nullable final T value) {
        while (true) {
            final FrugalKeyMap oldMap = _map;
            final FrugalKeyMap newMap;

            final T oldValue = _map.get(key);

            if (oldValue != null) {
                return oldValue;
            }

            if (value == null) {
                newMap = oldMap.minus(key);
            }
            else {
                newMap = oldMap.plus(key, value);
            }

            if (newMap == oldMap || UPDATER.compareAndSet(this, oldMap, newMap)) {
                return value;
            }
        }
    }

    @Override
    public <T> boolean replace(@NotNull final Key<T> key, @Nullable final T oldValue, @Nullable final T newValue) {
        while (true) {
            final FrugalKeyMap oldMap = _map;
            final T currentValue = _map.get(key);

            if (currentValue != oldValue) {
                return false;
            }

            final FrugalKeyMap newMap;

            if (newValue == null) {
                newMap = oldMap.minus(key);
            }
            else {
                newMap = oldMap.plus(key, newValue);
            }

            if (newMap == oldMap || UPDATER.compareAndSet(this, oldMap, newMap)) {
                return true;
            }
        }
    }

    @Override
    public final UserDataStoreBase clone() {
        try {
            return (UserDataStoreBase) super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw ExceptionUtilities.asRuntimeException(e);
        }
    }
}
