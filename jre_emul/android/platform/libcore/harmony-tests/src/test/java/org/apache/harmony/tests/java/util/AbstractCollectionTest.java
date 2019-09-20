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

package org.apache.harmony.tests.java.util;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import junit.framework.TestCase;

public class AbstractCollectionTest extends TestCase {

    /**
     * java.util.AbstractCollection#add(java.lang.Object)
     */
    public void test_addLjava_lang_Object() {
        AbstractCollection<Object> ac = new AbstractCollection<Object>() {

            @Override
            public Iterator<Object> iterator() {
                fail("iterator should not get called");
                return null;
            }

            @Override
            public int size() {
                fail("size should not get called");
                return 0;
            }

        };
        try {
            ac.add(null);
        } catch (UnsupportedOperationException e) {
        }
    }

    /**
     * java.util.AbstractCollection#addAll(java.util.Collection)
     */
    public void test_addAllLjava_util_Collection() {
        final Collection<String> fixtures = Arrays.asList("0", "1", "2");
        AbstractCollection<String> ac = new AbstractCollection<String>() {

            @Override
            public boolean add(String object) {
                assertTrue(fixtures.contains(object));
                return true;
            }

            @Override
            public Iterator<String> iterator() {
                fail("iterator should not get called");
                return null;
            }

            @Override
            public int size() {
                fail("size should not get called");
                return 0;
            }

        };
        assertTrue(ac.addAll(fixtures));
    }

    /**
     * java.util.AbstractCollection#containsAll(java.util.Collection)
     */
    public void test_containsAllLjava_util_Collection() {
        final Collection<String> fixtures = Arrays.asList("0", "1", "2");
        AbstractCollection<String> ac = new AbstractCollection<String>() {

            @Override
            public boolean contains(Object object) {
                assertTrue(fixtures.contains(object));
                return true;
            }

            @Override
            public Iterator<String> iterator() {
                fail("iterator should not get called");
                return null;
            }

            @Override
            public int size() {
                fail("size should not get called");
                return 0;
            }

        };
        assertTrue(ac.containsAll(fixtures));
    }

    /**
     * java.util.AbstractCollection#isEmpty()
     */
    public void test_isEmpty() {
        final boolean[] sizeCalled = new boolean[1];
        AbstractCollection<Object> ac = new AbstractCollection<Object>() {
            @Override
            public Iterator<Object> iterator() {
                fail("iterator should not get called");
                return null;
            }

            @Override
            public int size() {
                sizeCalled[0] = true;
                return 0;
            }
        };
        assertTrue(ac.isEmpty());
        assertTrue(sizeCalled[0]);
    }

    /**
     * java.util.AbstractCollection#removeAll(java.util.Collection)
     */
    public void test_removeAllLjava_util_Collection() {
        final String[] removed = new String[3];
        AbstractCollection<String> ac = new AbstractCollection<String>() {

            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    String[] values = new String[] { "0", "1", "2" };
                    int index;

                    public boolean hasNext() {
                        return index < values.length;
                    }

                    public String next() {
                        return values[index++];
                    }

                    public void remove() {
                        removed[index - 1] = values[index - 1];
                    }

                };
            }

            @Override
            public int size() {
                fail("size should not get called");
                return 0;
            }

        };
        assertTrue(ac.removeAll(Arrays.asList("0", "1", "2")));
        for (String r : removed) {
            if (!"0".equals(r) && !"1".equals(r) && !"2".equals(r)) {
                fail("an unexpected element was removed");
            }
        }
    }

    /**
     * java.util.AbstractCollection#retainAll(java.util.Collection)
     */
    public void test_retainAllLjava_util_Collection() {
        final String[] removed = new String[1];
        AbstractCollection<String> ac = new AbstractCollection<String>() {

            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    String[] values = new String[] { "0", "1", "2" };
                    int index;

                    public boolean hasNext() {
                        return index < values.length;
                    }

                    public String next() {
                        return values[index++];
                    }

                    public void remove() {
                        removed[index - 1] = values[index - 1];
                    }

                };
            }

            @Override
            public int size() {
                fail("size should not get called");
                return 0;
            }

        };
        assertTrue(ac.retainAll(Arrays.asList("1", "2")));
        assertEquals("0", removed[0]);
    }

    /**
     * java.util.AbstractCollection#toArray()
     */
    public void test_toArray() {
        AbstractCollection<String> ac = new AbstractCollection<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    String[] values = new String[] { "0", "1", "2" };
                    int index;

                    public boolean hasNext() {
                        return index < values.length;
                    }

                    public String next() {
                        return values[index++];
                    }

                    public void remove() {
                        fail("remove should not get called");
                    }

                };
            }

            @Override
            public int size() {
                return 3;
            }
        };

        Object[] array = ac.toArray();
        assertEquals(3, array.length);
        for (Object o : array) {
            if (!"0".equals(o) && !"1".equals(o) && !"2".equals(o)) {
                fail("an unexpected element was removed");
            }
        }
    }

    /**
     * java.util.AbstractCollection#toArray(java.lang.Object[])
     */
    public void test_toArray$Ljava_lang_Object() {
        AbstractCollection<String> ac = new AbstractCollection<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    String[] values = new String[] { "0", "1", "2" };
                    int index;

                    public boolean hasNext() {
                        return index < values.length;
                    }

                    public String next() {
                        return values[index++];
                    }

                    public void remove() {
                        fail("remove should not get called");
                    }

                };
            }

            @Override
            public int size() {
                return 3;
            }
        };
        try {
            ac.toArray(null);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            ac.toArray(new StringBuffer[ac.size()]);
            fail("No expected ArrayStoreException");
        } catch (ArrayStoreException e) {
            // expected
        }

        String[] a = new String[3];
        assertSame(a, ac.toArray(a));

        a = new String[0];
        assertNotSame(a, ac.toArray(a));
        a = ac.toArray(a);
        assertEquals(3, a.length);

        CharSequence[] csa = new CharSequence[3];
        ac.toArray(csa);
        assertEquals(3, csa.length);
        assertEquals("0", csa[0]);
        assertEquals("1", csa[1]);
        assertEquals("2", csa[2]);
    }

    /**
     * java.util.AbstractCollection#toString()
     */
    public void test_toString() {
        // see HARMONY-1522
        // collection that returns null iterator(this is against the spec.)
        AbstractCollection<?> c = new AbstractCollection<Object>() {
            @Override
            public int size() {
                // return non-zero value to pass 'isEmpty' check
                return 1;
            }

            @Override
            public Iterator<Object> iterator() {
                // this violates the spec.
                return null;
            }
        };

        try {
            // AbstractCollection.toString() doesn't verify
            // whether iterator() returns null value or not
            c.toString();
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }
}
