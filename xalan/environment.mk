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

# Make include file that sets the build environment.  The external
# environment variables are defined by Xcode, allowing this build
# to be used within Xcode.

J2OBJC_ROOT = ..

include $(J2OBJC_ROOT)/make/common.mk
include $(J2OBJC_ROOT)/make/j2objc_deps.mk
include $(J2OBJC_ROOT)/java_deps/jars.mk

SRC_DIR = $(abspath third_party/android/platform/external/apache-xml/src/main/java)
OBJS_DIR = $(BUILD_DIR)/objs

vpath %.java $(SRC_DIR)
vpath %.properties $(SRC_DIR)

force:
	@:

$(sort $(BUILD_DIR) $(ARCH_BUILD_DIR) $(ARCH_LIB_DIR) $(DIST_JAR_DIR)):
	@mkdir -p $@
