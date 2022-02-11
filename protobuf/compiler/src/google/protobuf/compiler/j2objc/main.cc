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

#include <google/protobuf/compiler/command_line_interface.h>
#include <google/protobuf/compiler/java/java_generator.h>
#include "j2objc_generator.h"

int main(int argc, const char* const argv[]) {
  CommandLineInterface cli;

  ::java::JavaGenerator java_generator;
  cli.RegisterGenerator("--java_out", "--java_opt", &java_generator,
                        "Generate Java source file.");

  google::protobuf::compiler::j2objc::J2ObjCGenerator j2objc_generator;
  std::string j2objc_opt_help_text = R"(
  --j2objc_opt=<opt1,opt2,..>
                              Options to generate Objective-C files.
                              prefixes: specifies prefixes for package names.
                              generate_class_mappings: generates a properties
                                file that maps Java classes to their proto
                                files.
                              lite: enable to generate lite version of the
                                Objective-C files.)";
  cli.RegisterGenerator(
      "--j2objc_out", "--j2objc_opt", &j2objc_generator,
      "Generate J2ObjC-compatible Objective-C files." + j2objc_opt_help_text);

  return cli.Run(argc, argv);
}
