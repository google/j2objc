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

//  Created by Keith Stanger on 11/28/12.
//
//  Hand written counterpart of com.google.protobuf.ByteString.

#import "com/google/protobuf/ByteString.h"

#import "IOSPrimitiveArray.h"
#import "J2ObjC_source.h"
#import "java/io/InputStream.h"
#import "java/io/OutputStream.h"
#import "java/lang/ArrayIndexOutOfBoundsException.h"
#import "java/lang/Byte.h"
#import "java/lang/IndexOutOfBoundsException.h"
#import "java/lang/UnsupportedOperationException.h"
#import "java/util/NoSuchElementException.h"

#define MIN_READ_FROM_CHUNK_SIZE 0x100   // 256b
#define MAX_READ_FROM_CHUNK_SIZE 0x2000  // 8k

J2OBJC_INITIALIZED_DEFN(ComGoogleProtobufByteString)

ComGoogleProtobufByteString *ComGoogleProtobufByteString_EMPTY;

ComGoogleProtobufByteString *CGPNewByteString(jint len) {
  CGPByteString *byteString = NSAllocateObject([CGPByteString class], len, nil);
  byteString->size_ = len;
  return byteString;
}

ComGoogleProtobufByteString *ComGoogleProtobufByteString_copyFromWithByteArray_(
    IOSByteArray *bytes) {
  (void)nil_chk(bytes);  // Ensure Java compatibility.
  CGPByteString *byteString = CGPNewByteString(bytes->size_);
  memcpy(byteString->buffer_, bytes->buffer_, bytes->size_);
  return [byteString autorelease];
}

ComGoogleProtobufByteString *ComGoogleProtobufByteString_copyFromUtf8WithNSString_(NSString *text) {
  (void)nil_chk(text);  // Ensure Java compatibility.
  NSUInteger length = [text lengthOfBytesUsingEncoding:NSUTF8StringEncoding];
  CGPByteString *byteString = CGPNewByteString((jint)length);
  [text getBytes:byteString->buffer_
       maxLength:length
      usedLength:NULL
        encoding:NSUTF8StringEncoding
         options:0
           range:NSMakeRange(0, [text length])
      remainingRange:NULL];
  return [byteString autorelease];
}


@interface ByteStringIterator : NSObject < ComGoogleProtobufByteString_ByteIterator > {
 @public
  ComGoogleProtobufByteString *byteString_;
  jint position_;
  jint limit_;
}

- (instancetype)initWithComGoogleProtobufByteString:(ComGoogleProtobufByteString *)byteString;
- (jboolean)hasNext;
- (JavaLangByte *)next;
- (jbyte)nextByte;
- (void)remove;

@end

J2OBJC_EMPTY_STATIC_INIT(ByteStringIterator)

static void ByteStringIterator_initWithComGoogleProtobufByteString_(
    ByteStringIterator *self, ComGoogleProtobufByteString *byteString);
static ByteStringIterator *create_ByteStringIterator_initWithComGoogleProtobufByteString_(
    ComGoogleProtobufByteString *byteString);

@implementation ComGoogleProtobufByteString

+ (ComGoogleProtobufByteString *)
    copyFromWithByteArray:(IOSByteArray *)bytes
    OBJC_METHOD_FAMILY_NONE {
  return ComGoogleProtobufByteString_copyFromWithByteArray_(bytes);
}

- (jbyte)byteAtWithInt:(jint)index {
  if (index < 0 || index >= size_) {
    @throw [[[JavaLangArrayIndexOutOfBoundsException alloc]
        initWithNSString:[NSString stringWithFormat:@"this.length=%d; index=%d", size_, index]]
        autorelease];
  }
  return buffer_[index];
}

- (jint)size {
  return size_;
}

- (jboolean)isEmpty {
  return size_ == 0;
}

- (ComGoogleProtobufByteString *)substringWithInt:(jint)beginIndex {
  return [self substringWithInt:beginIndex withInt:size_];
}

- (ComGoogleProtobufByteString *)substringWithInt:(jint)beginIndex withInt:(jint)endIndex {
  jint substringLength = endIndex - beginIndex;
  if (beginIndex < 0 || endIndex > size_ || substringLength < 0) {
    @throw [[[JavaLangIndexOutOfBoundsException alloc] init] autorelease];
  }
  if (substringLength == 0) {
    return ComGoogleProtobufByteString_EMPTY;
  } else {
    CGPByteString *byteString = CGPNewByteString(substringLength);
    memcpy(byteString->buffer_, buffer_ + beginIndex, substringLength);
    return [byteString autorelease];
  }
}

