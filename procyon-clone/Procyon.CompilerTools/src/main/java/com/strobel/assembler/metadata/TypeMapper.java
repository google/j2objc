/*
 * TypeMapper.java
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

import com.strobel.core.ArrayUtilities;

import java.util.List;

public abstract class TypeMapper<T> extends DefaultTypeVisitor<T, TypeReference> {
    @Override
    public TypeReference visitType(final TypeReference type, final T parameter) {
        return type;
    }

    public List<? extends TypeReference> visit(final List<? extends TypeReference> types, final T parameter) {
        TypeReference[] newTypes = null;

        for (int i = 0, n = types.size(); i < n; i++) {
            final TypeReference oldType = types.get(i);
            final TypeReference newType = visit(oldType, parameter);

            if (newType != oldType) {
                if (newTypes == null) {
                    newTypes = types.toArray(new TypeReference[types.size()]);
                }
                newTypes[i] = newType;
            }
        }

        if (newTypes != null) {
            return ArrayUtilities.asUnmodifiableList(newTypes);
        }

        return types;
    }

    public List<? extends TypeReference> visit(final List<? extends TypeReference> types) {
        return visit(types, null);
    }
}
