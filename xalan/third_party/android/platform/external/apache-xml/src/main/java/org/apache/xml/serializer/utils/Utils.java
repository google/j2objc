/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: Utils.java 468654 2006-10-28 07:09:23Z minchau $
 */
package org.apache.xml.serializer.utils;

/**
 * This class contains utilities used by the serializer.
 * 
 * This class is not a public API, it is only public because it is
 * used by the serializer.
 * 
 * @xsl.usage internal
 */
public final class Utils
{
    /**
     * A singleton Messages object is used to load the 
     * given resource bundle just once, it is
     * used by multiple transformations as long as the JVM stays up.
     */
    public static final Messages messages= 
        new Messages(SerializerMessages.class.getName());
}
