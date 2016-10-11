// Copyright 2011 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//
//  NSString+JavaString.m
//  JreEmulation
//
//  Created by Tom Ball on 8/24/11.
//

#import "NSString+JavaString.h"

#import "IOSClass.h"
#import "J2ObjC_source.h"
#import "com/google/j2objc/nio/charset/IOSCharset.h"
#import "java/io/ObjectStreamField.h"
#import "java/io/Serializable.h"
#import "java/io/UnsupportedEncodingException.h"
#import "java/lang/AssertionError.h"
#import "java/lang/Character.h"
#import "java/lang/ClassCastException.h"
#import "java/lang/Integer.h"
#import "java/lang/Iterable.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/StringBuffer.h"
#import "java/lang/StringBuilder.h"
#import "java/lang/StringIndexOutOfBoundsException.h"
#import "java/nio/charset/Charset.h"
#import "java/nio/charset/UnsupportedCharsetException.h"
#import "java/util/Comparator.h"
#import "java/util/Formatter.h"
#import "java/util/Locale.h"
#import "java/util/Objects.h"
#import "java/util/StringJoiner.h"
#import "java/util/function/Function.h"
#import "java/util/function/ToDoubleFunction.h"
#import "java/util/function/ToIntFunction.h"
#import "java/util/function/ToLongFunction.h"
#import "java/util/regex/Matcher.h"
#import "java/util/regex/Pattern.h"
#import "java/util/regex/PatternSyntaxException.h"
#import "java/util/stream/IntStream.h"
#import "java_lang_IntegralToString.h"
#import "java_lang_RealToString.h"

#define NSString_serialVersionUID -6849794470754667710LL

@implementation NSString (JavaString)

id makeException(Class exceptionClass) {
  id exception = [[exceptionClass alloc] init];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  return exception;
}

// TODO(tball): remove static method wrappers when reflection invocation calls functions directly.
+ (NSString *)valueOf:(id<NSObject>)obj {
  return NSString_valueOf_((id)obj);
}

NSString *NSString_valueOf_(id obj) {
  return obj ? [obj description] : @"null";
}

+ (NSString *)valueOfBool:(jboolean)value {
  return NSString_valueOfBool_(value);
}

NSString *NSString_valueOfBool_(jboolean value) {
  return value ? @"true" : @"false";
}

+ (NSString *)valueOfChar:(jchar)value {
  return NSString_valueOfChar_(value);
}

NSString *NSString_valueOfChar_(jchar value) {
  return [NSString stringWithCharacters:&value length:1];
}

NSString *NSString_valueOfChars_(IOSCharArray *data) {
  return NSString_valueOfChars_offset_count_(data, 0, data->size_);
}

+ (NSString *)valueOfChars:(IOSCharArray *)data {
  return NSString_valueOfChars_(data);
}

NSString *NSString_valueOfChars_offset_count_(IOSCharArray *data, jint offset, jint count) {
  id exception = nil;
  if (offset < 0) {
    exception = [[JavaLangStringIndexOutOfBoundsException alloc]
                 initWithInt:offset];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
  }
  if (count < 0) {
    exception = [[JavaLangStringIndexOutOfBoundsException alloc]
                 initWithInt:count];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
  }
  if (offset + count > data->size_) {
    exception = [[JavaLangStringIndexOutOfBoundsException alloc]
                 initWithInt:offset];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
  }
  if (exception) {
    @throw exception;
  }
  NSString *result = [NSString stringWithCharacters:data->buffer_ + offset
                                             length:(NSUInteger)count];
  return result;
}

+ (NSString *)valueOfChars:(IOSCharArray *)data
                    offset:(int)offset
                     count:(int)count {
  return NSString_valueOfChars_offset_count_(data, offset, count);
}

+ (NSString *)valueOfDouble:(double)value {
  return NSString_valueOfDouble_(value);
}

NSString *NSString_valueOfDouble_(jdouble value) {
  return RealToString_doubleToString(value);
}

+ (NSString *)valueOfFloat:(float)value {
  return NSString_valueOfFloat_(value);
}

NSString *NSString_valueOfFloat_(jfloat value) {
  return RealToString_floatToString(value);
}

+ (NSString *)valueOfInt:(int)value {
  return NSString_valueOfInt_(value);
}

NSString *NSString_valueOfInt_(jint value) {
  return IntegralToString_convertInt(NULL, value);
}

+ (NSString *)valueOfLong:(long long int)value {
  return NSString_valueOfLong_(value);
}

NSString *NSString_valueOfLong_(jlong value) {
  return IntegralToString_convertLong(NULL, value);
}

- (void)getChars:(int)sourceBegin
       sourceEnd:(int)sourceEnd
     destination:(IOSCharArray *)destination
destinationBegin:(int)destinationBegin {
  id exception = nil;
  if (sourceBegin < 0) {
    exception = [[JavaLangStringIndexOutOfBoundsException alloc]
                 initWithInt:sourceBegin];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
  }
  if (sourceEnd > (int) [self length]) {
    exception = [[JavaLangStringIndexOutOfBoundsException alloc]
                 initWithInt:sourceEnd];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
  }
  if (sourceBegin > sourceEnd) {
    exception = [[JavaLangStringIndexOutOfBoundsException alloc]
                 initWithInt:sourceEnd - sourceBegin];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
  }
  if (exception) {
    @throw exception;
  }

  NSRange range = NSMakeRange(sourceBegin, sourceEnd - sourceBegin);
  jint destinationLength = destination->size_;
  if (destinationBegin + (jint)range.length > destinationLength) {
    exception = [[JavaLangStringIndexOutOfBoundsException alloc]
                 initWithInt:(int) (destinationBegin + range.length)];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
  }
  if (exception) {
    @throw exception;
  }

  [self getCharacters:destination->buffer_ + destinationBegin range:range];
}

+ (NSString *)stringWithCharacters:(IOSCharArray *)value {
  return [NSString stringWithCharacters:value offset:0 length:value->size_];
}

