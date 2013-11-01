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
import java.util.concurrent.locks.*;

public class LockSupportTest extends JSR166TestCase{
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());	
    }
    public static Test suite() {
	return new TestSuite(LockSupportTest.class);
    }

    /**
     * park is released by unpark occurring after park
     */
    public void testPark() { 
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			LockSupport.park();
		    } catch(Exception e){
                        threadUnexpectedException();
                    }
		}
	    });
	try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            LockSupport.unpark(t);
            t.join();
	}
	catch(Exception e) {
            unexpectedException();
        }
    }

    /**
     * park is released by unpark occurring before park
     */
    public void testPark2() { 
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
                        Thread.sleep(SHORT_DELAY_MS);
			LockSupport.park();
		    } catch(Exception e){
                        threadUnexpectedException();
                    }
		}
	    });
	try {
            t.start();
            LockSupport.unpark(t);
            t.join();
	}
	catch(Exception e) {
            unexpectedException();
        }
    }

    /**
     * park is released by interrupt 
     */
    public void testPark3() { 
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			LockSupport.park();
		    } catch(Exception e){
                        threadUnexpectedException();
                    }
		}
	    });
	try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
	}
	catch(Exception e) {
            unexpectedException();
        }
    }

    /**
     * park returns if interrupted before park
     */
    public void testPark4() { 
        final ReentrantLock lock = new ReentrantLock();
        lock.lock();
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
                        lock.lock();
			LockSupport.park();
		    } catch(Exception e){
                        threadUnexpectedException();
                    }
		}
	    });
	try {
            t.start();
            t.interrupt();
            lock.unlock();
            t.join();
	}
	catch(Exception e) {
            unexpectedException();
        }
    }

    /**
     * parkNanos times out if not unparked
     */
    public void testParkNanos() { 
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			LockSupport.parkNanos(1000);
		    } catch(Exception e){
                        threadUnexpectedException();
                    }
		}
	    });
	try {
            t.start();
            t.join();
	}
	catch(Exception e) {
            unexpectedException();
        }
    }


    /**
     * parkUntil times out if not unparked
     */
    public void testParkUntil() { 
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
                        long d = new Date().getTime() + 100;
			LockSupport.parkUntil(d);
		    } catch(Exception e){
                        threadUnexpectedException();
                    }
		}
	    });
	try {
            t.start();
            t.join();
	}
	catch(Exception e) {
            unexpectedException();
        }
    }
}
