/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

import com.google.j2objc.annotations.Weak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.nio.ch.Interruptible;

/*-[
#import "java/lang/AssertionError.h"
#import "java_lang_Thread.h"
#import "objc-sync.h"
#import <pthread.h>
]-*/

/*-[
@interface NativeThread : NSObject {
 @public
  pthread_t t;
}
@end
@implementation NativeThread
@end
]-*/

/**
 * Simplified iOS version of java.lang.Thread, based on Apache Harmony source
 * (both luni-kernel and vmcore). This class uses pthread for thread creation
 * and maintains a pthread handle for using other pthread functionality.
 * pthread's thread local mechanism (pthread_setspecific) is used to associate
 * this wrapper object with the current thread.
 *
 * @author Tom Ball, Keith Stanger
 */
public class Thread implements Runnable {
  private static final int NANOS_PER_MILLI = 1000000;

  /** Android source declares this as the native VMThread class. */
  private final Object nativeThread;
  private Runnable target;
  private final long threadId;
  private String name;
  private final long stackSize;
  private int priority = NORM_PRIORITY;
  private volatile UncaughtExceptionHandler uncaughtExceptionHandler;
  private boolean isDaemon;
  boolean interrupted;
  private ClassLoader contextClassLoader;
  ThreadLocal.ThreadLocalMap threadLocals = null;
  ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;

  static final int STATE_NEW = 0;
  static final int STATE_RUNNABLE = 1;
  static final int STATE_BLOCKED = 2;
  static final int STATE_WAITING = 3;
  static final int STATE_TIMED_WAITING = 4;
  static final int STATE_TERMINATED = 5;
  // Accessing a volatile int is cheaper than a volatile object.
  volatile int state = STATE_NEW;

  /** The object the thread is waiting on (normally null). */
  Object blocker;

  /** The object in which this thread is blocked in an interruptible I/O operation, if any. */
  private Interruptible IOBlocker;
  private final Object IOBlockerLock = new Object();

  @Weak
  private ThreadGroup threadGroup;

  /** the park state of the thread */
  private int parkState = ParkState.UNPARKED;

  /** The synchronization object responsible for this thread parking. */
  private Object parkBlocker;

  /** Callbacks to run on interruption. */
  private final List<Runnable> interruptActions = new ArrayList<Runnable>();

  /**
   * Counter used to generate thread's ID
   */
  private static long threadOrdinalNum = 1;

  /**
   * A representation of a thread's state. A given thread may only be in one
   * state at a time.
   */
  public enum State {
    /**
     * The thread has been created, but has never been started.
     */
    NEW,
    /**
     * The thread may be run.
     */
    RUNNABLE,
    /**
     * The thread is blocked and waiting for a lock.
     */
    BLOCKED,
    /**
     * The thread is waiting.
     */
    WAITING,
    /**
     * The thread is waiting for a specified amount of time.
     */
    TIMED_WAITING,
    /**
     * The thread has been terminated.
     */
    TERMINATED
  }

  /** Park states */
  private static class ParkState {
      /** park state indicating unparked */
      private static final int UNPARKED = 1;

      /** park state indicating preemptively unparked */
      private static final int PREEMPTIVELY_UNPARKED = 2;

      /** park state indicating parked */
      private static final int PARKED = 3;
  }

  public interface UncaughtExceptionHandler {

    void uncaughtException(Thread t, Throwable e);
  }

  // Fields accessed reflectively by ThreadLocalRandom.
  long threadLocalRandomSeed;
  int threadLocalRandomProbe;
  int threadLocalRandomSecondarySeed;

  /**
   * Holds the default handler for uncaught exceptions, in case there is one.
   */
  private static volatile UncaughtExceptionHandler defaultUncaughtHandler =
      new SystemUncaughtExceptionHandler();

  /**
   * <p>
   * The maximum priority value allowed for a thread.
   * </p>
   */
  public final static int MAX_PRIORITY = 10;

  /**
   * <p>
   * The minimum priority value allowed for a thread.
   * </p>
   */
  public final static int MIN_PRIORITY = 1;

  /**
   * <p>
   * The normal (default) priority value assigned to threads.
   * </p>
   */
  public final static int NORM_PRIORITY = 5;

  /**
   * used to generate a default thread name
   */
  private static final String THREAD = "Thread-";

