/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent.atomic;

/*-[
#include <libkern/OSAtomic.h>
]-*/

/**
 * A set of methods providing fine-grained control over happens-before
 * and synchronization order relations among reads and/or writes.  The
 * methods of this class are designed for use in uncommon situations
 * where declaring variables {@code volatile} or {@code final}, using
 * instances of atomic classes, using {@code synchronized} blocks or
 * methods, or using other synchronization facilities are not possible
 * or do not provide the desired control.
 *
 * <p><b>Memory Ordering.</b> There are three methods for controlling
 * ordering relations among memory accesses (i.e., reads and
 * writes). Method {@code orderWrites} is typically used to enforce
 * order between two writes, and {@code orderAccesses} between a write
 * and a read.  Method {@code orderReads} is used to enforce order
 * between two reads with respect to other {@code orderWrites} and/or
 * {@code orderAccesses} invocations.  The formally specified
 * properties of these methods described below provide
 * platform-independent guarantees that are honored by all levels of a
 * platform (compilers, systems, processors).  The use of these
 * methods may result in the suppression of otherwise valid compiler
 * transformations and optimizations that could visibly violate the
 * specified orderings, and may or may not entail the use of
 * processor-level "memory barrier" instructions.
 *
 * <p>Each ordering method accepts a {@code ref} argument, and
 * controls ordering among accesses with respect to this reference.
 * Invocations must be placed <em>between</em> accesses performed in
 * expression evaluations and assignment statements to control the
 * orderings of prior versus subsequent accesses appearing in program
 * order. These methods also return their arguments to simplify
 * correct usage in these contexts.
 *
 * <p>Usages of ordering methods almost always take one of the forms
 * illustrated in the examples below.  These idioms arrange some of
 * the ordering properties associated with {@code volatile} and
 * related language-based constructions, but without other
 * compile-time and runtime benefits that make language-based
 * constructions far better choices when they are applicable.  Usages
 * should be restricted to the control of strictly internal
 * implementation matters inside a class or package, and must either
 * avoid or document any consequent violations of ordering or safety
 * properties expected by users of a class employing them.
 *
 * <p><b>Reachability.</b> Method {@code reachabilityFence}
 * establishes an ordering for strong reachability (as defined in the
 * {@link java.lang.ref} package specification) with respect to
 * garbage collection.  Method {@code reachabilityFence} differs from
 * the others in that it controls relations that are otherwise only
 * implicit in a program -- the reachability conditions triggering
 * garbage collection.  As illustrated in the sample usages below,
 * this method is applicable only when reclamation may have visible
 * effects, which is possible for objects with finalizers (see Section
 * 12.6 of the Java Language Specification) that are implemented in
 * ways that rely on ordering control for correctness.
 *
 * <p><b>Sample Usages</b>
 *
 * <p><b>Safe publication.</b> With care, method {@code orderWrites}
 * may be used to obtain the memory safety effects of {@code final}
 * for a field that cannot be declared as {@code final}, because its
 * primary initialization cannot be performed in a constructor, in
 * turn because it is used in a framework requiring that all classes
 * have a no-argument constructor; as in:
 *
 *  <pre> {@code
 * class WidgetHolder {
 *   private Widget widget;
 *   public WidgetHolder() {}
 *   public static WidgetHolder newWidgetHolder(Params params) {
 *     WidgetHolder h = new WidgetHolder();
 *     h.widget = new Widget(params);
 *     return Fences.orderWrites(h);
 *   }
 * }}</pre>
 *
 * Here, the invocation of {@code orderWrites} ensures that the
 * effects of the widget assignment are ordered before those of any
 * (unknown) subsequent stores of {@code h} in other variables that
 * make {@code h} available for use by other objects.  Initialization
 * sequences using {@code orderWrites} require more care than those
 * involving {@code final} fields.  When {@code final} is not used,
 * compilers cannot help you to ensure that the field is set correctly
 * across all usages.  You must fully initialize objects
 * <em>before</em> the {@code orderWrites} invocation that makes
 * references to them safe to assign to accessible variables. Further,
 * initialization sequences must not internally "leak" the reference
 * by using it as an argument to a callback method or adding it to a
 * static data structure.  If less constrained usages were required,
 * it may be possible to cope using more extensive sets of fences, or
 * as a normally better choice, using synchronization (locking).
 * Conversely, if it were possible to do so, the best option would be
 * to rewrite class {@code WidgetHolder} to use {@code final}.
 *
 * <p>An alternative approach is to place similar mechanics in the
 * (sole) method that makes such objects available for use by others.
 * Here is a stripped-down example illustrating the essentials. In
 * practice, among other changes, you would use access methods instead
 * of a public field.
 *
 *  <pre> {@code
 * class AnotherWidgetHolder {
 *   public Widget widget;
 *   void publish(Widget w) {
 *     this.widget = Fences.orderWrites(w);
 *   }
 *   // ...
 * }}</pre>
 *
 * In this case, the {@code orderWrites} invocation occurs before the
 * store making the object available. Correctness again relies on
 * ensuring that there are no leaks prior to invoking this method, and
 * that it really is the <em>only</em> means of accessing the
 * published object.  This approach is not often applicable --
 * normally you would publish objects using a thread-safe collection
 * that itself guarantees the expected ordering relations. However, it
 * may come into play in the construction of such classes themselves.
 *
 * <p><b>Safely updating fields.</b> Outside of the initialization
 * idioms illustrated above, Fence methods ordering writes must be
 * paired with those ordering reads. To illustrate, suppose class
 * {@code c} contains an accessible variable {@code data} that should
 * have been declared as {@code volatile} but wasn't:
 *
 *  <pre> {@code
 * class C {
 *    Object data;  // need volatile access but not volatile
 *    // ...
 * }
 *
 * class App {
 *   Object getData(C c) {
 *      return Fences.orderReads(c).data;
 *   }
 *
 *   void setData(C c) {
 *      Object newValue = ...;
 *      c.data = Fences.orderWrites(newValue);
 *      Fences.orderAccesses(c);
 *   }
 *   // ...
 * }}</pre>
 *
 * Method {@code getData} provides an emulation of {@code volatile}
 * reads of (non-long/double) fields by ensuring that the read of
 * {@code c} obtained as an argument is ordered before subsequent
 * reads using this reference, and then performs the read of its
 * field. Method {@code setData} provides an emulation of volatile
 * writes, ensuring that all other relevant writes have completed,
 * then performing the assignment, and then ensuring that the write is
 * ordered before any other access.  These techniques may apply even
 * when fields are not directly accessible, in which case calls to
 * fence methods would surround calls to methods such as {@code
 * c.getData()}.  However, these techniques cannot be applied to
 * {@code long} or {@code double} fields because reads and writes of
 * fields of these types are not guaranteed to be
 * atomic. Additionally, correctness may require that all accesses of
 * such data use these kinds of wrapper methods, which you would need
 * to manually ensure.
 *
 * <p>More generally, Fence methods can be used in this way to achieve
 * the safety properties of {@code volatile}. However their use does
 * not necessarily guarantee the full sequential consistency
 * properties specified in the Java Language Specification chapter 17
 * for programs using {@code volatile}. In particular, emulation using
 * Fence methods is not guaranteed to maintain the property that
 * {@code volatile} operations performed by different threads are
 * observed in the same order by all observer threads.
 *
 * <p><b>Acquire/Release management of threadsafe objects</b>. It may
 * be possible to use weaker conventions for volatile-like variables
 * when they are used to keep track of objects that fully manage their
 * own thread-safety and synchronization.  Here, an acquiring read
 * operation remains the same as a volatile-read, but a releasing
 * write differs by virtue of not itself ensuring an ordering of its
 * write with subsequent reads, because the required effects are
 * already ensured by the referenced objects.
 * For example:
 *
 *  <pre> {@code
 * class Item {
 *    synchronized f(); // ALL methods are synchronized
 *    // ...
 * }
 *
 * class ItemHolder {
 *   private Item item;
 *   Item acquireItem() {
 *      return Fences.orderReads(item);
 *   }
 *
 *   void releaseItem(Item x) {
 *      item = Fences.orderWrites(x);
 *   }
 *
 *   // ...
 * }}</pre>
 *
 * Because this construction avoids use of {@code orderAccesses},
 * which is typically more costly than the other fence methods, it may
 * result in better performance than using {@code volatile} or its
 * emulation. However, as is the case with most applications of fence
 * methods, correctness relies on the usage context -- here, the
 * thread safety of {@code Item}, as well as the lack of need for full
 * volatile semantics inside this class itself. However, the second
 * concern means that it can be difficult to extend the {@code
 * ItemHolder} class in this example to be more useful.
 *
 * <p><b>Avoiding premature finalization.</b> Finalization may occur
 * whenever a Java Virtual Machine detects that no reference to an
 * object will ever be stored in the heap: A garbage collector may
 * reclaim an object even if the fields of that object are still in
 * use, so long as the object has otherwise become unreachable. This
 * may have surprising and undesirable effects in cases such as the
 * following example in which the bookkeeping associated with a class
 * is managed through array indices. Here, method {@code action}
 * uses a {@code reachabilityFence} to ensure that the Resource
 * object is not reclaimed before bookkeeping on an associated
 * ExternalResource has been performed; in particular here, to ensure
 * that the array slot holding the ExternalResource is not nulled out
 * in method {@link Object#finalize}, which may otherwise run
 * concurrently.
 *
 *  <pre> {@code
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
 *       Fences.reachabilityFence(this);
 *     }
 *   }
 *   private static void update(ExternalResource ext) {
 *     ext.status = ...;
 *   }
 * }}</pre>
 *
 * Here, the call to {@code reachabilityFence} is nonintuitively
 * placed <em>after</em> the call to {@code update}, to ensure that
 * the array slot is not nulled out by {@link Object#finalize} before
 * the update, even if the call to {@code action} was the last use of
 * this object. This might be the case if for example a usage in a
 * user program had the form {@code new Resource().action();} which
 * retains no other reference to this Resource.  While probably
 * overkill here, {@code reachabilityFence} is placed in a {@code
 * finally} block to ensure that it is invoked across all paths in the
 * method.  In a method with more complex control paths, you might
 * need further precautions to ensure that {@code reachabilityFence}
 * is encountered along all of them.
 *
 * <p>It is sometimes possible to better encapsulate use of
 * {@code reachabilityFence}. Continuing the above example, if it
 * were OK for the call to method update to proceed even if the
 * finalizer had already executed (nulling out slot), then you could
 * localize use of {@code reachabilityFence}:
 *
 *  <pre> {@code
 * public void action2() {
 *   // ...
 *   Resource.update(getExternalResource());
 * }
 * private ExternalResource getExternalResource() {
 *   ExternalResource ext = externalResourceArray[myIndex];
 *   Fences.reachabilityFence(this);
 *   return ext;
 * }}</pre>
 *
 * <p>Method {@code reachabilityFence} is not required in
 * constructions that themselves ensure reachability. For example,
 * because objects that are locked cannot in general be reclaimed, it
 * would suffice if all accesses of the object, in all methods of
 * class Resource (including {@code finalize}) were enclosed in {@code
 * synchronized (this)} blocks. (Further, such blocks must not include
 * infinite loops, or themselves be unreachable, which fall into the
 * corner case exceptions to the "in general" disclaimer.) However,
 * method {@code reachabilityFence} remains a better option in cases
 * where this approach is not as efficient, desirable, or possible;
 * for example because it would encounter deadlock.
 *
 * <p><b>Formal Properties.</b>
 *
 * <p>Using the terminology of The Java Language Specification chapter
 * 17, the rules governing the semantics of the methods of this class
 * are as follows:
 *
 * <p> The following is still under construction.
 *
 * <dl>
 *
 *   <dt><b>[Definitions]</b>
 *   <dd>
 *   <ul>
 *
 *     <li>Define <em>sequenced(a, b)</em> to be true if <em>a</em>
 *     occurs before <em>b</em> in <em>program order</em>.
 *
 *     <li>Define <em>accesses(a, p)</em> to be true if
 *     <em>a</em> is a read or write of a field (or if an array, an
 *     element) of the object referenced by <em>p</em>.
 *
 *     <li>Define <em>deeplyAccesses(a, p)</em> to be true if either
 *     <em>accesses(a, p)</em> or <em>deeplyAccesses(a, q)</em> where
 *     <em>q</em> is the value seen by some read <em>r</em>
 *     such that <em>accesses(r, p)</em>.
 *
 *   </ul>
 *   <p>
 *   <dt><b>[Matching]</b>
 *   <dd> Given:
 *
 *   <ul>
 *
 *     <li><em>p</em>, a reference to an object
 *
 *     <li><em>wf</em>, an invocation of {@code orderWrites(p)} or
 *       {@code orderAccesses(p)}
 *
 *     <li><em>w</em>, a write of value <em>p</em>
 *
 *     <li> <em>rf</em>, an invocation of {@code orderReads(p)} or
 *     {@code orderAccesses(p)}
 *
 *     <li> <em>r</em>, a read returning value <em>p</em>
 *
 *   </ul>
 *   If:
 *   <ul>
 *     <li>sequenced(wf, w)
 *     <li>read <em>r</em> sees write <em>w</em>
 *     <li>sequenced(r, rf)
 *   </ul>
 *   Then:
 *   <ul>
 *
 *     <li> <em>wf happens-before rf</em>
 *
 *     <li> <em>wf</em> precedes <em>rf</em> in the
 *          <em>synchronization order</em>
 *
 *     <li> If (<em>r1</em>, <em>w1</em>) and (<em>r2</em>,
 *     <em>w2</em>) are two pairs of reads and writes, both
 *     respectively satisfying the above conditions for <em>p</em>,
 *     and sequenced(r1, r2) then it is not the case that <em>w2
 *     happens-before w1</em>.
 *
 *   </ul>
 *   <p>
 *   <dt><b>[Initial Reads]</b>
 *   <dd> Given:
 *
 *   <ul>
 *
 *     <li><em>p</em>, a reference to an object
 *
 *     <li> <em>a</em>, an access where deeplyAccesses(a, p)
 *
 *     <li><em>wf</em>, an invocation of {@code orderWrites(p)} or
 *       {@code orderAccesses(p)}
 *
 *     <li><em>w</em>, a write of value <em>p</em>
 *
 *     <li> <em>r</em>, a read returning value <em>p</em>
 *
 *     <li> <em>b</em>, an access where accesses(b, p)
 *
 *   </ul>
 *   If:
 *   <ul>
 *     <li>sequenced(a, wf);
 *     <li>sequenced(wf, w)
 *     <li>read <em>r</em> sees write <em>w</em>, and
 *         <em>r</em> is the first read by some thread
 *         <em>t</em> that sees value <em>p</em>
 *     <li>sequenced(r, b)
 *   </ul>
 *   Then:
 *   <ul>
 *     <li> the effects of <em>b</em> are constrained
 *          by the relation <em>a happens-before b</em>.
 *   </ul>
 *  <p>
 *  <dt><b>[orderAccesses]</b>
 *  <dd> Given:
 *
 *   <ul>
 *     <li><em>p</em>, a reference to an object
 *     <li><em>f</em>, an invocation of {@code orderAccesses(p)}
 *   </ul>
 *   If:
 *   <ul>
 *     <li>sequenced(f, w)
 *   </ul>
 *
 *    Then:
 *
 *   <ul>
 *
 *     <li> <em>f</em> is an element of the <em>synchronization order</em>.
 *
 *   </ul>
 *   <p>
 *   <dt><b>[Reachability]</b>
 *   <dd> Given:
 *
 *   <ul>
 *
 *     <li><em>p</em>, a reference to an object
 *
 *     <li><em>f</em>, an invocation of {@code reachabilityFence(p)}
 *
 *     <li><em>a</em>, an access where accesses(a, p)
 *
 *     <li><em>b</em>, an action (by a garbage collector) taking
 *     the form of an invocation of {@code
 *     p.finalize()} or of enqueing any {@link
 *     java.lang.ref.Reference} constructed with argument <em>p</em>
 *
 *   </ul>
 *
 *   If:
 *   <ul>
 *     <li>sequenced(a, f)
 *   </ul>
 *
 *    Then:
 *
 *   <ul>
 *
 *     <li> <em>a happens-before b</em>.
 *
 *   </ul>
 *
 * </dl>
 *
 * @since 1.7
 * @hide
 * @author Doug Lea
 */
