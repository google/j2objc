/*
 * WindowsNewLine.java
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

package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.TextLocation;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;

public final class WindowsNewLine extends NewLineNode {
    @Override
    public NewLineType getNewLineType() {
        return NewLineType.WINDOWS;
    }

    public WindowsNewLine() {
        super();
    }

    public WindowsNewLine(final TextLocation startLocation) {
        super(startLocation);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof UnixNewLine;
    }
}
