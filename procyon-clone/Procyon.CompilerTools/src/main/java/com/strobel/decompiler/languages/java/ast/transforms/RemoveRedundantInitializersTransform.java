/*
 * RemoveRedundantInitializersTransform.java
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
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.AssignmentExpression;
import com.strobel.decompiler.languages.java.ast.AstNode;
import com.strobel.decompiler.languages.java.ast.ConstructorDeclaration;
import com.strobel.decompiler.languages.java.ast.ContextTrackingVisitor;
import com.strobel.decompiler.languages.java.ast.Expression;
import com.strobel.decompiler.languages.java.ast.ExpressionStatement;
import com.strobel.decompiler.languages.java.ast.Keys;
import com.strobel.decompiler.languages.java.ast.MemberReferenceExpression;
import com.strobel.decompiler.languages.java.ast.ThisReferenceExpression;

public class RemoveRedundantInitializersTransform extends ContextTrackingVisitor<Void> {
    private boolean _inConstructor;

    public RemoveRedundantInitializersTransform(final DecompilerContext context) {
        super(context);
    }

    @Override
    public Void visitConstructorDeclaration(final ConstructorDeclaration node, final Void p) {
        final boolean wasInConstructor = _inConstructor;

        _inConstructor = true;

        try {
            return super.visitConstructorDeclaration(node, p);
        }
        finally {
            _inConstructor = wasInConstructor;
        }
    }

    @Override
    public Void visitAssignmentExpression(final AssignmentExpression node, final Void data) {
        super.visitAssignmentExpression(node, data);

        if (_inConstructor) {
            final Expression left = node.getLeft();

            if (left instanceof MemberReferenceExpression &&
                ((MemberReferenceExpression) left).getTarget() instanceof ThisReferenceExpression) {

                final MemberReferenceExpression reference = (MemberReferenceExpression) left;
                final MemberReference memberReference = reference.getUserData(Keys.MEMBER_REFERENCE);

                if (memberReference instanceof FieldReference) {
                    final FieldDefinition resolvedField = ((FieldReference) memberReference).resolve();

                    if (resolvedField != null && resolvedField.hasConstantValue()) {
                        final AstNode parent = node.getParent();

                        if (parent instanceof ExpressionStatement) {
                            parent.remove();
                        }
                        else {
                            reference.remove();
                            node.replaceWith(reference);
                        }
                    }
                }
            }
        }

        return null;
    }
}
