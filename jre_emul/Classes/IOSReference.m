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
static void WhileLocked(void (^block)(void));

// Global recursive mutux.
static pthread_mutex_t reference_mutex;

// Maps referents to sets of Reference instances that refer to them.
static CFMutableDictionaryRef weak_refs_map;

// Maps referent classes to their referent subclasses.
static NSMutableDictionary *referent_subclass_map;

// Set of all referent subclasses.
static NSMutableSet *referent_subclasses;

// Set of all soft ref queue candidates. These are only released when
// the runtime is notified of a low memory condition.
static NSMutableSet *soft_references;

static BOOL in_low_memory_cleanup;

+ (void)initReferent:(JavaLangRefReference *)reference {
  if (reference->referent_) {
    EnsureReferentSubclass(reference->referent_);
    AssociateReferenceWithReferent(reference->referent_, reference);
  }
}

+ (void)strengthenReferent:(JavaLangRefReference *)reference {
  [reference->referent_ retain];
}

+ (void)weakenReferent:(JavaLangRefReference *)reference {
  [reference->referent_ autorelease];
}

+ (void)removeAssociation:(JavaLangRefReference *)reference {
  if (reference->referent_) {
    RemoveReferenceAssociation(reference->referent_, reference);
  }
}

+ (void)handleMemoryWarning:(NSNotification *)notification {
  WhileLocked(^{
    in_low_memory_cleanup = YES;
    for (JavaLangRefSoftReference *reference in soft_references) {
      [reference->referent_ release];
    }
    in_low_memory_cleanup = NO;
  });
  [soft_references release];
  soft_references = [[NSMutableSet alloc] init];
}

+ (void)initialize {
  if (self == [IOSReference class]) {
    pthread_mutexattr_t mutexattr;
    pthread_mutexattr_init(&mutexattr);
    pthread_mutexattr_settype(&mutexattr, PTHREAD_MUTEX_RECURSIVE);
    pthread_mutex_init(&reference_mutex, &mutexattr);
    pthread_mutexattr_destroy(&mutexattr);

    weak_refs_map = CFDictionaryCreateMutable(NULL, 0, NULL, &kCFTypeDictionaryValueCallBacks);
    referent_subclasses = [[NSMutableSet alloc] init];
    referent_subclass_map = [[NSMutableDictionary alloc] init];
    soft_references = [[NSMutableSet alloc] init];

#if SUPPORTS_SOFT_REFERENCES
    // Register for iOS low memory notifications, to clear pending soft references.
    [[NSNotificationCenter defaultCenter]
        addObserver:self
           selector:@selector(handleMemoryWarning:)
               name: UIApplicationDidReceiveMemoryWarningNotification
             object:nil];
#endif
  }
}


static void WhileLocked(void (^block)(void)) {
  pthread_mutex_lock(&reference_mutex);
  block();
  pthread_mutex_unlock(&reference_mutex);
}


// Returns the referent subclass for a referent, or nil if one hasn't
// been created for that type.
static Class GetReferentSubclass(id obj) {
  Class class = object_getClass(obj);
  while (class && ![referent_subclasses containsObject:class])
    class = class_getSuperclass(class);
  return class;
}


// Returns YES if an object is constant. The retain and release methods
// don't modify retain counts when they are INT_MAX (or UINT_MAX on some
// architectures), so the retainCount test in ReferentSubclassRelease
// won't work. The only constants in translated code are string constants;
// since constant strings as reference referents does nothing in Java
// (since they are never GC'd), with this test they will do nothing in
// iOS as well.
static BOOL IsConstantObject(id obj) {
  NSUInteger retainCount = [obj retainCount];
  return retainCount == UINT_MAX || retainCount == INT_MAX;
}


// Create a custom subclass for specified referent class.
static Class CreateReferentSubclass(Class class) {
  NSString *newName = [NSString stringWithFormat: @"%s_ReferentSubclass", class_getName(class)];
  Class subclass = objc_allocateClassPair(class, [newName UTF8String], 0);
  Method dealloc = class_getInstanceMethod(class, @selector(dealloc));
  class_addMethod(subclass, @selector(dealloc), (IMP) ReferentSubclassDealloc,
                  method_getTypeEncoding(dealloc));
  Method release = class_getInstanceMethod(class, @selector(release));
  class_addMethod(subclass, @selector(release), (IMP) ReferentSubclassRelease,
                  method_getTypeEncoding(release));
  objc_registerClassPair(subclass);
  return subclass;
}


// Checks whether a referent subclass exists, and creates one if
// it doesn't. The exception is for constants, which are never
// dealloced.
static void EnsureReferentSubclass(id referent) {
  if (!GetReferentSubclass(referent) && !IsConstantObject(referent)) {
    Class class = object_getClass(referent);
    Class subclass = [referent_subclass_map objectForKey:class];
    if (!subclass) {
      subclass = CreateReferentSubclass(class);
      [referent_subclass_map setObject:subclass forKey:(id<NSCopying>) class];
      [referent_subclasses addObject:subclass];
    }
    if (class_getSuperclass(subclass) == class) {
      object_setClass(referent, subclass);
    }
  }
}


