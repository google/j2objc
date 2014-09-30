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
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.NullLiteral;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.List;

/**
 * Rewrites certain operators, such as object assignment, into appropriate
 * method calls.
 *
 * @author Keith Stanger
 */
public class OperatorRewriter extends TreeVisitor {

  private static Expression getTarget(Expression node, IVariableBinding var) {
    if (node instanceof QualifiedName) {
      return ((QualifiedName) node).getQualifier();
    } else if (node instanceof FieldAccess) {
      return ((FieldAccess) node).getExpression();
    }
    return new ThisExpression(var.getDeclaringClass());
  }

  @Override
  public void endVisit(Assignment node) {
    Assignment.Operator op = node.getOperator();
    Expression lhs = node.getLeftHandSide();
    Expression rhs = node.getRightHandSide();
    ITypeBinding lhsType = lhs.getTypeBinding();
    ITypeBinding rhsType = rhs.getTypeBinding();
    if (op == Assignment.Operator.ASSIGN) {
      IVariableBinding var = TreeUtil.getVariableBinding(lhs);
      if (var == null || var.getType().isPrimitive() || !Options.useReferenceCounting()) {
        return;
      }
      if (BindingUtil.isStatic(var)) {
        node.replaceWith(newStaticAssignInvocation(var, rhs));
      } else if (var.isField() && !BindingUtil.isWeakReference(var)) {
        Expression target = getTarget(lhs, var);
        node.replaceWith(newFieldSetterInvocation(var, target, rhs));
      }
    } else {
      String funcName = getOperatorAssignFunction(op, lhsType, rhsType);
      if (funcName != null) {
        FunctionInvocation invocation = new FunctionInvocation(funcName, lhsType, lhsType, null);
        List<Expression> args = invocation.getArguments();
        args.add(new PrefixExpression(PrefixExpression.Operator.ADDRESS_OF, TreeUtil.remove(lhs)));
        args.add(TreeUtil.remove(rhs));
        node.replaceWith(invocation);
      }
    }
  }

  public void endVisit(InfixExpression node) {
    InfixExpression.Operator op = node.getOperator();
    ITypeBinding nodeType = node.getTypeBinding();
    String funcName = getInfixFunction(op, nodeType);
    if (funcName != null) {
      FunctionInvocation invocation = new FunctionInvocation(funcName, nodeType, nodeType, null);
      List<Expression> args = invocation.getArguments();
      args.add(TreeUtil.remove(node.getLeftOperand()));
      args.add(TreeUtil.remove(node.getRightOperand()));
      node.replaceWith(invocation);
    }
  }

  private static boolean isFloatingPoint(ITypeBinding type) {
    return type.getName().equals("double") || type.getName().equals("float");
  }

  private FunctionInvocation newStaticAssignInvocation(IVariableBinding var, Expression value) {
    String assignFunc =
        TreeUtil.retainResult(value) ? "JreStrongAssignAndConsume" : "JreStrongAssign";
    FunctionInvocation invocation = new FunctionInvocation(
        assignFunc, value.getTypeBinding(), Types.resolveIOSType("id"), null);
    List<Expression> args = invocation.getArguments();
    args.add(new PrefixExpression(PrefixExpression.Operator.ADDRESS_OF, new SimpleName(var)));
    args.add(new NullLiteral());
    args.add(TreeUtil.remove(value));
    return invocation;
  }

  private static FunctionInvocation newFieldSetterInvocation(
      IVariableBinding var, Expression instance, Expression value) {
    ITypeBinding varType = var.getType();
    ITypeBinding declaringType = var.getDeclaringClass().getTypeDeclaration();
    String setterFormat = "%s_set_%s";
    if (TreeUtil.retainResult(value)) {
      setterFormat = "%s_setAndConsume_%s";
    }
    String setterName = String.format(setterFormat, NameTable.getFullName(declaringType),
        NameTable.javaFieldToObjC(NameTable.getName(var)));
    FunctionInvocation invocation = new FunctionInvocation(
        setterName, varType, varType, declaringType);
    invocation.getArguments().add(TreeUtil.remove(instance));
    invocation.getArguments().add(TreeUtil.remove(value));
    return invocation;
  }

  private static String intOrLong(ITypeBinding type) {
    switch (type.getBinaryName().charAt(0)) {
      case 'I':
        return "32";
      case 'J':
        return "64";
      default:
        throw new AssertionError("Type expected to be int or long but was: " + type.getName());
    }
  }

  private static String getInfixFunction(InfixExpression.Operator op, ITypeBinding nodeType) {
    switch (op) {
      case REMAINDER:
        if (isFloatingPoint(nodeType)) {
          return nodeType.getName().equals("float") ? "fmodf" : "fmod";
        }
        return null;
      case LEFT_SHIFT:
        return "LShift" + intOrLong(nodeType);
      case RIGHT_SHIFT_SIGNED:
        return "RShift" + intOrLong(nodeType);
      case RIGHT_SHIFT_UNSIGNED:
        return "URShift" + intOrLong(nodeType);
      default:
        return null;
    }
  }

  private static String getOperatorAssignFunction(
      Assignment.Operator op, ITypeBinding lhsType, ITypeBinding rhsType) {
    String lhsName = NameTable.capitalize(lhsType.getName());
    switch (op) {
      case LEFT_SHIFT_ASSIGN:
        return "LShiftAssign" + lhsName;
      case RIGHT_SHIFT_SIGNED_ASSIGN:
        return "RShiftAssign" + lhsName;
      case RIGHT_SHIFT_UNSIGNED_ASSIGN:
        return "URShiftAssign" + lhsName;
      case REMAINDER_ASSIGN:
        if (isFloatingPoint(lhsType) || isFloatingPoint(rhsType)) {
          return "ModAssign" + lhsName;
        }
        return null;
      default:
        return null;
    }
  }
}
