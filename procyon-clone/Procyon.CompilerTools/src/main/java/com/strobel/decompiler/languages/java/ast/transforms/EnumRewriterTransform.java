/*
 * EnumRewriterTransform.java
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

import javax.lang.model.element.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.strobel.core.CollectionUtilities.first;
import static com.strobel.core.CollectionUtilities.firstOrDefault;

public class EnumRewriterTransform implements IAstTransform {
    private final DecompilerContext _context;

    public EnumRewriterTransform(final DecompilerContext context) {
        _context = VerifyArgument.notNull(context, "context");
    }

    @Override
    public void run(final AstNode compilationUnit) {
        compilationUnit.acceptVisitor(new Visitor(_context), null);
    }

    private final static class Visitor extends ContextTrackingVisitor<Void> {
        private Map<String, FieldDeclaration> _valueFields = new LinkedHashMap<>();
        private Map<String, ObjectCreationExpression> _valueInitializers = new LinkedHashMap<>();
        private MemberReference _valuesField;

        protected Visitor(final DecompilerContext context) {
            super(context);
        }

        @Override
        public Void visitTypeDeclaration(final TypeDeclaration typeDeclaration, final Void p) {
            final MemberReference oldValuesField = _valuesField;
            final Map<String, FieldDeclaration> oldValueFields = _valueFields;
            final Map<String, ObjectCreationExpression> oldValueInitializers = _valueInitializers;

            final LinkedHashMap<String, FieldDeclaration> valueFields = new LinkedHashMap<>();
            final LinkedHashMap<String, ObjectCreationExpression> valueInitializers = new LinkedHashMap<>();

            _valuesField = findValuesField(typeDeclaration);
            _valueFields = valueFields;
            _valueInitializers = valueInitializers;

            try {
                super.visitTypeDeclaration(typeDeclaration, p);
            }
            finally {
                _valuesField = oldValuesField;
                _valueFields = oldValueFields;
                _valueInitializers = oldValueInitializers;
            }

            rewrite(valueFields, valueInitializers);

            return null;
        }

        private MemberReference findValuesField(final TypeDeclaration declaration) {
            final TypeDefinition definition = declaration.getUserData(Keys.TYPE_DEFINITION);

            if (definition == null || !definition.isEnum()) {
                return null;
            }

            final AstBuilder astBuilder = context.getUserData(Keys.AST_BUILDER);

            if (astBuilder == null) {
                return null;
            }

            final MethodDeclaration pattern = new MethodDeclaration();

            pattern.setName("values");
            pattern.setReturnType(astBuilder.convertType(definition.makeArrayType()));
            pattern.getModifiers().add(new JavaModifierToken(Modifier.PUBLIC));
            pattern.getModifiers().add(new JavaModifierToken(Modifier.STATIC));
            pattern.setBody(
                new BlockStatement(
                    new ReturnStatement(
                        Expression.MYSTERY_OFFSET,
                        new Choice(
                            new MemberReferenceExpression(
                                Expression.MYSTERY_OFFSET,
                                new NamedNode(
                                    "valuesField",
                                    new TypeReferenceExpression(
                                        Expression.MYSTERY_OFFSET,
                                        astBuilder.convertType(definition)
                                    ).member(Pattern.ANY_STRING)
                                ).toExpression(),
                                "clone"
                            ).invoke(),
                            new CastExpression(
                                astBuilder.convertType(definition.makeArrayType()),
                                new MemberReferenceExpression(
                                    Expression.MYSTERY_OFFSET,
                                    new NamedNode(
                                        "valuesField",
                                        new TypeReferenceExpression(
                                            Expression.MYSTERY_OFFSET,
                                            astBuilder.convertType(definition)
                                        ).member(Pattern.ANY_STRING)
                                    ).toExpression(),
                                    "clone"
                                ).invoke()
                            )
                        ).toExpression()
                    )
                )
            );

            for (final EntityDeclaration d : declaration.getMembers()) {
                if (d instanceof MethodDeclaration) {
                    final Match match = pattern.match(d);

                    if (match.success()) {
                        final MemberReferenceExpression reference = firstOrDefault(match.<MemberReferenceExpression>get("valuesField"));
                        return reference.getUserData(Keys.MEMBER_REFERENCE);
                    }
                }
            }

            return null;
        }

        @Override
        public Void visitFieldDeclaration(final FieldDeclaration node, final Void data) {
            final TypeDefinition currentType = context.getCurrentType();

            if (currentType != null && currentType.isEnum()) {
                final FieldDefinition field = node.getUserData(Keys.FIELD_DEFINITION);

                if (field != null) {
                    if (field.isEnumConstant()) {
                        _valueFields.put(field.getName(), node);
                    }
                }
            }

            return super.visitFieldDeclaration(node, data);
        }

        @Override
        public Void visitAssignmentExpression(final AssignmentExpression node, final Void data) {
            final TypeDefinition currentType = context.getCurrentType();
            final MethodDefinition currentMethod = context.getCurrentMethod();

            if (currentType != null &&
                currentMethod != null &&
                currentType.isEnum() &&
                currentMethod.isTypeInitializer()) {

                final Expression left = node.getLeft();
                final Expression right = node.getRight();

                final MemberReference member = left.getUserData(Keys.MEMBER_REFERENCE);

                if (member instanceof FieldReference) {
                    final FieldDefinition resolvedField = ((FieldReference) member).resolve();

                    if (resolvedField != null &&
                        (right instanceof ObjectCreationExpression ||
                         right instanceof ArrayCreationExpression)) {

                        final String fieldName = resolvedField.getName();

                        if (resolvedField.isEnumConstant() &&
                            right instanceof ObjectCreationExpression &&
                            MetadataResolver.areEquivalent(currentType, resolvedField.getFieldType())) {

                            _valueInitializers.put(fieldName, (ObjectCreationExpression) right);
                        }
                        else if (resolvedField.isSynthetic() &&
                                 !context.getSettings().getShowSyntheticMembers() &&
                                 matchesValuesField(resolvedField) &&
                                 MetadataResolver.areEquivalent(currentType.makeArrayType(), resolvedField.getFieldType())) {

                            final Statement parentStatement = findStatement(node);

                            if (parentStatement != null) {
                                parentStatement.remove();
                            }
                        }
                    }
                }
            }

            return super.visitAssignmentExpression(node, data);
        }

        private final static INode SUPER_PATTERN = new SubtreeMatch(
            new BlockStatement(
                new Repeat(new TypedNode(VariableDeclarationStatement.class)).toStatement(),
                new NamedNode(
                    "superCall",
                    new ExpressionStatement(
                        new InvocationExpression(
                            Expression.MYSTERY_OFFSET,
                            new SuperReferenceExpression(Expression.MYSTERY_OFFSET),
                            new Repeat(new AnyNode()).toExpression()
                        )
                    )
                ).toStatement(),
                new Repeat(new AnyNode()).toStatement()
            )
        );

        @Override
        public Void visitConstructorDeclaration(final ConstructorDeclaration node, final Void p) {
            final TypeDefinition currentType = context.getCurrentType();
            final MethodDefinition constructor = node.getUserData(Keys.METHOD_DEFINITION);

            if (currentType != null && currentType.isEnum()) {
                final List<ParameterDefinition> pDefinitions = constructor.getParameters();
                final AstNodeCollection<ParameterDeclaration> pDeclarations = node.getParameters();

                for (int i = 0; i < pDefinitions.size() && !pDeclarations.isEmpty() && pDefinitions.get(i).isSynthetic(); i++) {
                    pDeclarations.firstOrNullObject().remove();
                }

                final BlockStatement body = node.getBody();
                final Match superCallMatch = SUPER_PATTERN.match(body);
                final AstNodeCollection<Statement> statements = body.getStatements();

                if (superCallMatch.success()) {
                    final Statement superCall = first(superCallMatch.<Statement>get("superCall"));
                    superCall.remove();
                }

                if (statements.isEmpty()) {
                    if (pDeclarations.isEmpty()) {
                        node.remove();
                    }
                }
                else if (currentType.isAnonymous()) {
                    final InstanceInitializer initializer = new InstanceInitializer();
                    final BlockStatement initializerBody = new BlockStatement();

                    for (final Statement statement : statements) {
                        statement.remove();
                        initializerBody.add(statement);
                    }

                    initializer.setBody(initializerBody);
                    node.replaceWith(initializer);
                }
            }

            return super.visitConstructorDeclaration(node, p);
        }

        @Override
        public Void visitMethodDeclaration(final MethodDeclaration node, final Void p) {
            final TypeDefinition currentType = context.getCurrentType();

            if (currentType != null && currentType.isEnum() && !context.getSettings().getShowSyntheticMembers()) {
                final MethodDefinition method = node.getUserData(Keys.METHOD_DEFINITION);

                if (method != null &&
                    method.isPublic() &&
                    method.isStatic()) {

                    switch (method.getName()) {
                        case "values": {
                            if (method.getParameters().isEmpty() &&
                                MetadataResolver.areEquivalent(currentType.makeArrayType(), method.getReturnType())) {

                                node.remove();
                            }
                            break;
                        }

                        case "valueOf": {
                            if (currentType.equals(method.getReturnType().resolve()) &&
                                method.getParameters().size() == 1) {

                                final ParameterDefinition pd = method.getParameters().get(0);

                                if ("java/lang/String".equals(pd.getParameterType().getInternalName())) {
                                    node.remove();
                                }
                            }
                            break;
                        }
                    }
                }
            }

            return super.visitMethodDeclaration(node, p);
        }

        private void rewrite(
            final LinkedHashMap<String, FieldDeclaration> valueFields,
            final LinkedHashMap<String, ObjectCreationExpression> valueInitializers) {

//            assert valueFields.size() == valueInitializers.size();

            if (valueFields.isEmpty() || valueFields.size() != valueInitializers.size()) {
                return;
            }

            final MethodDeclaration typeInitializer = findMethodDeclaration(first(valueInitializers.values()));

            for (final String name : valueFields.keySet()) {
                final FieldDeclaration field = valueFields.get(name);
                final ObjectCreationExpression initializer = valueInitializers.get(name);

                assert field != null && initializer != null;

                final MethodReference constructor = (MethodReference) initializer.getUserData(Keys.MEMBER_REFERENCE);
                final MethodDefinition resolvedConstructor = constructor.resolve();

                final EnumValueDeclaration enumDeclaration = new EnumValueDeclaration();
                final Statement initializerStatement = findStatement(initializer);

                assert initializerStatement != null;

                initializerStatement.remove();

                enumDeclaration.setName(name);
                enumDeclaration.putUserData(Keys.FIELD_DEFINITION, field.getUserData(Keys.FIELD_DEFINITION));
                enumDeclaration.putUserData(Keys.MEMBER_REFERENCE, field.getUserData(Keys.MEMBER_REFERENCE));

                for (final Annotation annotation : field.getAnnotations()) {
                    annotation.remove();
                    enumDeclaration.getAnnotations().add(annotation);
                }

                if (resolvedConstructor != null) {
                    enumDeclaration.putUserData(Keys.TYPE_DEFINITION, resolvedConstructor.getDeclaringType());
                }

                int i = 0;

                final AstNodeCollection<Expression> arguments = initializer.getArguments();
                final boolean trimArguments = arguments.size() == constructor.getParameters().size();

                for (final Expression argument : arguments) {
                    if (trimArguments && resolvedConstructor != null && resolvedConstructor.isSynthetic() && i++ < 2) {
                        continue;
                    }

                    argument.remove();
                    enumDeclaration.getArguments().add(argument);
                }

                if (initializer instanceof AnonymousObjectCreationExpression) {
                    final AnonymousObjectCreationExpression creation = (AnonymousObjectCreationExpression) initializer;

                    for (final EntityDeclaration member : creation.getTypeDeclaration().getMembers()) {
                        member.remove();
                        enumDeclaration.getMembers().add(member);
                    }
                }

                field.replaceWith(enumDeclaration);
            }

            if (typeInitializer != null && typeInitializer.getBody().getStatements().isEmpty()) {
                typeInitializer.remove();
            }
        }

        private Statement findStatement(final AstNode node) {
            for (AstNode current = node; current != null; current = current.getParent()) {
                if (current instanceof Statement) {
                    return (Statement) current;
                }
            }
            return null;
        }

        private MethodDeclaration findMethodDeclaration(final AstNode node) {
            for (AstNode current = node; current != null; current = current.getParent()) {
                if (current instanceof MethodDeclaration) {
                    return (MethodDeclaration) current;
                }
            }
            return null;
        }

        private boolean matchesValuesField(final FieldDefinition field) {
            if (field == null) {
                return false;
            }

            if (field.isEquivalentTo(_valuesField)) {
                return true;
            }

            final String fieldName = field.getName();

            return StringUtilities.equals(fieldName, "$VALUES") ||
                   StringUtilities.equals(fieldName, "ENUM$VALUES");
        }
    }
}
