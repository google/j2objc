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

package com.google.devtools.j2objc.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Metadata required to create links for indexing in <a href="http://kythe.io">Kythe.</a>
 *
 * <p>The JSON objects produced by this class conform to the format expected by Kythe's
 * postprocessing pipeline; this allows Kythe to create links from source .java files to generated
 * .h files.
 */
public class KytheIndexingMetadata {

  /**
   * A Vname is a unique identifier for a node in a Kythe semantic graph.
   *
   * <p>For more information, see <a href="https://kythe.io/docs/kythe-uri-spec.html">the Kythe
   * documentation.</a>
   */
  static class VName {
    private final String corpus;
    private final String path;

    private VName(String corpus, String path) {
      this.corpus = corpus;
      this.path = path;
    }

    private String toJson() {
      return String.format(
          "{\"corpus\":\"%s\",\"path\":\"%s\",\"language\":\"java\"}", corpus, path);
    }
  }

  static class AnchorAnchorMetadata {
    private final String type = "anchor_anchor";
    private final int sourceBegin;
    private final int sourceEnd;
    private final int targetBegin;
    private final int targetEnd;
    private final String edge = "/kythe/edge/imputes";
    private final VName sourceVName;

    AnchorAnchorMetadata(
        int sourceBegin,
        int sourceEnd,
        int targetBegin,
        int targetEnd,
        String corpus,
        String path) {
      this.sourceBegin = sourceBegin;
      this.sourceEnd = sourceEnd;
      this.targetBegin = targetBegin;
      this.targetEnd = targetEnd;
      this.sourceVName = new VName(corpus, path);
    }

    private String toJson() {
      return String.format(
          "{\"type\":\"%s\",\"source_begin\":%d,\"source_end\":%d,\"target_begin\":%d,"
              + "\"target_end\":%d,\"edge\":\"%s\",\"source_vname\":%s}",
          type, sourceBegin, sourceEnd, targetBegin, targetEnd, edge, sourceVName.toJson());
    }
  }

  private final String type = "kythe0";
  private final List<AnchorAnchorMetadata> meta = new ArrayList<>();

  public void addAnchorAnchor(
      int sourceBegin,
      int sourceEnd,
      int targetBegin,
      int targetEnd,
      String sourceCorpus,
      String sourcePath) {
    meta.add(
        new AnchorAnchorMetadata(
            sourceBegin, sourceEnd, targetBegin, targetEnd, sourceCorpus, sourcePath));
  }

  public boolean isEmpty() {
    return meta.isEmpty();
  }

  public String toJson() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(String.format("{\"type\":\"%s\",\"meta\":[", type));
    stringBuilder.append(
        meta.stream().map(AnchorAnchorMetadata::toJson).collect(Collectors.joining(",")));
    stringBuilder.append("]}");
    return stringBuilder.toString();
  }
}
