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

#import "com/google/protobuf/CodedOutputStream_PackagePrivate.h"

#import "J2ObjC_source.h"
#import "java/io/OutputStream.h"

namespace {

static const int kMaxVarintBytes = 10;
static const int kMaxVarint32Bytes = 5;

}  // namespace

CGPCodedOutputStream::CGPCodedOutputStream(JavaIoOutputStream *output)
  : output_(output),
    bytes_([IOSByteArray newArrayWithLength:CGP_CODED_STREAM_BUFFER_SIZE]),
    buffer_((uint8 *)bytes_->buffer_),
    buffer_size_((int)bytes_->size_),
    total_bytes_((int)bytes_->size_),
    had_error_(false) {
}

CGPCodedOutputStream::CGPCodedOutputStream(void *buffer, int size)
  : output_(nil),
    bytes_(nil),
    buffer_((uint8 *)buffer),
    buffer_size_(size),
    total_bytes_(size),
    had_error_(false) {
}

CGPCodedOutputStream::~CGPCodedOutputStream() {
  [bytes_ release];
}

bool CGPCodedOutputStream::Skip(int count) {
  if (count < 0) return false;

  while (count > buffer_size_) {
    count -= buffer_size_;
    if (!Refresh()) return false;
  }

  Advance(count);
  return true;
}

bool CGPCodedOutputStream::GetDirectBufferPointer(void** data, int* size) {
  if (buffer_size_ == 0 && !Refresh()) return false;

  *data = buffer_;
  *size = buffer_size_;
  return true;
}

void CGPCodedOutputStream::WriteRaw(const void* data, int size) {
  while (buffer_size_ < size) {
    memcpy(buffer_, data, buffer_size_);
    size -= buffer_size_;
    data = reinterpret_cast<const uint8*>(data) + buffer_size_;
    if (!Refresh()) return;
  }

  memcpy(buffer_, data, size);
  Advance(size);
}

void CGPCodedOutputStream::WriteLittleEndian32(uint32 value) {
  uint8 bytes[sizeof(value)];

  bool use_fast = buffer_size_ >= (int)sizeof(value);
  uint8* ptr = use_fast ? buffer_ : bytes;

  WriteLittleEndian32ToArray(value, ptr);

  if (use_fast) {
    Advance(sizeof(value));
  } else {
    WriteRaw(bytes, sizeof(value));
  }
}

void CGPCodedOutputStream::WriteLittleEndian64(uint64 value) {
  uint8 bytes[sizeof(value)];

  bool use_fast = buffer_size_ >= (int)sizeof(value);
  uint8* ptr = use_fast ? buffer_ : bytes;

  WriteLittleEndian64ToArray(value, ptr);

  if (use_fast) {
    Advance(sizeof(value));
  } else {
    WriteRaw(bytes, sizeof(value));
  }
}

inline uint8* CGPCodedOutputStream::WriteVarint32FallbackToArrayInline(
    uint32 value, uint8* target) {
  target[0] = static_cast<uint8>(value | 0x80);
  if (value >= (1 << 7)) {
    target[1] = static_cast<uint8>((value >>  7) | 0x80);
    if (value >= (1 << 14)) {
      target[2] = static_cast<uint8>((value >> 14) | 0x80);
      if (value >= (1 << 21)) {
        target[3] = static_cast<uint8>((value >> 21) | 0x80);
        if (value >= (1 << 28)) {
          target[4] = static_cast<uint8>(value >> 28);
          return target + 5;
        } else {
          target[3] &= 0x7F;
          return target + 4;
        }
      } else {
        target[2] &= 0x7F;
        return target + 3;
      }
    } else {
      target[1] &= 0x7F;
      return target + 2;
    }
  } else {
    target[0] &= 0x7F;
    return target + 1;
  }
}

