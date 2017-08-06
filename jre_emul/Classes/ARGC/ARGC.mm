//
//  ARC+GC.mm
//
//  Created by DAE HOON JI on 19/07/2017.
//  Copyright © 2017 DAE HOON JI. All rights reserved.
//

#import "ARGC.h"
#import "objc/runtime.h"
#import "../IOSClass.h"
#import <atomic>

#define J2OBJC_SMART_GC 1
static const int MAX_TOUCH_DEPTH = 512;
#ifdef DEBUG
static int GC_DEBUG = 0;
static int GC_DEBUG_ROOTS = 0;
#else
#define GC_DEBUG 0
#define GC_DEBUG_ROOTS 0
#endif
static int DEBUG_SN = -1;
static BOOL GC_DEBUG_SHOW_DEALLOC = 0;
static BOOL IN_GC_SCANNING = 0;
static void* GC_DEBUG_INSTANCE = (void*)-1;
#define GC_TRACE(obj) (GC_DEBUG && (GC_DEBUG_INSTANCE == (void*)-1 || obj == GC_DEBUG_INSTANCE))

#pragma clang diagnostic ignored "-Wobjc-missing-super-calls"

typedef uint16_t scan_offset_t;

typedef ARGCObject* ObjP;
typedef std::atomic<ObjP> RefSlot;

static const int _1M = 1024*1024;
static const int REFS_IN_BUCKET = _1M / sizeof(ObjP);

extern "C" {
    id ARGC_cloneInstance(id);
};

static const int64_t REF_COUNT_MASK = 0xFFFFFFF;
static const int64_t SCANNED_FLAG = 0x4000LL * 0x10000;
static const int64_t UNREACHABLE_FLAG = 0x8000LL * 0x10000;
static const int64_t REACHABLE_MASK = 0xFC000LL * 0x10000;
static const int SLOT_INDEX_SHIFT = (32+4);
static const int64_t SLOT_INDEX_MASK = (uint64_t)-1 << SLOT_INDEX_SHIFT;

class RefContext {
public:
    void initialize() {
        /**
         objc 는 refCount 가 0보다 작아질 때, 객체를 릴리즈한다.
         즉, refCount가 0인 상태도 객체가 살아있는 상태이다.
         externalRefCount가 항상 0보다 크거나 같은 상태를 유지하기 위하여
         초기값을 1로 설정한다. (stack/register 참조 상태). externalRefCount 가 0이 될 때,
         즉, 정상적인 경우, 객체가 해제될 상태에서 객체가 살아 남아 있는 상태가
         GC가 동작되어야 할 상황이다.
         */
        _gc_context = SLOT_INDEX_MASK | SCANNED_FLAG | reachable_mark | 1;
    }
    
    // RefSlot Management;
    
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
    
    // External Reference Counting;
    
    int32_t refCount() {
        return _gc_context & REF_COUNT_MASK;
    }
    
    void incRefCount() {
        _gc_context ++;
    }
    
    int32_t decRefCount() {
        int32_t cnt = (--_gc_context) & REF_COUNT_MASK;
        return cnt;
    }
    
    // State;

    BOOL isAlreadyScanned() {
        return __isAlreadyScanned(_gc_context);
    }
    
    BOOL isReachable() {
        return __isReachable(_gc_context);
    }

    BOOL isScanned() {
        return (_gc_context & SCANNED_FLAG) != 0;
    }
    
    BOOL isScannedReachable() {
        return __isScannedReachable(_gc_context);
    }
    
    BOOL markUnreachable() {
        while (true) {
            int64_t v = _gc_context;
            if (__isAlreadyScanned(v)) {
                return !__isReachable(v);
            }
            int64_t new_v = (v & ~REACHABLE_MASK) | UNREACHABLE_FLAG;
            if (_gc_context.compare_exchange_strong(v, new_v)) {
                return true;
            }
        }
    }
    
    BOOL touchReachable() {
        while (true) {
            int64_t v = _gc_context;
            if (__isAlreadyScanned(v)) {
                return __isReachable(v);
            }
            int64_t new_v = (v & ~REACHABLE_MASK) | reachable_mark;
            if (_gc_context.compare_exchange_strong(v, new_v)) {
                return true;
            }
        }
    }
    
