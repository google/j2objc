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

.PHONY: translator jre_emul dist test

PROJECT_ROOT = .

include make/detect_xcode.mk

MAN_DIR = doc/man
MAN_PAGES = $(MAN_DIR)/j2objc.1 $(MAN_DIR)/j2objcc.1

default: dist

$(DIST_DIR)/j2objc: scripts/j2objc.sh
	@install -C $< $@

$(DIST_DIR)/j2objcc: scripts/j2objcc.sh
	@install -C $< $@

install-scripts: $(DIST_DIR)/j2objc $(DIST_DIR)/j2objcc
	@:

install-man-pages: $(MAN_PAGES)
	@mkdir -p $(DIST_DIR)/man/man1
	@install -C -m 0644 $? $(DIST_DIR)/man/man1

translator_dist: translator jre_emul_jar_dist install-scripts

translator:
	@cd translator && $(MAKE) dist

jre_emul_jar_dist:
	@cd jre_emul && $(MAKE) emul_jar_dist

jre_emul_dist: translator_dist jre_emul

jre_emul:
	@cd jre_emul && $(MAKE) dist

junit_dist: translator_dist jre_emul_dist
	@cd junit && $(MAKE) dist

dist: translator_dist jre_emul_dist junit_dist install-man-pages


clean:
	@rm -rf $(DIST_DIR)
	@cd translator && $(MAKE) clean
	@cd jre_emul && $(MAKE) clean
	@cd junit && $(MAKE) clean

test:
	@cd translator && $(MAKE) test

test_all: test
	@cd jre_emul && $(MAKE) -f tests.mk
