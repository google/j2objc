/*
 * Binder.java
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

import com.strobel.core.ArrayUtilities;
import com.strobel.util.TypeUtils;

import java.util.Set;

/**
 * @author Mike Strobel
 */
public abstract class Binder {
    static boolean compareMethodSignatureAndName(final MethodBase m1, final MethodBase m2) {
        final ParameterList p1 = m1.getParameters();
        final ParameterList p2 = m2.getParameters();

        if (p1.size() != p2.size()) {
            return false;
        }

        for (int i = 0, n = p1.size(); i < n; i++) {
            if (!TypeUtils.areEquivalent(p1.get(i).getParameterType(), p2.get(i).getParameterType())) {
                return false;
            }
        }

        return true;
    }

    static int getHierarchyDepth(final Type t) {
        int depth = 0;

        Type currentType = t;
        do {
            depth++;
            currentType = currentType.getBaseType();
        } while (currentType != null);

        return depth;
    }

    static MethodBase findMostDerivedNewSlotMethod(final MethodBase[] match, final int cMatches) {
        int deepestHierarchy = 0;
        MethodBase methodWithDeepestHierarchy = null;

        for (int i = 0; i < cMatches; i++) {
            // Calculate the depth of the hierarchy of the declaring type of the
            // current method.
            final int currentHierarchyDepth = getHierarchyDepth(match[i].getDeclaringType());

            // The two methods have the same name, signature, and hierarchy depth.
            // This can only happen if at least one is vararg or generic.
            if (currentHierarchyDepth == deepestHierarchy) {
                throw Error.ambiguousMatch();
            }

            // Check to see if this method is on the most derived class.
            if (currentHierarchyDepth > deepestHierarchy) {
                deepestHierarchy = currentHierarchyDepth;
                methodWithDeepestHierarchy = match[i];
            }
        }

        return methodWithDeepestHierarchy;
    }

    public abstract MethodBase selectMethod(final Set<BindingFlags> bindingFlags, final MethodBase[] matches, final Type[] parameterTypes);
}

@SuppressWarnings("ConstantConditions")
final class DefaultBinder extends Binder {
    @Override
    public MethodBase selectMethod(final Set<BindingFlags> bindingFlags, final MethodBase[] matches, final Type[] types) {
        if (ArrayUtilities.isNullOrEmpty(matches)) {
            return null;
        }

        final MethodBase[] candidates = matches.clone();

        //
        // Find all the methods that can be described by the parameter types.
        // Remove all of them that cannot.
        //

        int stop;
        int currentIndex = 0;

        for (int i = 0, n = candidates.length; i < n; i++) {
            final MethodBase candidate = candidates[i];
            final ParameterList parameters = candidate.getParameters();
            final int parameterCount = parameters.size();
            final boolean isVarArgs = candidate.getCallingConvention() == CallingConvention.VarArgs;

            if (parameterCount != types.length && !isVarArgs) {
                continue;
            }

            for (stop = 0; stop < Math.min(parameterCount,  types.length); stop++) {
                final Type parameterType = parameters.get(stop).getParameterType();

                if (parameterType == types[stop] || parameterType == Types.Object) {
                    continue;
                }

                if (parameterType.isAssignableFrom(types[stop])) {
                    continue;
                }

                if (!isVarArgs || stop != parameterCount - 1) {
                    break;
                }

                if (!parameterType.getElementType().isAssignableFrom(types[stop])) {
                    break;
                }
            }

            if (stop == parameterCount ||
                stop == parameterCount - 1 && isVarArgs) {

                candidates[currentIndex++] = candidate;
            }
        }

        if (currentIndex == 0) {
            return null;
        }

        if (currentIndex == 1) {
            return candidates[0];
        }

        // Walk all of the methods looking the most specific method to invoke
        int currentMin = 0;
        boolean ambiguous = false;

        final int[] parameterOrder = new int[types.length];

        for (int i = 0, n = types.length; i < n; i++) {
            parameterOrder[i] = i;
        }

        for (int i = 1; i < currentIndex; i++) {
            final MethodBase m1 = candidates[currentMin];
            final MethodBase m2 = candidates[i];
            
            final Type<?> varArgType1;
            final Type<?> varArgType2;
            
            if (m1.getCallingConvention() == CallingConvention.VarArgs) {
                final TypeList pt1 = m1.getParameters().getParameterTypes();
                varArgType1 = pt1.get(pt1.size() - 1).getElementType();
            }
            else {
                varArgType1 = null;
            }
            
            if (m2.getCallingConvention() == CallingConvention.VarArgs) {
                final TypeList pt2 = m2.getParameters().getParameterTypes();
                varArgType2 = pt2.get(pt2.size() - 1).getElementType();
            }
            else {
                varArgType2 = null;
            }
            
            final int newMin = findMostSpecificMethod(
                m1,
                parameterOrder,
                varArgType1,
                candidates[i],
                parameterOrder,
                varArgType2,
                types,
                null
            );

            if (newMin == 0) {
                ambiguous = true;
            }
            else {
                if (newMin == 2) {
                    ambiguous = false;
                    currentMin = i;
                }
            }
        }
        if (ambiguous) {
            throw Error.ambiguousMatch();
        }

        return candidates[currentMin];
    }

