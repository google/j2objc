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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * Tests type variables and their properties.
 */
public class TypeVariableTest extends GenericReflectionTestsBase {

    static class A<T>{}
    public void testSimpleTypeVariableOnClass(){
        Class<? extends A> clazz = A.class;
        TypeVariable[] typeParameters = clazz.getTypeParameters();
        assertLenghtOne(typeParameters);
        TypeVariable<Class> typeVariable = typeParameters[0];
        assertEquals(clazz, typeVariable.getGenericDeclaration());
        assertEquals("T", typeVariable.getName());
        Type[] bounds = typeVariable.getBounds();
        assertLenghtOne(bounds);
        assertEquals(Object.class, bounds[0]);
    }

    static class B{
        <T> void b(){};
    }
    public void testSimpleTypeVariableOnMethod() throws Exception{
        Class<? extends B> clazz = B.class;
        Method method = clazz.getDeclaredMethod("b");
        TypeVariable<Method>[] typeParameters = method.getTypeParameters();
        assertLenghtOne(typeParameters);
        TypeVariable<Method> typeVariable = typeParameters[0];
        assertEquals(method, typeVariable.getGenericDeclaration());
        assertEquals("T", typeVariable.getName());
        Type[] bounds = typeVariable.getBounds();
        assertLenghtOne(bounds);
        assertEquals(Object.class, bounds[0]);
    }

    static class C {
        <T>C(){}
    }
    public void testSimpleTypeVariableOnConstructor() throws Exception{
        Class<? extends C> clazz = C.class;
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        TypeVariable<?>[] typeParameters = constructor.getTypeParameters();
        assertLenghtOne(typeParameters);
        TypeVariable<?> typeVariable = typeParameters[0];
        assertEquals(constructor, typeVariable.getGenericDeclaration());
        assertEquals("T", typeVariable.getName());
        Type[] bounds = typeVariable.getBounds();
        assertLenghtOne(bounds);
        assertEquals(Object.class, bounds[0]);
    }

    static class D<Q,R,S>{}
    public void testMultipleTypeVariablesOnClass() throws Exception {
        Class<? extends D> clazz = D.class;
        TypeVariable<?>[] typeParameters = clazz.getTypeParameters();
        assertEquals(3, typeParameters.length);
        assertEquals("Q", typeParameters[0].getName());
        assertEquals(clazz, typeParameters[0].getGenericDeclaration());

        assertEquals("R", typeParameters[1].getName());
        assertEquals(clazz, typeParameters[1].getGenericDeclaration());

        assertEquals("S", typeParameters[2].getName());
        assertEquals(clazz, typeParameters[2].getGenericDeclaration());

    }

    static class E {
        <Q,R,S> void e(){}
    }
    public void testMultipleTypeVariablesOnMethod() throws Exception {
        Class<? extends E> clazz = E.class;
        Method method = clazz.getDeclaredMethod("e");

        TypeVariable<?>[] typeParameters = method.getTypeParameters();
        assertEquals(3, typeParameters.length);
        assertEquals("Q", typeParameters[0].getName());
        assertEquals(method, typeParameters[0].getGenericDeclaration());

        assertEquals("R", typeParameters[1].getName());
        assertEquals(method, typeParameters[1].getGenericDeclaration());

        assertEquals("S", typeParameters[2].getName());
        assertEquals(method, typeParameters[2].getGenericDeclaration());
    }

    static class F {
        <Q,R,S> F(){}
    }
    public void testMultipleTypeVariablesOnConstructor() throws Exception {
        Class<? extends F> clazz = F.class;
        Constructor<?> constructor = clazz.getDeclaredConstructor();

        TypeVariable<?>[] typeParameters = constructor.getTypeParameters();
        assertEquals(3, typeParameters.length);
        assertEquals("Q", typeParameters[0].getName());
        assertEquals(constructor, typeParameters[0].getGenericDeclaration());

        assertEquals("R", typeParameters[1].getName());
        assertEquals(constructor, typeParameters[1].getGenericDeclaration());

        assertEquals("S", typeParameters[2].getName());
        assertEquals(constructor, typeParameters[2].getGenericDeclaration());
    }

    static class G <T extends Number>{}

    public void testSingleBound() throws Exception {
        Class<? extends G> clazz = G.class;
        TypeVariable[] typeParameters = clazz.getTypeParameters();
        TypeVariable<Class> typeVariable = typeParameters[0];
        Type[] bounds = typeVariable.getBounds();
        assertLenghtOne(bounds);
        assertEquals(Number.class, bounds[0]);
    }

    static class H <T extends Number & Serializable >{}
    public void testMultipleBound() throws Exception {
        Class<? extends H> clazz = H.class;
        TypeVariable[] typeParameters = clazz.getTypeParameters();
        TypeVariable<Class> typeVariable = typeParameters[0];
        Type[] bounds = typeVariable.getBounds();
        assertEquals(2, bounds.length);
        assertEquals(Number.class, bounds[0]);
        assertEquals(Serializable.class, bounds[1]);
    }
}
