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

#ifndef _JavaLangAbstractStringBuilder_H_
#define _JavaLangAbstractStringBuilder_H_

#import "J2ObjC_header.h"
#import "java/lang/Appendable.h"
#import "java/lang/CharSequence.h"

@class IOSCharArray;
@class JavaLangStringBuffer;

// Defines a string builder struct so that J2ObjC string concatenation does not
// need to allocate a new ObjC string builder object.
typedef struct JreStringBuilder {
  uint16_t *buffer_;
  int32_t bufferSize_;
  int32_t count_;
} JreStringBuilder;

@interface JavaLangAbstractStringBuilder : NSObject < JavaLangAppendable, JavaLangCharSequence > {
 @package
  JreStringBuilder delegate_;
}

- (IOSCharArray *)getValue;

- (instancetype)initPackagePrivate;

- (instancetype)initPackagePrivateWithInt:(int32_t)capacity;

- (instancetype)initWithNSString:(NSString *)string;

- (int32_t)capacity;

- (uint16_t)charAtWithInt:(int32_t)index;

- (void)ensureCapacityWithInt:(int32_t)min;

- (void)getCharsWithInt:(int32_t)start
                withInt:(int32_t)end
          withCharArray:(IOSCharArray *)dst
                withInt:(int32_t)dstStart;

- (int32_t)java_length;

- (void)setCharAtWithInt:(int32_t)index
                withChar:(uint16_t)ch;

- (void)setLengthWithInt:(int32_t)length;

- (NSString *)substringWithInt:(int32_t)start;

- (NSString *)substringWithInt:(int32_t)start
                       withInt:(int32_t)end;

- (NSString *)description;

- (id<JavaLangCharSequence>)subSequenceFrom:(int32_t)start
                                         to:(int32_t)end;

- (int32_t)indexOfWithNSString:(NSString *)string;

- (int32_t)indexOfWithNSString:(NSString *)subString
                    withInt:(int32_t)start;

- (int32_t)lastIndexOfWithNSString:(NSString *)string;

- (int32_t)lastIndexOfWithNSString:(NSString *)subString
                        withInt:(int32_t)start;

- (void)trimToSize;

- (int32_t)codePointAtWithInt:(int32_t)index;

- (int32_t)codePointBeforeWithInt:(int32_t)index;

- (int32_t)codePointCountWithInt:(int32_t)start
                      withInt:(int32_t)end;

- (int32_t)offsetByCodePointsWithInt:(int32_t)index
                          withInt:(int32_t)codePointOffset;

- (int32_t)compareToWithJavaLangAbstractStringBuilder:(JavaLangAbstractStringBuilder *)other;

@end

CF_EXTERN_C_BEGIN

void JavaLangAbstractStringBuilder_initPackagePrivate(JavaLangAbstractStringBuilder *self);
void JavaLangAbstractStringBuilder_initPackagePrivateWithInt_(
    JavaLangAbstractStringBuilder *self, int32_t capacity);
void JavaLangAbstractStringBuilder_initWithNSString_(
    JavaLangAbstractStringBuilder *self, NSString *string);

void JreStringBuilder_initWithCapacity(JreStringBuilder *sb, int32_t capacity);

void JreStringBuilder_appendNull(JreStringBuilder *sb);
void JreStringBuilder_appendBuffer(JreStringBuilder *sb, const unichar *buffer, int length);
void JreStringBuilder_appendStringBuffer(JreStringBuilder *sb, JavaLangStringBuffer *toAppend);
void JreStringBuilder_appendCharArray(JreStringBuilder *sb, IOSCharArray *chars);
void JreStringBuilder_appendCharArraySubset(
    JreStringBuilder *sb, IOSCharArray *chars, int32_t offset, int32_t length);
void JreStringBuilder_appendChar(JreStringBuilder *sb, uint16_t ch);
void JreStringBuilder_appendString(JreStringBuilder *sb, NSString *string);
void JreStringBuilder_appendCharSequence(JreStringBuilder *sb, id<JavaLangCharSequence> s);
void JreStringBuilder_appendCharSequenceSubset(
    JreStringBuilder *sb, id<JavaLangCharSequence> s, int32_t start, int32_t end);
void JreStringBuilder_appendInt(JreStringBuilder *sb, int32_t i);
void JreStringBuilder_appendLong(JreStringBuilder *sb, int64_t l);
void JreStringBuilder_appendDouble(JreStringBuilder *sb, double d);
void JreStringBuilder_appendFloat(JreStringBuilder *sb, float f);

void JreStringBuilder_delete(JreStringBuilder *sb, int32_t start, int32_t end);
void JreStringBuilder_deleteCharAt(JreStringBuilder *sb, int32_t index);

void JreStringBuilder_insertCharArray(JreStringBuilder *sb, int32_t index, IOSCharArray *chars);
void JreStringBuilder_insertCharArraySubset(
    JreStringBuilder *sb, int32_t index, IOSCharArray *chars, int32_t start, int32_t length);
void JreStringBuilder_insertChar(JreStringBuilder *sb, int32_t index, uint16_t ch);
void JreStringBuilder_insertString(JreStringBuilder *sb, int32_t index, NSString *string);
void JreStringBuilder_insertCharSequence(
    JreStringBuilder *sb, int32_t index, id<JavaLangCharSequence> s, int32_t start, int32_t end);

void JreStringBuilder_replace(JreStringBuilder *sb, int32_t start, int32_t end, NSString *string);

void JreStringBuilder_reverse(JreStringBuilder *sb);

NSString *JreStringBuilder_toString(JreStringBuilder *sb);
NSString *JreStringBuilder_toStringAndDealloc(JreStringBuilder *sb);

CF_EXTERN_C_END

#endif // _JavaLangAbstractStringBuilder_H_
