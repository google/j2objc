/*
 * TryCatchStatement.java
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
import com.strobel.decompiler.patterns.Role;

public class TryCatchStatement extends Statement {
    public static final TokenRole TRY_KEYWORD_ROLE = new TokenRole("try", TokenRole.FLAG_KEYWORD);
    public static final Role<BlockStatement> TRY_BLOCK_ROLE = new Role<>("TryBlock", BlockStatement.class, BlockStatement.NULL);
    public static final Role<CatchClause> CATCH_CLAUSE_ROLE = new Role<>("CatchClause", CatchClause.class);
    public static final TokenRole FINALLY_KEYWORD_ROLE = new TokenRole("finally", TokenRole.FLAG_KEYWORD);
    public static final Role<BlockStatement> FINALLY_BLOCK_ROLE = new Role<>("FinallyBlock", BlockStatement.class, BlockStatement.NULL);
    public static final Role<VariableDeclarationStatement> TRY_RESOURCE_ROLE = new Role<>("TryResource", VariableDeclarationStatement.class);

    public TryCatchStatement() {
        super(Expression.MYSTERY_OFFSET);
    }

    public TryCatchStatement(final int offset) {
        super(offset);
    }

    public final JavaTokenNode getTryToken() {
        return getChildByRole(TRY_KEYWORD_ROLE);
    }

    public final JavaTokenNode getFinallyToken() {
        return getChildByRole(FINALLY_KEYWORD_ROLE);
    }

    public final AstNodeCollection<CatchClause> getCatchClauses() {
        return getChildrenByRole(CATCH_CLAUSE_ROLE);
    }

    public final AstNodeCollection<VariableDeclarationStatement> getResources() {
        return getChildrenByRole(TRY_RESOURCE_ROLE);
    }

    public final BlockStatement getTryBlock() {
        return getChildByRole(TRY_BLOCK_ROLE);
    }

    public final void setTryBlock(final BlockStatement value) {
        setChildByRole(TRY_BLOCK_ROLE, value);
    }

    public final BlockStatement getFinallyBlock() {
        return getChildByRole(FINALLY_BLOCK_ROLE);
    }

    public final void setFinallyBlock(final BlockStatement value) {
        setChildByRole(FINALLY_BLOCK_ROLE, value);
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitTryCatchStatement(this, data);
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof TryCatchStatement) {
            final TryCatchStatement otherStatement = (TryCatchStatement) other;

            return !otherStatement.isNull() &&
                   getTryBlock().matches(otherStatement.getTryBlock(), match) &&
                   getCatchClauses().matches(otherStatement.getCatchClauses(), match) &&
                   getFinallyBlock().matches(otherStatement.getFinallyBlock(), match);
        }

        return false;
    }
}
