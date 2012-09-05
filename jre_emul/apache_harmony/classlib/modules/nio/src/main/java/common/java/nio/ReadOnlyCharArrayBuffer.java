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
 * CharArrayBuffer, ReadWriteCharArrayBuffer and ReadOnlyCharArrayBuffer compose
 * the implementation of array based char buffers.
 * <p>
 * ReadOnlyCharArrayBuffer extends CharArrayBuffer with all the write methods
 * throwing read only exception.
 * </p>
 * <p>
 * This class is marked final for runtime performance.
 * </p>
 * 
 */
final class ReadOnlyCharArrayBuffer extends CharArrayBuffer {

    static ReadOnlyCharArrayBuffer copy(CharArrayBuffer other, int markOfOther) {
        ReadOnlyCharArrayBuffer buf = new ReadOnlyCharArrayBuffer(other
                .capacity(), other.backingArray, other.offset);
        buf.limit = other.limit();
        buf.position = other.position();
        buf.mark = markOfOther;
        return buf;
    }

    ReadOnlyCharArrayBuffer(int capacity, char[] backingArray, int arrayOffset) {
        super(capacity, backingArray, arrayOffset);
    }

    @Override
    public CharBuffer asReadOnlyBuffer() {
        return duplicate();
    }

    @Override
    public CharBuffer compact() {
        throw new ReadOnlyBufferException();
    }

    @Override
    public CharBuffer duplicate() {
        return copy(this, mark);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    protected char[] protectedArray() {
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
    public CharBuffer put(char c) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public CharBuffer put(int index, char c) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public final CharBuffer put(char[] src, int off, int len) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public final CharBuffer put(CharBuffer src) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public CharBuffer put(String src, int start, int end) {
        if ((start < 0) || (end < 0)
                || (long) start + (long) end > src.length()) {
            throw new IndexOutOfBoundsException();
        }
        throw new ReadOnlyBufferException();
    }

    @Override
    public CharBuffer slice() {
        return new ReadOnlyCharArrayBuffer(remaining(), backingArray, offset
                + position);
    }
}
