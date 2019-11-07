/*
 * TypeUtils.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.util;

import com.strobel.annotations.NotNull;
import com.strobel.core.VerifyArgument;
import com.strobel.reflection.BindingFlags;
import com.strobel.reflection.MethodInfo;
import com.strobel.reflection.PrimitiveTypes;
import com.strobel.reflection.Type;
import com.strobel.reflection.TypeList;
import com.strobel.reflection.Types;

import javax.lang.model.type.TypeKind;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Strobel
 */
@SuppressWarnings("Duplicates")
public final class TypeUtils {
    private TypeUtils() {
        throw ContractUtils.unreachable();
    }

    public static boolean isAutoUnboxed(final Type<?> type) {
        return type == Types.Integer ||
               type == Types.Long ||
               type == Types.Double ||
               type == Types.Float ||
               type == Types.Short ||
               type == Types.Byte ||
               type == Types.Boolean ||
               type == Types.Character;
    }

    public static Type<?> getUnderlyingPrimitive(final Type<?> type) {
        if (type == Types.Integer) {
            return PrimitiveTypes.Integer;
        }
        if (type == Types.Long) {
            return PrimitiveTypes.Long;
        }
        if (type == Types.Double) {
            return PrimitiveTypes.Double;
        }
        if (type == Types.Float) {
            return PrimitiveTypes.Float;
        }
        if (type == Types.Short) {
            return PrimitiveTypes.Short;
        }
        if (type == Types.Byte) {
            return PrimitiveTypes.Byte;
        }
        if (type == Types.Boolean) {
            return PrimitiveTypes.Boolean;
        }
        if (type == Types.Character) {
            return PrimitiveTypes.Character;
        }
        return null;
    }

    public static Type<?> getBoxedTypeOrSelf(final Type<?> type) {
        final Type<?> boxedType = getBoxedType(type);
        return boxedType != null ? boxedType : type;
    }

    public static Type<?> getUnderlyingPrimitiveOrSelf(final Type<?> type) {
        final Type<?> underlyingPrimitive = getUnderlyingPrimitive(type);
        return  underlyingPrimitive != null ? underlyingPrimitive : type;
    }

