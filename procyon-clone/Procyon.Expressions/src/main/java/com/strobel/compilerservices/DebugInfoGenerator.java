/*
 * DebugInfoGenerator.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.compilerservices;

import com.strobel.expressions.Expression;
import com.strobel.expressions.LambdaExpression;
import com.strobel.reflection.MethodBase;
import com.strobel.reflection.emit.CodeGenerator;
import com.strobel.reflection.emit.LocalBuilder;

/**
 * @author strobelm
 */
@SuppressWarnings("UnusedParameters")
public abstract class DebugInfoGenerator {
    private static final DebugInfoGenerator EMPTY = new DebugInfoGenerator() {
        @Override
        public void markSequencePoint(final LambdaExpression<?> method, final int bytecodeOffset, final Expression sequencePoint) {
        }
    };
    
    public static DebugInfoGenerator empty() {
        return EMPTY;
    }
    
    public abstract void markSequencePoint(
        final LambdaExpression<?> method,
        final int bytecodeOffset,
        final Expression sequencePoint);

    public void markSequencePoint(
        final LambdaExpression<?> method,
        final MethodBase methodBase,
        final CodeGenerator generator,
        final Expression sequencePoint) {
        
        markSequencePoint(method, generator.offset(), sequencePoint);
    }
    
    public void setLocalName(final LocalBuilder local, final String name) {}
}
