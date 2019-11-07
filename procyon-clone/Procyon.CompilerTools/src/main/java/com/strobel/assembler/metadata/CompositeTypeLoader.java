/*
 * CompositeTypeLoader.java
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

public final class CompositeTypeLoader implements ITypeLoader {
    private final ITypeLoader[] _typeLoaders;

    public CompositeTypeLoader(final ITypeLoader... typeLoaders) {
        _typeLoaders = VerifyArgument.noNullElementsAndNotEmpty(typeLoaders, "typeLoaders").clone();
    }

    @Override
    public boolean tryLoadType(final String internalName, final Buffer buffer) {
        for (final ITypeLoader typeLoader : _typeLoaders) {
            if (typeLoader.tryLoadType(internalName, buffer)) {
                return true;
            }

            buffer.reset();
        }

        return false;
    }
}
