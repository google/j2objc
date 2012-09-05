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

package java.nio;

/**
 * ShortArrayBuffer, ReadWriteShortArrayBuffer and ReadOnlyShortArrayBuffer
 * compose the implementation of array based short buffers.
 * <p>
 * ReadOnlyShortArrayBuffer extends ShortArrayBuffer with all the write methods
 * throwing read only exception.
 * </p>
 * <p>
 * This class is marked final for runtime performance.
 * </p>
 * 
 */
final class ReadOnlyShortArrayBuffer extends ShortArrayBuffer {

    static ReadOnlyShortArrayBuffer copy(ShortArrayBuffer other, int markOfOther) {
        ReadOnlyShortArrayBuffer buf = new ReadOnlyShortArrayBuffer(other
                .capacity(), other.backingArray, other.offset);
        buf.limit = other.limit();
        buf.position = other.position();
        buf.mark = markOfOther;
        return buf;
    }

    ReadOnlyShortArrayBuffer(int capacity, short[] backingArray, int arrayOffset) {
        super(capacity, backingArray, arrayOffset);
    }

    @Override
    public ShortBuffer asReadOnlyBuffer() {
        return duplicate();
    }

    @Override
    public ShortBuffer compact() {
        throw new ReadOnlyBufferException();
    }

    @Override
    public ShortBuffer duplicate() {
        return copy(this, mark);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    protected short[] protectedArray() {
        throw new ReadOnlyBufferException();
    }

    @Override
    protected int protectedArrayOffset() {
        throw new ReadOnlyBufferException();
    }

    @Override
    protected boolean protectedHasArray() {
        return false;
    }

    @Override
    public ShortBuffer put(ShortBuffer buf) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public ShortBuffer put(short c) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public ShortBuffer put(int index, short c) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public final ShortBuffer put(short[] src, int off, int len) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public ShortBuffer slice() {
        return new ReadOnlyShortArrayBuffer(remaining(), backingArray, offset
                + position);
    }

}
