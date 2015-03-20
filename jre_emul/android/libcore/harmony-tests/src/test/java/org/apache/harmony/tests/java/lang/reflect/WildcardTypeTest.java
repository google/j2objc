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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Tests bounded type parameters declared on methods and bounded wildcards.
 */
public class WildcardTypeTest extends GenericReflectionTestsBase {
    @SuppressWarnings({"unchecked", "hiding"})
    static class BoundedWildcardsGenericMethods<T> {

        public <T extends BoundedWildcardsGenericMethods> void lowerBoundedParamNoReturn( BoundedWildcardsGenericMethods<? super T> param) {}

        public <T extends BoundedWildcardsGenericMethods> void upperBoundedParamNoReturn( BoundedWildcardsGenericMethods<? extends T> param) {}

        public <T extends BoundedWildcardsGenericMethods> T lowerBoundedParamReturn(BoundedWildcardsGenericMethods<? super T> param) { return (T) new Object(); }

        public <T extends BoundedWildcardsGenericMethods> T upperBoundedParamReturn(BoundedWildcardsGenericMethods<? extends T> param) { return (T) new Object();}
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends BoundedWildcardsGenericMethods> clazz = BoundedWildcardsGenericMethods.class;

    /**
     * Tests that there are is one Type Parameter on the Class itself.
     */
    public void testBoundedGenericMethods() {
        assertLenghtOne(clazz.getTypeParameters());
    }

    /**
     * Tests whether the type parameter is bounded by BoundedGenericMethods like:
     * <T extends BoundedGenericMethods>.
     * @param method the declaring method
     */
    private void checkBoundedTypeParameter(Method method) {
        TypeVariable<Method> typeParameter = getTypeParameter(method);
        assertEquals("T", typeParameter.getName());
        assertEquals(method, typeParameter.getGenericDeclaration());

        Type[] bounds = typeParameter.getBounds();
        assertLenghtOne(bounds);
        Type bound = bounds[0];
        assertEquals(BoundedWildcardsGenericMethods.class, bound);
    }

    private void checkLowerBoundedParameter(Method method) {
        Type genericParameterType = method.getGenericParameterTypes()[0];
        assertInstanceOf(ParameterizedType.class, genericParameterType);

        ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;

        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        assertLenghtOne(actualTypeArguments);
        assertInstanceOf(WildcardType.class, actualTypeArguments[0]);

        WildcardType wildcardType = (WildcardType) actualTypeArguments[0];

        Type[] lowerBounds = wildcardType.getLowerBounds();
        assertLenghtOne(lowerBounds);
        Type lowerBound = lowerBounds[0];
        assertEquals(getTypeParameter(method), lowerBound);

        Type[] upperBounds = wildcardType.getUpperBounds();
        assertEquals(Object.class, upperBounds[0]);
    }

    private void checkUpperBoundedParameter(Method method) {
        assertLenghtOne(method.getGenericParameterTypes());
        Type genericParameterType = method.getGenericParameterTypes()[0];
        assertInstanceOf(ParameterizedType.class, genericParameterType);

        ParameterizedType parameterizedType = (ParameterizedType) genericParameterType;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        assertLenghtOne(actualTypeArguments);
        assertInstanceOf(WildcardType.class, actualTypeArguments[0]);

        WildcardType wildcardType = (WildcardType) actualTypeArguments[0];
        assertLenghtZero(wildcardType.getLowerBounds());

        Type[] upperBounds = wildcardType.getUpperBounds();
        assertLenghtOne(upperBounds);
        Type upperBound = upperBounds[0];
        assertEquals(getTypeParameter(method), upperBound);
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

        assertEquals(BoundedWildcardsGenericMethods.class, bound);
    }

    public void testUpperBoundedParamNoReturn() throws Exception {
        Method method = clazz.getMethod("upperBoundedParamNoReturn", BoundedWildcardsGenericMethods.class);
        checkBoundedTypeParameter(method);
        checkUpperBoundedParameter(method);
    }

    public void testLowerBoundedParamReturn() throws Exception {
        Method method = clazz.getMethod("lowerBoundedParamReturn", BoundedWildcardsGenericMethods.class);
        checkBoundedTypeParameter(method);
        checkLowerBoundedParameter(method);
        checkReturnType(method);
    }

    public void testUpperBoundedParamReturn() throws Exception {
        Method method = clazz.getMethod("upperBoundedParamReturn", BoundedWildcardsGenericMethods.class);
        checkBoundedTypeParameter(method);
        checkUpperBoundedParameter(method);
        checkReturnType(method);
    }

    public void testLowerBoundedParamNoReturn() throws Exception {
        Method method = clazz.getMethod("lowerBoundedParamNoReturn", BoundedWildcardsGenericMethods.class);
        checkBoundedTypeParameter(method);
        assertLenghtOne(method.getGenericParameterTypes());
        checkLowerBoundedParameter(method);
    }

}
