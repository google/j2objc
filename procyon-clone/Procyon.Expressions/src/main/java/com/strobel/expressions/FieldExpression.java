/*
 * FieldExpression.java
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

import com.strobel.reflection.FieldInfo;
import com.strobel.reflection.Type;

/**
 * Represents accessing a field.
 * @author Mike Strobel
 */
final class FieldExpression extends MemberExpression {

    private final FieldInfo _field;

    FieldExpression(final Expression target, final FieldInfo field) {
        super(target);
        _field = field;
    }

    @Override
    FieldInfo getMember() {
        return _field;
    }

    @Override
    public final Type<?> getType() {
        return _field.getFieldType();
    }
}
