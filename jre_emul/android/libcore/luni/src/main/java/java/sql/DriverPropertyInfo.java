/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.sql;

/**
 * A class holding information about driver properties of a database connection.
 * This class is returned by the
 * {@link Driver#getPropertyInfo(String, java.util.Properties)} method and
 * allows for the advanced connection handling.
 */
public class DriverPropertyInfo {

    /**
     * If the value member can be chosen from a set of possible values, they are
     * contained here. Otherwise choices is {@code null}.
     */
    public String[] choices;

    /**
     * A description of the property. May be {@code null}.
     */
    public String description;

    /**
     * The name of the property.
     */
    public String name;

    /**
     * {@code true} when the value member must be provided during {@code
     * Driver.connect}. {@code false} otherwise.
     */
    public boolean required;

    /**
     * The current value associated with this property. It is depending on the
     * data gathered by the {@code getPropertyInfo} method, the general Java
     * environment and the driver's default values.
     */
    public String value;

    /**
     * Creates a {@code DriverPropertyInfo} instance with the supplied name and
     * value. Other class members take their default values.
     *
     * @param name
     *            The property name.
     * @param value
     *            The property value.
     */
    public DriverPropertyInfo(String name, String value) {
        this.name = name;
        this.value = value;
        this.choices = null;
        this.description = null;
        this.required = false;
    }
}
