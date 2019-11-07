/*
 * LabelStatement.java
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

public class LabelStatement extends Statement {
    public LabelStatement(final int offset, final String name) {
        super(offset);
        setLabel(name);
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

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitLabelStatement(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof LabelStatement &&
               matchString(getLabel(), ((LabelStatement) other).getLabel());
    }
}
