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
//  IOSReference.m
//  JreEmulation
//
//  Created by Tom Ball on 8/15/13.
//

#import "IOSReference.h"

#import "IOSClass.h"
#import "J2ObjC_source.h"
#import "java/lang/ref/PhantomReference.h"
#import "java/lang/ref/Reference.h"
#import "java/lang/ref/SoftReference.h"

#import <objc/runtime.h>
#import <pthread.h>

#if TARGET_OS_MAC == 0
#import <UIKit/UIApplication.h>
#define SUPPORTS_SOFT_REFERENCES 1
#endif

#if __has_feature(objc_arc)
#error "IOSReference is not built with ARC"
#endif

// This class implements a variation on the design described in
// Mike Ash's "Zeroing Weak References in Objective-C" blog entry.
// Thanks, Mike, for your excellent article!
//
// http://mikeash.com/pyblog/friday-qa-2010-07-16-zeroing-weak-references-in-objective-c.html
//
// This design creates a subclass of a referent's class (one per
// referent type), then replaces the referent's class with it.
// These referent subclasses have a dealloc method that zeroes
// the reference's referent field, like iOS does for __weak
// references. They also have a release method that posts refs
// to their associated queues when they don't have other
// references.
//
// Note: only iOS provides low-memory notification support. Since
// the primary purpose of soft references is to support caching,
// and since iOS is the J2ObjC's primary target platform, on OS X
// soft references are never released. This shouldn't be an issue
// in practice, due to OS X virtual memory and the infrequent use
// of large caches in client code. The alternative of throwing an
// UnsupportedOperationException seems less useful.

@interface NSObject (JavaLangRefReferenceSwizzled)
- (void)JavaLangRefReference_original_dealloc;
- (void)JavaLangRefReference_original_release;
@end


@implementation IOSReference

static void AssociateReferenceWithReferent(id referent, JavaLangRefReference *reference);
static void EnsureReferentSubclass(id referent);
static void RemoveReferenceAssociation(id referent, JavaLangRefReference *reference);
static void RealReferentRelease(id referent);
static void ReferentSubclassDealloc(id self, SEL _cmd);
static void ReferentSubclassRelease(id self, SEL _cmd);
static IOSClass *ReferentSubclassGetClass(id self, SEL _cmd);
static void WhileLocked(void (^block)(void));
static CFMutableSetRef CreateReferentSet(void);

// Global recursive mutux.
static pthread_mutex_t reference_mutex;

// Maps referents to sets of Reference instances that refer to them.
static CFMutableDictionaryRef weak_refs_map;

// Maps referent classes to their referent subclasses.
static CFMutableDictionaryRef referent_subclass_map;

// Set of all referent subclasses.
static CFMutableSetRef referent_subclasses;

// Set of all soft ref queue candidates. These are only released when
// the runtime is notified of a low memory condition.
static CFMutableSetRef soft_references;

static jboolean in_low_memory_cleanup;

+ (void)initReferent:(JavaLangRefReference *)reference {
  WhileLocked(^{
    id referent = JreLoadVolatileId(&reference->referent_);
    if (referent) {
      EnsureReferentSubclass(referent);
      AssociateReferenceWithReferent(referent, reference);
    }
  });
}

+ (id)getReferent:(JavaLangRefReference *)reference {
  // The referent must be loaded under mutex to avoid a race with another
  // thread that might be releasing the referent. We can't rely only on the
  // volatile synchronization because it is a @Weak volatile, but WhileLocked
  // will synchronize with the release implementation in the referent subclass.
  __block id referent;
  WhileLocked(^{
    // The volatile load ensures that the result is retained in this thread.
    referent = JreLoadVolatileId(&reference->referent_);
  });
  return referent;
}

+ (void)clearReferent:(JavaLangRefReference *)reference {
  WhileLocked(^{
    id referent = JreLoadVolatileId(&reference->referent_);
    if (referent) {
      RemoveReferenceAssociation(referent, reference);
    }
    JreAssignVolatileId(&reference->referent_, nil);
  });
}

+ (void)handleMemoryWarning:(NSNotification *)notification {
  WhileLocked(^{
    in_low_memory_cleanup = true;
    CFRelease(soft_references);
    soft_references = CreateReferentSet();
    in_low_memory_cleanup = false;
  });
}

