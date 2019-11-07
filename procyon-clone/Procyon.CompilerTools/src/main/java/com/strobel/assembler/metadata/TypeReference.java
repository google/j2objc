/*
 * TypeReference.java
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

import com.strobel.core.ArrayUtilities;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.util.ContractUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class TypeReference extends MemberReference implements IGenericParameterProvider, IGenericContext {
    private String _name;
    private TypeReference _declaringType;
    private ArrayType _arrayType;

    public TypeReference() {
    }

    @Override
    public boolean containsGenericParameters() {
        if (isGenericType()) {
            if (isGenericDefinition()) {
                if (hasGenericParameters()) {
                    return true;
                }
            }
            else if (this instanceof IGenericInstance) {
                final List<TypeReference> typeArguments = ((IGenericInstance) this).getTypeArguments();

                for (int i = 0, n = typeArguments.size(); i < n; i++) {
                    if (typeArguments.get(i).containsGenericParameters()) {
                        return true;
                    }
                }
            }
        }

        return super.containsGenericParameters();
    }

    @Override
    public String getName() {
        return _name;
    }

    public String getPackageName() {
        return StringUtilities.EMPTY;
    }

    @Override
    public TypeReference getDeclaringType() {
        return _declaringType;
    }

    @Override
    public boolean isEquivalentTo(final MemberReference member) {
        return member instanceof TypeReference &&
               MetadataResolver.areEquivalent(this, (TypeReference) member);
    }

    protected void setName(final String name) {
        _name = name;
    }

    protected final void setDeclaringType(final TypeReference declaringType) {
        _declaringType = declaringType;
    }

    public abstract String getSimpleName();

    public String getFullName() {
        final StringBuilder name = new StringBuilder();
        appendName(name, true, true);
        return name.toString();
    }

    public String getInternalName() {
        final StringBuilder name = new StringBuilder();
        appendName(name, true, false);
        return name.toString();
    }

    public TypeReference getUnderlyingType() {
        return this;
    }

    public TypeReference getElementType() {
        return null;
    }

    public abstract <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter);

    @Override
    public int hashCode() {
        return getInternalName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof TypeReference &&
               MetadataHelper.isSameType(this, (TypeReference) obj, true);
    }

    // <editor-fold defaultstate="collapsed" desc="Specification Factories">

    public TypeReference makeArrayType() {
        if (_arrayType == null) {
            synchronized (this) {
                if (_arrayType == null) {
                    _arrayType = ArrayType.create(this);
                }
            }
        }
        return _arrayType;
    }

    public TypeReference makeGenericType(final List<? extends TypeReference> typeArguments) {
        VerifyArgument.notNull(typeArguments, "typeArguments");

        return makeGenericType(
            typeArguments.toArray(new TypeReference[typeArguments.size()])
        );
    }

    public TypeReference makeGenericType(final TypeReference... typeArguments) {
        VerifyArgument.noNullElementsAndNotEmpty(typeArguments, "typeArguments");

        final TypeReference[] adjustedTypeArguments = Arrays.copyOf(typeArguments, typeArguments.length);

        if (checkRecursive(this, ArrayUtilities.asUnmodifiableList(adjustedTypeArguments))) {
            for (int i = 0; i < adjustedTypeArguments.length; i++) {
                final TypeReference t = adjustedTypeArguments[i];
                adjustedTypeArguments[i] = t.isGenericType() ? t.getRawType() : t;
            }
        }

        if (isGenericDefinition()) {
            return new ParameterizedType(
                this,
                ArrayUtilities.asUnmodifiableList(adjustedTypeArguments)
            );
        }

        if (this instanceof IGenericInstance) {
            return new ParameterizedType(
                (TypeReference) ((IGenericInstance) this).getGenericDefinition(),
                ArrayUtilities.asUnmodifiableList(adjustedTypeArguments)
            );
        }

        throw Error.notGenericType(this);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Type Bounds">

    public boolean isWildcardType() {
        return false;
    }

    public boolean isCompoundType() {
        return false;
    }

    public boolean isBoundedType() {
        return this.isGenericParameter() ||
               this.isWildcardType() ||
               this instanceof ICapturedType ||
               this instanceof CompoundTypeReference;
    }

    public boolean isUnbounded() {
        return true;
    }

    public boolean hasExtendsBound() {
        return isGenericParameter() ||
               (isWildcardType() &&
                !BuiltinTypes.Object.equals(getExtendsBound()) &&
                MetadataResolver.areEquivalent(BuiltinTypes.Bottom, getSuperBound()));
    }

    public boolean hasSuperBound() {
        return isWildcardType() &&
               !MetadataResolver.areEquivalent(BuiltinTypes.Bottom, getSuperBound());
    }

    public TypeReference getExtendsBound() {
        throw ContractUtils.unsupported();
    }

    public TypeReference getSuperBound() {
        throw ContractUtils.unsupported();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Type Attributes">

    public JvmType getSimpleType() {
        return JvmType.Object;
    }

    public boolean isNested() {
        return getDeclaringType() != null && !isGenericParameter();
    }

    public boolean isArray() {
        return getSimpleType() == JvmType.Array;
    }

    public boolean isPrimitive() {
        return false;
    }

    public boolean isVoid() {
        return false;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Generics">

    @Override
    public boolean hasGenericParameters() {
        return !getGenericParameters().isEmpty();
    }

    @Override
    public boolean isGenericDefinition() {
        return hasGenericParameters() &&
               isDefinition();
    }

    @Override
    public List<GenericParameter> getGenericParameters() {
        return Collections.emptyList();
    }

    public boolean isGenericParameter() {
        return getSimpleType() == JvmType.TypeVariable;
    }

    public boolean isGenericType() {
        return hasGenericParameters();
    }

    public TypeReference getRawType() {
        if (isGenericType()) {
            final TypeReference underlyingType = getUnderlyingType();

            if (underlyingType != this) {
                return underlyingType.getRawType();
            }

            return new RawType(this);
        }
        throw ContractUtils.unsupported();
    }

    @Override
    public GenericParameter findTypeVariable(final String name) {
        for (final GenericParameter genericParameter : getGenericParameters()) {
            if (StringUtilities.equals(genericParameter.getName(), name)) {
                return genericParameter;
            }
        }

        final TypeReference declaringType = getDeclaringType();

        if (declaringType != null) {
            return declaringType.findTypeVariable(name);
        }

        return null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Name and Signature Formatting">

    /**
     * Human-readable brief description of a type or member, which does not include information super types, thrown exceptions, or modifiers other than
     * 'static'.
     */
    public String getBriefDescription() {
        return appendBriefDescription(new StringBuilder()).toString();
    }

    /**
     * Human-readable full description of a type or member, which includes specification of super types (in brief format), thrown exceptions, and modifiers.
     */
    public String getDescription() {
        return appendDescription(new StringBuilder()).toString();
    }

    /**
     * Human-readable erased description of a type or member.
     */
    public String getErasedDescription() {
        return appendErasedDescription(new StringBuilder()).toString();
    }

    /**
     * Human-readable simple description of a type or member, which does not include information super type or fully-qualified type names.
     */
    public String getSimpleDescription() {
        return appendSimpleDescription(new StringBuilder()).toString();
    }

    @Override
    protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
        final String simpleName = getSimpleName();
        final TypeReference declaringType = getDeclaringType();

        if (dottedName && simpleName != null && declaringType != null) {
            return declaringType.appendName(sb, fullName, true).append('.').append(simpleName);
        }

        final String name = fullName ? getName() : simpleName;
        final String packageName = fullName ? getPackageName() : null;

        if (StringUtilities.isNullOrEmpty(packageName)) {
            return sb.append(name);
        }

        if (dottedName) {
            return sb.append(packageName)
                     .append('.')
                     .append(name);
        }

        final int packageEnd = packageName.length();

        for (int i = 0; i < packageEnd; i++) {
            final char c = packageName.charAt(i);
            sb.append(c == '.' ? '/' : c);
        }

        sb.append('/');

        return sb.append(name);
    }

    protected StringBuilder appendBriefDescription(final StringBuilder sb) {
        StringBuilder s = appendName(sb, true, true);

        final List<? extends TypeReference> typeArguments;

        if (this instanceof IGenericInstance) {
            typeArguments = ((IGenericInstance) this).getTypeArguments();
        }
        else if (this.isGenericDefinition()) {
            typeArguments = getGenericParameters();
        }
        else {
            typeArguments = Collections.emptyList();
        }

        final int count = typeArguments.size();

        if (count > 0) {
            s.append('<');
            for (int i = 0; i < count; ++i) {
                if (i != 0) {
                    s.append(", ");
                }
                s = typeArguments.get(i).appendBriefDescription(s);
            }
            s.append('>');
        }

        return s;
    }

    protected StringBuilder appendSimpleDescription(final StringBuilder sb) {
        StringBuilder s = sb.append(getSimpleName());

        if (isGenericType()) {
            final List<? extends TypeReference> typeArguments;

            if (this instanceof IGenericInstance) {
                typeArguments = ((IGenericInstance) this).getTypeArguments();
            }
            else {
                typeArguments = getGenericParameters();
            }

            final int count = typeArguments.size();

            if (count > 0) {
                s.append('<');
                for (int i = 0; i < count; ++i) {
                    if (i != 0) {
                        s.append(", ");
                    }

                    final TypeReference typeArgument = typeArguments.get(i);

                    if (typeArgument instanceof GenericParameter) {
                        s.append(typeArgument.getSimpleName());
                    }
                    else {
                        s = typeArgument.appendSimpleDescription(s);
                    }
                }
                s.append('>');
            }
        }

        return s;
    }

    protected StringBuilder appendErasedDescription(final StringBuilder sb) {
        return appendName(sb, true, true);
    }

    protected StringBuilder appendDescription(final StringBuilder sb) {
        StringBuilder s = appendName(sb, false, true);

        if (this instanceof IGenericInstance) {
            final List<TypeReference> typeArguments = ((IGenericInstance) this).getTypeArguments();
            final int count = typeArguments.size();

            if (count > 0) {
                s.append('<');
                for (int i = 0; i < count; ++i) {
                    if (i != 0) {
                        s.append(", ");
                    }
                    s = typeArguments.get(i).appendBriefDescription(s);
                }
                s.append('>');
            }
        }

        return s;
    }

    protected StringBuilder appendSignature(final StringBuilder sb) {
        if (isGenericParameter()) {
            sb.append('T');
            sb.append(getName());
            sb.append(';');
            return sb;
        }

        return appendClassSignature(sb);
    }

    protected StringBuilder appendErasedSignature(final StringBuilder sb) {
        if (isGenericType() && !isGenericDefinition()) {
            return getUnderlyingType().appendErasedSignature(sb);
        }
        return appendErasedClassSignature(sb);
    }

    @Override
    public String toString() {
        return getBriefDescription();
    }

    protected StringBuilder appendGenericSignature(final StringBuilder sb) {
        StringBuilder s = sb;

        if (isGenericParameter()) {
            final TypeReference extendsBound = getExtendsBound();
            final TypeDefinition resolvedBound = extendsBound.resolve();

            s.append(getName());

            if (resolvedBound != null && resolvedBound.isInterface()) {
                s.append(':');
            }

            s.append(':');
            s = extendsBound.appendSignature(s);

            return s;
        }

        if (this instanceof IGenericInstance) {
            final List<TypeReference> typeArguments = ((IGenericInstance) this).getTypeArguments();
            final int count = typeArguments.size();

            if (count > 0) {
                s.append('<');
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0; i < count; ++i) {
                    s = typeArguments.get(i).appendGenericSignature(s);
                }
                s.append('>');
            }
        }

        return s;
    }

    protected StringBuilder appendClassSignature(final StringBuilder sb) {
        StringBuilder s = sb;

        s.append('L');
        s = appendName(s, true, false);

        if (this instanceof IGenericInstance) {
            final List<TypeReference> typeArguments = ((IGenericInstance) this).getTypeArguments();
            final int count = typeArguments.size();

            if (count > 0) {
                s.append('<');
                for (int i = 0; i < count; ++i) {
                    final TypeReference type = typeArguments.get(i);
                    if (type.isGenericDefinition()) {
                        s = type.appendErasedSignature(s);
                    }
                    else {
                        s = type.appendSignature(s);
                    }
                }
                s.append('>');
            }
        }

        s.append(';');
        return s;
    }

    protected StringBuilder appendErasedClassSignature(StringBuilder sb) {
        sb.append('L');
        sb = appendName(sb, true, false);
        sb.append(';');
        return sb;
    }

    protected StringBuilder appendClassDescription(final StringBuilder sb) {
        StringBuilder s = sb;

        appendName(sb, true, true);

        if (this instanceof IGenericInstance) {
            final List<TypeReference> typeArguments = ((IGenericInstance) this).getTypeArguments();
            final int count = typeArguments.size();

            if (count > 0) {
                s.append('<');
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0; i < count; ++i) {
                    s = typeArguments.get(i).appendErasedClassSignature(s);
                }
                s.append('>');
            }
        }

        return s;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Member Resolution">

    public TypeDefinition resolve() {
        final TypeReference declaringType = getDeclaringType();

        return declaringType != null ? declaringType.resolve(this) : null;
    }

    public FieldDefinition resolve(final FieldReference field) {
        final TypeDefinition resolvedType = this.resolve();

        if (resolvedType != null) {
            return MetadataResolver.getField(resolvedType.getDeclaredFields(), field);
        }

        return null;
    }

    public MethodDefinition resolve(final MethodReference method) {
        final TypeDefinition resolvedType = this.resolve();

        if (resolvedType != null) {
            return MetadataResolver.getMethod(resolvedType.getDeclaredMethods(), method);
        }

        return null;
    }

    public TypeDefinition resolve(final TypeReference type) {
        final TypeDefinition resolvedType = this.resolve();

        if (resolvedType != null) {
            return MetadataResolver.getNestedType(resolvedType.getDeclaredTypes(), type);
        }

        return null;
    }

    protected static boolean checkRecursive(final TypeReference type, final List<? extends TypeReference> arguments) {
        //noinspection SimplifiableIfStatement
        if (type.isGenericParameter() || type.isWildcardType()) {
            return false;
        }
        return checkRecursiveCore(type.getInternalName(), arguments, 0);
    }

    private static boolean checkRecursiveCore(final String typeName, final List<? extends TypeReference> arguments, final int depth) {
        for (final TypeReference argument : arguments) {
            if (argument instanceof IGenericInstance) {
                final String argumentName = argument.getInternalName();

                if (StringUtilities.equals(argumentName, typeName)) {
                    if (depth > 3) {
                        return true;
                    }

                    if (checkRecursiveCore(typeName, ((IGenericInstance) argument).getTypeArguments(), depth + 1)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // </editor-fold>
}
