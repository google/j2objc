/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 2000, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.nio;

import java.io.FileDescriptor;

import libcore.io.Memory;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

// Not final because it is extended in tests.
/** @hide */
public class DirectByteBuffer extends MappedByteBuffer implements DirectBuffer {

    /**
     * Stores the details of the memory backing a DirectByteBuffer. This could be a pointer
     * (passed through from JNI or resulting from a mapping) or a non-movable byte array allocated
     * from Java. Each MemoryRef also has an isAccessible associated with it, which determines
     * whether the underlying memory is "accessible". The notion of "accessibility" is usually
     * defined by the allocator of the reference, and is separate from the accessibility of the
     * memory as defined by the underlying system.
     *
     * A single MemoryRef instance is shared across all slices and duplicates of a given buffer.
     */
    final static class MemoryRef {
        byte[] buffer;
        long allocatedAddress;
        final int offset;
        boolean isAccessible;
        boolean isFreed;


        // Reference to original DirectByteBuffer that held this MemoryRef. The field is set
        // only for the MemoryRef created through JNI NewDirectByteBuffer(void*, long) function.
        // This allows users of JNI NewDirectByteBuffer to create a PhantomReference on the
        // DirectByteBuffer instance that will only be put in the associated ReferenceQueue when
        // the underlying memory is not referenced by any DirectByteBuffer instance. The
        // MemoryRef can outlive the original DirectByteBuffer instance if, for example, slice()
        // or asReadOnlyBuffer() are called and all strong references to the original DirectByteBuffer
        // are discarded.
        final Object originalBufferObject;

        private native void allocate(int byteCount) /*-[
            JreStrongAssign(&self->buffer_, [IOSByteArray arrayWithLength:byteCount]);
            self->allocatedAddress_ = (long long) (uintptr_t) (self->buffer_)->buffer_;
        ]-*/;

        MemoryRef(int capacity) {
            allocate(capacity + 7);
            /* J2ObjC: unused.
            VMRuntime runtime = VMRuntime.getRuntime();
            buffer = (byte[]) runtime.newNonMovableArray(byte.class, capacity + 7);
            allocatedAddress = runtime.addressOf(buffer);
            */
            // Offset is set to handle the alignment: http://b/16449607
            offset = (int) (((allocatedAddress + 7) & ~(long) 7) - allocatedAddress);
            isAccessible = true;
            isFreed = false;
            originalBufferObject = null;
        }

        MemoryRef(long allocatedAddress, Object originalBufferObject) {
            buffer = null;
            this.allocatedAddress = allocatedAddress;
            this.offset = 0;
            this.originalBufferObject = originalBufferObject;
            isAccessible = true;
        }

        void free() {
            buffer = null;
            allocatedAddress = 0;
            isAccessible = false;
            isFreed = true;
        }
    }

    final Cleaner cleaner;
    final MemoryRef memoryRef;

    DirectByteBuffer(int capacity, MemoryRef memoryRef) {
        super(-1, 0, capacity, capacity, memoryRef.buffer, memoryRef.offset);
        // Only have references to java objects, no need for a cleaner since the GC will do all
        // the work.
        this.memoryRef = memoryRef;
        this.address = memoryRef.allocatedAddress + memoryRef.offset;
        cleaner = null;
        this.isReadOnly = false;
    }

    // Invoked only by JNI: NewDirectByteBuffer(void*, long)
    @SuppressWarnings("unused")
    DirectByteBuffer(long addr, int cap) {
        super(-1, 0, cap, cap);
        memoryRef = new MemoryRef(addr, this);
        address = addr;
        cleaner = null;
    }

    /** @hide */
    public DirectByteBuffer(int cap, long addr,
                            FileDescriptor fd,
                            Runnable unmapper,
                            boolean isReadOnly) {
        super(-1, 0, cap, cap, fd);
        this.isReadOnly = isReadOnly;
        memoryRef = new MemoryRef(addr, null);
        address = addr;
        cleaner = Cleaner.create(memoryRef, unmapper);
    }

    // For duplicates and slices
    DirectByteBuffer(MemoryRef memoryRef,         // package-private
                     int mark, int pos, int lim, int cap,
                     int off) {
        this(memoryRef, mark, pos, lim, cap, off, false);
    }

    DirectByteBuffer(MemoryRef memoryRef,         // package-private
                     int mark, int pos, int lim, int cap,
                     int off, boolean isReadOnly) {
        super(mark, pos, lim, cap, memoryRef.buffer, off);
        this.isReadOnly = isReadOnly;
        this.memoryRef = memoryRef;
        address = memoryRef.allocatedAddress + off;
        cleaner = null;
    }

    @Override
    public final Object attachment() {
        return memoryRef;
    }

