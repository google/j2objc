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

// A J2ObjC "namespace" wrapper for the ICU functions used by jre_emul.
//
// This is a private header, only to be used by jre_emul classes. Apps
// and other libraries should use ICU's headers directly.

#ifndef _J2ObjC_ICU_H_
#define _J2ObjC_ICU_H_

// Enable function renaming.
#undef U_DISABLE_RENAMING
#define U_DISABLE_RENAMING 0

// Define a _j2objc function suffix, without any version number. This
// is necessary because the version number may be different, depending on
// the iOS version.
#include "unicode/uvernum.h"
#undef U_ICU_ENTRY_POINT_RENAME
#define U_ICU_ENTRY_POINT_RENAME(x) x ## _j2objc

#include "unicode/uchar.h"
#include "unicode/uregex.h"

// JRE classes referencing ICU need to call this function during initialization.
U_STABLE void J2ObjC_icu_init(void);

#endif /* _J2ObjC_ICU_H_ */
