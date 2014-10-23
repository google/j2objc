licenses(["notice"])

package(
    default_visibility = ["//visibility:public"],
)

package_group(
    name = "j2objc_guava_users",
    packages = [],
)

filegroup(
    name = "guava_src_files",
    srcs = glob(["**/*.java"]),
)

filegroup(
    name = "guava_files",
    srcs = glob(["**/*"]),
)

# Please ask j2objc-users@ if you want to use this library.
java_library(
    name = "guava",
    srcs = [":guava_src_files"],
    javacopts = ["-extra_checks:off"],
    visibility = [":j2objc_guava_users"],
    deps = [
        "//third_party/java/jsr305_annotations",
        "//third_party/java_src/j2objc:annotations",
    ],
)
