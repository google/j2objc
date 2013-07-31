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

import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;

import java.util.List;

/**
 * Rewrites certain operators, such as object assignment, into appropriate
 * method calls.
 *
 * @author Keith Stanger
 */
public class OperatorRewriter extends ErrorReportingASTVisitor {

  private final IOSMethodBinding retainedAssignBinding;

  public OperatorRewriter() {
    ITypeBinding idType = Types.resolveIOSType("id");
    retainedAssignBinding = IOSMethodBinding.newFunction(
        "JreOperatorRetainedAssign", idType, null, idType, idType, idType);
  }

  private static Expression getTarget(Expression node, IVariableBinding var) {
    if (node instanceof QualifiedName) {
      return ((QualifiedName) node).getQualifier();
    } else if (node instanceof FieldAccess) {
      return ((FieldAccess) node).getExpression();
    }
    return ASTFactory.newThisExpression(node.getAST(), var.getDeclaringClass());
  }

  @Override
  public void endVisit(Assignment node) {
    AST ast = node.getAST();
    Assignment.Operator op = node.getOperator();
    Expression lhs = node.getLeftHandSide();
    Expression rhs = node.getRightHandSide();
    if (op == Assignment.Operator.ASSIGN) {
      IVariableBinding var = Types.getVariableBinding(lhs);
      if (var == null || var.getType().isPrimitive() || !Options.useReferenceCounting()) {
        return;
      }
      if (BindingUtil.isStatic(var)) {
        ASTUtil.setProperty(node, newStaticAssignInvocation(var, NodeCopier.copySubtree(ast, rhs)));
      } else if (var.isField() && !Types.isWeakReference(var)) {
        Expression target = getTarget(lhs, var);
        ASTUtil.setProperty(node, newFieldSetterInvocation(
            var, NodeCopier.copySubtree(ast, target), NodeCopier.copySubtree(ast, rhs)));
      }
    }
  }

  private MethodInvocation newStaticAssignInvocation(IVariableBinding var, Expression value) {
    AST ast = value.getAST();
    MethodInvocation invocation = ASTFactory.newMethodInvocation(ast, retainedAssignBinding, null);
    List<Expression> args = ASTUtil.getArguments(invocation);
    args.add(ASTFactory.newAddressOf(ast, ASTFactory.newSimpleName(ast, var)));
    args.add(ASTFactory.newNullLiteral(ast));
    args.add(value);
    return invocation;
  }

  private static MethodInvocation newFieldSetterInvocation(
      IVariableBinding var, Expression instance, Expression value) {
    AST ast = instance.getAST();
    ITypeBinding varType = var.getType();
    ITypeBinding declaringType = var.getDeclaringClass().getTypeDeclaration();
    String setterName = String.format("%s_set_%s", NameTable.getFullName(declaringType),
        NameTable.javaFieldToObjC(NameTable.getName(var)));
    IOSMethodBinding binding = IOSMethodBinding.newFunction(
        setterName, varType, declaringType, declaringType, varType);
    MethodInvocation invocation = ASTFactory.newMethodInvocation(ast, binding, null);
    ASTUtil.getArguments(invocation).add(instance);
    ASTUtil.getArguments(invocation).add(value);
    return invocation;
  }
}
