/*
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
import java.io.IOException;

public class GenerateObjectiveCGenericsTest extends GenerationTest {

  @Override
  public void setUp() throws IOException {
    super.setUp();
  }

  public void testGenericsOptionClass() throws IOException {
    options.setAsObjCGenericDecl(true);
    addSourceFile(
        "public class Test<V> {                      "
            + "  V get(V input) {"
            + "    return input;"
            + "  }"
            + "}",
        "Test.java");

    String testHeader = translateSourceFile("Test", "Test.h");
    String testSource = translateSourceFile("Test", "Test.m");

    assertTranslation(testHeader, "@interface Test<V> : NSObject");
    assertTranslation(testHeader, "- (V)getWithId:(V)input;");
    assertTranslation(testSource, "- (id)getWithId:(id)input");
  }

  public void testGenericsAnnotatedClass() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.GenerateObjectiveCGenerics; "
            + "@GenerateObjectiveCGenerics "
            + "public class Test<V> {"
            + "  V get(V input) { "
            + "    return input; "
            + "  }"
            + "}",
        "Test.java");

    String testHeader = translateSourceFile("Test", "Test.h");
    String testSource = translateSourceFile("Test", "Test.m");

    assertTranslation(testHeader, "@interface Test<V> : NSObject");
    assertTranslation(testHeader, "- (V)getWithId:(V)input;");
    assertTranslation(testSource, "- (id)getWithId:(id)input");
  }

  public void testPublicDeclaration() throws IOException {
    options.setAsObjCGenericDecl(true);
    addSourceFile(
        "public class Test<V> {                         "
            + "  V get(V input) { "
            + "    return input;"
            + "  }"
            + "}",
        "Test.java");

    String testHeader = translateSourceFile("Test", "Test.h");
    String testSource = translateSourceFile("Test", "Test.m");

    assertTranslation(testHeader, "@interface Test<V> : NSObject");
    assertTranslation(testHeader, "- (V)getWithId:(V)input;");
    assertTranslation(testSource, "@implementation Test\n");
    assertTranslation(testSource, "- (id)getWithId:(id)input");
  }

  public void testPrivateDeclaration() throws IOException {
    options.setAsObjCGenericDecl(true);
    addSourceFile(
        "public class Test<V> {"
            + "  private V get(V input) { "
            + "    return input; "
            + "  }"
            + "}",
        "Test.java");

    String testHeader = translateSourceFile("Test", "Test.h");
    String testSource = translateSourceFile("Test", "Test.m");

    assertTranslation(testHeader, "@interface Test<V> : NSObject");
    assertNotInTranslation(testHeader, "- (V)getWithId:(V)input;");
    assertTranslation(testSource, "@interface Test<V> ()");
    assertTranslation(testSource, "- (V)getWithId:(V)input;");
    assertTranslation(testSource, "@implementation Test\n");
    assertTranslation(testSource, "- (id)getWithId:(id)input");
  }

  public void testGenericsClassForwardDeclaration() throws IOException {
    options.setAsObjCGenericDecl(true);
    addSourceFile("public class Foo<K,V> {}", "Foo.java");
    addSourceFile(
        "public class Bar {                   "
            + "  Foo get(Foo input) {"
            + "    return input;"
            + "  }"
            + "}",
        "Bar.java");

    String forwardDeclaredHeader = translateSourceFile("Bar", "Bar.h");
    assertTranslation(forwardDeclaredHeader, "@class Foo<K, V>;");
  }

  public void testGenericsAnnotatedClassForwardDeclaration() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.GenerateObjectiveCGenerics; "
            + "@GenerateObjectiveCGenerics "
            + "public class Foo<K,V> {}",
        "Foo.java");
    addSourceFile("public class Baz<R,T> {}", "Baz.java");
    addSourceFile(
        "public class Bar {"
            + "  Foo getFoo(Foo input) { "
            + "    return input; "
            + "  }"
            + "  Baz getBaz(Baz input) { "
            + "    return input; "
            + "  }"
            + "}",
        "Bar.java");

    String forwardDeclaredHeader = translateSourceFile("Bar", "Bar.h");
    assertTranslation(forwardDeclaredHeader, "@class Foo<K, V>;");
    // No generics because this class is not annotated.
    assertTranslation(forwardDeclaredHeader, "@class Baz;");
  }

  public void testGenericsInterfaceForwardDeclaration() throws IOException {
    options.setAsObjCGenericDecl(true);
    addSourceFile("public interface Foo<K,V> {}", "Foo.java");
    addSourceFile(
        "public class Bar {               "
            + "  Foo get(Foo input) {"
            + "    return input;"
            + "  }"
            + "}",
        "Bar.java");

    String forwardDeclaredHeader = translateSourceFile("Bar", "Bar.h");
    assertTranslation(forwardDeclaredHeader, "@protocol Foo;");
  }

  public void testReturnedType() throws IOException {
    options.setAsObjCGenericDecl(true);
    addSourceFile(
        "public class Future<V> {"
            + "  private final V value; "
            + "  public Future(V value) {"
            + "    this.value = value; "
            + "  }"
            + "  public V get() { "
            + "    return value; "
            + "  }"
            + "}",
        "Future.java");
    addSourceFile(
        "public class Test<T> {"
            + "  public Future<String> fetch(String url) {"
            + "    return new Future<>(url);"
            + "  }"
            + "  public T get(T input) {"
            + "    return input;"
            + "  }"
            + "}",
        "Test.java");

    String testHeader = translateSourceFile("Test", "Test.h");
    String testSource = translateSourceFile("Test", "Test.m");

    assertTranslation(testHeader, "- (Future<NSString *> *)fetchWithNSString:(NSString *)url;");
    assertTranslation(testHeader, "- (T)getWithId:(T)input;");
    assertTranslation(testSource, "- (Future *)fetchWithNSString:(NSString *)url");
    assertTranslation(testSource, "- (id)getWithId:(id)input");
  }

  public void testAnnotatedReturnedType() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.GenerateObjectiveCGenerics; "
            + "@GenerateObjectiveCGenerics "
            + "public class Future<V> {"
            + "  private final V value; "
            + "  public Future(V value) {"
            + "    this.value = value; "
            + "  }"
            + "  public V get() { "
            + "    return value; "
            + "  }"
            + "}",
        "Future.java");
    addSourceFile(
        "public class Test<T> {"
            + "  public Future<String> fetch(String url) {"
            + "    return new Future<>(url);"
            + "  }"
            + "  public T get(T input) {"
            + "    return input;"
            + "  }"
            + "}",
        "Test.java");

    String testHeader = translateSourceFile("Test", "Test.h");
    String testSource = translateSourceFile("Test", "Test.m");

    assertTranslation(testHeader, "- (Future<NSString *> *)fetchWithNSString:(NSString *)url;");
    assertTranslation(testHeader, "- (id)getWithId:(id)input;");
    assertTranslation(testSource, "- (Future *)fetchWithNSString:(NSString *)url");
    assertTranslation(testSource, "- (id)getWithId:(id)input");
  }

  public void testNestedReturnedType() throws IOException {
    options.setAsObjCGenericDecl(true);
    addSourceFile(
        "public class List<V> {"
            + "  private final V value; "
            + "  public List(V value) {"
            + "    this.value = value; "
            + "  }"
            + "}",
        "List.java");
    addSourceFile(
        "public class Test {"
            + "  public List<List<String>> fetch(String url) {"
            + "    List<String> list = new List<String>(url); "
            + "    return new List<List<String>>(list); "
            + "  }"
            + "}",
        "Test.java");

    String testHeader = translateSourceFile("Test", "Test.h");
    String testSource = translateSourceFile("Test", "Test.m");

    assertTranslation(
        testHeader, "- (List<List<NSString *> *> *)fetchWithNSString:(NSString *)url;");
    assertTranslation(testSource, "- (List *)fetchWithNSString:(NSString *)url");
  }

  public void testMultipleGenericTypes() throws IOException {
    options.setAsObjCGenericDecl(true);
    addSourceFile(
        "public class Test<U, V> {"
            + "  private U first; "
            + "  private V second; "
            + "  public U getFirst() { return first; } "
            + "  public V getSecond() { return second; } "
            + "}",
        "Test.java");

    String testHeader = translateSourceFile("Test", "Test.h");

    assertTranslation(testHeader, "@interface Test<U, V> : NSObject");
    assertTranslation(testHeader, "- (U)getFirst;");
    assertTranslation(testHeader, "- (V)getSecond;");
  }

  public void testReturnedTypeWithMultipleTypes() throws IOException {
    options.setAsObjCGenericDecl(true);
    addSourceFile(
        "public class Map<K, V> {                                   "
            + "  private K key;"
            + "  private V value;"
            + "}",
        "Map.java");

    addSourceFile(
        "public class List<E> {                                     "
            + "  private E element;"
            + "}",
        "List.java");
    addSourceFile(
        "public class Test { "
            + "  public Map<String, List<List<String>>> get("
            + "    Map<String, List<List<String>>> input) {"
            + "      return input; "
            + "  }"
            + "}",
        "Test.java");

    String testHeader = translateSourceFile("Test", "Test.h");

    assertTranslation(testHeader, "- (Map<NSString *, List<List<NSString *> *> *> *)getWithMap");
  }

  public void testReturnTypedInterfaces() throws IOException {
    options.setAsObjCGenericDecl(true);
    addSourceFile(
        "import com.google.common.util.concurrent.ListenableFuture; "
            + "public class Test<V> {"
            + "  ListenableFuture<V> get(ListenableFuture<V> input) { "
            + "    return input; "
            + "  }"
            + "}",
        "Test.java");

    String testHeader = translateSourceFile("Test", "Test.h");
    String testSource = translateSourceFile("Test", "Test.m");

    assertTranslation(
        testHeader,
        "- (id<ComGoogleCommonUtilConcurrentListenableFuture>)"
            + "getWithComGoogleCommonUtilConcurrentListenableFuture:"
            + "(id<ComGoogleCommonUtilConcurrentListenableFuture>)input;");
    assertTranslation(
        testSource,
        "- (id<ComGoogleCommonUtilConcurrentListenableFuture>)"
            + "getWithComGoogleCommonUtilConcurrentListenableFuture:"
            + "(id<ComGoogleCommonUtilConcurrentListenableFuture>)input");
  }

  public void testMethodDeclaredGenericsIgnored() throws IOException {
    options.setAsObjCGenericDecl(true);
    addSourceFile(
        "public class Test {                     "
            + "public <V> V get(V input) {"
            + "    return input;"
            + "  }"
            + "}",
        "Test.java");

    String testHeader = translateSourceFile("Test", "Test.h");
    String testSource = translateSourceFile("Test", "Test.m");

    assertTranslation(testHeader, "@interface Test : NSObject");
    assertTranslation(testHeader, "- (id)getWithId:(id)input;");
    assertTranslation(testSource, "- (id)getWithId:(id)input");
  }

  // Inner class generics, which is presently unsupported.
  public void testInnerClassGenerics() throws IOException {
    options.setAsObjCGenericDecl(true);
    addSourceFile(
        "public class A<X> { "
            + "  X getAX(X input) { "
            + "    return input; "
            + "  } "
            + "  class B<Y> {  "
            + "    X getBX(X input) { "
            + "      return input; "
            + "    }"
            + "    Y getBY(Y input) { "
            + "      return input; "
            + "    }"
            + "  } "
            + "}",
        "A.java");

    String testHeader = translateSourceFile("A", "A.h");

    assertTranslation(testHeader, "@interface A<X> : NSObject");
    assertTranslation(testHeader, "- (X)getAXWithId:(X)input;");
    assertTranslation(testHeader, "@interface A_B<Y> : NSObject");
    // Inner classes using generics from outer class presently unsupported.
    assertTranslation(testHeader, "- (id)getBXWithId:(id)input;");
    assertTranslation(testHeader, "- (Y)getBYWithId:(Y)input;");
  }

  // Inner interface generics. Interfaces (ObjC protocols) cannot carry generics at all.
  public void testInnerInterfaceGenerics() throws IOException {
    options.setAsObjCGenericDecl(true);
    addSourceFile(
        "public class A<X> { "
            + "  X getAX(X input) { "
            + "    return input; "
            + "  } "
            + "  interface B<Y> {  "
            + "    Y getBY(Y input);"
            + "  } "
            + "}",
        "A.java");

    String testHeader = translateSourceFile("A", "A.h");

    assertTranslation(testHeader, "@interface A<X> : NSObject");
    assertTranslation(testHeader, "- (X)getAXWithId:(X)input;");
    assertTranslation(testHeader, "@protocol A_B < JavaObject >");
    assertTranslation(testHeader, "- (id)getBYWithId:(id)input;");
  }

  public void testClassGenericsIgnored() throws IOException {
    options.setAsObjCGenericDecl(true);
    addSourceFile(
        "public class Foo<T> {                           "
            + "  Class<T> sameClass(Class<T> thing) { "
            + "    return thing; "
            + "  } "
            + "}",
        "Foo.java");

    String header = translateSourceFile("Foo", "Foo.h");
    assertTranslation(header, "- (IOSClass *)sameClassWithIOSClass:(IOSClass *)thing");
  }
}
