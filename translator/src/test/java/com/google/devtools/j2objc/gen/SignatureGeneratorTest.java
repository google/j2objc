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
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;

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
    List<BodyDeclaration> decls = unit.getTypes().get(0).getBodyDeclarations();
    assertEquals(8, decls.size() - 2);  // Ignore added init, dealloc methods.

    // Verify a and b don't return a signature, since they aren't generic types.
    assertNull(SignatureGenerator.createFieldTypeSignature(getVariable(decls, 0)));
    assertNull(SignatureGenerator.createFieldTypeSignature(getVariable(decls, 1)));

    assertEquals("TX;", SignatureGenerator.createFieldTypeSignature(getVariable(decls, 2)));
    assertEquals("[TY;", SignatureGenerator.createFieldTypeSignature(getVariable(decls, 3)));
    assertEquals("Ljava/lang/Class<*>;",
        SignatureGenerator.createFieldTypeSignature(getVariable(decls, 4)));
    assertEquals("Ljava/util/List<TX;>;",
        SignatureGenerator.createFieldTypeSignature(getVariable(decls, 5)));
    assertEquals("Ljava/lang/Comparable<-TX;>;",
        SignatureGenerator.createFieldTypeSignature(getVariable(decls, 6)));
    assertEquals("LA<+Ljava/lang/Number;*Ljava/lang/String;>;",
        SignatureGenerator.createFieldTypeSignature(getVariable(decls, 7)));
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
    List<BodyDeclaration> decls = unit.getTypes().get(0).getBodyDeclarations();
    assertEquals(6, decls.size() - 1);  // Ignore added init method.

    // Verify a, b and c don't return a signature, since they aren't generic types.
    assertNull(SignatureGenerator.createMethodTypeSignature(getMethod(decls, 0)));
    assertNull(SignatureGenerator.createMethodTypeSignature(getMethod(decls, 1)));
    assertNull(SignatureGenerator.createMethodTypeSignature(getMethod(decls, 2)));

    assertEquals("()TX;", SignatureGenerator.createMethodTypeSignature(getMethod(decls, 3)));
    assertEquals("(TX;TY;)V", SignatureGenerator.createMethodTypeSignature(getMethod(decls, 4)));
    assertEquals("<T:Ljava/lang/Throwable;>(Ljava/lang/Throwable;)V",
        SignatureGenerator.createMethodTypeSignature(getMethod(decls, 5)));
  }

  private IVariableBinding getVariable(List<BodyDeclaration> decls, int i) {
    assertTrue(i < decls.size());
    return ((FieldDeclaration) decls.get(i)).getFragments().get(0).getVariableBinding();
  }

  private IMethodBinding getMethod(List<BodyDeclaration> decls, int i) {
    assertTrue(i < decls.size());
    return ((MethodDeclaration) decls.get(i)).getMethodBinding();
  }
}
