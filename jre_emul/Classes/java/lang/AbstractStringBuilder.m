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

#include "java/lang/AbstractStringBuilder.h"

#include "java/io/InvalidObjectException.h"
#include "java/lang/ArrayIndexOutOfBoundsException.h"
#include "java/lang/Character.h"
#include "java/lang/Integer.h"
#include "java/lang/NegativeArraySizeException.h"
#include "java/lang/NullPointerException.h"
#include "java/lang/StringIndexOutOfBoundsException.h"
#include "java/util/Arrays.h"
#include "libcore/util/EmptyArray.h"

#define INITIAL_CAPACITY 16

static void AbstractStringBuilder_move(JavaLangAbstractStringBuilder *self, jint size, jint index);

static inline void NewBuffer(JavaLangAbstractStringBuilder *self, jint size) {
  self->buffer_ = malloc(size * sizeof(jchar));
  self->bufferSize_ = size;
}

static JavaLangStringIndexOutOfBoundsException *IndexAndLength(
    JavaLangAbstractStringBuilder *self, jint index) {
  @throw [[[JavaLangStringIndexOutOfBoundsException alloc]
      initWithInt:self->count_ withInt:index] autorelease];
}

static JavaLangStringIndexOutOfBoundsException *StartEndAndLength(
    JavaLangAbstractStringBuilder *self, jint start, jint end) {
  @throw [[[JavaLangStringIndexOutOfBoundsException alloc]
      initWithInt:self->count_ withInt:start withInt:end - start] autorelease];
}

@implementation JavaLangAbstractStringBuilder

- (IOSCharArray *)getValue {
  return [IOSCharArray arrayWithChars:buffer_ count:count_];
}

- (void)setWithCharArray:(IOSCharArray *)val
                 withInt:(jint)len {
  if (val == nil) {
    val = LibcoreUtilEmptyArray_get_CHAR_();
  }
  if (((IOSCharArray *) nil_chk(val))->size_ < len) {
    @throw [[[JavaIoInvalidObjectException alloc] initWithNSString:@"count out of range"] autorelease];
  }
  if (len > bufferSize_) {
    free(buffer_);
    NewBuffer(self, len);
  }
  memcpy(buffer_, val->buffer_, len * sizeof(jchar));
  count_ = len;
}

- (instancetype)init {
  if (self = [super init]) {
    NewBuffer(self, INITIAL_CAPACITY);
  }
  return self;
}

- (instancetype)initWithInt:(jint)capacity {
  if (self = [super init]) {
    if (capacity < 0) {
      @throw [[[JavaLangNegativeArraySizeException alloc] initWithNSString:
          [JavaLangInteger toStringWithInt:capacity]] autorelease];
    }
    NewBuffer(self, capacity);
  }
  return self;
}

- (instancetype)initWithNSString:(NSString *)string {
  if (self = [super init]) {
    count_ = ((jint) [((NSString *) nil_chk(string)) length]);
    NewBuffer(self, count_ + INITIAL_CAPACITY);
    [string getCharacters:buffer_ range:NSMakeRange(0, count_)];
  }
  return self;
}

static void EnlargeBuffer(JavaLangAbstractStringBuilder *self, jint min) {
  jint newCount = ((self->bufferSize_ >> 1) + self->bufferSize_) + 2;
  jchar *oldBuffer = self->buffer_;
  NewBuffer(self, min > newCount ? min : newCount);
  memcpy(self->buffer_, oldBuffer, self->count_ * sizeof(jchar));
  free(oldBuffer);
}

void AbstractStringBuilder_appendNull(JavaLangAbstractStringBuilder *self) {
  jint newCount = self->count_ + 4;
  if (newCount > self->bufferSize_) {
    EnlargeBuffer(self, newCount);
  }
  jchar *buf = self->buffer_ + self->count_;
  *(buf++) = 'n';
  *(buf++) = 'u';
  *(buf++) = 'l';
  *(buf++) = 'l';
  self->count_ += 4;
}

