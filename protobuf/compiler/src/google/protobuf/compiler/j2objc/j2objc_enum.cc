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

#include <google/protobuf/compiler/j2objc/j2objc_enum.h>
#include <google/protobuf/compiler/j2objc/j2objc_helpers.h>

#include <string>

#include "google/protobuf/compiler/j2objc/common.h"

namespace google {
namespace protobuf {
namespace compiler {
namespace j2objc {

EnumGenerator::EnumGenerator(const EnumDescriptor* descriptor)
  : descriptor_(descriptor) {
    for (int i = 0; i < descriptor_->value_count(); i++) {
      const EnumValueDescriptor* value = descriptor_->value(i);
      const EnumValueDescriptor* canonical_value =
        descriptor_->FindValueByNumber(value->number());

      if (value == canonical_value) {
        canonical_values_.push_back(value);
      } else {
        Alias alias;
        alias.value = value;
        alias.canonical_value = canonical_value;
        aliases_.push_back(alias);
      }
    }
}

EnumGenerator::~EnumGenerator() {
}

void EnumGenerator::CollectSourceImports(std::set<std::string>* imports) const {
  imports->insert("java/lang/IllegalArgumentException.h");
  imports->insert("java/lang/IllegalStateException.h");
}

void EnumGenerator::GenerateHeader(io::Printer* printer) {
  printer->Print(
    "\nJ2OBJC_CLASS_DECLARATION($classname$);\n",
    "classname", ClassName(descriptor_));
  printer->Print(
      "\n// Java enum ordinals for use with [... ordinal], etc.\n"
      "// WARNING: NOT the proto enum value, for that use the value constants "
      "below.\n");
  printer->Print("typedef NS_ENUM(jint, $ordinalenumname$) {\n",
                 "ordinalenumname", COrdinalEnumName(descriptor_));
  printer->Indent();

  for (int i = 0; i < canonical_values_.size(); i++) {
      printer->Print("$ordinalname$ = $ordinal$,\n", "ordinalname",
                     EnumOrdinalName(canonical_values_[i]), "ordinal",
                     SimpleItoa(i));
  }
  if (!descriptor_->is_closed()) {
      printer->Print("$ordinalname$_UNRECOGNIZED = $count$,\n", "ordinalname",
                     COrdinalEnumName(descriptor_), "count",
                     SimpleItoa(canonical_values_.size()));
  }

  printer->Outdent();
  printer->Print("};\n\n");

  printer->Print(
      "\n// Java enum ordinal preprocessor name that allows for stricter enum "
      "types\n"
      "// outside transpiled code.\n"
      "#if J2OBJC_IMPORTED_BY_JAVA_IMPLEMENTATION\n"
      "#define $ordinalpreprocessorname$ jint\n"
      "#else\n"
      "#define $ordinalpreprocessorname$ $ordinalenumname$\n"
      "#endif\n\n",
      "ordinalpreprocessorname", COrdinalPreprocessorName(descriptor_),
      "ordinalenumname", COrdinalEnumName(descriptor_));

  printer->Print(
      "// Value enum for use with [... getNumber], etc.\n"
      "// These enum values are the proto (wire) values.");
  printer->Print("\ntypedef NS_ENUM(jint, $valueenumname$) {\n",
                 "valueenumname", CValueEnumName(descriptor_));
  printer->Indent();

  for (int i = 0; i < canonical_values_.size(); i++) {
      printer->Print("$valuename$ = $value$,\n", "valuename",
                     EnumValueName(canonical_values_[i]), "value",
                     SimpleItoa(canonical_values_[i]->number()));
  }
  if (!descriptor_->is_closed()) {
      printer->Print("$ordinalname$_UNRECOGNIZED = -1,\n", "ordinalname",
                     CValueEnumName(descriptor_));
  }

  printer->Outdent();
  printer->Print("};\n\n");

  printer->Print(
      "\n// Java enum value preprocessor name that allows for stricter enum "
      "types\n"
      "// outside transpiled code.\n"
      "#if J2OBJC_IMPORTED_BY_JAVA_IMPLEMENTATION\n"
      "#define $valuepreprocessorname$ jint\n"
      "#else\n"
      "#define $valuepreprocessorname$ $valueenumname$\n"
      "#endif\n\n",
      "valuepreprocessorname", CValuePreprocessorName(descriptor_),
      "valueenumname", CValueEnumName(descriptor_));

  printer->Print(
      "// Proto enum preprocessor defines.\n"
      "// Prefer to use the value enum above when possible.\n");
  for (int i = 0; i < canonical_values_.size(); i++) {
      printer->Print(
          "#define $classname$_$name$_VALUE ($valuepreprocessorname$)$value$\n",
          "classname", ClassName(descriptor_), "name",
          canonical_values_[i]->name(), "value",
          SimpleItoa(canonical_values_[i]->number()), "valuepreprocessorname",
          CValuePreprocessorName(descriptor_));
  }
  if (!descriptor_->is_closed()) {
      printer->Print(
          "#define $classname$_UNRECOGNIZED_VALUE "
          "($valuepreprocessorname$)-1\n",
          "classname", ClassName(descriptor_), "valuepreprocessorname",
          CValuePreprocessorName(descriptor_));
  }

  printer->Print(
      "\n"
      "@interface $classname$ :"
      " JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> {\n"
      " @private\n"
      "  $valueenumname$ value_;\n"
      "}\n"
      "\n"
      "+ (IOSObjectArray *)values;\n"
      "+ ($classname$ *)valueOfWithNSString:(NSString *)name;\n"
      "+ ($classname$ *)valueOfWithInt:($valuepreprocessorname$)value;\n"
      "+ ($classname$ *)forNumberWithInt:($valuepreprocessorname$)value;\n"
      "- ($valuepreprocessorname$)getNumber;\n"
      "\n"
      "@end\n"
      "\n"
      "J2OBJC_STATIC_INIT($classname$)\n"
      "\n"
      "J2OBJC_TYPE_LITERAL_HEADER($classname$)\n"
      "\n"
      "/*! INTERNAL ONLY - Use enum accessors declared below. */\n"
      "FOUNDATION_EXPORT $classname$ *$classname$_values_[];\n"
      "\n"
      "FOUNDATION_EXPORT IOSObjectArray *$classname$_values(void);\n"
      "FOUNDATION_EXPORT $classname$ *$classname$_valueOfWithNSString_("
      "NSString *name);\n"
      "FOUNDATION_EXPORT $classname$ *$classname$_valueOfWithInt_("
      "$valuepreprocessorname$ value);\n"
      "FOUNDATION_EXPORT $classname$ *$classname$_forNumberWithInt_("
      "$valuepreprocessorname$ value);\n"
      "FOUNDATION_EXPORT $classname$ *$classname$_fromOrdinal("
      "$ordinalpreprocessorname$ ordinal);\n\n",
      "classname", ClassName(descriptor_), "ordinalpreprocessorname",
      COrdinalPreprocessorName(descriptor_), "valueenumname",
      CValueEnumName(descriptor_), "valuepreprocessorname",
      CValuePreprocessorName(descriptor_));

  for (int i = 0; i < canonical_values_.size(); i++) {
    printer->Print(
        "FOUNDATION_EXPORT $classname$ *$classname$_get_$name$(void);\n",
        "classname", ClassName(descriptor_), "name",
        canonical_values_[i]->name());
  }
  if (!descriptor_->is_closed()) {
    printer->Print(
        "FOUNDATION_EXPORT $classname$ *$classname$_get_UNRECOGNIZED(void);\n",
        "classname", ClassName(descriptor_));
  }
}

const int kMaxRowChars = 80;

void EnumGenerator::GenerateSource(io::Printer* printer) {
  const int canonical_count = canonical_values_.size();
  const int enum_count = canonical_count + (descriptor_->is_closed() ? 0 : 1);
  printer->Print(
      "\nJ2OBJC_INITIALIZED_DEFN($classname$)\n"
      "\n"
      "$classname$ *$classname$_values_[$count$];\n"
      "\n"
      "ComGoogleProtobufDescriptors_EnumDescriptor"
      " *$classname$_descriptor_ = nil;\n"
      "\n"
      "@implementation $classname$\n"
      "\n"
      "+ (void)initialize {\n"
      "  if (self == [$classname$ class]) {\n"
      "    NSString *names[] = {",
      "classname", ClassName(descriptor_), "count", SimpleItoa(enum_count));

  // Count characters and only add line breaks when the line exceeds the max.
  int row_chars = kMaxRowChars + 1;
  for (int i = 0; i < canonical_values_.size(); i++) {
    std::string name = canonical_values_[i]->name();
    size_t added_chars = name.length() + 5;
    if (row_chars + added_chars > kMaxRowChars) {
      printer->Print("\n     ");
      row_chars = 5;
    };
    printer->Print(" @\"$name$\",", "name", name);
    row_chars += added_chars;
  }
  if (!descriptor_->is_closed()) {
    printer->Print(" @\"UNRECOGNIZED\",");
  }
  printer->Print("\n"
      "    };\n"
      "    jint int_values[] = {");
  row_chars = kMaxRowChars + 1;
  for (int i = 0; i < canonical_values_.size(); i++) {
    std::string value = SimpleItoa(canonical_values_[i]->number());
    size_t added_chars = value.length() + 2;
    if (row_chars + added_chars > kMaxRowChars) {
      printer->Print("\n     ");
      row_chars = 5;
    };
    printer->Print(" $value$,", "value", value);
    row_chars += added_chars;
  }
  if (!descriptor_->is_closed()) {
    printer->Print(" -1,");
  }

  printer->Print(
      "\n"
      "    };\n"
      "    $classname$_descriptor_ = "
      "CGPInitializeEnumType(self, $count$, $classname$_values_, names,"
      " int_values, $is_closed$);\n"
      "    J2OBJC_SET_INITIALIZED($classname$)\n"
      "  }\n"
      "}\n"
      "\n"
      "+ (IOSObjectArray *)values {\n"
      "  return $classname$_values();\n"
      "}\n"
      "\n"
      "+ ($classname$ *)valueOfWithNSString:(NSString *)name {\n"
      "  return $classname$_valueOfWithNSString_(name);\n"
      "}\n"
      "\n"
      "+ ($classname$ *)valueOfWithInt:($valuepreprocessorname$)value {\n"
      "  return $classname$_valueOfWithInt_(value);\n"
      "}\n"
      "\n"
      "+ ($classname$ *)forNumberWithInt:($valuepreprocessorname$)value {\n"
      "  return $classname$_forNumberWithInt_(value);\n"
      "}\n"
      "\n",
      "classname", ClassName(descriptor_), "count", SimpleItoa(enum_count),
      "valuepreprocessorname", CValuePreprocessorName(descriptor_),
      "is_closed", SimpleItoa(descriptor_->is_closed()));

  printer->Print(
      "- ($valuepreprocessorname$)getNumber {\n",
      "valuepreprocessorname", CValuePreprocessorName(descriptor_));
  if (!descriptor_->is_closed()) {
    printer->Print(
        // "=="" is safe because it's testing a unique enum constant.
        "  if (self == $classname$_get_UNRECOGNIZED()) {\n"
        "    @throw "
        "create_JavaLangIllegalArgumentException_initWithNSString_(\n"
        "        @\"Can't get the number of an unknown enum value.\");\n"
        "  }\n",
        "classname", ClassName(descriptor_));
  }

  printer->Print(
      "  return value_;\n"
      "}\n"
      "\n"
      "+ (ComGoogleProtobufDescriptors_EnumDescriptor *)getDescriptor {\n"
      "  return $classname$_descriptor_;\n"
      "}\n"
      "\n"
      "- (ComGoogleProtobufDescriptors_EnumValueDescriptor *)"
      "getValueDescriptor {\n",
      "classname", ClassName(descriptor_));

  if (!descriptor_->is_closed()) {
    printer->Print(
        "  if (value_ == $classname$_Value_UNRECOGNIZED) {\n"
        "    @throw create_JavaLangIllegalStateException_initWithNSString_(\n"
        "        @\"Can't get the descriptor of an unrecognized enum "
        "value.\");\n"
        "  }\n",
        "classname", ClassName(descriptor_));
  }

  printer->Print(
      "  return $classname$_descriptor_->values_->buffer_[[self ordinal]];\n"
      "}\n"
      "\n"
      "@end\n"
      "\n"
      "J2OBJC_CLASS_TYPE_LITERAL_SOURCE($classname$)\n"
      "\n"
      "IOSObjectArray *$classname$_values(void) {\n"
      "  $classname$_initialize();\n"
      "  return [IOSObjectArray arrayWithObjects:$classname$_values_"
      " count:$count$ type:$classname$_class_()];\n"
      "}\n"
      "\n"
      "$classname$ *$classname$_valueOfWithNSString_(NSString *name) {\n"
      "  $classname$_initialize();\n"
      "  for (jint i = 0; i < $count$; i++) {\n"
      "    $classname$ *e = $classname$_values_[i];\n"
      "    if ([name isEqual:[e name]]) {\n"
      "      return e;\n"
      "    }\n"
      "  }\n"
      "  @throw create_JavaLangIllegalArgumentException_initWithNSString_("
      "name);\n",
      "classname", ClassName(descriptor_), "count",
      SimpleItoa(enum_count));  // Include UNRECOGNIZED constant.

  printer->Print(
      "}\n"
      "\n"
      "$classname$ *$classname$_valueOfWithInt_($valuepreprocessorname$ value) "
      "{\n"
      "  return $classname$_forNumberWithInt_(value);\n"
      "}\n"
      "\n"
      "$classname$ *$classname$_forNumberWithInt_($valuepreprocessorname$ "
      "value) {\n"
      "  $classname$_initialize();\n"
      "  for (jint i = 0; i < $count$; i++) {\n"
      "    $classname$ *e = $classname$_values_[i];\n"
      "    if (value == [e getNumber]) {\n"
      "      return e;\n"
      "    }\n"
      "  }\n"
      "  return nil;\n"
      "}\n"
      "\n",
      "classname", ClassName(descriptor_), "count", SimpleItoa(canonical_count),
      "valuepreprocessorname", CValuePreprocessorName(descriptor_));

  printer->Print(
      "$classname$ *$classname$_fromOrdinal($ordinalpreprocessorname$ ordinal) "
      "{\n"
      "  $classname$_initialize();\n"
      "  if (ordinal >= $count$) {\n"
      "    return nil;\n"
      "  }\n"
      "  return $classname$_values_[ordinal];\n"
      "}\n",
      "classname", ClassName(descriptor_), "count", SimpleItoa(enum_count),
      "ordinalpreprocessorname", COrdinalPreprocessorName(descriptor_));

  for (int i = 0; i < canonical_values_.size(); i++) {
    printer->Print(
        "\n$classname$ *$classname$_get_$name$(void) {\n"
        "  $classname$_initialize();\n"
        "  return $classname$_values_[$classname$_Enum_$name$];\n"
        "}\n",
        "classname", ClassName(descriptor_),
        "name", canonical_values_[i]->name());
  }
  if (!descriptor_->is_closed()) {
    printer->Print(
        "\n$classname$ *$classname$_get_$name$(void) {\n"
        "  $classname$_initialize();\n"
        "  return $classname$_values_[$classname$_Enum_$name$];\n"
        "}\n",
        "classname", ClassName(descriptor_), "name", "UNRECOGNIZED");
  }
}

}  // namespace j2objc
}  // namespace compiler
}  // namespace protobuf
}  // namespace google
