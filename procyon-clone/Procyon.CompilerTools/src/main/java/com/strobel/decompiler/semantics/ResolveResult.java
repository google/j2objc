/*
 * ResolveResult.java
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

package com.strobel.decompiler.semantics;

import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.languages.Region;

import java.util.Collections;

/// <summary>
/// Represents the result of resolving an expression.
/// </summary>
public class ResolveResult {
    private final TypeReference _type;

    public ResolveResult(final TypeReference type) {
        _type = VerifyArgument.notNull(type, "type");
    }

    public final TypeReference getType() {
        return _type;
    }

    public boolean isCompileTimeConstant() {
        return false;
    }

    public Object getConstantValue() {
        return null;
    }

    public boolean isError() {
        return false;
    }

    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + " " + _type + "]";
    }

    public final Iterable<ResolveResult> getChildResults() {
        return Collections.emptyList();
    }

    public final Region getDefinitionRegion() {
        return Region.EMPTY;
    }
}