#!/bin/bash
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

set -e
curl -O https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/jarjar/jarjar-src-1.4.zip
unzip jarjar-src-1.4.zip
mv jarjar-1.4 jarjar-1.4-patched
unzip jarjar-src-1.4.zip
rm jarjar-src-1.4.zip
cd jarjar-1.4-patched
rm -f lib/asm-4.0.jar lib/asm-commons-4.0.jar
curl -o lib/asm-7.0.jar http://central.maven.org/maven2/org/ow2/asm/asm/7.0/asm-7.0.jar
curl -o lib/asm-commons-7.0.jar http://central.maven.org/maven2/org/ow2/asm/asm-commons/7.0/asm-commons-7.0.jar
patch -p1 < ../jarjar.patch
cd ..
