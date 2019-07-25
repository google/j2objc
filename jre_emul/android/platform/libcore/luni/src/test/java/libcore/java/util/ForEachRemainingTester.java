/*
 * Copyright (C) 2016 The Android Open Source Project
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
 * limitations under the License
 */

package libcore.java.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Supplier;

import static junit.framework.Assert.*;

/**
 * This class contains general purpose test cases for the {@code forEachRemaining} method inherited
 * from {@code Iterator}.
 */
public class ForEachRemainingTester {

    public static <T> void runTests(Supplier<? extends Collection<T>> collectionSupplier,
            T[] initialData) throws Exception {
        test_forEachRemaining(collectionSupplier.get(), initialData);
        test_forEachRemaining_NPE(collectionSupplier.get(), initialData);
        test_forEachRemaining_CME(collectionSupplier.get(), initialData);

        Collection<T> collection = collectionSupplier.get();
        if (collection instanceof List) {
            List<T> asList = (List<T>) collection;
            test_forEachRemaining_list(asList, initialData);
            test_forEachRemaining_NPE_list(asList, initialData);
            test_forEachRemaining_CME_list(asList, initialData);
        }
    }

    public static <T> void test_forEachRemaining_list(List<T> collection, T[] initialData)
            throws Exception {
        test_forEachRemaining(collection, initialData);

        ArrayList<T> recorder = new ArrayList<>();
        ListIterator<T> lit = collection.listIterator(1);

        lit.forEachRemaining((T i) -> recorder.add(i));
        if (initialData.length > 0) {
            assertEquals(initialData.length - 1, recorder.size());
            for (int i = 1; i < initialData.length; i++){
                assertEquals(initialData[i], recorder.get(i - 1));
            }
        }
    }

    public static <T> void test_forEachRemaining(Collection<T> collection, T[] initialData)
            throws Exception {
        collection.addAll(Arrays.asList(initialData));

        ArrayList<T> recorder = new ArrayList<>();
        collection.iterator().forEachRemaining((T i) -> recorder.add(i));
        // Note that the collection may not override equals and hashCode.
        assertEquals(new ArrayList<T>(collection), recorder);

        recorder.clear();
        Iterator<T> it = collection.iterator();
        it.next();

        it.forEachRemaining((T i) -> recorder.add(i));
        if (initialData.length > 0) {
            assertEquals(initialData.length - 1, recorder.size());
            for (int i = 1; i < initialData.length; i++){
                assertEquals(initialData[i], recorder.get(i - 1));
            }
        }
    }

    public static <T> void test_forEachRemaining_NPE(Collection<T> collection, T[] initialData)
            throws Exception {
        try {
            collection.iterator().forEachRemaining(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public static <T> void test_forEachRemaining_CME(Collection<T> collection, T[] initialData)
            throws Exception {
        collection.add(initialData[0]);
        try {
            collection.iterator().forEachRemaining(i -> collection.add(i));
            fail();
        } catch (ConcurrentModificationException expected) {
        }
    }

    public static <T> void test_forEachRemaining_NPE_list(Collection<T> collection, T[] initialData)
            throws Exception {
        try {
            collection.iterator().forEachRemaining(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public static <T> void test_forEachRemaining_CME_list(Collection<T> collection, T[] initialData)
            throws Exception {
        collection.add(initialData[0]);
        try {
            collection.iterator().forEachRemaining(i -> collection.add(i));
            fail();
        } catch (ConcurrentModificationException expected) {
        }
    }
}
