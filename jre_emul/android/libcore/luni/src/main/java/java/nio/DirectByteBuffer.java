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

import java.nio.channels.FileChannel.MapMode;

import libcore.io.Memory;
import libcore.io.SizeOf;

class DirectByteBuffer extends MappedByteBuffer {
  // This is the offset into {@code Buffer.block} at which this buffer logically starts.
  // TODO: rewrite this so we set 'block' to an OffsetMemoryBlock?
  protected final int offset;
  private boolean freed;

  private final boolean isReadOnly;

  protected DirectByteBuffer(MemoryBlock block, int capacity, int offset, boolean isReadOnly, MapMode mapMode) {
    super(block, capacity, mapMode, block.toLong() + offset);

    long baseSize = block.getSize();
    // We're throwing this exception after we passed a bogus value
    // to the superclass constructor, but it doesn't make any
    // difference in this case.
    if (baseSize >= 0 && (capacity + offset) > baseSize) {
      throw new IllegalArgumentException("capacity + offset > baseSize");
    }

    this.offset = offset;
    this.isReadOnly = isReadOnly;
  }

  // Used by the JNI NewDirectByteBuffer function.
  DirectByteBuffer(long address, int capacity) {
    this(MemoryBlock.wrapFromJni(address, capacity), capacity, 0, false, null);
  }

  private static DirectByteBuffer copy(DirectByteBuffer other, int markOfOther, boolean isReadOnly) {
    other.checkNotFreed();
    DirectByteBuffer buf = new DirectByteBuffer(other.block, other.capacity(), other.offset, isReadOnly, other.mapMode);
    buf.limit = other.limit;
    buf.position = other.position();
    buf.mark = markOfOther;
    return buf;
  }

  @Override public ByteBuffer asReadOnlyBuffer() {
    return copy(this, mark, true);
  }

  @Override public ByteBuffer compact() {
    checkNotFreed();
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    Memory.memmove(this, 0, this, position, remaining());
    position = limit - position;
    limit = capacity;
    mark = UNSET_MARK;
    return this;
  }

  @Override public ByteBuffer duplicate() {
    return copy(this, mark, isReadOnly);
  }

  @Override public ByteBuffer slice() {
    checkNotFreed();
    return new DirectByteBuffer(block, remaining(), offset + position, isReadOnly, mapMode);
  }

  @Override public boolean isReadOnly() {
    return isReadOnly;
  }

  @Override byte[] protectedArray() {
    checkNotFreed();
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    byte[] array = this.block.array();
    if (array == null) {
      throw new UnsupportedOperationException();
    }
    return array;
  }

  @Override int protectedArrayOffset() {
    protectedArray(); // Throw if we don't have an array or are read-only.
    return offset;
  }

  @Override boolean protectedHasArray() {
    return !isReadOnly && (block.array() != null);
  }

  @Override public final ByteBuffer get(byte[] dst, int dstOffset, int byteCount) {
    checkNotFreed();
    checkGetBounds(1, dst.length, dstOffset, byteCount);
    this.block.peekByteArray(offset + position, dst, dstOffset, byteCount);
    position += byteCount;
    return this;
  }

  final void get(char[] dst, int dstOffset, int charCount) {
    checkNotFreed();
    int byteCount = checkGetBounds(SizeOf.CHAR, dst.length, dstOffset, charCount);
    this.block.peekCharArray(offset + position, dst, dstOffset, charCount, order.needsSwap);
    position += byteCount;
  }

  final void get(double[] dst, int dstOffset, int doubleCount) {
    checkNotFreed();
    int byteCount = checkGetBounds(SizeOf.DOUBLE, dst.length, dstOffset, doubleCount);
    this.block.peekDoubleArray(offset + position, dst, dstOffset, doubleCount, order.needsSwap);
    position += byteCount;
  }

