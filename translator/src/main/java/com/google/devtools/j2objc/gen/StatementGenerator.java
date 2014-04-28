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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.translate.DestructorGenerator;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.IOSTypeBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTNodeException;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.UnicodeUtils;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Returns an Objective-C equivalent of a Java AST node.
 *
 * @author Tom Ball
 */
public class StatementGenerator extends ErrorReportingASTVisitor {
  private final SourceBuilder buffer;
  private final Set<IVariableBinding> fieldHiders;
  private final boolean asFunction;
  private final boolean useReferenceCounting;
  // The boolean value indicates whether the expression should be cast when the
  // resolved type of the expression is known to be "id".
  private final Map<Expression, Boolean> needsCastNodes = Maps.newHashMap();

  private static final String EXPONENTIAL_FLOATING_POINT_REGEX =
      "[+-]?\\d*\\.?\\d*[eE][+-]?\\d+";
  private static final String FLOATING_POINT_SUFFIX_REGEX = ".*[fFdD]";
  private static final String HEX_LITERAL_REGEX = "0[xX].*";
  private static final Pattern TRIGRAPH_REGEX = Pattern.compile("@\".*\\?\\?[=/'()!<>-].*\"");

  public static String generate(ASTNode node, Set<IVariableBinding> fieldHiders,
      boolean asFunction, SourcePosition sourcePosition) throws ASTNodeException {
    StatementGenerator generator = new StatementGenerator(node, fieldHiders, asFunction,
        sourcePosition);
    if (node == null) {
      throw new NullPointerException("cannot generate a null statement");
    }
    generator.run(node);
    return generator.getResult();
  }

  public static String generateArguments(IMethodBinding method, List<Expression> args,
      Set<IVariableBinding> fieldHiders, SourcePosition sourcePosition) {
    StatementGenerator generator = new StatementGenerator(null, fieldHiders, false,
        sourcePosition);
    if (IOSMethodBinding.hasVarArgsTarget(method)) {
      generator.printVarArgs(method, args);
    } else {
      int nArgs = args.size();
      for (int i = 0; i < nArgs; i++) {
        Expression arg = args.get(i);
        generator.printArgument(method, arg, i);
        if (i + 1 < nArgs) {
          generator.buffer.append(' ');
        }
      }
    }
    return generator.getResult();
  }

  private StatementGenerator(ASTNode node, Set<IVariableBinding> fieldHiders, boolean asFunction,
                             SourcePosition sourcePosition) {
    CompilationUnit unit = null;
    if (node != null && node.getRoot() instanceof CompilationUnit) {
      unit = (CompilationUnit) node.getRoot();
    }
    buffer = new SourceBuilder(unit, Options.emitLineDirectives(), sourcePosition);
    this.fieldHiders = fieldHiders;
    this.asFunction = asFunction;
    useReferenceCounting = !Options.useARC();
  }

  private String getResult() {
    return buffer.toString();
  }

  private void printArguments(IMethodBinding method, List<Expression> args) {
    if (IOSMethodBinding.hasVarArgsTarget(method)) {
      printVarArgs(method, args);
    } else if (!args.isEmpty()) {
      int nArgs = args.size();
      for (int i = 0; i < nArgs; i++) {
        Expression arg = args.get(i);
        printArgument(method, arg, i);
        if (i + 1 < nArgs) {
          buffer.append(' ');
        }
      }
    }
  }

  private void printArgument(IMethodBinding method, Expression arg, int index) {
    if (method != null) {
      IOSMethod iosMethod = IOSMethodBinding.getIOSMethod(method);
      if (iosMethod != null) {
        // mapped methods already have converted parameters
        if (index > 0) {
          buffer.append(iosMethod.getParameters().get(index).getParameterName());
        }
      } else {
        method = BindingUtil.getOriginalMethodBinding(method.getMethodDeclaration());
        ITypeBinding[] parameterTypes = method.getParameterTypes();
        assert index < parameterTypes.length : "method called with more parameters than declared";
        ITypeBinding parameter = parameterTypes[index];
        String keyword = NameTable.parameterKeyword(parameter);
        if (index == 0) {
          keyword = NameTable.capitalize(keyword);
        }
        buffer.append(keyword);
      }
    }
    buffer.append(':');
    arg.accept(this);
  }

  private void printVarArgs(IMethodBinding method, List<Expression> args) {
    method = method.getMethodDeclaration();
    ITypeBinding[] parameterTypes = method.getParameterTypes();
    Iterator<Expression> it = args.iterator();
    for (int i = 0; i < parameterTypes.length; i++) {
      if (i < parameterTypes.length - 1) {
        // Not the last parameter
        printArgument(method, it.next(), i);
        if (it.hasNext() || i + 1 < parameterTypes.length) {
          buffer.append(' ');
        }
      } else {
        if (i == 0) {
          buffer.append(':');
          if (it.hasNext()) {
            it.next().accept(this);
          }
        }
        // Method mapped to Obj-C varargs method call, so just append args.
        while (it.hasNext()) {
          buffer.append(", ");
          it.next().accept(this);
        }
        buffer.append(", nil");
      }
    }
  }

  @Override
  public boolean visit(AnonymousClassDeclaration node) {
    // Multi-method anonymous classes should have been converted by the
    // InnerClassExtractor.
    assert node.bodyDeclarations().size() == 1;

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
    ITypeBinding type = Types.getTypeBinding(node);
    assert type.isArray();
    ITypeBinding componentType = type.getComponentType();
    String componentTypeName = componentType.isPrimitive() ?
        NameTable.primitiveTypeToObjC(componentType.getName()) : "id";
    buffer.append(String.format("(%s[]){ ", componentTypeName));
    for (Iterator<Expression> it = ASTUtil.getExpressions(node).iterator(); it.hasNext(); ) {
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
    ITypeBinding binding = Types.mapType(Types.getTypeBinding(node));
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
      String assertStatementString =
          extractNodeCode(buffer.getSourcePosition().getSource(), node);
      assertStatementString = CharMatcher.WHITESPACE.trimFrom(assertStatementString);
      assertStatementString = makeQuotedString(assertStatementString);
      // Generates the following string:
      // filename.java:456 condition failed: foobar != fish.
      buffer.append("@\"" + buffer.getSourcePosition().getFilename() + ":"
                  + buffer.getLineNumber(node)
                  + " condition failed: " + assertStatementString + "\"");
    }
    buffer.append(");\n");
    return false;
  }

