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
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.BooleanLiteral;
import com.google.devtools.j2objc.ast.CStringLiteral;
import com.google.devtools.j2objc.ast.CharacterLiteral;
import com.google.devtools.j2objc.ast.CommaExpression;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.NumberLiteral;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.SuperFieldAccess;
import com.google.devtools.j2objc.ast.SynchronizedStatement;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.types.PointerType;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import com.google.j2objc.annotations.RetainedLocalRef;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Rewrites certain operators, such as object assignment, into appropriate
 * method calls.
 *
 * @author Keith Stanger
 */
public class OperatorRewriter extends UnitTreeVisitor {

  private final LinkedList<Set<VariableElement>> retainedLocalCandidateStack = new LinkedList<>();
  private Set<VariableElement> retainedLocalCandidates = new HashSet<>();
  private boolean isSynchronizedMethod = false;

  public OperatorRewriter(CompilationUnit unit) {
    super(unit);
  }

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
        && typeUtil.isAssignable(
            typeUtil.getJavaString().asType(), assignment.getLeftHandSide().getTypeMirror());
  }

  @Override
  public void endVisit(InfixExpression node) {
    InfixExpression.Operator op = node.getOperator();
    TypeMirror nodeType = node.getTypeMirror();
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
        FunctionElement element = new FunctionElement(funcName, nodeType, null)
            .addParameters(leftOperand.getTypeMirror(), rightOperand.getTypeMirror());
        FunctionInvocation invocation = new FunctionInvocation(element, nodeType);
        List<Expression> args = invocation.getArguments();
        args.add(leftOperand);
        args.add(rightOperand);
        leftOperand = invocation;
      }

      node.replaceWith(leftOperand);
    } else if (op == InfixExpression.Operator.PLUS && typeUtil.isString(nodeType)
               && !isStringAppend(node.getParent())) {
      rewriteStringConcatenation(node);
    }
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    isSynchronizedMethod = Modifier.isSynchronized(node.getModifiers());
    retainedLocalCandidates.addAll(
        node.getParameters().stream()
            .map(VariableDeclaration::getVariableElement)
            .filter(v -> !v.asType().getKind().isPrimitive())
            .collect(Collectors.toList()));
    return true;
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    retainedLocalCandidateStack.clear();
    retainedLocalCandidates.clear();
    isSynchronizedMethod = false;
  }

  @Override
  public boolean visit(SynchronizedStatement node) {
    retainedLocalCandidateStack.add(retainedLocalCandidates);
    retainedLocalCandidates = new HashSet<>();
    return true;
  }

  @Override
  public void endVisit(SynchronizedStatement node) {
    retainedLocalCandidates = retainedLocalCandidateStack.removeLast();
  }

  @Override
  public void endVisit(ReturnStatement node) {
    Expression expr = node.getExpression();
    if ((isSynchronizedMethod || !retainedLocalCandidateStack.isEmpty()) && expr != null
        && !expr.getTypeMirror().getKind().isPrimitive()) {
      rewriteRetainedLocal(expr);
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
    VariableElement var = node.getVariableElement();
    if (initializer != null) {
      initializer.accept(this);
      handleRetainedLocal(var, node.getInitializer());
    }
    if (!var.asType().getKind().isPrimitive()) {
      retainedLocalCandidates.add(var);
    }
    return false;
  }

  @Override
  public void endVisit(VariableDeclarationFragment node) {
    if (options.useReferenceCounting()) {
      Expression initializer = node.getInitializer();
      if (initializer != null) {
        VariableElement var = node.getVariableElement();
        if (!var.asType().getKind().isPrimitive()
            && !ElementUtil.isFinal(var)
            && !ElementUtil.isVolatile(var)
            && !isRetainedLocal(var)
            && !TypeUtil.isArray(var.asType())
            && !(var.asType() instanceof PointerType)) {
          if (initializer instanceof FieldAccess || initializer instanceof Name) {
            VariableElement initializerVar = TreeUtil.getVariableElement(initializer);
            if (!ElementUtil.isVolatile(initializerVar)
                && !(ElementUtil.isStatic(initializerVar) && ElementUtil.isFinal(initializerVar))) {
              rewriteRetainedLocal(initializer);
            }
          } else if (initializer instanceof MethodInvocation) {
            ExecutableElement method = ((MethodInvocation) initializer).getExecutableElement();
            if (!typeUtil.isMappedClass((TypeElement) method.getEnclosingElement())
                && !ElementUtil.isStatic(method)
                && !ElementUtil.isDefault(method)) {
              rewriteRetainedLocal(initializer);
            }
          }
        }
      }
    }
  }

  private boolean isRetainedLocal(VariableElement var) {
    if (ElementUtil.isLocalVariable(var)
        && ElementUtil.hasAnnotation(var, RetainedLocalRef.class)) {
      return true;
    }
    for (Set<VariableElement> candidates : retainedLocalCandidateStack) {
      if (candidates.contains(var)) {
        return true;
      }
    }
    return false;
  }

  private void rewriteRetainedLocal(Expression expr) {
    if (expr.getKind() == TreeNode.Kind.STRING_LITERAL) {
      return;
    }
    FunctionElement element =
        new FunctionElement("JreRetainedLocalValue", TypeUtil.ID_TYPE, null);
    FunctionInvocation invocation = new FunctionInvocation(element, expr.getTypeMirror());
    expr.replaceWith(invocation);
    invocation.addArgument(expr);
  }

  private void handleRetainedLocal(VariableElement var, Expression rhs) {
    if (isRetainedLocal(var)) {
      rewriteRetainedLocal(rhs);
    }
  }

  private void rewriteVolatileLoad(Expression node) {
    VariableElement var = TreeUtil.getVariableElement(node);
    if (var != null && ElementUtil.isVolatile(var) && !TranslationUtil.isAssigned(node)) {
      TypeMirror type = node.getTypeMirror();
      TypeMirror declaredType = type.getKind().isPrimitive() ? type : TypeUtil.ID_TYPE;
      String funcName = "JreLoadVolatile" + NameTable.capitalize(declaredType.toString());
      FunctionElement element = new FunctionElement(funcName, declaredType, null)
          .addParameters(TypeUtil.ID_PTR_TYPE);
      FunctionInvocation invocation = new FunctionInvocation(element, type);
      node.replaceWith(invocation);
      invocation.addArgument(new PrefixExpression(
          new PointerType(type), PrefixExpression.Operator.ADDRESS_OF, node));
    }
  }

  private String getAssignmentFunctionName(
      Assignment node, VariableElement var, boolean isRetainedWith) {
    if (!ElementUtil.isField(var)) {
      return null;
    }
    TypeMirror type = var.asType();
    boolean isPrimitive = type.getKind().isPrimitive();
    boolean isStrong = !isPrimitive && !ElementUtil.isWeakReference(var);
    boolean isVolatile = ElementUtil.isVolatile(var);

    if (isRetainedWith) {
      return isVolatile ? "JreVolatileRetainedWithAssign" : "JreRetainedWithAssign";
    }

    if (isVolatile) {
      // We can't use the "AndConsume" optimization for volatile objects because that might leave
      // the newly created object vulnerable to being deallocated by another thread assigning to the
      // same field.
      return isStrong ? "JreVolatileStrongAssign" : "JreAssignVolatile"
          + (isPrimitive ? NameTable.capitalize(TypeUtil.getName(type)) : "Id");
    }

    if (isStrong && options.useReferenceCounting()) {
      String funcName = "JreStrongAssign";
      Expression retainedRhs = TranslationUtil.retainResult(node.getRightHandSide());
      if (retainedRhs != null) {
        funcName += "AndConsume";
        node.setRightHandSide(retainedRhs);
      }
      return funcName;
    }

    return null;
  }

  // Counter to create unique local variables for the RetainedWith target.
  private int rwCount = 0;

  // Gets the target object for a call to the RetainedWith wrapper.
  private Expression getRetainedWithTarget(Assignment node, VariableElement var) {
    Expression lhs = node.getLeftHandSide();
    if (!(lhs instanceof FieldAccess)) {
      return new ThisExpression(ElementUtil.getDeclaringClass(var).asType());
    }
    // To avoid duplicating the target expression we must save the result to a local variable.
    FieldAccess fieldAccess = (FieldAccess) lhs;
    Expression target = fieldAccess.getExpression();
    VariableElement targetVar = GeneratedVariableElement.newLocalVar(
        "__rw$" + rwCount++, target.getTypeMirror(), null);
    TreeUtil.asStatementList(TreeUtil.getOwningStatement(lhs))
        .add(0, new VariableDeclarationStatement(targetVar, null));
    fieldAccess.setExpression(new SimpleName(targetVar));
    CommaExpression commaExpr = new CommaExpression(
        new Assignment(new SimpleName(targetVar), target));
    node.replaceWith(commaExpr);
    commaExpr.addExpression(node);
    return new SimpleName(targetVar);
  }

  private void rewriteRegularAssignment(Assignment node) {
    VariableElement var = TreeUtil.getVariableElement(node.getLeftHandSide());
    if (var == null) {
      return;
    }
    handleRetainedLocal(var, node.getRightHandSide());
    boolean isRetainedWith = ElementUtil.isRetainedWithField(var);
    String funcName = getAssignmentFunctionName(node, var, isRetainedWith);
    if (funcName == null) {
      return;
    }
    TypeMirror type = node.getTypeMirror();
    TypeMirror idType = TypeUtil.ID_TYPE;
    TypeMirror declaredType = type.getKind().isPrimitive() ? type : idType;
    Expression lhs = node.getLeftHandSide();
    FunctionElement element = new FunctionElement(funcName, declaredType, null);
    FunctionInvocation invocation = new FunctionInvocation(element, type);
    List<Expression> args = invocation.getArguments();
    if (isRetainedWith) {
      element.addParameters(idType);
      args.add(getRetainedWithTarget(node, var));
    }
    element.addParameters(TypeUtil.ID_PTR_TYPE, idType);
    args.add(new PrefixExpression(
        new PointerType(lhs.getTypeMirror()), PrefixExpression.Operator.ADDRESS_OF,
        TreeUtil.remove(lhs)));
    args.add(TreeUtil.remove(node.getRightHandSide()));
    node.replaceWith(invocation);
  }

  private static String intSizePostfix(TypeMirror type) {
    switch (type.getKind()) {
      case INT: return "32";
      case LONG: return "64";
      default:
        throw new AssertionError("Type expected to be int or long but was: " + type);
    }
  }

  private static String getInfixFunction(InfixExpression.Operator op, TypeMirror nodeType) {
    switch (op) {
      case DIVIDE:
        switch (nodeType.getKind()) {
          case INT: return "JreIntDiv";
          case LONG: return "JreLongDiv";
          default: return null;

        }
      case REMAINDER:
        switch (nodeType.getKind()) {
          case INT: return "JreIntMod";
          case LONG: return "JreLongMod";
          case FLOAT: return "fmodf";
          case DOUBLE: return "fmod";
          default: return null;
        }
      case LEFT_SHIFT:
        return "JreLShift" + intSizePostfix(nodeType);
      case RIGHT_SHIFT_SIGNED:
        return "JreRShift" + intSizePostfix(nodeType);
      case RIGHT_SHIFT_UNSIGNED:
        return "JreURShift" + intSizePostfix(nodeType);
      default:
        return null;
    }
  }

  private static boolean isVolatile(Expression varNode) {
    VariableElement var = TreeUtil.getVariableElement(varNode);
    return var != null && ElementUtil.isVolatile(var);
  }

  private static boolean shouldRewriteCompoundAssign(Assignment node) {
    Expression lhs = node.getLeftHandSide();
    TypeMirror lhsType = lhs.getTypeMirror();
    TypeMirror rhsType = node.getRightHandSide().getTypeMirror();
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
        return isVolatile(lhs) || TypeUtil.isFloatingPoint(lhsType)
            || TypeUtil.isFloatingPoint(rhsType);
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
    TypeKind lhsKind = node.getLeftHandSide().getTypeMirror().getKind();
    TypeKind rhsKind = node.getRightHandSide().getTypeMirror().getKind();
    if (lhsKind == TypeKind.DOUBLE || rhsKind == TypeKind.DOUBLE) {
      return "D";
    }
    if (lhsKind == TypeKind.FLOAT || rhsKind == TypeKind.FLOAT) {
      return "F";
    }
    if (lhsKind == TypeKind.LONG || rhsKind == TypeKind.LONG) {
      return "J";
    }
    return "I";
  }

  private void rewriteCompoundAssign(Assignment node) {
    if (!shouldRewriteCompoundAssign(node)) {
      return;
    }
    Expression lhs = node.getLeftHandSide();
    Expression rhs = node.getRightHandSide();
    TypeMirror lhsType = lhs.getTypeMirror();
    TypeMirror lhsPointerType = new PointerType(lhsType);
    String funcName = "Jre" + node.getOperator().getName() + (isVolatile(lhs) ? "Volatile" : "")
        + NameTable.capitalize(lhsType.toString()) + getPromotionSuffix(node);
    FunctionElement element = new FunctionElement(funcName, lhsType, null)
        .addParameters(lhsPointerType, rhs.getTypeMirror());
    FunctionInvocation invocation = new FunctionInvocation(element, lhsType);
    List<Expression> args = invocation.getArguments();
    args.add(new PrefixExpression(
        lhsPointerType, PrefixExpression.Operator.ADDRESS_OF, TreeUtil.remove(lhs)));
    args.add(TreeUtil.remove(rhs));
    node.replaceWith(invocation);
  }

  private CStringLiteral getStrcatTypesCString(List<Expression> operands) {
    StringBuilder typeArg = new StringBuilder();
    for (Expression expr : operands) {
      typeArg.append(getStringConcatenationTypeCharacter(expr));
    }
    return new CStringLiteral(typeArg.toString());
  }

  private void rewriteStringConcatenation(InfixExpression node) {
    List<Expression> childOperands = node.getOperands();
    List<Expression> operands = Lists.newArrayListWithCapacity(childOperands.size());
    TreeUtil.moveList(childOperands, operands);

    operands = coalesceStringLiterals(operands);
    if (operands.size() == 1 && typeUtil.isString(operands.get(0).getTypeMirror())) {
      node.replaceWith(operands.get(0));
      return;
    }

    TypeMirror stringType = typeUtil.getJavaString().asType();
    FunctionElement element = new FunctionElement("JreStrcat", stringType, null)
        .addParameters(TypeUtil.NATIVE_CHAR_PTR)
        .setIsVarargs(true);
    FunctionInvocation invocation = new FunctionInvocation(element, stringType);
    List<Expression> args = invocation.getArguments();
    args.add(getStrcatTypesCString(operands));
    args.addAll(operands);
    node.replaceWith(invocation);
  }

  private List<Expression> getStringAppendOperands(Assignment node) {
    Expression rhs = node.getRightHandSide();
    if (rhs instanceof InfixExpression && typeUtil.isString(rhs.getTypeMirror())) {
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
    TypeMirror lhsType = lhs.getTypeMirror();
    String funcName = "JreStrAppend" + translationUtil.getOperatorFunctionModifier(lhs);
    FunctionElement element = new FunctionElement(funcName, TypeUtil.ID_TYPE, null)
        .addParameters(TypeUtil.ID_PTR_TYPE, TypeUtil.NATIVE_CHAR_PTR)
        .setIsVarargs(true);
    FunctionInvocation invocation = new FunctionInvocation(element, lhsType);
    List<Expression> args = invocation.getArguments();
    args.add(new PrefixExpression(
        new PointerType(lhsType), PrefixExpression.Operator.ADDRESS_OF, TreeUtil.remove(lhs)));
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
      args.add(new CharacterLiteral(literal.charAt(0), typeUtil));
    } else {
      args.add(new StringLiteral(literal, typeUtil));
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
    TypeMirror operandType = operand.getTypeMirror();
    if (operandType.getKind().isPrimitive()) {
      return TypeUtil.getBinaryName(operandType).charAt(0);
    } else if (typeUtil.isString(operandType)) {
      return '$';
    } else {
      return '@';
    }
  }
}
