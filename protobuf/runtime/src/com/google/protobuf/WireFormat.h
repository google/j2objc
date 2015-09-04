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

#ifndef __ComGoogleProtobufWireFormat_H__
#define __ComGoogleProtobufWireFormat_H__

#import "JreEmulation.h"

#import "com/google/protobuf/ByteString.h"
#ifdef __cplusplus
#import "com/google/protobuf/CodedInputStream.h"
#import "com/google/protobuf/CodedOutputStream.h"
#endif
#import "com/google/protobuf/FieldTypes.h"

CF_EXTERN_C_BEGIN

#define CGPWireFormatTagTypeBits 3
#define CGPWireFormatTagTypeMask 7

typedef enum {
  CGPWireFormatVarint = 0,
  CGPWireFormatFixed64 = 1,
  CGPWireFormatLengthDelimited = 2,
  CGPWireFormatStartGroup = 3,
  CGPWireFormatEndGroup = 4,
  CGPWireFormatFixed32 = 5,
} CGPWireFormat;

enum {
  CGPWireFormatMessageSetItem = 1,
  CGPWireFormatMessageSetTypeId = 2,
  CGPWireFormatMessageSetMessage = 3
};

static const uint32_t CGPWireFormatMessageSetItemTag =
    (CGPWireFormatMessageSetItem << CGPWireFormatTagTypeBits) | CGPWireFormatStartGroup;
static const uint32_t CGPWireFormatMessageSetItemEndTag =
    (CGPWireFormatMessageSetItem << CGPWireFormatTagTypeBits) | CGPWireFormatEndGroup;
static const uint32_t CGPWireFormatMessageSetTypeIdTag =
    (CGPWireFormatMessageSetTypeId << CGPWireFormatTagTypeBits) | CGPWireFormatVarint;
static const uint32_t CGPWireFormatMessageSetMessageTag =
    (CGPWireFormatMessageSetMessage << CGPWireFormatTagTypeBits) | CGPWireFormatLengthDelimited;

CGP_ALWAYS_INLINE inline uint32_t CGPWireFormatMakeTag(
    uint32_t fieldNumber, CGPWireFormat wireType) {
  return (fieldNumber << CGPWireFormatTagTypeBits) | wireType;
}

CGP_ALWAYS_INLINE inline CGPWireFormat CGPWireFormatGetTagWireType(uint32_t tag) {
  return (CGPWireFormat)(tag & CGPWireFormatTagTypeMask);
}

CGP_ALWAYS_INLINE inline uint32_t CGPWireFormatGetTagFieldNumber(uint32_t tag) {
  return tag >> CGPWireFormatTagTypeBits;
}

CGPWireFormat CGPWireFormatForType(CGPFieldType type, BOOL isPacked);

// Returns the wire size of the type if fixed. Otherwise returns 0.
size_t CGPTypeFixedSize(CGPFieldType type);

CGP_ALWAYS_INLINE inline int32_t CGPZigZagEncode32(int32_t n) {
  // Note:  the right-shift must be arithmetic
  return (n << 1) ^ (n >> 31);
}

CGP_ALWAYS_INLINE inline int32_t CGPZigZagDecode32(uint32_t n) {
  return (n >> 1) ^ -(int32_t)(n & 1);
}

CGP_ALWAYS_INLINE inline int64_t CGPZigZagEncode64(int64_t n) {
  // Note:  the right-shift must be arithmetic
  return (n << 1) ^ (n >> 63);
}

CGP_ALWAYS_INLINE inline int64_t CGPZigZagDecode64(uint64_t n) {
  return (n >> 1) ^ -(int64_t)(n & 1);
}

#ifdef __cplusplus

class CGPCodedInputStream;

BOOL CGPWireFormatSkipField(CGPCodedInputStream *stream, uint32_t tag);

BOOL CGPWireFormatSkipMessage(CGPCodedInputStream *stream);

CGP_ALWAYS_INLINE inline BOOL CGPReadInt32(CGPCodedInputStream *input, int32_t *value) {
  return input->ReadVarint32((uint32_t *)value);
}

CGP_ALWAYS_INLINE inline BOOL CGPReadSint32(CGPCodedInputStream *input, int32_t *value) {
  uint32_t temp;
  if (!input->ReadVarint32(&temp)) return false;
  *value = CGPZigZagDecode32(temp);
  return true;
}

CGP_ALWAYS_INLINE inline BOOL CGPReadFixed32(CGPCodedInputStream *input, int32_t *value) {
  return input->ReadLittleEndian32((uint32_t *)value);
}

CGP_ALWAYS_INLINE inline BOOL CGPReadInt64(CGPCodedInputStream *input, int64_t *value) {
  return input->ReadVarint64((uint64_t *)value);
}

CGP_ALWAYS_INLINE inline BOOL CGPReadSint64(CGPCodedInputStream *input, int64_t *value) {
  uint64_t temp;
  if (!input->ReadVarint64(&temp)) return false;
  *value = CGPZigZagDecode64(temp);
  return true;
}

