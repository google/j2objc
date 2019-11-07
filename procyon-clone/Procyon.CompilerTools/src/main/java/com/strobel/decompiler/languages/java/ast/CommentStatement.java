/*
 * CommentStatement.java
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

final class CommentStatement extends Statement {
    private final String _comment;

    CommentStatement(final String comment) {
        super( Expression.MYSTERY_OFFSET);
        _comment = comment;
    }

    final String getComment() {
        return _comment;
    }

    public static void replaceAll(final AstNode tree) {
        for (final AstNode node : tree.getDescendants()) {
            if (node instanceof CommentStatement) {
                node.getParent().insertChildBefore(
                    node,
                    new Comment(((CommentStatement) node).getComment()),
                    Roles.COMMENT
                );
                node.remove();
            }
        }
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return null;
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof CommentStatement &&
               matchString(_comment, ((CommentStatement) other)._comment);
    }
}
