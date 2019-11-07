/*
 * MetadataFilters.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is based on Mono.Cecil from Jb Evain, Copyright (c) Jb Evain;
 * and ILSpy/ICSharpCode from SharpDevelop, Copyright (c) AlphaSierraPapa.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.assembler.metadata;

import com.strobel.core.Predicate;
import com.strobel.core.Predicates;
import com.strobel.core.StringUtilities;
import com.strobel.util.ContractUtils;

public final class MetadataFilters {
    private MetadataFilters() {
        throw ContractUtils.unreachable();
    }

    public static <T extends TypeReference> Predicate<T> isSubType(final TypeReference anchor) {
        return new Predicate<T>() {
            @Override
            public final boolean test(final T t) {
                return MetadataHelper.isSubType(t, anchor);
            }
        };
    }

    public static <T extends TypeReference> Predicate<T> isSuperType(final TypeReference anchor) {
        return new Predicate<T>() {
            @Override
            public final boolean test(final T t) {
                return MetadataHelper.isSubType(anchor, t);
            }
        };
    }

    public static <T extends TypeReference> Predicate<T> isAssignableFrom(final TypeReference sourceType) {
        return new Predicate<T>() {
            @Override
            public final boolean test(final T t) {
                return MetadataHelper.isAssignableFrom(t, sourceType);
            }
        };
    }

    public static <T extends TypeReference> Predicate<T> isAssignableTo(final TypeReference targetType) {
        return new Predicate<T>() {
            @Override
            public final boolean test(final T t) {
                return MetadataHelper.isAssignableFrom(targetType, t);
            }
        };
    }

    public static <T extends MemberReference> Predicate<T> matchName(final String name) {
        return new Predicate<T>() {
            @Override
            public final boolean test(final T t) {
                return StringUtilities.equals(t.getName(), name);
            }
        };
    }

    public static <T extends MemberReference> Predicate<T> matchDescriptor(final String descriptor) {
        return new Predicate<T>() {
            @Override
            public final boolean test(final T t) {
                return StringUtilities.equals(t.getErasedSignature(), descriptor);
            }
        };
    }

    public static <T extends MemberReference> Predicate<T> matchSignature(final String signature) {
        return new Predicate<T>() {
            @Override
            public final boolean test(final T t) {
                return StringUtilities.equals(t.getSignature(), signature);
            }
        };
    }

    public static <T extends MemberReference> Predicate<T> matchNameAndDescriptor(final String name, final String descriptor) {
        return Predicates.and(MetadataFilters.<T>matchName(name), matchDescriptor(descriptor));
    }

    public static <T extends MemberReference> Predicate<T> matchNameAndSignature(final String name, final String signature) {
        return Predicates.and(MetadataFilters.<T>matchName(name), matchSignature(signature));
    }
}
