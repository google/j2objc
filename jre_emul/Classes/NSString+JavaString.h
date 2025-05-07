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
//  NSString+JavaString.h
//  JreEmulation
//
//  Created by Tom Ball on 8/24/11.
//

#if __has_feature(nullability)
#pragma clang diagnostic push
#pragma GCC diagnostic ignored "-Wnullability"
#pragma GCC diagnostic ignored "-Wnullability-completeness"
#endif

#ifndef _NSString_JavaString_H_
#define _NSString_JavaString_H_

#import "IOSObjectArray.h"
#import "IOSPrimitiveArray.h"
#import "J2ObjC_header.h"
#import "java/io/Serializable.h"
#import "java/lang/CharSequence.h"
#import "java/lang/Comparable.h"

@class JavaLangStringBuffer;
@class JavaLangStringBuilder;
@class JavaNioCharsetCharset;
@class JavaUtilLocale;
@protocol JavaLangIterable;
@protocol JavaUtilComparator;
@protocol JavaUtilFunctionFunction;
@protocol JavaUtilStreamStream;

// A category that adds java.lang.String-like methods to NSString.  The method
// list is not exhaustive, since methods that can be directly substituted are
// inlined.
@interface NSString (JavaString) <JavaIoSerializable, JavaLangComparable, JavaLangCharSequence>

// String.valueOf(Object)
+ (nonnull NSString *)java_valueOf:(id<NSObject>)obj;

// String.valueOf(boolean)
+ (nonnull NSString *)java_valueOfBool:(bool)value;

// String.valueOf(char)
+ (nonnull NSString *)java_valueOfChar:(uint16_t)value;

// String.valueOf(char[])
+ (nonnull NSString *)java_valueOfChars:(IOSCharArray *)data;

// String.valueOf(char[], offset, count)
+ (nonnull NSString *)java_valueOfChars:(IOSCharArray *)data
                                 offset:(int32_t)offset
                                  count:(int32_t)count;

// String.valueOf(double)
+ (nonnull NSString *)java_valueOfDouble:(double)value;

// String.valueOf(float)
+ (nonnull NSString *)java_valueOfFloat:(float)value;

// String.valueOf(int)
+ (nonnull NSString *)java_valueOfInt:(int32_t)value;

// String.valueOf(long)
+ (nonnull NSString *)java_valueOfLong:(int64_t)value;

// String.getChars(int, int, char[], int)
- (void)java_getChars:(int32_t)sourceBegin
            sourceEnd:(int32_t)sourceEnd
          destination:(IOSCharArray *)dest
     destinationBegin:(int32_t)dstBegin;

// String(byte[])
+ (NSString *)java_stringWithBytes:(IOSByteArray *)value;

// String(byte[], int)
+ (NSString *)java_stringWithBytes:(IOSByteArray *)value
                            hibyte:(int32_t)hibyte;

// String(byte[], int, int)
+ (NSString *)java_stringWithBytes:(IOSByteArray *)value
                            offset:(int32_t)offset
                            length:(int32_t)count;

+ (NSString *)java_stringWithBytes:(IOSByteArray *)value
                            hibyte:(NSUInteger)hibyte
                            offset:(NSUInteger)offset
                            length:(NSUInteger)length;

// String(byte[], String)
+ (NSString *)java_stringWithBytes:(IOSByteArray *)value
                       charsetName:(NSString *)charsetName;

// String(byte[], Charset)
+ (NSString *)java_stringWithBytes:(IOSByteArray *)value
                           charset:(JavaNioCharsetCharset *)charset;

// String(byte[], int, int, String)
+ (NSString *)java_stringWithBytes:(IOSByteArray *)value
                            offset:(int32_t)offset
                            length:(int32_t)count
                       charsetName:(NSString *)charsetName;

// String(byte[], int, int, Charset)
+ (NSString *)java_stringWithBytes:(IOSByteArray *)value
                            offset:(int32_t)offset
                            length:(int32_t)count
                          charset:(JavaNioCharsetCharset *)charset;

