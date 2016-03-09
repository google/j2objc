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

import java.text.Normalizer;

public class NormalizerTest extends junit.framework.TestCase {
    public void testNormalize() {
        final String src = "\u03d3\u03D4\u1E9B";

        // Should already be canonical composed
        assertEquals(src, Normalizer.normalize(src, Normalizer.Form.NFC));

        // Composed to canonical decomposed
        assertEquals("\u03d2\u0301\u03d2\u0308\u017f\u0307",
                Normalizer.normalize(src, Normalizer.Form.NFD));

        // Composed to compatibility composed
        assertEquals("\u038e\u03ab\u1e61", Normalizer.normalize(src, Normalizer.Form.NFKC));

        // Composed to compatibility decomposed
        assertEquals("\u03a5\u0301\u03a5\u0308\u0073\u0307",
                Normalizer.normalize(src, Normalizer.Form.NFKD));

        // Decomposed to canonical composed
        assertEquals("\u00e9", Normalizer.normalize("\u0065\u0301", Normalizer.Form.NFC));

        // Decomposed to compatibility composed
        assertEquals("\u1e69", Normalizer.normalize("\u1e9b\u0323", Normalizer.Form.NFKC));

        try {
            Normalizer.normalize(null, Normalizer.Form.NFC);
            fail("Did not throw error on null argument");
        } catch (NullPointerException e) {
            // pass
        }
    }

    public void testIsNormalized() {
        String target;

        target = new String(new char[] {0x03D3, 0x03D4, 0x1E9B});
        assertTrue(Normalizer.isNormalized(target, Normalizer.Form.NFC));
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFD));
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFKC));
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFKD));

        target = new String(new char[] {0x03D2, 0x0301, 0x03D2, 0x0308, 0x017F, 0x0307});
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFC));
        assertTrue(Normalizer.isNormalized(target, Normalizer.Form.NFD));
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFKC));
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFKD));

        target = new String(new char[] {0x038E, 0x03AB, 0x1E61});
        assertTrue(Normalizer.isNormalized(target, Normalizer.Form.NFC));
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFD));
        assertTrue(Normalizer.isNormalized(target, Normalizer.Form.NFKC));
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFKD));

        target = new String(new char[] {0x03A5, 0x0301, 0x03A5, 0x0308, 0x0073, 0x0307});
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFC));
        assertTrue(Normalizer.isNormalized(target, Normalizer.Form.NFD));
        assertFalse(Normalizer.isNormalized(target, Normalizer.Form.NFKC));
        assertTrue(Normalizer.isNormalized(target, Normalizer.Form.NFKD));

        try {
            Normalizer.isNormalized(null, Normalizer.Form.NFC);
            fail("Did not throw NullPointerException on null argument");
        } catch (NullPointerException e) {
            // pass
        }
    }
}
