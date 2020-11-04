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
//  Sanjay Ghemawat, Jeff Dean, Cyrus Najmabadi, and others.

#include <google/protobuf/compiler/j2objc/j2objc_oneof.h>

#include <string>

#include <google/protobuf/compiler/j2objc/j2objc_helpers.h>

namespace google {
namespace protobuf {
namespace compiler {
namespace j2objc {

namespace {

std::string CapitalizedName(const OneofDescriptor* descriptor) {
  return UnderscoresToCamelCase(descriptor->name(), true);
}

std::string NotSetName(const OneofDescriptor* descriptor) {
  return ToUpper(CapitalizedName(descriptor)) + "_NOT_SET";
}

std::string CaseClassName(const OneofDescriptor* descriptor) {
  return ClassName(descriptor->containing_type()) + "_"
      + UnderscoresToCamelCase(descriptor->name(), true) + "Case";
}

std::string CaseValueName(const FieldDescriptor* descriptor) {
  return ToUpper(descriptor->name());
}

void FillValueNames(const OneofDescriptor* descriptor,
                    std::vector<std::string>* names) {
  for (int i = 0; i < descriptor->field_count(); i++) {
    names->push_back(CaseValueName(descriptor->field(i)));
  }
  names->push_back(NotSetName(descriptor));
}

void FillNumbers(
    const OneofDescriptor* descriptor, std::vector<int> *numbers) {
  for (int i = 0; i < descriptor->field_count(); i++) {
    numbers->push_back(descriptor->field(i)->number());
  }
  numbers->push_back(0);
}

} // namespace

OneofGenerator::OneofGenerator(const OneofDescriptor* descriptor)
  : descriptor_(descriptor) {
}

OneofGenerator::~OneofGenerator() {
}

void OneofGenerator::CollectMessageOrBuilderForwardDeclarations(
    std::set<std::string>* declarations) const {
  declarations->insert("@class " + CaseClassName(descriptor_));
  declarations->insert(
      "J2OBJC_CLASS_DECLARATION(" + CaseClassName(descriptor_) + ")");
}

void OneofGenerator::CollectHeaderImports(
    std::set<std::string>* imports) const {
  imports->insert("com/google/protobuf/Internal.h");
}

void OneofGenerator::CollectSourceImports(
    std::set<std::string>* imports) const {
  imports->insert("java/lang/IllegalArgumentException.h");
}

void OneofGenerator::GenerateStorageDeclaration(io::Printer* printer) const {
  printer->Print("jint $javaname$_;\n",
                 "javaname", CapitalizedName(descriptor_));
}

void OneofGenerator::GenerateOneofData(io::Printer* printer) const {
  printer->Print(
      "{\n"
      "  .name = \"$name$\",\n"
      "  .javaName = \"$javaname$\",\n"
      "  .firstFieldIdx = $firstidx$,\n"
      "  .fieldCount = $count$,\n"
      "  .offset = offsetof($classname$_Storage, $javaname$_),\n"
      "},\n",
      "name", descriptor_->name(),
      "javaname", CapitalizedName(descriptor_),
      "classname", ClassName(descriptor_->containing_type()),
      "firstidx", SimpleItoa(descriptor_->field(0)->index()),
      "count", SimpleItoa(descriptor_->field_count()));
}

void OneofGenerator::GenerateHeader(io::Printer* printer) {
  printer->Print(
      "\ntypedef NS_ENUM(NSUInteger, $classname$_Enum) {\n",
      "classname", CaseClassName(descriptor_));
  printer->Indent();

  for (int i = 0; i < descriptor_->field_count(); i++) {
    printer->Print(
        "$classname$_Enum_$name$ = $value$,\n",
        "classname", CaseClassName(descriptor_),
        "name", CaseValueName(descriptor_->field(i)),
        "value", SimpleItoa(i));
  }
  printer->Print(
      "$classname$_Enum_$name$ = $value$,\n",
      "classname", CaseClassName(descriptor_),
      "name", NotSetName(descriptor_),
      "value", SimpleItoa(descriptor_->field_count()));

  printer->Outdent();
  printer->Print("};\n");

  printer->Print(
      "\n"
      "@interface $classname$ :"
      " JavaLangEnum<ComGoogleProtobufInternal_EnumLite> {\n"
      " @private\n"
      "  jint value_;\n"
      "}\n"
      "\n"
      "+ (IOSObjectArray *)values;\n"
      "+ ($classname$ *)valueOfWithNSString:(NSString *)name;\n"
      "+ ($classname$ *)valueOfWithInt:(jint)value;\n"
      "+ ($classname$ *)forNumberWithInt:(jint)value;\n"
      "- (jint)getNumber;\n"
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
      "jint value);\n"
      "FOUNDATION_EXPORT $classname$ *$classname$_forNumberWithInt_("
      "jint value);\n"
      "FOUNDATION_EXPORT $classname$ *$classname$_fromOrdinal("
      "NSUInteger ordinal);\n\n",
      "classname", CaseClassName(descriptor_));

  for (int i = 0; i < descriptor_->field_count(); i++) {
    printer->Print(
        "FOUNDATION_EXPORT $classname$ *$classname$_get_$name$(void);\n",
        "classname", CaseClassName(descriptor_),
        "name", CaseValueName(descriptor_->field(i)));
  }
  printer->Print(
      "inline $classname$ *$classname$_get_$name$(void);\n"
      "J2OBJC_ENUM_CONSTANT($classname$, $name$)\n",
      "classname", CaseClassName(descriptor_), "name", NotSetName(descriptor_));
}

const int kMaxRowChars = 80;

void OneofGenerator::GenerateSource(io::Printer* printer) {
  std::vector<std::string> valueNames;
  FillValueNames(descriptor_, &valueNames);
  std::vector<int> numbers;
  FillNumbers(descriptor_, &numbers);

  printer->Print("\n"
      "J2OBJC_INITIALIZED_DEFN($classname$)\n"
      "\n"
      "$classname$ *$classname$_values_[$count$];\n"
      "\n"
      "@implementation $classname$\n"
      "\n"
      "+ (void)initialize {\n"
      "  if (self == [$classname$ class]) {\n"
      "    static NSString *names[] = {",
      "classname", CaseClassName(descriptor_),
      "count", SimpleItoa(descriptor_->field_count() + 1));

  // Count characters and only add line breaks when the line exceeds the max.
  int row_chars = kMaxRowChars + 1;
  for (int i = 0; i < valueNames.size(); i++) {
    std::string name = valueNames[i];
    size_t added_chars = name.length() + 5;
    if (row_chars + added_chars > kMaxRowChars) {
      printer->Print("\n     ");
      row_chars = 5;
    };
    printer->Print(" @\"$name$\",", "name", name);
    row_chars += added_chars;
  }
  printer->Print("\n"
      "    };\n"
      "    static jint int_values[] = {");
  row_chars = kMaxRowChars + 1;
  for (int i = 0; i < numbers.size(); i++) {
    std::string value = SimpleItoa(numbers[i]);
    size_t added_chars = value.length() + 2;
    if (row_chars + added_chars > kMaxRowChars) {
      printer->Print("\n     ");
      row_chars = 5;
    };
    printer->Print(" $value$,", "value", value);
    row_chars += added_chars;
  }

  printer->Print(
      "\n"
      "    };\n"
      "    CGPInitializeOneofCaseEnum("
      "self, $count$, $classname$_values_, names, int_values);\n"
      "    J2OBJC_SET_INITIALIZED($classname$)\n"
      "  }\n"
      "}\n"
      "\n"
      "+ (IOSObjectArray *)values {\n"
      "  return $classname$_values();"
      "}\n"
      "\n"
      "+ ($classname$ *)valueOfWithNSString:(NSString *)name {\n"
      "  return $classname$_valueOfWithNSString_(name);\n"
      "}\n"
      "\n"
      "+ ($classname$ *)valueOfWithInt:(jint)value {\n"
      "  return $classname$_valueOfWithInt_(value);\n"
      "}\n"
      "\n"
      "+ ($classname$ *)forNumberWithInt:(jint)value {\n"
      "  return $classname$_forNumberWithInt_(value);\n"
      "}\n"
      "\n"
      "- (jint)getNumber {\n"
      "  return value_;\n"
      "}\n"
      "\n"
      "@end\n"
      "\n"
      "J2OBJC_CLASS_TYPE_LITERAL_SOURCE($classname$)\n"
      "\n"
      "IOSObjectArray *$classname$_values(void) {\n"
      "  $classname$_initialize();"
      "  return [IOSObjectArray arrayWithObjects:$classname$_values_"
      " count:$count$ type:$classname$_class_()];\n"
      "}\n"
      "\n"
      "$classname$ *$classname$_valueOfWithNSString_(NSString *name) {\n"
      "  $classname$_initialize();"
      "  for (jint i = 0; i < $count$; i++) {\n"
      "    $classname$ *e = $classname$_values_[i];\n"
      "    if ([name isEqual:[e name]]) {\n"
      "      return e;\n"
      "    }\n"
      "  }\n"
      "  @throw create_JavaLangIllegalArgumentException_initWithNSString_("
      "name);\n"
      "}\n"
      "\n"
      "$classname$ *$classname$_valueOfWithInt_(jint value) {\n"
      "  return $classname$_forNumberWithInt_(value);\n"
      "}\n"
      "\n"
      "$classname$ *$classname$_forNumberWithInt_(jint value) {\n"
      "  $classname$_initialize();"
      "  for (jint i = 0; i < $count$; i++) {\n"
      "    $classname$ *e = $classname$_values_[i];\n"
      "    if (value == [e getNumber]) {\n"
      "      return e;\n"
      "    }\n"
      "  }\n"
      "  return nil;\n"
      "}\n"
      "\n"
      "$classname$ *$classname$_fromOrdinal(NSUInteger ordinal) {\n"
      "  $classname$_initialize();\n"
      "  if (ordinal >= $count$) {\n"
      "    return nil;\n"
      "  }\n"
      "  return $classname$_values_[ordinal];\n"
      "}\n",
      "classname", CaseClassName(descriptor_), "count",
      SimpleItoa(descriptor_->field_count() + 1));
  for (int i = 0; i < descriptor_->field_count(); i++) {
    printer->Print(
        "\n$classname$ *$classname$_get_$name$(void) {\n"
        "  $classname$_initialize();\n"
        "  return $classname$_values_[$classname$_Enum_$name$];\n"
        "}\n",
        "classname", CaseClassName(descriptor_),
        "name", CaseValueName(descriptor_->field(i)));
  }
}

void OneofGenerator::GenerateMessageOrBuilder(io::Printer* printer) {
  printer->Print("\n"
      "- ($classname$ *)get$capitalized_name$Case;\n",
      "classname", CaseClassName(descriptor_),
      "capitalized_name", CapitalizedName(descriptor_));
}

}  // namespace j2objc
}  // namespace compiler
}  // namespace protobuf
}  // namespace google
