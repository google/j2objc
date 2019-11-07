/*
 * Annotation.java
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

public class Annotation extends Expression {
    private boolean _hasArgumentList;

    public Annotation() {
        super( Expression.MYSTERY_OFFSET); // Probably unimportant to show a line # for this.
    }
    
    public final AstType getType() {
        return getChildByRole(Roles.TYPE);
    }

    public final void setType(final AstType type) {
        setChildByRole(Roles.TYPE, type);
    }

    public final boolean hasArgumentList() {
        return _hasArgumentList;
    }

    public final void setHasArgumentList(final boolean hasArgumentList) {
        verifyNotFrozen();
        _hasArgumentList = hasArgumentList;
    }

    public final AstNodeCollection<Expression> getArguments() {
        return getChildrenByRole(Roles.ARGUMENT);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitAnnotation(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof Annotation) {
            final Annotation otherAnnotation = (Annotation) other;

            return !otherAnnotation.isNull() &&
                   getType().matches(otherAnnotation.getType(), match) &&
                   getArguments().matches(otherAnnotation.getArguments(), match);
        }

        return false;
    }

    @Override
    public String toString() {
        return isNull() ? "Null" : getText();
    }
}
