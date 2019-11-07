/*
 * FlattenSwitchBlocksTransform.java
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
import com.strobel.decompiler.languages.java.ast.AstNode;
import com.strobel.decompiler.languages.java.ast.BlockStatement;
import com.strobel.decompiler.languages.java.ast.ContextTrackingVisitor;
import com.strobel.decompiler.languages.java.ast.Statement;
import com.strobel.decompiler.languages.java.ast.SwitchSection;
import com.strobel.decompiler.languages.java.ast.VariableDeclarationStatement;

import static com.strobel.core.CollectionUtilities.any;
import static com.strobel.core.CollectionUtilities.ofType;

public class FlattenSwitchBlocksTransform extends ContextTrackingVisitor<AstNode> implements IAstTransform {
    public FlattenSwitchBlocksTransform(final DecompilerContext context) {
        super(context);
    }

    @Override
    public void run(final AstNode compilationUnit) {
        if (context.getSettings().getFlattenSwitchBlocks()) {
            compilationUnit.acceptVisitor(this, null);
        }
    }

    @Override
    public AstNode visitSwitchSection(final SwitchSection node, final Void p) {
        if (node.getStatements().size() != 1) {
            return super.visitSwitchSection(node, p);
        }

        final Statement firstStatement = node.getStatements().firstOrNullObject();

        if (firstStatement instanceof BlockStatement) {
            final BlockStatement block = (BlockStatement) firstStatement;

            if (any(ofType(block.getStatements(), VariableDeclarationStatement.class))) {
                return super.visitSwitchSection(node, p);
            }

            block.remove();
            block.getStatements().moveTo(node.getStatements());
        }

        return super.visitSwitchSection(node, p);
    }
}
