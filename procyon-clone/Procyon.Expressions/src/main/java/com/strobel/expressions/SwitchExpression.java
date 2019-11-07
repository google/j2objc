/*
 * SwitchExpression.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is based on the Dynamic Language Runtime from Microsoft,
 *   Copyright (c) Microsoft Corporation.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.expressions;

import com.strobel.core.ArrayUtilities;
import com.strobel.core.ReadOnlyList;
import com.strobel.core.VerifyArgument;
import com.strobel.reflection.MethodInfo;
import com.strobel.reflection.PrimitiveTypes;
import com.strobel.reflection.Type;
import com.strobel.reflection.Types;
import com.strobel.reflection.emit.SwitchOptions;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author strobelm
 */
public final class SwitchExpression extends Expression {
    private final Type                     _type;
    private final Expression               _switchValue;
    private final ReadOnlyList<SwitchCase> _cases;
    private final Expression               _defaultBody;
    private final MethodInfo               _comparison;
    private final SwitchOptions            _options;

    public SwitchExpression(
        final Type type,
        final Expression switchValue,
        final Expression defaultBody,
        final MethodInfo comparison,
        final ReadOnlyList<SwitchCase> cases,
        final SwitchOptions options) {

        _type = VerifyArgument.notNull(type, "type");
        _switchValue = VerifyArgument.notNull(switchValue, "switchValue");
        _defaultBody = defaultBody;
        _comparison = comparison;
        _cases = VerifyArgument.notEmpty(cases, "cases");
        _options = options != null ? options : SwitchOptions.Default;
    }

    public final Expression getSwitchValue() {
        return _switchValue;
    }

    public final ReadOnlyList<SwitchCase> getCases() {
        return _cases;
    }

    public final Expression getDefaultBody() {
        return _defaultBody;
    }

    public final MethodInfo getComparison() {
        return _comparison;
    }

    public final SwitchOptions getOptions() {
        return _options;
    }

    @Override
    public final Type<?> getType() {
        return _type;
    }

    @Override
    public final ExpressionType getNodeType() {
        return ExpressionType.Switch;
    }

    @Override
    protected final Expression accept(final ExpressionVisitor visitor) {
        return visitor.visitSwitch(this);
    }

    public final SwitchExpression update(
        final Expression switchValue,
        final ReadOnlyList<SwitchCase> cases,
        final Expression defaultBody,
        final SwitchOptions options) {

        if (switchValue == _switchValue && options == _options && cases == _cases && defaultBody == _defaultBody) {
            return this;
        }

        return Expression.makeSwitch(_type, switchValue, _options, defaultBody, _comparison, cases);
    }

    @Override
    public boolean canReduce() {
        final Type<?> switchValueType = _switchValue.getType();

        return (switchValueType == Types.String && _options != SwitchOptions.PreferTrie ||
                switchValueType.isEnum());
    }

    @Override
    public Expression reduce() {
        final Type<?> switchValueType = _switchValue.getType();

        if (switchValueType == Types.String && _options != SwitchOptions.PreferTrie) {
            return rewriteStringSwitch();
        }

        if (switchValueType.isEnum()) {
            return rewriteEnumSwitch();
        }

        return this;
    }

    private Expression rewriteStringSwitch() {
        final ArrayList<Integer> hashList = new ArrayList<>();
        final HashMap<Integer, String[]> reverseHashMap = new HashMap<>();
        final HashMap<String, Integer> caseIndexMap = new HashMap<>();

        for (int i = 0, n = _cases.size(); i < n; i++) {
            final SwitchCase switchCase = _cases.get(i);

            for (final Expression testValue : switchCase.getTestValues()) {
                final String value = (String) ((ConstantExpression) testValue).getValue();
                final int hash = value.hashCode();

                final String[] oldValue = reverseHashMap.put(
                    hash,
                    ArrayUtilities.append(reverseHashMap.get(hash), value)
                );

                if (oldValue == null) {
                    hashList.add(hash);
                }

                caseIndexMap.put(value, i);
            }
        }

        final ParameterExpression caseIndex = variable(PrimitiveTypes.Integer);
        final ParameterExpression switchValue = variable(_switchValue.getType());
        final SwitchCase[] hashCases = new SwitchCase[hashList.size()];

        for (int i = 0, n = hashList.size(); i < n; i++) {
            final Integer hash = hashList.get(i);
            final String[] matchingStrings = reverseHashMap.get(hash);
            final Expression[] caseBranches = new Expression[matchingStrings.length];

            for (int j = 0; j < matchingStrings.length; j++) {
                final String matchingString = matchingStrings[j];

                caseBranches[j] = ifThen(
                    call(switchValue, "equals", constant(matchingString)),
                    assign(caseIndex, constant(caseIndexMap.get(matchingString)))
                );
            }

            final Expression caseBody;

            if (caseBranches.length == 1) {
                caseBody = caseBranches[0];
            }
            else {
                caseBody = block(caseBranches);
            }

            hashCases[i] = switchCase(
                block(caseBody),
                constant(hash)
            );
        }

        final SwitchExpression hashSwitch = makeSwitch(
            call(switchValue, "hashCode"),
            _options,
            hashCases
        );

        final SwitchCase[] resultCases = new SwitchCase[_cases.size()];

        for (int i = 0, n = _cases.size(); i < n; i++) {
            resultCases[i] = switchCase(_cases.get(i).getBody(), constant(i));
        }

        final SwitchExpression resultSwitch = makeSwitch(
            _type,
            caseIndex,
            SwitchOptions.PreferTable,
            _defaultBody,
            null,
            resultCases
        );

        return block(
            _type,
            new ParameterExpression[] { switchValue, caseIndex },
            assign(switchValue, _switchValue),
            assign(caseIndex, constant(-1)),
            hashSwitch,
            resultSwitch
        );
    }

    private Expression rewriteEnumSwitch() {
        final SwitchCase[] cases = _cases.toArray(new SwitchCase[_cases.size()]);

        for (int i = 0, n = _cases.size(); i < n; i++) {
            final SwitchCase oldCase = _cases.get(i);
            final ExpressionList<? extends Expression> oldTestValues = oldCase.getTestValues();
            final Expression[] testValues = new Expression[oldTestValues.size()];

            for (int j = 0, m = oldTestValues.size(); j < m; j++) {
                final Enum<?> enumValue = (Enum<?>) ((ConstantExpression) oldTestValues.get(j)).getValue();
                testValues[j] = constant(enumValue.ordinal());
            }

            cases[i] = switchCase(oldCase.getBody(), testValues);
        }

        return makeSwitch(
            _type,
            call(_switchValue, "ordinal"),
            _options,
            _defaultBody,
            _comparison,
            cases
        );
    }
}
