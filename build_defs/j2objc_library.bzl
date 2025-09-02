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

"""A macro for transpiling Java source files into Objective-C."""

load("@rules_cc//cc:defs.bzl", "cc_common")
load("@rules_cc//cc:objc_library.bzl", "objc_library")
load("@rules_java//java:defs.bzl", "JavaInfo")

_default_j2objc_c_flags = [
    # Java language spec allows int and long overflow.
    "-fno-strict-overflow",

    # LLVM/clang warnings about legal Java patterns.
    "-Wno-bitwise-instead-of-logical",
    "-Wno-parentheses",
    "-Wno-unused-variable",

    # General build health
    "-Werror",
]

def _j2objc_transpile_impl(ctx):
    """Implementation of the j2objc_transpile rule."""
    output_dir = ctx.actions.declare_directory(ctx.label.name + "_j2objc_sources")
    header_dir = ctx.actions.declare_directory(ctx.label.name + "_j2objc_headers")
    param_file = ctx.actions.declare_file(ctx.label.name + "_params.txt")

    classpath_deps = depset(
        transitive = [dep[JavaInfo].transitive_compile_time_jars for dep in ctx.attr.deps],
    )

    j2objc_args = ctx.actions.args()
    j2objc_args.add("-d", output_dir.path)
    j2objc_args.add("-Xpublic-hdrs", header_dir.path)

    if ctx.attr.transpile_with_arc:
        j2objc_args.add("-use-arc")

    classpath_string = ":".join([f.path for f in classpath_deps.to_list()])
    if classpath_string:
        j2objc_args.add("-classpath")
        j2objc_args.add(classpath_string)

    j2objc_args.add_all(ctx.files.srcs)

    ctx.actions.write(
        output = param_file,
        content = j2objc_args,
    )

    ctx.actions.run(
        mnemonic = "J2ObjCTranspile",
        outputs = [output_dir, header_dir],
        inputs = depset(
            direct = ctx.files.srcs + [param_file],
            transitive = [classpath_deps],
            order = "preorder",
        ),
        executable = ctx.executable._translator,
        arguments = ["@" + param_file.path],
        progress_message = "J2ObjC Transpiling: %s" % ctx.label,
    )

    compilation_context = cc_common.create_compilation_context(
        headers = depset([header_dir]),
        includes = depset([header_dir.path]),
    )

    return [
        DefaultInfo(files = depset([output_dir, header_dir])),
        OutputGroupInfo(
            public_headers = depset([header_dir]),
            transpiled_sources = depset([output_dir]),
        ),
        CcInfo(compilation_context = compilation_context),
    ]

_j2objc_transpile = rule(
    implementation = _j2objc_transpile_impl,
    attrs = {
        "srcs": attr.label_list(allow_files = [".java"]),
        "deps": attr.label_list(providers = [JavaInfo]),
        "swift_friendly": attr.bool(
            default = False,
            doc = "Generate ARC-compatible sources.",
        ),
        "transpile_with_arc": attr.bool(
            default = True,
            doc = "Generate ARC-compatible sources.",
        ),
        "translation_flags": attr.string_list(
            doc = "Extra flags to pass to the j2objc transpiler.",
        ),
        "_translator": attr.label(
            default = Label("//translator:j2objc"),
            executable = True,
            cfg = "exec",
        ),
        "_cc_toolchain": attr.label(
            default = Label("@bazel_tools//tools/cpp:current_cc_toolchain"),
            providers = [cc_common.CcToolchainInfo],
        ),
    },
)

def j2objc_library(
        name,
        srcs = [],
        deps = [],
        swift_friendly = False,
        transpile_with_arc = True,
        translation_flags = [],
        copts = [],
        **kwargs):
    """A macro for transpiling Java libraries to Objective-C.

    Args:
      name: A unique name for this target.
      srcs: List of source files and/or source jars.
      deps: List of dependent j2objc_library and objc_library targets.
      swift_friendly: Generate class properties and nullability annotations
        to improve Swift importing of generated headers.
      transpile_with_arc: Generate ARC-compatible sources.
      translation_flags: Extra flags to pass to the j2objc transpiler.
      copts: Extra flags to pass to the Objective-C compiler.
      **kwargs: Any additional objc_library arguments.
    """

    transpiled_target_name = name + "_transpiled"
    transpiled_srcs_fg = transpiled_target_name + "_srcs"
    transpiled_hdrs_fg = transpiled_target_name + "_hdrs"

    _j2objc_transpile(
        name = transpiled_target_name,
        srcs = srcs,
        deps = deps + ["@j2objc//jre_emul:jre_emul"],
        swift_friendly = swift_friendly,
        transpile_with_arc = transpile_with_arc,
        translation_flags = translation_flags,
    )

    native.filegroup(
        name = transpiled_srcs_fg,
        srcs = [":" + transpiled_target_name],
        output_group = "transpiled_sources",
    )
    native.filegroup(
        name = transpiled_hdrs_fg,
        srcs = [":" + transpiled_target_name],
        output_group = "public_headers",
    )

    all_copts = []
    if transpile_with_arc:
        all_copts.append("-fobjc-arc-exceptions")
    else:
        all_copts.append("-fobjc-weak")
    all_copts.extend(_default_j2objc_c_flags)

    # Add target-specified copts last, so they can potentially override this list.
    all_copts.extend(copts)

    srcs = []
    non_arc_srcs = []
    if transpile_with_arc:
        srcs = [":" + transpiled_srcs_fg]
    else:
        non_arc_srcs = [":" + transpiled_srcs_fg]
    hdrs = [":" + transpiled_hdrs_fg]
    objc_library(
        name = name,
        srcs = srcs,
        hdrs = hdrs,
        non_arc_srcs = non_arc_srcs,
        copts = all_copts,
        deps = [":" + transpiled_target_name] + [dep + "_j2objc" for dep in deps] + ["@j2objc//jre_emul:jre_emul_lib"],
        **kwargs
    )
