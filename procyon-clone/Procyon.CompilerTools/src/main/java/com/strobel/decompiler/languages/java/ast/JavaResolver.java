/*
 * JavaResolver.java
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

import com.strobel.assembler.metadata.*;
import com.strobel.core.Comparer;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.ast.Variable;
import com.strobel.decompiler.semantics.ResolveResult;
import com.strobel.functions.Function;

public class JavaResolver implements Function<AstNode, ResolveResult> {
    private final DecompilerContext _context;

    public JavaResolver(final DecompilerContext context) {
        _context = VerifyArgument.notNull(context, "context");
    }

    @Override
    public ResolveResult apply(final AstNode input) {
        return input.acceptVisitor(new ResolveVisitor(_context), null);
    }

    private final static class ResolveVisitor extends ContextTrackingVisitor<ResolveResult> {
        protected ResolveVisitor(final DecompilerContext context) {
            super(context);
        }

        @Override
        public ResolveResult visitVariableDeclaration(final VariableDeclarationStatement node, final Void data) {
            return resolveType(node.getType());
        }

        @Override
        public ResolveResult visitVariableInitializer(final VariableInitializer node, final Void data) {
            return node.getInitializer().acceptVisitor(this, data);
        }

        @Override
        public ResolveResult visitObjectCreationExpression(final ObjectCreationExpression node, final Void p) {
/*
            final ResolveResult result = resolveTypeFromMember(node.getUserData(Keys.MEMBER_REFERENCE));

            if (result != null) {
                return result;
            }
*/
            return node.getType().acceptVisitor(this, p);
        }

        @Override
        public ResolveResult visitAnonymousObjectCreationExpression(final AnonymousObjectCreationExpression node, final Void p) {
            final ResolveResult result = resolveTypeFromMember(node.getUserData(Keys.MEMBER_REFERENCE));

            if (result != null) {
                return result;
            }

            return node.getType().acceptVisitor(this, p);
        }

        @Override
        public ResolveResult visitComposedType(final ComposedType node, final Void p) {
            return resolveType(node.toTypeReference());
        }

        @Override
        public ResolveResult visitSimpleType(final SimpleType node, final Void p) {
            return resolveType(node.toTypeReference());
        }

        @Override
        public ResolveResult visitThisReferenceExpression(final ThisReferenceExpression node, final Void data) {
            if (node.getTarget().isNull()) {
                return resolveType(node.getUserData(Keys.TYPE_REFERENCE));
            }
            return node.getTarget().acceptVisitor(this, data);
        }

        @Override
        public ResolveResult visitSuperReferenceExpression(final SuperReferenceExpression node, final Void data) {
            if (node.getTarget().isNull()) {
                return resolveType(node.getUserData(Keys.TYPE_REFERENCE));
            }
            return node.getTarget().acceptVisitor(this, data);
        }

        @Override
        public ResolveResult visitTypeReference(final TypeReferenceExpression node, final Void p) {
            return resolveType(node.getType().getUserData(Keys.TYPE_REFERENCE));
        }

        @Override
        public ResolveResult visitWildcardType(final WildcardType node, final Void p) {
            return resolveType(node.toTypeReference());
        }

        @Override
        public ResolveResult visitIdentifier(final Identifier node, final Void p) {
            final ResolveResult result = resolveTypeFromMember(node.getUserData(Keys.MEMBER_REFERENCE));

            if (result != null) {
                return result;
            }

            return resolveTypeFromVariable(node.getUserData(Keys.VARIABLE));
        }

        @Override
        public ResolveResult visitIdentifierExpression(final IdentifierExpression node, final Void data) {
            ResolveResult result = resolveTypeFromMember(node.getUserData(Keys.MEMBER_REFERENCE));

            if (result != null) {
                return result;
            }

            final Variable variable = node.getUserData(Keys.VARIABLE);

            if (variable == null) {
                return null;
            }

/*
            if (variable.isParameter()) {
                for (final AstNode n : node.getAncestors()) {
                    final AstNodeCollection<ParameterDeclaration> parameters = n.getChildrenByRole(Roles.PARAMETER);

                    if (parameters.isEmpty()) {
                        continue;
                    }

                    for (final ParameterDeclaration p : parameters) {
                        final ParameterDefinition definition = p.getUserData(Keys.PARAMETER_DEFINITION);

                        if (definition != null) {
                            if (StringUtilities.equals(p.getName(), variable.getName())) {
                                return resolveType(definition.getParameterType());
                            }
                        }
                    }
                }
            }
*/

            result = resolveTypeFromVariable(variable);

            if (result != null) {
                return result;
            }

            return super.visitIdentifierExpression(node, data);
        }

        protected ResolveResult resolveLambda(final AstNode node) {
            final TypeReference lambdaType = node.getUserData(Keys.TYPE_REFERENCE);

            if (lambdaType != null) {
                return resolveType(lambdaType);
            }

            final DynamicCallSite callSite = node.getUserData(Keys.DYNAMIC_CALL_SITE);

            if (callSite != null) {
                return resolveType(callSite.getMethodType().getReturnType());
            }

            return null;
        }

        @Override
        public ResolveResult visitMethodGroupExpression(final MethodGroupExpression node, final Void data) {
            return resolveLambda(node);
        }

        @Override
        public ResolveResult visitLambdaExpression(final LambdaExpression node, final Void data) {
            return resolveLambda(node);
        }

        @Override
        public ResolveResult visitMemberReferenceExpression(final MemberReferenceExpression node, final Void p) {
            final ResolveResult targetResult = node.getTarget().acceptVisitor(this, p);

            MemberReference memberReference = node.getUserData(Keys.MEMBER_REFERENCE);

            if (memberReference == null) {
                if (StringUtilities.equals(node.getMemberName(), "length")) {
                    if (targetResult != null &&
                        targetResult.getType() != null &&
                        targetResult.getType().isArray()) {

                        return new ResolveResult(BuiltinTypes.Integer);
                    }
                }
                if (node.getParent() instanceof InvocationExpression) {
                    memberReference = node.getParent().getUserData(Keys.MEMBER_REFERENCE);
                }
            }
            else if (targetResult != null &&
                     targetResult.getType() != null) {

                if (memberReference instanceof FieldReference) {
                    final FieldDefinition resolvedField = ((FieldReference) memberReference).resolve();

                    memberReference = MetadataHelper.asMemberOf(
                        resolvedField != null ? resolvedField : (FieldReference) memberReference,
                        targetResult.getType()
                    );
                }
                else {
                    final MethodDefinition resolvedMethod = ((MethodReference) memberReference).resolve();

                    memberReference = MetadataHelper.asMemberOf(
                        resolvedMethod != null ? resolvedMethod : (MethodReference) memberReference,
                        targetResult.getType()
                    );
                }
            }

            return resolveTypeFromMember(memberReference);
        }

        @Override
        public ResolveResult visitInvocationExpression(final InvocationExpression node, final Void p) {
            final ResolveResult result = resolveTypeFromMember(node.getUserData(Keys.MEMBER_REFERENCE));

            if (result != null) {
                return result;
            }

            return node.getTarget().acceptVisitor(this, p);
        }

        @Override
        protected ResolveResult visitChildren(final AstNode node, final Void p) {
            ResolveResult result = null;

            AstNode next;

            for (AstNode child = node.getFirstChild(); child != null; child = next) {
                //
                // Store next to allow the loop to continue if the visitor removes/replaces child.
                //
                next = child.getNextSibling();

                if (child instanceof JavaTokenNode) {
                    continue;
                }

                final ResolveResult childResult = child.acceptVisitor(this, p);

                if (childResult == null) {
                    return null;
                }
                else if (result == null) {
                    result = childResult;
                }
                else if (result.isCompileTimeConstant() &&
                         childResult.isCompileTimeConstant() &&
                         Comparer.equals(result.getConstantValue(), childResult.getConstantValue())) {

                    //noinspection UnnecessaryContinue
                    continue;
                }
                else {
                    final TypeReference commonSuperType = doBinaryPromotion(result, childResult);

                    if (commonSuperType != null) {
                        result = new ResolveResult(commonSuperType);
                    }
                    else {
                        return null;
                    }
                }
            }

            return null;
        }

        private TypeReference doBinaryPromotion(final ResolveResult left, final ResolveResult right) {
            final TypeReference leftType = left.getType();
            final TypeReference rightType = right.getType();

            if (leftType == null) {
                return rightType;
            }

            if (rightType == null) {
                return leftType;
            }

            if (StringUtilities.equals(leftType.getInternalName(), "java/lang/String")) {
                return leftType;
            }

            if (StringUtilities.equals(rightType.getInternalName(), "java/lang/String")) {
                return rightType;
            }

            return MetadataHelper.findCommonSuperType(leftType, rightType);
        }

        private TypeReference doBinaryPromotionStrict(final ResolveResult left, final ResolveResult right) {
            if (left == null || right == null) {
                return null;
            }

            TypeReference leftType = left.getType();
            TypeReference rightType = right.getType();

            if (leftType == null || rightType == null) {
                return null;
            }

            leftType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(leftType);
            rightType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(rightType);

            if (StringUtilities.equals(leftType.getInternalName(), "java/lang/String")) {
                return leftType;
            }

            if (StringUtilities.equals(rightType.getInternalName(), "java/lang/String")) {
                return rightType;
            }

            return MetadataHelper.findCommonSuperType(leftType, rightType);
        }

        @Override
        public ResolveResult visitPrimitiveExpression(final PrimitiveExpression node, final Void p) {
            final String literalValue = node.getLiteralValue();
            final Object value = node.getValue();

            final TypeReference primitiveType;

            if (value instanceof String || value == null && literalValue != null) {
                final TypeDefinition currentType = context.getCurrentType();
                final IMetadataResolver resolver = currentType != null ? currentType.getResolver() : MetadataSystem.instance();

                primitiveType = resolver.lookupType("java/lang/String");
            }
            else if (value instanceof Number) {
                if (value instanceof Byte) {
                    primitiveType = BuiltinTypes.Byte;
                }
                else if (value instanceof Short) {
                    primitiveType = BuiltinTypes.Short;
                }
                else if (value instanceof Integer) {
                    primitiveType = BuiltinTypes.Integer;
                }
                else if (value instanceof Long) {
                    primitiveType = BuiltinTypes.Long;
                }
                else if (value instanceof Float) {
                    primitiveType = BuiltinTypes.Float;
                }
                else if (value instanceof Double) {
                    primitiveType = BuiltinTypes.Double;
                }
                else {
                    primitiveType = null;
                }
            }
            else if (value instanceof Character) {
                primitiveType = BuiltinTypes.Character;
            }
            else if (value instanceof Boolean) {
                primitiveType = BuiltinTypes.Boolean;
            }
            else {
                primitiveType = null;
            }

            if (primitiveType == null) {
                return null;
            }

            return new PrimitiveResolveResult(
                primitiveType,
                value != null ? value : literalValue
            );
        }

        @Override
        public ResolveResult visitClassOfExpression(final ClassOfExpression node, final Void data) {
            final TypeReference type = node.getType().getUserData(Keys.TYPE_REFERENCE);

            if (type == null) {
                return null;
            }

            if (BuiltinTypes.Class.isGenericType()) {
                return new ResolveResult(BuiltinTypes.Class.makeGenericType(type));
            }

            return new ResolveResult(BuiltinTypes.Class);
        }

        @Override
        public ResolveResult visitCastExpression(final CastExpression node, final Void data) {
            final ResolveResult childResult = node.getExpression().acceptVisitor(this, data);
            final ResolveResult typeResult = resolveType(node.getType());

            if (typeResult == null) {
                return childResult;
            }

            final TypeReference resolvedType = typeResult.getType();

            if (resolvedType != null) {
                if (resolvedType.isPrimitive() &&
                    childResult != null &&
                    childResult.isCompileTimeConstant()) {

                    return new PrimitiveResolveResult(
                        resolvedType,
                        JavaPrimitiveCast.cast(resolvedType.getSimpleType(), childResult.getConstantValue())
                    );
                }

                return new ResolveResult(resolvedType);
            }

            return typeResult;
        }

        @Override
        public ResolveResult visitNullReferenceExpression(final NullReferenceExpression node, final Void data) {
            return new ResolveResult(BuiltinTypes.Null);
        }

        @Override
        public ResolveResult visitBinaryOperatorExpression(final BinaryOperatorExpression node, final Void data) {
            final ResolveResult leftResult = node.getLeft().acceptVisitor(this, data);
            final ResolveResult rightResult = node.getRight().acceptVisitor(this, data);

            if (leftResult == null || rightResult == null) {
                return null;
            }

            final TypeReference leftType = leftResult.getType();
            final TypeReference rightType = rightResult.getType();

            if (leftType == null || rightType == null) {
                return null;
            }

            final TypeReference operandType = doBinaryPromotionStrict(leftResult, rightResult);

            if (operandType == null) {
                return null;
            }

            final TypeReference resultType;

            switch (node.getOperator()) {
                case LOGICAL_AND:
                case LOGICAL_OR:
                case GREATER_THAN:
                case GREATER_THAN_OR_EQUAL:
                case EQUALITY:
                case INEQUALITY:
                case LESS_THAN:
                case LESS_THAN_OR_EQUAL: {
                    resultType = BuiltinTypes.Boolean;
                    break;
                }

                default: {
                    switch (operandType.getSimpleType()) {
                        case Byte:
                        case Character:
                        case Short:
                            resultType = BuiltinTypes.Integer;
                            break;
                        default:
                            resultType = operandType;
                            break;
                    }
                }
            }

            if (leftResult.isCompileTimeConstant() && rightResult.isCompileTimeConstant()) {
                if (operandType.isPrimitive()) {
                    final Object result = BinaryOperations.doBinary(
                        node.getOperator(),
                        operandType.getSimpleType(),
                        leftResult.getConstantValue(),
                        rightResult.getConstantValue()
                    );

                    if (result != null) {
                        return new PrimitiveResolveResult(resultType, result);
                    }
                }
            }

            return new ResolveResult(resultType);
        }

        @Override
        public ResolveResult visitInstanceOfExpression(final InstanceOfExpression node, final Void data) {
            final ResolveResult childResult = node.getExpression().acceptVisitor(this, data);

            if (childResult == null) {
                return new ResolveResult(BuiltinTypes.Boolean);
            }

            final TypeReference childType = childResult.getType();
            final ResolveResult typeResult = resolveType(node.getType());

            if (childType == null || typeResult == null || typeResult.getType() == null) {
                return new ResolveResult(BuiltinTypes.Boolean);
            }

            return new PrimitiveResolveResult(
                BuiltinTypes.Boolean,
                MetadataHelper.isSubType(typeResult.getType(), childType)
            );
        }

        @Override
        public ResolveResult visitIndexerExpression(final IndexerExpression node, final Void data) {
            final ResolveResult childResult = node.getTarget().acceptVisitor(this, data);

            if (childResult == null || childResult.getType() == null || !childResult.getType().isArray()) {
                return null;
            }

            final TypeReference elementType = childResult.getType().getElementType();

            if (elementType == null) {
                return null;
            }

            return new ResolveResult(elementType);
        }

        @Override
        public ResolveResult visitUnaryOperatorExpression(final UnaryOperatorExpression node, final Void data) {
            final ResolveResult childResult = node.getExpression().acceptVisitor(this, data);

            if (childResult == null || childResult.getType() == null) {
                return null;
            }

            final TypeReference resultType;

            switch (childResult.getType().getSimpleType()) {
                case Byte:
                case Character:
                case Short:
                case Integer:
                    resultType = BuiltinTypes.Integer;
                    break;

                default:
                    resultType = childResult.getType();
            }

            if (childResult.isCompileTimeConstant()) {
                final Object resultValue = UnaryOperations.doUnary(node.getOperator(), childResult.getConstantValue());

                if (resultValue != null) {
                    return new PrimitiveResolveResult(resultType, resultValue);
                }
            }

            return new ResolveResult(resultType);
        }

        @Override
        public ResolveResult visitConditionalExpression(final ConditionalExpression node, final Void data) {
            final ResolveResult conditionResult = node.getCondition().acceptVisitor(this, data);

            if (conditionResult != null &&
                conditionResult.isCompileTimeConstant()) {

                if (Boolean.TRUE.equals(conditionResult.getConstantValue())) {
                    return node.getTrueExpression().acceptVisitor(this, data);
                }

                if (Boolean.FALSE.equals(conditionResult.getConstantValue())) {
                    return node.getFalseExpression().acceptVisitor(this, data);
                }
            }

            final ResolveResult leftResult = node.getTrueExpression().acceptVisitor(this, data);

            if (leftResult == null || leftResult.getType() == null) {
                return null;
            }

            final ResolveResult rightResult = node.getFalseExpression().acceptVisitor(this, data);

            if (rightResult == null || rightResult.getType() == null) {
                return null;
            }

            final TypeReference resultType = MetadataHelper.findCommonSuperType(
                leftResult.getType(),
                rightResult.getType()
            );

            if (resultType != null) {
                if (leftResult.getType().isPrimitive() || rightResult.getType().isPrimitive()) {
                    return new ResolveResult(MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(resultType));
                }
                return new ResolveResult(resultType);
            }

            return null;
        }

        @Override
        public ResolveResult visitArrayCreationExpression(final ArrayCreationExpression node, final Void data) {
            final TypeReference elementType = node.getType().toTypeReference();

            if (elementType == null) {
                return null;
            }

            final int rank = node.getDimensions().size() + node.getAdditionalArraySpecifiers().size();

            TypeReference arrayType = elementType;

            for (int i = 0; i < rank; i++) {
                arrayType = arrayType.makeArrayType();
            }

            return new ResolveResult(arrayType);
        }

        @Override
        public ResolveResult visitAssignmentExpression(final AssignmentExpression node, final Void data) {
            final ResolveResult leftResult = node.getLeft().acceptVisitor(this, data);

            if (leftResult != null && leftResult.getType() != null) {
                return new ResolveResult(leftResult.getType());
            }

            return null;
        }

        @Override
        public ResolveResult visitParenthesizedExpression(final ParenthesizedExpression node, final Void data) {
            return node.getExpression().acceptVisitor(this, data);
        }
    }

    private static ResolveResult resolveTypeFromVariable(final Variable variable) {
        if (variable == null) {
            return null;
        }

        TypeReference type = variable.getType();

        if (type == null) {
            if (variable.isParameter()) {
                final ParameterDefinition parameter = variable.getOriginalParameter();

                if (parameter != null) {
                    type = parameter.getParameterType();
                }
            }

            final VariableDefinition originalVariable = variable.getOriginalVariable();

            if (originalVariable != null) {
                type = originalVariable.getVariableType();
            }
        }

        if (type != null) {
            return new ResolveResult(type);
        }

        return null;
    }

    private static ResolveResult resolveType(final AstType type) {
        if (type == null || type.isNull()) {
            return null;
        }

        return resolveType(type.toTypeReference());
    }

    private static ResolveResult resolveType(final TypeReference type) {
        return type == null ? null : new ResolveResult(type);
    }

    private static ResolveResult resolveTypeFromMember(final MemberReference member) {
        if (member == null) {
            return null;
        }

        if (member instanceof FieldReference) {
            return new ResolveResult(((FieldReference) member).getFieldType());
        }

        if (member instanceof MethodReference) {
            final MethodReference method = (MethodReference) member;

            if (method.isConstructor()) {
                return new ResolveResult(method.getDeclaringType());
            }

            return new ResolveResult(method.getReturnType());
        }

        return null;
    }

    private final static class PrimitiveResolveResult extends ResolveResult {
        private final Object _value;

        private PrimitiveResolveResult(final TypeReference type, final Object value) {
            super(type);
            _value = value;
        }

        @Override
        public boolean isCompileTimeConstant() {
            return true;
        }

        @Override
        public Object getConstantValue() {
            return _value;
        }
    }

    private final static class BinaryOperations {
        static Object doBinary(final BinaryOperatorType operator, final JvmType type, final Object left, final Object right) {
            switch (operator) {
                case BITWISE_AND:
                    return and(type, left, right);
                case BITWISE_OR:
                    return or(type, left, right);
                case EXCLUSIVE_OR:
                    return xor(type, left, right);

                case LOGICAL_AND:
                    return andAlso(left, right);
                case LOGICAL_OR:
                    return orElse(left, right);

                case GREATER_THAN:
                    return greaterThan(type, left, right);
                case GREATER_THAN_OR_EQUAL:
                    return greaterThanOrEqual(type, left, right);
                case EQUALITY:
                    return equal(type, left, right);
                case INEQUALITY:
                    return notEqual(type, left, right);
                case LESS_THAN:
                    return lessThan(type, left, right);
                case LESS_THAN_OR_EQUAL:
                    return lessThanOrEqual(type, left, right);

                case ADD:
                    return add(type, left, right);
                case SUBTRACT:
                    return subtract(type, left, right);
                case MULTIPLY:
                    return multiply(type, left, right);
                case DIVIDE:
                    return divide(type, left, right);
                case MODULUS:
                    return remainder(type, left, right);

                case SHIFT_LEFT:
                    return leftShift(type, left, right);
                case SHIFT_RIGHT:
                    return rightShift(type, left, right);
                case UNSIGNED_SHIFT_RIGHT:
                    return unsignedRightShift(type, left, right);

                default:
                    return null;
            }
        }

        private static Object add(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch (type) {
                    case Byte:
                        return (byte) (((Number) left).intValue() + ((Number) right).intValue());
                    case Character:
                        return (char) ((Number) left).intValue() + ((Number) right).intValue();
                    case Short:
                        return (short) ((Number) left).intValue() + ((Number) right).intValue();
                    case Integer:
                        return ((Number) left).intValue() + ((Number) right).intValue();
                    case Long:
                        return ((Number) left).longValue() + ((Number) right).longValue();
                    case Float:
                        return ((Number) left).floatValue() + ((Number) right).floatValue();
                    case Double:
                        return ((Number) left).doubleValue() + ((Number) right).doubleValue();
                }
            }

            return null;
        }

        private static Object subtract(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch (type) {
                    case Byte:
                        return (byte) (((Number) left).intValue() - ((Number) right).intValue());
                    case Character:
                        return (char) ((Number) left).intValue() - ((Number) right).intValue();
                    case Short:
                        return (short) ((Number) left).intValue() - ((Number) right).intValue();
                    case Integer:
                        return ((Number) left).intValue() - ((Number) right).intValue();
                    case Long:
                        return ((Number) left).longValue() - ((Number) right).longValue();
                    case Float:
                        return ((Number) left).floatValue() - ((Number) right).floatValue();
                    case Double:
                        return ((Number) left).doubleValue() - ((Number) right).doubleValue();
                }
            }

            return null;
        }

        private static Object multiply(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch (type) {
                    case Byte:
                        return (byte) (((Number) left).intValue() * ((Number) right).intValue());
                    case Character:
                        return (char) ((Number) left).intValue() * ((Number) right).intValue();
                    case Short:
                        return (short) ((Number) left).intValue() * ((Number) right).intValue();
                    case Integer:
                        return ((Number) left).intValue() * ((Number) right).intValue();
                    case Long:
                        return ((Number) left).longValue() * ((Number) right).longValue();
                    case Float:
                        return ((Number) left).floatValue() * ((Number) right).floatValue();
                    case Double:
                        return ((Number) left).doubleValue() * ((Number) right).doubleValue();
                }
            }

            return null;
        }

        private static Object divide(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                if (type.isIntegral() && ((Number) right).longValue() == 0L) {
                    return null;
                }
                switch (type) {
                    case Byte:
                        return (byte) (((Number) left).intValue() / ((Number) right).intValue());
                    case Character:
                        return (char) ((Number) left).intValue() / ((Number) right).intValue();
                    case Short:
                        return (short) ((Number) left).intValue() / ((Number) right).intValue();
                    case Integer:
                        return ((Number) left).intValue() / ((Number) right).intValue();
                    case Long:
                        return ((Number) left).longValue() / ((Number) right).longValue();
                    case Float:
                        return ((Number) left).floatValue() / ((Number) right).floatValue();
                    case Double:
                        return ((Number) left).doubleValue() / ((Number) right).doubleValue();
                }
            }

            return null;
        }

        private static Object remainder(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch (type) {
                    case Byte:
                        return (byte) (((Number) left).intValue() % ((Number) right).intValue());
                    case Character:
                        return (char) ((Number) left).intValue() % ((Number) right).intValue();
                    case Short:
                        return (short) ((Number) left).intValue() % ((Number) right).intValue();
                    case Integer:
                        return ((Number) left).intValue() % ((Number) right).intValue();
                    case Long:
                        return ((Number) left).longValue() % ((Number) right).longValue();
                    case Float:
                        return ((Number) left).floatValue() % ((Number) right).floatValue();
                    case Double:
                        return ((Number) left).doubleValue() % ((Number) right).doubleValue();
                }
            }

            return null;
        }

        private static Object and(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch (type) {
                    case Byte:
                        return (byte) (((Number) left).intValue() & ((Number) right).intValue());
                    case Character:
                        return (char) ((Number) left).intValue() & ((Number) right).intValue();
                    case Short:
                        return (short) ((Number) left).intValue() & ((Number) right).intValue();
                    case Integer:
                        return ((Number) left).intValue() & ((Number) right).intValue();
                    case Long:
                        return ((Number) left).longValue() & ((Number) right).longValue();
                }
            }

            return null;
        }

        private static Object or(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch (type) {
                    case Byte:
                        return (byte) (((Number) left).intValue() | ((Number) right).intValue());
                    case Character:
                        return (char) ((Number) left).intValue() | ((Number) right).intValue();
                    case Short:
                        return (short) ((Number) left).intValue() | ((Number) right).intValue();
                    case Integer:
                        return ((Number) left).intValue() | ((Number) right).intValue();
                    case Long:
                        return ((Number) left).longValue() | ((Number) right).longValue();
                }
            }

            return null;
        }

        private static Object xor(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch (type) {
                    case Byte:
                        return (byte) (((Number) left).intValue() ^ ((Number) right).intValue());
                    case Character:
                        return (char) ((Number) left).intValue() ^ ((Number) right).intValue();
                    case Short:
                        return (short) ((Number) left).intValue() ^ ((Number) right).intValue();
                    case Integer:
                        return ((Number) left).intValue() ^ ((Number) right).intValue();
                    case Long:
                        return ((Number) left).longValue() ^ ((Number) right).longValue();
                }
            }

            return null;
        }

        private static Object leftShift(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch (type) {
                    case Byte:
                        return (byte) (((Number) left).intValue() << ((Number) right).intValue());
                    case Character:
                        return (char) ((Number) left).intValue() << ((Number) right).intValue();
                    case Short:
                        return (short) ((Number) left).intValue() << ((Number) right).intValue();
                    case Integer:
                        return ((Number) left).intValue() << ((Number) right).intValue();
                    case Long:
                        return ((Number) left).longValue() << ((Number) right).longValue();
                }
            }

            return null;
        }

        private static Object rightShift(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch (type) {
                    case Byte:
                        return (byte) (((Number) left).intValue() >> ((Number) right).intValue());
                    case Character:
                        return (char) ((Number) left).intValue() >> ((Number) right).intValue();
                    case Short:
                        return (short) ((Number) left).intValue() >> ((Number) right).intValue();
                    case Integer:
                        return ((Number) left).intValue() >> ((Number) right).intValue();
                    case Long:
                        return ((Number) left).longValue() >> ((Number) right).longValue();
                }
            }

            return null;
        }

        private static Object unsignedRightShift(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch (type) {
                    case Byte:
                        return (byte) (((Number) left).intValue() >>> ((Number) right).intValue());
                    case Character:
                        return (char) ((Number) left).intValue() >>> ((Number) right).intValue();
                    case Short:
                        return (short) ((Number) left).intValue() >>> ((Number) right).intValue();
                    case Integer:
                        return ((Number) left).intValue() >>> ((Number) right).intValue();
                    case Long:
                        return ((Number) left).longValue() >>> ((Number) right).longValue();
                }
            }

            return null;
        }

        private static Object andAlso(final Object left, final Object right) {
            return Boolean.TRUE.equals(asBoolean(left)) &&
                   Boolean.TRUE.equals(asBoolean(right));
        }

        private static Object orElse(final Object left, final Object right) {
            return Boolean.TRUE.equals(asBoolean(left)) ||
                   Boolean.TRUE.equals(asBoolean(right));
        }

        private static Boolean asBoolean(final Object o) {
            if (o instanceof Boolean) {
                return (Boolean) o;
            }
            else if (o instanceof Number) {
                final Number n = (Number) o;

                if (o instanceof Float) {
                    return n.floatValue() != 0f;
                }
                else if (o instanceof Double) {
                    return n.doubleValue() != 0f;
                }
                else {
                    return n.longValue() != 0L;
                }
            }

            return null;
        }

        private static Boolean lessThan(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch (type) {
                    case Byte:
                    case Character:
                    case Short:
                    case Integer:
                    case Long:
                        return ((Number) left).longValue() < ((Number) right).longValue();
                    case Float:
                        return ((Number) left).floatValue() < ((Number) right).floatValue();
                    case Double:
                        return ((Number) left).doubleValue() < ((Number) right).doubleValue();
                }
            }

            return null;
        }

        private static Boolean lessThanOrEqual(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch (type) {
                    case Byte:
                    case Character:
                    case Short:
                    case Integer:
                    case Long:
                        return ((Number) left).longValue() <= ((Number) right).longValue();
                    case Float:
                        return ((Number) left).floatValue() <= ((Number) right).floatValue();
                    case Double:
                        return ((Number) left).doubleValue() <= ((Number) right).doubleValue();
                }
            }

            return null;
        }

        private static Boolean greaterThan(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch (type) {
                    case Byte:
                    case Character:
                    case Short:
                    case Integer:
                    case Long:
                        return ((Number) left).longValue() > ((Number) right).longValue();
                    case Float:
                        return ((Number) left).floatValue() > ((Number) right).floatValue();
                    case Double:
                        return ((Number) left).doubleValue() > ((Number) right).doubleValue();
                }
            }

            return null;
        }

        private static Boolean greaterThanOrEqual(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch (type) {
                    case Byte:
                    case Character:
                    case Short:
                    case Integer:
                    case Long:
                        return ((Number) left).longValue() >= ((Number) right).longValue();
                    case Float:
                        return ((Number) left).floatValue() >= ((Number) right).floatValue();
                    case Double:
                        return ((Number) left).doubleValue() >= ((Number) right).doubleValue();
                }
            }

            return null;
        }

        private static Boolean equal(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch (type) {
                    case Byte:
                    case Character:
                    case Short:
                    case Integer:
                    case Long:
                        return ((Number) left).longValue() == ((Number) right).longValue();
                    case Float:
                        return ((Number) left).floatValue() == ((Number) right).floatValue();
                    case Double:
                        return ((Number) left).doubleValue() == ((Number) right).doubleValue();
                }
            }

            return null;
        }

        private static Boolean notEqual(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch (type) {
                    case Byte:
                    case Character:
                    case Short:
                    case Integer:
                    case Long:
                        return ((Number) left).longValue() != ((Number) right).longValue();
                    case Float:
                        return ((Number) left).floatValue() != ((Number) right).floatValue();
                    case Double:
                        return ((Number) left).doubleValue() != ((Number) right).doubleValue();
                }
            }

            return null;
        }
    }

    private final static class UnaryOperations {
        static Object doUnary(final UnaryOperatorType operator, final Object operand) {
            switch (operator) {
                case NOT:
                    return isFalse(operand);
                case BITWISE_NOT:
                    return not(operand);
                case MINUS:
                    return minus(operand);
                case PLUS:
                    return plus(operand);
                case INCREMENT:
                    return preIncrement(operand);
                case DECREMENT:
                    return preDecrement(operand);
                case POST_INCREMENT:
                    return postIncrement(operand);
                case POST_DECREMENT:
                    return postDecrement(operand);
            }

            return null;
        }

        private static Object isFalse(final Object operand) {
            if (Boolean.TRUE.equals(operand)) {
                return Boolean.FALSE;
            }

            if (Boolean.FALSE.equals(operand)) {
                return Boolean.TRUE;
            }

            if (operand instanceof Number) {
                final Number n = (Number) operand;

                if (n instanceof Float) {
                    return n.floatValue() != 0f;
                }

                if (n instanceof Double) {
                    return n.doubleValue() != 0d;
                }

                return n.longValue() != 0L;
            }

            return null;
        }

        private static Object not(final Object operand) {
            if (operand instanceof Number) {
                final Number n = (Number) operand;

                if (n instanceof Byte) {
                    return ~n.byteValue();
                }

                if (n instanceof Short) {
                    return ~n.shortValue();
                }

                if (n instanceof Integer) {
                    return ~n.intValue();
                }

                if (n instanceof Long) {
                    return ~n.longValue();
                }
            }
            else if (operand instanceof Character) {
                return ~((Character) operand);
            }

            return null;
        }

        private static Object minus(final Object operand) {
            if (operand instanceof Number) {
                final Number n = (Number) operand;

                if (n instanceof Byte) {
                    return -n.byteValue();
                }

                if (n instanceof Short) {
                    return -n.shortValue();
                }

                if (n instanceof Integer) {
                    return -n.intValue();
                }

                if (n instanceof Long) {
                    return -n.longValue();
                }
            }
            else if (operand instanceof Character) {
                return -((Character) operand);
            }

            return null;
        }

        private static Object plus(final Object operand) {
            if (operand instanceof Number) {
                final Number n = (Number) operand;

                if (n instanceof Byte) {
                    return +n.byteValue();
                }

                if (n instanceof Short) {
                    return +n.shortValue();
                }

                if (n instanceof Integer) {
                    return +n.intValue();
                }

                if (n instanceof Long) {
                    return +n.longValue();
                }
            }
            else if (operand instanceof Character) {
                return +((Character) operand);
            }

            return null;
        }

        private static Object preIncrement(final Object operand) {
            if (operand instanceof Number) {
                final Number n = (Number) operand;

                if (n instanceof Byte) {
                    byte b = n.byteValue();
                    return ++b;
                }

                if (n instanceof Short) {
                    short s = n.shortValue();
                    return ++s;
                }

                if (n instanceof Integer) {
                    int i = n.intValue();
                    return ++i;
                }

                if (n instanceof Long) {
                    long l = n.longValue();
                    return ++l;
                }
            }
            else if (operand instanceof Character) {
                char c = ((Character) operand);
                return ++c;
            }

            return null;
        }

        private static Object preDecrement(final Object operand) {
            if (operand instanceof Number) {
                final Number n = (Number) operand;

                if (n instanceof Byte) {
                    byte b = n.byteValue();
                    return --b;
                }

                if (n instanceof Short) {
                    short s = n.shortValue();
                    return --s;
                }

                if (n instanceof Integer) {
                    int i = n.intValue();
                    return --i;
                }

                if (n instanceof Long) {
                    long l = n.longValue();
                    return --l;
                }
            }
            else if (operand instanceof Character) {
                char c = ((Character) operand);
                return --c;
            }

            return null;
        }

        private static Object postIncrement(final Object operand) {
            if (operand instanceof Number) {
                return operand;
            }
            else if (operand instanceof Character) {
                return operand;
            }

            return null;
        }

        private static Object postDecrement(final Object operand) {
            if (operand instanceof Number) {
                return operand;
            }
            else if (operand instanceof Character) {
                return operand;
            }

            return null;
        }
    }
}
