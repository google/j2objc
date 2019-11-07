/*
 * InvocationExpression.java
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

public class InvocationExpression extends Expression {

    public InvocationExpression(final Expression target, final Expression... arguments) {
        this(target.getOffset(), target, arguments);
    }

    public InvocationExpression(final Expression target, final Iterable<Expression> arguments) {
        this(target.getOffset(), target, arguments);
    }

    public InvocationExpression(final int offset, final Expression target, final Iterable<Expression> arguments) {
        super(offset);

        addChild(target, Roles.TARGET_EXPRESSION);

        if (arguments != null) {
            for (final Expression argument : arguments) {
                addChild(argument, Roles.ARGUMENT);
            }
        }
    }

    public InvocationExpression(final int offset, final Expression target, final Expression... arguments) {
        super(offset);

        addChild(target, Roles.TARGET_EXPRESSION);

        if (arguments != null) {
            for (final Expression argument : arguments) {
                addChild(argument, Roles.ARGUMENT);
            }
        }
    }

    public final Expression getTarget() {
        return getChildByRole(Roles.TARGET_EXPRESSION);
    }

    public final void setTarget(final Expression value) {
        setChildByRole(Roles.TARGET_EXPRESSION, value);
    }

    public final AstNodeCollection<Expression> getArguments() {
        return getChildrenByRole(Roles.ARGUMENT);
    }

    public final JavaTokenNode getLeftParenthesisToken() {
        return getChildByRole(Roles.LEFT_PARENTHESIS);
    }

    public final JavaTokenNode getRightParenthesisToken() {
        return getChildByRole(Roles.LEFT_PARENTHESIS);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitInvocationExpression(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof InvocationExpression) {
            final InvocationExpression otherExpression = (InvocationExpression) other;

            return getTarget().matches(otherExpression.getTarget(), match) &&
                   getArguments().matches(otherExpression.getArguments(), match);
        }

        return false;
    }
}
