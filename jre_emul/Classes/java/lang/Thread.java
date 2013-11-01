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

import java.util.HashMap;
import java.util.Map;

/*-[
#import "java/lang/IllegalThreadStateException.h"
#import "java/lang/InterruptedException.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/Runnable.h"
#import "objc-sync.h"
]-*/

/**
 * Simplified iOS version of java.lang.Thread, based on Apache Harmony source
 * (both luni-kernel and vmcore).  This class has a native NSThread delegate,
 * and most instance data is stored in the NSThread's associated dictionary.
 * This allows threads to be fetched without the need for a global map, and
 * avoids issues synchronizing iOS thread attributes with this wrapper.
 *
 * @author Tom Ball
 */
public class Thread implements Runnable {
  private static final int NANOS_PER_MILLI = 1000000;

  /**
   * The associated native NSThread instance.  Other instance data is stored
   * in the thread dictionary.
   */
  private Object nsThread;

  private boolean isDaemon;
  private boolean interrupted;
  private boolean running;

  /** the park state of the thread */
  private int parkState = ParkState.UNPARKED;

  /** The synchronization object responsible for this thread parking. */
  private Object parkBlocker = new Object();

  private static ThreadGroup systemThreadGroup;
  private static ThreadGroup mainThreadGroup;

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

  /**
   * Holds the default handler for uncaught exceptions, in case there is one.
   */
  private static UncaughtExceptionHandler defaultUncaughtHandler =
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

  // Thread dictionary keys
  private static final String KEY_PREFIX = "JreThread-";
  private static final String JAVA_THREAD = "JreThread-JavaThread";
  private static final String TARGET = "JreThread-TargetKey";
  private static final String THREADGROUP = "JreThread-GroupKey";
  private static final String THREAD_ID = "JreThread-IdKey";
  private static final String UNCAUGHT_HANDLER = "JreThread-UncaughtHandler";

  // Milliseconds between polls for testing thread completion.
  private static final int POLL_INTERVAL = 100;

  static {
    initializeThreadClass();
  }

  /**
   * Constructs a new Thread with no runnable object and a newly generated
   * name. The new Thread will belong to the same ThreadGroup as the Thread
   * calling this constructor.
   *
   * @see java.lang.ThreadGroup
   */
  public Thread() {
    create(null, null, THREAD, 0, true);
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
    create(null, runnable, THREAD, 0, true);
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
    create(null, runnable, threadName, 0, true);
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
    create(null, null, threadName, 0, true);
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
    create(group, runnable, THREAD, 0, true);
  }

