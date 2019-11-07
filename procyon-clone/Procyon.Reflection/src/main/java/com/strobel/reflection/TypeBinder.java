/*
 * TypeBinder.java
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
import com.strobel.util.ContractUtils;
import com.strobel.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Mike Strobel
 */
public class TypeBinder extends TypeMapper<TypeBindings> {
    final static Method GET_CLASS_METHOD;
    final static TypeBinder DEFAULT_BINDER;

    static {
        Method getClassMethod;

        try {
            getClassMethod = Object.class.getMethod("getClass");
        }
        catch (final NoSuchMethodException ignored) {
            getClassMethod = null;
        }

        GET_CLASS_METHOD = getClassMethod;
        DEFAULT_BINDER = new TypeBinder();
    }

    public static TypeBinder defaultBinder() {
        return DEFAULT_BINDER;
    }

    public ConstructorList visit(final Type<?> declaringType, final ConstructorList constructors, final TypeBindings bindings) {
        VerifyArgument.notNull(constructors, "constructors");

        ConstructorInfo[] newConstructors = null;

        for (int i = 0, n = constructors.size(); i < n; i++) {
            final ConstructorInfo oldConstructor = constructors.get(i);
            final ConstructorInfo newConstructor = visitConstructor(declaringType, oldConstructor, bindings);

            if (newConstructor != oldConstructor) {
                if (newConstructors == null) {
                    newConstructors = constructors.toArray();
                }
                newConstructors[i] = newConstructor;
            }
        }

        if (newConstructors != null) {
            return new ConstructorList(newConstructors);
        }

        return constructors;
    }

    public FieldList visit(final Type<?> declaringType, final FieldList fields, final TypeBindings bindings) {
        VerifyArgument.notNull(fields, "fields");

        FieldInfo[] newFields = null;

        for (int i = 0, n = fields.size(); i < n; i++) {
            final FieldInfo oldField = fields.get(i);
            final FieldInfo newField = visitField(declaringType, oldField, bindings);

            if (newField != oldField) {
                if (newFields == null) {
                    newFields = fields.toArray();
                }
                newFields[i] = newField;
            }
        }

        if (newFields != null) {
            return new FieldList(newFields);
        }

        return fields;
    }

    public MethodList visit(final Type<?> declaringType, final MethodList methods, final TypeBindings bindings) {
        VerifyArgument.notNull(methods, "methods");

        MethodInfo[] newMethods = null;

        for (int i = 0, n = methods.size(); i < n; i++) {
            final MethodInfo oldMethod = methods.get(i);
            final MethodInfo newMethod = visitMethod(declaringType, oldMethod, bindings);

            if (newMethod != oldMethod) {
                if (newMethods == null) {
                    newMethods = methods.toArray();
                }
                newMethods[i] = newMethod;
            }
        }

        if (newMethods != null) {
            return new MethodList(newMethods);
        }

        return methods;
    }

    public TypeBindings visitTypeBindings(final TypeBindings typeBindings, final TypeBindings bindings) {
        TypeBindings newTypeBindings = typeBindings;

        for (final Type<?> genericParameter : typeBindings.getGenericParameters()) {
            final Type<?> oldBoundType = typeBindings.getBoundType(genericParameter);
            final Type<?> newBoundType = visit(oldBoundType, bindings);

            if (oldBoundType != newBoundType) {
                newTypeBindings = newTypeBindings.withAdditionalBinding(
                    genericParameter,
                    newBoundType
                );
            }
        }

        return newTypeBindings;
    }

    public FieldInfo visitField(final Type<?> declaringType, final FieldInfo field, final TypeBindings bindings) {
        final Type<?> oldFieldType = field.getFieldType();
        final Type<?> newFieldType = visit(field.getFieldType(), bindings);

        if (TypeUtils.areEquivalent(oldFieldType, newFieldType) &&
            TypeUtils.areEquivalent(field.getDeclaringType(), declaringType)) {

            return field;
        }

        return new ReflectedField(
            declaringType,
            field.getReflectedType(),
            field.getRawField(),
            newFieldType
        );
    }

