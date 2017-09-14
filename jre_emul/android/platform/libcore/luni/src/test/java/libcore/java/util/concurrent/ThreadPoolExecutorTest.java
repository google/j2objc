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

package libcore.java.util.concurrent;

import junit.framework.TestCase;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorTest extends TestCase {

    // http://b/27702221
    public void testCorePoolSizeGreaterThanMax() {
        ThreadPoolExecutor tp = new ThreadPoolExecutor(
                1 /* core pool size */, 1 /* max pool size */,
                1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10));

        // It should be illegal to set a core pool size that's larger than the max
        // pool size but apps have been allowed to get away with it so far. The pattern
        // below occurs in a commonly used library. Note that the executor is in a sane
        // state at the end of both method calls.
        tp.setCorePoolSize(5);
        tp.setMaximumPoolSize(5);
    }
}
