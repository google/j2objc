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
