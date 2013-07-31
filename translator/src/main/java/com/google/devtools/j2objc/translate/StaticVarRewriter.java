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

import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SwitchCase;

/**
 * Converts static variable access to static method calls where necessary.
 *
 * @author Keith Stanger
 */
public class StaticVarRewriter extends ErrorReportingASTVisitor {

  private boolean useAccessor(ASTNode currentNode, IVariableBinding var) {
    return BindingUtil.isStatic(var) && !BindingUtil.isPrimitiveConstant(var)
        && !Types.getTypeBinding(ASTUtil.getOwningType(currentNode)).getTypeDeclaration().isEqualTo(
            var.getDeclaringClass().getTypeDeclaration());
  }

  @Override
  public boolean visit(Assignment node) {
    AST ast = node.getAST();
    Expression lhs = node.getLeftHandSide();
    IVariableBinding lhsVar = Types.getVariableBinding(lhs);
    if (lhsVar != null && useAccessor(node, lhsVar)) {
      boolean isPrimitive = lhsVar.getType().isPrimitive();
      if (node.getOperator() == Assignment.Operator.ASSIGN && !isPrimitive) {
        Expression newValue = NodeCopier.copySubtree(ast, node.getRightHandSide());
        ASTUtil.setProperty(node, newSetterInvocation(ast, lhsVar, newValue));
        newValue.accept(this);
        return false;
      } else if (isPrimitive) {
        ASTUtil.setProperty(lhs, newGetterInvocation(ast, lhsVar, true));
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
    IVariableBinding var = Types.getVariableBinding(node);
    if (var != null && useAccessor(node, var)) {
      ASTUtil.setProperty(node, newGetterInvocation(node.getAST(), var, false));
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
    IVariableBinding operandVar = Types.getVariableBinding(node.getOperand());
    PostfixExpression.Operator op = node.getOperator();
    boolean isIncOrDec = op == PostfixExpression.Operator.INCREMENT
        || op == PostfixExpression.Operator.DECREMENT;
    if (isIncOrDec && operandVar != null && useAccessor(node, operandVar)) {
      node.setOperand(newGetterInvocation(node.getAST(), operandVar, true));
      return false;
    }
    return true;
  }

  @Override
  public boolean visit(PrefixExpression node) {
    IVariableBinding operandVar = Types.getVariableBinding(node.getOperand());
    PrefixExpression.Operator op = node.getOperator();
    boolean isIncOrDec = op == PrefixExpression.Operator.INCREMENT
        || op == PrefixExpression.Operator.DECREMENT;
    if (isIncOrDec && operandVar != null && useAccessor(node, operandVar)) {
      node.setOperand(newGetterInvocation(node.getAST(), operandVar, true));
      return false;
    }
    return true;
  }

  private MethodInvocation newGetterInvocation(AST ast, IVariableBinding var, boolean assignable) {
    ITypeBinding declaringType = var.getDeclaringClass().getTypeDeclaration();
    String getterName = var.isEnumConstant() ? NameTable.getName(var) :
        NameTable.getStaticAccessorName(var.getName());
    if (assignable) {
      getterName += "Ref";
    }
    IOSMethod iosMethod = IOSMethod.create(NameTable.getFullName(declaringType) + " " + getterName);
    IOSMethodBinding binding = IOSMethodBinding.newMethod(
        iosMethod, Modifier.PUBLIC | Modifier.STATIC, var.getType(), declaringType);
    MethodInvocation invocation = ASTFactory.newMethodInvocation(
        ast, binding, ASTFactory.newSimpleName(ast, declaringType));
    if (assignable) {
      invocation = ASTFactory.newDereference(ast, invocation);
    }
    return invocation;
  }

  private MethodInvocation newSetterInvocation(AST ast, IVariableBinding var, Expression value) {
    ITypeBinding varType = var.getType();
    ITypeBinding declaringType = var.getDeclaringClass();
    IOSMethod iosMethod = IOSMethod.create(String.format(
        "%s set%s:(%s)value", NameTable.getFullName(declaringType),
        NameTable.capitalize(var.getName()), NameTable.getSpecificObjCType(varType)));
    IOSMethodBinding binding = IOSMethodBinding.newMethod(
        iosMethod, Modifier.PUBLIC | Modifier.STATIC, varType, declaringType);
    binding.addParameter(varType);
    MethodInvocation invocation = ASTFactory.newMethodInvocation(
        ast, binding, ASTFactory.newSimpleName(ast, declaringType));
    ASTUtil.getArguments(invocation).add(value);
    return invocation;
  }
}
