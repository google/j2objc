/*
 * Label.java
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

import com.strobel.decompiler.ITextOutput;

public class Label extends Node {
    private String _name;
    private int _offset = -1;

    public Label() {
    }

    public Label(final String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }

    public int getOffset() {
        return _offset;
    }

    public void setOffset(final int offset) {
        _offset = offset;
    }

    @Override
    public void writeTo(final ITextOutput output) {
        output.writeDefinition(getName() + ":", this);
    }
}