    BOOL markScannedReachable() {
        while (true) {
            int64_t v = _gc_context;
            if (__isScannedReachable(v)) {
                return false;
            }
            int64_t new_v = (v & ~REACHABLE_MASK) | SCANNED_FLAG | reachable_mark;
            if (_gc_context.compare_exchange_strong(v, new_v)) {
                return true;
            }
        }
    }
    
    BOOL isPhantom() {
        return (_gc_context & INT_MIN) != 0;
    }
    
    void markPhantom() {
        _gc_context |= INT_MIN;
    }
    
    static void change_generation() {
        reachable_mark = (reachable_mark << 1) & ((uint64_t)15 << 32);
        if (reachable_mark == 0) {
            reachable_mark = (uint64_t)1 << 32;
        }
    }
private:
    static BOOL __isReachable(int64_t v) {
        return (v & reachable_mark) != 0;
    }
    
    static BOOL __isAlreadyScanned(int64_t v) {
        return (v & (UNREACHABLE_FLAG | reachable_mark)) != 0;
    }
    static BOOL __isScannedReachable(int64_t v) {
        return (v & reachable_mark) != 0 && (v & SCANNED_FLAG) != 0;
    }
    
    std::atomic<int64_t> _gc_context;
    static volatile int64_t reachable_mark;
};

volatile int64_t RefContext::reachable_mark = 0;

@interface NSObject(ARGCObject)
@end
@implementation NSObject(ARGCObject)
- (void) forEachObjectField: (ARGCObjectFieldVisitor) visitor inDepth:(int) depth
{
    // do nothing
}
@end


@interface ARGCObject()
{
@public
    RefContext _gc_info;
}
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


//##############################################################


class ARGC {
public:
    static ARGC _instance;
    
    ARGC() {
        RefContext::change_generation();
        JavaLangRefReference_class = objc_lookUpClass("JavaLangRefReference");
        deallocInstance = dealloc_now;
        _scanOffsetCache = [[NSMutableDictionary alloc] init];
        tableLock = [[NSObject alloc]init];
        scanLock = [[NSObject alloc]init];
        argcObjectSize = class_getInstanceSize([ARGCObject class]) + sizeof(void*);
    }
    
    int size() {
        return _cntRef;
    }
    
    void doGC();

    static void markInstance(ObjP obj) {
        touchInstance(obj, 0);
    }
    
    static void touchInstance(ObjP obj) {
        touchInstance(obj, 0);
    }
    
    static void markInstance(ObjP obj, int depth);

    static void touchInstance(ObjP obj, int depth);
    
    static void markInstanceEx(id obj, int depth);
    
    static void touchInstanceEx(id obj, int depth);
    
    static int32_t getExternalRefCount(ObjP obj) {
        return obj->_gc_info.refCount();
    }
    
    static void releaseExternalReference(ObjP obj) {
        int cnt = obj->_gc_info.decRefCount();
        if (GC_DEBUG && cnt < 0) {
            /**
             App 종료 시? AutoReleasePool 을 정리하면서, WeakReference 등의 외부 참조 개수가 0 이하로 줄어드는 문제 발생한다.
             이유를 알 수 없다.
             */
            NSLog(@"release Ext --- %p %d(%d) %@", obj, (int)NSExtraRefCount(obj), obj->_gc_info.refCount(), [obj class]);
        }
    }
    
    static void retainExternalReference(ObjP obj) {
        obj->_gc_info.incRefCount();
        touchInstance(obj);
//        if (!isPublished(obj)) {
//            _instance.addStrongRef(obj);
//        }
    }
    
    static BOOL inGC() {
        return _instance._inGC;
    }
    
    static BOOL isJavaObject(id obj) {
        return [obj isKindOfClass:[ARGCObject class]];
    }
    
    ObjP allocateInstance(Class cls, NSUInteger extraBytes, NSZone* zone) {
        getScanOffsets(cls);
        size_t instanceSize = class_getInstanceSize(cls);
        if (instanceSize < argcObjectSize) {
            extraBytes = argcObjectSize - instanceSize;
        }
        
        ObjP obj = (ObjP)NSAllocateObject(cls, extraBytes, zone);
        if (GC_DEBUG) NSLog(@"alloc %p %@", obj, cls);
        obj->_gc_info.initialize();
        _cntAllocated ++;
//        if (obj->sn_ == DEBUG_SN) {
//            GC_DEBUG_INSTANCE = (void *)obj;
//            NSLog(@"DEBUG_SN found");
//        }
        // refCount 0 인 상태, 즉 allocation 직후에도 stack에서 참조가능한 상태이다.
        addStrongRef(obj);
        return obj;
    }
    
