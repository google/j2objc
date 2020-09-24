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
//  JreRetainedWith.h
//  JreEmulation
//
//  Created by Keith Stanger on Mar. 18, 2016.
//

#if __has_feature(objc_arc)
#error "JreRetainedWith cannot be built with ARC"
#endif

#include "FastPointerLookup.h"
#include "J2ObjC_source.h"
#include "java/lang/AssertionError.h"

// Associate the return reference so that it can be artificially weakened when
// the child's retain count is 1.
static char returnRefKey;
// Associate the child's original class for "super" calls in the swizzled
// methods.
static char superClsKey;

static id RetainedWithRetain(id self, SEL _cmd) {
  @synchronized (self) {
    if ([self retainCount] == 1) {
      [objc_getAssociatedObject(self, &returnRefKey) retain];
    }
    Class superCls = objc_getAssociatedObject(self, &superClsKey);
    IMP superRetain = class_getMethodImplementation(superCls, @selector(retain));
    return ((id (*)(id, SEL))superRetain)(self, @selector(retain));
  }
}

static void RetainedWithRelease(id self, SEL _cmd) {
  @synchronized (self) {
    if ([self retainCount] == 2) {
      [objc_getAssociatedObject(self, &returnRefKey) autorelease];
    }
    Class superCls = objc_getAssociatedObject(self, &superClsKey);
    IMP superRelease = class_getMethodImplementation(superCls, @selector(release));
    ((id (*)(id, SEL))superRelease)(self, @selector(release));
  }
}

static IOSClass *RetainedWithGetClass(id self, SEL _cmd) {
  Class superCls = objc_getAssociatedObject(self, &superClsKey);
  return IOSClass_fromClass(superCls);
}

// Creates a subclass with swizzled retain, release, and getClass methods.
static void *CreateSubclass(void *clsPtr) {
  Class cls = (Class)clsPtr;
  NSString *newName = [NSString stringWithFormat:@"%s_JreRetainedWith", class_getName(cls)];
  Class subclass = objc_allocateClassPair(cls, [newName UTF8String], 0);
  Method retain = class_getInstanceMethod(cls, @selector(retain));
  class_addMethod(subclass, @selector(retain), (IMP)RetainedWithRetain,
                  method_getTypeEncoding(retain));
  Method release = class_getInstanceMethod(cls, @selector(release));
  class_addMethod(subclass, @selector(release), (IMP)RetainedWithRelease,
                  method_getTypeEncoding(release));
  Method getClass = class_getInstanceMethod(cls, @selector(java_getClass));
  class_addMethod(subclass, @selector(java_getClass), (IMP)RetainedWithGetClass,
                  method_getTypeEncoding(getClass));
  objc_registerClassPair(subclass);
  return subclass;
}

static FastPointerLookup_t subclassLookup = FAST_POINTER_LOOKUP_INIT(&CreateSubclass);

// Swizzle the class of the child and make necessary associations.
static void ApplyRetainedWithSubclass(id parent, id child) {
  Class cls = object_getClass(child);
  Class subclass = (Class)FastPointerLookup(&subclassLookup, cls);
  objc_setAssociatedObject(child, &returnRefKey, parent, OBJC_ASSOCIATION_ASSIGN);
  objc_setAssociatedObject(child, &superClsKey, cls, OBJC_ASSOCIATION_ASSIGN);
  object_setClass(child, subclass);
}

// Counts how many times the child refers back to the parent.
static NSUInteger CountReturnRefs(id parent, id child) {
  NSUInteger returnRefs = 0;
  Class cls = object_getClass(child);
  while (cls && cls != [NSObject class]) {
    unsigned int ivarCount;
    Ivar *ivars = class_copyIvarList(cls, &ivarCount);
    for (unsigned int i = 0; i < ivarCount; i++) {
      Ivar ivar = ivars[i];
      const char *ivarType = ivar_getTypeEncoding(ivar);
      if (*ivarType == '@') {
        ptrdiff_t offset = ivar_getOffset(ivar);
        if (*(id *)((uintptr_t)child + offset) == parent) {
          returnRefs++;
        };
      }
    }
    free(ivars);
    cls = class_getSuperclass(cls);
  }
  return returnRefs;
}

// Called upon destruction of the parent. We must set all return refs to nil to
// avoid calling release on a dealloc'ed object from child's dealloc method.
void JreRetainedWithHandleDealloc(id parent, id child) {
  Class cls = object_getClass(child);
  while (cls && cls != [NSObject class]) {
    unsigned int ivarCount;
    Ivar *ivars = class_copyIvarList(cls, &ivarCount);
    for (unsigned int i = 0; i < ivarCount; i++) {
      Ivar ivar = ivars[i];
      const char *ivarType = ivar_getTypeEncoding(ivar);
      if (*ivarType == '@') {
        ptrdiff_t offset = ivar_getOffset(ivar);
        id *objField = (id *)((uintptr_t)child + offset);
        if (*objField == parent) {
          *objField = nil;
        };
      }
    }
    free(ivars);
    cls = class_getSuperclass(cls);
  }
}

// Requires that value has a retain count of at least 2 so that its return
// reference can remain strong to start.
void JreRetainedWithInitialize(id parent, id value) {
  NSUInteger returnRefs = CountReturnRefs(parent, value);
  if (returnRefs > 0) {
    // Make all but one of the return refs weak.
    while (returnRefs-- > 1) {
      [parent release];
    }
    ApplyRetainedWithSubclass(parent, value);
  }
}

// Handles the existing child value during reassignment of a @RetainedWith field. If the parent has
// been cloned then the existing child will point back to a different parent and no change is
// necessary. Otherwise, we return the existing child to normal behavior by fixing the retain count
// of the return ref and then setting its return ref to nil. The child maintains its swizzled class.
void JreRetainedWithHandlePreviousValue(id parent, id value) {
  id returnRef = objc_getAssociatedObject(value, &returnRefKey);
  if (returnRef == parent) {
    @synchronized (value) {
      // Strengthen all return refs that had been weakened upon assignment of the value.
      NSUInteger returnRefs = CountReturnRefs(parent, value);
      // If the retain count is greater than one then one of the return refs is already strong.
      if ([value retainCount] > 1) {
        returnRefs--;
      }
      while (returnRefs-- > 0) {
        [parent retain];
      }
      // Assigning the return ref to nil will return this value to normal behavior.
      // JreRetainedWithHandleDealloc will not get called for this value because the parent now
      // points to a different child.
      objc_setAssociatedObject(value, &returnRefKey, nil, OBJC_ASSOCIATION_ASSIGN);
    }
  }
}
