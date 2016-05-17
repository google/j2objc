/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

/**
 * Stubbed out version of {@link AnnotatedElement}'s 1.8 methods, to be removed
 * during OpenJDK merge.
 *
 * @hide
 */
public final class AnnotatedElements {

  public static <T extends Annotation> T getDeclaredAnnotation(AnnotatedElement element,
      Class<T> annotationClass) {
    throw new AssertionError("not implemented");
  }

  public static <T extends Annotation> T[] getDeclaredAnnotationsByType(AnnotatedElement element,
      Class<T> annotationClass) {
    throw new AssertionError("not implemented");
  }

  public static <T extends Annotation> T[] getAnnotationsByType(AnnotatedElement element,
      Class<T> annotationClass) {
    throw new AssertionError("not implemented");
  }

  private AnnotatedElements() {
    throw new AssertionError("Instances of AnnotatedElements not allowed");
  }
}

