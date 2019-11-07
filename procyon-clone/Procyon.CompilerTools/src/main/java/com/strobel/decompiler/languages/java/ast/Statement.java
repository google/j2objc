/*
 * Statement.java
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

public abstract class Statement extends AstNode {

    /**
     * the offset of 'this' Expression, as computed for its bytecode by the Java compiler
     */
    private int _offset;

    protected Statement(final int offset) {
        _offset = offset;
    }

    @Override
    public Statement clone() {
        return (Statement) super.clone();
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.STATEMENT;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Role<? extends Statement> getRole() {
        return (Role<? extends Statement>) super.getRole();
    }

    public boolean isEmbeddable() {
        return false;
    }

    // <editor-fold defaultstate="collapsed" desc="Null Statement">

    public final static Statement NULL = new NullStatement(Expression.MYSTERY_OFFSET);

    public final Statement getNextStatement() {
        AstNode next = getNextSibling();

        while (next != null && !(next instanceof Statement)) {
            next = next.getNextSibling();
        }

        return (Statement) next;
    }

    public final Statement getPreviousStatement() {
        AstNode previous = getPreviousSibling();

        while (previous != null && !(previous instanceof Statement)) {
            previous = previous.getPreviousSibling();
        }

        return (Statement) previous;
    }

    private static final class NullStatement extends Statement {
        public NullStatement(final int offset) {
            super(offset);
        }

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

    public static Statement forPattern(final Pattern pattern) {
        return new PatternPlaceholder(Expression.MYSTERY_OFFSET, VerifyArgument.notNull(pattern, "pattern"));
    }

    private final static class PatternPlaceholder extends Statement {
        final Pattern child;

        PatternPlaceholder(final int offset, final Pattern child) {
            super(offset);
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

    /**
     * Returns the bytecode offset for 'this' expression, as computed by the Java compiler.
     */
    public int getOffset() {
        return _offset;
    }

    // </editor-fold>
}
