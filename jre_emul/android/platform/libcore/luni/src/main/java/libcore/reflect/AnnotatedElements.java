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
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.annotation.Repeatable;
import java.lang.reflect.*;
import java.util.ArrayList;

/**
 * Implementation of {@link AnnotatedElement}'s 1.8 methods.
 *
 * <p>This implementation is shared between all the classes implementing {@link AnnotatedElement},
 * avoiding code duplication.</p>
 *
 * @hide
 */
public final class AnnotatedElements {
  /**
   * Default implementation for {@link AnnotatedElement#getDeclaredAnnotation}.
   *
   * @return Directly present annotation of type {@code annotationClass} for {@code element},
   *         or {@code null} if none was found.
   */
  public static <T extends Annotation> T getDeclaredAnnotation(AnnotatedElement element,
      Class<T> annotationClass) {
    if (annotationClass == null) {
      throw new NullPointerException("annotationClass");
    }

    Annotation[] annotations = element.getDeclaredAnnotations();

    // Safeguard: getDeclaredAnnotations should never return null.
    if (annotations == null) {
      return null;
    }

    // The annotation might be directly present:
    // Return the first (and only) annotation whose class matches annotationClass.
    for (int i = 0; i < annotations.length; ++i) {
      if (annotationClass.isInstance(annotations[i])) {
        return (T)annotations[i];  // Safe because of above guard.
      }
    }

    // The annotation was *not* directly present:
    // If the array was empty, or we found no matches, return null.
    return null;
  }

  /**
   * Default implementation for {@link AnnotatedElement#getDeclaredAnnotationsByType}.
   *
   * @return Directly/indirectly present list of annotations of type {@code annotationClass} for
   *         {@code element}, or an empty array if none were found.
   */
  public static <T extends Annotation> T[] getDeclaredAnnotationsByType(AnnotatedElement element,
      Class<T> annotationClass) {
    if (annotationClass == null) {
      throw new NullPointerException("annotationClass");
    }

    Annotation[] annotations = element.getDeclaredAnnotations();

    // Store a list of repeatable annotations that have been extracted from their container.
    ArrayList<T> unfoldedAnnotations = new ArrayList<T>();

    Class<? extends Annotation> repeatableAnnotationClass =
        getRepeatableAnnotationContainerClassFor(annotationClass);

    for (int i = 0; i < annotations.length; ++i) {
      if (annotationClass.isInstance(annotations[i])) {
        // Is it directly present?
        unfoldedAnnotations.add((T)annotations[i]);  // Safe, guarded by above check.
      } else if (repeatableAnnotationClass != null &&
          repeatableAnnotationClass.isInstance(annotations[i])) {
        // Is it repeatably (indirectly) present?
        insertAnnotationValues(annotations[i], annotationClass, unfoldedAnnotations);
      }
    }

    return unfoldedAnnotations.toArray((T[])Array.newInstance(annotationClass, 0));
  }

  /**
   * Extracts annotations from a container annotation and inserts them into a list.
   *
   * <p>
   * Given a complex annotation "annotation", it should have a "T[] value()" method on it.
   * Call that method and add all of the nested annotations into unfoldedAnnotations list.
   * </p>
   */
  private static <T extends Annotation> void insertAnnotationValues(Annotation annotation,
      Class<T> annotationClass, ArrayList<T> unfoldedAnnotations) {
    // annotation is a complex annotation which has elements of instance annotationClass
    // (whose static type is T).
    //
    // @interface SomeName {  <--- = annotation.getClass()
    //   ...
    //   T[] value();        <--- T.class == annotationClass
    // }
    //
    // Use reflection to access these values.
    Class<T[]> annotationArrayClass =
        (Class<T[]>)((T[])Array.newInstance(annotationClass, 0)).getClass();

    Method valuesMethod;
    try {
      valuesMethod = annotation.getClass().getDeclaredMethod("value");
      // This will always succeed unless the annotation and its repeatable annotation class were
      // recompiled separately, then this is a binary incompatibility error.
    } catch (NoSuchMethodException e) {
      throw new AssertionError("annotation container = " + annotation +
          "annotation element class = " + annotationClass + "; missing value() method");
    } catch (SecurityException e) {
      throw new IncompleteAnnotationException(annotation.getClass(), "value");
    }

    // Ensure that value() returns a T[]
    if (!valuesMethod.getReturnType().isArray()) {
      throw new AssertionError("annotation container = " + annotation +
          "annotation element class = " + annotationClass + "; value() doesn't return array");
    }

    // Ensure that the T[] value() is actually the correct type (T==annotationClass).
    if (!annotationClass.equals(valuesMethod.getReturnType().getComponentType())) {
      throw new AssertionError("annotation container = " + annotation +
          "annotation element class = " + annotationClass + "; value() returns incorrect type");
    }

    // Append those values into the existing list.
    T[] nestedAnnotations;
    try {
      nestedAnnotations = (T[])valuesMethod.invoke(annotation);  // Safe because of #getMethod.
    } catch (IllegalAccessException|InvocationTargetException e) {
      throw new AssertionError(e);
    }

    for (int i = 0; i < nestedAnnotations.length; ++i) {
      unfoldedAnnotations.add(nestedAnnotations[i]);
    }
  }

  /**
   * Find the {@code \@Repeatable} container annotation class for an annotation class, or
   * {@code null}.
   *
   * <p>
   * Given:
   *
   * <code>
   *  @Repeatable(X.class)
   *  @interface SomeName {     <--- = annotationClass
   *  }...
   * </code>
   *
   * <p>
   * Returns {@code X.class}
   *
   * Otherwise if there was no {@code \@Repeatable} annotation, return {@code null}.
   * </p>
   */
  private static <T extends Annotation> Class<? extends Annotation>
      getRepeatableAnnotationContainerClassFor(Class<T> annotationClass) {

    Repeatable repeatableAnnotation = annotationClass.getDeclaredAnnotation(Repeatable.class);
    return (repeatableAnnotation == null) ? null : repeatableAnnotation.value();
  }

  /**
   * Default implementation of {@link AnnotatedElement#getAnnotationsByType}.
   *
   * <p>
   * This method does not handle inherited annotations and is
   * intended for use for {@code Method}, {@code Field}, {@code Package}.
   * The {@link Class#getAnnotationsByType} is implemented explicitly.
   * </p>
   *
   * @return Associated annotations of type {@code annotationClass} for {@code element}.
   */
  public static <T extends Annotation> T[] getAnnotationsByType(AnnotatedElement element,
      Class<T> annotationClass) {
    if (annotationClass == null) {
      throw new NullPointerException("annotationClass");
    }

    // Find any associated annotations [directly or repeatably (indirectly) present on this class].
    T[] annotations = element.getDeclaredAnnotationsByType(annotationClass);
    if (annotations == null) {
      throw new AssertionError("annotations must not be null");  // Internal error.
    }

    // If nothing was found, we would look for associated annotations recursively up to the root
    // class. However this can only happen if AnnotatedElement is a Class, which is handled
    // in the Class override of this method.
    return annotations;
  }

  private AnnotatedElements() {
    throw new AssertionError("Instances of AnnotatedElements not allowed");
  }
}

