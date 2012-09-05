//
//  LinkedHashMap_PackagePrivate.h
//  JreEmulation
//
//  Created by Tom Ball on 2/23/12.
//  Copyright 2012 Google, Inc. All rights reserved.
//

#import "java/util/LinkedHashMap.h"
#import "java/util/HashMap_PackagePrivate.h"

// Non-public classes, methods and properties for LinkedHashMap.
@interface JavaUtilLinkedHashMap ()

@property (readonly) NSMutableArray *index;
@property (readonly) BOOL lastAccessedOrder;

@end

@interface JavaUtilLinkedHashMap_KeySet : JavaUtilHashMap_KeySet
@end

@interface JavaUtilLinkedHashMap_KeySetIterator :
    JavaUtilHashMap_KeySetIterator
@end


@interface JavaUtilLinkedHashMap_EntrySet : JavaUtilHashMap_EntrySet
@end

@interface JavaUtilLinkedHashMap_EntrySetIterator :
    JavaUtilHashMap_EntrySetIterator
@end


@interface JavaUtilLinkedHashMap_Values : JavaUtilHashMap_Values
@end

@interface JavaUtilLinkedHashMap_ValuesIterator :
    JavaUtilHashMap_ValuesIterator
@end
