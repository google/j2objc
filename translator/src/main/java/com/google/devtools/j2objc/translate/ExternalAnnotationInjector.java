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
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NormalAnnotation;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.types.GeneratedAnnotationMirror;
import com.google.devtools.j2objc.types.GeneratedAnnotationValue;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.ExternalAnnotations;
import com.google.devtools.j2objc.util.Mappings;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.afu.scenelib.Annotation;
import org.checkerframework.afu.scenelib.el.AClass;
import org.checkerframework.afu.scenelib.el.AElement;
import org.checkerframework.afu.scenelib.el.AField;
import org.checkerframework.afu.scenelib.el.AMethod;
import org.checkerframework.afu.scenelib.el.AScene;
import org.checkerframework.afu.scenelib.field.AnnotationFieldType;
import org.checkerframework.afu.scenelib.field.BasicAFT;
import org.checkerframework.afu.scenelib.field.EnumAFT;

/** Records external annotations from an annotated AST that match declarations in a J2ObjC AST. */
public final class ExternalAnnotationInjector extends UnitTreeVisitor {

  // Contains the external annotations.
  private final AScene annotatedAst;

  // While visiting the J2ObjC AST, the position in the annotated AST is maintained using this
  // stack.
  private final Deque<Optional<AElement>> annotatedElementStack = new ArrayDeque<>();

  // Contains any annotation types that cannot be resolved.
  private final List<String> missingAnnotationTypes = new ArrayList<>();

  public ExternalAnnotationInjector(CompilationUnit unit, ExternalAnnotations externalAnnotations) {
    super(unit);
    this.annotatedAst = externalAnnotations.getScene();
  }

  @Override
  public boolean visit(PackageDeclaration node) {
    PackageElement element = node.getPackageElement();
    String elementName = element.getQualifiedName() + ".package-info";
    AClass annotatedElement = annotatedAst.classes.get(elementName);
    if (annotatedElement != null) {
      recordAnnotations(element, annotatedElement.tlAnnotationsHere);
    }
    return false;
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    return visitAbstractTypeDeclaration(node);
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
      return true;
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
      recordAnnotations(node.getExecutableElement(), annotations);
      injectAnnotationsToNode(node, annotations);
    }
    return true;
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    if (!annotatedElementStack.peekLast().isPresent()) {
      return false;
    }
    AClass annotatedParent = (AClass) annotatedElementStack.peekLast().get();
    VariableElement element = node.getFragment().getVariableElement();
    AField annotatedField = annotatedParent.fields.get(ElementUtil.getName(element));
    if (annotatedField != null) {
      recordAnnotations(element, annotatedField.tlAnnotationsHere);
    }
    return false;
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    endVisitAbstractTypeDeclaration();
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
      recordAnnotations(node.getTypeElement(), annotatedElement.tlAnnotationsHere);
      injectAnnotationsToNode(node, annotatedElement.tlAnnotationsHere);
    }
    annotatedElementStack.addLast(Optional.ofNullable(annotatedElement));
    return true;
  }

  private void endVisitAbstractTypeDeclaration() {
    annotatedElementStack.removeLast();
  }

  private void recordAnnotations(AnnotatedConstruct construct, Set<Annotation> annotations) {
    for (Annotation annotation : annotations) {
      GeneratedAnnotationMirror annotationMirror = generateAnnotationMirror(annotation);
      if (annotationMirror != null) {
        ExternalAnnotations.add(construct, annotationMirror);
      }
    }
  }

  private void injectAnnotationsToNode(BodyDeclaration declaration, Set<Annotation> annotations) {
    for (Annotation annotation : annotations) {
      NormalAnnotation newAnnotation = new NormalAnnotation();
      AnnotationMirror annotationMirror = generateAnnotationMirror(annotation);
      if (annotationMirror != null) {
        newAnnotation.setAnnotationMirror(annotationMirror);
        newAnnotation.setTypeName(new SimpleName(annotationMirror.getAnnotationType().asElement()));
        declaration.addAnnotation(newAnnotation);
      }
    }
  }

  private void reportNoSuchClass(Annotation annotation) {
    String className = annotation.def.name;
    if (!missingAnnotationTypes.contains(className)) {
      missingAnnotationTypes.add(className);
      ErrorUtil.error(
          "cannot find symbol: " + className + "referenced in " + annotation.def.source);
    }
  }

  private GeneratedAnnotationMirror generateAnnotationMirror(Annotation annotation) {
    TypeElement element = typeUtil.resolveJavaType(annotation.def.name);
    if (element == null) {
      reportNoSuchClass(annotation);
      return null;
    }
    DeclaredType type = (DeclaredType) element.asType();
    GeneratedAnnotationMirror annotationMirror = new GeneratedAnnotationMirror(type);
    for (Map.Entry<String, Object> entry : annotation.fieldValues.entrySet()) {
      String fieldName = entry.getKey();
      // For our uses cases, the scenelib library encodes the annotation value as a string.
      String fieldValue = (String) entry.getValue();
      AnnotationFieldType fieldType = annotation.def.fieldTypes.get(fieldName);
      AnnotationField field = generateAnnotationField(annotation, fieldType, fieldName, fieldValue);
      annotationMirror.addElementValue(field.element, field.value);
    }
    return annotationMirror;
  }

  private AnnotationField generateAnnotationField(
      Annotation annotation, AnnotationFieldType type, String name, String value) {
    AnnotationField field = new AnnotationField();
    if (type instanceof BasicAFT) {
      Class<?> enclosedType = ((BasicAFT) type).type;
      if (String.class.isAssignableFrom(enclosedType)) {
        field.element = GeneratedExecutableElement
            .newMethodWithSelector(name, typeUtil.getJavaString().asType(), null);
        field.value = new GeneratedAnnotationValue(value);
      } else {
        ErrorUtil.error("ExternalAnnotationInjector: unsupported field type " + type);
      }
    } else if (type instanceof EnumAFT) {
      String enumTypeString = annotation.def.name + "." + ((EnumAFT) type).typeName;
      TypeMirror enumType = typeUtil.resolveJavaType(enumTypeString).asType();
      field.element =
          GeneratedExecutableElement.newMethodWithSelector(
              name, enumType, /* enclosingElement = */ null);
      field.value =
          new GeneratedAnnotationValue(
              GeneratedVariableElement.newParameter(
                  value, enumType, /* enclosingElement = */ null));
    } else {
      ErrorUtil.error("ExternalAnnotationInjector: unsupported field type " + type);
    }
    return field;
  }

  private static class AnnotationField {
    ExecutableElement element;
    AnnotationValue value;
  }
}
