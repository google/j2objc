/*
 * IMetadataScope.java
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

/**
 * @author Mike Strobel
 */
public interface IMetadataScope {
    public abstract TypeReference lookupType(final int token);
    public abstract FieldReference lookupField(final int token);
    public abstract MethodReference lookupMethod(final int token);
    public abstract MethodHandle lookupMethodHandle(final int token);
    public abstract IMethodSignature lookupMethodType(final int token);
    public abstract DynamicCallSite lookupDynamicCallSite(final int token);
    public abstract FieldReference lookupField(final int typeToken, final int nameAndTypeToken);
    public abstract MethodReference lookupMethod(final int typeToken, final int nameAndTypeToken);
    public abstract <T> T lookupConstant(final int token);
    public abstract Object lookup(final int token);
}
