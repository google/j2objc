/*
 * VariableReference.java
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

import com.strobel.core.Freezable;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;

/**
 * User: Mike Strobel
 * Date: 1/6/13
 * Time: 2:07 PM
 */
public abstract class VariableReference extends Freezable implements IMetadataTypeMember {
    private String _name;
    private TypeReference _variableType;

    protected VariableReference(final TypeReference variableType) {
        _variableType = VerifyArgument.notNull(variableType, "variableType");
    }

    protected VariableReference(final String name, final TypeReference variableType) {
        _name = name;
        _variableType = VerifyArgument.notNull(variableType, "variableType");
    }

    public final String getName() {
        return _name;
    }

    @Override
    public abstract TypeReference getDeclaringType();

    public final boolean hasName() {
        return !StringUtilities.isNullOrEmpty(_name);
    }

    protected final void setName(final String name) {
        _name = name;
    }

    public final TypeReference getVariableType() {
        return _variableType;
    }

    protected final void setVariableType(final TypeReference variableType) {
        _variableType = variableType;
    }

    public abstract int getSlot();

    public abstract VariableDefinition resolve();

    @Override
    public String toString() {
        return "VariableReference{" + "Slot=" + getSlot() +
               ", Name=" + (hasName() ? _name : "<unnamed>")
               + ", VariableType=" + _variableType + '}';
    }
}
