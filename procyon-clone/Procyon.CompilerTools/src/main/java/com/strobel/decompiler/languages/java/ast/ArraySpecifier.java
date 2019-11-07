/*
 * ArraySpecifier.java
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

import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.Role;

public class ArraySpecifier extends AstNode {
    public final JavaTokenNode getLeftBracketToken() {
        return getChildByRole(Roles.LEFT_BRACKET);
    }

    public final JavaTokenNode getRightBracketToken() {
        return getChildByRole(Roles.RIGHT_BRACKET);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Role<? extends ArraySpecifier> getRole() {
        return (Role<? extends ArraySpecifier>) super.getRole();
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitArraySpecifier(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof ArraySpecifier;
    }

    @Override
    public String toString() {
        return "[]";
    }
}
