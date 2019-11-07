/*
 * ParameterDefinition.java
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
import com.strobel.assembler.metadata.annotations.CustomAnnotation;
import com.strobel.core.StringUtilities;

import java.util.Collections;
import java.util.List;

/**
 * User: Mike Strobel
 * Date: 1/6/13
 * Time: 5:42 PM
 */
public final class ParameterDefinition extends ParameterReference implements IAnnotationsProvider {
    private final Collection<CustomAnnotation> _customAnnotations = new Collection<>();
    private final List<CustomAnnotation> _customAnnotationsView = Collections.unmodifiableList(_customAnnotations);

    private final int _size;
    private int _slot;
    private IMethodSignature _method;
    private TypeReference _declaringType;
    private long _flags;

    public ParameterDefinition(final int slot, final TypeReference parameterType) {
        super(StringUtilities.EMPTY, parameterType);
        _slot = slot;
        _size = parameterType.getSimpleType().isDoubleWord() ? 2 : 1;
    }

    public ParameterDefinition(final int slot, final String name, final TypeReference parameterType) {
        super(name, parameterType);
        _slot = slot;
        _size = parameterType.getSimpleType().isDoubleWord() ? 2 : 1;
    }

    public final int getSize() {
        return _size;
    }

    public final int getSlot() {
        return _slot;
    }

    public final long getFlags() {
        return _flags;
    }

    final void setFlags(final long flags) {
        _flags = flags;
    }

    final void setSlot(final int slot) {
        _slot = slot;
    }

    public final IMethodSignature getMethod() {
        return _method;
    }

    final void setMethod(final IMethodSignature method) {
        _method = method;
    }

    public final boolean isFinal() {
        return Flags.testAny(_flags, Flags.FINAL);
    }

    public final boolean isMandated() {
        return Flags.testAny(_flags, Flags.MANDATED);
    }

    public final boolean isSynthetic() {
        return Flags.testAny(_flags, Flags.SYNTHETIC);
    }

    @Override
    public boolean hasAnnotations() {
        return !getAnnotations().isEmpty();
    }

    @Override
    public List<CustomAnnotation> getAnnotations() {
        return _customAnnotationsView;
    }

    protected final Collection<CustomAnnotation> getAnnotationsInternal() {
        return _customAnnotations;
    }

    @Override
    public final TypeReference getDeclaringType() {
        return _declaringType;
    }

    @Override
    protected void setParameterType(final TypeReference parameterType) {
        super.setParameterType(parameterType);

        final IMethodSignature method = _method;

        if (method != null) {
            method.invalidateSignature();
        }
    }

    final void setDeclaringType(final TypeReference declaringType) {
        _declaringType = declaringType;
    }

    @Override
    public ParameterDefinition resolve() {
        final TypeReference resolvedParameterType = super.getParameterType().resolve();

        if (resolvedParameterType != null) {
            setParameterType(resolvedParameterType);
        }

        return this;
    }

    // <editor-fold defaultstate="collapsed" desc="Metadata Loading">

    private List<CustomAnnotation> populateCustomAnnotations() {
        return Collections.emptyList();
    }

    // </editor-fold>
}
