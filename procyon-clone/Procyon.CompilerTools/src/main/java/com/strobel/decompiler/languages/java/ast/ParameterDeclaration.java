/*
 * ParameterDeclaration.java
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
import com.strobel.decompiler.languages.EntityType;
import com.strobel.decompiler.patterns.BacktrackingInfo;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.Pattern;
import com.strobel.decompiler.patterns.Role;

public class ParameterDeclaration extends EntityDeclaration {
    public final static Role<Annotation> ANNOTATION_ROLE = EntityDeclaration.ANNOTATION_ROLE;

    public ParameterDeclaration() {
    }

    public ParameterDeclaration(final String name, final AstType type) {
        setName(name);
        setType(type);
    }

//    public final AstNodeCollection<Annotation> getAnnotations() {
//        return getChildrenByRole(ANNOTATION_ROLE);
//    }
//
//    public final String getName() {
//        return getChildByRole(Roles.IDENTIFIER).getName();
//    }
//
//    public final void setName(final String value) {
//        setChildByRole(Roles.IDENTIFIER, Identifier.create(value));
//    }
//
//    public final Identifier getNameToken() {
//        return getChildByRole(Roles.IDENTIFIER);
//    }
//
//    public final void setNameToken(final Identifier value) {
//        setChildByRole(Roles.IDENTIFIER, value);
//    }

    @Override
    @SuppressWarnings("unchecked")
    public Role<? extends ParameterDeclaration> getRole() {
        return (Role<? extends ParameterDeclaration>) super.getRole();
    }

    public final AstType getType() {
        return getChildByRole(Roles.TYPE);
    }

    public final void setType(final AstType value) {
        setChildByRole(Roles.TYPE, value);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PARAMETER;
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitParameterDeclaration(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof ParameterDeclaration) {
            final ParameterDeclaration otherDeclaration = (ParameterDeclaration) other;

            return !otherDeclaration.isNull() &&
                   matchAnnotationsAndModifiers(otherDeclaration, match) &&
                   matchString(getName(), otherDeclaration.getName()) &&
                   getType().matches(otherDeclaration.getType(), match);
        }

        return false;
    }

    // <editor-fold defaultstate="collapsed" desc="Pattern Placeholder">

    public static ParameterDeclaration forPattern(final Pattern pattern) {
        return new PatternPlaceholder(VerifyArgument.notNull(pattern, "pattern"));
    }

    private final static class PatternPlaceholder extends ParameterDeclaration {
        final Pattern child;

        PatternPlaceholder(final Pattern child) {
            this.child = child;
        }

        @Override
        public NodeType getNodeType() {
            return NodeType.PATTERN;
        }

        @Override
        public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
            return visitor.visitPatternPlaceholder(this, child, data);
        }

        @Override
        public boolean matchesCollection(final Role role, final INode position, final Match match, final BacktrackingInfo backtrackingInfo) {
            return child.matchesCollection(role, position, match, backtrackingInfo);
        }

        @Override
        public boolean matches(final INode other, final Match match) {
            return child.matches(other, match);
        }
    }

    // </editor-fold>
}
