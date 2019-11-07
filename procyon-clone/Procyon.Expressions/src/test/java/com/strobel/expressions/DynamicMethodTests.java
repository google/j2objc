/*
 * DynamicMethodTests.java
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

import com.strobel.core.VerifyArgument;
import com.strobel.reflection.DynamicMethod;
import com.strobel.reflection.Type;
import com.strobel.reflection.Types;
import org.junit.Before;
import org.junit.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import static com.strobel.expressions.Expression.*;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodType.methodType;
import static org.junit.Assert.assertTrue;

/**
 * @author Mike Strobel
 */
@SuppressWarnings("ALL")
public final class DynamicMethodTests {
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private final HashMap<String, Object> map = new HashMap<>();

    private final MetaProperty booleanProperty = new MetaProperty("BooleanProperty", boolean.class);

    @Before
    public void setup() {
        map.put(booleanProperty.name, true);
    }

    interface BooleanAccessor {
        boolean get(final Map<String,Object> map);
    }

    @Test
    public void testHashMapAccess() throws Throwable {
        final MethodHandle lookupHandle = getLookupHandle(booleanProperty);

        final boolean test1 = (boolean)lookupHandle.invoke(map);
        final boolean test2 = (Boolean)lookupHandle.invokeWithArguments(map);

        final ParameterExpression instance = parameter(Types.Map.makeGenericType(Types.String, Types.Object));
        
        final LambdaExpression<BooleanAccessor> accessorLambda = lambda(
            Type.of(BooleanAccessor.class),
            call(
                constant(lookupHandle, Types.MethodHandle),
                DynamicMethod.invokeExact(lookupHandle),
                instance
            ),
            instance
        );

        final BooleanAccessor accessor = accessorLambda.compile();
        final boolean result = accessor.get(map);

        assertTrue(result);
    }

    private MethodHandle getLookupHandle(final MetaProperty property) throws Throwable {
        final MethodHandle get = lookup.findVirtual(
            Map.class,
            "get",
            methodType(Object.class, Object.class)
        );

        return insertArguments(
            get.asType(
                get.type()
                   .changeReturnType(boolean.class)
                   .changeParameterType(
                       1,
                       String.class
                   )
            ),
            1,
//            map,
            property.name
        );
    }

    private final static class MetaProperty {
        final String name;
        final Class<?> type;

        private MetaProperty(final String name, final Class<?> type) {
            this.name = VerifyArgument.notNull(name, "name");
            this.type = VerifyArgument.notNull(type, "type");
        }
    }
}
