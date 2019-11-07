/*
 * OptionalNode.java
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

public final class OptionalNode extends Pattern {
    private final INode _node;

    public OptionalNode(final INode node) {
        _node = VerifyArgument.notNull(node, "node");
    }

    public final INode getNode() {
        return _node;
    }

    @Override
    public final boolean matchesCollection(
        final Role role,
        final INode position,
        final Match match,
        final BacktrackingInfo backtrackingInfo) {

        backtrackingInfo.stack.push(new PossibleMatch(position, match.getCheckPoint()));
        return _node.matches(position, match);
    }

    @Override
    public final boolean matches(final INode other, final Match match) {
        return other == null || other.isNull() || _node.matches(other, match);
    }
}
