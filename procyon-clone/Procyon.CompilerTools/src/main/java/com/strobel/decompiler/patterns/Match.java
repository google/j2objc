/*
 * Match.java
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

import com.strobel.annotations.NotNull;
import com.strobel.core.Pair;
import com.strobel.core.StringUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class Match {
    private final static Match FAILURE = new Match(null);

    private final List<Pair<String, INode>> _results;

    private Match(final List<Pair<String, INode>> results) {
        _results = results;
    }

    public final boolean success() {
        return _results != null;
    }

    public final void add(final String groupName, final INode node) {
        if (groupName != null && node != null) {
            _results.add(Pair.create(groupName, node));
        }
    }

    public final boolean has(final String groupName) {
        for (int i = 0; i < _results.size(); i++) {
            if (StringUtilities.equals(groupName, _results.get(i).getFirst())) {
                return true;
            }
        }

        return false;
    }

    public final <T extends INode> Iterable<T> get(final String groupName) {
        if (_results == null) {
            return Collections.emptyList();
        }

        return new Iterable<T>() {
            @NotNull
            @Override
            public final Iterator<T> iterator() {
                return new Iterator<T>() {
                    int index = 0;
                    boolean ready;
                    T next;

                    @Override
                    public boolean hasNext() {
                        if (!ready) {
                            selectNext();
                        }

                        return ready;
                    }

                    @SuppressWarnings("unchecked")
                    private void selectNext() {
                        for (; index < _results.size(); index++) {
                            final Pair<String, INode> pair = _results.get(index);

                            if (StringUtilities.equals(groupName, pair.getFirst())) {
                                next = (T) pair.getSecond();
                                ready = true;
                                index++;
                                return;
                            }
                        }
                    }

                    @Override
                    public T next() {
                        if (!ready) {
                            selectNext();
                        }

                        if (ready) {
                            final T result = next;

                            next = null;
                            ready = false;

                            return result;
                        }

                        throw new NoSuchElementException();
                    }

                    @Override
                    public void remove() {
                    }
                };
            }
        };
    }

    final int getCheckPoint() {
        return _results.size();
    }

    final void restoreCheckPoint(final int checkpoint) {
        for (int i = _results.size() - 1; i >= checkpoint; i--) {
            _results.remove(i);
        }
    }

    public static Match createNew() {
        return new Match(new ArrayList<Pair<String, INode>>());
    }

    public static Match failure() {
        return FAILURE;
    }
}