  // Milliseconds between polls for testing thread completion.
  private static final int POLL_INTERVAL = 20;

  static {
    initializeThreadClass();
  }

  private static native Object newNativeThread() /*-[
    return [[[NativeThread alloc] init] autorelease];
  ]-*/;

  /**
   * Constructs a new Thread with no runnable object and a newly generated
   * name. The new Thread will belong to the same ThreadGroup as the Thread
   * calling this constructor.
   *
   * @see java.lang.ThreadGroup
   */
  public Thread() {
    this(null, null, THREAD, 0, null);
  }

  /**
   * Constructs a new Thread with a runnable object and a newly generated
   * name. The new Thread will belong to the same ThreadGroup as the Thread
   * calling this constructor.
   *
   * @param runnable a java.lang.Runnable whose method <code>run</code> will
   *        be executed by the new Thread
   * @see java.lang.ThreadGroup
   * @see java.lang.Runnable
   */
  public Thread(Runnable runnable) {
    this(null, runnable, THREAD, 0, null);
  }

  /**
   * Constructs a new Thread with a runnable object and name provided. The new
   * Thread will belong to the same ThreadGroup as the Thread calling this
   * constructor.
   *
   * @param runnable a java.lang.Runnable whose method <code>run</code> will
   *        be executed by the new Thread
   * @param threadName Name for the Thread being created
   * @see java.lang.ThreadGroup
   * @see java.lang.Runnable
   */
  public Thread(Runnable runnable, String threadName) {
    this(null, runnable, threadName, 0, null);
  }

  /**
   * Constructs a new Thread with no runnable object and the name provided.
   * The new Thread will belong to the same ThreadGroup as the Thread calling
   * this constructor.
   *
   * @param threadName Name for the Thread being created
   * @see java.lang.ThreadGroup
   * @see java.lang.Runnable
   */
  public Thread(String threadName) {
    this(null, null, threadName, 0, null);
  }

  /**
   * Constructs a new Thread with a runnable object and a newly generated
   * name. The new Thread will belong to the ThreadGroup passed as parameter.
   *
   * @param group ThreadGroup to which the new Thread will belong
   * @param runnable a java.lang.Runnable whose method <code>run</code> will
   *        be executed by the new Thread
   * @throws SecurityException if <code>group.checkAccess()</code> fails
   *         with a SecurityException
   * @throws IllegalThreadStateException if <code>group.destroy()</code> has
   *         already been done
   * @see java.lang.ThreadGroup
   * @see java.lang.Runnable
   * @see java.lang.SecurityException
   * @see java.lang.SecurityManager
   */
  public Thread(ThreadGroup group, Runnable runnable) {
    this(group, runnable, THREAD, 0, null);
  }

  /**
   * Constructs a new Thread with a runnable object, the given name and
   * belonging to the ThreadGroup passed as parameter.
   *
   * @param group ThreadGroup to which the new Thread will belong
   * @param runnable a java.lang.Runnable whose method <code>run</code> will
   *        be executed by the new Thread
   * @param threadName Name for the Thread being created
   * @param stack Platform dependent stack size
   * @throws SecurityException if <code>group.checkAccess()</code> fails
   *         with a SecurityException
   * @throws IllegalThreadStateException if <code>group.destroy()</code> has
   *         already been done
   * @see java.lang.ThreadGroup
   * @see java.lang.Runnable
   * @see java.lang.SecurityException
   * @see java.lang.SecurityManager
   */
  public Thread(ThreadGroup group, Runnable runnable, String threadName, long stack) {
    this(group, runnable, threadName, stack, null);
  }

  /**
   * Constructs a new Thread with a runnable object, the given name and
   * belonging to the ThreadGroup passed as parameter.
   *
   * @param group ThreadGroup to which the new Thread will belong
   * @param runnable a java.lang.Runnable whose method <code>run</code> will
   *        be executed by the new Thread
   * @param threadName Name for the Thread being created
   * @throws SecurityException if <code>group.checkAccess()</code> fails
   *         with a SecurityException
   * @throws IllegalThreadStateException if <code>group.destroy()</code> has
   *         already been done
   * @see java.lang.ThreadGroup
   * @see java.lang.Runnable
   * @see java.lang.SecurityException
   * @see java.lang.SecurityManager
   */
  public Thread(ThreadGroup group, Runnable runnable, String threadName) {
    this(group, runnable, threadName, 0, null);
  }

