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

# Makefile for building the iOS jsr305 library.
#
# Author: Tom Ball

.PHONY: default clean

include environment.mk

JAVA_SOURCES = \
	javax/annotation/CheckForNull.java \
	javax/annotation/CheckForSigned.java \
	javax/annotation/CheckReturnValue.java \
	javax/annotation/concurrent/GuardedBy.java \
	javax/annotation/concurrent/Immutable.java \
	javax/annotation/concurrent/NotThreadSafe.java \
	javax/annotation/concurrent/ThreadSafe.java \
	javax/annotation/Detainted.java \
	javax/annotation/MatchesPattern.java \
	javax/annotation/meta/Exclusive.java \
	javax/annotation/meta/Exhaustive.java \
	javax/annotation/meta/TypeQualifier.java \
	javax/annotation/meta/TypeQualifierDefault.java \
	javax/annotation/meta/TypeQualifierNickname.java \
	javax/annotation/meta/TypeQualifierValidator.java \
	javax/annotation/meta/When.java \
	javax/annotation/Nonnegative.java \
	javax/annotation/Nonnull.java \
	javax/annotation/Nullable.java \
	javax/annotation/OverridingMethodsMustInvokeSuper.java \
	javax/annotation/ParametersAreNonnullByDefault.java \
	javax/annotation/PropertyKey.java \
	javax/annotation/RegEx.java \
	javax/annotation/Signed.java \
	javax/annotation/Syntax.java \
	javax/annotation/Tainted.java \
	javax/annotation/Untainted.java \
	javax/annotation/WillClose.java \
	javax/annotation/WillCloseWhenClosed.java \
	javax/annotation/WillNotClose.java

EXTRACTED_JAVA = $(JAVA_SOURCES:%=$(JAVA_SRC_DIR)/%)
JAVA_SOURCES_MANIFEST = $(BUILD_DIR)/java_sources.mf
OBJC_SOURCES_MANIFEST = $(BUILD_DIR)/objc_sources.mf

CREATE_JAR_NAME = j2objc_jsr305
CREATE_JAR_SOURCES = $(EXTRACTED_JAVA)
include ../make/create_jar.mk

TRANSLATE_JAVA_FULL = $(JAVA_SOURCES:%=$(JAVA_SRC_DIR)/%)
TRANSLATE_JAVA_RELATIVE = $(JAVA_SOURCES)
TRANSLATE_ARGS = -sourcepath $(JAVA_SRC_DIR)
include ../make/translate.mk

ARCH_HEADERS = $(TRANSLATE_HEADERS:$(GEN_OBJC_DIR)/%=$(ARCH_INCLUDE_DIR)/%)

FAT_LIB_NAME = jsr305
FAT_LIB_SOURCES_RELATIVE = $(TRANSLATE_SOURCES:$(GEN_OBJC_DIR)/%=%)
FAT_LIB_SOURCE_DIRS = $(GEN_OBJC_DIR)
FAT_LIB_COMPILE = $(J2OBJCC) -fobjc-abi-version=2 -fobjc-legacy-dispatch
include ../make/fat_lib.mk

FRAMEWORK_NAME = JSR305
include ../make/framework.mk

JSR305_LICENSE = LICENSE
JSR305_LICENSE_DIST = $(DIST_LICENSE_DIR)/jsr305_license.txt

default: lib

java: classes jar

# TODO(kstanger): Remove this.
classes: $(JAVA_SOURCES_MANIFEST)
	@echo Building class files
	@mkdir -p $(CLASSES_DIR)
	@$(JAVAC) -d $(CLASSES_DIR) \
	    -sourcepath $(SOURCE_PATH) -classpath $(CLASS_PATH) \
	    $$(cat $(JAVA_SOURCES_MANIFEST))

lib: $(FAT_LIBS)
	@:

$(JSR305_SRC_JAR): | java_deps_dist

$(BUILD_DIR)/.extracted: $(JSR305_SRC_JAR) | $(BUILD_DIR)
	@mkdir -p $(JAVA_SRC_DIR)
	@unzip -q -o -d $(JAVA_SRC_DIR) $(JSR305_SRC_JAR) $(JAVA_SOURCES)
	@touch $(EXTRACTED_JAVA)
	@touch $@

$(EXTRACTED_JAVA): $(BUILD_DIR)/.extracted
	@:

$(ARCH_INCLUDE_DIR)/%.h: $(GEN_OBJC_DIR)/%.h
	@mkdir -p $(@D)
	install -m 0644 $< $@

$(JSR305_LICENSE_DIST): $(JSR305_LICENSE)
	@mkdir -p $(@D)
	@install -m 0644 $< $@

$(JAVA_SOURCES_MANIFEST): $(EXTRACTED_JAVA) | $(BUILD_DIR)
	@echo "Building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@for i in $^; do echo $$i >> $@; done

java_sources_manifest: $(JAVA_SOURCES_MANIFEST)
	@:

$(OBJC_SOURCES_MANIFEST): $(TRANSLATE_JAVA_FULL) | $(BUILD_DIR)
	@echo "Building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@files='$(sort $(TRANSLATE_OBJC))' && for i in $$files; do \
	  echo $$i >> $@; \
	done

objc_sources_manifest: $(OBJC_SOURCES_MANIFEST)
	@:

$(BUILD_DIR):
	@mkdir -p $@

clean:
	@rm -rf $(BUILD_DIR) $(FAT_LIBS_DIST)
	@rm -rf $(JSR305_LICENSE_DIST) $(ARCH_INCLUDE_DIR)/javax/annotation
	@rm -rf $(XCFRAMEWORK_DIR)

dist: $(FAT_LIBS_DIST) $(ARCH_HEADERS) $(JSR305_LICENSE_DIST)
