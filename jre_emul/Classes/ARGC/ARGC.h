//
//  ARC+GC.h
//
//  Created by DAE HOON JI on 19/07/2017.
//  Copyright © 2017 DAE HOON JI. All rights reserved.
//

#ifndef __ARGC_H__
#define __ARGC_H__

#import <Foundation/Foundation.h>

#ifdef __cplusplus
extern "C" {
#endif

@interface ARGCObject : NSObject
@end

    
@interface ARGCProxy : ARGCObject {
@public
    id obj;
}
@end
    
    
@interface ARGCArray : NSObject <NSCopying> {
@public
    const NSInteger length_;
}

@property (readonly) NSInteger length;

- (void) OS_NORETURN throwInvalidIndexExceptionWithIndex:(NSInteger) index withLength: (NSInteger)length;

@end

    
#define ARGC_FIELD_REF __unsafe_unretained

typedef void (*ARGCObjectFieldVisitor)(ARGC_FIELD_REF id obj, int depth);
    
/* replace filed with newValue and returns old value. */
void ARGC_assignARGCObject(ARGC_FIELD_REF id* pField, __unsafe_unretained ARGCObject* newValue);

/* replace filed with newValue and returns old value. */
void ARGC_assignGenericObject(ARGC_FIELD_REF id* pField, __unsafe_unretained id newValue);

/* execute garbage collection now. */
void ARGC_collectGarbage();

/* set background garbage collection interval. (default: 1000ms) */
void ARGC_setGarbageCollectionInterval(int time_in_ms);

    
/* wake garbage collection thread. */
void ARGC_requestGC();

/* wake garbage collection thread. */
id ARGC_allocateObject(Class cls, NSUInteger extraBytes, NSZone* zone) NS_RETURNS_RETAINED;

id ARGC_globalLock(__unsafe_unretained id obj) NS_RETURNS_RETAINED;
id ARGC_globalUnlock(__unsafe_unretained id obj) NS_RETURNS_RETAINED;
#define ARGC_FIELD(type, name)
#define ARGC_PROXY_FIELD(type, name)
#define ARGC_SYNTHESIZE(type, name)
#define ARGC_PROXY_SYNTHESIZE(type, name)
    
#ifdef __cplusplus
};
#endif

#endif // __ARGC_H__
