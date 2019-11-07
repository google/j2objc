/*
 * IntroduceOuterClassReferencesTransform.java
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

import com.strobel.assembler.metadata.*;
import com.strobel.core.Predicates;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.ast.Variable;
import com.strobel.decompiler.languages.java.ast.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.strobel.core.CollectionUtilities.*;

public class IntroduceOuterClassReferencesTransform extends ContextTrackingVisitor<Void> {
    private final List<AstNode> _nodesToRemove;
    private final Set<String> _outerClassFields;
    private final Set<ParameterReference> _parametersToRemove;

    public IntroduceOuterClassReferencesTransform(final DecompilerContext context) {
        super(context);

        _nodesToRemove = new ArrayList<>();
        _parametersToRemove = new HashSet<>();
        _outerClassFields = new HashSet<>();
    }

    @Override
    public void run(final AstNode compilationUnit) {
        //
        // First run through and locate any outer class member access$ methods.
        //
        new PhaseOneVisitor().run(compilationUnit);

        super.run(compilationUnit);

        for (final AstNode node : _nodesToRemove) {
            node.remove();
        }
    }

    @Override
    public Void visitInvocationExpression(final InvocationExpression node, final Void data) {
        super.visitInvocationExpression(node, data);

        final Expression target = node.getTarget();
        final AstNodeCollection<Expression> arguments = node.getArguments();

        if (target instanceof MemberReferenceExpression && arguments.size() == 1) {
            final MemberReferenceExpression memberReference = (MemberReferenceExpression) target;

            MemberReference reference = memberReference.getUserData(Keys.MEMBER_REFERENCE);

            if (reference == null) {
                reference = node.getUserData(Keys.MEMBER_REFERENCE);
            }

            if (reference instanceof MethodReference) {
                final MethodReference method = (MethodReference) reference;

                if (method.isConstructor()) {
                    final MethodDefinition resolvedMethod = method.resolve();

                    if (resolvedMethod != null) {
                        final TypeDefinition declaringType = resolvedMethod.getDeclaringType();

                        if (declaringType.isInnerClass() || declaringType.isLocalClass()) {
                            for (final ParameterDefinition p : resolvedMethod.getParameters()) {
                                if (_parametersToRemove.contains(p)) {
                                    final int parameterIndex = p.getPosition();
                                    final Expression argumentToRemove = getOrDefault(arguments, parameterIndex);

                                    if (argumentToRemove != null) {
                                        _nodesToRemove.add(argumentToRemove);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Void visitMemberReferenceExpression(final MemberReferenceExpression node, final Void data) {
        tryIntroduceOuterClassReference(node, node.getTarget() instanceof ThisReferenceExpression);
        return super.visitMemberReferenceExpression(node, data);
    }

    private boolean tryIntroduceOuterClassReference(final MemberReferenceExpression node, final boolean hasThisOnLeft) {
        final TypeDefinition currentType = context.getCurrentType();

        if (!currentType.isInnerClass()) {
            return false;
        }

        final MemberReference reference = node.getUserData(Keys.MEMBER_REFERENCE);

        final FieldReference field;
        final FieldDefinition resolvedField;

        if (reference instanceof FieldReference) {
            field = (FieldReference) reference;
            resolvedField = field.resolve();
        }
        else {
            field = null;
            resolvedField = null;
        }

        if (resolvedField != null && !_outerClassFields.contains(resolvedField.getFullName())) {
            return false;
        }

        if (!hasThisOnLeft ||
            currentType.isStatic() ||
            node.getParent() instanceof AssignmentExpression && node.getRole() == AssignmentExpression.LEFT_ROLE ||
            resolvedField == null ||
            !resolvedField.isSynthetic()) {

            return tryInsertOuterClassReference(node, reference);
        }

        if (node.getParent() instanceof MemberReferenceExpression &&
            tryIntroduceOuterClassReference((MemberReferenceExpression) node.getParent(), true)) {

            return true;
        }

        final SimpleType outerType;

        final TypeReference outerTypeReference = field.getFieldType();
        final TypeDefinition resolvedOuterType = outerTypeReference.resolve();

        if (resolvedOuterType != null && resolvedOuterType.isAnonymous()) {
            if (resolvedOuterType.getExplicitInterfaces().isEmpty()) {
                outerType = new SimpleType(resolvedOuterType.getBaseType().getSimpleName());
                outerType.putUserData(Keys.ANONYMOUS_BASE_TYPE_REFERENCE, resolvedOuterType.getBaseType());
            }
            else {
                outerType = new SimpleType(resolvedOuterType.getExplicitInterfaces().get(0).getSimpleName());
                outerType.putUserData(Keys.ANONYMOUS_BASE_TYPE_REFERENCE, resolvedOuterType.getExplicitInterfaces().get(0));
            }
        }
        else {
            if (resolvedOuterType != null) {
                outerType = new SimpleType(resolvedOuterType.getSimpleName());
            }
            else {
                outerType = new SimpleType(outerTypeReference.getSimpleName());
            }
        }

        outerType.putUserData(Keys.TYPE_REFERENCE, outerTypeReference);

        final ThisReferenceExpression replacement = new ThisReferenceExpression(node.getOffset());

        replacement.setTarget(new TypeReferenceExpression(node.getOffset(), outerType));
        replacement.putUserData(Keys.TYPE_REFERENCE, outerTypeReference);

        node.replaceWith(replacement);

        return true;
    }

    @Override
    public Void visitIdentifierExpression(final IdentifierExpression node, final Void data) {
        final Variable variable = node.getUserData(Keys.VARIABLE);

        if (variable != null &&
            variable.isParameter() &&
            _parametersToRemove.contains(variable.getOriginalParameter())) {

            final ParameterDefinition parameter = variable.getOriginalParameter();

            assert parameter != null;

            final TypeReference parameterType = parameter.getParameterType();

            if (!MetadataResolver.areEquivalent(context.getCurrentType(), parameterType) &&
                isContextWithinTypeInstance(parameterType)) {

                final TypeDefinition resolvedType = parameterType.resolve();
                final TypeReference declaredType;

                if (resolvedType != null && resolvedType.isAnonymous()) {
                    if (resolvedType.getExplicitInterfaces().isEmpty()) {
                        declaredType = resolvedType.getBaseType();
                    }
                    else {
                        declaredType = resolvedType.getExplicitInterfaces().get(0);
                    }
                }
                else {
                    declaredType = parameterType;
                }

                final SimpleType outerType = new SimpleType(declaredType.getSimpleName());

                outerType.putUserData(Keys.TYPE_REFERENCE, declaredType);

                final ThisReferenceExpression thisReference = new ThisReferenceExpression(node.getOffset());

                thisReference.setTarget(new TypeReferenceExpression(node.getOffset(), outerType));
                node.replaceWith(thisReference);

                return null;
            }
        }

        return super.visitIdentifierExpression(node, data);
    }

    private boolean tryInsertOuterClassReference(final MemberReferenceExpression node, final MemberReference reference) {
        if (node == null || reference == null) {
            return false;
        }

        if (!(node.getTarget() instanceof ThisReferenceExpression)) {
            return false;
        }
        else if (!node.getChildByRole(Roles.TARGET_EXPRESSION).isNull()) {
            return false;
        }

        final TypeReference declaringType = reference.getDeclaringType();

        if (MetadataResolver.areEquivalent(context.getCurrentType(), declaringType) ||
            !isContextWithinTypeInstance(declaringType)) {

            return false;
        }

        final TypeDefinition resolvedType = declaringType.resolve();
        final TypeReference declaredType;

        if (resolvedType != null && resolvedType.isAnonymous()) {
            if (resolvedType.getExplicitInterfaces().isEmpty()) {
                declaredType = resolvedType.getBaseType();
            }
            else {
                declaredType = resolvedType.getExplicitInterfaces().get(0);
            }
        }
        else {
            declaredType = declaringType;
        }

        final SimpleType outerType = new SimpleType(declaredType.getSimpleName());

        outerType.putUserData(Keys.TYPE_REFERENCE, declaredType);

        final ThisReferenceExpression thisReference;

        if (node.getTarget() instanceof ThisReferenceExpression) {
            thisReference = (ThisReferenceExpression) node.getTarget();
            thisReference.setTarget(new TypeReferenceExpression(node.getOffset(), outerType));
        }

        return true;
    }

    // <editor-fold defaultstate="collapsed" desc="PhaseOneVisitor Class">

    private class PhaseOneVisitor extends ContextTrackingVisitor<Void> {
        private PhaseOneVisitor() {
            super(IntroduceOuterClassReferencesTransform.this.context);
        }

        @Override
        public Void visitAssignmentExpression(final AssignmentExpression node, final Void p) {
            super.visitAssignmentExpression(node, p);

            final TypeDefinition currentType = context.getCurrentType();

            if (context.getSettings().getShowSyntheticMembers() ||
                context.getCurrentMethod() == null ||
                !context.getCurrentMethod().isConstructor() ||
                !currentType.isInnerClass() && !currentType.isLocalClass()) {

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

                        if (resolvedField != null &&
                            resolvedField.isSynthetic() &&
                            MetadataResolver.areEquivalent(resolvedField.getFieldType(), currentType.getDeclaringType())) {

                            final ParameterDefinition parameter = variable.getOriginalParameter();

                            assert parameter != null;

                            _outerClassFields.add(resolvedField.getFullName());
                            _parametersToRemove.add(parameter);

                            final ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) firstOrDefault(
                                node.getAncestorsAndSelf(),
                                Predicates.<AstNode>instanceOf(ConstructorDeclaration.class)
                            );

                            if (constructorDeclaration != null && !constructorDeclaration.isNull()) {
                                final ParameterDeclaration parameterToRemove = getOrDefault(
                                    constructorDeclaration.getParameters(),
                                    parameter.getPosition()
                                );

                                if (parameterToRemove != null) {
                                    _nodesToRemove.add(parameterToRemove);
                                }
                            }

                            if (node.getParent() instanceof ExpressionStatement) {
                                _nodesToRemove.add(node.getParent());
                            }
                            else {
                                final TypeReference fieldType = resolvedField.getFieldType();
                                final ThisReferenceExpression replacement = new ThisReferenceExpression(left.getOffset());
                                final SimpleType type = new SimpleType(fieldType.getSimpleName());

                                type.putUserData(Keys.TYPE_REFERENCE, fieldType);
                                replacement.putUserData(Keys.TYPE_REFERENCE, fieldType);
                                replacement.setTarget(new TypeReferenceExpression(left.getOffset(), type));
                                right.replaceWith(replacement);
                            }
                        }
                    }
                }
            }

            return null;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Helper Methods">

    private boolean isContextWithinTypeInstance(final TypeReference type) {
        final MethodReference method = context.getCurrentMethod();

        if (method != null) {
            final MethodDefinition resolvedMethod = method.resolve();

            if (resolvedMethod != null && resolvedMethod.isStatic()) {
                return false;
            }
        }

        final TypeReference scope = context.getCurrentType();

        for (TypeReference current = scope;
             current != null;
             current = current.getDeclaringType()) {

            if (MetadataResolver.areEquivalent(current, type)) {
                return true;
            }

            final TypeDefinition resolved = current.resolve();

            if (resolved != null && resolved.isLocalClass()) {
                final MethodReference declaringMethod = resolved.getDeclaringMethod();

                if (declaringMethod != null) {
                    final MethodDefinition resolvedDeclaringMethod = declaringMethod.resolve();

                    if (resolvedDeclaringMethod != null && resolvedDeclaringMethod.isStatic()) {
                        break;
                    }
                }
            }
        }

        return false;
    }

    // </editor-fold>
}
