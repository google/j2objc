/*
 * RewriteInnerClassConstructorCalls.java
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

import com.strobel.assembler.metadata.MetadataResolver;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.semantics.ResolveResult;

import static com.strobel.assembler.metadata.MetadataHelper.isEnclosedBy;

public class RewriteInnerClassConstructorCalls extends ContextTrackingVisitor<Void> {
    private final JavaResolver _resolver;

    public RewriteInnerClassConstructorCalls(final DecompilerContext context) {
        super(context);
        _resolver = new JavaResolver(context);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Void visitObjectCreationExpression(final ObjectCreationExpression node, final Void data) {
        super.visitObjectCreationExpression(node, data);

        final AstNodeCollection<Expression> arguments = node.getArguments();

        if (!arguments.isEmpty()) {
            final Expression firstArgument = arguments.firstOrNullObject();
            final ResolveResult resolvedArgument = _resolver.apply(firstArgument);

            if (resolvedArgument != null) {
                final TypeReference createdType = node.getType().getUserData(Keys.TYPE_REFERENCE);
                final TypeReference argumentType = resolvedArgument.getType();

                if (createdType != null && argumentType != null) {
                    final TypeDefinition resolvedCreatedType = createdType.resolve();

                    if (resolvedCreatedType != null &&
                        resolvedCreatedType.isInnerClass() &&
                        !resolvedCreatedType.isStatic() &&
                        isEnclosedBy(resolvedCreatedType, argumentType)) {

                        if (isContextWithinTypeInstance(argumentType) &&
                            firstArgument instanceof ThisReferenceExpression) {

                            final MethodReference constructor = (MethodReference) node.getUserData(Keys.MEMBER_REFERENCE);

                            if (constructor != null &&
                                arguments.size() == constructor.getParameters().size()) {

                                firstArgument.remove();
                            }
                        }
                        else {
                            firstArgument.remove();
                            node.setTarget(firstArgument);

                            final SimpleType type = new SimpleType(resolvedCreatedType.getSimpleName());

                            type.putUserData(Keys.TYPE_REFERENCE, resolvedCreatedType);
                            node.getType().replaceWith(type);
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public Void visitSuperReferenceExpression(final SuperReferenceExpression node, final Void data) {
        super.visitSuperReferenceExpression(node, data);

        if (node.getParent() instanceof InvocationExpression) {
            final InvocationExpression parent = (InvocationExpression) node.getParent();

            if (!parent.getArguments().isEmpty()) {
                final Expression firstArgument = parent.getArguments().firstOrNullObject();
                final ResolveResult resolvedArgument = _resolver.apply(firstArgument);

                if (resolvedArgument != null) {
                    final TypeReference superType = node.getUserData(Keys.TYPE_REFERENCE);
                    final TypeReference argumentType = resolvedArgument.getType();

                    if (superType != null && argumentType != null) {
                        final TypeDefinition resolvedSuperType = superType.resolve();

                        if (resolvedSuperType != null &&
                            resolvedSuperType.isInnerClass() &&
                            !resolvedSuperType.isStatic() &&
                            isEnclosedBy(context.getCurrentType(), argumentType)) {

                            firstArgument.remove();

                            if (!(firstArgument instanceof ThisReferenceExpression)) {
                                node.setTarget(firstArgument);
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

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
}