  final void get(float[] dst, int dstOffset, int floatCount) {
    checkNotFreed();
    int byteCount = checkGetBounds(SizeOf.FLOAT, dst.length, dstOffset, floatCount);
    this.block.peekFloatArray(offset + position, dst, dstOffset, floatCount, order.needsSwap);
    position += byteCount;
  }

  final void get(int[] dst, int dstOffset, int intCount) {
    checkNotFreed();
    int byteCount = checkGetBounds(SizeOf.INT, dst.length, dstOffset, intCount);
    this.block.peekIntArray(offset + position, dst, dstOffset, intCount, order.needsSwap);
    position += byteCount;
  }

  final void get(long[] dst, int dstOffset, int longCount) {
    checkNotFreed();
    int byteCount = checkGetBounds(SizeOf.LONG, dst.length, dstOffset, longCount);
    this.block.peekLongArray(offset + position, dst, dstOffset, longCount, order.needsSwap);
    position += byteCount;
  }

  final void get(short[] dst, int dstOffset, int shortCount) {
    checkNotFreed();
    int byteCount = checkGetBounds(SizeOf.SHORT, dst.length, dstOffset, shortCount);
    this.block.peekShortArray(offset + position, dst, dstOffset, shortCount, order.needsSwap);
    position += byteCount;
  }

  @Override public final byte get() {
    checkNotFreed();
    if (position == limit) {
      throw new BufferUnderflowException();
    }
    return this.block.peekByte(offset + position++);
  }

  @Override public final byte get(int index) {
    checkNotFreed();
    checkIndex(index);
    return this.block.peekByte(offset + index);
  }

  @Override public final char getChar() {
    checkNotFreed();
    int newPosition = position + SizeOf.CHAR;
    if (newPosition > limit) {
      throw new BufferUnderflowException();
    }
    char result = (char) this.block.peekShort(offset + position, order);
    position = newPosition;
    return result;
  }

  @Override public final char getChar(int index) {
    checkNotFreed();
    checkIndex(index, SizeOf.CHAR);
    return (char) this.block.peekShort(offset + index, order);
  }

  @Override public final double getDouble() {
    checkNotFreed();
    int newPosition = position + SizeOf.DOUBLE;
    if (newPosition > limit) {
      throw new BufferUnderflowException();
    }
    double result = Double.longBitsToDouble(this.block.peekLong(offset + position, order));
    position = newPosition;
    return result;
  }

  @Override public final double getDouble(int index) {
    checkNotFreed();
    checkIndex(index, SizeOf.DOUBLE);
    return Double.longBitsToDouble(this.block.peekLong(offset + index, order));
  }

  @Override public final float getFloat() {
    checkNotFreed();
    int newPosition = position + SizeOf.FLOAT;
    if (newPosition > limit) {
      throw new BufferUnderflowException();
    }
    float result = Float.intBitsToFloat(this.block.peekInt(offset + position, order));
    position = newPosition;
    return result;
  }

  @Override public final float getFloat(int index) {
    checkNotFreed();
    checkIndex(index, SizeOf.FLOAT);
    return Float.intBitsToFloat(this.block.peekInt(offset + index, order));
  }

  @Override public final int getInt() {
    checkNotFreed();
    int newPosition = position + SizeOf.INT;
    if (newPosition > limit) {
      throw new BufferUnderflowException();
    }
    int result = this.block.peekInt(offset + position, order);
    position = newPosition;
    return result;
  }

  @Override public final int getInt(int index) {
    checkNotFreed();
    checkIndex(index, SizeOf.INT);
    return this.block.peekInt(offset + index, order);
  }

  @Override public final long getLong() {
    checkNotFreed();
    int newPosition = position + SizeOf.LONG;
    if (newPosition > limit) {
      throw new BufferUnderflowException();
    }
    long result = this.block.peekLong(offset + position, order);
    position = newPosition;
    return result;
  }

  @Override public final long getLong(int index) {
    checkNotFreed();
    checkIndex(index, SizeOf.LONG);
    return this.block.peekLong(offset + index, order);
  }