+ (NSString *)stringWithCharacters:(IOSCharArray *)value
                            offset:(int)offset
                            length:(int)count {
  id exception = nil;
  if (offset < 0) {
    exception =
        [[JavaLangStringIndexOutOfBoundsException alloc] initWithInt:offset];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
  }
  if (count < 0) {
    exception =
        [[JavaLangStringIndexOutOfBoundsException alloc] initWithInt:offset];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
  }
  if (offset > value->size_ - count) {
    exception = [[JavaLangStringIndexOutOfBoundsException alloc]
                 initWithInt:offset + count];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
  }
  if (exception) {
    @throw exception;
  }
  return [self stringWithOffset:offset
                         length:count
                     characters:value];
}

// Package-private constructor, with parameters already checked.
+ (NSString *)stringWithOffset:(int)offset
                        length:(int)count
                    characters:(IOSCharArray *)value {
  if (count == 0) {
    return [NSString string];
  }
  NSString *result = [NSString stringWithCharacters:value->buffer_ + offset
                                             length:count];
  return result;
}

+ (NSString *)stringWithJavaLangStringBuffer:(JavaLangStringBuffer *)sb {
  return [sb description];
}

+ (NSString *)stringWithJavaLangStringBuilder:(JavaLangStringBuilder *)sb {
  if (!sb) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  return [sb description];
}

- (jint)compareToWithId:(id)another {
  if (!another) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  if (![another isKindOfClass:[NSString class]]) {
    @throw makeException([JavaLangClassCastException class]);
  }
  return (jint)[self compare:(NSString *) another options:NSLiteralSearch];
}

- (jint)compareToIgnoreCase:(NSString *)another {
  if (!another) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  return (jint)[self caseInsensitiveCompare:another];
}

- (NSString *)substring:(int)beginIndex {
  if (beginIndex < 0 || beginIndex > (int) [self length]) {
    @throw AUTORELEASE([[JavaLangStringIndexOutOfBoundsException alloc]
                        initWithInt:beginIndex]);
  }
  return [self substringFromIndex:(NSUInteger) beginIndex];
}

- (NSString *)substring:(int)beginIndex
               endIndex:(int)endIndex {
  if (beginIndex < 0) {
    @throw AUTORELEASE([[JavaLangStringIndexOutOfBoundsException alloc]
                        initWithInt:beginIndex]);
  }
  if (endIndex < beginIndex) {
    @throw AUTORELEASE([[JavaLangStringIndexOutOfBoundsException alloc]
                        initWithInt:endIndex - beginIndex]);
  }
  if (endIndex > (int) [self length]) {
    @throw AUTORELEASE([[JavaLangStringIndexOutOfBoundsException alloc]
                        initWithInt:endIndex]);
  }
  NSRange range = NSMakeRange(beginIndex, endIndex - beginIndex);
  return [self substringWithRange:range];
}

- (int)indexOf:(int)ch {
  return [self indexOf:ch fromIndex:0];
}

- (int)indexOf:(int)ch fromIndex:(int)index {
  unichar c = (unichar) ch;
  NSString *s = [NSString stringWithCharacters:&c length:1];
  return [self indexOfString:s fromIndex:(int)index];
}

- (int)indexOfString:(NSString *)s {
  if (!s) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  if ([s length] == 0) {
    return 0;
  }
  NSRange range = [self rangeOfString:s];
  return range.location == NSNotFound ? -1 : (int) range.location;
}

- (int)indexOfString:(NSString *)s fromIndex:(int)index {
  if (!s) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  if ([s length] == 0) {
    return 0;
  }
  NSUInteger max = [self length];
  if ((NSUInteger) index >= max) {
    return -1;
  }
  if (index < 0) {
    index = 0;
  }
  NSRange searchRange = NSMakeRange((NSUInteger) index,
                                    max - (NSUInteger) index);
  NSRange range = [self rangeOfString:s
                              options:NSLiteralSearch
                                range:searchRange];
  return range.location == NSNotFound ? -1 : (int) range.location;
}

- (jboolean)isEmpty {
  return [self length] == 0;
}

- (int)lastIndexOf:(int)ch {
  unichar c = (unichar) ch;
  NSString *s = [NSString stringWithCharacters:&c length:1];
  return [self lastIndexOfString:s];
}

- (int)lastIndexOf:(int)ch fromIndex:(int)index {
  unichar c = (unichar) ch;
  NSString *s = [NSString stringWithCharacters:&c length:1];
  return [self lastIndexOfString:s fromIndex:(int)index];
}

- (int)lastIndexOfString:(NSString *)s {
  if (!s) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  if ([s length] == 0) {
    return (int) [self length];
  }
  NSRange range = [self rangeOfString:s options:NSBackwardsSearch];
  return range.location == NSNotFound ? -1 : (int) range.location;
}

- (int)lastIndexOfString:(NSString *)s fromIndex:(int)index {
  if (!s) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  int max = (int) [self length];
  if (index < 0) {
    return -1;
  }
  if (max == 0) {
    return max;
  }
  int sLen = (int) [s length];
  if (sLen == 0) {
    return index;
  }
  int bound = index + sLen;
  if (bound > max) {
    bound = max;
  }
  NSRange searchRange = NSMakeRange((NSUInteger) 0, (NSUInteger) bound);
  NSRange range = [self rangeOfString:s
                              options:NSBackwardsSearch
                                range:searchRange];
  return range.location == NSNotFound ? -1 : (int) range.location;
}

- (IOSCharArray *)toCharArray {
  return [IOSCharArray arrayWithNSString:self];
}

- (jchar)charAtWithInt:(jint)index {
  if (index < 0 || index >= (jint) [self length]) {
    @throw makeException([JavaLangStringIndexOutOfBoundsException class]);
  }
  return [self characterAtIndex:(NSUInteger)index];
}

