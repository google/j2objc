// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#ifndef J2OBJC_MAIN_H_
#define J2OBJC_MAIN_H_

//  Main function that provides JVM-like start-up for OS X.
//  It's equivalent to j2objc/jre_emul/Classes/J2ObjCMain.m, but with
//  the main class name specified.
int j2objc_main(const char* className, int argc, const char *argv[]);

#endif  // J2OBJC_MAIN_H_
