# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Makefile for building the runtime for custom J2ObjC protocol buffers.
#
# Author: Keith Stanger

SUFFIXES:

J2OBJC_ROOT = ../..

include $(J2OBJC_ROOT)/make/common.mk
include $(J2OBJC_ROOT)/make/j2objc_deps.mk

SRCS = \
  com/google/protobuf/ByteString.m \
  com/google/protobuf/CodedInputStream.mm \
  com/google/protobuf/CodedOutputStream.mm \
  com/google/protobuf/Descriptors.m \
  com/google/protobuf/Extension.m \
  com/google/protobuf/ExtensionLite.m \
  com/google/protobuf/ExtensionRegistry.mm \
  com/google/protobuf/ExtensionRegistryLite.mm \
  com/google/protobuf/GeneratedMessage.mm \
  com/google/protobuf/InvalidProtocolBufferException.m \
  com/google/protobuf/RepeatedField.m \
  com/google/protobuf/WireFormat.mm

SRC_DIR = $(CURDIR)/src

DIST_LIBRARY = $(ARCH_LIB_DIR)/libprotobuf_runtime.a
DIST_JAR = $(DIST_JAR_DIR)/protobuf_runtime.jar

OBJC_SOURCES_MANIFEST = $(BUILD_DIR)/objc_sources.mf


ifneq "$(findstring clean,$(MAKECMDGOALS))" "clean"
ifndef PROTOBUF_ROOT_DIR
$(error PROTOBUF_ROOT_DIR not defined)
endif
endif

PROTO_PATH = $(PROTOBUF_ROOT_DIR)/src
PROTOBUF_JAVA_DIR = $(PROTOBUF_ROOT_DIR)/java/src/main/java
DESCRIPTOR_PROTO = $(PROTO_PATH)/google/protobuf/descriptor.proto
DESCRIPTOR_PROTO_M = $(GEN_OBJC_DIR)/com/google/protobuf/DescriptorProtos.m
DESCRIPTOR_PROTO_H = $(DESCRIPTOR_PROTO_M:.m=.h)
DESCRIPTOR_PROTO_JAVA = $(DESCRIPTOR_PROTO_M:$(GEN_OBJC_DIR)/%.m=$(GEN_JAVA_DIR)/%.java)

CREATE_JAR_NAME = protobuf_runtime
CREATE_JAR_SOURCES := $(shell find $(PROTOBUF_JAVA_DIR) -name *.java) $(DESCRIPTOR_PROTO_JAVA)
CREATE_JAR_JAVAC_ARGS = -nowarn
include $(J2OBJC_ROOT)/make/create_jar.mk

SOURCE_HEADERS := $(shell find src -name '*.h')
SOURCE_HEADERS := $(SOURCE_HEADERS:src/%=%)
GENERATED_HEADERS = $(DESCRIPTOR_PROTO_H:$(GEN_OBJC_DIR)/%=%)
RELATIVE_HEADERS = $(SOURCE_HEADERS) $(GENERATED_HEADERS)
HEADERS = $(SOURCE_HEADERS:%=$(SRC_DIR)/%) $(GENERATED_HEADERS:%=$(GEN_OBJC_DIR)/%)
DIST_HEADERS = $(RELATIVE_HEADERS:%=$(ARCH_INCLUDE_DIR)/%)

SOURCES_FULL = $(SRCS:%=$(SRC_DIR)/%) $(DESCRIPTOR_PROTO_M)
ALL_SOURCES_FULL = $(HEADERS) $(SOURCES_FULL)

J2OBJCC = $(ARCH_BIN_DIR)/j2objcc

C_FLAGS = $(DEBUGFLAGS) -Werror -Wobjc-missing-property-synthesis -Wshorten-64-to-32 \
  $(ARCH_FLAGS) $(SDK_FLAGS) -fobjc-abi-version=2 -fobjc-legacy-dispatch -stdlib=libc++
INCLUDE_DIRS = src $(GEN_OBJC_DIR)

FAT_LIB_NAME = protobuf_runtime
FAT_LIB_SOURCES_RELATIVE = $(SRCS) com/google/protobuf/DescriptorProtos.m
FAT_LIB_SOURCE_DIRS = src $(GEN_OBJC_DIR)
FAT_LIB_COMPILE = $(J2OBJCC) $(C_FLAGS) $(INCLUDE_DIRS:%=-I%)
include $(J2OBJC_ROOT)/make/fat_lib.mk
fat_lib_dependencies: jre_emul_dist $(DESCRIPTOR_PROTO_H)

PROTOC = $(DIST_DIR)/j2objc_protoc
$(PROTOC): protobuf_compiler_dist
	@:

dist: $(DIST_JAR) $(DIST_LIBRARY) $(ARCH_BUILD_DIR)/.dist_headers
	@:

java: $(DIST_JAR)
	@:

generate: $(ALL_SOURCES_FULL)

objc_sources_manifest: $(OBJC_SOURCES_MANIFEST)
	@:

$(OBJC_SOURCES_MANIFEST):
	@mkdir -p $(@D)
	@echo "Building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@files='$(ALL_SOURCES_FULL)' && for i in $$files; do \
	  echo $$i >> $@; \
	done

clean:
	@rm -rf $(BUILD_DIR) $(DIST_JAR) $(DIST_LIBRARY) $(DIST_HEADERS)

$(DESCRIPTOR_PROTO_M): $(DESCRIPTOR_PROTO) $(PROTOC)
	@mkdir -p $(@D)
	$(PROTOC) --proto_path=$(PROTO_PATH) --j2objc_out=$(GEN_OBJC_DIR) $<

$(DESCRIPTOR_PROTO_H): $(DESCRIPTOR_PROTO_M)
	@:

$(DESCRIPTOR_PROTO_JAVA): $(DESCRIPTOR_PROTO) $(PROTOC)
	@mkdir -p $(@D)
	$(PROTOC) --proto_path=$(PROTO_PATH) --java_out=$(GEN_JAVA_DIR) $<

$(DIST_LIBRARY): $(FAT_LIB_LIBRARY)
	@mkdir -p $(@D)
	install -m 0644 $< $@

$(DIST_JAR): $(CREATE_JAR_RESULT)
	@mkdir -p $(@D)
	install -m 0644 $< $@

$(ARCH_BUILD_DIR)/.dist_headers: $(HEADERS)
	@mkdir -p $(ARCH_INCLUDE_DIR)
	(cd src && tar cf - $(SOURCE_HEADERS)) | ( cd $(ARCH_INCLUDE_DIR); tar xfpm - )
	(cd $(GEN_OBJC_DIR) && tar cf - $(GENERATED_HEADERS)) | \
	  ( cd $(ARCH_INCLUDE_DIR); tar xfpm - )
	@touch $@
