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

package com.google.devtools.j2objc.translate;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedTypeBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.IOSVariableBinding;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Rewrites the Java AST to replace difficult to translate code with methods
 * that are more Objective C/iOS specific. For example, Objective C doesn't have
 * the concept of class variables, so they need to be replaced with static
 * accessor methods referencing private static data.
 *
 * @author Tom Ball
 */
public class Rewriter extends ErrorReportingASTVisitor {

  /**
   * The list of Objective-C type qualifier keywords.
   */
  private static final List<String> typeQualifierKeywords = Lists.newArrayList("in", "out",
      "inout", "oneway", "bycopy", "byref");

  @Override
  public boolean visit(TypeDeclaration node) {
    return visitType(node.getAST(), Types.getTypeBinding(node), ASTUtil.getBodyDeclarations(node),
                     node.getModifiers());
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    return visitType(node.getAST(), Types.getTypeBinding(node), ASTUtil.getBodyDeclarations(node),
                     node.getModifiers());
  }

  @Override
  public boolean visit(AnonymousClassDeclaration node) {
    return visitType(node.getAST(), Types.getTypeBinding(node), ASTUtil.getBodyDeclarations(node),
                     Modifier.NONE);
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    return visitType(node.getAST(), Types.getTypeBinding(node), ASTUtil.getBodyDeclarations(node),
                     node.getModifiers());
  }

  private boolean visitType(
      AST ast, ITypeBinding typeBinding, List<BodyDeclaration> members, int modifiers) {
    ITypeBinding[] interfaces = typeBinding.getInterfaces();
    if (interfaces.length > 0) {
      if (Modifier.isAbstract(modifiers) || typeBinding.isEnum()) {

        // Add any interface methods that aren't defined by this abstract type.
        // Obj-C needs these to verify that the generated class implements the
        // interface/protocol.
        for (ITypeBinding intrface : interfaces) {
          // Collect needed methods from this interface and all super-interfaces.
          Queue<ITypeBinding> interfaceQueue = new LinkedList<ITypeBinding>();
          Set<IMethodBinding> interfaceMethods = new LinkedHashSet<IMethodBinding>();
          interfaceQueue.add(intrface);
          while ((intrface = interfaceQueue.poll()) != null) {
            interfaceMethods.addAll(Arrays.asList(intrface.getDeclaredMethods()));
            interfaceQueue.addAll(Arrays.asList(intrface.getInterfaces()));
          }
          addMissingMethods(ast, typeBinding, interfaceMethods, members);
        }
      } else if (!typeBinding.isInterface()) {
        // Check for methods that the type *explicitly implements* for cases
        // where a superclass provides the implementation.  For example, many
        // Java interfaces define equals(Object) to provide documentation, which
        // a class doesn't need to implement in Java, but does in Obj-C.  These
        // classes need a forwarding method to pass the Obj-C compiler.
        Set<IMethodBinding> interfaceMethods = new LinkedHashSet<IMethodBinding>();
        for (ITypeBinding intrface : interfaces) {
          interfaceMethods.addAll(Arrays.asList(intrface.getDeclaredMethods()));
        }
        addForwardingMethods(ast, typeBinding, interfaceMethods, members);
      }
    }

    removeSerialization(members);

    renameDuplicateMembers(typeBinding);
    return true;
  }

  private void addMissingMethods(
      AST ast, ITypeBinding typeBinding, Set<IMethodBinding> interfaceMethods,
      List<BodyDeclaration> decls) {
    for (IMethodBinding interfaceMethod : interfaceMethods) {
      if (!isMethodImplemented(typeBinding, interfaceMethod, decls)) {
        addAbstractMethod(ast, typeBinding, interfaceMethod, decls);
      }
    }
  }

  private void addForwardingMethods(
      AST ast, ITypeBinding typeBinding, Set<IMethodBinding> interfaceMethods,
      List<BodyDeclaration> decls) {
    for (IMethodBinding interfaceMethod : interfaceMethods) {
      String methodName = interfaceMethod.getName();
      // These are the only java.lang.Object methods that are both overridable
      // and translated to Obj-C.
      if (methodName.matches("equals|hashCode|toString")) {
        if (!isMethodImplemented(typeBinding, interfaceMethod, decls)) {
          addForwardingMethod(ast, typeBinding, interfaceMethod, decls);
        }
      }
    }
  }

  private boolean isMethodImplemented(
      ITypeBinding type, IMethodBinding interfaceMethod, List<BodyDeclaration> decls) {
    for (BodyDeclaration decl : decls) {
      if (!(decl instanceof MethodDeclaration)) {
        continue;
      }

      if (Types.getMethodBinding(decl).isSubsignature(interfaceMethod)) {
        return true;
      }
    }
    return isMethodImplemented(type.getSuperclass(), interfaceMethod);
  }

