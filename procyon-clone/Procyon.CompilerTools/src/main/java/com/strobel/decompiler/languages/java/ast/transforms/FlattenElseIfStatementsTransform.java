/*
 * FlattenElseIfStatementsTransform.java
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
import com.strobel.decompiler.languages.java.ast.*;

public class FlattenElseIfStatementsTransform extends ContextTrackingVisitor<Void> {
    public FlattenElseIfStatementsTransform(final DecompilerContext context) {
        super(context);
    }

    @Override
    public Void visitIfElseStatement(final IfElseStatement node, final Void data) {
        super.visitIfElseStatement(node, data);

        final Statement trueStatement = node.getTrueStatement();
        final Statement falseStatement = node.getFalseStatement();

        if (trueStatement instanceof BlockStatement &&
            falseStatement instanceof BlockStatement &&
            ((BlockStatement) trueStatement).getStatements().isEmpty() &&
            !((BlockStatement) falseStatement).getStatements().isEmpty()) {

            final Expression condition = node.getCondition();

            condition.remove();
            node.setCondition(new UnaryOperatorExpression(UnaryOperatorType.NOT, condition));
            falseStatement.remove();
            node.setTrueStatement(falseStatement);
            node.setFalseStatement(null);

            return null;
        }

        if (falseStatement instanceof BlockStatement) {
            final BlockStatement falseBlock = (BlockStatement) falseStatement;
            final AstNodeCollection<Statement> falseStatements = falseBlock.getStatements();

            if (falseStatements.hasSingleElement() &&
                falseStatements.firstOrNullObject() instanceof IfElseStatement) {

                final Statement elseIf = falseStatements.firstOrNullObject();

                elseIf.remove();
                falseStatement.replaceWith(elseIf);

                return null;
            }
        }

        return null;
    }
}