  /**
   * Constructs a new Thread with no runnable object, the given name and
   * belonging to the ThreadGroup passed as parameter.
   *
   * @param group ThreadGroup to which the new Thread will belong
   * @param threadName Name for the Thread being created
   * @throws SecurityException if <code>group.checkAccess()</code> fails
   *         with a SecurityException
   * @throws IllegalThreadStateException if <code>group.destroy()</code> has
   *         already been done
   * @see java.lang.ThreadGroup
   * @see java.lang.SecurityException
   * @see java.lang.SecurityManager
   */
  public Thread(ThreadGroup group, String threadName) {
    this(group, null, threadName, 0, null);
  }

  private Thread(
      ThreadGroup group, Runnable runnable, String name, long stack, Object nativeThread) {
    this.target = runnable;
    this.threadId = getNextThreadId();
    if (name.equals(THREAD)) {
      name += threadId;
    }
    this.name = name;
    this.stackSize = stack;
    if (nativeThread == null) {
      // Thread is not yet started.
      Thread currentThread = currentThread();
      nativeThread = newNativeThread();
      this.priority = currentThread.getPriority();
      if (group == null) {
        group = currentThread.getThreadGroup();
      }
    } else {
      // Thread is already running.
      state = STATE_RUNNABLE;
      group.add(this);
    }
    this.threadGroup = group;
    this.nativeThread = nativeThread;
  }

  /*-[
  void *start_routine(void *arg) {
    JavaLangThread *thread = (JavaLangThread *)arg;
    pthread_setspecific(java_thread_key, thread);
    @autoreleasepool {
      @try {
        [thread run];
      } @catch (JavaLangThrowable *t) {
        JavaLangThread_rethrowWithJavaLangThrowable_(thread, t);
      } @catch (id error) {
        JavaLangThread_rethrowWithJavaLangThrowable_(
            thread, create_JavaLangThrowable_initWithNSString_(
                [NSString stringWithFormat:@"Unknown error: %@", [error description]]));
      }
      return NULL;
    }
  }
  ]-*/

  private static Thread createMainThread(Object nativeThread) {
    return new Thread(ThreadGroup.mainThreadGroup, null, "main", 0, nativeThread);
  }

  /**
   * Create a Thread wrapper around the main native thread.
   */
  private static native void initializeThreadClass() /*-[
    initJavaThreadKeyOnce();
    NativeThread *nt = [[[NativeThread alloc] init] autorelease];
    nt->t = pthread_self();
    JavaLangThread *mainThread = JavaLangThread_createMainThreadWithId_(nt);
    pthread_setspecific(java_thread_key, [mainThread retain]);
  ]-*/;

  private static Thread createCurrentThread(Object nativeThread) {
    return new Thread(ThreadGroup.mainThreadGroup, null, THREAD, 0, nativeThread);
  }

  public static native Thread currentThread() /*-[
    JavaLangThread *thread = pthread_getspecific(java_thread_key);
    if (thread) {
      return thread;
    }
    NativeThread *nt = [[[NativeThread alloc] init] autorelease];
    nt->t = pthread_self();
    thread = JavaLangThread_createCurrentThreadWithId_(nt);
    pthread_setspecific(java_thread_key, [thread retain]);
    return thread;
  ]-*/;

  public synchronized void start() {
    if (state != STATE_NEW) {
      throw new IllegalThreadStateException("This thread was already started!");
    }
    threadGroup.add(this);
    state = STATE_RUNNABLE;
    start0();
    if (priority != NORM_PRIORITY) {
      nativeSetPriority(priority);
    }
  }

  private native void start0() /*-[
    NativeThread *nt = (NativeThread *)self->nativeThread_;
    pthread_attr_t attr;
    pthread_attr_init(&attr);
    size_t stack = (size_t)self->stackSize_;
    if (stack >= PTHREAD_STACK_MIN) {
      pthread_attr_setstacksize(&attr, stack);
    }
    pthread_create(&nt->t, &attr, &start_routine, [self retain]);
  ]-*/;

