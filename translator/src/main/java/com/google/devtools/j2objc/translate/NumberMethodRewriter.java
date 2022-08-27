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

import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import java.lang.reflect.Modifier;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Adds hash and isEqual: methods to java.lang.Number subclasses that
 * do not define them. This is necessary because classes that do not
 * define these methods expect Object.equals() and Object.hashCode()
 * behavior; since Number is mapped to NSNumber, though, by default
 * they inherit NSNumber behavior.
 *
 * @author Tom Ball
 */
public class NumberMethodRewriter extends UnitTreeVisitor {

  public NumberMethodRewriter(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    DeclaredType type = (DeclaredType) node.getTypeElement().asType();
    if (typeUtil.isSubtype(type, typeUtil.getJavaNumber().asType())) {
      ExecutablePair equalsMethod = findNumberMethod(type, "equals", "java.lang.Object");
      if (equalsMethod == null) {
        addEqualsMethod(node);
      }
      ExecutablePair hashCodeMethod = findNumberMethod(type, "hashCode");
      if (hashCodeMethod == null) {
        addHashCodeMethod(node);
      }
    }
  }

  /**
   * Convert override of NSNumber.initWithLong() to initWithLongLong(), to match Java type.
   * b/243948394.
   */
  @Override
  public void endVisit(MethodDeclaration method) {
    if (isNumberLongConstructor(method.getExecutableElement())) {
      ExecutableElement newMethodElement =
          updateNumberLongConstructor(method.getExecutableElement());
      MethodDeclaration unused = method.setExecutableElement(newMethodElement);
    }
  }

  @Override
  public void endVisit(ClassInstanceCreation invocation) {
    if (isNumberLongConstructor(invocation.getExecutableElement())) {
      ExecutableElement newMethodElement =
          updateNumberLongConstructor(invocation.getExecutableElement());
      ClassInstanceCreation unused = invocation.setExecutablePair(
          new ExecutablePair(newMethodElement, invocation.getExecutableType()));
    }
  }

  @SuppressWarnings("TypeEquals")
  private ExecutablePair findNumberMethod(DeclaredType type, String name, String... paramTypes) {
    ExecutablePair method = typeUtil.findMethod(type, name, paramTypes);
    if (method != null) {
      return method;
    }
    TypeMirror supertype = ((TypeElement) type.asElement()).getSuperclass();
    if (typeUtil.isSameType(supertype, typeUtil.getJavaObject().asType())) {
      return null;
    }
    return findNumberMethod((DeclaredType) supertype, name, paramTypes);
  }

  private void addEqualsMethod(TypeDeclaration node) {
    TypeElement typeElement = node.getTypeElement();
    GeneratedExecutableElement equalsElement = GeneratedExecutableElement.newMethodWithSelector(
        "isEqual:", typeUtil.getBoolean(), typeElement);
    GeneratedVariableElement paramElement = GeneratedVariableElement.newParameter(
        "obj", typeUtil.getJavaObject().asType(), equalsElement);
    equalsElement.addParameter(paramElement);
    node.addBodyDeclaration(new MethodDeclaration(equalsElement)
        .addParameter(new SingleVariableDeclaration(paramElement))
        .setBody(new Block().addStatement(new NativeStatement("return self == obj;")))
        .setModifiers(Modifier.PUBLIC));
  }

  private void addHashCodeMethod(TypeDeclaration node) {
    GeneratedExecutableElement element = GeneratedExecutableElement.newMethodWithSelector(
        "hash", typeUtil.getInt(), node.getTypeElement());
    node.addBodyDeclaration(new MethodDeclaration(element)
        .setBody(new Block().addStatement(new NativeStatement("return (NSUInteger)self;")))
        .setModifiers(Modifier.PUBLIC));
  }

  private boolean isNumberLongConstructor(ExecutableElement method) {
    TypeMirror owningClassType = method.getEnclosingElement().asType();
    if (typeUtil.isSubtype(owningClassType, typeUtil.getJavaNumber().asType())) {
      if (ElementUtil.isConstructor(method)
          && method.getParameters().size() == 1) {
        VariableElement var = method.getParameters().get(0);
        return var.asType().getKind() == TypeKind.LONG;
      }
    }
    return false;
  }

  // Fix selector so constructor overrides NSNumber.initWithLongLong(), to match jlong type.
  private ExecutableElement updateNumberLongConstructor(ExecutableElement constructor) {
    return GeneratedExecutableElement.mutableCopy("initWithLongLong:", constructor);
  }
}
