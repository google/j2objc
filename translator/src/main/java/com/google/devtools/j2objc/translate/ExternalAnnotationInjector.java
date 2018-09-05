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

import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.types.GeneratedAnnotationMirror;
import com.google.devtools.j2objc.types.GeneratedAnnotationValue;
import com.google.devtools.j2objc.types.GeneratedElement;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedTypeElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.ExternalAnnotations;
import com.google.devtools.j2objc.util.Mappings;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import scenelib.annotations.Annotation;
import scenelib.annotations.el.AClass;
import scenelib.annotations.el.AElement;
import scenelib.annotations.el.AMethod;
import scenelib.annotations.el.AScene;
import scenelib.annotations.field.AnnotationFieldType;
import scenelib.annotations.field.EnumAFT;

/** Adds external annotations from an annotated AST to matching declarations in a J2ObjC AST. */
public final class ExternalAnnotationInjector extends UnitTreeVisitor {

  // Contains the external annotations.
  private final AScene annotatedAst;

  // While visiting the J2ObjC AST, the position in the annotated AST is maintained using this
  // stack.
  Deque<Optional<AElement>> annotatedElementStack = new ArrayDeque<>();

  public ExternalAnnotationInjector(CompilationUnit unit, ExternalAnnotations externalAnnotations) {
    super(unit);
    this.annotatedAst = externalAnnotations.getScene();
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    return visitAbstractTypeDeclaration(node);
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    return visitAbstractTypeDeclaration(node);
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    if (!annotatedElementStack.peekLast().isPresent()) {
      return false;
    }
    AClass annotatedParent = (AClass) annotatedElementStack.peekLast().get();
    ExecutableElement executable = node.getExecutableElement();
    String prefix = elementUtil.getBinaryName(ElementUtil.getDeclaringClass(executable)) + ".";
    String methodName = Mappings.getMethodKey(executable, typeUtil).substring(prefix.length());
    AMethod annotatedMethod = annotatedParent.methods.get(methodName);
    if (annotatedMethod != null) {
      Set<Annotation> annotations = new LinkedHashSet<>();
      annotations.addAll(annotatedMethod.tlAnnotationsHere);
      annotations.addAll(annotatedMethod.returnType.tlAnnotationsHere);
      injectAnnotationsToMethod(node, annotations);
    }
    return false;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    endVisitAbstractTypeDeclaration();
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    endVisitAbstractTypeDeclaration();
  }

  private boolean visitAbstractTypeDeclaration(AbstractTypeDeclaration node) {
    String elementName = elementUtil.getBinaryName(node.getTypeElement());
    AClass annotatedElement = annotatedAst.classes.get(elementName);
    if (annotatedElement != null && !annotatedElement.tlAnnotationsHere.isEmpty()) {
      injectAnnotationsToType(node, annotatedElement.tlAnnotationsHere);
    }
    annotatedElementStack.addLast(Optional.ofNullable(annotatedElement));
    return true;
  }

  private void endVisitAbstractTypeDeclaration() {
    annotatedElementStack.removeLast();
  }

  private void injectAnnotationsToType(AbstractTypeDeclaration node, Set<Annotation> annotations) {
    GeneratedTypeElement generatedElement = GeneratedTypeElement.mutableCopy(node.getTypeElement());
    injectAnnotationsToElement(generatedElement, annotations);
    node.setTypeElement(generatedElement);
  }

  private void injectAnnotationsToMethod(MethodDeclaration node, Set<Annotation> annotations) {
    ExecutableElement element = node.getExecutableElement();
    GeneratedExecutableElement generatedElement =
        GeneratedExecutableElement.mutableCopy(nameTable.getMethodSelector(element), element);
    injectAnnotationsToElement(generatedElement, annotations);
    node.setExecutableElement(generatedElement);
  }

  private void injectAnnotationsToElement(GeneratedElement element, Set<Annotation> annotations) {
    for (Annotation annotation : annotations) {
      element.addAnnotationMirror(generateAnnotationMirror(annotation));
    }
  }

  private GeneratedAnnotationMirror generateAnnotationMirror(Annotation annotation) {
    GeneratedAnnotationMirror annotationMirror = new GeneratedAnnotationMirror(annotation.def.name);
    for (Map.Entry<String, Object> entry : annotation.fieldValues.entrySet()) {
      String fieldName = entry.getKey();
      // For our uses cases, the scenelib library encodes the annotation value as a string.
      String fieldValue = (String) entry.getValue();
      AnnotationFieldType fieldType = annotation.def.fieldTypes.get(fieldName);
      AnnotationField field = generateAnnotationField(fieldType, fieldName, fieldValue);
      annotationMirror.addElementValue(field.element, field.value);
    }
    return annotationMirror;
  }

  private AnnotationField generateAnnotationField(
      AnnotationFieldType type, String name, String value) {
    AnnotationField field = new AnnotationField();
    if (type instanceof EnumAFT) {
      int index = value.lastIndexOf('.');
      String enumTypeString = value.substring(0, index);
      String enumValue = value.substring(index + 1);
      TypeMirror enumType = typeUtil.resolveJavaType(enumTypeString).asType();
      field.element =
          GeneratedExecutableElement.newMethodWithSelector(
              name, enumType, /* enclosingElement = */ null);
      field.value =
          new GeneratedAnnotationValue(
              GeneratedVariableElement.newParameter(
                  enumValue, enumType, /* enclosingElement = */ null));
    } else {
      ErrorUtil.error("Unsupported field type in external annotation: " + type);
    }
    return field;
  }

  private static class AnnotationField {
    ExecutableElement element;
    AnnotationValue value;
  }
}
