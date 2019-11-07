/*
 * LeftmostBinaryOperandNode.java
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

public class LeftmostBinaryOperandNode extends Pattern {
    private final boolean _matchWithoutOperator;
    private final BinaryOperatorType _operatorType;
    private final INode _operandPattern;

    public LeftmostBinaryOperandNode(final INode pattern) {
        this(pattern, BinaryOperatorType.ANY, false);
    }

    public LeftmostBinaryOperandNode(final INode pattern, final BinaryOperatorType type, final boolean matchWithoutOperator) {
        _matchWithoutOperator = matchWithoutOperator;
        _operatorType = VerifyArgument.notNull(type, "type");
        _operandPattern = VerifyArgument.notNull(pattern, "pattern");
    }

    public final INode getOperandPattern() {
        return _operandPattern;
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (_matchWithoutOperator || other instanceof BinaryOperatorExpression) {
            INode current = other;

            while (current instanceof BinaryOperatorExpression &&
                   (_operatorType == BinaryOperatorType.ANY ||
                    ((BinaryOperatorExpression) current).getOperator() == _operatorType)) {

                current = ((BinaryOperatorExpression) current).getLeft();
            }

            return current != null && _operandPattern.matches(current, match);
        }

        return false;
    }
}
