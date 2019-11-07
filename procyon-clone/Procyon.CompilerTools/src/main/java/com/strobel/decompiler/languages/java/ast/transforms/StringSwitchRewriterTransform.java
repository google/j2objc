/*
 * StringSwitchRewriterTransform.java
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
import com.strobel.assembler.metadata.MemberReference;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.core.Predicate;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.patterns.AnyNode;
import com.strobel.decompiler.patterns.IdentifierExpressionBackReference;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.NamedNode;
import com.strobel.decompiler.patterns.OptionalNode;
import com.strobel.decompiler.patterns.Pattern;
import com.strobel.decompiler.patterns.Repeat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.strobel.core.CollectionUtilities.*;

public class StringSwitchRewriterTransform extends ContextTrackingVisitor<Void> {
    public StringSwitchRewriterTransform(final DecompilerContext context) {
        super(context);
    }

    // <editor-fold defaultstate="collapsed" desc="Patterns">

    private final static VariableDeclarationStatement TABLE_SWITCH_INPUT;
    private final static Pattern HASH_CODE_PATTERN;
    private final static BlockStatement CASE_BODY_PATTERN;

    static {
        final SimpleType intType = new SimpleType("int");

        intType.putUserData(Keys.TYPE_REFERENCE, BuiltinTypes.Integer);

        TABLE_SWITCH_INPUT = new VariableDeclarationStatement(
            intType,
            Pattern.ANY_STRING,
            new PrimitiveExpression( Expression.MYSTERY_OFFSET, -1)
        );

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

        final IfElseStatement test = new IfElseStatement(Expression.MYSTERY_OFFSET,
            new InvocationExpression(
                Expression.MYSTERY_OFFSET,
                new MemberReferenceExpression(
                    Expression.MYSTERY_OFFSET,
                    new NamedNode("input", new IdentifierExpression( Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression(),
                    "equals"
                ),
                new NamedNode("stringValue", new PrimitiveExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression()
            ),
            new BlockStatement(
                new ExpressionStatement(
                    new AssignmentExpression(
                        new NamedNode("tableSwitchInput", new IdentifierExpression(Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression(),
                        new NamedNode("tableSwitchCaseValue", new PrimitiveExpression(Expression.MYSTERY_OFFSET, PrimitiveExpression.ANY_VALUE)).toExpression()
                    )
                ),
                new OptionalNode(new BreakStatement(Expression.MYSTERY_OFFSET)).toStatement()
            )
        );

        final IfElseStatement additionalTest = new IfElseStatement(Expression.MYSTERY_OFFSET,
            new InvocationExpression(
                Expression.MYSTERY_OFFSET,
                new MemberReferenceExpression(
                    Expression.MYSTERY_OFFSET,
                    new IdentifierExpressionBackReference("input").toExpression(),
                    "equals"
                ),
                new NamedNode("stringValue", new PrimitiveExpression( Expression.MYSTERY_OFFSET, Pattern.ANY_STRING)).toExpression()
            ),
            new BlockStatement(
                new ExpressionStatement(
                    new AssignmentExpression(
                        new IdentifierExpressionBackReference("tableSwitchInput").toExpression(),
                        new NamedNode("tableSwitchCaseValue", new PrimitiveExpression( Expression.MYSTERY_OFFSET, PrimitiveExpression.ANY_VALUE)).toExpression()
                    )
                ),
                new OptionalNode(new BreakStatement(Expression.MYSTERY_OFFSET)).toStatement()
            )
        );

        caseBody.add(test);
        caseBody.add(new Repeat(additionalTest).toStatement());
        caseBody.add(new BreakStatement(Expression.MYSTERY_OFFSET));

        CASE_BODY_PATTERN = caseBody;
    }

    // </editor-fold>

    @Override
    @SuppressWarnings("ConstantConditions")
    public Void visitSwitchStatement(final SwitchStatement node, final Void data) {
        super.visitSwitchStatement(node, data);

        final Statement previous = node.getPreviousStatement();

        if (previous == null || previous.isNull()) {
            return null;
        }

        Statement next = node.getNextStatement();

        if (next == null || next.isNull()) {
            return null;
        }

        if (!(next instanceof SwitchStatement)) {
            //
            // There may be a variable declaration between the two switches to hold the
            // "return value" of the second switch.
            //

            next = next.getNextStatement();

            if (next == null || next.isNull()) {
                return null;
            }
        }

        if (!(next instanceof SwitchStatement)) {
            return null;
        }

        final Match m1 = TABLE_SWITCH_INPUT.match(previous);

        if (!m1.success()) {
            return null;
        }

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

        final Map<Integer, List<String>> tableInputMap = new LinkedHashMap<>();

        IdentifierExpression tableSwitchInput = null;

        for (final SwitchSection section : node.getSwitchSections()) {
            final Match m3 = CASE_BODY_PATTERN.match(section.getStatements().firstOrNullObject());

            if (!m3.success()) {
                return null;
            }

            if (tableSwitchInput == null) {
                tableSwitchInput = first(m3.<IdentifierExpression>get("tableSwitchInput"));
                assert tableSwitchInput != null;
            }

            final List<PrimitiveExpression> stringValues = toList(m3.<PrimitiveExpression>get("stringValue"));
            final List<PrimitiveExpression> tableSwitchCaseValues = toList(m3.<PrimitiveExpression>get("tableSwitchCaseValue"));

            if (stringValues.isEmpty() || stringValues.size() != tableSwitchCaseValues.size()) {
                return null;
            }

            for (int i = 0; i < stringValues.size(); i++) {
                final PrimitiveExpression stringValue = stringValues.get(i);
                final PrimitiveExpression tableSwitchCaseValue = tableSwitchCaseValues.get(i);

                if (!(tableSwitchCaseValue.getValue() instanceof Integer)) {
                    return null;
                }

                final Integer k = (Integer) tableSwitchCaseValue.getValue();
                final String v = (String) stringValue.getValue();

                List<String> list = tableInputMap.get(k);

                if (list == null) {
                    tableInputMap.put(k, list = new ArrayList<>());
                }

                list.add(v);
            }
        }

        if (tableSwitchInput == null) {
            return null;
        }

        final SwitchStatement tableSwitch = (SwitchStatement) next;

        if (!tableSwitchInput.matches(tableSwitch.getExpression())) {
            return null;
        }

        final boolean allCasesFound = all(
            tableSwitch.getSwitchSections(),
            new Predicate<SwitchSection>() {
                @Override
                public boolean test(final SwitchSection s) {
                    return !s.getCaseLabels().isEmpty() &&
                           all(
                               s.getCaseLabels(),
                               new Predicate<CaseLabel>() {
                                   @Override
                                   public boolean test(final CaseLabel c) {
                                       return c.getExpression().isNull() ||
                                              (c.getExpression() instanceof PrimitiveExpression &&
                                               ((PrimitiveExpression) c.getExpression()).getValue() instanceof Integer &&
                                               tableInputMap.containsKey(((PrimitiveExpression) c.getExpression()).getValue()));
                                   }
                               }
                           );
                }
            }
        );

        if (!allCasesFound) {
            return null;
        }

        final AstNode newInput = first(m2.<AstNode>get("target"));

        newInput.remove();
        tableSwitch.getExpression().replaceWith(newInput);

        for (final SwitchSection s : tableSwitch.getSwitchSections()) {
            for (final CaseLabel c : s.getCaseLabels()) {
                if (c.getExpression() == null || c.getExpression().isNull()) {
                    continue;
                }

                final PrimitiveExpression test = (PrimitiveExpression) c.getExpression();
                final Integer testValue = (Integer) test.getValue();
                final List<String> stringValues = tableInputMap.get(testValue);

                assert stringValues != null && !stringValues.isEmpty();

                test.setValue(stringValues.get(0));

                CaseLabel insertionPoint = c;

                for (int i = 1; i < stringValues.size(); i++) {
                    final CaseLabel newLabel = new CaseLabel(new PrimitiveExpression( Expression.MYSTERY_OFFSET, stringValues.get(i)));
                    s.getCaseLabels().insertAfter(insertionPoint, newLabel);
                    insertionPoint = newLabel;
                }
            }
        }

        node.remove();
        previous.remove();

        return null;
    }
}
