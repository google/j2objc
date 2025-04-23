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
#import "java/lang/Deprecated.h"
#import "java/lang/Double.h"
#import "java/lang/Float.h"
#import "java/lang/Integer.h"
#import "java/lang/Iterable.h"
#import "java/lang/Long.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/OutOfMemoryError.h"
#import "java/lang/StringBuffer.h"
#import "java/lang/StringBuilder.h"
#import "java/lang/StringIndexOutOfBoundsException.h"
#import "java/lang/StringUTF16.h"
#import "java/lang/annotation/Annotation.h"
#import "java/nio/ByteBuffer.h"
#import "java/nio/CharBuffer.h"
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

#define NSString_serialVersionUID -6849794470754667710LL

__attribute__((unused)) static IOSObjectArray *NSString__Annotations$0(void);
__attribute__((unused)) static IOSObjectArray *NSString__Annotations$1(void);
__attribute__((unused)) static IOSObjectArray *NSString__Annotations$2(void);
__attribute__((unused)) static IOSObjectArray *NSString__Annotations$3(void);

@implementation NSString (JavaString)

id makeException(Class exceptionClass) {
  id exception = [[exceptionClass alloc] init];
#if !__has_feature(objc_arc)
  [exception autorelease];
#endif
  return exception;
}

static void checkBounds(jint length, jint offset, jint count) {
  if ((offset | count) < 0 || offset > length - count) {
    @throw create_JavaLangStringIndexOutOfBoundsException_initWithInt_withInt_withInt_(
        length, offset, count);
  }
}

// TODO(tball): remove static method wrappers when reflection invocation calls functions directly.
+ (NSString *)java_valueOf:(id<NSObject>)obj {
  return NSString_java_valueOf_((id)obj);
}

NSString *NSString_java_valueOf_(id obj) { return obj ? [obj description] : @"null"; }

+ (NSString *)java_valueOfBool:(jboolean)value {
  return NSString_java_valueOfBool_(value);
}

NSString *NSString_java_valueOfBool_(jboolean value) { return value ? @"true" : @"false"; }

+ (NSString *)java_valueOfChar:(jchar)value {
  return NSString_java_valueOfChar_(value);
}

NSString *NSString_java_valueOfChar_(jchar value) {
  return [NSString stringWithCharacters:&value length:1];
}

NSString *NSString_java_valueOfChars_(IOSCharArray *data) {
  (void)nil_chk(data);
  return [NSString stringWithCharacters:data->buffer_ length:data->size_];
}

+ (NSString *)java_valueOfChars:(IOSCharArray *)data {
  return NSString_java_valueOfChars_(data);
}

NSString *NSString_java_valueOfChars_offset_count_(IOSCharArray *data, jint offset, jint count) {
  (void)nil_chk(data);
  checkBounds(data->size_, offset, count);
  return [NSString stringWithCharacters:data->buffer_ + offset length:count];
}

+ (NSString *)java_valueOfChars:(IOSCharArray *)data offset:(jint)offset count:(jint)count {
  return NSString_java_valueOfChars_offset_count_(data, offset, count);
}

+ (NSString *)java_valueOfDouble:(jdouble)value {
  return NSString_java_valueOfDouble_(value);
}

NSString *NSString_java_valueOfDouble_(jdouble value) {
  return JavaLangDouble_toStringWithDouble_(value);
}

+ (NSString *)java_valueOfFloat:(jfloat)value {
  return NSString_java_valueOfFloat_(value);
}

NSString *NSString_java_valueOfFloat_(jfloat value) {
  return JavaLangFloat_toStringWithFloat_(value);
}

+ (NSString *)java_valueOfInt:(jint)value {
  return NSString_java_valueOfInt_(value);
}

NSString *NSString_java_valueOfInt_(jint value) { return JavaLangInteger_toStringWithInt_(value); }

+ (NSString *)java_valueOfLong:(jlong)value {
  return NSString_java_valueOfLong_(value);
}

NSString *NSString_java_valueOfLong_(jlong value) { return JavaLangLong_toStringWithLong_(value); }

- (void)java_getChars:(jint)sourceBegin
            sourceEnd:(jint)sourceEnd
          destination:(IOSCharArray *)destination
     destinationBegin:(jint)destinationBegin {
  if (sourceBegin < 0) {
    @throw create_JavaLangStringIndexOutOfBoundsException_initWithInt_(sourceBegin);
  }
  if (sourceEnd > (jint)[self length]) {
    @throw create_JavaLangStringIndexOutOfBoundsException_initWithInt_(sourceEnd);
  }
  if (sourceBegin > sourceEnd) {
    @throw create_JavaLangStringIndexOutOfBoundsException_initWithInt_(sourceEnd - sourceBegin);
  }

  NSRange range = NSMakeRange(sourceBegin, sourceEnd - sourceBegin);
  jint destinationLength = destination->size_;
  if (destinationBegin + (jint)range.length > destinationLength) {
    @throw create_JavaLangStringIndexOutOfBoundsException_initWithInt_(
        (jint)(destinationBegin + range.length));
  }

  [self getCharacters:destination->buffer_ + destinationBegin range:range];
}

// Package-private constructor, with parameters already checked.
static NSString *StringFromCharArray(IOSCharArray *value, jint offset, jint count) {
  if (count == 0) {
    return [NSString string];
  }
  NSString *result = [NSString stringWithCharacters:value->buffer_ + offset length:count];
  return result;
}

+ (NSString *)java_stringWithCharacters:(IOSCharArray *)value {
  (void)nil_chk(value);
  return StringFromCharArray(value, 0, value->size_);
}

+ (NSString *)java_stringWithCharacters:(IOSCharArray *)value
                                 offset:(jint)offset
                                 length:(jint)count {
  (void)nil_chk(value);
  checkBounds(value->size_, offset, count);
  return StringFromCharArray(value, offset, count);
}

+ (NSString *)java_stringWithJavaLangStringBuffer:(JavaLangStringBuffer *)sb {
  return [sb description];
}

+ (NSString *)java_stringWithJavaLangStringBuilder:(JavaLangStringBuilder *)sb {
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
  return (jint)[self compare:(NSString *)another options:NSLiteralSearch];
}

