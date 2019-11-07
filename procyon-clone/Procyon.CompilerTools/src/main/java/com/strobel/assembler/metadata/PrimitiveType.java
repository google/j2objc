/*
 * PrimitiveType.java
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

/**
 * @author Mike Strobel
 */
public final class PrimitiveType extends TypeDefinition {
    private final JvmType _jvmType;

    PrimitiveType(final JvmType jvmType) {
        super(MetadataSystem.instance());
        _jvmType = VerifyArgument.notNull(jvmType, "jvmType");
        setFlags(Flags.PUBLIC);
        setName(_jvmType.getPrimitiveName());
    }

    @Override
    public String getInternalName() {
        return _jvmType.getDescriptorPrefix();
    }

    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitPrimitiveType(this, parameter);
    }

    @Override
    public String getSimpleName() {
        return _jvmType.getPrimitiveName();
    }

    @Override
    public String getFullName() {
        return _jvmType.getPrimitiveName();
    }

    @Override
    public final boolean isPrimitive() {
        return true;
    }

    @Override
    public final boolean isVoid() {
        return _jvmType == JvmType.Void;
    }

    @Override
    public final JvmType getSimpleType() {
        return _jvmType;
    }

    @Override
    protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
        return sb.append(_jvmType.getPrimitiveName());
    }

    @Override
    protected StringBuilder appendBriefDescription(final StringBuilder sb) {
        return sb.append(_jvmType.getPrimitiveName());
    }

    @Override
    protected StringBuilder appendSimpleDescription(final StringBuilder sb) {
        return sb.append(_jvmType.getPrimitiveName());
    }

    @Override
    protected StringBuilder appendErasedDescription(final StringBuilder sb) {
        return sb.append(_jvmType.getPrimitiveName());
    }

    @Override
    protected StringBuilder appendClassDescription(final StringBuilder sb) {
        return sb.append(_jvmType.getPrimitiveName());
    }

    @Override
    protected StringBuilder appendSignature(final StringBuilder sb) {
        return sb.append(_jvmType.getDescriptorPrefix());
    }

    @Override
    protected StringBuilder appendErasedSignature(final StringBuilder sb) {
        return sb.append(_jvmType.getDescriptorPrefix());
    }

    @Override
    protected StringBuilder appendClassSignature(final StringBuilder sb) {
        return sb.append(_jvmType.getDescriptorPrefix());
    }

    @Override
    protected StringBuilder appendErasedClassSignature(final StringBuilder sb) {
        return sb.append(_jvmType.getDescriptorPrefix());
    }

    @Override
    public StringBuilder appendGenericSignature(final StringBuilder sb) {
        return sb.append(_jvmType.getDescriptorPrefix());
    }
}
