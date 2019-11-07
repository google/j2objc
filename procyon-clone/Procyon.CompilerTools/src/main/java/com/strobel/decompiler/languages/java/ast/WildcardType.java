/*
 * WildcardType.java
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

public class WildcardType extends AstType {
    public final static TokenRole WILDCARD_TOKEN_ROLE = new TokenRole("?");
    public final static TokenRole EXTENDS_KEYWORD_ROLE = Roles.EXTENDS_KEYWORD;
    public final static TokenRole SUPER_KEYWORD_ROLE = new TokenRole("super", TokenRole.FLAG_KEYWORD);

    public final JavaTokenNode getWildcardToken() {
        return getChildByRole(WILDCARD_TOKEN_ROLE);
    }

    public final AstNodeCollection<AstType> getExtendsBounds() {
        return getChildrenByRole(Roles.EXTENDS_BOUND);
    }

    public final AstNodeCollection<AstType> getSuperBounds() {
        return getChildrenByRole(Roles.SUPER_BOUND);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitWildcardType(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof WildcardType) {
            final WildcardType otherWildcard = (WildcardType) other;

            return getExtendsBounds().matches(otherWildcard.getExtendsBounds(), match) &&
                   getSuperBounds().matches(otherWildcard.getSuperBounds(), match);
        }

        return false;
    }
}
