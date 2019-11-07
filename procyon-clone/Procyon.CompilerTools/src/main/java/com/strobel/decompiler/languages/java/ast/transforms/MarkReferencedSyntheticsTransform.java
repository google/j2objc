/*
 * MarkReferencedSyntheticsTransform.java
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

import com.strobel.assembler.metadata.FieldReference;
import com.strobel.assembler.metadata.Flags;
import com.strobel.assembler.metadata.IMemberDefinition;
import com.strobel.assembler.metadata.MemberReference;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.AstBuilder;
import com.strobel.decompiler.languages.java.ast.ContextTrackingVisitor;
import com.strobel.decompiler.languages.java.ast.Keys;
import com.strobel.decompiler.languages.java.ast.MemberReferenceExpression;

public class MarkReferencedSyntheticsTransform extends ContextTrackingVisitor<Void> {
    public MarkReferencedSyntheticsTransform(final DecompilerContext context) {
        super(context);
    }

    @Override
    public Void visitMemberReferenceExpression(final MemberReferenceExpression node, final Void data) {
        super.visitMemberReferenceExpression(node, data);

        if (isCurrentMemberVisible()) {
            MemberReference member = node.getUserData(Keys.MEMBER_REFERENCE);

            if (member == null && node.getParent() != null) {
                member = node.getParent().getUserData(Keys.MEMBER_REFERENCE);
            }

            if (member != null) {
                final IMemberDefinition resolvedMember;

                if (member instanceof FieldReference) {
                    resolvedMember = ((FieldReference) member).resolve();
                }
                else {
                    resolvedMember = ((MethodReference) member).resolve();
                }

                if (resolvedMember != null &&
                    resolvedMember.isSynthetic() &&
                    !Flags.testAny(resolvedMember.getFlags(), Flags.BRIDGE)) {

                    context.getForcedVisibleMembers().add(resolvedMember);
                }
            }
        }

        return null;
    }

    private boolean isCurrentMemberVisible() {
        final MethodDefinition currentMethod = context.getCurrentMethod();

        if (currentMethod != null && AstBuilder.isMemberHidden(currentMethod, context)) {
            return false;
        }

        final TypeDefinition currentType = context.getCurrentType();

        if (currentType != null && AstBuilder.isMemberHidden(currentType, context)) {
            return false;
        }

        return true;
    }
}