// String(char[])
+ (NSString *)java_stringWithCharacters:(IOSCharArray *)value;

// String(char[], int, int)
+ (NSString *)java_stringWithCharacters:(IOSCharArray *)value
                                 offset:(int32_t)offset
                                 length:(int32_t)count;

// String(int[], int, int)
+ (NSString *)java_stringWithInts:(IOSIntArray *)codePoints
                           offset:(int32_t)offset
                           length:(int32_t)count;

// String(StringBuffer)
+ (NSString *)java_stringWithJavaLangStringBuffer:(JavaLangStringBuffer *)sb;

// String(StringBuilder)
+ (NSString *)java_stringWithJavaLangStringBuilder:(JavaLangStringBuilder *)sb;

// String.substring(int)
- (nonnull NSString *)java_substring:(int32_t)beginIndex;

// String.substring(int, int)
- (nonnull NSString *)java_substring:(int32_t)beginIndex
                            endIndex:(int32_t)endIndex;

// String.indexOf(int)
- (int32_t)java_indexOf:(int32_t)ch;

// String.indexOf(int, int)
- (int32_t)java_indexOf:(int32_t)ch fromIndex:(int32_t)index;

// String.indexOf(String)
- (int32_t)java_indexOfString:(NSString *)s;

// String.indexOf(String, int)
- (int32_t)java_indexOfString:(NSString *)s fromIndex:(int32_t)index;

// String.isEmpty()
- (bool)java_isEmpty;

// String.lastIndexOf(int)
- (int32_t)java_lastIndexOf:(int32_t)ch;

// String.lastIndexOf(int, int)
- (int32_t)java_lastIndexOf:(int32_t)ch fromIndex:(int32_t)index;

// String.lastIndexOf(String)
- (int32_t)java_lastIndexOfString:(NSString *)s;

// String.lastIndexOf(String, int)
- (int32_t)java_lastIndexOfString:(NSString *)s fromIndex:(int32_t)index;

// String.length()
- (int32_t)java_length;

// String.toCharArray()
- (nonnull IOSCharArray *)java_toCharArray;

// java.lang.Comparable implementation methods
- (int32_t)compareToWithId:(id)another;

// CharSequence.charAt(int)
- (uint16_t)charAtWithInt:(int32_t)index;

// CharSequence.isEmpty()
- (bool)isEmpty;

// CharSequence.subSequence(int, int)
- (nonnull id<JavaLangCharSequence>)subSequenceFrom:(int32_t)start
                                                 to:(int32_t)end;

// String.compareToIgnoreCase(String)
- (int32_t)java_compareToIgnoreCase:(NSString *)another;

// String.replace(char, char)
- (nonnull NSString *)java_replace:(uint16_t)oldchar withChar:(uint16_t)newchar;

// String.replace(CharSequence, CharSequence)
- (nonnull NSString *)java_replace:(id<JavaLangCharSequence>)oldSequence
                      withSequence:(id<JavaLangCharSequence>)newSequence;

// String.replaceAll(String, String)
- (nonnull NSString *)java_replaceAll:(NSString *)regex
                      withReplacement:(NSString *)replacement;

// String.replaceFirst(String, String)
- (nonnull NSString *)java_replaceFirst:(NSString *)regex
                        withReplacement:(NSString *)replacement;

// String.getBytes()
- (nonnull IOSByteArray *)java_getBytes;

// String.getBytes(String)
- (nonnull IOSByteArray *)java_getBytesWithCharsetName:(NSString *)charsetName;

// String.getBytes(Charset)
- (nonnull IOSByteArray *)java_getBytesWithCharset:(JavaNioCharsetCharset *)charset;

// String.getBytes(int, int, byte[], int)
- (void)java_getBytesWithSrcBegin:(int32_t)srcBegin
                       withSrcEnd:(int32_t)srcEnd
                          withDst:(IOSByteArray *)dst
                     withDstBegin:(int32_t)dstBegin;

