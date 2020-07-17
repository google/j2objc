#include "J2ObjC_header.h"

#if !defined (OrgSlowstreamJ2objcUnreachableError_) 
#define OrgSlowstreamJ2objcUnreachableError_

@class IOSObjectArray;
@class JavaLangThrowable;

@interface OrgSlowstreamJ2objcUnreachableError : NSObject {
}

#pragma mark Public

- (instancetype)initWithNSObjectArray:(IOSObjectArray *)objects;

+ (id)throwUnreachableObjectErrorWithNSObjectArray:(IOSObjectArray *)objects;

+ (jbyte)throwUnreachablePrimitiveErrorWithNSObjectArray:(IOSObjectArray *)objects;

@end

FOUNDATION_EXPORT void OrgSlowstreamJ2objcUnreachableError_initWithNSObjectArray_(OrgSlowstreamJ2objcUnreachableError * self, IOSObjectArray * objects) J2OBJC_METHOD_ATTR;

FOUNDATION_EXPORT OrgSlowstreamJ2objcUnreachableError *new_OrgSlowstreamJ2objcUnreachableError_initWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR NS_RETURNS_RETAINED;

FOUNDATION_EXPORT OrgSlowstreamJ2objcUnreachableError *create_OrgSlowstreamJ2objcUnreachableError_initWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR;

FOUNDATION_EXPORT jbyte OrgSlowstreamJ2objcUnreachableError_throwUnreachablePrimitiveErrorWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR;

FOUNDATION_EXPORT id OrgSlowstreamJ2objcUnreachableError_throwUnreachableObjectErrorWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR;

#endif

