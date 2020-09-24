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

#include "J2ObjC_source.h"
#include "java/io/InvalidObjectException.h"
#include "java/lang/ArrayIndexOutOfBoundsException.h"
#include "java/lang/Character.h"
#include "java/lang/Integer.h"
#include "java/lang/Long.h"
#include "java/lang/NegativeArraySizeException.h"
#include "java/lang/NullPointerException.h"
#include "java/lang/OutOfMemoryError.h"
#include "java/lang/StringBuffer.h"
#include "java/lang/StringIndexOutOfBoundsException.h"
#include "java/util/Arrays.h"
#include "java/util/stream/IntStream.h"
#include "java_lang_Integer.h"
#include "java_lang_Long.h"
#include "libcore/util/EmptyArray.h"
#include "sun/misc/FloatingDecimal.h"

// AbstractStringBuilder is abstract and doesn't implement all members of Appendable or
// CharSequence.
#pragma clang diagnostic ignored "-Wprotocol"

// Full name as expected by generated metadata.
#define JavaLangAbstractStringBuilder_INITIAL_CAPACITY 16
// Shorter alias for convenience.
#define INITIAL_CAPACITY JavaLangAbstractStringBuilder_INITIAL_CAPACITY

static void JreStringBuilder_move(JreStringBuilder *self, jint size, jint index);

static void OutOfMemory() {
  @throw AUTORELEASE([[JavaLangOutOfMemoryError alloc] init]);
}

static inline void NewBuffer(JreStringBuilder *sb, jint size) {
  sb->buffer_ = (jchar *)malloc(size * sizeof(jchar));
  if (__builtin_expect(sb->buffer_ == NULL, 0)) {
    OutOfMemory();
  }
  sb->bufferSize_ = size;
}

static JavaLangStringIndexOutOfBoundsException *IndexAndLength(JreStringBuilder *sb, jint index) {
  id exc = [[JavaLangStringIndexOutOfBoundsException alloc] initWithNSString:
      [NSString stringWithFormat:@"this.length=%d; index=%d", sb->count_, index]];
  @throw AUTORELEASE(exc);
}

static JavaLangStringIndexOutOfBoundsException *StartEndAndLength(
    JreStringBuilder *sb, jint start, jint end) {
  id exc = [[JavaLangStringIndexOutOfBoundsException alloc] initWithNSString:
      [NSString stringWithFormat:@"this.length=%d; start=%d; length=%d", sb->count_, start,
      end - start]];
  @throw AUTORELEASE(exc);
}

@implementation JavaLangAbstractStringBuilder

- (IOSCharArray *)getValue {
  return [IOSCharArray arrayWithChars:delegate_.buffer_ count:delegate_.count_];
}

void JreStringBuilder_initWithCapacity(JreStringBuilder *sb, jint capacity) {
  NewBuffer(sb, capacity);
  sb->count_ = 0;
}

- (instancetype)initPackagePrivate {
  JavaLangAbstractStringBuilder_initPackagePrivate(self);
  return self;
}

- (instancetype)initPackagePrivateWithInt:(jint)capacity {
  JavaLangAbstractStringBuilder_initPackagePrivateWithInt_(self, capacity);
  return self;
}

- (instancetype)initWithNSString:(NSString *)string {
  JavaLangAbstractStringBuilder_initWithNSString_(self, string);
  return self;
}

void JavaLangAbstractStringBuilder_initPackagePrivate(JavaLangAbstractStringBuilder *self) {
  NewBuffer(&self->delegate_, INITIAL_CAPACITY);
}

void JavaLangAbstractStringBuilder_initPackagePrivateWithInt_(
    JavaLangAbstractStringBuilder *self, jint capacity) {
  if (capacity < 0) {
    id exc = [[JavaLangNegativeArraySizeException alloc] initWithNSString:
        JavaLangInteger_toStringWithInt_(capacity)];
    @throw AUTORELEASE(exc);
  }
  NewBuffer(&self->delegate_, capacity);
}

void JavaLangAbstractStringBuilder_initWithNSString_(
    JavaLangAbstractStringBuilder *self, NSString *string) {
  (void)nil_chk(string);
  self->delegate_.count_ = (jint)[string length];
  NewBuffer(&self->delegate_, self->delegate_.count_ + INITIAL_CAPACITY);
  [string getCharacters:self->delegate_.buffer_ range:NSMakeRange(0, self->delegate_.count_)];
}

