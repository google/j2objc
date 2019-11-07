/*
 * MemberList.java
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
import com.strobel.core.ArrayUtilities;
import com.strobel.core.ReadOnlyList;
import com.strobel.core.VerifyArgument;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Strobel
 */
public class MemberList<T extends MemberInfo> extends ReadOnlyList<T> {
    @SuppressWarnings("unchecked")
    private final static MemberList<?> EMPTY = new MemberList(MemberInfo.class);
    private final Class<T> _memberType;

    @SuppressWarnings("unchecked")
    public static <T extends MemberInfo> MemberList<T> empty() {
        return (MemberList<T>) EMPTY;
    }

    @SafeVarargs
    public MemberList(final Class<T> memberType, final T... members) {
        super(VerifyArgument.noNullElements(members, "members"));
        _memberType = VerifyArgument.notNull(memberType, "memberType");
    }

    public MemberList(final Class<T> memberType, final List<? extends T> members) {
        super(memberType, VerifyArgument.noNullElements(members, "members"));
        _memberType = memberType;
    }

    public MemberList(final Class<T> memberType, final T[] members, final int offset, final int length) {
        super(VerifyArgument.noNullElements(members, offset, length, "members"), offset, length);
        _memberType = VerifyArgument.notNull(memberType, "memberType");
    }

    @NotNull
    @Override
    public MemberList<T> subList(final int fromIndex, final int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size());

        final int offset = getOffset() + fromIndex;
        final int length = toIndex - fromIndex;

        if (length == 0) {
            return empty();
        }

        return new MemberList<>(_memberType, getElements(), offset, length);
    }

    public static MemberList<?> combine(final MemberList<?>... lists) {
        if (ArrayUtilities.isNullOrEmpty(lists))
            return empty();

        final ArrayList<MemberInfo> members = new ArrayList<>();

        for (final MemberList<?> list : lists) {
            if (list != null) {
                for (final MemberInfo member : list) {
                    members.add(member);
                }
            }
        }

        return new MemberList<>(MemberInfo.class, members);
    }

    Class<T> getMemberType() {
        return _memberType;
    }
}