  @Override public final short getShort() {
    checkNotFreed();
    int newPosition = position + SizeOf.SHORT;
    if (newPosition > limit) {
      throw new BufferUnderflowException();
    }
    short result = this.block.peekShort(offset + position, order);
    position = newPosition;
    return result;
  }

  @Override public final short getShort(int index) {
    checkNotFreed();
    checkIndex(index, SizeOf.SHORT);
    return this.block.peekShort(offset + index, order);
  }

  @Override public final boolean isDirect() {
    return true;
  }

  /** @hide */
  @Override public final boolean isValid() {
    return !freed;
  }

  /**
   * Invalidates the buffer. Subsequent operations which touch the inner
   * buffer will throw {@link IllegalStateException}.
   */
  public final void free() {
    if (!freed) {
      block.free();
      freed = true;
    }
  }

  @Override public final CharBuffer asCharBuffer() {
    checkNotFreed();
    return ByteBufferAsCharBuffer.asCharBuffer(this);
  }

  @Override public final DoubleBuffer asDoubleBuffer() {
    checkNotFreed();
    return ByteBufferAsDoubleBuffer.asDoubleBuffer(this);
  }

  @Override public final FloatBuffer asFloatBuffer() {
    checkNotFreed();
    return ByteBufferAsFloatBuffer.asFloatBuffer(this);
  }

  @Override public final IntBuffer asIntBuffer() {
    checkNotFreed();
    return ByteBufferAsIntBuffer.asIntBuffer(this);
  }

  @Override public final LongBuffer asLongBuffer() {
    checkNotFreed();
    return ByteBufferAsLongBuffer.asLongBuffer(this);
  }

  @Override public final ShortBuffer asShortBuffer() {
    checkNotFreed();
    return ByteBufferAsShortBuffer.asShortBuffer(this);
  }

  @Override public ByteBuffer put(byte value) {
    checkNotFreed();
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    if (position == limit) {
      throw new BufferOverflowException();
    }
    this.block.pokeByte(offset + position++, value);
    return this;
  }

  @Override public ByteBuffer put(int index, byte value) {
    checkNotFreed();
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    checkIndex(index);
    this.block.pokeByte(offset + index, value);
    return this;
  }

  @Override public ByteBuffer put(byte[] src, int srcOffset, int byteCount) {
    checkNotFreed();
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    checkPutBounds(1, src.length, srcOffset, byteCount);
    this.block.pokeByteArray(offset + position, src, srcOffset, byteCount);
    position += byteCount;
    return this;
  }

  final void put(char[] src, int srcOffset, int charCount) {
    checkNotFreed();
    int byteCount = checkPutBounds(SizeOf.CHAR, src.length, srcOffset, charCount);
    this.block.pokeCharArray(offset + position, src, srcOffset, charCount, order.needsSwap);
    position += byteCount;
  }

  final void put(double[] src, int srcOffset, int doubleCount) {
    checkNotFreed();
    int byteCount = checkPutBounds(SizeOf.DOUBLE, src.length, srcOffset, doubleCount);
    this.block.pokeDoubleArray(offset + position, src, srcOffset, doubleCount, order.needsSwap);
    position += byteCount;
  }

  final void put(float[] src, int srcOffset, int floatCount) {
    checkNotFreed();
    int byteCount = checkPutBounds(SizeOf.FLOAT, src.length, srcOffset, floatCount);
    this.block.pokeFloatArray(offset + position, src, srcOffset, floatCount, order.needsSwap);
    position += byteCount;
  }

  final void put(int[] src, int srcOffset, int intCount) {
    checkNotFreed();
    int byteCount = checkPutBounds(SizeOf.INT, src.length, srcOffset, intCount);
    this.block.pokeIntArray(offset + position, src, srcOffset, intCount, order.needsSwap);
    position += byteCount;
  }

  final void put(long[] src, int srcOffset, int longCount) {
    checkNotFreed();
    int byteCount = checkPutBounds(SizeOf.LONG, src.length, srcOffset, longCount);
    this.block.pokeLongArray(offset + position, src, srcOffset, longCount, order.needsSwap);
    position += byteCount;
  }

