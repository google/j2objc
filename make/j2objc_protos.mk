# Defines the targets for building custom j2objc protos.
#
# The including makefile must define the variables:
#   J2OBJC_PROTOS_INPUTS
#   J2OBJC_PROTOS_PATHS
# The including makefile may define the optional variables:
#   J2OBJC_PROTOS_NAME
#   J2OBJC_PROTOS_PREFIX_FILES
#   J2OBJC_PROTOS_GENERATE_CLASS_MAPPINGS
#
# The following variables are defined by this include:
#   J2OBJC_PROTOS_JAVA
#
# Author: Keith Stanger

J2OBJC_PROTOS_GENERATED_FILES_INCLUDE = $(BUILD_DIR)/j2objc_protos_generated_files.mk
ifndef IS_CLEAN_GOAL
ifeq ($(wildcard $(J2OBJC_PROTOS_GENERATED_FILES_INCLUDE)),)
# Avoid a warning from the include directive that the file doesn't exist, then
# immediately delete the file so that make rebuilds it correctly.
$(shell mkdir -p $(dir $(J2OBJC_PROTOS_GENERATED_FILES_INCLUDE)))
$(shell touch $(J2OBJC_PROTOS_GENERATED_FILES_INCLUDE))
include $(J2OBJC_PROTOS_GENERATED_FILES_INCLUDE)
$(shell rm $(J2OBJC_PROTOS_GENERATED_FILES_INCLUDE))
else
include $(J2OBJC_PROTOS_GENERATED_FILES_INCLUDE)
endif
endif

J2OBJC_PROTOS_NAME ?= $(CURDIR)

J2OBJC_PROTOS_JAVA_TARGET = $(GEN_JAVA_DIR)/.j2objc_protos_java_mark
J2OBJC_PROTOS_JAVA = $(GENERATED_JAVA:%=$(GEN_JAVA_DIR)/%)

J2OBJC_PROTOS_SOURCES = $(GENERATED_SOURCES:%=$(GEN_OBJC_DIR)/%)
J2OBJC_PROTOS_HEADERS = $(GENERATED_HEADERS:%=$(GEN_OBJC_DIR)/%)
J2OBJC_PROTOS_OBJC = $(J2OBJC_PROTOS_SOURCES) $(J2OBJC_PROTOS_HEADERS)

J2OBJC_PROTOS_OBJC_TARGET = $(GEN_OBJC_DIR)/.j2objc_protos_objc_mark

J2OBJC_PROTOS_PLUGIN = $(DIST_DIR)/j2objc_protoc_plugin
$(J2OBJC_PROTOS_PLUGIN): protobuf_compiler_dist
	@:

J2OBJC_PROTOS_RELATIVE_INPUTS := $(realpath $(J2OBJC_PROTOS_INPUTS))
$(foreach path,$(realpath $(J2OBJC_PROTOS_PATHS)),\
  $(eval J2OBJC_PROTOS_RELATIVE_INPUTS := $(patsubst $(path)/%,%,$(J2OBJC_PROTOS_RELATIVE_INPUTS))))

$(call check_protobuf_dir)

J2OBJC_PROTOS_COMPILE = $(PROTOBUF_PROTOC) $(J2OBJC_PROTOS_PATHS:%=--proto_path=%)

J2OBJC_PROTOS_JAVA_OPTIONS_STR = $(subst $(space),$(comma),$(strip $(J2OBJC_PROTOS_JAVA_OPTIONS)))

ifdef J2OBJC_PROTOS_GENERATE_CLASS_MAPPINGS
J2OBJC_PROTOS_OPTIONS += generate_class_mappings
J2OBJC_PROTOS_CLASS_MAPPING_FILES = \
  $(J2OBJC_PROTOS_RELATIVE_INPUTS:%.proto=$(GEN_OBJC_DIR)/%.clsmap.properties)
endif

ifdef J2OBJC_PROTOS_PREFIX_FILES
J2OBJC_PROTOS_OPTIONS += $(J2OBJC_PROTOS_PREFIX_FILES:%=prefixes=%)
endif

J2OBJC_PROTOS_OPTIONS_STR = $(subst $(space),$(comma),$(strip $(J2OBJC_PROTOS_OPTIONS)))

J2OBJC_PROTOS_COMPILE_JAVA = $(J2OBJC_PROTOS_COMPILE) \
  --java_out=$(J2OBJC_PROTOS_JAVA_OPTIONS_STR):$(GEN_JAVA_DIR) $(J2OBJC_PROTOS_INPUTS)
J2OBJC_PROTOS_COMPILE_J2OBJC = $(J2OBJC_PROTOS_COMPILE) \
  --plugin=protoc-gen-j2objc=$(J2OBJC_PROTOS_PLUGIN) \
  --j2objc_out=$(J2OBJC_PROTOS_OPTIONS_STR):$(GEN_OBJC_DIR) $(J2OBJC_PROTOS_INPUTS)

$(J2OBJC_PROTOS_GENERATED_FILES_INCLUDE): $(J2OBJC_PROTOS_INPUTS)
	@mkdir -p $(BUILD_DIR)
	@echo Generating objc protos include file for $(J2OBJC_PROTOS_NAME).
	@$(J2OBJC_ROOT)/scripts/gen_proto_library_include.py $^ > $@

$(J2OBJC_PROTOS_JAVA_TARGET): $(J2OBJC_PROTOS_INPUTS)
	@echo Generating Java protos for $(J2OBJC_PROTOS_NAME)
	@mkdir -p $(GEN_JAVA_DIR)
	@$(J2OBJC_PROTOS_COMPILE_JAVA)
	@touch $@

j2objc_protos_java $(J2OBJC_PROTOS_JAVA): $(J2OBJC_PROTOS_JAVA_TARGET)
	@:

$(J2OBJC_PROTOS_OBJC_TARGET): $(J2OBJC_PROTOS_INPUTS) $(J2OBJC_PROTOS_PLUGIN) \
    $(J2OBJC_PROTOS_PREFIX_FILES)
	@echo Generating J2ObjC protos for $(OBJC_PROTOS_NAME)
	@mkdir -p $(GEN_OBJC_DIR)
	@$(J2OBJC_PROTOS_COMPILE_J2OBJC)
	@touch $@

j2objc_protos_objc $(J2OBJC_PROTOS_OBJC) $(J2OBJC_PROTOS_CLASS_MAPPING_FILES): \
    $(J2OBJC_PROTOS_OBJC_TARGET)
	@:
