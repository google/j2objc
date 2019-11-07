/*
 * EnumSwitchRewriterTransform.java
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

import com.strobel.assembler.metadata.BuiltinTypes;
import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.SafeCloseable;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EnumSwitchRewriterTransform implements IAstTransform {
    private final DecompilerContext _context;

    public EnumSwitchRewriterTransform(final DecompilerContext context) {
        _context = VerifyArgument.notNull(context, "context");
    }

    @Override
    public void run(final AstNode compilationUnit) {
        compilationUnit.acceptVisitor(new Visitor(_context), null);
    }

    private final static class Visitor extends ContextTrackingVisitor<Void> {
        private final static class SwitchMapInfo {
            final String enclosingType;
            final Map<String, List<SwitchStatement>> switches = new LinkedHashMap<>();
            final Map<String, Map<Integer, Expression>> mappings = new LinkedHashMap<>();

            TypeDeclaration enclosingTypeDeclaration;

            SwitchMapInfo(final String enclosingType) {
                this.enclosingType = enclosingType;
            }
        }

        private final Map<String, SwitchMapInfo> _switchMaps = new LinkedHashMap<>();
        private boolean _isSwitchMapWrapper;

        protected Visitor(final DecompilerContext context) {
            super(context);
        }

        @Override
        public Void visitTypeDeclaration(final TypeDeclaration typeDeclaration, final Void p) {
            final boolean oldIsSwitchMapWrapper = _isSwitchMapWrapper;
            final TypeDefinition typeDefinition = typeDeclaration.getUserData(Keys.TYPE_DEFINITION);
            final boolean isSwitchMapWrapper = isSwitchMapWrapper(typeDefinition);

            if (isSwitchMapWrapper) {
                final String internalName = typeDefinition.getInternalName();

                SwitchMapInfo info = _switchMaps.get(internalName);

                if (info == null) {
                    _switchMaps.put(internalName, info = new SwitchMapInfo(internalName));
                }

                info.enclosingTypeDeclaration = typeDeclaration;
            }

            _isSwitchMapWrapper = isSwitchMapWrapper;

            try {
                super.visitTypeDeclaration(typeDeclaration, p);
            }
            finally {
                _isSwitchMapWrapper = oldIsSwitchMapWrapper;
            }

            rewrite();

            return null;
        }

        @Override
        public Void visitSwitchStatement(final SwitchStatement node, final Void data) {
            final Expression test = node.getExpression();

            if (test instanceof IndexerExpression) {
                final IndexerExpression indexer = (IndexerExpression) test;
                final Expression array = indexer.getTarget();
                final Expression argument = indexer.getArgument();

                if (!(array instanceof MemberReferenceExpression)) {
                    return super.visitSwitchStatement(node, data);
                }

                final MemberReferenceExpression arrayAccess = (MemberReferenceExpression) array;
                final Expression arrayOwner = arrayAccess.getTarget();
                final String mapName = arrayAccess.getMemberName();

                if (mapName == null || !mapName.startsWith("$SwitchMap$") || !(arrayOwner instanceof TypeReferenceExpression)) {
                    return super.visitSwitchStatement(node, data);
                }

                final TypeReferenceExpression enclosingTypeExpression = (TypeReferenceExpression) arrayOwner;
                final TypeReference enclosingType = enclosingTypeExpression.getType().getUserData(Keys.TYPE_REFERENCE);

                if (!isSwitchMapWrapper(enclosingType) || !(argument instanceof InvocationExpression)) {
                    return super.visitSwitchStatement(node, data);
                }

                final InvocationExpression invocation = (InvocationExpression) argument;
                final Expression invocationTarget = invocation.getTarget();

                if (!(invocationTarget instanceof MemberReferenceExpression)) {
                    return super.visitSwitchStatement(node, data);
                }

                final MemberReferenceExpression memberReference = (MemberReferenceExpression) invocationTarget;

                if (!"ordinal".equals(memberReference.getMemberName())) {
                    return super.visitSwitchStatement(node, data);
                }

                final String enclosingTypeName = enclosingType.getInternalName();

                SwitchMapInfo info = _switchMaps.get(enclosingTypeName);

                if (info == null) {
                    _switchMaps.put(enclosingTypeName, info = new SwitchMapInfo(enclosingTypeName));

                    final TypeDefinition resolvedType = enclosingType.resolve();

                    if (resolvedType != null) {
                        AstBuilder astBuilder = context.getUserData(Keys.AST_BUILDER);

                        if (astBuilder == null) {
                            astBuilder = new AstBuilder(context);
                        }

                        try (final SafeCloseable importSuppression = astBuilder.suppressImports()) {
                            final TypeDeclaration declaration = astBuilder.createType(resolvedType);

                            declaration.acceptVisitor(this, data);
                        }
                    }
                }

                List<SwitchStatement> switches = info.switches.get(mapName);

                if (switches == null) {
                    info.switches.put(mapName, switches = new ArrayList<>());
                }

                switches.add(node);
            }

            return super.visitSwitchStatement(node, data);
        }

        @Override
        public Void visitAssignmentExpression(final AssignmentExpression node, final Void data) {
            final TypeDefinition currentType = context.getCurrentType();
            final MethodDefinition currentMethod = context.getCurrentMethod();

            if (_isSwitchMapWrapper &&
                currentType != null &&
                currentMethod != null &&
                currentMethod.isTypeInitializer()) {

                final Expression left = node.getLeft();
                final Expression right = node.getRight();

                if (left instanceof IndexerExpression &&
                    right instanceof PrimitiveExpression) {

                    String mapName = null;

                    final Expression array = ((IndexerExpression) left).getTarget();
                    final Expression argument = ((IndexerExpression) left).getArgument();

                    if (array instanceof MemberReferenceExpression) {
                        mapName = ((MemberReferenceExpression) array).getMemberName();
                    }
                    else if (array instanceof IdentifierExpression) {
                        mapName = ((IdentifierExpression) array).getIdentifier();
                    }

                    if (mapName == null || !mapName.startsWith("$SwitchMap$")) {
                        return super.visitAssignmentExpression(node, data);
                    }

                    if (!(argument instanceof InvocationExpression)) {
                        return super.visitAssignmentExpression(node, data);
                    }

                    final InvocationExpression invocation = (InvocationExpression) argument;
                    final Expression invocationTarget = invocation.getTarget();

                    if (!(invocationTarget instanceof MemberReferenceExpression)) {
                        return super.visitAssignmentExpression(node, data);
                    }

                    final MemberReferenceExpression memberReference = (MemberReferenceExpression) invocationTarget;
                    final Expression memberTarget = memberReference.getTarget();

                    if (!(memberTarget instanceof MemberReferenceExpression) || !"ordinal".equals(memberReference.getMemberName())) {
                        return super.visitAssignmentExpression(node, data);
                    }

                    final MemberReferenceExpression outerMemberReference = (MemberReferenceExpression) memberTarget;
                    final Expression outerMemberTarget = outerMemberReference.getTarget();

                    if (!(outerMemberTarget instanceof TypeReferenceExpression)) {
                        return super.visitAssignmentExpression(node, data);
                    }

                    final String enclosingType = currentType.getInternalName();

                    SwitchMapInfo info = _switchMaps.get(enclosingType);

                    if (info == null) {
                        _switchMaps.put(enclosingType, info = new SwitchMapInfo(enclosingType));

                        AstBuilder astBuilder = context.getUserData(Keys.AST_BUILDER);

                        if (astBuilder == null) {
                            astBuilder = new AstBuilder(context);
                        }

                        info.enclosingTypeDeclaration = astBuilder.createType(currentType);
                    }

                    final PrimitiveExpression value = (PrimitiveExpression) right;

                    assert value.getValue() instanceof Integer;

                    Map<Integer, Expression> mapping = info.mappings.get(mapName);

                    if (mapping == null) {
                        info.mappings.put(mapName, mapping = new LinkedHashMap<>());
                    }

                    final IdentifierExpression enumValue = new IdentifierExpression( Expression.MYSTERY_OFFSET, outerMemberReference.getMemberName());

                    enumValue.putUserData(Keys.MEMBER_REFERENCE, outerMemberReference.getUserData(Keys.MEMBER_REFERENCE));

                    mapping.put(((Number) value.getValue()).intValue(), enumValue);
                }
            }

            return super.visitAssignmentExpression(node, data);
        }

        private void rewrite() {
            if (_switchMaps.isEmpty()) {
                return;
            }

            for (final SwitchMapInfo info : _switchMaps.values()) {
                rewrite(info);
            }

            //
            // Remove switch map type wrappers that are no longer referenced.
            //

        outer:
            for (final SwitchMapInfo info : _switchMaps.values()) {
                for (final String mapName : info.switches.keySet()) {
                    final List<SwitchStatement> switches = info.switches.get(mapName);

                    if (switches != null && !switches.isEmpty()) {
                        continue outer;
                    }
                }

                final TypeDeclaration enclosingTypeDeclaration = info.enclosingTypeDeclaration;

                if (enclosingTypeDeclaration != null) {
                    enclosingTypeDeclaration.remove();
                }
            }
        }

        private void rewrite(final SwitchMapInfo info) {
            if (info.switches.isEmpty()) {
                return;
            }

            for (final String mapName : info.switches.keySet()) {
                final List<SwitchStatement> switches = info.switches.get(mapName);
                final Map<Integer, Expression> mappings = info.mappings.get(mapName);

                if (switches != null && mappings != null) {
                    for (int i = 0; i < switches.size(); i++) {
                        if (rewriteSwitch(switches.get(i), mappings)) {
                            switches.remove(i--);
                        }
                    }
                }
            }
        }

        private boolean rewriteSwitch(final SwitchStatement s, final Map<Integer, Expression> mappings) {
            final Map<Expression, Expression> replacements = new IdentityHashMap<>();

            for (final SwitchSection section : s.getSwitchSections()) {
                for (final CaseLabel caseLabel : section.getCaseLabels()) {
                    final Expression expression = caseLabel.getExpression();

                    if (expression.isNull()) {
                        continue;
                    }

                    if (expression instanceof PrimitiveExpression) {
                        final Object value = ((PrimitiveExpression) expression).getValue();

                        if (value instanceof Integer) {
                            final Expression replacement = mappings.get(value);

                            if (replacement != null) {
                                replacements.put(expression, replacement);
                                continue;
                            }
                        }
                    }

                    //
                    // If we can't rewrite all cases, we abort.
                    //

                    return false;
                }
            }

            final IndexerExpression indexer = (IndexerExpression) s.getExpression();
            final InvocationExpression argument = (InvocationExpression) indexer.getArgument();
            final MemberReferenceExpression memberReference = (MemberReferenceExpression) argument.getTarget();
            final Expression newTest = memberReference.getTarget();

            newTest.remove();
            indexer.replaceWith(newTest);

            for (final Map.Entry<Expression, Expression> entry : replacements.entrySet()) {
                entry.getKey().replaceWith(entry.getValue().clone());
            }

            return true;
        }

        private static boolean isSwitchMapWrapper(final TypeReference type) {
            if (type == null) {
                return false;
            }

            final TypeDefinition definition = type instanceof TypeDefinition ? (TypeDefinition) type
                                                                             : type.resolve();

            if (definition == null || !definition.isSynthetic() || !definition.isInnerClass()) {
                return false;
            }

            for (final FieldDefinition field : definition.getDeclaredFields()) {
                if (field.getName().startsWith("$SwitchMap$") &&
                    BuiltinTypes.Integer.makeArrayType().equals(field.getFieldType())) {

                    return true;
                }
            }

            return false;
        }
    }
}