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

# Build file for J2ObjC's examples/Hello app.

load("@bazel_skylib//rules:build_test.bzl", "build_test")
load("@j2objc//build_defs:j2objc_library.bzl", "j2objc_library")
load("@rules_apple//apple:ios.bzl", "ios_application")

licenses(["reciprocal"])

ios_application(
    name = "hello_app",
    bundle_id = "com.google.j2objc.Hello",
    entitlements = "Hello/HelloJ2ObjC.entitlements",
    families = [
        "iphone",
        "ipad",
    ],
    infoplists = ["Hello/Info.plist"],
    minimum_os_version = "13.0",
    deps = [":hello_library"],
)

ios_application(
    name = "hello_j2objc_app",
    bundle_id = "com.google.j2objc.Hello",
    entitlements = "Hello/HelloJ2ObjC.entitlements",
    families = [
        "iphone",
        "ipad",
    ],
    infoplists = [
        "Hello/Info.plist",
    ],
    minimum_os_version = "13.0",
    deps = [
        ":hello_j2objc_library",
    ],
)

build_test(
    name = "hello_build_test",
    targets = [
        ":hello_app",
        ":hello_j2objc_app",
    ],
)

filegroup(
    name = "hello_assets",
    srcs = glob([
        "Hello/Assets.xcassets/*.*",
    ]),
)

objc_library(
    name = "hello_library",
    srcs = [
        "Hello/AppDelegate.m",
        "Hello/ViewControllerNoJ2ObjC.m",
        "Hello/main.m",
    ],
    hdrs = glob(
        ["**/*.h"],
    ),
    data = [
        "Hello/Base.lproj/LaunchScreen.storyboard",
        "Hello/Base.lproj/Main.storyboard",
        ":hello_assets",
    ],
    sdk_frameworks = [
        "UIKit",
    ],
)

objc_library(
    name = "hello_j2objc_library",
    srcs = [
        "Hello/AppDelegate.m",
        "Hello/ViewController.m",
        "Hello/main.m",
    ],
    hdrs = glob(
        ["**/*.h"],
    ),
    data = [
        "Hello/Base.lproj/LaunchScreen.storyboard",
        "Hello/Base.lproj/Main.storyboard",
        ":hello_assets",
    ],
    sdk_frameworks = [
        "UIKit",
    ],
    deps = [
        ":status",
    ],
)

j2objc_library(
    name = "status",
    srcs = [
        "Hello/org/j2objc/Status.java",
    ],
)
