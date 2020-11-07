// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// https://developers.google.com/protocol-buffers/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

//  Created by Keith Stanger on Mar. 20, 2013.
//
//  Hand written counterpart for com.google.protobuf.GeneratedMessage and
//  friends.

#include "com/google/protobuf/GeneratedMessage_PackagePrivate.h"

#include <map>
#include <string>
#include <vector>

#include "com/google/protobuf/ByteString.h"
#include "com/google/protobuf/CodedInputStream.h"
#include "com/google/protobuf/Descriptors_PackagePrivate.h"
#include "com/google/protobuf/ExtensionRegistry.h"
#include "com/google/protobuf/ExtensionRegistryLite.h"
#include "com/google/protobuf/Internal.h"
#include "com/google/protobuf/InvalidProtocolBufferException.h"
#include "com/google/protobuf/MapField.h"
#include "com/google/protobuf/ProtocolMessageEnum.h"
#include "com/google/protobuf/RepeatedField.h"
#include "com/google/protobuf/WireFormat.h"
#include "java/io/InputStream.h"
#include "java/lang/IllegalArgumentException.h"
#include "java/lang/IndexOutOfBoundsException.h"
#include "java/lang/StringBuilder.h"
#include "java/lang/UnsupportedOperationException.h"
#include "java/util/ArrayList.h"
#include "java/util/HashMap.h"

// GeneratedMessage is an abstract class so not all the methods in the Message
// protocol are implemented here.
#pragma GCC diagnostic ignored "-Wprotocol"
#pragma clang diagnostic ignored "-Wprotocol"
#pragma GCC diagnostic ignored "-Wincomplete-implementation"
#pragma clang diagnostic ignored "-Wincomplete-implementation"

#define NIL_CHECK_Int(value)
#define NIL_CHECK_Long(value)
#define NIL_CHECK_Float(value)
#define NIL_CHECK_Double(value)
#define NIL_CHECK_Bool(value)
#define NIL_CHECK_Enum(value) (void)nil_chk(value);
#define NIL_CHECK_Retainable(value) (void)nil_chk(value);

// Forward declarations.
class CGPExtensionValue;
class CGPExtensionMapComparator;
typedef std::map<CGPFieldDescriptor *, CGPExtensionValue, CGPExtensionMapComparator>
    CGPExtensionMap;

static void MergeFromMessage(
    id msg, CGPExtensionMap *msgExtensionMap, id other, CGPExtensionMap *otherExtensionMap,
    CGPDescriptor *descriptor);
static BOOL MergeFromStream(
    id msg, CGPDescriptor *descriptor, CGPCodedInputStream *stream,
    CGPExtensionRegistryLite *registry, CGPExtensionMap *extensionMap);
static inline int SerializedSizeForMessage(
    ComGoogleProtobufGeneratedMessage *msg, CGPDescriptor *descriptor);
static void WriteMessage(id msg, CGPDescriptor *descriptor, CGPCodedOutputStream *output);
static void MessageToString(
    id msg, CGPDescriptor *descriptor, NSMutableString *builder, int indent);

#define REPEATED_FIELD_GETTER_IMP(NAME) \
  CGP_ALWAYS_INLINE inline TYPE_##NAME CGPRepeatedFieldGet##NAME( \
      CGPRepeatedField *field, jint idx) { \
    CGPRepeatedFieldCheckBounds(field, idx); \
    return ((TYPE_##NAME *)field->data->buffer)[idx]; \
  }

FOR_EACH_TYPE_NO_ENUM(REPEATED_FIELD_GETTER_IMP)

#undef REPEATED_FIELD_GETTER_IMP

#define REPEATED_FIELD_ADDER_IMP(NAME) \
  CGP_ALWAYS_INLINE inline void CGPRepeatedFieldAdd##NAME( \
      CGPRepeatedField *field, TYPE_##NAME value) { \
    uint32_t total_size = CGPRepeatedFieldTotalSize(field); \
    if (CGPRepeatedFieldSize(field) == total_size) { \
      CGPRepeatedFieldReserve(field, total_size + 1, sizeof(TYPE_##NAME)); \
    } \
    ((TYPE_##NAME *)field->data->buffer)[field->data->size++] = TYPE_RETAIN_##NAME(value); \
  }

FOR_EACH_TYPE_WITH_ENUM(REPEATED_FIELD_ADDER_IMP)

#undef REPEATED_FIELD_ADDER_IMP

#define REPEATED_FIELD_SETTER_IMP(NAME) \
  CGP_ALWAYS_INLINE inline void CGPRepeatedFieldSet##NAME( \
      CGPRepeatedField *field, jint idx, TYPE_##NAME value) { \
    CGPRepeatedFieldCheckBounds(field, idx); \
    TYPE_##NAME *ptr = &((TYPE_##NAME *)field->data->buffer)[idx]; \
    TYPE_ASSIGN_##NAME(*ptr, value); \
  } \

FOR_EACH_TYPE_WITH_ENUM(REPEATED_FIELD_SETTER_IMP)

#undef REPEATED_FIELD_SETTER_IMP

// Declares a value type for the C++ extensions map to implement correct
// equality and handle memory management
class CGPExtensionValue {
 public:
  CGPExtensionValue() : value_(nil) {}

  CGPExtensionValue(const CGPExtensionValue &other) {
    if ([other.value_ conformsToProtocol:@protocol(JavaUtilList)]) {
      id<JavaUtilList> otherList = (id<JavaUtilList>)other.value_;
      value_ = [[JavaUtilArrayList alloc] initWithInt:[otherList size]];
      [value_ addAllWithJavaUtilCollection:otherList];
    } else {
      value_ = RETAIN_(other.value_);
    }
  }

  ~CGPExtensionValue() {
    AUTORELEASE(value_);
  }

  id get() { return value_; }

  void set(id value) {
    AUTORELEASE(value_);
    value_ = RETAIN_(value);
  }

  void set_retained(id __attribute__((ns_consumed)) retainedValue) {
    AUTORELEASE(value_);
    value_ = retainedValue;
  }

  bool operator==(const CGPExtensionValue &other) const {
    return [value_ isEqual:other.value_];
  }

  bool operator!=(const CGPExtensionValue &other) const {
    return !(*this == other);
  }

 private:
  id value_;
};

class CGPExtensionMapComparator {
 public:
  bool operator()(const CGPFieldDescriptor *left, const CGPFieldDescriptor *right) const {
    return CGPFieldGetNumber(left) < CGPFieldGetNumber(right);
  }
};

@interface ComGoogleProtobufGeneratedMessage_ExtendableMessage () {
 @package
  CGPExtensionMap extensionMap_;
}
@end

@interface ComGoogleProtobufGeneratedMessage_ExtendableBuilder () {
 @package
  CGPExtensionMap extensionMap_;
}
@end

static inline CGPExtensionMap *MessageExtensionMap(id msg, CGPDescriptor *descriptor) {
  return CGPIsExtendable(descriptor) ?
      &((ComGoogleProtobufGeneratedMessage_ExtendableMessage *)msg)->extensionMap_ : NULL;
}

static inline CGPExtensionMap *BuilderExtensionMap(id msg, CGPDescriptor *descriptor) {
  return CGPIsExtendable(descriptor) ?
      &((ComGoogleProtobufGeneratedMessage_ExtendableBuilder *)msg)->extensionMap_ : NULL;
}

// This struct describes how to access a field's "has" state. For regular
// singular fields, there is a single bit representing the "has" state. For
// oneof fields, the "has" state can be determined from the value of the current
// oneof case.
typedef struct {
  size_t offset;
  union {
    uint32_t mask;  // For regular fields that use the has bit.
    jint fieldNum;  // For oneof fields.
  };
  bool isOneof;
} CGPHasLocator;

// We use the most significant bit (sign bit) of the oneof case field number to
// indicate that the field is retainable. This allows the value to be correctly
// released when a new value is being set without having to look up the previous
// value's field descriptor.
#define ONEOF_FIELD_NUM_MASK 0x7fffffff
#define ONEOF_RETAINABLE_MASK 0x80000000

static CGPHasLocator GetHasLocator(Class cls, const CGPFieldDescriptor *field) {
  CGPOneofDescriptor *oneof = field->containingOneof_;
  CGPHasLocator result;
  result.isOneof = oneof != nil;
  if (oneof) {
    result.offset = CGPOneofGetOffset(oneof, cls);
    result.fieldNum = CGPFieldGetNumber(field);
    if (CGPIsRetainedType(CGPFieldGetJavaType(field))) {
      // The sign bit is used to indicate that the current value need to be
      // released when a new field in the oneof is set.
      result.fieldNum |= ONEOF_RETAINABLE_MASK;
    }
  } else {
    uint32_t idx = CGPFieldGetHasBitIndex(field);
    uint32_t byteIndex = idx / 32;
    result.mask = (1 << (idx % 32));
    result.offset = class_getInstanceSize(cls) + byteIndex * sizeof(uint32_t);
  }
  return result;
}

static bool GetHas(id msg, CGPHasLocator loc) {
  uintptr_t ptr = (uintptr_t)msg + loc.offset;
  if (loc.isOneof) {
    return *(jint *)ptr == loc.fieldNum;
  } else {
    return (*(uint32_t *)ptr & loc.mask) ? true : false;
  }
}

// Sets the "has" state of a field to "on". This should always be paired with a
// call to ClearPreviousOneof() to ensure that the previous value of a oneof
// field is properly released.
static void SetHas(id msg, CGPHasLocator loc) {
  uintptr_t ptr = (uintptr_t)msg + loc.offset;
  if (loc.isOneof) {
    *(jint *)ptr = loc.fieldNum;
  } else {
    *(uint32_t *)ptr |= loc.mask;
  }
}

// Returns whether the "has" state has been unset. For oneof fields this will be
// false if the oneof is currently set with a different field.
static bool UnsetHas(id msg, CGPHasLocator loc) {
  uintptr_t ptr = (uintptr_t)msg + loc.offset;
  if (loc.isOneof) {
    jint *oneofCase = (jint *)ptr;
    if (*oneofCase == loc.fieldNum) {
      *oneofCase = 0;
      return true;
    }
    return false;
  } else {
    *(uint32_t *)ptr &= ~loc.mask;
    return true;
  }
}

// Clears and releases the previous value iff it is a oneof field and the oneof
// was previously set to a different field.
static inline void ClearPreviousOneof(id msg, CGPHasLocator loc, uintptr_t ptr) {
  if (loc.isOneof) {
    jint *oneofCase = (jint *)((uintptr_t)msg + loc.offset);
    // Only clear if the oneof is set to a different field. Merging logic relies
    // on the value being preserved if it is for the correct field.
    if (*oneofCase != loc.fieldNum) {
      if (*oneofCase & ONEOF_RETAINABLE_MASK) {
        id *objPtr = (id *)ptr;
        AUTORELEASE(*objPtr);
        *objPtr = nil;
      }
      *oneofCase = 0;
    }
  }
}

#define REPEATED_FIELD_PTR(msg, offset) ((CGPRepeatedField *)((uint8_t *)msg + offset))
#define MAP_FIELD_PTR(msg, offset) ((CGPMapField *)((uint8_t *)msg + offset))
#define FIELD_PTR(TYPE, msg, offset) ((TYPE *)((uint8_t *)msg + offset))

#define SINGULAR_SETTER_IMP(NAME) \
  static void SingularSet##NAME(id msg, TYPE_##NAME value, size_t offset, CGPHasLocator hasLoc) { \
    TYPE_##NAME *ptr = FIELD_PTR(TYPE_##NAME, msg, offset); \
    ClearPreviousOneof(msg, hasLoc, (uintptr_t)ptr); \
    TYPE_ASSIGN_##NAME(*ptr, value); \
    SetHas(msg, hasLoc); \
  }

FOR_EACH_TYPE_WITH_ENUM(SINGULAR_SETTER_IMP)

#undef SINGULAR_SETTER_IMP


// *****************************************************************************
// ********** Dynamic field accessors ******************************************
// *****************************************************************************

#define SINGULAR_GETTER_IMP(NAME) \
  static IMP GetSingularGetterImp##NAME( \
      size_t offset, CGPHasLocator hasLoc, TYPE_##NAME defaultValue) { \
    return imp_implementationWithBlock(^TYPE_##NAME(id msg) { \
      if (GetHas(msg, hasLoc)) { \
        return *FIELD_PTR(TYPE_##NAME, msg, offset); \
      } \
      return defaultValue; \
    }); \
  }

FOR_EACH_TYPE_NO_ENUM(SINGULAR_GETTER_IMP)

#undef SINGULAR_GETTER_IMP

#define REPEATED_GETTER_IMP(NAME) \
  static IMP GetRepeatedGetterImp##NAME(size_t offset) { \
    return imp_implementationWithBlock(^TYPE_##NAME(id msg, jint idx) { \
      return CGPRepeatedFieldGet##NAME(REPEATED_FIELD_PTR(msg, offset), idx); \
    }); \
  }

FOR_EACH_TYPE_NO_ENUM(REPEATED_GETTER_IMP)

#undef REPEATED_GETTER_IMP

static BOOL AddGetterMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  BOOL repeated = CGPFieldIsRepeated(field);
  IMP imp = NULL;
  char encoding[64];
  size_t offset = CGPFieldGetOffset(field, cls);
  CGPHasLocator hasLoc = GetHasLocator(cls, field);

#define ADD_GETTER_METHOD_CASE(NAME) \
  imp = repeated ? GetRepeatedGetterImp##NAME(offset) : \
      GetSingularGetterImp##NAME(offset, hasLoc, field->data_->defaultValue.value##NAME); \
  strcpy(encoding, @encode(TYPE_##NAME)); \
  break;

  SWITCH_TYPES_NO_ENUM(CGPFieldGetJavaType(field), ADD_GETTER_METHOD_CASE)

#undef ADD_GETTER_METHOD_CASE

  strcat(encoding, "@:");
  if (repeated) {
    strcat(encoding, @encode(int));
  }
  return class_addMethod(cls, sel, imp, encoding);
}

static BOOL AddHasMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  CGPHasLocator loc = GetHasLocator(cls, field);
  IMP imp = imp_implementationWithBlock(^jboolean(id msg) {
    return GetHas(msg, loc);
  });
  char encoding[64];
  strcpy(encoding, @encode(jboolean));
  strcat(encoding, "@:");
  return class_addMethod(cls, sel, imp, encoding);
}

static BOOL AddCountMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  size_t offset = CGPFieldGetOffset(field, cls);
  IMP imp;
  if (CGPFieldIsMap(field)) {
    CGPFieldJavaType keyType = CGPFieldGetJavaType(CGPFieldMapKey(field));
    CGPFieldJavaType valueType = CGPFieldGetJavaType(CGPFieldMapValue(field));
    imp = imp_implementationWithBlock(^jint(id msg) {
      return CGPMapFieldMapSize(MAP_FIELD_PTR(msg, offset), keyType, valueType);
    });
  } else {
    imp = imp_implementationWithBlock(^jint(id msg) {
      return CGPRepeatedFieldSize(REPEATED_FIELD_PTR(msg, offset));
    });
  }
  char encoding[64];
  strcpy(encoding, @encode(jint));
  strcat(encoding, "@:");
  return class_addMethod(cls, sel, imp, encoding);
}

static BOOL AddListGetterMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  size_t offset = CGPFieldGetOffset(field, cls);
  CGPFieldJavaType type = CGPFieldGetJavaType(field);
  IMP imp = imp_implementationWithBlock(^id(id msg) {
    return AUTORELEASE(CGPNewRepeatedFieldList(REPEATED_FIELD_PTR(msg, offset), type));
  });
  return class_addMethod(cls, sel, imp, "@@:");
}

static BOOL AddMapGetterMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  size_t offset = CGPFieldGetOffset(field, cls);
  CGPFieldJavaType keyType = CGPFieldGetJavaType(CGPFieldMapKey(field));
  CGPFieldJavaType valueType = CGPFieldGetJavaType(CGPFieldMapValue(field));
  IMP imp = imp_implementationWithBlock(^id(id msg) {
    return CGPMapFieldAsJavaMap(MAP_FIELD_PTR(msg, offset), keyType, valueType);
  });
  return class_addMethod(cls, sel, imp, "@@:");
}

static BOOL AddClearMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  IMP imp;
  size_t offset = CGPFieldGetOffset(field, cls);
  CGPFieldJavaType type = CGPFieldGetJavaType(field);
  if (CGPFieldIsMap(field)) {
    CGPFieldJavaType keyType = CGPFieldGetJavaType(CGPFieldMapKey(field));
    CGPFieldJavaType valueType = CGPFieldGetJavaType(CGPFieldMapValue(field));
    imp = imp_implementationWithBlock(^id(id msg) {
      CGPMapFieldClear(MAP_FIELD_PTR(msg, offset), keyType, valueType);
      return msg;
    });
  } else if (CGPFieldIsRepeated(field)) {
    imp = imp_implementationWithBlock(^id(id msg) {
      CGPRepeatedFieldClear(REPEATED_FIELD_PTR(msg, offset), type);
      return msg;
    });
  } else {
    CGPHasLocator hasLoc = GetHasLocator(cls, field);
    if (CGPIsRetainedType(type)) {
      imp = imp_implementationWithBlock(^id(id msg) {
        if (UnsetHas(msg, hasLoc)) {
          id *ptr = FIELD_PTR(id, msg, offset);
          AUTORELEASE(*ptr);
          *ptr = nil;
        }
        return msg;
      });
    } else {
      imp = imp_implementationWithBlock(^id(id msg) {
        UnsetHas(msg, hasLoc);
        return msg;
      });
    }
  }
  return class_addMethod(cls, sel, imp, "@@:");
}

#define GET_SINGULAR_SETTER_IMP(NAME) \
  static IMP GetSingularSetterImp##NAME(size_t offset, CGPHasLocator hasLoc) { \
    return imp_implementationWithBlock(^id(id msg, TYPE_##NAME value) { \
      NIL_CHECK_##NAME(value) \
      SingularSet##NAME(msg, value, offset, hasLoc); \
      return msg; \
    }); \
  }

FOR_EACH_TYPE_WITH_ENUM(GET_SINGULAR_SETTER_IMP)

#undef GET_SINGULAR_SETTER_IMP

#define GET_REPEATED_SETTER_IMP(NAME) \
  static IMP GetRepeatedSetterImp##NAME(size_t offset) { \
    return imp_implementationWithBlock(^id(id msg, jint idx, TYPE_##NAME value) { \
      NIL_CHECK_##NAME(value) \
      CGPRepeatedFieldSet##NAME(REPEATED_FIELD_PTR(msg, offset), idx, value); \
      return msg; \
    }); \
  }

FOR_EACH_TYPE_WITH_ENUM(GET_REPEATED_SETTER_IMP)

#undef GET_REPEATED_SETTER_IMP

static BOOL AddSetterMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  BOOL repeated = CGPFieldIsRepeated(field);
  IMP imp = NULL;
  char encoding[64];
  strcpy(encoding, "@@:");
  if (repeated) {
    strcat(encoding, @encode(int));
  }
  size_t offset = CGPFieldGetOffset(field, cls);

#define ADD_SETTER_METHOD_CASE(NAME) \
  imp = repeated ? GetRepeatedSetterImp##NAME(offset) : \
      GetSingularSetterImp##NAME(offset, GetHasLocator(cls, field)); \
  strcat(encoding, @encode(TYPE_##NAME)); \
  break;

  SWITCH_TYPES_WITH_ENUM(CGPFieldGetJavaType(field), ADD_SETTER_METHOD_CASE)

#undef ADD_SETTER_METHOD_CASE

  return class_addMethod(cls, sel, imp, encoding);
}

static BOOL AddBuilderSetterMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  size_t offset = CGPFieldGetOffset(field, cls);
  CGPHasLocator hasLoc = GetHasLocator(cls, field);
  IMP imp = imp_implementationWithBlock(^id(id msg, id value) {
    (void)nil_chk(value);
    id *ptr = FIELD_PTR(id, msg, offset);
    ClearPreviousOneof(msg, hasLoc, (uintptr_t)ptr);
    id builtValue = [(ComGoogleProtobufGeneratedMessage_Builder *)value build];
    AUTORELEASE(*ptr);
    *ptr = RETAIN_(builtValue);
    SetHas(msg, hasLoc);
    return msg;
  });
  return class_addMethod(cls, sel, imp, "@@:@");
}

#define GET_ADDER_IMP(NAME) \
  static IMP GetAdderImp##NAME(size_t offset) { \
    return imp_implementationWithBlock(^id(id msg, TYPE_##NAME value) { \
      NIL_CHECK_##NAME(value) \
      CGPRepeatedFieldAdd##NAME(REPEATED_FIELD_PTR(msg, offset), value); \
      return msg; \
    }); \
  }

