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
    assertInTranslation(header, "@protocol FooCompanionProtocol");
    // The companion protocol method should be an instance method instead of class method
    assertInTranslation(header, "- (void)doSomething;");
    assertInTranslation(header, "@property (readonly, class) id<FooCompanionProtocol> companion;");

    String impl = translateSourceFile(source, "Foo", "Foo.m");
    assertInTranslation(impl, "+ (id<FooCompanionProtocol>)companion {");
    assertInTranslation(impl, "return (id<FooCompanionProtocol>)self;");
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
    assertInTranslation(header, "@protocol BarCompanionProtocol");
    assertInTranslation(header, "- (id)doSomethingWithId:(id)arg;");
    assertInTranslation(header, "@property (readonly, class) id<BarCompanionProtocol> companion;");
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
    assertInTranslation(header, "@protocol BazCompanionProtocol");
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
    assertInTranslation(header, "@protocol FooCompanionProtocol");
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
    assertInTranslation(header, "@protocol FooCompanionProtocol");
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
    assertInTranslation(header, "@protocol FooCompanionProtocol");
    assertInTranslation(
        header, "@property (readonly) int32_t CONSTANT_VALUE NS_SWIFT_NAME(CONSTANT_VALUE);");
    assertInTranslation(header, "- (void)doSomething;");
    assertInTranslation(header, "@interface FooCompanion : NSObject");
    assertInTranslation(header, "@property (readonly, class) id<FooCompanionProtocol> shared;");

    String impl = translateSourceFile(source, "Foo", "Foo.m");
    assertInTranslation(impl, "@implementation FooCompanion");
    assertInTranslation(impl, "+ (id<FooCompanionProtocol>)shared {");
    assertInTranslation(impl, "return (id<FooCompanionProtocol>)self;");
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
    assertTranslatedLines(
        header,
        """
        @protocol FooCompanionProtocol


        @property (readonly, copy, nonatomic, getter=getBar, nonnull) NSString *bar;

        @end
        """);
    assertTranslatedLines(
        header,
        """
        @interface Foo : NSObject

        #pragma clang diagnostic push
        #pragma clang diagnostic ignored "-Wincompatible-property-type"
        @property (readonly, class) id<FooCompanionProtocol> companion;
        #pragma clang diagnostic pop

        #pragma mark Public

        - (instancetype)init;


        @property (readonly, copy, nonatomic, getter=getBar, class, nonnull) NSString *bar;

        @end
        """);
  }

  public void testGeneratedTypeWithoutStaticMembersIgnored() throws IOException {
    String source =
        """
        import com.google.j2objc.annotations.GenerateObjCCompanion;
        import javax.annotation.processing.Generated;
        @Generated("com.google.auto.value.processor.AutoValueProcessor")
        @GenerateObjCCompanion
        public class AutoValue_Foo {
          public int value() {
            return 1;
          }
        }
        """;
    String header = translateSourceFile(source, "AutoValue_Foo", "AutoValue_Foo.h");
    assertInTranslation(header, "@interface AutoValue_Foo : NSObject");
    assertNotInTranslation(header, "AutoValue_FooCompanionProtocol");
  }

  public void testInterfaceCompanionWithOnlyStaticFields() throws IOException {
    options.setClassProperties(true);
    String source =
        """
        @com.google.j2objc.annotations.GenerateObjCCompanion
        public interface Foo {
          public static final int CONSTANT_VALUE = 1;
        }
        """;
    String header = translateSourceFile(source, "Foo", "Foo.h");
    assertInTranslation(header, "@protocol FooCompanionProtocol");
    assertInTranslation(
        header, "@property (readonly) int32_t CONSTANT_VALUE NS_SWIFT_NAME(CONSTANT_VALUE);");
    assertInTranslation(header, "@interface FooCompanion : NSObject");
    assertInTranslation(header, "@property (readonly, class) id<FooCompanionProtocol> shared;");

    String impl = translateSourceFile(source, "Foo", "Foo.m");
    assertInTranslation(impl, "@implementation FooCompanion");
    assertInTranslation(impl, "+ (id<FooCompanionProtocol>)shared {");
    assertInTranslation(impl, "return (id<FooCompanionProtocol>)self;");
  }

  // J2KT translates classes with only static members and private, never invoked constructors as
  // Kotlin objects, which are exposed to Objective-C via `shared` rather than `companion`.
  public void testSharedPropertyForStaticOnlyClass() throws IOException {
    String source =
        """
        @com.google.j2objc.annotations.GenerateObjCCompanion
        public class Foo {
          private Foo() {}
          public static void doSomething() {}
        }
        """;
    String header = translateSourceFile(source, "Foo", "Foo.h");
    assertTranslatedLines(
        header,
        """
        #pragma clang diagnostic push
        #pragma clang diagnostic ignored "-Wincompatible-property-type"
        @property (readonly, class) id<FooCompanionProtocol> companion;
        @property (readonly, class) id<FooCompanionProtocol> shared;
        #pragma clang diagnostic pop
        """);

    String impl = translateSourceFile(source, "Foo", "Foo.m");
    assertTranslatedLines(
        impl,
        """
        + (id<FooCompanionProtocol>)companion {
          return (id<FooCompanionProtocol>)self;
        }

        + (id<FooCompanionProtocol>)shared {
          return (id<FooCompanionProtocol>)self;
        }
        """);
  }

  public void testNoSharedPropertyForClassWithImplicitConstructor() throws IOException {
    String source =
        """
        @com.google.j2objc.annotations.GenerateObjCCompanion
        public class Foo {
          public static void doSomething() {}
        }
        """;
    assertNoSharedProperty(source);
  }

  // An instance initializer is dead code in a class that is never instantiated: J2KT drops it from
  // the object, and J2ObjC has folded it into the (private, never invoked) constructor by now.
  public void testSharedPropertyForClassWithInstanceInitializer() throws IOException {
    String source =
        """
        @com.google.j2objc.annotations.GenerateObjCCompanion
        public class Foo {
          public static int count = 0;
          { count++; }
          private Foo() {}
          public static void doSomething() {}
        }
        """;
    String header = translateSourceFile(source, "Foo", "Foo.h");
    assertInTranslation(header, "@property (readonly, class) id<FooCompanionProtocol> shared;");
  }

  public void testNoSharedPropertyForClassWithInstanceMembers() throws IOException {
    String source =
        """
        @com.google.j2objc.annotations.GenerateObjCCompanion
        public class Foo {
          private Foo() {}
          public static void doSomething() {}
          public void doSomethingElse() {}
        }
        """;
    assertNoSharedProperty(source);
  }

  public void testNoSharedPropertyForInstantiatedClass() throws IOException {
    String source =
        """
        @com.google.j2objc.annotations.GenerateObjCCompanion
        public class Foo {
          private static final Object INSTANCE = new Foo();
          private Foo() {}
          public static void doSomething() {}
        }
        """;
    assertNoSharedProperty(source);
  }

  public void testNoSharedPropertyForSubclassedClass() throws IOException {
    String source =
        """
        @com.google.j2objc.annotations.GenerateObjCCompanion
        public class Foo {
          private Foo() {}
          public static void doSomething() {}
          static class Bar extends Foo {}
        }
        """;
    assertNoSharedProperty(source);
  }

  public void testNoSharedPropertyForClassWithSuperTypes() throws IOException {
    String source =
        """
        @com.google.j2objc.annotations.GenerateObjCCompanion
        public class Foo implements Runnable {
          private Foo() {}
          public static void doSomething() {}
          public void run() {}
        }
        """;
    assertNoSharedProperty(source);
  }

  public void testNoSharedPropertyForKtNativeClassWithCompanionName() throws IOException {
    addSourceFile(
        """
        package javaemul.internal.annotations;
        public @interface KtNative {
          String name() default "<auto>";
          String companionName() default "<auto>";
        }
        """,
        "javaemul/internal/annotations/KtNative.java");
    String source =
        """
        @com.google.j2objc.annotations.GenerateObjCCompanion
        @javaemul.internal.annotations.KtNative(companionName = "foo.FooCompanion")
        public class Foo {
          private Foo() {}
          public static native void doSomething();
        }
        """;
    assertNoSharedProperty(source);
  }

  public void testNoSharedPropertyForClassWithInstanceField() throws IOException {
    String source =
        """
        @com.google.j2objc.annotations.GenerateObjCCompanion
        public class Foo {
          private static final Object INSTANCE = new Object();
          private Foo() {}
          public static void doSomething() {}
        }
        """;
    assertNoSharedProperty(source);
  }

  public void testNoSharedPropertyForNativeJsType() throws IOException {
    addSourceFile(
        """
        package jsinterop.annotations;
        public @interface JsType {
          boolean isNative() default false;
        }
        """,
        "jsinterop/annotations/JsType.java");
    String source =
        """
        @com.google.j2objc.annotations.GenerateObjCCompanion
        @jsinterop.annotations.JsType(isNative = true)
        public class Foo {
          private Foo() {}
          public static native void doSomething();
        }
        """;
    assertNoSharedProperty(source);
  }

  private void assertNoSharedProperty(String source) throws IOException {
    String header = translateSourceFile(source, "Foo", "Foo.h");
    assertInTranslation(header, "@property (readonly, class) id<FooCompanionProtocol> companion;");
    assertNotInTranslation(header, "id<FooCompanionProtocol> shared;");

    String impl = translateSourceFile(source, "Foo", "Foo.m");
    assertInTranslation(impl, "+ (id<FooCompanionProtocol>)companion {");
    assertNotInTranslation(impl, "+ (id<FooCompanionProtocol>)shared {");
  }
}


