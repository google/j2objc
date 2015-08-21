/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.lang;

/*
 * Stub implementation of java.lang.Object.  This class is not translated,
 * but is only included in jre_emul.jar so that jar can be used for
 * verifying that the JRE emulation library supports all JRE references
 * in specified sources.  The easiest way to do this is to run:
 *
 * javac -bootclasspath <path>/jre_emul.jar -extdirs '' <sources>
 */

/**
 * The root class of the Java class hierarchy. All non-primitive types
 * (including arrays) inherit either directly or indirectly from this class.
 *
 * <a name="writing_equals"><h4>Writing a correct {@code equals} method</h4></a>
 * <p>Follow this style to write a canonical {@code equals} method:
 * <pre>
 *   // Use @Override to avoid accidental overloading.
 *   &#x0040;Override public boolean equals(Object o) {
 *     // Return true if the objects are identical.
 *     // (This is just an optimization, not required for correctness.)
 *     if (this == o) {
 *       return true;
 *     }
 *
 *     // Return false if the other object has the wrong type.
 *     // This type may be an interface depending on the interface's specification.
 *     if (!(o instanceof MyType)) {
 *       return false;
 *     }
 *
 *     // Cast to the appropriate type.
 *     // This will succeed because of the instanceof, and lets us access private fields.
 *     MyType lhs = (MyType) o;
 *
 *     // Check each field. Primitive fields, reference fields, and nullable reference
 *     // fields are all treated differently.
 *     return primitiveField == lhs.primitiveField &amp;&amp;
 *             referenceField.equals(lhs.referenceField) &amp;&amp;
 *             (nullableField == null ? lhs.nullableField == null
 *                                    : nullableField.equals(lhs.nullableField));
 *   }
 * </pre>
 * <p>If you override {@code equals}, you should also override {@code hashCode}: equal
 * instances must have equal hash codes.
 *
 * <p>See <i>Effective Java</i> item 8 for much more detail and clarification.
 *
 * <a name="writing_hashCode"><h4>Writing a correct {@code hashCode} method</h4></a>
 * <p>Follow this style to write a canonical {@code hashCode} method:
 * <pre>
 *   &#x0040;Override public int hashCode() {
 *     // Start with a non-zero constant.
 *     int result = 17;
 *
 *     // Include a hash for each field.
 *     result = 31 * result + (booleanField ? 1 : 0);
 *
 *     result = 31 * result + byteField;
 *     result = 31 * result + charField;
 *     result = 31 * result + shortField;
 *     result = 31 * result + intField;
 *
 *     result = 31 * result + (int) (longField ^ (longField >>> 32));
 *
 *     result = 31 * result + Float.floatToIntBits(floatField);
 *
 *     long doubleFieldBits = Double.doubleToLongBits(doubleField);
 *     result = 31 * result + (int) (doubleFieldBits ^ (doubleFieldBits >>> 32));
 *
 *     result = 31 * result + Arrays.hashCode(arrayField);
 *
 *     result = 31 * result + referenceField.hashCode();
 *     result = 31 * result +
 *         (nullableReferenceField == null ? 0
 *                                         : nullableReferenceField.hashCode());
 *
 *     return result;
 *   }
 * </pre>
 *
 * <p>If you don't intend your type to be used as a hash key, don't simply rely on the default
 * {@code hashCode} implementation, because that silently and non-obviously breaks any future
 * code that does use your type as a hash key. You should throw instead:
 * <pre>
 *   &#x0040;Override public int hashCode() {
 *     throw new UnsupportedOperationException();
 *   }
 * </pre>
 *
 * <p>See <i>Effective Java</i> item 9 for much more detail and clarification.
 *
 * <a name="writing_toString"><h4>Writing a useful {@code toString} method</h4></a>
 * <p>For debugging convenience, it's common to override {@code toString} in this style:
 * <pre>
 *   &#x0040;Override public String toString() {
 *     return getClass().getName() + "[" +
 *         "primitiveField=" + primitiveField + ", " +
 *         "referenceField=" + referenceField + ", " +
 *         "arrayField=" + Arrays.toString(arrayField) + "]";
 *   }
 * </pre>
 * <p>The set of fields to include is generally the same as those that would be tested
 * in your {@code equals} implementation.
 * <p>See <i>Effective Java</i> item 10 for much more detail and clarification.
 */
