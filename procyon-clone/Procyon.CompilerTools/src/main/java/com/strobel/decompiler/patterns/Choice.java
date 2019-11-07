/*
 * Choice.java
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
import com.strobel.core.VerifyArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public final class Choice extends Pattern implements Iterable<INode> {
    private final ArrayList<INode> _alternatives = new ArrayList<>();

    public Choice() {
    }

    public Choice(final INode... alternatives) {
        Collections.addAll(_alternatives, VerifyArgument.notNull(alternatives, "alternatives"));
    }

    public final void add(final INode alternative) {
        _alternatives.add(VerifyArgument.notNull(alternative, "alternative"));
    }

    public final void add(final String name, final INode alternative) {
        _alternatives.add(new NamedNode(name, VerifyArgument.notNull(alternative, "alternative")));
    }

    @NotNull
    @Override
    public final Iterator<INode> iterator() {
        return _alternatives.iterator();
    }

    @Override
    public final boolean matches(final INode other, final Match match) {
        final int checkpoint = match.getCheckPoint();

        for (final INode alternative : _alternatives) {
            if (alternative.matches(other, match)) {
                return true;
            }
            match.restoreCheckPoint(checkpoint);
        }

        return false;
    }
}
