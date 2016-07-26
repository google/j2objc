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

import com.google.common.collect.Lists;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.types.FunctionBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.util.BindingUtil;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.List;
import javax.lang.model.type.TypeMirror;

/**
 * Writes the __javaClone method in order to support correct Java clone()
 * behavior.
 *
 * @author Keith Stanger
 */
public class JavaCloneWriter extends TreeVisitor {

  private static final String JAVA_CLONE_METHOD = "__javaClone:";

  @Override
  public void endVisit(TypeDeclaration node) {
    ITypeBinding type = node.getTypeBinding();
    IVariableBinding originalVar = new GeneratedVariableBinding(
        "original", 0, type, false, true, null, null);
    List<Statement> adjustments = getFieldAdjustments(node, originalVar);
    if (adjustments.isEmpty()) {
      return;
    }

    ITypeBinding voidType = typeEnv.resolveJavaType("void");
    int modifiers = Modifier.PUBLIC | BindingUtil.ACC_SYNTHETIC;
    IOSMethodBinding methodBinding = IOSMethodBinding.newMethod(
        JAVA_CLONE_METHOD, modifiers, voidType, type);
    methodBinding.addParameter(type);

    MethodDeclaration declaration = new MethodDeclaration(methodBinding);
    declaration.setHasDeclaration(false);
    node.addBodyDeclaration(declaration);
    declaration.addParameter(new SingleVariableDeclaration(originalVar));

    Block body = new Block();
    declaration.setBody(body);
    List<Statement> statements = body.getStatements();

    ITypeBinding nsObjectType = typeEnv.resolveIOSType("NSObject");
    IOSMethodBinding cloneMethod = IOSMethodBinding.newMethod(
        JAVA_CLONE_METHOD, Modifier.PUBLIC, voidType, nsObjectType);
    SuperMethodInvocation superCall = new SuperMethodInvocation(cloneMethod);
    superCall.addArgument(new SimpleName(originalVar));
    statements.add(new ExpressionStatement(superCall));

    statements.addAll(adjustments);
  }

  private List<Statement> getFieldAdjustments(TypeDeclaration node, IVariableBinding originalVar) {
    List<Statement> adjustments = Lists.newArrayList();
    for (VariableDeclarationFragment decl : TreeUtil.getAllFields(node)) {
      IVariableBinding var = decl.getVariableBinding();
      if (BindingUtil.isStatic(var) || var.getType().isPrimitive()) {
        continue;
      }
      boolean isWeak = BindingUtil.isWeakReference(var);
      boolean isVolatile = BindingUtil.isVolatile(var);
      if (isVolatile) {
        adjustments.add(createVolatileCloneStatement(var, originalVar, isWeak));
      } else if (isWeak) {
        adjustments.add(createReleaseStatement(var));
      }
    }
    return adjustments;
  }

  private Statement createReleaseStatement(IVariableBinding var) {
    if (Options.useARC()) {
      ITypeBinding voidType = typeEnv.resolveJavaType("void");
      FunctionBinding binding = new FunctionBinding("JreRelease", voidType, null);
      binding.addParameters(typeEnv.resolveIOSTypeMirror("id"));
      FunctionInvocation invocation = new FunctionInvocation(binding, voidType);
      invocation.addArgument(new SimpleName(var));
      return new ExpressionStatement(invocation);
    } else {
      return new ExpressionStatement(
          new MethodInvocation(typeEnv.getReleaseMethod(), new SimpleName(var)));
    }
  }

  private Statement createVolatileCloneStatement(
      IVariableBinding var, IVariableBinding originalVar, boolean isWeak) {
    TypeMirror voidType = typeEnv.resolveJavaTypeMirror("void");
    TypeMirror pointerType = typeEnv.getPointerType(BindingConverter.getType(var.getType()));
    String funcName = "JreCloneVolatile" + (isWeak ? "" : "Strong");
    FunctionBinding binding = new FunctionBinding(funcName, voidType, null);
    binding.addParameters(pointerType, pointerType);
    FunctionInvocation invocation = new FunctionInvocation(binding, voidType);
    invocation.addArgument(new PrefixExpression(
        pointerType, PrefixExpression.Operator.ADDRESS_OF, new SimpleName(var)));
    invocation.addArgument(new PrefixExpression(
        pointerType, PrefixExpression.Operator.ADDRESS_OF,
        new FieldAccess(var, new SimpleName(originalVar))));
    return new ExpressionStatement(invocation);
  }
}
