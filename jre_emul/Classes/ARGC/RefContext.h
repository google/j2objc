//
//  RefContext.h
//  JreEmulation
//
//  Created by DAE HOON JI on 2019/11/15.
//

#ifndef RefContext_h
#define RefContext_h

#import <atomic>

#define GC_DEBUG 1

static const int64_t BIND_COUNT_MASK        = 0x00fffFFFF;
static const int64_t PHANTOM_FLAG           = 0x02000LL * 0x10000;
static const int64_t FINALIZED_FLAG         = 0x04000LL * 0x10000;
static const int64_t WEAK_REACHABLE_FLAG    = 0x08000LL * 0x10000;
static const int64_t STRONG_REACHABLE_MASK  = 0x70000LL * 0x10000;
static const int64_t PENDING_RELEASE_FLAG   = 0x80000LL * 0x10000;
static const int64_t REACHABLE_MASK     = WEAK_REACHABLE_FLAG | STRONG_REACHABLE_MASK;
static const int SLOT_INDEX_SHIFT = (32+4);
static const int64_t SLOT_INDEX_MASK = (uint64_t)-1 << SLOT_INDEX_SHIFT;


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
        _flags = v;
    }
    
    int32_t slotIndex() {
        return _flags >> SLOT_INDEX_SHIFT;
    }
    
    void setSlotIndex(int64_t index) {
        index <<= SLOT_INDEX_SHIFT;
        while (true) {
            int64_t v = _flags;
            int64_t new_v = (v & ~SLOT_INDEX_MASK) | index;
            if (_flags.compare_exchange_strong(v, new_v)) {
                return;
            }
        }
    }

    void updateSlotIndex(int64_t index) {
        index <<= (32+4);
        while (true) {
            int64_t v = _flags;
            int64_t new_v = (v & ~((uint64_t)-1 << (32+4))) | index;
            if (_flags.compare_exchange_strong(v, new_v)) {
                return;
            }
        }
    }
    

    int32_t bindCount() {
        return _flags & BIND_COUNT_MASK;
    }
 
    BOOL isRootReachable() {
        return bindCount() > 0;
    }

    void bind() {
        _flags ++;
        if (GC_DEBUG) {
            assert((_flags & BIND_COUNT_MASK)  < BIND_COUNT_MASK);
        }
    }
    
    int32_t unbind() {
        int32_t cnt = (--_flags) & BIND_COUNT_MASK;
        if (GC_DEBUG) {
            assert(cnt < BIND_COUNT_MASK);
        }
        return cnt;
    }
    
    BOOL isReachable(BOOL isStrong) {
        return isStrong ? isStrongReachable() : isReachable() ;
    }

    BOOL isReachable() {
        return __isReachable(_flags);
    }

    BOOL isStrongReachable() {
        return __isStrongReachable(_flags);
    }
    
    BOOL isUntouchable() {
        return (_flags & REACHABLE_MASK) == 0;
    }

    BOOL markPendingRelease() {
        return this->markFlags(PENDING_RELEASE_FLAG);
    }

    BOOL isPendingRelease() {
        return (_flags & PENDING_RELEASE_FLAG) != 0;
    }
    
    BOOL markUntouchable() {
        return this->clearFlags(REACHABLE_MASK);
    }
    
    BOOL isFinalized() {
        return (_flags & FINALIZED_FLAG) != 0;
    }
    
    BOOL markFinalized() {
        return this->markFlags(FINALIZED_FLAG);
    }
    
    BOOL markPhantom() {
        return this->markFlags(PHANTOM_FLAG);
    }

    BOOL isPhantom() {
        return (_flags & PHANTOM_FLAG) != 0;
    }

    BOOL markReachable(BOOL isStrong) {
        return isStrong ? markStrongReachable() : markWeakReachable();
    }
    
    BOOL markWeakReachable() {
        while (true) {
            int64_t v = _flags;
            if (__isReachable(v)) {
                return false;
            }
            int64_t new_v = v | WEAK_REACHABLE_FLAG;
            if (_flags.compare_exchange_strong(v, new_v)) {
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
            int64_t v = _flags;
            if (__isStrongReachable(v)) {
                return false;
            }
            int64_t new_v = (v & ~REACHABLE_MASK) | strong_reachable_generation_bit;
            if (_flags.compare_exchange_strong(v, new_v)) {
                return true;
            }
        }
    }
    


private:
    BOOL markFlags(int64_t flag) {
        while (true) {
            int64_t v = _flags;
            if ((v & flag) != 0) {
                return false;
            }
            int64_t new_v = v | flag;
            if (_flags.compare_exchange_strong(v, new_v)) {
                return true;
            }
        }
    }

    BOOL clearFlags(int64_t flag) {
        while (true) {
            int64_t v = _flags;
            if ((v & flag) == 0) {
                return false;
            }
            int64_t new_v = v & ~flag;
            if (_flags.compare_exchange_strong(v, new_v)) {
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


    std::atomic<int64_t> _flags;
    static volatile int64_t strong_reachable_generation_bit;

public:

    static void change_generation() {
        strong_reachable_generation_bit = (strong_reachable_generation_bit << 1) & STRONG_REACHABLE_MASK;
        if (strong_reachable_generation_bit == 0) {
            strong_reachable_generation_bit = ((STRONG_REACHABLE_MASK << 1) ^ STRONG_REACHABLE_MASK) & STRONG_REACHABLE_MASK;
        }
    }
    
};
#endif /* RefContext_h */
