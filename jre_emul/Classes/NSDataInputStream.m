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
  int position_;
  int length_;
}

@end

@implementation NSDataInputStream

- (id)initWithData:(NSData *)data {
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

- (int)read {
  if (position_ == length_) {
    return -1;
  }

  // Ensure that we don't sign extend and accidentally return -1
  unsigned char c = bytes_[position_++];
  return (int) c;
}

- (int)readWithJavaLangByteArray:(IOSByteArray *)b
                         withInt:(int)offset
                         withInt:(int)len {
  if (len == 0) {
    return 0;
  }

  if (position_ == length_) {
    return -1;
  }

  int remaining = [data_ length] - position_;
  if (remaining < len) {
    len = remaining;
  }

  [nil_chk(b) replaceBytes:bytes_ + position_
                    length:len
                    offset:offset];
  position_ += len;

  return len;
}

@end

