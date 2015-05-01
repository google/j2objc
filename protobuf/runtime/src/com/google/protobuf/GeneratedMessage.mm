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

#import "com/google/protobuf/GeneratedMessage_PackagePrivate.h"

#include <map>

#import "com/google/protobuf/ByteString.h"
#import "com/google/protobuf/RepeatedField.h"
#import "com/google/protobuf/CodedInputStream.h"
#import "com/google/protobuf/Descriptors_PackagePrivate.h"
#import "com/google/protobuf/ExtensionRegistryLite.h"
#import "com/google/protobuf/InvalidProtocolBufferException.h"
#import "com/google/protobuf/ProtocolMessageEnum.h"
#import "com/google/protobuf/WireFormat.h"
#import "java/io/InputStream.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/IndexOutOfBoundsException.h"
#import "java/lang/StringBuilder.h"
#import "java/lang/UnsupportedOperationException.h"
#import "java/util/ArrayList.h"
#import "java/util/HashMap.h"

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
#define NIL_CHECK_Enum(value) nil_chk(value);
#define NIL_CHECK_Retainable(value) nil_chk(value);

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
      value_ = [other.value_ retain];
    }
  }

  ~CGPExtensionValue() {
    [value_ autorelease];
  }

  id get() { return value_; }

  void set(id value) {
    [value_ autorelease];
    value_ = [value retain];
  }

  void set_retained(id __attribute__((ns_consumed)) retainedValue) {
    [value_ autorelease];
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

typedef struct {
  size_t offset;
  uint32_t mask;
} CGPHasBitLocator;

static CGPHasBitLocator GetHasBitLocator(Class cls, const CGPFieldDescriptor *field) {
  uint32_t idx = CGPFieldGetHasBitIndex(field);
  CGPHasBitLocator result;
  uint32_t byteIndex = idx / 32;
  result.mask = (1 << (idx % 32));
  result.offset = class_getInstanceSize(cls) + byteIndex * sizeof(uint32_t);
  return result;
}

CGP_ALWAYS_INLINE static inline BOOL GetHasBit(id msg, CGPHasBitLocator loc) {
  return (*(uint32_t *)((uint8_t *)msg + loc.offset) & loc.mask) ? YES : NO;
}

CGP_ALWAYS_INLINE static inline void SetHasBit(id msg, CGPHasBitLocator loc) {
  *(uint32_t *)((uint8_t *)msg + loc.offset) |= loc.mask;
}

CGP_ALWAYS_INLINE static inline void UnsetHasBit(id msg, CGPHasBitLocator loc) {
  *(uint32_t *)((uint8_t *)msg + loc.offset) &= ~loc.mask;
}

#define REPEATED_FIELD_PTR(msg, offset) ((CGPRepeatedField *)((uint8_t *)msg + offset))
#define FIELD_PTR(TYPE, msg, offset) ((TYPE *)((uint8_t *)msg + offset))

#define SINGULAR_SETTER_IMP(NAME) \
  static void SingularSet##NAME( \
      id msg, TYPE_##NAME value, size_t offset, CGPHasBitLocator hasLoc) { \
    TYPE_##NAME *ptr = FIELD_PTR(TYPE_##NAME, msg, offset); \
    TYPE_ASSIGN_##NAME(*ptr, value); \
    SetHasBit(msg, hasLoc); \
  }

FOR_EACH_TYPE_WITH_ENUM(SINGULAR_SETTER_IMP)

#undef SINGULAR_SETTER_IMP


// *****************************************************************************
// ********** Dynamic field accessors ******************************************
// *****************************************************************************

#define SINGULAR_GETTER_IMP(NAME) \
  static IMP GetSingularGetterImp##NAME( \
      size_t offset, CGPHasBitLocator hasLoc, TYPE_##NAME defaultValue) { \
    return imp_implementationWithBlock(^TYPE_##NAME(id msg) { \
      if (GetHasBit(msg, hasLoc)) { \
        return *FIELD_PTR(TYPE_##NAME, msg, offset); \
      } \
      return defaultValue; \
    }); \
  }

FOR_EACH_TYPE_NO_ENUM(SINGULAR_GETTER_IMP)

#undef SINGULAR_GETTER_IMP

#define REPEATED_GETTER_IMP(NAME) \
  static IMP GetRepeatedGetterImp##NAME(size_t offset) { \
    return imp_implementationWithBlock(^TYPE_##NAME(id msg, int idx) { \
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
  CGPHasBitLocator hasLoc = GetHasBitLocator(cls, field);

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
  CGPHasBitLocator loc = GetHasBitLocator(cls, field);
  IMP imp = imp_implementationWithBlock(^BOOL(id msg) {
    return GetHasBit(msg, loc);
  });
  char encoding[64];
  strcpy(encoding, @encode(BOOL));
  strcat(encoding, "@:");
  return class_addMethod(cls, sel, imp, encoding);
}

static BOOL AddCountMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  size_t offset = CGPFieldGetOffset(field, cls);
  IMP imp = imp_implementationWithBlock(^int(id msg) {
    return CGPRepeatedFieldSize(REPEATED_FIELD_PTR(msg, offset));
  });
  char encoding[64];
  strcpy(encoding, @encode(int));
  strcat(encoding, "@:");
  return class_addMethod(cls, sel, imp, encoding);
}

static BOOL AddListGetterMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  size_t offset = CGPFieldGetOffset(field, cls);
  CGPFieldJavaType type = CGPFieldGetJavaType(field);
  IMP imp = imp_implementationWithBlock(^id(id msg) {
    return [CGPNewRepeatedFieldList(REPEATED_FIELD_PTR(msg, offset), type) autorelease];
  });
  return class_addMethod(cls, sel, imp, "@@:");
}