  void exit() {
    state = STATE_TERMINATED;
    if (threadGroup != null) {
      threadGroup.threadTerminated(this);
      threadGroup = null;
    }
    target = null;
    uncaughtExceptionHandler = null;
    interruptActions.clear();
  }

  @Override
  public void run() {
    if (target != null) {
      target.run();
    }
  }

  private void rethrow(Throwable t) throws Throwable {
    UncaughtExceptionHandler ueh = getUncaughtExceptionHandler();
    if (ueh != null) {
      ueh.uncaughtException(this, t);
    } else {
      throw t;
    }
  }

  public static int activeCount() {
      return currentThread().getThreadGroup().activeCount();
  }

  public boolean isDaemon() {
    return isDaemon;
  }

  public void setDaemon(boolean isDaemon) {
    this.isDaemon = isDaemon;
  }

  /**
   * Prints to the standard error stream a text representation of the current
   * stack for this Thread.
   *
   * @see Throwable#printStackTrace()
   */
  public static void dumpStack() {
    new Throwable("stack dump").printStackTrace();
  }

  public static int enumerate(Thread[] threads) {
    Thread thread = Thread.currentThread();
    return thread.getThreadGroup().enumerate(threads);
  }

  public long getId() {
    return threadId;
  }

  public final String getName() {
    return name;
  }

  public final void setName(String name) {
    checkAccess();
    if (name == null) {
      throw new NullPointerException("name == null");
    }
    this.name = name;
  }

  public final int getPriority() {
    return priority;
  }

  public final void setPriority(int newPriority) {
    ThreadGroup g;
    checkAccess();
    if (newPriority > MAX_PRIORITY || newPriority < MIN_PRIORITY) {
      throw new IllegalArgumentException();
    }
    if ((g = getThreadGroup()) != null) {
      if (newPriority > g.getMaxPriority()) {
        newPriority = g.getMaxPriority();
      }
      synchronized (this) {
        this.priority = newPriority;
        if (isAlive()) {
          nativeSetPriority(newPriority);
        }
      }
    }
  }

  private native void nativeSetPriority(int priority) /*-[
    struct sched_param param;
    param.sched_priority = (priority * 64 / JavaLangThread_MAX_PRIORITY) - 1;
    NativeThread *nt = (NativeThread *)self->nativeThread_;
    pthread_setschedparam(nt->t, SCHED_OTHER, &param);
  ]-*/;

  public State getState() {
    switch (state) {
      case STATE_NEW:
        return State.NEW;
      case STATE_RUNNABLE:
        return State.RUNNABLE;
      case STATE_BLOCKED:
        return State.BLOCKED;
      case STATE_WAITING:
        return State.WAITING;
      case STATE_TIMED_WAITING:
        return State.TIMED_WAITING;
      case STATE_TERMINATED:
        return State.TERMINATED;
    }

    // Unreachable.
    return null;
  }

  public ThreadGroup getThreadGroup() {
    return state == STATE_TERMINATED ? null : threadGroup;
  }

  public StackTraceElement[] getStackTrace() {
    // Get the stack trace for a new exception, stripping the exception's
    // and preamble's (runtime startup) frames.
    StackTraceElement[] exceptionTrace = new Throwable().getStackTrace();
    int firstElement = 0;
    int lastElement = exceptionTrace.length;
    for (int i = 0; i < exceptionTrace.length; i++) {
      String methodName = exceptionTrace[i].getMethodName();
      if (methodName.contains("getStackTrace")) {
        firstElement = i;
        continue;
      }
      if (methodName.contains("mainWithNSStringArray:")) {
        lastElement = i;
        break;
      }
    }
    int nFrames = lastElement - firstElement + 1;
    if (nFrames < 0) {
      // Something failed, return the whole stack trace.
      return exceptionTrace;
    }
    if (firstElement + nFrames > exceptionTrace.length) {
      nFrames = exceptionTrace.length - firstElement;
    }
    StackTraceElement[] result = new StackTraceElement[nFrames];
    System.arraycopy(exceptionTrace, firstElement, result, 0, nFrames);
    return result;
  }

  @Deprecated
  public int countStackFrames() {
      return getStackTrace().length;
  }

  /** Set the IOBlocker field; invoked from java.nio code. */
  public void blockedOn(Interruptible b) {
    synchronized (IOBlockerLock) {
      IOBlocker = b;
    }
  }

