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
 */

//
//  Created by daehoon.zee on 30/10/2016.
//  https://github.com/zeedh/j2objc.git
//

#import "ARGC.h"
#import "objc/runtime.h"
#include "java/lang/ref/Reference.h"
#include "java/lang/ref/SoftReference.h"
#include "java/lang/ref/WeakReference.h"
#include "java/lang/ref/ReferenceQueue.h"
#include "java/lang/OutOfMemoryError.h"
#include "java/io/PrintStream.h"
#include "java/lang/StringBuilder.h"
#include "java/io/ByteArrayOutputStream.h"
#include "java/lang/Exception.h"
#include "java/lang/reflect/Modifier.h"

#include "RefContext.h"
#include "NSObject+ARGC.h"
#include "IOSPrimitiveArray.h"
#include "IOSReflection.h"
#include "IOSMetadata.h"
#include "IOSClass.h"
#include "IOSReference.h"
#include "IOSConcreteClass.h"
#include "IOSProtocolClass.h"
#include "NSString+JavaString.h"

#if !GC_DEBUG
#pragma GCC optimize ("O2")
#pragma clang optimize on
#else
#pragma clang optimize off
#endif

static const int MAX_TOUCH_DEPTH = 512;
static const int SCAN_BUFF_SIZE = 4096;

FOUNDATION_EXPORT int GC_LOG_ALL = 0;
FOUNDATION_EXPORT int GC_LOG_LOOP = GC_DEBUG;
FOUNDATION_EXPORT int GC_LOG_RC = 0;
FOUNDATION_EXPORT int GC_LOG_ROOTS = 0;
FOUNDATION_EXPORT int GC_LOG_ALIVE = 0;
FOUNDATION_EXPORT int GC_LOG_ALLOC = 0;
FOUNDATION_EXPORT int GC_LOG_WEAK_SOFT_REF = 1;
FOUNDATION_EXPORT int GC_TRACE_RELEASE = 0;

FOUNDATION_EXPORT void* GC_TRACE_REF = (void*)-1;
FOUNDATION_EXPORT void* GC_TRACE_CLASS = (void*)-1;

#define GC_TRACE(jobj, flag) (GC_DEBUG && (GC_LOG_ALL || jobj == GC_TRACE_REF || (flag) || getARGCClass(jobj) == GC_TRACE_CLASS))

static NSString* SKIP_LOG = NULL;
static const BOOL ENABLE_PENDING_RELEASE = false;
static const int _1M = 1024*1024;
static const int REFS_IN_BUCKET = _1M / sizeof(void*);
static const int USE_AUTORELEASE_OLD = 0;


static Class g_ARGCClass;
static Class g_stringClass;
static Class g_objectArrayClass;
static Class g_referenceClass;
static IOSClass* g_javaStringClass;
static NSPointerArray* g_softRefs;
static NSPointerArray* g_weakRefs;

static const int SCANNING = 1;
static const int FINALIZING = 2;
static const int FINISHED = 0;
static scan_offset_t _emptyFields[1] = { 0 };

static int refAssocKey;
static int iosClassAssocKey;
//static int metadataAssocKey;
static int64_t gc_interval = 0;

static Class getARGCClass(id jobj) {
  /*
   NSObject 상속 객체에 대해서만 사용 가능.
   그 외의 경우엔 메모리 억세스 오류 발생.
   */
  Class c = ((Class*)jobj)[0];
  return c;//[jobj class];
}

static JObj_p toARGCObject(id oid) {
  if (oid == NULL) return NULL;
  //return [oid toJObject];
  //object_getClass(oid);
  if ([oid isKindOfClass:g_ARGCClass]) {
    JObj_p jobj = (JObj_p)oid;
    if (!jobj->_rc.isPhantom()) {
      return jobj;
    }
  }
  return NULL;
}

//##############################################################


static void OutOfMemory() {
  @throw AUTORELEASE([[JavaLangOutOfMemoryError alloc] init]);
}

class ARGC {
public:
    typedef void (*ARGCVisitor)(JObj_p jobj);
    
    static ARGC _instance;
    static std::atomic_int gc_state;//GC_IN_SCANNING;
    std::atomic<ARGCVisitor> deallocMethod;

    Method _no_java_finalize;

    ARGC();
    
    int size() {
        return _cntRef;
    }
    
    void doGC();

    void doClearReferences(NSPointerArray* refList, BOOL markSoftRef);
    
    static void markReachableSubInstances(JObj_p jobj);

    static void markRootInstance(JObj_p jobj);
    
    static void markArrayItems(id array);
    
    static void scanInstances(id* pItem, int cntItem, JObj_p* scanBuff, JObj_p* pBuffEnd);
    
    static void touchInstance(JObj_p jobj) {
        if (!jobj->_rc.isStrongReachable()) {
            markRootInstance(jobj);
        }
    }
    
    static BOOL inGC() {
        return _instance._inGC;
    }
    
    id allocateInstance(Class cls, NSUInteger extraBytes, NSZone* zone) {
        size_t instanceSize = class_getInstanceSize(cls);
        if (instanceSize < argcObjectSize) {
            extraBytes = argcObjectSize - instanceSize;
        }
        
        ARGCObject* oid = NSAllocateObject(cls, extraBytes, zone);
        for (int cntLoop = 1; oid == NULL; cntLoop++) {
            _clearSoftReference = 1;
            doGC();
            oid = NSAllocateObject(cls, extraBytes, zone);
            if (cntLoop > 2) {
                OutOfMemory();
            }
        }
        if (GC_TRACE(oid, (GC_LOG_ALLOC || GC_LOG_RC))) {
            NSLog(@"alloc %p %@", oid, cls);
        }
        registerScanOffsets(cls);
        oid->_rc.initialize(_finalizeClasses[cls] == NULL);
        _cntAllocated ++;
        // bindCount 0 인 상태, 즉 allocation 직후에도 stack에서 참조가능한 상태이다.
        addStrongRef(oid);
        return oid;
    }
    
    static void bindRoot(JObj_p jobj, NSString* tag) {
        if (!jobj->_rc.isPhantom()) {
            jobj->_rc.bind();
            ARGC::touchInstance(jobj);
            ARGC::increaseReferenceCount(jobj, tag);
        }
        else {
            ARGC::increaseReferenceCount(jobj, @"retain-phatom");
        }
    }

