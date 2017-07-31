//
//  ARC+GC.mm
//
//  Created by DAE HOON JI on 19/07/2017.
//  Copyright © 2017 DAE HOON JI. All rights reserved.
//

#import "ARGC.h"
#import "objc/runtime.h"
#import <atomic>

#define J2OBJC_SMART_GC 1
#define GC_DEBUG 1

typedef uint16_t scan_offset_t;

typedef ARGCObject* ObjP;
typedef void (*ARGCObjectFiledVisitor)(id obj);
typedef std::atomic<ObjP> RefSlot;

static const int _1M = 1024*1024;
static const int REFS_IN_BUCKET = _1M / sizeof(ObjP);



class RefContext {
public:
    void initialize() {
        _gc_context = ((uint64_t)-1 << (32+4)) | reachable_mark | 1;
    }
    
    // RefSlot Management;
    
    int32_t slotIndex() {
        return _gc_context >> (32+4);
    }

    void setSlotIndex(int64_t index) {
        index <<= (32+4);
        while (true) {
            int64_t v = _gc_context;
            int64_t new_v = (v & ~((uint64_t)-1 << (32+4))) | index;
            if (_gc_context.compare_exchange_weak(v, new_v)) {
                return;
            }
        }
    }

    // External Reference Counting;
    
    int32_t refCount() {
        return (int)_gc_context;
    }
    
    void incRefCount() {
        _gc_context ++;
    }

    int32_t decRefCount() {
        int32_t cnt = (int32_t)(--_gc_context);
        assert(cnt >= 0);
        return cnt;
    }

    // State;
    
    BOOL isStrongReachable() {
        return (_gc_context & reachable_mark) != 0;
    }
    
    BOOL markStrongReachable() {
        while (!this->isStrongReachable()) {
            int64_t v = _gc_context;
            if (_gc_context.compare_exchange_weak(v, v | reachable_mark)) {
                return true;
            }
        }
        return false;
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
    std::atomic<int64_t> _gc_context;
    static int64_t reachable_mark;
};

int64_t RefContext::reachable_mark = 0;


@interface ARGCObject()
{
@public
    RefContext _gc_info;
}
- (void) forEachObjectField: (ARGCObjectFiledVisitor) visitor;

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
    }

    int size() {
        return _cntRef;
    }
    
    void doGC();
    
    static void markInstance(ObjP obj);

    static void markInstanceEx(id obj);
    
    static int32_t getExternalRefCount(ObjP obj) {
        return obj->_gc_info.refCount();
    }
    
    static void releaseExternalReference(ObjP obj) {
        obj->_gc_info.decRefCount();
        releaseReferenceCount(obj);
    }
    
    static void retainExternalReference(ObjP obj) {
        obj->_gc_info.incRefCount();
        retainReferenceCount(obj);
        markInstance(obj);
        if (!isPublished(obj)) {
            _instance.addStrongRef(obj);
        }
    }

    static BOOL inGC() {
        return _instance._inGC;
    }
    
    static BOOL isJavaObject(id obj) {
        return [obj isKindOfClass:[ARGCObject class]];
    }
    
    static ObjP allocateInstance(Class cls, NSUInteger extraBytes, NSZone* zone) {
        id obj = NSAllocateObject(cls, extraBytes, zone);
        ((ObjP)obj)->_gc_info.initialize();
        _instance._cntAllocated ++;
        return obj;
    }
    
    static void deallocInstance(ObjP obj) {
        if (ARGC::unregisterRef(obj)) {
            [obj forEachObjectField: releaseReferenceCount];
            _instance.addPhantom(obj);
        }
    }

    static BOOL isReachable(ObjP obj) {
        return obj->_gc_info.isStrongReachable();
    }
    

    static BOOL isPublished(ObjP obj) {
        return obj->_gc_info.slotIndex() >= 0;
    }
    
    static void retainReferenceCount(id obj) {
        NSIncrementExtraRefCount(obj);
    }
    
    static void releaseReferenceCount(id obj) {
        if (NSDecrementExtraRefCountWasZero(obj)) {
            [obj dealloc];
        }
    }

    static void releaseReferenceCountAndPublish(ObjP obj) {
        if (NSDecrementExtraRefCountWasZero(obj)) {
            [obj dealloc];
        }
        else if (!isPublished(obj)) {
            _instance.addStrongRef(obj);
        }
    }

    static const scan_offset_t* getScanOffsets(Class clazz) {
        ScanOffsetArray* scanOffsets = _instance.makeScanOffsets(clazz);
        return scanOffsets->offsets;
    }
    
    