static void EnlargeBuffer(JreStringBuilder *sb, jint min) {
  jint newSize = MAX(((sb->bufferSize_ >> 1) + sb->bufferSize_) + 2, min);
  sb->buffer_ = (jchar *)realloc(sb->buffer_, newSize * sizeof(jchar));
  if (__builtin_expect(sb->buffer_ == NULL, 0)) {
    OutOfMemory();
  }
  sb->bufferSize_ = newSize;
}

static void EnsureCapacity(JreStringBuilder *sb, jint size) {
  if (size > sb->bufferSize_) {
    EnlargeBuffer(sb, size);
  }
}

void JreStringBuilder_appendNull(JreStringBuilder *sb) {
  jint newCount = sb->count_ + 4;
  EnsureCapacity(sb, newCount);
  jchar *buf = sb->buffer_ + sb->count_;
  *(buf++) = 'n';
  *(buf++) = 'u';
  *(buf++) = 'l';
  *(buf++) = 'l';
  sb->count_ += 4;
}

void JreStringBuilder_appendBuffer(JreStringBuilder *sb, const unichar *buffer, int length) {
  int newCount = sb->count_ + length;
  EnsureCapacity(sb, newCount);
  memcpy(sb->buffer_ + sb->count_, buffer, length * sizeof(jchar));
  sb->count_ = newCount;
}

void JreStringBuilder_appendStringBuffer(JreStringBuilder *sb, JavaLangStringBuffer *toAppend) {
  if (toAppend) {
    @synchronized(toAppend) {
      JreStringBuilder_appendBuffer(sb, toAppend->delegate_.buffer_, toAppend->delegate_.count_);
    }
  } else {
    JreStringBuilder_appendNull(sb);
  }
}

void JreStringBuilder_appendCharArray(JreStringBuilder *sb, IOSCharArray *chars) {
  (void)nil_chk(chars);
  JreStringBuilder_appendBuffer(sb, chars->buffer_, chars->size_);
}

void JreStringBuilder_appendCharArraySubset(
    JreStringBuilder *sb, IOSCharArray *chars, jint offset, jint length) {
  (void)nil_chk(chars);
  IOSArray_checkRange(chars->size_, offset, length);
  JreStringBuilder_appendBuffer(sb, chars->buffer_ + offset, length);
}

void JreStringBuilder_appendChar(JreStringBuilder *sb, jchar ch) {
  EnsureCapacity(sb, sb->count_ + 1);
  sb->buffer_[sb->count_++] = ch;
}

void JreStringBuilder_appendString(JreStringBuilder *sb, NSString *string) {
  if (string == nil) {
    JreStringBuilder_appendNull(sb);
    return;
  }
  jint length = (jint)CFStringGetLength((CFStringRef)string);
  jint newCount = sb->count_ + length;
  EnsureCapacity(sb, newCount);
  CFStringGetCharacters((CFStringRef)string, CFRangeMake(0, length), sb->buffer_ + sb->count_);
  sb->count_ = newCount;
}

void JreStringBuilder_appendCharSequence(JreStringBuilder *sb, id<JavaLangCharSequence> s) {
  if (s) {
    JreStringBuilder_appendCharSequenceSubset(sb, s, 0, [s java_length]);
  } else {
    JreStringBuilder_appendNull(sb);
  }
}

void JreStringBuilder_appendCharSequenceSubset(
    JreStringBuilder *sb, id<JavaLangCharSequence> s, jint start, jint end) {
  if (s == nil) {
    s = @"null";
  }
  if ((start | end) < 0 || start > end || end > [s java_length]) {
    @throw AUTORELEASE([[JavaLangIndexOutOfBoundsException alloc] init]);
  }
  jint length = end - start;
  jint newCount = sb->count_ + length;
  EnsureCapacity(sb, newCount);
  if ([s isKindOfClass:[NSString class]]) {
    [(NSString *)s getCharacters:sb->buffer_ + sb->count_ range:NSMakeRange(start, end - start)];
  } else if ([s isKindOfClass:[JavaLangAbstractStringBuilder class]]) {
    JavaLangAbstractStringBuilder *other = (JavaLangAbstractStringBuilder *)s;
    memcpy(sb->buffer_ + sb->count_, other->delegate_.buffer_ + start, length * sizeof(jchar));
  } else {
    jint j = sb->count_;
    for (jint i = start; i < end; i++) {
      sb->buffer_[j++] = [s charAtWithInt:i];
    }
  }
  sb->count_ = newCount;
}

