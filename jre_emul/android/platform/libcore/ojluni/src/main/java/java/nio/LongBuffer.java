/*
 * Copyright (c) 2000, 2008, Oracle and/or its affiliates. All rights reserved.
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

// -- This file was mechanically generated: Do not edit! -- //

package java.nio;


/**
 * A long buffer.
 *
 * <p> This class defines four categories of operations upon
 * long buffers:
 *
 * <ul>
 *
 * <li><p> Absolute and relative {@link #get() </code><i>get</i><code>} and
 * {@link #put(long) </code><i>put</i><code>} methods that read and write
 * single longs; </p></li>
 *
 * <li><p> Relative {@link #get(long[]) </code><i>bulk get</i><code>}
 * methods that transfer contiguous sequences of longs from this buffer
 * into an array; and</p></li>
 *
 * <li><p> Relative {@link #put(long[]) </code><i>bulk put</i><code>}
 * methods that transfer contiguous sequences of longs from a
 * long array or some other long
 * buffer into this buffer;&#32;and </p></li>
 *
 * <li><p> Methods for {@link #compact </code>compacting<code>}, {@link
 * #duplicate </code>duplicating<code>}, and {@link #slice
 * </code>slicing<code>} a long buffer.  </p></li>
 *
 * </ul>
 *
 * <p> Long buffers can be created either by {@link #allocate
 * </code><i>allocation</i><code>}, which allocates space for the buffer's
 * content, by {@link #wrap(long[]) </code><i>wrapping</i><code>} an existing
 * long array  into a buffer, or by creating a
 * <a href="ByteBuffer.html#views"><i>view</i></a> of an existing byte buffer.
 *
 * <p> Like a byte buffer, a long buffer is either <a
 * href="ByteBuffer.html#direct"><i>direct</i> or <i>non-direct</i></a>.  A
 * long buffer created via the <tt>wrap</tt> methods of this class will
 * be non-direct.  A long buffer created as a view of a byte buffer will
 * be direct if, and only if, the byte buffer itself is direct.  Whether or not
 * a long buffer is direct may be determined by invoking the {@link
 * #isDirect isDirect} method.  </p>
 *
 * <p> Methods in this class that do not otherwise have a value to return are
 * specified to return the buffer upon which they are invoked.  This allows
 * method invocations to be chained.
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 */