// String.format(String, ...), String.format(Locale, String, ...)
+ (nonnull NSString *)java_formatWithNSString:(NSString *)format
                            withNSObjectArray:(IOSObjectArray *)args;
+ (nonnull NSString *)java_formatWithJavaUtilLocale:(JavaUtilLocale *)locale
                                       withNSString:(NSString *)format
                                  withNSObjectArray:(IOSObjectArray *)args;

// String.startsWith(String), String.startsWith(String, int), String.endsWith(String)
- (bool)java_hasPrefix:(NSString *)prefix;
- (bool)java_hasPrefix:(NSString *)prefix offset:(int32_t)offset;
- (bool)java_hasSuffix:(NSString *)suffix;

// String.trim()
- (nonnull NSString *)java_trim;

// String.split(String)
- (nonnull IOSObjectArray *)java_split:(NSString *)str;

// String equalsIgnoreCase(String)
- (bool)java_equalsIgnoreCase:(NSString *)aString;

// String.toLowerCase(Locale), toUpperCase(Locale)
- (nonnull NSString *)java_lowercaseStringWithJRELocale:(JavaUtilLocale *)locale;
- (nonnull NSString *)java_uppercaseStringWithJRELocale:(JavaUtilLocale *)locale;

// String.regionMatches(...)
- (bool)java_regionMatches:(int32_t)thisOffset
                   aString:(NSString *)aString
               otherOffset:(int32_t)otherOffset
                     count:(int32_t)count;

- (bool)java_regionMatches:(bool)caseInsensitive
                thisOffset:(int32_t)thisOffset
                   aString:(NSString *)aString
               otherOffset:(int32_t)otherOffset
                     count:(int32_t)count;

// String.intern()
- (nonnull NSString *)java_intern;

// String.concat(String)
- (nonnull NSString *)java_concat:string;

// String.contains(CharSequence)
- (bool)java_contains:(id<JavaLangCharSequence>)sequence;

// String.codePointAt(int), codePointBefore(int), codePointCount(int, int)
- (int32_t)java_codePointAt:(int32_t)index;
- (int32_t)java_codePointBefore:(int32_t)index;
- (int32_t)java_codePointCount:(int32_t)beginIndex endIndex:(int32_t)endIndex;

// String.matches(), split(String, int)
- (bool)java_matches:(NSString *)regex;
- (nonnull IOSObjectArray *)java_split:(NSString *)regex limit:(int32_t)limit;

// String.contentEquals(CharSequence), contentEquals(StringBuffer)
- (bool)java_contentEqualsCharSequence:(id<JavaLangCharSequence>)seq;
- (bool)java_contentEqualsStringBuffer:(JavaLangStringBuffer *)sb;

// String.offsetByCodePoints(int, int)
- (int32_t)java_offsetByCodePoints:(int32_t)index codePointOffset:(int32_t)offset;

// String.join(CharSequence, CharSequence...)
+ (nonnull NSString *)java_joinWithJavaLangCharSequence:(id<JavaLangCharSequence>)delimiter
                          withJavaLangCharSequenceArray:(IOSObjectArray *)elements;

// String.join(CharSequence, Iterable<? extends CharSequence>)
+ (nonnull NSString *)java_joinWithJavaLangCharSequence:(id<JavaLangCharSequence>)delimiter
                                   withJavaLangIterable:(id<JavaLangIterable>)elements;

// String.repeat(int)
- (nonnull NSString *)java_repeat:(int32_t)count;

// String.strip()
- (nonnull NSString *)java_strip;

// String.stripLeading()
- (nonnull NSString *)java_stripLeading;

// String.stripTrailing()
- (nonnull NSString *)java_stripTrailing;

// String.isBlank()
- (bool)java_isBlank;

// String.lines()
- (id<JavaUtilStreamStream>)java_lines;

- (nonnull NSString *)java_indent:(int32_t)n;

// String.stripIndent()
- (nonnull NSString *)java_stripIndent;

// String.translateEscapes()
- (nonnull NSString *)java_translateEscapes;

