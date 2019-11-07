/*
 * EclipseStringSwitchRewriterTransform.java
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

import com.strobel.assembler.metadata.MemberReference;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.patterns.AnyNode;
import com.strobel.decompiler.patterns.Choice;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.NamedNode;
import com.strobel.decompiler.patterns.Pattern;
import com.strobel.decompiler.patterns.Repeat;
import com.strobel.decompiler.patterns.SingleOrBinaryAggregateNode;

import java.util.ArrayList;
import java.util.List;

import static com.strobel.core.CollectionUtilities.*;

public class EclipseStringSwitchRewriterTransform extends ContextTrackingVisitor<Void> {
    public EclipseStringSwitchRewriterTransform(final DecompilerContext context) {
        super(context);
    }

    // <editor-fold defaultstate="collapsed" desc="Patterns">

    private final static Pattern HASH_CODE_PATTERN;
    private final static BlockStatement CASE_BODY_PATTERN;

    static {
        HASH_CODE_PATTERN = new NamedNode(
            "hashCodeCall",
            new InvocationExpression(
                Expression.MYSTERY_OFFSET,
                new MemberReferenceExpression(
                    Expression.MYSTERY_OFFSET,
                    new AnyNode("target").toExpression(),
                    "hashCode"
                )
            )
        );

        final BlockStatement caseBody = new BlockStatement();

        final IfElseStatement test = new IfElseStatement( Expression.MYSTERY_OFFSET,
            new UnaryOperatorExpression(
                UnaryOperatorType.NOT,
                new SingleOrBinaryAggregateNode(
                    BinaryOperatorType.LOGICAL_OR,
                    new InvocationExpression(
                        Expression.MYSTERY_OFFSET,
                        new MemberReferenceExpression(
                            Expression.MYSTERY_OFFSET,
                            new NamedNode("input", new IdentifierExpression( Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression(),
                            "equals"
                        ),
                        new NamedNode("stringValue", new PrimitiveExpression( Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression()
                    )
                ).toExpression()
            ),
            new BlockStatement(
                new Choice(
                    new NamedNode("defaultBreak", new BreakStatement(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)),
                    new ReturnStatement(Expression.MYSTERY_OFFSET)
                ).toStatement()
            )
        );

        caseBody.add(new NamedNode("test", test).toStatement());
        caseBody.add(new Repeat(new AnyNode("statements")).toStatement());

        CASE_BODY_PATTERN = caseBody;
    }

    // </editor-fold>

    @Override
    @SuppressWarnings("ConstantConditions")
    public Void visitSwitchStatement(final SwitchStatement node, final Void data) {
        super.visitSwitchStatement(node, data);

        final Expression input = node.getExpression();

        if (input == null || input.isNull()) {
            return null;
        }

        final Match m2 = HASH_CODE_PATTERN.match(input);

        if (!m2.success()) {
            return null;
        }

        final InvocationExpression hashCodeCall = first(m2.<InvocationExpression>get("hashCodeCall"));
        final MemberReference hashCodeMethod = hashCodeCall.getUserData(Keys.MEMBER_REFERENCE);

        if (!(hashCodeMethod instanceof MethodReference &&
              "java/lang/String".equals(hashCodeMethod.getDeclaringType().getInternalName()))) {

            return null;
        }

        final List<Match> matches = new ArrayList<>();

        final AstNodeCollection<SwitchSection> sections = node.getSwitchSections();

        for (final SwitchSection section : sections) {
            final AstNodeCollection<CaseLabel> caseLabels = section.getCaseLabels();

            if (caseLabels.isEmpty() ||
                caseLabels.hasSingleElement() && caseLabels.firstOrNullObject().isNull()) {

                //
                // Eclipse does not emit default sections.
                //
                return null;
            }

            final Match m3 = CASE_BODY_PATTERN.match(section.getStatements().firstOrNullObject());

            if (m3.success()) {
                matches.add(m3);
            }
            else {
                return null;
            }
        }

        int matchIndex = 0;
        BreakStatement defaultBreak = null;

        for (final SwitchSection section : sections) {
            final Match m = matches.get(matchIndex++);
            final IfElseStatement test = first(m.<IfElseStatement>get("test"));
            final List<PrimitiveExpression> stringValues = toList(m.<PrimitiveExpression>get("stringValue"));
            final AstNodeCollection<CaseLabel> caseLabels = section.getCaseLabels();

            if (defaultBreak == null) {
                defaultBreak = firstOrDefault(m.<BreakStatement>get("defaultBreak"));
            }

            caseLabels.clear();
            test.remove();

            for (int i = 0; i < stringValues.size(); i++) {
                final PrimitiveExpression stringValue = stringValues.get(i);

                stringValue.remove();
                caseLabels.add(new CaseLabel(stringValue));
            }
        }

        if (defaultBreak != null) {
            final SwitchSection defaultSection = new SwitchSection();

            defaultBreak.remove();

            defaultSection.getCaseLabels().add(new CaseLabel());
            defaultSection.getStatements().add(defaultBreak);

            sections.add(defaultSection);
        }

        final AstNode newInput = first(m2.<AstNode>get("target"));

        newInput.remove();
        node.getExpression().replaceWith(newInput);

        return null;
    }
}