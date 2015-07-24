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
import com.google.devtools.j2objc.ast.NumberLiteral;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.SuperFieldAccess;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Rewrites certain operators, such as object assignment, into appropriate
 * method calls.
 *
 * @author Keith Stanger
 */
public class OperatorRewriter extends TreeVisitor {

  @Override
  public void endVisit(Assignment node) {
    if (node.getOperator() == Assignment.Operator.ASSIGN) {
      rewriteRegularAssignment(node);
    } else if (isStringAppend(node)) {
      rewriteStringAppend(node);
    } else {
      rewriteCompoundAssign(node);
    }
  }

  private boolean isStringAppend(TreeNode node) {
    if (!(node instanceof Assignment)) {
      return false;
    }
    Assignment assignment = (Assignment) node;
    return assignment.getOperator() == Assignment.Operator.PLUS_ASSIGN
        && typeEnv.resolveJavaType("java.lang.String").isAssignmentCompatible(
            assignment.getLeftHandSide().getTypeBinding());
  }

  @Override
  public void endVisit(InfixExpression node) {
    InfixExpression.Operator op = node.getOperator();
    ITypeBinding nodeType = node.getTypeBinding();
    String funcName = getInfixFunction(op, nodeType);
    if (funcName != null) {
      Iterator<Expression> operandIter = node.getOperands().iterator();
      Expression leftOperand = operandIter.next();
      operandIter.remove();

      // This takes extended operands into consideration. If a node has three operands, o1 o2 o3,
      // the function invocations should be like f(f(o1, o2), o3), given that the infix operators
      // translated here are all left-associative.
      while (operandIter.hasNext()) {
        Expression rightOperand = operandIter.next();
        operandIter.remove();
        FunctionInvocation invocation = new FunctionInvocation(funcName, nodeType, nodeType, null);
        List<Expression> args = invocation.getArguments();
        args.add(leftOperand);
        args.add(rightOperand);
        leftOperand = invocation;
      }

      node.replaceWith(leftOperand);
    } else if (op == InfixExpression.Operator.PLUS && typeEnv.isStringType(nodeType)
        && !isStringAppend(node.getParent())) {
      rewriteStringConcatenation(node);
    }
  }

  @Override
  public boolean visit(FieldAccess node) {
    rewriteVolatileLoad(node);
    node.getExpression().accept(this);
    return false;
  }

  @Override
  public boolean visit(SuperFieldAccess node) {
    rewriteVolatileLoad(node);
    return false;
  }

  @Override
  public boolean visit(QualifiedName node) {
    rewriteVolatileLoad(node);
    return false;
  }

  @Override
  public boolean visit(SimpleName node) {
    rewriteVolatileLoad(node);
    return false;
  }

  @Override
  public boolean visit(VariableDeclarationFragment node) {
    // Skip name so that it doesn't get mistaken for a variable load.
    Expression initializer = node.getInitializer();
    if (initializer != null) {
      initializer.accept(this);
    }
    return false;
  }

  private void rewriteVolatileLoad(Expression node) {
    IVariableBinding var = TreeUtil.getVariableBinding(node);
    if (var != null && BindingUtil.isVolatile(var) && !TranslationUtil.isAssigned(node)) {
      ITypeBinding type = node.getTypeBinding();
      ITypeBinding declaredType = type.isPrimitive() ? type : typeEnv.resolveIOSType("id");
      String funcName = "JreLoadVolatile" + NameTable.capitalize(declaredType.getName());
      FunctionInvocation invocation = new FunctionInvocation(funcName, type, declaredType, null);
      node.replaceWith(invocation);
      invocation.getArguments().add(new PrefixExpression(
          typeEnv.getPointerType(type), PrefixExpression.Operator.ADDRESS_OF, node));
    }
  }

