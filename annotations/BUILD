# Description:
#   Java annotations used by J2ObjC users to affect how code is translated.

licenses(["notice"])  # Apache License 2.0

package(
    default_visibility = [
        "//third_party/java_src/j2objc:internal_visibility",
    ],
)

java_library(
    name = "annotations_lib",
    srcs = glob(["src/main/java/com/google/j2objc/annotations/*.java"]),
    compatible_with = [
        "//buildenv/target:android",
        "//buildenv/target:appengine",
        "//buildenv/target:gce",
    ],
    # We don't transpile annotations_lib since the only runtime annotation
    # is provided as part of jre_emul:jre_emul_lib.
    constraints = ["android"],
)

java_library(
    name = "annotations",
    compatible_with = [
        "//buildenv/target:android",
        "//buildenv/target:appengine",
        "//buildenv/target:gce",
    ],
    constraints = ["android"],
    exports = [":annotations_lib"],
)

genrule(
    name = "annotations_jar",
    srcs = [":libannotations_lib.jar"],
    outs = ["annotations_jar.jar"],
    cmd = "cp $< $@",
    # Exposed for use by j2objc_find_cycles_test macro.
    visibility = ["//visibility:public"],
)

filegroup(
    name = "src_files",
    srcs = glob(["src/main/java/com/google/j2objc/annotations/*.java"]),
)

filegroup(
    name = "public_files",
    srcs = glob(
        ["**/*"],
        exclude = ["build_result/**/*"],
    ),
)
