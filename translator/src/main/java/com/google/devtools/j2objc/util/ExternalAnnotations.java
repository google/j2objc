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

package com.google.devtools.j2objc.util;

import com.google.common.collect.ImmutableList;
import com.google.devtools.j2objc.types.GeneratedAnnotationMirror;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.AnnotatedConstruct;
import scenelib.annotations.el.AScene;
import scenelib.annotations.io.IndexFileParser;

/**
 * Helps to forward external annotations from {@link com.google.devtools.j2objc.Options} to {@link
 * com.google.devtools.j2objc.translate.ExternalAnnotationInjector}
 *
 * Records external annotations found by the
 * {@link com.google.devtools.j2objc.translate.ExternalAnnotationInjector}.
 */
public final class ExternalAnnotations {

  // An annotated scene represents the annotations on a set of Java classes and packages.
  private final AScene scene = new AScene();

  private static final Map<AnnotatedConstruct, List<GeneratedAnnotationMirror>> annotations =
      new HashMap<>();

  public static void add(AnnotatedConstruct construct, GeneratedAnnotationMirror annotation) {
    annotations.computeIfAbsent(construct, k -> new ArrayList<>()).add(annotation);
  }

  public static List<GeneratedAnnotationMirror> get(AnnotatedConstruct construct) {
    return annotations.getOrDefault(construct, ImmutableList.of());
  }

  /**
   * {@link com.google.devtools.j2objc.Options} should use this method to process external
   * annotation files.
   */
  public void addExternalAnnotationFile(String file) throws IOException {
    IndexFileParser.parseFile(file, scene);
  }

  /**
   * {@link com.google.devtools.j2objc.translate.ExternalAnnotationInjector} should use this method
   * to retrieve external annotations.
   */
  public AScene getScene() {
    return scene;
  }
}
