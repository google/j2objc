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
 * A helper interface with constants used by the serialization implementation.
 */
public abstract interface ObjectStreamConstants {

    /**
     * The stream header's magic number.
     */
    public static final short STREAM_MAGIC = (short) 0xaced;

    /**
     * The stream header's version number.
     */
    public static final short STREAM_VERSION = 5;

    // These are tags to indicate the stream contents

    /**
     * The minimum tag value.
     */
    public static final byte TC_BASE = 0x70;

    /**
     * Tag to mark a {@code null} object reference.
     */
    public static final byte TC_NULL = (byte) 0x70;

    /**
     * Tag to mark a reference to an object that has already been written to the
     * stream.
     */
    public static final byte TC_REFERENCE = (byte) 0x71;

    /**
     * Tag to mark a new class descriptor.
     */
    public static final byte TC_CLASSDESC = (byte) 0x72;

    /**
     * Tag to mark a new object.
     */
    public static final byte TC_OBJECT = (byte) 0x73;

    /**
     * Tag to mark a new string.
     */
    public static final byte TC_STRING = (byte) 0x74;

    /**
     * Tag to mark a new array.
     */
    public static final byte TC_ARRAY = (byte) 0x75;

    /**
     * Tag to mark a reference to a class.
     */
    public static final byte TC_CLASS = (byte) 0x76;

    /**
     * Tag to mark a block of optional data. The byte following this tag
     * indicates the size of the block.
     */
    public static final byte TC_BLOCKDATA = (byte) 0x77;

    /**
     * Tag to mark the end of block data blocks for an object.
     */
    public static final byte TC_ENDBLOCKDATA = (byte) 0x78;

    /**
     * Tag to mark a stream reset.
     */
    public static final byte TC_RESET = (byte) 0x79;

    /**
     * Tag to mark a long block of data. The long following this tag
     * indicates the size of the block.
     */
    public static final byte TC_BLOCKDATALONG = (byte) 0x7A;

    /**
     * Tag to mark an exception.
     */
    public static final byte TC_EXCEPTION = (byte) 0x7B;

    /**
     * Tag to mark a long string.
     */
    public static final byte TC_LONGSTRING = (byte) 0x7C;

    /**
     * Tag to mark a new proxy class descriptor.
     */
    public static final byte TC_PROXYCLASSDESC = (byte) 0x7D;

    /**
     * The maximum tag value.
     */
    public static final byte TC_MAX = 0x7E;

    /**
     * Handle for the first object that gets serialized.
     */
    public static final int baseWireHandle = 0x007e0000;

    /**
     * Stream protocol version 1.
     */
    public static final int PROTOCOL_VERSION_1 = 1;

    /**
     * Stream protocol version 2.
     */
    public static final int PROTOCOL_VERSION_2 = 2;

    /**
     * Permission constant to enable subclassing of ObjectInputStream and
     * ObjectOutputStream.
     */
    public static final SerializablePermission SUBCLASS_IMPLEMENTATION_PERMISSION = new SerializablePermission(
            "enableSubclassImplementation");

    /**
     * Permission constant to enable object substitution during serialization
     * and deserialization.
     */
    public static final SerializablePermission SUBSTITUTION_PERMISSION = new SerializablePermission(
            "enableSubstitution");

    // Flags that indicate if the object was serializable, externalizable
    // and had a writeObject method when dumped.
    /**
     * Bit mask for the {@code flag} field in ObjectStreamClass. Indicates
     * that a serializable class has its own {@code writeObject} method.
     */
    public static final byte SC_WRITE_METHOD = 0x01; // If SC_SERIALIZABLE

    /**
     * Bit mask for the {@code flag} field in ObjectStreamClass. Indicates
     * that a class is serializable.
     */
    public static final byte SC_SERIALIZABLE = 0x02;

    /**
     * Bit mask for the {@code flag} field in ObjectStreamClass. Indicates
     * that a class is externalizable.
     */
    public static final byte SC_EXTERNALIZABLE = 0x04;

    /**
     * Bit mask for the {@code flag} field in ObjectStreamClass. Indicates
     * that an externalizable class is written in block data mode.
     */
    public static final byte SC_BLOCK_DATA = 0x08; // If SC_EXTERNALIZABLE

    /**
     * Tag to mark a new enum.
     */
    public static final byte TC_ENUM = 0x7E;

    /**
     * Bit mask for the {@code flag} field in ObjectStreamClass. Indicates
     * that a class is an enum type.
     */
    public static final byte SC_ENUM = 0x10;
}
