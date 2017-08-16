/*
 * Copyright (C) 2014 The Android Open Source Project
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

package java.nio;

/**
 * A read/write HeapDoubleBuffer.
 */

class HeapDoubleBuffer extends DoubleBuffer {

    // For speed these fields are actually declared in X-Buffer;
    // these declarations are here as documentation
    /*

      protected final double[] hb;
      protected final int offset;

    */

    HeapDoubleBuffer(int cap, int lim) {            // package-private
        this(cap, lim, false);
    }

    HeapDoubleBuffer(double[] buf, int off, int len) { // package-private
        this(buf, off, len, false);
    }

    protected HeapDoubleBuffer(double[] buf,
                               int mark, int pos, int lim, int cap,
                               int off) {
        this(buf, mark, pos, lim, cap, off, false);
    }

    HeapDoubleBuffer(int cap, int lim, boolean isReadOnly) {            // package-private
        super(-1, 0, lim, cap, new double[cap], 0);
        this.isReadOnly = isReadOnly;
    }

    HeapDoubleBuffer(double[] buf, int off, int len, boolean isReadOnly) { // package-private
        super(-1, off, off + len, buf.length, buf, 0);
        this.isReadOnly = isReadOnly;
    }

    protected HeapDoubleBuffer(double[] buf,
                               int mark, int pos, int lim, int cap,
                               int off, boolean isReadOnly) {
        super(mark, pos, lim, cap, buf, off);
        this.isReadOnly = isReadOnly;
    }


    public DoubleBuffer slice() {
        return new HeapDoubleBuffer(hb,
                                    -1,
                                    0,
                                    this.remaining(),
                                    this.remaining(),
                                    this.position() + offset,
                                    isReadOnly);
    }

    public DoubleBuffer duplicate() {
        return new HeapDoubleBuffer(hb,
                                    this.markValue(),
                                    this.position(),
                                    this.limit(),
                                    this.capacity(),
                                    offset,
                                    isReadOnly);
    }

    public DoubleBuffer asReadOnlyBuffer() {
        return new HeapDoubleBuffer(hb,
                                     this.markValue(),
                                     this.position(),
                                     this.limit(),
                                     this.capacity(),
                                     offset, true);
    }

    protected int ix(int i) {
        return i + offset;
    }

    public double get() {
        return hb[ix(nextGetIndex())];
    }

    public double get(int i) {
        return hb[ix(checkIndex(i))];
    }

    public DoubleBuffer get(double[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        if (length > remaining())
            throw new BufferUnderflowException();
        System.arraycopy(hb, ix(position()), dst, offset, length);
        position(position() + length);
        return this;
    }

    public boolean isDirect() {
        return false;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public DoubleBuffer put(double x) {
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        hb[ix(nextPutIndex())] = x;
        return this;
    }

    public DoubleBuffer put(int i, double x) {
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        hb[ix(checkIndex(i))] = x;
        return this;
    }

    public DoubleBuffer put(double[] src, int offset, int length) {
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        checkBounds(offset, length, src.length);
        if (length > remaining())
            throw new BufferOverflowException();
        System.arraycopy(src, offset, hb, ix(position()), length);
        position(position() + length);
        return this;
    }

    public DoubleBuffer put(DoubleBuffer src) {
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        if (src instanceof HeapDoubleBuffer) {
            if (src == this)
                throw new IllegalArgumentException();
            HeapDoubleBuffer sb = (HeapDoubleBuffer)src;
            int n = sb.remaining();
            if (n > remaining())
                throw new BufferOverflowException();
            System.arraycopy(sb.hb, sb.ix(sb.position()),
                             hb, ix(position()), n);
            sb.position(sb.position() + n);
            position(position() + n);
        } else if (src.isDirect()) {
            int n = src.remaining();
            if (n > remaining())
                throw new BufferOverflowException();
            src.get(hb, ix(position()), n);
            position(position() + n);
        } else {
            super.put(src);
        }
        return this;
    }

    public DoubleBuffer compact() {
        if (isReadOnly) {
            throw new ReadOnlyBufferException();
        }
        System.arraycopy(hb, ix(position()), hb, ix(0), remaining());
        position(remaining());
        limit(capacity());
        discardMark();
        return this;
    }

    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }
}
