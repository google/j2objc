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


def main():
  parser = argparse.ArgumentParser()
  parser.add_argument(
      "--output", required=True, help="The output .srcjar file path."
  )
  parser.add_argument(
      "--module_info",
      required=True,
      help="The path to the module-info.java file.",
  )
  parser.add_argument(
      "--srcjar", required=True, help="The source jar to process."
  )
  args = parser.parse_args()

  with tempfile.TemporaryDirectory() as tmpdir:
    stage_dir = pathlib.Path(tmpdir)
    java_base_dir = stage_dir / "java.base"
    java_base_dir.mkdir(parents=True, exist_ok=True)

    # Copy the module-info.java file to the root of the module sources.
    shutil.copy(args.module_info, java_base_dir / "module-info.java")

    # Unzip the source jar into the module directory.
    with zipfile.ZipFile(args.srcjar, "r") as zf:
      zf.extractall(java_base_dir)

    # Delete the META-INF directory after unzipping.
    meta_inf_path = java_base_dir / "META-INF"
    if meta_inf_path.exists() and meta_inf_path.is_dir():
      shutil.rmtree(meta_inf_path)

    # Zip the staged directory into the final srcjar with sorted entries.
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