public class Fences {
    private Fences() {} // Non-instantiable

    /**
     * Informally: Ensures that a read of the given reference prior to
     * the invocation of this method occurs before a subsequent use of
     * the given reference with the effect of reading or writing a
     * field (or if an array, element) of the referenced object.  The
     * use of this method is sensible only when paired with other
     * invocations of {@link #orderWrites} and/or {@link
     * #orderAccesses} for the given reference. For details, see the
     * class documentation for this class.
     *
     * @param ref the reference. If null, this method has no effect.
     * @return the given ref, to simplify usage
     */
    public static <T> T orderReads(T ref) {
        memoryBarrier();
        return ref;
    }

    /**
     * Informally: Ensures that a use of the given reference with the
     * effect of reading or writing a field (or if an array, element)
     * of the referenced object, prior to the invocation of this
     * method occur before a subsequent write of the reference. For
     * details, see the class documentation for this class.
     *
     * @param ref the reference. If null, this method has no effect.
     * @return the given ref, to simplify usage
     */
    public static <T> T orderWrites(T ref) {
        memoryBarrier();
        return ref;
    }

    /**
     * Informally: Ensures that accesses (reads or writes) using the
     * given reference prior to the invocation of this method occur
     * before subsequent accesses.  For details, see the class
     * documentation for this class.
     *
     * @param ref the reference. If null, this method has no effect.
     * @return the given ref, to simplify usage
     */
    public static <T> T orderAccesses(T ref) {
        memoryBarrier();
        return ref;
    }

    /**
     * Ensures that the object referenced by the given reference
     * remains <em>strongly reachable</em> (as defined in the {@link
     * java.lang.ref} package documentation), regardless of any prior
     * actions of the program that might otherwise cause the object to
     * become unreachable; thus, the referenced object is not
     * reclaimable by garbage collection at least until after the
     * invocation of this method. Invocation of this method does not
     * itself initiate garbage collection or finalization.
     *
     * <p>See the class-level documentation for further explanation
     * and usage examples.
     *
     * @param ref the reference. If null, this method has no effect.
     */
    public static void reachabilityFence(Object ref) {
        if (ref != null) {
            synchronized (ref) {}
        }
    }

    /**
     * Invoke an iOS memory barrier, as defined in "man barrier".
     */
    private static native void memoryBarrier() /*-[
      OSMemoryBarrier();
    ]-*/;
}
