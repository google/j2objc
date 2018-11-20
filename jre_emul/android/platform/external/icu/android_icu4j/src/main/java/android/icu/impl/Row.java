/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 **********************************************************************
 * Copyright (c) 2002-2014, Google, International Business Machines
 * Corporation and others.  All Rights Reserved.
 **********************************************************************
 * Author: Mark Davis
 **********************************************************************
 */
package android.icu.impl;

import android.icu.util.Freezable;


/**
 * @hide Only a subset of ICU is exposed in Android
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class Row<C0, C1, C2, C3, C4> implements java.lang.Comparable, Cloneable,
                                        Freezable<Row<C0, C1, C2, C3, C4>>{
    protected Object[] items;
    protected volatile boolean frozen;

    /**
     * Convenience Methods
     */
    public static <C0, C1> R2<C0,C1> of(C0 p0, C1 p1) {
        return new R2<C0,C1>(p0,p1);
    }
    public static <C0, C1, C2> R3<C0,C1,C2> of(C0 p0, C1 p1, C2 p2) {
        return new R3<C0,C1,C2>(p0,p1,p2);
    }
    public static <C0, C1, C2, C3> R4<C0,C1,C2,C3> of(C0 p0, C1 p1, C2 p2, C3 p3) {
        return new R4<C0,C1,C2,C3>(p0,p1,p2,p3);
    }
    public static <C0, C1, C2, C3, C4> R5<C0,C1,C2,C3,C4> of(C0 p0, C1 p1, C2 p2, C3 p3, C4 p4) {
        return new R5<C0,C1,C2,C3,C4>(p0,p1,p2,p3,p4);
    }

    public static class R2<C0, C1> extends Row<C0, C1, C1, C1, C1> {
        public R2(C0 a, C1 b)  {
            items = new Object[] {a, b};
        }
    }
    public static class R3<C0, C1, C2> extends Row<C0, C1, C2, C2, C2> {
        public R3(C0 a, C1 b, C2 c)  {
            items = new Object[] {a, b, c};
        }
    }
    public static class R4<C0, C1, C2, C3> extends Row<C0, C1, C2, C3, C3> {
        public R4(C0 a, C1 b, C2 c, C3 d)  {
            items = new Object[] {a, b, c, d};
        }
    }
    public static class R5<C0, C1, C2, C3, C4> extends Row<C0, C1, C2, C3, C4> {
        public R5(C0 a, C1 b, C2 c, C3 d, C4 e)  {
            items = new Object[] {a, b, c, d, e};
        }
    }

    public Row<C0, C1, C2, C3, C4> set0(C0 item) {
        return set(0, item);
    }
    public C0 get0() {
        return (C0) items[0];
    }
    public Row<C0, C1, C2, C3, C4> set1(C1 item) {
        return set(1, item);
    }
    public C1 get1() {
        return (C1) items[1];
    }
    public Row<C0, C1, C2, C3, C4> set2(C2 item) {
        return set(2, item);
    }
    public C2 get2() {
        return (C2) items[2];
    }
    public Row<C0, C1, C2, C3, C4> set3(C3 item) {
        return set(3, item);
    }
    public C3 get3() {
        return (C3) items[3];
    }
    public Row<C0, C1, C2, C3, C4> set4(C4 item) {
        return set(4, item);
    }
    public C4 get4() {
        return (C4) items[4];
    }

    protected Row<C0, C1, C2, C3, C4> set(int i, Object item) {
        if (frozen) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        items[i] = item;
        return this;
    }

    @Override
    public int hashCode() {
        int sum = items.length;
        for (Object item : items) {
            sum = sum*37 + Utility.checkHash(item);
        }
        return sum;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        try {
            Row<C0, C1, C2, C3, C4> that = (Row<C0, C1, C2, C3, C4>)other;
            if (items.length != that.items.length) {
                return false;
            }
            int i = 0;
            for (Object item : items) {
                if (!Utility.objectEquals(item, that.items[i++])) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int compareTo(Object other) {
        int result;
        Row<C0, C1, C2, C3, C4> that = (Row<C0, C1, C2, C3, C4>)other;
        result = items.length - that.items.length;
        if (result != 0) {
            return result;
        }
        int i = 0;
        for (Object item : items) {
            result = Utility.checkCompare(((Comparable)item), ((Comparable)that.items[i++]));
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("[");
        boolean first = true;
        for (Object item : items) {
            if (first) {
                first = false;
            } else {
                result.append(", ");
            }
            result.append(item);
        }
        return result.append("]").toString();
    }

    @Override
    public boolean isFrozen() {
        return frozen;
    }

    @Override
    public Row<C0, C1, C2, C3, C4> freeze() {
        frozen = true;
        return this;
    }

    @Override
    public Object clone() {
        if (frozen) return this;
        try {
            Row<C0, C1, C2, C3, C4> result = (Row<C0, C1, C2, C3, C4>) super.clone();
            items = items.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public Row<C0, C1, C2, C3, C4> cloneAsThawed() {
        try {
            Row<C0, C1, C2, C3, C4> result = (Row<C0, C1, C2, C3, C4>) super.clone();
            items = items.clone();
            result.frozen = false;
            return result;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}

