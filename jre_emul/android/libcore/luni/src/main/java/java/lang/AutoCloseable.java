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

package java.lang;

/**
 * Defines an interface for classes that can (or need to) be closed once they
 * are not used any longer. Calling the {@code close} method releases resources
 * that the object holds.
 *
 * <p>A common pattern for using an {@code AutoCloseable} resource: <pre>   {@code
 *   Closable foo = new Foo();
 *   try {
 *      ...;
 *   } finally {
 *      foo.close();
 *   }
 * }</pre>
 *
 * @since 1.7
 * @hide 1.7
 */
public interface AutoCloseable {

    /**
     * Closes the object and release any system resources it holds.
     *
     * <p>Unless the implementing class specifies otherwise, it is an error to
     * call {@link #close} more than once.
     */
    void close() throws Exception;
}
