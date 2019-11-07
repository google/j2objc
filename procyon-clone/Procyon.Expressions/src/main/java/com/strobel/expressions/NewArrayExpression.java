/*
 * NewArrayExpression.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is based on the Dynamic Language Runtime from Microsoft,
 *   Copyright (c) Microsoft Corporation.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.expressions;

import com.strobel.reflection.Type;

/**
 * @author Mike Strobel
 */
public class NewArrayExpression extends Expression {
    private final ExpressionList<? extends Expression> _expressions;
    private final Type _type;

    public NewArrayExpression(final Type type, final ExpressionList<? extends Expression> expressions) {
        _type = type;
        _expressions = expressions;
    }

    public final ExpressionList<? extends Expression> getExpressions() {
        return _expressions;
    }

    @Override
    public final Type<?> getType() {
        return _type;
    }

    @Override
    protected final Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitNewArray(this);
    }

    public final NewArrayExpression update(final ExpressionList<? extends Expression> expressions) {
        if (expressions == _expressions) {
            return this;
        }
        if (getNodeType() == ExpressionType.NewArrayInit) {
            return newArrayInit(_type.getElementType(), expressions);
        }
        return newArrayBounds(_type.getElementType(), expressions.get(0));
    }

    static NewArrayExpression make(final ExpressionType nodeType, final Type type, final ExpressionList<? extends Expression> expressions) {
        if (nodeType == ExpressionType.NewArrayInit) {
            return new NewArrayInitExpression(type, expressions);
        }
        else {
            return new NewArrayBoundsExpression(type, expressions);
        }
    }
}

final class NewArrayBoundsExpression extends NewArrayExpression {
    NewArrayBoundsExpression(final Type type, final ExpressionList<? extends Expression> expressions) {
        super(type, expressions);
    }

    @Override
    public final ExpressionType getNodeType() {
        return ExpressionType.NewArrayBounds;
    }
}

final class NewArrayInitExpression extends NewArrayExpression {
    NewArrayInitExpression(final Type type, final ExpressionList<? extends Expression> expressions) {
        super(type, expressions);
    }

    @Override
    public final ExpressionType getNodeType() {
        return ExpressionType.NewArrayInit;
    }
}