  /**
   * Posts an interrupt request to this {@code Thread}. Unless the caller is
   * the {@link #currentThread()}, the method {@code checkAccess()} is called
   * for the installed {@code SecurityManager}, if any. This may result in a
   * {@code SecurityException} being thrown. The further behavior depends on
   * the state of this {@code Thread}:
   * <ul>
   * <li>
   * {@code Thread}s blocked in one of {@code Object}'s {@code wait()} methods
   * or one of {@code Thread}'s {@code join()} or {@code sleep()} methods will
   * be woken up, their interrupt status will be cleared, and they receive an
   * {@link InterruptedException}.
   * <li>
   * {@code Thread}s blocked in an I/O operation of an
   * {@link java.nio.channels.InterruptibleChannel} will have their interrupt
   * status set and receive an
   * {@link java.nio.channels.ClosedByInterruptException}. Also, the channel
   * will be closed.
   * <li>
   * {@code Thread}s blocked in a {@link java.nio.channels.Selector} will have
   * their interrupt status set and return immediately. They don't receive an
   * exception in this case.
   * <ul>
   *
   * @throws SecurityException
   *             if <code>checkAccess()</code> fails with a SecurityException
   * @see java.lang.SecurityException
   * @see java.lang.SecurityManager
   * @see Thread#interrupted
   * @see Thread#isInterrupted
   */
  public void interrupt() {
    synchronized(nativeThread) {
      synchronized (interruptActions) {
        for (int i = interruptActions.size() - 1; i >= 0; i--) {
          interruptActions.get(i).run();
        }
      }

      synchronized (IOBlockerLock) {
        Interruptible b = IOBlocker;
        if (b != null) {
          b.interrupt(this);
        }
      }

      if (interrupted) {
        return;  // No further action needed.
      }
      interrupted = true;
      if (blocker != null) {
        synchronized(blocker) {
          blocker.notify();
        }
      }
    }
  }

  /**
   * Returns a <code>boolean</code> indicating whether the current Thread (
   * <code>currentThread()</code>) has a pending interrupt request (<code>
   * true</code>) or not (<code>false</code>). It also has the side-effect of
   * clearing the flag.
   *
   * @return a <code>boolean</code> indicating the interrupt status
   * @see Thread#currentThread
   * @see Thread#interrupt
   * @see Thread#isInterrupted
   */
  public static native boolean interrupted() /*-[
    JavaLangThread *currentThread = JavaLangThread_currentThread();
    @synchronized(currentThread->nativeThread_) {
      jboolean result = currentThread->interrupted_;
      currentThread->interrupted_ = false;
      return result;
    }
  ]-*/;

  /**
   * Returns a <code>boolean</code> indicating whether the receiver has a
   * pending interrupt request (<code>true</code>) or not (
   * <code>false</code>)
   *
   * @return a <code>boolean</code> indicating the interrupt status
   * @see Thread#interrupt
   * @see Thread#interrupted
   */
  public boolean isInterrupted() {
    return interrupted;
  }

  /**
   * Blocks the current Thread (<code>Thread.currentThread()</code>) until
   * the receiver finishes its execution and dies.
   *
   * @throws InterruptedException if <code>interrupt()</code> was called for
   *         the receiver while it was in the <code>join()</code> call
   * @see Object#notifyAll
   * @see java.lang.ThreadDeath
   */
  public final void join() throws InterruptedException {
      if (!isAlive()) {
          return;
      }

      Object lock = currentThread().nativeThread;
      synchronized (lock) {
          while (isAlive()) {
              lock.wait(POLL_INTERVAL);
          }
      }
  }

  /**
   * Blocks the current Thread (<code>Thread.currentThread()</code>) until
   * the receiver finishes its execution and dies or the specified timeout
   * expires, whatever happens first.
   *
   * @param millis The maximum time to wait (in milliseconds).
   * @throws InterruptedException if <code>interrupt()</code> was called for
   *         the receiver while it was in the <code>join()</code> call
   * @see Object#notifyAll
   * @see java.lang.ThreadDeath
   */
  public final void join(long millis) throws InterruptedException {
      join(millis, 0);
  }

