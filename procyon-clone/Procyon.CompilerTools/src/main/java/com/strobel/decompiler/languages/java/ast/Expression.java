/*
 * Expression.java
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

public abstract class Expression extends AstNode {
    public final static Expression[] EMPTY_EXPESSIONS = new Expression[0];

    // <editor-fold defaultstate="collapsed" desc="Null Expression">

    public final static Expression NULL = new NullExpression();

    /**
     * a constant to indicate that no bytecode offset is known for an expression
     */
    public static final int MYSTERY_OFFSET = com.strobel.decompiler.ast.Expression.MYSTERY_OFFSET;

    /**
     * the offset of 'this' Expression, as computed for its bytecode by the Java compiler
     */
    private int _offset;

    protected Expression(final int offset) {
        _offset = offset;
    }

    /**
     * Returns the bytecode offset for 'this' expression.
     */
    public int getOffset() {
        return _offset;
    }

    /**
     * Sets the bytecode offset for 'this' expression.
     */
    public void setOffset(final int offset) {
        _offset = offset;
    }

    private static final class NullExpression extends Expression {
        public NullExpression() {
            super(Expression.MYSTERY_OFFSET);
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

    @Override
    public Expression clone() {
        return (Expression) super.clone();
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.EXPRESSION;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Role<? extends Expression> getRole() {
        return (Role<? extends Expression>) super.getRole();
    }

    // <editor-fold defaultstate="collapsed" desc="Pattern Placeholder">

    public static Expression forPattern(final Pattern pattern) {
        return new PatternPlaceholder(VerifyArgument.notNull(pattern, "pattern"));
    }

    private final static class PatternPlaceholder extends Expression {
        final Pattern child;

        PatternPlaceholder(final Pattern child) {
            super(Expression.MYSTERY_OFFSET);
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

    // <editor-fold defaultstate="collapsed" desc="Fluent Interface">

    public InvocationExpression invoke(final Expression... arguments) {
        return new InvocationExpression(this.getOffset(), this, arguments);
    }

    public InvocationExpression invoke(final Iterable<Expression> arguments) {
        return new InvocationExpression(this.getOffset(), this, arguments);
    }

    public InvocationExpression invoke(final String methodName, final Expression... arguments) {
        return invoke(methodName, null, arguments);
    }

    public InvocationExpression invoke(final String methodName, final Iterable<Expression> arguments) {
        return invoke(methodName, null, arguments);
    }

    public InvocationExpression invoke(final String methodName, final Iterable<AstType> typeArguments, final Expression... arguments) {
        final MemberReferenceExpression mre = new MemberReferenceExpression(this.getOffset(), this, methodName, typeArguments);
        return new InvocationExpression(this.getOffset(), mre, arguments);
    }

    public InvocationExpression invoke(final String methodName, final Iterable<AstType> typeArguments, final Iterable<Expression> arguments) {
        final MemberReferenceExpression mre = new MemberReferenceExpression(this.getOffset(), this, methodName, typeArguments);
        return new InvocationExpression(this.getOffset(), mre, arguments);
    }

    public MemberReferenceExpression member(final String memberName) {
        return new MemberReferenceExpression(this.getOffset(), this, memberName);
    }

    public CastExpression cast(final AstType type) {
        return new CastExpression(type, this);
    }

    public ReturnStatement makeReturn() {
        return new ReturnStatement(getOffset(), this);
    }

    public ThrowStatement makeThrow() {
        return new ThrowStatement(this);
    }

    // </editor-fold>
}
