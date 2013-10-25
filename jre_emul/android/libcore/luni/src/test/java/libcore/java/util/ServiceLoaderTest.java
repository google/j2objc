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

package libcore.java.util;

import java.util.Iterator;
import java.util.ServiceLoader;

public class ServiceLoaderTest extends junit.framework.TestCase {
    public static interface UnimplementedInterface { }
    public void test_noImplementations() {
        assertFalse(ServiceLoader.load(UnimplementedInterface.class).iterator().hasNext());
    }

    public static class Impl1 implements ServiceLoaderTestInterface { }
    public static class Impl2 implements ServiceLoaderTestInterface { }
    public void test_implementations() {
        ServiceLoader<ServiceLoaderTestInterface> loader = ServiceLoader.load(ServiceLoaderTestInterface.class);
        Iterator<ServiceLoaderTestInterface> it = loader.iterator();
        assertTrue(it.hasNext());
        assertTrue(it.next() instanceof Impl1);
        assertTrue(it.hasNext());
        assertTrue(it.next() instanceof Impl2);
        assertFalse(it.hasNext());
    }
}
