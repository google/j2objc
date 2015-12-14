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

package com.google.devtools.j2objc.translate;

import com.google.devtools.j2objc.GenerationTest;

import java.io.IOException;

/**
 * Unit tests for {@link PrivateDeclarationResolver}.
 *
 * @author Keith Stanger
 */
public class PrivateDeclarationResolverTest extends GenerationTest {

  public void testPrivateSuperclassOfPublicClass() throws IOException {
    String translation = translateSourceFile(
        "class Test { private static class A {} public static class B extends A {} }",
        "Test", "Test.h");
    // Make sure the private superclass is still declared in the header.
    assertTranslation(translation, "@interface Test_A");
  }

  public void testPrivateGenericSuperclassOfPublicClass() throws IOException {
    String translation = translateSourceFile(
        "class Test { private static class A<T> {} public static class B extends A<String> {} }",
        "Test", "Test.h");
    // Make sure the private superclass is still declared in the header.
    assertTranslation(translation, "@interface Test_A");
  }

  public void testPublicClassInsidePrivateClass() throws IOException {
    String translation = translateSourceFile(
        "class Test { private static class A { public static class B {} } }", "Test", "Test.h");
    // "B" should not be public because it is inside the private class "A".
    assertNotInTranslation(translation, "Test_A_B");
  }

  public void testPrivateBaseClassExposedBySubclass() throws IOException {
    String translation = translateSourceFile(
        "class Test { private static class Base { protected int field; protected void method() {}"
        + " protected static void staticMethod() {} } public static class Foo extends Base {} }",
        "Test", "Test.h");
    assertTranslation(translation, "jint field_");
    assertTranslation(translation, "- (void)method;");
    assertTranslation(translation, "+ (void)staticMethod;");
    assertTranslation(translation, "FOUNDATION_EXPORT void Test_Base_staticMethod();");
  }
}
