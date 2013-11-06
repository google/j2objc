/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

import junit.framework.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.*;
import java.util.*;
import java.io.*;

public class ReentrantLockTest extends JSR166TestCase {
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
	return new TestSuite(ReentrantLockTest.class);
    }

    /**
     * A runnable calling lockInterruptibly
     */
    class InterruptibleLockRunnable implements Runnable {
        final ReentrantLock lock;
        InterruptibleLockRunnable(ReentrantLock l) { lock = l; }
        public void run() {
            try {
                lock.lockInterruptibly();
            } catch(InterruptedException success){}
        }
    }


    /**
     * A runnable calling lockInterruptibly that expects to be
     * interrupted
     */
    class InterruptedLockRunnable implements Runnable {
        final ReentrantLock lock;
        InterruptedLockRunnable(ReentrantLock l) { lock = l; }
        public void run() {
            try {
                lock.lockInterruptibly();
                threadShouldThrow();
            } catch(InterruptedException success){}
        }
    }

    /**
     * Subclass to expose protected methods
     */
    static class PublicReentrantLock extends ReentrantLock {
        PublicReentrantLock() { super(); }
        public Collection<Thread> getQueuedThreads() {
            return super.getQueuedThreads();
        }
        public Collection<Thread> getWaitingThreads(Condition c) {
            return super.getWaitingThreads(c);
        }


    }

    /**
     * Constructor sets given fairness
     */
    public void testConstructor() {
	ReentrantLock rl = new ReentrantLock();
        assertFalse(rl.isFair());
	ReentrantLock r2 = new ReentrantLock(true);
        assertTrue(r2.isFair());
    }

    /**
     * locking an unlocked lock succeeds
     */
    public void testLock() {
	ReentrantLock rl = new ReentrantLock();
        rl.lock();
        assertTrue(rl.isLocked());
        rl.unlock();
    }

    /**
     * locking an unlocked fair lock succeeds
     */
    public void testFairLock() {
	ReentrantLock rl = new ReentrantLock(true);
        rl.lock();
        assertTrue(rl.isLocked());
        rl.unlock();
    }

    /**
     * Unlocking an unlocked lock throws IllegalMonitorStateException
     */
    public void testUnlock_IllegalMonitorStateException() {
	ReentrantLock rl = new ReentrantLock();
	try {
	    rl.unlock();
	    shouldThrow();

	} catch(IllegalMonitorStateException success){}
    }

    /**
     * tryLock on an unlocked lock succeeds
     */
    public void testTryLock() {
	ReentrantLock rl = new ReentrantLock();
        assertTrue(rl.tryLock());
        assertTrue(rl.isLocked());
        rl.unlock();
    }


    /**
     * hasQueuedThreads reports whether there are waiting threads
     */
    public void testhasQueuedThreads() {
	final ReentrantLock lock = new ReentrantLock();
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        try {
            assertFalse(lock.hasQueuedThreads());
            lock.lock();
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.hasQueuedThreads());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.hasQueuedThreads());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.hasQueuedThreads());
            lock.unlock();
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
	final ReentrantLock lock = new ReentrantLock();
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        try {
            assertEquals(0, lock.getQueueLength());
            lock.lock();
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(1, lock.getQueueLength());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(2, lock.getQueueLength());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(1, lock.getQueueLength());
            lock.unlock();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(0, lock.getQueueLength());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * getQueueLength reports number of waiting threads
     */
    public void testGetQueueLength_fair() {
	final ReentrantLock lock = new ReentrantLock(true);
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        try {
            assertEquals(0, lock.getQueueLength());
            lock.lock();
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(1, lock.getQueueLength());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(2, lock.getQueueLength());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(1, lock.getQueueLength());
            lock.unlock();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(0, lock.getQueueLength());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * hasQueuedThread(null) throws NPE
     */
    public void testHasQueuedThreadNPE() {
	final ReentrantLock sync = new ReentrantLock();
        try {
            sync.hasQueuedThread(null);
            shouldThrow();
        } catch (NullPointerException success) {
        }
    }

    /**
     * hasQueuedThread reports whether a thread is queued.
     */
    public void testHasQueuedThread() {
	final ReentrantLock sync = new ReentrantLock();
        Thread t1 = new Thread(new InterruptedLockRunnable(sync));
        Thread t2 = new Thread(new InterruptibleLockRunnable(sync));
        try {
            assertFalse(sync.hasQueuedThread(t1));
            assertFalse(sync.hasQueuedThread(t2));
            sync.lock();
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.hasQueuedThread(t1));
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(sync.hasQueuedThread(t1));
            assertTrue(sync.hasQueuedThread(t2));
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(sync.hasQueuedThread(t1));
            assertTrue(sync.hasQueuedThread(t2));
            sync.unlock();
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(sync.hasQueuedThread(t1));
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(sync.hasQueuedThread(t2));
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
	final PublicReentrantLock lock = new PublicReentrantLock();
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        try {
            assertTrue(lock.getQueuedThreads().isEmpty());
            lock.lock();
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
            lock.unlock();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.getQueuedThreads().isEmpty());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    }


    /**
     * timed tryLock is interruptible.
     */
    public void testInterruptedException2() {
	final ReentrantLock lock = new ReentrantLock();
	lock.lock();
	Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
			lock.tryLock(MEDIUM_DELAY_MS,TimeUnit.MILLISECONDS);
			threadShouldThrow();
		    } catch(InterruptedException success){}
		}
	    });
        try {
            t.start();
            t.interrupt();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * TryLock on a locked lock fails
     */
    public void testTryLockWhenLocked() {
	final ReentrantLock lock = new ReentrantLock();
	lock.lock();
	Thread t = new Thread(new Runnable() {
                public void run() {
                    threadAssertFalse(lock.tryLock());
		}
	    });
        try {
            t.start();
            t.join();
            lock.unlock();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * Timed tryLock on a locked lock times out
     */
    public void testTryLock_Timeout() {
	final ReentrantLock lock = new ReentrantLock();
	lock.lock();
	Thread t = new Thread(new Runnable() {
                public void run() {
		    try {
                        threadAssertFalse(lock.tryLock(1, TimeUnit.MILLISECONDS));
                    } catch (Exception ex) {
                        threadUnexpectedException();
                    }
		}
	    });
        try {
            t.start();
            t.join();
            lock.unlock();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * getHoldCount returns number of recursive holds
     */
    public void testGetHoldCount() {
	ReentrantLock lock = new ReentrantLock();
	for(int i = 1; i <= SIZE; i++) {
	    lock.lock();
	    assertEquals(i,lock.getHoldCount());
	}
	for(int i = SIZE; i > 0; i--) {
	    lock.unlock();
	    assertEquals(i-1,lock.getHoldCount());
	}
    }


    /**
     * isLocked is true when locked and false when not
     */
    public void testIsLocked() {
	final ReentrantLock lock = new ReentrantLock();
	lock.lock();
	assertTrue(lock.isLocked());
	lock.unlock();
	assertFalse(lock.isLocked());
	Thread t = new Thread(new Runnable() {
		public void run() {
		    lock.lock();
		    try {
			Thread.sleep(SMALL_DELAY_MS);
		    }
		    catch(Exception e) {
                        threadUnexpectedException();
                    }
		    lock.unlock();
		}
	    });
	try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.isLocked());
            t.join();
            assertFalse(lock.isLocked());
        } catch(Exception e){
            unexpectedException();
        }
    }


    /**
     * lockInterruptibly is interruptible.
     */
    public void testLockInterruptibly1() {
	final ReentrantLock lock = new ReentrantLock();
	lock.lock();
	Thread t = new Thread(new InterruptedLockRunnable(lock));
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            lock.unlock();
            t.join();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * lockInterruptibly succeeds when unlocked, else is interruptible
     */
    public void testLockInterruptibly2() {
	final ReentrantLock lock = new ReentrantLock();
	try {
            lock.lockInterruptibly();
        } catch(Exception e) {
            unexpectedException();
        }
	Thread t = new Thread(new InterruptedLockRunnable(lock));
        try {
            t.start();
            t.interrupt();
            assertTrue(lock.isLocked());
            assertTrue(lock.isHeldByCurrentThread());
            t.join();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * Calling await without holding lock throws IllegalMonitorStateException
     */
    public void testAwait_IllegalMonitor() {
	final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        try {
            c.await();
            shouldThrow();
        }
        catch (IllegalMonitorStateException success) {
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * Calling signal without holding lock throws IllegalMonitorStateException
     */
    public void testSignal_IllegalMonitor() {
	final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        try {
            c.signal();
            shouldThrow();
        }
        catch (IllegalMonitorStateException success) {
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * awaitNanos without a signal times out
     */
    public void testAwaitNanos_Timeout() {
	final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        try {
            lock.lock();
            long t = c.awaitNanos(100);
            assertTrue(t <= 0);
            lock.unlock();
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     *  timed await without a signal times out
     */
    public void testAwait_Timeout() {
	final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        try {
            lock.lock();
            c.await(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
            lock.unlock();
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * awaitUntil without a signal times out
     */
    public void testAwaitUntil_Timeout() {
	final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        try {
            lock.lock();
            java.util.Date d = new java.util.Date();
            c.awaitUntil(new java.util.Date(d.getTime() + 10));
            lock.unlock();
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * await returns when signalled
     */
    public void testAwait() {
	final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.lock();
                        c.await();
                        lock.unlock();
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            lock.lock();
            c.signal();
            lock.unlock();
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * hasWaiters throws NPE if null
     */
    public void testHasWaitersNPE() {
	final ReentrantLock lock = new ReentrantLock();
        try {
            lock.hasWaiters(null);
            shouldThrow();
        } catch (NullPointerException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * getWaitQueueLength throws NPE if null
     */
    public void testGetWaitQueueLengthNPE() {
	final ReentrantLock lock = new ReentrantLock();
        try {
            lock.getWaitQueueLength(null);
            shouldThrow();
        } catch (NullPointerException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }


    /**
     * getWaitingThreads throws NPE if null
     */
    public void testGetWaitingThreadsNPE() {
	final PublicReentrantLock lock = new PublicReentrantLock();
        try {
            lock.getWaitingThreads(null);
            shouldThrow();
        } catch (NullPointerException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }


    /**
     * hasWaiters throws IAE if not owned
     */
    public void testHasWaitersIAE() {
	final ReentrantLock lock = new ReentrantLock();
        final Condition c = (lock.newCondition());
	final ReentrantLock lock2 = new ReentrantLock();
        try {
            lock2.hasWaiters(c);
            shouldThrow();
        } catch (IllegalArgumentException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * hasWaiters throws IMSE if not locked
     */
    public void testHasWaitersIMSE() {
	final ReentrantLock lock = new ReentrantLock();
        final Condition c = (lock.newCondition());
        try {
            lock.hasWaiters(c);
            shouldThrow();
        } catch (IllegalMonitorStateException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }


    /**
     * getWaitQueueLength throws IAE if not owned
     */
    public void testGetWaitQueueLengthIAE() {
	final ReentrantLock lock = new ReentrantLock();
        final Condition c = (lock.newCondition());
	final ReentrantLock lock2 = new ReentrantLock();
        try {
            lock2.getWaitQueueLength(c);
            shouldThrow();
        } catch (IllegalArgumentException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * getWaitQueueLength throws IMSE if not locked
     */
    public void testGetWaitQueueLengthIMSE() {
	final ReentrantLock lock = new ReentrantLock();
        final Condition c = (lock.newCondition());
        try {
            lock.getWaitQueueLength(c);
            shouldThrow();
        } catch (IllegalMonitorStateException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }


    /**
     * getWaitingThreads throws IAE if not owned
     */
    public void testGetWaitingThreadsIAE() {
	final PublicReentrantLock lock = new PublicReentrantLock();
        final Condition c = (lock.newCondition());
	final PublicReentrantLock lock2 = new PublicReentrantLock();
        try {
            lock2.getWaitingThreads(c);
            shouldThrow();
        } catch (IllegalArgumentException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * getWaitingThreads throws IMSE if not locked
     */
    public void testGetWaitingThreadsIMSE() {
	final PublicReentrantLock lock = new PublicReentrantLock();
        final Condition c = (lock.newCondition());
        try {
            lock.getWaitingThreads(c);
            shouldThrow();
        } catch (IllegalMonitorStateException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }



    /**
     * hasWaiters returns true when a thread is waiting, else false
     */
    public void testHasWaiters() {
	final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.lock();
                        threadAssertFalse(lock.hasWaiters(c));
                        threadAssertEquals(0, lock.getWaitQueueLength(c));
                        c.await();
                        lock.unlock();
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            lock.lock();
            assertTrue(lock.hasWaiters(c));
            assertEquals(1, lock.getWaitQueueLength(c));
            c.signal();
            lock.unlock();
            Thread.sleep(SHORT_DELAY_MS);
            lock.lock();
            assertFalse(lock.hasWaiters(c));
            assertEquals(0, lock.getWaitQueueLength(c));
            lock.unlock();
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * getWaitQueueLength returns number of waiting threads
     */
    public void testGetWaitQueueLength() {
	final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
	Thread t1 = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.lock();
                        threadAssertFalse(lock.hasWaiters(c));
                        threadAssertEquals(0, lock.getWaitQueueLength(c));
                        c.await();
                        lock.unlock();
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

	Thread t2 = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.lock();
                        threadAssertTrue(lock.hasWaiters(c));
                        threadAssertEquals(1, lock.getWaitQueueLength(c));
                        c.await();
                        lock.unlock();
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

        try {
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            lock.lock();
            assertTrue(lock.hasWaiters(c));
            assertEquals(2, lock.getWaitQueueLength(c));
            c.signalAll();
            lock.unlock();
            Thread.sleep(SHORT_DELAY_MS);
            lock.lock();
            assertFalse(lock.hasWaiters(c));
            assertEquals(0, lock.getWaitQueueLength(c));
            lock.unlock();
            t1.join(SHORT_DELAY_MS);
            t2.join(SHORT_DELAY_MS);
            assertFalse(t1.isAlive());
            assertFalse(t2.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * getWaitingThreads returns only and all waiting threads
     */
    public void testGetWaitingThreads() {
	final PublicReentrantLock lock = new PublicReentrantLock();
        final Condition c = lock.newCondition();
	Thread t1 = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.lock();
                        threadAssertTrue(lock.getWaitingThreads(c).isEmpty());
                        c.await();
                        lock.unlock();
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

	Thread t2 = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.lock();
                        threadAssertFalse(lock.getWaitingThreads(c).isEmpty());
                        c.await();
                        lock.unlock();
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

        try {
            lock.lock();
            assertTrue(lock.getWaitingThreads(c).isEmpty());
            lock.unlock();
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            lock.lock();
            assertTrue(lock.hasWaiters(c));
            assertTrue(lock.getWaitingThreads(c).contains(t1));
            assertTrue(lock.getWaitingThreads(c).contains(t2));
            c.signalAll();
            lock.unlock();
            Thread.sleep(SHORT_DELAY_MS);
            lock.lock();
            assertFalse(lock.hasWaiters(c));
            assertTrue(lock.getWaitingThreads(c).isEmpty());
            lock.unlock();
            t1.join(SHORT_DELAY_MS);
            t2.join(SHORT_DELAY_MS);
            assertFalse(t1.isAlive());
            assertFalse(t2.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /** A helper class for uninterruptible wait tests */
    class UninterruptableThread extends Thread {
        private ReentrantLock lock;
        private Condition c;

        public volatile boolean canAwake = false;
        public volatile boolean interrupted = false;
        public volatile boolean lockStarted = false;

        public UninterruptableThread(ReentrantLock lock, Condition c) {
            this.lock = lock;
            this.c = c;
        }

        public synchronized void run() {
            lock.lock();
            lockStarted = true;

            while (!canAwake) {
                c.awaitUninterruptibly();
            }

            interrupted = isInterrupted();
            lock.unlock();
        }
    }

    /**
     * awaitUninterruptibly doesn't abort on interrupt
     */
    public void testAwaitUninterruptibly() {
        final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
        UninterruptableThread thread = new UninterruptableThread(lock, c);

        try {
            thread.start();

            while (!thread.lockStarted) {
                Thread.sleep(100);
            }

            lock.lock();
            try {
                thread.interrupt();
                thread.canAwake = true;
                c.signal();
            } finally {
                lock.unlock();
            }

            thread.join();
            assertTrue(thread.interrupted);
            assertFalse(thread.isAlive());
        } catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * await is interruptible
     */
    public void testAwait_Interrupt() {
	final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.lock();
                        c.await();
                        lock.unlock();
                        threadShouldThrow();
		    }
		    catch(InterruptedException success) {
                    }
		}
	    });

        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * awaitNanos is interruptible
     */
    public void testAwaitNanos_Interrupt() {
	final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.lock();
                        c.awaitNanos(1000 * 1000 * 1000); // 1 sec
                        lock.unlock();
                        threadShouldThrow();
		    }
		    catch(InterruptedException success) {
                    }
		}
	    });

        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * awaitUntil is interruptible
     *
    TODO(tball): replace with pthread_cancel (b/11536576)
    public void testAwaitUntil_Interrupt() {
	final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.lock();
                        java.util.Date d = new java.util.Date();
                        c.awaitUntil(new java.util.Date(d.getTime() + 10000));
                        lock.unlock();
                        threadShouldThrow();
		    }
		    catch(InterruptedException success) {
                    }
		}
	    });

        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    */

    /**
     * signalAll wakes up all threads
     */
    public void testSignalAll() {
	final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
	Thread t1 = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.lock();
                        c.await();
                        lock.unlock();
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

	Thread t2 = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.lock();
                        c.await();
                        lock.unlock();
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

        try {
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            lock.lock();
            c.signalAll();
            lock.unlock();
            t1.join(SHORT_DELAY_MS);
            t2.join(SHORT_DELAY_MS);
            assertFalse(t1.isAlive());
            assertFalse(t2.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * await after multiple reentrant locking preserves lock count
     */
    public void testAwaitLockCount() {
	final ReentrantLock lock = new ReentrantLock();
        final Condition c = lock.newCondition();
	Thread t1 = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.lock();
                        threadAssertEquals(1, lock.getHoldCount());
                        c.await();
                        threadAssertEquals(1, lock.getHoldCount());
                        lock.unlock();
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

	Thread t2 = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.lock();
			lock.lock();
                        threadAssertEquals(2, lock.getHoldCount());
                        c.await();
                        threadAssertEquals(2, lock.getHoldCount());
                        lock.unlock();
                        lock.unlock();
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

        try {
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            lock.lock();
            c.signalAll();
            lock.unlock();
            t1.join(SHORT_DELAY_MS);
            t2.join(SHORT_DELAY_MS);
            assertFalse(t1.isAlive());
            assertFalse(t2.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * A serialized lock deserializes as unlocked
     *
    // TODO(tball): enable when serialization is supported.
    public void testSerialization() {
        ReentrantLock l = new ReentrantLock();
        l.lock();
        l.unlock();

        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(l);
            out.close();

            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            ReentrantLock r = (ReentrantLock) in.readObject();
            r.lock();
            r.unlock();
        } catch(Exception e){
            e.printStackTrace();
            unexpectedException();
        }
    }

    /**
     * toString indicates current lock state
     *
    public void testToString() {
        ReentrantLock lock = new ReentrantLock();
        String us = lock.toString();
        assertTrue(us.indexOf("Unlocked") >= 0);
        lock.lock();
        String ls = lock.toString();
        assertTrue(ls.indexOf("Locked") >= 0);
    }
    */
}
