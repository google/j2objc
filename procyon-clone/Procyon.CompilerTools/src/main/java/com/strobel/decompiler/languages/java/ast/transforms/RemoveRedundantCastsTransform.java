/*
 * RemoveRedundantCastsTransform.java
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

import com.strobel.annotations.Nullable;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.AstNode;
import com.strobel.decompiler.languages.java.ast.CastExpression;
import com.strobel.decompiler.languages.java.ast.ContextTrackingVisitor;
import com.strobel.decompiler.languages.java.ast.JavaResolver;
import com.strobel.decompiler.languages.java.ast.ParenthesizedExpression;
import com.strobel.decompiler.languages.java.utilities.RedundantCastUtility;

import java.util.List;

public class RemoveRedundantCastsTransform extends ContextTrackingVisitor<Void> {
    private final JavaResolver _resolver;

    public RemoveRedundantCastsTransform(final DecompilerContext context) {
        super(context);
        _resolver = new JavaResolver(context);
    }

    @Override
    public void run(final AstNode compilationUnit) {
        if (context.getSettings().getRetainRedundantCasts()) {
            return;
        }
        super.run(compilationUnit);
    }

    @Override
    public Void visitCastExpression(final CastExpression node, final Void data) {
        super.visitCastExpression(node, data);

        final List<CastExpression> redundantCasts = RedundantCastUtility.getRedundantCastsInside(
            _resolver,
            skipParenthesesUp(node.getParent())
        );

        if (redundantCasts.contains(node)) {
            RedundantCastUtility.removeCast(node);
        }

        return null;
    }

    @Nullable
    private static AstNode skipParenthesesUp(final AstNode e) {
        AstNode result = e;

        while (result instanceof ParenthesizedExpression) {
            result = result.getParent();
        }

        return result;
    }
}
