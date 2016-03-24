#! /usr/bin/awk -f
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

# Generates a JUnit 4 Suite of all classes specified as arguments.
# This script is normally invoked by Make, ensuring that the list of
# test classes is up-to-date.

BEGIN {
  print("import org.junit.runner.RunWith;")
  print("import org.junit.runners.Suite;\n")
  print("@RunWith(Suite.class)")
  print("@Suite.SuiteClasses({")
  for (i = 1; i < ARGC; i++) {
    printf("  %s.class,\n", ARGV[i])
  }
  print("})")
  print("public class AllJreTests {\n")
  print("}")
  exit(0)
}