    public ParameterList visitParameters(final ParameterList parameters, final TypeBindings bindings) {
        VerifyArgument.notNull(parameters, "parameters");

        ParameterInfo[] newParameters = null;

        for (int i = 0, n = parameters.size(); i < n; i++) {
            final ParameterInfo oldParameter = parameters.get(i);
            final Type<?> oldParameterType = oldParameter.getParameterType();
            final Type<?> newParameterType = visit(oldParameterType, bindings);

            if (newParameterType != oldParameterType) {
                if (newParameters == null) {
                    newParameters = parameters.toArray();
                }
                newParameters[i] = new ParameterInfo(oldParameter.getName(), i, newParameterType);
            }
        }

        if (newParameters != null) {
            return new ParameterList(newParameters);
        }

        return parameters;
    }

    public MemberInfo visitMember(final Type<?> declaringType, final MemberInfo member, final TypeBindings bindings) {
        switch (member.getMemberType()) {
            case Constructor:
                return visitConstructor(declaringType, (ConstructorInfo) member, bindings);

            case Field:
                return visitField(declaringType, (FieldInfo) member, bindings);

            case Method:
                return visitMethod(declaringType, (MethodInfo) member, bindings);

            case TypeInfo:
            case NestedType:
                return visitType((Type<?>) member, bindings);

            default:
                throw ContractUtils.unreachable();
        }
    }

    public MethodInfo visitMethod(final Type<?> declaringType, final MethodInfo method, final TypeBindings bindings) {
        if (method.isGenericMethodDefinition()) {
            boolean hasChanged = false;

            final MethodInfo newDefinition;

            if (!method.isGenericMethodDefinition()) {
                final MethodInfo oldDefinition = method.getGenericMethodDefinition();

                newDefinition = visitMethod(declaringType, oldDefinition, bindings);
                hasChanged = newDefinition != oldDefinition;
            }
            else {
                newDefinition = method.getGenericMethodDefinition();
            }

            final TypeBindings oldBindings = method.getTypeBindings();
            final TypeBindings newBindings;

            if (oldBindings.hasUnboundParameters()) {
                newBindings = visitTypeBindings(
                    oldBindings,
                    bindings.withAdditionalBindings(oldBindings)
                );

                hasChanged |= newBindings != oldBindings;
            }
            else {
                newBindings = oldBindings;
            }

            if (hasChanged) {
                return visitMethod(declaringType, newDefinition.makeGenericMethod(newBindings.getBoundTypes()), newBindings);
            }
        }

        final Type<?> oldReturnType = method.getReturnType();
        final Type<?> returnType = visit(oldReturnType, bindings);
        final ParameterList oldParameters = method.getParameters();
        final ParameterList newParameters = visitParameters(oldParameters, bindings);
        final TypeList oldThrownTypes = method.getThrownTypes();
        final Type<?>[] newThrownTypes = new Type<?>[oldThrownTypes.size()];

        boolean hasChanged = !oldReturnType.equals(returnType) || oldParameters != newParameters;
        boolean thrownTypesChanged = false;

        for (int i = 0, n = newThrownTypes.length; i < n; i++) {
            final Type<?> oldThrownType = oldThrownTypes.get(i);
            final Type<?> newThrownType = visit(oldThrownType, bindings);

            newThrownTypes[i] = newThrownType;

            if (!oldThrownType.equals(newThrownType)) {
                thrownTypesChanged = true;
            }
        }

        hasChanged |= thrownTypesChanged;

        if (!hasChanged) {
            if (!TypeUtils.areEquivalent(method.getDeclaringType(), declaringType)) {
                return MethodInfo.declaredOn(method, declaringType, method.getReflectedType());
            }
            return method;
        }

        return new ReflectedMethod(
            method,
            declaringType,
            method.getReflectedType(),
            method.getRawMethod(),
            newParameters,
            returnType,
            thrownTypesChanged ? new TypeList(newThrownTypes) : oldThrownTypes,
            method.getTypeBindings()
        );
    }

