/*
 * IndexerExpression.java
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

public class IndexerExpression extends Expression {
    public IndexerExpression( final int offset, final Expression target, final Expression argument) {
        super( offset);
        setTarget(target);
        setArgument(argument);
    }

    public final Expression getTarget() {
        return getChildByRole(Roles.TARGET_EXPRESSION);
    }

    public final void setTarget(final Expression value) {
        setChildByRole(Roles.TARGET_EXPRESSION, value);
    }

    public final Expression getArgument() {
        return getChildByRole(Roles.ARGUMENT);
    }

    public final void setArgument(final Expression value) {
        setChildByRole(Roles.ARGUMENT, value);
    }

    public final JavaTokenNode getLeftBracketToken() {
        return getChildByRole(Roles.LEFT_BRACKET);
    }

    public final JavaTokenNode getRightBracketToken() {
        return getChildByRole(Roles.RIGHT_BRACKET);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitIndexerExpression(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof IndexerExpression) {
            final IndexerExpression otherIndexer = (IndexerExpression) other;

            return !otherIndexer.isNull() &&
                   getTarget().matches(otherIndexer.getTarget(), match) &&
                   getArgument().matches(otherIndexer.getArgument(), match);
        }

        return false;
    }
}
