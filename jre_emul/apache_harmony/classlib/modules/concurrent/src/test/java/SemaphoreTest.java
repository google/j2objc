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
import java.io.*;

public class SemaphoreTest extends JSR166TestCase {
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
	return new TestSuite(SemaphoreTest.class);
    }

    /**
     * Subclass to expose protected methods
     */
    static class PublicSemaphore extends Semaphore {
        PublicSemaphore(int p, boolean f) { super(p, f); }
        public Collection<Thread> getQueuedThreads() {
            return super.getQueuedThreads();
        }
        public void reducePermits(int p) {
            super.reducePermits(p);
        }
    }

    /**
     * A runnable calling acquire
     */
    class InterruptibleLockRunnable implements Runnable {
        final Semaphore lock;
        InterruptibleLockRunnable(Semaphore l) { lock = l; }
        public void run() {
            try {
                lock.acquire();
            } catch(InterruptedException success){}
        }
    }


    /**
     * A runnable calling acquire that expects to be
     * interrupted
     */
    class InterruptedLockRunnable implements Runnable {
        final Semaphore lock;
        InterruptedLockRunnable(Semaphore l) { lock = l; }
        public void run() {
            try {
                lock.acquire();
                threadShouldThrow();
            } catch(InterruptedException success){}
        }
    }

    /**
     * Zero, negative, and positive initial values are allowed in constructor
     */
    public void testConstructor() {
        Semaphore s0 = new Semaphore(0, false);
        assertEquals(0, s0.availablePermits());
        assertFalse(s0.isFair());
        Semaphore s1 = new Semaphore(-1, false);
        assertEquals(-1, s1.availablePermits());
        assertFalse(s1.isFair());
        Semaphore s2 = new Semaphore(-1, false);
        assertEquals(-1, s2.availablePermits());
        assertFalse(s2.isFair());
    }

    /**
     * Constructor without fairness argument behaves as nonfair
     */
    public void testConstructor2() {
        Semaphore s0 = new Semaphore(0);
        assertEquals(0, s0.availablePermits());
        assertFalse(s0.isFair());
        Semaphore s1 = new Semaphore(-1);
        assertEquals(-1, s1.availablePermits());
        assertFalse(s1.isFair());
        Semaphore s2 = new Semaphore(-1);
        assertEquals(-1, s2.availablePermits());
        assertFalse(s2.isFair());
    }

    /**
     * tryAcquire succeeds when sufficient permits, else fails
     */
    public void testTryAcquireInSameThread() {
        Semaphore s = new Semaphore(2, false);
        assertEquals(2, s.availablePermits());
        assertTrue(s.tryAcquire());
        assertTrue(s.tryAcquire());
        assertEquals(0, s.availablePermits());
        assertFalse(s.tryAcquire());
    }

    /**
     * Acquire and release of semaphore succeed if initially available
     */
    public void testAcquireReleaseInSameThread() {
        Semaphore s = new Semaphore(1, false);
        try {
            s.acquire();
            s.release();
            s.acquire();
            s.release();
            s.acquire();
            s.release();
            s.acquire();
            s.release();
            s.acquire();
            s.release();
            assertEquals(1, s.availablePermits());
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * Uninterruptible acquire and release of semaphore succeed if
     * initially available
     */
    public void testAcquireUninterruptiblyReleaseInSameThread() {
        Semaphore s = new Semaphore(1, false);
        try {
            s.acquireUninterruptibly();
            s.release();
            s.acquireUninterruptibly();
            s.release();
            s.acquireUninterruptibly();
            s.release();
            s.acquireUninterruptibly();
            s.release();
            s.acquireUninterruptibly();
            s.release();
            assertEquals(1, s.availablePermits());
	} finally {
        }
    }

    /**
     * Timed Acquire and release of semaphore succeed if
     * initially available
     */
    public void testTimedAcquireReleaseInSameThread() {
        Semaphore s = new Semaphore(1, false);
        try {
            assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release();
            assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release();
            assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release();
            assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release();
            assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release();
            assertEquals(1, s.availablePermits());
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * A release in one thread enables an acquire in another thread
     */
    public void testAcquireReleaseInDifferentThreads() {
        final Semaphore s = new Semaphore(0, false);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.acquire();
                        s.release();
                        s.release();
                        s.acquire();
		    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
		}
	    });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            s.release();
            s.release();
            s.acquire();
            s.acquire();
            s.release();
            t.join();
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * A release in one thread enables an uninterruptible acquire in another thread
     */
    public void testUninterruptibleAcquireReleaseInDifferentThreads() {
        final Semaphore s = new Semaphore(0, false);
	Thread t = new Thread(new Runnable() {
		public void run() {
                    s.acquireUninterruptibly();
                    s.release();
                    s.release();
                    s.acquireUninterruptibly();
		}
	    });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            s.release();
            s.release();
            s.acquireUninterruptibly();
            s.acquireUninterruptibly();
            s.release();
            t.join();
	} catch( InterruptedException e){
            unexpectedException();
        }
    }


    /**
     *  A release in one thread enables a timed acquire in another thread
     */
    public void testTimedAcquireReleaseInDifferentThreads() {
        final Semaphore s = new Semaphore(1, false);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
                        s.release();
                        threadAssertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        s.release();
                        threadAssertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));

		    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
		}
	    });
        try {
            t.start();
            assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release();
            assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release();
            s.release();
            t.join();
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * A waiting acquire blocks interruptibly
     */
    public void testAcquire_InterruptedException() {
	final Semaphore s = new Semaphore(0, false);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.acquire();
			threadShouldThrow();
		    } catch(InterruptedException success){}
		}
	    });
	t.start();
	try {
	    Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    /**
     *  A waiting timed acquire blocks interruptibly
     */
    public void testTryAcquire_InterruptedException() {
	final Semaphore s = new Semaphore(0, false);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.tryAcquire(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
			threadShouldThrow();
		    } catch(InterruptedException success){
                    }
		}
	    });
	t.start();
	try {
	    Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * hasQueuedThreads reports whether there are waiting threads
     */
    public void testHasQueuedThreads() {
	final Semaphore lock = new Semaphore(1, false);
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        try {
            assertFalse(lock.hasQueuedThreads());
            lock.acquireUninterruptibly();
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.hasQueuedThreads());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.hasQueuedThreads());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.hasQueuedThreads());
            lock.release();
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(lock.hasQueuedThreads());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * getQueueLength reports number of waiting threads
     */
    public void testGetQueueLength() {
	final Semaphore lock = new Semaphore(1, false);
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        try {
            assertEquals(0, lock.getQueueLength());
            lock.acquireUninterruptibly();
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(1, lock.getQueueLength());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(2, lock.getQueueLength());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(1, lock.getQueueLength());
            lock.release();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(0, lock.getQueueLength());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * getQueuedThreads includes waiting threads
     */
    public void testGetQueuedThreads() {
	final PublicSemaphore lock = new PublicSemaphore(1, false);
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        try {
            assertTrue(lock.getQueuedThreads().isEmpty());
            lock.acquireUninterruptibly();
            assertTrue(lock.getQueuedThreads().isEmpty());
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.getQueuedThreads().contains(t1));
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.getQueuedThreads().contains(t1));
            assertTrue(lock.getQueuedThreads().contains(t2));
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(lock.getQueuedThreads().contains(t1));
            assertTrue(lock.getQueuedThreads().contains(t2));
            lock.release();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.getQueuedThreads().isEmpty());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * drainPermits reports and removes given number of permits
     */
    public void testDrainPermits() {
        Semaphore s = new Semaphore(0, false);
        assertEquals(0, s.availablePermits());
        assertEquals(0, s.drainPermits());
        s.release(10);
        assertEquals(10, s.availablePermits());
        assertEquals(10, s.drainPermits());
        assertEquals(0, s.availablePermits());
        assertEquals(0, s.drainPermits());
    }

    /**
     * reducePermits reduces number of permits
     */
    public void testReducePermits() {
        PublicSemaphore s = new PublicSemaphore(10, false);
        assertEquals(10, s.availablePermits());
        s.reducePermits(1);
        assertEquals(9, s.availablePermits());
        s.reducePermits(10);
        assertEquals(-1, s.availablePermits());
    }

    /**
     * a deserialized serialized semaphore has same number of permits
     *
    TODO(tball): enable when serialization is supported.
    public void testSerialization() {
        Semaphore l = new Semaphore(3, false);
        try {
            l.acquire();
            l.release();
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(l);
            out.close();

            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            Semaphore r = (Semaphore) in.readObject();
            assertEquals(3, r.availablePermits());
            assertFalse(r.isFair());
            r.acquire();
            r.release();
        } catch(Exception e){
            unexpectedException();
        }
    }*/


    /**
     * Zero, negative, and positive initial values are allowed in constructor
     */
    public void testConstructor_fair() {
        Semaphore s0 = new Semaphore(0, true);
        assertEquals(0, s0.availablePermits());
        assertTrue(s0.isFair());
        Semaphore s1 = new Semaphore(-1, true);
        assertEquals(-1, s1.availablePermits());
        Semaphore s2 = new Semaphore(-1, true);
        assertEquals(-1, s2.availablePermits());
    }

    /**
     * tryAcquire succeeds when sufficient permits, else fails
     */
    public void testTryAcquireInSameThread_fair() {
        Semaphore s = new Semaphore(2, true);
        assertEquals(2, s.availablePermits());
        assertTrue(s.tryAcquire());
        assertTrue(s.tryAcquire());
        assertEquals(0, s.availablePermits());
        assertFalse(s.tryAcquire());
    }

    /**
     * tryAcquire(n) succeeds when sufficient permits, else fails
     */
    public void testTryAcquireNInSameThread_fair() {
        Semaphore s = new Semaphore(2, true);
        assertEquals(2, s.availablePermits());
        assertTrue(s.tryAcquire(2));
        assertEquals(0, s.availablePermits());
        assertFalse(s.tryAcquire());
    }

    /**
     * Acquire and release of semaphore succeed if initially available
     */
    public void testAcquireReleaseInSameThread_fair() {
        Semaphore s = new Semaphore(1, true);
        try {
            s.acquire();
            s.release();
            s.acquire();
            s.release();
            s.acquire();
            s.release();
            s.acquire();
            s.release();
            s.acquire();
            s.release();
            assertEquals(1, s.availablePermits());
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * Acquire(n) and release(n) of semaphore succeed if initially available
     */
    public void testAcquireReleaseNInSameThread_fair() {
        Semaphore s = new Semaphore(1, true);
        try {
            s.release(1);
            s.acquire(1);
            s.release(2);
            s.acquire(2);
            s.release(3);
            s.acquire(3);
            s.release(4);
            s.acquire(4);
            s.release(5);
            s.acquire(5);
            assertEquals(1, s.availablePermits());
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * Acquire(n) and release(n) of semaphore succeed if initially available
     */
    public void testAcquireUninterruptiblyReleaseNInSameThread_fair() {
        Semaphore s = new Semaphore(1, true);
        try {
            s.release(1);
            s.acquireUninterruptibly(1);
            s.release(2);
            s.acquireUninterruptibly(2);
            s.release(3);
            s.acquireUninterruptibly(3);
            s.release(4);
            s.acquireUninterruptibly(4);
            s.release(5);
            s.acquireUninterruptibly(5);
            assertEquals(1, s.availablePermits());
	} finally {
        }
    }

    /**
     * release(n) in one thread enables timed acquire(n) in another thread
     */
    public void testTimedAcquireReleaseNInSameThread_fair() {
        Semaphore s = new Semaphore(1, true);
        try {
            s.release(1);
            assertTrue(s.tryAcquire(1, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release(2);
            assertTrue(s.tryAcquire(2, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release(3);
            assertTrue(s.tryAcquire(3, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release(4);
            assertTrue(s.tryAcquire(4, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release(5);
            assertTrue(s.tryAcquire(5, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            assertEquals(1, s.availablePermits());
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * release in one thread enables timed acquire in another thread
     */
    public void testTimedAcquireReleaseInSameThread_fair() {
        Semaphore s = new Semaphore(1, true);
        try {
            assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release();
            assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release();
            assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release();
            assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release();
            assertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release();
            assertEquals(1, s.availablePermits());
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * A release in one thread enables an acquire in another thread
     */
    public void testAcquireReleaseInDifferentThreads_fair() {
        final Semaphore s = new Semaphore(0, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.acquire();
                        s.acquire();
                        s.acquire();
                        s.acquire();
		    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
		}
	    });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            s.release();
            s.release();
            s.release();
            s.release();
            s.release();
            s.release();
            t.join();
            assertEquals(2, s.availablePermits());
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * release(n) in one thread enables acquire(n) in another thread
     */
    public void testAcquireReleaseNInDifferentThreads_fair() {
        final Semaphore s = new Semaphore(0, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.acquire();
                        s.release(2);
                        s.acquire();
		    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
		}
	    });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            s.release(2);
            s.acquire(2);
            s.release(1);
            t.join();
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * release(n) in one thread enables acquire(n) in another thread
     */
    public void testAcquireReleaseNInDifferentThreads_fair2() {
        final Semaphore s = new Semaphore(0, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
                        s.acquire(2);
                        s.acquire(2);
                        s.release(4);
		    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
		}
	    });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            s.release(6);
            s.acquire(2);
            s.acquire(2);
            s.release(2);
            t.join();
	} catch( InterruptedException e){
            unexpectedException();
        }
    }





    /**
     * release in one thread enables timed acquire in another thread
     */
    public void testTimedAcquireReleaseInDifferentThreads_fair() {
        final Semaphore s = new Semaphore(1, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
                        threadAssertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        threadAssertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        threadAssertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        threadAssertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        threadAssertTrue(s.tryAcquire(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));

		    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
		}
	    });
	t.start();
        try {
            s.release();
            s.release();
            s.release();
            s.release();
            s.release();
            t.join();
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * release(n) in one thread enables timed acquire(n) in another thread
     */
    public void testTimedAcquireReleaseNInDifferentThreads_fair() {
        final Semaphore s = new Semaphore(2, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
                        threadAssertTrue(s.tryAcquire(2, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        s.release(2);
                        threadAssertTrue(s.tryAcquire(2, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        s.release(2);
		    } catch(InterruptedException ie){
                        threadUnexpectedException();
                    }
		}
	    });
	t.start();
        try {
            assertTrue(s.tryAcquire(2, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release(2);
            assertTrue(s.tryAcquire(2, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            s.release(2);
            t.join();
	} catch( InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * A waiting acquire blocks interruptibly
     */
    public void testAcquire_InterruptedException_fair() {
	final Semaphore s = new Semaphore(0, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.acquire();
			threadShouldThrow();
		    } catch(InterruptedException success){}
		}
	    });
	t.start();
	try {
	    Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * A waiting acquire(n) blocks interruptibly
     */
    public void testAcquireN_InterruptedException_fair() {
	final Semaphore s = new Semaphore(2, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.acquire(3);
			threadShouldThrow();
		    } catch(InterruptedException success){}
		}
	    });
	t.start();
	try {
	    Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    /**
     *  A waiting tryAcquire blocks interruptibly
     */
    public void testTryAcquire_InterruptedException_fair() {
	final Semaphore s = new Semaphore(0, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.tryAcquire(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
			threadShouldThrow();
		    } catch(InterruptedException success){
                    }
		}
	    });
	t.start();
	try {
	    Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    /**
     *  A waiting tryAcquire(n) blocks interruptibly
     */
    public void testTryAcquireN_InterruptedException_fair() {
	final Semaphore s = new Semaphore(1, true);
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			s.tryAcquire(4, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
			threadShouldThrow();
		    } catch(InterruptedException success){
                    }
		}
	    });
	t.start();
	try {
	    Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * getQueueLength reports number of waiting threads
     */
    public void testGetQueueLength_fair() {
	final Semaphore lock = new Semaphore(1, true);
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        try {
            assertEquals(0, lock.getQueueLength());
            lock.acquireUninterruptibly();
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(1, lock.getQueueLength());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(2, lock.getQueueLength());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(1, lock.getQueueLength());
            lock.release();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(0, lock.getQueueLength());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    }


    /**
     * a deserialized serialized semaphore has same number of permits
     *
    TODO(tball): enable when serialization is supported.
    public void testSerialization_fair() {
        Semaphore l = new Semaphore(3, true);

        try {
            l.acquire();
            l.release();
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(l);
            out.close();

            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            Semaphore r = (Semaphore) in.readObject();
            assertEquals(3, r.availablePermits());
            assertTrue(r.isFair());
            r.acquire();
            r.release();
        } catch(Exception e){
            unexpectedException();
        }
    }
    */

    /**
     * toString indicates current number of permits
     */
    public void testToString() {
        Semaphore s = new Semaphore(0);
        String us = s.toString();
        assertTrue(us.indexOf("Permits = 0") >= 0);
        s.release();
        String s1 = s.toString();
        assertTrue(s1.indexOf("Permits = 1") >= 0);
        s.release();
        String s2 = s.toString();
        assertTrue(s2.indexOf("Permits = 2") >= 0);
    }

}
