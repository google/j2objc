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
 * Tests bounded type parameters declared on methods.
 */
public class BoundedGenericMethodsTests extends GenericReflectionTestsBase {
    @SuppressWarnings("unchecked")
    static class BoundedGenericMethods<S> {

        public <T extends BoundedGenericMethods> void noParamNoReturn() {}
        public <T extends BoundedGenericMethods> void paramNoReturn(T param) {}

        public <T extends BoundedGenericMethods> T noParamReturn() {
            return (T) new Object();
        }
        public <T extends BoundedGenericMethods> T paramReturn(T t) {
            return t;
        }
    }
    @SuppressWarnings("unchecked")
    private static Class<? extends BoundedGenericMethods> clazz = BoundedGenericMethodsTests.BoundedGenericMethods.class;

    /**
     * Tests whether the type parameter is upper bounded by BoundedGenericMethods.
     * <T extends BoundedGenericMethods>.
     *
     * @param method
     *            the declaring method
     */
    private void checkBoundedTypeParameter(Method method) {
        TypeVariable<Method> typeParameter = getTypeParameter(method);
        assertEquals("T", typeParameter.getName());
        assertEquals(method, typeParameter.getGenericDeclaration());

        Type[] bounds = typeParameter.getBounds();
        assertLenghtOne(bounds);
        Type bound = bounds[0];
        assertEquals(BoundedGenericMethods.class, bound);
    }

    /**
     * Tests whether the specified method declares a parameter with the type of
     * the type parameter.
     *
     * @param method
     *            the declaring method
     */
    private void parameterType(Method method) {
        TypeVariable<Method> typeParameter = getTypeParameter(method);
        assertLenghtOne(method.getGenericParameterTypes());
        Type genericParameterType = method.getGenericParameterTypes()[0];
        assertEquals(typeParameter, genericParameterType);
        assertTrue(genericParameterType instanceof TypeVariable);
        TypeVariable<?> typeVariable = (TypeVariable<?>) genericParameterType;
        assertEquals(method, typeVariable.getGenericDeclaration());

        Type[] paramBounds = typeVariable.getBounds();
        assertLenghtOne(paramBounds);
        Type paramBound = paramBounds[0];
        assertEquals(BoundedGenericMethods.class, paramBound);
    }

    @SuppressWarnings("unchecked")
    private void checkReturnType(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        assertEquals(getTypeParameter(method), genericReturnType);
        assertTrue(genericReturnType instanceof TypeVariable);

        TypeVariable<Method> returnTypeVariable = (TypeVariable<Method>) genericReturnType;
        assertEquals(method, returnTypeVariable.getGenericDeclaration());

        Type[] bounds = returnTypeVariable.getBounds();
        assertLenghtOne(bounds);
        Type bound = bounds[0];

        assertEquals(BoundedGenericMethods.class, bound);
    }



    /**
     * Tests that there are is one Type Parameter on the Class itself.
     */
    public void testBoundedGenericMethods() {
        assertLenghtOne(clazz.getTypeParameters());
    }
    public void testNoParamNoReturn() throws SecurityException, NoSuchMethodException {
        Method method = clazz.getMethod("noParamNoReturn");
        checkBoundedTypeParameter(method);
    }
    public void testUnboundedParamNoReturn() throws SecurityException, NoSuchMethodException {
        Method method = clazz.getMethod("paramNoReturn", BoundedGenericMethods.class);
        checkBoundedTypeParameter(method);
        parameterType(method);
    }
    public void testNoParamReturn() throws SecurityException, NoSuchMethodException {
        Method method = clazz.getMethod("noParamReturn");
        checkBoundedTypeParameter(method);
        assertLenghtZero(method.getGenericParameterTypes());
        checkReturnType(method);
    }
    public void testUnboundedParamReturn() throws SecurityException, NoSuchMethodException {
        Method method = clazz.getMethod("paramReturn", BoundedGenericMethods.class);
        checkBoundedTypeParameter(method);
        parameterType(method);
        checkReturnType(method);
    }
}
