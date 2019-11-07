/*
 * CustomAnnotation.java
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

package com.strobel.assembler.metadata.annotations;

import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.VerifyArgument;

import java.util.List;

/**
 * @author Mike Strobel
 */
public final class CustomAnnotation {
    private final TypeReference _annotationType;
    private final List<AnnotationParameter> _parameters;

    public CustomAnnotation(final TypeReference annotationType, final List<AnnotationParameter> parameters) {
        _annotationType = VerifyArgument.notNull(annotationType, "annotationType");
        _parameters = VerifyArgument.notNull(parameters, "parameters");
    }

    public TypeReference getAnnotationType() {
        return _annotationType;
    }

    public boolean hasParameters() {
        return !_parameters.isEmpty();
    }

    public List<AnnotationParameter> getParameters() {
        return _parameters;
    }
}
