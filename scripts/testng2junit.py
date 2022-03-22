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
"""Migrates all java files in a directory from TestNG to JUnit.

Used by J2ObjC to translate Android's libcore new TestNG unit tests.

Usage:
    testng2junit.py <directory_to_migrate>
"""

import os
import sys
import regex as re


def MigrateImports(content):
  """Updates import statements from TestNG to JUnit."""
  content_new = re.sub('org.testng.annotations.Test', 'org.junit.Test', content)

  content_new = re.sub('org.testng.annotations.BeforeMethod;',
                       'org.junit.Before;', content_new)

  content_new = re.sub('org.testng.annotations.BeforeClass;',
                       'org.junit.BeforeClass;', content_new)

  content_new = re.sub(
      'import org.testng.annotations.DataProvider;',
      '''import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.runner.RunWith;''', content_new)

  # for remaining imports such as assertEquals
  content_new = re.sub('testng', 'junit', content_new)

  return content_new


def MigrateAnnotations(content):
  content_new = re.sub('@Test\npublic class', 'public class', content)

  content_new = re.sub('@BeforeMethod', '@Before', content_new)

  return content_new


def MigrateDataProviders(content):
  """TestNG allows a DataProvider to be renamed."""
  # Make a list of tuples mapping the
  # new name to original name.
  # @DataProvider(name="MillisInstantNoNanos")
  # Object[][] provider_factory_millis_long() {
  data_provider_regex = re.compile(
      r'@DataProvider\(name\s?=\s?(".*")\)\s*.*\[\]\[\] (.*)\(\)')
  data_provider_rename_tuples = re.findall(data_provider_regex, content)

  # Remove the renamed data provider from test annotation and put it in.
  # @UseDataProvider annotation
  # @Test(dataProvider="MillisInstantNoNanos")
  data_provider_test_regex = re.compile(
      r'@Test\(dataProvider\s*=\s*(".*"),?\s?(.*)?\)')
  content_new = data_provider_test_regex.sub(
      '@Test(\\2)\n    @UseDataProvider(\\1)', content)

  for tup in data_provider_rename_tuples:
    content_new = re.sub(tup[0], '"' + tup[1] + '"', content_new)

  content_new = re.sub('@DataProvider.*', '@DataProvider', content_new)

  if 'DataProvider' in content_new:
    content_new = re.sub('public class',
                         '@RunWith(DataProviderRunner.class)\npublic class',
                         content_new)

  # In JUnit data providers have to be public and static.
  object_array_provider_regex = re.compile(r'Object\[\]\[\] (.*)\(\)')
  content_new = object_array_provider_regex.sub(
      'public static Object[][] \\1()', content_new)

  return content_new


def MigrateExceptions(content):
  content_new = re.sub('expectedExceptions', 'expected', content)

  exception_patt = re.compile(r'expected\s?=\s?{(.*)}')
  content_new = exception_patt.sub('expected=\\1', content_new)

  return content_new


def MigrateAsserts(content):
  """Converts TestNG assertions to JUnit."""
  # TestNG has an overload for assertEquals that takes parameters:
  # obj1, obj2, message. JUnit also has this overload but takes parameters:
  # message, obj1, obj2.
  assert_equals_overload_regex = re.compile(
      r'assertEquals\((.*), (.*), (("|String).*)\);')
  content_new = assert_equals_overload_regex.sub('assertEquals(\\3, \\1, \\2);',
                                                 content)

  multiline_assert_equals_overload_regex = re.compile(
      r'assertEquals\((.*), (.*),\s*(".*\s*\+.*)\);')
  content_new = multiline_assert_equals_overload_regex.sub(
      'assertEquals(\\3, \\1, \\2);', content_new)

  multiline_assert_equals_overload_regex = re.compile(
      r'assertEquals\((.*), (.*),\s*(".*\s*\+ String.*\s*.*)\);')
  content_new = multiline_assert_equals_overload_regex.sub(
      'assertEquals(\\3, \\1, \\2);', content_new)

  # TestNG has overloads for assert(True|False|NotNull|Same) taking two
  # parameters: condition, message. JUnit also has these overloads but takes
  # parameters: message, condition.
  assert_conditional_regex = re.compile(
      r'assert(True|False|NotNull|Same)\((.*), (.*)\);')
  content_new = assert_conditional_regex.sub('assert\\1(\\3, \\2);',
                                             content_new)

  return content_new


def main():
  directory_to_migrate = sys.argv[1]
  directory_contents = os.listdir(directory_to_migrate)
  if directory_contents is None:
    print('usage: testng2junit.py <directory_to_migrate>')
    sys.exit(1)
  full_paths = [
      os.path.join(directory_to_migrate, x) for x in directory_contents
  ]
  files = [x for x in full_paths if os.path.isfile(x)]
  for file_name in files:
    if not file_name.endswith('java'):
      continue
    with open(file_name, 'r') as f:
      content = f.read()
      content_new = MigrateImports(content)
      content_new = MigrateAnnotations(content_new)
      content_new = MigrateDataProviders(content_new)
      content_new = MigrateExceptions(content_new)
      content_new = MigrateAsserts(content_new)
      with open(file_name, 'w') as fn:
        fn.write(content_new)


if __name__ == '__main__':
  sys.exit(main())
