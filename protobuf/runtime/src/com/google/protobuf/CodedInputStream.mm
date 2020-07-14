// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// https://developers.google.com/protocol-buffers/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import "com/google/protobuf/CodedInputStream.h"

#import "com/google/protobuf/ByteString.h"
#import "java/io/InputStream.h"
#import "java/nio/ByteBuffer.h"
#import "java/nio/CharBuffer.h"
#import "java/nio/charset/Charset.h"

namespace {

static const int kMaxVarintBytes = 10;
static const int kMaxVarint32Bytes = 5;

}  // namespace

CGPCodedInputStream::~CGPCodedInputStream() {
  [bytes_ release];
}

inline void CGPCodedInputStream::RecomputeBufferLimits() {
  buffer_end_ += buffer_size_after_limit_;
  if (current_limit_ < total_bytes_read_) {
    // The limit position is in the current buffer.  We must adjust
    // the buffer size accordingly.
    buffer_size_after_limit_ = total_bytes_read_ - current_limit_;
    buffer_end_ -= buffer_size_after_limit_;
  } else {
    buffer_size_after_limit_ = 0;
  }
}

CGPCodedInputStream::Limit CGPCodedInputStream::PushLimit(int byte_limit) {
  // Current position relative to the beginning of the stream.
  int current_position = CurrentPosition();

  Limit old_limit = current_limit_;

  // security: byte_limit is possibly evil, so check for negative values
  // and overflow.
  if (byte_limit >= 0 &&
      byte_limit <= INT_MAX - current_position) {
    current_limit_ = current_position + byte_limit;
  } else {
    // Negative or overflow.
    current_limit_ = INT_MAX;
  }

  // We need to enforce all limits, not just the new one, so if the previous
  // limit was before the new requested limit, we continue to enforce the
  // previous limit.
  current_limit_ = MIN(current_limit_, old_limit);

  RecomputeBufferLimits();
  return old_limit;
}

void CGPCodedInputStream::PopLimit(Limit limit) {
  // The limit passed in is actually the *old* limit, which we returned from
  // PushLimit().
  current_limit_ = limit;
  RecomputeBufferLimits();

  // We may no longer be at a legitimate message end.  ReadTag() needs to be
  // called again to find out.
  legitimate_message_end_ = false;
}

int CGPCodedInputStream::BytesUntilLimit() const {
  if (current_limit_ == INT_MAX) return -1;
  int current_position = CurrentPosition();

  return current_limit_ - current_position;
}

bool CGPCodedInputStream::Skip(int count) {
  if (count < 0) return false;  // security: count is often user-supplied

  int current_buffer_size;
  while ((current_buffer_size = BufferSize()) < count) {
    count -= current_buffer_size;
    Advance(current_buffer_size);
    if (!Refresh()) return false;
  }

  Advance(count);
  return true;
}

bool CGPCodedInputStream::ReadRaw(void* buffer, int size) {
  int current_buffer_size;
  while ((current_buffer_size = BufferSize()) < size) {
    // Reading past end of buffer.  Copy what we have, then refresh.
    memcpy(buffer, buffer_, current_buffer_size);
    buffer = reinterpret_cast<uint8*>(buffer) + current_buffer_size;
    size -= current_buffer_size;
    Advance(current_buffer_size);
    if (!Refresh()) return false;
  }

  memcpy(buffer, buffer_, size);
  Advance(size);

  return true;
}

bool CGPCodedInputStream::ReadStringFallback(string* buffer, int size) {
  if (!buffer->empty()) {
    buffer->clear();
  }

  int current_buffer_size;
  while ((current_buffer_size = BufferSize()) < size) {
    // Some STL implementations "helpfully" crash on buffer->append(NULL, 0).
    if (current_buffer_size != 0) {
      // Note:  string1.append(string2) is O(string2.size()) (as opposed to
      //   O(string1.size() + string2.size()), which would be bad).
      buffer->append(reinterpret_cast<const char*>(buffer_),
                     current_buffer_size);
    }
    size -= current_buffer_size;
    Advance(current_buffer_size);
    if (!Refresh()) return false;
  }

  buffer->append(reinterpret_cast<const char*>(buffer_), size);
  Advance(size);

  return true;
}

