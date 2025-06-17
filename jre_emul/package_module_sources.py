# Copyright 2024 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""A helper script to arrange JRE source files into a modular layout."""

import argparse
import os
import pathlib
import shutil
import sys
import tempfile
import zipfile


def find_java_package_path(file_path_str, source_roots):
  """Finds the Java package path part of a source file path.

  This is done by finding which of the known source_roots is a prefix
  of the file path and returning the remainder.

  Args:
    file_path_str: The string path to the source file, relative to the package
      (e.g., "Classes/java/lang/Object.java").
    source_roots: A list of known JRE source root directories (e.g., "Classes").

  Returns:
    A pathlib.Path object of the relative Java package path, or None if no
    matching source root is found.
  """
  for root in source_roots:
    # Ensure the root ends with a separator for a clean prefix check
    root_prefix = root if root.endswith("/") else root + "/"
    if file_path_str.startswith(root_prefix):
      # Return the path segment after the root prefix.
      return pathlib.Path(file_path_str[len(root_prefix) :])
  return None


def main():
  parser = argparse.ArgumentParser()
  parser.add_argument(
      "--output", required=True, help="The output .srcjar file path."
  )
  parser.add_argument("--module_info", required=True)
  parser.add_argument(
      "--source-root",
      action="append",
      dest="source_roots",
      default=[],
      help="A directory to treat as a source root for prefix stripping.",
  )
  parser.add_argument(
      "--srcs", nargs="+", default=[], help="List of source files."
  )
  args = parser.parse_args()

  with tempfile.TemporaryDirectory() as tmpdir:
    stage_dir = pathlib.Path(tmpdir)

    module_info_dest = stage_dir / "java.base" / "module-info.java"
    module_info_dest.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy(args.module_info, module_info_dest)

    for src_file in args.srcs:
      path_for_search = src_file
      if path_for_search.startswith("jre_emul/"):
        path_for_search = path_for_search[len("jre_emul/") :]

      relative_path = find_java_package_path(path_for_search, args.source_roots)
      if not relative_path:
        sys.exit(f"ERROR: Could not find a matching source root for {src_file}")

      dest_path = stage_dir / "java.base" / relative_path
      dest_path.parent.mkdir(parents=True, exist_ok=True)
      shutil.copy(src_file, dest_path)

    # --- THIS IS THE UPDATED SECTION ---
    # 3. Zip the staged directory into the final srcjar with sorted entries.
    with zipfile.ZipFile(args.output, "w", zipfile.ZIP_DEFLATED) as zf:
      # Glob all files in the staging directory and sort them to ensure
      # the zip order is deterministic.
      all_files = sorted(list(stage_dir.rglob("*")))

      for full_path in all_files:
        # Calculate the path to be stored in the zip archive.
        archive_name = full_path.relative_to(stage_dir)
        zf.write(full_path, archive_name)


if __name__ == "__main__":
  main()
