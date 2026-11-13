/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.j2objc.kmp;

import com.google.j2objc.annotations.ObjectiveCName;

/** Base value object used for bounded generic KMP return tests. */
@ObjectiveCName("KmpItem")
public class Item {

  private final String name;

  public Item(String name) {
    this.name = name;
  }

  /** Returns this item's name. */
  public String getName() {
    return name;
  }
}
