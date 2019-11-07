/*
 * Repeat.java
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

package com.strobel.decompiler.patterns;

import com.strobel.core.VerifyArgument;

import java.util.Stack;

public final class Repeat extends Pattern {
    private final INode _node;

    private int _minCount;
    private int _maxCount;

    public Repeat(final INode node) {
        _node = VerifyArgument.notNull(node, "node");
        _minCount = 0;
        _maxCount = Integer.MAX_VALUE;
    }

    public final INode getNode() {
        return _node;
    }

    public final int getMinCount() {
        return _minCount;
    }

    public final void setMinCount(final int minCount) {
        _minCount = minCount;
    }

    public final int getMaxCount() {
        return _maxCount;
    }

    public final void setMaxCount(final int maxCount) {
        _maxCount = maxCount;
    }

    @Override
    public final boolean matchesCollection(
        final Role role,
        final INode position,
        final Match match,
        final BacktrackingInfo backtrackingInfo) {

        final Stack<PossibleMatch> backtrackingStack = backtrackingInfo.stack;

        assert position == null || position.getRole() == role;

        int matchCount = 0;
        INode current = position;

        if (_minCount <= 0)
            backtrackingStack.push(new PossibleMatch(current, match.getCheckPoint()));

        while (matchCount < _maxCount &&
               current != null &&
               _node.matches(current, match)) {

            matchCount++;

            do {
                current = current.getNextSibling();
            } while (current != null && current.getRole() != role);

            if (matchCount >= _minCount) {
                backtrackingStack.push(new PossibleMatch(current, match.getCheckPoint()));
            }
        }

        //
        // Never do a single-element match; always make the caller look at the results on the
        // back-tracking stack.
        //
        return false;
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other == null || other.isNull()) {
            return _minCount <= 0;
        }
        return _maxCount >= 1 && _node.matches(other, match);
    }
}
