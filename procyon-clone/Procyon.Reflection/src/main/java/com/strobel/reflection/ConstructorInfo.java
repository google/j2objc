/*
 * ConstructorInfo.java
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Mike Strobel
 */
public abstract class ConstructorInfo extends MethodBase {
    @Override
    public final MemberType getMemberType() {
        return MemberType.Constructor;
    }

    @Override
    public final String getName() {
        return "<init>";
    }

    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        return getRawConstructor().getAnnotation(annotationClass);
    }

    @NotNull
    @Override
    public Annotation[] getAnnotations() {
        return getRawConstructor().getAnnotations();
    }

    @NotNull
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return getRawConstructor().getDeclaredAnnotations();
    }

    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
        return getRawConstructor().isAnnotationPresent(annotationClass);
    }

    public abstract Constructor<?> getRawConstructor();

    @Override
    public StringBuilder appendErasedSignature(final StringBuilder sb) {
        final Constructor<?> rawConstructor = getRawConstructor();
        final TypeList parameterTypes = Type.list(rawConstructor.getParameterTypes());

        StringBuilder s = sb;
        s.append('(');

        for (int i = 0, n = parameterTypes.size(); i < n; ++i) {
            s = parameterTypes.get(i).appendErasedSignature(s);
        }

        s.append(')');
        s = PrimitiveTypes.Void.appendErasedSignature(s);

        return s;

    }

    @Override
    public StringBuilder appendSignature(final StringBuilder sb) {
        final ParameterList parameters = getParameters();

        StringBuilder s = sb;
        s.append('(');

        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterInfo p = parameters.get(i);
            s = p.getParameterType().appendSignature(s);
        }

        s.append(')');
        s = PrimitiveTypes.Void.appendErasedSignature(s);

        return s;
    }

    @Override
    public StringBuilder appendErasedDescription(final StringBuilder sb) {
        final Constructor<?> rawConstructor = getRawConstructor();
        final TypeList parameterTypes = Type.list(rawConstructor.getParameterTypes());

        StringBuilder s = PrimitiveTypes.Void.appendBriefDescription(sb);

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

    public Object invoke(final Object... args) {
        final Constructor<?> rawConstructor = getRawConstructor();

        if (rawConstructor == null) {
            throw Error.rawMethodBindingFailure(this);
        }

        try {
            return rawConstructor.newInstance(args);
        }
        catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw Error.targetInvocationException(e);
        }
    }

    @Override
    public StringBuilder appendDescription(final StringBuilder sb) {
        StringBuilder s = PrimitiveTypes.Void.appendBriefDescription(sb);

        s.append(' ');
        s.append(getName());
        s.append('(');

        final ParameterList parameters = getParameters();

        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterInfo p = parameters.get(i);
            if (i != 0) {
                s.append(", ");
            }
            s = p.getParameterType().appendBriefDescription(s);
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
        StringBuilder s = PrimitiveTypes.Void.appendSimpleDescription(sb);

        s.append(' ');
        s.append(getName());
        s.append('(');

        final ParameterList parameters = getParameters();

        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterInfo p = parameters.get(i);
            if (i != 0) {
                s.append(", ");
            }
            s = p.getParameterType().appendSimpleDescription(s);
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
        StringBuilder s = PrimitiveTypes.Void.appendBriefDescription(sb);

        s.append(' ');
        s.append(getName());
        s.append('(');

        final ParameterList parameters = getParameters();

        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterInfo p = parameters.get(i);
            if (i != 0) {
                s.append(", ");
            }
            final Type parameterType = p.getParameterType();
            if (parameterType.isGenericParameter()) {
                s.append(parameterType.getName());
            }
            else {
                s = parameterType.appendSimpleDescription(s);
            }

        }

        s.append(')');

        return s;
    }

    @Override
    public boolean containsGenericParameter(final Type<?> genericParameter) {
        return false;
    }
}

class ReflectedConstructor extends ConstructorInfo {
    private final Type _declaringType;
    private final ParameterList _parameters;
    private final Constructor _rawConstructor;
    private final TypeList _thrownTypes;
    private final SignatureType _signatureType;

    ReflectedConstructor(final Type declaringType, final Constructor rawConstructor, final ParameterList parameters, final TypeList thrownTypes) {
        _declaringType = VerifyArgument.notNull(declaringType, "declaringType");
        _rawConstructor = VerifyArgument.notNull(rawConstructor, "rawConstructor");
        _parameters = VerifyArgument.notNull(parameters, "parameters");
        _thrownTypes = VerifyArgument.notNull(thrownTypes, "thrownTypes");
        _signatureType = new SignatureType(PrimitiveTypes.Void, _parameters.getParameterTypes());
    }

    @Override
    public SignatureType getSignatureType() {
        return _signatureType;
    }

    @Override
    public ParameterList getParameters() {
        return _parameters;
    }

    @Override
    public TypeList getThrownTypes() {
        return _thrownTypes;
    }

/*
    @Override
    public String getName() {
        return _rawConstructor.getName();
    }
*/

    @Override
    public Type getDeclaringType() {
        return _declaringType;
    }

    @Override
    public Constructor<?> getRawConstructor() {
        return _rawConstructor;
    }

    @Override
    public int getModifiers() {
        return _rawConstructor.getModifiers();
    }
}
