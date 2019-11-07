/*
 * DepthFirstAstVisitor.java
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

/// <summary>
/// AST visitor with a default implementation that visits all node depth-first.
/// </summary>
public abstract class DepthFirstAstVisitor<T, S> implements IAstVisitor<T, S> {
    protected boolean shouldContinue() {
        return true;
    }

    protected S visitChildren(final AstNode node, final T data) {
        AstNode next;
        
        for (AstNode child = node.getFirstChild(); child != null; child = next) {
            if (!shouldContinue()) {
                break;
            }

            //
            // Store next to allow the loop to continue if the visitor removes/replaces child.
            //
            next = child.getNextSibling();
            child.acceptVisitor(this, data);
        }
        
        return null;
    }

    @Override
    public S visitComment(final Comment node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitPatternPlaceholder(final AstNode node, final Pattern pattern, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitInvocationExpression(final InvocationExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitTypeReference(final TypeReferenceExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitJavaTokenNode(final JavaTokenNode node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitMemberReferenceExpression(final MemberReferenceExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitIdentifier(final Identifier node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitNullReferenceExpression(final NullReferenceExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitThisReferenceExpression(final ThisReferenceExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitSuperReferenceExpression(final SuperReferenceExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitClassOfExpression(final ClassOfExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitBlockStatement(final BlockStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitExpressionStatement(final ExpressionStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitBreakStatement(final BreakStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitContinueStatement(final ContinueStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitDoWhileStatement(final DoWhileStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitEmptyStatement(final EmptyStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitIfElseStatement(final IfElseStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitLabelStatement(final LabelStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitLabeledStatement(final LabeledStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitReturnStatement(final ReturnStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitSwitchStatement(final SwitchStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitSwitchSection(final SwitchSection node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitCaseLabel(final CaseLabel node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitThrowStatement(final ThrowStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitCatchClause(final CatchClause node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitAnnotation(final Annotation node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitNewLine(final NewLineNode node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitVariableDeclaration(final VariableDeclarationStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitVariableInitializer(final VariableInitializer node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitText(final TextNode node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitImportDeclaration(final ImportDeclaration node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitSimpleType(final SimpleType node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitMethodDeclaration(final MethodDeclaration node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitInitializerBlock(final InstanceInitializer node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitConstructorDeclaration(final ConstructorDeclaration node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitTypeParameterDeclaration(final TypeParameterDeclaration node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitParameterDeclaration(final ParameterDeclaration node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitFieldDeclaration(final FieldDeclaration node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitTypeDeclaration(final TypeDeclaration node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitCompilationUnit(final CompilationUnit node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitPackageDeclaration(final PackageDeclaration node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitArraySpecifier(final ArraySpecifier node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitComposedType(final ComposedType node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitWhileStatement(final WhileStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitPrimitiveExpression(final PrimitiveExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitCastExpression(final CastExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitBinaryOperatorExpression(final BinaryOperatorExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitInstanceOfExpression(final InstanceOfExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitIndexerExpression(final IndexerExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitIdentifierExpression(final IdentifierExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitUnaryOperatorExpression(final UnaryOperatorExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitConditionalExpression(final ConditionalExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitArrayInitializerExpression(final ArrayInitializerExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitObjectCreationExpression(final ObjectCreationExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitArrayCreationExpression(final ArrayCreationExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitAssignmentExpression(final AssignmentExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitForStatement(final ForStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitForEachStatement(final ForEachStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitGotoStatement(final GotoStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitParenthesizedExpression(final ParenthesizedExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitSynchronizedStatement(final SynchronizedStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitAnonymousObjectCreationExpression(final AnonymousObjectCreationExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitWildcardType(final WildcardType node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitMethodGroupExpression(final MethodGroupExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitEnumValueDeclaration(final EnumValueDeclaration node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitAssertStatement(final AssertStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitLambdaExpression(final LambdaExpression node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitLocalTypeDeclarationStatement(final LocalTypeDeclarationStatement node, final T data) {
        return visitChildren(node, data);
    }

    @Override
    public S visitTryCatchStatement(final TryCatchStatement node, final T data) {
        return visitChildren(node, data);
    }
}