FOR_EACH_TYPE_WITH_ENUM(GET_ADDER_IMP)

#undef GET_ADDER_IMP

static BOOL AddAdderMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  IMP imp = NULL;
  char encoding[64];
  strcpy(encoding, "@@:");
  size_t offset = CGPFieldGetOffset(field, cls);

#define ADD_ADDER_METHOD_CASE(NAME) \
  imp = GetAdderImp##NAME(offset); \
  strcat(encoding, @encode(TYPE_##NAME)); \
  break;

  SWITCH_TYPES_WITH_ENUM(CGPFieldGetJavaType(field), ADD_ADDER_METHOD_CASE)

#undef ADD_ADDER_METHOD_CASE

  return class_addMethod(cls, sel, imp, encoding);
}

static BOOL AddBuilderAdderMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  size_t offset = CGPFieldGetOffset(field, cls);
  IMP imp = imp_implementationWithBlock(^id(
      id msg, ComGoogleProtobufGeneratedMessage_Builder *value) {
    (void)nil_chk(value);
    CGPRepeatedFieldAddRetainable(REPEATED_FIELD_PTR(msg, offset), [value build]);
    return msg;
  });
  return class_addMethod(cls, sel, imp, "@@:@");
}

#define GET_ADD_ALL_IMP(NAME) \
  static IMP GetAddAllImp##NAME(size_t offset) { \
    return imp_implementationWithBlock(^id(id msg, id<JavaLangIterable> values) { \
      CGPRepeatedField *repeatedField = REPEATED_FIELD_PTR(msg, offset); \
      for (id value in nil_chk(values)) { \
        CGPRepeatedFieldAdd##NAME(repeatedField, CGPUnboxValue##NAME(nil_chk(value))); \
      } \
      return msg; \
    }); \
  }

FOR_EACH_TYPE_WITH_ENUM(GET_ADD_ALL_IMP)

#undef GET_ADD_ALL_IMP

static BOOL AddAddAllMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  IMP imp = NULL;
  size_t offset = CGPFieldGetOffset(field, cls);

#define ADD_ALL_METHOD_CASE(NAME) \
  imp = GetAddAllImp##NAME(offset); \
  break;

  SWITCH_TYPES_WITH_ENUM(CGPFieldGetJavaType(field), ADD_ALL_METHOD_CASE)

#undef ADD_ALL_METHOD_CASE

  return class_addMethod(cls, sel, imp, "@@:@");
}

#define GET_CONTAINS_IMP(NAME) \
  static IMP GetContainsImp##NAME( \
      size_t offset, CGPFieldJavaType keyType, CGPFieldJavaType valueType) { \
    return imp_implementationWithBlock(^jboolean(id msg, TYPE_##NAME pKey) { \
      CGPValue key; \
      key.CGPValueField_##NAME = pKey; \
      return CGPMapFieldGetWithKey(MAP_FIELD_PTR(msg, offset), key, keyType, valueType) != nil; \
    }); \
  }

GET_CONTAINS_IMP(Int)
GET_CONTAINS_IMP(Long)
GET_CONTAINS_IMP(Bool)
GET_CONTAINS_IMP(Id)

#undef GET_CONTAINS_IMP

static BOOL AddContainsMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  IMP imp = NULL;
  char encoding[64];
  strcpy(encoding, @encode(jboolean));
  strcat(encoding, "@:");
  size_t offset = CGPFieldGetOffset(field, cls);
  CGPFieldJavaType keyType = CGPFieldGetJavaType(CGPFieldMapKey(field));
  CGPFieldJavaType valueType = CGPFieldGetJavaType(CGPFieldMapValue(field));

#define CONTAINS_METHOD_CASE(NAME) \
  imp = GetContainsImp##NAME(offset, keyType, valueType); \
  strcat(encoding, @encode(TYPE_##NAME)); \
  break;

  switch (keyType) {
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_INT:
      CONTAINS_METHOD_CASE(Int)
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_LONG:
      CONTAINS_METHOD_CASE(Long)
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BOOLEAN:
      CONTAINS_METHOD_CASE(Bool)
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_STRING:
      CONTAINS_METHOD_CASE(Id)
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_FLOAT:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_DOUBLE:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_ENUM:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BYTE_STRING:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_MESSAGE:
      return NO;
  }

#undef CONTAINS_METHOD_CASE

  return class_addMethod(cls, sel, imp, encoding);
}

#define GET_MAP_GETTER_IMP(KEY_NAME, VALUE_NAME) \
  static IMP GetMapGetOrThrowImp##KEY_NAME##VALUE_NAME( \
      size_t offset, CGPFieldJavaType keyType, CGPFieldJavaType valueType) { \
    return imp_implementationWithBlock(^TYPE_##VALUE_NAME(id msg, TYPE_##KEY_NAME pKey) { \
      CGPValue key; \
      key.CGPValueField_##KEY_NAME = pKey; \
      CGPMapFieldEntry *entry = CGPMapFieldGetWithKey( \
          MAP_FIELD_PTR(msg, offset), key, keyType, valueType); \
      if (entry) { \
        return entry->value.CGPValueField_##VALUE_NAME; \
      } \
      @throw create_JavaLangIllegalArgumentException_init(); \
    }); \
  } \
  static IMP GetMapGetOrDefaultImp##KEY_NAME##VALUE_NAME( \
      size_t offset, CGPFieldJavaType keyType, CGPFieldJavaType valueType) { \
    return imp_implementationWithBlock(^TYPE_##VALUE_NAME( \
        id msg, TYPE_##KEY_NAME pKey, TYPE_##VALUE_NAME defaultValue) { \
      CGPValue key; \
      key.CGPValueField_##KEY_NAME = pKey; \
      CGPMapFieldEntry *entry = CGPMapFieldGetWithKey( \
          MAP_FIELD_PTR(msg, offset), key, keyType, valueType); \
      if (entry) { \
        return entry->value.CGPValueField_##VALUE_NAME; \
      } \
      return defaultValue; \
    }); \
  }

#define GET_MAP_GETTER_IMP_FOR_VALUE(VALUE_NAME) \
  GET_MAP_GETTER_IMP(Int, VALUE_NAME) \
  GET_MAP_GETTER_IMP(Long, VALUE_NAME) \
  GET_MAP_GETTER_IMP(Bool, VALUE_NAME) \
  GET_MAP_GETTER_IMP(Id, VALUE_NAME) \

FOR_EACH_TYPE_NO_ENUM(GET_MAP_GETTER_IMP_FOR_VALUE)

#undef GET_MAP_GETTER_IMP

