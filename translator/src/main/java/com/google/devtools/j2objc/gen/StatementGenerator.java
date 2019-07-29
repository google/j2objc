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

package com.google.devtools.j2objc.gen;

import com.google.common.base.CharMatcher;
import com.google.devtools.j2objc.ast.ArrayAccess;
import com.google.devtools.j2objc.ast.ArrayCreation;
import com.google.devtools.j2objc.ast.ArrayInitializer;
import com.google.devtools.j2objc.ast.ArrayType;
import com.google.devtools.j2objc.ast.AssertStatement;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BooleanLiteral;
import com.google.devtools.j2objc.ast.BreakStatement;
import com.google.devtools.j2objc.ast.CStringLiteral;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.CatchClause;
import com.google.devtools.j2objc.ast.CharacterLiteral;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CommaExpression;
import com.google.devtools.j2objc.ast.ConditionalExpression;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.ContinueStatement;
import com.google.devtools.j2objc.ast.CreationReference;
import com.google.devtools.j2objc.ast.DoStatement;
import com.google.devtools.j2objc.ast.EmptyStatement;
import com.google.devtools.j2objc.ast.EnhancedForStatement;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionMethodReference;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.ForStatement;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.IfStatement;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.Initializer;
import com.google.devtools.j2objc.ast.InstanceofExpression;
import com.google.devtools.j2objc.ast.IntersectionType;
import com.google.devtools.j2objc.ast.LabeledStatement;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.MarkerAnnotation;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.NativeExpression;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.NormalAnnotation;
import com.google.devtools.j2objc.ast.NullLiteral;
import com.google.devtools.j2objc.ast.NumberLiteral;
import com.google.devtools.j2objc.ast.ParenthesizedExpression;
import com.google.devtools.j2objc.ast.PostfixExpression;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.PrimitiveType;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.QualifiedType;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SimpleType;
import com.google.devtools.j2objc.ast.SingleMemberAnnotation;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperFieldAccess;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.SuperMethodReference;
import com.google.devtools.j2objc.ast.SwitchCase;
import com.google.devtools.j2objc.ast.SwitchStatement;
import com.google.devtools.j2objc.ast.SynchronizedStatement;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.ThrowStatement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeNode.Kind;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TryStatement;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.ast.TypeMethodReference;
import com.google.devtools.j2objc.ast.UnionType;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationExpression;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.ast.WhileStatement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Returns an Objective-C equivalent of a Java AST node.
 *
 * @author Tom Ball
 */
public class StatementGenerator extends UnitTreeVisitor {

  private final SourceBuilder buffer;

  public static String generate(TreeNode node, int currentLine) {
    StatementGenerator generator = new StatementGenerator(node, currentLine);
    if (node == null) {
      throw new NullPointerException("cannot generate a null statement");
    }
    node.accept(generator);
    return generator.getResult();
  }

  private StatementGenerator(TreeNode node, int currentLine) {
    super(TreeUtil.getCompilationUnit(node));
    buffer = new SourceBuilder(options.emitLineDirectives(), currentLine);
  }

  private String getResult() {
    return buffer.toString();
  }

  private void printMethodInvocationNameAndArgs(String selector, List<Expression> args) {
    String[] selParts = selector.split(":");
    if (args.isEmpty()) {
      assert selParts.length == 1 && !selector.endsWith(":");
      buffer.append(' ');
      buffer.append(selector);
    } else {
      assert selParts.length == args.size();
      for (int i = 0; i < args.size(); i++) {
        buffer.append(' ');
        buffer.append(selParts[i]);
        buffer.append(':');
        args.get(i).accept(this);
      }
    }
  }

  @Override
  public boolean preVisit(TreeNode node) {
    super.preVisit(node);
    if (!(node instanceof Block)) {
      buffer.syncLineNumbers(node);
    }
    return true;
  }

  @Override
  public boolean visit(ArrayAccess node) {
    throw new AssertionError("ArrayAccess nodes are rewritten by ArrayRewriter.");
  }

  @Override
  public boolean visit(ArrayCreation node) {
    throw new AssertionError("ArrayCreation nodes are rewritten by ArrayRewriter.");
  }

  @Override
  public boolean visit(ArrayInitializer node) {
    javax.lang.model.type.ArrayType type = (javax.lang.model.type.ArrayType) node.getTypeMirror();
    TypeMirror componentType = type.getComponentType();
    buffer.append(UnicodeUtils.format("(%s[]){ ", NameTable.getPrimitiveObjCType(componentType)));
    for (Iterator<Expression> it = node.getExpressions().iterator(); it.hasNext(); ) {
      it.next().accept(this);
      if (it.hasNext()) {
        buffer.append(", ");
      }
    }
    buffer.append(" }");
    return false;
  }

  @Override
  public boolean visit(ArrayType node) {
    TypeElement iosArray = typeUtil.getIosArray(node.getTypeMirror().getComponentType());
    buffer.append(ElementUtil.getName(iosArray));
    return false;
  }

  @Override
  public boolean visit(AssertStatement node) {
    buffer.append("JreAssert(");
    acceptMacroArgument(node.getExpression());
    buffer.append(", ");
    if (node.getMessage() != null) {
      acceptMacroArgument(node.getMessage());
    } else {
      int startPos = node.getStartPosition();
      String assertStatementString =
          unit.getSource().substring(startPos, startPos + node.getLength());
      assertStatementString = CharMatcher.whitespace().trimFrom(assertStatementString);
      // Generates the following string:
      // filename.java:456 condition failed: foobar != fish.
      String msg = TreeUtil.getSourceFileName(unit) + ":" + node.getLineNumber()
          + " condition failed: " + assertStatementString;
      buffer.append(LiteralGenerator.generateStringLiteral(msg));
    }
    buffer.append(");\n");
    return false;
  }

  @Override
  public boolean visit(Assignment node) {
    node.getLeftHandSide().accept(this);
    buffer.append(' ');
    buffer.append(node.getOperator().toString());
    buffer.append(' ');
    node.getRightHandSide().accept(this);
    return false;
  }

  @Override
  public boolean visit(Block node) {
    if (node.hasAutoreleasePool()) {
      buffer.append("{\n@autoreleasepool ");
    }
    buffer.append("{\n");
    printStatements(node.getStatements());
    buffer.append("}\n");
    if (node.hasAutoreleasePool()) {
      buffer.append("}\n");
    }
    return false;
  }

  private void printStatements(List<?> statements) {
    for (Iterator<?> it = statements.iterator(); it.hasNext(); ) {
      Statement s = (Statement) it.next();
      s.accept(this);
    }
  }

  @Override
  public boolean visit(BooleanLiteral node) {
    buffer.append(node.booleanValue() ? "true" : "false");
    return false;
  }

  @Override
  public boolean visit(BreakStatement node) {
    if (node.getLabel() != null) {
      // Objective-C doesn't have a labeled break, so use a goto.
      buffer.append("goto ");
      node.getLabel().accept(this);
    } else {
      buffer.append("break");
    }
    buffer.append(";\n");
    return false;
  }

  @Override
  public boolean visit(CStringLiteral node) {
    buffer.append("\"");
    buffer.append(node.getLiteralValue());
    buffer.append("\"");
    return false;
  }

  @Override
  public boolean visit(CastExpression node) {
    buffer.append("(");
    buffer.append(nameTable.getObjCType(node.getType().getTypeMirror()));
    buffer.append(") ");
    node.getExpression().accept(this);
    return false;
  }

  @Override
  public boolean visit(CharacterLiteral node) {
    buffer.append(UnicodeUtils.escapeCharLiteral(node.charValue()));
    return false;
  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    throw new AssertionError("ClassInstanceCreation nodes are rewritten by Functionizer.");
  }

