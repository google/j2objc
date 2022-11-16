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

# Makefile for building the jre_concurrent subset library as an XCFramework.
#
# To use, first run "make -j8 dist" in this directory to build the static
# libraries and headers. Next, run "make -f jre_concurrent_framework.mk".

include environment.mk
include jre_sources.mk

.DEFAULT_GOAL = framework

FRAMEWORK_NAME = JSON
FRAMEWORK_HEADERS = $(JSON_PUBLIC_SOURCES:.java=.h)
STATIC_LIBRARY_NAME = json
STATIC_HEADERS_DIR = $(ARCH_INCLUDE_DIR)
include ../make/framework.mk

LIBS := $(call emit_library_rules,json,$(JSON_OBJS_RELATIVE))

dist: framework

lib: $(LIBS)

clean:
	@rm -rf $(FRAMEWORK_DIR)
