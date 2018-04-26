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
#import "java/lang/OutOfMemoryError.h"
#import "java/lang/ref/PhantomReference.h"
#import "java/lang/ref/Reference.h"
#import "java/lang/ref/SoftReference.h"

#import <objc/runtime.h>
#import <pthread.h>
#import <stdio.h>

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
static CFMutableSetRef CreateSoftReferenceSet();

// NOLINTBEGIN

// An object to use for synchronizing access to soft_references and in_low_memory_cleanup.
static NSObject *soft_references_lock;

// Set of all referent objects that are soft ref queue candidates. These are only released when
// the runtime is notified of a low memory condition. Synchronized using soft_references_lock.
static CFMutableSetRef soft_references;

// true while cleaning up soft references. Synchronized using soft_references_lock.
static jboolean in_low_memory_cleanup;

// NOLINTEND

+ (void)initReferent:(JavaLangRefReference *)reference {
  @synchronized(reference) {
    id referent = JreLoadVolatileId(&reference->referent_);
    if (referent) {
      EnsureReferentSubclass(referent);
      AssociateReferenceWithReferent(referent, reference);
    }
  }
}

+ (id)getReferent:(JavaLangRefReference *)reference {
  // The referent must be loaded under @synchronized(reference) to avoid a race with another thread
  // that might be releasing the referent. We can't rely only on the volatile synchronization
  // because it is a @Weak volatile, but @synchronized(reference) will synchronize with the release
  // implementation in the referent subclass.
  @synchronized(reference) {
    // The volatile load ensures that the result is retained in this thread.
    return JreLoadVolatileId(&reference->referent_);
  }
}

+ (void)clearReferent:(JavaLangRefReference *)reference {
  @synchronized(reference) {
    id referent = JreLoadVolatileId(&reference->referent_);
    if (referent) {
      RemoveReferenceAssociation(referent, reference);
    }
    JreAssignVolatileId(&reference->referent_, nil);
  }
}

+ (void)handleMemoryWarning:(NSNotification *)notification {
  @synchronized(soft_references_lock) {
    in_low_memory_cleanup = true;
    CFSetRemoveAllValues(soft_references);
    in_low_memory_cleanup = false;
  }
}

