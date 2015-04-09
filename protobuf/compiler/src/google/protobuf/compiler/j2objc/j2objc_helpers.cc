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

#include <google/protobuf/compiler/j2objc/j2objc_helpers.h>

#include <fstream>
#include <map>
#include <set>
#include <sstream>

#include <google/protobuf/stubs/common.h>
#include <google/protobuf/descriptor.pb.h>
#include <google/protobuf/stubs/strutil.h>

namespace google {
namespace protobuf {
namespace compiler {
namespace j2objc {

namespace {

const char* kDefaultPackage = "";

static map<string, string> prefixes;

static string fileDirMappingFile = "";

static bool generateFileDirMapping = false;

const char* const kKeywordList[] = {
  "TYPE_BOOL",
  "TRUE",
  "FALSE",
  "YES",
  "NO",
  "NULL",
  "FILE"
};

set<string> MakeKeywordsMap() {
  set<string> result;
  for (int i = 0; i < GOOGLE_ARRAYSIZE(kKeywordList); i++) {
    result.insert(kKeywordList[i]);
  }
  return result;
}

set<string> kKeywords = MakeKeywordsMap();

const string& FieldName(const FieldDescriptor* field) {
  // Groups are hacky:  The name of the field is just the lower-cased name
  // of the group type.  In Java, though, we would like to retain the original
  // capitalization of the type name.
  if (field->type() == FieldDescriptor::TYPE_GROUP) {
    return field->message_type()->name();
  } else {
    return field->name();
  }
}

string UnderscoresToCamelCaseImpl(const string& input, bool cap_next_letter) {
  string result;
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

string StripProto(const string& filename) {
  if (HasSuffixString(filename, ".protodevel")) {
    return StripSuffixString(filename, ".protodevel");
  } else {
    return StripSuffixString(filename, ".proto");
  }
}

string CapitalizeJavaPackage(const string input) {
  string result;
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

string GetPackagePrefix(const FileDescriptor *file) {
  string java_package = FileJavaPackage(file);
  map<string, string>::iterator it = prefixes.find(java_package);
  if (it != prefixes.end()) {
    return it->second;
  }
  return CapitalizeJavaPackage(java_package);
}

string GetJavaClassPrefix(const FileDescriptor *file,
                          const Descriptor *containing_type) {
  if (containing_type != NULL) {
    return JavaClassName(containing_type);
  } else {
    if (file->options().java_multiple_files()) {
      return FileJavaPackage(file);
    } else {
      return JavaClassName(file);
    }
  }
}

string GetClassPrefix(const FileDescriptor *file,
                      const Descriptor *containing_type) {
  if (containing_type != NULL) {
    return ClassName(containing_type) + "_";
  } else {
    if (file->options().java_multiple_files()) {
      return GetPackagePrefix(file);
    } else {
      return ClassName(file) + "_";
    }
  }
}

} // namespace

string SafeName(const string& name) {
  string result = name;
  if (kKeywords.count(result) > 0) {
    result.append("_");
  }
  return result;
}

string UnderscoresToCamelCase(const FieldDescriptor* field) {
  return UnderscoresToCamelCaseImpl(FieldName(field), false);
}

string UnderscoresToCapitalizedCamelCase(const FieldDescriptor* field) {
  return UnderscoresToCamelCaseImpl(FieldName(field), true);
}

string FileClassName(const FileDescriptor* file) {
  if (file->options().has_java_outer_classname()) {
    return file->options().java_outer_classname();
  } else {
    return UnderscoresToCamelCaseImpl(StripProto(FileBaseName(file)), true);
  }
}

string FileParentDir(const FileDescriptor* file) {
  string::size_type last_slash = file->name().find_last_of('/');
  return file->name().substr(0, last_slash + 1);
}

string FileBaseName(const FileDescriptor* file) {
  string::size_type last_slash = file->name().find_last_of('/');
  return last_slash == string::npos ?
      file->name() : file->name().substr(last_slash + 1);
}

string FileJavaPackage(const FileDescriptor* file) {
  string result;

  if (file->options().has_java_package()) {
    result = file->options().java_package();
  } else {
    result = kDefaultPackage;
    if (!file->package().empty()) {
      if (!result.empty()) result += '.';
      result += file->package();
    }
  }


  return result;
}

string JavaPackageToDir(string package_name) {
  string package_dir =
    StringReplace(package_name, ".", "/", true);
  if (!package_dir.empty()) package_dir += "/";
  return package_dir;
}

string ClassName(const Descriptor *descriptor) {
  return GetClassPrefix(descriptor->file(), descriptor->containing_type())
      + descriptor->name();
}

string TypeName(const EnumDescriptor *descriptor) {
  return GetClassPrefix(descriptor->file(), descriptor->containing_type())
      + descriptor->name();
}

string ClassName(const EnumDescriptor *descriptor) {
  return TypeName(descriptor) + "Enum";
}

string ClassName(const FileDescriptor *descriptor) {
  return GetPackagePrefix(descriptor) + FileClassName(descriptor);
}

string EnumValueName(const EnumValueDescriptor *descriptor) {
  return TypeName(descriptor->type()) + "_" + descriptor->name();
}

string FieldConstantName(const FieldDescriptor *field) {
  string name = field->name() + "_FIELD_NUMBER";
  UpperString(&name);
  return name;
}

string JavaClassName(const Descriptor *descriptor) {
  return GetJavaClassPrefix(descriptor->file(), descriptor->containing_type())
      + "." + descriptor->name();
}

string JavaClassName(const EnumDescriptor *descriptor) {
  return GetJavaClassPrefix(descriptor->file(), descriptor->containing_type())
      + "." + descriptor->name();
}

string JavaClassName(const FileDescriptor *descriptor) {
  return FileJavaPackage(descriptor) + "." + FileClassName(descriptor);
}

string GetHeader(const FileDescriptor *descriptor) {
  if (UseStaticOutputFile()) {
    return StaticOutputFileName(descriptor, ".h");
  } else {
    return JavaPackageToDir(FileJavaPackage(descriptor))
        + FileClassName(descriptor) + ".h";
  }
}

string GetHeader(const Descriptor *descriptor) {
  const FileDescriptor *file = descriptor->file();
  if (file->options().java_multiple_files()) {
    const Descriptor *containing_type = descriptor->containing_type();
    if (containing_type != NULL) {
      return GetHeader(containing_type);
    } else {
      if (UseStaticOutputFile()) {
        return StaticOutputFileName(file, ".h");
      } else {
        return JavaPackageToDir(FileJavaPackage(file))
            + descriptor->name() + ".h";
      }
    }
  } else {
    return GetHeader(file);
  }
}

string GetHeader(const EnumDescriptor *descriptor) {
  const FileDescriptor *file = descriptor->file();
  if (file->options().java_multiple_files()) {
    const Descriptor *containing_type = descriptor->containing_type();
    if (containing_type != NULL) {
      return GetHeader(containing_type);
    } else {
      if (UseStaticOutputFile()) {
        return StaticOutputFileName(file, ".h");
      } else {
        return JavaPackageToDir(FileJavaPackage(file))
            + descriptor->name() + ".h";
      }
    }
  } else {
    return GetHeader(file);
  }
}

string JoinFlags(const vector<string> &flags) {
  if (flags.size() == 0) {
    return "0";
  }
  string result;
  for (size_t i = 0; i < flags.size(); i++) {
    if (i > 0) {
      result.append(" | ");
    }
    result.append(flags[i]);
  }
  return result;
}

string GetFieldFlags(const FieldDescriptor *field) {
  vector<string> flags;
  if (field->is_required()) {
    flags.push_back("CGPFieldFlagRequired");
  }
  if (field->is_repeated()) {
    flags.push_back("CGPFieldFlagRepeated");
  }
  if (field->is_extension()) {
    flags.push_back("CGPFieldFlagExtension");
  }
  if (field->options().packed()) {
    flags.push_back("CGPFieldFlagPacked");
  }
  return JoinFlags(flags);
}

static string DefaultValueInt(int32 i) {
  // gcc and llvm reject the decimal form of kint32min and kint64min.
  if (i == INT_MIN) {
    return "-0x80000000";
  }
  return SimpleItoa(i);
}

static string DefaultValueLong(int64 l) {
  // gcc and llvm reject the decimal form of kint32min and kint64min.
  if (l == LLONG_MIN) {
    return "-0x8000000000000000LL";
  }
  return SimpleItoa(l) + "LL";
}

static string HandleExtremeFloatingPoint(string val, bool add_float_suffix) {
  if (val == "nan") {
    return "NAN";
  } else if (val == "inf") {
    return "INFINITY";
  } else if (val == "-inf") {
    return "-INFINITY";
  } else {
    // float strings with ., e or E need to have f appended
    if (add_float_suffix && (val.find(".") != string::npos ||
                             val.find("e") != string::npos ||
                             val.find("E") != string::npos)) {
      val += "f";
    }
    return val;
  }
}

// Escape C++ trigraphs by escaping question marks to \?
static string EscapeTrigraphs(const string& to_escape) {
  return StringReplace(to_escape, "?", "\\?", true);
}

JavaType GetJavaType(const FieldDescriptor* field) {
  switch (field->type()) {
    case FieldDescriptor::TYPE_INT32:
    case FieldDescriptor::TYPE_UINT32:
    case FieldDescriptor::TYPE_SINT32:
    case FieldDescriptor::TYPE_FIXED32:
    case FieldDescriptor::TYPE_SFIXED32:
      return JAVATYPE_INT;

    case FieldDescriptor::TYPE_INT64:
    case FieldDescriptor::TYPE_UINT64:
    case FieldDescriptor::TYPE_SINT64:
    case FieldDescriptor::TYPE_FIXED64:
    case FieldDescriptor::TYPE_SFIXED64:
      return JAVATYPE_LONG;

    case FieldDescriptor::TYPE_FLOAT:
      return JAVATYPE_FLOAT;

    case FieldDescriptor::TYPE_DOUBLE:
      return JAVATYPE_DOUBLE;

    case FieldDescriptor::TYPE_BOOL:
      return JAVATYPE_BOOLEAN;

    case FieldDescriptor::TYPE_STRING:
      return JAVATYPE_STRING;

    case FieldDescriptor::TYPE_BYTES:
      return JAVATYPE_BYTES;

    case FieldDescriptor::TYPE_ENUM:
      return JAVATYPE_ENUM;

    case FieldDescriptor::TYPE_GROUP:
    case FieldDescriptor::TYPE_MESSAGE:
      return JAVATYPE_MESSAGE;

    // No default because we want the compiler to complain if any new
    // types are added.
  }

  GOOGLE_LOG(FATAL) << "Can't get here.";
  return JAVATYPE_INT;
}

string DefaultValue(const FieldDescriptor* field) {
  if (field->is_repeated()) {
    return "nil";
  }
  // Switch on cpp_type since we need to know which default_value_* method
  // of FieldDescriptor to call.
  switch (field->cpp_type()) {
    case FieldDescriptor::CPPTYPE_INT32:
      return DefaultValueInt(field->default_value_int32());
    case FieldDescriptor::CPPTYPE_UINT32:
      return DefaultValueInt(field->default_value_uint32());
    case FieldDescriptor::CPPTYPE_INT64:
      return DefaultValueLong(field->default_value_int64());
    case FieldDescriptor::CPPTYPE_UINT64:
      return DefaultValueLong(field->default_value_uint64());
    case FieldDescriptor::CPPTYPE_DOUBLE:
      return HandleExtremeFloatingPoint(
          SimpleDtoa(field->default_value_double()), false);
    case FieldDescriptor::CPPTYPE_FLOAT:
      return HandleExtremeFloatingPoint(
          SimpleFtoa(field->default_value_float()), true);
    case FieldDescriptor::CPPTYPE_BOOL:
      return field->default_value_bool() ? "YES" : "NO";
    case FieldDescriptor::CPPTYPE_STRING: {
      const string &default_string = field->default_value_string();
      if (field->type() == FieldDescriptor::TYPE_BYTES) {
        const bool has_default_value = field->has_default_value();
        if (!has_default_value || default_string.length() == 0) {
          // If the field is defined as being the empty string,
          // then we will just assign to nil, as the empty string is the
          // default for data.
          return "nil";
        }
        // We want constant fields in our data structures so we can
        // declare them as static. To achieve this we cheat and stuff
        // an escaped c string (prefixed with a length) into the data
        // field, and cast it to an (NSData*) so it will compile.
        // The runtime library knows how to handle it.

        // Must convert to a standard byte order for packing length into
        // a cstring.
        uint32_t length = ghtonl(default_string.length());
        string bytes((const char*)&length, sizeof(length));
        bytes.append(default_string);
        return "\"" + CEscape(bytes) + "\"";
      } else {
        return "@\"" + EscapeTrigraphs(CEscape(default_string)) + "\"";
      }
    }
    case FieldDescriptor::CPPTYPE_ENUM:
      return EnumValueName(field->default_value_enum());
    case FieldDescriptor::CPPTYPE_MESSAGE:
      return "nil";
  }

  GOOGLE_LOG(FATAL) << "Can't get here.";
  return "";
}

string GetFieldTypeEnumValue(const FieldDescriptor *descriptor) {
  switch (descriptor->type()) {
    case FieldDescriptor::TYPE_DOUBLE: return "DOUBLE";
    case FieldDescriptor::TYPE_FLOAT: return "FLOAT";
    case FieldDescriptor::TYPE_INT64: return "INT64";
    case FieldDescriptor::TYPE_UINT64: return "UINT64";
    case FieldDescriptor::TYPE_INT32: return "INT32";
    case FieldDescriptor::TYPE_FIXED64: return "FIXED64";
    case FieldDescriptor::TYPE_FIXED32: return "FIXED32";
    case FieldDescriptor::TYPE_BOOL: return "BOOL";
    case FieldDescriptor::TYPE_STRING: return "STRING";
    case FieldDescriptor::TYPE_GROUP: return "GROUP";
    case FieldDescriptor::TYPE_MESSAGE: return "MESSAGE";
    case FieldDescriptor::TYPE_BYTES: return "BYTES";
    case FieldDescriptor::TYPE_UINT32: return "UINT32";
    case FieldDescriptor::TYPE_ENUM: return "ENUM";
    case FieldDescriptor::TYPE_SFIXED32: return "SFIXED32";
    case FieldDescriptor::TYPE_SFIXED64: return "SFIXED64";
    case FieldDescriptor::TYPE_SINT32: return "SINT32";
    case FieldDescriptor::TYPE_SINT64: return "SINT64";
  }
}

string GetDefaultValueTypeName(const FieldDescriptor *descriptor) {
  if (descriptor->is_repeated()) {
    return "Id";
  }
  switch (GetJavaType(descriptor)) {
    case JAVATYPE_INT:
    case JAVATYPE_ENUM:
      return "Int";
    case JAVATYPE_LONG: return "Long";
    case JAVATYPE_FLOAT: return "Float";
    case JAVATYPE_DOUBLE: return "Double";
    case JAVATYPE_BOOLEAN: return "Bool";
    case JAVATYPE_BYTES:
      return "Ptr";
    case JAVATYPE_STRING:
    case JAVATYPE_MESSAGE:
      return "Id";
  }
}

string GetFieldDataClassName(const FieldDescriptor *descriptor) {
  switch (GetJavaType(descriptor)) {
    case JAVATYPE_INT:
    case JAVATYPE_LONG:
    case JAVATYPE_FLOAT:
    case JAVATYPE_DOUBLE:
    case JAVATYPE_BOOLEAN:
    case JAVATYPE_STRING:
    case JAVATYPE_BYTES:
      return "NULL";
    case JAVATYPE_ENUM:
      return "\"" + ClassName(descriptor->enum_type()) + "\"";
    case JAVATYPE_MESSAGE:
      return "\"" + ClassName(descriptor->message_type()) + "\"";
  }
}

string GetFieldOptionsData(const FieldDescriptor *descriptor) {
  string field_options = descriptor->options().SerializeAsString();
  // Must convert to a standard byte order for packing length into
  // a cstring.
  uint32_t length = ghtonl(field_options.length());
  if (length > 0) {
    string bytes((const char*)&length, sizeof(length));
    bytes.append(field_options);
    return "\"" + CEscape(bytes) + "\"";
  }
  return "NULL";
}

void ParsePrefixLine(string line) {
  string::size_type equals = line.find('=');
  if (equals != string::npos) {
    prefixes.insert(pair<string, string>(line.substr(0, equals),
                                         line.substr(equals + 1)));
  }
}

void ParsePrefixFile(string prefix_file) {
  ifstream in(prefix_file.c_str());
  if (in.is_open()) {
    string line;
    while (in.good()) {
      getline(in, line);
      ParsePrefixLine(line);
    }
    in.close();
  } else {
    GOOGLE_LOG(FATAL) << "Could not open prefixes file: " + prefix_file;
  }
}

string MappedInputName(const FileDescriptor* file) {
  return StripProto(file->name());
}

string StaticOutputFileName(const FileDescriptor* file, string suffix) {
  return MappedInputName(file) + ".j2objc.pb" + suffix;
}

string FileDirMappingOutputName(const FileDescriptor* file) {
  return MappedInputName(file) + ".j2objc.mapping";
}

// TODO(user): Remove this function once the single header mapping file is
// no longer used.
void SetFileDirMapping(string mapping_output_file) {
  fileDirMappingFile = mapping_output_file;
}

void GenerateFileDirMapping() {
  generateFileDirMapping = true;
}

bool IsGenerateFileDirMapping() {
  return generateFileDirMapping;
}

// TODO(user): Remove this function once the single header mapping file is
// no longer used.
bool UseStaticOutputFile() {
  return fileDirMappingFile != "" || IsGenerateFileDirMapping();
}

// TODO(user): Remove this function once the single header mapping file is
// no longer used.
void printMapping(const FileDescriptor* file) {
  if (fileDirMappingFile != "") {
    string headerFile = StaticOutputFileName(file, ".h");
    ofstream ofs(fileDirMappingFile.c_str(),
                 std::ofstream::out | std::ofstream::app);

    if (ofs.is_open()) {
      for (int i = 0; i < file->enum_type_count(); i++) {
        ofs << JavaClassName(file->enum_type(i)) << "=" << headerFile
            << std::endl;
      }
      for (int i = 0; i < file->message_type_count(); i++) {
        string messageClassName = JavaClassName(file->message_type(i));
        ofs << messageClassName << "=" << headerFile << std::endl;
        ofs << messageClassName + "OrBuilder" << "=" << headerFile
            << std::endl;
      }

      ofs << JavaClassName(file) << "=" << headerFile << std::endl;
      ofs.close();
    }
  }
}

}  // namespace j2objc
}  // namespace compiler
}  // namespace protobuf
}  // namespace google
