/*
 * PrimitiveType.java
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

import javax.lang.model.type.TypeKind;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author strobelm
 */
final class PrimitiveType<T> extends Type<T> {

    private final Class<T> _class;
    private final String _signature;
    private final String _description;
    private final TypeKind _kind;

    PrimitiveType(final Class<T> clazz, final char signature, final String description, final TypeKind kind) {
        _class = VerifyArgument.notNull(clazz, "clazz");

        if (!clazz.isPrimitive()) {
            throw Error.notPrimitiveType(clazz);
        }

        _kind = VerifyArgument.notNull(kind, "kind");
        _signature = String.valueOf(signature);
        _description = VerifyArgument.notNull(description, "description");
    }

    @Override
    public TypeKind getKind() {
        return _kind;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public Class<T> getErasedClass() {
        return _class;
    }

    @Override
    protected TypeBindings getTypeBindings() {
        return TypeBindings.empty();
    }

    @Override
    public MemberList<? extends MemberInfo> getMember(final String name, final Set<BindingFlags> bindingFlags, final Set<MemberType> memberTypes) {
        return MemberList.empty();
    }

    @Override
    public FieldInfo getField(final String name, final Set<BindingFlags> bindingFlags) {
        return null;
    }

    @Override
    public MethodInfo getMethod(
        final String name,
        final Set<BindingFlags> bindingFlags,
        final CallingConvention callingConvention,
        final Type... parameterTypes) {
        return null;
    }

    @Override
    public ConstructorInfo getConstructor(final Set<BindingFlags> bindingFlags, final CallingConvention callingConvention, final Type... parameterTypes) {
        return null;
    }

    @Override
    public MemberList getMembers(final Set<BindingFlags> bindingFlags, final Set<MemberType> memberTypes) {
        return MemberList.empty();
    }

    @Override
    public FieldList getFields(final Set<BindingFlags> bindingFlags) {
        return FieldList.empty();
    }

    @Override
    public MethodList getMethods(final Set<BindingFlags> bindingFlags, final CallingConvention callingConvention) {
        return MethodList.empty();
    }

    @Override
    public ConstructorList getConstructors(final Set<BindingFlags> bindingFlags) {
        return ConstructorList.empty();
    }

    @Override
    public TypeList getNestedTypes(final Set<BindingFlags> bindingFlags) {
        return TypeList.empty();
    }

    @Override
    protected Type<T[]> createArrayType() {
        if (getKind() == TypeKind.VOID) {
            throw ContractUtils.unsupported();
        }
        return new ArrayType<>(this);
    }

    @Override
    public MemberType getMemberType() {
        return MemberType.TypeInfo;
    }

    @Override
    public String getFullName() {
        return _class.getName();
    }

    @Override
    public String getInternalName() {
        return _signature;
    }

    @Override
    public Type getDeclaringType() {
        return null;
    }

    @Override
    public int getModifiers() {
        return _class.getModifiers();
    }

    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
        return _class.isAnnotationPresent(annotationClass);
    }

    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        return _class.getAnnotation(annotationClass);
    }

    @NotNull
    @Override
    public Annotation[] getAnnotations() {
        return _class.getAnnotations();
    }

    @NotNull
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return _class.getDeclaredAnnotations();
    }

    @Override
    public <P, R> R accept(final TypeVisitor<P, R> visitor, final P parameter) {
        return visitor.visitPrimitiveType(this, parameter);
    }

    @Override
    public StringBuilder appendSignature(final StringBuilder sb) {
        sb.append(_signature);
        return sb;
    }

    @Override
    public StringBuilder appendErasedSignature(final StringBuilder sb) {
        sb.append(_signature);
        return sb;
    }

    @Override
    public StringBuilder appendDescription(final StringBuilder sb) {
        sb.append(_description);
        return sb;
    }

    @Override
    public StringBuilder appendBriefDescription(final StringBuilder sb) {
        sb.append(_description);
        return sb;
    }

    @Override
    public StringBuilder appendSimpleDescription(final StringBuilder sb) {
        sb.append(_description);
        return sb;
    }
}
