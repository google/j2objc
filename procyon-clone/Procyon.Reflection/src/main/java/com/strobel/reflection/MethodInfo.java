/*
 * MethodInfo.java
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
import com.strobel.core.ArrayUtilities;
import com.strobel.core.Fences;
import com.strobel.core.VerifyArgument;
import com.strobel.util.ContractUtils;
import com.strobel.util.TypeUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;

/**
 * @author Mike Strobel
 */
public abstract class MethodInfo extends MethodBase {
    protected MethodInfo _erasedMethodDefinition;

    public final boolean isAbstract() {
        return Modifier.isAbstract(getModifiers());
    }

    public final boolean isDefault() {
        if (isAbstract() || isStatic()) {
            return false;
        }

        final Type declaringType = getDeclaringType();

        return declaringType != null &&
               declaringType.isInterface();
    }

    public abstract Type<?> getReturnType();

    @Override
    public final MemberType getMemberType() {
        return MemberType.Method;
    }

    public abstract Method getRawMethod();

    public Object getDefaultValue() {
        return getRawMethod().getDefaultValue();
    }

    @Override
    public String getName() {
        return getRawMethod().getName();
    }

    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        return getRawMethod().getAnnotation(annotationClass);
    }

    @NotNull
    @Override
    public Annotation[] getAnnotations() {
        return getRawMethod().getAnnotations();
    }

    @NotNull
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return getRawMethod().getDeclaredAnnotations();
    }

    @Override
    public boolean isEquivalentTo(final MemberInfo m) {
        return m instanceof MethodInfo &&
               super.isEquivalentTo(m) &&
               ((MethodInfo) m).getReturnType().isEquivalentTo(getReturnType());
    }

    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
        return getRawMethod().isAnnotationPresent(annotationClass);
    }

    public Object invoke(final Object instance, final Object... args) {
        final Method rawMethod = getRawMethod();

        if (rawMethod == null) {
            throw Error.rawMethodBindingFailure(this);
        }

        try {
            return rawMethod.invoke(instance, args);
        }
        catch (InvocationTargetException | IllegalAccessException e) {
            throw Error.targetInvocationException(e);
        }
    }

    public MethodInfo findOverriddenMethod() {
        final Type baseType = getDeclaringType().getBaseType();

        MethodInfo baseMethod;

        if (baseType != null && baseType != Type.NullType && (baseMethod = findBaseMethod(baseType)) != null) {
            return baseMethod;
        }

        for (final Type<?> ifType : getDeclaringType().getExplicitInterfaces()) {
            if ((baseMethod = findBaseMethod(ifType)) != null) {
                return baseMethod;
            }
        }

        return null;
    }

    public MethodInfo findBaseMethod(final Type<?> relativeTo) {
        VerifyArgument.notNull(relativeTo, "relativeTo");

        final Type<?> declaringType = getDeclaringType();

        if (!relativeTo.isAssignableFrom(declaringType)) {
            throw Error.invalidAncestorType(relativeTo, declaringType);
        }

        if (isStatic() || isPrivate()) {
            return null;
        }

        final ParameterList parameters = getParameters();

        return relativeTo.getMethod(
            getName(),
            BindingFlags.AllInstance,
            getCallingConvention(),
            parameters.getParameterTypes().toArray(new Type[parameters.size()])
        );
    }

    @Override
    public StringBuilder appendDescription(final StringBuilder sb) {
        StringBuilder s = new StringBuilder();

        for (final javax.lang.model.element.Modifier modifier : Flags.asModifierSet(getModifiers())) {
            s.append(modifier.toString());
            s.append(' ');
        }

        if (isGenericMethodDefinition()) {
            final TypeList genericParameters = getGenericMethodParameters();

            s.append('<');
            for (int i = 0, n = genericParameters.size(); i < n; i++) {
                if (i != 0) {
                    s.append(", ");
                }
                s = genericParameters.get(i).appendSimpleDescription(s);
            }
            s.append('>');
            s.append(' ');
        }

        Type returnType = getReturnType();

        while (returnType.isWildcardType()) {
            returnType = returnType.getExtendsBound();
        }

        if (returnType.isGenericParameter()) {
            s.append(returnType.getName());
        }
        else {
            s = returnType.appendSimpleDescription(s);
        }

        s.append(' ');
        s.append(getName());
        s.append('(');

        final ParameterList parameters = getParameters();

        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterInfo p = parameters.get(i);
            if (i != 0) {
                s.append(", ");
            }

            Type parameterType = p.getParameterType();

            while (parameterType.isWildcardType()) {
                parameterType = parameterType.getExtendsBound();
            }

            if (parameterType.isGenericParameter()) {
                s.append(parameterType.getName());
            }
            else {
                s = parameterType.appendSimpleDescription(s);
            }
        }

        s.append(')');

        final TypeList thrownTypes = getThrownTypes();

        if (!thrownTypes.isEmpty()) {
            s.append(" throws ");

            for (int i = 0, n = thrownTypes.size(); i < n; ++i) {
                final Type t = thrownTypes.get(i);
                if (i != 0) {
                    s.append(", ");
                }
                s = t.appendBriefDescription(s);
            }
        }

        return s;
    }

    @Override
    public StringBuilder appendSimpleDescription(final StringBuilder sb) {
        StringBuilder s = new StringBuilder();

        for (final javax.lang.model.element.Modifier modifier : Flags.asModifierSet(getModifiers())) {
            s.append(modifier.toString());
            s.append(' ');
        }

        if (isGenericMethodDefinition()) {
            final TypeList genericParameters = getGenericMethodParameters();

            s.append('<');
            for (int i = 0, n = genericParameters.size(); i < n; i++) {
                if (i != 0) {
                    s.append(", ");
                }
                s = genericParameters.get(i).appendSimpleDescription(s);
            }
            s.append('>');
            s.append(' ');
        }

        Type returnType = getReturnType();

        while (returnType.isWildcardType()) {
            returnType = returnType.getExtendsBound();
        }

        if (returnType.isGenericParameter()) {
            s.append(returnType.getName());
        }
        else {
            s = returnType.appendSimpleDescription(s);
        }

        s.append(' ');
        s.append(getName());
        s.append('(');

        final ParameterList parameters = getParameters();

        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterInfo p = parameters.get(i);
            if (i != 0) {
                s.append(", ");
            }

            Type parameterType = p.getParameterType();

            while (parameterType.isWildcardType()) {
                parameterType = parameterType.getExtendsBound();
            }

            if (parameterType.isGenericParameter()) {
                s.append(parameterType.getName());
            }
            else {
                s = parameterType.appendSimpleDescription(s);
            }
        }

        s.append(')');

        final TypeList thrownTypes = getThrownTypes();

        if (!thrownTypes.isEmpty()) {
            s.append(" throws ");

            for (int i = 0, n = thrownTypes.size(); i < n; ++i) {
                final Type t = thrownTypes.get(i);
                if (i != 0) {
                    s.append(", ");
                }
                s = t.appendSimpleDescription(s);
            }
        }

        return s;
    }

    @Override
    public StringBuilder appendBriefDescription(final StringBuilder sb) {
        StringBuilder s = new StringBuilder();

        Type returnType = getReturnType();

        while (returnType.isWildcardType()) {
            returnType = returnType.getExtendsBound();
        }

        if (returnType.isGenericParameter()) {
            s.append(returnType.getName());
        }
        else {
            s = returnType.appendBriefDescription(s);
        }

        s.append(' ');
        s.append(getName());
        s.append('(');

        final ParameterList parameters = getParameters();

        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterInfo p = parameters.get(i);
            if (i != 0) {
                s.append(", ");
            }

            Type parameterType = p.getParameterType();

            while (parameterType.isWildcardType()) {
                parameterType = parameterType.getExtendsBound();
            }

            if (parameterType.isGenericParameter()) {
                s.append(parameterType.getName());
            }
            else {
                s = parameterType.appendBriefDescription(s);
            }
        }

        s.append(')');

        return s;
    }

    @Override
    public StringBuilder appendErasedDescription(final StringBuilder sb) {
        if (isGenericMethod() && !isGenericMethodDefinition()) {
            return getGenericMethodDefinition().appendErasedDescription(sb);
        }

        for (final javax.lang.model.element.Modifier modifier : Flags.asModifierSet(getModifiers())) {
            sb.append(modifier.toString());
            sb.append(' ');
        }

        final TypeList parameterTypes = getParameters().getParameterTypes();

        StringBuilder s = getReturnType().appendErasedDescription(sb);

        s.append(' ');
        s.append(getName());
        s.append('(');

        for (int i = 0, n = parameterTypes.size(); i < n; ++i) {
            if (i != 0) {
                s.append(", ");
            }
            s = parameterTypes.get(i).appendErasedDescription(s);
        }

        s.append(')');
        return s;
    }

    @Override
    public StringBuilder appendSignature(final StringBuilder sb) {
        StringBuilder s = sb;

        if (isGenericMethod()) {
            final TypeList typeArguments = getTypeBindings().getBoundTypes();
            final int count = typeArguments.size();

            if (count > 0) {
                s.append('<');
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0; i < count; ++i) {
                    final Type type = typeArguments.get(i);
                    s = type.appendGenericSignature(s);
                }
                s.append('>');
            }
        }

        final ParameterList parameters = getParameters();

        s.append('(');

        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterInfo p = parameters.get(i);
            s = p.getParameterType().appendSignature(s);
        }

        s.append(')');
        s = getReturnType().appendSignature(s);

        return s;
    }

    @Override
    public StringBuilder appendErasedSignature(final StringBuilder sb) {
        StringBuilder s = sb;
        s.append('(');

        final TypeList parameterTypes = getParameters().getParameterTypes();

        for (int i = 0, n = parameterTypes.size(); i < n; ++i) {
            s = parameterTypes.get(i).appendErasedSignature(s);
        }

        s.append(')');
        s = getReturnType().appendErasedSignature(s);

        return s;
    }

    public boolean isGenericMethod() {
        return !getGenericMethodParameters().isEmpty();
    }

    public boolean isGenericMethodDefinition() {
        if (!isGenericMethod()) {
            return false;
        }

        final TypeBindings typeArguments = getTypeBindings();

        return !typeArguments.isEmpty() &&
               !typeArguments.hasBoundParameters();
    }

    protected TypeBindings getTypeBindings() {
        return TypeBindings.empty();
    }

    public TypeList getTypeArguments() {
        return getTypeBindings().getBoundTypes();
    }

    public TypeList getGenericMethodParameters() {
        return getTypeBindings().getGenericParameters();
    }

    public MethodInfo getGenericMethodDefinition() {
        if (isGenericMethod()) {
            if (isGenericMethodDefinition()) {
                return this;
            }
            throw ContractUtils.unreachable();
        }
        throw Error.notGenericMethod(this);
    }

    public MethodInfo getErasedMethodDefinition() {
        if (_erasedMethodDefinition != null) {
            return _erasedMethodDefinition;
        }

//        final Type<?> declaringType = getDeclaringType();
//
//        if (declaringType.isGenericTypeDefinition()) {
//            final Type<?> erasedType = declaringType.getErasedType();
//
//            final MemberList<? extends MemberInfo> members = erasedType.findMembers(
//                MemberType.methodsOnly(),
//                BindingFlags.fromMember(this),
//                Type.FilterRawMember,
//                getRawMethod()
//            );
//
//            assert !members.isEmpty();
//
//            final MethodInfo erasedTypeMethod = (MethodInfo) members.get(0);
//
//            final MethodInfo erasedMethodDefinition = erasedTypeMethod.isGenericMethod() ? erasedTypeMethod.getErasedMethodDefinition()
//                                                                                         : erasedTypeMethod;
//
//            if (TypeUtils.areEquivalent(getReflectedType(), erasedMethodDefinition.getReflectedType())) {
//                _erasedMethodDefinition = erasedMethodDefinition;
//            }
//            else {
//                _erasedMethodDefinition = new RuntimeMethodInfo(
//                    erasedMethodDefinition.getRawMethod(),
//                    erasedMethodDefinition.getDeclaringType(),
//                    getReflectedType().getCache(),
//                    erasedMethodDefinition.getModifiers(),
//                    erasedMethodDefinition instanceof RuntimeMethodInfo ? ((RuntimeMethodInfo) erasedMethodDefinition).getBindingFlags()
//                                                                        : BindingFlags.fromMember(erasedMethodDefinition),
//                    erasedMethodDefinition.getParameters(),
//                    erasedMethodDefinition.getReturnType(),
//                    erasedMethodDefinition.getThrownTypes(),
//                    erasedMethodDefinition.getTypeBindings()
//                );
//            }
//
//            return _erasedMethodDefinition;
//        }
//
//        if (isGenericMethod()) {
//            if (isGenericMethodDefinition()) {
//                final ParameterList oldParameters = getParameters();
//                final TypeList parameterTypes = Helper.erasure(oldParameters.getParameterTypes());
//
//                final ParameterInfo[] parameters = new ParameterInfo[oldParameters.size()];
//
//                for (int i = 0, n = parameters.length; i < n; i++) {
//                    final ParameterInfo oldParameter = oldParameters.get(i);
//                    if (parameterTypes.get(i) == oldParameter.getParameterType()) {
//                        parameters[i] = oldParameter;
//                    }
//                    else {
//                        parameters[i] = new ParameterInfo(
//                            oldParameter.getName(),
//                            oldParameter.getPosition(),
//                            parameterTypes.get(i)
//                        );
//                    }
//                }
//
//                _erasedMethodDefinition = new ReflectedMethod(
//                    declaringType.isGenericType() ? declaringType.getErasedType()
//                                                  : declaringType,
//                    getReflectedType(),
//                    getRawMethod(),
//                    new ParameterList(parameters),
//                    Helper.erasure(getReturnType()),
//                    Helper.erasure(getThrownTypes()),
//                    TypeBindings.empty()
//                );
//
//                return _erasedMethodDefinition;
//            }
//
//            _erasedMethodDefinition = getGenericMethodDefinition().getErasedMethodDefinition();
//        }
//        else {
//            _erasedMethodDefinition = this;
//        }

        final Type<?> declaringType = this.getDeclaringType();
        final Type<?> actualDeclaringType;

        if (declaringType.isGenericType()) {
            actualDeclaringType = declaringType.getErasedType();
        }
        else {
            actualDeclaringType = declaringType;
        }

        _erasedMethodDefinition = ErasedType.GenericEraser.visitMethod(
            actualDeclaringType,
            this.isGenericMethod() ? this.getGenericMethodDefinition()
                                   : this,
            getTypeBindings()
        );

        return _erasedMethodDefinition;
    }

    public boolean containsGenericParameters() {
        if (getReturnType().containsGenericParameters()) {
            return true;
        }

        final ParameterList parameters = getParameters();

        for (int i = 0, n = parameters.size(); i < n; i++) {
            final ParameterInfo parameter = parameters.get(i);
            if (parameter.getParameterType().containsGenericParameters()) {
                return true;
            }
        }

        return false;
    }

    public boolean containsGenericParameter(final Type<?> genericParameter) {
        if (!VerifyArgument.notNull(genericParameter, "genericParameter").isGenericParameter()) {
            throw Error.notGenericParameter(genericParameter);
        }

        if (isGenericMethodDefinition()) {
            for (final Type<?> gp : getGenericMethodParameters()) {
                if (gp.containsGenericParameter(genericParameter)) {
                    return true;
                }
            }
        }

        return false;
    }

    public MethodInfo makeGenericMethod(final Type<?>... typeArguments) {
        return makeGenericMethod(Type.list(VerifyArgument.noNullElements(typeArguments, "typeArguments")));
    }

    public MethodInfo makeGenericMethod(final TypeList typeArguments) {
        if (!isGenericMethodDefinition()) {
            throw Error.notGenericMethodDefinition(this);
        }

        final TypeBindings bindings = TypeBindings.create(getGenericMethodParameters(), typeArguments);

        if (!bindings.hasBoundParameters()) {
            throw new IllegalArgumentException("At least one generic parameter must be bound.");
        }

        return new GenericMethod(bindings, this);
    }

    static MethodInfo reflectedOn(final MethodInfo method, final Type<?> reflectedType) {
        if (TypeUtils.areEquivalent(reflectedType, method.getReflectedType())) {
            return method;
        }

        return new DelegatingMethodInfo(method, reflectedType);
    }

    static MethodInfo declaredOn(final MethodInfo method, final Type<?> declaringType, final Type<?> reflectedType) {
        if (TypeUtils.areEquivalent(declaringType, method.getDeclaringType()) &&
            TypeUtils.areEquivalent(reflectedType, method.getReflectedType())) {

            return method;
        }

        return new DelegatingMethodInfo(method, declaringType, reflectedType);
    }
}

