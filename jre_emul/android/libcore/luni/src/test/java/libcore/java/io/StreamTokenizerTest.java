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

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Locale;
import junit.framework.TestCase;

public class StreamTokenizerTest extends TestCase {
    public void testLowerCase() throws Exception {
        Locale.setDefault(Locale.US);
        StreamTokenizer st = new StreamTokenizer(new StringReader("aIb aIb"));
        st.lowerCaseMode(true);
        st.nextToken();
        assertEquals("aib", st.sval);

        Locale.setDefault(new Locale("tr", "TR"));
        st.nextToken();
        assertEquals("a\u0131b", st.sval);
    }
}
