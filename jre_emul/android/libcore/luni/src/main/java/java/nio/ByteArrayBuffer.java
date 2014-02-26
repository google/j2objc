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

import libcore.io.SizeOf;
import libcore.io.Memory;

/**
 * ByteArrayBuffer implements byte[]-backed ByteBuffers.
 */
final class ByteArrayBuffer extends ByteBuffer {

  /**
   * These fields are non-private for NioUtils.unsafeArray.
   */
  final byte[] backingArray;
  final int arrayOffset;

  private final boolean isReadOnly;

  ByteArrayBuffer(byte[] backingArray) {
    this(backingArray.length, backingArray, 0, false);
  }

  private ByteArrayBuffer(int capacity, byte[] backingArray, int arrayOffset, boolean isReadOnly) {
    super(capacity, 0);
    this.backingArray = backingArray;
    this.arrayOffset = arrayOffset;
    this.isReadOnly = isReadOnly;
    if (arrayOffset + capacity > backingArray.length) {
      throw new IndexOutOfBoundsException("backingArray.length=" + backingArray.length +
                                              ", capacity=" + capacity + ", arrayOffset=" + arrayOffset);
    }
  }

  private static ByteArrayBuffer copy(ByteArrayBuffer other, int markOfOther, boolean isReadOnly) {
    ByteArrayBuffer buf = new ByteArrayBuffer(other.capacity(), other.backingArray, other.arrayOffset, isReadOnly);
    buf.limit = other.limit;
    buf.position = other.position();
    buf.mark = markOfOther;
    return buf;
  }

  @Override public ByteBuffer asReadOnlyBuffer() {
    return copy(this, mark, true);
  }

  @Override public ByteBuffer compact() {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    System.arraycopy(backingArray, position + arrayOffset, backingArray, arrayOffset, remaining());
    position = limit - position;
    limit = capacity;
    mark = UNSET_MARK;
    return this;
  }

  @Override public ByteBuffer duplicate() {
    return copy(this, mark, isReadOnly);
  }

  @Override public ByteBuffer slice() {
    return new ByteArrayBuffer(remaining(), backingArray, arrayOffset + position, isReadOnly);
  }

  @Override public boolean isReadOnly() {
    return isReadOnly;
  }

  @Override byte[] protectedArray() {
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

  @Override public final ByteBuffer get(byte[] dst, int dstOffset, int byteCount) {
    checkGetBounds(1, dst.length, dstOffset, byteCount);
    System.arraycopy(backingArray, arrayOffset + position, dst, dstOffset, byteCount);
    position += byteCount;
    return this;
  }

  final void get(char[] dst, int dstOffset, int charCount) {
    int byteCount = checkGetBounds(SizeOf.CHAR, dst.length, dstOffset, charCount);
    Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.CHAR, order.needsSwap);
    position += byteCount;
  }

