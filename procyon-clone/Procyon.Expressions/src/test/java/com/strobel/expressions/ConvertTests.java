/*
 * ConvertTests.java
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

import com.strobel.reflection.Type;
import com.strobel.reflection.TypeList;
import com.strobel.reflection.emit.TypeBuilder;
import org.junit.Test;

import java.lang.invoke.MethodHandle;

import static com.strobel.expressions.Expression.*;
import static org.junit.Assert.*;

/**
 * @author strobelm
 */
public class ConvertTests extends AbstractExpressionTest {
    @Test
    public void testTypeBuilderConversion() throws Throwable {
        final TypeBuilder<?> derivedClass = new TypeBuilder<>(
            BaseClass.class.getPackage().getName() + ".ConvertTestDerivedClass",
            BaseClass.class.getModifiers(),
            Type.of(BaseClass.class),
            TypeList.empty()
        );

        final LambdaExpression lambda = lambda(
            convert(
                makeNew(derivedClass.defineDefaultConstructor()),
                Type.of(AncestorClass.class)
            )
        );

        final MethodHandle handle = lambda.compileHandle();

        assertEquals(AncestorClass.class, handle.type().returnType());
        assertSame(derivedClass.createType(), Type.of(((AncestorClass)handle.invokeExact()).getClass()));
    }

    static class AncestorClass {}
    static class BaseClass extends AncestorClass {}
}