    static void unbindRoot(JObj_p jobj) {
        if (jobj->_rc.isPhantom()) {
            decreaseRefCount(jobj, @"-phntm");
            return;
        }

        jobj->_rc.unbind();
        
        if (ARGC::decreaseRefCountOrDealloc(jobj, @"release")) {
            if (!jobj->_rc.isRootReachable()) {
                ARGC::mayHaveGarbage = 1;
            }
        }
        return;
    }

    static void increaseReferenceCount(id oid, NSString* tag) {
        NSIncrementExtraRefCount(oid);
        //checkRefCount(jobj, tag);
    }

    static void increaseReferenceCount(JObj_p jobj, NSString* tag) {
        NSIncrementExtraRefCount(jobj);
        checkRefCount(jobj, tag);
    }

    static void decreaseGenericReferenceCountOrDealloc(__unsafe_unretained id oid) {
        JObj_p jobj = toARGCObject(oid);
        if (jobj != NULL) {
            if (GC_TRACE(jobj, 0)) {
                decreaseRefCountOrDealloc(jobj, @"--.fld");
            }
            else {
                decreaseRefCountOrDealloc(jobj, @"--.fld");
            }
        }
        else {
            if (GC_DEBUG && GC_LOG_ALLOC) {
                NSLog(@"--natv %p #%d %@", oid, (int)NSExtraRefCount(oid), [oid class]);
            }
            [oid release];
        }
    }

    static BOOL decreaseRefCount(JObj_p jobj, NSString* tag) {
        if (!NSDecrementExtraRefCountWasZero(jobj)) {
            checkRefCount(jobj, tag);
            return true;
        }

        if (GC_TRACE(jobj, GC_LOG_ALLOC)) {
            NSLog(@"--zero %p -1(%d) %@", jobj, jobj->_rc.bindCount(), getARGCClass(jobj));
        }
        return false;
    }
    
    static BOOL decreaseRefCountOrDealloc(JObj_p jobj, NSString* tag) {
        if (decreaseRefCount(jobj, tag)) {
            return true;
        }

//        if (gc_state != SCANNING) {
            [jobj dealloc];
//        }
//        else if (FALSE && !jobj->_rc.isReachable()) {
//            /* do not dealloc sub-references. */
//            markRootInstance(jobj, false);
//        }
        return false;
    }
    
    static BOOL markStrongReachable(JObj_p jobj) {
        BOOL res = jobj->_rc.markStrongReachable();
        if (res && GC_TRACE(jobj, 0)) {
            NSLog(@"Mark Reachable %p", jobj);
        }
        return res;
    }
    
    static const scan_offset_t* getScanOffsets(Class clazz) {
        ScanOffsetArray* scanOffsets = objc_getAssociatedObject(clazz, &_instance);
        if (scanOffsets == NULL) {
            return NULL;
        }
        return scanOffsets->offsets;
    }
    
    static void checkRefCount(JObj_p jobj, NSString* tag) {
        if (tag != SKIP_LOG && GC_TRACE(jobj, GC_LOG_RC)) {
            int bindCount = jobj->_rc.bindCount();
            int refCount = (int)NSExtraRefCount(jobj)+1;
            NSLog(@"%@ %p #%d+%d %@", tag, jobj, bindCount, refCount-bindCount, getARGCClass(jobj));
            if ((int)NSExtraRefCount(jobj) < (int)jobj->_rc.bindCount() - 1) {
                // 멀티쓰레드환경이라 두 값의 비교가 정확하지 않다. 관련 정보를 적는다.
                NSLog(@"Maybe error %@ %p %d(%d) %@", tag, jobj, (int)NSExtraRefCount(jobj), jobj->_rc.bindCount(), getARGCClass(jobj));
            };
        }
    }
    
private:
    static void dealloc_in_collecting(JObj_p jobj);

    static void dealloc_in_scan(JObj_p jobj);
    
    static void dealloc_now(JObj_p jobj);
    
    RefSlot* newBucket() {
        RefSlot* pMem = (RefSlot*)malloc(_1M);
        memset(pMem, 0, _1M);
        return pMem;
    }
    
    void checkSlotIndex(int idxSlot, std::atomic_int* pCount) {
        if (GC_DEBUG) {
            if (idxSlot > *pCount) {
                @synchronized (tableLock) {
                    assert(idxSlot <= *pCount);
                }
            }
        }
    }
    
    BOOL unregisterRef(JObj_p jobj) {
        int idxSlot = jobj->_rc.slotIndex();
        RefSlot* pSlot = getRefSlot(idxSlot, false);
        if (pSlot->compare_exchange_strong(jobj, NULL)) {
            return true;
        }
        return false;
    }
    
    RefSlot* getRefSlot(int index, BOOL checkIndex=true) {
        if (checkIndex) checkSlotIndex(index, &_cntRef);
        int idxBucket = index / REFS_IN_BUCKET;
        int idxRef = index % REFS_IN_BUCKET;
        return _table[idxBucket] + idxRef;
    }
    
    RefSlot* getPhantomSlot(int index) {
        checkSlotIndex(index, &_cntPhantom);
        int idxBucket = index / REFS_IN_BUCKET;
        int idxRef = index % REFS_IN_BUCKET;
        return _phantomeTable[idxBucket] + idxRef;
    }
    
    void addStrongRef(JObj_p jobj) {
        @synchronized (tableLock) {
            int index = _cntRef;
            jobj->_rc.setSlotIndex(index);
            
            int idxBucket = index / REFS_IN_BUCKET;
            int idxRef = index % REFS_IN_BUCKET;
            RefSlot* row = _table[idxBucket];
            if (row == NULL) {
                row = _table[idxBucket];
                if (row == NULL) {
                    row = _table[idxBucket] = newBucket();
                }
            }
            row[idxRef] = jobj;
            // 반드시 jobj 추가후 _cntRef 증가.
            _cntRef ++;
        }
    }
    
    void reclaimInstance(JObj_p jobj) {
        if (GC_TRACE(jobj, GC_LOG_ALLOC)) {
            NSLog(@"dealloc: %p %@", jobj, getARGCClass(jobj));
        }
        NSDeallocateObject(jobj);
        _cntDeallocated ++;
    }
    
