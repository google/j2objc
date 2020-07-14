# Copyright 2011 Google Inc. All Rights Reserved.
#
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

# Makefile for building j2objc.  It's purpose is as a subproject in an Xcode
# project.
#
# Author: Tom Ball

.PHONY: translator dist test

default: dist

J2OBJC_ROOT = .

include make/common.mk
include make/j2objc_deps.mk

# Force test targets to be run sequentially to avoid interspersed output.
ifdef IS_TEST_GOAL
.NOTPARALLEL:
endif

MAN_DIR = doc/man
MAN_PAGES = $(MAN_DIR)/j2objc.1 $(MAN_DIR)/j2objcc.1

install-man-pages: $(MAN_PAGES)
	@mkdir -p $(DIST_DIR)/man/man1
	@install -C -m 0644 $? $(DIST_DIR)/man/man1

EXTRA_DIST_FILES = LICENSE WORKSPACE BUILD

$(DIST_DIR)/%: %.dist
	@mkdir -p $(@D)
	@install -C -m 644 $< $@

$(DIST_DIR)/%: %
	@mkdir -p $(@D)
	@install -C -m 644 $< $@

install-extras: $(EXTRA_DIST_FILES:%=$(DIST_DIR)/%)
	@:

frameworks: dist
	@cd jre_emul && $(MAKE) framework
	@cd junit && $(MAKE) framework
	@cd jsr305 && $(MAKE) framework
	@cd inject/javax_inject && $(MAKE) framework
	@cd guava && $(MAKE) framework
	@cd testing/mockito && $(MAKE) framework
	@cd xalan && $(MAKE) framework

all_frameworks: frameworks protobuf_dist
	@cd protobuf/runtime && $(MAKE) framework

dist: print_environment translator_dist jre_emul_dist junit_dist jsr305_dist \
  javax_inject_dist guava_dist mockito_dist cycle_finder_dist \
  xalan_dist install-man-pages install-extras

protobuf_dist: protobuf_compiler_dist protobuf_runtime_dist

all_dist: dist all_frameworks examples_dist

clean:
	@rm -rf $(BUILD_DIR) $(DIST_DIR)
	@cd annotations && $(MAKE) clean
	@cd java_deps && $(MAKE) clean
	@cd translator && $(MAKE) clean
	@cd jre_emul && $(MAKE) clean
	@cd junit && $(MAKE) clean
	@cd jsr305 && $(MAKE) clean
	@cd inject/javax_inject && $(MAKE) clean
	@cd guava && $(MAKE) clean
	@cd testing/mockito && $(MAKE) clean
	@cd cycle_finder && $(MAKE) clean
	@cd protobuf/runtime && $(MAKE) clean
	@cd protobuf/compiler && $(MAKE) clean
	@cd protobuf/tests && $(MAKE) clean
	@cd xalan && $(MAKE) clean

test_translator: annotations_dist java_deps_dist jre_emul_dist
	@cd translator && $(MAKE) test
	@cd translator && $(MAKE) regression-test

test_jre_emul: jre_emul_dist junit_dist
	@cd jre_emul && $(MAKE) -f tests.mk test

test_jre_cycles: cycle_finder_dist
	@$(MAKE) -C jre_emul -f java.mk find_cycles

test_junit_cycles: cycle_finder_dist
	@cd junit && $(MAKE) find_cycles

test_cycle_finder: cycle_finder_dist
	@cd cycle_finder && $(MAKE) test

test: test_translator test_jre_emul test_cycle_finder test_jre_cycles

test_protobuf: junit_dist protobuf_compiler_dist protobuf_runtime_dist
	@cd protobuf/tests && $(MAKE) test

test_all: test test_protobuf

examples_dist: install_examples

copy_examples:
	@cp -r examples $(DIST_DIR)

install_examples: copy_examples
	@sed -i '' 's/\/dist//' $(DIST_DIR)/examples/Hello/config.xcconfig
	@sed -i '' 's/\/dist//' $(DIST_DIR)/examples/Hello/Hello.xcodeproj/project.pbxproj
	@sed -i '' 's/\/dist//' $(DIST_DIR)/examples/HelloSwift/config.xcconfig
	@sed -i '' 's/\/dist//' $(DIST_DIR)/examples/HelloSwift/HelloSwift.xcodeproj/project.pbxproj
	@sed -i '' 's/\/dist//' $(DIST_DIR)/examples/protobuf/Makefile
	@sed -i '' 's/\<path to local j2objc distribution\>/..\/../' \
	  $(DIST_DIR)/examples/Contacts/WORKSPACE

print_environment:
	@echo Locale: $${LANG}
	@echo `uname -a`
	@echo `xcodebuild -version`
	@echo `xcrun cc -v`
	@echo Environment:
	@env | grep -v '^_' | sort
	@echo