- (IOSByteArray *)toByteArray {
  return [IOSByteArray arrayWithBytes:buffer_ count:size_];
}

- (NSString *)toStringWithJavaNioCharsetCharset:(JavaNioCharsetCharset *)charset {
  return [NSString java_stringWithBytes:[self toByteArray] charset:charset];
}

- (NSString *)toStringWithNSString:(NSString *)charsetName {
  return [NSString java_stringWithBytes:[self toByteArray] charsetName:charsetName];
}

- (NSString *)toStringUtf8 {
  return [[[NSString alloc] initWithBytes:buffer_
                                   length:size_
                                 encoding:NSUTF8StringEncoding] autorelease];
}

- (id<ComGoogleProtobufByteString_ByteIterator>)iterator {
  return create_ByteStringIterator_initWithComGoogleProtobufByteString_(self);
}

+ (ComGoogleProtobufByteString *)readFromWithJavaIoInputStream:(JavaIoInputStream *)streamToDrain {
  return ComGoogleProtobufByteString_readFromWithJavaIoInputStream_(streamToDrain);
}

+ (ComGoogleProtobufByteString *)readFromWithJavaIoInputStream:(JavaIoInputStream *)streamToDrain
                                                       withInt:(jint)chunkSize {
  return ComGoogleProtobufByteString_readFromWithJavaIoInputStream_withInt_(
      streamToDrain, chunkSize);
}

+ (ComGoogleProtobufByteString *)readFromWithJavaIoInputStream:(JavaIoInputStream *)streamToDrain
                                                       withInt:(jint)minChunkSize
                                                       withInt:(jint)maxChunkSize {
  return ComGoogleProtobufByteString_readFromWithJavaIoInputStream_withInt_withInt_(
      streamToDrain, minChunkSize, maxChunkSize);
}

- (void)writeToWithJavaIoOutputStream:(JavaIoOutputStream *)output {
  if (size_ > 0) {
    IOSByteArray *bytes = [IOSByteArray newArrayWithBytes:buffer_ count:size_];
    @try {
      [output writeWithByteArray:bytes];
    } @finally {
      [bytes release];
    }
  }
}

- (BOOL)isEqual:(id)other {
  if (other == self) {
    return YES;
  }
  if (!(object_getClass(other) == [CGPByteString class])) {
    return NO;
  }
  CGPByteString *otherByteString = (CGPByteString *)other;
  if (size_ != otherByteString->size_) {
    return NO;
  }
  if (size_ == 0) {
    return YES;
  }
  return memcmp(buffer_, otherByteString->buffer_, size_) == 0;
}

- (void)forEachWithJavaUtilFunctionConsumer:(id<JavaUtilFunctionConsumer>)arg0 {
  JavaLangIterable_forEachWithJavaUtilFunctionConsumer_(self, arg0);
}

- (id<JavaUtilSpliterator>)spliterator {
  return JavaLangIterable_spliterator(self);
}

