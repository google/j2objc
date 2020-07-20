/*
 * Copyright (C) 2014 The Android Open Source Project
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
package libcore.java.nio.charset;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.Collections;
import java.util.Iterator;

/**
 * This class is registered as a charset provider by the META-INF in the libcore
 * tests jar. Since there isn't any convenient API to dynamically register and de-register
 * charset-providers, this class allows tests to plug in a delegate that lives for the
 * duration of the test.
 */
public final class SettableCharsetProvider extends CharsetProvider {
    private static CharsetProvider delegate;

    public static void setDelegate(CharsetProvider cp) {
        delegate = cp;
    }

    public static void clearDelegate() {
        delegate = null;
    }

    @Override
    public Iterator<Charset> charsets() {
        if (delegate != null) {
            return delegate.charsets();
        }

        return Collections.emptyIterator();
    }

    @Override
    public Charset charsetForName(String charsetName) {
        if (delegate != null) {
            return delegate.charsetForName(charsetName);
        }

        return null;
    }
}
