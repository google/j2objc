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

# Template for determining the list of transitive dependencies for a set of java
# source files. As a side-effect of determining the dependencies a jar of the
# compiled java sources is produced.
#
# The including makefile may define these variables:
#   TRANSITIVE_JAVA_DEPS_NAME
#   TRANSITIVE_JAVA_DEPS_ROOT_SOURCES
#     - List of relative java sources.
#   TRANSITIVE_JAVA_DEPS_SOURCEPATH
#   TRANSITIVE_JAVA_DEPS_DEPENDENCIES
#     - Concrete targets that should force re-generation.
#   TRANSITIVE_JAVA_DEPS_JAVAC_ARGS
#
# The following variables will be defined by this include:
#   TRANSITIVE_JAVA_DEPS_FULL_SOURCES
#     - Relative list of all java files depended on by the root sources.
#   TRANSITIVE_JAVA_DEPS_JAR
#     - The .jar file that is produced.
#
# The including makefile may also add dependent order-only targets by adding
# requirements to the "transitive_java_deps_dependencies" target.
#
# Author: Keith Stanger

TRANSITIVE_JAVA_DEPS_JAR = $(BUILD_DIR)/$(TRANSITIVE_JAVA_DEPS_NAME).jar
TRANSITIVE_JAVA_DEPS_INCLUDE = $(BUILD_DIR)/$(TRANSITIVE_JAVA_DEPS_NAME)_transitive.mk
TRANSITIVE_JAVA_DEPS_STAGE_DIR = /tmp/j2objc_$(TRANSITIVE_JAVA_DEPS_NAME)
TRANSITIVE_JAVA_DEPS_ROOT_LIST = $(BUILD_DIR)/$(TRANSITIVE_JAVA_DEPS_NAME)_root_list

TRANSITIVE_JAVA_DEPS_SOURCEPATH_LIST = $(subst :, ,$(TRANSITIVE_JAVA_DEPS_SOURCEPATH))

ifndef IS_CLEAN_GOAL
ifeq ($(wildcard $(TRANSITIVE_JAVA_DEPS_INCLUDE)),)
# Avoid a warning from the include directive that the file doesn't exist, then
# immediately delete the file so that make rebuilds it correctly.
$(shell mkdir -p $(dir $(TRANSITIVE_JAVA_DEPS_INCLUDE)))
$(shell touch $(TRANSITIVE_JAVA_DEPS_INCLUDE))
include $(TRANSITIVE_JAVA_DEPS_INCLUDE)
$(shell rm $(TRANSITIVE_JAVA_DEPS_INCLUDE))
else
include $(TRANSITIVE_JAVA_DEPS_INCLUDE)
endif
endif

vpath %.java $(TRANSITIVE_JAVA_DEPS_SOURCEPATH)

TRANSITIVE_JAVA_DEPS_SOURCEPATH_ARG = \
    $(if $(TRANSITIVE_JAVA_DEPS_SOURCEPATH),-sourcepath $(TRANSITIVE_JAVA_DEPS_SOURCEPATH),)

jar: $(TRANSITIVE_JAVA_DEPS_JAR)
	@:

transitive_java_deps_dependencies:
	@:

$(TRANSITIVE_JAVA_DEPS_ROOT_LIST): $(TRANSITIVE_JAVA_DEPS_ROOT_SOURCES) \
    | transitive_java_deps_dependencies
	@if [ -e $@ ]; then rm $@; fi
	@files='$^' && for i in $$files; do echo $$i >> $@; done

TRANSITIVE_JAVA_DEPS_JAVAC_CMD =\
    $(JAVAC) $(TRANSITIVE_JAVA_DEPS_SOURCEPATH_ARG)\
    -d $(TRANSITIVE_JAVA_DEPS_STAGE_DIR) $(TRANSITIVE_JAVA_DEPS_JAVAC_ARGS)\
    `cat $(TRANSITIVE_JAVA_DEPS_ROOT_LIST)`

$(TRANSITIVE_JAVA_DEPS_JAR): \
    $(TRANSITIVE_JAVA_DEPS_ROOT_LIST) $(TRANSITIVE_JAVA_DEPS_FULL_SOURCES) \
    $(TRANSITIVE_JAVA_DEPS_DEPENDENCIES)
	@mkdir -p $(@D)
	@echo "Building $(notdir $@)"
	@rm -rf $(TRANSITIVE_JAVA_DEPS_STAGE_DIR)
	@mkdir $(TRANSITIVE_JAVA_DEPS_STAGE_DIR)
	@$(TRANSITIVE_JAVA_DEPS_JAVAC_CMD)
	@for file in `find $(TRANSITIVE_JAVA_DEPS_STAGE_DIR) -name *.java`; do \
	  copy=`echo $$file | sed "s,$(TRANSITIVE_JAVA_DEPS_STAGE_DIR),$(GEN_JAVA_DIR),"`; \
	  mkdir -p `dirname $$copy`; \
	  diff $$file $$copy &> /dev/null || mv $$file $$copy; \
	done
	@jar cf $@ -C $(TRANSITIVE_JAVA_DEPS_STAGE_DIR) .

$(TRANSITIVE_JAVA_DEPS_INCLUDE): $(TRANSITIVE_JAVA_DEPS_JAR)
	@echo "Building $(notdir $@)"
	@echo "TRANSITIVE_JAVA_DEPS_FULL_SOURCES = \\" > $@
	@for file in `jar tf $< | grep \.class$$ | sed "s/\.class/\.java/"`; do \
	  for path in $(TRANSITIVE_JAVA_DEPS_SOURCEPATH_LIST); do \
	    if [ -f $$path/$$file ]; then \
	      echo "  $${file//\$$/\$$\$$} \\" >> $@; \
	      break; \
	    fi; \
	  done; \
	done

# If a java file in the transitive deps has been removed we don't want make to
# fail. We just want the .jar to rebuild.
%.java:
	@:
