/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2002-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

 
package android.icu.dev.test.collator;

import java.util.Comparator;

import org.junit.Ignore;
import org.junit.Test;

public class TestComparator {

    // TODO(user): apparently orphaned - added dummy test to pass ant junit
    @Ignore
    @Test
    public void dummyTest() {}
    
    // test the symmetry and transitivity
    public void test(Comparator comp, int count) {
        Object c = null;
        Object b = newObject(c);
        Object a = newObject(b);
        int compab = comp.compare(a,b);
        while (--count >= 0) {
            // rotate old values
            c = b;
            b = a;
            int compbc = compab;
            
            // allocate new and get comparisons
            a = newObject(b);
            compab = comp.compare(a,b);
            int compba = comp.compare(b,a);
            int compac = comp.compare(a,c);
            
            // check symmetry
            if (compab != -compba) {
                log("Symmetry Failure", new Object[] {a, b});
            }
            
            // check transitivity
            check(a, b, c,  compab,  compbc,  compac);
            check(a, c, b,  compab, -compbc,  compab);
            check(b, a, c, -compab,  compac,  compbc);
            check(b, c, a,  compbc, -compac, -compab);
            check(c, a, b, -compac,  compab, -compbc);
            check(c, b, a, -compbc, -compab, -compac);
        }
    }
    
    private void check(Object a, Object b, Object c, 
      int compab, int compbc, int compac) {
        if (compab <= 0 && compbc <= 0 && !(compac <= 0)) {
            log("Transitivity Failure", new Object[] {a, b, c});             
        }
    }
   
    public Object newObject(Object c) {
        // return a new object
        return "";
    }
    
    public String format(Object c) {
        // return a new object
        return c.toString();
    }
    
    public void log(String title, Object[] arguments) {
        String result = title + ": [";
        for (int i = 0; i < arguments.length; ++i) {
            if (i != 0) result += ", ";
            result += format(arguments[i]);
        }
        result += "]";
        throw new RuntimeException(result);
    }
}