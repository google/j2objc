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

# Starlark rules for compiling Java code to iOS using j2objc.

load("@bazel_skylib//:bzl_library.bzl", "bzl_library")

package(default_visibility = ["//visibility:public"])

exports_files(["j2objc_library.bzl"])

bzl_library(
    name = "j2objc_library_bzl",
    srcs = [
        "j2objc_library.bzl",
    ],
    visibility = ["//visibility:private"],
    deps = [
        "@rules_cc//cc/common",
    ],
)
