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

public class AtomicLongFieldUpdaterTest extends JSR166TestCase {
    volatile long x = 0;
    int z;
    long w;

    public static void main(String[] args){
        junit.textui.TestRunner.run(suite());
    }
    public static Test suite() {
        return new TestSuite(AtomicLongFieldUpdaterTest.class);
    }

    /**
     * Construction with non-existent field throws RuntimeException
     */
    public void testConstructor(){
        try{
            AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> 
                a = AtomicLongFieldUpdater.newUpdater
                (AtomicLongFieldUpdaterTest.class, "y");
            shouldThrow();
        }
        catch (RuntimeException rt) {}
    }

    /**
     * construction with field not of given type throws RuntimeException
     */
    public void testConstructor2(){
        try{
            AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> 
                a = AtomicLongFieldUpdater.newUpdater
                (AtomicLongFieldUpdaterTest.class, "z");
            shouldThrow();
        }
        catch (RuntimeException rt) {}
    }

    /**
     * construction with non-volatile field throws RuntimeException
     */
    public void testConstructor3(){
        try{
            AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> 
                a = AtomicLongFieldUpdater.newUpdater
                (AtomicLongFieldUpdaterTest.class, "w");
            shouldThrow();
        }

        catch (RuntimeException rt) {}
    }

    static class Base {
        protected volatile long f = 0;
    }
    static class Sub1 extends Base {
        AtomicLongFieldUpdater<Base> fUpdater
                = AtomicLongFieldUpdater.newUpdater(Base.class, "f");
    }
    static class Sub2 extends Base {}

    public void testProtectedFieldOnAnotherSubtype() {
        Sub1 sub1 = new Sub1();
        Sub2 sub2 = new Sub2();

        sub1.fUpdater.set(sub1, 1);
        try {
            sub1.fUpdater.set(sub2, 2);
            shouldThrow();
        }
        catch (RuntimeException rt) {}
    }

    /**
     *  get returns the last value set or assigned
     */
    public void testGetSet(){
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        try {
            a = AtomicLongFieldUpdater.newUpdater(AtomicLongFieldUpdaterTest.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = 1;
	assertEquals(1,a.get(this));
	a.set(this,2);
	assertEquals(2,a.get(this));
	a.set(this,-3);
	assertEquals(-3,a.get(this));

    }
    /**
     * compareAndSet succeeds in changing value if equal to expected else fails
     */
    public void testCompareAndSet(){
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        try {
            a = AtomicLongFieldUpdater.newUpdater(AtomicLongFieldUpdaterTest.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = 1;
	assertTrue(a.compareAndSet(this,1,2));
	assertTrue(a.compareAndSet(this,2,-4));
	assertEquals(-4,a.get(this));
	assertFalse(a.compareAndSet(this,-5,7));
	assertFalse((7 == a.get(this)));
	assertTrue(a.compareAndSet(this,-4,7));
	assertEquals(7,a.get(this));
    }


    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() {
        x = 1;
        final AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest>a;
        try {
            a = AtomicLongFieldUpdater.newUpdater(AtomicLongFieldUpdaterTest.class, "x");
        } catch (RuntimeException ok) {
            return;
        }

        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!a.compareAndSet(AtomicLongFieldUpdaterTest.this, 2, 3)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(a.compareAndSet(this, 1, 2));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(a.get(this), 3);
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
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        try {
            a = AtomicLongFieldUpdater.newUpdater(AtomicLongFieldUpdaterTest.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = 1;
	while(!a.weakCompareAndSet(this,1,2));
        while(!a.weakCompareAndSet(this,2,-4));
	assertEquals(-4,a.get(this));
	while(!a.weakCompareAndSet(this,-4,7));
	assertEquals(7,a.get(this));
    }

    /**
     *  getAndSet returns previous value and sets to given value
     */
    public void testGetAndSet(){
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        try {
            a = AtomicLongFieldUpdater.newUpdater(AtomicLongFieldUpdaterTest.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = 1;
	assertEquals(1,a.getAndSet(this, 0));
	assertEquals(0,a.getAndSet(this,-10));
	assertEquals(-10,a.getAndSet(this,1));
    }

    /**
     * getAndAdd returns previous value and adds given value
     */
    public void testGetAndAdd(){
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        try {
            a = AtomicLongFieldUpdater.newUpdater(AtomicLongFieldUpdaterTest.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = 1;
	assertEquals(1,a.getAndAdd(this,2));
	assertEquals(3,a.get(this));
	assertEquals(3,a.getAndAdd(this,-4));
	assertEquals(-1,a.get(this));
    }

    /**
     * getAndDecrement returns previous value and decrements
     */
    public void testGetAndDecrement(){
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        try {
            a = AtomicLongFieldUpdater.newUpdater(AtomicLongFieldUpdaterTest.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = 1;
	assertEquals(1,a.getAndDecrement(this));
	assertEquals(0,a.getAndDecrement(this));
	assertEquals(-1,a.getAndDecrement(this));
    }

    /**
     * getAndIncrement returns previous value and increments
     */
    public void testGetAndIncrement(){
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        try {
            a = AtomicLongFieldUpdater.newUpdater(AtomicLongFieldUpdaterTest.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = 1;
	assertEquals(1,a.getAndIncrement(this));
	assertEquals(2,a.get(this));
	a.set(this,-2);
	assertEquals(-2,a.getAndIncrement(this));
	assertEquals(-1,a.getAndIncrement(this));
	assertEquals(0,a.getAndIncrement(this));
	assertEquals(1,a.get(this));
    }

    /**
     * addAndGet adds given value to current, and returns current value
     */
    public void testAddAndGet(){
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        try {
            a = AtomicLongFieldUpdater.newUpdater(AtomicLongFieldUpdaterTest.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = 1;
	assertEquals(3,a.addAndGet(this,2));
	assertEquals(3,a.get(this));
	assertEquals(-1,a.addAndGet(this,-4));
	assertEquals(-1,a.get(this));
    }

    /**
     *  decrementAndGet decrements and returns current value
     */
    public void testDecrementAndGet(){
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        try {
            a = AtomicLongFieldUpdater.newUpdater(AtomicLongFieldUpdaterTest.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = 1;
	assertEquals(0,a.decrementAndGet(this));
	assertEquals(-1,a.decrementAndGet(this));
	assertEquals(-2,a.decrementAndGet(this));
	assertEquals(-2,a.get(this));
    }

    /**
     * incrementAndGet increments and returns current value
     */
    public void testIncrementAndGet(){
        AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> a;
        try {
            a = AtomicLongFieldUpdater.newUpdater(AtomicLongFieldUpdaterTest.class, "x");
        } catch (RuntimeException ok) {
            return;
        }
        x = 1;
	assertEquals(2,a.incrementAndGet(this));
	assertEquals(2,a.get(this));
	a.set(this,-2);
	assertEquals(-1,a.incrementAndGet(this));
	assertEquals(0,a.incrementAndGet(this));
	assertEquals(1,a.incrementAndGet(this));
	assertEquals(1,a.get(this));
    }

}
