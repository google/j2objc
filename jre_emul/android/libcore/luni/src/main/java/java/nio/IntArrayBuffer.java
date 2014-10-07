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
 * IntArrayBuffer implements int[]-based IntBuffers.
 */
final class IntArrayBuffer extends IntBuffer {

  private final int[] backingArray;

  private final int arrayOffset;

  private final boolean isReadOnly;

  IntArrayBuffer(int[] array) {
    this(array.length, array, 0, false);
  }

  private IntArrayBuffer(int capacity, int[] backingArray, int arrayOffset, boolean isReadOnly) {
    super(capacity, 0);
    this.backingArray = backingArray;
    this.arrayOffset = arrayOffset;
    this.isReadOnly = isReadOnly;
  }

  private static IntArrayBuffer copy(IntArrayBuffer other, int markOfOther, boolean isReadOnly) {
    IntArrayBuffer buf = new IntArrayBuffer(other.capacity(), other.backingArray, other.arrayOffset, isReadOnly);
    buf.limit = other.limit;
    buf.position = other.position();
    buf.mark = markOfOther;
    return buf;
  }

  @Override public IntBuffer asReadOnlyBuffer() {
    return copy(this, mark, true);
  }

  @Override public IntBuffer compact() {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    System.arraycopy(backingArray, position + arrayOffset, backingArray, arrayOffset, remaining());
    position = limit - position;
    limit = capacity;
    mark = UNSET_MARK;
    return this;
  }

  @Override public IntBuffer duplicate() {
    return copy(this, mark, isReadOnly);
  }

  @Override public IntBuffer slice() {
    return new IntArrayBuffer(remaining(), backingArray, arrayOffset + position, isReadOnly);
  }

  @Override public boolean isReadOnly() {
    return isReadOnly;
  }

  @Override int[] protectedArray() {
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

  @Override public final int get() {
    if (position == limit) {
      throw new BufferUnderflowException();
    }
    return backingArray[arrayOffset + position++];
  }

  @Override public final int get(int index) {
    checkIndex(index);
    return backingArray[arrayOffset + index];
  }

  @Override public final IntBuffer get(int[] dst, int dstOffset, int intCount) {
    if (intCount > remaining()) {
      throw new BufferUnderflowException();
    }
    System.arraycopy(backingArray, arrayOffset + position, dst, dstOffset, intCount);
    position += intCount;
    return this;
  }

  @Override public final boolean isDirect() {
    return false;
  }

  @Override public final ByteOrder order() {
    return ByteOrder.nativeOrder();
  }

  @Override public IntBuffer put(int c) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    if (position == limit) {
      throw new BufferOverflowException();
    }
    backingArray[arrayOffset + position++] = c;
    return this;
  }

  @Override public IntBuffer put(int index, int c) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    checkIndex(index);
    backingArray[arrayOffset + index] = c;
    return this;
  }

  @Override public IntBuffer put(int[] src, int srcOffset, int intCount) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    if (intCount > remaining()) {
      throw new BufferOverflowException();
    }
    System.arraycopy(src, srcOffset, backingArray, arrayOffset + position, intCount);
    position += intCount;
    return this;
  }
}
