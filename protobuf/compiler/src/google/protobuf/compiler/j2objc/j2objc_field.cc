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

#include <google/protobuf/stubs/common.h>
#include <google/protobuf/compiler/j2objc/j2objc_helpers.h>
#include <google/protobuf/io/printer.h>
#include <google/protobuf/stubs/strutil.h>

namespace google {
namespace protobuf {
namespace compiler {
namespace j2objc {

namespace {

  string GetParameterType(const FieldDescriptor *descriptor) {
    switch (GetJavaType(descriptor)) {
      case JAVATYPE_INT: return "Int";
      case JAVATYPE_LONG: return "Long";
      case JAVATYPE_FLOAT: return "Float";
      case JAVATYPE_DOUBLE: return "Double";
      case JAVATYPE_BOOLEAN: return "Boolean";
      case JAVATYPE_STRING: return "NSString";
      case JAVATYPE_BYTES: return "ComGoogleProtobufByteString";
      case JAVATYPE_ENUM: return ClassName(descriptor->enum_type());
      case JAVATYPE_MESSAGE: return ClassName(descriptor->message_type());
    }
  }

  string GetStorageType(const FieldDescriptor *descriptor) {
    switch (GetJavaType(descriptor)) {
      case JAVATYPE_INT: return "int";
      case JAVATYPE_LONG: return "long long int";
      case JAVATYPE_FLOAT: return "float";
      case JAVATYPE_DOUBLE: return "double";
      case JAVATYPE_BOOLEAN: return "BOOL";
      case JAVATYPE_STRING: return "NSString *";
      case JAVATYPE_BYTES: return "ComGoogleProtobufByteString *";
      case JAVATYPE_ENUM:
        return ClassName(descriptor->enum_type()) + " *";
      case JAVATYPE_MESSAGE:
        return ClassName(descriptor->message_type()) + " *";
    }
  }

