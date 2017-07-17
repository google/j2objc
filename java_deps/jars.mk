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

JARJAR_HOME = jarjar
JARJAR_JAR = jarjar-1.4.jar
JAVA_DEPS_JAR_DIR = $(J2OBJC_ROOT)/java_deps/$(BUILD_DIR_NAME)

JUNIT_JAR = j2objc_junit.jar

JUNIT_ROOT_JAR = junit-4.11.jar
JUNIT_SOURCE_JAR = junit-4.11-sources.jar

# JUnit dependency
HAMCREST_JAR = hamcrest-core-1.3.jar
HAMCREST_SOURCE_JAR = hamcrest-core-1.3-sources.jar

MOCKITO_JAR = mockito-core-1.9.5.jar
MOCKITO_SOURCE_JAR = mockito-core-1.9.5-sources.jar

PROTOBUF_JAR = protobuf-java-3.3.0.jar

ASM_JAR = asm-all-5.1.jar

ECLIPSE_JARS = \
    org.eclipse.core.contenttype-3.4.200.v20140207-1251.jar \
    org.eclipse.core.jobs-3.6.1.v20141014-1248.jar \
    org.eclipse.core.resources-3.9.1.v20140825-1431.jar \
    org.eclipse.core.runtime-3.10.0.v20140318-2214.jar \
    org.eclipse.equinox.common-3.6.200.v20130402-1505.jar \
    org.eclipse.equinox.preferences-3.5.200.v20140224-1527.jar \
    org.eclipse.jdt.compiler.apt-1.1.0.v20140509-1235.jar \
    org.eclipse.jdt.core-3.10.0.v20140604-1726.jar \
    org.eclipse.osgi-3.10.2.v20150203-1939.jar \
    org.eclipse.text-3.5.101.jar

GUAVA_JAR = guava-19.0.jar
GUAVA_SOURCE_JAR = guava-19.0-sources.jar

JSR305_JAR = jsr305-3.0.0.jar
JSR305_SOURCE_JAR = jsr305-3.0.0.jar

JAVAX_INJECT_JAR = javax.inject-1.jar
JAVAX_INJECT_SOURCE_JAR = javax.inject-1-sources.jar

JAVAC_JAR = tools.jar

DOCLAVA_JAR = doclava-1.0.6.jar
JSILVER_JAR = jsilver-1.0.0.jar

PROCYON_JARS = \
    procyon-core-0.5.32.jar \
    procyon-compilertools-0.5.32.jar
