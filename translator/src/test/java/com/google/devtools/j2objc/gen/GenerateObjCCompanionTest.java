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

/** Tests for @GenerateObjCCompanion companion protocol and property generation. */
public class GenerateObjCCompanionTest extends GenerationTest {

  @Override
  public void setUp() throws IOException {
    super.setUp();
    options.setAsObjCGenericDecl(true);
  }

  public void testCompanionHeaderAndImplementation() throws IOException {
    String source =
        """
        @com.google.j2objc.annotations.GenerateObjCCompanion
        public class Foo {
          public static void doSomething() {}
        }
        """;
    String header = translateSourceFile(source, "Foo", "Foo.h");
    assertInTranslation(header, "@class Foo;");
    assertInTranslation(header, "@protocol FooCompanion");
    // The companion protocol method should be an instance method instead of class method
    assertInTranslation(header, "- (void)doSomething;");
    assertInTranslation(header, "@property (readonly, class) id<FooCompanion> companion;");

    String impl = translateSourceFile(source, "Foo", "Foo.m");
    assertInTranslation(impl, "+ (id<FooCompanion>)companion {");
    assertInTranslation(impl, "return (id<FooCompanion>)self;");
  }

  public void testGenericVariablesAreLost() throws IOException {
    String source =
        """
        @com.google.j2objc.annotations.GenerateObjCCompanion
        public class Bar<T> {
          public static <K> K doSomething(K arg) { return arg; }
        }
        """;
    String header = translateSourceFile(source, "Bar", "Bar.h");
    assertInTranslation(header, "@class Bar;");
    assertInTranslation(header, "@protocol BarCompanion");
    assertInTranslation(header, "- (id)doSomethingWithId:(id)arg;");
    assertInTranslation(header, "@property (readonly, class) id<BarCompanion> companion;");
  }

  public void testBoundGenericMethodTypesArePreserved() throws IOException {
    // Using ThreadLocal because it's a class and doesn't require an import.
    String source =
        """
        @com.google.j2objc.annotations.GenerateObjCCompanion
        public class Baz {
          public static ThreadLocal<String> identity(ThreadLocal<String> val) {
            return val; }
        }
        """;
    String header = translateSourceFile(source, "Baz", "Baz.h");
    assertInTranslation(header, "@class Baz;");
    assertInTranslation(header, "@protocol BazCompanion");
    assertInTranslation(
        header,
        "- (JavaLangThreadLocal<NSString *>"
            + " *)identityWithJavaLangThreadLocal:(JavaLangThreadLocal<NSString *> *)val;");
  }

  public void testCompanionPropertiesForStaticFields() throws IOException {
    options.setClassProperties(true);
    String source =
        """
        @com.google.j2objc.annotations.GenerateObjCCompanion
        public class Foo {
          public static final int CONSTANT_VALUE = 1;
          public static int mutableStaticField = 2;
          private static int privateStaticField = 3;
        }
        """;
    String header = translateSourceFile(source, "Foo", "Foo.h");
    assertInTranslation(header, "@protocol FooCompanion");
    // Should generate instance property for CONSTANT_VALUE in companion protocol.
    assertInTranslation(
        header, "@property (readonly) int32_t CONSTANT_VALUE NS_SWIFT_NAME(CONSTANT_VALUE);");
    // Should generate instance property for mutableStaticField in companion protocol.
    assertInTranslation(
        header, "@property int32_t mutableStaticField NS_SWIFT_NAME(mutableStaticField);");
    // Should NOT generate property for privateStaticField.
    assertNotInTranslation(header, "privateStaticField");
  }

  public void testCompanionWithOnlyStaticFields() throws IOException {
    options.setClassProperties(true);
    String source =
        """
        @com.google.j2objc.annotations.GenerateObjCCompanion
        public class Foo {
          public static final int CONSTANT_VALUE = 1;
        }
        """;
    // This should compile successfully without throwing "types without static methods" exception.
    String header = translateSourceFile(source, "Foo", "Foo.h");
    assertInTranslation(header, "@protocol FooCompanion");
    assertInTranslation(
        header, "@property (readonly) int32_t CONSTANT_VALUE NS_SWIFT_NAME(CONSTANT_VALUE);");
  }

  public void testInterfaceCompanion() throws IOException {
    options.setClassProperties(true);
    String source =
        """
        @com.google.j2objc.annotations.GenerateObjCCompanion
        public interface Foo {
          public static final int CONSTANT_VALUE = 1;
          public static void doSomething() {}
        }
        """;
    String header = translateSourceFile(source, "Foo", "Foo.h");
    assertInTranslation(header, "@protocol FooCompanion");
    assertInTranslation(
        header, "@property (readonly) int32_t CONSTANT_VALUE NS_SWIFT_NAME(CONSTANT_VALUE);");
    assertInTranslation(header, "- (void)doSomething;");
    assertInTranslation(header, "@interface Foo : NSObject");
    assertInTranslation(header, "@property (readonly, class) id<FooCompanion> companion;");

    String impl = translateSourceFile(source, "Foo", "Foo.m");
    assertInTranslation(impl, "@implementation Foo");
    assertInTranslation(impl, "+ (id<FooCompanion>)companion {");
    assertInTranslation(impl, "return (id<FooCompanion>)self;");
  }

  public void testCompanionPropertiesForPropertyMethods() throws IOException {
    String source =
        """
        import com.google.j2objc.annotations.Property;
        @com.google.j2objc.annotations.GenerateObjCCompanion
        public class Foo {
          @Property("readonly, nonnull")
          public static String getBar() {
            return "bar";
          }
        }
        """;
    String header = translateSourceFile(source, "Foo", "Foo.h");
    assertInTranslation(header, "@protocol FooCompanion");
    assertInTranslation(header, "@property (nonatomic, getter=getBar, readonly) NSString * bar;");
    assertInTranslation(
        header, "@property (class, nonatomic, getter=getBar, readonly) NSString * bar;");
  }
}