+ (void)initialize {
  if (self == [IOSReference class]) {
    soft_references_lock = [[NSObject alloc] init];
    soft_references = CreateSoftReferenceSet();

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

static CFMutableSetRef CreateSoftReferenceSet() {
  CFSetCallBacks softReferenceSetCallBacks = kCFTypeSetCallBacks;
  // Set equal callback to NULL to use pointer equality.
  softReferenceSetCallBacks.equal = NULL;
  // Set hash callback to NULL to compute hash codes by converting pointers to integers
  softReferenceSetCallBacks.hash = NULL;
  return CFSetCreateMutable(NULL, 0, &softReferenceSetCallBacks);
}

// Returns the CFMutableSet of JavaLangRefReference associated with a reference object for the
// provided key. Must be called inside @synchronized(referent).
static CFMutableSetRef ReferenceSetForReferent(const void *key, id referent,
                                               jboolean createIfMissing) {
  CFMutableSetRef set = (CFMutableSetRef)objc_getAssociatedObject(referent, key);
  if (!set && createIfMissing) {
    set = CFSetCreateMutable(NULL, 0, NULL);
    objc_setAssociatedObject(referent, key, (id)set, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
    CFRelease(set);
  }
  return set;
}

// Returns the set of (non-soft, non-phantom) JavaLangRefReference objects that refer to referent.
// Must be called inside @synchronized(referent).
static CFMutableSetRef WeakReferencesForReferent(id referent, jboolean createIfMissing) {
  return ReferenceSetForReferent(&WeakReferencesForReferent, referent, createIfMissing);
}

// Returns the set of JavaLangRefSoftReference objects that refer to referent.
// Must be called inside @synchronized(referent).
static CFMutableSetRef SoftReferencesForReferent(id referent, jboolean createIfMissing) {
  return ReferenceSetForReferent(&SoftReferencesForReferent, referent, createIfMissing);
}

// Returns the set of JavaLangRefPhantomReference objects that refer to referent.
// Must be called inside @synchronized(referent).
static CFMutableSetRef PhantomReferencesForReferent(id referent, jboolean createIfMissing) {
  return ReferenceSetForReferent(&PhantomReferencesForReferent, referent, createIfMissing);
}

// Returns a malloc-ed c string containing the name of the referent subclass of a given class. If
// cls is already a referent subclass, its name is returned unmodified.
static char *CreateReferentSubclassNameForClass(Class cls) {
  char *result = NULL;
  if (asprintf(&result, "%s_ReferentSubclass", class_getName(cls)) < 0) {
    // asprintf's internal malloc failed, so throw an out of memory error.
    // NOLINTNEXTLINE
    @throw [[[JavaLangOutOfMemoryError alloc] init] autorelease];
  }
  return result;
}

// Returns true if cls is a runtime generated referrent subclass.
// Must be called inside @synchronized(cls).
jboolean IsReferrentSubclass(Class cls) {
  return (objc_getAssociatedObject(cls, &IsReferrentSubclass) != nil);
}

// Returns the referent subclass for a referent, or nil if one hasn't been created for that type.
// Must be called inside @synchronized(cls).
static Class GetReferentSubclass(Class cls) {
  Class subclass = objc_getAssociatedObject(cls, &GetReferentSubclass);
  if (subclass) {
    return subclass;
  }
  if (IsReferrentSubclass(cls)) {
    return cls;
  }
  return nil;
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
  return (retainCount == UINT_MAX || retainCount == INT_MAX);
}

// Create a custom subclass for specified referent class.
// Must be called inside @synchronized(cls).
static Class CreateReferentSubclass(Class cls) {
  char *newName = CreateReferentSubclassNameForClass(cls);
  Class subclass = objc_allocateClassPair(cls, newName, 0);
  @synchronized(subclass) {
    // Replace the referent subclass.
    Method dealloc = class_getInstanceMethod(cls, @selector(dealloc));
    class_addMethod(subclass, @selector(dealloc), (IMP)ReferentSubclassDealloc,
                    method_getTypeEncoding(dealloc));
    Method release = class_getInstanceMethod(cls, @selector(release));
    class_addMethod(subclass, @selector(release), (IMP)ReferentSubclassRelease,
                    method_getTypeEncoding(release));
    Method getClass = class_getInstanceMethod(cls, @selector(java_getClass));
    class_addMethod(subclass, @selector(java_getClass), (IMP)ReferentSubclassGetClass,
                    method_getTypeEncoding(getClass));
    objc_registerClassPair(subclass);

    // Mark the referrent subclass for use by IsReferrentSubclass.
    objc_setAssociatedObject(subclass, &IsReferrentSubclass, subclass, OBJC_ASSOCIATION_ASSIGN);

    // Annotate the class with the referrent subclass for use by GetReferentSubclass.
    objc_setAssociatedObject(cls, &GetReferentSubclass, subclass,
                             OBJC_ASSOCIATION_RETAIN_NONATOMIC);

    free(newName);
    return subclass;
  }
}

// Checks whether a referent subclass exists, and creates one if
// it doesn't. The exception is for constants, which are never
// dealloced.
static void EnsureReferentSubclass(id referent) {
  if (IsConstantObject(referent)) {
    return;
  }
  Class cls = object_getClass(referent);
  Class subclass;
  @synchronized(cls) {
    // Ensure that the referent subclass for cls exists.
    subclass = GetReferentSubclass(cls) ?: CreateReferentSubclass(cls);
  }

  // Patch the object's class to match the referent subclass.
  if (class_getSuperclass(subclass) == cls) {
    object_setClass(referent, subclass);
  }
}

// Returns the referent's original class.
static Class GetRealSuperclass(Class referentClass) {
  @synchronized(referentClass) {
    return IsReferrentSubclass(referentClass) ? class_getSuperclass(referentClass) : referentClass;
  }
}

// Add an association between a referent and its reference. Because
// multiple references can share a referent, a reference set is used.
static void AssociateReferenceWithReferent(id referent, JavaLangRefReference *reference) {
  @synchronized(referent) {
    if ([reference isKindOfClass:[JavaLangRefSoftReference class]]) {
      CFMutableSetRef softSet = SoftReferencesForReferent(referent, true);
      CFSetAddValue(softSet, reference);
    } else if ([reference isKindOfClass:[JavaLangRefPhantomReference class]]) {
      CFMutableSetRef phantomSet = PhantomReferencesForReferent(referent, true);
      CFSetAddValue(phantomSet, reference);
    } else {
      CFMutableSetRef weakSet = WeakReferencesForReferent(referent, true);
      CFSetAddValue(weakSet, reference);
    }
  }
}

// Remove the association between a referent and all of its references.
static void RemoveReferenceAssociation(id referent, JavaLangRefReference *reference) {
  @synchronized(referent) {
    if ([reference isKindOfClass:[JavaLangRefSoftReference class]]) {
      CFMutableSetRef softSet = SoftReferencesForReferent(referent, false);
      if (softSet) {
        CFSetRemoveValue(softSet, reference);
        if (CFSetGetCount(softSet) == 0) {
          // We could also remove the empty soft reference set here.
          @synchronized(soft_references_lock) {
            CFSetRemoveValue(soft_references, referent);
          }
        }
      }
    } else if ([reference isKindOfClass:[JavaLangRefPhantomReference class]]) {
      CFMutableSetRef phantomSet = PhantomReferencesForReferent(referent, false);
      if (phantomSet) {
        CFSetRemoveValue(phantomSet, reference);
        // We could also remove the phantom reference set here if it is now empty.
      }
    } else {
      CFMutableSetRef weakSet = WeakReferencesForReferent(referent, false);
      if (weakSet) {
        CFSetRemoveValue(weakSet, reference);
        // We could also remove the weak reference set here if it is now empty.
      }
    }
  }
}

// Invoke a referent subclass's original release method.
static void RealReferentDealloc(id referent) {
  Class referentClass = object_getClass(referent);
  Class superclass = GetRealSuperclass(referentClass);
  IMP superDealloc = class_getMethodImplementation(superclass, @selector(dealloc));
  ((void (*)(id, SEL))superDealloc)(referent, @selector(dealloc));
}

#pragma mark Referent Subclass Method Implementations

// Implementation of CFSetApplierFunction that clears the referent field of a reference.
// The reference access must be synchronized by the caller.
static void ClearReferentField(const void *value, void *context) {
  JavaLangRefReference *reference = (JavaLangRefReference *)value;
  JreAssignVolatileId(&reference->referent_, nil);
}

// Implementation of CFSetApplierFunction that enqueues a reference.
// The reference access must be synchronized by the caller.
static void EnqueueReference(const void *value, void *context) {
  JavaLangRefReference *reference = (JavaLangRefReference *)value;
  [reference enqueue];
}

// Dealloc method for referent subclasses, which directly calls the
// original class's dealloc method. Normally "[super dealloc]" isn't
// permissible with ARC, but in this case it's actually invoking a
// delegate object's dealloc method (which won't invoke super-dealloc).
static void ReferentSubclassDealloc(id self, SEL _cmd) {
  @synchronized(self) {
    CFMutableSetRef weakSet = WeakReferencesForReferent(self, false);
    if (weakSet) {
      CFSetApplyFunction(weakSet, &ClearReferentField, NULL);
      CFSetApplyFunction(weakSet, &EnqueueReference, NULL);

      // Remove reference associations.
      objc_setAssociatedObject(self, &WeakReferencesForReferent, nil,
                               OBJC_ASSOCIATION_RETAIN_NONATOMIC);
    }

    CFMutableSetRef softSet = SoftReferencesForReferent(self, false);
    if (softSet) {
      CFSetApplyFunction(softSet, &ClearReferentField, NULL);
      CFSetApplyFunction(softSet, &EnqueueReference, NULL);

      // Remove reference associations.
      objc_setAssociatedObject(self, &SoftReferencesForReferent, nil,
                               OBJC_ASSOCIATION_RETAIN_NONATOMIC);
    }

    CFMutableSetRef phantomSet = PhantomReferencesForReferent(self, false);
    if (phantomSet) {
      // Retain the phantom set until they can be enqueued after dealloc.
      CFRetain(phantomSet);

      CFSetApplyFunction(phantomSet, &ClearReferentField, NULL);

      // Remove reference associations.
      objc_setAssociatedObject(self, &PhantomReferencesForReferent, nil,
                               OBJC_ASSOCIATION_RETAIN_NONATOMIC);

      // Real dealloc.
      RealReferentDealloc(self);

      CFSetApplyFunction(phantomSet, &EnqueueReference, NULL);

      CFRelease(phantomSet);
    } else {
      // No phantom refs, so just do the real dealloc.
      RealReferentDealloc(self);
    }
  }
}

// Invoke a referent subclass's original release method.
static void RealReferentRelease(id referent) {
  Class referentClass = object_getClass(referent);
  Class superclass = GetRealSuperclass(referentClass);
  IMP superRelease = class_getMethodImplementation(superclass, @selector(release));
  ((void (*)(id, SEL))superRelease)(referent, @selector(release));
}

// Release method for referent subclasses, which directly calls the
// original class's release method. If the instance would be
// deallocated when this function returns, it is added to its
// associated reference queue, if any.
static void ReferentSubclassRelease(id self, SEL _cmd) {
  @synchronized(self) {
    // If the retainCount is 1 during release, RealReferentRelease will trigger dealloc.
    if ([self retainCount] == 1) {
      CFMutableSetRef softSet = SoftReferencesForReferent(self, false);
      if (softSet && CFSetGetCount(softSet) > 0) {
        // referent is softly reachable. Save it from deallocation.
        @synchronized(soft_references_lock) {
          if (!in_low_memory_cleanup) {
            CFSetAddValue(soft_references, self);
          }
        }
      }
    }
    RealReferentRelease(self);
  }
}

// Override getClass in the subclass so that it returns the IOSClass for the
// original class of the referent.
static IOSClass *ReferentSubclassGetClass(id self, SEL _cmd) {
  Class realSuperclass = GetRealSuperclass(object_getClass(self));
  return IOSClass_fromClass(realSuperclass);
}

@end
