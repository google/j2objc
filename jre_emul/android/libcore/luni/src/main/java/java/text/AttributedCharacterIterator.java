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

import java.io.InvalidObjectException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

/**
 * Extends the
 * {@link CharacterIterator} interface, adding support for iterating over
 * attributes and not only characters. An
 * {@code AttributedCharacterIterator} also allows the user to find runs and
 * their limits. Runs are defined as ranges of characters that all have the same
 * attributes with the same values.
 */
public interface AttributedCharacterIterator extends CharacterIterator {

    /**
     * Defines keys for text attributes.
     */
    public static class Attribute implements Serializable {

        private static final long serialVersionUID = -9142742483513960612L;

        /**
         * This attribute marks segments from an input method. Most input
         * methods create these segments for words.
         *
         * The value objects are of the type {@code Annotation} which contain
         * {@code null}.
         */
        public static final Attribute INPUT_METHOD_SEGMENT = new Attribute("input_method_segment");

        /**
         * The attribute describing the language of a character. The value
         * objects are of type {@code Locale} or a subtype of it.
         */
        public static final Attribute LANGUAGE = new Attribute("language");

        /**
         * For languages that have different reading directions of text (like
         * Japanese), this attribute allows to define which reading should be
         * used. The value objects are of type {@code Annotation} which
         * contain a {@code String}.
         */
        public static final Attribute READING = new Attribute("reading");

        private String name;

        /**
         * The constructor for an {@code Attribute} with the name passed.
         *
         * @param name
         *            the name of the new {@code Attribute}.
         */
        protected Attribute(String name) {
            this.name = name;
        }

        /**
         * Compares this attribute with the specified object. Checks if both
         * objects are the same instance. It is defined final so all subclasses
         * have the same behavior for this method.
         *
         * @param object
         *            the object to compare against.
         * @return {@code true} if the object passed is equal to this instance;
         *         {@code false} otherwise.
         */
        @Override
        public final boolean equals(Object object) {
            return this == object;
        }

        /**
         * Returns the name of this attribute.
         *
         * @return the name of this attribute.
         */
        protected String getName() {
            return name;
        }

        /**
         * Calculates the hash code for objects of type {@code Attribute}. It
         * is defined final so all sub types calculate their hash code
         * identically.
         *
         * @return the hash code for this instance of {@code Attribute}.
         */
        @Override
        public final int hashCode() {
            return super.hashCode();
        }

        /**
         * Resolves a deserialized instance to the correct constant attribute.
         *
         * @return the {@code Attribute} this instance represents.
         * @throws InvalidObjectException
         *             if this instance is not of type {@code Attribute.class}
         *             or if it is not a known {@code Attribute}.
         */
        protected Object readResolve() throws InvalidObjectException {
            /*
             * This class is used like Java enums, where all instances are
             * defined as fields of their own class. To preserve identity
             * equality, resolve to the canonical instance when deserialized.
             */
            try {
                for (Field field : getClass().getFields()) {
                    if (field.getType() == getClass() && Modifier.isStatic(field.getModifiers())) {
                        Attribute candidate = (Attribute) field.get(null);
                        if (name.equals(candidate.name)) {
                            return candidate;
                        }
                    }
                }
            } catch (IllegalAccessException e) {
            }
            throw new InvalidObjectException("Failed to resolve " + this);
        }

        /**
         * Returns the name of the class followed by a "(", the name of the
         * attribute, and a ")".
         *
         * @return the string representing this instance.
         */
        @Override
        public String toString() {
            return getClass().getName() + '(' + getName() + ')';
        }
    }

    /**
     * Returns a set of attributes present in the {@code
     * AttributedCharacterIterator}. An empty set is returned if no attributes
     * were defined.
     *
     * @return a set of attribute keys; may be empty.
     */
    public Set<Attribute> getAllAttributeKeys();

    /**
     * Returns the value stored in the attribute for the current character. If
     * the attribute was not defined then {@code null} is returned.
     *
     * @param attribute the attribute for which the value should be returned.
     * @return the value of the requested attribute for the current character or
     *         {@code null} if it was not defined.
     */
    public Object getAttribute(Attribute attribute);

    /**
     * Returns a map of all attributes of the current character. If no
     * attributes were defined for the current character then an empty map is
     * returned.
     *
     * @return a map of all attributes for the current character or an empty
     *         map.
     */
    public Map<Attribute, Object> getAttributes();

    /**
     * Returns the index of the last character in the run having the same
     * attributes as the current character.
     *
     * @return the index of the last character of the current run.
     */
    public int getRunLimit();

    /**
     * Returns the index of the last character in the run that has the same
     * attribute value for the given attribute as the current character.
     *
     * @param attribute
     *            the attribute which the run is based on.
     * @return the index of the last character of the current run.
     */
    public int getRunLimit(Attribute attribute);

    /**
     * Returns the index of the last character in the run that has the same
     * attribute values for the attributes in the set as the current character.
     *
     * @param attributes
     *            the set of attributes which the run is based on.
     * @return the index of the last character of the current run.
     */
    public int getRunLimit(Set<? extends Attribute> attributes);

    /**
     * Returns the index of the first character in the run that has the same
     * attributes as the current character.
     *
     * @return the index of the last character of the current run.
     */
    public int getRunStart();

    /**
     * Returns the index of the first character in the run that has the same
     * attribute value for the given attribute as the current character.
     *
     * @param attribute
     *            the attribute which the run is based on.
     * @return the index of the last character of the current run.
     */
    public int getRunStart(Attribute attribute);

    /**
     * Returns the index of the first character in the run that has the same
     * attribute values for the attributes in the set as the current character.
     *
     * @param attributes
     *            the set of attributes which the run is based on.
     * @return the index of the last character of the current run.
     */
    public int getRunStart(Set<? extends Attribute> attributes);
}
