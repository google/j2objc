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

public class AtomicBooleanTest extends JSR166TestCase {
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(AtomicBooleanTest.class);
    }

    /**
     * constructor initializes to given value
     */
    public void testConstructor() {
        AtomicBoolean ai = new AtomicBoolean(true);
	assertEquals(true,ai.get());
    }

    /**
     * default constructed initializes to false
     */
    public void testConstructor2() {
        AtomicBoolean ai = new AtomicBoolean();
	assertEquals(false,ai.get());
    }

    /**
     * get returns the last value set
     */
    public void testGetSet() {
        AtomicBoolean ai = new AtomicBoolean(true);
	assertEquals(true,ai.get());
	ai.set(false);
	assertEquals(false,ai.get());
	ai.set(true);
	assertEquals(true,ai.get());

    }

    /**
     * compareAndSet succeeds in changing value if equal to expected else fails
     */
    public void testCompareAndSet() {
        AtomicBoolean ai = new AtomicBoolean(true);
	assertTrue(ai.compareAndSet(true,false));
	assertEquals(false,ai.get());
	assertTrue(ai.compareAndSet(false,false));
	assertEquals(false,ai.get());
	assertFalse(ai.compareAndSet(true,false));
	assertFalse((ai.get()));
	assertTrue(ai.compareAndSet(false,true));
	assertEquals(true,ai.get());
    }

    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
    public void testCompareAndSetInMultipleThreads() {
        final AtomicBoolean ai = new AtomicBoolean(true);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!ai.compareAndSet(false, true)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(ai.compareAndSet(true, false));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch(Exception e) {
            unexpectedException();
        }
    }

    /**
     * repeated weakCompareAndSet succeeds in changing value when equal
     * to expected
     */
    public void testWeakCompareAndSet() {
        AtomicBoolean ai = new AtomicBoolean(true);
	while(!ai.weakCompareAndSet(true,false));
	assertEquals(false,ai.get());
	while(!ai.weakCompareAndSet(false,false));
        assertEquals(false,ai.get());
        while(!ai.weakCompareAndSet(false,true));
	assertEquals(true,ai.get());
    }

    /**
     * getAndSet returns previous value and sets to given value
     */
    public void testGetAndSet() {
        AtomicBoolean ai = new AtomicBoolean(true);
	assertEquals(true,ai.getAndSet(false));
	assertEquals(false,ai.getAndSet(false));
	assertEquals(false,ai.getAndSet(true));
	assertEquals(true,ai.get());
    }

    /**
     * a deserialized serialized atomic holds same value
     *
     // TODO(tball): enable when serialization is supported.
    public void testSerialization() {
        AtomicBoolean l = new AtomicBoolean();

        try {
            l.set(true);
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(l);
            out.close();

            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            AtomicBoolean r = (AtomicBoolean) in.readObject();
            assertEquals(l.get(), r.get());
        } catch(Exception e){
            e.printStackTrace();
            unexpectedException();
        }
    }
    */

    /**
     * toString returns current value.
     */
    public void testToString() {
        AtomicBoolean ai = new AtomicBoolean();
        assertEquals(ai.toString(), Boolean.toString(false));
        ai.set(true);
        assertEquals(ai.toString(), Boolean.toString(true));
    }

}