  private boolean isMethodImplemented(ITypeBinding type, IMethodBinding method) {
    if (type == null || type.getQualifiedName().equals("java.lang.Object")) {
      return false;
    }

    for (IMethodBinding m : type.getDeclaredMethods()) {
      if (method.isSubsignature(m) ||
          (method.getName().equals(m.getName()) &&
          method.getReturnType().getErasure().isEqualTo(m.getReturnType().getErasure()) &&
          Arrays.equals(method.getParameterTypes(), m.getParameterTypes()))) {
        return true;
      }
    }

    return isMethodImplemented(type.getSuperclass(), method);
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    if (Types.hasAutoreleasePoolAnnotation(Types.getBinding(node))) {
      if (node.getBody() != null) {
        Types.addAutoreleasePool(node.getBody());
      }
    }

    // change the names of any methods that conflict with NSObject messages
    IMethodBinding binding = Types.getMethodBinding(node);
    String name = binding.getName();
    renameReservedNames(name, binding);

    handleCompareToMethod(node, binding);

    List<SingleVariableDeclaration> params = ASTUtil.getParameters(node);
    for (int i = 0; i < params.size(); i++) {
      // Change the names of any parameters that are type qualifier keywords.
      SingleVariableDeclaration param = params.get(i);
      name = param.getName().getIdentifier();
      if (typeQualifierKeywords.contains(name)) {
        IVariableBinding varBinding = Types.getVariableBinding(param);
        NameTable.rename(varBinding, name + "Arg");
      }
    }

    // Rename any labels that have the same names; legal in Java but not C.
    final Map<String, Integer> labelCounts = Maps.newHashMap();
    final AST ast = node.getAST();
    node.accept(new ASTVisitor() {
      @Override
      public void endVisit(LabeledStatement labeledStatement) {
        final String name = labeledStatement.getLabel().getIdentifier();
        int value = labelCounts.containsKey(name) ? labelCounts.get(name) + 1 : 1;
        labelCounts.put(name, value);
        if (value > 1) {
          final String newName = name + '_' + value;
          labeledStatement.setLabel(ASTFactory.newLabel(ast, newName));
          // Update references to this label.
          labeledStatement.accept(new ASTVisitor() {
            @Override
            public void endVisit(ContinueStatement node) {
              if (node.getLabel() != null && node.getLabel().getIdentifier().equals(name)) {
                node.setLabel(ASTFactory.newLabel(ast, newName));
              }
            }
            @Override
            public void endVisit(BreakStatement node) {
              if (node.getLabel() != null && node.getLabel().getIdentifier().equals(name)) {
                node.setLabel(ASTFactory.newLabel(ast, newName));
              }
            }
          });

        }
      }
    });
    return true;
  }

  /**
   * Adds an instanceof check to compareTo methods. This helps Comparable types
   * behave well in sorted collections which rely on Java's runtime type
   * checking.
   */
  private void handleCompareToMethod(MethodDeclaration node, IMethodBinding binding) {
    if (!binding.getName().equals("compareTo") || node.getBody() == null) {
      return;
    }
    ITypeBinding comparableType =
        BindingUtil.findInterface(binding.getDeclaringClass(), "java.lang.Comparable");
    if (comparableType == null) {
      return;
    }
    ITypeBinding[] typeArguments = comparableType.getTypeArguments();
    ITypeBinding[] parameterTypes = binding.getParameterTypes();
    if (typeArguments.length != 1 || parameterTypes.length != 1
        || !typeArguments[0].isEqualTo(parameterTypes[0])) {
      return;
    }

    AST ast = node.getAST();
    IVariableBinding param = Types.getVariableBinding(ASTUtil.getParameters(node).get(0));

    Expression nullCheck = ASTFactory.newInfixExpression(
        ast, ASTFactory.newSimpleName(ast, param), InfixExpression.Operator.NOT_EQUALS,
        ASTFactory.newNullLiteral(ast), ast.resolveWellKnownType("boolean"));
    Expression instanceofExpr = ASTFactory.newInstanceofExpression(
        ast, ASTFactory.newSimpleName(ast, param), typeArguments[0]);
    instanceofExpr = ASTFactory.newPrefixExpression(
        ast, PrefixExpression.Operator.NOT, instanceofExpr, "boolean");

    ITypeBinding cceType = GeneratedTypeBinding.newTypeBinding(
        "java.lang.ClassCastException", ast.resolveWellKnownType("java.lang.RuntimeException"),
        false);
    ClassInstanceCreation newCce = ast.newClassInstanceCreation();
    newCce.setType(ASTFactory.newType(ast, cceType));
    Types.addBinding(newCce, GeneratedMethodBinding.newConstructor(cceType, 0));

    ThrowStatement throwStmt = ast.newThrowStatement();
    throwStmt.setExpression(newCce);

    Block ifBlock = ast.newBlock();
    ASTUtil.getStatements(ifBlock).add(throwStmt);

    IfStatement ifStmt = ast.newIfStatement();
    ifStmt.setExpression(ASTFactory.newInfixExpression(
        ast, nullCheck, InfixExpression.Operator.CONDITIONAL_AND, instanceofExpr,
        ast.resolveWellKnownType("boolean")));
    ifStmt.setThenStatement(ifBlock);

    ASTUtil.getStatements(node.getBody()).add(0, ifStmt);
  }

