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
//  Sanjay Ghemawat, Jeff Dean, Kenton Varda, and others.

#ifndef GOOGLE_PROTOBUF_COMPILER_J2OBJC_HELPERS_H__
#define GOOGLE_PROTOBUF_COMPILER_J2OBJC_HELPERS_H__

#include <string>

#include "google/protobuf/compiler/j2objc/common.h"

namespace google {
namespace protobuf {
namespace compiler {
namespace j2objc {

std::string SafeName(const std::string &name);

std::string UnderscoresToCamelCase(const std::string &input,
                                   bool cap_next_letter);

// Converts the field's name to camel-case, e.g. "foo_bar_baz" becomes
// "fooBarBaz" or "FooBarBaz", respectively.
std::string UnderscoresToCamelCase(const FieldDescriptor *field);
std::string UnderscoresToCapitalizedCamelCase(const FieldDescriptor *field);

inline bool IsMapEntry(const Descriptor* descriptor) {
  return descriptor->options().map_entry();
}

inline bool IsMapField(const FieldDescriptor* descriptor) {
  const Descriptor* message_type = descriptor->message_type();
  return message_type != NULL ? IsMapEntry(message_type) : false;
}

// Returns the file's base name.
std::string FileBaseName(const FileDescriptor *file);

// Returns the file's parent directory.
std::string FileParentDir(const FileDescriptor *file);

// Gets the unqualified class name for the file.  Each .proto file becomes a
// single Java class, with all its contents nested in that class.
std::string FileClassName(const FileDescriptor *file);

// Check whether there is any type defined in the proto file that has
// the given class name.
bool HasConflictingClassName(const FileDescriptor *file,
                             const std::string &classname);

// Returns the file's Java package name.
std::string FileJavaPackage(const FileDescriptor *file);

// Returns output directory for the given package name.
std::string JavaPackageToDir(std::string package_name);

// These return the J2ObjC class name corresponding to the given descriptor.
std::string ClassName(const Descriptor *descriptor);
std::string ClassName(const EnumDescriptor *descriptor);
std::string ClassName(const FileDescriptor *descriptor);

std::string CEnumName(const EnumDescriptor *descriptor);

std::string EnumValueName(const EnumValueDescriptor *descriptor);

// These return the Java class name corresponding to the given descriptor.
std::string JavaClassName(const Descriptor *descriptor);
std::string JavaClassName(const EnumDescriptor *descriptor);
std::string JavaClassName(const FileDescriptor *descriptor);

// Get the unqualified name that should be used for a field's field
// number constant.
std::string FieldConstantName(const FieldDescriptor *field);

std::string GetHeader(const FileDescriptor *descriptor);
std::string GetHeader(const Descriptor *descriptor);
std::string GetHeader(const EnumDescriptor *descriptor);

std::string JoinFlags(const std::vector<std::string> &flags);
std::string GetFieldFlags(const FieldDescriptor *field);

enum JavaType {
  JAVATYPE_INT,
  JAVATYPE_LONG,
  JAVATYPE_FLOAT,
  JAVATYPE_DOUBLE,
  JAVATYPE_BOOLEAN,
  JAVATYPE_STRING,
  JAVATYPE_BYTES,
  JAVATYPE_ENUM,
  JAVATYPE_MESSAGE
};

JavaType GetJavaType(const FieldDescriptor* field);

std::string DefaultValue(const FieldDescriptor *field);
std::string GetFieldTypeEnumValue(const FieldDescriptor *descriptor);
std::string GetDefaultValueTypeName(const FieldDescriptor *descriptor);
std::string GetFieldOptionsData(const FieldDescriptor *descriptor);

void ParsePrefixFile(std::string prefix_file);

std::string MappedInputName(const FileDescriptor *file);
std::string StaticOutputFileName(const FileDescriptor *file,
                                 std::string suffix);
std::string FileDirMappingOutputName(const FileDescriptor *file);
void GenerateFileDirMapping();
bool IsGenerateFileDirMapping();

}  // namespace j2objc
}  // namespace compiler
}  // namespace protobuf
}  // namespace google

#endif  // GOOGLE_PROTOBUF_COMPILER_J2OBJC_HELPERS_H__
