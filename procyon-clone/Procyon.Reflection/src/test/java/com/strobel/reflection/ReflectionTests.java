/*
 * ReflectionTests.java
 *
 * Copyright (c) 2014 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.reflection;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ReflectionTests {
    @Test
    public void testGetErasedNonGenericMethodFromGenericTypeInstance() throws Throwable {
        final Type<ArrayList<String>> stringList = Types.ArrayList.makeGenericType(Types.String);
        final Type<?> classType = Types.Class.makeGenericType(Type.makeExtendsWildcard(stringList));
        final Type<?> erasedClassType = Types.Class.getErasedType();
        final MethodInfo getClass = stringList.getMethod("getClass");

        assertNotNull(getClass);
        assertEquals(classType, getClass.getReturnType());

        final MethodInfo erasedGetClass = getClass.getErasedMethodDefinition();

        assertNotNull(erasedGetClass);
        assertEquals(erasedClassType, erasedGetClass.getReturnType());

        assertEquals(Types.Object, erasedGetClass.getDeclaringType());
        assertEquals(stringList, erasedGetClass.getReflectedType());
    }

    @Test
    public void testGetErasedMethodFromGenericTypeDefinition() throws Throwable {
        final Type<List> genericList = Types.List;
        final Type<?> erasedList = Types.List.getErasedType();
        final Type<Object[]> objectArray = Types.Object.makeArrayType();
        final MethodInfo toArray = genericList.getMethod("toArray", objectArray);

        assertNotNull(toArray);

        final MethodInfo erasedToArray = toArray.getErasedMethodDefinition();

        assertNotNull(erasedToArray);
        assertEquals(objectArray, erasedToArray.getReturnType());
        assertEquals(1, erasedToArray.getParameters().size());
        assertEquals(objectArray, erasedToArray.getParameters().get(0).getParameterType());

        assertEquals(erasedList, erasedToArray.getDeclaringType());
        assertEquals(genericList, erasedToArray.getReflectedType());
    }

    @Test
    public void testGetErasedMethodFromGenericTypeInstance() throws Throwable {
        final Type<List<String>> stringList = Types.List.makeGenericType(Types.String);
        final Type<?> erasedList = Types.List.getErasedType();
        final Type<Object[]> objectArray = Types.Object.makeArrayType();
        final MethodInfo toArray = stringList.getMethod("toArray", objectArray);

        assertNotNull(toArray);

        final MethodInfo erasedToArray = toArray.getErasedMethodDefinition();

        assertNotNull(erasedToArray);
        assertEquals(objectArray, erasedToArray.getReturnType());
        assertEquals(1, erasedToArray.getParameters().size());
        assertEquals(objectArray, erasedToArray.getParameters().get(0).getParameterType());

        assertEquals(erasedList, erasedToArray.getDeclaringType());
        assertEquals(stringList, erasedToArray.getReflectedType());
    }

    @Test
    public void testGetGenericMethodFromErasedType() throws Throwable {
        final Type<?> erasedList = Types.List.getErasedType();
        final Type<Object[]> objectArray = Types.Object.makeArrayType();
        final MethodInfo erasedToArray = erasedList.getMethod("toArray", objectArray);

        assertNotNull(erasedToArray);
        assertEquals(objectArray, erasedToArray.getReturnType());
        assertEquals(1, erasedToArray.getParameters().size());
        assertEquals(objectArray, erasedToArray.getParameters().get(0).getParameterType());

        assertEquals(erasedList, erasedToArray.getDeclaringType());
        assertEquals(erasedList, erasedToArray.getReflectedType());
    }

    private final static class ExtendedMap<K, V> extends HashMap<K, V> {}

    @Test
    public void testMethodReflectedType() throws Throwable {
        final Type<HashMap> gd = Types.HashMap;
        final Type<HashMap> gi = Types.HashMap.makeGenericType(Types.String, Types.Date);
        final Type<ExtendedMap> ge = Type.of(ExtendedMap.class).makeGenericType(Types.String, Types.Date);

        final MethodInfo gb = gd.getMethod("put", Types.Object, Types.Object);
        final MethodInfo gm = gi.getMethod("put", Types.String, Types.Date);
        final MethodInfo go = ge.getMethod("put", Types.String, Types.Date);

        assertSame(gd, gb.getReflectedType());
        assertSame(gi, gm.getReflectedType());
        assertSame(ge, go.getReflectedType());

        final MethodInfo egb = gb.getErasedMethodDefinition();
        final MethodInfo egm = gm.getErasedMethodDefinition();
        final MethodInfo ego = go.getErasedMethodDefinition();

        assertSame(gd, egb.getReflectedType());
        assertSame(gi, egm.getReflectedType());
        assertSame(ge, ego.getReflectedType());
    }

    @Test
    public void testMethodDeclaringType() throws Throwable {
        final Type<?> gr = Types.HashMap.getErasedType();
        final Type<HashMap> gd = Types.HashMap;
        final Type<HashMap> gi = Types.HashMap.makeGenericType(Types.String, Types.Date);
        final Type<ExtendedMap> ge = Type.of(ExtendedMap.class).makeGenericType(Types.String, Types.Date);

        final MethodInfo gb = gd.getMethod("put", Types.Object, Types.Object);
        final MethodInfo gm = gi.getMethod("put", Types.String, Types.Date);
        final MethodInfo go = ge.getMethod("put", Types.String, Types.Date);

        assertSame(gd, gb.getDeclaringType());
        assertSame(gi, gm.getDeclaringType());
        assertSame(gi, go.getDeclaringType());

        final MethodInfo egb = gb.getErasedMethodDefinition();
        final MethodInfo egm = gm.getErasedMethodDefinition();
        final MethodInfo ego = go.getErasedMethodDefinition();

        assertSame(gr, egb.getDeclaringType());
        assertSame(gr, egm.getDeclaringType());
        assertSame(gr, ego.getDeclaringType());
    }

    @Test
    public void testGetUnderlyingType() throws Throwable {
        final Type<?> gd = Types.HashMap;
        final Type<?> ge = Types.HashMap.getErasedType();
        final Type<?> gi = Types.HashMap.makeGenericType(Types.String, Types.Date);
        final Type<?> gda = gd.makeArrayType();
        final Type<?> gea = ge.makeArrayType();
        final Type<?> gia = gi.makeArrayType();

        final Type<?> ud = gd.getUnderlyingType();
        final Type<?> ue = ge.getUnderlyingType();
        final Type<?> ui = gi.getUnderlyingType();
        final Type<?> uda = gda.getUnderlyingType();
        final Type<?> uea = gea.getUnderlyingType();
        final Type<?> uia = gia.getUnderlyingType();

        assertSame(gd, ud);
        assertSame(gd, ue);
        assertSame(gd, ui);
        assertSame(gd, uda);
        assertSame(gd, uea);
        assertSame(gd, uia);
    }
}