- (jint)java_compareToIgnoreCase:(NSString *)another {
  if (!another) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  return (jint)[self caseInsensitiveCompare:another];
}

- (NSString *)java_substring:(jint)beginIndex {
  if (beginIndex < 0 || beginIndex > (jint)[self length]) {
    @throw create_JavaLangStringIndexOutOfBoundsException_initWithInt_(beginIndex);
  }
  return [self substringFromIndex:(NSUInteger)beginIndex];
}

- (NSString *)java_substring:(jint)beginIndex endIndex:(jint)endIndex {
  if (beginIndex < 0) {
    @throw create_JavaLangStringIndexOutOfBoundsException_initWithInt_(beginIndex);
  }
  if (endIndex < beginIndex) {
    @throw create_JavaLangStringIndexOutOfBoundsException_initWithInt_(endIndex - beginIndex);
  }
  if (endIndex > (jint)[self length]) {
    @throw create_JavaLangStringIndexOutOfBoundsException_initWithInt_(endIndex);
  }
  NSRange range = NSMakeRange(beginIndex, endIndex - beginIndex);
  return [self substringWithRange:range];
}

- (jint)java_indexOf:(jint)ch {
  return [self java_indexOf:ch fromIndex:0];
}

- (jint)java_indexOf:(jint)ch fromIndex:(jint)index {
  unichar c = (unichar)ch;
  NSString *s = [NSString stringWithCharacters:&c length:1];
  return [self java_indexOfString:s fromIndex:(jint)index];
}

- (jint)java_indexOfString:(NSString *)s {
  if (!s) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  if ([s length] == 0) {
    return 0;
  }
  NSRange range = [self rangeOfString:s];
  return range.location == NSNotFound ? -1 : (jint)range.location;
}

- (jint)java_indexOfString:(NSString *)s fromIndex:(jint)index {
  if (!s) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  if ([s length] == 0) {
    return 0;
  }
  NSUInteger max = [self length];
  if ((NSUInteger)index >= max) {
    return -1;
  }
  if (index < 0) {
    index = 0;
  }
  NSRange searchRange = NSMakeRange((NSUInteger)index, max - (NSUInteger)index);
  NSRange range = [self rangeOfString:s options:NSLiteralSearch range:searchRange];
  return range.location == NSNotFound ? -1 : (jint)range.location;
}

- (jboolean)java_isEmpty {
  return [self length] == 0;
}

- (jint)java_lastIndexOf:(jint)ch {
  unichar c = (unichar)ch;
  NSString *s = [NSString stringWithCharacters:&c length:1];
  return [self java_lastIndexOfString:s];
}

- (jint)java_lastIndexOf:(jint)ch fromIndex:(jint)index {
  unichar c = (unichar)ch;
  NSString *s = [NSString stringWithCharacters:&c length:1];
  return [self java_lastIndexOfString:s fromIndex:(jint)index];
}

- (jint)java_lastIndexOfString:(NSString *)s {
  if (!s) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  if ([s length] == 0) {
    return (jint)[self length];
  }
  NSRange range = [self rangeOfString:s options:NSBackwardsSearch];
  return range.location == NSNotFound ? -1 : (jint)range.location;
}

- (jint)java_lastIndexOfString:(NSString *)s fromIndex:(jint)index {
  if (!s) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  int max = (jint)[self length];
  if (index < 0) {
    return -1;
  }
  if (max == 0) {
    return max;
  }
  int sLen = (jint)[s length];
  if (sLen == 0) {
    return index;
  }
  int bound = index + sLen;
  if (bound > max) {
    bound = max;
  }
  NSRange searchRange = NSMakeRange((NSUInteger)0, (NSUInteger)bound);
  NSRange range = [self rangeOfString:s options:NSBackwardsSearch range:searchRange];
  return range.location == NSNotFound ? -1 : (jint)range.location;
}

- (jint)java_length {
  return (jint)[self length];
}

- (IOSCharArray *)java_toCharArray {
  return [IOSCharArray arrayWithNSString:self];
}

- (jchar)charAtWithInt:(jint)index {
  if (index < 0 || index >= (jint)[self length]) {
    @throw create_JavaLangStringIndexOutOfBoundsException_initWithInt_(index);
  }
  return [self characterAtIndex:(NSUInteger)index];
}

- (id<JavaLangCharSequence>)subSequenceFrom:(jint)start to:(jint)end {
  return [self java_substring:start endIndex:end];
}

