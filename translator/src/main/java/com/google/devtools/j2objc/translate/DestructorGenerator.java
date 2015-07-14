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

import com.google.common.collect.Lists;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TryStatement;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.List;

/**
 * Adds release methods to Java classes, in preparation for translation
 * to iOS.  Because Objective-C allows messages to be sent to nil, all
 * fields can be released regardless of whether they currently reference
 * data.
 *
 * @author Tom Ball
 */
public class DestructorGenerator extends TreeVisitor {

  @Override
  public boolean visit(TypeDeclaration node) {
    if (node.isInterface()) {
      return true;
    }
    MethodDeclaration destructor = findDestructor(node);
    if (destructor != null && Options.useARC()) {
      removeSuperFinalizeStatement(destructor.getBody());
    }
    List<Statement> releaseStatements = createReleaseStatements(node);
    if (destructor == null && !releaseStatements.isEmpty()) {
      // No destructor, so create a new one if there are releasable fields.
      destructor = buildFinalizeMethod(node.getTypeBinding());
      node.getBodyDeclarations().add(destructor);
    }
    if (destructor != null) {
      addReleaseStatements(destructor, releaseStatements);
    }
    return true;
  }

  private MethodDeclaration findDestructor(TypeDeclaration node) {
    for (MethodDeclaration method : TreeUtil.getMethodDeclarations(node)) {
      if (BindingUtil.isDestructor(method.getMethodBinding())) {
        return method;
      }
    }
    return null;
  }

  private void removeSuperFinalizeStatement(Block body) {
    body.accept(new TreeVisitor() {
      @Override
      public boolean visit(final ExpressionStatement node) {
        Expression e = node.getExpression();
        if (e instanceof SuperMethodInvocation) {
          IMethodBinding m = ((SuperMethodInvocation) e).getMethodBinding();
          if (BindingUtil.isDestructor(m)) {
            node.remove();
            return false;
          }
        }
        return true;
      }
    });
  }

  private boolean isStatic(FieldDeclaration f) {
    return (f.getModifiers() & Modifier.STATIC) != 0;
  }

  private SuperMethodInvocation findSuperFinalizeInvocation(MethodDeclaration node) {
    // Find existing super.finalize(), if any.
    final SuperMethodInvocation[] superFinalize = new SuperMethodInvocation[1];
    node.accept(new TreeVisitor() {
      @Override
      public void endVisit(SuperMethodInvocation node) {
        if (BindingUtil.isDestructor(node.getMethodBinding())) {
          superFinalize[0] = node;
        }
      }
    });
    return superFinalize[0];
  }

  private List<Statement> createReleaseStatements(TypeDeclaration node) {
    List<Statement> statements = Lists.newArrayList();
    for (VariableDeclarationFragment fragment : TreeUtil.getAllFields(node)) {
      IVariableBinding var = fragment.getVariableBinding();
      ITypeBinding type = var.getType();
      if (BindingUtil.isStatic(var) || type.isPrimitive() || BindingUtil.isWeakReference(var)
          || (Options.useARC() && !BindingUtil.isVolatile(var))) {
        continue;
      }
      statements.add(createRelease(var));
    }
    return statements;
  }

  private Statement createRelease(IVariableBinding var) {
    ITypeBinding voidType = typeEnv.resolveJavaType("void");
    boolean isVolatile = BindingUtil.isVolatile(var);
    FunctionInvocation releaseInvocation = new FunctionInvocation(
        isVolatile ? "JreReleaseVolatile" : "RELEASE_", voidType, voidType, null);
    Expression arg = new SimpleName(var);
    if (isVolatile) {
      arg = new PrefixExpression(
          typeEnv.getPointerType(var.getType()), PrefixExpression.Operator.ADDRESS_OF, arg);
    }
    releaseInvocation.getArguments().add(arg);
    return new ExpressionStatement(releaseInvocation);
  }

  private void addReleaseStatements(MethodDeclaration method, List<Statement> releaseStatements) {
    SuperMethodInvocation superFinalize = findSuperFinalizeInvocation(method);

    List<Statement> statements = method.getBody().getStatements();
    if (superFinalize != null) {
      // Release statements must be inserted before the [super dealloc] call.
      statements =
          TreeUtil.asStatementList(TreeUtil.getOwningStatement(superFinalize)).subList(0, 0);
    } else if (!statements.isEmpty() && statements.get(0) instanceof TryStatement) {
      TryStatement tryStatement = ((TryStatement) statements.get(0));
      if (tryStatement.getBody() != null) {
        statements = tryStatement.getBody().getStatements();
      }
    }

    statements.addAll(releaseStatements);

    if (Options.useReferenceCounting() && superFinalize == null) {
      IMethodBinding methodBinding = method.getMethodBinding();
      GeneratedMethodBinding binding = GeneratedMethodBinding.newMethod(
          NameTable.DEALLOC_METHOD, Modifier.PUBLIC, typeEnv.mapTypeName("void"),
          methodBinding.getDeclaringClass());
      SuperMethodInvocation call = new SuperMethodInvocation(binding);
      ExpressionStatement stmt = new ExpressionStatement(call);
      statements.add(stmt);
    }
  }

  private MethodDeclaration buildFinalizeMethod(ITypeBinding declaringClass) {
    ITypeBinding voidType = typeEnv.mapTypeName("void");
    int modifiers = Modifier.PUBLIC | BindingUtil.ACC_SYNTHETIC;
    GeneratedMethodBinding binding = GeneratedMethodBinding.newMethod(
        NameTable.DEALLOC_METHOD, modifiers, voidType, declaringClass);
    MethodDeclaration method = new MethodDeclaration(binding);
    method.setBody(new Block());
    return method;
  }
}
