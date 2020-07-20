/*
 * Copyright (C) 2018 The Android Open Source Project
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

import libcore.api.CorePlatformApi;
import libcore.api.IntraCoreApi;

/**
 * Version code constants for Android releases.
 *
 * <p>Note: The constants are "public static final int" and are intended for use with annotations
 * so must stay compile-time resolvable and inline-able. They must match the values from
 * framework's android.os.Build.VERSION_CODES class.
 *
 * <p>Only historically fixed API levels should be included or abstract concepts like "CURRENT"
 * should be added. Do not predict API levels.
 *
 * {@hide}
 */
@CorePlatformApi
@IntraCoreApi
public class VersionCodes {

    private VersionCodes() {
    }

    /**
     * The version code for Android Oreo (API version 26).
     */
    @CorePlatformApi
    @IntraCoreApi
    public static final int O = 26;

    /**
     * The version code for Android Pie (API version 28).
     */
    @CorePlatformApi
    @IntraCoreApi
    public static final int P = 28;
}
