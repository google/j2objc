package com.strobel.expressions;

import com.strobel.core.StringUtilities;
import com.strobel.functions.Block;
import com.strobel.reflection.MemberInfo;
import com.strobel.reflection.PrimitiveTypes;
import com.strobel.reflection.Type;
import com.strobel.reflection.Types;
import com.strobel.util.ContractUtils;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;

import static java.lang.String.format;

final class DebugViewWriter extends ExpressionVisitor {
    private final static int FLOW_NONE = 0x0000;
    private final static int FLOW_SPACE = 0x0001;
    private final static int FLOW_NEW_LINE = 0x0002;
    private final static int FLOW_BREAK = 0x8000;

    private final static int TAB_SIZE = 4;
    private final static int MAX_COLUMN = 160;

    private final StringBuilder _out;

    private int _column;
    private int _delta;
    private int _flow;

    private DebugViewWriter(final StringBuilder out) {
        _out = out;
    }

    // All the unique lambda expressions in the ET, will be used for displaying all
    // the lambda definitions.
    private final Deque<LambdaExpression> _lambdas = new ArrayDeque<>();

    // Associate every unique anonymous LambdaExpression in the tree with an integer.
    // The id is used to create a name for the anonymous lambda.
    //
    private final Map<LambdaExpression, Integer> _lambdaIds = new IdentityHashMap<>();

    // Associate every unique anonymous parameter or variable in the tree with an integer.
    // The id is used to create a name for the anonymous parameter or variable.
    //
    private final Map<ParameterExpression, Integer> _paramIds = new IdentityHashMap<>();

    // Associate every unique anonymous LabelTarget in the tree with an integer.
    // The id is used to create a name for the anonymous LabelTarget.
    //
    private final Map<LabelTarget, Integer> _labelIds = new IdentityHashMap<>();

    private final Block<? extends Expression> VISITOR_BLOCK = new Block<Expression>() {
        @Override
        public void accept(final Expression input) {
            visit(input);
        }
    };

    @SuppressWarnings("unchecked")
    private <T extends Expression> Block<T> visitorBlock() {
        return (Block<T>) VISITOR_BLOCK;
    }

    private int base() {
        return 0;
    }

    private int delta() {
        return _delta;
    }

    private int depth() {
        return base() + delta();
    }

    private void indent() {
        _delta += TAB_SIZE;
    }

    private void unindent() {
        _delta = Math.max(0, _delta - TAB_SIZE);
    }

    private void newLine() {
        _flow = FLOW_NEW_LINE;
    }

    // <editor-fold defaultstate="collapsed" desc="Unique ID Management">

    private static <T> int getId(final T e, final Map<T, Integer> ids) {
        Integer id = ids.get(e);
        if (id == null) {
            // e is met the first time
            id = ids.size() + 1;
            ids.put(e, id);
        }
        return id;
    }

    private int getLambdaId(final LambdaExpression le) {
        assert StringUtilities.isNullOrEmpty(le.getName());
        return getId(le, _lambdaIds);
    }

    private int getParamId(final ParameterExpression p) {
        assert StringUtilities.isNullOrEmpty(p.getName());
        return getId(p, _paramIds);
    }

