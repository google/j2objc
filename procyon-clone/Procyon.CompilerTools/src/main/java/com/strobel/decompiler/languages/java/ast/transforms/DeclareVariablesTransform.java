/*
 * DeclareVariablesTransform.java
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

import com.strobel.assembler.metadata.MetadataHelper;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.ParameterDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.StringUtilities;
import com.strobel.core.StrongBox;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.ast.Variable;
import com.strobel.decompiler.languages.java.ast.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.strobel.core.CollectionUtilities.*;

@SuppressWarnings("ProtectedField")
public class DeclareVariablesTransform implements IAstTransform {
    protected final List<VariableToDeclare> variablesToDeclare = new ArrayList<>();
    protected final DecompilerContext context;

    public DeclareVariablesTransform(final DecompilerContext context) {
        this.context = VerifyArgument.notNull(context, "context");
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void run(final AstNode node) {
        run(node, null);

        for (final VariableToDeclare v : variablesToDeclare) {
            final Variable variable = v.getVariable();
            final AssignmentExpression replacedAssignment = v.getReplacedAssignment();

            if (replacedAssignment == null) {
                final BlockStatement block = (BlockStatement) v.getInsertionPoint().getParent();
                final AnalysisResult analysisResult = analyze(v, block);
                final VariableDeclarationStatement declaration = new VariableDeclarationStatement(v.getType().clone(), v.getName(), Expression.MYSTERY_OFFSET);

                if (variable != null) {
                    declaration.getVariables().firstOrNullObject().putUserData(Keys.VARIABLE, variable);
                }

                if (analysisResult.isSingleAssignment) {
                    declaration.addModifier(Modifier.FINAL);
                }
                else if (analysisResult.needsInitializer && variable != null) {
                    declaration.getVariables().firstOrNullObject().setInitializer(
                        AstBuilder.makeDefaultValue(variable.getType())
                    );
                }

                Statement insertionPoint = v.getInsertionPoint();

                while (insertionPoint.getPreviousSibling() instanceof LabelStatement) {
                    insertionPoint = (Statement) insertionPoint.getPreviousSibling();
                }

                block.getStatements().insertBefore(insertionPoint, declaration);
            }
        }

        //
        // Do all the insertions before the replacements.  This is necessary because a replacement
        // might remove our reference point from the AST.
        //

        for (final VariableToDeclare v : variablesToDeclare) {
            final Variable variable = v.getVariable();
            final AssignmentExpression replacedAssignment = v.getReplacedAssignment();

            if (replacedAssignment != null) {
                final VariableInitializer initializer = new VariableInitializer(v.getName());
                final Expression right = replacedAssignment.getRight();
                final AstNode parent = replacedAssignment.getParent();

                if (parent.isNull() || parent.getParent() == null) {
                    continue;
                }
                final AnalysisResult analysisResult = analyze(v, parent.getParent());

                right.remove();
                right.putUserDataIfAbsent(Keys.MEMBER_REFERENCE, replacedAssignment.getUserData(Keys.MEMBER_REFERENCE));
                right.putUserDataIfAbsent(Keys.VARIABLE, variable);

                initializer.setInitializer(right);
                initializer.putUserData(Keys.VARIABLE, variable);

                final VariableDeclarationStatement declaration = new VariableDeclarationStatement();

                declaration.setType(v.getType().clone());
                declaration.getVariables().add(initializer);

                if (parent instanceof ExpressionStatement) {
                    if (analysisResult.isSingleAssignment) {
                        declaration.addModifier(Modifier.FINAL);
                    }

                    declaration.putUserDataIfAbsent(Keys.MEMBER_REFERENCE, parent.getUserData(Keys.MEMBER_REFERENCE));
                    declaration.putUserData(Keys.VARIABLE, variable);
                    parent.replaceWith(declaration);
                }
                else {
                    if (analysisResult.isSingleAssignment) {
                        declaration.addModifier(Modifier.FINAL);
                    }

                    replacedAssignment.replaceWith(declaration);
                }
            }
        }

        variablesToDeclare.clear();
    }

    private AnalysisResult analyze(final VariableToDeclare v, final AstNode scope) {
        final BlockStatement block = v.getBlock();
        final DefiniteAssignmentAnalysis analysis = new DefiniteAssignmentAnalysis(context, block);

        if (v.getInsertionPoint() != null) {
            final Statement parentStatement = v.getInsertionPoint();
            analysis.setAnalyzedRange(parentStatement, block);
        }
        else {
            final ExpressionStatement parentStatement = (ExpressionStatement) v.getReplacedAssignment().getParent();
            analysis.setAnalyzedRange(parentStatement, block);
        }

        analysis.analyze(v.getName());

        final boolean needsInitializer = !analysis.getUnassignedVariableUses().isEmpty();
        final IsSingleAssignmentVisitor isSingleAssignmentVisitor = new IsSingleAssignmentVisitor(v.getName(), v.getReplacedAssignment());

        scope.acceptVisitor(isSingleAssignmentVisitor, null);

        return new AnalysisResult(isSingleAssignmentVisitor.isSingleAssignment(), needsInitializer);
    }

    private final static class AnalysisResult {
        final boolean isSingleAssignment;
        final boolean needsInitializer;

        private AnalysisResult(final boolean singleAssignment, final boolean needsInitializer) {
            isSingleAssignment = singleAssignment;
            this.needsInitializer = needsInitializer;
        }
    }

    private void run(final AstNode node, final DefiniteAssignmentAnalysis daa) {
        DefiniteAssignmentAnalysis analysis = daa;

        if (node instanceof BlockStatement) {
            final BlockStatement block = (BlockStatement) node;
            final List<VariableDeclarationStatement> variables = new ArrayList<>();

            for (final Statement statement : block.getStatements()) {
                if (statement instanceof VariableDeclarationStatement) {
                    variables.add((VariableDeclarationStatement) statement);
                }
            }

            if (!variables.isEmpty()) {
                //
                // Remove old variable declarations.
                //
                for (final VariableDeclarationStatement declaration : variables) {
                    assert declaration.getVariables().size() == 1 &&
                           declaration.getVariables().firstOrNullObject().getInitializer().isNull();

                    declaration.remove();
                }
            }

            if (analysis == null) {
                analysis = new DefiniteAssignmentAnalysis(block, new JavaResolver(context));
            }

            for (final VariableDeclarationStatement declaration : variables) {
                final VariableInitializer initializer = declaration.getVariables().firstOrNullObject();
                final String variableName = initializer.getName();
                final Variable variable = declaration.getUserData(Keys.VARIABLE);

                declareVariableInBlock(analysis, block, declaration.getType(), variableName, variable, true);
            }
        }

        if (node instanceof MethodDeclaration ||
            node instanceof ConstructorDeclaration) {

            final Set<ParameterDefinition> unassignedParameters = new HashSet<>();
            final AstNodeCollection<ParameterDeclaration> parameters = node.getChildrenByRole(Roles.PARAMETER);
            final Map<ParameterDefinition, ParameterDeclaration> declarationMap = new HashMap<>();
            final Map<String, ParameterDefinition> parametersByName = new HashMap<>();

            for (final ParameterDeclaration parameter : parameters) {
                final ParameterDefinition definition = parameter.getUserData(Keys.PARAMETER_DEFINITION);

                if (definition != null) {
                    unassignedParameters.add(definition);
                    declarationMap.put(definition, parameter);
                    parametersByName.put(parameter.getName(), definition);
                }
            }

            node.acceptVisitor(new ParameterAssignmentVisitor(unassignedParameters, parametersByName), null);

            for (final ParameterDefinition definition : unassignedParameters) {
                final ParameterDeclaration declaration = declarationMap.get(definition);

                if (declaration != null && !declaration.hasModifier(Modifier.FINAL)) {
                    declaration.addChild(new JavaModifierToken(Modifier.FINAL), EntityDeclaration.MODIFIER_ROLE);
                }
            }
        }

        for (AstNode child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof TypeDeclaration) {
                final TypeDefinition currentType = context.getCurrentType();
                final MethodDefinition currentMethod = context.getCurrentMethod();

                context.setCurrentType(null);
                context.setCurrentMethod(null);

                try {
                    new DeclareVariablesTransform(context).run(child);
                }
                finally {
                    context.setCurrentType(currentType);
                    context.setCurrentMethod(currentMethod);
                }
            }
            else {
                run(child, analysis);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void declareVariableInBlock(
        final DefiniteAssignmentAnalysis analysis,
        final BlockStatement block,
        final AstType type,
        final String variableName,
        final Variable variable,
        final boolean allowPassIntoLoops) {

        //
        // The point at which the variable would be declared if we decide to declare it in this block.
        //
        final StrongBox<Statement> declarationPoint = new StrongBox<>();

        final boolean canMoveVariableIntoSubBlocks = findDeclarationPoint(
            analysis,
            variableName,
            allowPassIntoLoops,
            block,
            declarationPoint,
            null
        );

        if (declarationPoint.get() == null) {
            //
            // The variable isn't used at all.
            //
            return;
        }

        if (canMoveVariableIntoSubBlocks) {
            for (final Statement statement : block.getStatements()) {
                if (!usesVariable(statement, variableName)) {
                    continue;
                }

                boolean processChildren = true;

                if (statement instanceof ForStatement && statement == declarationPoint.get()) {
                    final ForStatement forStatement = (ForStatement) statement;
                    final AstNodeCollection<Statement> initializers = forStatement.getInitializers();

                    for (final Statement initializer : initializers) {
                        if (tryConvertAssignmentExpressionIntoVariableDeclaration(block, initializer, type, variableName)) {
                            processChildren = false;
                            break;
                        }
                    }
                }

                if (processChildren) {
                    for (final AstNode child : statement.getChildren()) {
                        if (child instanceof BlockStatement) {
                            declareVariableInBlock(analysis, (BlockStatement) child, type, variableName, variable, allowPassIntoLoops);
                        }
                        else if (hasNestedBlocks(child)) {
                            for (final AstNode nestedChild : child.getChildren()) {
                                if (nestedChild instanceof BlockStatement) {
                                    declareVariableInBlock(
                                        analysis,
                                        (BlockStatement) nestedChild,
                                        type,
                                        variableName,
                                        variable,
                                        allowPassIntoLoops
                                    );
                                }
                            }
                        }
                    }
                }

                final boolean canStillMoveIntoSubBlocks = findDeclarationPoint(
                    analysis,
                    variableName,
                    allowPassIntoLoops,
                    block,
                    declarationPoint,
                    statement
                );

                if (!canStillMoveIntoSubBlocks && declarationPoint.get() != null) {
                    if (!tryConvertAssignmentExpressionIntoVariableDeclaration(block, declarationPoint.get(), type, variableName)) {
                        final VariableToDeclare vtd = new VariableToDeclare(type, variableName, variable, declarationPoint.get(), block);
                        variablesToDeclare.add(vtd);
                    }
                    return;
                }
            }
        }
        else if (!tryConvertAssignmentExpressionIntoVariableDeclaration(block, declarationPoint.get(), type, variableName)) {
            final VariableToDeclare vtd = new VariableToDeclare(type, variableName, variable, declarationPoint.get(), block);
            variablesToDeclare.add(vtd);
        }
    }

    public static boolean findDeclarationPoint(
        final DefiniteAssignmentAnalysis analysis,
        final VariableDeclarationStatement declaration,
        final BlockStatement block,
        final StrongBox<Statement> declarationPoint,
        final Statement skipUpThrough) {

        final String variableName = declaration.getVariables().firstOrNullObject().getName();

        return findDeclarationPoint(analysis, variableName, true, block, declarationPoint, skipUpThrough);
    }

    static boolean findDeclarationPoint(
        final DefiniteAssignmentAnalysis analysis,
        final String variableName,
        final boolean allowPassIntoLoops,
        final BlockStatement block,
        final StrongBox<Statement> declarationPoint,
        final Statement skipUpThrough) {

        declarationPoint.set(null);

        Statement waitFor = skipUpThrough;

        if (block.getParent() instanceof CatchClause) {
            final CatchClause catchClause = (CatchClause) block.getParent();

            if (StringUtilities.equals(catchClause.getVariableName(), variableName)) {
                return false;
            }
        }

        for (final Statement statement : block.getStatements()) {
            if (waitFor != null) {
                if (statement == waitFor) {
                    waitFor = null;
                }
                continue;
            }

            if (usesVariable(statement, variableName)) {
                if (declarationPoint.get() != null) {
                    return canRedeclareVariable(analysis, block, statement, variableName);
                }

                declarationPoint.set(statement);

                if (!canMoveVariableIntoSubBlock(analysis, block, statement, variableName, allowPassIntoLoops)) {
                    //
                    // If it's not possible to move the variable use into a nested block,
                    // we need to declare it in this block.
                    //
                    return false;
                }

                //
                // If we can move the variable into a sub-block, we need to ensure that the
                // remaining code does not use the variable that was assigned by the first
                // sub-block.
                //

                final Statement nextStatement = statement.getNextStatement();

                if (nextStatement != null) {
                    analysis.setAnalyzedRange(nextStatement, block);
                    analysis.analyze(variableName);

                    if (!analysis.getUnassignedVariableUses().isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static boolean canMoveVariableIntoSubBlock(
        final DefiniteAssignmentAnalysis analysis,
        final BlockStatement block,
        final Statement statement,
        final String variableName,
        final boolean allowPassIntoLoops) {

        if (!allowPassIntoLoops && AstNode.isLoop(statement)) {
            return false;
        }

        if (statement instanceof ForStatement) {
            final ForStatement forStatement = (ForStatement) statement;

            //
            // ForStatement is a special case: we can move the variable declarations into the initializer.
            //

            if (!forStatement.getInitializers().isEmpty()) {
                boolean result = false;
                TypeReference lastInitializerType = null;
                StrongBox<Statement> declarationPoint = null;

                final Set<String> variableNames = new HashSet<>();

                for (final Statement initializer : forStatement.getInitializers()) {
                    if (initializer instanceof ExpressionStatement &&
                        ((ExpressionStatement) initializer).getExpression() instanceof AssignmentExpression) {

                        final Expression e = ((ExpressionStatement) initializer).getExpression();

                        if (e instanceof AssignmentExpression &&
                            ((AssignmentExpression) e).getOperator() == AssignmentOperatorType.ASSIGN &&
                            ((AssignmentExpression) e).getLeft() instanceof IdentifierExpression) {

                            final IdentifierExpression identifier = (IdentifierExpression) ((AssignmentExpression) e).getLeft();
                            final boolean usedByInitializer = usesVariable(((AssignmentExpression) e).getRight(), variableName);

                            if (usedByInitializer) {
                                return false;
                            }

                            final Variable variable = identifier.getUserData(Keys.VARIABLE);

                            if (variable == null || variable.isParameter()) {
                                return false;
                            }

                            final TypeReference variableType = variable.getType();

                            if (lastInitializerType == null) {
                                lastInitializerType = variableType;
                            }
                            else if (!MetadataHelper.isSameType(lastInitializerType, variableType)) {
                                return false;
                            }

                            if (!variableNames.add(identifier.getIdentifier())) {
                                //
                                // We cannot move the declaration if the any variable appears more than once in the initializer list.
                                //
                                return false;
                            }

                            if (result) {
                                if (declarationPoint == null) {
                                    declarationPoint = new StrongBox<>();
                                }

                                //
                                // We can only move the declaration if we can also move the declarations for all the other
                                // variables initialized by the loop header.
                                //

                                if (!findDeclarationPoint(analysis, identifier.getIdentifier(), allowPassIntoLoops, block, declarationPoint, null) ||
                                    declarationPoint.get() != statement) {

                                    return false;
                                }
                            }
                            else if (StringUtilities.equals(identifier.getIdentifier(), variableName)) {
                                result = true;
                            }
                        }
                    }
                }

                if (result) {
                    return true;
                }
            }
        }

        if (statement instanceof TryCatchStatement) {
            final TryCatchStatement tryCatch = (TryCatchStatement) statement;

            //
            // TryCatchStatements with resources are a special case: we can move the resource declarations
            // into the resource list.
            //

            if (!tryCatch.getResources().isEmpty()) {
                for (final VariableDeclarationStatement resource : tryCatch.getResources()) {
                    if (StringUtilities.equals(first(resource.getVariables()).getName(), variableName)) {
                        return true;
                    }
                }
            }
        }

        //
        // We can move the variable into a sub-block only if the variable is used only in that
        // sub-block (and not in expressions such as the loop condition).
        //

        for (AstNode child = statement.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (!(child instanceof BlockStatement) && usesVariable(child, variableName)) {
                if (hasNestedBlocks(child)) {
                    //
                    // Loops, catch clauses, switch sections, and labeled statements can contain nested blocks.
                    //
                    for (AstNode grandChild = child.getFirstChild(); grandChild != null; grandChild = grandChild.getNextSibling()) {
                        if (!(grandChild instanceof BlockStatement) && usesVariable(grandChild, variableName)) {
                            return false;
                        }
                    }
                }
                else {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean usesVariable(final AstNode node, final String variableName) {
        if (node instanceof AnonymousObjectCreationExpression) {
            for (final Expression argument : ((AnonymousObjectCreationExpression) node).getArguments()) {
                if (usesVariable(argument, variableName)) {
                    return true;
                }
            }
            return false;
        }

        if (node instanceof TypeDeclaration) {
            final TypeDeclaration type = (TypeDeclaration) node;

            for (final FieldDeclaration field : ofType(type.getMembers(), FieldDeclaration.class)) {
                if (!field.getVariables().isEmpty() && usesVariable(first(field.getVariables()), variableName)) {
                    return true;
                }
            }

            for (final MethodDeclaration method : ofType(type.getMembers(), MethodDeclaration.class)) {
                if (usesVariable(method.getBody(), variableName)) {
                    return true;
                }
            }

            return false;
        }

        if (node instanceof IdentifierExpression) {
            if (StringUtilities.equals(((IdentifierExpression) node).getIdentifier(), variableName)) {
                return true;
            }
        }

        if (node instanceof ForStatement) {
            final ForStatement forLoop = (ForStatement) node;

            for (final Statement statement : forLoop.getInitializers()) {
                if (statement instanceof VariableDeclarationStatement) {
                    final AstNodeCollection<VariableInitializer> variables = ((VariableDeclarationStatement) statement).getVariables();

                    for (final VariableInitializer variable : variables) {
                        if (StringUtilities.equals(variable.getName(), variableName)) {
                            //
                            // No need to introduce the variable here.
                            //
                            return false;
                        }
                    }
                }
            }
        }

        if (node instanceof TryCatchStatement) {
            final TryCatchStatement tryCatch = (TryCatchStatement) node;

            for (final VariableDeclarationStatement resource : tryCatch.getResources()) {
                if (StringUtilities.equals(first(resource.getVariables()).getName(), variableName)) {
                    //
                    // No need to introduce the variable here.
                    //
                    return false;
                }
            }
        }

        if (node instanceof ForEachStatement) {
            if (StringUtilities.equals(((ForEachStatement) node).getVariableName(), variableName)) {
                //
                // No need to introduce the variable here.
                //
                return false;
            }
        }

        if (node instanceof CatchClause) {
            if (StringUtilities.equals(((CatchClause) node).getVariableName(), variableName)) {
                //
                // No need to introduce the variable here.
                //
                return false;
            }
        }

        for (AstNode child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (usesVariable(child, variableName)) {
                return true;
            }
        }

        return false;
    }

    private static boolean canRedeclareVariable(
        final DefiniteAssignmentAnalysis analysis,
        final BlockStatement block,
        final AstNode node,
        final String variableName) {

        if (node instanceof ForStatement) {
            final ForStatement forLoop = (ForStatement) node;

            for (final Statement statement : forLoop.getInitializers()) {
                if (statement instanceof VariableDeclarationStatement) {
                    final AstNodeCollection<VariableInitializer> variables = ((VariableDeclarationStatement) statement).getVariables();

                    for (final VariableInitializer variable : variables) {
                        if (StringUtilities.equals(variable.getName(), variableName)) {
                            return true;
                        }
                    }
                }
                else if (statement instanceof ExpressionStatement &&
                         ((ExpressionStatement) statement).getExpression() instanceof AssignmentExpression) {

                    final AssignmentExpression assignment = (AssignmentExpression) ((ExpressionStatement) statement).getExpression();
                    final Expression left = assignment.getLeft();
                    final Expression right = assignment.getRight();

                    if (left instanceof IdentifierExpression &&
                        StringUtilities.equals(((IdentifierExpression) left).getIdentifier(), variableName) &&
                        !usesVariable(right, variableName)) {

                        return true;
                    }
                }
            }
        }

        if (node instanceof ForEachStatement) {
            if (StringUtilities.equals(((ForEachStatement) node).getVariableName(), variableName)) {
                return true;
            }
        }

        if (node instanceof TryCatchStatement) {
            final TryCatchStatement tryCatch = (TryCatchStatement) node;

            for (final VariableDeclarationStatement resource : tryCatch.getResources()) {
                if (StringUtilities.equals(first(resource.getVariables()).getName(), variableName)) {
                    return true;
                }
            }
        }

        for (AstNode prev = node.getPreviousSibling();
             prev != null &&
             !prev.isNull(); prev = prev.getPreviousSibling()) {

            if (usesVariable(prev, variableName)) {
                final Statement statement = firstOrDefault(ofType(prev.getAncestorsAndSelf(), Statement.class));

                if (statement == null) {
                    return false;
                }

                if (!canMoveVariableIntoSubBlock(analysis, block, statement, variableName, true)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean hasNestedBlocks(final AstNode node) {
        return node.getChildByRole(Roles.EMBEDDED_STATEMENT) instanceof BlockStatement ||
               node instanceof TryCatchStatement ||
               node instanceof CatchClause ||
               node instanceof SwitchSection;
    }

    private boolean tryConvertAssignmentExpressionIntoVariableDeclaration(
        final BlockStatement block,
        final Statement declarationPoint,
        final AstType type,
        final String variableName) {

        return declarationPoint instanceof ExpressionStatement &&
               tryConvertAssignmentExpressionIntoVariableDeclaration(
                   block,
                   ((ExpressionStatement) declarationPoint).getExpression(),
                   type,
                   variableName
               );
    }

    private boolean tryConvertAssignmentExpressionIntoVariableDeclaration(
        final BlockStatement block,
        final Expression expression,
        final AstType type,
        final String variableName) {

        if (expression instanceof AssignmentExpression) {
            final AssignmentExpression assignment = (AssignmentExpression) expression;

            if (assignment.getOperator() == AssignmentOperatorType.ASSIGN) {
                if (assignment.getLeft() instanceof IdentifierExpression) {
                    final IdentifierExpression identifier = (IdentifierExpression) assignment.getLeft();

                    if (StringUtilities.equals(identifier.getIdentifier(), variableName)) {
                        variablesToDeclare.add(
                            new VariableToDeclare(
                                type,
                                variableName,
                                identifier.getUserData(Keys.VARIABLE),
                                assignment,
                                block
                            )
                        );

                        return true;
                    }
                }
            }
        }

        return false;
    }

    // <editor-fold defaultstate="collapsed" desc="VariableToDeclare Class">

    protected final static class VariableToDeclare {
        private final AstType _type;
        private final String _name;
        private final Variable _variable;
        private final Statement _insertionPoint;
        private final AssignmentExpression _replacedAssignment;
        private final BlockStatement _block;

        public VariableToDeclare(
            final AstType type,
            final String name,
            final Variable variable,
            final Statement insertionPoint,
            final BlockStatement block) {

            _type = type;
            _name = name;
            _variable = variable;
            _insertionPoint = insertionPoint;
            _replacedAssignment = null;
            _block = block;
        }

        public VariableToDeclare(
            final AstType type,
            final String name,
            final Variable variable,
            final AssignmentExpression replacedAssignment,
            final BlockStatement block) {

            _type = type;
            _name = name;
            _variable = variable;
            _insertionPoint = null;
            _replacedAssignment = replacedAssignment;
            _block = block;
        }

        public BlockStatement getBlock() {
            return _block;
        }

        public AstType getType() {
            return _type;
        }

        public String getName() {
            return _name;
        }

        public Variable getVariable() {
            return _variable;
        }

        public AssignmentExpression getReplacedAssignment() {
            return _replacedAssignment;
        }

        public Statement getInsertionPoint() {
            return _insertionPoint;
        }

        @Override
        public String toString() {
            return "VariableToDeclare{" +
                   "Type=" + _type +
                   ", Name='" + _name + '\'' +
                   ", Variable=" + _variable +
                   ", InsertionPoint=" + _insertionPoint +
                   ", ReplacedAssignment=" + _replacedAssignment +
                   '}';
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="IsSingleAssignmentVisitor Class">

    private final class IsSingleAssignmentVisitor extends DepthFirstAstVisitor<Void, Boolean> {
        private final String _variableName;
        private final AssignmentExpression _replacedAssignment;
        private boolean _abort;
        private int _loopOrTryDepth;
        private int _assignmentCount;

        IsSingleAssignmentVisitor(final String variableName, final AssignmentExpression replacedAssignment) {
            _variableName = VerifyArgument.notNull(variableName, "variableName");
            _replacedAssignment = replacedAssignment;
        }

        final boolean isAssigned() {
            return _assignmentCount > 0 && !_abort;
        }

        final boolean isSingleAssignment() {
            return _assignmentCount < 2 && !_abort;
        }

        @Override
        protected Boolean visitChildren(final AstNode node, final Void data) {
            if (_abort) {
                return Boolean.FALSE;
            }
            return super.visitChildren(node, data);
        }

        @Override
        public Boolean visitForStatement(final ForStatement node, final Void p) {
            if (_abort) {
                return Boolean.FALSE;
            }

            ++_loopOrTryDepth;

            try {
                return super.visitForStatement(node, p);
            }
            finally {
                --_loopOrTryDepth;
            }
        }

        @Override
        public Boolean visitIfElseStatement(final IfElseStatement node, final Void data) {
            return visitCondition(node.getCondition(), node.getTrueStatement(), node.getFalseStatement());
        }

        @Override
        public Boolean visitConditionalExpression(final ConditionalExpression node, final Void data) {
            return visitCondition(node.getCondition(), node.getTrueExpression(), node.getFalseExpression());
        }

        private Boolean visitCondition(final AstNode condition, final AstNode ifTrue, final AstNode ifFalse) {
            if (_abort) {
                return Boolean.FALSE;
            }

            condition.acceptVisitor(this, null);

            final int originalAssignmentCount = _assignmentCount;

            _assignmentCount = 0;

            try {
                ifTrue.acceptVisitor(this, null);

                if (_assignmentCount > 0) {
                    _abort = true;
                }
                else {
                    ifFalse.acceptVisitor(this, null);
                    _abort |= _assignmentCount > 0;
                }
            }
            finally {
                _assignmentCount += originalAssignmentCount;
            }

            _abort |= _assignmentCount > 1;

            return !_abort;
        }

        @Override
        public Boolean visitForEachStatement(final ForEachStatement node, final Void p) {
            if (_abort) {
                return Boolean.FALSE;
            }

            ++_loopOrTryDepth;

            try {
                if (StringUtilities.equals(node.getVariableName(), _variableName)) {
                    ++_assignmentCount;
                }
                return super.visitForEachStatement(node, p);
            }
            finally {
                --_loopOrTryDepth;
            }
        }

        @Override
        public Boolean visitDoWhileStatement(final DoWhileStatement node, final Void p) {
            if (_abort) {
                return Boolean.FALSE;
            }

            ++_loopOrTryDepth;

            try {
                return super.visitDoWhileStatement(node, p);
            }
            finally {
                --_loopOrTryDepth;
            }
        }

        @Override
        public Boolean visitWhileStatement(final WhileStatement node, final Void p) {
            if (_abort) {
                return Boolean.FALSE;
            }

            ++_loopOrTryDepth;

            try {
                return super.visitWhileStatement(node, p);
            }
            finally {
                --_loopOrTryDepth;
            }
        }

        @Override
        public Boolean visitTryCatchStatement(final TryCatchStatement node, final Void data) {
            if (_abort) {
                return Boolean.FALSE;
            }

            ++_loopOrTryDepth;

            try {
                return super.visitTryCatchStatement(node, data);
            }
            finally {
                --_loopOrTryDepth;
            }
        }

        @Override
        public Boolean visitAssignmentExpression(final AssignmentExpression node, final Void p) {
            if (_abort) {
                return Boolean.FALSE;
            }

            final Expression left = node.getLeft();

            if (left instanceof IdentifierExpression &&
                StringUtilities.equals(((IdentifierExpression) left).getIdentifier(), _variableName)) {

                if (_loopOrTryDepth != 0 && _replacedAssignment != node) {
                    _abort = true;
                    return Boolean.FALSE;
                }

                ++_assignmentCount;
            }

            return super.visitAssignmentExpression(node, p);
        }

        @Override
        public Boolean visitTypeDeclaration(final TypeDeclaration node, final Void data) {
            return !_abort;
        }

        @Override
        public Boolean visitUnaryOperatorExpression(final UnaryOperatorExpression node, final Void p) {
            if (_abort) {
                return Boolean.FALSE;
            }

            final Expression operand = node.getExpression();

            switch (node.getOperator()) {
                case INCREMENT:
                case DECREMENT:
                case POST_INCREMENT:
                case POST_DECREMENT: {
                    if (operand instanceof IdentifierExpression &&
                        StringUtilities.equals(((IdentifierExpression) operand).getIdentifier(), _variableName)) {

                        if (_loopOrTryDepth != 0) {
                            _abort = true;
                            return Boolean.FALSE;
                        }

                        if (_assignmentCount == 0) {
                            ++_assignmentCount;
                        }

                        ++_assignmentCount;
                    }
                    break;
                }
            }

            return super.visitUnaryOperatorExpression(node, p);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ParameterAssignmentVisitor Class">

    private final class ParameterAssignmentVisitor extends DepthFirstAstVisitor<Void, Boolean> {
        private final Set<ParameterDefinition> _unassignedParameters;
        private final Map<String, ParameterDefinition> _parametersByName;

        ParameterAssignmentVisitor(
            final Set<ParameterDefinition> unassignedParameters,
            final Map<String, ParameterDefinition> parametersByName) {

            _unassignedParameters = unassignedParameters;
            _parametersByName = parametersByName;

            for (final ParameterDefinition p : unassignedParameters) {
                _parametersByName.put(p.getName(), p);
            }
        }

        @Override
        protected Boolean visitChildren(final AstNode node, final Void data) {
            return super.visitChildren(node, data);
        }

        @Override
        public Boolean visitAssignmentExpression(final AssignmentExpression node, final Void p) {
            final Expression left = node.getLeft();
            final Variable variable = left.getUserData(Keys.VARIABLE);

            if (variable != null && variable.isParameter()) {
                _unassignedParameters.remove(variable.getOriginalParameter());
                return super.visitAssignmentExpression(node, p);
            }

            ParameterDefinition parameter = left.getUserData(Keys.PARAMETER_DEFINITION);

            if (parameter == null && left instanceof IdentifierExpression) {
                parameter = _parametersByName.get(((IdentifierExpression) left).getIdentifier());
            }

            if (parameter != null) {
                _unassignedParameters.remove(parameter);
            }

            return super.visitAssignmentExpression(node, p);
        }

        @Override
        public Boolean visitTypeDeclaration(final TypeDeclaration node, final Void data) {
            return null;
        }

        @Override
        public Boolean visitUnaryOperatorExpression(final UnaryOperatorExpression node, final Void p) {
            final Expression operand = node.getExpression();

            switch (node.getOperator()) {
                case INCREMENT:
                case DECREMENT:
                case POST_INCREMENT:
                case POST_DECREMENT: {
                    ParameterDefinition parameter = operand.getUserData(Keys.PARAMETER_DEFINITION);

                    if (parameter == null && operand instanceof IdentifierExpression) {
                        parameter = _parametersByName.get(((IdentifierExpression) operand).getIdentifier());
                    }

                    if (parameter != null) {
                        _unassignedParameters.remove(parameter);
                    }

                    break;
                }
            }

            return super.visitUnaryOperatorExpression(node, p);
        }
    }

    // </editor-fold>
}