public class Object {
  
  /**
   * Constructs a new instance of {@code Object}.
   */
  public Object() {}

  /**
   * Returns the unique instance of {@link Class} that represents this
   * object's class. Note that {@code getClass()} is a special case in that it
   * actually returns {@code Class<? extends Foo>} where {@code Foo} is the
   * erasure of the type of the expression {@code getClass()} was called upon.
   * <p>
   * As an example, the following code actually compiles, although one might
   * think it shouldn't:
   * <p>
   * <pre>{@code
   *   List<Integer> l = new ArrayList<Integer>();
   *   Class<? extends List> c = l.getClass();}</pre>
   *
   * @return this object's {@code Class} instance.
   */
  public final Class<?> getClass() {
    return null;
  }

  /**
   * Returns an integer hash code for this object. By contract, any two
   * objects for which {@link #equals} returns {@code true} must return
   * the same hash code value. This means that subclasses of {@code Object}
   * usually override both methods or neither method.
   *
   * <p>Note that hash values must not change over time unless information used in equals
   * comparisons also changes.
   *
   * <p>See <a href="{@docRoot}reference/java/lang/Object.html#writing_hashCode">Writing a correct
   * {@code hashCode} method</a>
   * if you intend implementing your own {@code hashCode} method.
   *
   * @return this object's hash code.
   * @see #equals
   */
  public int hashCode() {
    return 0;
  }

  /**
   * Compares this instance with the specified object and indicates if they
   * are equal. In order to be equal, {@code o} must represent the same object
   * as this instance using a class-specific comparison. The general contract
   * is that this comparison should be reflexive, symmetric, and transitive.
   * Also, no object reference other than null is equal to null.
   *
   * <p>The default implementation returns {@code true} only if {@code this ==
   * o}. See <a href="{@docRoot}reference/java/lang/Object.html#writing_equals">Writing a correct
   * {@code equals} method</a>
   * if you intend implementing your own {@code equals} method.
   *
   * <p>The general contract for the {@code equals} and {@link
   * #hashCode()} methods is that if {@code equals} returns {@code true} for
   * any two objects, then {@code hashCode()} must return the same value for
   * these objects. This means that subclasses of {@code Object} usually
   * override either both methods or neither of them.
   *
   * @param o
   *            the object to compare this instance with.
   * @return {@code true} if the specified object is equal to this {@code
   *         Object}; {@code false} otherwise.
   * @see #hashCode
   */
  public boolean equals(Object obj) {
    return false;
  }

  /**
   * Creates and returns a copy of this {@code Object}. The default
   * implementation returns a so-called "shallow" copy: It creates a new
   * instance of the same class and then copies the field values (including
   * object references) from this instance to the new instance. A "deep" copy,
   * in contrast, would also recursively clone nested objects. A subclass that
   * needs to implement this kind of cloning should call {@code super.clone()}
   * to create the new instance and then create deep copies of the nested,
   * mutable objects.
   *
   * @return a copy of this object.
   * @throws CloneNotSupportedException
   *             if this object's class does not implement the {@code
   *             Cloneable} interface.
   */
  protected Object clone() throws CloneNotSupportedException {
    return null;
  }

  /**
   * Returns a string containing a concise, human-readable description of this
   * object. Subclasses are encouraged to override this method and provide an
   * implementation that takes into account the object's type and data. The
   * default implementation is equivalent to the following expression:
   * <pre>
   *   getClass().getName() + '@' + Integer.toHexString(hashCode())</pre>
   * <p>See <a href="{@docRoot}reference/java/lang/Object.html#writing_toString">Writing a useful
   * {@code toString} method</a>
   * if you intend implementing your own {@code toString} method.
   *
   * @return a printable representation of this object.
   */
  public String toString() {
    return "";
  }