    void addPhantom(JObj_p jobj, BOOL isThreadSafe) {
        if (jobj->_rc.markFinalized()) {
            JreFinalize(jobj);
            if (jobj->_rc.isStrongReachable()) {
                return;
            }
        }
        if (!jobj->_rc.markPhantom()) {
            return;
        }

        [jobj forEachObjectField:(ARGCObjectFieldVisitor)decreaseGenericReferenceCountOrDealloc inDepth: 0];
        _instance.unregisterRef(jobj);

        if (GC_TRACE(jobj, GC_LOG_ALLOC)) {
            NSLog(@"addPhantom: %p %@ %@", jobj, getARGCClass(jobj), isThreadSafe ? @"delete-now" : @"delete-later");
        }

        if (isThreadSafe) {
            assert(NSExtraRefCount(jobj) == 0);
            reclaimInstance(jobj);
        }
        else {
            @synchronized (tableLock) {
                int index = _cntPhantom ++;
                int idxBucket = index / REFS_IN_BUCKET;
                int idxRef = index % REFS_IN_BUCKET;
                RefSlot* row = _phantomeTable[idxBucket];
                if (row == NULL) {
                    row = _phantomeTable[idxBucket];
                    if (row == NULL) {
                        row = _phantomeTable[idxBucket] = newBucket();
                    }
                }
                row[idxRef] = jobj;
            }
        }
    }
    
    void registerScanOffsets(Class clazz);
    
    size_t argcObjectSize;
    std::atomic_int _cntRef;
    std::atomic_int _cntPhantom;
    std::atomic_int _cntAllocated;
    std::atomic_int _cntDeallocated;
    std::atomic_int _cntMarkingThread;
    std::atomic_int _cntDellocThread;
    RefSlot* _table[1024];
    RefSlot* _phantomeTable[1024];
    NSMutableDictionary* _scanOffsetCache;
    NSMutableDictionary* _finalizeClasses;
    NSObject* tableLock;
    NSObject* scanLock;
    
    BOOL _inGC;
public:
    static std::atomic_int _clearSoftReference;
    static int mayHaveGarbage;
};

ARGC ARGC::_instance;
int ARGC::mayHaveGarbage = 0;
std::atomic_int ARGC::gc_state;
std::atomic_int ARGC::_clearSoftReference;
volatile int64_t RefContext::strong_reachable_generation_bit = 0;