void AbstractStringBuilder_appendBuffer(
    JavaLangAbstractStringBuilder *self, const unichar *buffer, int length) {
  int newCount = self->count_ + length;
  if (newCount > self->bufferSize_) {
    EnlargeBuffer(self, newCount);
  }
  memcpy(self->buffer_ + self->count_, buffer, length * sizeof(jchar));
  self->count_ = newCount;
}

void AbstractStringBuilder_appendCharArray(
    JavaLangAbstractStringBuilder *self, IOSCharArray *chars) {
  nil_chk(chars);
  AbstractStringBuilder_appendBuffer(self, chars->buffer_, chars->size_);
}

void AbstractStringBuilder_appendCharArraySubset(
    JavaLangAbstractStringBuilder *self, IOSCharArray *chars, jint offset, jint length) {
  nil_chk(chars);
  JavaUtilArrays_checkOffsetAndCountWithInt_withInt_withInt_(chars->size_, offset, length);
  AbstractStringBuilder_appendBuffer(self, chars->buffer_ + offset, length);
}

void AbstractStringBuilder_appendChar(JavaLangAbstractStringBuilder *self, jchar ch) {
  if (self->count_ == self->bufferSize_) {
    EnlargeBuffer(self, self->count_ + 1);
  }
  self->buffer_[self->count_++] = ch;
}

void AbstractStringBuilder_appendString(JavaLangAbstractStringBuilder *self, NSString *string) {
  if (string == nil) {
    AbstractStringBuilder_appendNull(self);
    return;
  }
  jint length = (jint) [string length];
  jint newCount = self->count_ + length;
  if (newCount > self->bufferSize_) {
    EnlargeBuffer(self, newCount);
  }
  [string getCharacters:self->buffer_ + self->count_ range:NSMakeRange(0, length)];
  self->count_ = newCount;
}

void AbstractStringBuilder_appendCharSequence(
    JavaLangAbstractStringBuilder *self, id<JavaLangCharSequence> s, jint start, jint end) {
  if (s == nil) {
    s = @"null";
  }
  if ((start | end) < 0 || start > end || end > [s sequenceLength]) {
    @throw [[[JavaLangIndexOutOfBoundsException alloc] init] autorelease];
  }
  jint length = end - start;
  jint newCount = self->count_ + length;
  if (newCount > self->bufferSize_) {
    EnlargeBuffer(self, newCount);
  }
  if ([s isKindOfClass:[NSString class]]) {
    [(NSString *)s getCharacters:self->buffer_ + self->count_ range:NSMakeRange(start, end - start)];
  } else if ([s isKindOfClass:[JavaLangAbstractStringBuilder class]]) {
    JavaLangAbstractStringBuilder *other = (JavaLangAbstractStringBuilder *)s;
    memcpy(self->buffer_ + self->count_, other->buffer_ + start, length * sizeof(jchar));
  } else {
    jint j = self->count_;
    for (jint i = start; i < end; i++) {
      self->buffer_[j++] = [s charAtWithInt:i];
    }
  }
  self->count_ = newCount;
}

- (jint)capacity {
  return bufferSize_;
}

- (jchar)charAtWithInt:(jint)index {
  if (index < 0 || index >= count_) {
    @throw IndexAndLength(self, index);
  }
  return buffer_[index];
}

void AbstractStringBuilder_delete(JavaLangAbstractStringBuilder *self, jint start, jint end) {
  if (start >= 0) {
    if (end > self->count_) {
      end = self->count_;
    }
    if (end == start) {
      return;
    }
    if (end > start) {
      jint length = self->count_ - end;
      if (length >= 0) {
        memmove(self->buffer_ + start, self->buffer_ + end, length * sizeof(jchar));
      }
      self->count_ -= end - start;
      return;
    }
  }
  @throw StartEndAndLength(self, start, end);
}

void AbstractStringBuilder_deleteCharAt(JavaLangAbstractStringBuilder *self, jint index) {
  if (index < 0 || index >= self->count_) {
    @throw IndexAndLength(self, index);
  }
  jint length = self->count_ - index - 1;
  if (length > 0) {
    memmove(self->buffer_ + index, self->buffer_ + index + 1, length * sizeof(jchar));
  }
  self->count_--;
}

