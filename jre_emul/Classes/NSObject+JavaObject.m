// Copyright 2011 Google Inc. All Rights Reserved.
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
//  Created by Tom Ball on 8/15/11.
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

// A category that adds Java Object-compatible methods to NSObject.
@implementation NSObject (JavaObject)

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

- (IOSClass *)java_getClass {
  return IOSClass_fromClass([self class]);
}

- (int)compareToWithId:(id)other {
#if __has_feature(objc_arc)
  @throw [[JavaLangClassCastException alloc] init];
#else
  @throw [[[JavaLangClassCastException alloc] init] autorelease];
#endif
  return 0;
}

- (void)java_notify {
  int result = objc_sync_notify(self);
  if (result == OBJC_SYNC_SUCCESS) {  // Test most likely outcome first.
    return;
  }
  if (result == OBJC_SYNC_NOT_OWNING_THREAD_ERROR) {
    @throw AUTORELEASE([[JavaLangIllegalMonitorStateException alloc] init]);
  } else {
    NSString *msg = [NSString stringWithFormat:@"system error %d", result];
    @throw AUTORELEASE([[JavaLangInternalError alloc] initWithNSString:msg]);
  }
}

- (void)java_notifyAll {
  int result = objc_sync_notifyAll(self);
  if (result == OBJC_SYNC_SUCCESS) {  // Test most likely outcome first.
    return;
  }
  if (result == OBJC_SYNC_NOT_OWNING_THREAD_ERROR) {
    @throw AUTORELEASE([[JavaLangIllegalMonitorStateException alloc] init]);
  } else {
    NSString *msg = [NSString stringWithFormat:@"system error %d", result];
    @throw AUTORELEASE([[JavaLangInternalError alloc] initWithNSString:msg]);
  }
}

static void doWait(id obj, long long timeout) {
  if (timeout < 0) {
    @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] init]);
  }
  JavaLangThread *javaThread = JavaLangThread_currentThread();
  int result = OBJC_SYNC_SUCCESS;
  if (!javaThread->interrupted_) {
    assert(javaThread->blocker_ == nil);
    javaThread->blocker_ = obj;
    result = objc_sync_wait(obj, timeout);
    javaThread->blocker_ = nil;
  }
  jboolean wasInterrupted = javaThread->interrupted_;
  javaThread->interrupted_ = false;
  if (wasInterrupted) {
    @throw AUTORELEASE([[JavaLangInterruptedException alloc] init]);
  }
  if (result == OBJC_SYNC_SUCCESS) {
    return;  // success
  }
  if (result == OBJC_SYNC_TIMED_OUT) {
    return;
  }
  if (result == OBJC_SYNC_NOT_OWNING_THREAD_ERROR) {
    @throw AUTORELEASE([[JavaLangIllegalMonitorStateException alloc] init]);
  } else {
    NSString *msg = [NSString stringWithFormat:@"system error %d", result];
    @throw AUTORELEASE([[JavaLangInternalError alloc] initWithNSString:msg]);
  }
}

- (void)java_wait {
  doWait(self, 0LL);
}

- (void)java_waitWithLong:(long long)timeout {
  doWait(self, timeout);
}

- (void)java_waitWithLong:(long long)timeout withInt:(int)nanos {
  if (nanos < 0) {
    @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] init]);
  }
  doWait(self, timeout + (nanos == 0 ? 0 : 1));
}

- (void)java_finalize {
}

- (void)__javaClone:(id)original {
}

+ (const J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { NULL, NULL, 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "LIOSClass;", 0x11, 0, -1, -1, 1, -1, -1 },
    { NULL, "I", 0x1, 2, -1, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, 3, 4, -1, -1, -1, -1 },
    { NULL, "LNSObject;", 0x4, 5, -1, 6, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, 7, -1, -1, -1, -1, -1 },
    { NULL, "V", 0x4, 8, -1, 9, -1, -1, -1 },
    { NULL, "V", 0x11, 10, -1, -1, -1, -1, -1 },
    { NULL, "V", 0x11, 11, -1, -1, -1, -1, -1 },
    { NULL, "V", 0x11, 12, 13, 14, -1, -1, -1 },
    { NULL, "V", 0x11, 12, 15, 14, -1, -1, -1 },
    { NULL, "V", 0x11, 12, -1, 14, -1, -1, -1 },
  };
  #pragma clang diagnostic push
  #pragma clang diagnostic ignored "-Wobjc-multiple-method-names"
  methods[0].selector = @selector(init);
  methods[1].selector = @selector(java_getClass);
  methods[2].selector = @selector(hash);
  methods[3].selector = @selector(isEqual:);
  methods[4].selector = @selector(java_clone);
  methods[5].selector = @selector(description);
  methods[6].selector = @selector(java_finalize);
  methods[7].selector = @selector(java_notify);
  methods[8].selector = @selector(java_notifyAll);
  methods[9].selector = @selector(java_waitWithLong:);
  methods[10].selector = @selector(java_waitWithLong:withInt:);
  methods[11].selector = @selector(java_wait);
  #pragma clang diagnostic pop
  static const void *ptrTable[] = {
    "getClass", "()Ljava/lang/Class<*>;", "hashCode", "equals", "LNSObject;", "clone",
    "LJavaLangCloneNotSupportedException;", "toString", "finalize", "LJavaLangThrowable;", "notify",
    "notifyAll", "wait", "J", "LJavaLangInterruptedException;", "JI" };
  static const J2ObjcClassInfo _NSObject = {
    "Object", "java.lang", ptrTable, methods, NULL, 7, 0x1, 12, 0, -1, -1, -1, -1, -1 };
  return &_NSObject;
}

// Unimplemented private methods for java.lang.ref.Reference. The methods'
// implementations are set when swizzling the Reference's referent class.
- (void)_java_lang_ref_original_dealloc {}
- (void)_java_lang_ref_original_release {}

@end

@implementation JreObjectCategoryDummy
@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(NSObject)

J2OBJC_NAME_MAPPING(NSObject, "java.lang.Object", "NSObject")
