# Description:
#    cycle_finder, a tool for finding reference cycles in a Java program.
#    Mainly useful when translating to a language without garbage collection.

package(
    default_visibility = [
        "//third_party/java_src/j2objc:internal_visibility",
    ],
)

licenses(["notice"])  # Apache License 2.0

load("//third_party/java_src/j2objc/builddefs:j2objc.bzl", "resources")

resources(
    name = "cycle_finder_resources",
    dest_dir = "src/main/java/",
    files = [
        "com/google/devtools/cyclefinder/CycleFinder.properties",
    ],
    src_dir = "src/main/resources/",
)

java_library(
    name = "cycle_finder",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = [
        ":cycle_finder_resources",
    ],
    deps = [
        "//java/com/google/common/annotations",
        "//java/com/google/common/base",
        "//java/com/google/common/collect",
        "//java/com/google/common/io",
        "//third_party/java/eclipse:eclipse-jdt-core-4.5",
        "//third_party/java_src/j2objc/annotations:annotations_lib",
        "//third_party/java_src/j2objc/translator",
    ],
)

java_test(
    name = "tests",
    size = "small",
    srcs = glob(["src/test/java/**/*.java"]),
    test_class = "com.google.devtools.cyclefinder.CycleFinderTest",
    deps = [
        ":cycle_finder",
        "//java/com/google/common/base",
        "//java/com/google/common/collect",
        "//java/com/google/common/io",
        "//third_party/java/eclipse:eclipse-jdt-core-4.5",
        "//third_party/java/junit:not-testonly",
        "//third_party/java_src/j2objc/translator",
    ],
)

filegroup(
    name = "public_files",
    srcs = glob(
        ["**/*"],
        exclude = ["build_result/**/*"],
    ),
)
