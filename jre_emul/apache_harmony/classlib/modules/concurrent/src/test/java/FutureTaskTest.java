/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

import junit.framework.*;
import java.util.concurrent.*;
import java.util.*;

public class FutureTaskTest extends JSR166TestCase {

    public static void main(String[] args) {
	junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
	return new TestSuite(FutureTaskTest.class);
    }

    /**
     * Subclass to expose protected methods
     */
    static class PublicFutureTask extends FutureTask {
        public PublicFutureTask(Callable r) { super(r); }
        public boolean runAndReset() { return super.runAndReset(); }
        public void set(Object x) { super.set(x); }
        public void setException(Throwable t) { super.setException(t); }
    }

    /**
     * Creating a future with a null callable throws NPE
     */
    public void testConstructor() {
        try {
            FutureTask task = new FutureTask(null);
            shouldThrow();
        }
        catch(NullPointerException success) {
        }
    }

    /**
     * creating a future with null runnable fails
     */
    public void testConstructor2() {
        try {
            FutureTask task = new FutureTask(null, Boolean.TRUE);
            shouldThrow();
        }
        catch(NullPointerException success) {
        }
    }

    /**
     * isDone is true when a task completes
     */
    public void testIsDone() {
        FutureTask task = new FutureTask( new NoOpCallable());
	task.run();
	assertTrue(task.isDone());
	assertFalse(task.isCancelled());
    }

    /**
     * runAndReset of a non-cancelled task succeeds
     */
    public void testRunAndReset() {
        PublicFutureTask task = new PublicFutureTask(new NoOpCallable());
	assertTrue(task.runAndReset());
        assertFalse(task.isDone());
    }

    /**
     * runAndReset after cancellation fails
     */
    public void testResetAfterCancel() {
        PublicFutureTask task = new PublicFutureTask(new NoOpCallable());
        assertTrue(task.cancel(false));
	assertFalse(task.runAndReset());
	assertTrue(task.isDone());
	assertTrue(task.isCancelled());
    }