- (NSString *)java_replace:(jchar)oldchar withChar:(jchar)newchar {
  CFStringRef this = (__bridge CFStringRef)self;
  CFIndex length = CFStringGetLength(this);
  unichar *chars = malloc(length * sizeof(unichar));
  CFRange range = {0, length};
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

- (NSString *)java_replace:(id<JavaLangCharSequence>)oldSequence
              withSequence:(id<JavaLangCharSequence>)newSequence {
  NSString *oldString = [oldSequence description];
  NSString *newString = [newSequence description];
  return [self stringByReplacingOccurrencesOfString:oldString withString:newString];
}

- (NSString *)java_replaceAll:(NSString *)regex withReplacement:(NSString *)replacement {
  NSString *result;
  @autoreleasepool {
    JavaUtilRegexMatcher *matcher =
        [JavaUtilRegexPattern_compileWithNSString_(regex) matcherWithJavaLangCharSequence:self];
    result = RETAIN_([matcher replaceAllWithNSString:replacement]);
  }
  return AUTORELEASE(result);
}

- (NSString *)java_replaceFirst:(NSString *)regex withReplacement:(NSString *)replacement {
  NSString *result;
  @autoreleasepool {
    JavaUtilRegexMatcher *matcher =
        [JavaUtilRegexPattern_compileWithNSString_(regex) matcherWithJavaLangCharSequence:self];
    result = RETAIN_([matcher replaceFirstWithNSString:replacement]);
  }
  return AUTORELEASE(result);
}

+ (NSString *)java_stringWithBytes:(IOSByteArray *)value {
  return [self java_stringWithBytes:value
                             offset:0
                             length:value->size_
                            charset:JavaNioCharsetCharset_defaultCharset()];
}

+ (NSString *)java_stringWithBytes:(IOSByteArray *)value charsetName:(NSString *)charsetName {
  return [self java_stringWithBytes:value offset:0 length:value->size_ charsetName:charsetName];
}

+ (NSString *)java_stringWithBytes:(IOSByteArray *)value charset:(JavaNioCharsetCharset *)charset {
  return [self java_stringWithBytes:value offset:0 length:value->size_ charset:charset];
}

+ (NSString *)java_stringWithBytes:(IOSByteArray *)value hibyte:(jint)hibyte {
  return [NSString java_stringWithBytes:value hibyte:hibyte offset:0 length:value->size_];
}

+ (NSString *)java_stringWithBytes:(IOSByteArray *)value offset:(jint)offset length:(jint)count {
  return [NSString java_stringWithBytes:value
                                 offset:offset
                                 length:count
                                charset:JavaNioCharsetCharset_defaultCharset()];
}

+ (NSString *)java_stringWithBytes:(IOSByteArray *)value
                            offset:(jint)offset
                            length:(jint)count
                       charsetName:(NSString *)charset {
  return [NSString java_stringWithBytes:value
                                 offset:offset
                                 length:count
                                charset:JavaNioCharsetCharset_forNameUEEWithNSString_(charset)];
}

+ (NSString *)java_stringWithBytes:(IOSByteArray *)value
                            offset:(jint)offset
                            length:(jint)count
                           charset:(JavaNioCharsetCharset *)charset {
  (void)nil_chk(value);
  checkBounds(value->size_, offset, count);
  if ([charset isKindOfClass:[ComGoogleJ2objcNioCharsetIOSCharset class]]) {
    CFStringEncoding encoding =
        (CFStringEncoding)[(ComGoogleJ2objcNioCharsetIOSCharset *)charset cfEncoding];
    NSString *result = (NSString *)CFStringCreateWithBytes(
        NULL, (const UInt8 *)value->buffer_ + offset, count, encoding, true);
    // CFString can return nil if there are invalid bytes in the input.
    if (result) {
      return AUTORELEASE(result);
    }
  }
  JavaNioCharBuffer *cb =
      [charset decodeWithJavaNioByteBuffer:JavaNioByteBuffer_wrapWithByteArray_withInt_withInt_(
                                               value, offset, count)];
  const unichar *chars = ((IOSCharArray *)[cb array])->buffer_ + [cb position];
  return [NSString stringWithCharacters:chars length:[cb remaining]];
}

+ (NSString *)java_stringWithBytes:(IOSByteArray *)value
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

+ (NSString *)java_stringWithInts:(IOSIntArray *)codePoints offset:(jint)offset length:(jint)count {
  if (!codePoints) {
    @throw create_JavaLangNullPointerException_initWithNSString_(@"codePoints == null");
  }
  jint ncps = codePoints->size_;
  if ((offset | count) < 0 || count > ncps - offset) {
    @throw create_JavaLangStringIndexOutOfBoundsException_initWithInt_withInt_withInt_(ncps, offset,
                                                                                       count);
  }
  IOSCharArray *value = [IOSCharArray arrayWithLength:count * 2];
  jint end = offset + count;
  jint length = 0;
  for (jint i = offset; i < end; i++) {
    length += JavaLangCharacter_toCharsWithInt_withCharArray_withInt_(codePoints->buffer_[i], value,
                                                                      length);
  }
  return [NSString stringWithCharacters:value->buffer_ length:length];
}

- (IOSByteArray *)java_getBytes {
  return [self java_getBytesWithCharset:JavaNioCharsetCharset_defaultCharset()];
}

- (IOSByteArray *)java_getBytesWithCharsetName:(NSString *)charsetName {
  if (!charsetName) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  return [self java_getBytesWithCharset:JavaNioCharsetCharset_forNameUEEWithNSString_(charsetName)];
}

static IOSByteArray *GetBytesWithEncoding(NSString *self, CFStringEncoding encoding) {
  CFStringRef cfStr = (CFStringRef)self;
  CFIndex strLength = CFStringGetLength(cfStr);
  CFIndex max_length = CFStringGetMaximumSizeForEncoding(strLength, encoding);
  jboolean includeBOM = (encoding == kCFStringEncodingUTF16);
  if (includeBOM) {
    max_length += 2;
    encoding = kCFStringEncodingUTF16BE;  // Java uses big-endian.
  }
  UInt8 *buffer = (UInt8 *)malloc(max_length * sizeof(UInt8));
  UInt8 *p = buffer;
  if (includeBOM) {
    *p++ = 0xFE;
    *p++ = 0xFF;
    max_length -= 2;
  }
  CFRange range = CFRangeMake(0, strLength);
  CFIndex used_length;
  CFIndex numConverted =
      CFStringGetBytes(cfStr, range, encoding, 0, false, p, max_length, &used_length);
  if (includeBOM) {
    used_length += 2;
  }
  IOSByteArray *result = nil;
  // If numConverted != strLength then getBytes failed to encode the whole string, possibly due to a
  // character that can't be represented in the desired encoding.
  if (numConverted == strLength) {
    result = [IOSByteArray arrayWithBytes:(jbyte *)buffer count:(jint)used_length];
  }
  free(buffer);
  return result;
}

- (IOSByteArray *)java_getBytesWithCharset:(JavaNioCharsetCharset *)charset {
  (void)nil_chk(charset);
  IOSByteArray *result;
  if ([charset isKindOfClass:[ComGoogleJ2objcNioCharsetIOSCharset class]]) {
    CFStringEncoding encoding =
        (CFStringEncoding)[(ComGoogleJ2objcNioCharsetIOSCharset *)charset cfEncoding];
    result = GetBytesWithEncoding(self, encoding);
    if (result) {
      return result;
    }
  }
  JavaNioByteBuffer *bb =
      [charset encodeWithJavaNioCharBuffer:JavaNioCharBuffer_wrapWithCharArray_(
                                               [IOSCharArray arrayWithNSString:self])];
  result = [IOSByteArray arrayWithLength:[bb remaining]];
  [bb getWithByteArray:result];
  return result;
}

- (void)java_getBytesWithSrcBegin:(jint)srcBegin
                       withSrcEnd:(jint)srcEnd
                          withDst:(IOSByteArray *)dst
                     withDstBegin:(jint)dstBegin {
  int copyLength = srcEnd - srcBegin;
  NSString *badParamMsg = nil;
  if (srcBegin < 0) {
    badParamMsg = @"srcBegin < 0";
  } else if (srcBegin > srcEnd) {
    badParamMsg = @"srcBegin > srcEnd";
  } else if (srcEnd > (jint)[self length]) {
    badParamMsg = @"srcEnd > string length";
  } else if (copyLength > (jint)[self length]) {
    badParamMsg = @"dstBegin+(srcEnd-srcBegin) > dst.length";
  }
  if (badParamMsg) {
    @throw create_JavaLangStringIndexOutOfBoundsException_initWithNSString_(badParamMsg);
  }
  (void)nil_chk(dst);
  NSUInteger maxBytes = [self maximumLengthOfBytesUsingEncoding:NSUTF8StringEncoding];
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
    @throw create_JavaLangStringIndexOutOfBoundsException_initWithNSString_(
        @"dstBegin+(srcEnd-srcBegin) > dst.length");
  }
  [dst replaceBytes:(jbyte *)bytes length:(jint)bytesUsed offset:dstBegin];
  free(bytes);
}

