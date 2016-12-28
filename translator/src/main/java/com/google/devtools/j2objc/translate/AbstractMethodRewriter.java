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
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

/**
 * Checks for missing methods that would cause an ObjC compilation error. Adds stubs for existing
 * abstract methods. Adds the ABSTRACT bit to a MethodDeclaration node if the method is a
 * non-default one from an interface.
 *
 * @author Tom Ball, Keith Stanger
 */
public class AbstractMethodRewriter extends UnitTreeVisitor {

  private final CodeReferenceMap deadCodeMap;

  public AbstractMethodRewriter(CompilationUnit unit, CodeReferenceMap deadCodeMap) {
    super(unit);
    this.deadCodeMap = deadCodeMap;
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    ExecutableElement methodElement = node.getExecutableElement();
    if (!ElementUtil.isAbstract(methodElement)) {
      return;
    }

    // JDT only adds the abstract bit to a MethodDeclaration node's modifiers if the abstract
    // method is from a class. Since we want our code generator to go over an interface's
    // method nodes for default method support and skip abstract methods, we add the bit if the
    // method is from an interface.
    TypeElement declaringClass = ElementUtil.getDeclaringClass(methodElement);
    if (declaringClass.getKind().isInterface()) {
      node.addModifiers(java.lang.reflect.Modifier.ABSTRACT);
      return;
    }

    // There's no need to stub out an abstract method for an interface's companion class.
    // Similarly, if this is an abstract method in a class and there's no need for reflection,
    // we skip the stubbing out.
    if (!translationUtil.needsReflection(declaringClass)) {
      unit.setHasIncompleteProtocol();
      unit.setHasIncompleteImplementation();
      return;
    }

    Block body = new Block();
    // Generate a body which throws a NSInvalidArgumentException.
    String bodyCode = "// can't call an abstract method\n"
        + "[self doesNotRecognizeSelector:_cmd];";
    if (!TypeUtil.isVoid(node.getReturnType().getTypeMirror())) {
      bodyCode += "\nreturn 0;"; // Never executes, but avoids a gcc warning.
    }
    body.addStatement(new NativeStatement(bodyCode));
    node.setBody(body);
    node.removeModifiers(java.lang.reflect.Modifier.ABSTRACT);
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    visitType(node);
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    visitType(node);
  }

  @Override
  public void endVisit(AnnotationTypeDeclaration node) {
    visitType(node);
  }

  private void visitType(AbstractTypeDeclaration node) {
    addReturnTypeNarrowingDeclarations(node);
  }

  // Adds declarations for any methods where the known return type is more
  // specific than what is already declared in inherited types.
  private void addReturnTypeNarrowingDeclarations(AbstractTypeDeclaration node) {
    TypeElement type = node.getTypeElement();

    // No need to run this if the entire class is dead.
    if (deadCodeMap != null && deadCodeMap.containsClass(type, elementUtil)) {
      return;
    }

    Map<String, ExecutablePair> newDeclarations = new HashMap<>();
    Map<String, TypeMirror> resolvedReturnTypes = new HashMap<>();
    for (DeclaredType inheritedType : typeUtil.getObjcOrderedInheritedTypes(type.asType())) {
      TypeElement inheritedElem = (TypeElement) inheritedType.asElement();
      for (ExecutableElement methodElem : ElementUtil.getMethods(inheritedElem)) {
        if (ElementUtil.isPrivate(methodElem)) {
          continue;
        }
        TypeMirror declaredReturnType = typeUtil.erasure(methodElem.getReturnType());
        if (!TypeUtil.isReferenceType(declaredReturnType)) {
          continue;  // Short circuit
        }
        String selector = nameTable.getMethodSelector(methodElem);
        ExecutableType methodType = typeUtil.asMemberOf(inheritedType, methodElem);
        TypeMirror returnType = typeUtil.erasure(methodType.getReturnType());
        TypeMirror resolvedReturnType = resolvedReturnTypes.get(selector);
        if (resolvedReturnType == null) {
          resolvedReturnType = declaredReturnType;
          resolvedReturnTypes.put(selector, resolvedReturnType);
        } else if (!typeUtil.isSubtype(returnType, resolvedReturnType)) {
          continue;
        }
        if (resolvedReturnType != returnType
            && !nameTable.getObjCType(resolvedReturnType).equals(
                nameTable.getObjCType(returnType))) {
          newDeclarations.put(selector, new ExecutablePair(methodElem, methodType));
          resolvedReturnTypes.put(selector, returnType);
        }
      }
    }

    for (Map.Entry<String, ExecutablePair> newDecl : newDeclarations.entrySet()) {
      if (deadCodeMap != null
          && deadCodeMap.containsMethod(newDecl.getValue().element(), typeUtil)) {
        continue;
      }
      node.addBodyDeclaration(newReturnTypeNarrowingDeclaration(
          newDecl.getKey(), newDecl.getValue(), type));
    }
  }

  private MethodDeclaration newReturnTypeNarrowingDeclaration(
      String selector, ExecutablePair method, TypeElement declaringClass) {
    GeneratedExecutableElement element = GeneratedExecutableElement.newMethodWithSelector(
        selector, method.type().getReturnType(), declaringClass)
        // Preserve visibility of the original method.
        .addModifiers(ElementUtil.getVisibilityModifiers(method.element()))
        .addModifiers(Modifier.ABSTRACT);
    MethodDeclaration decl = new MethodDeclaration(element);
    if (!declaringClass.getKind().isInterface()) {
      unit.setHasIncompleteImplementation();
    }
    int argCount = 0;
    for (TypeMirror paramType : method.type().getParameterTypes()) {
      VariableElement param = GeneratedVariableElement.newParameter(
          "arg" + argCount++, paramType, element);
      element.addParameter(param);
      decl.addParameter(new SingleVariableDeclaration(param));
    }
    return decl;
  }
}
