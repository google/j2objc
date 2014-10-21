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
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.PostfixExpression;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SwitchCase;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Converts static variable access to static method calls where necessary.
 *
 * @author Keith Stanger
 */
public class StaticVarRewriter extends TreeVisitor {

  private boolean useAccessor(TreeNode currentNode, IVariableBinding var) {
    if (!BindingUtil.isStatic(var) || BindingUtil.isPrimitiveConstant(var)) {
      return false;
    }
    AbstractTypeDeclaration owningType = TreeUtil.getOwningType(currentNode);
    return owningType == null || !owningType.getTypeBinding().getTypeDeclaration().isEqualTo(
        var.getDeclaringClass().getTypeDeclaration());
  }

  @Override
  public boolean visit(Assignment node) {
    Expression lhs = node.getLeftHandSide();
    IVariableBinding lhsVar = TreeUtil.getVariableBinding(lhs);
    if (lhsVar != null && useAccessor(node, lhsVar)) {
      boolean isPrimitive = lhsVar.getType().isPrimitive();
      if (node.getOperator() == Assignment.Operator.ASSIGN && !isPrimitive) {
        Expression rhs = node.getRightHandSide();
        node.replaceWith(newSetterInvocation(lhsVar, TreeUtil.remove(rhs)));
        rhs.accept(this);
        return false;
      } else if (isPrimitive) {
        lhs.replaceWith(newGetterInvocation(lhsVar, true));
      }
    }
    return true;
  }

  @Override
  public boolean visit(SimpleName node) {
    return visitName(node);
  }

  @Override
  public boolean visit(QualifiedName node) {
    return visitName(node);
  }

  private boolean visitName(Name node) {
    IVariableBinding var = TreeUtil.getVariableBinding(node);
    if (var != null && useAccessor(node, var)) {
      TreeNode parent = node.getParent();
      if (parent instanceof QualifiedName && node == ((QualifiedName) parent).getQualifier()) {
        // QualifiedName nodes can only have qualifier children of type Name, so
        // we must convert QualifiedName parents to FieldAccess nodes.
        FieldAccess newParent = TreeUtil.convertToFieldAccess((QualifiedName) parent);
        node = (Name) newParent.getExpression();
      }
      node.replaceWith(newGetterInvocation(var, false));
      return false;
    }
    return true;
  }

  @Override
  public boolean visit(SwitchCase node) {
    // Avoid using an accessor method for enums in a switch case.
    return false;
  }

  @Override
  public boolean visit(PostfixExpression node) {
    IVariableBinding operandVar = TreeUtil.getVariableBinding(node.getOperand());
    PostfixExpression.Operator op = node.getOperator();
    boolean isIncOrDec = op == PostfixExpression.Operator.INCREMENT
        || op == PostfixExpression.Operator.DECREMENT;
    if (isIncOrDec && operandVar != null && useAccessor(node, operandVar)) {
      node.setOperand(newGetterInvocation(operandVar, true));
      return false;
    }
    return true;
  }

  @Override
  public boolean visit(PrefixExpression node) {
    IVariableBinding operandVar = TreeUtil.getVariableBinding(node.getOperand());
    PrefixExpression.Operator op = node.getOperator();
    boolean isIncOrDec = op == PrefixExpression.Operator.INCREMENT
        || op == PrefixExpression.Operator.DECREMENT;
    if (isIncOrDec && operandVar != null && useAccessor(node, operandVar)) {
      node.setOperand(newGetterInvocation(operandVar, true));
      return false;
    }
    return true;
  }

  private Expression newGetterInvocation(IVariableBinding var, boolean assignable) {
    ITypeBinding declaringType = var.getDeclaringClass().getTypeDeclaration();
    String varName = NameTable.getStaticVarName(var);
    String getterName = "get";
    ITypeBinding returnType = var.getType();
    if (assignable) {
      getterName += "Ref";
      returnType = Types.getPointerType(returnType);
    }
    getterName = NameTable.getFullName(declaringType) + "_" + getterName + "_" + varName;
    Expression invocation = new FunctionInvocation(
        getterName, returnType, returnType, declaringType);
    if (assignable) {
      invocation = new PrefixExpression(PrefixExpression.Operator.DEREFERENCE, invocation);
    }
    return invocation;
  }

  private FunctionInvocation newSetterInvocation(IVariableBinding var, Expression value) {
    ITypeBinding varType = var.getType();
    ITypeBinding declaringType = var.getDeclaringClass();
    String funcFormat = "%s_set_%s";
    if (Options.useReferenceCounting() && TreeUtil.retainResult(value)) {
      funcFormat = "%s_setAndConsume_%s";
    }
    String funcName = String.format(
        funcFormat, NameTable.getFullName(declaringType), NameTable.getStaticVarName(var));
    FunctionInvocation invocation = new FunctionInvocation(
        funcName, varType, varType, declaringType);
    invocation.getArguments().add(value);
    return invocation;
  }
}