NSString *NSString_java_formatWithNSString_withNSObjectArray_(NSString *format,
                                                              IOSObjectArray *args) {
  JavaUtilFormatter *formatter = [[JavaUtilFormatter alloc] init];
  NSString *result = [[formatter formatWithNSString:format withNSObjectArray:args] description];
  RELEASE_(formatter);
  return result;
}

+ (NSString *)java_formatWithNSString:(NSString *)format withNSObjectArray:(IOSObjectArray *)args {
  return NSString_java_formatWithNSString_withNSObjectArray_(format, args);
}

NSString *NSString_java_formatWithJavaUtilLocale_withNSString_withNSObjectArray_(
    JavaUtilLocale *locale, NSString *format, IOSObjectArray *args) {
  JavaUtilFormatter *formatter =
      AUTORELEASE([[JavaUtilFormatter alloc] initWithJavaUtilLocale:locale]);
  return [[formatter formatWithNSString:format withNSObjectArray:args] description];
}

+ (NSString *)java_formatWithJavaUtilLocale:(JavaUtilLocale *)locale
                               withNSString:(NSString *)format
                          withNSObjectArray:(IOSObjectArray *)args {
  return NSString_java_formatWithJavaUtilLocale_withNSString_withNSObjectArray_(locale, format,
                                                                                args);
}

static jboolean RangeIsEqual(NSString *self, NSString *other, jint startIdx) {
  NSUInteger selfLen = [self length];
  NSUInteger rangeLen = [other length];
  if (startIdx < 0 || startIdx + rangeLen > selfLen) {
    return false;
  }
  NSRange range = NSMakeRange(startIdx, rangeLen);
  return [self compare:other options:NSLiteralSearch range:range] == NSOrderedSame;
}

- (jboolean)java_hasPrefix:(NSString *)prefix {
  (void)nil_chk(prefix);
  return RangeIsEqual(self, prefix, 0);
}

- (jboolean)java_hasPrefix:(NSString *)prefix offset:(jint)offset {
  (void)nil_chk(prefix);
  return RangeIsEqual(self, prefix, offset);
}

- (jboolean)java_hasSuffix:(NSString *)suffix {
  (void)nil_chk(suffix);
  return RangeIsEqual(self, suffix, (jint)[self length] - (jint)[suffix length]);
}

- (NSString *)java_trim {
  // Java's String.trim() trims characters <= u0020, not NSString whitespace.
  NSCharacterSet *trimCharacterSet = [NSCharacterSet characterSetWithRange:NSMakeRange(0, 0x21)];
  return [self stringByTrimmingCharactersInSet:trimCharacterSet];
}

- (IOSObjectArray *)java_split:(NSString *)str {
  if (!str) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  return [self java_split:str limit:0];
}

- (IOSObjectArray *)java_split:(NSString *)str limit:(jint)n {
  if (!str) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  IOSObjectArray *result;
  @autoreleasepool {
    JavaUtilRegexPattern *p = JavaUtilRegexPattern_compileWithNSString_(str);
    result = RETAIN_([p splitWithJavaLangCharSequence:self withInt:n]);
  }
  return AUTORELEASE(result);
}

- (jboolean)java_equalsIgnoreCase:(NSString *)aString {
  NSComparisonResult result = [self compare:aString options:NSCaseInsensitiveSearch];
  return result == NSOrderedSame;
}

- (NSString *)java_lowercaseStringWithJRELocale:(JavaUtilLocale *)javaLocale {
  if (!javaLocale) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:[javaLocale description]];
#if !__has_feature(objc_arc)
  [locale autorelease];
#endif
  CFMutableStringRef tempCFMString =
      CFStringCreateMutableCopy(NULL, 0, (ARCBRIDGE CFStringRef)self);
  CFStringLowercase(tempCFMString, (ARCBRIDGE CFLocaleRef)locale);
  NSString *result = [(ARCBRIDGE NSString *)tempCFMString copy];
  CFRelease(tempCFMString);
#if !__has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (NSString *)java_uppercaseStringWithJRELocale:(JavaUtilLocale *)javaLocale {
  if (!javaLocale) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  NSLocale *locale = [[NSLocale alloc] initWithLocaleIdentifier:[javaLocale description]];
#if !__has_feature(objc_arc)
  [locale autorelease];
#endif
  CFMutableStringRef tempCFMString =
      CFStringCreateMutableCopy(NULL, 0, (ARCBRIDGE CFStringRef)self);
  CFStringUppercase(tempCFMString, (ARCBRIDGE CFLocaleRef)locale);
  NSString *result = [(ARCBRIDGE NSString *)tempCFMString copy];
  CFRelease(tempCFMString);
