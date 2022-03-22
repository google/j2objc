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

"""Replaces the __metadata methods in one file with those from a second file.

Useful for injecting metadata into hand-coded JRE sources.

Usage:
  replace_metadata.py original_file replacement_file output_file
"""

import os
import re
import sys


def FirstChar(line):
  """Gets the first non-space character of the line."""
  idx = 0
  while line[idx] == ' ':
    idx += 1
  return line[idx]


def SplitLine(line, idx, new_lines):
  """Split the line at the given index."""
  new_lines.append(line[:idx] + '\n')
  while line[idx] == ' ':
    idx += 1
  return line[idx:]


def SplitString(line, new_lines):
  """Split a long string at the beginning of the line."""
  str_start = line.find('"')
  str_end = line[str_start + 1:].find('"')
  if str_end <= 100 and line[str_end + 1] == ' ':
    return SplitLine(line, str_end, new_lines)
  idx = min(str_end, 99)
  # Break the string on a semicolon if possible
  last_semi = line[:idx].rfind(';')
  if last_semi != -1:
    idx = last_semi + 1
  new_lines.append(line[:idx] + '"\n')
  while line[idx] == ' ':
    idx += 1
  return '"' + line[idx:]


def FormatLines(lines):
  """Reformat lines by splitting long lines."""
  new_lines = []
  for line in lines:
    if len(line) <= 100:
      new_lines.append(line)
      continue
    padding = 0
    for c in line:
      if c != ' ': break
      padding += 1
    small_indent = ' ' * padding
    indent = ' ' * (padding + 2)
    while len(line) > 100:
      i = line.find('= {')
      if i != -1:
        line = indent + SplitLine(line, i + 3, new_lines)
        continue
      m = re.search(r' *}', line)
      if m and m.start() <= 100:
        line = small_indent + SplitLine(line, m.start(), new_lines)
        continue
      i = line[:100].rfind(',')
      if i != -1:
        line = indent + SplitLine(line, i + 1, new_lines)
        continue
      if FirstChar(line) == '"' and line[:90].count('"') == 1:
        line = indent + SplitString(line, new_lines)
        continue
      break
    new_lines.append(line)
  return new_lines


def GetReplacementLines(class_name, contents):
  """Finds the replacement metadata for the given class."""
  current_class = ''
  in_replacement = False
  replacement_lines = []
  for line in contents:
    match = re.match(r'@implementation (\w+)', line)
    if match:
      current_class = match.group(1)
    if (line == '+ (const J2ObjcClassInfo *)__metadata {\n' and
        current_class == class_name):
      in_replacement = True
    if in_replacement:
      replacement_lines.append(line)
    if in_replacement and line == '}\n':
      break
  return FormatLines(replacement_lines)


def ProcessContents(orig_contents, replacement_contents):
  """Generates the updated file contents."""
  new_contents = []
  skip = False
  for line in orig_contents:
    match = re.match(r'@implementation (\w+)', line)
    if match:
      current_class = match.group(1)
    if line == '+ (const J2ObjcClassInfo *)__metadata {\n':
      replacement_lines = GetReplacementLines(current_class,
                                              replacement_contents)
      if replacement_lines:
        new_contents.extend(replacement_lines)
        skip = True
    if not skip:
      new_contents.append(line)
    if line == '}\n':
      skip = False
  return new_contents


def main():
  orig_file_name = sys.argv[1]
  replacement_file_name = sys.argv[2]
  output_file_name = sys.argv[3]
  with open(orig_file_name, 'r') as orig_file:
    with open(replacement_file_name, 'r') as replacement_file:
      orig_contents = orig_file.readlines()
      replacement_contents = replacement_file.readlines()
      new_contents = ProcessContents(orig_contents, replacement_contents)
      output_path = os.path.dirname(output_file_name)
      if not os.path.exists(output_path):
        os.makedirs(output_path)
      with open(output_file_name, 'w') as output_file:
        output_file.writelines(new_contents)

if __name__ == '__main__':
  sys.exit(main())
