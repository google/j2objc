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

# Builds the j2objc annotations as a .jar file.
#
# Author: Tom Ball, Keith Stanger

J2OBJC_ROOT = ..

include ../make/common.mk
include classes.mk

JAR_NAME = j2objc_annotations.jar
JAR = $(BUILD_DIR)/$(JAR_NAME)
DIST_JAR = $(DIST_JAR_DIR)/$(JAR_NAME)

CLASSES_DIR = $(BUILD_DIR)/classes
JAVA_SOURCE_DIR = src/main/java

SOURCE_JAVA_FULL = $(ANNOTATION_SOURCE_JAVA:%=$(JAVA_SOURCE_DIR)/%)

$(BUILD_DIR) $(CLASSES_DIR) $(DIST_JAR_DIR):
	@mkdir -p $@

$(JAR): $(SOURCE_JAVA_FULL) | $(BUILD_DIR) $(CLASSES_DIR)
	@echo Building j2objc annotations
	@$(JAVAC) -encoding UTF-8 -d $(CLASSES_DIR) -source 1.7 -target 1.7 \
	    -nowarn $^
	@jar cf $(JAR) -C $(CLASSES_DIR) .

$(DIST_JAR): $(JAR) | $(DIST_JAR_DIR)
	@install -m 0644 $< $@

dist: $(DIST_JAR)
	@:

clean:
	@rm -rf $(BUILD_DIR) $(DIST_JAR)