- (id<JavaLangCharSequence>)subSequenceFrom:(int)start
                                         to:(int)end {
  NSUInteger maxLength = [self length];
  if (start < 0 || start > end || (NSUInteger) end > maxLength) {
    @throw makeException([JavaLangStringIndexOutOfBoundsException class]);
    return nil;
  }
  int length = end - start;
  NSRange range = NSMakeRange((NSUInteger) start, (NSUInteger) length);
  unichar *buffer = malloc(length * sizeof(unichar));
  [self getCharacters:buffer range:range];
  NSString *subString = [NSString stringWithCharacters:buffer length:length];
  free(buffer);
  return (id<JavaLangCharSequence>) subString;
}

- (NSString *)replace:(jchar)oldchar withChar:(jchar)newchar {
  CFStringRef this = (__bridge CFStringRef)self;
  CFIndex length = CFStringGetLength(this);
  unichar *chars = malloc(length * sizeof(unichar));
  CFRange range = { 0, length };
  CFStringGetCharacters(this, range, chars);
  BOOL modified = NO;
  for (CFIndex i = 0; i < length; i++) {
    if (chars[i] == oldchar) {
      chars[i] = newchar;
      modified = YES;
    }
  }
  NSString *result = modified ? [NSString stringWithCharacters:chars length:length] : self;
  free(chars);
  return result;
}

- (NSString *)replace:(id<JavaLangCharSequence>)oldSequence
         withSequence:(id<JavaLangCharSequence>)newSequence {
  NSString *oldString = [oldSequence description];
  NSString *newString = [newSequence description];
  return [self stringByReplacingOccurrencesOfString:oldString
                                         withString:newString];
}

- (NSString *)replaceAll:(NSString *)regex
         withReplacement:(NSString *)replacement {
  return [[JavaUtilRegexPattern_compileWithNSString_(regex) matcherWithJavaLangCharSequence:self]
      replaceAllWithNSString:replacement];
}


- (NSString *)replaceFirst:(NSString *)regex
           withReplacement:(NSString *)replacement {
  return [[JavaUtilRegexPattern_compileWithNSString_(regex) matcherWithJavaLangCharSequence:self]
      replaceFirstWithNSString:replacement];
}


+ (NSString *)stringWithBytes:(IOSByteArray *)value {
  NSStringEncoding encoding = [NSString defaultCStringEncoding];
  return [self stringWithBytes:value
                        offset:0
                        length:value->size_
               encoding:encoding];
}

+ (NSString *)stringWithBytes:(IOSByteArray *)value
                  charsetName:(NSString *)charsetName {
  return [self stringWithBytes:value
                        offset:0
                        length:value->size_
                   charsetName:charsetName];
}

+ (NSString *)stringWithBytes:(IOSByteArray *)value
                      charset:(JavaNioCharsetCharset *)charset {
  return [self stringWithBytes:value
                        offset:0
                        length:value->size_
                       charset:charset];
}

NSStringEncoding parseCharsetName(NSString *charset) {
  JavaNioCharsetCharset *cs = JavaNioCharsetCharset_forNameUEEWithNSString_(charset);
  return (NSStringEncoding)[(ComGoogleJ2objcNioCharsetIOSCharset *)cs nsEncoding];
}

+ (NSString *)stringWithBytes:(IOSByteArray *)value
                       hibyte:(int)hibyte {
  return [NSString stringWithBytes:value
                            hibyte:hibyte
                            offset:0
                            length:value->size_];
}


+ (NSString *)stringWithBytes:(IOSByteArray *)value
                       offset:(int)offset
                       length:(int)count {
  return [NSString stringWithBytes:value
                            offset:offset
                            length:count
                      encoding:NSUTF8StringEncoding];
}

+ (NSString *)stringWithBytes:(IOSByteArray *)value
                       offset:(int)offset
                       length:(int)count
                  charsetName:(NSString *)charset {
  NSStringEncoding encoding = parseCharsetName(charset);
  return [NSString stringWithBytes:value
                            offset:offset
                            length:count
                      encoding:encoding];
}

+ (NSString *)stringWithBytes:(IOSByteArray *)value
                       offset:(int)offset
                       length:(int)count
                      charset:(JavaNioCharsetCharset *)charset {
  if (![charset isKindOfClass:[ComGoogleJ2objcNioCharsetIOSCharset class]]) {
    @throw AUTORELEASE([[JavaNioCharsetUnsupportedCharsetException alloc]
                        initWithNSString:[charset description]]);
  }
  ComGoogleJ2objcNioCharsetIOSCharset *iosCharset = (ComGoogleJ2objcNioCharsetIOSCharset *) charset;
  NSStringEncoding encoding = (NSStringEncoding) [iosCharset nsEncoding];
  return [NSString stringWithBytes:value
                            offset:offset
                            length:count
                          encoding:encoding];
}

+ (NSString *)stringWithBytes:(IOSByteArray *)value
                       hibyte:(NSUInteger)hibyte
                       offset:(NSUInteger)offset
                       length:(NSUInteger)length {
  jbyte *bytes = value->buffer_;
  unichar *chars = malloc(length * sizeof(unichar));
  for (NSUInteger i = 0; i < length; i++) {
    jbyte b = bytes[i + offset];
    // Expression from String(byte[],int) javadoc.
    chars[i] = (unichar)(((hibyte & 0xff) << 8) | (b & 0xff));
  }
  NSString *s = [NSString stringWithCharacters:chars length:length];
  free(chars);
  return s;
}

+ (NSString *)stringWithBytes:(IOSByteArray *)value
                       offset:(int)offset
                       length:(int)count
                 encoding:(NSStringEncoding)encoding {
  id exception = nil;
  if (offset < 0) {
    exception =
        [[JavaLangStringIndexOutOfBoundsException alloc] initWithInt:offset];
  }
  if (count < 0) {
    exception =
        [[JavaLangStringIndexOutOfBoundsException alloc] initWithInt:offset];
  }
  if (offset > (int) value->size_ - count) {
    exception = [[JavaLangStringIndexOutOfBoundsException alloc]
                 initWithInt:offset + count];
  }
  if (exception) {
    @throw [exception autorelease];
  }

  return [[[NSString alloc] initWithBytes:value->buffer_ + offset
                                   length:count
                                 encoding:encoding] autorelease];
}

