/*
 * TypedNode.java
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

public class TypedNode extends Pattern {
    private final Class<? extends INode> _nodeType;
    private final String _groupName;

    public TypedNode(final Class<? extends INode> nodeType) {
        _nodeType = VerifyArgument.notNull(nodeType, "nodeType");
        _groupName = null;
    }

    public TypedNode(final String groupName, final Class<? extends INode> nodeType) {
        _groupName = groupName;
        _nodeType = VerifyArgument.notNull(nodeType, "nodeType");
    }

    public final Class<? extends INode> getNodeType() {
        return _nodeType;
    }

    public final String getGroupName() {
        return _groupName;
    }

    @Override
    public final boolean matches(final INode other, final Match match) {
        if (_nodeType.isInstance(other)) {
            match.add(_groupName, other);
            return !other.isNull();
        }
        return false;
    }
}
