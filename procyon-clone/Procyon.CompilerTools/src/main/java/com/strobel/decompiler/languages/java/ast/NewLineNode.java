/*
 * NewLineNode.java
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

import com.strobel.core.Environment;
import com.strobel.decompiler.languages.TextLocation;
import com.strobel.decompiler.patterns.Role;

public abstract class NewLineNode extends AstNode {
    private final TextLocation _startLocation;
    private final TextLocation _endLocation;

    protected NewLineNode() {
        this(TextLocation.EMPTY);
    }

    protected NewLineNode(final TextLocation startLocation) {
        _startLocation = startLocation != null ? startLocation : TextLocation.EMPTY;
        _endLocation = new TextLocation(_startLocation.line() + 1, 1);
    }

    public abstract NewLineType getNewLineType();

    @Override
    public TextLocation getStartLocation() {
        return _startLocation;
    }

    @Override
    public TextLocation getEndLocation() {
        return _endLocation;
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitNewLine(this, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Role<? extends NewLineNode> getRole() {
        return (Role<? extends NewLineNode>) super.getRole();
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.WHITESPACE;
    }

    public static NewLineNode create() {
        if (Environment.isWindows() || Environment.isOS2()) {
            return new WindowsNewLine();
        }
        if (Environment.isMac()) {
            return new MacNewLine();
        }
        return new UnixNewLine();
    }
}
