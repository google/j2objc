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

#import "JreEmulation.h"

@class IOSCharArray;

@protocol JavaLangCharSequence;

@interface JavaLangAbstractStringBuilder : NSObject {
 @package
  jchar *buffer_;
  jint bufferSize_;
  jint count_;
}

- (IOSCharArray *)getValue;

- (void)setWithCharArray:(IOSCharArray *)val
                 withInt:(jint)len;

- (instancetype)init;

- (instancetype)initWithInt:(jint)capacity;

- (instancetype)initWithNSString:(NSString *)string;

- (jint)capacity;

- (jchar)charAtWithInt:(jint)index;

- (void)ensureCapacityWithInt:(jint)min;

- (void)getCharsWithInt:(jint)start
                withInt:(jint)end
          withCharArray:(IOSCharArray *)dst
                withInt:(jint)dstStart;

- (jint)length;

- (void)setCharAtWithInt:(jint)index
                withChar:(jchar)ch;

- (void)setLengthWithInt:(jint)length;

- (NSString *)substringWithInt:(jint)start;

- (NSString *)substringWithInt:(jint)start
                       withInt:(jint)end;

- (NSString *)description;

- (id<JavaLangCharSequence>)subSequenceWithInt:(jint)start
                                       withInt:(jint)end;

- (jint)indexOfWithNSString:(NSString *)string;

- (jint)indexOfWithNSString:(NSString *)subString
                    withInt:(jint)start;

- (jint)lastIndexOfWithNSString:(NSString *)string;

- (jint)lastIndexOfWithNSString:(NSString *)subString
                        withInt:(jint)start;

- (void)trimToSize;

- (jint)codePointAtWithInt:(jint)index;

- (jint)codePointBeforeWithInt:(jint)index;

- (jint)codePointCountWithInt:(jint)start
                      withInt:(jint)end;

- (jint)offsetByCodePointsWithInt:(jint)index
                          withInt:(jint)codePointOffset;

@end

CF_EXTERN_C_BEGIN

void AbstractStringBuilder_appendNull(JavaLangAbstractStringBuilder *self);
void AbstractStringBuilder_appendBuffer(
    JavaLangAbstractStringBuilder *self, const unichar *buffer, int length);
void AbstractStringBuilder_appendCharArray(
    JavaLangAbstractStringBuilder *self, IOSCharArray *chars);
void AbstractStringBuilder_appendCharArraySubset(
    JavaLangAbstractStringBuilder *self, IOSCharArray *chars, jint offset, jint length);
void AbstractStringBuilder_appendChar(JavaLangAbstractStringBuilder *self, jchar ch);
void AbstractStringBuilder_appendString(JavaLangAbstractStringBuilder *self, NSString *string);
void AbstractStringBuilder_appendCharSequence(
    JavaLangAbstractStringBuilder *self, id<JavaLangCharSequence> s, jint start, jint end);
void AbstractStringBuilder_appendRaw(
    JavaLangAbstractStringBuilder *self, const jchar *buf, jint length);

void AbstractStringBuilder_delete(JavaLangAbstractStringBuilder *self, jint start, jint end);
void AbstractStringBuilder_deleteCharAt(JavaLangAbstractStringBuilder *self, jint index);

void AbstractStringBuilder_insertCharArray(
    JavaLangAbstractStringBuilder *self, jint index, IOSCharArray *chars);
void AbstractStringBuilder_insertCharArraySubset(
    JavaLangAbstractStringBuilder *self, jint index, IOSCharArray *chars, jint start, jint length);
void AbstractStringBuilder_insertChar(JavaLangAbstractStringBuilder *self, jint index, jchar ch);
void AbstractStringBuilder_insertString(
    JavaLangAbstractStringBuilder *self, jint index, NSString *string);
void AbstractStringBuilder_insertCharSequence(
    JavaLangAbstractStringBuilder *self, jint index, id<JavaLangCharSequence> s, jint start,
    jint end);

void AbstractStringBuilder_replace(
    JavaLangAbstractStringBuilder *self, jint start, jint end, NSString *string);

void AbstractStringBuilder_reverse(JavaLangAbstractStringBuilder *self);

NSString *AbstractStringBuilder_toString(JavaLangAbstractStringBuilder *self);

CF_EXTERN_C_END

#endif // _JavaLangAbstractStringBuilder_H_
