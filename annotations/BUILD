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

# Defines the j2objc-annotations library.

load("@rules_java//java:defs.bzl", "java_library")
load("//build_defs:j2objc_library.bzl", "j2objc_library")

package(
    default_visibility = ["//visibility:public"],
)

java_library(
    name = "annotations_lib",
    srcs = glob(["src/main/java/com/google/j2objc/annotations/*.java"]),
)

java_library(
    name = "annotations",
    visibility = ["//visibility:public"],
    exports = [":annotations_lib"],
)

j2objc_library(
    name = "annotations-j2objc",
    srcs = glob(["src/main/java/com/google/j2objc/annotations/*.java"]),
)
