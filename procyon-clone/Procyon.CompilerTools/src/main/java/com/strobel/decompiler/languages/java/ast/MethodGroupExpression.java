/*
 * MethodGroupExpression.java
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

public class MethodGroupExpression extends Expression {
    public final static Role<Expression> CLOSURE_ARGUMENT_RULE = new Role<>("ClosureArgument", Expression.class, Expression.NULL);
    public final static TokenRole DOUBLE_COLON_ROLE = new TokenRole("::", TokenRole.FLAG_OPERATOR);

    public MethodGroupExpression( int offset, final Expression target, final String methodName) {
        super( offset);
        setTarget(target);
        setMethodName(methodName);
    }

    
    public final AstNodeCollection<Expression> getClosureArguments() {
        return getChildrenByRole(CLOSURE_ARGUMENT_RULE);
    }

    public final JavaTokenNode getDoubleColonToken() {
        return getChildByRole(DOUBLE_COLON_ROLE);
    }

    public final String getMethodName() {
        return getChildByRole(Roles.IDENTIFIER).getName();
    }

    public final void setMethodName(final String name) {
        setChildByRole(Roles.IDENTIFIER, Identifier.create(name));
    }

    public final Identifier getMethodNameToken() {
        return getChildByRole(Roles.IDENTIFIER);
    }

    public final void setMethodNameToken(final Identifier token) {
        setChildByRole(Roles.IDENTIFIER, token);
    }

    public final Expression getTarget() {
        return getChildByRole(Roles.TARGET_EXPRESSION);
    }

    public final void setTarget(final Expression value) {
        setChildByRole(Roles.TARGET_EXPRESSION, value);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitMethodGroupExpression(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        return false;
    }

    @Override
    public boolean isReference() {
        return true;
    }
}
