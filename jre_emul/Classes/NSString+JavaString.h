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

#import <Foundation/Foundation.h>
#import "IOSByteArray.h"
#import "IOSCharArray.h"
#import "IOSIntArray.h"
#import "IOSObjectArray.h"
#import "java/lang/CharSequence.h"
#import "java/lang/Comparable.h"

@class JavaLangStringBuffer;
@class JavaLangStringBuilder;
@class JavaNioCharsetCharset;
@class JavaUtilLocale;
@protocol JavaUtilComparator;

// A category that adds java.lang.String-like methods to NSString.  The method
// list is not exhaustive, since methods that can be directly substituted are
// inlined.  For example, "foo".length() is directly translated to
// [@"foo" length].
@interface NSString (JavaString) <JavaLangComparable, JavaLangCharSequence>

// String.valueOf(Object)
+ (NSString *)valueOf:(id<NSObject>)obj;

// String.valueOf(boolean)
+ (NSString *)valueOfBool:(BOOL)value;

// String.valueOf(char)
+ (NSString *)valueOfChar:(unichar)value;

// String.valueOf(char[])
+ (NSString *)valueOfChars:(IOSCharArray *)data;

// String.valueOf(char[], offset, count)
+ (NSString *)valueOfChars:(IOSCharArray *)data
                    offset:(int)offset
                     count:(int)count;

// String.valueOf(double)
+ (NSString *)valueOfDouble:(double)value;

// String.valueOf(float)
+ (NSString *)valueOfFloat:(float)value;

// String.valueOf(int)
+ (NSString *)valueOfInt:(int)value;

// String.valueOf(long)
+ (NSString *)valueOfLong:(long long int)value;

// String.valueOf(short)
+ (NSString *)valueOfShort:(short)value;

// String.getChars(int, int, char[], int)
- (void)getChars:(int)sourceBegin
       sourceEnd:(int)sourceEnd
     destination:(IOSCharArray *)dest
destinationBegin:(int)dstBegin;

// String(byte[])
+ (NSString *)stringWithBytes:(IOSByteArray *)value;

// String(byte[], int)
+ (NSString *)stringWithBytes:(IOSByteArray *)value
                       hibyte:(int)hibyte;

// String(byte[], int, int)
+ (NSString *)stringWithBytes:(IOSByteArray *)value
                       offset:(int)offset
                       length:(int)count;

+ (NSString *)stringWithBytes:(IOSByteArray *)value
                       hibyte:(NSUInteger)hibyte
                       offset:(NSUInteger)offset
                       length:(NSUInteger)length;

// String(byte[], String)
+ (NSString *)stringWithBytes:(IOSByteArray *)value
                  charsetName:(NSString *)charsetName;

// String(byte[], Charset)
+ (NSString *)stringWithBytes:(IOSByteArray *)value
                  charset:(JavaNioCharsetCharset *)charset;

// String(byte[], int, int, String)
+ (NSString *)stringWithBytes:(IOSByteArray *)value
                       offset:(int)offset
                       length:(int)count
              charsetName:(NSString *)charsetName;

// String(byte[], int, int, Charset)
+ (NSString *)stringWithBytes:(IOSByteArray *)value
                       offset:(int)offset
                       length:(int)count
                  charset:(JavaNioCharsetCharset *)charset;

+ (NSString *)stringWithBytes:(IOSByteArray *)value
                       offset:(int)offset
                       length:(int)count
                 encoding:(NSStringEncoding)encoding;

// String(char[])
+ (NSString *)stringWithCharacters:(IOSCharArray *)value;

// String(char[], int, int)
+ (NSString *)stringWithCharacters:(IOSCharArray *)value
                            offset:(int)offset
                            length:(int)count;

// String(int, int, char[])
+ (NSString *)stringWithOffset:(int)offset
                        length:(int)length
                    characters:(IOSCharArray *)value;

// String(int[], int, int)
+ (NSString *)stringWithInts:(IOSIntArray *)codePoints
                      offset:(int)offset
                      length:(int)length;

// String(StringBuffer)
+ (NSString *)stringWithJavaLangStringBuffer:(JavaLangStringBuffer *)sb;

// String(StringBuilder)
+ (NSString *)stringWithJavaLangStringBuilder:(JavaLangStringBuilder *)sb;

// String.substring(int)
- (NSString *)substring:(int)beginIndex;

// String.substring(int, int)
- (NSString *)substring:(int)beginIndex
               endIndex:(int)endIndex;

// String.indexOf(int)
- (int)indexOf:(int)ch;

// String.indexOf(int, int)
- (int)indexOf:(int)ch fromIndex:(int)index;