  /**
   * Constructs a new Thread with a runnable object, the given name and
   * belonging to the ThreadGroup passed as parameter.
   *
   * @param group ThreadGroup to which the new Thread will belong
   * @param runnable a java.lang.Runnable whose method <code>run</code> will
   *        be executed by the new Thread
   * @param name Name for the Thread being created
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
    create(group, runnable, threadName, stack, true);
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
    create(group, runnable, threadName, 0, true);
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
    create(group, null, threadName, 0, true);
  }

  private Thread(ThreadGroup group, String threadName, boolean createThread) {
    create(group, null, threadName, 0, createThread);
  }

  /**
   * Shared native constructor code.
   */
  private native void create(ThreadGroup group, Runnable target, String name, long stack,
          boolean createThread) /*-[
    NSThread *currentThread = [NSThread currentThread];
    NSMutableDictionary *currentThreadData = [currentThread threadDictionary];
    JavaLangThreadGroup *threadGroup = nil;
    if (group != nil) {
      threadGroup = group;
    } else {
      threadGroup = [currentThreadData objectForKey:JavaLangThread_THREADGROUP_];
    }
    assert(threadGroup != nil);

    NSThread *thread;
    NSMutableDictionary *newThreadData;
    if (createThread) {
      if (!target) {
        // If there isn't a Runnable, then this should be a subclass of Thread
        // with run() overwritten (or it does nothing, like in Java).
        target = self;
      }
      thread = [[NSThread alloc] initWithTarget:self
                                       selector:@selector(run0WithId:)
                                         object:nil];
#if ! __has_feature(objc_arc)
      [thread autorelease];
#endif
      newThreadData = [thread threadDictionary];

      // Copy thread data from parent thread, except for data from this class.
      for (id key in currentThreadData) {
        if ([key isKindOfClass:[NSString class]] && ![key hasPrefix:JavaLangThread_KEY_PREFIX_]) {
          [newThreadData setObject:[currentThreadData objectForKey:key] forKey:key];
        }
      }
    } else {
      thread = currentThread;
      newThreadData = currentThreadData;
    }

    // Add data for this thread.
    [newThreadData setObject:threadGroup forKey:JavaLangThread_THREADGROUP_];
    if (target != nil) {
      [newThreadData setObject:target forKey:JavaLangThread_TARGET_];
    }
    if (stack != 0L) {
      [thread setStackSize:(NSUInteger) stack];
    }
    [thread setThreadPriority:[NSThread threadPriority]];
    NSNumber *threadId = [NSNumber numberWithLongLong:[JavaLangThread getNextThreadId]];
    [newThreadData setObject:threadId forKey:JavaLangThread_THREAD_ID_];

    if (!name) {
      JavaLangNullPointerException *npe = [[JavaLangNullPointerException alloc] init];
#if !__has_feature(objc_arc)
      [npe autorelease];
#endif
      @throw npe;
    }
    if ([name isEqual:JavaLangThread_THREAD_]) {
      name = [name stringByAppendingFormat:@"%@", threadId];
    }
    [thread setName:name];
    [self setNameWithNSString:name];

    int priority = [currentThread isMainThread] ? 5 : [currentThread threadPriority] * 10;
    [self setPriority0WithInt:priority];

    [threadGroup addWithJavaLangThread:self];
    nsThread_ = thread;
#if !__has_feature(objc_arc)
    [thread retain];
#endif
    [newThreadData setObject:self forKey:JavaLangThread_JAVA_THREAD_];
  ]-*/;

  /**
   * Create a Thread wrapper around the main native thread.
   */
  private static native void initializeThreadClass() /*-[
    NSThread *currentThread = [NSThread currentThread];
    [currentThread setName:@"main"];
    if (JavaLangThread_systemThreadGroup_ == nil) {
      JavaLangThread_systemThreadGroup_ = [[JavaLangThreadGroup alloc] init];
      JavaLangThread_mainThreadGroup_ =
          [[JavaLangThreadGroup alloc]
           initWithJavaLangThreadGroup:JavaLangThread_systemThreadGroup_
                          withNSString:@"main"];
#if ! __has_feature(objc_arc)
      [JavaLangThread_systemThreadGroup_ autorelease];
      [JavaLangThread_mainThreadGroup_ autorelease];
#endif
    }

    // Now there is a main threadgroup,
    (void) [[JavaLangThread alloc]
            initWithJavaLangThreadGroup:JavaLangThread_mainThreadGroup_
                           withNSString:@"main"
                            withBoolean:FALSE];
  ]-*/;

  public static native Thread currentThread() /*-[
    NSDictionary *threadData = [[NSThread currentThread] threadDictionary];
    JavaLangThread *thread = [threadData objectForKey:JavaLangThread_JAVA_THREAD_];
    if (!thread) {
      NSString *name = [[NSThread currentThread] name];
      thread =
          [[JavaLangThread alloc] initWithJavaLangThreadGroup:JavaLangThread_mainThreadGroup_
                                                 withNSString:name
                                                  withBoolean:FALSE];
#if !__has_feature(objc_arc)
      [thread autorelease];
#endif
    }
    return thread;
  ]-*/;

  public native void start() /*-[
    NSThread *nativeThread = (NSThread *) nsThread_;
    if ([nativeThread isExecuting]) {
      JavaLangIllegalThreadStateException *e =
          [[JavaLangIllegalThreadStateException alloc]
           initWithNSString:@"This thread was already started!"];
#if !__has_feature(objc_arc)
      [e autorelease];
#endif
      @throw e;
    }
    running_ = YES;
    [(NSThread *) nativeThread start];
  ]-*/;

