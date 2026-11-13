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

import com.google.j2objc.annotations.ObjectiveCKmpMethod;
import com.google.j2objc.annotations.ObjectiveCName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Non-final concrete service exposing KMP-adapted value and void methods. */
@ObjectiveCName("KmpOpenService")
public class OpenService {

  private List<String> recordedTags = Collections.emptyList();

  /** Returns derived tags for {@code prefix}. */
  @ObjectiveCKmpMethod(selector = "fetchTags:", adapter = KmpTestAdapter.class)
  public List<String> getTags(List<String> prefix) {
    List<String> out = new ArrayList<>();
    for (String item : prefix) {
      out.add("java-base:" + item);
    }
    return out;
  }

  /** Records {@code tags} on this service instance. */
  @ObjectiveCKmpMethod(selector = "recordTags:", adapter = KmpTestAdapter.class)
  public void setTags(List<String> tags) {
    this.recordedTags = new ArrayList<>(tags);
  }

  /** Returns the tags stored by the Java {@link #setTags} implementation. */
  public List<String> getRecordedTags() {
    return recordedTags;
  }
}
