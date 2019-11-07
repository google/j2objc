/*
 * RewriteLocalClassesTransform.java
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

import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.AnonymousObjectCreationExpression;
import com.strobel.decompiler.languages.java.ast.AstNode;
import com.strobel.decompiler.languages.java.ast.ContextTrackingVisitor;
import com.strobel.decompiler.languages.java.ast.Keys;
import com.strobel.decompiler.languages.java.ast.LocalClassHelper;
import com.strobel.decompiler.languages.java.ast.ObjectCreationExpression;
import com.strobel.decompiler.languages.java.ast.TypeDeclaration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RewriteLocalClassesTransform extends ContextTrackingVisitor<Void> {
    private final Map<TypeReference, TypeDeclaration> _localTypes = new LinkedHashMap<>();
    private final Map<TypeReference, List<ObjectCreationExpression>> _instantiations = new LinkedHashMap<>();

    public RewriteLocalClassesTransform(final DecompilerContext context) {
        super(context);
    }

    @Override
    public void run(final AstNode compilationUnit) {
        final PhaseOneVisitor phaseOneVisitor = new PhaseOneVisitor(context);

        compilationUnit.acceptVisitor(phaseOneVisitor, null);

        super.run(compilationUnit);

        for (final TypeReference localType : _localTypes.keySet()) {
            final TypeDeclaration declaration = _localTypes.get(localType);
            final List<ObjectCreationExpression> instantiations = _instantiations.get(localType);

            LocalClassHelper.replaceClosureMembers(
                context,
                declaration,
                instantiations != null ? instantiations : Collections.<ObjectCreationExpression>emptyList()
            );
        }
    }

    @Override
    public Void visitObjectCreationExpression(final ObjectCreationExpression node, final Void p) {
        super.visitObjectCreationExpression(node, p);

        final TypeReference type = node.getType().getUserData(Keys.TYPE_REFERENCE);
        final TypeDefinition resolvedType = type != null ? type.resolve() : null;

        if (resolvedType != null &&
            isLocalOrAnonymous(resolvedType) &&
            !resolvedType.isEquivalentTo(context.getCurrentType())) {

            List<ObjectCreationExpression> instantiations = _instantiations.get(type);

            if (instantiations == null) {
                _instantiations.put(type, instantiations = new ArrayList<>());
            }

            instantiations.add(node);
        }

        return null;
    }

    private static boolean isLocalOrAnonymous(final TypeDefinition type) {
        if (type == null) {
            return false;
        }
        return type.isLocalClass() || type.isAnonymous();
    }

    @Override
    public Void visitAnonymousObjectCreationExpression(final AnonymousObjectCreationExpression node, final Void p) {
        super.visitAnonymousObjectCreationExpression(node, p);

        final TypeDefinition resolvedType = node.getTypeDeclaration().getUserData(Keys.TYPE_DEFINITION);

        if (resolvedType != null && isLocalOrAnonymous(resolvedType)) {
            List<ObjectCreationExpression> instantiations = _instantiations.get(resolvedType);

            if (instantiations == null) {
                _instantiations.put(resolvedType, instantiations = new ArrayList<>());
            }

            instantiations.add(node);
        }

        return null;
    }

    private final class PhaseOneVisitor extends ContextTrackingVisitor<Void> {
        protected PhaseOneVisitor(final DecompilerContext context) {
            super(context);
        }

        @Override
        public Void visitTypeDeclaration(final TypeDeclaration typeDeclaration, final Void p) {
            final TypeDefinition type = typeDeclaration.getUserData(Keys.TYPE_DEFINITION);

            if (type != null && (isLocalOrAnonymous(type) || type.isAnonymous())) {
                _localTypes.put(type, typeDeclaration);
            }

            return super.visitTypeDeclaration(typeDeclaration, p);
        }
    }
}
