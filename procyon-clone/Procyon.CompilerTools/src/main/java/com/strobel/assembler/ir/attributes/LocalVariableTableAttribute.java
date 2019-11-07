/*
 * LocalVariableTableAttribute.java
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

import com.strobel.core.ArrayUtilities;

import java.util.List;

/**
 * @author Mike Strobel
 */
public final class LocalVariableTableAttribute extends SourceAttribute {
    private final List<LocalVariableTableEntry> _entries;

    public LocalVariableTableAttribute(final String name, final LocalVariableTableEntry[] entries) {
        super(name, 2 + (entries.length * 10));
        _entries = ArrayUtilities.asUnmodifiableList(entries.clone());
    }

    public List<LocalVariableTableEntry> getEntries() {
        return _entries;
    }
}
