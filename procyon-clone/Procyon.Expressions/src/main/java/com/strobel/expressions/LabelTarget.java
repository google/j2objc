/*
 * LabelTarget.java
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

import com.strobel.core.StringUtilities;
import com.strobel.reflection.Type;

/**
 * Used to denote the target of a {@link GotoExpression}
 * @author Mike Strobel
 */
public final class LabelTarget {
    private final Type _type;
    private final String _name;

    LabelTarget(final Type type, final String name) {
        _type = type;
        _name = name;
    }

    public final String getName() {
        return _name;
    }

    public final Type getType() {
        return _type;
    }

    public final String toString() {
        return StringUtilities.isNullOrEmpty(_name) ? "UnnamedLabel" : _name;
    }
}