  @Override
  public boolean visit(Assignment node) {
    if (Types.hasDeferredFieldSetter(node)) {
      printDeferredFieldSetter(node);
      return false;
    }
    node.getLeftHandSide().accept(this);
    buffer.append(' ');
    buffer.append(getOperatorStr(node.getOperator()));
    buffer.append(' ');
    node.getRightHandSide().accept(this);
    return false;
  }

  private void printDeferredFieldSetter(Assignment node) {
    Expression lhs = node.getLeftHandSide();
    IVariableBinding var = Types.getVariableBinding(lhs);
    ITypeBinding declaringType = var.getDeclaringClass().getTypeDeclaration();
    String setterName = String.format("%s_set_%s", NameTable.getFullName(declaringType),
        NameTable.javaFieldToObjC(NameTable.getName(var)));
    buffer.append(setterName);
    buffer.append('(');
    if (lhs instanceof QualifiedName) {
      ((QualifiedName) lhs).getQualifier().accept(this);
    } else if (lhs instanceof FieldAccess) {
      ((FieldAccess) lhs).getExpression().accept(this);
    } else {
      buffer.append("self");
    }
    buffer.append(", ");
    node.getRightHandSide().accept(this);
    buffer.append(')');
  }

  private static String getOperatorStr(Assignment.Operator op) {
    if (op.equals(Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN)) {
      return ">>=";
    }
    return op.toString();
  }

  private void printUnsignedRightShift(Expression lhs, Expression rhs) {
    // (type) (((unsigned type) lhs) >> rhs);
    String type = getRightShiftType(lhs);
    buffer.append("(");
    buffer.append(type);
    buffer.append(") (((unsigned ");
    buffer.append(type);
    buffer.append(") ");
    lhs.accept(this);
    buffer.append(") >> ");
    rhs.accept(this);
    buffer.append(")");
  }

  private String getRightShiftType(Expression node) {
    ITypeBinding binding = Types.getTypeBinding(node);
    assert binding != null;
    AST ast = node.getAST();
    if (ast.resolveWellKnownType("int").equals(binding)) {
      return "int";
    } else if (ast.resolveWellKnownType("long").equals(binding)) {
      return "long long";
    } else if (ast.resolveWellKnownType("byte").equals(binding)) {
      return "char";
    } else if (ast.resolveWellKnownType("short").equals(binding)) {
      return "short";
    } else if (ast.resolveWellKnownType("char").equals(binding)) {
      return "unichar";
    } else {
      throw new AssertionError("invalid right shift expression type: " + binding.getName());
    }
  }

  @Override
  public boolean visit(Block node) {
    if (Types.hasAutoreleasePool(node)) {
      buffer.append("{\n@autoreleasepool ");
    }
    buffer.append("{\n");
    List<?> stmts = node.statements();
    // In case it's the body of a dealloc method, we generate the debug statement.
    // If we detect a -[super dealloc] method call, we are in a dealloc method.
    int size = stmts.size();
    if (size > 0) {
      if (stmts.get(size - 1) instanceof ExpressionStatement) {
        ASTNode subnode = ((ExpressionStatement) stmts.get(size - 1)).getExpression();
        if (subnode instanceof SuperMethodInvocation) {
          SuperMethodInvocation invocation = (SuperMethodInvocation) subnode;
          IMethodBinding binding = Types.getMethodBinding(invocation);
          String methodName = NameTable.getName(binding);
          if (Options.memoryDebug() &&
              ((methodName.equals(DestructorGenerator.FINALIZE_METHOD)) ||
               (methodName.equals(DestructorGenerator.DEALLOC_METHOD)))) {
            buffer.append("JreMemDebugRemove(self);\n");
          }
        }
      }
    }
    printStatements(stmts);
    buffer.append("}\n");
    if (Types.hasAutoreleasePool(node)) {
      buffer.append("}\n");
    }
    return false;
  }

