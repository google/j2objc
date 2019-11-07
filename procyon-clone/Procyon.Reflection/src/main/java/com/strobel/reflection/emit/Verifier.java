/*
 * Verifier.java
 *
 * Copyright (c) 2015 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.reflection.emit;

import com.strobel.core.StringUtilities;
import com.strobel.reflection.*;
import com.strobel.util.ContractUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;

import static java.lang.String.format;

final class Verifier {
    private final static String VERIFY_LOCAL_VARIABLE_TYPES = "com.strobel.reflection.emit.Verifier.VerifyLocalVariableTypes";
    private final static GenericParameterResolver GENERIC_PARAMETER_RESOLVER = new GenericParameterResolver();

    private Verifier() {
    }

    public static void verify(final MethodBase method, final MethodBuilder scope) {
        final GenericParameterScopeVerifier methodLevelVerifier = new GenericParameterScopeVerifier();
        methodLevelVerifier.visit(method, scope);
    }

    public static void verify(final FieldInfo field, final MethodBuilder scope) {
        final GenericParameterScopeVerifier methodLevelVerifier = new GenericParameterScopeVerifier();
        methodLevelVerifier.visit(field, scope);
    }

    public static void verify(final TypeBuilder<?> type) {
        final GenericParameterScopeVerifier typeLevelVerifier = new GenericParameterScopeVerifier();

        typeLevelVerifier._frames.push(new VerifierFrame(FrameType.TYPE_SIGNATURE, type));

        typeLevelVerifier._frames.push(new VerifierFrame(FrameType.SUPER_CLASS, type));

        final Type<?> baseType = type.getBaseType();

        if (baseType != null) {
            typeLevelVerifier.visit(baseType, type);
        }

        typeLevelVerifier._frames.pop();

        typeLevelVerifier.visit(type.getExplicitInterfaces(), type, FrameType.SUPER_INTERFACE);

        if (type.isGenericTypeDefinition()) {
            typeLevelVerifier.visit(type.getGenericTypeParameters(), type, FrameType.TYPE_VARIABLE);
        }
        else if (type.isGenericType()) {
            typeLevelVerifier.visit(type.getTypeArguments(), type, FrameType.TYPE_ARGUMENT);
        }

        for (final FieldBuilder field : type.fieldBuilders) {
            typeLevelVerifier.visit(field, type);
        }

        for (final ConstructorBuilder ctor : type.constructorBuilders) {
            final GenericParameterScopeVerifier methodLevelVerifier = new GenericParameterScopeVerifier();
            methodLevelVerifier._frames.addAll(typeLevelVerifier._frames);
            methodLevelVerifier._visitedTypes.addAll(typeLevelVerifier._visitedTypes);
            methodLevelVerifier.visit(ctor.getMethodBuilder(), ctor.getMethodBuilder());
        }

        for (final MethodBuilder method : type.methodBuilders) {
            final GenericParameterScopeVerifier methodLevelVerifier = new GenericParameterScopeVerifier();
            methodLevelVerifier._frames.addAll(typeLevelVerifier._frames);
            methodLevelVerifier._visitedTypes.addAll(typeLevelVerifier._visitedTypes);
            methodLevelVerifier.visit(method, method);
        }

        typeLevelVerifier._frames.pop();
    }

    private final static class GenericParameterScopeVerifier extends SimpleVisitor<MemberInfo, Void> {
        private final Stack<VerifierFrame> _frames = new Stack<>();
        private final Set<Type<?>> _visitedTypes = new LinkedHashSet<>();

        public Void visit(final FieldInfo field, final MemberInfo scope) {
//            field.getDeclaringType().accept(this, scope);
            _frames.push(new VerifierFrame(FrameType.FIELD_SIGNATURE, field));
            field.getFieldType().accept(this, scope);
            _frames.pop();
            return null;
        }

        public Void visit(final MethodBase method, final MemberInfo scope) {
//            method.getDeclaringType().accept(this, scope);

            _frames.push(new VerifierFrame(FrameType.METHOD_SIGNATURE, method));

            if (method instanceof MethodInfo) {
                final MethodInfo m = (MethodInfo) method;

                _frames.push(new VerifierFrame(FrameType.METHOD_RETURN_TYPE, method));
                m.getReturnType().accept(this, scope);
                _frames.pop();

                if (m.isGenericMethodDefinition()) {
                    visit(m.getGenericMethodParameters(), scope, FrameType.TYPE_VARIABLE);
                }
                else if (m.isGenericMethod()) {
                    visit(m.getTypeArguments(), scope, FrameType.TYPE_ARGUMENT);
                }
            }

            visit(method.getParameters().getParameterTypes(), scope, FrameType.METHOD_PARAMETER);
            visit(method.getThrownTypes(), scope, FrameType.METHOD_THROWS_LIST);

            if (StringUtilities.isTrue(System.getProperty(VERIFY_LOCAL_VARIABLE_TYPES))) {
                final MethodBuilder mb;

                if (method instanceof MethodBuilder) {
                    mb = (MethodBuilder) method;
                }
                else if (method instanceof ConstructorBuilder) {
                    mb = ((ConstructorBuilder) method).getMethodBuilder();
                }
                else {
                    mb = null;
                }

                if (mb != null && mb.generator != null) {
                    for (int i = 0; i < mb.generator.localCount; i++) {
                        final LocalBuilder local = mb.generator.locals[i];
                        _frames.push(new VerifierFrame(FrameType.LOCAL_VARIABLE, local.getLocalType(), local));
                        local.getLocalType().accept(this, scope);
                        _frames.pop();
                    }
                }
            }

            _frames.pop();

            return null;
        }

        @Override
        public Void visitTypeParameter(final Type<?> type, final MemberInfo scope) {
            super.visitTypeParameter(type, scope);

            if (type.hasSuperBound()) {
                _frames.push(new VerifierFrame(FrameType.TYPE_BOUND, type.getSuperBound()));
                visit(type.getSuperBound(), scope);
                _frames.pop();
            }
            else if (type.hasExtendsBound() && !Types.Object.isEquivalentTo(type.getExtendsBound())) {
                _frames.push(new VerifierFrame(FrameType.TYPE_BOUND, type.getExtendsBound()));
                visit(type.getExtendsBound(), scope);
                _frames.pop();
            }

            if (!type.isGenericParameter() || !_visitedTypes.add(type)) {
                return null;
            }

            if (GENERIC_PARAMETER_RESOLVER.visitScope(scope, type) != Boolean.TRUE) {
                final ArrayList<VerifierFrame> frames = new ArrayList<>(_frames);

                Collections.reverse(frames);

                throw new VerificationException(
                    typeVariableOutOfScopeError(type, scope),
                    frames.toArray(new VerifierFrame[frames.size()])
                );
            }

            return null;
        }

        public Void visit(final TypeList types, final MemberInfo scope, final FrameType frameType) {
            for (final Type<?> type : types) {
                _frames.push(new VerifierFrame(frameType, type));
                type.accept(this, scope);
                _frames.pop();
            }
            return null;
        }

        @Override
        public Void visitArrayType(final Type<?> type, final MemberInfo scope) {
            super.visitArrayType(type, scope);
            _frames.push(new VerifierFrame(FrameType.TYPE_SIGNATURE, type));
            type.getElementType().accept(this, scope);
            _frames.pop();
            return null;
        }

        @Override
        public Void visitClassType(final Type<?> type, final MemberInfo scope) {
            super.visitClassType(type, scope);

            _frames.push(new VerifierFrame(FrameType.TYPE_SIGNATURE, type));

            if (type.isGenericTypeDefinition()) {
                visit(type.getGenericTypeParameters(), scope, FrameType.TYPE_VARIABLE);
            }
            else if (type.isGenericType()) {
                visit(type.getTypeArguments(), scope, FrameType.TYPE_ARGUMENT);
            }

            _frames.pop();

            return null;
        }

        @Override
        public Void visitPrimitiveType(final Type<?> type, final MemberInfo scope) {
            return null;
        }

        @Override
        public Void visitWildcardType(final Type<?> type, final MemberInfo scope) {
            super.visitWildcardType(type, scope);

            if (type.isUnbounded()) {
                return null;
            }

            if (type.hasSuperBound()) {
                _frames.push(new VerifierFrame(FrameType.TYPE_BOUND, type.getSuperBound()));
                visit(type.getSuperBound(), scope);
                _frames.pop();
            }
            else {
                _frames.push(new VerifierFrame(FrameType.TYPE_BOUND, type.getExtendsBound()));
                visit(type.getExtendsBound(), scope);
                _frames.pop();
            }

            return null;
        }

        @Override
        public Void visitCapturedType(final Type<?> type, final MemberInfo scope) {
            super.visitCapturedType(type, scope);

            if (type instanceof ICapturedType) {
                visit(((ICapturedType) type).getWildcard(), scope);
            }

            return null;
        }
    }

    static String typeVariableOutOfScopeError(final Type<?> typeParameter, final MemberInfo scope) {
        final String site;
        final String owner;

        final MethodBase declaringMethod = typeParameter.getDeclaringMethod();

        final Type<?> declaringType = declaringMethod != null ? declaringMethod.getDeclaringType()
                                                              : typeParameter.getDeclaringType();

        if (declaringMethod != null) {
            owner = format(
                "method '%s' on type '%s'",
                declaringMethod.getSimpleDescription(),
                declaringType.getSimpleDescription()
            );
        }
        else {
            owner = "type '" + declaringType.getSimpleDescription() + "'";
        }

        if (scope instanceof MethodBase) {
            site = format(
                "method '%s' on type '%s'",
                scope.getSimpleDescription(),
                scope.getDeclaringType().getSimpleDescription()
            );
        }
        else {
            site = "type '" + scope.getSimpleDescription() + "'";
        }

        return format(
            "Type variable '%s' cannot be resolved in %s (variable owned by %s).",
            typeParameter.getName(),
            site,
            owner
        );
    }

    private final static class GenericParameterResolver extends SimpleVisitor<Type<?>, Boolean> {
        public Boolean visitScope(final MemberInfo scope, final Type<?> s) {
            if (scope instanceof MethodBase) {
                final MethodBase method = (MethodBase) scope;

                if (method.containsGenericParameter(s)) {
                    return Boolean.TRUE;
                }

                final Type<?> declaringType = method.getDeclaringType();

                if (declaringType != null && !declaringType.isStatic()) {
                    return declaringType.accept(this, s);
                }

                return Boolean.FALSE;
            }

            if (scope instanceof Type<?>) {
                final Type<?> type = (Type<?>) scope;

                if (type.containsGenericParameter(s)) {
                    return Boolean.TRUE;
                }

                if (!type.isStatic()) {
                    final Type<?> declaringType = type.getDeclaringType();

                    if (declaringType != null) {
                        return declaringType.accept(this, s);
                    }

                    return Boolean.FALSE;
                }
            }

            return Boolean.FALSE;
        }

        public Boolean visitTypes(final TypeList types, final Type<?> s) {
            for (final Type<?> type : types) {
                if (visit(type, s) == Boolean.TRUE) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }

        @Override
        public Boolean visitCapturedType(final Type<?> t, final Type<?> s) {
            return Boolean.FALSE;
        }

        @Override
        public Boolean visitClassType(final Type<?> type, final Type<?> s) {
            Boolean result;

            if (type.containsGenericParameter(s)) {
                return Boolean.TRUE;
            }

            final MethodBase declaringMethod = type.getDeclaringMethod();

            if (declaringMethod != null) {
                return visitScope(declaringMethod, s);
            }

            if (type.isStatic()) {
                return Boolean.FALSE;
            }

            final Type<?> declaringType = type.getDeclaringType();

            if (declaringType != null && declaringType != Type.NullType) {
                return visit(declaringType, s);
            }

            return Boolean.FALSE;
        }

        @Override
        public Boolean visitPrimitiveType(final Type<?> type, final Type<?> s) {
            return Boolean.FALSE;
        }

        @Override
        public Boolean visitTypeParameter(final Type<?> type, final Type<?> s) {
            if (type.isEquivalentTo(s)) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }

        @Override
        public Boolean visitWildcardType(final Type<?> type, final Type<?> s) {
            return Boolean.FALSE;
        }

        @Override
        public Boolean visitArrayType(final Type<?> type, final Type<?> s) {
            return Boolean.FALSE;
        }
    }

    enum FrameType {
        FIELD_SIGNATURE,
        METHOD_SIGNATURE,
        METHOD_RETURN_TYPE,
        METHOD_PARAMETER,
        METHOD_THROWS_LIST,
        METHOD_ARGUMENT,
        TYPE_SIGNATURE,
        TYPE_VARIABLE,
        LOCAL_VARIABLE,
        TYPE_ARGUMENT,
        TYPE_BOUND,
        SUPER_CLASS,
        SUPER_INTERFACE,
    }

    final static class VerifierFrame {
        private final FrameType _frameType;
        private final MemberInfo _member;
        private final Object _location;

        public VerifierFrame(final FrameType frameType, final MemberInfo member) {
            this(frameType, member, null);
        }

        public VerifierFrame(final FrameType frameType, final MemberInfo member, final Object location) {
            _frameType = frameType;
            _member = member;
            _location = location;
        }

        @Override
        public String toString() {
            switch (_frameType) {
                case FIELD_SIGNATURE: {
                    return "field: " + _member.getDescription();
                }

                case METHOD_SIGNATURE: {
                    return "method: " + _member.getDescription();
                }

                case METHOD_RETURN_TYPE: {
                    return "return type: " + _member.getBriefDescription();
                }

                case METHOD_THROWS_LIST: {
                    return "throws list: " + _member.getBriefDescription();
                }

                case METHOD_PARAMETER: {
                    if (_location instanceof ParameterInfo) {
                        final ParameterInfo p = (ParameterInfo) _location;

                        if (StringUtilities.isNullOrEmpty(p.getName())) {
                            return "parameter #" + p.getPosition() +
                                   ": " + _member.getSignature();
                        }

                        return "parameter #" + p.getPosition() +
                               " (" + p.getName() + "): " + _member.getSignature();
                    }
                    return "parameter: " + _member.getSignature();
                }

                case METHOD_ARGUMENT: {
                    if (_location instanceof ParameterInfo) {
                        final ParameterInfo p = (ParameterInfo) _location;

                        if (StringUtilities.isNullOrEmpty(p.getName())) {
                            return "argument #" + p.getPosition() +
                                   ": " + _member.getBriefDescription();
                        }

                        return "argument #" + p.getPosition() +
                               " (" + p.getName() + "): " + _member.getBriefDescription();
                    }
                    return "argument: " + _member.getBriefDescription();
                }

                case TYPE_SIGNATURE: {
                    return "type: " + _member.getDescription();
                }

                case TYPE_VARIABLE: {
                    return "type variable: " + _member.getDescription();
                }

                case TYPE_BOUND: {
                    return "type bound: " + _member.getDescription();
                }

                case SUPER_CLASS: {
                    return "super class: " + _member.getDescription();
                }

                case SUPER_INTERFACE: {
                    return "super interface: " + _member.getDescription();
                }

                case TYPE_ARGUMENT: {
                    if (_location instanceof Type<?> &&
                        ((Type<?>) _location).isGenericParameter()) {

                        final Type<?> gp = (Type<?>) _location;

                        return "type argument #" + gp.getGenericParameterPosition() +
                               " (" + gp.getName() + "): " + _member.getBriefDescription();
                    }
                    return "type argument: " + _member.getBriefDescription();
                }

                case LOCAL_VARIABLE: {
                    if (_location instanceof LocalVariableInfo) {
                        final LocalVariableInfo local = (LocalVariableInfo) _location;

                        if (local instanceof LocalBuilder) {
                            return "local variable #" + local.getLocalIndex() +
                                   " (" + ((LocalBuilder) local).getName() + "): " +
                                   local.getLocalType().getBriefDescription();
                        }

                        return "local variable #" + local.getLocalIndex() +
                               ": " + local.getLocalType().getBriefDescription();
                    }
                    return "local variable in method " + _member.getName();
                }

                default: {
                    throw ContractUtils.unreachable();
                }
            }
        }
    }
}
