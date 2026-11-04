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

package com.google.devtools.j2objc.util;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.j2objc.annotations.ObjectiveCName;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

/**
 * UnitTests for the {@link ElementUtil} class.
 *
 * @author Keith Stanger
 */
public class ElementUtilTest extends GenerationTest {

  @Override
  protected void setUp() throws IOException {
    super.setUp();
    addSourceFile(String.join("\n",
        "@interface DefaultRetentionAnnotation {",
        "  String name();",
        "}"),
        "DefaultRetentionAnnotation.java");
    addSourceFile(String.join("\n",
        "import java.lang.annotation.*;",
        "@Retention(RetentionPolicy.CLASS)",
        "@interface ClassRetentionAnnotation {}"),
        "ClassRetentionAnnotation.java");
  }

  public void testIsRuntimeAnnotation() throws IOException {
    // SuppressWarnings is a source-level annotation.
    CompilationUnit unit =
        translateType("Example", "@SuppressWarnings(\"test\") class Example {}");
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    Annotation annotation = decl.getAnnotations().get(0);
    assertFalse(ElementUtil.isRuntimeAnnotation(annotation.getAnnotationMirror()));

    // Deprecated is a runtime annotation.
    unit = translateType("Example", "@Deprecated class Example {}");
    decl = unit.getTypes().get(0);
    annotation = decl.getAnnotations().get(0);
    assertTrue(ElementUtil.isRuntimeAnnotation(annotation.getAnnotationMirror()));

    // Check class annotation.
    unit = translateType("Example", "@ClassRetentionAnnotation class Example {}");
    decl = unit.getTypes().get(0);
    annotation = decl.getAnnotations().get(0);
    assertFalse(ElementUtil.isRuntimeAnnotation(annotation.getAnnotationMirror()));

    // Check default retention, also class.
    unit =
        translateType("Example", "@DefaultRetentionAnnotation(name = \"foo\") class Example {}");
    decl = unit.getTypes().get(0);
    annotation = decl.getAnnotations().get(0);
    assertFalse(ElementUtil.isRuntimeAnnotation(annotation.getAnnotationMirror()));
  }

  public void testIsGeneratedAnnotation() throws IOException {
    // SuppressWarnings is a source-level annotation.
    CompilationUnit unit =
        translateType("Example", "@SuppressWarnings(\"test\") class Example {}");
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    Annotation annotation = decl.getAnnotations().get(0);
    assertFalse(ElementUtil.isGeneratedAnnotation(annotation.getAnnotationMirror()));

    // Deprecated is a runtime annotation.
    unit = translateType("Example", "@Deprecated class Example {}");
    decl = unit.getTypes().get(0);
    annotation = decl.getAnnotations().get(0);
    assertTrue(ElementUtil.isGeneratedAnnotation(annotation.getAnnotationMirror()));

    // Check class annotation.
    unit = translateType("Example", "@ClassRetentionAnnotation class Example {}");
    decl = unit.getTypes().get(0);
    annotation = decl.getAnnotations().get(0);
    assertTrue(ElementUtil.isGeneratedAnnotation(annotation.getAnnotationMirror()));

    // Check default retention, also class.
    unit =
        translateType("Example", "@DefaultRetentionAnnotation(name = \"foo\") class Example {}");
    decl = unit.getTypes().get(0);
    annotation = decl.getAnnotations().get(0);
    assertTrue(ElementUtil.isGeneratedAnnotation(annotation.getAnnotationMirror()));
  }

  public void testGetAnnotation() throws IOException {
    CompilationUnit unit = translateType("Example",
        "@com.google.j2objc.annotations.ObjectiveCName(\"E\") class Example {}");
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    TypeElement element = decl.getTypeElement();
    AnnotationMirror annotation = ElementUtil.getAnnotation(element, ObjectiveCName.class);
    assertEquals("com.google.j2objc.annotations.ObjectiveCName",
        annotation.getAnnotationType().toString());
  }

