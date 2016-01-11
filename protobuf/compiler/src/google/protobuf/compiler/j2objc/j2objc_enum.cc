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

#include <string>

#include <google/protobuf/io/printer.h>
#include <google/protobuf/descriptor.h>
#include <google/protobuf/stubs/strutil.h>
#include <google/protobuf/compiler/j2objc/j2objc_helpers.h>

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

void EnumGenerator::GenerateHeader(io::Printer* printer) {
  printer->Print(
    "\ntypedef NS_ENUM(NSUInteger, $classname$) {\n",
    "classname", CEnumName(descriptor_));
  printer->Indent();

  for (int i = 0; i < canonical_values_.size(); i++) {
    printer->Print(
      "$name$ = $value$,\n",
      "name", EnumValueName(canonical_values_[i]),
      "value", SimpleItoa(i));
  }

  printer->Outdent();
  printer->Print("};\n\n");

  for (int i = 0; i < canonical_values_.size(); i++) {
    printer->Print(
        "#define $classname$_$name$_VALUE $value$\n",
        "classname", ClassName(descriptor_),
        "name", canonical_values_[i]->name(),
        "value", SimpleItoa(canonical_values_[i]->number()));
  }

  printer->Print(
      "\n"
      "@interface $classname$ :"
      " JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> {\n"
      " @private\n"
      "  jint value_;\n"
      "}\n"
      "\n"
      "+ (IOSObjectArray *)values;\n"
      "+ ($classname$ *)valueOfWithNSString:(NSString *)name;\n"
      "+ ($classname$ *)valueOfWithInt:(jint)value;\n"
      "- (jint)getNumber;\n"
      "\n"
      "@end\n"
      "\n"
      "J2OBJC_STATIC_INIT($classname$)\n"
      "\n"
      "J2OBJC_TYPE_LITERAL_HEADER($classname$)\n"
      "\n"
      "FOUNDATION_EXPORT $classname$ *$classname$_values_[];\n"
      "\n"
      "FOUNDATION_EXPORT IOSObjectArray *$classname$_values();\n"
      "FOUNDATION_EXPORT $classname$ *$classname$_valueOfWithNSString_("
          "NSString *name);\n"
      "FOUNDATION_EXPORT $classname$ *$classname$_valueOfWithInt_("
          "jint value);\n\n",
      "classname", ClassName(descriptor_));

  for (int i = 0; i < canonical_values_.size(); i++) {
    printer->Print(
        "#define $classname$_$name$ $classname$_values_[$nativeenumname$]\n"
        "J2OBJC_ENUM_CONSTANT_GETTER($classname$, $name$)\n",
        "classname", ClassName(descriptor_),
        "name", canonical_values_[i]->name(),
        "nativeenumname", EnumValueName(canonical_values_[i]));
  }
}

const int kMaxRowChars = 80;

void EnumGenerator::GenerateSource(io::Printer* printer) {
  printer->Print(
      "\nJ2OBJC_INITIALIZED_DEFN($classname$)\n"
      "\n"
      "$classname$ *$classname$_values_[$count$];\n"
      "\n"
      "static ComGoogleProtobufDescriptors_EnumDescriptor"
          " *$classname$_descriptor = nil;\n"
      "\n"
      "@implementation $classname$\n"
      "\n"
      "+ (void)initialize {\n"
      "  if (self == [$classname$ class]) {\n"
      "    static NSString *names[] = {",
      "classname", ClassName(descriptor_),
      "count", SimpleItoa(canonical_values_.size()));

  // Count characters and only add line breaks when the line exceeds the max.
  int row_chars = kMaxRowChars + 1;
  for (int i = 0; i < canonical_values_.size(); i++) {
    string name = canonical_values_[i]->name();
    size_t added_chars = name.length() + 5;
    if (row_chars + added_chars > kMaxRowChars) {
      printer->Print("\n     ");
      row_chars = 5;
    };
    printer->Print(" @\"$name$\",", "name", canonical_values_[i]->name());
    row_chars += added_chars;
  }
  printer->Print("\n"
      "    };\n"
      "    static jint int_values[] = {");
  row_chars = kMaxRowChars + 1;
  for (int i = 0; i < canonical_values_.size(); i++) {
    string value = SimpleItoa(canonical_values_[i]->number());
    size_t added_chars = value.length() + 2;
    if (row_chars + added_chars > kMaxRowChars) {
      printer->Print("\n     ");
      row_chars = 5;
    };
    printer->Print(" $value$,", "value", value);
    row_chars += added_chars;
  }

  printer->Print("\n"
      "    };\n"
      "    $classname$_descriptor = "
          "CGPInitializeEnumType(self, $count$, $classname$_values_, names,"
          " int_values);\n"
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
      "- (jint)getNumber {\n"
      "  return value_;\n"
      "}\n"
      "\n"
      "+ (ComGoogleProtobufDescriptors_EnumDescriptor *)getDescriptor {\n"
      "  return $classname$_descriptor;\n"
      "}\n"
      "\n"
      "- (ComGoogleProtobufDescriptors_EnumValueDescriptor *)"
          "getValueDescriptor {\n"
      "  return $classname$_descriptor->values_->buffer_[[self ordinal]];\n"
      "}\n"
      "\n"
      "@end\n"
      "\n"
      "J2OBJC_CLASS_TYPE_LITERAL_SOURCE($classname$)\n"
      "\n"
      "IOSObjectArray *$classname$_values() {\n"
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
      "  @throw [[[JavaLangIllegalArgumentException alloc] initWithNSString:name] autorelease];\n"
      "}\n"
      "\n"
      "$classname$ *$classname$_valueOfWithInt_(jint value) {\n"
      "  $classname$_initialize();"
      "  for (jint i = 0; i < $count$; i++) {\n"
      "    $classname$ *e = $classname$_values_[i];\n"
      "    if (value == [e getNumber]) {\n"
      "      return e;\n"
      "    }\n"
      "  }\n"
      "  @throw [[[JavaLangIllegalArgumentException alloc]\n"
      "      initWithNSString:[NSString stringWithFormat:@\"%d\", value]] autorelease];\n"
      "}\n\n",
      "classname", ClassName(descriptor_),
      "count", SimpleItoa(canonical_values_.size()));
}

}  // namespace j2objc
}  // namespace compiler
}  // namespace protobuf
}  // namespace google