  /**
   * Invoked when the garbage collector has detected that this instance is no longer reachable.
   * The default implementation does nothing, but this method can be overridden to free resources.
   *
   * <p>Note that objects that override {@code finalize} are significantly more expensive than
   * objects that don't. Finalizers may be run a long time after the object is no longer
   * reachable, depending on memory pressure, so it's a bad idea to rely on them for cleanup.
   * Note also that finalizers are run on a single VM-wide finalizer thread,
   * so doing blocking work in a finalizer is a bad idea. A finalizer is usually only necessary
   * for a class that has a native peer and needs to call a native method to destroy that peer.
   * Even then, it's better to provide an explicit {@code close} method (and implement
   * {@link java.io.Closeable}), and insist that callers manually dispose of instances. This
   * works well for something like files, but less well for something like a {@code BigInteger}
   * where typical calling code would have to deal with lots of temporaries. Unfortunately,
   * code that creates lots of temporaries is the worst kind of code from the point of view of
   * the single finalizer thread.
   *
   * <p>If you <i>must</i> use finalizers, consider at least providing your own
   * {@link java.lang.ref.ReferenceQueue} and having your own thread process that queue.
   *
   * <p>Unlike constructors, finalizers are not automatically chained. You are responsible for
   * calling {@code super.finalize()} yourself.
   *
   * <p>Uncaught exceptions thrown by finalizers are ignored and do not terminate the finalizer
   * thread.
   *
   * See <i>Effective Java</i> Item 7, "Avoid finalizers" for more.
   */
  protected void finalize() throws Throwable {}

  /**
   * Causes a thread which is waiting on this object's monitor (by means of
   * calling one of the {@code wait()} methods) to be woken up. If more than
   * one thread is waiting, one of them is chosen at the discretion of the
   * VM. The chosen thread will not run immediately. The thread
   * that called {@code notify()} has to release the object's monitor first.
   * Also, the chosen thread still has to compete against other threads that
   * try to synchronize on the same object.
   *
   * <p>This method can only be invoked by a thread which owns this object's
   * monitor. A thread becomes owner of an object's monitor
   * <ul>
   * <li>by executing a synchronized method of that object;</li>
   * <li>by executing the body of a {@code synchronized} statement that
   * synchronizes on the object;</li>
   * <li>by executing a synchronized static method if the object is of type
   * {@code Class}.</li>
   * </ul>
   *
   * @see #notifyAll
   * @see #wait()
   * @see #wait(long)
   * @see #wait(long,int)
   * @see java.lang.Thread
   */
  public final void notify() {}

  /**
   * Causes all threads which are waiting on this object's monitor (by means
   * of calling one of the {@code wait()} methods) to be woken up. The threads
   * will not run immediately. The thread that called {@code notify()} has to
   * release the object's monitor first. Also, the threads still have to
   * compete against other threads that try to synchronize on the same object.
   *
   * <p>This method can only be invoked by a thread which owns this object's
   * monitor. A thread becomes owner of an object's monitor
   * <ul>
   * <li>by executing a synchronized method of that object;</li>
   * <li>by executing the body of a {@code synchronized} statement that
   * synchronizes on the object;</li>
   * <li>by executing a synchronized static method if the object is of type
   * {@code Class}.</li>
   * </ul>
   *
   * @throws IllegalMonitorStateException
   *             if the thread calling this method is not the owner of this
   *             object's monitor.
   * @see #notify
   * @see #wait()
   * @see #wait(long)
   * @see #wait(long,int)
   * @see java.lang.Thread
   */
  public final void notifyAll() {}

