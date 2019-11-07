/*
 * SwitchCase.java
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

import com.strobel.core.VerifyArgument;

/**
 * @author strobelm
 */
public final class SwitchCase {
    private final ExpressionList<? extends Expression> _testValues;
    private final Expression _body;

    SwitchCase(final Expression body, final ExpressionList<? extends Expression> testValues) {
        _body = VerifyArgument.notNull(body, "body");
        _testValues = VerifyArgument.notNull(testValues, "testValues");
    }

    public final ExpressionList<? extends Expression> getTestValues() {
        return _testValues;
    }

    public final Expression getBody() {
        return _body;
    }

    @Override
    public final String toString() {
        return ExpressionStringBuilder.switchCaseToString(this);
    }

    public final SwitchCase update(final ExpressionList<? extends Expression> testValues, final Expression body) {
        if (testValues == _testValues && body == _body) {
            return this;
        }
        return Expression.switchCase(body, testValues);
    }
}
