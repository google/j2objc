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
#import "java/io/UnsupportedEncodingException.h"
#import "java/lang/AssertionError.h"
#import "java/lang/Character.h"
#import "java/lang/ClassCastException.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/StringBuffer.h"
#import "java/lang/StringBuilder.h"
#import "java/lang/StringIndexOutOfBoundsException.h"
#import "java/util/Locale.h"
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
  return [self compare:(NSString *) another];
}

- (int)compareToIgnoreCase:(NSString *)another {
  return [self caseInsensitiveCompare:another];
}

- (NSString *)substring:(int)beginIndex {
  return [self substringFromIndex:(NSUInteger) beginIndex];
}

- (NSString *)substring:(int)beginIndex
               endIndex:(int)endIndex {
  NSRange range = NSMakeRange(beginIndex, endIndex - beginIndex);
  return [self substringWithRange:range];
}

- (int)indexOf:(int)ch {
  return [self indexOf:ch fromIndex:0];
}

- (int)indexOf:(int)ch fromIndex:(int)index {
  unichar c = (unichar) ch;
  NSString *s = [NSString stringWithCharacters:&c length:1];
  return [self lastIndexOfString:s fromIndex:(int)index];
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
  NSRange searchRange = NSMakeRange((NSUInteger) index,
                                    [self length] - (NSUInteger) index);
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
  if ([s length] == 0) {
    return [self length];
  }
  NSRange searchRange = NSMakeRange((NSUInteger) index, (NSUInteger) index);
  NSRange range = [self rangeOfString:s
                              options:NSBackwardsSearch
                                range:searchRange];
  return range.location == NSNotFound ? -1 : (int) range.location;
}

- (IOSCharArray *)toCharArray {
  NSUInteger len = [self length];
  unichar *buffer = calloc(len, sizeof(unichar));
  NSRange range = NSMakeRange(0, len);
  [self getCharacters:buffer range:range];
  IOSCharArray *array = [IOSCharArray arrayWithCharacters:buffer count:len];
  free(buffer);
  return array;
}