  /**
   * Blocks the current Thread (<code>Thread.currentThread()</code>) until
   * the receiver finishes its execution and dies or the specified timeout
   * expires, whatever happens first.
   *
   * @param millis The maximum time to wait (in milliseconds).
   * @param nanos Extra nanosecond precision
   * @throws InterruptedException if <code>interrupt()</code> was called for
   *         the receiver while it was in the <code>join()</code> call
   * @see Object#notifyAll
   * @see java.lang.ThreadDeath
   */
  public final void join(long millis, int nanos) throws InterruptedException {
      if (millis < 0 || nanos < 0 || nanos >= NANOS_PER_MILLI) {
          throw new IllegalArgumentException("bad timeout: millis=" + millis + ",nanos=" + nanos);
      }

      // avoid overflow: if total > 292,277 years, just wait forever
      boolean overflow = millis >= (Long.MAX_VALUE - nanos) / NANOS_PER_MILLI;
      boolean forever = (millis | nanos) == 0;
      if (forever | overflow) {
          join();
          return;
      }

      if (!isAlive()) {
          return;
      }

      Object lock = currentThread().nativeThread;
      synchronized (lock) {
          if (!isAlive()) {
              return;
          }

          // guaranteed not to overflow
          long nanosToWait = millis * NANOS_PER_MILLI + nanos;

          // wait until this thread completes or the timeout has elapsed
          long start = System.nanoTime();
          while (true) {
              if (millis > POLL_INTERVAL) {
                lock.wait(POLL_INTERVAL);
              } else {
                lock.wait(millis, nanos);
              }
              if (!isAlive()) {
                  break;
              }
              long nanosElapsed = System.nanoTime() - start;
              long nanosRemaining = nanosToWait - nanosElapsed;
              if (nanosRemaining <= 0) {
                  break;
              }
              millis = nanosRemaining / NANOS_PER_MILLI;
              nanos = (int) (nanosRemaining - millis * NANOS_PER_MILLI);
          }
      }
  }

  public final boolean isAlive() {
    int s = state;
    return s != STATE_NEW && s != STATE_TERMINATED;
  }

  public void checkAccess() {
    // Access checks not implemented on iOS.
  }

  public static void sleep(long millis) throws InterruptedException {
     sleep(millis, 0);
  }

  public static void sleep(long millis, int nanos) throws InterruptedException {
      if (millis < 0) {
          throw new IllegalArgumentException("millis < 0: " + millis);
      }
      if (nanos < 0) {
          throw new IllegalArgumentException("nanos < 0: " + nanos);
      }
      if (nanos > 999999) {
          throw new IllegalArgumentException("nanos > 999999: " + nanos);
      }

      // The JLS 3rd edition, section 17.9 says: "...sleep for zero
      // time...need not have observable effects."
      if (millis == 0 && nanos == 0) {
          // ...but we still have to handle being interrupted.
          if (Thread.interrupted()) {
            throw new InterruptedException();
          }
          return;
      }

      Object lock = currentThread().nativeThread;
      synchronized(lock) {
          lock.wait(millis, nanos);
      }
  }

  /**
   * Causes the calling Thread to yield execution time to another Thread that
   * is ready to run. The actual scheduling is implementation-dependent.
   */
  public static native void yield() /*-[
    pthread_yield_np();
  ]-*/;

  private static synchronized long getNextThreadId() {
    return threadOrdinalNum++;
  }

  /**
   * Indicates whether the current Thread has a monitor lock on the specified
   * object.
   *
   * @param object the object to test for the monitor lock
   * @return true if the current thread has a monitor lock on the specified
   *         object; false otherwise
   */
  public static native boolean holdsLock(Object object) /*-[
    return j2objc_sync_holds_lock(object);
  ]-*/;

  /**
   * Returns the context ClassLoader for this Thread.
   *
   * @return ClassLoader The context ClassLoader
   * @see java.lang.ClassLoader
   */
  public ClassLoader getContextClassLoader() {
    return contextClassLoader != null ? contextClassLoader : ClassLoader.getSystemClassLoader();
  }

  public void setContextClassLoader(ClassLoader cl) {
    contextClassLoader = cl;
  }

  public String toString() {
    ThreadGroup group = getThreadGroup();
    if (group != null) {
      return "Thread[" + getName() + "," + getPriority() + "," + group.getName() + "]";
    }
    return "Thread[" + getName() + "," + getPriority() + ",]";
  }

