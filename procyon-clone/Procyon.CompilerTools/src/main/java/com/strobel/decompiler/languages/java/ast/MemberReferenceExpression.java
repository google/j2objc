/*
 * MemberReferenceExpression.java
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

public class MemberReferenceExpression extends Expression {

    public MemberReferenceExpression(
        final Expression target,
        final String memberName,
        final AstType... typeArguments) {

        this(target.getOffset(), target, memberName, typeArguments);
    }

    public MemberReferenceExpression(
        final Expression target,
        final String memberName,
        final Iterable<AstType> typeArguments) {

        this(target.getOffset(), target, memberName, typeArguments);
    }

    public MemberReferenceExpression(
        final int offset,
        final Expression target,
        final String memberName,
        final Iterable<AstType> typeArguments) {

        super(offset);

        addChild(target, Roles.TARGET_EXPRESSION);

        setMemberName(memberName);

        if (typeArguments != null) {
            for (final AstType argument : typeArguments) {
                addChild(argument, Roles.TYPE_ARGUMENT);
            }
        }
    }

    public MemberReferenceExpression(
        final int offset,
        final Expression target,
        final String memberName,
        final AstType... typeArguments) {

        super(offset);

        addChild(target, Roles.TARGET_EXPRESSION);

        setMemberName(memberName);

        if (typeArguments != null) {
            for (final AstType argument : typeArguments) {
                addChild(argument, Roles.TYPE_ARGUMENT);
            }
        }
    }

    public final String getMemberName() {
        return getChildByRole(Roles.IDENTIFIER).getName();
    }

    public final void setMemberName(final String name) {
        setChildByRole(Roles.IDENTIFIER, Identifier.create(name));
    }

    public final Identifier getMemberNameToken() {
        return getChildByRole(Roles.IDENTIFIER);
    }

    public final void setMemberNameToken(final Identifier token) {
        setChildByRole(Roles.IDENTIFIER, token);
    }

    public final Expression getTarget() {
        return getChildByRole(Roles.TARGET_EXPRESSION);
    }

    public final void setTarget(final Expression value) {
        setChildByRole(Roles.TARGET_EXPRESSION, value);
    }

    public final AstNodeCollection<AstType> getTypeArguments() {
        return getChildrenByRole(Roles.TYPE_ARGUMENT);
    }

    public final JavaTokenNode getDotToken() {
        return getChildByRole(Roles.DOT);
    }

    public final JavaTokenNode getLeftChevronToken() {
        return getChildByRole(Roles.LEFT_CHEVRON);
    }

    public final JavaTokenNode getRightChevronToken() {
        return getChildByRole(Roles.RIGHT_CHEVRON);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitMemberReferenceExpression(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof MemberReferenceExpression) {
            final MemberReferenceExpression otherExpression = (MemberReferenceExpression) other;

            return !otherExpression.isNull() &&
                   getTarget().matches(otherExpression.getTarget(), match) &&
                   matchString(getMemberName(), otherExpression.getMemberName()) &&
                   getTypeArguments().matches(otherExpression.getTypeArguments(), match);
        }

        return false;
    }
}
