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

import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.types.FunctionBinding;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Generate shims for classes and enums that implement interfaces with default methods. Each shim
 * calls the functionalized default method implementation defined in the interface.
 *
 * @author Lukhnos Liu
 */
public class DefaultMethodShimGenerator extends TreeVisitor {

  @Override
  public void endVisit(EnumDeclaration node) {
    addDefaultMethodShims(node);
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    addDefaultMethodShims(node);
  }

  /**
   * Implement the shims that call the default methods defined in the interfaces.
   *
   * To match the semantics of Java 8, we need to observe the following constraints:
   *
   * 1. If an interface I has a default method M, and if class C or any of C's super classes
   *    implements I, only one shim is generated to call M in the class that implements I.
   * 2. If an interface I1 has a default method M and I2 redeclares M, the class C that implements
   *    I2 should have a shim that calls I2's M, not I1's.
   * 3. If a class C inherits a concrete method M that's also declared in some interface I as
   *    a default method, the concrete method takes precedence and no shim should be generated.
   *
   * We let JDT handle the improbable cases -- for example, if interface I2 re-declares method M
   * in I1 and turns it into abstract (that is I2 no longer provides a body for M), than a class C
   * that implements I2 can only be an abstract class. If C is not abstract, it results in a
   * compiler error. Of course, if C is an abstract class because of the abstract M, we should never
   * generate a shim for M.
   *
   * Note that the node parameter here can be a class or an interface. If node is an interface,
   * the shim methods added here will go into its companion class. We need the shims so that lambdas
   * based on the interface will also carry the default methods.
   */
  private void addDefaultMethodShims(AbstractTypeDeclaration node) {
    ITypeBinding type = node.getTypeBinding();
    if (type.isAnnotation()) {
      return;
    }

    // First, collect all interfaces that are implemented by this type.
    Set<ITypeBinding> interfaces = BindingUtil.getAllInterfaces(type);

    // Now, collect those implemented by the super. This gets an empty set if type is an interface.
    Set<ITypeBinding> implementedBySuper = BindingUtil.getAllInterfaces(type.getSuperclass());

    // Remove those already implemented by super. These are the interfaces we care about. This
    // guarantees that only one shim is ever generated for one default method (provided the
    // default method is not re-declared later) in the inheritance chain.
    interfaces.removeAll(implementedBySuper);

    // Collect the methods declared in the interfaces. If there is already an existing method in
    // sigMethods, we test if the iterating method overrides it. If so, we replace the entry.
    // This guaranteed that the collected methods are from the leaf interfaces implemented by type.
    Map<String, IMethodBinding> sigMethods = new TreeMap<>();
    for (ITypeBinding t : interfaces) {
      for (IMethodBinding method : t.getDeclaredMethods()) {
        String signature = BindingUtil.getDefaultMethodSignature(method);
        IMethodBinding existingMethod = sigMethods.get(signature);
        if (existingMethod == null || method.overrides(existingMethod)) {
          sigMethods.put(signature, method);
        }
      }
    }

    // Remove the methods declared in this type.
    for (IMethodBinding method : type.getDeclaredMethods()) {
      sigMethods.remove(BindingUtil.getDefaultMethodSignature(method));
    }

    // The concrete methods that the type inherits take precedence.
    ITypeBinding superType = type;
    while ((superType = superType.getSuperclass()) != null) {
      for (IMethodBinding method : superType.getDeclaredMethods()) {
        if (BindingUtil.isAbstract(method)) {
          continue;
        }
        sigMethods.remove(BindingUtil.getDefaultMethodSignature(method));
      }
    }

    // The remaining default methods are what we need to create shims for.
    for (IMethodBinding method : sigMethods.values()) {
      if (!BindingUtil.isDefault(method)) {
        continue;
      }

      // Create the method binding and declaration.
      GeneratedMethodBinding binding = new GeneratedMethodBinding(method);

      // Don't carry over the default method flag from the original binding.
      binding.removeModifiers(Modifier.DEFAULT);
      // Mark synthetic to avoid writing metadata.
      binding.addModifiers(BindingUtil.ACC_SYNTHETIC);

      binding.setDeclaringClass(type);
      MethodDeclaration methodDecl = new MethodDeclaration(binding);
      methodDecl.setHasDeclaration(false);

      // The shim's only purpose is to call the default method implementation and returns it value
      // if required.
      String name = nameTable.getFullFunctionName(method);
      FunctionBinding fb = new FunctionBinding(name, method.getReturnType(), type);
      fb.addParameters(type);
      fb.addParameters(method.getParameterTypes());
      FunctionInvocation invocation = new FunctionInvocation(fb, method.getReturnType());

      // All default method implementations require self as the first function call argument.
      invocation.getArguments().add(new ThisExpression(type));

      // For each parameter in the default method, assign a name, and use the name in both the
      // method declaration and the function invocation.
      for (int i = 0; i < method.getParameterTypes().length; i++) {
        ITypeBinding paramType = method.getParameterTypes()[i];
        String paramName = UnicodeUtils.format("arg%d", i);
        GeneratedVariableBinding varBinding = new GeneratedVariableBinding(paramName, 0, paramType,
            false, true, type, null);
        methodDecl.getParameters().add(new SingleVariableDeclaration(varBinding));
        invocation.getArguments().add(new SimpleName(varBinding));
      }

      Statement stmt = BindingUtil.isVoid(method.getReturnType())
          ? new ExpressionStatement(invocation)
          : new ReturnStatement(invocation);

      Block block = new Block();
      block.getStatements().add(stmt);
      methodDecl.setBody(block);
      node.getBodyDeclarations().add(methodDecl);
    }
  }
}