// Returns the referent's original class.
static Class GetRealSuperclass(id obj) {
  return class_getSuperclass(GetReferentSubclass(obj));
}


// Add an association between a referent and its reference. Because
// multiple references can share a referent, a reference set is used.
static void AssociateReferenceWithReferent(id referent, JavaLangRefReference *reference) {
  WhileLocked(^{
    CFMutableSetRef set = (void *)CFDictionaryGetValue(weak_refs_map, referent);
    if (!set) {
      set = CFSetCreateMutable(NULL, 0, NULL);
      CFDictionarySetValue(weak_refs_map, referent, set);
      CFRelease(set);
    }
    CFSetAddValue(set, reference);
  });
}


// Remove the association between a referent and all of its references.
static void RemoveReferenceAssociation(id referent, JavaLangRefReference *reference) {
  WhileLocked(^{
    CFMutableSetRef set = (void *)CFDictionaryGetValue(weak_refs_map, referent);
    CFSetRemoveValue(set, reference);
  });
}


// A referent is being dealloc'd, so remove all references to it from
// the weak refs map.
static BOOL RemoveAllReferenceAssociations(id referent) {
  BOOL enqueued = NO;
  CFMutableSetRef set = (void *) CFDictionaryGetValue(weak_refs_map, referent);
  if (set) {
    NSSet *setCopy = (ARCBRIDGE NSSet *) set;
    for (JavaLangRefReference *reference in setCopy) {
      enqueued |= [reference enqueueInternal];
      reference->referent_ = nil;
    }
    CFDictionaryRemoveValue(weak_refs_map, referent);
  }
  return enqueued;
}


// Dealloc method for referent subclasses, which directly calls the
// original class's dealloc method. Normally "[super dealloc]" isn't
// permissible with ARC, but in this case it's actually invoking a
// delegate object's dealloc method (which won't invoke super-dealloc).
static void ReferentSubclassDealloc(id self, SEL _cmd) {
  BOOL enqueued = RemoveAllReferenceAssociations(self);
  if (!enqueued) {
    Class superclass = GetRealSuperclass(self);
    IMP superDealloc = class_getMethodImplementation(superclass, @selector(dealloc));
    ((void (*)(id, SEL))superDealloc)(self, _cmd);
  }
}


// Queues any references to this referent to their associated reference
// queue (if one is defined) when the referent's retainCount is low.
static void MaybeQueueReferences(id referent) {
  if ([referent retainCount] == 1) {
    // Add any associated references to their respective reference queues.
    CFMutableSetRef set = (void *) CFDictionaryGetValue(weak_refs_map, referent);
    if (set) {
      NSSet *setCopy = (ARCBRIDGE NSSet *) set;
      for (JavaLangRefReference *reference in setCopy) {
        if ([reference isKindOfClass:[JavaLangRefSoftReference class]]) {
          JavaLangRefSoftReference *softRef = (JavaLangRefSoftReference *) reference;
          if (in_low_memory_cleanup) {
            // Queue reference.
            [reference enqueueInternal];
            softRef->queued_ = YES;
          } else if (!softRef->queued_) {
            // Add to soft_references list.
            [referent retain];
            [soft_references addObject:reference];
          }
        } else {
          [reference enqueueInternal];
        }
      }
    }
  }
}


// Queues any phantom references to this referent when its retainCount
// is low. Phantom references are queued after the release message is sent,
// just like in Java they are queued after being finalized.
static void MaybeQueuePhantomReferences(id referent) {
  // Add any associated phantom references to their respective queues.
  CFMutableSetRef set = (void *) CFDictionaryGetValue(weak_refs_map, referent);
  if (set) {
    NSSet *setCopy = (ARCBRIDGE NSSet *) set;
    for (JavaLangRefReference *reference in setCopy) {
      if ([reference isKindOfClass:[JavaLangRefPhantomReference class]]) {
        // Enqueue PhantomReference, now that it's been "finalized".
        [reference enqueueInternal];
      }
    }
  }
}


// Invoke a referent subclass's original release method.
static void RealReferentRelease(id referent) {
  Class superclass = GetRealSuperclass(referent);
  IMP superRelease = class_getMethodImplementation(superclass, @selector(release));
  WhileLocked(^{
    ((void (*)(id, SEL))superRelease)(referent, @selector(release));
  });
}


// Release method for referent subclasses, which directly calls the
// original class's release method. If the instance would be
// deallocated when this function returns, it is added to its
// associated reference queue, if any.
static void ReferentSubclassRelease(id self, SEL _cmd) {
  MaybeQueueReferences(self);
  NSUInteger retainCount = [self retainCount];
  RealReferentRelease(self);
  if (retainCount == 1) {
    MaybeQueuePhantomReferences(self);
  }
}

@end