- (void)ensureCapacityWithInt:(jint)min {
  if (min > bufferSize_) {
    jint ourMin = bufferSize_ * 2 + 2;
    EnlargeBuffer(self, MAX(ourMin, min));
  }
}

- (void)getCharsWithInt:(jint)start
                withInt:(jint)end
          withCharArray:(IOSCharArray *)dst
                withInt:(jint)dstStart {
  if (start > count_ || end > count_ || start > end) {
    @throw StartEndAndLength(self, start, end);
  }
  jint length = end - start;
  IOSArray_checkRange(bufferSize_, start, length);
  IOSArray_checkRange(dst->size_, dstStart, length);
  memcpy(dst->buffer_ + dstStart, buffer_ + start, length * sizeof(jchar));
}

void AbstractStringBuilder_insertCharArray(
    JavaLangAbstractStringBuilder *self, jint index, IOSCharArray *chars) {
  if (index < 0 || index > self->count_) {
    @throw IndexAndLength(self, index);
  }
  nil_chk(chars);
  if (chars->size_ != 0) {
    AbstractStringBuilder_move(self, chars->size_, index);
    memcpy(self->buffer_ + index, chars->buffer_, chars->size_ * sizeof(jchar));
    self->count_ += chars->size_;
  }
}

void AbstractStringBuilder_insertCharArraySubset(
    JavaLangAbstractStringBuilder *self, jint index, IOSCharArray *chars, jint start, jint length) {
  nil_chk(chars);
  if (index >= 0 && index <= self->count_) {
    if (start >= 0 && length >= 0 && length <= chars->size_ - start) {
      if (length != 0) {
        AbstractStringBuilder_move(self, length, index);
        memcpy(self->buffer_ + index, chars->buffer_ + start, length * sizeof(jchar));
        self->count_ += length;
      }
      return;
    }
  }
  @throw [[[JavaLangStringIndexOutOfBoundsException alloc] initWithNSString:
      [NSString stringWithFormat:@"this.length=%d; index=%d; chars.length=%d; start=%d; length=%d",
          self->count_, index, chars->size_, start, length]] autorelease];
}

void AbstractStringBuilder_insertChar(JavaLangAbstractStringBuilder *self, jint index, jchar ch) {
  if (index < 0 || index > self->count_) {
    @throw [[[JavaLangArrayIndexOutOfBoundsException alloc]
        initWithInt:self->count_ withInt:index] autorelease];
  }
  AbstractStringBuilder_move(self, 1, index);
  self->buffer_[index] = ch;
  self->count_++;
}

void AbstractStringBuilder_insertString(
    JavaLangAbstractStringBuilder *self, jint index, NSString *string) {
  if (index >= 0 && index <= self->count_) {
    if (string == nil) {
      string = @"null";
    }
    jint min = (jint)[string length];
    if (min != 0) {
      AbstractStringBuilder_move(self, min, index);
      [string getCharacters:self->buffer_ + index range:NSMakeRange(0, min)];
      self->count_ += min;
    }
  } else {
    @throw IndexAndLength(self, index);
  }
}

void AbstractStringBuilder_insertCharSequence(
    JavaLangAbstractStringBuilder *self, jint index, id<JavaLangCharSequence> s, jint start,
    jint end) {
  if (s == nil) {
    s = @"null";
  }
  if ((index | start | end) < 0 || index > self->count_ || start > end
      || end > [s sequenceLength]) {
    @throw [[[JavaLangIndexOutOfBoundsException alloc] init] autorelease];
  }
  AbstractStringBuilder_insertString(self, index, [[s subSequenceFrom:start to:end] description]);
}

- (jint)length {
  return count_;
}