void CGPCodedOutputStream::WriteVarint32(uint32 value) {
  if (buffer_size_ >= kMaxVarint32Bytes) {
    // Fast path:  We have enough bytes left in the buffer to guarantee that
    // this write won't cross the end, so we can skip the checks.
    uint8* target = buffer_;
    uint8* end = WriteVarint32FallbackToArrayInline(value, target);
    int size = (int)(end - target);
    Advance(size);
  } else {
    // Slow path:  This write might cross the end of the buffer, so we
    // compose the bytes first then use WriteRaw().
    uint8 bytes[kMaxVarint32Bytes];
    int size = 0;
    while (value > 0x7F) {
      bytes[size++] = (static_cast<uint8>(value) & 0x7F) | 0x80;
      value >>= 7;
    }
    bytes[size++] = static_cast<uint8>(value) & 0x7F;
    WriteRaw(bytes, size);
  }
}

inline uint8* CGPCodedOutputStream::WriteVarint64ToArrayInline(
    uint64 value, uint8* target) {
  // Splitting into 32-bit pieces gives better performance on 32-bit
  // processors.
  uint32 part0 = static_cast<uint32>(value      );
  uint32 part1 = static_cast<uint32>(value >> 28);
  uint32 part2 = static_cast<uint32>(value >> 56);

  int size;

  // Here we can't really optimize for small numbers, since the value is
  // split into three parts.  Cheking for numbers < 128, for instance,
  // would require three comparisons, since you'd have to make sure part1
  // and part2 are zero.  However, if the caller is using 64-bit integers,
  // it is likely that they expect the numbers to often be very large, so
  // we probably don't want to optimize for small numbers anyway.  Thus,
  // we end up with a hardcoded binary search tree...
  if (part2 == 0) {
    if (part1 == 0) {
      if (part0 < (1 << 14)) {
        if (part0 < (1 << 7)) {
          size = 1; goto size1;
        } else {
          size = 2; goto size2;
        }
      } else {
        if (part0 < (1 << 21)) {
          size = 3; goto size3;
        } else {
          size = 4; goto size4;
        }
      }
    } else {
      if (part1 < (1 << 14)) {
        if (part1 < (1 << 7)) {
          size = 5; goto size5;
        } else {
          size = 6; goto size6;
        }
      } else {
        if (part1 < (1 << 21)) {
          size = 7; goto size7;
        } else {
          size = 8; goto size8;
        }
      }
    }
  } else {
    if (part2 < (1 << 7)) {
      size = 9; goto size9;
    } else {
      size = 10; goto size10;
    }
  }

  NSCAssert(NO, @"Can't get here.");

  size10: target[9] = static_cast<uint8>((part2 >>  7) | 0x80);
  size9 : target[8] = static_cast<uint8>((part2      ) | 0x80);
  size8 : target[7] = static_cast<uint8>((part1 >> 21) | 0x80);
  size7 : target[6] = static_cast<uint8>((part1 >> 14) | 0x80);
  size6 : target[5] = static_cast<uint8>((part1 >>  7) | 0x80);
  size5 : target[4] = static_cast<uint8>((part1      ) | 0x80);
  size4 : target[3] = static_cast<uint8>((part0 >> 21) | 0x80);
  size3 : target[2] = static_cast<uint8>((part0 >> 14) | 0x80);
  size2 : target[1] = static_cast<uint8>((part0 >>  7) | 0x80);
  size1 : target[0] = static_cast<uint8>((part0      ) | 0x80);

  target[size-1] &= 0x7F;
  return target + size;
}

void CGPCodedOutputStream::WriteVarint64(uint64 value) {
  if (buffer_size_ >= kMaxVarintBytes) {
    // Fast path:  We have enough bytes left in the buffer to guarantee that
    // this write won't cross the end, so we can skip the checks.
    uint8* target = buffer_;

    uint8* end = WriteVarint64ToArrayInline(value, target);
    int size = (int)(end - target);
    Advance(size);
  } else {
    // Slow path:  This write might cross the end of the buffer, so we
    // compose the bytes first then use WriteRaw().
    uint8 bytes[kMaxVarintBytes];
    int size = 0;
    while (value > 0x7F) {
      bytes[size++] = (static_cast<uint8>(value) & 0x7F) | 0x80;
      value >>= 7;
    }
    bytes[size++] = static_cast<uint8>(value) & 0x7F;
    WriteRaw(bytes, size);
  }
}

