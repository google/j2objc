//
//  NSDataOutputStream.h
//  JreEmulation
//
//  Created by Pankaj Kakkar on 5/20/13.
//
//

#ifndef _NSDataOutputStream_H_
#define _NSDataOutputStream_H_

#import "java/io/OutputStream.h"

#if __has_feature(nullability)
#pragma clang diagnostic push
#pragma GCC diagnostic ignored "-Wnullability-completeness"
#endif

// A concrete subclass of java.io.InputStream that writes into
// a backing NSData instance, retrievable at any point.
NS_ASSUME_NONNULL_BEGIN
@interface NSDataOutputStream : JavaIoOutputStream

+ (NSDataOutputStream *)stream;

// Retrieve the data written so far. If further writes to the
// stream are possible, callers must copy this instance to insulate
// themselves.
- (NSData *)data;

@end
NS_ASSUME_NONNULL_END

#if __has_feature(nullability)
#pragma clang diagnostic pop
#endif

#endif // _NSDataOutputStream_H_
