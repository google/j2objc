/*
 * MetadataHelper.java
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

package com.strobel.assembler.metadata;

import com.strobel.annotations.NotNull;
import com.strobel.annotations.Nullable;
import com.strobel.collections.ListBuffer;
import com.strobel.core.ArrayUtilities;
import com.strobel.core.Pair;
import com.strobel.core.Predicate;
import com.strobel.core.Predicates;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;

import java.util.*;

import static com.strobel.core.CollectionUtilities.*;

public final class MetadataHelper {
    public static boolean areGenericsSupported(final TypeDefinition t) {
        return t != null && t.getCompilerMajorVersion() >= 49;
    }

    public static int getArrayRank(final TypeReference t) {
        if (t == null) {
            return 0;
        }

        int rank = 0;
        TypeReference current = t;

        while (current.isArray()) {
            ++rank;
            current = current.getElementType();
        }

        return rank;
    }

    @Nullable
    public static TypeDefinition getOutermostEnclosingType(final TypeReference innerType) {
        TypeReference t = innerType;

        while (t != null) {
            final TypeDefinition r = t.resolve();

            if (r == null || !r.isNested()) {
                return r;
            }

            final MethodReference m = r.getDeclaringMethod();

            t = m != null ? m.getDeclaringType()
                          : t.getDeclaringType();
        }

        return null;
    }

    public static boolean isEnclosedBy(final TypeReference innerType, final TypeReference outerType) {
        if (innerType == null || outerType == null || BuiltinTypes.Object.isEquivalentTo(outerType)) {
            return false;
        }

        final TypeDefinition innerResolved = innerType.resolve();
        final TypeDefinition outerResolved = outerType.resolve();

        final TypeReference inner = innerResolved != null ? innerResolved : innerType;
        final TypeReference outer = outerResolved != null ? outerResolved : outerType;

        for (TypeReference current = inner.getDeclaringType();
             current != null;
             current = current.getDeclaringType()) {

            if (isSameType(current, outer, false)) {
                return true;
            }
        }

        final TypeReference outerBaseType = outerResolved != null ? outerResolved.getBaseType() : null;

        return outerBaseType != null && isEnclosedBy(inner, outerBaseType) ||
               innerResolved != null && isEnclosedBy(innerResolved.getBaseType(), outer);
    }

    public static boolean canReferenceTypeVariablesOf(final TypeReference declaringType, final TypeReference referenceSite) {
        if (declaringType == null || referenceSite == null) {
            return false;
        }

        if (declaringType == referenceSite) {
            return declaringType.isGenericType();
        }

        for (TypeReference current = referenceSite.getDeclaringType();
             current != null; ) {

            if (isSameType(current, declaringType)) {
                return true;
            }

            final TypeDefinition resolvedType = current.resolve();

            if (resolvedType != null) {
                final MethodReference declaringMethod = resolvedType.getDeclaringMethod();

                if (declaringMethod != null) {
                    current = declaringMethod.getDeclaringType();
                    continue;
                }
            }

            current = current.getDeclaringType();
        }

        return false;
    }

    public static TypeReference findCommonSuperType(final TypeReference type1, final TypeReference type2) {
        VerifyArgument.notNull(type1, "type1");
        VerifyArgument.notNull(type2, "type2");

        if (type1 == type2) {
            return type1;
        }

        if (type1.isPrimitive()) {
            if (type2.isPrimitive()) {
                if (isAssignableFrom(type1, type2)) {
                    return type1;
                }
                if (isAssignableFrom(type2, type1)) {
                    return type2;
                }
                return doNumericPromotion(type1, type2);
            }
            return findCommonSuperType(getBoxedTypeOrSelf(type1), type2);
        }
        else if (type2.isPrimitive()) {
            return findCommonSuperType(type1, getBoxedTypeOrSelf(type2));
        }

        int rank1 = 0;
        int rank2 = 0;

        TypeReference elementType1 = type1;
        TypeReference elementType2 = type2;

        while (elementType1.isArray()) {
            elementType1 = elementType1.getElementType();
            ++rank1;
        }

        while (elementType2.isArray()) {
            elementType2 = elementType2.getElementType();
            ++rank2;
        }

        if (rank1 != rank2) {
            return BuiltinTypes.Object;
        }

        if (rank1 != 0 && (elementType1.isPrimitive() || elementType2.isPrimitive())) {
            if (elementType1.isPrimitive() && elementType2.isPrimitive()) {
                TypeReference promotedType = doNumericPromotion(elementType1, elementType2);

                while (rank1-- > 0) {
                    promotedType = promotedType.makeArrayType();
                }

                return promotedType;
            }
            return BuiltinTypes.Object;
        }

        while (!elementType1.isUnbounded()) {
            elementType1 = elementType1.hasSuperBound() ? elementType1.getSuperBound()
                                                        : elementType1.getExtendsBound();
        }

        while (!elementType2.isUnbounded()) {
            elementType2 = elementType2.hasSuperBound() ? elementType2.getSuperBound()
                                                        : elementType2.getExtendsBound();
        }

        TypeReference result = findCommonSuperTypeCore(elementType1, elementType2);

        while (rank1-- > 0) {
            result = result.makeArrayType();
        }

        return result;
    }

    private static TypeReference doNumericPromotion(final TypeReference leftType, final TypeReference rightType) {
        final JvmType left = leftType.getSimpleType();
        final JvmType right = rightType.getSimpleType();

        if (left == right) {
            return leftType;
        }

        if (left == JvmType.Double || right == JvmType.Double) {
            return BuiltinTypes.Double;
        }

        if (left == JvmType.Float || right == JvmType.Float) {
            return BuiltinTypes.Float;
        }

        if (left == JvmType.Long || right == JvmType.Long) {
            return BuiltinTypes.Long;
        }

        if (left.isNumeric() && left != JvmType.Boolean || right.isNumeric() && right != JvmType.Boolean) {
            return BuiltinTypes.Integer;
        }

        return leftType;
    }

    private static TypeReference findCommonSuperTypeCore(final TypeReference type1, final TypeReference type2) {
        if (isAssignableFrom(type1, type2)) {
            if (type2.isGenericType() && !type1.isGenericType()) {
                final TypeDefinition resolved1 = type1.resolve();

                if (resolved1 != null) {
                    return substituteGenericArguments(resolved1, type2);
                }
            }
            return substituteGenericArguments(type1, type2);
        }

        if (isAssignableFrom(type2, type1)) {
            if (type1.isGenericType() && !type2.isGenericType()) {
                final TypeDefinition resolved2 = type2.resolve();

                if (resolved2 != null) {
                    return substituteGenericArguments(resolved2, type1);
                }
            }
            return substituteGenericArguments(type2, type1);
        }

        final TypeDefinition c = type1.resolve();
        final TypeDefinition d = type2.resolve();

        if (c == null || d == null || c.isInterface() || d.isInterface()) {
            return BuiltinTypes.Object;
        }

        TypeReference current = c;

        while (current != null) {
            for (final TypeReference interfaceType : getInterfaces(current)) {
                if (isAssignableFrom(interfaceType, d)) {
                    return interfaceType;
                }
            }

            current = getBaseType(current);

            if (current != null) {
                if (isAssignableFrom(current, d)) {
                    return current;
                }
            }
        }

        return BuiltinTypes.Object;

//        do {
//            final TypeReference baseType = current.getBaseType();
//
//            if (baseType == null || (current = baseType.resolve()) == null) {
//                return BuiltinTypes.Object;
//            }
//        }
//        while (!isAssignableFrom(current, d));
//
//        return current;
    }

    public static ConversionType getConversionType(final TypeReference target, final TypeReference source) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.notNull(target, "target");

        final TypeReference underlyingTarget = getUnderlyingPrimitiveTypeOrSelf(target);
        final TypeReference underlyingSource = getUnderlyingPrimitiveTypeOrSelf(source);

        if (underlyingTarget.getSimpleType().isNumeric() && underlyingSource.getSimpleType().isNumeric()) {
            return getNumericConversionType(target, source);
        }

        if (StringUtilities.equals(target.getInternalName(), "java/lang/Object")) {
            return ConversionType.IMPLICIT;
        }

        if (isSameType(target, source, true)) {
            return ConversionType.IDENTITY;
        }

        if (isAssignableFrom(target, source, false)) {
            return ConversionType.IMPLICIT;
        }

        int targetRank = 0;
        int sourceRank = 0;

        TypeReference targetElementType = target;
        TypeReference sourceElementType = source;

        while (targetElementType.isArray()) {
            ++targetRank;
            targetElementType = targetElementType.getElementType();
        }

        while (sourceElementType.isArray()) {
            ++sourceRank;
            sourceElementType = sourceElementType.getElementType();
        }

        if (sourceRank != targetRank) {
            if (isSameType(sourceElementType, BuiltinTypes.Object)) {
                return ConversionType.EXPLICIT;
            }
            return ConversionType.NONE;
        }

        return ConversionType.EXPLICIT;
    }

    @NotNull
    public static ConversionType getNumericConversionType(final TypeReference target, final TypeReference source) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.notNull(target, "target");

        if (isSameType(target, source)) {
            return ConversionType.IDENTITY;
        }

        if (!source.isPrimitive()) {
            final TypeReference unboxedSourceType = getUnderlyingPrimitiveTypeOrSelf(source);

            if (unboxedSourceType == source || unboxedSourceType == BuiltinTypes.Void) {
                return ConversionType.NONE;
            }

            final ConversionType unboxedConversion = getNumericConversionType(target, unboxedSourceType);

            switch (unboxedConversion) {
                case IDENTITY:
                case IMPLICIT:
                    return ConversionType.IMPLICIT;
                case IMPLICIT_LOSSY:
                    return ConversionType.IMPLICIT_LOSSY; // loss of 'null' -> lossy
                case EXPLICIT:
                    return ConversionType.NONE;
                default:
                    return unboxedConversion;
            }
        }

        if (!target.isPrimitive()) {
            final TypeReference unboxedTargetType = getUnderlyingPrimitiveTypeOrSelf(target);

            if (unboxedTargetType == target || unboxedTargetType == BuiltinTypes.Void) {
                return ConversionType.NONE;
            }

            switch (getNumericConversionType(unboxedTargetType, source)) {
                case IDENTITY:
                    return ConversionType.IMPLICIT;
                case IMPLICIT:
                    return ConversionType.EXPLICIT_TO_UNBOXED;
                case IMPLICIT_LOSSY:
                    return ConversionType.EXPLICIT;
                default:
                    return ConversionType.NONE;
            }
        }

        final JvmType targetJvmType = target.getSimpleType();
        final JvmType sourceJvmType = source.getSimpleType();

        if (targetJvmType == sourceJvmType) {
            return ConversionType.IDENTITY;
        }

        if (sourceJvmType == JvmType.Boolean) {
            return ConversionType.NONE;
        }

        switch (targetJvmType) {
            case Float:
            case Double: {
                if (sourceJvmType.isIntegral()) {
                    return sourceJvmType.bitWidth() >= targetJvmType.bitWidth() ? ConversionType.IMPLICIT_LOSSY
                                                                                : ConversionType.IMPLICIT;
                }

                return sourceJvmType.bitWidth() <= targetJvmType.bitWidth() ? ConversionType.IMPLICIT
                                                                            : ConversionType.EXPLICIT;
            }

            case Byte:
            case Short: {
                if (sourceJvmType == JvmType.Character) {
                    return ConversionType.EXPLICIT;
                }
                // fall through
            }

            case Integer:
            case Long: {
                if (sourceJvmType.isIntegral() &&
                    sourceJvmType.bitWidth() <= targetJvmType.bitWidth()) {

                    return ConversionType.IMPLICIT;
                }

                return ConversionType.EXPLICIT;
            }

            case Character: {
                return sourceJvmType.isNumeric() ? ConversionType.EXPLICIT
                                                 : ConversionType.NONE;
            }
        }

        return ConversionType.NONE;
    }

    public static boolean hasImplicitNumericConversion(final TypeReference target, final TypeReference source) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.notNull(target, "target");

        if (target == source) {
            return true;
        }

        if (!target.isPrimitive() || !source.isPrimitive()) {
            return false;
        }

        final JvmType targetJvmType = target.getSimpleType();
        final JvmType sourceJvmType = source.getSimpleType();

        if (targetJvmType == sourceJvmType) {
            return true;
        }

        if (sourceJvmType == JvmType.Boolean) {
            return false;
        }

        switch (targetJvmType) {
            case Float:
            case Double:
                return sourceJvmType.bitWidth() <= targetJvmType.bitWidth();

            case Byte:
            case Short:
            case Integer:
            case Long:
                return sourceJvmType.isIntegral() &&
                       sourceJvmType.bitWidth() <= targetJvmType.bitWidth();
        }

        return false;
    }

    public static boolean isConvertible(final TypeReference source, final TypeReference target) {
        return isConvertible(source, target, true);
    }

    public static boolean isConvertible(final TypeReference source, final TypeReference target, final boolean allowUnchecked) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.notNull(target, "target");

        final boolean tPrimitive = target.isPrimitive();
        final boolean sPrimitive = source.isPrimitive();

        if (source == BuiltinTypes.Null) {
            return !tPrimitive;
        }

        if (target.isWildcardType() && target.isUnbounded()) {
            return !sPrimitive;
        }

        if (tPrimitive == sPrimitive) {
            return allowUnchecked ? isSubTypeUnchecked(source, target)
                                  : isSubType(source, target);
        }

        if (tPrimitive) {
            return getNumericConversionType(target, source).isImplicit();
        }

        return allowUnchecked ? isSubTypeUnchecked(getBoxedTypeOrSelf(source), target)
                              : isSubType(getBoxedTypeOrSelf(source), target);
    }

    private static boolean isSubTypeUnchecked(final TypeReference t, final TypeReference s) {
        return isSubtypeUncheckedInternal(t, s);
    }

    private static boolean isSubtypeUncheckedInternal(final TypeReference t, final TypeReference s) {
        if (t == s) {
            return true;
        }

        if (t == null || s == null) {
            return false;
        }

        if (t.isArray() && s.isArray()) {
            if (t.getElementType().isPrimitive()) {
                return isSameType(getElementType(t), getElementType(s));
            }
            else {
                return isSubTypeUnchecked(getElementType(t), getElementType(s));
            }
        }
        else if (isSubType(t, s)) {
            return true;
        }
        else if (t.isGenericParameter() && t.hasExtendsBound()) {
            return isSubTypeUnchecked(getUpperBound(t), s);
        }
        else if (!isRawType(s)) {
            final TypeReference t2 = asSuper(s, t);
            if (t2 != null && isRawType(t2)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isAssignableFrom(final TypeReference target, final TypeReference source) {
        return isConvertible(source, target);
    }

    public static boolean isAssignableFrom(final TypeReference target, final TypeReference source, final boolean allowUnchecked) {
        return isConvertible(source, target, allowUnchecked);
    }

    public static boolean isSubType(final TypeReference type, final TypeReference baseType) {
        VerifyArgument.notNull(type, "type");
        VerifyArgument.notNull(baseType, "baseType");

        return isSubType(type, baseType, true);
    }

    public static boolean isPrimitiveBoxType(final TypeReference type) {
        VerifyArgument.notNull(type, "type");

        switch (type.getInternalName()) {
            case "java/lang/Void":
            case "java/lang/Boolean":
            case "java/lang/Byte":
            case "java/lang/Character":
            case "java/lang/Short":
            case "java/lang/Integer":
            case "java/lang/Long":
            case "java/lang/Float":
            case "java/lang/Double":
                return true;

            default:
                return false;
        }
    }

    public static TypeReference getBoxedTypeOrSelf(final TypeReference type) {
        VerifyArgument.notNull(type, "type");

        if (type.isPrimitive()) {
            switch (type.getSimpleType()) {
                case Boolean:
                    return CommonTypeReferences.Boolean;
                case Byte:
                    return CommonTypeReferences.Byte;
                case Character:
                    return CommonTypeReferences.Character;
                case Short:
                    return CommonTypeReferences.Short;
                case Integer:
                    return CommonTypeReferences.Integer;
                case Long:
                    return CommonTypeReferences.Long;
                case Float:
                    return CommonTypeReferences.Float;
                case Double:
                    return CommonTypeReferences.Double;
                case Void:
                    return CommonTypeReferences.Void;
            }
        }

        return type;
    }

    @NotNull
    public static TypeReference getUnderlyingPrimitiveTypeOrSelf(@NotNull final TypeReference type) {
        VerifyArgument.notNull(type, "type");

        if (type.isPrimitive()) {
            return type;
        }

        switch (type.getInternalName()) {
            case "java/lang/Void":
                return BuiltinTypes.Void;
            case "java/lang/Boolean":
                return BuiltinTypes.Boolean;
            case "java/lang/Byte":
                return BuiltinTypes.Byte;
            case "java/lang/Character":
                return BuiltinTypes.Character;
            case "java/lang/Short":
                return BuiltinTypes.Short;
            case "java/lang/Integer":
                return BuiltinTypes.Integer;
            case "java/lang/Long":
                return BuiltinTypes.Long;
            case "java/lang/Float":
                return BuiltinTypes.Float;
            case "java/lang/Double":
                return BuiltinTypes.Double;
            default:
                return type;
        }
    }

    public static TypeReference getDeclaredType(final TypeReference type) {
        if (type == null) {
            return null;
        }

        final TypeDefinition resolvedType = type.resolve();

        if (resolvedType == null) {
            return type;
        }

        if (resolvedType.isAnonymous()) {
            final List<TypeReference> interfaces = resolvedType.getExplicitInterfaces();
            final TypeReference baseType = interfaces.isEmpty() ? resolvedType.getBaseType() : interfaces.get(0);

            if (baseType != null) {
                final TypeReference asSuperType = asSuper(baseType, type);

                if (asSuperType != null) {
                    return asSuperType;
                }

                return baseType.isGenericType() ? new RawType(baseType) : baseType;
            }
        }

        return type;
    }

    public static TypeReference getBaseType(final TypeReference type) {
        if (type == null) {
            return null;
        }

        final TypeDefinition resolvedType = type.resolve();

        if (resolvedType == null) {
            return null;
        }

        final TypeReference baseType = resolvedType.getBaseType();

        if (baseType == null) {
            return null;
        }

        return substituteGenericArguments(baseType, type);
    }

    public static List<TypeReference> getInterfaces(final TypeReference type) {
        final List<TypeReference> result = INTERFACES_VISITOR.visit(type);
        return result != null ? result : Collections.<TypeReference>emptyList();
    }

    public static TypeReference asSubType(final TypeReference type, final TypeReference baseType) {
        VerifyArgument.notNull(type, "type");
        VerifyArgument.notNull(baseType, "baseType");

        TypeReference effectiveType = type;

        if (type instanceof RawType) {
            effectiveType = type.getUnderlyingType();
        }
        else if (isRawType(type)) {
            final TypeDefinition resolvedType = type.resolve();
            effectiveType = resolvedType != null ? resolvedType : type;
        }

        return AS_SUBTYPE_VISITOR.visit(baseType, effectiveType);
    }

    public static TypeReference asSuper(final TypeReference type, final TypeReference subType) {
        VerifyArgument.notNull(subType, "t");
        VerifyArgument.notNull(type, "s");

        return AS_SUPER_VISITOR.visit(subType, type);
    }

    @SuppressWarnings("ConstantConditions")
    public static Map<TypeReference, TypeReference> getGenericSubTypeMappings(final TypeReference type, final TypeReference baseType) {
        VerifyArgument.notNull(type, "type");
        VerifyArgument.notNull(baseType, "baseType");

        if (type.isArray() && baseType.isArray()) {
            TypeReference elementType = type.getElementType();
            TypeReference baseElementType = baseType.getElementType();

            while (elementType.isArray() && baseElementType.isArray()) {
                elementType = elementType.getElementType();
                baseElementType = baseElementType.getElementType();
            }

            return getGenericSubTypeMappings(elementType, baseElementType);
        }

        TypeReference current = type;

        final List<? extends TypeReference> baseArguments;

        if (baseType.isGenericDefinition()) {
            baseArguments = baseType.getGenericParameters();
        }
        else if (baseType.isGenericType()) {
            baseArguments = ((IGenericInstance) baseType).getTypeArguments();
        }
        else {
            baseArguments = Collections.emptyList();
        }

        final TypeDefinition resolvedBaseType = baseType.resolve();

        while (current != null) {
            final TypeDefinition resolved = current.resolve();

            if (resolvedBaseType != null &&
                resolvedBaseType.isGenericDefinition() &&
                isSameType(resolved, resolvedBaseType)) {

                if (current instanceof IGenericInstance &&
                    baseType instanceof IGenericInstance) {

                    final List<? extends TypeReference> typeArguments = ((IGenericInstance) current).getTypeArguments();

                    if (baseArguments.size() == typeArguments.size()) {
                        final Map<TypeReference, TypeReference> map = new HashMap<>();

                        for (int i = 0; i < typeArguments.size(); i++) {
                            map.put(typeArguments.get(i), baseArguments.get(i));
                        }

                        return map;
                    }
                }
                else if (baseType instanceof IGenericInstance &&
                         resolved.isGenericDefinition()) {

                    final List<GenericParameter> genericParameters = resolved.getGenericParameters();
                    final List<? extends TypeReference> typeArguments = ((IGenericInstance) baseType).getTypeArguments();

                    if (genericParameters.size() == typeArguments.size()) {
                        final Map<TypeReference, TypeReference> map = new HashMap<>();

                        for (int i = 0; i < typeArguments.size(); i++) {
                            map.put(genericParameters.get(i), typeArguments.get(i));
                        }

                        return map;
                    }
                }
            }

            if (resolvedBaseType != null && resolvedBaseType.isInterface()) {
                for (final TypeReference interfaceType : getInterfaces(current)) {
                    final Map<TypeReference, TypeReference> interfaceMap = getGenericSubTypeMappings(interfaceType, baseType);

                    if (!interfaceMap.isEmpty()) {
                        return interfaceMap;
                    }
                }
            }

            current = getBaseType(current);
        }

        return Collections.emptyMap();
    }

    public static MethodReference asMemberOf(final MethodReference method, final TypeReference baseType) {
        VerifyArgument.notNull(method, "method");
        VerifyArgument.notNull(baseType, "baseType");

        final MethodReference asMember;

        TypeReference base = baseType;

        if (baseType instanceof RawType) {
            asMember = erase(method);
        }
        else {
            while (base.isGenericParameter() || base.isWildcardType()) {
                if (base.hasExtendsBound()) {
                    base = getUpperBound(base);
                }
                else {
                    base = BuiltinTypes.Object;
                }
            }

            final TypeReference asSuper = asSuper(method.getDeclaringType(), base);

            Map<TypeReference, TypeReference> map;

            try {
                map = adapt(method.getDeclaringType(), asSuper != null ? asSuper : base);
            }
            catch (final AdaptFailure ignored) {
                map = getGenericSubTypeMappings(method.getDeclaringType(), asSuper != null ? asSuper : base);
            }

            asMember = TypeSubstitutionVisitor.instance().visitMethod(method, map);

            if (asMember != method && asMember instanceof GenericMethodInstance) {
                ((GenericMethodInstance) asMember).setDeclaringType(asSuper != null ? asSuper : base);
            }
        }

        @SuppressWarnings("UnnecessaryLocalVariable")
        final MethodReference result = specializeIfNecessary(method, asMember, base);

        return result;
    }

    private static MethodReference specializeIfNecessary(
        final MethodReference originalMethod,
        final MethodReference asMember,
        final TypeReference baseType) {

        if (baseType.isArray() &&
            StringUtilities.equals(asMember.getName(), "clone") &&
            asMember.getParameters().isEmpty()) {

            return ensureReturnType(originalMethod, asMember, baseType, baseType);
        }
        else if (StringUtilities.equals(asMember.getName(), "getClass") &&
                 asMember.getParameters().isEmpty()) {

            TypeReference classType;
            TypeDefinition resolvedClassType;
            TypeDefinition resolvedType = baseType.resolve();

            //noinspection UnusedAssignment
            if (resolvedType == null ||
                (classType = resolvedType.getResolver().lookupType("java/lang/Class")) == null ||
                (resolvedClassType = classType.resolve()) == null) {

                resolvedType = originalMethod.getDeclaringType().resolve();
            }

            if (resolvedType == null ||
                (classType = resolvedType.getResolver().lookupType("java/lang/Class")) == null ||
                (resolvedClassType = classType.resolve()) == null) {

                return asMember;
            }

            if (resolvedClassType.isGenericType()) {
                final MethodDefinition resolvedMethod = originalMethod.resolve();

                return new GenericMethodInstance(
                    baseType,
                    resolvedMethod != null ? resolvedMethod : asMember,
                    resolvedClassType.makeGenericType(WildcardType.makeExtends(erase(baseType))),
                    Collections.<ParameterDefinition>emptyList(),
                    Collections.<TypeReference>emptyList()
                );
            }

            return asMember;
        }

        return asMember;
    }

    private static MethodReference ensureReturnType(
        final MethodReference originalMethod,
        final MethodReference method,
        final TypeReference returnType,
        final TypeReference declaringType) {

        if (isSameType(method.getReturnType(), returnType, true)) {
            return method;
        }

        final MethodDefinition resolvedMethod = originalMethod.resolve();
        final List<TypeReference> typeArguments;

        if (method instanceof IGenericInstance && method.isGenericMethod()) {
            typeArguments = ((IGenericInstance) method).getTypeArguments();
        }
        else {
            typeArguments = Collections.emptyList();
        }

        final MethodReference definition = resolvedMethod != null ? resolvedMethod
                                                                  : originalMethod;

        return new GenericMethodInstance(
            declaringType,
            definition,
            returnType,
            copyParameters(method.getParameters()),
            typeArguments
        );
    }

    @NotNull
    static List<TypeReference> checkTypeArguments(
        @Nullable final IGenericParameterProvider owner,
        @NotNull final List<TypeReference> typeArguments) {

        final boolean allowWildcards = !(owner instanceof IMethodSignature);

        if (typeArguments.isEmpty() || allowWildcards) {
            return typeArguments;
        }

        boolean valid = true;

        for (final TypeReference t : typeArguments) {
            if (t.isWildcardType() || t instanceof ICapturedType) {
                valid = false;
                break;
            }
        }

        if (valid) {
            return typeArguments;
        }

        final TypeReference[] adjustedTypeArguments = typeArguments.toArray(new TypeReference[typeArguments.size()]);

        for (int i = 0; i < adjustedTypeArguments.length; i++) {
            final TypeReference t = adjustedTypeArguments[i];

            if (t.isWildcardType() || t instanceof ICapturedType) {
                adjustedTypeArguments[i] = t.getExtendsBound();
            }
        }

        return ArrayUtilities.asUnmodifiableList(adjustedTypeArguments);
    }

    public static FieldReference asMemberOf(final FieldReference field, final TypeReference baseType) {
        VerifyArgument.notNull(field, "field");
        VerifyArgument.notNull(baseType, "baseType");

        final Map<TypeReference, TypeReference> map = adapt(field.getDeclaringType(), baseType);

        return TypeSubstitutionVisitor.instance().visitField(field, map);
    }

    public static TypeReference asMemberOf(final TypeReference innerType, final TypeReference baseType) {
        VerifyArgument.notNull(innerType, "innerType");
        VerifyArgument.notNull(baseType, "baseType");

        final Map<TypeReference, TypeReference> map = adapt(innerType.getDeclaringType(), baseType);

        return TypeSubstitutionVisitor.instance().visit(innerType, map);
    }

    public static TypeReference substituteGenericArguments(
        final TypeReference inputType,
        final TypeReference substitutionsProvider) {

        if (inputType == null || substitutionsProvider == null) {
            return inputType;
        }

        return substituteGenericArguments(inputType, adapt(inputType, substitutionsProvider));
    }

    public static TypeReference substituteGenericArguments(
        final TypeReference inputType,
        final MethodReference substitutionsProvider) {

        if (inputType == null) {
            return null;
        }

        if (substitutionsProvider == null || !isGenericSubstitutionNeeded(inputType)) {
            return inputType;
        }

        final TypeReference declaringType = substitutionsProvider.getDeclaringType();

        assert declaringType != null;

        if (!substitutionsProvider.isGenericMethod() && !declaringType.isGenericType()) {
            return null;
        }

        final List<? extends TypeReference> methodGenericParameters;
        final List<? extends TypeReference> genericParameters;
        final List<? extends TypeReference> methodTypeArguments;
        final List<? extends TypeReference> typeArguments;

        if (substitutionsProvider.isGenericMethod()) {
            methodGenericParameters = substitutionsProvider.getGenericParameters();
        }
        else {
            methodGenericParameters = Collections.emptyList();
        }

        if (substitutionsProvider.isGenericDefinition()) {
            methodTypeArguments = methodGenericParameters;
        }
        else {
            methodTypeArguments = ((IGenericInstance) substitutionsProvider).getTypeArguments();
        }

        if (declaringType.isGenericType()) {
            genericParameters = declaringType.getGenericParameters();

            if (declaringType.isGenericDefinition()) {
                typeArguments = genericParameters;
            }
            else {
                typeArguments = ((IGenericInstance) declaringType).getTypeArguments();
            }
        }
        else {
            genericParameters = Collections.emptyList();
            typeArguments = Collections.emptyList();
        }

        if (methodTypeArguments.isEmpty() && typeArguments.isEmpty()) {
            return inputType;
        }

        final Map<TypeReference, TypeReference> map = new HashMap<>();

        if (methodTypeArguments.size() == methodGenericParameters.size()) {
            for (int i = 0; i < methodTypeArguments.size(); i++) {
                map.put(methodGenericParameters.get(i), methodTypeArguments.get(i));
            }
        }

        if (typeArguments.size() == genericParameters.size()) {
            for (int i = 0; i < typeArguments.size(); i++) {
                map.put(genericParameters.get(i), typeArguments.get(i));
            }
        }

        return substituteGenericArguments(inputType, map);
    }

    public static TypeReference substituteGenericArguments(
        final TypeReference inputType,
        final Map<TypeReference, TypeReference> substitutionsProvider) {

        if (inputType == null) {
            return null;
        }

        if (substitutionsProvider == null || substitutionsProvider.isEmpty()) {
            return inputType;
        }

        return TypeSubstitutionVisitor.instance().visit(inputType, substitutionsProvider);
    }

    private static boolean isGenericSubstitutionNeeded(final TypeReference type) {
        if (type == null) {
            return false;
        }

        final TypeDefinition resolvedType = type.resolve();

        return resolvedType != null &&
               resolvedType.containsGenericParameters();
    }

    public static List<MethodReference> findMethods(final TypeReference type) {
        return findMethods(type, Predicates.alwaysTrue());
    }

    public static List<MethodReference> findMethods(
        final TypeReference type,
        final Predicate<? super MethodReference> filter) {

        return findMethods(type, filter, false);
    }

    public static List<MethodReference> findMethods(
        final TypeReference type,
        final Predicate<? super MethodReference> filter,
        final boolean includeBridgeMethods) {

        return findMethods(type, filter, includeBridgeMethods, false);
    }

    public static List<MethodReference> findMethods(
        final TypeReference type,
        final Predicate<? super MethodReference> filter,
        final boolean includeBridgeMethods,
        final boolean includeOverriddenMethods) {

        VerifyArgument.notNull(type, "type");
        VerifyArgument.notNull(filter, "filter");

        final Set<String> descriptors = new HashSet<>();
        final ArrayDeque<TypeReference> agenda = new ArrayDeque<>();

        List<MethodReference> results = null;

        agenda.addLast(getUpperBound(type));
        descriptors.add(type.getInternalName());

        while (!agenda.isEmpty()) {
            final TypeDefinition resolvedType = agenda.removeFirst().resolve();

            if (resolvedType == null) {
                break;
            }

            final TypeReference baseType = resolvedType.getBaseType();

            if (baseType != null && descriptors.add(baseType.getInternalName())) {
                agenda.addLast(baseType);
            }

            for (final TypeReference interfaceType : resolvedType.getExplicitInterfaces()) {
                if (interfaceType != null && descriptors.add(interfaceType.getInternalName())) {
                    agenda.addLast(interfaceType);
                }
            }

            for (final MethodDefinition method : resolvedType.getDeclaredMethods()) {
                if (!includeBridgeMethods && method.isBridgeMethod()) {
                    continue;
                }

                if (filter.test(method)) {
                    final String key = (includeOverriddenMethods ? method.getFullName() : method.getName()) + ":" + method.getErasedSignature();

                    if (descriptors.add(key)) {
                        if (results == null) {
                            results = new ArrayList<>();
                        }

                        final MethodReference asMember = asMemberOf(method, type);

                        results.add(asMember != null ? asMember : method);
                    }
                }
            }
        }

        return results != null ? results
                               : Collections.<MethodReference>emptyList();
    }

    public static boolean isOverloadCheckingRequired(final MethodReference method) {
        final MethodDefinition resolved = method.resolve();
        final boolean isVarArgs = resolved != null && resolved.isVarArgs();
        final TypeReference declaringType = (resolved != null ? resolved : method).getDeclaringType();
        final int parameterCount = (resolved != null ? resolved.getParameters() : method.getParameters()).size();

        final List<MethodReference> methods = findMethods(
            declaringType,
            Predicates.and(
                MetadataFilters.<MethodReference>matchName(method.getName()),
                new Predicate<MethodReference>() {
                    @Override
                    public boolean test(final MethodReference m) {
                        final List<ParameterDefinition> p = m.getParameters();

                        final MethodDefinition r = m instanceof MethodDefinition ? (MethodDefinition) m
                                                                                 : m.resolve();

                        if (r != null && r.isBridgeMethod()) {
                            return false;
                        }

                        if (isVarArgs) {
                            return r != null && r.isVarArgs() ||
                                   p.size() >= parameterCount;
                        }

                        if (p.size() < parameterCount) {
                            return r != null && r.isVarArgs();
                        }

                        return p.size() == parameterCount;
                    }
                }
            )
        );

        return methods.size() > 1;
    }

    public static boolean isInterface(final TypeReference t) {
        final TypeDefinition resolvedType = t.resolve();
        return resolvedType != null && resolvedType.isInterface();
    }

    public static TypeReference getLowerBound(final TypeReference t) {
        return LOWER_BOUND_VISITOR.visit(t);
    }

    public static TypeReference getUpperBound(final TypeReference t) {
        return UPPER_BOUND_VISITOR.visit(t);
    }

    public static TypeReference getElementType(final TypeReference t) {
        if (t.isArray()) {
            return t.getElementType();
        }

        if (t.isWildcardType()) {
            return getElementType(getUpperBound(t));
        }

        return null;
    }

    public static TypeReference getSuperType(final TypeReference t) {
        if (t == null) {
            return null;
        }

        return SUPER_VISITOR.visit(t);
    }

    public static boolean isSubTypeNoCapture(final TypeReference type, final TypeReference baseType) {
        return isSubType(type, baseType, false);
    }

    public static boolean isSubType(final TypeReference type, final TypeReference baseType, final boolean capture) {
        if (type == baseType) {
            return true;
        }

        if (type == null || baseType == null) {
            return false;
        }

        if (baseType instanceof CompoundTypeReference) {
            final CompoundTypeReference c = (CompoundTypeReference) baseType;

            if (!isSubType(type, getSuperType(c), capture)) {
                return false;
            }

            for (final TypeReference interfaceType : c.getInterfaces()) {
                if (!isSubType(type, interfaceType, capture)) {
                    return false;
                }
            }

            return true;
        }

        final TypeReference lower = getLowerBound(baseType);

        if (lower != baseType) {
            return isSubType(capture ? capture(type) : type, lower, false);
        }

        return IS_SUBTYPE_VISITOR.visit(capture ? capture(type) : type, baseType);
    }

    private static TypeReference capture(final TypeReference type) {
        // TODO: Implement wildcard capture.
        return type;
    }

    public static Map<TypeReference, TypeReference> adapt(final TypeReference source, final TypeReference target) {
        final Adapter adapter = new Adapter();
        adapter.visit(source, target);
        return adapter.mapping;
    }

    private static Map<TypeReference, TypeReference> adaptSelf(final TypeReference t) {
        final TypeDefinition r = t.resolve();

        return r != null ? adapt(r, t)
                         : Collections.<TypeReference, TypeReference>emptyMap();
    }

    private static TypeReference rewriteSupers(final TypeReference t) {
        if (!(t instanceof IGenericInstance)) {
            return t;
        }

        final Map<TypeReference, TypeReference> map = adaptSelf(t);

        if (map.isEmpty()) {
            return t;
        }

        Map<TypeReference, TypeReference> rewrite = null;

        for (final TypeReference k : map.keySet()) {
            final TypeReference original = map.get(k);

            TypeReference s = rewriteSupers(original);

            if (s.hasSuperBound() && !s.hasExtendsBound()) {
                s = WildcardType.unbounded();

                if (rewrite == null) {
                    rewrite = new HashMap<>(map);
                }
            }
            else if (s != original) {
                s = WildcardType.makeExtends(getUpperBound(s));

                if (rewrite == null) {
                    rewrite = new HashMap<>(map);
                }
            }

            if (rewrite != null) {
                map.put(k, s);
            }
        }

        if (rewrite != null) {
            return substituteGenericArguments(t, rewrite);
        }

        else {
            return t;
        }
    }

    /**
     * Check if {@code t} contains {@code s}.
     *
     * <p>{@code T} contains {@code S} if:
     *
     * <p>{@code L(T) <: L(S) && U(S) <: U(T)}
     *
     * <p>This relation is only used by isSubType(), that is:
     *
     * <p>{@code C<S> <: C<T> if T contains S.}
     *
     * <p>Because of F-bounds, this relation can lead to infinite recursion.  Thus, we must
     * somehow break that recursion.  Notice that containsType() is only called from isSubType().
     * Since the arguments have already been checked against their bounds, we know:
     *
     * <p>{@code U(S) <: U(T) if T is "super" bound (U(T) *is* the bound)}
     *
     * <p>{@code L(T) <: L(S) if T is "extends" bound (L(T) is bottom)}
     *
     * @param t
     *     a type
     * @param s
     *     a type
     */
    public static boolean containsType(final TypeReference t, final TypeReference s) {
        return CONTAINS_TYPE_VISITOR.visit(t, s);
    }

    public static boolean isSameType(final TypeReference t, final TypeReference s) {
        return isSameType(t, s, false);
    }

    public static boolean isSameType(final TypeReference t, final TypeReference s, final boolean strict) {
        if (t == s) {
            return true;
        }

        if (t == null || s == null) {
            return false;
        }

        return strict ? SAME_TYPE_VISITOR_STRICT.visit(t, s)
                      : SAME_TYPE_VISITOR_LOOSE.visit(t, s);
    }

    public static boolean areSameTypes(final List<? extends TypeReference> t, final List<? extends TypeReference> s) {
        return areSameTypes(t, s, false);
    }

    public static boolean areSameTypes(
        final List<? extends TypeReference> t,
        final List<? extends TypeReference> s,
        final boolean strict) {

        if (t.size() != s.size()) {
            return false;
        }

        for (int i = 0, n = t.size(); i < n; i++) {
            if (!isSameType(t.get(i), s.get(i), strict)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isCaptureOf(final TypeReference t, final TypeReference s) {
        return isSameWildcard(t, s);
    }

    private static boolean isSameWildcard(final TypeReference t, final TypeReference s) {
        VerifyArgument.notNull(t, "t");
        VerifyArgument.notNull(s, "s");

        if (!t.isWildcardType() || !s.isWildcardType()) {
            return false;
        }

        if (t.isUnbounded()) {
            return s.isUnbounded();
        }

        if (t.hasSuperBound()) {
            return s.hasSuperBound() && isSameType(t.getSuperBound(), s.getSuperBound());
        }

        return s.hasExtendsBound() && isSameType(t.getExtendsBound(), s.getExtendsBound());
    }

    private static List<? extends TypeReference> getTypeArguments(final TypeReference t) {
        if (t instanceof IGenericInstance) {
            return ((IGenericInstance) t).getTypeArguments();
        }

        if (t.isGenericType()) {
            return t.getGenericParameters();
        }

        return Collections.emptyList();
    }

    private static boolean containsType(final List<? extends TypeReference> t, final List<? extends TypeReference> s) {
        if (t.size() != s.size()) {
            return false;
        }

        if (t.isEmpty()) {
            return true;
        }

        for (int i = 0, n = t.size(); i < n; i++) {
            if (!containsType(t.get(i), s.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean containsTypeEquivalent(final TypeReference t, final TypeReference s) {
        return s == t ||
               containsType(t, s) && containsType(s, t);
    }

    private static boolean containsTypeEquivalent(final List<? extends TypeReference> t, final List<? extends TypeReference> s) {
        if (t.size() != s.size()) {
            return false;
        }

        for (int i = 0, n = t.size(); i < n; i++) {
            if (!containsTypeEquivalent(t.get(i), s.get(i))) {
                return false;
            }
        }

        return true;
    }

    private final static ThreadLocal<HashSet<Pair<TypeReference, TypeReference>>> CONTAINS_TYPE_CACHE =
        new ThreadLocal<HashSet<Pair<TypeReference, TypeReference>>>() {
            @Override
            protected final HashSet<Pair<TypeReference, TypeReference>> initialValue() {
                return new HashSet<>();
            }
        };

    private final static ThreadLocal<HashSet<Pair<TypeReference, TypeReference>>> ADAPT_CACHE =
        new ThreadLocal<HashSet<Pair<TypeReference, TypeReference>>>() {
            @Override
            protected final HashSet<Pair<TypeReference, TypeReference>> initialValue() {
                return new HashSet<>();
            }
        };

    private static boolean containsTypeRecursive(final TypeReference t, final TypeReference s) {
        final HashSet<Pair<TypeReference, TypeReference>> cache = CONTAINS_TYPE_CACHE.get();
        final Pair<TypeReference, TypeReference> pair = new Pair<>(t, s);

        if (cache.add(pair)) {
            try {
                return containsType(getTypeArguments(t), getTypeArguments(s));
            }
            finally {
                cache.remove(pair);
            }
        }
        else {
            return containsType(getTypeArguments(t), getTypeArguments(rewriteSupers(s)));
        }
    }

    private static TypeReference arraySuperType(final TypeReference t) {
        final TypeDefinition resolved = t.resolve();

        if (resolved != null) {
            final IMetadataResolver resolver = resolved.getResolver();
            final TypeReference cloneable = resolver.lookupType("java/lang/Cloneable");
            final TypeReference serializable = resolver.lookupType("java/io/Serializable");

            if (cloneable != null) {
                if (serializable != null) {
                    return new CompoundTypeReference(
                        null,
                        ArrayUtilities.asUnmodifiableList(cloneable, serializable)
                    );
                }
                return cloneable;
            }

            if (serializable != null) {
                return serializable;
            }
        }

        return BuiltinTypes.Object;
    }

    public static boolean isRawType(final TypeReference t) {
        if (t == null) {
            return false;
        }

        if (t instanceof RawType) {
            return true;
        }

        if (t.isGenericType()) {
            return false;
        }

        final TypeReference r = t.resolve();

        return r != null &&
               r.isGenericType();
    }

    public static int getUnboundGenericParameterCount(final TypeReference t) {
        if (t == null || t instanceof RawType || !t.isGenericType()) {
            return 0;
        }

        final List<GenericParameter> genericParameters = t.getGenericParameters();

        if (t.isGenericDefinition()) {
            return genericParameters.size();
        }

        final IGenericParameterProvider genericDefinition = ((IGenericInstance) t).getGenericDefinition();

        if (!genericDefinition.isGenericDefinition()) {
            return 0;
        }

        final List<TypeReference> typeArguments = ((IGenericInstance) t).getTypeArguments();

        assert genericParameters.size() == typeArguments.size();

        int count = 0;

        for (int i = 0; i < genericParameters.size(); i++) {
            final GenericParameter genericParameter = genericParameters.get(i);
            final TypeReference typeArgument = typeArguments.get(i);

            if (isSameType(genericParameter, typeArgument, true)) {
                ++count;
            }
        }

        return count;
    }

    public static List<TypeReference> eraseRecursive(final List<TypeReference> types) {
        ArrayList<TypeReference> result = null;

        for (int i = 0, n = types.size(); i < n; i++) {
            final TypeReference type = types.get(i);
            final TypeReference erased = eraseRecursive(type);

            if (result != null) {
                result.set(i, erased);
            }
            else if (type != erased) {
                result = new ArrayList<>(types);
                result.set(i, erased);
            }
        }

        return result != null ? result : types;
    }

    public static TypeReference eraseRecursive(final TypeReference type) {
        return erase(type, true);
    }

    private static boolean eraseNotNeeded(final TypeReference type) {
        return type == null ||
               type instanceof RawType ||
               type.isPrimitive() ||
               StringUtilities.equals(type.getInternalName(), CommonTypeReferences.String.getInternalName());
    }

    public static TypeReference erase(final TypeReference type) {
        return erase(type, false);
    }

    public static TypeReference erase(final TypeReference type, final boolean recurse) {
        if (eraseNotNeeded(type)) {
            return type;
        }

        return type.accept(ERASE_VISITOR, recurse);
    }

    public static MethodReference erase(final MethodReference method) {
        if (method != null) {
            MethodReference baseMethod = method;

            final MethodDefinition resolvedMethod = baseMethod.resolve();

            if (resolvedMethod != null) {
                baseMethod = resolvedMethod;
            }
            else if (baseMethod instanceof IGenericInstance) {
                baseMethod = (MethodReference) ((IGenericInstance) baseMethod).getGenericDefinition();
            }

            if (baseMethod != null) {
                return new RawMethod(baseMethod);
            }
        }
        return method;
    }

    private static TypeReference classBound(final TypeReference t) {
        //
        // TODO: Implement class bound computation.
        //
        return t;
    }

    public static boolean isOverride(final MethodDefinition method, final MethodReference ancestorMethod) {
        final MethodDefinition resolvedAncestor = ancestorMethod.resolve();

        if (resolvedAncestor == null || resolvedAncestor.isFinal() || resolvedAncestor.isPrivate() || resolvedAncestor.isStatic()) {
            return false;
        }

        final int modifiers = method.getModifiers() & Flags.AccessFlags;
        final int ancestorModifiers = resolvedAncestor.getModifiers() & Flags.AccessFlags;

        if (modifiers != ancestorModifiers) {
            return false;
        }

        if (!StringUtilities.equals(method.getName(), ancestorMethod.getName())) {
            return false;
        }

        if (method.getDeclaringType().isInterface()) {
            return false;
        }

        final MethodDefinition resolved = method.resolve();

        final TypeReference declaringType = erase(
            resolved != null ? resolved.getDeclaringType()
                             : method.getDeclaringType()
        );

        final TypeReference ancestorDeclaringType = erase(resolvedAncestor.getDeclaringType());

        if (isSameType(declaringType, ancestorDeclaringType)) {
            return false;
        }

        if (StringUtilities.equals(method.getErasedSignature(), ancestorMethod.getErasedSignature())) {
            return true;
        }

        if (!isSubType(declaringType, ancestorDeclaringType)) {
            return false;
        }

        final List<ParameterDefinition> parameters = method.getParameters();
        final List<ParameterDefinition> ancestorParameters = ancestorMethod.getParameters();

        if (parameters.size() != ancestorParameters.size()) {
            return false;
        }

        final TypeReference ancestorReturnType = erase(ancestorMethod.getReturnType());
        final TypeReference baseReturnType = erase(method.getReturnType());

        if (!isAssignableFrom(ancestorReturnType, baseReturnType)) {
            return false;
        }

        for (int i = 0, n = ancestorParameters.size(); i < n; i++) {
            final TypeReference parameterType = erase(parameters.get(i).getParameterType());
            final TypeReference ancestorParameterType = erase(ancestorParameters.get(i).getParameterType());

            if (!isSameType(parameterType, ancestorParameterType, false)) {
                return false;
            }
        }

        return true;
    }

    // <editor-fold defaultstate="collapsed" desc="Visitors">

    private final static TypeMapper<Void> UPPER_BOUND_VISITOR = new TypeMapper<Void>() {
        @Override
        public TypeReference visitType(final TypeReference t, final Void ignored) {
            if (t.isWildcardType() || t.isGenericParameter() || t instanceof ICapturedType) {
                return t.isUnbounded() || t.hasSuperBound() ? BuiltinTypes.Object
                                                            : visit(t.getExtendsBound());
            }
            return t;
        }

        @Override
        public TypeReference visitCapturedType(final CapturedType t, final Void ignored) {
            return t.getExtendsBound();
        }

        @Override
        public TypeReference visitArrayType(final ArrayType t, final Void ignored) {
            final TypeReference oldElementType = t.getElementType();
            final TypeReference newElementType = visit(t.getElementType(), ignored);

            if (oldElementType != newElementType) {
                return newElementType.makeArrayType();
            }

            return t;
        }
    };

    private final static TypeMapper<Void> LOWER_BOUND_VISITOR = new TypeMapper<Void>() {
        @Override
        public TypeReference visitWildcard(final WildcardType t, final Void ignored) {
            return t.hasSuperBound() ? visit(t.getSuperBound())
                                     : BuiltinTypes.Bottom;
        }

        @Override
        public TypeReference visitCapturedType(final CapturedType t, final Void ignored) {
            return t.getSuperBound();
        }

        @Override
        public TypeReference visitArrayType(final ArrayType t, final Void ignored) {
            final TypeReference oldElementType = t.getElementType();
            final TypeReference newElementType = visit(t.getElementType(), ignored);

            if (oldElementType != newElementType) {
                return newElementType.makeArrayType();
            }

            return t;
        }
    };

    private final static TypeRelation IS_SUBTYPE_VISITOR = new TypeRelation() {
        @Override
        public Boolean visitArrayType(final ArrayType t, final TypeReference s) {
            if (s.isArray()) {
                final TypeReference et = getElementType(t);
                final TypeReference es = getElementType(s);

                assert et != null && es != null;
                
                if (et.isPrimitive()) {
                    return isSameType(et, es);
                }

                return isSubTypeNoCapture(et, es);
            }

            final String sName = s.getInternalName();

            return StringUtilities.equals(sName, "java/lang/Object") ||
                   StringUtilities.equals(sName, "java/lang/Cloneable") ||
                   StringUtilities.equals(sName, "java/io/Serializable");
        }

        @Override
        public Boolean visitBottomType(final TypeReference t, final TypeReference s) {
            switch (t.getSimpleType()) {
                case Object:
                case Array:
                case TypeVariable:
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public Boolean visitClassType(final TypeReference t, final TypeReference s) {
            final TypeReference superType = asSuper(s, t);

            return superType != null &&
                   StringUtilities.equals(superType.getInternalName(), s.getInternalName()) &&
                   // You're not allowed to write
                   //     Vector<Object> vec = new Vector<String>();
                   // But with wildcards you can write
                   //     Vector<? extends Object> vec = new Vector<String>();
                   // which means that subtype checking must be done
                   // here instead of same-type checking (via containsType).
                   (!(s instanceof IGenericInstance) || containsTypeRecursive(s, superType)) &&
                   isSubTypeNoCapture(superType.getDeclaringType(), s.getDeclaringType());
        }

        @Override
        public Boolean visitCompoundType(final CompoundTypeReference t, final TypeReference s) {
            return super.visitCompoundType(t, s);
        }

        @Override
        public Boolean visitGenericParameter(final GenericParameter t, final TypeReference s) {
            return isSubTypeNoCapture(
                t.hasExtendsBound() ? t.getExtendsBound() : BuiltinTypes.Object,
                s
            );
        }

        @Override
        public Boolean visitParameterizedType(final TypeReference t, final TypeReference s) {
            return visitClassType(t, s);
        }

        @Override
        public Boolean visitPrimitiveType(final PrimitiveType t, final TypeReference s) {
            final JvmType jt = t.getSimpleType();
            final JvmType js = s.getSimpleType();

            if (jt == js) {
                return true;
            }

            if (jt == JvmType.Boolean || js == JvmType.Boolean) {
                return false;
            }

            switch (js) {
                case Byte:
                    return jt != JvmType.Character && jt.isIntegral() && jt.bitWidth() <= js.bitWidth();

                case Short:
                    if (jt == JvmType.Character) {
                        return false;
                    }
                    // fall through
                case Integer:
                case Long:
                    return jt.isIntegral() && jt.bitWidth() <= js.bitWidth();

                case Float:
                case Double:
                    return jt.isIntegral() || jt.bitWidth() <= js.bitWidth();

                default:
                    return Boolean.FALSE;
            }
        }

        @Override
        public Boolean visitRawType(final RawType t, final TypeReference s) {
            return visitClassType(t, s);
        }

        @Override
        public Boolean visitWildcard(final WildcardType t, final TypeReference s) {
            //
            // We shouldn't be here.  Return FALSE to avoid crash.
            //
            return Boolean.FALSE;
        }

        @Override
        public Boolean visitCapturedType(final CapturedType t, final TypeReference s) {
            return isSubTypeNoCapture(
                t.hasExtendsBound() ? t.getExtendsBound() : BuiltinTypes.Object,
                s
            );
        }

        @Override
        public Boolean visitType(final TypeReference t, final TypeReference s) {
            return Boolean.FALSE;
        }
    };

    private final static TypeRelation CONTAINS_TYPE_VISITOR = new TypeRelation() {
        private TypeReference U(final TypeReference t) {
            TypeReference current = t;

            while (current.isWildcardType()) {
                if (current.isUnbounded()) {
                    return BuiltinTypes.Object;
                }

                if (current.hasSuperBound()) {
                    return current.getSuperBound();
                }

                current = current.getExtendsBound();
            }

            return current;
        }

        private TypeReference L(final TypeReference t) {
            TypeReference current = t;

            while (current.isWildcardType()) {
                if (current.isUnbounded() || current.hasExtendsBound()) {
                    return BuiltinTypes.Bottom;
                }

                current = current.getSuperBound();
            }

            return current;
        }

        @Override
        public Boolean visitType(final TypeReference t, final TypeReference s) {
            return isSameType(t, s);
        }

        @Override
        public Boolean visitWildcard(final WildcardType t, final TypeReference s) {
            return isSameWildcard(t, s) ||
                   isCaptureOf(s, t) ||
                   (t.hasExtendsBound() || isSubTypeNoCapture(L(t), getLowerBound(s))) &&
                   (t.hasSuperBound() || isSubTypeNoCapture(getUpperBound(s), U(t)));
        }
    };

    private final static TypeMapper<TypeReference> AS_SUPER_VISITOR = new TypeMapper<TypeReference>() {
        @Override
        public TypeReference visitType(final TypeReference t, final TypeReference s) {
            return null;
        }

        @Override
        public TypeReference visitArrayType(final ArrayType t, final TypeReference s) {
            return isSubType(t, s) ? s : null;
        }

        @Override
        public TypeReference visitClassType(final TypeReference t, final TypeReference s) {
            if (StringUtilities.equals(t.getInternalName(), s.getInternalName())) {
                return t;
            }

            final TypeReference st = getSuperType(t);

            if (st != null &&
                (st.getSimpleType() == JvmType.Object ||
                 st.getSimpleType() == JvmType.TypeVariable)) {

                final TypeReference x = asSuper(s, st);

                if (x != null) {
                    return x;
                }
            }

            final TypeDefinition ds = s.resolve();

            if (ds != null && ds.isInterface()) {
                for (final TypeReference i : getInterfaces(t)) {
                    final TypeReference x = asSuper(s, i);

                    if (x != null) {
                        return x;
                    }
                }
            }

            return null;
        }

        @Override
        public TypeReference visitGenericParameter(final GenericParameter t, final TypeReference s) {
            if (isSameType(t, s)) {
                return t;
            }
            return asSuper(s, t.hasExtendsBound() ? t.getExtendsBound() : BuiltinTypes.Object);
        }

        @Override
        public TypeReference visitNullType(final TypeReference t, final TypeReference s) {
            return super.visitNullType(t, s);
        }

        @Override
        public TypeReference visitParameterizedType(final TypeReference t, final TypeReference s) {
//            final TypeReference r = this.visitClassType(t, s);
//            return substituteGenericArguments(r, adapt(t, s));
            return this.visitClassType(t, s);
        }

        @Override
        public TypeReference visitPrimitiveType(final PrimitiveType t, final TypeReference s) {
            return super.visitPrimitiveType(t, s);
        }

        @Override
        public TypeReference visitRawType(final RawType t, final TypeReference s) {
            return this.visitClassType(t, s);
        }

        @Override
        public TypeReference visitWildcard(final WildcardType t, final TypeReference s) {
            return super.visitWildcard(t, s);
        }
    };

    private final static TypeMapper<Void> SUPER_VISITOR = new TypeMapper<Void>() {
        @Override
        public TypeReference visitType(final TypeReference t, final Void ignored) {
            return null;
        }

        @Override
        public TypeReference visitArrayType(final ArrayType t, final Void ignored) {
            final TypeReference et = getElementType(t);

            assert et != null;
            
            if (et.isPrimitive() || isSameType(et, BuiltinTypes.Object)) {
                return arraySuperType(et);
            }

            final TypeReference superType = getSuperType(et);

            return superType != null ? superType.makeArrayType()
                                     : null;
        }

        @Override
        public TypeReference visitCompoundType(final CompoundTypeReference t, final Void ignored) {
            //
            // TODO: Is this correct?
            //

            final TypeReference bt = t.getBaseType();

            if (bt != null) {
                return getSuperType(bt);
            }

            return t;
        }

        @Override
        public TypeReference visitClassType(final TypeReference t, final Void ignored) {
            final TypeDefinition resolved = t.resolve();

            if (resolved == null) {
                return BuiltinTypes.Object;
            }

            TypeReference superType;

            if (resolved.isInterface()) {
                superType = resolved.getBaseType();

                if (superType == null) {
                    superType = firstOrDefault(resolved.getExplicitInterfaces());
                }
            }
            else {
                superType = resolved.getBaseType();
            }

            if (superType == null) {
                return null;
            }

            if (resolved.isGenericDefinition()) {
                if (!t.isGenericType()) {
                    return eraseRecursive(superType);
                }

                if (t.isGenericDefinition()) {
                    return superType;
                }

                return substituteGenericArguments(superType, classBound(t));
            }

            return superType;
        }

        @Override
        public TypeReference visitGenericParameter(final GenericParameter t, final Void ignored) {
            return t.hasExtendsBound() ? t.getExtendsBound()
                                       : BuiltinTypes.Object;
        }

        @Override
        public TypeReference visitNullType(final TypeReference t, final Void ignored) {
            return BuiltinTypes.Object;
        }

        @Override
        public TypeReference visitParameterizedType(final TypeReference t, final Void ignored) {
            return visitClassType(t, ignored);
        }

        @Override
        public TypeReference visitRawType(final RawType t, final Void ignored) {
            TypeReference genericDefinition = t.getUnderlyingType();

            if (!genericDefinition.isGenericDefinition()) {
                final TypeDefinition resolved = genericDefinition.resolve();

                if (resolved == null || !resolved.isGenericDefinition()) {
                    return BuiltinTypes.Object;
                }

                genericDefinition = resolved;
            }

            final TypeReference baseType = getBaseType(genericDefinition);

            return baseType != null && baseType.isGenericType() ? eraseRecursive(baseType)
                                                                : baseType;
        }

        @Override
        public TypeReference visitWildcard(final WildcardType t, final Void ignored) {
            if (t.isUnbounded()) {
                return BuiltinTypes.Object;
            }

            if (t.hasExtendsBound()) {
                return t.getExtendsBound();
            }

            //
            // A note on wildcards: there is no good way to determine a supertype for a
            // super bounded wildcard.
            //
            return null;
        }
    };

    static List<ParameterDefinition> copyParameters(final List<ParameterDefinition> parameters) {
        final List<ParameterDefinition> newParameters = new ArrayList<>();

        for (final ParameterDefinition p : parameters) {
            if (p.hasName()) {
                newParameters.add(new ParameterDefinition(p.getSlot(), p.getName(), p.getParameterType()));
            }
            else {
                newParameters.add(new ParameterDefinition(p.getSlot(), p.getParameterType()));
            }
        }

        return newParameters;
    }

    private final static class Adapter extends DefaultTypeVisitor<TypeReference, Void> {
        final ListBuffer<TypeReference> from = ListBuffer.lb();
        final ListBuffer<TypeReference> to = ListBuffer.lb();
        final Map<TypeReference, TypeReference> mapping = new HashMap<>();

        private void adaptRecursive(
            final List<? extends TypeReference> source,
            final List<? extends TypeReference> target) {

            if (source.size() == target.size()) {
                for (int i = 0, n = source.size(); i < n; i++) {
                    adaptRecursive(source.get(i), target.get(i));
                }
            }
        }

        @Override
        public Void visitClassType(final TypeReference source, final TypeReference target) {
            adaptRecursive(getTypeArguments(source), getTypeArguments(target));
            return null;
        }

        @Override
        public Void visitParameterizedType(final TypeReference source, final TypeReference target) {
            adaptRecursive(getTypeArguments(source), getTypeArguments(target));
            return null;
        }

        private void adaptRecursive(final TypeReference source, final TypeReference target) {
            final HashSet<Pair<TypeReference, TypeReference>> cache = ADAPT_CACHE.get();
            final Pair<TypeReference, TypeReference> pair = Pair.create(source, target);

            if (cache.add(pair)) {
                try {
                    visit(source, target);
                }
                finally {
                    cache.remove(pair);
                }
            }
        }

        @Override
        public Void visitArrayType(final ArrayType source, final TypeReference target) {
            if (target.isArray()) {
                adaptRecursive(getElementType(source), getElementType(target));
            }
            return null;
        }

        @Override
        public Void visitWildcard(final WildcardType source, final TypeReference target) {
            if (source.hasExtendsBound()) {
                adaptRecursive(getUpperBound(source), getUpperBound(target));
            }
            else if (source.hasSuperBound()) {
                adaptRecursive(getLowerBound(source), getLowerBound(target));
            }
            return null;
        }

        @Override
        @SuppressWarnings("StatementWithEmptyBody")
        public Void visitGenericParameter(final GenericParameter source, final TypeReference target) {
            TypeReference value = mapping.get(source);

            if (value != null) {
                if (value.hasSuperBound() && target.hasSuperBound()) {
                    value = isSubType(getLowerBound(value), getLowerBound(target)) ? target
                                                                                   : value;
                }
                else if (value.hasExtendsBound() && target.hasExtendsBound()) {
                    value = isSubType(getUpperBound(value), getUpperBound(target)) ? value
                                                                                   : target;
                }
                else if (value.isWildcardType() && value.isUnbounded()) {
                    // do nothing
                }
                else if (!isSameType(value, target)) {
                    throw new AdaptFailure();
                }
            }
            else {
                value = target;
                from.append(source);
                to.append(target);
            }

            mapping.put(source, value);

            return null;
        }
    }

    public static class AdaptFailure extends RuntimeException {
        static final long serialVersionUID = -7490231548272701566L;
    }

    private final static SameTypeVisitor SAME_TYPE_VISITOR_LOOSE = new LooseSameTypeVisitor();
    private final static SameTypeVisitor SAME_TYPE_VISITOR_STRICT = new StrictSameTypeVisitor();

    abstract static class SameTypeVisitor extends TypeRelation {
        abstract boolean areSameGenericParameters(final GenericParameter gp1, final GenericParameter gp2);
        abstract protected boolean containsTypes(final List<? extends TypeReference> t1, final List<? extends TypeReference> t2);

        @Override
        public Boolean visit(final TypeReference t, final TypeReference s) {
            if (t == null) {
                return s == null;
            }

            if (s == null) {
                return false;
            }

            return t.accept(this, s);
        }

        @Override
        public Boolean visitType(final TypeReference t, final TypeReference s) {
            return Boolean.FALSE;
        }

        @Override
        public Boolean visitArrayType(final ArrayType t, final TypeReference s) {
            return s.isArray() &&
                   containsTypeEquivalent(getElementType(t), getElementType(s));
        }

        @Override
        public Boolean visitBottomType(final TypeReference t, final TypeReference s) {
            return t == s;
        }

        @Override
        public Boolean visitClassType(final TypeReference t, final TypeReference s) {
            if (t == s) {
                return true;
            }

            if (!(t instanceof RawType) && isRawType(t)) {
                final TypeDefinition tResolved = t.resolve();

                if (tResolved != null) {
                    return visitClassType(tResolved, s);
                }
            }

            if (!(s instanceof RawType) && isRawType(s)) {
                final TypeDefinition sResolved = s.resolve();

                if (sResolved != null) {
                    return visitClassType(t, sResolved);
                }
            }

            if (t.isGenericDefinition()) {
                return s.isGenericDefinition() &&
                       StringUtilities.equals(t.getInternalName(), s.getInternalName()) &&
                       visit(t.getDeclaringType(), s.getDeclaringType());
            }

            return s.getSimpleType() == JvmType.Object &&
                   StringUtilities.equals(t.getInternalName(), s.getInternalName()) &&
                   //isSameType(t.getDeclaringType(), s.getDeclaringType(), false) &&
                   containsTypes(getTypeArguments(t), getTypeArguments(s));
        }

        @Override
        public Boolean visitCompoundType(final CompoundTypeReference t, final TypeReference s) {
            if (!s.isCompoundType()) {
                return false;
            }

            if (!visit(getSuperType(t), getSuperType(s))) {
                return false;
            }

            final HashSet<TypeReference> set = new HashSet<>();

            for (final TypeReference i : getInterfaces(t)) {
                set.add(i);
            }

            for (final TypeReference i : getInterfaces(s)) {
                if (!set.remove(i)) {
                    return false;
                }
            }

            return set.isEmpty();
        }

        @Override
        public Boolean visitGenericParameter(final GenericParameter t, final TypeReference s) {
            if (s instanceof GenericParameter) {
                //
                // Type substitution does not preserve type variable types; check that type variable
                // bounds are indeed the same.
                //
                return areSameGenericParameters(t, (GenericParameter) s);
            }

            //
            // Special case for s == ? super X, where upper(s) == u; check that u == t.
            //

            return s.hasSuperBound() &&
                   !s.hasExtendsBound() &&
                   visit(t, getUpperBound(s));
        }

        @Override
        public Boolean visitNullType(final TypeReference t, final TypeReference s) {
            return t == s;
        }

        @Override
        public Boolean visitParameterizedType(final TypeReference t, final TypeReference s) {
            return visitClassType(t, s);
        }

        @Override
        public Boolean visitPrimitiveType(final PrimitiveType t, final TypeReference s) {
            return t.getSimpleType() == s.getSimpleType();
        }

        @Override
        public Boolean visitRawType(final RawType t, final TypeReference s) {
            return s.getSimpleType() == JvmType.Object &&
                   !s.isGenericType() &&
                   StringUtilities.equals(t.getInternalName(), s.getInternalName());
        }

        @Override
        public Boolean visitWildcard(final WildcardType t, final TypeReference s) {
            if (s.isWildcardType()) {
                if (t.isUnbounded()) {
                    return s.isUnbounded();
                }

                if (t.hasExtendsBound()) {
                    return s.hasExtendsBound() &&
                           visit(getUpperBound(t), getUpperBound(s));
                }

                if (t.hasSuperBound()) {
                    return s.hasSuperBound() &&
                           visit(getLowerBound(t), getLowerBound(s));
                }
            }

            return Boolean.FALSE;
        }
    }

    final static class LooseSameTypeVisitor extends SameTypeVisitor {
        @Override
        boolean areSameGenericParameters(final GenericParameter gp1, final GenericParameter gp2) {
            if (gp1 == gp2) {
                return true;
            }

            if (gp1 == null || gp2 == null) {
                return false;
            }

            if (!StringUtilities.equals(gp1.getName(), gp2.getName())) {
                return false;
            }

            final IGenericParameterProvider owner1 = gp1.getOwner();
            final IGenericParameterProvider owner2 = gp2.getOwner();

            if (owner1.getGenericParameters().indexOf(gp1) != owner1.getGenericParameters().indexOf(gp2)) {
                return false;
            }

            if (owner1 == owner2) {
                return true;
            }

            if (owner1 instanceof TypeReference) {
                return owner2 instanceof TypeReference &&
                       StringUtilities.equals(
                           ((TypeReference) owner1).getInternalName(),
                           ((TypeReference) owner2).getInternalName()
                       );
            }

            return owner1 instanceof MethodReference &&
                   owner2 instanceof MethodReference &&
                   StringUtilities.equals(
                       ((MethodReference) owner1).getFullName(),
                       ((MethodReference) owner2).getFullName()
                   ) &&
                   StringUtilities.equals(
                       ((MethodReference) owner1).getErasedSignature(),
                       ((MethodReference) owner2).getErasedSignature()
                   );
        }

        @Override
        protected boolean containsTypes(final List<? extends TypeReference> t1, final List<? extends TypeReference> t2) {
            return containsTypeEquivalent(t1, t2);
        }
    }

    final static class StrictSameTypeVisitor extends SameTypeVisitor {
        @Override
        boolean areSameGenericParameters(final GenericParameter gp1, final GenericParameter gp2) {
            if (gp1 == gp2) {
                return true;
            }

            if (gp1 == null || gp2 == null) {
                return false;
            }

            if (!StringUtilities.equals(gp1.getName(), gp2.getName())) {
                return false;
            }

            final IGenericParameterProvider owner1 = gp1.getOwner();
            final IGenericParameterProvider owner2 = gp2.getOwner();

            if (owner1 == null || owner2 == null) {
                if (owner1 != owner2) {
                    return false;
                }
            }
            else if (indexOfByIdentity(owner1.getGenericParameters(), gp1) != indexOfByIdentity(owner2.getGenericParameters(), gp2)) {
                return false;
            }

            if (owner1 == owner2) {
                return true;
            }

            if (owner1 instanceof TypeReference) {
                return owner2 instanceof TypeReference &&
                       StringUtilities.equals(gp1.getName(), gp2.getName()) &&
                       StringUtilities.equals(
                           ((TypeReference) owner1).getInternalName(),
                           ((TypeReference) owner2).getInternalName()
                       );
            }

            return owner1 instanceof MethodReference &&
                   owner2 instanceof MethodReference &&
                   StringUtilities.equals(gp1.getName(), gp2.getName()) &&
                   StringUtilities.equals(
                       ((MethodReference) owner1).getFullName(),
                       ((MethodReference) owner2).getFullName()
                   ) &&
                   StringUtilities.equals(
                       ((MethodReference) owner1).getErasedSignature(),
                       ((MethodReference) owner2).getErasedSignature()
                   );
        }

        @Override
        protected boolean containsTypes(final List<? extends TypeReference> t1, final List<? extends TypeReference> t2) {
            return areSameTypes(t1, t2, true);
        }

        @Override
        public Boolean visitWildcard(final WildcardType t, final TypeReference s) {
            if (s.isWildcardType()) {
                if (t.isUnbounded()) {
                    return s.isUnbounded();
                }

                if (t.hasExtendsBound()) {
                    return s.hasExtendsBound() &&
                           isSameType(t.getExtendsBound(), s.getExtendsBound());
                }

                return s.hasSuperBound() &&
                       isSameType(t.getSuperBound(), s.getSuperBound());
            }

            return false;
        }
    }

    private final static DefaultTypeVisitor<Void, List<TypeReference>> INTERFACES_VISITOR =
        new DefaultTypeVisitor<Void, List<TypeReference>>() {
            @Override
            public List<TypeReference> visitClassType(final TypeReference t, final Void ignored) {
                final TypeDefinition r = t.resolve();

                if (r == null) {
                    return Collections.emptyList();
                }

                final List<TypeReference> interfaces = r.getExplicitInterfaces();

                if (r.isGenericDefinition()) {
                    if (t.isGenericDefinition()) {
                        return interfaces;
                    }

                    if (isRawType(t)) {
                        return eraseRecursive(interfaces);
                    }

                    final List<? extends TypeReference> formal = getTypeArguments(r);
                    final List<? extends TypeReference> actual = getTypeArguments(t);

                    final ArrayList<TypeReference> result = new ArrayList<>();
                    final Map<TypeReference, TypeReference> mappings = new HashMap<>();

                    for (int i = 0, n = formal.size(); i < n; i++) {
                        mappings.put(formal.get(i), actual.get(i));
                    }

                    for (int i = 0, n = interfaces.size(); i < n; i++) {
                        result.add(substituteGenericArguments(interfaces.get(i), mappings));
                    }

                    return result;
                }

                return interfaces;
            }

            @Override
            public List<TypeReference> visitWildcard(final WildcardType t, final Void ignored) {
                if (t.hasExtendsBound()) {
                    final TypeReference bound = t.getExtendsBound();
                    final TypeDefinition resolvedBound = bound.resolve();

                    if (resolvedBound != null) {
                        if (resolvedBound.isInterface()) {
                            return Collections.singletonList(bound);
                        }
                        if (resolvedBound.isCompoundType()) {
                            visit(bound, null);
                        }
                    }

                    return visit(bound, null);
                }

                return Collections.emptyList();
            }

            @Override
            public List<TypeReference> visitGenericParameter(final GenericParameter t, final Void ignored) {
                if (t.hasExtendsBound()) {
                    final TypeReference bound = t.getExtendsBound();
                    final TypeDefinition resolvedBound = bound.resolve();

                    if (resolvedBound != null) {
                        if (resolvedBound.isInterface()) {
                            return Collections.singletonList(bound);
                        }
                        if (resolvedBound.isCompoundType()) {
                            visit(bound, null);
                        }
                    }

                    return visit(bound, null);
                }

                return Collections.emptyList();
            }
        };

    private final static TypeMapper<TypeReference> AS_SUBTYPE_VISITOR = new TypeMapper<TypeReference>() {
        @Override
        public TypeReference visitClassType(final TypeReference t, final TypeReference s) {
            if (isSameType(t, s)) {
                return t;
            }

            final TypeReference base = asSuper(t, s);

            if (base == null) {
                return null;
            }

            Map<TypeReference, TypeReference> mappings;

            try {
                mappings = adapt(base, t);
            }
            catch (final AdaptFailure ignored) {
                mappings = getGenericSubTypeMappings(t, base);
            }

            final TypeReference result = substituteGenericArguments(s, mappings);

            if (!isSubType(result, t)) {
                return null;
            }

            final List<? extends TypeReference> tTypeArguments = getTypeArguments(t);
            final List<? extends TypeReference> sTypeArguments = getTypeArguments(s);
            final List<? extends TypeReference> resultTypeArguments = getTypeArguments(result);

            List<TypeReference> openGenericParameters = null;

            for (final TypeReference a : sTypeArguments) {
                if (a.isGenericParameter() &&
                    indexOfByIdentity(resultTypeArguments, a) >= 0 &&
                    indexOfByIdentity(tTypeArguments, a) < 0) {

                    if (openGenericParameters == null) {
                        openGenericParameters = new ArrayList<>();
                    }

                    openGenericParameters.add(a);
                }
            }

            if (openGenericParameters != null) {
                if (isRawType(t)) {
                    //
                    // The subtype of a raw type is raw.
                    //
                    return eraseRecursive(result);
                }
                else {
                    final Map<TypeReference, TypeReference> unboundMappings = new HashMap<>();

                    for (final TypeReference p : openGenericParameters) {
                        unboundMappings.put(p, WildcardType.unbounded());
                    }

                    return substituteGenericArguments(result, unboundMappings);
                }
            }

            return result;
        }
    };

    private final static DefaultTypeVisitor<Boolean, TypeReference> ERASE_VISITOR = new DefaultTypeVisitor<Boolean, TypeReference>() {
        @Override
        public TypeReference visitArrayType(final ArrayType t, final Boolean recurse) {
            final TypeReference elementType = getElementType(t);
            final TypeReference erasedElementType = erase(getElementType(t), recurse);

            return erasedElementType == elementType ? t : erasedElementType.makeArrayType();
        }

        @Override
        public TypeReference visitBottomType(final TypeReference t, final Boolean recurse) {
            return t;
        }

        @Override
        public TypeReference visitClassType(final TypeReference t, final Boolean recurse) {
            if (t.isGenericType()) {
                return new RawType(t);
            }
            else {
                final TypeDefinition resolved = t.resolve();

                if (resolved != null && resolved.isGenericDefinition()) {
                    return new RawType(resolved);
                }
            }
            return t;
        }

        @Override
        public TypeReference visitCompoundType(final CompoundTypeReference t, final Boolean recurse) {
            final TypeReference baseType = t.getBaseType();
            return erase(baseType != null ? baseType : first(t.getInterfaces()), recurse);
        }

        @Override
        public TypeReference visitGenericParameter(final GenericParameter t, final Boolean recurse) {
            return erase(getUpperBound(t), recurse);
        }

        @Override
        public TypeReference visitNullType(final TypeReference t, final Boolean recurse) {
            return t;
        }

        @Override
        public TypeReference visitPrimitiveType(final PrimitiveType t, final Boolean recurse) {
            return t;
        }

        @Override
        public TypeReference visitRawType(final RawType t, final Boolean recurse) {
            return t;
        }

        @Override
        public TypeReference visitType(final TypeReference t, final Boolean recurse) {
            if (t.isGenericType()) {
                return new RawType(t);
            }
            return t;
        }

        @Override
        public TypeReference visitWildcard(final WildcardType t, final Boolean recurse) {
            return erase(getUpperBound(t), recurse);
        }
    };

    private static final DefaultTypeVisitor<Void, Boolean> IS_DECLARED_TYPE = new DefaultTypeVisitor<Void, Boolean>() {
        @Override
        public Boolean visitWildcard(final WildcardType t, final Void ignored) {
            return false;
        }

        @Override
        public Boolean visitArrayType(final ArrayType t, final Void ignored) {
            return false;
        }

        @Override
        public Boolean visitBottomType(final TypeReference t, final Void ignored) {
            return false;
        }

        @Override
        public Boolean visitCapturedType(final CapturedType t, final Void ignored) {
            return false;
        }

        @Override
        public Boolean visitClassType(final TypeReference t, final Void ignored) {
            return true;
        }

        @Override
        public Boolean visitCompoundType(final CompoundTypeReference t, final Void ignored) {
            return false;
        }

        @Override
        public Boolean visitGenericParameter(final GenericParameter t, final Void ignored) {
            return false;
        }

        @Override
        public Boolean visitNullType(final TypeReference t, final Void ignored) {
            return false;
        }

        @Override
        public Boolean visitParameterizedType(final TypeReference t, final Void ignored) {
            return true;
        }

        @Override
        public Boolean visitPrimitiveType(final PrimitiveType t, final Void ignored) {
            return false;
        }

        @Override
        public Boolean visitRawType(final RawType t, final Void ignored) {
            return true;
        }

        @Override
        public Boolean visitType(final TypeReference t, final Void ignored) {
            return false;
        }
    };

    // </editor-fold>
}