public abstract class LongBuffer
        extends Buffer
        implements Comparable<LongBuffer> {

    // These fields are declared here rather than in Heap-X-Buffer in order to
    // reduce the number of virtual method invocations needed to access these
    // values, which is especially costly when coding small buffers.
    //
    final long[] hb;                  // Non-null only for heap buffers
    final int offset;
    boolean isReadOnly;                 // Valid only for heap buffers

    // Creates a new buffer with the given mark, position, limit, capacity,
    // backing array, and array offset
    //
    LongBuffer(int mark, int pos, int lim, int cap,   // package-private
               long[] hb, int offset) {
        super(mark, pos, lim, cap, 3);
        this.hb = hb;
        this.offset = offset;
    }

    // Creates a new buffer with the given mark, position, limit, and capacity
    //
    LongBuffer(int mark, int pos, int lim, int cap) { // package-private
        this(mark, pos, lim, cap, null, 0);
    }


    /**
     * Allocates a new long buffer.
     *
     * <p> The new buffer's position will be zero, its limit will be its
     * capacity, its mark will be undefined, and each of its elements will be
     * initialized to zero.  It will have a {@link #array
     * </code>backing array<code>}, and its {@link #arrayOffset </code>array
     * offset<code>} will be zero.
     *
     * @param capacity The new buffer's capacity, in longs
     * @return The new long buffer
     * @throws IllegalArgumentException If the <tt>capacity</tt> is a negative integer
     */
    public static LongBuffer allocate(int capacity) {
        if (capacity < 0)
            throw new IllegalArgumentException();
        return new HeapLongBuffer(capacity, capacity);
    }

    /**
     * Wraps a long array into a buffer.
     *
     * <p> The new buffer will be backed by the given long array;
     * that is, modifications to the buffer will cause the array to be modified
     * and vice versa.  The new buffer's capacity will be
     * <tt>array.length</tt>, its position will be <tt>offset</tt>, its limit
     * will be <tt>offset + length</tt>, and its mark will be undefined.  Its
     * {@link #array </code>backing array<code>} will be the given array, and
     * its {@link #arrayOffset </code>array offset<code>} will be zero.  </p>
     *
     * @param array  The array that will back the new buffer
     * @param offset The offset of the subarray to be used; must be non-negative and
     *               no larger than <tt>array.length</tt>.  The new buffer's position
     *               will be set to this value.
     * @param length The length of the subarray to be used;
     *               must be non-negative and no larger than
     *               <tt>array.length - offset</tt>.
     *               The new buffer's limit will be set to <tt>offset + length</tt>.
     * @return The new long buffer
     * @throws IndexOutOfBoundsException If the preconditions on the <tt>offset</tt> and
     *                                   <tt>length</tt>
     *                                   parameters do not hold
     */
    public static LongBuffer wrap(long[] array,
                                  int offset, int length) {
        try {
            return new HeapLongBuffer(array, offset, length);
        } catch (IllegalArgumentException x) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Wraps a long array into a buffer.
     *
     * <p> The new buffer will be backed by the given long array;
     * that is, modifications to the buffer will cause the array to be modified
     * and vice versa.  The new buffer's capacity and limit will be
     * <tt>array.length</tt>, its position will be zero, and its mark will be
     * undefined.  Its {@link #array </code>backing array<code>} will be the
     * given array, and its {@link #arrayOffset </code>array offset<code>} will
     * be zero.  </p>
     *
     * @param array The array that will back this buffer
     * @return The new long buffer
     */
    public static LongBuffer wrap(long[] array) {
        return wrap(array, 0, array.length);
    }


    /**
     * Creates a new long buffer whose content is a shared subsequence of
     * this buffer's content.
     *
     * <p> The content of the new buffer will start at this buffer's current
     * position.  Changes to this buffer's content will be visible in the new
     * buffer, and vice versa; the two buffers' position, limit, and mark
     * values will be independent.
     *
     * <p> The new buffer's position will be zero, its capacity and its limit
     * will be the number of longs remaining in this buffer, and its mark
     * will be undefined.  The new buffer will be direct if, and only if, this
     * buffer is direct, and it will be read-only if, and only if, this buffer
     * is read-only.  </p>
     *
     * @return The new long buffer
     */
    public abstract LongBuffer slice();

    /**
     * Creates a new long buffer that shares this buffer's content.
     *
     * <p> The content of the new buffer will be that of this buffer.  Changes
     * to this buffer's content will be visible in the new buffer, and vice
     * versa; the two buffers' position, limit, and mark values will be
     * independent.
     *
     * <p> The new buffer's capacity, limit, position, and mark values will be
     * identical to those of this buffer.  The new buffer will be direct if,
     * and only if, this buffer is direct, and it will be read-only if, and
     * only if, this buffer is read-only.  </p>
     *
     * @return The new long buffer
     */
    public abstract LongBuffer duplicate();

    /**
     * Creates a new, read-only long buffer that shares this buffer's
     * content.
     *
     * <p> The content of the new buffer will be that of this buffer.  Changes
     * to this buffer's content will be visible in the new buffer; the new
     * buffer itself, however, will be read-only and will not allow the shared
     * content to be modified.  The two buffers' position, limit, and mark
     * values will be independent.
     *
     * <p> The new buffer's capacity, limit, position, and mark values will be
     * identical to those of this buffer.
     *
     * <p> If this buffer is itself read-only then this method behaves in
     * exactly the same way as the {@link #duplicate duplicate} method.  </p>
     *
     * @return The new, read-only long buffer
     */
    public abstract LongBuffer asReadOnlyBuffer();


    // -- Singleton get/put methods --

    /**
     * Relative <i>get</i> method.  Reads the long at this buffer's
     * current position, and then increments the position. </p>
     *
     * @return The long at the buffer's current position
     * @throws BufferUnderflowException If the buffer's current position is not smaller than its
     *                                  limit
     */
    public abstract long get();

    /**
     * Relative <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> Writes the given long into this buffer at the current
     * position, and then increments the position. </p>
     *
     * @param l The long to be written
     * @return This buffer
     * @throws BufferOverflowException If this buffer's current position is not smaller than its
     *                                 limit
     * @throws ReadOnlyBufferException If this buffer is read-only
     */
    public abstract LongBuffer put(long l);

    /**
     * Absolute <i>get</i> method.  Reads the long at the given
     * index. </p>
     *
     * @param index The index from which the long will be read
     * @return The long at the given index
     * @throws IndexOutOfBoundsException If <tt>index</tt> is negative
     *                                   or not smaller than the buffer's limit
     */
    public abstract long get(int index);

    /**
     * Absolute <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> Writes the given long into this buffer at the given
     * index. </p>
     *
     * @param index The index at which the long will be written
     * @param l     The long value to be written
     * @return This buffer
     * @throws IndexOutOfBoundsException If <tt>index</tt> is negative
     *                                   or not smaller than the buffer's limit
     * @throws ReadOnlyBufferException   If this buffer is read-only
     */
    public abstract LongBuffer put(int index, long l);


    // -- Bulk get operations --

    /**
     * Relative bulk <i>get</i> method.
     *
     * <p> This method transfers longs from this buffer into the given
     * destination array.  If there are fewer longs remaining in the
     * buffer than are required to satisfy the request, that is, if
     * <tt>length</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>, then no
     * longs are transferred and a {@link BufferUnderflowException} is
     * thrown.
     *
     * <p> Otherwise, this method copies <tt>length</tt> longs from this
     * buffer into the given array, starting at the current position of this
     * buffer and at the given offset in the array.  The position of this
     * buffer is then incremented by <tt>length</tt>.
     *
     * <p> In other words, an invocation of this method of the form
     * <tt>src.get(dst,&nbsp;off,&nbsp;len)</tt> has exactly the same effect as
     * the loop
     *
     * <pre>
     *     for (int i = off; i < off + len; i++)
     *         dst[i] = src.get(); </pre>
     *
     * except that it first checks that there are sufficient longs in
     * this buffer and it is potentially much more efficient. </p>
     *
     * @param dst    The array into which longs are to be written
     * @param offset The offset within the array of the first long to be
     *               written; must be non-negative and no larger than
     *               <tt>dst.length</tt>
     * @param length The maximum number of longs to be written to the given
     *               array; must be non-negative and no larger than
     *               <tt>dst.length - offset</tt>
     * @return This buffer
     * @throws BufferUnderflowException  If there are fewer than <tt>length</tt> longs
     *                                   remaining in this buffer
     * @throws IndexOutOfBoundsException If the preconditions on the <tt>offset</tt> and
     *                                   <tt>length</tt>
     *                                   parameters do not hold
     */
    public LongBuffer get(long[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        if (length > remaining())
            throw new BufferUnderflowException();
        int end = offset + length;
        for (int i = offset; i < end; i++)
            dst[i] = get();
        return this;
    }

    /**
     * Relative bulk <i>get</i> method.
     *
     * <p> This method transfers longs from this buffer into the given
     * destination array.  An invocation of this method of the form
     * <tt>src.get(a)</tt> behaves in exactly the same way as the invocation
     *
     * <pre>
     *     src.get(a, 0, a.length) </pre>
     *
     * @return This buffer
     * @throws BufferUnderflowException If there are fewer than <tt>length</tt> longs
     *                                  remaining in this buffer
     */
    public LongBuffer get(long[] dst) {
        return get(dst, 0, dst.length);
    }


    // -- Bulk put operations --

    /**
     * Relative bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> This method transfers the longs remaining in the given source
     * buffer into this buffer.  If there are more longs remaining in the
     * source buffer than in this buffer, that is, if
     * <tt>src.remaining()</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>,
     * then no longs are transferred and a {@link
     * BufferOverflowException} is thrown.
     *
     * <p> Otherwise, this method copies
     * <i>n</i>&nbsp;=&nbsp;<tt>src.remaining()</tt> longs from the given
     * buffer into this buffer, starting at each buffer's current position.
     * The positions of both buffers are then incremented by <i>n</i>.
     *
     * <p> In other words, an invocation of this method of the form
     * <tt>dst.put(src)</tt> has exactly the same effect as the loop
     *
     * <pre>
     *     while (src.hasRemaining())
     *         dst.put(src.get()); </pre>
     *
     * except that it first checks that there is sufficient space in this
     * buffer and it is potentially much more efficient. </p>
     *
     * @param src The source buffer from which longs are to be read;
     *            must not be this buffer
     * @return This buffer
     * @throws BufferOverflowException  If there is insufficient space in this buffer
     *                                  for the remaining longs in the source buffer
     * @throws IllegalArgumentException If the source buffer is this buffer
     * @throws ReadOnlyBufferException  If this buffer is read-only
     */
    public LongBuffer put(LongBuffer src) {
        if (src == this)
            throw new IllegalArgumentException();
        int n = src.remaining();
        if (n > remaining())
            throw new BufferOverflowException();
        for (int i = 0; i < n; i++)
            put(src.get());
        return this;
    }

    /**
     * Relative bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> This method transfers longs into this buffer from the given
     * source array.  If there are more longs to be copied from the array
     * than remain in this buffer, that is, if
     * <tt>length</tt>&nbsp;<tt>&gt;</tt>&nbsp;<tt>remaining()</tt>, then no
     * longs are transferred and a {@link BufferOverflowException} is
     * thrown.
     *
     * <p> Otherwise, this method copies <tt>length</tt> longs from the
     * given array into this buffer, starting at the given offset in the array
     * and at the current position of this buffer.  The position of this buffer
     * is then incremented by <tt>length</tt>.
     *
     * <p> In other words, an invocation of this method of the form
     * <tt>dst.put(src,&nbsp;off,&nbsp;len)</tt> has exactly the same effect as
     * the loop
     *
     * <pre>
     *     for (int i = off; i < off + len; i++)
     *         dst.put(a[i]); </pre>
     *
     * except that it first checks that there is sufficient space in this
     * buffer and it is potentially much more efficient. </p>
     *
     * @param src    The array from which longs are to be read
     * @param offset The offset within the array of the first long to be read;
     *               must be non-negative and no larger than <tt>array.length</tt>
     * @param length The number of longs to be read from the given array;
     *               must be non-negative and no larger than
     *               <tt>array.length - offset</tt>
     * @return This buffer
     * @throws BufferOverflowException   If there is insufficient space in this buffer
     * @throws IndexOutOfBoundsException If the preconditions on the <tt>offset</tt> and
     *                                   <tt>length</tt>
     *                                   parameters do not hold
     * @throws ReadOnlyBufferException   If this buffer is read-only
     */
    public LongBuffer put(long[] src, int offset, int length) {
        checkBounds(offset, length, src.length);
        if (length > remaining())
            throw new BufferOverflowException();
        int end = offset + length;
        for (int i = offset; i < end; i++)
            this.put(src[i]);
        return this;
    }

    /**
     * Relative bulk <i>put</i> method&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> This method transfers the entire content of the given source
     * long array into this buffer.  An invocation of this method of the
     * form <tt>dst.put(a)</tt> behaves in exactly the same way as the
     * invocation
     *
     * <pre>
     *     dst.put(a, 0, a.length) </pre>
     *
     * @return This buffer
     * @throws BufferOverflowException If there is insufficient space in this buffer
     * @throws ReadOnlyBufferException If this buffer is read-only
     */
    public final LongBuffer put(long[] src) {
        return put(src, 0, src.length);
    }


    // -- Other stuff --

    /**
     * Tells whether or not this buffer is backed by an accessible long
     * array.
     *
     * <p> If this method returns <tt>true</tt> then the {@link #array() array}
     * and {@link #arrayOffset() arrayOffset} methods may safely be invoked.
     * </p>
     *
     * @return <tt>true</tt> if, and only if, this buffer
     * is backed by an array and is not read-only
     */
    public final boolean hasArray() {
        return (hb != null) && !isReadOnly;
    }

    /**
     * Returns the long array that backs this
     * buffer&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> Modifications to this buffer's content will cause the returned
     * array's content to be modified, and vice versa.
     *
     * <p> Invoke the {@link #hasArray hasArray} method before invoking this
     * method in order to ensure that this buffer has an accessible backing
     * array.  </p>
     *
     * @return The array that backs this buffer
     * @throws ReadOnlyBufferException       If this buffer is backed by an array but is read-only
     * @throws UnsupportedOperationException If this buffer is not backed by an accessible array
     */
    public final long[] array() {
        if (hb == null)
            throw new UnsupportedOperationException();
        if (isReadOnly)
            throw new ReadOnlyBufferException();
        return hb;
    }

    /**
     * Returns the offset within this buffer's backing array of the first
     * element of the buffer&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> If this buffer is backed by an array then buffer position <i>p</i>
     * corresponds to array index <i>p</i>&nbsp;+&nbsp;<tt>arrayOffset()</tt>.
     *
     * <p> Invoke the {@link #hasArray hasArray} method before invoking this
     * method in order to ensure that this buffer has an accessible backing
     * array.  </p>
     *
     * @return The offset within this buffer's array
     * of the first element of the buffer
     * @throws ReadOnlyBufferException       If this buffer is backed by an array but is read-only
     * @throws UnsupportedOperationException If this buffer is not backed by an accessible array
     */
    public final int arrayOffset() {
        if (hb == null)
            throw new UnsupportedOperationException();
        if (isReadOnly)
            throw new ReadOnlyBufferException();
        return offset;
    }

    /**
     * Compacts this buffer&nbsp;&nbsp;<i>(optional operation)</i>.
     *
     * <p> The longs between the buffer's current position and its limit,
     * if any, are copied to the beginning of the buffer.  That is, the
     * long at index <i>p</i>&nbsp;=&nbsp;<tt>position()</tt> is copied
     * to index zero, the long at index <i>p</i>&nbsp;+&nbsp;1 is copied
     * to index one, and so forth until the long at index
     * <tt>limit()</tt>&nbsp;-&nbsp;1 is copied to index
     * <i>n</i>&nbsp;=&nbsp;<tt>limit()</tt>&nbsp;-&nbsp;<tt>1</tt>&nbsp;-&nbsp;<i>p</i>.
     * The buffer's position is then set to <i>n+1</i> and its limit is set to
     * its capacity.  The mark, if defined, is discarded.
     *
     * <p> The buffer's position is set to the number of longs copied,
     * rather than to zero, so that an invocation of this method can be
     * followed immediately by an invocation of another relative <i>put</i>
     * method. </p>
     *
     * @return This buffer
     * @throws ReadOnlyBufferException If this buffer is read-only
     */
    public abstract LongBuffer compact();

    /**
     * Tells whether or not this long buffer is direct. </p>
     *
     * @return <tt>true</tt> if, and only if, this buffer is direct
     */
    public abstract boolean isDirect();


    /**
     * Returns a string summarizing the state of this buffer.  </p>
     *
     * @return A summary string
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append("[pos=");
        sb.append(position());
        sb.append(" lim=");
        sb.append(limit());
        sb.append(" cap=");
        sb.append(capacity());
        sb.append("]");
        return sb.toString();
    }


    /**
     * Returns the current hash code of this buffer.
     *
     * <p> The hash code of a long buffer depends only upon its remaining
     * elements; that is, upon the elements from <tt>position()</tt> up to, and
     * including, the element at <tt>limit()</tt>&nbsp;-&nbsp;<tt>1</tt>.
     *
     * <p> Because buffer hash codes are content-dependent, it is inadvisable
     * to use buffers as keys in hash maps or similar data structures unless it
     * is known that their contents will not change.  </p>
     *
     * @return The current hash code of this buffer
     */
    public int hashCode() {
        int h = 1;
        int p = position();
        for (int i = limit() - 1; i >= p; i--)
            h = 31 * h + (int) get(i);
        return h;
    }

    /**
     * Tells whether or not this buffer is equal to another object.
     *
     * <p> Two long buffers are equal if, and only if,
     *
     * <p><ol>
     *
     * <li><p> They have the same element type,  </p></li>
     *
     * <li><p> They have the same number of remaining elements, and
     * </p></li>
     *
     * <li><p> The two sequences of remaining elements, considered
     * independently of their starting positions, are pointwise equal.
     *
     *
     *
     *
     *
     *
     *
     * </p></li>
     *
     * </ol>
     *
     * <p> A long buffer is not equal to any other type of object.  </p>
     *
     * @param ob The object to which this buffer is to be compared
     * @return <tt>true</tt> if, and only if, this buffer is equal to the
     * given object
     */
    public boolean equals(Object ob) {
        if (this == ob)
            return true;
        if (!(ob instanceof LongBuffer))
            return false;
        LongBuffer that = (LongBuffer) ob;
        if (this.remaining() != that.remaining())
            return false;
        int p = this.position();
        for (int i = this.limit() - 1, j = that.limit() - 1; i >= p; i--, j--)
            if (!equals(this.get(i), that.get(j)))
                return false;
        return true;
    }

    private static boolean equals(long x, long y) {


        return x == y;

    }

    /**
     * Compares this buffer to another.
     *
     * <p> Two long buffers are compared by comparing their sequences of
     * remaining elements lexicographically, without regard to the starting
     * position of each sequence within its corresponding buffer.
     *
     *
     *
     *
     *
     *
     *
     *
     * Pairs of {@code long} elements are compared as if by invoking
     * {@link Long#compare(long, long)}.
     *
     *
     * <p> A long buffer is not comparable to any other type of object.
     *
     * @return A negative integer, zero, or a positive integer as this buffer
     * is less than, equal to, or greater than the given buffer
     */
    public int compareTo(LongBuffer that) {
        int n = this.position() + Math.min(this.remaining(), that.remaining());
        for (int i = this.position(), j = that.position(); i < n; i++, j++) {
            int cmp = compare(this.get(i), that.get(j));
            if (cmp != 0)
                return cmp;
        }
        return this.remaining() - that.remaining();
    }

    private static int compare(long x, long y) {


        return Long.compare(x, y);

    }

    // -- Other char stuff --


    // -- Other byte stuff: Access to binary data --


    /**
     * Retrieves this buffer's byte order.
     *
     * <p> The byte order of a long buffer created by allocation or by
     * wrapping an existing <tt>long</tt> array is the {@link
     * ByteOrder#nativeOrder </code>native order<code>} of the underlying
     * hardware.  The byte order of a long buffer created as a <a
     * href="ByteBuffer.html#views">view</a> of a byte buffer is that of the
     * byte buffer at the moment that the view is created.  </p>
     *
     * @return This buffer's byte order
     */
    public abstract ByteOrder order();


}
