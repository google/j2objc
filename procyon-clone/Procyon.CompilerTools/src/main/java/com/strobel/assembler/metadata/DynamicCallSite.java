/*
 * DynamicCallSite.java
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

import com.strobel.core.VerifyArgument;

import java.util.List;

public final class DynamicCallSite {
    private final int _bootstrapMethodIndex;
    private final MethodHandle _bootstrapMethodHandle;
    private final List<Object> _bootstrapArguments;
    private final String _methodName;
    private final IMethodSignature _methodType;

    public DynamicCallSite(
        final int bootstrapMethodIndex,
        final MethodHandle method,
        final List<Object> bootstrapArguments,
        final String methodName,
        final IMethodSignature methodType) {

        _bootstrapMethodIndex = bootstrapMethodIndex;
        _bootstrapMethodHandle = VerifyArgument.notNull(method, "method");
        _bootstrapArguments = VerifyArgument.notNull(bootstrapArguments, "bootstrapArguments");
        _methodName = VerifyArgument.notNull(methodName, "methodName");
        _methodType = VerifyArgument.notNull(methodType, "methodType");
    }

    public final int getBootstrapMethodIndex() {
        return _bootstrapMethodIndex;
    }

    public final String getMethodName() {
        return _methodName;
    }

    public final IMethodSignature getMethodType() {
        return _methodType;
    }

    public final List<Object> getBootstrapArguments() {
        return _bootstrapArguments;
    }

    public final MethodHandle getBootstrapMethodHandle() {
        return _bootstrapMethodHandle;
    }

    public final MethodReference getBootstrapMethod() {
        return _bootstrapMethodHandle.getMethod();
    }
}