+ (NSString *)stringWithInts:(IOSIntArray *)codePoints
                      offset:(int)offset
                      length:(int)count {
  if (!codePoints) {
    @throw create_JavaLangNullPointerException_initWithNSString_(@"codePoints == null");
  }
  jint ncps = codePoints->size_;
  if ((offset | count) < 0 || count > ncps - offset) {
    @throw create_JavaLangStringIndexOutOfBoundsException_initWithInt_withInt_withInt_(
        ncps, offset, count);
  }
  IOSCharArray *value = [IOSCharArray arrayWithLength:count * 2];
  jint end = offset + count;
  jint length = 0;
  for (jint i = offset; i < end; i++) {
    length += JavaLangCharacter_toCharsWithInt_withCharArray_withInt_(
        codePoints->buffer_[i], value, length);
  }
  return [NSString stringWithCharacters:value->buffer_ length:length];
}

- (IOSByteArray *)getBytes  {
  JavaNioCharsetCharset *charset = JavaNioCharsetCharset_defaultCharset();
  NSStringEncoding encoding =
      (NSStringEncoding)[(ComGoogleJ2objcNioCharsetIOSCharset *)charset nsEncoding];
  return [self getBytesWithEncoding:encoding];
}

- (IOSByteArray *)getBytesWithCharsetName:(NSString *)charsetName {
  if (!charsetName) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  NSStringEncoding encoding = parseCharsetName(charsetName);
  return [self getBytesWithEncoding:encoding];
}

- (IOSByteArray *)getBytesWithCharset:(JavaNioCharsetCharset *)charset {
  nil_chk(charset);
  if (![charset isKindOfClass:[ComGoogleJ2objcNioCharsetIOSCharset class]]) {
    @throw AUTORELEASE([[JavaNioCharsetUnsupportedCharsetException alloc]
                        initWithNSString:[charset description]]);
  }
  ComGoogleJ2objcNioCharsetIOSCharset *iosCharset = (ComGoogleJ2objcNioCharsetIOSCharset *) charset;
  NSStringEncoding encoding = (NSStringEncoding) [iosCharset nsEncoding];
  return [self getBytesWithEncoding:encoding];
}

- (IOSByteArray *)getBytesWithEncoding:(NSStringEncoding)encoding {
  if (!encoding) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  int max_length = (int) [self maximumLengthOfBytesUsingEncoding:encoding];
  jboolean includeBOM = (encoding == NSUTF16StringEncoding);
  if (includeBOM) {
    max_length += 2;
    encoding = NSUTF16BigEndianStringEncoding;  // Java uses big-endian.
  }
  char *buffer = (char *)malloc(max_length * sizeof(char));
  char *p = buffer;
  if (includeBOM) {
    *p++ = (char) 0xFE;
    *p++ = (char) 0xFF;
    max_length -= 2;
  }
  NSRange range = NSMakeRange(0, [self length]);
  NSUInteger used_length;
  [self getBytes:p
       maxLength:max_length
      usedLength:&used_length
        encoding:encoding
         options:0
           range:range
  remainingRange:NULL];
  if (includeBOM) {
    used_length += 2;
  }
  IOSByteArray *result = [IOSByteArray arrayWithBytes:(jbyte *)buffer
                                                count:(jint)used_length];
  free(buffer);
  return result;
}

- (void)getBytesWithSrcBegin:(int)srcBegin
                  withSrcEnd:(int)srcEnd
                     withDst:(IOSByteArray *)dst
                withDstBegin:(int)dstBegin {
  int copyLength = srcEnd - srcBegin;
  NSString *badParamMsg = nil;
  if (srcBegin < 0) {
    badParamMsg = @"srcBegin < 0";
  } else if (srcBegin > srcEnd) {
    badParamMsg = @"srcBegin > srcEnd";
  } else if (srcEnd > (int) [self length]) {
    badParamMsg = @"srcEnd > string length";
  } else if (copyLength > (int) [self length]) {
    badParamMsg = @"dstBegin+(srcEnd-srcBegin) > dst.length";
  }
  if (badParamMsg) {
    @throw AUTORELEASE([[JavaLangStringIndexOutOfBoundsException alloc]
                        initWithNSString:badParamMsg]);
  }
  if (!dst) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  NSUInteger maxBytes =
      [self maximumLengthOfBytesUsingEncoding:NSUTF8StringEncoding];
  char *bytes = (char *)malloc(maxBytes);
  NSUInteger bytesUsed;
  NSRange range = NSMakeRange(srcBegin, srcEnd - srcBegin);
  [self getBytes:bytes
       maxLength:maxBytes
      usedLength:&bytesUsed
        encoding:NSUTF8StringEncoding
         options:0
           range:range
  remainingRange:NULL];

  // Double-check there won't be a buffer overflow, since the encoded length
  // of the copied substring is now known.
  if ((jint)bytesUsed > (dst->size_ - dstBegin)) {
    free(bytes);
    @throw AUTORELEASE(
        [[JavaLangStringIndexOutOfBoundsException alloc]
         initWithNSString:@"dstBegin+(srcEnd-srcBegin) > dst.length"]);
  }
  [dst replaceBytes:(jbyte *)bytes length:(jint)bytesUsed offset:dstBegin];
  free(bytes);
}

NSString *NSString_formatWithNSString_withNSObjectArray_(NSString *format, IOSObjectArray *args) {
  JavaUtilFormatter *formatter = [[JavaUtilFormatter alloc] init];
  NSString *result = [[formatter formatWithNSString:format withNSObjectArray:args] description];
  RELEASE_(formatter);
  return result;
}

+ (NSString *)formatWithNSString:(NSString *)format withNSObjectArray:(IOSObjectArray *)args {
  return NSString_formatWithNSString_withNSObjectArray_(format, args);
}

NSString *NSString_formatWithJavaUtilLocale_withNSString_withNSObjectArray_(
    JavaUtilLocale *locale, NSString *format, IOSObjectArray *args) {
  JavaUtilFormatter *formatter =
      AUTORELEASE([[JavaUtilFormatter alloc] initWithJavaUtilLocale:locale]);
  return [[formatter formatWithNSString:format withNSObjectArray:args] description];
}

