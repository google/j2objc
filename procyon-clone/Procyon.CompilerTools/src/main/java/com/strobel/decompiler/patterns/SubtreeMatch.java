/*
 * SubtreeMatch.java
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

import com.strobel.core.Predicate;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.utilities.TreeTraversal;

import static com.strobel.core.CollectionUtilities.any;

public final class SubtreeMatch extends Pattern {
    private final boolean _matchMultiple;
    private final INode _target;

    public SubtreeMatch(final INode target) {
        this(target, false);
    }

    public SubtreeMatch(final INode target, final boolean matchMultiple) {
        _matchMultiple = matchMultiple;
        _target = VerifyArgument.notNull(target, "target");
    }

    public final INode getTarget() {
        return _target;
    }

    @Override
    public final boolean matches(final INode other, final Match match) {
        if (_matchMultiple) {
            boolean result = false;

            for (final INode n : TreeTraversal.preOrder(other, INode.CHILD_ITERATOR)) {
                if (_target.matches(n, match)) {
                    result = true;
                }
            }

            return result;
        }
        else {
            return any(
                TreeTraversal.preOrder(other, INode.CHILD_ITERATOR),
                new Predicate<INode>() {
                    @Override
                    public boolean test(final INode n) {
                        return _target.matches(n, match);
                    }
                }
            );
        }
    }
}
