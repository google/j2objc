/*
 * IntroduceStringConcatenationTransform.java
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

import com.strobel.assembler.metadata.CommonTypeReferences;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.StringUtilities;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.OptionalNode;
import com.strobel.decompiler.patterns.TypedExpression;
import com.strobel.decompiler.semantics.ResolveResult;

import java.util.ArrayList;
import java.util.List;

import static com.strobel.core.CollectionUtilities.firstOrDefault;

public class IntroduceStringConcatenationTransform extends ContextTrackingVisitor<Void> {
    private final INode _stringBuilderArgumentPattern;

    public IntroduceStringConcatenationTransform(final DecompilerContext context) {
        super(context);

        _stringBuilderArgumentPattern = new OptionalNode(
            new TypedExpression(
                "firstArgument",
                CommonTypeReferences.String,
                new JavaResolver(context)
            )
        );
    }

    @Override
    public Void visitObjectCreationExpression(final ObjectCreationExpression node, final Void data) {
        final AstNodeCollection<Expression> arguments = node.getArguments();

        if (arguments.isEmpty() ||
            arguments.hasSingleElement()) {

            final Expression firstArgument;

            if (arguments.hasSingleElement()) {
                final Match m = _stringBuilderArgumentPattern.match(arguments.firstOrNullObject());

                if (!m.success()) {
                    return super.visitObjectCreationExpression(node, data);
                }

                firstArgument = firstOrDefault(m.<Expression>get("firstArgument"));
            }
            else {
                firstArgument = null;
            }

            final TypeReference typeReference = node.getType().toTypeReference();

            if (typeReference != null &&
                isStringBuilder(typeReference)) {

                convertStringBuilderToConcatenation(node, firstArgument);
            }
        }

        return super.visitObjectCreationExpression(node, data);
    }

    private boolean isStringBuilder(final TypeReference typeReference) {
        if (StringUtilities.equals(typeReference.getInternalName(), "java/lang/StringBuilder")) {
            return true;
        }

        return context.getCurrentType() != null &&
               context.getCurrentType().getCompilerMajorVersion() < 49 &&
               StringUtilities.equals(typeReference.getInternalName(), "java/lang/StringBuffer");
    }

    private void convertStringBuilderToConcatenation(final ObjectCreationExpression node, final Expression firstArgument) {
        if (node.getParent() == null || node.getParent().getParent() == null) {
            return;
        }

        final ArrayList<Expression> operands = new ArrayList<>();

        if (firstArgument != null) {
            operands.add(firstArgument);
        }

        AstNode current;
        AstNode parent;

        for (current = node.getParent(), parent = current.getParent();
             current instanceof MemberReferenceExpression && parent instanceof InvocationExpression && parent.getParent() != null;
             current = parent.getParent(), parent = current.getParent()) {

            final String memberName = ((MemberReferenceExpression) current).getMemberName();
            final AstNodeCollection<Expression> arguments = ((InvocationExpression) parent).getArguments();

            if (StringUtilities.equals(memberName, "append") && arguments.size() == 1) {
                operands.add(arguments.firstOrNullObject());
            }
            else {
                break;
            }
        }

        if (operands.size() > 1 &&
            anyIsString(operands.subList(0, 2)) &&
            current instanceof MemberReferenceExpression &&
            parent instanceof InvocationExpression &&
            !(parent.getParent() instanceof ExpressionStatement) &&
            StringUtilities.equals(((MemberReferenceExpression) current).getMemberName(), "toString") &&
            ((InvocationExpression) parent).getArguments().isEmpty()) {

            for (final Expression operand : operands) {
                operand.remove();
            }

            Expression concatenation = new BinaryOperatorExpression(operands.get(0), BinaryOperatorType.ADD, operands.get(1));

            for (int i = 2; i < operands.size(); i++) {
                concatenation = new BinaryOperatorExpression(concatenation, BinaryOperatorType.ADD, operands.get(i));
            }

            parent.replaceWith(concatenation);
        }
    }

    private boolean anyIsString(final List<Expression> expressions) {
        final JavaResolver resolver = new JavaResolver(context);

        for (int i = 0; i < expressions.size(); i++) {
            final ResolveResult result = resolver.apply(expressions.get(i));

            if (result != null &&
                result.getType() != null &&
                CommonTypeReferences.String.isEquivalentTo(result.getType())) {

                return true;
            }
        }

        return false;
    }
}
