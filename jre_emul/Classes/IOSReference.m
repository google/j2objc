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

#import "NSObject+JavaObject.h"
#import "IOSReference.h"

#import "IOSClass.h"
#import "J2ObjC_source.h"
#import "java/lang/ref/PhantomReference.h"
#import "java/lang/ref/Reference.h"
#import "java/lang/ref/SoftReference.h"

#import "java/util/zip/ZipFile.h"//_ZipFileInflaterInputStream
@class JavaUtilZipZipFile_ZipFileInputStream;

#import <objc/runtime.h>
#import <pthread.h>

#if TARGET_OS_MAC == 0
#import <UIKit/UIApplication.h>
#define SUPPORTS_SOFT_REFERENCES 1
#endif

#if __has_feature(objc_arc)


//////////////////////////////////////////////////////////////////////
// Added by DaeHoon Zee 8/1 2017
//////////////////////////////////////////////////////////////////////

@interface IOSReference() {
    NSPointerArray* refArray_;
    void* referentAddr_;
}
@end

@implementation IOSReference

static int assocKey;
static NSMutableArray* g_softRefArray_ = NULL;

//#define DBGLog(...) NSLog(__VAR_ARGS__)
#define DBGLog(...) //NSLog(__VAR_ARGS__)

#define USE_WEAK_REF_ARRAY 1

void ARGC_markWeakRef(id referent);
BOOL ARGC_isAliveObject(__unsafe_unretained ARGCObject* reference);

