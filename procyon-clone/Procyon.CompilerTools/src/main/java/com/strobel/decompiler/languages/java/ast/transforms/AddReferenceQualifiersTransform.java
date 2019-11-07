/*
 * AddReferenceQualifiersTransform.java
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

import com.strobel.assembler.metadata.FieldReference;
import com.strobel.assembler.metadata.MemberReference;
import com.strobel.assembler.metadata.MetadataHelper;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.StringUtilities;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.strobel.core.CollectionUtilities.first;

public class AddReferenceQualifiersTransform extends ContextTrackingVisitor<Void> {
    private final Set<AstNode> _addQualifierCandidates = new LinkedHashSet<>();
    private final Set<AstNode> _removeQualifierCandidates = new LinkedHashSet<>();
    private final boolean _simplifyMemberReferences;

    public AddReferenceQualifiersTransform(final DecompilerContext context) {
        super(context);
        _simplifyMemberReferences = context.getSettings().getSimplifyMemberReferences();
    }

    @Override
    public void run(final AstNode compilationUnit) {
        super.run(compilationUnit);

        addQualifiersWhereNecessary();
        removeQualifiersWherePossible();
    }

    private void addQualifiersWhereNecessary() {
        for (final AstNode candidate : _addQualifierCandidates) {
            if (candidate instanceof SimpleType) {
                final SimpleType type = (SimpleType) candidate;

                TypeReference referencedType = type.getUserData(Keys.ANONYMOUS_BASE_TYPE_REFERENCE);

                if (referencedType == null) {
                    referencedType = type.getUserData(Keys.TYPE_REFERENCE);
                }

                final String s = qualifyReference(candidate, referencedType);

                if (!StringUtilities.isNullOrEmpty(s)) {
                    type.setIdentifier(s);
                }
            }
        }
    }

    private void removeQualifiersWherePossible() {
        for (final AstNode candidate : _removeQualifierCandidates) {
            if (candidate instanceof MemberReferenceExpression) {
                final FieldReference field = (FieldReference) candidate.getUserData(Keys.MEMBER_REFERENCE);

                if (field != null) {
                    final IdentifierExpression identifier = new IdentifierExpression(((Expression) candidate).getOffset(), field.getName());
                    identifier.copyUserDataFrom(candidate);
                    candidate.replaceWith(identifier);
                }
            }
        }
    }

    private static NameResolveMode modeForType(final AstNode type) {
        if (type != null &&
            type.getParent() instanceof TypeReferenceExpression &&
            ((TypeReferenceExpression) type.getParent()).getType() == type) {

            return NameResolveMode.EXPRESSION;
        }

        return NameResolveMode.TYPE;
    }

    private String qualifyReference(final AstNode node, final TypeReference type) {
        if (type == null || type.isGenericParameter() || type.isWildcardType()) {
            return null;
        }

        final TypeDefinition resolvedType = type.resolve();
        final TypeReference t = resolvedType != null ? resolvedType : (type.isGenericType() ? type.getUnderlyingType() : type);
        final Object resolvedObject = resolveName(node, t.getSimpleName(), modeForType(node));

        if (!context.getSettings().getForceFullyQualifiedReferences() &&
            resolvedObject instanceof TypeReference &&
            MetadataHelper.isSameType(t, (TypeReference) resolvedObject)) {

            return t.getSimpleName();
        }

        if (t.isNested()) {
            final String outerReference = qualifyReference(node, t.getDeclaringType());

            if (outerReference != null) {
                return outerReference + "." + t.getSimpleName();
            }
        }

        if (resolvedObject != null) {
            return t.getFullName();
        }

        return null;
    }

    @Override
    public Void visitSimpleType(final SimpleType node, final Void data) {
        final AstNode parent = node.getParent();

        if (parent instanceof ObjectCreationExpression &&
            !((ObjectCreationExpression) parent).getTarget().isNull()) {

            //
            // Ignore types used in `outer.new Inner()`; we assume the name of the inner class resolves
            // correctly when accessed via inner `.new`.
            //
            return super.visitSimpleType(node, data);
        }

        int i;
        String name = node.getIdentifier();
        TypeReference type = node.getUserData(Keys.TYPE_REFERENCE);

        if (type.isPrimitive() || type.isGenericParameter()) {
            //
            // Ignore primitives and generic parameters; they cannot be qualified.
            //
            return super.visitSimpleType(node, data);
        }

        while (type.isNested() && (i = name.lastIndexOf('.')) > 0 && i < name.length() - 1) {
            type = type.getDeclaringType();
            name = name.substring(0, i);
        }

        final Object resolvedObject = resolveName(node, name, modeForType(node));

        if (context.getSettings().getForceFullyQualifiedReferences() ||
            resolvedObject == null ||
            !(resolvedObject instanceof TypeReference &&
              MetadataHelper.isSameType(type, (TypeReference) resolvedObject))) {

            _addQualifierCandidates.add(node);
        }

        return super.visitSimpleType(node, data);
    }

    @Override
    public Void visitCompilationUnit(final CompilationUnit node, final Void data) {
        super.visitCompilationUnit(node, data);

        final Set<String> topLevelTypeNames = new LinkedHashSet<>();
        final List<ImportDeclaration> importsToRemove = new ArrayList<>();

        for (final AstNode m : node.getChildrenByRole(CompilationUnit.TYPE_ROLE)) {
            if (m instanceof TypeDeclaration) {
                topLevelTypeNames.add(((TypeDeclaration) m).getName());
            }
        }

        for (final ImportDeclaration d : node.getChildrenByRole(CompilationUnit.IMPORT_ROLE)) {
            final TypeReference importedType = d.getUserData(Keys.TYPE_REFERENCE);

            if (importedType != null && topLevelTypeNames.contains(importedType.getSimpleName())) {
                importsToRemove.add(d);
            }
        }

        for (final ImportDeclaration d : importsToRemove) {
            d.remove();
        }

        return null;
    }

    @Override
    public Void visitMemberReferenceExpression(final MemberReferenceExpression node, final Void data) {
        if (_simplifyMemberReferences) {
            final MemberReference member = node.getUserData(Keys.MEMBER_REFERENCE);

            if (member instanceof FieldReference &&
                context.getCurrentType() != null &&
                MetadataHelper.isEnclosedBy(context.getCurrentType(), member.getDeclaringType())) {

                final Object resolvedObject = resolveName(node, member.getName(), NameResolveMode.EXPRESSION);

                if (resolvedObject instanceof FieldReference &&
                    MetadataHelper.isSameType(((FieldReference) resolvedObject).getDeclaringType(), member.getDeclaringType())) {

                    _removeQualifierCandidates.add(node);
                }
            }
        }

        return super.visitMemberReferenceExpression(node, data);
    }

    protected Object resolveName(final AstNode location, final String name, final NameResolveMode mode) {
        if (location == null || location.isNull() || name == null) {
            return null;
        }

        final NameResolveResult result;

        if (mode == NameResolveMode.TYPE) {
            result = JavaNameResolver.resolveAsType(name, location);
        }
        else {
            result = JavaNameResolver.resolve(name, location);
        }

        if (result.hasMatch() && !result.isAmbiguous()) {
            return first(result.getCandidates());
        }

        return null;
    }
}
