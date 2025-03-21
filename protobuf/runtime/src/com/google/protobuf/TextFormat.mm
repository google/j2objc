#include "com/google/protobuf/TextFormat.h"
#import <Foundation/Foundation.h>

@implementation ComGoogleProtobufTextFormat_Printer

- (NSString *)printToStringWithComGoogleProtobufMessageOrBuilder:(id<ComGoogleProtobufMessageOrBuilder>)messageOrBuilder {
    // Implement this if needed.
    return [messageOrBuilder description];
}

@end

@implementation ComGoogleProtobufTextFormat

static ComGoogleProtobufTextFormat_Printer *sharedPrinter = nil;

+ (void)initialize {
    if (self == [ComGoogleProtobufTextFormat class]) {
        sharedPrinter = [[ComGoogleProtobufTextFormat_Printer alloc] init];
    }
}

+ (ComGoogleProtobufTextFormat_Printer *)printer {
    return sharedPrinter;
}

@end

ComGoogleProtobufTextFormat_Printer *ComGoogleProtobufTextFormat_printer(void) {
    static ComGoogleProtobufTextFormat_Printer *printer = nil;
    if (printer == nil) {
        printer = [[ComGoogleProtobufTextFormat_Printer alloc] init];
    }
    return printer;
}