bool CGPCodedInputStream::ReadLittleEndian32Fallback(uint32* value) {
  uint8 bytes[sizeof(*value)];

  const uint8* ptr;
  if ((unsigned)BufferSize() >= sizeof(*value)) {
    // Fast path:  Enough bytes in the buffer to read directly.
    ptr = buffer_;
    Advance(sizeof(*value));
  } else {
    // Slow path:  Had to read past the end of the buffer.
    if (!ReadRaw(bytes, sizeof(*value))) return false;
    ptr = bytes;
  }
  uint32 readVal;
  memcpy(&readVal, ptr, sizeof(readVal));
  *value = OSSwapLittleToHostInt32(readVal);
  return true;
}

bool CGPCodedInputStream::ReadLittleEndian64Fallback(uint64* value) {
  uint8 bytes[sizeof(*value)];

  const uint8* ptr;
  if ((unsigned)BufferSize() >= sizeof(*value)) {
    // Fast path:  Enough bytes in the buffer to read directly.
    ptr = buffer_;
    Advance(sizeof(*value));
  } else {
    // Slow path:  Had to read past the end of the buffer.
    if (!ReadRaw(bytes, sizeof(*value))) return false;
    ptr = bytes;
  }
  uint64 readVal;
  memcpy(&readVal, ptr, sizeof(readVal));
  *value = OSSwapLittleToHostInt64(readVal);
  return true;
}

namespace {

inline const uint8* ReadVarint32FromArray(
    const uint8* buffer, uint32* value) CGP_ALWAYS_INLINE;
inline const uint8* ReadVarint32FromArray(const uint8* buffer, uint32* value) {
  // Fast path:  We have enough bytes left in the buffer to guarantee that
  // this read won't cross the end, so we can skip the checks.
  const uint8* ptr = buffer;
  uint32 b;
  uint32 result;

  b = *(ptr++); result  = (b & 0x7F)      ; if (!(b & 0x80)) goto done;
  b = *(ptr++); result |= (b & 0x7F) <<  7; if (!(b & 0x80)) goto done;
  b = *(ptr++); result |= (b & 0x7F) << 14; if (!(b & 0x80)) goto done;
  b = *(ptr++); result |= (b & 0x7F) << 21; if (!(b & 0x80)) goto done;
  b = *(ptr++); result |=  b         << 28; if (!(b & 0x80)) goto done;

  // If the input is larger than 32 bits, we still need to read it all
  // and discard the high-order bits.
  for (int i = 0; i < kMaxVarintBytes - kMaxVarint32Bytes; i++) {
    b = *(ptr++); if (!(b & 0x80)) goto done;
  }

  // We have overrun the maximum size of a varint (10 bytes).  Assume
  // the data is corrupt.
  return NULL;

 done:
  *value = result;
  return ptr;
}

}  // namespace

bool CGPCodedInputStream::ReadVarint32Slow(uint32* value) {
  uint64 result;
  // Directly invoke ReadVarint64Fallback, since we already tried to optimize
  // for one-byte varints.
  if (!ReadVarint64Fallback(&result)) return false;
  *value = (uint32)result;
  return true;
}

bool CGPCodedInputStream::ReadVarint32Fallback(uint32* value) {
  if (BufferSize() >= kMaxVarintBytes ||
      // Optimization:  If the varint ends at exactly the end of the buffer,
      // we can detect that and still use the fast path.
      (buffer_end_ > buffer_ && !(buffer_end_[-1] & 0x80))) {
    const uint8* end = ReadVarint32FromArray(buffer_, value);
    if (end == NULL) return false;
    buffer_ = end;
    return true;
  } else {
    // Really slow case: we will incur the cost of an extra function call here,
    // but moving this out of line reduces the size of this function, which
    // improves the common case. In micro benchmarks, this is worth about 10-15%
    return ReadVarint32Slow(value);
  }
}

uint32 CGPCodedInputStream::ReadTagSlow() {
  if (buffer_ == buffer_end_) {
    // Call refresh.
    if (!Refresh()) {
      legitimate_message_end_ = true;
      return 0;
    }
  }

  // For the slow path, just do a 64-bit read. Try to optimize for one-byte tags
  // again, since we have now refreshed the buffer.
  uint64 result = 0;
  if (!ReadVarint64(&result)) return 0;
  return static_cast<uint32>(result);
}

