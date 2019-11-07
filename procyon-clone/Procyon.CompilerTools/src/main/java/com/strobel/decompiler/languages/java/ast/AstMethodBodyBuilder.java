/*
 * AstMethodBodyBuilder.java
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

package com.strobel.decompiler.languages.java.ast;

import com.strobel.annotations.NotNull;
import com.strobel.assembler.metadata.*;
import com.strobel.assembler.metadata.annotations.CustomAnnotation;
import com.strobel.core.ExceptionUtilities;
import com.strobel.core.Predicate;
import com.strobel.core.StringComparison;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.DecompilerHelpers;
import com.strobel.decompiler.PlainTextOutput;
import com.strobel.decompiler.ast.*;
import com.strobel.decompiler.ast.Label;
import com.strobel.decompiler.languages.Languages;
import com.strobel.decompiler.patterns.AnyNode;
import com.strobel.decompiler.patterns.Choice;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.OptionalNode;
import com.strobel.decompiler.semantics.ResolveResult;
import com.strobel.util.ContractUtils;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.strobel.core.CollectionUtilities.*;

public class AstMethodBodyBuilder {
    private final AstBuilder _astBuilder;
    private final MethodDefinition _method;
    private final MetadataParser _parser;
    private final DecompilerContext _context;
    private final Set<Variable> _localVariablesToDefine = new LinkedHashSet<>();

    private final static INode LAMBDA_BODY_PATTERN;
    private final static INode EMPTY_LAMBDA_BODY_PATTERN;

    static {
        LAMBDA_BODY_PATTERN = new Choice(
            new BlockStatement(
                new ExpressionStatement(new AnyNode("body").toExpression()),
                new OptionalNode(new ReturnStatement(Expression.MYSTERY_OFFSET)).toStatement()
            ),
            new BlockStatement(
                new ReturnStatement(Expression.MYSTERY_OFFSET, new AnyNode("body").toExpression())
            ),
            new AnyNode("body").toBlockStatement()
        );

        EMPTY_LAMBDA_BODY_PATTERN = new BlockStatement(new ReturnStatement(Expression.MYSTERY_OFFSET));
    }

    public static BlockStatement createMethodBody(
        final AstBuilder astBuilder,
        final MethodDefinition method,
        final DecompilerContext context,
        final Iterable<ParameterDeclaration> parameters) {

        VerifyArgument.notNull(astBuilder, "astBuilder");
        VerifyArgument.notNull(method, "method");
        VerifyArgument.notNull(context, "context");

        final MethodDefinition oldCurrentMethod = context.getCurrentMethod();

/*
        assert oldCurrentMethod == null ||
               oldCurrentMethod == method ||
               method.getDeclaringType().getDeclaringMethod() == oldCurrentMethod;
*/

        context.setCurrentMethod(method);

        try {
            final AstMethodBodyBuilder builder = new AstMethodBodyBuilder(astBuilder, method, context);
            return builder.createMethodBody(parameters);
        }
        catch (Throwable t) {
            return createErrorBlock(astBuilder, context, method, t);
        }
        finally {
            context.setCurrentMethod(oldCurrentMethod);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static BlockStatement createErrorBlock(
        final AstBuilder astBuilder,
        final DecompilerContext context,
        final MethodDefinition method,
        final Throwable t) {

        final BlockStatement block = new BlockStatement();

        final List<String> lines = StringUtilities.split(
            ExceptionUtilities.getStackTraceString(t),
            true,
            '\r',
            '\n'
        );

        block.addChild(new Comment(" ", CommentType.SingleLine), Roles.COMMENT);
        block.addChild(new Comment(" This method could not be decompiled.", CommentType.SingleLine), Roles.COMMENT);
        block.addChild(new Comment(" ", CommentType.SingleLine), Roles.COMMENT);

        try {
            final PlainTextOutput bytecodeOutput = new PlainTextOutput();
            final DecompilationOptions bytecodeOptions = new DecompilationOptions();

            bytecodeOptions.getSettings().setIncludeLineNumbersInBytecode(false);

            Languages.bytecode().decompileMethod(method, bytecodeOutput, bytecodeOptions);

            final List<String> bytecodeLines = StringUtilities.split(
                bytecodeOutput.toString(),
                true,
                '\r',
                '\n'
            );

            block.addChild(new Comment(" Original Bytecode:", CommentType.SingleLine), Roles.COMMENT);
            block.addChild(new Comment(" ", CommentType.SingleLine), Roles.COMMENT);

            for (int i = 4; i < bytecodeLines.size(); i++) {
                final String line = StringUtilities.removeLeft(bytecodeLines.get(i), "      ");

                block.addChild(new Comment(line.replace("\t", "  "), CommentType.SingleLine), Roles.COMMENT);
            }

            block.addChild(new Comment(" ", CommentType.SingleLine), Roles.COMMENT);
        }
        catch (final Throwable ignored) {
            block.addChild(new Comment(" Could not show original bytecode, likely due to the same error.", CommentType.SingleLine), Roles.COMMENT);
            block.addChild(new Comment(" ", CommentType.SingleLine), Roles.COMMENT);
        }

        if (context.getSettings().getIncludeErrorDiagnostics()) {
            block.addChild(new Comment(" The error that occurred was:", CommentType.SingleLine), Roles.COMMENT);
            block.addChild(new Comment(" ", CommentType.SingleLine), Roles.COMMENT);

            for (final String line : lines) {
                block.addChild(new Comment(" " + line.replace("\t", "    "), CommentType.SingleLine), Roles.COMMENT);
            }

            block.addChild(new Comment(" ", CommentType.SingleLine), Roles.COMMENT);
        }

        try {
            final TypeDefinition currentType = astBuilder.getContext().getCurrentType();
            final IMetadataResolver resolver = currentType != null ? currentType.getResolver() : MetadataSystem.instance();
            final MetadataParser parser = new MetadataParser(resolver);

            block.add(
                new ThrowStatement(
                    new ObjectCreationExpression(
                           Expression.MYSTERY_OFFSET,
                        astBuilder.convertType(parser.parseTypeDescriptor("java/lang/IllegalStateException")),
                        new PrimitiveExpression( Expression.MYSTERY_OFFSET, "An error occurred while decompiling this method.")
                    )
                )
            );
        }
        catch (Throwable ignored) {
            block.add(new EmptyStatement());
        }

        return block;
    }

    private AstMethodBodyBuilder(final AstBuilder astBuilder, final MethodDefinition method, final DecompilerContext context) {
        _astBuilder = astBuilder;
        _method = method;
        _context = context;
        _parser = new MetadataParser(method.getDeclaringType());
    }

    @SuppressWarnings("ConstantConditions")
    private BlockStatement createMethodBody(final Iterable<ParameterDeclaration> parameters) {
        final MethodBody body = _method.getBody();

        if (body == null) {
            return null;
        }

        final Block method = new Block();

        method.getBody().addAll(
            com.strobel.decompiler.ast.AstBuilder.build(body, true, _context)
        );

        AstOptimizer.optimize(_context, method);

        final Set<ParameterDefinition> unmatchedParameters = new LinkedHashSet<>(_method.getParameters());
        final Set<Variable> methodParameters = new LinkedHashSet<>();
        final Set<Variable> localVariables = new LinkedHashSet<>();

        final List<com.strobel.decompiler.ast.Expression> expressions = method.getSelfAndChildrenRecursive(
            com.strobel.decompiler.ast.Expression.class
        );

        for (final com.strobel.decompiler.ast.Expression e : expressions) {
            final Object operand = e.getOperand();

            if (operand instanceof Variable) {
                final Variable variable = (Variable) operand;

                if (variable.isParameter()) {
                    methodParameters.add(variable);
                    unmatchedParameters.remove(variable.getOriginalParameter());
                }
                else {
                    localVariables.add(variable);
                }
            }
        }

        final List<Variable> orderedParameters = new ArrayList<>();

        for (final ParameterDefinition p : unmatchedParameters) {
            final Variable v = new Variable();
            v.setName(p.getName());
            v.setOriginalParameter(p);
            v.setType(p.getParameterType());
            orderedParameters.add(v);
        }

        for (final Variable parameter : methodParameters) {
            orderedParameters.add(parameter);
        }

        Collections.sort(
            orderedParameters,
            new Comparator<Variable>() {
                @Override
                public int compare(@NotNull final Variable p1, @NotNull final Variable p2) {
                    return Integer.compare(p1.getOriginalParameter().getSlot(), p2.getOriginalParameter().getSlot());
                }
            }
        );

        final List<CatchBlock> catchBlocks = method.getSelfAndChildrenRecursive(
            CatchBlock.class
        );

        for (final CatchBlock catchBlock : catchBlocks) {
            final Variable exceptionVariable = catchBlock.getExceptionVariable();

            if (exceptionVariable != null) {
                localVariables.add(exceptionVariable);
            }
        }

        NameVariables.assignNamesToVariables(_context, orderedParameters, localVariables, method);

        for (final Variable p : orderedParameters) {
            final ParameterDeclaration declaration = firstOrDefault(
                parameters,
                new Predicate<ParameterDeclaration>() {
                    @Override
                    public boolean test(final ParameterDeclaration pd) {
                        return pd.getUserData(Keys.PARAMETER_DEFINITION) == p.getOriginalParameter();
                    }
                }
            );

            if (declaration != null) {
                declaration.setName(p.getName());
            }
        }

        final BlockStatement astBlock = transformBlock(method);

        CommentStatement.replaceAll(astBlock);

        final AstNodeCollection<Statement> statements = astBlock.getStatements();
        final Statement insertionPoint = firstOrDefault(statements);

        for (final Variable v : _localVariablesToDefine) {
            TypeReference variableType = v.getType();

            final TypeDefinition resolvedType = variableType.resolve();

            if (resolvedType != null && resolvedType.isAnonymous()) {
                if (resolvedType.getExplicitInterfaces().isEmpty()) {
                    variableType = resolvedType.getBaseType();
                }
                else {
                    variableType = resolvedType.getExplicitInterfaces().get(0);
                }
            }

            final AstType type = _astBuilder.convertType(variableType);
            final VariableDeclarationStatement declaration = new VariableDeclarationStatement(type, v.getName(), Expression.MYSTERY_OFFSET);

            declaration.putUserData(Keys.VARIABLE, v);
            statements.insertBefore(insertionPoint, declaration);
        }

        return astBlock;
    }

    private BlockStatement transformBlock(final Block block) {
        final BlockStatement astBlock = new BlockStatement();

        if (block != null) {
            final List<Node> children = block.getChildren();
            for (int i = 0; i < children.size(); i++) {
                final Node node = children.get(i);

                final Statement statement = transformNode(
                    node,
                    i < children.size() - 1 ? children.get(i + 1) : null
                );

                astBlock.getStatements().add(statement);

                if (statement instanceof SynchronizedStatement) {
                    i++;
                }
            }
        }

        return astBlock;
    }

    private Statement transformNode(final Node node, final Node next) {
        if (node instanceof Label) {
            return new LabelStatement(Expression.MYSTERY_OFFSET,((Label) node).getName());
        }

        if (node instanceof Block) {
            return transformBlock((Block) node);
        }

        if (node instanceof com.strobel.decompiler.ast.Expression) {
            final com.strobel.decompiler.ast.Expression expression = (com.strobel.decompiler.ast.Expression) node;

            if (expression.getCode() == AstCode.MonitorEnter &&
                next instanceof TryCatchBlock) {

                final TryCatchBlock tryCatch = (TryCatchBlock) next;
                final Block finallyBlock = tryCatch.getFinallyBlock();

                if (finallyBlock != null &&
                    finallyBlock.getBody().size() == 1) {

                    final Node finallyNode = finallyBlock.getBody().get(0);

                    if (finallyNode instanceof com.strobel.decompiler.ast.Expression &&
                        ((com.strobel.decompiler.ast.Expression) finallyNode).getCode() == AstCode.MonitorExit) {

                        return transformSynchronized(expression, tryCatch);
                    }
                }
            }

            final List<Range> ranges = new ArrayList<>();

            final List<com.strobel.decompiler.ast.Expression> childExpressions = node.getSelfAndChildrenRecursive(
                com.strobel.decompiler.ast.Expression.class
            );

            for (final com.strobel.decompiler.ast.Expression e : childExpressions) {
                ranges.addAll(e.getRanges());
            }

            @SuppressWarnings("UnusedDeclaration")
            final List<Range> orderedAndJoinedRanges = Range.orderAndJoint(ranges);
            final AstNode codeExpression = transformExpression((com.strobel.decompiler.ast.Expression) node, true);

            if (codeExpression != null) {
                if (codeExpression instanceof Expression) {
                    return new ExpressionStatement((Expression) codeExpression);
                }
                return (Statement) codeExpression;
            }
        }

        if (node instanceof Loop) {
            final Loop loop = (Loop) node;
            final Statement loopStatement;
            final com.strobel.decompiler.ast.Expression loopCondition = loop.getCondition();

            if (loopCondition != null) {
                if (loop.getLoopType() == LoopType.PostCondition) {
                    final DoWhileStatement doWhileStatement = new DoWhileStatement(loopCondition.getOffset());
                    doWhileStatement.setCondition((Expression) transformExpression(loopCondition, false));
                    loopStatement = doWhileStatement;
                }
                else {
                    final WhileStatement whileStatement = new WhileStatement(loopCondition.getOffset());
                    whileStatement.setCondition((Expression) transformExpression(loopCondition, false));
                    loopStatement = whileStatement;
                }
            }
            else {
                final WhileStatement whileStatement = new WhileStatement(Expression.MYSTERY_OFFSET);
                loopStatement = whileStatement;
                whileStatement.setCondition(new PrimitiveExpression(Expression.MYSTERY_OFFSET, true));
            }

            loopStatement.setChildByRole(Roles.EMBEDDED_STATEMENT, transformBlock(loop.getBody()));

            return loopStatement;
        }

        if (node instanceof Condition) {
            final Condition condition = (Condition) node;
            final com.strobel.decompiler.ast.Expression testCondition = condition.getCondition();
            final Block trueBlock = condition.getTrueBlock();
            final Block falseBlock = condition.getFalseBlock();
            final boolean hasFalseBlock = falseBlock.getEntryGoto() != null || !falseBlock.getBody().isEmpty();

            return new IfElseStatement(
                testCondition.getOffset(),
                (Expression) transformExpression(testCondition, false),
                transformBlock(trueBlock),
                hasFalseBlock ? transformBlock(falseBlock) : null
            );
        }

        if (node instanceof Switch) {
            final Switch switchNode = (Switch) node;
            final com.strobel.decompiler.ast.Expression testCondition = switchNode.getCondition();

            if (TypeAnalysis.isBoolean(testCondition.getInferredType())) {
                testCondition.setExpectedType(BuiltinTypes.Integer);
            }

            final List<CaseBlock> caseBlocks = switchNode.getCaseBlocks();
            final SwitchStatement switchStatement = new SwitchStatement((Expression) transformExpression(testCondition, false));

            for (final CaseBlock caseBlock : caseBlocks) {
                final SwitchSection section = new SwitchSection();
                final AstNodeCollection<CaseLabel> caseLabels = section.getCaseLabels();

                if (caseBlock.getValues().isEmpty()) {
                    caseLabels.add(new CaseLabel());
                }
                else {
                    final TypeReference referenceType;

                    if (testCondition.getExpectedType() != null) {
                        referenceType = testCondition.getExpectedType();
                    }
                    else {
                        referenceType = testCondition.getInferredType();
                    }

                    for (final Integer value : caseBlock.getValues()) {
                        final CaseLabel caseLabel = new CaseLabel();
                        caseLabel.setExpression(AstBuilder.makePrimitive(value, referenceType));
                        caseLabels.add(caseLabel);
                    }
                }

                section.getStatements().add(transformBlock(caseBlock));
                switchStatement.getSwitchSections().add(section);
            }

            return switchStatement;
        }

        if (node instanceof TryCatchBlock) {
            final TryCatchBlock tryCatchNode = ((TryCatchBlock) node);
            final Block finallyBlock = tryCatchNode.getFinallyBlock();
            final List<CatchBlock> catchBlocks = tryCatchNode.getCatchBlocks();

            final TryCatchStatement tryCatch = new TryCatchStatement( Expression.MYSTERY_OFFSET);

            tryCatch.setTryBlock(transformBlock(tryCatchNode.getTryBlock()));

            for (final CatchBlock catchBlock : catchBlocks) {
                final CatchClause catchClause = new CatchClause(transformBlock(catchBlock));

                for (final TypeReference caughtType : catchBlock.getCaughtTypes()) {
                    catchClause.getExceptionTypes().add(_astBuilder.convertType(caughtType));
                }

                final Variable exceptionVariable = catchBlock.getExceptionVariable();

                if (exceptionVariable != null) {
                    catchClause.setVariableName(exceptionVariable.getName());
                    catchClause.putUserData(Keys.VARIABLE, exceptionVariable);
                }

                tryCatch.getCatchClauses().add(catchClause);
            }

            if (finallyBlock != null && (!finallyBlock.getBody().isEmpty() || catchBlocks.isEmpty())) {
                tryCatch.setFinallyBlock(transformBlock(finallyBlock));
            }

            return tryCatch;
        }

        throw new IllegalArgumentException("Unknown node type: " + node);
    }

    private SynchronizedStatement transformSynchronized(final com.strobel.decompiler.ast.Expression expression, final TryCatchBlock tryCatch) {
        final SynchronizedStatement s = new SynchronizedStatement( expression.getOffset());

        s.setExpression((Expression) transformExpression(expression.getArguments().get(0), false));

        if (tryCatch.getCatchBlocks().isEmpty()) {
            s.setEmbeddedStatement(transformBlock(tryCatch.getTryBlock()));
        }
        else {
            tryCatch.setFinallyBlock(null);
            s.setEmbeddedStatement(new BlockStatement(transformNode(tryCatch, null)));
        }

        return s;
    }

    private AstNode transformExpression(final com.strobel.decompiler.ast.Expression e, final boolean isTopLevel) {
        return transformByteCode(e, isTopLevel);
    }

    @SuppressWarnings("ConstantConditions")
    private AstNode transformByteCode(final com.strobel.decompiler.ast.Expression byteCode, final boolean isTopLevel) {
        final Object operand = byteCode.getOperand();
        final Label label = operand instanceof Label ? (Label) operand : null;
        final AstType operandType = operand instanceof TypeReference ? _astBuilder.convertType((TypeReference) operand) : AstType.NULL;
        final Variable variableOperand = operand instanceof Variable ? (Variable) operand : null;
        final FieldReference fieldOperand = operand instanceof FieldReference ? (FieldReference) operand : null;

        final List<Expression> arguments = new ArrayList<>();

        for (final com.strobel.decompiler.ast.Expression e : byteCode.getArguments()) {
            arguments.add((Expression) transformExpression(e, false));
        }

        final Expression arg1 = arguments.size() >= 1 ? arguments.get(0) : null;
        final Expression arg2 = arguments.size() >= 2 ? arguments.get(1) : null;
        final Expression arg3 = arguments.size() >= 3 ? arguments.get(2) : null;

        switch (byteCode.getCode()) {
            case Nop:
                return null;

            case AConstNull:
                return new NullReferenceExpression( byteCode.getOffset());

            case LdC: {
                if (operand instanceof TypeReference) {
                    operandType.getChildrenByRole(Roles.TYPE_ARGUMENT).clear();
                    return new ClassOfExpression(byteCode.getOffset(), operandType);
                }

                final TypeReference type = byteCode.getInferredType() != null ? byteCode.getInferredType()
                                                                              : byteCode.getExpectedType();

                if (type != null) {
                    switch (type.getSimpleType()) {
                        case Byte:
                        case Short:
                            return new PrimitiveExpression(
                                byteCode.getOffset(),
                                JavaPrimitiveCast.cast(JvmType.Integer, operand)
                            );

                        default:
                            return new PrimitiveExpression(
                                byteCode.getOffset(),
                                JavaPrimitiveCast.cast(type.getSimpleType(), operand)
                            );
                    }
                }

                return new PrimitiveExpression(byteCode.getOffset(), operand);
            }

            case Pop:
            case Pop2:
            case Dup:
            case DupX1:
            case DupX2:
            case Dup2:
            case Dup2X1:
            case Dup2X2:
                return arg1;

            case Swap:
                return arg1;

            case I2L:
                return new CastExpression(_astBuilder.convertType(BuiltinTypes.Long), arg1);
            case I2F:
                return new CastExpression(_astBuilder.convertType(BuiltinTypes.Float), arg1);
            case I2D:
                return new CastExpression(_astBuilder.convertType(BuiltinTypes.Double), arg1);
            case L2I:
                return new CastExpression(_astBuilder.convertType(BuiltinTypes.Integer), arg1);
            case L2F:
                return new CastExpression(_astBuilder.convertType(BuiltinTypes.Float), arg1);
            case L2D:
                return new CastExpression(_astBuilder.convertType(BuiltinTypes.Double), arg1);
            case F2I:
                return new CastExpression(_astBuilder.convertType(BuiltinTypes.Integer), arg1);
            case F2L:
                return new CastExpression(_astBuilder.convertType(BuiltinTypes.Long), arg1);
            case F2D:
                return new CastExpression(_astBuilder.convertType(BuiltinTypes.Double), arg1);
            case D2I:
                return new CastExpression(_astBuilder.convertType(BuiltinTypes.Integer), arg1);
            case D2L:
                return new CastExpression(_astBuilder.convertType(BuiltinTypes.Long), arg1);
            case D2F:
                return new CastExpression(_astBuilder.convertType(BuiltinTypes.Float), arg1);
            case I2B:
                return new CastExpression(_astBuilder.convertType(BuiltinTypes.Byte), arg1);
            case I2C:
                return new CastExpression(_astBuilder.convertType(BuiltinTypes.Character), arg1);
            case I2S:
                return new CastExpression(_astBuilder.convertType(BuiltinTypes.Short), arg1);

            case Goto:
                return new GotoStatement(byteCode.getOffset(), ((Label) operand).getName());

            case GetStatic: {
                final ConvertTypeOptions options = new ConvertTypeOptions();
                options.setIncludeTypeParameterDefinitions(false);
                final MemberReferenceExpression fieldReference = _astBuilder.convertType(fieldOperand.getDeclaringType(), options)
                                                                            .member(fieldOperand.getName());
                fieldReference.putUserData(Keys.MEMBER_REFERENCE, fieldOperand);
                return fieldReference;
            }

            case PutStatic: {
                final ConvertTypeOptions options = new ConvertTypeOptions();
                options.setIncludeTypeParameterDefinitions(false);

                final FieldDefinition resolvedField = fieldOperand.resolve();
                final Expression fieldReference;

                if (resolvedField != null &&
                    resolvedField.isFinal() &&
                    StringUtilities.equals(resolvedField.getDeclaringType().getInternalName(), _context.getCurrentType().getInternalName())) {

                    //
                    // Fields marked 'static final' cannot be initialized using a fully qualified name.
                    //

                    fieldReference = new IdentifierExpression( byteCode.getOffset(), fieldOperand.getName());
                }
                else {
                    fieldReference = _astBuilder.convertType(fieldOperand.getDeclaringType(), options)
                                                .member(fieldOperand.getName());
                }

                fieldReference.putUserData(Keys.MEMBER_REFERENCE, fieldOperand);
                return new AssignmentExpression(fieldReference, arg1);
            }

            case GetField: {
                final MemberReferenceExpression fieldReference;

                if (arg1 instanceof ThisReferenceExpression &&
                    MetadataHelper.isSubType(_context.getCurrentType(), fieldOperand.getDeclaringType()) &&
                    !StringUtilities.equals(fieldOperand.getDeclaringType().getInternalName(), _context.getCurrentType().getInternalName())) {

                    fieldReference = new SuperReferenceExpression(arg1.getOffset()).member(fieldOperand.getName());
                }
                else {
                    fieldReference = arg1.member(fieldOperand.getName());
                }

                fieldReference.putUserData(Keys.MEMBER_REFERENCE, fieldOperand);
                return fieldReference;
            }

            case PutField: {
                final MemberReferenceExpression fieldReference;

                if (arg1 instanceof ThisReferenceExpression &&
                    MetadataHelper.isSubType(_context.getCurrentType(), fieldOperand.getDeclaringType()) &&
                    !StringUtilities.equals(fieldOperand.getDeclaringType().getInternalName(), _context.getCurrentType().getInternalName())) {

                    fieldReference = new SuperReferenceExpression(arg1.getOffset()).member(fieldOperand.getName());
                }
                else {
                    fieldReference = arg1.member(fieldOperand.getName());
                }

                fieldReference.putUserData(Keys.MEMBER_REFERENCE, fieldOperand);
                return new AssignmentExpression(fieldReference, arg2);
            }

            case InvokeSpecial:
            case InvokeStatic:
                return transformCall(false, byteCode, arguments);
            case InvokeVirtual:
            case InvokeInterface:
                return transformCall(true, byteCode, arguments);

            case InvokeDynamic: {
                final DynamicCallSite callSite = (DynamicCallSite) operand;
                final MethodReference bootstrapMethod = callSite.getBootstrapMethod();

                if ("java/lang/invoke/LambdaMetafactory".equals(bootstrapMethod.getDeclaringType().getInternalName()) &&
                    (StringUtilities.equals("metafactory", bootstrapMethod.getName(), StringComparison.OrdinalIgnoreCase) ||
                     StringUtilities.equals("altMetafactory", bootstrapMethod.getName(), StringComparison.OrdinalIgnoreCase)) &&
                    callSite.getBootstrapArguments().size() >= 3 &&
                    callSite.getBootstrapArguments().get(1) instanceof MethodHandle) {

                    final MethodHandle targetMethodHandle = (MethodHandle) callSite.getBootstrapArguments().get(1);
                    final MethodReference targetMethod = targetMethodHandle.getMethod();
                    final TypeReference declaringType = targetMethod.getDeclaringType();
                    final String methodName = targetMethod.isConstructor() ? "new" : targetMethod.getName();

                    final boolean hasInstanceArgument;

                    switch (targetMethodHandle.getHandleType()) {
                        case GetField:
                        case PutField:
                        case InvokeVirtual:
                        case InvokeInterface:
                        case InvokeSpecial:
//                            assert arg1 != null;
                            hasInstanceArgument = arg1 != null;
                            break;

                        default:
                            hasInstanceArgument = false;
                            break;
                    }

                    final MethodGroupExpression methodGroup = new MethodGroupExpression(
                        byteCode.getOffset(),
                        hasInstanceArgument ? arg1
                                            : new TypeReferenceExpression( byteCode.getOffset(), _astBuilder.convertType(declaringType)),
                        methodName
                    );

                    methodGroup.getClosureArguments().addAll(
                        hasInstanceArgument ? arguments.subList(1, arguments.size()) : arguments
                    );

                    methodGroup.putUserData(Keys.DYNAMIC_CALL_SITE, callSite);
                    methodGroup.putUserData(Keys.MEMBER_REFERENCE, targetMethod);

                    if (byteCode.getInferredType() != null) {
                        methodGroup.putUserData(Keys.TYPE_REFERENCE, byteCode.getInferredType());
                    }

                    return methodGroup;
                }

                break;
            }

            case Bind: {
                final Lambda lambda = (Lambda) byteCode.getOperand();
                final LambdaExpression lambdaExpression = new LambdaExpression(byteCode.getOffset());
                final AstNodeCollection<ParameterDeclaration> declarations = lambdaExpression.getParameters();

                for (final Variable v : lambda.getParameters()) {
                    final ParameterDefinition p = v.getOriginalParameter();
                    final ParameterDeclaration d = new ParameterDeclaration(v.getName(), null);

                    d.putUserData(Keys.PARAMETER_DEFINITION, p);
                    d.putUserData(Keys.VARIABLE, v);

                    for (final CustomAnnotation annotation : p.getAnnotations()) {
                        d.getAnnotations().add(_astBuilder.createAnnotation(annotation));
                    }

                    declarations.add(d);

                    if (p.isFinal()) {
                        EntityDeclaration.addModifier(d, Modifier.FINAL);
                    }
                }

                final BlockStatement body = transformBlock(lambda.getBody());
                final Match m = LAMBDA_BODY_PATTERN.match(body);

                if (m.success()) {
                    final AstNode bodyNode = first(m.<AstNode>get("body"));
                    bodyNode.remove();
                    lambdaExpression.setBody(bodyNode);

                    if (EMPTY_LAMBDA_BODY_PATTERN.matches(bodyNode)) {
                        bodyNode.getChildrenByRole(BlockStatement.STATEMENT_ROLE).clear();
                    }
                }
                else {
                    lambdaExpression.setBody(body);
                }

                lambdaExpression.putUserData(Keys.TYPE_REFERENCE, byteCode.getInferredType());

                final DynamicCallSite callSite = lambda.getCallSite();

                if (callSite != null) {
                    lambdaExpression.putUserData(Keys.DYNAMIC_CALL_SITE, callSite);
                }

                return lambdaExpression;
            }

            case ArrayLength:
                final MemberReferenceExpression length = arg1.member("length");
                final TypeReference arrayType = single(byteCode.getArguments()).getInferredType();

                if (arrayType != null) {
                    length.putUserData(
                        Keys.MEMBER_REFERENCE,
                        _parser.parseField(arrayType, "length", "I")
                    );
                }

                return length;

            case AThrow:
                return new ThrowStatement(arg1);

            case CheckCast:
                return new CastExpression(operandType, arg1);

            case InstanceOf:
                return new InstanceOfExpression( byteCode.getOffset(), arg1, operandType);

            case MonitorEnter:
            case MonitorExit:
                break;

            case MultiANewArray: {
                final ArrayCreationExpression arrayCreation =
                        new ArrayCreationExpression( byteCode.getOffset());

                int rank = 0;
                AstType elementType = operandType;

                while (elementType instanceof ComposedType) {
                    rank += ((ComposedType) elementType).getArraySpecifiers().size();
                    elementType = ((ComposedType) elementType).getBaseType();
                }

                arrayCreation.setType(elementType.clone());

                for (int i = 0; i < arguments.size(); i++) {
                    arrayCreation.getDimensions().add(arguments.get(i));
                    --rank;
                }

                for (int i = 0; i < rank; i++) {
                    arrayCreation.getAdditionalArraySpecifiers().add(new ArraySpecifier());
                }

                return arrayCreation;
            }

            case Breakpoint:
                return null;

            case Load: {
                if (!variableOperand.isParameter()) {
                    _localVariablesToDefine.add(variableOperand);
                }
                if (variableOperand.isParameter() && variableOperand.getOriginalParameter().getPosition() < 0) {
                    final ThisReferenceExpression self = new ThisReferenceExpression( byteCode.getOffset());
                    self.putUserData(Keys.TYPE_REFERENCE, _context.getCurrentType());
                    return self;
                }
                final IdentifierExpression name = new IdentifierExpression( byteCode.getOffset(),
                        variableOperand.getName());
                name.putUserData(Keys.VARIABLE, variableOperand);
                return name;
            }

            case Store: {
                if (!variableOperand.isParameter()) {
                    _localVariablesToDefine.add(variableOperand);
                }
                final IdentifierExpression name = new IdentifierExpression( byteCode.getOffset(),
                        variableOperand.getName());
                name.putUserData(Keys.VARIABLE, variableOperand);
                return new AssignmentExpression(name, arg1);
            }

            case LoadElement: {
                return new IndexerExpression( byteCode.getOffset(), arg1, arg2);
            }
            case StoreElement: {
                return new AssignmentExpression(
                    new IndexerExpression( byteCode.getOffset(), arg1, arg2),
                    arg3
                );
            }

            case Add:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.ADD, arg2);
            case Sub:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.SUBTRACT, arg2);
            case Mul:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.MULTIPLY, arg2);
            case Div:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.DIVIDE, arg2);
            case Rem:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.MODULUS, arg2);
            case Neg:
                return new UnaryOperatorExpression(UnaryOperatorType.MINUS, arg1);
            case Shl:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.SHIFT_LEFT, arg2);
            case Shr:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.SHIFT_RIGHT, arg2);
            case UShr:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.UNSIGNED_SHIFT_RIGHT, arg2);
            case And:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.BITWISE_AND, arg2);
            case Or:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.BITWISE_OR, arg2);
            case Not:
                return new UnaryOperatorExpression(UnaryOperatorType.NOT, arg1);
            case Xor:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.EXCLUSIVE_OR, arg2);

            case Inc: {
                if (!variableOperand.isParameter()) {
                    _localVariablesToDefine.add(variableOperand);
                }

                final IdentifierExpression name = new IdentifierExpression( byteCode.getOffset(),
                        variableOperand.getName());

                name.getIdentifierToken().putUserData(Keys.VARIABLE, variableOperand);
                name.putUserData(Keys.VARIABLE, variableOperand);

                final PrimitiveExpression deltaExpression = (PrimitiveExpression) arg1;
                final int delta = (int) JavaPrimitiveCast.cast(JvmType.Integer, deltaExpression.getValue());

                switch (delta) {
                    case -1:
                        return new UnaryOperatorExpression(UnaryOperatorType.DECREMENT, name);
                    case 1:
                        return new UnaryOperatorExpression(UnaryOperatorType.INCREMENT, name);
                    default:
                        return new AssignmentExpression(name, AssignmentOperatorType.ADD, arg1);
                }
            }

            case CmpEq:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.EQUALITY, arg2);
            case CmpNe:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.INEQUALITY, arg2);
            case CmpLt:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.LESS_THAN, arg2);
            case CmpGe:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.GREATER_THAN_OR_EQUAL, arg2);
            case CmpGt:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.GREATER_THAN, arg2);
            case CmpLe:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.LESS_THAN_OR_EQUAL, arg2);

            case Return:
                return new ReturnStatement(byteCode.getOffset(), arg1);

            case NewArray: {
                final ArrayCreationExpression arrayCreation =
                        new ArrayCreationExpression( byteCode.getOffset());

                TypeReference elementType = operandType.getUserData(Keys.TYPE_REFERENCE);

                while (elementType.isArray()) {
                    arrayCreation.getAdditionalArraySpecifiers().add(new ArraySpecifier());
                    elementType = elementType.getElementType();
                }

                arrayCreation.setType(_astBuilder.convertType(elementType));
                arrayCreation.getDimensions().add(arg1);

                return arrayCreation;
            }

            case LogicalNot:
                return new UnaryOperatorExpression(UnaryOperatorType.NOT, arg1);

            case LogicalAnd:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.LOGICAL_AND, arg2);
            case LogicalOr:
                return new BinaryOperatorExpression(arg1, BinaryOperatorType.LOGICAL_OR, arg2);

            case InitObject:
                return transformCall(false, byteCode, arguments);

            case InitArray: {
                final ArrayCreationExpression arrayCreation =
                        new ArrayCreationExpression( byteCode.getOffset());

                TypeReference elementType = operandType.getUserData(Keys.TYPE_REFERENCE);

                while (elementType.isArray()) {
                    arrayCreation.getAdditionalArraySpecifiers().add(new ArraySpecifier());
                    elementType = elementType.getElementType();
                }

                arrayCreation.setType(_astBuilder.convertType(elementType));
                arrayCreation.setInitializer(new ArrayInitializerExpression(arguments));

                return arrayCreation;
            }

            case Wrap:
                return null;

            case TernaryOp:
                return new ConditionalExpression(arg1, arg2, arg3);

            case LoopOrSwitchBreak:
                return label != null ? new GotoStatement(byteCode.getOffset(), label.getName()) : new BreakStatement(byteCode.getOffset());

            case LoopContinue:
                return label != null ? new ContinueStatement(byteCode.getOffset(), label.getName()) : new ContinueStatement( byteCode.getOffset());

            case CompoundAssignment:
                throw ContractUtils.unreachable();

            case PreIncrement: {
                final Integer incrementAmount = (Integer) operand;
                if (incrementAmount < 0) {
                    return new UnaryOperatorExpression(UnaryOperatorType.DECREMENT, arg1);
                }
                return new UnaryOperatorExpression(UnaryOperatorType.INCREMENT, arg1);
            }

            case PostIncrement: {
                final Integer incrementAmount = (Integer) operand;
                if (incrementAmount < 0) {
                    return new UnaryOperatorExpression(UnaryOperatorType.POST_DECREMENT, arg1);
                }
                return new UnaryOperatorExpression(UnaryOperatorType.POST_INCREMENT, arg1);
            }

            case Box:
            case Unbox:
                throw ContractUtils.unreachable();

            case Leave:
            case EndFinally:
                return null;

            case DefaultValue:
                return AstBuilder.makeDefaultValue((TypeReference) operand);
        }

        final Expression inlinedAssembly = inlineAssembly(byteCode, arguments);

        if (isTopLevel) {
            return new CommentStatement(" " + inlinedAssembly.toString());
        }

        return inlinedAssembly;
    }

    @SuppressWarnings("ConstantConditions")
    private Expression transformCall(
        final boolean isVirtual,
        final com.strobel.decompiler.ast.Expression byteCode,
        final List<Expression> arguments) {

        final MethodReference methodReference = (MethodReference) byteCode.getOperand();

        final boolean hasThis = byteCode.getCode() == AstCode.InvokeVirtual ||
                                byteCode.getCode() == AstCode.InvokeInterface ||
                                byteCode.getCode() == AstCode.InvokeSpecial;

        Expression target;

        final TypeReference declaringType = methodReference.getDeclaringType();

        if (hasThis) {
            target = arguments.remove(0);

            if (target instanceof NullReferenceExpression) {
                target = new CastExpression(_astBuilder.convertType(declaringType), target);
            }
        }
        else {
            final MethodDefinition resolvedMethod;

            if (byteCode.getCode() == AstCode.InvokeStatic &&
                declaringType.isEquivalentTo(_context.getCurrentType()) &&
                (!_context.getSettings().getForceExplicitTypeArguments() ||
                 (resolvedMethod = methodReference.resolve()) == null ||
                 !resolvedMethod.isGenericMethod())) {

                target = Expression.NULL;
            }
            else {
                final ConvertTypeOptions options = new ConvertTypeOptions();
                options.setIncludeTypeArguments(false);
                options.setIncludeTypeParameterDefinitions(false);
                options.setAllowWildcards(false);
                target = new TypeReferenceExpression( byteCode.getOffset(), _astBuilder.convertType(declaringType, options));
            }
        }

        if (target instanceof ThisReferenceExpression) {
            if (!isVirtual && !declaringType.isEquivalentTo(_method.getDeclaringType())) {
                target = new SuperReferenceExpression( byteCode.getOffset());
                target.putUserData(Keys.TYPE_REFERENCE, declaringType);
            }
        }
        else if (methodReference.isConstructor()) {
            final ObjectCreationExpression creation;
            final TypeDefinition resolvedType = declaringType.resolve();

            if (resolvedType != null) {
                final AstType declaredType;

                TypeReference instantiatedType;

                if (resolvedType.isAnonymous()) {
                    if (resolvedType.getExplicitInterfaces().isEmpty()) {
                        instantiatedType = resolvedType.getBaseType();
                    }
                    else {
                        instantiatedType = resolvedType.getExplicitInterfaces().get(0);
                    }
                }
                else {
                    instantiatedType = resolvedType;
                }

                final List<TypeReference> typeArguments = byteCode.getUserData(AstKeys.TYPE_ARGUMENTS);

                if (typeArguments != null &&
                    resolvedType.isGenericDefinition() &&
                    typeArguments.size() == resolvedType.getGenericParameters().size()) {

                    instantiatedType = instantiatedType.makeGenericType(typeArguments);
                }

                declaredType = _astBuilder.convertType(instantiatedType);

                if (resolvedType.isAnonymous()) {
                    creation = new AnonymousObjectCreationExpression(
                        byteCode.getOffset(),
                        _astBuilder.createType(resolvedType).clone(),
                        declaredType
                    );
                }
                else {
                    creation = new ObjectCreationExpression( byteCode.getOffset(), declaredType);
                }
            }
            else {
                final ConvertTypeOptions options = new ConvertTypeOptions();
                options.setIncludeTypeParameterDefinitions(false);
                creation = new ObjectCreationExpression( byteCode.getOffset(), _astBuilder.convertType(declaringType, options));
            }

            creation.getArguments().addAll(adjustArgumentsForMethodCall(methodReference, arguments));
            creation.putUserData(Keys.MEMBER_REFERENCE, methodReference);

            return creation;
        }

        final InvocationExpression invocation;

        if (methodReference.isConstructor()) {
            invocation = new InvocationExpression(
                byteCode.getOffset(),
                target,
                adjustArgumentsForMethodCall(methodReference, arguments)
            );
        }
        else {
            invocation = target.invoke(
                methodReference.getName(),
                convertTypeArguments(methodReference),
                adjustArgumentsForMethodCall(methodReference, arguments)
            );

            if (target.isNull()) {
                invocation.setOffset(byteCode.getOffset());
            }
        }

        invocation.putUserData(Keys.MEMBER_REFERENCE, methodReference);

        return invocation;
    }

    @SuppressWarnings("UnusedParameters")
    private List<AstType> convertTypeArguments(final MethodReference methodReference) {
        if (_context.getSettings().getForceExplicitTypeArguments() &&
            methodReference instanceof IGenericInstance) {

            final List<TypeReference> typeArguments = ((IGenericInstance) methodReference).getTypeArguments();

            if (!typeArguments.isEmpty()) {
                final List<AstType> astTypeArguments = new ArrayList<>();

                for (final TypeReference type : typeArguments) {
                    astTypeArguments.add(_astBuilder.convertType(type));
                }

                return astTypeArguments;
            }
        }

        return Collections.emptyList();
    }

    @SuppressWarnings("UnusedParameters")
    private List<Expression> adjustArgumentsForMethodCall(final MethodReference method, final List<Expression> arguments) {
        if (!arguments.isEmpty() && method.isConstructor()) {
            final TypeReference declaringType = method.getDeclaringType();

            if (declaringType.isNested()) {
                final TypeDefinition resolvedType = declaringType.resolve();

                if (resolvedType != null) {
                    if (resolvedType.isLocalClass()) {
                        return arguments;
                    }

                    if (resolvedType.isInnerClass()) {
                        final MethodDefinition resolvedMethod = method.resolve();

                        if (resolvedMethod != null &&
                            resolvedMethod.isSynthetic() &&
                            (resolvedMethod.getFlags() & Flags.AccessFlags) == 0) {

                            final List<ParameterDefinition> parameters = resolvedMethod.getParameters();

                            int start = 0;
                            int end = arguments.size();

                            for (int i = parameters.size() - 1; i >= 0; i--) {
                                final TypeReference parameterType = parameters.get(i).getParameterType();
                                final TypeDefinition resolvedParameterType = parameterType.resolve();

                                if (resolvedParameterType != null && resolvedParameterType.isAnonymous()) {
                                    --end;
                                }
                                else {
                                    break;
                                }
                            }

                            if (!resolvedType.isStatic() && !_context.getSettings().getShowSyntheticMembers()) {
                                ++start;
                            }

                            if (start > end) {
                                return Collections.emptyList();
                            }

                            return adjustArgumentsForMethodCallCore(
                                method.getParameters().subList(start, end),
                                arguments.subList(start, end)
                            );
                        }
                    }
                }
            }
        }

        return adjustArgumentsForMethodCallCore(method.getParameters(), arguments);
    }

    private List<Expression> adjustArgumentsForMethodCallCore(
        final List<ParameterDefinition> parameters,
        final List<Expression> arguments) {

        final int parameterCount = parameters.size();

        assert parameterCount == arguments.size();

        final JavaResolver resolver = new JavaResolver(_context);
        final ConvertTypeOptions options = new ConvertTypeOptions();

        options.setAllowWildcards(false);

        for (int i = 0, n = arguments.size(); i < n; i++) {
            final Expression argument = arguments.get(i);
            final ResolveResult resolvedArgument = resolver.apply(argument);

            if (resolvedArgument == null ||
                argument instanceof LambdaExpression /*||
                argument instanceof MethodGroupExpression*/) {

                continue;
            }

            final ParameterDefinition p = parameters.get(i);

            final TypeReference aType = resolvedArgument.getType();
            final TypeReference pType = p.getParameterType();

            if (isCastRequired(pType, aType, true)) {
                arguments.set(
                    i,
                    new CastExpression(_astBuilder.convertType(pType, options), argument)
                );
            }
        }

        int first = 0, last = parameterCount - 1;

        while (first < parameterCount && parameters.get(first).isSynthetic()) {
            ++first;
        }

        while (last >= 0 && parameters.get(last).isSynthetic()) {
            --last;
        }

        if (first >= parameterCount || last < 0) {
            return Collections.emptyList();
        }

        if (first == 0 && last == parameterCount - 1) {
            return arguments;
        }

        return arguments.subList(first, last + 1);
    }

    private boolean isCastRequired(final TypeReference targetType, final TypeReference sourceType, final boolean exactMatch) {
        if (targetType == null || sourceType == null) {
            return false;
        }

        if (targetType.isPrimitive()) {
            return sourceType.getSimpleType() != targetType.getSimpleType();
        }

        if (exactMatch) {
            return !MetadataHelper.isSameType(targetType, sourceType, true);
        }

        return !MetadataHelper.isAssignableFrom(targetType, sourceType);
    }

    private static Expression inlineAssembly(final com.strobel.decompiler.ast.Expression byteCode, final List<Expression> arguments) {
        if (byteCode.getOperand() != null) {
            arguments.add(0, new IdentifierExpression( byteCode.getOffset(),
                    formatByteCodeOperand(byteCode.getOperand())));
        }
        return new IdentifierExpression( byteCode.getOffset(), byteCode.getCode().getName()).invoke(arguments);
    }

    private static String formatByteCodeOperand(final Object operand) {
        if (operand == null) {
            return StringUtilities.EMPTY;
        }

        final PlainTextOutput output = new PlainTextOutput();
        DecompilerHelpers.writeOperand(output, operand);
        return output.toString();
    }
}
