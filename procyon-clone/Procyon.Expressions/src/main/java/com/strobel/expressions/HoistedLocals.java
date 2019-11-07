/*
 * HoistedLocals.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is based on the Dynamic Language Runtime from Microsoft,
 *   Copyright (c) Microsoft Corporation.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.expressions;

import com.strobel.core.ArrayUtilities;
import com.strobel.core.StrongBox;
import com.strobel.reflection.Types;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


//
// Suppose we have something like:
//
//    (String s)->()->s.
//
// We wish to generate the outer as:
//
//      Func<String> outerMethod(Closure closure, String s)
//      {
//          object[] locals = new object[1];
//          locals[0] = new StrongBox<String>();
//          ((StrongBox<String>)locals[0]).setValue(s);
//
//          return ((DynamicMethod)closure.Constants[0]).createDelegate(
//              Type.of(Func).makeGenericType(Types.String),
//              new Closure(null, locals)
//          );
//      }
//
// ... and the inner as:
//
//      string innerMethod(Closure closure)
//      {
//          object[] locals = closure.Locals;
//          return ((StrongBox<String>)locals[0]).getValue();
//      }
//
// This class tracks that "s" was hoisted into a closure, as the 0th
// element in the array
//

@SuppressWarnings("PackageVisibleField")
final class HoistedLocals {
    final HoistedLocals parent;
    final Map<ParameterExpression, Integer> indexes;
    final ParameterExpressionList variables;
    final ParameterExpression selfVariable;

    HoistedLocals(final HoistedLocals parent, final ParameterExpression... variables) {
        this(
            parent,
            ArrayUtilities.isNullOrEmpty(variables)
            ? ParameterExpressionList.empty()
            : new ParameterExpressionList(variables)
        );
    }

    HoistedLocals(final HoistedLocals parent, final ParameterExpressionList variables) {
        this.parent = parent;

        if (parent != null) {
            // Add the parent locals array as the 0th element in the array
            this.variables = variables.add(0, parent.selfVariable);
        }
        else {
            this.variables = variables;
        }

        final Map<ParameterExpression, Integer> indexes = new LinkedHashMap<>();

        for (int i = 0, n = this.variables.size(); i < n; i++) {
            final ParameterExpression variable = this.variables.get(i);
            indexes.put(variable, i);
        }

        this.selfVariable = Expression.variable(Types.Object.makeArrayType(), null);
        this.indexes = Collections.unmodifiableMap(indexes);
    }

    ParameterExpression getParentVariable() {
        return this.parent != null ? this.parent.selfVariable : null;
    }

    @SuppressWarnings("unchecked")
    static Object[] getParent(final Object[] locals) {
        return ((StrongBox<Object[]>)locals[0]).value;
    }
}