void AbstractStringBuilder_move(JavaLangAbstractStringBuilder *self, jint size, jint index) {
  jint newCount;
  if (self->bufferSize_ - self->count_ >= size) {
    memmove(self->buffer_ + index + size, self->buffer_ + index,
        (self->count_ - index) * sizeof(jchar));
    return;
  }
  newCount = MAX(self->count_ + size, self->bufferSize_ * 2 + 2);
  jchar *oldBuffer = self->buffer_;
  NewBuffer(self, newCount);
  memcpy(self->buffer_, oldBuffer, index * sizeof(jchar));
  memcpy(self->buffer_ + index + size, oldBuffer + index, (self->count_ - index) * sizeof(jchar));
  free(oldBuffer);
}

void AbstractStringBuilder_replace(
    JavaLangAbstractStringBuilder *self, jint start, jint end, NSString *string) {
  if (start >= 0) {
    if (end > self->count_) {
      end = self->count_;
    }
    if (end > start) {
      nil_chk(string);
      jint stringLength = (jint)[string length];
      jint diff = end - start - stringLength;
      if (diff > 0) {
        memmove(self->buffer_ + start + stringLength, self->buffer_ + end,
            (self->count_ - end) * sizeof(jchar));
      } else if (diff < 0) {
        AbstractStringBuilder_move(self, -diff, end);
      }
      [string getCharacters:self->buffer_ + start range:NSMakeRange(0, stringLength)];
      self->count_ -= diff;
      return;
    }
    if (start == end) {
      if (string == nil) {
        @throw [[[JavaLangNullPointerException alloc]
            initWithNSString:@"string == null"] autorelease];
      }
      AbstractStringBuilder_insertString(self, start, string);
      return;
    }
  }
  @throw StartEndAndLength(self, start, end);
}

void AbstractStringBuilder_reverse(JavaLangAbstractStringBuilder *self) {
  if (self->count_ < 2) {
    return;
  }
  jchar *buf = self->buffer_;
  jint end = self->count_ - 1;
  jchar frontHigh = buf[0];
  jchar endLow = buf[end];
  jboolean allowFrontSur = YES, allowEndSur = YES;
  for (jint i = 0, mid = self->count_ / 2; i < mid; i++, --end) {
    jchar frontLow = buf[i + 1];
    jchar endHigh = buf[end - 1];
    jboolean surAtFront = allowFrontSur && frontLow >= (jint)0xdc00 && frontLow <= (jint)0xdfff
        && frontHigh >= (jint)0xd800 && frontHigh <= (jint)0xdbff;
    if (surAtFront && (self->count_ < 3)) {
      return;
    }
    jboolean surAtEnd = allowEndSur && endHigh >= (jint)0xd800 && endHigh <= (jint)0xdbff
        && endLow >= (jint)0xdc00 && endLow <= (jint)0xdfff;
    allowFrontSur = allowEndSur = YES;
    if (surAtFront == surAtEnd) {
      if (surAtFront) {
        buf[end] = frontLow;
        buf[end - 1] = frontHigh;
        buf[i] = endHigh;
        buf[i + 1] = endLow;
        frontHigh = buf[i + 2];
        endLow = buf[end - 2];
        i++;
        end--;
      } else {
        buf[end] = frontHigh;
        buf[i] = endLow;
        frontHigh = frontLow;
        endLow = endHigh;
      }
    } else {
      if (surAtFront) {
        buf[end] = frontLow;
        buf[i] = endLow;
        endLow = endHigh;
        allowFrontSur = NO;
      } else {
        buf[end] = frontHigh;
        buf[i] = endHigh;
        frontHigh = frontLow;
        allowEndSur = NO;
      }
    }
  }
  if ((self->count_ & 1) == 1 && (!allowFrontSur || !allowEndSur)) {
    buf[end] = allowFrontSur ? endLow : frontHigh;
  }
}

- (void)setCharAtWithInt:(jint)index
                withChar:(jchar)ch {
  if (index < 0 || index >= count_) {
    @throw IndexAndLength(self, index);
  }
  buffer_[index] = ch;
}