#if !__has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (jboolean)java_regionMatches:(jint)thisOffset
                       aString:(NSString *)aString
                   otherOffset:(jint)otherOffset
                         count:(jint)count {
  return [self java_regionMatches:false
                       thisOffset:thisOffset
                          aString:aString
                      otherOffset:otherOffset
                            count:count];
}

- (jboolean)java_regionMatches:(jboolean)caseInsensitive
                    thisOffset:(jint)thisOffset
                       aString:(NSString *)aString
                   otherOffset:(jint)otherOffset
                         count:(jint)count {
  if (thisOffset < 0 || count > (jint)[self length] - thisOffset) {
    return false;
  }
  if (otherOffset < 0 || count > (jint)[aString length] - otherOffset) {
    return false;
  }
  if (!aString) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  NSString *this_ = (thisOffset == 0 && count == (jint)[self length])
                        ? self
                        : [self substringWithRange:NSMakeRange(thisOffset, count)];
  NSString *other = (otherOffset == 0 && count == (jint)[aString length])
                        ? aString
                        : [aString substringWithRange:NSMakeRange(otherOffset, count)];
  NSUInteger options = NSLiteralSearch;
  if (caseInsensitive) {
    options |= NSCaseInsensitiveSearch;
  }
  return [this_ compare:other options:options] == NSOrderedSame;
}

- (NSString *)java_intern {
  // No actual interning is done, since NSString doesn't support it.
  // Instead, any "string == otherString" expression is changed to
  // "string.equals(otherString)
  return self;
}

- (NSString *)java_concat:string {
  if (!string) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  return [self stringByAppendingString:string];
}

- (jboolean)java_contains:(id<JavaLangCharSequence>)sequence {
  if (!sequence) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  if ([sequence java_length] == 0) {
    return true;
  }
  NSRange range = [self rangeOfString:[sequence description]];
  return range.location != NSNotFound;
}

- (jint)java_codePointAt:(jint)index {
  return JavaLangCharacter_codePointAtWithJavaLangCharSequence_withInt_(self, index);
}

- (jint)java_codePointBefore:(jint)index {
  return JavaLangCharacter_codePointBeforeWithJavaLangCharSequence_withInt_(self, index);
}

- (jint)java_codePointCount:(jint)beginIndex endIndex:(jint)endIndex {
  return JavaLangCharacter_codePointCountWithJavaLangCharSequence_withInt_withInt_(self, beginIndex,
                                                                                   endIndex);
}

- (jint)java_offsetByCodePoints:(jint)index codePointOffset:(jint)offset {
  return JavaLangCharacter_offsetByCodePointsWithJavaLangCharSequence_withInt_withInt_(self, index,
                                                                                       offset);
}

- (jboolean)java_matches:(NSString *)regex {
  return JavaUtilRegexPattern_matchesWithNSString_withNSString_(regex, self);
}

- (jboolean)java_contentEqualsCharSequence:(id<JavaLangCharSequence>)seq {
  return [self isEqualToString:[(id)seq description]];
}

- (jboolean)java_contentEqualsStringBuffer:(JavaLangStringBuffer *)sb {
  return [self isEqualToString:[sb description]];
}

- (IOSClass *)java_getClass {
  return NSString_class_();
}

+ (NSString *)java_joinWithJavaLangCharSequence:(id<JavaLangCharSequence>)delimiter
                  withJavaLangCharSequenceArray:(IOSObjectArray *)elements {
  return NSString_java_joinWithJavaLangCharSequence_withJavaLangCharSequenceArray_(delimiter,
                                                                                   elements);
}

+ (NSString *)java_joinWithJavaLangCharSequence:(id<JavaLangCharSequence>)delimiter
                           withJavaLangIterable:(id<JavaLangIterable>)elements {
  return NSString_java_joinWithJavaLangCharSequence_withJavaLangIterable_(delimiter, elements);
}

- (NSString *)java_repeat:(jint)count {
  if (count < 0) {
    @throw create_JavaLangIllegalArgumentException_initWithNSString_(
        JreStrcat("$I", @"count is negative: ", count));
  }
  if (count == 1) {
    return self;
  }
  jint len = [((NSString *)nil_chk(self)) java_length];
  if (len == 0 || count == 0) {
    return @"";
  }
  if (JreIntDiv(JavaLangInteger_MAX_VALUE, count) < len) {
    @throw create_JavaLangOutOfMemoryError_initWithNSString_(
        JreStrcat("$I$I$", @"Repeating ", len, @" bytes String ", count,
                  @" times will produce a String exceeding maximum size."));
  }

  NSMutableString *result = [NSMutableString stringWithCapacity:self.length * count];
  for (NSUInteger i = 0; i < count; i++) {
    [result appendString:self];
  }
  return result;
}

- (NSString *)java_strip {
  NSString *ret = JavaLangStringUTF16_stripWithNSString_(self);
  return ret == nil ? self : ret;
}

- (NSString *)java_stripLeading {
  NSString *ret = JavaLangStringUTF16_stripLeadingWithNSString_(self);
  return ret == nil ? self : ret;
}

- (NSString *)java_stripTrailing {
  NSString *ret = JavaLangStringUTF16_stripTrailingWithNSString_(self);
  return ret == nil ? self : ret;
}