  @Override
  public boolean visit(CommaExpression node) {
    buffer.append('(');
    for (Iterator<Expression> it = node.getExpressions().iterator(); it.hasNext(); ) {
      Expression e = it.next();
      e.accept(this);
      if (it.hasNext()) {
        buffer.append(", ");
      }
    }
    buffer.append(')');
    return false;
  }

  @Override
  public boolean visit(ConditionalExpression node) {
    node.getExpression().accept(this);
    buffer.append(" ? ");
    node.getThenExpression().accept(this);
    buffer.append(" : ");
    node.getElseExpression().accept(this);
    return false;
  }

  @Override
  public boolean visit(ConstructorInvocation node) {
    throw new AssertionError("ConstructorInvocation nodes are rewritten by Functionizer.");
  }

  @Override
  public boolean visit(ContinueStatement node) {
    if (node.getLabel() != null) {
      // Objective-C doesn't have a labeled continue, so use a goto.
      buffer.append("goto ");
      node.getLabel().accept(this);
    } else {
      buffer.append("continue");
    }
    buffer.append(";\n");
    return false;
  }

  @Override
  public boolean visit(CreationReference node) {
    throw new AssertionError("CreationReference nodes are rewritten by LambdaRewriter.");
  }

  @Override
  public boolean visit(DoStatement node) {
    buffer.append("do ");
    node.getBody().accept(this);
    buffer.append(" while (");
    node.getExpression().accept(this);
    buffer.append(");\n");
    return false;
  }


  @Override
  public boolean visit(EmptyStatement node) {
    // Preserve line number difference with owner, to allow suppression of
    // clang empty-statement warnings in Java source.
    TreeNode parent = node.getParent();
    if (parent.getKind() != Kind.SWITCH_STATEMENT
        && node.getLineNumber() != parent.getLineNumber()) {
      buffer.newline();
      buffer.printIndent();
    }
    buffer.append(";\n");
    return false;
  }

