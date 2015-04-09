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

#include <google/protobuf/descriptor.h>

namespace google {
namespace protobuf {
namespace compiler {
namespace j2objc {

string SafeName(const string& name);

// Converts the field's name to camel-case, e.g. "foo_bar_baz" becomes
// "fooBarBaz" or "FooBarBaz", respectively.
string UnderscoresToCamelCase(const FieldDescriptor* field);
string UnderscoresToCapitalizedCamelCase(const FieldDescriptor* field);

// Returns the file's base name.
string FileBaseName(const FileDescriptor* file);

// Returns the file's parent directory.
string FileParentDir(const FileDescriptor* file);

void printMapping(const FileDescriptor* file);

// Gets the unqualified class name for the file.  Each .proto file becomes a
// single Java class, with all its contents nested in that class.
string FileClassName(const FileDescriptor* file);

// Returns the file's Java package name.
string FileJavaPackage(const FileDescriptor* file);

// Returns output directory for the given package name.
string JavaPackageToDir(string package_name);

// These return the J2ObjC class name corresponding to the given descriptor.
string ClassName(const Descriptor *descriptor);
string ClassName(const EnumDescriptor *descriptor);
string ClassName(const FileDescriptor *descriptor);

string TypeName(const EnumDescriptor *descriptor);

string EnumValueName(const EnumValueDescriptor *descriptor);

// These return the Java class name corresponding to the given descriptor.
string JavaClassName(const Descriptor *descriptor);
string JavaClassName(const EnumDescriptor *descriptor);
string JavaClassName(const FileDescriptor *descriptor);

// Get the unqualified name that should be used for a field's field
// number constant.
string FieldConstantName(const FieldDescriptor *field);

string GetHeader(const FileDescriptor *descriptor);
string GetHeader(const Descriptor *descriptor);
string GetHeader(const EnumDescriptor *descriptor);

string JoinFlags(const vector<string> &flags);
string GetFieldFlags(const FieldDescriptor *field);

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

string DefaultValue(const FieldDescriptor *field);
string GetFieldTypeEnumValue(const FieldDescriptor *descriptor);
string GetDefaultValueTypeName(const FieldDescriptor *descriptor);
string GetFieldDataClassName(const FieldDescriptor *descriptor);
string GetFieldOptionsData(const FieldDescriptor *descriptor);

void ParsePrefixFile(string prefix_file);

string MappedInputName(const FileDescriptor* file);
string StaticOutputFileName(const FileDescriptor* file, string suffix);
string FileDirMappingOutputName(const FileDescriptor* file);
// TODO(user): Remove this function once the single header mapping file is
// no longer used.
void SetFileDirMapping(string mapping_output_file);
bool UseStaticOutputFile();
void GenerateFileDirMapping();
bool IsGenerateFileDirMapping();

}  // namespace j2objc
}  // namespace compiler
}  // namespace protobuf
}  // namespace google

#endif  // GOOGLE_PROTOBUF_COMPILER_J2OBJC_HELPERS_H__
