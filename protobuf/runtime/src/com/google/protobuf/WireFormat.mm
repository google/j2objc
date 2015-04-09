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

#import "com/google/protobuf/WireFormat.h"

#import "com/google/protobuf/CodedInputStream.h"

CGPWireFormat CGPWireFormatForType(CGPFieldType type, BOOL isPacked) {
  if (isPacked) {
    return CGPWireFormatLengthDelimited;
  }
  switch (type) {
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_INT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_UINT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_INT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_BOOL:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_UINT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_ENUM:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SINT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SINT64:
      return CGPWireFormatVarint;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FLOAT:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED32:
      return CGPWireFormatFixed32;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_DOUBLE:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED64:
      return CGPWireFormatFixed64;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_STRING:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_MESSAGE:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_BYTES:
      return CGPWireFormatLengthDelimited;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_GROUP:
      return CGPWireFormatStartGroup;
  }
  __builtin_unreachable();
}

size_t CGPTypeFixedSize(CGPFieldType type) {
  switch (type) {
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_BOOL:
      return 1;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FLOAT:
      return sizeof(uint32_t);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_DOUBLE:
      return sizeof(uint64_t);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_INT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_UINT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SINT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_INT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_UINT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SINT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_ENUM:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_STRING:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_MESSAGE:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_BYTES:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_GROUP:
      return 0;
  }
  __builtin_unreachable();
}

BOOL CGPWireFormatSkipField(CGPCodedInputStream *stream, uint32_t tag) {
  CGPWireFormat wireType = CGPWireFormatGetTagWireType(tag);
  switch (wireType) {
    case CGPWireFormatVarint:
      {
        uint64_t value;
        return stream->ReadVarint64(&value);
      }
    case CGPWireFormatFixed64:
      {
        uint64_t value;
        return stream->ReadLittleEndian64(&value);
      }
    case CGPWireFormatLengthDelimited:
      {
        uint32_t length;
        if (!stream->ReadVarint32(&length)) return NO;
        return stream->Skip(length);
      }
    case CGPWireFormatStartGroup:
      if (!CGPWireFormatSkipMessage(stream)) return NO;
      return stream->LastTagWas(CGPWireFormatMakeTag(
          CGPWireFormatGetTagFieldNumber(tag), CGPWireFormatEndGroup));
    case CGPWireFormatEndGroup:
      return NO;
    case CGPWireFormatFixed32:
      {
        uint32_t value;
        return stream->ReadLittleEndian32(&value);
      }
  }
}

BOOL CGPWireFormatSkipMessage(CGPCodedInputStream *stream) {
  while (YES) {
    uint32_t tag = stream->ReadTag();
    if (tag == 0) {
      return YES;
    }
    CGPWireFormat wireType = CGPWireFormatGetTagWireType(tag);
    if (wireType == CGPWireFormatEndGroup) {
      return YES;
    }
    if (!CGPWireFormatSkipField(stream, tag)) return NO;
  }
}

void CGPWriteString(NSString *value, CGPCodedOutputStream *output) {
  NSUInteger length = [value lengthOfBytesUsingEncoding:NSUTF8StringEncoding];
  output->WriteVarint32((int)length);
  void *buffer;
  int bufferSize;
  NSUInteger usedLength = 0;
  NSRange range = NSMakeRange(0, [value length]);
  while (range.length > 0 && output->GetDirectBufferPointer(&buffer, &bufferSize)) {
    NSUInteger additionalUsedLength;
    NSRange remainingRange;
    [value getBytes:buffer
          maxLength:bufferSize
         usedLength:&additionalUsedLength
           encoding:NSUTF8StringEncoding
            options:0
              range:range
     remainingRange:&remainingRange];
    usedLength += additionalUsedLength;
    range = remainingRange;
    output->Skip((int)additionalUsedLength);
  }
  NSCAssert2(usedLength == length,
             @"String length was wrong: %d vs %d", (int)length, (int)usedLength);
}
