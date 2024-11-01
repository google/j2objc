/*
 * Copyright (C) 2023 The Android Open Source Project
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

package libcore.java.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

public class RecordTest {

    public record RecordInteger(int x) {};

    @Test
    public void testHashCode() {
        RecordInteger a = new RecordInteger(9);
        RecordInteger b = new RecordInteger(9);
        RecordInteger c = new RecordInteger(0);

        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());
    }

    @Test
    public void testEquals() {
        RecordInteger a = new RecordInteger(9);
        RecordInteger b = new RecordInteger(9);
        RecordInteger c = new RecordInteger(0);

        assertTrue(a.equals(b));
        assertEquals(a, b);
        assertFalse(a.equals(c));
        assertNotEquals(a, c);
    }

    @Test
    public void testToString() {
        RecordInteger a = new RecordInteger(9);
        RecordInteger b = new RecordInteger(9);
        RecordInteger c = new RecordInteger(0);

        assertEquals(a.toString(), b.toString());
        assertNotEquals(a.toString(), c.toString());
    }

    @Test
    public void testReflection() {
        RecordInteger a = new RecordInteger(9);

        Field[] fields = a.getClass().getDeclaredFields();
        assertEquals(Arrays.deepToString(fields), 1, fields.length);
        Constructor<?> c = RecordInteger.class.getConstructors()[0];
        assertEquals(Arrays.deepToString(c.getParameters()), 1, c.getParameters().length);
        assertEquals(c.getParameters()[0].toString(), "x", c.getParameters()[0].getName());
        assertEquals(fields[0].toString(), "x", fields[0].getName());
        assertTrue(a.getClass().isRecord());
        assertEquals(Arrays.deepToString(a.getClass().getRecordComponents()),
                1, a.getClass().getRecordComponents().length);
    }
}
