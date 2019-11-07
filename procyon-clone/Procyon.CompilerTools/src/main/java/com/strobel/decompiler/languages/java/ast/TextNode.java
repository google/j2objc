/*
 * TextNode.java
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

import com.strobel.core.StringUtilities;
import com.strobel.decompiler.languages.TextLocation;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.Role;

public class TextNode extends AstNode {
    private String _text;
    private TextLocation _startLocation;
    private TextLocation _endLocation;

    public TextNode() {
    }

    public TextNode(final String text) {
        this(text, TextLocation.EMPTY, TextLocation.EMPTY);
    }

    public TextNode(final String text, final TextLocation startLocation, final TextLocation endLocation) {
        _text = text;
        _startLocation = startLocation;
        _endLocation = endLocation;
    }

    public final String getText() {
        return _text;
    }

    public final void setText(final String text) {
        verifyNotFrozen();
        _text = text;
    }

    public final TextLocation getStartLocation() {
        return _startLocation;
    }

    public final void setStartLocation(final TextLocation startLocation) {
        verifyNotFrozen();
        _startLocation = startLocation;
    }

    public final TextLocation getEndLocation() {
        return _endLocation;
    }

    public final void setEndLocation(final TextLocation endLocation) {
        verifyNotFrozen();
        _endLocation = endLocation;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Role<? extends TextNode> getRole() {
        return (Role<? extends TextNode>) super.getRole();
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitText(this, data);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.WHITESPACE;
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof TextNode &&
               StringUtilities.equals(getText(), ((TextNode) other).getText());
    }
}