- (void)setLengthWithInt:(jint)length {
  if (length < 0) {
    @throw [[[JavaLangStringIndexOutOfBoundsException alloc]
        initWithNSString:[NSString stringWithFormat:@"length < 0: %d", length]] autorelease];
  }
  if (length > bufferSize_) {
    EnlargeBuffer(self, length);
  }
  if (count_ < length) {
    memset(buffer_ + count_, 0, (length - count_) * sizeof(jchar));
  }
  count_ = length;
}

- (NSString *)substringWithInt:(jint)start {
  if (start >= 0 && start <= count_) {
    if (start == count_) {
      return @"";
    }
    return [NSString stringWithCharacters:buffer_ + start length:count_ - start];
  }
  @throw IndexAndLength(self, start);
}

- (NSString *)substringWithInt:(jint)start
                       withInt:(jint)end {
  if (start >= 0 && start <= end && end <= count_) {
    if (start == end) {
      return @"";
    }
    return [NSString stringWithCharacters:buffer_ + start length:end - start];
  }
  @throw StartEndAndLength(self, start, end);
}

- (NSString *)description {
  return AbstractStringBuilder_toString(self);
}

NSString *AbstractStringBuilder_toString(JavaLangAbstractStringBuilder *self) {
  if (self->count_ == 0) {
    return @"";
  }
  return [NSString stringWithCharacters:self->buffer_ length:self->count_];
}

- (id<JavaLangCharSequence>)subSequenceWithInt:(jint)start
                                       withInt:(jint)end {
  return [self substringWithInt:start withInt:end];
}

- (jint)indexOfWithNSString:(NSString *)string {
  return [self indexOfWithNSString:string withInt:0];
}

- (jint)indexOfWithNSString:(NSString *)subString
                    withInt:(jint)start {
  nil_chk(subString);
  if (start < 0) {
    start = 0;
  }
  jint subCount = (jint)[subString length];
  if (subCount > 0) {
    if (subCount + start > count_) {
      return -1;
    }
    jchar firstChar = [subString characterAtIndex:0];
    while (YES) {
      jint i = start;
      jboolean found = NO;
      for (; i < count_; i++) {
        if (buffer_[i] == firstChar) {
          found = YES;
          break;
        }
      }
      if (!found || subCount + i > count_) {
        return -1;
      }
      jint o1 = i, o2 = 0;
      while (++o2 < subCount && buffer_[++o1] == [subString characterAtIndex:o2]) {
      }
      if (o2 == subCount) {
        return i;
      }
      start = i + 1;
    }
  }
  return (start < count_ || start == 0) ? start : count_;
}

- (jint)lastIndexOfWithNSString:(NSString *)string {
  return [self lastIndexOfWithNSString:string withInt:count_];
}

- (jint)lastIndexOfWithNSString:(NSString *)subString
                        withInt:(jint)start {
  nil_chk(subString);
  jint subCount = (jint)[subString length];
  if (subCount <= count_ && start >= 0) {
    if (subCount > 0) {
      if (start > count_ - subCount) {
        start = count_ - subCount;
      }
      jchar firstChar = [subString characterAtIndex:0];
      while (YES) {
        jint i = start;
        jboolean found = NO;
        for (; i >= 0; --i) {
          if (buffer_[i] == firstChar) {
            found = YES;
            break;
          }
        }
        if (!found) {
          return -1;
        }
        jint o1 = i, o2 = 0;
        while (++o2 < subCount && buffer_[++o1] == [subString characterAtIndex:o2]) {
        }
        if (o2 == subCount) {
          return i;
        }
        start = i - 1;
      }
    }
    return start < count_ ? start : count_;
  }
  return -1;
}

- (void)trimToSize {
  if (count_ < bufferSize_) {
    jchar *oldBuffer = buffer_;
    NewBuffer(self, count_);
    memcpy(buffer_, oldBuffer, count_ * sizeof(jchar));
    free(oldBuffer);
  }
}

// Defined in Character.java
jint JavaLangCharacter_codePointAtRaw(const jchar *seq, jint index, jint limit);
jint JavaLangCharacter_codePointBeforeRaw(const jchar *seq, jint index, jint start);
jint JavaLangCharacter_codePointCountRaw(const jchar *seq, jint offset, jint count);
jint JavaLangCharacter_offsetByCodePointsRaw(
    const jchar *seq, jint start, jint count, jint index, jint codePointOffset);

