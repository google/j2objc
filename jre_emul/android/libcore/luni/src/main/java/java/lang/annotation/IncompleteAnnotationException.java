/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package java.lang.annotation;

/**
 * Indicates that an element of an annotation type was accessed that was added
 * after the type was compiled or serialized. This does not apply to new
 * elements that have default values.
 *
 * @since 1.5
 */
public class IncompleteAnnotationException extends RuntimeException {

    private static final long serialVersionUID = 8445097402741811912L;

    private Class<? extends Annotation> annotationType;

    private String elementName;

    /**
     * Constructs an instance with the incomplete annotation type and the name
     * of the element that's missing.
     *
     * @param annotationType
     *            the annotation type.
     * @param elementName
     *            the name of the incomplete element.
     */
    public IncompleteAnnotationException(Class<? extends Annotation> annotationType,
            String elementName) {
        super("The element " + elementName + " is not complete for the annotation " +
                annotationType.getName());
        this.annotationType = annotationType;
        this.elementName = elementName;
    }

    /**
     * Returns the annotation type.
     *
     * @return a Class instance.
     */
    public Class<? extends Annotation> annotationType() {
        return annotationType;
    }

    /**
     * Returns the incomplete element's name.
     *
     * @return the name of the element.
     */
    public String elementName() {
        return elementName;
    }
}