private:
    RefSlot* newBucket() {
        return (RefSlot*)malloc(_1M);
    }

    static BOOL unregisterRef(ObjP obj) {
        int idxSlot = obj->_gc_info.slotIndex();
        if (idxSlot < 0) {
            return true;
        }
        RefSlot* pSlot = _instance.getRefSlot(idxSlot);
        return pSlot->compare_exchange_weak(obj, NULL);
    }
    
    RefSlot* getRefSlot(int index) {
        assert(index <= _cntRef);
        int idxBucket = index / REFS_IN_BUCKET;
        int idxRef = index % REFS_IN_BUCKET;
        return _table[idxBucket] + idxRef;
    }
    
    
    void addStrongRef(ObjP obj) {
        assert(obj->_gc_info.isStrongReachable());
        int index = _cntRef ++;
        obj->_gc_info.setSlotIndex(index);
        int idxBucket = index / REFS_IN_BUCKET;
        int idxRef = index % REFS_IN_BUCKET;
        RefSlot* row = _table[idxBucket];
        if (row == NULL) {
            row = _table[idxBucket] = newBucket();
        }
        row[idxRef] = obj;
    }
    
    void reclaimInstance(ObjP obj) {
        if (GC_DEBUG) {
            NSLog(@"dealloc: %@", obj);
        }
        NSDeallocateObject(obj);
        _cntDeallocated ++;
    }
    
    void addPhantom(ObjP obj) {
        
        if (NSExtraRefCount(obj) <= 0) {
            reclaimInstance(obj);
        }
        else {
            // Append phantom, call delloc later.
            *(ObjP*)&obj->_gc_info = _phantomQ;
            _phantomQ = obj;
            // call finalize methods ??
        }
    }
    
    ScanOffsetArray* makeScanOffsets(Class clazz);
    
    std::atomic_int _cntRef;
    std::atomic_int _cntAllocated;
    std::atomic_int _cntDeallocated;
    ObjP _phantomQ = NULL;
    RefSlot* _table[1024];
    NSMutableDictionary* _scanOffsetCache;
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
    id obj = ARGC::allocateInstance(self, 0, NULL);
    return obj;
}

+ (instancetype)allocWithZone:(struct _NSZone *) zone
{
    /**
     allocWithZone:zone is deprecated. The method ignores 'zone' argument.
     So [[cls allocWithZone:myZone] zone] is not equals to myZone;
     */
    id obj = ARGC::allocateInstance(self, 0, zone);
    return obj;
}


- (instancetype)copy;
{
    id clone = [super copy];
    ((ObjP)clone)->_gc_info.initialize();
    [clone forEachObjectField:ARGC::retainReferenceCount];
    return clone;
}

- (void)dealloc
{
    ARGC::deallocInstance(self);
}

- (instancetype)retain
{
    ARGC::retainExternalReference(self);
    return self;
}

- (oneway void)release
{
    ARGC::releaseExternalReference(self);
}

- (instancetype)autorelease
{
    return [super autorelease];
}

