/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1997, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang.ref;

import com.google.j2objc.annotations.Weak;

/*-[
#import "IOSReference.h"
]-*/

/**
 * Abstract base class for reference objects. This class defines the operations common to all
 * reference objects. Because reference objects are implemented in close cooperation with the
 * garbage collector, this class may not be subclassed directly.
 *
 * @author Mark Reinhold
 * @since 1.2
 */
public abstract class Reference<T> {
  // BEGIN Android-changed: Reimplemented to accommodate a different GC and compiler.
  // ClassLinker knows about the fields of this class.
  // Backported refersTo() from OpenJDK 16.

  /**
   * Forces JNI path. If GC is not in progress (ie: not going through slow path), the referent can
   * be quickly returned through intrinsic without passing through JNI. This flag forces the JNI
   * path so that it can be tested and benchmarked.
   */
  // private static boolean disableIntrinsic = false; // J2ObjC unused.

  /**
   * Slow path flag for the reference processor. Used by the reference processor to determine
   * whether or not the referent can be immediately returned. Because the referent might get swept
   * during GC, the slow path, which passes through JNI, must be taken. After initialization, this
   * is only accessed by native code. It is not used with the concurrent copying collector. It is
   * enabled with mutators suspended, but disabled asynchronously.
   */
  // private static boolean slowPathEnabled = false; // J2ObjC unused.

  // Treated specially by GC. ART's ClassLinker::LinkFieldsHelper::LinkFields()
  // knows this is the alphabetically last non-static field.
  // We assume that Reference.get() and Reference.clear() are intended to be
  // callable concurrently, and thus referent accesses should be treated as
  // volatile everywhere.
  @Weak volatile T referent; /* Treated specially by GC */

  @Weak final ReferenceQueue<? super T> queue;

    /*
     * This field forms a singly-linked list of reference objects that have
     * been enqueued. The queueNext field is non-null if and only if this
     * reference has been enqueued. After this reference has been enqueued and
     * before it has been removed from its queue, the queueNext field points
     * to the next reference on the queue. The last reference on a queue
     * points to itself. Once this reference has been removed from the
     * reference queue, the queueNext field points to the
     * ReferenceQueue.sQueueNextUnenqueued sentinel reference object for the
     * rest of this reference's lifetime.
     * <p>
     * Access to the queueNext field is guarded by synchronization on a lock
     * internal to 'queue'.
     */
    Reference queueNext;

    /**
     * The pendingNext field is initially set by the GC. After the GC forms a
     * complete circularly linked list, the list is handed off to the
     * ReferenceQueueDaemon using the ReferenceQueue.class lock. The
     * ReferenceQueueDaemon can then read the pendingNext fields without
     * additional synchronization.
     */
    Reference<?> pendingNext;

  /* -- Referent accessor and setters -- */

  /*
   * Returns this reference object's referent. If this reference object has been cleared, either by
   * the program or by the garbage collector, then this method returns <code>null</code>.
   *
   * @return The object to which this reference refers, or <code>null</code> if this reference
   *     object has been cleared
   */
  public native T get() /*-[
      return [IOSReference getReferent:self];
    ]-*/;

  /**
   * Tests if the referent of this reference object is {@code obj}. Using a {@code null} {@code obj}
   * returns {@code true} if the reference object has been cleared. Prefer this to a comparison with
   * the result of {@code get}.
   *
   * @param obj the object to compare with this reference object's referent
   * @return {@code true} if {@code obj} is the referent of this reference object public final
   *     boolean refersTo(T obj) { return refersTo0(obj); }
   */

  /*
   *J2ObjC unused.
   * Implementation of refersTo().
    @FastNative
    private final native boolean refersTo0(Object o);
  */

  /**
   * Clears this reference object. Invoking this method will not cause this object to be enqueued.
   *
   * <p>This method is invoked only by Java code; when the garbage collector clears references it
   * does so directly, without invoking this method.
   */
  public native void clear() /*-[
        [IOSReference clearReferent:self];
    ]-*/;

  /* -- Queue operations -- */

