
/*
 * DeclareLocalClassesTransform.java
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
import com.strobel.assembler.metadata.IGenericInstance;
import com.strobel.assembler.metadata.MetadataHelper;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.ParameterDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.StringUtilities;
import com.strobel.core.StrongBox;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;

import java.util.*;

import static com.strobel.core.CollectionUtilities.*;

@SuppressWarnings("ProtectedField")
public class DeclareLocalClassesTransform implements IAstTransform {
    protected final DecompilerContext context;
    protected final AstBuilder astBuilder;

    public DeclareLocalClassesTransform(final DecompilerContext context) {
        this.context = VerifyArgument.notNull(context, "context");
        this.astBuilder = context.getUserData(Keys.AST_BUILDER);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void run(final AstNode node) {
        if (astBuilder == null) {
            return;
        }

        run(node, null);
    }

    private void run(final AstNode node, final DefiniteAssignmentAnalysis daa) {
        DefiniteAssignmentAnalysis analysis = daa;

        if (node instanceof MethodDeclaration) {
            final MethodDeclaration method = (MethodDeclaration) node;
            final List<TypeDeclaration> localTypes = new ArrayList<>();

            for (final TypeDeclaration localType : method.getDeclaredTypes()) {
                localTypes.add(localType);
            }

            if (!localTypes.isEmpty()) {
                //
                // Remove old variable declarations.
                //
                for (final TypeDeclaration localType : localTypes) {
                    localType.remove();
                }

                if (analysis == null) {
                    analysis = new DefiniteAssignmentAnalysis(method.getBody(), new JavaResolver(context));
                }

                boolean madeProgress;

                final Set<TypeToDeclare> typesToDeclare = new LinkedHashSet<>();

                do {
                    madeProgress = false;

                    //
                    // Run through the unplaced local classes and try to find the latest possible declaration
                    // site based on where the classes are referenced.
                    //

                    for (final Iterator<TypeDeclaration> iterator = localTypes.iterator(); iterator.hasNext(); ) {
                        final TypeDeclaration localType = iterator.next();

                        if (declareTypeInBlock(method.getBody(), localType, true, typesToDeclare)) {
                            madeProgress = true;
                            iterator.remove();
                        }
                    }

                    if (!madeProgress && !localTypes.isEmpty()) {
                        //
                        // We have some unplaced local class declarations, but we didn't find any dependent
                        // statements to use as insertion points.  There may still be interdependencies among
                        // those as-of-yet undeclared classes, so insert any one of them at the beginning of
                        // the method, then try once more to place the remaining declarations.
                        //

                        final TypeDeclaration firstUndeclared = first(localTypes);

                        method.getBody().insertChildBefore(
                            method.getBody().getFirstChild(),
                            new LocalTypeDeclarationStatement(Expression.MYSTERY_OFFSET, firstUndeclared),
                            BlockStatement.STATEMENT_ROLE
                        );

                        madeProgress = true;
                        localTypes.remove(0);
                    }

                    for (final TypeToDeclare v : typesToDeclare) {
                        final BlockStatement block = (BlockStatement) v.getInsertionPoint().getParent();

                        if (block == null) {
                            continue;
                        }

                        Statement insertionPoint = v.getInsertionPoint();

                        while (insertionPoint.getPreviousSibling() instanceof LabelStatement) {
                            insertionPoint = (Statement) insertionPoint.getPreviousSibling();
                        }

                        block.insertChildBefore(
                            insertionPoint,
                            new LocalTypeDeclarationStatement(Expression.MYSTERY_OFFSET, v.getDeclaration()),
                            BlockStatement.STATEMENT_ROLE
                        );
                    }

                    typesToDeclare.clear();

                    //
                    // We might not have found insertion points for all our local classes, but if succeeded
                    // for any of them, then we might have introduced the dependencies we were looking for.
                    // If we made at least some progress, run through the list again.
                    //
                }
                while (madeProgress && !localTypes.isEmpty());
            }
        }

        for (AstNode child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof TypeDeclaration) {
                final TypeDefinition currentType = context.getCurrentType();
                final MethodDefinition currentMethod = context.getCurrentMethod();

                context.setCurrentType(null);
                context.setCurrentMethod(null);

                try {
                    final TypeDefinition type = child.getUserData(Keys.TYPE_DEFINITION);

                    if (type != null && type.isInterface()) {
                        continue;
                    }

                    new DeclareLocalClassesTransform(context).run(child);
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
    private boolean declareTypeInBlock(
        final BlockStatement block,
        final TypeDeclaration type,
        final boolean allowPassIntoLoops,
        final Set<TypeToDeclare> typesToDeclare) {

        //
        // The point at which the variable would be declared if we decide to declare it in this block.
        //
        final StrongBox<Statement> declarationPoint = new StrongBox<>();
        final TypeDefinition typeDefinition = type.getUserData(Keys.TYPE_DEFINITION);

        final boolean canMoveVariableIntoSubBlocks = findDeclarationPoint(
            typeDefinition,
            allowPassIntoLoops,
            block,
            declarationPoint,
            null
        );

        if (declarationPoint.get() == null) {
            //
            // The variable isn't used at all.
            //
            return false;
        }

        if (canMoveVariableIntoSubBlocks) {
            for (final Statement statement : block.getStatements()) {
                if (!referencesType(statement, typeDefinition)) {
                    continue;
                }

                for (final AstNode child : statement.getChildren()) {
                    if (child instanceof BlockStatement) {
                        if (declareTypeInBlock((BlockStatement) child, type, allowPassIntoLoops, typesToDeclare)) {
                            return true;
                        }
                    }
                    else if (hasNestedBlocks(child)) {
                        for (final AstNode nestedChild : child.getChildren()) {
                            if (nestedChild instanceof BlockStatement &&
                                declareTypeInBlock((BlockStatement) nestedChild, type, allowPassIntoLoops, typesToDeclare)) {

                                return true;
                            }
                        }
                    }
                }

                final boolean canStillMoveIntoSubBlocks = findDeclarationPoint(
                    typeDefinition,
                    allowPassIntoLoops,
                    block,
                    declarationPoint,
                    statement
                );

                if (!canStillMoveIntoSubBlocks && declarationPoint.get() != null) {
                    final TypeToDeclare vtd = new TypeToDeclare(type, typeDefinition, declarationPoint.get(), block);
                    typesToDeclare.add(vtd);
                    return true;
                }
            }

            return false;
        }
        else {
            final TypeToDeclare vtd = new TypeToDeclare(type, typeDefinition, declarationPoint.get(), block);
            typesToDeclare.add(vtd);
            return true;
        }
    }

    public static boolean findDeclarationPoint(
        final TypeDeclaration declaration,
        final BlockStatement block,
        final StrongBox<Statement> declarationPoint,
        final Statement skipUpThrough) {

        return findDeclarationPoint(declaration.getUserData(Keys.TYPE_DEFINITION), true, block, declarationPoint, skipUpThrough);
    }

    static boolean findDeclarationPoint(
        final TypeReference localType,
        final boolean allowPassIntoLoops,
        final BlockStatement block,
        final StrongBox<Statement> declarationPoint,
        final Statement skipUpThrough) {

        declarationPoint.set(null);

        Statement waitFor = skipUpThrough;

        for (final Statement statement : block.getStatements()) {
            if (waitFor != null) {
                if (statement == waitFor) {
                    waitFor = null;
                }
                continue;
            }

            if (referencesType(statement, localType)) {
                if (declarationPoint.get() != null) {
                    return false;
                }

                declarationPoint.set(statement);

                if (!canMoveLocalTypeIntoSubBlock(statement, localType, allowPassIntoLoops)) {
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

                AstNode nextNode = statement.getNextSibling();

                while (nextNode != null) {
                    if (referencesType(nextNode, localType)) {
                        return false;
                    }
                    nextNode = nextNode.getNextSibling();
                }
            }
        }

        return true;
    }

    private static boolean canMoveLocalTypeIntoSubBlock(
        final Statement statement,
        final TypeReference localType,
        final boolean allowPassIntoLoops) {

        if (!allowPassIntoLoops && AstNode.isLoop(statement)) {
            return false;
        }

        //
        // We can move the local class into a sub-block only if the local class is used only in that
        // sub-block (and not in expressions such as the loop condition).
        //

        for (AstNode child = statement.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (!(child instanceof BlockStatement) && referencesType(child, localType)) {
                if (hasNestedBlocks(child)) {
                    //
                    // Loops, catch clauses, switch sections, and labeled statements can contain nested blocks.
                    //
                    for (AstNode grandChild = child.getFirstChild(); grandChild != null; grandChild = grandChild.getNextSibling()) {
                        if (!(grandChild instanceof BlockStatement) && referencesType(grandChild, localType)) {
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

    private static boolean referencesType(final AstType reference, final TypeReference localType) {
        return reference != null &&
               referencesType(reference.getUserData(Keys.TYPE_REFERENCE), localType);
    }

    private static boolean referencesType(final TypeReference reference, final TypeReference localType) {
        if (reference == null || localType  == null) {
            return false;
        }

        TypeReference type = reference;

        while (type.isArray()) {
            type = type.getElementType();
        }

        TypeReference target = localType;

        while (target.isArray()) {
            target = target.getElementType();
        }

        if (StringUtilities.equals(type.getInternalName(), target.getInternalName())) {
            return true;
        }

        if (type.hasExtendsBound()) {
            final TypeReference bound = type.getExtendsBound();

            if (!bound.isGenericParameter() && !MetadataHelper.isSameType(bound, type) && referencesType(bound, localType)) {
                return true;
            }
        }

        if (type.hasSuperBound()) {
            final TypeReference bound = type.getSuperBound();

            if (!bound.isGenericParameter() && !MetadataHelper.isSameType(bound, type) && referencesType(bound, localType)) {
                return true;
            }
        }

        if (type.isGenericType()) {
            if (type instanceof IGenericInstance) {
                final List<TypeReference> typeArguments = ((IGenericInstance) type).getTypeArguments();

                for (final TypeReference typeArgument : typeArguments) {
                    if (!MetadataHelper.isSameType(typeArgument, type) && referencesType(typeArgument, localType)) {
                        return true;
                    }
                }
            }
            else {
                for (final TypeReference typeArgument : type.getGenericParameters()) {
                    if (!MetadataHelper.isSameType(typeArgument, type) && referencesType(typeArgument, localType)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean referencesType(final AstNode node, final TypeReference localType) {
        if (node instanceof AnonymousObjectCreationExpression) {
            for (final Expression argument : ((AnonymousObjectCreationExpression) node).getArguments()) {
                if (referencesType(argument, localType)) {
                    return true;
                }
            }
            return false;
        }

        if (node instanceof LocalTypeDeclarationStatement) {
            return referencesType(((LocalTypeDeclarationStatement) node).getTypeDeclaration(), localType);
        }

        if (node instanceof TypeDeclaration) {
            final TypeDeclaration type = (TypeDeclaration) node;

            final AstType baseType = type.getBaseType();

            if (baseType != null && !baseType.isNull() && referencesType(baseType, localType)) {
                return true;
            }

            for (final AstType ifType : type.getInterfaces()) {
                if (referencesType(ifType, localType)) {
                    return true;
                }
            }

            for (final FieldDeclaration field : ofType(type.getMembers(), FieldDeclaration.class)) {
                final FieldDefinition fieldDefinition = field.getUserData(Keys.FIELD_DEFINITION);

                if (fieldDefinition != null &&
                    StringUtilities.equals(fieldDefinition.getFieldType().getInternalName(), localType.getInternalName())) {

                    return true;
                }

                if (!field.getVariables().isEmpty() && referencesType(first(field.getVariables()), localType)) {
                    return true;
                }
            }

            for (final MethodDeclaration method : ofType(type.getMembers(), MethodDeclaration.class)) {
                final MethodDefinition methodDefinition = method.getUserData(Keys.METHOD_DEFINITION);

                if (methodDefinition != null) {
                    if (StringUtilities.equals(methodDefinition.getReturnType().getInternalName(), localType.getInternalName())) {
                        return true;
                    }

                    for (final ParameterDefinition parameter : methodDefinition.getParameters()) {
                        if (StringUtilities.equals(parameter.getParameterType().getInternalName(), localType.getInternalName())) {
                            return true;
                        }
                    }
                }

                if (referencesType(method.getBody(), localType)) {
                    return true;
                }
            }

            return false;
        }

        if (node instanceof AstType) {
            return referencesType((AstType) node, localType);
        }

        if (node instanceof ForStatement) {
            final ForStatement forLoop = (ForStatement) node;

            for (final Statement statement : forLoop.getInitializers()) {
                if (statement instanceof VariableDeclarationStatement) {
                    final AstType type = ((VariableDeclarationStatement) statement).getType();

                    if (referencesType(type, localType)) {
                        return true;
                    }
                }
            }
        }

        if (node instanceof ForEachStatement) {
            final ForEachStatement forEach = (ForEachStatement) node;

            if (referencesType(forEach.getVariableType(), localType)) {
                return true;
            }
        }

        if (node instanceof TryCatchStatement) {
            final TryCatchStatement tryCatch = (TryCatchStatement) node;

            for (final VariableDeclarationStatement resource : tryCatch.getResources()) {
                if (referencesType(resource.getType(), localType)) {
                    return true;
                }
            }
        }

        if (node instanceof CatchClause) {
            for (final AstType type : ((CatchClause) node).getExceptionTypes()) {
                if (referencesType(type, localType))
                    return true;
            }
        }

        for (AstNode child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (referencesType(child, localType)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasNestedBlocks(final AstNode node) {
        return node.getChildByRole(Roles.EMBEDDED_STATEMENT) instanceof BlockStatement ||
               node instanceof TryCatchStatement ||
               node instanceof CatchClause ||
               node instanceof SwitchSection;
    }

    // <editor-fold defaultstate="collapsed" desc="VariableToDeclare Class">

    protected final static class TypeToDeclare {
        private final TypeDeclaration _declaration;
        private final TypeDefinition _typeDefinition;
        private final Statement _insertionPoint;
        private final BlockStatement _block;

        public TypeToDeclare(
            final TypeDeclaration declaration,
            final TypeDefinition definition,
            final Statement insertionPoint,
            final BlockStatement block) {

            _declaration = declaration;
            _typeDefinition = definition;
            _insertionPoint = insertionPoint;
            _block = block;
        }

        public BlockStatement getBlock() {
            return _block;
        }

        public TypeDeclaration getDeclaration() {
            return _declaration;
        }

        public TypeDefinition getTypeDefinition() {
            return _typeDefinition;
        }

        public Statement getInsertionPoint() {
            return _insertionPoint;
        }

        @Override
        public String toString() {
            return "TypeToDeclare{" +
                   "Type=" + _typeDefinition.getSignature() +
                   ", InsertionPoint=" + _insertionPoint +
                   '}';
        }
    }

    // </editor-fold>
}