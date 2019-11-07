/*
 * LocalClassHelper.java
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

import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.FieldReference;
import com.strobel.assembler.metadata.MemberReference;
import com.strobel.assembler.metadata.MetadataHelper;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.ParameterDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.ast.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.strobel.core.CollectionUtilities.*;

public final class LocalClassHelper {
    private static final ConvertTypeOptions OUTER_TYPE_CONVERT_OPTIONS;

    static {
        OUTER_TYPE_CONVERT_OPTIONS = new ConvertTypeOptions(false, false);
        OUTER_TYPE_CONVERT_OPTIONS.setIncludeTypeArguments(false);
    }

    public static void replaceClosureMembers(final DecompilerContext context, final AnonymousObjectCreationExpression node) {
        replaceClosureMembers(context, node.getTypeDeclaration(), Collections.singletonList(node));
    }

    public static void replaceClosureMembers(
        final DecompilerContext context,
        final TypeDeclaration declaration,
        final List<? extends ObjectCreationExpression> instantiations) {

        VerifyArgument.notNull(context, "context");
        VerifyArgument.notNull(declaration, "declaration");
        VerifyArgument.notNull(instantiations, "instantiations");

        final Map<String, Expression> initializers = new HashMap<>();
        final Map<String, Expression> replacements = new HashMap<>();
        final List<AstNode> nodesToRemove = new ArrayList<>();
        final List<ParameterDefinition> parametersToRemove = new ArrayList<>();
        final List<Expression> originalArguments;

        if (instantiations.isEmpty()) {
            originalArguments = Collections.emptyList();
        }
        else {
            originalArguments = new ArrayList<>(instantiations.get(0).getArguments());
        }

        new ClosureRewriterPhaseOneVisitor(context, originalArguments, replacements, initializers, parametersToRemove, nodesToRemove).run(declaration);

        rewriteThisReferences(context, declaration, initializers);

        new ClosureRewriterPhaseTwoVisitor(context, replacements, initializers).run(declaration);

        for (final ObjectCreationExpression instantiation : instantiations) {
            for (final ParameterDefinition p : parametersToRemove) {
                final Expression argumentToRemove = getOrDefault(instantiation.getArguments(), p.getPosition());

                if (argumentToRemove != null) {
                    instantiation.getArguments().remove(argumentToRemove);
                }
            }
        }

        for (final AstNode n : nodesToRemove) {
            if (n instanceof Expression) {
                final int argumentIndex = originalArguments.indexOf(n);

                if (argumentIndex >= 0) {
                    for (final ObjectCreationExpression instantiation : instantiations) {
                        final Expression argumentToRemove = getOrDefault(instantiation.getArguments(), argumentIndex);

                        if (argumentToRemove != null) {
                            argumentToRemove.remove();
                        }
                    }
                }
            }

            n.remove();
        }
    }

    public static void introduceInitializerBlocks(final DecompilerContext context, final AstNode node) {
        VerifyArgument.notNull(context, "context");
        VerifyArgument.notNull(node, "node");

        new IntroduceInitializersVisitor(context).run(node);
    }

    private static void rewriteThisReferences(
        final DecompilerContext context,
        final TypeDeclaration declaration,
        final Map<String, Expression> initializers) {

        final TypeDefinition innerClass = declaration.getUserData(Keys.TYPE_DEFINITION);

        if (innerClass != null) {
            final ContextTrackingVisitor<Void> thisRewriter = new ThisReferenceReplacingVisitor(context, innerClass);

            for (final Expression e : initializers.values()) {
                thisRewriter.run(e);
            }
        }
    }

    private final static class ClosureRewriterPhaseOneVisitor extends ContextTrackingVisitor<Void> {
        private final Map<String, Expression> _replacements;
        private final List<Expression> _originalArguments;
        private final List<ParameterDefinition> _parametersToRemove;
        private final Map<String, Expression> _initializers;
        private final List<AstNode> _nodesToRemove;

        private boolean _baseConstructorCalled;

        public ClosureRewriterPhaseOneVisitor(
            final DecompilerContext context,
            final List<Expression> originalArguments,
            final Map<String, Expression> replacements,
            final Map<String, Expression> initializers,
            final List<ParameterDefinition> parametersToRemove,
            final List<AstNode> nodesToRemove) {

            super(context);

            _originalArguments = VerifyArgument.notNull(originalArguments, "originalArguments");
            _replacements = VerifyArgument.notNull(replacements, "replacements");
            _initializers = VerifyArgument.notNull(initializers, "initializers");
            _parametersToRemove = VerifyArgument.notNull(parametersToRemove, "parametersToRemove");
            _nodesToRemove = VerifyArgument.notNull(nodesToRemove, "nodesToRemove");
        }

        @Override
        public Void visitConstructorDeclaration(final ConstructorDeclaration node, final Void p) {
            final boolean wasDone = _baseConstructorCalled;

            _baseConstructorCalled = false;

            try {
                return super.visitConstructorDeclaration(node, p);
            }
            finally {
                _baseConstructorCalled = wasDone;
            }
        }

        @Override
        protected Void visitChildren(final AstNode node, final Void p) {
            final MethodDefinition currentMethod = context.getCurrentMethod();

            if (currentMethod != null && !(currentMethod.isConstructor()/* && currentMethod.isSynthetic()*/)) {
                return null;
            }

            return super.visitChildren(node, p);
        }

        @Override
        public Void visitSuperReferenceExpression(final SuperReferenceExpression node, final Void p) {
            super.visitSuperReferenceExpression(node, p);

            if (context.getCurrentMethod() != null &&
                context.getCurrentMethod().isConstructor() &&
                node.getParent() instanceof InvocationExpression) {

                //
                // We only care about field initializations that occur before the base constructor call.
                //
                _baseConstructorCalled = true;
            }

            return null;
        }

        @Override
        public Void visitAssignmentExpression(final AssignmentExpression node, final Void p) {
            super.visitAssignmentExpression(node, p);

            if (context.getCurrentMethod() == null || !context.getCurrentMethod().isConstructor()) {
                return null;
            }

            final Expression left = node.getLeft();
            final Expression right = node.getRight();

            if (left instanceof MemberReferenceExpression) {
                if (right instanceof IdentifierExpression) {
                    final Variable variable = right.getUserData(Keys.VARIABLE);

                    if (variable == null || !variable.isParameter()) {
                        return null;
                    }

                    final MemberReferenceExpression memberReference = (MemberReferenceExpression) left;
                    final MemberReference member = memberReference.getUserData(Keys.MEMBER_REFERENCE);

                    if (member instanceof FieldReference &&
                        memberReference.getTarget() instanceof ThisReferenceExpression) {

                        final FieldDefinition resolvedField = ((FieldReference) member).resolve();

                        if (resolvedField != null && resolvedField.isSynthetic()) {
                            final ParameterDefinition parameter = variable.getOriginalParameter();

                            if (parameter == null) {
                                return null;
                            }

                            int parameterIndex = parameter.getPosition();

                            if (parameter.getMethod().getParameters().size() > _originalArguments.size()) {
                                parameterIndex -= (parameter.getMethod().getParameters().size() - _originalArguments.size());
                            }

                            if (parameterIndex >= 0 && parameterIndex < _originalArguments.size()) {
                                final Expression argument = _originalArguments.get(parameterIndex);

                                if (argument == null) {
                                    return null;
                                }

                                _nodesToRemove.add(argument);

                                if (argument instanceof ThisReferenceExpression) {
                                    //
                                    // Don't replace outer class references; they will be rewritten later.
                                    //
                                    markConstructorParameterForRemoval(node, parameter);
                                    return null;
                                }

                                _parametersToRemove.add(parameter);

                                final String fullName = member.getFullName();

                                if (!hasSideEffects(argument)) {
                                    _replacements.put(fullName, argument);
                                }
                                else {
                                    context.getForcedVisibleMembers().add(resolvedField);
                                    _initializers.put(fullName, argument);
                                }

                                if (node.getParent() instanceof ExpressionStatement) {
                                    _nodesToRemove.add(node.getParent());
                                }

                                markConstructorParameterForRemoval(node, parameter);
                            }
                        }
                        else if (_baseConstructorCalled &&
                                 resolvedField != null &&
                                 context.getCurrentMethod().isConstructor() &&
                                 (!context.getCurrentMethod().isSynthetic() ||
                                  context.getSettings().getShowSyntheticMembers())) {

                            final MemberReferenceExpression leftMemberReference = (MemberReferenceExpression) left;
                            final MemberReference leftMember = leftMemberReference.getUserData(Keys.MEMBER_REFERENCE);
                            final Variable rightVariable = right.getUserData(Keys.VARIABLE);

                            if (rightVariable.isParameter()) {
                                final ParameterDefinition parameter = variable.getOriginalParameter();

                                if (parameter == null) {
                                    return null;
                                }
                                
                                final int parameterIndex = parameter.getPosition();

                                if (parameterIndex >= 0 && parameterIndex < _originalArguments.size()) {
                                    final Expression argument = _originalArguments.get(parameterIndex);

                                    if (parameterIndex == 0 &&
                                        argument instanceof ThisReferenceExpression &&
                                        isLocalOrAnonymous(context.getCurrentType())) {

                                        //
                                        // Don't replace outer class references; they will be rewritten later.
                                        //
                                        return null;
                                    }

                                    final FieldDefinition resolvedTargetField = ((FieldReference) leftMember).resolve();

                                    if (resolvedTargetField != null && !resolvedTargetField.isSynthetic()) {
                                        _parametersToRemove.add(parameter);
                                        _initializers.put(resolvedTargetField.getFullName(), argument);

                                        if (node.getParent() instanceof ExpressionStatement) {
                                            _nodesToRemove.add(node.getParent());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else if (_baseConstructorCalled && right instanceof MemberReferenceExpression) {
                    final MemberReferenceExpression leftMemberReference = (MemberReferenceExpression) left;
                    final MemberReference leftMember = leftMemberReference.getUserData(Keys.MEMBER_REFERENCE);
                    final MemberReferenceExpression rightMemberReference = (MemberReferenceExpression) right;
                    final MemberReference rightMember = right.getUserData(Keys.MEMBER_REFERENCE);

                    if (rightMember instanceof FieldReference &&
                        rightMemberReference.getTarget() instanceof ThisReferenceExpression) {

                        final FieldDefinition resolvedTargetField = ((FieldReference) leftMember).resolve();
                        final FieldDefinition resolvedSourceField = ((FieldReference) rightMember).resolve();

                        if (resolvedSourceField != null &&
                            resolvedTargetField != null &&
                            resolvedSourceField.isSynthetic() &&
                            !resolvedTargetField.isSynthetic()) {

                            final Expression initializer = _replacements.get(rightMember.getFullName());

                            if (initializer != null) {
                                _initializers.put(resolvedTargetField.getFullName(), initializer);

                                if (node.getParent() instanceof ExpressionStatement) {
                                    _nodesToRemove.add(node.getParent());
                                }
                            }
                        }
                    }
                }
            }

            return null;
        }

        private void markConstructorParameterForRemoval(final AssignmentExpression node, final ParameterDefinition parameter) {
            final ConstructorDeclaration constructorDeclaration = node.getParent(ConstructorDeclaration.class);

            if (constructorDeclaration != null) {
                final AstNodeCollection<ParameterDeclaration> parameters = constructorDeclaration.getParameters();

                for (final ParameterDeclaration p : parameters) {
                    if (p.getUserData(Keys.PARAMETER_DEFINITION) == parameter) {
                        _nodesToRemove.add(p);
                        break;
                    }
                }
            }
        }
    }

    private static boolean isLocalOrAnonymous(final TypeDefinition type) {
        return type != null && (type.isLocalClass() || type.isAnonymous());
    }

    private static boolean hasSideEffects(final Expression e) {
        return !(e instanceof IdentifierExpression ||
                 e instanceof PrimitiveExpression ||
                 e instanceof ThisReferenceExpression ||
                 e instanceof SuperReferenceExpression ||
                 e instanceof NullReferenceExpression ||
                 e instanceof ClassOfExpression);
    }

    private final static class ClosureRewriterPhaseTwoVisitor extends ContextTrackingVisitor<Void> {
        private final Map<String, Expression> _replacements;
        private final Map<String, Expression> _initializers;

        protected ClosureRewriterPhaseTwoVisitor(
            final DecompilerContext context,
            final Map<String, Expression> replacements,
            final Map<String, Expression> initializers) {

            super(context);

            _replacements = VerifyArgument.notNull(replacements, "replacements");
            _initializers = VerifyArgument.notNull(initializers, "initializers");
        }

        @Override
        public Void visitFieldDeclaration(final FieldDeclaration node, final Void data) {
            super.visitFieldDeclaration(node, data);

            final FieldDefinition field = node.getUserData(Keys.FIELD_DEFINITION);

            if (field != null &&
                !_initializers.isEmpty() &&
                node.getVariables().size() == 1 &&
                node.getVariables().firstOrNullObject().getInitializer().isNull()) {

                final Expression initializer = _initializers.get(field.getFullName());

                if (initializer != null) {
                    node.getVariables().firstOrNullObject().setInitializer(initializer.clone());
                }
            }

            return null;
        }

        @Override
        public Void visitMemberReferenceExpression(final MemberReferenceExpression node, final Void p) {
            super.visitMemberReferenceExpression(node, p);

            if (node.getParent() instanceof AssignmentExpression &&
                node.getRole() == AssignmentExpression.LEFT_ROLE) {

                return null;
            }

            final MemberReference member = node.getUserData(Keys.MEMBER_REFERENCE);

            if (member instanceof FieldReference) {
                final Expression replacement = _replacements.get(member.getFullName());

                if (replacement != null) {
                    node.replaceWith(replacement.clone());
                }
            }

            return null;
        }
    }

    private static class ThisReferenceReplacingVisitor extends ContextTrackingVisitor<Void> {
        private final TypeDefinition _innerClass;

        public ThisReferenceReplacingVisitor(final DecompilerContext context, final TypeDefinition innerClass) {
            super(context);
            _innerClass = innerClass;
        }

        @Override
        public Void visitMemberReferenceExpression(final MemberReferenceExpression node, final Void data) {
            super.visitMemberReferenceExpression(node, data);

            if (node.getTarget() instanceof ThisReferenceExpression) {
                final ThisReferenceExpression thisReference = (ThisReferenceExpression) node.getTarget();
                final Expression target = thisReference.getTarget();

                if (target == null || target.isNull()) {
                    MemberReference member = node.getUserData(Keys.MEMBER_REFERENCE);

                    if (member == null && node.getParent() instanceof InvocationExpression) {
                        member = node.getParent().getUserData(Keys.MEMBER_REFERENCE);
                    }

                    if (member != null &&
                        MetadataHelper.isEnclosedBy(_innerClass, member.getDeclaringType())) {

                        final AstBuilder astBuilder = context.getUserData(Keys.AST_BUILDER);

                        if (astBuilder != null) {
                            thisReference.setTarget(
                                new TypeReferenceExpression(
                                    thisReference.getOffset(),
                                    astBuilder.convertType(
                                        member.getDeclaringType(),
                                        OUTER_TYPE_CONVERT_OPTIONS
                                    )
                                )
                            );
                        }
                    }
                }
            }

            return null;
        }
    }

    private final static class IntroduceInitializersVisitor extends ContextTrackingVisitor<Void> {
        public IntroduceInitializersVisitor(final DecompilerContext context) {
            super(context);
        }

        @Override
        public Void visitSuperReferenceExpression(final SuperReferenceExpression node, final Void p) {
            super.visitSuperReferenceExpression(node, p);

            if (context.getCurrentMethod() != null &&
                context.getCurrentMethod().isConstructor() &&
                context.getCurrentMethod().getDeclaringType().isAnonymous() &&
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

                final InstanceInitializer initializer = new InstanceInitializer();
                final BlockStatement initializerBody = new BlockStatement();

                for (Statement current = parentStatement.getNextStatement(); current != null; ) {
                    final Statement next = current.getNextStatement();

                    current.remove();
                    initializerBody.addChild(current, current.getRole());
                    current = next;
                }

                initializer.setBody(initializerBody);
                constructor.getParent().insertChildAfter(constructor, initializer, Roles.TYPE_MEMBER);
            }

            return null;
        }
    }
}
