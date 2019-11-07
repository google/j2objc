/*
 * TypeReferenceExpression.java
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

import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;

public class TypeReferenceExpression extends Expression {
    public TypeReferenceExpression(final AstType type) {
        this(MYSTERY_OFFSET, type);
    }

    public TypeReferenceExpression(final int offset, final AstType type) {
        super(offset);
        addChild(VerifyArgument.notNull(type, "type"), Roles.TYPE);
    }

    public final AstType getType() {
        return getChildByRole(Roles.TYPE);
    }

    public final void setType(final AstType type) {
        setChildByRole(Roles.TYPE, type);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitTypeReference(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof TypeReferenceExpression &&
               !other.isNull() &&
               getType().matches(((TypeReferenceExpression) other).getType(), match);
    }

    @Override
    public boolean isReference() {
        return true;
    }
}