  @Override
  public boolean visit(MethodInvocation node) {
    boolean visitChildren = true;
    if (rewriteStringFormat(node)) {
      visitChildren =  false;
    }
    IMethodBinding binding = Types.getMethodBinding(node);
    String name = binding.getName();
    renameReservedNames(name, binding);
    return visitChildren;
  }

  @Override
  public boolean visit(SuperMethodInvocation node) {
    renameReservedNames(node.getName().getIdentifier(), Types.getMethodBinding(node));
    return true;
  }

  private void renameReservedNames(String name, IMethodBinding binding) {
    if (NameTable.isReservedName(name)) {
      NameTable.rename(binding, name + "__");
    }
  }

  private static Statement getLoopBody(Statement s) {
    if (s instanceof DoStatement) {
      return ((DoStatement) s).getBody();
    } else if (s instanceof EnhancedForStatement) {
      return ((EnhancedForStatement) s).getBody();
    } else if (s instanceof ForStatement) {
      return ((ForStatement) s).getBody();
    } else if (s instanceof WhileStatement) {
      return ((WhileStatement) s).getBody();
    }
    return null;
  }

  @Override
  public boolean visit(LabeledStatement node) {
    Statement loopBody = getLoopBody(node.getBody());
    if (loopBody == null) {
      return true;
    }

    final AST ast = node.getAST();
    final String labelIdentifier = node.getLabel().getIdentifier();

    final boolean[] hasContinue = new boolean[1];
    final boolean[] hasBreak = new boolean[1];
    node.accept(new ASTVisitor() {
      @Override
      public void endVisit(ContinueStatement node) {
        if (node.getLabel() != null && node.getLabel().getIdentifier().equals(labelIdentifier)) {
          hasContinue[0] = true;
          node.setLabel(ASTFactory.newLabel(ast, "continue_" + labelIdentifier));
        }
      }
      @Override
      public void endVisit(BreakStatement node) {
        if (node.getLabel() != null && node.getLabel().getIdentifier().equals(labelIdentifier)) {
          hasBreak[0] = true;
          node.setLabel(ASTFactory.newLabel(ast, "break_" + labelIdentifier));
        }
      }
    });

    if (hasContinue[0]) {
      LabeledStatement newLabelStmt = ast.newLabeledStatement();
      newLabelStmt.setLabel(ASTFactory.newLabel(ast, "continue_" + labelIdentifier));
      newLabelStmt.setBody(ast.newEmptyStatement());
      // Put the loop body into an inner block so the continue label is outside
      // the scope of any variable initializations.
      Block newBlock = ast.newBlock();
      ASTUtil.setProperty(loopBody, newBlock);
      ASTUtil.getStatements(newBlock).add(loopBody);
      ASTUtil.getStatements(newBlock).add(newLabelStmt);
    }
    if (hasBreak[0]) {
      LabeledStatement newLabelStmt = ast.newLabeledStatement();
      newLabelStmt.setLabel(ASTFactory.newLabel(ast, "break_" + labelIdentifier));
      newLabelStmt.setBody(ast.newEmptyStatement());
      ASTUtil.insertAfter(node, newLabelStmt);
    }

    if (hasContinue[0] || hasBreak[0]) {
      // Replace this node with its statement, thus deleting the label.
      ASTUtil.setProperty(node, NodeCopier.copySubtree(ast, node.getBody()));
    }
    return true;
  }

  private Block makeBlock(Statement stmt) {
    if (stmt instanceof Block) {
      return (Block) stmt;
    }
    AST ast = stmt.getAST();
    Block block = ast.newBlock();
    ASTUtil.getStatements(block).add(stmt);
    return block;
  }

  @Override
  public void endVisit(ForStatement node) {
    // It should not be possible to have multiple VariableDeclarationExpression
    // nodes in the initializers.
    if (node.initializers().size() == 1) {
      Object initializer = node.initializers().get(0);
      if (initializer instanceof VariableDeclarationExpression) {
        List<VariableDeclarationFragment> fragments =
            ASTUtil.getFragments((VariableDeclarationExpression) initializer);
        for (VariableDeclarationFragment fragment : fragments) {
          if (Types.hasAutoreleasePoolAnnotation(Types.getBinding(fragment))) {
            Statement loopBody = node.getBody();
            if (!(loopBody instanceof Block)) {
              AST ast = node.getAST();
              Block block = ast.newBlock();
              ASTUtil.getStatements(block).add(NodeCopier.copySubtree(ast, loopBody));
              node.setBody(block);
            }
            Types.addAutoreleasePool((Block) node.getBody());
          }
        }
      }
    }
  }

  @Override
  public void endVisit(EnhancedForStatement node) {
    AST ast = node.getAST();
    Expression expression = node.getExpression();
    ITypeBinding expressionType = Types.getTypeBinding(expression);
    IVariableBinding loopVariable = Types.getVariableBinding(node.getParameter());
    Block loopBody = makeBlock(NodeCopier.copySubtree(ast, node.getBody()));

    if (Types.hasAutoreleasePoolAnnotation(loopVariable)) {
      Types.addAutoreleasePool(loopBody);
    }

    Block newBlock = expressionType.isArray() ?
        makeArrayIterationBlock(ast, expression, expressionType, loopVariable, loopBody) :
        makeIterableBlock(ast, expression, expressionType, loopVariable, loopBody);
    ASTUtil.setProperty(node, newBlock);
  }

  private Block makeArrayIterationBlock(
      AST ast, Expression expression, ITypeBinding expressionType, IVariableBinding loopVariable,
      Block loopBody) {
    IVariableBinding arrayVariable = new GeneratedVariableBinding(
        "a__", 0, expressionType, false, false, null, null);
    IVariableBinding sizeVariable = new GeneratedVariableBinding(
        "n__", 0, ast.resolveWellKnownType("int"), false, false, null, null);
    IVariableBinding indexVariable = new GeneratedVariableBinding(
        "i__", 0, ast.resolveWellKnownType("int"), false, false, null, null);

    IVariableBinding lengthVariable = new GeneratedVariableBinding(
        "length", 0, ast.resolveWellKnownType("int"), false, false, null, null);
    QualifiedName arrayLength = ast.newQualifiedName(
        ASTFactory.newSimpleName(ast, arrayVariable),
        ASTFactory.newSimpleName(ast, lengthVariable));
    Types.addBinding(arrayLength, lengthVariable);

    VariableDeclarationStatement arrayDecl = ASTFactory.newVariableDeclarationStatement(
        ast, arrayVariable, NodeCopier.copySubtree(ast, expression));
    VariableDeclarationStatement sizeDecl = ASTFactory.newVariableDeclarationStatement(
        ast, sizeVariable, arrayLength);

    VariableDeclarationExpression indexDecl = ASTFactory.newVariableDeclarationExpression(
        ast, indexVariable, ASTFactory.newNumberLiteral(ast, "0", "int"));
    InfixExpression loopCondition = ASTFactory.newInfixExpression(
        ast, indexVariable, InfixExpression.Operator.LESS, sizeVariable,
        ast.resolveWellKnownType("boolean"));
    PostfixExpression incrementExpr = ASTFactory.newPostfixExpression(
        ast, indexVariable, PostfixExpression.Operator.INCREMENT);

    VariableDeclarationStatement itemDecl = ASTFactory.newVariableDeclarationStatement(
        ast, loopVariable, ASTFactory.newArrayAccess(ast, arrayVariable, indexVariable));
    ASTUtil.getStatements(loopBody).add(0, itemDecl);

    ForStatement forLoop = ASTFactory.newForStatement(
        ast, indexDecl, loopCondition, incrementExpr, loopBody);

    Block block = ast.newBlock();
    List<Statement> stmts = ASTUtil.getStatements(block);
    stmts.add(arrayDecl);
    stmts.add(sizeDecl);
    stmts.add(forLoop);

    return block;
  }

  private Block makeIterableBlock(
      AST ast, Expression expression, ITypeBinding expressionType, IVariableBinding loopVariable,
      Block loopBody) {
    ITypeBinding iterableType = BindingUtil.findInterface(expressionType, "java.lang.Iterable");
    IMethodBinding iteratorMethod = BindingUtil.findDeclaredMethod(iterableType, "iterator");
    ITypeBinding iteratorType = iteratorMethod.getReturnType();
    IMethodBinding hasNextMethod = BindingUtil.findDeclaredMethod(iteratorType, "hasNext");
    IMethodBinding nextMethod = BindingUtil.findDeclaredMethod(iteratorType, "next");
    assert hasNextMethod != null && nextMethod != null;

    IVariableBinding iteratorVariable = new GeneratedVariableBinding(
        "iter__", 0, iteratorType, false, false, null, null);

    MethodInvocation iteratorInvocation = ASTFactory.newMethodInvocation(
        ast, iteratorMethod, NodeCopier.copySubtree(ast, expression));
    VariableDeclarationStatement iteratorDecl = ASTFactory.newVariableDeclarationStatement(
        ast, iteratorVariable, iteratorInvocation);
    MethodInvocation hasNextInvocation = ASTFactory.newMethodInvocation(
        ast, hasNextMethod, ASTFactory.newSimpleName(ast, iteratorVariable));
    MethodInvocation nextInvocation = ASTFactory.newMethodInvocation(
        ast, nextMethod, ASTFactory.newSimpleName(ast, iteratorVariable));

    ASTUtil.getStatements(loopBody).add(0, ASTFactory.newVariableDeclarationStatement(
        ast, loopVariable, nextInvocation));

    WhileStatement whileLoop = ast.newWhileStatement();
    whileLoop.setExpression(hasNextInvocation);
    whileLoop.setBody(loopBody);

    Block block = ast.newBlock();
    List<Statement> stmts = ASTUtil.getStatements(block);
    stmts.add(iteratorDecl);
    stmts.add(whileLoop);

    return block;
  }

