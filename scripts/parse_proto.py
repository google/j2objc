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

"""Parses metadata from a .proto file

Parses metadata from a .proto file including various options and a list of top
level messages and enums declared within the proto file.
"""


import re
import string


def CamelCase(name):
  return ''.join(string.capwords(name, '_').split('_'))


class ProtoMetadata:
  """Parses a proto file to extract options and other metadata."""

  multiple_files = False
  package = ''
  java_package = ''
  java_api_version = 2
  java_alt_api_package = ''
  outer_class = ''

  def __init__(self):
    self.messages = []
    self.enums = []


def MatchOptions(line, data):
  # package
  match = re.match(r'package\s*([\w\.]+)\s*;', line)
  if match:
    data.package = match.group(1)

  # java package
  match = re.match(r'option\s+java_package\s*=\s*"([^"]+)', line)
  if match:
    data.java_package = match.group(1)


  # outer classname
  match = re.match(r'option\s+java_outer_classname\s*=\s*"([^"]+)"', line)
  if match:
    data.outer_class = match.group(1)

  # multiple files?
  match = re.match(r'option\s+java_multiple_files\s*=\s*(\S+)\s*;', line)
  if match:
    data.multiple_files = True if match.group(1).lower() == 'true' else False


def MatchTypes(line, data):
  # messages and enums
  match = re.match(r'\s*(message|enum)\s+(\S+)\s+{$', line)
  if match:
    if match.group(1) == 'message':
      data.messages.append(match.group(2))
    else:
      data.enums.append(match.group(2))


def MatchGroups(line, data):
  match = re.match(
      r'\s*(required|optional|repeated)\s+group\s+(\S+)\s+=\s+\d+\s+{$', line)
  if match:
    data.messages.append(match.group(2))


def ParseProto(filename):
  data = ProtoMetadata()
  data.outer_class = CamelCase(filename.rsplit('/', 1)[-1].split('.', 1)[0])
  with open(filename, 'r') as fh:
    brace_depth = 0
    inside_extend = False

    for line in fh:
      line = line.rstrip()

      MatchOptions(line, data)

      if brace_depth == 0 and re.match(r'\s*extend\s+\S+\s+{$', line):
        inside_extend = True
        brace_depth += 1
        continue

      # never emit anything for nested definitions
      if brace_depth == 0:
        MatchTypes(line, data)
      elif brace_depth == 1 and inside_extend:
        MatchGroups(line, data)

      brace_depth += line.count('{')
      brace_depth -= line.count('}')
      if brace_depth == 0:
        inside_extend = False

  return data
