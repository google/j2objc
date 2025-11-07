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

#include <google/protobuf/compiler/protolite/j2objc_helpers.h>

#include <map>

namespace google {
namespace protobuf {
namespace compiler {
namespace j2objc {

namespace {

// The field number of the "j2objc_package_prefix" file option defined in
// j2objc-descriptor.proto.
const int kPackagePrefixFieldNumber = 102687446;

// NOLINTBEGIN(runtime/string) - Existing code design requires globals.
static std::string globalPrefix;
static std::string globalPostfix;
static std::map<std::string, std::string> prefixes;
static std::map<std::string, std::string> wildcardPrefixes;
// NOLINTEND(error_category)

std::string CapitalizeJavaPackage(const std::string input) {
  std::string result;
  bool cap_next_letter = true;
  for (int i = 0; i < input.size(); i++) {
    if ('.' == input[i]) {
      cap_next_letter = true;
      continue;
    }
    if (cap_next_letter && 'a' <= input[i] && input[i] <= 'z') {
      result += input[i] + ('A' - 'a');
    } else {
      result += input[i];
    }
    cap_next_letter = false;
  }
  return result;
}

const UnknownField *FindUnknownField(const FileDescriptor *file, int field_num) {
  const Reflection *reflection = file->options().GetReflection();
  const UnknownFieldSet& unknown_fields =
      reflection->GetUnknownFields(file->options());
  if (!unknown_fields.empty()) {
    for (int i = 0; i < unknown_fields.field_count(); i++) {
      const UnknownField& field = unknown_fields.field(i);
      if (field.number() == field_num) {
        return &field;
      }
    }
  }
  return nullptr;
}

std::string GetPackagePrefix(const FileDescriptor *file) {
  // Check for the "j2objc_package_prefix" option using unknown fields so we
  // don't have to pre-build j2objc-descriptor.pb.[h|cc].
  const UnknownField *package_prefix_field =
      FindUnknownField(file, kPackagePrefixFieldNumber);
  if (package_prefix_field) {
    return absl::StrCat(globalPrefix, package_prefix_field->length_delimited());
  }

  // Look for a matching prefix from the prefixes file.
  std::string java_package = java::FileJavaPackage(file);
  std::map<std::string, std::string>::iterator it = prefixes.find(java_package);
  if (it != prefixes.end()) {
    return it->second;
  }

  // Look for a matching wildcard prefix.
  std::string sub_package = java_package;
  while (!sub_package.empty()) {
    it = wildcardPrefixes.find(sub_package);
    if (it != wildcardPrefixes.end()) {
      prefixes.insert(
          std::pair<std::string, std::string>(java_package, it->second));
      return globalPrefix + it->second;
    }
    size_t lastDot = sub_package.find_last_of(".");
    if (lastDot == std::string::npos) {
      break;
    }
    sub_package.erase(lastDot);
  }

  return globalPrefix + CapitalizeJavaPackage(java_package);
}

std::string GetClassPrefix(const FileDescriptor* file,
                           const Descriptor* containing_type,
                           bool is_own_file) {
  if (containing_type != nullptr) {
    return ClassName(containing_type) + "_";
  } else {
    if (is_own_file) {
      return GetPackagePrefix(file);
    } else {
      return ClassName(file) + "_";
    }
  }
}

} // namespace

std::string UnderscoresToCamelCase(const std::string &input,
                                   bool cap_next_letter) {
  std::string result;
  // Note:  I distrust ctype.h due to locales.
  for (int i = 0; i < input.size(); i++) {
    if ('a' <= input[i] && input[i] <= 'z') {
      if (cap_next_letter) {
        result += input[i] + ('A' - 'a');
      } else {
        result += input[i];
      }
      cap_next_letter = false;
    } else if ('A' <= input[i] && input[i] <= 'Z') {
      if (i == 0 && !cap_next_letter) {
        // Force first letter to lower-case unless explicitly told to
        // capitalize it.
        result += input[i] + ('a' - 'A');
      } else {
        // Capital letters after the first are left as-is.
        result += input[i];
      }
      cap_next_letter = false;
    } else if ('0' <= input[i] && input[i] <= '9') {
      result += input[i];
      cap_next_letter = true;
    } else {
      cap_next_letter = true;
    }
  }
  return result;
}

bool HasConflictingClassName(const FileDescriptor *file,
                             const std::string &classname) {
  for (int i = 0; i < file->enum_type_count(); i++) {
    if (file->enum_type(i)->name() == classname) {
      return true;
    }
  }
  for (int i = 0; i < file->message_type_count(); i++) {
    if (file->message_type(i)->name() == classname) {
      return true;
    }
  }
  for (int i = 0; i < file->service_count(); i++) {
    if (file->service(i)->name() == classname) {
      return true;
    }
  }
  return false;
}

std::string FileBaseName(const FileDescriptor *file) {
  std::string::size_type last_slash = file->name().find_last_of('/');
  return std::string(last_slash == std::string::npos ? file->name()
                                         : file->name().substr(last_slash + 1));
}

std::string JavaPackageToDir(std::string package_name) {
  std::string package_dir = absl::StrReplaceAll(package_name, {{".", "/"}});
  if (!package_dir.empty()) package_dir += "/";
  return package_dir;
}

std::string ClassName(const Descriptor *descriptor) {
  return absl::StrCat(
      GetClassPrefix(descriptor->file(), descriptor->containing_type(),
                     !java::NestedInFileClass(*descriptor)),
      descriptor->name(), globalPostfix);
}

std::string ClassName(const EnumDescriptor *descriptor) {
  return absl::StrCat(
      GetClassPrefix(descriptor->file(), descriptor->containing_type(),
                     !java::NestedInFileClass(*descriptor)),
      descriptor->name(), globalPostfix);
}

std::string ClassName(const FileDescriptor *descriptor) {
  return absl::StrCat(GetPackagePrefix(descriptor),
                      java::FileClassName(descriptor), globalPostfix);
}

std::string JavaClassName(const Descriptor *descriptor) {
  return java::QualifiedClassName(descriptor);
}

std::string JavaClassName(const EnumDescriptor *descriptor) {
  return java::QualifiedClassName(descriptor);
}

std::string JavaClassName(const FileDescriptor *descriptor) {
  return absl::StrCat(java::FileJavaPackage(descriptor), ".",
                      java::FileClassName(descriptor));
}

std::string MappedInputName(const FileDescriptor *file) {
  return StripProto(file->name());
}

}  // namespace j2objc
}  // namespace compiler
}  // namespace protobuf
}  // namespace google
