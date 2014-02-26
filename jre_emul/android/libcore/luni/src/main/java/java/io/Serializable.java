/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.io;

/**
 * Marks classes that can be serialized by {@link ObjectOutputStream} and
 * deserialized by {@link ObjectInputStream}.
 *
 * <p><strong>Warning:</strong> this interface limits how its implementing
 * classes can change in the future. By implementing {@code Serializable} you
 * expose your flexible in-memory implementation details as a rigid binary
 * representation. Simple code changes--like renaming private fields--are
 * not safe when the changed class is serializable.
 *
 * <h3>The Serialized Form</h3>
 * By default, the serialization mechanism encodes an object's class name, the
 * names of its non-transient fields (including non-public fields), and the
 * values of all of those fields. The output is an opaque sequence of bytes.
 * Those bytes can be decoded into a new, equivalent instance as long as the
 * decoder has compatible versions of the originating classes.
 *
 * <p>Changing the class name, field names or field types breaks serialization
 * compatibility and complicates interoperability between old and new versions
 * of the serializable class. Adding or removing fields also complicates
 * serialization between versions of a class because it requires your code to
 * cope with missing fields.
 *
 * <p>Every serializable class is assigned a version identifier called a {@code
 * serialVersionUID}. By default, this identifier is computed by hashing the
 * class declaration and its members. This identifier is included in the
 * serialized form so that version conflicts can be detected during
 * deserialization. If the local {@code serialVersionUID} differs from the
 * {@code serialVersionUID} in the serialized data, deserialization will fail
 * with an {@link InvalidClassException}.
 *
 * <p>You can avoid this failure by declaring an explicit {@code
 * serialVersionUID}. Declaring an explicit {@code serialVersionUID} tells the
 * serialization mechanism that the class is forward and backward compatible
 * with all versions that share that {@code serialVersionUID}. Declaring a
 * {@code serialVersionUID} looks like this: <pre>   {@code
 *
 *     private static final long serialVersionUID = 0L;
 * }</pre>
 * If you declare a {@code serialVersionUID}, you should increment it each
 * time your class changes incompatibly with the previous version. Typically
 * this is when you add, change or remove a non-transient field.
 *
 * <p>You can take control of your serialized form by implementing these two
 * methods with these exact signatures in your serializable classes:
 * <pre>   {@code
 *
 *   private void writeObject(java.io.ObjectOutputStream out)
 *       throws IOException {
 *     // write 'this' to 'out'...
 *   }
 *
 *   private void readObject(java.io.ObjectInputStream in)
 *       throws IOException, ClassNotFoundException {
 *     // populate the fields of 'this' from the data in 'in'...
 *   }
 * }</pre>
 * It is impossible to maintain serialization compatibility across a class name
 * change. For this reason, implementing {@code Serializable} in anonymous
 * inner classes is highly discouraged: simply reordering the members in the
 * file could change the generated class name and break serialization
 * compatibility.
 *
 * <p>You can exclude member fields from serialization by giving them the {@code
 * transient} modifier. Upon deserialization, the transient field's value will
 * be null, 0, or false according to its type.
 *
 * <h3>Implement Serializable Judiciously</h3>
 * Refer to <i>Effective Java</i>'s chapter on serialization for thorough
 * coverage of the serialization API. The book explains how to use this
 * interface without harming your application's maintainability.
 *
 * <h3>Recommended Alternatives</h3>
 * <strong>JSON</strong> is concise, human-readable and efficient. Android
 * includes both a {@link android.util.JsonReader streaming API} and a {@link
 * org.json.JSONObject tree API} to read and write JSON. Use a binding library
 * like <a href="http://code.google.com/p/google-gson/">GSON</a> to read and
 * write Java objects directly.
 */
public interface Serializable {
}
