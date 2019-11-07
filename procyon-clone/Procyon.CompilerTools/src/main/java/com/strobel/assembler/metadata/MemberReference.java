/*
 * MemberReference.java
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

import com.strobel.assembler.metadata.annotations.CustomAnnotation;

import java.util.Collections;
import java.util.List;

/**
 * User: Mike Strobel
 * Date: 1/6/13
 * Time: 4:38 PM
 */
public abstract class MemberReference implements IAnnotationsProvider, IMetadataTypeMember {
    protected MemberReference() {
    }

    public boolean isSpecialName() {
        return false;
    }

    public boolean isDefinition() {
        return false;
    }

    public boolean containsGenericParameters() {
        final TypeReference declaringType = getDeclaringType();

        return declaringType != null &&
               declaringType.containsGenericParameters();
    }

    public abstract TypeReference getDeclaringType();

    public boolean isEquivalentTo(final MemberReference member) {
        return member == this;
    }

    // <editor-fold defaultstate="collapsed" desc="Annotations">

    @Override
    public boolean hasAnnotations() {
        return !getAnnotations().isEmpty();
    }

    @Override
    public List<CustomAnnotation> getAnnotations() {
        return Collections.emptyList();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Name and Signature Formatting">

    public abstract String getName();

    public String getFullName() {
        final StringBuilder name = new StringBuilder();
        appendName(name, true, false);
        return name.toString();
    }

    /**
     * Method that returns full generic signature of a type or member.
     */
    public String getSignature() {
        return appendSignature(new StringBuilder()).toString();
    }

    /**
     * Method that returns type erased signature of a type or member;
     * suitable as non-generic signature some packages need.
     */
    public String getErasedSignature() {
        return appendErasedSignature(new StringBuilder()).toString();
    }

    protected abstract StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName);
    protected abstract StringBuilder appendSignature(StringBuilder sb);
    protected abstract StringBuilder appendErasedSignature(StringBuilder sb);

    @Override
    public String toString() {
        return getFullName() + ":" + getSignature();
    }

    // </editor-fold>
}
