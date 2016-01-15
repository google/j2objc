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
 * $Id: ElemEmpty.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;


/**
 * Simple empty elem to push on the stack when nothing
 * else got pushed, so that pop() works correctly.
 * @xsl.usage internal
 */
public class ElemEmpty extends ElemTemplateElement
{
    static final long serialVersionUID = 7544753713671472252L;

  /**
   * Constructor ElemEmpty
   *
   */
  public ElemEmpty(){}
}