  @Override
  public void endVisit(InfixExpression node) {
    InfixExpression.Operator op = node.getOperator();
    ITypeBinding type = Types.getTypeBinding(node);
    ITypeBinding lhsType = Types.getTypeBinding(node.getLeftOperand());
    ITypeBinding rhsType = Types.getTypeBinding(node.getRightOperand());
    if (Types.isJavaStringType(type) && op == InfixExpression.Operator.PLUS
        && !Types.isJavaStringType(lhsType) && !Types.isJavaStringType(rhsType)) {
      // String concatenation where the first two operands are not strings.
      // We move all the preceding non-string operands into a sub-expression.
      AST ast = node.getAST();
      ITypeBinding nonStringExprType = getAdditionType(ast, lhsType, rhsType);
      InfixExpression nonStringExpr = ast.newInfixExpression();
      InfixExpression stringExpr = ast.newInfixExpression();
      nonStringExpr.setOperator(InfixExpression.Operator.PLUS);
      stringExpr.setOperator(InfixExpression.Operator.PLUS);
      nonStringExpr.setLeftOperand(NodeCopier.copySubtree(ast, node.getLeftOperand()));
      nonStringExpr.setRightOperand(NodeCopier.copySubtree(ast, node.getRightOperand()));
      List<Expression> extendedOperands = ASTUtil.getExtendedOperands(node);
      List<Expression> nonStringOperands = ASTUtil.getExtendedOperands(nonStringExpr);
      List<Expression> stringOperands = ASTUtil.getExtendedOperands(stringExpr);
      boolean foundStringType = false;
      for (Expression expr : extendedOperands) {
        Expression copiedExpr = NodeCopier.copySubtree(ast, expr);
        ITypeBinding exprType = Types.getTypeBinding(expr);
        if (foundStringType || Types.isJavaStringType(exprType)) {
          if (foundStringType) {
            stringOperands.add(copiedExpr);
          } else {
            stringExpr.setRightOperand(copiedExpr);
          }
          foundStringType = true;
        } else {
          nonStringOperands.add(copiedExpr);
          nonStringExprType = getAdditionType(ast, nonStringExprType, exprType);
        }
      }
      Types.addBinding(nonStringExpr, nonStringExprType);
      stringExpr.setLeftOperand(nonStringExpr);
      Types.addBinding(stringExpr, ast.resolveWellKnownType("java.lang.String"));
      ASTUtil.setProperty(node, stringExpr);
    } else if (op == InfixExpression.Operator.CONDITIONAL_AND) {
      // Avoid logical-op-parentheses compiler warnings.
      if (node.getParent() instanceof InfixExpression) {
        InfixExpression parent = (InfixExpression) node.getParent();
        if (parent.getOperator() == InfixExpression.Operator.CONDITIONAL_OR) {
          AST ast = node.getAST();
          ParenthesizedExpression expr =
              ASTFactory.newParenthesizedExpression(ast, NodeCopier.copySubtree(ast, node));
          ASTUtil.setProperty(node, expr);
        }
      }
    } else if (op == InfixExpression.Operator.AND) {
      // Avoid bitwise-op-parentheses compiler warnings.
      if (node.getParent() instanceof InfixExpression &&
          ((InfixExpression) node.getParent()).getOperator() == InfixExpression.Operator.OR) {
        AST ast = node.getAST();
        ParenthesizedExpression expr =
            ASTFactory.newParenthesizedExpression(ast, NodeCopier.copySubtree(ast, node));
        ASTUtil.setProperty(node, expr);
      }
    }
  }

  private ITypeBinding getAdditionType(AST ast, ITypeBinding aType, ITypeBinding bType) {
    ITypeBinding doubleType = ast.resolveWellKnownType("double");
    ITypeBinding boxedDoubleType = ast.resolveWellKnownType("java.lang.Double");
    if (aType == doubleType || bType == doubleType
        || aType == boxedDoubleType || bType == boxedDoubleType) {
      return doubleType;
    }
    ITypeBinding floatType = ast.resolveWellKnownType("float");
    ITypeBinding boxedFloatType = ast.resolveWellKnownType("java.lang.Float");
    if (aType == floatType || bType == floatType
        || aType == boxedFloatType || bType == boxedFloatType) {
      return floatType;
    }
    ITypeBinding longType = ast.resolveWellKnownType("long");
    ITypeBinding boxedLongType = ast.resolveWellKnownType("java.lang.Long");
    if (aType == longType || bType == longType
        || aType == boxedLongType || bType == boxedLongType) {
      return longType;
    }
    return ast.resolveWellKnownType("int");
  }

  /**
   * Moves all variable declarations above the first case statement.
   */
  @Override
  public void endVisit(SwitchStatement node) {
    AST ast = node.getAST();
    List<Statement> statements = ASTUtil.getStatements(node);
    int insertIdx = 0;
    Block block = ast.newBlock();
    List<Statement> blockStmts = ASTUtil.getStatements(block);
    for (int i = 0; i < statements.size(); i++) {
      Statement stmt = statements.get(i);
      if (stmt instanceof VariableDeclarationStatement) {
        VariableDeclarationStatement declStmt = (VariableDeclarationStatement) stmt;
        statements.remove(i--);
        List<VariableDeclarationFragment> fragments = ASTUtil.getFragments(declStmt);
        for (VariableDeclarationFragment decl : fragments) {
          Expression initializer = decl.getInitializer();
          if (initializer != null) {
            Assignment assignment = ASTFactory.newAssignment(ast,
                NodeCopier.copySubtree(ast, decl.getName()),
                NodeCopier.copySubtree(ast, initializer));
            statements.add(++i, ast.newExpressionStatement(assignment));
            decl.setInitializer(null);
          }
        }
        blockStmts.add(insertIdx++, NodeCopier.copySubtree(ast, declStmt));
      }
    }
    if (blockStmts.size() > 0) {
      // There is at least one variable declaration, so copy this switch
      // statement into the new block and replace it in the parent list.
      blockStmts.add(NodeCopier.copySubtree(ast, node));
      ASTUtil.setProperty(node, block);
    }
  }

