/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

#ifndef __ARGC_H__
#define __ARGC_H__

#import <Foundation/Foundation.h>
#import "J2ObjC_types.h"
#import "IOSMetadata.h"

CF_EXTERN_C_BEGIN

@class IOSClass;

@interface ARGCObject : NSObject {    
}
@end

        
@interface ARGCArray : NSObject <NSCopying> {
@public
  const NSInteger length_;
}

@property (readonly) NSInteger length;

- (void) OS_NORETURN throwInvalidIndexExceptionWithIndex:(NSInteger) index withLength: (NSInteger)length;

@end
    
/* replace field with newValue that extends ARGCObject. */
void ARGC_assignARGCObject(ARGC_FIELD_REF id* pField, __unsafe_unretained id newValue) J2OBJC_METHOD_ATTR;

/* replace field with newValue that maybe extends ARGCObject. */
void ARGC_assignGenericObject(ARGC_FIELD_REF id* pField, __unsafe_unretained id newValue) J2OBJC_METHOD_ATTR;

/* replace field with newValue that is static or not extends ARGCObject. */
void ARGC_assignStrongObject(__strong id* pField, __unsafe_unretained id newValue) J2OBJC_METHOD_ATTR;


/* execute garbage collection now. */
void ARGC_collectGarbage(bool clearSoftRef);

/* set background garbage collection interval. (default: 1000ms) */
void ARGC_setGarbageCollectionInterval(int time_in_ms);

    
/* wake garbage collection thread. */
void ARGC_requestGC();

id ARGC_allocateArray(Class cls, NSUInteger extraBytes, NSZone* zone) NS_RETURNS_RETAINED J2OBJC_METHOD_ATTR;

void ARGC_initStatic(Class cls) NS_RETURNS_RETAINED J2OBJC_METHOD_ATTR;

#define ARGC_FIELD(type, name)
#define ARGC_PROXY_FIELD(type, name)
#define ARGC_SYNTHESIZE(type, name)
#define ARGC_PROXY_SYNTHESIZE(type, name)

CF_EXTERN_C_END

#endif // __ARGC_H__