class ReflectedMethod extends MethodInfo {
    private final MethodInfo _baseMethod;
    private final Type<?> _declaringType;
    private final Method _rawMethod;
    private final ParameterList _parameters;
    private final SignatureType _signatureType;
    private final TypeBindings _bindings;
    private final TypeList _thrownTypes;
    private final Type<?> _reflectedType;

    ReflectedMethod(
        final MethodInfo baseMethod,
        final Type<?> declaringType,
        final Method rawMethod,
        final ParameterList parameters,
        final Type<?> returnType,
        final TypeList thrownTypes,
        final TypeBindings bindings) {

        this(
            baseMethod,
            declaringType,
            declaringType,
            rawMethod,
            parameters,
            returnType,
            thrownTypes,
            bindings
        );
    }

    ReflectedMethod(
        final MethodInfo baseMethod,
        final Type declaringType,
        final Type reflectedType,
        final Method rawMethod,
        final ParameterList parameters,
        final Type returnType,
        final TypeList thrownTypes,
        final TypeBindings bindings) {

        _baseMethod = baseMethod;

        Type[] genericParameters = null;

        for (int i = 0, n = bindings.size(); i < n; i++) {
            final Type p = bindings.getGenericParameter(i);

            if (p instanceof GenericParameter<?>) {
                final GenericParameter<?> gp = (GenericParameter<?>) p;
                final TypeVariable<?> typeVariable = gp.getRawTypeVariable();

                if (typeVariable.getGenericDeclaration() == rawMethod) {
                    gp.setDeclaringMethod(this);

                    if (genericParameters == null) {
                        genericParameters = new Type[] { gp };
                    }
                    else {
                        genericParameters = ArrayUtilities.append(genericParameters, gp);
                    }

                    if (bindings.hasBoundParameter(gp)) {
                        throw new IllegalArgumentException(
                            "ReflectedMethod cannot be used with bound generic method parameters.  " +
                            "Use GenericMethod instead."
                        );
                    }
                }
            }
        }

        _declaringType = VerifyArgument.notNull(declaringType, "declaringType");
        _reflectedType = VerifyArgument.notNull(reflectedType, "reflectedType");
        _rawMethod = VerifyArgument.notNull(rawMethod, "rawMethod");
        _parameters = VerifyArgument.notNull(parameters, "parameters");

        _signatureType = new SignatureType(
            VerifyArgument.notNull(returnType, "returnType"),
            _parameters.getParameterTypes()
        );

        _thrownTypes = VerifyArgument.notNull(thrownTypes, "thrownTypes");

        if (genericParameters == null) {
            _bindings = TypeBindings.empty();
        }
        else {
            _bindings = TypeBindings.createUnbound(new TypeList(genericParameters));
        }
    }

