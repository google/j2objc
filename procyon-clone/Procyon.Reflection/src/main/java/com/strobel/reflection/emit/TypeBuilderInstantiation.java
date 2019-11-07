/*
 * TypeBuilderInstantiation.java
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

import com.strobel.core.VerifyArgument;
import com.strobel.reflection.*;

import java.util.ArrayList;

/**
 * @author Mike Strobel
 */
@SuppressWarnings("unchecked")
final class TypeBuilderInstantiation extends Type {
    private final TypeBuilder<?> _definition;
    private final TypeBindings _typeBindings;

    private Type<?> _baseType;

    private TypeBuilderInstantiation(final TypeBuilder<?> definition, final TypeBindings typeBindings) {
        _definition = VerifyArgument.notNull(definition, "definition");
        _typeBindings = VerifyArgument.notNull(typeBindings, "typeBindings");
    }

    static Type makeGenericType(final TypeBuilder<?> type, final TypeList typeArguments) {
        VerifyArgument.notNull(type, "type");
        VerifyArgument.notNull(typeArguments, "typeArguments");

        if (!type.isGenericTypeDefinition()) {
            throw Error.genericTypeDefinitionRequired();
        }

        return new TypeBuilderInstantiation(
            type,
            TypeBindings.create(
                type.getGenericTypeParameters(),
                typeArguments
            )
        );
    }

    private Type<?> substitute(final TypeList substitutes) {
        final TypeList typeArguments = getTypeArguments();
        final Type<?>[] substituteArguments = new Type<?>[typeArguments.size()];

        for (int i = 0, n = substituteArguments.length; i < n; i++) {
            final Type t = typeArguments.get(i);

            if (t instanceof TypeBuilderInstantiation) {
                substituteArguments[i] = ((TypeBuilderInstantiation)t).substitute(substitutes);
            }
            else if (t instanceof GenericParameterBuilder<?>) {
                substituteArguments[i] = substitutes.get(t.getGenericParameterPosition());
            }
            else {
                substituteArguments[i] = t;
            }
        }

        return getGenericTypeDefinition().makeGenericType(substituteArguments);
    }

    @Override
    public Type getDeclaringType() {
        return _definition;
    }

    @Override
    public boolean isGenericType() {
        return true;
    }

    @Override
    public Type getBaseType() {
        if (_baseType != null) {
            return _baseType;
        }

        final Type definitionBase = _definition.getBaseType();

        if (definitionBase == null) {
            return null;
        }

        if (definitionBase instanceof TypeBuilderInstantiation) {
            _baseType = ((TypeBuilderInstantiation)definitionBase).substitute(getTypeArguments());
        }
        else {
            _baseType = definitionBase;
        }

        return _baseType;
    }

    @Override
    public TypeList getExplicitInterfaces() {
        final TypeList definitionInterfaces = _definition.getExplicitInterfaces();
        final ArrayList<Type<?>> interfaces = new ArrayList<>();

        for (final Type interfaceType : definitionInterfaces) {
            interfaces.add(substitute(interfaceType, _typeBindings));
        }

        return Type.list(interfaces);
    }

    @Override
    public Class<?> getErasedClass() {
        return _definition.getErasedClass();
    }

    @Override
    public MethodBase getDeclaringMethod() {
        return _definition.getDeclaringMethod();
    }

    @Override
    protected TypeBindings getTypeBindings() {
        return _typeBindings;
    }

    @Override
    public Type getGenericTypeDefinition() {
        return _definition;
    }

    @Override
    protected String getClassSimpleName() {
        return _definition.getClassSimpleName();
    }

    @Override
    protected String getClassFullName() {
        return _definition.getClassFullName();
    }

    @Override
    protected ConstructorList getDeclaredConstructors() {
        throw Error.typeHasNotBeenCreated();
    }

    @Override
    protected MethodList getDeclaredMethods() {
        throw Error.typeHasNotBeenCreated();
    }

    @Override
    protected FieldList getDeclaredFields() {
        throw Error.typeHasNotBeenCreated();
    }

    @Override
    protected TypeList getDeclaredTypes() {
        throw Error.typeHasNotBeenCreated();
    }

    @Override
    public Type getReflectedType() {
        return _definition.getReflectedType();
    }

    @Override
    public int getModifiers() {
        return _definition.getModifiers();
    }

    @Override
    public Object accept(final TypeVisitor visitor, final Object parameter) {
        return visitor.visitClassType(this, parameter);
    }

    @Override
    public boolean isEquivalentTo(final Type other) {
        if (other == this) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (other == _definition) {
            return _typeBindings.getBoundTypes().isEquivalentTo(_definition.getGenericTypeParameters());
        }

        if (other instanceof TypeBuilderInstantiation) {
            final TypeBuilderInstantiation tbi = (TypeBuilderInstantiation) other;

            return tbi._definition == _definition &&
                   _typeBindings.getBoundTypes().isEquivalentTo(tbi._typeBindings.getBoundTypes());
        }

        return _definition.isCreated() &&
               _definition.createType().makeGenericType(_typeBindings.getBoundTypes()).isEquivalentTo(other);
    }
}
