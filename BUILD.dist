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

licenses(["notice"])  # Apache License 2.0

exports_files(["LICENSE"])

package(
    default_visibility = ["//visibility:public"],
)

genrule(
    name = "jre_emul_jar",
    srcs = ["lib/jre_emul.jar"],
    outs = ["jre_emul.jar"],
    cmd = "cp $< $@",
)

java_import(
    name = "annotations",
    jars = ["lib/j2objc_annotations.jar"],
)

java_import(
    name = "j2objc",
    jars = ["lib/j2objc.jar"],
    exports = [":annotations"],
)

# JRE libraries

objc_import(
    name = "jre_core_lib",
    hdrs = glob(["include/**"]),
    includes = ["include"],
    archives = ["lib/libjre_core.a"],
    sdk_dylibs = [
        "libiconv",
    ],
)

objc_library(
    name = "jre_beans_lib",
    tags = ["j2objc_jre_lib"],
    deps = [
        ":jre_beans_lib_import",
        ":jre_core_lib",
        ":jre_util_lib",
    ],
)

objc_import(
    name = "jre_beans_lib_import",
    archives = ["lib/libjre_beans.a"],
    visibility = ["//visibility:private"],
)

objc_library(
    name = "jre_channels_lib",
    tags = ["j2objc_jre_lib"],
    deps = [
        ":jre_channels_lib_import",
        ":jre_core_lib",
        ":jre_net_lib",
        ":jre_security_lib",
        ":jre_util_lib",
    ],
)

objc_import(
    name = "jre_channels_lib_import",
    archives = ["lib/libjre_channels.a"],
    visibility = ["//visibility:private"],
)

objc_library(
    name = "jre_concurrent_lib",
    tags = ["j2objc_jre_lib"],
    deps = [
        ":jre_concurrent_lib_import",
        ":jre_core_lib",
        ":jre_util_lib",
    ],
)

objc_import(
    name = "jre_concurrent_lib_import",
    archives = ["lib/libjre_concurrent.a"],
    visibility = ["//visibility:private"],
)

objc_library(
    name = "jre_io_lib",
    tags = ["j2objc_jre_lib"],
    deps = [
        ":jre_core_lib",
        ":jre_io_lib_import",
    ],
)

objc_import(
    name = "jre_io_lib_import",
    archives = ["lib/libjre_io.a"],
    visibility = ["//visibility:private"],
)

objc_library(
    name = "jre_net_lib",
    tags = ["j2objc_jre_lib"],
    deps = [
        ":jre_core_lib",
        ":jre_net_lib_import",
    ],
)

objc_import(
    name = "jre_net_lib_import",
    archives = ["lib/libjre_net.a"],
    visibility = ["//visibility:private"],
)

objc_library(
    name = "jre_security_lib",
    tags = ["j2objc_jre_lib"],
    deps = [
        ":jre_core_lib",
        ":jre_net_lib",
        ":jre_security_lib_import",
    ],
)

objc_import(
    name = "jre_security_lib_import",
    archives = ["lib/libjre_security.a"],
    sdk_frameworks = [
        "Security",
    ],
    visibility = ["//visibility:private"],
)

objc_library(
    name = "jre_sql_lib",
    tags = ["j2objc_jre_lib"],
    deps = [
        ":jre_core_lib",
        ":jre_sql_lib_import",
    ],
)

objc_import(
    name = "jre_sql_lib_import",
    archives = ["lib/libjre_sql.a"],
    visibility = ["//visibility:private"],
)

objc_library(
    name = "jre_ssl_lib",
    tags = ["j2objc_jre_lib"],
    deps = [
        ":jre_core_lib",
        ":jre_net_lib",
        ":jre_security_lib",
        ":jre_ssl_lib_import",
        ":jre_util_lib",
    ],
)

objc_import(
    name = "jre_ssl_lib_import",
    archives = ["lib/libjre_ssl.a"],
    visibility = ["//visibility:private"],
)

objc_library(
    name = "jre_util_lib",
    tags = ["j2objc_jre_lib"],
    deps = [
        ":jre_core_lib",
        ":jre_util_lib_import",
    ],
)

objc_import(
    name = "jre_util_lib_import",
    archives = ["lib/libjre_util.a"],
    visibility = ["//visibility:private"],
)

objc_library(
    name = "jre_xml_lib",
    tags = ["j2objc_jre_lib"],
    deps = [
        ":jre_core_lib",
        ":jre_net_lib",
        ":jre_security_lib",
        ":jre_xml_lib_import",
    ],
)

objc_import(
    name = "jre_xml_lib_import",
    archives = ["lib/libjre_xml.a"],
    visibility = ["//visibility:private"],
)

objc_library(
    name = "jre_zip_lib",
    tags = ["j2objc_jre_lib"],
    deps = [
        ":jre_core_lib",
        ":jre_io_lib",
        ":jre_net_lib",
        ":jre_security_lib",
        ":jre_zip_lib_import",
    ],
)

objc_import(
    name = "jre_zip_lib_import",
    archives = ["lib/libjre_zip.a"],
    sdk_dylibs = [
        "libz",
    ],
    visibility = ["//visibility:private"],
)

objc_library(
    name = "android_util_lib",
    tags = ["j2objc_jre_lib"],
    deps = [
        ":android_util_lib_import",
        ":jre_concurrent_lib",
        ":jre_core_lib",
        ":jre_net_lib",
        ":jre_security_lib",
        ":jre_util_lib"
    ],
)

objc_import(
    name = "android_util_lib_import",
    archives = ["lib/libandroid_util.a"],
    visibility = ["//visibility:private"],
)

# Protobuf libraries

filegroup(
    name = "proto_plugin",
    srcs = ["j2objc_protoc_plugin"]
)

objc_import(
    name = "proto_runtime",
    hdrs = glob(["include/**"]),
    includes = ["include"],
    archives = ["lib/libprotobuf_runtime.a"],
)
