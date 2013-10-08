/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

import junit.framework.*;
import java.util.concurrent.atomic.*;
import java.io.*;

public class AtomicLongTest extends JSR166TestCase {
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(AtomicLongTest.class);
    }

    /**
     * constructor initializes to given value
     */
    public void testConstructor(){
        AtomicLong ai = new AtomicLong(1);
	assertEquals(1,ai.get());
    }

    /**
     * default constructed initializes to zero
     */
    public void testConstructor2(){
        AtomicLong ai = new AtomicLong();
	assertEquals(0,ai.get());
    }

    /**
     * get returns the last value set
     */
    public void testGetSet(){
        AtomicLong ai = new AtomicLong(1);
	assertEquals(1,ai.get());
	ai.set(2);
	assertEquals(2,ai.get());
	ai.set(-3);
	assertEquals(-3,ai.get());

    }

    /**
     * compareAndSet succeeds in changing value if equal to expected else fails
     */
    public void testCompareAndSet(){
        AtomicLong ai = new AtomicLong(1);
	assertTrue(ai.compareAndSet(1,2));
	assertTrue(ai.compareAndSet(2,-4));
	assertEquals(-4,ai.get());
	assertFalse(ai.compareAndSet(-5,7));
	assertFalse((7 == ai.get()));
	assertTrue(ai.compareAndSet(-4,7));
	assertEquals(7,ai.get());
    }

    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() {
        final AtomicLong ai = new AtomicLong(1);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!ai.compareAndSet(2, 3)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(ai.compareAndSet(1, 2));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(ai.get(), 3);
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
        AtomicLong ai = new AtomicLong(1);
	while(!ai.weakCompareAndSet(1,2));
	while(!ai.weakCompareAndSet(2,-4));
	assertEquals(-4,ai.get());
	while(!ai.weakCompareAndSet(-4,7));
	assertEquals(7,ai.get());
    }

    /**
     * getAndSet returns previous value and sets to given value
     */
    public void testGetAndSet(){
        AtomicLong ai = new AtomicLong(1);
	assertEquals(1,ai.getAndSet(0));
	assertEquals(0,ai.getAndSet(-10));
	assertEquals(-10,ai.getAndSet(1));
    }

    /**
     * getAndAdd returns previous value and adds given value
     */
    public void testGetAndAdd(){
        AtomicLong ai = new AtomicLong(1);
	assertEquals(1,ai.getAndAdd(2));
	assertEquals(3,ai.get());
	assertEquals(3,ai.getAndAdd(-4));
	assertEquals(-1,ai.get());
    }

    /**
     * getAndDecrement returns previous value and decrements
     */
    public void testGetAndDecrement(){
        AtomicLong ai = new AtomicLong(1);
	assertEquals(1,ai.getAndDecrement());
	assertEquals(0,ai.getAndDecrement());
	assertEquals(-1,ai.getAndDecrement());
    }

    /**
     * getAndIncrement returns previous value and increments
     */
    public void testGetAndIncrement(){
        AtomicLong ai = new AtomicLong(1);
	assertEquals(1,ai.getAndIncrement());
	assertEquals(2,ai.get());
	ai.set(-2);
	assertEquals(-2,ai.getAndIncrement());
	assertEquals(-1,ai.getAndIncrement());
	assertEquals(0,ai.getAndIncrement());
	assertEquals(1,ai.get());
    }

    /**
     * addAndGet adds given value to current, and returns current value
     */
    public void testAddAndGet(){
        AtomicLong ai = new AtomicLong(1);
	assertEquals(3,ai.addAndGet(2));
	assertEquals(3,ai.get());
	assertEquals(-1,ai.addAndGet(-4));
	assertEquals(-1,ai.get());
    }

    /**
     * decrementAndGet decrements and returns current value
     */
    public void testDecrementAndGet(){
        AtomicLong ai = new AtomicLong(1);
	assertEquals(0,ai.decrementAndGet());
	assertEquals(-1,ai.decrementAndGet());
	assertEquals(-2,ai.decrementAndGet());
	assertEquals(-2,ai.get());
    }

    /**
     * incrementAndGet increments and returns current value
     */
    public void testIncrementAndGet(){
        AtomicLong ai = new AtomicLong(1);
	assertEquals(2,ai.incrementAndGet());
	assertEquals(2,ai.get());
	ai.set(-2);
	assertEquals(-1,ai.incrementAndGet());
	assertEquals(0,ai.incrementAndGet());
	assertEquals(1,ai.incrementAndGet());
	assertEquals(1,ai.get());
    }

    /**
     * a deserialized serialized atomic holds same value
     *
     // TODO(tball): enable when serialization is supported.
    public void testSerialization() {
        AtomicLong l = new AtomicLong();

        try {
            l.set(-22);
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(l);
            out.close();

            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            AtomicLong r = (AtomicLong) in.readObject();
            assertEquals(l.get(), r.get());
        } catch(Exception e){
            unexpectedException();
        }
    }
    */

    /**
     * toString returns current value.
     */
    public void testToString() {
        AtomicLong ai = new AtomicLong();
        for (long i = -12; i < 6; ++i) {
            ai.set(i);
            assertEquals(ai.toString(), Long.toString(i));
        }
    }

    /**
     * longValue returns current value.
     */
    public void testLongValue() {
        AtomicLong ai = new AtomicLong();
        for (int i = -12; i < 6; ++i) {
            ai.set(i);
            assertEquals((long)i, ai.longValue());
        }
    }

    /**
     * floatValue returns current value.
     */
    public void testFloatValue() {
        AtomicLong ai = new AtomicLong();
        for (int i = -12; i < 6; ++i) {
            ai.set(i);
            assertEquals((float)i, ai.floatValue());
        }
    }

    /**
     * doubleValue returns current value.
     */
    public void testDoubleValue() {
        AtomicLong ai = new AtomicLong();
        for (int i = -12; i < 6; ++i) {
            ai.set(i);
            assertEquals((double)i, ai.doubleValue());
        }
    }


}
