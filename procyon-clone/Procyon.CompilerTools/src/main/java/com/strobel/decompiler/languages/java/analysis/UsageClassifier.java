/*
 * UsageClassifier.java
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

package com.strobel.decompiler.languages.java.analysis;

import com.strobel.decompiler.languages.java.ast.AssignmentExpression;
import com.strobel.decompiler.languages.java.ast.AssignmentOperatorType;
import com.strobel.decompiler.languages.java.ast.AstNode;
import com.strobel.decompiler.languages.java.ast.BinaryOperatorExpression;
import com.strobel.decompiler.languages.java.ast.Expression;
import com.strobel.decompiler.languages.java.ast.UnaryOperatorExpression;

public final class UsageClassifier {
    public static UsageType getUsageType(final Expression expression) {
        final AstNode parent = expression.getParent();

        if (parent instanceof BinaryOperatorExpression) {
            return UsageType.Read;
        }

        if (parent instanceof AssignmentExpression) {
            if (expression.matches(((AssignmentExpression) parent).getLeft())) {
                final AssignmentOperatorType operator = ((AssignmentExpression) parent).getOperator();

                if (operator == AssignmentOperatorType.ANY || operator == AssignmentOperatorType.ASSIGN) {
                    return UsageType.Write;
                }

                return UsageType.ReadWrite;
            }
            return UsageType.Read;
        }

        if (parent instanceof UnaryOperatorExpression) {
            final UnaryOperatorExpression unary = (UnaryOperatorExpression) parent;

            switch (unary.getOperator()) {
                case ANY:
                    return UsageType.ReadWrite;

                case NOT:
                case BITWISE_NOT:
                case MINUS:
                case PLUS:
                    return UsageType.Read;

                case INCREMENT:
                case DECREMENT:
                case POST_INCREMENT:
                case POST_DECREMENT:
                    return UsageType.ReadWrite;
            }
        }

        return UsageType.Read;
    }
}
