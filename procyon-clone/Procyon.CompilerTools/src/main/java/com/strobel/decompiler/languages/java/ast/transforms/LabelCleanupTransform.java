/*
 * LabelCleanupTransform.java
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

import com.strobel.core.StringUtilities;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;

public class LabelCleanupTransform extends ContextTrackingVisitor<Void> {
    public LabelCleanupTransform(final DecompilerContext context) {
        super(context);
    }

    @Override
    public Void visitLabeledStatement(final LabeledStatement node, final Void data) {
        super.visitLabeledStatement(node, data);

        if (node.getStatement() instanceof BlockStatement) {
            final BlockStatement block = (BlockStatement) node.getStatement();

            if (block.getStatements().hasSingleElement() &&
                block.getStatements().firstOrNullObject() instanceof LabeledStatement) {

                final LabeledStatement nestedLabeledStatement = (LabeledStatement) block.getStatements().firstOrNullObject();

                //
                // We have back-to-back labels; dump the first and redirect its references to the second.
                //

                final String nextLabel = nestedLabeledStatement.getChildByRole(Roles.LABEL).getName();

                redirectLabels(node, node.getLabel(), nextLabel);

                nestedLabeledStatement.remove();
                node.replaceWith(nestedLabeledStatement);
            }
        }

        return null;
    }

    @Override
    public Void visitLabelStatement(final LabelStatement node, final Void data) {
        super.visitLabelStatement(node, data);

        final Statement next = node.getNextStatement();

        if (next == null) {
            return null;
        }

        if (next instanceof LabelStatement ||
            next instanceof LabeledStatement) {

            //
            // We have back-to-back labels; dump the first and redirect its references to the second.
            //

            final String nextLabel = next.getChildByRole(Roles.LABEL).getName();

            redirectLabels(node.getParent(), node.getLabel(), nextLabel);

            node.remove();
        }
        else {
            //
            // Replace LabelStatement with LabeledStatement.
            //

            next.remove();

            node.replaceWith(
                new LabeledStatement(
                    node.getLabel(),
                    AstNode.isLoop(next) ? next : new BlockStatement(next)
                )
            );
        }

        return null;
    }

    private void redirectLabels(final AstNode node, final String labelName, final String nextLabel) {
        for (final AstNode n : node.getDescendantsAndSelf()) {
            if (AstNode.isUnconditionalBranch(n)) {
                final Identifier label = n.getChildByRole(Roles.IDENTIFIER);

                if (!label.isNull() && StringUtilities.equals(label.getName(), labelName)) {
                    label.setName(nextLabel);
                }
            }
        }
    }
}
