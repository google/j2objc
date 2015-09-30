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

package java.util;

/**
 * Thrown when a locale subtag or field is not well formed.
 *
 * See {@link Locale} and {@link Locale.Builder}.
 *
 * @since 1.7
 */
public class IllformedLocaleException extends RuntimeException {

    private final int errorIndex;

    /**
     * Constructs a new instance with no detail message and an error index
     * of {@code -1}.
     */
    public IllformedLocaleException() {
        this(null, -1);
    }

    /**
     * Constructs a new instance with the specified error message.
     */
    public IllformedLocaleException(String message) {
        this(message, -1);
    }

    /**
     * Constructs a new instance with the specified error message and
     * error index.
     */
    public IllformedLocaleException(String message, int errorIndex) {
        super(message);
        this.errorIndex = errorIndex;
    }

    public int getErrorIndex() {
        return errorIndex;
    }
}