    typedef void (*ARGCVisitor)(ObjP obj);
    
    std::atomic<ARGCVisitor> deallocInstance;
    
//    static BOOL isPublished(ObjP obj) {
//        return obj->_gc_info.slotIndex() >= 0;
//    }
    
    static void retainReferenceCount(id obj) {
        retainReferenceCountEx(obj);
        if (GC_DEBUG) NSLog(@"retain --- %p %d(?) %@", obj, (int)NSExtraRefCount(obj), [obj class]);
    }

    static void retainReferenceCountEx(ARGCObject* obj) {
        NSIncrementExtraRefCount(obj);
    }
    
    static void releaseReferenceCount(id obj) {
        if (isJavaObject(obj)) {
            releaseReferenceCountAndPublish(obj);
        }
        else {
            if (GC_DEBUG) NSLog(@"release --- %p %d(?) %@", obj, (int)NSExtraRefCount(obj) - 1, [obj class]);
            releaseReferenceCountEx(obj);
        }
    }
    
    static BOOL releaseReferenceCountEx(id obj) {
        if (!NSDecrementExtraRefCountWasZero(obj)) {
            return true;
        }
        // 주의) NSExtraRefCount() 사용해선 안된다.
        // NSExtraRefCount(__NSCFConstantString*) 는 항상 0을 반환.
        NSUInteger rc = [obj retainCount];
        if (rc <= 1) {
            //assert(!isJavaObject(obj) || getExternalRefCount((ARGCObject*)obj) <= 0);
            [obj dealloc];
        }
        return false;
    }

    static BOOL releaseReferenceCountAndPublish(ObjP obj) {
        if (releaseReferenceCountEx(obj)) {
            checkRefCount(obj, @"release Pub");
//            if (!isPublished(obj)) {
//                _instance.addStrongRef(obj);
//            }
            return true;
        }
        return false;
    }
    
    static const scan_offset_t* getScanOffsets(Class clazz) {
        ScanOffsetArray* scanOffsets = _instance.makeScanOffsets(clazz);
        return scanOffsets->offsets;
    }
    
    static void checkRefCount(ObjP obj, NSString* tag) {
        if (GC_DEBUG) {
            NSLog(@"%@ %p %d(%d) %@", tag, obj, (int)NSExtraRefCount(obj), obj->_gc_info.refCount(), [obj class]);
            if ((int)NSExtraRefCount(obj) < (int)obj->_gc_info.refCount() - 1) {
                // 멀티쓰레드환경이라 두 값의 비교가 정확하지 않다. 관련 정보를 적는다.
                int MAX_THREAD_COUNT = 32;
                NSLog(@"Maybe error %@ %p %d(%d) %@", tag, obj, (int)NSExtraRefCount(obj), obj->_gc_info.refCount(), [obj class]);
                //assert((int)NSExtraRefCount(obj) >= (int)obj->_gc_info.refCount() - MAX_THREAD_COUNT);
            };
        }
    }
    
private:
    static void dealloc_in_collecting(ObjP obj);

    static void dealloc_in_scan(ObjP obj);
    
    static void dealloc_now(ObjP obj);
    
    RefSlot* newBucket() {
        return (RefSlot*)malloc(_1M);
    }
    
    BOOL unregisterRef(ObjP obj) {
        int idxSlot = obj->_gc_info.slotIndex();
        assert(idxSlot <= _cntRef);
        RefSlot* pSlot = getRefSlot(idxSlot);
        if (pSlot->compare_exchange_strong(obj, NULL)) {
            return true;
        }
        assert(*pSlot == NULL);
        return false;
    }
    
    RefSlot* getRefSlot(int index) {
        assert(index <= _cntRef);
        int idxBucket = index / REFS_IN_BUCKET;
        int idxRef = index % REFS_IN_BUCKET;
        return _table[idxBucket] + idxRef;
    }
    
    RefSlot* getPhantomSlot(int index) {
        assert(index <= _cntPhantom);
        int idxBucket = index / REFS_IN_BUCKET;
        int idxRef = index % REFS_IN_BUCKET;
        return _phantomeTable[idxBucket] + idxRef;
    }
    
