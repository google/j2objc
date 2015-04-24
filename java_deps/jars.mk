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
JUNIT_JAR = j2objc_junit.jar

ECLIPSE_JARS = \
    org.eclipse.core.contenttype-3.4.200.v20130326-1255.jar \
    org.eclipse.core.jobs-3.5.300.v20130429-1813.jar \
    org.eclipse.core.resources-3.8.101.v20130717-0806.jar \
    org.eclipse.core.runtime-3.9.0.v20130326-1255.jar \
    org.eclipse.equinox.common-3.6.200.v20130402-1505.jar \
    org.eclipse.equinox.preferences-3.5.100.v20130422-1538.jar \
    org.eclipse.jdt.compiler.apt-1.0.600.v20130530-1010.jar \
    org.eclipse.jdt.core-3.9.1.v20130905-0837.jar \
    org.eclipse.osgi-3.9.1.v20130814-1242.jar \
    org.eclipse.text-3.5.101.jar

GUAVA_JAR = guava-14.0.1.jar

JSR305_JAR = jsr305-3.0.0.jar
JSR305_SOURCE_JAR = jsr305-3.0.0.jar

JAVAX_INJECT_JAR = javax.inject-1.jar
JAVAX_INJECT_SOURCE_JAR = javax.inject-1-sources.jar

JUNIT_ROOT_JAR = junit-4.11.jar
JUNIT_SOURCE_JAR = junit-4.11-sources.jar
HAMCREST_JAR = hamcrest-core-1.3.jar
HAMCREST_SOURCE_JAR = hamcrest-core-1.3-sources.jar

MOCKITO_JAR = mockito-core-1.9.5.jar
MOCKITO_SOURCE_JAR = mockito-core-1.9.5-sources.jar

JARJAR = jarjar-1.3.jar
