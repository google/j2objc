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
#import "../IOSClass.h"
#import "../IOSReference.h"
#include "java/lang/ref/Reference.h"
#include "java/lang/ref/SoftReference.h"
#include "java/lang/ref/WeakReference.h"
#include "java/lang/ref/ReferenceQueue.h"
#import <atomic>

#define GC_DEBUG 1

#if !GC_DEBUG
#pragma GCC optimize ("O2")
#pragma clang optimize on
#else
#pragma clang optimize off
#endif

static const int MAX_TOUCH_DEPTH = 512;
static const int SCAN_BUFF_SIZE = 4096;

FOUNDATION_EXPORT int GC_LOG_LOOP = GC_DEBUG;
FOUNDATION_EXPORT int GC_LOG_RC = 0;
FOUNDATION_EXPORT int GC_LOG_ROOTS = 0;
FOUNDATION_EXPORT int GC_LOG_ALIVE = 0;
FOUNDATION_EXPORT int GC_LOG_ALLOC = 0;
FOUNDATION_EXPORT int GC_LOG_WEAK_SOFT_REF = 1;
FOUNDATION_EXPORT void* GC_TRACE_REF = (void*)-1;
FOUNDATION_EXPORT void* GC_TRACE_CLASS = (void*)-1;
#define GC_TRACE(obj, flag) (GC_DEBUG && (obj == GC_TRACE_REF || (flag) || [obj class] == GC_TRACE_CLASS))


typedef uint16_t scan_offset_t;
typedef ARGCObject* ObjP;
typedef std::atomic<ObjP> RefSlot;

static const BOOL ENABLE_DELAY_FINALIZE = false;
static const BOOL ENABLE_PENDING_RELEASE = false;
static const int _1M = 1024*1024;
static const int REFS_IN_BUCKET = _1M / sizeof(ObjP);

static const int64_t BIND_COUNT_MASK        = 0x00fffFFFF;
static const int64_t PHANTOM_FLAG           = 0x02000LL * 0x10000;
static const int64_t FINALIZED_FLAG         = 0x04000LL * 0x10000;
static const int64_t WEAK_REACHABLE_FLAG    = 0x08000LL * 0x10000;
static const int64_t STRONG_REACHABLE_MASK  = 0x70000LL * 0x10000;
static const int64_t PENDING_RELEASE_FLAG   = 0x80000LL * 0x10000;
static const int64_t REACHABLE_MASK     = WEAK_REACHABLE_FLAG | STRONG_REACHABLE_MASK;
static const int SLOT_INDEX_SHIFT = (32+4);
static const int64_t SLOT_INDEX_MASK = (uint64_t)-1 << SLOT_INDEX_SHIFT;


extern "C" {
    //void clearReclaimingReference(__unsafe_unretained id referent);
    void JreFinalize(id self);
};

class RefContext {
public:
    void initialize(BOOL isFinalized) {
        /**
         objc 의 refCount 의 초기값은 0이며, 0보다 작아질 때 객체를 릴리즈한다.
         bindCount의 초기값은 1이며, stack/static 참조가 없을 때 0이 된다.
         */
        uint64_t v = SLOT_INDEX_MASK | strong_reachable_generation_bit | 1;
        if (isFinalized) {
            v |= FINALIZED_FLAG;
        }
        _gc_context = v;
    }
    
    int32_t slotIndex() {
        return _gc_context >> SLOT_INDEX_SHIFT;
    }
    
    void setSlotIndex(int64_t index) {
        index <<= SLOT_INDEX_SHIFT;
        while (true) {
            int64_t v = _gc_context;
            int64_t new_v = (v & ~SLOT_INDEX_MASK) | index;
            if (_gc_context.compare_exchange_strong(v, new_v)) {
                return;
            }
        }
    }

    void updateSlotIndex(int64_t index) {
        index <<= (32+4);
        while (true) {
            int64_t v = _gc_context;
            int64_t new_v = (v & ~((uint64_t)-1 << (32+4))) | index;
            if (_gc_context.compare_exchange_strong(v, new_v)) {
                return;
            }
        }
    }
    

    int32_t bindCount() {
        return _gc_context & BIND_COUNT_MASK;
    }
    
    void bind() {
        _gc_context ++;
        if (GC_DEBUG) {
            assert((_gc_context & BIND_COUNT_MASK)  < BIND_COUNT_MASK);
        }
    }
    
    int32_t unbind() {
        int32_t cnt = (--_gc_context) & BIND_COUNT_MASK;
        if (GC_DEBUG) {
            assert(cnt < BIND_COUNT_MASK);
        }
        return cnt;
    }
    

    BOOL isReachable() {
        return __isReachable(_gc_context);
    }

    BOOL isStrongReachable() {
        return __isStrongReachable(_gc_context);
    }
    
    BOOL isUntouchable() {
        return (_gc_context & REACHABLE_MASK) == 0;
    }

    BOOL markPendingRelease() {
        return this->markFlags(PENDING_RELEASE_FLAG);
    }

    BOOL isPendingRelease() {
        return (_gc_context & PENDING_RELEASE_FLAG) != 0;
    }
    
    BOOL markUntouchable() {
        return this->clearFlags(REACHABLE_MASK);
    }
    
    BOOL isFinalized() {
        return (_gc_context & FINALIZED_FLAG) != 0;
    }
    
//    void clearFinalized() {
//        this->clearFlags(FINALIZED_FLAG);
//    }

