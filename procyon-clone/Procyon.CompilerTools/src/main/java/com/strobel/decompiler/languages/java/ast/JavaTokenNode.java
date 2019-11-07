/*
 * JavaTokenNode.java
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

import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.languages.TextLocation;
import com.strobel.decompiler.languages.java.JavaFormattingOptions;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.Role;

public class JavaTokenNode extends AstNode {
    private TextLocation _startLocation;

    public JavaTokenNode(final TextLocation startLocation) {
        _startLocation = VerifyArgument.notNull(startLocation, "startLocation");
    }

    public TextLocation getStartLocation() {
        return _startLocation;
    }

    public void setStartLocation(final TextLocation startLocation) {
        _startLocation = startLocation;
    }

    public TextLocation getEndLocation() {
        return new TextLocation(_startLocation.line(), _startLocation.column() + getTokenLength());
    }

    @Override
    public String getText(final JavaFormattingOptions options) {
        final Role role = getRole();

        if (role instanceof TokenRole) {
            return ((TokenRole) role).getToken();
        }

        return null;
    }

    protected int getTokenLength() {
        final Role<?> role = getRole();

        if (role instanceof TokenRole) {
            return ((TokenRole) role).getLength();
        }

        return 0;
    }

    // <editor-fold defaultstate="collapsed" desc="Null JavaTokenNode">

    public final static JavaTokenNode NULL = new NullJavaTokenNode();

    private static final class NullJavaTokenNode extends JavaTokenNode {
        public NullJavaTokenNode() {
            super(TextLocation.EMPTY);
        }

        @Override
        public final boolean isNull() {
            return true;
        }

        @Override
        public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
            return null;
        }

        @Override
        public boolean matches(final INode other, final Match match) {
            return other == null || other.isNull();
        }
    }

    // </editor-fold>

    @Override
    @SuppressWarnings("unchecked")
    public Role<? extends JavaTokenNode> getRole() {
        return (Role<? extends JavaTokenNode>) super.getRole();
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitJavaTokenNode(this, data);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.TOKEN;
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof JavaTokenNode &&
               !other.isNull() &&
               !(other instanceof JavaModifierToken);
    }

    @Override
    public String toString() {
        return String.format(
            "[JavaTokenNode: StartLocation=%s, EndLocation=%s, Role=%s]",
            getStartLocation(),
            getEndLocation(),
            getRole()
        );
    }
}
