/*
 * PrimitiveTypes.java
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

import javax.lang.model.type.TypeKind;
import java.util.List;

/**
 * @author strobelm
 */
public final class PrimitiveTypes {
    private PrimitiveTypes() {}

    public final static Type<Void> Void = new PrimitiveType<>(java.lang.Void.TYPE, 'V', "void", TypeKind.VOID);
    public final static Type<Boolean> Boolean = new PrimitiveType<>(java.lang.Boolean.TYPE, 'Z', "boolean", TypeKind.BOOLEAN);
    public final static Type<Byte> Byte = new PrimitiveType<>(java.lang.Byte.TYPE, 'B', "byte", TypeKind.BYTE);
    public final static Type<Short> Short = new PrimitiveType<>(java.lang.Short.TYPE, 'S', "short", TypeKind.SHORT);
    public final static Type<Character> Character = new PrimitiveType<>(java.lang.Character.TYPE, 'C', "char", TypeKind.CHAR);
    public final static Type<Integer> Integer = new PrimitiveType<>(java.lang.Integer.TYPE, 'I', "int", TypeKind.INT);
    public final static Type<Long> Long = new PrimitiveType<>(java.lang.Long.TYPE, 'J', "long", TypeKind.LONG);
    public final static Type<Float> Float = new PrimitiveType<>(java.lang.Float.TYPE, 'F', "float", TypeKind.FLOAT);
    public final static Type<Double> Double = new PrimitiveType<>(java.lang.Double.TYPE, 'D', "double", TypeKind.DOUBLE);

    private final static List<Type<?>> AllPrimitives = ArrayUtilities.asUnmodifiableList(
        Void, Boolean, Byte, Short, Character, Integer, Long,  Float, Double
    );

    static {
        Type.CACHE.add(PrimitiveTypes.Void);
        Type.CACHE.add(PrimitiveTypes.Boolean);
        Type.CACHE.add(PrimitiveTypes.Byte);
        Type.CACHE.add(PrimitiveTypes.Short);
        Type.CACHE.add(PrimitiveTypes.Character);
        Type.CACHE.add(PrimitiveTypes.Integer);
        Type.CACHE.add(PrimitiveTypes.Long);
        Type.CACHE.add(PrimitiveTypes.Float);
        Type.CACHE.add(PrimitiveTypes.Double);
    }

    static void ensureRegistered() {
        if (Void != Type.CACHE.find(java.lang.Void.TYPE)) {
            throw new IllegalStateException("Primitive types were not successfully registered!");
        }
    }

    public static List<Type<?>> allPrimitives() {
        return AllPrimitives;
    }
}
