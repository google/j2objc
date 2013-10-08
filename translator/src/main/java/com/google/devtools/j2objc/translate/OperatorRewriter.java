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
import com.google.devtools.j2objc.types.PointerTypeBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

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
        "JreOperatorRetainedAssign", idType, null, new PointerTypeBinding(idType), idType, idType);
  }

  @Override
  public void endVisit(Assignment node) {
    AST ast = node.getAST();
    Assignment.Operator op = node.getOperator();
    Expression lhs = node.getLeftHandSide();
    Expression rhs = node.getRightHandSide();
    ITypeBinding lhsType = Types.getTypeBinding(lhs);
    ITypeBinding rhsType = Types.getTypeBinding(rhs);
    if (op == Assignment.Operator.ASSIGN) {
      IVariableBinding var = Types.getVariableBinding(lhs);
      if (var == null || var.getType().isPrimitive() || !Options.useReferenceCounting()) {
        return;
      }
      if (BindingUtil.isStatic(var)) {
        ASTUtil.setProperty(node, newStaticAssignInvocation(ast, var, rhs));
      } else if (var.isField() && !BindingUtil.isWeakReference(var)) {
        Types.addDeferredFieldSetter(node);
      }
    } else if (op == Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN) {
      if (!lhsType.getName().equals("char")) {
        ASTUtil.setProperty(node, newUnsignedRightShift(ast, lhsType, lhs, rhs));
      }
    } else if (op == Assignment.Operator.REMAINDER_ASSIGN) {
      if (isFloatingPoint(lhsType) || isFloatingPoint(rhsType)) {
        ASTUtil.setProperty(node, newModAssign(ast, lhsType, rhsType, lhs, rhs));
      }
    }
  }

  public void endVisit(InfixExpression node) {
    InfixExpression.Operator op = node.getOperator();
    ITypeBinding nodeType = Types.getTypeBinding(node);
    if (op == InfixExpression.Operator.REMAINDER && isFloatingPoint(nodeType)) {
      AST ast = node.getAST();
      String funcName = nodeType.getName().equals("float") ? "fmodf" : "fmod";
      IOSMethodBinding binding = IOSMethodBinding.newFunction(
          funcName, nodeType, null, nodeType, nodeType);
      MethodInvocation invocation = ASTFactory.newMethodInvocation(ast, binding, null);
      List<Expression> args = ASTUtil.getArguments(invocation);
      args.add(NodeCopier.copySubtree(ast, node.getLeftOperand()));
      args.add(NodeCopier.copySubtree(ast, node.getRightOperand()));
      ASTUtil.setProperty(node, invocation);
    }
  }

  private boolean isFloatingPoint(ITypeBinding type) {
    return type.getName().equals("double") || type.getName().equals("float");
  }

  private MethodInvocation newStaticAssignInvocation(
      AST ast, IVariableBinding var, Expression value) {
    MethodInvocation invocation = ASTFactory.newMethodInvocation(ast, retainedAssignBinding, null);
    List<Expression> args = ASTUtil.getArguments(invocation);
    args.add(ASTFactory.newAddressOf(ast, ASTFactory.newSimpleName(ast, var)));
    args.add(ASTFactory.newNullLiteral(ast));
    args.add(NodeCopier.copySubtree(ast, value));
    return invocation;
  }

  private static MethodInvocation newUnsignedRightShift(
      AST ast, ITypeBinding assignType, Expression lhs, Expression rhs) {
    String funcName = "URShiftAssign" + NameTable.capitalize(assignType.getName());
    IOSMethodBinding binding = IOSMethodBinding.newFunction(
        funcName, assignType, null, new PointerTypeBinding(assignType),
        Types.resolveJavaType("int"));
    MethodInvocation invocation = ASTFactory.newMethodInvocation(ast, binding, null);
    List<Expression> args = ASTUtil.getArguments(invocation);
    args.add(ASTFactory.newAddressOf(ast, NodeCopier.copySubtree(ast, lhs)));
    args.add(NodeCopier.copySubtree(ast, rhs));
    return invocation;
  }

  private static MethodInvocation newModAssign(
      AST ast, ITypeBinding lhsType, ITypeBinding rhsType, Expression lhs, Expression rhs) {
    String funcName = "ModAssign" + NameTable.capitalize(lhsType.getName());
    IOSMethodBinding binding = IOSMethodBinding.newFunction(
        funcName, lhsType, null, new PointerTypeBinding(lhsType), rhsType);
    MethodInvocation invocation = ASTFactory.newMethodInvocation(ast, binding, null);
    List<Expression> args = ASTUtil.getArguments(invocation);
    args.add(ASTFactory.newAddressOf(ast, NodeCopier.copySubtree(ast, lhs)));
    args.add(NodeCopier.copySubtree(ast, rhs));
    return invocation;
  }
}
