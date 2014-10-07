/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

import junit.framework.*;

public class SystemTest extends JSR166TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());   
    }
    
    public static Test suite() {
        return new TestSuite(SystemTest.class);
    }

    /** 
     * Worst case rounding for millisecs; set for 60 cycle millis clock.
     * This value might need to be changed os JVMs with coarser
     *  System.currentTimeMillis clocks.
     */
    static final long MILLIS_ROUND = 17;

    /**
     * Nanos between readings of millis is no longer than millis (plus
     * possible rounding).
     * This shows only that nano timing not (much) worse than milli.
     */
    public void testNanoTime1() {
        try {
            long m1 = System.currentTimeMillis();
            Thread.sleep(1);
            long n1 = System.nanoTime();
            Thread.sleep(SHORT_DELAY_MS);
            long n2 = System.nanoTime();
            Thread.sleep(1);
            long m2 = System.currentTimeMillis();
            long millis = m2 - m1;
            long nanos = n2 - n1;
            assertTrue(nanos >= 0);
            long nanosAsMillis = nanos / 1000000;
            assertTrue(nanosAsMillis <= millis + MILLIS_ROUND);
        }
        catch(InterruptedException ie) {
            unexpectedException();
        }
    }

    /**
     * Millis between readings of nanos is less than nanos, adjusting
     * for rounding.
     * This shows only that nano timing not (much) worse than milli.
     */
    public void testNanoTime2() {
        try {
            long n1 = System.nanoTime();
            Thread.sleep(1);
            long m1 = System.currentTimeMillis();
            Thread.sleep(SHORT_DELAY_MS);
            long m2 = System.currentTimeMillis();
            Thread.sleep(1);
            long n2 = System.nanoTime();
            long millis = m2 - m1;
            long nanos = n2 - n1;
            
            assertTrue(nanos >= 0);
            long nanosAsMillis = nanos / 1000000;
            assertTrue(millis <= nanosAsMillis + MILLIS_ROUND);
        }
        catch(InterruptedException ie) {
            unexpectedException();
        }
    }

}

