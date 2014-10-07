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
 * DoubleArrayBuffer implements double[]-based DoubleBuffers.
 */
final class DoubleArrayBuffer extends DoubleBuffer {

  private final double[] backingArray;

  private final int arrayOffset;

  private final boolean isReadOnly;

  DoubleArrayBuffer(double[] array) {
    this(array.length, array, 0, false);
  }

  private DoubleArrayBuffer(int capacity, double[] backingArray, int arrayOffset, boolean isReadOnly) {
    super(capacity, 0);
    this.backingArray = backingArray;
    this.arrayOffset = arrayOffset;
    this.isReadOnly = isReadOnly;
  }

  private static DoubleArrayBuffer copy(DoubleArrayBuffer other, int markOfOther, boolean isReadOnly) {
    DoubleArrayBuffer buf = new DoubleArrayBuffer(other.capacity(), other.backingArray, other.arrayOffset, isReadOnly);
    buf.limit = other.limit;
    buf.position = other.position();
    buf.mark = markOfOther;
    return buf;
  }

  @Override public DoubleBuffer asReadOnlyBuffer() {
    return copy(this, mark, true);
  }

  @Override public DoubleBuffer compact() {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    System.arraycopy(backingArray, position + arrayOffset, backingArray, arrayOffset, remaining());
    position = limit - position;
    limit = capacity;
    mark = UNSET_MARK;
    return this;
  }

  @Override public DoubleBuffer duplicate() {
    return copy(this, mark, isReadOnly);
  }

  @Override public DoubleBuffer slice() {
    return new DoubleArrayBuffer(remaining(), backingArray, arrayOffset + position, isReadOnly);
  }

  @Override public boolean isReadOnly() {
    return isReadOnly;
  }

  @Override double[] protectedArray() {
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

  @Override public final double get() {
    if (position == limit) {
      throw new BufferUnderflowException();
    }
    return backingArray[arrayOffset + position++];
  }

  @Override public final double get(int index) {
    checkIndex(index);
    return backingArray[arrayOffset + index];
  }

  @Override public final DoubleBuffer get(double[] dst, int dstOffset, int doubleCount) {
    if (doubleCount > remaining()) {
      throw new BufferUnderflowException();
    }
    System.arraycopy(backingArray, arrayOffset + position, dst, dstOffset, doubleCount);
    position += doubleCount;
    return this;
  }

  @Override public final boolean isDirect() {
    return false;
  }

  @Override public final ByteOrder order() {
    return ByteOrder.nativeOrder();
  }

  @Override public DoubleBuffer put(double c) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    if (position == limit) {
      throw new BufferOverflowException();
    }
    backingArray[arrayOffset + position++] = c;
    return this;
  }

  @Override public DoubleBuffer put(int index, double c) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    checkIndex(index);
    backingArray[arrayOffset + index] = c;
    return this;
  }

  @Override public DoubleBuffer put(double[] src, int srcOffset, int doubleCount) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    if (doubleCount > remaining()) {
      throw new BufferOverflowException();
    }
    System.arraycopy(src, srcOffset, backingArray, arrayOffset + position, doubleCount);
    position += doubleCount;
    return this;
  }
}
