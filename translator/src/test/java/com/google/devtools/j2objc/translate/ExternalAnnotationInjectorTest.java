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
 * External annotations are provided in the Java annotation index file format
 * (https://types.cs.washington.edu/dev/annotation-file-utilities/annotation-file-format.html).
 */
public class ExternalAnnotationInjectorTest extends GenerationTest {

  // In order to test different paths, the non-null version is a type annotation and the nullable
  // version is a declaration annotation.
  private static final String EXTERNAL_NULLABILITY_ANNOTATIONS =
      "package p: "
          + "annotation @NonNull: @java.lang.annotation.Target(value={TYPE_USE}) "
          + "annotation @Nullable: "
          + "class Test: "
          + "  method foo()Ljava/lang/String;:"
          + "    return: @p.NonNull"
          + "  method bar()Ljava/lang/String;: @p.Nullable"
          // An annotated method not present in the Java source should not cause problems. Note that
          // method qux is not declared in the test cases.
          + "  method qux()Ljava/lang/String;:"
          + "    return: @p.Nullable";

  public void testInjectNullability_returnType_instanceMethod() throws IOException {
    options.setNullability(true);
    options.addExternalAnnotationFileContents(EXTERNAL_NULLABILITY_ANNOTATIONS);
    String source =
        "package p;"
            + "public class Test { "
            + "  public String foo() { return null; } "
            + "  public String bar() { return null; } "
            + "  public String baz() { return null; } " // no external annotation.
            + "}";
    String translation = translateSourceFile(source, "p.Test", "p/Test.h");
    assertTranslation(translation, "- (NSString * __nonnull)foo;");
    assertTranslation(translation, "- (NSString * __nullable)bar;");
    assertTranslation(translation, "- (NSString *)baz;");
    assertNotInTranslation(translation, "qux");
  }

  public void testInjectNullability_returnType_classMethod() throws IOException {
    options.setNullability(true);
    options.addExternalAnnotationFileContents(EXTERNAL_NULLABILITY_ANNOTATIONS);
    String source =
        "package p;"
            + "public class Test { "
            + "  public static String foo() { return null; } "
            + "  public static String bar() { return null; } "
            + "}";
    String translation = translateSourceFile(source, "p.Test", "p/Test.h");
    assertTranslation(translation, "+ (NSString * __nonnull)foo;");
    assertTranslation(translation, "+ (NSString * __nullable)bar;");
  }

  public void testInjectNullability_returnType_enumMethod() throws IOException {
    options.setNullability(true);
    options.addExternalAnnotationFileContents(EXTERNAL_NULLABILITY_ANNOTATIONS);
    String source =
        "package p;"
            + "public enum Test { "
            + " ONE, TWO, THREE; "
            + "    public String foo() { return null; } "
            + "    public String bar() { return null; } "
            + "}";
    String translation = translateSourceFile(source, "p.Test", "p/Test.h");
    assertTranslation(translation, "- (NSString * __nonnull)foo;");
    assertTranslation(translation, "- (NSString * __nullable)bar;");
  }

  public void testInjectNullability_returnType_interfaceMethod() throws IOException {
    options.setNullability(true);
    options.addExternalAnnotationFileContents(EXTERNAL_NULLABILITY_ANNOTATIONS);
    String source =
        "package p;"
            + "public interface Test { "
            + "    String foo(); "
            + "    default String bar() { return null; } "
            + "}";
    String translation = translateSourceFile(source, "p.Test", "p/Test.h");
    assertTranslation(translation, "- (NSString * __nonnull)foo;");
    assertTranslation(translation, "- (NSString * __nullable)bar;");
  }

  public void testInjectNullability_returnType_nestedClassMethod() throws IOException {
    options.setNullability(true);
    String externalNullabilityAnnotations =
        "package p: "
            + "annotation @NonNull: "
            + "annotation @Nullable: "
            + "class Test$StaticNestedClass: "
            + "  method foo()Ljava/lang/String;:"
            + "    return: @p.NonNull "
            + "class Test$InnerClass: "
            + "  method bar()Ljava/lang/String;:"
            + "    return: @p.Nullable";
    options.addExternalAnnotationFileContents(externalNullabilityAnnotations);
    String source =
        "package p;"
            + "public class Test { "
            + "  public static class StaticNestedClass { "
            + "    public String foo() { return null; } "
            + "  } "
            + "  public class InnerClass { "
            + "    public String bar() { return null; } "
            + "  }"
            + "}";
    String translation = translateSourceFile(source, "p.Test", "p/Test.h");
    assertTranslation(translation, "- (NSString * __nonnull)foo;");
    assertTranslation(translation, "- (NSString * __nullable)bar;");
  }

  // Nullability specifiers should not be applied to primitive types.
  public void testInjectNullability_returnType_primitiveType() throws IOException {
    options.setNullability(true);
    String externalNullabilityAnnotations =
        "package p: "
            + "annotation @NonNull: "
            + "class Test: "
            + "  method foo()Z:"
            + "    return: @p.NonNull ";
    options.addExternalAnnotationFileContents(externalNullabilityAnnotations);
    String source =
        "package p;"
            + "public class Test { "
            + "  public boolean foo() { return true; } "
            + "}";
    String translation = translateSourceFile(source, "p.Test", "p/Test.h");
    assertTranslation(translation, "- (jboolean)foo;");
  }

  // Verify that visiting a constructor does not affect the generated code.
  public void testVisitingConstructor() throws IOException {
    options.setNullability(true);
    String externalNullabilityAnnotations =
        "package p: "
            + "annotation @AnAnnotation: "
            + "class Test: "
            + "  method <init>()V: @p.AnAnnotation";
    options.addExternalAnnotationFileContents(externalNullabilityAnnotations);
    String source = "package p; public class Test { public Test() {} }";
    String translation = translateSourceFile(source, "p.Test", "p/Test.h");
    assertTranslation(translation, "- (instancetype __nonnull)init;");
    assertTranslation(translation, "FOUNDATION_EXPORT void PTest_init(PTest *self);");
    assertTranslation(
        translation, "FOUNDATION_EXPORT PTest *new_PTest_init(void) NS_RETURNS_RETAINED;");
    assertTranslation(translation, "FOUNDATION_EXPORT PTest *create_PTest_init(void);");
  }

  // Verify that visited methods generate the expected forward declarations.
  public void testForwardDeclaration() throws IOException {
    options.setNullability(true);
    String externalNullabilityAnnotations =
        "package p: "
            + "annotation @AnAnnotation: "
            + "class Test: "
            + "  method foo(Ljava/lang/Thread;)V: @p.AnAnnotation";
    options.addExternalAnnotationFileContents(externalNullabilityAnnotations);
    String source =
        "package p;"
            + "public class Test { "
            + "  public void foo(Thread t) {} "
            + "  public void bar(ThreadGroup t) {} " // no external annotation.
            + "}";
    String translation = translateSourceFile(source, "p.Test", "p/Test.h");
    assertTranslation(translation, "@class JavaLangThread;");
    assertTranslation(translation, "@class JavaLangThreadGroup;");
  }

  // TODO(user): This visitor replaces the original ExecutableElement with a
  // GeneratedExecutableElement to inject annotations. The Functionizer visitor is not able to
  // match method invocations (ExecutableElements) with annotated method declarations
  // (GeneratedExecutableElement).
  /*public void testFunctionizer() throws IOException {
    options.setNullability(true);
    String externalNullabilityAnnotations =
        "package p: "
            + "annotation @AnAnnotation: "
            + "class Test: "
            + "  method foo()V: @p.AnAnnotation";
    options.addExternalAnnotationFileContents(externalNullabilityAnnotations);
    String source =
        "package p;"
            + "public class Test { "
            + "  public final void foo() {} "
            + "  public void bar() { foo(); } "
            + "}";
    String translation = translateSourceFile(source, "p.Test", "p/Test.m");
    assertTranslation(translation, "__attribute__((unused)) static void PTest_foo(PTest *self);");
    assertTranslatedLines(translation, "- (void)bar {", "  PTest_foo(self);", "}");
  }*/

  private static final String REFLECTION_SUPPORT_ANNOTATION =
      "package com.google.j2objc.annotations: "
          + "annotation @ReflectionSupport: "
          + "  enum com.google.j2objc.annotations.ReflectionSupport.Level value ";

  public void testInjectReflectionSupport_type_keepReflection() throws IOException {
    String externalReflectionSupportAnnotations =
        REFLECTION_SUPPORT_ANNOTATION
            + "package p: "
            + "class Test: "
            + "  @com.google.j2objc.annotations.ReflectionSupport( "
            + "    com.google.j2objc.annotations.ReflectionSupport.Level.FULL) ";
    options.addExternalAnnotationFileContents(externalReflectionSupportAnnotations);
    options.setStripReflection(true);
    String source = "package p; public class Test {}";
    String translation = translateSourceFile(source, "p.Test", "p/Test.m");
    assertTranslation(translation, "__metadata");
  }

  public void testInjectReflectionSupport_type_stripReflection() throws IOException {
    String externalReflectionSupportAnnotations =
        REFLECTION_SUPPORT_ANNOTATION
            + "package p: "
            + "class Test: "
            + "  @com.google.j2objc.annotations.ReflectionSupport( "
            + "    com.google.j2objc.annotations.ReflectionSupport.Level.NATIVE_ONLY) ";
    options.addExternalAnnotationFileContents(externalReflectionSupportAnnotations);
    options.setStripReflection(false);
    String source = "package p; public class Test {}";
    String translation = translateSourceFile(source, "p.Test", "p/Test.m");
    assertNotInTranslation(translation, "__metadata");
  }

}
