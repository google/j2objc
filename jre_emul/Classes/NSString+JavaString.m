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
#import "JreEmulation.h"
#import "IOSClass.h"
#import "java/io/ObjectStreamField.h"
#import "java/io/Serializable.h"
#import "java/io/UnsupportedEncodingException.h"
#import "java/lang/AssertionError.h"
#import "java/lang/Character.h"
#import "java/lang/ClassCastException.h"
#import "java/lang/Integer.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/StringBuffer.h"
#import "java/lang/StringBuilder.h"
#import "java/lang/StringIndexOutOfBoundsException.h"
#import "java/nio/charset/Charset.h"
#import "java/nio/charset/IOSCharset.h"
#import "java/nio/charset/UnsupportedCharsetException.h"
#import "java/util/Comparator.h"
#import "java/util/Formatter.h"
#import "java/util/Locale.h"
#import "java/util/regex/Pattern.h"
#import "java/util/regex/PatternSyntaxException.h"
#import "java_lang_IntegralToString.h"
#import "java_lang_RealToString.h"

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
  return NSString_valueOfWithId_(obj);
}

NSString *NSString_valueOfWithId_(id<NSObject> obj) {
  return obj ? [obj description] : @"null";
}

+ (NSString *)valueOfBool:(BOOL)value {
  return NSString_valueOfWithBoolean_(value);
}

NSString *NSString_valueOfWithBoolean_(BOOL value) {
  return value ? @"true" : @"false";
}

+ (NSString *)valueOfChar:(unichar)value {
  return NSString_valueOfWithChar_(value);
}

NSString *NSString_valueOfWithChar_(unichar value) {
  return [NSString stringWithCharacters:&value length:1];
}

NSString *NSString_valueOfWithCharArray_(IOSCharArray *data) {
  return NSString_copyValueOfWithCharArray_withInt_withInt_(data, 0, data->size_);
}

NSString *NSString_copyValueOfWithCharArray_(IOSCharArray *data) {
  return NSString_valueOfWithCharArray_(data);
}

+ (NSString *)valueOfChars:(IOSCharArray *)data {
  return NSString_valueOfWithCharArray_(data);
}

