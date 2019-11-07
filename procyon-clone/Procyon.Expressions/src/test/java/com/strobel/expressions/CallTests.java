/*
 * CallTests.java
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

package com.strobel.expressions;

import com.strobel.reflection.MethodInfo;
import com.strobel.reflection.Type;
import com.strobel.reflection.TypeList;
import com.strobel.reflection.Types;
import com.strobel.reflection.emit.MethodBuilder;
import com.strobel.reflection.emit.TypeBuilder;
import org.junit.Test;

import java.lang.reflect.Modifier;

import static com.strobel.expressions.Expression.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author strobelm
 */
public class CallTests extends AbstractExpressionTest {
    @Test
    public void testCallSuperMethod() throws Throwable {
        final Type<BaseClass> baseType = Type.of(BaseClass.class);

        final TypeBuilder<?> derivedType = new TypeBuilder<>(
            baseType.getPackage().getName() + ".CallTestDerivedClass",
            baseType.getModifiers(),
            baseType,
            TypeList.empty()
        );

        final LambdaExpression lambda = lambda(
            concat(
                call(base(baseType), "toString"),
                constant(":Derived")
            )
        );

        final MethodInfo baseMethod = baseType.getMethod("toString");

        final MethodBuilder toString = derivedType.defineMethod(
            "toString",
            Modifier.PUBLIC|Modifier.FINAL,
            Types.String
        );

        lambda.compileToMethod(toString);

        final Type<?> generatedType = derivedType.createType();

        assertEquals("Base:Derived", generatedType.newInstance().toString());
        assertSame(baseMethod, toString.findOverriddenMethod());
    }

    static class BaseClass {
        @Override
        public String toString() {
            return "Base";
        }
    }
}
