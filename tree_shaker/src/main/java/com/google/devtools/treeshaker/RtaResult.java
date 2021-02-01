/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.treeshaker;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.util.Collection;

/**
 * Wrapper Object containing the set of live types and live members discovered by the RTA algorithm.
 */
@AutoValue
abstract class RtaResult {
  abstract ImmutableList<String> getUnusedTypes();
  abstract ImmutableList<String> getUnusedMethods();

  @AutoValue.Builder
  abstract static class Builder {
    abstract ImmutableList.Builder<String> unusedTypesBuilder();
    abstract ImmutableList.Builder<String> unusedMethodsBuilder();
    abstract RtaResult build();
  }

  static RtaResult build(Collection<Type> types) {
    Builder builder = new AutoValue_RtaResult.Builder();

    for (Type type : types) {
      if (type.isLive()) {
        for (Member member : type.getMembers()) {
          if (!member.isLive()) {
            builder.unusedMethodsBuilder().add(
                member.getDeclaringType().getName() + "." + member.getName());
          }
        }
      } else {
        builder.unusedTypesBuilder().add(type.getName());
      }
    }

    return builder.build();
  }
}
