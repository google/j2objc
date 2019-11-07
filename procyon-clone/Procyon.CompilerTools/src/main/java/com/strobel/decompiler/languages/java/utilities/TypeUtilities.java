/*
 * TypeUtilities.java
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

package com.strobel.decompiler.languages.java.utilities;

import com.strobel.annotations.NotNull;
import com.strobel.annotations.Nullable;
import com.strobel.assembler.metadata.*;
import com.strobel.core.Comparer;
import com.strobel.core.Predicates;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.semantics.ResolveResult;
import com.strobel.functions.Function;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.strobel.core.CollectionUtilities.firstOrDefault;
import static com.strobel.core.CollectionUtilities.indexOf;

public final class TypeUtilities {
    private static final String OBJECT_DESCRIPTOR = "java/lang/Object";
    private static final String STRING_DESCRIPTOR = "java/lang/String";

    private static final Map<JvmType, Integer> TYPE_TO_RANK_MAP;
    private static final Map<Class, TypeDefinition> BOXED_PRIMITIVES_BY_CLASS;

    private static final int BYTE_RANK = 1;
    private static final int SHORT_RANK = 2;
    private static final int CHAR_RANK = 3;
    private static final int INT_RANK = 4;
    private static final int LONG_RANK = 5;
    private static final int FLOAT_RANK = 6;
    private static final int DOUBLE_RANK = 7;
    private static final int BOOL_RANK = 10;
    private static final int STRING_RANK = 100;
    private static final int MAX_NUMERIC_RANK = DOUBLE_RANK;

    static {
        final Map<JvmType, Integer> rankMap = new EnumMap<>(JvmType.class);
        final Map<Class, TypeDefinition> boxedPrimitivesByClass = new HashMap<>();

        rankMap.put(JvmType.Byte, BYTE_RANK);
        rankMap.put(JvmType.Short, SHORT_RANK);
        rankMap.put(JvmType.Character, CHAR_RANK);
        rankMap.put(JvmType.Integer, INT_RANK);
        rankMap.put(JvmType.Long, LONG_RANK);
        rankMap.put(JvmType.Float, FLOAT_RANK);
        rankMap.put(JvmType.Double, DOUBLE_RANK);
        rankMap.put(JvmType.Boolean, BOOL_RANK);

        boxedPrimitivesByClass.put(Byte.class, BuiltinTypes.Byte);
        boxedPrimitivesByClass.put(Short.class, BuiltinTypes.Short);
        boxedPrimitivesByClass.put(Character.class, BuiltinTypes.Character);
        boxedPrimitivesByClass.put(Integer.class, BuiltinTypes.Integer);
        boxedPrimitivesByClass.put(Long.class, BuiltinTypes.Long);
        boxedPrimitivesByClass.put(Float.class, BuiltinTypes.Float);
        boxedPrimitivesByClass.put(Double.class, BuiltinTypes.Double);
        boxedPrimitivesByClass.put(Boolean.class, BuiltinTypes.Boolean);
        boxedPrimitivesByClass.put(Void.class, BuiltinTypes.Void);

        TYPE_TO_RANK_MAP = Collections.unmodifiableMap(rankMap);
        BOXED_PRIMITIVES_BY_CLASS = Collections.unmodifiableMap(boxedPrimitivesByClass);
    }

    private static int getTypeRank(@NotNull final TypeReference type) {
        final TypeReference unboxedType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(type);
        final Integer rank = TYPE_TO_RANK_MAP.get(unboxedType.getSimpleType());

        if (rank != null) {
            return rank;
        }

        if (StringUtilities.equals(type.getInternalName(), STRING_DESCRIPTOR)) {
            return STRING_RANK;
        }

        return Integer.MAX_VALUE;
    }

    public static boolean isPrimitive(@Nullable final TypeReference type) {
        return type != null && type.isPrimitive();
    }

    public static boolean isPrimitiveOrWrapper(@Nullable final TypeReference type) {
        if (type == null) {
            return false;
        }
        return MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(type).isPrimitive();
    }

    public static boolean isBoolean(@Nullable final TypeReference type) {
        if (type == null) {
            return false;
        }
        return MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(type).getSimpleType() == JvmType.Boolean;
    }

    public static boolean isArithmetic(@Nullable final TypeReference type) {
        if (type == null) {
            return false;
        }
        final JvmType jvmType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(type).getSimpleType();
        return jvmType.isNumeric() && jvmType != JvmType.Boolean;
    }

    public static boolean isBinaryOperatorApplicable(
        @NotNull final BinaryOperatorType op,
        @NotNull final AstType lType,
        @NotNull final AstType rType,
        @Nullable final TypeReference expectedResultType,
        final boolean strict) {

        return isBinaryOperatorApplicable(
            op,
            VerifyArgument.notNull(lType, "lType").toTypeReference(),
            VerifyArgument.notNull(rType, "rType").toTypeReference(),
            expectedResultType,
            strict
        );
    }

    public static boolean isBinaryOperatorApplicable(
        @NotNull final BinaryOperatorType op,
        @Nullable final TypeReference lType,
        @Nullable final TypeReference rType,
        @Nullable final TypeReference expectedResultType,
        final boolean strict) {

        if (lType == null || rType == null) {
            return true;
        }

        VerifyArgument.notNull(op, "op");

        final int lRank = getTypeRank(lType);
        final int rRank = getTypeRank(rType);

        final TypeReference lUnboxed = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(lType);
        final TypeReference rUnboxed = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(rType);

        int resultRank = BOOL_RANK;
        boolean isApplicable = false;

        switch (op) {
            case BITWISE_AND:
            case BITWISE_OR:
            case EXCLUSIVE_OR: {
                if (lUnboxed.isPrimitive() && rUnboxed.isPrimitive()) {
                    isApplicable = lRank <= LONG_RANK && rRank <= LONG_RANK ||
                                   isBoolean(lUnboxed) || isBoolean(rUnboxed);

                    resultRank = lRank <= LONG_RANK ? INT_RANK : BOOL_RANK;
                }
                break;
            }

            case LOGICAL_AND:
            case LOGICAL_OR: {
                if (lUnboxed.isPrimitive() && rUnboxed.isPrimitive()) {
                    isApplicable = isBoolean(lType) && isBoolean(rType);
                }
                break;
            }

            case GREATER_THAN:
            case GREATER_THAN_OR_EQUAL:
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL: {
                if (lUnboxed.isPrimitive() && rUnboxed.isPrimitive()) {
                    isApplicable = lRank <= MAX_NUMERIC_RANK && rRank <= MAX_NUMERIC_RANK;
                }
                break;
            }

            case EQUALITY:
            case INEQUALITY: {
                if (lUnboxed.isPrimitive() && rUnboxed.isPrimitive() &&
                    (lType.isPrimitive() || rType.isPrimitive())) {
                    isApplicable = lRank <= MAX_NUMERIC_RANK && rRank <= MAX_NUMERIC_RANK
                                   || lRank == BOOL_RANK && rRank == BOOL_RANK;
                }
                else {
                    if (lType.isPrimitive()) {
                        return MetadataHelper.isConvertible(lType, rType);
                    }
                    if (rType.isPrimitive()) {
                        return MetadataHelper.isConvertible(rType, lType);
                    }
                    isApplicable = MetadataHelper.isConvertible(lType, rType) ||
                                   MetadataHelper.isConvertible(rType, lType);
                }
                break;
            }

            case ADD: {
                if (StringUtilities.equals(lType.getInternalName(), STRING_DESCRIPTOR)) {
                    isApplicable = !rType.isVoid();
                    resultRank = STRING_RANK;
                    break;
                }
                else if (StringUtilities.equals(rType.getInternalName(), STRING_DESCRIPTOR)) {
                    isApplicable = !lType.isVoid();
                    resultRank = STRING_RANK;
                    break;
                }

                if (lUnboxed.isPrimitive() && rUnboxed.isPrimitive()) {
                    resultRank = Math.max(lRank, rRank);
                    isApplicable = lRank <= MAX_NUMERIC_RANK && rRank <= MAX_NUMERIC_RANK;
                }

                break;
            }

            case SUBTRACT:
            case MULTIPLY:
            case DIVIDE:
            case MODULUS: {
                if (lUnboxed.isPrimitive() && rUnboxed.isPrimitive()) {
                    resultRank = Math.max(lRank, rRank);
                    isApplicable = lRank <= MAX_NUMERIC_RANK && rRank <= MAX_NUMERIC_RANK;
                }
                break;
            }

            case SHIFT_LEFT:
            case SHIFT_RIGHT:
            case UNSIGNED_SHIFT_RIGHT: {
                if (lUnboxed.isPrimitive() && rUnboxed.isPrimitive()) {
                    isApplicable = lRank <= LONG_RANK && rRank <= LONG_RANK;
                    resultRank = INT_RANK;
                }
                break;
            }
        }

        if (isApplicable && strict) {
            if (resultRank > MAX_NUMERIC_RANK) {
                isApplicable = lRank == resultRank ||
                               StringUtilities.equals(lType.getInternalName(), OBJECT_DESCRIPTOR);
            }
            else {
                isApplicable = lRank <= MAX_NUMERIC_RANK;
            }
        }

        if(isApplicable && expectedResultType != null) {
            final int expectedResultRank = getTypeRank(
                MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(expectedResultType)
            );
            isApplicable = resultRank == expectedResultRank;
        }

        return isApplicable;
    }

    @Nullable
    public static AstNode skipParenthesesUp(final AstNode e) {
        AstNode result = e;

        while (result instanceof ParenthesizedExpression) {
            result = result.getParent();
        }

        return result;
    }

    @Nullable
    public static AstNode skipParenthesesDown(final AstNode e) {
        AstNode result = e;

        while (result instanceof ParenthesizedExpression) {
            result = ((ParenthesizedExpression) result).getExpression();
        }

        return result;
    }

    @Nullable
    public static Expression skipParenthesesDown(final Expression e) {
        Expression result = e;

        while (result instanceof ParenthesizedExpression) {
            result = ((ParenthesizedExpression) result).getExpression();
        }

        return result;
    }

    private static boolean checkSameExpression(final Expression template, final Expression expression) {
        return Comparer.equals(template, skipParenthesesDown(expression));
    }

    private static TypeReference getType(@NotNull final Function<AstNode, ResolveResult> resolver, @NotNull final AstNode node) {
        final ResolveResult result = resolver.apply(node);
        return result != null ? result.getType() : null;
    }

    @Nullable
    public static TypeReference getExpectedTypeByParent(final Function<AstNode, ResolveResult> resolver, final Expression expression) {
        VerifyArgument.notNull(resolver, "resolver");
        VerifyArgument.notNull(expression, "expression");

        final AstNode parent = skipParenthesesUp(expression.getParent());

        if (expression.getRole() == Roles.CONDITION) {
            return CommonTypeReferences.Boolean;
        }

        if (parent instanceof VariableInitializer) {
            if (checkSameExpression(expression, ((VariableInitializer) parent).getInitializer())) {
                if (parent.getParent() instanceof VariableDeclarationStatement) {
                    return getType(resolver, parent.getParent());
                }
            }
        }
        else if (parent instanceof ArrayCreationExpression &&
                 expression.getRole() == ArrayCreationExpression.INITIALIZER_ROLE) {

            return getType(resolver, parent);
        }
        else if (expression instanceof ArrayInitializerExpression &&
                 parent instanceof ArrayInitializerExpression) {

            final TypeReference expectedArrayType = getExpectedTypeByParent(resolver, (Expression) parent);

            if (expectedArrayType != null && expectedArrayType.isArray()) {
                return expectedArrayType.getElementType();
            }
        }
        else if (parent instanceof AssignmentExpression) {
            if (checkSameExpression(expression, ((AssignmentExpression) parent).getRight())) {
                return getType(resolver, ((AssignmentExpression) parent).getLeft());
            }
        }
        else if (parent instanceof ReturnStatement) {
            final LambdaExpression lambdaExpression = firstOrDefault(parent.getAncestors(LambdaExpression.class));

            if (lambdaExpression != null) {
                final DynamicCallSite callSite = lambdaExpression.getUserData(Keys.DYNAMIC_CALL_SITE);

                if (callSite == null) {
                    return null;
                }

                final MethodReference method = (MethodReference) callSite.getBootstrapArguments().get(0);

                return method.getDeclaringType();
            }
            else {
                final MethodDeclaration method = firstOrDefault(parent.getAncestors(MethodDeclaration.class));

                if (method != null) {
                    return getType(resolver, method.getReturnType());
                }
            }
        }
        else if (parent instanceof ConditionalExpression) {
            if (checkSameExpression(expression, ((ConditionalExpression) parent).getTrueExpression())) {
                return getType(resolver, ((ConditionalExpression) parent).getFalseExpression());
            }
            else if (checkSameExpression(expression, ((ConditionalExpression) parent).getFalseExpression())) {
                return getType(resolver, ((ConditionalExpression) parent).getTrueExpression());
            }
        }
        else if (expression.getRole() == Roles.ARGUMENT && parent instanceof InvocationExpression) {
            final MemberReference reference = parent.getUserData(Keys.MEMBER_REFERENCE);
            final MethodReference method = reference instanceof MethodReference ? (MethodReference) reference : null;
            final int position = indexOf(((InvocationExpression) parent).getArguments(), expression);

            if (method != null && position >= 0 && method.getParameters().size() > 0) {
                final MethodDefinition resolved = method.resolve();

                if (resolved != null &&
                    resolved.getParameters().size() == method.getParameters().size() &&
                    resolved.isVarArgs() &&
                    position >= resolved.getParameters().size() - 1) {

                    return method.getParameters().get(method.getParameters().size() - 1).getParameterType();
                }

                return method.getParameters().get(position).getParameterType();
            }
        }

        return null;
    }

    public static IMethodSignature getLambdaSignature(final MethodGroupExpression node) {
        return getLambdaSignatureCore(node);
    }

    public static IMethodSignature getLambdaSignature(final LambdaExpression node) {
        return getLambdaSignatureCore(node);
    }

    public static boolean isValidPrimitiveLiteralAssignment(final TypeReference targetType, final Object value) {
        VerifyArgument.notNull(targetType, "targetType");

        if (targetType.getSimpleType() == JvmType.Boolean) {
            return value instanceof Boolean;
        }

        if (!(targetType.isPrimitive() && (value instanceof Number || value instanceof Character))) {
            return false;
        }

        final Number n = value instanceof Character ? (int) (char) value : (Number) value;

        if (n instanceof Float || n instanceof Double) {
            if (targetType.getSimpleType() == JvmType.Float) {
                return n.doubleValue() == (double)(float)n.doubleValue();
            }
            return targetType.getSimpleType() == JvmType.Double;
        }

        final TypeDefinition valueType = BOXED_PRIMITIVES_BY_CLASS.get(n.getClass());
        final JvmType valueJvmType = valueType != null ? valueType.getSimpleType() : JvmType.Void;

        if (n instanceof Long) {
            switch (targetType.getSimpleType()) {
                case Long:
                case Float:
                case Double:
                    return true;
            }
        }

        switch (targetType.getSimpleType()) {
            case Byte:
                return valueJvmType.isSubWordOrInt32() &&
                       valueJvmType != JvmType.Boolean &&
                       n.intValue() >= Byte.MIN_VALUE &&
                       n.intValue() <= Byte.MAX_VALUE;

            case Character:
                return valueJvmType.isSubWordOrInt32() &&
                       valueJvmType != JvmType.Boolean &&
                       n.intValue() >= Character.MIN_VALUE &&
                       n.intValue() <= Character.MAX_VALUE;

            case Short:
                return valueJvmType.isSubWordOrInt32() &&
                       valueJvmType != JvmType.Boolean &&
                       n.intValue() >= Short.MIN_VALUE &&
                       n.intValue() <= Short.MAX_VALUE;

            case Integer:
                return valueJvmType.isSubWordOrInt32() &&
                       valueJvmType != JvmType.Boolean &&
                       n.longValue() >= Integer.MIN_VALUE &&
                       n.longValue() <= Integer.MAX_VALUE;

            case Long:
                return valueJvmType.isIntegral() &&
                       valueJvmType != JvmType.Boolean;

            case Float:
            case Double:
                return true;

            default:
                return false;
        }
    }

    private static IMethodSignature getLambdaSignatureCore(final Expression node) {
        VerifyArgument.notNull(node, "node");

        final TypeReference lambdaType = node.getUserData(Keys.TYPE_REFERENCE);
        final DynamicCallSite callSite = node.getUserData(Keys.DYNAMIC_CALL_SITE);

        if (lambdaType == null) {
            if (callSite == null) {
                return null;
            }

            return (IMethodSignature) callSite.getBootstrapArguments().get(2);
        }

        final TypeDefinition resolvedType = lambdaType.resolve();

        if (resolvedType == null) {
            if (callSite == null) {
                return null;
            }

            return (IMethodSignature) callSite.getBootstrapArguments().get(2);
        }

        MethodReference functionMethod = null;

        final List<MethodReference> methods = MetadataHelper.findMethods(
            resolvedType,
            callSite != null ? MetadataFilters.matchName(callSite.getMethodName())
                             : Predicates.<MemberReference>alwaysTrue()
        );

        for (final MethodReference m : methods) {
            final MethodDefinition r = m.resolve();

            if (r != null && r.isAbstract() && !r.isStatic() && !r.isDefault()) {
                functionMethod = r;
                break;
            }
        }

        if (functionMethod != null) {
            final TypeReference asMemberOf = MetadataHelper.asSuper(functionMethod.getDeclaringType(), lambdaType);
            final TypeReference effectiveType = asMemberOf != null ? asMemberOf : lambdaType;

            if (MetadataHelper.isRawType(effectiveType)) {
                return MetadataHelper.erase(functionMethod);
            }

            functionMethod = MetadataHelper.asMemberOf(functionMethod, effectiveType);
        }

        return functionMethod;
    }
}