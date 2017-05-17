# Description:
#   The Java to Objective C translator, aka "j2objc". This is a pure-Java
#   project, and so can be built and tested on Linux or macOS.

package(
    default_visibility = [
        "//third_party/java_src/j2objc:internal_visibility",
    ],
)

licenses(["notice"])  # Apache License 2.0

load("//third_party/java_src/j2objc/builddefs:j2objc.bzl", "resources")

resources(
    name = "translator_resources",
    dest_dir = "src/main/java/",
    files = [
        "com/google/devtools/j2objc/J2ObjC.properties",
        "com/google/devtools/j2objc/JRE.mappings",
    ],
    src_dir = "src/main/resources/",
)

resources(
    name = "translator_test_resources",
    dest_dir = "src/test/java/",
    files = [
        "com/google/devtools/j2objc/annotations/Processor.jar",
        "com/google/devtools/j2objc/mappings.j2objc",
        "com/google/devtools/j2objc/testMappings.j2objc",
        "com/google/devtools/j2objc/util/example.jar",
        "com/google/devtools/j2objc/util/packageInfoLookupTest.jar",
    ],
    src_dir = "src/test/resources/",
)

java_library(
    name = "translator",
    srcs = glob(
        ["src/main/java/**/*.java"],
        exclude = [
            "src/main/java/com/google/devtools/j2objc/docs/*.java",
        ],
    ),
    resources = [
        ":translator_resources",
    ],
    deps = [
        "//java/com/google/common/annotations",
        "//java/com/google/common/base",
        "//java/com/google/common/collect",
        "//java/com/google/common/io",
        "//third_party/java/asm",
        "//third_party/java/eclipse:eclipse-jdt-core-4.5",
        "//third_party/java/jdk:langtools",
        "//third_party/java/jsr305_annotations",
        "//third_party/java_src/j2objc/annotations:annotations_lib",
    ],
)

java_test(
    name = "tests",
    size = "small",
    timeout = "moderate",
    srcs = glob(["src/test/java/**/*.java"]),
    jvm_flags = ["-Xss4m"],
    resources = [":translator_test_resources"],
    test_class = "com.google.devtools.j2objc.SmallTests",
    deps = [
        ":translator",
        "//java/com/google/common/base",
        "//java/com/google/common/collect",
        "//java/com/google/common/io",
        "//third_party/java/eclipse:eclipse-jdt-core-4.5",
        "//third_party/java/junit:not-testonly",
    ],
)

filegroup(
    name = "public_files",
    srcs = glob(
        ["**/*"],
        exclude = ["build_result/**/*"],
    ),
)
