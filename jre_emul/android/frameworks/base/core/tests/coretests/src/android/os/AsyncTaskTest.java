package android.os;

import junit.framework.TestCase;
import android.test.suitebuilder.annotation.SmallTest;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Things missing here:
 *
 * onPostExecute, onProgressUpdate and onCancelled are only called in the iOS Simulator and on a
 * real device :( So currently this test fails using junit.
 */

public class AsyncTaskTest extends TestCase {

    private final Thread mainThread = Thread.currentThread();

    boolean preExecuteCalled = false;
    boolean postExecuteCalled = false;
    boolean progressUpdateCalled = false;

    @SmallTest
    public void testAsyncTask() throws Throwable {
        final Waiter waiter = new Waiter();

        class SimpleAsyncTask extends AsyncTask<String, Integer, String> {
            @Override
            protected void onPreExecute() {
                waiter.assertEquals(mainThread, Thread.currentThread());

                preExecuteCalled = true;
            }

            @Override
            protected void onPostExecute(String result) {
                waiter.assertEquals(mainThread, Thread.currentThread());
                waiter.assertEquals("hello world", result);

                postExecuteCalled = true;

                waiter.resume();
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                waiter.assertEquals(mainThread, Thread.currentThread());
                waiter.assertEquals(10, values[0]);

                progressUpdateCalled = true;
            }

            @Override
            protected void onCancelled(String s) {
                // This callback must not be called here
                waiter.assertTrue(false);
            }

            @Override
            protected void onCancelled() {
                // This callback must not be called here
                waiter.assertTrue(false);
            }

            @Override
            protected String doInBackground(String... strings) {
                // We must live in a background thread here
                waiter.assertFalse(mainThread.equals(Thread.currentThread()));

                // Check the input strings
                waiter.assertEquals("hello", strings[0]);
                waiter.assertEquals("world", strings[1]);

                publishProgress(10);

                // Return a result
                return "hello world";
            }
        }

        SimpleAsyncTask task = new SimpleAsyncTask();
        task.execute("hello", "world");

        waiter.await(500);

        // Make sure the result is correct. This implicitly makes sure that doInBackground
        // was called.
        assertEquals("hello world", task.get());

        // Make sure the other functions were all called
        assertTrue(preExecuteCalled);
        assertTrue(postExecuteCalled);
        assertTrue(progressUpdateCalled);
    }
}

/**
 * Waits on a test, carrying out assertions, until being resumed.
 *
 * @author Jonathan Halterman
 */
class Waiter {
    private static final String TIMEOUT_MESSAGE = "Test timed out while waiting for an expected result";
    private final Thread mainThread;
    private AtomicInteger remainingResumes = new AtomicInteger(0);
    private volatile Throwable failure;

    /**
     * Creates a new Waiter.
     */
    public Waiter() {
        mainThread = Thread.currentThread();
    }

    public void assertEquals(Object expected, Object actual) {
        if (expected == null && actual == null)
            return;
        if (expected != null && expected.equals(actual))
            return;
        fail(format(expected, actual));
    }

    /**
     * Asserts that the {@code condition} is false.
     */
    public void assertFalse(boolean condition) {
        if (condition)
            fail("expected false");
    }

    /**
     * Asserts that the {@code object} is not null.
     */
    public void assertNotNull(Object object) {
        if (object == null)
            fail("expected not null");
    }

    /**
     * Asserts that the {@code object} is null.
     */
    public void assertNull(Object object) {
        if (object != null)
            fail(format("null", object));
    }

    /**
     * Asserts that the {@code condition} is true.
     */
    public void assertTrue(boolean condition) {
        if (!condition)
            fail("expected true");
    }

    /**
     * Waits until {@link #resume()} is called the expected number of times, or the test is failed.
     *
     * @throws IllegalStateException if called from outside the main test thread
     * @throws TimeoutException if the operation times out while waiting for a result
     * @throws Throwable if any assertion fails
     */
    public void await() throws Throwable {
        await(0, 1);
    }

    /**
     * Waits until the {@code waitDuration} has elapsed, {@link #resume()} is called the expected
     * number of times, or the test is failed.
     *
     * @param waitDuration Duration to wait in milliseconds
     * @throws IllegalStateException if called from outside the main test thread
     * @throws TimeoutException if the operation times out while waiting for a result
     * @throws Throwable if any assertion fails
     */
    public void await(long waitDuration) throws Throwable {
        await(waitDuration, 1);
    }

    /**
     * Waits until the {@code waitDuration} has elapsed, {@link #resume()} is called
     * {@code expectedResumes} times, or the test is failed.
     *
     * @param waitDuration Duration to wait in milliseconds
     * @param expectedResumes Number of times {@link #resume()} is expected to be called before the
     *          awaiting thread is resumed
     * @throws IllegalStateException if called from outside the main test thread
     * @throws TimeoutException if the operation times out while waiting for a result
     * @throws Throwable if any assertion fails
     */
    public void await(long waitDuration, int expectedResumes) throws Throwable {
        if (Thread.currentThread() != mainThread)
            throw new IllegalStateException("Must be called from within the main test thread");

        remainingResumes.compareAndSet(0, expectedResumes);
        synchronized (this) {
            // Loop to avoid spurious wakeups
            while (true) {
                try {
                    if (waitDuration < 0)
                        wait();
                    else
                        wait(waitDuration);
                    throw new TimeoutException(TIMEOUT_MESSAGE);
                } catch (InterruptedException e) {
                    if (failure != null) {
                        Throwable f = failure;
                        failure = null;
                        throw f;
                    }
                    break;
                }
            }
        }
    }

    /**
     * Instructs the waiter to expect {@link #resume()} to be called.
     */
    public void expectResume() {
        remainingResumes.addAndGet(1);
    }

    /**
     * Instructs the waiter to expect the {@code resumeNumber} resumes to occur.
     */
    public void expectResumes(int resumeNumber) {
        remainingResumes.addAndGet(resumeNumber);
    }

    /**
     * Fails the current test.
     */
    public void fail() {
        fail(new AssertionError());
    }

    /**
     * Fails the current test for the given {@code reason}.
     */
    public void fail(String reason) {
        fail(new AssertionError(reason));
    }

    /**
     * Fails the current test with the given {@code reason}, sets the number of expected resumes to 0,
     * and throws the {@code reason} in the current thread and the main test thread.
     */
    public void fail(Throwable reason) {
        remainingResumes.set(0);

        AssertionError ae = null;
        if (reason instanceof AssertionError)
            ae = (AssertionError) reason;
        else {
            ae = new AssertionError();
            ae.initCause(reason);
        }

        if (Thread.currentThread() == mainThread)
            throw ae;

        failure = reason;
        mainThread.interrupt();
        throw ae;
    }

    /**
     * Gets the remaining number of expected resumes that must occur before any waiting/sleeping
     * threads are unblocked.
     */
    public int getExpectedResumes() {
        return remainingResumes.get();
    }

    /**
     * Resumes the waiter when the expected number of {@link #resume()} calls have occurred.
     *
     * @throws IllegalStateException if the waiter is not expecting resume to be called
     */
    public void resume() {
        resume(mainThread);
    }

    /**
     * Resumes the waiter if {@code thread} is not the mainThread or the expected number of resumes
     * have occurred.
     *
     * <p>
     * Note: This method is likely not very useful to call directly since a concurrent run of a test
     * case resulting in the need to resume from a separate thread would yield no correlation between
     * the initiating thread and the thread where the resume call takes place.
     *
     * @param thread Thread to resume
     * @throws IllegalStateException if the waiter is not expecting resume to be called
     */
    public void resume(Thread thread) {
        if (thread != mainThread)
            thread.interrupt();
        else {
            int expectedResumes = remainingResumes.decrementAndGet();
            if (expectedResumes < 0)
                throw new IllegalStateException("The waiter is not expecting resume to be called");
            if (expectedResumes == 0)
                thread.interrupt();
        }
    }

    /**
     * Sleeps until the {@code sleepDuration} has elapsed, {@link #resume()} is called the expected
     * number of times, or the test is failed.
     *
     * @param sleepDuration Duration to sleep in milliseconds
     * @throws IllegalStateException if called from outside the main test thread
     * @throws TimeoutException if the operation times out while waiting for a result
     * @throws Throwable if any assertion fails
     */
    public void sleep(long sleepDuration) throws Throwable {
        sleep(sleepDuration, 1);
    }

    /**
     * Sleeps until the {@code sleepDuration} has elapsed, {@link #resume()} is called
     * {@code expectedResumes} times, or the test is failed.
     *
     * @param sleepDuration Duration to sleep in milliseconds
     * @param expectedResumes Number of times {@link #resume()} is expected to be called before the
     *          sleeping thread is resumed
     * @throws IllegalStateException if called from outside the main test thread
     * @throws TimeoutException if the operation times out while waiting for a result
     * @throws Throwable if any assertion fails
     */
    public void sleep(long sleepDuration, int expectedResumes) throws Throwable {
        if (Thread.currentThread() != mainThread)
            throw new IllegalStateException("Must be called from within the main test thread");

        remainingResumes.compareAndSet(0, expectedResumes);
        try {
            Thread.sleep(sleepDuration);
            throw new TimeoutException(TIMEOUT_MESSAGE);
        } catch (InterruptedException ignored) {
        } finally {
            if (failure != null) {
                Throwable f = failure;
                failure = null;
                throw f;
            }
        }
    }

    private String format(Object expected, Object actual) {
        return "expected:<" + expected + "> but was:<" + actual + ">";
    }
}
