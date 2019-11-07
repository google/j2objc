/*
 * BlockStatement.java
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

import java.util.Collections;
import java.util.Iterator;

public class BlockStatement extends Statement implements Iterable<Statement> {
    public final static Role<Statement> STATEMENT_ROLE = new Role<>("Statement", Statement.class, Statement.NULL);

    public BlockStatement() {
        super(Expression.MYSTERY_OFFSET);
    }

    public BlockStatement(final Iterable<Statement> statements) {
        super(Expression.MYSTERY_OFFSET);
        if (statements != null) {
            for (final Statement statement : statements) {
                getStatements().add(statement);
            }
        }
    }

    public BlockStatement(final Statement... statements) {
        super(Expression.MYSTERY_OFFSET);
        Collections.addAll(getStatements(), statements);
    }

    public final JavaTokenNode getLeftBraceToken() {
        return getChildByRole(Roles.LEFT_BRACE);
    }

    public final AstNodeCollection<Statement> getStatements() {
        return getChildrenByRole(STATEMENT_ROLE);
    }

    public final JavaTokenNode getRightBraceToken() {
        return getChildByRole(Roles.RIGHT_BRACE);
    }

    public final void add(final Statement statement) {
        addChild(statement, STATEMENT_ROLE);
    }

    public final void add(final Expression expression) {
        addChild(new ExpressionStatement(expression), STATEMENT_ROLE);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitBlockStatement(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof BlockStatement &&
               !other.isNull() &&
               getStatements().matches(((BlockStatement) other).getStatements(), match);
    }

    @Override
    public final Iterator<Statement> iterator() {
        return getStatements().iterator();
    }

    // <editor-fold defaultstate="collapsed" desc="Null BlockStatement">

    public final static BlockStatement NULL = new NullBlockStatement();

    private static final class NullBlockStatement extends BlockStatement {
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

    public static BlockStatement forPattern(final Pattern pattern) {
        return new PatternPlaceholder(VerifyArgument.notNull(pattern, "pattern"));
    }

    private final static class PatternPlaceholder extends BlockStatement {
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
