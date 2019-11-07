/*
 * ParameterizedType.java
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

import java.util.List;

/**
 * @author Mike Strobel
 */
final class ParameterizedType extends TypeReference implements IGenericInstance {
    private final TypeReference _genericDefinition;
    private final List<TypeReference> _typeParameters;

    ParameterizedType(final TypeReference genericDefinition, final List<TypeReference> typeParameters) {
        _genericDefinition = genericDefinition;
        _typeParameters = typeParameters;
    }

    @Override
    public String getName() {
        return _genericDefinition.getName();
    }

    @Override
    public String getPackageName() {
        return _genericDefinition.getPackageName();
    }

    @Override
    public String getFullName() {
        return _genericDefinition.getFullName();
    }

    @Override
    public String getInternalName() {
        return _genericDefinition.getInternalName();
    }

    @Override
    public TypeReference getDeclaringType() {
        return _genericDefinition.getDeclaringType();
    }

    @Override
    public String getSimpleName() {
        return _genericDefinition.getSimpleName();
    }

    @Override
    public boolean isGenericDefinition() {
        return false;
    }

    @Override
    public List<GenericParameter> getGenericParameters() {
        if (!_genericDefinition.isGenericDefinition()) {
            final TypeDefinition resolvedDefinition = _genericDefinition.resolve();

            if (resolvedDefinition != null) {
                return resolvedDefinition.getGenericParameters();
            }
        }

        return _genericDefinition.getGenericParameters();
    }

    @Override
    public boolean hasTypeArguments() {
        return true;
    }

    @Override
    public List<TypeReference> getTypeArguments() {
        return _typeParameters;
    }

    @Override
    public IGenericParameterProvider getGenericDefinition() {
        return _genericDefinition;
    }

    @Override
    public TypeReference getUnderlyingType() {
        return _genericDefinition;
    }

    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitParameterizedType(this, parameter);
    }

    @Override
    public TypeDefinition resolve() {
        return _genericDefinition.resolve();
    }

    @Override
    public FieldDefinition resolve(final FieldReference field) {
        return _genericDefinition.resolve(field);
    }

    @Override
    public MethodDefinition resolve(final MethodReference method) {
        return _genericDefinition.resolve(method);
    }

    @Override
    public TypeDefinition resolve(final TypeReference type) {
        return _genericDefinition.resolve(type);
    }
}
