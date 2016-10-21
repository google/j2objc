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

import com.google.j2objc.WeakProxy;

import java.io.EmulatedFields.ObjectSlot;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import libcore.util.EmptyArray;

/**
 * A specialized {@link InputStream} that is able to read (deserialize) Java
 * objects as well as primitive data types (int, byte, char etc.). The data has
 * typically been saved using an ObjectOutputStream.
 *
 * @see ObjectOutputStream
 * @see ObjectInput
 * @see Serializable
 * @see Externalizable
 */
public class ObjectInputStream extends InputStream implements ObjectInput, ObjectStreamConstants {

    // TODO: this is non-static to avoid sync contention. Would static be faster?
    private InputStream emptyStream = new ByteArrayInputStream(EmptyArray.BYTE);

    // To put into objectsRead when reading unsharedObject
    private static final Object UNSHARED_OBJ = new Object(); // $NON-LOCK-1$

    // If the receiver has already read & not consumed a TC code
    private boolean hasPushbackTC;

    // Push back TC code if the variable above is true
    private byte pushbackTC;

    // How many nested levels to readObject. When we reach 0 we have to validate
    // the graph then reset it
    private int nestedLevels;

    // All objects are assigned an ID (integer handle)
    private int nextHandle;

    // Where we read from
    private DataInputStream input;

    // Where we read primitive types from
    private DataInputStream primitiveTypes;

    // Where we keep primitive type data
    private InputStream primitiveData = emptyStream;

    // Resolve object is a mechanism for replacement
    private boolean enableResolve;

    /**
     * All the objects we've read, indexed by their serialization handle (minus the base offset).
     */
    private ArrayList<Object> objectsRead;

    // Used by defaultReadObject
    private Object currentObject;

    // Used by defaultReadObject
    private ObjectStreamClass currentClass;

    // All validations to be executed when the complete graph is read. See inner
    // type below.
    private InputValidationDesc[] validations;

    // Allows the receiver to decide if it needs to call readObjectOverride
    private boolean subclassOverridingImplementation;

    // Original caller's class loader, used to perform class lookups
    private ClassLoader callerClassLoader;

    // false when reading missing fields
    private boolean mustResolve = true;

    // Handle for the current class descriptor
    private int descriptorHandle = -1;

    private static final HashMap<String, Class<?>> PRIMITIVE_CLASSES = new HashMap<String, Class<?>>();
    static {
        PRIMITIVE_CLASSES.put("boolean", boolean.class);
        PRIMITIVE_CLASSES.put("byte", byte.class);
        PRIMITIVE_CLASSES.put("char", char.class);
        PRIMITIVE_CLASSES.put("double", double.class);
        PRIMITIVE_CLASSES.put("float", float.class);
        PRIMITIVE_CLASSES.put("int", int.class);
        PRIMITIVE_CLASSES.put("long", long.class);
        PRIMITIVE_CLASSES.put("short", short.class);
        PRIMITIVE_CLASSES.put("void", void.class);
    }

    // Internal type used to keep track of validators & corresponding priority
    static class InputValidationDesc {
        ObjectInputValidation validator;

        int priority;
    }

    /**
     * GetField is an inner class that provides access to the persistent fields
     * read from the source stream.
     */
    public abstract static class GetField {
        /**
         * Gets the ObjectStreamClass that describes a field.
         *
         * @return the descriptor class for a serialized field.
         */
        public abstract ObjectStreamClass getObjectStreamClass();

        /**
         * Indicates if the field identified by {@code name} is defaulted. This
         * means that it has no value in this stream.
         *
         * @param name
         *            the name of the field to check.
         * @return {@code true} if the field is defaulted, {@code false}
         *         otherwise.
         * @throws IllegalArgumentException
         *             if {@code name} does not identify a serializable field.
         * @throws IOException
         *             if an error occurs while reading from the source input
         *             stream.
         */
        public abstract boolean defaulted(String name) throws IOException,
                IllegalArgumentException;

        /**
         * Gets the value of the boolean field identified by {@code name} from
         * the persistent field.
         *
         * @param name
         *            the name of the field to get.
         * @param defaultValue
         *            the default value that is used if the field does not have
         *            a value when read from the source stream.
         * @return the value of the field identified by {@code name}.
         * @throws IOException
         *             if an error occurs while reading from the source input
         *             stream.
         * @throws IllegalArgumentException
         *             if the type of the field identified by {@code name} is
         *             not {@code boolean}.
         */
        public abstract boolean get(String name, boolean defaultValue)
                throws IOException, IllegalArgumentException;

        /**
         * Gets the value of the character field identified by {@code name} from
         * the persistent field.
         *
         * @param name
         *            the name of the field to get.
         * @param defaultValue
         *            the default value that is used if the field does not have
         *            a value when read from the source stream.
         * @return the value of the field identified by {@code name}.
         * @throws IOException
         *             if an error occurs while reading from the source input
         *             stream.
         * @throws IllegalArgumentException
         *             if the type of the field identified by {@code name} is
         *             not {@code char}.
         */
        public abstract char get(String name, char defaultValue)
                throws IOException, IllegalArgumentException;

        /**
         * Gets the value of the byte field identified by {@code name} from the
         * persistent field.
         *
         * @param name
         *            the name of the field to get.
         * @param defaultValue
         *            the default value that is used if the field does not have
         *            a value when read from the source stream.
         * @return the value of the field identified by {@code name}.
         * @throws IOException
         *             if an error occurs while reading from the source input
         *             stream.
         * @throws IllegalArgumentException
         *             if the type of the field identified by {@code name} is
         *             not {@code byte}.
         */
        public abstract byte get(String name, byte defaultValue)
                throws IOException, IllegalArgumentException;

        /**
         * Gets the value of the short field identified by {@code name} from the
         * persistent field.
         *
         * @param name
         *            the name of the field to get.
         * @param defaultValue
         *            the default value that is used if the field does not have
         *            a value when read from the source stream.
         * @return the value of the field identified by {@code name}.
         * @throws IOException
         *             if an error occurs while reading from the source input
         *             stream.
         * @throws IllegalArgumentException
         *             if the type of the field identified by {@code name} is
         *             not {@code short}.
         */
        public abstract short get(String name, short defaultValue)
                throws IOException, IllegalArgumentException;

        /**
         * Gets the value of the integer field identified by {@code name} from
         * the persistent field.
         *
         * @param name
         *            the name of the field to get.
         * @param defaultValue
         *            the default value that is used if the field does not have
         *            a value when read from the source stream.
         * @return the value of the field identified by {@code name}.
         * @throws IOException
         *             if an error occurs while reading from the source input
         *             stream.
         * @throws IllegalArgumentException
         *             if the type of the field identified by {@code name} is
         *             not {@code int}.
         */
        public abstract int get(String name, int defaultValue)
                throws IOException, IllegalArgumentException;

        /**
         * Gets the value of the long field identified by {@code name} from the
         * persistent field.
         *
         * @param name
         *            the name of the field to get.
         * @param defaultValue
         *            the default value that is used if the field does not have
         *            a value when read from the source stream.
         * @return the value of the field identified by {@code name}.
         * @throws IOException
         *             if an error occurs while reading from the source input
         *             stream.
         * @throws IllegalArgumentException
         *             if the type of the field identified by {@code name} is
         *             not {@code long}.
         */
        public abstract long get(String name, long defaultValue)
                throws IOException, IllegalArgumentException;

        /**
         * Gets the value of the float field identified by {@code name} from the
         * persistent field.
         *
         * @param name
         *            the name of the field to get.
         * @param defaultValue
         *            the default value that is used if the field does not have
         *            a value when read from the source stream.
         * @return the value of the field identified by {@code name}.
         * @throws IOException
         *             if an error occurs while reading from the source input
         *             stream.
         * @throws IllegalArgumentException
         *             if the type of the field identified by {@code float} is
         *             not {@code char}.
         */
        public abstract float get(String name, float defaultValue)
                throws IOException, IllegalArgumentException;

        /**
         * Gets the value of the double field identified by {@code name} from
         * the persistent field.
         *
         * @param name
         *            the name of the field to get.
         * @param defaultValue
         *            the default value that is used if the field does not have
         *            a value when read from the source stream.
         * @return the value of the field identified by {@code name}.
         * @throws IOException
         *             if an error occurs while reading from the source input
         *             stream.
         * @throws IllegalArgumentException
         *             if the type of the field identified by {@code name} is
         *             not {@code double}.
         */
        public abstract double get(String name, double defaultValue)
                throws IOException, IllegalArgumentException;

        /**
         * Gets the value of the object field identified by {@code name} from
         * the persistent field.
         *
         * @param name
         *            the name of the field to get.
         * @param defaultValue
         *            the default value that is used if the field does not have
         *            a value when read from the source stream.
         * @return the value of the field identified by {@code name}.
         * @throws IOException
         *             if an error occurs while reading from the source input
         *             stream.
         * @throws IllegalArgumentException
         *             if the type of the field identified by {@code name} is
         *             not {@code Object}.
         */
        public abstract Object get(String name, Object defaultValue)
                throws IOException, IllegalArgumentException;
    }

