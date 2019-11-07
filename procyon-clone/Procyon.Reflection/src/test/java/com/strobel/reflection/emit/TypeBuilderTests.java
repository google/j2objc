/*
 * TypeBuilderTests.java
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

package com.strobel.reflection.emit;

import com.strobel.core.ReadOnlyList;
import com.strobel.reflection.BindingFlags;
import com.strobel.reflection.MethodInfo;
import com.strobel.reflection.PrimitiveTypes;
import com.strobel.reflection.Type;
import com.strobel.reflection.TypeList;
import com.strobel.reflection.Types;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;

import static org.junit.Assert.*;

/**
 * @author Mike Strobel
 */
public final class TypeBuilderTests {
    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation {
        int value();
    }

    @Test
    public void testGenericTypeBuilder() throws Throwable {
        final TypeBuilder<?> typeBuilder = new TypeBuilder<>(
            TypeBuilderTests.class.getPackage().getName() + ".TestGenericType",
            Modifier.PUBLIC | Modifier.FINAL,
            Types.Object,
            TypeList.empty()
        );

        typeBuilder.defineDefaultConstructor();

        final GenericParameterBuilder<?>[] gp = typeBuilder.defineGenericParameters("T");

        final MethodBuilder method = typeBuilder.defineMethod(
            "test",
            Modifier.PUBLIC | Modifier.FINAL,
            gp[0],
            Type.list(gp[0])
        );

        final Type<TestAnnotation> annotationType = Type.of(TestAnnotation.class);

        final AnnotationBuilder<TestAnnotation> annotation = AnnotationBuilder.create(
            annotationType,
            annotationType.getMethods(BindingFlags.AllDeclared),
            new ReadOnlyList<Object>(42)
        );

        typeBuilder.addCustomAnnotation(annotation);

        final CodeGenerator code = method.getCodeGenerator();

        code.emitLoadArgument(0);
        code.emitReturn(gp[0]);

        final Type<?> createdType = typeBuilder.createType();
        final Type<?> boundType = createdType.makeGenericType(Types.String);
        final MethodInfo boundMethod = boundType.getMethod("test");
        final Object instance = createdType.newInstance();
        final String parameter = "test";

        final Object result = boundMethod.invoke(instance, parameter);

        assertSame(parameter, result);

        final TestAnnotation bakedTypeAnnotation = annotation.getAnnotation();
        final TestAnnotation createdTypeAnnotation = createdType.getErasedClass().getAnnotation(TestAnnotation.class);

        assertNotNull(createdTypeAnnotation);
        assertEquals(createdTypeAnnotation.value(), 42);
        assertEquals(bakedTypeAnnotation, createdTypeAnnotation);
        assertEquals(createdTypeAnnotation, bakedTypeAnnotation);
    }

    @Test
    public void testOverrideGenericMethodNoBridge() throws Throwable {
        final MethodInfo baseToArray = Types.ArrayList.getMethod("toArray", Types.Object.makeArrayType());

        final TypeBuilder<ArrayList> mockList = new TypeBuilder<>(
            TypeBuilderTests.class.getPackage().getName() + ".TestOverrideGenericMethodNoBridge",
            Modifier.PUBLIC | Modifier.FINAL,
            Types.Object,
            TypeList.empty()
        );

        final GenericParameterBuilder<?>[] gps = mockList.defineGenericParameters("E");

        final MethodBuilder toArray = mockList.defineMethod(
            baseToArray.getName(),
            Modifier.PUBLIC,
            Types.Object.makeArrayType()
        );

        final GenericParameterBuilder<?>[] mgps = toArray.defineGenericParameters("T");

        final GenericParameterBuilder<?> e = gps[0];
        final GenericParameterBuilder<?> t = mgps[0];

        mockList.setBaseType(Types.ArrayList.makeGenericType(e));

        toArray.setReturnType(t.makeArrayType());
        toArray.setParameters(Type.list(t.makeArrayType()));
        toArray.defineParameter(0, "a");

        final CodeGenerator g = toArray.getCodeGenerator();

        g.emitThis();
        g.emitLoadArgument(0);
        g.call(OpCode.INVOKESPECIAL, baseToArray);
        g.dup();
        g.emitConstant(2);
        g.emitConstant("wat");
        g.emitStoreElement(Types.String);
        g.emitReturn(t.makeArrayType());

        final Type<ArrayList> createdType = mockList.createType();
        final Type<ArrayList<String>> mockStringList = createdType.makeGenericType(Types.String);
        final ArrayList<String> mockInstance = mockStringList.newInstance();

        mockInstance.add("foo");
        mockInstance.add("bar");
        mockInstance.add("baz");

        final String[] array = new String[1];
        final String[] result = mockInstance.toArray(array);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("foo", result[0]);
        assertEquals("bar", result[1]);
        assertEquals("wat", result[2]);
    }

