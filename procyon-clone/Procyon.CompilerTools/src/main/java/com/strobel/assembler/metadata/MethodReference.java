/*
 * MethodReference.java
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

import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.util.ContractUtils;

import java.util.Collections;
import java.util.List;

/**
 * User: Mike Strobel
 * Date: 1/6/13
 * Time: 2:29 PM
 */
public abstract class MethodReference extends MemberReference implements IMethodSignature,
                                                                         IGenericParameterProvider,
                                                                         IGenericContext {
    protected final static String CONSTRUCTOR_NAME = "<init>";
    protected final static String STATIC_INITIALIZER_NAME = "<clinit>";

    // <editor-fold defaultstate="collapsed" desc="Signature">

    public abstract TypeReference getReturnType();

    public boolean hasParameters() {
        return !getParameters().isEmpty();
    }

    public abstract List<ParameterDefinition> getParameters();

    public List<TypeReference> getThrownTypes() {
        return Collections.emptyList();
    }

    @Override
    public void invalidateSignature() {
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Method Attributes">

    @Override
    public boolean isSpecialName() {
        return CONSTRUCTOR_NAME.equals(getName()) ||
               STATIC_INITIALIZER_NAME.equals(getName());
    }

    @Override
    public boolean containsGenericParameters() {
        if (super.containsGenericParameters() || hasGenericParameters()) {
            return true;
        }

        if (getReturnType().containsGenericParameters()) {
            return true;
        }

        if (hasParameters()) {
            final List<ParameterDefinition> parameters = getParameters();

            for (int i = 0, n = parameters.size(); i < n; i++) {
                if (parameters.get(i).getParameterType().containsGenericParameters()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean isEquivalentTo(final MemberReference member) {
        if (super.isEquivalentTo(member)) {
            return true;
        }

        if (member instanceof MethodReference) {
            final MethodReference method = (MethodReference) member;

            return StringUtilities.equals(method.getName(), this.getName()) &&
                   StringUtilities.equals(method.getErasedSignature(), this.getErasedSignature()) &&
                   MetadataResolver.areEquivalent(method.getDeclaringType(), this.getDeclaringType());
        }

        return false;

    }

    @Override
    protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
        if (fullName) {
            final TypeReference declaringType = getDeclaringType();

            if (declaringType != null) {
                return declaringType.appendName(sb, true, false).append('.').append(getName());
            }
        }

        return sb.append(getName());
    }

    public boolean isConstructor() {
        return MethodDefinition.CONSTRUCTOR_NAME.equals(getName());
    }

    public boolean isTypeInitializer() {
        return MethodDefinition.STATIC_INITIALIZER_NAME.equals(getName());
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Generics">

    public boolean isGenericMethod() {
        return hasGenericParameters();
    }

    @Override
    public boolean hasGenericParameters() {
        return !getGenericParameters().isEmpty();
    }

    @Override
    public boolean isGenericDefinition() {
        return hasGenericParameters() &&
               isDefinition();
    }

    public List<GenericParameter> getGenericParameters() {
        return Collections.emptyList();
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

    // <editor-fold defaultstate="collapsed" desc="Member Resolution">

    public MethodDefinition resolve() {
        final TypeReference declaringType = getDeclaringType();

        if (declaringType == null)
            throw ContractUtils.unsupported();

        return declaringType.resolve(this);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Name and Signature Formatting">

    @Override
    public StringBuilder appendSignature(final StringBuilder sb) {
        return appendSignature(this, sb);
    }

    @Override
    public StringBuilder appendErasedSignature(final StringBuilder sb) {
        return appendErasedSignature(this, sb);
    }

    static StringBuilder appendSignature(final IMethodSignature method, final StringBuilder sb) {
        final List<ParameterDefinition> parameters = method.getParameters();

        StringBuilder s = sb;
        s.append('(');

        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterDefinition p = parameters.get(i);
            s = p.getParameterType().appendSignature(s);
        }

        s.append(')');
        s = method.getReturnType().appendSignature(s);

        return s;
    }

    static StringBuilder appendErasedSignature(final IMethodSignature method, final StringBuilder sb) {
        StringBuilder s = sb;
        s.append('(');

        final List<ParameterDefinition> parameterTypes = method.getParameters();

        for (int i = 0, n = parameterTypes.size(); i < n; ++i) {
            s = parameterTypes.get(i).getParameterType().appendErasedSignature(s);
        }

        s.append(')');
        s = method.getReturnType().appendErasedSignature(s);

        return s;
    }


    // </editor-fold>
}