    @Override
    public Type<?> getReturnType() {
        return _signatureType.getReturnType();
    }

    @Override
    public SignatureType getSignatureType() {
        return _signatureType;
    }

    @Override
    public Method getRawMethod() {
        return _rawMethod;
    }

    @Override
    public Type getDeclaringType() {
        return _declaringType;
    }

    @Override
    public Type getReflectedType() {
        return _reflectedType;
    }

    @Override
    public int getModifiers() {
        return _rawMethod.getModifiers();
    }

    @Override
    public ParameterList getParameters() {
        return _parameters;
    }

    @Override
    public TypeList getThrownTypes() {
        return _thrownTypes;
    }

    @Override
    public CallingConvention getCallingConvention() {
        return _rawMethod.isVarArgs() ? CallingConvention.VarArgs : CallingConvention.Standard;
    }

    @Override
    protected TypeBindings getTypeBindings() {
        return _bindings;
    }

    @Override
    public MethodInfo getErasedMethodDefinition() {
        if (_erasedMethodDefinition != null) {
            return _erasedMethodDefinition;
        }

        if (_baseMethod != null) {
            _erasedMethodDefinition = Fences.orderWrites(
                reflectedOn(
                    _baseMethod.getErasedMethodDefinition(),
                    getReflectedType()
                )
            );
        }

        return super.getErasedMethodDefinition();
    }

    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        return _rawMethod.getAnnotation(annotationClass);
    }

    @NotNull
    @Override
    public Annotation[] getAnnotations() {
        return _rawMethod.getAnnotations();
    }

    @NotNull
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return _rawMethod.getDeclaredAnnotations();
    }

    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
        return _rawMethod.isAnnotationPresent(annotationClass);
    }

    @Override
    public StringBuilder appendErasedDescription(final StringBuilder sb) {
        if (_baseMethod != null) {
            return _baseMethod.appendErasedDescription(sb);
        }
        return super.appendErasedDescription(sb);
    }

    @Override
    public StringBuilder appendErasedSignature(final StringBuilder sb) {
        if (_baseMethod != null) {
            return _baseMethod.appendErasedSignature(sb);
        }
        return super.appendErasedSignature(sb);
    }
}

