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

package tests.support;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

/**
 * Support class to verify that the methods of
 * {@code ObjectInputStream.GetField} handle default values correctly.
 * <p>
 * <strong>Important:</strong> The test method
 * {@code test_defaultedLjava_lang_String()} in
 * {@code tests.api.java.io.ObjectInputStreamGetFieldTest} depends on a
 * reference file which can be generated with
 * {@code tests.util.FieldTestFileGenerator}. However, the default mechanism of
 * {@code GetField} only works if the fields that are supposed to be read have
 * not been written to the file. This can only be accomplished if the fields do
 * not exist (are not declared) when writing an instance of this class.
 * Therefore, when executing {@code tests.util.FieldTestFileGenerator}, the
 * contents of this class have to be commented out.
 * </p>
 */
public class Support_GetPutFieldsDefaulted implements Serializable {

    private static final long serialVersionUID = 1L;

    public ObjectInputStream.GetField getField;
    public ObjectOutputStream.PutField putField;

    public boolean booleanValue = false;
    public byte byteValue = 0;
    public char charValue = 0;
    public double doubleValue = 0.0;
    public float floatValue = 0.0f;
    public long longValue = 0;
    public int intValue = 0;
    public short shortValue = 0;
    public SimpleClass objectValue = null;

    class SimpleClass implements Serializable {

        private static final long serialVersionUID = 1L;
        private int a;
        private String b;

        public SimpleClass(int aValue, String bValue) {
            a = aValue;
            b = bValue;
        }

        public int getA() {
            return a;
        }

        public String getB() {
            return b;
        }

        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }

            SimpleClass other = (SimpleClass) obj;
            return (a == other.getA() && b.equals(other.getB()));
        }
    }

    public void initTestValues() {
        booleanValue = true;
        byteValue = (byte) 0x0b;
        charValue = 'D';
        doubleValue = 523452.4532;
        floatValue = 298.54f;
        longValue = 1234567890l;
        intValue = 999999;
        objectValue = new SimpleClass(1965, "Hello Jupiter");
        shortValue = 4321;
    }

    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Support_GetPutFieldsDefaulted other = (Support_GetPutFieldsDefaulted) obj;
        return (booleanValue == other.booleanValue &&
                byteValue == other.byteValue &&
                charValue == other.charValue &&
                doubleValue == other.doubleValue &&
                floatValue == other.floatValue &&
                longValue == other.longValue &&
                intValue == other.intValue &&
                objectValue.equals(other.objectValue) &&
                shortValue == other.shortValue
                );
    }

    private void readObject(ObjectInputStream ois) throws Exception {
        getField = ois.readFields();
        booleanValue = getField.get("booleanValue", true);
        byteValue = getField.get("byteValue", (byte) 0x0b);
        charValue = getField.get("charValue", (char) 'D');
        doubleValue = getField.get("doubleValue", 523452.4532);
        floatValue = getField.get("floatValue", 298.54f);
        longValue = getField.get("longValue", (long) 1234567890l);
        intValue = getField.get("intValue", 999999);
        objectValue = (Support_GetPutFieldsDefaulted.SimpleClass)
                       getField.get("objectValue",
                                    new SimpleClass(1965, "Hello Jupiter"));
        shortValue = getField.get("shortValue", (short) 4321);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        putField = oos.putFields();
        // Do not put anything into putField so that the get methods
        // will have to use default values.
        oos.writeFields();
    }

}
