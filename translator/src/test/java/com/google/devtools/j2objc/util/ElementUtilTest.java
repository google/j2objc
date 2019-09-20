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
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

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
}