uint32 CGPCodedInputStream::ReadTagFallback() {
  const int buf_size = BufferSize();
  if (buf_size >= kMaxVarintBytes ||
      // Optimization:  If the varint ends at exactly the end of the buffer,
      // we can detect that and still use the fast path.
      (buf_size > 0 && !(buffer_end_[-1] & 0x80))) {
    uint32 tag;
    const uint8* end = ReadVarint32FromArray(buffer_, &tag);
    if (end == NULL) {
      return 0;
    }
    buffer_ = end;
    return tag;
  } else {
    // We are commonly at a limit when attempting to read tags. Try to quickly
    // detect this case without making another function call.
    if ((buf_size == 0) &&
        ((buffer_size_after_limit_ > 0) ||
         (total_bytes_read_ == current_limit_))) {
      // We hit a byte limit.
      legitimate_message_end_ = true;
      return 0;
    }
    return ReadTagSlow();
  }
}

bool CGPCodedInputStream::ReadVarint64Slow(uint64* value) {
  // Slow path:  This read might cross the end of the buffer, so we
  // need to check and refresh the buffer if and when it does.

  uint64 result = 0;
  int count = 0;
  uint32 b;

  do {
    if (count == kMaxVarintBytes) return false;
    while (buffer_ == buffer_end_) {
      if (!Refresh()) return false;
    }
    b = *buffer_;
    result |= static_cast<uint64>(b & 0x7F) << (7 * count);
    Advance(1);
    ++count;
  } while (b & 0x80);

  *value = result;
  return true;
}

bool CGPCodedInputStream::ReadVarint64Fallback(uint64* value) {
  if (BufferSize() >= kMaxVarintBytes ||
      // Optimization:  If the varint ends at exactly the end of the buffer,
      // we can detect that and still use the fast path.
      (buffer_end_ > buffer_ && !(buffer_end_[-1] & 0x80))) {
    // Fast path:  We have enough bytes left in the buffer to guarantee that
    // this read won't cross the end, so we can skip the checks.

    const uint8* ptr = buffer_;
    uint32 b;

    // Splitting into 32-bit pieces gives better performance on 32-bit
    // processors.
    uint32 part0 = 0, part1 = 0, part2 = 0;

    b = *(ptr++); part0  = (b & 0x7F)      ; if (!(b & 0x80)) goto done;
    b = *(ptr++); part0 |= (b & 0x7F) <<  7; if (!(b & 0x80)) goto done;
    b = *(ptr++); part0 |= (b & 0x7F) << 14; if (!(b & 0x80)) goto done;
    b = *(ptr++); part0 |= (b & 0x7F) << 21; if (!(b & 0x80)) goto done;
    b = *(ptr++); part1  = (b & 0x7F)      ; if (!(b & 0x80)) goto done;
    b = *(ptr++); part1 |= (b & 0x7F) <<  7; if (!(b & 0x80)) goto done;
    b = *(ptr++); part1 |= (b & 0x7F) << 14; if (!(b & 0x80)) goto done;
    b = *(ptr++); part1 |= (b & 0x7F) << 21; if (!(b & 0x80)) goto done;
    b = *(ptr++); part2  = (b & 0x7F)      ; if (!(b & 0x80)) goto done;
    b = *(ptr++); part2 |= (b & 0x7F) <<  7; if (!(b & 0x80)) goto done;

    // We have overrun the maximum size of a varint (10 bytes).  The data
    // must be corrupt.
    return false;

   done:
    Advance((int)(ptr - buffer_));
    *value = (static_cast<uint64>(part0)      ) |
             (static_cast<uint64>(part1) << 28) |
             (static_cast<uint64>(part2) << 56);
    return true;
  } else {
    return ReadVarint64Slow(value);
  }
}

