/*
 * RewriteNewArrayLambdas.java
 *
 * Copyright (c) 2014 Mike Strobel
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

import com.strobel.assembler.metadata.DynamicCallSite;
import com.strobel.assembler.metadata.IMethodSignature;
import com.strobel.assembler.metadata.JvmType;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.patterns.AnyNode;
import com.strobel.decompiler.patterns.IdentifierExpressionBackReference;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.NamedNode;
import com.strobel.decompiler.patterns.OptionalNode;
import com.strobel.decompiler.patterns.Pattern;

import static com.strobel.core.CollectionUtilities.first;

public class RewriteNewArrayLambdas extends ContextTrackingVisitor<Void> {
    protected RewriteNewArrayLambdas(final DecompilerContext context) {
        super(context);
    }

    @Override
    public Void visitLambdaExpression(final LambdaExpression node, final Void data) {
        super.visitLambdaExpression(node, data);

        final DynamicCallSite callSite = node.getUserData(Keys.DYNAMIC_CALL_SITE);

        if (callSite != null &&
            callSite.getBootstrapArguments().size() >= 3 &&
            callSite.getBootstrapArguments().get(2) instanceof IMethodSignature) {

            final IMethodSignature signature = (IMethodSignature) callSite.getBootstrapArguments().get(2);

            if (signature.getParameters().size() == 1 &&
                signature.getParameters().get(0).getParameterType().getSimpleType() == JvmType.Integer &&
                signature.getReturnType().isArray() &&
                !signature.getReturnType().getElementType().isGenericType()) {

                final LambdaExpression pattern = new LambdaExpression(Expression.MYSTERY_OFFSET);
                final ParameterDeclaration size = new ParameterDeclaration();

                size.setName(Pattern.ANY_STRING);
                size.setAnyModifiers(true);
                size.setType(new OptionalNode(new SimpleType("int")).toType());

                pattern.getParameters().add(new NamedNode("size", size).toParameterDeclaration());

                final ArrayCreationExpression arrayCreation = new ArrayCreationExpression(Expression.MYSTERY_OFFSET);

                arrayCreation.getDimensions().add(new IdentifierExpressionBackReference("size").toExpression());
                arrayCreation.setType(new NamedNode("type", new AnyNode()).toType());

                pattern.setBody(arrayCreation);

                final Match match = pattern.match(node);

                if (match.success()) {
                    final AstType type = first(match.<AstType>get("type"));

                    if (signature.getReturnType().getElementType().isEquivalentTo(type.toTypeReference())) {
                        final MethodGroupExpression replacement = new MethodGroupExpression(
                            node.getOffset(),
                            new TypeReferenceExpression(Expression.MYSTERY_OFFSET, type.clone().makeArrayType()),
                            "new"
                        );

                        final TypeReference lambdaType = node.getUserData(Keys.TYPE_REFERENCE);

                        if (lambdaType != null) {
                            replacement.putUserData(Keys.TYPE_REFERENCE, lambdaType);
                        }

                        replacement.putUserData(Keys.DYNAMIC_CALL_SITE, callSite);

                        node.replaceWith(replacement);
                    }
                }
            }
        }

        return null;
    }
}
