/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
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

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

/**
 * Generate shims for classes and enums that implement interfaces with default methods. Each shim
 * calls the functionalized default method implementation defined in the interface. Also generates
 * delegating shims for inherited methods that declare a different selector than the implementation.
 *
 * @author Lukhnos Liu, Keith Stanger
 */
public class DefaultMethodShimGenerator extends UnitTreeVisitor {

  private CodeReferenceMap deadCodeMap;

  public DefaultMethodShimGenerator(CompilationUnit unit, CodeReferenceMap deadCodeMap) {
    super(unit);
    this.deadCodeMap = deadCodeMap;
  }

  /**
   * Handles adding all the shims for a single type and manages state for collecting inherited
   * methods of the type.
   */
  private class TypeFixer {

    private final AbstractTypeDeclaration typeNode;
    private final TypeElement typeElem;
    private final Set<TypeElement> visitedTypes = new HashSet<>();
    private final SetMultimap<String, ExecutablePair> existingMethods = LinkedHashMultimap.create();
    private final SetMultimap<String, ExecutablePair> newMethods = LinkedHashMultimap.create();
    private SetMultimap<String, ExecutablePair> collector = existingMethods;

    private TypeFixer(AbstractTypeDeclaration node) {
      typeNode = node;
      typeElem = node.getTypeElement();
    }

    private void visit() {
      // Collect existing methods.
      collectMethods((DeclaredType) typeElem.asType());
      collectInheritedMethods(typeElem.getSuperclass());

      // Collect new methods from newly implemented interfaces.
      collector = newMethods;
      for (TypeMirror t : typeElem.getInterfaces()) {
        collectInheritedMethods((DeclaredType) t);
      }

      for (String signature : newMethods.keySet()) {
        fixNewMethods(signature);
      }
    }

    private void collectInheritedMethods(TypeMirror type) {
      if (TypeUtil.isNone(type)) {
        return;
      }
      collectMethods((DeclaredType) type);
      for (TypeMirror supertype : typeUtil.directSupertypes(type)) {
        collectInheritedMethods(supertype);
      }
    }

    private void collectMethods(DeclaredType type) {
      TypeElement typeElem = TypeUtil.asTypeElement(type);
      if (visitedTypes.contains(typeElem)) {
        return;
      }
      visitedTypes.add(typeElem);

      for (ExecutableElement methodElem : Iterables.filter(
          ElementUtil.getMethods(typeElem), ElementUtil::isInstanceMethod)) {
        if (!isDeadMethod(methodElem)) {
          ExecutablePair method = new ExecutablePair(
              methodElem, typeUtil.asMemberOf(type, methodElem));
          collector.put(getOverrideSignature(method), method);
        }
      }
    }

    private void fixNewMethods(String signature) {
      Set<ExecutablePair> existingMethods = this.existingMethods.get(signature);
      Set<ExecutablePair> newMethods = this.newMethods.get(signature);
      Iterable<ExecutablePair> allMethods = Iterables.concat(existingMethods, newMethods);
      ExecutablePair first = allMethods.iterator().next();
      String mainSelector = nameTable.getMethodSelector(first.element());
      ExecutablePair impl = resolveImplementation(allMethods);

      // Find the set of selectors for this method that don't yet have shims in a superclass.
      Set<String> existingSelectors = new HashSet<>();
      Map<String, ExecutablePair> newSelectors = new LinkedHashMap<>();
      for (ExecutablePair method : existingMethods) {
        existingSelectors.add(nameTable.getMethodSelector(method.element()));
      }
      existingSelectors.add(mainSelector);
      for (ExecutablePair method : newMethods) {
        String sel =  nameTable.getMethodSelector(method.element());
        if (!existingSelectors.contains(sel) && !newSelectors.containsKey(sel)) {
          newSelectors.put(sel, method);
        }
      }

      if (newMethods.contains(impl)) {
        if (ElementUtil.isDefault(impl.element())) {
          addDefaultMethodShim(mainSelector, impl);
        } else {
          // Must be an abstract class.
          unit.setHasIncompleteProtocol();
        }
      }
      for (Map.Entry<String, ExecutablePair> entry : newSelectors.entrySet()) {
        addRenamingMethodShim(entry.getKey(), entry.getValue(), impl);
      }
    }

