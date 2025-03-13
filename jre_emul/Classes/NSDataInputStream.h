//
//  NSDataInputStream.h
//  JreEmulation
//
//  Created by Pankaj Kakkar on 5/20/13.
//
//

#ifndef _NSDataInputStream_H_
#define _NSDataInputStream_H_

#import "java/io/InputStream.h"

#if __has_feature(nullability)
#pragma clang diagnostic push
#pragma GCC diagnostic ignored "-Wnullability-completeness"
#endif

// A concrete subclass of java.io.InputStream that reads from
// a given NSData instance. The NSData instance is copied at
// initialization, so further modifications (if it happened to
// be mutable) will not be visible.
NS_ASSUME_NONNULL_BEGIN
@interface NSDataInputStream : JavaIoInputStream

- (instancetype)initWithData:(NSData *)data;
+ (NSDataInputStream *)streamWithData:(NSData *)data;

@end
NS_ASSUME_NONNULL_END

#if __has_feature(nullability)
#pragma clang diagnostic pop
#endif

#endif // _NSDataInputStream_H_
