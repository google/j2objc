//
//  NSDataInputStream.m
//  JreEmulation
//
//  Created by Pankaj Kakkar on 5/20/13.
//
//

#import "NSDataInputStream.h"

#import "IOSPrimitiveArray.h"

@interface NSDataInputStream() {
@private
  NSData *data_;
  size_t position_;
}

@end

@implementation NSDataInputStream

- (instancetype)initWithData:(NSData *)data {
  if ((self = [super init])) {
    data_ = [data retain];
    position_ = 0;
  }

  return self;
}

+ (NSDataInputStream *)streamWithData:(NSData *)data {
  return AUTORELEASE([[NSDataInputStream alloc] initWithData:data]);
}

#if !__has_feature(objc_arc)
- (void)dealloc {
  [data_ release];
  [super dealloc];
}
#endif

- (jint)read {
  if (position_ == data_.length) {
    return -1;
  }

  // Ensure that we don't sign extend and accidentally return -1
  unsigned char b = *((const unsigned char *)(data_.bytes) + position_++);
  return (jint) b;
}

- (jint)readWithByteArray:(IOSByteArray *)b
                  withInt:(jint)offset
                  withInt:(jint)len {
  if (len == 0) {
    return 0;
  }

  if ((size_t) position_ == data_.length) {
    return -1;
  }

  jint remaining = (jint) (data_.length - position_);
  if (remaining < len) {
    len = remaining;
  }

  [nil_chk(b) replaceBytes:(const jbyte *)(data_.bytes) + position_
                    length:len
                    offset:offset];
  position_ += len;

  return len;
}

- (jint)available {
  return (jint) (data_.length - position_);
}

- (void)close {
  [data_ release];
  data_ = nil;
  position_ = 0;
}

@end

