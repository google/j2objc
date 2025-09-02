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

# This file builds the JRE emulation library (jre_emul.jar).

load("@rules_cc//cc:objc_import.bzl", "objc_import")
load("@rules_java//java:defs.bzl", "java_library")
load(
    ":constants.bzl",
    "JRE_ANDROID_SRCS_PUBLIC_QUALIFIED",
    "JRE_JAVA_SRCS_NO_TRANSLATE_QUALIFIED",
    "JRE_JAVA_SRCS_PRIVATE_QUALIFIED",
    "JRE_JAVA_SRCS_PUBLIC_JRE_QUALIFIED",
)

filegroup(
    name = "jre_emul_sources",
    srcs = (
        JRE_ANDROID_SRCS_PUBLIC_QUALIFIED +
        JRE_JAVA_SRCS_NO_TRANSLATE_QUALIFIED +
        JRE_JAVA_SRCS_PRIVATE_QUALIFIED +
        JRE_JAVA_SRCS_PUBLIC_JRE_QUALIFIED
    ),
)

java_library(
    name = "jre_emul_source_lib",
    srcs = [":jre_emul_sources"],
)

genrule(
    name = "jre_emul_modular_srcjar",
    srcs = [
        ":libjre_emul_source_lib-src.jar",  # Use the new srcjar
        ":package_module_sources.py",
        "java.base/module-info.java",
    ],
    outs = ["jre_emul_module.srcjar"],
    # Updated command to pass the srcjar to the script.
    cmd = "python3 $(location :package_module_sources.py) " +
          "--output=$@ " +
          "--module_info=$(location java.base/module-info.java) " +
          "--srcjar=$(location :libjre_emul_source_lib-src.jar)",
)

java_library(
    name = "jre_emul",
    srcs = [":jre_emul_modular_srcjar"],
    javacopts = [
        "-XepDisableAllChecks",
        "-Xlint:-rawtypes,-unchecked",
        "-source",
        "11",
        "-target",
        "11",
        "--system",
        "none",
        "--patch-module=java.base=.",
        # disable invokedynamic string concatenation, since StringConcatFactory isn't available
        "-XDstringConcat=inline",
        # So that JRE classes are allowed to depend on the j2objc annotations
        "--add-reads",
        "java.base=ALL-UNNAMED",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//annotations:annotations_lib",
    ],
)

objc_library(
    name = "jre_emul_sdk_libs",
    sdk_dylibs = [
        "libiconv",
        "libz",
    ],
)

# TODO: the following is temporary until jre_emul is built using Bazel.
# Assumes that "make dist" was previously run to build the library.

config_setting(
    name = "is_ios_device",
    values = {"cpu": "ios_arm64"},
)

config_setting(
    name = "is_ios_simulator",
    # This value is for simulators on Apple Silicon Macs.
    # For older Intel Macs, you might need "ios_x86_64".
    values = {"cpu": "ios_sim_arm64"},
)

config_setting(
    name = "is_macos_platform",
    # This value is for macOS on Apple Silicon Macs.
    # For older Intel Macs, you might need "darwin_x86_64".
    values = {"cpu": "darwin_arm64"},
)

objc_import(
    name = "jre_emul_lib",
    hdrs = glob(
        [
            "Classes/**/*.h",
            "build_result/Classes/**/*.h",
        ],
    ),
    archives = select(
        {
            "//jre_emul:is_ios_device": ["build_result/iphone/libjre_emul.a"],
            "//jre_emul:is_ios_simulator": ["build_result/simulator/libjre_emul.a"],
            "//jre_emul:is_macos_platform": ["build_result/macosx/libjre_emul.a"],
        },
        no_match_error = "Apple device isn't supported.",
    ),
    includes = [
        "Classes",
        "build_result/Classes",
    ],
    sdk_frameworks = [
        "Security",
        "Foundation",
    ],
    visibility = ["//visibility:public"],
    deps = [
        ":jre_emul_sdk_libs",
    ],
    alwayslink = True,
)
