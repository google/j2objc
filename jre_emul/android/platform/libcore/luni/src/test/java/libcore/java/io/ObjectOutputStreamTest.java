/*
 * Copyright (C) 2010 The Android Open Source Project
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;

public final class ObjectOutputStreamTest extends TestCase {
    public void testLongString() throws Exception {
        // Most modified UTF-8 is limited to 64KiB, but serialized strings can have an 8-byte
        // length, so this should never throw java.io.UTFDataFormatException...
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 64*1024 * 2; ++i) {
            sb.append('a');
        }
        String s = sb.toString();
        ObjectOutputStream os = new ObjectOutputStream(new ByteArrayOutputStream());
        os.writeObject(s);
    }

    public static class CallsCloseInWriteObjectMethod implements Serializable {
        private String message;

        public CallsCloseInWriteObjectMethod(String message) {
            this.message = message;
        }

        private void writeObject(ObjectOutputStream oos) throws IOException {
            oos.writeObject(message);
            oos.close();
        }

        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            message = (String) ois.readObject();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CallsCloseInWriteObjectMethod that = (CallsCloseInWriteObjectMethod) o;

            return message.equals(that.message);
        }

        @Override
        public int hashCode() {
            return message.hashCode();
        }
    }

    // http://b/28159133
    public void testCloseInWriteObject() throws Exception {
        String hello = "Hello";
        CallsCloseInWriteObjectMethod object = new CallsCloseInWriteObjectMethod(hello);
        // This reproduces the problem in http://b/28159133 as follows:
        //   the list class gets handle N
        //   the object closes the ObjectOutputStream and clears the handle table
        //   the hello gets handle N
        //   the reuse of hello has a reference to handle N
        // When it is deserialized the list contains object, hello, Arrays.asList().getClass()
        // instead of object, hello, hello.
        List<Serializable> input = Arrays.asList(object, hello, hello);
        @SuppressWarnings("unchecked")
        List<CallsCloseInWriteObjectMethod> output = (List<CallsCloseInWriteObjectMethod>)
                roundTrip(input);

        assertEquals(input, output);
    }

    private Serializable roundTrip(Object object)
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
        }

        Serializable read;
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            read = (Serializable) ois.readObject();
        }
        return read;
    }
}
