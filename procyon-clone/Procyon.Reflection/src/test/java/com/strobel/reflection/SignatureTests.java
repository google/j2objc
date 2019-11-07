/*
 * SignatureTests.java
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

import com.strobel.reflection.emit.GenericParameterBuilder;
import com.strobel.reflection.emit.MethodBuilder;
import com.strobel.reflection.emit.TypeBuilder;
import org.junit.Test;

import java.lang.reflect.Modifier;

import static org.junit.Assert.*;

@SuppressWarnings("SpellCheckingInspection")
public class SignatureTests {
    @Test
    public void testSignatureOfNonGenericMethodInGenericDefinition() {
        final MethodInfo put = Types.Map.getMethod("put");

        assertEquals("(TK;TV;)TV;", put.getSignature());
        assertEquals("(TK;TV;)TV;", put.getSignature());
        assertEquals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", put.getErasedSignature());
    }

    @Test
    public void testSignatureOfNonGenericMethodInGenericInstance() {
        final MethodInfo put = Types.Map.makeGenericType(Types.String, Types.Date).getMethod("put");

        assertEquals("(Ljava/lang/String;Ljava/util/Date;)Ljava/util/Date;", put.getSignature());
        assertEquals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", put.getErasedSignature());
    }

    @Test
    public void testSignatureOfNonGenericMethodInErasedType() {
        MethodInfo put = Types.Map.getErasedType().getMethod("put");

        assertEquals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", put.getSignature());
        assertEquals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", put.getErasedSignature());

        put = Types.Map.makeGenericType(Types.String, Types.Date).getErasedType().getMethod("put");

        assertEquals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", put.getSignature());
        assertEquals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", put.getErasedSignature());

        put = Types.Map.getMethod("put").getErasedMethodDefinition();

        assertEquals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", put.getSignature());
        assertEquals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", put.getErasedSignature());

        put = Types.Map.makeGenericType(Types.String, Types.Date).getMethod("put").getErasedMethodDefinition();

        assertEquals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", put.getSignature());
        assertEquals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", put.getErasedSignature());
    }

    @Test
    public void testSignatureOfGenericMethodInGenericDefinition() {
        final MethodInfo toArray = Types.List.getMethod("toArray", Types.Object.makeArrayType());

        assertEquals("<T:Ljava/lang/Object;>([TT;)[TT;", toArray.getSignature());
        assertEquals("([Ljava/lang/Object;)[Ljava/lang/Object;", toArray.getErasedSignature());
    }

    @Test
    public void testSignatureOfGenericMethodInGenericInstance() {
        final MethodInfo toArray = Types.List.makeGenericType(Types.String).getMethod("toArray", Types.Object.makeArrayType());

        assertEquals("<T:Ljava/lang/Object;>([TT;)[TT;", toArray.getSignature());
        assertEquals("([Ljava/lang/Object;)[Ljava/lang/Object;", toArray.getErasedSignature());
    }

    @Test
    public void testSignatureOfGenericMethodInErasedType() {
        MethodInfo toArray = Types.List.getErasedType().getMethod("toArray", Types.Object.makeArrayType());

        assertEquals("([Ljava/lang/Object;)[Ljava/lang/Object;", toArray.getSignature());
        assertEquals("([Ljava/lang/Object;)[Ljava/lang/Object;", toArray.getErasedSignature());

        toArray = Types.List.makeGenericType(Types.String).getErasedType().getMethod("toArray", Types.Object.makeArrayType());

        assertEquals("([Ljava/lang/Object;)[Ljava/lang/Object;", toArray.getSignature());
        assertEquals("([Ljava/lang/Object;)[Ljava/lang/Object;", toArray.getErasedSignature());

        toArray = Types.List.getMethod("toArray", Types.Object.makeArrayType()).getErasedMethodDefinition();

        assertEquals("([Ljava/lang/Object;)[Ljava/lang/Object;", toArray.getSignature());
        assertEquals("([Ljava/lang/Object;)[Ljava/lang/Object;", toArray.getErasedSignature());

        toArray = Types.List.makeGenericType(Types.String).getMethod("toArray", Types.Object.makeArrayType()).getErasedMethodDefinition();

        assertEquals("([Ljava/lang/Object;)[Ljava/lang/Object;", toArray.getSignature());
        assertEquals("([Ljava/lang/Object;)[Ljava/lang/Object;", toArray.getErasedSignature());
    }

    @Test
    public void testSignatureOfGenericMethodInNonGenericTypeBuilder() {
        final TypeBuilder<?> tb = new TypeBuilder(
            "TestSignatureOfGenericMethodInTypeBuilder",
            Modifier.PUBLIC,
            Types.Object,
            TypeList.empty()
        );

        final MethodBuilder test = tb.defineMethod("test", Modifier.PUBLIC, Types.Object);
        final GenericParameterBuilder<?>[] gps = test.defineGenericParameters("X");

        test.setReturnType(gps[0]);
        test.setParameters(Type.list(gps[0]));
        test.defineParameter(0, "t");

        assertEquals("<X:Ljava/lang/Object;>(TX;)TX;", test.getSignature());
        assertEquals("(Ljava/lang/Object;)Ljava/lang/Object;", test.getErasedSignature());
    }

    @Test
    public void testSignatureOfNonGenericMethodInGenericTypeBuilder() {
        final TypeBuilder<?> tb = new TypeBuilder(
            "TestSignatureOfGenericMethodInTypeBuilder",
            Modifier.PUBLIC,
            Types.Object,
            TypeList.empty()
        );

        final GenericParameterBuilder<?>[] gps = tb.defineGenericParameters("X");
        final MethodBuilder test = tb.defineMethod("test", Modifier.PUBLIC, gps[0], Type.list(gps[0]));

        test.defineParameter(0, "t");

        assertEquals("(TX;)TX;", test.getSignature());
        assertEquals("(Ljava/lang/Object;)Ljava/lang/Object;", test.getErasedSignature());
    }

    @Test
    public void testSignatureOfGenericMethodInGenericTypeBuilder() {
        final TypeBuilder<?> tb = new TypeBuilder(
            "TestSignatureOfGenericMethodInTypeBuilder",
            Modifier.PUBLIC,
            Types.Object,
            TypeList.empty()
        );

        final MethodBuilder test = tb.defineMethod("test", Modifier.PUBLIC, Types.Object);

        final GenericParameterBuilder<?>[] tgps = tb.defineGenericParameters("X");
        final GenericParameterBuilder<?>[] mgps = test.defineGenericParameters("Y");

        final GenericParameterBuilder<?> x = tgps[0];
        final GenericParameterBuilder<?> y = mgps[0];

        y.setBaseTypeConstraint(x);

        test.setReturnType(y);
        test.setParameters(Type.list(x));
        test.defineParameter(0, "t");

        assertEquals("<Y:TX;>(TX;)TY;", test.getSignature());
        assertEquals("(Ljava/lang/Object;)Ljava/lang/Object;", test.getErasedSignature());
    }

    @Test
    public void testCyclicInheritance() {
        final Type<Cycle.A> a = Type.of(Cycle.A.class);
        final Type<Cycle.B> b = Type.of(Cycle.B.class);
        final Type<Cycle.C> c = Type.of(Cycle.C.class);

        assertEquals("Lcom/strobel/reflection/SignatureTests$Cycle$A<TT;>;", a.getSignature());
        assertEquals("Lcom/strobel/reflection/SignatureTests$Cycle$B;", b.getSignature());
        assertEquals("Lcom/strobel/reflection/SignatureTests$Cycle$C;", c.getSignature());

        assertEquals("<T:Ljava/lang/Object;>Ljava/lang/Object;", a.getGenericSignature());
        assertEquals("Lcom/strobel/reflection/SignatureTests$Cycle$A<Lcom/strobel/reflection/SignatureTests$Cycle$C;>;", b.getGenericSignature());
        assertEquals("Lcom/strobel/reflection/SignatureTests$Cycle$B;", c.getGenericSignature());
    }

    @Test
    public void testCyclicInheritanceWithTypeParameters() {
        final Type<G> g = Type.of(G.class);
        final Type<F> f = Type.of(F.class);

        final Type<G.Node> gn = Type.of(G.Node.class);
        final Type<F.MyNode> fn = Type.of(F.MyNode.class);

        assertEquals("Lcom/strobel/reflection/SignatureTests$G<TN;>;", g.getSignature());
        assertEquals("Lcom/strobel/reflection/SignatureTests$F;", f.getSignature());

        assertEquals("Lcom/strobel/reflection/SignatureTests$G$Node<TN;>;", gn.getSignature());
        assertEquals("Lcom/strobel/reflection/SignatureTests$F$MyNode;", fn.getSignature());

        assertEquals("<N:Lcom/strobel/reflection/SignatureTests$G$Node<TN;>;>Ljava/lang/Object;", g.getGenericSignature());
        assertEquals("Lcom/strobel/reflection/SignatureTests$G<Lcom/strobel/reflection/SignatureTests$F$MyNode;>;", f.getGenericSignature());

        assertEquals("<N:Lcom/strobel/reflection/SignatureTests$G$Node<TN;>;>Ljava/lang/Object;", gn.getGenericSignature());
        assertEquals("Lcom/strobel/reflection/SignatureTests$G$Node<Lcom/strobel/reflection/SignatureTests$F$MyNode;>;", fn.getGenericSignature());
    }

    @SuppressWarnings("ALL")
    private static class Cycle {
        class A<T> {}
        class B extends A<C> {}
        class C extends B {}
    }

    @SuppressWarnings("ALL")
    private static class G<N extends G.Node<N>> {
        static class Node<N extends Node<N>> {
        }
    }

    private static class F extends G<F.MyNode> {
        static class MyNode extends G.Node<MyNode> {
        }
    }
}
