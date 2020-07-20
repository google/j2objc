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

#ifndef GOOGLE_PROTOBUF_COMPILER_J2OBJC_FILE_H__
#define GOOGLE_PROTOBUF_COMPILER_J2OBJC_FILE_H__

#include <string>
#include <vector>

#include "google/protobuf/compiler/j2objc/common.h"

namespace google {
namespace protobuf {
namespace compiler {
namespace j2objc {

class FileGenerator {
 public:
  explicit FileGenerator(const FileDescriptor* file);
  ~FileGenerator();

  // Checks for problems that would otherwise lead to cryptic compile errors.
  // Returns true if there are no problems, or writes an error description to
  // the given string and returns false otherwise.
  bool Validate(std::string* error);

  void Generate(GeneratorContext* generator_context,
                std::vector<std::string>* file_list);

  // If we aren't putting everything into one file, this will write all the
  // files other than the outer file (i.e. one for each message, enum, and
  // service type).
  void GenerateSiblings(GeneratorContext* generator_context,
                        std::vector<std::string>* file_list);

  void GenerateHeaderMappings(GeneratorContext* context);
  void GenerateClassMappings(GeneratorContext* generator_context);

 private:
  void GenerateBoilerplate(io::Printer* printer);
  void GenerateHeaderBoilerplate(io::Printer* printer);
  void GenerateSourceBoilerplate(io::Printer* printer);
  void GenerateHeader(GeneratorContext* generator_context,
                      std::vector<std::string>* file_list);
  void GenerateSource(GeneratorContext* generator_context,
                      std::vector<std::string>* file_list);
  void GenerateEnumHeader(GeneratorContext* context,
                          std::vector<std::string>* file_list,
                          const EnumDescriptor* descriptor);
  void GenerateEnumSource(GeneratorContext* context,
                          std::vector<std::string>* file_list,
                          const EnumDescriptor* descriptor);
  void GenerateMessageHeader(GeneratorContext* context,
                             std::vector<std::string>* file_list,
                             const Descriptor* descriptor);
  void GenerateMessageSource(GeneratorContext* context,
                             std::vector<std::string>* file_list,
                             const Descriptor* descriptor);
  void GenerateMessageOrBuilder(GeneratorContext* context,
                                std::vector<std::string>* file_list,
                                const Descriptor* descriptor);
  std::string GetFileName(std::string suffix);
  bool GenerateMultipleFiles();

  const FileDescriptor* file_;
  std::string output_dir_;
  std::string classname_;

  GOOGLE_DISALLOW_EVIL_CONSTRUCTORS(FileGenerator);
};

}  // namespace j2objc
}  // namespace compiler
}  // namespace protobuf
}  // namespace google

#endif  // GOOGLE_PROTOBUF_COMPILER_J2OBJC_FILE_H__
