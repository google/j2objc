/*
 * MethodBody.java
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
import com.strobel.assembler.ir.ExceptionHandler;
import com.strobel.assembler.ir.InstructionCollection;
import com.strobel.assembler.ir.StackMapFrame;
import com.strobel.core.Freezable;
import com.strobel.core.VerifyArgument;

import java.util.Collections;
import java.util.List;

public final class MethodBody extends Freezable {
    private final MethodDefinition _method;
    private final InstructionCollection _instructions;
    private final VariableDefinitionCollection _variables;
    private final Collection<ExceptionHandler> _exceptionHandlers;

    private List<StackMapFrame> _stackMapFrames;
    private ParameterDefinition _thisParameter;
    private int _maxStackSize;
    private int _maxLocals;
    private int _codeSize;

    public MethodBody(final MethodDefinition methodDefinition) {
        _method = VerifyArgument.notNull(methodDefinition, "methodDefinition");
        _instructions = new InstructionCollection();
        _variables = new VariableDefinitionCollection(methodDefinition);
        _exceptionHandlers = new Collection<>();
    }

    public final IMetadataResolver getResolver() {
        final TypeDefinition declaringType = _method.getDeclaringType();

        if (declaringType != null) {
            return declaringType.getResolver();
        }

        return MetadataSystem.instance();
    }

    public final InstructionCollection getInstructions() {
        return _instructions;
    }

    public final VariableDefinitionCollection getVariables() {
        return _variables;
    }

    public final List<ExceptionHandler> getExceptionHandlers() {
        return _exceptionHandlers;
    }

    public final List<StackMapFrame> getStackMapFrames() {
        final List<StackMapFrame> stackMapFrames = _stackMapFrames;

        return stackMapFrames != null ? stackMapFrames
                                      : Collections.<StackMapFrame>emptyList();
    }

    final void setStackMapFrames(final List<StackMapFrame> stackMapFrames) {
        _stackMapFrames = stackMapFrames;
    }

    public final MethodDefinition getMethod() {
        return _method;
    }

    public final boolean hasThis() {
        return _thisParameter != null;
    }

    public final ParameterDefinition getThisParameter() {
        return _thisParameter;
    }

    public final int getMaxStackSize() {
        return _maxStackSize;
    }

    public final int getCodeSize() {
        return _codeSize;
    }

    public final int getMaxLocals() {
        return _maxLocals;
    }

    final void setThisParameter(final ParameterDefinition thisParameter) {
        _thisParameter = thisParameter;
    }

    final void setMaxStackSize(final int maxStackSize) {
        _maxStackSize = maxStackSize;
    }

    final void setCodeSize(final int codeSize) {
        _codeSize = codeSize;
    }

    final void setMaxLocals(final int maxLocals) {
        _maxLocals = maxLocals;
    }

    @Override
    protected final void freezeCore() {
        _instructions.freezeIfUnfrozen();
        _variables.freezeIfUnfrozen();
        _exceptionHandlers.freezeIfUnfrozen();

        super.freezeCore();
    }

    public final ParameterDefinition getParameter(final int index) {
        final MethodReference method = getMethod();

        int i = index;

        if (_thisParameter != null) {
            if (index == 0) {
                return _thisParameter;
            }
            --i;
        }

        if (method == null) {
            return null;
        }

        final List<ParameterDefinition> parameters = method.getParameters();

        if (i < 0 || i >= parameters.size()) {
            return null;
        }

        return parameters.get(i);
    }
}
