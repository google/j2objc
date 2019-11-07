/*
 * ConstructorList.java
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

import java.util.List;

/**
 * @author Mike Strobel
 */
public final class ConstructorList extends MemberList<ConstructorInfo> {
    private final static ConstructorList EMPTY = new ConstructorList();

    @SuppressWarnings("unchecked")
    public static ConstructorList empty() {
        return EMPTY;
    }

    public ConstructorList(final List<? extends ConstructorInfo> elements) {
        super(ConstructorInfo.class, elements);
    }

    public ConstructorList(final ConstructorInfo... elements) {
        super(ConstructorInfo.class, elements);
    }

    public ConstructorList(final ConstructorInfo[] elements, final int offset, final int length) {
        super(ConstructorInfo.class, elements, offset, length);
    }

    @NotNull
    @Override
    public ConstructorList subList(final int fromIndex, final int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size());

        final int offset = getOffset() + fromIndex;
        final int length = toIndex - fromIndex;

        if (length == 0) {
            return empty();
        }

        return new ConstructorList(getElements(), offset, length);
    }
}
