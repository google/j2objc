/*
 * TypedPrimitiveValueNode.java
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
import com.strobel.decompiler.languages.java.ast.PrimitiveExpression;

public final class TypedLiteralNode extends Pattern {
    private final String _groupName;
    private final Class<?> _primitiveType;

    public TypedLiteralNode(final Class<?> primitiveType) {
        _groupName = null;
        _primitiveType = VerifyArgument.notNull(primitiveType, "primitiveType");
    }

    public TypedLiteralNode(final String groupName, final Class<?> primitiveType) {
        _groupName = groupName;
        _primitiveType = VerifyArgument.notNull(primitiveType, "primitiveType");
    }

    @Override
    public final boolean matches(final INode other, final Match match) {
        if (other instanceof PrimitiveExpression) {
            final PrimitiveExpression primitive = (PrimitiveExpression) other;

            if (_primitiveType.isInstance(primitive.getValue())) {
                match.add(_groupName, other);
                return true;
            }
        }
        return false;
    }
}