    /**
     * setting value causes get to return it
     */
    public void testSet() {
        PublicFutureTask task = new PublicFutureTask(new NoOpCallable());
        task.set(one);
        try {
            assertEquals(task.get(), one);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }

    /**
     * setException causes get to throw ExecutionException
     */
    public void testSetException() {
        Exception nse = new NoSuchElementException();
        PublicFutureTask task = new PublicFutureTask(new NoOpCallable());
        task.setException(nse);
        try {
            Object x = task.get();
            shouldThrow();
        }
        catch(ExecutionException ee) {
            Throwable cause = ee.getCause();
            assertEquals(cause, nse);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }

    /**
     *  Cancelling before running succeeds
     */
    public void testCancelBeforeRun() {
        FutureTask task = new FutureTask( new NoOpCallable());
        assertTrue(task.cancel(false));
	task.run();
	assertTrue(task.isDone());
	assertTrue(task.isCancelled());
    }

    /**
     * Cancel(true) before run succeeds
     */
    public void testCancelBeforeRun2() {
        FutureTask task = new FutureTask( new NoOpCallable());
        assertTrue(task.cancel(true));
	task.run();
	assertTrue(task.isDone());
	assertTrue(task.isCancelled());
    }

    /**
     * cancel of a completed task fails
     */
    public void testCancelAfterRun() {
        FutureTask task = new FutureTask( new NoOpCallable());
	task.run();
        assertFalse(task.cancel(false));
	assertTrue(task.isDone());
	assertFalse(task.isCancelled());
    }

    /**
     * cancel(true) interrupts a running task
     */
    public void testCancelInterrupt() {
        FutureTask task = new FutureTask( new Callable() {
                public Object call() {
                    try {
                        Thread.sleep(MEDIUM_DELAY_MS);
                        threadShouldThrow();
                    }
                    catch (InterruptedException success) {}
                    return Boolean.TRUE;
                } });
        Thread t = new  Thread(task);
        t.start();

        try {
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(task.cancel(true));
            t.join();
            assertTrue(task.isDone());
            assertTrue(task.isCancelled());
        } catch(InterruptedException e){
            unexpectedException();
        }
    }


    /**
     * cancel(false) does not interrupt a running task
     */
    public void testCancelNoInterrupt() {
        FutureTask task = new FutureTask( new Callable() {
                public Object call() {
                    try {
                        Thread.sleep(MEDIUM_DELAY_MS);
                    }
                    catch (InterruptedException success) {
                        threadFail("should not interrupt");
                    }
                    return Boolean.TRUE;
                } });
        Thread t = new  Thread(task);
        t.start();

        try {
            Thread.sleep(SHORT_DELAY_MS);
            assertTrue(task.cancel(false));
            t.join();
            assertTrue(task.isDone());
            assertTrue(task.isCancelled());
        } catch(InterruptedException e){
            unexpectedException();
        }
    }

    /**
     * set in one thread causes get in another thread to retrieve value
     */
    public void testGet1() {
	final FutureTask ft = new FutureTask(new Callable() {
		public Object call() {
		    try {
			Thread.sleep(MEDIUM_DELAY_MS);
		    } catch(InterruptedException e){
                        threadUnexpectedException();
                    }
                    return Boolean.TRUE;
		}
	});
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			ft.get();
		    } catch(Exception e){
                        threadUnexpectedException();
                    }
		}
	    });
	try {
            assertFalse(ft.isDone());
            assertFalse(ft.isCancelled());
            t.start();
	    Thread.sleep(SHORT_DELAY_MS);
	    ft.run();
	    t.join();
	    assertTrue(ft.isDone());
            assertFalse(ft.isCancelled());
	} catch(InterruptedException e){
            unexpectedException();

        }
    }

    /**
     * set in one thread causes timed get in another thread to retrieve value
     */
    public void testTimedGet1() {
	final FutureTask ft = new FutureTask(new Callable() {
		public Object call() {
		    try {
			Thread.sleep(MEDIUM_DELAY_MS);
		    } catch(InterruptedException e){
                        threadUnexpectedException();
                    }
                    return Boolean.TRUE;
		}
            });
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			ft.get(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
		    } catch(TimeoutException success) {
                    } catch(Exception e){
                        threadUnexpectedException();
                    }
		}
	    });
	try {
            assertFalse(ft.isDone());
            assertFalse(ft.isCancelled());
            t.start();
	    ft.run();
	    t.join();
	    assertTrue(ft.isDone());
            assertFalse(ft.isCancelled());
	} catch(InterruptedException e){
            unexpectedException();

        }
    }

    /**
     *  Cancelling a task causes timed get in another thread to throw CancellationException
     */
    public void testTimedGet_Cancellation() {
	final FutureTask ft = new FutureTask(new Callable() {
		public Object call() {
		    try {
			Thread.sleep(SMALL_DELAY_MS);
                        threadShouldThrow();
		    } catch(InterruptedException e) {
                    }
		    return Boolean.TRUE;
		}
	    });
	try {
	    Thread t1 = new Thread(new Runnable() {
		    public void run() {
			try {
			    ft.get(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
			    threadShouldThrow();
			} catch(CancellationException success) {}
			catch(Exception e){
                            threadUnexpectedException();
			}
		    }
		});
            Thread t2 = new Thread(ft);
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
	    ft.cancel(true);
	    t1.join();
	    t2.join();
	} catch(InterruptedException ie){
            unexpectedException();
        }
    }

    /**
     * Cancelling a task causes get in another thread to throw CancellationException
     */
    public void testGet_Cancellation() {
	final FutureTask ft = new FutureTask(new Callable() {
		public Object call() {
		    try {
			Thread.sleep(MEDIUM_DELAY_MS);
                        threadShouldThrow();
		    } catch(InterruptedException e){
                    }
                    return Boolean.TRUE;
		}
	    });
	try {
	    Thread t1 = new Thread(new Runnable() {
		    public void run() {
			try {
			    ft.get();
			    threadShouldThrow();
			} catch(CancellationException success){
                        }
			catch(Exception e){
                            threadUnexpectedException();
                        }
		    }
		});
            Thread t2 = new Thread(ft);
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
	    ft.cancel(true);
	    t1.join();
	    t2.join();
	} catch(InterruptedException success){
            unexpectedException();
        }
    }


    /**
     * A runtime exception in task causes get to throw ExecutionException
     */
    public void testGet_ExecutionException() {
        final FutureTask ft = new FutureTask(new Callable() {
            public Object call() {
                throw new AssertionError();
            }
        });
        try {
            ft.run();
            ft.get();
            shouldThrow();
        } catch(ExecutionException success){
        }
        catch(Exception e){
            unexpectedException();
        }
    }

    /**
     *  A runtime exception in task causes timed get to throw ExecutionException
     */
    public void testTimedGet_ExecutionException2() {
        final FutureTask ft = new FutureTask(new Callable() {
            public Object call() {
                throw new AssertionError();
            }
        });
        try {
          ft.run();
          ft.get(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
          shouldThrow();
        } catch (ExecutionException success) {
        } catch (TimeoutException success) {
        } // unlikely but OK
        catch (Exception e) {
          unexpectedException();
        }
    }


    /**
     * Interrupting a waiting get causes it to throw InterruptedException
     */
    public void testGet_InterruptedException() {
	final FutureTask ft = new FutureTask(new NoOpCallable());
	Thread t = new Thread(new Runnable() {
		public void run() {
		    try {
			ft.get();
			threadShouldThrow();
		    } catch(InterruptedException success){
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
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     *  Interrupting a waiting timed get causes it to throw InterruptedException
     */
    public void testTimedGet_InterruptedException2() {
	final FutureTask ft = new FutureTask(new NoOpCallable());
	Thread t = new Thread(new Runnable() {
	 	public void run() {
		    try {
			ft.get(LONG_DELAY_MS,TimeUnit.MILLISECONDS);
			threadShouldThrow();
		    } catch(InterruptedException success){}
		    catch(Exception e){
                        threadUnexpectedException();
		    }
		}
	    });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch(Exception e){
            unexpectedException();
        }
    }

    /**
     * A timed out timed get throws TimeoutException
     */
    public void testGet_TimeoutException() {
	try {
            FutureTask ft = new FutureTask(new NoOpCallable());
	    ft.get(1,TimeUnit.MILLISECONDS);
	    shouldThrow();
	} catch(TimeoutException success){}
	catch(Exception success){
	    unexpectedException();
	}
    }

}
