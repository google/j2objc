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
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.BooleanLiteral;
import com.google.devtools.j2objc.ast.CStringLiteral;
import com.google.devtools.j2objc.ast.CharacterLiteral;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NullLiteral;
import com.google.devtools.j2objc.ast.NumberLiteral;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;

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
      if (var == null || var.getType().isPrimitive() || !Options.useReferenceCounting()
          || var.isEnumConstant()) {
        return;
      }
      if (BindingUtil.isStatic(var)) {
        node.replaceWith(newStaticAssignInvocation(var, rhs));
      } else if (var.isField() && !BindingUtil.isWeakReference(var) && !inDeallocMethod(lhs)) {
        Expression target = getTarget(lhs, var);
        node.replaceWith(newFieldSetterInvocation(var, target, rhs));
      }
    } else {
      String funcName = getOperatorAssignFunction(op, lhsType, rhsType);
      if (funcName != null) {
        FunctionInvocation invocation = new FunctionInvocation(funcName, lhsType, lhsType, null);
        List<Expression> args = invocation.getArguments();
        args.add(new PrefixExpression(
            typeEnv.getPointerType(lhsType), PrefixExpression.Operator.ADDRESS_OF,
            TreeUtil.remove(lhs)));
        args.add(TreeUtil.remove(rhs));
        node.replaceWith(invocation);
      }
    }
  }

  private boolean inDeallocMethod(TreeNode node) {
    MethodDeclaration method = TreeUtil.getOwningMethod(node);
    return method != null && BindingUtil.isDestructor(method.getMethodBinding());
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
    } else if (op == InfixExpression.Operator.PLUS && typeEnv.isStringType(nodeType)) {
      rewriteStringConcatenation(node);
    }
  }

  private FunctionInvocation newStaticAssignInvocation(IVariableBinding var, Expression value) {
    String assignFunc = "JreStrongAssign";
    Expression retainedValue = TranslationUtil.retainResult(value);
    if (retainedValue != null) {
      assignFunc = "JreStrongAssignAndConsume";
      value = retainedValue;
    }
    FunctionInvocation invocation = new FunctionInvocation(
        assignFunc, value.getTypeBinding(), typeEnv.resolveIOSType("id"), null);
    List<Expression> args = invocation.getArguments();
    args.add(new PrefixExpression(
        typeEnv.getPointerType(var.getType()), PrefixExpression.Operator.ADDRESS_OF,
        new SimpleName(var)));
    args.add(new NullLiteral());
    args.add(TreeUtil.remove(value));
    return invocation;
  }

  private FunctionInvocation newFieldSetterInvocation(
      IVariableBinding var, Expression instance, Expression value) {
    ITypeBinding varType = var.getType();
    ITypeBinding declaringType = var.getDeclaringClass().getTypeDeclaration();
    String setterFormat = "%s_set_%s";
    Expression retainedValue = TranslationUtil.retainResult(value);
    if (retainedValue != null) {
      setterFormat = "%s_setAndConsume_%s";
      value = retainedValue;
    }
    String setterName = String.format(setterFormat, nameTable.getFullName(declaringType),
        NameTable.javaFieldToObjC(nameTable.getVariableName(var)));
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
        if (BindingUtil.isFloatingPoint(nodeType)) {
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
        if (BindingUtil.isFloatingPoint(lhsType) || BindingUtil.isFloatingPoint(rhsType)) {
          return "ModAssign" + lhsName;
        }
        return null;
      default:
        return null;
    }
  }

  private void rewriteStringConcatenation(InfixExpression node) {
    List<Expression> extendedOperands = node.getExtendedOperands();
    List<Expression> operands = Lists.newArrayListWithCapacity(extendedOperands.size() + 2);
    operands.add(TreeUtil.remove(node.getLeftOperand()));
    operands.add(TreeUtil.remove(node.getRightOperand()));
    TreeUtil.moveList(extendedOperands, operands);

    operands = coalesceStringLiterals(operands);
    if (operands.size() == 1 && typeEnv.isStringType(operands.get(0).getTypeBinding())) {
      node.replaceWith(operands.get(0));
      return;
    }

    ITypeBinding stringType = typeEnv.resolveIOSType("NSString");
    FunctionInvocation invocation =
        new FunctionInvocation("JreStrcat", stringType, stringType, null);
    List<Expression> args = invocation.getArguments();
    StringBuilder typeArg = new StringBuilder();
    for (Expression expr : operands) {
      typeArg.append(getStringConcatenationTypeCharacter(expr));
    }
    args.add(new CStringLiteral(typeArg.toString(), typeEnv));
    for (Expression expr : operands) {
      args.add(expr);
    }
    node.replaceWith(invocation);
  }

  private List<Expression> coalesceStringLiterals(List<Expression> rawOperands) {
    List<Expression> operands = Lists.newArrayListWithCapacity(rawOperands.size());
    String currentLiteral = null;
    for (Expression expr : rawOperands) {
      String literalValue = getLiteralStringValue(expr);
      if (literalValue != null) {
        currentLiteral = currentLiteral == null ? literalValue : currentLiteral + literalValue;
      } else {
        if (currentLiteral != null) {
          addStringLiteralArgument(operands, currentLiteral);
          currentLiteral = null;
        }
        operands.add(expr);
      }
    }
    if (currentLiteral != null) {
      addStringLiteralArgument(operands, currentLiteral);
    }
    return operands;
  }

  private void addStringLiteralArgument(List<Expression> args, String literal) {
    if (literal.length() == 0) {
      return;  // Skip it.
    } else if (literal.length() == 1) {
      args.add(new CharacterLiteral(literal.charAt(0), typeEnv));
    } else {
      args.add(new StringLiteral(literal, typeEnv));
    }
  }

  private static String getLiteralStringValue(Expression expr) {
    switch (expr.getKind()) {
      case STRING_LITERAL:
        String literalValue = ((StringLiteral) expr).getLiteralValue();
        if (UnicodeUtils.hasValidCppCharacters(literalValue)) {
          return literalValue;
        } else {
          return null;
        }
      case BOOLEAN_LITERAL:
        return String.valueOf(((BooleanLiteral) expr).booleanValue());
      case CHARACTER_LITERAL:
        return String.valueOf(((CharacterLiteral) expr).charValue());
      case NUMBER_LITERAL:
        return ((NumberLiteral) expr).getValue().toString();
      default:
        return null;
    }
  }

  /**
   * Returns a character to indicate the type of an argument.
   * '$' for String, '@' for other objects, and the binary name character for
   * the primitives.
   */
  private char getStringConcatenationTypeCharacter(Expression operand) {
    ITypeBinding operandType = operand.getTypeBinding();
    if (operandType.isPrimitive()) {
      return operandType.getBinaryName().charAt(0);
    } else if (typeEnv.isStringType(operandType)) {
      return '$';
    } else {
      return '@';
    }
  }
}