  public void testGetAnnotationValue() throws IOException {
    CompilationUnit unit = translateType("Example",
        "@com.google.j2objc.annotations.ObjectiveCName(\"E\") class Example {}");
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    TypeElement element = decl.getTypeElement();
    AnnotationMirror annotation = ElementUtil.getAnnotation(element, ObjectiveCName.class);
    Object value = ElementUtil.getAnnotationValue(annotation, "value");
    assertEquals("E", value);
  }

  public void testHasAnnotation() throws IOException {
    CompilationUnit unit = translateType("Example",
        "@com.google.j2objc.annotations.ObjectiveCName(\"E\") class Example {}");
    AbstractTypeDeclaration decl = unit.getTypes().get(0);
    TypeElement element = decl.getTypeElement();
    assertTrue(ElementUtil.hasAnnotation(element, ObjectiveCName.class));
  }

  // Verify nullability annotation on a method with a void return type is ignored.
  public void testNullableAnnotationOnVoidMethod() throws IOException {
    String translation = translateSourceFile(
        "class Test { @javax.annotation.Nullable void foo() {} }", "Test", "Test.m");
    assertInTranslation(translation, "- (void)foo"); // not (void _Nullable)foo
  }

  private static final String NULLABILITY_SOURCE =
      """
      import java.util.List;
      import org.jspecify.annotations.Nullable;

      public class %s<T> {
        public class Inner<U> {}

        public List<@Nullable String> listOfNullable;
        public @Nullable List<String> nullableList;
        public List<? extends @Nullable String> wildcardOfNullable;
        public @Nullable String[] arrayOfNullable;
        public String @Nullable [] nullableArray;
        public @Nullable %<s<String>.Inner<Integer> enclosingNullable;
        public %<s<@Nullable String>.Inner<Integer> outerArgNullable;
        public %<s<String>.@Nullable Inner<Integer> innerNullable;

        public <V> void method(
            @Nullable V nullableTypeVar, List<@Nullable V> listOfNullableTypeVar) {}
      }
      """;

  public void testHasNullableAnnotationOnlyConsidersTopLevelType_source() throws IOException {
    CompilationUnit unit =
        translateType("FromSource", String.format(NULLABILITY_SOURCE, "FromSource"));
    assertTopLevelNullability(unit.getTypes().get(0).getTypeElement());
  }

  public void testHasNullableAnnotationOnlyConsidersTopLevelType_classFile() throws IOException {
    createClassFile("FromClassFile", String.format(NULLABILITY_SOURCE, "FromClassFile"));
    CompilationUnit unit = translateType("Client", "class Client { FromClassFile<String> ref; }");
    assertTopLevelNullability(unit.getEnv().elementUtil().getTypeElement("FromClassFile"));
  }

  private static void assertTopLevelNullability(TypeElement type) {
    Map<String, VariableElement> vars = new HashMap<>();
    for (VariableElement field : ElementFilter.fieldsIn(type.getEnclosedElements())) {
      vars.put(field.getSimpleName().toString(), field);
    }
    for (ExecutableElement method : ElementFilter.methodsIn(type.getEnclosedElements())) {
      for (VariableElement param : method.getParameters()) {
        vars.put(param.getSimpleName().toString(), param);
      }
    }
    assertFalse(ElementUtil.hasNullableAnnotation(vars.get("listOfNullable")));
    assertTrue(ElementUtil.hasNullableAnnotation(vars.get("nullableList")));
    assertFalse(ElementUtil.hasNullableAnnotation(vars.get("wildcardOfNullable")));
    assertFalse(ElementUtil.hasNullableAnnotation(vars.get("arrayOfNullable")));
    assertTrue(ElementUtil.hasNullableAnnotation(vars.get("nullableArray")));
    assertFalse(ElementUtil.hasNullableAnnotation(vars.get("enclosingNullable")));
    assertFalse(ElementUtil.hasNullableAnnotation(vars.get("outerArgNullable")));
    assertTrue(ElementUtil.hasNullableAnnotation(vars.get("innerNullable")));
    assertTrue(ElementUtil.hasNullableAnnotation(vars.get("nullableTypeVar")));
    assertFalse(ElementUtil.hasNullableAnnotation(vars.get("listOfNullableTypeVar")));
  }
}
