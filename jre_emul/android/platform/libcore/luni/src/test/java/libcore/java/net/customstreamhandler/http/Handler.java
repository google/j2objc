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

package libcore.java.net.customstreamhandler.http;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * This specially-named class is created reflectively by {@link URL}. For the
 * test to be effective, its name must be "Handler" and parent package name must
 * be "http".
 */
public final class Handler extends URLStreamHandler {
    @Override protected URLConnection openConnection(URL url) throws IOException {
        return new HandlerURLConnection(url);
    }

    public static class HandlerURLConnection extends URLConnection {
        protected HandlerURLConnection(URL url) {
            super(url);
        }

        @Override public void connect() throws IOException {
        }
    }
}