CGP_ALWAYS_INLINE inline BOOL CGPReadFixed64(CGPCodedInputStream *input, int64_t *value) {
  return input->ReadLittleEndian64((uint64_t *)value);
}

CGP_ALWAYS_INLINE inline BOOL CGPReadBool(CGPCodedInputStream *input, bool *value) {
  uint32_t temp;
  if (!input->ReadVarint32(&temp)) return false;
  *value = temp != 0;
  return true;
}

CGP_ALWAYS_INLINE inline BOOL CGPReadFloat(CGPCodedInputStream *input, float *value) {
  return input->ReadLittleEndian32((uint32_t *)value);
}

CGP_ALWAYS_INLINE inline BOOL CGPReadDouble(CGPCodedInputStream *input, double *value) {
  return input->ReadLittleEndian64((uint64_t *)value);
}

CGP_ALWAYS_INLINE inline BOOL CGPReadEnum(CGPCodedInputStream *input, int32_t *value) {
  return input->ReadVarint32((uint32_t *)value);
}

CGP_ALWAYS_INLINE inline int CGPGetTagSize(uint32_t tag) {
  return CGPCodedOutputStream::VarintSize32(tag);
}

CGP_ALWAYS_INLINE inline int CGPGetInt32Size(int32_t value) {
  return CGPCodedOutputStream::VarintSize32SignExtended(value);
}

CGP_ALWAYS_INLINE inline int CGPGetUint32Size(int32_t value) {
  return CGPCodedOutputStream::VarintSize32(value);
}

CGP_ALWAYS_INLINE inline int CGPGetSint32Size(int32_t value) {
  return CGPCodedOutputStream::VarintSize32(CGPZigZagEncode32(value));
}

CGP_ALWAYS_INLINE inline int CGPGetInt64Size(uint64_t value) {
  return CGPCodedOutputStream::VarintSize64(value);
}

CGP_ALWAYS_INLINE inline int CGPGetSint64Size(uint64_t value) {
  return CGPCodedOutputStream::VarintSize64(CGPZigZagEncode64(value));
}

CGP_ALWAYS_INLINE inline int CGPGetEnumSize(int32_t value) {
  return CGPCodedOutputStream::VarintSize32SignExtended(value);
}

CGP_ALWAYS_INLINE inline int CGPGetBytesSize(CGPByteString *value) {
  int bytesLength = value->size_;
  return CGPGetInt32Size(bytesLength) + bytesLength;
}

CGP_ALWAYS_INLINE inline int CGPGetStringSize(NSString *value) {
  int length = (int)[value lengthOfBytesUsingEncoding:NSUTF8StringEncoding];
  return CGPGetInt32Size(length) + length;
}

CGP_ALWAYS_INLINE inline void CGPWriteInt32(int32_t value, CGPCodedOutputStream *output) {
  output->WriteVarint32SignExtended(value);
}

CGP_ALWAYS_INLINE inline void CGPWriteUint32(int32_t value, CGPCodedOutputStream *output) {
  output->WriteVarint32(value);
}

CGP_ALWAYS_INLINE inline void CGPWriteSint32(int32_t value, CGPCodedOutputStream *output) {
  output->WriteVarint32(CGPZigZagEncode32(value));
}

CGP_ALWAYS_INLINE inline void CGPWriteFixed32(int32_t value, CGPCodedOutputStream *output) {
  output->WriteLittleEndian32(value);
}

CGP_ALWAYS_INLINE inline void CGPWriteInt64(int64_t value, CGPCodedOutputStream *output) {
  output->WriteVarint64(value);
}

CGP_ALWAYS_INLINE inline void CGPWriteSint64(int64_t value, CGPCodedOutputStream *output) {
  output->WriteVarint64(CGPZigZagEncode64(value));
}

CGP_ALWAYS_INLINE inline void CGPWriteFixed64(int64_t value, CGPCodedOutputStream *output) {
  output->WriteLittleEndian64(value);
}

CGP_ALWAYS_INLINE inline void CGPWriteBool(BOOL value, CGPCodedOutputStream *output) {
  output->WriteVarint32(value ? 1 : 0);
}

CGP_ALWAYS_INLINE inline void CGPWriteFloat(float value, CGPCodedOutputStream *output) {
  output->WriteLittleEndian32(*(uint32_t *)&value);
}

CGP_ALWAYS_INLINE inline void CGPWriteDouble(double value, CGPCodedOutputStream *output) {
  output->WriteLittleEndian64(*(uint64_t *)&value);
}

CGP_ALWAYS_INLINE inline void CGPWriteEnum(int32_t value, CGPCodedOutputStream *output) {
  output->WriteVarint32SignExtended(value);
}

CGP_ALWAYS_INLINE inline void CGPWriteBytes(CGPByteString *value, CGPCodedOutputStream *output) {
  output->WriteVarint32(value->size_);
  output->WriteRaw(value->buffer_, value->size_);
}

void CGPWriteString(NSString *value, CGPCodedOutputStream *output);

#endif

CF_EXTERN_C_END

#endif // __ComGoogleProtobufWireFormat_H__
