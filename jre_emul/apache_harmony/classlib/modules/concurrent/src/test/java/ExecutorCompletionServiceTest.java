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
import java.math.BigInteger;
import java.security.*;

public class ExecutorCompletionServiceTest extends JSR166TestCase{
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());  
    }
    public static Test suite() {
        return new TestSuite(ExecutorCompletionServiceTest.class);
    }


    /**
     * Creating a new ECS with null Executor throw NPE
     */ 
    public void testConstructorNPE() {
        try {
            ExecutorCompletionService ecs = new ExecutorCompletionService(null);
            shouldThrow();
        } catch (NullPointerException success) {
        }
    }

    /**
     * Creating a new ECS with null queue throw NPE
     */ 
    public void testConstructorNPE2() {
        try {
            ExecutorService e = Executors.newCachedThreadPool();
            ExecutorCompletionService ecs = new ExecutorCompletionService(e, null);
            shouldThrow();
        } catch (NullPointerException success) {
        }
    }

    /**
     * Submitting a null callable throws NPE
     */ 
    public void testSubmitNPE() {
        ExecutorService e = Executors.newCachedThreadPool();
        ExecutorCompletionService ecs = new ExecutorCompletionService(e);
        try {
            Callable c = null;
            ecs.submit(c);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * Submitting a null runnable throws NPE
     */ 
    public void testSubmitNPE2() {
        ExecutorService e = Executors.newCachedThreadPool();
        ExecutorCompletionService ecs = new ExecutorCompletionService(e);
        try {
            Runnable r = null;
            ecs.submit(r, Boolean.TRUE);
            shouldThrow();
        } catch (NullPointerException success) {
        } finally {
            joinPool(e);
        }
    }

    /**
     * A taken submitted task is completed
     */ 
    public void testTake() {
        ExecutorService e = Executors.newCachedThreadPool();
        ExecutorCompletionService ecs = new ExecutorCompletionService(e);
        try {
            Callable c = new StringTask();
            ecs.submit(c);
            Future f = ecs.take();
            assertTrue(f.isDone());
        } catch (Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * Take returns the same future object returned by submit
     */ 
    public void testTake2() {
        ExecutorService e = Executors.newCachedThreadPool();
        ExecutorCompletionService ecs = new ExecutorCompletionService(e);
        try {
            Callable c = new StringTask();
            Future f1 = ecs.submit(c);
            Future f2 = ecs.take();
            assertSame(f1, f2);
        } catch (Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * If poll returns non-null, the returned task is completed
     */ 
    public void testPoll1() {
        ExecutorService e = Executors.newCachedThreadPool();
        ExecutorCompletionService ecs = new ExecutorCompletionService(e);
        try {
            assertNull(ecs.poll());
            Callable c = new StringTask();
            ecs.submit(c);
            Thread.sleep(SHORT_DELAY_MS);
            for (;;) {
                Future f = ecs.poll();
                if (f != null) {
                    assertTrue(f.isDone());
                    break;
                }
            }
        } catch (Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }

    /**
     * If timed poll returns non-null, the returned task is completed
     */ 
    public void testPoll2() {
        ExecutorService e = Executors.newCachedThreadPool();
        ExecutorCompletionService ecs = new ExecutorCompletionService(e);
        try {
            assertNull(ecs.poll());
            Callable c = new StringTask();
            ecs.submit(c);
            Future f = ecs.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
            if (f != null) 
                assertTrue(f.isDone());
        } catch (Exception ex) {
            unexpectedException();
        } finally {
            joinPool(e);
        }
    }
}
