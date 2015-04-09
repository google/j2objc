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

#include <google/protobuf/compiler/j2objc/j2objc_message.h>

#include <algorithm>
#include <google/protobuf/io/printer.h>
#include <google/protobuf/descriptor.pb.h>
#include <google/protobuf/stubs/strutil.h>
#include <google/protobuf/compiler/j2objc/j2objc_enum.h>
#include <google/protobuf/compiler/j2objc/j2objc_extension.h>
#include <google/protobuf/compiler/j2objc/j2objc_helpers.h>

namespace google {
namespace protobuf {
namespace compiler {
namespace j2objc {

namespace {

  struct FieldOrderingByNumber {
    inline bool operator()(const FieldDescriptor* a,
                           const FieldDescriptor* b) const {
      return a->number() < b->number();
    }
  };

  // Sort the fields of the given Descriptor by number into a new[]'d array
  // and return it.
  const FieldDescriptor** SortFieldsByNumber(const Descriptor* descriptor) {
    const FieldDescriptor** fields =
        new const FieldDescriptor*[descriptor->field_count()];
    for (int i = 0; i < descriptor->field_count(); i++) {
      fields[i] = descriptor->field(i);
    }
    sort(fields, fields + descriptor->field_count(), FieldOrderingByNumber());
    return fields;
  }

  string GetMessageFlags(const Descriptor *descriptor) {
    vector<string> flags;
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

void MessageGenerator::CollectForwardDeclarations(set<string> &declarations)
    const {
  declarations.insert("@class " + ClassName(descriptor_) + "_Builder");
  declarations.insert("@class ComGoogleProtobufDescriptors_Descriptor");

  for (int i = 0; i < descriptor_->field_count(); i++) {
    field_generators_.get(descriptor_->field(i))
        .CollectForwardDeclarations(declarations);
  }

  for (int i = 0; i < descriptor_->nested_type_count(); i++) {
    MessageGenerator(descriptor_->nested_type(i))
        .CollectForwardDeclarations(declarations);
  }
}

void MessageGenerator::CollectMessageOrBuilderImports(set<string> &imports)
    const {
  if (descriptor_->extension_range_count() > 0) {
    imports.insert("com/google/protobuf/GeneratedMessage.h");
  } else {
    imports.insert("com/google/protobuf/MessageOrBuilder.h");
  }
}

void MessageGenerator::CollectMessageOrBuilderForwardDeclarations(
    set<string> &declarations) const {
  for (int i = 0; i < descriptor_->field_count(); i++) {
    field_generators_.get(descriptor_->field(i))
        .CollectMessageOrBuilderForwardDeclarations(declarations);
  }

  for (int i = 0; i < descriptor_->nested_type_count(); i++) {
    MessageGenerator(descriptor_->nested_type(i))
        .CollectMessageOrBuilderForwardDeclarations(declarations);
  }
}

void MessageGenerator::CollectSourceImports(set<string> &imports) {
  imports.insert("com/google/protobuf/GeneratedMessage_PackagePrivate.h");

  for (int i = 0; i < descriptor_->field_count(); i++) {
    field_generators_.get(descriptor_->field(i)).CollectSourceImports(imports);
  }

  for (int i = 0; i < descriptor_->extension_count(); i++) {
    ExtensionGenerator(descriptor_->extension(i)).CollectSourceImports(imports);
  }

  for (int i = 0; i < descriptor_->nested_type_count(); i++) {
    MessageGenerator(descriptor_->nested_type(i)).CollectSourceImports(imports);
  }
}

void MessageGenerator::GenerateEnumHeader(io::Printer* printer) {
  for (int i = 0; i < descriptor_->enum_type_count(); i++) {
    EnumGenerator(descriptor_->enum_type(i)).GenerateHeader(printer);
  }

  for (int i = 0; i < descriptor_->nested_type_count(); i++) {
    MessageGenerator(descriptor_->nested_type(i)).GenerateEnumHeader(printer);
  }
}

void MessageGenerator::GenerateMessageHeader(io::Printer* printer) {
  string superclassName = "ComGoogleProtobufGeneratedMessage";
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
          "(ComGoogleProtobufExtensionRegistryLite *)registry;\n",
      "classname", ClassName(descriptor_),
      "superclassname", superclassName);

  if (descriptor_->field_count() > 0) {
    printer->Print("\n");
  }
  for (int i = 0; i < descriptor_->field_count(); i++) {
    field_generators_.get(descriptor_->field(i)).GenerateFieldHeader(printer);
  }

  printer->Print("\n"
      "@end\n"
      "\n"
      "FOUNDATION_EXPORT $classname$ *$classname$_getDefaultInstance();\n"
      "FOUNDATION_EXPORT $classname$_Builder *$classname$_newBuilder();\n"
      "FOUNDATION_EXPORT $classname$_Builder *$classname$_newBuilderWith"
          "$classname$_($classname$ *message);\n"
      "FOUNDATION_EXPORT ComGoogleProtobufDescriptors_Descriptor "
          "*$classname$_getDescriptor();\n"
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
      "\n"
      "J2OBJC_STATIC_INIT($classname$)\n"
      "\n"
      "J2OBJC_TYPE_LITERAL_HEADER($classname$)\n"
      "\n"
      "FOUNDATION_EXPORT ComGoogleProtobufDescriptors_Descriptor "
          "*$classname$_descriptor_;\n",
      "classname", ClassName(descriptor_));

  for (int i = 0; i < descriptor_->extension_count(); i++) {
    ExtensionGenerator(descriptor_->extension(i))
        .GenerateMembersHeader(printer);
  }

  for (int i = 0; i < descriptor_->nested_type_count(); i++) {
    MessageGenerator(descriptor_->nested_type(i)).GenerateMessageHeader(printer);
  }

  GenerateBuilderHeader(printer);
}

void MessageGenerator::GenerateHeader(io::Printer* printer) {
  GenerateEnumHeader(printer);
  GenerateMessageHeader(printer);
}

void MessageGenerator::GenerateSource(io::Printer* printer) {
  scoped_array<const FieldDescriptor * > sorted_fields(
      SortFieldsByNumber(descriptor_));
  uint32_t singularFieldCount = 0;
  for (int i = 0; i < descriptor_->field_count(); i++) {
    if (!sorted_fields[i]->is_repeated()) {
      singularFieldCount++;
    }
  }

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
      "num_has_bytes", SimpleItoa((singularFieldCount + 31) / 32));

