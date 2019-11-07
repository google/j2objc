/*
 * ArrayCreationExpression.java
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

public class ArrayCreationExpression extends Expression {
    public final static TokenRole NEW_KEYWORD_ROLE = new TokenRole("new", TokenRole.FLAG_KEYWORD);

    public final static Role<ArraySpecifier> ADDITIONAL_ARRAY_SPECIFIER_ROLE = new Role<>(
        "AdditionalArraySpecifier",
        ArraySpecifier.class
    );

    public final static Role<ArrayInitializerExpression> INITIALIZER_ROLE = new Role<>(
        "Initializer",
        ArrayInitializerExpression.class,
        ArrayInitializerExpression.NULL
    );
    
    public ArrayCreationExpression( int offset) {
        super( offset);
    }

    public final AstNodeCollection<Expression> getDimensions() {
        return getChildrenByRole(Roles.ARGUMENT);
    }

    public final ArrayInitializerExpression getInitializer() {
        return getChildByRole(INITIALIZER_ROLE);
    }
    
    public final void setInitializer(final ArrayInitializerExpression value) {
        setChildByRole(INITIALIZER_ROLE, value);
    }

    public final AstNodeCollection<ArraySpecifier> getAdditionalArraySpecifiers() {
        return getChildrenByRole(ADDITIONAL_ARRAY_SPECIFIER_ROLE);
    }

    public final AstType getType() {
        return getChildByRole(Roles.TYPE);
    }

    public final void setType(final AstType type) {
        setChildByRole(Roles.TYPE, type);
    }

    public final JavaTokenNode getNewToken() {
        return getChildByRole(NEW_KEYWORD_ROLE);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitArrayCreationExpression(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof ArrayCreationExpression) {
            final ArrayCreationExpression otherExpression = (ArrayCreationExpression) other;

            return !otherExpression.isNull() &&
                   getType().matches(otherExpression.getType(), match) &&
                   getDimensions().matches(otherExpression.getDimensions(), match) &&
                   getInitializer().matches(otherExpression.getInitializer(), match) &&
                   getAdditionalArraySpecifiers().matches(otherExpression.getAdditionalArraySpecifiers(), match);
        }

        return false;
    }
}