final class DelegatingMethodInfo extends MethodInfo {
    private final Type<?> _reflectedType;
    private final Type<?> _declaringType;
    private final MethodInfo _methodInfo;

    DelegatingMethodInfo(final MethodInfo method, final Type<?> reflectedType) {
        _methodInfo = unwrap(VerifyArgument.notNull(method, "method"));
        _declaringType = _methodInfo.getDeclaringType();
        _reflectedType = VerifyArgument.notNull(reflectedType, "reflectedType");
    }

    DelegatingMethodInfo(final MethodInfo method, final Type<?> declaringType, final Type<?> reflectedType) {
        _methodInfo = unwrap(VerifyArgument.notNull(method, "method"));
        _declaringType = VerifyArgument.notNull(declaringType, "declaringType");
        _reflectedType = VerifyArgument.notNull(reflectedType, "reflectedType");
    }

    private static MethodInfo unwrap(final MethodInfo method) {
        MethodInfo m = method;

        while (m instanceof DelegatingMethodInfo) {
            m = ((DelegatingMethodInfo) m)._methodInfo;
        }

        return m;
    }

    @Override
    public Type<?> getReturnType() {
        return _methodInfo.getReturnType();
    }

    @Override
    public Method getRawMethod() {
        return _methodInfo.getRawMethod();
    }

