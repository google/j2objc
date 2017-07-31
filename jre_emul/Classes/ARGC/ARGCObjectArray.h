//
//  ARGCArray.h
//  ARC+GC
//
//  Created by DAE HOON JI on 2017. 7. 22..
//  Copyright © 2017년 DAE HOON JI. All rights reserved.
//

#ifndef ARGCArray_h
#define ARGCArray_h

#import "ARGC.h"

@interface ARGCObjectArray : ARGCArray <NSFastEnumeration> {
@public
    Class const elementType_;
    id ARGC_FIELD_REF buffer_[1];
}

@property (readonly) Class elementType;

+ (instancetype) arrayWithObjects:(const id *)objects
                            count:(NSInteger)count
                             type:(Class)elementType;

+ (instancetype) arrayWithLength:(NSInteger)length
                            type:(Class)elementType;

- (id) objectAtIndex:(NSInteger)index;

- (id) replaceObjectAtIndex:(NSInteger)index
                 withObject:(id)value;

- (void) OS_NORETURN throwInvalidItemTypeExceptionWithType:(Class) itemType;

- (void) getObjects:(NSObject **)buffer
             length:(NSInteger)length;

@end


#endif /* ARGCArray_h */
