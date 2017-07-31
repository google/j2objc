//
//  ARGCArray.h
//  ARC+GC
//
//  Created by DAE HOON JI on 2017. 7. 22..
//  Copyright © 2017년 DAE HOON JI. All rights reserved.
//

#ifndef ARGCArrayUtil_h
#define ARGCArrayUtil_h

#import "ARGC.h"

void OS_INLINE ARGC_checkArrayIndex(ARGCArray* array, NSInteger index) {
    if ((NSUInteger)index >= array->length_) {
        [array throwInvalidIndexExceptionWithIndex:index withLength:0];
    }
}

void OS_INLINE ARGC_checkArrayRange(ARGCArray* array, NSInteger index, NSInteger length) {
    if ((index | length) < 0 || (NSUInteger)(index + length) >= array->length_) {
        [array throwInvalidIndexExceptionWithIndex:index withLength:length];
    }
}

void OS_INLINE ARGC_checkArrayRange(ARGCArray* array, NSRange range) {
    ARGC_checkArrayRange(array, range.location, range.length);
}

#endif /* ARGCArrayUtil_h */
