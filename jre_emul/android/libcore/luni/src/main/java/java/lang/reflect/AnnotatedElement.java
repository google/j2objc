/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.lang.reflect;

import java.lang.annotation.Annotation;

/**
 * This interface provides reflective access to annotation information.
 *
 * @since 1.5
 */
public interface AnnotatedElement {

    /**
     * Returns, for this element, the annotation with the specified type, or
     * {@code null} if no annotation with the specified type is present
     * (including inherited annotations).
     *
     * @param annotationType
     *            the type of the annotation to search for
     * @return the annotation with the specified type or {@code null}
     * @throws NullPointerException
     *             if {@code annotationType} is {@code null}
     */
    <T extends Annotation> T getAnnotation(Class<T> annotationType);

    /**
     * Returns, for this element, an array containing all annotations (including
     * inherited annotations). If there are no annotations present, this method
     * returns a zero length array.
     *
     * @return an array of all annotations for this element
     */
    Annotation[] getAnnotations();

    /**
     * Returns, for this element, all annotations that are explicitly declared
     * (not inherited). If there are no declared annotations present, this
     * method returns a zero length array.
     *
     * @return an array of annotations declared for this element
     */
    Annotation[] getDeclaredAnnotations();

    /**
     * Indicates whether or not this element has an annotation with the
     * specified annotation type (including inherited annotations).
     *
     * @param annotationType
     *            the type of the annotation to search for
     * @return {@code true} if the annotation exists, {@code false} otherwise
     * @throws NullPointerException
     *             if {@code annotationType} is {@code null}
     */
    boolean isAnnotationPresent(Class<? extends Annotation> annotationType);
}