static BOOL AddClearMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  IMP imp;
  size_t offset = CGPFieldGetOffset(field, cls);
  CGPFieldJavaType type = CGPFieldGetJavaType(field);
  if (CGPFieldIsRepeated(field)) {
    imp = imp_implementationWithBlock(^id(id msg) {
      CGPRepeatedFieldClear(REPEATED_FIELD_PTR(msg, offset), type);
      return msg;
    });
  } else {
    CGPHasBitLocator hasLoc = GetHasBitLocator(cls, field);
    if (CGPIsRetainedType(type)) {
      imp = imp_implementationWithBlock(^id(id msg) {
        UnsetHasBit(msg, hasLoc);
        id *ptr = FIELD_PTR(id, msg, offset);
        [*ptr autorelease];
        *ptr = nil;
        return msg;
      });
    } else {
      imp = imp_implementationWithBlock(^id(id msg) {
        UnsetHasBit(msg, hasLoc);
        return msg;
      });
    }
  }
  return class_addMethod(cls, sel, imp, "@@:");
}

#define GET_SINGULAR_SETTER_IMP(NAME) \
  static IMP GetSingularSetterImp##NAME(size_t offset, CGPHasBitLocator hasLoc) { \
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
    return imp_implementationWithBlock(^id(id msg, int idx, TYPE_##NAME value) { \
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
      GetSingularSetterImp##NAME(offset, GetHasBitLocator(cls, field)); \
  strcat(encoding, @encode(TYPE_##NAME)); \
  break;

  SWITCH_TYPES_WITH_ENUM(CGPFieldGetJavaType(field), ADD_SETTER_METHOD_CASE)

#undef ADD_SETTER_METHOD_CASE

  return class_addMethod(cls, sel, imp, encoding);
}

static BOOL AddBuilderSetterMethod(Class cls, SEL sel, CGPFieldDescriptor *field) {
  size_t offset = CGPFieldGetOffset(field, cls);
  CGPHasBitLocator hasLoc = GetHasBitLocator(cls, field);
  IMP imp = imp_implementationWithBlock(^id(id msg, id value) {
    nil_chk(value);
    id *ptr = FIELD_PTR(id, msg, offset);
    id builtValue = [(ComGoogleProtobufGeneratedMessage_Builder *)value build];
    [*ptr autorelease];
    *ptr = [builtValue retain];
    SetHasBit(msg, hasLoc);
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
    nil_chk(value);
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

static const char *GetParamKeyword(CGPFieldDescriptor *field) {
  switch (CGPFieldGetJavaType(field)) {
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_INT: return "Int";
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_LONG: return "Long";
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_FLOAT: return "Float";
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_DOUBLE: return "Double";
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_BOOLEAN: return "Boolean";
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_STRING: return "NSString";
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_BYTE_STRING:
      return "ComGoogleProtobufByteString";
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_ENUM: return field->data_->className;
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_MESSAGE:
      return field->data_->className;
  }
  __builtin_unreachable();
}

static BOOL ResolveGetAccessor(Class cls, CGPDescriptor *descriptor, SEL sel, const char *selName) {
  IOSObjectArray *fields = descriptor->fields_;
  ComGoogleProtobufDescriptors_FieldDescriptor **fieldsBuf = fields->buffer_;
  NSUInteger count = fields->size_;
  for (NSUInteger i = 0; i < count; ++i) {
    ComGoogleProtobufDescriptors_FieldDescriptor *field = fieldsBuf[i];
    const char *fieldName = field->data_->javaName;
    size_t nameLen = strlen(fieldName);
    if (strncmp(fieldName, selName, nameLen) != 0) {
      continue;
    }
    const char *tail = selName + nameLen;
    if (CGPFieldIsRepeated(field)) {
      if (strcmp("WithInt:", tail) == 0) {
        return AddGetterMethod(cls, sel, field);
      } else if (strcmp("Count", tail) == 0) {
        return AddCountMethod(cls, sel, field);
      } else if (strcmp("List", tail) == 0) {
        return AddListGetterMethod(cls, sel, field);
      }
    } else {
      if (*tail == 0) {
        return AddGetterMethod(cls, sel, field);
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
    if (!CGPFieldIsRepeated(field) && strcmp(field->data_->javaName, selName) == 0) {
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
    const char *fieldName = field->data_->javaName;
    size_t nameLen = strlen(fieldName);
    if (strncmp(fieldName, selName, nameLen) != 0) {
      continue;
    }
    const char *tail = selName + nameLen;
    BOOL repeated = CGPFieldIsRepeated(field);
    if (repeated) {
      if (strncmp("WithInt:with", tail, 12) != 0) {
        continue;
      }
      tail += 12;
    } else {
      if (strncmp("With", tail, 4) != 0) {
        continue;
      }
      tail += 4;
    }
    const char *paramKeyword = GetParamKeyword(field);
    size_t paramLen = strlen(paramKeyword);
    if (strncmp(paramKeyword, tail, paramLen) != 0) {
      continue;
    }
    tail += paramLen;
    if (strcmp(":", tail) == 0) {
      return AddSetterMethod(cls, sel, field);
    } else if (strcmp("_Builder:", tail) == 0) {
      return AddBuilderSetterMethod(cls, sel, field);
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
    if (strcmp(field->data_->javaName, selName) == 0) {
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
    const char *fieldName = field->data_->javaName;
    size_t nameLen = strlen(fieldName);
    if (strncmp(fieldName, selName, nameLen) != 0) {
      continue;
    }
    const char *tail = selName + nameLen;
    if (strncmp("With", tail, 4) != 0) {
      continue;
    }
    tail += 4;
    const char *paramKeyword = GetParamKeyword(field);
    size_t paramLen = strlen(paramKeyword);
    if (strncmp(paramKeyword, tail, paramLen) != 0) {
      continue;
    }
    tail += paramLen;
    if (strcmp(":", tail) == 0) {
      AddAdderMethod(cls, sel, field);
    } else if (strcmp("_Builder:", tail) == 0) {
      AddBuilderAdderMethod(cls, sel, field);
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
    const char *fieldName = field->data_->javaName;
    size_t nameLen = strlen(fieldName);
    if (strncmp(fieldName, selName, nameLen) != 0) {
      continue;
    }
    const char *tail = selName + nameLen;
    if (strcmp("WithJavaLangIterable:", tail) == 0) {
      AddAddAllMethod(cls, sel, field);
    }
  }
  return NO;
}

static BOOL ResolveAccessor(Class cls, CGPDescriptor *descriptor, SEL sel, BOOL isBuilder) {
  const char *selName = sel_getName(sel);
  if (strncmp("get", selName, 3) == 0) {
    return ResolveGetAccessor(cls, descriptor, sel, selName + 3);
  } else if (strncmp("has", selName, 3) == 0) {
    return ResolveHasAccessor(cls, descriptor, sel, selName + 3);
  }
  if (isBuilder) {
    if (strncmp("set", selName, 3) == 0) {
      return ResolveSetAccessor(cls, descriptor, sel, selName + 3);
    } else if (strncmp("clear", selName, 5) == 0) {
      return ResolveClearAccessor(cls, descriptor, sel, selName + 5);
    } else if (strncmp("add", selName, 3) == 0) {
      if (strncmp("All", selName + 3, 3) == 0
          && ResolveAddAllAccessor(cls, descriptor, sel, selName + 6)) {
        return YES;
      }
      return ResolveAddAccessor(cls, descriptor, sel, selName + 3);
    }
  }
  return NO;
}


// *****************************************************************************
// ********** Reflective field accessors (using descriptors) ******************
// *****************************************************************************

static id GetSingularField(id msg, CGPFieldDescriptor *field) {
  Class msgCls = object_getClass(msg);
  BOOL isSet = GetHasBit(msg, GetHasBitLocator(msgCls, field));
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
  nil_chk(field);
  if (CGPFieldIsRepeated(field)) {
    size_t offset = CGPFieldGetOffset(field, object_getClass(msg));
    return CGPRepeatedFieldCopyList(REPEATED_FIELD_PTR(msg, offset), field);
  } else {
    return GetSingularField(msg, field);
  }
}

static BOOL HasField(id msg, CGPFieldDescriptor *descriptor) {
  if (CGPFieldIsRepeated(nil_chk(descriptor))) {
    @throw AUTORELEASE([[JavaLangUnsupportedOperationException alloc]
        initWithNSString:@"hasField() called on a repeated field."]);
  }
  CGPHasBitLocator hasLoc = GetHasBitLocator(object_getClass(msg), descriptor);
  return GetHasBit(msg, hasLoc);
}

static id<JavaUtilMap> GetAllFields(id msg) {
  id<JavaUtilMap> result = [[JavaUtilHashMap alloc] init];
  Class msgCls = object_getClass(msg);
  CGPDescriptor *descriptor = [msgCls getDescriptor];
  NSUInteger fieldCount = descriptor->fields_->size_;
  CGPFieldDescriptor **fieldsBuf = descriptor->fields_->buffer_;
  for (int i = 0; i < fieldCount; i++) {
    CGPFieldDescriptor *field = fieldsBuf[i];
    if (CGPFieldIsRepeated(field)) {
      size_t offset = CGPFieldGetOffset(field, msgCls);
      CGPRepeatedField *repeatedField = REPEATED_FIELD_PTR(msg, offset);
      if (CGPRepeatedFieldSize(repeatedField) > 0) {
        [result putWithId:field withId:CGPRepeatedFieldCopyList(repeatedField, field)];
      }
    } else {
      if (GetHasBit(msg, GetHasBitLocator(msgCls, field))) {
        [result putWithId:field withId:GetSingularField(msg, field)];
      }
    }
  }
  return [result autorelease];
}

static void CheckIsRepeated(CGPFieldDescriptor *descriptor) {
  if (!CGPFieldIsRepeated(descriptor)) {
    @throw [[[JavaLangUnsupportedOperationException alloc] initWithNSString:
        @"Expected a repeated field."] autorelease];
  }
}

static int GetRepeatedFieldCount(id msg, CGPFieldDescriptor *descriptor) {
  CheckIsRepeated(nil_chk(descriptor));
  size_t offset = CGPFieldGetOffset(descriptor, object_getClass(msg));
  return CGPRepeatedFieldSize(REPEATED_FIELD_PTR(msg, offset));
}

static id GetRepeatedField(id msg, CGPFieldDescriptor *descriptor, int index) {
  CheckIsRepeated(nil_chk(descriptor));
  size_t offset = CGPFieldGetOffset(descriptor, object_getClass(msg));
  CGPRepeatedField *field = REPEATED_FIELD_PTR(msg, offset);
  return CGPRepeatedFieldGet(field, index, descriptor);
}

static void ReleaseAllFields(id self, Class cls, CGPDescriptor *descriptor) {
  CGPFieldDescriptor **fields = descriptor->fields_->buffer_;
  NSUInteger count = descriptor->fields_->size_;
  for (NSUInteger i = 0; i < count; i++) {
    CGPFieldDescriptor *field = fields[i];
    uint8_t *ptr = ((uint8_t *)self + CGPFieldGetOffset(field, cls));
    CGPFieldJavaType javaType = CGPFieldGetJavaType(field);
    if (CGPFieldIsRepeated(field)) {
      CGPRepeatedFieldClear((CGPRepeatedField *)ptr, javaType);
    } else if (CGPIsRetainedType(javaType)) {
      [*(id *)ptr autorelease];
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
    uint8_t *ptr = ((uint8_t *)copy + CGPFieldGetOffset(field, copyCls));
    CGPFieldJavaType javaType = CGPFieldGetJavaType(field);
    if (CGPFieldIsRepeated(field)) {
      CGPRepeatedFieldCopyData((CGPRepeatedField *)ptr, javaType);
    } else if (CGPIsRetainedType(javaType)) {
      [*(id *)ptr retain];
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
    if (CGPFieldIsRepeated(field)) {
      CGPRepeatedField *repeatedField = REPEATED_FIELD_PTR(msg, msgOffset);
      CGPRepeatedField *otherRepeatedField = REPEATED_FIELD_PTR(other, otherOffset);
      if (otherRepeatedField->data != NULL) {
        CGPRepeatedFieldAppendOther(repeatedField, otherRepeatedField, type);
      }
    } else {
      CGPHasBitLocator otherHasLoc = GetHasBitLocator(otherCls, field);
      if (!GetHasBit(other, otherHasLoc)) {
        continue;
      }
      CGPHasBitLocator hasLoc = GetHasBitLocator(msgCls, field);
      if (CGPJavaTypeIsMessage(type) && GetHasBit(msg, hasLoc)) {
        id *fieldPtr = FIELD_PTR(id, msg, msgOffset);
        [*fieldPtr autorelease];
        *fieldPtr = NewMergedMessageField(
            *fieldPtr, *FIELD_PTR(id, other, otherOffset), field->valueType_);
        continue;
      }

#define MERGE_FIELD_CASE(NAME) \
      { \
        TYPE_##NAME *ptr = FIELD_PTR(TYPE_##NAME, msg, msgOffset); \
        TYPE_ASSIGN_##NAME(*ptr, *FIELD_PTR(TYPE_##NAME, other, otherOffset)); \
        break; \
      }

      SWITCH_TYPES_WITH_ENUM(type, MERGE_FIELD_CASE)

#undef MERGE_FIELD_CASE

      SetHasBit(msg, hasLoc);
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
      if (CGPJavaTypeIsMessage(CGPFieldGetJavaType(field)) && value != nil) {
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
  return [builder autorelease];
}


// *****************************************************************************
// ********** Deserializing ****************************************************
// *****************************************************************************

static inline BOOL ReadEnumValueDescriptor(
    CGPCodedInputStream *input, CGPEnumDescriptor *enumType, id *valueDescriptor) {
  int value;
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
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_##ENUM_NAME: \
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
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_ENUM:
      if (!ReadEnumValueDescriptor(stream, field->valueType_, result)) return NO;
      *isRetained = NO;
      return YES;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_BYTES:
      if (!stream->ReadRetainedByteString(result)) return NO;
      *isRetained = YES;
      return YES;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_STRING:
      if (!stream->ReadRetainedNSString(result)) return NO;
      *isRetained = YES;
      return YES;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_GROUP:
      isGroup = YES;
      // fall through.
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_MESSAGE:
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
          [msgField release];
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
          if (isRetained) [value release];
        }
      }
      stream->PopLimit(limit);
    } else {
      if (!MergeExtensionValueFromStream(stream, field, nil, registry, &value, &isRetained))
        return NO;
      if (value != nil) {
        AddExtensionWithReflectionType(extensionMap, field, value);
        if (isRetained) [value release];
      }
    }
  } else {
    id existingValue = nil;
    // For message types we need to keep the fields from the existing message.
    if (CGPJavaTypeIsMessage(CGPFieldGetJavaType(field))) {
      CGPExtensionMap::iterator it = extensionMap->find(field);
      if (it != extensionMap->end()) {
        existingValue = it->second.get();
      }
    }
    if (!MergeExtensionValueFromStream(stream, field, existingValue, registry, &value, &isRetained))
      return NO;
    if (value != nil) {
      (*extensionMap)[field].set_retained(isRetained ? value : [value retain]);
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
          rawBytes = [CGPNewByteString(CGPGetBytesSize(tempBytes)) autorelease];
          CGPCodedOutputStream tempOutput(rawBytes->buffer_, rawBytes->size_);
          CGPWriteBytes(tempBytes, &tempOutput);
          [tempBytes release];
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
    int32_t fieldNumber = CGPWireFormatGetTagFieldNumber(tag);
    CGPFieldDescriptor *field = CGPExtensionRegistryFind(registry, descriptor, fieldNumber);
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
  size_t offset = CGPFieldGetOffset(field, msgCls);
  BOOL repeated = CGPFieldIsRepeated(field);
  BOOL isGroup = NO;
  switch (CGPFieldGetType(field)) {
#define MERGE_FIELD_CASE(NAME, ENUM_NAME, JAVA_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_##ENUM_NAME: \
      if (repeated) { \
        CGPRepeatedField *repeatedField = REPEATED_FIELD_PTR(msg, offset); \
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
        if (!CGPRead##NAME(stream, FIELD_PTR(TYPE_##JAVA_NAME, msg, offset))) return NO; \
        SetHasBit(msg, GetHasBitLocator(msgCls, field)); \
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
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_ENUM:
      {
        CGPEnumDescriptor *enumType = field->valueType_;
        id value;
        if (repeated) {
          CGPRepeatedField *repeatedField = REPEATED_FIELD_PTR(msg, offset);
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
          *FIELD_PTR(id, msg, offset) = value;
          SetHasBit(msg, GetHasBitLocator(msgCls, field));
        }
      }
      return YES;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_BYTES:
      {
        CGPByteString *value;
        if (!stream->ReadRetainedByteString(&value)) return NO;
        if (repeated) {
          CGPRepeatedField *repeatedField = REPEATED_FIELD_PTR(msg, offset);
          CGPRepeatedFieldAddRetainedId(repeatedField, value);
        } else {
          id *ptr = FIELD_PTR(id, msg, offset);
          [*ptr autorelease];
          *ptr = value;
          SetHasBit(msg, GetHasBitLocator(msgCls, field));
        }
      }
      return YES;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_STRING:
      {
        NSString *value;
        if (!stream->ReadRetainedNSString(&value)) return NO;
        if (repeated) {
          CGPRepeatedField *repeatedField = REPEATED_FIELD_PTR(msg, offset);
          CGPRepeatedFieldAddRetainedId(repeatedField, value);
        } else {
          id *ptr = FIELD_PTR(id, msg, offset);
          [*ptr autorelease];
          *ptr = value;
          SetHasBit(msg, GetHasBitLocator(msgCls, field));
        }
      }
      return YES;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_GROUP:
      isGroup = YES;
      // fall through.
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_MESSAGE:
      {
        CGPDescriptor *fieldType = field->valueType_;
        ComGoogleProtobufGeneratedMessage *msgField = CGPNewMessage(fieldType);
        if (repeated) {
          CGPRepeatedFieldAddRetainedId(REPEATED_FIELD_PTR(msg, offset), msgField);
        } else {
          id *ptr = FIELD_PTR(id, msg, offset);
          CGPHasBitLocator hasLoc = GetHasBitLocator(msgCls, field);
          if (GetHasBit(msg, hasLoc)) {
            CopyMessage(msgField, MessageExtensionMap(msgField, fieldType),
                        *ptr, MessageExtensionMap(*ptr, fieldType), fieldType);
          }
          [*ptr autorelease];
          *ptr = msgField;
          SetHasBit(msg, hasLoc);
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
  @throw [[[ComGoogleProtobufInvalidProtocolBufferException alloc] init] autorelease];
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
  ComGoogleProtobufGeneratedMessage *msg = [CGPNewMessage(descriptor) autorelease];
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
  ComGoogleProtobufGeneratedMessage *msg = [CGPNewMessage(descriptor) autorelease];
  CGPCodedInputStream codedStream(input, INT_MAX);
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
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_##ENUM_NAME: \
      return CGPGet##NAME##Size(CGPFromReflectionType##JAVA_NAME(value));
    EXT_SIZE_VARIABLE_LENGTH_CASE(Int32, INT32, Int)
    EXT_SIZE_VARIABLE_LENGTH_CASE(Uint32, UINT32, Int)
    EXT_SIZE_VARIABLE_LENGTH_CASE(Sint32, SINT32, Int)
    EXT_SIZE_VARIABLE_LENGTH_CASE(Int64, INT64, Long)
    EXT_SIZE_VARIABLE_LENGTH_CASE(Int64, UINT64, Long)
    EXT_SIZE_VARIABLE_LENGTH_CASE(Sint64, SINT64, Long)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FLOAT:
      return sizeof(uint32_t);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_DOUBLE:
      return sizeof(uint64_t);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_BOOL:
      return 1;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_ENUM:
      return CGPGetEnumSize(((CGPEnumValueDescriptor *)value)->number_);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_BYTES:
      return CGPGetBytesSize(value);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_STRING:
      return CGPGetStringSize(value);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_GROUP:
      return SerializedSizeForMessage(value, field->valueType_) + CGPGetTagSize(field->tag_);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_MESSAGE:
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
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_##ENUM_NAME: \
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
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FLOAT:
      arraySize = arrayLen * sizeof(uint32_t);
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_DOUBLE:
      arraySize = arrayLen * sizeof(uint64_t);
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_BOOL:
      arraySize = arrayLen;
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_ENUM:
      {
        CGPEnumDescriptor *enumType = field->valueType_;
        id *buffer = (id *)data->buffer;
        for (uint32_t i = 0; i < arrayLen; i++) {
          arraySize += CGPGetEnumSize(CGPEnumGetIntValue(enumType, buffer[i]));
        }
      }
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_BYTES:
      {
        id *buffer = (id *)data->buffer;
        for (uint32_t i = 0; i < arrayLen; i++) {
          arraySize += CGPGetBytesSize(buffer[i]);
        }
      }
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_STRING:
      {
        id *buffer = (id *)data->buffer;
        for (uint32_t i = 0; i < arrayLen; i++) {
          arraySize += CGPGetStringSize(buffer[i]);
        }
      }
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_GROUP:
      {
        id *buffer = (id *)data->buffer;
        for (uint32_t i = 0; i < arrayLen; i++) {
          arraySize += SerializedSizeForMessage(buffer[i], field->valueType_);
        }
        arraySize += arrayLen * tagSize;  // End group tags.
      }
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_MESSAGE:
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
  CGPHasBitLocator hasLoc = GetHasBitLocator(msgCls, field);
  if (!GetHasBit(msg, hasLoc)) {
    return 0;
  }

  int tagSize = CGPGetTagSize(field->tag_);
  size_t offset = CGPFieldGetOffset(field, msgCls);

  switch (CGPFieldGetType(field)) {
#define SINGULAR_FIELD_SIZE_CASE(NAME, ENUM_NAME, JAVA_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_##ENUM_NAME: \
      return tagSize + CGPGet##NAME##Size(*FIELD_PTR(TYPE_##JAVA_NAME, msg, offset));
    SINGULAR_FIELD_SIZE_CASE(Int32, INT32, Int)
    SINGULAR_FIELD_SIZE_CASE(Uint32, UINT32, Int)
    SINGULAR_FIELD_SIZE_CASE(Sint32, SINT32, Int)
    SINGULAR_FIELD_SIZE_CASE(Int64, INT64, Long)
    SINGULAR_FIELD_SIZE_CASE(Int64, UINT64, Long)
    SINGULAR_FIELD_SIZE_CASE(Sint64, SINT64, Long)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FLOAT:
      return tagSize + sizeof(uint32_t);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_DOUBLE:
      return tagSize + sizeof(uint64_t);
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_BOOL:
      return tagSize + 1;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_ENUM:
      return tagSize + CGPGetEnumSize(CGPEnumGetIntValue(
          field->valueType_, *FIELD_PTR(id, msg, offset)));
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_BYTES:
      return tagSize + CGPGetBytesSize(*FIELD_PTR(id, msg, offset));
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_STRING:
      return tagSize + CGPGetStringSize(*FIELD_PTR(id, msg, offset));
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_GROUP:
      {
        int msgSize = SerializedSizeForMessage(*FIELD_PTR(id, msg, offset), field->valueType_);
        return tagSize * 2 + msgSize;
      }
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_MESSAGE:
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
    if (CGPFieldIsRepeated(field)) {
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
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_##ENUM_NAME: \
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
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_ENUM:
      CGPWriteEnum(((CGPEnumValueDescriptor *)value)->number_, output);
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_GROUP:
      WriteMessage(value, field->valueType_, output);
      output->WriteTag(CGPWireFormatMakeTag(CGPFieldGetNumber(field), CGPWireFormatEndGroup));
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_MESSAGE:
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
  CGPHasBitLocator hasLoc = GetHasBitLocator(msgCls, field);
  if (!GetHasBit(msg, hasLoc)) {
    return;
  }
  size_t offset = CGPFieldGetOffset(field, msgCls);
  output->WriteTag(field->tag_);

  switch (CGPFieldGetType(field)) {
#define WRITE_SINGULAR_FIELD_CASE(NAME, ENUM_NAME, JAVA_NAME) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_##ENUM_NAME: \
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
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_ENUM:
      CGPWriteEnum(CGPEnumGetIntValue(field->valueType_, *FIELD_PTR(id, msg, offset)), output);
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_GROUP:
      WriteMessage(*FIELD_PTR(id, msg, offset), field->valueType_, output);
      output->WriteTag(CGPWireFormatMakeTag(CGPFieldGetNumber(field), CGPWireFormatEndGroup));
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_MESSAGE:
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
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_##ENUM_NAME: \
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
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_##ENUM_NAME: \
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
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_ENUM:
      {
        id *buffer = (id *)data->buffer;
        CGPEnumDescriptor *enumType = field->valueType_;
        if (CGPFieldIsPacked(field)) {
          int intValues[arrayLen];
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
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_BYTES:
      {
        id *buffer = (id *)data->buffer;
        for (uint32_t i = 0; i < arrayLen; i++) {
          output->WriteTag(field->tag_);
          CGPWriteBytes(buffer[i], output);
        }
      }
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_STRING:
      {
        id *buffer = (id *)data->buffer;
        for (uint32_t i = 0; i < arrayLen; i++) {
          output->WriteTag(field->tag_);
          CGPWriteString(buffer[i], output);
        }
      }
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_GROUP:
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
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_MESSAGE:
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
  if (CGPFieldIsRepeated(field)) {
    WriteRepeatedField(msg, field, output);
  } else {
    WriteSingularField(msg, field, output);
  }
}

static void WriteMessage(id msg, CGPDescriptor *descriptor, CGPCodedOutputStream *output) {
  NSUInteger fieldsCount = descriptor->fields_->size_;
  CGPFieldDescriptor **fieldsBuf = descriptor->fields_->buffer_;
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
    BOOL isMessage = CGPJavaTypeIsMessage(CGPFieldGetJavaType(field));
    if (CGPFieldIsRepeated(field)) {
      if (isMessage) {
        size_t offset = CGPFieldGetOffset(field, object_getClass(msg));
        CGPRepeatedField *repeatedField = REPEATED_FIELD_PTR(msg, offset);
        CGPRepeatedFieldData *data = repeatedField->data;
        if (data != NULL) {
          uint32_t arraySize = data->size;
          id *arrayBuffer = (id *)data->buffer;
          CGPDescriptor *msgType = field->valueType_;
          for (int i = 0; i < arraySize; i++) {
            if (!MessageIsInitialized(arrayBuffer[i], msgType)) return NO;
          }
        }
      }
    } else {
      BOOL required = CGPFieldIsRequired(field);
      if (!required && !isMessage) continue;
      Class msgCls = object_getClass(msg);
      BOOL hasField = GetHasBit(msg, GetHasBitLocator(msgCls, field));
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
  char *buffer = byteString->buffer_;
  JavaLangStringBuilder *builder = [[[JavaLangStringBuilder alloc] initWithInt:length] autorelease];
  for (int i = 0; i < length; i++) {
    char b = buffer[i];
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
    id value, CGPFieldDescriptor *field, NSMutableString *builder, char *padding, int indent) {
  const char *fieldName = field->data_->name;

  switch (CGPFieldGetType(field)) {
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_INT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SINT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_UINT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_INT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SINT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_UINT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_BOOL:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FLOAT:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_DOUBLE:
      [builder appendFormat:@"%s[%s]: %@\n", padding, fieldName, value];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_ENUM:
      [builder appendFormat:@"%s[%s]: %@\n",
          padding, fieldName, ((CGPEnumValueDescriptor *)value)->enum_];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_BYTES:
      [builder appendFormat:@"%s[%s]: \"%@\"\n", padding, fieldName, BytesToString(value)];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_STRING:
      [builder appendFormat:@"%s[%s]: \"%@\"\n", padding, fieldName, value];
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_GROUP:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_MESSAGE:
      [builder appendFormat:@"%s[%s]: {\n", padding, fieldName];
      MessageToString(value, field->valueType_, builder, indent + 1);
      [builder appendFormat:@"%s}\n", padding];
      return;
  }
}

static void FieldToString(
    id msg, CGPFieldDescriptor *field, NSMutableString *builder, char *padding, int indent) {
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
    if (!GetHasBit(msg, GetHasBitLocator(msgCls, field))) {
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
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_INT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SINT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED32:
      FIELD_TO_STRING_CASE(int32_t, "%d", value)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_UINT32:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED32:
      FIELD_TO_STRING_CASE(uint32_t, "%u", value)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_INT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SINT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED64:
      FIELD_TO_STRING_CASE(int64_t, "%qd", value)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_UINT64:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED64:
      FIELD_TO_STRING_CASE(uint64_t, "%qu", value)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_BOOL:
      FIELD_TO_STRING_CASE(BOOL, "%s", value ? "true" : "false")
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_FLOAT:
      FIELD_TO_STRING_CASE(float, "%g", value)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_DOUBLE:
      FIELD_TO_STRING_CASE(double, "%g", value)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_ENUM:
      FIELD_TO_STRING_CASE(id, "%@", value)
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_BYTES:
      FIELD_TO_STRING_CASE(id, "%@", BytesToString(value))
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_STRING:
      FIELD_TO_STRING_CASE(id, "\"%@\"", value)
      return;
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_GROUP:
    case ComGoogleProtobufDescriptors_FieldDescriptor_Type_MESSAGE:
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
  char padding[paddingSize + 1];
  memset(&padding, ' ', paddingSize);
  padding[paddingSize] = 0;

  NSUInteger fieldsCount = descriptor->fields_->size_;
  CGPFieldDescriptor **fieldsBuf = descriptor->fields_->buffer_;
  for (NSUInteger i = 0; i < fieldsCount; i++) {
    FieldToString(msg, fieldsBuf[i], builder, padding, indent);
  }
  CGPExtensionMap *extensionMap = MessageExtensionMap(msg, descriptor);
  if (extensionMap != NULL) {
    for (CGPExtensionMap::iterator it = extensionMap->begin(); it != extensionMap->end(); it++) {
      CGPFieldDescriptor *field = it->first;
      if (CGPFieldIsRepeated(field)) {
        id<JavaUtilList> list = it->second.get();
        for (id elem in list) {
          ExtensionFieldToString(elem, field, builder, padding, indent);
        }
      } else {
        ExtensionFieldToString(it->second.get(), field, builder, padding, indent);
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
  for (int i = 0; i < count; i++) {
    CGPFieldDescriptor *field = fields[i];
    size_t offset = CGPFieldGetOffset(field, msgCls);
    CGPFieldJavaType type = CGPFieldGetJavaType(field);
    if (CGPFieldIsRepeated(field)) {
      CGPRepeatedField *msgRepeatedField = REPEATED_FIELD_PTR(msg, offset);
      CGPRepeatedField *otherRepeatedField = REPEATED_FIELD_PTR(other, offset);
      if (CGPRepeatedFieldIsEqual(msgRepeatedField, otherRepeatedField, type)) {
        continue;
      } else {
        return NO;
      }
    }
    CGPHasBitLocator hasLoc = GetHasBitLocator(msgCls, field);
    BOOL msgHasField = GetHasBit(msg, hasLoc);
    BOOL otherHasField = GetHasBit(other, hasLoc);
    if (msgHasField != otherHasField) {
      return NO;
    }
    if (msgHasField && !FieldIsEqual(msg, other, offset, type)) {
      return NO;
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

#define HASH_Int(value) value
#define HASH_Long(value) (int)((uint64_t)value ^ ((uint64_t)value >> 32))
#define HASH_Float(value) *(int *)&value
#define HASH_Double(value) (int)(*(uint64_t *)&value ^ (*(uint64_t *)&value >> 32))
#define HASH_Bool(value) (value ? 1231 : 1237)
#define HASH_Id(value) (int)[value hash]

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
    for (int i = 0; i < length; i++) { \
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
  if (!GetHasBit(msg, GetHasBitLocator(msgCls, field))) {
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
  for (int i = 0; i < count; i++) {
    CGPFieldDescriptor *field = fields[i];
    if (CGPFieldIsRepeated(field)) {
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
  return [CGPNewBuilder(descriptor) autorelease];
}

- (ComGoogleProtobufGeneratedMessage_Builder *)newBuilderForType {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  return [CGPNewBuilder(descriptor) autorelease];
}

- (ComGoogleProtobufGeneratedMessage_Builder *)toBuilder {
  Class selfCls = object_getClass(self);
  CGPDescriptor *descriptor = [selfCls getDescriptor];
  ComGoogleProtobufGeneratedMessage_Builder *newBuilder = CGPNewBuilder(descriptor);
  CopyAllFields(self, selfCls, newBuilder, descriptor->builderClass_, descriptor);
  return [newBuilder autorelease];
}

+ (ComGoogleProtobufGeneratedMessage *)getDefaultInstance {
  CGPDescriptor *descriptor = [self getDescriptor];
  return [CGPNewMessage(descriptor) autorelease];
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
  ComGoogleProtobufGeneratedMessage *msg = [CGPNewMessage(descriptor) autorelease];
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

- (int)getSerializedSize {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  return SerializedSizeForMessage(self, descriptor);
}

- (IOSByteArray *)toByteArray {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  int size = [self getSerializedSize];
  IOSByteArray *array = [IOSByteArray arrayWithLength:size];
  CGPCodedOutputStream codedStream(array->buffer_, (int)array->size_);
  WriteMessage(self, descriptor, &codedStream);
  NSAssert(!codedStream.HadError(), @"Serialization error");
  return array;
}

- (CGPByteString *)toByteString {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  int size = [self getSerializedSize];
  CGPByteString *byteString = CGPNewByteString(size);
  CGPCodedOutputStream codedStream(byteString->buffer_, byteString->size_);
  WriteMessage(self, descriptor, &codedStream);
  NSAssert(!codedStream.HadError(), @"Serialization error");
  return [byteString autorelease];
}

- (NSData *)toNSData {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  int size = [self getSerializedSize];
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
  NSAssert(!codedStream.HadError(), @"Serialization error");
}

- (void)writeDelimitedToWithJavaIoOutputStream:(JavaIoOutputStream *)output {
  CGPDescriptor *descriptor = [object_getClass(self) getDescriptor];
  CGPCodedOutputStream codedStream(output);
  CGPWriteInt32(SerializedSizeForMessage(self, descriptor), &codedStream);
  WriteMessage(self, descriptor, &codedStream);
  NSAssert(!codedStream.HadError(), @"Serialization error");
}

- (CGPDescriptor *)getDescriptorForType {
  return [object_getClass(self) getDescriptor];
}

- (id)getFieldWithComGoogleProtobufDescriptors_FieldDescriptor:(CGPFieldDescriptor *)descriptor {
  return GetField(self, descriptor);
}

- (BOOL)hasFieldWithComGoogleProtobufDescriptors_FieldDescriptor:(CGPFieldDescriptor *)descriptor {
  return HasField(self, descriptor);
}

- (int)getRepeatedFieldCountWithComGoogleProtobufDescriptors_FieldDescriptor:
    (CGPFieldDescriptor *)descriptor {
  return GetRepeatedFieldCount(self, descriptor);
}

- (id)getRepeatedFieldWithComGoogleProtobufDescriptors_FieldDescriptor:
    (CGPFieldDescriptor *)descriptor withInt:(int)index {
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
  return [builder autorelease];
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
  return [newMsg autorelease];
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
  return [CGPNewBuilder(descriptor) autorelease];
}

- (id)getFieldWithComGoogleProtobufDescriptors_FieldDescriptor:(CGPFieldDescriptor *)descriptor {
  return GetField(self, descriptor);
}

- (BOOL)hasFieldWithComGoogleProtobufDescriptors_FieldDescriptor:(CGPFieldDescriptor *)descriptor {
  return HasField(self, descriptor);
}

- (int)getRepeatedFieldCountWithComGoogleProtobufDescriptors_FieldDescriptor:
    (CGPFieldDescriptor *)descriptor {
  return GetRepeatedFieldCount(self, descriptor);
}

- (id)getRepeatedFieldWithComGoogleProtobufDescriptors_FieldDescriptor:
    (CGPFieldDescriptor *)descriptor withInt:(int)index {
  return GetRepeatedField(self, descriptor, index);
}

- (id<ComGoogleProtobufMessage_Builder>)
    setFieldWithComGoogleProtobufDescriptors_FieldDescriptor:(CGPFieldDescriptor *)descriptor
                                                      withId:(id)object {
  nil_chk(descriptor);
  nil_chk(object);
  CGPFieldJavaType javaType = CGPFieldGetJavaType(descriptor);
  Class cls = object_getClass(self);
  size_t offset = CGPFieldGetOffset(descriptor, cls);
  if (CGPFieldIsRepeated(descriptor)) {
    CGPRepeatedFieldAssignFromList(REPEATED_FIELD_PTR(self, offset), object, javaType);
  } else {
    CGPHasBitLocator hasLoc = GetHasBitLocator(cls, descriptor);

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
  nil_chk(object);
  size_t offset = CGPFieldGetOffset(descriptor, object_getClass(self));
  CGPRepeatedField *field = REPEATED_FIELD_PTR(self, offset);
  CGPRepeatedFieldAdd(field, object, CGPFieldGetJavaType(descriptor));
  return self;
}

- (id<ComGoogleProtobufMessage_Builder>)
    setRepeatedFieldWithComGoogleProtobufDescriptors_FieldDescriptor:
    (CGPFieldDescriptor *)descriptor withInt:(int)index withId:(id)object {
  nil_chk(object);
  size_t offset = CGPFieldGetOffset(descriptor, object_getClass(self));
  CGPRepeatedField *field = REPEATED_FIELD_PTR(self, offset);
  CGPRepeatedFieldSet(field, index, object, CGPFieldGetJavaType(descriptor));
  return self;
}

- (id<ComGoogleProtobufMessage_Builder>)
    clearFieldWithComGoogleProtobufDescriptors_FieldDescriptor:(CGPFieldDescriptor *)descriptor {
  Class cls = object_getClass(self);
  size_t offset = CGPFieldGetOffset(descriptor, cls);
  if (CGPFieldIsRepeated(descriptor)) {
    CGPRepeatedFieldClear(REPEATED_FIELD_PTR(self, offset), CGPFieldGetJavaType(descriptor));
  } else {
    CGPHasBitLocator hasLoc = GetHasBitLocator(cls, descriptor);
    UnsetHasBit(self, hasLoc);
    if (CGPIsRetainedType(CGPFieldGetJavaType(descriptor))) {
      id *ptr = FIELD_PTR(id, self, offset);
      [*ptr autorelease];
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

- (BOOL)mergeDelimitedFromWithJavaIoInputStream:(JavaIoInputStream *)input {
  return [self mergeDelimitedFromWithJavaIoInputStream:input
            withComGoogleProtobufExtensionRegistryLite:nil];
}

- (BOOL)mergeDelimitedFromWithJavaIoInputStream:(JavaIoInputStream *)input
    withComGoogleProtobufExtensionRegistryLite:(CGPExtensionRegistryLite *)extensionRegistry {
  int firstByte = [input read];
  if (firstByte == -1) {
    return NO;
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
  return YES;
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
          [[[JavaUtilArrayList alloc] initWithInt:[valueList size]] autorelease];
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
        [[[JavaUtilArrayList alloc] initWithInt:[valueList size]] autorelease];
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
    @throw [[[JavaLangIndexOutOfBoundsException alloc] init] autorelease];
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

- (id)getExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension withInt:(int)index {
  return GetRepeatedExtension(&extensionMap_, extension, index);
}

- (int)getExtensionCountWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension {
  return GetExtensionCount(extension, &extensionMap_);
}

- (BOOL)hasExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension {
  return extensionMap_.find(extension->fieldDescriptor_) != extensionMap_.end();
}

// Support older API that accepts Extension instead of ExtensionLite
- (id)getExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension {
  return GetSingularExtension(&extensionMap_, extension);
}

- (id)getExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension withInt:(int)index {
  return GetRepeatedExtension(&extensionMap_, extension, index);
}

- (int)getExtensionCountWithComGoogleProtobufExtension:(CGPExtension *)extension {
  return GetExtensionCount(extension, &extensionMap_);
}

- (BOOL)hasExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension {
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

- (id)getExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension withInt:(int)index {
  return GetRepeatedExtension(&extensionMap_, extension, index);
}

- (int)getExtensionCountWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension {
  return GetExtensionCount(extension, &extensionMap_);
}

- (BOOL)hasExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension {
  return extensionMap_.find(extension->fieldDescriptor_) != extensionMap_.end();
}

- (id)setExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension withId:(id)value {
  nil_chk(value);
  CGPFieldDescriptor *field = extension->fieldDescriptor_;
  extensionMap_[field].set(ToReflectionType(field, value));
  return self;
}

- (id)addExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension withId:(id)value {
  nil_chk(value);
  CGPFieldDescriptor *field = extension->fieldDescriptor_;
  AddExtensionWithReflectionType(&extensionMap_, field,
      ToReflectionTypeSingular(CGPFieldGetJavaType(field), value));
  return self;
}

- (id)clearExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension {
  extensionMap_.erase(extension->fieldDescriptor_);
  return self;
}

// Support older API that accepts Extension instead of ExtensionLite
- (id)getExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension {
  return GetSingularExtension(&extensionMap_, extension);
}

- (id)getExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension withInt:(int)index {
  return GetRepeatedExtension(&extensionMap_, extension, index);
}

- (int)getExtensionCountWithComGoogleProtobufExtension:(CGPExtension *)extension {
  return GetExtensionCount(extension, &extensionMap_);
}

- (BOOL)hasExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension {
  return extensionMap_.find(extension->fieldDescriptor_) != extensionMap_.end();
}

- (id)setExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension withId:(id)value {
  return [self setExtensionWithComGoogleProtobufExtensionLite:extension withId:value];
}

- (id)addExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension withId:(id)value {
  return [self addExtensionWithComGoogleProtobufExtensionLite:extension withId:value];
}

- (id)clearExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension {
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
J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleProtobufMessage)
J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleProtobufMessage_Builder)
J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleProtobufMessageOrBuilder)
J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleProtobufMessageLite)
J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleProtobufMessageLite_Builder)
J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleProtobufMessageLiteOrBuilder)
J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(ComGoogleProtobufProtocolMessageEnum)