static BOOL AddMapGetWithKeyMethod(Class cls, SEL sel, CGPFieldDescriptor *field, bool orDefault) {
  IMP imp = NULL;
  char encoding[64];
  size_t offset = CGPFieldGetOffset(field, cls);
  CGPFieldJavaType keyType = CGPFieldGetJavaType(CGPFieldMapKey(field));
  CGPFieldJavaType valueType = CGPFieldGetJavaType(CGPFieldMapValue(field));

#define MAP_GETTER_CASE(KEY_NAME, VALUE_NAME) \
  imp = orDefault ? GetMapGetOrDefaultImp##KEY_NAME##VALUE_NAME(offset, keyType, valueType) : \
      GetMapGetOrThrowImp##KEY_NAME##VALUE_NAME(offset, keyType, valueType); \
  strcat(encoding, @encode(TYPE_##KEY_NAME)); \
  break;

#define MAP_GETTER_INNER_SWITCH(VALUE_NAME) \
  strcpy(encoding, @encode(TYPE_##VALUE_NAME)); \
  strcat(encoding, "@:"); \
  switch (keyType) { \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_INT: \
      MAP_GETTER_CASE(Int, VALUE_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_LONG: \
      MAP_GETTER_CASE(Long, VALUE_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BOOLEAN: \
      MAP_GETTER_CASE(Bool, VALUE_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_STRING: \
      MAP_GETTER_CASE(Id, VALUE_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_FLOAT: \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_DOUBLE: \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_ENUM: \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BYTE_STRING: \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_MESSAGE: \
      return NO; \
  } \
  if (orDefault) { \
    strcat(encoding, @encode(TYPE_##VALUE_NAME)); \
  } \
  break;

  SWITCH_TYPES_NO_ENUM(valueType, MAP_GETTER_INNER_SWITCH)

#undef MAP_GETTER_CASE
#undef MAP_GETTER_INNER_SWITCH

  return class_addMethod(cls, sel, imp, encoding);
}

#define GET_PUT_IMP(KEY_NAME, VALUE_NAME) \
  static IMP GetPutImp##KEY_NAME##VALUE_NAME( \
      size_t offset, CGPFieldJavaType keyType, CGPFieldJavaType valueType) { \
    return imp_implementationWithBlock( \
        ^id(id msg, TYPE_##KEY_NAME pKey, TYPE_##VALUE_NAME pValue) { \
      CGPValue key; \
      key.CGPValueField_##KEY_NAME = pKey; \
      CGPValue value; \
      value.CGPValueField_##VALUE_NAME = pValue; \
      CGPMapFieldPut( \
          MAP_FIELD_PTR(msg, offset), key, keyType, value, valueType, \
          /* retainedKeyAndValue */ false); \
      return msg; \
    }); \
  }

#define GET_PUT_IMP_FOR_VALUE(VALUE_NAME) \
  GET_PUT_IMP(Int, VALUE_NAME) \
  GET_PUT_IMP(Long, VALUE_NAME) \
  GET_PUT_IMP(Bool, VALUE_NAME) \
  GET_PUT_IMP(Id, VALUE_NAME)

FOR_EACH_TYPE_NO_ENUM(GET_PUT_IMP_FOR_VALUE)

#undef GET_PUT_IMP
#undef GET_PUT_IMP_FOR_VALUE

static BOOL AddPutMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  IMP imp = NULL;
  char encoding[64];
  strcpy(encoding, "@@:");
  size_t offset = CGPFieldGetOffset(field, cls);
  CGPFieldJavaType keyType = CGPFieldGetJavaType(CGPFieldMapKey(field));
  CGPFieldJavaType valueType = CGPFieldGetJavaType(CGPFieldMapValue(field));

#define PUT_METHOD_CASE(KEY_NAME, VALUE_NAME) \
  imp = GetPutImp##KEY_NAME##VALUE_NAME(offset, keyType, valueType); \
  strcat(encoding, @encode(TYPE_##KEY_NAME)); \
  break;

#define PUT_METHOD_INNER_SWITCH(VALUE_NAME) \
  switch (keyType) { \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_INT: \
      PUT_METHOD_CASE(Int, VALUE_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_LONG: \
      PUT_METHOD_CASE(Long, VALUE_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BOOLEAN: \
      PUT_METHOD_CASE(Bool, VALUE_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_STRING: \
      PUT_METHOD_CASE(Id, VALUE_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_FLOAT: \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_DOUBLE: \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_ENUM: \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BYTE_STRING: \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_MESSAGE: \
      return NO; \
  } \
  strcat(encoding, @encode(TYPE_##VALUE_NAME)); \
  break;

  SWITCH_TYPES_NO_ENUM(valueType, PUT_METHOD_INNER_SWITCH)

#undef PUT_METHOD_CASE
#undef PUT_METHOD_INNER_SWITCH

  return class_addMethod(cls, sel, imp, encoding);
}

#define GET_MAP_REMOVE_IMP(NAME) \
  static IMP GetMapRemoveImp##NAME( \
      size_t offset, CGPFieldJavaType keyType, CGPFieldJavaType valueType) { \
    return imp_implementationWithBlock(^id(id msg, TYPE_##NAME pKey) { \
      CGPValue key; \
      key.CGPValueField_##NAME = pKey; \
      CGPMapFieldRemove(MAP_FIELD_PTR(msg, offset), key, keyType, valueType); \
      return msg; \
    }); \
  }

GET_MAP_REMOVE_IMP(Int)
GET_MAP_REMOVE_IMP(Long)
GET_MAP_REMOVE_IMP(Bool)
GET_MAP_REMOVE_IMP(Id)

#undef GET_REMOVE_IMP

static BOOL AddMapRemoveMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  IMP imp = NULL;
  char encoding[64];
  strcpy(encoding, "@@:");
  size_t offset = CGPFieldGetOffset(field, cls);
  CGPFieldJavaType keyType = CGPFieldGetJavaType(CGPFieldMapKey(field));
  CGPFieldJavaType valueType = CGPFieldGetJavaType(CGPFieldMapValue(field));

#define REMOVE_METHOD_CASE(NAME) \
  imp = GetMapRemoveImp##NAME(offset, keyType, valueType); \
  strcat(encoding, @encode(TYPE_##NAME)); \
  break;

  switch (keyType) {
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_INT:
      REMOVE_METHOD_CASE(Int)
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_LONG:
      REMOVE_METHOD_CASE(Long)
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BOOLEAN:
      REMOVE_METHOD_CASE(Bool)
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_STRING:
      REMOVE_METHOD_CASE(Id)
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_FLOAT:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_DOUBLE:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_ENUM:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BYTE_STRING:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_MESSAGE:
      return NO;
  }

#undef REMOVE_METHOD_CASE

  return class_addMethod(cls, sel, imp, encoding);
}

static BOOL AddRepeatedMessageRemoveMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  size_t offset = CGPFieldGetOffset(field, cls);
  IMP imp = imp_implementationWithBlock(^id(id msg, TYPE_Int index) {
    CGPRepeatedMessageFieldRemove(REPEATED_FIELD_PTR(msg, offset), index);
    return msg;
  });
  return class_addMethod(cls, sel, imp, "@@:i");
}

static IMP GetOneofImp(size_t offset, Class cls) {
  return imp_implementationWithBlock(^id(id msg) {
    jint number = *FIELD_PTR(jint, msg, offset);
    // Remove the sign bit which marks whether the value is retainable.
    number &= ONEOF_FIELD_NUM_MASK;
    return [cls forNumberWithInt:number];
  });
}

static BOOL AddOneofGetterMethod(Class cls, SEL sel, CGPOneofDescriptor *oneof) {
  size_t offset = CGPOneofGetOffset(oneof, cls);
  IMP imp = GetOneofImp(offset, CGPOneofGetCaseClass(oneof));
  return class_addMethod(cls, sel, imp, "@@:");
}

static const char *GetParamKeyword(CGPFieldDescriptor *field) {
  switch (CGPFieldGetJavaType(field)) {
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_INT: return "Int";
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_LONG: return "Long";
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_FLOAT: return "Float";
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_DOUBLE: return "Double";
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BOOLEAN: return "Boolean";
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_STRING: return "NSString";
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BYTE_STRING:
      return "ComGoogleProtobufByteString";
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_ENUM:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_MESSAGE:
      return class_getName(field->data_->objcType);
  }
  __builtin_unreachable();
}

static bool Matches(const char **strPtr, const char *match, size_t len) {
  if (strncmp(*strPtr, match, len) == 0) {
    *strPtr += len;
    return true;
  }
  return false;
}

static bool MatchesStr(const char **strPtr, const char *match) {
  return Matches(strPtr, match, strlen(match));
}

static bool MatchesKeyword(const char **strPtr, CGPFieldDescriptor *field) {
  return MatchesStr(strPtr, GetParamKeyword(field));
}

static bool MatchesName(const char **strPtr, CGPFieldDescriptor *field) {
  return MatchesStr(strPtr, field->data_->javaName);
}

static bool MatchesEnd(const char *str, const char *match) {
  return strcmp(str, match) == 0;
}

static BOOL ResolveGetAccessor(Class cls, CGPDescriptor *descriptor, SEL sel, const char *selName) {
  IOSObjectArray *fields = descriptor->fields_;
  ComGoogleProtobufDescriptors_FieldDescriptor **fieldsBuf = fields->buffer_;
  NSUInteger count = fields->size_;
  for (NSUInteger i = 0; i < count; ++i) {
    ComGoogleProtobufDescriptors_FieldDescriptor *field = fieldsBuf[i];
    const char *tail = selName;
    if (!MatchesName(&tail, field)) {
      continue;
    }
    const char *tail2 = tail;
    if (CGPFieldIsMap(field)) {
      if (MatchesEnd(tail, "Count")) {
        return AddCountMethod(cls, sel, field);
      } else if (MatchesEnd(tail, "Map")) {
        return AddMapGetterMethod(cls, sel, field);
      } else if (Matches(&tail, "OrThrowWith", 11)
          && MatchesKeyword(&tail, CGPFieldMapKey(field)) && MatchesEnd(tail, ":")) {
        return AddMapGetWithKeyMethod(cls, sel, field, false);
      } else if (Matches(&tail2, "OrDefaultWith", 13)
          && MatchesKeyword(&tail2, CGPFieldMapKey(field)) && Matches(&tail2, ":with", 5)
          && MatchesKeyword(&tail2, CGPFieldMapValue(field)) && MatchesEnd(tail2, ":")) {
        return AddMapGetWithKeyMethod(cls, sel, field, true);
      }
    } else if (CGPFieldIsRepeated(field)) {
      if (MatchesEnd(tail, "WithInt:")) {
        return AddGetterMethod(cls, sel, field);
      } else if (MatchesEnd(tail, "Count")) {
        return AddCountMethod(cls, sel, field);
      } else if (MatchesEnd(tail, "List")) {
        return AddListGetterMethod(cls, sel, field);
      }
    } else {
      if (*tail == 0) {
        return AddGetterMethod(cls, sel, field);
      }
    }
  }

  IOSObjectArray *oneofs = descriptor->oneofs_;
  if (oneofs) {
    ComGoogleProtobufDescriptors_OneofDescriptor **oneofsBuf = oneofs->buffer_;
    NSUInteger oneofsCount = oneofs->size_;
    for (NSUInteger i = 0; i < oneofsCount; ++i) {
      ComGoogleProtobufDescriptors_OneofDescriptor *oneof = oneofsBuf[i];
      const char *tail = selName;
      if (MatchesStr(&tail, oneof->data_->javaName) && MatchesEnd(tail, "Case")) {
        return AddOneofGetterMethod(cls, sel, oneof);
      }
    }
  }
  return NO;
}

static BOOL ResolveHasAccessor(Class cls, CGPDescriptor *descriptor, SEL sel, const char *selName) {
  IOSObjectArray *fields = descriptor->fields_;
  ComGoogleProtobufDescriptors_FieldDescriptor **fieldsBuf = fields->buffer_;
  NSUInteger count = fields->size_;
  for (NSUInteger i = 0; i < count; ++i) {
    ComGoogleProtobufDescriptors_FieldDescriptor *field = fieldsBuf[i];
    if (!CGPFieldIsRepeated(field) && MatchesEnd(selName, field->data_->javaName)) {
      return AddHasMethod(cls, sel, field);
    }
  }
  return NO;
}

static BOOL ResolveSetAccessor(Class cls, CGPDescriptor *descriptor, SEL sel, const char *selName) {
  IOSObjectArray *fields = descriptor->fields_;
  ComGoogleProtobufDescriptors_FieldDescriptor **fieldsBuf = fields->buffer_;
  NSUInteger count = fields->size_;
  for (NSUInteger i = 0; i < count; ++i) {
    ComGoogleProtobufDescriptors_FieldDescriptor *field = fieldsBuf[i];
    if (CGPFieldIsMap(field)) {
      continue;
    }
    const char *tail = selName;
    if (MatchesName(&tail, field)
        && (CGPFieldIsRepeated(field) ? Matches(&tail, "WithInt:with", 12)
            : Matches(&tail, "With", 4))
        && MatchesKeyword(&tail, field)) {
      if (MatchesEnd(tail, ":")) {
        return AddSetterMethod(cls, sel, field);
      } else if (MatchesEnd(tail, "_Builder:")) {
        return AddBuilderSetterMethod(cls, sel, field);
      }
    }
  }
  return NO;
}

static BOOL ResolveClearAccessor(
    Class cls, CGPDescriptor *descriptor, SEL sel, const char *selName) {
  IOSObjectArray *fields = descriptor->fields_;
  ComGoogleProtobufDescriptors_FieldDescriptor **fieldsBuf = fields->buffer_;
  NSUInteger count = fields->size_;
  for (NSUInteger i = 0; i < count; ++i) {
    ComGoogleProtobufDescriptors_FieldDescriptor *field = fieldsBuf[i];
    if (MatchesEnd(selName, field->data_->javaName)) {
      return AddClearMethod(cls, sel, field);
    }
  }
  return NO;
}

static BOOL ResolveAddAccessor(Class cls, CGPDescriptor *descriptor, SEL sel, const char *selName) {
  IOSObjectArray *fields = descriptor->fields_;
  ComGoogleProtobufDescriptors_FieldDescriptor **fieldsBuf = fields->buffer_;
  NSUInteger count = fields->size_;
  for (NSUInteger i = 0; i < count; ++i) {
    ComGoogleProtobufDescriptors_FieldDescriptor *field = fieldsBuf[i];
    const char *tail = selName;
    if (MatchesName(&tail, field) && Matches(&tail, "With", 4) && MatchesKeyword(&tail, field)) {
      if (MatchesEnd(tail, ":")) {
        return AddAdderMethod(cls, sel, field);
      } else if (MatchesEnd(tail, "_Builder:")) {
        return AddBuilderAdderMethod(cls, sel, field);
      }
    }
  }
  return NO;
}

static BOOL ResolveAddAllAccessor(
    Class cls, CGPDescriptor *descriptor, SEL sel, const char *selName) {
  IOSObjectArray *fields = descriptor->fields_;
  ComGoogleProtobufDescriptors_FieldDescriptor **fieldsBuf = fields->buffer_;
  NSUInteger count = fields->size_;
  for (NSUInteger i = 0; i < count; ++i) {
    ComGoogleProtobufDescriptors_FieldDescriptor *field = fieldsBuf[i];
    const char *tail = selName;
    if (MatchesName(&tail, field) && MatchesEnd(tail, "WithJavaLangIterable:")) {
      return AddAddAllMethod(cls, sel, field);
    }
  }
  return NO;
}

static BOOL ResolveContainsAccessor(
    Class cls, CGPDescriptor *descriptor, SEL sel, const char *selName) {
  IOSObjectArray *fields = descriptor->fields_;
  CGPFieldDescriptor **fieldsBuf = fields->buffer_;
  NSUInteger count = fields->size_;
  for (NSUInteger i = 0; i < count; ++i) {
    CGPFieldDescriptor *field = fieldsBuf[i];
    const char *tail = selName;
    if (CGPFieldIsMap(field) && MatchesName(&tail, field) && Matches(&tail, "With", 4)
        && MatchesKeyword(&tail, CGPFieldMapKey(field)) && MatchesEnd(tail, ":")) {
      return AddContainsMethod(cls, sel, field);
    }
  }
  return NO;
}

static BOOL ResolvePutAccessor(Class cls, CGPDescriptor *descriptor, SEL sel, const char *selName) {
  IOSObjectArray *fields = descriptor->fields_;
  CGPFieldDescriptor **fieldsBuf = fields->buffer_;
  NSUInteger count = fields->size_;
  for (NSUInteger i = 0; i < count; ++i) {
    CGPFieldDescriptor *field = fieldsBuf[i];
    const char *tail = selName;
    if (CGPFieldIsMap(field) && MatchesName(&tail, field) && Matches(&tail, "With", 4)
        && MatchesKeyword(&tail, CGPFieldMapKey(field)) && Matches(&tail, ":with", 5)
        && MatchesKeyword(&tail, CGPFieldMapValue(field)) && MatchesEnd(tail, ":")) {
      return AddPutMethod(cls, sel, field);
    }
  }
  return NO;
}

static BOOL ResolveRemoveAccessor(
    Class cls, CGPDescriptor *descriptor, SEL sel, const char *selName) {
  IOSObjectArray *fields = descriptor->fields_;
  CGPFieldDescriptor **fieldsBuf = fields->buffer_;
  NSUInteger count = fields->size_;
  for (NSUInteger i = 0; i < count; ++i) {
    CGPFieldDescriptor *field = fieldsBuf[i];
    const char *tail = selName;
    if (CGPFieldIsMap(field) && MatchesName(&tail, field) && Matches(&tail, "With", 4)
        && MatchesKeyword(&tail, CGPFieldMapKey(field)) && MatchesEnd(tail, ":")) {
      return AddMapRemoveMethod(cls, sel, field);
    }
    tail = selName;
    if (CGPFieldIsRepeated(field) && CGPFieldTypeIsMessage(field) && MatchesName(&tail, field)
        && MatchesEnd(tail, "WithInt:")) {
      return AddRepeatedMessageRemoveMethod(cls, sel, field);
    }
  }
  return NO;
}

static BOOL ResolveAccessor(Class cls, CGPDescriptor *descriptor, SEL sel, BOOL isBuilder) {
  const char *selName = sel_getName(sel);
  if (Matches(&selName, "get", 3)) {
    return ResolveGetAccessor(cls, descriptor, sel, selName);
  } else if (Matches(&selName, "has", 3)) {
    return ResolveHasAccessor(cls, descriptor, sel, selName);
  } else if (Matches(&selName, "contains", 8)) {
    return ResolveContainsAccessor(cls, descriptor, sel, selName);
  }
  if (isBuilder) {
    if (Matches(&selName, "set", 3)) {
      return ResolveSetAccessor(cls, descriptor, sel, selName);
    } else if (Matches(&selName, "clear", 5)) {
      return ResolveClearAccessor(cls, descriptor, sel, selName);
    } else if (Matches(&selName, "add", 3)) {
      const char *addAllSelName = selName;
      if (Matches(&addAllSelName, "All", 3)
          && ResolveAddAllAccessor(cls, descriptor, sel, addAllSelName)) {
        return YES;
      }
      return ResolveAddAccessor(cls, descriptor, sel, selName);
    } else if (Matches(&selName, "put", 3)) {
      return ResolvePutAccessor(cls, descriptor, sel, selName);
    } else if (Matches(&selName, "remove", 6)) {
      return ResolveRemoveAccessor(cls, descriptor, sel, selName);
    }
  }
  return NO;
}


// *****************************************************************************
// ********** Reflective field accessors (using descriptors) ******************
// *****************************************************************************

static id GetSingularField(id msg, CGPFieldDescriptor *field) {
  Class msgCls = object_getClass(msg);
  bool isSet = GetHas(msg, GetHasLocator(msgCls, field));
  size_t offset = CGPFieldGetOffset(field, msgCls);

#define GET_FIELD_CASE(NAME) \
  { \
    TYPE_##NAME value = isSet ? *FIELD_PTR(TYPE_##NAME, msg, offset) \
        : field->data_->defaultValue.CGPValueField_##NAME; \
    return CGPToReflectionType##NAME(value, field); \
  }

  SWITCH_TYPES_WITH_ENUM(CGPFieldGetJavaType(field), GET_FIELD_CASE)

#undef GET_FIELD_CASE

  __builtin_unreachable();
}

static id GetField(id msg, CGPFieldDescriptor *field) {
  (void)nil_chk(field);
  if (CGPFieldIsMap(field)) {
    size_t offset = CGPFieldGetOffset(field, object_getClass(msg));
    CGPFieldJavaType keyType = CGPFieldGetJavaType(CGPFieldMapKey(field));
    CGPFieldJavaType valueType = CGPFieldGetJavaType(CGPFieldMapValue(field));
    return CGPMapFieldCopyList(MAP_FIELD_PTR(msg, offset), keyType, valueType);
  } else if (CGPFieldIsRepeated(field)) {
    size_t offset = CGPFieldGetOffset(field, object_getClass(msg));
    return CGPRepeatedFieldCopyList(REPEATED_FIELD_PTR(msg, offset), field);
  } else {
    return GetSingularField(msg, field);
  }
}

static jboolean HasField(id msg, CGPFieldDescriptor *descriptor) {
  if (CGPFieldIsRepeated(nil_chk(descriptor))) {
    @throw AUTORELEASE([[JavaLangUnsupportedOperationException alloc]
        initWithNSString:@"hasField() called on a repeated field."]);
  }
  CGPHasLocator hasLoc = GetHasLocator(object_getClass(msg), descriptor);
  return GetHas(msg, hasLoc);
}

static id<JavaUtilMap> GetAllFields(id msg) {
  id<JavaUtilMap> result = [[JavaUtilHashMap alloc] init];
  Class msgCls = object_getClass(msg);
  CGPDescriptor *descriptor = [msgCls getDescriptor];
  NSUInteger fieldCount = descriptor->fields_->size_;
  CGPFieldDescriptor **fieldsBuf = descriptor->fields_->buffer_;
  for (NSUInteger i = 0; i < fieldCount; i++) {
    CGPFieldDescriptor *field = fieldsBuf[i];
    if (CGPFieldIsMap(field)) {
      CGPFieldJavaType keyType = CGPFieldGetJavaType(CGPFieldMapKey(field));
      CGPFieldJavaType valueType = CGPFieldGetJavaType(CGPFieldMapValue(field));
      CGPMapField *mapField = MAP_FIELD_PTR(msg, CGPFieldGetOffset(field, msgCls));
      if (!CGPMapFieldIsEmpty(mapField)) {
        [result putWithId:field withId:CGPMapFieldCopyList(mapField, keyType, valueType)];
      }
    } else if (CGPFieldIsRepeated(field)) {
      CGPRepeatedField *repeatedField = REPEATED_FIELD_PTR(msg, CGPFieldGetOffset(field, msgCls));
      if (CGPRepeatedFieldSize(repeatedField) > 0) {
        [result putWithId:field withId:CGPRepeatedFieldCopyList(repeatedField, field)];
      }
    } else {
      if (GetHas(msg, GetHasLocator(msgCls, field))) {
        [result putWithId:field withId:GetSingularField(msg, field)];
      }
    }
  }
  return AUTORELEASE(result);
}

static void CheckIsRepeated(CGPFieldDescriptor *descriptor) {
  if (!CGPFieldIsRepeated(descriptor)) {
    @throw AUTORELEASE([[JavaLangUnsupportedOperationException alloc] initWithNSString:
        @"Expected a repeated field."]);
  }
}

static jint GetRepeatedFieldCount(id msg, CGPFieldDescriptor *descriptor) {
  CheckIsRepeated(nil_chk(descriptor));
  size_t offset = CGPFieldGetOffset(descriptor, object_getClass(msg));
  if (CGPFieldIsMap(descriptor)) {
    return CGPMapFieldListSize(MAP_FIELD_PTR(msg, offset));
  } else {
    return CGPRepeatedFieldSize(REPEATED_FIELD_PTR(msg, offset));
  }
}

static id GetRepeatedField(id msg, CGPFieldDescriptor *descriptor, jint index) {
  CheckIsRepeated(nil_chk(descriptor));
  size_t offset = CGPFieldGetOffset(descriptor, object_getClass(msg));
  if (CGPFieldIsMap(descriptor)) {
    return CGPMapFieldGetAtIndex(MAP_FIELD_PTR(msg, offset), index, descriptor);
  } else {
    return CGPRepeatedFieldGet(REPEATED_FIELD_PTR(msg, offset), index, descriptor);
  }
}

static void ReleaseAllFields(id self, Class cls, CGPDescriptor *descriptor) {
  CGPFieldDescriptor **fields = descriptor->fields_->buffer_;
  NSUInteger count = descriptor->fields_->size_;
  for (NSUInteger i = 0; i < count; i++) {
    CGPFieldDescriptor *field = fields[i];
    uintptr_t ptr = ((uintptr_t)self + CGPFieldGetOffset(field, cls));
    CGPFieldJavaType javaType = CGPFieldGetJavaType(field);
    if (CGPFieldIsMap(field)) {
      CGPMapFieldClear(
          (CGPMapField *)ptr, CGPFieldGetJavaType(CGPFieldMapKey(field)),
          CGPFieldGetJavaType(CGPFieldMapValue(field)));
    } else if (CGPFieldIsRepeated(field)) {
      CGPRepeatedFieldClear((CGPRepeatedField *)ptr, javaType);
    } else if (CGPIsRetainedType(javaType) && GetHas(self, GetHasLocator(cls, field))) {
      AUTORELEASE(*(id *)ptr);
    }
  }
}

static void CopyAllFields(
    id orig, Class origCls, id copy, Class copyCls, CGPDescriptor *descriptor) {
  memcpy((uint8_t *)copy + class_getInstanceSize(copyCls),
      (uint8_t *)orig + class_getInstanceSize(origCls), descriptor->storageSize_);
  // Retain object types.
  CGPFieldDescriptor **fields = descriptor->fields_->buffer_;
  NSUInteger count = descriptor->fields_->size_;
  for (NSUInteger i = 0; i < count; i++) {
    CGPFieldDescriptor *field = fields[i];
    uintptr_t ptr = ((uintptr_t)copy + CGPFieldGetOffset(field, copyCls));
    CGPFieldJavaType javaType = CGPFieldGetJavaType(field);
    if (CGPFieldIsMap(field)) {
      CGPMapFieldCopyData(
          (CGPMapField *)ptr, CGPFieldGetJavaType(CGPFieldMapKey(field)),
          CGPFieldGetJavaType(CGPFieldMapValue(field)));
    } else if (CGPFieldIsRepeated(field)) {
      CGPRepeatedFieldCopyData((CGPRepeatedField *)ptr, javaType);
    } else if (CGPIsRetainedType(javaType) && GetHas(copy, GetHasLocator(copyCls, field))) {
      RETAIN_(*(id *)ptr);
    }
  }
}

static void CopyMessage(
    id copy, CGPExtensionMap *copyExtensionMap, id orig, CGPExtensionMap *origExtensionMap,
    CGPDescriptor *descriptor) {
  CopyAllFields(orig, object_getClass(orig), copy, object_getClass(copy), descriptor);
  if (copyExtensionMap != NULL) {
    *copyExtensionMap = *origExtensionMap;
  }
}

ComGoogleProtobufGeneratedMessage *CGPNewMessage(
    ComGoogleProtobufDescriptors_Descriptor *descriptor) {
  ComGoogleProtobufGeneratedMessage *msg =
      NSAllocateObject(descriptor->messageClass_, descriptor->storageSize_, nil);
  msg->memoizedSize_ = -1;
  return msg;
}

ComGoogleProtobufGeneratedMessage_Builder *CGPNewBuilder(
    ComGoogleProtobufDescriptors_Descriptor *descriptor) {
  return NSAllocateObject(descriptor->builderClass_, descriptor->storageSize_, nil);
}


// *****************************************************************************
// ********** Merging from another message *************************************
// *****************************************************************************

static ComGoogleProtobufGeneratedMessage *NewMergedMessageField(
    id msg, id other, CGPDescriptor *descriptor) {
  id newMsg = CGPNewMessage(descriptor);
  CGPExtensionMap *newExtensionMap = MessageExtensionMap(newMsg, descriptor);
  CopyMessage(newMsg, newExtensionMap, msg, MessageExtensionMap(msg, descriptor), descriptor);
  MergeFromMessage(
      newMsg, newExtensionMap, other, MessageExtensionMap(other, descriptor), descriptor);
  return newMsg;
}

static void MergeFieldsFromMessage(id msg, id other, CGPDescriptor *descriptor) {
  Class msgCls = object_getClass(msg);
  Class otherCls = object_getClass(other);
  CGPFieldDescriptor **fields = descriptor->fields_->buffer_;
  NSUInteger count = descriptor->fields_->size_;
  for (NSUInteger i = 0; i < count; i++) {
    CGPFieldDescriptor *field = fields[i];
    CGPFieldJavaType type = CGPFieldGetJavaType(field);
    size_t msgOffset = CGPFieldGetOffset(field, msgCls);
    size_t otherOffset = CGPFieldGetOffset(field, otherCls);
    if (CGPFieldIsMap(field)) {
      CGPMapFieldAppendOther(
          MAP_FIELD_PTR(msg, msgOffset), MAP_FIELD_PTR(other, otherOffset),
          CGPFieldGetJavaType(CGPFieldMapKey(field)), CGPFieldGetJavaType(CGPFieldMapValue(field)));
    } else if (CGPFieldIsRepeated(field)) {
      CGPRepeatedField *repeatedField = REPEATED_FIELD_PTR(msg, msgOffset);
      CGPRepeatedField *otherRepeatedField = REPEATED_FIELD_PTR(other, otherOffset);
      if (otherRepeatedField->data != NULL) {
        CGPRepeatedFieldAppendOther(repeatedField, otherRepeatedField, type);
      }
    } else {
      CGPHasLocator otherHasLoc = GetHasLocator(otherCls, field);
      if (!GetHas(other, otherHasLoc)) {
        continue;
      }
      CGPHasLocator hasLoc = GetHasLocator(msgCls, field);
      uintptr_t fieldPtr = (uintptr_t)msg + msgOffset;
      uintptr_t otherFieldPtr = (uintptr_t)other + otherOffset;
      if (CGPJavaTypeIsMessage(type) && GetHas(msg, hasLoc)) {
        id *msgPtr = (id *)fieldPtr;
        AUTORELEASE(*msgPtr);
        *msgPtr = NewMergedMessageField(*msgPtr, *(id *)otherFieldPtr, field->valueType_);
        continue;
      }
      ClearPreviousOneof(msg, hasLoc, fieldPtr);

#define MERGE_FIELD_CASE(NAME) \
      { \
        TYPE_ASSIGN_##NAME(*(TYPE_##NAME *)fieldPtr, *(TYPE_##NAME *)otherFieldPtr); \
        break; \
      }

      SWITCH_TYPES_WITH_ENUM(type, MERGE_FIELD_CASE)

#undef MERGE_FIELD_CASE

      SetHas(msg, hasLoc);
    }
  }
}

static void MergeExtensions(CGPExtensionMap *msgExtensionMap, CGPExtensionMap *otherExtensionMap) {
  if (msgExtensionMap->empty()) {
    *msgExtensionMap = *otherExtensionMap;
    return;
  }
  for (CGPExtensionMap::iterator it = otherExtensionMap->begin(); it != otherExtensionMap->end();
       it++) {
    CGPFieldDescriptor *field = it->first;
    CGPExtensionValue &extValue = (*msgExtensionMap)[field];
    id value = extValue.get();
    id otherValue = it->second.get();
    if (CGPFieldIsRepeated(field)) {
      id<JavaUtilList> list = value;
      if (list == nil) {
        list = [[JavaUtilArrayList alloc] init];
        extValue.set_retained(list);
      }
      [list addAllWithJavaUtilCollection:otherValue];
    } else {
      if (CGPFieldTypeIsMessage(field) && value != nil) {
        extValue.set_retained(NewMergedMessageField(value, otherValue, field->valueType_));
      } else {
        extValue.set(otherValue);
      }
    }
  }
}

static void MergeFromMessage(
    id msg, CGPExtensionMap *msgExtensionMap, id other, CGPExtensionMap *otherExtensionMap,
    CGPDescriptor *descriptor) {
  MergeFieldsFromMessage(msg, other, descriptor);
  if (msgExtensionMap != NULL && otherExtensionMap != NULL) {
    MergeExtensions(msgExtensionMap, otherExtensionMap);
  }
}

static id DynamicMergeFromMessage(id builder, SEL _cmd, id other) {
  CGPDescriptor *descriptor = [object_getClass(builder) getDescriptor];
  MergeFromMessage(
      builder, BuilderExtensionMap(builder, descriptor), other,
      MessageExtensionMap(other, descriptor), descriptor);
  return builder;
}

ComGoogleProtobufGeneratedMessage_Builder *CGPBuilderFromPrototype(
    CGPDescriptor *descriptor, ComGoogleProtobufGeneratedMessage *prototype) {
  ComGoogleProtobufGeneratedMessage_Builder *builder = CGPNewBuilder(descriptor);
  CopyMessage(builder, BuilderExtensionMap(builder, descriptor),
              prototype, MessageExtensionMap(prototype, descriptor), descriptor);
  return AUTORELEASE(builder);
}


// *****************************************************************************
// ********** Deserializing ****************************************************
// *****************************************************************************

static inline BOOL ReadEnumValueDescriptor(
    CGPCodedInputStream *input, CGPEnumDescriptor *enumType, id *valueDescriptor) {
  jint value;
  if (!CGPReadEnum(input, &value)) return NO;
  *valueDescriptor = CGPEnumValueDescriptorFromInt(enumType, value);
  return YES;
}

static BOOL ReadEnumJavaValue(
    CGPCodedInputStream *input, CGPEnumDescriptor *enumType, id *javaValue) {
  CGPEnumValueDescriptor *valueDescriptor;
  if (!ReadEnumValueDescriptor(input, enumType, &valueDescriptor)) return NO;
  *javaValue = valueDescriptor == nil ? nil : valueDescriptor->enum_;
  return YES;
}

static inline BOOL MergeGroupFieldFromStream(
    id msg, CGPFieldDescriptor *field, CGPCodedInputStream *input,
    CGPExtensionRegistryLite *registry) {
  CGPDescriptor *type = field->valueType_;
  if (!MergeFromStream(msg, type, input, registry, MessageExtensionMap(msg, type))) return NO;
  if (!input->LastTagWas(CGPWireFormatMakeTag(CGPFieldGetNumber(field), CGPWireFormatEndGroup)))
    return NO;
  return YES;
}

static inline BOOL MergeMessageFieldFromStream(
    id msg, CGPFieldDescriptor *field, CGPCodedInputStream *input,
    CGPExtensionRegistryLite *registry) {
  int length;
  if (!CGPReadInt32(input, &length)) return NO;
  CGPCodedInputStream::Limit limit = input->PushLimit(length);
  CGPDescriptor *type = field->valueType_;
  if (!MergeFromStream(msg, type, input, registry, MessageExtensionMap(msg, type))) return NO;
  if (!input->ConsumedEntireMessage()) return NO;
  input->PopLimit(limit);
  return YES;
}

// Reads a retained value if it is a retainable type.
static BOOL ReadMapEntryField(
    CGPCodedInputStream *stream, CGPFieldDescriptor *field, uint32_t tag,
    CGPExtensionRegistryLite *registry, CGPValue *value) {
  CGPFieldType type = CGPFieldGetType(field);
  CGPWireFormat wireType = CGPWireFormatGetTagWireType(tag);
  if (wireType != CGPWireFormatForType(type, false)) {
    return NO;
  }
  BOOL isGroup = NO;
  switch (type) {
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_INT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_UINT32:
      return CGPReadInt32(stream, &value->valueInt);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SINT32:
      return CGPReadSint32(stream, &value->valueInt);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED32:
      return CGPReadFixed32(stream, &value->valueInt);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_INT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_UINT64:
      return CGPReadInt64(stream, &value->valueLong);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SINT64:
      return CGPReadSint64(stream, &value->valueLong);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED64:
      return CGPReadFixed64(stream, &value->valueLong);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BOOL:
      return CGPReadBool(stream, &value->valueBool);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FLOAT:
      return CGPReadFloat(stream, &value->valueFloat);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_DOUBLE:
      return CGPReadDouble(stream, &value->valueDouble);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_ENUM:
      return ReadEnumJavaValue(stream, field->valueType_, &value->valueId)
          && value->valueId != nil;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BYTES:
      return stream->ReadRetainedByteString(&value->valueId);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_STRING:
      return stream->ReadRetainedNSString(&value->valueId);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_GROUP:
      isGroup = YES;
      FALLTHROUGH_INTENDED;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_MESSAGE: {
      ComGoogleProtobufGeneratedMessage *newMsg = CGPNewMessage(field->valueType_);
      if (!(isGroup ?
          MergeGroupFieldFromStream(newMsg, field, stream, registry) :
          MergeMessageFieldFromStream(newMsg, field, stream, registry))) {
        RELEASE_(newMsg);
        return NO;
      }
      value->valueId = newMsg;
      return YES;
    }
  }
  __builtin_unreachable();
}

static BOOL MergeMapEntryFromStream(
    CGPMapField *field, CGPCodedInputStream *stream, CGPDescriptor *entry,
    CGPExtensionRegistryLite *registry) {
  int length;
  if (!CGPReadInt32(stream, &length)) return NO;
  CGPCodedInputStream::Limit limit = stream->PushLimit(length);
  CGPFieldDescriptor *keyField = entry->fields_->buffer_[0];
  CGPFieldDescriptor *valueField = entry->fields_->buffer_[1];
  BOOL hasKey = NO;
  BOOL hasValue = NO;
  CGPValue key;
  CGPValue value;
  while (YES) {
    uint32_t tag = stream->ReadTag();
    if (tag == 0) break;
    switch (CGPWireFormatGetTagFieldNumber(tag)) {
      case 1:
        if (hasKey && CGPIsRetainedType(CGPFieldGetJavaType(keyField))) {
          RELEASE_(key.valueId);
        }
        ReadMapEntryField(stream, keyField, tag, registry, &key);
        hasKey = YES;
        break;
      case 2:
        if (hasValue && CGPIsRetainedType(CGPFieldGetJavaType(valueField))) {
          RELEASE_(value.valueId);
        }
        ReadMapEntryField(stream, valueField, tag, registry, &value);
        hasValue = YES;
        break;
      default:
        if (!CGPWireFormatSkipField(stream, tag)) return NO;
        break;
    }
  }
  if (!stream->ConsumedEntireMessage()) return NO;
  stream->PopLimit(limit);
  CGPMapFieldPut(
      field, key, CGPFieldGetJavaType(keyField), value, CGPFieldGetJavaType(valueField),
      /* retainedKeyAndValue */ true);
  return YES;
}

static void AddExtensionWithReflectionType(
    CGPExtensionMap *extensionMap, CGPFieldDescriptor *field, id retainedValue) {
  CGPExtensionValue &extValue = (*extensionMap)[field];
  id<JavaUtilList> list = extValue.get();
  if (list == nil) {
    list = [[JavaUtilArrayList alloc] init];
    extValue.set_retained(list);
  }
  [list addWithId:retainedValue];
}

static BOOL MergeExtensionValueFromStream(
    CGPCodedInputStream *stream, CGPFieldDescriptor *field, id existingValue,
    CGPExtensionRegistryLite *registry, id *result, BOOL *isRetained) {
  BOOL isGroup = NO;
  switch (CGPFieldGetType(field)) {
#define READ_EXTENSION_CASE(ENUM_NAME, WIRE_NAME, JAVA_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_##ENUM_NAME: \
      { \
        TYPE_##JAVA_NAME value; \
        if (!CGPRead##WIRE_NAME(stream, &value)) return NO; \
        *result = CGPToReflectionType##JAVA_NAME(value, field); \
        *isRetained = NO; \
        return YES; \
      }
    READ_EXTENSION_CASE(INT32, Int32, Int)
    READ_EXTENSION_CASE(UINT32, Int32, Int)
    READ_EXTENSION_CASE(SINT32, Sint32, Int)
    READ_EXTENSION_CASE(FIXED32, Fixed32, Int)
    READ_EXTENSION_CASE(SFIXED32, Fixed32, Int)
    READ_EXTENSION_CASE(INT64, Int64, Long)
    READ_EXTENSION_CASE(UINT64, Int64, Long)
    READ_EXTENSION_CASE(SINT64, Sint64, Long)
    READ_EXTENSION_CASE(FIXED64, Fixed64, Long)
    READ_EXTENSION_CASE(SFIXED64, Fixed64, Long)
    READ_EXTENSION_CASE(BOOL, Bool, Bool)
    READ_EXTENSION_CASE(FLOAT, Float, Float)
    READ_EXTENSION_CASE(DOUBLE, Double, Double)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_ENUM:
      if (!ReadEnumValueDescriptor(stream, field->valueType_, result)) return NO;
      *isRetained = NO;
      return YES;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BYTES:
      if (!stream->ReadRetainedByteString(result)) return NO;
      *isRetained = YES;
      return YES;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_STRING:
      if (!stream->ReadRetainedNSString(result)) return NO;
      *isRetained = YES;
      return YES;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_GROUP:
      isGroup = YES;
      FALLTHROUGH_INTENDED;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_MESSAGE:
      {
        CGPDescriptor *fieldType = field->valueType_;
        ComGoogleProtobufGeneratedMessage *msgField = CGPNewMessage(fieldType);
        if (existingValue != nil) {
          CopyMessage(msgField, MessageExtensionMap(msgField, fieldType),
                      existingValue, MessageExtensionMap(existingValue, fieldType), fieldType);
        }
        if (!(isGroup ?
              MergeGroupFieldFromStream(msgField, field, stream, registry) :
              MergeMessageFieldFromStream(msgField, field, stream, registry))) {
          AUTORELEASE(msgField);
          return NO;
        }
        *result = msgField;
        *isRetained = YES;
        return YES;
      }
  }
#undef READ_EXTENSION_CASE
  __builtin_unreachable();
}

static BOOL MergeExtensionFromStream(
    CGPCodedInputStream *stream, CGPFieldDescriptor *field, CGPExtensionRegistryLite *registry,
    CGPExtensionMap *extensionMap) {
  id value;
  BOOL isRetained;
  if (CGPFieldIsRepeated(field)) {
    if (CGPFieldIsPacked(field)) {
      int length;
      if (!CGPReadInt32(stream, &length)) return NO;
      CGPCodedInputStream::Limit limit = stream->PushLimit(length);
      while (stream->BytesUntilLimit() > 0) {
        if (!MergeExtensionValueFromStream(stream, field, nil, registry, &value, &isRetained))
          return NO;
        if (value != nil) {
          AddExtensionWithReflectionType(extensionMap, field, value);
          if (isRetained) RELEASE_(value);
        }
      }
      stream->PopLimit(limit);
    } else {
      if (!MergeExtensionValueFromStream(stream, field, nil, registry, &value, &isRetained))
        return NO;
      if (value != nil) {
        AddExtensionWithReflectionType(extensionMap, field, value);
        if (isRetained) RELEASE_(value);
      }
    }
  } else {
    id existingValue = nil;
    // For message types we need to keep the fields from the existing message.
    if (CGPFieldTypeIsMessage(field)) {
      CGPExtensionMap::iterator it = extensionMap->find(field);
      if (it != extensionMap->end()) {
        existingValue = it->second.get();
      }
    }
    if (!MergeExtensionValueFromStream(stream, field, existingValue, registry, &value, &isRetained))
      return NO;
    if (value != nil) {
      (*extensionMap)[field].set_retained(isRetained ? value : RETAIN_(value));
    }
  }
  return YES;
}

static BOOL MergeMessageSetExtensionFromStream(
    CGPCodedInputStream *stream, CGPDescriptor *descriptor, CGPExtensionRegistryLite *registry,
    CGPExtensionMap *extensionMap) {
  uint32_t typeId = 0;
  CGPByteString *rawBytes = nil;
  CGPFieldDescriptor *extension = nil;

  while (true) {
    uint32_t tag = stream->ReadTag();
    if (tag == 0) {
      return NO;
    }

    switch (tag) {
      case CGPWireFormatMessageSetTypeIdTag:
        if (!stream->ReadVarint32(&typeId)) return NO;
        extension = CGPExtensionRegistryFind(registry, descriptor, typeId);
        if (rawBytes != nil && extension != nil) {
          CGPCodedInputStream newInput(rawBytes->buffer_, rawBytes->size_);
          if (!MergeExtensionFromStream(&newInput, extension, registry, extensionMap)) return NO;
        }
        rawBytes = nil;
        break;

      case CGPWireFormatMessageSetMessageTag:
        if (typeId == 0) {
          // We haven't seen a type id yet. Read the data into rawBytes.
          CGPByteString *tempBytes;
          if (!stream->ReadRetainedByteString(&tempBytes)) return NO;
          rawBytes = AUTORELEASE(CGPNewByteString(CGPGetBytesSize(tempBytes)));
          CGPCodedOutputStream tempOutput(rawBytes->buffer_, rawBytes->size_);
          CGPWriteBytes(tempBytes, &tempOutput);
          RELEASE_(tempBytes);
        } else {
          // We already saw the type id so we can parse directly.
          if (extension != nil) {
            if (!MergeExtensionFromStream(stream, extension, registry, extensionMap)) return NO;
          } else {
            CGPWireFormatSkipField(
                stream, CGPWireFormatMakeTag(typeId, CGPWireFormatLengthDelimited));
          }
        }
        break;

      case CGPWireFormatMessageSetItemEndTag:
        return YES;

      default:
        if (!CGPWireFormatSkipField(stream, tag)) return NO;
    }
  }
}

static BOOL ParseUnknownField(
    CGPCodedInputStream *stream, CGPDescriptor *descriptor, CGPExtensionRegistryLite *registry,
    CGPExtensionMap *extensionMap, uint32_t tag) {
  if (registry != nil && extensionMap != NULL) {
    uint32_t fieldNumber = CGPWireFormatGetTagFieldNumber(tag);
    CGPFieldDescriptor *field = CGPExtensionRegistryFind(registry, descriptor, fieldNumber);
    if (!field && [registry isKindOfClass:[ComGoogleProtobufExtensionRegistry class]]) {
      ComGoogleProtobufExtensionRegistry_ExtensionInfo *extension =
          [(ComGoogleProtobufExtensionRegistry *)registry
              findExtensionByNumberWithComGoogleProtobufDescriptors_Descriptor:descriptor
                                                                       withInt:fieldNumber];
      if (extension) {
        field = extension->descriptor_;
      }
    }
    if (field != nil && field->tag_ == tag) {
      return MergeExtensionFromStream(stream, field, registry, extensionMap);
    }
    if (CGPIsMessageSetWireFormat(descriptor) && tag == CGPWireFormatMessageSetItemTag) {
      return MergeMessageSetExtensionFromStream(stream, descriptor, registry, extensionMap);
    }
  }

  return CGPWireFormatSkipField(stream, tag);
}

static BOOL MergeFieldFromStream(
    id msg, CGPFieldDescriptor *field, CGPCodedInputStream *stream,
    CGPExtensionRegistryLite *registry) {
  Class msgCls = object_getClass(msg);
  uintptr_t fieldPtr = (uintptr_t)msg + CGPFieldGetOffset(field, msgCls);
  BOOL repeated = CGPFieldIsRepeated(field);
  BOOL isGroup = NO;
  CGPHasLocator hasLoc;
  if (!repeated) {
    hasLoc = GetHasLocator(msgCls, field);
    ClearPreviousOneof(msg, hasLoc, fieldPtr);
  }
  switch (CGPFieldGetType(field)) {
#define MERGE_FIELD_CASE(NAME, ENUM_NAME, JAVA_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_##ENUM_NAME: \
      if (repeated) { \
        CGPRepeatedField *repeatedField = (CGPRepeatedField *)fieldPtr; \
        TYPE_##JAVA_NAME value; \
        if (CGPFieldIsPacked(field)) { \
          int length; \
          if (!CGPReadInt32(stream, &length)) return NO; \
          CGPRepeatedFieldReserveAdditionalCapacity( \
              repeatedField, length, sizeof(TYPE_##JAVA_NAME)); \
          CGPCodedInputStream::Limit limit = stream->PushLimit(length); \
          while (stream->BytesUntilLimit() > 0) { \
            if (!CGPRead##NAME(stream, &value)) return NO; \
            CGPRepeatedFieldAdd##JAVA_NAME(repeatedField, value); \
          } \
          stream->PopLimit(limit); \
        } else { \
          if (!CGPRead##NAME(stream, &value)) return NO; \
          CGPRepeatedFieldAdd##JAVA_NAME(repeatedField, value); \
        } \
      } else { \
        if (!CGPRead##NAME(stream, (TYPE_##JAVA_NAME *)fieldPtr)) return NO; \
        SetHas(msg, hasLoc); \
      } \
      return YES;
    MERGE_FIELD_CASE(Int32, INT32, Int)
    MERGE_FIELD_CASE(Int32, UINT32, Int)
    MERGE_FIELD_CASE(Sint32, SINT32, Int)
    MERGE_FIELD_CASE(Fixed32, FIXED32, Int)
    MERGE_FIELD_CASE(Fixed32, SFIXED32, Int)
    MERGE_FIELD_CASE(Int64, INT64, Long)
    MERGE_FIELD_CASE(Int64, UINT64, Long)
    MERGE_FIELD_CASE(Sint64, SINT64, Long)
    MERGE_FIELD_CASE(Fixed64, FIXED64, Long)
    MERGE_FIELD_CASE(Fixed64, SFIXED64, Long)
    MERGE_FIELD_CASE(Bool, BOOL, Bool)
    MERGE_FIELD_CASE(Float, FLOAT, Float)
    MERGE_FIELD_CASE(Double, DOUBLE, Double)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_ENUM:
      {
        CGPEnumDescriptor *enumType = field->valueType_;
        id value;
        if (repeated) {
          CGPRepeatedField *repeatedField = (CGPRepeatedField *)fieldPtr;
          if (CGPFieldIsPacked(field)) {
            int length;
            if (!CGPReadInt32(stream, &length)) return NO;
            CGPRepeatedFieldReserveAdditionalCapacity(repeatedField, length, sizeof(id));
            CGPCodedInputStream::Limit limit = stream->PushLimit(length);
            while (stream->BytesUntilLimit() > 0) {
              if (!ReadEnumJavaValue(stream, enumType, &value)) return NO;
              if (value != nil) {
                CGPRepeatedFieldAddEnum(repeatedField, value);
              }
            } \
            stream->PopLimit(limit);
          } else {
            if (!ReadEnumJavaValue(stream, enumType, &value)) return NO;
            if (value != nil) {
              CGPRepeatedFieldAddEnum(repeatedField, value);
            }
          }
        } else {
          if (!ReadEnumJavaValue(stream, enumType, &value)) return NO;
          if (value == nil) return YES; // Skip setting has-bit.
          *(id *)fieldPtr = value;
          SetHas(msg, hasLoc);
        }
      }
      return YES;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BYTES:
      {
        CGPByteString *value;
        if (!stream->ReadRetainedByteString(&value)) return NO;
        if (repeated) {
          CGPRepeatedFieldAddRetainedId((CGPRepeatedField *)fieldPtr, value);
        } else {
          id *ptr = (id *)fieldPtr;
          AUTORELEASE(*ptr);
          *ptr = value;
          SetHas(msg, hasLoc);
        }
      }
      return YES;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_STRING:
      {
        NSString *value;
        if (!stream->ReadRetainedNSString(&value)) return NO;
        if (repeated) {
          CGPRepeatedFieldAddRetainedId((CGPRepeatedField *)fieldPtr, value);
        } else {
          id *ptr = (id *)fieldPtr;
          AUTORELEASE(*ptr);
          *ptr = value;
          SetHas(msg, hasLoc);
        }
      }
      return YES;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_GROUP:
      isGroup = YES;
      FALLTHROUGH_INTENDED;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_MESSAGE:
      {
        CGPDescriptor *fieldType = field->valueType_;
        if (CGPFieldIsMap(field)) {
          return MergeMapEntryFromStream((CGPMapField *)fieldPtr, stream, fieldType, registry);
        }
        ComGoogleProtobufGeneratedMessage *msgField = CGPNewMessage(fieldType);
        if (repeated) {
          CGPRepeatedFieldAddRetainedId((CGPRepeatedField *)fieldPtr, msgField);
        } else {
          id *ptr = (id *)fieldPtr;
          if (GetHas(msg, hasLoc)) {
            CopyMessage(msgField, MessageExtensionMap(msgField, fieldType),
                        *ptr, MessageExtensionMap(*ptr, fieldType), fieldType);
          }
          AUTORELEASE(*ptr);
          *ptr = msgField;
          SetHas(msg, hasLoc);
        }
        if (isGroup) {
          if (!MergeGroupFieldFromStream(msgField, field, stream, registry)) return NO;
        } else {
          if (!MergeMessageFieldFromStream(msgField, field, stream, registry)) return NO;
        }
      }
      return YES;
  }
#undef MERGE_FIELD_CASE
  __builtin_unreachable();
}

static BOOL MergeFromStream(
    id msg, CGPDescriptor *descriptor, CGPCodedInputStream *stream,
    CGPExtensionRegistryLite *registry, CGPExtensionMap *extensionMap) {
  NSUInteger index = 0;
  NSUInteger fieldsCount = descriptor->fields_->size_;
  CGPFieldDescriptor **fieldsBuf = descriptor->fields_->buffer_;
  while (YES) {
    BOOL merged = NO;
    uint32_t tag = stream->ReadTag();
    if (tag == 0) break;
    for (NSUInteger i = 0; i < fieldsCount; i++) {
      if (index >= fieldsCount) {
        index = 0;
      }
      CGPFieldDescriptor *field = fieldsBuf[index];
      if (tag == field->tag_) {
        if (!MergeFieldFromStream(msg, field, stream, registry)) return NO;
        merged = YES;
        break;
      } else {
        index++;
      }
    }
    if (!merged) {
      CGPWireFormat wireType = CGPWireFormatGetTagWireType(tag);
      if (wireType == CGPWireFormatEndGroup) {
        return YES;
      }
      if (!ParseUnknownField(stream, descriptor, registry, extensionMap, tag)) return NO;
    }
  }
  return YES;
}

static void InvalidPB() {
  @throw AUTORELEASE([[ComGoogleProtobufInvalidProtocolBufferException alloc] init]);
}

void CGPMergeFromRawData(id msg, CGPDescriptor *descriptor, const char *data, uint32_t length) {
  CGPCodedInputStream codedStream(data, length);
  BOOL success = MergeFromStream(msg, descriptor, &codedStream, nil, nil)
      && codedStream.ConsumedEntireMessage();
  if (!success) {
    InvalidPB();
  }
}

ComGoogleProtobufGeneratedMessage *CGPParseFromByteArray(
    CGPDescriptor *descriptor, IOSByteArray *bytes, CGPExtensionRegistryLite *registry) {
  ComGoogleProtobufGeneratedMessage *msg = AUTORELEASE(CGPNewMessage(descriptor));
  CGPCodedInputStream codedStream(bytes->buffer_, (int)bytes->size_);
  BOOL success =
      MergeFromStream(msg, descriptor, &codedStream, registry, MessageExtensionMap(msg, descriptor))
      && codedStream.ConsumedEntireMessage();
  if (!success) {
    InvalidPB();
  }
  return msg;
}

ComGoogleProtobufGeneratedMessage *CGPParseFromInputStream(
    CGPDescriptor *descriptor, JavaIoInputStream *input, CGPExtensionRegistryLite *registry) {
  ComGoogleProtobufGeneratedMessage *msg = AUTORELEASE(CGPNewMessage(descriptor));
  CGPCodedInputStream codedStream(input, INT_MAX);
  BOOL success =
      MergeFromStream(msg, descriptor, &codedStream, registry, MessageExtensionMap(msg, descriptor))
      && codedStream.ConsumedEntireMessage();
  if (!success) {
    InvalidPB();
  }
  return msg;
}

ComGoogleProtobufGeneratedMessage *CGPParseDelimitedFromInputStream(
    CGPDescriptor *descriptor, JavaIoInputStream *input, CGPExtensionRegistryLite *registry) {
  int firstByte = [input read];
  if (firstByte == -1) {
    return nil;
  }
  uint32_t length;
  if (!CGPCodedInputStream::ReadVarint32(firstByte, input, &length)) InvalidPB();
  ComGoogleProtobufGeneratedMessage *msg = AUTORELEASE(CGPNewMessage(descriptor));
  CGPCodedInputStream codedStream(input, length);
  BOOL success =
      MergeFromStream(msg, descriptor, &codedStream, registry, MessageExtensionMap(msg, descriptor))
      && codedStream.ConsumedEntireMessage();
  if (!success) {
    InvalidPB();
  }
  return msg;
}


// *****************************************************************************
// ********** Computing serialized size ****************************************
// *****************************************************************************

static int SerializedSizeForSingularExtensionValue(CGPFieldDescriptor *field, id value) {
  switch (CGPFieldGetType(field)) {
#define EXT_SIZE_VARIABLE_LENGTH_CASE(NAME, ENUM_NAME, JAVA_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_##ENUM_NAME: \
      return CGPGet##NAME##Size(CGPFromReflectionType##JAVA_NAME(value));
    EXT_SIZE_VARIABLE_LENGTH_CASE(Int32, INT32, Int)
    EXT_SIZE_VARIABLE_LENGTH_CASE(Uint32, UINT32, Int)
    EXT_SIZE_VARIABLE_LENGTH_CASE(Sint32, SINT32, Int)
    EXT_SIZE_VARIABLE_LENGTH_CASE(Int64, INT64, Long)
    EXT_SIZE_VARIABLE_LENGTH_CASE(Int64, UINT64, Long)
    EXT_SIZE_VARIABLE_LENGTH_CASE(Sint64, SINT64, Long)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FLOAT:
      return sizeof(uint32_t);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_DOUBLE:
      return sizeof(uint64_t);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BOOL:
      return 1;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_ENUM:
      return CGPGetEnumSize(((CGPEnumValueDescriptor *)value)->number_);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BYTES:
      return CGPGetBytesSize(value);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_STRING:
      return CGPGetStringSize(value);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_GROUP:
      return SerializedSizeForMessage(value, field->valueType_) + CGPGetTagSize(field->tag_);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_MESSAGE:
      {
        int msgSize = SerializedSizeForMessage(value, field->valueType_);
        return CGPGetInt32Size(msgSize) + msgSize;
      }
  }
#undef EXT_SIZE_VARIABLE_LENGTH_CASE
  __builtin_unreachable();
}

static int SerializedSizeForExtensionValue(CGPFieldDescriptor *field, id value) {
  BOOL repeated = CGPFieldIsRepeated(field);
  CGPFieldType type = CGPFieldGetType(field);
  int tagSize = CGPGetTagSize(field->tag_);
  if (repeated) {
    id<JavaUtilList> list = value;
    int listSize = [list size];
    int arraySize = 0;
    size_t typeSize = CGPTypeFixedSize(type);
    if (typeSize == 0) {
      for (id elem in list) {
        arraySize += SerializedSizeForSingularExtensionValue(field, elem);
      }
    } else {
      arraySize = (int)typeSize * listSize;
    }
    if (CGPFieldIsPacked(field)) {
      return tagSize + CGPGetInt32Size(arraySize) + arraySize;
    } else {
      return listSize * tagSize + arraySize;
    }
  } else {
    return tagSize + SerializedSizeForSingularExtensionValue(field, value);
  }
}

static int SerializedSizeForMessageSetExtension(CGPFieldDescriptor *field, id value) {
  int msgSize = SerializedSizeForMessage(value, field->valueType_);
  return CGPGetTagSize(CGPWireFormatMessageSetItemTag) * 2
      + CGPGetTagSize(CGPWireFormatMessageSetTypeIdTag) + CGPGetUint32Size(CGPFieldGetNumber(field))
      + CGPGetTagSize(CGPWireFormatMessageSetMessageTag) + CGPGetUint32Size(msgSize) + msgSize;
}

static int SerializedSizeForExtensions(CGPDescriptor *descriptor, CGPExtensionMap *extensionMap) {
  BOOL messageSetFormat = CGPIsMessageSetWireFormat(descriptor);
  int size = 0;
  for (CGPExtensionMap::iterator it = extensionMap->begin(); it != extensionMap->end(); it++) {
    if (messageSetFormat) {
      size += SerializedSizeForMessageSetExtension(it->first, it->second.get());
    } else {
      size += SerializedSizeForExtensionValue(it->first, it->second.get());
    }
  }
  return size;
}

static int SerializedSizeForMapEntryField(CGPFieldDescriptor *field, CGPValue value) {
  int tagSize = CGPGetTagSize(field->tag_);
  switch (CGPFieldGetType(field)) {
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_INT32:
      return tagSize + CGPGetInt32Size(value.valueInt);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_UINT32:
      return tagSize + CGPGetUint32Size(value.valueInt);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SINT32:
      return tagSize + CGPGetSint32Size(value.valueInt);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_INT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_UINT64:
      return tagSize + CGPGetInt64Size(value.valueLong);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SINT64:
      return tagSize + CGPGetSint64Size(value.valueLong);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FLOAT:
      return tagSize + sizeof(uint32_t);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_DOUBLE:
      return tagSize + sizeof(uint64_t);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BOOL:
      return tagSize + 1;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_ENUM:
      return tagSize + CGPGetEnumSize(CGPEnumGetIntValue(field->valueType_, value.valueId));
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BYTES:
      return tagSize + CGPGetBytesSize(value.valueId);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_STRING:
      return tagSize + CGPGetStringSize(value.valueId);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_GROUP:
      {
        int msgSize = SerializedSizeForMessage(value.valueId, field->valueType_);
        return tagSize * 2 + msgSize;
      }
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_MESSAGE:
      {
        int msgSize = SerializedSizeForMessage(value.valueId, field->valueType_);
        return tagSize + CGPGetInt32Size(msgSize) + msgSize;
      }
  }
  __builtin_unreachable();
}

static int SerializedSizeForMapField(id msg, CGPFieldDescriptor *field) {
  size_t offset = CGPFieldGetOffset(field, object_getClass(msg));
  CGPMapFieldData *data = MAP_FIELD_PTR(msg, offset)->data;
  if (data == NULL) {
    return 0;
  }
  int tagSize = CGPGetTagSize(field->tag_);
  int numEntries = 0;
  int entriesSize = 0;

  CGPFieldDescriptor *keyField = CGPFieldMapKey(field);
  CGPFieldDescriptor *valueField = CGPFieldMapValue(field);
  CGPMapFieldEnsureValidMap(data, CGPFieldGetJavaType(keyField), CGPFieldGetJavaType(valueField));
  CGPMapFieldEntry *entry = data->header.next;
  while (entry != &data->header) {
    int entrySize = SerializedSizeForMapEntryField(keyField, entry->key) +
        SerializedSizeForMapEntryField(valueField, entry->value);
    entriesSize += CGPGetInt32Size(entrySize) + entrySize;
    numEntries++;
    entry = entry->next;
  }

  return entriesSize + numEntries * tagSize;
}

static int SerializedSizeForRepeatedField(id msg, CGPFieldDescriptor *field) {
  Class msgCls = object_getClass(msg);
  int tagSize = CGPGetTagSize(field->tag_);
  size_t offset = CGPFieldGetOffset(field, msgCls);
  CGPRepeatedFieldData *data = REPEATED_FIELD_PTR(msg, offset)->data;
  if (data == NULL) {
    return 0;
  }
  uint32_t arrayLen = data->size;
  int arraySize = 0;

  switch (CGPFieldGetType(field)) {
#define REPEATED_FIELD_SIZE_VARIABLE_LENGTH_CASE(NAME, ENUM_NAME, JAVA_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_##ENUM_NAME: \
      { \
        TYPE_##JAVA_NAME *buffer = (TYPE_##JAVA_NAME *)data->buffer; \
        for (uint32_t i = 0; i < arrayLen; i++) { \
          arraySize += CGPGet##NAME##Size(buffer[i]); \
        } \
      } \
      break;
    REPEATED_FIELD_SIZE_VARIABLE_LENGTH_CASE(Int32, INT32, Int)
    REPEATED_FIELD_SIZE_VARIABLE_LENGTH_CASE(Uint32, UINT32, Int)
    REPEATED_FIELD_SIZE_VARIABLE_LENGTH_CASE(Sint32, SINT32, Int)
    REPEATED_FIELD_SIZE_VARIABLE_LENGTH_CASE(Int64, INT64, Long)
    REPEATED_FIELD_SIZE_VARIABLE_LENGTH_CASE(Int64, UINT64, Long)
    REPEATED_FIELD_SIZE_VARIABLE_LENGTH_CASE(Sint64, SINT64, Long)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FLOAT:
      arraySize = arrayLen * sizeof(uint32_t);
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_DOUBLE:
      arraySize = arrayLen * sizeof(uint64_t);
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BOOL:
      arraySize = arrayLen;
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_ENUM:
      {
        CGPEnumDescriptor *enumType = field->valueType_;
        id *buffer = (id *)data->buffer;
        for (uint32_t i = 0; i < arrayLen; i++) {
          arraySize += CGPGetEnumSize(CGPEnumGetIntValue(enumType, buffer[i]));
        }
      }
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BYTES:
      {
        id *buffer = (id *)data->buffer;
        for (uint32_t i = 0; i < arrayLen; i++) {
          arraySize += CGPGetBytesSize(buffer[i]);
        }
      }
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_STRING:
      {
        id *buffer = (id *)data->buffer;
        for (uint32_t i = 0; i < arrayLen; i++) {
          arraySize += CGPGetStringSize(buffer[i]);
        }
      }
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_GROUP:
      {
        id *buffer = (id *)data->buffer;
        for (uint32_t i = 0; i < arrayLen; i++) {
          arraySize += SerializedSizeForMessage(buffer[i], field->valueType_);
        }
        arraySize += arrayLen * tagSize;  // End group tags.
      }
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_MESSAGE:
      {
        id *buffer = (id *)data->buffer;
        for (uint32_t i = 0; i < arrayLen; i++) {
          int msgSize = SerializedSizeForMessage(buffer[i], field->valueType_);
          arraySize += CGPGetInt32Size(msgSize) + msgSize;
        }
      }
      break;
  }

  if (CGPFieldIsPacked(field)) {
    return arraySize + tagSize + CGPGetInt32Size(arraySize);
  } else {
    return arraySize + arrayLen * tagSize;
  }

#undef REPEATED_FIELD_SIZE_VARIABLE_LENGTH_CASE
}

static int SerializedSizeForSingularField(id msg, CGPFieldDescriptor *field) {
  Class msgCls = object_getClass(msg);
  if (!GetHas(msg, GetHasLocator(msgCls, field))) {
    return 0;
  }

  int tagSize = CGPGetTagSize(field->tag_);
  size_t offset = CGPFieldGetOffset(field, msgCls);

  switch (CGPFieldGetType(field)) {
#define SINGULAR_FIELD_SIZE_CASE(NAME, ENUM_NAME, JAVA_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_##ENUM_NAME: \
      return tagSize + CGPGet##NAME##Size(*FIELD_PTR(TYPE_##JAVA_NAME, msg, offset));
    SINGULAR_FIELD_SIZE_CASE(Int32, INT32, Int)
    SINGULAR_FIELD_SIZE_CASE(Uint32, UINT32, Int)
    SINGULAR_FIELD_SIZE_CASE(Sint32, SINT32, Int)
    SINGULAR_FIELD_SIZE_CASE(Int64, INT64, Long)
    SINGULAR_FIELD_SIZE_CASE(Int64, UINT64, Long)
    SINGULAR_FIELD_SIZE_CASE(Sint64, SINT64, Long)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FLOAT:
      return tagSize + sizeof(uint32_t);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_DOUBLE:
      return tagSize + sizeof(uint64_t);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BOOL:
      return tagSize + 1;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_ENUM:
      return tagSize + CGPGetEnumSize(CGPEnumGetIntValue(
          field->valueType_, *FIELD_PTR(id, msg, offset)));
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BYTES:
      return tagSize + CGPGetBytesSize(*FIELD_PTR(id, msg, offset));
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_STRING:
      return tagSize + CGPGetStringSize(*FIELD_PTR(id, msg, offset));
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_GROUP:
      {
        int msgSize = SerializedSizeForMessage(*FIELD_PTR(id, msg, offset), field->valueType_);
        return tagSize * 2 + msgSize;
      }
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_MESSAGE:
      {
        int msgSize = SerializedSizeForMessage(*FIELD_PTR(id, msg, offset), field->valueType_);
        return tagSize + CGPGetInt32Size(msgSize) + msgSize;
      }
  }
#undef SINGULAR_FIELD_SIZE_CASE
  __builtin_unreachable();
}

static int ComputeSerializedSizeForMessage(
    ComGoogleProtobufGeneratedMessage *msg, CGPDescriptor *descriptor) {
  int size = 0;
  NSUInteger fieldsCount = descriptor->fields_->size_;
  CGPFieldDescriptor **fieldsBuf = descriptor->fields_->buffer_;
  for (NSUInteger i = 0; i < fieldsCount; i++) {
    CGPFieldDescriptor *field = fieldsBuf[i];
    if (CGPFieldIsMap(field)) {
      size += SerializedSizeForMapField(msg, field);
    } else if (CGPFieldIsRepeated(field)) {
      size += SerializedSizeForRepeatedField(msg, field);
    } else {
      size += SerializedSizeForSingularField(msg, field);
    }
  }
  CGPExtensionMap *extensionMap = MessageExtensionMap(msg, descriptor);
  if (extensionMap != NULL) {
    size += SerializedSizeForExtensions(descriptor, extensionMap);
  }
  msg->memoizedSize_ = size;
  return size;
}

static inline int SerializedSizeForMessage(
    ComGoogleProtobufGeneratedMessage *msg, CGPDescriptor *descriptor) {
  // Assuming that reading and writing an int field is atomic.
  int size = msg->memoizedSize_;
  if (size != -1) {
    return size;
  }
  return ComputeSerializedSizeForMessage(msg, descriptor);
}


// *****************************************************************************
// ********** Serializing ******************************************************
// *****************************************************************************

static void WriteSingularExtensionValue(
    CGPFieldDescriptor *field, id value, CGPCodedOutputStream *output) {
  switch (CGPFieldGetType(field)) {
#define WRITE_SINGULAR_EXTENSION_CASE(NAME, ENUM_NAME, JAVA_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_##ENUM_NAME: \
      CGPWrite##NAME(CGPFromReflectionType##JAVA_NAME(value), output); \
      return;
    WRITE_SINGULAR_EXTENSION_CASE(Int32, INT32, Int)
    WRITE_SINGULAR_EXTENSION_CASE(Uint32, UINT32, Int)
    WRITE_SINGULAR_EXTENSION_CASE(Sint32, SINT32, Int)
    WRITE_SINGULAR_EXTENSION_CASE(Fixed32, FIXED32, Int)
    WRITE_SINGULAR_EXTENSION_CASE(Fixed32, SFIXED32, Int)
    WRITE_SINGULAR_EXTENSION_CASE(Int64, INT64, Long)
    WRITE_SINGULAR_EXTENSION_CASE(Int64, UINT64, Long)
    WRITE_SINGULAR_EXTENSION_CASE(Sint64, SINT64, Long)
    WRITE_SINGULAR_EXTENSION_CASE(Fixed64, FIXED64, Long)
    WRITE_SINGULAR_EXTENSION_CASE(Fixed64, SFIXED64, Long)
    WRITE_SINGULAR_EXTENSION_CASE(Bool, BOOL, Bool)
    WRITE_SINGULAR_EXTENSION_CASE(Float, FLOAT, Float)
    WRITE_SINGULAR_EXTENSION_CASE(Double, DOUBLE, Double)
    WRITE_SINGULAR_EXTENSION_CASE(Bytes, BYTES, Retainable)
    WRITE_SINGULAR_EXTENSION_CASE(String, STRING, Retainable)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_ENUM:
      CGPWriteEnum(((CGPEnumValueDescriptor *)value)->number_, output);
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_GROUP:
      WriteMessage(value, field->valueType_, output);
      output->WriteTag(CGPWireFormatMakeTag(CGPFieldGetNumber(field), CGPWireFormatEndGroup));
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_MESSAGE:
      CGPWriteInt32(SerializedSizeForMessage(value, field->valueType_), output);
      WriteMessage(value, field->valueType_, output);
      return;
  }
#undef WRITE_SINGULAR_EXTENSION_CASE
}

static void WriteExtension(CGPFieldDescriptor *field, id value, CGPCodedOutputStream *output) {
  BOOL repeated = CGPFieldIsRepeated(field);
  if (repeated) {
    id<JavaUtilList> list = value;
    int listSize = [list size];
    if (CGPFieldIsPacked(field)) {
      int arraySize = 0;
      size_t typeSize = CGPTypeFixedSize(CGPFieldGetType(field));
      if (typeSize == 0) {
        for (id elem in list) {
          arraySize += SerializedSizeForSingularExtensionValue(field, elem);
        }
      } else {
        arraySize = (int)typeSize * listSize;
      }
      output->WriteTag(field->tag_);
      CGPWriteInt32(arraySize, output);
      for (id elem in list) {
        WriteSingularExtensionValue(field, elem, output);
      }
    } else {
      for (id elem in list) {
        output->WriteTag(field->tag_);
        WriteSingularExtensionValue(field, elem, output);
      }
    }
  } else {
    output->WriteTag(field->tag_);
    WriteSingularExtensionValue(field, value, output);
  }
}

static void WriteMessageSetExtension(
    CGPFieldDescriptor *field, id value, CGPCodedOutputStream *output) {
  output->WriteTag(CGPWireFormatMessageSetItemTag);
  output->WriteTag(CGPWireFormatMessageSetTypeIdTag);
  CGPWriteUint32(CGPFieldGetNumber(field), output);
  output->WriteTag(CGPWireFormatMessageSetMessageTag);
  CGPWriteUint32(SerializedSizeForMessage(value, field->valueType_), output);
  WriteMessage(value, field->valueType_, output);
  output->WriteTag(CGPWireFormatMessageSetItemEndTag);
}

static void WriteSingularField(id msg, CGPFieldDescriptor *field, CGPCodedOutputStream *output) {
  Class msgCls = object_getClass(msg);
  if (!GetHas(msg, GetHasLocator(msgCls, field))) {
    return;
  }
  size_t offset = CGPFieldGetOffset(field, msgCls);
  output->WriteTag(field->tag_);

  switch (CGPFieldGetType(field)) {
#define WRITE_SINGULAR_FIELD_CASE(NAME, ENUM_NAME, JAVA_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_##ENUM_NAME: \
      CGPWrite##NAME(*FIELD_PTR(TYPE_##JAVA_NAME, msg, offset), output); \
      return;
      WRITE_SINGULAR_FIELD_CASE(Int32, INT32, Int)
      WRITE_SINGULAR_FIELD_CASE(Uint32, UINT32, Int)
      WRITE_SINGULAR_FIELD_CASE(Sint32, SINT32, Int)
      WRITE_SINGULAR_FIELD_CASE(Fixed32, FIXED32, Int)
      WRITE_SINGULAR_FIELD_CASE(Fixed32, SFIXED32, Int)
      WRITE_SINGULAR_FIELD_CASE(Int64, INT64, Long)
      WRITE_SINGULAR_FIELD_CASE(Int64, UINT64, Long)
      WRITE_SINGULAR_FIELD_CASE(Sint64, SINT64, Long)
      WRITE_SINGULAR_FIELD_CASE(Fixed64, FIXED64, Long)
      WRITE_SINGULAR_FIELD_CASE(Fixed64, SFIXED64, Long)
      WRITE_SINGULAR_FIELD_CASE(Bool, BOOL, Bool)
      WRITE_SINGULAR_FIELD_CASE(Float, FLOAT, Float)
      WRITE_SINGULAR_FIELD_CASE(Double, DOUBLE, Double)
      WRITE_SINGULAR_FIELD_CASE(Bytes, BYTES, Id)
      WRITE_SINGULAR_FIELD_CASE(String, STRING, Id)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_ENUM:
      CGPWriteEnum(CGPEnumGetIntValue(field->valueType_, *FIELD_PTR(id, msg, offset)), output);
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_GROUP:
      WriteMessage(*FIELD_PTR(id, msg, offset), field->valueType_, output);
      output->WriteTag(CGPWireFormatMakeTag(CGPFieldGetNumber(field), CGPWireFormatEndGroup));
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_MESSAGE:
      {
        id msgField = *FIELD_PTR(id, msg, offset);
        CGPDescriptor *msgDescriptor = field->valueType_;
        CGPWriteInt32(SerializedSizeForMessage(msgField, msgDescriptor), output);
        WriteMessage(msgField, msgDescriptor, output);
      }
      return;
  }
#undef WRITE_SINGULAR_FIELD_CASE
}

static void WriteMapEntryField(
    CGPFieldDescriptor *field, CGPValue value, CGPCodedOutputStream *output) {
  output->WriteTag(field->tag_);
  switch (CGPFieldGetType(field)) {
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_INT32:
      CGPWriteInt32(value.valueInt, output);
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_UINT32:
      CGPWriteUint32(value.valueInt, output);
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SINT32:
      CGPWriteSint32(value.valueInt, output);
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED32:
      CGPWriteFixed32(value.valueInt, output);
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_INT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_UINT64:
      CGPWriteInt64(value.valueLong, output);
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SINT64:
      CGPWriteSint64(value.valueLong, output);
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED64:
      CGPWriteFixed64(value.valueLong, output);
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BOOL:
      CGPWriteBool(value.valueBool, output);
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FLOAT:
      CGPWriteFloat(value.valueFloat, output);
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_DOUBLE:
      CGPWriteDouble(value.valueDouble, output);
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_ENUM:
      CGPWriteEnum(CGPEnumGetIntValue(field->valueType_, value.valueId), output);
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BYTES:
      CGPWriteBytes(value.valueId, output);
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_STRING:
      CGPWriteString(value.valueId, output);
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_GROUP:
      WriteMessage(value.valueId, field->valueType_, output);
      output->WriteTag(CGPWireFormatMakeTag(CGPFieldGetNumber(field), CGPWireFormatEndGroup));
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_MESSAGE:
      CGPWriteInt32(SerializedSizeForMessage(value.valueId, field->valueType_), output);
      WriteMessage(value.valueId, field->valueType_, output);
      return;
  }
}

static void WriteMapField(id msg, CGPFieldDescriptor *field, CGPCodedOutputStream *output) {
  size_t offset = CGPFieldGetOffset(field, object_getClass(msg));
  CGPMapFieldData *data = MAP_FIELD_PTR(msg, offset)->data;
  if (data == NULL) {
    return;
  }

  CGPFieldDescriptor *keyField = CGPFieldMapKey(field);
  CGPFieldDescriptor *valueField = CGPFieldMapValue(field);
  CGPMapFieldEnsureValidMap(data, CGPFieldGetJavaType(keyField), CGPFieldGetJavaType(valueField));
  CGPMapFieldEntry *entry = data->header.next;
  while (entry != &data->header) {
    int entrySize = SerializedSizeForMapEntryField(keyField, entry->key) +
        SerializedSizeForMapEntryField(valueField, entry->value);
    output->WriteTag(field->tag_);
    CGPWriteInt32(entrySize, output);
    WriteMapEntryField(CGPFieldMapKey(field), entry->key, output);
    WriteMapEntryField(CGPFieldMapValue(field), entry->value, output);
    entry = entry->next;
  }
}


static void WriteRepeatedField(id msg, CGPFieldDescriptor *field, CGPCodedOutputStream *output) {
  Class msgCls = object_getClass(msg);
  size_t offset = CGPFieldGetOffset(field, msgCls);
  CGPRepeatedFieldData *data = REPEATED_FIELD_PTR(msg, offset)->data;
  if (data == NULL) {
    return;
  }
  uint32_t arrayLen = data->size;

  switch (CGPFieldGetType(field)) {
#define WRITE_REPEATED_FIELD_VARIABLE_LENGTH_CASE(NAME, ENUM_NAME, JAVA_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_##ENUM_NAME: \
      { \
        TYPE_##JAVA_NAME *buffer = (TYPE_##JAVA_NAME *)data->buffer; \
        if (CGPFieldIsPacked(field)) { \
          int arraySize = 0; \
          for (uint32_t i = 0; i < arrayLen; i++) { \
            arraySize += CGPGet##NAME##Size(buffer[i]); \
          } \
          output->WriteTag(field->tag_); \
          CGPWriteInt32(arraySize, output); \
          for (uint32_t i = 0; i < arrayLen; i++) { \
            CGPWrite##NAME(buffer[i], output); \
          } \
        } else { \
          for (uint32_t i = 0; i < arrayLen; i++) { \
            output->WriteTag(field->tag_); \
            CGPWrite##NAME(buffer[i], output); \
          } \
        } \
      } \
      return;
#define WRITE_REPEATED_FIELD_FIXED_LENGTH_CASE(NAME, ENUM_NAME, JAVA_NAME, SIZE) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_##ENUM_NAME: \
      { \
        TYPE_##JAVA_NAME *buffer = (TYPE_##JAVA_NAME *)data->buffer; \
        if (CGPFieldIsPacked(field)) { \
          output->WriteTag(field->tag_); \
          CGPWriteInt32(arrayLen * SIZE, output); \
          for (uint32_t i = 0; i < arrayLen; i++) { \
            CGPWrite##NAME(buffer[i], output); \
          } \
        } else { \
          for (uint32_t i = 0; i < arrayLen; i++) { \
            output->WriteTag(field->tag_); \
            CGPWrite##NAME(buffer[i], output); \
          } \
        } \
      } \
      return;
      WRITE_REPEATED_FIELD_VARIABLE_LENGTH_CASE(Int32, INT32, Int)
      WRITE_REPEATED_FIELD_VARIABLE_LENGTH_CASE(Uint32, UINT32, Int)
      WRITE_REPEATED_FIELD_VARIABLE_LENGTH_CASE(Sint32, SINT32, Int)
      WRITE_REPEATED_FIELD_FIXED_LENGTH_CASE(Fixed32, FIXED32, Int, sizeof(uint32_t))
      WRITE_REPEATED_FIELD_FIXED_LENGTH_CASE(Fixed32, SFIXED32, Int, sizeof(uint32_t))
      WRITE_REPEATED_FIELD_VARIABLE_LENGTH_CASE(Int64, INT64, Long)
      WRITE_REPEATED_FIELD_VARIABLE_LENGTH_CASE(Int64, UINT64, Long)
      WRITE_REPEATED_FIELD_VARIABLE_LENGTH_CASE(Sint64, SINT64, Long)
      WRITE_REPEATED_FIELD_FIXED_LENGTH_CASE(Fixed64, FIXED64, Long, sizeof(uint64_t))
      WRITE_REPEATED_FIELD_FIXED_LENGTH_CASE(Fixed64, SFIXED64, Long, sizeof(uint64_t))
      WRITE_REPEATED_FIELD_FIXED_LENGTH_CASE(Bool, BOOL, Bool, 1)
      WRITE_REPEATED_FIELD_FIXED_LENGTH_CASE(Float, FLOAT, Float, sizeof(uint32_t))
      WRITE_REPEATED_FIELD_FIXED_LENGTH_CASE(Double, DOUBLE, Double, sizeof(uint64_t))
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_ENUM:
      {
        id *buffer = (id *)data->buffer;
        CGPEnumDescriptor *enumType = field->valueType_;
        if (CGPFieldIsPacked(field)) {
          std::vector<jint> intValues(arrayLen);
          int arraySize = 0;
          for (uint32_t i = 0; i < arrayLen; i++) {
            intValues[i] = CGPEnumGetIntValue(enumType, buffer[i]);
            arraySize += CGPGetEnumSize(intValues[i]);
          }
          output->WriteTag(field->tag_);
          CGPWriteInt32(arraySize, output);
          for (uint32_t i = 0; i < arrayLen; i++) {
            CGPWriteEnum(intValues[i], output);
          }
        } else {
          for (uint32_t i = 0; i < arrayLen; i++) {
            output->WriteTag(field->tag_);
            CGPWriteEnum(CGPEnumGetIntValue(enumType, buffer[i]), output);
          }
        }
      }
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BYTES:
      {
        id *buffer = (id *)data->buffer;
        for (uint32_t i = 0; i < arrayLen; i++) {
          output->WriteTag(field->tag_);
          CGPWriteBytes(buffer[i], output);
        }
      }
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_STRING:
      {
        id *buffer = (id *)data->buffer;
        for (uint32_t i = 0; i < arrayLen; i++) {
          output->WriteTag(field->tag_);
          CGPWriteString(buffer[i], output);
        }
      }
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_GROUP:
      {
        id *buffer = (id *)data->buffer;
        CGPDescriptor *msgType = field->valueType_;
        uint32_t endTag = CGPWireFormatMakeTag(CGPFieldGetNumber(field), CGPWireFormatEndGroup);
        for (uint32_t i = 0; i < arrayLen; i++) {
          output->WriteTag(field->tag_);
          WriteMessage(buffer[i], msgType, output);
          output->WriteTag(endTag);
        }
      }
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_MESSAGE:
      {
        id *buffer = (id *)data->buffer;
        for (uint32_t i = 0; i < arrayLen; i++) {
          id elem = buffer[i];
          output->WriteTag(field->tag_);
          CGPWriteInt32(SerializedSizeForMessage(elem, field->valueType_), output);
          WriteMessage(elem, field->valueType_, output);
        }
      }
      return;
  }
#undef WRITE_REPEATED_FIELD_VARIABLE_LENGTH_CASE
#undef WRITE_REPEATED_FIELD_FIXED_LENGTH_CASE
}

static void WriteField(id msg, CGPFieldDescriptor *field, CGPCodedOutputStream *output) {
  if (CGPFieldIsMap(field)) {
    WriteMapField(msg, field, output);
  } else if (CGPFieldIsRepeated(field)) {
    WriteRepeatedField(msg, field, output);
  } else {
    WriteSingularField(msg, field, output);
  }
}

static void WriteMessage(id msg, CGPDescriptor *descriptor, CGPCodedOutputStream *output) {
  IOSObjectArray *orderedFields = CGPGetSerializationOrderFields(descriptor);
  NSUInteger fieldsCount = orderedFields->size_;
  CGPFieldDescriptor **fieldsBuf = orderedFields->buffer_;
  CGPExtensionMap *extensionMap = MessageExtensionMap(msg, descriptor);
  if (extensionMap == NULL) {
    for (NSUInteger i = 0; i < fieldsCount; i++) {
      WriteField(msg, fieldsBuf[i], output);
    }
  } else {
    NSUInteger fieldIndex = 0;
    CGPFieldDescriptor *nextField = nil;
    CGPExtensionMap::iterator nextExtension = extensionMap->begin();
    if (fieldsCount > 0) {
      nextField = fieldsBuf[fieldIndex++];
    }
    while (nextField != nil || nextExtension != extensionMap->end()) {
      if (nextExtension == extensionMap->end()
          || (nextField != nil
              && CGPFieldGetNumber(nextField) < CGPFieldGetNumber(nextExtension->first))) {
        WriteField(msg, nextField, output);
        if (fieldIndex == fieldsCount) {
          nextField = nil;
        } else {
          nextField = fieldsBuf[fieldIndex++];
        }
      } else {
        if (CGPIsMessageSetWireFormat(descriptor)) {
          WriteMessageSetExtension(nextExtension->first, nextExtension->second.get(), output);
        } else {
          WriteExtension(nextExtension->first, nextExtension->second.get(), output);
        }
        nextExtension++;
      }
    }
  }
}


// *****************************************************************************
// ********** isInitialized ****************************************************
// *****************************************************************************

static BOOL MessageIsInitialized(id msg, CGPDescriptor *descriptor) {
  NSUInteger fieldsCount = descriptor->fields_->size_;
  CGPFieldDescriptor **fieldsBuf = descriptor->fields_->buffer_;
  for (NSUInteger i = 0; i < fieldsCount; i++) {
    CGPFieldDescriptor *field = fieldsBuf[i];
    if (CGPFieldIsMap(field)) {
      CGPFieldDescriptor *valueField = CGPFieldMapValue(field);
      if (CGPFieldTypeIsMessage(valueField)) {
        size_t offset = CGPFieldGetOffset(field, object_getClass(msg));
        CGPMapFieldData *data = MAP_FIELD_PTR(msg, offset)->data;
        if (data != NULL) {
          CGPDescriptor *msgType = valueField->valueType_;
          CGPMapFieldEnsureValidMap(
              data, CGPFieldGetJavaType(CGPFieldMapKey(field)), CGPFieldGetJavaType(valueField));
          CGPMapFieldEntry *entry = data->header.next;
          while (entry != &data->header) {
            if (entry != NULL && !MessageIsInitialized(entry->value.valueId, msgType)) {
              return NO;
            }
            entry = entry->next;
          }
        }
      }
    } else if (CGPFieldIsRepeated(field)) {
      if (CGPFieldTypeIsMessage(field)) {
        size_t offset = CGPFieldGetOffset(field, object_getClass(msg));
        CGPRepeatedField *repeatedField = REPEATED_FIELD_PTR(msg, offset);
        CGPRepeatedFieldData *data = repeatedField->data;
        if (data != NULL) {
          uint32_t arraySize = data->size;
          id *arrayBuffer = (id *)data->buffer;
          CGPDescriptor *msgType = field->valueType_;
          for (uint32_t i = 0; i < arraySize; i++) {
            if (!MessageIsInitialized(arrayBuffer[i], msgType)) return NO;
          }
        }
      }
    } else {
      BOOL isMessage = CGPFieldTypeIsMessage(field);
      BOOL required = CGPFieldIsRequired(field);
      if (!required && !isMessage) continue;
      Class msgCls = object_getClass(msg);
      bool hasField = GetHas(msg, GetHasLocator(msgCls, field));
      if (required && !hasField) return NO;
      if (isMessage && hasField) {
        size_t offset = CGPFieldGetOffset(field, msgCls);
        id fieldValue = *FIELD_PTR(id, msg, offset);
        if (!MessageIsInitialized(fieldValue, field->valueType_)) return NO;
      }
    }
  }
  return YES;
}


// *****************************************************************************
// ********** toString *********************************************************
// *****************************************************************************

// Partially translated from com.google.protobuf.TextFormat.escapeBytes().
static NSString *BytesToString(CGPByteString *byteString) {
  int length = byteString->size_;
  int8_t *buffer = byteString->buffer_;
  JavaLangStringBuilder *builder = AUTORELEASE([[JavaLangStringBuilder alloc] initWithInt:length]);
  for (int i = 0; i < length; i++) {
    int8_t b = buffer[i];
    switch (b) {
      case (int) 0x07:
        [builder appendWithNSString:@"\\a"]; break;
      case 0x0008:
        [builder appendWithNSString:@"\\b"]; break;
      case 0x000c:
        [builder appendWithNSString:@"\\f"]; break;
      case 0x000a:
        [builder appendWithNSString:@"\\n"]; break;
      case 0x000d:
        [builder appendWithNSString:@"\\r"]; break;
      case 0x0009:
        [builder appendWithNSString:@"\\t"]; break;
      case (int) 0x0b:
        [builder appendWithNSString:@"\\v"]; break;
      case '\\':
        [builder appendWithNSString:@"\\\\"]; break;
      case '\'':
        [builder appendWithNSString:@"\\'"]; break;
      case '"':
        [builder appendWithNSString:@"\\\""]; break;
      default:
        if (b >= (int) 0x20) {
          [builder appendWithChar:(unichar) b];
        } else {
          [builder appendWithChar:'\\'];
          [builder appendWithChar:(unichar) ('0' + (((char) (((unsigned char) b) >> 6)) & 3))];
          [builder appendWithChar:(unichar) ('0' + (((char) (((unsigned char) b) >> 3)) & 7))];
          [builder appendWithChar:(unichar) ('0' + (b & 7))];
        }
        break;
    }
  }
  return [builder description];
}

static void ExtensionFieldToString(
    id value, CGPFieldDescriptor *field, NSMutableString *builder, const char *padding,
    int indent) {
  const char *fieldName = field->data_->name;

  switch (CGPFieldGetType(field)) {
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_INT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SINT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_UINT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_INT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SINT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_UINT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BOOL:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FLOAT:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_DOUBLE:
      [builder appendFormat:@"%s[%s]: %@\n", padding, fieldName, value];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_ENUM:
      [builder appendFormat:@"%s[%s]: %@\n",
          padding, fieldName, ((CGPEnumValueDescriptor *)value)->enum_];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BYTES:
      [builder appendFormat:@"%s[%s]: \"%@\"\n", padding, fieldName, BytesToString(value)];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_STRING:
      [builder appendFormat:@"%s[%s]: \"%@\"\n", padding, fieldName, value];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_GROUP:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_MESSAGE:
      [builder appendFormat:@"%s[%s]: {\n", padding, fieldName];
      MessageToString(value, field->valueType_, builder, indent + 1);
      [builder appendFormat:@"%s}\n", padding];
      return;
  }
}

void ValueToString(
    CGPValue value, CGPFieldDescriptor *field, NSMutableString *builder, const char *padding,
    int indent) {
  const char *fieldName = field->data_->name;
  switch (CGPFieldGetType(field)) {
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_INT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SINT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED32:
      [builder appendFormat:@"%s%s: %d\n", padding, fieldName, value.valueInt];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_UINT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED32:
      [builder appendFormat:@"%s%s: %u\n", padding, fieldName, (uint32_t)value.valueInt];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_INT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SINT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED64:
      [builder appendFormat:@"%s%s: %qd\n", padding, fieldName, value.valueLong];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_UINT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED64:
      [builder appendFormat:@"%s%s: %qu\n", padding, fieldName, (uint64_t)value.valueLong];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BOOL:
      [builder appendFormat:@"%s%s: %s\n", padding, fieldName, value.valueBool ? "true" : "false"];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FLOAT:
      [builder appendFormat:@"%s%s: %g\n", padding, fieldName, value.valueFloat];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_DOUBLE:
      [builder appendFormat:@"%s%s: %g\n", padding, fieldName, value.valueDouble];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_ENUM:
      [builder appendFormat:@"%s%s: %@\n", padding, fieldName, value.valueId];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BYTES:
      [builder appendFormat:@"%s%s: %@\n", padding, fieldName, BytesToString(value.valueId)];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_STRING:
      [builder appendFormat:@"%s%s: \"%@\"\n", padding, fieldName, value.valueId];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_GROUP:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_MESSAGE:
      [builder appendFormat:@"%s%s: {\n", padding, fieldName];
      MessageToString(value.valueId, field->valueType_, builder, indent + 1);
      [builder appendFormat:@"%s}\n", padding];
      return;
  }
}

static void MapFieldToString(
    id msg, CGPFieldDescriptor *field, NSMutableString *builder, const char *padding, int indent) {
  size_t offset = CGPFieldGetOffset(field, object_getClass(msg));
  CGPMapFieldData *data = MAP_FIELD_PTR(msg, offset)->data;
  if (data == NULL) {
    return;
  }

  int paddingSize = (indent + 1) * 2;
  std::string innerPadding(paddingSize, ' ');

  const char *fieldName = field->data_->name;
  CGPFieldDescriptor *keyField = CGPFieldMapKey(field);
  CGPFieldDescriptor *valueField = CGPFieldMapValue(field);
  CGPMapFieldEnsureValidMap(data, CGPFieldGetJavaType(keyField), CGPFieldGetJavaType(valueField));
  CGPMapFieldEntry *entry = data->header.next;
  while (entry != &data->header) {
    [builder appendFormat:@"%s%s: {\n", padding, fieldName];
    ValueToString(entry->key, keyField, builder, innerPadding.c_str(), indent + 1);
    ValueToString(entry->value, valueField, builder, innerPadding.c_str(), indent + 1);
    [builder appendFormat:@"%s}\n", padding];
    entry = entry->next;
  }
}

static void FieldToString(
    id msg, CGPFieldDescriptor *field, NSMutableString *builder, const char *padding, int indent) {
  Class msgCls = object_getClass(msg);
  size_t offset = CGPFieldGetOffset(field, msgCls);
  BOOL repeated = CGPFieldIsRepeated(field);
  CGPRepeatedFieldData *data = NULL;
  if (repeated) {
    data = REPEATED_FIELD_PTR(msg, offset)->data;
    if (data == NULL) {
      return;
    }
  } else {
    if (!GetHas(msg, GetHasLocator(msgCls, field))) {
      return;
    }
  }
  const char *fieldName = field->data_->name;

  switch (CGPFieldGetType(field)) {
#define FIELD_TO_STRING_CASE(TYPE, FORMAT, VALUE) \
      if (repeated) { \
        TYPE *buffer = (TYPE *)data->buffer; \
        for (uint32_t i = 0; i < data->size; i++) { \
          TYPE value = buffer[i]; \
          [builder appendFormat:@"%s%s: " FORMAT "\n", padding, fieldName, VALUE]; \
        } \
      } else { \
        TYPE value = *FIELD_PTR(TYPE, msg, offset); \
        [builder appendFormat:@"%s%s: " FORMAT "\n", padding, fieldName, VALUE]; \
      } \
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_INT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SINT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED32:
      FIELD_TO_STRING_CASE(int32_t, "%d", value)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_UINT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED32:
      FIELD_TO_STRING_CASE(uint32_t, "%u", value)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_INT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SINT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED64:
      FIELD_TO_STRING_CASE(int64_t, "%qd", value)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_UINT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED64:
      FIELD_TO_STRING_CASE(uint64_t, "%qu", value)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BOOL:
      FIELD_TO_STRING_CASE(BOOL, "%s", value ? "true" : "false")
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FLOAT:
      FIELD_TO_STRING_CASE(float, "%g", value)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_DOUBLE:
      FIELD_TO_STRING_CASE(double, "%g", value)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_ENUM:
      FIELD_TO_STRING_CASE(id, "%@", value)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BYTES:
      FIELD_TO_STRING_CASE(id, "%@", BytesToString(value))
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_STRING:
      FIELD_TO_STRING_CASE(id, "\"%@\"", value)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_GROUP:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_MESSAGE:
      if (repeated) {
        id *buffer = (id *)data->buffer;
        for (uint32_t i = 0; i < data->size; i++) {
          [builder appendFormat:@"%s%s: {\n", padding, fieldName];
          MessageToString(buffer[i], field->valueType_, builder, indent + 1);
          [builder appendFormat:@"%s}\n", padding];
        }
      } else {
        [builder appendFormat:@"%s%s: {\n", padding, fieldName];
        MessageToString(*FIELD_PTR(id, msg, offset), field->valueType_, builder, indent + 1);
        [builder appendFormat:@"%s}\n", padding];
      }
      return;
  }
}

static void MessageToString(
    id msg, CGPDescriptor *descriptor, NSMutableString *builder, int indent) {
  int paddingSize = indent * 2;
  std::string padding(paddingSize, ' ');

  NSUInteger fieldsCount = descriptor->fields_->size_;
  CGPFieldDescriptor **fieldsBuf = descriptor->fields_->buffer_;
  for (NSUInteger i = 0; i < fieldsCount; i++) {
    CGPFieldDescriptor *field = fieldsBuf[i];
    if (CGPFieldIsMap(field)) {
      MapFieldToString(msg, field, builder, padding.c_str(), indent);
    } else {
      FieldToString(msg, field, builder, padding.c_str(), indent);
    }
  }
  CGPExtensionMap *extensionMap = MessageExtensionMap(msg, descriptor);
  if (extensionMap != NULL) {
    for (CGPExtensionMap::iterator it = extensionMap->begin(); it != extensionMap->end(); it++) {
      CGPFieldDescriptor *field = it->first;
      if (CGPFieldIsRepeated(field)) {
        id<JavaUtilList> list = it->second.get();
        for (id elem in list) {
          ExtensionFieldToString(elem, field, builder, padding.c_str(), indent);
        }
      } else {
        ExtensionFieldToString(it->second.get(), field, builder, padding.c_str(), indent);
      }
    }
  }
}


// *****************************************************************************
// ********** isEqual and hash *************************************************
// *****************************************************************************

#define FieldIsEqualInt(a, b) a == b
#define FieldIsEqualLong(a, b) a == b
#define FieldIsEqualFloat(a, b) a == b
#define FieldIsEqualDouble(a, b) a == b
#define FieldIsEqualBool(a, b) a == b
#define FieldIsEqualEnum(a, b) a == b
#define FieldIsEqualRetainable(a, b) a == b || [a isEqual:b]

static BOOL FieldIsEqual(id self, id other, size_t offset, CGPFieldJavaType type) {
#define IS_FIELD_EQUAL_CASE(NAME) \
  return FieldIsEqual##NAME( \
      *FIELD_PTR(TYPE_##NAME, self, offset), *FIELD_PTR(TYPE_##NAME, other, offset));

  SWITCH_TYPES_WITH_ENUM(type, IS_FIELD_EQUAL_CASE)

#undef IS_FIELD_EQUAL_CASE

  __builtin_unreachable();
}

static BOOL MessageIsEqual(id msg, id other, CGPDescriptor *descriptor) {
  if (msg == other) {
    return YES;
  }
  Class msgCls = object_getClass(msg);
  if (msgCls != object_getClass(other)) {
    return NO;
  }
  NSUInteger count = descriptor->fields_->size_;
  CGPFieldDescriptor **fields = descriptor->fields_->buffer_;
  for (NSUInteger i = 0; i < count; i++) {
    CGPFieldDescriptor *field = fields[i];
    size_t offset = CGPFieldGetOffset(field, msgCls);
    CGPFieldJavaType type = CGPFieldGetJavaType(field);
    if (CGPFieldIsMap(field)) {
      CGPFieldJavaType keyType = CGPFieldGetJavaType(CGPFieldMapKey(field));
      CGPFieldJavaType valueType = CGPFieldGetJavaType(CGPFieldMapValue(field));
      CGPMapField *msgMapField = MAP_FIELD_PTR(msg, offset);
      CGPMapField *otherMapField = MAP_FIELD_PTR(other, offset);
      if (!CGPMapFieldIsEqual(msgMapField, otherMapField, keyType, valueType)) {
        return NO;
      }
    } else if (CGPFieldIsRepeated(field)) {
      CGPRepeatedField *msgRepeatedField = REPEATED_FIELD_PTR(msg, offset);
      CGPRepeatedField *otherRepeatedField = REPEATED_FIELD_PTR(other, offset);
      if (!CGPRepeatedFieldIsEqual(msgRepeatedField, otherRepeatedField, type)) {
        return NO;
      }
    } else {
      CGPHasLocator hasLoc = GetHasLocator(msgCls, field);
      bool msgHasField = GetHas(msg, hasLoc);
      bool otherHasField = GetHas(other, hasLoc);
      if (msgHasField != otherHasField) {
        return NO;
      }
      if (msgHasField && !FieldIsEqual(msg, other, offset, type)) {
        return NO;
      }
    }
  }
  NSCAssert(msgCls == descriptor->messageClass_, @"Message type expected.");
  CGPExtensionMap *msgExtensionMap = MessageExtensionMap(msg, descriptor);
  CGPExtensionMap *otherExtensionMap = MessageExtensionMap(other, descriptor);
  if (msgExtensionMap != NULL && *msgExtensionMap != *otherExtensionMap) {
    return NO;
  }
  return YES;
}

static int MapFieldHash(id msg, CGPFieldDescriptor *field, int hash) {
  size_t offset = CGPFieldGetOffset(field, object_getClass(msg));
  CGPMapField *mapField = MAP_FIELD_PTR(msg, offset);
  if (CGPMapFieldIsEmpty(mapField)) {
    return hash;
  }
  hash = 37 * hash + CGPFieldGetNumber(field);
  CGPFieldJavaType keyType = CGPFieldGetJavaType(CGPFieldMapKey(field));
  CGPFieldJavaType valueType = CGPFieldGetJavaType(CGPFieldMapValue(field));
  return 53 * hash + CGPMapFieldHash(mapField, keyType, valueType);
}

static int RepeatedFieldHash(id msg, CGPFieldDescriptor *field, int hash) {
  size_t offset = CGPFieldGetOffset(field, object_getClass(msg));
  CGPRepeatedField *repeatedField = REPEATED_FIELD_PTR(msg, offset);
  uint32_t length = CGPRepeatedFieldSize(repeatedField);
  if (length == 0) {
    return hash;
  }
  hash = 37 * hash + CGPFieldGetNumber(field);

#define REPEATED_FIELD_HASH_CASE(NAME) \
  { \
    TYPE_##NAME *buffer = (TYPE_##NAME *)repeatedField->data->buffer; \
    for (uint32_t i = 0; i < length; i++) { \
      TYPE_##NAME value = buffer[i]; \
      hash = 31 * hash + HASH_##NAME(value); \
    } \
  } \
  break;

  SWITCH_TYPES_NO_ENUM(CGPFieldGetJavaType(field), REPEATED_FIELD_HASH_CASE)

#undef REPEATED_FIELD_HASH_CASE

  return hash;
}

static int SingularFieldHash(id msg, CGPFieldDescriptor *field, int hash) {
  Class msgCls = object_getClass(msg);
  if (!GetHas(msg, GetHasLocator(msgCls, field))) {
    return hash;
  }
  size_t offset = CGPFieldGetOffset(field, msgCls);
  hash = 37 * hash + CGPFieldGetNumber(field);

#define SINGULAR_FIELD_HASH_CASE(NAME) \
  { \
    TYPE_##NAME value = *FIELD_PTR(TYPE_##NAME, msg, offset); \
    return 53 * hash + HASH_##NAME(value); \
  }

  SWITCH_TYPES_NO_ENUM(CGPFieldGetJavaType(field), SINGULAR_FIELD_HASH_CASE)

#undef SINGULAR_FIELD_HASH_CASE

  __builtin_unreachable();
}

static int MessageHash(ComGoogleProtobufGeneratedMessage *msg, CGPDescriptor *descriptor) {
  int hash = msg->memoizedHash_;
  if (hash != 0) {
    return hash;
  }
  hash = 41;
  hash = 19 * hash + (int)[descriptor hash];
  NSUInteger count = descriptor->fields_->size_;
  CGPFieldDescriptor **fields = descriptor->fields_->buffer_;
  for (NSUInteger i = 0; i < count; i++) {
    CGPFieldDescriptor *field = fields[i];
    if (CGPFieldIsMap(field)) {
      hash = MapFieldHash(msg, field, hash);
    } else if (CGPFieldIsRepeated(field)) {
      hash = RepeatedFieldHash(msg, field, hash);
    } else {
      hash = SingularFieldHash(msg, field, hash);
    }
  }
  CGPExtensionMap *extensionMap = MessageExtensionMap(msg, descriptor);
  if (extensionMap != NULL) {
    for (CGPExtensionMap::iterator it = extensionMap->begin(); it != extensionMap->end(); it++) {
      hash = 31 * hash + ((int)[it->first hash] ^ (int)[it->second.get() hash]);
    }
  }
  msg->memoizedHash_ = hash;
  return hash;
}


// *****************************************************************************
// ********** Objective C type implementations *********************************
// *****************************************************************************

@implementation ComGoogleProtobufGeneratedMessage

+ (id)allocWithZone:(NSZone *)zone {
  NSAssert(NO, @"Direct allocation of protocol buffer messages is forbidden.");
  return nil;
}

+ (ComGoogleProtobufGeneratedMessage_Builder *)newBuilder {
  CGPDescriptor *descriptor = [self getDescriptor];
  return AUTORELEASE(CGPNewBuilder(descriptor));
}

- (ComGoogleProtobufGeneratedMessage_Builder *)newBuilderForType {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  return AUTORELEASE(CGPNewBuilder(descriptor));
}

- (ComGoogleProtobufGeneratedMessage_Builder *)toBuilder {
  Class selfCls = object_getClass(self);
  CGPDescriptor *descriptor = [selfCls getDescriptor];
  ComGoogleProtobufGeneratedMessage_Builder *newBuilder = CGPNewBuilder(descriptor);
  CopyAllFields(self, selfCls, newBuilder, descriptor->builderClass_, descriptor);
  return AUTORELEASE(newBuilder);
}

+ (ComGoogleProtobufGeneratedMessage *)getDefaultInstance {
  CGPDescriptor *descriptor = [self getDescriptor];
  return AUTORELEASE(CGPNewMessage(descriptor));
}

- (ComGoogleProtobufGeneratedMessage *)getDefaultInstanceForType {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  return descriptor->defaultInstance_;
}

+ (id)parseFromWithByteArray:(IOSByteArray *)bytes {
  return CGPParseFromByteArray([self getDescriptor], bytes, nil);
}

+ (id)parseFromWithByteArray:(IOSByteArray *)bytes
    withComGoogleProtobufExtensionRegistryLite:(CGPExtensionRegistryLite *)registry {
  return CGPParseFromByteArray([self getDescriptor], bytes, registry);
}

+ (id)parseFromNSData:(NSData *)data {
  return [self parseFromNSData:data registry:nil];
}

+ (id)parseFromNSData:(NSData *)data registry:(CGPExtensionRegistryLite *)registry {
  CGPDescriptor *descriptor = [self getDescriptor];
  ComGoogleProtobufGeneratedMessage *msg = AUTORELEASE(CGPNewMessage(descriptor));
  CGPCodedInputStream codedStream([data bytes], (int)[data length]);
  BOOL success =
      MergeFromStream(msg, descriptor, &codedStream, registry, MessageExtensionMap(msg, descriptor))
      && codedStream.ConsumedEntireMessage();
  if (!success) {
    InvalidPB();
  }
  return msg;
}

+ (id)parseFromWithJavaIoInputStream:(JavaIoInputStream *)input {
  return CGPParseFromInputStream([self getDescriptor], input, nil);
}

+ (id)parseFromWithJavaIoInputStream:(JavaIoInputStream *)input
    withComGoogleProtobufExtensionRegistryLite:(CGPExtensionRegistryLite *)registry {
  return CGPParseFromInputStream([self getDescriptor], input, registry);
}

+ (id)parseDelimitedFromWithJavaIoInputStream:(JavaIoInputStream *)input {
  return CGPParseDelimitedFromInputStream([self getDescriptor], input, nil);
}

+ (id)parseDelimitedFromWithJavaIoInputStream:(JavaIoInputStream *)input
    withComGoogleProtobufExtensionRegistryLite:(CGPExtensionRegistryLite *)registry {
  return CGPParseDelimitedFromInputStream([self getDescriptor], input, registry);
}

- (jint)getSerializedSize {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  return SerializedSizeForMessage(self, descriptor);
}

- (IOSByteArray *)toByteArray {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  jint size = [self getSerializedSize];
  IOSByteArray *array = [IOSByteArray arrayWithLength:size];
  CGPCodedOutputStream codedStream(array->buffer_, (int)array->size_);
  WriteMessage(self, descriptor, &codedStream);
  NSAssert(!codedStream.HadError(), @"Serialization error");
  return array;
}

- (CGPByteString *)toByteString {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  jint size = [self getSerializedSize];
  CGPByteString *byteString = CGPNewByteString(size);
  CGPCodedOutputStream codedStream(byteString->buffer_, byteString->size_);
  WriteMessage(self, descriptor, &codedStream);
  NSAssert(!codedStream.HadError(), @"Serialization error");
  return AUTORELEASE(byteString);
}

- (NSData *)toNSData {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  jint size = [self getSerializedSize];
  void *buffer = malloc(size);
  CGPCodedOutputStream codedStream(buffer, size);
  WriteMessage(self, descriptor, &codedStream);
  NSAssert(!codedStream.HadError(), @"Serialization error");
  return [NSData dataWithBytesNoCopy:buffer length:size freeWhenDone:YES];
}

- (void)writeToWithJavaIoOutputStream:(JavaIoOutputStream *)output {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  CGPCodedOutputStream codedStream(output);
  WriteMessage(self, descriptor, &codedStream);
  codedStream.FlushBuffer();
  NSAssert(!codedStream.HadError(), @"Serialization error");
}

- (void)writeDelimitedToWithJavaIoOutputStream:(JavaIoOutputStream *)output {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  CGPCodedOutputStream codedStream(output);
  CGPWriteInt32(SerializedSizeForMessage(self, descriptor), &codedStream);
  WriteMessage(self, descriptor, &codedStream);
  codedStream.FlushBuffer();
  NSAssert(!codedStream.HadError(), @"Serialization error");
}

- (CGPDescriptor *)getDescriptorForType {
  return [object_getClass(self) getDescriptor];
}

- (id)getFieldWithComGoogleProtobufDescriptors_FieldDescriptor:(CGPFieldDescriptor *)descriptor {
  return GetField(self, descriptor);
}

- (jboolean)hasFieldWithComGoogleProtobufDescriptors_FieldDescriptor:
    (CGPFieldDescriptor *)descriptor {
  return HasField(self, descriptor);
}

- (jint)getRepeatedFieldCountWithComGoogleProtobufDescriptors_FieldDescriptor:
    (CGPFieldDescriptor *)descriptor {
  return GetRepeatedFieldCount(self, descriptor);
}

- (id)getRepeatedFieldWithComGoogleProtobufDescriptors_FieldDescriptor:
    (CGPFieldDescriptor *)descriptor withInt:(jint)index {
  return GetRepeatedField(self, descriptor, index);
}

- (id<JavaUtilMap>)getAllFields {
  return GetAllFields(self);
}

- (BOOL)isEqual:(id)other {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  return MessageIsEqual(self, other, descriptor);
}

- (NSUInteger)hash {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  return MessageHash(self, descriptor);
}

- (NSString *)description {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  NSMutableString *builder = [NSMutableString string];
  MessageToString(self, descriptor, builder, 0);
  return builder;
}

- (void)dealloc {
  Class selfCls = object_getClass(self);
  CGPDescriptor *descriptor = [selfCls getDescriptor];
  ReleaseAllFields(self, selfCls, descriptor);
  [super dealloc];
}

+ (BOOL)resolveInstanceMethod:(SEL)sel {
  if (!class_respondsToSelector(object_getClass(self), @selector(getDescriptor))) {
    return NO;
  }
  ComGoogleProtobufDescriptors_Descriptor *descriptor = [self getDescriptor];
  return ResolveAccessor(self, descriptor, sel, NO);
}

static id DynamicNewBuilder(Class self, SEL _cmd, ComGoogleProtobufGeneratedMessage *prototype) {
  CGPDescriptor *descriptor = [self getDescriptor];
  ComGoogleProtobufGeneratedMessage_Builder *builder = CGPNewBuilder(descriptor);
  CopyMessage(builder, BuilderExtensionMap(builder, descriptor),
              prototype, MessageExtensionMap(prototype, descriptor), descriptor);
  return AUTORELEASE(builder);
}

+ (BOOL)resolveClassMethod:(SEL)sel {
  const char *name = sel_getName(sel);
  if (memcmp(name, "newBuilderWith", 14) == 0) {
    const char *className = class_getName(self);
    size_t classNameLen = strlen(className);
    if (memcmp(name + 14, className, classNameLen) == 0
        && strcmp(name + classNameLen + 14, ":") == 0) {
      IMP imp = (IMP)DynamicNewBuilder;
      return class_addMethod(object_getClass(self), sel, imp, "@#:@");
    }
  }
  return NO;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufGeneratedMessage)

@implementation ComGoogleProtobufGeneratedMessage_Builder

+ (id)allocWithZone:(NSZone *)zone {
  NSAssert(NO, @"Direct allocation of protocol buffer messages is forbidden.");
  return nil;
}

- (id)build {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  if (!MessageIsInitialized(self, descriptor)) {
    @throw AUTORELEASE([[JavaLangRuntimeException alloc]
        initWithNSString:@"Message was missing required fields."]);
  }
  return [self buildPartial];
}

- (id)buildPartial {
  Class selfCls = object_getClass(self);
  CGPDescriptor *descriptor = [selfCls getDescriptor];
  ComGoogleProtobufGeneratedMessage *newMsg = CGPNewMessage(descriptor);
  CopyAllFields(self, selfCls, newMsg, descriptor->messageClass_, descriptor);
  return AUTORELEASE(newMsg);
}

- (ComGoogleProtobufGeneratedMessage_Builder *)clear {
  Class selfCls = object_getClass(self);
  CGPDescriptor *descriptor = [selfCls getDescriptor];
  ReleaseAllFields(self, selfCls, descriptor);
  uint8_t *fieldStorage = (uint8_t *)self + class_getInstanceSize(selfCls);
  memset(fieldStorage, 0, descriptor->storageSize_);
  return self;
}

- (CGPDescriptor *)getDescriptorForType {
  return [object_getClass(self) getDescriptor];
}

- (ComGoogleProtobufGeneratedMessage *)getDefaultInstanceForType {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  return descriptor->defaultInstance_;
}

- (id<ComGoogleProtobufMessage_Builder>)
      newBuilderForFieldWithComGoogleProtobufDescriptors_FieldDescriptor:
          (CGPFieldDescriptor *)fieldDescriptor {
  CGPDescriptor *descriptor = [fieldDescriptor getMessageType];
  return AUTORELEASE(CGPNewBuilder(descriptor));
}

- (id)getFieldWithComGoogleProtobufDescriptors_FieldDescriptor:(CGPFieldDescriptor *)descriptor {
  return GetField(self, descriptor);
}

- (jboolean)hasFieldWithComGoogleProtobufDescriptors_FieldDescriptor:
    (CGPFieldDescriptor *)descriptor {
  return HasField(self, descriptor);
}

- (jint)getRepeatedFieldCountWithComGoogleProtobufDescriptors_FieldDescriptor:
    (CGPFieldDescriptor *)descriptor {
  return GetRepeatedFieldCount(self, descriptor);
}

- (id)getRepeatedFieldWithComGoogleProtobufDescriptors_FieldDescriptor:
    (CGPFieldDescriptor *)descriptor withInt:(jint)index {
  return GetRepeatedField(self, descriptor, index);
}

- (id<ComGoogleProtobufMessage_Builder>)
    setFieldWithComGoogleProtobufDescriptors_FieldDescriptor:(CGPFieldDescriptor *)descriptor
                                                      withId:(id)object {
  (void)nil_chk(descriptor);
  (void)nil_chk(object);
  CGPFieldJavaType javaType = CGPFieldGetJavaType(descriptor);
  Class cls = object_getClass(self);
  size_t offset = CGPFieldGetOffset(descriptor, cls);
  if (CGPFieldIsMap(descriptor)) {
    CGPMapFieldAssignFromList(MAP_FIELD_PTR(self, offset), object, descriptor);
  } else if (CGPFieldIsRepeated(descriptor)) {
    CGPRepeatedFieldAssignFromList(REPEATED_FIELD_PTR(self, offset), object, javaType);
  } else {
    CGPHasLocator hasLoc = GetHasLocator(cls, descriptor);

#define SET_SINGULAR_FIELD_CASE(NAME) \
  SingularSet##NAME(self, CGPFromReflectionType##NAME(object), offset, hasLoc); \
  break;

    SWITCH_TYPES_WITH_ENUM(javaType, SET_SINGULAR_FIELD_CASE)

#undef SET_SINGULAR_FIELD_CASE

  }
  return self;
}

- (id<ComGoogleProtobufMessage_Builder>)
    addRepeatedFieldWithComGoogleProtobufDescriptors_FieldDescriptor:
    (CGPFieldDescriptor *)descriptor withId:(id)object {
  (void)nil_chk(object);
  size_t offset = CGPFieldGetOffset(descriptor, object_getClass(self));
  if (CGPFieldIsMap(descriptor)) {
    CGPMapFieldAdd(MAP_FIELD_PTR(self, offset), object, descriptor);
  } else {
    CGPRepeatedFieldAdd(REPEATED_FIELD_PTR(self, offset), object, CGPFieldGetJavaType(descriptor));
  }
  return self;
}

- (id<ComGoogleProtobufMessage_Builder>)
    setRepeatedFieldWithComGoogleProtobufDescriptors_FieldDescriptor:
    (CGPFieldDescriptor *)descriptor withInt:(jint)index withId:(id)object {
  (void)nil_chk(object);
  size_t offset = CGPFieldGetOffset(descriptor, object_getClass(self));
  if (CGPFieldIsMap(descriptor)) {
    CGPMapFieldSet(MAP_FIELD_PTR(self, offset), index, object, descriptor);
  } else {
    CGPRepeatedFieldSet(
        REPEATED_FIELD_PTR(self, offset), index, object, CGPFieldGetJavaType(descriptor));
  }
  return self;
}

- (id<ComGoogleProtobufMessage_Builder>)
    clearFieldWithComGoogleProtobufDescriptors_FieldDescriptor:(CGPFieldDescriptor *)descriptor {
  Class cls = object_getClass(self);
  size_t offset = CGPFieldGetOffset(descriptor, cls);
  if (CGPFieldIsMap(descriptor)) {
    CGPFieldJavaType keyType = CGPFieldGetJavaType(CGPFieldMapKey(descriptor));
    CGPFieldJavaType valueType = CGPFieldGetJavaType(CGPFieldMapValue(descriptor));
    CGPMapFieldClear(MAP_FIELD_PTR(self, offset), keyType, valueType);
  } else if (CGPFieldIsRepeated(descriptor)) {
    CGPRepeatedFieldClear(REPEATED_FIELD_PTR(self, offset), CGPFieldGetJavaType(descriptor));
  } else {
    CGPHasLocator hasLoc = GetHasLocator(cls, descriptor);
    if (UnsetHas(self, hasLoc) && CGPIsRetainedType(CGPFieldGetJavaType(descriptor))) {
      id *ptr = FIELD_PTR(id, self, offset);
      AUTORELEASE(*ptr);
      *ptr = nil;
    }
  }
  return self;
}

- (id<JavaUtilMap>)getAllFields {
  return GetAllFields(self);
}

- (id<ComGoogleProtobufMessage_Builder>)mergeFromWithComGoogleProtobufMessage:
    (id<ComGoogleProtobufMessage>)message {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  CGPDescriptor *otherDescriptor = [object_getClass(message) getDescriptor];
  if (descriptor != otherDescriptor) {
    @throw [[[JavaLangIllegalArgumentException alloc] initWithNSString:
        @"mergeFrom(Message) can only merge messages of the same type."] autorelease];
  }
  MergeFromMessage(
      self, BuilderExtensionMap(self, descriptor), message,
      MessageExtensionMap(message, descriptor), descriptor);
  return self;
}

- (id<ComGoogleProtobufMessage_Builder>)
    mergeFromWithJavaIoInputStream:(JavaIoInputStream *)input {
  return [self mergeFromWithJavaIoInputStream:input withComGoogleProtobufExtensionRegistryLite:nil];
}

- (id<ComGoogleProtobufMessage_Builder>)
    mergeFromWithJavaIoInputStream:(JavaIoInputStream *)input
    withComGoogleProtobufExtensionRegistryLite:
        (ComGoogleProtobufExtensionRegistryLite *)extensionRegistry {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  CGPCodedInputStream codedStream(input, INT_MAX);
  BOOL success = MergeFromStream(
      self, descriptor, &codedStream, extensionRegistry, BuilderExtensionMap(self, descriptor))
      && codedStream.ConsumedEntireMessage();
  if (!success) {
    InvalidPB();
  }
  return self;
}

- (jboolean)mergeDelimitedFromWithJavaIoInputStream:(JavaIoInputStream *)input {
  return [self mergeDelimitedFromWithJavaIoInputStream:input
            withComGoogleProtobufExtensionRegistryLite:nil];
}

- (jboolean)mergeDelimitedFromWithJavaIoInputStream:(JavaIoInputStream *)input
    withComGoogleProtobufExtensionRegistryLite:(CGPExtensionRegistryLite *)extensionRegistry {
  int firstByte = [input read];
  if (firstByte == -1) {
    return false;
  }
  uint32_t length;
  if (!CGPCodedInputStream::ReadVarint32(firstByte, input, &length)) InvalidPB();
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  CGPCodedInputStream codedStream(input, length);
  if (!MergeFromStream(
      self, descriptor, &codedStream, extensionRegistry, BuilderExtensionMap(self, descriptor))
      || !codedStream.ConsumedEntireMessage()) {
    InvalidPB();
  }
  return true;
}

- (id<ComGoogleProtobufMessage_Builder>)
    mergeFromWithComGoogleProtobufByteString:(CGPByteString *)data {
  return [self mergeFromWithComGoogleProtobufByteString:data
             withComGoogleProtobufExtensionRegistryLite:nil];
}

- (id<ComGoogleProtobufMessage_Builder>)
    mergeFromWithComGoogleProtobufByteString:(CGPByteString *)data
    withComGoogleProtobufExtensionRegistryLite:(CGPExtensionRegistryLite *)extensionRegistry {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  CGPCodedInputStream codedStream(data->buffer_, data->size_);
  BOOL success = MergeFromStream(
      self, descriptor, &codedStream, extensionRegistry, BuilderExtensionMap(self, descriptor))
      && codedStream.ConsumedEntireMessage();
  if (!success) {
    InvalidPB();
  }
  return self;
}

- (id<ComGoogleProtobufMessage_Builder>)mergeFromWithByteArray:(IOSByteArray *)data {
  return [self mergeFromWithByteArray:data withComGoogleProtobufExtensionRegistryLite:nil];
}

- (id<ComGoogleProtobufMessage_Builder>)mergeFromWithByteArray:(IOSByteArray *)data
    withComGoogleProtobufExtensionRegistryLite:
        (ComGoogleProtobufExtensionRegistryLite *)extensionRegistry {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  CGPCodedInputStream codedStream(data->buffer_, (int)data->size_);
  BOOL success = MergeFromStream(
      self, descriptor, &codedStream, extensionRegistry, BuilderExtensionMap(self, descriptor))
      && codedStream.ConsumedEntireMessage();
  if (!success) {
    InvalidPB();
  }
  return self;
}

- (void)dealloc {
  Class selfCls = object_getClass(self);
  CGPDescriptor *descriptor = [selfCls getDescriptor];
  ReleaseAllFields(self, selfCls, descriptor);
  [super dealloc];
}

+ (BOOL)resolveInstanceMethod:(SEL)sel {
  if (!class_respondsToSelector(object_getClass(self), @selector(getDescriptor))) {
    return NO;
  }
  ComGoogleProtobufDescriptors_Descriptor *descriptor = [self getDescriptor];
  if (ResolveAccessor(self, descriptor, sel, YES)) {
    return YES;
  }
  const char *name = sel_getName(sel);
  if (memcmp(name, "mergeFromWith", 13) == 0) {
    const char *className = class_getName(descriptor->messageClass_);
    size_t classNameLen = strlen(className);
    if (memcmp(name + 13, className, classNameLen) == 0
        && strcmp(name + classNameLen + 13, ":") == 0) {
      IMP imp = (IMP)DynamicMergeFromMessage;
      return class_addMethod(self, sel, imp, "@#:@");
    }
  }
  return NO;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufGeneratedMessage_Builder)

static id FromReflectionTypeSingular(CGPFieldJavaType type, id value) {
  if (CGPJavaTypeIsEnum(type)) {
    return ((CGPEnumValueDescriptor *)value)->enum_;
  }
  return value;
}

static id FromReflectionType(CGPFieldDescriptor *field, id value) {
  CGPFieldJavaType type = CGPFieldGetJavaType(field);
  if (CGPFieldIsRepeated(field)) {
    if (CGPJavaTypeIsEnum(type)) {
      id<JavaUtilList> valueList = (id<JavaUtilList>)value;
      id<JavaUtilList> result =
          AUTORELEASE([[JavaUtilArrayList alloc] initWithInt:[valueList size]]);
      for (id element in valueList) {
        [result addWithId:FromReflectionTypeSingular(type, element)];
      }
      return result;
    } else {
      return value;
    }
  } else {
    return FromReflectionTypeSingular(type, value);
  }
}

static id ToReflectionTypeSingular(CGPFieldJavaType type, id value) {
  if (CGPJavaTypeIsEnum(type)) {
    return [value getValueDescriptor];
  }
  return value;
}

static id ToReflectionType(CGPFieldDescriptor *field, id value) {
  CGPFieldJavaType type = CGPFieldGetJavaType(field);
  if (CGPFieldIsRepeated(field)) {
    id<JavaUtilList> valueList = (id<JavaUtilList>)value;
    id<JavaUtilList> result =
        AUTORELEASE([[JavaUtilArrayList alloc] initWithInt:[valueList size]]);
    for (id element in valueList) {
      [result addWithId:ToReflectionTypeSingular(type, element)];
    }
    return result;
  } else {
    return ToReflectionTypeSingular(type, value);
  }
}

static id<JavaUtilMap> GetAllFieldsExtendable(id msg, CGPExtensionMap *extensionMap) {
  id<JavaUtilMap> result = GetAllFields(msg);
  for (CGPExtensionMap::iterator it = extensionMap->begin(); it != extensionMap->end(); it++) {
    [result putWithId:it->first withId:it->second.get()];
  }
  return result;
}

static id GetSingularExtension(CGPExtensionMap *extensionMap, ComGoogleProtobufExtensionLite *extension) {
  CGPFieldDescriptor *field = extension->fieldDescriptor_;
  CGPExtensionMap::iterator it = extensionMap->find(field);
  id value;
  if (it != extensionMap->end()) {
    value = it->second.get();
  } else {
    value = CGPFieldGetDefaultValue(field);
  }
  return FromReflectionType(field, value);
}

static id GetRepeatedExtension(
    CGPExtensionMap *extensionMap, ComGoogleProtobufExtensionLite *extension, int index) {
  CGPFieldDescriptor *field = extension->fieldDescriptor_;
  CGPFieldJavaType type = CGPFieldGetJavaType(field);
  CGPExtensionMap::iterator it = extensionMap->find(field);
  id value;
  if (it != extensionMap->end()) {
    value = [((id<JavaUtilList>)it->second.get()) getWithInt:index];
  } else {
    @throw AUTORELEASE([[JavaLangIndexOutOfBoundsException alloc] init]);
  }
  return FromReflectionTypeSingular(type, value);
}

static int GetExtensionCount(ComGoogleProtobufExtensionLite *extension, CGPExtensionMap *extensionMap) {
  CGPFieldDescriptor *field = extension->fieldDescriptor_;
  CGPExtensionMap::iterator it = extensionMap->find(field);
  if (it != extensionMap->end()) {
    id<JavaUtilList> list = it->second.get();
    return [list size];
  }
  return 0;
}

J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleProtobufGeneratedMessage_ExtendableMessageOrBuilder)

@implementation ComGoogleProtobufGeneratedMessage_ExtendableMessage

- (ComGoogleProtobufGeneratedMessage_ExtendableBuilder *)toBuilder {
  ComGoogleProtobufGeneratedMessage_ExtendableBuilder *builder =
      (ComGoogleProtobufGeneratedMessage_ExtendableBuilder *)[super toBuilder];
  builder->extensionMap_ = extensionMap_;
  return builder;
}

- (id)getExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension {
  return GetSingularExtension(&extensionMap_, extension);
}
- (id)getExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension {
  return GetSingularExtension(&extensionMap_, extension);
}
- (id)getExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension {
  return GetSingularExtension(&extensionMap_, extension);
}

- (id)getExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension withInt:(jint)index {
  return GetRepeatedExtension(&extensionMap_, extension, index);
}
- (id)getExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension withInt:(jint)index {
  return GetRepeatedExtension(&extensionMap_, extension, index);
}
- (id)getExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension withInt:(jint)index {
  return GetRepeatedExtension(&extensionMap_, extension, index);
}

- (jint)getExtensionCountWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension {
  return GetExtensionCount(extension, &extensionMap_);
}
- (jint)getExtensionCountWithComGoogleProtobufExtension:(CGPExtension *)extension {
  return GetExtensionCount(extension, &extensionMap_);
}
- (jint)getExtensionCountWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension {
  return GetExtensionCount(extension, &extensionMap_);
}

- (jboolean)hasExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension {
  return extensionMap_.find(extension->fieldDescriptor_) != extensionMap_.end();
}
- (jboolean)hasExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension {
  return extensionMap_.find(extension->fieldDescriptor_) != extensionMap_.end();
}
- (jboolean)hasExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension {
  return extensionMap_.find(extension->fieldDescriptor_) != extensionMap_.end();
}

- (id<JavaUtilMap>)getAllFields {
  return GetAllFieldsExtendable(self, &extensionMap_);
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufGeneratedMessage_ExtendableMessage)

@implementation ComGoogleProtobufGeneratedMessage_ExtendableBuilder

- (id)buildPartial {
  ComGoogleProtobufGeneratedMessage_ExtendableMessage *msg =
      (ComGoogleProtobufGeneratedMessage_ExtendableMessage *)[super buildPartial];
  msg->extensionMap_ = extensionMap_;
  return msg;
}

- (id)getExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension {
  return GetSingularExtension(&extensionMap_, extension);
}
- (id)getExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension {
  return GetSingularExtension(&extensionMap_, extension);
}
- (id)getExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension {
  return GetSingularExtension(&extensionMap_, extension);
}

- (id)getExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension withInt:(jint)index {
  return GetRepeatedExtension(&extensionMap_, extension, index);
}
- (id)getExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension withInt:(jint)index {
  return GetRepeatedExtension(&extensionMap_, extension, index);
}
- (id)getExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension withInt:(jint)index {
  return GetRepeatedExtension(&extensionMap_, extension, index);
}

- (jint)getExtensionCountWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension {
  return GetExtensionCount(extension, &extensionMap_);
}
- (jint)getExtensionCountWithComGoogleProtobufExtension:(CGPExtension *)extension {
  return GetExtensionCount(extension, &extensionMap_);
}
- (jint)getExtensionCountWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension {
  return GetExtensionCount(extension, &extensionMap_);
}

- (jboolean)hasExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension {
  return extensionMap_.find(extension->fieldDescriptor_) != extensionMap_.end();
}
- (jboolean)hasExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension {
  return extensionMap_.find(extension->fieldDescriptor_) != extensionMap_.end();
}
- (jboolean)hasExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension {
  return extensionMap_.find(extension->fieldDescriptor_) != extensionMap_.end();
}

- (id)setExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension withId:(id)value {
  (void)nil_chk(value);
  CGPFieldDescriptor *field = extension->fieldDescriptor_;
  extensionMap_[field].set(ToReflectionType(field, value));
  return self;
}
- (id)setExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension withId:(id)value {
  return [self setExtensionWithComGoogleProtobufExtensionLite:extension withId:value];
}
- (id)setExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension withId:(id)value {
  return [self setExtensionWithComGoogleProtobufExtensionLite:extension withId:value];
}

- (id)addExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension withId:(id)value {
  (void)nil_chk(value);
  CGPFieldDescriptor *field = extension->fieldDescriptor_;
  AddExtensionWithReflectionType(&extensionMap_, field,
      ToReflectionTypeSingular(CGPFieldGetJavaType(field), value));
  return self;
}
- (id)addExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension withId:(id)value {
  return [self addExtensionWithComGoogleProtobufExtensionLite:extension withId:value];
}
- (id)addExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension withId:(id)value {
  return [self addExtensionWithComGoogleProtobufExtensionLite:extension withId:value];
}

- (id)clearExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension {
  extensionMap_.erase(extension->fieldDescriptor_);
  return self;
}
- (id)clearExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension {
  return [self clearExtensionWithComGoogleProtobufExtensionLite:extension];
}
- (id)clearExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension {
  return [self clearExtensionWithComGoogleProtobufExtensionLite:extension];
}

- (id<JavaUtilMap>)getAllFields {
  return GetAllFieldsExtendable(self, &extensionMap_);
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufGeneratedMessage_ExtendableBuilder)

@implementation ComGoogleProtobufGeneratedMessage_GeneratedExtension
@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufGeneratedMessage_GeneratedExtension)

// Define the type literal accessors for all the Message type interfaces here
// because they don't have source files.
J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleProtobufInternal_EnumLite)
J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleProtobufMessage)
J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleProtobufMessage_Builder)
J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleProtobufMessageOrBuilder)
J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleProtobufMessageLite)
J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleProtobufMessageLite_Builder)
J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleProtobufMessageLiteOrBuilder)
J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleProtobufProtocolMessageEnum)
