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
import java.util.ArrayList;
import java.util.List;

/**
 * Specializes {@link GenericCovariantBase} to {@link Integer} while covariantly narrowing the
 * return type from {@link Object} to {@link List}.
 */
@ObjectiveCName("KmpCovariantListSubService")
public class CovariantListSubService extends GenericCovariantBase<Integer> {

  @Override
  public List<String> compute(Integer id, List<String> inputItems) {
    List<String> out = new ArrayList<>();
    out.add("java-covariant:" + id + ":" + inputItems.get(0));
    return out;
  }
}
