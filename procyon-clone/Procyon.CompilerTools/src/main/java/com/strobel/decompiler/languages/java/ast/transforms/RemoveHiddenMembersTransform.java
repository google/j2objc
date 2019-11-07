/*
 * RemoveHiddenMembersTransform.java
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
import com.strobel.assembler.metadata.Flags;
import com.strobel.assembler.metadata.MemberReference;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.core.Predicate;
import com.strobel.core.StringUtilities;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.NamedNode;

import static com.strobel.core.CollectionUtilities.*;

public class RemoveHiddenMembersTransform extends ContextTrackingVisitor<Void> {
    public RemoveHiddenMembersTransform(final DecompilerContext context) {
        super(context);
    }

    @Override
    public Void visitTypeDeclaration(final TypeDeclaration node, final Void p) {
        if (!(node.getParent() instanceof CompilationUnit)) {
            final TypeDefinition type = node.getUserData(Keys.TYPE_DEFINITION);

            if (type != null && AstBuilder.isMemberHidden(type, context)) {
                node.remove();
                return null;
            }
        }

        return super.visitTypeDeclaration(node, p);
    }

    @Override
    public Void visitFieldDeclaration(final FieldDeclaration node, final Void data) {
        final FieldDefinition field = node.getUserData(Keys.FIELD_DEFINITION);

        if (field != null && AstBuilder.isMemberHidden(field, context)) {
            node.remove();
            return null;
        }

        return super.visitFieldDeclaration(node, data);
    }

    @Override
    public Void visitMethodDeclaration(final MethodDeclaration node, final Void p) {
        final MethodDefinition method = node.getUserData(Keys.METHOD_DEFINITION);

        if (method != null) {
            if (AstBuilder.isMemberHidden(method, context)) {
                node.remove();
                return null;
            }

            if (method.isTypeInitializer()) {
                if (node.getBody().getStatements().isEmpty()) {
                    node.remove();
                    return null;
                }
            }
        }

        return super.visitMethodDeclaration(node, p);
    }

    private final static INode DEFAULT_CONSTRUCTOR_BODY;
    private final static AstNode EMPTY_SUPER;

    static {
        DEFAULT_CONSTRUCTOR_BODY = new BlockStatement(
            new ExpressionStatement(
                new InvocationExpression(
                    Expression.MYSTERY_OFFSET,
                    new SuperReferenceExpression(Expression.MYSTERY_OFFSET)
                )
            )
        );

        EMPTY_SUPER = new ExpressionStatement(
            new NamedNode(
                "target",
                new SuperReferenceExpression(Expression.MYSTERY_OFFSET).invoke()
            ).toExpression()
        );
    }

    @Override
    public Void visitConstructorDeclaration(final ConstructorDeclaration node, final Void p) {
        final MethodDefinition method = node.getUserData(Keys.METHOD_DEFINITION);

        if (method != null) {
            final TypeDefinition declaringType = method.getDeclaringType();

            if (declaringType != null) {
                if (AstBuilder.isMemberHidden(method, context)) {
                    if (declaringType.isEnum() &&
                        declaringType.isAnonymous() &&
                        !node.getBody().getStatements().isEmpty()) {

                        //
                        // Keep initializer blocks in anonymous enum value bodies.
                        //
                        return super.visitConstructorDeclaration(node, p);
                    }

                    node.remove();
                    return null;
                }

                if (!context.getSettings().getShowSyntheticMembers() &&
                    (method.getModifiers() & Flags.AccessFlags) == (declaringType.getModifiers() & Flags.AccessFlags) &&
                    node.getParameters().isEmpty() &&
                    node.getThrownTypes().isEmpty() &&
                    node.getTypeParameters().isEmpty() &&
                    node.getAnnotations().isEmpty() &&
                    DEFAULT_CONSTRUCTOR_BODY.matches(node.getBody())) {

                    //
                    // Remove redundant default constructors.
                    //

                    final boolean hasOtherConstructors = any(
                        declaringType.getDeclaredMethods(),
                        new Predicate<MethodDefinition>() {
                            @Override
                            public boolean test(final MethodDefinition m) {
                                return m.isConstructor() &&
                                       !m.isSynthetic() &&
                                       !StringUtilities.equals(m.getErasedSignature(), method.getErasedSignature());
                            }
                        }
                    );

                    if (!hasOtherConstructors) {
                        node.remove();
                        return null;
                    }
                }
            }
        }

        return super.visitConstructorDeclaration(node, p);
    }

    @Override
    public Void visitExpressionStatement(final ExpressionStatement node, final Void data) {
        super.visitExpressionStatement(node, data);

        //
        // Remove `super()`-style invocations within constructors, but only if they actually
        // bind to a constructor.  Keep calls to potentially obfuscated methods named 'super'.
        //

        if (inConstructor() && !context.getSettings().getShowSyntheticMembers()) {
            final Match match = EMPTY_SUPER.match(node);

            if (match.success()) {
                final AstNode target = first(match.<AstNode>get("target"));
                final MemberReference member = target.getUserData(Keys.MEMBER_REFERENCE);

                if (member instanceof MethodReference && ((MethodReference) member).isConstructor()) {
                    node.remove();
                }
            }
        }

        return null;
    }
}