// String.indexOf(String)
- (int)indexOfString:(NSString *)s;

// String.indexOf(String, int)
- (int)indexOfString:(NSString *)s fromIndex:(int)index;

// String.isEmpty()
- (BOOL)isEmpty;

// String.lastIndexOf(int)
- (int)lastIndexOf:(int)ch;

// String.lastIndexOf(int, int)
- (int)lastIndexOf:(int)ch fromIndex:(int)index;

// String.lastIndexOf(String)
- (int)lastIndexOfString:(NSString *)s;

// String.lastIndexOf(String, int)
- (int)lastIndexOfString:(NSString *)s fromIndex:(int)index;

// String.toCharArray()
- (IOSCharArray *)toCharArray;

// java.lang.Comparable implementation methods
- (int)compareToWithId:(id)another;

// CharSequence.charAt(int)
- (unichar)charAtWithInt:(int)index;

// CharSequence.toString()
- (NSString *)sequenceDescription;

// CharSequence.length()
- (int)sequenceLength;

// CharSequence.subSequence(int, int)
- (id<JavaLangCharSequence>)subSequenceFrom:(int)start
                                         to:(int)end;

// String.compareToIgnoreCase(String)
- (int)compareToIgnoreCase:(NSString *)another;

// String.replace(char, char)
- (NSString *)replace:(unichar)oldchar withChar:(unichar)newchar;

// String.replace(CharSequence, CharSequence)
- (NSString *)replace:(id<JavaLangCharSequence>)oldSequence
         withSequence:(id<JavaLangCharSequence>)newSequence;

// String.replaceAll(String, String)
- (NSString *)replaceAll:(NSString *)regex
         withReplacement:(NSString *)replacement;

// String.replaceFirst(String, String)
- (NSString *)replaceFirst:(NSString *)regex
           withReplacement:(NSString *)replacement;

// String.getBytes()
- (IOSByteArray *)getBytes;

// String.getBytes(String)
- (IOSByteArray *)getBytesWithCharsetName:(NSString *)charsetName;

// String.getBytes(Charset)
- (IOSByteArray *)getBytesWithCharset:(JavaNioCharsetCharset *)charset;

- (IOSByteArray *)getBytesWithEncoding:(NSStringEncoding)encoding;

// String.getBytes(int, int, byte[], int)
- (void)getBytesWithSrcBegin:(int)srcBegin
                  withSrcEnd:(int)srcEnd
                     withDst:(IOSByteArray *)dst
                withDstBegin:(int)dstBegin;

// String.format(Locale, String, ...)
+ (NSString *)stringWithFormat:(NSString *)format locale:(id)locale, ...;

// String.startsWith(String, int)
- (BOOL)hasPrefix:(NSString *)aString offset:(int)offset;

// String.trim()
- (NSString *)trim;

// String.split(String)
- (IOSObjectArray *)split:(NSString *)str;

// String equalsIgnoreCase(String)
- (BOOL)equalsIgnoreCase:(NSString *)aString;

// String.toLowerCase(Locale), toUpperCase(Locale)
- (NSString *)lowercaseStringWithJRELocale:(JavaUtilLocale *)locale;
- (NSString *)uppercaseStringWithJRELocale:(JavaUtilLocale *)locale;

// String.regionMatches(...)
- (BOOL)regionMatches:(int)thisOffset
              aString:(NSString *)aString
          otherOffset:(int)otherOffset
                count:(int)count;

- (BOOL)regionMatches:(BOOL)caseInsensitive
           thisOffset:(int)thisOffset
              aString:(NSString *)aString
          otherOffset:(int)otherOffset
                count:(int)count;

// String.intern()
- (NSString *)intern;

// String.concat(String)
- (NSString *)concat:string;

// String.contains(CharSequence)
- (BOOL)contains:(id<JavaLangCharSequence>)sequence;

// String.codePointAt(int), codePointBefore(int), codePointCount(int, int)
- (int)codePointAt:(int)index;
- (int)codePointBefore:(int)index;
- (int)codePointCount:(int)beginIndex endIndex:(int)endIndex;

// String.matches(), split(String, int)
- (BOOL)matches:(NSString *)regex;
- (IOSObjectArray *)split:(NSString *)regex limit:(int)limit;

// String.contentEquals(CharSequence), contentEquals(StringBuffer)
- (BOOL)contentEqualsCharSequence:(id<JavaLangCharSequence>)seq;
- (BOOL)contentEqualsStringBuffer:(JavaLangStringBuffer *)sb;

// String.offsetByCodePoints(int, int)

+ (id<JavaUtilComparator>)CASE_INSENSITIVE_ORDER;

@end

#endif // _NSString_JavaString_H_