- (unichar)charAtWithInt:(int)index {
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
    @throw makeException([JavaLangIndexOutOfBoundsException class]);
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
                  charsetName:(NSString *)charset {
  return [self stringWithBytes:value
                        offset:0
                        length:[value count]
                   charsetName:charset];
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

- (IOSByteArray *)getBytes  {
  // UTF-8 is the Java default charset, unless the file.encoding system
  // property is set.
  return [self getBytesWithEncoding:NSUTF8StringEncoding];
}

- (IOSByteArray *)getBytesWithCharset:(NSString *)charsetName {
  if (charsetName == nil) {
    id exception = [[JavaLangNullPointerException alloc] init];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  NSStringEncoding encoding = parseCharsetName(charsetName);
  return [self getBytesWithEncoding:encoding];
}

- (IOSByteArray *)getBytesWithEncoding:(NSStringEncoding)encoding  {
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

+ (NSString *)stringWithFormat:(NSString *)format locale:(id)locale, ... {
  va_list args;
  va_start(args, locale);
  NSString *result = [[NSString alloc] initWithFormat:format
                                               locale:locale
                                            arguments:args];
  va_end(args);
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (BOOL)hasPrefix:(NSString *)aString offset:(int)offset {
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
  NSArray *components = nil;

  NSCharacterSet *regexChars =
      [NSCharacterSet characterSetWithCharactersInString:@"?*+."];
  NSRange range = [str rangeOfCharacterFromSet:regexChars];

#if TARGET_OS_IPHONE || MAC_OS_X_VERSION_MIN_REQUIRED >= 1070
  NSError *error = nil;

  // NSRegularExpression is only available on iOS > 5.0 and OSX > 10.7
  NSRegularExpression *regex =
      [NSRegularExpression regularExpressionWithPattern:str
                                                options:0
                                                  error:&error];

  if (range.location != NSNotFound && error == nil) {
    // Regex parsed correctly.
    NSMutableArray *mutableComponents = [NSMutableArray array];
    components = mutableComponents;
    __block NSInteger lastMatchEnd = 0;
    [regex enumerateMatchesInString:self
                            options:0
                              range:NSMakeRange(0, [self length])
                         usingBlock:
        ^(NSTextCheckingResult *matchResult, NSMatchingFlags flags,
              BOOL *stop) {
            NSRange matchRange = matchResult.range;
            NSInteger start = lastMatchEnd;
            if (start == matchRange.location) {
              lastMatchEnd += matchRange.length;
              return;
            }
            NSInteger length = matchRange.location - start;
            NSRange nextComponent = NSMakeRange(start, length);
            lastMatchEnd = matchRange.location + matchRange.length;
            [mutableComponents
                addObject:[self substringWithRange:nextComponent]];

        }];
    [mutableComponents addObject:[self substringFromIndex:lastMatchEnd]];
  }

#else
  // Warn if there are regex chars.
  if (range.location != NSNotFound) {
    NSLog(@"Warning: possible regex characters in separator: %@", str);
  }

  if (NO) {  // Awkward ... but keeps the rest of the code pretty.
  }

#endif  // !TARGET_OS_MAC || MAC_OS_X_VERSION_MIN_REQUIRED >= 1070
  else {
    // Either had no regex characters (simple case), or there was an error
    // parsing the regex, or NSRegularExpression not available in SDK being
    // targeted.
    components = [self componentsSeparatedByString:str];
  }

  // String.split spec says that trailing empty strings are not to be included
  // in the result.
  int count = [components count];
  int trailingEmptyStringCount = 0;
  for (int i = count - 1; i >= 0; i--) {
    NSString *component = [components objectAtIndex:i];
    if ([component length] > 0) {
      break;
    }

    trailingEmptyStringCount++;
  }

  // componentsSeparatedByString returns empty strings if there are matches
  // at the beginning.
  int beginningOccurrencesCount = 0;
  for (int i = 0; i < count; i++) {
    NSString *component = [components objectAtIndex:i];
    if (![component isEqualToString:@""]) {
      break;
    }
    beginningOccurrencesCount++;
  }

  int resultCount = count - trailingEmptyStringCount -
      beginningOccurrencesCount;

  IOSClass *stringClass = [IOSClass classWithClass:[NSString class]];
  if (resultCount < 1) {
    return [[IOSObjectArray alloc] initWithLength:0 type:stringClass];
  }

  int startIndex = beginningOccurrencesCount;
  int endIndex = beginningOccurrencesCount + resultCount;
  IOSObjectArray *result =
      [[IOSObjectArray alloc] initWithLength:resultCount
                                        type:stringClass];
  for (int i = startIndex; i < endIndex; i++) {
    [result replaceObjectAtIndex:i - startIndex
                      withObject:[components objectAtIndex:i]];
  }

#if !__has_feature(objc_arc)
  [result autorelease];
#endif

  return result;
}

- (BOOL)equalsIgnoreCase:(NSString *)aString {
  NSComparisonResult result = [self caseInsensitiveCompare:aString];
  return result == NSOrderedSame;
}

- (NSString *)lowercaseStringWithJRELocale:(JavaUtilLocale *)javaLocale {
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
  return [self regionMatches:YES
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
  NSString *other = otherOffset == 0 ? aString :
      [aString substringFromIndex:otherOffset];
  NSUInteger options = NSLiteralSearch;
  if (caseInsensitive) {
    options |= NSCaseInsensitiveSearch;
  }
  return [self compare:other
               options:options
                 range:NSMakeRange(thisOffset, count)];
}

- (NSString *)intern {
  // No actual interning is done, since NSString doesn't support it.
  // Instead, any "string == otherString" expression is changed to
  // "string.equals(otherString)
  return self;
}

- (NSString *)concat:string {
  return [self stringByAppendingString:string];
}

- (BOOL)contains:(id<JavaLangCharSequence>)sequence {
  if ([sequence sequenceLength] == 0) {
    return 0;
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
  NSError *error;
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

- (IOSObjectArray *)split:(NSString *)regex limit:(int)limit {
  IOSObjectArray *parts = [self split:regex];
  if (limit == 0 || [parts count] <= limit) {
    return parts;
  }
  IOSObjectArray *result = [IOSObjectArray arrayWithType:[parts elementType]
                                                   count:limit];
  for (int i = 0; i < limit; i++) {
    id part = [parts objectAtIndex:i];
    [result replaceObjectAtIndex:i withObject:part];
  }
  return result;
}

@end
