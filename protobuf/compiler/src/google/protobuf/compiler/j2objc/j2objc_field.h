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
//  Sanjay Ghemawat, Jeff Dean, Kenton Varda, and others.

#ifndef GOOGLE_PROTOBUF_COMPILER_J2OBJC_FIELD_H__
#define GOOGLE_PROTOBUF_COMPILER_J2OBJC_FIELD_H__

#include <map>
#include <memory>
#include <set>
#include <string>

#include "google/protobuf/compiler/j2objc/common.h"

namespace google {
namespace protobuf {
namespace compiler {
namespace j2objc {

// Expose for use by ExtensionGenerator.
void CollectSourceImportsForField(std::set<std::string>* imports,
                                  const FieldDescriptor* descriptor);
void GenerateObjcClassRef(
    io::Printer *printer, const FieldDescriptor *descriptor);

class FieldGenerator {
 public:
  FieldGenerator(const FieldDescriptor *descriptor);
  virtual ~FieldGenerator();

  virtual void GenerateFieldHeader(io::Printer* printer) const;
  virtual void GenerateFieldBuilderHeader(io::Printer* printer) const = 0;

  virtual void GenerateMessageOrBuilderProtocol(io::Printer* printer) const = 0;

  virtual void GenerateDeclaration(io::Printer *printer) const = 0;
  virtual void GenerateMapEntryFieldData(io::Printer *printer) const;
  virtual void GenerateFieldData(io::Printer *printer) const;

  virtual void CollectForwardDeclarations(
      std::set<std::string>* declarations) const;
  virtual void CollectMessageOrBuilderForwardDeclarations(
      std::set<std::string>* declarations) const;
  virtual void CollectSourceImports(std::set<std::string>* imports) const;
  virtual void CollectMessageOrBuilderImports(
      std::set<std::string>* imports) const;

 protected:
  const FieldDescriptor* descriptor_;
  std::map<std::string, std::string> variables_;

  virtual void GenerateFieldDataOffset(io::Printer *printer) const;
  virtual void GenerateClassNameOrMapData(io::Printer *printer) const;
  virtual void GenerateStaticRefs(io::Printer *printer) const;

 private:
  GOOGLE_DISALLOW_EVIL_CONSTRUCTORS(FieldGenerator);
};

class SingleFieldGenerator : public FieldGenerator {
 public:
  SingleFieldGenerator(const FieldDescriptor *descriptor, uint32_t *numHasBits);

  virtual ~SingleFieldGenerator() { }

  virtual void GenerateFieldBuilderHeader(io::Printer* printer) const;

  virtual void GenerateMessageOrBuilderProtocol(io::Printer* printer) const;

  virtual void GenerateDeclaration(io::Printer* printer) const;

 private:
  GOOGLE_DISALLOW_EVIL_CONSTRUCTORS(SingleFieldGenerator);
};

class RepeatedFieldGenerator : public FieldGenerator {
 public:
  RepeatedFieldGenerator(const FieldDescriptor *descriptor)
      : FieldGenerator(descriptor) {
  }

  virtual ~RepeatedFieldGenerator() { }

  virtual void GenerateFieldBuilderHeader(io::Printer* printer) const;

  virtual void GenerateMessageOrBuilderProtocol(io::Printer* printer) const;

  virtual void GenerateDeclaration(io::Printer* printer) const;

  virtual void CollectForwardDeclarations(
      std::set<std::string>* declarations) const;
  virtual void CollectMessageOrBuilderForwardDeclarations(
      std::set<std::string>* declarations) const;
  virtual void CollectMessageOrBuilderImports(
      std::set<std::string>* imports) const;

 private:
  GOOGLE_DISALLOW_EVIL_CONSTRUCTORS(RepeatedFieldGenerator);
};

class MapFieldGenerator : public FieldGenerator {
 public:
  MapFieldGenerator(
      const FieldDescriptor *descriptor, uint32_t map_fields_idx);

  virtual ~MapFieldGenerator() { }

  virtual void GenerateFieldBuilderHeader(io::Printer* printer) const;
  virtual void GenerateMessageOrBuilderProtocol(io::Printer* printer) const;
  virtual void GenerateDeclaration(io::Printer* printer) const;
  virtual void GenerateMapEntryFieldData(io::Printer *printer) const;

  virtual void CollectForwardDeclarations(
      std::set<std::string>* declarations) const;
  virtual void CollectMessageOrBuilderForwardDeclarations(
      std::set<std::string>* declarations) const;
  virtual void CollectSourceImports(std::set<std::string>* imports) const;

 protected:
  virtual void GenerateClassNameOrMapData(io::Printer *printer) const;
  virtual void GenerateStaticRefs(io::Printer *printer) const;

 private:
  const FieldDescriptor* key_field_;
  const FieldDescriptor* value_field_;
  const uint32_t entry_fields_idx_;

  GOOGLE_DISALLOW_EVIL_CONSTRUCTORS(MapFieldGenerator);
};

class MapEntryFieldGenerator : public FieldGenerator {
 public:
  MapEntryFieldGenerator(const FieldDescriptor *descriptor)
      : FieldGenerator(descriptor) {
  }
  virtual ~MapEntryFieldGenerator() { }

  virtual void GenerateFieldBuilderHeader(io::Printer* printer) const;
  virtual void GenerateMessageOrBuilderProtocol(io::Printer* printer) const;
  virtual void GenerateDeclaration(io::Printer* printer) const;

 protected:
  virtual void GenerateFieldDataOffset(io::Printer *printer) const;
  virtual void GenerateStaticRefs(io::Printer *printer) const;

 private:
  GOOGLE_DISALLOW_EVIL_CONSTRUCTORS(MapEntryFieldGenerator);
};

// Convenience class which constructs FieldGenerators for a Descriptor.
class FieldGeneratorMap {
 public:
  explicit FieldGeneratorMap(const Descriptor* descriptor);
  ~FieldGeneratorMap();

  const FieldGenerator& get(const FieldDescriptor* field) const;
  uint32_t numHasBits() const {
    return numHasBits_;
  }
  uint32_t numMapFields() const {
    return numMapFields_;
  }

 private:
  const Descriptor* descriptor_;
  std::unique_ptr<std::unique_ptr<FieldGenerator> []> field_generators_;
  uint32_t numHasBits_;
  uint32_t numMapFields_;

  FieldGenerator* MakeGenerator(const FieldDescriptor* field);

  GOOGLE_DISALLOW_EVIL_CONSTRUCTORS(FieldGeneratorMap);
};

}  // namespace j2objc
}  // namespace compiler
}  // namespace protobuf
}  // namespace google

#endif  // GOOGLE_PROTOBUF_COMPILER_J2OBJC_FIELD_H__
