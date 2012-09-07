/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple iOS version of java.lang.ThreadLocal.
 *
 * @author Pankaj Kakkar
 */
public class ThreadLocal<T> {

  private static final String KEY_PREFIX = "jre_tls_key";

  private static final AtomicInteger nextId = new AtomicInteger(0);

  private final String key = KEY_PREFIX + nextId.getAndIncrement();

  public T get() {
    return getNative();
  }

  protected T initialValue() {
    return null;
  }

  public void remove() {
    removeNative();
  }

  public void set(T value) {
    setNative(value);
  }

  private native T getNative() /*-{
    NSMutableDictionary *dict = [[NSThread currentThread] threadDictionary];
    id value = [dict objectForKey:self.key];
    if (value != nil) {
      // NSNull indicates that the value was explicitly set to null.
      return value == [NSNull null] ? nil : value;
    }

    id initialValue = [self initialValue];
    if (initialValue) {
      [dict setObject:initialValue forKey:self.key];
    } // else return nil to show this key's value hasn't been set.
    return initialValue;
  }-*/;

  private native void removeNative() /*-{
    [[[NSThread currentThread] threadDictionary] removeObjectForKey:self.key];
  }-*/;

  private native void setNative(T value) /*-{
    if (!value) {
      // Use NSNull singleton to show this value was explicitly set to null,
      // not just undefined.
      value = [NSNull null];
    }
    [[[NSThread currentThread] threadDictionary] setObject:value forKey:self.key];
  }-*/;
}