    void addStrongRef(ObjP obj) {
        @synchronized (tableLock) {
            int index = _cntRef ++;
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
        }
    }
    
    void reclaimInstance(id obj) {
        if (GC_DEBUG && GC_DEBUG_SHOW_DEALLOC) {
            NSLog(@"dealloc: %p %@", obj, [obj class]);
        }
        NSDeallocateObject(obj);
        _cntDeallocated ++;
    }
    
    void addPhantom(ObjP dead, BOOL inGCThread) {
        //assert(NULL == *getRefSlot(dead->_gc_info.slotIndex()));
        ARGCPhantom* obj = (ARGCPhantom*)dead;

        if (inGCThread) {
            if (NSExtraRefCount(obj) <= 0) {
                reclaimInstance(obj);
            }
            else {
                // Append phantom, call delloc later.
                obj->_next = _phantomQ;
                _phantomQ = obj;
                // call finalize methods ??
            }
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
                row[idxRef] = dead;
            }
        }
    }
    
    ScanOffsetArray* makeScanOffsets(Class clazz);
    
    size_t argcObjectSize;
    std::atomic_int _cntRef;
    std::atomic_int _cntAllocated;
    std::atomic_int _cntDeallocated;
    ARGCPhantom* _phantomQ = NULL;
    //std::atomic<ARGCPhantom* > _sharedPantomQ;
    RefSlot* _table[1024];
    RefSlot* _phantomeTable[1024];
    std::atomic_int _cntPhantom;
    NSMutableDictionary* _scanOffsetCache;
    Class JavaLangRefReference_class;
    NSObject* tableLock;
    NSObject* scanLock;
    
    BOOL _inGC = FALSE;
};


ARGC ARGC::_instance;
static scan_offset_t _emptyFields[1] = { 0 };



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


- (id)copy
{
    return ARGC_cloneInstance(self);
}

- (void)dealloc
{
    ARGC::ARGCVisitor fn = ARGC::_instance.deallocInstance;
    fn(self);
    //[super dealloc];
}

- (instancetype)retain
{
    ARGC::retainExternalReference(self);
    ARGC::retainReferenceCountEx(self);
    ARGC::checkRefCount(self, @"retain Ext");
    return self;
}


- (BOOL)retainWeakReference
{
    if ([super retainWeakReference]) {
        ARGC::retainExternalReference(self);
        // retainWeakReference 함수 수행 중에는 NSLog 등 다른 API 호출이 금지된 것으로 보인다.
        //ARGC::checkRefCount(self, @"retainWeak");
        return true;
    }
    return false;
}
 
- (oneway void)release
{
    ARGC::releaseExternalReference(self);
    if (ARGC::releaseReferenceCountEx(self)) {
        ARGC::checkRefCount(self, @"release Ext");
    }
}

- (instancetype)autorelease
{
    return [super autorelease];
}

- (void) forEachObjectField: (ARGCObjectFieldVisitor) visitor inDepth:(int) depth
{
    const scan_offset_t* scanOffsets = ARGC::getScanOffsets([self class]);
    while (true) {
        scan_offset_t offset = *scanOffsets++;
        if (offset == 0) {
            break;
        }
        id field = *((id *)self + offset);
        if (field) {
            visitor(field, depth);
        }
    }
}

@end


