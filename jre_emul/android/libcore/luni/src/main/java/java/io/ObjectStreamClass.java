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

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import libcore.io.Memory;
import libcore.util.EmptyArray;

/**
 * Represents a descriptor for identifying a class during serialization and
 * deserialization. Information contained in the descriptor includes the name
 * and SUID of the class as well as field names and types. Information inherited
 * from the superclasses is also taken into account.
 *
 * @see ObjectOutputStream
 * @see ObjectInputStream
 * @see java.lang.Class
 */
public class ObjectStreamClass implements Serializable {

    // No need to compute the SUID for ObjectStreamClass, just use the value
    // below
    private static final long serialVersionUID = -6120832682080437368L;

    // Name of the field that contains the SUID value (if present)
    private static final String UID_FIELD_NAME = "serialVersionUID";

    static final long CONSTRUCTOR_IS_NOT_RESOLVED = -1;

    private static final int CLASS_MODIFIERS_MASK = Modifier.PUBLIC | Modifier.FINAL |
            Modifier.INTERFACE | Modifier.ABSTRACT;

    private static final int FIELD_MODIFIERS_MASK = Modifier.PUBLIC | Modifier.PRIVATE |
            Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL | Modifier.VOLATILE |
            Modifier.TRANSIENT;

    private static final int METHOD_MODIFIERS_MASK = Modifier.PUBLIC | Modifier.PRIVATE |
            Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL | Modifier.SYNCHRONIZED |
            Modifier.NATIVE | Modifier.ABSTRACT | Modifier.STRICT;

    private static final Class<?>[] READ_PARAM_TYPES = new Class[] { ObjectInputStream.class };
    private static final Class<?>[] WRITE_PARAM_TYPES = new Class[] { ObjectOutputStream.class };

    /**
     * Constant indicating that the class has no Serializable fields.
     */
    public static final ObjectStreamField[] NO_FIELDS = new ObjectStreamField[0];

    /*
     * used to fetch field serialPersistentFields and checking its type
     */
    static final Class<?> ARRAY_OF_FIELDS;

    static {
        try {
            ARRAY_OF_FIELDS = Class.forName("[Ljava.io.ObjectStreamField;");
        } catch (ClassNotFoundException e) {
            // This should not happen
            throw new AssertionError(e);
        }
    }

    private static final String CLINIT_NAME = "<clinit>";

    private static final int CLINIT_MODIFIERS = Modifier.STATIC;

    private static final String CLINIT_SIGNATURE = "()V";

    // Used to determine if an object is Serializable or Externalizable
    private static final Class<Serializable> SERIALIZABLE = Serializable.class;

    private static final Class<Externalizable> EXTERNALIZABLE = Externalizable.class;

    // Used to test if the object is a String or a class.
    static final Class<String> STRINGCLASS = String.class;

    static final Class<?> CLASSCLASS = Class.class;

    static final Class<ObjectStreamClass> OBJECTSTREAMCLASSCLASS = ObjectStreamClass.class;

    private transient Method methodWriteReplace;

    private transient Method methodReadResolve;

    private transient Method methodWriteObject;

    private transient Method methodReadObject;

    private transient Method methodReadObjectNoData;

    /**
     * Indicates whether the class properties resolved
     *
     * @see #resolveProperties()
     */
    private transient boolean arePropertiesResolved;

    /**
     * Cached class properties
     *
     * @see #resolveProperties()
     * @see #isSerializable()
     * @see #isExternalizable()
     * @see #isProxy()
     * @see #isEnum()
     */
    private transient boolean isSerializable;
    private transient boolean isExternalizable;
    private transient boolean isProxy;
    private transient boolean isEnum;

    // ClassDesc //

    // Name of the class this descriptor represents
    private transient String className;

    // Corresponding loaded class with the name above
    private transient Class<?> resolvedClass;

    private transient Class<?> resolvedConstructorClass;
    private transient long resolvedConstructorMethodId;

    // Serial version UID of the class the descriptor represents
    private transient long svUID;

    // ClassDescInfo //

    // Any combination of SC_WRITE_METHOD, SC_SERIALIZABLE and SC_EXTERNALIZABLE
    // (see ObjectStreamConstants)
    private transient byte flags;

    // Descriptor for the superclass of the class associated with this
    // descriptor
    private transient ObjectStreamClass superclass;

    // Array of ObjectStreamField (see below) describing the fields of this
    // class
    private transient ObjectStreamField[] fields;

    // Array of ObjectStreamField describing the serialized fields of this class
    private transient ObjectStreamField[] loadFields;

    // ObjectStreamField doesn't override hashCode or equals, so this is equivalent to an
    // IdentityHashMap, which is fine for our purposes.
    private transient HashMap<ObjectStreamField, Field> reflectionFields =
            new HashMap<ObjectStreamField, Field>();

    // MethodID for deserialization constructor
    private transient long constructor = CONSTRUCTOR_IS_NOT_RESOLVED;

