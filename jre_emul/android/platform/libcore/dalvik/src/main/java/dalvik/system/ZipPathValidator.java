/*
 * Copyright (C) 2022 The Android Open Source Project
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

package dalvik.system;

import libcore.util.NonNull;

import java.util.Objects;
import java.util.zip.ZipException;

/**
 * Enables validation of zip file entry paths to prevent exploitation of the path traversal
 * vulnerability, e.g. zip path entries containing ".." or "/". For more details, read
 * <a href="https://developer.android.com/topic/security/risks/zip-path-traversal">this</a>.
 * <p>
 * The default implementation accepts all zip file entry paths without raising any exceptions.
 * <p>
 * For custom validation rules, the core functionality should be implemented in a {@link Callback}
 * interface and that instance should be set in {@link #setCallback(Callback)}.
 * <p>
 * Existing validation could be set to a default one by calling {@link #clearCallback()}.
 */
public final class ZipPathValidator {

    /**
     * Default implementation of the {@link Callback} interface which accepts all paths.
     *
     * @hide
     */
    public static final Callback DEFAULT = new Callback() {};

    private static volatile Callback sInstance = DEFAULT;

    /**
     * Clears the current validation mechanism by setting the current callback instance to a default
     * validation.
     */
    public static void clearCallback() {
        sInstance = DEFAULT;
    }

    /**
     * Sets the current callback implementation for zip paths.
     * <p>
     * The provided callback should not perform IO or any blocking operations, but only perform path
     * validation. A typical implementation will validate String entries in a single pass and throw
     * a {@link ZipException} if the path contains potentially hazardous components such as "..".
     *
     * @param callback An instance of {@link Callback}'s implementation.
     */
    public static void setCallback(@NonNull Callback callback) {
        sInstance = Objects.requireNonNull(callback);
    }

    /**
     * Retrieves the current validator set by {@link #setCallback(Callback)}.
     *
     * @return Current callback.
     *
     * @hide
     */
    public static @NonNull Callback getInstance() {
        return sInstance;
    }

    /**
     * Returns true if the current validator is the default implementation {@link DEFAULT},
     * otherwise false.
     *
     * @hide
     */
    public static boolean isClear() {
        return sInstance.equals(DEFAULT);
    }

    /**
     * Interface that defines the core validation mechanism when accessing zip file entry paths.
     */
    public interface Callback {
        /**
         * Called to check the validity of the path of a zip entry. The default implementation
         * accepts all paths without raising any exceptions.
         * <p>
         * This method will be called by {@link java.util.zip.ZipInputStream#getNextEntry} or
         * {@link java.util.zip.ZipFile#ZipFile(String)}.
         *
         * @param path The name of the zip entry.
         * @throws ZipException If the zip entry is invalid depending on the implementation.
         */
        default void onZipEntryAccess(@NonNull String path) throws ZipException {}
    }

    private ZipPathValidator() {}
}