- (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state
                                  objects:(__unsafe_unretained id *)stackbuf
                                    count:(NSUInteger)len {
  return JreDefaultFastEnumeration(self, state, stackbuf);
}

- (NSUInteger)hash {
  jint h = size_;
  for (jint i = 0; i < size_; i++) {
    h = h * 31 + buffer_[i];
  }
  return h;
}

+ (void)initialize {
  if (self == [ComGoogleProtobufByteString class]) {
    ComGoogleProtobufByteString_EMPTY = CGPNewByteString(0);
    J2OBJC_SET_INITIALIZED(ComGoogleProtobufByteString)
  }
}

@end

// We commission the first few bytes of each byte array chunk as metadata so
// that we can chain the chunks without requiring additional memory allocation.
typedef struct ChunkData {
  jint numBytes;
  IOSByteArray *next;
} ChunkData;

static bool ReadChunk(JavaIoInputStream *input, jint chunkSize, IOSByteArray **pChunk) {
  IOSByteArray *chunk = [IOSByteArray newArrayWithLength:sizeof(ChunkData) + chunkSize];
  *pChunk = chunk;
  jint *bytesRead = &((ChunkData *)chunk->buffer_)->numBytes;
  while (*bytesRead < chunkSize) {
    jint count = [input readWithByteArray:chunk
                                  withInt:sizeof(ChunkData) + *bytesRead
                                  withInt:chunkSize - *bytesRead];
    if (count == -1) {
      return true;
    }
    *bytesRead += count;
  }
  return false;
}

static void ReleaseChunks(IOSByteArray *chunk) {
  while (chunk) {
    ChunkData *chunkData = (ChunkData *)chunk->buffer_;
    IOSByteArray *next = chunkData->next;
    [chunk release];
    chunk = next;
  }
}

ComGoogleProtobufByteString *ByteStringFromChunks(jint size, IOSByteArray *chunk) {
  CGPByteString *byteString = CGPNewByteString(size);
  void *buffer = byteString->buffer_;
  while (chunk) {
    ChunkData *chunkData = (ChunkData *)chunk->buffer_;
    memcpy(buffer, (void *)chunk->buffer_ + sizeof(ChunkData), chunkData->numBytes);
    buffer += chunkData->numBytes;
    IOSByteArray *next = chunkData->next;
    [chunk release];
    chunk = next;
  }
  return [byteString autorelease];
}

// Unlike the Java implementation, which uses RopeByteString to chain the chunks
// together, this implementation simply copies the contents of the chunks back
// into a single buffer.
ComGoogleProtobufByteString
    *ComGoogleProtobufByteString_readFromWithJavaIoInputStream_withInt_withInt_(
    JavaIoInputStream *streamToDrain, jint minChunkSize, jint maxChunkSize) {
  ComGoogleProtobufByteString_initialize();
  jint chunkSize = minChunkSize;
  jint totalBytes = 0;
  IOSByteArray *firstChunk;

  @try {
    IOSByteArray **nextChunk = &firstChunk;
    bool finished;
    do {
      finished = ReadChunk(streamToDrain, chunkSize, nextChunk);
      ChunkData *chunkData = (ChunkData *)((*nextChunk)->buffer_);
      chunkSize = MIN(chunkSize * 2, maxChunkSize);
      totalBytes += chunkData->numBytes;
      nextChunk = &chunkData->next;
    } while (!finished);
  } @catch (id e) {
    ReleaseChunks(firstChunk);
    @throw e;
  }

  if (totalBytes == 0) {
    ReleaseChunks(firstChunk);
    return ComGoogleProtobufByteString_EMPTY;
  } else {
    return ByteStringFromChunks(totalBytes, firstChunk);
  }
}

ComGoogleProtobufByteString *ComGoogleProtobufByteString_readFromWithJavaIoInputStream_(
    JavaIoInputStream *streamToDrain) {
  return ComGoogleProtobufByteString_readFromWithJavaIoInputStream_withInt_withInt_(
      streamToDrain, MIN_READ_FROM_CHUNK_SIZE, MAX_READ_FROM_CHUNK_SIZE);
}

ComGoogleProtobufByteString *ComGoogleProtobufByteString_readFromWithJavaIoInputStream_withInt_(
    JavaIoInputStream *streamToDrain, jint chunkSize) {
  return ComGoogleProtobufByteString_readFromWithJavaIoInputStream_withInt_withInt_(
      streamToDrain, chunkSize, chunkSize);
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufByteString)

@implementation ByteStringIterator

- (instancetype)initWithComGoogleProtobufByteString:(ComGoogleProtobufByteString *)byteString {
  ByteStringIterator_initWithComGoogleProtobufByteString_(self, byteString);
  return self;
}

- (jboolean)hasNext {
  return position_ < limit_;
}

- (JavaLangByte *)next {
  return JavaLangByte_valueOfWithByte_([self nextByte]);
}

- (jbyte)nextByte {
  @try {
    return [byteString_ byteAtWithInt:position_++];
  } @catch (JavaLangIndexOutOfBoundsException *e) {
    @throw create_JavaUtilNoSuchElementException_initWithNSString_([e getMessage]);
  }
}

- (void)remove {
  @throw create_JavaLangUnsupportedOperationException_init();
}

- (void)forEachRemainingWithJavaUtilFunctionConsumer:(id<JavaUtilFunctionConsumer>)arg0 {
  JavaUtilIterator_forEachRemainingWithJavaUtilFunctionConsumer_(self, arg0);
}

- (void)dealloc {
  RELEASE_(byteString_);
  [super dealloc];
}

@end

void ByteStringIterator_initWithComGoogleProtobufByteString_(
    ByteStringIterator *self, ComGoogleProtobufByteString *byteString) {
  JreStrongAssign(&self->byteString_, byteString);
  NSObject_init(self);
  self->position_ = 0;
  self->limit_ = [byteString size];
}

ByteStringIterator *create_ByteStringIterator_initWithComGoogleProtobufByteString_(
    ComGoogleProtobufByteString *byteString) {
  J2OBJC_CREATE_IMPL(ByteStringIterator, initWithComGoogleProtobufByteString_, byteString)
}
