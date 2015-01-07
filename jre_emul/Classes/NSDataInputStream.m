//
//  NSDataInputStream.m
//  JreEmulation
//
//  Created by Pankaj Kakkar on 5/20/13.
//
//

#import "NSDataInputStream.h"

@interface NSDataInputStream() {
@private
  NSData *data_;
  const char *bytes_;
  size_t position_;
  size_t length_;
}

@end

@implementation NSDataInputStream

- (instancetype)initWithData:(NSData *)data {
  if ((self = [super init])) {
    data_ = [nil_chk(data) copy];
    bytes_ = (const char *) [data_ bytes];
    position_ = 0;
    length_ = [data_ length];
  }

  return self;
}

+ (NSDataInputStream *)streamWithData:(NSData *)data {
  return AUTORELEASE([[NSDataInputStream alloc] initWithData:data]);
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [data_ autorelease];
  [super dealloc];
}
#endif

- (jint)read {
  if (position_ == length_) {
    return -1;
  }

  // Ensure that we don't sign extend and accidentally return -1
  unsigned char c = bytes_[position_++];
  return (jint) c;
}

- (jint)readWithJavaLangByteArray:(IOSByteArray *)b
                          withInt:(jint)offset
                          withInt:(jint)len {
  if (len == 0) {
    return 0;
  }

  if ((size_t) position_ == length_) {
    return -1;
  }

  jint remaining = (jint) ([data_ length] - position_);
  if (remaining < len) {
    len = remaining;
  }

  [nil_chk(b) replaceBytes:(const jbyte *)(bytes_ + position_)
                    length:len
                    offset:offset];
  position_ += len;

  return len;
}

@end

