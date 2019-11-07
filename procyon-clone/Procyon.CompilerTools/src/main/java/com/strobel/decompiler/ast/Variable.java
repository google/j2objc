/*
 * Variable.java
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

package com.strobel.decompiler.ast;

import com.strobel.assembler.metadata.ParameterDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.assembler.metadata.VariableDefinition;

public final class Variable {
    private String _name;
    private boolean _isGenerated;
    private boolean _isLambdaParameter;
    private TypeReference _type;
    private VariableDefinition _originalVariable;
    private ParameterDefinition _originalParameter;

    public final String getName() {
        return _name;
    }

    public final void setName(final String name) {
        _name = name;
    }

    public final boolean isParameter() {
        if (_originalParameter != null) {
            return true;
        }

        final VariableDefinition originalVariable = _originalVariable;

        return originalVariable != null &&
               originalVariable.isParameter();
    }

    public final boolean isGenerated() {
        return _isGenerated;
    }

    public final boolean isGeneratedStackVariable() {
        return _isGenerated && _name != null && _name.startsWith("stack_");
    }

    public final void setGenerated(final boolean generated) {
        _isGenerated = generated;
    }

    public final TypeReference getType() {
        return _type;
    }

    public final void setType(final TypeReference type) {
        _type = type;
    }

    public final VariableDefinition getOriginalVariable() {
        return _originalVariable;
    }

    public final void setOriginalVariable(final VariableDefinition originalVariable) {
        _originalVariable = originalVariable;
    }

    public final ParameterDefinition getOriginalParameter() {
        final ParameterDefinition originalParameter = _originalParameter;

        if (originalParameter != null) {
            return originalParameter;
        }

        final VariableDefinition originalVariable = _originalVariable;

        if (originalVariable != null) {
            return originalVariable.getParameter();
        }

        return null;
    }

    public final void setOriginalParameter(final ParameterDefinition originalParameter) {
        _originalParameter = originalParameter;
    }

    public final boolean isLambdaParameter() {
        return _isLambdaParameter;
    }

    public final void setLambdaParameter(final boolean lambdaParameter) {
        _isLambdaParameter = lambdaParameter;
    }

    @Override
    public final String toString() {
        return _name;
    }
}
