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
 * This class wraps a byte buffer to be a char buffer.
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
final class ByteBufferAsCharBuffer extends CharBuffer {

    private final ByteBuffer byteBuffer;

    static CharBuffer asCharBuffer(ByteBuffer byteBuffer) {
        ByteBuffer slice = byteBuffer.slice();
        slice.order(byteBuffer.order());
        return new ByteBufferAsCharBuffer(slice);
    }

    private ByteBufferAsCharBuffer(ByteBuffer byteBuffer) {
        super(byteBuffer.capacity() / SizeOf.CHAR, byteBuffer.effectiveDirectAddress);
        this.byteBuffer = byteBuffer;
        this.byteBuffer.clear();
    }

    @Override
    public CharBuffer asReadOnlyBuffer() {
        ByteBufferAsCharBuffer buf = new ByteBufferAsCharBuffer(byteBuffer.asReadOnlyBuffer());
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        buf.byteBuffer.order = byteBuffer.order;
        return buf;
    }

    @Override
    public CharBuffer compact() {
        if (byteBuffer.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        byteBuffer.limit(limit * SizeOf.CHAR);
        byteBuffer.position(position * SizeOf.CHAR);
        byteBuffer.compact();
        byteBuffer.clear();
        position = limit - position;
        limit = capacity;
        mark = UNSET_MARK;
        return this;
    }

    @Override
    public CharBuffer duplicate() {
        ByteBuffer bb = byteBuffer.duplicate().order(byteBuffer.order());
        ByteBufferAsCharBuffer buf = new ByteBufferAsCharBuffer(bb);
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        return buf;
    }

    @Override
    public char get() {
        if (position == limit) {
            throw new BufferUnderflowException();
        }
        return byteBuffer.getChar(position++ * SizeOf.CHAR);
    }

    @Override
    public char get(int index) {
        checkIndex(index);
        return byteBuffer.getChar(index * SizeOf.CHAR);
    }

    @Override
    public CharBuffer get(char[] dst, int dstOffset, int charCount) {
        byteBuffer.limit(limit * SizeOf.CHAR);
        byteBuffer.position(position * SizeOf.CHAR);
        if (byteBuffer instanceof DirectByteBuffer) {
            ((DirectByteBuffer) byteBuffer).get(dst, dstOffset, charCount);
        } else {
            ((ByteArrayBuffer) byteBuffer).get(dst, dstOffset, charCount);
        }
        this.position += charCount;
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

    @Override char[] protectedArray() {
        throw new UnsupportedOperationException();
    }

    @Override int protectedArrayOffset() {
        throw new UnsupportedOperationException();
    }

    @Override boolean protectedHasArray() {
        return false;
    }

    @Override
    public CharBuffer put(char c) {
        if (position == limit) {
            throw new BufferOverflowException();
        }
        byteBuffer.putChar(position++ * SizeOf.CHAR, c);
        return this;
    }

    @Override
    public CharBuffer put(int index, char c) {
        checkIndex(index);
        byteBuffer.putChar(index * SizeOf.CHAR, c);
        return this;
    }

    @Override
    public CharBuffer put(char[] src, int srcOffset, int charCount) {
        byteBuffer.limit(limit * SizeOf.CHAR);
        byteBuffer.position(position * SizeOf.CHAR);
        if (byteBuffer instanceof DirectByteBuffer) {
            ((DirectByteBuffer) byteBuffer).put(src, srcOffset, charCount);
        } else {
            ((ByteArrayBuffer) byteBuffer).put(src, srcOffset, charCount);
        }
        this.position += charCount;
        return this;
    }

    @Override
    public CharBuffer slice() {
        byteBuffer.limit(limit * SizeOf.CHAR);
        byteBuffer.position(position * SizeOf.CHAR);
        ByteBuffer bb = byteBuffer.slice().order(byteBuffer.order());
        CharBuffer result = new ByteBufferAsCharBuffer(bb);
        byteBuffer.clear();
        return result;
    }

    @Override public CharBuffer subSequence(int start, int end) {
        checkStartEndRemaining(start, end);
        CharBuffer result = duplicate();
        result.limit(position + end);
        result.position(position + start);
        return result;
    }
}
