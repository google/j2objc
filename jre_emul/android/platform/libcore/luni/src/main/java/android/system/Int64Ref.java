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

/**
 * A signed 64bit integer reference suitable for passing to lower-level system calls.
 */
public class Int64Ref {
    public long value;

    public Int64Ref(long value) {
        this.value = value;
    }

    @Override public String toString() {
        return Objects.toString(this);
    }
}
