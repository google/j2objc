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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.AnonymousClassDeclaration;
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
import com.google.devtools.j2objc.ast.Dimension;
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
import com.google.devtools.j2objc.ast.MemberValuePair;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.NameQualifiedType;
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
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TryStatement;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.ast.TypeMethodReference;
import com.google.devtools.j2objc.ast.UnionType;
import com.google.devtools.j2objc.ast.VariableDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationExpression;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.ast.WhileStatement;
import com.google.devtools.j2objc.types.IOSTypeBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.UnicodeUtils;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Returns an Objective-C equivalent of a Java AST node.
 *
 * @author Tom Ball
 */
public class StatementGenerator extends TreeVisitor {

  private final SourceBuilder buffer;
  private final boolean asFunction;
  private final boolean useReferenceCounting;

  public static String generate(TreeNode node, boolean asFunction, int currentLine) {
    StatementGenerator generator = new StatementGenerator(node, asFunction, currentLine);
    if (node == null) {
      throw new NullPointerException("cannot generate a null statement");
    }
    generator.run(node);
    return generator.getResult();
  }

  private StatementGenerator(TreeNode node, boolean asFunction, int currentLine) {
    buffer = new SourceBuilder(Options.emitLineDirectives(), currentLine);
    this.asFunction = asFunction;
    useReferenceCounting = !Options.useARC();
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

  private boolean assertIncompleteJava8Support(TreeNode node) {
    // A temporary stub to show pseudocode in place of Java 8 features.
    // TODO(kirbs): Implement correct conversion of Java 8 features to Objective-C.
    if (!Options.isJava8Translator()) {
      assert false : "not implemented yet";
    }
    buffer.append(node.toString());
    return false;
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
  public boolean visit(AnonymousClassDeclaration node) {
    // Multi-method anonymous classes should have been converted by the
    // InnerClassExtractor.
    assert node.getBodyDeclarations().size() == 1;

    // Generate an iOS block.
    assert false : "not implemented yet";

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
    ITypeBinding type = node.getTypeBinding();
    assert type.isArray();
    ITypeBinding componentType = type.getComponentType();
    buffer.append(String.format("(%s[]){ ", NameTable.getPrimitiveObjCType(componentType)));
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
    ITypeBinding binding = typeEnv.mapType(node.getTypeBinding());
    if (binding instanceof IOSTypeBinding) {
      buffer.append(binding.getName());
    } else {
      node.getComponentType().accept(this);
      buffer.append("[]");
    }
    return false;
  }

  @Override
  public boolean visit(AssertStatement node) {
    buffer.append(asFunction ? "NSCAssert(" : "NSAssert(");
    node.getExpression().accept(this);
    buffer.append(", ");
    if (node.getMessage() != null) {
      Expression expr = node.getMessage();
      boolean isString = expr instanceof StringLiteral;
      if (!isString) {
        buffer.append('[');
      }
      int start = buffer.length();
      expr.accept(this);
      int end = buffer.length();
      // Commas inside sub-expression of the NSAssert macro will be incorrectly interpreted as
      // new argument indicators in the macro. Replace commas with the J2OBJC_COMMA macro.
      String substring = buffer.substring(start, end);
      substring = substring.replaceAll(",", " J2OBJC_COMMA()");
      buffer.replace(start, end, substring);
      if (!isString) {
        buffer.append(" description]");
      }
    } else {
      int startPos = node.getStartPosition();
      String assertStatementString =
          unit.getSource().substring(startPos, startPos + node.getLength());
      assertStatementString = CharMatcher.WHITESPACE.trimFrom(assertStatementString);
      assertStatementString = makeQuotedString(assertStatementString);
      // Avoid format-invalid-specific warnings.
      assertStatementString = assertStatementString.replace("%", "%%");
      // Generates the following string:
      // filename.java:456 condition failed: foobar != fish.
      buffer.append("@\"" + TreeUtil.getSourceFileName(unit) + ":" + node.getLineNumber()
          + " condition failed: " + assertStatementString + "\"");
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
    buffer.append(node.booleanValue() ? "YES" : "NO");
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
    ITypeBinding type = node.getType().getTypeBinding();
    buffer.append("(");
    buffer.append(nameTable.getSpecificObjCType(type));
    buffer.append(") ");
    node.getExpression().accept(this);
    return false;
  }

  private void printMultiCatch(CatchClause node, boolean hasResources) {
    SingleVariableDeclaration exception = node.getException();
    for (Type exceptionType : ((UnionType) exception.getType()).getTypes()) {
      buffer.append("@catch (");
      exceptionType.accept(this);
      buffer.append(" *");
      exception.getName().accept(this);
      buffer.append(") {\n");
      printMainExceptionStore(hasResources, node);
      printStatements(node.getBody().getStatements());
      buffer.append("}\n");
    }
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
    boolean castNeeded = false;
    ITypeBinding thenType = node.getThenExpression().getTypeBinding();
    ITypeBinding elseType = node.getElseExpression().getTypeBinding();

    if (!thenType.equals(elseType)
        && !(node.getThenExpression() instanceof NullLiteral)
        && !(node.getElseExpression() instanceof NullLiteral)) {
      // gcc fails to compile a conditional expression where the two clauses of
      // the expression have different type. So cast any interface type down to
      // "id" to make the compiler happy. Concrete object types all have a
      // common ancestor of NSObject, so they don't need a cast.
      castNeeded = true;
    }

    node.getExpression().accept(this);

    buffer.append(" ? ");
    if (castNeeded && thenType.isInterface()) {
      buffer.append("((id) ");
    }
    node.getThenExpression().accept(this);
    if (castNeeded && thenType.isInterface()) {
      buffer.append(')');
    }

    buffer.append(" : ");
    if (castNeeded && elseType.isInterface()) {
      buffer.append("((id) ");
    }
    node.getElseExpression().accept(this);
    if (castNeeded && elseType.isInterface()) {
      buffer.append(')');
    }

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
    // TODO(kirbs): Implement correct conversion of Java 8 features to Objective-C.
    assert Options
        .isJava8Translator() : "CreationReference in translator with -source less than 8.";
    ITypeBinding functionalTypeBinding = node.getTypeBinding();
    ITypeBinding returnType = node.getType().getTypeBinding();
    printCreationReferenceCall(returnType, functionalTypeBinding, node.getMethodBinding());
    return false;
  }

  /**
   * Generates a creation reference using a block wrapper surrounding a new_Type_init call. This
   * block is used to create a new function class. Currently a seperate class will be created for
   * each unique FullFunctionName of the creation reference method bindings. We could reduce the
   * number of class types by using the capturing lambda construct and generating new class names
   * based on return type and selector.
   */
  public void printCreationReferenceCall(ITypeBinding returnType,
      ITypeBinding functionalTypeBinding, IMethodBinding methodBinding) {
    String functionalClassName = nameTable.getFullName(functionalTypeBinding);
    String newClassName = nameTable.getFullFunctionName(methodBinding);
    printLambdaCallWithoutBlocks(functionalTypeBinding.getFunctionalInterfaceMethod(),
        functionalClassName, newClassName, methodBinding, false);
    buffer.append('^');
    buffer.append(nameTable.getSpecificObjCType(returnType));
    // Required argument for imp_implementationWithBlock.
    buffer.append("(id _self");
    char var = 'a';
    for (ITypeBinding t : methodBinding.getParameterTypes()) {
      buffer.append(", ");
      buffer.append(nameTable.getSpecificObjCType(t));
      buffer.append(' ');
      buffer.append(var++);
    }
    buffer.append(") {\n return ");
    buffer.append(nameTable.getAllocatingConstructorName(methodBinding));
    buffer.append("(");
    var = 'a';
    boolean delimiterFlag = false;
    for (int i = 0; i < methodBinding.getParameterTypes().length; i++) {
      if (delimiterFlag) {
        buffer.append(", ");
      } else {
        delimiterFlag = true;
      }
      buffer.append(var++);
    }
    buffer.append(");\n}");
    buffer.append(")");
  }

  @Override
  public boolean visit(Dimension node) {
    // TODO(kirbs): Implement correct conversion of Java 8 features to Objective-C.
    return assertIncompleteJava8Support(node);
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
    // TODO(kirbs): Implement correct conversion of Java 8 features to Objective-C.
    return assertIncompleteJava8Support(node);
  }

  @Override
  public boolean visit(ExpressionStatement node) {
    Expression expression = node.getExpression();
    ITypeBinding type = expression.getTypeBinding();
    if (!type.isPrimitive() && Options.useARC()
        && (expression instanceof MethodInvocation
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
    buffer.append(node.getName());
    buffer.append('(');
    for (Iterator<Expression> iter = node.getArguments().iterator(); iter.hasNext(); ) {
      iter.next().accept(this);
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
    ITypeBinding rightBinding = node.getRightOperand().getTypeBinding();
    if (rightBinding.isInterface()) {
      // Our version of "isInstance" is faster than "conformsToProtocol".
      buffer.append(String.format("[%s_class_() isInstance:", nameTable.getFullName(rightBinding)));
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
    // TODO(kirbs): Implement correct conversion of Java 8 lambdas to Objective-C blocks.
    assert Options.isJava8Translator() :
      "Lambda expression in translator with -source less than 8.";
    IMethodBinding functionalInterface = node.getFunctionalInterfaceMethod();
    printLambdaCall(functionalInterface, node.functionalTypeBinding(), node.getMethodBinding(),
        node.getParameters(), node.isCapturing());
    node.getBody().accept(this);
    buffer.append(")");
    return false;
  }

  /**
   * Creates a block that is swizzled in as a class method in created capturing lambdas at runtime.
   * This outer block calls the underlying block for each lambda instance. Each captured lambda has
   * an outer block that matches the function signature of the functional interface, which calls an
   * underlying block specific to the instance.
   */
  public void printBlockCallWrapper(IMethodBinding binding) {
    buffer.append('^');
    buffer.append(nameTable.getSpecificObjCType(binding.getReturnType()));
    // Required argument for imp_implementationWithBlock.
    buffer.append("(id _self");
    char var = 'a';
    for (ITypeBinding t : binding.getParameterTypes()) {
      buffer.append(", ");
      buffer.append(nameTable.getSpecificObjCType(t));
      buffer.append(' ');
      buffer.append(var++);
    }
    buffer.append(") {\n id (^block)() = objc_getAssociatedObject(_self, (void *) 0);\n");
    if (!BindingUtil.isVoid(binding.getReturnType())) {
      buffer.append("return ");
    }
    buffer.append("block(_self");
    var = 'a';
    for (int i = 0; i < binding.getParameterTypes().length; i++) {
      buffer.append(", ");
      buffer.append(var++);
    }
    buffer.append(");\n},\n");
  }

  /**
   * Creates a lambda call by combining the call without blocks and the call blocks, so that the
   * calling portion sans blocks can be reused by method references.
   */
  public void printLambdaCall(IMethodBinding functionalInterface,
      ITypeBinding functionalTypeBinding, IMethodBinding methodBinding,
      List<VariableDeclaration> parameters, boolean isCapturing) {
    String functionalClassName = nameTable.getFullName(functionalTypeBinding);
    String createdClassName = nameTable.getFullLambdaName(methodBinding);
    printLambdaCallWithoutBlocks(functionalInterface, functionalClassName, createdClassName,
        methodBinding, isCapturing);
    printLambdaCallBlocks(functionalInterface, parameters, isCapturing);
  }

  /**
   * The lambda call sans wrapper and method blocks.
   */
  public void printLambdaCallWithoutBlocks(IMethodBinding functionalInterface,
      String functionalClassName, String newClassName, IMethodBinding methodBinding,
      boolean isCapturing) {
    if (isCapturing) {
      buffer.append("GetCapturingLambda(");
    } else {
      buffer.append("GetNonCapturingLambda(");
    }
    buffer.append('[');
    buffer.append(functionalClassName);
    buffer.append(" class], @protocol(");
    buffer.append(functionalClassName);
    buffer.append("), @\"");
    buffer.append(newClassName);
    buffer.append("\", @selector(");
    buffer.append(nameTable.getMethodSelector(functionalInterface));
    buffer.append("),\n");
  }

  /**
   * The lambda wrapper and method blocks.
   */
  public void printLambdaCallBlocks(IMethodBinding functionalInterface,
      List<VariableDeclaration> parameters, boolean isCapturing) {
    if (isCapturing) {
      printBlockCallWrapper(functionalInterface);
    }
    buffer.append('^');
    buffer.append(nameTable.getSpecificObjCType(functionalInterface.getReturnType()));
    // Required argument for imp_implementationWithBlock.
    buffer.append("(id _self");
    for (VariableDeclaration x : parameters) {
      IVariableBinding variableBinding = x.getVariableBinding();
      buffer.append(", ");
      buffer.append(nameTable.getSpecificObjCType(x.getVariableBinding().getType()));
      buffer.append(' ');
      buffer.append(nameTable.getVariableQualifiedName(variableBinding.getVariableDeclaration()));
    }
    buffer.append(")");
  }

  @Override
  public boolean visit(MarkerAnnotation node) {
    printAnnotationCreation(node);
    return false;
  }

  @Override
  public boolean visit(MethodInvocation node) {
    IMethodBinding binding = node.getMethodBinding();
    assert binding != null;

    // Object receiving the message, or null if it's a method in this class.
    Expression receiver = node.getExpression();
    buffer.append('[');
    if (BindingUtil.isStatic(binding)) {
      buffer.append(nameTable.getFullName(binding.getDeclaringClass()));
    } else if (receiver != null) {
      receiver.accept(this);
    } else {
      buffer.append("self");
    }
    printMethodInvocationNameAndArgs(nameTable.getMethodSelector(binding), node.getArguments());
    buffer.append(']');
    return false;
  }

  @Override
  public boolean visit(NameQualifiedType node) {
    // TODO(kirbs): Implement correct conversion of Java 8 features to Objective-C.
    return assertIncompleteJava8Support(node);
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
    printAnnotationCreation(node);
    return false;
  }

  private void printAnnotationCreation(Annotation node) {
    IAnnotationBinding annotation = node.getAnnotationBinding();
    buffer.append(useReferenceCounting ? "[[[" : "[[");
    buffer.append(nameTable.getFullName(annotation.getAnnotationType()));
    buffer.append(" alloc] init");

    if (node instanceof NormalAnnotation) {
      Map<String, Expression> args = Maps.newHashMap();
      for (MemberValuePair pair : ((NormalAnnotation) node).getValues()) {
        args.put(pair.getName().getIdentifier(), pair.getValue());
      }
      IMemberValuePairBinding[] members = BindingUtil.getSortedMemberValuePairs(annotation);
      for (int i = 0; i < members.length; i++) {
        if (i == 0) {
          buffer.append("With");
        } else {
          buffer.append(" with");
        }
        IMemberValuePairBinding member = members[i];
        String name = NameTable.getAnnotationPropertyName(member.getMethodBinding());
        buffer.append(NameTable.capitalize(name));
        buffer.append(':');
        Expression value = args.get(name);
        if (value != null) {
          value.accept(this);
        }
      }
    } else if (node instanceof SingleMemberAnnotation) {
      SingleMemberAnnotation sma = (SingleMemberAnnotation) node;
      buffer.append("With");
      IMethodBinding accessorBinding = annotation.getAllMemberValuePairs()[0].getMethodBinding();
      String name = NameTable.getAnnotationPropertyName(accessorBinding);
      buffer.append(NameTable.capitalize(name));
      buffer.append(':');
      sma.getValue();
    }

    buffer.append(']');
    if (useReferenceCounting) {
      buffer.append(" autorelease]");
    }
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
      buffer.append(LiteralGenerator.fixNumberToken(token, node.getTypeBinding()));
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
    buffer.append(NameTable.getPrimitiveObjCType(node.getTypeBinding()));
    return false;
  }

  @Override
  public boolean visit(QualifiedName node) {
    IBinding binding = node.getBinding();
    if (binding instanceof IVariableBinding) {
      IVariableBinding var = (IVariableBinding) binding;
      if (BindingUtil.isGlobalVar(var)) {
        buffer.append(nameTable.getVariableQualifiedName(var));
        return false;
      }
    }
    if (binding instanceof ITypeBinding) {
      buffer.append(nameTable.getFullName((ITypeBinding) binding));
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
    ITypeBinding binding = node.getTypeBinding();
    if (binding != null) {
      buffer.append(nameTable.getFullName(binding));
      return false;
    }
    return true;
  }

  @Override
  public boolean visit(ReturnStatement node) {
    buffer.append("return");
    Expression expr = node.getExpression();
    IMethodBinding methodBinding = TreeUtil.getOwningMethodBinding(node);
    if (expr != null) {
      buffer.append(' ');
      expr.accept(this);
    } else if (methodBinding != null && methodBinding.isConstructor()) {
      // A return statement without any expression is allowed in constructors.
      buffer.append(" self");
    }
    buffer.append(";\n");
    return false;
  }

  @Override
  public boolean visit(SimpleName node) {
    IBinding binding = node.getBinding();
    if (binding instanceof IVariableBinding) {
      buffer.append(nameTable.getVariableQualifiedName((IVariableBinding) binding));
      return false;
    }
    if (binding instanceof ITypeBinding) {
      if (binding instanceof IOSTypeBinding) {
        buffer.append(binding.getName());
      } else {
        buffer.append(nameTable.getFullName((ITypeBinding) binding));
      }
    } else {
      buffer.append(node.getIdentifier());
    }
    return false;
  }

  @Override
  public boolean visit(SimpleType node) {
    ITypeBinding binding = node.getTypeBinding();
    if (binding != null) {
      String name = nameTable.getFullName(binding);
      buffer.append(name);
      return false;
    }
    return true;
  }

  @Override
  public boolean visit(SingleMemberAnnotation node) {
    printAnnotationCreation(node);
    return false;
  }

  @Override
  public boolean visit(SingleVariableDeclaration node) {
    buffer.append(nameTable.getSpecificObjCType(node.getVariableBinding()));
    if (node.isVarargs()) {
      buffer.append("...");
    }
    if (buffer.charAt(buffer.length() - 1) != '*') {
      buffer.append(" ");
    }
    node.getName().accept(this);
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
    buffer.append(nameTable.getVariableQualifiedName(node.getVariableBinding()));
    return false;
  }

  @Override
  public boolean visit(SuperMethodInvocation node) {
    IMethodBinding binding = node.getMethodBinding();
    assert node.getQualifier() == null
        : "Qualifiers expected to be handled by SuperMethodInvocationRewriter.";
    assert !BindingUtil.isStatic(binding) : "Static invocations are rewritten by Functionizer.";
    buffer.append("[super");
    printMethodInvocationNameAndArgs(nameTable.getMethodSelector(binding), node.getArguments());
    buffer.append(']');
    return false;
  }

  @Override
  public boolean visit(SuperMethodReference node) {
    // TODO(kirbs): Implement correct conversion of Java 8 features to Objective-C.
    return assertIncompleteJava8Support(node);
  }

  @Override
  public boolean visit(SwitchCase node) {
    if (node.isDefault()) {
      buffer.append("  default:\n");
    } else {
      buffer.append("  case ");
      Expression expr = node.getExpression();
      boolean isEnumConstant = expr.getTypeBinding().isEnum();
      if (isEnumConstant) {
        String typeName = nameTable.getFullName(expr.getTypeBinding());
        String bareTypeName = typeName.endsWith("Enum")
            ? typeName.substring(0, typeName.length() - 4) : typeName;
        buffer.append(bareTypeName).append("_");
      }
      if (isEnumConstant && expr instanceof SimpleName) {
        buffer.append(((SimpleName) expr).getIdentifier());
      } else if (isEnumConstant && expr instanceof QualifiedName) {
        buffer.append(((QualifiedName) expr).getName().getIdentifier());
      } else {
        expr.accept(this);
      }
      buffer.append(":\n");
    }
    return false;
  }

  @Override
  public boolean visit(SwitchStatement node) {
    Expression expr = node.getExpression();
    ITypeBinding exprType = expr.getTypeBinding();
    if (typeEnv.isJavaStringType(exprType)) {
      printStringSwitchStatement(node);
      return false;
    }
    buffer.append("switch (");
    if (exprType.isEnum()) {
      buffer.append('[');
    }
    expr.accept(this);
    if (exprType.isEnum()) {
      buffer.append(" ordinal]");
    }
    buffer.append(") ");
    buffer.append("{\n");
    List<Statement> stmts = node.getStatements();
    for (Statement stmt : stmts) {
      stmt.accept(this);
    }
    if (!stmts.isEmpty() && stmts.get(stmts.size() - 1) instanceof SwitchCase) {
      // Last switch case doesn't have an associated statement, so add
      // an empty one.
      buffer.append(";\n");
    }
    buffer.append("}\n");
    return false;
  }

  private void printStringSwitchStatement(SwitchStatement node) {
    buffer.append("{\n");

    // Define an array of all the string constant case values.
    List<String> caseValues = Lists.newArrayList();
    List<Statement> stmts = node.getStatements();
    for (Statement stmt : stmts) {
      if (stmt instanceof SwitchCase) {
        SwitchCase caseStmt = (SwitchCase) stmt;
        if (!caseStmt.isDefault()) {
          caseValues.add(getStringConstant(caseStmt.getExpression()));
        }
      }
    }
    buffer.append("NSArray *__caseValues = [NSArray arrayWithObjects:");
    for (String value : caseValues) {
      buffer.append("@\"" + UnicodeUtils.escapeStringLiteral(value) + "\", ");
    }
    buffer.append("nil];\n");
    buffer.append("NSUInteger __index = [__caseValues indexOfObject:");
    node.getExpression().accept(this);
    buffer.append("];\n");
    buffer.append("switch (__index) {\n");
    for (Statement stmt : stmts) {
      if (stmt instanceof SwitchCase) {
        SwitchCase caseStmt = (SwitchCase) stmt;
        if (caseStmt.isDefault()) {
          stmt.accept(this);
        } else {
          int i = caseValues.indexOf(getStringConstant(caseStmt.getExpression()));
          assert i >= 0;
          buffer.append("case ");
          buffer.append(i);
          buffer.append(":\n");
        }
      } else {
        stmt.accept(this);
      }
    }
    buffer.append("}\n}\n");
  }

  private static String getStringConstant(Expression expr) {
    Object constantValue = expr.getConstantValue();
    if (constantValue == null) {
      constantValue = TreeUtil.getVariableBinding(expr).getConstantValue();
    }
    assert constantValue != null && constantValue instanceof String;
    return (String) constantValue;
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
    List<VariableDeclarationExpression> resources = node.getResources();
    boolean hasResources = !resources.isEmpty();
    if (hasResources) {
      buffer.append("{\n");
      buffer.append("JavaLangThrowable *__mainException = nil;\n");
    }
    for (VariableDeclarationExpression var : resources) {
      var.accept(this);
      buffer.append(";\n");
    }
    buffer.append("@try ");
    node.getBody().accept(this);
    buffer.append(' ');
    for (CatchClause cc : node.getCatchClauses()) {
      if (cc.getException().getType() instanceof UnionType) {
        printMultiCatch(cc, hasResources);
      }
      buffer.append("@catch (");
      cc.getException().accept(this);
      buffer.append(") {\n");
      printMainExceptionStore(hasResources, cc);
      printStatements(cc.getBody().getStatements());
      buffer.append("}\n");
    }
    if (node.getFinally() != null || resources.size() > 0) {
      buffer.append(" @finally {\n");
      if (node.getFinally() != null) {
        printStatements(node.getFinally().getStatements());
      }
      for (VariableDeclarationExpression var : resources) {
        for (VariableDeclarationFragment frag : var.getFragments()) {
          buffer.append("@try {\n[");
          buffer.append(frag.getName().getFullyQualifiedName());
          buffer.append(" close];\n}\n");
          buffer.append("@catch (JavaLangThrowable *e) {\n");
          buffer.append("if (__mainException) {\n");
          buffer.append("[__mainException addSuppressedWithJavaLangThrowable:e];\n} else {\n");
          buffer.append("__mainException = e;\n}\n");
          buffer.append("}\n");
        }
      }
      if (hasResources) {
        buffer.append("if (__mainException) {\n@throw __mainException;\n}\n");
      }
      buffer.append("}\n");
    }
    if (hasResources) {
      buffer.append("}\n");
    }
    return false;
  }

  private void printMainExceptionStore(boolean hasResources, CatchClause cc) {
    if (hasResources) {
      buffer.append("__mainException = ");
      buffer.append(cc.getException().getName().getFullyQualifiedName());
      buffer.append(";\n");
    }
  }

  @Override
  public boolean visit(TypeLiteral node) {
    ITypeBinding type = node.getType().getTypeBinding();
    if (type.isPrimitive()) {
      buffer.append(String.format("[IOSClass %sClass]", type.getName()));
    } else {
      buffer.append(nameTable.getFullName(type));
      buffer.append("_class_()");
    }
    return false;
  }

  @Override
  public boolean visit(TypeMethodReference node) {
    // TODO(kirbs): Implement correct conversion of Java 8 features to Objective-C.
    return assertIncompleteJava8Support(node);
  }

  @Override
  public boolean visit(VariableDeclarationExpression node) {
    String typeString = nameTable.getSpecificObjCType(node.getTypeBinding());
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
    node.getName().accept(this);
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
    IVariableBinding binding = vars.get(0).getVariableBinding();
    String objcType = nameTable.getSpecificObjCType(binding);
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

  // Returns a string where all characters that will interfere in
  // a valid Objective-C string are quoted.
  private static String makeQuotedString(String originalString) {
    int location = 0;
    StringBuffer buffer = new StringBuffer(originalString);
    while ((location = buffer.indexOf("\\", location)) != -1) {
      buffer.replace(location++, location++, "\\\\");
    }
    location = 0;
    while ((location = buffer.indexOf("\"", location)) != -1) {
      buffer.replace(location++, location++, "\\\"");
    }
    location = 0;
    while ((location = buffer.indexOf("\n")) != -1) {
      buffer.replace(location++, location++, "\\n");
    }
    return buffer.toString();
  }
}
