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

SUFFIXES:
.PHONY: translator dist test

default: dist

# Force test targets to be run sequentially to avoid interspersed output.
ifeq "$(findstring test,$(MAKECMDGOALS))" "test"
.NOTPARALLEL:
endif

J2OBJC_ROOT = .

include make/common.mk
include make/j2objc_deps.mk

MAN_DIR = doc/man
MAN_PAGES = $(MAN_DIR)/j2objc.1 $(MAN_DIR)/j2objcc.1

install-man-pages: $(MAN_PAGES)
	@mkdir -p $(DIST_DIR)/man/man1
	@install -C -m 0644 $? $(DIST_DIR)/man/man1

dist: print_environment translator_dist jre_emul_dist junit_dist jsr305_dist \
	  javax_inject_dist guava_dist mockito_dist cycle_finder_dist install-man-pages

protobuf_dist: protobuf_compiler_dist protobuf_runtime_dist


all_dist: dist protobuf_dist

clean:
	@rm -rf $(DIST_DIR)
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

test_translator: annotations_dist java_deps_dist
	@cd translator && $(MAKE) test

test_jre_emul: jre_emul_dist junit_dist
	@cd jre_emul && $(MAKE) -f tests.mk test

test_jre_cycles: cycle_finder_dist
	@cd jre_emul && $(MAKE) find_cycles

test_junit_cycles: cycle_finder_dist
	@cd junit && $(MAKE) find_cycles

test_guava_cycles: cycle_finder_dist jre_emul_java_manifest
	@cd guava && $(MAKE) find_cycles

test_cycle_finder: cycle_finder_dist
	@cd cycle_finder && $(MAKE) test

test: test_translator test_jre_emul \
   test_cycle_finder test_jre_cycles test_guava_cycles test_junit_cycles

test_protobuf: junit_dist protobuf_compiler_dist protobuf_runtime_dist
	@cd protobuf/tests && $(MAKE) test


test_all: test test_protobuf

print_environment:
	@echo Locale: $${LANG}
	@echo `uname -a`
	@echo `xcodebuild -version`
	@echo `xcrun cc -v`
	@echo Environment:
	@env | grep -v '^_' | sort
	@echo
