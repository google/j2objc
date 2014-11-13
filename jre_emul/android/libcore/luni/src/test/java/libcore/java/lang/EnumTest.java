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

package libcore.java.lang;

import junit.framework.TestCase;
import libcore.util.SerializationTester;

public final class EnumTest extends TestCase {
    public void testEnumSerialization() {
        String s = "aced00057e7200236c6962636f72652e6a6176612e6c616e672e456e756d5465"
                + "737424526f7368616d626f00000000000000001200007872000e6a6176612e6c6"
                + "16e672e456e756d000000000000000012000078707400055041504552";
        Roshambo value = Roshambo.PAPER;
        assertTrue(value.getClass() == Roshambo.class);
        new SerializationTester<Roshambo>(value, s).test();
    }

    public void testEnumSubclassSerialization() {
        String s = "aced00057e7200236c6962636f72652e6a6176612e6c616e672e456e756d5465"
                + "737424526f7368616d626f00000000000000001200007872000e6a6176612e6c6"
                + "16e672e456e756d00000000000000001200007870740004524f434b";
        Roshambo value = Roshambo.ROCK;
        assertTrue(value.getClass() != Roshambo.class);
        new SerializationTester<Roshambo>(value, s).test();
    }

    enum Roshambo {
        ROCK {
            @Override public String toString() {
                return "rock!";
            }
        },
        PAPER,
        SCISSORS
    }
}
