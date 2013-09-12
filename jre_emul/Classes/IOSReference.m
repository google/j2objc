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
#import "java/lang/ref/Reference.h"

#import <dlfcn.h>
#import <objc/runtime.h>
#import <pthread.h>


// This class implements a variation on the design described in
// Mike Ash's "Zeroing Weak References in Objective-C" blog entry.
// Thanks, Mike, for your excellent article!
//
// http://mikeash.com/pyblog/friday-qa-2010-07-16-zeroing-weak-references-in-objective-c.html
//
// This design creates a subclass of a referent's class (one per
// referent type), then replaces the referent's class with it.
// These referent subclasses have a dealloc method that zeroes
// the reference's referent field, posts a copy of the referent
// to the reference's queue (if it exists), and then calls the
// original class's dealloc method.

@interface NSObject (JavaLangRefReferenceSwizzled)
- (void)JavaLangRefReference_original_dealloc;
@end


@implementation IOSReference

static void AssociateReferenceWithReferent(id referent, JavaLangRefReference *reference);
static void EnsureReferentSubclass(id referent);
static void RemoveReferenceAssociation(id referent, JavaLangRefReference *reference);

// Global recursive mutux used with WhileLocked().
static pthread_mutex_t reference_mutex;

// Maps referents to sets of Reference instances that refer to them.
static CFMutableDictionaryRef weak_refs_map;

// Maps referent classes to their referent subclasses.
static NSMutableDictionary *referent_subclass_map;

// Set of all referent subclasses.
static NSMutableSet *referent_subclasses;

+ (void)initReferent:(JavaLangRefReference *)reference {
  EnsureReferentSubclass(reference->referent_);
  AssociateReferenceWithReferent(reference->referent_, reference);
}

+ (void)deallocReferent:(JavaLangRefReference *)reference {
  if (reference->referent_) {
    RemoveReferenceAssociation(reference->referent_, reference);
  }
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
  }
}


// Returns the referent subclass for a referent, or nil if one hasn't
// been created for that type.
static Class GetReferentSubclass(id obj) {
  Class class = object_getClass(obj);
  while (class && ![referent_subclasses containsObject:class])
    class = class_getSuperclass(class);
  return class;
}


// Returns YES if an object is constant. Normally object retain
// counts shouldn't be queried, but it's permissible here because
// retain/release don't modify retain counts when they are INT_MAX
// (or UINT_MAX on some architectures).
static BOOL IsConstantObject(id obj) {
  unsigned int retainCount = [obj retainCount];
  return retainCount == UINT_MAX || retainCount == INT_MAX;
}


// Create a custom subclass for specified referent class.
static Class CreateReferentSubclass(Class class) {
  NSString *newName = [NSString stringWithFormat: @"%s_ReferentSubclass", class_getName(class)];
  Class subclass = objc_allocateClassPair(class, [newName UTF8String], 0);
  Method dealloc = class_getInstanceMethod(class, @selector(dealloc));
  class_addMethod(subclass, @selector(dealloc), (IMP) ReferentSubclassDealloc,
                  method_getTypeEncoding(dealloc));
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
  CFMutableSetRef set = (void *)CFDictionaryGetValue(weak_refs_map, referent);
  if (!set) {
    set = CFSetCreateMutable(NULL, 0, NULL);
    CFDictionarySetValue(weak_refs_map, referent, set);
    CFRelease(set);
  }
  CFSetAddValue(set, reference);
}


// Remove the association between a referent and all of its references.
static void RemoveReferenceAssociation(id referent, JavaLangRefReference *reference) {
  CFMutableSetRef set = (void *)CFDictionaryGetValue(weak_refs_map, referent);
  CFSetRemoveValue(set, reference);
}


// A referent is being dealloc'd, so remove all references to it from
// the weak refs map. If one or more references have a
static BOOL RemoveAllReferenceAssociations(id referent) {
  BOOL enqueued = NO;
  CFMutableSetRef set = (void *) CFDictionaryGetValue(weak_refs_map, referent);
  if (set) {
    NSSet *setCopy = (ARCBRIDGE NSSet *) set;
    for (JavaLangRefReference *reference in setCopy) {
      enqueued |= [reference enqueueInternal];
      [reference clear];
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


@end
