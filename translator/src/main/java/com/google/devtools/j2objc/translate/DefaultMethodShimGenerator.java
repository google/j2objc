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
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.types.FunctionBinding;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * Generate shims for classes and enums that implement interfaces with default methods. Each shim
 * calls the functionalized default method implementation defined in the interface. Also generates
 * delegating shims for inherited methods that declare a different selector than the implementation.
 *
 * @author Lukhnos Liu, Keith Stanger
 */
public class DefaultMethodShimGenerator extends UnitTreeVisitor {

  public DefaultMethodShimGenerator(CompilationUnit unit) {
    super(unit);
  }

  /**
   * Pairs an ExecutableElement with an ExecutableType representing the resolved type variables as a
   * member of the current subclass being fixed.
   */
  private static class ExecutablePair {
    private final ExecutableElement elem;
    private final ExecutableType type;
    private ExecutablePair(ExecutableElement elem, ExecutableType type) {
      this.elem = elem;
      this.type = type;
    }
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
      if (type == null) {
        return;
      }
      collectMethods((DeclaredType) type);
      for (TypeMirror supertype : env.typeUtilities().directSupertypes(type)) {
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
          ElementUtil.getDeclaredMethods(typeElem), ElementUtil::isInstanceMethod)) {
        ExecutablePair method = new ExecutablePair(
            methodElem, (ExecutableType) env.typeUtilities().asMemberOf(type, methodElem));
        collector.put(getOverrideSignature(method), method);
      }
    }

    private void fixNewMethods(String signature) {
      Set<ExecutablePair> existingMethods = this.existingMethods.get(signature);
      Set<ExecutablePair> newMethods = this.newMethods.get(signature);
      Iterable<ExecutablePair> allMethods = Iterables.concat(existingMethods, newMethods);
      ExecutablePair first = allMethods.iterator().next();
      String mainSelector = nameTable.getMethodSelector(first.elem);
      ExecutablePair impl = resolveImplementation(allMethods);

      // Find the set of selectors for this method that don't yet have shims in a superclass.
      Set<String> existingSelectors = new HashSet<>();
      Map<String, ExecutablePair> newSelectors = new LinkedHashMap<>();
      for (ExecutablePair method : existingMethods) {
        existingSelectors.add(nameTable.getMethodSelector(method.elem));
      }
      existingSelectors.add(mainSelector);
      for (ExecutablePair method : newMethods) {
        String sel =  nameTable.getMethodSelector(method.elem);
        if (!existingSelectors.contains(sel) && !newSelectors.containsKey(sel)) {
          newSelectors.put(sel, method);
        }
      }

      if (ElementUtil.isDefault(impl.elem) && newMethods.contains(impl)) {
        addDefaultMethodShim(mainSelector, impl);
      }
      for (Map.Entry<String, ExecutablePair> entry : newSelectors.entrySet()) {
        addRenamingMethodShim(entry.getKey(), entry.getValue(), impl);
      }
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
          || (!declaredByClass(b.elem) && declaredByClass(a.elem))
          || env.elementUtilities().overrides(a.elem, b.elem,
              ElementUtil.getDeclaringClass(a.elem));
    }

    private void addShimWithInvocation(
        String selector, ExecutablePair method, Expression invocation, List<Expression> args) {
      IOSMethodBinding binding = IOSMethodBinding.newMappedMethod(
          selector, (IMethodBinding) BindingConverter.unwrapTypeMirrorIntoBinding(method.type));
      // Mark synthetic to avoid writing metadata.
      binding.addModifiers(BindingUtil.ACC_SYNTHETIC);
      binding.removeModifiers(Modifier.ABSTRACT | Modifier.DEFAULT);
      binding.setDeclaringClass(BindingConverter.unwrapTypeElement(typeElem));

      MethodDeclaration methodDecl = new MethodDeclaration(binding);
      methodDecl.setHasDeclaration(false);

      int i = 0;
      for (TypeMirror paramType : method.type.getParameterTypes()) {
        GeneratedVariableElement newParam = new GeneratedVariableElement(
            "arg" + i++, paramType, ElementKind.PARAMETER, null);
        methodDecl.addParameter(new SingleVariableDeclaration(newParam));
        args.add(new SimpleName(newParam));
      }

      Block block = new Block();
      block.addStatement(TypeUtil.isVoid(method.elem.getReturnType())
          ? new ExpressionStatement(invocation) : new ReturnStatement(invocation));
      methodDecl.setBody(block);
      typeNode.addBodyDeclaration(methodDecl);
    }

    private void addDefaultMethodShim(String selector, ExecutablePair method) {
      // The shim's only purpose is to call the default method implementation and returns it value
      // if required.
      TypeElement declaringClass = ElementUtil.getDeclaringClass(method.elem);
      String name = nameTable.getFullFunctionName(method.elem);
      FunctionBinding fb = new FunctionBinding(name, method.elem.getReturnType(), declaringClass);
      fb.addParameters(declaringClass.asType());
      fb.addParameters(((ExecutableType) method.elem.asType()).getParameterTypes());
      FunctionInvocation invocation = new FunctionInvocation(fb, method.type.getReturnType());

      // All default method implementations require self as the first function call argument.
      invocation.addArgument(new ThisExpression(typeElem.asType()));
      addShimWithInvocation(selector, method, invocation, invocation.getArguments());
    }

    private void addRenamingMethodShim(
        String selector, ExecutablePair method, ExecutablePair delegate) {
      MethodInvocation invocation = new MethodInvocation(delegate.elem, delegate.type, null);
      addShimWithInvocation(selector, method, invocation, invocation.getArguments());
    }
  }

  // Generates a signature that will be the same for methods that can override each other and unique
  // otherwise. Used as a key to group inherited methods together.
  private String getOverrideSignature(ExecutablePair method) {
    StringBuilder sb = new StringBuilder(ElementUtil.getName(method.elem));
    sb.append('(');
    for (TypeMirror pType : method.type.getParameterTypes()) {
      pType = env.typeUtilities().erasure(pType);
      sb.append(TypeUtil.getBinaryName(pType));
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
    new TypeFixer(node).visit();
  }
}
