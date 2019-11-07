/*
 * FieldBuilder.java
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

import com.strobel.annotations.NotNull;
import com.strobel.core.ReadOnlyList;
import com.strobel.core.VerifyArgument;
import com.strobel.reflection.FieldInfo;
import com.strobel.reflection.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author strobelm
 */
@SuppressWarnings("PackageVisibleField")
public final class FieldBuilder extends FieldInfo {
    private final TypeBuilder _typeBuilder;
    private final String _name;
    private final int _modifiers;
    private final Object _constantValue;

    private Type<?> _type;

    private ReadOnlyList<AnnotationBuilder<? extends Annotation>> _annotations;

    FieldInfo generatedField;

    FieldBuilder(final TypeBuilder typeBuilder, final String name, final Type<?> type, final int modifiers, final Object constantValue) {
        _constantValue = constantValue;
        _typeBuilder = VerifyArgument.notNull(typeBuilder, "typeBuilder");
        _name = VerifyArgument.notNull(name, "name");
        _type = VerifyArgument.notNull(type, "type");
        _modifiers = modifiers;
        _annotations = ReadOnlyList.emptyList();
    }

    FieldInfo getCreatedField() {
        _typeBuilder.verifyCreated();
        return generatedField;
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

    final void verifyTypeNotCreated() {
        if (_typeBuilder.isCreated() || generatedField != null) {
            throw Error.cannotModifyFieldAfterTypeCreated();
        }
    }

    @Override
    public Type<?> getFieldType() {
        if (generatedField != null) {
            return generatedField.getFieldType();
        }
        return _type;
    }

    public void setFieldType(final Type<?> fieldType) {
        _type = VerifyArgument.notNull(fieldType, "fieldType");
        invalidateCaches();
    }

    @Override
    public Field getRawField() {
        final FieldInfo createdField = getCreatedField();
        return createdField.getRawField();
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public TypeBuilder getDeclaringType() {
        return _typeBuilder;
    }

    @Override
    public int getModifiers() {
        return _modifiers;
    }

    public Object getConstantValue() {
        return _constantValue;
    }

    @Override
    public Type getReflectedType() {
        return _typeBuilder;
    }

    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        _typeBuilder.verifyCreated();
        return generatedField.getAnnotation(annotationClass);
    }

    @NotNull
    @Override
    public Annotation[] getAnnotations() {
        _typeBuilder.verifyCreated();
        return generatedField.getAnnotations();
    }

    @NotNull
    @Override
    public Annotation[] getDeclaredAnnotations() {
        _typeBuilder.verifyCreated();
        return _typeBuilder.getDeclaredAnnotations();
    }

    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
        _typeBuilder.verifyCreated();
        return generatedField.isAnnotationPresent(annotationClass);
    }
}
