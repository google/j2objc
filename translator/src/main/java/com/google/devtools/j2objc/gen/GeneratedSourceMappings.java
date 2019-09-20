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

import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.Name;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains byte-offset mappings between identifiers in a source .java file and their equivalents in
 * a generated Objective-C header file.
 *
 * <p>These mappings may be converted to {@link
 * com.google.devtools.j2objc.gen.KytheIndexingMetadata} objects to support cross-language links in
 * <a href="http://kythe.io">Kythe.</a>
 */
public class GeneratedSourceMappings {

  /** Maps an identifier from a byte range in a source file and target file. */
  public static class Mapping {
    private final String identifier;
    private final int sourceBegin;
    private final int sourceEnd;
    private final int targetBegin;
    private final int targetEnd;

    private Mapping(
        String identifier, int sourceBegin, int sourceEnd, int targetBegin, int targetEnd) {
      this.identifier = identifier;
      this.sourceBegin = sourceBegin;
      this.sourceEnd = sourceEnd;
      this.targetBegin = targetBegin;
      this.targetEnd = targetEnd;
    }

    static Mapping fromMethodDeclaration(
        MethodDeclaration methodDeclaration, int targetBegin, int targetEnd) {
      assert methodDeclaration.getName() != null;
      Name name = methodDeclaration.getName();
      int sourceBegin = name.getStartPosition();
      int sourceEnd = sourceBegin + name.getLength();
      return new Mapping(name.toString(), sourceBegin, sourceEnd, targetBegin, targetEnd);
    }

    public String getIdentifier() {
      return identifier;
    }

    public int getSourceBegin() {
      return sourceBegin;
    }

    public int getSourceEnd() {
      return sourceEnd;
    }

    public int getTargetBegin() {
      return targetBegin;
    }

    public int getTargetEnd() {
      return targetEnd;
    }
  }

  private final Set<Mapping> mappings = new HashSet<>();
  private int targetOffset = 0;

  public void addMethodMapping(MethodDeclaration methodDeclaration, int targetBegin, int length) {
    assert methodDeclaration.getName() != null;
    if (methodDeclaration.getName().getStartPosition() != -1) {
      mappings.add(
          Mapping.fromMethodDeclaration(methodDeclaration, targetBegin, targetBegin + length));
    }
  }

  public Set<Mapping> getMappings() {
    return Collections.unmodifiableSet(mappings);
  }

  public void setTargetOffset(int targetOffset) {
    this.targetOffset = targetOffset;
  }

  public int getTargetOffset() {
    return targetOffset;
  }
}
