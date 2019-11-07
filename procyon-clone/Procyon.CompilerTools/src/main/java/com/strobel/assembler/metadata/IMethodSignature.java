/*
 * IMethodSignature.java
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

import java.util.List;

public interface IMethodSignature extends IGenericParameterProvider, IGenericContext {
    boolean hasParameters();
    List<ParameterDefinition> getParameters();
    TypeReference getReturnType();
    List<TypeReference> getThrownTypes();

    String getSignature();
    String getErasedSignature();

    /**
     * Invalidate any signature information.  This is not guaranteed to be thread-safe,
     * should only be called when a partially constructed method definition changes, e.g.,
     * by changing the return type or parameter types.
     */
    void invalidateSignature();
}