  // Android-changed: deprecate since 9.
  // @Deprecated(since="16")
  /**
   * Tests if this reference object is in its associated queue, if any. This method returns {@code
   * true} only if all of the following conditions are met:
   *
   * <ul>
   *   <li>this reference object was registered with a queue when it was created; and
   *   <li>the garbage collector has added this reference object to the queue or {@link #enqueue()}
   *       is called; and
   *   <li>this reference object is not yet removed from the queue.
   * </ul>
   *
   * Otherwise, this method returns {@code false}. This method may return {@code false} if this
   * reference object has been cleared but not enqueued due to the race condition.
   *
   * @deprecated This method was never implemented to test if a reference object has been cleared
   *     and enqueued as it was previously specified since 1.2. This method could be misused due to
   *     the inherent race condition or without an associated {@code ReferenceQueue}. An application
   *     relying on this method to release critical resources could cause serious performance issue.
   *     An application should use {@link ReferenceQueue} to reliably determine what reference
   *     objects that have been enqueued or {@code refersTo(null)} to determine if this reference
   *     object has been cleared.
   * @return {@code true} if and only if this reference object is in its associated queue (if any).
   */
  @Deprecated // (since = "9")
  public boolean isEnqueued() {
        // Contrary to what the documentation says, this method returns false
        // after this reference object has been removed from its queue
        // (b/26647823). ReferenceQueue.isEnqueued preserves this historically
        // incorrect behavior.
        return queue != null && queue.isEnqueued(this);
    }

    /**
     * Adds this reference object to the queue with which it is registered,
     * if any.
     *
     * <p> This method is invoked only by Java code; when the garbage collector
     * enqueues references it does so directly, without invoking this method.
     *
     * @return   <code>true</code> if this reference object was successfully
     *           enqueued; <code>false</code> if it was already enqueued or if
     *           it was not registered with a queue when it was created
     */
    public boolean enqueue() {
       return queue != null && queue.enqueue(this);
    }


    /* -- Constructors -- */

    Reference(T referent) {
        this(referent, null);
    }

    Reference(T referent, ReferenceQueue<? super T> queue) {
        this.referent = referent;
        this.queue = queue;
        initReferent();
    }
  // END Android-changed: Reimplemented to accommodate a different GC and compiler.

