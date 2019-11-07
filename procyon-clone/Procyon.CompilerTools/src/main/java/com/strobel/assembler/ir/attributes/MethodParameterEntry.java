/*
 * MethodParameterEntry.java
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

package com.strobel.assembler.ir.attributes;

import com.strobel.assembler.metadata.Flags;

public final class MethodParameterEntry {
    private final String _name;
    private final int _flags;

    public MethodParameterEntry(final String name, final int flags) {
        _name = name;
        _flags = flags;
    }

    public String getName() {
        return _name;
    }

    public int getFlags() {
        return _flags;
    }

    @Override
    public String toString() {
        return "MethodParameterEntry{" +
               "name='" + _name + "'" +
               ", flags=" + Flags.toString(_flags) +
               '}';
    }
}