    void setConstructor(long newConstructor) {
        constructor = newConstructor;
    }

    long getConstructor() {
        return constructor;
    }

    Field getReflectionField(ObjectStreamField osf) {
        synchronized (reflectionFields) {
            Field field = reflectionFields.get(osf);
            if (field != null) {
                return field;
            }
        }

        try {
            Class<?> declaringClass = forClass();
            Field field = declaringClass.getDeclaredField(osf.getName());
            field.setAccessible(true);
            synchronized (reflectionFields) {
                reflectionFields.put(osf, field);
            }
            return reflectionFields.get(osf);
        } catch (NoSuchFieldException ex) {
            // The caller messed up. We'll return null and won't try to resolve this again.
            return null;
        }
    }

    /*
     * If an ObjectStreamClass describes an Externalizable class, it (the
     * descriptor) should not have field descriptors (ObjectStreamField) at all.
     * The ObjectStreamClass that gets saved should simply have no field info.
     * This is a footnote in page 1511 (class Serializable) of "The Java Class
     * Libraries, Second Edition, Vol. I".
     */

    /**
     * Constructs a new instance of this class.
     */
    ObjectStreamClass() {
    }

    /**
     * Compute class descriptor for a given class <code>cl</code>.
     *
     * @param cl
     *            a java.langClass for which to compute the corresponding
     *            descriptor
     * @return the computer class descriptor
     */
    private static ObjectStreamClass createClassDesc(Class<?> cl) {

        ObjectStreamClass result = new ObjectStreamClass();

        boolean isArray = cl.isArray();
        boolean serializable = isSerializable(cl);
        boolean externalizable = isExternalizable(cl);

        result.isSerializable = serializable;
        result.isExternalizable = externalizable;

        // Now we fill in the values
        result.setName(cl.getName());
        result.setClass(cl);
        Class<?> superclass = cl.getSuperclass();
        if (superclass != null) {
            result.setSuperclass(lookup(superclass));
        }

        Field[] declaredFields = null;

        // Compute the SUID
        if (serializable || externalizable) {
            if (result.isEnum() || result.isProxy()) {
                result.setSerialVersionUID(0L);
            } else {
                declaredFields = cl.getDeclaredFields();
                result.setSerialVersionUID(computeSerialVersionUID(cl, declaredFields));
            }
        }

        // Serializables need field descriptors
        if (serializable && !isArray) {
            if (declaredFields == null) {
                declaredFields = cl.getDeclaredFields();
            }
            result.buildFieldDescriptors(declaredFields);
        } else {
            // Externalizables or arrays do not need FieldDesc info
            result.setFields(NO_FIELDS);
        }

        // Copy all fields to loadFields - they should be read by default in
        // ObjectInputStream.defaultReadObject() method
        ObjectStreamField[] fields = result.getFields();

        if (fields != null) {
            ObjectStreamField[] loadFields = new ObjectStreamField[fields.length];

            for (int i = 0; i < fields.length; ++i) {
                loadFields[i] = new ObjectStreamField(fields[i].getName(),
                        fields[i].getType(), fields[i].isUnshared());

                // resolve type string to init typeString field in
                // ObjectStreamField
                loadFields[i].getTypeString();
            }
            result.setLoadFields(loadFields);
        }

        byte flags = 0;
        if (externalizable) {
            flags |= ObjectStreamConstants.SC_EXTERNALIZABLE;
            flags |= ObjectStreamConstants.SC_BLOCK_DATA; // use protocol version 2 by default
        } else if (serializable) {
            flags |= ObjectStreamConstants.SC_SERIALIZABLE;
        }
        result.methodWriteReplace = findMethod(cl, "writeReplace");
        result.methodReadResolve = findMethod(cl, "readResolve");
        result.methodWriteObject = findPrivateMethod(cl, "writeObject", WRITE_PARAM_TYPES);
        result.methodReadObject = findPrivateMethod(cl, "readObject", READ_PARAM_TYPES);
        result.methodReadObjectNoData = findPrivateMethod(cl, "readObjectNoData", EmptyArray.CLASS);
        if (result.hasMethodWriteObject()) {
            flags |= ObjectStreamConstants.SC_WRITE_METHOD;
        }
        result.setFlags(flags);

        return result;
    }

