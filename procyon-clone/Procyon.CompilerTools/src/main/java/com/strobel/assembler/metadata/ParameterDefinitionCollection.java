/*
 * ParameterDefinitionCollection.java
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

/**
 * @author Mike Strobel
 */
public final class ParameterDefinitionCollection extends Collection<ParameterDefinition> {
    final IMethodSignature signature;

    private TypeReference _declaringType;

    ParameterDefinitionCollection(final IMethodSignature signature) {
        this.signature = signature;
    }

    public final TypeReference getDeclaringType() {
        return _declaringType;
    }

    final void setDeclaringType(final TypeReference declaringType) {
        _declaringType = declaringType;

        for (int i = 0; i < size(); i++) {
            get(i).setDeclaringType(declaringType);
        }
    }

    @Override
    protected void afterAdd(final int index, final ParameterDefinition p, final boolean appended) {
        p.setMethod(signature);
        p.setPosition(index);
        p.setDeclaringType(_declaringType);

        if (!appended) {
            for (int i = index + 1; i < size(); i++) {
                get(i).setPosition(i + 1);
            }
        }

        signature.invalidateSignature();
    }

    @Override
    protected void beforeSet(final int index, final ParameterDefinition p) {
        final ParameterDefinition current = get(index);

        current.setMethod(null);
        current.setPosition(-1);
        current.setDeclaringType(null);

        p.setMethod(signature);
        p.setPosition(index);
        p.setDeclaringType(_declaringType);

        signature.invalidateSignature();
    }

    @Override
    protected void afterRemove(final int index, final ParameterDefinition p) {
        p.setMethod(null);
        p.setPosition(-1);
        p.setDeclaringType(null);

        for (int i = index; i < size(); i++) {
            get(i).setPosition(i);
        }

        signature.invalidateSignature();
    }

    @Override
    protected void beforeClear() {
        for (int i = 0; i < size(); i++) {
            get(i).setMethod(null);
            get(i).setPosition(-1);
            get(i).setDeclaringType(null);
        }

        signature.invalidateSignature();
    }
}
