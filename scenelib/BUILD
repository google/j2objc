# This packages builds a subset of the annotation-file-utilities package as a scenelib.jar file.

package(
    default_visibility = ["//:internal_visibility"],
)

# This genrule extracts specific class files from a "fat jar"
# within the annotation-tools archive and repackages them into scenelib.jar.
genrule(
    name = "scenelib",
    srcs = ["@annotation-tools//:annotation-file-utilities/annotation-file-utilities-all.jar"],
    outs = ["scenelib.jar"],
    cmd = """
        set -e
        mkdir -p unzipped_files
        unzip -q -d unzipped_files \
            $(location @annotation-tools//:annotation-file-utilities/annotation-file-utilities-all.jar) \
            "org/checkerframework/afu/scenelib/*" "org/objectweb/asm/*"
        jar cMf $(location scenelib.jar) -C unzipped_files .
    """,
    visibility = ["//visibility:public"],
)
