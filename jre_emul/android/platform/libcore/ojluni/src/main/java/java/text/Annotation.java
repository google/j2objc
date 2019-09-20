/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.text;

/**
 * Wrapper for a text attribute value which represents an annotation. An
 * annotation has two special aspects:
 * <ol>
 * <li>it is connected to a range of main text; if this range or the main text
 * is changed then the annotation becomes invalid,</li>
 * <li>it can not be joined with adjacent annotations even if the text attribute
 * value is the same.</li>
 * </ol>
 * <p>
 * By wrapping text attribute values into an {@code Annotation}, these aspects
 * will be taken into account when handling annotation text and the
 * corresponding main text.
 * <p>
 * Note: There is no semantic connection between this annotation class and the
 * {@code java.lang.annotation} package.
 */
public class Annotation {

    private Object value;

    /**
     * Constructs a new {@code Annotation}.
     *
     * @param attribute the attribute attached to this annotation. This may be
     *        {@code null}.
     */
    public Annotation(Object attribute) {
        value = attribute;
    }

    /**
     * Returns the value of this annotation. The value may be {@code null}.
     *
     * @return the value of this annotation or {@code null}.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns this annotation in string representation.
     *
     * @return the string representation of this annotation.
     */
    @Override
    public String toString() {
        return getClass().getName() + "[value=" + value + ']';
    }
}