  final void put(short[] src, int srcOffset, int shortCount) {
    checkNotFreed();
    int byteCount = checkPutBounds(SizeOf.SHORT, src.length, srcOffset, shortCount);
    this.block.pokeShortArray(offset + position, src, srcOffset, shortCount, order.needsSwap);
    position += byteCount;
  }

  @Override public ByteBuffer putChar(char value) {
    checkNotFreed();
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    int newPosition = position + SizeOf.CHAR;
    if (newPosition > limit) {
      throw new BufferOverflowException();
    }
    this.block.pokeShort(offset + position, (short) value, order);
    position = newPosition;
    return this;
  }

  @Override public ByteBuffer putChar(int index, char value) {
    checkNotFreed();
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    checkIndex(index, SizeOf.CHAR);
    this.block.pokeShort(offset + index, (short) value, order);
    return this;
  }

  @Override public ByteBuffer putDouble(double value) {
    checkNotFreed();
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    int newPosition = position + SizeOf.DOUBLE;
    if (newPosition > limit) {
      throw new BufferOverflowException();
    }
    this.block.pokeLong(offset + position, Double.doubleToRawLongBits(value), order);
    position = newPosition;
    return this;
  }

  @Override public ByteBuffer putDouble(int index, double value) {
    checkNotFreed();
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    checkIndex(index, SizeOf.DOUBLE);
    this.block.pokeLong(offset + index, Double.doubleToRawLongBits(value), order);
    return this;
  }

  @Override public ByteBuffer putFloat(float value) {
    checkNotFreed();
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    int newPosition = position + SizeOf.FLOAT;
    if (newPosition > limit) {
      throw new BufferOverflowException();
    }
    this.block.pokeInt(offset + position, Float.floatToRawIntBits(value), order);
    position = newPosition;
    return this;
  }

  @Override public ByteBuffer putFloat(int index, float value) {
    checkNotFreed();
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    checkIndex(index, SizeOf.FLOAT);
    this.block.pokeInt(offset + index, Float.floatToRawIntBits(value), order);
    return this;
  }

  @Override public ByteBuffer putInt(int value) {
    checkNotFreed();
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    int newPosition = position + SizeOf.INT;
    if (newPosition > limit) {
      throw new BufferOverflowException();
    }
    this.block.pokeInt(offset + position, value, order);
    position = newPosition;
    return this;
  }

  @Override public ByteBuffer putInt(int index, int value) {
    checkNotFreed();
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    checkIndex(index, SizeOf.INT);
    this.block.pokeInt(offset + index, value, order);
    return this;
  }

  @Override public ByteBuffer putLong(long value) {
    checkNotFreed();
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    int newPosition = position + SizeOf.LONG;
    if (newPosition > limit) {
      throw new BufferOverflowException();
    }
    this.block.pokeLong(offset + position, value, order);
    position = newPosition;
    return this;
  }

  @Override public ByteBuffer putLong(int index, long value) {
    checkNotFreed();
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    checkIndex(index, SizeOf.LONG);
    this.block.pokeLong(offset + index, value, order);
    return this;
  }

  @Override public ByteBuffer putShort(short value) {
    checkNotFreed();
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    int newPosition = position + SizeOf.SHORT;
    if (newPosition > limit) {
      throw new BufferOverflowException();
    }
    this.block.pokeShort(offset + position, value, order);
    position = newPosition;
    return this;
  }

  @Override public ByteBuffer putShort(int index, short value) {
    checkNotFreed();
    if (isReadOnly) {
      throw new ReadOnlyBufferException();
    }
    checkIndex(index, SizeOf.SHORT);
    this.block.pokeShort(offset + index, value, order);
    return this;
  }

  private void checkNotFreed() {
    if (freed) {
      throw new IllegalStateException("buffer was freed");
    }
  }

}
