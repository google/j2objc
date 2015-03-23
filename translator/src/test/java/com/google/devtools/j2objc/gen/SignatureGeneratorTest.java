/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.j2objc.gen;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.io.IOException;
import java.util.List;

/**
 * Verifies generic signature generation. Signature strings used here were created by
 * compiling each source using javac, then running "javap -h" to see what the generic
 * signature attribute strings were created.
 *
 * @author Tom Ball
 */
public class SignatureGeneratorTest extends GenerationTest {

  public void testClassSignatures() throws IOException {
    CompilationUnit unit = translateType("A",
        "import java.lang.ref.*; import java.util.concurrent.*; "
        + "class A {} "
        + "class B<X> {} "
        + "class C<X,Y,Z> {} "
        + "abstract class D extends java.util.AbstractList<String> {} "
        + "class E<C extends Comparable<? super C>> {}"
        + "class F extends WeakReference<ForkJoinTask<?>> { "
        + "  F(ForkJoinTask<?> task, Throwable ex, F next) { super(task, null); }}");
    List<AbstractTypeDeclaration> decls = unit.getTypes();
    assertEquals(6, decls.size());

    // Verify A doesn't return a signature, since it isn't a generic type.
    assertNull(SignatureGenerator.createClassSignature(decls.get(0).getTypeBinding()));

    assertEquals("<X:Ljava/lang/Object;>Ljava/lang/Object;",
        SignatureGenerator.createClassSignature(decls.get(1).getTypeBinding()));
    assertEquals("<X:Ljava/lang/Object;Y:Ljava/lang/Object;Z:Ljava/lang/Object;>Ljava/lang/Object;",
        SignatureGenerator.createClassSignature(decls.get(2).getTypeBinding()));
    assertEquals("Ljava/util/AbstractList<Ljava/lang/String;>;",
        SignatureGenerator.createClassSignature(decls.get(3).getTypeBinding()));
    assertEquals("<C::Ljava/lang/Comparable<-TC;>;>Ljava/lang/Object;",
        SignatureGenerator.createClassSignature(decls.get(4).getTypeBinding()));
    assertEquals("Ljava/lang/ref/WeakReference<Ljava/util/concurrent/ForkJoinTask<*>;>;",
        SignatureGenerator.createClassSignature(decls.get(5).getTypeBinding()));
  }

  public void testFieldSignatures() throws IOException {
    CompilationUnit unit = translateType("A", "class A<X,Y,Z> { int a; double[] b; X c; Y[] d; "
        + "Class<?> e; java.util.List<X> f; Comparable<? super X> g; "
        + "A<? extends Number, ?, String> h; }");
    IVariableBinding[] vars = unit.getTypes().get(0).getTypeBinding().getDeclaredFields();
    assertEquals(8, vars.length);

    // Verify a and b don't return a signature, since they aren't generic types.
    assertNull(SignatureGenerator.createFieldTypeSignature(vars[0]));
    assertNull(SignatureGenerator.createFieldTypeSignature(vars[1]));

    assertEquals("TX;", SignatureGenerator.createFieldTypeSignature(vars[2]));
    assertEquals("[TY;", SignatureGenerator.createFieldTypeSignature(vars[3]));
    assertEquals("Ljava/lang/Class<*>;", SignatureGenerator.createFieldTypeSignature(vars[4]));
    assertEquals("Ljava/util/List<TX;>;", SignatureGenerator.createFieldTypeSignature(vars[5]));
    assertEquals("Ljava/lang/Comparable<-TX;>;",
        SignatureGenerator.createFieldTypeSignature(vars[6]));
    assertEquals("LA<+Ljava/lang/Number;*Ljava/lang/String;>;",
        SignatureGenerator.createFieldTypeSignature(vars[7]));
  }

  public void testMethodSignatures() throws IOException {
    CompilationUnit unit = translateType("A", "class A<X,Y> { "
        + "void a() {} "
        + "int b(boolean z) { return 0; } "
        + "void c(int i) throws IndexOutOfBoundsException {} "
        + "X d() { return null; } "
        + "void e(X x, Y y) {} "
        + "<T extends Throwable> void rethrow(Throwable t) {}"
        + "}");
    IMethodBinding[] methods = unit.getTypes().get(0).getTypeBinding().getDeclaredMethods();
    assertEquals(7, methods.length); // methods[0] is the default constructor.

    // Verify a, b and c don't return a signature, since they aren't generic types.
    assertNull(SignatureGenerator.createMethodTypeSignature(methods[1]));
    assertNull(SignatureGenerator.createMethodTypeSignature(methods[2]));
    assertNull(SignatureGenerator.createMethodTypeSignature(methods[3]));

    assertEquals("()TX;", SignatureGenerator.createMethodTypeSignature(methods[4]));
    assertEquals("(TX;TY;)V", SignatureGenerator.createMethodTypeSignature(methods[5]));
    assertEquals("<T:Ljava/lang/Throwable;>(Ljava/lang/Throwable;)V",
        SignatureGenerator.createMethodTypeSignature(methods[6]));
  }

  public void testMultipleBounds() throws IOException {
    CompilationUnit unit = translateType("A",
        "class A <T extends Number & java.io.Serializable >{}");
    List<AbstractTypeDeclaration> decls = unit.getTypes();
    assertEquals(1, decls.size());
    assertEquals("<T:Ljava/lang/Number;:Ljava/io/Serializable;>Ljava/lang/Object;",
        SignatureGenerator.createClassSignature(decls.get(0).getTypeBinding()));
  }

  public void testGenericInterface() throws IOException {
    CompilationUnit unit = translateType("A",
        "interface A<E> extends java.util.Collection<E> {}");
    List<AbstractTypeDeclaration> decls = unit.getTypes();
    assertEquals(1, decls.size());
    assertEquals("<E:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/Collection<TE;>;",
        SignatureGenerator.createClassSignature(decls.get(0).getTypeBinding()));
  }
}
