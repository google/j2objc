/*
 * AssertStatement.java
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

public class AssertStatement extends Statement {
    public final static TokenRole ASSERT_KEYWORD_ROLE = new TokenRole("assert", TokenRole.FLAG_KEYWORD);

    public AssertStatement( int offset) {
        super( offset);
    }
    
    public final JavaTokenNode getColon() {
        return getChildByRole(Roles.COLON);
    }

    public final Expression getCondition() {
        return getChildByRole(Roles.CONDITION);
    }

    public final void setCondition(final Expression value) {
        setChildByRole(Roles.CONDITION, value);
    }

    public final Expression getMessage() {
        return getChildByRole(Roles.EXPRESSION);
    }

    public final void setMessage(final Expression message) {
        setChildByRole(Roles.EXPRESSION, message);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitAssertStatement(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof AssertStatement) {
            final AssertStatement otherAssert = (AssertStatement) other;

            return getCondition().matches(otherAssert.getCondition(), match) &&
                   getMessage().matches(otherAssert.getMessage());
        }

        return false;
    }
}