  /**
   * Rewrites String.format()'s format string to be iOS-compatible.
   *
   * @return true if the node was rewritten
   */
  private boolean rewriteStringFormat(MethodInvocation node) {
    IMethodBinding binding = Types.getMethodBinding(node);
    if (binding == null) {
      // No binding due to error already reported.
      return false;
    }
    ITypeBinding typeBinding = binding.getDeclaringClass();
    AST ast = node.getAST();
    if (typeBinding.equals(ast.resolveWellKnownType("java.lang.String"))
        && binding.getName().equals("format")) {

      List<Expression> args = ASTUtil.getArguments(node);
      if (args.isEmpty()) {
        return false;
      }
      Expression first = args.get(0);
      typeBinding = Types.getTypeBinding(first);
      if (typeBinding.getQualifiedName().equals("java.util.Locale")) {
        args.remove(0); // discard locale parameter
        first = args.get(0);
        typeBinding = Types.getTypeBinding(first);
      }
      if (first instanceof StringLiteral) {
        String format = ((StringLiteral) first).getLiteralValue();
        String convertedFormat = convertStringFormatString(format, args);
        if (!format.equals(convertedFormat)) {
          StringLiteral newLiteral = ast.newStringLiteral();
          newLiteral.setLiteralValue(convertedFormat);
          Types.addBinding(newLiteral, ast.resolveWellKnownType("java.lang.String"));
          args.set(0, newLiteral);
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Convert a Java string format string into a NSString equivalent.
   */
  @SuppressWarnings("fallthrough")
  private String convertStringFormatString(String s, List<Expression> args) {
    if (s.isEmpty()) {
      return s;
    }
    int iArg = 1;  // First argument after format string.
    char[] chars = s.toCharArray();
    StringBuffer result = new StringBuffer();
    boolean inSpecifier = false;
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      if (c == '%') {
        result.append('%');
        inSpecifier = true;
      } else if (inSpecifier) {
        switch (c) {
          case 's':
          case 'S':
            result.append('@');
            inSpecifier = false;
            iArg++;
            break;
          case 'c':
          case 'C':
            result.append('C');
            inSpecifier = false;
            iArg++;
            break;
          case 'h':
          case 'H':
            result.append('x');
            inSpecifier = false;
            iArg++;
            break;
          case 'd':
            if (Types.isLongType(Types.getTypeBinding(args.get(iArg)))) {
              result.append("ll");
            }
            result.append(c);
            inSpecifier = false;
            iArg++;
            break;
          case 'e':
          case 'f':
          case 'g':
          case 'o':
          case 'x':
            result.append(c);
            inSpecifier = false;
            iArg++;
            break;
          case '%':
            result.append(c);
            inSpecifier = false;
            break;
          default:
            result.append(c);
        }
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }

  /**
   * Add an abstract method to the given type that implements the given
   * interface method binding.
   */
  private void addAbstractMethod(
      AST ast, ITypeBinding typeBinding, IMethodBinding interfaceMethod,
      List<BodyDeclaration> decls) {
    MethodDeclaration method = createInterfaceMethodBody(ast, typeBinding, interfaceMethod);

    ASTUtil.getModifiers(method).add(ast.newModifier(ModifierKeyword.ABSTRACT_KEYWORD));

    decls.add(method);
  }

  /**
   * Java interfaces that redeclare java.lang.Object's equals, hashCode, or
   * toString methods need a forwarding method if the implementing class
   * relies on java.lang.Object's implementation.  This is because NSObject
   * is declared as adhering to the NSObject protocol, but doesn't explicitly
   * declare these method in its interface.  This prevents gcc from finding
   * an implementation, so it issues a warning.
   */
  private void addForwardingMethod(
      AST ast, ITypeBinding typeBinding, IMethodBinding interfaceMethod,
      List<BodyDeclaration> decls) {
    Logger.getAnonymousLogger().fine(String.format("adding %s to %s",
        interfaceMethod.getName(), typeBinding.getQualifiedName()));
    MethodDeclaration method = createInterfaceMethodBody(ast, typeBinding, interfaceMethod);

    // Add method body with single "super.method(parameters);" statement.
    Block body = ast.newBlock();
    method.setBody(body);
    SuperMethodInvocation superInvocation = ast.newSuperMethodInvocation();
    superInvocation.setName(NodeCopier.copySubtree(ast, method.getName()));

    for (SingleVariableDeclaration param : ASTUtil.getParameters(method)) {
      Expression arg = NodeCopier.copySubtree(ast, param.getName());
      ASTUtil.getArguments(superInvocation).add(arg);
    }
    Types.addBinding(superInvocation, Types.getMethodBinding(method));
    ReturnStatement returnStmt = ast.newReturnStatement();
    returnStmt.setExpression(superInvocation);
    ASTUtil.getStatements(body).add(returnStmt);

    decls.add(method);
  }

  private MethodDeclaration createInterfaceMethodBody(
      AST ast, ITypeBinding typeBinding, IMethodBinding interfaceMethod) {
    GeneratedMethodBinding methodBinding =
        GeneratedMethodBinding.newOverridingMethod(interfaceMethod, typeBinding);
    MethodDeclaration method = ASTFactory.newMethodDeclaration(ast, methodBinding);

    ITypeBinding[] parameterTypes = interfaceMethod.getParameterTypes();
    for (int i = 0; i < parameterTypes.length; i++) {
      ITypeBinding paramType = parameterTypes[i];
      IVariableBinding paramBinding = IOSVariableBinding.newParameter(
          "param" + i, i, paramType, methodBinding, paramType.getDeclaringClass(),
          Modifier.isFinal(paramType.getModifiers()));
      ASTUtil.getParameters(method).add(ASTFactory.newSingleVariableDeclaration(ast, paramBinding));
      methodBinding.addParameter(paramType);
    }
    return method;
  }

  /**
   * Remove private serialization methods and fields; since Java serialization
   * isn't supported, they only take up space.  The list of methods is taken
   * from the java.io.Serialization javadoc comments.
   */
  private void removeSerialization(List<BodyDeclaration> members) {
    for (Iterator<BodyDeclaration> iterator = members.iterator(); iterator.hasNext(); ) {
      BodyDeclaration member = iterator.next();
      int mods = member.getModifiers();
      if (member instanceof MethodDeclaration) {
        IMethodBinding binding = Types.getMethodBinding(member);
        String name = binding.getName();
        ITypeBinding[] parameterTypes = binding.getParameterTypes();
        ITypeBinding returnType = binding.getReturnType();
        if (name.equals("readObject")
            && Modifier.isPrivate(mods)
            && parameterTypes.length == 1
            && parameterTypes[0].getQualifiedName().equals("java.io.ObjectInputStream")
            && returnType.getBinaryName().equals("V")) {
          iterator.remove();
          continue;
        }
        if (name.equals("writeObject")
            && Modifier.isPrivate(mods)
            && parameterTypes.length == 1
            && parameterTypes[0].getQualifiedName().equals("java.io.ObjectOutputStream")
            && returnType.getBinaryName().equals("V")) {
          iterator.remove();
          continue;
        }
        if (name.equals("readObjectNoData")
            && Modifier.isPrivate(mods)
            && parameterTypes.length == 0
            && returnType.getBinaryName().equals("V")) {
          iterator.remove();
          continue;
        }
        if ((name.equals("readResolve") || name.equals("writeResolve"))
            && Modifier.isPrivate(mods)
            && parameterTypes.length == 0
            && returnType.getQualifiedName().equals("java.lang.Object")) {
          iterator.remove();
          continue;
        }
      } else if (member instanceof FieldDeclaration) {
        FieldDeclaration field = (FieldDeclaration) member;
        Type type = field.getType();
        VariableDeclarationFragment var = (VariableDeclarationFragment) field.fragments().get(0);
        if (var.getName().getIdentifier().equals("serialVersionUID")
            && type.isPrimitiveType()
            && ((PrimitiveType) type).getPrimitiveTypeCode() == PrimitiveType.LONG
            && Modifier.isPrivate(mods) && Modifier.isStatic(mods)) {
          iterator.remove();
          continue;
        }
      }
    }
  }

  /**
   * If a field and method have the same name, or if a field hides a visible
   * superclass field, rename the field.  This is necessary to avoid a name
   * clash when the fields are declared as properties.
   */
  private void renameDuplicateMembers(ITypeBinding typeBinding) {
    Map<String, IVariableBinding> fields = Maps.newHashMap();

    // Check all superclass(es) fields with declared fields.
    ITypeBinding superclass = typeBinding.getSuperclass();
    if (superclass != null) {
      addFields(superclass, true, true, fields);
      for (IVariableBinding var : typeBinding.getDeclaredFields()) {
        String name = var.getName();
        IVariableBinding field = fields.get(name);
        if (field != null) {
          name += '_' + typeBinding.getName();
          NameTable.rename(var, name);
          fields.put(name, var);
        }
      }
    }
  }

  private void addFields(ITypeBinding type, boolean includePrivate, boolean includeSuperclasses,
      Map<String, IVariableBinding> fields) {
    for (IVariableBinding field : type.getDeclaredFields()) {
      if (!fields.containsValue(field)) { // if not already renamed
        int mods = field.getModifiers();
        if (!Modifier.isStatic(mods)) {
          if (includePrivate) {
            fields.put(field.getName(), field);
          } else if (Modifier.isPublic(mods) || Modifier.isProtected(mods)) {
            fields.put(field.getName(), field);
          } else {
            IPackageBinding typePackage = type.getPackage();
            IPackageBinding fieldPackage = field.getDeclaringClass().getPackage();
            if (typePackage.isEqualTo(fieldPackage)) {
              fields.put(field.getName(), field);
            }
          }
        }
      }
    }
    ITypeBinding superclass = type.getSuperclass();
    if (includeSuperclasses && superclass != null) {
      addFields(superclass, false, true, fields);
    }
  }

  @Override
  public void endVisit(SingleVariableDeclaration node) {
    if (node.getExtraDimensions() > 0) {
      node.setType(ASTFactory.newType(node.getAST(), Types.getTypeBinding(node)));
      node.setExtraDimensions(0);
    }
  }

  @Override
  public void endVisit(VariableDeclarationStatement node) {
    AST ast = node.getAST();
    LinkedListMultimap<Integer, VariableDeclarationFragment> newDeclarations =
        rewriteExtraDimensions(ast, node.getType(), ASTUtil.getFragments(node));
    if (newDeclarations != null) {
      List<Statement> statements = ASTUtil.getStatements((Block) node.getParent());
      int location = 0;
      while (location < statements.size() && !node.equals(statements.get(location))) {
        location++;
      }
      for (Integer dimensions : newDeclarations.keySet()) {
        List<VariableDeclarationFragment> fragments = newDeclarations.get(dimensions);
        VariableDeclarationStatement newDecl =
            ASTFactory.newVariableDeclarationStatement(ast, fragments.get(0));
        ASTUtil.getFragments(newDecl).addAll(fragments.subList(1, fragments.size()));
        statements.add(++location, newDecl);
      }
    }
  }

  @Override
  public void endVisit(FieldDeclaration node) {
    AST ast = node.getAST();
    LinkedListMultimap<Integer, VariableDeclarationFragment> newDeclarations =
        rewriteExtraDimensions(ast, node.getType(), ASTUtil.getFragments(node));
    if (newDeclarations != null) {
      List<BodyDeclaration> bodyDecls = ASTUtil.getBodyDeclarations(node.getParent());
      int location = 0;
      while (location < bodyDecls.size() && !node.equals(bodyDecls.get(location))) {
        location++;
      }
      for (Integer dimensions : newDeclarations.keySet()) {
        List<VariableDeclarationFragment> fragments = newDeclarations.get(dimensions);
        FieldDeclaration newDecl = ASTFactory.newFieldDeclaration(ast, fragments.get(0));
        ASTUtil.getFragments(newDecl).addAll(fragments.subList(1, fragments.size()));
        bodyDecls.add(++location, newDecl);
      }
    }
  }

  private LinkedListMultimap<Integer, VariableDeclarationFragment> rewriteExtraDimensions(
      AST ast, Type typeNode, List<VariableDeclarationFragment> fragments) {
    // Removes extra dimensions on variable declaration fragments and creates extra field
    // declaration nodes if necessary.
    // eg. "int i1, i2[], i3[][];" becomes "int i1; int[] i2; int[][] i3".
    LinkedListMultimap<Integer, VariableDeclarationFragment> newDeclarations = null;
    int masterDimensions = -1;
    Iterator<VariableDeclarationFragment> iter = fragments.iterator();
    while (iter.hasNext()) {
      VariableDeclarationFragment frag = iter.next();
      int dimensions = frag.getExtraDimensions();
      ITypeBinding binding = Types.getTypeBinding(frag);
      if (masterDimensions == -1) {
        masterDimensions = dimensions;
        if (dimensions != 0) {
          ASTUtil.setProperty(typeNode, ASTFactory.newType(ast, binding));
        }
      } else if (dimensions != masterDimensions) {
        if (newDeclarations == null) {
          newDeclarations = LinkedListMultimap.create();
        }
        VariableDeclarationFragment newFrag = ASTFactory.newVariableDeclarationFragment(
            ast, Types.getVariableBinding(frag),
            NodeCopier.copySubtree(ast, frag.getInitializer()));
        newDeclarations.put(dimensions, newFrag);
        iter.remove();
      } else {
        frag.setExtraDimensions(0);
      }
    }
    return newDeclarations;
  }

  @Override
  public void endVisit(Assignment node) {
    AST ast = node.getAST();
    Assignment.Operator op = node.getOperator();
    Expression lhs = node.getLeftHandSide();
    Expression rhs = node.getRightHandSide();
    ITypeBinding lhsType = Types.getTypeBinding(lhs);
    if (op == Assignment.Operator.PLUS_ASSIGN && Types.isJavaStringType(lhsType)) {
      // Change "str1 += str2" to "str1 = str1 + str2".
      node.setOperator(Assignment.Operator.ASSIGN);
      node.setRightHandSide(ASTFactory.newInfixExpression(
          ast, NodeCopier.copySubtree(ast, lhs), InfixExpression.Operator.PLUS,
          NodeCopier.copySubtree(ast, rhs), lhsType));
    }
  }
}
