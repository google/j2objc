/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

import java.util.concurrent.atomic.*;
import junit.framework.*;
import java.util.*;

public class AtomicReferenceFieldUpdaterTest extends JSR166TestCase{
    volatile Integer x = null;
    Object z;
    Integer w;

    public static void main(String[] args){
        junit.textui.TestRunner.run(suite());
    }
    public static Test suite() {
        return new TestSuite(AtomicReferenceFieldUpdaterTest.class);
    }

    /**
     * Construction with non-existent field throws RuntimeException
     */
    public void testConstructor(){
        try{
            AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>
                a = AtomicReferenceFieldUpdater.newUpdater
                (AtomicReferenceFieldUpdaterTest.class, Integer.class, "y");
            shouldThrow();
        }
        catch (RuntimeException rt) {}
    }


    /**
     * construction with field not of given type throws RuntimeException
     */
    public void testConstructor2(){
        try{
            AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>
                a = AtomicReferenceFieldUpdater.newUpdater
                (AtomicReferenceFieldUpdaterTest.class, Integer.class, "z");
            shouldThrow();
        }
        catch (RuntimeException rt) {}
    }

    /**
     * Constructor with non-volatile field throws exception
     */
    public void testConstructor3(){
        try{
            AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>
                a = AtomicReferenceFieldUpdater.newUpdater
                (AtomicReferenceFieldUpdaterTest.class, Integer.class, "w");
            shouldThrow();
        }
        catch (RuntimeException rt) {}
    }

    static class Base {
        protected volatile Object f = null;
    }
    static class Sub1 extends Base {
        AtomicReferenceFieldUpdater<Base, Object> fUpdater
                = AtomicReferenceFieldUpdater.newUpdater(Base.class, Object.class, "f");
    }
    static class Sub2 extends Base {}

    public void testProtectedFieldOnAnotherSubtype() {
        Sub1 sub1 = new Sub1();
        Sub2 sub2 = new Sub2();

        sub1.fUpdater.set(sub1, "f");
        try {
            sub1.fUpdater.set(sub2, "g");
            shouldThrow();
        }
        catch (RuntimeException rt) {}
    }

    /**
     *  get returns the last value set or assigned
     */
    public void testGetSet(){
        AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>a;
        try {
            a = AtomicReferenceFieldUpdater.newUpdater(AtomicReferenceFieldUpdaterTest.class, Integer.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = one;
	assertEquals(one,a.get(this));
	a.set(this,two);
	assertEquals(two,a.get(this));
	a.set(this,m3);
	assertEquals(m3,a.get(this));
    }
    /**
     * compareAndSet succeeds in changing value if equal to expected else fails
     */
    public void testCompareAndSet(){
        AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>a;
        try {
            a = AtomicReferenceFieldUpdater.newUpdater(AtomicReferenceFieldUpdaterTest.class, Integer.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = one;
	assertTrue(a.compareAndSet(this,one,two));
	assertTrue(a.compareAndSet(this,two,m4));
	assertEquals(m4,a.get(this));
	assertFalse(a.compareAndSet(this,m5,seven));
	assertFalse((seven == a.get(this)));
	assertTrue(a.compareAndSet(this,m4,seven));
	assertEquals(seven,a.get(this));
    }

    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() {
        x = one;
        final AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>a;
        try {
            a = AtomicReferenceFieldUpdater.newUpdater(AtomicReferenceFieldUpdaterTest.class, Integer.class, "x");
        } catch (RuntimeException ok) {
            return;
        }

        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!a.compareAndSet(AtomicReferenceFieldUpdaterTest.this, two, three)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(a.compareAndSet(this, one, two));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(a.get(this), three);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }

    /**
     * repeated weakCompareAndSet succeeds in changing value when equal
     * to expected 
     */
    public void testWeakCompareAndSet(){
        AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>a;
        try {
            a = AtomicReferenceFieldUpdater.newUpdater(AtomicReferenceFieldUpdaterTest.class, Integer.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = one;
	while(!a.weakCompareAndSet(this,one,two));
	while(!a.weakCompareAndSet(this,two,m4));
	assertEquals(m4,a.get(this));
	while(!a.weakCompareAndSet(this,m4,seven));
	assertEquals(seven,a.get(this));
    }

    /**
     * getAndSet returns previous value and sets to given value
     */
    public void testGetAndSet(){
        AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, Integer>a;
        try {
            a = AtomicReferenceFieldUpdater.newUpdater(AtomicReferenceFieldUpdaterTest.class, Integer.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = one;
	assertEquals(one,a.getAndSet(this, zero));
	assertEquals(zero,a.getAndSet(this,m10));
	assertEquals(m10,a.getAndSet(this,1));
    }

}
