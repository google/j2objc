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
static NSZone* _gcZone;
+ (void) initialize
{
    _gcZone = NSCreateZone(8 * _1M, _1M, true);
}


+ (instancetype)alloc
{
    instancetype obj = [NSObject allocWithZone: _gcZone];
    return obj;
}

- (void)dealloc
{
    NSDeallocateObject(self);
}

- (instancetype)retain
{
    instancetype self = [super retain];
    if ([self retainCount] > 1) {
        
    }
    return self;
}
- (oneway void)release
{
}

- (instancetype)autorelease
{
    
}
@end
