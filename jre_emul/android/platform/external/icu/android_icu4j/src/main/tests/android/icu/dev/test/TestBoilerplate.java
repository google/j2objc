/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2004-2016, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import android.icu.text.UnicodeSet;

/**
 * To use, override the abstract and the protected methods as necessary.
 * Tests boilerplate invariants:
 * <br>a.equals(a)
 * <br>!a.equals(null)
 * <br>if a.equals(b) then 
 * <br>(1) a.hashCode() == b.hashCode  // note: the reverse is not necessarily true.
 * <br>(2) a functions in all aspects as equivalent to b
 * <br>(3) b.equals(a)
 * <br>if b = clone(a)
 * <br>(1) b.equals(a), and the above checks
 * <br>(2) if mutable(a), then a.clone() != a // note: the reverse is not necessarily true.
 * @author Davis
 */
public abstract class TestBoilerplate<T> extends TestFmwk {

    protected static Random random = new Random(12345);

    protected final void _test() throws Exception {
        List<T> list = new LinkedList<T>();
        while (_addTestObject(list)) {
        }
        T[] testArray = (T[]) list.toArray();
        for (int i = 0; i < testArray.length; ++i) {
            //logln("Testing " + i);
            T a = testArray[i];
            int aHash = a.hashCode();
            if (a.equals(null)) {
                errln("Equality/Null invariant fails: " + i);
            }
            if (!a.equals(a)) {
                errln("Self-Equality invariant fails: " + i);
            }
            T b;                
            if (_canClone(a)) {
                b = _clone(a);
                if (b == a) {
                    if (_isMutable(a)) {
                        errln("Clone/Mutability invariant fails: " + i);
                    }
                } else {
                    if (!a.equals(b)) {
                        errln("Clone/Equality invariant fails: " + i);
                    }
                }
                _checkEquals(i, -1, a, aHash, b);
            }
            for (int j = i; j < testArray.length; ++j) {
                b = testArray[j];
                if (a.equals(b)) _checkEquals(i, j, a, aHash, b);
            }
        }
    }

    private void _checkEquals(int i, int j, T a, int aHash, T b) {
        int bHash = b.hashCode();
        if (!b.equals(a)) errln("Equality/Symmetry",i, j);
        if (aHash != bHash) errln("Equality/Hash",i, j);
        if (a != b && !_hasSameBehavior(a,b)) {
            errln("Equality/Equivalence",i, j);
        }
    }

    private void errln(String title, int i, int j) {
        if (j < 0) errln("Clone/" + title + "invariant fails: " + i);
        else errln(title + "invariant fails: " + i + "," + j);
    }

    /**
     * Must be overridden to check whether a and be behave the same
     */
    protected abstract boolean _hasSameBehavior(T a, T b);

    /**
     * This method will be called multiple times until false is returned.
     * The results should be a mixture of different objects of the same
     * type: some equal and most not equal.
     * The subclasser controls how many are produced (recommend about 
     * 100, based on the size of the objects and how costly they are
     * to run this test on. The running time grows with the square of the
     * count.
     * NOTE: this method will only be called if the objects test as equal.
     */
    protected abstract boolean _addTestObject(List<T> c);
    /**
     * Override if the tested objects are mutable.
     * <br>Since Java doesn't tell us, we need a function to tell if so.
     * The default is true, so must be overridden if not.
     */
    protected boolean _isMutable(T a) {
        return true;
    }
    /**
     * Override if the tested objects can be cloned.
     */
    protected boolean _canClone(T a) {
        return true;
    }
    /**
     * Produce a clone of the object. Tries two methods
     * (a) clone
     * (b) constructor
     * Must be overridden if _canClone returns true and
     * the above methods don't work.
     * @param a
     * @return clone
     */
    protected T _clone(T a) throws Exception {
        Class aClass = a.getClass();
        try {
            Method cloner = aClass.getMethod("clone", (Class[])null);
            return (T) cloner.invoke(a,(Object[])null);
        } catch (NoSuchMethodException e) {
            Constructor constructor = aClass.getConstructor(new Class[] {aClass});
            return (T) constructor.newInstance(new Object[]{a});
        }
    }
    
    /* Utilities */
    public static boolean verifySetsIdentical(AbstractTestLog here, UnicodeSet set1, UnicodeSet set2) {
        if (set1.equals(set2)) return true;
        TestFmwk.errln("Sets differ:");
        TestFmwk.errln("UnicodeMap - HashMap");
        TestFmwk.errln(new UnicodeSet(set1).removeAll(set2).toPattern(true));
        TestFmwk.errln("HashMap - UnicodeMap");
        TestFmwk.errln(new UnicodeSet(set2).removeAll(set1).toPattern(true));
        return false;
    }

    public static boolean verifySetsIdentical(AbstractTestLog here, Set values1, Set values2) {
        if (values1.equals(values2)) return true;
        Set temp;
        TestFmwk.errln("Values differ:");
        TestFmwk.errln("UnicodeMap - HashMap");
        temp = new TreeSet(values1);
        temp.removeAll(values2);
        TestFmwk.errln(show(temp));
        TestFmwk.errln("HashMap - UnicodeMap");
        temp = new TreeSet(values2);
        temp.removeAll(values1);
        TestFmwk.errln(show(temp));
        return false;
    }
    
    public static String show(Map m) {
        StringBuilder buffer = new StringBuilder();
        for (Iterator it = m.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            buffer.append(key + "=>" + m.get(key) + "\r\n");
        }
        return buffer.toString();
    }
    
    public static <T> UnicodeSet getSet(Map<Integer, T> m, T value) {
        UnicodeSet result = new UnicodeSet();
        for (Iterator<Integer> it = m.keySet().iterator(); it.hasNext();) {
            Integer key = it.next();
            T val = m.get(key);
            if (!val.equals(value)) continue;
            result.add(key.intValue());
        }
        return result;
    }
    
    public static String show(Collection c) {
        StringBuilder buffer = new StringBuilder();
        for (Iterator it = c.iterator(); it.hasNext();) {
            buffer.append(it.next() + "\r\n");
        }
        return buffer.toString();
    }
}
