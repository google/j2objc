/*
 * ITypeInfo.java
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

package com.strobel.decompiler.types;

import com.strobel.collections.ImmutableList;

interface ITypeInfo {
    String getName();
    String getPackageName();
    String getFullName();
    String getCanonicalName();
    String getInternalName();
    String getSignature();

    boolean isArray();
    boolean isPrimitive();
    boolean isPrimitiveOrVoid();
    boolean isVoid();
    boolean isRawType();
    boolean isGenericType();
    boolean isGenericTypeInstance();
    boolean isGenericTypeDefinition();
    boolean isGenericParameter();
    boolean isWildcard();
    boolean isUnknownType();
    boolean isBound();
    boolean isAnonymous();
    boolean isLocal();

    boolean hasConstraints();
    boolean hasSuperConstraint();
    boolean hasExtendsConstraint();

    ITypeInfo getDeclaringType();

    ITypeInfo getElementType();
    ITypeInfo getSuperConstraint();
    ITypeInfo getExtendsConstraint();
    ITypeInfo getSuperClass();

    ImmutableList<ITypeInfo> getSuperInterfaces();
    ImmutableList<ITypeInfo> getGenericParameters();
    ImmutableList<ITypeInfo> getTypeArguments();

    ITypeInfo getGenericDefinition();

    void addListener(ITypeListener listener);
    void removeListener(ITypeListener listener);
}
