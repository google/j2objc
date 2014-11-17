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

# Builds a J2ObjC translated Guava library.
#
# Author: Keith Stanger

include environment.mk

TEST_SOURCES = \
    com/google/common/collect/CountTest.java

EXTRACTED_TESTS = $(TEST_SOURCES:%=$(GEN_JAVA_DIR)/%)
OBJS = $(TEST_SOURCES:%.java=$(OBJS_DIR)/%.o)
TEST_BIN = $(BUILD_DIR)/guava_tests
TEST_CLASSES = $(subst /,.,$(TEST_SOURCES:%.java=%))

EXTRACT_TARGET = $(BUILD_DIR)/.extracted

INCLUDE_DIRS = $(GEN_OBJC_DIR)

J2OBJCC = $(ARCH_BIN_DIR)/j2objcc

TRANSLATE_JAVA_FULL = $(EXTRACTED_TESTS)
TRANSLATE_JAVA_RELATIVE = $(TEST_SOURCES)
TRANSLATE_ARGS = -cp $(DIST_LIB_DIR)/j2objc_guava.jar:$(DIST_LIB_DIR)/$(JUNIT_JAR) -q
include ../make/translate.mk

test: $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore $(TEST_CLASSES)

$(EXTRACT_TARGET): $(GUAVA_TESTS_SRC_JAR)
	@echo Extracting test sources from .jar.
	@unzip -q -o -d $(GEN_JAVA_DIR) $(GUAVA_TESTS_SRC_JAR) $(TEST_SOURCES)
	@cd $(GEN_JAVA_DIR) && touch $(TEST_SOURCES)
	@touch $@

$(EXTRACTED_TESTS): $(EXTRACT_TARGET)
	@:

$(OBJS_DIR)/%.o: $(GEN_OBJC_DIR)/%.m
	@mkdir -p $(@D)
	$(J2OBJCC) -c $(INCLUDE_DIRS:%=-I%) -o $@ $<

$(TEST_BIN): $(OBJS)
	$(J2OBJCC) -ObjC -l junit -l jsr305 -L $(BUILD_DIR) -l guava -o $@ $<