  private String getAssignmentFunctionName(Assignment node) {
    IVariableBinding var = TreeUtil.getVariableBinding(node.getLeftHandSide());
    if (var == null || !var.isField() || var.isEnumConstant()) {
      return null;
    }
    ITypeBinding type = var.getType();
    boolean isPrimitive = type.isPrimitive();
    boolean isStrong = !isPrimitive && !BindingUtil.isWeakReference(var);
    boolean isVolatile = BindingUtil.isVolatile(var);

    if (isStrong) {
      String funcName = null;
      if (isVolatile) {
        funcName = "JreVolatileStrongAssign";
      } else if (Options.useReferenceCounting()) {
        funcName = "JreStrongAssign";
      }
      if (funcName != null) {
        Expression retainedRhs = TranslationUtil.retainResult(node.getRightHandSide());
        if (retainedRhs != null) {
          funcName += "AndConsume";
          node.setRightHandSide(retainedRhs);
        }
      }
      return funcName;
    }

    if (isVolatile) {
      return "JreAssignVolatile" + (isPrimitive ? NameTable.capitalize(type.getName()) : "Id");
    }
    return null;
  }

  private void rewriteRegularAssignment(Assignment node) {
    String funcName = getAssignmentFunctionName(node);
    if (funcName == null) {
      return;
    }
    ITypeBinding type = node.getTypeBinding();
    ITypeBinding declaredType = type.isPrimitive() ? type : typeEnv.resolveIOSType("id");
    Expression lhs = node.getLeftHandSide();
    FunctionInvocation invocation = new FunctionInvocation(funcName, type, declaredType, null);
    List<Expression> args = invocation.getArguments();
    args.add(new PrefixExpression(
        typeEnv.getPointerType(lhs.getTypeBinding()), PrefixExpression.Operator.ADDRESS_OF,
        TreeUtil.remove(lhs)));
    args.add(TreeUtil.remove(node.getRightHandSide()));
    node.replaceWith(invocation);
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
        return "JreLShift" + intOrLong(nodeType);
      case RIGHT_SHIFT_SIGNED:
        return "JreRShift" + intOrLong(nodeType);
      case RIGHT_SHIFT_UNSIGNED:
        return "JreURShift" + intOrLong(nodeType);
      default:
        return null;
    }
  }

  private static boolean isVolatile(Expression varNode) {
    IVariableBinding var = TreeUtil.getVariableBinding(varNode);
    return var != null && BindingUtil.isVolatile(var);
  }

  private static boolean shouldRewriteCompoundAssign(Assignment node) {
    Expression lhs = node.getLeftHandSide();
    ITypeBinding lhsType = lhs.getTypeBinding();
    ITypeBinding rhsType = node.getRightHandSide().getTypeBinding();
    switch (node.getOperator()) {
      case LEFT_SHIFT_ASSIGN:
      case RIGHT_SHIFT_SIGNED_ASSIGN:
      case RIGHT_SHIFT_UNSIGNED_ASSIGN:
        return true;
      case PLUS_ASSIGN:
      case MINUS_ASSIGN:
      case TIMES_ASSIGN:
      case DIVIDE_ASSIGN:
      case REMAINDER_ASSIGN:
        return isVolatile(lhs) || BindingUtil.isFloatingPoint(lhsType)
            || BindingUtil.isFloatingPoint(rhsType);
      default:
        return isVolatile(lhs);
    }
  }

  private static boolean needsPromotionSuffix(Assignment.Operator op) {
    switch (op) {
      case PLUS_ASSIGN:
      case MINUS_ASSIGN:
      case TIMES_ASSIGN:
      case DIVIDE_ASSIGN:
      case REMAINDER_ASSIGN:
        return true;
      default:
        return false;
    }
  }

  /**
   * Some operator functions are given a suffix indicating the promotion type of
   * the operands according to JLS 5.6.2.
   */
  private static String getPromotionSuffix(Assignment node) {
    if (!needsPromotionSuffix(node.getOperator())) {
      return "";
    }
    char lhs = node.getLeftHandSide().getTypeBinding().getBinaryName().charAt(0);
    char rhs = node.getRightHandSide().getTypeBinding().getBinaryName().charAt(0);
    if (lhs == 'D' || rhs == 'D') {
      return "D";
    }
    if (lhs == 'F' || rhs == 'F') {
      return "F";
    }
    if (lhs == 'J' || rhs == 'J') {
      return "J";
    }
    return "I";
  }

  private void rewriteCompoundAssign(Assignment node) {
    if (!shouldRewriteCompoundAssign(node)) {
      return;
    }
    Expression lhs = node.getLeftHandSide();
    ITypeBinding lhsType = lhs.getTypeBinding();
    String funcName = "Jre" + node.getOperator().getName() + (isVolatile(lhs) ? "Volatile" : "")
        + NameTable.capitalize(lhsType.getName()) + getPromotionSuffix(node);
    FunctionInvocation invocation = new FunctionInvocation(funcName, lhsType, lhsType, null);
    List<Expression> args = invocation.getArguments();
    args.add(new PrefixExpression(
        typeEnv.getPointerType(lhsType), PrefixExpression.Operator.ADDRESS_OF,
        TreeUtil.remove(lhs)));
    args.add(TreeUtil.remove(node.getRightHandSide()));
    node.replaceWith(invocation);
  }

  private CStringLiteral getStrcatTypesCString(List<Expression> operands) {
    StringBuilder typeArg = new StringBuilder();
    for (Expression expr : operands) {
      typeArg.append(getStringConcatenationTypeCharacter(expr));
    }
    return new CStringLiteral(typeArg.toString(), typeEnv);
  }

  private void rewriteStringConcatenation(InfixExpression node) {
    List<Expression> childOperands = node.getOperands();
    List<Expression> operands = Lists.newArrayListWithCapacity(childOperands.size());
    TreeUtil.moveList(childOperands, operands);

    operands = coalesceStringLiterals(operands);
    if (operands.size() == 1 && typeEnv.isStringType(operands.get(0).getTypeBinding())) {
      node.replaceWith(operands.get(0));
      return;
    }

    ITypeBinding stringType = typeEnv.resolveIOSType("NSString");
    FunctionInvocation invocation =
        new FunctionInvocation("JreStrcat", stringType, stringType, null);
    List<Expression> args = invocation.getArguments();
    args.add(getStrcatTypesCString(operands));
    args.addAll(operands);
    node.replaceWith(invocation);
  }

  private List<Expression> getStringAppendOperands(Assignment node) {
    Expression rhs = node.getRightHandSide();
    if (rhs instanceof InfixExpression) {
      InfixExpression infixExpr = (InfixExpression) rhs;
      if (infixExpr.getOperator() == InfixExpression.Operator.PLUS) {
        List<Expression> operands = infixExpr.getOperands();
        List<Expression> result = Lists.newArrayListWithCapacity(operands.size());
        TreeUtil.moveList(operands, result);
        return coalesceStringLiterals(result);
      }
    }
    return Collections.singletonList(TreeUtil.remove(rhs));
  }

  private void rewriteStringAppend(Assignment node) {
    List<Expression> operands = getStringAppendOperands(node);
    Expression lhs = node.getLeftHandSide();
    ITypeBinding lhsType = lhs.getTypeBinding();
    ITypeBinding idType = typeEnv.resolveIOSType("id");
    String funcName = "JreStrAppend" + TranslationUtil.getOperatorFunctionModifier(lhs);
    FunctionInvocation invocation = new FunctionInvocation(funcName, lhsType, idType, null);
    List<Expression> args = invocation.getArguments();
    args.add(new PrefixExpression(
        typeEnv.getPointerType(lhsType), PrefixExpression.Operator.ADDRESS_OF,
        TreeUtil.remove(lhs)));
    args.add(getStrcatTypesCString(operands));
    args.addAll(operands);
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
