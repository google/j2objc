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

.PHONY: default clean

include environment.mk

#
TEST_JAVA_SRC_DIR = tests
TESTS_DIR = $(BUILD_DIR)/tests

TEST_JAVA_SOURCES = $(TEST_JAVA_SRC_DIR)/com/google/j2objc/mockito/MockMakerTest.java
TEST_OBJC_SOURCES = $(TEST_JAVA_SOURCES:$(TEST_JAVA_SRC_DIR)/%.java=$(TESTS_DIR)/%.m)
TEST_CLASSES = com.google.j2objc.mockito.MockMakerTest

CLASS_PATH_OPT = -classpath $(DIST_JAR_DIR)/$(JUNIT_JAR):$(DIST_JAR_DIR)/$(MOCKITO_JAR):$(DIST_JAR_DIR)/$(HAMCREST_JAR)
LINKER_OPT = -ObjC -ljunit -lmockito -ljre_emul -I$(TESTS_DIR)

TEST_BIN = $(TESTS_DIR)/mockito_tests

#
default: test

test: $(TEST_BIN)
	$< org.junit.runner.JUnitCore $(TEST_CLASSES)

clean:
	rm -rf $(TESTS_DIR)

#
$(TEST_BIN): $(TEST_OBJC_SOURCES) | $(TESTS_DIR)
	$(DIST_DIR)/j2objcc $(LINKER_OPT) -o $@ $?

$(TESTS_DIR)/%.m: $(TEST_JAVA_SRC_DIR)/%.java
	$(DIST_DIR)/j2objc $(CLASS_PATH_OPT) -d $(TESTS_DIR) $<

$(TESTS_DIR):
	@mkdir -p $@
