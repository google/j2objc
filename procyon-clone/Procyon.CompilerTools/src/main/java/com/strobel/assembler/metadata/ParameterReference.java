/*
 * ParameterReference.java
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

import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;

/**
 * User: Mike Strobel
 * Date: 1/6/13
 * Time: 5:41 PM
 */
public abstract class ParameterReference implements IMetadataTypeMember {
    private String _name;
    private int _position = -1;
    private TypeReference _parameterType;

    protected ParameterReference(final String name, final TypeReference parameterType) {
        _name = name != null ? name : StringUtilities.EMPTY;
        _parameterType = VerifyArgument.notNull(parameterType, "parameterType");
    }

    @Override
    public abstract TypeReference getDeclaringType();

    public String getName() {
        if (StringUtilities.isNullOrEmpty(_name)) {
            if (_position < 0) {
                return _name;
            }
            return "p" + _position;
        }
        return _name;
    }

    public final boolean hasName() {
        return !StringUtilities.isNullOrEmpty(_name);
    }

    protected void setName(final String name) {
        _name = name;
    }

    public int getPosition() {
        return _position;
    }

    protected void setPosition(final int position) {
        _position = position;
    }

    public TypeReference getParameterType() {
        return _parameterType;
    }

    protected void setParameterType(final TypeReference parameterType) {
        _parameterType = parameterType;
    }

    @Override
    public String toString() {
        return getName();
    }

    public abstract ParameterDefinition resolve();
}
