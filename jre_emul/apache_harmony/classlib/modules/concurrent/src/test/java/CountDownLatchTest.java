/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes, 
 * Pat Fisher, Mike Judd. 
 */

import junit.framework.*;
import java.util.*;
import java.util.concurrent.*;

public class CountDownLatchTest extends JSR166TestCase {
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());	
    }
    public static Test suite() {
	return new TestSuite(CountDownLatchTest.class);
    }

    /**
     * negative constructor argument throws IAE
     */
    public void testConstructor() {
        try {
            new CountDownLatch(-1);
            shouldThrow();
        } catch(IllegalArgumentException success){}
    }

    /**
     * getCount returns initial count and decreases after countDown
     */
    public void testGetCount() {
	final CountDownLatch l = new CountDownLatch(2);
	assertEquals(2, l.getCount());
	l.countDown();
	assertEquals(1, l.getCount());
    }

    /**
     * countDown decrements count when positive and has no effect when zero
     */
    public void testCountDown() {
	final CountDownLatch l = new CountDownLatch(1);
	assertEquals(1, l.getCount());
	l.countDown();
	assertEquals(0, l.getCount());
	l.countDown();
	assertEquals(0, l.getCount());
    }

    /**
     * await returns after countDown to zero, but not before
     */
    public void testAwait() {
	final CountDownLatch l = new CountDownLatch(2);

	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
                        threadAssertTrue(l.getCount() > 0);
			l.await();
                        threadAssertTrue(l.getCount() == 0);
		    } catch(InterruptedException e){
                        threadUnexpectedException();
                    }
		}
	    });
	t.start();
	try {
            assertEquals(l.getCount(), 2);
            Thread.sleep(SHORT_DELAY_MS);
            l.countDown();
            assertEquals(l.getCount(), 1);
            l.countDown();
            assertEquals(l.getCount(), 0);
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }
    

    /**
     * timed await returns after countDown to zero
     */
    public void testTimedAwait() {
	final CountDownLatch l = new CountDownLatch(2);

	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
                        threadAssertTrue(l.getCount() > 0);
			threadAssertTrue(l.await(SMALL_DELAY_MS, TimeUnit.MILLISECONDS));
		    } catch(InterruptedException e){
                        threadUnexpectedException();
                    }
		}
	    });
	t.start();
	try {
            assertEquals(l.getCount(), 2);
            Thread.sleep(SHORT_DELAY_MS);
            l.countDown();
            assertEquals(l.getCount(), 1);
            l.countDown();
            assertEquals(l.getCount(), 0);
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }
    
    /**
     * await throws IE if interrupted before counted down
     */
    public void testAwait_InterruptedException() {
        final CountDownLatch l = new CountDownLatch(1);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertTrue(l.getCount() > 0);
                        l.await();
                        threadShouldThrow();
                    } catch(InterruptedException success){}
                }
            });
	t.start();
	try {
            assertEquals(l.getCount(), 1);
            t.interrupt();
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * timed await throws IE if interrupted before counted down
     */
    public void testTimedAwait_InterruptedException() {
        final CountDownLatch l = new CountDownLatch(1);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertTrue(l.getCount() > 0);
                        l.await(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();                        
                    } catch(InterruptedException success){}
                }
            });
        t.start();
        try {
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(l.getCount(), 1);
            t.interrupt();
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * timed await times out if not counted down before timeout
     */
    public void testAwaitTimeout() {
        final CountDownLatch l = new CountDownLatch(1);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertTrue(l.getCount() > 0);
                        threadAssertFalse(l.await(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        threadAssertTrue(l.getCount() > 0);
                    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
                }
            });
        t.start();
        try {
            assertEquals(l.getCount(), 1);
            t.join();
        } catch (InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * toString indicates current count
     */
    public void testToString() {
        CountDownLatch s = new CountDownLatch(2);
        String us = s.toString();
        assertTrue(us.indexOf("Count = 2") >= 0);
        s.countDown();
        String s1 = s.toString();
        assertTrue(s1.indexOf("Count = 1") >= 0);
        s.countDown();
        String s2 = s.toString();
        assertTrue(s2.indexOf("Count = 0") >= 0);
    }

}