  /**
   * Unparks this thread. This unblocks the thread it if it was
   * previously parked, or indicates that the thread is "preemptively
   * unparked" if it wasn't already parked. The latter means that the
   * next time the thread is told to park, it will merely clear its
   * latent park bit and carry on without blocking.
   *
   * <p>See {@link java.util.concurrent.locks.LockSupport} for more
   * in-depth information of the behavior of this method.</p>
   *
   * @hide for Unsafe
   */
  public final void unpark$() {
    Object vmt = nativeThread;

    synchronized (vmt) {
      switch (parkState) {
        case ParkState.PREEMPTIVELY_UNPARKED: {
          /*
           * Nothing to do in this case: By definition, a
           * preemptively unparked thread is to remain in
           * the preemptively unparked state if it is told
           * to unpark.
           */
          break;
        }
        case ParkState.UNPARKED: {
          parkState = ParkState.PREEMPTIVELY_UNPARKED;
          break;
        }
        default /*parked*/: {
          parkState = ParkState.UNPARKED;
          vmt.notifyAll();
          break;
        }
      }
    }
  }

  /**
   * Parks the current thread for a particular number of nanoseconds, or
   * indefinitely. If not indefinitely, this method unparks the thread
   * after the given number of nanoseconds if no other thread unparks it
   * first. If the thread has been "preemptively unparked," this method
   * cancels that unparking and returns immediately. This method may
   * also return spuriously (that is, without the thread being told to
   * unpark and without the indicated amount of time elapsing).
   *
   * <p>See {@link java.util.concurrent.locks.LockSupport} for more
   * in-depth information of the behavior of this method.</p>
   *
   * <p>This method must only be called when <code>this</code> is the current
   * thread.
   *
   * @param nanos number of nanoseconds to park for or <code>0</code>
   * to park indefinitely
   * @throws IllegalArgumentException thrown if <code>nanos &lt; 0</code>
   *
   * @hide for Unsafe
   */
  public final void parkFor$(long nanos) {
    Object vmt = nativeThread;

    synchronized (vmt) {
      switch (parkState) {
        case ParkState.PREEMPTIVELY_UNPARKED: {
          parkState = ParkState.UNPARKED;
          break;
        }
        case ParkState.UNPARKED: {
          long millis = nanos / NANOS_PER_MILLI;
          nanos %= NANOS_PER_MILLI;

          parkState = ParkState.PARKED;
          try {
            vmt.wait(millis, (int) nanos);
          } catch (InterruptedException ex) {
            interrupt();
          } finally {
            /*
             * Note: If parkState manages to become
             * PREEMPTIVELY_UNPARKED before hitting this
             * code, it should left in that state.
             */
            if (parkState == ParkState.PARKED) {
              parkState = ParkState.UNPARKED;
            }
          }
          break;
        }
        default /*parked*/: {
          throw new AssertionError("shouldn't happen: attempt to repark");
        }
      }
    }
  }

  /**
   * Parks the current thread until the specified system time. This
   * method attempts to unpark the current thread immediately after
   * <code>System.currentTimeMillis()</code> reaches the specified
   * value, if no other thread unparks it first. If the thread has
   * been "preemptively unparked," this method cancels that
   * unparking and returns immediately. This method may also return
   * spuriously (that is, without the thread being told to unpark
   * and without the indicated amount of time elapsing).
   *
   * <p>See {@link java.util.concurrent.locks.LockSupport} for more
   * in-depth information of the behavior of this method.</p>
   *
   * <p>This method must only be called when <code>this</code> is the
   * current thread.
   *
   * @param time the time after which the thread should be unparked,
   * in absolute milliseconds-since-the-epoch
   *
   * @hide for Unsafe
   */
  public final void parkUntil$(long time) {
    Object vmt = nativeThread;

    synchronized (vmt) {
      /*
       * Note: This conflates the two time bases of "wall clock"
       * time and "monotonic uptime" time. However, given that
       * the underlying system can only wait on monotonic time,
       * it is unclear if there is any way to avoid the
       * conflation. The downside here is that if, having
       * calculated the delay, the wall clock gets moved ahead,
       * this method may not return until well after the wall
       * clock has reached the originally designated time. The
       * reverse problem (the wall clock being turned back)
       * isn't a big deal, since this method is allowed to
       * spuriously return for any reason, and this situation
       * can safely be construed as just such a spurious return.
       */
      long delayMillis = time - System.currentTimeMillis();

      if (delayMillis <= 0) {
        parkState = ParkState.UNPARKED;
      } else {
        parkFor$(delayMillis * NANOS_PER_MILLI);
      }
    }
  }

