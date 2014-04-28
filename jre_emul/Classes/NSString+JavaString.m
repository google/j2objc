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

@implementation NSString (JavaString)

id makeException(Class exceptionClass) {
  id exception = [[exceptionClass alloc] init];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  return exception;
}

+ (NSString *)valueOf:(id<NSObject>)obj {
  return obj ? [obj description] : @"null";
}

+ (NSString *)valueOfBool:(BOOL)value {
  return value ? @"true" : @"false";
}

+ (NSString *)valueOfChar:(unichar)value {
  return [NSString stringWithFormat:@"%C", value];
}

+ (NSString *)valueOfChars:(IOSCharArray *)data {
  return [NSString valueOfChars:data offset:0 count:[data count]];
}

+ (NSString *)valueOfChars:(IOSCharArray *)data
                    offset:(int)offset
                     count:(int)count {
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
  if (offset + count > [data count]) {
    exception = [[JavaLangStringIndexOutOfBoundsException alloc]
                 initWithInt:offset];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
  }
  if (exception) {
    @throw exception;
  }
  unichar *chars = [data getChars];
  NSString *result = [NSString stringWithCharacters:chars + offset
                                             length:(NSUInteger)count];
  free(chars);
  return result;
}

+ (NSString *)valueOfDouble:(double)value {
  return [[NSNumber numberWithDouble:value] stringValue];
}

+ (NSString *)valueOfFloat:(float)value {
  return [[NSNumber numberWithFloat:value] stringValue];
}

+ (NSString *)valueOfInt:(int)value {
  return [NSString stringWithFormat:@"%i", value];
}

+ (NSString *)valueOfLong:(long long int)value {
  return [NSString stringWithFormat:@"%qi", value];
}

