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

# MOE:begin_strip
ECLIPSE_JARS = \
    org.eclipse.core.contenttype_3.4.100.v20110423-0524.jar \
    org.eclipse.core.jobs_3.5.100.v20110404.jar \
    org.eclipse.core.resources_3.7.100.v20110510-0712.jar \
    org.eclipse.core.runtime_3.7.0.v20110110.jar \
    org.eclipse.equinox.common_3.6.0.v20110523.jar \
    org.eclipse.equinox.preferences_3.4.1.R37x_v20110725.jar \
    org.eclipse.jdt.core_3.7.1.v_B76_R37x.jar \
    org.eclipse.osgi_3.7.1.R37x_v20110808-1106.jar \
    org.eclipse.text_3.5.101.r371_v20110810-0800.jar

GUAVA_JAR = guava-jdk5.jar

ICU4J_JAR = icu4j-core.jar
ICU4J_SOURCE_JAR = icu4j-core-src.jar

JSR305_JAR = jsr305.jar
JSR305_SOURCE_JAR = jsr305-src.jar

JUNIT_JAR = junit.jar
JUNIT_SOURCE_JAR = junit-src.jar

# JUnit dependency
HAMCREST_SOURCE_JAR = hamcrest-core-1.1-src.jar

# MOE:end_strip
# MOE:insert ECLIPSE_JARS = \
# MOE:insert     org.eclipse.core.contenttype-3.4.100.v20100505-1235.jar \
# MOE:insert     org.eclipse.core.jobs-3.5.0.v20100515.jar \
# MOE:insert     org.eclipse.core.resources-3.6.0.v20100526-0737.jar \
# MOE:insert     org.eclipse.core.runtime-3.6.0.v20100505.jar \
# MOE:insert     org.eclipse.equinox.common-3.6.0.v20100503.jar \
# MOE:insert     org.eclipse.equinox.preferences-3.3.0.v20100503.jar \
# MOE:insert     org.eclipse.jdt.core-3.8.1.v20120531-0637.jar \
# MOE:insert     org.eclipse.osgi-3.6.0.v20100517.jar \
# MOE:insert     org.eclipse.text-3.5.100.v20110505-0800.jar

# MOE:insert GUAVA_JAR = guava-13.0.jar

# MOE:insert ICU4J_JAR = icu4j-51.1.jar
# MOE:insert ICU4J_SOURCE_JAR = icu4j-51.1-sources.jar

# MOE:insert JSR305_JAR = jsr305-2.0.1.jar
# MOE:insert JSR305_SOURCE_JAR = jsr305-2.0.1.jar

# MOE:insert JUNIT_JAR = junit-4.10.jar
# MOE:insert JUNIT_SOURCE_JAR = junit-4.10-sources.jar
# MOE:insert HAMCREST_SOURCE_JAR = hamcrest-core-1.1.jar