void JreStringBuilder_appendInt(JreStringBuilder *sb, jint i) {
  if (i == JavaLangInteger_MIN_VALUE) {
    JreStringBuilder_appendString(sb, @"-2147483648");
    return;
  }
  jint appendedLength = (i < 0) ? JavaLangInteger_stringSizeWithInt_(-i) + 1
      : JavaLangInteger_stringSizeWithInt_(i);
  jint newCount = sb->count_ + appendedLength;
  EnsureCapacity(sb, newCount);
  JavaLangInteger_getCharsRaw(i, newCount, sb->buffer_);
  sb->count_ = newCount;
}

void JreStringBuilder_appendLong(JreStringBuilder *sb, jlong l) {
  if (l == JavaLangLong_MIN_VALUE) {
    JreStringBuilder_appendString(sb, @"-9223372036854775808");
    return;
  }
  jint appendedLength = (l < 0) ? JavaLangLong_stringSizeWithLong_(-l) + 1
      : JavaLangLong_stringSizeWithLong_(l);
  jint newCount = sb->count_ + appendedLength;
  EnsureCapacity(sb, newCount);
  JavaLangLong_getCharsRaw(l, newCount, sb->buffer_);
  sb->count_ = newCount;
}

void JreStringBuilder_appendDouble(JreStringBuilder *sb, jdouble d) {
  SunMiscFloatingDecimal_appendToWithDouble_withId_(d, (id)sb);
}

void JreStringBuilder_appendFloat(JreStringBuilder *sb, jfloat f) {
  SunMiscFloatingDecimal_appendToWithFloat_withId_(f, (id)sb);
}

- (jint)capacity {
  return delegate_.bufferSize_;
}

- (jchar)charAtWithInt:(jint)index {
  if (index < 0 || index >= delegate_.count_) {
    @throw IndexAndLength(&delegate_, index);
  }
  return delegate_.buffer_[index];
}

void JreStringBuilder_delete(JreStringBuilder *sb, jint start, jint end) {
  if (start >= 0) {
    if (end > sb->count_) {
      end = sb->count_;
    }
    if (end == start) {
      return;
    }
    if (end > start) {
      jint length = sb->count_ - end;
      if (length >= 0) {
        memmove(sb->buffer_ + start, sb->buffer_ + end, length * sizeof(jchar));
      }
      sb->count_ -= end - start;
      return;
    }
  }
  @throw StartEndAndLength(sb, start, end);
}

void JreStringBuilder_deleteCharAt(JreStringBuilder *sb, jint index) {
  if (index < 0 || index >= sb->count_) {
    @throw IndexAndLength(sb, index);
  }
  jint length = sb->count_ - index - 1;
  if (length > 0) {
    memmove(sb->buffer_ + index, sb->buffer_ + index + 1, length * sizeof(jchar));
  }
  sb->count_--;
}

- (void)ensureCapacityWithInt:(jint)min {
  if (min > delegate_.bufferSize_) {
    jint ourMin = delegate_.bufferSize_ * 2 + 2;
    EnlargeBuffer(&delegate_, MAX(ourMin, min));
  }
}

- (void)getCharsWithInt:(jint)start
                withInt:(jint)end
          withCharArray:(IOSCharArray *)dst
                withInt:(jint)dstStart {
  if (start > delegate_.count_ || end > delegate_.count_ || start > end) {
    @throw StartEndAndLength(&delegate_, start, end);
  }
  jint length = end - start;
  IOSArray_checkRange(delegate_.bufferSize_, start, length);
  (void)nil_chk(dst);
  IOSArray_checkRange(dst->size_, dstStart, length);
  memcpy(dst->buffer_ + dstStart, delegate_.buffer_ + start, length * sizeof(jchar));
}

