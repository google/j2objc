//
//  ARC+GC.h
//
//  Created by DAE HOON JI on 19/07/2017.
//  Copyright © 2017 DAE HOON JI. All rights reserved.
//

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
    
    /* replace filed with newValue and returns old value. */
    void ARGC_assignARGCObject(ARGC_FIELD_REF id* pField, ARGCObject* newValue);
    
    /* replace filed with newValue and returns old value. */
    void ARGC_assignGenericObject(ARGC_FIELD_REF id* pField, id newValue);
    
    /* execute garbage collection. */
    void ARGC_collectGarbage();
    
    /* wake garbage collection thread. */
    void ARGC_requestGC();
    
    /* allocation object. */
    id ARGC_allocateObject(Class cls, NSUInteger extraBytes, NSZone* zone);

    /* clone object. */
    id ARGC_cloneObject(id obj);
    
    
#define ARGC_FIELD(type, name)
#define ARGC_PROXY_FIELD(type, name)
#define ARGC_SYNTHESIZE(type, name)
#define ARGC_PROXY_SYNTHESIZE(type, name)
    
#ifdef __cplusplus
};
#endif

