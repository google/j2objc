#ifndef GOOGLE_PROTOBUF_COMPILER_J2OBJC_J2OBJC_EXTENSION_LITE_H_
#define GOOGLE_PROTOBUF_COMPILER_J2OBJC_J2OBJC_EXTENSION_LITE_H_

#include <set>
#include <string>

#include "google/protobuf/compiler/j2objc/common.h"
#include "google/protobuf/compiler/j2objc/j2objc_extension.h"

namespace google {
namespace protobuf {
namespace compiler {
namespace j2objc {

class ExtensionLiteGenerator : public ExtensionGenerator {
 public:
  explicit ExtensionLiteGenerator(const FieldDescriptor* descriptor);
  virtual ~ExtensionLiteGenerator();

  virtual void CollectSourceImports(std::set<std::string>* imports) const;
  virtual void GenerateMembersHeader(io::Printer* printer) const;
  virtual void GenerateSourceDefinition(io::Printer* printer) const;
  virtual void GenerateFieldData(io::Printer* printer) const;
  virtual void GenerateClassReference(io::Printer* printer) const;
  virtual void GenerateSourceInitializer(io::Printer* printer) const;
  virtual void GenerateRegistrationCode(io::Printer* printer) const;

 private:
  const FieldDescriptor* descriptor_;
  GOOGLE_DISALLOW_EVIL_CONSTRUCTORS(ExtensionLiteGenerator);
};

}  // namespace j2objc
}  // namespace compiler
}  // namespace protobuf
}  // namespace google

#endif  // GOOGLE_PROTOBUF_COMPILER_J2OBJC_J2OBJC_EXTENSION_LITE_H_
