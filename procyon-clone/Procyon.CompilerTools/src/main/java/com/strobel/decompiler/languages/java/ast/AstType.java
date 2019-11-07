/*
 * AstType.java
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

import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.patterns.BacktrackingInfo;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.Pattern;
import com.strobel.decompiler.patterns.Role;
import com.strobel.util.ContractUtils;

public abstract class AstType extends AstNode {
    public final static AstType[] EMPTY_TYPES = new AstType[0];

    @Override
    public NodeType getNodeType() {
        return NodeType.TYPE_REFERENCE;
    }

    public TypeReference toTypeReference() {
        return getUserData(Keys.TYPE_REFERENCE);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Role<? extends AstType> getRole() {
        return (Role<? extends AstType>) super.getRole();
    }

    // <editor-fold defaultstate="collapsed" desc="Null AstType">

    public final static AstType NULL = new NullAstType();

    private static final class NullAstType extends AstType {
        @Override
        public boolean isNull() {
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

        @Override
        public TypeReference toTypeReference() {
            throw ContractUtils.unreachable();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="PatternPlaceholder">

    public static AstType forPattern(final Pattern pattern) {
        return new PatternPlaceholder(VerifyArgument.notNull(pattern, "pattern"));
    }

    private final static class PatternPlaceholder extends AstType {
        private final Pattern _child;

        PatternPlaceholder(final Pattern child) {
            _child = child;
        }

        @Override
        public NodeType getNodeType() {
            return NodeType.PATTERN;
        }

        @Override
        public TypeReference toTypeReference() {
            throw ContractUtils.unsupported();
        }

        @Override
        public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
            return visitor.visitPatternPlaceholder(this, _child, data);
        }

        @Override
        public boolean matches(final INode other, final Match match) {
            return _child.matches(other, match);
        }

        @Override
        public boolean matchesCollection(final Role role, final INode position, final Match match, final BacktrackingInfo backtrackingInfo) {
            return _child.matchesCollection(role, position, match, backtrackingInfo);
        }
    }

    // </editor-fold>

    public AstType clone() {
        return (AstType) super.clone();
    }

    public AstType makeArrayType() {
        final ComposedType composedType = new ComposedType();

        composedType.setBaseType(this);

        final TypeReference typeReference = getUserData(Keys.TYPE_REFERENCE);

        if (typeReference != null) {
            composedType.putUserData(Keys.TYPE_REFERENCE, typeReference);
        }

        composedType.makeArrayType();

        return composedType;
    }

    public InvocationExpression invoke(final String methodName, final Expression... arguments) {
        return makeReference().invoke(methodName, arguments);
    }

    public InvocationExpression invoke(final String methodName, final Iterable<Expression> arguments) {
        return makeReference().invoke(methodName, arguments);
    }

    public InvocationExpression invoke(final String methodName, final Iterable<AstType> typeArguments, final Expression... arguments) {
        return makeReference().invoke(methodName, typeArguments, arguments);
    }

    public InvocationExpression invoke(final String methodName, final Iterable<AstType> typeArguments, final Iterable<Expression> arguments) {
        return makeReference().invoke(methodName, typeArguments, arguments);
    }

    public MemberReferenceExpression member(final String memberName) {
        return makeReference().member(memberName);
    }

    public TypeReferenceExpression makeReference() {
        final TypeReferenceExpression t = new TypeReferenceExpression(Expression.MYSTERY_OFFSET, this);
        final TypeReference r = getUserData(Keys.TYPE_REFERENCE);

        if (r != null) {
            t.putUserData(Keys.TYPE_REFERENCE, r);
        }

        return t;
    }

    public ObjectCreationExpression makeNew() {
        return new ObjectCreationExpression(Expression.MYSTERY_OFFSET, this, Expression.EMPTY_EXPESSIONS);
    }

    public ObjectCreationExpression makeNew(final Expression... arguments) {
        return new ObjectCreationExpression(Expression.MYSTERY_OFFSET, this, arguments);
    }

    public ObjectCreationExpression makeNew(final Iterable<Expression> arguments) {
        return new ObjectCreationExpression(Expression.MYSTERY_OFFSET, this, arguments);
    }
}
