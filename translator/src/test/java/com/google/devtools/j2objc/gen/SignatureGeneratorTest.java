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

import com.google.common.collect.Lists;
import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.util.ElementUtil;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

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
    SignatureGenerator signatureGenerator = unit.getEnv().signatureGenerator();
    List<AbstractTypeDeclaration> decls = unit.getTypes();
    assertEquals(6, decls.size());

    // Verify A doesn't return a signature, since it isn't a generic type.
    assertNull(signatureGenerator.createClassSignature(decls.get(0).getTypeElement()));

    assertEquals("<X:Ljava/lang/Object;>Ljava/lang/Object;",
        signatureGenerator.createClassSignature(decls.get(1).getTypeElement()));
    assertEquals("<X:Ljava/lang/Object;Y:Ljava/lang/Object;Z:Ljava/lang/Object;>Ljava/lang/Object;",
        signatureGenerator.createClassSignature(decls.get(2).getTypeElement()));
    assertEquals("Ljava/util/AbstractList<Ljava/lang/String;>;",
        signatureGenerator.createClassSignature(decls.get(3).getTypeElement()));
    assertEquals("<C::Ljava/lang/Comparable<-TC;>;>Ljava/lang/Object;",
        signatureGenerator.createClassSignature(decls.get(4).getTypeElement()));
    assertEquals("Ljava/lang/ref/WeakReference<Ljava/util/concurrent/ForkJoinTask<*>;>;",
        signatureGenerator.createClassSignature(decls.get(5).getTypeElement()));
  }

  public void testFieldSignatures() throws IOException {
    CompilationUnit unit = translateType("A", "class A<X,Y,Z> { int a; double[] b; X c; Y[] d; "
        + "Class<?> e; java.util.List<X> f; Comparable<? super X> g; "
        + "A<? extends Number, ?, String> h; }");
    SignatureGenerator signatureGenerator = unit.getEnv().signatureGenerator();
    List<VariableElement> vars =
        ElementUtil.getDeclaredFields(unit.getTypes().get(0).getTypeElement());
    assertEquals(8, vars.size());

    // Verify a and b don't return a signature, since they aren't generic types.
    assertNull(signatureGenerator.createFieldTypeSignature(vars.get(0)));
    assertNull(signatureGenerator.createFieldTypeSignature(vars.get(1)));

    assertEquals("TX;", signatureGenerator.createFieldTypeSignature(vars.get(2)));
    assertEquals("[TY;", signatureGenerator.createFieldTypeSignature(vars.get(3)));
    assertEquals("Ljava/lang/Class<*>;", signatureGenerator.createFieldTypeSignature(vars.get(4)));
    assertEquals("Ljava/util/List<TX;>;", signatureGenerator.createFieldTypeSignature(vars.get(5)));
    assertEquals("Ljava/lang/Comparable<-TX;>;",
        signatureGenerator.createFieldTypeSignature(vars.get(6)));
    assertEquals("LA<+Ljava/lang/Number;*Ljava/lang/String;>;",
        signatureGenerator.createFieldTypeSignature(vars.get(7)));
  }

  public void testMethodSignatures() throws IOException {
    CompilationUnit unit = translateType("A", "class A<X, Y, Z extends Throwable> { "
        + "void a() {} "
        + "int b(boolean z) { return 0; } "
        + "void c(int i) throws IndexOutOfBoundsException {} "
        + "X d() { return null; } "
        + "void e(X x, Y y) {} "
        + "void f() throws Z {} "
        + "<T extends Throwable> void rethrow(Throwable t) {}"
        + "}");
    SignatureGenerator signatureGenerator = unit.getEnv().signatureGenerator();
    List<ExecutableElement> methods =
        ElementUtil.getExecutables(unit.getTypes().get(0).getTypeElement());
    assertEquals(8, methods.size()); // methods[0] is the default constructor.

    // Verify a, b and c don't return a signature, since they aren't generic types.
    assertNull(signatureGenerator.createMethodTypeSignature(methods.get(1)));
    assertNull(signatureGenerator.createMethodTypeSignature(methods.get(2)));
    assertNull(signatureGenerator.createMethodTypeSignature(methods.get(3)));

    assertEquals("()TX;", signatureGenerator.createMethodTypeSignature(methods.get(4)));
    assertEquals("(TX;TY;)V", signatureGenerator.createMethodTypeSignature(methods.get(5)));
    assertEquals("()V^TZ;", signatureGenerator.createMethodTypeSignature(methods.get(6)));
    assertEquals("<T:Ljava/lang/Throwable;>(Ljava/lang/Throwable;)V",
        signatureGenerator.createMethodTypeSignature(methods.get(7)));
  }

  public void testMultipleBounds() throws IOException {
    CompilationUnit unit = translateType("A",
        "class A <T extends Number & java.io.Serializable >{}");
    SignatureGenerator signatureGenerator = unit.getEnv().signatureGenerator();
    List<AbstractTypeDeclaration> decls = unit.getTypes();
    assertEquals(1, decls.size());
    assertEquals("<T:Ljava/lang/Number;:Ljava/io/Serializable;>Ljava/lang/Object;",
        signatureGenerator.createClassSignature(decls.get(0).getTypeElement()));
  }

  public void testGenericInterface() throws IOException {
    CompilationUnit unit = translateType("A",
        "interface A<E> extends java.util.Collection<E> {}");
    SignatureGenerator signatureGenerator = unit.getEnv().signatureGenerator();
    List<AbstractTypeDeclaration> decls = unit.getTypes();
    assertEquals(1, decls.size());
    assertEquals("<E:Ljava/lang/Object;>Ljava/lang/Object;Ljava/util/Collection<TE;>;",
        signatureGenerator.createClassSignature(decls.get(0).getTypeElement()));
  }

  public void testJniSignatures() throws IOException {
    CompilationUnit unit = translateType("D", "package foo.bar; class D {"
        + "native void foo(int i, float f, String s);"
        + "native void a_b$c();"
        + "native void 你好世界();"
        + "native void bar();"
        + "native void bar(String s);"
        + "native void bar(boolean b, String s);"
        + "native void bar(String[] s); "
        + "static class 测试 { native void mumble(); }}");
    SignatureGenerator signatureGenerator = unit.getEnv().signatureGenerator();
    List<AbstractTypeDeclaration> decls = unit.getTypes();
    assertEquals(2, decls.size());
    List<ExecutableElement> methods = Lists.newArrayList(
        ElementUtil.getMethods(decls.get(0).getTypeElement()));
    assertEquals(7, methods.size());
    
    Set<String> signatures = new HashSet<>(methods.size());
    for (ExecutableElement method : methods) {
      signatures.add(signatureGenerator.createJniFunctionSignature(method));
    }

    // Expected JNI signatures were copied from javah output.

    // Verify no parameters, since foo isn't overloaded.
    assertTrue(signatures.contains("Java_foo_bar_D_foo"));

    // Verify underscores and dollar signs in names are mangled.
    assertTrue(signatures.contains("Java_foo_bar_D_a_1b_00024c"));

    // Verify Unicode characters are mangled.
    assertTrue(signatures.contains("Java_foo_bar_D__04f60_0597d_04e16_0754c"));

    // Verify overloaded methods have parameter suffixes.
    assertTrue(signatures.contains("Java_foo_bar_D_bar__"));
    assertTrue(signatures.contains("Java_foo_bar_D_bar__Ljava_lang_String_2"));
    assertTrue(signatures.contains("Java_foo_bar_D_bar__ZLjava_lang_String_2"));
    assertTrue(signatures.contains("Java_foo_bar_D_bar___3Ljava_lang_String_2"));

    // Check Unicode class name mangling.
    methods = Lists.newArrayList(ElementUtil.getMethods(decls.get(1).getTypeElement()));
    assertEquals(1, methods.size());
    assertEquals("Java_foo_bar_D_00024_06d4b_08bd5_mumble",
        signatureGenerator.createJniFunctionSignature(methods.get(0)));
  }

  public void testGenericTypeMetadata() throws IOException {
    String translation = translateSourceFile(
        "import java.util.*; class Test <T> { Set<T> set; "
        + " void test(Map<Long, List<T>> map) {} }",
        "Test", "Test.m");

    // Assert class metadata has generic signature.
    assertTranslation(translation, "\"<T:Ljava/lang/Object;>Ljava/lang/Object;\"");

    // Assert method metadata has generic signature. (in pointer table)
    assertTranslation(translation, "\"(Ljava/util/Map<Ljava/lang/Long;Ljava/util/List<TT;>;>;)V\"");

    // Assert field metadata has generic signature. (in pointer table)
    assertTranslation(translation, "\"Ljava/util/Set<TT;>;\"");
  }

  public void testMethodParameterizedReturnTypeMetadata() throws IOException {
    String translation = translateSourceFile(
        "import java.util.*; class Test { List<String> getStringList() { return null; }}",
        "Test", "Test.m");

    // Assert method metadata has generic return signature.
    assertTranslation(translation, "\"()Ljava/util/List<Ljava/lang/String;>;\"");
  }

  public void testGenericMethodWithConcreteTypeArgument() throws IOException {
    CompilationUnit unit = translateType("MyList",
        "abstract class MyList extends java.util.AbstractList<String> { "
        + "public boolean add(String s) { return true; }}");
    SignatureGenerator signatureGenerator = unit.getEnv().signatureGenerator();
    List<ExecutableElement> methods =
        ElementUtil.getExecutables(unit.getTypes().get(0).getTypeElement());
    assertEquals(2, methods.size()); // methods[0] is the default constructor.

    // add(String) does not need a generic signature, even though it overrides a generic method.
    assertNull(signatureGenerator.createMethodTypeSignature(methods.get(1)));
  }

  public void testGenericClassWithArrayTypeVariable() throws IOException {
    String translation = translateSourceFile(
        "abstract class Test extends java.util.AbstractList<String[]> {}", "Test", "Test.m");

    // Assert array signature is valid in class signature.
    assertTranslation(translation, "Ljava/util/AbstractList<[Ljava/lang/String;>;");
  }
}
