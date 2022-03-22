#!/usr/bin/python3
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

"""
Script for generating a jar file from a javac-like sourcepath and a
list of Java source files.

Usage:
  $ gen_java_source_jar.py [-h] [-sourcepath <path>] -o output-file ...
"""


# Used by argparse to build help string.
USAGE_STRING ="""
  Generates a jar file that contains Java sources in a layout useful
  for debugger use. Source file names may be relative, starting with
  their package directory, such as "java/lang/String.java". The
  -sourcepath argument specifies the root directories to search for
  the specified source files."""

import argparse
import os
import shutil
import subprocess
import sys
import tempfile


def BuildSourceJar(jar_file, sources):
  """Creates a jar file of a list of source files."""
  staging_dir = tempfile.mkdtemp()
  for f in sources.keys():
    temp_src = os.path.join(staging_dir, f)
    pkg_dir = os.path.dirname(temp_src)
    if not os.path.exists(pkg_dir):
      os.makedirs(os.path.dirname(temp_src))
    shutil.copyfile(sources[f], temp_src)
  out_file = os.path.join(os.getcwd(), jar_file)
  proc = subprocess.Popen("jar cf {} *".format(out_file), shell=True,
                          cwd=staging_dir)
  proc.wait()
  shutil.rmtree(staging_dir)
  return proc.returncode;


def GetSourcePath(file, sourcepath):
  """Locates a relative file in a list of source paths."""
  for root in sourcepath:
    path = os.path.join(root, file)
    if os.path.exists(path):
      return path
  return None


def GetSourceFile(file, sourcepath):
  """Return a relative file if it is embedded in a path."""
  for root in sourcepath:
    if file.find(root) == 0:
      prefix_length = len(root)
      if not root.endswith('/'):
        prefix_length += 1
      relative_file = file[prefix_length:]
      return relative_file
  return None


if __name__ == "__main__":
  errors = 0
  parser = argparse.ArgumentParser(description=USAGE_STRING)
  parser.add_argument("-sourcepath", metavar="<path>",
                      help="specify where to find source files")
  parser.add_argument("-o", metavar="jar-file", help="the jar file to create")
  parser.add_argument("java_files", help="Java source files",
                      nargs=argparse.REMAINDER)
  args = parser.parse_args()
  if args.o == None:
    print("error: no jar file specified")
    errors += 1
  if args.sourcepath == None:
    sourcepath = [ '.' ]
  else:
    sourcepath = args.sourcepath.split(':')
  file_map = {}
  for f in args.java_files:
    relative_file = GetSourceFile(f, sourcepath)
    if relative_file:
      f = relative_file
    path = GetSourcePath(f, sourcepath)
    if path:
      file_map[f] = path
    else:
      print("file not found: {}".format(file))
      errors += 1
  if not errors:
    errors = BuildSourceJar(args.o, file_map)
  sys.exit(errors)
