/*
 * LambdaTransform.java
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
import com.strobel.decompiler.languages.java.ast.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LambdaTransform extends ContextTrackingVisitor<Void> {
    private final Map<String, MethodDeclaration> _methodDeclarations;

    public LambdaTransform(final DecompilerContext context) {
        super(context);
        _methodDeclarations = new HashMap<>();
    }

    @Override
    public void run(final AstNode compilationUnit) {
        compilationUnit.acceptVisitor(
            new ContextTrackingVisitor<Void>(context) {
                @Override
                public Void visitMethodDeclaration(final MethodDeclaration node, final Void p) {
                    final MemberReference methodReference = node.getUserData(Keys.MEMBER_REFERENCE);

                    if (methodReference instanceof MethodReference) {
                        _methodDeclarations.put(makeMethodKey((MethodReference) methodReference), node);
                    }

                    return super.visitMethodDeclaration(node, p);
                }
            },
            null
        );

        super.run(compilationUnit);
    }

    @Override
    public Void visitMethodGroupExpression(final MethodGroupExpression node, final Void data) {
        final MemberReference reference = node.getUserData(Keys.MEMBER_REFERENCE);

        if (reference instanceof MethodReference) {
            final MethodReference method = (MethodReference) reference;
            final MethodDefinition resolvedMethod = method.resolve();
            final DynamicCallSite callSite = node.getUserData(Keys.DYNAMIC_CALL_SITE);

            if (resolvedMethod != null && resolvedMethod.isSynthetic() && callSite != null) {
                inlineLambda(node, resolvedMethod);
                return null;
            }
        }

        return super.visitMethodGroupExpression(node, data);
    }

    @SuppressWarnings("ConstantConditions")
    private void inlineLambda(final MethodGroupExpression methodGroup, final MethodDefinition method) {
        final MethodDeclaration declaration = _methodDeclarations.get(makeMethodKey(method));

        if (declaration == null) {
            return;
        }

        final BlockStatement body = (BlockStatement) declaration.getBody().clone();
        final AstNodeCollection<ParameterDeclaration> parameters = declaration.getParameters();
        final Map<String, IdentifierExpression> renamedVariables = new HashMap<>();
        final AstNodeCollection<Expression> closureArguments = methodGroup.getClosureArguments();
        final Statement firstStatement = body.getStatements().firstOrNullObject();

        final int offset;

        if (firstStatement != null && !firstStatement.isNull()) {
            offset = firstStatement.getOffset();
        }
        else {
            offset = Expression.MYSTERY_OFFSET;
        }

        Expression a = closureArguments.firstOrNullObject();

        for (ParameterDeclaration p = parameters.firstOrNullObject();
             p != null && !p.isNull() && a != null && !a.isNull();
             p = (ParameterDeclaration) p.getNextSibling(p.getRole()), a = (Expression) a.getNextSibling(a.getRole())) {

            if (a instanceof IdentifierExpression) {
                renamedVariables.put(p.getName(), (IdentifierExpression) a);
            }
        }

        body.acceptVisitor(
            new ContextTrackingVisitor<Void>(context) {
                @Override
                public Void visitIdentifier(final Identifier node, final Void p) {
                    final String oldName = node.getName();

                    if (oldName != null) {
                        final IdentifierExpression newName = renamedVariables.get(oldName);

                        if (newName != null && newName.getIdentifier() != null) {
                            node.setName(newName.getIdentifier());
                        }
                    }

                    return super.visitIdentifier(node, p);
                }

                @Override
                public Void visitIdentifierExpression(final IdentifierExpression node, final Void p) {
                    final String oldName = node.getIdentifier();

                    if (oldName != null) {
                        final IdentifierExpression newName = renamedVariables.get(oldName);

                        if (newName != null) {
                            node.replaceWith(newName.clone());
                            return null;
                        }
                    }

                    return super.visitIdentifierExpression(node, p);
                }
            },
            null
        );

        final LambdaExpression lambda = new LambdaExpression(offset);
        final DynamicCallSite callSite = methodGroup.getUserData(Keys.DYNAMIC_CALL_SITE);

        TypeReference lambdaType = methodGroup.getUserData(Keys.TYPE_REFERENCE);

        if (callSite != null) {
            lambda.putUserData(Keys.DYNAMIC_CALL_SITE, callSite);
        }

        if (lambdaType != null) {
            lambda.putUserData(Keys.TYPE_REFERENCE, lambdaType);
        }
        else if (callSite != null) {
            lambdaType = callSite.getMethodType().getReturnType();
        }
        else {
            return;
        }

        body.remove();

        if (body.getStatements().size() == 1 &&
            (firstStatement instanceof ExpressionStatement || firstStatement instanceof ReturnStatement)) {

            final Expression simpleBody = firstStatement.getChildByRole(Roles.EXPRESSION);

            simpleBody.remove();
            lambda.setBody(simpleBody);
        }
        else {
            lambda.setBody(body);
        }

        int parameterCount = 0;
        int parametersToSkip = closureArguments.size();

        for (final ParameterDeclaration p : declaration.getParameters()) {
            if (parametersToSkip-- > 0) {
                continue;
            }

            final ParameterDeclaration lambdaParameter = (ParameterDeclaration) p.clone();

            lambdaParameter.setType(AstType.NULL);
            lambda.addChild(lambdaParameter, Roles.PARAMETER);

            ++parameterCount;
        }

        if (!MetadataHelper.isRawType(lambdaType)) {
            final TypeDefinition resolvedType = lambdaType.resolve();

            if (resolvedType != null) {
                MethodReference functionMethod = null;

                final List<MethodReference> methods = MetadataHelper.findMethods(
                    resolvedType,
                    callSite != null ? MetadataFilters.matchName(callSite.getMethodName())
                                     : Predicates.<MemberReference>alwaysTrue()
                );

                for (final MethodReference m : methods) {
                    final MethodDefinition r = m.resolve();

                    if (r != null && r.isAbstract() && !r.isStatic() && !r.isDefault()) {
                        functionMethod = r;
                        break;
                    }
                }

                if (functionMethod != null &&
                    functionMethod.containsGenericParameters() &&
                    functionMethod.getParameters().size() == parameterCount) {

                    final TypeReference asMemberOf = MetadataHelper.asSuper(functionMethod.getDeclaringType(), lambdaType);

                    if (asMemberOf != null && !MetadataHelper.isRawType(asMemberOf)) {
                        functionMethod = MetadataHelper.asMemberOf(
                            functionMethod,
                            MetadataHelper.isRawType(asMemberOf) ? MetadataHelper.erase(asMemberOf)
                                                                 : asMemberOf
                        );

                        lambda.putUserData(Keys.MEMBER_REFERENCE, functionMethod);

                        if (functionMethod != null) {
                            int i;
                            ParameterDeclaration p;

                            final List<ParameterDefinition> fp = functionMethod.getParameters();

                            for (i = 0, p = lambda.getParameters().firstOrNullObject();
                                 i < parameterCount;
                                 i++, p = p.getNextSibling(Roles.PARAMETER)) {

                                p.putUserData(Keys.PARAMETER_DEFINITION, fp.get(i));
                            }
                        }
                    }
                }
            }
        }

        methodGroup.replaceWith(lambda);
        lambda.acceptVisitor(this, null);
    }

    private static String makeMethodKey(final MethodReference method) {
        return method.getFullName() + ":" + method.getErasedSignature();
    }
}