  string GetDeclarationSpace(const FieldDescriptor *descriptor) {
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

  string GetFieldName(const FieldDescriptor *descriptor) {
    if (descriptor->type() == FieldDescriptor::TYPE_GROUP) {
      return descriptor->message_type()->name();
    } else {
      return descriptor->name();
    }
  }

  int GetHasBitIndex(const FieldDescriptor *descriptor) {
    if (descriptor->is_repeated()) {
      return 0;
    }
    const Descriptor *containing_type = descriptor->containing_type();
    int hasBitIndex = 0;
    for (int i = 0; i < descriptor->index(); i++) {
      if (!containing_type->field(i)->is_repeated()) {
        hasBitIndex++;
      }
    }
    return hasBitIndex;
  }

  string GetListType(const FieldDescriptor *descriptor) {
    if (GetJavaType(descriptor) == JAVATYPE_STRING) {
      return "ComGoogleProtobufProtocolStringList";
    }
    return "JavaUtilList";
  }

  void SetCommonFieldVariables(const FieldDescriptor* descriptor,
      map<string, string>* variables) {
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
    (*variables)["field_data_class_name"] = GetFieldDataClassName(descriptor);
    (*variables)["default_value_type"] = GetDefaultValueTypeName(descriptor);
    (*variables)["default_value"] = DefaultValue(descriptor);
    (*variables)["has_bit_index"] = SimpleItoa(GetHasBitIndex(descriptor));
    (*variables)["containing_type_name"] =
        ClassName(descriptor->containing_type());
    (*variables)["options_data"] = GetFieldOptionsData(descriptor);
    (*variables)["list_type"] = GetListType(descriptor);
  }

  void CollectCommonForwardDeclarations(
      set<string> &declarations, const FieldDescriptor *descriptor) {
    JavaType type = GetJavaType(descriptor);
    if (type == JAVATYPE_BYTES) {
      declarations.insert("@class ComGoogleProtobufByteString");
    } else if (type == JAVATYPE_ENUM) {
      declarations.insert("@class " + ClassName(descriptor->enum_type()));
    } else if (type == JAVATYPE_MESSAGE) {
      string classname = ClassName(descriptor->message_type());
      declarations.insert("@class " + classname);
      declarations.insert("@class " + classname + "_Builder");
    }
  }
}  // namespace

FieldGenerator::FieldGenerator(const FieldDescriptor *descriptor)
    : descriptor_(descriptor) {
  SetCommonFieldVariables(descriptor, &variables_);
}

FieldGenerator::~FieldGenerator() {
}

void FieldGenerator::CollectForwardDeclarations(set<string> &declarations)
    const {
  CollectCommonForwardDeclarations(declarations, descriptor_);
}

void FieldGenerator::CollectMessageOrBuilderForwardDeclarations(
    set<string> &declarations) const {
  CollectCommonForwardDeclarations(declarations, descriptor_);
}

void FieldGenerator::CollectSourceImports(set<string> &imports) const {
}

void FieldGenerator::CollectMessageOrBuilderImports(set<string> &imports) const {
}

void FieldGenerator::GenerateFieldHeader(io::Printer *printer) const {
  printer->Print(variables_,
      "#define $classname$_$constant_name$ $field_number$\n");
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
      "  .offset = offsetof($classname$_Storage, $camelcase_name$_),\n"
      "  .className = $field_data_class_name$,\n"
      "  .containingType = \"$containing_type_name$\",\n"
      "  .optionsData = $options_data$,\n"
      "},\n");
}

void SingleFieldGenerator::CollectSourceImports(set<string> &imports) const {
  FieldGenerator::CollectSourceImports(imports);
  if (GetJavaType(descriptor_) == JAVATYPE_ENUM) {
    imports.insert(GetHeader(descriptor_->enum_type()));
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
    set<string> &declarations) const {
  FieldGenerator::CollectForwardDeclarations(declarations);
  declarations.insert("@protocol JavaLangIterable");
}

void RepeatedFieldGenerator::CollectMessageOrBuilderForwardDeclarations(
    set<string> &declarations) const {
  FieldGenerator::CollectMessageOrBuilderForwardDeclarations(declarations);
  declarations.insert("@protocol " + GetListType(descriptor_));
}

void RepeatedFieldGenerator::CollectMessageOrBuilderImports(set<string> &imports) const {
  if (GetJavaType(descriptor_) == JAVATYPE_STRING) {
    // When translated against an older Java protobuf runtime, the caller
    // will need the full type info for ProtocolStringList.
    imports.insert("com/google/protobuf/ProtocolStringList.h");
  }
}

void RepeatedFieldGenerator::GenerateFieldBuilderHeader(io::Printer* printer)
    const {
  printer->Print(variables_,
      "- ($classname$_Builder*)set$capitalized_name$WithInt:(int)index\n"
      "    with$parameter_type$:($storage_type$)value;\n"
      "- ($classname$_Builder*)add$capitalized_name$With$parameter_type$:\n"
      "    ($storage_type$)value;\n"
      "- ($classname$_Builder*)addAll$capitalized_name$WithJavaLangIterable:\n"
      "    (id<JavaLangIterable>)values;\n"
      "- ($classname$_Builder*)clear$capitalized_name$;\n"
  );
  if (GetJavaType(descriptor_) == JAVATYPE_MESSAGE) {
    printer->Print(variables_,
        "- ($classname$_Builder*)\n"
        "    add$capitalized_name$With$parameter_type$_Builder:\n"
        "    ($parameter_type$_Builder *)value;\n");
  }
}

void RepeatedFieldGenerator::GenerateMessageOrBuilderProtocol(
    io::Printer* printer) const {
  printer->Print(variables_, "\n"
    "- (int)get$capitalized_name$Count;\n"
    "- (id<$list_type$>)get$capitalized_name$List;\n"
    "- ($storage_type$)get$capitalized_name$WithInt:(int)index;\n"
  );
}

void RepeatedFieldGenerator::GenerateDeclaration(io::Printer* printer) const {
  printer->Print(variables_, "CGPRepeatedField $camelcase_name$_;\n");
}

FieldGeneratorMap::FieldGeneratorMap(const Descriptor* descriptor)
  : descriptor_(descriptor),
    field_generators_(
        new scoped_ptr<FieldGenerator>[descriptor->field_count()]) {

  // Construct all the FieldGenerators.
  for (int i = 0; i < descriptor->field_count(); i++) {
    field_generators_[i].reset(MakeGenerator(descriptor->field(i)));
  }
}

FieldGenerator* FieldGeneratorMap::MakeGenerator(
    const FieldDescriptor* field) {
  if (field->is_repeated()) {
    return new RepeatedFieldGenerator(field);
  } else {
    return new SingleFieldGenerator(field);
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
