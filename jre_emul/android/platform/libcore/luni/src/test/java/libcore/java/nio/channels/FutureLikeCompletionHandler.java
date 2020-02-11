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
 * limitations under the License.
 */

package libcore.java.nio.channels;

import java.nio.channels.CompletionHandler;

/** A CompletionHandler that behaves like a Future and enables compact, single-threaded tests. */
public class FutureLikeCompletionHandler<V> implements CompletionHandler<V, Object> {
    Throwable e;
    boolean done;
    V result;
    Object attachment;

    public void completed(V result, Object attachment) {
        synchronized (this) {
            if (done) {
                e = new IllegalStateException("CompletionHandler used twice");
            }
            this.result = result;
            this.done = true;
            this.attachment = attachment;
            this.notifyAll();
        }
    }

    public void failed(Throwable exc, Object attachment) {
        synchronized (this) {
            if (done) {
                e = new IllegalStateException("CompletionHandler used twice");
            }
            this.e = exc;
            this.done = true;
            this.attachment = attachment;
            this.notifyAll();
        }
    }

    V get(long timeoutMiliseconds) throws Throwable {
        synchronized (this) {
            while (!done) {
                wait(timeoutMiliseconds);
            }
            if (e != null) {
                throw e;
            }
            return result;
        }
    }

    public Object getAttachment() {
        synchronized (this) {
            return attachment;
        }
    }
}
