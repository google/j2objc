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

import java.util.Arrays;

/**
 * This class wraps a char sequence to be a char buffer.
 * <p>
 * Implementation notice:
 * <ul>
 * <li>Char sequence based buffer is always readonly.</li>
 * </ul>
 * </p>
 *
 */
final class CharSequenceAdapter extends CharBuffer {

    static CharSequenceAdapter copy(CharSequenceAdapter other) {
        CharSequenceAdapter buf = new CharSequenceAdapter(other.sequence);
        buf.limit = other.limit;
        buf.position = other.position;
        buf.mark = other.mark;
        return buf;
    }

    final CharSequence sequence;

    CharSequenceAdapter(CharSequence chseq) {
        super(chseq.length(), 0);
        sequence = chseq;
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
        return copy(this);
    }

    @Override
    public char get() {
        if (position == limit) {
            throw new BufferUnderflowException();
        }
        return sequence.charAt(position++);
    }

    @Override
    public char get(int index) {
        checkIndex(index);
        return sequence.charAt(index);
    }

    @Override
    public final CharBuffer get(char[] dst, int dstOffset, int charCount) {
        Arrays.checkOffsetAndCount(dst.length, dstOffset, charCount);
        if (charCount > remaining()) {
            throw new BufferUnderflowException();
        }
        int newPosition = position + charCount;
        sequence.toString().getChars(position, newPosition, dst, dstOffset);
        position = newPosition;
        return this;
    }

    @Override
    public boolean isDirect() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public ByteOrder order() {
        return ByteOrder.nativeOrder();
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
        throw new ReadOnlyBufferException();
    }

    @Override
    public CharBuffer put(int index, char c) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public final CharBuffer put(char[] src, int srcOffset, int charCount) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public CharBuffer put(String src, int start, int end) {
        throw new ReadOnlyBufferException();
    }

    @Override
    public CharBuffer slice() {
        return new CharSequenceAdapter(sequence.subSequence(position, limit));
    }

    @Override public CharBuffer subSequence(int start, int end) {
        checkStartEndRemaining(start, end);
        CharSequenceAdapter result = copy(this);
        result.position = position + start;
        result.limit = position + end;
        return result;
    }
}
