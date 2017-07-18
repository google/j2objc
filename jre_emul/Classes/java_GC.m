//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//
//  NSObject+JavaObject.m
//  JreEmulation
//
//  Created by DaeHoon Zee 17 Jul 2017.
//

#import "NSObject+JavaObject.h"

#import "IOSClass.h"
#import "J2ObjC_source.h"
#import "java/lang/ClassCastException.h"
#import "java/lang/CloneNotSupportedException.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/IllegalMonitorStateException.h"
#import "java/lang/InternalError.h"
#import "java/lang/InterruptedException.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/Thread.h"
#import "objc-sync.h"
#import <Foundation/Foundation.h>

@implementation java_Object (JavaObject)
static const int _1M = 1024*1024;
NSMutableArray<java_Object*>* _globalRoots;
NSMutableArray<java_Object*>* _weakObjects;

static NSZone* _gcZone;


+ (void) initialize
{
    _gcZone = NSCreateZone(8 * _1M, _1M, true);
    _globalRoots = [[NSMutableArray alloc] init];
}


+ (instancetype)alloc
{
    id obj = [NSObject allocWithZone: _gcZone];
    [obj autorelease];
    [_weakObjects addObject:obj];
    return obj;
}

- (void)dealloc
{
    NSDeallocateObject(self);
}

- (instancetype)retain
{
    self = [super retain];
    if ([self retainCount] == 2) {
        [_globalRoots addObject:self];
    }
    return self;
}

- (oneway void)release
{
    [super release];
}

- (instancetype)autorelease
{
    return [super autorelease];
}


- (id)java_clone {
    if (![NSCopying_class_() isInstance:self]) {
        @throw AUTORELEASE([[JavaLangCloneNotSupportedException alloc] init]);
    }
    
    // Use the Java getClass method because it returns the class we want in case
    // self's class hass been swizzled by a WeakReference or RetainedWith field.
    Class cls = [self java_getClass].objcClass;
    size_t instanceSize = class_getInstanceSize(cls);
    // We don't want to copy the NSObject portion of the object, in particular the
    // isa pointer, because it may contain the retain count.
    size_t nsObjectSize = class_getInstanceSize([NSObject class]);
    
    // Deliberately not calling "init" on the cloned object. To match Java's
    // behavior we simply copy the data. However we must additionally retain all
    // fields with object type.
    id clone = AUTORELEASE([cls alloc]);
    memcpy((char *)clone + nsObjectSize, (char *)self + nsObjectSize, instanceSize - nsObjectSize);
    
    // Reflectively examine all the fields for the object's type and retain any
    // object fields.
    while (cls && cls != [NSObject class]) {
        unsigned int ivarCount;
        Ivar *ivars = class_copyIvarList(cls, &ivarCount);
        for (unsigned int i = 0; i < ivarCount; i++) {
            Ivar ivar = ivars[i];
            const char *ivarType = ivar_getTypeEncoding(ivar);
            if (*ivarType == '@') {
                ptrdiff_t offset = ivar_getOffset(ivar);
                id field = *(id *)((char *)clone + offset);
                [field retain];
            }
        }
        free(ivars);
        cls = class_getSuperclass(cls);
    }
    
    // Releases any @Weak fields that shouldn't have been retained.
    [clone __javaClone:self];
    return clone;
}

@end