+ (void)initialize {
  if (self == [IOSReference class]) {
    pthread_mutexattr_t mutexattr;
    pthread_mutexattr_init(&mutexattr);
    pthread_mutexattr_settype(&mutexattr, PTHREAD_MUTEX_RECURSIVE);
    pthread_mutex_init(&reference_mutex, &mutexattr);
    pthread_mutexattr_destroy(&mutexattr);

    weak_refs_map = CFDictionaryCreateMutable(NULL, 0, NULL, &kCFTypeDictionaryValueCallBacks);
    referent_subclasses = CreateReferentSet();
    referent_subclass_map =
        CFDictionaryCreateMutable(NULL, 0, NULL, &kCFTypeDictionaryValueCallBacks);
    soft_references = CreateReferentSet();

#ifdef SUPPORTS_SOFT_REFERENCES
    // Register for iOS low memory notifications, to clear pending soft references.
    [[NSNotificationCenter defaultCenter]
        addObserver:self
           selector:@selector(handleMemoryWarning:)
               name: UIApplicationDidReceiveMemoryWarningNotification
             object:nil];
#endif
  }
}

static CFMutableSetRef CreateReferentSet() {
  CFSetCallBacks referenceSetCallBacks = kCFTypeSetCallBacks;
  // Set equal callback to NULL to use pointer equality.
  referenceSetCallBacks.equal = NULL;
  // Set hash callback to NULL to compute hash codes by converting pointers to integers
  referenceSetCallBacks.hash = NULL;
  return CFSetCreateMutable(NULL, 0, &referenceSetCallBacks);
}

static void WhileLocked(void (^block)(void)) {
  pthread_mutex_lock(&reference_mutex);
  block();
  pthread_mutex_unlock(&reference_mutex);
}


// Returns the referent subclass for a referent, or nil if one hasn't
// been created for that type. Caller must hold the mutex.
static Class GetReferentSubclass(id obj) {
  Class cls = object_getClass(obj);
  while (cls && !CFSetContainsValue(referent_subclasses, cls)) {
    cls = class_getSuperclass(cls);
  }
  return cls;
}


// Returns true if an object is constant. The retain and release methods
// don't modify retain counts when they are INT_MAX (or UINT_MAX on some
// architectures), so the retainCount test in ReferentSubclassRelease
// won't work. The only constants in translated code are string constants
// and classes; since constants as reference referents do nothing in Java
// (since they are never GC'd), with this test they will do nothing in
// iOS as well.
static jboolean IsConstantObject(id obj) {
  if ([obj isKindOfClass:[IOSClass class]]) {
    return true;
  }
  NSUInteger retainCount = [obj retainCount];
  return retainCount == UINT_MAX || retainCount == INT_MAX;
}


// Create a custom subclass for specified referent class.
static Class CreateReferentSubclass(Class cls) {
  NSString *newName = [NSString stringWithFormat: @"%s_ReferentSubclass", class_getName(cls)];
  Class subclass = objc_allocateClassPair(cls, [newName UTF8String], 0);
  Method dealloc = class_getInstanceMethod(cls, @selector(dealloc));
  class_addMethod(subclass, @selector(dealloc), (IMP) ReferentSubclassDealloc,
                  method_getTypeEncoding(dealloc));
  Method release = class_getInstanceMethod(cls, @selector(release));
  class_addMethod(subclass, @selector(release), (IMP) ReferentSubclassRelease,
                  method_getTypeEncoding(release));
  Method getClass = class_getInstanceMethod(cls, @selector(java_getClass));
  class_addMethod(subclass, @selector(java_getClass), (IMP) ReferentSubclassGetClass,
                  method_getTypeEncoding(getClass));
  objc_registerClassPair(subclass);
  return subclass;
}


// Checks whether a referent subclass exists, and creates one if
// it doesn't. The exception is for constants, which are never
// dealloced. Caller must hold the mutex.
static void EnsureReferentSubclass(id referent) {
  if (!GetReferentSubclass(referent) && !IsConstantObject(referent)) {
    Class cls = object_getClass(referent);
    Class subclass = (Class)CFDictionaryGetValue(referent_subclass_map, cls);
    if (!subclass) {
      subclass = CreateReferentSubclass(cls);
      CFDictionaryAddValue(referent_subclass_map, cls, subclass);
      CFSetAddValue(referent_subclasses, subclass);
    }
    if (class_getSuperclass(subclass) == cls) {
      object_setClass(referent, subclass);
    }
  }
}


// Returns the referent's original class. Caller must hold the mutex.
static Class GetRealSuperclass(id obj) {
  return class_getSuperclass(GetReferentSubclass(obj));
}


// Add an association between a referent and its reference. Because
// multiple references can share a referent, a reference set is used.
// Caller must hold the mutex.
static void AssociateReferenceWithReferent(id referent, JavaLangRefReference *reference) {
  CFMutableSetRef set = (CFMutableSetRef)CFDictionaryGetValue(weak_refs_map, referent);
  if (!set) {
    set = CFSetCreateMutable(NULL, 0, NULL);
    CFDictionarySetValue(weak_refs_map, referent, set);
    CFRelease(set);
  }
  CFSetAddValue(set, reference);
}


