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

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;

import junit.framework.TestCase;

public class GenericReflectionTestsBase extends TestCase{

    /**
     * Returns the type parameter of the declaring method.
     *
     * @param method
     *            the declaring method
     * @return the type parameter of the method
     */
    public TypeVariable<Method> getTypeParameter(Method method) {
        TypeVariable<Method>[] typeParameters = method.getTypeParameters();
        assertLenghtOne(typeParameters);
        TypeVariable<Method> typeParameter = typeParameters[0];
        return typeParameter;
    }

    /**
     * Returns the type parameter of the declaring class.
     *
     * @param method
     *            the declaring method.
     * @return the type parameter of the method.
     */
    @SuppressWarnings("unchecked")
    public TypeVariable<Class> getTypeParameter(Class<?> clazz) {
        TypeVariable[] typeParameters = clazz.getTypeParameters();
        assertLenghtOne(typeParameters);
        TypeVariable<Class> typeVariable = typeParameters[0];
        assertEquals(clazz, typeVariable.getGenericDeclaration());
        assertEquals("T", typeVariable.getName());
        return typeVariable;
    }

    public static void assertLenghtOne(Object[] array) {
        TestCase.assertEquals("Array does NOT contain exactly one element.", 1, array.length);
    }

    public static void assertLenghtZero(Object[] array) {
        TestCase.assertEquals("Array has more than zero elements.", 0, array.length);
    }

    public static void assertInstanceOf(Class<?> expectedClass, Object actual) {
        TestCase.assertTrue(actual.getClass().getName() + " is not instance of :" + expectedClass.getName(), expectedClass
                .isInstance(actual));
    }

    public static void assertNotEquals(Object expected, Object actual) {
        TestCase.assertFalse(actual.toString() + " has not to be equal to " + expected.toString(), expected.equals(actual));
    }

}
