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
 * $Id: CharArrayWrapper.java 468655 2006-10-28 07:12:06Z minchau $
 */
package org.apache.xml.utils.res;

/**
 *
 * It is a mutable object to wrap the char[] used in
 * the contents of the XResourceBundle class
 */
public class CharArrayWrapper {
    private char[] m_char;
    
    public CharArrayWrapper(char[] arg){
        m_char = arg;
    }
    
    public char getChar(int index){
        return m_char[index];
    }
    
    public int getLength(){
        return m_char.length;
    }
}
