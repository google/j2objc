/*
 * Copyright (C) 2017 The Android Open Source Project
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

package android.system;

import libcore.util.Objects;

import android.compat.annotation.UnsupportedAppUsage;

/**
 * @hide
 * A signed 32bit integer reference suitable for passing to lower-level system calls.
 */
@libcore.api.CorePlatformApi
public class Int32Ref {
    @UnsupportedAppUsage
    @libcore.api.CorePlatformApi
    public int value;

    @libcore.api.CorePlatformApi
    public Int32Ref(int value) {
        this.value = value;
    }

    @Override public String toString() {
        return Objects.toString(this);
    }
}
