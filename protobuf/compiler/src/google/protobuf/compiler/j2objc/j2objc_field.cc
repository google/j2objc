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

// Author: tball@google.com (Tom Ball)
//  Based on original Protocol Buffers design by
//  Sanjay Ghemawat, Jeff Dean, Cyrus Najmabadi, and others.

#include <google/protobuf/compiler/j2objc/j2objc_field.h>

#include <google/protobuf/compiler/j2objc/j2objc_helpers.h>

namespace google {
namespace protobuf {
namespace compiler {
namespace j2objc {

namespace {

std::string GetParameterType(const FieldDescriptor* descriptor) {
  switch (GetJavaType(descriptor)) {
    case JAVATYPE_INT:
      return "Int";
    case JAVATYPE_LONG:
      return "Long";
    case JAVATYPE_FLOAT:
      return "Float";
    case JAVATYPE_DOUBLE:
      return "Double";
    case JAVATYPE_BOOLEAN:
      return "Boolean";
    case JAVATYPE_STRING:
      return "NSString";
    case JAVATYPE_BYTES:
      return "ComGoogleProtobufByteString";
    case JAVATYPE_ENUM:
      return ClassName(descriptor->enum_type());
    case JAVATYPE_MESSAGE:
      return ClassName(descriptor->message_type());
  }
}

std::string GetStorageType(const FieldDescriptor* descriptor) {
  switch (GetJavaType(descriptor)) {
    case JAVATYPE_INT:
      return "jint";
    case JAVATYPE_LONG:
      return "jlong";
    case JAVATYPE_FLOAT:
      return "jfloat";
    case JAVATYPE_DOUBLE:
      return "jdouble";
    case JAVATYPE_BOOLEAN:
      return "jboolean";
    case JAVATYPE_STRING:
      return "NSString *";
    case JAVATYPE_BYTES:
      return "ComGoogleProtobufByteString *";
    case JAVATYPE_ENUM:
      return ClassName(descriptor->enum_type()) + " *";
    case JAVATYPE_MESSAGE:
      return ClassName(descriptor->message_type()) + " *";
  }
}

std::string GetDeclarationSpace(const FieldDescriptor* descriptor) {
  switch (GetJavaType(descriptor)) {
    case JAVATYPE_INT:
    case JAVATYPE_LONG:
    case JAVATYPE_FLOAT:
    case JAVATYPE_DOUBLE:
    case JAVATYPE_BOOLEAN:
      return " ";
    case JAVATYPE_STRING:
    case JAVATYPE_BYTES:
    case JAVATYPE_ENUM:
    case JAVATYPE_MESSAGE:
      return "";
  }
}

std::string GetFieldName(const FieldDescriptor* descriptor) {
  if (descriptor->type() == FieldDescriptor::TYPE_GROUP) {
    return descriptor->message_type()->name();
  } else {
    return descriptor->name();
  }
}

std::string GetListType(const FieldDescriptor* descriptor) {
  if (GetJavaType(descriptor) == JAVATYPE_STRING) {
    return "ComGoogleProtobufProtocolStringList";
  }
  return "JavaUtilList";
}

void SetCommonFieldVariables(const FieldDescriptor* descriptor,
                             std::map<std::string, std::string>* variables) {
  (*variables)["classname"] = ClassName(descriptor->containing_type());
  (*variables)["camelcase_name"] = UnderscoresToCamelCase(descriptor);
  (*variables)["capitalized_name"] =
      UnderscoresToCapitalizedCamelCase(descriptor);
  (*variables)["field_number"] = SimpleItoa(descriptor->number());
  (*variables)["constant_name"] = FieldConstantName(descriptor);
  (*variables)["parameter_type"] = GetParameterType(descriptor);
  (*variables)["storage_type"] = GetStorageType(descriptor);
  (*variables)["decl_space"] = GetDeclarationSpace(descriptor);
  (*variables)["field_name"] = GetFieldName(descriptor);
  (*variables)["flags"] = GetFieldFlags(descriptor);
  (*variables)["field_type"] = GetFieldTypeEnumValue(descriptor);
  (*variables)["default_value_type"] = GetDefaultValueTypeName(descriptor);
  (*variables)["default_value"] = DefaultValue(descriptor);
  (*variables)["has_bit_index"] = "0";
  (*variables)["options_data"] = GetFieldOptionsData(descriptor);
  (*variables)["list_type"] = GetListType(descriptor);
}

void CollectForwardDeclarationsForFieldType(std::set<std::string>* declarations,
                                            const FieldDescriptor* descriptor,
                                            bool includeBuilder) {
  JavaType type = GetJavaType(descriptor);
  if (type == JAVATYPE_BYTES) {
    declarations->insert("@class ComGoogleProtobufByteString");
  } else if (type == JAVATYPE_ENUM) {
    declarations->insert("@class " + ClassName(descriptor->enum_type()));
    declarations->insert("J2OBJC_CLASS_DECLARATION(" +
                         ClassName(descriptor->enum_type()) + ")");
    declarations->insert(
        "FOUNDATION_EXPORT ComGoogleProtobufDescriptors_EnumDescriptor *" +
        ClassName(descriptor->enum_type()) + "_descriptor_");
  } else if (type == JAVATYPE_MESSAGE) {
    std::string classname = ClassName(descriptor->message_type());
    declarations->insert("@class " + classname);
    declarations->insert("J2OBJC_CLASS_DECLARATION(" + classname + ")");
    if (includeBuilder) {
      declarations->insert("@class " + classname + "_Builder");
      declarations->insert("J2OBJC_CLASS_DECLARATION(" + classname +
                           "_Builder)");
    }
  }
}
}  // namespace

void CollectSourceImportsForField(std::set<std::string>* imports,
                                  const FieldDescriptor* descriptor) {
  // Enums and messages have their Class referenced in the field metadata.
  switch (GetJavaType(descriptor)) {
    case JAVATYPE_ENUM:
      imports->insert(GetHeader(descriptor->enum_type()));
      break;
    case JAVATYPE_MESSAGE:
      imports->insert(GetHeader(descriptor->message_type()));
      break;
    default:
      // add nothing.
      break;
  }
}

void GenerateObjcClassRef(
    io::Printer *printer, const FieldDescriptor *descriptor) {
  JavaType type = GetJavaType(descriptor);
  std::string classref;
  if (type == JAVATYPE_ENUM) {
    classref =
        "J2OBJC_CLASS_REFERENCE(" + ClassName(descriptor->enum_type()) + ")";
  } else if (type == JAVATYPE_MESSAGE) {
    classref =
        "J2OBJC_CLASS_REFERENCE(" + ClassName(descriptor->message_type()) + ")";
  } else {
    classref = "NULL";
  }
  printer->Print("  .objcType = $classref$,\n", "classref", classref);
}

FieldGenerator::FieldGenerator(const FieldDescriptor *descriptor)
    : descriptor_(descriptor) {
  SetCommonFieldVariables(descriptor, &variables_);
}

FieldGenerator::~FieldGenerator() {
}

void FieldGenerator::CollectForwardDeclarations(
    std::set<std::string>* declarations) const {
  CollectForwardDeclarationsForFieldType(declarations, descriptor_, true);
}

void FieldGenerator::CollectMessageOrBuilderForwardDeclarations(
    std::set<std::string>* declarations) const {
  CollectForwardDeclarationsForFieldType(declarations, descriptor_, false);
}

void FieldGenerator::CollectSourceImports(
    std::set<std::string>* imports) const {
  // Imports needed for generated metadata of enum and message fields.
  CollectSourceImportsForField(imports, descriptor_);
}

void FieldGenerator::CollectMessageOrBuilderImports(
    std::set<std::string>* imports) const {}

void FieldGenerator::GenerateFieldHeader(io::Printer *printer) const {
  printer->Print(variables_,
      "#define $classname$_$constant_name$ $field_number$\n");
}

void FieldGenerator::GenerateMapEntryFieldData(io::Printer *printer) const {
}

void FieldGenerator::GenerateFieldData(io::Printer *printer) const {
  printer->Print(variables_,
      "{\n"
      "  .name = \"$field_name$\",\n"
      "  .javaName = \"$capitalized_name$\",\n"
      "  .number = $field_number$,\n"
      "  .flags = $flags$,\n"
      "  .type = ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_"
          "$field_type$,\n"
      "  .defaultValue.value$default_value_type$ = $default_value$,\n"
      "  .hasBitIndex = $has_bit_index$,\n"
  );
  GenerateFieldDataOffset(printer);
  GenerateClassNameOrMapData(printer);
  GenerateStaticRefs(printer);
  printer->Print(variables_,
      "  .containingType = NULL,\n"  // Used by extensions.
      "  .optionsData = $options_data$,\n"
      "},\n");
}

void FieldGenerator::GenerateFieldDataOffset(io::Printer *printer) const {
  printer->Print(variables_,
      "  .offset = offsetof($classname$_Storage, $camelcase_name$_),\n");
}

void FieldGenerator::GenerateClassNameOrMapData(io::Printer *printer) const {
  GenerateObjcClassRef(printer, descriptor_);
}

void FieldGenerator::GenerateStaticRefs(io::Printer *printer) const {
  JavaType type = GetJavaType(descriptor_);
  std::string staticref;
  if (type == JAVATYPE_MESSAGE) {
    staticref = "&" +
        GetParameterType(descriptor_) + "_descriptor_";
  } else if (type == JAVATYPE_ENUM) {
    staticref = "&" +
        GetParameterType(descriptor_) + "_descriptor_";
  } else {
    staticref = "NULL";
  }
  printer->Print("  .descriptorRef = $staticref$,\n", "staticref", staticref);
}

SingleFieldGenerator::SingleFieldGenerator(
    const FieldDescriptor *descriptor, uint32_t *numHasBits)
  : FieldGenerator(descriptor) {
  if (descriptor->containing_oneof() == NULL) {
    variables_["has_bit_index"] = SimpleItoa((*numHasBits)++);
  }
}

void SingleFieldGenerator::GenerateFieldBuilderHeader(io::Printer* printer)
    const {
  printer->Print(variables_, "\n"
      "- ($classname$_Builder *)set$capitalized_name$With$parameter_type$:\n"
      "    ($storage_type$)value;\n"
      "- ($classname$_Builder *)clear$capitalized_name$;\n");
  if (GetJavaType(descriptor_) == JAVATYPE_MESSAGE) {
    printer->Print(variables_,
        "- ($classname$_Builder*)\n"
        "    set$capitalized_name$With$parameter_type$_Builder:\n"
        "    ($parameter_type$_Builder *)value;\n");
  }
}

void SingleFieldGenerator::GenerateMessageOrBuilderProtocol(io::Printer* printer)
    const {
  printer->Print(variables_, "\n"
      "- (BOOL)has$capitalized_name$;\n"
      "- ($storage_type$)get$capitalized_name$;\n"
  );
}

void SingleFieldGenerator::GenerateDeclaration(io::Printer* printer) const {
  printer->Print(variables_, "$storage_type$$decl_space$$camelcase_name$_;\n");
}

void RepeatedFieldGenerator::CollectForwardDeclarations(
    std::set<std::string>* declarations) const {
  FieldGenerator::CollectForwardDeclarations(declarations);
  declarations->insert("@protocol JavaLangIterable");
}

void RepeatedFieldGenerator::CollectMessageOrBuilderForwardDeclarations(
    std::set<std::string>* declarations) const {
  FieldGenerator::CollectMessageOrBuilderForwardDeclarations(declarations);
  declarations->insert("@protocol " + GetListType(descriptor_));
}

void RepeatedFieldGenerator::CollectMessageOrBuilderImports(
    std::set<std::string>* imports) const {
  if (GetJavaType(descriptor_) == JAVATYPE_STRING) {
    // When translated against an older Java protobuf runtime, the caller
    // will need the full type info for ProtocolStringList.
    imports->insert("com/google/protobuf/ProtocolStringList.h");
  }
}

void RepeatedFieldGenerator::GenerateFieldBuilderHeader(io::Printer* printer)
    const {
  printer->Print(variables_, "\n"
      "- ($classname$_Builder *)set$capitalized_name$WithInt:(int)index\n"
      "    with$parameter_type$:($storage_type$)value;\n"
      "- ($classname$_Builder *)add$capitalized_name$With$parameter_type$:\n"
      "    ($storage_type$)value;\n"
      "- ($classname$_Builder *)addAll$capitalized_name$WithJavaLangIterable:\n"
      "    (id<JavaLangIterable>)values;\n"
      "- ($classname$_Builder *)clear$capitalized_name$;\n"
  );
  if (GetJavaType(descriptor_) == JAVATYPE_MESSAGE) {
    printer->Print(variables_,
        "- ($classname$_Builder*)\n"
        "    add$capitalized_name$With$parameter_type$_Builder:\n"
        "    ($parameter_type$_Builder *)value;\n"
        "- ($classname$_Builder *)remove$capitalized_name$WithInt:(int)index;\n"
    );
  }
}

void RepeatedFieldGenerator::GenerateMessageOrBuilderProtocol(
    io::Printer* printer) const {
  printer->Print(variables_, "\n"
      "- (jint)get$capitalized_name$Count;\n"
      "- (id<$list_type$>)get$capitalized_name$List;\n"
      "- ($storage_type$)get$capitalized_name$WithInt:(int)index;\n"
  );
}

void RepeatedFieldGenerator::GenerateDeclaration(io::Printer* printer) const {
  printer->Print(variables_, "CGPRepeatedField $camelcase_name$_;\n");
}

MapFieldGenerator::MapFieldGenerator(
    const FieldDescriptor *descriptor, uint32_t map_fields_idx)
    : FieldGenerator(descriptor),
    entry_fields_idx_(map_fields_idx * 2) {
  GOOGLE_CHECK_EQ(FieldDescriptor::TYPE_MESSAGE, descriptor->type());
  const Descriptor* entry_message = descriptor->message_type();
  GOOGLE_CHECK(entry_message->options().map_entry());
  key_field_ = entry_message->FindFieldByName("key");
  value_field_ = entry_message->FindFieldByName("value");

  variables_["key_storage_type"] = GetStorageType(key_field_);
  variables_["key_parameter_type"] = GetParameterType(key_field_);
  variables_["key_descriptor_type"] = GetFieldTypeEnumValue(key_field_);
  variables_["value_storage_type"] = GetStorageType(value_field_);
  variables_["value_parameter_type"] = GetParameterType(value_field_);
  variables_["value_descriptor_type"] = GetFieldTypeEnumValue(value_field_);
  variables_["map_entry_fields_idx"] = SimpleItoa(entry_fields_idx_);
}

void MapFieldGenerator::CollectForwardDeclarations(
    std::set<std::string>* declarations) const {}

void MapFieldGenerator::CollectMessageOrBuilderForwardDeclarations(
    std::set<std::string>* declarations) const {
  CollectForwardDeclarationsForFieldType(declarations, value_field_, false);
  declarations->insert("@protocol JavaUtilMap");
}

void MapFieldGenerator::CollectSourceImports(
    std::set<std::string>* imports) const {
  // Don't call super. Map fields are a special case.
  imports->insert("com/google/protobuf/MapField.h");
  CollectSourceImportsForField(imports, key_field_);
  CollectSourceImportsForField(imports, value_field_);
}

void MapFieldGenerator::GenerateFieldBuilderHeader(io::Printer* printer) const {
  printer->Print(variables_, "\n"
      "- ($classname$_Builder *)clear$capitalized_name$;\n"
      "- ($classname$_Builder *)remove$capitalized_name$With"
          "$key_parameter_type$:($key_storage_type$)key;\n"
      "- ($classname$_Builder *)put$capitalized_name$With$key_parameter_type$:"
          "($key_storage_type$)key with$value_parameter_type$:"
          "($value_storage_type$)value;\n"
  );
}

void MapFieldGenerator::GenerateMessageOrBuilderProtocol(
    io::Printer* printer) const {
  printer->Print(variables_, "\n"
      "- (jint)get$capitalized_name$Count;\n"
      "- (jboolean)contains$capitalized_name$With$key_parameter_type$:"
          "($key_storage_type$)key;\n"
      "- (id<JavaUtilMap>)get$capitalized_name$Map;\n"
      "- ($value_storage_type$)get$capitalized_name$OrDefaultWith"
          "$key_parameter_type$:($key_storage_type$)key "
          "with$value_parameter_type$:($value_storage_type$)defaultValue;\n"
      "- ($value_storage_type$)get$capitalized_name$OrThrowWith"
          "$key_parameter_type$:($key_storage_type$)key;\n"
  );
}

void MapFieldGenerator::GenerateDeclaration(io::Printer* printer) const {
  printer->Print(variables_, "CGPMapField $camelcase_name$_;\n");
}

void MapFieldGenerator::GenerateMapEntryFieldData(io::Printer *printer) const {
  MapEntryFieldGenerator(key_field_).GenerateFieldData(printer);
  MapEntryFieldGenerator(value_field_).GenerateFieldData(printer);
}

void MapFieldGenerator::GenerateClassNameOrMapData(io::Printer *printer) const {
  printer->Print(variables_,
      "  .mapEntryFields = &mapEntryFields[$map_entry_fields_idx$],\n");
}

void MapEntryFieldGenerator::GenerateFieldDataOffset(io::Printer *printer)
    const {
  printer->Print(variables_, "  .offset = 0,\n");
}

void MapFieldGenerator::GenerateStaticRefs(io::Printer *printer) const {
  printer->Print(variables_, "  .descriptorRef = NULL,\n");
}

void MapEntryFieldGenerator::GenerateFieldBuilderHeader(io::Printer* printer)
    const {
}

void MapEntryFieldGenerator::GenerateMessageOrBuilderProtocol(
    io::Printer* printer) const {
}

void MapEntryFieldGenerator::GenerateDeclaration(io::Printer* printer) const {
}

void MapEntryFieldGenerator::GenerateStaticRefs(io::Printer *printer) const {
  printer->Print(variables_, "  .descriptorRef = NULL,\n");
}

FieldGeneratorMap::FieldGeneratorMap(const Descriptor* descriptor)
  : descriptor_(descriptor),
    field_generators_(
        new std::unique_ptr<FieldGenerator>[descriptor->field_count()]),
    numHasBits_(0),
    numMapFields_(0) {

  // Construct all the FieldGenerators.
  for (int i = 0; i < descriptor->field_count(); i++) {
    field_generators_[i].reset(MakeGenerator(descriptor->field(i)));
  }
}

FieldGenerator* FieldGeneratorMap::MakeGenerator(
    const FieldDescriptor* field) {
  // is_repeated() is also true for map fields so test for maps first.
  if (field->is_map()) {
    return new MapFieldGenerator(field, numMapFields_++);
  } else if (field->is_repeated()) {
    return new RepeatedFieldGenerator(field);
  } else {
    return new SingleFieldGenerator(field, &numHasBits_);
  }
}

FieldGeneratorMap::~FieldGeneratorMap() {
}

const FieldGenerator& FieldGeneratorMap::get(
  const FieldDescriptor* field) const {
    GOOGLE_CHECK_EQ(field->containing_type(), descriptor_);
    return *field_generators_[field->index()];
}

}  // namespace j2objc
}  // namespace compiler
}  // namespace protobuf
}  // namespace google
