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

# Builds a J2ObjC translated Guava library.

default: dist

clean: clean_jre clean_android
	@:

clean_jre:
	@$(MAKE) -f jre.mk clean

clean_android:
	@$(MAKE) -C android -f android.mk clean

dist: dist_jre dist_android
	@:

dist_jre:
	@$(MAKE) -f jre.mk dist

dist_android:
	@$(MAKE) -C android -f android.mk dist

framework: framework_jre framework_android
	@:

framework_jre:
	@$(MAKE) -f jre.mk framework

framework_android:
	@$(MAKE) -C android -f android.mk framework

java: java_jre java_android
	@:

java_jre:
	@$(MAKE) -f jre.mk java

java_android:
	@$(MAKE) -C android -f android.mk java
