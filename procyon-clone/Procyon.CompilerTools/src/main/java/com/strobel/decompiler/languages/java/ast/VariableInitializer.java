/*
 * VariableInitializer.java
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
import com.strobel.decompiler.patterns.BacktrackingInfo;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.Pattern;
import com.strobel.decompiler.patterns.Role;

public class VariableInitializer extends AstNode {
    public VariableInitializer() {
    }

    public VariableInitializer(final String name) {
        setName(name);
    }

    public VariableInitializer(final String name, final Expression initializer) {
        setName(name);
        setInitializer(initializer);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }

    public final Expression getInitializer() {
        return getChildByRole(Roles.EXPRESSION);
    }

    public final void setInitializer(final Expression value) {
        setChildByRole(Roles.EXPRESSION, value);
    }

    public final String getName() {
        return getChildByRole(Roles.IDENTIFIER).getName();
    }

    public final void setName(final String value) {
        setChildByRole(Roles.IDENTIFIER, Identifier.create(value));
    }

    public final Identifier getNameToken() {
        return getChildByRole(Roles.IDENTIFIER);
    }

    public final void setNameToken(final Identifier value) {
        setChildByRole(Roles.IDENTIFIER, value);
    }

    public final JavaTokenNode getAssignToken() {
        return getChildByRole(Roles.ASSIGN);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Role<? extends VariableInitializer> getRole() {
        return (Role<? extends VariableInitializer>) super.getRole();
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitVariableInitializer(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof VariableInitializer) {
            final VariableInitializer otherInitializer = (VariableInitializer) other;

            return !other.isNull() &&
                   matchString(getName(), otherInitializer.getName()) &&
                   getInitializer().matches(otherInitializer.getInitializer(), match);
        }

        return false;
    }

    @Override
    public String toString() {
        final Expression initializer = this.getInitializer();

        if (initializer.isNull()) {
            return "[VariableInitializer " + this.getName() + "]";
        }
        else {
            return "[VariableInitializer " + this.getName() + " = " + initializer + "]";
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Null VariableInitializer">

    public final static VariableInitializer NULL = new NullVariableInitializer();

    private static final class NullVariableInitializer extends VariableInitializer {
        @Override
        public final boolean isNull() {
            return true;
        }

        @Override
        public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
            return null;
        }

        @Override
        public boolean matches(final INode other, final Match match) {
            return other == null || other.isNull();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Pattern Placeholder">

    public static VariableInitializer forPattern(final Pattern pattern) {
        return new PatternPlaceholder(VerifyArgument.notNull(pattern, "pattern"));
    }

    private final static class PatternPlaceholder extends VariableInitializer {
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
