#ifndef JavaLangThrowable_
#define JavaLangThrowable_

#include "J2ObjC_common.h"
#include "NSException+JavaThrowable.h"

@compatibility_alias JavaLangThrowable NSException;

__attribute__((always_inline)) inline JavaLangThrowable *create_JavaLangThrowable_initWithNSString_(
    NSString *msg) {
  return AUTORELEASE([[JavaLangThrowable alloc] initWithNSString:msg]);
}

#endif  // JavaLangThrowable_
