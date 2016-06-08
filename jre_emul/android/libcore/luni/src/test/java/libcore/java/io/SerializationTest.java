/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.io;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.io.Serializable;
import junit.framework.TestCase;
import libcore.util.SerializationTester;

public final class SerializationTest extends TestCase {

    // http://b/4471249
    public void testSerializeFieldMadeTransient() throws Exception {
        // Does ObjectStreamClass have the right idea?
        ObjectStreamClass osc = ObjectStreamClass.lookup(FieldMadeTransient.class);
        ObjectStreamField[] fields = osc.getFields();
        assertEquals(1, fields.length);
        assertEquals("nonTransientInt", fields[0].getName());
        assertEquals(int.class, fields[0].getType());

        // this was created by serializing a FieldMadeTransient with a non-0 transientInt
        String s = "aced0005737200346c6962636f72652e6a6176612e696f2e53657269616c697a6174696f6e54657"
                + "374244669656c644d6164655472616e7369656e74000000000000000002000149000c7472616e736"
                + "9656e74496e747870abababab";
        FieldMadeTransient deserialized = (FieldMadeTransient) SerializationTester.deserializeHex(s);
        assertEquals(0, deserialized.transientInt);
    }

    static class FieldMadeTransient implements Serializable {
        private static final long serialVersionUID = 0L;
        private transient int transientInt;
        private int nonTransientInt;
    }

    public void testSerialVersionUidChange() throws Exception {
        // this was created by serializing a SerialVersionUidChanged with serialVersionUID = 0L
        String s = "aced0005737200396c6962636f72652e6a6176612e696f2e53657269616c697a6174696f6e54657"
                + "3742453657269616c56657273696f6e5569644368616e67656400000000000000000200014900016"
                + "1787000000003";
        try {
            SerializationTester.deserializeHex(s);
            fail();
        } catch (InvalidClassException expected) {
        }
    }

    static class SerialVersionUidChanged implements Serializable {
        private static final long serialVersionUID = 1L; // was 0L
        private int a;
    }

    public void testMissingSerialVersionUid() throws Exception {
        // this was created by serializing a FieldsChanged with one int field named 'a'
        String s = "aced00057372002f6c6962636f72652e6a6176612e696f2e53657269616c697a6174696f6e54657"
                + "374244669656c64734368616e6765643bcfb934e310fa1c02000149000161787000000003";
        try {
            SerializationTester.deserializeHex(s);
            fail();
        } catch (InvalidClassException expected) {
        }
    }

    static class FieldsChanged implements Serializable {
        private int b; // was 'a'
    }
}
