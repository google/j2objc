/*
 * CaseBlock.java
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

package com.strobel.decompiler.ast;

import com.strobel.assembler.Collection;
import com.strobel.decompiler.ITextOutput;

import java.util.List;

public final class CaseBlock extends Block {
    private final List<Integer> _values = new Collection<>();

    public final List<Integer> getValues() {
        return _values;
    }

    public final boolean isDefault() {
        return _values.isEmpty();
    }

    @Override
    public final void writeTo(final ITextOutput output) {
        if (isDefault()) {
            output.writeKeyword("default");
            output.writeLine(":");
        }
        else {
            for (final Integer value : _values) {
                output.writeKeyword("case");
                output.writeLine(" %d:", value);
            }
        }

        output.indent();
        super.writeTo(output);
        output.unindent();
    }
}
