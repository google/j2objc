/*
 * Types.java
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

import com.strobel.core.BooleanBox;
import com.strobel.core.ByteBox;
import com.strobel.core.CharacterBox;
import com.strobel.core.Comparer;
import com.strobel.core.DoubleBox;
import com.strobel.core.FloatBox;
import com.strobel.core.IntegerBox;
import com.strobel.core.LongBox;
import com.strobel.core.ShortBox;
import com.strobel.core.StrongBox;

import java.io.Serializable;
import java.lang.Error;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * @author Mike Strobel
 */
public final class Types {

    private Types() {}

    public static final Type<Object> Object;

    public static final Type<Enum> Enum;
    public static final Type<Number> Number;
    public static final Type<Boolean> Boolean;
    public static final Type<Byte> Byte;
    public static final Type<Character> Character;
    public static final Type<Short> Short;
    public static final Type<Integer> Integer;
    public static final Type<Long> Long;
    public static final Type<Float> Float;
    public static final Type<Double> Double;
    public static final Type<String> String;
    public static final Type<Date> Date;
    public static final Type<UUID> UUID;

    public static final Type<Comparer> Comparer;
    
    public static final Type<Runnable> Runnable;
    public static final Type<Callable> Callable;

    public static final Type<Error> Error;
    public static final Type<Throwable> Throwable;
    public static final Type<Exception> Exception;
    public static final Type<RuntimeException> RuntimeException;

    public static final Type<StringBuilder> StringBuilder;
    public static final Type<StringBuffer> StringBuffer;

    public static final Type<BigInteger> BigInteger;
    public static final Type<BigDecimal> BigDecimal;

    public static final Type<System> System;

    public static final Type<java.lang.annotation.Annotation> Annotation;
    public static final Type<Class> Class;
    public static final Type<ClassLoader> ClassLoader;

    public static final Type<Serializable> Serializable;
    public static final Type<Cloneable> Cloneable;
    public static final Type<Comparable> Comparable;

    public static final Type<Iterable> Iterable;
    public static final Type<Iterator> Iterator;
    public static final Type<Collection> Collection;
    public static final Type<List> List;
    public static final Type<Set> Set;
    public static final Type<Map> Map;
    public static final Type<ArrayList> ArrayList;
    public static final Type<HashMap> HashMap;
    public static final Type<HashSet> HashSet;

    public static final Type<MethodHandle> MethodHandle;

    public static final Type<StrongBox> StrongBox;
    public static final Type<BooleanBox> BooleanBox;
    public static final Type<CharacterBox> CharacterBox;
    public static final Type<ByteBox> ByteBox;
    public static final Type<ShortBox> ShortBox;
    public static final Type<IntegerBox> IntegerBox;
    public static final Type<LongBox> LongBox;
    public static final Type<FloatBox> FloatBox;
    public static final Type<DoubleBox> DoubleBox;

    static {
        Object = Type.of(Object.class);
        Enum = Type.of(Enum.class);
        Number = Type.of(Number.class);
        Boolean = Type.of(Boolean.class);
        Byte = Type.of(Byte.class);
        Character = Type.of(Character.class);
        Short = Type.of(Short.class);
        Integer = Type.of(Integer.class);
        Long = Type.of(Long.class);
        Float = Type.of(Float.class);
        Double = Type.of(Double.class);
        String = Type.of(String.class);
        Date = Type.of(Date.class);
        UUID = Type.of(UUID.class);

        Comparer = Type.of(Comparer.class);

        Runnable = Type.of(Runnable.class);
        Callable = Type.of(Callable.class);

        Error = Type.of(java.lang.Error.class);
        Throwable = Type.of(Throwable.class);
        Exception = Type.of(Exception.class);
        RuntimeException = Type.of(RuntimeException.class);

        StringBuffer = Type.of(StringBuffer.class);
        StringBuilder = Type.of(StringBuilder.class);

        BigInteger = Type.of(BigInteger.class);
        BigDecimal = Type.of(BigDecimal.class);

        System = Type.of(System.class);

        Annotation = Type.of(Annotation.class);
        Class = Type.of(Class.class);
        ClassLoader = Type.of(ClassLoader.class);

        Serializable = Type.of(Serializable.class);
        Cloneable = Type.of(Cloneable.class);
        Comparable = Type.of(Comparable.class);

        Iterable = Type.of(Iterable.class);
        Iterator = Type.of(Iterator.class);
        Collection = Type.of(Collection.class);
        List = Type.of(List.class);
        Set = Type.of(Set.class);
        Map = Type.of(Map.class);
        ArrayList = Type.of(ArrayList.class);
        HashMap = Type.of(HashMap.class);
        HashSet = Type.of(HashSet.class);

        MethodHandle = Type.of(MethodHandle.class);

        StrongBox = Type.of(StrongBox.class);
        BooleanBox = Type.of(BooleanBox.class);
        CharacterBox = Type.of(CharacterBox.class);
        ByteBox = Type.of(ByteBox.class);
        ShortBox = Type.of(ShortBox.class);
        IntegerBox = Type.of(IntegerBox.class);
        LongBox = Type.of(LongBox.class);
        FloatBox = Type.of(FloatBox.class);
        DoubleBox = Type.of(DoubleBox.class);
    }

    static void ensureRegistered() {
        if (Types.Object != Type.CACHE.find(java.lang.Object.class)) {
            throw new IllegalStateException("Standard Java types were not successfully registered!");
        }
    }
}