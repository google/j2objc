/*
 * VerifierTests.java
 *
 * Copyright (c) 2015 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.reflection.emit;

import com.strobel.core.ExceptionUtilities;
import com.strobel.core.StringUtilities;
import com.strobel.reflection.Type;
import com.strobel.reflection.TypeList;
import com.strobel.reflection.Types;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Modifier;

import static org.junit.Assert.fail;

public class VerifierTests {
    // <editor-fold defaultstate="collapsed" desc="Helpers">

    static abstract class SuperClass<T> {

    }

    interface SuperInterface<T> {

    }

    static void assertVerificationException(final String message, final Runnable action) {
        try {
            action.run();
        }
        catch (final Throwable t) {
            String m = t.getMessage();

            final int firstBreak = m.indexOf('\n');

            if (firstBreak >= 0) {
                m = m.substring(0, firstBreak);
            }

            if (t instanceof VerificationException &&
                StringUtilities.equals(message, m)) {

                System.out.printf(">> %s: %s%n", t.getClass().getSimpleName(), ExceptionUtilities.getMessage(t));
                return;
            }

            fail(
                "VerificationException expected, but got " + t.getClass().getSimpleName() + "!\n" +
                "    Expected Message: " + message + "\n" +
                "      Actual Message: " + m + "\n" +
                ExceptionUtilities.getStackTraceString(t)
            );
        }

        fail(
            "VerificationException expected!\n" +
            "    Expected Message: " + message
        );
    }

    static void assertVerificationException(final String message, final TypeBuilder<?> typeBuilder) {
        try {
            Verifier.verify(typeBuilder);
        }
        catch (final Throwable t) {
            String m = t.getMessage();

            final int firstBreak = m.indexOf('\n');

            if (firstBreak >= 0) {
                m = m.substring(0, firstBreak);
            }

            if (t instanceof VerificationException &&
                StringUtilities.equals(message, m)) {

                System.out.printf(">> %s: %s%n", t.getClass().getSimpleName(), ExceptionUtilities.getMessage(t));
                return;
            }

            fail(
                "VerificationException expected, but got " + t.getClass().getSimpleName() + "!\n" +
                "    Expected Message: " + message + "\n" +
                "      Actual Message: " + m + "\n" +
                ExceptionUtilities.getStackTraceString(t)
            );
        }

        fail(
            "VerificationException expected!\n" +
            "    Expected Message: " + message
        );
    }

    static void assertVerificationException(final Class<? extends Throwable> exceptionType, final String message, final Runnable action) {
        try {
            action.run();
        }
        catch (final Throwable t) {
            if (exceptionType.isInstance(t) &&
                StringUtilities.equals(message, t.getMessage())) {

                return;
            }
        }

        fail("Expected " + exceptionType.getSimpleName() + ".");
    }

    static TypeBuilder<?> buildType() {
        final String callerName = new Throwable().getStackTrace()[1].getMethodName();
        final String testName = Character.toUpperCase(callerName.charAt(0)) + callerName.substring(1);

        final TypeBuilder<?> t = new TypeBuilder<>(
            VerifierTests.class.getPackage().getName() + "." + testName,
            Modifier.PUBLIC | Modifier.FINAL
        );

        final MethodBuilder m = t.defineMethod(
            "g",
            Modifier.PUBLIC | Modifier.ABSTRACT
        );

        final GenericParameterBuilder<?>[] methodTypeVariables = m.defineGenericParameters("X", "Y");

        methodTypeVariables[1].setBaseTypeConstraint(methodTypeVariables[0]);

        m.setReturnType(methodTypeVariables[0]);
        m.setSignature(methodTypeVariables[0], Type.list(methodTypeVariables[1]));
        m.defineParameter(0, "p");

        return t;
    }

    static TypeList getOutOfScopeTypeVariables(final TypeBuilder<?> site) {
        return Type.list(
            Types.Map.getGenericTypeParameters().get(0),
            Types.Map.getNestedType("Entry").getGenericTypeParameters().get(1),
            site.methodBuilders.get(0).getGenericMethodParameters().get(0)
        );
    }

    // </editor-fold>

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("com.strobel.reflection.emit.TypeBuilder.VerifyGeneratedClasses", "true");
        System.setProperty("com.strobel.reflection.emit.Verifier.VerifyLocalVariableTypes", "true");
    }

    @Test
    public void testSuperClassContainsOutOfScopeTypeVariable() throws Throwable {
        final TypeBuilder<?> t = buildType();

        for (final Type<?> v : getOutOfScopeTypeVariables(t)) {
            t.setBaseType(Type.of(SuperClass.class).makeGenericType(v));

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, t),
                t
            );

            t.setBaseType(Type.of(SuperClass.class).makeGenericType(Type.makeExtendsWildcard(v)));

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, t),
                t
            );

            t.setBaseType(Type.of(SuperClass.class).makeGenericType(Type.makeSuperWildcard(v)));

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, t),
                t
            );
        }
    }

    @Test
    public void testSuperInterfaceContainsOutOfScopeTypeVariable() throws Throwable {
        final TypeBuilder<?> t = buildType();

        for (final Type<?> v : getOutOfScopeTypeVariables(t)) {
            t.setInterfaces(Type.list(Type.of(SuperInterface.class).makeGenericType(v)));

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, t),
                t
            );

            t.setInterfaces(Type.list(Type.of(SuperInterface.class).makeGenericType(Type.makeExtendsWildcard(v))));

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, t),
                t
            );

            t.setInterfaces(Type.list(Type.of(SuperInterface.class).makeGenericType(Type.makeSuperWildcard(v))));

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, t),
                t
            );
        }
    }

    @Test
    public void testFieldTypeContainsOutOfScopeTypeVariable() throws Throwable {
        final TypeBuilder<?> t = buildType();
        final FieldBuilder f = t.defineField("f", Types.Object, Modifier.PUBLIC);

        for (final Type<?> v : getOutOfScopeTypeVariables(t)) {
            f.setFieldType(v);

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, t),
                t
            );

            f.setFieldType(Type.of(SuperClass.class).makeGenericType(v));

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, t),
                t
            );

            f.setFieldType(Type.of(SuperClass.class).makeGenericType(Type.makeExtendsWildcard(v)));

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, t),
                t
            );

            f.setFieldType(Type.of(SuperClass.class).makeGenericType(Type.makeSuperWildcard(v)));

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, t),
                t
            );
        }
    }

    @Test
    public void testConstructorContainsOutOfScopeTypeVariable() throws Throwable {
        final TypeBuilder<?> t = buildType();
        final TypeList typeVariables = getOutOfScopeTypeVariables(t);
        final ConstructorBuilder c = t.defineConstructor(Modifier.PUBLIC, Type.list(Types.Object));

        final CodeGenerator g = new CodeGenerator(c.getMethodBuilder());
        final LocalBuilder l = g.declareLocal("l", Types.Object);

        c.defineParameter(0, "v");
        c.getMethodBuilder().generator = g;

        for (final Type<?> v : typeVariables) {
            g.locals[0] = new LocalBuilder(l.getLocalIndex(), l.getName(), v, l.getMethodBuilder());

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, c),
                t
            );

            g.locals[0] = l;

            c.setParameterTypes(Type.list(v));

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, c),
                t
            );

            c.setParameterTypes(Type.list(Type.of(SuperClass.class).makeGenericType(v)));

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, c),
                t
            );

            c.setParameterTypes(Type.list(Types.Object));
        }
    }

    @Test
    public void testMethodContainsOutOfScopeTypeVariable() throws Throwable {
        final TypeBuilder<?> t = buildType();
        final TypeList typeVariables = getOutOfScopeTypeVariables(t);
        final MethodBuilder m = t.defineMethod("f", Modifier.PUBLIC, Types.Object, Type.list(Types.Object));

        final CodeGenerator g = new CodeGenerator(m);
        final LocalBuilder l = g.declareLocal("l", Types.Object);

        m.defineParameter(0, "v");

        for (final Type<?> v : typeVariables) {
            m.generator = g;
            g.locals[0] = new LocalBuilder(l.getLocalIndex(), l.getName(), v, l.getMethodBuilder());

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, m),
                t
            );

            g.locals[0] = l;
            m.generator = null;

            m.setSignature(Types.Object, Type.list(v));

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, m),
                t
            );

            m.setSignature(Types.Object, Type.list(Type.of(SuperClass.class).makeGenericType(v)));

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, m),
                t
            );

            m.setSignature(v, Type.list(Types.Object));

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, m),
                t
            );

            m.setSignature(Types.Object, Type.list(Types.Object));
            m.setThrownTypes(Type.list(v));

            assertVerificationException(
                Verifier.typeVariableOutOfScopeError(v, m),
                t
            );

            m.setThrownTypes(TypeList.empty());
        }
    }
}
