/*
 * BreakStatement.java
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
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;

public class BreakStatement extends Statement {
    public final static TokenRole BREAK_KEYWORD_ROLE = new TokenRole("break", TokenRole.FLAG_KEYWORD);

    public BreakStatement( int offset) {
        super( offset);
    }

    public BreakStatement( int offset, final String label) {
        super( offset);
        setLabel(label);
    }

    public final JavaTokenNode getBreakToken() {
        return getChildByRole(BREAK_KEYWORD_ROLE);
    }

    public final JavaTokenNode getSemicolonToken() {
        return getChildByRole(Roles.SEMICOLON);
    }

    public final String getLabel() {
        return getChildByRole(Roles.IDENTIFIER).getName();
    }

    public final void setLabel(final String value) {
        if (StringUtilities.isNullOrEmpty(value)) {
            setChildByRole(Roles.IDENTIFIER, Identifier.create(null));
        }
        else {
            setChildByRole(Roles.IDENTIFIER, Identifier.create(value));
        }
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitBreakStatement(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof BreakStatement &&
               matchString(getLabel(), ((BreakStatement) other).getLabel());
    }
}
