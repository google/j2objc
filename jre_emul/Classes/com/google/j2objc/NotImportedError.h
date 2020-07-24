#include "J2ObjC_header.h"

#if !defined (ComGoogleJ2objcNotImportedError_)
#define ComGoogleJ2objcNotImportedError_

@class IOSObjectArray;
@class JavaLangThrowable;

@interface ComGoogleJ2objcNotImportedError : NSObject {
}

#pragma mark Public

- (instancetype)initWithNSObjectArray:(IOSObjectArray *)objects;

+ (id)throwUnreachableObjectErrorWithNSObjectArray:(IOSObjectArray *)objects;

+ (jbyte)throwUnreachablePrimitiveErrorWithNSObjectArray:(IOSObjectArray *)objects;

@end

FOUNDATION_EXPORT void ComGoogleJ2objcNotImportedError_initWithNSObjectArray_(ComGoogleJ2objcNotImportedError * self, IOSObjectArray * objects) J2OBJC_METHOD_ATTR;

FOUNDATION_EXPORT ComGoogleJ2objcNotImportedError *new_ComGoogleJ2objcNotImportedError_initWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR NS_RETURNS_RETAINED;

FOUNDATION_EXPORT ComGoogleJ2objcNotImportedError *create_ComGoogleJ2objcNotImportedError_initWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR;

FOUNDATION_EXPORT jbyte ComGoogleJ2objcNotImportedError_throwUnreachablePrimitiveErrorWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR;

FOUNDATION_EXPORT id ComGoogleJ2objcNotImportedError_throwUnreachableObjectErrorWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR;

#endif

