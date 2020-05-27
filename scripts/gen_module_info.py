#!/usr/bin/python
#
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

"""Script that generates a module-info.java for the given root directory.

Usage:
  $ gen_module_info.py --name <module_name> -root <path> -output <path>
"""

import argparse
import os

# Used by argparse to build help string.
USAGE_STRING = """
Script that generates a module-info.java for the given root directory.
"""


def Main():
  """Creates a module-info.java for the given root directory.

  The output file contains one entry for each subfolder containing at least
  one class file.
  """
  parser = argparse.ArgumentParser(description=USAGE_STRING)
  parser.add_argument(
      "--name",
      required=True,
      help="module name")
  parser.add_argument(
      "--root",
      required=True,
      help="path to root directory")
  parser.add_argument(
      "--output",
      required=True,
      help="path to output file")
  args = parser.parse_args()
  root = os.path.normpath(args.root)
  module_info = open(args.output, "w")
  module_info.write("module {} {{\n".format(args.name))
  for subdir, _, files in os.walk(root):
    if any(
        f.endswith(".class")
        for f in files
        if not f.endswith("package-info.class")):
      package = subdir[len(root) + 1:]  # "+ 1" to remove the "/"
      package = package.replace("/", ".")
      module_info.write("  exports {};\n".format(package))
  module_info.write("}\n")
  module_info.close()

if __name__ == "__main__":
  Main()