    @Override
    public Object getDefaultValue() {
        return _methodInfo.getDefaultValue();
    }

    @Override
    public String getName() {
        return _methodInfo.getName();
    }

    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        return _methodInfo.getAnnotation(annotationClass);
    }

    @NotNull
    @Override
    public Annotation[] getAnnotations() {
        return _methodInfo.getAnnotations();
    }

    @NotNull
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return _methodInfo.getDeclaredAnnotations();
    }

    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
        return _methodInfo.isAnnotationPresent(annotationClass);
    }

    @Override
    public Object invoke(final Object instance, final Object... args) {
        return _methodInfo.invoke(instance, args);
    }

    @Override
    public MethodInfo findOverriddenMethod() {
        return _methodInfo.findOverriddenMethod();
    }

    @Override
    public MethodInfo findBaseMethod(final Type<?> relativeTo) {
        return _methodInfo.findBaseMethod(relativeTo);
    }

    @Override
    public StringBuilder appendDescription(final StringBuilder sb) {
        return _methodInfo.appendDescription(sb);
    }

    @Override
    public StringBuilder appendSimpleDescription(final StringBuilder sb) {
        return _methodInfo.appendSimpleDescription(sb);
    }

    @Override
    public StringBuilder appendBriefDescription(final StringBuilder sb) {
        return _methodInfo.appendBriefDescription(sb);
    }

    @Override
    public StringBuilder appendErasedDescription(final StringBuilder sb) {
        return _methodInfo.appendErasedDescription(sb);
    }

    @Override
    public StringBuilder appendSignature(final StringBuilder sb) {
        return _methodInfo.appendSignature(sb);
    }

    @Override
    public StringBuilder appendErasedSignature(final StringBuilder sb) {
        return _methodInfo.appendErasedSignature(sb);
    }

    @Override
    public boolean isGenericMethod() {
        return _methodInfo.isGenericMethod();
    }

    @Override
    public boolean isGenericMethodDefinition() {
        return _methodInfo.isGenericMethodDefinition();
    }

    @Override
    public TypeBindings getTypeBindings() {
        return _methodInfo.getTypeBindings();
    }

    @Override
    public TypeList getTypeArguments() {
        return _methodInfo.getTypeArguments();
    }

    @Override
    public TypeList getGenericMethodParameters() {
        return _methodInfo.getGenericMethodParameters();
    }

    @Override
    public MethodInfo getGenericMethodDefinition() {
        return _methodInfo.getGenericMethodDefinition();
    }

    @Override
    public MethodInfo getErasedMethodDefinition() {
        return _methodInfo.getErasedMethodDefinition();
    }

    @Override
    public boolean containsGenericParameters() {
        return _methodInfo.containsGenericParameters();
    }

    @Override
    public MethodInfo makeGenericMethod(final Type<?>... typeArguments) {
        return _methodInfo.makeGenericMethod(typeArguments);
    }

    @Override
    public MethodInfo makeGenericMethod(final TypeList typeArguments) {
        return _methodInfo.makeGenericMethod(typeArguments);
    }

    @Override
    public SignatureType getSignatureType() {
        return _methodInfo.getSignatureType();
    }

    @Override
    public ParameterList getParameters() {
        return _methodInfo.getParameters();
    }

    @Override
    public TypeList getThrownTypes() {
        return _methodInfo.getThrownTypes();
    }

    @Override
    public CallingConvention getCallingConvention() {
        return _methodInfo.getCallingConvention();
    }

    @Override
    public Type getDeclaringType() {
        return _declaringType;
    }

    @Override
    public Type getReflectedType() {
        return _reflectedType;
    }

    @Override
    public int getModifiers() {
        return _methodInfo.getModifiers();
    }

    @Override
    public <T extends Annotation> T getDeclaredAnnotation(final Class<T> annotationClass) {
        return _methodInfo.getDeclaredAnnotation(annotationClass);
    }

    @Override
    public <T extends Annotation> T[] getAnnotationsByType(final Class<T> annotationClass) {
        return _methodInfo.getAnnotationsByType(annotationClass);
    }

    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(final Class<T> annotationClass) {
        return _methodInfo.getDeclaredAnnotationsByType(annotationClass);
    }

    @Override
    public boolean isEquivalentTo(final MemberInfo m) {
        return _methodInfo.isEquivalentTo(m);
    }

    @Override
    public String getSignature() {
        return _methodInfo.getSignature();
    }

    @Override
    public String getErasedSignature() {
        return _methodInfo.getErasedSignature();
    }

    @Override
    public String getBriefDescription() {
        return _methodInfo.getBriefDescription();
    }

    @Override
    public String getDescription() {
        return _methodInfo.getDescription();
    }

    @Override
    public String getErasedDescription() {
        return _methodInfo.getErasedDescription();
    }

    @Override
    public String getSimpleDescription() {
        return _methodInfo.getSimpleDescription();
    }

    @Override
    public String toString() {
        return _methodInfo.toString();
    }
}

