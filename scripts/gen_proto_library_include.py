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

"""Script for generating a makefile with lists of generated proto files.

Generates the lists of .java, .m and .h files that will be generated from a set
of .proto files. Intended for use by proto_library.mk.

Usage:
  gen_proto_library_include.py proto1 proto2 ...
"""


import re
import sys

import parse_proto


def GetPackage(data):
  package = data.package

  if data.java_package:
    package = data.java_package
  package = re.sub('\\.', '/', package)
  if package:
    package = package + '/'

  return package


def GetGeneratedFilesForProto(filename, java_files, objc_files, header_files):
  """Parses a proto file and prints corresponding java class path names."""
  data = parse_proto.ParseProto(filename)
  package = GetPackage(data)

  java_files.append(package + data.outer_class + '.java')
  objc_files.append(package + data.outer_class + '.m')
  header_files.append(package + data.outer_class + '.h')

  if data.multiple_files:
    for m in data.messages:
      java_files.append(package + m + '.java')
      java_files.append(package + m + 'OrBuilder.java')
      objc_files.append(package + m + '.m')
      header_files.append(package + m + '.h')
      header_files.append(package + m + 'OrBuilder.h')
    for e in data.enums:
      java_files.append(package + e + '.java')
      objc_files.append(package + e + '.m')
      header_files.append(package + e + '.h')


def main():
  java_files = []
  objc_files = []
  header_files = []
  for filename in sys.argv[1:]:
    GetGeneratedFilesForProto(filename, java_files, objc_files, header_files)

  print('GENERATED_JAVA = \\')
  for f in java_files:
    print('    ' + f + ' \\')
  print('')

  print('GENERATED_SOURCES = \\')
  for f in objc_files:
    print('    ' + f + ' \\')
  print('')

  print('GENERATED_HEADERS = \\')
  for f in header_files:
    print('    ' + f + ' \\')
  print('')

if __name__ == '__main__':
  sys.exit(main())