- (jboolean)java_isBlank {
  NSUInteger length = [self length];
  jint left = 0;
  while (left < length) {
    jint codepoint = JavaLangCharacter_codePointAtWithJavaLangCharSequence_withInt_(self, left);
    if (codepoint != ' ' && codepoint != 0x0009 &&
        !JavaLangCharacter_isWhitespaceWithInt_(codepoint)) {
      return false;
    }
  }
  return true;
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
      {NULL, NULL, 0x1, -1, -1, -1, -1, -1, -1},
      {NULL, NULL, 0x1, -1, 0, -1, -1, -1, -1},
      {NULL, NULL, 0x1, -1, 1, -1, -1, -1, -1},
      {NULL, NULL, 0x1, -1, 2, -1, -1, -1, -1},
      {NULL, NULL, 0x1, -1, 3, -1, -1, -1, -1},
      {NULL, NULL, 0x1, -1, 4, -1, -1, 5, -1},
      {NULL, NULL, 0x1, -1, 6, -1, -1, 7, -1},
      {NULL, NULL, 0x1, -1, 8, 9, -1, -1, -1},
      {NULL, NULL, 0x1, -1, 10, -1, -1, -1, -1},
      {NULL, NULL, 0x1, -1, 11, 9, -1, -1, -1},
      {NULL, NULL, 0x1, -1, 12, -1, -1, -1, -1},
      {NULL, NULL, 0x1, -1, 13, -1, -1, -1, -1},
      {NULL, NULL, 0x1, -1, 14, -1, -1, -1, -1},
      {NULL, NULL, 0x1, -1, 15, -1, -1, -1, -1},
      {NULL, NULL, 0x1, -1, 16, -1, -1, -1, -1},
      {NULL, "I", 0x1, 17, -1, -1, -1, -1, -1},
      {NULL, "Z", 0x1, 18, -1, -1, -1, -1, -1},
      {NULL, "C", 0x1, 19, 20, -1, -1, -1, -1},
      {NULL, "I", 0x1, 21, 20, -1, -1, -1, -1},
      {NULL, "I", 0x1, 22, 20, -1, -1, -1, -1},
      {NULL, "I", 0x1, 23, 24, -1, -1, -1, -1},
      {NULL, "I", 0x1, 25, 24, -1, -1, -1, -1},
      {NULL, "V", 0x1, 26, 27, -1, -1, -1, -1},
      {NULL, "V", 0x1, 28, 29, -1, -1, 30, -1},
      {NULL, "[B", 0x1, 28, 0, 9, -1, -1, -1},
      {NULL, "[B", 0x1, 28, 31, -1, -1, -1, -1},
      {NULL, "[B", 0x1, 28, -1, -1, -1, -1, -1},
      {NULL, "Z", 0x1, 32, 33, -1, -1, -1, -1},
      {NULL, "Z", 0x1, 34, 15, -1, -1, -1, -1},
      {NULL, "Z", 0x1, 34, 35, -1, -1, -1, -1},
      {NULL, "Z", 0x1, 36, 0, -1, -1, -1, -1},
      {NULL, "I", 0x1, 37, 0, -1, -1, -1, -1},
      {NULL, "I", 0x1, 38, 0, -1, -1, -1, -1},
      {NULL, "Z", 0x1, 39, 40, -1, -1, -1, -1},
      {NULL, "Z", 0x1, 39, 41, -1, -1, -1, -1},
      {NULL, "Z", 0x1, 42, 43, -1, -1, -1, -1},
      {NULL, "Z", 0x1, 42, 0, -1, -1, -1, -1},
      {NULL, "Z", 0x1, 44, 0, -1, -1, -1, -1},
      {NULL, "I", 0x1, 45, -1, -1, -1, -1, -1},
      {NULL, "I", 0x1, 46, 20, -1, -1, -1, -1},
      {NULL, "I", 0x1, 46, 24, -1, -1, -1, -1},
      {NULL, "I", 0x1, 47, 20, -1, -1, -1, -1},
      {NULL, "I", 0x1, 47, 24, -1, -1, -1, -1},
      {NULL, "I", 0x1, 46, 0, -1, -1, -1, -1},
      {NULL, "I", 0x1, 46, 43, -1, -1, -1, -1},
      {NULL, "I", 0x8, 46, 48, -1, -1, -1, -1},
      {NULL, "I", 0x1, 47, 0, -1, -1, -1, -1},
      {NULL, "I", 0x1, 47, 43, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x1, 49, 20, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x1, 49, 24, -1, -1, -1, -1},
      {NULL, "LJavaLangCharSequence;", 0x1, 50, 24, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x1, 51, 0, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x1, 52, 53, -1, -1, -1, -1},
      {NULL, "Z", 0x1, 54, 0, -1, -1, -1, -1},
      {NULL, "Z", 0x1, 55, 35, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x1, 56, 57, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x1, 58, 57, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x1, 52, 59, -1, -1, -1, -1},
      {NULL, "[LNSString;", 0x1, 60, 43, -1, -1, -1, -1},
      {NULL, "[LNSString;", 0x1, 60, 0, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x89, 61, 62, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x9, 61, 63, -1, 64, -1, -1},
      {NULL, "LNSString;", 0x1, 65, 66, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x1, 65, -1, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x1, 67, 66, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x1, 67, -1, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x1, 68, -1, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x1, 69, -1, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x1, 70, -1, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x1, 71, -1, -1, -1, -1, -1},
      {NULL, "Z", 0x1, 72, -1, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x1, 73, -1, -1, -1, -1, -1},
      {NULL, "[C", 0x1, 74, -1, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x89, 75, 76, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x89, 75, 77, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x9, 78, 33, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x9, 78, 1, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x9, 78, 2, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x9, 79, 2, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x9, 79, 1, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x9, 78, 80, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x9, 78, 81, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x9, 78, 20, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x9, 78, 82, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x9, 78, 83, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x9, 78, 84, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x1, 85, -1, -1, -1, -1, -1},
      {NULL, "LNSString;", 0x1, 86, 20, -1, -1, -1, -1},
  };
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wobjc-multiple-method-names"
#pragma clang diagnostic ignored "-Wundeclared-selector"
  methods[0].selector = @selector(string);
  methods[1].selector = @selector(stringWithString:);
  methods[2].selector = @selector(java_stringWithCharacters:);
  methods[3].selector = @selector(java_stringWithCharacters:offset:length:);
  methods[4].selector = @selector(java_stringWithInts:offset:length:);
  methods[5].selector = @selector(java_stringWithBytes:hibyte:offset:length:);
  methods[6].selector = @selector(java_stringWithBytes:hibyte:);
  methods[7].selector = @selector(java_stringWithBytes:offset:length:charsetName:);
  methods[8].selector = @selector(java_stringWithBytes:offset:length:charset:);
  methods[9].selector = @selector(java_stringWithBytes:charsetName:);
  methods[10].selector = @selector(java_stringWithBytes:charset:);
  methods[11].selector = @selector(java_stringWithBytes:offset:length:);
  methods[12].selector = @selector(java_stringWithBytes:);
  methods[13].selector = @selector(java_stringWithJavaLangStringBuffer:);
  methods[14].selector = @selector(java_stringWithJavaLangStringBuilder:);
  methods[15].selector = @selector(java_length);
  methods[16].selector = @selector(java_isEmpty);
  methods[17].selector = @selector(charAtWithInt:);
  methods[18].selector = @selector(java_codePointAt:);
  methods[19].selector = @selector(java_codePointBefore:);
  methods[20].selector = @selector(java_codePointCount:endIndex:);
  methods[21].selector = @selector(java_offsetByCodePoints:codePointOffset:);
  methods[22].selector = @selector(java_getChars:sourceEnd:destination:destinationBegin:);
  methods[23].selector = @selector(java_getBytesWithSrcBegin:withSrcEnd:withDst:withDstBegin:);
  methods[24].selector = @selector(java_getBytesWithCharsetName:);
  methods[25].selector = @selector(java_getBytesWithCharset:);
  methods[26].selector = @selector(java_getBytes);
  methods[27].selector = @selector(isEqual:);
  methods[28].selector = @selector(java_contentEqualsStringBuffer:);
  methods[29].selector = @selector(java_contentEqualsCharSequence:);
  methods[30].selector = @selector(java_equalsIgnoreCase:);
  methods[31].selector = @selector(compareToWithId:);
  methods[32].selector = @selector(java_compareToIgnoreCase:);
  methods[33].selector = @selector(java_regionMatches:aString:otherOffset:count:);
  methods[34].selector = @selector(java_regionMatches:thisOffset:aString:otherOffset:count:);
  methods[35].selector = @selector(java_hasPrefix:offset:);
  methods[36].selector = @selector(java_hasPrefix:);
  methods[37].selector = @selector(java_hasSuffix:);
  methods[38].selector = @selector(hash);
  methods[39].selector = @selector(java_indexOf:);
  methods[40].selector = @selector(java_indexOf:fromIndex:);
  methods[41].selector = @selector(java_lastIndexOf:);
  methods[42].selector = @selector(java_lastIndexOf:fromIndex:);
  methods[43].selector = @selector(java_indexOfString:);
  methods[44].selector = @selector(java_indexOfString:fromIndex:);
  methods[45].selector = @selector(indexOfWithByteArray:withByte:withInt:withNSString:withInt:);
  methods[46].selector = @selector(java_lastIndexOfString:);
  methods[47].selector = @selector(java_lastIndexOfString:fromIndex:);
  methods[48].selector = @selector(java_substring:);
  methods[49].selector = @selector(java_substring:endIndex:);
  methods[50].selector = @selector(subSequenceFrom:to:);
  methods[51].selector = @selector(java_concat:);
  methods[52].selector = @selector(java_replace:withChar:);
  methods[53].selector = @selector(java_matches:);
  methods[54].selector = @selector(java_contains:);
  methods[55].selector = @selector(java_replaceFirst:withReplacement:);
  methods[56].selector = @selector(java_replaceAll:withReplacement:);
  methods[57].selector = @selector(java_replace:withSequence:);
  methods[58].selector = @selector(java_split:limit:);
  methods[59].selector = @selector(java_split:);
  methods[60].selector = @selector(java_joinWithJavaLangCharSequence:
                                       withJavaLangCharSequenceArray:);
  methods[61].selector = @selector(java_joinWithJavaLangCharSequence:withJavaLangIterable:);
  methods[62].selector = @selector(java_lowercaseStringWithJRELocale:);
  methods[63].selector = @selector(lowercaseString);
  methods[64].selector = @selector(java_uppercaseStringWithJRELocale:);
  methods[65].selector = @selector(uppercaseString);
  methods[66].selector = @selector(java_trim);
  methods[67].selector = @selector(java_strip);
  methods[68].selector = @selector(java_stripLeading);
  methods[69].selector = @selector(java_stripTrailing);
  methods[70].selector = @selector(java_isBlank);
  methods[71].selector = @selector(description);
  methods[72].selector = @selector(java_toCharArray);
  methods[73].selector = @selector(java_formatWithNSString:withNSObjectArray:);
  methods[74].selector = @selector(java_formatWithJavaUtilLocale:withNSString:withNSObjectArray:);
  methods[75].selector = @selector(java_valueOf:);
  methods[76].selector = @selector(java_valueOfChars:);
  methods[77].selector = @selector(java_valueOfChars:offset:count:);
  methods[78].selector = @selector(java_valueOfChars:offset:count:);
  methods[79].selector = @selector(java_valueOfChars:);
  methods[80].selector = @selector(java_valueOfBool:);
  methods[81].selector = @selector(java_valueOfChar:);
  methods[82].selector = @selector(java_valueOfInt:);
  methods[83].selector = @selector(java_valueOfLong:);
  methods[84].selector = @selector(java_valueOfFloat:);
  methods[85].selector = @selector(java_valueOfDouble:);
  methods[86].selector = @selector(java_intern);
  methods[87].selector = @selector(java_repeat:);
#pragma clang diagnostic pop
  static const J2ObjcFieldInfo fields[] = {
      {"serialVersionUID", "J", .constantValue.asLong = NSString_serialVersionUID, 0x1a, -1, -1, -1,
       -1},
      {"serialPersistentFields", "[LJavaIoObjectStreamField;", .constantValue.asLong = 0, 0x1a, -1,
       87, -1, -1},
      {"CASE_INSENSITIVE_ORDER", "LJavaUtilComparator;", .constantValue.asLong = 0, 0x19, -1, 88,
       89, -1},
  };
  static const void *ptrTable[] = {
      "LNSString;",
      "[C",
      "[CII",
      "[III",
      "[BIII",
      (void *)&NSString__Annotations$0,
      "[BI",
      (void *)&NSString__Annotations$1,
      "[BIILNSString;",
      "LJavaIoUnsupportedEncodingException;",
      "[BIILJavaNioCharsetCharset;",
      "[BLNSString;",
      "[BLJavaNioCharsetCharset;",
      "[BII",
      "[B",
      "LJavaLangStringBuffer;",
      "LJavaLangStringBuilder;",
      "length",
      "isEmpty",
      "charAt",
      "I",
      "codePointAt",
      "codePointBefore",
      "codePointCount",
      "II",
      "offsetByCodePoints",
      "getChars",
      "II[CI",
      "getBytes",
      "II[BI",
      (void *)&NSString__Annotations$2,
      "LJavaNioCharsetCharset;",
      "equals",
      "LNSObject;",
      "contentEquals",
      "LJavaLangCharSequence;",
      "equalsIgnoreCase",
      "compareTo",
      "compareToIgnoreCase",
      "regionMatches",
      "ILNSString;II",
      "ZILNSString;II",
      "startsWith",
      "LNSString;I",
      "endsWith",
      "hashCode",
      "indexOf",
      "lastIndexOf",
      "[BBILNSString;I",
      "substring",
      "subSequence",
      "concat",
      "replace",
      "CC",
      "matches",
      "contains",
      "replaceFirst",
      "LNSString;LNSString;",
      "replaceAll",
      "LJavaLangCharSequence;LJavaLangCharSequence;",
      "split",
      "join",
      "LJavaLangCharSequence;[LJavaLangCharSequence;",
      "LJavaLangCharSequence;LJavaLangIterable;",
      "(Ljava/lang/CharSequence;Ljava/lang/Iterable<+Ljava/lang/CharSequence;>;)Ljava/lang/String;",
      "toLowerCase",
      "LJavaUtilLocale;",
      "toUpperCase",
      "trim",
      "strip",
      "stripLeading",
      "stripTrailing",
      "isBlank",
      "toString",
      "toCharArray",
      "format",
      "LNSString;[LNSObject;",
      "LJavaUtilLocale;LNSString;[LNSObject;",
      "valueOf",
      "copyValueOf",
      "Z",
      "C",
      "J",
      "F",
      "D",
      "intern",
      "repeat",
      &NSString_serialPersistentFields,
      &NSString_CASE_INSENSITIVE_ORDER,
      "Ljava/util/Comparator<Ljava/lang/String;>;",
      "LNSString_CaseInsensitiveComparator;",
      "Ljava/lang/Object;Ljava/io/Serializable;Ljava/lang/Comparable<Ljava/lang/String;>;"
      "Ljava/lang/CharSequence;"};
  static const J2ObjcClassInfo _NSString = {
      "String", "java.lang", ptrTable, methods, fields, 7, 0x11, 88, 3, -1, 90, -1, 91, -1};
  return &_NSString;
}

@end

NSString *NSString_java_joinWithJavaLangCharSequence_withJavaLangCharSequenceArray_(
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

NSString *NSString_java_joinWithJavaLangCharSequence_withJavaLangIterable_(
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

@interface NSString_CaseInsensitiveComparator : NSObject <JavaUtilComparator, JavaIoSerializable>
@end

@implementation NSString_CaseInsensitiveComparator

- (jint)compareWithId:(NSString *)o1 withId:(NSString *)o2 {
  if (!o1) {
    @throw makeException([JavaLangNullPointerException class]);
  }
  return [o1 java_compareToIgnoreCase:o2];
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
                              (id<JavaUtilFunctionFunction>)arg0
                                             withJavaUtilComparator:(id<JavaUtilComparator>)arg1 {
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
      {NULL, NULL, 0x2, -1, -1, -1, -1, -1, -1},
      {NULL, "I", 0x1, 0, 1, -1, -1, -1, -1},
  };
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wobjc-multiple-method-names"
#pragma clang diagnostic ignored "-Wundeclared-selector"
  methods[0].selector = @selector(init);
  methods[1].selector = @selector(compareWithId:withId:);
#pragma clang diagnostic pop
  static const J2ObjcFieldInfo fields[] = {
      {"serialVersionUID", "J",
       .constantValue.asLong = NSString_CaseInsensitiveComparator_serialVersionUID, 0x1a, -1, -1,
       -1, -1},
  };
  static const void *ptrTable[] = {
      "compare", "LNSString;LNSString;", "LNSString;",
      "Ljava/lang/Object;Ljava/util/Comparator<Ljava/lang/String;>;Ljava/io/Serializable;"};
  static const J2ObjcClassInfo _NSString_CaseInsensitiveComparator = {"CaseInsensitiveComparator",
                                                                      "java.lang",
                                                                      ptrTable,
                                                                      methods,
                                                                      fields,
                                                                      7,
                                                                      0xa,
                                                                      2,
                                                                      1,
                                                                      2,
                                                                      -1,
                                                                      -1,
                                                                      3,
                                                                      -1};
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
                              [IOSObjectArray newArrayWithLength:0
                                                            type:JavaIoObjectStreamField_class_()]);
    J2OBJC_SET_INITIALIZED(NSString)
  }
}

@end

IOSObjectArray *NSString__Annotations$0() {
  return [IOSObjectArray arrayWithObjects:(id[]){create_JavaLangDeprecated(false, @"")}
                                    count:1
                                     type:JavaLangAnnotationAnnotation_class_()];
}

IOSObjectArray *NSString__Annotations$1() {
  return [IOSObjectArray arrayWithObjects:(id[]){create_JavaLangDeprecated(false, @"")}
                                    count:1
                                     type:JavaLangAnnotationAnnotation_class_()];
}

IOSObjectArray *NSString__Annotations$2() {
  return [IOSObjectArray arrayWithObjects:(id[]){create_JavaLangDeprecated(false, @"")}
                                    count:1
                                     type:JavaLangAnnotationAnnotation_class_()];
}

IOSObjectArray *NSString__Annotations$3() {
  return [IOSObjectArray arrayWithObjects:(id[]){create_JavaLangDeprecated(false, @"")}
                                    count:1
                                     type:JavaLangAnnotationAnnotation_class_()];
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(NSString)

J2OBJC_NAME_MAPPING(NSString, "java.lang.String", "NSString")