    BOOL markFinalized() {
        return this->markFlags(FINALIZED_FLAG);
    }
    
    BOOL markPhantom() {
        return this->markFlags(PHANTOM_FLAG);
    }

    BOOL isPhantom() {
        return (_gc_context & PHANTOM_FLAG) != 0;
    }

    BOOL markReachable(BOOL isStrong) {
        return isStrong ? markStrongReachable() : markWeakReachable();
    }
    
    BOOL markWeakReachable() {
        while (true) {
            int64_t v = _gc_context;
            if (__isReachable(v)) {
                return false;
            }
            int64_t new_v = v | WEAK_REACHABLE_FLAG;
            if (_gc_context.compare_exchange_strong(v, new_v)) {
                return true;
            }
        }
    }
    
    void clearWeakReachable() {
        this->clearFlags(WEAK_REACHABLE_FLAG);
    }

    BOOL markStrongReachable() {
        if (GC_DEBUG && this->isUntouchable()) {
            NSLog(@"mark on untouchable");
        }
        
        while (true) {
            int64_t v = _gc_context;
            if (__isStrongReachable(v)) {
                return false;
            }
            int64_t new_v = (v & ~REACHABLE_MASK) | strong_reachable_generation_bit;
            if (_gc_context.compare_exchange_strong(v, new_v)) {
                return true;
            }
        }
    }
    


private:
    BOOL markFlags(int64_t flag) {
        while (true) {
            int64_t v = _gc_context;
            if ((v & flag) != 0) {
                return false;
            }
            int64_t new_v = v | flag;
            if (_gc_context.compare_exchange_strong(v, new_v)) {
                return true;
            }
        }
    }

    BOOL clearFlags(int64_t flag) {
        while (true) {
            int64_t v = _gc_context;
            if ((v & flag) == 0) {
                return false;
            }
            int64_t new_v = v & ~flag;
            if (_gc_context.compare_exchange_strong(v, new_v)) {
                return true;
            }
        }
    }

private:

    static BOOL __isReachable(int64_t v) {
        return (v & (WEAK_REACHABLE_FLAG | strong_reachable_generation_bit)) != 0;
    }
    
    static BOOL __isStrongReachable(int64_t v) {
        return (v & strong_reachable_generation_bit) != 0;
    }


    std::atomic<int64_t> _gc_context;
    static volatile int64_t strong_reachable_generation_bit;

public:
    static void change_generation() {
        strong_reachable_generation_bit = (strong_reachable_generation_bit << 1) & STRONG_REACHABLE_MASK;
        if (strong_reachable_generation_bit == 0) {
            strong_reachable_generation_bit = ((STRONG_REACHABLE_MASK << 1) ^ STRONG_REACHABLE_MASK) & STRONG_REACHABLE_MASK;
        }
    }
    
};

volatile int64_t RefContext::strong_reachable_generation_bit = 0;



@interface ARGCObject()
{
@public
    RefContext _gc_info;
}
- (void) forEachObjectField: (ARGCObjectFieldVisitor) visitor inDepth:(int) depth;
@end

@interface ARGCPhantom : NSObject
{
@public
    ARGCPhantom* _next;
}
@end
@implementation ARGCPhantom
@end

@interface Counter : NSObject
{
@public
    int count;
}
@end
@implementation Counter
@end

@interface ScanOffsetArray : NSObject
{
@public
    scan_offset_t offsets[1];
}
@end
@implementation ScanOffsetArray
@end

static const int USE_AUTORELEASE_OLD = 0;


//##############################################################

static const int SCANNING = 1;
static const int FINALIZING = 2;
static const int FINISHED = 0;
static Class JavaUtilHashMap_KeySet_CLASS = 0;
static NSPointerArray* g_softRefs;
static NSPointerArray* g_weakRefs;

class ARGC {
public:
    typedef void (*ARGCVisitor)(ObjP obj);
    
    static ARGC _instance;
    static std::atomic_int gc_state;//GC_IN_SCANNING;
    std::atomic<ARGCVisitor> deallocMethod;

    Method _no_java_finalize;

    ARGC() {
        this->_inGC = false;
        g_softRefs = [[NSPointerArray alloc] initWithOptions:NSPointerFunctionsOpaqueMemory];
        g_weakRefs = [[NSPointerArray alloc] initWithOptions:NSPointerFunctionsOpaqueMemory];

        RefContext::change_generation();
        JavaLangRefReference_class = objc_lookUpClass("JavaLangRefReference");
        JavaUtilHashMap_KeySet_CLASS = objc_lookUpClass("JavaUtilHashMap_KeySet");
        GC_TRACE_CLASS = objc_lookUpClass("JavaUtilConcurrentLocksAbstractQueuedSynchronizer");
        obj_array_class = [IOSObjectArray class];
        deallocMethod = dealloc_now;
        _no_java_finalize = class_getInstanceMethod([NSObject class], @selector(java_finalize));

        _scanOffsetCache = [[NSMutableDictionary alloc] init];
        _finalizeClasses = [[NSMutableDictionary alloc] init];
        tableLock = [[NSObject alloc]init];
        scanLock = [[NSObject alloc]init];
        argcObjectSize = class_getInstanceSize([ARGCObject class]) + sizeof(void*);
        ARGC_setGarbageCollectionInterval(1000);
    }
    
