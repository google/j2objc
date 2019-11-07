/*
 * DynamicMethod.java
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

import com.strobel.core.VerifyArgument;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * @author Mike Strobel
 */
public final class DynamicMethod extends MethodInfo {
    private static final Method INVOKE_EXACT;
    private static final Method INVOKE;

    static {
        try {
            INVOKE = MethodHandle.class.getMethod("invoke", Object[].class);
            INVOKE_EXACT = MethodHandle.class.getMethod("invokeExact", Object[].class);
        }
        catch (NoSuchMethodException e) {
            throw Error.targetInvocationException(e);
        }
    }

    public static DynamicMethod invoke(final MethodHandle methodHandle) {
        return new DynamicMethod(methodHandle, INVOKE);
    }

    public static DynamicMethod invokeExact(final MethodHandle methodHandle) {
        return new DynamicMethod(methodHandle, INVOKE_EXACT);
    }

    public static DynamicMethod invoke(final MethodType methodType) {
        return new DynamicMethod(methodType, INVOKE);
    }

    public static DynamicMethod invokeExact(final MethodType methodType) {
        return new DynamicMethod(methodType, INVOKE_EXACT);
    }

    private final ParameterList _parameters;
    private final Method _invokeMethod;
    private final MethodHandle _methodHandle;
    private final SignatureType _signatureType;

    private DynamicMethod(final MethodHandle methodHandle, final Method invokeMethod) {
        _methodHandle = VerifyArgument.notNull(methodHandle, "methodHandle");
        _invokeMethod = VerifyArgument.notNull(invokeMethod, "invokeMethod");
        
        final MethodType methodType = methodHandle.type();

        final ParameterInfo[] parameters = new ParameterInfo[methodType.parameterCount()];

        for (int i = 0, n = parameters.length; i < n; i++) {
            parameters[i] = new ParameterInfo(
                "p" + i,
                i,
                Type.of(methodType.parameterType(i))
            );
        }

        _parameters = new ParameterList(parameters);

        _signatureType = new SignatureType(
            Type.of(methodType.returnType()),
            _parameters.getParameterTypes()
        );
    }

    private DynamicMethod(final MethodType methodType, final Method invokeMethod) {
        VerifyArgument.notNull(methodType, "methodType");

        _invokeMethod = VerifyArgument.notNull(invokeMethod, "invokeMethod");
        _methodHandle = null;

        final ParameterInfo[] parameters = new ParameterInfo[methodType.parameterCount()];

        for (int i = 0, n = parameters.length; i < n; i++) {
            parameters[i] = new ParameterInfo(
                "p" + i,
                i,
                Type.of(methodType.parameterType(i))
            );
        }

        _parameters = new ParameterList(parameters);

        _signatureType = new SignatureType(
            Type.of(methodType.returnType()),
            _parameters.getParameterTypes()
        );
    }

    public MethodHandle getHandle() {
        return _methodHandle;
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
        return _invokeMethod;
    }

    @Override
    public Type getDeclaringType() {
        return Types.MethodHandle;
    }

    @Override
    public int getModifiers() {
        return _invokeMethod.getModifiers();
    }

    @Override
    public ParameterList getParameters() {
        return _parameters;
    }
}
