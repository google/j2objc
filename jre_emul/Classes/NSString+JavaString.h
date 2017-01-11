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
//  NSString+JavaString.h
//  JreEmulation
//
//  Created by Tom Ball on 8/24/11.
//

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

// A category that adds java.lang.String-like methods to NSString.  The method
// list is not exhaustive, since methods that can be directly substituted are
// inlined.  For example, "foo".length() is directly translated to
// [@"foo" length].
@interface NSString (JavaString) <JavaIoSerializable, JavaLangComparable, JavaLangCharSequence>

// String.valueOf(Object)
+ (NSString *)java_valueOf:(id<NSObject>)obj;

// String.valueOf(boolean)
+ (NSString *)java_valueOfBool:(jboolean)value;

// String.valueOf(char)
+ (NSString *)java_valueOfChar:(jchar)value;

// String.valueOf(char[])
+ (NSString *)java_valueOfChars:(IOSCharArray *)data;

// String.valueOf(char[], offset, count)
+ (NSString *)java_valueOfChars:(IOSCharArray *)data
                         offset:(int)offset
                          count:(int)count;

// String.valueOf(double)
+ (NSString *)java_valueOfDouble:(double)value;

// String.valueOf(float)
+ (NSString *)java_valueOfFloat:(float)value;

// String.valueOf(int)
+ (NSString *)java_valueOfInt:(int)value;

// String.valueOf(long)
+ (NSString *)java_valueOfLong:(long long int)value;

// String.getChars(int, int, char[], int)
- (void)java_getChars:(int)sourceBegin
            sourceEnd:(int)sourceEnd
          destination:(IOSCharArray *)dest
     destinationBegin:(int)dstBegin;

// String(byte[])
+ (NSString *)java_stringWithBytes:(IOSByteArray *)value;

// String(byte[], int)
+ (NSString *)java_stringWithBytes:(IOSByteArray *)value
                            hibyte:(int)hibyte;

// String(byte[], int, int)
+ (NSString *)java_stringWithBytes:(IOSByteArray *)value
                            offset:(int)offset
                            length:(int)count;

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
                            offset:(int)offset
                            length:(int)count
                       charsetName:(NSString *)charsetName;

// String(byte[], int, int, Charset)
+ (NSString *)java_stringWithBytes:(IOSByteArray *)value
                            offset:(int)offset
                            length:(int)count
                          charset:(JavaNioCharsetCharset *)charset;

+ (NSString *)java_stringWithBytes:(IOSByteArray *)value
                            offset:(int)offset
                            length:(int)count
                          encoding:(NSStringEncoding)encoding;

// String(char[])
+ (NSString *)java_stringWithCharacters:(IOSCharArray *)value;

// String(char[], int, int)
+ (NSString *)java_stringWithCharacters:(IOSCharArray *)value
                                 offset:(int)offset
                                 length:(int)count;

// String(int, int, char[])
+ (NSString *)java_stringWithOffset:(int)offset
                             length:(int)length
                         characters:(IOSCharArray *)value;

// String(int[], int, int)
+ (NSString *)java_stringWithInts:(IOSIntArray *)codePoints
                           offset:(int)offset
                           length:(int)count;

// String(StringBuffer)
+ (NSString *)java_stringWithJavaLangStringBuffer:(JavaLangStringBuffer *)sb;

// String(StringBuilder)
+ (NSString *)java_stringWithJavaLangStringBuilder:(JavaLangStringBuilder *)sb;

// String.substring(int)
- (NSString *)java_substring:(int)beginIndex;

// String.substring(int, int)
- (NSString *)java_substring:(int)beginIndex
                    endIndex:(int)endIndex;

// String.indexOf(int)
- (int)java_indexOf:(int)ch;

// String.indexOf(int, int)
- (int)java_indexOf:(int)ch fromIndex:(int)index;

// String.indexOf(String)
- (int)java_indexOfString:(NSString *)s;

// String.indexOf(String, int)
- (int)java_indexOfString:(NSString *)s fromIndex:(int)index;

// String.isEmpty()
- (jboolean)java_isEmpty;