    @Override
    public final Cleaner cleaner() {
        return cleaner;
    }

    @Override
    public final ByteBuffer slice() {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        int pos = position();
        int lim = limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        int off = pos + offset;
        assert (off >= 0);
        return new DirectByteBuffer(memoryRef, -1, 0, rem, rem, off, isReadOnly);
    }

    @Override
    public final ByteBuffer duplicate() {
        if (memoryRef.isFreed) {
            throw new IllegalStateException("buffer has been freed");
        }
        return new DirectByteBuffer(memoryRef,
                this.markValue(),
                this.position(),
                this.limit(),
                this.capacity(),
                offset,
                isReadOnly);
    }

    @Override
    public final ByteBuffer asReadOnlyBuffer() {
        if (memoryRef.isFreed) {
            throw new IllegalStateException("buffer has been freed");
        }
        return new DirectByteBuffer(memoryRef,
                this.markValue(),
                this.position(),
                this.limit(),
                this.capacity(),
                offset,
                true);
    }

    @Override
    public final long address() {
        return address;
    }

    private long ix(int i) {
        return address + i;
    }

    private byte get(long a) {
        return Memory.peekByte(a);
    }

    @Override
    public final byte get() {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return get(ix(nextGetIndex()));
    }

    @Override
    public final byte get(int i) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return get(ix(checkIndex(i)));
    }

    // This method is not declared final because it is overridden in tests.
    @Override
    public ByteBuffer get(byte[] dst, int dstOffset, int length) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        checkBounds(dstOffset, length, dst.length);
        int pos = position();
        int lim = limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        if (length > rem)
            throw new BufferUnderflowException();
        Memory.peekByteArray(ix(pos),
                dst, dstOffset, length);
        position = pos + length;
        return this;
    }

    private ByteBuffer put(long a, byte x) {
        Memory.pokeByte(a, x);
        return this;
    }

    @Override
    public ByteBuffer put(ByteBuffer src) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return super.put(src);
    }

    @Override
    public final ByteBuffer put(byte x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        put(ix(nextPutIndex()), x);
        return this;
    }

    @Override
    public final ByteBuffer put(int i, byte x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        put(ix(checkIndex(i)), x);
        return this;
    }

    // This method is not declared final because it is overridden in tests.
    @Override
    public ByteBuffer put(byte[] src, int srcOffset, int length) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        checkBounds(srcOffset, length, src.length);
        int pos = position();
        int lim = limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        if (length > rem)
            throw new BufferOverflowException();
        Memory.pokeByteArray(ix(pos),
                src, srcOffset, length);
        position = pos + length;
        return this;
    }

    @Override
    public final ByteBuffer compact() {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        int pos = position();
        int lim = limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        System.arraycopy(hb, position + offset, hb, offset, remaining());
        position(rem);
        limit(capacity());
        discardMark();
        return this;
    }

    @Override
    public final boolean isDirect() {
        return true;
    }

    @Override
    public final boolean isReadOnly() {
        return isReadOnly;
    }

    // Used by java.nio.Bits
    @Override
    final byte _get(int i) {                          // package-private
        return get(i);
    }

    // Used by java.nio.Bits
    @Override
    final void _put(int i, byte b) {                  // package-private
        put(i, b);
    }

    @Override
    public final char getChar() {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        int newPosition = position + Character.BYTES;
        if (newPosition > limit()) {
            throw new BufferUnderflowException();
        }
        char x = (char) Memory.peekShort(ix(position), !nativeByteOrder);
        position = newPosition;
        return x;
    }

    @Override
    public final char getChar(int i) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        checkIndex(i, Character.BYTES);
        return (char) Memory.peekShort(ix(i), !nativeByteOrder);
    }

    @Override
    char getCharUnchecked(int i) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return (char) Memory.peekShort(ix(i), !nativeByteOrder);
    }

    @Override
    void getUnchecked(int pos, char[] dst, int dstOffset, int length) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        Memory.peekCharArray(ix(pos),
                dst, dstOffset, length, !nativeByteOrder);
    }

    private ByteBuffer putChar(long a, char x) {
        Memory.pokeShort(a, (short) x, !nativeByteOrder);
        return this;
    }

    @Override
    public final ByteBuffer putChar(char x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        putChar(ix(nextPutIndex(Character.BYTES)), x);
        return this;
    }

    @Override
    public final ByteBuffer putChar(int i, char x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        putChar(ix(checkIndex(i, Character.BYTES)), x);
        return this;
    }

    @Override
    void putCharUnchecked(int i, char x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        putChar(ix(i), x);
    }

    @Override
    void putUnchecked(int pos, char[] src, int srcOffset, int length) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        Memory.pokeCharArray(ix(pos),
                src, srcOffset, length, !nativeByteOrder);
    }

    @Override
    public final CharBuffer asCharBuffer() {
        if (memoryRef.isFreed) {
            throw new IllegalStateException("buffer has been freed");
        }
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);
        int size = rem >> 1;
        return new ByteBufferAsCharBuffer(this,
                -1,
                0,
                size,
                size,
                off,
                order());
    }

    private short getShort(long a) {
        return Memory.peekShort(a, !nativeByteOrder);
    }

    @Override
    public final short getShort() {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return getShort(ix(nextGetIndex(Short.BYTES)));
    }

    @Override
    public final short getShort(int i) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return getShort(ix(checkIndex(i, Short.BYTES)));
    }

    @Override
    short getShortUnchecked(int i) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return getShort(ix(i));
    }

    @Override
    void getUnchecked(int pos, short[] dst, int dstOffset, int length) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        Memory.peekShortArray(ix(pos),
                dst, dstOffset, length, !nativeByteOrder);
    }

    private ByteBuffer putShort(long a, short x) {
        Memory.pokeShort(a, x, !nativeByteOrder);
        return this;
    }

    @Override
    public final ByteBuffer putShort(short x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        putShort(ix(nextPutIndex(Short.BYTES)), x);
        return this;
    }

    @Override
    public final ByteBuffer putShort(int i, short x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        putShort(ix(checkIndex(i, Short.BYTES)), x);
        return this;
    }

    @Override
    void putShortUnchecked(int i, short x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        putShort(ix(i), x);
    }

    @Override
    void putUnchecked(int pos, short[] src, int srcOffset, int length) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        Memory.pokeShortArray(ix(pos),
                src, srcOffset, length, !nativeByteOrder);
    }

    @Override
    public final ShortBuffer asShortBuffer() {
        if (memoryRef.isFreed) {
            throw new IllegalStateException("buffer has been freed");
        }
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);
        int size = rem >> 1;
        return new ByteBufferAsShortBuffer(this,
                -1,
                0,
                size,
                size,
                off,
                order());
    }

    private int getInt(long a) {
        return Memory.peekInt(a, !nativeByteOrder);
    }

    @Override
    public int getInt() {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return getInt(ix(nextGetIndex(Integer.BYTES)));
    }

    @Override
    public int getInt(int i) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return getInt(ix(checkIndex(i, (Integer.BYTES))));
    }

    @Override
    final int getIntUnchecked(int i) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return getInt(ix(i));
    }

    @Override
    final void getUnchecked(int pos, int[] dst, int dstOffset, int length) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        Memory.peekIntArray(ix(pos),
                dst, dstOffset, length, !nativeByteOrder);
    }

    private ByteBuffer putInt(long a, int x) {
        Memory.pokeInt(a, x, !nativeByteOrder);
        return this;
    }

    @Override
    public final ByteBuffer putInt(int x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        putInt(ix(nextPutIndex(Integer.BYTES)), x);
        return this;
    }

    @Override
    public final ByteBuffer putInt(int i, int x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        putInt(ix(checkIndex(i, Integer.BYTES)), x);
        return this;
    }

    @Override
    final void putIntUnchecked(int i, int x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        putInt(ix(i), x);
    }

    @Override
    final void putUnchecked(int pos, int[] src, int srcOffset, int length) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        Memory.pokeIntArray(ix(pos),
                src, srcOffset, length, !nativeByteOrder);
    }

    @Override
    public final IntBuffer asIntBuffer() {
        if (memoryRef.isFreed) {
            throw new IllegalStateException("buffer has been freed");
        }
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);
        int size = rem >> 2;
        return new ByteBufferAsIntBuffer(this,
                -1,
                0,
                size,
                size,
                off,
                order());
    }

    private long getLong(long a) {
        return Memory.peekLong(a, !nativeByteOrder);
    }

    @Override
    public final long getLong() {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return getLong(ix(nextGetIndex(Long.BYTES)));
    }

    @Override
    public final long getLong(int i) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return getLong(ix(checkIndex(i, Long.BYTES)));
    }

    @Override
    final long getLongUnchecked(int i) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return getLong(ix(i));
    }

    @Override
    final void getUnchecked(int pos, long[] dst, int dstOffset, int length) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        Memory.peekLongArray(ix(pos),
                dst, dstOffset, length, !nativeByteOrder);
    }

    private ByteBuffer putLong(long a, long x) {
        Memory.pokeLong(a, x, !nativeByteOrder);
        return this;
    }

    @Override
    public final ByteBuffer putLong(long x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        putLong(ix(nextPutIndex(Long.BYTES)), x);
        return this;
    }

    @Override
    public final ByteBuffer putLong(int i, long x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        putLong(ix(checkIndex(i, Long.BYTES)), x);
        return this;
    }

    @Override
    final void putLongUnchecked(int i, long x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        putLong(ix(i), x);
    }

    @Override
    final void putUnchecked(int pos, long[] src, int srcOffset, int length) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        Memory.pokeLongArray(ix(pos),
                src, srcOffset, length, !nativeByteOrder);
    }

    @Override
    public final LongBuffer asLongBuffer() {
        if (memoryRef.isFreed) {
            throw new IllegalStateException("buffer has been freed");
        }
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);
        int size = rem >> 3;
        return new ByteBufferAsLongBuffer(this,
                -1,
                0,
                size,
                size,
                off,
                order());
    }

    private float getFloat(long a) {
        int x = Memory.peekInt(a, !nativeByteOrder);
        return Float.intBitsToFloat(x);
    }

    @Override
    public final float getFloat() {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return getFloat(ix(nextGetIndex(Float.BYTES)));
    }

    @Override
    public final float getFloat(int i) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return getFloat(ix(checkIndex(i, Float.BYTES)));
    }

    @Override
    final float getFloatUnchecked(int i) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return getFloat(ix(i));
    }

    @Override
    final void getUnchecked(int pos, float[] dst, int dstOffset, int length) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        Memory.peekFloatArray(ix(pos),
                dst, dstOffset, length, !nativeByteOrder);
    }

    private ByteBuffer putFloat(long a, float x) {
        int y = Float.floatToRawIntBits(x);
        Memory.pokeInt(a, y, !nativeByteOrder);
        return this;
    }

    @Override
    public final ByteBuffer putFloat(float x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        putFloat(ix(nextPutIndex(Float.BYTES)), x);
        return this;
    }

    @Override
    public final ByteBuffer putFloat(int i, float x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        putFloat(ix(checkIndex(i, Float.BYTES)), x);
        return this;
    }

    @Override
    final void putFloatUnchecked(int i, float x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        putFloat(ix(i), x);
    }

    @Override
    final void putUnchecked(int pos, float[] src, int srcOffset, int length) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        Memory.pokeFloatArray(ix(pos),
                src, srcOffset, length, !nativeByteOrder);
    }

    @Override
    public final FloatBuffer asFloatBuffer() {
        if (memoryRef.isFreed) {
            throw new IllegalStateException("buffer has been freed");
        }
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);
        int size = rem >> 2;
        return new ByteBufferAsFloatBuffer(this,
                -1,
                0,
                size,
                size,
                off,
                order());
    }

    private double getDouble(long a) {
        long x = Memory.peekLong(a, !nativeByteOrder);
        return Double.longBitsToDouble(x);
    }

    @Override
    public final double getDouble() {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return getDouble(ix(nextGetIndex(Double.BYTES)));
    }

    @Override
    public final double getDouble(int i) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return getDouble(ix(checkIndex(i, Double.BYTES)));
    }

    @Override
    final double getDoubleUnchecked(int i) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        return getDouble(ix(i));
    }

    @Override
    final void getUnchecked(int pos, double[] dst, int dstOffset, int length) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        Memory.peekDoubleArray(ix(pos),
                dst, dstOffset, length, !nativeByteOrder);
    }

    private ByteBuffer putDouble(long a, double x) {
        long y = Double.doubleToRawLongBits(x);
        Memory.pokeLong(a, y, !nativeByteOrder);
        return this;
    }

    @Override
    public final ByteBuffer putDouble(double x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        putDouble(ix(nextPutIndex(Double.BYTES)), x);
        return this;
    }

    @Override
    public final ByteBuffer putDouble(int i, double x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        putDouble(ix(checkIndex(i, Double.BYTES)), x);
        return this;
    }

    @Override
    final void putDoubleUnchecked(int i, double x) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        putDouble(ix(i), x);
    }

    @Override
    final void putUnchecked(int pos, double[] src, int srcOffset, int length) {
        if (!memoryRef.isAccessible) {
            throw new IllegalStateException("buffer is inaccessible");
        }
        Memory.pokeDoubleArray(ix(pos),
                src, srcOffset, length, !nativeByteOrder);
    }

    @Override
    public final DoubleBuffer asDoubleBuffer() {
        if (memoryRef.isFreed) {
            throw new IllegalStateException("buffer has been freed");
        }
        int off = this.position();
        int lim = this.limit();
        assert (off <= lim);
        int rem = (off <= lim ? lim - off : 0);

        int size = rem >> 3;
        return new ByteBufferAsDoubleBuffer(this,
                -1,
                0,
                size,
                size,
                off,
                order());
    }

    @Override
    public final boolean isAccessible() {
        return memoryRef.isAccessible;
    }

    @Override
    public final void setAccessible(boolean value) {
        memoryRef.isAccessible = value;
    }
}