void JreStringBuilder_insertCharArray(JreStringBuilder *sb, jint index, IOSCharArray *chars) {
  if (index < 0 || index > sb->count_) {
    @throw IndexAndLength(sb, index);
  }
  (void)nil_chk(chars);
  if (chars->size_ != 0) {
    JreStringBuilder_move(sb, chars->size_, index);
    memcpy(sb->buffer_ + index, chars->buffer_, chars->size_ * sizeof(jchar));
    sb->count_ += chars->size_;
  }
}

void JreStringBuilder_insertCharArraySubset(
    JreStringBuilder *sb, jint index, IOSCharArray *chars, jint start, jint length) {
  (void)nil_chk(chars);
  if (index >= 0 && index <= sb->count_) {
    if (start >= 0 && length >= 0 && length <= chars->size_ - start) {
      if (length != 0) {
        JreStringBuilder_move(sb, length, index);
        memcpy(sb->buffer_ + index, chars->buffer_ + start, length * sizeof(jchar));
        sb->count_ += length;
      }
      return;
    }
  }
  id exc = [[JavaLangStringIndexOutOfBoundsException alloc] initWithNSString:
      [NSString stringWithFormat:@"this.length=%d; index=%d; chars.length=%d; start=%d; length=%d",
          sb->count_, index, chars->size_, start, length]];
  @throw AUTORELEASE(exc);
}

void JreStringBuilder_insertChar(JreStringBuilder *sb, jint index, jchar ch) {
  if (index < 0 || index > sb->count_) {
    id exc = [[JavaLangArrayIndexOutOfBoundsException alloc] initWithNSString:
        [NSString stringWithFormat:@"this.length=%d; index=%d", sb->count_, index]];
    @throw AUTORELEASE(exc);
  }
  JreStringBuilder_move(sb, 1, index);
  sb->buffer_[index] = ch;
  sb->count_++;
}

void JreStringBuilder_insertString(JreStringBuilder *sb, jint index, NSString *string) {
  if (index >= 0 && index <= sb->count_) {
    if (string == nil) {
      string = @"null";
    }
    jint min = (jint)[string length];
    if (min != 0) {
      JreStringBuilder_move(sb, min, index);
      [string getCharacters:sb->buffer_ + index range:NSMakeRange(0, min)];
      sb->count_ += min;
    }
  } else {
    @throw IndexAndLength(sb, index);
  }
}

void JreStringBuilder_insertCharSequence(
    JreStringBuilder *sb, jint index, id<JavaLangCharSequence> s, jint start,
    jint end) {
  if (s == nil) {
    s = @"null";
  }
  if ((index | start | end) < 0 || index > sb->count_ || start > end
      || end > [s java_length]) {
    @throw AUTORELEASE([[JavaLangIndexOutOfBoundsException alloc] init]);
  }
  JreStringBuilder_insertString(sb, index, [[s subSequenceFrom:start to:end] description]);
}

- (jint)java_length {
  return delegate_.count_;
}

void JreStringBuilder_move(JreStringBuilder *sb, jint size, jint index) {
  jint newCount;
  if (sb->bufferSize_ - sb->count_ >= size) {
    memmove(sb->buffer_ + index + size, sb->buffer_ + index,
        (sb->count_ - index) * sizeof(jchar));
    return;
  }
  newCount = MAX(sb->count_ + size, sb->bufferSize_ * 2 + 2);
  jchar *oldBuffer = sb->buffer_;
  NewBuffer(sb, newCount);
  memcpy(sb->buffer_, oldBuffer, index * sizeof(jchar));
  memcpy(sb->buffer_ + index + size, oldBuffer + index, (sb->count_ - index) * sizeof(jchar));
  free(oldBuffer);
}

void JreStringBuilder_replace(JreStringBuilder *sb, jint start, jint end, NSString *string) {
  if (start >= 0) {
    if (end > sb->count_) {
      end = sb->count_;
    }
    if (end > start) {
      (void)nil_chk(string);
      jint stringLength = (jint)[string length];
      jint diff = end - start - stringLength;
      if (diff > 0) {
        memmove(sb->buffer_ + start + stringLength, sb->buffer_ + end,
            (sb->count_ - end) * sizeof(jchar));
      } else if (diff < 0) {
        JreStringBuilder_move(sb, -diff, end);
      }
      [string getCharacters:sb->buffer_ + start range:NSMakeRange(0, stringLength)];
      sb->count_ -= diff;
      return;
    }
    if (start == end) {
      if (string == nil) {
        @throw AUTORELEASE([[JavaLangNullPointerException alloc]
            initWithNSString:@"string == null"]);
      }
      JreStringBuilder_insertString(sb, start, string);
      return;
    }
  }
  @throw StartEndAndLength(sb, start, end);
}

