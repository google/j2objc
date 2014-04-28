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
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.j2objc.annotations.AutoreleasePool;
import com.google.j2objc.annotations.RetainedLocalRef;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
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
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
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

  private Map<IVariableBinding, IVariableBinding> localRefs = Maps.newHashMap();

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
    IMethodBinding binding = Types.getMethodBinding(node);

    if (BindingUtil.hasAnnotation(binding, AutoreleasePool.class)) {
      if (!binding.getReturnType().isPrimitive()) {
        ErrorUtil.warning(
            "Ignoring AutoreleasePool annotation on method with retainable return type");
      } else if (node.getBody() != null) {
        Types.addAutoreleasePool(node.getBody());
      }
    }

    // change the names of any methods that conflict with NSObject messages
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
    IMethodBinding binding = Types.getMethodBinding(node);
    String name = binding.getName();
    renameReservedNames(name, binding);
    return true;
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
  public void endVisit(LabeledStatement node) {
    Statement loopBody = getLoopBody(node.getBody());

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
      assert loopBody != null : "Continue statements must be inside a loop.";
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
          if (BindingUtil.hasAnnotation(Types.getBinding(fragment), AutoreleasePool.class)) {
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
  public void endVisit(InfixExpression node) {
    AST ast = node.getAST();
    InfixExpression.Operator op = node.getOperator();
    ITypeBinding type = Types.getTypeBinding(node);
    ITypeBinding lhsType = Types.getTypeBinding(node.getLeftOperand());
    ITypeBinding rhsType = Types.getTypeBinding(node.getRightOperand());
    if (Types.isJavaStringType(type) && op == InfixExpression.Operator.PLUS
        && !Types.isJavaStringType(lhsType) && !Types.isJavaStringType(rhsType)) {
      // String concatenation where the first two operands are not strings.
      // We move all the preceding non-string operands into a sub-expression.
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
          ParenthesizedExpression expr =
              ASTFactory.newParenthesizedExpression(ast, NodeCopier.copySubtree(ast, node));
          ASTUtil.setProperty(node, expr);
        }
      }
    } else if (op == InfixExpression.Operator.AND) {
      // Avoid bitwise-op-parentheses compiler warnings.
      if (node.getParent() instanceof InfixExpression &&
          ((InfixExpression) node.getParent()).getOperator() == InfixExpression.Operator.OR) {
        ParenthesizedExpression expr =
            ASTFactory.newParenthesizedExpression(ast, NodeCopier.copySubtree(ast, node));
        ASTUtil.setProperty(node, expr);
      }
    }

    // Avoid lower precedence compiler warnings.
    if (op == InfixExpression.Operator.AND || op == InfixExpression.Operator.OR) {
      if (node.getLeftOperand() instanceof InfixExpression) {
        Expression lhs = node.getLeftOperand();
        ParenthesizedExpression expr =
            ASTFactory.newParenthesizedExpression(ast, NodeCopier.copySubtree(ast, lhs));
        ASTUtil.setProperty(lhs, expr);
      }
      if (node.getRightOperand() instanceof InfixExpression) {
        Expression rhs = node.getRightOperand();
        ParenthesizedExpression expr =
            ASTFactory.newParenthesizedExpression(ast, NodeCopier.copySubtree(ast, rhs));
        ASTUtil.setProperty(rhs, expr);
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
   * Add an abstract method to the given type that implements the given
   * interface method binding.
   */
  private void addAbstractMethod(
      AST ast, ITypeBinding typeBinding, IMethodBinding interfaceMethod,
      List<BodyDeclaration> decls) {
    MethodDeclaration method = createInterfaceMethodBody(ast, typeBinding, interfaceMethod,
        interfaceMethod.getModifiers());

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
    MethodDeclaration method =
        createInterfaceMethodBody(ast, typeBinding, interfaceMethod, Modifier.PUBLIC);

    // Add method body with single "super.method(parameters);" statement.
    Block body = ast.newBlock();
    method.setBody(body);
    SuperMethodInvocation superInvocation =
        ASTFactory.newSuperMethodInvocation(ast, Types.getMethodBinding(method));

    for (SingleVariableDeclaration param : ASTUtil.getParameters(method)) {
      Expression arg = NodeCopier.copySubtree(ast, param.getName());
      ASTUtil.getArguments(superInvocation).add(arg);
    }
    ReturnStatement returnStmt = ast.newReturnStatement();
    returnStmt.setExpression(superInvocation);
    ASTUtil.getStatements(body).add(returnStmt);

    decls.add(method);
  }

  private MethodDeclaration createInterfaceMethodBody(
      AST ast, ITypeBinding typeBinding, IMethodBinding interfaceMethod, int modifiers) {
    GeneratedMethodBinding methodBinding =
        GeneratedMethodBinding.newOverridingMethod(interfaceMethod, typeBinding, modifiers);
    MethodDeclaration method = ASTFactory.newMethodDeclaration(ast, methodBinding);

    ITypeBinding[] parameterTypes = interfaceMethod.getParameterTypes();
    for (int i = 0; i < parameterTypes.length; i++) {
      ITypeBinding paramType = parameterTypes[i];
      IVariableBinding paramBinding = new GeneratedVariableBinding(
          "param" + i, 0, paramType, false, true, typeBinding, methodBinding);
      ASTUtil.getParameters(method).add(ASTFactory.newSingleVariableDeclaration(ast, paramBinding));
      methodBinding.addParameter(paramType);
    }
    return method;
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
    // Scan modifiers since variable declarations don't have variable bindings.
    if (ASTUtil.hasAnnotation(RetainedLocalRef.class, ASTUtil.getModifiers(node))) {
      ITypeBinding localRefType = Types.getLocalRefType();
      node.setType(ASTFactory.newType(ast, localRefType));
      Types.addBinding(node, localRefType);

      // Convert fragments to retained local refs.
      for (VariableDeclarationFragment fragment : ASTUtil.getFragments(node)) {
        IVariableBinding var = Types.getVariableBinding(fragment);
        GeneratedVariableBinding newVar = new GeneratedVariableBinding(
            var.getName(), var.getModifiers(), localRefType, false, false,
            var.getDeclaringClass(), var.getDeclaringMethod());
        localRefs.put(var, newVar);

        Expression initializer = fragment.getInitializer();
        if (localRefs.containsKey(Types.getBinding(initializer))) {
          initializer.accept(this);
        } else {
          // Create a constructor for a ScopedLocalRef for this fragment.
          IMethodBinding constructor = null;
          for (IMethodBinding m : localRefType.getDeclaredMethods()) {
            if (m.isConstructor()) {
              constructor = m;
              break;
            }
          }
          assert constructor != null : "failed finding ScopedLocalRef(var)";
          ClassInstanceCreation newInvocation = ASTFactory.newClassInstanceCreation(ast, constructor);
          ASTUtil.getArguments(newInvocation).add(NodeCopier.copySubtree(ast, initializer));
          fragment.setInitializer(newInvocation);
          Types.addBinding(fragment, newVar);
        }
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

  @Override
  public boolean visit(QualifiedName node) {
    // Check for ScopedLocalRefs.
    IBinding var = Types.getBinding(node);
    if (var instanceof IVariableBinding) {
      IVariableBinding localRef = localRefs.get(Types.getBinding(node.getQualifier()));
      if (localRef != null) {
        AST ast = node.getAST();
        SimpleName localRefField =
            ASTFactory.newSimpleName(ast, Types.getLocalRefType().getDeclaredFields()[0]);
        Expression newQualifier = NodeCopier.copySubtree(ast, node.getQualifier());
        Types.addBinding(newQualifier, localRef);
        FieldAccess localRefAccess = ASTFactory.newFieldAccess(
            ast, Types.getVariableBinding(localRefField), newQualifier);
        CastExpression newCast = ASTFactory.newCastExpression(
            ast, localRefAccess, Types.getTypeBinding(node.getQualifier()));
        ParenthesizedExpression newParens = ASTFactory.newParenthesizedExpression(ast, newCast);
        FieldAccess access = ASTFactory.newFieldAccess(ast, (IVariableBinding) var, newParens);
        ASTUtil.setProperty(node, access);
        return false;
      }
    }
    return true;
  }

  @Override
  public void endVisit(SimpleName node) {
    // Check for enum fields with reserved names.
    IVariableBinding var = Types.getVariableBinding(node);
    if (var != null) {
      var = var.getVariableDeclaration();
      ITypeBinding type = var.getDeclaringClass();
      if (type != null && !type.isArray()) {
        String fieldName = NameTable.getName(var);
        while ((type = type.getSuperclass()) != null) {
          for (IVariableBinding superField : type.getDeclaredFields()) {
            if (superField.getName().equals(fieldName)) {
              fieldName += '_' + NameTable.getName(var.getDeclaringClass());
              NameTable.rename(var, fieldName);
              return;
            }
          }
        }
      }
    }

    // Check for ScopedLocalRefs.
    IVariableBinding localRef = localRefs.get(Types.getBinding(node));
    if (localRef != null) {
      AST ast = node.getAST();
      FieldAccess access = ASTFactory.newFieldAccess(ast,
          Types.getLocalRefType().getDeclaredFields()[0], ASTFactory.newSimpleName(ast, localRef));
      CastExpression newCast = ASTFactory.newCastExpression(
          ast, access, Types.getTypeBinding(node));
      ParenthesizedExpression newParens = ASTFactory.newParenthesizedExpression(ast, newCast);
      ASTUtil.setProperty(node, newParens);
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
