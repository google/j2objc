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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.devtools.j2objc.sym.Symbols;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedTypeBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.IOSArrayTypeBinding;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.IOSVariableBinding;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
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

  @SuppressWarnings("unchecked")
  @Override
  public boolean visit(TypeDeclaration node) {
    return visitType(
        node.getAST(), Types.getTypeBinding(node), node.bodyDeclarations(), node.getModifiers());
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean visit(EnumDeclaration node) {
    return visitType(
        node.getAST(), Types.getTypeBinding(node), node.bodyDeclarations(), node.getModifiers());
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean visit(AnonymousClassDeclaration node) {
    return visitType(
        node.getAST(), Types.getTypeBinding(node), node.bodyDeclarations(), Modifier.NONE);
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

    List<SingleVariableDeclaration> params = getParameters(node);
    for (int i = 0; i < params.size(); i++) {
      // Change the names of any parameters that are type qualifier keywords.
      SingleVariableDeclaration param = params.get(i);
      name = param.getName().getIdentifier();
      if (typeQualifierKeywords.contains(name)) {
        IVariableBinding varBinding = param.resolveBinding();
        NameTable.rename(varBinding, name + "Arg");
      }
    }
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
        Types.findInterface(binding.getDeclaringClass(), "java.lang.Comparable");
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
    IVariableBinding param = Types.getVariableBinding(getParameters(node).get(0));

    Expression nullCheck = ASTFactory.createNullCheck(ast, param, false);
    Expression instanceofExpr = ASTFactory.newInstanceofExpression(
        ast, ASTFactory.newSimpleName(ast, param), Types.makeType(typeArguments[0]));
    instanceofExpr = ASTFactory.newPrefixExpression(
        ast, PrefixExpression.Operator.NOT, instanceofExpr, "boolean");

    ITypeBinding cceType = GeneratedTypeBinding.newTypeBinding(
        "java.lang.ClassCastException", ast.resolveWellKnownType("java.lang.RuntimeException"),
        false);
    ClassInstanceCreation newCce = ast.newClassInstanceCreation();
    newCce.setType(Types.makeType(cceType));
    Types.addBinding(newCce, new GeneratedMethodBinding(
        "ClassCastException", 0, cceType, cceType, true, false, false));

    ThrowStatement throwStmt = ast.newThrowStatement();
    throwStmt.setExpression(newCce);

    Block ifBlock = ast.newBlock();
    getStatements(ifBlock).add(throwStmt);

    IfStatement ifStmt = ast.newIfStatement();
    ifStmt.setExpression(ASTFactory.newInfixExpression(
        ast, nullCheck, InfixExpression.Operator.CONDITIONAL_AND, instanceofExpr, "boolean"));
    ifStmt.setThenStatement(ifBlock);

    getStatements(node.getBody()).add(0, ifStmt);
  }

  @Override
  public boolean visit(MethodInvocation node) {
    boolean visitChildren = true;
    if (rewriteSystemOut(node)) {
      visitChildren =  false;
    }
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

  @Override
  public boolean visit(FieldDeclaration node) {
    int mods = node.getModifiers();
    ASTNode parent = node.getParent();
    if (parent instanceof TypeDeclaration && ((TypeDeclaration) parent).isInterface()) {
      // Interface fields are implicitly static and final.
      mods |= Modifier.STATIC | Modifier.FINAL;
    }
    if (Modifier.isStatic(mods)) {
      @SuppressWarnings("unchecked")
      List<BodyDeclaration> classMembers =
          parent instanceof AbstractTypeDeclaration ?
              ((AbstractTypeDeclaration) parent).bodyDeclarations() :
              ((AnonymousClassDeclaration) parent).bodyDeclarations();  // safe by specification
      int indexOfNewMember = classMembers.indexOf(node) + 1;

      @SuppressWarnings("unchecked")
      List<VariableDeclarationFragment> fragments = node.fragments(); // safe by specification
      for (VariableDeclarationFragment var : fragments) {
        IVariableBinding binding = Types.getVariableBinding(var);
        if (Types.isPrimitiveConstant(binding) && Modifier.isPrivate(binding.getModifiers())) {
          // Don't define accessors for private constants, since they can be
          // directly referenced.
          continue;
        }

        // rename varName to varName_, per Obj-C style guide
        SimpleName oldName = var.getName();
        ITypeBinding type = ((AbstractTypeDeclaration) node.getParent()).resolveBinding();
        String varName = NameTable.getStaticVarQualifiedName(type, oldName.getIdentifier());
        NameTable.rename(binding, varName);
        ITypeBinding typeBinding = binding.getType();
        var.setExtraDimensions(0);  // if array, type was corrected above

        // add accessor(s)
        if (needsReader(var, classMembers)) {
          classMembers.add(indexOfNewMember++, makeStaticReader(var, mods));
        }
        if (!Modifier.isFinal(mods) && needsWriter(var, classMembers)) {
          classMembers.add(
              indexOfNewMember++,
              makeStaticWriter(var, oldName.getIdentifier(), node.getType(), mods));
        }

        // move non-constant initialization to init block
        Expression initializer = var.getInitializer();
        if (initializer != null && initializer.resolveConstantExpressionValue() == null) {
          var.setInitializer(null);

          AST ast = var.getAST();
          SimpleName newName = ast.newSimpleName(varName);
          Types.addBinding(newName, binding);
          Assignment assign = ast.newAssignment();
          assign.setLeftHandSide(newName);
          Expression newInit = NodeCopier.copySubtree(ast, initializer);
          assign.setRightHandSide(newInit);
          Types.addBinding(assign, typeBinding);

          Block initBlock = ast.newBlock();
          getStatements(initBlock).add(ast.newExpressionStatement(assign));
          Initializer staticInitializer = ast.newInitializer();
          staticInitializer.setBody(initBlock);
          @SuppressWarnings("unchecked")
          List<IExtendedModifier> initMods = staticInitializer.modifiers(); // safe by definition
          initMods.add(ast.newModifier(ModifierKeyword.STATIC_KEYWORD));
          classMembers.add(indexOfNewMember++, staticInitializer);
        }
      }
    }
    return true;
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

  /**
   * Inserts a new statement after a given node. If node is a Block, appends
   * toInsert to the end of the block. If node's parent is a Block, inserts
   * directly after node. Otherwise, creates a new Block with node and toInsert
   * as the only two statements.
   */
  private static <E extends Statement> E insertStatement(E node, Statement toInsert) {
    if (node instanceof Block) {
      getStatements((Block) node).add(toInsert);
      return node;
    } else if (node.getParent() instanceof Block) {
      List<Statement> stmts = getStatements((Block) node.getParent());
      // Find node in statement list, and add given statement after it.
      for (int i = 0; i < stmts.size(); i++) {
        if (stmts.get(i) == node) {
          stmts.add(i + 1, toInsert);
          break;
        }
      }
    } else {
      AST ast = node.getAST();
      Block block = ast.newBlock();
      List<Statement> stmts = getStatements(block);
      E oldNode = node;
      node = NodeCopier.copySubtree(ast, node);
      stmts.add(node);
      stmts.add(toInsert);
      ClassConverter.setProperty(oldNode, block);
    }
    return node;
  }

  @Override
  public boolean visit(LabeledStatement node) {
    Statement loopBody = getLoopBody(node.getBody());
    if (loopBody == null) {
      return true;
    }

    AST ast = node.getAST();
    final String labelIdentifier = node.getLabel().getIdentifier();

    final boolean[] hasContinue = new boolean[1];
    final boolean[] hasBreak = new boolean[1];
    node.accept(new ASTVisitor() {
      @Override
      public void endVisit(ContinueStatement node) {
        if (node.getLabel() != null && node.getLabel().getIdentifier().equals(labelIdentifier)) {
          hasContinue[0] = true;
          node.setLabel(Types.newLabel("continue_" + labelIdentifier));
        }
      }
      @Override
      public void endVisit(BreakStatement node) {
        if (node.getLabel() != null && node.getLabel().getIdentifier().equals(labelIdentifier)) {
          hasBreak[0] = true;
          node.setLabel(Types.newLabel("break_" + labelIdentifier));
        }
      }
    });

    if (hasContinue[0]) {
      LabeledStatement newLabelStmt = ast.newLabeledStatement();
      newLabelStmt.setLabel(Types.newLabel("continue_" + labelIdentifier));
      newLabelStmt.setBody(ast.newEmptyStatement());
      loopBody = insertStatement(loopBody, newLabelStmt);
    }
    if (hasBreak[0]) {
      LabeledStatement newLabelStmt = ast.newLabeledStatement();
      newLabelStmt.setLabel(Types.newLabel("break_" + labelIdentifier));
      newLabelStmt.setBody(ast.newEmptyStatement());
      node = insertStatement(node, newLabelStmt);
    }

    if (hasContinue[0] || hasBreak[0]) {
      // Replace this node with its statement, thus deleting the label.
      ClassConverter.setProperty(node, NodeCopier.copySubtree(ast, node.getBody()));
    }
    return true;
  }

  private Block makeBlock(Statement stmt) {
    if (stmt instanceof Block) {
      return (Block) stmt;
    }
    AST ast = stmt.getAST();
    Block block = ast.newBlock();
    getStatements(block).add(stmt);
    return block;
  }

  @Override
  public void endVisit(ForStatement node) {
    // It should not be possible to have multiple VariableDeclarationExpression
    // nodes in the initializers.
    if (node.initializers().size() == 1) {
      Object initializer = node.initializers().get(0);
      if (initializer instanceof VariableDeclarationExpression) {
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fragments =
            ((VariableDeclarationExpression) initializer).fragments(); // safe by definition
        for (VariableDeclarationFragment fragment : fragments) {
          if (Types.hasAutoreleasePoolAnnotation(Types.getBinding(fragment))) {
            Statement loopBody = node.getBody();
            if (!(loopBody instanceof Block)) {
              AST ast = node.getAST();
              Block block = ast.newBlock();
              getStatements(block).add(NodeCopier.copySubtree(ast, loopBody));
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
    ClassConverter.setProperty(node, newBlock);
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
        ast, indexVariable, InfixExpression.Operator.LESS, sizeVariable, "boolean");
    PostfixExpression incrementExpr = ASTFactory.newPostfixExpression(
        ast, indexVariable, PostfixExpression.Operator.INCREMENT);

    VariableDeclarationStatement itemDecl = ASTFactory.newVariableDeclarationStatement(
        ast, loopVariable, ASTFactory.newArrayAccess(ast, arrayVariable, indexVariable));
    getStatements(loopBody).add(0, itemDecl);

    ForStatement forLoop = ASTFactory.newForStatement(
        ast, indexDecl, loopCondition, incrementExpr, loopBody);

    Block block = ast.newBlock();
    List<Statement> stmts = getStatements(block);
    stmts.add(arrayDecl);
    stmts.add(sizeDecl);
    stmts.add(forLoop);

    return block;
  }

  private Block makeIterableBlock(
      AST ast, Expression expression, ITypeBinding expressionType, IVariableBinding loopVariable,
      Block loopBody) {
    ITypeBinding iterableType = Types.findInterface(expressionType, "java.lang.Iterable");
    IMethodBinding iteratorMethod = Types.findDeclaredMethod(iterableType, "iterator");
    ITypeBinding iteratorType = iteratorMethod.getReturnType();
    IMethodBinding hasNextMethod = Types.findDeclaredMethod(iteratorType, "hasNext");
    IMethodBinding nextMethod = Types.findDeclaredMethod(iteratorType, "next");
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

    getStatements(loopBody).add(0, ASTFactory.newVariableDeclarationStatement(
        ast, loopVariable, nextInvocation));

    WhileStatement whileLoop = ast.newWhileStatement();
    whileLoop.setExpression(hasNextInvocation);
    whileLoop.setBody(loopBody);

    Block block = ast.newBlock();
    List<Statement> stmts = getStatements(block);
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
      List<Expression> extendedOperands = getExtendedOperands(node);
      List<Expression> nonStringOperands = getExtendedOperands(nonStringExpr);
      List<Expression> stringOperands = getExtendedOperands(stringExpr);
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
      ClassConverter.setProperty(node, stringExpr);
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
   * Helper method to isolate the unchecked warning.
   */
  @SuppressWarnings("unchecked")
  private static List<Statement> getStatements(Block block) {
    return block.statements();
  }

  @SuppressWarnings("unchecked")
  private static List<SingleVariableDeclaration> getParameters(MethodDeclaration method) {
    return method.parameters();
  }

  @SuppressWarnings("unchecked")
  private static List<Expression> getExtendedOperands(InfixExpression expr) {
    return expr.extendedOperands();
  }

  /**
   * Returns true if a reader method is needed for a specified field.  The
   * heuristic used is to find a method that has the same name, returns the
   * same type, and has no parameters.  Obviously, lousy code can fail this
   * test, but it should work in practice with existing Java code standards.
   */
  private boolean needsReader(VariableDeclarationFragment var, List<BodyDeclaration> classMembers) {
    String methodName = var.getName().getIdentifier();
    ITypeBinding varType = Types.getTypeBinding(var);
    for (BodyDeclaration member : classMembers) {
      if (member instanceof MethodDeclaration) {
        IMethodBinding method = Types.getMethodBinding(member);
        if (method.getName().equals(methodName) && method.getReturnType().isEqualTo(varType) &&
            method.getParameterTypes().length == 0) {
          return false;
        }
      }
    }
    return true;
  }


  /**
   * Returns true if a writer method is needed for a specified field.  The
   * heuristic used is to find a method that has "set" plus the capitalized
   * field name, returns null, and takes a single parameter of the same type.
   * Obviously, lousy code can fail this test, but it should work in practice
   * with Google code standards.
   */
  private boolean needsWriter(VariableDeclarationFragment var, List<BodyDeclaration> classMembers) {
    String methodName = "set" + NameTable.capitalize(var.getName().getIdentifier());
    ITypeBinding varType = Types.getTypeBinding(var);
    ITypeBinding voidType = var.getAST().resolveWellKnownType("void");
    for (BodyDeclaration member : classMembers) {
      if (member instanceof MethodDeclaration) {
        IMethodBinding method = Types.getMethodBinding(member);
        ITypeBinding[] params = method.getParameterTypes();
        if (method.getName().equals(methodName) && method.getReturnType().isEqualTo(voidType) &&
            params.length == 1 && params[0].isEqualTo(varType)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public void endVisit(ArrayInitializer node) {
    ASTNode nodeToReplace = node;
    ASTNode parent = node.getParent();
    if (parent instanceof ArrayCreation) {
      // replace this redundant array creation node with its rewritten initializer.
      nodeToReplace = parent;
    }
    ITypeBinding type = Types.getTypeBinding(node);
    ClassConverter.setProperty(nodeToReplace, createIOSArrayInitializer(type, node));
  }

  /**
   * Convert an array initializer into a init method on the equivalent
   * IOSArray. This init method takes a C array and count, like
   * NSArray.arrayWithObjects:count:. For example, "int[] a = { 1, 2, 3 };"
   * translates to "[IOSIntArray initWithInts:(int[]){ 1, 2, 3 } count:3];".
   */
  private MethodInvocation createIOSArrayInitializer(ITypeBinding arrayType,
      ArrayInitializer arrayInit) {
    AST ast = arrayInit.getAST();

    int dimensions = arrayType.getDimensions();
    ITypeBinding componentType;
    IOSArrayTypeBinding iosArrayBinding;
    if (dimensions > 2) {
      // This gets resolved into IOSObjectArray, for an array of arrays.
      componentType = iosArrayBinding = Types.resolveArrayType(arrayType);
    } else if (dimensions == 2) {
      // Creates a single-dimension array type.
      componentType = Types.resolveArrayType(arrayType.getElementType());
      iosArrayBinding = Types.resolveArrayType(componentType);
    } else {
      componentType = Types.getTypeBinding(arrayInit).getComponentType();
      iosArrayBinding = Types.resolveArrayType(componentType);
    }

    // Create IOS message.
    MethodInvocation message = ast.newMethodInvocation();
    SimpleName receiver = ast.newSimpleName(iosArrayBinding.getName());
    Types.addBinding(receiver, iosArrayBinding);
    message.setExpression(receiver);
    String methodName = iosArrayBinding.getInitMethod();
    SimpleName messageName = ast.newSimpleName(methodName);
    GeneratedMethodBinding methodBinding = new GeneratedMethodBinding(methodName,
        Modifier.PUBLIC | Modifier.STATIC, iosArrayBinding, iosArrayBinding, false, false, true);
    Types.addBinding(messageName, methodBinding);
    message.setName(messageName);
    Types.addBinding(message, methodBinding);

    // Pass array initializer as C-style array to message.
    @SuppressWarnings("unchecked")
    List<Expression> args = message.arguments(); // safe by definition
    ArrayInitializer newArrayInit = NodeCopier.copySubtree(ast, arrayInit);
    args.add(newArrayInit);
    GeneratedVariableBinding argBinding = new GeneratedVariableBinding(arrayType,
        false, true, null, methodBinding);
    methodBinding.addParameter(argBinding);
    NumberLiteral arraySize =
          ast.newNumberLiteral(Integer.toString(arrayInit.expressions().size()));
    Types.addBinding(arraySize, ast.resolveWellKnownType("int"));
    args.add(arraySize);
    argBinding = new GeneratedVariableBinding(ast.resolveWellKnownType("int"),
        false, true, null, methodBinding);
    methodBinding.addParameter(argBinding);

    // Specify type for object arrays.
    if (iosArrayBinding.getName().equals("IOSObjectArray")) {
      TypeLiteral typeLiteral = ast.newTypeLiteral();
      typeLiteral.setType(Types.makeType(componentType));
      Types.addBinding(typeLiteral, Types.getIOSClass());
      args.add(typeLiteral);
      argBinding = new GeneratedVariableBinding("type", 0, Types.getIOSClass(),
          false, true, null, methodBinding);
      methodBinding.addParameter(argBinding);
    }

    return message;
  }

  /**
   * Add a static read accessor method for a specified variable. The generator
   * phase will rename the variable from "name" to "name_", following the Obj-C
   * style guide.
   */
  private MethodDeclaration makeStaticReader(VariableDeclarationFragment var,
      int modifiers) {
    AST ast = var.getAST();
    String varName = var.getName().getIdentifier();
    IVariableBinding varBinding = var.resolveBinding();
    String methodName;
    methodName = NameTable.getStaticAccessorName(varName);

    Type returnType = Types.makeType(varBinding.getType());
    MethodDeclaration accessor = createBlankAccessor(var, methodName, modifiers, returnType);

    ReturnStatement returnStmt = ast.newReturnStatement();
    SimpleName returnName = ast.newSimpleName(var.getName().getIdentifier() + "_");
    Types.addBinding(returnName, varBinding);
    returnStmt.setExpression(returnName);

    getStatements(accessor.getBody()).add(returnStmt);

    GeneratedMethodBinding binding =
        new GeneratedMethodBinding(accessor, varBinding.getDeclaringClass(), false);
    Types.addBinding(accessor, binding);
    Types.addBinding(accessor.getName(), binding);
    Symbols.scanAST(accessor);
    return accessor;
  }

  /**
   * Add a static write accessor method for a specified variable.
   */
  private MethodDeclaration makeStaticWriter(VariableDeclarationFragment var,
      String paramName, Type type, int modifiers) {
    AST ast = var.getAST();
    String varName = var.getName().getIdentifier();
    IVariableBinding varBinding = Types.getVariableBinding(var);

    Type returnType = ast.newPrimitiveType(PrimitiveType.VOID);
    Types.addBinding(returnType, ast.resolveWellKnownType("void"));
    String methodName = "set" + NameTable.capitalize(varName);
    MethodDeclaration accessor = createBlankAccessor(var, methodName, modifiers, returnType);
    GeneratedMethodBinding binding =
        new GeneratedMethodBinding(accessor, varBinding.getDeclaringClass(), false);
    Types.addBinding(accessor, binding);
    Types.addBinding(accessor.getName(), binding);

    SingleVariableDeclaration param = ast.newSingleVariableDeclaration();
    param.setName(ast.newSimpleName(paramName));
    Type paramType = NodeCopier.copySubtree(ast, type);
    param.setType(paramType);
    Types.addBinding(paramType, type.resolveBinding());
    GeneratedVariableBinding paramBinding = new GeneratedVariableBinding(paramName, 0,
        type.resolveBinding(), false, true, varBinding.getDeclaringClass(), binding);
    Types.addBinding(param, paramBinding);
    Types.addBinding(param.getName(), paramBinding);
    getParameters(accessor).add(param);
    binding.addParameter(paramBinding);

    Assignment assign = ast.newAssignment();
    SimpleName sn = ast.newSimpleName(NameTable.getName(varBinding));
    assign.setLeftHandSide(sn);
    Types.addBinding(sn, varBinding);
    assign.setRightHandSide(NodeCopier.copySubtree(ast, param.getName()));
    Types.addBinding(assign, varBinding.getType());
    ExpressionStatement assignStmt = ast.newExpressionStatement(assign);

    getStatements(accessor.getBody()).add(assignStmt);
    Symbols.scanAST(accessor);
    return accessor;
  }

  /**
   * Create an unbound accessor method, minus its code.
   */
  @SuppressWarnings("unchecked") // safe by specification
  private MethodDeclaration createBlankAccessor(VariableDeclarationFragment var,
      String name, int modifiers, Type returnType) {
    AST ast = var.getAST();
    MethodDeclaration accessor = ast.newMethodDeclaration();
    accessor.setName(ast.newSimpleName(name));
    accessor.modifiers().addAll(ast.newModifiers(modifiers));
    accessor.setBody(ast.newBlock());
    accessor.setReturnType2(NodeCopier.copySubtree(ast, returnType));
    return accessor;
  }

  /**
   * Rewrites System.out and System.err println calls as NSLog calls.
   *
   * @return true if the node was rewritten
   */
  // TODO(user): remove when there is iOS console support.
  @SuppressWarnings("unchecked")
  private boolean rewriteSystemOut(MethodInvocation node) {
    Expression expression = node.getExpression();
    if (expression instanceof Name) {
      Name expr = (Name) node.getExpression();
      IBinding binding = expr.resolveBinding();
      if (binding instanceof IVariableBinding) {
        IVariableBinding varBinding = (IVariableBinding) binding;
        ITypeBinding type = varBinding.getDeclaringClass();
        if (type == null) {
          return false;
        }
        String clsName = type.getQualifiedName();
        String varName = varBinding.getName();
        if (clsName.equals("java.lang.System")
            && (varName.equals("out") || varName.equals("err"))) {
          // Change System.out.* or System.err.* to NSLog
          AST ast = node.getAST();
          MethodInvocation newInvocation = ast.newMethodInvocation();
          IMethodBinding methodBinding = new IOSMethodBinding("NSLog",
              Types.getMethodBinding(node), null);
          Types.addBinding(newInvocation, methodBinding);
          Types.addFunction(methodBinding);
          newInvocation.setName(ast.newSimpleName("NSLog"));
          Types.addBinding(newInvocation.getName(), methodBinding);
          newInvocation.setExpression(null);

          // Insert NSLog format argument
          List<Expression> args = node.arguments();
          if (args.size() == 1) {
            Expression arg = args.get(0);
            arg.accept(this);
            String format = getFormatArgument(arg);
            StringLiteral literal = ast.newStringLiteral();
            literal.setLiteralValue(format);
            Types.addBinding(literal, ast.resolveWellKnownType("java.lang.String"));
            newInvocation.arguments().add(literal);

            // JDT won't let nodes be re-parented, so copy and map.
            ASTNode newArg = NodeCopier.copySubtree(ast, arg);
            if (arg instanceof MethodInvocation) {
              IMethodBinding argBinding = ((MethodInvocation) arg).resolveMethodBinding();
              if (!argBinding.getReturnType().isPrimitive() &&
                  !Types.isJavaStringType(argBinding.getReturnType())) {
                IOSMethodBinding newBinding =
                    new IOSMethodBinding("format", argBinding, Types.getNSString());
                Types.addMappedInvocation((MethodInvocation) newArg, newBinding);
              }
            }
            newInvocation.arguments().add(newArg);
          } else if (args.size() > 1 && node.getName().getIdentifier().equals("printf")) {
            newInvocation.arguments().addAll(NodeCopier.copySubtrees(ast, args));
          } else if (args.size() == 0) {
            // NSLog requires a format string.
            StringLiteral literal = ast.newStringLiteral();
            literal.setLiteralValue("");
            Types.addBinding(literal,  ast.resolveWellKnownType("java.lang.String"));
            newInvocation.arguments().add(literal);
          }

          // Replace old invocation with new.
          ASTNode parent = node.getParent();
          if (parent instanceof ExpressionStatement) {
            ExpressionStatement stmt = (ExpressionStatement) parent;
            stmt.setExpression(newInvocation);
          } else {
            throw new AssertionError("unknown parent type: " + parent.getClass().getSimpleName());
          }
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Rewrites String.format()'s format string to be iOS-compatible.
   *
   * @return true if the node was rewritten
   */
  private boolean rewriteStringFormat(MethodInvocation node) {
    IMethodBinding binding = node.resolveMethodBinding();
    if (binding == null) {
      // No binding due to error already reported.
      return false;
    }
    ITypeBinding typeBinding = binding.getDeclaringClass();
    AST ast = node.getAST();
    if (typeBinding.equals(ast.resolveWellKnownType("java.lang.String"))
        && binding.getName().equals("format")) {

      @SuppressWarnings("unchecked")
      List<Expression> args = node.arguments();
      if (args.isEmpty()) {
        return false;
      }
      Expression first = args.get(0);
      typeBinding = first.resolveTypeBinding();
      if (typeBinding.getQualifiedName().equals("java.util.Locale")) {
        args.remove(0); // discard locale parameter
        first = args.get(0);
        typeBinding = first.resolveTypeBinding();
      }
      if (first instanceof StringLiteral) {
        String format = ((StringLiteral) first).getLiteralValue();
        String convertedFormat = convertStringFormatString(format);
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
   * Given a AST node, return the appropriate printf() format specifier.
   */
  private String getFormatArgument(ASTNode node) {
    ITypeBinding type = Types.getTypeBinding(node);
    AST ast = node.getAST();
    if (node instanceof CharacterLiteral || type.isEqualTo(ast.resolveWellKnownType("char"))) {
      return "%C";
    }
    if (node instanceof BooleanLiteral || type.isEqualTo(ast.resolveWellKnownType("boolean"))) {
      return "%d";
    }
    if (type.isEqualTo(ast.resolveWellKnownType("byte")) ||
        type.isEqualTo(ast.resolveWellKnownType("int")) ||
        type.isEqualTo(ast.resolveWellKnownType("short"))) {
      return "%d";
    }
    if (type.isEqualTo(ast.resolveWellKnownType("long"))) {
      return "%lld";
    }
    if (type.isEqualTo(ast.resolveWellKnownType("float")) ||
        type.isEqualTo(ast.resolveWellKnownType("double"))) {
      return "%f";
    }
    if (node instanceof NumberLiteral) {
      String token = ((NumberLiteral) node).getToken();
      try {
        Integer.parseInt(token);
        return "%d";
      } catch (NumberFormatException e) {
        try {
          Long.parseLong(token);
          return "%lld";
        } catch (NumberFormatException e2) {
          try {
            Double.parseDouble(token);
            return "%f";
          } catch (NumberFormatException e3) {
            throw new AssertionError("unknown number literal format: \"" + token + "\"");
          }
        }
      }
    }
    return "%@"; // object, including string
  }

  /**
   * Convert a Java string format string into a NSString equivalent.
   */
  @SuppressWarnings("fallthrough")
  private String convertStringFormatString(String s) {
    if (s.isEmpty()) {
      return s;
    }
    String[] parts = s.split("%");
    StringBuffer result = new StringBuffer();
    int i = 0;
    if (!s.startsWith("%")) {
      result.append(parts[0]);
      i++;
    }
    while (i < parts.length) {
      String part = parts[i];
      if (part.length() > 0) {
        result.append('%');
        switch (part.charAt(0)) {
          case 's':
          case 'S':
            result.append('@');
            break;
          case 'c':
          case 'C':
            result.append('C');
            break;
          case 'h':
          case 'H':
            result.append('x');
            break;

          // These aren't mapped, so escape them so it's obvious when output
          case 'b':
          case 'B':
          case 't':
          case 'T':
          case 'n':
            result.append('%');
            // falls through
          default:
            result.append(part.charAt(0));
        }
        result.append(part.substring(1));
      }
      i++;
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

    @SuppressWarnings("unchecked")
    List<Modifier> modifiers = method.modifiers();
    modifiers.add(ast.newModifier(ModifierKeyword.ABSTRACT_KEYWORD));

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

    @SuppressWarnings("unchecked")
    List<Expression> args = superInvocation.arguments();  // safe by definition
    for (SingleVariableDeclaration param : getParameters(method)) {
      Expression arg = NodeCopier.copySubtree(ast, param.getName());
      args.add(arg);
    }
    Types.addBinding(superInvocation, Types.getMethodBinding(method));
    ReturnStatement returnStmt = ast.newReturnStatement();
    returnStmt.setExpression(superInvocation);
    getStatements(body).add(returnStmt);

    decls.add(method);
  }

  private MethodDeclaration createInterfaceMethodBody(
      AST ast, ITypeBinding typeBinding, IMethodBinding interfaceMethod) {
    IMethodBinding methodBinding = new IOSMethodBinding(interfaceMethod.getName(), interfaceMethod,
        typeBinding);

    MethodDeclaration method = ast.newMethodDeclaration();
    Types.addBinding(method, methodBinding);
    method.setReturnType2(Types.makeType(interfaceMethod.getReturnType()));

    SimpleName methodName = ast.newSimpleName(interfaceMethod.getName());
    Types.addBinding(methodName, methodBinding);
    method.setName(methodName);

    @SuppressWarnings("unchecked")
    List<Modifier> modifiers = method.modifiers();
    modifiers.add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));

    ITypeBinding[] parameterTypes = interfaceMethod.getParameterTypes();
    for (int i = 0; i < parameterTypes.length; i++) {
      ITypeBinding paramType = parameterTypes[i];
      String paramName = "param" + i;
      SingleVariableDeclaration param = ast.newSingleVariableDeclaration();
      IVariableBinding paramBinding = IOSVariableBinding.newParameter(paramName, i, paramType,
          methodBinding, paramType.getDeclaringClass(),
          Modifier.isFinal(paramType.getModifiers()));
      Types.addBinding(param, paramBinding);
      param.setName(ast.newSimpleName(paramName));
      Types.addBinding(param.getName(), paramBinding);
      param.setType(Types.makeType(paramType));
      getParameters(method).add(param);
    }
    Symbols.scanAST(method);
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

    // Check all declared fields with method names.
    addFields(typeBinding, true, false, fields);
    for (IMethodBinding method : typeBinding.getDeclaredMethods()) {
      String name = method.getName();
      IVariableBinding field = fields.get(name);
      if (field != null) {
        IVariableBinding newField;
        while ((newField = fields.get(name)) != null) {
          name += '_';
          field = newField;
        }
        NameTable.rename(field, name, true);
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
}