// Check if there is a SoftReference among a referent's references. Caller must
// hold the mutex.
static bool hasSoftReference(CFMutableSetRef set) {
  // CFSet doesn't have an interruptible iterator function.
  NSSet *setCopy = (ARCBRIDGE NSSet *) set;
  for (JavaLangRefReference *reference in setCopy) {
    if ([reference isKindOfClass:[JavaLangRefSoftReference class]]) {
      return true;
    }
  }
  return false;
}


// Remove the association between a referent and all of its references. Caller
// must hold the mutex
static void RemoveReferenceAssociation(id referent, JavaLangRefReference *reference) {
  CFMutableSetRef set = (CFMutableSetRef)CFDictionaryGetValue(weak_refs_map, referent);
  if (set) {
    CFSetRemoveValue(set, reference);
    if ([reference isKindOfClass:[JavaLangRefSoftReference class]] && !hasSoftReference(set)) {
      CFSetRemoveValue(soft_references, referent);
    }
  }
}


// Invoke a referent subclass's original release method.
static void RealReferentDealloc(id referent) {
  Class superclass = GetRealSuperclass(referent);
  IMP superDealloc = class_getMethodImplementation(superclass, @selector(dealloc));
  ((void (*)(id, SEL))superDealloc)(referent, @selector(dealloc));
}


typedef struct PhantomRefsContext {
  int num;
  id *refs;
} PhantomRefsContext;

static void ClearAndMaybeQueueReference(const void *value, void *context) {
  JavaLangRefReference *reference = (JavaLangRefReference *)value;
  PhantomRefsContext *ctx = (PhantomRefsContext *)context;
  // Clear the referent field.
  JreAssignVolatileId(&reference->referent_, nil);
  // Queue the reference unless it is a phantom.
  if ([reference isKindOfClass:[JavaLangRefPhantomReference class]]) {
    ctx->refs[ctx->num++] = reference;
  } else {
    [reference enqueue];
  }
}

// Dealloc method for referent subclasses, which directly calls the
// original class's dealloc method. Normally "[super dealloc]" isn't
// permissible with ARC, but in this case it's actually invoking a
// delegate object's dealloc method (which won't invoke super-dealloc).
static void ReferentSubclassDealloc(id self, SEL _cmd) {
  WhileLocked(^{
    CFMutableSetRef set = (CFMutableSetRef)CFDictionaryGetValue(weak_refs_map, self);
    if (set) {
      int count = (int)CFSetGetCount(set);
      CFMutableSetRef setCopy = CFSetCreateMutableCopy(NULL, count, set);
      PhantomRefsContext phantomRefsCtx;
      phantomRefsCtx.num = 0;
      id phantomRefs[count];
      phantomRefsCtx.refs = phantomRefs;
      CFSetApplyFunction(setCopy, ClearAndMaybeQueueReference, &phantomRefsCtx);

      // Real dealloc.
      RealReferentDealloc(self);
      // Remove reference associations.
      CFDictionaryRemoveValue(weak_refs_map, self);

      // Queue all phantom references.
      for (int i = 0; i < phantomRefsCtx.num; i++) {
        [phantomRefsCtx.refs[i] enqueue];
      }
      CFRelease(setCopy);
    }
  });
}


// Invoke a referent subclass's original release method.
static void RealReferentRelease(id referent) {
  Class superclass = GetRealSuperclass(referent);
  IMP superRelease = class_getMethodImplementation(superclass, @selector(release));
  ((void (*)(id, SEL))superRelease)(referent, @selector(release));
}


// Release method for referent subclasses, which directly calls the
// original class's release method. If the instance would be
// deallocated when this function returns, it is added to its
// associated reference queue, if any.
static void ReferentSubclassRelease(id self, SEL _cmd) {
  WhileLocked(^{
    if ([self retainCount] == 1 && !in_low_memory_cleanup) {
      CFMutableSetRef set = (CFMutableSetRef)CFDictionaryGetValue(weak_refs_map, self);
      if (set && hasSoftReference(set)) {
        // referent is softly reachable. Save it from deallocation.
        CFSetAddValue(soft_references, self);
      }
    }
    RealReferentRelease(self);
  });
}

// Override getClass in the subclass so that it returns the IOSClass for the
// original class of the referent.
static IOSClass *ReferentSubclassGetClass(id self, SEL _cmd) {
  __block Class realSuperclass;
  WhileLocked(^{
    realSuperclass = GetRealSuperclass(self);
  });
  return IOSClass_fromClass(realSuperclass);
}

@end
