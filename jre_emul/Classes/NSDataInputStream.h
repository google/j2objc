//
//  NSDataInputStream.h
//  JreEmulation
//
//  Created by Pankaj Kakkar on 5/20/13.
//
//

#import <Foundation/Foundation.h>

#import "java/io/InputStream.h"

// A concrete subclass of java.io.InputStream that reads from
// a given NSData instance. The NSData instance is copied at
// initialization, so further modifications (if it happened to
// be mutable) will not be visible.
@interface NSDataInputStream : JavaIoInputStream

- (id)initWithData:(NSData *)data;
+ (NSDataInputStream *)streamWithData:(NSData *)data;

@end

