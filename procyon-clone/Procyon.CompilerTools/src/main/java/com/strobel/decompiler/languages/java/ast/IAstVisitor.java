/*
 * IAstVisitor.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is based on Mono.Cecil from Jb Evain, Copyright (c) Jb Evain;
 * and ILSpy/ICSharpCode from SharpDevelop, Copyright (c) AlphaSierraPapa.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.Pattern;

public interface IAstVisitor<T, R> {
    R visitComment(Comment node, T data);
    R visitPatternPlaceholder(AstNode node, Pattern pattern, T data);
    R visitInvocationExpression(InvocationExpression node, T data);
    R visitTypeReference(TypeReferenceExpression node, T data);
    R visitJavaTokenNode(JavaTokenNode node, T data);
    R visitMemberReferenceExpression(MemberReferenceExpression node, T data);
    R visitIdentifier(Identifier node, T data);
    R visitNullReferenceExpression(NullReferenceExpression node, T data);
    R visitThisReferenceExpression(ThisReferenceExpression node, T data);
    R visitSuperReferenceExpression(SuperReferenceExpression node, T data);
    R visitClassOfExpression(ClassOfExpression node, T data);
    R visitBlockStatement(BlockStatement node, T data);
    R visitExpressionStatement(ExpressionStatement node, T data);
    R visitBreakStatement(BreakStatement node, T data);
    R visitContinueStatement(ContinueStatement node, T data);
    R visitDoWhileStatement(DoWhileStatement node, T data);
    R visitEmptyStatement(EmptyStatement node, T data);
    R visitIfElseStatement(IfElseStatement node, T data);
    R visitLabelStatement(LabelStatement node, T data);
    R visitLabeledStatement(LabeledStatement node, T data);
    R visitReturnStatement(ReturnStatement node, T data);
    R visitSwitchStatement(SwitchStatement node, T data);
    R visitSwitchSection(SwitchSection node, T data);
    R visitCaseLabel(CaseLabel node, T data);
    R visitThrowStatement(ThrowStatement node, T data);
    R visitCatchClause(CatchClause node, T data);
    R visitAnnotation(Annotation node, T data);
    R visitNewLine(NewLineNode node, T data);
    R visitVariableDeclaration(VariableDeclarationStatement node, T data);
    R visitVariableInitializer(VariableInitializer node, T data);
    R visitText(TextNode node, T data);
    R visitImportDeclaration(ImportDeclaration node, T data);
    R visitSimpleType(SimpleType node, T data);
    R visitMethodDeclaration(MethodDeclaration node, T data);
    R visitInitializerBlock(InstanceInitializer node, T data);
    R visitConstructorDeclaration(ConstructorDeclaration node, T data);
    R visitTypeParameterDeclaration(TypeParameterDeclaration node, T data);
    R visitParameterDeclaration(ParameterDeclaration node, T data);
    R visitFieldDeclaration(FieldDeclaration node, T data);
    R visitTypeDeclaration(TypeDeclaration node, T data);
    R visitCompilationUnit(CompilationUnit node, T data);
    R visitPackageDeclaration(PackageDeclaration node, T data);
    R visitArraySpecifier(ArraySpecifier node, T data);
    R visitComposedType(ComposedType node, T data);
    R visitWhileStatement(WhileStatement node, T data);
    R visitPrimitiveExpression(PrimitiveExpression node, T data);
    R visitCastExpression(CastExpression node, T data);
    R visitBinaryOperatorExpression(BinaryOperatorExpression node, T data);
    R visitInstanceOfExpression(InstanceOfExpression node, T data);
    R visitIndexerExpression(IndexerExpression node, T data);
    R visitIdentifierExpression(IdentifierExpression node, T data);
    R visitUnaryOperatorExpression(UnaryOperatorExpression node, T data);
    R visitConditionalExpression(ConditionalExpression node, T data);
    R visitArrayInitializerExpression(ArrayInitializerExpression node, T data);
    R visitObjectCreationExpression(ObjectCreationExpression node, T data);
    R visitArrayCreationExpression(ArrayCreationExpression node, T data);
    R visitAssignmentExpression(AssignmentExpression node, T data);
    R visitForStatement(ForStatement node, T data);
    R visitForEachStatement(ForEachStatement node, T data);
    R visitTryCatchStatement(TryCatchStatement node, T data);
    R visitGotoStatement(GotoStatement node, T data);
    R visitParenthesizedExpression(ParenthesizedExpression node, T data);
    R visitSynchronizedStatement(SynchronizedStatement node, T data);
    R visitAnonymousObjectCreationExpression(AnonymousObjectCreationExpression node, T data);
    R visitWildcardType(WildcardType node, T data);
    R visitMethodGroupExpression(MethodGroupExpression node, T data);
    R visitEnumValueDeclaration(EnumValueDeclaration node, T data);
    R visitAssertStatement(AssertStatement node, T data);
    R visitLambdaExpression(LambdaExpression node, T data);
    R visitLocalTypeDeclarationStatement(LocalTypeDeclarationStatement node, T data);
}