    /**
     * Constructs a new ObjectInputStream. This default constructor can be used
     * by subclasses that do not want to use the public constructor if it
     * allocates unneeded data.
     *
     * @throws IOException
     *             if an error occurs when creating this stream.
     */
    protected ObjectInputStream() throws IOException {
        // WARNING - we should throw IOException if not called from a subclass
        // according to the JavaDoc. Add the test.
        this.subclassOverridingImplementation = true;
    }

    /**
     * Constructs a new ObjectInputStream that reads from the InputStream
     * {@code input}.
     *
     * @param input
     *            the non-null source InputStream to filter reads on.
     * @throws IOException
     *             if an error occurs while reading the stream header.
     * @throws StreamCorruptedException
     *             if the source stream does not contain serialized objects that
     *             can be read.
     */
    public ObjectInputStream(InputStream input) throws StreamCorruptedException, IOException {
        this.input = (input instanceof DataInputStream)
                ? (DataInputStream) input : new DataInputStream(input);
        primitiveTypes = new DataInputStream(WeakProxy.forObject(this));
        enableResolve = false;
        this.subclassOverridingImplementation = false;
        resetState();
        nestedLevels = 0;
        // So read...() methods can be used by
        // subclasses during readStreamHeader()
        primitiveData = this.input;
        // Has to be done here according to the specification
        readStreamHeader();
        primitiveData = emptyStream;
    }

    @Override
    public int available() throws IOException {
        // returns 0 if next data is an object, or N if reading primitive types
        checkReadPrimitiveTypes();
        return primitiveData.available();
    }

    /**
     * Checks to if it is ok to read primitive types from this stream at
     * this point. One is not supposed to read primitive types when about to
     * read an object, for example, so an exception has to be thrown.
     *
     * @throws IOException
     *             If any IO problem occurred when trying to read primitive type
     *             or if it is illegal to read primitive types
     */
    private void checkReadPrimitiveTypes() throws IOException {
        // If we still have primitive data, it is ok to read primitive data
        if (primitiveData == input || primitiveData.available() > 0) {
            return;
        }

        // If we got here either we had no Stream previously created or
        // we no longer have data in that one, so get more bytes
        do {
            int next = 0;
            if (hasPushbackTC) {
                hasPushbackTC = false;
            } else {
                next = input.read();
                pushbackTC = (byte) next;
            }
            switch (pushbackTC) {
                case TC_BLOCKDATA:
                    primitiveData = new ByteArrayInputStream(readBlockData());
                    return;
                case TC_BLOCKDATALONG:
                    primitiveData = new ByteArrayInputStream(readBlockDataLong());
                    return;
                case TC_RESET:
                    resetState();
                    break;
                default:
                    if (next != -1) {
                        pushbackTC();
                    }
                    return;
            }
            // Only TC_RESET falls through
        } while (true);
    }

    /**
     * Closes this stream. This implementation closes the source stream.
     *
     * @throws IOException
     *             if an error occurs while closing this stream.
     */
    @Override
    public void close() throws IOException {
        input.close();
    }

    /**
     * Default method to read objects from this stream. Serializable fields
     * defined in the object's class and superclasses are read from the source
     * stream.
     *
     * @throws ClassNotFoundException
     *             if the object's class cannot be found.
     * @throws IOException
     *             if an I/O error occurs while reading the object data.
     * @throws NotActiveException
     *             if this method is not called from {@code readObject()}.
     * @see ObjectOutputStream#defaultWriteObject
     */
    public void defaultReadObject() throws IOException, ClassNotFoundException,
            NotActiveException {
        if (currentObject != null || !mustResolve) {
            readFieldValues(currentObject, currentClass);
        } else {
            throw new NotActiveException();
        }
    }

    /**
     * Enables object replacement for this stream. By default this is not
     * enabled. Only trusted subclasses (loaded with system class loader) are
     * allowed to change this status.
     *
     * @param enable
     *            {@code true} to enable object replacement; {@code false} to
     *            disable it.
     * @return the previous setting.
     * @see #resolveObject
     * @see ObjectOutputStream#enableReplaceObject
     */
    protected boolean enableResolveObject(boolean enable) {
        boolean originalValue = enableResolve;
        enableResolve = enable;
        return originalValue;
    }

    /**
     * Return the next {@code int} handle to be used to indicate cyclic
     * references being loaded from the stream.
     *
     * @return the next handle to represent the next cyclic reference
     */
    private int nextHandle() {
        return nextHandle++;
    }

    /**
     * Return the next token code (TC) from the receiver, which indicates what
     * kind of object follows
     *
     * @return the next TC from the receiver
     *
     * @throws IOException
     *             If an IO error occurs
     *
     * @see ObjectStreamConstants
     */
    private byte nextTC() throws IOException {
        if (hasPushbackTC) {
            hasPushbackTC = false; // We are consuming it
        } else {
            // Just in case a later call decides to really push it back,
            // we don't require the caller to pass it as parameter
            pushbackTC = input.readByte();
        }
        return pushbackTC;
    }

    /**
     * Pushes back the last TC code read
     */
    private void pushbackTC() {
        hasPushbackTC = true;
    }

    /**
     * Reads a single byte from the source stream and returns it as an integer
     * in the range from 0 to 255. Returns -1 if the end of the source stream
     * has been reached. Blocks if no input is available.
     *
     * @return the byte read or -1 if the end of the source stream has been
     *         reached.
     * @throws IOException
     *             if an error occurs while reading from this stream.
     */
    @Override
    public int read() throws IOException {
        checkReadPrimitiveTypes();
        return primitiveData.read();
    }

