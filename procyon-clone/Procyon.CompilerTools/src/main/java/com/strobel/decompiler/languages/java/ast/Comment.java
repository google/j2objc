/*
 * Comment.java
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

import com.strobel.core.Comparer;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;

public class Comment extends AstNode {
    private CommentType _commentType;
    private boolean _startsLine;
    private String _content;

    public Comment(final String content) {
        this(content, CommentType.SingleLine);
    }

    public Comment(final String content, final CommentType commentType) {
        _commentType = commentType;
        _content = content;
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitComment(this, data);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.WHITESPACE;
    }

    public final CommentType getCommentType() {
        return _commentType;
    }

    public final void setCommentType(final CommentType commentType) {
        verifyNotFrozen();
        _commentType = commentType;
    }

    public final boolean getStartsLine() {
        return _startsLine;
    }

    public final void setStartsLine(final boolean startsLine) {
        verifyNotFrozen();
        _startsLine = startsLine;
    }

    public final String getContent() {
        return _content;
    }

    public final void setContent(final String content) {
        verifyNotFrozen();
        _content = content;
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof Comment) {
            final Comment otherComment = (Comment) other;

            return otherComment._commentType == _commentType &&
                   Comparer.equals(otherComment._content, _content);
        }

        return false;
    }
}
