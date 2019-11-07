/*
 * IntroduceInitializersTransform.java
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

package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.FieldReference;
import com.strobel.assembler.metadata.MemberReference;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.core.StringUtilities;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.patterns.AnyNode;
import com.strobel.decompiler.patterns.Choice;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.MemberReferenceTypeNode;
import com.strobel.decompiler.patterns.Pattern;
import com.strobel.decompiler.patterns.TypedNode;

import java.util.HashMap;
import java.util.Map;

import static com.strobel.core.CollectionUtilities.firstOrDefault;

public class IntroduceInitializersTransform extends ContextTrackingVisitor<Void> {
    private final Map<String, FieldDeclaration> _fieldDeclarations;
    private final Map<String, AssignmentExpression> _initializers;

    private MethodDefinition _currentInitializerMethod;
    private MethodDefinition _currentConstructor;

    public IntroduceInitializersTransform(final DecompilerContext context) {
        super(context);

        _fieldDeclarations = new HashMap<>();
        _initializers = new HashMap<>();
    }

    @Override
    public void run(final AstNode compilationUnit) {
        new FieldGatherer().run(compilationUnit);

        super.run(compilationUnit);

        inlineInitializers();

        LocalClassHelper.introduceInitializerBlocks(context, compilationUnit);
    }

    private void inlineInitializers() {
        for (final String fieldName : _initializers.keySet()) {
            final FieldDeclaration declaration = _fieldDeclarations.get(fieldName);

            if (declaration != null &&
                declaration.getVariables().firstOrNullObject().getInitializer().isNull()) {

                final AssignmentExpression assignment = _initializers.get(fieldName);
                final Expression value = assignment.getRight();

                value.remove();
                declaration.getVariables().firstOrNullObject().setInitializer(value);

                final AstNode parent = assignment.getParent();

                if (parent instanceof ExpressionStatement) {
                    parent.remove();
                }
                else if (parent.getRole() == Roles.VARIABLE) {
                    final Expression left = assignment.getLeft();

                    left.remove();
                    assignment.replaceWith(left);
                }
                else {
                    final Expression left = assignment.getLeft();

                    left.remove();
                    parent.replaceWith(left);
                }
            }
        }
    }

    @Override
    public Void visitAnonymousObjectCreationExpression(final AnonymousObjectCreationExpression node, final Void data) {
        final MethodDefinition oldInitializer = _currentInitializerMethod;
        final MethodDefinition oldConstructor = _currentConstructor;

        _currentInitializerMethod = null;
        _currentConstructor = null;

        try {
            return super.visitAnonymousObjectCreationExpression(node, data);
        }
        finally {
            _currentInitializerMethod = oldInitializer;
            _currentConstructor = oldConstructor;
        }
    }

    @Override
    public Void visitMethodDeclaration(final MethodDeclaration node, final Void p) {
        final MethodDefinition oldInitializer = _currentInitializerMethod;
        final MethodDefinition oldConstructor = _currentConstructor;

        final MethodDefinition method = node.getUserData(Keys.METHOD_DEFINITION);

        if (method != null && method.isTypeInitializer()) {
            _currentConstructor = null;
            _currentInitializerMethod = method;
        }
        else {
            _currentConstructor = method != null && method.isConstructor() ? method : null;
            _currentInitializerMethod = null;
        }

        try {
            return super.visitMethodDeclaration(node, p);
        }
        finally {
            _currentConstructor = oldConstructor;
            _currentInitializerMethod = oldInitializer;
        }
    }

    private final static INode FIELD_ASSIGNMENT;

    static {
        FIELD_ASSIGNMENT = new AssignmentExpression(
            new MemberReferenceTypeNode(
                "target",
                new Choice(
                    new MemberReferenceExpression(
                        Expression.MYSTERY_OFFSET,
                        new Choice(
                            new TypedNode(TypeReferenceExpression.class),
                            new TypedNode(ThisReferenceExpression.class)
                        ).toExpression(),
                        Pattern.ANY_STRING
                    ),
                    new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)
                ).toExpression(),
                FieldReference.class
            ).toExpression(),
            AssignmentOperatorType.ASSIGN,
            new AnyNode("value").toExpression()
        );
    }

    @Override
    public Void visitAssignmentExpression(final AssignmentExpression node, final Void data) {
        super.visitAssignmentExpression(node, data);

        if (!(node.getParent() instanceof Statement)) {
            //
            // We only care about top-level assignment statements.
            //
            return null;
        }

        if (_currentInitializerMethod == null && _currentConstructor == null || context.getCurrentType() == null) {
            return null;
        }

        final Match match = FIELD_ASSIGNMENT.match(node);

        if (match.success()) {
            final Expression target = (Expression) firstOrDefault(match.get("target"));
            final FieldReference reference = (FieldReference) target.getUserData(Keys.MEMBER_REFERENCE);
            final FieldDefinition definition = reference.resolve();

            //
            // Final fields with constant values area already initialized inline, so any additional assignments
            // are illegal; remove them.
            //
            if (definition != null && definition.isFinal() && definition.getConstantValue() != null) {
                node.getParent().remove();
                return null;
            }

            //
            // Interfaces may not contain static initializer blocks, so if we're in such a block, try to
            // transform the assignments into field initializers.
            //
            if (_currentInitializerMethod != null &&
                _currentInitializerMethod.getDeclaringType().isInterface() &&
                StringUtilities.equals(context.getCurrentType().getInternalName(), reference.getDeclaringType().getInternalName())) {

                _initializers.put(reference.getFullName(), node);
            }
        }

        return null;
    }

    @Override
    public Void visitSuperReferenceExpression(final SuperReferenceExpression node, final Void p) {
        super.visitSuperReferenceExpression(node, p);

        final MethodDefinition method = context.getCurrentMethod();

        if (method != null &&
            method.isConstructor() &&
            (method.isSynthetic() || method.getDeclaringType().isAnonymous()) &&
            node.getParent() instanceof InvocationExpression &&
            node.getRole() == Roles.TARGET_EXPRESSION) {

            //
            // For anonymous classes, take all statements after the base constructor call and move them
            // into an instance initializer block.
            //

            final Statement parentStatement = firstOrDefault(node.getAncestors(Statement.class));
            final ConstructorDeclaration constructor = firstOrDefault(node.getAncestors(ConstructorDeclaration.class));

            if (parentStatement == null ||
                constructor == null ||
                constructor.getParent() == null ||
                parentStatement.getNextStatement() == null) {

                return null;
            }

            for (Statement current = parentStatement.getNextStatement();
                 current instanceof ExpressionStatement; ) {

                final Statement next = current.getNextStatement();
                final Expression expression = ((ExpressionStatement) current).getExpression();
                final Match match = FIELD_ASSIGNMENT.match(expression);

                if (match.success()) {
                    final Expression target = (Expression) firstOrDefault(match.get("target"));
                    final MemberReference reference = target.getUserData(Keys.MEMBER_REFERENCE);

                    if (StringUtilities.equals(context.getCurrentType().getInternalName(), reference.getDeclaringType().getInternalName())) {
                        _initializers.put(
                            reference.getFullName(),
                            (AssignmentExpression) expression
                        );
                    }
                }
                else {
                    break;
                }

                current = next;
            }
        }

        return null;
    }

    private final class FieldGatherer extends ContextTrackingVisitor<Void> {
        public FieldGatherer() {
            super(IntroduceInitializersTransform.this.context);
        }

        @Override
        public Void visitFieldDeclaration(final FieldDeclaration node, final Void p) {
            final FieldDefinition field = node.getUserData(Keys.FIELD_DEFINITION);

            if (field != null) {
                _fieldDeclarations.put(field.getFullName(), node);
            }

            return super.visitFieldDeclaration(node, p);
        }
    }
}
