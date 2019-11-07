/*
 * AnonymousLocalTypeCollection.java
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

import com.strobel.assembler.Collection;
import com.strobel.core.VerifyArgument;

public final class AnonymousLocalTypeCollection extends Collection<TypeDefinition> {
    private final MethodDefinition _owner;

    public AnonymousLocalTypeCollection(final MethodDefinition owner) {
        _owner = VerifyArgument.notNull(owner, "owner");
    }

    @Override
    protected void afterAdd(final int index, final TypeDefinition type, final boolean appended) {
        type.setDeclaringMethod(_owner);
    }

    @Override
    protected void beforeSet(final int index, final TypeDefinition type) {
        final TypeDefinition current = get(index);

        current.setDeclaringMethod(null);
        type.setDeclaringMethod(_owner);
    }

    @Override
    protected void afterRemove(final int index, final TypeDefinition type) {
        type.setDeclaringMethod(null);
    }

    @Override
    protected void beforeClear() {
        for (int i = 0; i < size(); i++) {
            get(i).setDeclaringMethod(null);
        }
    }
}