    public ConstructorInfo visitConstructor(final Type<?> declaringType, final ConstructorInfo constructor, final TypeBindings bindings) {
        final ParameterList parameters = constructor.getParameters();
        final TypeList thrown = constructor.getThrownTypes();
        final Type<?>[] parameterTypes = new Type<?>[parameters.size()];
        final Type<?>[] thrownTypes = new Type<?>[thrown.size()];

        boolean hasChanged = false;
        boolean thrownTypesChanged = false;

        for (int i = 0, n = parameterTypes.length; i < n; i++) {
            final Type<?> oldParameterType = parameters.get(i).getParameterType();
            parameterTypes[i] = visit(oldParameterType, bindings);
            if (!oldParameterType.equals(parameterTypes[i])) {
                hasChanged = true;
            }
        }

        for (int i = 0, n = thrownTypes.length; i < n; i++) {
            final Type<?> oldThrownType = thrown.get(i);
            final Type<?> newThrownType = visit(oldThrownType, bindings);

            thrownTypes[i] = newThrownType;

            if (!oldThrownType.equals(newThrownType)) {
                thrownTypesChanged = true;
            }
        }

        hasChanged |= thrownTypesChanged;

        if (!hasChanged) {
            if (!TypeUtils.areEquivalent(constructor.getDeclaringType(), declaringType)) {
                return new ReflectedConstructor(
                    declaringType,
                    constructor.getRawConstructor(),
                    constructor.getParameters(),
                    thrown
                );
            }

            return constructor;
        }

        final ArrayList<ParameterInfo> newParameters = new ArrayList<>();

        for (int i = 0, n = parameterTypes.length; i < n; i++) {
            newParameters.add(
                new ParameterInfo(
                    parameters.get(i).getName(),
                    i,
                    parameterTypes[i]
                )
            );
        }

        return new ReflectedConstructor(
            declaringType,
            constructor.getRawConstructor(),
            new ParameterList(newParameters),
            new TypeList(thrownTypes)
        );
    }

    @Override
    public Type<?> visitClassType(final Type<?> type, final TypeBindings bindings) {
        if (bindings.containsGenericParameter(type)) {
            return bindings.getBoundType(type);
        }

        final TypeBindings oldTypeBindings = type.getTypeBindings();
        final TypeBindings newTypeBindings = visitTypeBindings(oldTypeBindings, bindings);

        if (oldTypeBindings != newTypeBindings) {
            return type.getGenericTypeDefinition().makeGenericType(newTypeBindings.getBoundTypes());
        }

        return type;
    }

    @Override
    public Type<?> visitTypeParameter(final Type<?> type, final TypeBindings bindings) {
        return visitTypeParameterCore(type, bindings);
    }

    protected Type<?> visitTypeParameterCore(final Type<?> type, final TypeBindings bindings) {
        if (!bindings.containsGenericParameter(type) && Types.Object.equals(type.getExtendsBound())) {
            return type;
        }

        final CacheEntry entry = new CacheEntry(bindings, type);
        final Map<CacheEntry, CacheEntry> cache = cache();

        if (cache.containsKey(entry)) {
            return type;
        }

        cache.put(entry, entry);

        try {
            if (bindings.containsGenericParameter(type)) {
                return visit(bindings.getBoundType(type), bindings);
            }

            final Type<?> upperBound = type.getExtendsBound();
            final Type<?> newUpperBound = visit(upperBound, bindings);

            if (newUpperBound != upperBound) {
                if (type.getDeclaringMethod() != null) {
                    return new GenericParameter(
                        type.getFullName(),
                        (MethodInfo) type.getDeclaringMethod(),
                        newUpperBound,
                        type.getGenericParameterPosition()
                    );
                }

                return new GenericParameter(
                    type.getFullName(),
                    type.getDeclaringType(),
                    newUpperBound,
                    type.getGenericParameterPosition()
                );
            }

            return type;
        }
        finally {
            cache.remove(entry);
        }
    }

    @Override
    public Type<?> visitWildcardType(final Type<?> type, final TypeBindings bindings) {
        final Type<?> oldLower = type.getSuperBound();
        final Type<?> oldUpper = type.getExtendsBound();

        final Type<?> newLower = visit(oldLower, bindings);
        final Type<?> newUpper = visit(oldUpper, bindings);

        if (newLower != oldLower || newUpper != oldUpper) {
            return new WildcardType<>(newUpper, newLower);
        }

        return type;
    }

    @Override
    public Type<?> visitArrayType(final Type<?> type, final TypeBindings bindings) {
        final Type<?> oldElementType = type.getElementType();
        final Type<?> newElementType = visit(oldElementType, bindings);

        if (TypeUtils.areEquivalent(oldElementType, newElementType)) {
            return type;
        }

        return newElementType.makeArrayType();
    }

    // <editor-fold defaultstate="collapsed" desc="Generic Parameter Stack">

    //
    // We keep a thread-local cache of generic parameters we have already visited, so as to avoid infinite recursion
    // when visiting type variables with self-referencing bounds, e.g., `T extends Comparable<? super T>`.
    //

