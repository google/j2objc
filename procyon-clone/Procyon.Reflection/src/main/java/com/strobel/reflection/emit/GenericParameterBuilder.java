/*
 * GenericParameterBuilder.java
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
import com.strobel.util.ContractUtils;

import javax.lang.model.type.TypeKind;
import java.util.Set;

/**
 * @author Mike Strobel
 */
@SuppressWarnings("unchecked")
public final class GenericParameterBuilder<T> extends Type<T> {
    @SuppressWarnings("PackageVisibleField")
    final TypeBuilder typeBuilder;

    GenericParameterBuilder(final TypeBuilder typeBuilder) {
        this.typeBuilder = VerifyArgument.notNull(typeBuilder, "type");
    }

    @Override
    public Type getDeclaringType() {
        return typeBuilder.getDeclaringType();
    }

    @Override
    public int getModifiers() {
        return typeBuilder.getModifiers();
    }

    @Override
    public Type getReflectedType() {
        return typeBuilder.getReflectedType();
    }

    @Override
    public String getName() {
        return typeBuilder.getName();
    }

    @Override
    protected String getClassFullName() {
        return typeBuilder.getClassFullName();
    }

    @Override
    protected String getClassSimpleName() {
        return typeBuilder.getClassSimpleName();
    }

    @Override
    public String getShortName() {
        return typeBuilder.getShortName();
    }

    @Override
    public Type<? super T> getBaseType() {
        return typeBuilder.getBaseType();
    }

    @Override
    public StringBuilder appendBriefDescription(final StringBuilder sb) {
        return typeBuilder.appendBriefDescription(sb);
    }

    @Override
    public StringBuilder appendErasedDescription(final StringBuilder sb) {
        return typeBuilder.appendErasedDescription(sb);
    }

    @Override
    public StringBuilder appendErasedSignature(final StringBuilder sb) {
        return typeBuilder.appendErasedSignature(sb);
    }

    @Override
    public StringBuilder appendDescription(final StringBuilder sb) {
        return typeBuilder.appendDescription(sb);
    }

    @Override
    public StringBuilder appendSignature(final StringBuilder sb) {
        return typeBuilder.appendSignature(sb);
    }

    @Override
    public StringBuilder appendSimpleDescription(final StringBuilder sb) {
        return typeBuilder.appendSimpleDescription(sb);
    }

    @Override
    public StringBuilder appendGenericSignature(final StringBuilder sb) {
        return typeBuilder.appendGenericSignature(sb);
    }

    @Override
    public ConstructorInfo getConstructor(final Set<BindingFlags> bindingFlags, final CallingConvention callingConvention, final Type... parameterTypes) {
        throw ContractUtils.unsupported();
    }

    @Override
    public ConstructorList getConstructors(final Set<BindingFlags> bindingFlags) {
        throw ContractUtils.unsupported();
    }

    @Override
    protected ConstructorList getDeclaredConstructors() {
        throw ContractUtils.unsupported();
    }

    @Override
    protected FieldList getDeclaredFields() {
        throw ContractUtils.unsupported();
    }

    @Override
    protected MethodList getDeclaredMethods() {
        throw ContractUtils.unsupported();
    }

    @Override
    public MemberList getMembers(final Set<BindingFlags> bindingFlags, final Set<MemberType> memberTypes) {
        throw ContractUtils.unsupported();
    }

    @Override
    public MemberList getMember(final String name, final Set<BindingFlags> bindingFlags, final Set<MemberType> memberTypes) {
        throw ContractUtils.unsupported();
    }

    @Override
    public MethodInfo getMethod(
        final String name,
        final Set<BindingFlags> bindingFlags,
        final CallingConvention callingConvention,
        final Type... parameterTypes) {

        throw ContractUtils.unsupported();
    }

    @Override
    public MethodList getMethods(final Set<BindingFlags> bindingFlags, final CallingConvention callingConvention) {
        throw ContractUtils.unsupported();
    }

    @Override
    public Type<?> getNestedType(final String fullName, final Set<BindingFlags> bindingFlags) {
        throw ContractUtils.unsupported();
    }

    @Override
    public TypeList getNestedTypes(final Set<BindingFlags> bindingFlags) {
        throw ContractUtils.unsupported();
    }

    @Override
    public FieldList getFields(final Set<BindingFlags> bindingFlags) {
        throw ContractUtils.unsupported();
    }

    @Override
    public FieldInfo getField(final String name, final Set<BindingFlags> bindingFlags) {
        throw ContractUtils.unsupported();
    }

    @Override
    public Package getPackage() {
        throw ContractUtils.unsupported();
    }

    @Override
    public Type<?> getSuperBound() {
        return typeBuilder.getSuperBound();
    }

    @Override
    public String getFullName() {
        return typeBuilder.getFullName();
    }

    @Override
    public Type<?> getExtendsBound() {
        return typeBuilder.getExtendsBound();
    }

    @Override
    public MethodInfo getDeclaringMethod() {
        return (MethodInfo) typeBuilder.getDeclaringMethod();
    }

    @Override
    public boolean isSubTypeOf(final Type type) {
        return typeBuilder.isSubTypeOf(type);
    }

    @Override
    public boolean isGenericParameter() {
        return true;
    }

    @Override
    public boolean implementsInterface(final Type interfaceType) {
        return typeBuilder.implementsInterface(interfaceType);
    }

    @Override
    protected TypeBindings getTypeBindings() {
        throw ContractUtils.unsupported();
    }

    @Override
    public TypeList getTypeArguments() {
        return typeBuilder.getTypeArguments();
    }

    @Override
    public TypeList getInterfaces() {
        return typeBuilder.getInterfaces();
    }

    @Override
    public String getInternalName() {
        return typeBuilder.getInternalName();
    }

    @Override
    public TypeKind getKind() {
        return typeBuilder.getKind();
    }

    @Override
    public int getGenericParameterPosition() {
        return typeBuilder.getGenericParameterPosition();
    }

    @Override
    public Class<T> getErasedClass() {
        return (Class<T>) typeBuilder.getErasedClass();
    }

    @Override
    public boolean containsGenericParameters() {
        return typeBuilder.containsGenericParameters();
    }

    @Override
    public int hashCode() {
        return typeBuilder.hashCode();
    }

    @Override
    public boolean isAssignableFrom(final Type type) {
        return typeBuilder.isAssignableFrom(type);
    }

    @Override
    public boolean isBoundedType() {
        return typeBuilder.isBoundedType();
    }

    @Override
    public boolean isCompoundType() {
        return false;
    }

    @Override
    public boolean isEquivalentTo(final Type<?> other) {
        return other == this ||
               other == typeBuilder ||
               typeBuilder.isEquivalentTo(other);
    }

    @Override
    public boolean isGenericType() {
        return false;
    }

    @Override
    public boolean isGenericTypeDefinition() {
        return false;
    }

    @Override
    public boolean hasExtendsBound() {
        return false;
    }

    @Override
    public boolean isInstance(final Object o) {
        return typeBuilder.isInstance(o);
    }

    @Override
    public boolean isNested() {
        return typeBuilder.isNested();
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public <P, R> R accept(final TypeVisitor<P, R> visitor, final P parameter) {
        return (R)typeBuilder.accept(visitor, parameter);
    }

    public void setBaseTypeConstraint(final Type<?> baseType) {
        typeBuilder.setBaseType(baseType);
    }

    public void setInterfaceConstraints(final TypeList interfaceConstraints) {
        typeBuilder.setInterfaces(interfaceConstraints);
    }
}
