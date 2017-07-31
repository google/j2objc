//
//  ARC+GC.mm
//
//  Created by DAE HOON JI on 19/07/2017.
//  Copyright © 2017 DAE HOON JI. All rights reserved.
//

#import "ARGC.h"
#import "ARGCObjectArray.h"
#import "ARGCArrayUtil.h"

#import "objc/runtime.h"
#import <atomic>


@implementation ARGCObjectArray

@synthesize elementType = elementType_;

static inline void ARGC_checkItemType(ARGCObjectArray* array, id value) {
    if (value && ![value isKindOfClass:array->elementType_]) {
        [array throwInvalidItemTypeExceptionWithType:value];
    }
}

static ARGCObjectArray* _createArray(NSInteger length, Class type, const id* objects) {
    if (length < 0) {
        return NULL;
    }
    ARGCObjectArray *array = NSAllocateObject([ARGCObjectArray class], length * sizeof(id), nil);
    *(NSInteger*)&array->length_ = length;
    *(Class*)&array->elementType_ = type;
    if (objects) {
        for (int i = 0; i < length; i++) {
            array->buffer_[i] = [objects[i] retain];
        }
    }
    return array;
}

+ (instancetype)arrayWithLength:(NSInteger)length type:(Class) type {
    return _createArray(length, type, NULL);
}

+ (instancetype)arrayWithObjects:(const id *)objects
                           count:(NSInteger)count
                            type:(Class)elementType {
    return _createArray(count, elementType, objects);
}

+ (instancetype)arrayWithArray:(ARGCObjectArray *)array {
    return _createArray(array->length_, array->elementType_, array->buffer_);
}

+ (instancetype)arrayWithNSArray:(NSArray *)src type:(Class) type {
    NSUInteger cntItem = [src count];
    ARGCObjectArray* array = _createArray(cntItem, type, NULL);
    [src getObjects:array->buffer_ range:NSMakeRange(0, cntItem)];
    return array;
}

- (id)objectAtIndex:(NSInteger)index {
    ARGC_checkArrayIndex(self, index);
    return buffer_[index];
}

- (void) throwInvalidItemTypeExceptionWithType:(Class)itemType
{
    NSString *msg = [NSString stringWithFormat:
                     @"Array item type %@ mismatched to %@",
                     itemType, (id)self->elementType_];
    @throw [[NSException alloc]initWithName:@"ArrayStoreException" reason:msg userInfo:NULL];
}


- (id)replaceObjectAtIndex:(NSInteger)index withObject:(id)value {
    ARGC_checkArrayIndex(self, index);
    ARGC_checkItemType(self, value);
    id oldValue = buffer_[index];
    ARGC_assignGenericObject(buffer_ + index, value);
    return oldValue;
}

- (void)getObjects:(NSObject**) buffer length:(NSInteger)length {
    ARGC_checkArrayIndex(self, length - 1);
    for (NSInteger i = 0; i < length; i++) {
        id element = buffer_[i];
        buffer[i] = element;
    }
}


- (void)arraycopy:(NSInteger)srcOffset
      destination:(ARGCObjectArray*)dstArray
        dstOffset:(NSInteger) dstOffset
           length:(NSInteger) length
{

    ARGC_checkArrayRange(self, srcOffset, length);
    ARGC_checkArrayRange(dstArray, dstOffset, length);
    if (length == 0) {
        return;
    }
    
    id* dst = dstArray->buffer_ + dstOffset;
    id* src = buffer_ + srcOffset;
    if (self == dstArray) {
        if (srcOffset == dstOffset) {
            return;
        }
        for (NSInteger i = length; --i >= 0; dst++, src++) {
            ARGC_assignGenericObject(dst, *src);
        }
        return;
    }
    
    BOOL isAssignableType = dstArray->elementType_ == self->elementType_
        || [self->elementType_ isSubclassOfClass:dstArray->elementType_];
    
    if (isAssignableType) {
        for (NSInteger i = length; --i >= 0; dst++, src++) {
            ARGC_assignGenericObject(dst, *src);
        }
    }
    else {
        for (NSInteger i = length; --i >= 0; dst++, src++) {
            id item = *src;
            ARGC_checkItemType(dstArray, item);
            ARGC_assignGenericObject(dst, item);
        }
    }
}

- (id) copyWithZone:(NSZone*) zone {
    ARGCObjectArray *result = _createArray(self->length_, elementType_, self->buffer_);
    return result;
}

- (NSUInteger) countByEnumeratingWithState:(NSFastEnumerationState*) state
                                   objects:(__unsafe_unretained id*) stackbuf
                                     count:(NSUInteger) len
{
    if (state->state == 0) {
        state->mutationsPtr = (unsigned long *) (__bridge void *) self;
        state->itemsPtr = (__unsafe_unretained id *) (void *) buffer_;
        state->state = 1;
        return self->length_;
    } else {
        return 0;
    }
}

- (void*) buffer {
    return buffer_;
}

@end
