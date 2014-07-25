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

import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.PointerTypeBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.j2objc.annotations.AutoreleasePool;
import com.google.j2objc.annotations.LoopTranslation;
import com.google.j2objc.annotations.LoopTranslation.LoopStyle;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import java.util.List;

/**
 * Rewrites Java enhanced for loops into appropriate C constructs.
 *
 * @author Keith Stanger
 */
public class EnhancedForRewriter extends ErrorReportingASTVisitor {

  @Override
  public void endVisit(EnhancedForStatement node) {
    AST ast = node.getAST();
    Expression expression = node.getExpression();
    ITypeBinding expressionType = Types.getTypeBinding(expression);
    IVariableBinding loopVariable = Types.getVariableBinding(node.getParameter());

    if (BindingUtil.hasAnnotation(loopVariable, AutoreleasePool.class)) {
      Types.addAutoreleasePool(makeBlock(node.getBody()));
    }

    if (expressionType.isArray()) {
      ASTUtil.setProperty(node, makeArrayIterationBlock(
          ast, expression, expressionType, loopVariable, node.getBody()));
    } else if (emitJavaIteratorLoop(loopVariable)) {
      ASTUtil.setProperty(node, makeIterableBlock(
          ast, expression, expressionType, loopVariable, node.getBody()));
    } else if (loopVariable.getType().isPrimitive()) {
      boxLoopVariable(ast, node, expressionType, loopVariable);
    } else {
      GeneratedVariableBinding newLoopVariable = new GeneratedVariableBinding(loopVariable);
      newLoopVariable.setTypeQualifiers("__strong");
      Types.addBinding(node.getParameter(), newLoopVariable);
    }
  }

  private Block makeArrayIterationBlock(
      AST ast, Expression expression, ITypeBinding expressionType, IVariableBinding loopVariable,
      Statement loopBody) {
    ITypeBinding componentType = expressionType.getComponentType();
    ITypeBinding iosArrayType = Types.resolveArrayType(componentType);
    PointerTypeBinding bufferType = new PointerTypeBinding(componentType);
    IVariableBinding arrayVariable = new GeneratedVariableBinding(
        "a__", 0, expressionType, false, false, null, null);
    GeneratedVariableBinding bufferVariable = new GeneratedVariableBinding(
        "b__", 0, bufferType, false, false, null, null);
    bufferVariable.setTypeQualifiers("const*");
    GeneratedVariableBinding endVariable = new GeneratedVariableBinding(
        "e__", 0, bufferType, false, false, null, null);
    endVariable.setTypeQualifiers("const*");
    IVariableBinding bufferField = new GeneratedVariableBinding(
        "buffer", Modifier.PUBLIC, bufferType, true, false, iosArrayType, null);
    IVariableBinding sizeField = new GeneratedVariableBinding(
        "size", Modifier.PUBLIC, Types.resolveJavaType("int"), true, false, iosArrayType, null);

    VariableDeclarationStatement arrayDecl = ASTFactory.newVariableDeclarationStatement(
        ast, arrayVariable, NodeCopier.copySubtree(ast, expression));
    FieldAccess bufferAccess = ASTFactory.newFieldAccess(
        ast, bufferField, ASTFactory.newSimpleName(ast, arrayVariable));
    VariableDeclarationStatement bufferDecl = ASTFactory.newVariableDeclarationStatement(
        ast, bufferVariable, bufferAccess);
    InfixExpression endInit = ASTFactory.newInfixExpression(
        ast, ASTFactory.newSimpleName(ast, bufferVariable), InfixExpression.Operator.PLUS,
        ASTFactory.newFieldAccess(ast, sizeField, ASTFactory.newSimpleName(ast, arrayVariable)),
        bufferType);
    VariableDeclarationStatement endDecl = ASTFactory.newVariableDeclarationStatement(
        ast, endVariable, endInit);

    WhileStatement loop = ast.newWhileStatement();
    loop.setExpression(ASTFactory.newInfixExpression(
        ast, ASTFactory.newSimpleName(ast, bufferVariable), InfixExpression.Operator.LESS,
        ASTFactory.newSimpleName(ast, endVariable), Types.resolveJavaType("boolean")));
    Block newLoopBody = makeBlock(NodeCopier.copySubtree(ast, loopBody));
    loop.setBody(newLoopBody);
    ASTUtil.getStatements(newLoopBody).add(0, ASTFactory.newVariableDeclarationStatement(
        ast, loopVariable, ASTFactory.newDereference(ast, ASTFactory.newPostfixExpression(
            ast, bufferVariable, PostfixExpression.Operator.INCREMENT))));

    Block block = ast.newBlock();
    List<Statement> stmts = ASTUtil.getStatements(block);
    stmts.add(arrayDecl);
    stmts.add(bufferDecl);
    stmts.add(endDecl);
    stmts.add(loop);

    return block;
  }

  private boolean emitJavaIteratorLoop(IVariableBinding loopVariable) {
    IAnnotationBinding loopTranslation =
        BindingUtil.getAnnotation(loopVariable, LoopTranslation.class);
    if (loopTranslation == null) {
      return false;
    }
    Object style = BindingUtil.getAnnotationValue(loopTranslation, "value");
    if (style instanceof IVariableBinding
        && ((IVariableBinding) style).getName().equals(LoopStyle.JAVA_ITERATOR.name())) {
      return true;
    }
    return false;
  }

  private Block makeIterableBlock(
      AST ast, Expression expression, ITypeBinding expressionType, IVariableBinding loopVariable,
      Statement loopBody) {
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

    Block newLoopBody = makeBlock(NodeCopier.copySubtree(ast, loopBody));
    ASTUtil.getStatements(newLoopBody).add(0, ASTFactory.newVariableDeclarationStatement(
        ast, loopVariable, nextInvocation));

    WhileStatement whileLoop = ast.newWhileStatement();
    whileLoop.setExpression(hasNextInvocation);
    whileLoop.setBody(newLoopBody);

    Block block = ast.newBlock();
    List<Statement> stmts = ASTUtil.getStatements(block);
    stmts.add(iteratorDecl);
    stmts.add(whileLoop);

    return block;
  }

  private void boxLoopVariable(
      AST ast, EnhancedForStatement node, ITypeBinding expressionType,
      IVariableBinding loopVariable) {
    ITypeBinding[] typeArgs = expressionType.getTypeArguments();
    if (typeArgs.length == 0) {
      ITypeBinding iterableType = BindingUtil.findInterface(expressionType, "java.lang.Iterable");
      typeArgs = iterableType != null ? iterableType.getTypeArguments() : new ITypeBinding[0];
    }
    assert typeArgs.length == 1 && Types.isBoxedPrimitive(typeArgs[0]);
    IVariableBinding boxVariable = new GeneratedVariableBinding(
        "boxed__", 0, typeArgs[0], false, false, null, null);
    node.setParameter(ASTFactory.newSingleVariableDeclaration(ast, boxVariable));
    ASTUtil.getStatements(makeBlock(node.getBody())).add(0,
        ASTFactory.newVariableDeclarationStatement(
        ast, loopVariable, ASTFactory.newSimpleName(ast, boxVariable)));
  }

  private Block makeBlock(Statement stmt) {
    if (stmt instanceof Block) {
      return (Block) stmt;
    }
    Block block = stmt.getAST().newBlock();
    if (stmt.getParent() != null) {
      ASTUtil.setProperty(stmt, block);
    }
    ASTUtil.getStatements(block).add(stmt);
    return block;
  }
}