    private int getLabelTargetId(final LabelTarget target) {
        assert StringUtilities.isNullOrEmpty(target.getName());
        return getId(target, _labelIds);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Output Methods">

    private void out(final char c) {
        out(FLOW_NONE, c, FLOW_NONE);
    }

    private void out(final char c, final int after) {
        out(FLOW_NONE, c, after);
    }

    private void out(final int before, final char c, final int after) {
        switch (getFlow(before)) {
            case FLOW_NONE:
                break;
            case FLOW_SPACE:
                write(" ");
                break;
            case FLOW_NEW_LINE:
                writeLine();
                write(StringUtilities.repeat(' ', depth()));
                break;
        }

        write(c);

        _flow = after;
    }

    private void out(final String s) {
        out(FLOW_NONE, s, FLOW_NONE);
    }

    private void out(final int before, final String s) {
        out(before, s, FLOW_NONE);
    }

    private void out(final String s, final int after) {
        out(FLOW_NONE, s, after);
    }

    private void out(final int before, final String s, final int after) {
        switch (getFlow(before)) {
            case FLOW_NONE:
                break;
            case FLOW_SPACE:
                write(" ");
                break;
            case FLOW_NEW_LINE:
                writeLine();
                write(StringUtilities.repeat(' ', depth()));
                break;
        }

        write(s);

        _flow = after;
    }

    private void outMember(final Expression node, final Expression instance, final MemberInfo member) {
        if (instance != null) {
            parenthesizedVisit(node, instance);
            out("." + member.getName());
        }
        else {
            // For static members, include the type name
            out(member.getDeclaringType().toString() + "." + member.getName());
        }
    }

    private void writeLine() {
        _out.append(System.lineSeparator());
        _column = 0;
    }

    private void write(final String s) {
        _out.append(s);
        _column += s.length();
    }

    private void write(final char c) {
        _out.append(c);
        _column++;
    }

    private int getFlow(int flow) {
        final int last;

        last = checkBreak(_flow);
        flow = checkBreak(flow);

        // Get the biggest flow that is requested None < Space < NewLine
        return Math.max(last, flow);
    }

    private int checkBreak(int flow) {
        if ((flow & FLOW_BREAK) != 0) {
            if (_column > (MAX_COLUMN + depth())) {
                flow = FLOW_NEW_LINE;
            }
            else {
                flow &= ~FLOW_BREAK;
            }
        }
        return flow;
    }

    // </editor-fold>

    static void writeTo(final Expression node, final StringBuilder writer) {
        assert node != null;
        assert writer != null;

        new DebugViewWriter(writer).writeTo(node);
    }

    private void writeTo(final Expression node) {
        if (node instanceof LambdaExpression<?>) {
            writeLambda((LambdaExpression<?>) node);
        }
        else {
            visit(node);
        }

        //
        // Output all lambda expression definitions.
        // in the order of their appearances in the tree.
        //
        while (!_lambdas.isEmpty()) {
            writeLine();
            writeLine();
            writeLambda(_lambdas.removeFirst());
        }
    }

    private void writeLambda(final LambdaExpression lambda) {
        out(
            format(
                ".Lambda %s<%s>",
                getLambdaName(lambda),
                lambda.getType()
            )
        );

        visitDeclarations(lambda.getParameters());

        out(FLOW_SPACE, "{", FLOW_NEW_LINE);
        indent();
        visit(lambda.getBody());
        unindent();
        out(FLOW_NEW_LINE, "}");
    }

    private <T extends Expression> void visitExpressions(final char open, final ExpressionList<T> expressions) {
        visitExpressions(open, ',', expressions);
    }

    private <T extends Expression> void visitExpressions(final char open, final char separator, final ExpressionList<T> expressions) {
        visitExpressions(open, separator, expressions, this.<T>visitorBlock());
    }

    private void visitDeclarations(final ExpressionList<ParameterExpression> expressions) {
        visitExpressions(
            '(', ',',
            expressions,
            new Block<ParameterExpression>() {
                @Override
                public void accept(final ParameterExpression variable) {
                    out(variable.getType().toString());
                    out(" ");
                    visitParameter(variable);
                }
            }
        );
    }

    private <T extends Expression> void visitExpressions(final char open, final char separator, final ExpressionList<T> expressions, final Block<T> visit) {
        out(open);

        if (expressions != null) {
            indent();

            boolean isFirst = true;

            for (final T e : expressions) {
                if (isFirst) {
                    if (open == '{' || expressions.size() > 1) {
                        newLine();
                    }
                    isFirst = false;
                }
                else {
                    out(separator, FLOW_NEW_LINE);
                }
                visit.accept(e);
            }

            unindent();
        }

        final char close;

        switch (open) {
            case '(':
                close = ')';
                break;
            case '{':
                close = '}';
                break;
            case '[':
                close = ']';
                break;
            case '<':
                close = '>';
                break;
            default:
                throw ContractUtils.unreachable();
        }

        if (open == '{') {
            newLine();
        }

        out(close, FLOW_BREAK);
    }

    private void writeLabel(final LabelTarget target) {
        out(format(".LabelTarget %s:", getLabelTargetName(target)));
    }
    // <editor-fold defaultstate="collapsed" desc="Helper Methods">

    private static boolean isSimpleExpression(final Expression node) {
        if (node instanceof BinaryExpression) {
            final BinaryExpression binary = (BinaryExpression) node;

            return !(binary.getLeft() instanceof BinaryExpression ||
                     binary.getRight() instanceof BinaryExpression);
        }

        return false;
    }

    private static String getConstantValueSuffix(final Type<?> type) {
        if (type.isPrimitive()) {
            switch (type.getKind()) {
                case BYTE:
                    return "b";
                case SHORT:
                    return "s";
                case LONG:
                    return "L";
                case FLOAT:
                    return "f";
                case DOUBLE:
                    return "d";
            }
        }
        else if (type == Types.BigDecimal) {
            return "m";
        }
        return null;
    }

    private static boolean containsWhiteSpace(final String name) {
        for (int i = 0, n = name.length(); i < n; i++) {
            if (Character.isWhitespace(name.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static String quoteName(final String name) {
        return format("'%s'", name);
    }

    private static String getDisplayName(final String name) {
        if (containsWhiteSpace(name)) {
            //
            // Quote names containing whitespace.
            //
            return quoteName(name);
        }
        else {
            return name;
        }
    }

    private String getLambdaName(final LambdaExpression lambda) {
        if (StringUtilities.isNullOrEmpty(lambda.getName())) {
            return "#Lambda" + getLambdaId(lambda);
        }
        return getDisplayName(lambda.getName());
    }

    private String getLabelTargetName(final LabelTarget target) {
        if (StringUtilities.isNullOrEmpty(target.getName())) {
            // Create the label target name as #Label1, #Label2, etc.
            return format("#Label%s", getLabelTargetId(target));
        }
        else {
            return getDisplayName(target.getName());
        }
    }

    private String arrayToString(final Object value) {
        final Type<Object> type = Type.getType(value);

        if (!type.isArray()) {
            return value.toString();
        }

        switch (type.getKind()) {
            case BOOLEAN:
                return Arrays.toString((boolean[]) value);
            case BYTE:
                return Arrays.toString((byte[])value);
            case SHORT:
                return Arrays.toString((short[])value);
            case INT:
                return Arrays.toString((int[])value);
            case LONG:
                return Arrays.toString((long[])value);
            case CHAR:
                return Arrays.toString((char[])value);
            case FLOAT:
                return Arrays.toString((float[])value);
            case DOUBLE:
                return Arrays.toString((double[])value);
            default:
                return Arrays.toString((Object[])value);
        }
    }

    private void parenthesizedVisit(final Expression parent, final Expression nodeToVisit) {
        if (needsParentheses(parent, nodeToVisit)) {
            out("(");
            visit(nodeToVisit);
            out(")");
        }
        else {
            visit(nodeToVisit);
        }
    }

    private static boolean needsParentheses(final Expression parent, final Expression child) {
        assert parent != null;

        if (child == null) {
            return false;
        }

        //
        // Some nodes always have parentheses because of how they are displayed, for example:
        // ".Unbox(obj.Foo)"
        //
        switch (parent.getNodeType()) {
            case Increment:
            case Decrement:
            case IsTrue:
            case IsFalse:
            case Unbox:
            case Convert:
                return true;
        }

        final int childOpPrecedence = getOperatorPrecedence(child);
        final int parentOpPrecedence = getOperatorPrecedence(parent);

        if (childOpPrecedence == parentOpPrecedence) {
            //
            // When parent op and child op has the same precedence, we want to be a little
            // conservative to have more clarity.  Parentheses are not needed if:
            //
            //   1) Both ops are &&, ||, &, |, or ^, all of them are the only op that has
            //      the precedence.
            //
            //   2) Parent op is + or *, e.g. x + (y - z) can be simplified to x + y - z.
            //
            //   3) Parent op is -, / or %, and the child is the left operand.  In this case,
            //      if left and right operand are the same, we don't remove parenthesis,
            //      e.g., (x + y) - (x + y)
            // 
            switch (parent.getNodeType()) {
                case AndAlso:
                case OrElse:
                case And:
                case Or:
                case ExclusiveOr:
                    // Since these ops are the only ones on their precedence,
                    // the child op must be the same.
                    assert child.getNodeType() == parent.getNodeType();
                    // We remove the parenthesis, e.g. x && y && z
                    return false;

                case Add:
                case Multiply:
                    return false;

                case Subtract:
                case Divide:
                case Modulo:
                    assert parent instanceof BinaryExpression;
                    final BinaryExpression binary = (BinaryExpression) parent;
                    // Need to have parenthesis for the right operand.
                    return child == binary.getRight();
            }

            return true;
        }

        //
        // Special case: negate of a constant needs parentheses, to
        // disambiguate it from a negative constant.
        //
        if (child.getNodeType() == ExpressionType.Constant &&
            parent.getNodeType() == ExpressionType.Negate) {

            return true;
        }

        //
        // If the parent op has higher precedence, need parentheses for the child.
        //
        return childOpPrecedence < parentOpPrecedence;
    }

    private static int getOperatorPrecedence(final Expression node) {
        //
        // Roughly matches Java operator precedence, with some additional
        // operators. Also, things which are not binary/unary expressions,
        // such as conditional and type testing, don't use this mechanism.
        //
        switch (node.getNodeType()) {
            // Assignment
            case Assign:
            case ExclusiveOrAssign:
            case AddAssign:
            case SubtractAssign:
            case DivideAssign:
            case ModuloAssign:
            case MultiplyAssign:
            case LeftShiftAssign:
            case RightShiftAssign:
            case AndAssign:
            case OrAssign:
            case Coalesce:
                return 1;

            // Conditional (?:) would go here

            // Conditional OR
            case OrElse:
                return 2;

            // Conditional AND
            case AndAlso:
                return 3;

            // Logical OR
            case Or:
                return 4;

            // Logical XOR
            case ExclusiveOr:
                return 5;

            // Logical AND
            case And:
                return 6;

            // Equality
            case Equal:
            case NotEqual:
                return 7;

            // Relational, type testing
            case GreaterThan:
            case LessThan:
            case GreaterThanOrEqual:
            case LessThanOrEqual:
            case InstanceOf:
            case TypeEqual:
                return 8;

            // Shift
            case LeftShift:
            case RightShift:
            case UnsignedRightShift:
                return 9;

            // Additive
            case Add:
            case Subtract:
                return 10;

            // Multiplicative
            case Divide:
            case Modulo:
            case Multiply:
                return 11;

            // Unary
            case Negate:
            case UnaryPlus:
            case Not:
            case Convert:
            case ConvertChecked:
            case PreIncrementAssign:
            case PreDecrementAssign:
            case OnesComplement:
            case Increment:
            case Decrement:
            case IsTrue:
            case IsFalse:
            case Unbox:
            case Throw:
                return 12;

            // Primary, which includes all other node types:
            //   member access, calls, indexing, new.
            case PostIncrementAssign:
            case PostDecrementAssign:
            default:
                return 14;

            // These aren't expressions, so never need parentheses:
            //   constants, variables
            case Constant:
            case Parameter:
                return 15;
        }
    }

    // </editor-fold>

    @Override
    protected Expression visitBinary(final BinaryExpression node) {
        if (node.getNodeType() == ExpressionType.ArrayIndex) {
            parenthesizedVisit(node, node.getLeft());
            out("[");
            visit(node.getRight());
            out("]");
        }
        else if (node.getNodeType() == ExpressionType.ArrayLength) {
            parenthesizedVisit(node, node.getLeft());
            out(".length");
        }
        else {
            final boolean parenthesizeLeft = needsParentheses(node, node.getLeft());
            final boolean parenthesizeRight = needsParentheses(node, node.getRight());

            final String op;
            int beforeOp = FLOW_SPACE;

            switch (node.getNodeType()) {
                case Assign:
                    op = "=";
                    break;
                case Equal:
                case ReferenceEqual:
                    op = "==";
                    break;
                case NotEqual:
                case ReferenceNotEqual:
                    op = "!=";
                    break;
                case AndAlso:
                    op = "&&";
                    beforeOp = FLOW_BREAK | FLOW_SPACE;
                    break;
                case OrElse:
                    op = "||";
                    beforeOp = FLOW_BREAK | FLOW_SPACE;
                    break;
                case GreaterThan:
                    op = ">";
                    break;
                case LessThan:
                    op = "<";
                    break;
                case GreaterThanOrEqual:
                    op = ">=";
                    break;
                case LessThanOrEqual:
                    op = "<=";
                    break;
                case Add:
                    op = "+";
                    break;
                case AddAssign:
                    op = "+=";
                    break;
                case Subtract:
                    op = "-";
                    break;
                case SubtractAssign:
                    op = "-=";
                    break;
                case Divide:
                    op = "/";
                    break;
                case DivideAssign:
                    op = "/=";
                    break;
                case Modulo:
                    op = "%";
                    break;
                case ModuloAssign:
                    op = "%=";
                    break;
                case Multiply:
                    op = "*";
                    break;
                case MultiplyAssign:
                    op = "*=";
                    break;
                case LeftShift:
                    op = "<<";
                    break;
                case LeftShiftAssign:
                    op = "<<=";
                    break;
                case RightShift:
                    op = ">>";
                    break;
                case UnsignedRightShift:
                    op = ">>>";
                    break;
                case RightShiftAssign:
                    op = ">>=";
                    break;
                case UnsignedRightShiftAssign:
                    op = ">>>=";
                    break;
                case And:
                    op = "&";
                    break;
                case AndAssign:
                    op = "&=";
                    break;
                case Or:
                    op = "|";
                    break;
                case OrAssign:
                    op = "|=";
                    break;
                case ExclusiveOr:
                    op = "^";
                    break;
                case ExclusiveOrAssign:
                    op = "^=";
                    break;
                case Coalesce:
                    op = "??";
                    break;

                default:
                    throw ContractUtils.unreachable();
            }

            if (parenthesizeLeft) {
                out("(", FLOW_NONE);
            }

            visit(node.getLeft());

            if (parenthesizeLeft) {
                out(FLOW_NONE, ")", FLOW_BREAK);
            }

            out(beforeOp, op, FLOW_SPACE | FLOW_BREAK);

            if (parenthesizeRight) {
                out("(", FLOW_NONE);
            }

            visit(node.getRight());

            if (parenthesizeRight) {
                out(FLOW_NONE, ")", FLOW_BREAK);
            }
        }
        return node;
    }

    @Override
    protected Expression visitParameter(final ParameterExpression node) {
        //
        // Have '$' for the DebugView of ParameterExpressions
        //
        out("$");

        if (StringUtilities.isNullOrEmpty(node.getName())) {
            //
            // If no name if provided, generate a name as $var1, $var2.
            // No guarantee for not having name conflicts with user provided variable names.
            //
            final int id = getParamId(node);
            out("var" + id);
        }
        else {
            out(getDisplayName(node.getName()));
        }

        return node;
    }

    @Override
    protected <T> LambdaExpression<T> visitLambda(final LambdaExpression<T> node) {
        out(
            format(
                ".Lambda %s<%s>",
                getLambdaName(node),
                node.getType()
            )
        );

        // N^2 performance, for keeping the order of the lambdas.
        if (!_lambdas.contains(node)) {
            _lambdas.addLast(node);
        }

        return node;
    }

    @Override
    protected Expression visitConditional(final ConditionalExpression node) {
        if (isSimpleExpression(node.getTest())) {
            out(".If (");
            visit(node.getTest());
            out(") {", FLOW_NEW_LINE);
        }
        else {
            out(".If (", FLOW_NEW_LINE);
            indent();
            visit(node.getTest());
            unindent();
            out(FLOW_NEW_LINE, ") {", FLOW_NEW_LINE);
        }
        indent();
        visit(node.getIfTrue());
        unindent();
        out(FLOW_NEW_LINE, "} .Else {", FLOW_NEW_LINE);
        indent();
        visit(node.getIfFalse());
        unindent();
        out(FLOW_NEW_LINE, "}");
        return node;
    }

    @Override
    protected Expression visitConstant(final ConstantExpression node) {
        final Object value = node.getValue();

        if (value == null) {
            out("null");
        }
        else if (value instanceof String &&
                 node.getType() == Types.String) {

            out(StringUtilities.escape((String) value, true));
        }
        else if (value instanceof Character &&
                 node.getType() == PrimitiveTypes.Character) {

            out(StringUtilities.escape((char) value, true));
        }
        else if (value instanceof Integer && node.getType() == PrimitiveTypes.Integer ||
                 value instanceof Boolean && node.getType() == PrimitiveTypes.Boolean) {

            out(value.toString());
        }
        else if (value instanceof Class &&
                 "java/lang/Class".equals(node.getType().getInternalName())) {

            out(((Class<?>) value).getName() + ".class");
        }
        else if (value instanceof Type<?> &&
                 "com/strobel/reflection/Type".equals(node.getType().getInternalName())) {

            out(((Type<?>) value).getFullName() + ".class");
        }
        else {
            final String suffix = getConstantValueSuffix(node.getType());

            if (suffix != null) {
                out(String.valueOf(value));
                out(suffix);
            }
            else {
                final String toString = value.getClass().isArray() ? arrayToString(value)
                                                                   : String.valueOf(value);
                out(
                    format(
                        ".Constant<%s>(%s)",
                        node.getType().toString(),
                        toString
                    )
                );
            }
        }

        return node;
    }

    @Override
    protected Expression visitRuntimeVariables(final RuntimeVariablesExpression node) {
        out(".RuntimeVariables");
        visitExpressions('(', node.getVariables());
        return node;
    }

    @Override
    protected Expression visitMember(final MemberExpression node) {
        outMember(node, node.getTarget(), node.getMember());
        return node;
    }

    @Override
    protected Expression visitInvocation(final InvocationExpression node) {
        out(".Invoke ");
        parenthesizedVisit(node, node.getExpression());
        visitExpressions('(', node.getArguments());
        return node;
    }

    @Override
    protected Expression visitMethodCall(final MethodCallExpression node) {
        out(".Call ");
        if (node.getTarget() != null) {
            parenthesizedVisit(node, node.getTarget());
        }
        else if (node.getMethod().getDeclaringType() != null) {
            out(node.getMethod().getDeclaringType().toString());
        }
        else {
            out("<UnknownType>");
        }
        out(".");
        out(node.getMethod().getName());
        visitExpressions('(', node.getArguments());
        return node;
    }

    @Override
    protected Expression visitNewArray(final NewArrayExpression node) {
        if (node.getNodeType() == ExpressionType.NewArrayBounds) {
            // .NewArray MyType[expr1, expr2]
            out(".NewArray " + node.getType().getElementType().toString());
            visitExpressions('[', node.getExpressions());
        }
        else {
            // .NewArray MyType {expr1, expr2}
            out(".NewArray " + node.getType().toString(), FLOW_SPACE);
            visitExpressions('{', node.getExpressions());
        }
        return node;
    }

    @Override
    protected Expression visitNew(final NewExpression node) {
        out(".New " + node.getType().toString());
        visitExpressions('(', node.getArguments());
        return node;
    }

    @Override
    protected Expression visitDefaultValue(final DefaultValueExpression node) {
        out(".Default(" + node.getType().toString() + ")");
        return node;
    }

    @Override
    protected Expression visitExtension(final Expression node) {
        out(format(".Extension<%s>", node.getClass().getName()));

        if (node.canReduce()) {
            out(FLOW_SPACE, "{", FLOW_NEW_LINE);
            indent();
            visit(node.reduce());
            unindent();
            out(FLOW_NEW_LINE, "}");
        }

        return node;
    }

    @Override
    protected Expression visitLabel(final LabelExpression node) {
        out(".Label", FLOW_NEW_LINE);
        indent();
        visit(node.getDefaultValue());
        unindent();
        newLine();
        writeLabel(node.getTarget());
        return node;
    }

    @Override
    protected LabelTarget visitLabelTarget(final LabelTarget node) {
        writeLabel(node);
        return node;
    }

    @Override
    protected Expression visitGoto(final GotoExpression node) {
        out("." + node.getKind().toString(), FLOW_SPACE);
        out(getLabelTargetName(node.getTarget()), FLOW_SPACE);
        out("{", FLOW_SPACE);
        visit(node.getValue());
        out(FLOW_SPACE, "}");
        return node;
    }

    @Override
    protected Expression visitLoop(final LoopExpression node) {
        out(".Loop", FLOW_SPACE);
        if (node.getContinueTarget() != null) {
            writeLabel(node.getContinueTarget());
        }
        out(" {", FLOW_NEW_LINE);
        indent();
        visit(node.getBody());
        unindent();
        out(FLOW_NEW_LINE, "}");
        if (node.getBreakTarget() != null) {
            out("", FLOW_NEW_LINE);
            writeLabel(node.getBreakTarget());
        }
        return node;
    }

    @Override
    protected Expression visitForEach(final ForEachExpression node) {
        out(".ForEach", FLOW_SPACE);
        out("(");
        visit(node.getVariable());
        out(" in ");
        visit(node.getSequence());
        out(") {", FLOW_NEW_LINE);
        indent();
        visit(node.getBody());
        unindent();
        out(FLOW_NEW_LINE, "}");
        return node;
    }

    @Override
    protected Expression visitFor(final ForExpression node) {
        out(".For", FLOW_SPACE);
        out("(");
        visit(node.getVariable());
        out(" = ");
        visit(node.getInitializer());
        out("; ");
        visit(node.getTest());
        out("; ");
        visit(node.getStep());
        out(") {", FLOW_NEW_LINE);
        indent();
        visit(node.getBody());
        unindent();
        out(FLOW_NEW_LINE, "}");
        return node;
    }

    @Override
    protected Expression visitUnary(final UnaryExpression node) {
        final boolean parenthesize = needsParentheses(node, node.getOperand());

        final String op;
        int beforeOp = FLOW_SPACE;
        boolean trailing = false;
        Type<?> secondOp = null;

        switch (node.getNodeType()) {
            case Negate:
                op = "-";
                break;

            case Convert:
                secondOp = node.getType();
                // fall through...

            case Not:
            case IsFalse:
            case IsTrue:
            case OnesComplement:
            case ArrayLength:
            case IsNull:
            case IsNotNull:
            case Throw:
            case Unbox:
                op = "." + node.getNodeType();
                break;

            case UnaryPlus:
                op = "+";
                break;

            case PostDecrementAssign:
                trailing = true;
            case PreDecrementAssign:
                op = "--";
                break;

            case PostIncrementAssign:
                trailing = true;
            case PreIncrementAssign:
                op = "++";
                break;

            default:
                throw ContractUtils.unreachable();
        }

        if (!trailing) {
            out(beforeOp, op, parenthesize ? FLOW_NONE : FLOW_SPACE | FLOW_BREAK);
        }

        if (parenthesize) {
            out("(", FLOW_NONE);
        }

        visit(node.getOperand());

        if (parenthesize) {
            out(FLOW_NONE, ")", FLOW_BREAK);
        }

        if (trailing) {
            out(beforeOp, op, parenthesize ? FLOW_NONE : FLOW_SPACE | FLOW_BREAK);
        }

        return node;
    }

    @Override
    protected Expression visitTypeBinary(final TypeBinaryExpression node) {
        visit(node.getOperand());
        out(" ");
        out(node.getNodeType() == ExpressionType.InstanceOf ? " .InstanceOf " : " .TypeEqual ");
        out(node.getType().toString());
        return node;
    }

    @Override
    protected Expression visitBlock(final BlockExpression node) {
        out(".Block");

        // Display <type> if the type of the BlockExpression is different from the
        // last expression's type in the block.
        if (node.getType() != node.getExpression(node.getExpressionCount() - 1).getType()) {
            out(format("<%s>", node.getType().toString()));
        }

        visitDeclarations(node.getVariables());
        out(" ");
        // Use ; to separate expressions in the block
        visitExpressions('{', ';', node.getExpressions());

        return node;
    }

    @Override
    protected Expression visitTry(final TryExpression node) {
        out(".Try {", FLOW_NEW_LINE);
        indent();
        visit(node.getBody());
        unindent();

        visit(
            node.getHandlers(),
            new ElementVisitor<CatchBlock>() {
                @Override
                public CatchBlock visit(final CatchBlock node) {
                    return visitCatchBlock(node);
                }
            }
        );

        if (node.getFinallyBlock() != null) {
            out(FLOW_NEW_LINE, "} .Finally {", FLOW_NEW_LINE);
            indent();
            visit(node.getFinallyBlock());
            unindent();
        }

        out(FLOW_NEW_LINE, "}");
        return node;
    }

    @Override
    protected CatchBlock visitCatchBlock(final CatchBlock node) {
        out(FLOW_NEW_LINE, "} .Catch (" + node.getTest().toString());
        if (node.getVariable() != null) {
            out(FLOW_SPACE, "");
            visitParameter(node.getVariable());
        }
        if (node.getFilter() != null) {
            out(") .If (", FLOW_BREAK);
            visit(node.getFilter());
        }
        out(") {", FLOW_NEW_LINE);
        indent();
        visit(node.getBody());
        unindent();
        return node;
    }

    @Override
    protected SwitchCase visitSwitchCase(final SwitchCase node) {
        indent();
        for (final Expression test : node.getTestValues()) {
            out(".Case (");
            visit(test);
            out("):", FLOW_NEW_LINE);
        }
        indent();
        visit(node.getBody());
        unindent();
        unindent();
        newLine();
        return node;
    }

    @Override
    protected Expression visitSwitch(final SwitchExpression node) {
        out(".Switch ");
        out("(");
        visit(node.getSwitchValue());
        out(") {", FLOW_NEW_LINE);

        visit(
            node.getCases(),
            new ElementVisitor<SwitchCase>() {
                @Override
                public SwitchCase visit(final SwitchCase node) {
                    return visitSwitchCase(node);
                }
            }
        );

        if (node.getDefaultBody() != null) {
            indent();
            out(".Default:", FLOW_NEW_LINE);
            indent();
            visit(node.getDefaultBody());
            unindent();
            unindent();
            newLine();
        }

        out("}");
        return node;
    }

    @Override
    protected Expression visitConcat(final ConcatExpression node) {
        final ExpressionList<? extends Expression> operands = node.getOperands();

        boolean first = true;

        for (final Expression operand : operands) {
            if (first) {
                first = false;
            }
            else {
                out(FLOW_SPACE, "+", FLOW_SPACE | FLOW_BREAK);
            }

            final boolean parenthesize = needsParentheses(node, operand);

            if (parenthesize) {
                out("(", FLOW_NONE);
            }

            visit(operand);

            if (parenthesize) {
                out(")", FLOW_NONE);
            }
        }

        return node;
    }
}
