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

# Build file for J2ObjC's examples/Contacts app.

load("@bazel_skylib//rules:build_test.bzl", "build_test")
load("@j2objc//build_defs:j2objc_library.bzl", "j2objc_library")
load("@rules_apple//apple:ios.bzl", "ios_application")

licenses(["reciprocal"])

ios_application(
    name = "Contacts",
    bundle_id = "com.google.j2objc.example.contacts",
    families = [
        "iphone",
        "ipad",
    ],
    infoplists = ["src/Info.plist"],
    minimum_os_version = "13.0",
    deps = [":ContactsMainLib"],
)

objc_library(
    name = "ContactsMainLib",
    srcs = glob(["src/*.m"]),
    hdrs = glob(["src/*.h"]),
    data = ["//resources"],
    sdk_frameworks = [
        "CoreGraphics",
        "UIKit",
    ],
    deps = [":ContactsJava-j2objc"],
)

j2objc_library(
    name = "ContactsJava-j2objc",
    srcs = glob(["src/java/org/contacts/*.java"]),
)

build_test(
    name = "contacts_build_test",
    targets = [
        ":Contacts",
    ],
)