bool CGPCodedInputStream::Refresh() {
  NSCAssert(BufferSize() == 0, @"Expected end of buffer.");

  if (buffer_size_after_limit_ > 0 ||
      total_bytes_read_ == current_limit_) {
    // We've hit a limit.  Stop.
    return false;
  }

  int buffer_size = -1;
  if (input_ != nil) {
    int max_bytes_to_read =
        MIN(input_limit_ - total_bytes_read_, (int)bytes_->size_);
    if (max_bytes_to_read > 0) {
      buffer_size = [input_ readWithByteArray:bytes_
                                      withInt:0
                                      withInt:max_bytes_to_read];
      buffer_ = (uint8 *)bytes_->buffer_;
    }
  }
  if (buffer_size >= 0) {
    buffer_end_ = buffer_ + buffer_size;

    NSCAssert(total_bytes_read_ <= INT_MAX - buffer_size,
              @"CodedInputStream overflow.");
    total_bytes_read_ += buffer_size;

    RecomputeBufferLimits();
    return true;
  } else {
    buffer_ = NULL;
    buffer_end_ = NULL;
    return false;
  }
}

static NSString *RetainedStringFromBytes(const uint8 *bytes, uint32 size) {
  NSString *result = (NSString *)CFStringCreateWithBytes(
      NULL, bytes, size, kCFStringEncodingUTF8, false);
  if (result) {
    return result;
  }

  // CFString failed to decode the bytes, possibly due to an invalid UTF-8
  // sequence. Java's decoder will replace malformed bytes with a replacement
  // character but we have to copy the data into a ByteBuffer and out of a
  // CharBuffer.
  static JavaNioCharsetCharset *utf8Charset;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    utf8Charset = JavaNioCharsetCharset_forNameWithNSString_(@"UTF-8");
  });
  IOSByteArray *javaBytes = [IOSByteArray newArrayWithBytes:(const jbyte *)bytes count:size];
  JavaNioByteBuffer *bb = JavaNioByteBuffer_wrapWithByteArray_(javaBytes);
  JavaNioCharBuffer *cb = [utf8Charset decodeWithJavaNioByteBuffer:bb];
  [javaBytes release];
  return [[NSString alloc] initWithCharacters:[cb array]->buffer_ + [cb position]
                                       length:[cb remaining]];
}

bool CGPCodedInputStream::ReadRetainedNSString(NSString **value) {
  uint32 size;
  if (!ReadVarint32(&size)) return false;

  if ((unsigned)BufferSize() >= size) {
    *value = RetainedStringFromBytes(buffer_, size);
    Advance(size);
    return true;
  }

  // If there aren't enough bytes in the buffer, fallback to reading into a C++
  // string then copying into a CFStringRef.
  string string;
  if (!ReadStringFallback(&string, size)) return false;
  *value = RetainedStringFromBytes(reinterpret_cast<const uint8*>(string.data()), size);
  return true;
}

bool CGPCodedInputStream::ReadRetainedByteString(CGPByteString **value) {
  uint32 size;
  if (!ReadVarint32(&size)) return false;

  if ((unsigned)BufferSize() >= size) {
    *value = CGPNewByteString(size);
    memcpy((*value)->buffer_, buffer_, size);
    Advance(size);
    return true;
  }

  // If there aren't enough bytes in the buffer, fallback to reading into a C++
  // string then copying into a new ByteString.
  string string;
  if (!ReadStringFallback(&string, size)) return false;
  *value = CGPNewByteString(size);
  memcpy((*value)->buffer_, string.data(), size);
  return true;
}

// Translated from Java's CodedInputStream. Used by mergeDelimitedFrom. Reads
// one byte at a time from the stream because we don't know how long the message
// is yet and we can't read past the end of the message.
bool CGPCodedInputStream::ReadVarint32(
    int firstByte, JavaIoInputStream *input, uint32 *value) {
  *value = firstByte & (int) 0x7f;
  if ((firstByte & (int) 0x80) == 0) {
    return true;
  }
  int offset = 7;
  for (; offset < 32; offset += 7) {
    int b = [input read];
    if (b == -1) {
      return false;
    }
    *value |= (b & (int) 0x7f) << offset;
    if ((b & (int) 0x80) == 0) {
      return true;
    }
  }
  // Keep reading up to 64 bits.
  for (; offset < 64; offset += 7) {
    int b = [input read];
    if (b == -1) {
      return false;
    }
    if ((b & (int) 0x80) == 0) {
      return true;
    }
  }
  return false;
}
