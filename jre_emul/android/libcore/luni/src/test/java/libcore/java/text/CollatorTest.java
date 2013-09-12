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

import java.text.Collator;

public class CollatorTest extends junit.framework.TestCase {
    public void test_setStrengthI() throws Exception {
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
        assertEquals(Collator.PRIMARY, collator.getStrength());
        collator.setStrength(Collator.SECONDARY);
        assertEquals(Collator.SECONDARY, collator.getStrength());
        collator.setStrength(Collator.TERTIARY);
        assertEquals(Collator.TERTIARY, collator.getStrength());
        collator.setStrength(Collator.IDENTICAL);
        assertEquals(Collator.IDENTICAL, collator.getStrength());
        try {
            collator.setStrength(-1);
            fail("IllegalArgumentException was not thrown.");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void test_stackCorruption() throws Exception {
        // This used to crash Android.
        Collator mColl = Collator.getInstance();
        mColl.setStrength(Collator.PRIMARY);
        mColl.getCollationKey("2d294f2d3739433565147655394f3762f3147312d3731641452f310");
    }

    public void test_collationKeySize() throws Exception {
        // Test to verify that very large collation keys are not truncated.
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            b.append("0123456789ABCDEF");
        }
        String sixteen = b.toString();
        b.append("_THE_END");
        String sixteenplus = b.toString();

        Collator mColl = Collator.getInstance();
        mColl.setStrength(Collator.PRIMARY);

        byte [] arr = mColl.getCollationKey(sixteen).toByteArray();
        int len = arr.length;
        assertTrue("Collation key not 0 terminated", arr[arr.length - 1] == 0);
        len--;
        String foo = new String(arr, 0, len, "iso8859-1");

        arr = mColl.getCollationKey(sixteen).toByteArray();
        len = arr.length;
        assertTrue("Collation key not 0 terminated", arr[arr.length - 1] == 0);
        len--;
        String bar = new String(arr, 0, len, "iso8859-1");

        assertTrue("Collation keys should differ", foo.equals(bar));
    }
}
