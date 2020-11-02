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

#include "google/protobuf/compiler/j2objc/j2objc_message.h"

#include <algorithm>
#include <memory>

#include "google/protobuf/compiler/j2objc/j2objc_enum.h"
#include "google/protobuf/compiler/j2objc/j2objc_extension.h"
#include "google/protobuf/compiler/j2objc/j2objc_helpers.h"
#include "google/protobuf/compiler/j2objc/j2objc_oneof.h"

namespace google {
namespace protobuf {
namespace compiler {
namespace j2objc {

namespace {

std::string GetMessageFlags(const Descriptor* descriptor) {
  std::vector<std::string> flags;
  if (descriptor->extension_range_count() > 0) {
    flags.push_back("CGPMessageFlagExtendable");
  }
  if (descriptor->options().message_set_wire_format()) {
    flags.push_back("CGPMessageFlagMessageSetWireFormat");
  }
  return JoinFlags(flags);
}

} // namespace

MessageGenerator::MessageGenerator(const Descriptor* descriptor)
  : descriptor_(descriptor),
  field_generators_(descriptor) {
}

MessageGenerator::~MessageGenerator() {
}

void MessageGenerator::CollectForwardDeclarations(
    std::set<std::string>* declarations) const {
  declarations->insert(
      "J2OBJC_CLASS_DECLARATION(" + ClassName(descriptor_) + ")");
  declarations->insert("@class " + ClassName(descriptor_) + "_Builder");
  declarations->insert(
      "J2OBJC_CLASS_DECLARATION(" + ClassName(descriptor_) + "_Builder)");
  declarations->insert("@class ComGoogleProtobufDescriptors_Descriptor");

  for (int i = 0; i < descriptor_->field_count(); i++) {
    field_generators_.get(descriptor_->field(i))
        .CollectForwardDeclarations(declarations);
  }

  for (int i = 0; i < descriptor_->nested_type_count(); i++) {
    if (IsMapEntry(descriptor_->nested_type(i))) continue;
    MessageGenerator generator(descriptor_->nested_type(i));
    generator.CollectMessageOrBuilderForwardDeclarations(declarations);
    generator.CollectForwardDeclarations(declarations);
  }
}

void MessageGenerator::CollectMessageOrBuilderImports(
    std::set<std::string>* imports) const {
  if (descriptor_->extension_range_count() > 0) {
    imports->insert("com/google/protobuf/GeneratedMessage.h");
  } else {
    imports->insert("com/google/protobuf/MessageOrBuilder.h");
  }

  for (int i = 0; i < descriptor_->field_count(); i++) {
    field_generators_.get(descriptor_->field(i))
        .CollectMessageOrBuilderImports(imports);
  }
}

void MessageGenerator::CollectMessageOrBuilderForwardDeclarations(
    std::set<std::string>* declarations) const {
  for (int i = 0; i < descriptor_->field_count(); i++) {
    field_generators_.get(descriptor_->field(i))
        .CollectMessageOrBuilderForwardDeclarations(declarations);
  }

  for (int i = 0; i < descriptor_->oneof_decl_count(); i++) {
    OneofGenerator(descriptor_->oneof_decl(i))
        .CollectMessageOrBuilderForwardDeclarations(declarations);
  }
}

void MessageGenerator::CollectHeaderImports(
    std::set<std::string>* imports) const {
  for (int i = 0; i < descriptor_->oneof_decl_count(); i++) {
    OneofGenerator(descriptor_->oneof_decl(i)).CollectHeaderImports(imports);
  }

  for (int i = 0; i < descriptor_->nested_type_count(); i++) {
    if (IsMapEntry(descriptor_->nested_type(i))) continue;
    MessageGenerator generator(descriptor_->nested_type(i));
    generator.CollectMessageOrBuilderImports(imports);
    generator.CollectHeaderImports(imports);
  }
}

void MessageGenerator::CollectSourceImports(
    std::set<std::string>* imports) const {
  imports->insert("com/google/protobuf/GeneratedMessage_PackagePrivate.h");

  for (int i = 0; i < descriptor_->field_count(); i++) {
    field_generators_.get(descriptor_->field(i)).CollectSourceImports(imports);
  }

  for (int i = 0; i < descriptor_->oneof_decl_count(); i++) {
    OneofGenerator(descriptor_->oneof_decl(i)).CollectSourceImports(imports);
  }

  for (int i = 0; i < descriptor_->extension_count(); i++) {
    ExtensionGenerator(descriptor_->extension(i)).CollectSourceImports(imports);
  }

  for (int i = 0; i < descriptor_->enum_type_count(); i++) {
    EnumGenerator(descriptor_->enum_type(i)).CollectSourceImports(imports);
  }

  for (int i = 0; i < descriptor_->nested_type_count(); i++) {
    if (IsMapEntry(descriptor_->nested_type(i))) continue;
    MessageGenerator(descriptor_->nested_type(i)).CollectSourceImports(imports);
  }
}

void MessageGenerator::GenerateHeader(io::Printer* printer) {
  std::string superclassName = "ComGoogleProtobufGeneratedMessage";
  if (descriptor_->extension_range_count() > 0) {
    superclassName = "ComGoogleProtobufGeneratedMessage_ExtendableMessage";
  }

  printer->Print("\n"
      "@interface $classname$ : $superclassname$<$classname$OrBuilder>\n\n"
      "+ ($classname$ *)getDefaultInstance;\n"
      "- ($classname$ *)getDefaultInstanceForType;\n"
      "+ ($classname$_Builder *)newBuilder OBJC_METHOD_FAMILY_NONE;\n"
      "- ($classname$_Builder *)newBuilderForType OBJC_METHOD_FAMILY_NONE;\n"
      "- ($classname$_Builder *)toBuilder;\n"
      "+ ($classname$_Builder *)newBuilderWith$classname$:"
          "($classname$ *)message OBJC_METHOD_FAMILY_NONE;\n"
      "+ (ComGoogleProtobufDescriptors_Descriptor *)getDescriptor;\n"
      "+ ($classname$ *)parseFromWithByteArray:(IOSByteArray *)bytes;\n"
      "+ ($classname$ *)parseFromWithByteArray:(IOSByteArray *)bytes "
          "withComGoogleProtobufExtensionRegistryLite:"
          "(ComGoogleProtobufExtensionRegistryLite *)registry;\n"
      "+ ($classname$ *)parseFromNSData:(NSData *)data;\n"
      "+ ($classname$ *)parseFromNSData:(NSData *)data registry:"
          "(ComGoogleProtobufExtensionRegistryLite *)registry;\n"
      "+ ($classname$ *)parseFromWithJavaIoInputStream:"
          "(JavaIoInputStream *)input;\n"
      "+ ($classname$ *)parseFromWithJavaIoInputStream:"
          "(JavaIoInputStream *)bytes "
          "withComGoogleProtobufExtensionRegistryLite:"
          "(ComGoogleProtobufExtensionRegistryLite *)registry;\n"
      "+ ($classname$ *)parseDelimitedFromWithJavaIoInputStream:"
          "(JavaIoInputStream *)input;\n"
      "+ ($classname$ *)parseDelimitedFromWithJavaIoInputStream:"
          "(JavaIoInputStream *)bytes "
          "withComGoogleProtobufExtensionRegistryLite:"
          "(ComGoogleProtobufExtensionRegistryLite *)registry;\n",
      "classname", ClassName(descriptor_),
      "superclassname", superclassName);

  if (descriptor_->field_count() > 0) {
    printer->Print("\n");
  }
  for (int i = 0; i < descriptor_->field_count(); i++) {
    field_generators_.get(descriptor_->field(i)).GenerateFieldHeader(printer);
  }

  printer->Print(
      "\n"
      "@end\n"
      "\n"
      "FOUNDATION_EXPORT $classname$ *$classname$_getDefaultInstance(void);\n"
      "FOUNDATION_EXPORT $classname$_Builder *$classname$_newBuilder(void);\n"
      "FOUNDATION_EXPORT $classname$_Builder *$classname$_newBuilderWith"
      "$classname$_($classname$ *message);\n"
      "FOUNDATION_EXPORT ComGoogleProtobufDescriptors_Descriptor "
      "*$classname$_getDescriptor(void);\n"
      "FOUNDATION_EXPORT $classname$ *$classname$_parseFromWithByteArray_with"
      "ComGoogleProtobufExtensionRegistryLite_(IOSByteArray *bytes, "
      "ComGoogleProtobufExtensionRegistryLite *registry);\n"
      "CGP_ALWAYS_INLINE inline $classname$ *$classname$_"
      "parseFromWithByteArray_(IOSByteArray *bytes) {\n"
      "  return $classname$_parseFromWithByteArray_withComGoogleProtobuf"
      "ExtensionRegistryLite_(bytes, nil);\n"
      "}\n"
      "FOUNDATION_EXPORT $classname$ *$classname$_parseFromWithJavaIo"
      "InputStream_withComGoogleProtobufExtensionRegistryLite_("
      "JavaIoInputStream *input, "
      "ComGoogleProtobufExtensionRegistryLite *registry);\n"
      "CGP_ALWAYS_INLINE inline $classname$ *$classname$_parseFromWith"
      "JavaIoInputStream_(JavaIoInputStream *input) {\n"
      "  return $classname$_parseFromWithJavaIoInputStream_withComGoogle"
      "ProtobufExtensionRegistryLite_(input, nil);\n"
      "}\n"
      "FOUNDATION_EXPORT $classname$ *$classname$_parseDelimitedFromWithJavaIo"
      "InputStream_withComGoogleProtobufExtensionRegistryLite_("
      "JavaIoInputStream *input, "
      "ComGoogleProtobufExtensionRegistryLite *registry);\n"
      "CGP_ALWAYS_INLINE inline $classname$ *$classname$_parseDelimitedFromWith"
      "JavaIoInputStream_(JavaIoInputStream *input) {\n"
      "  return $classname$_parseDelimitedFromWithJavaIoInputStream_"
      "withComGoogleProtobufExtensionRegistryLite_(input, nil);\n"
      "}\n"
      "\n"
      "J2OBJC_STATIC_INIT($classname$)\n"
      "\n"
      "J2OBJC_TYPE_LITERAL_HEADER($classname$)\n"
      "\n"
      "FOUNDATION_EXPORT ComGoogleProtobufDescriptors_Descriptor "
      "*$classname$_descriptor_;\n",
      "classname", ClassName(descriptor_));

  for (int i = 0; i < descriptor_->oneof_decl_count(); i++) {
    OneofGenerator(descriptor_->oneof_decl(i)).GenerateHeader(printer);
  }

  for (int i = 0; i < descriptor_->extension_count(); i++) {
    ExtensionGenerator(descriptor_->extension(i))
        .GenerateMembersHeader(printer);
  }

  for (int i = 0; i < descriptor_->enum_type_count(); i++) {
    EnumGenerator(descriptor_->enum_type(i)).GenerateHeader(printer);
  }

  for (int i = 0; i < descriptor_->nested_type_count(); i++) {
    if (IsMapEntry(descriptor_->nested_type(i))) continue;
    MessageGenerator generator(descriptor_->nested_type(i));
    generator.GenerateMessageOrBuilder(printer);
    generator.GenerateHeader(printer);
  }

  GenerateBuilderHeader(printer);
}

void MessageGenerator::GenerateSource(io::Printer* printer) {
  printer->Print("\n"
      "J2OBJC_INITIALIZED_DEFN($classname$);\n"
      "\n"
      "ComGoogleProtobufDescriptors_Descriptor *$classname$_descriptor_;\n",
      "classname", ClassName(descriptor_));

  for (int i = 0; i < descriptor_->extension_count(); i++) {
    ExtensionGenerator(descriptor_->extension(i))
        .GenerateSourceDefinition(printer);
  }

  printer->Print("\n"
      "@implementation $classname$\n",
      "classname", ClassName(descriptor_));

  printer->Print("\n"
      "typedef struct $classname$_Storage {\n"
      "  uint32_t hasBits[$num_has_bytes$];\n",
      "classname", ClassName(descriptor_),
      "num_has_bytes", SimpleItoa((field_generators_.numHasBits() + 31) / 32));

  printer->Indent();
  for (int i = 0; i < descriptor_->oneof_decl_count(); i++) {
    OneofGenerator(descriptor_->oneof_decl(i))
        .GenerateStorageDeclaration(printer);
  }
  for (int i = 0; i < descriptor_->oneof_decl_count(); i++) {
    const OneofDescriptor* oneof = descriptor_->oneof_decl(i);
    printer->Print("union {\n");
    printer->Indent();
    for (int j = 0; j < oneof->field_count(); j++) {
      field_generators_.get(oneof->field(j)).GenerateDeclaration(printer);
    }
    printer->Outdent();
    printer->Print("};\n");
  }
  for (int i = 0; i < descriptor_->field_count(); i++) {
    const FieldDescriptor* field = descriptor_->field(i);
    if (field->containing_oneof() == NULL) {
      field_generators_.get(field).GenerateDeclaration(printer);
    }
  }
  printer->Outdent();

  printer->Print(
      "} $classname$_Storage;\n"
      "\n"
      "+ (ComGoogleProtobufDescriptors_Descriptor *)getDescriptor {\n"
      "  return $classname$_descriptor_;\n"
      "}\n",
      "classname", ClassName(descriptor_));

  printer->Print("\n"
      "// Minimal metadata for runtime access to Java class name.\n"
      "+ (const J2ObjcClassInfo *)__metadata {\n"
      "  static const J2ObjcClassInfo _$classname$ = { \"$simplename$\", "
          "\"$packagename$\", NULL, NULL, NULL, 7, 0x1, 0, 0, -1, -1, -1, "
          "-1, -1 };\n"
      "  return &_$classname$;\n"
      "}\n",
      "classname", ClassName(descriptor_),
      "simplename", descriptor_->name(),
      "packagename", FileJavaPackage(descriptor_->file()));

  printer->Print("\n"
      "+ (void)initialize {\n"
      "  if (self == [$classname$ class]) {\n",
      "classname", ClassName(descriptor_));
  printer->Indent();
  printer->Indent();
  // The descriptor must be assigned before field data is initialized.
  // Specifically the non-static initialization of field class types may result
  // in an access of this descriptor during its class initialization.
  printer->Print(
      "$classname$_descriptor_ = CGPInitDescriptor(self, "
          "[$classname$_Builder class], $flags$, "
          "sizeof($classname$_Storage));\n",
      "classname", ClassName(descriptor_),
      "flags", GetMessageFlags(descriptor_));
  if (field_generators_.numMapFields() > 0) {
    printer->Print("static CGPFieldData mapEntryFields[] = {\n");
    printer->Indent();
    for (int i = 0; i < descriptor_->field_count(); i++) {
      field_generators_.get(descriptor_->field(i))
          .GenerateMapEntryFieldData(printer);
    }
    printer->Outdent();
    printer->Print("};\n");
  }
  printer->Print("static CGPFieldData fields[] = {\n");
  printer->Indent();
  for (int i = 0; i < descriptor_->field_count(); i++) {
    field_generators_.get(descriptor_->field(i)).GenerateFieldData(printer);
  }
  printer->Outdent();
  printer->Print("};\n");
  if (descriptor_->oneof_decl_count() > 0) {
    printer->Print("static CGPOneofData oneofs[] = {\n");
    printer->Indent();
    for (int i = 0; i < descriptor_->oneof_decl_count(); i++) {
      OneofGenerator(descriptor_->oneof_decl(i)).GenerateOneofData(printer);
    }
    printer->Outdent();
    printer->Print("};\n");
  }
  printer->Print(
      "CGPInitFields($classname$_descriptor_, $fieldcount$, fields, "
          "$oneofcount$, $oneofdata$);\n",
      "classname", ClassName(descriptor_),
      "fieldcount", SimpleItoa(descriptor_->field_count()),
      "oneofcount", SimpleItoa(descriptor_->oneof_decl_count()),
      "oneofdata", descriptor_->oneof_decl_count() > 0 ? "oneofs" : "NULL");

  if (descriptor_->extension_count() > 0) {
    printer->Print("static CGPFieldData extensionFields[] = {\n");
    printer->Indent();
    for (int i = 0; i < descriptor_->extension_count(); i++) {
      ExtensionGenerator(descriptor_->extension(i)).GenerateFieldData(printer);
    }
    printer->Outdent();
    printer->Print("};\n");
    for (int i = 0; i < descriptor_->extension_count(); i++) {
      ExtensionGenerator(descriptor_->extension(i))
          .GenerateSourceInitializer(printer);
    }
  }
  printer->Print(
      "J2OBJC_SET_INITIALIZED($classname$)\n",
      "classname", ClassName(descriptor_));
  printer->Outdent();
  printer->Outdent();
  printer->Print("  }\n}\n");

  printer->Print(
      "\n"
      "@end\n"
      "\n"
      "J2OBJC_CLASS_TYPE_LITERAL_SOURCE($classname$)\n"
      "\n"
      "$classname$ *$classname$_getDefaultInstance(void) {\n"
      "  $classname$_initialize();\n"
      "  return AUTORELEASE("
      "($classname$ *)CGPNewMessage($classname$_descriptor_));\n"
      "}\n"
      "\n"
      "$classname$_Builder *$classname$_newBuilder(void) {\n"
      "  $classname$_initialize();\n"
      "  return AUTORELEASE("
      "($classname$_Builder *)CGPNewBuilder($classname$_descriptor_));\n"
      "}\n"
      "\n"
      "$classname$_Builder *$classname$_newBuilderWith$classname$_("
      "$classname$ *message) {\n"
      "  $classname$_initialize();\n"
      "  return ($classname$_Builder *)CGPBuilderFromPrototype("
      "$classname$_descriptor_, message);\n"
      "}\n"
      "\n"
      "ComGoogleProtobufDescriptors_Descriptor "
      "*$classname$_getDescriptor(void) {\n"
      "  $classname$_initialize();\n"
      "  return $classname$_descriptor_;\n"
      "}\n"
      "\n"
      "$classname$ *$classname$_parseFromWithByteArray_with"
      "ComGoogleProtobufExtensionRegistryLite_(IOSByteArray *bytes, "
      "ComGoogleProtobufExtensionRegistryLite *registry) {\n"
      "  $classname$_initialize();\n"
      "  return ($classname$ *)CGPParseFromByteArray("
      "$classname$_descriptor_, bytes, registry);\n"
      "}\n"
      "\n"
      "$classname$ *$classname$_parseFromWithJavaIoInputStream_withComGoogle"
      "ProtobufExtensionRegistryLite_(JavaIoInputStream *input, "
      "ComGoogleProtobufExtensionRegistryLite *registry) {\n"
      "  $classname$_initialize();\n"
      "  return ($classname$ *)CGPParseFromInputStream("
      "$classname$_descriptor_, input, registry);\n"
      "}\n"
      "$classname$ *$classname$_parseDelimitedFromWithJavaIoInputStream_with"
      "ComGoogleProtobufExtensionRegistryLite_(JavaIoInputStream *input,"
      " ComGoogleProtobufExtensionRegistryLite *registry) {\n"
      "  $classname$_initialize();\n"
      "  return ($classname$ *)CGPParseDelimitedFromInputStream("
      "$classname$_descriptor_, input, registry);\n"
      "}\n",
      "classname", ClassName(descriptor_));

  for (int i = 0; i < descriptor_->oneof_decl_count(); i++) {
    OneofGenerator(descriptor_->oneof_decl(i)).GenerateSource(printer);
  }

  for (int i = 0; i < descriptor_->enum_type_count(); i++) {
    EnumGenerator(descriptor_->enum_type(i)).GenerateSource(printer);
  }

  for (int i = 0; i < descriptor_->nested_type_count(); i++) {
    if (IsMapEntry(descriptor_->nested_type(i))) continue;
    MessageGenerator(descriptor_->nested_type(i)).GenerateSource(printer);
  }

  GenerateBuilderSource(printer);
}

void MessageGenerator::GenerateBuilderHeader(io::Printer* printer) {
  std::string superclassName = "ComGoogleProtobufGeneratedMessage_Builder";
  if (descriptor_->extension_range_count() > 0) {
    superclassName = "ComGoogleProtobufGeneratedMessage_ExtendableBuilder";
  }

  printer->Print("\n"
      "@interface $classname$_Builder : "
          "$superclassname$<$classname$OrBuilder>\n"
      "\n"
      "- ($classname$ *)getDefaultInstanceForType;\n"
      "- ($classname$_Builder *)mergeFromWith$classname$:"
          "($classname$ *)message;\n"
      "- ($classname$_Builder *)mergeFromWithComGoogleProtobufMessage:"
          "(id<ComGoogleProtobufMessage>)message;\n"
      "- ($classname$ *)build;\n"
      "- ($classname$ *)buildPartial;\n"
      "+ (ComGoogleProtobufDescriptors_Descriptor *)getDescriptor;\n",
      "classname", ClassName(descriptor_),
      "superclassname", superclassName);

  for (int i = 0; i < descriptor_->field_count(); i++) {
    field_generators_.get(descriptor_->field(i))
        .GenerateFieldBuilderHeader(printer);
  }

  printer->Print(
      "\n"
      "@end\n\n"
      "FOUNDATION_EXPORT ComGoogleProtobufDescriptors_Descriptor "
      "*$classname$_Builder_getDescriptor(void);\n"
      "\n"
      "J2OBJC_EMPTY_STATIC_INIT($classname$_Builder)\n"
      "\n"
      "J2OBJC_TYPE_LITERAL_HEADER($classname$_Builder)\n",
      "classname", ClassName(descriptor_));
}

void MessageGenerator::GenerateBuilderSource(io::Printer* printer) {
  printer->Print(
      "\n"
      "@implementation $classname$_Builder\n\n"
      "+ (ComGoogleProtobufDescriptors_Descriptor *)getDescriptor {\n"
      "  return [$classname$ getDescriptor];\n"
      "}\n"
      "\n"
      "@end\n"
      "\n"
      "J2OBJC_CLASS_TYPE_LITERAL_SOURCE($classname$_Builder)\n"
      // We don't generate a source file for the "OrBuilder" protocol.
      "J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE($classname$OrBuilder)\n"
      "\n"
      "ComGoogleProtobufDescriptors_Descriptor "
      "*$classname$_Builder_getDescriptor(void) {\n"
      "  $classname$_initialize();\n"
      "  return $classname$_descriptor_;\n"
      "}\n",
      "classname", ClassName(descriptor_));
}

void MessageGenerator::GenerateMessageOrBuilder(io::Printer* printer) {
  std::string protocolName = "ComGoogleProtobufMessageOrBuilder";
  if (descriptor_->extension_range_count() > 0) {
    protocolName =
        "ComGoogleProtobufGeneratedMessage_ExtendableMessageOrBuilder";
  }

  printer->Print("\n"
      "@protocol $classname$OrBuilder < $protocolname$ >\n",
      "classname", ClassName(descriptor_),
      "protocolname", protocolName);

  for (int i = 0; i < descriptor_->field_count(); i++) {
    field_generators_.get(descriptor_->field(i))
        .GenerateMessageOrBuilderProtocol(printer);
  }

  for (int i = 0; i < descriptor_->oneof_decl_count(); i++) {
    OneofGenerator(descriptor_->oneof_decl(i))
        .GenerateMessageOrBuilder(printer);
  }

  printer->Print("\n"
      "@end\n"
      "\n"
      "J2OBJC_EMPTY_STATIC_INIT($classname$OrBuilder)\n"
      "\n"
      "J2OBJC_TYPE_LITERAL_HEADER($classname$OrBuilder)\n",
      "classname", ClassName(descriptor_));
}

void MessageGenerator::GenerateExtensionRegistrationCode(io::Printer* printer) {
  for (int i = 0; i < descriptor_->extension_count(); i++) {
    ExtensionGenerator(descriptor_->extension(i))
        .GenerateRegistrationCode(printer);
  }

  for (int i = 0; i < descriptor_->nested_type_count(); i++) {
    if (IsMapEntry(descriptor_->nested_type(i))) continue;
    MessageGenerator(descriptor_->nested_type(i))
        .GenerateExtensionRegistrationCode(printer);
  }
}

}  // namespace j2objc
}  // namespace compiler
}  // namespace protobuf
}  // namespace google