    public static Type<?> getBoxedType(final Type<?> type) {
        if (isAutoUnboxed(type)) {
            return type;
        }
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == PrimitiveTypes.Integer) {
            return Types.Integer;
        }
        if (type == PrimitiveTypes.Long) {
            return Types.Long;
        }
        if (type == PrimitiveTypes.Double) {
            return Types.Double;
        }
        if (type == PrimitiveTypes.Float) {
            return Types.Float;
        }
        if (type == PrimitiveTypes.Short) {
            return Types.Short;
        }
        if (type == PrimitiveTypes.Byte) {
            return Types.Byte;
        }
        if (type == PrimitiveTypes.Boolean) {
            return Types.Boolean;
        }
        if (type == PrimitiveTypes.Character) {
            return Types.Character;
        }
        return null;
    }

    public static boolean isArithmetic(final Type<?> type) {
        return isNumeric(type) ||
               type == PrimitiveTypes.Character ||
               type == Types.Character;
    }

    public static boolean isNumeric(final Type<?> type) {
        final Type<?> underlyingPrimitive = getUnderlyingPrimitive(type);
        final Type<?> actualType = underlyingPrimitive != null ? underlyingPrimitive : type;

        return actualType == PrimitiveTypes.Integer ||
               actualType == PrimitiveTypes.Long ||
               actualType == PrimitiveTypes.Double ||
               actualType == PrimitiveTypes.Float ||
               actualType == PrimitiveTypes.Short ||
               actualType == PrimitiveTypes.Byte ||
               actualType == PrimitiveTypes.Character;
    }

    public static boolean isIntegralOrBoolean(final Type<?> type) {
        final Type<?> underlyingPrimitive = getUnderlyingPrimitive(type);
        final Type<?> actualType = underlyingPrimitive != null ? underlyingPrimitive : type;

        return actualType == PrimitiveTypes.Integer ||
               actualType == PrimitiveTypes.Long ||
               actualType == PrimitiveTypes.Short ||
               actualType == PrimitiveTypes.Byte ||
               actualType == PrimitiveTypes.Character ||
               actualType == PrimitiveTypes.Boolean;
    }

    public static boolean isIntegral(final Type<?> type) {
        final Type<?> underlyingPrimitive = getUnderlyingPrimitive(type);
        final Type<?> actualType = underlyingPrimitive != null ? underlyingPrimitive : type;

        return actualType == PrimitiveTypes.Integer ||
               actualType == PrimitiveTypes.Long ||
               actualType == PrimitiveTypes.Short ||
               actualType == PrimitiveTypes.Byte ||
               actualType == PrimitiveTypes.Character;
    }

    public static boolean isBoolean(final Type<?> type) {
        return type == PrimitiveTypes.Boolean || type == Types.Boolean;
    }

    public static boolean areEquivalent(final Type<?> class1, final Type<?> class2) {
        return class1 == null ? class2 == null
                              : class1.isEquivalentTo(class2);
    }

    public static boolean areEquivalentWithOrdering(final TypeList types1, final TypeList types2) {
        if (types1 == types2) {
            return true;
        }

        if (types1 == null || types2 == null ||
            types1.size() != types2.size()) {

            return false;
        }

        for (int i = 0, n = types1.size(); i < n; i++) {
            if (!areEquivalent(types1.get(i), types2.get(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean areEquivalent(final TypeList types1, final TypeList types2) {
        if (areEquivalentWithOrdering(types1, types2)) {
            return true;
        }

        if (types1 == types2) {
            return true;
        }

        if (types1 == null || types2 == null ||
            types1.size() != types2.size()) {

            return false;
        }

        final Set<Type<?>> set1 = new HashSet<>(types1);
        final Set<Type<?>> set2 = new HashSet<>(types2);

        return set1.size() == set2.size() &&
               set1.containsAll(set2);
    }

    public static boolean hasIdentityPrimitiveOrBoxingConversion(final Type<?> source, final Type<?> destination) {
        assert source != null && destination != null;

        if (source == Types.Object || destination == Types.Object) {
            return true;
        }

        final Type<?> unboxedSource = getUnderlyingPrimitiveOrSelf(source);
        final Type<?> unboxedDestination = getUnderlyingPrimitiveOrSelf(destination);

        // Identity conversion
        return unboxedSource == unboxedDestination ||
               areEquivalent(unboxedSource, unboxedDestination);
    }

    public static boolean hasReferenceConversion(final Type<?> source, final Type<?> destination) {
        assert source != null && destination != null;

        // void -> void conversion is handled elsewhere (it's an identity conversion)
        // All other void conversions are disallowed.
        if (source == PrimitiveTypes.Void || destination == PrimitiveTypes.Void) {
            return false;
        }

        if (areEquivalent(source, destination)) {
            return true;
        }

        if (source.isPrimitive() || destination.isPrimitive()) {
            return false;
        }

        // Object conversion
        if (source == Types.Object || destination == Types.Object) {
            return true;
        }

        // Interface conversion
        if (source.isInterface() || destination.isInterface()) {
            return true;
        }

        // Up conversion
        if (destination.isAssignableFrom(source)) {
            return true;
        }

        // Down conversion
        return source.isAssignableFrom(destination);
    }

    public static MethodInfo getCoercionMethod(final Type<?> source, final Type<?> destination) {
        // NOTE: If destination type is an autoboxing type, we will need an implicit box later.
        final Type<?> unboxedDestinationType = isAutoUnboxed(destination)
                                               ? getUnderlyingPrimitive(destination)
                                               : destination;

        if (destination == Types.String) {
            return Types.String.getMethod("valueOf", source);
        }

/*
        if (!destination.isPrimitive()) {
            return null;
        }
*/

        final MethodInfo method;

        if (destination == PrimitiveTypes.Integer) {
            method = source.getMethod("intValue", BindingFlags.PublicInstance);
        }
        else if (destination == PrimitiveTypes.Long) {
            method = source.getMethod("longValue", BindingFlags.PublicInstance);
        }
        else if (destination == PrimitiveTypes.Double) {
            method = source.getMethod("doubleValue", BindingFlags.PublicInstance);
        }
        else if (destination == PrimitiveTypes.Float) {
            method = source.getMethod("floatValue", BindingFlags.PublicInstance);
        }
        else if (destination == PrimitiveTypes.Short) {
            method = source.getMethod("shortValue", BindingFlags.PublicInstance);
        }
        else if (destination == PrimitiveTypes.Byte) {
            method = source.getMethod("byteValue", BindingFlags.PublicInstance);
        }
        else if (destination == PrimitiveTypes.Boolean) {
            method = source.getMethod("booleanValue", BindingFlags.PublicInstance);
        }
        else if (destination == PrimitiveTypes.Character) {
            method = source.getMethod("charValue", BindingFlags.PublicInstance);
        }
        else {
            method = destination.getMethod("valueOf", BindingFlags.PublicStatic, source);
        }

        if (method == null) {
            return null;
        }

        if (method.getReturnType() == unboxedDestinationType) {
            return method;
        }

        return null;
    }

    public static MethodInfo getBoxMethod(final Type<?> type) {
        final Type<?> boxedType = getBoxedTypeOrSelf(type);
        final Type<?> primitiveType = TypeUtils.getUnderlyingPrimitive(boxedType);

        if (primitiveType == null) {
            return null;
        }

        final MethodInfo boxMethod = boxedType.getMethod(
            "valueOf",
            BindingFlags.PublicStatic,
            primitiveType
        );

        if (boxMethod == null || !areEquivalent(boxMethod.getReturnType(), boxedType)) {
            return null;
        }

        return boxMethod;
    }

    public static MethodInfo getUnboxMethod(final Type<?> type) {
        final Type<?> boxedType = getBoxedTypeOrSelf(type);
        final Type<?> primitiveType= TypeUtils.getUnderlyingPrimitive(boxedType);

        if (primitiveType == null) {
            return null;
        }

        final Set<BindingFlags> bindingFlags = BindingFlags.PublicInstance;

        final MethodInfo unboxMethod;

        switch (primitiveType.getKind()) {
            case BOOLEAN:
                unboxMethod = boxedType.getMethod("booleanValue", bindingFlags);
                break;

            case BYTE:
                unboxMethod = boxedType.getMethod("byteValue", bindingFlags);
                break;

            case SHORT:
                unboxMethod = boxedType.getMethod("shortValue", bindingFlags);
                break;

            case INT:
                unboxMethod = boxedType.getMethod("intValue", bindingFlags);
                break;

            case LONG:
                unboxMethod = boxedType.getMethod("longValue", bindingFlags);
                break;

            case CHAR:
                unboxMethod = boxedType.getMethod("charValue", bindingFlags);
                break;

            case FLOAT:
                unboxMethod = boxedType.getMethod("floatValue", bindingFlags);
                break;

            case DOUBLE:
                unboxMethod = boxedType.getMethod("doubleValue", bindingFlags);
                break;

            default:
                unboxMethod = null;
                break;
        }

        if (unboxMethod == null || !areEquivalent(unboxMethod.getReturnType(), primitiveType)) {
            return null;
        }

        return unboxMethod;
    }

    public static MethodInfo getUnboxMethod(final Type<?> boxedType, final Type<?> unboxedType) {
        final Set<BindingFlags> bindingFlags = BindingFlags.PublicInstance;
        final MethodInfo unboxMethod;

        switch (unboxedType.getKind()) {
            case BOOLEAN:
                unboxMethod = boxedType.getMethod("booleanValue", bindingFlags);
                break;

            case BYTE:
                unboxMethod = boxedType.getMethod("byteValue", bindingFlags);
                break;

            case SHORT:
                unboxMethod = boxedType.getMethod("shortValue", bindingFlags);
                break;

            case INT:
                unboxMethod = boxedType.getMethod("intValue", bindingFlags);
                break;

            case LONG:
                unboxMethod = boxedType.getMethod("longValue", bindingFlags);
                break;

            case CHAR:
                unboxMethod = boxedType.getMethod("charValue", bindingFlags);
                break;

            case FLOAT:
                unboxMethod = boxedType.getMethod("floatValue", bindingFlags);
                break;

            case DOUBLE:
                unboxMethod = boxedType.getMethod("doubleValue", bindingFlags);
                break;

            default:
                unboxMethod = null;
                break;
        }

        if (unboxMethod == null || !areEquivalent(unboxMethod.getReturnType(), unboxedType)) {
            return null;
        }

        return unboxMethod;
    }

    public static boolean areReferenceAssignable(final Type<?> destination, final Type<?> source) {
        if (destination == Types.Object) {
            return true;
        }
        // WARNING: This actually checks "is this identity assignable and/or reference assignable?"
        return hasIdentityPrimitiveOrBoxingConversion(source, destination) ||
               !destination.isPrimitive() && !source.isPrimitive() && destination.isAssignableFrom(source);
    }

    public static boolean hasReferenceEquality(final Type<?> left, final Type<?> right) {
        if (left.isPrimitive() || right.isPrimitive()) {
            return false;
        }

        // If we have an interface and a reference type then we can do
        // reference equality.

        // If we have two reference types and one is assignable to the
        // other then we can do reference equality.

        return left.isInterface() || right.isInterface() ||
               areReferenceAssignable(left, right) ||
               areReferenceAssignable(right, left);
    }

    public static boolean hasBuiltInEqualityOperator(final Type<?> left, final Type<?> right) {
        if (left.isPrimitive() || right.isPrimitive()) {
            if (left == PrimitiveTypes.Boolean) {
                return hasIdentityPrimitiveOrBoxingConversion(left, right);
            }

            if (right == PrimitiveTypes.Boolean) {
                return hasIdentityPrimitiveOrBoxingConversion(right, left);
            }

            return isArithmetic(left) && isArithmetic(right);
        }

        return left.isEnum() && hasIdentityPrimitiveOrBoxingConversion(left, right) ||
               right.isEnum() && hasIdentityPrimitiveOrBoxingConversion(right, left);
    }

    public static boolean isValidInvocationTargetType(final MethodInfo method, final Type<?> targetType) {
        final Type declaringType = method.getDeclaringType();

        return areReferenceAssignable(declaringType, targetType) ||
               targetType.isSubTypeOf(declaringType);
    }

    public static boolean isSameOrSubType(final Type<?> type, final Type<?> subType) {
        return areEquivalent(type, subType) ||
               subType.isSubTypeOf(type);
    }

    public static boolean isImplicitNumericConversion(final Type<?> sourceType, final Type<?> targetType) {
        final TypeKind sourceKind = TypeUtils.getUnderlyingPrimitiveOrSelf(sourceType).getKind();
        final TypeKind targetKind = TypeUtils.getUnderlyingPrimitiveOrSelf(targetType).getKind();

        switch (sourceKind) {
            case BYTE:
                switch (targetKind) {
                    case BYTE:
                    case SHORT:
                    case INT:
                    case LONG:
                    case CHAR:
                    case FLOAT:
                    case DOUBLE:
                        return true;
                }
                return false;

            case SHORT:
                switch (targetKind) {
                    case SHORT:
                    case INT:
                    case LONG:
                    case CHAR:
                    case FLOAT:
                    case DOUBLE:
                        return true;
                }
                return false;

            case INT:
                switch (targetKind) {
                    case INT:
                    case LONG:
                    case CHAR:
                    case FLOAT:
                    case DOUBLE:
                        return true;
                }
                return false;

            case CHAR:
                switch (targetKind) {
                    case SHORT:
                    case INT:
                    case LONG:
                    case CHAR:
                    case FLOAT:
                    case DOUBLE:
                        return true;
                }
                return false;

            case LONG:
                switch (targetKind) {
                    case LONG:
                    case FLOAT:
                    case DOUBLE:
                        return true;
                }
                return false;

            case FLOAT:
                switch (targetKind) {
                    case FLOAT:
                    case DOUBLE:
                        return true;
                }
                return false;

            case DOUBLE:
                return targetKind == TypeKind.DOUBLE;
        }

        return false;
    }

    public static String getInternalName(@NotNull final Class<?> clazz) {
        VerifyArgument.notNull(clazz, "clazz");

        final StringBuilder sb = new StringBuilder(clazz.getName());

        for (int i = 0, n = sb.length(); i < n; i++) {
            if (sb.charAt(i) == '.') {
                sb.setCharAt(i, '/');
            }
        }

        return sb.toString();
    }

    public static boolean isSingleWord(final Type<?> type) {
        return isSingleWord(type.getKind());
    }

    public static boolean isSingleWord(final TypeKind kind) {
        switch (kind) {
            case LONG:
            case DOUBLE:
            case VOID:
                return false;
            default:
                return true;
        }
    }

    public static boolean isDoubleWord(final Type<?> type) {
        return isDoubleWord(type.getKind());
    }

    public static boolean isDoubleWord(final TypeKind kind) {
        switch (kind) {
            case LONG:
            case DOUBLE:
                return true;
            default:
                return false;
        }
    }

    public static boolean isNumeric(final TypeKind kind) {
        switch (kind) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                return true;
            default:
                return false;
        }
    }

    public static boolean isIntegral(final TypeKind kind) {
        switch (kind) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
            case LONG:
                return true;
            default:
            case FLOAT:
            case DOUBLE:
                return false;
        }
    }

    public static boolean isSubWordOrInt32(final Type<?> type) {
        return isSubWordOrInt32(type.getKind());
    }

    public static boolean isSubWordOrInt32(final TypeKind kind) {
        switch (kind) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
                return true;
            default:
                return false;
        }
    }

    public static int bitWidth(final Type<?> type) {
        return bitWidth(type.getKind());
    }

    public static int bitWidth(final TypeKind kind) {
        switch (kind) {
            case BOOLEAN:
                return 1;
            case BYTE:
                return 8;
            case CHAR:
            case SHORT:
                return 16;
            case INT:
                return 32;
            case LONG:
                return 64;
            case FLOAT:
                return 32;
            case DOUBLE:
                return 64;
            default:
                return 0;
        }
    }
}
