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

  public void testAnnotatedClass() throws IOException {
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
        "public class Test {"
            + "  public Future<String> fetch(String url) {"
            + "    return new Future<>(url);"
            + "  }"
            + "}",
        "Test.java");

    String testHeader = translateSourceFile("Test", "Test.h");
    String testSource = translateSourceFile("Test", "Test.m");

    assertTranslation(testHeader, "- (Future<NSString *> *)fetchWithNSString:(NSString *)url;");
    assertTranslation(testSource, "- (Future<NSString *> *)fetchWithNSString:(NSString *)url");
  }

  public void testNestedReturnedType() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.GenerateObjectiveCGenerics; "
            + "@GenerateObjectiveCGenerics "
            + "public class List<V> {"
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

    assertTranslation(
        testHeader, "- (List<List<NSString *> *> *)fetchWithNSString:(NSString *)url;");
  }

  public void testMultipleGenericTypes() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.GenerateObjectiveCGenerics; "
            + "@GenerateObjectiveCGenerics "
            + "public class Test<U, V> {"
            + "  private U first; "
            + "  private V second; "
            + "  public U getFirst() { return first; } "
            + "  public V getSecond() { return second; } "
            + "}",
        "Test.java");

    String testHeader = translateSourceFile("Test", "Test.h");

    assertTranslation(testHeader, "@interface Test<U, V> : NSObject");
  }

  public void testReturnedTypeWithMultipleTypes() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.GenerateObjectiveCGenerics; "
            + "@GenerateObjectiveCGenerics "
            + "public class Map<K, V> {"
            + "  private K key; "
            + "  private V value; "
            + "}",
        "Map.java");

    addSourceFile(
        "import com.google.j2objc.annotations.GenerateObjectiveCGenerics; "
            + "@GenerateObjectiveCGenerics "
            + "public class List<E> {"
            + "  private E element; "
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
    addSourceFile(
        "import com.google.j2objc.annotations.GenerateObjectiveCGenerics; "
            + "import com.google.common.util.concurrent.ListenableFuture; "
            + "@GenerateObjectiveCGenerics "
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
}