    @Test
    public void testOverrideGenericMethodWithBridge() throws Throwable {
        final MethodInfo baseToArray = Types.ArrayList.getMethod("toArray", Types.Object.makeArrayType());

        final TypeBuilder<ArrayList<String>> mockList = new TypeBuilder<>(
            TypeBuilderTests.class.getPackage().getName() + ".TestOverrideGenericMethodWithBridge",
            Modifier.PUBLIC | Modifier.FINAL,
            Types.ArrayList.makeGenericType(Types.String),
            TypeList.empty()
        );

        final MethodBuilder toArray = mockList.defineMethod(
            baseToArray.getName(),
            Modifier.PUBLIC,
            Types.String.makeArrayType(),
            Type.list(Types.String.makeArrayType())
        );

        toArray.defineParameter(0, "a");

        final CodeGenerator g = toArray.getCodeGenerator();

        g.emitThis();
        g.emitLoadArgument(0);
        g.call(OpCode.INVOKESPECIAL, baseToArray);
        g.dup();
        g.emitConstant(2);
        g.emitConstant("wat");
        g.emitStoreElement(Types.String);
        g.emit(OpCode.CHECKCAST, Types.String.makeArrayType());
        g.emitReturn(Types.String.makeArrayType());

        mockList.defineMethodOverride(toArray, baseToArray);

        final Type<ArrayList<String>> createdType = mockList.createType();
        final ArrayList<String> mockInstance = createdType.newInstance();

        mockInstance.add("foo");
        mockInstance.add("bar");
        mockInstance.add("baz");

        final String[] array = new String[1];
        final String[] result = mockInstance.toArray(array);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("foo", result[0]);
        assertEquals("bar", result[1]);
        assertEquals("wat", result[2]);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIsBridgeMethodNecessaryWhenImplementingParameterizedType() throws Throwable {
        final Type<Comparator> openComparator = Type.of(Comparator.class);
        final Type<Comparator> boundComparator = openComparator.makeGenericType(Types.String);

        final TypeBuilder<Comparator<String>> builder = new TypeBuilder<>(
            TypeBuilderTests.class.getPackage().getName() + ".TestStringComparator1",
            Modifier.PUBLIC | Modifier.FINAL,
            Types.Object,
            Type.list(boundComparator)
        );

        final MethodInfo boundCompare = boundComparator.getMethod("compare", Types.String, Types.String);

        assertNotNull(boundCompare);

        final MethodBuilder compareBuilder = builder.defineMethod(
            boundCompare.getName(),
            boundCompare.getModifiers() & ~Modifier.ABSTRACT,
            boundCompare.getReturnType(),
            boundCompare.getParameters().getParameterTypes(),
            boundCompare.getThrownTypes()
        );

        builder.defineMethodOverride(compareBuilder, boundCompare);

        final CodeGenerator g = compareBuilder.getCodeGenerator();

        g.emitInteger(0);
        g.emitReturn(PrimitiveTypes.Integer);

        assertTrue(TypeBuilder.isBridgeMethodNeeded(boundCompare, compareBuilder));

        assertEquals(0, ((Comparator) builder.createType().newInstance()).compare(null, null));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIsBridgeMethodNecessaryWhenExtendingGenericType() throws Throwable {
        final Type<Comparator> openComparator = Type.of(Comparator.class);

        final TypeBuilder<Comparator<String>> builder = new TypeBuilder<>(
            TypeBuilderTests.class.getPackage().getName() + ".TestStringComparator2",
            Modifier.PUBLIC | Modifier.FINAL
        );

        assertTrue(openComparator.isGenericTypeDefinition());
        assertTrue(openComparator.getGenericTypeParameters().size() == 1);

        final GenericParameterBuilder<?>[] typeVariables = builder.defineGenericParameters("T_i");

        final Type<Comparator> boundComparator = openComparator.makeGenericType(typeVariables);
        final MethodInfo boundCompare = boundComparator.getMethod("compare", Types.String, Types.String);

        assertNotNull(boundCompare);

        builder.setInterfaces(Type.list(boundComparator));

        final MethodBuilder compareBuilder = builder.defineMethod(
            boundCompare.getName(),
            boundCompare.getModifiers() & ~Modifier.ABSTRACT,
            boundCompare.getReturnType(),
            boundCompare.getParameters().getParameterTypes(),
            boundCompare.getThrownTypes()
        );

        builder.defineMethodOverride(compareBuilder, boundCompare);

        final CodeGenerator g = compareBuilder.getCodeGenerator();

        g.emitInteger(0);
        g.emitReturn(PrimitiveTypes.Integer);

        assertFalse(TypeBuilder.isBridgeMethodNeeded(boundCompare, compareBuilder));
        assertEquals(0, ((Comparator) builder.createType().newInstance()).compare(null, null));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIsBridgeMethodNecessaryWhenExtendingGenericTypeWithNewConstraint() throws Throwable {
        final Type<Comparator> openComparator = Type.of(Comparator.class);

        final TypeBuilder<Comparator<String>> builder = new TypeBuilder<>(
            TypeBuilderTests.class.getPackage().getName() + ".TestStringComparator3",
            Modifier.PUBLIC | Modifier.FINAL
        );

        assertTrue(openComparator.isGenericTypeDefinition());
        assertTrue(openComparator.getGenericTypeParameters().size() == 1);

        final GenericParameterBuilder<?>[] typeVariables = builder.defineGenericParameters("T_i");

        typeVariables[0].setBaseTypeConstraint(Types.String);

        final Type<Comparator> boundComparator = openComparator.makeGenericType(typeVariables);
        final MethodInfo boundCompare = boundComparator.getMethod("compare", Types.String, Types.String);

        assertNotNull(boundCompare);

        builder.setInterfaces(Type.list(boundComparator));

        final MethodBuilder compareBuilder = builder.defineMethod(
            boundCompare.getName(),
            boundCompare.getModifiers() & ~Modifier.ABSTRACT,
            boundCompare.getReturnType(),
            boundCompare.getParameters().getParameterTypes(),
            boundCompare.getThrownTypes()
        );

        builder.defineMethodOverride(compareBuilder, boundCompare);

        final CodeGenerator g = compareBuilder.getCodeGenerator();

        g.emitInteger(0);
        g.emitReturn(PrimitiveTypes.Integer);

        assertTrue(TypeBuilder.isBridgeMethodNeeded(boundCompare, compareBuilder));
        assertEquals(0, ((Comparator) builder.createType().newInstance()).compare(null, null));
    }

    @Test
    public void testTypeBuilderArrayTypes() throws Throwable {
        final TypeBuilder<?> type = new TypeBuilder<>(
            TypeBuilderTests.class.getPackage().getName() + ".TestTypeBuilderArrayTypes",
            Modifier.PUBLIC | Modifier.FINAL
        );

        final GenericParameterBuilder<?>[] typeVariables = type.defineGenericParameters("T");
        final GenericParameterBuilder<?> typeVariable = typeVariables[0];

        final Type<?> arrayType = type.makeArrayType();
        final Type<?> typeVariableArray = typeVariable.makeArrayType();

        typeVariable.setBaseTypeConstraint(Types.String);

        final MethodBuilder method = type.defineMethod(
            "makeArray()",
            Modifier.PUBLIC | Modifier.FINAL,
            typeVariableArray
        );

        final CodeGenerator g = method.getCodeGenerator();

        g.emitInteger(0);
        g.emitNewArray(typeVariableArray);
        g.emitReturn(typeVariableArray);

        assertTrue(arrayType.isArray());
        assertEquals(type, arrayType.getElementType());

        assertTrue(typeVariableArray.isArray());
        assertEquals(typeVariable, typeVariableArray.getElementType());
        assertTrue(typeVariableArray.isAssignableFrom(Types.String.makeArrayType()));

        final Type<?> createdType = type.createType();
        final Type<?> createdArrayType = createdType.makeArrayType();
        final Type<?> createdTypeVariable = createdType.getGenericTypeParameters().get(0);
        final Type<?> createdTypeVariableArray = createdTypeVariable.makeArrayType();

        assertTrue(type.isEquivalentTo(createdType));
        assertTrue(createdType.isEquivalentTo(type));
        assertTrue(arrayType.isEquivalentTo(createdArrayType));
        assertTrue(createdArrayType.isEquivalentTo(arrayType));
        assertTrue(typeVariable.isEquivalentTo(createdTypeVariable));
        assertTrue(createdTypeVariable.isEquivalentTo(typeVariable));
        assertTrue(typeVariableArray.isEquivalentTo(createdTypeVariableArray));
        assertTrue(createdTypeVariableArray.isEquivalentTo(typeVariableArray));
    }

    @Test
    public void testMethodBuilderArrayTypes() throws Throwable {
        final TypeBuilder<?> type = new TypeBuilder<>(
            TypeBuilderTests.class.getPackage().getName() + ".TestMethodBuilderArrayTypes",
            Modifier.PUBLIC | Modifier.FINAL
        );

        final MethodBuilder method = type.defineMethod(
            "makeArray",
            Modifier.PUBLIC | Modifier.FINAL
        );

        final GenericParameterBuilder<?>[] typeVariables = method.defineGenericParameters("T");
        final GenericParameterBuilder<?> typeVariable = typeVariables[0];
        final Type<?> typeVariableArray = typeVariable.makeArrayType();

        method.setReturnType(typeVariableArray);

        final CodeGenerator g = method.getCodeGenerator();

        g.emitInteger(0);
        g.emitNewArray(typeVariableArray);
        g.emitReturn(typeVariableArray);

        assertTrue(typeVariableArray.isArray());
        assertEquals(typeVariable, typeVariableArray.getElementType());
        assertTrue(typeVariableArray.isAssignableFrom(Types.String.makeArrayType()));

        final Type<?> createdType = type.createType();
        final MethodInfo createdMethod = createdType.getMethod("makeArray");

        final Type<?> createdTypeVariable = createdMethod.getGenericMethodParameters().get(0);
        final Type<?> createdTypeVariableArray = createdTypeVariable.makeArrayType();

        assertTrue(typeVariable.isEquivalentTo(createdTypeVariable));
        assertTrue(createdTypeVariable.isEquivalentTo(typeVariable));

        assertTrue(typeVariableArray.isEquivalentTo(createdTypeVariableArray));
        assertTrue(createdTypeVariableArray.isEquivalentTo(typeVariableArray));
    }
}
