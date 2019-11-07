/*
 * ArrayType.java
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

package com.strobel.reflection;

import com.strobel.annotations.NotNull;
import com.strobel.core.Fences;
import com.strobel.core.VerifyArgument;

import javax.lang.model.type.TypeKind;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;

/**
 * @author strobelm
 */
final class ArrayType<T> extends Type<T> {
    private final Type<?> _elementType;
    private final FieldList _fields = FieldList.empty();
    private final MethodList _methods = MethodList.empty();

    private Class<T> _erasedClass;

    @SuppressWarnings("unchecked")
    ArrayType(final Type<?> elementType) {
        _elementType = VerifyArgument.notNull(elementType, "elementType");
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.ARRAY;
    }

    @Override
    protected String getClassFullName() {
        return _elementType.getClassFullName() + "[]";
    }

    @Override
    protected String getClassSimpleName() {
        return _elementType.getClassSimpleName() + "[]";
    }

    @Override
    public String getInternalName() {
        return "[" + _elementType.getErasedSignature();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getErasedClass() {
        if (_erasedClass == null && Fences.orderReads(this)._erasedClass == null) {
            _erasedClass = Fences.orderWrites((Class<T>) Array.newInstance(_elementType.getErasedClass(), 0).getClass());
            Fences.orderAccesses(this);
        }
        return _erasedClass;
    }

    @Override
    public Type<?> getElementType() {
        return _elementType;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isGenericType() {
        return _elementType.isGenericType();
    }

    @Override
    public boolean hasElementType() {
        return true;
    }

    @Override
    public Type getGenericTypeDefinition() {
        if (_elementType.isGenericTypeDefinition()) {
            return this;
        }
        return _elementType.getGenericTypeDefinition().makeArrayType();
    }

    @Override
    public TypeBindings getTypeBindings() {
        return _elementType.getTypeBindings();
    }

    @Override
    public Type getDeclaringType() {
        return null;
    }

    @Override
    public int getModifiers() {
        return 0;
    }

    @Override
    protected MethodList getDeclaredMethods() {
        return _methods;
    }

    @Override
    public FieldList getDeclaredFields() {
        return _fields;
    }

    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
        return false;
    }

    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        return null;
    }

    @NotNull
    @Override
    public Annotation[] getAnnotations() {
        return new Annotation[0];
    }

    @NotNull
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return new Annotation[0];
    }

    @Override
    public StringBuilder appendSignature(final StringBuilder sb) {
        sb.append('[');
        return _elementType.appendSignature(sb);
    }

    @Override
    public StringBuilder appendErasedSignature(final StringBuilder sb) {
        return _elementType.appendErasedSignature(sb.append('['));
    }

    public StringBuilder appendBriefDescription(final StringBuilder sb) {
        return _elementType.appendBriefDescription(sb).append("[]");
    }

    public StringBuilder appendSimpleDescription(final StringBuilder sb) {
        return _elementType.appendSimpleDescription(sb).append("[]");
    }

    public StringBuilder appendDescription(final StringBuilder sb) {
        return appendBriefDescription(sb);
    }

    @Override
    public <P, R> R accept(final TypeVisitor<P, R> visitor, final P parameter) {
        return visitor.visitArrayType(this, parameter);
    }
}
