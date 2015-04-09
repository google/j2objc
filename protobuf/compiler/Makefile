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

SUFFIXES:

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
  google/protobuf/stubs/strutil.cc

COMPILER_SRC_PATH = google/protobuf/compiler/j2objc

OBJS = $(SRCS:%.cc=$(BUILD_DIR)/%.o)

MAIN_OBJ = $(BUILD_DIR)/google/protobuf/compiler/j2objc/main.o
PLUGIN_OBJ = $(BUILD_DIR)/google/protobuf/compiler/j2objc/plugin_main.o

DEPS = $(OBJS:.o=.d)
-include $(DEPS)

PROTOC_EXE = $(BUILD_DIR)/j2objc_protoc
PROTOC_EXE_DIST = $(DIST_DIR)/j2objc_protoc
PROTOC_PLUGIN = $(BUILD_DIR)/j2objc_protoc_plugin
PROTOC_PLUGIN_DIST = $(DIST_DIR)/j2objc_protoc_plugin

CXXFLAGS = -x c++ -stdlib=libc++ -Isrc -I../google/src
LDFLAGS = -l protobuf -l protoc /usr/lib/libc++.dylib

dist: $(PROTOC_EXE_DIST) $(PROTOC_PLUGIN_DIST)
	@:

clean:
	@rm -rf $(BUILD_DIR) $(PROTOC_EXE_DIST) $(PROTOC_PLUGIN_DIST)

$(PROTOC_EXE_DIST): $(PROTOC_EXE)
	@mkdir -p $(DIST_DIR)
	install $< $@

$(PROTOC_PLUGIN_DIST): $(PROTOC_PLUGIN)
	@mkdir -p $(DIST_DIR)
	install $< $@

$(BUILD_DIR)/%.o: src/%.cc
	@mkdir -p $(@D)
	$(CXX) -MD -c -o $@ $(CXXFLAGS) $<

$(BUILD_DIR)/%.o: ../google/src/%.cc
	@mkdir -p $(@D)
	$(CXX) -MD -c -o $@ $(CXXFLAGS) $<

# Create a binary replacement for protoc, which supports --j2objc_out.
$(PROTOC_EXE): $(OBJS) $(MAIN_OBJ)
	$(CXX) $(LDFLAGS) -o $@ $^

$(PROTOC_PLUGIN): $(OBJS) $(PLUGIN_OBJ)
	$(CXX) $(LDFLAGS) -o $@ $^
