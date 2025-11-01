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

#include "google/protobuf/compiler/protolite/j2objc_file.h"

#include "google/protobuf/compiler/protolite/j2objc_helpers.h"

namespace google {
namespace protobuf {
namespace compiler {
namespace j2objc {

FileGenerator::FileGenerator(const FileDescriptor* file)
    : file_(file), classname_(java::FileClassName(file)) {
  output_dir_ = JavaPackageToDir(java::FileJavaPackage(file));
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

void PrintProperty(io::Printer* printer, const std::string& key,
                   const std::string& value) {
  printer->Print("$key$=$value$\n", "key", key, "value", value);
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
