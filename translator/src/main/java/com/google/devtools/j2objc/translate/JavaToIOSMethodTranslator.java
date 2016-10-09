/*
 * Copyright 2011 Google Inc. All Rights Reserved.
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

import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * Translates invocations of mapped constructors to method invocation nodes.
 * Adds copyWithZone methods to Cloneable types.
 *
 * @author Tom Ball
 */
public class JavaToIOSMethodTranslator extends TreeVisitor {

  @Override
  public boolean visit(MethodDeclaration node) {
    IMethodBinding method = node.getMethodBinding();

    // Check if @ObjectiveCName is used but is mismatched with an overriden method.
    String name = NameTable.getMethodNameFromAnnotation(method);
    if (name != null) {
      String selector = nameTable.selectorForMethodName(method, name);
      String actualSelector = nameTable.getMethodSelector(method);
      if (!selector.equals(actualSelector)) {
        ErrorUtil.warning("ObjectiveCName(" + selector
            + "): Renamed method overrides a method with a different name.");
      }
    }
    return true;
  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    // translate any embedded method invocations
    if (node.getExpression() != null) {
      node.getExpression().accept(this);
    }
    for (Expression e : node.getArguments()) {
      e.accept(this);
    }
    if (node.getAnonymousClassDeclaration() != null) {
      node.getAnonymousClassDeclaration().accept(this);
    }

    IMethodBinding binding = node.getMethodBinding();
    String key = BindingUtil.getMethodKey(binding);
    String selector = NameTable.STRING_CONSTRUCTOR_TO_METHOD_MAPPINGS.get(key);
    if (selector != null) {
      assert !node.hasRetainedResult();
      if (key.equals("java.lang.String.String(Ljava/lang/String;)V")) {
        // Special case: replace new String(constant) to constant (avoid clang warning).
        Expression arg = node.getArgument(0);
        if (arg instanceof StringLiteral) {
          node.replaceWith(arg.copy());
          return false;
        }
      }
      IOSMethodBinding methodBinding = IOSMethodBinding.newMappedMethod(selector, binding);
      MethodInvocation newInvocation = new MethodInvocation(
          methodBinding, new SimpleName(binding.getDeclaringClass()));
      TreeUtil.copyList(node.getArguments(), newInvocation.getArguments());

      node.replaceWith(newInvocation);
    }
    return true;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    // If this type implements Cloneable but its parent doesn't, add a
    // copyWithZone: method that calls clone().
    ITypeBinding type = node.getTypeBinding();
    ITypeBinding javaLangCloneable = typeEnv.resolveJavaType("java.lang.Cloneable");
    if (type.isAssignmentCompatible(javaLangCloneable)) {
      ITypeBinding superclass = type.getSuperclass();
      if (superclass == null || !superclass.isAssignmentCompatible(javaLangCloneable)) {
        addCopyWithZoneMethod(node);
      }
    }
  }

  private void addCopyWithZoneMethod(TypeDeclaration node) {
    // Create copyWithZone: method.
    ITypeBinding type = node.getTypeBinding().getTypeDeclaration();
    ITypeBinding idType = typeEnv.resolveIOSType("id");
    ITypeBinding nsObjectType = typeEnv.resolveIOSType("NSObject");

    IOSMethodBinding binding = IOSMethodBinding.newMethod(
        "copyWithZone:", Modifier.PUBLIC | BindingUtil.ACC_SYNTHETIC, idType, type);
    MethodDeclaration cloneMethod = new MethodDeclaration(binding);
    cloneMethod.setHasDeclaration(false);

    // Add NSZone *zone parameter.
    GeneratedVariableBinding zoneBinding = new GeneratedVariableBinding(
        "zone", 0, typeEnv.resolveIOSType("NSZone"), false, true, binding.getDeclaringClass(),
        binding);
    binding.addParameter(zoneBinding.getType());
    cloneMethod.addParameter(new SingleVariableDeclaration(zoneBinding));

    Block block = new Block();
    cloneMethod.setBody(block);

    GeneratedMethodBinding cloneBinding = GeneratedMethodBinding.newMethod(
        "java_clone", 0, nsObjectType, type);
    MethodInvocation invocation = new MethodInvocation(cloneBinding, null);
    if (Options.useReferenceCounting()) {
      invocation = new MethodInvocation(typeEnv.getRetainMethod(), invocation);
    }
    block.addStatement(new ReturnStatement(invocation));

    node.addBodyDeclaration(cloneMethod);
  }
}