  // BEGIN Android-added: reachabilityFence() from upstream OpenJDK9+181.
  // The actual implementation differs from OpenJDK9.
  /**
   * Ensures that the object referenced by the given reference remains <a
   * href="package-summary.html#reachability"><em>strongly reachable</em></a>, regardless of any
   * prior actions of the program that might otherwise cause the object to become unreachable; thus,
   * the referenced object is not reclaimable by garbage collection at least until after the
   * invocation of this method. Invocation of this method does not itself initiate garbage
   * collection or finalization.
   *
   * <p>This method establishes an ordering for <a
   * href="package-summary.html#reachability"><em>strong reachability</em></a> with respect to
   * garbage collection. It controls relations that are otherwise only implicit in a program -- the
   * reachability conditions triggering garbage collection. This method is designed for use in
   * uncommon situations of premature finalization where using {@code synchronized} blocks or
   * methods, or using other synchronization facilities are not possible or do not provide the
   * desired control. This method is applicable only when reclamation may have visible effects,
   * which is possible for objects with finalizers (See <a
   * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-12.html#jls-12.6">Section 12.6 17
   * of <cite>The Java&trade; Language Specification</cite></a>) that are implemented in ways that
   * rely on ordering control for correctness.
   *
   * @apiNote Finalization may occur whenever the virtual machine detects that no reference to an
   *     object will ever be stored in the heap: The garbage collector may reclaim an object even if
   *     the fields of that object are still in use, so long as the object has otherwise become
   *     unreachable. This may have surprising and undesirable effects in cases such as the
   *     following example in which the bookkeeping associated with a class is managed through array
   *     indices. Here, method {@code action} uses a {@code reachabilityFence} to ensure that the
   *     {@code Resource} object is not reclaimed before bookkeeping on an associated {@code
   *     ExternalResource} has been performed; in particular here, to ensure that the array slot
   *     holding the {@code ExternalResource} is not nulled out in method {@link Object#finalize},
   *     which may otherwise run concurrently.
   *     <pre>{@code
   * class Resource {
   *   private static ExternalResource[] externalResourceArray = ...
   *
   *   int myIndex;
   *   Resource(...) {
   *     myIndex = ...
   *     externalResourceArray[myIndex] = ...;
   *     ...
   *   }
   *   protected void finalize() {
   *     externalResourceArray[myIndex] = null;
   *     ...
   *   }
   *   public void action() {
   *     try {
   *       // ...
   *       int i = myIndex;
   *       Resource.update(externalResourceArray[i]);
   *     } finally {
   *       Reference.reachabilityFence(this);
   *     }
   *   }
   *   private static void update(ExternalResource ext) {
   *     ext.status = ...;
   *   }
   * }
   * }</pre>
   *     Here, the invocation of {@code reachabilityFence} is nonintuitively placed <em>after</em>
   *     the call to {@code update}, to ensure that the array slot is not nulled out by {@link
   *     Object#finalize} before the update, even if the call to {@code action} was the last use of
   *     this object. This might be the case if, for example a usage in a user program had the form
   *     {@code new Resource().action();} which retains no other reference to this {@code Resource}.
   *     While probably overkill here, {@code reachabilityFence} is placed in a {@code finally}
   *     block to ensure that it is invoked across all paths in the method. In a method with more
   *     complex control paths, you might need further precautions to ensure that {@code
   *     reachabilityFence} is encountered along all of them.
   *     <p>It is sometimes possible to better encapsulate use of {@code reachabilityFence}.
   *     Continuing the above example, if it were acceptable for the call to method {@code update}
   *     to proceed even if the finalizer had already executed (nulling out slot), then you could
   *     localize use of {@code reachabilityFence}:
   *     <pre>{@code
   * public void action2() {
   *   // ...
   *   Resource.update(getExternalResource());
   * }
   * private ExternalResource getExternalResource() {
   *   ExternalResource ext = externalResourceArray[myIndex];
   *   Reference.reachabilityFence(this);
   *   return ext;
   * }
   * }</pre>
   *     <p>Method {@code reachabilityFence} is not required in constructions that themselves ensure
   *     reachability. For example, because objects that are locked cannot, in general, be
   *     reclaimed, it would suffice if all accesses of the object, in all methods of class {@code
   *     Resource} (including {@code finalize}) were enclosed in {@code synchronized (this)} blocks.
   *     (Further, such blocks must not include infinite loops, or themselves be unreachable, which
   *     fall into the corner case exceptions to the "in general" disclaimer.) However, method
   *     {@code reachabilityFence} remains a better option in cases where this approach is not as
   *     efficient, desirable, or possible; for example because it would encounter deadlock.
   * @param ref the reference. If {@code null}, this method has no effect.
   * @since 9
   */
  // @DontInline
  /* J2ObjC unused.
  public static void reachabilityFence(Object ref) {
      // This code is usually replaced by much faster intrinsic implementations.
      // It will be executed for tests run with the access checks interpreter in
      // ART, e.g. with --verify-soft-fail.  Since this is a volatile store, it
      // cannot easily be moved up past prior accesses, even if this method is
      // inlined.
      SinkHolder.sink = ref;
      // Leaving SinkHolder set to ref is unpleasant, since it keeps ref live
      // until the next reachabilityFence call. This causes e.g. 036-finalizer
      // to fail. Clear it again in a way that's unlikely to be optimizable.
      // The fact that finalize_count is volatile makes it hard to move the test up.
      if (SinkHolder.finalize_count == 0) {
          SinkHolder.sink = null;
      }
  }
  */

  /* J2ObjC unused.
  private static class SinkHolder {
      static volatile Object sink;

      // Ensure that sink looks live to even a reasonably clever compiler.
      private static volatile int finalize_count = 0;

      private static Object sinkUser = new Object() {
          protected void finalize() {
              if (sink == null && finalize_count > 0) {
                  throw new AssertionError("Can't get here");
              }
              finalize_count++;
          }
      };
  }
  */
  // END Android-added: reachabilityFence() from upstream OpenJDK9+181.

  @Override
  protected void finalize() {
    clear();
  }

  private native void initReferent() /*-[
    [IOSReference initReferent:self];
  ]-*/;
}
