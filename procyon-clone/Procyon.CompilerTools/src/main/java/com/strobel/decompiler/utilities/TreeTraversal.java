/*
 * TreeTraversal.java
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

package com.strobel.decompiler.utilities;

import com.strobel.core.Pair;
import com.strobel.functions.Function;
import com.strobel.util.ContractUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

public final class TreeTraversal {
    public static <T> Iterable<T> preOrder(final T root, final Function<T, Iterable<T>> recursion) {
        return preOrder(Collections.singletonList(root), recursion);
    }

    public static <T> Iterable<T> preOrder(final Iterable<T> input, final Function<T, Iterable<T>> recursion) {
        return new Iterable<T>() {
            @Override
            public final Iterator<T> iterator() {
                return new Iterator<T>() {
                    final Stack<Iterator<T>> stack = new Stack<>();

                    boolean returnedCurrent;
                    T next;

                    /* new() */ {
                        stack.push(input.iterator());
                    }

                    private T selectNext() {
                        if (next != null) {
                            return next;
                        }

                        while (!stack.isEmpty()) {
                            if (stack.peek().hasNext()) {
                                next = stack.peek().next();

                                if (next != null) {
                                    final Iterable<T> children = recursion.apply(next);

                                    if (children != null) {
                                        stack.push(children.iterator());
                                    }
                                }

                                return next;
                            }

                            stack.pop();
                        }

                        return null;
                    }

                    @Override
                    public final boolean hasNext() {
                        return selectNext() != null;
                    }

                    @Override
                    public final T next() {
                        final T next = selectNext();

                        if (next == null) {
                            throw new NoSuchElementException();
                        }

                        this.next = null;
                        return next;
                    }

                    @Override
                    public final void remove() {
                        throw ContractUtils.unsupported();
                    }
                };
            }
        };
    }

    public static <T> Iterable<T> postOrder(final T root, final Function<T, Iterable<T>> recursion) {
        return postOrder(Collections.singletonList(root), recursion);
    }

    public static <T> Iterable<T> postOrder(final Iterable<T> input, final Function<T, Iterable<T>> recursion) {
        return new Iterable<T>() {
            @Override
            public final Iterator<T> iterator() {
                return new Iterator<T>() {
                    final Stack<Pair<Iterator<T>, T>> stack = new Stack<>();

                    boolean returnedCurrent;
                    T next;

                    /* new() */ {
                        stack.push(Pair.create(input.iterator(), (T) null));
                    }

                    private T selectNext() {
                        if (next != null) {
                            return next;
                        }

                        while (!stack.isEmpty()) {
                            while (stack.peek().getFirst().hasNext()) {
                                next = stack.peek().getFirst().next();

                                if (next != null) {
                                    final Iterable<T> children = recursion.apply(next);

                                    if (children != null) {
                                        stack.push(Pair.create(children.iterator(), next));
                                        continue;
                                    }
                                }

                                return next;
                            }

                            next = stack.pop().getSecond();

                            if (next != null) {
                                return next;
                            }
                        }

                        return null;
                    }

                    @Override
                    public final boolean hasNext() {
                        return selectNext() != null;
                    }

                    @Override
                    public final T next() {
                        final T next = selectNext();

                        if (next == null) {
                            throw new NoSuchElementException();
                        }

                        this.next = null;
                        return next;
                    }

                    @Override
                    public final void remove() {
                        throw ContractUtils.unsupported();
                    }
                };
            }
        };
    }

/*
    public static void main(String[] args) {
        final class Node {
            final String self;
            final List<Node> children;

            Node(final String self, final Node... children) {
                this.self = self;
                this.children = ArrayUtilities.asUnmodifiableList(children);
            }
        }

        final List<Node> input = ArrayUtilities.asUnmodifiableList(
            new Node(
                "A",
                new Node(
                    "B",
                    new Node("C"), new Node("D")
                ),
                new Node("E")
            ),
            new Node("F")
        );

        final Function<Node, Iterable<Node>> recursion = new Function<Node, Iterable<Node>>() {
            @Override
            public Iterable<Node> apply(final Node input) {
                return input.children;
            }
        };
        final Iterable<Node> preOrderResults = TreeTraversal.preOrder(input, recursion);
        final Iterable<Node> postOrderResults = TreeTraversal.postOrder(input, recursion);

        int count = 0;

        System.out.print("Pre-Order: ");

        for (final Node n : preOrderResults) {
            if (count > 10) {
                throw new IllegalStateException();
            }
            System.out.print(n.self);
        }

        count = 0;

        System.out.println();
        System.out.print("Post-Order: ");

        for (final Node n : postOrderResults) {
            if (count > 10) {
                throw new IllegalStateException();
            }
            System.out.print(n.self);
        }
    }
*/
}
