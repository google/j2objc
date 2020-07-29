#import "J2ObjC_common.h"

#if J2OBJC_USE_GC
#include "com/google/j2objc/NotImportedError.h"

@implementation ComGoogleJ2objcNotImportedError
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

id new_ComGoogleJ2objcNotImportedError_initWithNSObjectArray_(IOSObjectArray * objects) J2OBJC_METHOD_ATTR NS_RETURNS_RETAINED
{
  [NSException raise:@"NSException" format:@"Unreachabe access error"];
  return nil;
}

id ComGoogleJ2objcNotImportedError_throwUnreachableObjectErrorWithNSObjectArray_(IOSObjectArray * objects) {
  [NSException raise:@"NSException" format:@"Unreachabe access error"];
  return nil;
}

#endif
