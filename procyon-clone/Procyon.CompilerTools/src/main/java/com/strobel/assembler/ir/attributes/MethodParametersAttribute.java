/*
 * MethodParametersAttribute.java
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

import com.strobel.core.VerifyArgument;

import java.util.List;

public final class MethodParametersAttribute extends SourceAttribute {
    private final List<MethodParameterEntry> _entries;

    public MethodParametersAttribute(final List<MethodParameterEntry> entries) {
        super(AttributeNames.MethodParameters, 1 + entries.size() * 4);
        _entries = VerifyArgument.notNull(entries, "entries");
    }

    public List<MethodParameterEntry> getEntries() {
        return _entries;
    }

}
