/*
 * AnyNode.java
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

public final class AnyNode extends Pattern {
    private final String _groupName;

    public AnyNode() {
        _groupName = null;
    }

    public AnyNode(final String groupName) {
        _groupName = groupName;
    }

    public final String getGroupName() {
        return _groupName;
    }

    @Override
    public final boolean matches(final INode other, final Match match) {
        match.add(_groupName, other);
        return other != null && !other.isNull();
    }
}
