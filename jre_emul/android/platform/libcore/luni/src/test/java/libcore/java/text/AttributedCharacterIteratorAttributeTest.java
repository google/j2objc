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

package libcore.java.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.text.AttributedCharacterIterator;
import java.text.DateFormat;
import java.text.NumberFormat;
import junit.framework.TestCase;
import libcore.util.SerializationTester;

/**
 * AttributedCharacterIterator.Attribute is used like the base enum type and
 * subclassed by unrelated classes.
 */
public final class AttributedCharacterIteratorAttributeTest extends TestCase {

    public void testSerialization() throws IOException, ClassNotFoundException {
        assertSameReserialized(AttributedCharacterIterator.Attribute.LANGUAGE);
        assertSameReserialized(DateFormat.Field.ERA);
        assertSameReserialized(DateFormat.Field.TIME_ZONE);
        assertSameReserialized(NumberFormat.Field.INTEGER);
    }

    public void testSerializingSubclass() throws IOException, ClassNotFoundException {
        AttributedCharacterIterator.Attribute a = new CustomAttribute();
        try {
            SerializationTester.reserialize(a);
            fail();
        } catch (InvalidObjectException expected) {
        }
    }

    private void assertSameReserialized(Object o) throws ClassNotFoundException, IOException {
        assertSame(o, SerializationTester.reserialize(o));
    }

    private static class CustomAttribute extends AttributedCharacterIterator.Attribute {
        public CustomAttribute() {
            super("a");
        }
    }
}