  printer->Indent();
  for (int i = 0; i < descriptor_->field_count(); i++) {
    field_generators_.get(sorted_fields[i]).GenerateDeclaration(printer);
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
      "+ (void)initialize {\n"
      "  if (self == [$classname$ class]) {\n",
      "classname", ClassName(descriptor_));
  printer->Indent();
  printer->Indent();
  printer->Print("static CGPFieldData fields[] = {\n");
  printer->Indent();
  for (int i = 0; i < descriptor_->field_count(); i++) {
    field_generators_.get(sorted_fields[i]).GenerateFieldData(printer);
  }
  printer->Outdent();
  printer->Print(
      "};\n"
      "CGPInitDescriptor(&$classname$_descriptor_, "
          "self, [$classname$_Builder class], $flags$, "
          "sizeof($classname$_Storage), $fieldcount$, fields);\n"
      "",
      "classname", ClassName(descriptor_),
      "flags", GetMessageFlags(descriptor_),
      "fieldcount", SimpleItoa(descriptor_->field_count()));

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

  printer->Print("\n"
      "@end\n"
      "\n"
      "J2OBJC_CLASS_TYPE_LITERAL_SOURCE($classname$)\n"
      "\n"
      "$classname$ *$classname$_getDefaultInstance() {\n"
      "  $classname$_initialize();\n"
      "  return ($classname$ *)[CGPNewMessage($classname$_descriptor_) "
          "autorelease];\n"
      "}\n"
      "\n"
      "$classname$_Builder *$classname$_newBuilder() {\n"
      "  $classname$_initialize();\n"
      "  return ($classname$_Builder *)[CGPNewBuilder($classname$_descriptor_) "
          "autorelease];\n"
      "}\n"
      "\n"
      "$classname$_Builder *$classname$_newBuilderWith$classname$_("
          "$classname$ *message) {\n"
      "  $classname$_initialize();\n"
      "  return ($classname$_Builder *)CGPBuilderFromPrototype("
          "$classname$_descriptor_, message);\n"
      "}\n"
      "\n"
      "ComGoogleProtobufDescriptors_Descriptor *$classname$_getDescriptor() {\n"
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
      "}\n",
      "classname", ClassName(descriptor_));

  for (int i = 0; i < descriptor_->enum_type_count(); i++) {
    EnumGenerator(descriptor_->enum_type(i)).GenerateSource(printer);
  }

  for (int i = 0; i < descriptor_->nested_type_count(); i++) {
    MessageGenerator(descriptor_->nested_type(i)).GenerateSource(printer);
  }

  GenerateBuilderSource(printer);
}

void MessageGenerator::GenerateBuilderHeader(io::Printer* printer) {
  string superclassName = "ComGoogleProtobufGeneratedMessage_Builder";
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

  printer->Print("\n"
      "@end\n\n"
      "FOUNDATION_EXPORT ComGoogleProtobufDescriptors_Descriptor "
          "*$classname$_Builder_getDescriptor();\n"
      "\n"
      "J2OBJC_EMPTY_STATIC_INIT($classname$_Builder)\n"
      "\n"
      "J2OBJC_TYPE_LITERAL_HEADER($classname$_Builder)\n",
      "classname", ClassName(descriptor_));
}

void MessageGenerator::GenerateBuilderSource(io::Printer* printer) {
  printer->Print("\n"
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
          "*$classname$_Builder_getDescriptor() {\n"
      "  $classname$_initialize();\n"
      "  return $classname$_descriptor_;\n"
      "}\n",
      "classname", ClassName(descriptor_));
}

void MessageGenerator::GenerateMessageOrBuilder(io::Printer* printer) {
  string protocolName = "ComGoogleProtobufMessageOrBuilder";
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

  printer->Print("\n"
      "@end\n"
      "\n"
      "J2OBJC_EMPTY_STATIC_INIT($classname$OrBuilder)\n"
      "\n"
      "J2OBJC_TYPE_LITERAL_HEADER($classname$OrBuilder)\n",
      "classname", ClassName(descriptor_));

  for (int i = 0; i < descriptor_->nested_type_count(); i++) {
    MessageGenerator(descriptor_->nested_type(i))
        .GenerateMessageOrBuilder(printer);
  }
}

void MessageGenerator::GenerateExtensionRegistrationCode(io::Printer* printer) {
  for (int i = 0; i < descriptor_->extension_count(); i++) {
    ExtensionGenerator(descriptor_->extension(i))
        .GenerateRegistrationCode(printer);
  }

  for (int i = 0; i < descriptor_->nested_type_count(); i++) {
    MessageGenerator(descriptor_->nested_type(i))
        .GenerateExtensionRegistrationCode(printer);
  }
}

}  // namespace j2objc
}  // namespace compiler
}  // namespace protobuf
}  // namespace google
