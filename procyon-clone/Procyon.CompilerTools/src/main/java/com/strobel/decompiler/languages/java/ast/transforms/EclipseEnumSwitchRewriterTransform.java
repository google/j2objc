/*
 * EclipseEnumSwitchRewriterTransform.java
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

import com.strobel.assembler.metadata.*;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.patterns.*;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.strobel.core.CollectionUtilities.*;

public class EclipseEnumSwitchRewriterTransform implements IAstTransform {
    private final DecompilerContext _context;

    public EclipseEnumSwitchRewriterTransform(final DecompilerContext context) {
        _context = VerifyArgument.notNull(context, "context");
    }

    @Override
    public void run(final AstNode compilationUnit) {
        final Visitor visitor = new Visitor(_context);
        compilationUnit.acceptVisitor(visitor, null);
        visitor.rewrite();
    }

    private final static class Visitor extends ContextTrackingVisitor<Void> {
        private final static class SwitchMapInfo {
            final FieldReference switchMapField;
            final List<SwitchStatement> switches = new ArrayList<>();
            final Map<Integer, Expression> mappings = new LinkedHashMap<>();

            MethodReference switchMapMethod;
            MethodDeclaration switchMapMethodDeclaration;
            FieldDeclaration switchMapFieldDeclaration;

            SwitchMapInfo(final FieldReference switchMapField) {
                this.switchMapField = switchMapField;
            }
        }

        private final Map<String, SwitchMapInfo> _switchMaps = new LinkedHashMap<>();

        protected Visitor(final DecompilerContext context) {
            super(context);
        }

        @Override
        public Void visitSwitchStatement(final SwitchStatement node, final Void data) {
            final TypeDefinition currentType = context.getCurrentType();

            if (currentType == null) {
                return super.visitSwitchStatement(node, data);
            }

            final Expression test = node.getExpression();
            final Match m = SWITCH_INPUT.match(test);

            if (m.success()) {
                final InvocationExpression switchMapMethodCall = first(m.<InvocationExpression>get("switchMapMethodCall"));
                final MethodReference switchMapMethod = (MethodReference) switchMapMethodCall.getUserData(Keys.MEMBER_REFERENCE);

                if (!isSwitchMapMethod(switchMapMethod)) {
                    return super.visitSwitchStatement(node, data);
                }

                final FieldDefinition switchMapField;

                try {
                    final FieldReference r = new MetadataParser(currentType.getResolver()).parseField(
                        currentType,
                        switchMapMethod.getName(),
                        switchMapMethod.getReturnType().getErasedSignature()
                    );

                    switchMapField = r.resolve();
                }
                catch (final Throwable t) {
                    return super.visitSwitchStatement(node, data);
                }

                final String key = makeKey(switchMapField);

                SwitchMapInfo info = _switchMaps.get(key);

                if (info == null) {
                    _switchMaps.put(key, info = new SwitchMapInfo(switchMapField));
                }

                info.switches.add(node);
            }

            return super.visitSwitchStatement(node, data);
        }

        @Override
        public Void visitFieldDeclaration(final FieldDeclaration node, final Void data) {
            final FieldReference field = (FieldReference) node.getUserData(Keys.MEMBER_REFERENCE);

            if (isSwitchMapField(field)) {
                final String key = makeKey(field);

                SwitchMapInfo info = _switchMaps.get(key);

                if (info == null) {
                    _switchMaps.put(key, info = new SwitchMapInfo(field));
                }

                info.switchMapFieldDeclaration = node;
            }

            return super.visitFieldDeclaration(node, data);
        }

        @Override
        public Void visitMethodDeclaration(final MethodDeclaration node, final Void p) {
            final MethodDefinition methodDefinition = node.getUserData(Keys.METHOD_DEFINITION);

            if (isSwitchMapMethod(methodDefinition)) {
                final Match m = SWITCH_TABLE_METHOD_BODY.match(node.getBody());

                if (m.success()) {
                    final MemberReferenceExpression fieldAccess = first(m.<MemberReferenceExpression>get("fieldAccess"));
                    final FieldReference field = (FieldReference) fieldAccess.getUserData(Keys.MEMBER_REFERENCE);
                    final List<MemberReferenceExpression> enumValues = toList(m.<MemberReferenceExpression>get("enumValue"));
                    final List<PrimitiveExpression> tableValues = toList(m.<PrimitiveExpression>get("tableValue"));

                    assert field != null &&
                           tableValues.size() == enumValues.size();

                    final String key = makeKey(field);

                    SwitchMapInfo info = _switchMaps.get(key);

                    if (info == null) {
                        _switchMaps.put(key, info = new SwitchMapInfo(field));
                    }

                    info.switchMapMethodDeclaration = node;

                    for (int i = 0; i < enumValues.size(); i++) {
                        final MemberReferenceExpression memberReference = enumValues.get(i);
                        final IdentifierExpression identifier = new IdentifierExpression( Expression.MYSTERY_OFFSET, memberReference.getMemberName());

                        identifier.putUserData(Keys.MEMBER_REFERENCE, memberReference.getUserData(Keys.MEMBER_REFERENCE));
                        info.mappings.put((Integer) tableValues.get(i).getValue(), identifier);
                    }
                }
            }

            return super.visitMethodDeclaration(node, p);
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

            for (final SwitchMapInfo info : _switchMaps.values()) {
                if (info.switchMapMethod == null ||
                    info.switchMapFieldDeclaration == null ||
                    info.switchMapMethodDeclaration == null) {

                    continue;
                }

                final List<SwitchStatement> switches = info.switches;

                if (switches.isEmpty() && !context.getSettings().getShowSyntheticMembers()) {
                    info.switchMapFieldDeclaration.remove();
                    info.switchMapMethodDeclaration.remove();
                }
            }
        }

        private void rewrite(final SwitchMapInfo info) {
            if (info.switches.isEmpty()) {
                return;
            }

            final List<SwitchStatement> switches = info.switches;
            final Map<Integer, Expression> mappings = info.mappings;

            for (int i = 0; i < switches.size(); i++) {
                if (rewriteSwitch(switches.get(i), mappings)) {
                    switches.remove(i--);
                }
            }
        }

        private boolean rewriteSwitch(final SwitchStatement s, final Map<Integer, Expression> mappings) {
            final Match m = SWITCH_INPUT.match(s.getExpression());

            if (!m.success()) {
                return false;
            }

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

            final Expression newTest = first(m.<Expression>get("target"));

            newTest.remove();
            s.getExpression().replaceWith(newTest);

            for (final Map.Entry<Expression, Expression> entry : replacements.entrySet()) {
                entry.getKey().replaceWith(entry.getValue().clone());
            }

            return true;
        }

        private static boolean isSwitchMapMethod(final MethodReference method) {
            if (method == null) {
                return false;
            }

            final MethodDefinition definition = method instanceof MethodDefinition ? (MethodDefinition) method
                                                                                   : method.resolve();

            return definition != null &&
                   definition.isSynthetic() &&
                   definition.isStatic() &&
                   definition.isPackagePrivate() &&
                   StringUtilities.startsWith(definition.getName(), "$SWITCH_TABLE$") &&
                   MetadataResolver.areEquivalent(BuiltinTypes.Integer.makeArrayType(), definition.getReturnType());
        }

        private static boolean isSwitchMapField(final FieldReference field) {
            if (field == null) {
                return false;
            }

            final FieldDefinition definition = field instanceof FieldDefinition ? (FieldDefinition) field
                                                                                : field.resolve();

            return definition != null &&
                   definition.isSynthetic() &&
                   definition.isStatic() &&
                   definition.isPrivate() &&
                   StringUtilities.startsWith(definition.getName(), "$SWITCH_TABLE$") &&
                   MetadataResolver.areEquivalent(BuiltinTypes.Integer.makeArrayType(), definition.getFieldType());
        }

        private static String makeKey(final FieldReference field) {
            return field.getFullName() + ":" + field.getErasedSignature();
        }

        private final static INode SWITCH_INPUT;
        private final static INode SWITCH_TABLE_METHOD_BODY;

        static {
            final SimpleType intType = new SimpleType("int");

            intType.putUserData(Keys.TYPE_REFERENCE, BuiltinTypes.Integer);

            final AstType intArrayType = new ComposedType(intType).makeArrayType();
            final BlockStatement body = new BlockStatement();

            final VariableDeclarationStatement v1 = new VariableDeclarationStatement(
                intArrayType,
                Pattern.ANY_STRING,
                Expression.MYSTERY_OFFSET
            );

            final VariableDeclarationStatement v2 = new VariableDeclarationStatement(
                intArrayType.clone(),
                Pattern.ANY_STRING,
                Expression.MYSTERY_OFFSET
            );

            body.add(new NamedNode("v1", v1).toStatement());
            body.add(new NamedNode("v2", v2).toStatement());

            body.add(
                new ExpressionStatement(
                    new AssignmentExpression(
                        new DeclaredVariableBackReference("v1").toExpression(),
                        new MemberReferenceExpressionRegexNode(
                            "fieldAccess",
                            new TypedNode(TypeReferenceExpression.class),
                            "\\$SWITCH_TABLE\\$.*"
                        ).toExpression()
                    )
                )
            );

            body.add(
                new IfElseStatement(Expression.MYSTERY_OFFSET,
                    new BinaryOperatorExpression(
                        new DeclaredVariableBackReference("v1").toExpression(),
                        BinaryOperatorType.INEQUALITY,
                        new NullReferenceExpression( Expression.MYSTERY_OFFSET)
                    ),
                    new BlockStatement(
                        new ReturnStatement(Expression.MYSTERY_OFFSET, new DeclaredVariableBackReference("v1").toExpression())
                    )
                )
            );

            final ArrayCreationExpression arrayCreation = new ArrayCreationExpression( Expression.MYSTERY_OFFSET);

            final Expression dimension = new MemberReferenceExpression(
                Expression.MYSTERY_OFFSET,
                new InvocationExpression(
                    Expression.MYSTERY_OFFSET,
                    new MemberReferenceExpression(
                        Expression.MYSTERY_OFFSET,
                        new Choice(
                            new TypedNode("enumType", TypeReferenceExpression.class),
                            Expression.NULL
                        ).toExpression(),
                        "values"
                    )
                ),
                "length"
            );

            arrayCreation.setType(intType.clone());
            arrayCreation.getDimensions().add(dimension);

            body.add(
                new AssignmentExpression(
                    new DeclaredVariableBackReference("v2").toExpression(),
                    arrayCreation
                )
            );

            final ExpressionStatement assignment = new ExpressionStatement(
                new AssignmentExpression(
                    new IndexerExpression(
                        Expression.MYSTERY_OFFSET,
                        new DeclaredVariableBackReference("v2").toExpression(),
                        new InvocationExpression(
                            Expression.MYSTERY_OFFSET,
                            new MemberReferenceExpression(
                                Expression.MYSTERY_OFFSET,
                                new NamedNode(
                                    "enumValue",
                                    new MemberReferenceExpression(
                                        Expression.MYSTERY_OFFSET,
                                        new TypedNode(TypeReferenceExpression.class).toExpression(),
                                        Pattern.ANY_STRING
                                    )
                                ).toExpression(),
                                "ordinal"
                            )
                        )
                    ),
                    new TypedLiteralNode("tableValue", Integer.class).toExpression()
                )
            );

            final TryCatchStatement tryCatch = new TryCatchStatement( Expression.MYSTERY_OFFSET);
            final CatchClause catchClause = new CatchClause(new BlockStatement());

            catchClause.setVariableName(Pattern.ANY_STRING);
            catchClause.getExceptionTypes().add(new SimpleType("NoSuchFieldError"));

            tryCatch.setTryBlock(new BlockStatement(assignment.clone()));
            tryCatch.getCatchClauses().add(catchClause);

            body.add(new Repeat(tryCatch).toStatement());

            body.add(
                new Choice(
                    new BlockStatement(
                        new ExpressionStatement(
                            new AssignmentExpression(
                                new BackReference("fieldAccess").toExpression(),
                                new DeclaredVariableBackReference("v2").toExpression()
                            )
                        ),
                        new ReturnStatement(Expression.MYSTERY_OFFSET, new DeclaredVariableBackReference("v2").toExpression())
                    ),
                    new ReturnStatement(
                        Expression.MYSTERY_OFFSET,
                        new AssignmentExpression(
                            new BackReference("fieldAccess").toExpression(),
                            new DeclaredVariableBackReference("v2").toExpression()
                        )
                    )
                ).toStatement()
            );

            SWITCH_TABLE_METHOD_BODY = body;

            SWITCH_INPUT = new IndexerExpression( Expression.MYSTERY_OFFSET,
                new NamedNode(
                    "switchMapMethodCall",
                    new InvocationExpression(
                        Expression.MYSTERY_OFFSET,
                        new MemberReferenceExpressionRegexNode(
                            Expression.NULL,
                            "\\$SWITCH_TABLE\\$.*"
                        ).toExpression()
                    )
                ).toExpression(),
                new NamedNode(
                    "ordinalCall",
                    new InvocationExpression(
                        Expression.MYSTERY_OFFSET,
                        new MemberReferenceExpression(
                            Expression.MYSTERY_OFFSET,
                            new AnyNode("target").toExpression(),
                            "ordinal"
                        )
                    )
                ).toExpression()
            );
        }
    }
}