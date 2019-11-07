/*
 * ParameterBuilder.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.reflection.emit;

import com.strobel.core.ReadOnlyList;
import com.strobel.core.VerifyArgument;
import com.strobel.reflection.Type;

import java.lang.annotation.Annotation;

/**
 * @author Mike Strobel
 */
public final class ParameterBuilder {
    private final MethodBuilder _declaringMethod;
    private final Type<?> _parameterType;
    private final int _position;

    private ReadOnlyList<AnnotationBuilder<? extends Annotation>> _annotations;
    private String _name;

    ParameterBuilder(
        final MethodBuilder declaringMethod,
        final int position,
        final String name,
        final Type<?> parameterType) {

        _declaringMethod = VerifyArgument.notNull(declaringMethod, "declaringMethod");
        _position = position;
        _name = name != null ? name : "p" + position;
        _parameterType = VerifyArgument.notNull(parameterType, "parameterType");
        _annotations = ReadOnlyList.emptyList();
    }

    @SuppressWarnings("unchecked")
    public void addCustomAnnotation(final AnnotationBuilder<? extends Annotation> annotation) {
        VerifyArgument.notNull(annotation, "annotation");
        final AnnotationBuilder[] newAnnotations = new AnnotationBuilder[this._annotations.size() + 1];
        _annotations.toArray(newAnnotations);
        newAnnotations[this._annotations.size()] = annotation;
        _annotations = new ReadOnlyList<AnnotationBuilder<? extends Annotation>>(newAnnotations);
    }

    public ReadOnlyList<AnnotationBuilder<? extends Annotation>> getCustomAnnotations() {
        return _annotations;
    }

    public String getName() {
        return _name;
    }

    public Type<?> getParameterType() {
        return _parameterType;
    }

    public int getPosition() {
        return _position;
    }

    void setName(final String name) {
        _name = name != null ? name : "p" + _position;
    }
}
