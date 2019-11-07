/*
 * SingleOrBinaryAggregateNode.java
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
import com.strobel.decompiler.languages.java.ast.BinaryOperatorExpression;
import com.strobel.decompiler.languages.java.ast.BinaryOperatorType;

public final class SingleOrBinaryAggregateNode extends Pattern {
    private final INode _pattern;
    private final BinaryOperatorType _operator;

    public SingleOrBinaryAggregateNode(final BinaryOperatorType operator, final INode pattern) {
        _pattern = VerifyArgument.notNull(pattern, "pattern");
        _operator = VerifyArgument.notNull(operator, "operator");
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (_pattern.matches(other, match)) {
            return true;
        }

        if (other instanceof BinaryOperatorExpression) {
            final BinaryOperatorExpression binary = (BinaryOperatorExpression) other;

            if (_operator != BinaryOperatorType.ANY && binary.getOperator() != _operator) {
                return false;
            }

            final int checkPoint = match.getCheckPoint();

            if (matches(binary.getLeft(), match) && matches(binary.getRight(), match)) {
                return true;
            }

            match.restoreCheckPoint(checkPoint);
        }

        return false;
    }
}