    int size() {
        return _cntRef;
    }
    
    void doGC();

    void doClearReferences(NSPointerArray* refList);
    

    static void markRootInstance(ObjP obj, BOOL isStrong);
    
    static void markArrayItems(id array, BOOL isStrong);
    
    static void scanInstances(id* pItem, int cntItem, ObjP* scanBuff, ObjP* pBuffEnd, BOOL isStrong);
    
    static void touchInstance(ObjP obj) {
        if (!obj->_gc_info.isStrongReachable()) {
            markRootInstance(obj, true);
        }
    }
    
    static BOOL isPhantom(ObjP obj) {
        return obj->_gc_info.isPhantom();
    }

    static BOOL isRootReachable(ObjP obj) {
        return obj->_gc_info.bindCount() > 0;
    }
    
    static BOOL markPendingRelease(ObjP obj) {
        if (!ENABLE_PENDING_RELEASE || NSExtraRefCount(obj) != 0) {
            return false;
        }
        return obj->_gc_info.markPendingRelease();
    }

    static void bindObject(ObjP obj) {
        if (!obj->_gc_info.isPhantom()) {
            obj->_gc_info.bind();
            touchInstance(obj);
            ARGC::increaseReferenceCount(obj, @"retain");
        }
        else {
            ARGC::increaseReferenceCount(obj, @"retain-phatom");
        }
    }
    
    static void unbindObject(ObjP obj) {
        if (obj->_gc_info.isPhantom()) {
            NSLog(@"release-phantom: %p %@", obj, [obj class]);
            NSDecrementExtraRefCountWasZero(obj);
            return;
        }

        int cnt = obj->_gc_info.unbind();
        
        if (ARGC::decreaseReferenceCount(obj, @"release")) {
            if (!ARGC::isRootReachable(obj)) {
                ARGC::mayHaveGarbage = 2;
            }
        }
        return;
    }
    
    static BOOL inGC() {
        return _instance._inGC;
    }
    
    static BOOL isJavaObject(id obj) {
        return [obj isKindOfClass:[ARGCObject class]];
    }
    
    ObjP allocateInstance(Class cls, NSUInteger extraBytes, NSZone* zone) {
        size_t instanceSize = class_getInstanceSize(cls);
        if (instanceSize < argcObjectSize) {
            extraBytes = argcObjectSize - instanceSize;
        }
        
        ObjP obj = (ObjP)NSAllocateObject(cls, extraBytes, zone);
        if (GC_TRACE(obj, (GC_LOG_ALLOC || GC_LOG_RC))) {
            NSLog(@"alloc %p %@", obj, cls);
        }
        registerScanOffsets(cls);
        initGCInfo(obj);
        _cntAllocated ++;
        // bindCount 0 인 상태, 즉 allocation 직후에도 stack에서 참조가능한 상태이다.
        addStrongRef(obj);
        return obj;
    }
    
    void initGCInfo(ObjP obj) {
        obj->_gc_info.initialize(_finalizeClasses[[obj class]] == NULL);
    }
    
    
    static void increaseReferenceCount(id obj, NSString* tag) {
        NSIncrementExtraRefCount(obj);
        checkRefCount(obj, tag);
    }

    static void decreaseGenericReferenceCount(id obj) {
        if (isJavaObject(obj)) {
            if (GC_TRACE(obj, 0)) {
                decreaseReferenceCount((ObjP)obj, @"--.fld");
            }
            else {
                decreaseReferenceCount((ObjP)obj, @"--.fld");
            }
        }
        else {
            if (GC_TRACE(obj, 0)) {
                NSLog(@"--natv %p #%d %@", obj, (int)NSExtraRefCount(obj), [obj class]);
            }
            [obj release];
        }
    }
    
    static BOOL decreaseReferenceCount(ObjP obj, NSString* tag) {
        if (!NSDecrementExtraRefCountWasZero(obj)) {
            checkRefCount(obj, tag);
            return true;
        }

        if (GC_TRACE(obj, 0)) {
            NSLog(@"--zero %p -1(%d) %@", obj, obj->_gc_info.bindCount(), [obj class]);
        }
        if (ENABLE_DELAY_FINALIZE && !obj->_gc_info.isFinalized()) {
            // dealloc in next GC cycle.
            return false;
        }
        if (gc_state != SCANNING) {
//            if (gc_state == SCANNING && !obj->_gc_info.isReachable()) {
//                /* do not dealloc sub-references. */
//                markRootInstance(obj, false);
//            }
            [obj dealloc];
        }
        return false;
    }
    
    static void decreaseReferenceCountAndPublish(ObjP obj) {
        if (USE_AUTORELEASE_OLD) {
            ARGC::bindObject(obj);
            [obj autorelease];
        }
        else {
            if (decreaseReferenceCount(obj, @"--&pub")) {
                touchInstance(obj);
                // 다음 gc 를 위해서.
                mayHaveGarbage = 2;
            }
        }
    }
    
    static const scan_offset_t* getScanOffsets(Class clazz) {
        ScanOffsetArray* scanOffsets = objc_getAssociatedObject(clazz, &_instance);
        if (scanOffsets == NULL) {
            return NULL;
        }
        return scanOffsets->offsets;
    }
    
