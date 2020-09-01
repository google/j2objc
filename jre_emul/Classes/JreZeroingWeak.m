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
//  JreZeroingWeak.h
//  JreEmulation
//
//  Created by Michał Pociecha-Łoś
//

#import "java/lang/ref/WeakReference.h"

id JreZeroingWeakGet(id zeroingWeak) { return zeroingWeak ? [zeroingWeak get] : nil; }

id JreMakeZeroingWeak(id object) {
  return object ? create_JavaLangRefWeakReference_initWithId_(object) : nil;
}