void JreStringBuilder_reverse(JreStringBuilder *sb) {
  if (sb->count_ < 2) {
    return;
  }
  jchar *buf = sb->buffer_;
  jint end = sb->count_ - 1;
  jchar frontHigh = buf[0];
  jchar endLow = buf[end];
  jboolean allowFrontSur = true, allowEndSur = true;
  for (jint i = 0, mid = sb->count_ / 2; i < mid; i++, --end) {
    jchar frontLow = buf[i + 1];
    jchar endHigh = buf[end - 1];
    jboolean surAtFront = allowFrontSur && frontLow >= (jint)0xdc00 && frontLow <= (jint)0xdfff
        && frontHigh >= (jint)0xd800 && frontHigh <= (jint)0xdbff;
    if (surAtFront && (sb->count_ < 3)) {
      return;
    }
    jboolean surAtEnd = allowEndSur && endHigh >= (jint)0xd800 && endHigh <= (jint)0xdbff
        && endLow >= (jint)0xdc00 && endLow <= (jint)0xdfff;
    allowFrontSur = allowEndSur = true;
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
        allowFrontSur = false;
      } else {
        buf[end] = frontHigh;
        buf[i] = endHigh;
        frontHigh = frontLow;
        allowEndSur = false;
      }
    }
  }
  if ((sb->count_ & 1) == 1 && (!allowFrontSur || !allowEndSur)) {
    buf[end] = allowFrontSur ? endLow : frontHigh;
  }
}

- (void)setCharAtWithInt:(jint)index
                withChar:(jchar)ch {
  if (index < 0 || index >= delegate_.count_) {
    @throw IndexAndLength(&delegate_, index);
  }
  delegate_.buffer_[index] = ch;
}

- (void)setLengthWithInt:(jint)length {
  if (length < 0) {
    id exc = [[JavaLangStringIndexOutOfBoundsException alloc]
        initWithNSString:[NSString stringWithFormat:@"length < 0: %d", length]];
    @throw AUTORELEASE(exc);
  }
  EnsureCapacity(&delegate_, length);
  if (delegate_.count_ < length) {
    memset(delegate_.buffer_ + delegate_.count_, 0, (length - delegate_.count_) * sizeof(jchar));
  }
  delegate_.count_ = length;
}

- (NSString *)substringWithInt:(jint)start {
  if (start >= 0 && start <= delegate_.count_) {
    if (start == delegate_.count_) {
      return @"";
    }
    return [NSString stringWithCharacters:delegate_.buffer_ + start
                                   length:delegate_.count_ - start];
  }
  @throw IndexAndLength(&delegate_, start);
}

- (NSString *)substringWithInt:(jint)start
                       withInt:(jint)end {
  if (start >= 0 && start <= end && end <= delegate_.count_) {
    if (start == end) {
      return @"";
    }
    return [NSString stringWithCharacters:delegate_.buffer_ + start length:end - start];
  }
  @throw StartEndAndLength(&delegate_, start, end);
}

- (NSString *)description {
  return JreStringBuilder_toString(&delegate_);
}

NSString *JreStringBuilder_toString(JreStringBuilder *sb) {
  if (sb->count_ == 0) {
    return @"";
  }
  return [NSString stringWithCharacters:sb->buffer_ length:sb->count_];
}

NSString *JreStringBuilder_toStringAndDealloc(JreStringBuilder *sb) {
  if (sb->count_ == 0) {
    free(sb->buffer_);
    return @"";
  }
  NSString *result;
  jint wasted = sb->bufferSize_ - sb->count_;
  // Same test as used by the original AbstractString.java to determine whether
  // to use a shared buffer.
  if (wasted >= 256 || (wasted >= INITIAL_CAPACITY && wasted >= (sb->count_ >> 1))) {
    result = (NSString *)CFStringCreateWithCharacters(NULL, sb->buffer_, sb->count_);
    free(sb->buffer_);
  } else {
    // Don't free the buffer because we're passing it off to the CFString constructor.
    result = (NSString *)CFStringCreateWithCharactersNoCopy(
        NULL, sb->buffer_, sb->count_, kCFAllocatorMalloc);
  }
  return AUTORELEASE(result);
}

