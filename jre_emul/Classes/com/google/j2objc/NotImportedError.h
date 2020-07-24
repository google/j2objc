#include "J2ObjC_header.h"

#if !defined (OrgSlowcodersJ2objcNotImportedError_)
#define OrgSlowcodersJ2objcNotImportedError_

@class IOSObjectArray;
@class JavaLangThrowable;

@interface OrgSlowcodersJ2objcNotImportedError : NSObject {
}

#pragma mark Public

- (instancetype)initWithNSObjectArray:(IOSObjectArray *)objects;

+ (id)throwUnreachableObjectErrorWithNSObjectArray:(IOSObjectArray *)objects;

+ (jbyte)throwUnreachablePrimitiveErrorWithNSObjectArray:(IOSObjectArray *)objects;

@end

FOUNDATION_EXPORT void OrgSlowcodersJ2objcNotImportedError_initWithNSObjectArray_(OrgSlowcodersJ2objcNotImportedError * self, IOSObjectArray * objects) J2OBJC_METHOD_ATTR;

FOUNDATION_EXPORT OrgSlowcodersJ2objcNotImportedError *new_OrgSlowcodersJ2objcNotImportedError_initWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR NS_RETURNS_RETAINED;

FOUNDATION_EXPORT OrgSlowcodersJ2objcNotImportedError *create_OrgSlowcodersJ2objcNotImportedError_initWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR;

FOUNDATION_EXPORT jbyte OrgSlowcodersJ2objcNotImportedError_throwUnreachablePrimitiveErrorWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR;

FOUNDATION_EXPORT id OrgSlowcodersJ2objcNotImportedError_throwUnreachableObjectErrorWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR;

#endif