+ (NSString *)formatWithJavaUtilLocale:(JavaUtilLocale *)locale
                          withNSString:(NSString *)format
                     withNSObjectArray:(IOSObjectArray *)args {
  return NSString_formatWithJavaUtilLocale_withNSString_withNSObjectArray_(locale, format, args);
}

- (jboolean)hasPrefix:(NSString *)aString offset:(int)offset {
  if (!aString) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  NSRange range = NSMakeRange(offset, [aString length]);
  return [self compare:aString
               options:NSLiteralSearch
                 range:range] == NSOrderedSame;
}

- (NSString *)trim {
  // Java's String.trim() trims characters <= u0020, not NSString whitespace.
  NSCharacterSet *trimCharacterSet = [NSCharacterSet characterSetWithRange:NSMakeRange(0, 0x21)];
  return [self stringByTrimmingCharactersInSet:trimCharacterSet];
}

- (IOSObjectArray *)split:(NSString *)str {
  if (!str) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  return [self split:str limit:0];
}

- (IOSObjectArray *)split:(NSString *)str limit:(int)n {
  if (!str) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  JavaUtilRegexPattern *p = JavaUtilRegexPattern_compileWithNSString_(str);
  return [p splitWithJavaLangCharSequence:self withInt:n];
}

- (jboolean)equalsIgnoreCase:(NSString *)aString {
  NSComparisonResult result =
      [self compare:aString options:NSCaseInsensitiveSearch];
  return result == NSOrderedSame;
}