- (jint)codePointAtWithInt:(jint)index {
  if (index < 0 || index >= count_) {
    @throw IndexAndLength(self, index);
  }
  return JavaLangCharacter_codePointAtRaw(buffer_, index, count_);
}

- (jint)codePointBeforeWithInt:(jint)index {
  if (index < 1 || index > count_) {
    @throw IndexAndLength(self, index);
  }
  return JavaLangCharacter_codePointBeforeRaw(buffer_, index, 0);
}

- (jint)codePointCountWithInt:(jint)start
                      withInt:(jint)end {
  if (start < 0 || end > count_ || start > end) {
    @throw StartEndAndLength(self, start, end);
  }
  return JavaLangCharacter_codePointCountRaw(buffer_, start, end - start);
}

- (jint)offsetByCodePointsWithInt:(jint)index
                          withInt:(jint)codePointOffset {
  return JavaLangCharacter_offsetByCodePointsRaw(buffer_, 0, count_, index, codePointOffset);
}

- (void)dealloc {
  free(buffer_);
  [super dealloc];
}

+ (J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { "getValue", NULL, "[C", 0x10, NULL },
    { "setWithCharArray:withInt:", "set", "V", 0x10, "Ljava.io.InvalidObjectException;" },
    { "init", "AbstractStringBuilder", NULL, 0x0, NULL },
    { "initWithInt:", "AbstractStringBuilder", NULL, 0x0, NULL },
    { "initWithNSString:", "AbstractStringBuilder", NULL, 0x0, NULL },
    { "capacity", NULL, "I", 0x1, NULL },
    { "charAtWithInt:", "charAt", "C", 0x1, NULL },
    { "ensureCapacityWithInt:", "ensureCapacity", "V", 0x1, NULL },
    { "getCharsWithInt:withInt:withCharArray:withInt:", "getChars", "V", 0x1, NULL },
    { "length", NULL, "I", 0x1, NULL },
    { "setCharAtWithInt:withChar:", "setCharAt", "V", 0x1, NULL },
    { "setLengthWithInt:", "setLength", "V", 0x1, NULL },
    { "substringWithInt:", "substring", "Ljava.lang.String;", 0x1, NULL },
    { "substringWithInt:withInt:", "substring", "Ljava.lang.String;", 0x1, NULL },
    { "description", "toString", "Ljava.lang.String;", 0x1, NULL },
    { "subSequenceWithInt:withInt:", "subSequence", "Ljava.lang.CharSequence;", 0x1, NULL },
    { "indexOfWithNSString:", "indexOf", "I", 0x1, NULL },
    { "indexOfWithNSString:withInt:", "indexOf", "I", 0x1, NULL },
    { "lastIndexOfWithNSString:", "lastIndexOf", "I", 0x1, NULL },
    { "lastIndexOfWithNSString:withInt:", "lastIndexOf", "I", 0x1, NULL },
    { "trimToSize", NULL, "V", 0x1, NULL },
    { "codePointAtWithInt:", "codePointAt", "I", 0x1, NULL },
    { "codePointBeforeWithInt:", "codePointBefore", "I", 0x1, NULL },
    { "codePointCountWithInt:withInt:", "codePointCount", "I", 0x1, NULL },
    { "offsetByCodePointsWithInt:withInt:", "offsetByCodePoints", "I", 0x1, NULL },
  };
  static J2ObjcFieldInfo fields[] = {
    { "INITIAL_CAPACITY_", NULL, 0x18, "I", NULL, .constantValue.asInt = INITIAL_CAPACITY },
    { "count_", NULL, 0x2, "I", NULL,  },
  };
  static J2ObjcClassInfo _JavaLangAbstractStringBuilder = { "AbstractStringBuilder", "java.lang", NULL, 0x400, 46, methods, 4, fields, 0, NULL};
  return &_JavaLangAbstractStringBuilder;
}

@end