final class GenericMethod extends MethodInfo {
    private final MethodInfo _genericMethodDefinition;
    private final TypeBindings _typeBindings;
    private final ParameterList _parameters;
    private final SignatureType _signatureType;

    GenericMethod(final TypeBindings typeBindings, final MethodInfo genericMethodDefinition) {
        _typeBindings = VerifyArgument.notNull(typeBindings, "typeBindings");
        _genericMethodDefinition = VerifyArgument.notNull(genericMethodDefinition, "genericMethodDefinition");

        final ParameterList definitionParameters = _genericMethodDefinition.getParameters();

        if (definitionParameters.isEmpty()) {
            _parameters = definitionParameters;
        }
        else {
            ParameterInfo[] parameters = null;

            for (int i = 0, n = definitionParameters.size(); i < n; i++) {
                final ParameterInfo parameter = definitionParameters.get(i);
                final Type parameterType = parameter.getParameterType();

                final Type resolvedParameterType = resolveBindings(parameterType);

                if (resolvedParameterType != parameterType) {
                    if (parameters == null) {
                        parameters = definitionParameters.toArray();
                    }

                    parameters[i] = new ParameterInfo(
                        parameter.getName(),
                        i,
                        resolveBindings(parameterType)
                    );
                }
            }

            if (parameters != null) {
                _parameters = new ParameterList(parameters);
            }
            else {
                _parameters = definitionParameters;
            }
        }

        _signatureType = new SignatureType(
            resolveBindings(genericMethodDefinition.getReturnType()),
            _parameters.getParameterTypes()
        );
    }

