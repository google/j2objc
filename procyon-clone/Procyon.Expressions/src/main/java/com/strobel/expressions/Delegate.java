/*
 * Delegate.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is based on the Dynamic Language Runtime from Microsoft,
 *   Copyright (c) Microsoft Corporation.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.expressions;

import com.strobel.core.VerifyArgument;
import com.strobel.reflection.MethodInfo;
import com.strobel.reflection.TargetInvocationException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * @author Mike Strobel
 */
public final class Delegate<T> {
    private final T _instance;
    private final MethodInfo _method;
    private MethodHandle _methodHandle;
    private MethodHandle _spreadInvoker;

    Delegate(final T instance, final MethodInfo method) {
        _instance = VerifyArgument.notNull(instance, "instance");
        _method = VerifyArgument.notNull(method, "method");
    }

    public final T getInstance() {
        return _instance;
    }

    public final MethodInfo getMethod() {
        return _method;
    }
    
    public final MethodHandle getMethodHandle() {
        if (_methodHandle == null) {
            try {
                _methodHandle = MethodHandles
                    .lookup()
                    .unreflect(_method.getRawMethod())
                    .bindTo(_instance);
            }
            catch (IllegalAccessException e) {
                throw new IllegalStateException("Could not resolve method handle.");
            }
        }
        return _methodHandle;
    }

    public final Object invokeDynamic(final Object... args) throws TargetInvocationException {
        try {
            if (_spreadInvoker == null) {
                final MethodHandle methodHandle = getMethodHandle();
                _spreadInvoker = methodHandle.asSpreader(Object[].class, _method.getParameters().size());
            }
            return _spreadInvoker.invoke(args);
        }
        catch (Throwable t) {
            throw new TargetInvocationException(t);
        }
    }
}
