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

# Defines all of the top-level dependencies for the various j2objc components.
#
# Author: Keith Stanger

# Call recursively only from the top-level makefile and not from xcode.
ifeq ("$(CONFIGURATION_BUILD_DIR)x$(MAKELEVEL)", "x0")
J2OBJC_CALL_RECURSIVE_DEPS = YES
endif

# Allow recursive calls from the j2objc root makefile.
ifeq ($(CURDIR), $(realpath $(J2OBJC_ROOT)))
J2OBJC_CALL_RECURSIVE_DEPS = YES
endif

ifeq ("$(J2OBJC_CALL_RECURSIVE_DEPS)", "YES")

annotations_dist:
	@$(MAKE) -C $(J2OBJC_ROOT)/annotations dist

java_deps_dist:
	@$(MAKE) -C $(J2OBJC_ROOT)/java_deps dist

jre_emul_jars_dist: annotations_dist
	@$(MAKE) -C $(J2OBJC_ROOT)/jre_emul -f java.mk jars_dist

translator: annotations_dist java_deps_dist
	@$(MAKE) -C $(J2OBJC_ROOT)/translator dist

translator_dist: translator jre_emul_jars_dist

jre_emul_dist: translator_dist jre_emul_jars_dist
	@$(MAKE) -C $(J2OBJC_ROOT)/jre_emul dist

jre_emul_java_manifest:
	@$(MAKE) -C $(J2OBJC_ROOT)/jre_emul -f java.mk java_sources_manifest

junit_dist: translator_dist jre_emul_dist
	@$(MAKE) -C $(J2OBJC_ROOT)/junit dist

junit_java: java_deps_dist jre_emul_jars_dist
	@$(MAKE) -C $(J2OBJC_ROOT)/junit java

junit_manifest:
	@$(MAKE) -C $(J2OBJC_ROOT)/junit java_sources_manifest

jsr305_dist: translator_dist jre_emul_dist java_deps_dist
	@$(MAKE) -C $(J2OBJC_ROOT)/jsr305 dist

javax_inject_dist: translator_dist jre_emul_dist java_deps_dist
	@$(MAKE) -C $(J2OBJC_ROOT)/inject/javax_inject dist

guava_dist: translator_dist jre_emul_dist jsr305_dist
	@$(MAKE) -C $(J2OBJC_ROOT)/guava dist

guava_java: java_deps_dist jre_emul_jars_dist
	@$(MAKE) -C $(J2OBJC_ROOT)/guava java

cycle_finder_dist: annotations_dist java_deps_dist translator_dist
	@$(MAKE) -C $(J2OBJC_ROOT)/cycle_finder dist

mockito_dist: translator_dist jre_emul_dist junit_dist
	@$(MAKE) -C $(J2OBJC_ROOT)/testing/mockito dist

mockito_java: java_deps_dist junit_java
	@$(MAKE) -C $(J2OBJC_ROOT)/testing/mockito java

mockito_manifest:
	@$(MAKE) -C $(J2OBJC_ROOT)/testing/mockito java_sources_manifest

protobuf_compiler_dist:
	@$(MAKE) -C $(J2OBJC_ROOT)/protobuf/compiler dist

protobuf_runtime_java:
	@$(MAKE) -C $(J2OBJC_ROOT)/protobuf/runtime java

protobuf_runtime_java: java_deps_dist

protobuf_runtime_dist: jre_emul_dist protobuf_compiler_dist
	@$(MAKE) -C $(J2OBJC_ROOT)/protobuf/runtime dist

xalan_dist: translator_dist jre_emul_dist
	@$(MAKE) -C $(J2OBJC_ROOT)/xalan dist

xalan_java: java_deps_dist jre_emul_jars_dist
	@$(MAKE) -C $(J2OBJC_ROOT)/xalan java

else

annotations_dist:
java_deps_dist:
jre_emul_jars_dist:
translator:
translator_dist:
jre_emul_dist:
jre_emul_java_manifest:
junit_dist:
junit_java:
junit_manifest:
jsr305_dist:
javax_inject dist:
guava_dist:
guava_java:
cycle_finder_dist:
mockito_dist:
mockito_java:
mockito_manifest:
protobuf_compiler_dist:
protobuf_runtime_java:
protobuf_runtime_dist:
xalan_dist:
xalan_java:

endif