// String.formatted(Object...)
- (NSString *)java_formattedWithNSObjectArray:(IOSObjectArray *)args;

// String.transform(Function)
- (nonnull id)transformWithJavaUtilFunctionFunction:(id<JavaUtilFunctionFunction>)f;

@end

// String.format(Locale, String, Object...)
FOUNDATION_EXPORT NSString *NSString_java_formatWithJavaUtilLocale_withNSString_withNSObjectArray_(
    JavaUtilLocale *l, NSString *s, IOSObjectArray *objs);
// String.format(String, Object...)
FOUNDATION_EXPORT NSString *NSString_java_formatWithNSString_withNSObjectArray_(
    NSString *s, IOSObjectArray *objs);
// String.valueOf(boolean)
FOUNDATION_EXPORT NSString *NSString_java_valueOfBool_(bool b);
// String.valueOf(char)
FOUNDATION_EXPORT NSString *NSString_java_valueOfChar_(uint16_t c);
// String.valueOf(char[])
// String.copyValueOf(char[])
FOUNDATION_EXPORT NSString *NSString_java_valueOfChars_(IOSCharArray *chars);
// String.valueOf(char[], int, int)
// String.copyValueOf(char[], int, int)
FOUNDATION_EXPORT NSString *NSString_java_valueOfChars_offset_count_(
    IOSCharArray *chars, int32_t i, int32_t j);
// String.valueOf(double)
FOUNDATION_EXPORT NSString *NSString_java_valueOfDouble_(double d);
// String.valueOf(float)
FOUNDATION_EXPORT NSString *NSString_java_valueOfFloat_(float f);
// String.valueOf(int)
FOUNDATION_EXPORT NSString *NSString_java_valueOfInt_(int32_t i);
// String.valueOf(long)
FOUNDATION_EXPORT NSString *NSString_java_valueOfLong_(int64_t l);
// String.valueOf(Object)
FOUNDATION_EXPORT NSString *NSString_java_valueOf_(id o);
// String.join(CharSequence, CharSequence...)
FOUNDATION_EXPORT NSString *
NSString_java_joinWithJavaLangCharSequence_withJavaLangCharSequenceArray_(
    id<JavaLangCharSequence> delimiter, IOSObjectArray *elements);
// String.join(CharSequence, Iterable<? extends CharSequence>)
FOUNDATION_EXPORT NSString *NSString_java_joinWithJavaLangCharSequence_withJavaLangIterable_(
    id<JavaLangCharSequence> delimiter, id<JavaLangIterable> elements);

// Empty class to force category to be loaded.
@interface JreStringCategoryDummy : NSObject
@end

// Use the category dummy to initialize static variables for the String class.
FOUNDATION_EXPORT _Atomic(bool) NSString__initialized;
__attribute__((always_inline)) inline void NSString_initialize(void) {
  if (__builtin_expect(!__c11_atomic_load(&NSString__initialized, __ATOMIC_ACQUIRE), 0)) {
    [JreStringCategoryDummy class];
  }
}

inline id<JavaUtilComparator> NSString_get_CASE_INSENSITIVE_ORDER(void);
/*! INTERNAL ONLY - Use accessor function from above. */
FOUNDATION_EXPORT id<JavaUtilComparator> NSString_CASE_INSENSITIVE_ORDER;
J2OBJC_STATIC_FIELD_OBJ_FINAL(NSString, CASE_INSENSITIVE_ORDER, id<JavaUtilComparator>)

inline IOSObjectArray *NSString_get_serialPersistentFields(void);
/*! INTERNAL ONLY - Use accessor function from above. */
FOUNDATION_EXPORT IOSObjectArray *NSString_serialPersistentFields;
J2OBJC_STATIC_FIELD_OBJ_FINAL(NSString, serialPersistentFields, IOSObjectArray *)

J2OBJC_TYPE_LITERAL_HEADER(NSString)

#endif // _NSString_JavaString_H_

#if __has_feature(nullability)
#pragma clang diagnostic pop
#endif
