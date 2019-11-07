package com.strobel.decompiler.patterns;

import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.languages.java.ast.AssignmentExpression;
import com.strobel.decompiler.languages.java.ast.AssignmentOperatorType;
import com.strobel.decompiler.languages.java.ast.Expression;

import java.util.ArrayDeque;

public class AssignmentChain extends Pattern {
    private final INode _valuePattern;
    private final INode _targetPattern;


    public AssignmentChain(final INode targetPattern, final INode valuePattern) {
        _targetPattern = VerifyArgument.notNull(targetPattern, "targetPattern");
        _valuePattern = VerifyArgument.notNull(valuePattern, "valuePattern");
    }

    public final INode getTargetPattern() {
        return _targetPattern;
    }

    public final INode getValuePattern() {
        return _valuePattern;
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof AssignmentExpression) {
            final ArrayDeque<AssignmentExpression> assignments = new ArrayDeque<>();

            INode current = other;

            final int checkPoint = match.getCheckPoint();

            while (current instanceof AssignmentExpression &&
                   ((AssignmentExpression) current).getOperator() == AssignmentOperatorType.ASSIGN) {

                final AssignmentExpression assignment = (AssignmentExpression) current;
                final Expression target = assignment.getLeft();

                if (!_targetPattern.matches(target, match)) {
                    assignments.clear();
                    match.restoreCheckPoint(checkPoint);
                    break;
                }

                assignments.addLast(assignment);
                current = assignment.getRight();
            }

            if (assignments.isEmpty() || !_valuePattern.matches(assignments.getLast().getRight(), match)) {
                match.restoreCheckPoint(checkPoint);
                return false;
            }

            return true;
        }

        return false;
    }
}