  public native void run() /*-[
    NSDictionary *threadData = [(NSThread *) nsThread_ threadDictionary];
    id<JavaLangRunnable> target =
        (id<JavaLangRunnable>) [threadData objectForKey:JavaLangThread_TARGET_];
    if (target && target != self) {
      @autoreleasepool {  // also needed by ARC
        [target run];
      }
    }
  ]-*/;

  /*
   * Thread entry, called by NSThread initWithTarget:selector:object:.
   * The arg parameter isn't used, but NSThread requires a message that
   * has an optional one.
   */
  private void run0(Object arg) throws Throwable {
    try {
      run();
    } catch (Throwable t) {
      UncaughtExceptionHandler ueh = getUncaughtExceptionHandler();
      if (ueh != null) {
        ueh.uncaughtException(currentThread(), t);
      } else {
        throw(t);
      }
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

  public static int enumerate(Thread[] threads) {
    Thread thread = Thread.currentThread();
    return thread.getThreadGroup().enumerate(threads);
  }

  public native long getId() /*-[
    NSDictionary *threadData = [[NSThread currentThread] threadDictionary];
    NSNumber *threadId = [threadData objectForKey:JavaLangThread_THREAD_ID_];
    return [threadId longLongValue];
  ]-*/;

  public native String getName() /*-[
    return [(NSThread *) nsThread_ name];
  ]-*/;

  public native void setName(String name) /*-[
    if (!name) {
      JavaLangNullPointerException *npe = [[JavaLangNullPointerException alloc] init];
#if !__has_feature(objc_arc)
      [npe autorelease];
#endif
      @throw npe;
    }
    [(NSThread *) nsThread_ setName:name];
  ]-*/;

  public native int getPriority() /*-[
    double nativePriority = [(NSThread *) nsThread_ threadPriority];
    return (int) (nativePriority * 10);
  ]-*/;

  public void setPriority(int priority) {
    checkAccess();
    if (priority > MAX_PRIORITY || priority < MIN_PRIORITY) {
      throw new IllegalArgumentException("Wrong Thread priority value");
    }
    ThreadGroup threadGroup = getThreadGroup();
    priority = (priority > threadGroup.getMaxPriority()) ? threadGroup.getMaxPriority() : priority;
    setPriority0(priority);
  }

  private native void setPriority0(int priority) /*-[
    [(NSThread *) nsThread_ setThreadPriority:priority / 10.0];
  ]-*/;

  public native State getState() /*-[
    if ([(NSThread *) nsThread_ isCancelled] || [(NSThread *) nsThread_ isFinished]) {
      return [JavaLangThread_StateEnum TERMINATED];
    }
    if ([(NSThread *) nsThread_ isExecuting]) {
      return [JavaLangThread_StateEnum RUNNABLE];
    }
    return [JavaLangThread_StateEnum NEW];
  ]-*/;

  public native ThreadGroup getThreadGroup() /*-[
    NSDictionary *threadData = [(NSThread *) nsThread_ threadDictionary];
    return (JavaLangThreadGroup *) [threadData objectForKey:JavaLangThread_THREADGROUP_];
  ]-*/;

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
  public native void interrupt() /*-[
    interrupted__ = YES;
    [(NSThread *) nsThread_ cancel];
  ]-*/;

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
  public static boolean interrupted() {
      return currentThread().isInterrupted();
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
  public native boolean isInterrupted() /*-[
    BOOL result = interrupted__;
    interrupted__ = NO;
    return result;
  ]-*/;

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
      join(Long.MAX_VALUE);
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
      long millisToWait = millis + (nanos >= 500000 ? 1 : 0);
      join0(millisToWait, POLL_INTERVAL);
  }

  private final native void join0(long millis, int pollInterval)
          throws InterruptedException /*-[
    interrupted__ = NO;
    NSThread *thread = (NSThread *) nsThread_;
    while (millis > 0 && [thread isExecuting] && ![thread isCancelled]) {
      millis -= pollInterval;
      double timeInterval = pollInterval / 1000.0; // NSThread uses seconds.
      [NSThread sleepForTimeInterval:timeInterval];
    }
    if (interrupted__) {
      interrupted__ = NO;  // throw exception clears flag.
      @throw AUTORELEASE([[JavaLangInterruptedException alloc] init]);
    }
  ]-*/;

  public native boolean isAlive() /*-[
    NSThread *nativeThread = (NSThread *) nsThread_;
    BOOL alive = [nativeThread isExecuting] && ![nativeThread isCancelled];
    if (!alive && running_) {
      // Thread finished, clean up.
      running_ = NO;
      [[self getThreadGroup] removeWithJavaLangThread:self];
    }
    return alive;
  ]-*/;

  public void checkAccess() {
    // Access checks not implemented on iOS.
  }

  public static void sleep(long millis) throws InterruptedException {
     sleep(millis, 0);
  }

  public static native void sleep(long millis, int nanos) throws InterruptedException /*-[
    JavaLangThread *currentThread = [JavaLangThread currentThread];
    currentThread->interrupted__ = NO;
    long long ticks = (millis * 1000000L) + nanos;
    NSTimeInterval ti = ticks / 1000000000.0;
    [NSThread sleepForTimeInterval:ti];
    if (currentThread->interrupted__) {
      currentThread->interrupted__ = NO;  // throw exception clears flag.
      @throw AUTORELEASE([[JavaLangInterruptedException alloc] init]);
    }
  ]-*/;

  /**
   * Causes the calling Thread to yield execution time to another Thread that
   * is ready to run. The actual scheduling is implementation-dependent.
   */
  public static void yield() {
      currentThread().yield0();
  }

  private native void yield0() /*-[
    [NSThread sleepForTimeInterval:0];
  ]-*/;

  private static synchronized long getNextThreadId() {
    return ++threadOrdinalNum;
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
    return ClassLoader.getSystemClassLoader();
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
  public void unpark() {
    synchronized (parkBlocker) {
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
          parkBlocker.notifyAll();
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
  public void parkFor(long nanos) {
    synchronized (parkBlocker) {
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
            parkBlocker.wait(millis, (int) nanos);
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
  public void parkUntil(long time) {
    synchronized (parkBlocker) {
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
        parkFor(delayMillis * NANOS_PER_MILLI);
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
   * Returns the thread's uncaught exception handler. If not explicitly set,
   * then the ThreadGroup's handler is returned. If the thread is terminated,
   * then <code>null</code> is returned.
   *
   * @return an {@link UncaughtExceptionHandler} instance or {@code null}.
   */
  public native UncaughtExceptionHandler getUncaughtExceptionHandler() /*-[
    NSDictionary *threadData = [[NSThread currentThread] threadDictionary];
    id<JavaLangThread_UncaughtExceptionHandler> uncaughtHandler =
        [threadData objectForKey:JavaLangThread_UNCAUGHT_HANDLER_];
    if (uncaughtHandler) {
      return uncaughtHandler;
    }
    return [threadData objectForKey:JavaLangThread_THREADGROUP_]; // ThreadGroup is instance of UEH
  ]-*/;

  public native void setUncaughtExceptionHandler(UncaughtExceptionHandler handler) /*-[
    NSMutableDictionary *threadData = [[NSThread currentThread] threadDictionary];
    if (handler) {
      [threadData setObject:handler forKey:JavaLangThread_UNCAUGHT_HANDLER_];
    } else {
      [threadData removeObjectForKey:JavaLangThread_UNCAUGHT_HANDLER_];
    }
  ]-*/;

  private static class SystemUncaughtExceptionHandler implements UncaughtExceptionHandler {
    @Override
    public synchronized void uncaughtException(Thread t, Throwable e) {
      System.err.print("Exception in thread \"" + t.getName() + "\" ");
      e.printStackTrace(System.err);
    }
  }
}
