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

//
// Throwable.h
// JreEmulation
//
// Backwards-compatibility header for those classes that haven't
// upgraded to use the java.lang.Throwable->NSException mapping.
// TODO(tball): remove file when clients have updated.
//

#ifndef JavaLangThrowable_H
#define JavaLangThrowable_H

#include "J2ObjC_header.h"
#include "NSException+JavaThrowable.h"

// Compatibility alias and methods are defined in NSException+JavaThrowable.h

#endif // JavaLangThrowable_H