+ (void)initReferent:(JavaLangRefReference *)reference withReferent:(id)referent
{
    if (!referent) {
        return;
    }
    @synchronized (self) {
        if (g_softRefArray_ == NULL) {
            g_softRefArray_ = [[NSMutableArray alloc]init];
        }
        if ([[[referent class] description] isEqualToString:@"IOSConcreteClass"]) {
            DBGLog(@"Adding ref: %p %@", referent, [referent class]);
        }
        ARGC_markWeakRef(referent);
        
        DBGLog(@"Adding ref: %p %@", referent, [referent class]);
        IOSReference* rm = objc_getAssociatedObject(referent, &assocKey);
        if (rm == NULL) {
            rm = [[IOSReference alloc]init];
            rm->referentAddr_ = (__bridge void*)referent;
            objc_setAssociatedObject(referent, &assocKey, rm, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
        }
        DBGLog(@"Add ref: %p: %p %@", rm, referent, [referent class]);
        *(__weak id*)(void*)&reference->referent_ = referent;
        if ([reference isKindOfClass:[JavaLangRefSoftReference class]]) {
            [g_softRefArray_ addObject:referent];
        }
        [rm->refArray_ addPointer:(void*)reference];
    }
}

#define POST_INC_WEAK_LOAD 0
void ARGC_retainExternalWeakRef(id obj);

+ (id)getReferent:(JavaLangRefReference *)reference
{
#if 1
    __strong id obj = *(__weak id*)(void*)&reference->referent_;
    if (POST_INC_WEAK_LOAD && obj != NULL) {
        ARGC_retainExternalWeakRef(obj);
    }
    return obj;
#else
    @synchronized ([self class]) {
        /*  return *(__weak id*) reference->referent_
            를 사용할 수 없다. 내부 RefCounter 를 [obj retain]을 거치지 않고,
            직접 증가시키는 문제가 있다.
         */
    return *(__unsafe_unretained id*)(void*)&reference->referent_;
    }
#endif
}

+ (void)clearReferent:(JavaLangRefReference *)reference
{
    if (USE_WEAK_REF_ARRAY) {
        *(__weak id*)(void*)&reference->referent_ = NULL;
    }
    else {
        @synchronized (self) {
            id referent = *(__unsafe_unretained id*)(void*)&reference->referent_;
            if (referent == NULL) {
                return;
            }
            reference->referent_ = NULL;
            IOSReference* rm = objc_getAssociatedObject(referent, &assocKey);
            if (rm == NULL) {
                return;
            }
            for (int i = (int)rm->refArray_.count; --i > 0; ) {
                if (reference == [rm->refArray_ pointerAtIndex:i]) {
                    [rm->refArray_ removePointerAtIndex:i];
                    break;
                }
            }
        }
    }
}

+ (void)handleMemoryWarning:(NSNotification *)notification {
    @synchronized (self) {
        [g_softRefArray_ removeAllObjects];
    };
    ARGC_collectGarbage();
}

- (instancetype)init
{
    if (USE_WEAK_REF_ARRAY) {
        refArray_ = [NSPointerArray weakObjectsPointerArray];
    }
    else {
        refArray_ = [[NSPointerArray alloc] initWithOptions:NSPointerFunctionsOpaqueMemory] ;
    }
    return self;
}

void ARGC_strongRetain(id);

- (void)dealloc
{
    dispatch_time_t next_t = dispatch_time(DISPATCH_TIME_NOW, 10 * NSEC_PER_SEC / 1000);
    __strong NSPointerArray* array = self->refArray_;
    void* referentAddr = self->referentAddr_;
    
    //dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^
    @autoreleasepool
    {
        if (USE_WEAK_REF_ARRAY) {
            for (__strong JavaLangRefReference *reference in array) {
                if (reference != NULL) {//ARGC_isAliveObject(reference)) {
                    /* objc_loadWeakRef에 의해 refCount가 1 증가했다. 이를 사후 반영*/
                    if (POST_INC_WEAK_LOAD) {
                        ARGC_retainExternalWeakRef(reference);
                    }
                    DBGLog(@"Remove ref: %p(%p)", referentAddr, reference);
                    [reference clear];
                    [reference enqueue];
                }
            }
        }
        else {
            @synchronized (self) {
                for (__unsafe_unretained JavaLangRefReference *reference in array) {
                    if (reference != NULL) {
                        DBGLog(@"Remove ref: %p(%p)", referentAddr, reference);
                        reference->referent_ = NULL;
                        [reference enqueue];
                    }
                }
            }
        }
    };
}

@end

//////////////////////////////////////////////////////////////////////


#else

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
static CFMutableSetRef CreateSoftReferenceSet();

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
    soft_references = CreateSoftReferenceSet();
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
    referent_subclasses = [[NSMutableSet alloc] init];
    referent_subclass_map = [[NSMutableDictionary alloc] init];
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

static void WhileLocked(void (^block)(void)) {
  pthread_mutex_lock(&reference_mutex);
  block();
  pthread_mutex_unlock(&reference_mutex);
}


// Returns the referent subclass for a referent, or nil if one hasn't
// been created for that type. Caller must hold the mutex.
static Class GetReferentSubclass(id obj) {
  Class cls = object_getClass(obj);
  while (cls && ![referent_subclasses containsObject:cls])
    cls = class_getSuperclass(cls);
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
    Class subclass = [referent_subclass_map objectForKey:cls];
    if (!subclass) {
      subclass = CreateReferentSubclass(cls);
      [referent_subclass_map setObject:subclass forKey:(id<NSCopying>) cls];
      [referent_subclasses addObject:subclass];
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


// Dealloc method for referent subclasses, which directly calls the
// original class's dealloc method. Normally "[super dealloc]" isn't
// permissible with ARC, but in this case it's actually invoking a
// delegate object's dealloc method (which won't invoke super-dealloc).
static void ReferentSubclassDealloc(id self, SEL _cmd) {
  WhileLocked(^{
    CFMutableSetRef set = (CFMutableSetRef)CFDictionaryGetValue(weak_refs_map, self);
    if (set) {
      NSSet *setCopy = (ARCBRIDGE NSSet *) set;
      int numPhantom = 0;
      id phantomRefs[setCopy.count];
      for (JavaLangRefReference *reference in setCopy) {
        // Clear the referent field.
        JreAssignVolatileId(&reference->referent_, nil);
        // Queue the reference unless it is a phantom.
        if ([reference isKindOfClass:[JavaLangRefPhantomReference class]]) {
          phantomRefs[numPhantom++] = reference;
        } else {
          [reference enqueue];
        }
      }

      // Real dealloc.
      RealReferentDealloc(self);
      // Remove reference associations.
      CFDictionaryRemoveValue(weak_refs_map, self);

      // Queue all phantom references.
      for (int i = 0; i < numPhantom; i++) {
        [phantomRefs[i] enqueue];
      }
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


#endif
