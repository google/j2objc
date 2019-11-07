/*
 * FieldList.java
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
public final class FieldList extends MemberList<FieldInfo> {
    private final static FieldList EMPTY = new FieldList();

    @SuppressWarnings("unchecked")
    public static FieldList empty() {
        return EMPTY;
    }

    public FieldList(final List<? extends FieldInfo> elements) {
        super(FieldInfo.class, elements);
    }

    public FieldList(final FieldInfo... elements) {
        super(FieldInfo.class, elements);
    }

    public FieldList(final FieldInfo[] elements, final int offset, final int length) {
        super(FieldInfo.class, elements, offset, length);
    }

    @NotNull
    @Override
    public FieldList subList(final int fromIndex, final int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size());

        final int offset = getOffset() + fromIndex;
        final int length = toIndex - fromIndex;

        if (length == 0) {
            return empty();
        }

        return new FieldList(getElements(), offset, length);
    }
}
