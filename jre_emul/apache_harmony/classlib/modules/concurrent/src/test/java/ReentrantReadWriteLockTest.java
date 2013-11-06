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
import java.io.*;
import java.util.*;

public class ReentrantReadWriteLockTest extends JSR166TestCase {
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
	return new TestSuite(ReentrantReadWriteLockTest.class);
    }

    /**
     * A runnable calling lockInterruptibly
     */
    class InterruptibleLockRunnable implements Runnable {
        final ReentrantReadWriteLock lock;
        InterruptibleLockRunnable(ReentrantReadWriteLock l) { lock = l; }
        public void run() {
            try {
                lock.writeLock().lockInterruptibly();
            } catch(InterruptedException success){}
        }
    }


    /**
     * A runnable calling lockInterruptibly that expects to be
     * interrupted
     */
    class InterruptedLockRunnable implements Runnable {
        final ReentrantReadWriteLock lock;
        InterruptedLockRunnable(ReentrantReadWriteLock l) { lock = l; }
        public void run() {
            try {
                lock.writeLock().lockInterruptibly();
                threadShouldThrow();
            } catch(InterruptedException success){}
        }
    }

    /**
     * Subclass to expose protected methods
     */
    static class PublicReentrantReadWriteLock extends ReentrantReadWriteLock {
        PublicReentrantReadWriteLock() { super(); }
        public Collection<Thread> getQueuedThreads() {
            return super.getQueuedThreads();
        }
        public Collection<Thread> getWaitingThreads(Condition c) {
            return super.getWaitingThreads(c);
        }
    }

    /**
     * Constructor sets given fairness, and is in unlocked state
     */
    public void testConstructor() {
	ReentrantReadWriteLock rl = new ReentrantReadWriteLock();
        assertFalse(rl.isFair());
        assertFalse(rl.isWriteLocked());
        assertEquals(0, rl.getReadLockCount());
	ReentrantReadWriteLock r2 = new ReentrantReadWriteLock(true);
        assertTrue(r2.isFair());
        assertFalse(r2.isWriteLocked());
        assertEquals(0, r2.getReadLockCount());
    }

    /**
     * write-locking and read-locking an unlocked lock succeed
     */
    public void testLock() {
	ReentrantReadWriteLock rl = new ReentrantReadWriteLock();
        rl.writeLock().lock();
        assertTrue(rl.isWriteLocked());
        assertTrue(rl.isWriteLockedByCurrentThread());
        assertEquals(0, rl.getReadLockCount());
        rl.writeLock().unlock();
        assertFalse(rl.isWriteLocked());
        assertFalse(rl.isWriteLockedByCurrentThread());
        assertEquals(0, rl.getReadLockCount());
        rl.readLock().lock();
        assertFalse(rl.isWriteLocked());
        assertFalse(rl.isWriteLockedByCurrentThread());
        assertEquals(1, rl.getReadLockCount());
        rl.readLock().unlock();
        assertFalse(rl.isWriteLocked());
        assertFalse(rl.isWriteLockedByCurrentThread());
        assertEquals(0, rl.getReadLockCount());
    }


    /**
     * locking an unlocked fair lock succeeds
     */
    public void testFairLock() {
	ReentrantReadWriteLock rl = new ReentrantReadWriteLock(true);
        rl.writeLock().lock();
        assertTrue(rl.isWriteLocked());
        assertTrue(rl.isWriteLockedByCurrentThread());
        assertEquals(0, rl.getReadLockCount());
        rl.writeLock().unlock();
        assertFalse(rl.isWriteLocked());
        assertFalse(rl.isWriteLockedByCurrentThread());
        assertEquals(0, rl.getReadLockCount());
        rl.readLock().lock();
        assertFalse(rl.isWriteLocked());
        assertFalse(rl.isWriteLockedByCurrentThread());
        assertEquals(1, rl.getReadLockCount());
        rl.readLock().unlock();
        assertFalse(rl.isWriteLocked());
        assertFalse(rl.isWriteLockedByCurrentThread());
        assertEquals(0, rl.getReadLockCount());
    }

    /**
     * getWriteHoldCount returns number of recursive holds
     */
    public void testGetWriteHoldCount() {
	ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	for(int i = 1; i <= SIZE; i++) {
	    lock.writeLock().lock();
	    assertEquals(i,lock.getWriteHoldCount());
	}
	for(int i = SIZE; i > 0; i--) {
	    lock.writeLock().unlock();
	    assertEquals(i-1,lock.getWriteHoldCount());
	}
    }


    /**
     * write-unlocking an unlocked lock throws IllegalMonitorStateException
     */
    public void testUnlock_IllegalMonitorStateException() {
	ReentrantReadWriteLock rl = new ReentrantReadWriteLock();
	try {
	    rl.writeLock().unlock();
	    shouldThrow();
	} catch(IllegalMonitorStateException success){}
    }


    /**
     * write-lockInterruptibly is interruptible
     */
    public void testWriteLockInterruptibly_Interrupted() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
			lock.writeLock().lockInterruptibly();
                        lock.writeLock().unlock();
			lock.writeLock().lockInterruptibly();
                        lock.writeLock().unlock();
		    } catch(InterruptedException success){}
		}
	    });
        try {
            lock.writeLock().lock();
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            lock.writeLock().unlock();
            t.join();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * timed write-tryLock is interruptible
     */
    public void testWriteTryLock_Interrupted() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	lock.writeLock().lock();
	Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
			lock.writeLock().tryLock(1000,TimeUnit.MILLISECONDS);
		    } catch(InterruptedException success){}
		}
	    });
        try {
            t.start();
            t.interrupt();
            lock.writeLock().unlock();
            t.join();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * read-lockInterruptibly is interruptible
     */
    public void testReadLockInterruptibly_Interrupted() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	lock.writeLock().lock();
	Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
			lock.readLock().lockInterruptibly();
		    } catch(InterruptedException success){}
		}
	    });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            lock.writeLock().unlock();
            t.join();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * timed read-tryLock is interruptible
     */
    public void testReadTryLock_Interrupted() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	lock.writeLock().lock();
	Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
			lock.readLock().tryLock(1000,TimeUnit.MILLISECONDS);
			threadShouldThrow();
		    } catch(InterruptedException success){}
		}
	    });
        try {
            t.start();
            t.interrupt();
            t.join();
        } catch(Exception e){
            unexpectedException();
        }
    }


    /**
     * write-tryLock fails if locked
     */
    public void testWriteTryLockWhenLocked() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	lock.writeLock().lock();
	Thread t = new Thread(new Runnable() {
                public void run() {
                    threadAssertFalse(lock.writeLock().tryLock());
		}
	    });
        try {
            t.start();
            t.join();
            lock.writeLock().unlock();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * read-tryLock fails if locked
     */
    public void testReadTryLockWhenLocked() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	lock.writeLock().lock();
	Thread t = new Thread(new Runnable() {
                public void run() {
                    threadAssertFalse(lock.readLock().tryLock());
		}
	    });
        try {
            t.start();
            t.join();
            lock.writeLock().unlock();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * Multiple threads can hold a read lock when not write-locked
     */
    public void testMultipleReadLocks() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	lock.readLock().lock();
	Thread t = new Thread(new Runnable() {
                public void run() {
                    threadAssertTrue(lock.readLock().tryLock());
                    lock.readLock().unlock();
		}
	    });
        try {
            t.start();
            t.join();
            lock.readLock().unlock();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * A writelock succeeds after reading threads unlock
     */
    public void testWriteAfterMultipleReadLocks() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	lock.readLock().lock();
	Thread t1 = new Thread(new Runnable() {
                public void run() {
                    lock.readLock().lock();
                    lock.readLock().unlock();
		}
	    });
	Thread t2 = new Thread(new Runnable() {
                public void run() {
                    lock.writeLock().lock();
                    lock.writeLock().unlock();
		}
	    });

        try {
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            lock.readLock().unlock();
            t1.join(MEDIUM_DELAY_MS);
            t2.join(MEDIUM_DELAY_MS);
            assertTrue(!t1.isAlive());
            assertTrue(!t2.isAlive());

        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * Readlocks succeed after a writing thread unlocks
     */
    public void testReadAfterWriteLock() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	lock.writeLock().lock();
	Thread t1 = new Thread(new Runnable() {
                public void run() {
                    lock.readLock().lock();
                    lock.readLock().unlock();
		}
	    });
	Thread t2 = new Thread(new Runnable() {
                public void run() {
                    lock.readLock().lock();
                    lock.readLock().unlock();
		}
	    });

        try {
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            lock.writeLock().unlock();
            t1.join(MEDIUM_DELAY_MS);
            t2.join(MEDIUM_DELAY_MS);
            assertTrue(!t1.isAlive());
            assertTrue(!t2.isAlive());

        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * Read trylock succeeds if write locked by current thread
     */
    public void testReadHoldingWriteLock() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	lock.writeLock().lock();
        assertTrue(lock.readLock().tryLock());
        lock.readLock().unlock();
        lock.writeLock().unlock();
    }

    /**
     * Read lock succeeds if write locked by current thread even if
     * other threads are waiting for readlock
     */
    public void testReadHoldingWriteLock2() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	lock.writeLock().lock();
	Thread t1 = new Thread(new Runnable() {
                public void run() {
                    lock.readLock().lock();
                    lock.readLock().unlock();
		}
	    });
	Thread t2 = new Thread(new Runnable() {
                public void run() {
                    lock.readLock().lock();
                    lock.readLock().unlock();
		}
	    });

        try {
            t1.start();
            t2.start();
            lock.readLock().lock();
            lock.readLock().unlock();
            Thread.sleep(SHORT_DELAY_MS);
            lock.readLock().lock();
            lock.readLock().unlock();
            lock.writeLock().unlock();
            t1.join(MEDIUM_DELAY_MS);
            t2.join(MEDIUM_DELAY_MS);
            assertTrue(!t1.isAlive());
            assertTrue(!t2.isAlive());

        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     *  Read lock succeeds if write locked by current thread even if
     * other threads are waiting for writelock
     */
    public void testReadHoldingWriteLock3() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	lock.writeLock().lock();
	Thread t1 = new Thread(new Runnable() {
                public void run() {
                    lock.writeLock().lock();
                    lock.writeLock().unlock();
		}
	    });
	Thread t2 = new Thread(new Runnable() {
                public void run() {
                    lock.writeLock().lock();
                    lock.writeLock().unlock();
		}
	    });

        try {
            t1.start();
            t2.start();
            lock.readLock().lock();
            lock.readLock().unlock();
            Thread.sleep(SHORT_DELAY_MS);
            lock.readLock().lock();
            lock.readLock().unlock();
            lock.writeLock().unlock();
            t1.join(MEDIUM_DELAY_MS);
            t2.join(MEDIUM_DELAY_MS);
            assertTrue(!t1.isAlive());
            assertTrue(!t2.isAlive());

        } catch(Exception e){
            unexpectedException();
        }
    }


    /**
     *  Write lock succeeds if write locked by current thread even if
     * other threads are waiting for writelock
     */
    public void testWriteHoldingWriteLock4() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	lock.writeLock().lock();
	Thread t1 = new Thread(new Runnable() {
                public void run() {
                    lock.writeLock().lock();
                    lock.writeLock().unlock();
		}
	    });
	Thread t2 = new Thread(new Runnable() {
                public void run() {
                    lock.writeLock().lock();
                    lock.writeLock().unlock();
		}
	    });

        try {
            t1.start();
            t2.start();
            lock.writeLock().lock();
            lock.writeLock().unlock();
            Thread.sleep(SHORT_DELAY_MS);
            lock.writeLock().lock();
            lock.writeLock().unlock();
            lock.writeLock().unlock();
            t1.join(MEDIUM_DELAY_MS);
            t2.join(MEDIUM_DELAY_MS);
            assertTrue(!t1.isAlive());
            assertTrue(!t2.isAlive());

        } catch(Exception e){
            unexpectedException();
        }
    }


    /**
     * Fair Read trylock succeeds if write locked by current thread
     */
    public void testReadHoldingWriteLockFair() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	lock.writeLock().lock();
        assertTrue(lock.readLock().tryLock());
        lock.readLock().unlock();
        lock.writeLock().unlock();
    }

    /**
     * Fair Read lock succeeds if write locked by current thread even if
     * other threads are waiting for readlock
     */
    public void testReadHoldingWriteLockFair2() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	lock.writeLock().lock();
	Thread t1 = new Thread(new Runnable() {
                public void run() {
                    lock.readLock().lock();
                    lock.readLock().unlock();
		}
	    });
	Thread t2 = new Thread(new Runnable() {
                public void run() {
                    lock.readLock().lock();
                    lock.readLock().unlock();
		}
	    });

        try {
            t1.start();
            t2.start();
            lock.readLock().lock();
            lock.readLock().unlock();
            Thread.sleep(SHORT_DELAY_MS);
            lock.readLock().lock();
            lock.readLock().unlock();
            lock.writeLock().unlock();
            t1.join(MEDIUM_DELAY_MS);
            t2.join(MEDIUM_DELAY_MS);
            assertTrue(!t1.isAlive());
            assertTrue(!t2.isAlive());

        } catch(Exception e){
            unexpectedException();
        }
    }


    /**
     * Fair Read lock succeeds if write locked by current thread even if
     * other threads are waiting for writelock
     */
    public void testReadHoldingWriteLockFair3() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	lock.writeLock().lock();
	Thread t1 = new Thread(new Runnable() {
                public void run() {
                    lock.writeLock().lock();
                    lock.writeLock().unlock();
		}
	    });
	Thread t2 = new Thread(new Runnable() {
                public void run() {
                    lock.writeLock().lock();
                    lock.writeLock().unlock();
		}
	    });

        try {
            t1.start();
            t2.start();
            lock.readLock().lock();
            lock.readLock().unlock();
            Thread.sleep(SHORT_DELAY_MS);
            lock.readLock().lock();
            lock.readLock().unlock();
            lock.writeLock().unlock();
            t1.join(MEDIUM_DELAY_MS);
            t2.join(MEDIUM_DELAY_MS);
            assertTrue(!t1.isAlive());
            assertTrue(!t2.isAlive());

        } catch(Exception e){
            unexpectedException();
        }
    }


    /**
     * Fair Write lock succeeds if write locked by current thread even if
     * other threads are waiting for writelock
     */
    public void testWriteHoldingWriteLockFair4() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	lock.writeLock().lock();
	Thread t1 = new Thread(new Runnable() {
                public void run() {
                    lock.writeLock().lock();
                    lock.writeLock().unlock();
		}
	    });
	Thread t2 = new Thread(new Runnable() {
                public void run() {
                    lock.writeLock().lock();
                    lock.writeLock().unlock();
		}
	    });

        try {
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.isWriteLockedByCurrentThread());
            assertTrue(lock.getWriteHoldCount() == 1);
            lock.writeLock().lock();
            assertTrue(lock.getWriteHoldCount() == 2);
            lock.writeLock().unlock();
            lock.writeLock().lock();
            lock.writeLock().unlock();
            lock.writeLock().unlock();
            t1.join(MEDIUM_DELAY_MS);
            t2.join(MEDIUM_DELAY_MS);
            assertTrue(!t1.isAlive());
            assertTrue(!t2.isAlive());

        } catch(Exception e){
            unexpectedException();
        }
    }


    /**
     * Read tryLock succeeds if readlocked but not writelocked
     */
    public void testTryLockWhenReadLocked() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	lock.readLock().lock();
	Thread t = new Thread(new Runnable() {
                public void run() {
                    threadAssertTrue(lock.readLock().tryLock());
                    lock.readLock().unlock();
		}
	    });
        try {
            t.start();
            t.join();
            lock.readLock().unlock();
        } catch(Exception e){
            unexpectedException();
        }
    }



    /**
     * write tryLock fails when readlocked
     */
    public void testWriteTryLockWhenReadLocked() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	lock.readLock().lock();
	Thread t = new Thread(new Runnable() {
                public void run() {
                    threadAssertFalse(lock.writeLock().tryLock());
		}
	    });
        try {
            t.start();
            t.join();
            lock.readLock().unlock();
        } catch(Exception e){
            unexpectedException();
        }
    }


    /**
     * Fair Read tryLock succeeds if readlocked but not writelocked
     */
    public void testTryLockWhenReadLockedFair() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	lock.readLock().lock();
	Thread t = new Thread(new Runnable() {
                public void run() {
                    threadAssertTrue(lock.readLock().tryLock());
                    lock.readLock().unlock();
		}
	    });
        try {
            t.start();
            t.join();
            lock.readLock().unlock();
        } catch(Exception e){
            unexpectedException();
        }
    }



    /**
     * Fair write tryLock fails when readlocked
     */
    public void testWriteTryLockWhenReadLockedFair() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	lock.readLock().lock();
	Thread t = new Thread(new Runnable() {
                public void run() {
                    threadAssertFalse(lock.writeLock().tryLock());
		}
	    });
        try {
            t.start();
            t.join();
            lock.readLock().unlock();
        } catch(Exception e){
            unexpectedException();
        }
    }



    /**
     * write timed tryLock times out if locked
     */
    public void testWriteTryLock_Timeout() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	lock.writeLock().lock();
	Thread t = new Thread(new Runnable() {
                public void run() {
		    try {
                        threadAssertFalse(lock.writeLock().tryLock(1, TimeUnit.MILLISECONDS));
                    } catch (Exception ex) {
                        threadUnexpectedException();
                    }
		}
	    });
        try {
            t.start();
            t.join();
            lock.writeLock().unlock();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * read timed tryLock times out if write-locked
     */
    public void testReadTryLock_Timeout() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	lock.writeLock().lock();
	Thread t = new Thread(new Runnable() {
                public void run() {
		    try {
                        threadAssertFalse(lock.readLock().tryLock(1, TimeUnit.MILLISECONDS));
                    } catch (Exception ex) {
                        threadUnexpectedException();
                    }
		}
	    });
        try {
            t.start();
            t.join();
            lock.writeLock().unlock();
        } catch(Exception e){
            unexpectedException();
        }
    }


    /**
     * write lockInterruptibly succeeds if lock free else is interruptible
     */
    public void testWriteLockInterruptibly() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	try {
            lock.writeLock().lockInterruptibly();
        } catch(Exception e) {
            unexpectedException();
        }
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.writeLock().lockInterruptibly();
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
            Thread.sleep(SHORT_DELAY_MS);
            t.join();
            lock.writeLock().unlock();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     *  read lockInterruptibly succeeds if lock free else is interruptible
     */
    public void testReadLockInterruptibly() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	try {
            lock.writeLock().lockInterruptibly();
        } catch(Exception e) {
            unexpectedException();
        }
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.readLock().lockInterruptibly();
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
            t.join();
            lock.writeLock().unlock();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * Calling await without holding lock throws IllegalMonitorStateException
     */
    public void testAwait_IllegalMonitor() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Condition c = lock.writeLock().newCondition();
        try {
            c.await();
            shouldThrow();
        }
        catch (IllegalMonitorStateException success) {
        }
        catch (Exception ex) {
            shouldThrow();
        }
    }

    /**
     * Calling signal without holding lock throws IllegalMonitorStateException
     */
    public void testSignal_IllegalMonitor() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Condition c = lock.writeLock().newCondition();
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
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Condition c = lock.writeLock().newCondition();
        try {
            lock.writeLock().lock();
            long t = c.awaitNanos(100);
            assertTrue(t <= 0);
            lock.writeLock().unlock();
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }


    /**
     *  timed await without a signal times out
     */
    public void testAwait_Timeout() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Condition c = lock.writeLock().newCondition();
        try {
            lock.writeLock().lock();
            lock.writeLock().unlock();
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * awaitUntil without a signal times out
     */
    public void testAwaitUntil_Timeout() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Condition c = lock.writeLock().newCondition();
        try {
            lock.writeLock().lock();
            java.util.Date d = new java.util.Date();
            lock.writeLock().unlock();
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * await returns when signalled
     */
    public void testAwait() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Condition c = lock.writeLock().newCondition();
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.writeLock().lock();
                        c.await();
                        lock.writeLock().unlock();
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            lock.writeLock().lock();
            c.signal();
            lock.writeLock().unlock();
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /** A helper class for uninterruptible wait tests */
    class UninterruptableThread extends Thread {
        private Lock lock;
        private Condition c;

        public volatile boolean canAwake = false;
        public volatile boolean interrupted = false;
        public volatile boolean lockStarted = false;

        public UninterruptableThread(Lock lock, Condition c) {
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
        final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Condition c = lock.writeLock().newCondition();
        UninterruptableThread thread = new UninterruptableThread(lock.writeLock(), c);

        try {
            thread.start();

            while (!thread.lockStarted) {
                Thread.sleep(100);
            }

            lock.writeLock().lock();
            try {
                thread.interrupt();
                thread.canAwake = true;
                c.signal();
            } finally {
                lock.writeLock().unlock();
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
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Condition c = lock.writeLock().newCondition();
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.writeLock().lock();
                        c.await();
                        lock.writeLock().unlock();
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
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Condition c = lock.writeLock().newCondition();
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.writeLock().lock();
                        c.awaitNanos(SHORT_DELAY_MS * 2 * 1000000);
                        lock.writeLock().unlock();
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
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Condition c = lock.writeLock().newCondition();
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.writeLock().lock();
                        java.util.Date d = new java.util.Date();
                        c.awaitUntil(new java.util.Date(d.getTime() + 10000));
                        lock.writeLock().unlock();
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
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Condition c = lock.writeLock().newCondition();
	Thread t1 = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.writeLock().lock();
                        c.await();
                        lock.writeLock().unlock();
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

	Thread t2 = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.writeLock().lock();
                        c.await();
                        lock.writeLock().unlock();
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
            lock.writeLock().lock();
            c.signalAll();
            lock.writeLock().unlock();
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
        ReentrantReadWriteLock l = new ReentrantReadWriteLock();
        l.readLock().lock();
        l.readLock().unlock();

        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(l);
            out.close();

            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            ReentrantReadWriteLock r = (ReentrantReadWriteLock) in.readObject();
            r.readLock().lock();
            r.readLock().unlock();
        } catch(Exception e){
            e.printStackTrace();
            unexpectedException();
        }
    }
    */

    /**
     * hasQueuedThreads reports whether there are waiting threads
     */
    public void testhasQueuedThreads() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        try {
            assertFalse(lock.hasQueuedThreads());
            lock.writeLock().lock();
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.hasQueuedThreads());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.hasQueuedThreads());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.hasQueuedThreads());
            lock.writeLock().unlock();
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(lock.hasQueuedThreads());
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
	final ReentrantReadWriteLock sync = new ReentrantReadWriteLock();
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
	final ReentrantReadWriteLock sync = new ReentrantReadWriteLock();
        Thread t1 = new Thread(new InterruptedLockRunnable(sync));
        Thread t2 = new Thread(new InterruptibleLockRunnable(sync));
        try {
            assertFalse(sync.hasQueuedThread(t1));
            assertFalse(sync.hasQueuedThread(t2));
            sync.writeLock().lock();
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
            sync.writeLock().unlock();
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
     * getQueueLength reports number of waiting threads
     */
    public void testGetQueueLength() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        try {
            assertEquals(0, lock.getQueueLength());
            lock.writeLock().lock();
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(1, lock.getQueueLength());
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(2, lock.getQueueLength());
            t1.interrupt();
            Thread.sleep(SHORT_DELAY_MS);
            assertEquals(1, lock.getQueueLength());
            lock.writeLock().unlock();
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
	final PublicReentrantReadWriteLock lock = new PublicReentrantReadWriteLock();
        Thread t1 = new Thread(new InterruptedLockRunnable(lock));
        Thread t2 = new Thread(new InterruptibleLockRunnable(lock));
        try {
            assertTrue(lock.getQueuedThreads().isEmpty());
            lock.writeLock().lock();
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
            lock.writeLock().unlock();
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(lock.getQueuedThreads().isEmpty());
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * hasWaiters throws NPE if null
     */
    public void testHasWaitersNPE() {
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
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
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
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
	final PublicReentrantReadWriteLock lock = new PublicReentrantReadWriteLock();
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
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Condition c = (lock.writeLock().newCondition());
	final ReentrantReadWriteLock lock2 = new ReentrantReadWriteLock();
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
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Condition c = (lock.writeLock().newCondition());
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
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Condition c = (lock.writeLock().newCondition());
	final ReentrantReadWriteLock lock2 = new ReentrantReadWriteLock();
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
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Condition c = (lock.writeLock().newCondition());
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
	final PublicReentrantReadWriteLock lock = new PublicReentrantReadWriteLock();
        final Condition c = (lock.writeLock().newCondition());
	final PublicReentrantReadWriteLock lock2 = new PublicReentrantReadWriteLock();
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
	final PublicReentrantReadWriteLock lock = new PublicReentrantReadWriteLock();
        final Condition c = (lock.writeLock().newCondition());
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
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Condition c = (lock.writeLock().newCondition());
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.writeLock().lock();
                        threadAssertFalse(lock.hasWaiters(c));
                        threadAssertEquals(0, lock.getWaitQueueLength(c));
                        c.await();
                        lock.writeLock().unlock();
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            lock.writeLock().lock();
            assertTrue(lock.hasWaiters(c));
            assertEquals(1, lock.getWaitQueueLength(c));
            c.signal();
            lock.writeLock().unlock();
            Thread.sleep(SHORT_DELAY_MS);
            lock.writeLock().lock();
            assertFalse(lock.hasWaiters(c));
            assertEquals(0, lock.getWaitQueueLength(c));
            lock.writeLock().unlock();
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
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        final Condition c = (lock.writeLock().newCondition());
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.writeLock().lock();
                        threadAssertFalse(lock.hasWaiters(c));
                        threadAssertEquals(0, lock.getWaitQueueLength(c));
                        c.await();
                        lock.writeLock().unlock();
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            lock.writeLock().lock();
            assertTrue(lock.hasWaiters(c));
            assertEquals(1, lock.getWaitQueueLength(c));
            c.signal();
            lock.writeLock().unlock();
            Thread.sleep(SHORT_DELAY_MS);
            lock.writeLock().lock();
            assertFalse(lock.hasWaiters(c));
            assertEquals(0, lock.getWaitQueueLength(c));
            lock.writeLock().unlock();
            t.join(SHORT_DELAY_MS);
            assertFalse(t.isAlive());
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }


    /**
     * getWaitingThreads returns only and all waiting threads
     */
    public void testGetWaitingThreads() {
	final PublicReentrantReadWriteLock lock = new PublicReentrantReadWriteLock();
        final Condition c = lock.writeLock().newCondition();
	Thread t1 = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.writeLock().lock();
                        threadAssertTrue(lock.getWaitingThreads(c).isEmpty());
                        c.await();
                        lock.writeLock().unlock();
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

	Thread t2 = new Thread(new Runnable() {
		public void run() {
		    try {
			lock.writeLock().lock();
                        threadAssertFalse(lock.getWaitingThreads(c).isEmpty());
                        c.await();
                        lock.writeLock().unlock();
		    }
		    catch(InterruptedException e) {
                        threadUnexpectedException();
                    }
		}
	    });

        try {
            lock.writeLock().lock();
            assertTrue(lock.getWaitingThreads(c).isEmpty());
            lock.writeLock().unlock();
            t1.start();
            Thread.sleep(SHORT_DELAY_MS);
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            lock.writeLock().lock();
            assertTrue(lock.hasWaiters(c));
            assertTrue(lock.getWaitingThreads(c).contains(t1));
            assertTrue(lock.getWaitingThreads(c).contains(t2));
            c.signalAll();
            lock.writeLock().unlock();
            Thread.sleep(SHORT_DELAY_MS);
            lock.writeLock().lock();
            assertFalse(lock.hasWaiters(c));
            assertTrue(lock.getWaitingThreads(c).isEmpty());
            lock.writeLock().unlock();
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
     * toString indicates current lock state
     */
    public void testToString() {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        String us = lock.toString();
        assertTrue(us.indexOf("Write locks = 0") >= 0);
        assertTrue(us.indexOf("Read locks = 0") >= 0);
        lock.writeLock().lock();
        String ws = lock.toString();
        assertTrue(ws.indexOf("Write locks = 1") >= 0);
        assertTrue(ws.indexOf("Read locks = 0") >= 0);
        lock.writeLock().unlock();
        lock.readLock().lock();
        lock.readLock().lock();
        String rs = lock.toString();
        assertTrue(rs.indexOf("Write locks = 0") >= 0);
        assertTrue(rs.indexOf("Read locks = 2") >= 0);
    }

    /**
     * readLock.toString indicates current lock state
     */
    public void testReadLockToString() {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        String us = lock.readLock().toString();
        assertTrue(us.indexOf("Read locks = 0") >= 0);
        lock.readLock().lock();
        lock.readLock().lock();
        String rs = lock.readLock().toString();
        assertTrue(rs.indexOf("Read locks = 2") >= 0);
    }

    /**
     * writeLock.toString indicates current lock state
     */
    public void testWriteLockToString() {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        String us = lock.writeLock().toString();
        assertTrue(us.indexOf("Unlocked") >= 0);
        lock.writeLock().lock();
        String ls = lock.writeLock().toString();
        assertTrue(ls.indexOf("Locked") >= 0);
    }

}