- (void) forEachObjectField: (ARGCObjectFiledVisitor) visitor
{
    const scan_offset_t* scanOffsets = ARGC::getScanOffsets([self class]);
    while (true) {
        scan_offset_t offset = *scanOffsets++;
        if (offset == 0) {
            break;
        }
        id field = *((id *)self + offset);
        if (field) {
            visitor(field);
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
        id clone = NSAllocateObject(clazz, 0, NULL);
        int cntObjField = 0;
        
        for (Class cls = clazz; cls && cls != [NSObject class]; ) {
            unsigned int ivarCount;
            Ivar *ivars = class_copyIvarList(cls, &ivarCount);
            for (unsigned int i = 0; i < ivarCount; i++) {
                Ivar ivar = ivars[i];
                const char *ivarType = ivar_getTypeEncoding(ivar);
                if (*ivarType == '@') {
                    ptrdiff_t offset = ivar_getOffset(ivar);
                    _offset_buf[cntObjField] = offset / sizeof(ObjP);
                    id obj = [[[NSObject alloc] retain] retain];
                    id* pField = (id *)((char *)clone + offset);
                    _test_obj_buf[cntObjField] = *pField = obj;
                    cntObjField ++;
                }
            }
            free(ivars);
            cls = class_getSuperclass(cls);
        }
        
        NSDeallocateObject(clone);
        int cntARGC = 0;
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

void ARGC::markInstance(ObjP obj) {
    if (!obj->_gc_info.markStrongReachable()) {
        return;
    }
    [obj forEachObjectField: markInstanceEx];
}

void ARGC::markInstanceEx(id obj) {
    if (isJavaObject(obj)) {
        markInstance(obj);
    }
}


void ARGC_assignARGCObject(ARGC_FIELD_REF id* pField, ObjP newValue) {
    if (*pField == newValue) {
        return;
    }
    
    if (newValue != NULL) {
        /**
         In progress scanning, if the value is not scanned yet but the owner is marked,
         value may not scanned after assign.
         */
        ARGC::markInstance(newValue);
    }
    
    std::atomic<ObjP>* field = (std::atomic<ObjP>*)pField;
    ObjP oldRef = field->exchange(newValue);
    if (oldRef == newValue) {
        return;
    }
    if (newValue != NULL) {
        ARGC::retainReferenceCount(newValue);
    }
    if (oldRef != NULL) {
        ARGC::releaseReferenceCountAndPublish(oldRef);
    }
}

void ARGC_assignGenericObject(ARGC_FIELD_REF id* pField, id newValue) {
    if (*pField == newValue) {
        return;
    }

    std::atomic<id>* field = (std::atomic<id>*)pField;
    if (newValue != NULL) {
        if (ARGC::isJavaObject(newValue)) {
            ARGC::markInstance(newValue);
            ARGC::retainReferenceCount(newValue);
        }
        else {
            [newValue retain];
        }
    }
    
    id oldRef = field->exchange(newValue);
    
    if (oldRef != NULL) {
        if (ARGC::isJavaObject(oldRef)) {
            ARGC::releaseReferenceCountAndPublish(oldRef);
        }
        else {
            [oldRef release];
        }
    }
}


void ARGC_collectGarbage() {
    ARGC::_instance.doGC();
}


void ARGC::doGC() {
    if (_cntRef == 0) {
        return;
    }
    
    RefContext::change_generation();

    int idxSlot = _cntRef;
    RefSlot* pScan = getRefSlot(idxSlot);
    for (; idxSlot > 0; ) {
        pScan = (idxSlot-- % REFS_IN_BUCKET) ? --pScan : getRefSlot(idxSlot);
        ObjP obj = *pScan;
        if (obj != NULL && getExternalRefCount(obj) > 0) {
            markInstance(obj);
        }
    }
    
    int cntAlive = 0;
    RefSlot* pAlive = _table[0];
    for (idxSlot = 0; true; idxSlot ++, pScan++) {
        if (idxSlot >= _cntRef) {
            if (_cntRef.compare_exchange_weak(idxSlot, cntAlive)) {
                break;
            }
        }
        if ((idxSlot % REFS_IN_BUCKET) == 0) {
            pScan = getRefSlot(idxSlot);
        }
        ObjP obj = *pScan;
        if (obj == NULL) {
            continue;
        }
        if (!obj->_gc_info.isStrongReachable()) {
            if (pScan->compare_exchange_weak(obj, NULL)) {
                addPhantom(obj);
            }
        }
        else {
            if ((cntAlive % REFS_IN_BUCKET) == 0) {
                pAlive = getRefSlot(cntAlive);
            }
            *pAlive ++ = obj;
            obj->_gc_info.setSlotIndex(cntAlive ++);
            if (GC_DEBUG) {
                NSLog(@"alive %@, %d", obj, (int)NSExtraRefCount(obj));
            }
        }
    }
    
    ObjP dead = this->_phantomQ;
    this->_phantomQ = NULL;
    for (;dead != NULL;) {
        ObjP next = *(ObjP*)&dead->_gc_info;
        reclaimInstance(dead);
        dead = next;
    }

    if (GC_DEBUG) {
        NSLog(@"total alive %d -> %d", idxSlot, (int)this->_cntRef);
    }
}

extern "C" {
    typedef id volatile_id;
    
    id JreLoadVolatileId(volatile_id *pVar);
    id JreAssignVolatileId(volatile_id *pVar, id value);
    id JreVolatileStrongAssign(volatile_id *pIvar, id value);
    bool JreCompareAndSwapVolatileStrongId(volatile_id *pVar, id expected, id newValue);
    id JreExchangeVolatileStrongId(volatile_id *pVar, id newValue);
    void JreCloneVolatile(volatile_id *pVar, volatile_id *pOther);
    void JreCloneVolatileStrong(volatile_id *pVar, volatile_id *pOther);
    void JreReleaseVolatile(volatile_id *pVar);
    
    id JreRetainedWithAssign(id parent, __strong id *pIvar, id value);
    id JreVolatileRetainedWithAssign(id parent, volatile_id *pIvar, id value);
    void JreRetainedWithRelease(id parent, id child);
    void JreVolatileRetainedWithRelease(id parent, volatile_id *pVar);

void printRefCount(id obj) {
    NSLog(@"ref: %d -> %@", (int)NSExtraRefCount(obj), obj);
}
fgxgxfgxfg
    void ARGC_initARGCObject(id obj) {
        ((ObjP)obj)->_gc_info.initialize();
    }

    void ARGC_genericRetain(id obj) {
        if (ARGC::isJavaObject(obj)) {
            ARGC::retainReferenceCount(obj);
        }
        else {
            [obj retain];
        }
    }

    uintptr_t* ARGC_toVolatileIdPtr(ARGC_FIELD_REF id* ptr) {
        return (uintptr_t*)ptr;
    }

    id ARGC_allocateObject(Class cls, NSUInteger extraBytes, NSZone* zone) {
        return ARGC::allocateInstance(cls, extraBytes, zone);
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

}



id JreLoadVolatileId(volatile_id *pVar) {
    return *pVar;
}

id JreAssignVolatileId(volatile_id *pVar, id value) {
    ARGC_assignGenericObject(pVar, value);
    return value;
}

id JreVolatileStrongAssign(volatile_id *pIvar, id value) {
    ARGC_assignGenericObject(pIvar, value);
    return value;
}

bool JreCompareAndSwapVolatileStrongId(volatile_id *pVar, id expected, id newValue) {
    std::atomic<id>* field = (std::atomic<id>*)pVar;
    return field->compare_exchange_strong(expected, newValue);
}

id JreExchangeVolatileStrongId(volatile_id *pVar, id newValue) {
    std::atomic<id>* field = (std::atomic<id>*)pVar;
    id oldValue = field->exchange(newValue);
    return oldValue;
}

void JreReleaseVolatile(volatile_id *pVar) {
    // This is only called from a dealloc method, so we can assume there are no
    // concurrent threads with access to this address. Therefore, synchronization
    // is unnecessary.
    //RELEASE_(*(id *));
}

void JreCloneVolatile(volatile_id *pVar, volatile_id *pOther) {
    *pVar = *pOther;
}

void JreCloneVolatileStrong(volatile_id *pVar, volatile_id *pOther) {
    *pVar = *pOther;
}

