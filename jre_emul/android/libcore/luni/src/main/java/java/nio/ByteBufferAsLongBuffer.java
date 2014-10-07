/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.nio;

import libcore.io.SizeOf;

/**
 * This class wraps a byte buffer to be a long buffer.
 * <p>
 * Implementation notice:
 * <ul>
 * <li>After a byte buffer instance is wrapped, it becomes privately owned by
 * the adapter. It must NOT be accessed outside the adapter any more.</li>
 * <li>The byte buffer's position and limit are NOT linked with the adapter.
 * The adapter extends Buffer, thus has its own position and limit.</li>
 * </ul>
 * </p>
 *
 */
final class ByteBufferAsLongBuffer extends LongBuffer {

    private final ByteBuffer byteBuffer;

    static LongBuffer asLongBuffer(ByteBuffer byteBuffer) {
        ByteBuffer slice = byteBuffer.slice();
        slice.order(byteBuffer.order());
        return new ByteBufferAsLongBuffer(slice);
    }

    private ByteBufferAsLongBuffer(ByteBuffer byteBuffer) {
        super(byteBuffer.capacity() / SizeOf.LONG, byteBuffer.effectiveDirectAddress);
        this.byteBuffer = byteBuffer;
        this.byteBuffer.clear();
    }

    @Override
    public LongBuffer asReadOnlyBuffer() {
        ByteBufferAsLongBuffer buf = new ByteBufferAsLongBuffer(byteBuffer.asReadOnlyBuffer());
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        buf.byteBuffer.order = byteBuffer.order;
        return buf;
    }

    @Override
    public LongBuffer compact() {
        if (byteBuffer.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        byteBuffer.limit(limit * SizeOf.LONG);
        byteBuffer.position(position * SizeOf.LONG);
        byteBuffer.compact();
        byteBuffer.clear();
        position = limit - position;
        limit = capacity;
        mark = UNSET_MARK;
        return this;
    }

    @Override
    public LongBuffer duplicate() {
        ByteBuffer bb = byteBuffer.duplicate().order(byteBuffer.order());
        ByteBufferAsLongBuffer buf = new ByteBufferAsLongBuffer(bb);
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        return buf;
    }

    @Override
    public long get() {
        if (position == limit) {
            throw new BufferUnderflowException();
        }
        return byteBuffer.getLong(position++ * SizeOf.LONG);
    }

    @Override
    public long get(int index) {
        checkIndex(index);
        return byteBuffer.getLong(index * SizeOf.LONG);
    }

    @Override
    public LongBuffer get(long[] dst, int dstOffset, int longCount) {
        byteBuffer.limit(limit * SizeOf.LONG);
        byteBuffer.position(position * SizeOf.LONG);
        if (byteBuffer instanceof DirectByteBuffer) {
            ((DirectByteBuffer) byteBuffer).get(dst, dstOffset, longCount);
        } else {
            ((ByteArrayBuffer) byteBuffer).get(dst, dstOffset, longCount);
        }
        this.position += longCount;
        return this;
    }

    @Override
    public boolean isDirect() {
        return byteBuffer.isDirect();
    }

    @Override
    public boolean isReadOnly() {
        return byteBuffer.isReadOnly();
    }

    @Override
    public ByteOrder order() {
        return byteBuffer.order();
    }

    @Override long[] protectedArray() {
        throw new UnsupportedOperationException();
    }

    @Override int protectedArrayOffset() {
        throw new UnsupportedOperationException();
    }

    @Override boolean protectedHasArray() {
        return false;
    }

    @Override
    public LongBuffer put(long c) {
        if (position == limit) {
            throw new BufferOverflowException();
        }
        byteBuffer.putLong(position++ * SizeOf.LONG, c);
        return this;
    }

    @Override
    public LongBuffer put(int index, long c) {
        checkIndex(index);
        byteBuffer.putLong(index * SizeOf.LONG, c);
        return this;
    }

    @Override
    public LongBuffer put(long[] src, int srcOffset, int longCount) {
        byteBuffer.limit(limit * SizeOf.LONG);
        byteBuffer.position(position * SizeOf.LONG);
        if (byteBuffer instanceof DirectByteBuffer) {
            ((DirectByteBuffer) byteBuffer).put(src, srcOffset, longCount);
        } else {
            ((ByteArrayBuffer) byteBuffer).put(src, srcOffset, longCount);
        }
        this.position += longCount;
        return this;
    }

    @Override
    public LongBuffer slice() {
        byteBuffer.limit(limit * SizeOf.LONG);
        byteBuffer.position(position * SizeOf.LONG);
        ByteBuffer bb = byteBuffer.slice().order(byteBuffer.order());
        LongBuffer result = new ByteBufferAsLongBuffer(bb);
        byteBuffer.clear();
        return result;
    }

}