    /**
     * Reads at most {@code length} bytes from the source stream and stores them
     * in byte array {@code buffer} starting at offset {@code count}. Blocks
     * until {@code count} bytes have been read, the end of the source stream is
     * detected or an exception is thrown.
     *
     * @param buffer
     *            the array in which to store the bytes read.
     * @param byteOffset
     *            the initial position in {@code buffer} to store the bytes
     *            read from the source stream.
     * @param byteCount
     *            the maximum number of bytes to store in {@code buffer}.
     * @return the number of bytes read or -1 if the end of the source input
     *         stream has been reached.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code length < 0}, or if
     *             {@code offset + length} is greater than the length of
     *             {@code buffer}.
     * @throws IOException
     *             if an error occurs while reading from this stream.
     * @throws NullPointerException
     *             if {@code buffer} is {@code null}.
     */
    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        Arrays.checkOffsetAndCount(buffer.length, byteOffset, byteCount);
        if (byteCount == 0) {
            return 0;
        }
        checkReadPrimitiveTypes();
        return primitiveData.read(buffer, byteOffset, byteCount);
    }

    /**
     * Reads and returns an array of raw bytes with primitive data. The array
     * will have up to 255 bytes. The primitive data will be in the format
     * described by {@code DataOutputStream}.
     *
     * @return The primitive data read, as raw bytes
     *
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     */
    private byte[] readBlockData() throws IOException {
        byte[] result = new byte[input.readByte() & 0xff];
        input.readFully(result);
        return result;
    }

    /**
     * Reads and returns an array of raw bytes with primitive data. The array
     * will have more than 255 bytes. The primitive data will be in the format
     * described by {@code DataOutputStream}.
     *
     * @return The primitive data read, as raw bytes
     *
     * @throws IOException
     *             If an IO exception happened when reading the primitive data.
     */
    private byte[] readBlockDataLong() throws IOException {
        byte[] result = new byte[input.readInt()];
        input.readFully(result);
        return result;
    }

    /**
     * Reads a boolean from the source stream.
     *
     * @return the boolean value read from the source stream.
     * @throws EOFException
     *             if the end of the input is reached before the read
     *             request can be satisfied.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     */
    public boolean readBoolean() throws IOException {
        return primitiveTypes.readBoolean();
    }

    /**
     * Reads a byte (8 bit) from the source stream.
     *
     * @return the byte value read from the source stream.
     * @throws EOFException
     *             if the end of the input is reached before the read
     *             request can be satisfied.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     */
    public byte readByte() throws IOException {
        return primitiveTypes.readByte();
    }

    /**
     * Reads a character (16 bit) from the source stream.
     *
     * @return the char value read from the source stream.
     * @throws EOFException
     *             if the end of the input is reached before the read
     *             request can be satisfied.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     */
    public char readChar() throws IOException {
        return primitiveTypes.readChar();
    }

    /**
     * Reads and discards block data and objects until TC_ENDBLOCKDATA is found.
     *
     * @throws IOException
     *             If an IO exception happened when reading the optional class
     *             annotation.
     * @throws ClassNotFoundException
     *             If the class corresponding to the class descriptor could not
     *             be found.
     */
    private void discardData() throws ClassNotFoundException, IOException {
        primitiveData = emptyStream;
        boolean resolve = mustResolve;
        mustResolve = false;
        do {
            byte tc = nextTC();
            if (tc == TC_ENDBLOCKDATA) {
                mustResolve = resolve;
                return; // End of annotation
            }
            readContent(tc);
        } while (true);
    }

    /**
     * Reads a class descriptor (an {@code ObjectStreamClass}) from the
     * stream.
     *
     * @return the class descriptor read from the stream
     *
     * @throws IOException
     *             If an IO exception happened when reading the class
     *             descriptor.
     * @throws ClassNotFoundException
     *             If the class corresponding to the class descriptor could not
     *             be found.
     */
    private ObjectStreamClass readClassDesc() throws ClassNotFoundException, IOException {
        byte tc = nextTC();
        switch (tc) {
            case TC_CLASSDESC:
                return readNewClassDesc(false);
            case TC_PROXYCLASSDESC:
                Class<?> proxyClass = readNewProxyClassDesc();
                ObjectStreamClass streamClass = ObjectStreamClass.lookup(proxyClass);
                streamClass.setLoadFields(ObjectStreamClass.NO_FIELDS);
                registerObjectRead(streamClass, nextHandle(), false);
                checkedSetSuperClassDesc(streamClass, readClassDesc());
                return streamClass;
            case TC_REFERENCE:
                return (ObjectStreamClass) readCyclicReference();
            case TC_NULL:
                return null;
            default:
                throw corruptStream(tc);
        }
    }

    private StreamCorruptedException corruptStream(byte tc) throws StreamCorruptedException {
        throw new StreamCorruptedException("Wrong format: " + Integer.toHexString(tc & 0xff));
    }

    /**
     * Reads the content of the receiver based on the previously read token
     * {@code tc}.
     *
     * @param tc
     *            The token code for the next item in the stream
     * @return the object read from the stream
     *
     * @throws IOException
     *             If an IO exception happened when reading the class
     *             descriptor.
     * @throws ClassNotFoundException
     *             If the class corresponding to the object being read could not
     *             be found.
     */
    private Object readContent(byte tc) throws ClassNotFoundException,
            IOException {
        switch (tc) {
            case TC_BLOCKDATA:
                return readBlockData();
            case TC_BLOCKDATALONG:
                return readBlockDataLong();
            case TC_CLASS:
                return readNewClass(false);
            case TC_CLASSDESC:
                return readNewClassDesc(false);
            case TC_ARRAY:
                return readNewArray(false);
            case TC_OBJECT:
                return readNewObject(false);
            case TC_STRING:
                return readNewString(false);
            case TC_LONGSTRING:
                return readNewLongString(false);
            case TC_REFERENCE:
                return readCyclicReference();
            case TC_NULL:
                return null;
            case TC_EXCEPTION:
                Exception exc = readException();
                throw new WriteAbortedException("Read an exception", exc);
            case TC_RESET:
                resetState();
                return null;
            default:
                throw corruptStream(tc);
        }
    }

    /**
     * Reads the content of the receiver based on the previously read token
     * {@code tc}. Primitive data content is considered an error.
     *
     * @param unshared
     *            read the object unshared
     * @return the object read from the stream
     *
     * @throws IOException
     *             If an IO exception happened when reading the class
     *             descriptor.
     * @throws ClassNotFoundException
     *             If the class corresponding to the object being read could not
     *             be found.
     */
    private Object readNonPrimitiveContent(boolean unshared)
            throws ClassNotFoundException, IOException {
        checkReadPrimitiveTypes();
        int remaining = primitiveData.available();
        if (remaining > 0) {
            OptionalDataException e = new OptionalDataException(remaining);
            e.length = remaining;
            throw e;
        }

        do {
            byte tc = nextTC();
            switch (tc) {
                case TC_CLASS:
                    return readNewClass(unshared);
                case TC_CLASSDESC:
                    return readNewClassDesc(unshared);
                case TC_ARRAY:
                    return readNewArray(unshared);
                case TC_OBJECT:
                    return readNewObject(unshared);
                case TC_STRING:
                    return readNewString(unshared);
                case TC_LONGSTRING:
                    return readNewLongString(unshared);
                case TC_ENUM:
                    return readEnum(unshared);
                case TC_REFERENCE:
                    if (unshared) {
                        readNewHandle();
                        throw new InvalidObjectException("Unshared read of back reference");
                    }
                    return readCyclicReference();
                case TC_NULL:
                    return null;
                case TC_EXCEPTION:
                    Exception exc = readException();
                    throw new WriteAbortedException("Read an exception", exc);
                case TC_RESET:
                    resetState();
                    break;
                case TC_ENDBLOCKDATA: // Can occur reading class annotation
                    pushbackTC();
                    OptionalDataException e = new OptionalDataException(true);
                    e.eof = true;
                    throw e;
                default:
                    throw corruptStream(tc);
            }
            // Only TC_RESET falls through
        } while (true);
    }

    /**
     * Reads the next item from the stream assuming it is a cyclic reference to
     * an object previously read. Return the actual object previously read.
     *
     * @return the object previously read from the stream
     *
     * @throws IOException
     *             If an IO exception happened when reading the class
     *             descriptor.
     * @throws InvalidObjectException
     *             If the cyclic reference is not valid.
     */
    private Object readCyclicReference() throws InvalidObjectException, IOException {
        return registeredObjectRead(readNewHandle());
    }

    /**
     * Reads a double (64 bit) from the source stream.
     *
     * @return the double value read from the source stream.
     * @throws EOFException
     *             if the end of the input is reached before the read
     *             request can be satisfied.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     */
    public double readDouble() throws IOException {
        return primitiveTypes.readDouble();
    }

    /**
     * Read the next item assuming it is an exception. The exception is not a
     * regular instance in the object graph, but the exception instance that
     * happened (if any) when dumping the original object graph. The set of seen
     * objects will be reset just before and just after loading this exception
     * object.
     * <p>
     * When exceptions are found normally in the object graph, they are loaded
     * as a regular object, and not by this method. In that case, the set of
     * "known objects" is not reset.
     *
     * @return the exception read
     *
     * @throws IOException
     *             If an IO exception happened when reading the exception
     *             object.
     * @throws ClassNotFoundException
     *             If a class could not be found when reading the object graph
     *             for the exception
     * @throws OptionalDataException
     *             If optional data could not be found when reading the
     *             exception graph
     * @throws WriteAbortedException
     *             If another exception was caused when dumping this exception
     */
    private Exception readException() throws WriteAbortedException,
            OptionalDataException, ClassNotFoundException, IOException {

        resetSeenObjects();

        // Now we read the Throwable object that was saved
        // WARNING - the grammar says it is a Throwable, but the
        // WriteAbortedException constructor takes an Exception. So, we read an
        // Exception from the stream
        Exception exc = (Exception) readObject();

        // We reset the receiver's state (the grammar has "reset" in normal
        // font)
        resetSeenObjects();
        return exc;
    }

    /**
     * Reads a collection of field descriptors (name, type name, etc) for the
     * class descriptor {@code cDesc} (an {@code ObjectStreamClass})
     *
     * @param cDesc
     *            The class descriptor (an {@code ObjectStreamClass})
     *            for which to write field information
     *
     * @throws IOException
     *             If an IO exception happened when reading the field
     *             descriptors.
     * @throws ClassNotFoundException
     *             If a class for one of the field types could not be found
     *
     * @see #readObject()
     */
    private void readFieldDescriptors(ObjectStreamClass cDesc)
            throws ClassNotFoundException, IOException {
        short numFields = input.readShort();
        ObjectStreamField[] fields = new ObjectStreamField[numFields];

        // We set it now, but each element will be inserted in the array further
        // down
        cDesc.setLoadFields(fields);

        // Check ObjectOutputStream.writeFieldDescriptors
        for (short i = 0; i < numFields; i++) {
            char typecode = (char) input.readByte();
            String fieldName = input.readUTF();
            boolean isPrimType = ObjectStreamClass.isPrimitiveType(typecode);
            String classSig;
            if (isPrimType) {
                classSig = String.valueOf(typecode);
            } else {
                // The spec says it is a UTF, but experience shows they dump
                // this String using writeObject (unlike the field name, which
                // is saved with writeUTF).
                // And if resolveObject is enabled, the classSig may be modified
                // so that the original class descriptor cannot be read
                // properly, so it is disabled.
                boolean old = enableResolve;
                try {
                    enableResolve = false;
                    classSig = (String) readObject();
                } finally {
                    enableResolve = old;
                }
            }

            classSig = formatClassSig(classSig);
            ObjectStreamField f = new ObjectStreamField(classSig, fieldName);
            fields[i] = f;
        }
    }

    /*
     * Format the class signature for ObjectStreamField, for example,
     * "[L[Ljava.lang.String;;" is converted to "[Ljava.lang.String;"
     */
    private static String formatClassSig(String classSig) {
        int start = 0;
        int end = classSig.length();

        if (end <= 0) {
            return classSig;
        }

        while (classSig.startsWith("[L", start)
                && classSig.charAt(end - 1) == ';') {
            start += 2;
            end--;
        }

        if (start > 0) {
            start -= 2;
            end++;
            return classSig.substring(start, end);
        }
        return classSig;
    }

    /**
     * Reads the persistent fields of the object that is currently being read
     * from the source stream. The values read are stored in a GetField object
     * that provides access to the persistent fields. This GetField object is
     * then returned.
     *
     * @return the GetField object from which persistent fields can be accessed
     *         by name.
     * @throws ClassNotFoundException
     *             if the class of an object being deserialized can not be
     *             found.
     * @throws IOException
     *             if an error occurs while reading from this stream.
     * @throws NotActiveException
     *             if this stream is currently not reading an object.
     */
    public GetField readFields() throws IOException, ClassNotFoundException, NotActiveException {
        if (currentObject == null) {
            throw new NotActiveException();
        }
        EmulatedFieldsForLoading result = new EmulatedFieldsForLoading(currentClass);
        readFieldValues(result);
        return result;
    }

    /**
     * Reads a collection of field values for the emulated fields
     * {@code emulatedFields}
     *
     * @param emulatedFields
     *            an {@code EmulatedFieldsForLoading}, concrete subclass
     *            of {@code GetField}
     *
     * @throws IOException
     *             If an IO exception happened when reading the field values.
     * @throws InvalidClassException
     *             If an incompatible type is being assigned to an emulated
     *             field.
     * @throws OptionalDataException
     *             If optional data could not be found when reading the
     *             exception graph
     *
     * @see #readFields
     * @see #readObject()
     */
    private void readFieldValues(EmulatedFieldsForLoading emulatedFields)
            throws OptionalDataException, InvalidClassException, IOException {
        EmulatedFields.ObjectSlot[] slots = emulatedFields.emulatedFields().slots();
        for (ObjectSlot element : slots) {
            element.defaulted = false;
            Class<?> type = element.field.getType();
            if (type == int.class) {
                element.fieldValue = input.readInt();
            } else if (type == byte.class) {
                element.fieldValue = input.readByte();
            } else if (type == char.class) {
                element.fieldValue = input.readChar();
            } else if (type == short.class) {
                element.fieldValue = input.readShort();
            } else if (type == boolean.class) {
                element.fieldValue = input.readBoolean();
            } else if (type == long.class) {
                element.fieldValue = input.readLong();
            } else if (type == float.class) {
                element.fieldValue = input.readFloat();
            } else if (type == double.class) {
                element.fieldValue = input.readDouble();
            } else {
                // Either array or Object
                try {
                    element.fieldValue = readObject();
                } catch (ClassNotFoundException cnf) {
                    // WARNING- Not sure this is the right thing to do. Write
                    // test case.
                    throw new InvalidClassException(cnf.toString());
                }
            }
        }
    }

    /**
     * Reads a collection of field values for the class descriptor
     * {@code classDesc} (an {@code ObjectStreamClass}). The
     * values will be used to set instance fields in object {@code obj}.
     * This is the default mechanism, when emulated fields (an
     * {@code GetField}) are not used. Actual values to load are stored
     * directly into the object {@code obj}.
     *
     * @param obj
     *            Instance in which the fields will be set.
     * @param classDesc
     *            A class descriptor (an {@code ObjectStreamClass})
     *            defining which fields should be loaded.
     *
     * @throws IOException
     *             If an IO exception happened when reading the field values.
     * @throws InvalidClassException
     *             If an incompatible type is being assigned to an emulated
     *             field.
     * @throws OptionalDataException
     *             If optional data could not be found when reading the
     *             exception graph
     * @throws ClassNotFoundException
     *             If a class of an object being de-serialized can not be found
     *
     * @see #readFields
     * @see #readObject()
     */
    private void readFieldValues(Object obj, ObjectStreamClass classDesc) throws OptionalDataException, ClassNotFoundException, IOException {
        // Now we must read all fields and assign them to the receiver
        ObjectStreamField[] fields = classDesc.getLoadFields();
        fields = (fields == null) ? ObjectStreamClass.NO_FIELDS : fields;
        Class<?> declaringClass = classDesc.forClass();
        if (declaringClass == null && mustResolve) {
            throw new ClassNotFoundException(classDesc.getName());
        }

        for (ObjectStreamField fieldDesc : fields) {
            Field field = classDesc.getReflectionField(fieldDesc);
            if (field != null && Modifier.isTransient(field.getModifiers())) {
                field = null; // No setting transient fields! (http://b/4471249)
            }
            // We may not have been able to find the field, or it may be transient, but we still
            // need to read the value and do the other checking...
            try {
                Class<?> type = fieldDesc.getTypeInternal();
                if (type == byte.class) {
                    byte b = input.readByte();
                    if (field != null) {
                        field.setByte(obj, b);
                    }
                } else if (type == char.class) {
                    char c = input.readChar();
                    if (field != null) {
                        field.setChar(obj, c);
                    }
                } else if (type == double.class) {
                    double d = input.readDouble();
                    if (field != null) {
                        field.setDouble(obj, d);
                    }
                } else if (type == float.class) {
                    float f = input.readFloat();
                    if (field != null) {
                        field.setFloat(obj, f);
                    }
                } else if (type == int.class) {
                    int i = input.readInt();
                    if (field != null) {
                        field.setInt(obj, i);
                    }
                } else if (type == long.class) {
                    long j = input.readLong();
                    if (field != null) {
                        field.setLong(obj, j);
                    }
                } else if (type == short.class) {
                    short s = input.readShort();
                    if (field != null) {
                        field.setShort(obj, s);
                    }
                } else if (type == boolean.class) {
                    boolean z = input.readBoolean();
                    if (field != null) {
                        field.setBoolean(obj, z);
                    }
                } else {
                    Object toSet = fieldDesc.isUnshared() ? readUnshared() : readObject();
                    if (toSet != null) {
                        // Get the field type from the local field rather than
                        // from the stream's supplied data. That's the field
                        // we'll be setting, so that's the one that needs to be
                        // validated.
                        String fieldName = fieldDesc.getName();
                        ObjectStreamField localFieldDesc = classDesc.getField(fieldName);
                        Class<?> fieldType = localFieldDesc.getTypeInternal();
                        Class<?> valueType = toSet.getClass();
                        if (!fieldType.isAssignableFrom(valueType)) {
                            throw new ClassCastException(classDesc.getName() + "." + fieldName + " - " + fieldType + " not compatible with " + valueType);
                        }
                        if (field != null) {
                            field.set(obj, toSet);
                        }
                    }
                }
            } catch (IllegalAccessException iae) {
                // ObjectStreamField should have called setAccessible(true).
                throw new AssertionError(iae);
            } catch (NoSuchFieldError ignored) {
            }
        }
    }

    /**
     * Reads a float (32 bit) from the source stream.
     *
     * @return the float value read from the source stream.
     * @throws EOFException
     *             if the end of the input is reached before the read
     *             request can be satisfied.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     */
    public float readFloat() throws IOException {
        return primitiveTypes.readFloat();
    }

    /**
     * Reads bytes from the source stream into the byte array {@code dst}.
     * This method will block until {@code dst.length} bytes have been read.
     *
     * @param dst
     *            the array in which to store the bytes read.
     * @throws EOFException
     *             if the end of the input is reached before the read
     *             request can be satisfied.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     */
    public void readFully(byte[] dst) throws IOException {
        primitiveTypes.readFully(dst);
    }

    /**
     * Reads {@code byteCount} bytes from the source stream into the byte array {@code dst}.
     *
     * @param dst
     *            the byte array in which to store the bytes read.
     * @param offset
     *            the initial position in {@code dst} to store the bytes
     *            read from the source stream.
     * @param byteCount
     *            the number of bytes to read.
     * @throws EOFException
     *             if the end of the input is reached before the read
     *             request can be satisfied.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     */
    public void readFully(byte[] dst, int offset, int byteCount) throws IOException {
        primitiveTypes.readFully(dst, offset, byteCount);
    }

    /**
     * Walks the hierarchy of classes described by class descriptor
     * {@code classDesc} and reads the field values corresponding to
     * fields declared by the corresponding class descriptor. The instance to
     * store field values into is {@code object}. If the class
     * (corresponding to class descriptor {@code classDesc}) defines
     * private instance method {@code readObject} it will be used to load
     * field values.
     *
     * @param object
     *            Instance into which stored field values loaded.
     * @param classDesc
     *            A class descriptor (an {@code ObjectStreamClass})
     *            defining which fields should be loaded.
     *
     * @throws IOException
     *             If an IO exception happened when reading the field values in
     *             the hierarchy.
     * @throws ClassNotFoundException
     *             If a class for one of the field types could not be found
     * @throws NotActiveException
     *             If {@code defaultReadObject} is called from the wrong
     *             context.
     *
     * @see #defaultReadObject
     * @see #readObject()
     */
    private void readHierarchy(Object object, ObjectStreamClass classDesc)
            throws IOException, ClassNotFoundException, NotActiveException {
        if (object == null && mustResolve) {
            throw new NotActiveException();
        }

        List<ObjectStreamClass> streamClassList = classDesc.getHierarchy();
        if (object == null) {
            for (ObjectStreamClass objectStreamClass : streamClassList) {
                readObjectForClass(null, objectStreamClass);
            }
        } else {
            List<Class<?>> superclasses = cachedSuperclasses.get(object.getClass());
            if (superclasses == null) {
                superclasses = cacheSuperclassesFor(object.getClass());
            }

            int lastIndex = 0;
            for (int i = 0, end = superclasses.size(); i < end; ++i) {
                Class<?> superclass = superclasses.get(i);
                int index = findStreamSuperclass(superclass, streamClassList, lastIndex);
                if (index == -1) {
                    readObjectNoData(object, superclass,
                            ObjectStreamClass.lookupStreamClass(superclass));
                } else {
                    for (int j = lastIndex; j <= index; j++) {
                        readObjectForClass(object, streamClassList.get(j));
                    }
                    lastIndex = index + 1;
                }
            }
        }
    }

    private HashMap<Class<?>, List<Class<?>>> cachedSuperclasses = new HashMap<Class<?>, List<Class<?>>>();

    private List<Class<?>> cacheSuperclassesFor(Class<?> c) {
        ArrayList<Class<?>> result = new ArrayList<Class<?>>();
        Class<?> nextClass = c;
        while (nextClass != null) {
            Class<?> testClass = nextClass.getSuperclass();
            if (testClass != null) {
                result.add(0, nextClass);
            }
            nextClass = testClass;
        }
        cachedSuperclasses.put(c, result);
        return result;
    }

    private int findStreamSuperclass(Class<?> cl, List<ObjectStreamClass> classList, int lastIndex) {
        for (int i = lastIndex, end = classList.size(); i < end; i++) {
            ObjectStreamClass objCl = classList.get(i);
            String forName = objCl.forClass().getName();

            if (objCl.getName().equals(forName)) {
                if (cl.getName().equals(objCl.getName())) {
                    return i;
                }
            } else {
                // there was a class replacement
                if (cl.getName().equals(forName)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void readObjectNoData(Object object, Class<?> cl, ObjectStreamClass classDesc)
            throws ObjectStreamException {
        if (!classDesc.isSerializable()) {
            return;
        }
        if (classDesc.hasMethodReadObjectNoData()){
            final Method readMethod = classDesc.getMethodReadObjectNoData();
            try {
                readMethod.invoke(object);
            } catch (InvocationTargetException e) {
                Throwable ex = e.getTargetException();
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                } else if (ex instanceof Error) {
                    throw (Error) ex;
                }
                throw (ObjectStreamException) ex;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.toString());
            }
        }

    }

    private void readObjectForClass(Object object, ObjectStreamClass classDesc)
            throws IOException, ClassNotFoundException, NotActiveException {
        // Have to do this before calling defaultReadObject or anything that
        // calls defaultReadObject
        currentObject = object;
        currentClass = classDesc;

        boolean hadWriteMethod = (classDesc.getFlags() & SC_WRITE_METHOD) != 0;
        Class<?> targetClass = classDesc.forClass();

        final Method readMethod;
        if (targetClass == null || !mustResolve) {
            readMethod = null;
        } else {
            readMethod = classDesc.getMethodReadObject();
        }
        try {
            if (readMethod != null) {
                // We have to be able to fetch its value, even if it is private
                readMethod.setAccessible(true);
                try {
                    readMethod.invoke(object, this);
                } catch (InvocationTargetException e) {
                    Throwable ex = e.getTargetException();
                    if (ex instanceof ClassNotFoundException) {
                        throw (ClassNotFoundException) ex;
                    } else if (ex instanceof RuntimeException) {
                        throw (RuntimeException) ex;
                    } else if (ex instanceof Error) {
                        throw (Error) ex;
                    }
                    throw (IOException) ex;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.toString());
                }
            } else {
                defaultReadObject();
            }
            if (hadWriteMethod) {
                discardData();
            }
        } finally {
            // Cleanup, needs to run always so that we can later detect invalid
            // calls to defaultReadObject
            currentObject = null; // We did not set this, so we do not need to
            // clean it
            currentClass = null;
        }
    }

    /**
     * Reads an integer (32 bit) from the source stream.
     *
     * @return the integer value read from the source stream.
     * @throws EOFException
     *             if the end of the input is reached before the read
     *             request can be satisfied.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     */
    public int readInt() throws IOException {
        return primitiveTypes.readInt();
    }

    /**
     * Reads the next line from the source stream. Lines are terminated by
     * {@code '\r'}, {@code '\n'}, {@code "\r\n"} or an {@code EOF}.
     *
     * @return the string read from the source stream.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     * @deprecated Use {@link BufferedReader} instead.
     */
    @Deprecated
    public String readLine() throws IOException {
        return primitiveTypes.readLine();
    }

    /**
     * Reads a long (64 bit) from the source stream.
     *
     * @return the long value read from the source stream.
     * @throws EOFException
     *             if the end of the input is reached before the read
     *             request can be satisfied.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     */
    public long readLong() throws IOException {
        return primitiveTypes.readLong();
    }

    /**
     * Read a new array from the receiver. It is assumed the array has not been
     * read yet (not a cyclic reference). Return the array read.
     *
     * @param unshared
     *            read the object unshared
     * @return the array read
     *
     * @throws IOException
     *             If an IO exception happened when reading the array.
     * @throws ClassNotFoundException
     *             If a class for one of the objects could not be found
     * @throws OptionalDataException
     *             If optional data could not be found when reading the array.
     */
    private Object readNewArray(boolean unshared) throws OptionalDataException,
            ClassNotFoundException, IOException {
        ObjectStreamClass classDesc = readClassDesc();

        if (classDesc == null) {
            throw missingClassDescriptor();
        }

        int newHandle = nextHandle();

        // Array size
        int size = input.readInt();
        Class<?> arrayClass = classDesc.forClass();
        Class<?> componentType = arrayClass.getComponentType();
        Object result = Array.newInstance(componentType, size);

        registerObjectRead(result, newHandle, unshared);

        // Now we have code duplication just because Java is typed. We have to
        // read N elements and assign to array positions, but we must typecast
        // the array first, and also call different methods depending on the
        // elements.
        if (componentType.isPrimitive()) {
            if (componentType == int.class) {
                int[] intArray = (int[]) result;
                for (int i = 0; i < size; i++) {
                    intArray[i] = input.readInt();
                }
            } else if (componentType == byte.class) {
                byte[] byteArray = (byte[]) result;
                input.readFully(byteArray, 0, size);
            } else if (componentType == char.class) {
                char[] charArray = (char[]) result;
                for (int i = 0; i < size; i++) {
                    charArray[i] = input.readChar();
                }
            } else if (componentType == short.class) {
                short[] shortArray = (short[]) result;
                for (int i = 0; i < size; i++) {
                    shortArray[i] = input.readShort();
                }
            } else if (componentType == boolean.class) {
                boolean[] booleanArray = (boolean[]) result;
                for (int i = 0; i < size; i++) {
                    booleanArray[i] = input.readBoolean();
                }
            } else if (componentType == long.class) {
                long[] longArray = (long[]) result;
                for (int i = 0; i < size; i++) {
                    longArray[i] = input.readLong();
                }
            } else if (componentType == float.class) {
                float[] floatArray = (float[]) result;
                for (int i = 0; i < size; i++) {
                    floatArray[i] = input.readFloat();
                }
            } else if (componentType == double.class) {
                double[] doubleArray = (double[]) result;
                for (int i = 0; i < size; i++) {
                    doubleArray[i] = input.readDouble();
                }
            } else {
                throw new ClassNotFoundException("Wrong base type in " + classDesc.getName());
            }
        } else {
            // Array of Objects
            Object[] objectArray = (Object[]) result;
            for (int i = 0; i < size; i++) {
                // TODO: This place is the opportunity for enhancement
                //      We can implement writing elements through fast-path,
                //      without setting up the context (see readObject()) for
                //      each element with public API
                objectArray[i] = readObject();
            }
        }
        if (enableResolve) {
            result = resolveObject(result);
            registerObjectRead(result, newHandle, false);
        }
        return result;
    }

    /**
     * Reads a new class from the receiver. It is assumed the class has not been
     * read yet (not a cyclic reference). Return the class read.
     *
     * @param unshared
     *            read the object unshared
     * @return The {@code java.lang.Class} read from the stream.
     *
     * @throws IOException
     *             If an IO exception happened when reading the class.
     * @throws ClassNotFoundException
     *             If a class for one of the objects could not be found
     */
    private Class<?> readNewClass(boolean unshared) throws ClassNotFoundException, IOException {
        ObjectStreamClass classDesc = readClassDesc();
        if (classDesc == null) {
            throw missingClassDescriptor();
        }
        Class<?> localClass = classDesc.forClass();
        if (localClass != null) {
            registerObjectRead(localClass, nextHandle(), unshared);
        }
        return localClass;
    }

    /*
     * read class type for Enum, note there's difference between enum and normal
     * classes
     */
    private ObjectStreamClass readEnumDesc() throws IOException,
            ClassNotFoundException {
        byte tc = nextTC();
        switch (tc) {
            case TC_CLASSDESC:
                return readEnumDescInternal();
            case TC_REFERENCE:
                return (ObjectStreamClass) readCyclicReference();
            case TC_NULL:
                return null;
            default:
                throw corruptStream(tc);
        }
    }

    private ObjectStreamClass readEnumDescInternal() throws IOException, ClassNotFoundException {
        ObjectStreamClass classDesc;
        primitiveData = input;
        int oldHandle = descriptorHandle;
        descriptorHandle = nextHandle();
        classDesc = readClassDescriptor();
        registerObjectRead(classDesc, descriptorHandle, false);
        descriptorHandle = oldHandle;
        primitiveData = emptyStream;
        classDesc.setClass(resolveClass(classDesc));
        // Consume unread class annotation data and TC_ENDBLOCKDATA
        discardData();
        ObjectStreamClass superClass = readClassDesc();
        checkedSetSuperClassDesc(classDesc, superClass);
        // Check SUIDs, note all SUID for Enum is 0L
        if (0L != classDesc.getSerialVersionUID() || 0L != superClass.getSerialVersionUID()) {
            throw new InvalidClassException(superClass.getName(),
                    "Incompatible class (SUID): " + superClass + " but expected " + superClass);
        }
        byte tc = nextTC();
        // discard TC_ENDBLOCKDATA after classDesc if any
        if (tc == TC_ENDBLOCKDATA) {
            // read next parent class. For enum, it may be null
            superClass.setSuperclass(readClassDesc());
        } else {
            // not TC_ENDBLOCKDATA, push back for next read
            pushbackTC();
        }
        return classDesc;
    }

    @SuppressWarnings("unchecked")// For the Enum.valueOf call
    private Object readEnum(boolean unshared) throws OptionalDataException,
            ClassNotFoundException, IOException {
        // read classdesc for Enum first
        ObjectStreamClass classDesc = readEnumDesc();
        int newHandle = nextHandle();
        // read name after class desc
        String name;
        byte tc = nextTC();
        switch (tc) {
            case TC_REFERENCE:
                if (unshared) {
                    readNewHandle();
                    throw new InvalidObjectException("Unshared read of back reference");
                }
                name = (String) readCyclicReference();
                break;
            case TC_STRING:
                name = (String) readNewString(unshared);
                break;
            default:
                throw corruptStream(tc);
        }

        Enum<?> result;
        try {
            result = Enum.valueOf((Class) classDesc.forClass(), name);
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        registerObjectRead(result, newHandle, unshared);
        return result;
    }

    /**
     * Reads a new class descriptor from the receiver. It is assumed the class
     * descriptor has not been read yet (not a cyclic reference). Return the
     * class descriptor read.
     *
     * @param unshared
     *            read the object unshared
     * @return The {@code ObjectStreamClass} read from the stream.
     *
     * @throws IOException
     *             If an IO exception happened when reading the class
     *             descriptor.
     * @throws ClassNotFoundException
     *             If a class for one of the objects could not be found
     */
    private ObjectStreamClass readNewClassDesc(boolean unshared)
            throws ClassNotFoundException, IOException {
        // So read...() methods can be used by
        // subclasses during readClassDescriptor()
        primitiveData = input;
        int oldHandle = descriptorHandle;
        descriptorHandle = nextHandle();
        ObjectStreamClass newClassDesc = readClassDescriptor();
        registerObjectRead(newClassDesc, descriptorHandle, unshared);
        descriptorHandle = oldHandle;
        primitiveData = emptyStream;

        // We need to map classDesc to class.
        try {
            newClassDesc.setClass(resolveClass(newClassDesc));
            // Check SUIDs & base name of the class
            verifyAndInit(newClassDesc);
        } catch (ClassNotFoundException e) {
            if (mustResolve) {
                throw e;
                // Just continue, the class may not be required
            }
        }

        // Resolve the field signatures using the class loader of the
        // resolved class
        ObjectStreamField[] fields = newClassDesc.getLoadFields();
        fields = (fields == null) ? ObjectStreamClass.NO_FIELDS : fields;
        ClassLoader loader = newClassDesc.forClass() == null ? callerClassLoader
                : newClassDesc.forClass().getClassLoader();
        for (ObjectStreamField element : fields) {
            element.resolve(loader);
        }

        // Consume unread class annotation data and TC_ENDBLOCKDATA
        discardData();
        checkedSetSuperClassDesc(newClassDesc, readClassDesc());
        return newClassDesc;
    }

    /**
     * Reads a new proxy class descriptor from the receiver. It is assumed the
     * proxy class descriptor has not been read yet (not a cyclic reference).
     * Return the proxy class descriptor read.
     *
     * @return The {@code Class} read from the stream.
     *
     * @throws IOException
     *             If an IO exception happened when reading the class
     *             descriptor.
     * @throws ClassNotFoundException
     *             If a class for one of the objects could not be found
     */
    private Class<?> readNewProxyClassDesc() throws ClassNotFoundException,
            IOException {
        int count = input.readInt();
        String[] interfaceNames = new String[count];
        for (int i = 0; i < count; i++) {
            interfaceNames[i] = input.readUTF();
        }
        Class<?> proxy = resolveProxyClass(interfaceNames);
        // Consume unread class annotation data and TC_ENDBLOCKDATA
        discardData();
        return proxy;
    }

    /**
     * Reads a class descriptor from the source stream.
     *
     * @return the class descriptor read from the source stream.
     * @throws ClassNotFoundException
     *             if a class for one of the objects cannot be found.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     */
    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass newClassDesc = new ObjectStreamClass();
        String name = input.readUTF();
        if (name.length() == 0) {
            throw new IOException("The stream is corrupted");
        }
        newClassDesc.setName(name);
        newClassDesc.setSerialVersionUID(input.readLong());
        newClassDesc.setFlags(input.readByte());

        /*
         * We must register the class descriptor before reading field
         * descriptors. If called outside of readObject, the descriptorHandle
         * might be unset.
         */
        if (descriptorHandle == -1) {
            descriptorHandle = nextHandle();
        }
        registerObjectRead(newClassDesc, descriptorHandle, false);

        readFieldDescriptors(newClassDesc);
        return newClassDesc;
    }

    /**
     * Creates the proxy class that implements the interfaces specified in
     * {@code interfaceNames}.
     *
     * @param interfaceNames
     *            the interfaces used to create the proxy class.
     * @return the proxy class.
     * @throws ClassNotFoundException
     *             if the proxy class or any of the specified interfaces cannot
     *             be created.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     * @see ObjectOutputStream#annotateProxyClass(Class)
     */
    protected Class<?> resolveProxyClass(String[] interfaceNames)
            throws IOException, ClassNotFoundException {
        ClassLoader loader = callerClassLoader;
        Class<?>[] interfaces = new Class<?>[interfaceNames.length];
        for (int i = 0; i < interfaceNames.length; i++) {
            interfaces[i] = Class.forName(interfaceNames[i], false, loader);
        }
        try {
            return Proxy.getProxyClass(loader, interfaces);
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(e.toString(), e);
        }
    }

    private int readNewHandle() throws IOException {
        return input.readInt();
    }

    /**
     * Read a new object from the stream. It is assumed the object has not been
     * loaded yet (not a cyclic reference). Return the object read.
     *
     * If the object implements <code>Externalizable</code> its
     * <code>readExternal</code> is called. Otherwise, all fields described by
     * the class hierarchy are loaded. Each class can define how its declared
     * instance fields are loaded by defining a private method
     * <code>readObject</code>
     *
     * @param unshared
     *            read the object unshared
     * @return the object read
     *
     * @throws IOException
     *             If an IO exception happened when reading the object.
     * @throws OptionalDataException
     *             If optional data could not be found when reading the object
     *             graph
     * @throws ClassNotFoundException
     *             If a class for one of the objects could not be found
     */
    private Object readNewObject(boolean unshared)
            throws OptionalDataException, ClassNotFoundException, IOException {
        ObjectStreamClass classDesc = readClassDesc();

        if (classDesc == null) {
            throw missingClassDescriptor();
        }

        int newHandle = nextHandle();
        Class<?> objectClass = classDesc.forClass();
        Object result = null;
        Object registeredResult = null;
        if (objectClass != null) {
            // Now we know which class to instantiate and which constructor to
            // run. We are allowed to run the constructor.
            result = classDesc.newInstance(objectClass);
            registerObjectRead(result, newHandle, unshared);
            registeredResult = result;
        } else {
            result = null;
        }

        try {
            // This is how we know what to do in defaultReadObject. And it is
            // also used by defaultReadObject to check if it was called from an
            // invalid place. It also allows readExternal to call
            // defaultReadObject and have it work.
            currentObject = result;
            currentClass = classDesc;

            // If Externalizable, just let the object read itself
            // Note that this value comes from the Stream, and in fact it could be
            // that the classes have been changed so that the info below now
            // conflicts with the newer class
            boolean wasExternalizable = (classDesc.getFlags() & SC_EXTERNALIZABLE) != 0;
            if (wasExternalizable) {
                boolean blockData = (classDesc.getFlags() & SC_BLOCK_DATA) != 0;
                if (!blockData) {
                    primitiveData = input;
                }
                if (mustResolve) {
                    Externalizable extern = (Externalizable) result;
                    extern.readExternal(this);
                }
                if (blockData) {
                    // Similar to readHierarchy. Anything not read by
                    // readExternal has to be consumed here
                    discardData();
                } else {
                    primitiveData = emptyStream;
                }
            } else {
                // If we got here, it is Serializable but not Externalizable.
                // Walk the hierarchy reading each class' slots
                readHierarchy(result, classDesc);
            }
        } finally {
            // Cleanup, needs to run always so that we can later detect invalid
            // calls to defaultReadObject
            currentObject = null;
            currentClass = null;
        }

        if (objectClass != null) {

            if (classDesc.hasMethodReadResolve()){
                Method methodReadResolve = classDesc.getMethodReadResolve();
                try {
                    result = methodReadResolve.invoke(result, (Object[]) null);
                } catch (IllegalAccessException ignored) {
                } catch (InvocationTargetException ite) {
                    Throwable target = ite.getTargetException();
                    if (target instanceof ObjectStreamException) {
                        throw (ObjectStreamException) target;
                    } else if (target instanceof Error) {
                        throw (Error) target;
                    } else {
                        throw (RuntimeException) target;
                    }
                }

            }
        }
        // We get here either if class-based replacement was not needed or if it
        // was needed but produced the same object or if it could not be
        // computed.

        // The object to return is the one we instantiated or a replacement for
        // it
        if (result != null && enableResolve) {
            result = resolveObject(result);
        }
        if (registeredResult != result) {
            registerObjectRead(result, newHandle, unshared);
        }
        return result;
    }

    private InvalidClassException missingClassDescriptor() throws InvalidClassException {
        throw new InvalidClassException("Read null attempting to read class descriptor for object");
    }

    /**
     * Read a string encoded in {@link DataInput modified UTF-8} from the
     * receiver. Return the string read.
     *
     * @param unshared
     *            read the object unshared
     * @return the string just read.
     * @throws IOException
     *             If an IO exception happened when reading the String.
     */
    private Object readNewString(boolean unshared) throws IOException {
        Object result = input.readUTF();
        if (enableResolve) {
            result = resolveObject(result);
        }
        registerObjectRead(result, nextHandle(), unshared);

        return result;
    }

    /**
     * Read a new String in UTF format from the receiver. Return the string
     * read.
     *
     * @param unshared
     *            read the object unshared
     * @return the string just read.
     *
     * @throws IOException
     *             If an IO exception happened when reading the String.
     */
    private Object readNewLongString(boolean unshared) throws IOException {
        long length = input.readLong();
        Object result = input.decodeUTF((int) length);
        if (enableResolve) {
            result = resolveObject(result);
        }
        registerObjectRead(result, nextHandle(), unshared);

        return result;
    }

    /**
     * Reads the next object from the source stream.
     *
     * @return the object read from the source stream.
     * @throws ClassNotFoundException
     *             if the class of one of the objects in the object graph cannot
     *             be found.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     * @throws OptionalDataException
     *             if primitive data types were found instead of an object.
     * @see ObjectOutputStream#writeObject(Object)
     */
    public final Object readObject() throws OptionalDataException,
            ClassNotFoundException, IOException {
        return readObject(false);
    }

    /**
     * Reads the next unshared object from the source stream.
     *
     * @return the new object read.
     * @throws ClassNotFoundException
     *             if the class of one of the objects in the object graph cannot
     *             be found.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     * @see ObjectOutputStream#writeUnshared
     */
    public Object readUnshared() throws IOException, ClassNotFoundException {
        return readObject(true);
    }

    private Object readObject(boolean unshared) throws OptionalDataException,
            ClassNotFoundException, IOException {
        boolean restoreInput = (primitiveData == input);
        if (restoreInput) {
            primitiveData = emptyStream;
        }

        // This is the spec'ed behavior in JDK 1.2. Very bizarre way to allow
        // behavior overriding.
        if (subclassOverridingImplementation && !unshared) {
            return readObjectOverride();
        }

        // If we still had primitive types to read, should we discard them
        // (reset the primitiveTypes stream) or leave as is, so that attempts to
        // read primitive types won't read 'past data' ???
        Object result;
        try {
            // We need this so we can tell when we are returning to the
            // original/outside caller
            if (++nestedLevels == 1) {
                // Remember the caller's class loader
                callerClassLoader = getClosestUserClassLoader();
            }

            result = readNonPrimitiveContent(unshared);
            if (restoreInput) {
                primitiveData = input;
            }
        } finally {
            // We need this so we can tell when we are returning to the
            // original/outside caller
            if (--nestedLevels == 0) {
                // We are going to return to the original caller, perform
                // cleanups.
                // No more need to remember the caller's class loader
                callerClassLoader = null;
            }
        }

        // Done reading this object. Is it time to return to the original
        // caller? If so we need to perform validations first.
        if (nestedLevels == 0 && validations != null) {
            // We are going to return to the original caller. If validation is
            // enabled we need to run them now and then cleanup the validation
            // collection
            try {
                for (InputValidationDesc element : validations) {
                    element.validator.validateObject();
                }
            } finally {
                // Validations have to be renewed, since they are only called
                // from readObject
                validations = null;
            }
        }
        return result;
    }

    private static final ClassLoader bootstrapLoader = Object.class.getClassLoader();
    private static final ClassLoader systemLoader = ClassLoader.getSystemClassLoader();

    /**
     * Searches up the call stack to find the closest user-defined class loader.
     *
     * @return a user-defined class loader or null if one isn't found
     */
    private static ClassLoader getClosestUserClassLoader() {
        return bootstrapLoader;
    }

    /**
     * Method to be overridden by subclasses to read the next object from the
     * source stream.
     *
     * @return the object read from the source stream.
     * @throws ClassNotFoundException
     *             if the class of one of the objects in the object graph cannot
     *             be found.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     * @throws OptionalDataException
     *             if primitive data types were found instead of an object.
     * @see ObjectOutputStream#writeObjectOverride
     */
    protected Object readObjectOverride() throws OptionalDataException,
            ClassNotFoundException, IOException {
        if (input == null) {
            return null;
        }
        // Subclasses must override.
        throw new IOException();
    }

    /**
     * Reads a short (16 bit) from the source stream.
     *
     * @return the short value read from the source stream.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     */
    public short readShort() throws IOException {
        return primitiveTypes.readShort();
    }

    /**
     * Reads and validates the ObjectInputStream header from the source stream.
     *
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     * @throws StreamCorruptedException
     *             if the source stream does not contain readable serialized
     *             objects.
     */
    protected void readStreamHeader() throws IOException,
            StreamCorruptedException {
        if (input.readShort() == STREAM_MAGIC
                && input.readShort() == STREAM_VERSION) {
            return;
        }
        throw new StreamCorruptedException();
    }

    /**
     * Reads an unsigned byte (8 bit) from the source stream.
     *
     * @return the unsigned byte value read from the source stream packaged in
     *         an integer.
     * @throws EOFException
     *             if the end of the input is reached before the read
     *             request can be satisfied.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     */
    public int readUnsignedByte() throws IOException {
        return primitiveTypes.readUnsignedByte();
    }

    /**
     * Reads an unsigned short (16 bit) from the source stream.
     *
     * @return the unsigned short value read from the source stream packaged in
     *         an integer.
     * @throws EOFException
     *             if the end of the input is reached before the read
     *             request can be satisfied.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     */
    public int readUnsignedShort() throws IOException {
        return primitiveTypes.readUnsignedShort();
    }

    /**
     * Reads a string encoded in {@link DataInput modified UTF-8} from the
     * source stream.
     *
     * @return the string encoded in {@link DataInput modified UTF-8} read from
     *         the source stream.
     * @throws EOFException
     *             if the end of the input is reached before the read
     *             request can be satisfied.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     */
    public String readUTF() throws IOException {
        return primitiveTypes.readUTF();
    }

    /**
     * Returns the previously-read object corresponding to the given serialization handle.
     * @throws InvalidObjectException
     *             If there is no previously-read object with this handle
     */
    private Object registeredObjectRead(int handle) throws InvalidObjectException {
        Object res = objectsRead.get(handle - ObjectStreamConstants.baseWireHandle);
        if (res == UNSHARED_OBJ) {
            throw new InvalidObjectException("Cannot read back reference to unshared object");
        }
        return res;
    }

    /**
     * Associates a read object with the its serialization handle.
     */
    private void registerObjectRead(Object obj, int handle, boolean unshared) throws IOException {
        if (unshared) {
            obj = UNSHARED_OBJ;
        }
        int index = handle - ObjectStreamConstants.baseWireHandle;
        int size = objectsRead.size();
        // ObjectOutputStream sometimes wastes a handle. I've compared hex dumps of the RI
        // and it seems like that's a 'feature'. Look for calls to objectsWritten.put that
        // are guarded by !unshared tests.
        while (index > size) {
            objectsRead.add(null);
            ++size;
        }
        if (index == size) {
            objectsRead.add(obj);
        } else {
            objectsRead.set(index, obj);
        }
    }

    /**
     * Registers a callback for post-deserialization validation of objects. It
     * allows to perform additional consistency checks before the {@code
     * readObject()} method of this class returns its result to the caller. This
     * method can only be called from within the {@code readObject()} method of
     * a class that implements "special" deserialization rules. It can be called
     * multiple times. Validation callbacks are then done in order of decreasing
     * priority, defined by {@code priority}.
     *
     * @param object
     *            an object that can validate itself by receiving a callback.
     * @param priority
     *            the validator's priority.
     * @throws InvalidObjectException
     *             if {@code object} is {@code null}.
     * @throws NotActiveException
     *             if this stream is currently not reading objects. In that
     *             case, calling this method is not allowed.
     * @see ObjectInputValidation#validateObject()
     */
    public synchronized void registerValidation(ObjectInputValidation object,
            int priority) throws NotActiveException, InvalidObjectException {
        // Validation can only be registered when inside readObject calls
        Object instanceBeingRead = this.currentObject;

        if (instanceBeingRead == null && nestedLevels == 0) {
            throw new NotActiveException();
        }
        if (object == null) {
            throw new InvalidObjectException("Callback object cannot be null");
        }
        // From now on it is just insertion in a SortedCollection. Since
        // the Java class libraries don't provide that, we have to
        // implement it from scratch here.
        InputValidationDesc desc = new InputValidationDesc();
        desc.validator = object;
        desc.priority = priority;
        // No need for this, validateObject does not take a parameter
        // desc.toValidate = instanceBeingRead;
        if (validations == null) {
            validations = new InputValidationDesc[1];
            validations[0] = desc;
        } else {
            int i = 0;
            for (; i < validations.length; i++) {
                InputValidationDesc validation = validations[i];
                // Sorted, higher priority first.
                if (priority >= validation.priority) {
                    break; // Found the index where to insert
                }
            }
            InputValidationDesc[] oldValidations = validations;
            int currentSize = oldValidations.length;
            validations = new InputValidationDesc[currentSize + 1];
            System.arraycopy(oldValidations, 0, validations, 0, i);
            System.arraycopy(oldValidations, i, validations, i + 1, currentSize
                    - i);
            validations[i] = desc;
        }
    }

    /**
     * Reset the collection of objects already loaded by the receiver.
     */
    private void resetSeenObjects() {
        objectsRead = new ArrayList<Object>();
        nextHandle = baseWireHandle;
        primitiveData = emptyStream;
    }

    /**
     * Reset the receiver. The collection of objects already read by the
     * receiver is reset, and internal structures are also reset so that the
     * receiver knows it is in a fresh clean state.
     */
    private void resetState() {
        resetSeenObjects();
        hasPushbackTC = false;
        pushbackTC = 0;
        // nestedLevels = 0;
    }

    /**
     * Loads the Java class corresponding to the class descriptor {@code
     * osClass} that has just been read from the source stream.
     *
     * @param osClass
     *            an ObjectStreamClass read from the source stream.
     * @return a Class corresponding to the descriptor {@code osClass}.
     * @throws ClassNotFoundException
     *             if the class for an object cannot be found.
     * @throws IOException
     *             if an I/O error occurs while creating the class.
     * @see ObjectOutputStream#annotateClass(Class)
     */
    protected Class<?> resolveClass(ObjectStreamClass osClass)
            throws IOException, ClassNotFoundException {
        // fastpath: obtain cached value
        Class<?> cls = osClass.forClass();
        if (cls == null) {
            // slowpath: resolve the class
            String className = osClass.getName();

            // if it is primitive class, for example, long.class
            cls = PRIMITIVE_CLASSES.get(className);

            if (cls == null) {
                // not primitive class
                // Use the first non-null ClassLoader on the stack. If null, use
                // the system class loader
                cls = Class.forName(className, true, callerClassLoader);
            }
        }
        return cls;
    }

    /**
     * Allows trusted subclasses to substitute the specified original {@code
     * object} with a new object. Object substitution has to be activated first
     * with calling {@code enableResolveObject(true)}. This implementation just
     * returns {@code object}.
     *
     * @param object
     *            the original object for which a replacement may be defined.
     * @return the replacement object for {@code object}.
     * @throws IOException
     *             if any I/O error occurs while creating the replacement
     *             object.
     * @see #enableResolveObject
     * @see ObjectOutputStream#enableReplaceObject
     * @see ObjectOutputStream#replaceObject
     */
    protected Object resolveObject(Object object) throws IOException {
        // By default no object replacement. Subclasses can override
        return object;
    }

    /**
     * Skips {@code length} bytes on the source stream. This method should not
     * be used to skip bytes at any arbitrary position, just when reading
     * primitive data types (int, char etc).
     *
     * @param length
     *            the number of bytes to skip.
     * @return the number of bytes actually skipped.
     * @throws IOException
     *             if an error occurs while skipping bytes on the source stream.
     * @throws NullPointerException
     *             if the source stream is {@code null}.
     */
    public int skipBytes(int length) throws IOException {
        // To be used with available. Ok to call if reading primitive buffer
        if (input == null) {
            throw new NullPointerException("source stream is null");
        }

        int offset = 0;
        while (offset < length) {
            checkReadPrimitiveTypes();
            long skipped = primitiveData.skip(length - offset);
            if (skipped == 0) {
                return offset;
            }
            offset += (int) skipped;
        }
        return length;
    }

    /**
     * Verify if the SUID & the base name for descriptor
     * <code>loadedStreamClass</code>matches
     * the SUID & the base name of the corresponding loaded class and
     * init private fields.
     *
     * @param loadedStreamClass
     *            An ObjectStreamClass that was loaded from the stream.
     *
     * @throws InvalidClassException
     *             If the SUID of the stream class does not match the VM class
     */
    private void verifyAndInit(ObjectStreamClass loadedStreamClass)
            throws InvalidClassException {

        Class<?> localClass = loadedStreamClass.forClass();
        ObjectStreamClass localStreamClass = ObjectStreamClass
                .lookupStreamClass(localClass);

        if (loadedStreamClass.getSerialVersionUID() != localStreamClass
                .getSerialVersionUID()) {
          // java.io.Serializable javadoc states "..., the requirement for
          // matching serialVersionUID values is waived for array classes."
          boolean isArray = loadedStreamClass.getName().startsWith("[");
          if (!isArray) {
            throw new InvalidClassException(loadedStreamClass.getName(),
                    "Incompatible class (SUID): " + loadedStreamClass +
                            " but expected " + localStreamClass);
          }
        }

        String loadedClassBaseName = getBaseName(loadedStreamClass.getName());
        String localClassBaseName = getBaseName(localStreamClass.getName());

        if (!loadedClassBaseName.equals(localClassBaseName)) {
            throw new InvalidClassException(loadedStreamClass.getName(),
                    String.format("Incompatible class (base name): %s but expected %s",
                            loadedClassBaseName, localClassBaseName));
        }

        loadedStreamClass.initPrivateFields(localStreamClass);
    }

    private static String getBaseName(String fullName) {
        int k = fullName.lastIndexOf('.');

        if (k == -1 || k == (fullName.length() - 1)) {
            return fullName;
        }
        return fullName.substring(k + 1);
    }

    // Avoid recursive defining.
    private static void checkedSetSuperClassDesc(ObjectStreamClass desc,
            ObjectStreamClass superDesc) throws StreamCorruptedException {
        if (desc.equals(superDesc)) {
            throw new StreamCorruptedException();
        }
        desc.setSuperclass(superDesc);
    }
}