  private void printStatements(List<?> statements) {
    for (Iterator<?> it = statements.iterator(); it.hasNext(); ) {
      Statement s = (Statement) it.next();
      buffer.syncLineNumbers(s);
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
  public boolean visit(CastExpression node) {
    ITypeBinding type = Types.getTypeBinding(node.getType());
    buffer.append("(");
    buffer.append(NameTable.getSpecificObjCType(type));
    buffer.append(") ");
    if (type.isInterface() && !type.isAnnotation()) {
      buffer.append("check_protocol_cast(");
      node.getExpression().accept(this);
      buffer.append(", @protocol(");
      buffer.append(NameTable.getFullName(type));
      buffer.append("))");
    } else if (type.isClass() || type.isAnnotation()) {
      buffer.append("check_class_cast(");
      node.getExpression().accept(this);
      buffer.append(", [");
      buffer.append(NameTable.getFullName(type));
      buffer.append(" class])");
    } else {
      // Cast type check not needed for primitive, enum and array types.
      node.getExpression().accept(this);
    }
    return false;
  }

  private void printMultiCatch(CatchClause node, boolean hasResources) {
    SingleVariableDeclaration exception = node.getException();
    for (Type exceptionType : ASTUtil.getTypes(((UnionType) exception.getType()))) {
      buffer.syncLineNumbers(node);
      buffer.append("@catch (");
      exceptionType.accept(this);
      buffer.append(' ');
      exception.getName().accept(this);
      buffer.append(") {\n");
      printMainExceptionStore(hasResources, node);
      buffer.syncLineNumbers(node);
      printStatements(ASTUtil.getStatements(node.getBody()));
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
    ITypeBinding type = Types.getTypeBinding(node.getType());
    boolean castPrinted = maybePrintCastFromId(node);
    buffer.append(useReferenceCounting ? "[[[" : "[[");
    ITypeBinding outerType = type.getDeclaringClass();
    buffer.append(NameTable.getFullName(type));
    buffer.append(" alloc] init");
    IMethodBinding method = Types.getMethodBinding(node);
    List<Expression> arguments = ASTUtil.getArguments(node);
    if (node.getExpression() != null && type.isMember() && arguments.size() > 0 &&
        !Types.getTypeBinding(arguments.get(0)).isEqualTo(outerType)) {
      // This is calling an untranslated "Outer.new Inner()" method,
      // so update its binding and arguments as if it had been translated.
      GeneratedMethodBinding newBinding = new GeneratedMethodBinding(method);
      newBinding.addParameter(0, outerType);
      method = newBinding;
      arguments = Lists.newArrayList(arguments);
      arguments.add(0, node.getExpression());
    }
    printArguments(method, arguments);
    buffer.append(']');
    if (useReferenceCounting) {
      buffer.append(" autorelease]");
    }
    if (castPrinted) {
      buffer.append(")");
    }
    return false;
  }

  @Override
  public boolean visit(ConditionalExpression node) {
    boolean castNeeded = false;
    ITypeBinding thenType = Types.getTypeBinding(node.getThenExpression());
    ITypeBinding elseType = Types.getTypeBinding(node.getElseExpression());

    if (!thenType.equals(elseType) &&
        !(node.getThenExpression() instanceof NullLiteral) &&
        !(node.getElseExpression() instanceof NullLiteral)) {
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
    IMethodBinding binding = Types.getMethodBinding(node);
    ITypeBinding declaringClass = binding.getDeclaringClass();
    List<Expression> args = ASTUtil.getArguments(node);
    buffer.append("[self init" + NameTable.getFullName(declaringClass));
    printArguments(binding, args);
    if (declaringClass.isEnum()) {
      buffer.append((args.isEmpty() ? "W" : " w") + "ithNSString:__name withInt:__ordinal");
    }
    buffer.append("]");
    return false;
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
  public boolean visit(ExpressionStatement node) {
    Expression expression = node.getExpression();
    IMethodBinding method = Types.getMethodBinding(expression);
    ITypeBinding type = Types.getTypeBinding(expression);
    if (method != null && !type.isPrimitive() && Options.useARC()) {
      // Avoid clang warning that the return value is unused.
      buffer.append("(void) ");
    }
    expression.accept(this);
    buffer.append(";\n");
    return false;
  }

  @Override
  public boolean visit(FieldAccess node) {
    Expression expr = node.getExpression();
    needsCastNodes.put(expr, true);
    // self->static_var is invalid Objective-C.
    if (!(expr instanceof ThisExpression && BindingUtil.isStatic(Types.getVariableBinding(node)))) {
      expr.accept(this);
      buffer.append("->");
    }
    node.getName().accept(this);
    return false;
  }

  @Override
  public boolean visit(ForStatement node) {
    buffer.append("for (");
    for (Iterator<Expression> it = ASTUtil.getInitializers(node).iterator(); it.hasNext(); ) {
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
    for (Iterator<Expression> it = ASTUtil.getUpdaters(node).iterator(); it.hasNext(); ) {
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
    Expression lhs = node.getLeftOperand();
    Expression rhs = node.getRightOperand();
    List<Expression> extendedOperands = ASTUtil.getExtendedOperands(node);
    ITypeBinding type = Types.getTypeBinding(node);
    ITypeBinding lhsType = Types.getTypeBinding(lhs);
    if (Types.isJavaStringType(type) &&
        op.equals(InfixExpression.Operator.PLUS)) {
      printStringConcatenation(lhs, rhs, extendedOperands);
    } else if ((op.equals(InfixExpression.Operator.EQUALS)
        || op.equals(InfixExpression.Operator.NOT_EQUALS))
        && (lhs instanceof StringLiteral || rhs instanceof StringLiteral)) {
      Expression first = lhs;
      Expression second = rhs;
      if (!(lhs instanceof StringLiteral)) {
        // In case the lhs can't call isEqual.
        first = rhs;
        second = lhs;
      }
      buffer.append(op.equals(InfixExpression.Operator.NOT_EQUALS) ? "![" : "[");
      first.accept(this);
      buffer.append(" isEqual:");
      second.accept(this);
      buffer.append("]");
    } else if (op.equals(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED) &&
        !lhsType.getName().equals("char")) {
      printUnsignedRightShift(lhs, rhs);
    } else if (op.equals(InfixExpression.Operator.LEFT_SHIFT) && lhsType.getName().equals("long")) {
      // The C compiler incorrectly shifts left when the shift is greater
      // than 32 with signed longs, so cast the lhs to unsigned, then
      // cast the result back to signed.
      buffer.append("(long long) (((uint64_t) ");
      lhs.accept(this);
      buffer.append(") << ");
      rhs.accept(this);
      buffer.append(')');
    } else {
      lhs.accept(this);
      buffer.append(' ');
      buffer.append(getOperatorStr(op));
      buffer.append(' ');
      rhs.accept(this);
      for (Iterator<Expression> it = extendedOperands.iterator(); it.hasNext(); ) {
        buffer.append(' ').append(op.toString()).append(' ');
        it.next().accept(this);
      }
    }
    return false;
  }

  private static String getOperatorStr(InfixExpression.Operator op) {
    if (op.equals(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED)) {
      return ">>";
    }
    return op.toString();
  }

  /**
   * Converts a string concatenation expression into a NSString format string and a
   * list of arguments for it, containing all the non-literal expressions.  If the
   * expression is all literals, then a string concatenation is printed.  If not,
   * then a NSString stringWithFormat: message is output.
   */
  @SuppressWarnings("fallthrough")
  private void printStringConcatenation(Expression leftOperand, Expression rightOperand,
      List<Expression> extendedOperands) {
    // Copy all operands into a single list.
    List<Expression> operands = Lists.newArrayList(leftOperand, rightOperand);
    operands.addAll(extendedOperands);
    StringBuilder format = new StringBuilder();

    List<Expression> args = Lists.newArrayList();
    for (Expression operand : operands) {
      IBinding binding = Types.getBinding(operand);
      if (binding instanceof IVariableBinding) {
        IVariableBinding var = (IVariableBinding) binding;
        var = var.getVariableDeclaration();
        Object value = var.getConstantValue();
        if (value instanceof String) {
          String s = (String) value;
          if (UnicodeUtils.hasValidCppCharacters(s)) {
            format.append(s.replace("%", "%%"));
          } else {
            ErrorUtil.error(operand,
                "String constant has Unicode or octal escape sequences that are not valid in " +
                "Objective-C.\nEither make string non-final, or remove characters.");
          }
          continue;
        } else if (value != null) {
          format.append(value.toString());
          continue;
        } // else fall through to next section.
      }
      if (operand instanceof StringLiteral) {
        String s = ((StringLiteral) operand).getLiteralValue();
        if (UnicodeUtils.hasValidCppCharacters(s)) {
          format.append(s.replace("%", "%%"));
        } else {
          // Convert to NSString invocation when printing args.
          format.append("%@");
          args.add(operand);
        }
      } else if (operand instanceof BooleanLiteral) {
        format.append(String.valueOf(((BooleanLiteral) operand).booleanValue()));
      } else if (operand instanceof CharacterLiteral) {
        format.append(((CharacterLiteral) operand).charValue());
      } else if (operand instanceof NumberLiteral) {
        format.append(((NumberLiteral) operand).getToken());
      } else {
        args.add(operand);

        // Append format specifier.
        ITypeBinding operandType = Types.getTypeBinding(operand);
        if (operandType.isPrimitive()) {
          String type = operandType.getBinaryName();
          assert type.length() == 1;
          switch (type.charAt(0)) {
            case 'B':  // byte
            case 'I':  // int
            case 'S':  // short
              format.append("%d");
              break;
            case 'J':  // long
              format.append("%lld");
              break;
            case 'D':  // double
            case 'F':  // float
              format.append("%f");
              break;
            case 'C':  // char
              format.append("%C");
              break;
            case 'Z':  // boolean
              format.append("%@");
              break;
            default:
              throw new AssertionError("unknown primitive type: " + type);
          }
        } else {
          format.append("%@");
        }
      }
    }

    String formatStr = UnicodeUtils.escapeStringLiteral(format.toString());
    if (args.isEmpty()) {
      buffer.append("@\"" + formatStr.replace("%%", "%") + "\""); // unescape % character
      return;
    }

    buffer.append("[NSString stringWithFormat:@\"");
    buffer.append(formatStr);
    buffer.append("\", ");
    for (Iterator<Expression> iter = args.iterator(); iter.hasNext(); ) {
      printStringConcatenationArg(iter.next());
      if (iter.hasNext()) {
        buffer.append(", ");
      }
    }
    buffer.append(']');
  }

  private void printStringConcatenationArg(Expression arg) {
    if (Types.getTypeBinding(arg).isEqualTo(arg.getAST().resolveWellKnownType("boolean"))) {
      buffer.append("[JavaLangBoolean toStringWithBoolean:");
      arg.accept(this);
      buffer.append(']');
      return;
    }
    if (arg instanceof StringLiteral) {
      // Strings with all valid C99 characters were previously converted,
      // so this literal needs to be defined with a char array.
      buffer.append(buildStringFromChars(((StringLiteral) arg).getLiteralValue()));
      return;
    }
    if (arg instanceof NullLiteral) {
      buffer.append("@\"null\"");
      return;
    }
    arg.accept(this);
  }

  // Some native objective-c methods are declared to return NSUInteger.
  private boolean returnValueNeedsIntCast(Expression arg) {
    if (arg instanceof MethodInvocation) {
      if (arg.getParent() instanceof ExpressionStatement) {
        // Avoid "unused return value" warning.
        return false;
      }
      MethodInvocation invocation = (MethodInvocation) arg;
      IMethodBinding methodBinding = Types.getMethodBinding(invocation);
      String methodName = methodBinding.getName();
      if (methodName.equals("hash") &&
          methodBinding.getReturnType().isEqualTo(arg.getAST().resolveWellKnownType("int"))) {
        return true;
      }
      if (invocation.getExpression() != null) {
        ITypeBinding callee = Types.mapType(Types.getTypeBinding(invocation.getExpression()));
        if (callee.getName().equals("NSString") && methodName.equals("length")) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean visit(InstanceofExpression node) {
    ITypeBinding leftBinding = Types.getTypeBinding(node.getLeftOperand());
    ITypeBinding rightBinding = Types.getTypeBinding(node.getRightOperand());

    buffer.append('[');
    if (leftBinding.isInterface()) {
      // Obj-C complains when a id<Protocol> is tested for a different
      // protocol, so cast it to a generic id.
      buffer.append("(id) ");
    }
    node.getLeftOperand().accept(this);
    if (rightBinding.isInterface()) {
      buffer.append(" conformsToProtocol: @protocol(");
      node.getRightOperand().accept(this);
      buffer.append(")");
    } else {
      buffer.append(" isKindOfClass:[");
      node.getRightOperand().accept(this);
      buffer.append(" class]");
    }
    buffer.append(']');
    return false;
  }

  @Override
  public boolean visit(LabeledStatement node) {
    node.getLabel().accept(this);
    buffer.append(": ");
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(MethodInvocation node) {
    IMethodBinding binding = Types.getMethodBinding(node);
    String methodName = NameTable.getName(binding);
    assert binding != null;

    // Object receiving the message, or null if it's a method in this class.
    Expression receiver = node.getExpression();
    ITypeBinding receiverType = receiver != null ? Types.getTypeBinding(receiver) :
        binding.getDeclaringClass();

    if (methodName.equals("isAssignableFrom") &&
        binding.getDeclaringClass().equals(Types.getIOSClass())) {
      printIsAssignableFromExpression(node);
    } else {
      IOSMethod iosMethod = IOSMethodBinding.getIOSMethod(binding);
      boolean castPrinted = maybePrintCast(node, getActualReturnType(binding, receiverType));
      if (iosMethod != null && iosMethod.isFunction()) {
        printFunctionInvocation(iosMethod, ASTUtil.getArguments(node));
      } else {
        if (returnValueNeedsIntCast(node)) {
          buffer.append("((int) ");
          castPrinted = true;
        }
        printMethodInvocation(binding, methodName, receiver, ASTUtil.getArguments(node));
      }
      if (castPrinted) {
        buffer.append(')');
      }
    }
    return false;
  }

  private void printFunctionInvocation(IOSMethod iosMethod, List<Expression> args) {
    if (iosMethod == IOSMethod.DEREFERENCE) {
      buffer.append("(*");
      args.get(0).accept(this);
      buffer.append(')');
      return;
    } else if (iosMethod == IOSMethod.ADDRESS_OF) {
      buffer.append('&');
      args.get(0).accept(this);
      return;
    }
    buffer.append(iosMethod.getName());
    buffer.append('(');
    boolean isFirst = true;
    for (Expression arg : args) {
      if (isFirst) {
        isFirst = false;
      } else {
        buffer.append(", ");
      }
      boolean hasNilCheck = Types.hasNilCheck(arg);
      if (hasNilCheck) {
        buffer.append("nil_chk(");
      }
      arg.accept(this);
      if (hasNilCheck) {
        buffer.append(')');
      }
    }
    buffer.append(')');
  }

  private void printMethodInvocation(
      IMethodBinding binding, String methodName, Expression receiver, List<Expression> args) {
    buffer.append('[');

    if (BindingUtil.isStatic(binding)) {
      buffer.append(NameTable.getFullName(binding.getDeclaringClass()));
    } else if (receiver != null) {
      needsCastNodes.put(receiver, true);
      boolean castPrinted = false;
      boolean hasNilCheck = Types.hasNilCheck(receiver);
      if (hasNilCheck) {
        castPrinted = maybePrintCastFromId(receiver);
        needsCastNodes.remove(receiver);
        buffer.append("nil_chk(");
      }
      receiver.accept(this);
      if (hasNilCheck) {
        buffer.append(')');
        if (castPrinted) {
          buffer.append(')');
        }
      }
    } else {
      buffer.append("self");
    }

    buffer.append(' ');
    if (binding instanceof IOSMethodBinding) {
      buffer.append(binding.getName());
    } else {
      buffer.append(methodName);
    }
    printArguments(binding, args);
    buffer.append(']');
  }

  private static ITypeBinding getActualReturnType(
      IMethodBinding method, ITypeBinding receiverType) {
    IMethodBinding actualDeclaration =
        getFirstDeclaration(getObjCMethodSignature(method), receiverType);
    if (actualDeclaration == null) {
      actualDeclaration = method.getMethodDeclaration();
    }
    ITypeBinding returnType = actualDeclaration.getReturnType();
    if (returnType.isTypeVariable()) {
      return Types.resolveIOSType("id");
    }
    return Types.mapType(returnType.getErasure());
  }

  /**
   * Finds the declaration for a given method and receiver in the same way that
   * the ObjC compiler will search for a declaration.
   */
  private static IMethodBinding getFirstDeclaration(String methodSig, ITypeBinding type) {
    if (type == null) {
      return null;
    }
    type = type.getTypeDeclaration();
    for (IMethodBinding declaredMethod : type.getDeclaredMethods()) {
      if (methodSig.equals(getObjCMethodSignature(declaredMethod))) {
        return declaredMethod;
      }
    }
    List<ITypeBinding> supertypes = Lists.newArrayList();
    supertypes.addAll(Arrays.asList(type.getInterfaces()));
    supertypes.add(type.isTypeVariable() ? 0 : supertypes.size(), type.getSuperclass());
    for (ITypeBinding supertype : supertypes) {
      IMethodBinding result = getFirstDeclaration(methodSig, supertype);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private static String getObjCMethodSignature(IMethodBinding method) {
    StringBuilder sb = new StringBuilder(method.getName());
    boolean first = true;
    for (ITypeBinding paramType : method.getParameterTypes()) {
      String keyword = NameTable.parameterKeyword(paramType);
      if (first) {
        first = false;
        keyword = NameTable.capitalize(keyword);
      }
      sb.append(keyword + ":");
    }
    return sb.toString();
  }

  /**
   * Class.isAssignableFrom() can test protocols as well as classes, so which
   * case needs to be detected and generated separately.
   */
  private void printIsAssignableFromExpression(MethodInvocation node) {
    assert !node.arguments().isEmpty();
    Expression firstExpression = node.getExpression();
    Expression secondExpression = (Expression) node.arguments().get(0);
    buffer.append('[');
    firstExpression.accept(this);
    buffer.append(" isAssignableFrom:");
    secondExpression.accept(this);
    buffer.append(']');
  }

  private boolean maybePrintCastFromId(Expression e) {
    return maybePrintCast(e, Types.resolveIOSType("id"));
  }

  private boolean maybePrintCast(Expression e, ITypeBinding actualType) {
    Boolean forceCastOfId = needsCastNodes.get(e);
    if (forceCastOfId == null) {
      return false;
    }
    ITypeBinding expectedType = Types.mapType(Types.getTypeBinding(e).getTypeDeclaration());
    if (actualType.isAssignmentCompatible(expectedType)) {
      return false;
    }
    if (actualType == Types.resolveIOSType("id") && !forceCastOfId) {
      return false;
    }
    ITypeBinding type = Types.getTypeBinding(e);
    if (type == null || type.isPrimitive() || Types.isVoidType(type)) {
      return false;
    }
    String typeName = NameTable.getSpecificObjCType(type);
    if (typeName.equals(NameTable.ID_TYPE)) {
      return false;
    }
    buffer.append("((" + typeName + ") ");
    return true;
  }

  @Override
  public boolean visit(NullLiteral node) {
    buffer.append("nil");
    return false;
  }

  @Override
  public boolean visit(NumberLiteral node) {
    String token = node.getToken();
    token = token.replace("_", "");  // Remove any embedded underscores.
    ITypeBinding binding = Types.getTypeBinding(node);
    assert binding.isPrimitive();
    char kind = binding.getKey().charAt(0);  // Primitive types have single-character keys.

    // Convert floating point literals to C format.  No checking is
    // necessary, since the format was verified by the parser.
    if (kind == 'D' || kind == 'F') {
      if (token.matches(FLOATING_POINT_SUFFIX_REGEX)) {
        token = token.substring(0, token.length() - 1);  // strip suffix
      }
      if (token.matches(HEX_LITERAL_REGEX)) {
        token = Double.toString(Double.parseDouble(token));
      } else if (!token.matches(EXPONENTIAL_FLOATING_POINT_REGEX)) {
        if (token.indexOf('.') == -1) {
          token += ".0";  // C requires a fractional part, except in exponential form.
        }
      }
      if (kind == 'F') {
        token += 'f';
      }
    } else if (kind == 'J') {
      if (token.equals("0x8000000000000000L") || token.equals("-9223372036854775808L")) {
        // Convert min long literal to an expression
        token = "-0x7fffffffffffffffLL - 1";
      } else {
        // Convert Java long literals to long long for Obj-C
        if (token.startsWith("0x")) {
          buffer.append("(long long) ");  // Ensure constant is treated as signed.
        }
        int pos = token.length() - 1;
        int numLs = 0;
        while (pos > 0 && token.charAt(pos) == 'L') {
          numLs++;
          pos--;
        }

        if (numLs == 1) {
          token += 'L';
        }
      }
    } else if (kind == 'I') {
      if (token.startsWith("0x")) {
        buffer.append("(int) ");  // Ensure constant is treated as signed.
      }
      if (token.equals("0x80000000") || token.equals("-2147483648")) {
        // Convert min int literal to an expression
        token = "-0x7fffffff - 1";
      }
    }
    buffer.append(token);
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
    buffer.append(NameTable.primitiveTypeToObjC(node));
    return false;
  }

  @Override
  public boolean visit(QualifiedName node) {
    IBinding binding = Types.getBinding(node);
    if (binding instanceof IVariableBinding) {
      IVariableBinding var = (IVariableBinding) binding;
      if (BindingUtil.isPrimitiveConstant(var)) {
        buffer.append(NameTable.getPrimitiveConstantName(var));
        return false;
      } else if (BindingUtil.isStatic(var)) {
        buffer.append(NameTable.getStaticVarQualifiedName(var));
        return false;
      }
    }
    if (binding instanceof ITypeBinding) {
      buffer.append(NameTable.getFullName((ITypeBinding) binding));
      return false;
    }
    Name qualifier = node.getQualifier();
    needsCastNodes.put(qualifier, true);
    qualifier.accept(this);
    buffer.append("->");
    node.getName().accept(this);
    return false;
  }

  @Override
  public boolean visit(QualifiedType node) {
    ITypeBinding binding = Types.getTypeBinding(node);
    if (binding != null) {
      buffer.append(NameTable.getFullName(binding));
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
      MethodDeclaration method = ASTUtil.getOwningMethod(node);
      boolean shouldRetainResult = false;

      // In manual reference counting mode, per convention, -copyWithZone: should return
      // an object with a reference count of +1.
      if (method.getName().getIdentifier().equals("copyWithZone") && useReferenceCounting) {
        shouldRetainResult = true;
      }
      if (shouldRetainResult) {
        buffer.append("[");
      }
      needsCastNodes.put(expr, false);
      expr.accept(this);
      if (shouldRetainResult) {
        buffer.append(" retain]");
      }
    } else if (Types.getMethodBinding(ASTUtil.getOwningMethod(node)).isConstructor()) {
      // A return statement without any expression is allowed in constructors.
      buffer.append(" self");
    }
    buffer.append(";\n");
    return false;
  }

  @Override
  public boolean visit(SimpleName node) {
    IBinding binding = Types.getBinding(node);
    if (binding instanceof IVariableBinding) {
      IVariableBinding var = (IVariableBinding) binding;
      if (BindingUtil.isPrimitiveConstant(var)) {
        buffer.append(NameTable.getPrimitiveConstantName(var));
      } else if (BindingUtil.isStatic(var)) {
        buffer.append(NameTable.getStaticVarQualifiedName(var));
      } else if (var.isField()) {
        boolean castPrinted = false;
        if (var.getVariableDeclaration().getType().isTypeVariable()) {
          castPrinted = maybePrintCastFromId(node);
        }
        buffer.append(NameTable.javaFieldToObjC(NameTable.getName(var)));
        if (castPrinted) {
          buffer.append(')');
        }
      } else {
        String name = NameTable.getName(var);
        buffer.append(name);
        if (!var.isField() && (fieldHiders.contains(var) || NameTable.isReservedName(name))) {
          buffer.append("Arg");
        }
      }
      return false;
    }
    if (binding instanceof ITypeBinding) {
      if (binding instanceof IOSTypeBinding) {
        buffer.append(binding.getName());
      } else {
        buffer.append(NameTable.getFullName((ITypeBinding) binding));
      }
    } else {
      buffer.append(node.getIdentifier());
    }
    return false;
  }

  @Override
  public boolean visit(SimpleType node) {
    ITypeBinding binding = Types.getTypeBinding(node);
    if (binding != null) {
      String name = NameTable.getFullName(binding);
      buffer.append(name);
      return false;
    }
    return true;
  }

  @Override
  public boolean visit(SingleVariableDeclaration node) {
    buffer.append(NameTable.getSpecificObjCType(Types.getVariableBinding(node)));
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
    String s = generateStringLiteral(node);
    if (TRIGRAPH_REGEX.matcher(s).matches()) {
      // Split string between the two '?' chars in the trigraph, so compiler
      // will concatenate the string without interpreting the trigraph.
      String[] substrings = s.split("\\?\\?");
      buffer.append(substrings[0]);
      for (int i = 1; i < substrings.length; i++) {
        buffer.append("?\" \"?");
        buffer.append(substrings[i]);
      }
    } else {
      buffer.append(s);
    }
    return false;
  }

  public static String generateStringLiteral(StringLiteral node) {
    if (UnicodeUtils.hasValidCppCharacters(node.getLiteralValue())) {
      return "@\"" + UnicodeUtils.escapeStringLiteral(node.getLiteralValue()) + "\"";
    } else {
      return buildStringFromChars(node.getLiteralValue());
    }
  }

  @VisibleForTesting
  static String buildStringFromChars(String s) {
    int length = s.length();
    StringBuilder buffer = new StringBuilder();
    buffer.append(
        "[NSString stringWithCharacters:(unichar[]) { ");
    int i = 0;
    while (i < length) {
      char c = s.charAt(i);
      buffer.append("(int) 0x");
      buffer.append(Integer.toHexString(c));
      if (++i < length) {
        buffer.append(", ");
      }
    }
    buffer.append(" } length:");
    String lengthString = Integer.toString(length);
    buffer.append(lengthString);
    buffer.append(']');
    return buffer.toString();
  }


  @Override
  public boolean visit(SuperConstructorInvocation node) {
    buffer.append("[super init");
    List<Expression> args = ASTUtil.getArguments(node);
    printArguments(Types.getMethodBinding(node), args);
    if (Types.getTypeBinding(node).isEnum()
        || Types.getTypeBinding(ASTUtil.getOwningType(node)).isEnum()) {
      buffer.append((args.isEmpty() ? "W" : " w") + "ithNSString:__name withInt:__ordinal");
    }
    buffer.append(']');
    return false;
  }

  @Override
  public boolean visit(SuperFieldAccess node) {
    buffer.append(NameTable.javaFieldToObjC(NameTable.getName(node.getName())));
    return false;
  }

  @Override
  public boolean visit(SuperMethodInvocation node) {
    IMethodBinding binding = Types.getMethodBinding(node);
    Name qualifier = node.getQualifier();
    if (qualifier != null) {
      String typeName = NameTable.getFullName(Types.getTypeBinding(qualifier).getSuperclass());
      String selectorName = NameTable.getMethodSelector(binding);
      ITypeBinding returnType = binding.getReturnType();
      boolean castPrinted = false;
      String closeImpCast = "";
      if (returnType.isPrimitive()) {
        // We must cast the IMP to have the correct return type.
        buffer.append(String.format("((%s (*)(id, SEL, ...))",
            NameTable.primitiveTypeToObjC(returnType.getName())));
        closeImpCast = ")";
      } else {
        castPrinted = maybePrintCastFromId(node);
      }
      buffer.append(String.format(
          "[%s instanceMethodForSelector:@selector(%s)]%s(", typeName, selectorName, closeImpCast));
      qualifier.accept(this);
      buffer.append(String.format(", @selector(%s)", selectorName));
      for (Expression arg : ASTUtil.getArguments(node)) {
        buffer.append(", ");
        arg.accept(this);
      }
      buffer.append(")");
      if (castPrinted) {
        buffer.append(")");
      }
    } else {
      boolean castPrinted = maybePrintCast(node, getActualReturnType(binding,
          Types.getTypeBinding(ASTUtil.getOwningType(node)).getSuperclass()));
      if (BindingUtil.isStatic(binding)) {
        buffer.append("[[super class] ");
      } else {
        buffer.append("[super ");
      }
      buffer.append(NameTable.getName(binding));
      printArguments(binding, ASTUtil.getArguments(node));
      buffer.append(']');
      if (castPrinted) {
        buffer.append(')');
      }
    }
    return false;
  }

  @Override
  public boolean visit(SwitchCase node) {
    if (node.isDefault()) {
      buffer.append("  default:\n");
    } else {
      buffer.append("  case ");
      Expression expr = node.getExpression();
      boolean isEnumConstant = Types.getTypeBinding(expr).isEnum();
      if (isEnumConstant) {
        String typeName = NameTable.getFullName(Types.getTypeBinding(expr));
        String bareTypeName = typeName.endsWith("Enum") ?
            typeName.substring(0, typeName.length() - 4) : typeName;
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
    ITypeBinding exprType = Types.getTypeBinding(expr);
    if (Types.isJavaStringType(exprType)) {
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
    List<Statement> stmts = ASTUtil.getStatements(node);
    for (Statement stmt : stmts) {
      buffer.syncLineNumbers(stmt);
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
    List<Statement> stmts = ASTUtil.getStatements(node);
    for (Statement stmt : stmts) {
      if (stmt instanceof SwitchCase) {
        SwitchCase caseStmt = (SwitchCase) stmt;
        if (!caseStmt.isDefault()) {
          assert (caseStmt.getExpression() instanceof StringLiteral);
          caseValues.add(((StringLiteral) caseStmt.getExpression()).getEscapedValue());
        }
      }
    }
    buffer.syncLineNumbers(node);
    buffer.append("NSArray *__caseValues = [NSArray arrayWithObjects:");
    for (String value : caseValues) {
      buffer.append("@" + value + ", ");
    }
    buffer.append("nil];\n");
    buffer.syncLineNumbers(node);
    buffer.append("NSUInteger __index = [__caseValues indexOfObject:");
    node.getExpression().accept(this);
    buffer.append("];\n");
    buffer.syncLineNumbers(node);
    buffer.append("switch (__index) {\n");
    for (Statement stmt : stmts) {
      buffer.syncLineNumbers(stmt);
      if (stmt instanceof SwitchCase) {
        SwitchCase caseStmt = (SwitchCase) stmt;
        if (caseStmt.isDefault()) {
          stmt.accept(this);
        } else {
          int i = caseValues.indexOf(((StringLiteral) caseStmt.getExpression()).getEscapedValue());
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

  @Override
  public boolean visit(SynchronizedStatement node) {
    buffer.append("@synchronized (");
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
    List<VariableDeclarationExpression> resources = ASTUtil.getResources(node);
    boolean hasResources = !resources.isEmpty();
    if (hasResources) {
      buffer.append("{\n");
      buffer.syncLineNumbers(node);
      buffer.append("JavaLangThrowable *__mainException = nil;\n");
    }
    for (VariableDeclarationExpression var : resources) {
      buffer.syncLineNumbers(var);
      var.accept(this);
      buffer.append(";\n");
    }
    buffer.append("@try ");
    node.getBody().accept(this);
    buffer.append(' ');
    for (Iterator<?> it = node.catchClauses().iterator(); it.hasNext(); ) {
      CatchClause cc = (CatchClause) it.next();
      if (cc.getException().getType().isUnionType()) {
        printMultiCatch(cc, hasResources);
      }
      buffer.syncLineNumbers(cc);
      buffer.append("@catch (");
      cc.getException().accept(this);
      buffer.append(") {\n");
      printMainExceptionStore(hasResources, cc);
      buffer.syncLineNumbers(cc);
      printStatements(ASTUtil.getStatements(cc.getBody()));
      buffer.append("}\n");
    }
    if (node.getFinally() != null || resources.size() > 0) {
      buffer.append(" @finally {\n");
      if (node.getFinally() != null) {
        printStatements(ASTUtil.getStatements(node.getFinally()));
      }
      for (VariableDeclarationExpression var : resources) {
        for (VariableDeclarationFragment frag : ASTUtil.getFragments(var)) {
          buffer.syncLineNumbers(var);
          buffer.append("@try {\n[");
          buffer.append(frag.getName().getFullyQualifiedName());
          buffer.append(" close];\n}\n");
          buffer.append("@catch (JavaLangThrowable *e) {\n");
          buffer.syncLineNumbers(var);
          buffer.append("if (__mainException) {\n");
          buffer.syncLineNumbers(var);
          buffer.append("[__mainException addSuppressedWithJavaLangThrowable:e];\n} else {\n");
          buffer.syncLineNumbers(var);
          buffer.append("__mainException = e;\n}\n");
          buffer.append("}\n");
        }
      }
      buffer.syncLineNumbers(node);
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
    ITypeBinding type = Types.getTypeBinding(node.getType());
    if (type.isPrimitive()) {
      buffer.append(String.format("[IOSClass %sClass]", type.getName()));
    } else if (type.isInterface()) {
      buffer.append("[IOSClass classWithProtocol:@protocol(");
      buffer.append(NameTable.getFullName(type));
      buffer.append(")]");
    } else {
      buffer.append("[IOSClass classWithClass:[");
      buffer.append(NameTable.getFullName(type));
      buffer.append(" class]]");
    }
    return false;
  }

  @Override
  public boolean visit(VariableDeclarationExpression node) {
    String typeString = NameTable.getSpecificObjCType(Types.getTypeBinding(node));
    boolean needsAsterisk = typeString.endsWith("*");
    buffer.append(typeString);
    if (!needsAsterisk) {
      buffer.append(' ');
    }
    for (Iterator<?> it = node.fragments().iterator(); it.hasNext(); ) {
      VariableDeclarationFragment f = (VariableDeclarationFragment) it.next();
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
      needsCastNodes.put(initializer, false);
      initializer.accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(VariableDeclarationStatement node) {
    List<VariableDeclarationFragment> vars = ASTUtil.getFragments(node);
    assert !vars.isEmpty();
    IVariableBinding binding = Types.getVariableBinding(vars.get(0));
    String objcType = NameTable.getSpecificObjCType(binding);
    String objcTypePointers = " ";
    int idx = objcType.indexOf(" *");
    if (idx != -1) {
      // Split the type at the first pointer. The second part of the type is
      // applied to each fragment. (eg. Foo *one, *two)
      objcTypePointers = objcType.substring(idx);
      objcType = objcType.substring(0, idx);
    }
    buffer.append(objcType);
    for (Iterator<VariableDeclarationFragment> it = vars.iterator(); it.hasNext(); ) {
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

  // Returns a string of the source at the location given by offset of the given length.
  private static String extractCode(String source, int offset, int length) {
    return source.substring(offset, offset + length);
  }

  // Returns a string of the code referenced by the given node.
  private static String extractNodeCode(String source, ASTNode node) {
    return extractCode(source, node.getStartPosition(), node.getLength());
  }

  // Returns a string where all characters that will interfer in
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
