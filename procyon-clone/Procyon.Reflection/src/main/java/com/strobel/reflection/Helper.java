/*
 * Helper.java
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

package com.strobel.reflection;

import com.strobel.collections.ImmutableList;
import com.strobel.collections.ListBuffer;
import com.strobel.core.Comparer;
import com.strobel.core.HashUtilities;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.reflection.emit.TypeBuilder;
import com.strobel.util.TypeUtils;

import javax.lang.model.type.TypeKind;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.strobel.collections.ListBuffer.lb;
import static com.strobel.util.TypeUtils.*;

/**
 * @author Mike Strobel
 */
@SuppressWarnings({ "unchecked" })
final class Helper {

    private Helper() {}

    public static boolean overrides(final MethodInfo baseMethod, final MethodInfo ancestorMethod) {
        if (ancestorMethod.isFinal() || ancestorMethod.isPrivate()) {
            return false;
        }

        final int baseModifier = baseMethod.getModifiers() & Flags.AccessFlags;
        final int ancestorModifier = ancestorMethod.getModifiers() & Flags.AccessFlags;

        if (baseModifier != ancestorModifier) {
            return false;
        }

        final Method rawMethod = baseMethod.getRawMethod();

        if (rawMethod != null && rawMethod == ancestorMethod.getRawMethod()) {
            return true;
        }

        final ParameterList baseParameters = baseMethod.getParameters();
        final ParameterList ancestorParameters = ancestorMethod.getParameters();

        if (baseParameters.size() != ancestorParameters.size()) {
            return false;
        }

        if (!StringUtilities.equals(baseMethod.getName(), ancestorMethod.getName())) {
            return false;
        }

        final Type baseDeclaringType = erasure(baseMethod.getDeclaringType());
        final Type ancestorDeclaringType = erasure(ancestorMethod.getDeclaringType());

        if (!isSubtype(baseDeclaringType, ancestorDeclaringType)) {
            return false;
        }

        final Type ancestorReturnType = erasure(ancestorMethod.getReturnType());
        final Type baseReturnType = erasure(baseMethod.getReturnType());

        if (!ancestorReturnType.isAssignableFrom(baseReturnType)) {
            return false;
        }

        final TypeList erasedBaseParameters = erasure(baseParameters.getParameterTypes());
        final TypeList erasedAncestorParameters = erasure(ancestorParameters.getParameterTypes());

        for (int i = 0, n = ancestorParameters.size(); i < n; i++) {
            final Type<?> baseParameterType = erasedBaseParameters.get(i);
            final Type<?> ancestorParameterType = erasedAncestorParameters.get(i);

            if (!TypeUtils.areEquivalent(baseParameterType, ancestorParameterType)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isOverridableIn(final MethodInfo method, final Type origin) {
        VerifyArgument.notNull(method, "method");
        VerifyArgument.notNull(origin, "origin");

        switch (method.getModifiers() & Flags.AccessFlags) {
            case 0:
                // for package private: can only override in the same package.
                return method.getDeclaringType().getPackage() == origin.getPackage() &&
                       !origin.isInterface();
            case Flags.PRIVATE:
                return false;
            case Flags.PUBLIC:
                return true;
            case Flags.PROTECTED:
                return !origin.isInterface();
            default:
                return false;
        }
    }

    public static boolean overrides(final MethodBase method, final MethodBase other, final boolean checkResult) {
        return method instanceof MethodInfo &&
               other instanceof MethodInfo &&
               overrides((MethodInfo)method, (MethodInfo)other, checkResult);
    }

    public static boolean overrides(final MethodInfo method, final MethodInfo other, final boolean checkResult) {
        if (method == other) {
            return true;
        }

        if (!isOverridableIn(other, method.getDeclaringType())) {
            return false;
        }

        // Check for a direct implementation
        if (asSuper(method.getDeclaringType(), other.getDeclaringType()) != null) {
            if (isSubSignature(method, other)) {
                if (!checkResult) {
                    return true;
                }
                if (returnTypeSubstitutable(method, other)) {
                    return true;
                }
            }
        }

        // Check for an inherited implementation

        //noinspection SimplifiableIfStatement
        if (method.isAbstract() || !other.isAbstract()) {
            return false;
        }

        return isSubSignature(method, other) &&
               (!checkResult || resultSubtype(method, other));
    }

    public static boolean resultSubtype(final MethodInfo t, final MethodInfo s) {
        final TypeList tVars = t.getTypeArguments();
        final TypeList sVars = s.getTypeArguments();
        final Type tReturn = t.getReturnType();
        final Type sReturn = substitute(s.getReturnType(), sVars, tVars);
        return covariantReturnType(tReturn, sReturn);
    }

    public static boolean covariantReturnType(final Type t, final Type s) {
        return isSameType(t, s) ||
               !t.isPrimitive() &&
               !s.isPrimitive() &&
               isAssignable(t, s);
    }

    public static boolean isAssignable(final Type sourceType, final Type targetType) {
        if (VerifyArgument.notNull(sourceType, "sourceType") ==
            VerifyArgument.notNull(targetType, "targetType")) {

            return true;
        }

        if (targetType.isGenericParameter() || targetType.hasExtendsBound()) {
            return isAssignable(sourceType, targetType.getExtendsBound());
        }

        if (sourceType instanceof TypeBuilder) {
            return isAssignable(sourceType.getBaseType(), targetType);
        }

        if (targetType instanceof TypeBuilder) {
            final TypeBuilder targetTypeBuilder = (TypeBuilder) targetType;

            return targetTypeBuilder.isCreated() &&
                   isAssignable(sourceType, targetTypeBuilder.createType());
        }

        return isConvertible(sourceType, targetType);
    }

    public static boolean isConvertible(final Type sourceType, final Type targetType) {
        final boolean tPrimitive = sourceType.isPrimitive();
        final boolean sPrimitive = targetType.isPrimitive();

        if (sourceType == Type.NullType) {
            return !targetType.isPrimitive();
        }

        if (targetType == Types.Object) {
            return true;
        }

        if (targetType.isGenericParameter()) {
            return isConvertible(sourceType, targetType.getExtendsBound());
        }

        if (tPrimitive == sPrimitive) {
            return isSubtypeUnchecked(sourceType, targetType);
        }

        return tPrimitive
               ? isSubtype(TypeUtils.getBoxedTypeOrSelf(sourceType), targetType)
               : isSubtype(TypeUtils.getUnderlyingPrimitiveOrSelf(sourceType), targetType);
    }

    public static boolean isSubtypeUnchecked(final Type t, final Type s) {
        if (t.isArray() && s.isArray()) {
            if (t.getElementType().isPrimitive()) {
                return isSameType(elementType(t), elementType(s));
            }
            return isSubtypeUnchecked(elementType(t), elementType(s));
        }
        else if (isSubtype(t, s)) {
            return true;
        }
        else if (t.isGenericParameter()) {
            return isSubtypeUnchecked(t.getExtendsBound(), s);
        }
        else if (s.isGenericParameter()) {
            return isSubtypeUnchecked(t, s.getExtendsBound());
        }
        else if (s.isGenericType() && !s.isGenericTypeDefinition()) {
            final Type t2 = asSuper(t, s);
            if (t2 != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean returnTypeSubstitutable(final MethodInfo r1, final MethodInfo r2) {
        if (hasSameArgs(r1, r2)) {
            return resultSubtype(r1, r2);
        }

        return covariantReturnType(
            r1.getReturnType(),
            erasure(r2.getReturnType())
        );
    }

    public static boolean isSubSignature(final MethodInfo t, final MethodInfo p) {
        return hasSameArgs(t, p) ||
               containsTypeEquivalent(t.getParameters().getParameterTypes(), erasure(p.getParameters().getParameterTypes()));
    }

    public static boolean hasSameArgs(final MethodInfo t, final MethodInfo p) {
        return containsTypeEquivalent(
            t.getParameters().getParameterTypes(),
            p.getParameters().getParameterTypes()
        );
    }

    public static boolean hasSameArgs(final TypeList t, final TypeList p) {
        return containsTypeEquivalent(t, p);
    }

    public static Type asSuper(final Type type, final Type other) {
        return AsSuperVisitor.visit(type, other);
    }

    public static boolean isSuperType(final Type type, final Type other) {
        if (type == other || other == Type.Bottom) {
            return true;
        }
        if (type.isGenericParameter()) {
            return isSuperType(type.getExtendsBound(), other);
        }
        return isSubtype(other, type);
    }

    public static boolean isSubtype(final Type t, final Type p) {
        return isSubtype(t, p, true);
    }

    public static boolean isSubtypeNoCapture(final Type t, final Type p) {
        return isSubtype(t, p, false);
    }

    public static boolean isSubtype(final Type t, final Type p, final boolean capture) {
        if (t == p) {
            return true;
        }

        if (p == null) {
            return false;
        }

        if (p == Types.Object) {
            return true;
        }

        if (TypeUtils.areEquivalent(t, p)) {
            return true;
        }

        if (p.isCompoundType()) {
            final Type baseType = p.getBaseType();

            if (baseType != null && !isSubtype(t, baseType, capture)) {
                return false;
            }

            final TypeList interfaces = p.getExplicitInterfaces();

            for (int i = 0, n = interfaces.size(); i < n; i++) {
                final Type type = interfaces.get(i);
                if (!isSubtype(t, type, capture)) {
                    return false;
                }
            }

            return true;
        }

        final Type lower = lowerBound(p);

        if (p != lower) {
            return isSubtype(capture ? capture(t) : t, lower, false);
        }

        return IsSubtypeRelation.visit(capture ? capture(t) : t, p);
    }

/*
    private static ImmutableList<Type<?>> freshTypeVariables(final ImmutableList<Type<?>> types) {
        final ListBuffer<Type<?>> result = lb();
        for (final Type t : types) {
            if (t.isWildcardType()) {
                final Type bound = t.getUpperBound();
                result.append(new CapturedType(Type.Bottom, bound, Type.Bottom, t));
            }
            else {
                result.append(t);
            }
        }
        return result.toList();
    }
*/

    private static TypeList freshTypeVariables(final TypeList types) {
        final ListBuffer<Type<?>> result = lb();
        for (final Type t : types) {
            if (t.isWildcardType()) {
                final Type bound = t.getExtendsBound();
                result.append(new CapturedType(Type.Bottom, bound, Type.Bottom, t));
            }
            else {
                result.append(t);
            }
        }
        return new TypeList(result.toList());
    }

    public static Type capture(Type t) {
        if (t.isGenericParameter() || t.isWildcardType() || t.isPrimitive() || t.isArray() || t == Type.Bottom || t == Type.NullType) {
            return t;
        }

        final Type declaringType = t.getDeclaringType();

        if (declaringType != Type.Bottom && declaringType != null) {
            final Type capturedDeclaringType = capture(declaringType);

            if (capturedDeclaringType != declaringType) {
                final Type memberType = capturedDeclaringType.getNestedType(t.getFullName());
                if (memberType != null) {
                    t = substitute(memberType, memberType.getGenericTypeParameters(), t.getTypeArguments());
                }
            }
        }

        if (!t.isGenericType()) {
            return t;
        }

        final Type G = t.getGenericTypeDefinition();
        final TypeList A = G.getTypeArguments();
        final TypeList T = t.getTypeArguments();
        final TypeList S = freshTypeVariables(T);

        ImmutableList<Type<?>> currentA = ImmutableList.from(A.toArray());
        ImmutableList<Type<?>> currentT = ImmutableList.from(T.toArray());
        ImmutableList<Type<?>> currentS = ImmutableList.from(S.toArray());

        boolean captured = false;
        while (!currentA.isEmpty() &&
               !currentT.isEmpty() &&
               !currentS.isEmpty()) {

            if (currentS.head != currentT.head) {
                captured = true;

                final WildcardType Ti = (WildcardType)currentT.head;
                Type Ui = currentA.head.getExtendsBound();
                CapturedType Si = (CapturedType)currentS.head;

                if (Ui == null) {
                    Ui = Types.Object;
                }

                if (Ti.isUnbounded()) {
                    currentS.head = Si = new CapturedType(
                        Si.getDeclaringType(),
                        substitute(Ui, A, S),
                        Type.Bottom,
                        Si.getWildcard()
                    );
                }
                else if (Ti.hasExtendsBound()) {
                    currentS.head = Si = new CapturedType(
                        Si.getDeclaringType(),
                        glb(Ti.getExtendsBound(), substitute(Ui, A, S)),
                        Type.Bottom,
                        Si.getWildcard()
                    );
                }
                else {
                    currentS.head = Si = new CapturedType(
                        Si.getDeclaringType(),
                        substitute(Ui, A, S),
                        Ti.getSuperBound(),
                        Si.getWildcard()
                    );
                }

                if (Si.getExtendsBound() == Si.getSuperBound()) {
                    currentS.head = Si.getExtendsBound();
                }
            }

            currentA = currentA.tail;
            currentT = currentT.tail;
            currentS = currentS.tail;
        }

        if (!currentA.isEmpty() || !currentT.isEmpty() || !currentS.isEmpty()) {
            return erasure(t); // some "rare" type involved
        }

        if (captured) {
            return t.getGenericTypeDefinition().makeGenericType(S.toArray());
        }
        else {
            return t;
        }
    }

    static boolean containsType(ImmutableList<Type<?>> ts, ImmutableList<Type<?>> ss) {
        while (ts.nonEmpty() && ss.nonEmpty() && containsType(ts.head, ss.head)) {
            ts = ts.tail;
            ss = ss.tail;
        }
        return ts.isEmpty() && ss.isEmpty();
    }

    static boolean containsType(final TypeList ts, final TypeList ss) {
        if (ts.size() != ss.size()) {
            return false;
        }

        if (ts.isEmpty()) {
            return true;
        }

        for (int i = 0, n = ts.size(); i < n; i++) {
            if (!containsType(ts.get(i), ss.get(i))) {
                return false;
            }
        }

        return true;
    }

    static boolean containsType(final Type t, final Type p) {
        return ContainsTypeRelation.visit(t, p);
    }

    static boolean containsTypeEquivalent(ImmutableList<Type<?>> ts, ImmutableList<Type<?>> tp) {
        while (ts.nonEmpty() && tp.nonEmpty() && containsTypeEquivalent(ts.head, tp.head)) {
            ts = ts.tail;
            tp = tp.tail;
        }
        return ts.isEmpty() && tp.isEmpty();
    }

    static boolean containsTypeEquivalent(final TypeList ts, final TypeList tp) {
        if (ts.size() != tp.size()) {
            return false;
        }

        if (ts.isEmpty()) {
            return true;
        }

        for (int i = 0, n = ts.size(); i < n; i++) {
            if (!containsTypeEquivalent(ts.get(i), tp.get(i))) {
                return false;
            }
        }

        return true;
    }

    public static TypeList map(final TypeList ts, final TypeMapping f) {
        if (ts.isEmpty()) {
            return TypeList.empty();
        }

        Type[] results = null;

        for (int i = 0, n = ts.size(); i < n; i++) {
            final Type t = ts.get(i);
            final Type r = f.apply(t);

            if (r != t) {
                if (results == null) {
                    results = ts.toArray();
                }
                results[i] = r;
            }
        }

        if (results != null) {
            return new TypeList(results);
        }

        return ts;
    }

    private static boolean containsTypeEquivalent(final Type t, final Type p) {
        return isSameType(t, p) || // shortcut
               containsType(t, p) && containsType(p, t);
    }

    public static boolean areSameTypes(final TypeList ts, final TypeList tp) {
        if (ts.size() != tp.size()) {
            return false;
        }

        if (ts.isEmpty()) {
            return true;
        }

        for (int i = 0, n = ts.size(); i < n; i++) {
            if (!isSameType(ts.get(i), tp.get(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean isSameType(final Type t, final Type p) {
        return IsSameTypeRelation.visit(t, p);
    }

    public static boolean isCaptureOf(final Type p, final Type t) {
        return p.isGenericParameter() &&
               p instanceof ICapturedType &&
               isSameWildcard(t, ((ICapturedType)p).getWildcard());
    }

    public static boolean isSameWildcard(final Type t, final Type p) {
        if (!p.isWildcardType() || !t.isWildcardType()) {
            return false;
        }

        if (p.isUnbounded()) {
            return t.isUnbounded();
        }

        if (p.hasSuperBound()) {
            return t.hasSuperBound() &&
                   isSameType(p.getSuperBound(), t.getSuperBound());
        }

        return p.hasExtendsBound() &&
               t.hasExtendsBound() &&
               isSameType(p.getExtendsBound(), t.getExtendsBound());
    }

    public static Type glb(final Type t, final Type p) {
        if (p == null) {
            return t;
        }
        else if (t.isPrimitive() || p.isPrimitive()) {
            return null;
        }
        else if (isSubtypeNoCapture(t, p)) {
            return t;
        }
        else if (isSubtypeNoCapture(p, t)) {
            return p;
        }

        final ImmutableList<Type<?>> closure = union(closure(t), closure(p));
        final ImmutableList<Type<?>> bounds = closureMin(closure);

        if (bounds.isEmpty()) {             // length == 0
            return Types.Object;
        }
        else if (bounds.tail.isEmpty()) { // length == 1
            return bounds.head;
        }
        else {                            // length > 1
            int classCount = 0;
            for (final Type bound : bounds) {
                if (!bound.isInterface()) {
                    classCount++;
                }
            }
            if (classCount > 1) {
                throw new AssertionError();
            }
        }

        Type baseClass = Types.Object;
        ImmutableList<Type<?>> interfaces = ImmutableList.empty();

        for (final Type bound : bounds) {
            if (bound.isInterface()) {
                interfaces = interfaces.append(bound);
            }
            else {
                baseClass = bound;
            }
        }

        return Type.makeCompoundType(
            baseClass,
            Type.list(interfaces)
        );
    }

    public static Type elementType(final Type t) {
        if (t.isArray()) {
            return t.getElementType();
        }
        if (t.isWildcardType()) {
            return elementType(upperBound(t));
        }
        return null;
    }

    public static Type<?> upperBound(final Type<?> t) {
        return UpperBoundVisitor.visit(t);
    }

    public static Type lowerBound(final Type t) {
        return LowerBoundVisitor.visit(t);
    }

    public static TypeList erasure(final TypeList ts) {
        return map(ts, ErasureFunctor);
    }

    public static Type erasureRecursive(final Type t) {
        return erasure(t, true);
    }

    public static TypeList erasureRecursive(final TypeList ts) {
        return map(ts, ErasureRecursiveFunctor);
    }

    public static Type erasure(final Type t) {
        return erasure(t, false);
    }

    public static Type substitute(final Type type, final ImmutableList<Type<?>> genericParameters, final ImmutableList<Type<?>> typeArguments) {
        return SubstitutingBinder.visit(
            type,
            TypeBindings.create(
                Type.list(genericParameters),
                Type.list(typeArguments)
            )
        );
    }

    public static Type substitute(final Type type, final TypeList genericParameters, final TypeList typeArguments) {
        return SubstitutingBinder.visit(
            type,
            TypeBindings.create(genericParameters, typeArguments)
        );
    }

    public static Type substitute(final Type type, final TypeBindings bindings) {
        return SubstitutingBinder.visit(type, bindings);
    }

    private static Type erasure(final Type t, final boolean recurse) {
        if (t.isPrimitive()) {
            return t;  // fast special case
        }
        else {
            return ErasureVisitor.visit(t, recurse);
        }
    }

    public static ImmutableList<Type<?>> interfaces(final Type type) {
        return InterfacesVisitor.visit(type, ImmutableList.<Type<?>>empty());
    }

    public static int rank(final Type t) {
        if (t == null) {
            return 0;
        }

        if (t.isPrimitive() || t.isWildcardType() || t.isArray() || t == Type.Bottom || t == Type.NullType) {
            throw new AssertionError();
        }

        if (t == Types.Object) {
            return 0;
        }

        int r = rank(superType(t));

        for (ImmutableList<Type<?>> l = interfaces(t);
             l.nonEmpty();
             l = l.tail) {

            final int headRank = rank(l.head);

            if (headRank > r) {
                r = headRank;
            }
        }

        return r + 1;
    }

    public static boolean precedes(final Type origin, final Type other) {
        if (origin == other) {
            return false;
        }

        if (origin.isGenericParameter() && other.isGenericParameter()) {
            return isSubtype(origin, other);
        }

        final boolean originIsClass = !origin.isWildcardType() &&
                                      !origin.isPrimitive() &&
                                      !origin.isArray() &&
                                      origin != Type.Bottom &&
                                      origin != Type.NullType;

        final boolean otherIsClass = !other.isWildcardType() &&
                                     !other.isPrimitive() &&
                                     !other.isArray() &&
                                     other != Type.Bottom &&
                                     other != Type.NullType;

        if (originIsClass && otherIsClass) {
            return rank(other) < rank(origin) ||
                   rank(other) == rank(origin) &&
                   other.getFullName().compareTo(origin.getFullName()) < 0;
        }

        return origin.isGenericParameter();
    }

    public static ImmutableList<Type<?>> union(final ImmutableList<Type<?>> cl1, final ImmutableList<Type<?>> cl2) {
        if (cl1.isEmpty()) {
            return cl2;
        }
        else if (cl2.isEmpty()) {
            return cl1;
        }
        else if (precedes(cl1.head, cl2.head)) {
            return union(cl1.tail, cl2).prepend(cl1.head);
        }
        else if (precedes(cl2.head, cl1.head)) {
            return union(cl1, cl2.tail).prepend(cl2.head);
        }
        else {
            return union(cl1.tail, cl2.tail).prepend(cl1.head);
        }
    }

    public static boolean isInheritedIn(final Type<?> site, final MemberInfo member) {
        if (site == null || site == Type.NullType) {
            return false;
        }

        if (member.isPublic()) {
            return true;
        }

        final Type declaringType = member.getDeclaringType();

        if (member.isPrivate()) {
            return TypeUtils.areEquivalent(site, declaringType);
        }

        if (member.isProtected()) {
            return !site.isInterface();
        }

        for (Type t = site;
             t != null && t != declaringType;
             t = superType(t)) {

            while (t != null && t.isGenericParameter()) {
                t = t.getExtendsBound();
            }

            if (t == null) {
                return true; // error recovery
            }

            if (t.isCompoundType()) {
                continue;
            }

            if (!inSamePackage(t, declaringType)) {
                return false;
            }
        }

        return !site.isInterface();
    }

    public static boolean inSamePackage(final Type t1, final Type t2) {
        if (t1 == t2) {
            return true;
        }

        final String name1 = t1.getFullName();
        final String name2 = t2.getFullName();

        if (name1 == null || name2 == null) {
            return false;
        }

        final int packageEnd1 = name1.lastIndexOf('.');
        final int packageEnd2 = name2.lastIndexOf('.');

        return packageEnd1 == packageEnd2 &&
               (packageEnd1 < 0 || StringUtilities.substringEquals(name1, 0, name2, 0, packageEnd2));
    }

    private final static TypeMapping ErasureFunctor = new TypeMapping("erasure") {
        public Type apply(final Type t) { return erasure(t); }
    };

    private final static TypeMapping ErasureRecursiveFunctor = new TypeMapping("erasureRecursive") {
        public Type apply(final Type t) { return erasureRecursive(t); }
    };

    private final static TypeBinder SubstitutingBinder = new TypeBinder();

    private final static TypeVisitor<Type, Type> AsSuperVisitor = new SimpleVisitor<Type, Type>() {
        @Override
        public Type visitClassType(final Type t, final Type p) {
            if (t == p) {
                return t;
            }

            if (t == null) {
                return null;
            }

            if (t.isRawType() && p.isGenericType() && t.isAssignableFrom(p.getErasedType())) {
                return t;
            }

            if (t.isGenericType()) {
                if (p.isGenericType()) {
                    if (t.getGenericTypeDefinition() == p.getGenericTypeDefinition()) {
                        boolean areTypeArgumentsAssignable = true;

                        final TypeList ta = t.getTypeArguments();
                        final TypeList tp = p.getTypeArguments();

                        for (int i = 0, n = ta.size(); i < n; i++) {
                            final Type<?> at = ta.get(i);
                            final Type<?> ap = tp.get(i);

                            if (ap == at) {
                                continue;
                            }

                            if (ap.hasExtendsBound()) {
                                final Type<?> extendsBound = ap.getExtendsBound();
                                if (extendsBound == p || extendsBound.isAssignableFrom(at)) {
                                    continue;
                                }
                            }

                            if (ap.hasSuperBound() && isSuperType(at, ap.getSuperBound())) {
                                continue;
                            }

                            areTypeArgumentsAssignable = false;
                            break;
                        }

                        if (areTypeArgumentsAssignable) {
                            return t;
                        }
                    }
                }
                else if (p instanceof ErasedType<?> && t.getErasedType() == p) {
                    return t;
                }
            }

            if (TypeUtils.areEquivalent(t, p)) {
                return t;
            }

            final Type superType = superType(t);

            if (superType != null && !superType.isInterface()) {
                final Type ancestor = asSuper(superType, p);
                if (ancestor != null) {
                    return ancestor;
                }
            }

            final TypeList interfaces = t.getExplicitInterfaces();

            for (int i = 0, n = interfaces.size(); i < n; i++) {
                final Type ancestor = asSuper(interfaces.get(i), p);
                if (ancestor != null) {
                    return ancestor;
                }
            }

/*
            if (t == Types.Object && !p.isPrimitive() && !p.isInterface()) {
                return t;
            }
*/

            return null;
        }

        @Override
        public Type visitPrimitiveType(final Type t, final Type p) {
            if (t == p) {
                return t;
            }
            return null;
        }

        @Override
        public Type visitTypeParameter(final Type t, final Type p) {
            if (t == p) {
                return t;
            }
            return asSuper(t.getExtendsBound(), p);
        }

        @Override
        public Type visitArrayType(final Type t, final Type p) {
            return isSubtype(t, p) ? p : null;
        }

        @Override
        public Type visitType(final Type t, final Type p) {
            return super.visitType(t, p);
        }
    };

    private final static TypeRelation IsSameTypeRelation = new TypeRelation() {
        @Override
        public Boolean visitCapturedType(final Type t, final Type p) {
            return super.visitCapturedType(t, p);
        }

        @Override
        public Boolean visitClassType(final Type type, final Type parameter) {
            return super.visitClassType(type, parameter);
        }

        @Override
        public Boolean visitPrimitiveType(final Type type, final Type parameter) {
            return type == parameter ? Boolean.TRUE : Boolean.FALSE;
        }

        @Override
        public Boolean visitTypeParameter(final Type type, final Type parameter) {
            return StringUtilities.equals(type.getFullName(), parameter.getFullName()) &&
                   Comparer.equals(type.getDeclaringType(), parameter.getDeclaringType()) &&
                   Comparer.equals(type.getDeclaringMethod(), parameter.getDeclaringMethod()) &&
                   visit(type.getExtendsBound(), parameter.getExtendsBound());
        }

        @Override
        public Boolean visitWildcardType(final Type type, final Type parameter) {
            return parameter.hasSuperBound() &&
                   !parameter.hasExtendsBound() &&
                   visit(type, upperBound(parameter));
        }

        @Override
        public Boolean visitArrayType(final Type type, final Type parameter) {
            return super.visitArrayType(type, parameter);
        }

        @Override
        public Boolean visitType(final Type type, final Type parameter) {
            return type == parameter;
        }
    };

    private final static TypeMapper<Void> UpperBoundVisitor = new TypeMapper<Void>() {
        @Override
        public Type visitWildcardType(final Type t, final Void ignored) {
            if (t.hasSuperBound()) {
                final Type lowerBound = t.getSuperBound();

                if (lowerBound.hasExtendsBound()) {
                    return visit(lowerBound.getExtendsBound());
                }

                return Types.Object;
            }
            else {
                return visit(t.getExtendsBound());
            }
        }

        @Override
        public Type visitCapturedType(final Type t, final Void ignored) {
            return visit(t.getExtendsBound());
        }
    };

    private final static TypeMapper<Void> LowerBoundVisitor = new TypeMapper<Void>() {
        @Override
        public Type visitWildcardType(final Type t, final Void ignored) {
            return t.hasExtendsBound() ? Type.Bottom : visit(t.getSuperBound());
        }

        @Override
        public Type visitCapturedType(final Type t, final Void ignored) {
            return visit(t.getSuperBound());
        }
    };

    private final static TypeMapper<Boolean> ErasureVisitor = new TypeMapper<Boolean>() {
        public Type visitType(final Type t, final Boolean recurse) {
            if (t.isPrimitive()) {
                return t;  // fast special case
            }
            else {
                return (recurse ? ErasureRecursiveFunctor : ErasureFunctor).apply(t);
            }
        }

        @Override
        public Type visitWildcardType(final Type t, final Boolean recurse) {
            return erasure(upperBound(t), recurse);
        }

        @Override
        public Type<?> visitClassType(final Type<?> t, final Boolean recurse) {
            return Type.of(t.getErasedClass());
        }

        @Override
        public Type visitTypeParameter(final Type t, final Boolean recurse) {
            return erasure(t.getExtendsBound(), recurse);
        }

        @Override
        public Type<?> visitArrayType(final Type<?> type, final Boolean recurse) {
            return erasure(type.getElementType(), recurse).makeArrayType();
        }
    };

    private final static TypeRelation ContainsTypeRelation = new TypeRelation() {

        private Type U(Type t) {
            while (t.isWildcardType()) {
                if (t.hasSuperBound()) {
                    final Type lowerBound = t.getSuperBound();
                    if (lowerBound.hasExtendsBound()) {
                        return lowerBound.getExtendsBound();
                    }
                    return Types.Object;
                }
                t = t.getExtendsBound();
            }
            return t;
        }

        private Type L(Type t) {
            while (t.isWildcardType()) {
                if (t.hasExtendsBound()) {
                    return Type.Bottom;
                }
                else {
                    t = t.getSuperBound();
                }
            }
            return t;
        }

        public Boolean visitType(final Type t, final Type p) {
            return isSameType(t, p);
        }

        @Override
        public Boolean visitWildcardType(final Type t, final Type p) {
            return isSameWildcard(t, p) ||
                   isCaptureOf(p, t) ||
                   ((t.hasExtendsBound() || isSubtypeNoCapture(L(t), lowerBound(p))) &&
                    (t.hasSuperBound() || isSubtypeNoCapture(upperBound(p), U(t))));
        }
    };

    private final static SimpleVisitor<ImmutableList<Type<?>>, ImmutableList<Type<?>>> InterfacesVisitor =
        new SimpleVisitor<ImmutableList<Type<?>>, ImmutableList<Type<?>>>() {
            @Override
            public ImmutableList<Type<?>> visitPrimitiveType(final Type<?> type, final ImmutableList<Type<?>> parameter) {
                return ImmutableList.empty();
            }

            @Override
            public ImmutableList<Type<?>> visitArrayType(final Type<?> type, final ImmutableList<Type<?>> parameter) {
                return ImmutableList.empty();
            }

            @Override
            public ImmutableList<Type<?>> visitCapturedType(final Type<?> t, final ImmutableList<Type<?>> s) {
                return ImmutableList.empty();
            }

            @Override
            public ImmutableList<Type<?>> visit(final Type<?> type) {
                return ImmutableList.empty();
            }

            @Override
            public ImmutableList<Type<?>> visitType(final Type<?> t, final ImmutableList<Type<?>> ignored) {
                return ImmutableList.empty();
            }

            @Override
            public ImmutableList<Type<?>> visitClassType(final Type<?> t, final ImmutableList<Type<?>> list) {
                final TypeList interfaces = t.getExplicitInterfaces();

                if (interfaces.isEmpty()) {
                    return ImmutableList.empty();
                }

                ImmutableList<Type<?>> result = union(list, ImmutableList.from(t.getExplicitInterfaces().toArray()));

                for (final Type ifType : interfaces) {
                    if (!list.contains(ifType)) {
                        result = union(result, visit(ifType, result));
                    }
                }

                return result;
            }

            @Override
            public ImmutableList<Type<?>> visitTypeParameter(final Type<?> t, final ImmutableList<Type<?>> list) {
                final Type upperBound = t.getExtendsBound();

                if (upperBound.isCompoundType()) {
                    return interfaces(upperBound);
                }

                if (upperBound.isInterface()) {
                    return ImmutableList.<Type<?>>of(upperBound);
                }

                return ImmutableList.empty();
            }

            @Override
            public ImmutableList<Type<?>> visitWildcardType(final Type<?> type, final ImmutableList<Type<?>> list) {
                return visit(type.getExtendsBound());
            }
        };

    public static Type superType(final Type t) {
        return SuperTypeVisitor.visit(t);
    }

    private final static UnaryTypeVisitor<Type> SuperTypeVisitor = new UnaryTypeVisitor<Type>() {

        public Type visitType(final Type t, final Void ignored) {
            // A note on wildcards: there is no good way to
            // determine a super type for a super-bounded wildcard.
            return null;
        }

        @Override
        public Type visitClassType(final Type t, final Void ignored) {
            return t.getBaseType();
        }

        @Override
        public Type visitTypeParameter(final Type t, final Void ignored) {
            final Type bound = t.getExtendsBound();

            if (!bound.isCompoundType() && !bound.isInterface()) {
                return bound;
            }

            return superType(bound);
        }

        @Override
        public Type visitArrayType(final Type t, final Void ignored) {
            final Type elementType = t.getElementType();

            if (elementType.isPrimitive() || isSameType(elementType, Types.Object)) {
                return arraySuperType();
            }
            else {
                return new ArrayType(superType(elementType));
            }
        }
    };

    private final static TypeRelation IsSubtypeRelation = new TypeRelation() {
        @Override
        public Boolean visitPrimitiveType(final Type t, final Type p) {
            final TypeKind kt = t.getKind();
            final TypeKind kp = p.getKind();

            if (kt == kp) {
                return true;
            }

            if (kt == TypeKind.BOOLEAN || kp == TypeKind.BOOLEAN) {
                return false;
            }

            switch (kp) {
                case BYTE:
                    return kt != TypeKind.CHAR && isIntegral(kt) && bitWidth(kt) <= bitWidth(kp);

                case SHORT:
                    if (kt == TypeKind.CHAR) {
                        return false;
                    }
                    // fall through
                case INT:
                case LONG:
                    return isIntegral(kt) && bitWidth(kt) <= bitWidth(kp);

                case FLOAT:
                case DOUBLE:
                    return isIntegral(kt) || bitWidth(kt) <= bitWidth(kp);

                default:
                    return Boolean.FALSE;
            }
        }

        public Boolean visitType(final Type t, final Type s) {
            if (t.isGenericParameter()) {
                return isSubtypeNoCapture(t.getExtendsBound(), s);
            }
            return Boolean.FALSE;
        }

        private final Set<TypePair> cache = new HashSet<>();

        private boolean containsTypeRecursive(final Type t, final Type s) {
            final TypePair pair = new TypePair(t, s);
            if (cache.add(pair)) {
                try {
                    return containsType(
                        t.getTypeArguments(),
                        s.getTypeArguments()
                    );
                }
                finally {
                    cache.remove(pair);
                }
            }
            else {
                return containsType(
                    t.getTypeArguments(),
                    rewriteSupers(s).getTypeArguments()
                );
            }
        }

        private Type rewriteSupers(final Type t) {
            if (!t.isGenericType()) {
                return t;
            }

            final ListBuffer<Type<?>> from = lb();
            final ListBuffer<Type<?>> to = lb();

            adaptSelf(t, from, to);

            if (from.isEmpty()) {
                return t;
            }

            final ListBuffer<Type<?>> rewrite = lb();
            boolean changed = false;
            for (final Type orig : to.toList()) {
                Type<?> s = rewriteSupers(orig);
                if (s.hasSuperBound() && !s.hasExtendsBound()) {
                    s = new WildcardType<>(
                        Types.Object,
                        Type.Bottom
                    );
                    changed = true;
                }
                else if (s != orig) {
                    s = new WildcardType<>(
                        upperBound(s),
                        Type.Bottom
                    );
                    changed = true;
                }
                rewrite.append(s);
            }
            if (changed) {
                return substitute(t.getGenericTypeDefinition(), from.toList(), rewrite.toList());
            }
            else {
                return t;
            }
        }

        @Override
        public Boolean visitClassType(final Type t, final Type s) {
/*
            final Type asSuper = asSuper(t, s);
            if (asSuper == null || (asSuper != s && asSuper != Types.Object)
                // You're not allowed to write
                //     Vector<Object> vec = new Vector<String>();
                // But with wildcards you can write
                //     Vector<? extends Object> vec = new Vector<String>();
                // which means that subtype checking must be done
                // here instead of same-Type checking (via containsType).
                || (s.isGenericParameter() && !containsTypeRecursive(s, asSuper))) {
                return false;
            }

            final Type superDeclaringType = asSuper.getDeclaringType();
            final Type sDeclaringType = s.getDeclaringType();

            return superDeclaringType == null ||
                   sDeclaringType == null ||
                   isSubtypeNoCapture(superDeclaringType, sDeclaringType);
*/
            final Type asSuper = asSuper(t, s);
            return asSuper != null //&& asSuper == s
                   // You're not allowed to write
                   //     Vector<Object> vec = new Vector<String>();
                   // But with wildcards you can write
                   //     Vector<? extends Object> vec = new Vector<String>();
                   // which means that subtype checking must be done
                   // here instead of same-type checking (via containsType).
                   && (!s.isGenericParameter() && !s.isWildcardType() || containsTypeRecursive(s, asSuper))
                   && isSubtypeNoCapture(asSuper.getDeclaringType(), s.getDeclaringType());
        }

        @Override
        public Boolean visitArrayType(final Type t, final Type s) {
            final Type elementType = t.getElementType();

            if (elementType.isPrimitive()) {
                return isSameType(elementType, elementType(s));
            }

            return isSubtypeNoCapture(elementType, elementType(s));
        }
    };

    public static void adapt(
        final Type source,
        final Type target,
        final ListBuffer<Type<?>> from,
        final ListBuffer<Type<?>> to)
        throws AdaptFailure {

        new Adapter(from, to).adapt(source, target);
    }

    @SuppressWarnings("PackageVisibleField")
    private final static class Adapter extends SimpleVisitor<Type, Void> {

        ListBuffer<Type<?>> from;
        ListBuffer<Type<?>> to;
        Map<Type, Type> mapping;

        Adapter(final ListBuffer<Type<?>> from, final ListBuffer<Type<?>> to) {
            this.from = from;
            this.to = to;
            mapping = new HashMap<>();
        }

        public void adapt(final Type source, final Type target)
            throws AdaptFailure {
            visit(source, target);
            ImmutableList<Type<?>> fromList = from.toList();
            ImmutableList<Type<?>> toList = to.toList();
            while (!fromList.isEmpty()) {
                final Type t = mapping.get(fromList.head);
                if (toList.head != t) {
                    toList.head = t;
                }
                fromList = fromList.tail;
                toList = toList.tail;
            }
        }

        @Override
        public Void visitClassType(final Type source, final Type target)
            throws AdaptFailure {

            adaptRecursive(
                source.getTypeArguments(),
                target.getTypeArguments()
            );

            return null;
        }

        @Override
        public Void visitArrayType(final Type source, final Type target)
            throws AdaptFailure {
            adaptRecursive(elementType(source), elementType(target));
            return null;
        }

        @Override
        public Void visitWildcardType(final Type source, final Type target)
            throws AdaptFailure {
            if (source.hasExtendsBound()) {
                adaptRecursive(upperBound(source), upperBound(target));
            }
            else if (source.hasSuperBound()) {
                adaptRecursive(lowerBound(source), lowerBound(target));
            }
            return null;
        }

        @Override
        public Void visitTypeParameter(final Type source, final Type target)
            throws AdaptFailure {
            // Check to see if there is
            // already a mapping for $source$, in which case
            // the old mapping will be merged with the new
            Type val = mapping.get(source);
            if (val != null) {
                if (val.hasSuperBound() && target.hasSuperBound()) {
                    val = isSubtype(lowerBound(val), lowerBound(target))
                          ? target : val;
                }
                else if (val.hasExtendsBound() && target.hasExtendsBound()) {
                    val = isSubtype(upperBound(val), upperBound(target))
                          ? val : target;
                }
                else if (!isSameType(val, target)) {
                    throw new AdaptFailure();
                }
            }
            else {
                val = target;
                from.append(source);
                to.append(target);
            }
            mapping.put(source, val);
            return null;
        }

        @Override
        public Void visitType(final Type source, final Type target) {
            return null;
        }

        private final Set<TypePair> cache = new HashSet<>();

        private void adaptRecursive(final Type source, final Type target) {
            final TypePair pair = new TypePair(source, target);
            if (cache.add(pair)) {
                try {
                    visit(source, target);
                }
                finally {
                    cache.remove(pair);
                }
            }
        }

        private void adaptRecursive(final TypeList source, final TypeList target) throws AdaptFailure {
            if (source.size() != target.size()) {
                return;
            }

            for (int i = 0, n = source.size(); i < n; i++) {
                adapt(source.get(i), target.get(i));
            }
        }
    }

    public static class AdaptFailure extends RuntimeException {
        static final long serialVersionUID = -7490231548272701566L;
    }

    private static void adaptSelf(
        final Type t,
        final ListBuffer<Type<?>> from,
        final ListBuffer<Type<?>> to) {
        try {
            //if (t.getGenericTypeDefinition() != t)
            adapt(t.getGenericTypeDefinition(), t, from, to);
        }
        catch (AdaptFailure ex) {
            // Adapt should never fail calculating a mapping from
            // t.getGenericTypeDefinition() to t as there can be no merge problem.
            throw new AssertionError(ex);
        }
    }

    public static int hashCode(final Type t) {
        return HashCodeVisitor.visit(t);
    }

    private static final UnaryTypeVisitor<Integer> HashCodeVisitor = new UnaryTypeVisitor<Integer>() {
        @Override
        public Integer visitPrimitiveType(final Type<?> type, final Void parameter) {
            return HashUtilities.hashCode(type.getKind());
        }

        public Integer visitType(final Type t, final Void ignored) {
            return t.getKind().hashCode();
        }

        @Override
        public Integer visitClassType(final Type t, final Void ignored) {
            int result = 0;

            final Type declaringType = t.getDeclaringType();

            if (declaringType != null) {
                result = visit(declaringType);
            }

            result = HashUtilities.combineHashCodes(result, HashUtilities.hashCode(t.getInternalName()));

            for (final Type s : t.getTypeArguments()) {
                result = HashUtilities.combineHashCodes(result, (int) visit(s));
            }

            return result;
        }

        @Override
        public Integer visitWildcardType(final Type t, final Void ignored) {
            int result = t.getKind().hashCode();
            if (t.getSuperBound() != Type.Bottom) {
                result *= 127;
                result += visit(t.getSuperBound());
            }
            else if (t.getExtendsBound() != Types.Object) {
                result *= 127;
                result += visit(t.getExtendsBound());
            }
            return result;
        }

        @Override
        public Integer visitArrayType(final Type t, final Void ignored) {
            return visit(t.getElementType()) + 12;
        }

        @Override
        public Integer visitTypeParameter(final Type t, final Void ignored) {
            if (t instanceof GenericParameter<?>) {
                return HashUtilities.combineHashCodes(
                    HashUtilities.hashCode(((GenericParameter) t).getRawTypeVariable()),
                    t.getGenericParameterPosition()
                );
            }
            return t.getGenericParameterPosition();
        }
    };

    public static boolean isReifiable(final Type t) {
        return IsReifiableVisitor.visit(t);
    }

    private final static UnaryTypeVisitor<Boolean> IsReifiableVisitor = new UnaryTypeVisitor<Boolean>() {

        public Boolean visitType(final Type t, final Void ignored) {
            return true;
        }

        @Override
        public Boolean visitClassType(final Type t, final Void ignored) {
            if (t.isCompoundType()) {
                return Boolean.FALSE;
            }
            else {
                if (!t.isGenericType()) {
                    return Boolean.TRUE;
                }

                for (final Type p : t.getTypeArguments()) {
                    if (p.isUnbounded()) {
                        return Boolean.FALSE;
                    }
                }

                return Boolean.TRUE;
            }
        }

        @Override
        public Boolean visitArrayType(final Type t, final Void ignored) {
            return visit(t.getElementType());
        }

        @Override
        public Boolean visitTypeParameter(final Type t, final Void ignored) {
            return false;
        }
    };

    private static Type _arraySuperType = null;

    private static Type arraySuperType() {
        // initialized lazily to avoid problems during compiler startup
        if (_arraySuperType == null) {
            synchronized (Helper.class) {
                if (_arraySuperType == null) {
                    // JLS 10.8: all arrays implement Cloneable and Serializable.
                    _arraySuperType = Type.makeCompoundType(
                        Types.Object,
                        Type.list(
                            Types.Serializable,
                            Types.Cloneable
                        )
                    );
                }
            }
        }
        return _arraySuperType;
    }

    public static Type asOuterSuper(Type t, final Type type) {
        switch (t.getKind()) {
            case DECLARED:
                do {
                    final Type s = asSuper(t, type);
                    if (s != null) {
                        return s;
                    }
                    t = t.getDeclaringType();
                } while (t.getKind() == TypeKind.DECLARED);
                return null;
            case ARRAY:
                return isSubtype(t, type) ? type : null;
            case TYPEVAR:
                return asSuper(t, type);
            case ERROR:
                return t;
            default:
                return null;
        }
    }

    public static MemberInfo asMemberOf(final Type type, final MemberInfo member) {
        return member.isStatic()
               ? member.getDeclaringType()
               : asMemberOfVisitor.visit(type, member);
    }

    private final static TypeBinder typeBinder = new TypeBinder();

    private static SimpleVisitor<MemberInfo, MemberInfo> asMemberOfVisitor = new SimpleVisitor<MemberInfo, MemberInfo>() {
        @Override
        public MemberInfo visitClassType(final Type<?> type, final MemberInfo member) {
            final Type owner = member.getDeclaringType();

            if (!member.isStatic() && owner.isGenericType()) {
                Type base = asOuterSuper(type, owner);
                //
                // If t is an intersection type T = CT & I1 & I2 ... & In, then
                // its supertypes CT, I1, ... In might contain wildcards, so we
                // need to go through capture conversion.
                //
                base = base.isCompoundType() ? capture(base) : base;

                if (base != null) {
                    final TypeBindings ownerBindings = owner.getTypeBindings();
                    final TypeBindings baseBindings = owner.getTypeBindings();
                    if (!ownerBindings.isEmpty()) {
                        if (baseBindings.isEmpty()) {
                            return typeBinder.visitMember(
                                owner,
                                member,
                                TypeBindings.create(
                                    ownerBindings.getGenericParameters(),
                                    erasure(ownerBindings.getBoundTypes())
                                )
                            );
                        }
                        return typeBinder.visitMember(
                            owner,
                            member,
                            ownerBindings.withAdditionalBindings(base.getTypeBindings())
                        );
                    }
                }
            }

            return member;
        }

        @Override
        public MemberInfo visitTypeParameter(final Type<?> type, final MemberInfo member) {
            return asMemberOf(type.getExtendsBound(), member);
        }

        @Override
        public MemberInfo visitWildcardType(final Type<?> type, final MemberInfo member) {
            return asMemberOf(upperBound(type), member);
        }

        @Override
        public MemberInfo visitType(final Type<?> type, final MemberInfo member) {
            return member;
        }
    };

    private final static Map<Type, ImmutableList<Type<?>>> closureCache = new HashMap<>();

    public static ImmutableList<Type<?>> insert(final ImmutableList<Type<?>> cl, final Type t) {
        if (cl.isEmpty() || precedes(t, cl.head)) {
            return cl.prepend(t);
        }
        else if (precedes(cl.head, t)) {
            return insert(cl.tail, t).prepend(cl.head);
        }
        else {
            return cl;
        }
    }

    private static ImmutableList<Type<?>> closureMin(ImmutableList<Type<?>> cl) {
        final ListBuffer<Type<?>> classes = lb();
        final ListBuffer<Type<?>> interfaces = lb();

        while (!cl.isEmpty()) {
            final Type current = cl.head;

            if (current.isInterface()) {
                interfaces.append(current);
            }
            else {
                classes.append(current);
            }

            final ListBuffer<Type<?>> candidates = lb();

            for (final Type t : cl.tail) {
                if (!isSubtypeNoCapture(current, t)) {
                    candidates.append(t);
                }
            }

            cl = candidates.toList();
        }

        return classes.appendList(interfaces).toList();
    }

    public static ImmutableList<Type<?>> closure(final Type<?> t) {
        ImmutableList<Type<?>> cl = closureCache.get(t);
        if (cl == null) {
            final Type st = superType(t);
            if (!t.isCompoundType()) {
                if (st != null && st.getKind() == TypeKind.DECLARED) {
                    cl = insert(closure(st), t);
                }
                else if (st != null && st.getKind() == TypeKind.TYPEVAR) {
                    cl = closure(st).prepend(t);
                }
                else {
                    cl = ImmutableList.<Type<?>>of(t);
                }
            }
            else {
                cl = closure(superType(t));
            }
            for (ImmutableList<Type<?>> l = interfaces(t); l.nonEmpty(); l = l.tail) {
                cl = union(cl, closure(l.head));
            }
            closureCache.put(t, cl);
        }
        return cl;
    }

    @SuppressWarnings("PackageVisibleField")
    final static class TypePair {
        final Type t1;
        final Type t2;

        TypePair(final Type t1, final Type t2) {
            this.t1 = t1;
            this.t2 = t2;
        }

        @Override
        public int hashCode() {
            return 127 * Helper.hashCode(t1) + Helper.hashCode(t2);
        }

        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof TypePair)) {
                return false;
            }

            final TypePair typePair = (TypePair)obj;

            return Helper.isSameType(t1, typePair.t1) &&
                   Helper.isSameType(t2, typePair.t2);
        }
    }
}
