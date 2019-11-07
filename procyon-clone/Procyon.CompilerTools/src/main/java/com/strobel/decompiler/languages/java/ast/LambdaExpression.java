/*
 * LambdaExpression.java
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

public class LambdaExpression extends Expression {
    public final static TokenRole ARROW_ROLE = new TokenRole ("->", TokenRole.FLAG_OPERATOR);
    public final static Role<AstNode> BODY_ROLE = new Role<>("Body", AstNode.class, AstNode.NULL);

    public LambdaExpression( final int offset) {
        super( offset);
    }
    
    public final AstNodeCollection<ParameterDeclaration> getParameters() {
        return getChildrenByRole(Roles.PARAMETER);
    }

    public final JavaTokenNode getArrowToken() {
        return getChildByRole(ARROW_ROLE);
    }

    public final AstNode getBody() {
        return getChildByRole(BODY_ROLE);
    }

    public final void setBody(final AstNode value) {
        setChildByRole(BODY_ROLE, value);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitLambdaExpression(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof LambdaExpression) {
            final LambdaExpression otherLambda = (LambdaExpression) other;

            return getParameters().matches(otherLambda.getParameters(), match) &&
                   getBody().matches(otherLambda.getBody(), match);
        }

        return false;
    }
}
