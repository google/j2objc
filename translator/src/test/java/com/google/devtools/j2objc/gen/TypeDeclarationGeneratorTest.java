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

/**
 * Tests for {@link TypeDeclarationGenerator}.
 *
 * @author Keith Stanger
 */
public class TypeDeclarationGeneratorTest extends GenerationTest {

  public void testAnonymousClassDeclaration() throws IOException {
    String translation = translateSourceFile(
      "public class Example { Runnable run = new Runnable() { public void run() {} }; }",
      "Example", "Example.m");
    assertTranslation(translation, "@interface Example_$1 : NSObject < JavaLangRunnable >");
    assertTranslation(translation, "- (void)run;");
    // Outer reference is not required.
    assertNotInTranslation(translation, "Example *this");
    assertNotInTranslation(translation, "- (id)initWithExample:");
  }

  public void testAnonymousConcreteSubclassOfGenericAbstractType() throws IOException {
    String translation = translateSourceFile(
        "public class Test {"
        + "  interface FooInterface<T> { public void foo1(T t); public void foo2(); }"
        + "  abstract static class Foo<T> implements FooInterface<T> { public void foo2() { } }"
        + "  Foo<Integer> foo = new Foo<Integer>() {"
        + "    public void foo1(Integer i) { } }; }",
        "Test", "Test.m");
    assertTranslation(translation, "foo1WithId:(JavaLangInteger *)i");
  }
}
