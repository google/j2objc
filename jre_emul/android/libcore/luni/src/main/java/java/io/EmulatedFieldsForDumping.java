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
 * An EmulatedFieldsForDumping is an object that represents a set of emulated
 * fields for an object being dumped. It is a concrete implementation for
 * ObjectOutputStream.PutField
 *
 *
 * @see ObjectOutputStream.PutField
 * @see EmulatedFieldsForLoading
 */
class EmulatedFieldsForDumping extends ObjectOutputStream.PutField {
    // Record the ObjectOutputStream that created this PutField for checking in 'write'.
    private final ObjectOutputStream oos;

    // The actual representation, with a more powerful API (set&get)
    private EmulatedFields emulatedFields;

    /**
     * Constructs a new instance of EmulatedFieldsForDumping.
     *
     * @param streamClass
     *            a ObjectStreamClass, which describe the fields to be emulated
     *            (names, types, etc).
     */
    EmulatedFieldsForDumping(ObjectOutputStream oos, ObjectStreamClass streamClass) {
        this.oos = oos;
        this.emulatedFields = new EmulatedFields(streamClass.fields(), (ObjectStreamField[]) null);
    }

    /**
     * Return the actual EmulatedFields instance used by the receiver. We have
     * the actual work in a separate class so that the code can be shared. The
     * receiver has to be of a subclass of PutField.
     *
     * @return array of ObjectSlot the receiver represents.
     */
    EmulatedFields emulatedFields() {
        return emulatedFields;
    }

    /**
     * Find and set the byte value of a given field named <code>name</code> in
     * the receiver.
     *
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     */
    @Override
    public void put(String name, byte value) {
        emulatedFields.put(name, value);
    }

    /**
     * Find and set the char value of a given field named <code>name</code> in
     * the receiver.
     *
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     */
    @Override
    public void put(String name, char value) {
        emulatedFields.put(name, value);
    }

    /**
     * Find and set the double value of a given field named <code>name</code>
     * in the receiver.
     *
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     */
    @Override
    public void put(String name, double value) {
        emulatedFields.put(name, value);
    }

    /**
     * Find and set the float value of a given field named <code>name</code>
     * in the receiver.
     *
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     */
    @Override
    public void put(String name, float value) {
        emulatedFields.put(name, value);
    }

    /**
     * Find and set the int value of a given field named <code>name</code> in
     * the receiver.
     *
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     */
    @Override
    public void put(String name, int value) {
        emulatedFields.put(name, value);
    }

    /**
     * Find and set the long value of a given field named <code>name</code> in
     * the receiver.
     *
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     */
    @Override
    public void put(String name, long value) {
        emulatedFields.put(name, value);
    }

    /**
     * Find and set the Object value of a given field named <code>name</code>
     * in the receiver.
     *
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     */
    @Override
    public void put(String name, Object value) {
        emulatedFields.put(name, value);
    }

    /**
     * Find and set the short value of a given field named <code>name</code>
     * in the receiver.
     *
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     */
    @Override
    public void put(String name, short value) {
        emulatedFields.put(name, value);
    }

    /**
     * Find and set the boolean value of a given field named <code>name</code>
     * in the receiver.
     *
     * @param name
     *            A String, the name of the field to set
     * @param value
     *            New value for the field.
     */
    @Override
    public void put(String name, boolean value) {
        emulatedFields.put(name, value);
    }

    /**
     * Write the field values to the specified ObjectOutput.
     *
     * @param output
     *            the ObjectOutput
     *
     * @throws IOException
     *             If an IO exception happened when writing the field values.
     */
    @Override
    @Deprecated
    public void write(ObjectOutput output) throws IOException {
        if (!output.equals(oos)) {
            throw new IllegalArgumentException("Attempting to write to a different stream than the one that created this PutField");
        }
        for (EmulatedFields.ObjectSlot slot : emulatedFields.slots()) {
            Object fieldValue = slot.getFieldValue();
            Class<?> type = slot.getField().getType();
            if (type == int.class) {
                output.writeInt(fieldValue != null ? ((Integer) fieldValue).intValue() : 0);
            } else if (type == byte.class) {
                output.writeByte(fieldValue != null ? ((Byte) fieldValue).byteValue() : 0);
            } else if (type == char.class) {
                output.writeChar(fieldValue != null ? ((Character) fieldValue).charValue() : 0);
            } else if (type == short.class) {
                output.writeShort(fieldValue != null ? ((Short) fieldValue).shortValue() : 0);
            } else if (type == boolean.class) {
                output.writeBoolean(fieldValue != null ? ((Boolean) fieldValue).booleanValue() : false);
            } else if (type == long.class) {
                output.writeLong(fieldValue != null ? ((Long) fieldValue).longValue() : 0);
            } else if (type == float.class) {
                output.writeFloat(fieldValue != null ? ((Float) fieldValue).floatValue() : 0);
            } else if (type == double.class) {
                output.writeDouble(fieldValue != null ? ((Double) fieldValue).doubleValue() : 0);
            } else {
                // Either array or Object
                output.writeObject(fieldValue);
            }
        }
    }
}
