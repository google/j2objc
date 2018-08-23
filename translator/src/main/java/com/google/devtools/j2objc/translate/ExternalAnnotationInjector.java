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
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ExternalAnnotations;
import com.google.devtools.j2objc.util.Mappings;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import scenelib.annotations.Annotation;
import scenelib.annotations.el.AClass;
import scenelib.annotations.el.AElement;
import scenelib.annotations.el.AMethod;
import scenelib.annotations.el.AScene;

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
      injectAnnotations(node, annotations);
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
    annotatedElementStack.addLast(Optional.ofNullable(annotatedElement));
    return true;
  }

  private void endVisitAbstractTypeDeclaration() {
    annotatedElementStack.removeLast();
  }

  private void injectAnnotations(MethodDeclaration node, Set<Annotation> annotations) {
    ExecutableElement element = node.getExecutableElement();
    GeneratedExecutableElement generatedElement =
        GeneratedExecutableElement.mutableCopy(nameTable.getMethodSelector(element), element);
    for (Annotation externalAnnotation : annotations) {
      generatedElement.addAnnotationMirror(
          new GeneratedAnnotationMirror(externalAnnotation.def.name));
    }
    node.setExecutableElement(generatedElement);
  }
}
