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

J2OBJC_ROOT = ../..

include $(J2OBJC_ROOT)/make/common.mk

SRCS = \
  google/protobuf/compiler/j2objc/j2objc_enum.cc \
  google/protobuf/compiler/j2objc/j2objc_extension.cc \
  google/protobuf/compiler/j2objc/j2objc_field.cc \
  google/protobuf/compiler/j2objc/j2objc_file.cc \
  google/protobuf/compiler/j2objc/j2objc_generator.cc \
  google/protobuf/compiler/j2objc/j2objc_helpers.cc \
  google/protobuf/compiler/j2objc/j2objc_message.cc \
  google/protobuf/compiler/j2objc/j2objc_message_lite.cc \
  google/protobuf/compiler/j2objc/j2objc_oneof.cc

OBJS = $(SRCS:%.cc=$(BUILD_DIR)/%.o)

MAIN_OBJ = $(BUILD_DIR)/google/protobuf/compiler/j2objc/main.o
PLUGIN_OBJ = $(BUILD_DIR)/google/protobuf/compiler/j2objc/plugin_main.o

DEPS = $(OBJS:.o=.d)
-include $(DEPS)

PROTOC_EXE = $(BUILD_DIR)/j2objc_protoc
PROTOC_EXE_DIST = $(DIST_DIR)/j2objc_protoc
PROTOC_PLUGIN = $(BUILD_DIR)/j2objc_protoc_plugin
PROTOC_PLUGIN_DIST = $(DIST_DIR)/j2objc_protoc_plugin

DESCRIPTOR_PROTO = google/protobuf/j2objc-descriptor.proto
DESCRIPTOR_PATH_DIST = $(DIST_DIR)/include/proto
DESCRIPTOR_PROTO_DIST = $(DESCRIPTOR_PATH_DIST)/$(DESCRIPTOR_PROTO)

$(call check_protobuf_dir)

PROTOBUF_LIB = -l protobuf
PROTOC_LIB = -l protoc
ABSL_LIBS = \
  -l absl_hash \
	-l absl_log_internal_check_op \
	-l absl_log_internal_message \
	-l absl_raw_hash_set \
	-l absl_raw_logging_internal \
	-l absl_spinlock_wait \
	-l absl_strings

CXXFLAGS = -x c++ -std=c++17 -stdlib=libc++ -Isrc -I$(PROTOBUF_INCLUDE_PATH) \
  -Wno-deprecated-declarations
LDFLAGS = -L $(PROTOBUF_LIB_PATH) $(PROTOBUF_LIB) $(PROTOC_LIB) \
  $(ABSL_LIBS)

dist: $(PROTOC_EXE_DIST) $(PROTOC_PLUGIN_DIST) $(DESCRIPTOR_PROTO_DIST)
	@:

clean:
	@rm -rf $(BUILD_DIR) $(PROTOC_EXE_DIST) $(PROTOC_PLUGIN_DIST) \
	  $(DESCRIPTOR_PATH_DIST)

$(PROTOC_EXE_DIST): $(PROTOC_EXE)
	@mkdir -p $(DIST_DIR)
	install $< $@

$(PROTOC_PLUGIN_DIST): $(PROTOC_PLUGIN)
	@mkdir -p $(DIST_DIR)
	install $< $@

$(BUILD_DIR)/%.o: src/%.cc
	@mkdir -p $(@D)
	$(CXX) -MD -c -o $@ $(CXXFLAGS) $<

$(PROTOC_EXE): $(OBJS) $(MAIN_OBJ)
	$(CXX) $(LDFLAGS) -o $@ $^

$(PROTOC_PLUGIN): $(OBJS) $(PLUGIN_OBJ)
	$(CXX) $(LDFLAGS) -o $@ $^

$(DESCRIPTOR_PROTO_DIST): src/$(DESCRIPTOR_PROTO)
	@mkdir -p $$(dirname $@)
	install $< $@