  /**
   * Causes the calling thread to wait until another thread calls the {@code
   * notify()} or {@code notifyAll()} method of this object or until the
   * specified timeout expires. This method can only be invoked by a thread
   * which owns this object's monitor; see {@link #notify()} on how a thread
   * can become the owner of a monitor.
   *
   * <p>A waiting thread can be sent {@code interrupt()} to cause it to
   * prematurely stop waiting, so {@code wait} should be called in a loop to
   * check that the condition that has been waited for has been met before
   * continuing.
   *
   * <p>While the thread waits, it gives up ownership of this object's
   * monitor. When it is notified (or interrupted), it re-acquires the monitor
   * before it starts running.
   *
   * <p>A timeout of zero means the calling thread should wait forever unless interrupted or
   * notified.
   *
   * @param millis
   *            the maximum time to wait in milliseconds.
   * @throws IllegalArgumentException
   *             if {@code millis < 0}.
   * @throws IllegalMonitorStateException
   *             if the thread calling this method is not the owner of this
   *             object's monitor.
   * @throws InterruptedException if the current thread has been interrupted.
   *             The interrupted status of the current thread will be cleared before the exception
   *             is thrown.
   * @see #notify
   * @see #notifyAll
   * @see #wait()
   * @see #wait(long,int)
   * @see java.lang.Thread
   */
  public final void wait(long timeout) throws InterruptedException {}

  /**
   * Causes the calling thread to wait until another thread calls the {@code
   * notify()} or {@code notifyAll()} method of this object or until the
   * specified timeout expires. This method can only be invoked by a thread
   * that owns this object's monitor; see {@link #notify()} on how a thread
   * can become the owner of a monitor.
   *
   * <p>A waiting thread can be sent {@code interrupt()} to cause it to
   * prematurely stop waiting, so {@code wait} should be called in a loop to
   * check that the condition that has been waited for has been met before
   * continuing.
   *
   * <p>While the thread waits, it gives up ownership of this object's
   * monitor. When it is notified (or interrupted), it re-acquires the monitor
   * before it starts running.
   *
   * <p>A timeout of zero means the calling thread should wait forever unless interrupted or
   * notified.
   *
   * @param millis
   *            the maximum time to wait in milliseconds.
   * @param nanos
   *            the fraction of a millisecond to wait, specified in
   *            nanoseconds.
   * @throws IllegalArgumentException
   *             if {@code millis < 0}, {@code nanos < 0} or {@code nanos >
   *             999999}.
   * @throws IllegalMonitorStateException
   *             if the thread calling this method is not the owner of this
   *             object's monitor.
   * @throws InterruptedException if the current thread has been interrupted.
   *             The interrupted status of the current thread will be cleared before the exception
   *             is thrown.
   * @see #notify
   * @see #notifyAll
   * @see #wait()
   * @see #wait(long,int)
   * @see java.lang.Thread
   */
  public final void wait(long timeout, int nanos) throws InterruptedException {}

  /**
   * Causes the calling thread to wait until another thread calls the {@code
   * notify()} or {@code notifyAll()} method of this object. This method can
   * only be invoked by a thread which owns this object's monitor; see
   * {@link #notify()} on how a thread can become the owner of a monitor.
   *
   * <p>A waiting thread can be sent {@code interrupt()} to cause it to
   * prematurely stop waiting, so {@code wait} should be called in a loop to
   * check that the condition that has been waited for has been met before
   * continuing.
   *
   * <p>While the thread waits, it gives up ownership of this object's
   * monitor. When it is notified (or interrupted), it re-acquires the monitor
   * before it starts running.
   *
   * @throws IllegalMonitorStateException
   *             if the thread calling this method is not the owner of this
   *             object's monitor.
   * @throws InterruptedException if the current thread has been interrupted.
   *             The interrupted status of the current thread will be cleared before the exception
   *             is thrown.
   * @see #notify
   * @see #notifyAll
   * @see #wait(long)
   * @see #wait(long,int)
   * @see java.lang.Thread
   */
  public final void wait() throws InterruptedException {}
}
