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

# To create XCFrameworks for all JRE subset libraries:
#
# 1. cd to the j2objc/jre_emul directory
# 2. run "./build_subset_frameworks.sh"
#
# To delete these frameworks, run "./build_subset_frameworks clean".

for mk in \
  android_util_framework.mk \
  jre_beans_framework.mk \
  jre_channels_framework.mk \
  jre_concurrent_framework.mk \
  jre_core_framework.mk \
  jre_file_framework.mk \
  jre_icu_framework.mk \
  jre_io_framework.mk \
  jre_net_framework.mk \
  jre_security_framework.mk \
  jre_sql_framework.mk \
  jre_ssl_framework.mk \
  jre_time_framework.mk \
  jre_util_framework.mk \
  jre_xml_framework.mk \
  jre_zip_framework.mk \
  json_framework.mk; do make -f subset_frameworks/$mk $*; done