// String.lastIndexOf(int)
- (int)java_lastIndexOf:(int)ch;

// String.lastIndexOf(int, int)
- (int)java_lastIndexOf:(int)ch fromIndex:(int)index;

// String.lastIndexOf(String)
- (int)java_lastIndexOfString:(NSString *)s;

// String.lastIndexOf(String, int)
- (int)java_lastIndexOfString:(NSString *)s fromIndex:(int)index;

// String.toCharArray()
- (IOSCharArray *)java_toCharArray;

// java.lang.Comparable implementation methods
- (jint)compareToWithId:(id)another;

// CharSequence.charAt(int)
- (jchar)charAtWithInt:(jint)index;

// CharSequence.subSequence(int, int)
- (id<JavaLangCharSequence>)subSequenceFrom:(int)start
                                         to:(int)end;

// String.compareToIgnoreCase(String)
- (jint)java_compareToIgnoreCase:(NSString *)another;

// String.replace(char, char)
- (NSString *)java_replace:(jchar)oldchar withChar:(jchar)newchar;

// String.replace(CharSequence, CharSequence)
- (NSString *)java_replace:(id<JavaLangCharSequence>)oldSequence
              withSequence:(id<JavaLangCharSequence>)newSequence;

// String.replaceAll(String, String)
- (NSString *)java_replaceAll:(NSString *)regex
              withReplacement:(NSString *)replacement;

// String.replaceFirst(String, String)
- (NSString *)java_replaceFirst:(NSString *)regex
                withReplacement:(NSString *)replacement;

// String.getBytes()
- (IOSByteArray *)java_getBytes;

// String.getBytes(String)
- (IOSByteArray *)java_getBytesWithCharsetName:(NSString *)charsetName;

// String.getBytes(Charset)
- (IOSByteArray *)java_getBytesWithCharset:(JavaNioCharsetCharset *)charset;

- (IOSByteArray *)java_getBytesWithEncoding:(NSStringEncoding)encoding;

// String.getBytes(int, int, byte[], int)
- (void)java_getBytesWithSrcBegin:(int)srcBegin
                       withSrcEnd:(int)srcEnd
                          withDst:(IOSByteArray *)dst
                     withDstBegin:(int)dstBegin;

// String.format(String, ...), String.format(Locale, String, ...)
+ (NSString *)java_formatWithNSString:(NSString *)format withNSObjectArray:(IOSObjectArray *)args;
+ (NSString *)java_formatWithJavaUtilLocale:(JavaUtilLocale *)locale
                               withNSString:(NSString *)format
                          withNSObjectArray:(IOSObjectArray *)args;

// String.startsWith(String, int)
- (jboolean)java_hasPrefix:(NSString *)aString offset:(int)offset;

// String.trim()
- (NSString *)java_trim;

// String.split(String)
- (IOSObjectArray *)java_split:(NSString *)str;

// String equalsIgnoreCase(String)
- (jboolean)java_equalsIgnoreCase:(NSString *)aString;

// String.toLowerCase(Locale), toUpperCase(Locale)
- (NSString *)java_lowercaseStringWithJRELocale:(JavaUtilLocale *)locale;
- (NSString *)java_uppercaseStringWithJRELocale:(JavaUtilLocale *)locale;

// String.regionMatches(...)
- (jboolean)java_regionMatches:(int)thisOffset
                       aString:(NSString *)aString
                   otherOffset:(int)otherOffset
                         count:(int)count;

- (jboolean)java_regionMatches:(jboolean)caseInsensitive
                    thisOffset:(int)thisOffset
                       aString:(NSString *)aString
                   otherOffset:(int)otherOffset
                         count:(int)count;

// String.intern()
- (NSString *)java_intern;

// String.concat(String)
- (NSString *)java_concat:string;

// String.contains(CharSequence)
- (jboolean)java_contains:(id<JavaLangCharSequence>)sequence;

// String.codePointAt(int), codePointBefore(int), codePointCount(int, int)
- (int)java_codePointAt:(int)index;
- (int)java_codePointBefore:(int)index;
- (int)java_codePointCount:(int)beginIndex endIndex:(int)endIndex;

