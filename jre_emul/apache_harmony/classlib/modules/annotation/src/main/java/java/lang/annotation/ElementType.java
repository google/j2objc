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
 * Defines an enumeration for Java program elements. It is used in conjunction
 * with the {@link Target} meta-annotation to restrict the use of an annotation
 * to certain program elements.
 *
 * @since 1.5
 */
public enum ElementType {
    /**
     * Class, interface or enum declaration.
     */
    TYPE,
    /**
     * Field declaration.
     */
    FIELD,
    /**
     * Method declaration.
     */
    METHOD,
    /**
     * Parameter declaration.
     */
    PARAMETER,
    /**
     * Constructor declaration.
     */
    CONSTRUCTOR,
    /**
     * Local variable declaration.
     */
    LOCAL_VARIABLE,
    /**
     * Annotation type declaration.
     */
    ANNOTATION_TYPE,
    /**
     * Package declaration.
     */
    PACKAGE
}
