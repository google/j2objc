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
#import "java/util/regex/Matcher.h"
#import "java/util/regex/Pattern.h"
#import "java/util/regex/PatternSyntaxException.h"
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

+ (NSString *)valueOfChar:(unichar)value {
  return NSString_valueOfChar_(value);
}

NSString *NSString_valueOfChar_(unichar value) {
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

- (unichar)charAtWithInt:(int)index {
  if (index < 0 || index >= (int) [self length]) {
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

- (NSString *)replace:(unichar)oldchar withChar:(unichar)newchar {
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
  return (NSStringEncoding)[(JavaNioCharsetIOSCharset *)cs nsEncoding];
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
                      length:(int)length {
  jint ncps = codePoints->size_;
  jint *ints = (jint *)malloc(ncps * sizeof(jint));
  [codePoints getInts:ints length:ncps];
  unichar *chars = (unichar *)malloc(length);
  for (int i = 0; i < length; i++) {
    chars[i] = ints[i + offset];
  }
  NSString *s = [NSString stringWithCharacters:chars length:length];
  free(chars);
  free(ints);
  return s;
}

- (IOSByteArray *)getBytes  {
  JavaNioCharsetCharset *charset = JavaNioCharsetCharset_defaultCharset();
  NSStringEncoding encoding = (NSStringEncoding)[(JavaNioCharsetIOSCharset *)charset nsEncoding];
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

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "init", "String", NULL, 0x1, NULL, NULL },
    { "initWithByteArray:", "String", NULL, 0x1, NULL, NULL },
    { "initWithByteArray:withInt:", "String", NULL, 0x1, NULL, NULL },
    { "initWithByteArray:withInt:withInt:", "String", NULL, 0x1, NULL, NULL },
    { "initWithByteArray:withInt:withInt:withInt:", "String", NULL, 0x1, NULL, NULL },
    { "initWithByteArray:withInt:withInt:withNSString:", "String", NULL, 0x1,
      "Ljava.io.UnsupportedEncodingException;", NULL },
    { "initWithByteArray:withInt:withInt:withJavaNioCharsetCharset:", "String", NULL, 0x1, NULL,
      NULL },
    { "initWithByteArray:withJavaNioCharsetCharset:", "String", NULL, 0x1, NULL, NULL },
    { "initWithByteArray:withNSString:", "String", NULL, 0x1,
      "Ljava.io.UnsupportedEncodingException;", NULL },
    { "initWithCharArray:", "String", NULL, 0x1, NULL, NULL },
    { "initWithCharArray:withInt:withInt:", "String", NULL, 0x1, NULL, NULL },
    { "initWithIntArray:withInt:withInt:", "String", NULL, 0x1, NULL, NULL },
    { "initWithInt:withInt:withCharArray:", "String", NULL, 0x0, NULL, NULL },
    { "initWithNSString:", "String", NULL, 0x1, NULL, NULL },
    { "initWithJavaLangStringBuffer:", "String", NULL, 0x1, NULL, NULL },
    { "initWithJavaLangStringBuilder:", "String", NULL, 0x1, NULL, NULL },
    { "valueOfChars:", "copyValueOf", "Ljava.lang.String;", 0x9, NULL, NULL },
    { "valueOfChars:offset:count:", "copyValueOf", "Ljava.lang.String;", 0x9, NULL, NULL },
    { "formatWithJavaUtilLocale:withNSString:withNSObjectArray:", "format", "Ljava.lang.String;",
      0x89, NULL, NULL },
    { "formatWithNSString:withNSObjectArray:", "format", "Ljava.lang.String;", 0x89, NULL, NULL },
    { "valueOfBool:", "valueOf", "Ljava.lang.String;", 0x9, NULL, NULL },
    { "valueOfChar:", "valueOf", "Ljava.lang.String;", 0x9, NULL, NULL },
    { "valueOfChars:", "valueOf", "Ljava.lang.String;", 0x9, NULL, NULL },
    { "valueOfChars:offset:count:", "valueOf", "Ljava.lang.String;", 0x9, NULL, NULL },
    { "valueOfDouble:", "valueOf", "Ljava.lang.String;", 0x9, NULL, NULL },
    { "valueOfFloat:", "valueOf", "Ljava.lang.String;", 0x9, NULL, NULL },
    { "valueOfInt:", "valueOf", "Ljava.lang.String;", 0x9, NULL, NULL },
    { "valueOfLong:", "valueOf", "Ljava.lang.String;", 0x9, NULL, NULL },
    { "valueOf:", "valueOf", "Ljava.lang.String;", 0x9, NULL, NULL },
    { "charAtWithInt:", "charAt", "C", 0x1, NULL, NULL },
    { "codePointAt:", "codePointAt", "I", 0x1, NULL, NULL },
    { "codePointBefore:", "codePointBefore", "I", 0x1, NULL, NULL },
    { "codePointCount:endIndex:", "codePointCount", "I", 0x1, NULL, NULL },
    { "compareToWithId:", "compareTo", "I", 0x1, NULL, NULL },
    { "compareToIgnoreCase:", "compareToIgnoreCase", "I", 0x1, NULL, NULL },
    { "concat:", "concat", "Ljava.lang.String;", 0x1, NULL, NULL },
    { "contains:", "contains", "Z", 0x1, NULL, NULL },
    { "hasSuffix:", "endsWith", "Z", 0x1, NULL, NULL },
    { "equalsIgnoreCase:", "equalsIgnoreCase", "Z", 0x1, NULL, NULL },
    { "getBytes", NULL, "[B", 0x1, NULL, NULL },
    { "getBytesWithCharset:", "getBytes", "[B", 0x1, NULL, NULL },
    { "getBytesWithCharsetName:", "getBytes", "[B", 0x1, "Ljava.io.UnsupportedEncodingException;",
      NULL },
    { "getBytesWithSrcBegin:withSrcEnd:withDst:withDstBegin:", "getBytes", "V", 0x1, NULL, NULL },
    { "getChars:sourceEnd:destination:destinationBegin:", "getChars", "V", 0x1, NULL, NULL },
    { "indexOf:", "indexOf", "I", 0x1, NULL, NULL },
    { "indexOf:fromIndex:", "indexOf", "I", 0x1, NULL, NULL },
    { "indexOfString:", "indexOf", "I", 0x1, NULL, NULL },
    { "indexOfString:fromIndex:", "indexOf", "I", 0x1, NULL, NULL },
    { "intern", NULL, "Ljava.lang.String;", 0x1, NULL, NULL },
    { "isEmpty", NULL, "Z", 0x1, NULL, NULL },
    { "lastIndexOf:", "lastIndexOf", "I", 0x1, NULL, NULL },
    { "lastIndexOf:fromIndex:", "lastIndexOf", "I", 0x1, NULL, NULL },
    { "lastIndexOfString:", "lastIndexOf", "I", 0x1, NULL, NULL },
    { "lastIndexOfString:fromIndex:", "lastIndexOf", "I", 0x1, NULL, NULL },
    { "length", NULL, "I", 0x1, NULL, NULL },
    { "matches:", "matches", "Z", 0x1, NULL, NULL },
    { "offsetByCodePoints:codePointOffset:", "offsetByCodePoints", "I", 0x1, NULL, NULL },
    { "regionMatches:thisOffset:aString:otherOffset:count:", "regionMatches", "Z", 0x1, NULL,
      NULL },
    { "regionMatches:aString:otherOffset:count:", "regionMatches", "Z", 0x1, NULL, NULL },
    { "replace:withChar:", "replace", "Ljava.lang.String;", 0x1, NULL, NULL },
    { "replace:withSequence:", "replace", "Ljava.lang.String;", 0x1, NULL, NULL },
    { "replaceAll:withReplacement:", "replaceAll", "Ljava.lang.String;", 0x1, NULL, NULL },
    { "replaceFirst:withReplacement:", "replaceFirst", "Ljava.lang.String;", 0x1, NULL, NULL },
    { "split:", "split", "[Ljava.lang.String;", 0x1, NULL, NULL },
    { "split:limit:", "split", "[Ljava.lang.String;", 0x1, NULL, NULL },
    { "hasPrefix:", "startsWith", "Z", 0x1, NULL, NULL },
    { "hasPrefix:offset:", "startsWith", "Z", 0x1, NULL, NULL },
    { "subSequenceFrom:to:", "subSequence", "Ljava.lang.CharSequence;", 0x1, NULL, NULL },
    { "substring:", "substring", "Ljava.lang.String;", 0x1, NULL, NULL },
    { "substring:endIndex:", "substring", "Ljava.lang.String;", 0x1, NULL, NULL },
    { "toCharArray", NULL, "[C", 0x1, NULL, NULL },
    { "lowercaseString", "toLowerCase", "Ljava.lang.String;", 0x1, NULL, NULL },
    { "lowercaseStringWithJRELocale:", "toLowerCase", "Ljava.lang.String;", 0x1, NULL, NULL },
    { "uppercaseString", "toUpperCase", "Ljava.lang.String;", 0x1, NULL, NULL },
    { "uppercaseStringWithJRELocale:", "toUpperCase", "Ljava.lang.String;", 0x1, NULL, NULL },
    { "trim", NULL, "Ljava.lang.String;", 0x1, NULL, NULL },
    { "contentEqualsCharSequence:", "contentEquals", "Z", 0x1, NULL, NULL },
    { "contentEqualsStringBuffer:", "contentEquals", "Z", 0x1, NULL, NULL },
  };
  static const J2ObjcFieldInfo fields[] = {
    { "CASE_INSENSITIVE_ORDER", "CASE_INSENSITIVE_ORDER", 0x19, "Ljava.util.Comparator;",
      &NSString_CASE_INSENSITIVE_ORDER, "Ljava/util/Comparator<Ljava/lang/String;>;",
      .constantValue.asLong = 0 },
    { "serialVersionUID", "serialVersionUID", 0x1a, "J", NULL, NULL,
      .constantValue.asLong = NSString_serialVersionUID },
    { "serialPersistentFields", "serialPersistentFields", 0x1a, "[Ljava.io.ObjectStreamField;",
      &NSString_serialPersistentFields, NULL, .constantValue.asLong = 0 },
  };
  static const char *inner_classes[] = {"Ljava.lang.String$CaseInsensitiveComparator;"};
  static const J2ObjcClassInfo _NSString = {
    2, "String", "java.lang", NULL, 0x1, 78, methods, 3, fields, 0, NULL, 1, inner_classes, NULL,
    "Ljava/lang/Object;Ljava/lang/CharSequence;"
    "Ljava/lang/Comparable<Ljava/lang/String;>;Ljava/io/Serializable;"
  };
  return &_NSString;
}

@end

#define CaseInsensitiveComparator_serialVersionUID 8575799808933029326LL

@interface JavaLangString_CaseInsensitiveComparator : NSObject
    < JavaUtilComparator, JavaIoSerializable >
@end

@implementation JavaLangString_CaseInsensitiveComparator

- (int)compareWithId:(NSString *)o1
              withId:(NSString *)o2 {
  if (!o1) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  return [o1 compareToIgnoreCase:o2];
}

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "compareWithId:withId:", "compare", "I", 0x1, NULL, NULL },
    { "init", NULL, NULL, 0x2, NULL, NULL },
  };
  static const J2ObjcFieldInfo fields[] = {
    { "serialVersionUID", "serialVersionUID", 0x1a, "J", NULL, NULL,
      .constantValue.asLong = CaseInsensitiveComparator_serialVersionUID },
  };
  static const J2ObjcClassInfo _NSString$CaseInsensitiveComparator = {
    2, "CaseInsensitiveComparator", "java.lang", "String", 0xa, 2, methods,
    1, fields, 0, NULL, 0, NULL, NULL,
    "Ljava/lang/Object;Ljava/util/Comparator<Ljava/lang/String;>;Ljava/io/Serializable;" };
  return &_NSString$CaseInsensitiveComparator;
}

@end

J2OBJC_INITIALIZED_DEFN(NSString)

id<JavaUtilComparator> NSString_CASE_INSENSITIVE_ORDER;
IOSObjectArray *NSString_serialPersistentFields;

@implementation JreStringCategoryDummy

+ (void)initialize {
  if (self == [JreStringCategoryDummy class]) {
    JreStrongAssignAndConsume(&NSString_CASE_INSENSITIVE_ORDER,
        [[JavaLangString_CaseInsensitiveComparator alloc] init]);
    JreStrongAssignAndConsume(&NSString_serialPersistentFields,
        [IOSObjectArray newArrayWithLength:0 type:JavaIoObjectStreamField_class_()]);
    J2OBJC_SET_INITIALIZED(NSString)
  }
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(NSString)
