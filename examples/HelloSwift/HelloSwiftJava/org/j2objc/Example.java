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

package org.j2objc;

/** Class to test interoperability of transpiled code with Swift. */
public class Example {
  public static final String HELLO_J2OBJC = "Hello J2ObjC";

  /** enum example. */
  public enum Day {
    SUNDAY("Sun"),
    MONDAY("Mon"),
    TUESDAY("Tue"),
    WEDNESDAY("Wed"),
    THURSDAY("Thu"),
    FRIDAY("Fri"),
    SATURDAY("Sat");

    private final String shortName;

    private Day(String shortName) {
      this.shortName = shortName;
    }

    public @NonNull String getShortName() {
      return shortName;
    }
  }
}

@interface NonNull {}
