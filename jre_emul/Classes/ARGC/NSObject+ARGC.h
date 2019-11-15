//
//  NSObject+ARGC.h
//  GTest
//
//  Created by DAE HOON JI on 2019/11/15.
//  Copyright © 2019 DAE HOON JI. All rights reserved.
//

#ifndef NSObject_ARGC_h
#define NSObject_ARGC_h

@class ARGCObject;
typedef ARGCObject* JObj_p;
typedef uint16_t scan_offset_t;
typedef std::atomic<JObj_p> RefSlot;

typedef void (*ARGCObjectFieldVisitor)(__unsafe_unretained id, int depth) J2OBJC_METHOD_ATTR;

extern "C" {
    void JreFinalize(id self);
    void ARGC_genericRetain(id oid);
    void ARGC_genericRelease(id oid);
};

@interface ARGCObject() {
@public
    RefContext _rc;
}
- (void) forEachObjectField: (ARGCObjectFieldVisitor) visitor inDepth:(int) depth;
@end

@interface NSObject(ARGCObject)
- (JObj_p) toJObject;
- (void) forEachObjectField: (ARGCObjectFieldVisitor) visitor inDepth:(int) depth;
@end


@interface ARGCPhantom : NSObject {
@public
    ARGCPhantom* _next;
}
@end

@interface Counter : NSObject {
@public
    int count;
}
@end

@interface ScanOffsetArray : NSObject {
@public
    scan_offset_t offsets[1];
}
@end

//inline JObj_p oid2jobj(ARGCObject* oid) {
//    return (JObj*)oid;//->_rc;
//}
//
//inline ARGCObject* RefContext::toARGCObject() {
//    return (ARGCObject*)this;
//    //const NSUInteger offset = (NSUInteger)&((ARGCObject*)0)->_rc;
//    //return (ARGCObject*) ((char*)this - offset);
//}
//
//inline Class RefContext::getClass() {
//    ARGCObject* oid = toARGCObject();
//    return [oid class];
//}




#endif /* NSObject_ARGC_h */
