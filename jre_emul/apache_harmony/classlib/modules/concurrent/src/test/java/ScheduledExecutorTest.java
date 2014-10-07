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
import java.util.concurrent.atomic.*;

public class ScheduledExecutorTest extends JSR166TestCase {
    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());	
    }
    public static Test suite() {
	return new TestSuite(ScheduledExecutorTest.class);
    }


    /**
     * execute successfully executes a runnable
     */
    public void testExecute() {
	try {
            TrackedShortRunnable runnable =new TrackedShortRunnable();
            ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
	    p1.execute(runnable);
	    assertFalse(runnable.done);
	    Thread.sleep(SHORT_DELAY_MS);
	    try { p1.shutdown(); } catch(SecurityException ok) { return; }
	    try {
                Thread.sleep(MEDIUM_DELAY_MS);
            } catch(InterruptedException e){
                unexpectedException();
            }
	    assertTrue(runnable.done);
            try { p1.shutdown(); } catch(SecurityException ok) { return; }
            joinPool(p1);
        }
	catch(Exception e){
            unexpectedException();
        }
        
    }


    /**
     * delayed schedule of callable successfully executes after delay
     */
    public void testSchedule1() {
	try {
            TrackedCallable callable = new TrackedCallable();
            ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
	    Future f = p1.schedule(callable, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
	    assertFalse(callable.done);
	    Thread.sleep(MEDIUM_DELAY_MS);
	    assertTrue(callable.done);
	    assertEquals(Boolean.TRUE, f.get());
            try { p1.shutdown(); } catch(SecurityException ok) { return; }
            joinPool(p1);
	} catch(RejectedExecutionException e){}
	catch(Exception e){
            e.printStackTrace();
            unexpectedException();
        }
    }

    /**
     *  delayed schedule of runnable successfully executes after delay
     */
    public void testSchedule3() {
	try {
            TrackedShortRunnable runnable = new TrackedShortRunnable();
            ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
	    p1.schedule(runnable, SMALL_DELAY_MS, TimeUnit.MILLISECONDS);
	    Thread.sleep(SHORT_DELAY_MS);
	    assertFalse(runnable.done);
	    Thread.sleep(MEDIUM_DELAY_MS);
	    assertTrue(runnable.done);
            try { p1.shutdown(); } catch(SecurityException ok) { return; }
            joinPool(p1);
        } catch(Exception e){
            unexpectedException();
        }
    }
    
    /**
     * scheduleAtFixedRate executes runnable after given initial delay
     */
    public void testSchedule4() {
	try {
            TrackedShortRunnable runnable = new TrackedShortRunnable();
            ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
	    ScheduledFuture h = p1.scheduleAtFixedRate(runnable, SHORT_DELAY_MS, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
	    assertFalse(runnable.done);
	    Thread.sleep(MEDIUM_DELAY_MS);
	    assertTrue(runnable.done);
            h.cancel(true);
            joinPool(p1);
        } catch(Exception e){
            unexpectedException();
        }
    }

    static class RunnableCounter implements Runnable {
        AtomicInteger count = new AtomicInteger(0);
        public void run() { count.getAndIncrement(); }
    }

    /**
     * scheduleWithFixedDelay executes runnable after given initial delay
     */
    public void testSchedule5() {
	try {
            TrackedShortRunnable runnable = new TrackedShortRunnable();
            ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
	    ScheduledFuture h = p1.scheduleWithFixedDelay(runnable, SHORT_DELAY_MS, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
	    assertFalse(runnable.done);
	    Thread.sleep(MEDIUM_DELAY_MS);
	    assertTrue(runnable.done);
            h.cancel(true);
            joinPool(p1);
        } catch(Exception e){
            unexpectedException();
        }
    }
    
    /**
     * scheduleAtFixedRate executes series of tasks at given rate
     */
    public void testFixedRateSequence() {
	try {
            ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
            RunnableCounter counter = new RunnableCounter();
	    ScheduledFuture h = 
                p1.scheduleAtFixedRate(counter, 0, 1, TimeUnit.MILLISECONDS);
	    Thread.sleep(SMALL_DELAY_MS);
            h.cancel(true);
            int c = counter.count.get();
            // By time scaling conventions, we must have at least
            // an execution per SHORT delay, but no more than one SHORT more
            assertTrue(c >= SMALL_DELAY_MS / SHORT_DELAY_MS);
            assertTrue(c <= SMALL_DELAY_MS + SHORT_DELAY_MS);
            joinPool(p1);
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * scheduleWithFixedDelay executes series of tasks with given period
     */
    public void testFixedDelaySequence() {
	try {
            ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
            RunnableCounter counter = new RunnableCounter();
	    ScheduledFuture h = 
                p1.scheduleWithFixedDelay(counter, 0, 1, TimeUnit.MILLISECONDS);
	    Thread.sleep(SMALL_DELAY_MS);
            h.cancel(true);
            int c = counter.count.get();
            assertTrue(c >= SMALL_DELAY_MS / SHORT_DELAY_MS);
            assertTrue(c <= SMALL_DELAY_MS + SHORT_DELAY_MS);
            joinPool(p1);
        } catch(Exception e){
            unexpectedException();
        }
    }


    /**
     *  execute (null) throws NPE
     */
    public void testExecuteNull() {
        ScheduledThreadPoolExecutor se = null;
        try {
	    se = new ScheduledThreadPoolExecutor(1);
	    se.execute(null);
            shouldThrow();
	} catch(NullPointerException success){}
	catch(Exception e){
            unexpectedException();
        }
	
	joinPool(se);
    }

    /**
     * schedule (null) throws NPE
     */
    public void testScheduleNull() {
        ScheduledThreadPoolExecutor se = new ScheduledThreadPoolExecutor(1);
	try {
            TrackedCallable callable = null;
	    Future f = se.schedule(callable, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
            shouldThrow();
	} catch(NullPointerException success){}
	catch(Exception e){
            unexpectedException();
        }
	joinPool(se);
    }
   
    /**
     * execute throws RejectedExecutionException if shutdown
     */
    public void testSchedule1_RejectedExecutionException() {
        ScheduledThreadPoolExecutor se = new ScheduledThreadPoolExecutor(1);
        try {
            se.shutdown();
            se.schedule(new NoOpRunnable(),
                        MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
            shouldThrow();
        } catch(RejectedExecutionException success){
        } catch (SecurityException ok) {
        }
        
        joinPool(se);

    }

    /**
     * schedule throws RejectedExecutionException if shutdown
     */
    public void testSchedule2_RejectedExecutionException() {
        ScheduledThreadPoolExecutor se = new ScheduledThreadPoolExecutor(1);
        try {
            se.shutdown();
            se.schedule(new NoOpCallable(),
                        MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
            shouldThrow();
        } catch(RejectedExecutionException success){
        } catch (SecurityException ok) {
        }
        joinPool(se);
    }

    /**
     * schedule callable throws RejectedExecutionException if shutdown
     */
     public void testSchedule3_RejectedExecutionException() {
         ScheduledThreadPoolExecutor se = new ScheduledThreadPoolExecutor(1);
         try {
            se.shutdown();
            se.schedule(new NoOpCallable(),
                        MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
            shouldThrow();
        } catch(RejectedExecutionException success){
        } catch (SecurityException ok) {
        }
         joinPool(se);
    }

    /**
     *  scheduleAtFixedRate throws RejectedExecutionException if shutdown
     */
    public void testScheduleAtFixedRate1_RejectedExecutionException() {
        ScheduledThreadPoolExecutor se = new ScheduledThreadPoolExecutor(1);
        try {
            se.shutdown();
            se.scheduleAtFixedRate(new NoOpRunnable(),
                                   MEDIUM_DELAY_MS, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
            shouldThrow();
        } catch(RejectedExecutionException success){
        } catch (SecurityException ok) {
        } 
        joinPool(se);
    }
    
    /**
     * scheduleWithFixedDelay throws RejectedExecutionException if shutdown
     */
    public void testScheduleWithFixedDelay1_RejectedExecutionException() {
        ScheduledThreadPoolExecutor se = new ScheduledThreadPoolExecutor(1);
        try {
            se.shutdown();
            se.scheduleWithFixedDelay(new NoOpRunnable(),
                                      MEDIUM_DELAY_MS, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
            shouldThrow();
        } catch(RejectedExecutionException success){
        } catch (SecurityException ok) {
        } 
        joinPool(se);
    }

    /**
     *  getActiveCount increases but doesn't overestimate, when a
     *  thread becomes active
     */
    public void testGetActiveCount() {
        ScheduledThreadPoolExecutor p2 = new ScheduledThreadPoolExecutor(2);
        assertEquals(0, p2.getActiveCount());
        p2.execute(new SmallRunnable());
        try {
            Thread.sleep(SHORT_DELAY_MS);
        } catch(Exception e){
            unexpectedException();
        }
        assertEquals(1, p2.getActiveCount());
        joinPool(p2);
    }
    
    /**
     *    getCompletedTaskCount increases, but doesn't overestimate,
     *   when tasks complete
     */
    public void testGetCompletedTaskCount() {
        ScheduledThreadPoolExecutor p2 = new ScheduledThreadPoolExecutor(2);
        assertEquals(0, p2.getCompletedTaskCount());
        p2.execute(new SmallRunnable());
        try {
            Thread.sleep(MEDIUM_DELAY_MS);
        } catch(Exception e){
            unexpectedException();
        }
        assertEquals(1, p2.getCompletedTaskCount());
        joinPool(p2);
    }
    
    /**
     *  getCorePoolSize returns size given in constructor if not otherwise set 
     */
    public void testGetCorePoolSize() {
        ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
        assertEquals(1, p1.getCorePoolSize());
        joinPool(p1);
    }
    
    /**
     *    getLargestPoolSize increases, but doesn't overestimate, when
     *   multiple threads active
     */
    public void testGetLargestPoolSize() {
        ScheduledThreadPoolExecutor p2 = new ScheduledThreadPoolExecutor(2);
        assertEquals(0, p2.getLargestPoolSize());
        p2.execute(new SmallRunnable());
        p2.execute(new SmallRunnable());
        try {
            Thread.sleep(SHORT_DELAY_MS);
        } catch(Exception e){
            unexpectedException();
        }
        assertEquals(2, p2.getLargestPoolSize());
        joinPool(p2);
    }
    
    /**
     *   getPoolSize increases, but doesn't overestimate, when threads
     *   become active
     */
    public void testGetPoolSize() {
        ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
        assertEquals(0, p1.getPoolSize());
        p1.execute(new SmallRunnable());
        assertEquals(1, p1.getPoolSize());
        joinPool(p1);
    }
    
    /**
     *    getTaskCount increases, but doesn't overestimate, when tasks
     *    submitted
     */
    public void testGetTaskCount() {
        ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
        assertEquals(0, p1.getTaskCount());
        for(int i = 0; i < 5; i++)
            p1.execute(new SmallRunnable());
        try {
            Thread.sleep(SHORT_DELAY_MS);
        } catch(Exception e){
            unexpectedException();
        }
        assertEquals(5, p1.getTaskCount());
        joinPool(p1);
    }

    /** 
     * getThreadFactory returns factory in constructor if not set
     */
    public void testGetThreadFactory() {
        ThreadFactory tf = new SimpleThreadFactory();
	ScheduledThreadPoolExecutor p = new ScheduledThreadPoolExecutor(1, tf);
        assertSame(tf, p.getThreadFactory());
        joinPool(p);
    }

    /** 
     * setThreadFactory sets the thread factory returned by getThreadFactory
     */
    public void testSetThreadFactory() {
        ThreadFactory tf = new SimpleThreadFactory();
	ScheduledThreadPoolExecutor p = new ScheduledThreadPoolExecutor(1);
        p.setThreadFactory(tf);
        assertSame(tf, p.getThreadFactory());
        joinPool(p);
    }

    /** 
     * setThreadFactory(null) throws NPE
     */
    public void testSetThreadFactoryNull() {
	ScheduledThreadPoolExecutor p = new ScheduledThreadPoolExecutor(1);
        try {
            p.setThreadFactory(null);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(p);
        }
    }
    
    /**
     *   is isShutDown is false before shutdown, true after
     */
    public void testIsShutdown() {
        
	ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
        try {
            assertFalse(p1.isShutdown());
        }
        finally {
            try { p1.shutdown(); } catch(SecurityException ok) { return; }
        }
	assertTrue(p1.isShutdown());
    }

        
    /**
     *   isTerminated is false before termination, true after
     */
    public void testIsTerminated() {
	ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
        try {
            p1.execute(new SmallRunnable());
        } finally {
            try { p1.shutdown(); } catch(SecurityException ok) { return; }
        }
        try {
	    assertTrue(p1.awaitTermination(LONG_DELAY_MS, TimeUnit.MILLISECONDS));
            assertTrue(p1.isTerminated());
	} catch(Exception e){
            unexpectedException();
        }	
    }

    /**
     *  isTerminating is not true when running or when terminated
     */
    public void testIsTerminating() {
	ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
        assertFalse(p1.isTerminating());
        try {
            p1.execute(new SmallRunnable());
            assertFalse(p1.isTerminating());
        } finally {
            try { p1.shutdown(); } catch(SecurityException ok) { return; }
        }
        try {
	    assertTrue(p1.awaitTermination(LONG_DELAY_MS, TimeUnit.MILLISECONDS));
            assertTrue(p1.isTerminated());
            assertFalse(p1.isTerminating());
	} catch(Exception e){
            unexpectedException();
        }	
    }

    /**
     * getQueue returns the work queue, which contains queued tasks
     */
    public void testGetQueue() {
        ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
        ScheduledFuture[] tasks = new ScheduledFuture[5];
        for(int i = 0; i < 5; i++){
            tasks[i] = p1.schedule(new SmallPossiblyInterruptedRunnable(), 1, TimeUnit.MILLISECONDS);
        }
        try {
            Thread.sleep(SHORT_DELAY_MS);
            BlockingQueue<Runnable> q = p1.getQueue();
            assertTrue(q.contains(tasks[4]));
            assertFalse(q.contains(tasks[0]));
        } catch(Exception e) {
            unexpectedException();
        } finally {
            joinPool(p1);
        }
    }

    /**
     * remove(task) removes queued task, and fails to remove active task
     */
    public void testRemove() {
        ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
        ScheduledFuture[] tasks = new ScheduledFuture[5];
        for(int i = 0; i < 5; i++){
            tasks[i] = p1.schedule(new SmallPossiblyInterruptedRunnable(), 1, TimeUnit.MILLISECONDS);
        }
        try {
            Thread.sleep(SHORT_DELAY_MS);
            BlockingQueue<Runnable> q = p1.getQueue();
            assertFalse(p1.remove((Runnable)tasks[0]));
            assertTrue(q.contains((Runnable)tasks[4]));
            assertTrue(q.contains((Runnable)tasks[3]));
            assertTrue(p1.remove((Runnable)tasks[4]));
            assertFalse(p1.remove((Runnable)tasks[4]));
            assertFalse(q.contains((Runnable)tasks[4]));
            assertTrue(q.contains((Runnable)tasks[3]));
            assertTrue(p1.remove((Runnable)tasks[3]));
            assertFalse(q.contains((Runnable)tasks[3]));
        } catch(Exception e) {
            unexpectedException();
        } finally {
            joinPool(p1);
        }
    }

    /**
     *  purge removes cancelled tasks from the queue
     */
    public void testPurge() {
        ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
        ScheduledFuture[] tasks = new ScheduledFuture[5];
        for(int i = 0; i < 5; i++){
            tasks[i] = p1.schedule(new SmallPossiblyInterruptedRunnable(), SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
        }
        try {
            int max = 5;
            if (tasks[4].cancel(true)) --max;
            if (tasks[3].cancel(true)) --max;
            // There must eventually be an interference-free point at
            // which purge will not fail. (At worst, when queue is empty.)
            int k;
            for (k = 0; k < SMALL_DELAY_MS; ++k) {
                p1.purge();
                long count = p1.getTaskCount();
                if (count >= 0 && count <= max)
                    break;
                Thread.sleep(1);
            }
            assertTrue(k < SMALL_DELAY_MS);
        } catch(Exception e) {
            unexpectedException();
        } finally {
            joinPool(p1);
        }
    }

    /**
     *  shutDownNow returns a list containing tasks that were not run
     */
    public void testShutDownNow() {
	ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
        for(int i = 0; i < 5; i++)
            p1.schedule(new SmallPossiblyInterruptedRunnable(), SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
        List l;
        try {
            l = p1.shutdownNow();
        } catch (SecurityException ok) { 
            return;
        }
	assertTrue(p1.isShutdown());
	assertTrue(l.size() > 0 && l.size() <= 5);
        joinPool(p1);
    }

    /**
     * In default setting, shutdown cancels periodic but not delayed
     * tasks at shutdown
     */
    public void testShutDown1() {
        try {
            ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
            assertTrue(p1.getExecuteExistingDelayedTasksAfterShutdownPolicy());
            assertFalse(p1.getContinueExistingPeriodicTasksAfterShutdownPolicy());

            ScheduledFuture[] tasks = new ScheduledFuture[5];
            for(int i = 0; i < 5; i++)
                tasks[i] = p1.schedule(new NoOpRunnable(), SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
            try { p1.shutdown(); } catch(SecurityException ok) { return; }
            BlockingQueue q = p1.getQueue();
            for (Iterator it = q.iterator(); it.hasNext();) {
                ScheduledFuture t = (ScheduledFuture)it.next();
                assertFalse(t.isCancelled());
            }
            assertTrue(p1.isShutdown());
            Thread.sleep(SMALL_DELAY_MS);
            for (int i = 0; i < 5; ++i) {
                assertTrue(tasks[i].isDone());
                assertFalse(tasks[i].isCancelled());
            }
            
        }
        catch(Exception ex) {
            unexpectedException();
        }
    }


    /**
     * If setExecuteExistingDelayedTasksAfterShutdownPolicy is false,
     * delayed tasks are cancelled at shutdown
     */
    public void testShutDown2() {
        try {
            ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
            p1.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            ScheduledFuture[] tasks = new ScheduledFuture[5];
            for(int i = 0; i < 5; i++)
                tasks[i] = p1.schedule(new NoOpRunnable(), SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
            try { p1.shutdown(); } catch(SecurityException ok) { return; }
            assertTrue(p1.isShutdown());
            BlockingQueue q = p1.getQueue();
            assertTrue(q.isEmpty());
            Thread.sleep(SMALL_DELAY_MS);
            assertTrue(p1.isTerminated());
        }
        catch(Exception ex) {
            unexpectedException();
        }
    }


    /**
     * If setContinueExistingPeriodicTasksAfterShutdownPolicy is set false,
     * periodic tasks are not cancelled at shutdown
     */
    public void testShutDown3() {
        try {
            ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
            p1.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
            ScheduledFuture task =
                p1.scheduleAtFixedRate(new NoOpRunnable(), 5, 5, TimeUnit.MILLISECONDS);
            try { p1.shutdown(); } catch(SecurityException ok) { return; }
            assertTrue(p1.isShutdown());
            BlockingQueue q = p1.getQueue();
            assertTrue(q.isEmpty());
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(p1.isTerminated());
        }
        catch(Exception ex) {
            unexpectedException();
        }
    }

    /**
     * if setContinueExistingPeriodicTasksAfterShutdownPolicy is true,
     * periodic tasks are cancelled at shutdown
     */
    public void testShutDown4() {
        ScheduledThreadPoolExecutor p1 = new ScheduledThreadPoolExecutor(1);
        try {
            p1.setContinueExistingPeriodicTasksAfterShutdownPolicy(true);
            ScheduledFuture task =
                p1.scheduleAtFixedRate(new NoOpRunnable(), 1, 1, TimeUnit.MILLISECONDS);
            assertFalse(task.isCancelled());
            try { p1.shutdown(); } catch(SecurityException ok) { return; }
            assertFalse(task.isCancelled());
            assertFalse(p1.isTerminated());
            assertTrue(p1.isShutdown());
            Thread.sleep(SHORT_DELAY_MS);
            assertFalse(task.isCancelled());
            assertTrue(task.cancel(true));
            assertTrue(task.isDone());
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(p1.isTerminated());
        }
        catch(Exception ex) {
            unexpectedException();
        }
        finally { 
            joinPool(p1);
        }
    }

    /**
     * completed submit of callable returns result
     */
    public void testSubmitCallable() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            Future<String> future = e.submit(new StringTask());
            String result = future.get();
            assertSame(TEST_STRING, result);
        }
        catch (ExecutionException ex) {
            unexpectedException();
        }
        catch (InterruptedException ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * completed submit of runnable returns successfully
     */
    public void testSubmitRunnable() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            Future<?> future = e.submit(new NoOpRunnable());
            future.get();
            assertTrue(future.isDone());
        }
        catch (ExecutionException ex) {
            unexpectedException();
        }
        catch (InterruptedException ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * completed submit of (runnable, result) returns result
     */
    public void testSubmitRunnable2() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            Future<String> future = e.submit(new NoOpRunnable(), TEST_STRING);
            String result = future.get();
            assertSame(TEST_STRING, result);
        }
        catch (ExecutionException ex) {
            unexpectedException();
        }
        catch (InterruptedException ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAny(null) throws NPE
     */
    public void testInvokeAny1() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            e.invokeAny(null);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAny(empty collection) throws IAE
     */
    public void testInvokeAny2() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            e.invokeAny(new ArrayList<Callable<String>>());
        } catch (IllegalArgumentException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAny(c) throws NPE if c has null elements
     */
    public void testInvokeAny3() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(null);
            e.invokeAny(l);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAny(c) throws ExecutionException if no task completes
     */
    public void testInvokeAny4() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new NPETask());
            e.invokeAny(l);
        } catch (ExecutionException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAny(c) returns result of some task
     */
    public void testInvokeAny5() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(new StringTask());
            String result = e.invokeAny(l);
            assertSame(TEST_STRING, result);
        } catch (ExecutionException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAll(null) throws NPE
     */
    public void testInvokeAll1() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            e.invokeAll(null);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAll(empty collection) returns empty collection
     */
    public void testInvokeAll2() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            List<Future<String>> r = e.invokeAll(new ArrayList<Callable<String>>());
            assertTrue(r.isEmpty());
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAll(c) throws NPE if c has null elements
     */
    public void testInvokeAll3() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(null);
            e.invokeAll(l);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * get of invokeAll(c) throws exception on failed task
     */
    public void testInvokeAll4() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new NPETask());
            List<Future<String>> result = e.invokeAll(l);
            assertEquals(1, result.size());
            for (Iterator<Future<String>> it = result.iterator(); it.hasNext();) 
                it.next().get();
        } catch(ExecutionException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAll(c) returns results of all completed tasks
     */
    public void testInvokeAll5() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(new StringTask());
            List<Future<String>> result = e.invokeAll(l);
            assertEquals(2, result.size());
            for (Iterator<Future<String>> it = result.iterator(); it.hasNext();) 
                assertSame(TEST_STRING, it.next().get());
        } catch (ExecutionException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAny(null) throws NPE
     */
    public void testTimedInvokeAny1() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            e.invokeAny(null, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAny(,,null) throws NPE
     */
    public void testTimedInvokeAnyNullTimeUnit() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            e.invokeAny(l, MEDIUM_DELAY_MS, null);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAny(empty collection) throws IAE
     */
    public void testTimedInvokeAny2() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            e.invokeAny(new ArrayList<Callable<String>>(), MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
        } catch (IllegalArgumentException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAny(c) throws NPE if c has null elements
     */
    public void testTimedInvokeAny3() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(null);
            e.invokeAny(l, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            ex.printStackTrace();
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAny(c) throws ExecutionException if no task completes
     */
    public void testTimedInvokeAny4() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new NPETask());
            e.invokeAny(l, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
        } catch(ExecutionException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAny(c) returns result of some task
     */
    public void testTimedInvokeAny5() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(new StringTask());
            String result = e.invokeAny(l, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
            assertSame(TEST_STRING, result);
        } catch (ExecutionException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAll(null) throws NPE
     */
    public void testTimedInvokeAll1() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            e.invokeAll(null, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAll(,,null) throws NPE
     */
    public void testTimedInvokeAllNullTimeUnit() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            e.invokeAll(l, MEDIUM_DELAY_MS, null);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAll(empty collection) returns empty collection
     */
    public void testTimedInvokeAll2() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            List<Future<String>> r = e.invokeAll(new ArrayList<Callable<String>>(), MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
            assertTrue(r.isEmpty());
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAll(c) throws NPE if c has null elements
     */
    public void testTimedInvokeAll3() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(null);
            e.invokeAll(l, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * get of element of invokeAll(c) throws exception on failed task
     */
    public void testTimedInvokeAll4() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new NPETask());
            List<Future<String>> result = e.invokeAll(l, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
            assertEquals(1, result.size());
            for (Iterator<Future<String>> it = result.iterator(); it.hasNext();) 
                it.next().get();
        } catch(ExecutionException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAll(c) returns results of all completed tasks
     */
    public void testTimedInvokeAll5() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(new StringTask());
            List<Future<String>> result = e.invokeAll(l, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
            assertEquals(2, result.size());
            for (Iterator<Future<String>> it = result.iterator(); it.hasNext();) 
                assertSame(TEST_STRING, it.next().get());
        } catch (ExecutionException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * timed invokeAll(c) cancels tasks not completed by timeout
     */
    public void testTimedInvokeAll6() {
        ExecutorService e = new ScheduledThreadPoolExecutor(2);
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(Executors.callable(new MediumPossiblyInterruptedRunnable(), TEST_STRING));
            l.add(new StringTask());
            List<Future<String>> result = e.invokeAll(l, SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
            assertEquals(3, result.size());
            Iterator<Future<String>> it = result.iterator(); 
            Future<String> f1 = it.next();
            Future<String> f2 = it.next();
            Future<String> f3 = it.next();
            assertTrue(f1.isDone());
            assertTrue(f2.isDone());
            assertTrue(f3.isDone());
            assertFalse(f1.isCancelled());
            assertTrue(f2.isCancelled());
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }


}