  /**
   * Returns the default exception handler that's executed when uncaught
   * exception terminates a thread.
   *
   * @return an {@link UncaughtExceptionHandler} or <code>null</code> if
   *         none exists.
   */
  public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler() {
      return defaultUncaughtHandler;
  }

  /**
   * Sets the default uncaught exception handler. This handler is invoked in
   * case any Thread dies due to an unhandled exception.
   *
   * @param handler
   *            The handler to set or null.
   */
  public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
      Thread.defaultUncaughtHandler = handler;
  }

  /**
   * Returns the handler invoked when this thread abruptly terminates
   * due to an uncaught exception. If this thread has not had an
   * uncaught exception handler explicitly set then this thread's
   * <tt>ThreadGroup</tt> object is returned, unless this thread
   * has terminated, in which case <tt>null</tt> is returned.
   * @since 1.5
   */
  public UncaughtExceptionHandler getUncaughtExceptionHandler() {
    UncaughtExceptionHandler h = uncaughtExceptionHandler;
    return h != null ? h : threadGroup;
  }

  /**
   * Set the handler invoked when this thread abruptly terminates
   * due to an uncaught exception.
   * <p>A thread can take full control of how it responds to uncaught
   * exceptions by having its uncaught exception handler explicitly set.
   * If no such handler is set then the thread's <tt>ThreadGroup</tt>
   * object acts as its handler.
   * @param eh the object to use as this thread's uncaught exception
   * handler. If <tt>null</tt> then this thread has no explicit handler.
   * @throws  SecurityException  if the current thread is not allowed to
   *          modify this thread.
   * @see #setDefaultUncaughtExceptionHandler
   * @see ThreadGroup#uncaughtException
   * @since 1.5
   */
  public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
    checkAccess();
    uncaughtExceptionHandler = eh;
  }

  private static class SystemUncaughtExceptionHandler implements UncaughtExceptionHandler {
    @Override
    public synchronized void uncaughtException(Thread t, Throwable e) {
      // Log the exception using the root logger (""), so it isn't accidentally filtered.
      Logger.getLogger("").log(
          Level.SEVERE, "Uncaught exception in thread \"" + t.getName() + "\"", e);
    }
  }

  /**
   * Adds a runnable to be invoked upon interruption. If this thread has
   * already been interrupted, the runnable will be invoked immediately. The
   * action should be idempotent as it may be invoked multiple times for a
   * single interruption.
   *
   * <p>Each call to this method must be matched with a corresponding call to
   * {@link #popInterruptAction$}.
   *
   * @hide used by NIO
   */
  public final void pushInterruptAction$(Runnable interruptAction) {
      synchronized (interruptActions) {
          interruptActions.add(interruptAction);
      }

      if (interruptAction != null && isInterrupted()) {
          interruptAction.run();
      }
  }

  /**
   * Removes {@code interruptAction} so it is not invoked upon interruption.
   *
   * @param interruptAction the pushed action, used to check that the call
   *     stack is correctly nested.
   *
   * @hide used by NIO
   */
  public final void popInterruptAction$(Runnable interruptAction) {
      synchronized (interruptActions) {
          Runnable removed = interruptActions.remove(interruptActions.size() - 1);
          if (interruptAction != removed) {
              throw new IllegalArgumentException(
                      "Expected " + interruptAction + " but was " + removed);
          }
      }
  }

  /**
   * Returns a map of stack traces for all live threads.
   */
  // TODO(dweis): Can we update this to return something useful?
  public static Map<Thread,StackTraceElement[]> getAllStackTraces() {
    return Collections.<Thread, StackTraceElement[]>emptyMap();
  }

  @Deprecated
  public final void stop() {
    stop(new ThreadDeath());
  }

  @Deprecated
  public final void stop(Throwable obj) {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  public void destroy() {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  public final void suspend() {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  public final void resume() {
    throw new UnsupportedOperationException();
  }
}