+ (NSString *)valueOfShort:(short)value {
  return [NSString stringWithFormat:@"%i", value];
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
  if (sourceEnd > [self length]) {
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
  int destinationLength = [destination count];
  if (destinationBegin + range.length > destinationLength) {
    exception = [[JavaLangStringIndexOutOfBoundsException alloc]
                 initWithInt:destinationBegin + range.length];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
  }
  if (exception) {
    @throw exception;
  }

  unichar * buffer = calloc(destinationLength, sizeof(unichar));
  [self getCharacters:buffer range:range];
  for (int i = 0; i < range.length; i++) {
    unichar c = *(buffer + i);
    [destination replaceCharAtIndex:i + destinationBegin withChar:c];
  }
  free(buffer);
}

+ (NSString *)stringWithCharacters:(IOSCharArray *)value {
  return [NSString stringWithCharacters:value offset:0 length:[value count]];
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
  if (offset > [value count] - count) {
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
  unichar *buffer = [value getChars];
  NSString *result = [NSString stringWithCharacters:buffer + offset
                                             length:count];
  free(buffer);
  return result;
}

+ (NSString *)stringWithJavaLangStringBuffer:(JavaLangStringBuffer *)sb {
  return [sb sequenceDescription];
}

+ (NSString *)stringWithJavaLangStringBuilder:(JavaLangStringBuilder *)sb {
  if (!sb) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  return [sb sequenceDescription];
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
  if (endIndex > [self length]) {
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
  if (index >= max) {
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
    return [self length];
  }
  NSRange range = [self rangeOfString:s options:NSBackwardsSearch];
  return range.location == NSNotFound ? -1 : (int) range.location;
}

- (int)lastIndexOfString:(NSString *)s fromIndex:(int)index {
  if (!s) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  NSUInteger max = [self length];
  if (index < 0) {
    return -1;
  }
  if (max == 0) {
    return max;
  }
  NSUInteger sLen = [s length];
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
  if (index < 0 || index >= [self length]) {
    @throw makeException([JavaLangStringIndexOutOfBoundsException class]);
  }
  return [self characterAtIndex:(NSUInteger)index];
}

- (NSString *)sequenceDescription {
  return self;
}

- (int)sequenceLength {
  return (int) [self length];
}

- (id<JavaLangCharSequence>)subSequenceFrom:(int)start
                                         to:(int)end {
  NSUInteger maxLength = [self length];
  if (start < 0 || start > end || end > maxLength) {
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
  NSString *oldString = [oldSequence sequenceDescription];
  NSString *newString = [newSequence sequenceDescription];
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
                        length:[value count]
               encoding:encoding];
}

+ (NSString *)stringWithBytes:(IOSByteArray *)value
                  charsetName:(NSString *)charsetName {
  return [self stringWithBytes:value
                        offset:0
                        length:[value count]
                   charsetName:charsetName];
}

+ (NSString *)stringWithBytes:(IOSByteArray *)value
                      charset:(JavaNioCharsetCharset *)charset {
  return [self stringWithBytes:value
                        offset:0
                        length:[value count]
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
                            length:[value count]];
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
  NSUInteger max = [value count];
  char *bytes = malloc(max);
  [value getBytes:bytes offset:0 length:max];
  unichar *chars = calloc(length, sizeof(unichar));
  for (int i = 0; i < length; i++) {
    char b = bytes[i + offset];
    // Expression from String(byte[],int) javadoc.
    chars[i] = (unichar)(((hibyte & 0xff) << 8) | (b & 0xff));
  }
  NSString *s = [NSString stringWithCharacters:chars length:length];
  free(chars);
  free(bytes);
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
  if (offset > [value count] - count) {
    exception = [[JavaLangStringIndexOutOfBoundsException alloc]
                 initWithInt:offset + count];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
  }
  if (exception) {
    @throw exception;
  }

  int buffer_size = offset + count;
  char *buffer = malloc(buffer_size * sizeof(char));
  [value getBytes:buffer offset:0 length:buffer_size];
  NSString *result = [[NSString alloc] initWithBytes:buffer + offset
                                              length:count
                                            encoding:encoding];
  free(buffer);
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

+ (NSString *)stringWithInts:(IOSIntArray *)codePoints
                      offset:(int)offset
                      length:(int)length {
  NSUInteger ncps = [codePoints count];
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
  // UTF-8 is the Java default charset, unless the file.encoding system
  // property is set.
  return [self getBytesWithEncoding:NSUTF8StringEncoding];
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

- (IOSByteArray *)getBytesWithEncoding:(NSStringEncoding)encoding  {
  if (!encoding) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  int max_length = [self maximumLengthOfBytesUsingEncoding:encoding];
  char *buffer = malloc(max_length * sizeof(char));
  NSRange range = NSMakeRange(0, [self length]);
  NSUInteger used_length;
  [self getBytes:buffer
       maxLength:max_length
      usedLength:&used_length
        encoding:encoding options:0
           range:range
  remainingRange:NULL];
  IOSByteArray *result = [IOSByteArray arrayWithBytes:buffer
                                                count:used_length];
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
  } else if (srcEnd > [self length]) {
    badParamMsg = @"srcEnd > string length";
  } else if (copyLength > [self length]) {
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
  char *dstBytes = malloc([dst count] - dstBegin);

  // Double-check there won't be a buffer overflow, since the encoded length
  // of the copied substring is now known.
  if (bytesUsed > ([dst count] - dstBegin)) {
    free(dstBytes);
    free(bytes);
    @throw AUTORELEASE(
        [[JavaLangStringIndexOutOfBoundsException alloc]
         initWithNSString:@"dstBegin+(srcEnd-srcBegin) > dst.length"]);
  }
  memcpy(dstBytes, bytes, bytesUsed);
  [dst replaceBytes:dstBytes length:bytesUsed offset:dstBegin];
  free(dstBytes);
  free(bytes);
}

+ (NSString *)formatWithNSString:(NSString *)format withNSObjectArray:(IOSObjectArray *)args {
  JavaUtilFormatter *formatter = [[JavaUtilFormatter alloc] init];
  NSString *result = [[formatter formatWithNSString:format withNSObjectArray:args] description];
  RELEASE_(formatter);
  return result;
}

+ (NSString *)formatWithJavaUtilLocale:(JavaUtilLocale *)locale
                          withNSString:(NSString *)format
                     withNSObjectArray:(IOSObjectArray *)args {
  JavaUtilFormatter *formatter =
      AUTORELEASE([[JavaUtilFormatter alloc] initWithJavaUtilLocale:locale]);
  return [[formatter formatWithNSString:format withNSObjectArray:args] description];
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
  return [self stringByTrimmingCharactersInSet:
          [NSCharacterSet whitespaceAndNewlineCharacterSet]];
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
  if (thisOffset < 0 || count > [self length] - thisOffset) {
    return NO;
  }
  if (otherOffset < 0 || count > [aString length] - otherOffset) {
    return NO;
  }
  if (!aString) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  NSString *this = (thisOffset == 0 && count == [self length])
      ? self : [self substringWithRange:NSMakeRange(thisOffset, count)];
  NSString *other = (otherOffset == 0 && count == [aString length])
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
    JreOperatorRetainedAssign(
        &NSString_CASE_INSENSITIVE_ORDER_, nil,
        [[[CaseInsensitiveComparator alloc] init] autorelease]);
    JreOperatorRetainedAssign(&NSString_serialPersistentFields_, nil,
        [IOSObjectArray arrayWithLength:0 type:
            [IOSClass classWithClass:[JavaIoObjectStreamField class]]]);
    NSString_initialized = YES;
  }
}

@end