void ARGC_bindJavaClass(id key, IOSClass* javaClass) {
  assert(objc_getAssociatedObject(key, &iosClassAssocKey) == NULL);
  NSIncrementExtraRefCount(javaClass);
  objc_setAssociatedObject(key, &iosClassAssocKey, javaClass, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

IOSClass* ARGC_getIOSClass(id key) NS_RETURNS_RETAINED J2OBJC_METHOD_ATTR {
  return (IOSClass*)objc_getAssociatedObject(key, &iosClassAssocKey);
}


void ARGC_bindIOSClass(Class nativeClass, const J2ObjcClassInfo *metaData) J2OBJC_METHOD_ATTR {
  assert(ARGC_getIOSClass(nativeClass) == NULL);
  IOSClass *javaClass = [[IOSConcreteClass alloc] initWithClass:nativeClass metadata:metaData];
  ARGC_bindJavaClass(nativeClass, javaClass);
}

void ARGC_bindIOSProtocol(Protocol* protocol, const J2ObjcClassInfo *metaData) J2OBJC_METHOD_ATTR {
  assert(ARGC_getIOSClass(protocol) == NULL);
  IOSClass *javaClass = [[IOSProtocolClass alloc] initWithProtocol:protocol metadata:metaData];
  ARGC_bindJavaClass(protocol, javaClass);
}


//void ARGC_bindIOSClass(id nativeClass, IOSClass* javaClass) J2OBJC_METHOD_ATTR {
//  assert(javaClass->metadata_ == ARGC_getMetaData(nativeClass));
//  assert(ARGC_getIOSClass(nativeClass) == NULL);
//  objc_setAssociatedObject(nativeClass, &iosClassAssocKey, (id)javaClass, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
//}

IOSClass* ARGC_getIOSConcreteClass(Class nativeClass) NS_RETURNS_RETAINED J2OBJC_METHOD_ATTR {
  IOSClass *javaClass = ARGC_getIOSClass(nativeClass);
  if (javaClass == NULL) {
    if ([nativeClass isKindOfClass:g_stringClass]) {
      ARGC_bindJavaClass(nativeClass, g_javaStringClass);
      javaClass = g_javaStringClass;
    }
  }
  return RETAIN_(javaClass);
}

IOSClass* ARGC_getIOSProtocolClass(Protocol* protocol) NS_RETURNS_RETAINED J2OBJC_METHOD_ATTR {
  IOSClass *javaClass = ARGC_getIOSClass(protocol);
  if (javaClass == NULL) {
    [NSClassFromString(NSStringFromProtocol(protocol)) class];
    javaClass = ARGC_getIOSClass(protocol);
  }
  return RETAIN_(javaClass);
}

@implementation ARGCObject

+ (const scan_offset_t*) scanOffsets
{
    return _emptyFields;
}

+ (void)load
{
    IOSClass* iosClass = ARGC_getIOSClass(self);
    NSLog(@"load: %@, %@", self, iosClass);
}

+ (void)initialize
{
    IOSClass* iosClass = ARGC_getIOSClass(self);
    NSLog(@"initialize: %@, %@", self, iosClass);
}

+ (instancetype)alloc
{
    IOSClass* iosClass = ARGC_getIOSClass(self);
    if (iosClass != NULL) {
      iosClass->metadata_->initialize();
    }
    else {
      //ARGC_bindIOSClass(self, &JreEmptyClassInfo
      NSLog(@"non ARGCObject alloc: %@", self);

    }
    id oid = ARGC::_instance.allocateInstance(self, 0, NULL);
    return oid;
}

+ (instancetype)allocWithZone:(struct _NSZone *) zone
{
    /**
     allocWithZone:zone is deprecated. The method ignores 'zone' argument.
     So [[cls allocWithZone:myZone] zone] is not equals to myZone;
     */
    id _id = ARGC::_instance.allocateInstance(self, 0, zone);
    return _id;
}

- (JObj_p) toJObject {
    return _rc.isPhantom() ? NULL : self;
}

- (instancetype)retain {
    ARGC::bindRoot(self, @"retain");
    return self;
}

- (BOOL)retainWeakReference {
    if ([super retainWeakReference]) {
        // retainWeakReference 함수 수행 중에는 NSLog 등 다른 API 호출이 금지된 것으로 보인다.
        if (self->_rc.isStrongReachable()) {
            self->_rc.bind();
        }
        else if (ARGC::gc_state == SCANNING) {
            ARGC::bindRoot(self, SKIP_LOG);
        }
        else {
            return false;
        }
        return true;
    }
    return false;
}

- (oneway void)release {
    ARGC::unbindRoot(self);
#if GC_ENABLE_TRACE_RELEASE
    if (GC_TRACE_RELEASE) {
        if (GC_TRACE(self, 0)) {
        GC_TRACE_RELEASE=0;
        @try {
            @throw new_JavaLangException_init();
        }
        @catch (JavaLangException* ex) {
            JavaIoByteArrayOutputStream* bos = new_JavaIoByteArrayOutputStream_init();
            JavaIoPrintStream*    dbgOut = new_JavaIoPrintStream_initWithJavaIoOutputStream_(bos);
            [ex printStackTraceWithJavaIoPrintStream:dbgOut];
            [dbgOut writeWithInt:0];
            NSLog(@"%p, %s", self, [bos toByteArray]->buffer_);
        }
        GC_TRACE_RELEASE=1;
        }
    }
#endif
}

#pragma clang diagnostic ignored "-Wobjc-missing-super-calls"
- (void)dealloc {
    ARGC::ARGCVisitor fn = ARGC::_instance.deallocMethod;
    fn(self);
}

#if GC_DEBUG
- (instancetype)autorelease {
    if (GC_TRACE(self, 0)) {
        ARGC::checkRefCount(self, @"autorelease");
    }
    if (GC_DEBUG && GC_LOG_ALLOC) {
        if (toARGCObject(self) == NULL) {
            NSLog(@"--nstr %p #%d %@", self, (int)NSExtraRefCount(self), [self class]);
        }
    }
    return [super autorelease];
}
#endif


- (void) forEachObjectField: (ARGCObjectFieldVisitor) visitor inDepth:(int) depth
{
    const scan_offset_t* scanOffsets = ARGC::getScanOffsets(getARGCClass(self));
    JObj_p jobj = self;
    while (true) {
        scan_offset_t offset = *scanOffsets++;
        if (offset == 0) {
            break;
        }
        id ref = *((id *)jobj + offset);
        JObj_p fv = toARGCObject(ref);
        if (fv != NULL) {
            visitor(fv, depth);
        }
    }
}

@end

ARGC::ARGC() {
    this->_inGC = false;
    g_softRefs = [[NSPointerArray alloc] initWithOptions:NSPointerFunctionsOpaqueMemory];
    g_weakRefs = [[NSPointerArray alloc] initWithOptions:NSPointerFunctionsOpaqueMemory];

    RefContext::change_generation();
    g_referenceClass = objc_lookUpClass("JavaLangRefReference");
    g_ARGCClass = ARGCObject.class;
    g_stringClass = NSString.class;
    g_objectArrayClass = IOSObjectArray.class;
    deallocMethod = dealloc_now;
    _no_java_finalize = class_getInstanceMethod(NSObject.class, @selector(java_finalize));

    _scanOffsetCache = [[NSMutableDictionary alloc] init];
    _finalizeClasses = [[NSMutableDictionary alloc] init];
    tableLock = [[NSObject alloc]init];
    scanLock = [[NSObject alloc]init];
    argcObjectSize = class_getInstanceSize(ARGCObject.class) + sizeof(void*);
    ARGC_setGarbageCollectionInterval(1000);
  
    // initialize IOSClass;
    (void)IOSClass.class;
    g_javaStringClass = NSString_class_();
    ARGC_bindJavaClass(ARGCObject.class, ARGC_getIOSClass(NSObject.class));
}

void ARGC::registerScanOffsets(Class clazz) {
    scan_offset_t _offset_buf[4096];
    id _test_obj_buf[4096];
    ScanOffsetArray* res = objc_getAssociatedObject(clazz, &_instance);
    if (res != NULL) {
        return;
    }

    @synchronized (_scanOffsetCache) {
        res = objc_getAssociatedObject(clazz, &_instance);
        if (res != NULL) {
            return;
        }
        objc_setAssociatedObject(clazz, &_instance, _scanOffsetCache, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
    }
    
    int cntARGC = 0;
    id clone = NSAllocateObject(clazz, 0, NULL);
    int cntObjField = 0;
    Method method = class_getInstanceMethod(clazz, @selector(java_finalize));
    assert(method != NULL);
    if (method != _no_java_finalize) {
        _finalizeClasses[clazz] = clazz;
    }
    
    for (Class cls = clazz; cls && cls != [NSObject class]; ) {
        unsigned int ivarCount;
        Ivar *ivars = class_copyIvarList(cls, &ivarCount);
        for (unsigned int i = 0; i < ivarCount; i++) {
            Ivar ivar = ivars[i];
            const char *ivarType = ivar_getTypeEncoding(ivar);
            if (*ivarType == '@') {
                ptrdiff_t offset = ivar_getOffset(ivar);
                _offset_buf[cntObjField] = offset / sizeof(JObj_p);
                id oid = [[[NSObject alloc] retain] retain];
                __unsafe_unretained id* pField = (__unsafe_unretained id *)((char *)clone + offset);
                _test_obj_buf[cntObjField] = *pField = oid;
                if (GC_DEBUG && cls == g_referenceClass) {
                    NSLog(@"Refernce[%d].%s field check", (int)(offset / sizeof(JObj_p)), ivar_getName(ivar));
                    //referent 가 volatile_id(uint_ptr)롤 처리되어 검사 불필요.;
                    //continue;
                }
                cntObjField ++;
            }
        }
        free(ivars);
        cls = class_getSuperclass(cls);
    }
    
    NSDeallocateObject(clone);
    for (int i = 0; i < cntObjField; i ++) {
        scan_offset_t offset = _offset_buf[i];
        id field = _test_obj_buf[i];
        NSUInteger cntRef = NSExtraRefCount(field);
        if (cntRef > 1) {
            /**
             Dealloc 과정에서 ARC-decreement 가 발생하지 않은 것은 ARGC-Field 이다.
             */
            _offset_buf[cntARGC++] = offset;
        }
    }

    _offset_buf[cntARGC++] = 0;
    int memsize = cntARGC * sizeof(scan_offset_t);
    ScanOffsetArray* offsetArray = NSAllocateObject([ScanOffsetArray class], memsize, NULL);
    memcpy(offsetArray->offsets, _offset_buf, memsize);
    @synchronized (_scanOffsetCache) {
        objc_setAssociatedObject(clazz, &_instance, offsetArray, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
    }
}

void ARGC_assignStrongObject(ARGC_FIELD_REF id* pField, __unsafe_unretained id newValue) {
    if (*pField == newValue) {
        return;
    }
    
    std::atomic<id>* field = (std::atomic<id>*)pField;
    id oldValue = field->exchange(newValue);
    if (oldValue == newValue) {
        return;
    }
    if (newValue != NULL) {
        [newValue retain];
    }
    if (oldValue != NULL) {
        if (GC_DEBUG && GC_LOG_ALLOC) {
            if (toARGCObject(oldValue) == NULL) {
                NSLog(@"--nstr %p #%d %@", oldValue, (int)NSExtraRefCount(oldValue), [oldValue class]);
            }
        }
        [oldValue release];
    }
    return;
}

void ARGC_assignARGCObject(ARGC_FIELD_REF id* pField, __unsafe_unretained id newValue0) {
    if (*pField == newValue0) {
        return;
    }
    ARGCObject* newValue = newValue0;
    std::atomic<ARGCObject*>* field = (std::atomic<ARGCObject*>*)pField;
    ARGCObject* oldValue = field->exchange(newValue);
    if (oldValue == newValue) {
        return;
    }
    if (newValue != NULL) {
        JObj_p jobj = newValue;
        ARGC::increaseReferenceCount(jobj, @"++argc");
        ARGC::touchInstance(jobj);
    }
    if (oldValue != NULL) {
        JObj_p jobj = oldValue;
        ARGC::decreaseRefCountOrDealloc(jobj, @"--argc");
    }
    return;
}

void ARGC_assignGenericObject(ARGC_FIELD_REF id* pField, __unsafe_unretained id newValue) {
    if (*pField == newValue) {
        return;
    }
    
    std::atomic<id>* field = (std::atomic<id>*)pField;
    id oldValue = field->exchange(newValue);
    if (oldValue == newValue) {
        return;
    }
    ARGC_genericRetain(newValue);
    ARGC_genericRelease(oldValue);
    return;
}



void ARGC_collectGarbage() {
    ARGC::_instance.doGC();
}

void ARGC::dealloc_in_collecting(JObj_p jobj) {
    if (GC_TRACE(jobj, GC_LOG_ALLOC)) {
        NSLog(@"dealloc-in-collecting: %p %@", jobj, getARGCClass(jobj));
    }
    
    _instance.addPhantom(jobj, false);
}

void ARGC::dealloc_in_scan(JObj_p jobj) {
    /*
     Do nothing
    if (ARGC::_instance.unregisterRef(jobj)) {
        [jobj forEachObjectField:(ARGCObjectFieldVisitor)decreaseGenericReferenceCountOrDealloc];
        _instance.addPhantom(jobj, false);
    }
    else {
        NSLog(@"Add Phantom scanned %p, %@", jobj, getARGCClass(jobj));
        //_instance.addStrongRef(jobj);
    }
    */
}

void ARGC::dealloc_now(JObj_p jobj) {
    _instance._cntDellocThread ++;
    _instance.addPhantom(jobj, true);
    _instance._cntDellocThread --;
}

void ARGC::scanInstances(id* pItem, int cntItem, JObj_p* scanBuff, JObj_p* pBuffEnd)
{
    
    JObj_p* pScanBuff = scanBuff;
    JObj_p debug_parent;
    for (int i = cntItem; --i >= 0; pItem++) {
        id oid = *pItem;
        JObj_p jobj = toARGCObject(oid);
        if (jobj == NULL || !markStrongReachable(jobj)) {
            continue;
        }

        *pScanBuff ++ = jobj;
        while (pScanBuff > scanBuff) {
            jobj = *--pScanBuff;
            Class cls = getARGCClass(jobj);
            if (cls == g_objectArrayClass) {
                ARGC::checkRefCount(jobj, @"mark__ scan");
                
                IOSObjectArray* array = (IOSObjectArray*)jobj;
                if (pScanBuff >= pBuffEnd) {
                    markArrayItems(array);
                }
                else {
                    scanInstances(array->buffer_, array->size_, pScanBuff, pBuffEnd);
                }
            }
            else {
                const scan_offset_t* scanOffsets = ARGC::getScanOffsets(cls);
                if (scanOffsets == NULL) {
                    continue;
                }
                debug_parent = jobj;
                for (scan_offset_t offset; (offset = *scanOffsets++) != 0; ) {
                    id oid = *((id*)jobj + offset);
                    JObj_p subObj = toARGCObject(oid);
                    if (subObj == NULL || !markStrongReachable(subObj))
                        continue;
                    
                    if (pScanBuff < pBuffEnd) {
                        *pScanBuff ++ = subObj;
                    }
                    else {
                        ARGC::checkRefCount(subObj, @"mark__ scan");
                        ARGC::markReachableSubInstances(subObj);
                    }
                }
            }
        }
    }
}

void ARGC::markReachableSubInstances(JObj_p jobj) {
    const int OBJ_STACK_SIZ = SCAN_BUFF_SIZE;
    JObj_p scanBuff[OBJ_STACK_SIZ];
    JObj_p* pBuffEnd = scanBuff + OBJ_STACK_SIZ;
    
    Class cls = getARGCClass(jobj);
    if (cls == g_objectArrayClass) {
        IOSObjectArray* array = (IOSObjectArray*)jobj;
        scanInstances(array->buffer_, array->size_, scanBuff, pBuffEnd);
    }
    else {
        const scan_offset_t* scanOffsets = ARGC::getScanOffsets(cls);
        assert (scanOffsets != NULL);
        scan_offset_t offset;
        while ((offset = *scanOffsets++) != 0) {
            id* field = ((id *)jobj + offset);
            scanInstances(field, 1, scanBuff, pBuffEnd);
        }
    }
}
    
void ARGC::markRootInstance(JObj_p jobj) {
    _instance._cntMarkingThread ++;
    //assert(gc_state >= SCANNING);
    if (markStrongReachable(jobj)) {
        markReachableSubInstances(jobj);
    }
    _instance._cntMarkingThread --;
}

void ARGC::markArrayItems(id oid) {
    _instance._cntMarkingThread ++;
    IOSObjectArray* array = (IOSObjectArray*)oid;
    const int OBJ_STACK_SIZ = SCAN_BUFF_SIZE;
    JObj_p scanBuff[OBJ_STACK_SIZ];
    JObj_p* pBuffEnd = scanBuff + OBJ_STACK_SIZ;
    scanInstances(array->buffer_, array->size_, scanBuff, pBuffEnd);
    _instance._cntMarkingThread --;
}

void ARGC::doClearReferences(NSPointerArray* refList, BOOL markSoftRef) {
    @synchronized ([IOSReference class]) {
        NSUInteger cntOrg = refList.count;
        int cntRef = (int)cntOrg;
        loop: for (int idx = cntRef; idx > 0; ) {
            id oid = (id)[refList pointerAtIndex:--idx];
            JObj_p jobj = toARGCObject(oid);

            if (jobj != NULL) {
                if (jobj->_rc.isReachable()) continue;
            }
            else {
                if (NSExtraRefCount(oid) > 0) continue;
            }

            NSPointerArray* rm = objc_getAssociatedObject(oid, &refAssocKey);
            if (markSoftRef) {
                int j = (int)rm.count;
                for (; --j >= 0; ) {
                    JavaLangRefReference* reference = (JavaLangRefReference*)[rm pointerAtIndex:j];
                    if (reference->_rc.isReachable()) {
                        if (jobj != NULL) {
                            markRootInstance(jobj);
                        }
                        break;
                    }
                }
                if (j >= 0) {
                    continue;
                }
            }
            
            for (int j = (int)rm.count; --j >= 0; ) {
                JavaLangRefReference* reference = (JavaLangRefReference*)[rm pointerAtIndex:j];
                if (reference != NULL) {
                    reference->referent_ = NULL;
                    [reference enqueue];
                    decreaseRefCount(reference, @"-c_rfc");
                }
            }
            
            if (jobj != NULL) {
                decreaseRefCount(jobj, @"-c_rf1");
            }
            else {
                [oid release];
                if (GC_DEBUG && GC_LOG_ALLOC) {
                    if (toARGCObject(oid) == NULL) {
                        NSLog(@"-c_rf2 %p #%d %@", oid, (int)NSExtraRefCount(oid), [oid class]);
                    }
                }
            }

            if (idx < --cntRef) {
                void* last = [refList pointerAtIndex:cntRef];
                [refList replacePointerAtIndex:idx withPointer:last];
            }
        }
        refList.count = cntRef;

    }

}


#define SLEEP_WHILE(cond) for (int cntSpin = 0; cond; ) { usleep(100); if (++cntSpin % 100 == 0) { NSLog(@"%s, %d", #cond, cntSpin); } }

void ARGC::doGC() {
    BOOL doClearSoftReference = _clearSoftReference;
    if (doClearSoftReference) {
        _clearSoftReference = 0;
    }
    else if (_cntRef == 0 || mayHaveGarbage <= 0) {
        return;
    }
    mayHaveGarbage --;
    NSMutableDictionary<Class, Counter*>* roots =
        (GC_LOG_ROOTS > 0 || GC_LOG_ALIVE > 0) ? [NSMutableDictionary new] : NULL;
    @synchronized (scanLock) {
        SLEEP_WHILE (_cntMarkingThread > 0);
        gc_state = SCANNING;
        RefContext::change_generation();
        //deallocMethod.exchange(dealloc_in_scan);
        deallocMethod.exchange(dealloc_in_collecting);
        SLEEP_WHILE (_cntDellocThread > 0);
        
        int idxSlot = 0;
        int cntRoot = 0;
        RefSlot* pScan = getRefSlot(0);
        JObj_p jobj;
        for (; idxSlot < _cntRef; idxSlot ++, pScan++) {
            if ((idxSlot % REFS_IN_BUCKET) == 0) {
                pScan = getRefSlot(idxSlot);
            }
            if ((jobj = *pScan) != NULL) {
                int idx = jobj->_rc.slotIndex();
                jobj->_rc.clearWeakReachable();
                assert(idx == idxSlot);
                if (jobj->_rc.isRootReachable()) {
                    ARGC::checkRefCount(jobj, @"##root");
                    markRootInstance(jobj);
                    if (GC_DEBUG) {
                        cntRoot ++;
                        if (GC_DEBUG && GC_LOG_ROOTS > 0) {
                            Counter* v = roots[getARGCClass(jobj)];
                            if (v == NULL) {
                                roots[getARGCClass(jobj)] = v = [Counter new];
                            }
                            v->count ++;
                        }
                    }
                }
            }
        }
        
        deallocMethod.exchange(dealloc_in_collecting);

        int cntScan = idxSlot;
        usleep(100);
        gc_state = FINALIZING;
        usleep(100);

        SLEEP_WHILE (_cntMarkingThread > 0);
        
        if (GC_DEBUG && GC_LOG_ROOTS > 0) {
            for (Class c in roots) {
                int cnt = roots[c]->count;
                if (cnt >= GC_LOG_ROOTS) {
                    NSLog(@"root %d %@", cnt, c);
                }
            }
            [roots removeAllObjects];
        }

        @autoreleasepool {
            for (idxSlot = 0; idxSlot < cntScan; idxSlot ++, pScan++) {
                if ((idxSlot % REFS_IN_BUCKET) == 0) {
                    pScan = getRefSlot(idxSlot);
                }
                jobj = *pScan;
                if (jobj != NULL && !jobj->_rc.isStrongReachable()) {
                    assert(!jobj->_rc.isRootReachable() || jobj->_rc.isPendingRelease());
                    //markRootInstance(jobj, false);
                    if (jobj->_rc.markFinalized()) {
                        JreFinalize(jobj);
                    }
                }
            }
        }

        doClearReferences(g_weakRefs, false);
        doClearReferences(g_softRefs, !doClearSoftReference);

        int cntAlive = 0;
        int cntAlivedGenerarion = 0;
        RefSlot* pAlive = _table[0];
        for (idxSlot = 0; true; idxSlot ++, pScan++) {
            if (GC_DEBUG && idxSlot == cntScan) {
                cntAlivedGenerarion = cntAlive;
            }
            
            if (idxSlot >= _cntRef) {
                @synchronized (tableLock) {
                    assert(idxSlot <= _cntRef);
                    if (idxSlot == _cntRef) {
                        _cntRef = cntAlive;
                        break;
                    }
                }
            }
            if ((idxSlot % REFS_IN_BUCKET) == 0) {
                pScan = getRefSlot(idxSlot);
            }
            
            if (idxSlot == cntAlive) {
                jobj = *pScan;
            }
            else {
                jobj = pScan->exchange(NULL);
            }
            
            if (jobj == NULL) {
                continue;
            }
            
            if (GC_DEBUG && roots != NULL) {
                Counter* v = roots[getARGCClass(jobj)];
                if (v == NULL) {
                    roots[getARGCClass(jobj)] = v = [Counter new];
                }
                v->count ++;
            }
            /*
             주의. 현재 jobj 가 다른 쓰레드에 의해 삭제될 수 있다.
             */
            if (!jobj->_rc.isStrongReachable()) {
                if (idxSlot == cntAlive) {
                    if (!pScan->compare_exchange_strong(jobj, NULL)) {
                        continue;
                    }
                }
                assert(!jobj->_rc.isRootReachable() || jobj->_rc.isPendingRelease());
                addPhantom(jobj, false);
//                @autoreleasepool {
//                    [jobj dealloc];
//                }
                continue;
            }
            
            if ((cntAlive % REFS_IN_BUCKET) == 0) {
                pAlive = getRefSlot(cntAlive);
            }
            if (idxSlot != cntAlive) {
                assert(*pAlive == NULL);
                *pAlive = jobj;
                jobj->_rc.setSlotIndex(cntAlive);
            }
            else {
                assert(*pAlive == jobj || *pAlive == NULL);
            }
            pAlive ++;
            cntAlive ++;
            ARGC::checkRefCount(jobj, @"alive in gc");
        }
        
        deallocMethod.exchange(dealloc_now);
        int alivePhantom = 0;
        for (int idxSlot = 0; true; idxSlot++) {
            if (idxSlot >= _cntPhantom) {
                @synchronized (tableLock) {
                    assert(idxSlot <= _cntPhantom);
                    if (idxSlot == _cntPhantom) {
                        _cntPhantom = alivePhantom;
                    }
                    break;
                }
            }
            RefSlot* pSlot = getPhantomSlot(idxSlot);
            jobj = *pSlot;
            if (jobj->_rc.isReachable()) {
                *getPhantomSlot(alivePhantom++) = jobj;
            }
            else {
                reclaimInstance(jobj);
            }
        }

        if (GC_DEBUG) {
            if (GC_DEBUG && GC_LOG_ALIVE > 0) {
                for (Class c in roots) {
                    int cnt = roots[c]->count;
                    if (cnt >= GC_LOG_ALIVE) {
                        NSLog(@"alive %d %@", cnt, c);
                    }
                }
                [roots release];
            }
            
            NSLog(@"scan: %d root: %d alive:%d/%d", cntScan, cntRoot, cntAlivedGenerarion, cntAlive);
        }
        else if (GC_LOG_LOOP) {
            NSLog(@"scan: %d", cntScan);
        }
    }
    

}


extern "C" {
    void ARGC_loopGC() {
        void (^SCAN)() = ^{
            if (ARGC::mayHaveGarbage > 0) {
                ARGC::_instance.doGC();
            }
            ARGC_loopGC();
        };
        
        if (gc_interval > 0) {
            dispatch_time_t next_t = dispatch_time(DISPATCH_TIME_NOW, gc_interval * NSEC_PER_SEC / 1000);
            dispatch_after(next_t, dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), SCAN);
        }
    }

    void ARGC_executeGC(int t) {  // deprecated.
        ARGC_setGarbageCollectionInterval(t);
    }
    void ARGC_setGarbageCollectionInterval(int t) {
        int64_t prev_interval = gc_interval;
        if (GC_DEBUG) {
            t /= 10;
        }
        gc_interval = t;
        if (prev_interval <= 0) {
            ARGC_loopGC();
            
        }
    }
    
    id JreLoadVolatileId(volatile_id *pVar);
    id JreAssignVolatileId(volatile_id *pVar, __unsafe_unretained id value);
    id JreVolatileStrongAssign(volatile_id *pIvar, __unsafe_unretained id value);
    bool JreCompareAndSwapVolatileStrongId(volatile_id *pVar, __unsafe_unretained id expected, __unsafe_unretained id newValue);
    id JreExchangeVolatileStrongId(volatile_id *pVar, __unsafe_unretained id newValue);
    void JreCloneVolatile(volatile_id *pVar, volatile_id *pOther);
    void JreCloneVolatileStrong(volatile_id *pVar, volatile_id *pOther);
    
    id JreRetainedWithAssign(id parent, __strong id *pIvar, __unsafe_unretained id value);
    id JreVolatileRetainedWithAssign(id parent, volatile_id *pIvar, __unsafe_unretained id value);
    void JreRetainedWithRelease(__unsafe_unretained id parent, __unsafe_unretained id child);
    void JreVolatileRetainedWithRelease(id parent, volatile_id *pVar);
    
    FOUNDATION_EXPORT void gc_trace(int64_t addr) {
        GC_TRACE_REF = (void*)addr;
        GC_LOG_ROOTS = 0;
        GC_LOG_ALLOC = 0;
        GC_LOG_RC = 0;
    }
    
    void ARGC_pubObject(id oid, id* ptr, id newValue) {
        if (class_isMetaClass([newValue class])) {
            JreExchangeVolatileStrongId(ptr, newValue);
        }
        else {
            ARGC_assignGenericObject(ptr, newValue);
        }
    }
    
    
    int ARGC_retainCount(id oid) {
        return (int)NSExtraRefCount(oid);
    }
    
    __attribute__((always_inline)) id ARGC_globalLock(id oid) {
        [oid retain];
        return oid;
    }
    
    id ARGC_globalUnlock(id oid) {
        if (GC_DEBUG && GC_LOG_ALLOC) {
            if (toARGCObject(oid) == NULL) {
                NSLog(@"--nstr %p #%d %@", oid, (int)NSExtraRefCount(oid), [oid class]);
            }
        }
        [oid autorelease];
        return oid;
    }
    
    void ARGC_strongRetain(id oid) {
        [oid retain];
    }

    void ARGC_release(id oid) {
        if (GC_DEBUG && GC_LOG_ALLOC) {
            if (toARGCObject(oid) == NULL) {
                NSLog(@"--nstr %p #%d %@", oid, (int)NSExtraRefCount(oid), [oid class]);
            }
        }
        [oid release];
    }

    void ARGC_autorelease(id oid) {
        if (GC_DEBUG && GC_LOG_ALLOC) {
            if (toARGCObject(oid) == NULL) {
                NSLog(@"--nstr %p #%d %@", oid, (int)NSExtraRefCount(oid), [oid class]);
            }
        }
        [oid autorelease];
    }
    
    void printRefCount(id oid) {
        JObj_p jobj = toARGCObject(oid);
        if (jobj != NULL) {
            NSLog(@"%p %d(%d) %@", jobj, (int)NSExtraRefCount(oid),
              jobj->_rc.bindCount(), [oid class]);
        }
        else {
            NSLog(@"%p %d(?) %@", jobj, (int)NSExtraRefCount(oid), [oid class]);
        }
    }
    
    void ARGC_strongRetainAutorelease(id oid) {
        if (oid) {
            [oid retain];
            [oid autorelease];
        }
    }
    
    void ARGC_genericRetain(id oid) {
        if (oid == NULL) return;

        JObj_p jobj = toARGCObject(oid);
        if (jobj != NULL) {
            ARGC::touchInstance(jobj);
            ARGC::increaseReferenceCount(jobj, @"++genr");
        }
        else {
            [oid retain];
        }
    }

    void ARGC_genericRelease(id oid) {
        if (oid == NULL) return;

        JObj_p jobj = toARGCObject(oid);
        if (jobj != NULL) {
            ARGC::decreaseRefCountOrDealloc(jobj, @"--genr");
        }
        else {
            if (GC_DEBUG && GC_LOG_ALLOC) {
                if (toARGCObject(oid) == NULL) {
                    NSLog(@"--nstr %p #%d %@", oid, (int)NSExtraRefCount(oid), [oid class]);
                }
            }
            [oid release];
        }
    }
    
    id ARGC_allocateObject(Class cls, NSUInteger extraBytes, NSZone* zone) {
        return ARGC::_instance.allocateInstance(cls, extraBytes, zone);
    }
    
#include "Volatiles.mm"
}


@implementation NSObject(ARGCObject)

- (JObj_p) toJObject {
    return NULL;
}

- (void) forEachObjectField: (ARGCObjectFieldVisitor) visitor inDepth:(int) depth {
    // do nothing
}
@end



@interface IOSObjectArray(ARGCObject)
@end
@implementation IOSObjectArray(ARGCObject)
- (void) forEachObjectField: (ARGCObjectFieldVisitor) visitor inDepth:(int) depth {
    ARGC_FIELD_REF id*pItem = buffer_ + 0;
    for (int i = size_; --i >= 0; ) {
        ARGC_FIELD_REF id oid = *pItem++;
        if (oid != NULL) {
            visitor(oid, depth);
        }
    }
    
}

- (JObj_p) toJObject
{
    return _rc.isPhantom() ? NULL : self;
}

@end




@interface IOSReference() {
}
@end

@implementation IOSReference

+ (void)initReferent:(JavaLangRefReference *)reference withReferent:(id)oid
{
    if (oid == nil) return;

    NSPointerArray* array_;
    NSString* tag;
    if ([reference isKindOfClass:[JavaLangRefSoftReference class]]) {
        array_ = g_softRefs;
        tag = @"SoftRef";
    }
    else {
        array_ = g_weakRefs;
        tag = @"WeakRef";
    }
    @synchronized (self) {
        reference->referent_ = oid;
        NSPointerArray* rm = objc_getAssociatedObject(oid, &refAssocKey);
        if (rm == NULL) {
            rm = [[NSPointerArray alloc] initWithOptions:NSPointerFunctionsOpaqueMemory];
            objc_setAssociatedObject(oid, &refAssocKey, rm, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
            ARGC::increaseReferenceCount(oid, @"+ireft");
            [array_ addPointer:oid];
        }
        //DBGLog(@"Add ref: %p: %p %@", rm, oid, getARGCClass(jobj));
        [rm addPointer:(void*)reference];
        ARGC::increaseReferenceCount(reference, @"+irefc");
    }
}
+ (id)getReferent:(JavaLangRefReference *)reference
{
    return (id)reference->referent_;
}

+ (void)clearReferent:(JavaLangRefReference *)reference
{
    id oid = (id)reference->referent_;
    if (oid == NULL) return;
    
    @synchronized (self) {
        reference->referent_ = NULL;
        NSPointerArray* rm = objc_getAssociatedObject(oid, &refAssocKey);
        if (rm == NULL) return;

        for (NSUInteger idx = rm.count; --idx >= 0; ) {
            if ([rm pointerAtIndex:idx] == reference) {
                [rm removePointerAtIndex:idx];
                ARGC::decreaseRefCount(reference, @"-clrRf");
                break;
            }
        }
    }

    //ARGC::checkRefCount(oid, @"ClearRef");
}

+ (void)handleMemoryWarning:(NSNotification *)notification {
    ARGC::_clearSoftReference = 1;
    ARGC_collectGarbage();
}


@end

@implementation ARGCPhantom
@end
@implementation Counter
@end
@implementation ScanOffsetArray
@end


