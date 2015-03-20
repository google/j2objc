/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tests.java.lang.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * Tests generic reflection on arrays with generic or parameterized component types.
 */
public class GenericArrayTypeTest extends GenericReflectionTestsBase {

    static class A<T> {
        T[] array;
    }
    public void testGetGenericComponentType() throws Exception {
        @SuppressWarnings("unchecked")
        Class<? extends A> clazz = GenericArrayTypeTest.A.class;
        Field field = clazz.getDeclaredField("array");
        Type genericType = field.getGenericType();
        assertInstanceOf(GenericArrayType.class, genericType);
        Type componentType = ((GenericArrayType) genericType).getGenericComponentType();
        assertEquals(getTypeParameter(clazz), componentType);
        assertInstanceOf(TypeVariable.class, componentType);
        TypeVariable<?> componentTypeVariable = (TypeVariable<?>) componentType;
        assertEquals("T", componentTypeVariable.getName());
        assertEquals(clazz, componentTypeVariable.getGenericDeclaration());
    }

    static class B<T> {
        B<T>[] array;
    }
    public void testParameterizedComponentType() throws Exception {
        @SuppressWarnings("unchecked")
        Class<? extends B> clazz = GenericArrayTypeTest.B.class;
        Field field = clazz.getDeclaredField("array");
        Type genericType = field.getGenericType();

        assertInstanceOf(GenericArrayType.class, genericType);
        GenericArrayType arrayType = (GenericArrayType) genericType;
        Type componentType = arrayType.getGenericComponentType();
        assertInstanceOf(ParameterizedType.class, componentType);
        ParameterizedType parameteriezdType = (ParameterizedType) componentType;
        assertEquals(clazz, parameteriezdType.getRawType());
        assertEquals(clazz.getTypeParameters()[0], parameteriezdType.getActualTypeArguments()[0]);
    }
}