  final void get(double[] dst, int dstOffset, int doubleCount) {
    int byteCount = checkGetBounds(SizeOf.DOUBLE, dst.length, dstOffset, doubleCount);
    Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.DOUBLE, order.needsSwap);
    position += byteCount;
  }

  final void get(float[] dst, int dstOffset, int floatCount) {
    int byteCount = checkGetBounds(SizeOf.FLOAT, dst.length, dstOffset, floatCount);
    Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.FLOAT, order.needsSwap);
    position += byteCount;
  }

  final void get(int[] dst, int dstOffset, int intCount) {
    int byteCount = checkGetBounds(SizeOf.INT, dst.length, dstOffset, intCount);
    Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.INT, order.needsSwap);
    position += byteCount;
  }

  final void get(long[] dst, int dstOffset, int longCount) {
    int byteCount = checkGetBounds(SizeOf.LONG, dst.length, dstOffset, longCount);
    Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.LONG, order.needsSwap);
    position += byteCount;
  }

  final void get(short[] dst, int dstOffset, int shortCount) {
    int byteCount = checkGetBounds(SizeOf.SHORT, dst.length, dstOffset, shortCount);
    Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.SHORT, order.needsSwap);
    position += byteCount;
  }

  @Override public final byte get() {
    if (position == limit) {
      throw new BufferUnderflowException();
    }
    return backingArray[arrayOffset + position++];
  }

  @Override public final byte get(int index) {
    checkIndex(index);
    return backingArray[arrayOffset + index];
  }

  @Override public final char getChar() {
    int newPosition = position + SizeOf.CHAR;
    if (newPosition > limit) {
      throw new BufferUnderflowException();
    }
    char result = (char) Memory.peekShort(backingArray, arrayOffset + position, order);
    position = newPosition;
    return result;
  }

  @Override public final char getChar(int index) {
    checkIndex(index, SizeOf.CHAR);
    return (char) Memory.peekShort(backingArray, arrayOffset + index, order);
  }

  @Override public final double getDouble() {
    return Double.longBitsToDouble(getLong());
  }

  @Override public final double getDouble(int index) {
    return Double.longBitsToDouble(getLong(index));
  }

  @Override public final float getFloat() {
    return Float.intBitsToFloat(getInt());
  }

  @Override public final float getFloat(int index) {
    return Float.intBitsToFloat(getInt(index));
  }

  @Override public final int getInt() {
    int newPosition = position + SizeOf.INT;
    if (newPosition > limit) {
      throw new BufferUnderflowException();
    }
    int result = Memory.peekInt(backingArray, arrayOffset + position, order);
    position = newPosition;
    return result;
  }

  @Override public final int getInt(int index) {
    checkIndex(index, SizeOf.INT);
    return Memory.peekInt(backingArray, arrayOffset + index, order);
  }

  @Override public final long getLong() {
    int newPosition = position + SizeOf.LONG;
    if (newPosition > limit) {
      throw new BufferUnderflowException();
    }
    long result = Memory.peekLong(backingArray, arrayOffset + position, order);
    position = newPosition;
    return result;
  }

  @Override public final long getLong(int index) {
    checkIndex(index, SizeOf.LONG);
    return Memory.peekLong(backingArray, arrayOffset + index, order);
  }

  @Override public final short getShort() {
    int newPosition = position + SizeOf.SHORT;
    if (newPosition > limit) {
      throw new BufferUnderflowException();
    }
    short result = Memory.peekShort(backingArray, arrayOffset + position, order);
    position = newPosition;
    return result;
  }

  @Override public final short getShort(int index) {
    checkIndex(index, SizeOf.SHORT);
    return Memory.peekShort(backingArray, arrayOffset + index, order);
  }

  @Override public final boolean isDirect() {
    return false;
  }

  @Override public ByteBuffer put(byte b) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    if (position == limit) {
      throw new BufferOverflowException();
    }
    backingArray[arrayOffset + position++] = b;
    return this;
  }

  @Override public ByteBuffer put(int index, byte b) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    checkIndex(index);
    backingArray[arrayOffset + index] = b;
    return this;
  }

  @Override public ByteBuffer put(byte[] src, int srcOffset, int byteCount) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    checkPutBounds(1, src.length, srcOffset, byteCount);
    System.arraycopy(src, srcOffset, backingArray, arrayOffset + position, byteCount);
    position += byteCount;
    return this;
  }

  final void put(char[] src, int srcOffset, int charCount) {
    int byteCount = checkPutBounds(SizeOf.CHAR, src.length, srcOffset, charCount);
    Memory.unsafeBulkPut(backingArray, arrayOffset + position, byteCount, src, srcOffset, SizeOf.CHAR, order.needsSwap);
    position += byteCount;
  }

  final void put(double[] src, int srcOffset, int doubleCount) {
    int byteCount = checkPutBounds(SizeOf.DOUBLE, src.length, srcOffset, doubleCount);
    Memory.unsafeBulkPut(backingArray, arrayOffset + position, byteCount, src, srcOffset, SizeOf.DOUBLE, order.needsSwap);
    position += byteCount;
  }

  final void put(float[] src, int srcOffset, int floatCount) {
    int byteCount = checkPutBounds(SizeOf.FLOAT, src.length, srcOffset, floatCount);
    Memory.unsafeBulkPut(backingArray, arrayOffset + position, byteCount, src, srcOffset, SizeOf.FLOAT, order.needsSwap);
    position += byteCount;
  }

  final void put(int[] src, int srcOffset, int intCount) {
    int byteCount = checkPutBounds(SizeOf.INT, src.length, srcOffset, intCount);
    Memory.unsafeBulkPut(backingArray, arrayOffset + position, byteCount, src, srcOffset, SizeOf.INT, order.needsSwap);
    position += byteCount;
  }

  final void put(long[] src, int srcOffset, int longCount) {
    int byteCount = checkPutBounds(SizeOf.LONG, src.length, srcOffset, longCount);
    Memory.unsafeBulkPut(backingArray, arrayOffset + position, byteCount, src, srcOffset, SizeOf.LONG, order.needsSwap);
    position += byteCount;
  }

  final void put(short[] src, int srcOffset, int shortCount) {
    int byteCount = checkPutBounds(SizeOf.SHORT, src.length, srcOffset, shortCount);
    Memory.unsafeBulkPut(backingArray, arrayOffset + position, byteCount, src, srcOffset, SizeOf.SHORT, order.needsSwap);
    position += byteCount;
  }

  @Override public ByteBuffer putChar(int index, char value) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    checkIndex(index, SizeOf.CHAR);
    Memory.pokeShort(backingArray, arrayOffset + index, (short) value, order);
    return this;
  }

  @Override public ByteBuffer putChar(char value) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    int newPosition = position + SizeOf.CHAR;
    if (newPosition > limit) {
      throw new BufferOverflowException();
    }
    Memory.pokeShort(backingArray, arrayOffset + position, (short) value, order);
    position = newPosition;
    return this;
  }

  @Override public ByteBuffer putDouble(double value) {
    return putLong(Double.doubleToRawLongBits(value));
  }

  @Override public ByteBuffer putDouble(int index, double value) {
    return putLong(index, Double.doubleToRawLongBits(value));
  }

  @Override public ByteBuffer putFloat(float value) {
    return putInt(Float.floatToRawIntBits(value));
  }

  @Override public ByteBuffer putFloat(int index, float value) {
    return putInt(index, Float.floatToRawIntBits(value));
  }

  @Override public ByteBuffer putInt(int value) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    int newPosition = position + SizeOf.INT;
    if (newPosition > limit) {
      throw new BufferOverflowException();
    }
    Memory.pokeInt(backingArray, arrayOffset + position, value, order);
    position = newPosition;
    return this;
  }

  @Override public ByteBuffer putInt(int index, int value) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    checkIndex(index, SizeOf.INT);
    Memory.pokeInt(backingArray, arrayOffset + index, value, order);
    return this;
  }

  @Override public ByteBuffer putLong(int index, long value) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    checkIndex(index, SizeOf.LONG);
    Memory.pokeLong(backingArray, arrayOffset + index, value, order);
    return this;
  }

  @Override public ByteBuffer putLong(long value) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    int newPosition = position + SizeOf.LONG;
    if (newPosition > limit) {
      throw new BufferOverflowException();
    }
    Memory.pokeLong(backingArray, arrayOffset + position, value, order);
    position = newPosition;
    return this;
  }

  @Override public ByteBuffer putShort(int index, short value) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    checkIndex(index, SizeOf.SHORT);
    Memory.pokeShort(backingArray, arrayOffset + index, value, order);
    return this;
  }

  @Override public ByteBuffer putShort(short value) {
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    int newPosition = position + SizeOf.SHORT;
    if (newPosition > limit) {
      throw new BufferOverflowException();
    }
    Memory.pokeShort(backingArray, arrayOffset + position, value, order);
    position = newPosition;
    return this;
  }

  @Override public final CharBuffer asCharBuffer() {
    return ByteBufferAsCharBuffer.asCharBuffer(this);
  }

  @Override public final DoubleBuffer asDoubleBuffer() {
    return ByteBufferAsDoubleBuffer.asDoubleBuffer(this);
  }

  @Override public final FloatBuffer asFloatBuffer() {
    return ByteBufferAsFloatBuffer.asFloatBuffer(this);
  }

  @Override public final IntBuffer asIntBuffer() {
    return ByteBufferAsIntBuffer.asIntBuffer(this);
  }

  @Override public final LongBuffer asLongBuffer() {
    return ByteBufferAsLongBuffer.asLongBuffer(this);
  }

  @Override public final ShortBuffer asShortBuffer() {
    return ByteBufferAsShortBuffer.asShortBuffer(this);
  }
}