    private static int findMostSpecificMethod(
        final MethodBase m1,
        final int[] varArgOrder1,
        final Type varArgArrayType1,
        final MethodBase m2,
        final int[] varArgOrder2,
        final Type varArgArrayType2,
        final Type[] types,
        final Object[] args) {

        //
        // Find the most specific method based on the parameters.
        //
        final int result = findMostSpecific(
            m1.getParameters(),
            varArgOrder1,
            varArgArrayType1,
            m2.getParameters(),
            varArgOrder2,
            varArgArrayType2,
            types,
            args
        );

        //
        // If the match was not ambiguous then return the result.
        //
        if (result != 0) {
            return result;
        }

        //
        // Check to see if the methods have the exact same name and signature.
        //
        if (compareMethodSignatureAndName(m1, m2)) {
            //
            // Determine the depth of the declaring types for both methods.
            //
            final int hierarchyDepth1 = getHierarchyDepth(m1.getDeclaringType());
            final int hierarchyDepth2 = getHierarchyDepth(m2.getDeclaringType());

            //
            // The most derived method is the most specific one.
            //
            if (hierarchyDepth1 == hierarchyDepth2) {
                return 0;
            }
            else if (hierarchyDepth1 < hierarchyDepth2) {
                return 2;
            }
            else {
                return 1;
            }
        }

        //
        // The match is ambiguous.
        //
        return 0;
    }

    private static int findMostSpecific(
        final ParameterList p1,
        final int[] varArgOrder1,
        final Type varArgArrayType1,
        final ParameterList p2,
        final int[] varArgOrder2,
        final Type varArgArrayType2,
        final Type[] types,
        final Object[] args) {
        //
        // A method using varargs is always less specific than one not using varargs.
        //
        if (varArgArrayType1 != null && varArgArrayType2 == null) {
            return 2;
        }
        if (varArgArrayType2 != null && varArgArrayType1 == null) {
            return 1;
        }

        //
        // Now either p1 and p2 both use params or neither does.
        //

        boolean p1Less = false;
        boolean p2Less = false;

        for (int i = 0, n = types.length; i < n; i++) {
            if (args != null && args[i] == Missing.Value) {
                continue;
            }

            final Type c1;
            final Type c2;

            //
            //  If a vararg array is present, then either
            //      the user re-ordered the parameters, in which case
            //          the argument to the vararg array is either an array
            //              in which case the params is conceptually ignored and so varArgArrayType1 == null
            //          or the argument to the vararg array is a single element
            //              in which case varArgOrder[i] == p1.Length - 1 for that element 
            //      or the user did not re-order the parameters in which case 
            //          the varArgOrder array could contain indexes larger than p.Length - 1 (see VSW 577286)
            //          so any index >= p.Length - 1 is being put in the vararg array 
            //

            if (varArgArrayType1 != null && varArgOrder1[i] >= p1.size() - 1) {
                c1 = varArgArrayType1;
            }
            else {
                c1 = p1.get(varArgOrder1[i]).getParameterType();
            }

            if (varArgArrayType2 != null && varArgOrder2[i] >= p2.size() - 1) {
                c2 = varArgArrayType2;
            }
            else {
                c2 = p2.get(varArgOrder2[i]).getParameterType();
            }

            if (c1 == c2) {
                continue;
            }

            switch (findMostSpecificType(c1, c2, types[i])) {
                case 0:
                    return 0;
                case 1:
                    p1Less = true;
                    break;
                case 2:
                    p2Less = true;
                    break;
            }
        }

        // Two way p1Less and p2Less can be equal.  All the arguments are the
        //  same they both equal false, otherwise there were things that both 
        //  were the most specific type on....
        if (p1Less == p2Less) {
            // if we cannot tell which is a better match based on parameter types (p1Less == p2Less),
            // let's see which one has the most matches without using the params array (the longer one wins). 
            if (!p1Less && args != null) {
                if (p1.size() > p2.size()) {
                    return 1;
                }
                else if (p2.size() > p1.size()) {
                    return 2;
                }
            }

            return 0;
        }
        else {
            return p1Less ? 1 : 2;
        }
    }

    private static int findMostSpecificType(final Type c1, final Type c2, final Type t) {
        //
        // If the two types are exact move on...
        //
        if (TypeUtils.areEquivalent(c1, c2)) {
            return 0;
        }

        if (TypeUtils.areEquivalent(c1, t)) {
            return 1;
        }

        if (TypeUtils.areEquivalent(c2, t)) {
            return 2;
        }

        final boolean c1FromC2 = c1.isAssignableFrom(c2);
        final boolean c2FromC1 = c2.isAssignableFrom(c1);

        if (c1FromC2 == c2FromC1) {
            return 0;
        }

        return c1FromC2 ? 2 : 1;
    }
}