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

package dalvik.system;

/**
 * A class encapsulating a StackTraceElement and lock state. This adds
 * critical thread state to the standard stack trace information, which
 * can be used to detect deadlocks at the Java level.
 *
 * @hide
 */
@libcore.api.CorePlatformApi
public class AnnotatedStackTraceElement {
    /**
     * The traditional StackTraceElement describing the Java stack frame.
     */
    private StackTraceElement stackTraceElement;

    /**
     * An array containing objects that are locked in this frame. May be null.
     */
    private Object[] heldLocks;

    /**
     * If this frame denotes the top of stack, <code>blockedOn<code> will hold
     * the object this thread is waiting to lock, or waiting on, if any. May be
     * null.
     */
    private Object blockedOn;

    // Internal allocation, only.
    private AnnotatedStackTraceElement() {
    }

    @libcore.api.CorePlatformApi
    public StackTraceElement getStackTraceElement() {
        return stackTraceElement;
    }

    @libcore.api.CorePlatformApi
    public Object[] getHeldLocks() {
        return heldLocks;
    }

    @libcore.api.CorePlatformApi
    public Object getBlockedOn() {
        return blockedOn;
    }
}
