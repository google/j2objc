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

// Author: kstanger@google.com (Keith Stanger)
//  Based on original Protocol Buffers design by
//  Sanjay Ghemawat, Jeff Dean, and others.

#include "google/protobuf/compiler/j2objc/j2objc_file.h"

#include <memory>

#include "google/protobuf/compiler/j2objc/j2objc_enum.h"
#include "google/protobuf/compiler/j2objc/j2objc_extension.h"
#include "google/protobuf/compiler/j2objc/j2objc_helpers.h"
#include "google/protobuf/compiler/j2objc/j2objc_message.h"
#include "google/protobuf/compiler/j2objc/j2objc_message_lite.h"

namespace google {
namespace protobuf {
namespace compiler {
namespace j2objc {

namespace {

void AddHeaderImports(std::set<std::string>& imports) {
  imports.insert("J2ObjC_header.h");
  imports.insert("com/google/protobuf/GeneratedMessage.h");
  imports.insert("com/google/protobuf/ProtocolMessageEnum.h");
  imports.insert("java/lang/Enum.h");
}

void AddSourceImports(std::set<std::string>& imports) {
  imports.insert("J2ObjC_source.h");
  imports.insert("com/google/protobuf/RepeatedField.h");
  imports.insert("com/google/protobuf/Descriptors_PackagePrivate.h");
}

void PrintSourcePreamble(io::Printer* printer) {
  printer->Print(
      "\n"
      "#pragma GCC diagnostic ignored \"-Wprotocol\"\n"
      "#pragma clang diagnostic ignored \"-Wprotocol\"\n"
      "#pragma GCC diagnostic ignored \"-Wincomplete-implementation\"\n"
      "#pragma clang diagnostic ignored \"-Wincomplete-implementation\"\n"
      "#pragma clang diagnostic ignored "
      "\"-Wdollar-in-identifier-extension\"\n");
}

void PrintImports(const std::set<std::string>* imports, io::Printer* printer) {
  if (!imports->empty()) {
    printer->Print("\n");
  }
  for (std::set<std::string>::const_iterator it = imports->begin();
       it != imports->end(); it++) {
    printer->Print("#import \"$header$\"\n", "header", *it);
  }
}

void PrintForwardDeclarations(const std::set<std::string>* declarations,
                              io::Printer* printer) {
  if (!declarations->empty()) {
    printer->Print("\n");
  }
  for (std::set<std::string>::const_iterator it = declarations->begin();
       it != declarations->end(); it++) {
    printer->Print("$declaration$;\n", "declaration", *it);
  }
}

}  // namespace

FileGenerator::FileGenerator(const FileDescriptor* file, bool enforce_lite)
    : file_(file),
      classname_(FileClassName(file)),
      enforce_lite_(enforce_lite) {
  if (IsGenerateFileDirMapping()) {
    output_dir_ = FileParentDir(file);
  } else {
    output_dir_ = JavaPackageToDir(FileJavaPackage(file));
  }
}

FileGenerator::~FileGenerator() {}

bool FileGenerator::Validate(std::string* error) {
  // Check that no class name matches the file's class name.  This is a common
  // problem that leads to Java compile errors that can be hard to understand.
  // It's especially bad when using the java_multiple_files, since we would
  // end up overwriting the outer class with one of the inner ones.
  if (HasConflictingClassName(file_, classname_)) {
    error->assign(file_->name());
    error->append(
        ": Cannot generate Java output because the file's outer class name, "
        "\"");
    error->append(classname_);
    error->append(
        "\", matches the name of one of the types declared inside it.  "
        "Please either rename the type or use the java_outer_classname "
        "option to specify a different outer class name for the .proto file.");
    return false;
  }

  return true;
}

void FileGenerator::GenerateBoilerplate(io::Printer* printer) {
  printer->Print(
      "// Generated by the protocol buffer compiler.  DO NOT EDIT!\n"
      "// source: $filename$\n",
      "filename", file_->name());
}

void FileGenerator::GenerateHeaderBoilerplate(io::Printer* printer) {
  GenerateBoilerplate(printer);
}

void FileGenerator::GenerateSourceBoilerplate(io::Printer* printer) {
  GenerateBoilerplate(printer);
}

void FileGenerator::GenerateHeader(GeneratorContext* context) {
  std::string filename = GetFileName(".h");

  std::unique_ptr<io::ZeroCopyOutputStream> output(context->Open(filename));
  io::Printer printer(output.get(), '$');

  GenerateHeaderBoilerplate(&printer);

  std::set<std::string> headers;
  AddHeaderImports(headers);
  std::set<std::string> declarations;
  if (!enforce_lite_) {
    declarations.insert("@class ComGoogleProtobufExtensionRegistry");
  }
  declarations.insert("@class ComGoogleProtobufExtensionRegistryLite");

  if (!GenerateMultipleFiles()) {
    for (int i = 0; i < file_->message_type_count(); i++) {
      std::unique_ptr<MessageGenerator> generator;
      if (enforce_lite_) {
        generator =
            std::make_unique<MessageLiteGenerator>(file_->message_type(i));
      } else {
        generator = std::make_unique<MessageGenerator>(file_->message_type(i));
      }
      generator->CollectMessageOrBuilderImports(&headers);
      generator->CollectHeaderImports(&headers);
      generator->CollectForwardDeclarations(&declarations);
      generator->CollectMessageOrBuilderForwardDeclarations(&declarations);
    }
  }

  PrintImports(&headers, &printer);
  PrintForwardDeclarations(&declarations, &printer);

  printer.Print(
      "\n"
      "#pragma clang diagnostic push\n"
      "#pragma clang diagnostic ignored \"-Wnullability-completeness\""
      "\n"
      "@interface $classname$ : NSObject\n"
      "\n",
      "classname", ClassName(file_));

  if (!enforce_lite_) {
    printer.Print(
        "+ (void)registerAllExtensionsWithComGoogleProtobufExtensionRegistry:"
        "(ComGoogleProtobufExtensionRegistry *)extensionRegistry;\n"
        "\n",
        "classname", ClassName(file_));
  }

  printer.Print(
      "+ (void)registerAllExtensionsWithComGoogleProtobufExtensionRegistryLite:"
      "(ComGoogleProtobufExtensionRegistryLite *)extensionRegistry;\n"
      "\n"
      "@end\n\n",
      "classname", ClassName(file_));

  if (!enforce_lite_) {
    printer.Print(
        "FOUNDATION_EXPORT void $classname$_registerAllExtensionsWith"
        "ComGoogleProtobufExtensionRegistry_("
        "ComGoogleProtobufExtensionRegistry *extensionRegistry);\n"
        "\n",
        "classname", ClassName(file_));
  }

  printer.Print(
      "FOUNDATION_EXPORT void $classname$_registerAllExtensionsWith"
      "ComGoogleProtobufExtensionRegistryLite_("
      "ComGoogleProtobufExtensionRegistryLite *extensionRegistry);\n",
      "classname", ClassName(file_));

  if (file_->extension_count() > 0) {
    printer.Print(
        "\n"
        "J2OBJC_STATIC_INIT($classname$)\n",
        "classname", ClassName(file_));
  } else {
    printer.Print(
        "\n"
        "J2OBJC_EMPTY_STATIC_INIT($classname$)\n",
        "classname", ClassName(file_));
  }

  printer.Print(
      "\n"
      "J2OBJC_TYPE_LITERAL_HEADER($classname$)\n",
      "classname", ClassName(file_));

  for (int i = 0; i < file_->extension_count(); i++) {
    ExtensionGenerator(file_->extension(i)).GenerateMembersHeader(&printer);
  }

  if (!GenerateMultipleFiles()) {
    for (int i = 0; i < file_->enum_type_count(); i++) {
      EnumGenerator(file_->enum_type(i)).GenerateHeader(&printer);
    }

    for (int i = 0; i < file_->message_type_count(); i++) {
      std::unique_ptr<MessageGenerator> generator;
      if (enforce_lite_) {
        generator =
            std::make_unique<MessageLiteGenerator>(file_->message_type(i));
      } else {
        generator = std::make_unique<MessageGenerator>(file_->message_type(i));
      }
      generator->GenerateMessageOrBuilder(&printer);
      generator->GenerateHeader(&printer);
    }
  }
  printer.Print(
      "\n"
      "#pragma clang diagnostic pop\n");
}

void FileGenerator::GenerateSource(GeneratorContext* context) {
  std::string filename = GetFileName(".m");

  std::unique_ptr<io::ZeroCopyOutputStream> output(context->Open(filename));
  io::Printer printer(output.get(), '$');

  GenerateSourceBoilerplate(&printer);

  std::set<std::string> headers;
  AddSourceImports(headers);
  headers.insert(GetFileName(".h"));
  if (!enforce_lite_) {
    headers.insert("com/google/protobuf/ExtensionRegistry.h");
  }
  headers.insert("com/google/protobuf/ExtensionRegistryLite.h");
  if (GenerateMultipleFiles()) {
    for (int i = 0; i < file_->message_type_count(); i++) {
      headers.insert(GetHeader(file_->message_type(i)));
    }
  } else {
    for (int i = 0; i < file_->message_type_count(); i++) {
      if (enforce_lite_) {
        MessageLiteGenerator(file_->message_type(i))
            .CollectSourceImports(&headers);
      } else {
        MessageGenerator(file_->message_type(i)).CollectSourceImports(&headers);
      }
    }
    for (int i = 0; i < file_->enum_type_count(); i++) {
      EnumGenerator(file_->enum_type(i)).CollectSourceImports(&headers);
    }
  }
  for (int i = 0; i < file_->extension_count(); i++) {
    ExtensionGenerator(file_->extension(i)).CollectSourceImports(&headers);
  }
  PrintImports(&headers, &printer);
  PrintSourcePreamble(&printer);

  if (file_->extension_count() > 0) {
    printer.Print("\nJ2OBJC_INITIALIZED_DEFN($classname$)\n", "classname",
                  ClassName(file_));
  }
  for (int i = 0; i < file_->extension_count(); i++) {
    ExtensionGenerator(file_->extension(i)).GenerateSourceDefinition(&printer);
  }

  printer.Print(
      "\n"
      "@implementation $classname$\n"
      "\n",
      "classname", ClassName(file_));

  if (!enforce_lite_) {
    printer.Print(
        "+ (void)registerAllExtensionsWithComGoogleProtobufExtensionRegistry:"
        "(ComGoogleProtobufExtensionRegistry *)extensionRegistry {\n"
        "  $classname$_registerAllExtensionsWithComGoogleProtobuf"
        "ExtensionRegistry_(extensionRegistry);\n"
        "}\n"
        "\n",
        "classname", ClassName(file_));
  }

  printer.Print(
      "+ (void)registerAllExtensionsWithComGoogleProtobufExtensionRegistryLite:"
      "(ComGoogleProtobufExtensionRegistryLite *)extensionRegistry {\n"
      "  $classname$_registerAllExtensionsWithComGoogleProtobuf"
      "ExtensionRegistryLite_(extensionRegistry);\n"
      "}\n",
      "classname", ClassName(file_));

  if (file_->extension_count() > 0) {
    printer.Print(
        "\n"
        "+ (void)initialize {\n"
        "  if (self == [$classname$ class]) {\n"
        "    static CGPFieldData extensionFields[] = {\n",
        "classname", ClassName(file_));
    printer.Indent();
    printer.Indent();
    printer.Indent();
    for (int i = 0; i < file_->extension_count(); i++) {
      ExtensionGenerator(file_->extension(i)).GenerateFieldData(&printer);
    }
    printer.Outdent();
    printer.Print("};\n");
    for (int i = 0; i < file_->extension_count(); i++) {
      ExtensionGenerator(file_->extension(i))
          .GenerateSourceInitializer(&printer);
    }
    printer.Print("J2OBJC_SET_INITIALIZED($classname$)\n", "classname",
                  ClassName(file_));
    printer.Outdent();
    printer.Outdent();
    printer.Print("  }\n}\n");
  }

  printer.Print(
      "\n"
      "@end\n"
      "\n"
      "J2OBJC_CLASS_TYPE_LITERAL_SOURCE($classname$)\n"
      "\n",
      "classname", ClassName(file_));

  if (!enforce_lite_) {
    printer.Print(
        "void $classname$_registerAllExtensionsWith"
        "ComGoogleProtobufExtensionRegistry_("
        "ComGoogleProtobufExtensionRegistry *extensionRegistry) {\n"
        "  $classname$_registerAllExtensionsWith"
        "ComGoogleProtobufExtensionRegistryLite_(extensionRegistry);\n"
        "}\n"
        "\n",
        "classname", ClassName(file_));
  }

  printer.Print(
      "void $classname$_registerAllExtensionsWith"
      "ComGoogleProtobufExtensionRegistryLite_("
      "ComGoogleProtobufExtensionRegistryLite *extensionRegistry) {\n",
      "classname", ClassName(file_));
  printer.Indent();
  for (int i = 0; i < file_->extension_count(); i++) {
    ExtensionGenerator(file_->extension(i)).GenerateRegistrationCode(&printer);
  }
  for (int i = 0; i < file_->message_type_count(); i++) {
    if (enforce_lite_) {
      MessageLiteGenerator(file_->message_type(i))
          .GenerateExtensionRegistrationCode(&printer);
    } else {
      MessageGenerator(file_->message_type(i))
          .GenerateExtensionRegistrationCode(&printer);
    }
  }
  printer.Outdent();
  printer.Print("}\n");

  if (!GenerateMultipleFiles()) {
    for (int i = 0; i < file_->enum_type_count(); i++) {
      EnumGenerator(file_->enum_type(i)).GenerateSource(&printer);
    }
    for (int i = 0; i < file_->message_type_count(); i++) {
      if (enforce_lite_) {
        MessageLiteGenerator(file_->message_type(i)).GenerateSource(&printer);
      } else {
        MessageGenerator(file_->message_type(i)).GenerateSource(&printer);
      }
    }
  }
}

std::string FileGenerator::GetFileName(std::string suffix) {
  if (IsGenerateFileDirMapping()) {
    return StaticOutputFileName(file_, suffix);
  } else {
    return output_dir_ + classname_ + suffix;
  }
}

void FileGenerator::Generate(GeneratorContext* context) {
  GenerateHeader(context);
  GenerateSource(context);
}

void FileGenerator::GenerateEnumHeader(GeneratorContext* context,
                                       const EnumDescriptor* descriptor) {
  std::string filename = absl::StrCat(output_dir_, descriptor->name(), ".h");
  std::unique_ptr<io::ZeroCopyOutputStream> output(context->Open(filename));
  io::Printer printer(output.get(), '$');

  GenerateBoilerplate(&printer);
  std::set<std::string> headers;
  AddHeaderImports(headers);
  PrintImports(&headers, &printer);

  EnumGenerator generator(descriptor);
  generator.GenerateHeader(&printer);
}

void FileGenerator::GenerateEnumSource(GeneratorContext* context,
                                       const EnumDescriptor* descriptor) {
  std::string filename = absl::StrCat(output_dir_, descriptor->name(), ".m");
  std::unique_ptr<io::ZeroCopyOutputStream> output(context->Open(filename));
  io::Printer printer(output.get(), '$');

  GenerateBoilerplate(&printer);

  EnumGenerator generator(descriptor);
  std::set<std::string> headers;
  headers.insert(absl::StrCat(output_dir_, descriptor->name(), ".h"));
  AddSourceImports(headers);
  generator.CollectSourceImports(&headers);
  PrintImports(&headers, &printer);

  generator.GenerateSource(&printer);
}

void FileGenerator::GenerateMessageHeader(GeneratorContext* context,
                                          const Descriptor* descriptor) {
  std::string filename = absl::StrCat(output_dir_, descriptor->name(), ".h");
  std::unique_ptr<io::ZeroCopyOutputStream> output(context->Open(filename));
  io::Printer printer(output.get(), '$');

  GenerateBoilerplate(&printer);

  MessageGenerator generator(descriptor);
  std::set<std::string> headers;
  headers.insert(absl::StrCat(output_dir_, descriptor->name(), "OrBuilder.h"));
  AddHeaderImports(headers);
  generator.CollectHeaderImports(&headers);
  PrintImports(&headers, &printer);

  std::set<std::string> declarations;
  generator.CollectForwardDeclarations(&declarations);
  PrintForwardDeclarations(&declarations, &printer);
  generator.GenerateHeader(&printer);
}

void FileGenerator::GenerateMessageSource(GeneratorContext* context,
                                          const Descriptor* descriptor) {
  std::string filename = absl::StrCat(output_dir_, descriptor->name(), ".m");
  std::unique_ptr<io::ZeroCopyOutputStream> output(context->Open(filename));
  io::Printer printer(output.get(), '$');

  GenerateBoilerplate(&printer);

  MessageGenerator generator(descriptor);
  std::set<std::string> headers;
  headers.insert(absl::StrCat(output_dir_, descriptor->name(), ".h"));
  generator.CollectSourceImports(&headers);
  AddSourceImports(headers);
  PrintImports(&headers, &printer);
  PrintSourcePreamble(&printer);
  generator.GenerateSource(&printer);
}

void FileGenerator::GenerateMessageOrBuilder(
    GeneratorContext* context,
    const Descriptor* descriptor) {
  std::string filename = absl::StrCat(output_dir_, descriptor->name(),
                                      "OrBuilder.h");
  std::unique_ptr<io::ZeroCopyOutputStream> output(context->Open(filename));
  io::Printer printer(output.get(), '$');

  GenerateBoilerplate(&printer);
  std::unique_ptr<MessageGenerator> generator;
  if (enforce_lite_) {
    generator = std::make_unique<MessageLiteGenerator>(descriptor);
  } else {
    generator = std::make_unique<MessageGenerator>(descriptor);
  }

  std::set<std::string> headers;
  generator->CollectMessageOrBuilderImports(&headers);
  PrintImports(&headers, &printer);

  std::set<std::string> declarations;
  generator->CollectMessageOrBuilderForwardDeclarations(&declarations);
  PrintForwardDeclarations(&declarations, &printer);
  generator->GenerateMessageOrBuilder(&printer);
}

void FileGenerator::GenerateSiblings(GeneratorContext* context) {
  if (GenerateMultipleFiles()) {
    for (int i = 0; i < file_->enum_type_count(); i++) {
      GenerateEnumHeader(context, file_->enum_type(i));
      GenerateEnumSource(context, file_->enum_type(i));
    }
    for (int i = 0; i < file_->message_type_count(); i++) {
      GenerateMessageHeader(context, file_->message_type(i));
      GenerateMessageSource(context, file_->message_type(i));
      GenerateMessageOrBuilder(context, file_->message_type(i));
    }
  }
}

bool FileGenerator::GenerateMultipleFiles() {
  return file_->options().java_multiple_files() && !IsGenerateFileDirMapping();
}

void PrintProperty(io::Printer* printer, const std::string& key,
                   const std::string& value) {
  printer->Print("$key$=$value$\n", "key", key, "value", value);
}

void FileGenerator::GenerateHeaderMappings(GeneratorContext* context) {
  std::string headerFile = StaticOutputFileName(file_, ".h");
  std::unique_ptr<io::ZeroCopyOutputStream> output(
      context->Open(FileDirMappingOutputName(file_)));
  io::Printer printer(output.get(), '$');

  for (int i = 0; i < file_->enum_type_count(); i++) {
    PrintProperty(&printer, JavaClassName(file_->enum_type(i)), headerFile);
  }

  for (int i = 0; i < file_->message_type_count(); i++) {
    std::string messageClassName = JavaClassName(file_->message_type(i));
    PrintProperty(&printer, messageClassName, headerFile);
    PrintProperty(&printer, messageClassName + "OrBuilder", headerFile);
  }

  PrintProperty(&printer, JavaClassName(file_), headerFile);
}

void PrintClassMappings(const Descriptor* descriptor, io::Printer* printer) {
  PrintProperty(printer, JavaClassName(descriptor), ClassName(descriptor));
  for (int i = 0; i < descriptor->nested_type_count(); i++) {
    PrintClassMappings(descriptor->nested_type(i), printer);
  }
}

void FileGenerator::GenerateClassMappings(GeneratorContext* context) {
  std::string filename = MappedInputName(file_) + ".clsmap.properties";
  std::unique_ptr<io::ZeroCopyOutputStream> output(context->Open(filename));
  io::Printer printer(output.get(), '$');
  PrintProperty(&printer, JavaClassName(file_), ClassName(file_));
  for (int i = 0; i < file_->enum_type_count(); i++) {
    PrintProperty(&printer, JavaClassName(file_->enum_type(i)),
                  ClassName(file_->enum_type(i)));
  }
  for (int i = 0; i < file_->message_type_count(); i++) {
    PrintClassMappings(file_->message_type(i), &printer);
  }
}

}  // namespace j2objc
}  // namespace compiler
}  // namespace protobuf
}  // namespace google
