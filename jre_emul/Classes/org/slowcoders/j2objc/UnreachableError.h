#include "J2ObjC_header.h"

#if !defined (OrgSlowcodersJ2objcUnreachableError_)
#define OrgSlowcodersJ2objcUnreachableError_

@class IOSObjectArray;
@class JavaLangThrowable;

@interface OrgSlowcodersJ2objcUnreachableError : NSObject {
}

#pragma mark Public

- (instancetype)initWithNSObjectArray:(IOSObjectArray *)objects;

+ (id)throwUnreachableObjectErrorWithNSObjectArray:(IOSObjectArray *)objects;

+ (jbyte)throwUnreachablePrimitiveErrorWithNSObjectArray:(IOSObjectArray *)objects;

@end

FOUNDATION_EXPORT void OrgSlowcodersJ2objcUnreachableError_initWithNSObjectArray_(OrgSlowcodersJ2objcUnreachableError * self, IOSObjectArray * objects) J2OBJC_METHOD_ATTR;

FOUNDATION_EXPORT OrgSlowcodersJ2objcUnreachableError *new_OrgSlowcodersJ2objcUnreachableError_initWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR NS_RETURNS_RETAINED;

FOUNDATION_EXPORT OrgSlowcodersJ2objcUnreachableError *create_OrgSlowcodersJ2objcUnreachableError_initWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR;

FOUNDATION_EXPORT jbyte OrgSlowcodersJ2objcUnreachableError_throwUnreachablePrimitiveErrorWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR;

FOUNDATION_EXPORT id OrgSlowcodersJ2objcUnreachableError_throwUnreachableObjectErrorWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR;

#endif

