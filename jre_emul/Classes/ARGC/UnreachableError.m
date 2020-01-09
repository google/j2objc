#include "org/slowcoders/j2objc/UnreachableError.h"

@implementation OrgSlowcodersJ2objcUnreachableError
- (instancetype)initWithNSObjectArray:(IOSObjectArray *)objects {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return nil;
}

+ (id)throwUnreachableObjectErrorWithNSObjectArray:(IOSObjectArray *)objects {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return nil;
}

+ (jbyte)throwUnreachablePrimitiveErrorWithNSObjectArray:(IOSObjectArray *)objects {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return 0;
}

@end

id new_OrgSlowcodersJ2objcUnreachableError_initWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR NS_RETURNS_RETAINED
{
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return nil;
}

id OrgSlowcodersJ2objcUnreachableError_throwUnreachableObjectErrorWithNSObjectArray_(IOSObjectArray * objects) {
    [NSException raise:@"NSException" format:@"Unreachabe access error"];
    return nil;
}

