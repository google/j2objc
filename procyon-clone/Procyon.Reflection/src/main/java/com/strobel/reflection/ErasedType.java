/*
 * ErasedType.java
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
import com.strobel.core.VerifyArgument;

import java.lang.annotation.Annotation;

/**
 * @author Mike Strobel
 */
@SuppressWarnings("unchecked")
final class ErasedType<T> extends Type<T> {
    final static TypeEraser GenericEraser = new TypeEraser();

    private final Type _originalType;

    private TypeList _interfaces;
    private Type _baseType;

    private FieldList _fields;
    private ConstructorList _constructors;
    private MethodList _methods;
    private TypeList _nestedTypes;

    ErasedType(final Type baseType) {
        VerifyArgument.notNull(baseType, "baseType");
        _originalType = baseType.isGenericType() ? baseType.getGenericTypeDefinition() : baseType;
    }

    private void ensureBaseType() {
        if (_baseType == null) {
            synchronized (CACHE_LOCK) {
                if (_baseType == null) {
                    final Type genericBaseType = _originalType.getBaseType();
                    if (genericBaseType == null || genericBaseType == NullType) {
                        _baseType = NullType;
                    }
                    else {
                        _baseType = GenericEraser.visit(genericBaseType);
                    }
                }
            }
        }
    }

    private void ensureInterfaces() {
        if (_interfaces == null) {
            synchronized (CACHE_LOCK) {
                if (_interfaces == null) {
                    _interfaces = GenericEraser.visit(_originalType.getExplicitInterfaces());
                }
            }
        }
    }

    private final static TypeMapper<Void> UpperBoundMapper = new TypeMapper<Void>() {
        @Override
        public Type<?> visitCapturedType(final Type<?> type, final Void parameter) {
            return super.visitCapturedType(((ICapturedType)type).getWildcard().getExtendsBound(), parameter);
        }

        @Override
        public Type<?> visitWildcardType(final Type<?> type, final Void parameter) {
            return visit(type.getExtendsBound());
        }

        @Override
        public Type<?> visitTypeParameter(final Type<?> type, final Void parameter) {
            return visit(type.getExtendsBound());
        }

        @Override
        public Type<?> visitArrayType(final Type<?> type, final Void parameter) {
            final Type<?> oldElementType = type.getElementType();
            final Type<?> newElementType = visit(oldElementType);

            if (newElementType != oldElementType) {
                return newElementType.makeArrayType();
            }

            return type;
        }
    };

    private void ensureFields() {
        if (_fields == null) {
            synchronized (CACHE_LOCK) {
                if (_fields == null) {
                    _fields = GenericEraser.visit(
                        this,
                        _originalType.getFields(BindingFlags.All),
                        TypeBindings.create(
                            _originalType.getGenericTypeParameters(),
                            UpperBoundMapper.visit(_originalType.getGenericTypeParameters())
                        )
                    );
                }
            }
        }
    }

    private void ensureConstructors() {
        synchronized (CACHE_LOCK) {
            if (_constructors == null) {
                _constructors = GenericEraser.visit(
                    this,
                    _originalType.getConstructors(BindingFlags.All),
                    TypeBindings.create(
                        _originalType.getGenericTypeParameters(),
                        UpperBoundMapper.visit(_originalType.getGenericTypeParameters())
                    )
                );
            }
        }
    }

    private void ensureMethods() {
        if (_methods == null) {
            synchronized (CACHE_LOCK) {
                if (_methods == null) {
                    _methods = GenericEraser.visit(
                        this,
                        _originalType.getMethods(BindingFlags.All),
                        TypeBindings.create(
                            _originalType.getGenericTypeParameters(),
                            UpperBoundMapper.visit(_originalType.getGenericTypeParameters())
                        )
                    );
                }
            }
        }
    }

    private void ensureNestedTypes() {
        if (_nestedTypes == null) {
            synchronized (CACHE_LOCK) {
                if (_nestedTypes == null) {
                    _nestedTypes = GenericEraser.visit(
                        _originalType.getDeclaredTypes()
                    );
                }
            }
        }
    }

    @Override
    public String getName() {
        return _originalType.getName();
    }

    @Override
    protected String getClassFullName() {
        return _originalType.getClassFullName();
    }

    @Override
    protected String getClassSimpleName() {
        return _originalType.getClassSimpleName();
    }

    @Override
    public String getFullName() {
        return _originalType.getFullName();
    }

    @Override
    public String getInternalName() {
        return _originalType.getInternalName();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getErasedClass() {
        return (Class<T>)_originalType.getErasedClass();
    }

    @NotNull
    @Override
    protected Type<?> getUnderlyingTypeCore() {
        return _originalType;
    }

    @Override
    public TypeList getExplicitInterfaces() {
        ensureInterfaces();
        return _interfaces;
    }

    @Override
    public Type<? super T> getBaseType() {
        ensureBaseType();
        final Type baseType = _baseType;
        return baseType == NullType ? null : baseType;
    }

    @Override
    public Type getGenericTypeDefinition() {
        return _originalType;
    }

    @Override
    public Type getDeclaringType() {
        return _originalType.getDeclaringType();
    }

    @Override
    public final boolean isGenericType() {
        return false;
    }

    @Override
    public boolean isRawType() {
        return true;
    }

    @Override
    public TypeBindings getTypeBindings() {
        return TypeBindings.empty();
    }

    @Override
    public int getModifiers() {
        return _originalType.getModifiers();
    }

    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
        return _originalType.isAnnotationPresent(annotationClass);
    }

    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        return (T)_originalType.getAnnotation(annotationClass);
    }

    @NotNull
    @Override
    public Annotation[] getAnnotations() {
        return _originalType.getAnnotations();
    }

    @NotNull
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return _originalType.getDeclaredAnnotations();
    }

    @Override
    public <P, R> R accept(final TypeVisitor<P, R> typeVisitor, final P parameter) {
        return typeVisitor.visitClassType(this, parameter);
    }

    @Override
    public ConstructorList getDeclaredConstructors() {
        ensureConstructors();
        return _constructors;
    }

    @Override
    protected MethodList getDeclaredMethods() {
        ensureMethods();
        return _methods;
    }

    @Override
    protected FieldList getDeclaredFields() {
        ensureFields();
        return _fields;
    }

    @Override
    protected TypeList getDeclaredTypes() {
        ensureNestedTypes();
        return _nestedTypes;
    }
}