- (id<JavaLangCharSequence>)subSequenceFrom:(jint)start
                                         to:(jint)end {
  return [self substringWithInt:start withInt:end];
}

- (jint)indexOfWithNSString:(NSString *)string {
  return [self indexOfWithNSString:string withInt:0];
}

- (jint)indexOfWithNSString:(NSString *)subString
                    withInt:(jint)start {
  (void)nil_chk(subString);
  if (start < 0) {
    start = 0;
  }
  jint subCount = (jint)[subString length];
  if (subCount > 0) {
    if (subCount + start > delegate_.count_) {
      return -1;
    }
    jchar firstChar = [subString characterAtIndex:0];
    while (true) {
      jint i = start;
      jboolean found = false;
      for (; i < delegate_.count_; i++) {
        if (delegate_.buffer_[i] == firstChar) {
          found = true;
          break;
        }
      }
      if (!found || subCount + i > delegate_.count_) {
        return -1;
      }
      jint o1 = i, o2 = 0;
      while (++o2 < subCount && delegate_.buffer_[++o1] == [subString characterAtIndex:o2]) {
      }
      if (o2 == subCount) {
        return i;
      }
      start = i + 1;
    }
  }
  return (start < delegate_.count_ || start == 0) ? start : delegate_.count_;
}

- (jint)lastIndexOfWithNSString:(NSString *)string {
  return [self lastIndexOfWithNSString:string withInt:delegate_.count_];
}

- (jint)lastIndexOfWithNSString:(NSString *)subString
                        withInt:(jint)start {
  (void)nil_chk(subString);
  jint subCount = (jint)[subString length];
  if (subCount <= delegate_.count_ && start >= 0) {
    if (subCount > 0) {
      if (start > delegate_.count_ - subCount) {
        start = delegate_.count_ - subCount;
      }
      jchar firstChar = [subString characterAtIndex:0];
      while (true) {
        jint i = start;
        jboolean found = false;
        for (; i >= 0; --i) {
          if (delegate_.buffer_[i] == firstChar) {
            found = true;
            break;
          }
        }
        if (!found) {
          return -1;
        }
        jint o1 = i, o2 = 0;
        while (++o2 < subCount && delegate_.buffer_[++o1] == [subString characterAtIndex:o2]) {
        }
        if (o2 == subCount) {
          return i;
        }
        start = i - 1;
      }
    }
    return start < delegate_.count_ ? start : delegate_.count_;
  }
  return -1;
}