    private boolean isDeadMethod(ExecutableElement methodElem) {
      return deadCodeMap != null && deadCodeMap.containsMethod(methodElem, typeUtil);
    }

    private ExecutablePair resolveImplementation(Iterable<ExecutablePair> allMethods) {
      ExecutablePair impl = null;
      for (ExecutablePair method : allMethods) {
        if (takesPrecedence(method, impl)) {
          impl = method;
        }
      }
      return impl;
    }

    private boolean declaredByClass(ExecutableElement e) {
      return ElementUtil.getDeclaringClass(e).getKind().isClass();
    }

    private boolean takesPrecedence(ExecutablePair a, ExecutablePair b) {
      return b == null
          || (!declaredByClass(b.element()) && declaredByClass(a.element()))
          || elementUtil.overrides(
              a.element(), b.element(), ElementUtil.getDeclaringClass(a.element()));
    }

    private void addShimWithInvocation(
        String selector, ExecutablePair method, Expression invocation, List<Expression> args) {
      GeneratedExecutableElement element = GeneratedExecutableElement.newMethodWithSelector(
              selector, method.type().getReturnType(), typeElem)
          .addModifiers(method.element().getModifiers())
          .removeModifiers(Modifier.ABSTRACT, Modifier.DEFAULT);

      MethodDeclaration methodDecl = new MethodDeclaration(element);
      methodDecl.setHasDeclaration(false);

      int i = 0;
      for (TypeMirror paramType : method.type().getParameterTypes()) {
        GeneratedVariableElement newParam = GeneratedVariableElement.newParameter(
            "arg" + i++, paramType, element);
        element.addParameter(newParam);
        methodDecl.addParameter(new SingleVariableDeclaration(newParam));
        args.add(new SimpleName(newParam));
      }

      Block block = new Block();
      block.addStatement(TypeUtil.isVoid(method.element().getReturnType())
          ? new ExpressionStatement(invocation) : new ReturnStatement(invocation));
      methodDecl.setBody(block);
      typeNode.addBodyDeclaration(methodDecl);
    }

    private void addDefaultMethodShim(String selector, ExecutablePair method) {
      // The shim's only purpose is to call the default method implementation and returns it value
      // if required.
      TypeElement declaringClass = ElementUtil.getDeclaringClass(method.element());
      String name = nameTable.getFullFunctionName(method.element());
      FunctionElement funcElement = new FunctionElement(
          name, method.element().getReturnType(), declaringClass)
          .addParameters(declaringClass.asType())
          .addParameters(((ExecutableType) method.element().asType()).getParameterTypes());
      FunctionInvocation invocation =
          new FunctionInvocation(funcElement, method.type().getReturnType());

      // All default method implementations require self as the first function call argument.
      invocation.addArgument(new ThisExpression(typeElem.asType()));
      addShimWithInvocation(selector, method, invocation, invocation.getArguments());
    }

    private void addRenamingMethodShim(
        String selector, ExecutablePair method, ExecutablePair delegate) {
      MethodInvocation invocation = new MethodInvocation(delegate, null);
      addShimWithInvocation(selector, method, invocation, invocation.getArguments());
    }
  }

  // Generates a signature that will be the same for methods that can override each other and unique
  // otherwise. Used as a key to group inherited methods together.
  private String getOverrideSignature(ExecutablePair method) {
    StringBuilder sb = new StringBuilder(ElementUtil.getName(method.element()));
    sb.append('(');
    for (TypeMirror pType : method.type().getParameterTypes()) {
      sb.append(typeUtil.getSignatureName(pType));
    }
    sb.append(')');
    return sb.toString();
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    new TypeFixer(node).visit();
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    if (!node.isInterface()) {
      new TypeFixer(node).visit();
    }
  }
}
