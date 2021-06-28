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

#include <google/protobuf/compiler/j2objc/j2objc_generator.h>

#include <google/protobuf/compiler/j2objc/j2objc_file.h>
#include <google/protobuf/compiler/j2objc/j2objc_helpers.h>

namespace google {
namespace protobuf {
namespace compiler {
namespace j2objc {

J2ObjCGenerator::J2ObjCGenerator() {}
J2ObjCGenerator::~J2ObjCGenerator() {}

bool J2ObjCGenerator::Generate(const FileDescriptor* file,
                               const std::string& parameter,
                               GeneratorContext* context,
                               std::string* error) const {
  // -----------------------------------------------------------------
  // parse generator options

  std::vector<std::pair<std::string, std::string> > options;
  ParseGeneratorParameter(parameter, &options);

  bool generate_class_mappings = false;

  for (int i = 0; i < options.size(); i++) {
    if (options[i].first == "prefixes") {
      ParsePrefixFile(options[i].second);
    } else if (options[i].first == "file_dir_mapping") {
      GenerateFileDirMapping();
    } else if (options[i].first == "generate_class_mappings") {
      generate_class_mappings = true;
    } else {
      *error = "Unknown generator option: " + options[i].first;
      return false;
    }
  }

  // -----------------------------------------------------------------

  FileGenerator file_generator(file);
  if (!file_generator.Validate(error)) {
    return false;
  }

  std::vector<std::string> all_files;

  // Generate main source and header files.
  file_generator.Generate(context, &all_files);

  // Generate sibling files.
  file_generator.GenerateSiblings(context, &all_files);

  if (IsGenerateFileDirMapping()) {
    file_generator.GenerateHeaderMappings(context);
  }

  if (generate_class_mappings) {
    file_generator.GenerateClassMappings(context);
  }

  return true;
}

}  // namespace j2objc
}  // namespace compiler
}  // namespace protobuf
}  // namespace google
