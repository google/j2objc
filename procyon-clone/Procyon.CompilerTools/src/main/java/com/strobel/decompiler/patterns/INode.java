/*
 * INode.java
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

import com.strobel.functions.Function;
import com.strobel.util.ContractUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;

public interface INode {
    boolean isNull();
    Role<?> getRole();
    INode getFirstChild();
    INode getNextSibling();

    boolean matches(final INode other, final Match match);
    boolean matchesCollection(final Role role, final INode position, final Match match, final BacktrackingInfo backtrackingInfo);
    Match match(INode other);
    boolean matches(INode other);

    public final static Function<INode, Iterable<INode>> CHILD_ITERATOR = new Function<INode, Iterable<INode>>() {
        @Override
        public Iterable<INode> apply(final INode input) {
            return new Iterable<INode>() {
                @Override
                public final Iterator<INode> iterator() {
                    return new Iterator<INode>() {
                        INode next = input.getFirstChild();

                        @Override
                        public final boolean hasNext() {
                            return next != null;
                        }

                        @Override
                        public final INode next() {
                            final INode result = next;

                            if (result == null) {
                                throw new NoSuchElementException();
                            }

                            next = result.getNextSibling();

                            return result;
                        }

                        @Override
                        public final void remove() {
                            throw ContractUtils.unsupported();
                        }
                    };
                }
            };
        }
    };
}