    /**
     * Builds the collection of field descriptors for the receiver
     *
     * @param declaredFields
     *            collection of java.lang.reflect.Field for which to compute
     *            field descriptors
     */
    void buildFieldDescriptors(Field[] declaredFields) {
        // We could find the field ourselves in the collection, but calling
        // reflect is easier. Optimize if needed.
        final Field f = ObjectStreamClass.fieldSerialPersistentFields(this.forClass());
        // If we could not find the emulated fields, we'll have to compute
        // dumpable fields from reflect fields
        boolean useReflectFields = f == null; // Assume we will compute the
        // fields to dump based on the
        // reflect fields

        ObjectStreamField[] _fields = null;
        if (!useReflectFields) {
            // The user declared a collection of emulated fields. Use them.
            // We have to be able to fetch its value, even if it is private
            f.setAccessible(true);
            try {
                // static field, pass null
                _fields = (ObjectStreamField[]) f.get(null);
            } catch (IllegalAccessException ex) {
                throw new AssertionError(ex);
            }
        } else {
            // Compute collection of dumpable fields based on reflect fields
            List<ObjectStreamField> serializableFields =
                    new ArrayList<ObjectStreamField>(declaredFields.length);
            // Filter, we are only interested in fields that are serializable
            for (Field declaredField : declaredFields) {
                int modifiers = declaredField.getModifiers();
                if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                    ObjectStreamField field = new ObjectStreamField(declaredField.getName(),
                            declaredField.getType());
                    serializableFields.add(field);
                }
            }

            if (serializableFields.size() == 0) {
                _fields = NO_FIELDS; // If no serializable fields, share the
                // special value so that users can test
            } else {
                _fields = serializableFields.toArray(new ObjectStreamField[serializableFields.size()]);
            }
        }
        Arrays.sort(_fields);
        // assign offsets
        int primOffset = 0, objectOffset = 0;
        for (int i = 0; i < _fields.length; i++) {
            Class<?> type = _fields[i].getType();
            if (type.isPrimitive()) {
                _fields[i].offset = primOffset;
                primOffset += primitiveSize(type);
            } else {
                _fields[i].offset = objectOffset++;
            }
        }
        fields = _fields;
    }

    /**
     * Compute and return the Serial Version UID of the class {@code cl}.
     * The value is computed based on the class name, superclass chain, field
     * names, method names, modifiers, etc.
     *
     * @param cl
     *            a java.lang.Class for which to compute the SUID
     * @param fields
     *            cl.getDeclaredFields(), pre-computed by the caller
     * @return the value of SUID of this class
     */
    private static long computeSerialVersionUID(Class<?> cl, Field[] fields) {
        /*
         * First we should try to fetch the static slot 'static final long
         * serialVersionUID'. If it is defined, return it. If not defined, we
         * really need to compute SUID using SHAOutputStream
         */
        for (int i = 0; i < fields.length; i++) {
            final Field field = fields[i];
            if (field.getType() == long.class) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                    if (UID_FIELD_NAME.equals(field.getName())) {
                        /*
                         * We need to be able to see it even if we have no
                         * visibility. That is why we set accessible first (new
                         * API in reflect 1.2)
                         */
                        field.setAccessible(true);
                        try {
                            // Static field, parameter is ignored
                            return field.getLong(null);
                        } catch (IllegalAccessException iae) {
                            throw new RuntimeException("Error fetching SUID: " + iae);
                        }
                    }
                }
            }
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
        ByteArrayOutputStream sha = new ByteArrayOutputStream();
        try {
            DataOutputStream output = new DataOutputStream(sha);
            output.writeUTF(cl.getName());
            int classModifiers = CLASS_MODIFIERS_MASK & cl.getModifiers();
            /*
             * Workaround for 1F9LOQO. Arrays are ABSTRACT in JDK, but that is
             * not in the specification. Since we want to be compatible for
             * X-loading, we have to pretend we have the same shape
             */
            boolean isArray = cl.isArray();
            if (isArray) {
                classModifiers |= Modifier.ABSTRACT;
            }
            // Required for JDK UID compatibility
            if (cl.isInterface() && !Modifier.isPublic(classModifiers)) {
                classModifiers &= ~Modifier.ABSTRACT;
            }
            output.writeInt(classModifiers);

            /*
             * In JDK1.2 arrays implement Cloneable and Serializable but not in
             * JDK 1.1.7. So, JDK 1.2 "pretends" arrays have no interfaces when
             * computing SHA-1 to be compatible.
             */
            if (!isArray) {
                // Interface information
                Class<?>[] interfaces = cl.getInterfaces();
                if (interfaces.length > 1) {
                    // Only attempt to sort if really needed (saves object
                    // creation, etc)
                    Comparator<Class<?>> interfaceComparator = new Comparator<Class<?>>() {
                        public int compare(Class<?> itf1, Class<?> itf2) {
                            return itf1.getName().compareTo(itf2.getName());
                        }
                    };
                    Arrays.sort(interfaces, interfaceComparator);
                }

                // Dump them
                for (int i = 0; i < interfaces.length; i++) {
                    output.writeUTF(interfaces[i].getName());
                }
            }

            // Field information
            if (fields.length > 1) {
                // Only attempt to sort if really needed (saves object creation,
                // etc)
                Comparator<Field> fieldComparator = new Comparator<Field>() {
                    public int compare(Field field1, Field field2) {
                        return field1.getName().compareTo(field2.getName());
                    }
                };
                Arrays.sort(fields, fieldComparator);
            }

            // Dump them
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                int modifiers = field.getModifiers() & FIELD_MODIFIERS_MASK;

                boolean skip = Modifier.isPrivate(modifiers) &&
                        (Modifier.isTransient(modifiers) || Modifier.isStatic(modifiers));
                if (!skip) {
                    // write name, modifier & "descriptor" of all but private
                    // static and private transient
                    output.writeUTF(field.getName());
                    output.writeInt(modifiers);
                    output.writeUTF(descriptorForFieldSignature(getFieldSignature(field)));
                }
            }

            /*
             * Normally constructors come before methods (because <init> <
             * anyMethodName). However, <clinit> is an exception. Besides,
             * reflect will not let us get to it.
             */
            if (hasClinit(cl)) {
                // write name, modifier & "descriptor"
                output.writeUTF(CLINIT_NAME);
                output.writeInt(CLINIT_MODIFIERS);
                output.writeUTF(CLINIT_SIGNATURE);
            }

            // Constructor information
            Constructor<?>[] constructors = cl.getDeclaredConstructors();
            if (constructors.length > 1) {
                // Only attempt to sort if really needed (saves object creation,
                // etc)
                Comparator<Constructor<?>> constructorComparator = new Comparator<Constructor<?>>() {
                    public int compare(Constructor<?> ctr1, Constructor<?> ctr2) {
                        // All constructors have same name, so we sort based on
                        // signature
                        return (getConstructorSignature(ctr1)
                                .compareTo(getConstructorSignature(ctr2)));
                    }
                };
                Arrays.sort(constructors, constructorComparator);
            }

            // Dump them
            for (int i = 0; i < constructors.length; i++) {
                Constructor<?> constructor = constructors[i];
                int modifiers = constructor.getModifiers()
                        & METHOD_MODIFIERS_MASK;
                boolean isPrivate = Modifier.isPrivate(modifiers);
                if (!isPrivate) {
                    /*
                     * write name, modifier & "descriptor" of all but private
                     * ones
                     *
                     * constructor.getName() returns the constructor name as
                     * typed, not the VM name
                     */
                    output.writeUTF("<init>");
                    output.writeInt(modifiers);
                    output.writeUTF(descriptorForSignature(
                            getConstructorSignature(constructor)).replace('/',
                            '.'));
                }
            }

            // Method information
            Method[] methods = cl.getDeclaredMethods();
            if (methods.length > 1) {
                Comparator<Method> methodComparator = new Comparator<Method>() {
                    public int compare(Method m1, Method m2) {
                        int result = m1.getName().compareTo(m2.getName());
                        if (result == 0) {
                            // same name, signature will tell which one comes
                            // first
                            return getMethodSignature(m1).compareTo(
                                    getMethodSignature(m2));
                        }
                        return result;
                    }
                };
                Arrays.sort(methods, methodComparator);
            }

            // Dump them
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                int modifiers = method.getModifiers() & METHOD_MODIFIERS_MASK;
                boolean isPrivate = Modifier.isPrivate(modifiers);
                if (!isPrivate) {
                    // write name, modifier & "descriptor" of all but private
                    // ones
                    output.writeUTF(method.getName());
                    output.writeInt(modifiers);
                    output.writeUTF(descriptorForSignature(
                            getMethodSignature(method)).replace('/', '.'));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e + " computing SHA-1/SUID");
        }

        // now compute the UID based on the SHA
        byte[] hash = digest.digest(sha.toByteArray());
        return Memory.peekLong(hash, 0, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Returns what the serialization specification calls "descriptor" given a
     * field signature.
     *
     * @param signature
     *            a field signature
     * @return containing the descriptor
     */
    private static String descriptorForFieldSignature(String signature) {
        return signature.replace('.', '/');
    }

    /**
     * Return what the serialization specification calls "descriptor" given a
     * method/constructor signature.
     *
     * @param signature
     *            a method or constructor signature
     * @return containing the descriptor
     */
    private static String descriptorForSignature(String signature) {
        return signature.substring(signature.indexOf("("));
    }

    /**
     * Return the java.lang.reflect.Field {@code serialPersistentFields}
     * if class {@code cl} implements it. Return null otherwise.
     *
     * @param cl
     *            a java.lang.Class which to test
     * @return {@code java.lang.reflect.Field} if the class has
     *         serialPersistentFields {@code null} if the class does not
     *         have serialPersistentFields
     */
    static Field fieldSerialPersistentFields(Class<?> cl) {
        try {
            Field f = cl.getDeclaredField("serialPersistentFields");
            int modifiers = f.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isPrivate(modifiers)
                    && Modifier.isFinal(modifiers)) {
                if (f.getType() == ARRAY_OF_FIELDS) {
                    return f;
                }
            }
        } catch (NoSuchFieldException nsm) {
            // Ignored
        }
        return null;
    }

    /**
     * Returns the class (java.lang.Class) for this descriptor.
     *
     * @return the class in the local VM that this descriptor represents;
     *         {@code null} if there is no corresponding class.
     */
    public Class<?> forClass() {
        return resolvedClass;
    }

    /**
     * Create and return a new instance of class 'instantiationClass'
     * using JNI to call the constructor chosen by resolveConstructorClass.
     *
     * The returned instance may have uninitialized fields, including final fields.
     */
    Object newInstance(Class<?> instantiationClass) throws InvalidClassException {
        resolveConstructorClass(instantiationClass);
        return newInstance(instantiationClass, resolvedConstructorMethodId);
    }
    private static native Object newInstance(Class<?> instantiationClass, long methodId);

    private Class<?> resolveConstructorClass(Class<?> objectClass) throws InvalidClassException {
        if (resolvedConstructorClass != null) {
            return resolvedConstructorClass;
        }

        // The class of the instance may not be the same as the class of the
        // constructor to run
        // This is the constructor to run if Externalizable
        Class<?> constructorClass = objectClass;

        // WARNING - What if the object is serializable and externalizable ?
        // Is that possible ?
        boolean wasSerializable = (flags & ObjectStreamConstants.SC_SERIALIZABLE) != 0;
        if (wasSerializable) {
            // Now we must run the constructor of the class just above the
            // one that implements Serializable so that slots that were not
            // dumped can be initialized properly
            while (constructorClass != null && ObjectStreamClass.isSerializable(constructorClass)) {
                constructorClass = constructorClass.getSuperclass();
            }
        }

        // Fetch the empty constructor, or null if none.
        Constructor<?> constructor = null;
        if (constructorClass != null) {
            try {
                constructor = constructorClass.getDeclaredConstructor(EmptyArray.CLASS);
            } catch (NoSuchMethodException ignored) {
            }
        }

        // Has to have an empty constructor
        if (constructor == null) {
            String className = constructorClass != null ? constructorClass.getName() : null;
            throw new InvalidClassException(className, "IllegalAccessException");
        }

        int constructorModifiers = constructor.getModifiers();
        boolean isPublic = Modifier.isPublic(constructorModifiers);
        boolean isProtected = Modifier.isProtected(constructorModifiers);
        boolean isPrivate = Modifier.isPrivate(constructorModifiers);

        // Now we must check if the empty constructor is visible to the
        // instantiation class
        boolean wasExternalizable = (flags & ObjectStreamConstants.SC_EXTERNALIZABLE) != 0;
        if (isPrivate || (wasExternalizable && !isPublic)) {
            throw new InvalidClassException(constructorClass.getName(), "IllegalAccessException");
        }

        // We know we are testing from a subclass, so the only other case
        // where the visibility is not allowed is when the constructor has
        // default visibility and the instantiation class is in a different
        // package than the constructor class
        if (!isPublic && !isProtected) {
            // Not public, not private and not protected...means default
            // visibility. Check if same package
            if (!inSamePackage(constructorClass, objectClass)) {
                throw new InvalidClassException(constructorClass.getName(), "IllegalAccessException");
            }
        }

        resolvedConstructorClass = constructorClass;
        resolvedConstructorMethodId = getConstructorId(resolvedConstructorClass);
        return constructorClass;
    }
    private static native long getConstructorId(Class<?> c);

    /**
     * Checks if two classes belong to the same package.
     *
     * @param c1
     *            one of the classes to test.
     * @param c2
     *            the other class to test.
     * @return {@code true} if the two classes belong to the same package,
     *         {@code false} otherwise.
     */
    private boolean inSamePackage(Class<?> c1, Class<?> c2) {
        String nameC1 = c1.getName();
        String nameC2 = c2.getName();
        int indexDotC1 = nameC1.lastIndexOf('.');
        int indexDotC2 = nameC2.lastIndexOf('.');
        if (indexDotC1 != indexDotC2) {
            return false; // cannot be in the same package if indices are not the same
        }
        if (indexDotC1 == -1) {
            return true; // both of them are in default package
        }
        return nameC1.regionMatches(0, nameC2, 0, indexDotC1);
    }

    /**
     * Return a String representing the signature for a Constructor {@code c}.
     *
     * @param c
     *            a java.lang.reflect.Constructor for which to compute the
     *            signature
     * @return the constructor's signature
     */
    static native String getConstructorSignature(Constructor<?> c);

    /**
     * Gets a field descriptor of the class represented by this class
     * descriptor.
     *
     * @param name
     *            the name of the desired field.
     * @return the field identified by {@code name} or {@code null} if there is
     *         no such field.
     */
    public ObjectStreamField getField(String name) {
        ObjectStreamField[] allFields = getFields();
        for (int i = 0; i < allFields.length; i++) {
            ObjectStreamField f = allFields[i];
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    /**
     * Returns the collection of field descriptors for the fields of the
     * corresponding class
     *
     * @return the receiver's collection of declared fields for the class it
     *         represents
     */
    ObjectStreamField[] fields() {
        if (fields == null) {
            Class<?> forCl = forClass();
            if (forCl != null && isSerializable() && !forCl.isArray()) {
                buildFieldDescriptors(forCl.getDeclaredFields());
            } else {
                // Externalizables or arrays do not need FieldDesc info
                setFields(NO_FIELDS);
            }
        }
        return fields;
    }

    /**
     * Returns a collection of field descriptors for the serialized fields of
     * the class represented by this class descriptor.
     *
     * @return an array of field descriptors or an array of length zero if there
     *         are no fields in this descriptor's class.
     */
    public ObjectStreamField[] getFields() {
        copyFieldAttributes();
        return loadFields == null ? fields().clone() : loadFields.clone();
    }

    private transient volatile List<ObjectStreamClass> cachedHierarchy;

    List<ObjectStreamClass> getHierarchy() {
        List<ObjectStreamClass> result = cachedHierarchy;
        if (result == null) {
            cachedHierarchy = result = makeHierarchy();
        }
        return result;
    }

    private List<ObjectStreamClass> makeHierarchy() {
        ArrayList<ObjectStreamClass> result = new ArrayList<ObjectStreamClass>();
        for (ObjectStreamClass osc = this; osc != null; osc = osc.getSuperclass()) {
            result.add(0, osc);
        }
        return result;
    }

    /**
     * If a Class uses "serialPersistentFields" to define the serialized fields,
     * this.loadFields cannot get the "unshared" information when deserializing
     * fields using current implementation of ObjectInputStream. This method
     * provides a way to copy the "unshared" attribute from this.fields.
     *
     */
    private void copyFieldAttributes() {
        if ((loadFields == null) || fields == null) {
            return;
        }

        for (int i = 0; i < loadFields.length; i++) {
            ObjectStreamField loadField = loadFields[i];
            String name = loadField.getName();
            for (int j = 0; j < fields.length; j++) {
                ObjectStreamField field = fields[j];
                if (name.equals(field.getName())) {
                    loadField.setUnshared(field.isUnshared());
                    loadField.setOffset(field.getOffset());
                    break;
                }
            }
        }
    }

    /**
     * Returns the collection of field descriptors for the input fields of the
     * corresponding class
     *
     * @return the receiver's collection of input fields for the class it
     *         represents
     */
    ObjectStreamField[] getLoadFields() {
        return loadFields;
    }

    /**
     * Return a String representing the signature for a field {@code f}.
     *
     * @param f
     *            a java.lang.reflect.Field for which to compute the signature
     * @return the field's signature
     */
    private static native String getFieldSignature(Field f);

    /**
     * Returns the flags for this descriptor, where possible combined values are
     *
     * ObjectStreamConstants.SC_WRITE_METHOD
     * ObjectStreamConstants.SC_SERIALIZABLE
     * ObjectStreamConstants.SC_EXTERNALIZABLE
     *
     * @return byte the receiver's flags for the class it represents
     */
    byte getFlags() {
        return flags;
    }

    /**
     * Return a String representing the signature for a method {@code m}.
     *
     * @param m
     *            a java.lang.reflect.Method for which to compute the signature
     * @return the method's signature
     */
    static native String getMethodSignature(Method m);

    /**
     * Returns the name of the class represented by this descriptor.
     *
     * @return the fully qualified name of the class this descriptor represents.
     */
    public String getName() {
        return className;
    }

    /**
     * Returns the Serial Version User ID of the class represented by this
     * descriptor.
     *
     * @return the SUID for the class represented by this descriptor.
     */
    public long getSerialVersionUID() {
        return svUID;
    }

    /**
     * Returns the descriptor (ObjectStreamClass) of the superclass of the class
     * represented by the receiver.
     *
     * @return an ObjectStreamClass representing the superclass of the class
     *         represented by the receiver.
     */
    ObjectStreamClass getSuperclass() {
        return superclass;
    }

    /**
     * Return true if the given class {@code cl} has the
     * compiler-generated method {@code clinit}. Even though it is
     * compiler-generated, it is used by the serialization code to compute SUID.
     * This is unfortunate, since it may depend on compiler optimizations in
     * some cases.
     *
     * @param cl
     *            a java.lang.Class which to test
     * @return {@code true} if the class has <clinit> {@code false}
     *         if the class does not have <clinit>
     */
    private static native boolean hasClinit(Class<?> cl);

    /**
     * Return true if instances of class {@code cl} are Externalizable,
     * false otherwise.
     *
     * @param cl
     *            a java.lang.Class which to test
     * @return {@code true} if instances of the class are Externalizable
     *         {@code false} if instances of the class are not
     *         Externalizable
     *
     * @see Object#hashCode
     */
    static boolean isExternalizable(Class<?> cl) {
        return EXTERNALIZABLE.isAssignableFrom(cl);
    }

    /**
     * Return true if the type code
     * <code>typecode<code> describes a primitive type
     *
     * @param typecode a char describing the typecode
     * @return {@code true} if the typecode represents a primitive type
     * {@code false} if the typecode represents an Object type (including arrays)
     *
     * @see Object#hashCode
     */
    static boolean isPrimitiveType(char typecode) {
        return !(typecode == '[' || typecode == 'L');
    }

    /**
     * Return true if instances of class {@code cl} are Serializable,
     * false otherwise.
     *
     * @param cl
     *            a java.lang.Class which to test
     * @return {@code true} if instances of the class are Serializable
     *         {@code false} if instances of the class are not
     *         Serializable
     *
     * @see Object#hashCode
     */
    static boolean isSerializable(Class<?> cl) {
        return SERIALIZABLE.isAssignableFrom(cl);
    }

    /**
     * Resolves the class properties, if they weren't already
     */
    private void resolveProperties() {
        if (arePropertiesResolved) {
            return;
        }

        Class<?> cl = forClass();
        isProxy = Proxy.isProxyClass(cl);
        isEnum = Enum.class.isAssignableFrom(cl);
        isSerializable = isSerializable(cl);
        isExternalizable = isExternalizable(cl);

        arePropertiesResolved = true;
    }

    boolean isSerializable() {
        resolveProperties();
        return isSerializable;
    }

    boolean isExternalizable() {
        resolveProperties();
        return isExternalizable;
    }

    boolean isProxy() {
        resolveProperties();
        return isProxy;
    }

    boolean isEnum() {
        resolveProperties();
        return isEnum;
    }

    /**
     * Returns the descriptor for a serializable class.
     * Returns null if the class doesn't implement {@code Serializable} or {@code Externalizable}.
     *
     * @param cl
     *            a java.lang.Class for which to obtain the corresponding
     *            descriptor
     * @return the corresponding descriptor if the class is serializable or
     *         externalizable; null otherwise.
     */
    public static ObjectStreamClass lookup(Class<?> cl) {
        ObjectStreamClass osc = lookupStreamClass(cl);
        return (osc.isSerializable() || osc.isExternalizable()) ? osc : null;
    }

    /**
     * Returns the descriptor for any class, whether or not the class
     * implements Serializable or Externalizable.
     *
     * @param cl
     *            a java.lang.Class for which to obtain the corresponding
     *            descriptor
     * @return the descriptor
     * @since 1.6
     */
    public static ObjectStreamClass lookupAny(Class<?> cl) {
        return lookupStreamClass(cl);
    }

    /**
     * Return the descriptor (ObjectStreamClass) corresponding to the class
     * {@code cl}. Returns an ObjectStreamClass even if instances of the
     * class cannot be serialized
     *
     * @param cl
     *            a java.langClass for which to obtain the corresponding
     *            descriptor
     * @return the corresponding descriptor
     */
    static ObjectStreamClass lookupStreamClass(Class<?> cl) {
        WeakHashMap<Class<?>, ObjectStreamClass> tlc = getCache();
        ObjectStreamClass cachedValue = tlc.get(cl);
        if (cachedValue == null) {
            cachedValue = createClassDesc(cl);
            tlc.put(cl, cachedValue);
        }
        return cachedValue;

    }

    /**
     * A ThreadLocal cache for lookupStreamClass, with the possibility of discarding the thread
     * local storage content when the heap is exhausted.
     */
    private static SoftReference<ThreadLocal<WeakHashMap<Class<?>, ObjectStreamClass>>> storage =
            new SoftReference<ThreadLocal<WeakHashMap<Class<?>, ObjectStreamClass>>>(null);

    private static WeakHashMap<Class<?>, ObjectStreamClass> getCache() {
        ThreadLocal<WeakHashMap<Class<?>, ObjectStreamClass>> tls = storage.get();
        if (tls == null) {
            tls = new ThreadLocal<WeakHashMap<Class<?>, ObjectStreamClass>>() {
                public WeakHashMap<Class<?>, ObjectStreamClass> initialValue() {
                    return new WeakHashMap<Class<?>, ObjectStreamClass>();
                }
            };
            storage = new SoftReference<ThreadLocal<WeakHashMap<Class<?>, ObjectStreamClass>>>(tls);
        }
        return tls.get();
    }

    /**
     * Return the java.lang.reflect.Method if class <code>cl</code> implements
     * <code>methodName</code> . Return null otherwise.
     *
     * @param cl
     *            a java.lang.Class which to test
     * @return <code>java.lang.reflect.Method</code> if the class implements
     *         writeReplace <code>null</code> if the class does not implement
     *         writeReplace
     */
    static Method findMethod(Class<?> cl, String methodName) {
        Class<?> search = cl;
        Method method = null;
        while (search != null) {
            try {
                method = search.getDeclaredMethod(methodName, (Class[]) null);
                if (search == cl
                        || (method.getModifiers() & Modifier.PRIVATE) == 0) {
                    method.setAccessible(true);
                    return method;
                }
            } catch (NoSuchMethodException nsm) {
            }
            search = search.getSuperclass();
        }
        return null;
    }

    /**
     * Return the java.lang.reflect.Method if class <code>cl</code> implements
     * private <code>methodName</code> . Return null otherwise.
     *
     * @param cl
     *            a java.lang.Class which to test
     * @return {@code java.lang.reflect.Method} if the class implements
     *         writeReplace {@code null} if the class does not implement
     *         writeReplace
     */
    static Method findPrivateMethod(Class<?> cl, String methodName,
            Class<?>[] param) {
        try {
            Method method = cl.getDeclaredMethod(methodName, param);
            if (Modifier.isPrivate(method.getModifiers()) && method.getReturnType() == void.class) {
                method.setAccessible(true);
                return method;
            }
        } catch (NoSuchMethodException nsm) {
            // Ignored
        }
        return null;
    }

    boolean hasMethodWriteReplace() {
        return (methodWriteReplace != null);
    }

    Method getMethodWriteReplace() {
        return methodWriteReplace;
    }

    boolean hasMethodReadResolve() {
        return (methodReadResolve != null);
    }

    Method getMethodReadResolve() {
        return methodReadResolve;
    }

    boolean hasMethodWriteObject() {
        return (methodWriteObject != null);
    }

    Method getMethodWriteObject() {
        return methodWriteObject;
    }

    boolean hasMethodReadObject() {
        return (methodReadObject != null);
    }

    Method getMethodReadObject() {
        return methodReadObject;
    }

    boolean hasMethodReadObjectNoData() {
        return (methodReadObjectNoData != null);
    }

    Method getMethodReadObjectNoData() {
        return methodReadObjectNoData;
    }

    void initPrivateFields(ObjectStreamClass desc) {
        methodWriteReplace = desc.methodWriteReplace;
        methodReadResolve = desc.methodReadResolve;
        methodWriteObject = desc.methodWriteObject;
        methodReadObject = desc.methodReadObject;
        methodReadObjectNoData = desc.methodReadObjectNoData;
    }

    /**
     * Set the class (java.lang.Class) that the receiver represents
     *
     * @param c
     *            aClass, the new class that the receiver describes
     */
    void setClass(Class<?> c) {
        resolvedClass = c;
    }

    /**
     * Set the collection of field descriptors for the fields of the
     * corresponding class
     *
     * @param f
     *            ObjectStreamField[], the receiver's new collection of declared
     *            fields for the class it represents
     */
    void setFields(ObjectStreamField[] f) {
        fields = f;
    }

    /**
     * Set the collection of field descriptors for the input fields of the
     * corresponding class
     *
     * @param f
     *            ObjectStreamField[], the receiver's new collection of input
     *            fields for the class it represents
     */
    void setLoadFields(ObjectStreamField[] f) {
        loadFields = f;
    }

    /**
     * Set the flags for this descriptor, where possible combined values are
     *
     * ObjectStreamConstants.SC_WRITE_METHOD
     * ObjectStreamConstants.SC_SERIALIZABLE
     * ObjectStreamConstants.SC_EXTERNALIZABLE
     *
     * @param b
     *            byte, the receiver's new flags for the class it represents
     */
    void setFlags(byte b) {
        flags = b;
    }

    /**
     * Set the name of the class represented by the receiver
     *
     * @param newName
     *            a String, the new fully qualified name of the class the
     *            receiver represents
     */
    void setName(String newName) {
        className = newName;
    }

    /**
     * Set the Serial Version User ID of the class represented by the receiver
     *
     * @param l
     *            a long, the new SUID for the class represented by the receiver
     */
    void setSerialVersionUID(long l) {
        svUID = l;
    }

    /**
     * Set the descriptor for the superclass of the class described by the
     * receiver
     *
     * @param c
     *            an ObjectStreamClass, the new ObjectStreamClass for the
     *            superclass of the class represented by the receiver
     */
    void setSuperclass(ObjectStreamClass c) {
        superclass = c;
    }

    private int primitiveSize(Class<?> type) {
        if (type == byte.class || type == boolean.class) {
            return 1;
        }
        if (type == short.class || type == char.class) {
            return 2;
        }
        if (type == int.class || type == float.class) {
            return 4;
        }
        if (type == long.class || type == double.class) {
            return 8;
        }
        throw new AssertionError();
    }

    /**
     * Returns a string containing a concise, human-readable description of this
     * descriptor.
     *
     * @return a printable representation of this descriptor.
     */
    @Override
    public String toString() {
        return getName() + ": static final long serialVersionUID =" + getSerialVersionUID() + "L;";
    }
}
