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

public class SynchronousQueueTest extends JSR166TestCase {

    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());
    }

    public static Test suite() {
	return new TestSuite(SynchronousQueueTest.class);
    }

    /**
     * A SynchronousQueue is both empty and full
     */
    public void testEmptyFull() {
        SynchronousQueue q = new SynchronousQueue();
        assertTrue(q.isEmpty());
	assertEquals(0, q.size());
        assertEquals(0, q.remainingCapacity());
        assertFalse(q.offer(zero));
    }

    /**
     * A fair SynchronousQueue is both empty and full
     */
    public void testFairEmptyFull() {
        SynchronousQueue q = new SynchronousQueue(true);
        assertTrue(q.isEmpty());
	assertEquals(0, q.size());
        assertEquals(0, q.remainingCapacity());
        assertFalse(q.offer(zero));
    }

    /**
     * offer(null) throws NPE
     */
    public void testOfferNull() {
	try {
            SynchronousQueue q = new SynchronousQueue();
            q.offer(null);
            shouldThrow();
        } catch (NullPointerException success) { }
    }

    /**
     * add(null) throws NPE
     */
    public void testAddNull() {
	try {
            SynchronousQueue q = new SynchronousQueue();
            q.add(null);
            shouldThrow();
        } catch (NullPointerException success) { }
    }

    /**
     * offer fails if no active taker
     */
    public void testOffer() {
        SynchronousQueue q = new SynchronousQueue();
        assertFalse(q.offer(one));
    }

    /**
     * add throws ISE if no active taker
     */
    public void testAdd() {
	try {
            SynchronousQueue q = new SynchronousQueue();
            assertEquals(0, q.remainingCapacity());
            q.add(one);
            shouldThrow();
        } catch (IllegalStateException success){
	}
    }

    /**
     * addAll(null) throws NPE
     */
    public void testAddAll1() {
        try {
            SynchronousQueue q = new SynchronousQueue();
            q.addAll(null);
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }

    /**
     * addAll(this) throws IAE
     */
    public void testAddAllSelf() {
        try {
            SynchronousQueue q = new SynchronousQueue();
            q.addAll(q);
            shouldThrow();
        }
        catch (IllegalArgumentException success) {}
    }

    /**
     * addAll of a collection with null elements throws NPE
     */
    public void testAddAll2() {
        try {
            SynchronousQueue q = new SynchronousQueue();
            Integer[] ints = new Integer[1];
            q.addAll(Arrays.asList(ints));
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    /**
     * addAll throws ISE if no active taker
     */
    public void testAddAll4() {
        try {
            SynchronousQueue q = new SynchronousQueue();
            Integer[] ints = new Integer[1];
            for (int i = 0; i < 1; ++i)
                ints[i] = new Integer(i);
            q.addAll(Arrays.asList(ints));
            shouldThrow();
        }
        catch (IllegalStateException success) {}
    }

    /**
     * put(null) throws NPE
     */
    public void testPutNull() {
	try {
            SynchronousQueue q = new SynchronousQueue();
            q.put(null);
            shouldThrow();
        }
        catch (NullPointerException success){
	}
        catch (InterruptedException ie) {
	    unexpectedException();
        }
     }

    /**
     * put blocks interruptibly if no active taker
     */
    public void testBlockingPut() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        SynchronousQueue q = new SynchronousQueue();
                        q.put(zero);
                        threadShouldThrow();
                    } catch (InterruptedException ie){
                    }
                }});
        t.start();
        try {
           Thread.sleep(SHORT_DELAY_MS);
           t.interrupt();
           t.join();
        }
        catch (InterruptedException ie) {
	    unexpectedException();
        }
    }

    /**
     * put blocks waiting for take
     */
    public void testPutWithTake() {
        final SynchronousQueue q = new SynchronousQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    int added = 0;
                    try {
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
			threadShouldThrow();
                    } catch (InterruptedException e){
                        assertTrue(added >= 1);
                    }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            q.take();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }

    /**
     * timed offer times out if elements not taken
     */
    public void testTimedOffer() {
        final SynchronousQueue q = new SynchronousQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {

                        threadAssertFalse(q.offer(new Object(), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        q.offer(new Object(), LONG_DELAY_MS, TimeUnit.MILLISECONDS);
			threadShouldThrow();
                    } catch (InterruptedException success){}
                }
            });

        try {
            t.start();
            Thread.sleep(SMALL_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }


    /**
     * take blocks interruptibly when empty
     */
    public void testTakeFromEmpty() {
        final SynchronousQueue q = new SynchronousQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.take();
			threadShouldThrow();
                    } catch (InterruptedException success){ }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }


    /**
     * put blocks interruptibly if no active taker
     */
    public void testFairBlockingPut() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        SynchronousQueue q = new SynchronousQueue(true);
                        q.put(zero);
                        threadShouldThrow();
                    } catch (InterruptedException ie){
                    }
                }});
        t.start();
        try {
           Thread.sleep(SHORT_DELAY_MS);
           t.interrupt();
           t.join();
        }
        catch (InterruptedException ie) {
	    unexpectedException();
        }
    }

    /**
     * put blocks waiting for take
     */
    public void testFairPutWithTake() {
        final SynchronousQueue q = new SynchronousQueue(true);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    int added = 0;
                    try {
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
			threadShouldThrow();
                    } catch (InterruptedException e){
                        assertTrue(added >= 1);
                    }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            q.take();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }

    /**
     * timed offer times out if elements not taken
     */
    public void testFairTimedOffer() {
        final SynchronousQueue q = new SynchronousQueue(true);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {

                        threadAssertFalse(q.offer(new Object(), SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        q.offer(new Object(), LONG_DELAY_MS, TimeUnit.MILLISECONDS);
			threadShouldThrow();
                    } catch (InterruptedException success){}
                }
            });

        try {
            t.start();
            Thread.sleep(SMALL_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }


    /**
     * take blocks interruptibly when empty
     */
    public void testFairTakeFromEmpty() {
        final SynchronousQueue q = new SynchronousQueue(true);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.take();
			threadShouldThrow();
                    } catch (InterruptedException success){ }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }

    /**
     * poll fails unless active taker
     */
    public void testPoll() {
        SynchronousQueue q = new SynchronousQueue();
	assertNull(q.poll());
    }

    /**
     * timed pool with zero timeout times out if no active taker
     */
    public void testTimedPoll0() {
        try {
            SynchronousQueue q = new SynchronousQueue();
            assertNull(q.poll(0, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e){
	    unexpectedException();
	}
    }

    /**
     * timed pool with nonzero timeout times out if no active taker
     */
    public void testTimedPoll() {
        try {
            SynchronousQueue q = new SynchronousQueue();
            assertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e){
	    unexpectedException();
	}
    }

    /**
     * Interrupted timed poll throws InterruptedException instead of
     * returning timeout status
     */
    public void testInterruptedTimedPoll() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        SynchronousQueue q = new SynchronousQueue();
                        assertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                    } catch (InterruptedException success){
                    }
                }});
        t.start();
        try {
           Thread.sleep(SHORT_DELAY_MS);
           t.interrupt();
           t.join();
        }
        catch (InterruptedException ie) {
	    unexpectedException();
        }
    }

    /**
     *  timed poll before a delayed offer fails; after offer succeeds;
     *  on interruption throws
     */
    public void testTimedPollWithOffer() {
        final SynchronousQueue q = new SynchronousQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
			threadShouldThrow();
                    } catch (InterruptedException success) { }
                }
            });
        try {
            t.start();
            Thread.sleep(SMALL_DELAY_MS);
            assertTrue(q.offer(zero, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }

    /**
     * Interrupted timed poll throws InterruptedException instead of
     * returning timeout status
     */
    public void testFairInterruptedTimedPoll() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        SynchronousQueue q = new SynchronousQueue(true);
                        assertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                    } catch (InterruptedException success){
                    }
                }});
        t.start();
        try {
           Thread.sleep(SHORT_DELAY_MS);
           t.interrupt();
           t.join();
        }
        catch (InterruptedException ie) {
	    unexpectedException();
        }
    }

    /**
     *  timed poll before a delayed offer fails; after offer succeeds;
     *  on interruption throws
     */
    public void testFairTimedPollWithOffer() {
        final SynchronousQueue q = new SynchronousQueue(true);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
			threadShouldThrow();
                    } catch (InterruptedException success) { }
                }
            });
        try {
            t.start();
            Thread.sleep(SMALL_DELAY_MS);
            assertTrue(q.offer(zero, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }


    /**
     * peek returns null
     */
    public void testPeek() {
        SynchronousQueue q = new SynchronousQueue();
	assertNull(q.peek());
    }

    /**
     * element throws NSEE
     */
    public void testElement() {
        SynchronousQueue q = new SynchronousQueue();
        try {
            q.element();
            shouldThrow();
        }
        catch (NoSuchElementException success) {}
    }

    /**
     * remove throws NSEE if no active taker
     */
    public void testRemove() {
        SynchronousQueue q = new SynchronousQueue();
        try {
            q.remove();
            shouldThrow();
        } catch (NoSuchElementException success){
	}
    }

    /**
     * remove(x) returns false
     */
    public void testRemoveElement() {
        SynchronousQueue q = new SynchronousQueue();
        assertFalse(q.remove(zero));
        assertTrue(q.isEmpty());
    }

    /**
     * contains returns false
     */
    public void testContains() {
        SynchronousQueue q = new SynchronousQueue();
        assertFalse(q.contains(zero));
    }

    /**
     * clear ensures isEmpty
     */
    public void testClear() {
        SynchronousQueue q = new SynchronousQueue();
        q.clear();
        assertTrue(q.isEmpty());
    }

    /**
     * containsAll returns false unless empty
     */
    public void testContainsAll() {
        SynchronousQueue q = new SynchronousQueue();
        Integer[] empty = new Integer[0];
        assertTrue(q.containsAll(Arrays.asList(empty)));
        Integer[] ints = new Integer[1]; ints[0] = zero;
        assertFalse(q.containsAll(Arrays.asList(ints)));
    }

    /**
     * retainAll returns false
     */
    public void testRetainAll() {
        SynchronousQueue q = new SynchronousQueue();
        Integer[] empty = new Integer[0];
        assertFalse(q.retainAll(Arrays.asList(empty)));
        Integer[] ints = new Integer[1]; ints[0] = zero;
        assertFalse(q.retainAll(Arrays.asList(ints)));
    }

    /**
     * removeAll returns false
     */
    public void testRemoveAll() {
        SynchronousQueue q = new SynchronousQueue();
        Integer[] empty = new Integer[0];
        assertFalse(q.removeAll(Arrays.asList(empty)));
        Integer[] ints = new Integer[1]; ints[0] = zero;
        assertFalse(q.containsAll(Arrays.asList(ints)));
    }


    /**
     * toArray is empty
     */
    public void testToArray() {
        SynchronousQueue q = new SynchronousQueue();
	Object[] o = q.toArray();
        assertEquals(o.length, 0);
    }

    /**
     * toArray(a) is nulled at position 0
     */
    public void testToArray2() {
        SynchronousQueue q = new SynchronousQueue();
	Integer[] ints = new Integer[1];
        assertNull(ints[0]);
    }

    /**
     * toArray(null) throws NPE
     */
    public void testToArray_BadArg() {
	try {
            SynchronousQueue q = new SynchronousQueue();
	    Object o[] = q.toArray(null);
	    shouldThrow();
	} catch(NullPointerException success){}
    }


    /**
     * iterator does not traverse any elements
     */
    public void testIterator() {
        SynchronousQueue q = new SynchronousQueue();
	Iterator it = q.iterator();
        assertFalse(it.hasNext());
        try {
            Object x = it.next();
            shouldThrow();
        }
        catch (NoSuchElementException success) {}
    }

    /**
     * iterator remove throws ISE
     */
    public void testIteratorRemove() {
        SynchronousQueue q = new SynchronousQueue();
	Iterator it = q.iterator();
        try {
            it.remove();
            shouldThrow();
        }
        catch (IllegalStateException success) {}
    }

    /**
     * toString returns a non-null string
     */
    public void testToString() {
        SynchronousQueue q = new SynchronousQueue();
        String s = q.toString();
        assertNotNull(s);
    }


    /**
     * offer transfers elements across Executor tasks
     */
    public void testOfferInExecutor() {
        final SynchronousQueue q = new SynchronousQueue();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        final Integer one = new Integer(1);

        executor.execute(new Runnable() {
            public void run() {
                threadAssertFalse(q.offer(one));
                try {
                    threadAssertTrue(q.offer(one, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS));
                    threadAssertEquals(0, q.remainingCapacity());
                }
                catch (InterruptedException e) {
                    threadUnexpectedException();
                }
            }
        });

        executor.execute(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(SMALL_DELAY_MS);
                    threadAssertEquals(one, q.take());
                }
                catch (InterruptedException e) {
                    threadUnexpectedException();
                }
            }
        });

        joinPool(executor);

    }

    /**
     * poll retrieves elements across Executor threads
     */
    public void testPollInExecutor() {
        final SynchronousQueue q = new SynchronousQueue();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(new Runnable() {
            public void run() {
                threadAssertNull(q.poll());
                try {
                    threadAssertTrue(null != q.poll(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS));
                    threadAssertTrue(q.isEmpty());
                }
                catch (InterruptedException e) {
                    threadUnexpectedException();
                }
            }
        });

        executor.execute(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(SMALL_DELAY_MS);
                    q.put(new Integer(1));
                }
                catch (InterruptedException e) {
                    threadUnexpectedException();
                }
            }
        });

        joinPool(executor);
    }

    /**
     * a deserialized serialized queue is usable
     *
    TODO(tball): enable when serialization is supported.
    public void testSerialization() {
        SynchronousQueue q = new SynchronousQueue();
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(q);
            out.close();

            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            SynchronousQueue r = (SynchronousQueue)in.readObject();
            assertEquals(q.size(), r.size());
            while (!q.isEmpty())
                assertEquals(q.remove(), r.remove());
        } catch(Exception e){
            e.printStackTrace();
            unexpectedException();
        }
    }
    */

    /**
     * drainTo(null) throws NPE
     */
    public void testDrainToNull() {
        SynchronousQueue q = new SynchronousQueue();
        try {
            q.drainTo(null);
            shouldThrow();
        } catch(NullPointerException success) {
        }
    }

    /**
     * drainTo(this) throws IAE
     */
    public void testDrainToSelf() {
        SynchronousQueue q = new SynchronousQueue();
        try {
            q.drainTo(q);
            shouldThrow();
        } catch(IllegalArgumentException success) {
        }
    }

    /**
     * drainTo(c) of empty queue doesn't transfer elements
     */
    public void testDrainTo() {
        SynchronousQueue q = new SynchronousQueue();
        ArrayList l = new ArrayList();
        q.drainTo(l);
        assertEquals(q.size(), 0);
        assertEquals(l.size(), 0);
    }

    /**
     * drainTo empties queue, unblocking a waiting put.
     */
    public void testDrainToWithActivePut() {
        final SynchronousQueue q = new SynchronousQueue();
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.put(new Integer(1));
                    } catch (InterruptedException ie){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t.start();
            ArrayList l = new ArrayList();
            Thread.sleep(SHORT_DELAY_MS);
            q.drainTo(l);
            assertTrue(l.size() <= 1);
            if (l.size() > 0)
                assertEquals(l.get(0), new Integer(1));
            t.join();
            assertTrue(l.size() <= 1);
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * drainTo(null, n) throws NPE
     */
    public void testDrainToNullN() {
        SynchronousQueue q = new SynchronousQueue();
        try {
            q.drainTo(null, 0);
            shouldThrow();
        } catch(NullPointerException success) {
        }
    }

    /**
     * drainTo(this, n) throws IAE
     */
    public void testDrainToSelfN() {
        SynchronousQueue q = new SynchronousQueue();
        try {
            q.drainTo(q, 0);
            shouldThrow();
        } catch(IllegalArgumentException success) {
        }
    }

    /**
     * drainTo(c, n) empties up to n elements of queue into c
     */
    public void testDrainToN() {
        final SynchronousQueue q = new SynchronousQueue();
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.put(one);
                    } catch (InterruptedException ie){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.put(two);
                    } catch (InterruptedException ie){
                        threadUnexpectedException();
                    }
                }
            });

        try {
            t1.start();
            t2.start();
            ArrayList l = new ArrayList();
            Thread.sleep(SHORT_DELAY_MS);
            q.drainTo(l, 1);
            assertTrue(l.size() == 1);
            q.drainTo(l, 1);
            assertTrue(l.size() == 2);
            assertTrue(l.contains(one));
            assertTrue(l.contains(two));
            t1.join();
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    }


}
