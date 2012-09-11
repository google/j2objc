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
 * Defines an enumeration for annotation retention policies. Used in conjunction
 * with the {@link Retention} annotation to specify an annotation's time-to-live
 * in the overall development life cycle.
 *
 * @since 1.5
 */
public enum RetentionPolicy {
    /**
     * Annotation is only available in the source code.
     */
    SOURCE,
    /**
     * Annotation is available in the source code and in the class file, but not
     * at runtime. This is the default policy.
     */
    CLASS,
    /**
     * Annotation is available in the source code, the class file and is
     * available at runtime.
     */
    RUNTIME
}
