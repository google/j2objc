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

#include <google/protobuf/compiler/j2objc/j2objc_extension.h>

#include <google/protobuf/compiler/j2objc/j2objc_field.h>
#include <google/protobuf/compiler/j2objc/j2objc_helpers.h>

namespace google {
namespace protobuf {
namespace compiler {
namespace j2objc {

namespace {
std::string ContainingClassName(const FieldDescriptor* descriptor) {
  const Descriptor* scope = descriptor->extension_scope();
  if (scope != NULL) {
    return ClassName(scope);
  } else {
    return ClassName(descriptor->file());
  }
}
}  // namespace

ExtensionGenerator::ExtensionGenerator(const FieldDescriptor* descriptor)
  : descriptor_(descriptor) {
}

ExtensionGenerator::~ExtensionGenerator() {
}

void ExtensionGenerator::CollectSourceImports(
    std::set<std::string>* imports) const {
  imports->insert("com/google/protobuf/GeneratedMessage_PackagePrivate.h");
  // Imports needed for metadata of enum and message types.
  CollectSourceImportsForField(imports, descriptor_);
}

void ExtensionGenerator::GenerateMembersHeader(io::Printer* printer) const {
  printer->Print(
      "\n"
      "inline ComGoogleProtobufGeneratedMessage_GeneratedExtension"
      " *$classname$_get_$name$(void);\n"
      "/*! INTERNAL ONLY - Use accessor function from above. */\n"
      "FOUNDATION_EXPORT ComGoogleProtobufGeneratedMessage_GeneratedExtension"
      " *$classname$_$name$;\n"
      "J2OBJC_STATIC_FIELD_OBJ_FINAL($classname$, $name$, "
      "ComGoogleProtobufGeneratedMessage_GeneratedExtension *)\n",
      "name", UnderscoresToCamelCase(descriptor_), "classname",
      ContainingClassName(descriptor_));
}

void ExtensionGenerator::GenerateSourceDefinition(io::Printer* printer) const {
  printer->Print(
      "ComGoogleProtobufGeneratedMessage_GeneratedExtension"
          " *$classname$_$name$;\n",
      "name", UnderscoresToCamelCase(descriptor_),
      "classname", ContainingClassName(descriptor_));
}

void ExtensionGenerator::GenerateFieldData(io::Printer* printer) const {
  std::map<std::string, std::string> vars;
  vars["field_name"] = descriptor_->name();
  vars["capitalized_name"] = UnderscoresToCapitalizedCamelCase(descriptor_);
  vars["field_number"] = SimpleItoa(descriptor_->number());
  vars["flags"] = GetFieldFlags(descriptor_);
  vars["field_type"] = GetFieldTypeEnumValue(descriptor_);
  vars["default_value_type"] = GetDefaultValueTypeName(descriptor_);
  vars["default_value"] = DefaultValue(descriptor_);
  vars["containing_type_name"] = ClassName(descriptor_->containing_type());
  vars["options_data"] = GetFieldOptionsData(descriptor_);
  printer->Print(vars,
      "{\n"
      "  .name = \"$field_name$\",\n"
      "  .javaName = \"$capitalized_name$\",\n"
      "  .number = $field_number$,\n"
      "  .flags = $flags$,\n"
      "  .type = ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_"
          "$field_type$,\n"
      "  .defaultValue.value$default_value_type$ = $default_value$,\n"
      "  .hasBitIndex = 0,\n"
      "  .offset = 0,\n");
  GenerateClassReference(printer);
  printer->Print(vars,
      "  .containingType = \"$containing_type_name$\",\n"
      "  .optionsData = $options_data$,\n"
      "},\n");
}

void ExtensionGenerator::GenerateClassReference(io::Printer *printer) const {
  GenerateObjcClassRef(printer, descriptor_);
}

void ExtensionGenerator::GenerateSourceInitializer(io::Printer* printer) const {
  printer->Print(
      "$classname$_$name$ = "
          "[[ComGoogleProtobufGeneratedMessage_GeneratedExtension alloc] "
          "initWithFieldData:&extensionFields[$num$]];\n",
      "classname", ContainingClassName(descriptor_),
      "name", UnderscoresToCamelCase(descriptor_),
      "num", SimpleItoa(descriptor_->index()));
}

void ExtensionGenerator::GenerateRegistrationCode(io::Printer* printer) const {
  printer->Print(
      "CGPExtensionRegistryAdd(extensionRegistry, $classname$_get_$name$());\n",
      "classname", ContainingClassName(descriptor_),
      "name", UnderscoresToCamelCase(descriptor_));
}

}  // namespace j2objc
}  // namespace compiler
}  // namespace protobuf
}  // namespace google