// String.matches(), split(String, int)
- (jboolean)java_matches:(NSString *)regex;
- (IOSObjectArray *)java_split:(NSString *)regex limit:(int)limit;

// String.contentEquals(CharSequence), contentEquals(StringBuffer)
- (jboolean)java_contentEqualsCharSequence:(id<JavaLangCharSequence>)seq;
- (jboolean)java_contentEqualsStringBuffer:(JavaLangStringBuffer *)sb;

// String.offsetByCodePoints(int, int)
- (int)java_offsetByCodePoints:(int)index codePointOffset:(int)offset;

// String.join(CharSequence, CharSequence...)
+ (NSString *)java_joinWithJavaLangCharSequence:(id<JavaLangCharSequence>)delimiter
                  withJavaLangCharSequenceArray:(IOSObjectArray *)elements;

// String.join(CharSequence, Iterable<? extends CharSequence>)
+ (NSString *)java_joinWithJavaLangCharSequence:(id<JavaLangCharSequence>)delimiter
                           withJavaLangIterable:(id<JavaLangIterable>)elements;

@end

// String.format(Locale, String, Object...)
FOUNDATION_EXPORT NSString *NSString_java_formatWithJavaUtilLocale_withNSString_withNSObjectArray_(
    JavaUtilLocale *l, NSString *s, IOSObjectArray *objs);
// String.format(String, Object...)
FOUNDATION_EXPORT NSString *NSString_java_formatWithNSString_withNSObjectArray_(
    NSString *s, IOSObjectArray *objs);
// String.valueOf(boolean)
FOUNDATION_EXPORT NSString *NSString_java_valueOfBool_(jboolean b);
// String.valueOf(char)
FOUNDATION_EXPORT NSString *NSString_java_valueOfChar_(jchar c);
// String.valueOf(char[])
// String.copyValueOf(char[])
FOUNDATION_EXPORT NSString *NSString_java_valueOfChars_(IOSCharArray *chars);
// String.valueOf(char[], int, int)
// String.copyValueOf(char[], int, int)
FOUNDATION_EXPORT NSString *NSString_java_valueOfChars_offset_count_(
    IOSCharArray *chars, jint i, jint j);
// String.valueOf(double)
FOUNDATION_EXPORT NSString *NSString_java_valueOfDouble_(jdouble d);
// String.valueOf(float)
FOUNDATION_EXPORT NSString *NSString_java_valueOfFloat_(jfloat f);
// String.valueOf(int)
FOUNDATION_EXPORT NSString *NSString_java_valueOfInt_(jint i);
// String.valueOf(long)
FOUNDATION_EXPORT NSString *NSString_java_valueOfLong_(jlong l);
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
FOUNDATION_EXPORT _Atomic(jboolean) NSString__initialized;
__attribute__((always_inline)) inline void NSString_initialize() {
  if (!__builtin_expect(NSString__initialized, true)) {
    [JreStringCategoryDummy class];
  }
}

inline id<JavaUtilComparator> NSString_get_CASE_INSENSITIVE_ORDER();
/*! INTERNAL ONLY - Use accessor function from above. */
FOUNDATION_EXPORT id<JavaUtilComparator> NSString_CASE_INSENSITIVE_ORDER;
J2OBJC_STATIC_FIELD_OBJ_FINAL(NSString, CASE_INSENSITIVE_ORDER, id<JavaUtilComparator>)

inline IOSObjectArray *NSString_get_serialPersistentFields();
/*! INTERNAL ONLY - Use accessor function from above. */
FOUNDATION_EXPORT IOSObjectArray *NSString_serialPersistentFields;
J2OBJC_STATIC_FIELD_OBJ_FINAL(NSString, serialPersistentFields, IOSObjectArray *)

J2OBJC_TYPE_LITERAL_HEADER(NSString)

/** Function that returns String hash values as specified by java.lang.String. */
FOUNDATION_EXPORT jint javaStringHashCode(NSString *string);

#endif // _NSString_JavaString_H_
