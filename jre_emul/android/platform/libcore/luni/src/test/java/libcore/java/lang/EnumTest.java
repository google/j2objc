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
import libcore.libcore.util.SerializationTester;

import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;

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

    public static final CountDownLatch cdl = new CountDownLatch(1);

    public static enum EnumA {
        A, B
    }

    public static class ToBeLoaded {
        public static final Object myValue;
        public static final EnumSet<EnumA> mySet;


        static {
            try {
                cdl.await();
            } catch (InterruptedException ie) {
                fail();
            }

            myValue = new String("xyz");
            // This is the key to reproducing the deadlock. This call will result in a call
            // to Enum.getSharedConstants, which will initialize classes while holding a lock.
            mySet = EnumSet.noneOf(EnumA.class);
        }
    }

    public static enum ComplicatedEnum {
        A(ToBeLoaded.myValue);

        private final Object value;
        ComplicatedEnum(Object v) {
            value = v;
        }
    }

    public void testDeadlock() {
        Thread t1 = new Thread() {
            public void run() {
                System.out.println(ToBeLoaded.myValue);
            }
        };
        // Should be stuck waiting on the latch.
        t1.start();

        Thread t2 = new Thread() {
            public void run() {
                // See matching call in ToBeLoaded.<clinit>.
                System.out.println(EnumSet.noneOf(ComplicatedEnum.class));
            }
        };

        // Should be blocked waiting for the initialization of ComplicatedEnum.
        t2.start();

        // Unblock the countdown latch so that both threads can make progress.
        cdl.countDown();

        // This test has no positive or negative assertions. If it fails, it will fail because
        // it timed out.
        try {
            t1.join();
        } catch (InterruptedException ie) {
            fail();
        }

        try {
            t2.join();
        } catch (InterruptedException ie) {
            fail();
        }
    }
}
