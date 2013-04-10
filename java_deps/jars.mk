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

# Defines the names of all dependent .jar files used by j2objc.
#
# Author: Keith Stanger

JAVA_DEPS_JAR_DIR = $(J2OBJC_ROOT)/java_deps/$(BUILD_DIR_NAME)

ECLIPSE_JARS = \
    org.eclipse.core.contenttype-3.4.100.v20100505-1235.jar \
    org.eclipse.core.jobs-3.5.0.v20100515.jar \
    org.eclipse.core.resources-3.6.0.v20100526-0737.jar \
    org.eclipse.core.runtime-3.6.0.v20100505.jar \
    org.eclipse.equinox.common-3.6.0.v20100503.jar \
    org.eclipse.equinox.preferences-3.3.0.v20100503.jar \
    org.eclipse.jdt.core-3.8.1.v20120531-0637.jar \
    org.eclipse.osgi-3.6.0.v20100517.jar \
    org.eclipse.text-3.5.100.v20110505-0800.jar

GUAVA_JAR = guava-13.0.jar

JSR305_JAR = jsr305-2.0.1.jar

JUNIT_JAR = junit-4.10.jar
JUNIT_SOURCE_JAR = junit-4.10-sources.jar
