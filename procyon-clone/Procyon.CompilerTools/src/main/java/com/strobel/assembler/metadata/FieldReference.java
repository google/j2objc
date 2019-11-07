/*
 * FieldReference.java
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
import com.strobel.util.ContractUtils;

/**
 * User: Mike Strobel
 * Date: 1/6/13
 * Time: 2:30 PM
 */
public abstract class FieldReference extends MemberReference {
    public abstract TypeReference getFieldType();

    @Override
    public boolean containsGenericParameters() {
        final TypeReference fieldType = getFieldType();

        return fieldType != null && fieldType.containsGenericParameters() ||
               super.containsGenericParameters();
    }

    @Override
    public boolean isEquivalentTo(final MemberReference member) {
        if (super.isEquivalentTo(member)) {
            return true;
        }

        if (member instanceof FieldReference) {
            final FieldReference field = (FieldReference) member;

            return StringUtilities.equals(field.getName(), this.getName()) &&
                   MetadataResolver.areEquivalent(field.getDeclaringType(), this.getDeclaringType());
        }

        return false;
    }

    public FieldDefinition resolve() {
        final TypeReference declaringType = getDeclaringType();

        if (declaringType == null) {
            throw ContractUtils.unsupported();
        }

        return declaringType.resolve(this);
    }

    // <editor-fold defaultstate="collapsed" desc="Name and Signature Formatting">

    @Override
    protected abstract StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName);


    @Override
    protected StringBuilder appendSignature(final StringBuilder sb) {
        return getFieldType().appendSignature(sb);
    }

    @Override
    protected StringBuilder appendErasedSignature(final StringBuilder sb) {
        return getFieldType().appendErasedSignature(sb);
    }

    // </editor-fold>
}