- (NSString *)lowercaseStringWithJRELocale:(JavaUtilLocale *)javaLocale {
  if (!javaLocale) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  NSLocale* locale =
      [[NSLocale alloc] initWithLocaleIdentifier:[javaLocale description]];
#if ! __has_feature(objc_arc)
  [locale autorelease];
#endif
  CFMutableStringRef tempCFMString =
      CFStringCreateMutableCopy(NULL, 0, (ARCBRIDGE CFStringRef)self);
  CFStringLowercase(tempCFMString, (ARCBRIDGE CFLocaleRef)locale);
  NSString *result = [(ARCBRIDGE NSString*)tempCFMString copy];
  CFRelease(tempCFMString);
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (NSString *)uppercaseStringWithJRELocale:(JavaUtilLocale *)javaLocale {
  if (!javaLocale) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  NSLocale* locale =
      [[NSLocale alloc] initWithLocaleIdentifier:[javaLocale description]];
#if ! __has_feature(objc_arc)
  [locale autorelease];
#endif
  CFMutableStringRef tempCFMString =
      CFStringCreateMutableCopy(NULL, 0, (ARCBRIDGE CFStringRef)self);
  CFStringUppercase(tempCFMString, (ARCBRIDGE CFLocaleRef)locale);
  NSString *result = [(ARCBRIDGE NSString*)tempCFMString copy];
  CFRelease(tempCFMString);
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (jboolean)regionMatches:(int)thisOffset
                  aString:(NSString *)aString
              otherOffset:(int)otherOffset
                    count:(int)count {
  return [self regionMatches:false
                  thisOffset:thisOffset
                     aString:aString
                 otherOffset:otherOffset
                       count:count];
}

- (jboolean)regionMatches:(jboolean)caseInsensitive
               thisOffset:(int)thisOffset
                  aString:(NSString *)aString
              otherOffset:(int)otherOffset
                    count:(int)count {
  if (thisOffset < 0 || count > (int) [self length] - thisOffset) {
    return false;
  }
  if (otherOffset < 0 || count > (int) [aString length] - otherOffset) {
    return false;
  }
  if (!aString) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  NSString *this_ = (thisOffset == 0 && count == (int) [self length])
      ? self : [self substringWithRange:NSMakeRange(thisOffset, count)];
  NSString *other = (otherOffset == 0 && count == (int) [aString length])
      ? aString : [aString substringWithRange:NSMakeRange(otherOffset, count)];
  NSUInteger options = NSLiteralSearch;
  if (caseInsensitive) {
    options |= NSCaseInsensitiveSearch;
  }
  return [this_ compare:other
                options:options] == NSOrderedSame;
}

- (NSString *)intern {
  // No actual interning is done, since NSString doesn't support it.
  // Instead, any "string == otherString" expression is changed to
  // "string.equals(otherString)
  return self;
}

- (NSString *)concat:string {
  if (!string) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  return [self stringByAppendingString:string];
}

- (jboolean)contains:(id<JavaLangCharSequence>)sequence {
  if (!sequence) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  if ([sequence length] == 0) {
    return true;
  }
  NSRange range = [self rangeOfString:[sequence description]];
  return range.location != NSNotFound;
}

- (int)codePointAt:(int)index {
  return JavaLangCharacter_codePointAtWithJavaLangCharSequence_withInt_(self, index);
}

- (int)codePointBefore:(int)index {
  return JavaLangCharacter_codePointBeforeWithJavaLangCharSequence_withInt_(self, index);
}

- (int)codePointCount:(int)beginIndex endIndex:(int)endIndex {
  return JavaLangCharacter_codePointCountWithJavaLangCharSequence_withInt_withInt_(
      self, beginIndex, endIndex);
}

- (int)offsetByCodePoints:(int)index codePointOffset:(int)offset {
  return JavaLangCharacter_offsetByCodePointsWithJavaLangCharSequence_withInt_withInt_(
      self, index, offset);
}

- (jboolean)matches:(NSString *)regex {
  return JavaUtilRegexPattern_matchesWithNSString_withNSString_(regex, self);
}

- (jboolean)contentEqualsCharSequence:(id<JavaLangCharSequence>)seq {
  return [self isEqualToString:[(id) seq description]];
}

- (jboolean)contentEqualsStringBuffer:(JavaLangStringBuffer *)sb {
  return [self isEqualToString:[sb description]];
}

- (IOSClass *)getClass {
  return NSString_class_();
}

+ (NSString *)joinWithJavaLangCharSequence:(id<JavaLangCharSequence>)delimiter
             withJavaLangCharSequenceArray:(IOSObjectArray *)elements {
  return NSString_joinWithJavaLangCharSequence_withJavaLangCharSequenceArray_(delimiter, elements);
}

+ (NSString *)joinWithJavaLangCharSequence:(id<JavaLangCharSequence>)delimiter
                      withJavaLangIterable:(id<JavaLangIterable>)elements {
  return NSString_joinWithJavaLangCharSequence_withJavaLangIterable_(delimiter, elements);
}


jint javaStringHashCode(NSString *string) {
  static const char *hashKey = "__JAVA_STRING_HASH_CODE_KEY__";
  id cachedHash = objc_getAssociatedObject(string, hashKey);
  if (cachedHash) {
    return (jint) [(JavaLangInteger *) cachedHash intValue];
  }
  jint len = (jint)[string length];
  jint hash = 0;
  if (len > 0) {
    unichar *chars = (unichar *)malloc(len * sizeof(unichar));
    [string getCharacters:chars range:NSMakeRange(0, len)];
    for (int i = 0; i < len; i++) {
      hash = 31 * hash + (int)chars[i];
    }
    free(chars);
  }
  if (![string isKindOfClass:[NSMutableString class]]) {
    // Only cache hash for immutable strings.
    objc_setAssociatedObject(string, hashKey, JavaLangInteger_valueOfWithInt_(hash),
                             OBJC_ASSOCIATION_RETAIN);
  }
  return hash;
}

// Java 8 default methods from CharSequence.
- (id<JavaUtilStreamIntStream>)chars {
  return JavaLangCharSequence_chars(self);
}

- (id<JavaUtilStreamIntStream>)codePoints {
  return JavaLangCharSequence_codePoints(self);
}

+ (const J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { NULL, NULL, 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 0, -1, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 1, -1, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 2, -1, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 3, -1, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 4, 5, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 6, -1, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 7, -1, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 8, 5, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 9, -1, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 10, -1, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 11, -1, -1, -1, -1 },
    { NULL, NULL, 0x0, -1, 12, -1, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 13, -1, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 14, -1, -1, -1, -1 },
    { NULL, NULL, 0x1, -1, 15, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x9, 16, 9, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x9, 16, 10, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x89, 17, 18, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x89, 17, 19, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x9, 20, 21, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x9, 20, 22, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x9, 20, 9, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x9, 20, 10, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x9, 20, 23, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x9, 20, 24, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x9, 20, 25, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x9, 20, 26, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x9, 20, 27, -1, -1, -1, -1 },
    { NULL, "C", 0x1, 28, 25, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 29, 25, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 30, 25, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 31, 32, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 33, 13, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 34, 13, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, 35, 13, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, 36, 37, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, 38, 13, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, 39, 13, -1, -1, -1, -1 },
    { NULL, "[B", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "[B", 0x1, 40, 41, -1, -1, -1, -1 },
    { NULL, "[B", 0x1, 40, 13, 5, -1, -1, -1 },
    { NULL, "V", 0x1, 40, 42, -1, -1, -1, -1 },
    { NULL, "V", 0x1, 43, 44, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 45, 25, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 45, 32, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 45, 13, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 45, 46, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 47, 25, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 47, 32, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 47, 13, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 47, 46, -1, -1, -1, -1 },
    { NULL, "I", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, 48, 13, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 49, 32, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, 50, 51, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, 50, 52, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, 53, 54, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, 53, 55, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, 56, 57, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, 58, 57, -1, -1, -1, -1 },
    { NULL, "[LNSString;", 0x1, 59, 13, -1, -1, -1, -1 },
    { NULL, "[LNSString;", 0x1, 59, 46, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, 60, 13, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, 60, 46, -1, -1, -1, -1 },
    { NULL, "LJavaLangCharSequence;", 0x1, 61, 32, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, 62, 25, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, 62, 32, -1, -1, -1, -1 },
    { NULL, "[C", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, 63, -1, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, 63, 64, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, 65, -1, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, 65, 64, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, 66, 37, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, 66, 14, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x89, 67, 68, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x9, 67, 69, -1, 70, -1, -1 },
  };
  #pragma clang diagnostic push
  #pragma clang diagnostic ignored "-Wobjc-multiple-method-names"
  methods[0].selector = @selector(string);
  methods[1].selector = @selector(stringWithBytes:);
  methods[2].selector = @selector(stringWithBytes:hibyte:);
  methods[3].selector = @selector(stringWithBytes:offset:length:);
  methods[4].selector = @selector(stringWithBytes:hibyte:offset:length:);
  methods[5].selector = @selector(stringWithBytes:offset:length:charsetName:);
  methods[6].selector = @selector(stringWithBytes:offset:length:charset:);
  methods[7].selector = @selector(stringWithBytes:charset:);
  methods[8].selector = @selector(stringWithBytes:charsetName:);
  methods[9].selector = @selector(stringWithCharacters:);
  methods[10].selector = @selector(stringWithCharacters:offset:length:);
  methods[11].selector = @selector(stringWithInts:offset:length:);
  methods[12].selector = @selector(stringWithOffset:length:characters:);
  methods[13].selector = @selector(stringWithString:);
  methods[14].selector = @selector(stringWithJavaLangStringBuffer:);
  methods[15].selector = @selector(stringWithJavaLangStringBuilder:);
  methods[16].selector = @selector(valueOfChars:);
  methods[17].selector = @selector(valueOfChars:offset:count:);
  methods[18].selector = @selector(formatWithJavaUtilLocale:withNSString:withNSObjectArray:);
  methods[19].selector = @selector(formatWithNSString:withNSObjectArray:);
  methods[20].selector = @selector(valueOfBool:);
  methods[21].selector = @selector(valueOfChar:);
  methods[22].selector = @selector(valueOfChars:);
  methods[23].selector = @selector(valueOfChars:offset:count:);
  methods[24].selector = @selector(valueOfDouble:);
  methods[25].selector = @selector(valueOfFloat:);
  methods[26].selector = @selector(valueOfInt:);
  methods[27].selector = @selector(valueOfLong:);
  methods[28].selector = @selector(valueOf:);
  methods[29].selector = @selector(charAtWithInt:);
  methods[30].selector = @selector(codePointAt:);
  methods[31].selector = @selector(codePointBefore:);
  methods[32].selector = @selector(codePointCount:endIndex:);
  methods[33].selector = @selector(compareToWithId:);
  methods[34].selector = @selector(compareToIgnoreCase:);
  methods[35].selector = @selector(concat:);
  methods[36].selector = @selector(contains:);
  methods[37].selector = @selector(hasSuffix:);
  methods[38].selector = @selector(equalsIgnoreCase:);
  methods[39].selector = @selector(getBytes);
  methods[40].selector = @selector(getBytesWithCharset:);
  methods[41].selector = @selector(getBytesWithCharsetName:);
  methods[42].selector = @selector(getBytesWithSrcBegin:withSrcEnd:withDst:withDstBegin:);
  methods[43].selector = @selector(getChars:sourceEnd:destination:destinationBegin:);
  methods[44].selector = @selector(indexOf:);
  methods[45].selector = @selector(indexOf:fromIndex:);
  methods[46].selector = @selector(indexOfString:);
  methods[47].selector = @selector(indexOfString:fromIndex:);
  methods[48].selector = @selector(intern);
  methods[49].selector = @selector(isEmpty);
  methods[50].selector = @selector(lastIndexOf:);
  methods[51].selector = @selector(lastIndexOf:fromIndex:);
  methods[52].selector = @selector(lastIndexOfString:);
  methods[53].selector = @selector(lastIndexOfString:fromIndex:);
  methods[54].selector = @selector(length);
  methods[55].selector = @selector(matches:);
  methods[56].selector = @selector(offsetByCodePoints:codePointOffset:);
  methods[57].selector = @selector(regionMatches:thisOffset:aString:otherOffset:count:);
  methods[58].selector = @selector(regionMatches:aString:otherOffset:count:);
  methods[59].selector = @selector(replace:withChar:);
  methods[60].selector = @selector(replace:withSequence:);
  methods[61].selector = @selector(replaceAll:withReplacement:);
  methods[62].selector = @selector(replaceFirst:withReplacement:);
  methods[63].selector = @selector(split:);
  methods[64].selector = @selector(split:limit:);
  methods[65].selector = @selector(hasPrefix:);
  methods[66].selector = @selector(hasPrefix:offset:);
  methods[67].selector = @selector(subSequenceFrom:to:);
  methods[68].selector = @selector(substring:);
  methods[69].selector = @selector(substring:endIndex:);
  methods[70].selector = @selector(toCharArray);
  methods[71].selector = @selector(lowercaseString);
  methods[72].selector = @selector(lowercaseStringWithJRELocale:);
  methods[73].selector = @selector(uppercaseString);
  methods[74].selector = @selector(uppercaseStringWithJRELocale:);
  methods[75].selector = @selector(trim);
  methods[76].selector = @selector(contentEqualsCharSequence:);
  methods[77].selector = @selector(contentEqualsStringBuffer:);
  methods[78].selector = @selector(joinWithJavaLangCharSequence:withJavaLangCharSequenceArray:);
  methods[79].selector = @selector(joinWithJavaLangCharSequence:withJavaLangIterable:);
  #pragma clang diagnostic pop
  static const J2ObjcFieldInfo fields[] = {
    { "CASE_INSENSITIVE_ORDER", "LJavaUtilComparator;", .constantValue.asLong = 0, 0x19, -1, 71, 72,
      -1 },
    { "serialVersionUID", "J", .constantValue.asLong = NSString_serialVersionUID, 0x1a, -1, -1, -1,
      -1 },
    { "serialPersistentFields", "[LJavaIoObjectStreamField;", .constantValue.asLong = 0, 0x1a, -1,
      73, -1, -1 },
  };
  static const void *ptrTable[] = {
    "[B", "[BI", "[BII", "[BIII", "[BIILNSString;", "LJavaIoUnsupportedEncodingException;",
    "[BIILJavaNioCharsetCharset;", "[BLJavaNioCharsetCharset;", "[BLNSString;", "[C", "[CII",
    "[III", "II[C", "LNSString;", "LJavaLangStringBuffer;", "LJavaLangStringBuilder;",
    "copyValueOf", "format", "LJavaUtilLocale;LNSString;[LNSObject;", "LNSString;[LNSObject;",
    "valueOf", "Z", "C", "D", "F", "I", "J", "LNSObject;", "charAt", "codePointAt",
    "codePointBefore", "codePointCount", "II", "compareTo", "compareToIgnoreCase", "concat",
    "contains", "LJavaLangCharSequence;", "endsWith", "equalsIgnoreCase", "getBytes",
    "LJavaNioCharsetCharset;", "II[BI", "getChars", "II[CI", "indexOf", "LNSString;I",
    "lastIndexOf", "matches", "offsetByCodePoints", "regionMatches", "ZILNSString;II",
    "ILNSString;II", "replace", "CC", "LJavaLangCharSequence;LJavaLangCharSequence;", "replaceAll",
    "LNSString;LNSString;", "replaceFirst", "split", "startsWith", "subSequence", "substring",
    "toLowerCase", "LJavaUtilLocale;", "toUpperCase", "contentEquals", "join",
    "LJavaLangCharSequence;[LJavaLangCharSequence;", "LJavaLangCharSequence;LJavaLangIterable;",
    "(Ljava/lang/CharSequence;Ljava/lang/Iterable<+Ljava/lang/CharSequence;>;)Ljava/lang/String;",
    &NSString_CASE_INSENSITIVE_ORDER, "Ljava/util/Comparator<Ljava/lang/String;>;",
    &NSString_serialPersistentFields, "LNSString_CaseInsensitiveComparator;",
    "Ljava/lang/Object;Ljava/lang/CharSequence;Ljava/lang/Comparable<Ljava/lang/String;>;"
    "Ljava/io/Serializable;" };
  static const J2ObjcClassInfo _NSString = {
    "String", "java.lang", ptrTable, methods, fields, 7, 0x1, 80, 3, -1, 74, -1, 75, -1 };
  return &_NSString;
}

@end

NSString *NSString_joinWithJavaLangCharSequence_withJavaLangCharSequenceArray_(
    id<JavaLangCharSequence> delimiter, IOSObjectArray *elements) {
  NSString_initialize();
  JavaUtilObjects_requireNonNullWithId_(delimiter);
  JavaUtilObjects_requireNonNullWithId_(elements);
  JavaUtilStringJoiner *joiner =
      create_JavaUtilStringJoiner_initWithJavaLangCharSequence_(delimiter);
  id<JavaLangCharSequence> const *element = elements->buffer_;
  id<JavaLangCharSequence> const *end = element + elements->size_;
  while (element < end) {
    id<JavaLangCharSequence> cs = *element++;
    [joiner addWithJavaLangCharSequence:cs];
  }
  return [joiner description];
}

NSString *NSString_joinWithJavaLangCharSequence_withJavaLangIterable_(
    id<JavaLangCharSequence> delimiter, id<JavaLangIterable> elements) {
  NSString_initialize();
  JavaUtilObjects_requireNonNullWithId_(delimiter);
  JavaUtilObjects_requireNonNullWithId_(elements);
  JavaUtilStringJoiner *joiner =
      create_JavaUtilStringJoiner_initWithJavaLangCharSequence_(delimiter);
  for (id<JavaLangCharSequence> __strong cs in elements) {
    [joiner addWithJavaLangCharSequence:cs];
  }
  return [joiner description];
}

#define NSString_CaseInsensitiveComparator_serialVersionUID 8575799808933029326LL

@interface NSString_CaseInsensitiveComparator : NSObject
    < JavaUtilComparator, JavaIoSerializable >
@end

@implementation NSString_CaseInsensitiveComparator

- (int)compareWithId:(NSString *)o1
              withId:(NSString *)o2 {
  if (!o1) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  return [o1 compareToIgnoreCase:o2];
}

// Java 8 default methods from Comparator.
- (id<JavaUtilComparator>)reversed {
  return JavaUtilComparator_reversed(self);
}

- (id<JavaUtilComparator>)thenComparingWithJavaUtilComparator:(id<JavaUtilComparator>)arg0 {
  return JavaUtilComparator_thenComparingWithJavaUtilComparator_(self, arg0);
}

- (id<JavaUtilComparator>)thenComparingWithJavaUtilFunctionFunction:
    (id<JavaUtilFunctionFunction>)arg0 {
  return JavaUtilComparator_thenComparingWithJavaUtilFunctionFunction_(self, arg0);
}

- (id<JavaUtilComparator>)thenComparingWithJavaUtilFunctionFunction:
    (id<JavaUtilFunctionFunction>)arg0 withJavaUtilComparator:(id<JavaUtilComparator>)arg1 {
  return JavaUtilComparator_thenComparingWithJavaUtilFunctionFunction_withJavaUtilComparator_(
      self, arg0, arg1);
}

- (id<JavaUtilComparator>)thenComparingDoubleWithJavaUtilFunctionToDoubleFunction:
    (id<JavaUtilFunctionToDoubleFunction>)arg0 {
  return JavaUtilComparator_thenComparingDoubleWithJavaUtilFunctionToDoubleFunction_(self, arg0);
}

- (id<JavaUtilComparator>)thenComparingIntWithJavaUtilFunctionToIntFunction:
    (id<JavaUtilFunctionToIntFunction>)arg0 {
  return JavaUtilComparator_thenComparingIntWithJavaUtilFunctionToIntFunction_(self, arg0);
}

- (id<JavaUtilComparator>)thenComparingLongWithJavaUtilFunctionToLongFunction:
    (id<JavaUtilFunctionToLongFunction>)arg0 {
  return JavaUtilComparator_thenComparingLongWithJavaUtilFunctionToLongFunction_(self, arg0);
}

+ (const J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { NULL, "I", 0x1, 0, 1, -1, -1, -1, -1 },
    { NULL, NULL, 0x2, -1, -1, -1, -1, -1, -1 },
  };
  #pragma clang diagnostic push
  #pragma clang diagnostic ignored "-Wobjc-multiple-method-names"
  methods[0].selector = @selector(compareWithId:withId:);
  methods[1].selector = @selector(init);
  #pragma clang diagnostic pop
  static const J2ObjcFieldInfo fields[] = {
    { "serialVersionUID", "J",
      .constantValue.asLong = NSString_CaseInsensitiveComparator_serialVersionUID, 0x1a, -1, -1, -1,
      -1 },
  };
  static const void *ptrTable[] = {
    "compare", "LNSString;LNSString;", "LNSString;",
    "Ljava/lang/Object;Ljava/util/Comparator<Ljava/lang/String;>;Ljava/io/Serializable;" };
  static const J2ObjcClassInfo _NSString_CaseInsensitiveComparator = {
    "CaseInsensitiveComparator", "java.lang", ptrTable, methods, fields, 7, 0xa, 2, 1, 2, -1, -1, 3,
    -1 };
  return &_NSString_CaseInsensitiveComparator;
}

@end

J2OBJC_INITIALIZED_DEFN(NSString)

id<JavaUtilComparator> NSString_CASE_INSENSITIVE_ORDER;
IOSObjectArray *NSString_serialPersistentFields;

@implementation JreStringCategoryDummy

+ (void)initialize {
  if (self == [JreStringCategoryDummy class]) {
    JreStrongAssignAndConsume(&NSString_CASE_INSENSITIVE_ORDER,
        [[NSString_CaseInsensitiveComparator alloc] init]);
    JreStrongAssignAndConsume(&NSString_serialPersistentFields,
        [IOSObjectArray newArrayWithLength:0 type:JavaIoObjectStreamField_class_()]);
    J2OBJC_SET_INITIALIZED(NSString)
  }
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(NSString)