ScanOffsetArray* ARGC::makeScanOffsets(Class clazz) {
    scan_offset_t _offset_buf[4096];
    id _test_obj_buf[4096];
    ScanOffsetArray* res = _scanOffsetCache[clazz];
    if (res != NULL) {
        return res;
    }
    @synchronized (_scanOffsetCache) {
        res = (ScanOffsetArray*)_scanOffsetCache[clazz];
        if (res != NULL) {
            return res;
        }
        
        int cntARGC = 0;
        id clone = NSAllocateObject(clazz, 0, NULL);
        int cntObjField = 0;
        BOOL referentSkipped = false;
        
        for (Class cls = clazz; cls && cls != [NSObject class]; ) {
            unsigned int ivarCount;
            Ivar *ivars = class_copyIvarList(cls, &ivarCount);
            for (unsigned int i = 0; i < ivarCount; i++) {
                Ivar ivar = ivars[i];
                const char *ivarType = ivar_getTypeEncoding(ivar);
                if (*ivarType == '@') {
                    if (cls == JavaLangRefReference_class && !referentSkipped) {
                        //NSLog(@"Refernce.%s field skipped", ivar_getName(ivar));
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
                _offset_buf[cntARGC++] = offset;
            }
        }
    
        _offset_buf[cntARGC++] = 0;
        int memsize = cntARGC * sizeof(scan_offset_t);
        ScanOffsetArray* offsetArray = NSAllocateObject([ScanOffsetArray class], memsize, NULL);
        memcpy(offsetArray->offsets, _offset_buf, memsize);
        _scanOffsetCache[clazz] = res = offsetArray;
    }
    return res;
}

void ARGC::markInstance(ObjP obj, int depth) {
    if (!obj->_gc_info.markScannedReachable()) {
        return;
    }
    [obj forEachObjectField: markInstanceEx inDepth: ++depth];
}

void ARGC::markInstanceEx(id obj, int depth) {
    if (isJavaObject(obj)) {
        markInstance(obj, depth);
    }
}

void ARGC::touchInstance(ObjP obj, int depth) {
    if (!obj->_gc_info.markScannedReachable()) {
        return;
    }
    [obj forEachObjectField: touchInstanceEx inDepth: ++depth];
}

void ARGC::touchInstanceEx(id obj, int depth) {
    if (isJavaObject(obj)) {
        if (depth > MAX_TOUCH_DEPTH
        &&  ((ObjP)obj)->_gc_info.touchReachable()) {
            return;
        }
        touchInstance(obj, depth);
    }
}


void ARGC_assignARGCObject(ARGC_FIELD_REF id* pField, ObjP newValue) {
    if (*pField == newValue) {
        return;
    }
    
    if (newValue != NULL) {
        assert(ARGC::isJavaObject(newValue));
        /**
         In progress scanning, if the value is not scanned yet but the owner is marked,
         value may not scanned after assign.
         */
        ARGC::touchInstance(newValue);
    }
    
    std::atomic<ObjP>* field = (std::atomic<ObjP>*)pField;
    ObjP oldValue = field->exchange(newValue);
    if (oldValue == newValue) {
        return;
    }
    if (newValue != NULL) {
        ARGC::retainReferenceCount(newValue);
    }
    if (oldValue != NULL) {
        assert(ARGC::isJavaObject(oldValue));
        if (ARGC::releaseReferenceCountAndPublish(oldValue)) {
            ARGC::touchInstance(oldValue);
        };
    }
}

void ARGC_assignGenericObject(ARGC_FIELD_REF id* pField, id newValue) {
    if (*pField == newValue) {
        return;
    }
    
    std::atomic<id>* field = (std::atomic<id>*)pField;
    if (newValue != NULL) {
        if (ARGC::isJavaObject(newValue)) {
            ARGC::touchInstance(newValue);
            ARGC::retainReferenceCount(newValue);
        }
        else {
            [newValue retain];
        }
    }
    
    id oldValue = field->exchange(newValue);
    
    if (oldValue != NULL) {
        if (ARGC::isJavaObject(oldValue)) {
            if (ARGC::releaseReferenceCountAndPublish(oldValue)) {
                ARGC::touchInstance(oldValue);
            };
        }
        else {
            [oldValue release];
        }
    }
}



void ARGC_collectGarbage() {
    ARGC::_instance.doGC();
}

void ARGC::dealloc_in_collecting(ObjP obj) {
    if (ARGC::_instance.unregisterRef(obj)) {
        [obj forEachObjectField:(ARGCObjectFieldVisitor)releaseReferenceCount inDepth: 0];
        _instance.addPhantom(obj, false);
    }
    else {
        //NSLog(@"Add Phantom later %p, %@", obj, [obj class]);
        //_instance.addStrongRef(obj);
    }
}

void ARGC::dealloc_in_scan(ObjP obj) {
    /*
     Do nothing
    if (ARGC::_instance.unregisterRef(obj)) {
        [obj forEachObjectField:(ARGCObjectFieldVisitor)releaseReferenceCount];
        _instance.addPhantom(obj, false);
    }
    else {
        NSLog(@"Add Phantom scanned %p, %@", obj, [obj class]);
        //_instance.addStrongRef(obj);
    }
    */
    
}

void ARGC::dealloc_now(ObjP obj) {
    if (ARGC::_instance.unregisterRef(obj)) {
        [obj forEachObjectField:(ARGCObjectFieldVisitor)releaseReferenceCount inDepth: 0];
        _instance.addPhantom(obj, false);
    }
    else {
        //NSLog(@"Add Phantom now %p, %@", obj, [obj class]);
        //_instance.addStrongRef(obj);
    }
}

void ARGC::doGC() {
    if (_cntRef == 0) {
        return;
    }
    NSMutableDictionary<Class, Counter*>* roots = GC_DEBUG_ROOTS ? [NSMutableDictionary new] : NULL;
    @synchronized (scanLock) {
        RefContext::change_generation();
        deallocInstance.exchange(dealloc_in_scan);
        
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
                assert(idx == idxSlot);
                if (getExternalRefCount(obj) > 0) {
                    
                    Counter* v = roots[[obj class]];
                    if (v == NULL) {
                        roots[[obj class]] = v = [Counter new];
                    }
                    v->count ++;
                    cntRoot ++;
                    
                    touchInstance(obj);
                }
//                else {
//                    obj->_gc_info.markUnreachable();
//                }
            }
        }
        if (GC_DEBUG_ROOTS) {
            for (Class c in roots) {
                int cnt = roots[c]->count;
                if (cnt > 100) {
                    NSLog(@"root %d %@", cnt, c);
                }
            }
        }
        int cntScan = idxSlot;

        for (idxSlot = 0; idxSlot < cntScan; idxSlot ++, pScan++) {
            if ((idxSlot % REFS_IN_BUCKET) == 0) {
                pScan = getRefSlot(idxSlot);
            }
            if ((obj = *pScan) != NULL) {
                if (!obj->_gc_info.markUnreachable()
                    &&  !obj->_gc_info.isScanned()) {
                    touchInstance(obj);
                }
            }
        }
        
        
        deallocInstance.exchange(dealloc_in_collecting);
        IN_GC_SCANNING = true;
        int cntAlive = 0;
        int cntAlivedGenerarion = 0;
        RefSlot* pAlive = _table[0];
        for (idxSlot = 0; true; idxSlot ++, pScan++) {
            if (idxSlot >= cntScan) {
                cntAlivedGenerarion = cntAlive;
            }
            
            if (idxSlot >= _cntRef) {
                @synchronized (tableLock) {
                    if (idxSlot == _cntRef) {
                        _cntRef = cntAlive;
                    }
                    break;
                }
            }
            if ((idxSlot % REFS_IN_BUCKET) == 0) {
                pScan = getRefSlot(idxSlot);
            }
            obj = pScan->exchange(NULL);
            if (obj == NULL) {
                continue;
            }
            /*
             주의. 현재 obj 가 다른 쓰레드에 의해 삭제될 수 있다.
             */
            if (!obj->_gc_info.isReachable()) {
                assert(getExternalRefCount(obj) <= 0);
                addPhantom(obj, true);
                continue;
            }
            
            if (!obj->_gc_info.isScanned()) {
                markInstance(obj);
            }
            
            if ((cntAlive % REFS_IN_BUCKET) == 0) {
                pAlive = getRefSlot(cntAlive);
            }
            assert(*pAlive == NULL);
            *pAlive ++ = obj;
            obj->_gc_info.setSlotIndex(cntAlive ++);
            if (GC_DEBUG) {
                NSLog(@"alive %@, %d", obj, (int)NSExtraRefCount(obj));
            }
        }
        
        deallocInstance.exchange(dealloc_now);
        
        ARGCPhantom*  dead = this->_phantomQ;
        this->_phantomQ = NULL;
        for (;dead != NULL;) {
            ARGCPhantom*  next = dead->_next;
            reclaimInstance(dead);
            dead = next;
        }

        for (int idxSlot = 0; true; idxSlot++) {
            if (idxSlot >= _cntPhantom) {
                @synchronized (tableLock) {
                    if (idxSlot == _cntPhantom) {
                        _cntPhantom = 0;
                    }
                    break;
                }
            }
            RefSlot* pSlot = getPhantomSlot(idxSlot);
            obj = *pSlot;
            reclaimInstance(dead);
        }

        IN_GC_SCANNING = false;

        if (GC_DEBUG_ROOTS) {
            NSLog(@"scan: %d root: %d alive:%d", cntScan, cntRoot, cntAlivedGenerarion);
        }
    }
    

}

