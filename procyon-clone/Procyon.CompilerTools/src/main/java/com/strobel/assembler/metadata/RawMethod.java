/*
 * RawMethod.java
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

import com.strobel.core.VerifyArgument;

import java.util.Collections;
import java.util.List;

public final class RawMethod extends MethodReference implements IGenericInstance {
    private final MethodReference _baseMethod;
    private final TypeReference _returnType;
    private final ParameterDefinitionCollection _parameters;

    private TypeReference _declaringType;

    public RawMethod(final MethodReference baseMethod) {
        VerifyArgument.notNull(baseMethod, "baseMethod");

        final TypeReference declaringType = baseMethod.getDeclaringType();

        _baseMethod = baseMethod;
        _declaringType = MetadataHelper.eraseRecursive(declaringType);
        _returnType = MetadataHelper.eraseRecursive(baseMethod.getReturnType());
        _parameters = new ParameterDefinitionCollection(this);

        for (final ParameterDefinition parameter : baseMethod.getParameters()) {
            if (parameter.hasName()) {
                _parameters.add(
                    new ParameterDefinition(
                        parameter.getSlot(),
                        parameter.getName(),
                        MetadataHelper.eraseRecursive(parameter.getParameterType())
                    )
                );
            }
            else {
                _parameters.add(
                    new ParameterDefinition(
                        parameter.getSlot(),
                        MetadataHelper.eraseRecursive(parameter.getParameterType())
                    )
                );
            }
        }

        _parameters.freeze();
    }

    public final MethodReference getBaseMethod() {
        return _baseMethod;
    }

    @Override
    public final boolean hasTypeArguments() {
        return false;
    }

    @Override
    public final List<TypeReference> getTypeArguments() {
        return Collections.emptyList();
    }

    @Override
    public final IGenericParameterProvider getGenericDefinition() {
        return (_baseMethod instanceof IGenericInstance) ? ((IGenericInstance) _baseMethod).getGenericDefinition()
                                                         : null;
    }

    @Override
    public final List<GenericParameter> getGenericParameters() {
        return Collections.emptyList();
    }

    @Override
    public final TypeReference getReturnType() {
        return _returnType;
    }

    @Override
    public final List<ParameterDefinition> getParameters() {
        return _parameters;
    }

    @Override
    public boolean isGenericMethod() {
        return hasTypeArguments();
    }

    @Override
    public MethodDefinition resolve() {
        return _baseMethod.resolve();
    }

    @Override
    public final TypeReference getDeclaringType() {
        return _declaringType;
    }

    final void setDeclaringType(final TypeReference declaringType) {
        _declaringType = declaringType;
    }

    @Override
    public final String getName() {
        return _baseMethod.getName();
    }
}
