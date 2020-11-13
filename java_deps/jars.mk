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

JUNIT_ROOT_JAR = junit-4.13.1.jar
JUNIT_SOURCE_JAR = junit-4.13.1-sources.jar

JUNIT_DATAPROVIDER_JAR = junit-dataprovider-1.10.4.jar
JUNIT_DATAPROVIDER_SOURCE_JAR = junit-dataprovider-1.10.4-sources.jar

# JUnit dependency
HAMCREST_JAR = hamcrest-core-1.3.jar
HAMCREST_SOURCE_JAR = hamcrest-core-1.3-sources.jar

PROTOBUF_JAR = protobuf-java-3.11.1.jar

ERROR_PRONE_ANNOTATIONS_JAR = error_prone_annotations-2.1.3.jar
ERROR_PRONE_ANNOTATIONS_SOURCE_JAR = error_prone_annotations-2.1.3-sources.jar

CHECKER_QUAL_JAR = checker-qual-2.0.0.jar
CHECKER_QUAL_SOURCE_JAR = checker-qual-2.0.0-sources.jar

CHECKER_COMPAT_QUAL_JAR = checker-compat-qual-2.0.0.jar
CHECKER_COMPAT_QUAL_SOURCE_JAR = checker-compat-qual-2.0.0-sources.jar

ANIMAL_SNIFFER_ANNOTATIONS_JAR = animal-sniffer-annotations-1.14.jar
ANIMAL_SNIFFER_ANNOTATIONS_SOURCE_JAR = animal-sniffer-annotations-1.14-sources.jar

GUAVA_JAR = guava-25.1-jre.jar
GUAVA_SOURCE_JAR = guava-25.1-jre-sources.jar

GUAVA_ANDROID_JAR = guava-25.1-android.jar
GUAVA_ANDROID_SOURCE_JAR = guava-25.1-android-sources.jar

SCENELIB_JAR = scenelib.jar
PLUME_UTIL_JAR = plume-util-1.0.6.jar

MOCKITO_JAR = mockito-core-2.23.4.jar
MOCKITO_SOURCE_JAR = mockito-core-2.23.4-sources.jar

JSR305_JAR = jsr305-3.0.0.jar
JSR305_SOURCE_JAR = jsr305-3.0.0.jar

JAVAX_INJECT_JAR = javax.inject-1.jar
JAVAX_INJECT_SOURCE_JAR = javax.inject-1-sources.jar

JAVAC_JAR = tools.jar

DOCLAVA_JAR = doclava-1.0.6.jar
JSILVER_JAR = jsilver-1.0.0.jar

PROCYON_COMPILERTOOLS_JAR = procyon-compilertools-0.5.32.jar
NEW_PROCYON_COMPILERTOOLS_JAR = procyon-compilertools-0.5.33.jar
PROCYON_JARS = \
    procyon-core-0.5.32.jar \
    $(PROCYON_COMPILERTOOLS_JAR)

# Flogger support
FLOGGER_JAR = flogger-0.5.1.jar
GOOGLE_EXTENSIONS_JAR = google-extensions-0.5.1.jar
