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
import java.math.BigInteger;
import java.security.*;

public class AbstractExecutorServiceTest extends JSR166TestCase{
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(AbstractExecutorServiceTest.class);
    }

    /**
     * A no-frills implementation of AbstractExecutorService, designed
     * to test the submit methods only.
     */
    static class DirectExecutorService extends AbstractExecutorService {
        public void execute(Runnable r) { r.run(); }
        public void shutdown() { shutdown = true; }
        public List<Runnable> shutdownNow() { shutdown = true; return Collections.EMPTY_LIST; }
        public boolean isShutdown() { return shutdown; }
        public boolean isTerminated() { return isShutdown(); }
        public boolean awaitTermination(long timeout, TimeUnit unit) { return isShutdown(); }
        private volatile boolean shutdown = false;
    }

    /**
     * execute(runnable) runs it to completion
     */
    public void testExecuteRunnable() {
        try {
            ExecutorService e = new DirectExecutorService();
            TrackedShortRunnable task = new TrackedShortRunnable();
            assertFalse(task.done);
            Future<?> future = e.submit(task);
            future.get();
            assertTrue(task.done);
        }
        catch (ExecutionException ex) {
            unexpectedException();
        }
        catch (InterruptedException ex) {
            unexpectedException();
        }
    }


    /**
     * Completed submit(callable) returns result
     */
    public void testSubmitCallable() {
        try {
            ExecutorService e = new DirectExecutorService();
            Future<String> future = e.submit(new StringTask());
            String result = future.get();
            assertSame(TEST_STRING, result);
        }
        catch (ExecutionException ex) {
            unexpectedException();
        }
        catch (InterruptedException ex) {
            unexpectedException();
        }
    }

    /**
     * Completed submit(runnable) returns successfully
     */
    public void testSubmitRunnable() {
        try {
            ExecutorService e = new DirectExecutorService();
            Future<?> future = e.submit(new NoOpRunnable());
            future.get();
            assertTrue(future.isDone());
        }
        catch (ExecutionException ex) {
            unexpectedException();
        }
        catch (InterruptedException ex) {
            unexpectedException();
        }
    }

    /**
     * Completed submit(runnable, result) returns result
     */
    public void testSubmitRunnable2() {
        try {
            ExecutorService e = new DirectExecutorService();
            Future<String> future = e.submit(new NoOpRunnable(), TEST_STRING);
            String result = future.get();
            assertSame(TEST_STRING, result);
        }
        catch (ExecutionException ex) {
            unexpectedException();
        }
        catch (InterruptedException ex) {
            unexpectedException();
        }
    }


    /**
     * A submitted privileged action to completion
     */
    public void testSubmitPrivilegedAction() {
        Policy savedPolicy = null;
        try {
            savedPolicy = Policy.getPolicy();
            AdjustablePolicy policy = new AdjustablePolicy();
            policy.addPermission(new RuntimePermission("getContextClassLoader"));
            policy.addPermission(new RuntimePermission("setContextClassLoader"));
            Policy.setPolicy(policy);
        } catch(AccessControlException ok) {
            return;
        }
        try {
            ExecutorService e = new DirectExecutorService();
            Future future = e.submit(Executors.callable(new PrivilegedAction() {
                    public Object run() {
                        return TEST_STRING;
                    }}));

            Object result = future.get();
            assertSame(TEST_STRING, result);
        }
        catch (ExecutionException ex) {
            unexpectedException();
        }
        catch (InterruptedException ex) {
            unexpectedException();
        }
        finally {
            try {
                Policy.setPolicy(savedPolicy);
            } catch(AccessControlException ok) {
                return;
            }
        }
    }

    /**
     * A submitted a privileged exception action runs to completion
     */
    public void testSubmitPrivilegedExceptionAction() {
        Policy savedPolicy = null;
        try {
            savedPolicy = Policy.getPolicy();
            AdjustablePolicy policy = new AdjustablePolicy();
            policy.addPermission(new RuntimePermission("getContextClassLoader"));
            policy.addPermission(new RuntimePermission("setContextClassLoader"));
            Policy.setPolicy(policy);
        } catch(AccessControlException ok) {
            return;
        }

        try {
            ExecutorService e = new DirectExecutorService();
            Future future = e.submit(Executors.callable(new PrivilegedExceptionAction() {
                    public Object run() {
                        return TEST_STRING;
                    }}));

            Object result = future.get();
            assertSame(TEST_STRING, result);
        }
        catch (ExecutionException ex) {
            unexpectedException();
        }
        catch (InterruptedException ex) {
            unexpectedException();
        }
        finally {
            Policy.setPolicy(savedPolicy);
        }
    }

    /**
     * A submitted failed privileged exception action reports exception
     */
    public void testSubmitFailedPrivilegedExceptionAction() {
        Policy savedPolicy = null;
        try {
            savedPolicy = Policy.getPolicy();
            AdjustablePolicy policy = new AdjustablePolicy();
            policy.addPermission(new RuntimePermission("getContextClassLoader"));
            policy.addPermission(new RuntimePermission("setContextClassLoader"));
            Policy.setPolicy(policy);
        } catch(AccessControlException ok) {
            return;
        }


        try {
            ExecutorService e = new DirectExecutorService();
            Future future = e.submit(Executors.callable(new PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        throw new IndexOutOfBoundsException();
                    }}));

            Object result = future.get();
            shouldThrow();
        }
        catch (ExecutionException success) {
        }
        catch (InterruptedException ex) {
            unexpectedException();
        }
        finally {
            Policy.setPolicy(savedPolicy);
        }
    }

    /**
     * execute(null runnable) throws NPE
     */
    public void testExecuteNullRunnable() {
        try {
            ExecutorService e = new DirectExecutorService();
            TrackedShortRunnable task = null;
            Future<?> future = e.submit(task);
            shouldThrow();
        }
        catch (NullPointerException success) {
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }


    /**
     * submit(null callable) throws NPE
     */
    public void testSubmitNullCallable() {
        try {
            ExecutorService e = new DirectExecutorService();
            StringTask t = null;
            Future<String> future = e.submit(t);
            shouldThrow();
        }
        catch (NullPointerException success) {
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }

    /**
     * submit(runnable) throws RejectedExecutionException if
     * executor is saturated.
     */
    public void testExecute1() {
        ThreadPoolExecutor p = new ThreadPoolExecutor(1,1, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1));
        try {

            for(int i = 0; i < 5; ++i){
                p.submit(new MediumRunnable());
            }
            shouldThrow();
        } catch(RejectedExecutionException success){}
        joinPool(p);
    }

    /**
     * submit(callable) throws RejectedExecutionException
     * if executor is saturated.
     */
    public void testExecute2() {
         ThreadPoolExecutor p = new ThreadPoolExecutor(1,1, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1));
        try {
            for(int i = 0; i < 5; ++i) {
                p.submit(new SmallCallable());
            }
            shouldThrow();
        } catch(RejectedExecutionException e){}
        joinPool(p);
    }


    /**
     *  Blocking on submit(callable) throws InterruptedException if
     *  caller interrupted.
     */
    public void testInterruptedSubmit() {
        final ThreadPoolExecutor p = new ThreadPoolExecutor(1,1,60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        p.submit(new Callable<Object>() {
                                public Object call() {
                                    try {
                                        Thread.sleep(MEDIUM_DELAY_MS);
                                        shouldThrow();
                                    } catch(InterruptedException e){
                                    }
                                    return null;
                                }
                            }).get();
                    } catch(InterruptedException success){
                    } catch(Exception e) {
                        unexpectedException();
                    }

                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
        } catch(Exception e){
            unexpectedException();
        }
        joinPool(p);
    }

    /**
     *  get of submitted callable throws Exception if callable
     *  interrupted
     */
    public void testSubmitIE() {
        final ThreadPoolExecutor p = new ThreadPoolExecutor(1,1,60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));

        final Callable c = new Callable() {
                public Object call() {
                    try {
                        p.submit(new SmallCallable()).get();
                        shouldThrow();
                    } catch(InterruptedException e){}
                    catch(RejectedExecutionException e2){}
                    catch(ExecutionException e3){}
                    return Boolean.TRUE;
                }
            };



        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.call();
                    } catch(Exception e){}
                }
          });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(InterruptedException e){
            unexpectedException();
        }

        joinPool(p);
    }

    /**
     *  get of submit(callable) throws ExecutionException if callable
     *  throws exception
     */
    public void testSubmitEE() {
        ThreadPoolExecutor p = new ThreadPoolExecutor(1,1,60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));

        try {
            Callable c = new Callable() {
                    public Object call() {
                        throw new AssertionError();
                    }
                };

            for(int i =0; i < 5; i++){
                p.submit(c).get();
            }

            shouldThrow();
        }
        catch(ExecutionException success){
        } catch(Exception e) {
            unexpectedException();
        }
        joinPool(p);
    }

    /**
     * invokeAny(null) throws NPE
     */
    public void testInvokeAny1() {
        ExecutorService e = new DirectExecutorService();
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
        ExecutorService e = new DirectExecutorService();
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
        ExecutorService e = new DirectExecutorService();
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(null);
            e.invokeAny(l);
        } catch (NullPointerException success) {
        } catch(Exception ex) {
            ex.printStackTrace();
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAny(c) throws ExecutionException if no task in c completes
     */
    public void testInvokeAny4() {
        ExecutorService e = new DirectExecutorService();
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new NPETask());
            e.invokeAny(l);
        } catch(ExecutionException success) {
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * invokeAny(c) returns result of some task in c if at least one completes
     */
    public void testInvokeAny5() {
        ExecutorService e = new DirectExecutorService();
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
        ExecutorService e = new DirectExecutorService();
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
        ExecutorService e = new DirectExecutorService();
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
        ExecutorService e = new DirectExecutorService();
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
     * get of returned element of invokeAll(c) throws exception on failed task
     */
    public void testInvokeAll4() {
        ExecutorService e = new DirectExecutorService();
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
     * invokeAll(c) returns results of all completed tasks in c
     */
    public void testInvokeAll5() {
        ExecutorService e = new DirectExecutorService();
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
        ExecutorService e = new DirectExecutorService();
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
     * timed invokeAny(null time unit) throws NPE
     */
    public void testTimedInvokeAnyNullTimeUnit() {
        ExecutorService e = new DirectExecutorService();
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
        ExecutorService e = new DirectExecutorService();
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
        ExecutorService e = new DirectExecutorService();
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
        ExecutorService e = new DirectExecutorService();
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
     * timed invokeAny(c) returns result of some task in c
     */
    public void testTimedInvokeAny5() {
        ExecutorService e = new DirectExecutorService();
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
        ExecutorService e = new DirectExecutorService();
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
     * timed invokeAll(null time unit) throws NPE
     */
    public void testTimedInvokeAllNullTimeUnit() {
        ExecutorService e = new DirectExecutorService();
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
        ExecutorService e = new DirectExecutorService();
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
        ExecutorService e = new DirectExecutorService();
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
     * get of returned element of invokeAll(c) throws exception on failed task
     */
    public void testTimedInvokeAll4() {
        ExecutorService e = new DirectExecutorService();
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
     * timed invokeAll(c) returns results of all completed tasks in c
     */
    public void testTimedInvokeAll5() {
        ExecutorService e = new DirectExecutorService();
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
     * timed invokeAll cancels tasks not completed by timeout
     */
    public void testTimedInvokeAll6() {
        ExecutorService e = new DirectExecutorService();
        try {
            ArrayList<Callable<String>> l = new ArrayList<Callable<String>>();
            l.add(new StringTask());
            l.add(Executors.callable(new MediumPossiblyInterruptedRunnable(), TEST_STRING));
            l.add(new StringTask());
            List<Future<String>> result = e.invokeAll(l, SMALL_DELAY_MS, TimeUnit.MILLISECONDS);
            assertEquals(3, result.size());
            Iterator<Future<String>> it = result.iterator();
            Future<String> f1 = it.next();
            Future<String> f2 = it.next();
            Future<String> f3 = it.next();
            assertTrue(f1.isDone());
            assertFalse(f1.isCancelled());
            assertTrue(f2.isDone());
            assertTrue(f3.isDone());
            assertTrue(f3.isCancelled());
        } catch(Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

}
