/*
 * TypeList.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.reflection;

import com.strobel.annotations.NotNull;
import com.strobel.core.HashUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.util.TypeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Mike Strobel
 */
@SuppressWarnings("unchecked")
public class TypeList extends MemberList<Type<?>> {
    private final static TypeList EMPTY = new TypeList();

    @SuppressWarnings("unchecked")
    public static TypeList empty() {
        return EMPTY;
    }

    public static TypeList combine(final TypeList first, final TypeList second) {
        return combineCore(first, second, false);
    }

    public static TypeList of(final Type... types) {
        return new TypeList(types);
    }

    public static TypeList of(final List<? extends Type<?>> types) {
        return new TypeList(types);
    }

    private static TypeList combineCore(final TypeList first, final TypeList second, final boolean merge) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");

        if (first.isEmpty()) {
            return second;
        }

        if (second.isEmpty()) {
            return first;
        }

        final ArrayList<Type<?>> types = new ArrayList<>();

        for (int i = 0, n = first.size(); i < n; i++) {
            final Type type = first.get(i);
            if (!merge || !types.contains(type)) {
                types.add(type);
            }
        }

        for (int i = 0, n = second.size(); i < n; i++) {
            final Type type = second.get(i);
            if (!merge || !types.contains(type)) {
                types.add(type);
            }
        }

        return new TypeList(types);
    }

    public TypeList(final Type... elements) {
        super((Class<Type<?>>) (Class) Type.class, elements);
    }

    public TypeList(final List<? extends Type<?>> elements) {
        super((Class<Type<?>>) (Class) Type.class, elements);
    }

    public TypeList(final Type[] elements, final int offset, final int length) {
        super((Class<Type<?>>) (Class) Type.class, elements, offset, length);
    }

    @NotNull
    @Override
    public TypeList subList(final int fromIndex, final int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size());

        final int offset = getOffset() + fromIndex;
        final int length = toIndex - fromIndex;

        if (length == 0) {
            return empty();
        }

        return new TypeList(getElements(), offset, length);
    }

    public final boolean containsGenericParameters() {
        for (int i = 0, n = this.size(); i < n; i++) {
            if (this.get(i).containsGenericParameters()) {
                return true;
            }
        }
        return false;
    }

    public final boolean containsGenericParameter(final Type<?> genericParameter) {
        if (!VerifyArgument.notNull(genericParameter, "genericParameter").isGenericParameter()) {
            throw Error.notGenericParameter(genericParameter);
        }

        for (int i = 0, n = this.size(); i < n; i++) {
            if (this.get(i).containsGenericParameter(genericParameter)) {
                return true;
            }
        }
        return false;
    }

    public final boolean containsSubTypeOf(final Type<?> type) {
        for (int i = 0, n = this.size(); i < n; i++) {
            if (this.get(i).isSubTypeOf(type)) {
                return true;
            }
        }
        return false;
    }

    public final boolean containsSuperTypeOf(final Type<?> type) {
        for (int i = 0, n = this.size(); i < n; i++) {
            if (type.isSubTypeOf(this.get(i))) {
                return true;
            }
        }
        return false;
    }

    public final boolean containsTypeAssignableFrom(final Type<?> type) {
        for (int i = 0, n = this.size(); i < n; i++) {
            if (this.get(i).isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    public final boolean isEquivalentTo(final TypeList types) {
        if (types == this) {
            return true;
        }

        if (types == null || types.size() != size()) {
            return false;
        }

        for (int i = 0, n = this.size(); i < n; i++) {
            if (!TypeUtils.areEquivalent(this.get(i), types.get(i))) {
                return false;
            }
        }

        return true;
    }

    public final boolean isAssignableFrom(final TypeList types) {
        if (types == this) {
            return true;
        }

        if (types == null || types.size() != size()) {
            return false;
        }

        for (int i = 0, n = this.size(); i < n; i++) {
            if (!this.get(i).isAssignableFrom(types.get(i))) {
                return false;
            }
        }

        return true;
    }

    public final TypeList getErasedTypes() {
        if (isEmpty()) {
            return empty();
        }

        final int size = size();
        final Type<?>[] erasedTypes = new Type<?>[size];

        for (int i = 0; i < size; i++) {
            erasedTypes[i] = get(i).getErasedType();
        }

        return new TypeList(erasedTypes);
    }

    @Override
    public int hashCode() {
        int hashCode = HashUtilities.NullHashCode;

        for (final Type<?> type : super.getElements()) {
            hashCode = HashUtilities.combineHashCodes(hashCode, type.hashCode());
        }

        return hashCode;
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }

    public boolean equals(final TypeList other) {
        return other == this ||
               other != null && Arrays.equals(super.getElements(), other.getElements());
    }
}
