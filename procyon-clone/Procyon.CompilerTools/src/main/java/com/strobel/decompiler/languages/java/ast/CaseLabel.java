/*
 * CaseLabel.java
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

public class CaseLabel extends AstNode {
    public final static TokenRole CASE_KEYWORD_ROLE = new TokenRole("case", TokenRole.FLAG_KEYWORD);
    public final static TokenRole DEFAULT_KEYWORD_ROLE = new TokenRole("default", TokenRole.FLAG_KEYWORD);

    public CaseLabel() {
    }

    public CaseLabel(final Expression value) {
        setExpression(value);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Role<? extends CaseLabel> getRole() {
        return (Role<? extends CaseLabel>) super.getRole();
    }

    public final JavaTokenNode getColonToken() {
        return getChildByRole(Roles.COLON);
    }

    public final Expression getExpression() {
        return getChildByRole(Roles.EXPRESSION);
    }

    public final void setExpression(final Expression value) {
        setChildByRole(Roles.EXPRESSION, value);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitCaseLabel(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof CaseLabel &&
               !other.isNull() &&
               getExpression().matches(((CaseLabel) other).getExpression(), match);
    }
}
