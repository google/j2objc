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
 * CharArrayBuffer implements char[]-based CharBuffers.
 */
final class CharArrayBuffer extends CharBuffer {

  private final char[] backingArray;

  private final int arrayOffset;

  private final boolean isReadOnly;

  CharArrayBuffer(char[] array) {
    this(array.length, array, 0, false);
  }

  private CharArrayBuffer(int capacity, char[] backingArray, int arrayOffset, boolean isReadOnly) {
    super(capacity, 0);
    this.backingArray = backingArray;
    this.arrayOffset = arrayOffset;
    this.isReadOnly = isReadOnly;
  }

  private static CharArrayBuffer copy(CharArrayBuffer other, int markOfOther, boolean isReadOnly) {
    CharArrayBuffer buf = new CharArrayBuffer(other.capacity(), other.backingArray, other.arrayOffset, isReadOnly);
    buf.limit = other.limit;
    buf.position = other.position();
    buf.mark = markOfOther;
    return buf;
  }

  @Override public CharBuffer asReadOnlyBuffer() {
    return copy(this, mark, true);
  }

  @Override public CharBuffer compact() {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    System.arraycopy(backingArray, position + arrayOffset, backingArray, arrayOffset, remaining());
    position = limit - position;
    limit = capacity;
    mark = UNSET_MARK;
    return this;
  }

  @Override public CharBuffer duplicate() {
    return copy(this, mark, isReadOnly);
  }

  @Override public CharBuffer slice() {
    return new CharArrayBuffer(remaining(), backingArray, arrayOffset + position, isReadOnly);
  }

  @Override public boolean isReadOnly() {
    return isReadOnly;
  }

  @Override char[] protectedArray() {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    return backingArray;
  }

  @Override int protectedArrayOffset() {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    return arrayOffset;
  }

  @Override boolean protectedHasArray() {
    if (isReadOnly) {
      return false;
    }
    return true;
  }

  @Override public final char get() {
    if (position == limit) {
      throw new BufferUnderflowException();
    }
    return backingArray[arrayOffset + position++];
  }

  @Override public final char get(int index) {
    checkIndex(index);
    return backingArray[arrayOffset + index];
  }

  @Override public final CharBuffer get(char[] dst, int srcOffset, int charCount) {
    if (charCount > remaining()) {
      throw new BufferUnderflowException();
    }
    System.arraycopy(backingArray, arrayOffset + position, dst, srcOffset, charCount);
    position += charCount;
    return this;
  }

  @Override public final boolean isDirect() {
    return false;
  }

  @Override public final ByteOrder order() {
    return ByteOrder.nativeOrder();
  }

  @Override public final CharBuffer subSequence(int start, int end) {
    checkStartEndRemaining(start, end);
    CharBuffer result = duplicate();
    result.limit(position + end);
    result.position(position + start);
    return result;
  }

  @Override public CharBuffer put(char c) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    if (position == limit) {
      throw new BufferOverflowException();
    }
    backingArray[arrayOffset + position++] = c;
    return this;
  }

  @Override public CharBuffer put(int index, char c) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    checkIndex(index);
    backingArray[arrayOffset + index] = c;
    return this;
  }

  @Override public CharBuffer put(char[] src, int srcOffset, int charCount) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    if (charCount > remaining()) {
      throw new BufferOverflowException();
    }
    System.arraycopy(src, srcOffset, backingArray, arrayOffset + position, charCount);
    position += charCount;
    return this;
  }

  @Override public final String toString() {
    return String.copyValueOf(backingArray, arrayOffset + position, remaining());
  }
}