- (void)trimToSize {
  if (delegate_.count_ < delegate_.bufferSize_) {
    jchar *oldBuffer = delegate_.buffer_;
    NewBuffer(&delegate_, delegate_.count_);
    memcpy(delegate_.buffer_, oldBuffer, delegate_.count_ * sizeof(jchar));
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
  if (index < 0 || index >= delegate_.count_) {
    @throw IndexAndLength(&delegate_, index);
  }
  return JavaLangCharacter_codePointAtRaw(delegate_.buffer_, index, delegate_.count_);
}

- (jint)codePointBeforeWithInt:(jint)index {
  if (index < 1 || index > delegate_.count_) {
    @throw IndexAndLength(&delegate_, index);
  }
  return JavaLangCharacter_codePointBeforeRaw(delegate_.buffer_, index, 0);
}

- (jint)codePointCountWithInt:(jint)start
                      withInt:(jint)end {
  if (start < 0 || end > delegate_.count_ || start > end) {
    @throw StartEndAndLength(&delegate_, start, end);
  }
  return JavaLangCharacter_codePointCountRaw(delegate_.buffer_, start, end - start);
}

- (jint)offsetByCodePointsWithInt:(jint)index
                          withInt:(jint)codePointOffset {
  return JavaLangCharacter_offsetByCodePointsRaw(
      delegate_.buffer_, 0, delegate_.count_, index, codePointOffset);
}

// Default methods in java.lang.CharSequence.
- (id<JavaUtilStreamIntStream>)chars {
  return JavaLangCharSequence_chars(self);
}

- (id<JavaUtilStreamIntStream>)codePoints {
  return JavaLangCharSequence_codePoints(self);
}

- (id<JavaLangAppendable>)appendWithJavaLangCharSequence:(id<JavaLangCharSequence>)cs {
  // can't call an abstract method
  [self doesNotRecognizeSelector:_cmd];
  return 0;
}

- (id<JavaLangAppendable>)appendWithJavaLangCharSequence:(id<JavaLangCharSequence>)cs
                                                 withInt:(jint)start
                                                 withInt:(jint)end {
  // can't call an abstract method
  [self doesNotRecognizeSelector:_cmd];
  return 0;
}

- (void)dealloc {
  free(delegate_.buffer_);
#if !__has_feature(objc_arc)
  [super dealloc];
#endif
}

// Suppress undeclared-selector warnings to avoid creating method bodies
// for all the abstract methods which are implemented in subclasses.
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wundeclared-selector"

+ (const J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { NULL, NULL, 0x0, -1, -1, -1, -1, -1, -1 },
    { NULL, NULL, 0x0, -1, 0, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 1, -1, -1, -1, -1, -1 },
    { NULL, "I", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "V", 0x1, 2, 0, -1, -1, -1, -1 },
    { NULL, "V", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "V", 0x1, 3, 0, -1, -1, -1, -1 },
    { NULL, "C", 0x1, 4, 0, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 5, 0, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 6, 0, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 7, 8, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 9, 8, -1, -1, -1, -1 },
    { NULL, "V", 0x1, 10, 11, -1, -1, -1, -1 },
    { NULL, "V", 0x1, 12, 13, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, 14, 0, -1, -1, -1, -1 },
    { NULL, "LJavaLangCharSequence;", 0x1, 15, 8, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, 14, 8, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 16, 17, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 16, 18, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 19, 17, -1, -1, -1, -1 },
    { NULL, "I", 0x1, 19, 18, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x401, 20, -1, -1, -1, -1, -1 },
    { NULL, "[C", 0x10, -1, -1, -1, -1, -1, -1 },
  };
  #pragma clang diagnostic push
  #pragma clang diagnostic ignored "-Wobjc-multiple-method-names"
  methods[0].selector = @selector(initPackagePrivate);
  methods[1].selector = @selector(initPackagePrivateWithInt:);
  methods[2].selector = @selector(java_length);
  methods[3].selector = @selector(capacity);
  methods[4].selector = @selector(ensureCapacityWithInt:);
  methods[5].selector = @selector(trimToSize);
  methods[6].selector = @selector(setLengthWithInt:);
  methods[7].selector = @selector(charAtWithInt:);
  methods[8].selector = @selector(codePointAtWithInt:);
  methods[9].selector = @selector(codePointBeforeWithInt:);
  methods[10].selector = @selector(codePointCountWithInt:withInt:);
  methods[11].selector = @selector(offsetByCodePointsWithInt:withInt:);
  methods[12].selector = @selector(getCharsWithInt:withInt:withCharArray:withInt:);
  methods[13].selector = @selector(setCharAtWithInt:withChar:);
  methods[14].selector = @selector(substringWithInt:);
  methods[15].selector = @selector(subSequenceFrom:to:);
  methods[16].selector = @selector(substringWithInt:withInt:);
  methods[17].selector = @selector(indexOfWithNSString:);
  methods[18].selector = @selector(indexOfWithNSString:withInt:);
  methods[19].selector = @selector(lastIndexOfWithNSString:);
  methods[20].selector = @selector(lastIndexOfWithNSString:withInt:);
  methods[21].selector = @selector(description);
  methods[22].selector = @selector(getValue);
  #pragma clang diagnostic pop
  static const void *ptrTable[] = {
    "I", "length", "ensureCapacity", "setLength", "charAt", "codePointAt", "codePointBefore",
    "codePointCount", "II", "offsetByCodePoints", "getChars", "II[CI", "setCharAt", "IC",
    "substring", "subSequence", "indexOf", "LNSString;", "LNSString;I", "lastIndexOf", "toString"
  };
  static const J2ObjcClassInfo _JavaLangAbstractStringBuilder = {
    "AbstractStringBuilder", "java.lang", ptrTable, methods, NULL, 7, 0x400, 23, 0, -1, -1, -1, -1,
    -1 };
  return &_JavaLangAbstractStringBuilder;
}

#pragma clang diagnostic pop

@end