    static void checkRefCount(ObjP obj, NSString* tag) {
        if (GC_TRACE(obj, GC_LOG_RC)) {
            int bindCount = obj->_gc_info.bindCount();
            int refCount = (int)NSExtraRefCount(obj)+1;
            NSLog(@"%@ %p #%d+%d %@", tag, obj, bindCount, refCount-bindCount, [obj class]);
            if ((int)NSExtraRefCount(obj) < (int)obj->_gc_info.bindCount() - 1) {
                // 멀티쓰레드환경이라 두 값의 비교가 정확하지 않다. 관련 정보를 적는다.
                int MAX_THREAD_COUNT = 32;
                NSLog(@"Maybe error %@ %p %d(%d) %@", tag, obj, (int)NSExtraRefCount(obj), obj->_gc_info.bindCount(), [obj class]);
                //assert((int)NSExtraRefCount(obj) >= (int)obj->_gc_info.bindCount() - MAX_THREAD_COUNT);
            };
        }
    }
    
private:
    static void dealloc_in_collecting(ObjP obj);

    static void dealloc_in_scan(ObjP obj);
    
    static void dealloc_now(ObjP obj);
    
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
    
    BOOL unregisterRef(ObjP obj) {
        int idxSlot = obj->_gc_info.slotIndex();
        RefSlot* pSlot = getRefSlot(idxSlot, false);
        if (pSlot->compare_exchange_strong(obj, NULL)) {
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
    
    void addStrongRef(ObjP obj) {
        @synchronized (tableLock) {
            int index = _cntRef;
            obj->_gc_info.setSlotIndex(index);
            
            int idxBucket = index / REFS_IN_BUCKET;
            int idxRef = index % REFS_IN_BUCKET;
            RefSlot* row = _table[idxBucket];
            if (row == NULL) {
                row = _table[idxBucket];
                if (row == NULL) {
                    row = _table[idxBucket] = newBucket();
                }
            }
            row[idxRef] = obj;
            // 반드시 obj 추가후 _cntRef 증가.
            _cntRef ++;
        }
    }
    
    void reclaimInstance(ObjP obj) {
        if (GC_TRACE(obj, GC_LOG_ALLOC)) {
            NSLog(@"dealloc: %p %@", obj, [obj class]);
        }
        NSDeallocateObject(obj);
        _cntDeallocated ++;
        mayHaveGarbage = 2;
    }
    
    void addPhantom(ObjP obj, BOOL isThreadSafe) {
        if (obj->_gc_info.markFinalized()) {
            JreFinalize(obj);
            if (obj->_gc_info.isStrongReachable()) {
                return;
            }
        }
        if (!obj->_gc_info.markPhantom()) {
            return;
        }

        [obj forEachObjectField:(ARGCObjectFieldVisitor)decreaseGenericReferenceCount inDepth: 0];
        _instance.unregisterRef(obj);

        if (GC_TRACE(obj, GC_LOG_ALLOC)) {
            NSLog(@"addPhantom: %p %@ %@", obj, [obj class], isThreadSafe ? @"delete-now" : @"delete-later");
        }

        if (isThreadSafe) {
            assert(NSExtraRefCount(obj) == 0);
            reclaimInstance(obj);
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
                row[idxRef] = obj;
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
    Class JavaLangRefReference_class;
    NSObject* tableLock;
    NSObject* scanLock;
    Class obj_array_class;
    
    BOOL _inGC;
public:
    static int mayHaveGarbage;
};


ARGC ARGC::_instance;
int ARGC::mayHaveGarbage = 0;
static int assocKey;
std::atomic_int ARGC::gc_state;
static scan_offset_t _emptyFields[1] = { 0 };
static int64_t gc_interval = 0;


@implementation ARGCObject

+ (const scan_offset_t*) scanOffsets
{
    return _emptyFields;
}

+ (instancetype)alloc
{
    id obj = ARGC::_instance.allocateInstance(self, 0, NULL);
    return obj;
}

+ (instancetype)allocWithZone:(struct _NSZone *) zone
{
    /**
     allocWithZone:zone is deprecated. The method ignores 'zone' argument.
     So [[cls allocWithZone:myZone] zone] is not equals to myZone;
     */
    id obj = ARGC::_instance.allocateInstance(self, 0, zone);
    return obj;
}


- (BOOL) isReachable:(BOOL)isStrong
{
    return isStrong ? self->_gc_info.isStrongReachable() : self->_gc_info.isReachable() ;
}

- (void) markARGCFields:(BOOL)isStrong
{
    ARGC::markRootInstance(self, isStrong);
}

- (ObjP) toARGCObject
{
    return ARGC::isPhantom(self) ? NULL : self;
}


#pragma clang diagnostic ignored "-Wobjc-missing-super-calls"
- (void)dealloc
{
    ARGC::ARGCVisitor fn = ARGC::_instance.deallocMethod;
    fn(self);
}

- (instancetype)retain
{
    ARGC::bindObject(self);
    return self;
}

//
//- (BOOL)retainWeakReference
//{
//    if ([super retainWeakReference]) {
//        // retainWeakReference 함수 수행 중에는 NSLog 등 다른 API 호출이 금지된 것으로 보인다.
//        if (self->_gc_info.isStrongReachable()) {
//            self->_gc_info.bind();
//        }
//        else if (ARGC::gc_state == SCANNING) {
//            ARGC::bindObject(self);
//        }
//        else {
//            //NSLog(@"retainWeakReference on unreachable %p", self);
//            return false;
//        }
//        //NSLog(@"retainWeakReference on %p", self);
//        return true;
//    }
//    return false;
//}

- (oneway void)release
{
//    if (ARGC::markPendingRelease(self)) {
//        if (GC_TRACE(self, 0)) {
//            ARGC::checkRefCount(self, @"autorelease on markPendingRelease");
//        }
//        [super autorelease];
//        return;
//    }
    ARGC::unbindObject(self);
}

- (instancetype)autorelease
{
    [super autorelease];
    if (GC_TRACE(self, 0)) {
        ARGC::checkRefCount(self, @"autorelease");
    }
    return self;
}

- (void) forEachObjectField: (ARGCObjectFieldVisitor) visitor inDepth:(int) depth
{
    const scan_offset_t* scanOffsets = ARGC::getScanOffsets([self class]);
    while (true) {
        scan_offset_t offset = *scanOffsets++;
        if (offset == 0) {
            break;
        }
        id ref = *((id *)self + offset);
        ObjP obj = [ref toARGCObject];
        if (obj != NULL) {
            visitor(obj, depth);
        }
    }
}

@end


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
    BOOL referentSkipped = false;
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
                if (cls == JavaLangRefReference_class) {
                    //NSLog(@"Refernce.%s field check", ivar_getName(ivar));
                    //referent 가 volatile_id(uint_ptr)롤 처리되어 검사 불필요.;
                    //continue;
                }
                ptrdiff_t offset = ivar_getOffset(ivar);
                _offset_buf[cntObjField] = offset / sizeof(ObjP);
                id obj = [[[NSObject alloc] retain] retain];
                __unsafe_unretained id* pField = (__unsafe_unretained id *)((char *)clone + offset);
                _test_obj_buf[cntObjField] = *pField = obj;
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
    
    std::atomic<ObjP>* field = (std::atomic<ObjP>*)pField;
    ObjP oldValue = field->exchange(newValue);
    if (oldValue == newValue) {
        return;
    }
    if (newValue != NULL) {
        [newValue retain];
    }
    if (oldValue != NULL) {
        [oldValue release];
    }
    return;
}

void ARGC_assignARGCObject(ARGC_FIELD_REF id* pField, __unsafe_unretained id newValue) {
    if (*pField == newValue) {
        return;
    }
    
    std::atomic<ObjP>* field = (std::atomic<ObjP>*)pField;
    ObjP oldValue = field->exchange(newValue);
    if (oldValue == newValue) {
        return;
    }
    if (newValue != NULL) {
        assert(ARGC::isJavaObject(newValue));
        ARGC::increaseReferenceCount(newValue, @"++argc");
        ARGC::touchInstance(newValue);
    }
    if (oldValue != NULL) {
        assert(ARGC::isJavaObject(oldValue));
        ARGC::decreaseReferenceCountAndPublish(oldValue);
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
    if (newValue != NULL) {
        if (ARGC::isJavaObject(newValue)) {
            ARGC::touchInstance(newValue);
            ARGC::increaseReferenceCount(newValue, @"++genr");
        }
        else {
            [newValue retain];
        }
    }
    
    
    if (oldValue != NULL) {
        if (ARGC::isJavaObject(oldValue)) {
            ARGC::decreaseReferenceCountAndPublish(oldValue);
        }
        else if (USE_AUTORELEASE_OLD) {
            [oldValue autorelease];
        }
        else {
            [oldValue release];
        }
    }
    return;
}



void ARGC_collectGarbage() {
    ARGC::_instance.doGC();
}

void ARGC::dealloc_in_collecting(ObjP obj) {
    if (GC_TRACE(obj, GC_LOG_ALLOC)) {
        NSLog(@"dealloc-in-collecting: %p %@", obj, [obj class]);
    }
    
    _instance.addPhantom(obj, false);
}

void ARGC::dealloc_in_scan(ObjP obj) {
    /*
     Do nothing
    if (ARGC::_instance.unregisterRef(obj)) {
        [obj forEachObjectField:(ARGCObjectFieldVisitor)decreaseGenericReferenceCount];
        _instance.addPhantom(obj, false);
    }
    else {
        NSLog(@"Add Phantom scanned %p, %@", obj, [obj class]);
        //_instance.addStrongRef(obj);
    }
    */
}

void ARGC::dealloc_now(ObjP obj) {
    _instance._cntDellocThread ++;
    _instance.addPhantom(obj, true);
    _instance._cntDellocThread --;
}

void ARGC::scanInstances(id* pItem, int cntItem, ObjP* scanBuff, ObjP* pBuffEnd, BOOL isStrong)
{
    
    ObjP* pScanBuff = scanBuff;
    for (int i = cntItem; --i >= 0; pItem++) {
        ObjP obj = *pItem;
        if (obj == NULL) {
            continue;
        }
        
        *pScanBuff ++ = obj;
        while (pScanBuff > scanBuff) {
            obj = *--pScanBuff;
            Class cls = [obj class];
            if (cls == _instance.obj_array_class) {
                if (!obj->_gc_info.markReachable(isStrong)) {
                    continue;
                }
                ARGC::checkRefCount(obj, @"mark__ scan");
                
                IOSObjectArray* array = (IOSObjectArray*)obj;
                if (pScanBuff >= pBuffEnd) {
                    markArrayItems(array, isStrong);
                }
                else {
                    scanInstances(array->buffer_, array->size_, pScanBuff, pBuffEnd, isStrong);
                }
            }
            else {
                const scan_offset_t* scanOffsets = ARGC::getScanOffsets(cls);
                if (scanOffsets == NULL) {
                    continue;
                }
                if (!obj->_gc_info.markReachable(isStrong)) {
                    continue;
                }
                for (scan_offset_t offset; (offset = *scanOffsets++) != 0; ) {
                    id field = *((id *)obj + offset);
                    if (field) {
                        if (pScanBuff >= pBuffEnd) {
                            [field markARGCFields:isStrong];
                        }
                        else if (![field isReachable:isStrong]) {
                            ARGC::checkRefCount((ObjP)field, @"mark__ scan");
                            *pScanBuff ++ = field;
                        }
                    }
                }
            }
        }
    }
}

void ARGC::markRootInstance(ObjP obj, BOOL isStrong) {
    _instance._cntMarkingThread ++;
    //assert(gc_state >= SCANNING);
    if (obj->_gc_info.markReachable(isStrong)) {
        
        const int OBJ_STACK_SIZ = SCAN_BUFF_SIZE;
        ObjP scanBuff[OBJ_STACK_SIZ];
        ObjP* pBuffEnd = scanBuff + OBJ_STACK_SIZ;
        
        Class cls = [obj class];
        if (cls == _instance.obj_array_class) {
            IOSObjectArray* array = (IOSObjectArray*)obj;
            scanInstances(array->buffer_, array->size_, scanBuff, pBuffEnd, isStrong);
        }
        else {
            const scan_offset_t* scanOffsets = ARGC::getScanOffsets(cls);
            assert (scanOffsets != NULL);
            scan_offset_t offset;
            while ((offset = *scanOffsets++) != 0) {
                id* field = ((id *)obj + offset);
                scanInstances(field, 1, scanBuff, pBuffEnd, isStrong);
            }
        }
    }
    _instance._cntMarkingThread --;
}

void ARGC::markArrayItems(id obj, BOOL isStrong) {
    _instance._cntMarkingThread ++;
    IOSObjectArray* array = (IOSObjectArray*)obj;
    const int OBJ_STACK_SIZ = SCAN_BUFF_SIZE;
    ObjP scanBuff[OBJ_STACK_SIZ];
    ObjP* pBuffEnd = scanBuff + OBJ_STACK_SIZ;
    scanInstances(array->buffer_, array->size_, scanBuff, pBuffEnd, isStrong);
    _instance._cntMarkingThread --;
}

void ARGC::doClearReferences(NSPointerArray* refList) {
    @synchronized ([IOSReference class]) {
        NSUInteger cntOrg = refList.count;
        int cntRef = (int)cntOrg;
        for (int idx = cntRef; idx > 0; ) {
            id obj = (id)[refList pointerAtIndex:--idx];
            NSPointerArray* rm = objc_getAssociatedObject(obj, &assocKey);

            if (isJavaObject(obj)) {
                if (((ObjP)obj)->_gc_info.isReachable()) continue;
                NSDecrementExtraRefCountWasZero(obj);
            }
            else {
                if (NSExtraRefCount(obj) > 0) continue;
                [obj release];
            }

            //objc_setAssociatedObject(obj, &assocKey, NULL, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
            for (int j = (int)rm.count; --j >= 0; ) {
                JavaLangRefReference* reference = (JavaLangRefReference*)[rm pointerAtIndex:j];
                if (reference != NULL) {
                    reference->referent_ = NULL;
                    [reference enqueue];
                    NSDecrementExtraRefCountWasZero(reference);
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
    if (_cntRef == 0 || mayHaveGarbage <= 0) {
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
        RefSlot* pScan;
        ObjP obj;
        for (; idxSlot < _cntRef; idxSlot ++, pScan++) {
            if ((idxSlot % REFS_IN_BUCKET) == 0) {
                pScan = getRefSlot(idxSlot);
            }
            if ((obj = *pScan) != NULL) {
                int idx = obj->_gc_info.slotIndex();
                obj->_gc_info.clearWeakReachable();
                assert(idx == idxSlot);
                if (isRootReachable(obj)) {
                    ARGC::checkRefCount(obj, @"##root");
                    markRootInstance(obj, true);
                    if (GC_DEBUG) {
                        cntRoot ++;
                        if (GC_DEBUG && GC_LOG_ROOTS > 0) {
                            Counter* v = roots[[obj class]];
                            if (v == NULL) {
                                roots[[obj class]] = v = [Counter new];
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
                obj = *pScan;
                if (obj != NULL && !obj->_gc_info.isStrongReachable()) {
                    assert(!isRootReachable(obj) || obj->_gc_info.isPendingRelease());
                    //markRootInstance(obj, false);
                    if (obj->_gc_info.markFinalized()) {
                        JreFinalize(obj);
                    }
                }
            }
        }

        doClearReferences(g_weakRefs);


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
                obj = *pScan;
            }
            else {
                obj = pScan->exchange(NULL);
            }
            
            if (obj == NULL) {
                continue;
            }
            
            if (GC_DEBUG && roots != NULL) {
                Counter* v = roots[[obj class]];
                if (v == NULL) {
                    roots[[obj class]] = v = [Counter new];
                }
                v->count ++;
            }
            /*
             주의. 현재 obj 가 다른 쓰레드에 의해 삭제될 수 있다.
             */
            if (!obj->_gc_info.isStrongReachable()) {
                if (idxSlot == cntAlive) {
                    if (!pScan->compare_exchange_strong(obj, NULL)) {
                        continue;
                    }
                }
                assert(!isRootReachable(obj) || obj->_gc_info.isPendingRelease());
                addPhantom(obj, false);
//                @autoreleasepool {
//                    [obj dealloc];
//                }
                continue;
            }
            
            if ((cntAlive % REFS_IN_BUCKET) == 0) {
                pAlive = getRefSlot(cntAlive);
            }
            if (idxSlot != cntAlive) {
                assert(*pAlive == NULL);
                *pAlive = obj;
                obj->_gc_info.setSlotIndex(cntAlive);
            }
            else {
                assert(*pAlive == obj || *pAlive == NULL);
            }
            pAlive ++;
            cntAlive ++;
            ARGC::checkRefCount(obj, @"alive in gc");
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
            obj = *pSlot;
            if (obj->_gc_info.isReachable()) {
                *getPhantomSlot(alivePhantom++) = obj;
            }
            else {
                reclaimInstance(obj);
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
    void ARGC_genericRetain(id obj);
    void ARGC_genericRelease(id obj);
    
    FOUNDATION_EXPORT void gc_trace(int64_t addr) {
        GC_TRACE_REF = (void*)addr;
        GC_LOG_ROOTS = 0;
        GC_LOG_ALLOC = 0;
        GC_LOG_RC = 0;
    }
    
    void ARGC_pubObject(id obj, id* ptr, id newValue) {
        if (class_isMetaClass([obj class])) {
            JreExchangeVolatileStrongId(ptr, newValue);
        }
        else {
            ARGC_assignGenericObject(ptr, newValue);
        }
    }
    
    
    int ARGC_retainCount(id obj) {
        return (int)NSExtraRefCount(obj);
    }
    
    __attribute__((always_inline)) id ARGC_globalLock(id obj) {
        [obj retain];
        return obj;
    }
    
    id ARGC_globalUnlock(id obj) {
        [obj autorelease];
        return obj;
    }
    
    void ARGC_strongRetain(id obj) {
        [obj retain];
    }

    void ARGC_release(id obj) {
        [obj release];
    }

    void ARGC_autorelease(id obj) {
        [obj autorelease];
    }
    
    void printRefCount(id obj) {
        if (ARGC::isJavaObject(obj)) {
            NSLog(@"%p %d(%d) %@", obj, (int)NSExtraRefCount(obj),
              ((ARGCObject*)obj)->_gc_info.bindCount(), [obj class]);
        }
        else {
            NSLog(@"%p %d(?) %@", obj, (int)NSExtraRefCount(obj), [obj class]);
        }
    }
    
    void ARGC_initARGCObject(id obj) {
        if (ARGC::isJavaObject(obj)) {
            ARGC::_instance.initGCInfo(obj);
        }
    }
    
    void ARGC_genericRetain(id obj) {
        if (ARGC::isJavaObject(obj)) {
            ARGC::touchInstance(obj);
            ARGC::increaseReferenceCount(obj, @"++genericRetain");
        }
        else {
            [obj retain];
        }
    }

    id ARGC_strongRetainAutorelease(id obj) {
        if (obj) {
            [obj retain];
            [obj autorelease];
        }
        return obj;
    }
    
    void ARGC_genericRelease(id oldRef) {
        if (ARGC::isJavaObject(oldRef)) {
            ARGC::decreaseReferenceCountAndPublish(oldRef);
        }
        else {
            [oldRef release];
        }
    }
    
    uintptr_t* ARGC_toVolatileIdPtr(ARGC_FIELD_REF id* ptr) {
        return (uintptr_t*)ptr;
    }
    
    id ARGC_allocateObject(Class cls, NSUInteger extraBytes, NSZone* zone) {
        return ARGC::_instance.allocateInstance(cls, extraBytes, zone);
    }
    
    
    id AGRG_getARGCField(id object, Ivar ivar_, int* err) {
        *err = 0;
        if (ivar_) {;
            id res = *(id*)((char *)object + ivar_getOffset(ivar_));
            return res;
        } else {
            // May be a mapped class "virtual" field, call equivalent accessor method if it exists.
            SEL getter = NSSelectorFromString([NSString stringWithFormat:@"__%s", ivar_getName(ivar_)]);
            if (getter && [object respondsToSelector:getter]) {
                return [object performSelector:getter];
            }
        }
        *err = 1;
        return nil;
    }
    
    void AGRG_setARGCField(id object, Ivar ivar_, id value) {
        
        if (ivar_) {
            ARGC_assignGenericObject((ARGC_FIELD_REF id *)((char *)object + ivar_getOffset(ivar_)), value);
        } else {
            // May be a mapped class "virtual" field, call equivalent accessor method if it exists.
            SEL setter = NSSelectorFromString([NSString stringWithFormat:@"__set%s:", ivar_getName(ivar_)]);
            if (setter && [object respondsToSelector:setter]) {
                [object performSelector:setter withObject:value];
            }
            // else: It's a final instance field, return without any side effects.
        }
    }
    
    void ARGC_deallocClass(IOSClass* cls) {
        NSLog(@"dealloc class %@", cls);
        int a = 3;
    }
    
}

id JreLoadVolatileId(volatile_id *pVar) {
    id obj = *(std::atomic<id>*)pVar;
    if (obj != NULL) {
        [obj retain];
        [obj autorelease];
    };
    return obj;
}

id JreAssignVolatileId(volatile_id *pVar, id value) {
    *(std::atomic<id>*)pVar = value;
    return value;
}

void JreReleaseVolatile(volatile_id *pVar) {
    ARGC_genericRelease(*pVar);
}

id JreVolatileNativeAssign(volatile_id *pVar, id newValue) {
    std::atomic<id>* field = (std::atomic<id>*)pVar;
    [newValue retain];
    id oldValue = field->exchange(newValue);
    [oldValue autorelease];
    return newValue;
}

id JreVolatileStrongAssign(volatile_id *pVar, id newValue) {
    ARGC_assignGenericObject((id*)pVar, newValue);
    return newValue;
}

bool JreCompareAndSwapVolatileStrongId(volatile_id *ptr, id expected, id newValue) {
    std::atomic<id>* field = (std::atomic<id>*)ptr;
    bool res =  field->compare_exchange_strong(expected, newValue);
    if (res) {
        if (newValue) ARGC_genericRetain(newValue);
        if (expected) ARGC_genericRelease(expected);
    }
    return res;
}

id JreExchangeVolatileStrongId(volatile_id *pVar, id newValue) {
    std::atomic<id>* field = (std::atomic<id>*)pVar;
    if (newValue) {
        [newValue retain];
    }
    id oldValue = field->exchange(newValue);
    if (oldValue) {
        [oldValue autorelease];
    }
    return oldValue;
}

void JreCloneVolatile(volatile_id *pVar, volatile_id *pOther) {
    std::atomic<id>* pDst = (std::atomic<id>*)pVar;
    std::atomic<id>* pSrc = (std::atomic<id>*)pOther;
    id obj  = *pSrc;
    *pDst = obj;
}


void JreCloneVolatileStrong(volatile_id *pVar, volatile_id *pOther) {
    std::atomic<id>* pDst = (std::atomic<id>*)pVar;
    std::atomic<id>* pSrc = (std::atomic<id>*)pOther;
    id obj  = *pSrc;
    [obj retain];
    *pDst = obj;
}


void start_GC() {
    dispatch_async(dispatch_get_main_queue(), ^ {
        ARGC::_instance.doGC();
    });
}




@interface NSObject(ARGCObject)
@end
@implementation NSObject(ARGCObject)
- (void) markARGCFields:(BOOL)isStrong
{
    // do nothing
}

- (ObjP) toARGCObject
{
    return NULL;
}

- (BOOL) isReachable:(BOOL)isStrong
{
    return true;
}

- (void) forEachObjectField: (ARGCObjectFieldVisitor) visitor inDepth:(int) depth
{
    // do nothing
}
@end



@interface IOSObjectArray(ARGCObject)
@end
@implementation IOSObjectArray(ARGCObject)
- (void) forEachObjectField: (ARGCObjectFieldVisitor) visitor inDepth:(int) depth {
    ARGC_FIELD_REF id*pItem = buffer_ + 0;
    for (int i = size_; --i >= 0; ) {
        ARGC_FIELD_REF id obj = *pItem++;
        if (obj != NULL) {
            visitor(obj, depth);
        }
    }
    
}

- (ObjP) toARGCObject
{
    return ARGC::isPhantom(self) ? NULL : self;
}

- (void) markARGCFields:(BOOL)isStrong
{
    ARGC::markArrayItems(self, isStrong);
}

@end




@interface IOSReference() {
}
@end

@implementation IOSReference

+ (void)initReferent:(JavaLangRefReference *)reference withReferent:(id)obj
{
    if (obj == nil) return;

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
        reference->referent_ = obj;
        NSPointerArray* rm = objc_getAssociatedObject(obj, &assocKey);
        if (rm == NULL) {
            rm = [[NSPointerArray alloc] initWithOptions:NSPointerFunctionsOpaqueMemory];
            objc_setAssociatedObject(obj, &assocKey, rm, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
            ARGC::increaseReferenceCount(obj, tag);
            [array_ addPointer:obj];
        }
        //DBGLog(@"Add ref: %p: %p %@", rm, obj, [obj class]);
        [rm addPointer:(void*)reference];
        ARGC::increaseReferenceCount(reference, @"refrnc");
    }
}
+ (id)getReferent:(JavaLangRefReference *)reference
{
    return reference->referent_;
}

+ (void)clearReferent:(JavaLangRefReference *)reference
{
    id obj = reference->referent_;
    if (obj == NULL) return;
    
    @synchronized (self) {
        reference->referent_ = NULL;
        NSPointerArray* rm = objc_getAssociatedObject(obj, &assocKey);
        if (rm == NULL) return;

        for (NSUInteger idx = rm.count; --idx >= 0; ) {
            if ([rm pointerAtIndex:idx] == reference) {
                [rm removePointerAtIndex:idx];
                NSDecrementExtraRefCountWasZero(reference);
                break;
            }
        }
    }

    ARGC::checkRefCount(obj, @"ClearRef");
}

+ (void)handleMemoryWarning:(NSNotification *)notification {
    @autoreleasepool {
        @synchronized (self) {
            //[g_softRefs removeAllObjects];
        };
    }
    ARGC_collectGarbage();
}


@end
