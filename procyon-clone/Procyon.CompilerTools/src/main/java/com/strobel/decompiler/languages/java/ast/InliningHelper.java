/*
 * InliningHelper.java
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

import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.ParameterDefinition;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.ast.Variable;

import java.util.Map;

public final class InliningHelper {
    public static AstNode inlineMethod(
        final MethodDeclaration method,
        final Map<ParameterDefinition, ? extends AstNode> argumentMappings) {

        VerifyArgument.notNull(method, "method");
        VerifyArgument.notNull(argumentMappings, "argumentMappings");

        final DecompilerContext context = new DecompilerContext();
        final MethodDefinition definition = method.getUserData(Keys.METHOD_DEFINITION);

        if (definition != null) {
            context.setCurrentType(definition.getDeclaringType());
        }

        final InliningVisitor visitor = new InliningVisitor(context, argumentMappings);

        visitor.run(method);

        return visitor.getInlinedBody();
    }

    private static class InliningVisitor extends ContextTrackingVisitor<Void> {
        private final Map<ParameterDefinition, ? extends AstNode> _argumentMappings;

        private AstNode _result;

        public InliningVisitor(
            final DecompilerContext context,
            final Map<ParameterDefinition, ? extends AstNode> argumentMappings) {

            super(context);
            _argumentMappings = VerifyArgument.notNull(argumentMappings, "argumentMappings");
        }

        public final AstNode getInlinedBody() {
            return _result;
        }

        public void run(final AstNode root) {
            if (!(root instanceof MethodDeclaration)) {
                throw new IllegalArgumentException("InliningVisitor must be run against a MethodDeclaration.");
            }

            final MethodDeclaration clone = (MethodDeclaration) root.clone();

            super.run(clone);

            final BlockStatement body = clone.getBody();
            final AstNodeCollection<Statement> statements = body.getStatements();

            if (statements.size() == 1) {
                final Statement firstStatement = statements.firstOrNullObject();

                if (firstStatement instanceof ExpressionStatement ||
                    firstStatement instanceof ReturnStatement) {

                    _result = firstStatement.getChildByRole(Roles.EXPRESSION);
                    _result.remove();

                    return;
                }
            }

            _result = body;
            _result.remove();
        }

        @Override
        public Void visitIdentifierExpression(final IdentifierExpression node, final Void p) {
            final Variable variable = node.getUserData(Keys.VARIABLE);

            if (variable != null && variable.isParameter()) {
                final ParameterDefinition parameter = variable.getOriginalParameter();

                assert parameter != null;

                if (areMethodsEquivalent((MethodReference) parameter.getMethod(), context.getCurrentMethod())) {
                    final AstNode replacement = _argumentMappings.get(parameter);

                    if (replacement != null) {
                        node.replaceWith(replacement.clone());
                        return null;
                    }
                }
            }

            return super.visitIdentifierExpression(node, p);
        }

        private boolean areMethodsEquivalent(final MethodReference m1, final MethodDefinition m2) {
            if (m1 == m2) {
                return true;
            }

            //noinspection SimplifiableIfStatement
            if (m1 == null || m2 == null) {
                return false;
            }

            return StringUtilities.equals(m1.getFullName(), m2.getFullName()) &&
                   StringUtilities.equals(m1.getErasedSignature(), m2.getErasedSignature());
        }
    }
}
