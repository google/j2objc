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

# Description:
#   The Java to Objective C translator, aka "j2objc".

load("@rules_java//java:defs.bzl", "java_binary", "java_library", "java_test")

JDK_COMPILER_ADD_EXPORTS = [
    # The javac packages that are used by the translator.
    "jdk.compiler/com.sun.tools.javac.%s" % p
    for p in (
        "api",
        "code",
        "comp",
        "file",
        "main",
        "model",
        "parser",
        "processing",
        "resources",
        "tree",
        "util",
    )
]

java_library(
    name = "translator",
    srcs = glob(
        ["src/main/java/**/*.java"],
        exclude = [
            "src/main/java/com/google/devtools/j2objc/docs/*.java",
        ],
    ),
    add_exports = JDK_COMPILER_ADD_EXPORTS,
    javacopts = [
        "-Xep:BoxedPrimitiveEquality:OFF",
    ],
    resources = glob(["src/main/resources/**/*"]),
    visibility = ["//visibility:public"],
    deps = [
        "//annotations:annotations_lib",
        "//scenelib",
        # Corrected Maven dependency labels:
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_protobuf_protobuf_java",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:com_google_flogger_flogger",
        "@maven//:io_github_eisop_checker_qual",
        "@maven//:org_jspecify_jspecify",
        "@maven//:org_plumelib_plume_util",
        "@maven//:org_bitbucket_mstrobel_procyon_core",
        "@maven//:org_bitbucket_mstrobel_procyon_compilertools",
    ],
)

java_test(
    name = "tests",
    srcs = glob(["src/test/java/**/*.java"]),
    resources = glob(["src/test/resources/**/*"]),
    test_class = "com.google.devtools.j2objc.SmallTests",
    deps = [
        ":translator",
        "//annotations:annotations_lib",
        "//jre_emul",
        "@maven//:com_google_flogger_flogger",
        "@maven//:com_google_flogger_google_extensions",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_truth_truth",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_mockito_mockito_core",
    ],
)

# Creates a single, executable command-line jar containing the
# translator and all of its dependencies.
java_binary(
    name = "j2objc",
    main_class = "com.google.devtools.j2objc.J2ObjC",
    visibility = ["//visibility:public"],
    runtime_deps = [
        ":translator",
    ],
)
