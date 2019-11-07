/*
 * LabeledStatement.java
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

public class LabeledStatement extends Statement {
    public LabeledStatement(final int offset) {
        super(offset);
    }

    public LabeledStatement(final int offset, final String name) {
        super(offset);
        setLabel(name);
    }

    public LabeledStatement(final String name, final Statement statement) {
        this( statement.getOffset());
        setLabel(name);
        setStatement(statement);
    }

    public final String getLabel() {
        return getChildByRole(Roles.LABEL).getName();
    }

    public final void setLabel(final String value) {
        setChildByRole(Roles.LABEL, Identifier.create(value));
    }

    public final Identifier getLabelToken() {
        return getChildByRole(Roles.LABEL);
    }

    public final void setLabelToken(final Identifier value) {
        setChildByRole(Roles.LABEL, value);
    }

    public final JavaTokenNode getColonToken() {
        return getChildByRole(Roles.COLON);
    }

    public final Statement getStatement() {
        return getChildByRole(Roles.EMBEDDED_STATEMENT);
    }

    public final void setStatement(final Statement value) {
        setChildByRole(Roles.EMBEDDED_STATEMENT, value);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitLabeledStatement(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof LabeledStatement) {
            final LabeledStatement otherStatement = (LabeledStatement) other;

            return matchString(getLabel(), otherStatement.getLabel()) &&
                   getStatement().matches(otherStatement.getStatement(), match);
        }

        return false
    ;}
}
