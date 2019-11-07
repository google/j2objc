/*
 * MethodList.java
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
import com.strobel.core.VerifyArgument;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Strobel
 */
public final class MethodList extends MemberList<MethodInfo> {
    private final static MethodList EMPTY = new MethodList();

    @SuppressWarnings("unchecked")
    public static MethodList empty() {
        return EMPTY;
    }

    public static MethodList combine(final MethodList first, final MethodList second) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");

        if (first.isEmpty()) {
            return second;
        }

        if (second.isEmpty()) {
            return first;
        }

        final ArrayList<MethodInfo> methods = new ArrayList<>(
            first.size() + second.size()
        );

        methods.addAll(first);
        methods.addAll(second);

        return new MethodList(methods);
    }


    public MethodList(final List<? extends MethodInfo> elements) {
        super(MethodInfo.class, elements);
    }

    public MethodList(final MethodInfo... elements) {
        super(MethodInfo.class, elements);
    }

    public MethodList(final MethodInfo[] elements, final int offset, final int length) {
        super(MethodInfo.class, elements, offset, length);
    }

    @NotNull
    @Override
    public MethodList subList(final int fromIndex, final int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size());

        final int offset = getOffset() + fromIndex;
        final int length = toIndex - fromIndex;

        if (length == 0) {
            return empty();
        }

        return new MethodList(getElements(), offset, length);
    }
}
