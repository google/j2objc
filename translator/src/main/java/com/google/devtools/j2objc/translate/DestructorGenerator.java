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
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NullLiteral;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TryStatement;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.Types;
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
  private final String destructorName;

  public DestructorGenerator() {
    destructorName = Options.useGC() ? NameTable.FINALIZE_METHOD : NameTable.DEALLOC_METHOD;
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    final List<IVariableBinding> releaseableFields = Lists.newArrayList();
    for (final FieldDeclaration field : TreeUtil.getFieldDeclarations(node)) {
      if (!field.getType().isPrimitiveType() && !isStatic(field)) {
        TreeVisitor varFinder = new TreeVisitor() {
          @Override
          public boolean visit(VariableDeclarationFragment node) {
            IVariableBinding binding = node.getVariableBinding();
            if (!Modifier.isStatic(field.getModifiers())) {
              releaseableFields.add(binding);
            }
            return true;
          }
        };
        varFinder.run(field);
      }
    }
    // We always generate a destructor method except if the type is an interface.
    if (!node.isInterface()) {
      boolean foundDestructor = false;

      // If a destructor method already exists, append release statements.
      for (MethodDeclaration method : TreeUtil.getMethodDeclarations(node)) {
        if (NameTable.FINALIZE_METHOD.equals(method.getName().getIdentifier())) {
          if (Options.useARC()) {
            removeSuperFinalizeStatement(method.getBody());
          }
          addReleaseStatements(method, releaseableFields);
          foundDestructor = true;
        }
      }

      // No destructor, so create a new one if there are releasable fields.
      if (!foundDestructor && !Options.useARC() && !releaseableFields.isEmpty()) {
        MethodDeclaration finalizeMethod =
            buildFinalizeMethod(node.getTypeBinding(), releaseableFields);
        node.getBodyDeclarations().add(finalizeMethod);
      }
    }

    // Rename method to correct destructor name.  This is down outside of
    // the loop above, because a class may have a finalize() method but no
    // releasable fields.
    for (MethodDeclaration method : TreeUtil.getMethodDeclarations(node)) {
      if (needsRenaming(method.getName())) {
        NameTable.rename(method.getMethodBinding(), destructorName);
      }
    }
    return super.visit(node);
  }

  private void removeSuperFinalizeStatement(Block body) {
    body.accept(new TreeVisitor() {
      @Override
      public boolean visit(final ExpressionStatement node) {
        Expression e = node.getExpression();
        if (e instanceof SuperMethodInvocation) {
          IMethodBinding m = ((SuperMethodInvocation) e).getMethodBinding();
          if (!Modifier.isStatic(m.getModifiers()) && m.getName().equals(NameTable.FINALIZE_METHOD)
              && m.getParameterTypes().length == 0) {
            node.remove();
            return false;
          }
        }
        return true;
      }
    });
  }

  @Override
  public boolean visit(MethodInvocation node) {
    if (needsRenaming(node.getName())) {
      NameTable.rename(node.getMethodBinding(), destructorName);
    }
    return true;
  }

  @Override
  public boolean visit(SuperMethodInvocation node) {
    if (needsRenaming(node.getName())) {
      NameTable.rename(node.getMethodBinding(), destructorName);
    }
    return true;
  }

  private boolean isStatic(FieldDeclaration f) {
    return (f.getModifiers() & Modifier.STATIC) != 0;
  }

  private boolean needsRenaming(SimpleName methodName) {
    return destructorName.equals(NameTable.DEALLOC_METHOD)
        && NameTable.FINALIZE_METHOD.equals(methodName.getIdentifier());
  }

  private SuperMethodInvocation findSuperFinalizeInvocation(MethodDeclaration node) {
    // Find existing super.finalize(), if any.
    final SuperMethodInvocation[] superFinalize = new SuperMethodInvocation[1];
    node.accept(new TreeVisitor() {
      @Override
      public void endVisit(SuperMethodInvocation node) {
        if (NameTable.FINALIZE_METHOD.equals(node.getName().getIdentifier())) {
          superFinalize[0] = node;
        }
      }
    });
    return superFinalize[0];
  }

  private void addReleaseStatements(MethodDeclaration method, List<IVariableBinding> fields) {
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
    for (IVariableBinding field : fields) {
      if (!field.getType().isPrimitive() && !BindingUtil.isWeakReference(field)) {
        Assignment assign = new Assignment(new SimpleName(field), new NullLiteral());
        ExpressionStatement stmt = new ExpressionStatement(assign);
        statements.add(stmt);
      }
    }
    if (Options.useReferenceCounting() && superFinalize == null) {
      IMethodBinding methodBinding = method.getMethodBinding();
      GeneratedMethodBinding binding = GeneratedMethodBinding.newMethod(
          destructorName, Modifier.PUBLIC, Types.mapTypeName("void"),
          methodBinding.getDeclaringClass());
      SuperMethodInvocation call = new SuperMethodInvocation(binding);
      ExpressionStatement stmt = new ExpressionStatement(call);
      statements.add(stmt);
    }
  }

  private MethodDeclaration buildFinalizeMethod(
      ITypeBinding declaringClass, List<IVariableBinding> fields) {
    ITypeBinding voidType = Types.mapTypeName("void");
    int modifiers = Modifier.PUBLIC | BindingUtil.ACC_SYNTHETIC;
    GeneratedMethodBinding binding = GeneratedMethodBinding.newMethod(
        destructorName, modifiers, voidType, declaringClass);
    MethodDeclaration method = new MethodDeclaration(binding);
    method.setBody(new Block());
    addReleaseStatements(method, fields);
    return method;
  }
}