    private static final ThreadLocal<Map<CacheEntry, CacheEntry>> CACHE = new ThreadLocal<Map<CacheEntry, CacheEntry>>() {
        @Override
        protected Map<CacheEntry, CacheEntry> initialValue() {
            return new LinkedHashMap<>();
        }
    };

    private static Map<CacheEntry, CacheEntry> cache() {
        return CACHE.get();
    }

    private static final class CacheEntry {
        @NotNull
        final TypeBindings bindings;
        @NotNull
        final Type<?> type;

        CacheEntry(final @NotNull TypeBindings bindings, final @NotNull Type<?> type) {
            this.bindings = bindings;
            this.type = type;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }

            if (o instanceof CacheEntry) {
                final CacheEntry entry = (CacheEntry) o;

                return entry.bindings == this.bindings &&
                       entry.type == this.type;
            }

            return false;
        }

        @Override
        public int hashCode() {
            int result = System.identityHashCode(bindings);
            result = 31 * result + System.identityHashCode(type);
            return result;
        }

        @Override
        public String toString() {
            return "CacheEntry{" +
                   "bindings=" + bindings +
                   ", type=" + type +
                   '}';
        }
    }

    // </editor-fold>
}

class TypeEraser extends TypeBinder {
    @Override
    public Type<?> visit(final Type<?> type) {
        return visit(type, TypeBindings.empty());
    }

    @Override
    public Type<?> visitClassType(final Type<?> type, final TypeBindings bindings) {
        if (type instanceof ErasedType<?>) {
            return type;
        }
        return type.getErasedType();
    }

    @Override
    public Type<?> visitTypeParameter(final Type<?> type, final TypeBindings bindings) {
        return visit(type.getExtendsBound());
    }

    @Override
    public Type<?> visitWildcardType(final Type<?> type, final TypeBindings bindings) {
        return visit(type.getExtendsBound());
    }

    @Override
    public Type<?> visitArrayType(final Type<?> type, final TypeBindings bindings) {
        final Type<?> oldElementType = type.getElementType();
        final Type<?> newElementType = visit(oldElementType);

        if (newElementType != oldElementType) {
            return newElementType.makeArrayType();
        }

        return type;
    }

    @Override
    public FieldInfo visitField(final Type<?> declaringType, final FieldInfo field, final TypeBindings bindings) {
        final Type<?> oldFieldType = field.getFieldType();
        final Type<?> newFieldType = visit(field.getFieldType(), bindings);

        if (TypeUtils.areEquivalent(oldFieldType, newFieldType) &&
            TypeUtils.areEquivalent(field.getDeclaringType(), declaringType)) {

            return field;
        }

        return new ErasedField(
            field,
            declaringType,
            newFieldType
        );
    }

    @Override
    public MethodInfo visitMethod(final Type<?> declaringType, final MethodInfo method, final TypeBindings bindings) {
        final Type<?> oldReturnType = method.getReturnType();
        final Type<?> returnType = visit(oldReturnType, bindings);
        final ParameterList oldParameters = method.getParameters();
        final ParameterList newParameters = visitParameters(oldParameters, bindings);
        final TypeList oldThrownTypes = method.getThrownTypes();
        final Type<?>[] newThrownTypes = new Type<?>[oldThrownTypes.size()];

        boolean hasChanged = !oldReturnType.equals(returnType) || oldParameters != newParameters;
        boolean thrownTypesChanged = false;

        for (int i = 0, n = newThrownTypes.length; i < n; i++) {
            final Type<?> oldThrownType = oldThrownTypes.get(i);
            final Type<?> newThrownType = visit(oldThrownType, bindings);

            newThrownTypes[i] = newThrownType;

            if (!oldThrownType.equals(newThrownType)) {
                thrownTypesChanged = true;
            }
        }

        hasChanged |= thrownTypesChanged;

        if (!hasChanged) {
            if (!TypeUtils.areEquivalent(method.getDeclaringType(), declaringType)) {
                return MethodInfo.declaredOn(method, declaringType, method.getReflectedType());
            }
            return method;
        }

        return new ErasedMethod(
            method,
            declaringType,
            newParameters,
            returnType,
            thrownTypesChanged ? new TypeList(newThrownTypes) : oldThrownTypes,
            TypeBindings.empty()
        );
    }
}