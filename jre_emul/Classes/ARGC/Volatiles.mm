
id JreLoadVolatileId(volatile_id *pVar) {
    id oid = *(std::atomic<id>*)pVar;
    if (oid != NULL) {
        [oid retain];
        [oid autorelease];
    };
    return oid;
}

id JreAssignVolatileId(volatile_id *pVar, id newValue) {
    ARGC_assignGenericObject((ARGC_FIELD_REF id*)pVar, newValue);
    return newValue;
}

void JreReleaseVolatile(volatile_id *pVar) {
    ARGC_assignGenericObject((ARGC_FIELD_REF id*)pVar, NULL);
}

id JreVolatileStrongAssign(volatile_id *pVar, id newValue) {
  ARGC_assignGenericObject((ARGC_FIELD_REF id*)pVar, newValue);
  return newValue;
}


id JreVolatileNativeAssign(volatile_id *pVar, id newValue) {
    ARGC_assignStrongObject((ARGC_FIELD_REF id*)pVar, newValue);
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
    id oldValue = field->exchange(newValue);
    if (oldValue == newValue) {
        return oldValue;
    }
    if (newValue) {
        [newValue retain];
    }
    if (oldValue) {
        if (GC_DEBUG && GC_LOG_ALLOC) {
            if ([oldValue toJObject] == NULL) {
                NSLog(@"--nstr %p #%d %@", oldValue, (int)NSExtraRefCount(oldValue), [oldValue class]);
            }
        }
        [oldValue autorelease];
    }
    return oldValue;
}

void JreCloneVolatile(volatile_id *pVar, volatile_id *pOther) {
    std::atomic<id>* pDst = (std::atomic<id>*)pVar;
    std::atomic<id>* pSrc = (std::atomic<id>*)pOther;
    id oid  = *pSrc;
    *pDst = oid;
}


void JreCloneVolatileStrong(volatile_id *pVar, volatile_id *pOther) {
    std::atomic<id>* pDst = (std::atomic<id>*)pVar;
    std::atomic<id>* pSrc = (std::atomic<id>*)pOther;
    id oid  = *pSrc;
    [oid retain];
    *pDst = oid;
}





