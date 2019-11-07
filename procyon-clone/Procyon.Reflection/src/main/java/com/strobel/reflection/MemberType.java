/*
 * MemberType.java
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

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Mike Strobel
 */
public enum MemberType {

    Constructor(1),
    Field(2),
    Method(4),
    TypeInfo(8),
    Custom(16),
    NestedType(32),

    All(NestedType.mask | TypeInfo.mask | Method.mask | Field.mask | Constructor.mask);

    @SuppressWarnings("PackageVisibleField")
    final int mask;

    private final static Set<MemberType> constructors = of(Constructor);
    private final static Set<MemberType> fields = of(Field);
    private final static Set<MemberType> methods = of(Method);
    private final static Set<MemberType> types = of(TypeInfo);
    private final static Set<MemberType> custom = of(Custom);
    private final static Set<MemberType> nestedTypes = of(NestedType);

    public static Set<MemberType> constructorsOnly() {
        return constructors;
    }

    public static Set<MemberType> fieldsOnly() {
        return fields;
    }

    public static Set<MemberType> methodsOnly() {
        return methods;
    }

    public static Set<MemberType> typesOnly() {
        return types;
    }

    public static Set<MemberType> customOnly() {
        return custom;
    }

    public static Set<MemberType> nestedTypesOnly() {
        return nestedTypes;
    }

    public static Set<MemberType> of(final MemberType m1) {
        return EnumSet.of(m1);
    }

    public static Set<MemberType> of(final MemberType m1, final MemberType m2) {
        return EnumSet.of(m1, m2);
    }

    public static Set<MemberType> of(final MemberType m1, final MemberType m2, final MemberType m3) {
        return EnumSet.of(m1, m2, m3);
    }

    public static Set<MemberType> of(
        final MemberType m1,
        final MemberType m2,
        final MemberType m3,
        final MemberType m4) {

        return EnumSet.of(m1, m2, m3, m4);
    }

    public static Set<MemberType> of(
        final MemberType m1,
        final MemberType m2,
        final MemberType m3,
        final MemberType m4,
        final MemberType m5) {

        return EnumSet.of(m1, m2, m3, m4, m5);
    }

    public static Set<MemberType> of(final MemberType m1, final MemberType... others) {
        return EnumSet.of(m1, others);
    }

    MemberType(final int mask) {
        this.mask = mask;
    }

    static int mask(final Set<MemberType> memberTypes) {
        int mask = 0;

        if (memberTypes.contains(Constructor)) {
            mask |= Constructor.mask;
        }

        if (memberTypes.contains(Field)) {
            mask |= Field.mask;
        }

        if (memberTypes.contains(Method)) {
            mask |= Method.mask;
        }

        if (memberTypes.contains(TypeInfo)) {
            mask |= TypeInfo.mask;
        }

        if (memberTypes.contains(Custom)) {
            mask |= Custom.mask;
        }

        if (memberTypes.contains(NestedType)) {
            mask |= NestedType.mask;
        }

        return mask;
    }
}
