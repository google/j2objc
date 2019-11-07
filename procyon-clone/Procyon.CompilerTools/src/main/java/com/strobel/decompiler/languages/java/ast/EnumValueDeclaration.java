/*
 * EnumValueDeclaration.java
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

import com.strobel.decompiler.languages.EntityType;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;

public class EnumValueDeclaration extends EntityDeclaration {
    public final AstNodeCollection<Expression> getArguments() {
        return getChildrenByRole(Roles.ARGUMENT);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ENUM_VALUE;
    }

    public final JavaTokenNode getLeftBraceToken() {
        return getChildByRole(Roles.LEFT_BRACE);
    }

    public final AstNodeCollection<EntityDeclaration> getMembers() {
        return getChildrenByRole(Roles.TYPE_MEMBER);
    }

    public final JavaTokenNode getRightBraceToken() {
        return getChildByRole(Roles.RIGHT_BRACE);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitEnumValueDeclaration(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof FieldDeclaration) {
            final FieldDeclaration otherDeclaration = (FieldDeclaration) other;

            return !otherDeclaration.isNull() &&
                   matchString(getName(), otherDeclaration.getName()) &&
                   matchAnnotationsAndModifiers(otherDeclaration, match) &&
                   getReturnType().matches(otherDeclaration.getReturnType(), match) &&
                   getMembers().matches(getMembers(), match);
        }

        return false;
    }
}