NSString *NSString_valueOfWithCharArray_withInt_withInt_(
    IOSCharArray *data, jint offset, jint count) {
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

NSString *NSString_copyValueOfWithCharArray_withInt_withInt_(
    IOSCharArray *data, jint offset, jint count) {
  return NSString_valueOfWithCharArray_withInt_withInt_(data, offset, count);
}

+ (NSString *)valueOfChars:(IOSCharArray *)data
                    offset:(int)offset
                     count:(int)count {
  return NSString_valueOfWithCharArray_withInt_withInt_(data, offset, count);
}

+ (NSString *)valueOfDouble:(double)value {
  return NSString_valueOfWithDouble_(value);
}

NSString *NSString_valueOfWithDouble_(jdouble value) {
  return RealToString_doubleToString(value);
}

+ (NSString *)valueOfFloat:(float)value {
  return NSString_valueOfWithFloat_(value);
}

NSString *NSString_valueOfWithFloat_(jfloat value) {
  return RealToString_floatToString(value);
}

+ (NSString *)valueOfInt:(int)value {
  return NSString_valueOfWithInt_(value);
}

NSString *NSString_valueOfWithInt_(jint value) {
  return IntegralToString_convertInt(NULL, value);
}

+ (NSString *)valueOfLong:(long long int)value {
  return NSString_valueOfWithLong_(value);
}

NSString *NSString_valueOfWithLong_(jlong value) {
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

- (int)compareToWithId:(id)another {
  if (!another) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  if (![another isKindOfClass:[NSString class]]) {
    @throw makeException([JavaLangClassCastException class]);
  }
  return [self compare:(NSString *) another options:NSLiteralSearch];
}

- (int)compareToIgnoreCase:(NSString *)another {
  if (!another) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  return [self caseInsensitiveCompare:another];
}

- (NSString *)substring:(int)beginIndex {
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

- (BOOL)isEmpty {
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

- (unichar)charAtWithInt:(int)index {
  if (index < 0 || index >= (int) [self length]) {
    @throw makeException([JavaLangStringIndexOutOfBoundsException class]);
  }
  return [self characterAtIndex:(NSUInteger)index];
}

- (int)sequenceLength {
  return (int) [self length];
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
  unichar *buffer = calloc(length, sizeof(unichar));
  [self getCharacters:buffer range:range];
  NSString *subString = [NSString stringWithCharacters:buffer length:length];
  free(buffer);
  return (id<JavaLangCharSequence>) subString;
}

- (NSString *)replace:(unichar)oldchar withChar:(unichar)newchar {
  return [self replace:[NSString stringWithCharacters:&oldchar length:1]
          withSequence:[NSString stringWithCharacters:&newchar length:1]];
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
  return
      [self stringByReplacingOccurrencesOfString:regex
                                      withString:replacement
                                         options:NSRegularExpressionSearch
                                           range:NSMakeRange(0, [self length])];
}


- (NSString *)replaceFirst:(NSString *)regex
           withReplacement:(NSString *)replacement {
  NSRange range = [self rangeOfString:regex options:NSRegularExpressionSearch];
  if (range.location == NSNotFound) {
    return self;
  }
  return [self stringByReplacingOccurrencesOfString:regex
                                         withString:replacement
                                            options:NSRegularExpressionSearch
                                              range:range];
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
  NSStringEncoding nsEncoding = NSUTF8StringEncoding; // defaults to UTF-8
  if (charset) {
    CFStringEncoding cfEncoding =
        CFStringConvertIANACharSetNameToEncoding((ARCBRIDGE CFStringRef)charset);
    if (cfEncoding == kCFStringEncodingInvalidId) {
      id exception = [[JavaIoUnsupportedEncodingException alloc]
                      initWithNSString:charset];
#if ! __has_feature(objc_arc)
      [exception autorelease];
#endif
      @throw exception;
    } else {
      nsEncoding = CFStringConvertEncodingToNSStringEncoding(cfEncoding);
    }
  }
  return nsEncoding;
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
  if (![charset isKindOfClass:[JavaNioCharsetIOSCharset class]]) {
    @throw AUTORELEASE([[JavaNioCharsetUnsupportedCharsetException alloc]
                        initWithNSString:[charset description]]);
  }
  JavaNioCharsetIOSCharset *iosCharset = (JavaNioCharsetIOSCharset *) charset;
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
  unichar *chars = calloc(length, sizeof(unichar));
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
                      length:(int)length {
  jint ncps = codePoints->size_;
  int *ints = malloc(ncps);
  [codePoints getInts:ints length:ncps];
  unichar *chars = malloc(length);
  for (int i = 0; i < length; i++) {
    chars[i] = ints[i + offset];
  }
  NSString *s = [NSString stringWithCharacters:chars length:length];
  free(chars);
  free(ints);
  return s;
}

- (IOSByteArray *)getBytes  {
  return [self getBytesWithCharsetName:[[JavaNioCharsetCharset defaultCharset] name]];
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
  if (![charset isKindOfClass:[JavaNioCharsetIOSCharset class]]) {
    @throw AUTORELEASE([[JavaNioCharsetUnsupportedCharsetException alloc]
                        initWithNSString:[charset description]]);
  }
  JavaNioCharsetIOSCharset *iosCharset = (JavaNioCharsetIOSCharset *) charset;
  NSStringEncoding encoding = (NSStringEncoding) [iosCharset nsEncoding];
  return [self getBytesWithEncoding:encoding];
}

- (IOSByteArray *)getBytesWithEncoding:(NSStringEncoding)encoding {
  if (!encoding) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  int max_length = (int) [self maximumLengthOfBytesUsingEncoding:encoding];
  BOOL includeBOM = (encoding == NSUTF16StringEncoding);
  if (includeBOM) {
    max_length += 2;
  }
  char *buffer = malloc(max_length * sizeof(char));
  NSRange range = NSMakeRange(0, [self length]);
  NSUInteger used_length;
  [self getBytes:buffer
       maxLength:max_length
      usedLength:&used_length
        encoding:encoding
         options:includeBOM ? NSStringEncodingConversionExternalRepresentation : 0
           range:range
  remainingRange:NULL];
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
  char *bytes = malloc(maxBytes);
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

- (BOOL)hasPrefix:(NSString *)aString offset:(int)offset {
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
  JavaUtilRegexPattern *p = [JavaUtilRegexPattern compileWithNSString:str];
  return [p splitWithJavaLangCharSequence:self withInt:n];
}

- (BOOL)equalsIgnoreCase:(NSString *)aString {
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

- (BOOL)regionMatches:(int)thisOffset
              aString:(NSString *)aString
          otherOffset:(int)otherOffset
                count:(int)count {
  return [self regionMatches:NO
                  thisOffset:thisOffset
                     aString:aString
                 otherOffset:otherOffset
                       count:count];
}

- (BOOL)regionMatches:(BOOL)caseInsensitive
           thisOffset:(int)thisOffset
              aString:(NSString *)aString
          otherOffset:(int)otherOffset
                count:(int)count {
  if (thisOffset < 0 || count > (int) [self length] - thisOffset) {
    return NO;
  }
  if (otherOffset < 0 || count > (int) [aString length] - otherOffset) {
    return NO;
  }
  if (!aString) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  NSString *this = (thisOffset == 0 && count == (int) [self length])
      ? self : [self substringWithRange:NSMakeRange(thisOffset, count)];
  NSString *other = (otherOffset == 0 && count == (int) [aString length])
      ? aString : [aString substringWithRange:NSMakeRange(otherOffset, count)];
  NSUInteger options = NSLiteralSearch;
  if (caseInsensitive) {
    options |= NSCaseInsensitiveSearch;
  }
  return [this compare:other
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

- (BOOL)contains:(id<JavaLangCharSequence>)sequence {
  if (!sequence) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  if ([sequence sequenceLength] == 0) {
    return YES;
  }
  NSRange range = [self rangeOfString:[sequence description]];
  return range.location != NSNotFound;
}

- (int)codePointAt:(int)index {
  return [JavaLangCharacter codePointAtWithJavaLangCharSequence:self
                                                        withInt:index];
}

- (int)codePointBefore:(int)index {
  return [JavaLangCharacter codePointBeforeWithJavaLangCharSequence:self
                                                            withInt:index];
}

- (int)codePointCount:(int)beginIndex endIndex:(int)endIndex {
  return [JavaLangCharacter codePointCountWithJavaLangCharSequence:self
                                                           withInt:beginIndex
                                                           withInt:endIndex];
}

- (int)offsetByCodePoints:(int)index codePointOffset:(int)offset {
  return [JavaLangCharacter offsetByCodePointsWithJavaLangCharSequence:self
                                                               withInt:index
                                                               withInt:offset];
}

- (BOOL)matches:(NSString *)regex {
  if (!regex) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  NSError *error = nil;
  NSRegularExpression *nsRegex =
      [NSRegularExpression regularExpressionWithPattern:regex
                                                options:0
                                                  error:&error];
  if (error) {
    JavaUtilRegexPatternSyntaxException *exception =
        [[JavaUtilRegexPatternSyntaxException alloc]
         initWithNSString:[error localizedDescription]
             withNSString:regex
                  withInt:-1];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  id result = [nsRegex firstMatchInString:self
                                  options:0
                                    range:NSMakeRange(0, [self length])];
  return result != nil;
}

- (BOOL)contentEqualsCharSequence:(id<JavaLangCharSequence>)seq {
  return [self isEqualToString:[(id) seq description]];
}

- (BOOL)contentEqualsStringBuffer:(JavaLangStringBuffer *)sb {
  return [self isEqualToString:[sb description]];
}

- (NSUInteger)hash {
  static const char *hashKey = "__JAVA_STRING_HASH_CODE_KEY__";
  id cachedHash = objc_getAssociatedObject(self, hashKey);
  if (cachedHash) {
    return [(JavaLangInteger *) cachedHash intValue];
  }
  int len = (int)[self length];
  int hash = 0;
  if (len > 0) {
    unichar *chars = malloc(len * sizeof(unichar));
    [self getCharacters:chars range:NSMakeRange(0, len)];
    for (int i = 0; i < len; i++) {
      hash = 31 * hash + (int)chars[i];
    }
    free(chars);
  }
  if (![self isKindOfClass:[NSMutableString class]]) {
    // Only cache hash for immutable strings.
    objc_setAssociatedObject(self, hashKey, [JavaLangInteger valueOfWithInt:hash],
                             OBJC_ASSOCIATION_RETAIN);
  }
  return hash;
}

+ (J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { "initWithByteArray:withInt:withInt:withNSString:", NULL, NULL, 0x1, "Ljava/io/UnsupportedEncodingException;" },
    { "initWithByteArray:withNSString:", NULL, NULL, 0x1, "Ljava/io/UnsupportedEncodingException;" },
    { "initWithInt:withInt:withCharArray:", NULL, NULL, 0x0, NULL },
    { "formatWithJavaUtilLocale:withNSString:withNSObjectArray:", NULL, "Ljava/lang/String;", 0x89, NULL },
    { "formatWithNSString:withNSObjectArray:", NULL, "Ljava/lang/String;", 0x89, NULL },
    { "valueOf:", "valueOf", "Ljava/lang/String;", 0x9, NULL },
    { "valueOfBool:", "valueOf", "Ljava/lang/String;", 0x9, NULL },
    { "valueOfChar:", "valueOf", "Ljava/lang/String;", 0x9, NULL },
    { "valueOfChars:", "valueOf", "Ljava/lang/String;", 0x9, NULL },
    { "valueOfChars:offset:count:", "valueOf", "Ljava/lang/String;", 0x9, NULL },
    { "valueOfDouble:", "valueOf", "Ljava/lang/String;", 0x9, NULL },
    { "valueOfFloat:", "valueOf", "Ljava/lang/String;", 0x9, NULL },
    { "valueOfInt:", "valueOf", "Ljava/lang/String;", 0x9, NULL },
    { "valueOfLong:", "valueOf", "Ljava/lang/String;", 0x9, NULL },
    { "concat:", NULL, "Ljava/lang/String;", 0x1, NULL },
    { "contains:", NULL, "Z", 0x1, NULL },
    { "codePointAt:", NULL, "I", 0x1, NULL },
    { "codePointBefore:", NULL, "I", 0x1, NULL },
    { "codePointCount:endIndex:", NULL, "I", 0x1, NULL },
    { "hasSuffix:", "endsWith", "Z", 0x1, NULL },
    { "equalsIgnoreCase:", NULL, "Z", 0x1, NULL },
    { "getBytes", NULL, "[B", 0x1, NULL },
    { "getBytesWithCharset:", NULL, "[B", 0x1, NULL },
    { "getBytesWithCharsetName:", NULL, "[B", 0x1, "Ljava/io/UnsupportedEncodingException;" },
    { "intern", NULL, "Ljava/lang/String;", 0x1, NULL },
    { "isEmpty", NULL, "Z", 0x1, NULL },
    { "matches:", NULL, "Z", 0x1, NULL },
    { "regionMatches:thisOffset:aString:otherOffset:count:", NULL, "Z", 0x1, NULL },
    { "regionMatches:aString:otherOffset:count:", NULL, "Z", 0x1, NULL },
    { "replace:withChar:", NULL, "Ljava/lang/String;", 0x1, NULL },
    { "replace:withSequence:", NULL, "Ljava/lang/String;", 0x1, NULL },
    { "replaceAll:withReplacement:", NULL, "Ljava/lang/String;", 0x1, NULL },
    { "replaceFirst:withReplacement:", NULL, "Ljava/lang/String;", 0x1, NULL },
    { "split:", NULL, "[Ljava/lang/String;", 0x1, NULL },
    { "split:limit:", NULL, "[Ljava/lang/String;", 0x1, NULL },
    { "hasPrefix:", NULL, "Z", 0x1, NULL },
    { "hasPrefix:offset:", NULL, "Z", 0x1, NULL },
    { "substring:", NULL, "Ljava/lang/String;", 0x1, NULL },
    { "substring:endIndex:", NULL, "Ljava/lang/String;", 0x1, NULL },
    { "toCharArray", NULL, "[C", 0x1, NULL },
    { "lowercaseString", NULL, "Ljava/lang/String;", 0x1, NULL },
    { "uppercaseStringWithJRELocale:", NULL, "Ljava/lang/String;", 0x1, NULL },
    { "uppercaseString", NULL, "Ljava/lang/String;", 0x1, NULL },
    { "trim", NULL, "Ljava/lang/String;", 0x1, NULL },
    { "contentEqualsCharSequence:", NULL, "Z", 0x1, NULL },
    { "contentEqualsStringBuffer:", NULL, "Z", 0x1, NULL },
  };
  static J2ObjcFieldInfo fields[] = {
    { "CASE_INSENSITIVE_ORDER_", NULL, 0x19, "Ljava/util/Comparator;", &NSString_CASE_INSENSITIVE_ORDER_ },
    { "serialPersistentFields_", NULL, 0x1a, "[Ljava/io/ObjectStreamField;", &NSString_serialPersistentFields_ },
    { "serialVersionUID_", NULL, 0x1a, "J", NULL, .constantValue.asLong = NSString_serialVersionUID },
  };
  static J2ObjcClassInfo _JavaLangString = { "String", "java.lang", NULL, 0x1, 46, methods, 3, fields, 0, NULL};
  return &_JavaLangString;
}

@end

#define CaseInsensitiveComparator_serialVersionUID 8575799808933029326LL

@interface CaseInsensitiveComparator : NSObject < JavaUtilComparator, JavaIoSerializable >
@end

@implementation CaseInsensitiveComparator

- (int)compareWithId:(NSString *)o1
              withId:(NSString *)o2 {
  if (!o1) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  return [o1 compareToIgnoreCase:o2];
}

+ (J2ObjcClassInfo *)__metadata {
  static J2ObjcFieldInfo fields[] = {
    { "serialVersionUID_", NULL, 0x1a, "J", NULL, .constantValue.asLong = CaseInsensitiveComparator_serialVersionUID },
  };
  static J2ObjcClassInfo _JavaLangString_CaseInsensitiveComparator = {
    "CaseInsensitiveComparator", "java.lang", "String", 0xa, 0, NULL, 1, fields, 0, NULL
  };
  return &_JavaLangString_CaseInsensitiveComparator;
}

@end

BOOL NSString_initialized = NO;

id<JavaUtilComparator> NSString_CASE_INSENSITIVE_ORDER_;
IOSObjectArray *NSString_serialPersistentFields_;

@implementation JreStringCategoryDummy

+ (void)initialize {
  if (self == [JreStringCategoryDummy class]) {
    JreStrongAssignAndConsume(
        &NSString_CASE_INSENSITIVE_ORDER_, nil, [[CaseInsensitiveComparator alloc] init]);
    JreStrongAssignAndConsume(&NSString_serialPersistentFields_, nil,
        [IOSObjectArray newArrayWithLength:0 type:
            [IOSClass classWithClass:[JavaIoObjectStreamField class]]]);
    NSString_initialized = YES;
  }
}

@end