  @Override
  public boolean visit(EnhancedForStatement node) {
    buffer.append("for (");
    node.getParameter().accept(this);
    buffer.append(" in ");
    node.getExpression().accept(this);
    buffer.append(") ");
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(ExpressionMethodReference node) {
    throw new AssertionError("ExpressionMethodReference nodes are rewritten by LambdaRewriter.");
  }

  @Override
  public boolean visit(ExpressionStatement node) {
    Expression expression = node.getExpression();
    TypeMirror type = expression.getTypeMirror();
    if (!type.getKind().isPrimitive() && !type.getKind().equals(TypeKind.VOID)
        && options.useARC() && (expression instanceof MethodInvocation
            || expression instanceof SuperMethodInvocation
            || expression instanceof FunctionInvocation)) {
      // Avoid clang warning that the return value is unused.
      buffer.append("(void) ");
    }
    expression.accept(this);
    buffer.append(";\n");
    return false;
  }

  @Override
  public boolean visit(FieldAccess node) {
    node.getExpression().accept(this);
    buffer.append("->");
    node.getName().accept(this);
    return false;
  }

  @Override
  public boolean visit(ForStatement node) {
    buffer.append("for (");
    for (Iterator<Expression> it = node.getInitializers().iterator(); it.hasNext(); ) {
      Expression next = it.next();
      next.accept(this);
      if (it.hasNext()) {
        buffer.append(", ");
      }
    }
    buffer.append("; ");
    if (node.getExpression() != null) {
      node.getExpression().accept(this);
    }
    buffer.append("; ");
    for (Iterator<Expression> it = node.getUpdaters().iterator(); it.hasNext(); ) {
      it.next().accept(this);
      if (it.hasNext()) {
        buffer.append(", ");
      }
    }
    buffer.append(") ");
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(FunctionInvocation node) {
    // If the function is actually a macro, then it's arguments may need to be wrapped in
    // parentheses.
    boolean isMacro = node.getFunctionElement().isMacro();
    buffer.append(node.getName());
    buffer.append('(');
    for (Iterator<Expression> iter = node.getArguments().iterator(); iter.hasNext(); ) {
      Expression arg = iter.next();
      if (isMacro) {
        acceptMacroArgument(arg);
      } else {
        arg.accept(this);
      }
      if (iter.hasNext()) {
        buffer.append(", ");
      }
    }
    buffer.append(')');
    return false;
  }

  @Override
  public boolean visit(IfStatement node) {
    buffer.append("if (");
    node.getExpression().accept(this);
    buffer.append(") ");
    node.getThenStatement().accept(this);
    if (node.getElseStatement() != null) {
      buffer.append(" else ");
      node.getElseStatement().accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(InfixExpression node) {
    InfixExpression.Operator op = node.getOperator();
    List<Expression> operands = node.getOperands();
    assert operands.size() >= 2;
    if ((op.equals(InfixExpression.Operator.EQUALS)
        || op.equals(InfixExpression.Operator.NOT_EQUALS))) {
      Expression lhs = operands.get(0);
      Expression rhs = operands.get(1);
      if (lhs instanceof StringLiteral || rhs instanceof StringLiteral) {
        if (!(lhs instanceof StringLiteral)) {
          // In case the lhs can't call isEqual.
          lhs = operands.get(1);
          rhs = operands.get(0);
        }
        buffer.append(op.equals(InfixExpression.Operator.NOT_EQUALS) ? "![" : "[");
        lhs.accept(this);
        buffer.append(" isEqual:");
        rhs.accept(this);
        buffer.append("]");
        return false;
      }
    }
    String opStr = ' ' + op.toString() + ' ';
    boolean isFirst = true;
    for (Expression operand : operands) {
      if (!isFirst) {
        buffer.append(opStr);
      }
      isFirst = false;
      operand.accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(InstanceofExpression node) {
    TypeElement type = TypeUtil.asTypeElement(node.getRightOperand().getTypeMirror());
    if (type != null && type.getKind().isInterface()) {
      // Our version of "isInstance" is faster than "conformsToProtocol".
      buffer.append(UnicodeUtils.format("[%s_class_() isInstance:", nameTable.getFullName(type)));
      node.getLeftOperand().accept(this);
      buffer.append(']');
    } else {
      buffer.append('[');
      node.getLeftOperand().accept(this);
      buffer.append(" isKindOfClass:[");
      node.getRightOperand().accept(this);
      buffer.append(" class]]");
    }
    return false;
  }

  @Override
  public boolean visit(IntersectionType node) {
    throw new AssertionError(
        "Intersection types should only occur in a cast expression,"
        + " and are handled by CastResolver");
  }

  @Override
  public boolean visit(LabeledStatement node) {
    node.getLabel().accept(this);
    buffer.append(": ");
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(LambdaExpression node) {
    throw new AssertionError(
        "Lambda expressions should have been rewritten by LambdaRewriter");
  }

  @Override
  public boolean visit(MarkerAnnotation node) {
    throw new AssertionError("Annotation nodes should not exist within method bodies.");
  }

  @Override
  public boolean visit(MethodInvocation node) {
    ExecutableElement element = node.getExecutableElement();
    assert element != null;

    // Object receiving the message, or null if it's a method in this class.
    Expression receiver = node.getExpression();
    buffer.append('[');
    if (ElementUtil.isStatic(element)) {
      buffer.append(nameTable.getFullName(ElementUtil.getDeclaringClass(element)));
    } else if (receiver != null) {
      receiver.accept(this);
    } else {
      buffer.append("self");
    }
    printMethodInvocationNameAndArgs(nameTable.getMethodSelector(element), node.getArguments());
    buffer.append(']');
    return false;
  }

  @Override
  public boolean visit(NativeExpression node) {
    buffer.append(node.getCode());
    return false;
  }

  @Override
  public boolean visit(NativeStatement node) {
    buffer.append(node.getCode());
    buffer.append('\n');
    return false;
  }

  @Override
  public boolean visit(NormalAnnotation node) {
    throw new AssertionError("Annotation nodes should not exist within method bodies.");
  }

  @Override
  public boolean visit(NullLiteral node) {
    buffer.append("nil");
    return false;
  }

  @Override
  public boolean visit(NumberLiteral node) {
    String token = node.getToken();
    if (token != null) {
      buffer.append(LiteralGenerator.fixNumberToken(token, node.getTypeMirror().getKind()));
    } else {
      buffer.append(LiteralGenerator.generate(node.getValue()));
    }
    return false;
  }

  @Override
  public boolean visit(ParenthesizedExpression node) {
    buffer.append("(");
    node.getExpression().accept(this);
    buffer.append(")");
    return false;
  }

  @Override
  public boolean visit(PostfixExpression node) {
    node.getOperand().accept(this);
    buffer.append(node.getOperator().toString());
    return false;
  }

  @Override
  public boolean visit(PrefixExpression node) {
    buffer.append(node.getOperator().toString());
    node.getOperand().accept(this);
    return false;
  }

  @Override
  public boolean visit(PrimitiveType node) {
    buffer.append(NameTable.getPrimitiveObjCType(node.getTypeMirror()));
    return false;
  }

  @Override
  public boolean visit(QualifiedName node) {
    Element element = node.getElement();
    if (ElementUtil.isVariable(element)) {
      VariableElement var = (VariableElement) element;
      if (ElementUtil.isGlobalVar(var)) {
        buffer.append(nameTable.getVariableQualifiedName(var));
        return false;
      }
    }
    if (ElementUtil.isTypeElement(element)) {
      buffer.append(nameTable.getFullName((TypeElement) element));
      return false;
    }
    Name qualifier = node.getQualifier();
    qualifier.accept(this);
    buffer.append("->");
    node.getName().accept(this);
    return false;
  }

  @Override
  public boolean visit(QualifiedType node) {
    TypeElement type = TypeUtil.asTypeElement(node.getTypeMirror());
    if (type != null) {
      buffer.append(nameTable.getFullName(type));
      return false;
    }
    return true;
  }

  @Override
  public boolean visit(ReturnStatement node) {
    buffer.append("return");
    Expression expr = node.getExpression();
    if (expr != null) {
      buffer.append(' ');
      expr.accept(this);
    }
    buffer.append(";\n");
    return false;
  }

  @Override
  public boolean visit(SimpleName node) {
    Element element = node.getElement();
    if (element != null && ElementUtil.isVariable(element)) {
      buffer.append(nameTable.getVariableQualifiedName((VariableElement) element));
      return false;
    }
    if (element != null && ElementUtil.isTypeElement(element)) {
      buffer.append(nameTable.getFullName((TypeElement) element));
    } else {
      buffer.append(node.getIdentifier());
    }
    return false;
  }

  @Override
  public boolean visit(SimpleType node) {
    TypeElement type = TypeUtil.asTypeElement(node.getTypeMirror());
    if (type != null) {
      buffer.append(nameTable.getFullName(type));
      return false;
    }
    return true;
  }

  @Override
  public boolean visit(SingleMemberAnnotation node) {
    throw new AssertionError("Annotation nodes should not exist within method bodies.");
  }

  @Override
  public boolean visit(SingleVariableDeclaration node) {
    buffer.append(nameTable.getObjCType(node.getVariableElement()));
    if (node.isVarargs()) {
      buffer.append("...");
    }
    if (buffer.charAt(buffer.length() - 1) != '*') {
      buffer.append(" ");
    }
    buffer.append(nameTable.getVariableQualifiedName(node.getVariableElement()));
    for (int i = 0; i < node.getExtraDimensions(); i++) {
      buffer.append("[]");
    }
    if (node.getInitializer() != null) {
      buffer.append(" = ");
      node.getInitializer().accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(StringLiteral node) {
    buffer.append(LiteralGenerator.generateStringLiteral(node.getLiteralValue()));
    return false;
  }

  @Override
  public boolean visit(SuperConstructorInvocation node) {
    throw new AssertionError("SuperConstructorInvocation nodes are rewritten by Functionizer.");
  }

  @Override
  public boolean visit(SuperFieldAccess node) {
    buffer.append(nameTable.getVariableQualifiedName(node.getVariableElement()));
    return false;
  }

  @Override
  public boolean visit(SuperMethodInvocation node) {
    ExecutableElement element = node.getExecutableElement();
    assert node.getReceiver() == null
        : "Receivers expected to be handled by SuperMethodInvocationRewriter.";
    assert !ElementUtil.isStatic(element) : "Static invocations are rewritten by Functionizer.";
    buffer.append("[super");
    printMethodInvocationNameAndArgs(nameTable.getMethodSelector(element), node.getArguments());
    buffer.append(']');
    return false;
  }

  @Override
  public boolean visit(SuperMethodReference node) {
    throw new AssertionError("SuperMethodReference nodes are rewritten by LambdaRewriter.");
  }

  @Override
  public boolean visit(SwitchCase node) {
    if (node.isDefault()) {
      buffer.append("  default:\n");
    } else {
      buffer.append("  case ");
      node.getExpression().accept(this);
      buffer.append(":\n");
    }
    return false;
  }

  @Override
  public boolean visit(SwitchStatement node) {
    Expression expr = node.getExpression();
    buffer.append("switch (");
    expr.accept(this);
    buffer.append(") ");
    buffer.append("{\n");
    buffer.indent();
    for (Statement stmt : node.getStatements()) {
      buffer.printIndent();
      stmt.accept(this);
    }
    buffer.unindent();
    buffer.printIndent();
    buffer.append("}\n");
    return false;
  }

  @Override
  public boolean visit(SynchronizedStatement node) {
    buffer.append("@synchronized(");
    node.getExpression().accept(this);
    buffer.append(") ");
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(ThisExpression node) {
    buffer.append("self");
    return false;
  }

  @Override
  public boolean visit(ThrowStatement node) {
    buffer.append("@throw ");
    node.getExpression().accept(this);
    buffer.append(";\n");
    return false;
  }

  @Override
  public boolean visit(TryStatement node) {
    assert node.getResources().isEmpty() : "try-with-resources is handled by Rewriter.";

    buffer.append("@try ");
    node.getBody().accept(this);

    for (CatchClause cc : node.getCatchClauses()) {
      if (cc.getException().getType() instanceof UnionType) {
        printMultiCatch(cc);
      } else {
        buffer.append("@catch (");
        cc.getException().accept(this);
        buffer.append(") {\n");
        printStatements(cc.getBody().getStatements());
        buffer.append("}\n");
      }
    }

    if (node.getFinally() != null) {
      buffer.append(" @finally {\n");
      printStatements(node.getFinally().getStatements());
      buffer.append("}\n");
    }
    return false;
  }

  private void printMultiCatch(CatchClause node) {
    SingleVariableDeclaration exception = node.getException();
    for (Type exceptionType : ((UnionType) exception.getType()).getTypes()) {
      buffer.append("@catch (");
      exceptionType.accept(this);
      buffer.append(" *");
      buffer.append(nameTable.getVariableQualifiedName(exception.getVariableElement()));
      buffer.append(") {\n");
      printStatements(node.getBody().getStatements());
      buffer.append("}\n");
    }
  }

  @Override
  public boolean visit(TypeLiteral node) {
    TypeMirror type = node.getType().getTypeMirror();
    int arrayDimensions = 0;
    while (TypeUtil.isArray(type)) {
      arrayDimensions++;
      type = ((javax.lang.model.type.ArrayType) type).getComponentType();
    }
    if (arrayDimensions > 0) {
      if (type.getKind().isPrimitive()) {
        buffer.append("IOSClass_").append(TypeUtil.getName(type)).append("Array(");
      } else {
        buffer.append("IOSClass_arrayType(")
            .append(nameTable.getFullName(TypeUtil.asTypeElement(type)))
            .append("_class_(), ");
      }
      buffer.append(arrayDimensions).append(")");
    } else if (type.getKind().isPrimitive() || TypeUtil.isVoid(type)) {
      buffer.append(UnicodeUtils.format("[IOSClass %sClass]", TypeUtil.getName(type)));
    } else {
      buffer.append(nameTable.getFullName(TypeUtil.asTypeElement(type))).append("_class_()");
    }
    return false;
  }

  @Override
  public boolean visit(TypeMethodReference node) {
    throw new AssertionError("TypeMethodReference nodes are rewritten by LambdaRewriter.");
  }

  @Override
  public boolean visit(VariableDeclarationExpression node) {
    String typeString = nameTable.getObjCType(node.getTypeMirror());
    boolean needsAsterisk = typeString.endsWith("*");
    buffer.append(typeString);
    if (!needsAsterisk) {
      buffer.append(' ');
    }
    for (Iterator<VariableDeclarationFragment> it = node.getFragments().iterator();
         it.hasNext(); ) {
      VariableDeclarationFragment f = it.next();
      f.accept(this);
      if (it.hasNext()) {
        buffer.append(", ");
        if (needsAsterisk) {
          buffer.append('*');
        }
      }
    }
    return false;
  }

  @Override
  public boolean visit(VariableDeclarationFragment node) {
    buffer.append(nameTable.getVariableQualifiedName(node.getVariableElement()));
    Expression initializer = node.getInitializer();
    if (initializer != null) {
      buffer.append(" = ");
      initializer.accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(VariableDeclarationStatement node) {
    List<VariableDeclarationFragment> vars = node.getFragments();
    assert !vars.isEmpty();
    VariableElement element = vars.get(0).getVariableElement();
    if (ElementUtil.suppressesWarning("unused", element)
        || ElementUtil.getName(element).startsWith("unused")) {
      buffer.append("__unused ");
    }
    String objcType = nameTable.getObjCType(element);
    String objcTypePointers = " ";
    int idx = objcType.indexOf(" *");
    if (idx != -1) {
      // Split the type at the first pointer. The second part of the type is
      // applied to each fragment. (eg. Foo *one, *two)
      objcTypePointers = objcType.substring(idx);
      objcType = objcType.substring(0, idx);
    }
    buffer.append(objcType);
    for (Iterator<VariableDeclarationFragment> it = vars.iterator(); it.hasNext();) {
      VariableDeclarationFragment f = it.next();
      buffer.append(objcTypePointers);
      f.accept(this);
      if (it.hasNext()) {
        buffer.append(",");
      }
    }
    buffer.append(";\n");
    return false;
  }

  @Override
  public boolean visit(WhileStatement node) {
    buffer.append("while (");
    node.getExpression().accept(this);
    buffer.append(") ");
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(Initializer node) {
    // All Initializer nodes should have been converted during initialization
    // normalization.
    throw new AssertionError("initializer node not converted");
  }

  private void acceptMacroArgument(Expression expr) {
    if (needsParenthesesForMacro(expr)) {
      buffer.append('(');
      expr.accept(this);
      buffer.append(')');
    } else {
      expr.accept(this);
    }
  }

  private boolean needsParenthesesForMacro(Expression expr) {
    boolean[] hasComma = { false };
    expr.accept(new TreeVisitor() {
      @Override
      public boolean visit(ArrayInitializer node) {
        hasComma[0] = true;
        return false;
      }
      @Override
      public boolean visit(CommaExpression node) {
        return false;  // Adds parentheses around children.
      }
      @Override
      public boolean visit(FunctionInvocation node) {
        return false;  // Adds parentheses around children.
      }
      @Override
      public boolean visit(StringLiteral node) {
        if (!UnicodeUtils.hasValidCppCharacters(node.getLiteralValue())) {
          // LiteralGenerator will emit the string using [NSString stringWithCharacters:].
          hasComma[0] = true;
          return false;
        }
        return true;
      }
    });
    return hasComma[0];
  }
}
