/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

import libcore.io.Memory;

class ByteBufferAsCharBuffer extends CharBuffer {      // package-private

    protected final ByteBuffer bb;
    protected final int offset;
    private final ByteOrder order;

    ByteBufferAsCharBuffer(ByteBuffer bb,
                           int mark, int pos, int lim, int cap,
                           int off, ByteOrder order) {
        super(mark, pos, lim, cap);
        this.bb = bb;
        this.isReadOnly = bb.isReadOnly;
        // There are only two possibilities for the type of ByteBuffer "bb", viz, DirectByteBuffer and
        // HeapByteBuffer. We only have to initialize the field when bb is an instance of
        // DirectByteBuffer.
        // The address field is used by NIOAccess#getBasePointer and GetDirectBufferAddress method
        // in art which return the address of the first usable byte of the underlying memory, i.e,
        // the position of parent buffer. Therefore, value of "off" will be equal to parent buffer's
        // position when the method is called from either HeapByteBuffer or DirectByteBuffer.
        if (bb instanceof DirectByteBuffer) {
            this.address = bb.address + off;
        }
        this.order = order;
        offset = off;
    }

    public CharBuffer slice() {
        int pos = this.position();
        int lim = this.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        int off = (pos << 1) + offset;
        assert (off >= 0);
        return new ByteBufferAsCharBuffer(bb, -1, 0, rem, rem, off, order);
    }

    public CharBuffer duplicate() {
        return new ByteBufferAsCharBuffer(bb,
                markValue(),
                position(),
                limit(),
                capacity(),
                offset,
                order);
    }

    public CharBuffer asReadOnlyBuffer() {
        return new ByteBufferAsCharBuffer(bb.asReadOnlyBuffer(),
                markValue(),
                position(),
                limit(),
                capacity(),
                offset,
                order);
    }

    protected int ix(int i) {
        return (i << 1) + offset;
    }

    public char get() {
        return get(nextGetIndex());
    }

    public char get(int i) {
        return bb.getCharUnchecked(ix(checkIndex(i)));
    }

    public CharBuffer get(char[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        if (length > remaining())
            throw new BufferUnderflowException();
        bb.getUnchecked(ix(position), dst, offset, length);
        position += length;
        return this;
    }

    char getUnchecked(int i) {
        return bb.getCharUnchecked(ix(i));
    }

    public CharBuffer put(char x) {
        put(nextPutIndex(), x);
        return this;
    }

    public CharBuffer put(int i, char x) {
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        bb.putCharUnchecked(ix(checkIndex(i)), x);
        return this;
    }

    public CharBuffer put(char[] src, int offset, int length) {
        checkBounds(offset, length, src.length);
        if (length > remaining())
            throw new BufferOverflowException();
        bb.putUnchecked(ix(position), src, offset, length);
        position += length;
        return this;
    }

    public CharBuffer compact() {
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        int pos = position();
        int lim = limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);
        if (!(bb instanceof DirectByteBuffer)) {
            System.arraycopy(bb.array(), ix(pos), bb.array(), ix(0), rem << 1);
        } else {
            Memory.memmove(this, ix(0), this, ix(pos), rem << 1);
        }
        position(rem);
        limit(capacity());
        discardMark();
        return this;
    }

    public boolean isDirect() {
        return bb.isDirect();
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public String toString(int start, int end) {
        if ((end > limit()) || (start > end))
            throw new IndexOutOfBoundsException();
        try {
            int len = end - start;
            char[] ca = new char[len];
            CharBuffer cb = CharBuffer.wrap(ca);
            CharBuffer db = this.duplicate();
            db.position(start);
            db.limit(end);
            cb.put(db);
            return new String(ca);
        } catch (StringIndexOutOfBoundsException x) {
            throw new IndexOutOfBoundsException();
        }
    }

    // --- Methods to support CharSequence ---

    public CharBuffer subSequence(int start, int end) {
        int pos = position();
        int lim = limit();
        assert (pos <= lim);
        pos = (pos <= lim ? pos : lim);
        int len = lim - pos;

        if ((start < 0) || (end > len) || (start > end))
            throw new IndexOutOfBoundsException();
        return new ByteBufferAsCharBuffer(bb,
                -1,
                pos + start,
                pos + end,
                capacity(),
                offset,
                order);
    }

    public ByteOrder order() {
        return order;
    }
}
