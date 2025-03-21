// This is a stub file to make TextFormat.printer().printToString() available to j2objc.
#ifndef __ComGoogleProtobufTextFormat_H__
#define __ComGoogleProtobufTextFormat_H__


#import "JreEmulation.h"
#include "com/google/protobuf/common.h"
#include "com/google/protobuf/MessageOrBuilder.h"

@class ComGoogleProtobufTextFormat_Printer;
@class ComGoogleProtobufTextFormat;

@interface ComGoogleProtobufTextFormat : NSObject

+ (ComGoogleProtobufTextFormat_Printer *)printer;

@end

@interface ComGoogleProtobufTextFormat_Printer : NSObject

- (NSString *)printToStringWithComGoogleProtobufMessageOrBuilder:(id<ComGoogleProtobufMessageOrBuilder>)messageOrBuilder;

@end

CF_EXTERN_C_BEGIN

ComGoogleProtobufTextFormat_Printer *ComGoogleProtobufTextFormat_printer(void);

CF_EXTERN_C_END

#endif // __ComGoogleProtobufTextFormat_H__