    @Override
    protected TypeBindings getTypeBindings() {
        return _typeBindings;
    }

    @Override
    public MethodInfo getGenericMethodDefinition() {
        return _genericMethodDefinition;
    }

    @Override
    public Type<?> getReturnType() {
        return _signatureType.getReturnType();
    }

    @Override
    public SignatureType getSignatureType() {
        return _signatureType;
    }

    @Override
    public Method getRawMethod() {
        return _genericMethodDefinition.getRawMethod();
    }

    private Type resolveBindings(final Type type) {
        return GenericType.GenericBinder.visit(type, _typeBindings);
    }

    @Override
    public String getName() {
        return _genericMethodDefinition.getName();
    }

    @Override
    public StringBuilder appendErasedDescription(final StringBuilder sb) {
        return _genericMethodDefinition.appendErasedDescription(sb);
    }

    @Override
    public StringBuilder appendErasedSignature(final StringBuilder sb) {
        return _genericMethodDefinition.appendErasedSignature(sb);
    }

    @Override
    public Type getDeclaringType() {
        return _genericMethodDefinition.getDeclaringType();
    }

    @Override
    public ParameterList getParameters() {
        return _parameters;
    }

    @Override
    public CallingConvention getCallingConvention() {
        return _genericMethodDefinition.getCallingConvention();
    }

    @Override
    public int getModifiers() {
        return _genericMethodDefinition.getModifiers();
    }
}