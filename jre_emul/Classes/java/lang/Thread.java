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

/*-{
#import "java/lang/IllegalThreadStateException.h"
#import "java/lang/InterruptedException.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/Runnable.h"
}-*/

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

  /**
   * The associated native NSThread instance.  Other instance data is stored
   * in the thread dictionary.
   */
  private Object nsThread;

  private boolean isDaemon;

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

  public interface UncaughtExceptionHandler {

    void uncaughtException(Thread t, Throwable e);

  }

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
	  boolean createThread) /*-{
    NSThread *currentThread = [NSThread currentThread];
    NSMutableDictionary *currentThreadData = [currentThread threadDictionary];
    JavaLangThreadGroup *threadGroup = nil;
    if (group != nil) {
      threadGroup = group;
    } else {
      threadGroup = [currentThreadData objectForKey:JavaLangThread_THREADGROUP_];
    }

    NSThread *thread;
    NSMutableDictionary *newThreadData;
    if (createThread) {
      if (!target) {
        // If there isn't a Runnable, then this should be a subclass of Thread
        // with run() overwritten (or it does nothing, like in Java).
        target = self;
      }
      thread = [[NSThread alloc] initWithTarget:target
                                       selector:@selector(run)
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

    [group addWithJavaLangThread:self];
    nsThread_ = thread;
#if !__has_feature(objc_arc)
    [thread retain];
#endif
    [newThreadData setObject:self forKey:JavaLangThread_JAVA_THREAD_];
  }-*/;
  
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
    [[JavaLangThread alloc] initWithJavaLangThreadGroup:JavaLangThread_mainThreadGroup_
                                           withNSString:@"main"
                                               withBOOL:FALSE];
  ]-*/;

  public static native Thread currentThread() /*-{
    NSDictionary *threadData = [[NSThread currentThread] threadDictionary];
    return (JavaLangThread *) [threadData objectForKey:JavaLangThread_JAVA_THREAD_];
  }-*/;

  public native void start() /*-{
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
    [[self getThreadGroup] addWithJavaLangThread:self];
    [(NSThread *) nativeThread start];
  }-*/;

  public native void run() /*-{
    NSDictionary *threadData = [(NSThread *) nsThread_ threadDictionary];
    id<JavaLangRunnable> target =
        (id<JavaLangRunnable>) [threadData objectForKey:JavaLangThread_TARGET_];
    if (target && target != self) {
      @autoreleasepool {  // also needed by ARC
        [target run];
      }
    }
  }-*/;

  public boolean isDaemon() {
    return isDaemon;
  }

  public void setDaemon(boolean isDaemon) {
    this.isDaemon = isDaemon;
  }

  public native long getId() /*-{
    NSDictionary *threadData = [[NSThread currentThread] threadDictionary];
    NSNumber *threadId = [threadData objectForKey:JavaLangThread_THREAD_ID_];
    return [threadId longLongValue];
  }-*/;

  public native String getName() /*-{
    return [(NSThread *) nsThread_ name];
  }-*/;

  public native void setName(String name) /*-{
    if (!name) {
      JavaLangNullPointerException *npe = [[JavaLangNullPointerException alloc] init];
#if !__has_feature(objc_arc)
      [npe autorelease];
#endif
      @throw npe;
    }
    [(NSThread *) nsThread_ setName:name];
  }-*/;

  public native int getPriority() /*-{
    double nativePriority = [(NSThread *) nsThread_ threadPriority];
    return (int) (nativePriority * 10);
  }-*/;

  public void setPriority(int priority) {
    checkAccess();
    if (priority > MAX_PRIORITY || priority < MIN_PRIORITY) {
      throw new IllegalArgumentException("Wrong Thread priority value");
    }
    ThreadGroup threadGroup = getThreadGroup();
    priority = (priority > threadGroup.getMaxPriority()) ? threadGroup.getMaxPriority() : priority;
    setPriority0(priority);
  }

  private native void setPriority0(int priority) /*-{
    [(NSThread *) nsThread_ setThreadPriority:priority / 10.0];
  }-*/;

  public native State getState() /*-{
    if ([(NSThread *) nsThread_ isCancelled] || [(NSThread *) nsThread_ isFinished]) {
      return [JavaLangThread_StateEnum TERMINATED];
    }
    if ([(NSThread *) nsThread_ isExecuting]) {
      return [JavaLangThread_StateEnum RUNNABLE];
    }
    return [JavaLangThread_StateEnum NEW];
  }-*/;

  public native ThreadGroup getThreadGroup() /*-{
    NSDictionary *threadData = [(NSThread *) nsThread_ threadDictionary];
    return (JavaLangThreadGroup *) [threadData objectForKey:JavaLangThread_THREADGROUP_];
  }-*/;

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
  public native boolean isInterrupted() /*-{
    return [(NSThread *) nsThread_ isCancelled];
  }-*/;

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
      NSThread *thread = (NSThread *) nsThread_;
      while (millis > 0 && [thread isExecuting]) {
        millis -= pollInterval;
        double timeInterval = pollInterval / 1000.0; // NSThread uses seconds.
        [NSThread sleepForTimeInterval:timeInterval];
      }
      if ([thread isCancelled]) {
        JavaLangInterruptedException *npe = [[JavaLangInterruptedException alloc] init];
#if !__has_feature(objc_arc)
        [npe autorelease];
#endif
        @throw npe;
      }
    ]-*/;

  public native boolean isAlive() /*-{
    return [(NSThread *) nsThread_ isExecuting];
  }-*/;

  public void checkAccess() {
    // Access checks not implemented on iOS.
  }

  public static void sleep(long millis) throws InterruptedException {
     sleep(millis, 0);
  }

  public static native void sleep(long millis, int nanos) throws InterruptedException /*-{
    long long ticks = (millis * 1000000L) + nanos;
    NSTimeInterval ti = ticks / 1000000000.0;
    [NSThread sleepForTimeInterval:ti];
  }-*/;

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
  
  public String toString() {
    ThreadGroup group = getThreadGroup();
    if (group != null) {
      return "Thread[" + getName() + "," + getPriority() + "," + group.getName() + "]";
    }
    return "Thread[" + getName() + "," + getPriority() + ",]";
  }
}
