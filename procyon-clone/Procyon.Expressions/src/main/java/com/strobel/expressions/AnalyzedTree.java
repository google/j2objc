/*
 * AnalyzedTree.java
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

import com.strobel.compilerservices.DebugInfoGenerator;

import java.util.HashMap;
import java.util.Map;

final class AnalyzedTree {
    private DebugInfoGenerator _debugInfoGenerator;

    final Map<Object, CompilerScope> scopes = new HashMap<>();
    final Map<LambdaExpression, BoundConstants> constants = new HashMap<>();

    DebugInfoGenerator getDebugInfoGenerator() {
        if (_debugInfoGenerator == null) {
            return DebugInfoGenerator.empty();
        }
        return _debugInfoGenerator;
    }

    void setDebugInfoGenerator(final DebugInfoGenerator debugInfoGenerator) {
        _debugInfoGenerator = debugInfoGenerator;
    }
}

