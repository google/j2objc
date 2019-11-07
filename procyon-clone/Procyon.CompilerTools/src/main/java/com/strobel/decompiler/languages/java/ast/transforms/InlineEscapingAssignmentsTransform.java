/*
 * InlineEscapingAssignmentsTransform.java
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

import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.ast.Variable;
import com.strobel.decompiler.languages.java.ast.*;

import static com.strobel.decompiler.patterns.Pattern.matchString;

public class InlineEscapingAssignmentsTransform extends ContextTrackingVisitor<Void> {
    public InlineEscapingAssignmentsTransform(final DecompilerContext context) {
        super(context);
    }

    @Override
    public Void visitReturnStatement(final ReturnStatement node, final Void data) {
        super.visitReturnStatement(node, data);

        tryInlineValue(node.getPreviousStatement(), node.getExpression());

        return null;
    }

    @Override
    public Void visitThrowStatement(final ThrowStatement node, final Void data) {
        super.visitThrowStatement(node, data);

        tryInlineValue(node.getPreviousStatement(), node.getExpression());

        return null;
    }

    private void tryInlineValue(final Statement previous, final Expression value) {
        if (!(previous instanceof VariableDeclarationStatement) || value == null || value.isNull()) {
            return;
        }

        final VariableDeclarationStatement d = (VariableDeclarationStatement) previous;
        final AstNodeCollection<VariableInitializer> variables = d.getVariables();
        final VariableInitializer initializer = variables.firstOrNullObject();

        final Variable variable = initializer.getUserData(Keys.VARIABLE);

        if (variable != null &&
            variable.getOriginalVariable() != null &&
            variable.getOriginalVariable().isFromMetadata()) {

            return;
        }

        if (variables.hasSingleElement() &&
            value instanceof IdentifierExpression &&
            matchString(initializer.getName(), ((IdentifierExpression) value).getIdentifier())) {

            final Expression assignedValue = initializer.getInitializer();

            previous.remove();
            assignedValue.remove();
            value.replaceWith(assignedValue);
        }
    }
}
