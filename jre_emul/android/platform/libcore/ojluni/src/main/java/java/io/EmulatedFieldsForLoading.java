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

package java.io;

/**
 * An EmulatedFieldsForLoading is an object that represents a set of emulated
 * fields for an object being loaded. It is a concrete implementation for
 * ObjectInputStream.GetField
 *
 * @see ObjectInputStream.GetField
 * @see EmulatedFieldsForDumping
 */
class EmulatedFieldsForLoading extends ObjectInputStream.GetField {

    // The class descriptor with the declared fields the receiver emulates
    private ObjectStreamClass streamClass;

    // The actual representation, with a more powerful API (set&get)
    private EmulatedFields emulatedFields;

    /**
     * Constructs a new instance of EmulatedFieldsForDumping.
     *
     * @param streamClass
     *            an ObjectStreamClass, defining the class for which to emulate
     *            fields.
     */
    EmulatedFieldsForLoading(ObjectStreamClass streamClass) {
        this.streamClass = streamClass;
        emulatedFields = new EmulatedFields(streamClass.getLoadFields(), streamClass.fields());
    }

    /**
     * Return a boolean indicating if the field named <code>name</code> has
     * been assigned a value explicitly (false) or if it still holds a default
     * value for the type (true) because it hasn't been assigned to yet.
     *
     * @param name
     *            A String, the name of the field to test
     * @return <code>true</code> if the field holds it default value,
     *         <code>false</code> otherwise.
     *
     * @throws IOException
     *             If an IO error occurs
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    @Override
    public boolean defaulted(String name) throws IOException,
            IllegalArgumentException {
        return emulatedFields.defaulted(name);
    }

    /**
     * Return the actual EmulatedFields instance used by the receiver. We have
     * the actual work in a separate class so that the code can be shared. The
     * receiver has to be of a subclass of GetField.
     *
     * @return array of ObjectSlot the receiver represents.
     */
    EmulatedFields emulatedFields() {
        return emulatedFields;
    }

    /**
     * Find and return the byte value of a given field named <code>name</code>
     * in the receiver. If the field has not been assigned any value yet, the
     * default value <code>defaultValue</code> is returned instead.
     *
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, or the
     *         default value otherwise
     *
     * @throws IOException
     *             If an IO error occurs
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    @Override
    public byte get(String name, byte defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }

    /**
     * Find and return the char value of a given field named <code>name</code>
     * in the receiver. If the field has not been assigned any value yet, the
     * default value <code>defaultValue</code> is returned instead.
     *
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, or the
     *         default value otherwise
     *
     * @throws IOException
     *             If an IO error occurs
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    @Override
    public char get(String name, char defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }

    /**
     * Find and return the double value of a given field named <code>name</code>
     * in the receiver. If the field has not been assigned any value yet, the
     * default value <code>defaultValue</code> is returned instead.
     *
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, or the
     *         default value otherwise
     *
     * @throws IOException
     *             If an IO error occurs
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    @Override
    public double get(String name, double defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }

    /**
     * Find and return the float value of a given field named <code>name</code>
     * in the receiver. If the field has not been assigned any value yet, the
     * default value <code>defaultValue</code> is returned instead.
     *
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, or the
     *         default value otherwise
     *
     * @throws IOException
     *             If an IO error occurs
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    @Override
    public float get(String name, float defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }

    /**
     * Find and return the int value of a given field named <code>name</code>
     * in the receiver. If the field has not been assigned any value yet, the
     * default value <code>defaultValue</code> is returned instead.
     *
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, or the
     *         default value otherwise
     *
     * @throws IOException
     *             If an IO error occurs
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    @Override
    public int get(String name, int defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }

    /**
     * Find and return the long value of a given field named <code>name</code>
     * in the receiver. If the field has not been assigned any value yet, the
     * default value <code>defaultValue</code> is returned instead.
     *
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, or the
     *         default value otherwise
     *
     * @throws IOException
     *             If an IO error occurs
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    @Override
    public long get(String name, long defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }

    /**
     * Find and return the Object value of a given field named <code>name</code>
     * in the receiver. If the field has not been assigned any value yet, the
     * default value <code>defaultValue</code> is returned instead.
     *
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, or the
     *         default value otherwise
     *
     * @throws IOException
     *             If an IO error occurs
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    @Override
    public Object get(String name, Object defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }

    /**
     * Find and return the short value of a given field named <code>name</code>
     * in the receiver. If the field has not been assigned any value yet, the
     * default value <code>defaultValue</code> is returned instead.
     *
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, or the
     *         default value otherwise
     *
     * @throws IOException
     *             If an IO error occurs
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    @Override
    public short get(String name, short defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }

    /**
     * Find and return the boolean value of a given field named
     * <code>name</code> in the receiver. If the field has not been assigned
     * any value yet, the default value <code>defaultValue</code> is returned
     * instead.
     *
     * @param name
     *            A String, the name of the field to find
     * @param defaultValue
     *            Return value in case the field has not been assigned to yet.
     * @return the value of the given field if it has been assigned, or the
     *         default value otherwise
     *
     * @throws IOException
     *             If an IO error occurs
     * @throws IllegalArgumentException
     *             If the corresponding field can not be found.
     */
    @Override
    public boolean get(String name, boolean defaultValue) throws IOException,
            IllegalArgumentException {
        return emulatedFields.get(name, defaultValue);
    }

    /**
     * Return the class descriptor for which the emulated fields are defined.
     *
     * @return ObjectStreamClass The class descriptor for which the emulated
     *         fields are defined.
     */
    @Override
    public ObjectStreamClass getObjectStreamClass() {
        return streamClass;
    }
}
