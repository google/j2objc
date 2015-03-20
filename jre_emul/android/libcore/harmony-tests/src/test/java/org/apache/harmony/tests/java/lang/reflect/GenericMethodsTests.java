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
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;


/**
 * Tests unbounded type parameters declared on methods.
 */
public class GenericMethodsTests extends GenericReflectionTestsBase{

    static class GenericMethods {

        public <T> void noParamNoReturn() {}

        public <T> void paramNoReturn(T param) {}

        @SuppressWarnings("unchecked")
        public <T> T noParamReturn() { return (T) new Object(); }

        public <T> T paramReturn(T param) {return param;}
    }

    private static Class<? extends GenericMethods> clazz = GenericMethodsTests.GenericMethods.class;

    /**
     * Tests that there are no Type Parameters on the Class itself.
     */
    public void testGenericMethods() {
        assertLenghtZero(clazz.getTypeParameters());
    }

    /**
     * Tests whether the specified method declares a type parameter T.
     * @param method the method
     */
    private void checkTypeParameter(Method method) {
        TypeVariable<Method> typeParameter = getTypeParameter(method);
        assertEquals("T", typeParameter.getName());
        assertEquals(method, typeParameter.getGenericDeclaration());
    }

    /**
     * Tests whether the specified method declares a parameter with the
     * type of the type parameter.
     * @param method the method
     */
    private void checkParameterType(Method method) {
        TypeVariable<Method> typeParameter = getTypeParameter(method);
        assertLenghtOne(method.getGenericParameterTypes());
        Type genericParameterType = method.getGenericParameterTypes()[0];
        assertEquals(typeParameter, genericParameterType);
        assertInstanceOf(TypeVariable.class, genericParameterType);
        assertEquals(method, ((TypeVariable<?>) genericParameterType).getGenericDeclaration());
    }

    /**
     * Tests whether the type of the return type is the declared type parameter.
     * @param method the declaring method
     */
    private void checkReturnType(Method method) {
        TypeVariable<Method> typeParameter = getTypeParameter(method);
        Type genericReturnType = method.getGenericReturnType();
        assertEquals(typeParameter, genericReturnType);
        assertInstanceOf(TypeVariable.class, genericReturnType);
        assertEquals(method, ((TypeVariable<?>) genericReturnType).getGenericDeclaration());
    }
    public void testNoParamNoReturn() throws Exception {
        Method method = clazz.getMethod("noParamNoReturn");
        checkTypeParameter(method);
    }

    public void testParamNoReturn() throws Exception {
        Method method = clazz.getMethod("paramNoReturn", Object.class);
        checkTypeParameter(method);
        checkParameterType(method);
    }

    public void testNoParamReturn() throws Exception {
        Method method = clazz.getMethod("noParamReturn");
        checkTypeParameter(method);
        assertLenghtZero(method.getGenericParameterTypes());
        checkReturnType(method);
    }
    public void testParamReturn() throws Exception {
        Method method = clazz.getMethod("paramReturn", Object.class);
        checkTypeParameter(method);
        checkParameterType(method);
        checkReturnType(method);
    }
    public void testIndependencyOfMethodTypeParameters() throws Exception {
        Method method0 = clazz.getMethod("paramNoReturn", Object.class);
        TypeVariable<Method> typeParameter0 = method0.getTypeParameters()[0];

        Method method1 = clazz.getMethod("noParamNoReturn");
        TypeVariable<Method> typeParameter1 = method1.getTypeParameters()[0];

        //Generic method type parameters NAMES are equal
        assertEquals(typeParameter0.getName(), typeParameter1.getName());
        //Generic method type PARAMETERS are not equal
        assertNotEquals(typeParameter0, typeParameter1);
    }
}