bool CGPCodedOutputStream::FlushBuffer() {
  if (output_ != NULL) {
    // Flush remaining bytes to the output stream.
    int size = (int)bytes_->size_ - buffer_size_;
    [output_ writeWithByteArray:bytes_ withInt:0 withInt:size];
    buffer_ = (uint8 *)bytes_->buffer_;
    buffer_size_ = (int)bytes_->size_;
    total_bytes_ += size;
    return true;
  } else {
    buffer_ = NULL;
    buffer_size_ = 0;
    had_error_ = true;
    return false;
  }
}

int CGPCodedOutputStream::VarintSize32Fallback(uint32 value) {
  if (value < (1 << 7)) {
    return 1;
  } else if (value < (1 << 14)) {
    return 2;
  } else if (value < (1 << 21)) {
    return 3;
  } else if (value < (1 << 28)) {
    return 4;
  } else {
    return 5;
  }
}

int CGPCodedOutputStream::VarintSize64(uint64 value) {
  if (value < (1ull << 35)) {
    if (value < (1ull << 7)) {
      return 1;
    } else if (value < (1ull << 14)) {
      return 2;
    } else if (value < (1ull << 21)) {
      return 3;
    } else if (value < (1ull << 28)) {
      return 4;
    } else {
      return 5;
    }
  } else {
    if (value < (1ull << 42)) {
      return 6;
    } else if (value < (1ull << 49)) {
      return 7;
    } else if (value < (1ull << 56)) {
      return 8;
    } else if (value < (1ull << 63)) {
      return 9;
    } else {
      return 10;
    }
  }
}

// ============================

@interface ComGoogleProtobufCodedOutputStream ()

- (instancetype)initWithJavaIoOutputStream:(JavaIoOutputStream *)output;

@end

static void ComGoogleProtobufCodedOutputStream_initWithJavaIoOutputStream_(
    ComGoogleProtobufCodedOutputStream *self, JavaIoOutputStream *output);
static ComGoogleProtobufCodedOutputStream *new_ComGoogleProtobufCodedOutputStream_init(void)
    NS_RETURNS_RETAINED;
static ComGoogleProtobufCodedOutputStream *create_ComGoogleProtobufCodedOutputStream_init(void);

@implementation ComGoogleProtobufCodedOutputStream

- (instancetype)initWithJavaIoOutputStream:(JavaIoOutputStream *)output {
  ComGoogleProtobufCodedOutputStream_initWithJavaIoOutputStream_(self, output);
  return self;
}

+ (ComGoogleProtobufCodedOutputStream *)newInstanceWithJavaIoOutputStream:
    (JavaIoOutputStream *)output {
  return ComGoogleProtobufCodedOutputStream_newInstanceWithJavaIoOutputStream_(output);
}

- (void)flush {
  self->codedStream_->FlushBuffer();
}

- (void)dealloc {
  if (codedStream_) {
    delete codedStream_;
    codedStream_ = NULL;
  }
  [super dealloc];
}

@end

void ComGoogleProtobufCodedOutputStream_initWithJavaIoOutputStream_(
    ComGoogleProtobufCodedOutputStream *self, JavaIoOutputStream *output) {
  NSObject_init(self);
  self->codedStream_ = new CGPCodedOutputStream(((JavaIoOutputStream *)nil_chk(output)));
}

ComGoogleProtobufCodedOutputStream *
create_ComGoogleProtobufCodedOutputStream_initWithJavaIoOutputStream_(JavaIoOutputStream *output){
    J2OBJC_CREATE_IMPL(ComGoogleProtobufCodedOutputStream, initWithJavaIoOutputStream_, output)}

ComGoogleProtobufCodedOutputStream
    *ComGoogleProtobufCodedOutputStream_newInstanceWithJavaIoOutputStream_(
        JavaIoOutputStream *output) {
  ComGoogleProtobufCodedOutputStream_initialize();
  return create_ComGoogleProtobufCodedOutputStream_initWithJavaIoOutputStream_(output);
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufCodedOutputStream)
