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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Tests parameterized types and their properties.
 */
public class ParameterizedTypeTest extends GenericReflectionTestsBase {

    static class A<T>{}
    static class B extends A<String>{}

    public void testStringParameterizedSuperClass() {
        Class<? extends B> clazz = B.class;
        Type genericSuperclass = clazz.getGenericSuperclass();
        assertInstanceOf(ParameterizedType.class, genericSuperclass);
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        assertEquals(ParameterizedTypeTest.class, parameterizedType.getOwnerType());
        assertEquals(A.class, parameterizedType.getRawType());

        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        assertLenghtOne(actualTypeArguments);
        assertEquals(String.class, actualTypeArguments[0]);
    }

    static class C<T>{}
    static class D<T> extends C<T>{}

    public void testTypeParameterizedSuperClass() {
        Class<? extends D> clazz = D.class;
        Type genericSuperclass = clazz.getGenericSuperclass();
        assertInstanceOf(ParameterizedType.class, genericSuperclass);
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        assertEquals(ParameterizedTypeTest.class, parameterizedType.getOwnerType());
        assertEquals(C.class, parameterizedType.getRawType());

        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        assertLenghtOne(actualTypeArguments);
        assertEquals(getTypeParameter(D.class), actualTypeArguments[0]);
    }

    static class E<T>{}
    static class F<T>{
        E<T> e;
    }

    public void testParameterizedMemeber() throws Exception{
        Class<? extends F> clazz = F.class;
        Field field = clazz.getDeclaredField("e");
        assertInstanceOf(ParameterizedType.class, field.getGenericType());
        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
        assertEquals(ParameterizedTypeTest.class, parameterizedType.getOwnerType());
        assertEquals(E.class, parameterizedType.getRawType());

        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        assertLenghtOne(actualTypeArguments);
        assertEquals(getTypeParameter(clazz), actualTypeArguments[0]);
    }
}
