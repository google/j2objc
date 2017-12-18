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

load("@build_bazel_rules_apple//apple:ios.bzl", "ios_application")

ios_application(
    name = "Contacts",
    bundle_id = "com.google.j2objc.example.contacts",
    infoplists = ["src/Info.plist"],
    families = ["iphone", "ipad"],
    deps = [":ContactsMainLib"],
    app_icons = [":ContactsAppIcons"],
    launch_images = [":ContactsLaunchImages"],
)

filegroup(
    name = "ContactsAppIcons",
    srcs = glob(["Assets.xcassets/AppIcon.appiconset/**"]),
)

filegroup(
    name = "ContactsLaunchImages",
    srcs = glob(["Assets.xcassets/LaunchImage.launchimage/**"]),
)

objc_library(
    name = "ContactsMainLib",
    srcs = glob(["src/*.m"]),
    hdrs = glob(["src/*.h"]),
    sdk_frameworks = ["CoreGraphics"],
    deps = [
        ":ContactsJava",
        "//resources",
    ],
)

j2objc_library(
    name = "ContactsJava",
    deps = [":ContactsJavaLib"],
    jre_deps = ["@bazel_j2objc//:jre_net_lib"],
)

java_library(
    name = "ContactsJavaLib",
    srcs = glob(["src/java/org/contacts/*.java"]),
)