void ARGC_requestGC() {
    dispatch_time_t interval = dispatch_time(DISPATCH_TIME_NOW, 1ull * NSEC_PER_SEC);
    void (^SCAN)() = ^{
        ARGC::_instance.doGC();
        ARGC_requestGC();
    };

    dispatch_after(interval , dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), SCAN);
}

extern "C" {
    id ARGC_cloneInstance(id);
    
    //typedef id volatile_id;
    
    id JreLoadVolatileId(volatile_id *pVar);
    id JreAssignVolatileId(volatile_id *pVar, __unsafe_unretained id value);
    id JreVolatileStrongAssign(volatile_id *pIvar, __unsafe_unretained id value);
    bool JreCompareAndSwapVolatileStrongId(volatile_id *pVar, __unsafe_unretained id expected, __unsafe_unretained id newValue);
    id JreExchangeVolatileStrongId(volatile_id *pVar, __unsafe_unretained id newValue);
    void JreCloneVolatile(volatile_id *pVar, volatile_id *pOther);
    void JreCloneVolatileStrong(volatile_id *pVar, volatile_id *pOther);
    void JreReleaseVolatile(volatile_id *pVar);
    
    id JreRetainedWithAssign(id parent, __strong id *pIvar, __unsafe_unretained id value);
    id JreVolatileRetainedWithAssign(id parent, volatile_id *pIvar, __unsafe_unretained id value);
    void JreRetainedWithRelease(__unsafe_unretained id parent, __unsafe_unretained id child);
    void JreVolatileRetainedWithRelease(id parent, volatile_id *pVar);
    
    void ARGC_retainExternalWeakRef(id obj) {
        if (ARGC::isJavaObject(obj)) {
            ARGC::retainExternalReference(obj);
            ARGC::checkRefCount(obj, @"retainWeak");
        }
    }
    
    int ARGC_retainCount(id obj) {
        return NSExtraRefCount(obj);
    }
    
    __attribute__((always_inline)) id ARGC_globalLock(id obj) {
        [obj retain];
        return obj;
    }
    
    id ARGC_globalUnlock(id obj) {
        [obj release];
        return obj;
    }
    
    void ARGC_strongRetain(id obj) {
        [obj retain];
    }

    void ARGC_autorelease(id obj) {
        [obj autorelease];
    }
    
    void printRefCount(id obj) {
        NSLog(@"ref: %d -> %@", (int)NSExtraRefCount(obj), obj);
    }
    
    void ARGC_initARGCObject(id obj) {
        if (ARGC::isJavaObject(obj)) {
            ((ObjP)obj)->_gc_info.initialize();
        }
    }
    
    void ARGC_genericRetain(id obj) {
        if (ARGC::isJavaObject(obj)) {
            ARGC::touchInstance(obj);
            ARGC::retainReferenceCount(obj);
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
            ARGC::releaseReferenceCountAndPublish(oldRef);
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
    
    id ARGC_cloneInstance(id obj) {
        id clone = NSCopyObject(obj, 0, NULL);

        if (ARGC::isJavaObject(obj)) {
            ((ObjP)clone)->_gc_info.initialize();
        }
        
        Class cls = [obj class];
        while (cls && cls != [NSObject class]) {
            unsigned int ivarCount;
            Ivar *ivars = class_copyIvarList(cls, &ivarCount);
            for (unsigned int i = 0; i < ivarCount; i++) {
                Ivar ivar = ivars[i];
                const char *ivarType = ivar_getTypeEncoding(ivar);
                if (*ivarType == '@') {
                    ptrdiff_t offset = ivar_getOffset(ivar);
                    __unsafe_unretained id field = *(__unsafe_unretained id *)((char *)clone + offset);
                    if (field != NULL) ARGC_genericRetain(field);
                }
            }
            free(ivars);
            cls = class_getSuperclass(cls);
        }
        
        return clone;
    }
    
    void ARGC_deallocClass(IOSClass* cls) {
        if (GC_DEBUG) NSLog(@"dealloc cls %@", cls);
        int a = 3;
    }
    
    NSUInteger JreDefaultFastEnumeration(__unsafe_unretained id obj, NSFastEnumerationState *state,
                                         __unsafe_unretained id *stackbuf, NSUInteger len);
}

id JreLoadVolatileId(volatile_id *pVar) {
    id obj = *(std::atomic<id>*)pVar;
    if (obj != NULL) {
        [obj retain];//ARGC_genericRetain(obj);
        [obj autorelease];
    };
    return obj;
}

id JreAssignVolatileId(volatile_id *pVar, id value) {
    *(std::atomic<id>*)pVar = value;
    return value;
}

id JreVolatileStrongAssign(volatile_id *pVar, id newValue) {
    std::atomic<id>* field = (std::atomic<id>*)pVar;
    [newValue retain];//ARGC_genericRetain(newValue);
    id oldValue = field->exchange(newValue);
    //ARGC_assignGenericObject(pIvar, value);
    //[oldValue autorelease];
    return newValue;
}

bool JreCompareAndSwapVolatileStrongId(volatile_id *pVar, id expected, id newValue) {
    std::atomic<id>* field = (std::atomic<id>*)pVar;
    if (newValue) {
        [newValue retain];//ARGC_genericRetain(newValue);
    }
    bool res =  field->compare_exchange_strong(expected, newValue);
    if (res) {
        if (expected) {
            // arc 사용 시에는 이미 id 가 caller 측에서 사용되고 있으므로
            // autoreleae 를 할 필요가 없다 [expected autorelease];
        }
    }
    else if (newValue) {
        [newValue release];//ARGC_genericRelease(newValue);
    }
    return res;
}

id JreExchangeVolatileStrongId(volatile_id *pVar, id newValue) {
    std::atomic<id>* field = (std::atomic<id>*)pVar;
    if (newValue) {
        [newValue retain];//ARGC_genericRetain(newValue);
    }
    id oldValue = field->exchange(newValue);
    if (oldValue) {
        [oldValue autorelease];
    }
    return oldValue;
}

void JreReleaseVolatile(volatile_id *pVar) {
    [*(id*)pVar release];// ARGC::releaseReferenceCount(*pVar);
    // This is only called from a dealloc method, so we can assume there are no
    // concurrent threads with access to this address. Therefore, synchronization
    // is unnecessary.
    //RELEASE_(*(id *));
}

void JreCloneVolatile(volatile_id *pVar, volatile_id *pOther) {
    std::atomic<id>* pDst = (std::atomic<id>*)pVar;
    std::atomic<id>* pSrc = (std::atomic<id>*)pOther;
    id obj  = *pSrc;
    //[obj retain];//ARGC_genericRetain(obj);
    *pDst = obj;
}

void JreCloneVolatileStrong(volatile_id *pVar, volatile_id *pOther) {
    std::atomic<id>* pDst = (std::atomic<id>*)pVar;
    std::atomic<id>* pSrc = (std::atomic<id>*)pOther;
    id obj  = *pSrc;
    [obj retain];//ARGC_genericRetain(obj);
    *pDst = obj;
}

NSUInteger JreDefaultFastEnumeration(
                                     __unsafe_unretained id obj, NSFastEnumerationState *state,
                                     __unsafe_unretained id *stackbuf, NSUInteger len) {
    SEL hasNextSel = @selector(hasNext);
    SEL nextSel = @selector(next);
    id iter = (__bridge id) (void *) state->extra[0];
    if (!iter) {
        static unsigned long no_mutation = 1;
        state->mutationsPtr = &no_mutation;
        // The for/in loop could break early so we have no guarantee of being able
        // to release the iterator. As long as the current autorelease pool is not
        // cleared within the loop, this should be fine.
        iter = nil_chk([obj iterator]);
        state->extra[0] = (unsigned long) iter;
        state->extra[1] = (unsigned long) [iter methodForSelector:hasNextSel];
        state->extra[2] = (unsigned long) [iter methodForSelector:nextSel];
    }
    bool (*hasNextImpl)(id, SEL) = (bool (*)(id, SEL)) state->extra[1];
    id (*nextImpl)(id, SEL) = (id (*)(id, SEL)) state->extra[2];
    NSUInteger objCount = 0;
    state->itemsPtr = stackbuf;
    while (hasNextImpl(iter, hasNextSel) && objCount < len) {
        *stackbuf++ = nextImpl(iter, nextSel);
        objCount++;
    }
    return objCount;
}

void start_GC() {
    dispatch_async(dispatch_get_main_queue(), ^ {
        ARGC::_instance.doGC();
    });
}

